package com.example.loanflow.loan.mapper;


import com.example.loanflow.loan.entity.LoanEmploymentPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoanLimitMapper {
    LoanEmploymentPolicy findByProductAndEmploymentType(
            @Param("productId") Long productId,
            @Param("employmentType") String employmentType
    );
}
