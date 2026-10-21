package com.example.loanflow.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewCallbackRequest {

    private Long applicationId;

    private String requestId;

    private String reviewResultId;

    private String reviewResult;
}
