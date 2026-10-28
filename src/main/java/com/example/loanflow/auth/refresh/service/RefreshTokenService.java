package com.example.loanflow.auth.refresh.service;


import com.example.loanflow.auth.refresh.entity.RefreshToken;
import com.example.loanflow.auth.refresh.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;

    // Refresh Token 만료 기간: 7일
    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    // 32바이트 = 256 비트 랜덤 토큰
    private static final int TOKEN_BYTE_LENGTH = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();



    // 1. Refresh Token 생성 및 DB 저장
    @Transactional
    public String createRefreshToken(Long memberId) {
        // 랜덤 Refresh Token 생성
        String rawToken = generateRandomToken();

        // SHA-256 해시 생성
        String tokenHash = hashToken(rawToken);

        // 만료 시간 계산 (UTC)
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(REFRESH_TOKEN_EXPIRATION_DAYS);

        // Entity 생성
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setMemberId(memberId);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(expiresAt);

        // DB 저장
        int insertedRows = refreshTokenMapper.insert(refreshToken);

        if (insertedRows != 1) {
            throw new IllegalStateException(
                    "Refresh Token 저장에 실패했습니다."
            );
        }

        // 원본 토큰은 클라이언트에 전달
        return rawToken;
    }


    // 2. Refresh Token 검증
    @Transactional(readOnly = true)
    public Long validateRefreshToken(String rawToken){

        if (rawToken == null || rawToken.isBlank()){
            throw unauthorized();
        }

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenMapper.findByTokenHash(tokenHash);

        // 존재하지 않는 토큰
        if (refreshToken == null){
            throw unauthorized();
        }

        // 이미 무효화된 토큰
        if (refreshToken.getRevokedAt() != null){
            throw unauthorized();
        }

        // 만료된 토큰
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (!refreshToken.getExpiresAt().isAfter(now)){
            throw unauthorized();
        }

        // 발글받은 회원 ID 반환
        return refreshToken.getMemberId();

    }


    // 3. Refresh Token Rotation
    @Transactional
    public String rotateRefreshToken(String rawToken){

        // 기존 토큰 검증
        Long memberId = validateRefreshToken(rawToken);

        // 기존 토큰 무효화
        String tokenHash = hashToken(rawToken);

        int updatedRows = refreshTokenMapper.revokeByTokenHash(tokenHash);

        if (updatedRows != 1){
            throw unauthorized();
        }

        // 새로운 Refresh Token 생성 및 저장
        return createRefreshToken(memberId);
    }


    // 4. Refresh Token 무효화 (로그아웃)
    @Transactional
    public void revokeRefreshToken(String rawToken){
        if (rawToken == null || rawToken.isBlank()){
            return;
        }

        String tokenHash = hashToken(rawToken);

        refreshTokenMapper.revokeByTokenHash(tokenHash);
    }




    // 5. 랜덤 토큰 생성
    private String generateRandomToken(){
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }


    // 6. SHA-256 해시 생성
    private String hashToken(String rawToken){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes){
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    e
            );
        }
    }


    // 7. 인증 실패 예외
    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "유효하지 않거나 만료된 Refresh Token입니다."
        );
    }


}
