package com.example.loanflow.auth.refresh.mapper;


import com.example.loanflow.auth.refresh.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {

    // Refresh Token 저장
    int insert(RefreshToken refreshToken);

    // 토큰 해시로 조회
    RefreshToken findByTokenHash(
            @Param("tokenHash") String tokenHash
    );

    // Refresh Token 무효화
    int revokeByTokenHash(
            @Param("tokenHash") String tokenHash
    );
}
