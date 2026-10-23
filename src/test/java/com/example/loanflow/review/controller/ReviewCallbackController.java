package com.example.loanflow.review.controller;

import com.example.loanflow.review.dto.ReviewCallbackRequest;
import com.example.loanflow.review.service.ReviewCallbackService;
import com.example.loanflow.review.service.LoanProcessingLogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewCallbackControllerTest {

    @Mock
    private ReviewCallbackService reviewCallbackService;

    @Mock
    private LoanProcessingLogService loanProcessingLogService;

    @InjectMocks
    private ReviewCallbackController reviewCallbackController;

    private ReviewCallbackRequest request;

    @BeforeEach
    void setUp() {

        // @Value로 주입되는 API Key를 테스트 환경에서 설정
        ReflectionTestUtils.setField(
                reviewCallbackController,
                "callbackApiKey",
                "test-api-key"
        );

        request = new ReviewCallbackRequest();

        request.setApplicationId(90L);
        request.setRequestId("REQ-001");
        request.setReviewResultId("RESULT-001");
        request.setReviewResult("APPROVED");
    }

    @Test
    @DisplayName("올바른 API Key로 콜백 요청 시 정상 처리된다")
    void receiveReviewResult_success() {

        // when
        ResponseEntity<Void> response =
                reviewCallbackController.receiveReviewResult(
                        "test-api-key",
                        request
                );

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(reviewCallbackService, times(1))
                .processResult(request);

        verifyNoInteractions(loanProcessingLogService);
    }

    @Test
    @DisplayName("잘못된 API Key로 요청하면 401 예외가 발생한다")
    void receiveReviewResult_invalidApiKey() {

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackController.receiveReviewResult(
                        "wrong-api-key",
                        request
                )
        );

        // then
        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatus()
        );

        verifyNoInteractions(
                reviewCallbackService,
                loanProcessingLogService
        );
    }

    @Test
    @DisplayName("API Key가 없으면 401 예외가 발생한다")
    void receiveReviewResult_missingApiKey() {

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackController.receiveReviewResult(
                        null,
                        request
                )
        );

        // then
        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatus()
        );

        verifyNoInteractions(
                reviewCallbackService,
                loanProcessingLogService
        );
    }

    @Test
    @DisplayName("콜백 처리 실패 시 FAILED 로그를 저장하고 원래 예외를 전달한다")
    void receiveReviewResult_processingFailed() {

        // given
        ResponseStatusException serviceException =
                new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "이미 처리된 신청서입니다."
                );

        doThrow(serviceException)
                .when(reviewCallbackService)
                .processResult(request);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackController.receiveReviewResult(
                        "test-api-key",
                        request
                )
        );

        // then
        assertSame(serviceException, exception);

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatus()
        );

        verify(loanProcessingLogService, times(1)).saveLog(
                eq(90L),
                eq("REQ-001"),
                eq("REVIEW_CALLBACK"),
                eq("FAILED"),
                eq("CALLBACK_ERROR"),
                contains("이미 처리된 신청서입니다.")
        );
    }

    @Test
    @DisplayName("실패 로그 저장 중 예외가 발생해도 원래 콜백 예외를 유지한다")
    void receiveReviewResult_loggingFailed() {

        // given
        ResponseStatusException serviceException =
                new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "심사 결과 반영에 실패했습니다."
                );

        doThrow(serviceException)
                .when(reviewCallbackService)
                .processResult(request);

        // 로그 저장 중에도 예외 발생
        doThrow(new RuntimeException("로그 INSERT 실패"))
                .when(loanProcessingLogService)
                .saveLog(
                        eq(90L),
                        eq("REQ-001"),
                        eq("REVIEW_CALLBACK"),
                        eq("FAILED"),
                        eq("CALLBACK_ERROR"),
                        anyString()
                );

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackController.receiveReviewResult(
                        "test-api-key",
                        request
                )
        );

        // then
        assertSame(serviceException, exception);

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatus()
        );

        verify(loanProcessingLogService, times(1)).saveLog(
                eq(90L),
                eq("REQ-001"),
                eq("REVIEW_CALLBACK"),
                eq("FAILED"),
                eq("CALLBACK_ERROR"),
                anyString()
        );
    }

    @Test
    @DisplayName("신청서 ID가 없는 요청은 FAILED 로그 저장을 시도하지 않는다")
    void receiveReviewResult_missingApplicationId() {

        // given
        request.setApplicationId(null);

        ResponseStatusException serviceException =
                new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "심사 결과 요청값이 올바르지 않습니다."
                );

        doThrow(serviceException)
                .when(reviewCallbackService)
                .processResult(request);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackController.receiveReviewResult(
                        "test-api-key",
                        request
                )
        );

        // then
        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatus()
        );

        verifyNoInteractions(loanProcessingLogService);
    }
}
