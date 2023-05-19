package com.tjpu.sp.dao.environmentalprotection.deviceproblemrecord;

import com.tjpu.sp.model.environmentalprotection.deviceproblemrecord.DeviceProblemRecordVO;

import java.util.List;
import java.util.Map;

public interface DeviceProblemRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(DeviceProblemRecordVO record);

    int insertSelective(DeviceProblemRecordVO record);

    DeviceProblemRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(DeviceProblemRecordVO record);

    int updateByPrimaryKey(DeviceProblemRecordVO record);

    List<Map<String,Object>> getDeviceProblemRecordsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getDeviceProblemRecordDetailById(Map<String, Object> paramMap);
}