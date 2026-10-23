package com.example.loanflow.review.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LoanProcessingLog {
    private Long logId;

    private Long applicationId;

    private String requestId;

    private String processType;

    private String processStatus;

    private String errorCode;

    private String errorMessage;

    private LocalDateTime createdAt;
}
