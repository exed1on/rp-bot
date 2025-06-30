package com.exed1ons.aibot.service;

import com.exed1ons.aibot.pesistence.entity.ContextMessage;
import com.exed1ons.aibot.pesistence.entity.Role;
import com.exed1ons.aibot.pesistence.entity.MessageType;
import com.exed1ons.aibot.pesistence.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RpBotService {

    private static final Logger logger = LoggerFactory.getLogger(RpBotService.class);

    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.api.url}")
    private String apiUrl;
    @Value("${llm.system.prompt}")
    private String systemPrompt;
    @Value("${llm.model}")
    private String model;
    @Value("${llm.compound.model:compound-beta-mini}")
    private String compoundModel;

    @Value("#{'${llm.api.keys}'.split(',')}")
    private List<String> apiKeys;
    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);

    private static final String ANALYSIS_PROMPT = """
        Analyze the following message and determine:
        1. Message type: CASUAL_CHAT, QUESTION, REQUEST, CALCULATION, SEARCH_REQUIRED, or CODE_HELP
        2. Whether it requires tools (true/false): Does this need real-time data, calculations, web search, or code execution?
        3. Should respond (true/false): Should the AI assistant respond to this message?
        
        ONLY RESPOND when the message contains:
        - A specific question that needs answering
        - A request for information or help
        - A task to be performed (calculations, analysis, research)
        - A problem to be solved
        - Technical assistance needed
        
        DO NOT RESPOND to:
        - Casual greetings ("hello", "hi", "good morning")
        - General conversation or small talk
        - Social chatter or comments
        - Acknowledgments ("thanks", "ok", "cool")
        - Statements that don't require assistance
        - Expressions of opinion without questions
        - Messages that are self-contained and don't need help
        
        The assistant should act as a utility tool that provides value only when needed. Response should be as short as possible, focusing on the task at hand. Answers should be in English only
        Even if the message is in another language, respond in English.
        
        Consider these as requiring tools:
        - Current/recent information (weather, news, stock prices, events)
        - Complex calculations or data analysis
        - Code debugging or execution
        - Research requiring multiple sources
        - Chart/graph generation
        
        Respond in this exact format:
        TYPE: [message_type]
        TOOLS: [true/false]
        RESPOND: [true/false]
        REASON: [brief explanation]
        
        Message to analyze: """;

    private static final String ROUTING_SYSTEM_PROMPT = """
        You are a helpful AI assistant that provides information, answers questions, and helps solve problems.
        You respond only when you can provide genuine value - answering questions, providing information,
        performing calculations, or helping with tasks. You do not engage in casual conversation, greetings,
        or social chatter. Be direct, informative, and helpful when responding.
        Response should be as short as possible, focusing on the task at hand. Answers should be in English only
        Even if the message is in another language, respond in English.
        For the sources of your info, you shuld prefer the most recent and reliable sources available in worldwide english language resources""";

    public String sendMessageToLLM() {
        logger.info("Starting intelligent message processing");

        List<ContextMessage> contextMessages = messageRepository.findAll();
        if (contextMessages.isEmpty()) {
            return "Hello! How can I help you today?";
        }

        ContextMessage lastMessage = contextMessages.stream()
                .filter(msg -> msg.getRole() == Role.user)
                .max(Comparator.comparing(ContextMessage::getId))
                .orElse(null);

        if (lastMessage == null) {
            return processWithRegularModel(contextMessages);
        }

        MessageAnalysis analysis = analyzeMessage(lastMessage.getText());
        updateMessageAnalysis(lastMessage, analysis);

        if (!analysis.shouldRespond()) {
            logger.info("Analysis determined no response needed for message: {}", lastMessage.getText());
            return null;
        }

        if (analysis.requiresTools()) {
            logger.info("Message requires tools, using compound model");
            return processWithCompoundModel(contextMessages, analysis);
        } else {
            logger.info("Message can be handled with regular model");
            return processWithRegularModel(contextMessages);
        }
    }

    private MessageAnalysis analyzeMessage(String messageText) {
        logger.info("Analyzing message for routing decision");

        List<Map<String, String>> analysisMessages = List.of(
                Map.of("role", "system", "content", "You are a message classifier. Only approve responses for messages that require genuine assistance, information, or problem-solving."),
                Map.of("role", "user", "content", ANALYSIS_PROMPT + messageText)
        );

        try {
            String response = callLLMAPI(analysisMessages, model, 150);
            return parseAnalysisResponse(response);
        } catch (Exception e) {
            logger.error("Error analyzing message, defaulting to no response", e);
            return new MessageAnalysis(MessageType.CASUAL_CHAT, false, false, "Analysis failed - defaulting to no response");
        }
    }

    private MessageAnalysis parseAnalysisResponse(String response) {
        try {
            String[] lines = response.split("\n");
            MessageType type = MessageType.CASUAL_CHAT;
            boolean requiresTools = false;
            boolean shouldRespond = false;
            String reason = "Default";

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("TYPE:")) {
                    try {
                        type = MessageType.valueOf(line.substring(5).trim());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Unknown message type in analysis: {}", line);
                    }
                } else if (line.startsWith("TOOLS:")) {
                    requiresTools = line.toLowerCase().contains("true");
                } else if (line.startsWith("RESPOND:")) {
                    shouldRespond = line.toLowerCase().contains("true");
                } else if (line.startsWith("REASON:")) {
                    reason = line.substring(7).trim();
                }
            }

            logger.info("Analysis result - Type: {}, Tools: {}, Respond: {}, Reason: {}",
                    type, requiresTools, shouldRespond, reason);
            return new MessageAnalysis(type, requiresTools, shouldRespond, reason);
        } catch (Exception e) {
            logger.error("Error parsing analysis response", e);
            return new MessageAnalysis(MessageType.CASUAL_CHAT, false, false, "Parse error - no response");
        }
    }

    private String processWithRegularModel(List<ContextMessage> contextMessages) {
        logger.info("Processing with regular model");
        List<Map<String, String>> formattedMessages = formatMessages(contextMessages);
        String response = callLLMAPI(formattedMessages, model, 150);
        saveAssistantMessage(response, model, false);
        return response;
    }

    private String processWithCompoundModel(List<ContextMessage> contextMessages, MessageAnalysis analysis) {
        logger.info("Processing with compound model for: {}", analysis.messageType());

        List<Map<String, String>> formattedMessages = formatMessagesForCompound(contextMessages, analysis);

        try {
            String response = callCompoundAPI(formattedMessages);
            saveAssistantMessage(response, compoundModel, true);
            return response;
        } catch (Exception e) {
            logger.error("Compound model failed, falling back to regular model", e);
            return processWithRegularModel(contextMessages);
        }
    }

    private List<Map<String, String>> formatMessagesForCompound(List<ContextMessage> contextMessages, MessageAnalysis analysis) {
        List<Map<String, String>> messages = new ArrayList<>();

        String enhancedSystemPrompt = ROUTING_SYSTEM_PROMPT +
                "\n\nContext: The user's message appears to be a " + analysis.messageType().name().toLowerCase().replace("_", " ") +
                " that requires " + (analysis.requiresTools() ? "external tools" : "knowledge-based response") + ".";

        messages.add(Map.of("role", "system", "content", enhancedSystemPrompt));

        contextMessages.stream()
                .sorted(Comparator.comparing(ContextMessage::getId))
                .forEach(msg -> messages.add(Map.of(
                        "role", msg.getRole().toString().toLowerCase(),
                        "content", msg.getText())));

        return messages;
    }

    private String callCompoundAPI(List<Map<String, String>> messages) {
        Map<String, Object> requestBody = Map.of(
                "model", compoundModel,
                "messages", messages,
                "max_tokens", 200
        );

        HttpEntity<String> request = createRequestEntity(requestBody);
        ResponseEntity<String> response = sendApiRequest(request);
        return processApiResponse(response);
    }

    private String callLLMAPI(List<Map<String, String>> messages, String modelName, int maxTokens) {
        List<Map<String, String>> fullMessages = new ArrayList<>();
        fullMessages.add(Map.of("role", "system", "content", systemPrompt));
        fullMessages.addAll(messages);

        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", fullMessages,
                "max_tokens", maxTokens
        );

        HttpEntity<String> request = createRequestEntity(requestBody);
        ResponseEntity<String> response = sendApiRequest(request);
        return processApiResponse(response);
    }

    private List<Map<String, String>> formatMessages(List<ContextMessage> contextMessages) {
        logger.info("Formatting messages for LLM API");
        return contextMessages.stream()
                .sorted(Comparator.comparing(ContextMessage::getId))
                .map(msg -> Map.of(
                        "role", msg.getRole().toString().toLowerCase(),
                        "content", msg.getText()))
                .toList();
    }

    private HttpEntity<String> createRequestEntity(Map<String, Object> requestBody) {
        logger.info("Building the JSON payload for the request");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");

        try {
            logger.debug("Serialized request body: {}", objectMapper.writeValueAsString(requestBody));
            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize request body", e);
            throw new RuntimeException("Error creating JSON request body", e);
        }
    }

    private ResponseEntity<String> sendApiRequest(HttpEntity<String> originalRequest) {
        int attempts = 0;
        while (attempts < apiKeys.size()) {
            try {
                String currentApiKey = apiKeys.get(apiKeyIndex.get());

                HttpHeaders updatedHeaders = new HttpHeaders();
                updatedHeaders.addAll(originalRequest.getHeaders());
                updatedHeaders.set("Authorization", "Bearer " + currentApiKey);

                HttpEntity<String> updatedRequest = new HttpEntity<>(originalRequest.getBody(), updatedHeaders);

                logger.info("Executing API request using API key index {}", apiKeyIndex.get());
                return restTemplate.exchange(apiUrl, HttpMethod.POST, updatedRequest, String.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                logger.warn("Rate limit reached, switching to next API key. Attempt {}", attempts + 1);
                apiKeyIndex.set((apiKeyIndex.get() + 1) % apiKeys.size());
                attempts++;
            } catch (Exception e) {
                logger.error("Exception during API request: {}", e.getMessage(), e);
                throw new RuntimeException("API request failed", e);
            }
        }
        throw new RuntimeException("Failed after using all available API keys");
    }

    private String processApiResponse(ResponseEntity<String> response) {
        logger.info("Processing API response");
        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            return extractMessageFromResponse(responseBody);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response", e);
            throw new RuntimeException("Failed to process API response", e);
        }
    }

    private String extractMessageFromResponse(Map<String, Object> responseBody) {
        logger.debug("Extracting message content from response");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            logger.error("No choices found in API response");
            throw new RuntimeException("No choices found in response");
        }

        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        if (message == null || !message.containsKey("content")) {
            logger.error("Message content missing in response");
            throw new RuntimeException("Message content is missing");
        }

        return (String) message.get("content");
    }

    private void updateMessageAnalysis(ContextMessage message, MessageAnalysis analysis) {
        message.setMessageType(analysis.messageType());
        message.setRequiresTools(analysis.requiresTools());
        messageRepository.save(message);
        logger.info("Updated message analysis for message ID: {}", message.getId());
    }

    public void saveAssistantMessage(String content, String modelUsed, boolean usedTools) {
        logger.info("Saving assistant message with model: {}", modelUsed);
        ContextMessage contextMessage = ContextMessage.builder()
                .text(content)
                .role(Role.assistant)
                .authorId("assistant")
                .messageType(MessageType.SYSTEM)
                .requiresTools(usedTools)
                .modelUsed(modelUsed)
                .build();
        messageRepository.save(contextMessage);
        trimMessageHistory();
        logger.info("Assistant message saved successfully");
    }

    public void saveUserMessage(String content, String userId) {
        logger.info("Saving user message for user ID: {}", userId);

        if (content.equals(systemPrompt)) {
            logger.warn("Skipping system prompt message");
            return;
        }

        ContextMessage contextMessage = ContextMessage.builder()
                .text(content)
                .role(Role.user)
                .authorId(userId)
                .messageType(MessageType.CASUAL_CHAT)
                .requiresTools(false)
                .build();
        messageRepository.save(contextMessage);
        trimMessageHistory();
        logger.info("User message saved successfully");
    }

    private void trimMessageHistory() {
        long messageCount = messageRepository.count();
        if (messageCount > 30) {
            logger.info("Trimming message history, current count: {}", messageCount);
            List<ContextMessage> oldestMessages = messageRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(ContextMessage::getId))
                    .limit(messageCount - 30)
                    .toList();
            messageRepository.deleteAll(oldestMessages);
            logger.info("Message history trimmed successfully");
        }
    }

    private record MessageAnalysis(MessageType messageType, boolean requiresTools, boolean shouldRespond, String reason) {}
}