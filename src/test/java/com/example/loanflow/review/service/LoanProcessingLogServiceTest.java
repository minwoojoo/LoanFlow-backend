package com.example.loanflow.review.service;


import com.example.loanflow.review.entity.LoanProcessingLog;
import com.example.loanflow.review.mapper.LoanProcessingLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanProcessingLogServiceTest {

    @Mock
    private LoanProcessingLogMapper loanProcessingLogMapper;

    @InjectMocks
    private LoanProcessingLogService loanProcessingLogService;

    @Test
    @DisplayName("심사 요청 성공 로그를 정상적으로 저장한다")
    void saveLog_success(){

        // given
        Long applicationId = 88L;
        String requestId = "REQ-001";

        when(loanProcessingLogMapper.insertLog(any(LoanProcessingLog.class)))
                .thenReturn(1);

        // when
        loanProcessingLogService.saveLog(
                applicationId,
                requestId,
                "REVIEW_REQUEST",
                "SUCCESS",
                null,
                null
        );

        // then
        ArgumentCaptor<LoanProcessingLog> captor =
                ArgumentCaptor.forClass(LoanProcessingLog.class);

        verify(loanProcessingLogMapper).insertLog(captor.capture());

        LoanProcessingLog savedLog = captor.getValue();

        assertEquals(applicationId, savedLog.getApplicationId());
        assertEquals(requestId, savedLog.getRequestId());
        assertEquals("REVIEW_REQUEST", savedLog.getProcessType());
        assertEquals("SUCCESS", savedLog.getProcessStatus());

        assertNull(savedLog.getErrorCode());
        assertNull(savedLog.getErrorMessage());
    }

    @Test
    @DisplayName("심사 요청 실패 로그에 오류 코드와 메시지를 저장한다")
    void saveLog_failed() {

        // given
        Long applicationId = 88L;
        String requestId = "REQ-001";

        when(loanProcessingLogMapper.insertLog(any(LoanProcessingLog.class)))
                .thenReturn(1);

        // when
        loanProcessingLogService.saveLog(
                applicationId,
                requestId,
                "REVIEW_REQUEST",
                "FAILED",
                "CONNECTION_ERROR",
                "Mock 서버 연결 실패"
        );

        // then
        ArgumentCaptor<LoanProcessingLog> captor =
                ArgumentCaptor.forClass(LoanProcessingLog.class);

        verify(loanProcessingLogMapper).insertLog(captor.capture());

        LoanProcessingLog savedLog = captor.getValue();

        assertEquals(applicationId, savedLog.getApplicationId());
        assertEquals(requestId, savedLog.getRequestId());
        assertEquals("REVIEW_REQUEST", savedLog.getProcessType());
        assertEquals("FAILED", savedLog.getProcessStatus());

        assertEquals("CONNECTION_ERROR", savedLog.getErrorCode());
        assertEquals("Mock 서버 연결 실패", savedLog.getErrorMessage());
    }

    @Test
    @DisplayName("로그 INSERT 결과가 0건이면 예외가 발생한다")
    void saveLog_insertFailed() {

        // given
        when(loanProcessingLogMapper.insertLog(any(LoanProcessingLog.class)))
                .thenReturn(0);

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loanProcessingLogService.saveLog(
                        88L,
                        "REQ-001",
                        "REVIEW_REQUEST",
                        "SUCCESS",
                        null,
                        null
                )
        );

        assertEquals(
                "심사 처리 로그 저장에 실패했습니다.",
                exception.getMessage()
        );

        verify(loanProcessingLogMapper, times(1))
                .insertLog(any(LoanProcessingLog.class));
    }

}
