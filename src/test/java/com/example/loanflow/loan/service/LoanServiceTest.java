package com.example.loanflow.loan.service;


import com.example.loanflow.loan.dto.request.LoanCreateRequest;
import com.example.loanflow.loan.dto.response.*;
import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.entity.LoanProduct;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.loan.mapper.LoanProductMapper;
import com.example.loanflow.terms.entity.Terms;
import com.example.loanflow.terms.mapper.TermsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoanProductMapper loanProductMapper;

    @Mock
    private TermsMapper termsMapper;

    @Mock
    private LoanLimitService loanLimitService;

    @InjectMocks
    private LoanService loanService;

    private Long memberId;
    private Long productId;

    @BeforeEach
    void setUp() {
        memberId = 41L;
        productId = 1L;
    }

    // 테스트용 요청 객체
    private LoanCreateRequest createRequest(List<Long> agreedTermsIds){
        LoanCreateRequest request = mock(LoanCreateRequest.class);

        when(request.getProductId()).thenReturn(productId);
        lenient().when(request.getAgreedTermsIds())
                .thenReturn(agreedTermsIds);

        return request;
    }

    // 테스트용 약관 객체
    private Terms createTerm(Long termsId, String requiredYn){
        Terms terms = mock(Terms.class);

        when(terms.getTermsId()).thenReturn(termsId);
        when(terms.getRequiredYn()).thenReturn(requiredYn);

        return terms;
    }

    @Test
    @DisplayName("정상적으로 대출 신청서와 약관 동의 내역을 생성한다.")
    void createApplication_success(){

        // given
        LoanCreateRequest request = createRequest(Arrays.asList(1L, 2L));

        LoanProduct product = mock(LoanProduct.class);
        when(product.getProductId()).thenReturn(productId);

        when(loanProductMapper.findActiveById(productId))
                .thenReturn(product);


        Terms requiredTerm = createTerm(1L, "Y");
        Terms optionalTerm = createTerm(2L, "N");

        when(termsMapper.findAllActive())
                .thenReturn(Arrays.asList(requiredTerm, optionalTerm));

        // INSERT 시 자동 생성된 PK를 흉내낸다.
        when(loanMapper.insertApplication(any(LoanApplication.class)))
                .thenAnswer(invocation -> {
                    LoanApplication application = invocation.getArgument(0);
                    application.setApplicationId(100L);
                    return 1;
                });

        when(loanMapper.insertConsent(anyLong(), anyLong()))
                .thenReturn(1);

        // when
        LoanCreateResponse response =
                loanService.createApplication(memberId, request);

        // then
        assertEquals(100L, response.getApplicationId());
        assertEquals("작성중", response.getApplicationStatus());

        verify(loanMapper, times(1))
                .insertApplication(any(LoanApplication.class));

        verify(loanMapper).insertConsent(100L, 1L);
        verify(loanMapper).insertConsent(100L, 2L);

    }


    @Test
    @DisplayName("이미 작성 중인 신청서가 있으면 중복 생성을 거부한다")
    void createApplication_duplicate() {

        // given
        LoanCreateRequest request =
                createRequest(Arrays.asList(1L));

        LoanProduct product = mock(LoanProduct.class);

        when(loanProductMapper.findActiveById(productId))
                .thenReturn(product);

        LoanApplication existingApplication =
                LoanApplication.builder()
                        .applicationId(22L)
                        .memberId(memberId)
                        .productId(productId)
                        .applicationStatus("작성중")
                        .build();

        when(loanMapper.findWritingApplication(
                memberId, productId
        )).thenReturn(existingApplication);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanService.createApplication(
                        memberId, request
                )
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(loanMapper, never())
                .insertApplication(any(LoanApplication.class));

        verify(loanMapper, never())
                .insertConsent(anyLong(), anyLong());
    }


    @Test
    @DisplayName("필수 약관에 동의하지 않으면 신청서를 생성하지 않는다")
    void createApplication_requiredTermsMissing() {

        // given
        LoanCreateRequest request =
                createRequest(Arrays.asList(2L));

        LoanProduct product = mock(LoanProduct.class);

        when(loanProductMapper.findActiveById(productId))
                .thenReturn(product);

        Terms requiredTerm = createTerm(1L, "Y");
        Terms optionalTerm = createTerm(2L, "N");

        when(termsMapper.findAllActive())
                .thenReturn(Arrays.asList(requiredTerm, optionalTerm));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanService.createApplication(
                        memberId, request
                )
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(loanMapper, never())
                .insertApplication(any(LoanApplication.class));
    }



    @Test
    @DisplayName("중복된 약관 ID가 있으면 신청서를 생성하지 않는다")
    void createApplication_duplicateTerms() {

        // given
        LoanCreateRequest request =
                createRequest(Arrays.asList(1L, 1L));

        LoanProduct product = mock(LoanProduct.class);

        when(loanProductMapper.findActiveById(productId))
                .thenReturn(product);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanService.createApplication(
                        memberId, request
                )
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(loanMapper, never())
                .insertApplication(any(LoanApplication.class));
    }


    @Test
    @DisplayName("작성 중인 대출 신청서가 존재하면 DTO를 반환한다")
    void findDraft_success() {

        // given
        LoanApplication application = LoanApplication.builder()
                .applicationId(22L)
                .memberId(memberId)
                .productId(productId)
                .applicationStatus("작성중")
                .build();

        when(loanMapper.findWritingApplication(
                memberId, productId
        )).thenReturn(application);

        // when
        LoanDraftResponse response =
                loanService.findDraft(memberId, productId);

        // then
        assertEquals(22L, response.getApplicationId());
        assertEquals(productId, response.getProductId());
        assertEquals("작성중", response.getApplicationStatus());
    }


    @Test
    @DisplayName("작성 중인 신청서가 없으면 404 예외가 발생한다")
    void findDraft_notFound() {

        // given
        when(loanMapper.findWritingApplication(
                memberId, productId
        )).thenReturn(null);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanService.findDraft(
                        memberId, productId
                )
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }


    @Test
    @DisplayName("작성 중인 신청서가 존재하지 않으면 404 오류가 발생한다")
    void submitApplication_notFound() {

        // given: 조회할 신청서가 존재하지 않도록 설정
        Long memberId = 1L;
        Long applicationId = 10L;

        when(loanMapper.findWritingByIdAndMember(
                applicationId,
                memberId
        )).thenReturn(null);

        // when: 최종 제출 메서드 실행
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loanService.submitApplication(
                        memberId,
                        applicationId
                )
        );

        // then: 404 오류인지 확인
        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatus()
        );

        // UPDATE가 실행되지 않았는지 확인
        verify(
                loanMapper,
                never()
        ).submitApplication(
                anyLong(),
                anyLong(),
                anyLong(),
                any(BigDecimal.class)
        );
    }



    @Test
    @DisplayName("정상적인 대출 신청서는 최종 제출에 성공한다")
    void submitApplication_success() {

        // given: 테스트 데이터 준비
        Long memberId = 1L;
        Long applicationId = 10L;
        Long availableLimit = 40000000L;
        BigDecimal interestRate = new BigDecimal("5.5");
        LocalDateTime appliedAt =
                LocalDateTime.of(2026, 10, 20, 13, 0);

        // 1. 작성 중인 신청서 생성
        LoanApplication application = LoanApplication.builder()
                .applicationId(applicationId)
                .memberId(memberId)
                .productId(1L)
                .applicationAmount(30000000L)
                .loanTerm(36L)
                .employmentType("EMPLOYED")
                .annualIncome(50000000L)
                .birthDate(LocalDate.of(1998, 5, 10))
                .applicationStatus("작성중")
                .build();

        // 2. 대출 상품 생성
        LoanProduct product = LoanProduct.builder()
                .productId(1L)
                .maxLoanTerm(60L)
                .build();

        // 3. 예상 한도 조회 결과 생성
        LoanLimitCheckResponse limitResult =
                new LoanLimitCheckResponse(
                        applicationId,
                        availableLimit,
                        interestRate,
                        true,
                        null
                );

        // 4. 최종 제출된 신청서 생성
        LoanApplication submittedApplication = LoanApplication.builder()
                .applicationId(applicationId)
                .memberId(memberId)
                .applicationStatus("심사중")
                .availableLimit(availableLimit)
                .estimatedInterestRate(interestRate)
                .appliedAt(appliedAt)
                .build();

        // 5. Mock 동작 설정
        when(loanMapper.findWritingByIdAndMember(
                applicationId, memberId
        )).thenReturn(application);

        when(loanProductMapper.findActiveById(1L))
                .thenReturn(product);

        when(loanLimitService.calculateForApplication(
                application,
                "EMPLOYED",
                50000000L,
                LocalDate.of(1998, 5, 10)
        )).thenReturn(limitResult);

        when(loanMapper.submitApplication(
                applicationId,
                memberId,
                availableLimit,
                interestRate
        )).thenReturn(1);

        when(loanMapper.findByIdAndMember(
                applicationId, memberId
        )).thenReturn(submittedApplication);

        // when: 최종 제출 실행
        LoanSubmitResponse response =
                loanService.submitApplication(
                        memberId,
                        applicationId
                );

        // then: 반환 결과 검증
        assertEquals(applicationId, response.getApplicationId());
        assertEquals("심사중", response.getApplicationStatus());
        assertEquals(availableLimit, response.getAvailableLimit());
        assertEquals(interestRate, response.getEstimatedInterestRate());
        assertEquals(appliedAt, response.getAppliedAt());

        // Mapper가 정확히 한 번 호출됐는지 확인
        verify(loanMapper, times(1))
                .submitApplication(
                        applicationId,
                        memberId,
                        availableLimit,
                        interestRate
                );
    }


    @Test
    @DisplayName("본인의 대출 신청 상세 정보를 정상적으로 조회한다")
    void getApplicationDetail_success() {

        // given: 테스트 데이터 준비
        Long memberId = 41L;
        Long applicationId = 15L;

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 10, 20, 10, 0);

        LocalDateTime appliedAt =
                LocalDateTime.of(2026, 10, 20, 11, 0);

        LoanDetailResponse detailResponse =
                new LoanDetailResponse(
                        applicationId,
                        1L,
                        "직장인 신용대출",
                        30000000L,
                        36L,
                        40000000L,
                        new BigDecimal("5.5"),
                        "EMPLOYED",
                        50000000L,
                        LocalDate.of(1998, 5, 10),
                        "심사중",
                        createdAt,
                        appliedAt,
                        null,
                        null
                );

        // Mapper가 상세 정보를 반환하도록 설정
        when(loanMapper.findApplicationDetail(
                applicationId,
                memberId
        )).thenReturn(detailResponse);

        // when: Service 실행
        LoanDetailResponse response =
                loanService.getApplicationDetail(
                        memberId,
                        applicationId
                );

        // then: 반환값 검증
        assertNotNull(response);
        assertEquals(applicationId, response.getApplicationId());
        assertEquals("직장인 신용대출", response.getProductName());
        assertEquals(30000000L, response.getApplicationAmount());
        assertEquals(36L, response.getLoanTerm());
        assertEquals("심사중", response.getApplicationStatus());
        assertEquals(appliedAt, response.getAppliedAt());
        assertNull(response.getReviewedAt());

        // Mapper가 정확히 1번 호출되었는지 확인
        verify(loanMapper, times(1))
                .findApplicationDetail(applicationId, memberId);
    }

}
