package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class LoanUpdateResponse {

    private Long applicationId;

    private Long memberId;

    private Long productId;

    private String statusName;

    private Long applicationAmount;

    private Long loanTerm;

    private String employmentType;

    private Long annualIncome;

    private LocalDate birthDate;

    private Long availableLimit;

    private BigDecimal estimatedInterestRate;
}
