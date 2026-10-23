package com.example.loanflow.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminProcessingLogReadResponse {
    private Long logId;

    private Long applicationId;

    private Long memberId;

    private String memberName;

    private String productName;

    private String processType;

    private String processStatus;

    private LocalDateTime createdAt;
}
