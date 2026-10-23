package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;
import com.example.loanflow.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "운영자 - 대시보드",
        description = "운영자용 대출 신청 통계 API"
)
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "대출 신청 통계 요약 조회")
    @GetMapping("summary")
    public AdminDashboardSummaryResponse getDashboardSummary(){

        return adminDashboardService.getDashboardSummary();
    }
}
