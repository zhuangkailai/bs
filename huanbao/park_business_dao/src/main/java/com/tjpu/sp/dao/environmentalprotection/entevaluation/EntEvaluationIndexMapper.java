package com.tjpu.sp.dao.environmentalprotection.entevaluation;

import com.tjpu.sp.model.environmentalprotection.entevaluation.EntEvaluationIndexVO;

import java.util.List;
import java.util.Map;

public interface EntEvaluationIndexMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntEvaluationIndexVO record);

    int insertSelective(EntEvaluationIndexVO record);

    EntEvaluationIndexVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntEvaluationIndexVO record);

    int updateByPrimaryKey(EntEvaluationIndexVO record);

    List<Map<String,Object>> getEntEvaluationIndexListDataByParamMap(Map<String, Object> parammap);

    Map<String,Object> getEntEvaluationIndexDetailById(String id);

    List<Map<String,Object>> getEntEvaluationIndexInfos();

    List<Map<String,Object>> getAllEntEvaluationIndexType();

    List<Map<String,Object>> getEntEvaluationIndexPageData(Map<String,Object> paramMap);
}