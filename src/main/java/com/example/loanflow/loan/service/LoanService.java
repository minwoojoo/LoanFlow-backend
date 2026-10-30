
package com.example.loanflow.loan.service;

import com.example.loanflow.loan.dto.request.LoanCreateRequest;
import com.example.loanflow.loan.dto.request.LoanUpdateRequest;
import com.example.loanflow.loan.dto.response.*;
import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.entity.LoanProduct;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.loan.mapper.LoanProductMapper;
import com.example.loanflow.activity.service.UserActivityLogService;
import com.example.loanflow.activity.enums.UserActivityEventType;

import com.example.loanflow.review.dto.MockReviewRequest;
import com.example.loanflow.terms.entity.Terms;
import com.example.loanflow.terms.mapper.TermsMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanMapper loanMapper;
    private final LoanProductMapper loanProductMapper;
    private final TermsMapper termsMapper;
    private final LoanLimitService loanLimitService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserActivityLogService userActivityLogService;



    // ==========================
    // 1. 신규 대출 신청서 생성
    // ==========================
    @Transactional
    public LoanCreateResponse createApplication(
            Long memberId,
            LoanCreateRequest request
    ) {

        // 1. 선택한 대출 상품 확인
        LoanProduct product = loanProductMapper.findActiveById(
                request.getProductId()
        );

        if (product == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "신청할 수 없는 대출 상품입니다."
            );
        }

        // 2. 동일 회원, 동일 상품의 기존 신청서 확인 및 중복 생성 방지
        LoanApplication existingApplication = loanMapper.findWritingApplication(
                memberId,
                request.getProductId()
        );

        System.out.println("===== 중복 신청서 확인 =====");
        System.out.println("memberId: " + memberId);
        System.out.println("productId: " + request.getProductId());
        System.out.println(
                "기존 신청서: " +
                        (existingApplication == null
                                ? "null"
                                : existingApplication.getApplicationId())
        );

        if (existingApplication != null){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 진행 중인 대출 신청서가 존재합니다."
            );
        }

        // 3. 활성 약관 목록 조회
        List<Terms> activeTerms = termsMapper.findAllActive();

        // 4. 동의한 약관 ID 가져오기
        List<Long> agreedTermsIds = request.getAgreedTermsIds();

        Set<Long> agreedIds = new HashSet<>(agreedTermsIds);

        // 5. 중복 약관 ID 검증
        if (agreedIds.size() != agreedTermsIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "중복된 약관 ID가 존재합니다."
            );
        }

        // 6. 활성 약관 및 필수 약관 ID 준비
        Set<Long> activeTermsIds = new HashSet<>();
        Set<Long> requiredTermsIds = new HashSet<>();

        for (Terms terms : activeTerms) {
            activeTermsIds.add(terms.getTermsId());

            if ("Y".equals(terms.getRequiredYn())) {
                requiredTermsIds.add(terms.getTermsId());
            }
        }

        // 7. 비활성 또는 존재하지 않는 약관인지 확인
        if (!activeTermsIds.containsAll(agreedIds)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "유효하지 않은 약관이 포함되어 있습니다."
            );
        }

        // 8. 필수 약관을 모두 동의했는지 확인
        if (!agreedIds.containsAll(requiredTermsIds)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "모든 필수 약관에 동의해야 합니다."
            );
        }

        // 9. 작성중 대출 신청서 객체 생성
        LoanApplication application = LoanApplication.builder()
                .memberId(memberId)
                .productId(product.getProductId())
                .requestId(UUID.randomUUID().toString())
                .statusCode("01")
                .build();

        // 10. 신청서 INSERT
        int applicationRows =
                loanMapper.createApplication(application);

        if (applicationRows != 1 ||
                application.getApplicationId() == null) {
            throw new IllegalStateException(
                    "대출 신청서 생성에 실패했습니다."
            );
        }

        Long applicationId = application.getApplicationId();

        // 11. 약관 동의 내역 INSERT
        for (Long termsId : agreedTermsIds) {
            int consentRows = loanMapper.insertConsent(
                    applicationId,
                    termsId
            );

            if (consentRows != 1) {
                throw new IllegalStateException(
                        "약관 동의 저장에 실패했습니다."
                );
            }
        }

        // 12. 신청서 생성 활동 로그 기록
        userActivityLogService.recordEvent(
                memberId,
                applicationId,
                UserActivityEventType.APPLICATION_CREATE
        );


        // 13. 생성 결과 반환
        return new LoanCreateResponse(
                applicationId,
                "작성중"
        );
    }





    // ==========================
    // 2. 작성 중인 대출 신청서 조회
    // ==========================
    @Transactional(readOnly = true)
    public LoanDraftResponse findDraft(
            Long memberId,
            Long productId
    ){
        // 1. 동일 회원, 동일 상품의 작성 중 신청서 조회
        LoanApplication application = loanMapper.findWritingApplication(
                memberId,
                productId
        );

        // 2. 작성 중 신청서가 존재하지 않는 경우
        if (application == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "작성 중인 대출 신청서가 존재하지 않습니다."
            );
        }

        // 3. Entity -> Response DTO 변환
        String statusName = loanMapper.findStatusNameByCode(application.getStatusCode());

        return new LoanDraftResponse(
                application.getApplicationId(),
                application.getProductId(),
                statusName,
                application.getApplicationAmount(),
                application.getBirthDate(),
                application.getEmploymentType(),
                application.getAnnualIncome(),
                application.getAvailableLimit(),
                application.getEstimatedInterestRate(),
                application.getLoanTerm()
        );

    }



    @Transactional
    public LoanUpdateResponse updateApplicationDraft(
            Long memberId,
            Long applicationId,
            LoanUpdateRequest request
    ) {

        // 1. 작성 중인 신청서 조회 및 소유권 확인
        LoanApplication application =
                loanMapper.findWritingByIdAndMember(
                        applicationId,
                        memberId
                );

        if (application == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "수정 가능한 대출 신청서를 찾을 수 없습니다."
            );
        }

        // 2. 연결된 활성 대출 상품 조회
        LoanProduct product =
                loanProductMapper.findActiveById(
                        application.getProductId()
                );

        if (product == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "신청 가능한 대출 상품이 아닙니다."
            );
        }


        // 3. 기존 저장값과 PATCH 요청값 병합

        // 희망 대출 금액
        Long applicationAmount =
                request.getApplicationAmount() != null
                        ? request.getApplicationAmount()
                        : application.getApplicationAmount();

        // 희망 대출 기간
        Long loanTerm =
                request.getLoanTerm() != null
                        ? request.getLoanTerm()
                        : application.getLoanTerm();

        // 직업 유형
        String employmentType =
                request.getEmploymentType() != null
                        ? request.getEmploymentType()
                        : application.getEmploymentType();

        // 연 소득
        Long annualIncome =
                request.getAnnualIncome() != null
                        ? request.getAnnualIncome()
                        : application.getAnnualIncome();

        // 생년월일
        LocalDate birthDate =
                request.getBirthDate() != null
                        ? request.getBirthDate()
                        : application.getBirthDate();


        // 4. 최대 대출 기간 초과 검증
        if (loanTerm != null
                && product.getMaxLoanTerm() != null
                && loanTerm > product.getMaxLoanTerm()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "해당 상품의 최대 대출 기간은 "
                            + product.getMaxLoanTerm()
                            + "개월입니다."
            );
        }


        // 5. 예상 한도 계산 가능 여부 확인
        boolean canCalculate =
                employmentType != null
                        && !employmentType.trim().isEmpty()
                        && annualIncome != null
                        && birthDate != null;


        // 6. 예상 한도 초과 검증
        if (applicationAmount != null && canCalculate) {

            // 6-1. 공통 예상 한도 계산 메서드 호출
            LoanLimitCheckResponse limitResult =
                    loanLimitService.calculateForApplication(
                            application,
                            employmentType,
                            annualIncome,
                            birthDate
                    );

            // 6-2. 신청 자격 검증
            if (!Boolean.TRUE.equals(limitResult.getEligible())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        limitResult.getIneligibleReason()
                );
            }

            // 6-3. 예상 한도 초과 여부 검증
            Long availableLimit =
                    limitResult.getAvailableLimit();

            if (availableLimit == null) {
                throw new IllegalStateException(
                        "예상 대출 한도를 계산할 수 없습니다."
                );
            }

            if (applicationAmount > availableLimit) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "희망 대출 금액이 예상 한도 "
                                + String.format("%,d", availableLimit)
                                + "원을 초과했습니다."
                );
            }
        }


        // 7. 모든 검증 성공 후 임시저장
        int updatedRows =
                loanMapper.updateApplicationDraft(
                        applicationId,
                        memberId,
                        request
                );

        if (updatedRows != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "신청서 임시저장에 실패했습니다."
            );
        }


        // 8. 저장된 신청서 다시 조회
        LoanApplication updatedApplication =
                loanMapper.findWritingByIdAndMember(
                        applicationId,
                        memberId
                );

        if (updatedApplication == null) {
            throw new IllegalStateException(
                    "임시저장된 신청서를 조회할 수 없습니다."
            );
        }


        // 9. 응답 DTO 반환
        String statusName = loanMapper.findStatusNameByCode(updatedApplication.getStatusCode());

        return new LoanUpdateResponse(
                updatedApplication.getApplicationId(),
                updatedApplication.getMemberId(),
                updatedApplication.getProductId(),
                statusName,
                updatedApplication.getApplicationAmount(),
                updatedApplication.getLoanTerm(),
                updatedApplication.getEmploymentType(),
                updatedApplication.getAnnualIncome(),
                updatedApplication.getBirthDate(),
                updatedApplication.getAvailableLimit(),
                updatedApplication.getEstimatedInterestRate()
        );
    }




    @Transactional
    public LoanSubmitResponse submitApplication(
            Long memberId,
            Long applicationId
    ) {

        // 1. 작성 중인 신청서 조회 및 소유권 검증
        LoanApplication application =
                loanMapper.findWritingByIdAndMember(
                        applicationId,
                        memberId
                );

        if (application == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "제출 가능한 대출 신청서를 찾을 수 없습니다."
            );
        }

        // 2. 활성 대출 상품 조회
        LoanProduct product = loanProductMapper.findActiveById(
                application.getProductId()
        );

        if (product == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "신청할 수 없는 대출 상품입니다."
            );
        }

        // 3. 필수 입력값 누락 검증
        if (application.getApplicationAmount() == null
                || application.getLoanTerm() == null
                || application.getEmploymentType() == null
                || application.getEmploymentType().trim().isEmpty()
                || application.getAnnualIncome() == null
                || application.getBirthDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "대출 신청에 필요한 정보를 모두 입력해야 합니다."
            );
        }

        // 4. 기본 유효성 검증
        if (application.getApplicationAmount() <= 0
                || application.getLoanTerm() <= 0
                || application.getAnnualIncome() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "대출 신청 정보가 올바르지 않습니다."
            );
        }

        // 5. 상품 최대 대출 기간 검증
        if (product.getMaxLoanTerm() != null
                && application.getLoanTerm() > product.getMaxLoanTerm()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "해당 상품의 최대 대출 기간은 "
                            + product.getMaxLoanTerm()
                            + "개월입니다."
            );
        }

        // 6. 예상 한도 및 신청 자격 재검증
        LoanLimitCheckResponse limitResult =
                loanLimitService.calculateForApplication(
                        application,
                        application.getEmploymentType(),
                        application.getAnnualIncome(),
                        application.getBirthDate()
                );

        if (!Boolean.TRUE.equals(limitResult.getEligible())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    limitResult.getIneligibleReason()
            );
        }

        Long availableLimit = limitResult.getAvailableLimit();

        if (availableLimit == null) {
            throw new IllegalStateException(
                    "예상 대출 한도를 계산할 수 없습니다."
            );
        }

        // 7. 희망 금액이 예상 한도를 초과하는지 검증
        if (application.getApplicationAmount() > availableLimit) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "희망 대출 금액이 예상 한도를 초과했습니다."
            );
        }

        // 8. 최종 제출 처리
        int updatedRows = loanMapper.submitApplication(
                applicationId,
                memberId,
                availableLimit,
                limitResult.getEstimatedInterestRate()
        );

        if (updatedRows != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "대출 신청서 제출에 실패했거나 이미 제출되었습니다."
            );
        }

        // 9. 최종 제출된 신청서 다시 조회
        LoanApplication submittedApplication =
                loanMapper.findByIdAndMember(
                        applicationId,
                        memberId
                );

        if (submittedApplication == null){
            throw new IllegalArgumentException(
                    "최종 제출된 대출 신청서를 조회할 수 없습니다."
            );
        }

        if (submittedApplication.getAppliedAt() == null){
            throw new IllegalArgumentException(
                    "대출 신청서 제출 시간이 저장되지 않았습니다."
            );
        }

        // 10. 신청서 최종 제출 활동 로그 기록
        userActivityLogService.recordEvent(
                memberId,
                submittedApplication.getApplicationId(),
                UserActivityEventType.APPLICATION_SUBMIT
        );


        // 심사 요청 이벤트 발행
        eventPublisher.publishEvent(
                new MockReviewRequest(
                        submittedApplication.getApplicationId(),
                        submittedApplication.getRequestId()
                )
        );


        // 11. 대출 신청 상태명 조회
        String statusName = loanMapper.findStatusNameByCode(submittedApplication.getStatusCode());

        if (statusName == null){
            throw new IllegalStateException(
                    "대출 신청 상태명을 조회할 수 없습니다."
            );
        }

        // 11. 응답 DTO 반환
        return new LoanSubmitResponse(
                submittedApplication.getApplicationId(),
                statusName,
                submittedApplication.getAvailableLimit(),
                submittedApplication.getEstimatedInterestRate(),
                submittedApplication.getAppliedAt()
        );
    }


    // 대출 신청 목록 조회
    @Transactional(readOnly = true)
    public List<LoanReadResponse> getMyApplications(
            Long memberId
    ) {
        return loanMapper.findMyApplications(memberId);
    }


    // 대출 신청 상세 조회
    @Transactional(readOnly = true)
    public LoanDetailResponse getApplicationDetail(
            Long memberId,
            Long applicationId
    ) {

        // 1. 로그인한 회원의 신청서 상세 조회
        LoanDetailResponse response =
                loanMapper.findApplicationDetail(
                        applicationId,
                        memberId
                );

        // 2. 신청서가 없거나 본인 소유가 아닌 경우
        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "조회할 수 있는 대출 신청서가 없습니다."
            );
        }

        // 3. 조회 결과 반환
        return response;
    }


    // 작성 중인 대출 신청서 삭제
    @Transactional
    public void deleteApplication(
            Long memberId,
            Long applicationId
    ){
        int number = loanMapper.deleteApplication(
                memberId,
                applicationId
        );

        if (number != 1){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "삭제 가능한 대출 신청서가 없습니다."
            );
        }
    }
}
