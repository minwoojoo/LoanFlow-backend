package com.example.loanflow.review.listener;


import com.example.loanflow.event.ReviewEventListener;
import com.example.loanflow.review.dto.MockReviewRequest;
import com.example.loanflow.review.service.LoanProcessingLogService;
import com.example.loanflow.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewEventListenerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private LoanProcessingLogService loanProcessingLogService;

    @InjectMocks
    private ReviewEventListener reviewEventListener;


    @Test
    @DisplayName("심사 요청 성공 시 SUCCESS 로그를 저장한다")
    void handleReviewRequested_success(){
        // given
        MockReviewRequest event = new MockReviewRequest(90L, "REQ-001");

        // when
        reviewEventListener.handleReviewRequested(event);

        // then
        InOrder inOrder = inOrder(
                reviewService,
                loanProcessingLogService
        );

        inOrder.verify(reviewService)
                .requestReview(90L, "REQ-001");

        inOrder.verify(loanProcessingLogService)
                .saveLog(
                        90L,
                        "REQ-001",
                        "REVIEW_REQUEST",
                        "SUCCESS",
                        null,
                        null
                );
    }

    @Test
    @DisplayName("심사 요청 실패 시 FAILED 로그를 저장한다")
    void handleReviewRequested_failed() {

        // given
        MockReviewRequest event = new MockReviewRequest(90L, "REQ-001");

        doThrow(new RuntimeException("Mock 서버 연결 실패"))
                .when(reviewService)
                .requestReview(90L, "REQ-001");

        // when
        reviewEventListener.handleReviewRequested(event);

        // then
        verify(loanProcessingLogService).saveLog(
                90L,
                "REQ-001",
                "REVIEW_REQUEST",
                "FAILED",
                "REVIEW_REQUEST_ERROR",
                "Mock 서버 연결 실패"
        );

        verify(loanProcessingLogService, never()).saveLog(
                eq(90L),
                eq("REQ-001"),
                eq("REVIEW_REQUEST"),
                eq("SUCCESS"),
                isNull(),
                isNull()
        );
    }

    @Test
    @DisplayName("로그 저장 실패가 발생해도 Listener에서 예외를 처리한다")
    void handleReviewRequested_logFailed() {

        // given
        MockReviewRequest event =
                new MockReviewRequest(90L, "REQ-001");

        doThrow(new RuntimeException("로그 INSERT 실패"))
                .when(loanProcessingLogService)
                .saveLog(
                        90L,
                        "REQ-001",
                        "REVIEW_REQUEST",
                        "SUCCESS",
                        null,
                        null
                );

        // when & then
        assertDoesNotThrow(() ->
                reviewEventListener.handleReviewRequested(event)
        );

        verify(reviewService).requestReview(90L, "REQ-001");
    }

}
