package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminLoanReadResponse;
import com.example.loanflow.admin.dto.AdminLoanDetailResponse;
import com.example.loanflow.admin.service.AdminLoanService;
import com.example.loanflow.auth.jwt.JwtTokenProvider;
import com.example.loanflow.member.entity.Role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminLoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AdminLoanService adminLoanService;

    private String createToken(Long memberId, Role role) {
        return jwtTokenProvider.createAccessToken(memberId, role);
    }

    @Test
    @DisplayName("ADMIN - 전체 대출 신청 목록 조회 성공")
    void getAllApplications_success() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);

        when(adminLoanService.getAllApplications())
                .thenReturn(Collections.singletonList(
                        new AdminLoanReadResponse()
                ));

        // when & then
        mockMvc.perform(get("/api/admin/loan-applications")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(adminLoanService, times(1))
                .getAllApplications();
    }

    @Test
    @DisplayName("ADMIN - 전체 신청서가 없으면 빈 목록 반환")
    void getAllApplications_empty() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);

        when(adminLoanService.getAllApplications())
                .thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/admin/loan-applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(adminLoanService).getAllApplications();
    }

    @Test
    @DisplayName("ADMIN - 대출 신청 상세 조회 성공")
    void getApplicationDetail_success() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);
        Long applicationId = 10L;

        when(adminLoanService.getApplicationDetail(applicationId))
                .thenReturn(new AdminLoanDetailResponse());

        // when & then
        mockMvc.perform(get("/api/admin/loan-applications/{applicationId}",
                        applicationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));

        verify(adminLoanService)
                .getApplicationDetail(applicationId);
    }

    @Test
    @DisplayName("ADMIN - 존재하지 않는 신청서 조회 시 404")
    void getApplicationDetail_notFound() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);
        Long applicationId = 999999L;

        when(adminLoanService.getApplicationDetail(applicationId))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "대출 신청서를 찾을 수 없습니다."
                ));

        // when & then
        mockMvc.perform(get("/api/admin/loan-applications/{applicationId}",
                        applicationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verify(adminLoanService)
                .getApplicationDetail(applicationId);
    }

    @Test
    @DisplayName("USER - 운영자 목록 조회 접근 차단")
    void getAllApplications_forbidden() throws Exception {

        // given
        String token = createToken(2L, Role.USER);

        // when & then
        mockMvc.perform(get("/api/admin/loan-applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminLoanService);
    }

    @Test
    @DisplayName("USER - 운영자 상세 조회 접근 차단")
    void getApplicationDetail_forbidden() throws Exception {

        // given
        String token = createToken(2L, Role.USER);

        // when & then
        mockMvc.perform(get("/api/admin/loan-applications/10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminLoanService);
    }
}
