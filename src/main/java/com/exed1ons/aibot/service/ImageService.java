package com.exed1ons.aibot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final RestTemplate restTemplate;

    @Value("${hf.api.key}")
    private String hfToken;

    @Value("${llm.appearance.prompt}")
    private String appearancePrompt;

    public byte[] generateAlinaImage(String promptContext) {
        try {
            String url = "https://router.huggingface.co/hf-inference/models/black-forest-labs/FLUX.1-schnell";
            String fullPrompt = appearancePrompt + ", " + promptContext;

            Map<String, String> body = Map.of("inputs", fullPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + hfToken);
            headers.set("Content-Type", "application/json");
            headers.setAccept(Collections.singletonList(MediaType.valueOf("image/png")));

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("requesting hugging face with correct accept headers...");
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("got the photo, size: {} bytes", response.getBody().length);
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("hugging face is being difficult again", e);
            return null;
        }
    }
}