package com.example.loanflow.auth.service;

import com.example.loanflow.auth.refresh.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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
    private final RefreshTokenService refreshTokenService;

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


    // 로그인 처리 및 access/refresh token 발급
    @Transactional
    public LoginResponse login(LoginRequest request){
        // 1. 아이디, 비밀번호 검증
        Member member = validateLogin(request);

        // 2. JWT Access Token 생성
        String accessToken =
                jwtTokenProvider.createAccessToken(member.getMemberId(), member.getRole());

        // 3. Refresh Token 생성 및 DB 저장
        String refreshToken =
                refreshTokenService.createRefreshToken(member.getMemberId());


        // 4. Controller에 두 토큰 전달
        return new LoginResponse(
                accessToken,
                "Bearer",
                member.getRole(),
                refreshToken
        );
    }


    // 토큰 재발급
    @Transactional
    public LoginResponse refresh(String rawRefreshToken){
        // 기존 Refresh Token 검증
        Long memberId =
                refreshTokenService.validateRefreshToken(rawRefreshToken);

        // 회원 조회
        Member member = memberMapper.findById(memberId);

        if (member == null){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "존재하지 않는 회원입니다."
            );
        }

        // Refresh Token 교체
        String newRefreshToken =
                refreshTokenService.rotateRefreshToken(rawRefreshToken);

        // 새로운 Access Token 생성
        String newAccessToken =
                jwtTokenProvider.createAccessToken(
                        member.getMemberId(),
                        member.getRole()
                );

        return new LoginResponse(
                newAccessToken,
                "Bearer",
                member.getRole(),
                newRefreshToken
        );
    }


    @Transactional
    public void logout(String rawRefreshToken){

        // Refresh Token 무효화
        refreshTokenService.revokeRefreshToken(rawRefreshToken);
    }
}
