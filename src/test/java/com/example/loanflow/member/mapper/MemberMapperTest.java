package com.example.loanflow.member.mapper;

import com.example.loanflow.member.dto.MemberUpdateRequest;
import com.example.loanflow.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberMapperTest {

    @Autowired
    private MemberMapper memberMapper;

    @Test
    void findAccountIdTest(){
        // DB에 존재하는 testuser 조회
        Member testMember = memberMapper.findAccountId("testuser");
        assertNotNull(testMember);

        // memberId를 사용해서 회원 조회
        Member member = memberMapper.findById(testMember.getMemberId());
        assertNotNull(member);


        // 조회 결과 검증
        assertEquals("testuser", testMember.getAccountId());
        assertEquals(testMember.getMemberId(), member.getMemberId());
        assertEquals(testMember.getName(), member.getName());

        System.out.println("조회된 회원 이름: " + testMember.getName());
    }

    @Test
    void updateMyInfoTest(){
        Member member = memberMapper.findAccountId("testuser");
        assertNotNull(member);

        Long memberId = member.getMemberId();

        MemberUpdateRequest request = new MemberUpdateRequest(
                "수정된이름",
                "EMPLOYED",
                50000000L,
                LocalDate.of(2000, 5, 15)
        );

        int updatedRows = memberMapper.updateMyInfo(memberId, request);

        assertEquals(1, updatedRows);

        Member updatedMember = memberMapper.findById(memberId);

        assertEquals("수정된이름", updatedMember.getName());
        assertEquals("EMPLOYED", updatedMember.getEmploymentType());
        assertEquals(50000000L, updatedMember.getAnnualIncome());
        assertEquals(LocalDate.of(2000, 5, 15), updatedMember.getBirthDate());
    }

}
