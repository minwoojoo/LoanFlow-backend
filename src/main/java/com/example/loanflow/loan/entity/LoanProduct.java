package com.example.loanflow.loan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {

    private Long productId;

    private String productName;

    private Long maxLimit;

    private BigDecimal baseInterestRate;

    private Long minAge;

    private Long maxAge;

    private Long maxLoanTerm;

    private String isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}