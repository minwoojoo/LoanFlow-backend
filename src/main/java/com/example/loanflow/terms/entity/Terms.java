package com.example.loanflow.terms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Terms {
    private Long termsId;

    private String termsName;

    private String content;

    private String termsCode;

    private String termsVersion;

    private String requiredYn;

    private String isActive;
}
