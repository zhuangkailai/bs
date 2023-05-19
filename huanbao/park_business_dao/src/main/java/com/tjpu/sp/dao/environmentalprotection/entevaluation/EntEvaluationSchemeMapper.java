package com.tjpu.sp.dao.environmentalprotection.entevaluation;

import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationSchemeVO;

import java.util.List;
import java.util.Map;

public interface EntEvaluationSchemeMapper {
    int deleteByPrimaryKey(String pkSchemeid);

    int insert(EntEvaluationSchemeVO record);

    int insertSelective(EntEvaluationSchemeVO record);

    EntEvaluationSchemeVO selectByPrimaryKey(String pkSchemeid);

    int updateByPrimaryKeySelective(EntEvaluationSchemeVO record);

    int updateByPrimaryKey(EntEvaluationSchemeVO record);

    List<Map<String,Object>> getEntEvaluationSchemeListDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEntEvaluationSchemeDetailById(String id);
}