package com.tjpu.sp.dao.environmentalprotection.taskmanagement;

import com.tjpu.sp.model.environmentalprotection.taskmanagement.TaskAlarmPollutantInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskAlarmPollutantInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TaskAlarmPollutantInfoVO record);

    int insertSelective(TaskAlarmPollutantInfoVO record);

    TaskAlarmPollutantInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TaskAlarmPollutantInfoVO record);

    int updateByPrimaryKey(TaskAlarmPollutantInfoVO record);

    List<Map<String,Object>> getTaskAlarmPollutantInfoByParamMap(Map<String,Object> paramMap);
}