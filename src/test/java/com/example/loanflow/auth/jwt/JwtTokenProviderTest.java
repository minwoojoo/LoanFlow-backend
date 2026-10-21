package com.example.loanflow.auth.jwt;

import com.example.loanflow.member.entity.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createAccessTokenTest(){
        // 1. JWT 생성
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);

        // 2. 토큰 생성 여부 확인
        assertNotNull(token);

        // 3. 토큰 검증 및 Claims 추출
        Claims claims = jwtTokenProvider.parseClaims(token);

        // 4. 회원 ID 확인
        assertEquals("1", claims.getSubject());

        // 5. 역할 확인
        assertEquals("USER", claims.get("role", String.class));

        // 6. 만료시간 확인
        assertTrue(claims.getExpiration().after(new java.util.Date()));
    }

}
