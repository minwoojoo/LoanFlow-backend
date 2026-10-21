package com.example.loanflow.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MockReviewRequest {
    private Long applicationId;

    private String requestId;
}
