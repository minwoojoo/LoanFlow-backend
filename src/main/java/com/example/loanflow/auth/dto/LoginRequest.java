package com.example.loanflow.auth.dto;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "아이디를 입력해 주세요.")
    private String accountId;

    @NotBlank(message = "비밀번호를 입력해 주세요")
    private String password;
}
