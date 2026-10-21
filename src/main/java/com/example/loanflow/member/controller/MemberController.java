package com.example.loanflow.member.controller;

import com.example.loanflow.member.dto.MemberInfoResponse;
import com.example.loanflow.member.dto.MemberUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.loanflow.member.service.MemberService;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name="회원", description = "회원 정보 관리 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Operation(
            summary = "내 정보 조회",
            description = "자신의 프로필 정보를 조회합니다."
    )
    @GetMapping("/me")
    public MemberInfoResponse getMyInfo(@AuthenticationPrincipal Long memberId){
        System.out.println("Controller memberId = " + memberId);

        return memberService.getMyInfo(memberId);
    }


    @Operation(
            summary = "내 정보 수정",
            description = "직업 타입, 연소득, 생년월일 등의 정보를 수정합니다."
    )
    @PatchMapping("/me")
    public void updateMyInfo(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberUpdateRequest request){
        memberService.updateMyInfo(memberId, request);
    }

}
