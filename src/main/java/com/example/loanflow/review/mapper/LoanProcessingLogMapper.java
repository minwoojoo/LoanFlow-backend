package com.example.loanflow.review.mapper;

import com.example.loanflow.admin.dto.AdminProcessingLogDetailResponse;
import com.example.loanflow.admin.dto.AdminProcessingLogReadResponse;
import com.example.loanflow.review.entity.LoanProcessingLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanProcessingLogMapper {

    // 심사 처리 로그 저장
    int insertLog(LoanProcessingLog log);


    // 운영자 - 전체 심사 처리 이력 목록 조회
    List<AdminProcessingLogReadResponse> findAllProcessingLogs();

    // 운영자 - 특정 심사 처리 이력 상세 조회
    AdminProcessingLogDetailResponse findProcessingLogDetail(
            @Param("logId") Long logId
    );
}
