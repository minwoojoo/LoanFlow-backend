package com.example.loanflow.auth.controller;

import com.example.loanflow.auth.dto.LoginRequest;
import com.example.loanflow.auth.dto.LoginResponse;
import com.example.loanflow.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Tag(
        name="인증",
        description = "사용자 로그인 및 JWT 인증 관련 API"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @Operation(
            summary = "사용자 로그인",
            description = "아이디와 비밀번호를 검증한 후 JWT Access Token을 발급합니다."
    )
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){

        return authService.login(request);
    }
}
