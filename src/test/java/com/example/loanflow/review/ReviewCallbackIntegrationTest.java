package com.example.loanflow.review;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "mock.review.callback-api-key=integration-test-key"
})
@AutoConfigureMockMvc
class ReviewCallbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long applicationId;
    private String requestId;

    private static final String CALLBACK_URL =
            "/api/callback/review-results";

    private static final String API_KEY =
            "integration-test-key";

    @BeforeEach
    void setUp() {

        // 테스트마다 서로 다른 requestId 사용
        requestId = UUID.randomUUID().toString();

        // 테스트 전용 심사중 신청서 생성
        jdbcTemplate.update(
                "INSERT INTO loan_application (" +
                        "member_id, " + "request_id, " + "application_amount, " +
                        "employment_type, " + "annual_income, " + "loan_term, " +
                        "application_status, " + "is_deleted, " + "applied_at, " +
                        "created_at, " + "updated_at" + ") " +

                        "SELECT " +
                        "member_id, " + "?, " + "1000000, " +
                        "'직장인', " + "36000000, " + "12, " +
                        "'심사중', " + "'N', " + "SYSTIMESTAMP, " +
                        "SYSTIMESTAMP, " + "SYSTIMESTAMP " +
                        "FROM member " +
                        "FETCH FIRST 1 ROWS ONLY",
                requestId
        );

        applicationId = jdbcTemplate.queryForObject(
                "SELECT application_id " +
                        "FROM loan_application WHERE request_id = ?",
                Long.class,
                requestId
        );

        assertNotNull(applicationId);
    }


    @Test
    @DisplayName("승인 콜백을 받으면 승인 상태와 SUCCESS 로그가 저장된다")
    void callback_approved() throws Exception {

        // given
        String reviewResultId = UUID.randomUUID().toString();

        // when
        sendCallback(reviewResultId, "APPROVED")
                .andExpect(status().isOk());

        // then
        assertEquals("승인", findApplicationStatus());

        assertEquals(
                reviewResultId,
                findReviewResultId()
        );

        assertEquals(
                1,
                countCallbackLogs("SUCCESS")
        );
    }

    @Test
    @DisplayName("거절 콜백을 받으면 거절 상태와 SUCCESS 로그가 저장된다")
    void callback_rejected() throws Exception {

        // given
        String reviewResultId = UUID.randomUUID().toString();

        // when
        sendCallback(reviewResultId, "REJECTED")
                .andExpect(status().isOk());

        // then
        assertEquals("거절", findApplicationStatus());

        assertEquals(
                reviewResultId,
                findReviewResultId()
        );

        assertEquals(
                1,
                countCallbackLogs("SUCCESS")
        );
    }

    @Test
    @DisplayName("동일한 콜백 재전송 시 중복 로그가 저장된다")
    void callback_duplicate() throws Exception {

        // given
        String reviewResultId = UUID.randomUUID().toString();

        // 최초 콜백
        sendCallback(reviewResultId, "APPROVED")
                .andExpect(status().isOk());

        // when - 동일한 결과 다시 전송
        sendCallback(reviewResultId, "APPROVED")
                .andExpect(status().isOk());

        // then
        assertEquals("승인", findApplicationStatus());

        assertEquals(
                1,
                countCallbackLogs("SUCCESS")
        );

        assertEquals(
                1,
                countCallbackLogs("DUPLICATE")
        );
    }

    @Test
    @DisplayName("심사 완료 후 다른 결과가 오면 409와 FAILED 로그가 발생한다")
    void callback_conflict() throws Exception {

        // given - 먼저 승인 처리
        String reviewResultId = UUID.randomUUID().toString();

        sendCallback(reviewResultId, "APPROVED")
                .andExpect(status().isOk());

        // when - 다른 심사 결과 전달
        sendCallback(
                UUID.randomUUID().toString(),
                "REJECTED"
        ).andExpect(status().isConflict());

        // then - 기존 승인 결과 유지
        assertEquals("승인", findApplicationStatus());

        assertEquals(
                reviewResultId,
                findReviewResultId()
        );

        assertEquals(
                1,
                countCallbackLogs("FAILED")
        );
    }

    @Test
    @DisplayName("잘못된 심사 결과는 400과 FAILED 로그가 발생한다")
    void callback_invalidResult() throws Exception {

        // when
        sendCallback(
                UUID.randomUUID().toString(),
                "UNKNOWN"
        ).andExpect(status().isBadRequest());

        // then
        assertEquals("심사중", findApplicationStatus());

        assertEquals(
                1,
                countCallbackLogs("FAILED")
        );

        assertEquals(
                0,
                countCallbackLogs("SUCCESS")
        );
    }

    // -----------------------------------------
    // 테스트 편의 메서드
    // -----------------------------------------

    private org.springframework.test.web.servlet.ResultActions sendCallback(
            String reviewResultId,
            String reviewResult
    ) throws Exception {

        Map<String, Object> body = new HashMap<>();

        body.put("applicationId", applicationId);
        body.put("requestId", requestId);
        body.put("reviewResultId", reviewResultId);
        body.put("reviewResult", reviewResult);

        return mockMvc.perform(
                post(CALLBACK_URL)
                        .header("X-Callback-Api-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
        );
    }

    private String findApplicationStatus() {

        return jdbcTemplate.queryForObject(
                "SELECT application_status " +
                        "FROM loan_application " +
                        "WHERE application_id = ?",
                String.class,
                applicationId
        );
    }

    private String findReviewResultId() {

        return jdbcTemplate.queryForObject(
                "SELECT review_result_id " +
                        "FROM loan_application " +
                        "WHERE application_id = ?",
                String.class,
                applicationId
        );
    }

    private int countCallbackLogs(String processStatus) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM loan_processing_log " +
                        "WHERE application_id = ? " +
                        "AND process_type = 'REVIEW_CALLBACK' " +
                        "AND process_status = ?",
                Integer.class,
                applicationId,
                processStatus
        );

        return count == null ? 0 : count;
    }
}
