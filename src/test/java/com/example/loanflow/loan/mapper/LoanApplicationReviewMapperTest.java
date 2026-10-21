package com.example.loanflow.loan.mapper;

import com.example.loanflow.loan.entity.LoanApplication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LoanApplicationReviewMapperTest {

    @Autowired
    private LoanApplicationMapper loanApplicationMapper;

    // 실제 DB에 존재하는 '심사중' 신청서 정보로 변경
    private static final Long TEST_APPLICATION_ID = 47L;
    private static final String TEST_REQUEST_ID = "c7fe8fd7-01c4-465d-b409-7fcd1ecc17e6";

    @Test
    @DisplayName("신청서 ID와 요청 식별키로 심사 대상 신청서를 조회한다")
    void findByApplicationAndRequestId_success() {

        // given
        Long applicationId = TEST_APPLICATION_ID;
        String requestId = TEST_REQUEST_ID;

        // when
        LoanApplication application =
                loanApplicationMapper.findByApplicationAndRequestId(
                        applicationId,
                        requestId
                );

        // then
        assertNotNull(application);
        assertEquals(applicationId, application.getApplicationId());
        assertEquals(requestId, application.getRequestId());
        assertEquals("심사중", application.getApplicationStatus());
    }

    @Test
    @DisplayName("심사중인 신청서에 승인 결과를 반영한다")
    void updateReviewResult_success() {

        // given
        Long applicationId = TEST_APPLICATION_ID;
        String requestId = TEST_REQUEST_ID;
        String reviewResultId = UUID.randomUUID().toString();

        // when
        int updatedRows = loanApplicationMapper.updateReviewResult(
                applicationId,
                requestId,
                reviewResultId,
                "승인"
        );

        // then
        assertEquals(1, updatedRows);

        LoanApplication updatedApplication =
                loanApplicationMapper.findByApplicationAndRequestId(
                        applicationId,
                        requestId
                );

        assertNotNull(updatedApplication);
        assertEquals("승인", updatedApplication.getApplicationStatus());
        assertEquals(
                reviewResultId,
                updatedApplication.getReviewResultId()
        );
    }

    @Test
    @DisplayName("이미 승인된 신청서는 중복 UPDATE되지 않는다")
    void updateReviewResult_alreadyReviewed() {

        // given
        Long applicationId = TEST_APPLICATION_ID;
        String requestId = TEST_REQUEST_ID;

        String firstResultId = UUID.randomUUID().toString();
        String secondResultId = UUID.randomUUID().toString();

        // 첫 번째 심사 결과 반영
        int firstUpdatedRows =
                loanApplicationMapper.updateReviewResult(
                        applicationId,
                        requestId,
                        firstResultId,
                        "승인"
                );

        assertEquals(1, firstUpdatedRows);

        // when: 동일 신청서에 두 번째 결과 반영 시도
        int secondUpdatedRows =
                loanApplicationMapper.updateReviewResult(
                        applicationId,
                        requestId,
                        secondResultId,
                        "거절"
                );

        // then
        assertEquals(0, secondUpdatedRows);

        LoanApplication application =
                loanApplicationMapper.findByApplicationAndRequestId(
                        applicationId,
                        requestId
                );

        assertNotNull(application);
        assertEquals("승인", application.getApplicationStatus());
        assertEquals(firstResultId, application.getReviewResultId());
    }

    @Test
    @DisplayName("요청 식별키가 일치하지 않으면 심사 결과를 반영하지 않는다")
    void updateReviewResult_invalidRequestId() {

        // given
        Long applicationId = TEST_APPLICATION_ID;
        String invalidRequestId = UUID.randomUUID().toString();
        String reviewResultId = UUID.randomUUID().toString();

        // when
        int updatedRows =
                loanApplicationMapper.updateReviewResult(
                        applicationId,
                        invalidRequestId,
                        reviewResultId,
                        "승인"
                );

        // then
        assertEquals(0, updatedRows);
    }
}
