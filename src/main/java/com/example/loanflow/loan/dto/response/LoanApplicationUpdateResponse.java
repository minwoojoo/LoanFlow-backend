package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class LoanApplicationUpdateResponse {

    private Long applicationId;

    private String applicationStatus;

    private Long applicationAmount;

    private Long loanTerm;

    private String employmentType;

    private Long annualIncome;

    private LocalDate birthDate;
}
