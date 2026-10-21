
package com.example.loanflow.review.listener;

import com.example.loanflow.review.event.ReviewRequestedEvent;
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

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleReviewRequested(
            ReviewRequestedEvent event
    ) {

        try {
            reviewService.requestReview(
                    event.getApplicationId(),
                    event.getRequestId()
            );

        } catch (Exception e) {
            log.error(
                    "Mock 심사 요청 실패 - applicationId: {}, requestId: {}",
                    event.getApplicationId(),
                    event.getRequestId(),
                    e
            );
        }
    }
}
