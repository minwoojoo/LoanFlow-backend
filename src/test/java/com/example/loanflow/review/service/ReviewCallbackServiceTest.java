
package com.example.loanflow.review.service;

import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.review.dto.ReviewCallbackRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class ReviewCallbackServiceTest {

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private ReviewCallbackService reviewCallbackService;

    private ReviewCallbackRequest createRequest(
            Long applicationId,
            String requestId,
            String reviewResultId,
            String reviewResult
    ) {
        ReviewCallbackRequest request = new ReviewCallbackRequest();
        request.setApplicationId(applicationId);
        request.setRequestId(requestId);
        request.setReviewResultId(reviewResultId);
        request.setReviewResult(reviewResult);
        return request;
    }

    private LoanApplication createApplication(
            String status,
            String reviewResultId
    ) {
        return LoanApplication.builder()
                .applicationId(15L)
                .requestId("REQ-001")
                .applicationStatus(status)
                .reviewResultId(reviewResultId)
                .build();
    }

    @Test
    @DisplayName("정상적인 승인 콜백이면 신청 상태를 승인으로 변경한다")
    void processResult_approved() {

        ReviewCallbackRequest request =
                createRequest(15L, "REQ-001", "RESULT-001", "APPROVED");

        when(loanMapper.findByApplicationAndRequestId(
                15L, "REQ-001"
        )).thenReturn(createApplication("심사중", null));

        when(loanMapper.updateReviewResult(
                15L, "REQ-001", "RESULT-001", "승인"
        )).thenReturn(1);

        assertDoesNotThrow(() ->
                reviewCallbackService.processResult(request)
        );

        verify(loanMapper).updateReviewResult(
                15L, "REQ-001", "RESULT-001", "승인"
        );
    }

    @Test
    @DisplayName("정상적인 거절 콜백이면 신청 상태를 거절로 변경한다")
    void processResult_rejected() {

        ReviewCallbackRequest request =
                createRequest(15L, "REQ-001", "RESULT-002", "REJECTED");

        when(loanMapper.findByApplicationAndRequestId(
                15L, "REQ-001"
        )).thenReturn(createApplication("심사중", null));

        when(loanMapper.updateReviewResult(
                15L, "REQ-001", "RESULT-002", "거절"
        )).thenReturn(1);

        assertDoesNotThrow(() ->
                reviewCallbackService.processResult(request)
        );

        verify(loanMapper).updateReviewResult(
                15L, "REQ-001", "RESULT-002", "거절"
        );
    }

    @Test
    @DisplayName("동일한 심사 결과가 재전송되면 중복 UPDATE하지 않는다")
    void processResult_duplicate() {

        ReviewCallbackRequest request =
                createRequest(15L, "REQ-001", "RESULT-001", "APPROVED");

        when(loanMapper.findByApplicationAndRequestId(
                15L, "REQ-001"
        )).thenReturn(createApplication("승인", "RESULT-001"));

        assertDoesNotThrow(() ->
                reviewCallbackService.processResult(request)
        );

        verify(loanMapper, never()).updateReviewResult(
                anyLong(), anyString(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("이미 완료된 심사를 다른 결과로 변경하려 하면 409 오류가 발생한다")
    void processResult_conflict() {

        ReviewCallbackRequest request =
                createRequest(15L, "REQ-001", "RESULT-002", "REJECTED");

        when(loanMapper.findByApplicationAndRequestId(
                15L, "REQ-001"
        )).thenReturn(createApplication("승인", "RESULT-001"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackService.processResult(request)
        );

        assertEquals(CONFLICT, exception.getStatus());

        verify(loanMapper, never()).updateReviewResult(
                anyLong(), anyString(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("존재하지 않는 신청서의 콜백은 404 오류가 발생한다")
    void processResult_notFound() {

        ReviewCallbackRequest request =
                createRequest(15L, "REQ-001", "RESULT-001", "APPROVED");

        when(loanMapper.findByApplicationAndRequestId(
                15L, "REQ-001"
        )).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackService.processResult(request)
        );

        assertEquals(NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("잘못된 심사 결과값은 400 오류가 발생한다")
    void processResult_invalidResult() {

        ReviewCallbackRequest request =
                createRequest(15L, "REQ-001", "RESULT-001", "PENDING");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reviewCallbackService.processResult(request)
        );

        assertEquals(BAD_REQUEST, exception.getStatus());

        verifyNoInteractions(loanMapper);
    }
}
