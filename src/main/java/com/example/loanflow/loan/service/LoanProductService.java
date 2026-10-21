package com.example.loanflow.loan.service;


import com.example.loanflow.loan.dto.response.LoanProductReadResponse;
import com.example.loanflow.loan.entity.LoanProduct;
import com.example.loanflow.loan.mapper.LoanProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanProductService {
    private final LoanProductMapper loanProductMapper;

    // 활성화된 대출 상품 목록 조회
    public List<LoanProductReadResponse> getActiveProducts(){

        // 1. Entity 목록 조회
        List<LoanProduct> products = loanProductMapper.findAllActive();

        // 2. Response DTO 리스트 생성
        List<LoanProductReadResponse> responses = new ArrayList<>();

        // 3. Entity를 Response DTO로 변환
        for (LoanProduct product : products){
            LoanProductReadResponse response = new LoanProductReadResponse(
                    product.getProductId(),
                    product.getProductName(),
                    product.getMaxLimit(),
                    product.getBaseInterestRate(),
                    product.getMaxLoanTerm()
            );
            responses.add(response);
        }

        return responses;
    }
}
