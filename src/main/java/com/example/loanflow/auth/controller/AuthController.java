package com.example.loanflow.auth.controller;

import com.example.loanflow.auth.dto.LoginRequest;
import com.example.loanflow.auth.dto.LoginResponse;
import com.example.loanflow.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;


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
            description = "아이디와 비밀번호를 검증한 후 Access Token과 Refresh Token을 발급합니다. " +
                    "Access Token은 응답 본문으로 반환하고, " +
                    "Refresh Token은 HttpOnly 쿠키에 저장합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){

        LoginResponse response = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)  // 로컬 HTTP 개발 환경
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(response);
    }


    @Operation(
            summary = "Access Token 및 Refresh Token 재발급",
            description = "HttpOnly 쿠키에 저장된 Refresh Token을 검증한 후, " +
                    "새로운 Access Token과 Refresh Token을 발급합니다. " +
                    "기존 Refresh Token은 무효화되며, " +
                    "새로운 Access Token은 응답 본문으로, " +
                    "Refresh Token은 HttpOnly 쿠키로 반환합니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(
                    value = "refreshToken",
                    required = false
            ) String rawRefreshToken
    ){
        // Refresh Token을 이용해 토큰 재발급
        LoginResponse response = authService.refresh(rawRefreshToken);

        // 새 Refresh Token을 HttpOnly 쿠키에 저장
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        // 새 Access Token을 JSON으로 반환
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(response);
    }



    @Operation(
            summary = "사용자 로그아웃",
            description = "HttpOnly 쿠키에 저장된 Refresh Token을 무효화하고, " +
                    "브라우저에 저장된 Refresh Token 쿠키를 삭제합니다. " +
                    "로그아웃 성공 시 HTTP 204 No Content를 반환합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    value = "refreshToken",
                    required = false
            ) String rawRefreshToken
    ) {
        // Refresh Token 무효화
        authService.logout(rawRefreshToken);

        // 브라우저의 Refresh Token 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();

        // 204 No Content 반환
        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        deleteCookie.toString()
                )
                .build();
    }

}
