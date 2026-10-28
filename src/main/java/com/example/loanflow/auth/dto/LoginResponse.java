package com.example.loanflow.auth.dto;

import com.example.loanflow.member.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private String accessToken;

    private String tokenType;

    private Role role;

    @JsonIgnore
    private String refreshToken;
}
