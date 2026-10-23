package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminProcessingLogDetailResponse;
import com.example.loanflow.admin.dto.AdminProcessingLogReadResponse;
import com.example.loanflow.admin.service.AdminProcessingLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "운영자 - 심사 처리 이력 관리",
        description = "운영자용 심사 처리 이력 조회 API"
)
@RestController
@RequestMapping("/api/admin/processing-logs")
@RequiredArgsConstructor
public class AdminProcessingLogController {

    private final AdminProcessingLogService adminProcessingLogService;


    @Operation(summary = "전체 심사 처리 이력 목록 조회")
    @GetMapping
    public List<AdminProcessingLogReadResponse> getAllProcessingLogs(){
        return adminProcessingLogService.getAllProcessingLogs();
    }


    @Operation(summary = "심사 처리 이력 상세 조회")
    @GetMapping("/{id}")
    public AdminProcessingLogDetailResponse getProcessingLogDetail(
        @PathVariable("id") Long logId
    ){
        return adminProcessingLogService.getProcessingLogDetail(logId);
    }

}
