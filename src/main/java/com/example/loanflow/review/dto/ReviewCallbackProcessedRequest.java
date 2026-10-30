
package com.example.loanflow.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCallbackProcessedRequest {

    private final Long applicationId;

    private final String requestId;

    private final String processStatus;
}
