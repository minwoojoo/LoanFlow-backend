package com.example.loanflow.admin.service;


import com.example.loanflow.admin.dto.AdminProcessingLogDetailResponse;
import com.example.loanflow.admin.dto.AdminProcessingLogReadResponse;
import com.example.loanflow.review.mapper.LoanProcessingLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProcessingLogService {

    private final LoanProcessingLogMapper loanProcessingLogMapper;

    // 운영자 - 전체 심사 처리 이력 목록 조회
    public List<AdminProcessingLogReadResponse> getAllProcessingLogs(){
        return loanProcessingLogMapper.findAllProcessingLogs();
    }

    // 운영자 - 심사 처리 이력 상세 조회
    public AdminProcessingLogDetailResponse getProcessingLogDetail(Long logId){
        AdminProcessingLogDetailResponse log =
                loanProcessingLogMapper.findProcessingLogDetail(logId);

        if (log == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "심사 처리 이력을 찾을 수 없습니다."
            );
        }

        return log;
    }
}
