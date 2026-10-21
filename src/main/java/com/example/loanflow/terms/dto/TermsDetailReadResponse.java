package com.example.loanflow.terms.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TermsDetailReadResponse {
    private Long termsId;

    private String termsName;

    private String content;

}
