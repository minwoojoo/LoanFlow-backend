package com.example.loanflow.review.controller;

import com.example.loanflow.review.dto.ReviewCallbackRequest;
import com.example.loanflow.review.service.LoanProcessingLogService;
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
    private final LoanProcessingLogService loanProcessingLogService;

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

        // 심사 결과 처리
        try {
            reviewCallbackService.processResult(request);
        } catch (RuntimeException e){
            log.error(
                    "심사 콜백 처리 실패 - applicationId: {}",
                    request == null ? null : request.getApplicationId(),
                    e
            );

            // 실패 로그 저장
            saveFailureLog(request, e);

            // 원래 예외를 다시 전달
            throw e;
        }

        return ResponseEntity.ok().build();
    }

    private void saveFailureLog(
            ReviewCallbackRequest request,
            RuntimeException e
    ){
        if (request == null || request.getApplicationId() == null){
            return;
        }

        try{
            String errorMessage = e.getMessage() == null ? "콜백처리 오류" : e.getMessage();

            if (errorMessage.length() > 500){
                errorMessage = errorMessage.substring(0, 500);
            }

            loanProcessingLogService.saveLog(
                    request.getApplicationId(),
                    request.getRequestId(),
                    "REVIEW_CALLBACK",
                    "FAILED",
                    "CALLBACK_ERROR",
                    errorMessage
            );

        } catch (Exception logException){
            // 로그 저장 실패가 원래 콜백 예외를 덮어쓰지 않도록 처리
            log.error(
                    "심사 콜백 실패 로그 저장 실패 - applicationId: {}",
                    request.getApplicationId(),
                    logException
            );
        }
    }

}
