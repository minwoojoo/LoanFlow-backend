package com.example.loanflow.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminProcessingLogDetailResponse {

    private Long logId;

    private Long applicationId;

    private String requestId;

    private Long memberId;

    private String memberName;

    private Long productId;

    private String productName;

    private String processType;

    private String processStatus;

    private String errorCode;

    private String errorMessage;

    private LocalDateTime createdAt;

}
