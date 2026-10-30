package com.example.loanflow._core.config;

import com.example.loanflow.auth.jwt.JwtTokenProvider;
import com.example.loanflow.auth.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
            // csrf 보호 비활성화
            http.csrf().disable()

            // 서버에서 로그인 세션을 생성하지 않음
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // api 접근 권한 설정
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout").permitAll()

                .antMatchers(
                        HttpMethod.POST,
                        "/api/callback/review-results"
                ).permitAll()

                .antMatchers("/api/admin/**").hasRole("ADMIN")

                .antMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs",
                        "/v3/api-docs/**"
                ).permitAll()

                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()

            // 인증 실패 시 401 반환
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                })
            .and()


            // 기본 로그인 화면 및 HTTP Basic 인증 비활성화
            .formLogin().disable()
            .httpBasic().disable();

            // JWT 인증 필터 등록
            http.addFilterBefore(
                    new JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter.class
            );

            return http.build();
    }
}

