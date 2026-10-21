package com.example.loanflow.loan.mapper;

import com.example.loanflow.loan.dto.request.LoanApplicationUpdateRequest;
import com.example.loanflow.loan.dto.response.LoanApplicationDetailResponse;
import com.example.loanflow.loan.dto.response.LoanApplicationReadResponse;
import com.example.loanflow.loan.entity.LoanApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LoanApplicationMapper {

    // 작성 중 대출 신청서 생성
    int insertApplication(LoanApplication application);

    // 약관 동의 내역 저장
    int insertConsent(
            @Param("applicationId") Long applicationId,
            @Param("termsId") Long termsId
    );

    // 동일 회원, 동일 상품의 진행 중 신청서 조회
    LoanApplication findWritingApplication(
            @Param("memberId") Long memberId,
            @Param("productId") Long productId
    );

    // 신청서 ID와 고객 ID로 특정 신청서 조회
    LoanApplication findWritingByIdAndMember(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId
    );

    int updateApplicationDraft(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId,
            @Param("request") LoanApplicationUpdateRequest request
    );

    // 최종 제출 처리
    int submitApplication(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId,
            @Param("availableLimit") Long availableLimit,
            @Param("estimatedInterestRate") BigDecimal estimatedInterestRate
    );

    // 제출된 신청서 조회
    LoanApplication findByIdAndMember(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId
    );


    // 내 대출 신청 목록 조회
    List<LoanApplicationReadResponse> findMyApplications(
            @Param("memberId") Long memberId
    );

    // 대출 신청 상세 조회
    LoanApplicationDetailResponse findApplicationDetail(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId
    );
}
