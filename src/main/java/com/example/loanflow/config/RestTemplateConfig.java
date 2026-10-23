package com.example.loanflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(){
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // 서버 연결 최대 대기 시간: 5초
        factory.setConnectTimeout(5000);

        // 서버 응답 최대 대기 시간: 7초
        factory.setReadTimeout(70000);

        return new RestTemplate(factory);
    }
}
