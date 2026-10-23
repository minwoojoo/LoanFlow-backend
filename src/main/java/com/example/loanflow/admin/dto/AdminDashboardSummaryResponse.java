package com.example.loanflow.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponse {
    private Long totalCount;

    private Long draftCount;

    private Long reviewingCount;

    private Long approvedCount;

    private Long rejectedCount;

}
