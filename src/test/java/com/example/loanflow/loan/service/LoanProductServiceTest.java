package com.example.loanflow.loan.service;


import com.example.loanflow.loan.dto.response.LoanProductReadResponse;
import com.example.loanflow.loan.entity.LoanProduct;
import com.example.loanflow.loan.mapper.LoanProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanProductServiceTest {
    @Mock
    private LoanProductMapper loanProductMapper;

    @InjectMocks
    private LoanProductService loanProductService;


    @Test
    @DisplayName("활성 대출 상품 목록을 정상적으로 조회한다")
    void findAllActive_success(){

        // given
        LoanProduct product = mock(LoanProduct.class);

        when(product.getProductId()).thenReturn(1L);
        when(product.getProductName()).thenReturn("일반 신용대출");
        when(product.getMaxLimit()).thenReturn(50000000L);
        when(product.getBaseInterestRate())
                .thenReturn(new BigDecimal("4.50"));

        when(loanProductMapper.findAllActive())
                .thenReturn(Arrays.asList(product));

        // when
        List<LoanProductReadResponse> responses =
                loanProductService.getActiveProducts();

        // then
        assertEquals(1, responses.size());

        LoanProductReadResponse response = responses.get(0);

        assertEquals(1L, response.getProductId());
        assertEquals("일반 신용대출", response.getProductName());
        assertEquals(50000000L, response.getMaxLimit());

        assertEquals(
                0,
                new BigDecimal("4.50")
                        .compareTo(response.getBaseInterestRate())
        );

        verify(loanProductMapper, times(1)).findAllActive();
    }

    @Test
    @DisplayName("활성 대출 상품이 없으면 빈 목록을 반환한다")
    void findAllActive_empty() {

        // given
        when(loanProductMapper.findAllActive())
                .thenReturn(Collections.emptyList());

        // when
        List<LoanProductReadResponse> responses =
                loanProductService.getActiveProducts();

        // then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(loanProductMapper, times(1)).findAllActive();
    }
}
