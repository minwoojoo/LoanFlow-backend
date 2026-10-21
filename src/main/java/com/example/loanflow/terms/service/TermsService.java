package com.example.loanflow.terms.service;

import com.example.loanflow.terms.entity.Terms;
import com.example.loanflow.terms.mapper.TermsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.loanflow.terms.dto.TermsReadResponse;
import com.example.loanflow.terms.dto.TermsDetailReadResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TermsService {
    private final TermsMapper termsMapper;

    public List<TermsReadResponse> getAllActiveTerms(){
        List<Terms> termsList = termsMapper.findAllActive();

        return termsList.stream()
                .map(terms -> new TermsReadResponse(
                        terms.getTermsId(),
                        terms.getTermsName(),
                        terms.getRequiredYn()
                ))
                .collect(Collectors.toList());
    }

    public TermsDetailReadResponse getDetailActiveTerms(Long termId){
        Terms terms = termsMapper.findActiveById(termId);

        if(terms == null ){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 약관을 찾을 수 없습니다."
            );
        }

        return new TermsDetailReadResponse(
                terms.getTermsId(),
                terms.getTermsName(),
                terms.getContent()
        );
    }

}
