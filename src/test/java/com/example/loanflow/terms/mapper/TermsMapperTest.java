package com.example.loanflow.terms.mapper;


import com.example.loanflow.terms.entity.Terms;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
public class TermsMapperTest {

    @Autowired
    private TermsMapper termsMapper;

    @Test
    void findAllTest(){
        List<Terms> termsList = termsMapper.findAllActive();

        assertEquals(4, termsList.size());
        Terms terms = termsList.stream()
                        .filter(t -> "PRIVACY_COLLECTION".equals(t.getTermsCode()))
                        .findFirst().orElseThrow();

        assertEquals("Y", terms.getRequiredYn());
        assertEquals("1.0", terms.getTermsVersion());
        assertNotNull(terms.getContent());
    }

    @Test
    void findByIdTest(){
        Terms terms = termsMapper.findActiveById(1L);

        assertNotNull(terms);
        assertEquals(1L, terms.getTermsId());
        assertNotNull(terms.getContent());
        assertFalse(terms.getContent().isBlank());
    }
}
