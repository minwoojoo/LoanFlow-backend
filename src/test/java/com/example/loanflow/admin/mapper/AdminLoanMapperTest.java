package com.example.loanflow.admin.mapper;

import com.example.loanflow.admin.dto.AdminLoanReadResponse;
import com.example.loanflow.admin.dto.AdminLoanDetailResponse;
import com.example.loanflow.loan.mapper.LoanMapper;

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

@SpringBootTest
@Transactional
class AdminLoanMapperTest {

    @Autowired
    private LoanMapper loanMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long applicationId;
    private Long memberId;
    private Long productId;
    private String memberName;
    private String productName;

    @BeforeEach
    void setUp() {

        // 1. 기존 회원 한 명 조회
        memberId = jdbcTemplate.queryForObject(
                "SELECT MIN(member_id) FROM member",
                Long.class
        );

        assertNotNull(memberId, "테스트용 회원이 필요합니다.");

        // 2. 기존 대출 상품 한 개 조회
        productId = jdbcTemplate.queryForObject(
                "SELECT MIN(product_id) FROM loan_product",
                Long.class
        );

        assertNotNull(productId, "테스트용 대출 상품이 필요합니다.");

        // 3. 회원 이름 조회
        memberName = jdbcTemplate.queryForObject(
                "SELECT name FROM member WHERE member_id = ?",
                String.class,
                memberId
        );

        // 4. 대출 상품 이름 조회
        productName = jdbcTemplate.queryForObject(
                "SELECT product_name FROM loan_product WHERE product_id = ?",
                String.class,
                productId
        );

        // 5. 테스트용 신청서 생성
        String requestId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO loan_application (" +
                        "member_id, product_id, request_id, " +
                        "application_amount, loan_term, " +
                        "application_status, is_deleted, " +
                        "created_at, updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, " +
                        "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                memberId,
                productId,
                requestId,
                10000000L,
                12,
                "심사중",
                "N"
        );

        // 6. 생성한 신청서 ID 조회
        applicationId = jdbcTemplate.queryForObject(
                "SELECT application_id " +
                        "FROM loan_application " +
                        "WHERE request_id = ?",
                Long.class,
                requestId
        );
    }

    @Test
    @DisplayName("운영자 - 전체 신청 목록에 테스트 신청서가 포함된다")
    void findAllApplications_success() {

        // when
        List<AdminLoanReadResponse> result =
                loanMapper.findAllApplications();

        // then
        assertNotNull(result);

        AdminLoanReadResponse application =
                result.stream()
                        .filter(item ->
                                applicationId.equals(
                                        item.getApplicationId()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        assertNotNull(application);

        assertEquals(memberId, application.getMemberId());
        assertEquals(memberName, application.getMemberName());

        assertEquals(productId, application.getProductId());
        assertEquals(productName, application.getProductName());

        assertEquals(
                10000000L,
                application.getApplicationAmount()
        );

        assertEquals(
                "심사중",
                application.getApplicationStatus()
        );

        assertNotNull(application.getCreatedAt());
        assertEquals("N", application.getIsDeleted());
    }

    @Test
    @DisplayName("운영자 - 대출 신청 상세 조회 성공")
    void findAdminApplicationDetail_success() {

        // when
        AdminLoanDetailResponse result =
                loanMapper.findAdminApplicationDetail(applicationId);

        // then
        assertNotNull(result);

        assertEquals(applicationId, result.getApplicationId());
        assertEquals(memberId, result.getMemberId());
        assertEquals(memberName, result.getMemberName());

        assertEquals(productId, result.getProductId());
        assertEquals(productName, result.getProductName());

        assertEquals(
                10000000L,
                result.getApplicationAmount()
        );

        assertEquals(12L, result.getLoanTerm());
        assertEquals("심사중", result.getApplicationStatus());

        assertNotNull(result.getRequestId());
        assertNotNull(result.getCreatedAt());
        assertEquals("N", result.getIsDeleted());
    }

    @Test
    @DisplayName("운영자 - 존재하지 않는 신청서 상세 조회 시 null")
    void findAdminApplicationDetail_notFound() {

        // given
        Long nonexistentId = -1L;

        // when
        AdminLoanDetailResponse result =
                loanMapper.findAdminApplicationDetail(nonexistentId);

        // then
        assertNull(result);
    }
}
