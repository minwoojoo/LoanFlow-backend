package com.example.loanflow.loan.controller;


import com.example.loanflow.loan.dto.response.LoanProductReadResponse;
import com.example.loanflow.loan.service.LoanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "대출 상품", description = "대출 상품 관련 API")
@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
public class LoanProductController {
    private final LoanProductService loanProductService;

    @Operation(
            summary = "대출 상품 목록 조회",
            description = "현재 활성화된 대출 상품 목록을 조회합니다."
    )
    @GetMapping
    public List<LoanProductReadResponse> getActiveProducts(){
        return loanProductService.getActiveProducts();
    }
}
