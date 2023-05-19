package com.tjpu.sp.dao.envhousekeepers.problemconsult;

import com.tjpu.sp.model.envhousekeepers.problemconsult.EntProblemConsultRecordVO;

import java.util.List;
import java.util.Map;

public interface EntProblemConsultRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntProblemConsultRecordVO record);

    int insertSelective(EntProblemConsultRecordVO record);

    EntProblemConsultRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntProblemConsultRecordVO record);

    int updateByPrimaryKey(EntProblemConsultRecordVO record);

    List<Map<String,Object>> getEntProblemConsultRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEntProblemConsultRecordDetailByID(String id);

    List<Map<String,Object>> getAllSearchProblemDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getNoReadProblemConsultRecordByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getNoReadEntProblemConsultRecordByParam(Map<String, Object> parammap);
}