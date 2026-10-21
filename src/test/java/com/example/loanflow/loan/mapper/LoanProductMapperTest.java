
package com.example.loanflow.loan.mapper;

import com.example.loanflow.loan.entity.LoanProduct;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional(readOnly = true)
class LoanProductMapperTest {

    @Autowired
    private LoanProductMapper loanProductMapper;

    // =========================================
    // 1. 활성 대출 상품 목록 조회
    // =========================================
    @Test
    @DisplayName("활성화된 대출 상품 목록을 정상적으로 조회한다")
    void findAllActive_success() {

        // when
        List<LoanProduct> products = loanProductMapper.findAllActive();

        // then
        assertNotNull(products);
        assertFalse(products.isEmpty());

        for (LoanProduct product : products) {
            assertNotNull(product.getProductId());
            assertNotNull(product.getProductName());
        }
    }

    // =========================================
    // 2. DB 컬럼과 Entity 필드 매핑 확인
    // =========================================
    @Test
    @DisplayName("대출 상품의 DB 컬럼이 Entity 필드에 정상 매핑된다")
    void findAllActive_mapping() {

        // when
        List<LoanProduct> products = loanProductMapper.findAllActive();

        // then
        assertNotNull(products);
        assertFalse(products.isEmpty());

        for (LoanProduct product : products) {

            assertNotNull(product.getProductId());
            assertNotNull(product.getProductName());
            assertNotNull(product.getBaseInterestRate());

            // maxLimit는 NULL을 허용하므로 검사하지 않는다.
        }
    }

    // =========================================
    // 3. 특정 활성 대출 상품 조회
    // =========================================
    @Test
    @DisplayName("상품 ID로 활성 대출 상품을 정상 조회한다")
    void findActiveById_success() {

        // given
        Long productId = 1L;

        // when
        LoanProduct product = loanProductMapper.findActiveById(productId);

        // then
        assertNotNull(product);
        assertEquals(productId, product.getProductId());

        assertNotNull(product.getProductName());
        assertNotNull(product.getBaseInterestRate());

        assertNotNull(product.getMinAge());
        assertNotNull(product.getMaxAge());
        assertNotNull(product.getMaxLoanTerm());
    }

    // =========================================
    // 4. 존재하지 않는 대출 상품 조회
    // =========================================
    @Test
    @DisplayName("존재하지 않는 상품을 조회하면 null을 반환한다")
    void findActiveById_notFound() {

        // given
        Long productId = -1L;

        // when
        LoanProduct product = loanProductMapper.findActiveById(productId);

        // then
        assertNull(product);
    }
}
