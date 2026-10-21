package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanApplicationCreateResponse {

    private Long applicationId;

    private String applicationStatus;
}
