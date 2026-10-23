
package com.example.loanflow.loan.service;

import com.example.loanflow.loan.dto.request.LoanLimitCheckRequest;
import com.example.loanflow.loan.dto.response.LoanLimitCheckResponse;
import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.entity.LoanEmploymentPolicy;
import com.example.loanflow.loan.entity.LoanProduct;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.loan.mapper.LoanLimitMapper;
import com.example.loanflow.loan.mapper.LoanProductMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanLimitServiceTest {

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoanProductMapper loanProductMapper;

    @Mock
    private LoanLimitMapper loanLimitMapper;

    @InjectMocks
    private LoanLimitService loanLimitService;

    private Long memberId;
    private Long applicationId;
    private Long productId;

    private LoanLimitCheckRequest request;
    private LoanApplication application;
    private LoanProduct product;
    private LoanEmploymentPolicy policy;

    @BeforeEach
    void setUp() {

        memberId = 41L;
        applicationId = 22L;
        productId = 1L;

        // Request DTO
        request = mock(LoanLimitCheckRequest.class);

        lenient().when(request.getApplicationId())
                .thenReturn(applicationId);
        lenient().when(request.getEmploymentType())
                .thenReturn("EMPLOYED");
        lenient().when(request.getAnnualIncome())
                .thenReturn(50000000L);
        lenient().when(request.getBirthDate())
                .thenReturn(LocalDate.now().minusYears(25));

        // 신청서 Entity
        application = mock(LoanApplication.class);

        lenient().when(application.getApplicationId())
                .thenReturn(applicationId);
        lenient().when(application.getProductId())
                .thenReturn(productId);

        // 상품 Entity
        product = mock(LoanProduct.class);

        lenient().when(product.getProductId())
                .thenReturn(productId);
        lenient().when(product.getMinAge())
                .thenReturn(20L);
        lenient().when(product.getMaxAge())
                .thenReturn(65L);
        lenient().when(product.getMaxLoanTerm())
                .thenReturn(60L);
        lenient().when(product.getMaxLimit())
                .thenReturn(100000000L);
        lenient().when(product.getBaseInterestRate())
                .thenReturn(new BigDecimal("4.50"));

        // 직업별 한도 정책 Entity
        policy = mock(LoanEmploymentPolicy.class);

        lenient().when(policy.getLimitMultiplier())
                .thenReturn(new BigDecimal("0.80"));

        // 공통 Mapper 동작
        lenient().when(
                loanMapper.findWritingByIdAndMember(
                        applicationId, memberId
                )
        ).thenReturn(application);

        lenient().when(
                loanProductMapper.findActiveById(productId)
        ).thenReturn(product);

        lenient().when(
                loanLimitMapper.findByProductAndEmploymentType(
                        productId, "EMPLOYED"
                )
        ).thenReturn(policy);
    }

    @Test
    @DisplayName("정상적으로 예상 한도 및 금리를 조회한다")
    void checkLimit_success() {

        LoanLimitCheckResponse response =
                loanLimitService.checkLimit(memberId, request);

        assertTrue(response.getEligible());
        assertEquals(40000000L, response.getAvailableLimit());

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(response.getEstimatedInterestRate())
        );

        assertNull(response.getIneligibleReason());
    }

    @Test
    @DisplayName("계산 결과가 상품 최대 한도를 초과하면 상한을 적용한다")
    void checkLimit_maxLimit() {

        when(product.getMaxLimit()).thenReturn(30000000L);

        LoanLimitCheckResponse response =
                loanLimitService.checkLimit(memberId, request);

        assertEquals(30000000L, response.getAvailableLimit());
        assertTrue(response.getEligible());
    }

    @Test
    @DisplayName("상품 최대 한도가 없으면 계산된 한도를 그대로 반환한다")
    void checkLimit_noMaxLimit() {

        when(product.getMaxLimit()).thenReturn(null);

        LoanLimitCheckResponse response =
                loanLimitService.checkLimit(memberId, request);

        assertEquals(40000000L, response.getAvailableLimit());
    }

    @Test
    @DisplayName("최소 신청 나이 미달 시 조회 불가 결과를 반환한다")
    void checkLimit_underMinAge() {

        when(request.getBirthDate())
                .thenReturn(LocalDate.now().minusYears(19));

        LoanLimitCheckResponse response =
                loanLimitService.checkLimit(memberId, request);

        assertFalse(response.getEligible());
        assertNull(response.getAvailableLimit());
        assertNotNull(response.getIneligibleReason());
    }

    @Test
    @DisplayName("최대 신청 나이 초과 시 조회 불가 결과를 반환한다")
    void checkLimit_overMaxAge() {

        when(request.getBirthDate())
                .thenReturn(LocalDate.now().minusYears(66));

        LoanLimitCheckResponse response =
                loanLimitService.checkLimit(memberId, request);

        assertFalse(response.getEligible());
        assertNull(response.getAvailableLimit());
        assertNotNull(response.getIneligibleReason());
    }

    @Test
    @DisplayName("작성 중인 신청서가 없으면 404를 반환한다")
    void checkLimit_applicationNotFound() {

        when(loanMapper.findWritingByIdAndMember(
                applicationId, memberId
        )).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanLimitService.checkLimit(memberId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("활성 대출 상품이 없으면 400을 반환한다")
    void checkLimit_productNotFound() {

        when(loanProductMapper.findActiveById(productId))
                .thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanLimitService.checkLimit(memberId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    @DisplayName("직업별 한도 정책이 없으면 400을 반환한다")
    void checkLimit_policyNotFound() {

        when(loanLimitMapper.findByProductAndEmploymentType(
                productId, "EMPLOYED"
        )).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanLimitService.checkLimit(memberId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
