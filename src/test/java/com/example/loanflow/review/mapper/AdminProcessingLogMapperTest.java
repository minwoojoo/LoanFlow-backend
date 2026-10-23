
package com.example.loanflow.review.mapper;

import com.example.loanflow.admin.dto.AdminProcessingLogReadResponse;
import com.example.loanflow.admin.dto.AdminProcessingLogDetailResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;

@SpringBootTest
@Transactional
class AdminProcessingLogMapperTest {

    @Autowired
    private LoanProcessingLogMapper loanProcessingLogMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long applicationId;
    private Long memberId;
    private Long productId;

    private Long successLogId;
    private Long failedLogId;

    private String memberName;
    private String productName;
    private String requestId;

    @BeforeEach
    void setUp() {

        // 1. 테스트에 사용할 기존 회원 조회
        memberId = jdbcTemplate.queryForObject(
                "SELECT MIN(member_id) FROM member",
                Long.class
        );

        assertNotNull(memberId, "테스트용 회원이 필요합니다.");

        memberName = jdbcTemplate.queryForObject(
                "SELECT name FROM member WHERE member_id = ?",
                String.class,
                memberId
        );

        // 2. 테스트에 사용할 기존 대출 상품 조회
        productId = jdbcTemplate.queryForObject(
                "SELECT MIN(product_id) FROM loan_product",
                Long.class
        );

        assertNotNull(productId, "테스트용 대출 상품이 필요합니다.");

        productName = jdbcTemplate.queryForObject(
                "SELECT product_name FROM loan_product WHERE product_id = ?",
                String.class,
                productId
        );

        // 3. 테스트용 대출 신청서 생성
        requestId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO loan_application (" +
                        "member_id, product_id, request_id, " +
                        "application_status, is_deleted, " +
                        "created_at, updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, " +
                        "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                memberId,
                productId,
                requestId,
                "심사중",
                "N"
        );

        applicationId = jdbcTemplate.queryForObject(
                "SELECT application_id " +
                        "FROM loan_application " +
                        "WHERE request_id = ?",
                Long.class,
                requestId
        );

        assertNotNull(applicationId);

        // 4. 성공 처리 로그 생성
        jdbcTemplate.update(
                "INSERT INTO loan_processing_log (" +
                        "application_id, request_id, " +
                        "process_type, process_status, " +
                        "error_code, error_message, created_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, " +
                        "CURRENT_TIMESTAMP)",
                applicationId,
                requestId,
                "REVIEW_REQUEST",
                "SUCCESS",
                null,
                null
        );

        // 5. 실패 처리 로그 생성
        jdbcTemplate.update(
                "INSERT INTO loan_processing_log (" +
                        "application_id, request_id, " +
                        "process_type, process_status, " +
                        "error_code, error_message, created_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, " +
                        "CURRENT_TIMESTAMP)",
                applicationId,
                requestId,
                "REVIEW_CALLBACK",
                "FAILED",
                "CALLBACK_ERROR",
                "콜백 처리 중 오류가 발생했습니다."
        );

        // 6. 생성한 로그 ID 조회
        successLogId = jdbcTemplate.queryForObject(
                "SELECT log_id " +
                        "FROM loan_processing_log " +
                        "WHERE application_id = ? " +
                        "AND process_type = 'REVIEW_REQUEST' " +
                        "AND process_status = 'SUCCESS'",
                Long.class,
                applicationId
        );

        failedLogId = jdbcTemplate.queryForObject(
                "SELECT log_id " +
                        "FROM loan_processing_log " +
                        "WHERE application_id = ? " +
                        "AND process_type = 'REVIEW_CALLBACK' " +
                        "AND process_status = 'FAILED'",
                Long.class,
                applicationId
        );
    }

    @Test
    @DisplayName("운영자 - 전체 처리 이력 목록 조회 성공")
    void findAllProcessingLogs_success() {

        // when
        List<AdminProcessingLogReadResponse> result =
                loanProcessingLogMapper.findAllProcessingLogs();

        // then
        assertNotNull(result);

        // 이번 테스트에서 생성한 로그만 추출
        List<AdminProcessingLogReadResponse> testLogs =
                result.stream()
                        .filter(log ->
                                applicationId.equals(log.getApplicationId())
                        )
                        .collect(Collectors.toList());

        assertEquals(2, testLogs.size());

        assertTrue(
                testLogs.stream().anyMatch(log ->
                        successLogId.equals(log.getLogId())
                )
        );

        assertTrue(
                testLogs.stream().anyMatch(log ->
                        failedLogId.equals(log.getLogId())
                )
        );
    }

    @Test
    @DisplayName("운영자 - 처리 이력 목록에서 회원 및 상품 JOIN 확인")
    void findAllProcessingLogs_joinSuccess() {

        // when
        List<AdminProcessingLogReadResponse> result =
                loanProcessingLogMapper.findAllProcessingLogs();

        // then
        AdminProcessingLogReadResponse log =
                result.stream()
                        .filter(item ->
                                successLogId.equals(item.getLogId())
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(log);

        assertEquals(applicationId, log.getApplicationId());
        assertEquals(memberId, log.getMemberId());
        assertEquals(memberName, log.getMemberName());

        assertEquals(productName, log.getProductName());
        assertEquals("REVIEW_REQUEST", log.getProcessType());
        assertEquals("SUCCESS", log.getProcessStatus());

        assertNotNull(log.getCreatedAt());
    }

    @Test
    @DisplayName("운영자 - 성공 로그 상세 조회")
    void findProcessingLogDetail_success() {

        // when
        AdminProcessingLogDetailResponse result =
                loanProcessingLogMapper
                        .findProcessingLogDetail(successLogId);

        // then
        assertNotNull(result);

        assertEquals(successLogId, result.getLogId());
        assertEquals(applicationId, result.getApplicationId());
        assertEquals(requestId, result.getRequestId());

        assertEquals(memberId, result.getMemberId());
        assertEquals(memberName, result.getMemberName());

        assertEquals(productId, result.getProductId());
        assertEquals(productName, result.getProductName());

        assertEquals("REVIEW_REQUEST", result.getProcessType());
        assertEquals("SUCCESS", result.getProcessStatus());

        assertNull(result.getErrorCode());
        assertNull(result.getErrorMessage());

        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("운영자 - 실패 로그의 오류 코드 및 메시지 조회")
    void findProcessingLogDetail_failed() {

        // when
        AdminProcessingLogDetailResponse result =
                loanProcessingLogMapper
                        .findProcessingLogDetail(failedLogId);

        // then
        assertNotNull(result);

        assertEquals(failedLogId, result.getLogId());
        assertEquals("REVIEW_CALLBACK", result.getProcessType());
        assertEquals("FAILED", result.getProcessStatus());

        assertEquals("CALLBACK_ERROR", result.getErrorCode());
        assertEquals(
                "콜백 처리 중 오류가 발생했습니다.",
                result.getErrorMessage()
        );
    }

    @Test
    @DisplayName("운영자 - 존재하지 않는 로그 ID 조회 시 null")
    void findProcessingLogDetail_notFound() {

        // given
        Long nonexistentLogId = -1L;

        // when
        AdminProcessingLogDetailResponse result =
                loanProcessingLogMapper
                        .findProcessingLogDetail(nonexistentLogId);

        // then
        assertNull(result);
    }
}