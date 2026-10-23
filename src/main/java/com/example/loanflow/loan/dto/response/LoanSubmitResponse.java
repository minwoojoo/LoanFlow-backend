package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoanSubmitResponse {

    private Long applicationId;

    private String applicationStatus;

    private Long availableLimit;

    private BigDecimal estimatedInterestRate;

    private LocalDateTime appliedAt;
}