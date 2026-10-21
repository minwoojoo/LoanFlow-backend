
package com.example.loanflow.loan.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanEmploymentPolicy {

    private Long policyId;

    private Long productId;

    private String employmentType;

    private BigDecimal limitMultiplier;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
