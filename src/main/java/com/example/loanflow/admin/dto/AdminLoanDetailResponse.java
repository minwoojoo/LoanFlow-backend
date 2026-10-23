package com.example.loanflow.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoanDetailResponse {

    // 신청서 및 회원 정보
    private Long applicationId;
    private Long memberId;
    private String memberName;

    // 대출 상품 정보
    private Long productId;
    private String productName;
    private Long maxLoanTerm;

    // 대출 신청 상세 정보
    private Long applicationAmount;
    private Long loanTerm;
    private Long availableLimit;
    private BigDecimal estimatedInterestRate;

    // 신청 당시 회원 정보
    private String employmentType;
    private Long annualIncome;
    private LocalDate birthDate;

    // 신청 및 심사 상태
    private String applicationStatus;
    private String requestId;
    private String reviewResultId;

    // 삭제 정보
    private String isDeleted;
    private LocalDateTime deletedAt;

    // 처리 일시
    private LocalDateTime createdAt;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
}
