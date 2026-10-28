package com.example.loanflow.review.service;

import com.example.loanflow.loan.entity.LoanApplication;
import com.example.loanflow.loan.entity.LoanApplicationStatus;
import com.example.loanflow.loan.mapper.LoanMapper;
import com.example.loanflow.review.dto.ReviewCallbackRequest;
import com.example.loanflow.review.event.ReviewCallbackProcessedEvent;
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

    @Transactional
    public void processResult(ReviewCallbackRequest request) {

        // 1. 필수값 검증
        if (request == null
                || request.getApplicationId() == null
                || isBlank(request.getRequestId())
                || isBlank(request.getReviewResultId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "심사 결과 요청값이 올바르지 않습니다."
            );
        }

        // 2. 심사 결과를 신청 상태로 변환
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

        // 3. 신청서 조회
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

        // 4. 이미 심사 완료된 신청서인지 확인
        if (!"02".equals(application.getStatusCode())) {

            // 동일한 결과가 재전송된 경우 정상 처리
            if (request.getReviewResultId().equals(application.getReviewResultId())
                    && statusCode.equals(application.getStatusCode())) {

                log.info(
                        "중복 심사 콜백 - applicationId: {}",
                        request.getApplicationId()
                );

                // 중복 콜백 처리 로그 이벤트 발행
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

        // 6. UPDATE 실패 또는 동시 콜백 확인
        if (updatedRows != 1) {

            LoanApplication current =
                    loanMapper.findByApplicationAndRequestId(
                            request.getApplicationId(),
                            request.getRequestId()
                    );

            String currentStatusName = loanMapper.findStatusNameByCode(current.getStatusCode());

            if (current != null
                    && request.getReviewResultId().equals(current.getReviewResultId())
                    && statusCode.equals(currentStatusName)) {

                // 동시 요청으로 이미 처리된 동일 콜백
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

        // 7. 심사 결과 처리 성공 이벤트 발행
        publishLogEvent(request, "SUCCESS");
    }

    private void publishLogEvent(ReviewCallbackRequest request, String processStatus) {
        eventPublisher.publishEvent(
                new ReviewCallbackProcessedEvent(
                        request.getApplicationId(),
                        request.getRequestId(),
                        processStatus
                )
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
