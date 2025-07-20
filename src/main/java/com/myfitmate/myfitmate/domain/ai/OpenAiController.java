package com.myfitmate.myfitmate.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class OpenAiController {

    private final OpenAiService openAiService;

    @PostMapping("/ask")
    public CompletableFuture<ResponseEntity<String>> ask(@RequestBody String prompt) {
        return openAiService.askAsync(prompt)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> {
                    e.printStackTrace(); // 서버 로그 확인
                    return ResponseEntity.status(500).body("응답 중 오류가 발생했습니다.");
                });
    }
}
