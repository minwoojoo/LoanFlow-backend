package com.example.loanflow.member.entity;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private Long memberId;

    private String accountId;

    private String password;

    private String name;

    private Role role;

    private String employmentType;

    private Long annualIncome;

    private LocalDate birthDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
