package com.example.loanflow.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    private String employmentType;

    @Min(value = 0, message = "연 소득은 0원 이상이어야 합니다.")
    private Long annualIncome;

    private LocalDate birthDate;
}
