package com.example.loanflow.review.listener;

import com.example.loanflow.review.event.ReviewCallbackProcessedEvent;
import com.example.loanflow.review.service.LoanProcessingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCallbackLogListener {

    private final LoanProcessingLogService loanProcessingLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCallbackProcessed(ReviewCallbackProcessedEvent event){
        try{
            loanProcessingLogService.saveLog(
                    event.getApplicationId(),
                    event.getRequestId(),
                    "REVIEW_CALLBACK",
                    event.getProcessStatus(),
                    null,
                    null
            );
        } catch (Exception e){
            log.error(
                    "심사 콜백 로그 저장 실패 - applicationId: {}",
                    event.getApplicationId(),
                    e
            );
        }
    }

}
