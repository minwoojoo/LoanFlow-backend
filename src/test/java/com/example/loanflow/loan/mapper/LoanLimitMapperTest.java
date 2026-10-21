
package com.example.loanflow.loan.mapper;

import com.example.loanflow.loan.entity.LoanEmploymentPolicy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class LoanEmploymentPolicyMapperTest {

    @Autowired
    private LoanLimitMapper loanLimitMapper;

    private final Long productId = 1L;

    @Test
    @DisplayName("상품 ID와 직업 유형으로 한도 정책을 조회한다")
    void findByProductAndEmploymentType_success() {

        // when
        LoanEmploymentPolicy policy =
                loanLimitMapper.findByProductAndEmploymentType(
                        productId,
                        "EMPLOYED"
                );

        // then
        assertNotNull(policy);
        assertEquals(productId, policy.getProductId());
        assertEquals("EMPLOYED", policy.getEmploymentType());
    }

    @Test
    @DisplayName("직업별 한도 배수가 BigDecimal로 정상 매핑된다")
    void findByProductAndEmploymentType_mapping() {

        // when
        LoanEmploymentPolicy policy =
                loanLimitMapper.findByProductAndEmploymentType(
                        productId,
                        "EMPLOYED"
                );

        // then
        assertNotNull(policy);
        assertNotNull(policy.getLimitMultiplier());

        // 한도 배수는 0 이상이어야 한다.
        assertTrue(
                policy.getLimitMultiplier()
                        .compareTo(BigDecimal.ZERO) >= 0
        );
    }

    @Test
    @DisplayName("존재하지 않는 직업 유형은 null을 반환한다")
    void findByProductAndEmploymentType_notFound() {

        // when
        LoanEmploymentPolicy policy =
                loanLimitMapper.findByProductAndEmploymentType(
                        productId,
                        "UNKNOWN"
                );

        // then
        assertNull(policy);
    }
}
