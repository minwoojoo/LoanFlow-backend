
package com.example.loanflow.review.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewCallbackProcessedEvent {

    private final Long applicationId;

    private final String requestId;

    private final String processStatus;
}
