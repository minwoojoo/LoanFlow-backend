package com.example.loanflow.loan.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Past;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class LoanApplicationUpdateRequest {

    // 희망 대출 금액
    @Positive(message = "희망 대출 금액은 1원 이상이어야 합니다.")
    private Long applicationAmount;

    // 희망 대출 기간 (개월)
    @Positive(message = "대출 기간은 1개월 이상이어야 합니다.")
    private Long loanTerm;

    // 직업 유형
    private String employmentType;

    // 연 소득
    @PositiveOrZero(message = "연 소득은 0 이상이어야 합니다.")
    private Long annualIncome;

    // 생년월일
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;
}