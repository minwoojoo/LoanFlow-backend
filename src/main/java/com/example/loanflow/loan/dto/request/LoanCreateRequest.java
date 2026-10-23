package com.example.loanflow.loan.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
public class LoanCreateRequest {

    @NotNull(message = "대출 상품을 선택해주세요.")
    private Long productId;

    @NotEmpty(message = "동의한 약관이 필요합니다.")
    private List<@NotNull Long> agreedTermsIds;
}
