package com.example.loanflow.terms.controller;

import com.example.loanflow.terms.dto.TermsDetailReadResponse;
import com.example.loanflow.terms.dto.TermsReadResponse;
import com.example.loanflow.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "약관", description = "약관 조회 API")
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {
    private final TermsService termsService;

    @Operation(
            summary = "활성 약관 목록 조회",
            description = "현재 활성화된 약관 목록을 조회합니다."
    )
    @GetMapping
    public List<TermsReadResponse> getAllActiveTerms(){
        return termsService.getAllActiveTerms();
    }


    @Operation(
            summary = "활성 약관 상세 조회",
            description = "약관 ID를 이용하여 활성 약관의 상세 내용을 조회합니다."
    )
    @GetMapping("/{id}")
    public TermsDetailReadResponse getDetailActiveTerms(@PathVariable("id") Long termsId){
        return termsService.getDetailActiveTerms(termsId);
    }

}
