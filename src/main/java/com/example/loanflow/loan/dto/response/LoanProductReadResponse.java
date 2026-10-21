package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LoanProductReadResponse {

    private Long productId;

    private String productName;

    private Long maxLimit;

    private BigDecimal baseInterestRate;

    private Long maxLoanTerm;

}
