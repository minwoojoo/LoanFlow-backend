package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;
import com.example.loanflow.admin.service.AdminDashboardService;

import com.example.loanflow.auth.jwt.JwtTokenProvider;
import com.example.loanflow.member.entity.Role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @Test
    @DisplayName("ADMIN - 대시보드 통계 요약 조회 성공")
    void getDashboardSummary_success() throws Exception {

        // given
        String token =
                jwtTokenProvider.createAccessToken(1L, Role.ADMIN);

        AdminDashboardSummaryResponse expected =
                new AdminDashboardSummaryResponse(
                        100L,
                        20L,
                        30L,
                        40L,
                        10L
                );

        when(adminDashboardService.getDashboardSummary())
                .thenReturn(expected);

        // when & then
        mockMvc.perform(
                        get("/api/admin/dashboard/summary")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.draftCount").value(20))
                .andExpect(jsonPath("$.reviewingCount").value(30))
                .andExpect(jsonPath("$.approvedCount").value(40))
                .andExpect(jsonPath("$.rejectedCount").value(10));

        verify(adminDashboardService, times(1))
                .getDashboardSummary();
    }

    @Test
    @DisplayName("ADMIN - 신청서가 없으면 모든 통계가 0으로 반환된다")
    void getDashboardSummary_empty() throws Exception {

        // given
        String token =
                jwtTokenProvider.createAccessToken(1L, Role.ADMIN);

        AdminDashboardSummaryResponse expected =
                new AdminDashboardSummaryResponse(
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                );

        when(adminDashboardService.getDashboardSummary())
                .thenReturn(expected);

        // when & then
        mockMvc.perform(
                        get("/api/admin/dashboard/summary")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.draftCount").value(0))
                .andExpect(jsonPath("$.reviewingCount").value(0))
                .andExpect(jsonPath("$.approvedCount").value(0))
                .andExpect(jsonPath("$.rejectedCount").value(0));

        verify(adminDashboardService, times(1))
                .getDashboardSummary();
    }

    @Test
    @DisplayName("USER - 대시보드 통계 요약 조회 접근 차단")
    void getDashboardSummary_forbidden() throws Exception {

        // given
        String token =
                jwtTokenProvider.createAccessToken(2L, Role.USER);

        // when & then
        mockMvc.perform(
                        get("/api/admin/dashboard/summary")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminDashboardService);
    }

    @Test
    @DisplayName("미인증 사용자 - 대시보드 통계 요약 조회 접근 차단")
    void getDashboardSummary_unauthorized() throws Exception {

        // when & then
        mockMvc.perform(
                        get("/api/admin/dashboard/summary")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminDashboardService);
    }
}
