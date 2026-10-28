package com.example.loanflow.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class AdminDashboardTrendResponse {
    private String applicationDate;

    private Long applicationCount;
}
