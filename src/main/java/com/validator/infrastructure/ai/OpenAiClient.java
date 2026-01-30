package com.validator.infrastructure.ai;

import com.validator.exception.AiServiceException;
import com.validator.infrastructure.config.OpenAiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiClient.class);

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

        logger.info("OpenAI 요청을 시작합니다. model={}, promptLength={}", properties.getModel(), prompt.length());

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a secure code reviewer. Return JSON only."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2
        );

        String response = executeWithRetry(requestBody, 3, 500);
        if (response == null || response.isBlank()) {
            throw new AiServiceException("OPENAI_EMPTY_RESPONSE", "OpenAI 응답이 비어 있습니다.");
        }
        logger.info("OpenAI 응답을 수신했습니다. length={}", response == null ? 0 : response.length());
        return response;
    }

    private String executeWithRetry(Map<String, Object> requestBody, int maxAttempts, long initialBackoffMs) {
        long backoffMs = initialBackoffMs;
        WebClientResponseException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return webClient.post()
                        .uri("/v1/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (WebClientResponseException ex) {
                lastException = ex;
                int status = ex.getStatusCode().value();
                boolean retryable = status == HttpStatus.TOO_MANY_REQUESTS.value()
                        || (status >= 500 && status < 600);
                if (!retryable || attempt == maxAttempts) {
                    throw ex;
                }
                logger.warn("OpenAI 요청이 실패했습니다. status={}, attempt={}/{}. 잠시 후 재시도합니다.",
                        status, attempt, maxAttempts);
                sleep(backoffMs);
                backoffMs *= 2;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        return null;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
