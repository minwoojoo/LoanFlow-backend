package com.example.loanflow.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthTestController {

    // 로그인한 사용자만 접근 가능
    @GetMapping("/api/test/user")
    public String userTest(Authentication authentication){
        return "관리자 인증 성공! memberId = " + authentication.getPrincipal();
    }

    // 관리자만 접근 가능
    @GetMapping("/api/admin/test")
    public String adminTest(Authentication authentication){
        return "관리자 인증 성공! memberId = " + authentication.getPrincipal();
    }
}
