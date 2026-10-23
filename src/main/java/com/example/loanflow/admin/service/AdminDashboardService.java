package com.example.loanflow.admin.service;

import com.example.loanflow.admin.dto.AdminDashboardSummaryResponse;
import com.example.loanflow.admin.mapper.AdminDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {
    private final AdminDashboardMapper adminDashboardMapper;

    // 운영자 - 대출 신청 통계 요약 조회
    public AdminDashboardSummaryResponse getDashboardSummary(){
        return adminDashboardMapper.findDashboardSummary();
    }
}
