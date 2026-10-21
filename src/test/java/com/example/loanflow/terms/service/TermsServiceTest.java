package com.example.loanflow.terms.service;

import com.example.loanflow.terms.dto.TermsDetailReadResponse;
import com.example.loanflow.terms.dto.TermsReadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TermsServiceTest {

    @Autowired
    private TermsService termsService;

    // 1. 활성 약관 목록 조회 테스트
    @Test
    void getAllActiveTermsTest() {

        List<TermsReadResponse> termsList =
                termsService.getAllActiveTerms();

        assertNotNull(termsList);
        assertFalse(termsList.isEmpty());

        for (TermsReadResponse terms : termsList) {
            assertNotNull(terms.getTermsId());
            assertNotNull(terms.getTermsName());
            assertNotNull(terms.getRequiredYn());
        }
    }

    // 2. 활성 약관 상세 조회 테스트
    @Test
    void getDetailActiveTermsTest() {

        // 먼저 활성 약관 목록에서 ID 하나 가져오기
        List<TermsReadResponse> termsList =
                termsService.getAllActiveTerms();

        assertFalse(termsList.isEmpty());

        Long termsId = termsList.get(0).getTermsId();

        // 상세 조회
        TermsDetailReadResponse response =
                termsService.getDetailActiveTerms(termsId);

        assertNotNull(response);
        assertEquals(termsId, response.getTermsId());
        assertNotNull(response.getTermsName());
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isBlank());
    }

    // 3. 존재하지 않는 약관 조회 테스트
    @Test
    void getDetailActiveTermsNotFoundTest() {

        Long invalidTermsId = -1L;

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> termsService.getDetailActiveTerms(invalidTermsId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
