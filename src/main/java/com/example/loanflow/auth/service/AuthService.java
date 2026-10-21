package com.example.loanflow.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.loanflow.auth.dto.LoginRequest;
import com.example.loanflow.auth.dto.LoginResponse;
import com.example.loanflow.auth.jwt.JwtTokenProvider;
import com.example.loanflow.member.entity.Member;
import com.example.loanflow.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 아이디와 비밀번호 검증
    public Member validateLogin(LoginRequest request){

        Member member = memberMapper.findAccountId(request.getAccountId());

        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return member;
    }

    // 로그인 처리 및 JWT 발급
    public LoginResponse login(LoginRequest request){
        // 1. 아이디, 비밀번호 검증
        Member member = validateLogin(request);

        // 2. JWT Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId(), member.getRole());

        // 3. 로그인 응답 변환
        return new LoginResponse(
                accessToken,
                "Bearer",
                member.getRole()
        );
    }

}
