package com.example.loanflow.loan.controller;


import com.example.loanflow.loan.dto.request.LoanApplicationCreateRequest;
import com.example.loanflow.loan.dto.request.LoanApplicationUpdateRequest;
import com.example.loanflow.loan.dto.response.*;
import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Tag(name = "대출 신청", description = "대출 신청서 관리 API")
@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {
    private final LoanApplicationService loanApplicationService;

    @Operation(
            summary = "대출 신청서 신규 생성",
            description = "약관 동의 후 작성중 상태의 대출 신청서를 생성합니다."
    )
    @PostMapping
    public ResponseEntity<LoanApplicationCreateResponse> createApplication(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody LoanApplicationCreateRequest request
            ){
        LoanApplicationCreateResponse response = loanApplicationService.createApplication(
                memberId, request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "작성 중인 대출 신청서 조회",
            description = "작성 중인 선칭서를 조횝합니다."
    )
    @GetMapping("/draft")
    public ResponseEntity<LoanApplicationDraftResponse> findDraft(
            @AuthenticationPrincipal Long memberId,
            @RequestParam("productId") Long productId
    ){
        LoanApplicationDraftResponse response = loanApplicationService.findDraft(
                memberId,
                productId
        );

        return ResponseEntity.ok(response);
    }



    @Operation(
            summary = "대출 신청서 임시저장",
            description = "작성 중인 대출 신청서의 입력 정보를 부분 수정합니다."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<LoanApplicationUpdateResponse> updateApplicationDraft(
            @AuthenticationPrincipal Long memberId,
            @PathVariable("id") Long applicationId,
            @Valid @RequestBody LoanApplicationUpdateRequest request
    ) {

        LoanApplicationUpdateResponse response =
                loanApplicationService.updateApplicationDraft(
                        memberId,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "신청서 최종 제출",
            description = "작성 중인 대출 신청서의 필수 정보를 검증하고 최종 제출합니다."
    )
    @PostMapping("{id}/submit")
    public ResponseEntity<LoanApplicationSubmitResponse> submitApplication(
            @AuthenticationPrincipal Long memberId,
            @PathVariable("id") Long applicationId
    ){
        LoanApplicationSubmitResponse response =
                loanApplicationService.submitApplication(
                        memberId,
                        applicationId
                );
        return ResponseEntity.ok(response);
    }



    @Operation(
            summary = "내 대출 신청 목록 조회",
            description = "로그인한 회원의 대출 신청 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<List<LoanApplicationReadResponse>> getMyApplications(
            @AuthenticationPrincipal Long memberId
    ){
        // 1. 로그인한 회원의 신청 목록 조회
        List<LoanApplicationReadResponse> response =
                loanApplicationService.getMyApplications(memberId);

        // 2. 조회 결과 반환
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "대출 신청 상세 조회",
            description = "로그인한 회원의 대출 신청 상세 정보와 진행 일시를 조회합니다."
    )
    @GetMapping("{id}")
    public ResponseEntity<LoanApplicationDetailResponse> getApplicationDetail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable("id") Long applicationId
    ){
        LoanApplicationDetailResponse response =
                loanApplicationService.getApplicationDetail(
                        memberId,
                        applicationId
                );

        return ResponseEntity.ok(response);
    }

}
