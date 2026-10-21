
package com.example.loanflow.loan.mapper;

import com.example.loanflow.loan.dto.response.LoanApplicationDetailResponse;
import com.example.loanflow.loan.entity.LoanApplication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LoanApplicationMapperTest {

    @Autowired
    private LoanApplicationMapper loanApplicationMapper;

    private final Long memberId = 41L;
    private final Long productId = 1L;
    private final Long termsId = 1L;

    // ========================================
    // 1. 작성 중인 신청서 조회 성공
    // ========================================
    @Test
    @DisplayName("동일 회원과 상품의 작성 중 신청서를 조회한다")
    void findWritingApplication_success() {

        // given: 테스트용 작성 중 신청서 생성
        LoanApplication savedApplication = createTestApplication();

        // when
        LoanApplication application =
                loanApplicationMapper.findWritingApplication(
                        memberId,
                        productId
                );

        // then
        assertNotNull(application);
        assertEquals(memberId, application.getMemberId());
        assertEquals(productId, application.getProductId());
        assertEquals("작성중", application.getApplicationStatus());
    }

    // ========================================
    // 2. 작성 중인 신청서가 없는 경우
    // ========================================
    @Test
    @DisplayName("작성 중인 신청서가 없으면 null을 반환한다")
    void findWritingApplication_notFound() {

        // given
        Long nonexistentProductId = -1L;

        // when
        LoanApplication application =
                loanApplicationMapper.findWritingApplication(
                        memberId,
                        nonexistentProductId
                );

        // then
        assertNull(application);
    }

    // ========================================
    // 3. 대출 신청서 INSERT
    // ========================================
    @Test
    @DisplayName("대출 신청서를 생성하고 자동 생성된 PK를 가져온다")
    void insertApplication_success() {

        // given
        LoanApplication application = LoanApplication.builder()
                .memberId(memberId)
                .productId(productId)
                .requestId(UUID.randomUUID().toString())
                .applicationStatus("작성중")
                .build();

        // when
        int rows = loanApplicationMapper.insertApplication(application);

        // then
        assertEquals(1, rows);
        assertNotNull(application.getApplicationId());
        assertTrue(application.getApplicationId() > 0L);
    }

    // ========================================
    // 4. 약관 동의 내역 INSERT
    // ========================================
    @Test
    @DisplayName("생성한 신청서에 약관 동의 내역을 저장한다")
    void insertConsent_success() {

        // given
        LoanApplication application = LoanApplication.builder()
                .memberId(memberId)
                .productId(productId)
                .requestId(UUID.randomUUID().toString())
                .applicationStatus("작성중")
                .build();

        // 먼저 신청서 생성
        int applicationRows =
                loanApplicationMapper.insertApplication(application);

        assertEquals(1, applicationRows);
        assertNotNull(application.getApplicationId());

        // when
        int consentRows = loanApplicationMapper.insertConsent(
                application.getApplicationId(),
                termsId
        );

        // then
        assertEquals(1, consentRows);
    }


    private LoanApplication createTestApplication() {

        LoanApplication application = LoanApplication.builder()
                .memberId(memberId)
                .productId(productId)
                .requestId(UUID.randomUUID().toString())
                .applicationStatus("작성중")
                .build();

        int rows = loanApplicationMapper.insertApplication(application);

        assertEquals(1, rows);
        assertNotNull(application.getApplicationId());

        return application;
    }


    @Test
    @DisplayName("최종 제출 시 신청서 상태, 예상 한도, 금리, 제출 일시가 저장된다")
    void submitApplication_success() {

        // given: 작성 중인 테스트 신청서 생성
        LoanApplication application = createTestApplication();

        Long applicationId = application.getApplicationId();
        Long availableLimit = 40000000L;
        BigDecimal interestRate = new BigDecimal("5.5");

        // when: 최종 제출 UPDATE 실행
        int updatedRows = loanApplicationMapper.submitApplication(
                applicationId,
                memberId,
                availableLimit,
                interestRate
        );

        // then: UPDATE 건수 확인
        assertEquals(1, updatedRows);

        // DB에서 제출 완료된 신청서 조회
        LoanApplication submittedApplication =
                loanApplicationMapper.findByIdAndMember(
                        applicationId,
                        memberId
                );

        assertNotNull(submittedApplication);

        // 최종 제출 상태 검증
        assertEquals(
                "심사중",
                submittedApplication.getApplicationStatus()
        );

        // 예상 한도 검증
        assertEquals(
                availableLimit,
                submittedApplication.getAvailableLimit()
        );

        // 예상 금리 검증
        assertEquals(
                0,
                interestRate.compareTo(
                        submittedApplication.getEstimatedInterestRate()
                )
        );

        // DB에 실제 제출 일시가 저장되었는지 검증
        assertNotNull(submittedApplication.getAppliedAt());
    }


    @Test
    @DisplayName("본인의 대출 신청 상세 정보와 상품명을 조회한다")
    void findApplicationDetail_success() {

        // given: 테스트 신청서 생성
        LoanApplication application = createTestApplication();

        Long applicationId = application.getApplicationId();

        // when: 신청서 상세 조회
        LoanApplicationDetailResponse response =
                loanApplicationMapper.findApplicationDetail(
                        applicationId,
                        memberId
                );

        // then
        assertNotNull(response);

        // 신청서 정보 검증
        assertEquals(applicationId, response.getApplicationId());
        assertEquals(productId, response.getProductId());
        assertEquals("작성중", response.getApplicationStatus());

        // JOIN으로 가져온 상품명 확인
        assertNotNull(response.getProductName());
        assertFalse(response.getProductName().trim().isEmpty());

        // 생성 시각 확인
        assertNotNull(response.getCreatedAt());

        // 아직 제출 전이므로 제출 일시는 null
        assertNull(response.getAppliedAt());
    }


    @Test
    @DisplayName("다른 회원의 대출 신청 상세 정보는 조회할 수 없다")
    void findApplicationDetail_notOwner() {

        // given: 회원 41의 신청서 생성
        LoanApplication application = createTestApplication();

        Long applicationId = application.getApplicationId();

        // 다른 회원 ID
        Long otherMemberId = -1L;

        // when
        LoanApplicationDetailResponse response =
                loanApplicationMapper.findApplicationDetail(
                        applicationId,
                        otherMemberId
                );

        // then
        assertNull(response);
    }

}
