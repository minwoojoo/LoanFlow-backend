package com.example.loanflow.loan.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class LoanDraftResponse {
    private Long applicationId;

    private Long productId;

    String applicationStatus;

    private Long applicationAmount;

    private LocalDate birthDate;

    private String employmentType;

    private Long annualIncome;

    private Long availableLimit;

    private BigDecimal estimatedInterestRate;

    private Long loanTerm;
}
