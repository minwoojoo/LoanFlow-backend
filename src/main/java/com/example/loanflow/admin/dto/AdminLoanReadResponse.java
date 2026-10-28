package com.example.loanflow.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoanReadResponse {

    private Long applicationId;

    // 신청 회원 정보
    private Long memberId;
    private String memberName;

    // 대출 상품 정보
    private Long productId;
    private String productName;

    // 대출 신청 정보
    private Long applicationAmount;
    private String statusName;

    // 신청 및 심사 일시
    private LocalDateTime createdAt;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;

    // 삭제 여부
    private String isDeleted;
}
