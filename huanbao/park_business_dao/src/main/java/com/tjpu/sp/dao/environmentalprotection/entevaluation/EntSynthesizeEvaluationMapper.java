package com.tjpu.sp.dao.environmentalprotection.entevaluation;

import com.tjpu.sp.model.environmentalprotection.entevaluation.EntSynthesizeEvaluationVO;

import java.util.List;
import java.util.Map;

public interface EntSynthesizeEvaluationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntSynthesizeEvaluationVO record);

    int insertSelective(EntSynthesizeEvaluationVO record);

    EntSynthesizeEvaluationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntSynthesizeEvaluationVO record);

    int updateByPrimaryKey(EntSynthesizeEvaluationVO record);

    List<Map<String,Object>> getEntSynthesizeEvaluationListDataByParamMap(Map<String, Object> parammap);

    List<Map<String,Object>> getEvaluationLevelByParamMap(Map<Object, Object> objectObjectHashMap);

    Map<String,Object> getEntSynthesizeEvaluationDetailById(String id);

    List<Map<String, Object>> getLastEntEvaDataList();

    List<Map<String,Object>> getEntLastTwoEvaluationData();



    List<Map<String, Object>> countEntEvaDataList();

    List<Map<String, Object>> getEntRegionEvaDataList();

    List<Map<String, Object>> getLastEntEvaDataListByParam(Map<String, Object> paramMap);
}