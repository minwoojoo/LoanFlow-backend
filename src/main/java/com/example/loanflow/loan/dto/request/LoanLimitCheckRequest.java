package com.example.loanflow.loan.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class LoanLimitCheckRequest {
    @NotNull(message = "대출 신청서 ID는 필수입니다.")
    private Long applicationId;

    @NotBlank(message = "직업 유형은 필수입니다.")
    private String employmentType;

    @NotNull(message = "연 소득은 필수입니다.")
    @PositiveOrZero(message = "연 소득은 0 이상이어야 합니다.")
    private Long annualIncome;

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

}
