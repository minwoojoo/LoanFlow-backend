package com.example.loanflow.review.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewRequestedEvent {

    private Long applicationId;

    private String requestId;
}
