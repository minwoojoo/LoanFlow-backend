package com.example.loanflow.terms.mapper;

import com.example.loanflow.terms.dto.TermsCreateRequest;
import com.example.loanflow.terms.entity.Terms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TermsMapper {

    List<Terms> findAllActive();

    Terms findActiveById(@Param("termsId") Long termsId);

    // 운영자용 약관 조회
    Terms findById(@Param("termsId") Long termsId);

    List<Terms> findAll();

    // 동일 코드 + 버전 중복 검사
    int countByCodeAndVersion(
            @Param("termsCode") String termsCode,
            @Param("termsVersion") String termsVersion
    );

    // 약관 등록
    int createTerms(TermsCreateRequest request);

    // 약관 비활성화
    int deactivateTerms(@Param("termsId") Long termsId);

}
