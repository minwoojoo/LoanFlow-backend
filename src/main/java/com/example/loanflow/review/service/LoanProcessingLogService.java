package com.example.loanflow.review.service;

import com.example.loanflow.review.entity.LoanProcessingLog;
import com.example.loanflow.review.mapper.LoanProcessingLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanProcessingLogService {

    private final LoanProcessingLogMapper loanProcessingLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
            Long applicationId,
            String requestId,
            String processType,
            String processStatus,
            String errorCode,
            String errorMessage
    ){

        LoanProcessingLog log = LoanProcessingLog.builder()
                .applicationId(applicationId)
                .requestId(requestId)
                .processType(processType)
                .processStatus(processStatus)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();

        int insertedRows = loanProcessingLogMapper.insertLog(log);

        if (insertedRows != 1){
            throw new IllegalStateException(
                    "심사 처리 로그 저장에 실패했습니다."
            );
        }

    }
}
