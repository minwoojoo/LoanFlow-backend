package com.example.loanflow.loan.mapper;

import com.example.loanflow.admin.dto.AdminLoanDetailResponse;
import com.example.loanflow.admin.dto.AdminLoanReadResponse;
import com.example.loanflow.loan.dto.request.LoanUpdateRequest;
import com.example.loanflow.loan.dto.response.LoanDetailResponse;
import com.example.loanflow.loan.dto.response.LoanDraftResponse;
import com.example.loanflow.loan.dto.response.LoanReadResponse;
import com.example.loanflow.loan.dto.response.LoanUpdateResponse;
import com.example.loanflow.loan.entity.LoanApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LoanMapper {

    // 작성 중 대출 신청서 생성
    int createApplication(LoanApplication application);

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
            @Param("request") LoanUpdateRequest request
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
    List<LoanReadResponse> findMyApplications(
            @Param("memberId") Long memberId
    );

    // 대출 신청 상세 조회
    LoanDetailResponse findApplicationDetail(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId
    );

    // 작성 중인 대출 신청서 삭제
    int deleteApplication(
            @Param("memberId") Long memberId,
            @Param("applicationId") Long applicationId
    );


    // 심사 대상 신청서 조회
    LoanApplication findByApplicationAndRequestId(
            @Param("applicationId") Long applicationId,
            @Param("requestId") String requestId
    );

    // 심사 결과 반영
    int updateReviewResult(
            @Param("applicationId") Long applicationId,
            @Param("requestId") String requestId,
            @Param("statusCode") String statusCode,
            @Param("reviewResultId") String reviewResultId

    );


    // 상태 코드에 해당하는 상태명 조회
    String findStatusNameByCode(
            @Param("statusCode") String statusCode
    );


    // 운영자 - 전체 대출 신청 목록 조회
    List<AdminLoanReadResponse> findAllApplications();

    // 운영자 - 대출 신청 상세 조회
    AdminLoanDetailResponse findAdminApplicationDetail(
            @Param("applicationId") Long applicationId
    );
}
