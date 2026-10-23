package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminProcessingLogReadResponse;
import com.example.loanflow.admin.dto.AdminProcessingLogDetailResponse;
import com.example.loanflow.admin.service.AdminProcessingLogService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProcessingLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AdminProcessingLogService adminProcessingLogService;

    // 테스트용 JWT 생성
    private String createToken(Long memberId, Role role) {
        return jwtTokenProvider.createAccessToken(memberId, role);
    }

    @Test
    @DisplayName("ADMIN - 전체 심사 처리 이력 목록 조회 성공")
    void getAllProcessingLogs_success() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);

        AdminProcessingLogReadResponse log =
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

        when(adminProcessingLogService.getAllProcessingLogs())
                .thenReturn(Collections.singletonList(log));

        // when & then
        mockMvc.perform(
                        get("/api/admin/processing-logs")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].logId").value(1))
                .andExpect(jsonPath("$[0].applicationId").value(100))
                .andExpect(jsonPath("$[0].memberName").value("김철수"))
                .andExpect(jsonPath("$[0].processType").value("REVIEW_REQUEST"))
                .andExpect(jsonPath("$[0].processStatus").value("SUCCESS"));

        verify(adminProcessingLogService, times(1))
                .getAllProcessingLogs();
    }

    @Test
    @DisplayName("ADMIN - 처리 이력이 없는 경우 빈 목록 반환")
    void getAllProcessingLogs_empty() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);

        when(adminProcessingLogService.getAllProcessingLogs())
                .thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(
                        get("/api/admin/processing-logs")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(adminProcessingLogService).getAllProcessingLogs();
    }

    @Test
    @DisplayName("ADMIN - 심사 처리 이력 상세 조회 성공")
    void getProcessingLogDetail_success() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);
        Long logId = 10L;

        AdminProcessingLogDetailResponse log =
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

        when(adminProcessingLogService.getProcessingLogDetail(logId))
                .thenReturn(log);

        // when & then
        mockMvc.perform(
                        get("/api/admin/processing-logs/{id}", logId)
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logId").value(10))
                .andExpect(jsonPath("$.applicationId").value(100))
                .andExpect(jsonPath("$.requestId").value("request-test-001"))
                .andExpect(jsonPath("$.memberName").value("김철수"))
                .andExpect(jsonPath("$.productName").value("직장인 신용대출"))
                .andExpect(jsonPath("$.processStatus").value("SUCCESS"));

        verify(adminProcessingLogService, times(1))
                .getProcessingLogDetail(logId);
    }

    @Test
    @DisplayName("ADMIN - 존재하지 않는 처리 이력 조회 시 404")
    void getProcessingLogDetail_notFound() throws Exception {

        // given
        String token = createToken(1L, Role.ADMIN);
        Long logId = 999999L;

        when(adminProcessingLogService.getProcessingLogDetail(logId))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "심사 처리 이력을 찾을 수 없습니다."
                ));

        // when & then
        mockMvc.perform(
                        get("/api/admin/processing-logs/{id}", logId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNotFound());

        verify(adminProcessingLogService)
                .getProcessingLogDetail(logId);
    }

    @Test
    @DisplayName("USER - 심사 처리 이력 목록 조회 접근 차단")
    void getAllProcessingLogs_forbidden() throws Exception {

        // given
        String token = createToken(2L, Role.USER);

        // when & then
        mockMvc.perform(
                        get("/api/admin/processing-logs")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminProcessingLogService);
    }

    @Test
    @DisplayName("USER - 심사 처리 이력 상세 조회 접근 차단")
    void getProcessingLogDetail_forbidden() throws Exception {

        // given
        String token = createToken(2L, Role.USER);

        // when & then
        mockMvc.perform(
                        get("/api/admin/processing-logs/10")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminProcessingLogService);
    }
}
