package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminDashboardFunnelResponse;
import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;
import com.example.loanflow.admin.dto.AdminDashboardTrendResponse;
import com.example.loanflow.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "운영자 - 대시보드",
        description = "운영자용 대출 신청 통계 API"
)
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(
            summary = "대출 신청 통계 요약 조회",
            description = "전체 대출 신청 건수와 작성중, 심사중, 승인, 거절 상태별 건수를 조회합니다."
    )
    @GetMapping("summary")
    public AdminDashboardSummaryResponse getDashboardSummary(){

        return adminDashboardService.getDashboardSummary();
    }




    @Operation(
            summary = "일별 대출 신청 추이 조회",
            description = "오늘을 포함한 최근 7일간 최종 제출된 대출 신청 건수를 날짜별로 조회합니다. 신청 건수가 없는 날짜도 0건으로 반환합니다."
    )
    @GetMapping("/trends")
    public List<AdminDashboardTrendResponse> getDailyApplicationTrends() {
        return adminDashboardService.getDailyApplicationTrends();
    }

    @Operation(
            summary = "대출 신청 퍼널 분석 조회",
            description = "최근 7일간 신청서 생성, 한도 조회, 최종 제출 단계별 도달 신청서 건수를 조회합니다."
    )
    @GetMapping("/funnel")
    public AdminDashboardFunnelResponse analyzeFunnel(){
        return adminDashboardService.analyzeFunnel();
    }
}
