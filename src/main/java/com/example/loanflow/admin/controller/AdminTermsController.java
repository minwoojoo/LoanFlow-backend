package com.example.loanflow.admin.controller;

import com.example.loanflow.admin.dto.AdminTermsDetailResponse;
import com.example.loanflow.admin.dto.AdminTermsListResponse;
import com.example.loanflow.terms.dto.TermsCreateRequest;
import com.example.loanflow.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "운영자 - 약관 관리",
        description = "운영자용 약관 조회, 등록 및 비활성화 API"
)
@RestController
@RequestMapping("/api/admin/terms")
@RequiredArgsConstructor
public class AdminTermsController {
    private final TermsService termsService;

    @Operation(
            summary = "전체 약관 목록 조회",
            description = "활성 및 비활성 약관을 포함한 전체 약관 목록을 조회합니다."
    )
    @GetMapping
    public List<AdminTermsListResponse> getAllTerms(){
        return termsService.getAllTerms();
    }



    @Operation(
            summary = "약관 상세 조회",
            description = "약관 ID를 기준으로 약관의 상세 정보와 내용을 조회합니다."
    )
    @GetMapping("/{id}")
    public AdminTermsDetailResponse getTermsById(
            @PathVariable("id") Long termsId
    ){
        return termsService.getTermsById(termsId);
    }



    @Operation(
            summary = "신규 약관 등록",
            description = "신규 약관을 등록합니다. 동일한 약관 코드와 버전은 중복 등록할 수 없습니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTerms(
            @RequestBody TermsCreateRequest request
    ){
        termsService.createTerms(request);
    }



    @Operation(
            summary = "약관 비활성화",
            description = "약관을 비활성화합니다. 기존 약관 동의 이력은 유지됩니다."
    )
    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateTerms(
            @PathVariable("id") Long termsId
    ){
        termsService.deactivateTerms(termsId);
    }
}
