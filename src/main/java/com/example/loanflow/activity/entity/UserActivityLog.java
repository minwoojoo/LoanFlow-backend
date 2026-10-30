package com.example.loanflow.activity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog {

    private Long logId;

    private Long memberId;

    private Long applicationId;

    private String sessionId;

    private String eventType;

    private LocalDateTime occurredAt;
}
