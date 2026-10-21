
package com.example.loanflow.review.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Test
    @DisplayName("Mock 서버에 심사 요청을 보내고 202 Accepted 응답을 받는다")
    void requestReview_success() {

        // given
        Long applicationId = 15L;
        String requestId = "REQ-TEST-001";

        // when & then
        assertDoesNotThrow(() ->
                reviewService.requestReview(
                        applicationId,
                        requestId
                )
        );
    }
}
