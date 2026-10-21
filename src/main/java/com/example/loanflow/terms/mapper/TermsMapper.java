package com.example.loanflow.terms.mapper;

import com.example.loanflow.terms.entity.Terms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TermsMapper {

    List<Terms> findAllActive();

    Terms findActiveById(@Param("termsId") Long termsId);

}
