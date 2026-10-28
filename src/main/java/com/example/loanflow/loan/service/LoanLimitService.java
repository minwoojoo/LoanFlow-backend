package com.example.loanflow.loan.service;

import com.example.loanflow.loan.dto.request.LoanLimitCheckRequest;
import com.example.loanflow.loan.dto.response.LoanLimitCheckResponse;
import com.example.loanflow.activity.service.UserActivityLogService;
import com.example.loanflow.activity.enums.UserActivityEventType;
import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.entity.LoanEmploymentPolicy;
import com.example.loanflow.loan.entity.LoanProduct;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.loan.mapper.LoanLimitMapper;
import com.example.loanflow.loan.mapper.LoanProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class LoanLimitService {

    private final LoanMapper loanMapper;
    private final LoanProductMapper loanProductMapper;
    private final LoanLimitMapper loanLimitMapper;
    private final UserActivityLogService userActivityLogService;

    // =========================
    // 예상 대출 한도 및 금리 조회 API
    // =========================
    @Transactional
    public LoanLimitCheckResponse checkLimit(
            Long memberId,
            LoanLimitCheckRequest request
    ) {

        // 1. 작성 중인 신청서 조회 및 소유권 검증
        LoanApplication application =
                loanMapper.findWritingByIdAndMember(
                        request.getApplicationId(),
                        memberId
                );

        if (application == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "작성 중인 대출 신청서를 찾을 수 없습니다."
            );
        }

        // 2. 공통 예상 한도 계산 메서드 호출
        LoanLimitCheckResponse result = calculateForApplication(
                application,
                request.getEmploymentType(),
                request.getAnnualIncome(),
                request.getBirthDate()
        );

        // 3. 한도 조회 활동 로그 기록
        userActivityLogService.recordEvent(
                memberId,
                application.getApplicationId(),
                UserActivityEventType.LIMIT_CHECK
        );

        // 4. 기존 결과 반환
        return result;
    }

    // =========================
    // 공통 예상 한도 계산 메서드
    // 한도 조회 / 임시저장 / 최종 제출에서 재사용
    // =========================
    @Transactional(readOnly = true)
    public LoanLimitCheckResponse calculateForApplication(
            LoanApplication application,
            String employmentType,
            Long annualIncome,
            LocalDate birthDate
    ) {

        // 1. 입력값 검증
        if (employmentType == null
                || employmentType.trim().isEmpty()
                || annualIncome == null
                || annualIncome < 0
                || birthDate == null
                || !birthDate.isBefore(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "예상 한도 계산을 위한 입력값이 올바르지 않습니다."
            );
        }

        // 2. 신청서에 연결된 활성 대출 상품 조회
        LoanProduct product =
                loanProductMapper.findActiveById(
                        application.getProductId()
                );

        if (product == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "신청할 수 없는 대출 상품입니다."
            );
        }

        // 3. 만 나이 계산
        int age = Period.between(
                birthDate,
                LocalDate.now()
        ).getYears();

        // 4. 최소 신청 나이 검증
        if (product.getMinAge() != null
                && age < product.getMinAge()) {

            return ineligible(
                    application.getApplicationId(),
                    "해당 상품의 최소 신청 나이는 만 "
                            + product.getMinAge()
                            + "세입니다."
            );
        }

        // 5. 최대 신청 나이 검증
        if (product.getMaxAge() != null
                && age > product.getMaxAge()) {

            return ineligible(
                    application.getApplicationId(),
                    "해당 상품의 최대 신청 나이는 만 "
                            + product.getMaxAge()
                            + "세입니다."
            );
        }

        // 6. 직업별 한도 정책 조회
        LoanEmploymentPolicy policy =
                loanLimitMapper.findByProductAndEmploymentType(
                        product.getProductId(),
                        employmentType
                );

        if (policy == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "해당 직업 유형에 대한 대출 정책이 존재하지 않습니다."
            );
        }

        // 7. 예상 대출 한도 계산
        Long availableLimit = calculateLimit(
                annualIncome,
                policy.getLimitMultiplier(),
                product.getMaxLimit()
        );

        // 8. 예상 금리 조회
        BigDecimal estimatedInterestRate = product.getBaseInterestRate();

        // 9. 응답 DTO 반환
        return new LoanLimitCheckResponse(
                application.getApplicationId(),
                availableLimit,
                estimatedInterestRate,
                true,
                null
        );
    }

    // =========================
    // 예상 한도 계산 메서드
    // =========================
    private Long calculateLimit(
            Long annualIncome,
            BigDecimal multiplier,
            Long maxLimit
    ) {

        // 1. 연 소득을 BigDecimal로 변환
        BigDecimal income = BigDecimal.valueOf(annualIncome);

        // 2. 직업별 한도 배수 적용
        BigDecimal calculatedLimit = income.multiply(multiplier);

        // 3. 원 단위에서 소수점 이하 버림
        calculatedLimit = calculatedLimit.setScale(
                0,
                RoundingMode.DOWN
        );

        // 4. 상품 최대 한도 적용
        if (maxLimit != null) {
            calculatedLimit = calculatedLimit.min(
                    BigDecimal.valueOf(maxLimit)
            );
        }

        // 5. Long으로 변환
        return calculatedLimit.longValueExact();
    }

    // =========================
    // 신청 자격 미충족 응답 생성
    // =========================
    private LoanLimitCheckResponse ineligible(
            Long applicationId,
            String reason
    ) {

        return new LoanLimitCheckResponse(
                applicationId,
                null,
                null,
                false,
                reason
        );
    }
}