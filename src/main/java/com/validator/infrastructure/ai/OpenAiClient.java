package com.validator.infrastructure.ai;

import com.validator.exception.AiServiceException;
import com.validator.infrastructure.config.OpenAiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {

    private final WebClient webClient;
    private final OpenAiProperties properties;

    public OpenAiClient(WebClient webClient, OpenAiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public String analyzeCode(String prompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiServiceException("OPENAI_KEY_MISSING", "OpenAI API 키가 설정되지 않았습니다.");
        }

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a secure code reviewer. Return JSON only."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
