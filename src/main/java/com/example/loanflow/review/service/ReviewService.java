package com.example.loanflow.review.service;

import com.example.loanflow.integration.MockReviewClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final MockReviewClient mockReviewClient;

    public void requestReview(
            Long applicationId,
            String requestId
    ) {
        // 1. 심사 요청 시작 로그
        log.info(
                "심사 요청 시작 - applicationId: {}, requestId: {}",
                applicationId,
                requestId
        );

        // 2. Mock 서버에 심사 요청 전송
        mockReviewClient.requestReview(
                applicationId,
                requestId
        );

        // 3. 심사 요청 접수 성공 로그
        log.info(
                "심사 요청 접수 완료 - applicationId: {}, requestId: {}",
                applicationId,
                requestId
        );
    }
}
