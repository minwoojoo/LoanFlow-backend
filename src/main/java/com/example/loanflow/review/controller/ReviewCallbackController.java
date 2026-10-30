package com.example.loanflow.review.controller;

import com.example.loanflow.review.dto.ReviewCallbackRequest;
import com.example.loanflow.review.service.ReviewCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@Tag(
        name = "심사 결과 콜백",
        description = "Mock 심사 서버의 심사 결과 수신 API"
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/callback")
public class ReviewCallbackController {

    private final ReviewCallbackService reviewCallbackService;

    @Value("${mock.review.callback-api-key}")
    private String callbackApiKey;

    @Operation(
            summary = "심사 결과 수신",
            description = "Mock 심사 서버로부터 승인 또는 거절 결과를 수신하여 대출 신청 상태를 변경합니다."
    )
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

        // Service에서 업무 처리 및 실패 로그 관리
        reviewCallbackService.processResult(request);

        return ResponseEntity.ok().build();
    }
}
