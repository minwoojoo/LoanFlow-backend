package com.example.loanflow.event;

import com.example.loanflow.review.dto.MockReviewRequest;
import com.example.loanflow.review.service.LoanProcessingLogService;
import com.example.loanflow.review.service.ReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListener {

    private final ReviewService reviewService;
    private final LoanProcessingLogService loanProcessingLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewRequested(MockReviewRequest event) {

        Long applicationId = event.getApplicationId();
        String requestId = event.getRequestId();

        try {
            // 1. Mock 서버에 심사 요청 전송
            reviewService.requestReview(applicationId, requestId);

        } catch (Exception e) {
            // 2. 심사 요청 실패 로그
            log.error(
                    "Mock 심사 요청 실패 - applicationId: {}",
                    applicationId,
                    e
            );

            saveProcessingLog(
                    applicationId,
                    requestId,
                    "FAILED",
                    "REVIEW_REQUEST_ERROR",
                    e.getMessage()
            );
            return;
        }

        // 3. 심사 요청 접수 성공 로그
        saveProcessingLog(
                applicationId,
                requestId,
                "SUCCESS",
                null,
                null
        );
    }

    private void saveProcessingLog(
            Long applicationId,
            String requestId,
            String status,
            String errorCode,
            String errorMessage
    ) {
        try {
            loanProcessingLogService.saveLog(
                    applicationId,
                    requestId,
                    "REVIEW_REQUEST",
                    status,
                    errorCode,
                    errorMessage
            );
        } catch (Exception e) {
            log.error(
                    "심사 처리 로그 저장 실패 - applicationId: {}, status: {}",
                    applicationId,
                    status,
                    e
            );
        }
    }
}
