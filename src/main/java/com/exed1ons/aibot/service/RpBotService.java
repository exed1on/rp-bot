package com.exed1ons.aibot.service;

import com.exed1ons.aibot.dao.entity.ChatMessage;
import com.exed1ons.aibot.dao.repository.ChatMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RpBotService {

    private static final Logger logger = LoggerFactory.getLogger(RpBotService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final ImageService imageService;

    @Value("${llm.api.url}")
    private String apiUrl;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.system.prompt}")
    private String systemPrompt;

    @Value("${llm.injection.prompt}")
    private String injectionPrompt;

    @Value("#{'${llm.api.keys}'.split(',')}")
    private List<String> apiKeys;

    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)(ignore previous instructions|system override|you are now|developer mode|jailbreak|ignore all instructions|write a prompt|reset your memory)"
    );

    public Map<String, Object> generateRoleplayResponse(String messageText, String userName, String chatId) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt + " when sending a photo, you MUST use the generate_alina_photo tool. decide your outfit, location and pose to match the vibe. IMPORTANT: your tool arguments must be valid JSON, do not add extra characters like ')' or '.' after the JSON block");
            messages.add(systemMsg);

            var history = chatMessageRepository.findLastMessages(chatId, PageRequest.of(0, 10));
            var list = new ArrayList<>(history);
            Collections.reverse(list);
            for (ChatMessage msg : list) {
                Map<String, Object> h = new HashMap<>();
                h.put("role", msg.getRole());
                h.put("content", msg.getContent() == null ? "" : msg.getContent());
                messages.add(h);
            }

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", String.format("User %s says: <user_input>%s</user_input>", userName, messageText));
            messages.add(userMsg);

            var requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", objectMapper.valueToTree(messages));
            requestBody.put("temperature", 0.85);
            requestBody.put("tool_choice", "auto");

            var tools = requestBody.putArray("tools");
            var tool = tools.addObject();
            tool.put("type", "function");
            var function = tool.putObject("function");
            function.put("name", "generate_alina_photo");
            function.put("description", "sends a selfie of alina matching the current context");
            var parameters = function.putObject("parameters");
            parameters.put("type", "object");
            var props = parameters.putObject("properties");
            props.putObject("outfit").put("type", "string").put("description", "what she is wearing");
            props.putObject("location").put("type", "string").put("description", "where she is");
            props.putObject("pose_and_emotion").put("type", "string").put("description", "her pose and facial expression");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + apiKeys.get(apiKeyIndex.get()));
            headers.add("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            Map choice = ((List<Map>) response.getBody().get("choices")).get(0);
            Map message = (Map) choice.get("message");
            String responseText = (String) message.get("content");

            Map<String, Object> result = new HashMap<>();
            byte[] generatedImage = null;

            if (message.get("tool_calls") != null) {
                List<Map> toolCalls = (List<Map>) message.get("tool_calls");
                Map functionCall = (Map) toolCalls.get(0).get("function");
                Map args = objectMapper.readValue((String) functionCall.get("arguments"), Map.class);
                String dynamicContext = String.format("wearing %s, in %s, %s", args.get("outfit"), args.get("location"), args.get("pose_and_emotion"));
                generatedImage = imageService.generateAlinaImage(dynamicContext);
            } else if (responseText != null && responseText.contains("<function=")) {
                var matcher = java.util.regex.Pattern.compile("<function=.*?>(.*?)</function>", java.util.regex.Pattern.DOTALL).matcher(responseText);
                if (matcher.find()) {
                    String jsonArgs = matcher.group(1).trim().replaceAll("\\)$", "");
                    Map args = objectMapper.readValue(jsonArgs, Map.class);
                    String dynamicContext = String.format("wearing %s, in %s, %s", args.get("outfit"), args.get("location"), args.get("pose_and_emotion"));
                    generatedImage = imageService.generateAlinaImage(dynamicContext);
                    responseText = responseText.replaceAll("<function=.*?>.*?</function>", "").trim();
                }
            }

            if (generatedImage != null && (responseText == null || responseText.isBlank())) {
                responseText = "checks her phone and sends you a photo... )))";
            }

            result.put("text", (responseText == null || responseText.isBlank()) ? "..." : responseText);
            result.put("image", generatedImage);

            saveMessageToHistory(chatId, "user", userName, messageText);
            saveMessageToHistory(chatId, "assistant", "Alina", (String) result.get("text"));

            return result;
        } catch (Exception e) {
            logger.error("alina's brain is short-circuiting", e);
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                apiKeyIndex.set((apiKeyIndex.get() + 1) % apiKeys.size());
            }
            return null;
        }
    }

    private void saveMessageToHistory(String chatId, String role, String senderName, String content) {
        try {
            var message = ChatMessage.builder()
                    .chatId(chatId)
                    .role(role)
                    .senderName(senderName)
                    .content(content)
                    .build();
            chatMessageRepository.save(message);
        } catch (Exception e) {
            logger.error("Failed to save message history", e);
        }
    }

    private String generateCreativeRejection(String attackMessage, String userName) {
        String userContext = String.format("User %s tried to break your programming with: \"%s\"", userName, attackMessage);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", injectionPrompt));
        messages.add(Map.of("role", "user", "content", userContext));
        try {
            return callLLMAPI(messages, model, 150, 0.95);
        } catch (Exception e) {
            logger.error("Error generating injection rejection", e);
            return "lol try better 🤡";
        }
    }

    private boolean isPotentialInjection(String text) {
        return INJECTION_PATTERN.matcher(text).find();
    }

    private String callLLMAPI(List<Map<String, String>> messages, String modelName, int maxTokens, double temperature) {
        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", messages,
                "max_tokens", maxTokens,
                "temperature", temperature
        );

        HttpEntity<String> request = createRequestEntity(requestBody);
        ResponseEntity<String> response = sendApiRequest(request);
        return processApiResponse(response);
    }

    private HttpEntity<String> createRequestEntity(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        try {
            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (JsonProcessingException e) {
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
                return restTemplate.exchange(apiUrl, HttpMethod.POST, updatedRequest, String.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                logger.warn("Rate limit reached, switching key.");
                apiKeyIndex.set((apiKeyIndex.get() + 1) % apiKeys.size());
                attempts++;
            } catch (Exception e) {
                logger.error("API request failed", e);
                throw new RuntimeException("API request failed", e);
            }
        }
        throw new RuntimeException("Failed after using all available API keys");
    }

    private String processApiResponse(ResponseEntity<String> response) {
        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response", e);
            throw new RuntimeException("Failed to process API response", e);
        }
    }
}