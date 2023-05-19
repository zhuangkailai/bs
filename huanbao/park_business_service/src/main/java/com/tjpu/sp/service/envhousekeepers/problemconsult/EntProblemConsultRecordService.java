package com.tjpu.sp.service.envhousekeepers.problemconsult;

import com.tjpu.sp.model.envhousekeepers.problemconsult.EntProblemConsultRecordVO;

import java.util.List;
import java.util.Map;

public interface EntProblemConsultRecordService {

    List<Map<String,Object>> getEntProblemConsultRecordByParamMap(Map<String, Object> paramMap);

    int insert(EntProblemConsultRecordVO entity);

    EntProblemConsultRecordVO selectByPrimaryKey(String id);

    int updateByPrimaryKey(EntProblemConsultRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getEntProblemConsultRecordDetailByID(String id);

    List<Map<String,Object>> getAllSearchProblemDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getNoReadProblemConsultRecordByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getNoReadEntProblemConsultRecordByParam(Map<String, Object> parammap);
}
