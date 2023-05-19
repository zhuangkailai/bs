package com.tjpu.sp.service.envhousekeepers.problemconsult;

import com.tjpu.sp.model.envhousekeepers.problemconsult.CommonProblemRecordVO;

import java.util.List;
import java.util.Map;

public interface CommonProblemRecordService {

    List<Map<String,Object>> getCommonProblemRecordByParamMap(Map<String, Object> paramMap);

    void insert(CommonProblemRecordVO entity);

    CommonProblemRecordVO selectByPrimaryKey(String id);

    void updateByPrimaryKey(CommonProblemRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getCommonProblemRecordDetailByID(String id);

    List<Map<String,Object>> getKeyCommonProblemRecordByParam(Map<String,Object> param);
}
