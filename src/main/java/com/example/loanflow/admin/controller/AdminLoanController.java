package com.example.loanflow.admin.controller;


import com.example.loanflow.admin.dto.AdminLoanDetailResponse;
import com.example.loanflow.admin.dto.AdminLoanReadResponse;
import com.example.loanflow.admin.service.AdminLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(
        name = "운영자 - 대출 신청 관리",
        description = "운영자용 대출 신청 조회 API"
)
@RestController
@RequestMapping("/api/admin/loan-applications")
@RequiredArgsConstructor
public class AdminLoanController {

    private final AdminLoanService adminLoanService;

    @Operation(summary = "전체 대출 신청 목록 조회")
    @GetMapping
    public List<AdminLoanReadResponse> getAllApplications(){
        return adminLoanService.getAllApplications();
    }


    @Operation(summary = "대출 신청 상세 조회")
    @GetMapping("/{id}")
    public AdminLoanDetailResponse getApplicationDetail(
            @PathVariable("id") Long applicationId
    ){
        return adminLoanService.getApplicationDetail(applicationId);
    }
}
