package com.example.loanflow.terms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TermsCreateRequest {

    private String termsName;

    private String termsCode;

    private String termsVersion;

    private String content;

    private String requiredYn;
}
