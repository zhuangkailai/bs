package com.tjpu.sp.dao.envhousekeepers.checkentinfo;

import com.tjpu.sp.model.envhousekeepers.checkentinfo.EntCheckFeedbackRecordVO;

import java.util.List;
import java.util.Map;

public interface EntCheckFeedbackRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntCheckFeedbackRecordVO record);

    int insertSelective(EntCheckFeedbackRecordVO record);

    EntCheckFeedbackRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntCheckFeedbackRecordVO record);

    int updateByPrimaryKey(EntCheckFeedbackRecordVO record);

    Map<String,Object> getEntCheckFeedbackRecordDetailByParam(Map<String, Object> param);

    List<Map<String, Object>> getEntCheckFeedbackRecordDataByParam(Map<String, Object> param);

    EntCheckFeedbackRecordVO selectByPollutionidAndCheckTime(Map<String, Object> param);
}