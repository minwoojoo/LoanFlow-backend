package com.example.loanflow.admin.mapper;

import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminDashboardMapperTest {

    @Autowired
    private AdminDashboardMapper adminDashboardMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long memberId;
    private Long productId;

    private AdminDashboardSummaryResponse beforeSummary;

    @BeforeEach
    void setUp() {

        // 1. 기존 통계 조회
        beforeSummary = adminDashboardMapper.findDashboardSummary();

        // 2. 테스트용 회원 조회
        memberId = jdbcTemplate.queryForObject(
                "SELECT MIN(member_id) FROM member",
                Long.class
        );

        assertNotNull(memberId, "테스트용 회원이 필요합니다.");

        // 3. 테스트용 대출 상품 조회
        productId = jdbcTemplate.queryForObject(
                "SELECT MIN(product_id) FROM loan_product",
                Long.class
        );

        assertNotNull(productId, "테스트용 대출 상품이 필요합니다.");

        // 4. 상태별 테스트 신청서 생성
        insertApplication("작성중", "N");
        insertApplication("심사중", "N");
        insertApplication("승인", "N");
        insertApplication("거절", "N");

        // 5. 삭제된 신청서 추가
        insertApplication("승인", "Y");
    }

    // 테스트 신청서 생성
    private void insertApplication(
            String status,
            String isDeleted
    ) {

        jdbcTemplate.update(
                "INSERT INTO loan_application (" +
                        "member_id, " +
                        "product_id, " +
                        "request_id, " +
                        "application_status, " +
                        "is_deleted" +
                        ") VALUES (?, ?, ?, ?, ?)",
                memberId,
                productId,
                UUID.randomUUID().toString(),
                status,
                isDeleted
        );
    }

    @Test
    @DisplayName("운영자 - 전체 신청 건수 및 상태별 통계 조회")
    void findDashboardSummary_success() {

        // when
        AdminDashboardSummaryResponse result =
                adminDashboardMapper.findDashboardSummary();

        // then
        assertNotNull(result);

        assertEquals(
                beforeSummary.getTotalCount() + 4,
                result.getTotalCount()
        );

        assertEquals(
                beforeSummary.getDraftCount() + 1,
                result.getDraftCount()
        );

        assertEquals(
                beforeSummary.getReviewingCount() + 1,
                result.getReviewingCount()
        );

        assertEquals(
                beforeSummary.getApprovedCount() + 1,
                result.getApprovedCount()
        );

        assertEquals(
                beforeSummary.getRejectedCount() + 1,
                result.getRejectedCount()
        );
    }

    @Test
    @DisplayName("운영자 - 전체 신청 건수와 상태별 건수 합계 일치")
    void findDashboardSummary_totalMatchesStatuses() {

        // when
        AdminDashboardSummaryResponse result =
                adminDashboardMapper.findDashboardSummary();

        // then
        long statusTotal =
                result.getDraftCount()
                        + result.getReviewingCount()
                        + result.getApprovedCount()
                        + result.getRejectedCount();

        assertEquals(result.getTotalCount(), statusTotal);
    }
}
