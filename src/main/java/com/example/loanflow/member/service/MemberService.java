package com.example.loanflow.member.service;

import com.example.loanflow.member.dto.MemberInfoResponse;
import com.example.loanflow.member.dto.MemberUpdateRequest;
import com.example.loanflow.member.entity.Member;
import com.example.loanflow.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;

    // 고객 정보 조회 로직
    public MemberInfoResponse getMyInfo(Long memberId){
        Member member = memberMapper.findById(memberId);

        if (member == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "회원을 찾을 수 없습니다."
            );
        }

        return new MemberInfoResponse(
                member.getMemberId(),
                member.getName(),
                member.getEmploymentType(),
                member.getAnnualIncome(),
                member.getBirthDate()
        );
    }


    // 고객 정보 수정 로직
    @Transactional
    public void updateMyInfo(Long memberId, MemberUpdateRequest request){
        // 1. 회원 정보 수정
        int updatedRows = memberMapper.updateMyInfo(memberId, request);

        // 2. 수정된 회원이 없다면 예외 처리
        if (updatedRows == 0){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "회원을 찾을 수 없습니다."
            );
        }

    }
}
