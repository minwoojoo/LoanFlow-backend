package com.example.loanflow.review.service;

import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.review.dto.ReviewCallbackRequest;
import com.example.loanflow.review.dto.ReviewCallbackProcessedRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCallbackService {

    private final LoanMapper loanMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final LoanProcessingLogService loanProcessingLogService;

    // 심사 결과 처리
    @Transactional
    public void processResult(ReviewCallbackRequest request) {

        try {
            processReview(request);

        } catch (RuntimeException e) {

            log.error(
                    "심사 콜백 처리 실패 - applicationId: {}",
                    request == null ? null : request.getApplicationId(),
                    e
            );

            // 실패 로그 저장
            saveFailureLog(request, e);

            // 기존 트랜잭션 롤백 및 HTTP 오류 응답을 위해 예외 재전파
            throw e;
        }
    }

    // 실제 심사 결과 검증 및 DB 반영
    private void processReview(ReviewCallbackRequest request) {

        // 1. 필수 입력값 검증
        if (request == null
                || request.getApplicationId() == null
                || isBlank(request.getRequestId())
                || isBlank(request.getReviewResultId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "심사 결과 요청값이 올바르지 않습니다."
            );
        }

        // 2. 심사 결과를 신청 상태 코드로 변환
        String statusCode;

        if ("APPROVED".equals(request.getReviewResult())) {
            statusCode = "03";

        } else if ("REJECTED".equals(request.getReviewResult())) {
            statusCode = "04";

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "유효하지 않은 심사 결과입니다."
            );
        }

        // 3. 신청서 ID + 요청 ID로 조회
        LoanApplication application =
                loanMapper.findByApplicationAndRequestId(
                        request.getApplicationId(),
                        request.getRequestId()
                );

        if (application == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "심사 대상 신청서를 찾을 수 없습니다."
            );
        }

        // 4. 심사 진행 상태인지 확인
        if (!"02".equals(application.getStatusCode())) {

            // 동일한 결과가 재전송된 경우 중복 처리
            if (request.getReviewResultId()
                    .equals(application.getReviewResultId())
                    && statusCode.equals(application.getStatusCode())) {

                log.info(
                        "중복 심사 콜백 - applicationId: {}",
                        request.getApplicationId()
                );

                publishLogEvent(request, "DUPLICATE");
                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 처리되었거나 심사할 수 없는 신청서입니다."
            );
        }

        // 5. 심사 결과 UPDATE
        int updatedRows = loanMapper.updateReviewResult(
                request.getApplicationId(),
                request.getRequestId(),
                statusCode,
                request.getReviewResultId()
        );

        // 6. UPDATE 실패 또는 동시 Callback 확인
        if (updatedRows != 1) {

            LoanApplication current =
                    loanMapper.findByApplicationAndRequestId(
                            request.getApplicationId(),
                            request.getRequestId()
                    );

            // 이전 코드의 null 검사 순서 및 상태명 비교 오류 수정
            if (current != null
                    && request.getReviewResultId()
                    .equals(current.getReviewResultId())
                    && statusCode.equals(current.getStatusCode())) {

                log.info(
                        "동시 요청으로 인한 중복 심사 콜백 - applicationId: {}",
                        request.getApplicationId()
                );

                publishLogEvent(request, "DUPLICATE");
                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "심사 결과 반영에 실패했습니다."
            );
        }

        log.info(
                "심사 결과 반영 완료 - applicationId: {}, reviewResultId: {}, status: {}",
                request.getApplicationId(),
                request.getReviewResultId(),
                statusCode
        );

        // 7. 성공 로그 이벤트 발행
        publishLogEvent(request, "SUCCESS");
    }

    // Callback 실패 로그 저장
    private void saveFailureLog(
            ReviewCallbackRequest request,
            RuntimeException e
    ) {

        // 신청서 ID가 없는 요청은 로그 저장 생략
        if (request == null || request.getApplicationId() == null) {
            return;
        }

        try {
            String errorMessage = e.getMessage() == null
                    ? "콜백 처리 오류"
                    : e.getMessage();

            // DB 컬럼 길이 초과 방지
            if (errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 500);
            }

            // 별도 트랜잭션에서 실패 로그 저장
            loanProcessingLogService.saveLog(
                    request.getApplicationId(),
                    request.getRequestId(),
                    "REVIEW_CALLBACK",
                    "FAILED",
                    "CALLBACK_ERROR",
                    errorMessage
            );

        } catch (Exception logException) {

            // 로그 저장 오류가 원래 예외를 덮어쓰지 않도록 처리
            log.error(
                    "심사 콜백 실패 로그 저장 실패 - applicationId: {}",
                    request.getApplicationId(),
                    logException
            );
        }
    }

    // Callback 처리 성공 또는 중복 로그 이벤트 발행
    private void publishLogEvent(
            ReviewCallbackRequest request,
            String processStatus
    ) {

        eventPublisher.publishEvent(
                new ReviewCallbackProcessedRequest(
                        request.getApplicationId(),
                        request.getRequestId(),
                        processStatus
                )
        );
    }

    // 공통 문자열 검증
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}