package com.example.loanflow.member.mapper;

import com.example.loanflow.member.dto.MemberUpdateRequest;
import com.example.loanflow.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface MemberMapper {

    // 로그인 시 회원 조회
    Member findAccountId(@Param("accountId") String accountId);

    // 내 정보 조회
    Member findById(@Param("memberId") Long memberId);

    // 내 정보 수정
    int updateMyInfo(@Param("memberId") Long memberId, @Param("request") MemberUpdateRequest request);

}
