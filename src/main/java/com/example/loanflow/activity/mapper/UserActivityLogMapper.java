package com.example.loanflow.activity.mapper;

import com.example.loanflow.activity.entity.UserActivityLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserActivityLogMapper {
    int insertActivityLog(UserActivityLog log);
}
