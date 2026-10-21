package com.example.loanflow.review.client;

import com.example.loanflow.review.dto.MockReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MockReviewClient {
    private final RestTemplate restTemplate;

    @Value("${mock.review.base-url}")
    private String mockReviewBaseUrl;

    public void requestReview(
            Long applicationId,
            String requestId
    ){
        // 1. Mock 서버에 전달할 요청 DTO 생성
        MockReviewRequest request =
                new MockReviewRequest(applicationId, requestId);

        // 2. Mock 서버 URL
        String url = mockReviewBaseUrl + "/mock/review-requests";

        // 3. HTTP POST 요청 전송
        ResponseEntity<Void> response =
                restTemplate.postForEntity(
                        url,
                        request,
                        Void.class
                );


        if (response.getStatusCode() != HttpStatus.ACCEPTED){
            throw new IllegalArgumentException(
                    "Mock 심사 요청 접수 실패: " + response.getStatusCode()
            );
        }
    }
}
