package com.exed1ons.aibot.service;

import com.exed1ons.aibot.pesistence.entity.ContextMessage;
import com.exed1ons.aibot.pesistence.entity.Role;
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

    @Value("#{'${llm.api.keys}'.split(',')}")
    private List<String> apiKeys;
    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);

    public String sendMessageToLLM() {
        logger.info("Fetching all context messages from the repository.");
        List<ContextMessage> contextMessages = messageRepository.findAll();
        List<Map<String, String>> formattedMessages = formatMessages(contextMessages);

        logger.info("Creating request entity to send to LLM API.");
        HttpEntity<String> request = createRequestEntity(formattedMessages);

        logger.info("Sending request to LLM API at {}", apiUrl);
        return processApiResponse(sendApiRequest(request));
    }

    private List<Map<String, String>> formatMessages(List<ContextMessage> contextMessages) {
        logger.info("Formatting messages for LLM API.");
        return contextMessages.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole().toString().toLowerCase(),
                        "content", msg.getText()))
                .toList();
    }

    private HttpEntity<String> createRequestEntity(List<Map<String, String>> formattedMessages) {
        logger.info("Building the JSON payload for the request.");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(formattedMessages);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "max_tokens", 80
        );

        try {
            logger.debug("Serialized request body: {}", objectMapper.writeValueAsString(requestBody));
            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize request body", e);
            throw new RuntimeException("Error creating JSON request body", e);
        }
    }

    private ResponseEntity<String> sendApiRequest(HttpEntity<String> request) {
        int attempts = 0;
        while (attempts < apiKeys.size()) {
            try {
                String currentApiKey = apiKeys.get(apiKeyIndex.get());
                HttpHeaders headers = request.getHeaders();
                headers.set("Authorization", "Bearer " + currentApiKey);

                logger.info("Executing API request to LLM using API key index {}", apiKeyIndex.get());
                return restTemplate.exchange(apiUrl, HttpMethod.POST, new HttpEntity<>(request.getBody(), headers), String.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                logger.warn("Rate limit reached for current API key, switching to next key. Retry attempt {}", attempts + 1);
                apiKeyIndex.set((apiKeyIndex.get() + 1) % apiKeys.size());
                attempts++;
            } catch (Exception e) {
                logger.error("Exception occurred during API request: {}", e.getMessage(), e);
                throw new RuntimeException("API request failed", e);
            }
        }
        throw new RuntimeException("Failed to process request after using all available API keys");
    }

    private String processApiResponse(ResponseEntity<String> response) {
        logger.info("Processing the API response.");
        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            String assistantMessage = extractMessageFromResponse(responseBody);
            logger.info("Successfully extracted message from API response.");
            saveAssistantMessage(assistantMessage);
            return assistantMessage;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse API response", e);
            throw new RuntimeException("Failed to process API response", e);
        }
    }

    private String extractMessageFromResponse(Map<String, Object> responseBody) {
        logger.debug("Extracting message content from the response body.");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            logger.error("No choices found in the API response.");
            throw new RuntimeException("No choices found in response");
        }

        Map<String, Object> choice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        if (message == null || !message.containsKey("content")) {
            logger.error("Message content missing in the response.");
            throw new RuntimeException("Message content is missing");
        }

        return (String) message.get("content");
    }

    public void saveAssistantMessage(String content) {
        logger.info("Saving the assistant's message to the database.");
        ContextMessage contextMessage = ContextMessage.builder()
                .text(content)
                .role(Role.assistant)
                .authorId("assistant")
                .build();
        messageRepository.save(contextMessage);
        trimMessageHistory();
        logger.info("Assistant message saved successfully.");
    }

    public void saveUserMessage(String content, String userId) {
        logger.info("Saving user message for user ID: {}", userId);

        if (content.equals(systemPrompt)) {
            logger.warn("Skipping saving of the system prompt message.");
            return;
        }

        ContextMessage contextMessage = ContextMessage.builder()
                .text(content)
                .role(Role.user)
                .authorId(userId)
                .build();
        messageRepository.save(contextMessage);
        trimMessageHistory();
        logger.info("User message saved successfully.");
    }

    private void trimMessageHistory() {
        long messageCount = messageRepository.count();
        if (messageCount > 30) {
            logger.info("Message count exceeds 30. Deleting the oldest messages.");
            List<ContextMessage> oldestMessages = messageRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(ContextMessage::getId))
                    .limit(messageCount - 30)
                    .toList();
            messageRepository.deleteAll(oldestMessages);
            logger.info("Oldest messages deleted successfully to maintain 30 message limit.");
        }
    }
}