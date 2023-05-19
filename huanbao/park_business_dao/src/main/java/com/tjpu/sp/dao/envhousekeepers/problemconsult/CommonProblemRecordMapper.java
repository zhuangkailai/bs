package com.tjpu.sp.dao.envhousekeepers.problemconsult;

import com.tjpu.sp.model.envhousekeepers.problemconsult.CommonProblemRecordVO;

import java.util.List;
import java.util.Map;

public interface CommonProblemRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(CommonProblemRecordVO record);

    int insertSelective(CommonProblemRecordVO record);

    CommonProblemRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(CommonProblemRecordVO record);

    int updateByPrimaryKey(CommonProblemRecordVO record);

    List<Map<String,Object>> getCommonProblemRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getCommonProblemRecordDetailByID(String id);

    List<Map<String,Object>> getCommonProblemTypesByParam(Map<String, Object> param);

    List<Map<String,Object>> getKeyCommonProblemRecordByParam(Map<String, Object> param);
}