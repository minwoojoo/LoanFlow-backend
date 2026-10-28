package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailResponse {
    private Long applicationId;

    private Long productId;

    private String productName;

    private Long applicationAmount;

    private Long loanTerm;

    private Long availableLimit;

    private BigDecimal estimatedInterestRate;

    private String employmentType;

    private Long annualIncome;

    private LocalDate birthDate;

    private String statusName;

    private LocalDateTime createdAt;

    private LocalDateTime appliedAt;

    private LocalDateTime reviewedAt;

    private Long maxLoanTerm;
}
