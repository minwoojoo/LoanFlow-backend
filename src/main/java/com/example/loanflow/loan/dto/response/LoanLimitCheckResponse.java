package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LoanLimitCheckResponse {
    // 대출 신청서 ID
    private Long applicationId;

    private Long availableLimit;

    private BigDecimal estimatedInterestRate;

    private Boolean eligible;

    private String ineligibleReason;
}
