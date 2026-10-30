package com.example.loanflow.terms.service;

import com.example.loanflow.admin.dto.AdminTermsDetailResponse;
import com.example.loanflow.admin.dto.AdminTermsListResponse;
import com.example.loanflow.terms.dto.*;
import com.example.loanflow.terms.entity.Terms;
import com.example.loanflow.terms.mapper.TermsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;



@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsMapper termsMapper;

    public List<TermsReadResponse> getAllActiveTerms(){

        List<Terms> termsList = termsMapper.findAllActive();

        List<TermsReadResponse> responseList = new ArrayList<>();

        for (Terms terms : termsList) {

            TermsReadResponse response = new TermsReadResponse(
                    terms.getTermsId(),
                    terms.getTermsName(),
                    terms.getRequiredYn()
            );

            responseList.add(response);
        }

        return responseList;
    }

    public TermsDetailReadResponse getDetailActiveTerms(Long termId){
        Terms terms = termsMapper.findActiveById(termId);

        if(terms == null ){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 약관을 찾을 수 없습니다."
            );
        }

        return new TermsDetailReadResponse(
                terms.getTermsId(),
                terms.getTermsName(),
                terms.getContent()
        );
    }

    // 운영자 - 전체 약관 목록 조회
    @Transactional(readOnly = true)
    public List<AdminTermsListResponse> getAllTerms(){
        List<Terms> termsList = termsMapper.findAll();

        List<AdminTermsListResponse> responseList = new ArrayList<>();

        for (Terms terms : termsList) {

            AdminTermsListResponse response = new AdminTermsListResponse(
                    terms.getTermsId(),
                    terms.getTermsName(),
                    terms.getTermsCode(),
                    terms.getTermsVersion(),
                    terms.getRequiredYn(),
                    terms.getIsActive()
            );

            responseList.add(response);
        }

        return responseList;
    }


    // 운영자 - 약관 상세 조회
    @Transactional(readOnly = true)
    public AdminTermsDetailResponse getTermsById(Long termsId){
        Terms terms = termsMapper.findById(termsId);

        if (terms == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 약관입니다."
            );
        }

        return new AdminTermsDetailResponse(
                terms.getTermsId(),
                terms.getTermsName(),
                terms.getTermsCode(),
                terms.getTermsVersion(),
                terms.getContent(),
                terms.getRequiredYn(),
                terms.getIsActive()
        );
    }

    // 운영자 - 신규 약관 등록
    @Transactional
    public void createTerms(TermsCreateRequest request){
        // 요청 객체 검증
        if(request == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "약관 등록 정보가 없습니다."
            );
        }

        // 필수 입력값 검증
        if (isBlank(request.getTermsName())
            || isBlank(request.getTermsCode())
            || isBlank(request.getTermsVersion())
            || isBlank(request.getContent())){

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "약관명, 코드, 버전, 내용은 필수 입력값입니다."
            );
        }

        // 필수 약관 여부 검증
        if (!"Y".equals(request.getRequiredYn())
                && !"N".equals(request.getRequiredYn())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "필수 약관 여부는 Y 또는 N이어야 합니다."
            );
        }

        // 동일 코드 + 버전 중복 검사
        int duplicateCount = termsMapper.countByCodeAndVersion(
                request.getTermsCode(),
                request.getTermsVersion()
        );

        if (duplicateCount > 0){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "동일한 코드와 버전의 약관이 이미 존재합니다."
            );
        }

        // 신규 약관 등록
        // is_active는 Mapper XML에서 'Y'로 설정
        int insertedRows = termsMapper.createTerms(request);

        if (insertedRows != 1){
            throw new IllegalStateException(
                    "약관 등록에 실패했습니다."
            );
        }
    }

    // 운영자 - 약관 비활성화
    @Transactional
    public void deactivateTerms(Long termsId){
        // 약관 존재 여부 확인
        Terms terms = termsMapper.findById(termsId);

        if(terms == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 약관입니다."
            );
        }

        // 이미 비활성화된 약관인지 확인
        if ("N".equals(terms.getIsActive())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 비활성화된 약관입니다."
            );
        }

        // 비활성화 처리
        int updatedRows = termsMapper.deactivateTerms(termsId);

        if (updatedRows != 1){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "약관 비활성화에 실패했습니다."
            );
        }
    }


    // 공통 문자열 검증
    private boolean isBlank(String value){
        return value == null || value.trim().isEmpty();
    }

}
