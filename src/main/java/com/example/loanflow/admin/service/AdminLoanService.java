package com.example.loanflow.admin.service;

import com.example.loanflow.admin.dto.AdminLoanDetailResponse;
import com.example.loanflow.admin.dto.AdminLoanReadResponse;
import com.example.loanflow.loan.mapper.LoanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLoanService {

    private final LoanMapper loanMapper;

    // 운영자 - 전체 대출 신청 목록 조회
    public List<AdminLoanReadResponse> getAllApplications(){

        return loanMapper.findAllApplications();
    }


    // 운영자 - 대출 신청 상세 조회
    public AdminLoanDetailResponse getApplicationDetail(
            Long applicationId
    ){
        AdminLoanDetailResponse application =
                loanMapper.findAdminApplicationDetail(applicationId);

        if (application == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "대출 신청서를 찾을 수 없습니다."
            );
        }

        return application;
    }

}
