package com.myfitmate.myfitmate.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final OpenAiConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    // 동기 방식 (필요 시 유지)
    public String ask(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful health assistant."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
        );

        Map<String, Object> message = (Map<String, Object>) ((Map) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
        return (String) message.get("content");
    }

    // 비동기 + 예외 처리 버전
    @Async
    public CompletableFuture<String> askAsync(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            Map<String, Object> body = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful health assistant."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );

            Map<String, Object> message = (Map<String, Object>) ((Map) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
            return CompletableFuture.completedFuture((String) message.get("content"));

        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그 확인용
            return CompletableFuture.failedFuture(e);
        }
    }
}
