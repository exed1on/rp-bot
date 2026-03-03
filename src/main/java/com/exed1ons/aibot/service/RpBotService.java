package com.exed1ons.aibot.service;

import com.exed1ons.aibot.dao.entity.ChatMessage;
import com.exed1ons.aibot.dao.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RpBotService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final ImageService imageService;

    @Value("${llm.api.url}")
    private String apiUrl;
    @Value("${llm.model}")
    private String model;
    @Value("#{'${llm.api.keys}'.split(',')}")
    private List<String> apiKeys;
    @Value("${llm.system.prompt}")
    private String systemPrompt;
    @Value("${llm.tool.definition}")
    private String toolDefinition;
    @Value("${llm.image.prompt.template}")
    private String imagePromptTemplate;

    @Value("${llm.fallback.url}")
    private String fallbackUrl;
    @Value("${llm.fallback.model}")
    private String fallbackModel;
    @Value("#{'${llm.fallback.keys}'.split(',')}")
    private List<String> fallbackKeys;

    private final AtomicInteger primaryKeyIndex = new AtomicInteger(0);
    private final AtomicInteger fallbackKeyIndex = new AtomicInteger(0);

    public Map<String, Object> generateRoleplayResponse(String messageText, String userName, String chatId) {
        Map<String, Object> result = tryProvider(chatId, userName, messageText, apiUrl, model, apiKeys, primaryKeyIndex);
        if (result == null) {
            result = tryProvider(chatId, userName, messageText, fallbackUrl, fallbackModel, fallbackKeys, fallbackKeyIndex);
        }
        return result;
    }

    private Map<String, Object> tryProvider(String chatId, String userName, String text, String url, String modelName, List<String> keys, AtomicInteger index) {
        int attempts = 0;
        while (attempts < keys.size()) {
            try {
                List<Map<String, Object>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", systemPrompt));

                var history = chatMessageRepository.findLastMessages(chatId, PageRequest.of(0, 10));
                var list = new ArrayList<>(history);
                Collections.reverse(list);
                for (var msg : list) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent() == null ? "" : msg.getContent()));
                }

                messages.add(Map.of("role", "user", "content", String.format("User %s says: <user_input>%s</user_input>", userName, text)));

                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", modelName);
                requestBody.set("messages", objectMapper.valueToTree(messages));
                requestBody.put("temperature", 0.85);
                requestBody.put("tool_choice", "auto");
                requestBody.set("tools", objectMapper.readTree(toolDefinition));

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + keys.get(index.get()));
                headers.add("Content-Type", "application/json");

                log.info("requesting llm ({})", modelName);
                ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(requestBody.toString(), headers), Map.class);

                Map choice = ((List<Map>) response.getBody().get("choices")).get(0);
                Map message = (Map) choice.get("message");
                String responseText = (String) message.get("content");
                byte[] img = null;

                if (message.get("tool_calls") != null) {
                    JsonNode toolCalls = objectMapper.valueToTree(message.get("tool_calls"));
                    JsonNode args = objectMapper.readTree(toolCalls.get(0).get("function").get("arguments").asText());
                    img = imageService.generateAlinaImage(formatPrompt(args));
                }
                else if (responseText != null && (responseText.contains("generate_alina_photo") || responseText.contains("<tool"))) {
                    Pattern pattern = Pattern.compile("\\{.*\\}");
                    Matcher matcher = pattern.matcher(responseText);
                    if (matcher.find()) {
                        JsonNode args = objectMapper.readTree(matcher.group());
                        img = imageService.generateAlinaImage(formatPrompt(args));
                        responseText = responseText.replaceAll("\\{.*\\}", "").replaceAll("<.*?>", "").trim();
                    }
                }

                if (img != null && (responseText == null || responseText.isBlank())) {
                    responseText = "";
                }

                Map<String, Object> result = new HashMap<>();
                result.put("text", responseText == null ? "..." : responseText);
                result.put("image", img);

                saveMessageToHistory(chatId, "user", userName, text);
                saveMessageToHistory(chatId, "assistant", "Alina", (String) result.get("text"));

                return result;

            } catch (Exception e) {
                log.error("provider error: {}", e.getMessage());
                index.set((index.get() + 1) % keys.size());
                attempts++;
            }
        }
        return null;
    }

    private String formatPrompt(JsonNode args) {
        return String.format(imagePromptTemplate,
                args.path("perspective").asText(""),
                args.path("subject_description").asText(""),
                args.path("clothing").asText(""),
                args.path("location").asText(""),
                args.path("vibe_and_emotion").asText(""));
    }

    private void saveMessageToHistory(String chatId, String role, String senderName, String content) {
        try {
            chatMessageRepository.save(ChatMessage.builder()
                    .chatId(chatId).role(role).senderName(senderName).content(content).build());
        } catch (Exception e) {
            log.error("fail history", e);
        }
    }
}