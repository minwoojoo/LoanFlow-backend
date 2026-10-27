package com.example.loanflow.terms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminTermsDetailResponse {

    private Long termsId;

    private String termsName;

    private String termsCode;

    private String termsVersion;

    private String content;

    private String requiredYn;

    private String isActive;
}
