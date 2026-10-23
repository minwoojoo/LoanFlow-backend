package com.example.loanflow.admin.service;

import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;
import com.example.loanflow.admin.mapper.AdminDashboardMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private AdminDashboardMapper adminDashboardMapper;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    @DisplayName("운영자 - 대시보드 통계 요약 조회 성공")
    void getDashboardSummary_success() {

        // given
        AdminDashboardSummaryResponse expected =
                new AdminDashboardSummaryResponse(
                        100L,  // 전체 신청
                        20L,   // 작성중
                        30L,   // 심사중
                        40L,   // 승인
                        10L    // 거절
                );

        when(adminDashboardMapper.findDashboardSummary())
                .thenReturn(expected);

        // when
        AdminDashboardSummaryResponse result =
                adminDashboardService.getDashboardSummary();

        // then
        assertNotNull(result);

        assertSame(expected, result);

        assertEquals(100L, result.getTotalCount());
        assertEquals(20L, result.getDraftCount());
        assertEquals(30L, result.getReviewingCount());
        assertEquals(40L, result.getApprovedCount());
        assertEquals(10L, result.getRejectedCount());

        verify(adminDashboardMapper, times(1))
                .findDashboardSummary();

        verifyNoMoreInteractions(adminDashboardMapper);
    }

    @Test
    @DisplayName("운영자 - 신청서가 없으면 모든 통계가 0으로 반환된다")
    void getDashboardSummary_empty() {

        // given
        AdminDashboardSummaryResponse expected =
                new AdminDashboardSummaryResponse(
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                );

        when(adminDashboardMapper.findDashboardSummary())
                .thenReturn(expected);

        // when
        AdminDashboardSummaryResponse result =
                adminDashboardService.getDashboardSummary();

        // then
        assertNotNull(result);

        assertEquals(0L, result.getTotalCount());
        assertEquals(0L, result.getDraftCount());
        assertEquals(0L, result.getReviewingCount());
        assertEquals(0L, result.getApprovedCount());
        assertEquals(0L, result.getRejectedCount());

        verify(adminDashboardMapper, times(1))
                .findDashboardSummary();
    }
}
