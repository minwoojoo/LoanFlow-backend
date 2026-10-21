package com.example.loanflow.terms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TermsReadResponse {
    private Long termsId;

    private String termsName;

    private String requiredYn;
}
