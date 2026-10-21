
package com.example.loanflow.loan.mapper;

import com.example.loanflow.loan.entity.LoanProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanProductMapper {

    // 활성화된 대출 상품 전체 목록 조회
    List<LoanProduct> findAllActive();

    // 특정 활성 대출 상품 조회
    LoanProduct findActiveById(
            @Param("productId") Long productId
    );
}
