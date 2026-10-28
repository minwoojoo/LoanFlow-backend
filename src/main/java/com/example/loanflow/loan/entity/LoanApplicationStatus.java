package com.example.loanflow.loan.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationStatus {
    private String statusCode;

    private String statusName;
}
