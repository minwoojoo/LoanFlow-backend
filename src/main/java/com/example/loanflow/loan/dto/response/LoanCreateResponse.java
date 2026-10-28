package com.example.loanflow.loan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanCreateResponse {

    private Long applicationId;

    private String statusName;
}
