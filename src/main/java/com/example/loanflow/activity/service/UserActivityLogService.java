package com.example.loanflow.activity.service;

import com.example.loanflow.activity.entity.UserActivityLog;
import com.example.loanflow.activity.enums.UserActivityEventType;
import com.example.loanflow.activity.mapper.UserActivityLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActivityLogService {
    private final UserActivityLogMapper userActivityLogMapper;

    public void recordEvent(
            Long memberId,
            Long applicationId,
            UserActivityEventType eventType
    ){
        UserActivityLog activityLog = UserActivityLog.builder()
                .memberId(memberId)
                .applicationId(applicationId)
                .sessionId(null)
                .eventType(eventType.name())
                .build();

        userActivityLogMapper.insertActivityLog(activityLog);
    }
}
