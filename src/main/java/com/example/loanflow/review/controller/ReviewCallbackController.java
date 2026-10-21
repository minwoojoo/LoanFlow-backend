package com.example.loanflow.review.controller;

import com.example.loanflow.review.dto.ReviewCallbackRequest;
import com.example.loanflow.review.service.ReviewCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/callback")
public class ReviewCallbackController {

    private final ReviewCallbackService reviewCallbackService;

    @Value("${mock.review.callback-api-key}")
    private String callbackApiKey;

    @PostMapping("/review-results")
    public ResponseEntity<Void> receiveReviewResult(
            @RequestHeader(
                    value = "X-Callback-api-key",
                    required = false
            ) String apiKey,

            @RequestBody ReviewCallbackRequest request
    ){
        // 서버 간 API Key 검증
        if (apiKey == null || !callbackApiKey.equals(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "유효하지 않은 콜백 API Key입니다."
            );
        }

        reviewCallbackService.processResult(request);

        return ResponseEntity.ok().build();
    }

}
