
package com.example.loanflow.review.mapper;

import com.example.loanflow.review.entity.LoanProcessingLog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class LoanProcessingLogMapperTest {

    @Autowired
    private LoanProcessingLogMapper loanProcessingLogMapper;

    @Test
    @DisplayName("심사 요청 성공 로그를 저장한다")
    void insertLog_success() {

        // given
        LoanProcessingLog log = LoanProcessingLog.builder()
                .applicationId(88L)
                .requestId(UUID.randomUUID().toString())
                .processType("REVIEW_REQUEST")
                .processStatus("SUCCESS")
                .build();

        // when
        int insertedRows = loanProcessingLogMapper.insertLog(log);

        // then
        assertEquals(1, insertedRows);
    }

    @Test
    @DisplayName("심사 요청 실패 로그를 저장한다")
    void insertLog_failed() {

        // given
        LoanProcessingLog log = LoanProcessingLog.builder()
                .applicationId(88L)
                .requestId(UUID.randomUUID().toString())
                .processType("REVIEW_REQUEST")
                .processStatus("FAILED")
                .errorCode("CONNECTION_ERROR")
                .errorMessage("Mock 서버 연결 실패")
                .build();

        // when
        int insertedRows = loanProcessingLogMapper.insertLog(log);

        // then
        assertEquals(1, insertedRows);
    }
}
