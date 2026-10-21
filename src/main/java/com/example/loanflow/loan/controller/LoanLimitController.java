package com.example.loanflow.loan.controller;

import com.example.loanflow.loan.dto.request.LoanLimitCheckRequest;
import com.example.loanflow.loan.dto.response.LoanLimitCheckResponse;
import com.example.loanflow.loan.service.LoanLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;



@Tag(name = "대출 한도 조회", description = "예상 대출 한도 및 금리 조회 API")
@RestController
@RequestMapping("/api/loan-limits")
@RequiredArgsConstructor
public class LoanLimitController {
    private final LoanLimitService loanLimitService;

    @Operation(
            summary = "예상 한도 및 금리 조회",
            description = "고객 정보와 대출 상품 조건을 기반으로 예상 한도 및 금리를 계산합니다."
    )
    @PostMapping("/check")
    public ResponseEntity<LoanLimitCheckResponse> checkLimit(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody LoanLimitCheckRequest request
    ){
        LoanLimitCheckResponse response = loanLimitService.checkLimit(memberId,request);

        return ResponseEntity.ok(response);
    }


}
