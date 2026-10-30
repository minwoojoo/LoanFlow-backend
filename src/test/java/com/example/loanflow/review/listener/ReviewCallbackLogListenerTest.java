package com.example.loanflow.review.listener;

import com.example.loanflow.event.ReviewCallbackLogListener;
import com.example.loanflow.review.dto.ReviewCallbackProcessedRequest;
import com.example.loanflow.review.service.LoanProcessingLogService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewCallbackLogListenerTest {

    @Mock
    private LoanProcessingLogService loanProcessingLogService;

    @InjectMocks
    private ReviewCallbackLogListener reviewCallbackLogListener;

    @Test
    @DisplayName("심사 콜백 성공 시 SUCCESS 로그를 저장한다")
    void handleCallbackProcessed_success() {

        // given
        ReviewCallbackProcessedRequest event =
                new ReviewCallbackProcessedRequest(
                        90L,
                        "REQ-001",
                        "SUCCESS"
                );

        // when
        reviewCallbackLogListener.handleCallbackProcessed(event);

        // then
        verify(loanProcessingLogService, times(1)).saveLog(
                90L,
                "REQ-001",
                "REVIEW_CALLBACK",
                "SUCCESS",
                null,
                null
        );

        verifyNoMoreInteractions(loanProcessingLogService);
    }

    @Test
    @DisplayName("중복 콜백 수신 시 DUPLICATE 로그를 저장한다")
    void handleCallbackProcessed_duplicate() {

        // given
        ReviewCallbackProcessedRequest event =
                new ReviewCallbackProcessedRequest(
                        90L,
                        "REQ-001",
                        "DUPLICATE"
                );

        // when
        reviewCallbackLogListener.handleCallbackProcessed(event);

        // then
        verify(loanProcessingLogService, times(1)).saveLog(
                90L,
                "REQ-001",
                "REVIEW_CALLBACK",
                "DUPLICATE",
                null,
                null
        );

        verifyNoMoreInteractions(loanProcessingLogService);
    }

    @Test
    @DisplayName("콜백 로그 저장 실패 시 예외를 전파하지 않는다")
    void handleCallbackProcessed_logFailed() {

        // given
        ReviewCallbackProcessedRequest event =
                new ReviewCallbackProcessedRequest(
                        90L,
                        "REQ-001",
                        "SUCCESS"
                );

        // 로그 저장 시 예외가 발생하도록 설정
        doThrow(new RuntimeException("로그 INSERT 실패"))
                .when(loanProcessingLogService)
                .saveLog(
                        90L,
                        "REQ-001",
                        "REVIEW_CALLBACK",
                        "SUCCESS",
                        null,
                        null
                );

        // when & then
        assertDoesNotThrow(() ->
                reviewCallbackLogListener.handleCallbackProcessed(event)
        );

        verify(loanProcessingLogService, times(1)).saveLog(
                90L,
                "REQ-001",
                "REVIEW_CALLBACK",
                "SUCCESS",
                null,
                null
        );
    }
}
