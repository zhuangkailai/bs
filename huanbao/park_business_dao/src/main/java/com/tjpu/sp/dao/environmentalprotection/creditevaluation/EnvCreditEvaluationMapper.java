package com.tjpu.sp.dao.environmentalprotection.creditevaluation;

import com.tjpu.sp.model.environmentalprotection.creditevaluation.EnvCreditEvaluationVO;

import java.util.List;
import java.util.Map;

public interface EnvCreditEvaluationMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(EnvCreditEvaluationVO record);

    int insertSelective(EnvCreditEvaluationVO record);

    EnvCreditEvaluationVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(EnvCreditEvaluationVO record);

    int updateByPrimaryKey(EnvCreditEvaluationVO record);

    List<Map<String, Object>> getEnvCreditEvaluationsByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getEnvCreditEvaluationDetailByID(String pkid);

    List<Map<String, Object>> getEntRegionEvaDataList();

    List<Map<String, Object>> countEntEvaDataList();

    String getLastEntEnvCreditByPid(String pollutionid);



    List<Map<String, Object>> getLastEntEvaDataListByParam(Map<String, Object> paramMap);
}