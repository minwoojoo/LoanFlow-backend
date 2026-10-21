package com.example.loanflow.loan.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationConsent {
    private Long consentId;

    private Long applicationId;

    private Long termsId;

    private String agreedYn;

    private LocalDateTime agreedAt;
}
