package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoanReadResponse {
    private Long applicationId;

    private String productName;

    private Long applicationAmount;

    private String statusName;

    private LocalDateTime createdAt;

    private LocalDateTime appliedAt;
}
