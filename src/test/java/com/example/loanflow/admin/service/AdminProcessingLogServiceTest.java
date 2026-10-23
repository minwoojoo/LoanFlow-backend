package com.example.loanflow.admin.service;

import com.example.loanflow.admin.dto.AdminProcessingLogReadResponse;
import com.example.loanflow.admin.dto.AdminProcessingLogDetailResponse;
import com.example.loanflow.review.mapper.LoanProcessingLogMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProcessingLogServiceTest {

    @Mock
    private LoanProcessingLogMapper loanProcessingLogMapper;

    @InjectMocks
    private AdminProcessingLogService adminProcessingLogService;

    @Test
    @DisplayName("운영자 - 전체 심사 처리 이력 조회 성공")
    void getAllProcessingLogs_success() {

        // given
        AdminProcessingLogReadResponse log1 =
                new AdminProcessingLogReadResponse(
                        1L,
                        100L,
                        10L,
                        "김철수",
                        "직장인 신용대출",
                        "REVIEW_REQUEST",
                        "SUCCESS",
                        LocalDateTime.of(2026, 10, 23, 9, 0)
                );

        AdminProcessingLogReadResponse log2 =
                new AdminProcessingLogReadResponse(
                        2L,
                        100L,
                        10L,
                        "김철수",
                        "직장인 신용대출",
                        "REVIEW_CALLBACK",
                        "FAILED",
                        LocalDateTime.of(2026, 10, 23, 9, 1)
                );

        List<AdminProcessingLogReadResponse> expected =
                Arrays.asList(log1, log2);

        when(loanProcessingLogMapper.findAllProcessingLogs())
                .thenReturn(expected);

        // when
        List<AdminProcessingLogReadResponse> result =
                adminProcessingLogService.getAllProcessingLogs();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(expected, result);

        assertEquals("REVIEW_REQUEST", result.get(0).getProcessType());
        assertEquals("SUCCESS", result.get(0).getProcessStatus());

        assertEquals("REVIEW_CALLBACK", result.get(1).getProcessType());
        assertEquals("FAILED", result.get(1).getProcessStatus());

        verify(loanProcessingLogMapper, times(1))
                .findAllProcessingLogs();
    }

    @Test
    @DisplayName("운영자 - 처리 이력이 없으면 빈 목록 반환")
    void getAllProcessingLogs_empty() {

        // given
        when(loanProcessingLogMapper.findAllProcessingLogs())
                .thenReturn(Collections.emptyList());

        // when
        List<AdminProcessingLogReadResponse> result =
                adminProcessingLogService.getAllProcessingLogs();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(loanProcessingLogMapper, times(1))
                .findAllProcessingLogs();
    }

    @Test
    @DisplayName("운영자 - 심사 처리 이력 상세 조회 성공")
    void getProcessingLogDetail_success() {

        // given
        Long logId = 1L;

        AdminProcessingLogDetailResponse expected =
                new AdminProcessingLogDetailResponse(
                        logId,
                        100L,
                        "request-test-001",
                        10L,
                        "김철수",
                        1L,
                        "직장인 신용대출",
                        "REVIEW_REQUEST",
                        "SUCCESS",
                        null,
                        null,
                        LocalDateTime.of(2026, 10, 23, 9, 0)
                );

        when(loanProcessingLogMapper.findProcessingLogDetail(logId))
                .thenReturn(expected);

        // when
        AdminProcessingLogDetailResponse result =
                adminProcessingLogService.getProcessingLogDetail(logId);

        // then
        assertNotNull(result);
        assertSame(expected, result);

        assertEquals(logId, result.getLogId());
        assertEquals("REVIEW_REQUEST", result.getProcessType());
        assertEquals("SUCCESS", result.getProcessStatus());

        verify(loanProcessingLogMapper, times(1))
                .findProcessingLogDetail(logId);
    }

    @Test
    @DisplayName("운영자 - 존재하지 않는 심사 처리 이력 조회 시 404")
    void getProcessingLogDetail_notFound() {

        // given
        Long logId = 999999L;

        when(loanProcessingLogMapper.findProcessingLogDetail(logId))
                .thenReturn(null);

        // when
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminProcessingLogService
                                .getProcessingLogDetail(logId)
                );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        assertEquals(
                "심사 처리 이력을 찾을 수 없습니다.",
                exception.getReason()
        );

        verify(loanProcessingLogMapper, times(1))
                .findProcessingLogDetail(logId);
    }
}
