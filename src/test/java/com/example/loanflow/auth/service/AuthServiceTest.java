package com.example.loanflow.auth.service;

import com.example.loanflow.auth.dto.LoginResponse;
import com.example.loanflow.member.entity.Role;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.loanflow.auth.dto.LoginRequest;
import com.example.loanflow.member.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void loginSuccessTest(){
        LoginRequest request = new LoginRequest();
        request.setAccountId("testuser");
        request.setPassword("1234");

        Member member = authService.validateLogin(request);

        assertNotNull(member);
        assertEquals("testuser", member.getAccountId());

    }

    @Test
    void loginFailTest(){
        LoginRequest request = new LoginRequest();
        request.setAccountId("testuser");
        request.setPassword("wrong1234");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () ->
                        authService.validateLogin(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void loginTokenSuccessTest() {
        // 1. 로그인 요청 생성
        LoginRequest request = new LoginRequest();
        request.setAccountId("testuser");
        request.setPassword("1234");

        // 2. 로그인 및 JWT 발급
        LoginResponse response = authService.login(request);

        // 3. JWT 발급 여부 확인
        assertNotNull(response.getAccessToken());

        // 4. 토큰 타입 확인
        assertEquals("Bearer", response.getTokenType());

        // 5. 사용자 역할 확인
        assertEquals(Role.USER, response.getRole());
    }
}
