package com.example.loanflow.loan.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {
    private Long applicationId;

    private Long memberId;

    private Long productId;

    private String statusCode;

    private String requestId;

    private Long applicationAmount;

    private LocalDate birthDate;

    private String employmentType;

    private Long annualIncome;

    private Long availableLimit;

    private BigDecimal estimatedInterestRate;

    private Long loanTerm;

    private String reviewResultId;

    private String isDeleted;

    private LocalDateTime deletedAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime appliedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
