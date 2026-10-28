package com.example.loanflow.auth.jwt;

import com.example.loanflow.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException{
        // 1. Authorization 헤더 가져오기
        String authorization = request.getHeader("Authorization");

        // 2. Bearer 토큰인지 확인
        if (authorization != null && authorization.startsWith("Bearer ")){
            String token = authorization.substring(7);

            try{
                // 3. JWT 서명 및 만료 검증
                Claims claims = jwtTokenProvider.parseClaims(token);

                // 4. 회원 ID와 역할 추출
                Long memberId = Long.valueOf(claims.getSubject());
                Role role = Role.valueOf(claims.get("role", String.class));

                // 5. Spring Security 권한 객체 생성
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

                // 6. 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        memberId,
                        null,
                        Collections.singletonList(authority)
                );

                // 7. SecurityContext에 인증 정보 등록
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch(JwtException | IllegalArgumentException e){
                // 잘못된 토큰은 인증 정보로 등록하지 않음
                SecurityContextHolder.clearContext();

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired JWT"
                );
                return;
            }
        }

        // 8. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        if (!"POST".equals(request.getMethod())) {
            return false;
        }

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/logout");
    }
}
