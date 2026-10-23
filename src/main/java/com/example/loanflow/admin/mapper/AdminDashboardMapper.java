package com.example.loanflow.admin.mapper;

import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDashboardMapper {

    // 운영자 - 대출 신청 통계 요약 조회
    AdminDashboardSummaryResponse findDashboardSummary();
}
