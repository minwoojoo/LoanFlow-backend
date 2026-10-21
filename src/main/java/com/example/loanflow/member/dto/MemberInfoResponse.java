package com.example.loanflow.member.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MemberInfoResponse {
    private Long memberId;

    private String name;

    private String employmentType;

    private Long annualIncome;

    private LocalDate birthDate;
}
