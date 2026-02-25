package com.exed1ons.aibot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RpBotService {

    private static final Logger logger = LoggerFactory.getLogger(RpBotService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

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

    public String generateRoleplayResponse(String messageText, String userName) {

        if (isPotentialInjection(messageText)) {
            logger.warn("Potential prompt injection detected from user: {}", userName);
            return generateCreativeRejection(messageText, userName);
        }

        String safeUserMessage = String.format("User %s says: <user_input>%s</user_input>", userName, messageText);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", safeUserMessage));

        try {
            return callLLMAPI(messages, model, 250, 0.85);
        } catch (Exception e) {
            logger.error("Error generating RP response", e);
            return null;
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
            return "чел... просто нет. 🤡";
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