package com.exed1ons.aibot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final RestTemplate restTemplate;

    @Value("${pollinations.api.key:}")
    private String apiKey;

    @Value("${llm.appearance.prompt}")
    private String appearancePrompt;

    public byte[] generateAlinaImage(String promptContext) {
        try {
            String fullPrompt = appearancePrompt + ", " + promptContext;

            URI uri = UriComponentsBuilder.fromHttpUrl("https://gen.pollinations.ai/image/{prompt}")
                    .queryParam("model", "grok-imagine")
                    .queryParam("width", 1024)
                    .queryParam("height", 1024)
                    .queryParam("nologo", "true")
                    .queryParam("safe", "false")
                    .buildAndExpand(fullPrompt)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("requesting pollinations image: {}", uri);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("received image, size: {} bytes", response.getBody().length);
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            log.error("failed to generate image: {}", e.getMessage());
            return null;
        }
    }
}