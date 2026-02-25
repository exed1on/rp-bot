package com.exed1ons.aibot.service;

import com.exed1ons.aibot.dao.entity.ChatMessage;
import com.exed1ons.aibot.dao.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Value("#{'${llm.api.keys}'.split(',')}")
    private List<String> apiKeys;

    @Value("${llm.system.prompt}")
    private String systemPrompt;
    @Value("${llm.injection.prompt}")
    private String injectionPrompt;

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
            logger.warn("primary provider failed, attempting fallback to sambanova...");
            result = tryProvider(chatId, userName, messageText, fallbackUrl, fallbackModel, fallbackKeys, fallbackKeyIndex);
        }

        return result;
    }

    private Map<String, Object> tryProvider(String chatId, String userName, String text, String url, String modelName, List<String> keys, AtomicInteger index) {
        int attempts = 0;
        while (attempts < keys.size()) {
            try {
                List<Map<String, Object>> messages = new ArrayList<>();
                Map<String, Object> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt + " when sending a photo, you MUST use the generate_alina_photo tool. decide your outfit, location and pose to match the vibe. arguments must be valid JSON");
                messages.add(systemMsg);

                var history = chatMessageRepository.findLastMessages(chatId, PageRequest.of(0, 10));
                var list = new ArrayList<>(history);
                Collections.reverse(list);
                for (var msg : list) {
                    Map<String, Object> h = new HashMap<>();
                    h.put("role", msg.getRole());
                    h.put("content", msg.getContent() == null ? "" : msg.getContent());
                    messages.add(h);
                }

                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", String.format("User %s says: <user_input>%s</user_input>", userName, text));
                messages.add(userMsg);

                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", modelName);
                requestBody.set("messages", objectMapper.valueToTree(messages));
                requestBody.put("temperature", 0.85);
                requestBody.put("tool_choice", "auto");

                var tools = requestBody.putArray("tools");
                var tool = tools.addObject();
                tool.put("type", "function");
                var function = tool.putObject("function");
                function.put("name", "generate_alina_photo");
                function.put("description", "sends a selfie ONLY when the user explicitly asks to see alina or requests a photo");
                var parameters = function.putObject("parameters");
                parameters.put("type", "object");
                var props = parameters.putObject("properties");
                props.putObject("outfit").put("type", "string");
                props.putObject("location").put("type", "string");
                props.putObject("pose_and_emotion").put("type", "string");

                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + keys.get(index.get()));
                headers.add("Content-Type", "application/json");

                logger.info("requesting llm ({})", modelName);
                HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                Map choice = ((List<Map>) response.getBody().get("choices")).get(0);
                Map message = (Map) choice.get("message");
                String responseText = (String) message.get("content");

                Map<String, Object> result = new HashMap<>();
                byte[] img = null;

                if (message.get("tool_calls") != null) {
                    List<Map> toolCalls = (List<Map>) message.get("tool_calls");
                    Map functionCall = (Map) toolCalls.get(0).get("function");
                    Map args = objectMapper.readValue((String) functionCall.get("arguments"), Map.class);
                    img = imageService.generateAlinaImage(String.format("wearing %s, in %s, %s", args.get("outfit"), args.get("location"), args.get("pose_and_emotion")));
                } else if (responseText != null && responseText.contains("<function=")) {
                    var matcher = java.util.regex.Pattern.compile("<function=.*?>(.*?)</function>", java.util.regex.Pattern.DOTALL).matcher(responseText);
                    if (matcher.find()) {
                        String json = matcher.group(1).trim().replaceAll("\\)$", "");
                        Map args = objectMapper.readValue(json, Map.class);
                        img = imageService.generateAlinaImage(String.format("wearing %s, in %s, %s", args.get("outfit"), args.get("location"), args.get("pose_and_emotion")));
                        responseText = responseText.replaceAll("<function=.*?>.*?</function>", "").trim();
                    }
                }

                if (img != null && (responseText == null || responseText.isBlank())) {
                    responseText = "just stares at the camera... )))";
                }

                result.put("text", responseText == null ? "..." : responseText);
                result.put("image", img);

                saveMessageToHistory(chatId, "user", userName, text);
                saveMessageToHistory(chatId, "assistant", "Alina", (String) result.get("text"));

                return result;

            } catch (Exception e) {
                logger.error("provider error: {}", e.getMessage());
                if (e.getMessage() != null && (e.getMessage().contains("429") || e.getMessage().contains("401"))) {
                    index.set((index.get() + 1) % keys.size());
                    attempts++;
                } else {
                    break;
                }
            }
        }
        return null;
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
            logger.error("fail history", e);
        }
    }
}