package com.example.loanflow.admin.service;

import com.example.loanflow.admin.dto.AdminLoanReadResponse;
import com.example.loanflow.admin.dto.AdminLoanDetailResponse;
import com.example.loanflow.loan.mapper.LoanMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLoanServiceTest {

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private AdminLoanService adminLoanService;

    @Test
    @DisplayName("전체 대출 신청 목록 조회 성공")
    void getAllApplications_success() {

        // given
        AdminLoanReadResponse application1 = new AdminLoanReadResponse();

        AdminLoanReadResponse application2 = new AdminLoanReadResponse();

        List<AdminLoanReadResponse> expected = Arrays.asList(application1, application2);

        when(loanMapper.findAllApplications())
                .thenReturn(expected);

        // when
        List<AdminLoanReadResponse> result =
                adminLoanService.getAllApplications();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(loanMapper, times(1))
                .findAllApplications();
    }

    @Test
    @DisplayName("전체 대출 신청 목록이 없는 경우 빈 목록 반환")
    void getAllApplications_empty() {

        // given
        when(loanMapper.findAllApplications())
                .thenReturn(Collections.emptyList());

        // when
        List<AdminLoanReadResponse> result =
                adminLoanService.getAllApplications();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(loanMapper, times(1))
                .findAllApplications();
    }

    @Test
    @DisplayName("대출 신청 상세 조회 성공")
    void getApplicationDetail_success() {

        // given
        Long applicationId = 1L;

        AdminLoanDetailResponse expected =
                new AdminLoanDetailResponse();

        when(loanMapper.findAdminApplicationDetail(applicationId))
                .thenReturn(expected);

        // when
        AdminLoanDetailResponse result =
                adminLoanService.getApplicationDetail(applicationId);

        // then
        assertNotNull(result);
        assertSame(expected, result);

        verify(loanMapper, times(1))
                .findAdminApplicationDetail(applicationId);
    }

    @Test
    @DisplayName("존재하지 않는 신청서 상세 조회 시 404 예외 발생")
    void getApplicationDetail_notFound() {

        // given
        Long applicationId = 999999L;

        when(loanMapper.findAdminApplicationDetail(applicationId))
                .thenReturn(null);

        // when
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminLoanService
                                .getApplicationDetail(applicationId)
                );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        assertEquals(
                "대출 신청서를 찾을 수 없습니다.",
                exception.getReason()
        );

        verify(loanMapper, times(1))
                .findAdminApplicationDetail(applicationId);
    }
}
