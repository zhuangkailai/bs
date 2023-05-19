package com.tjpu.sp.dao.envhousekeepers.checkproblemexpound;

import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface CheckProblemExpoundMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(CheckProblemExpoundVO record);

    int insertSelective(CheckProblemExpoundVO record);

    CheckProblemExpoundVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(CheckProblemExpoundVO record);

    int updateByPrimaryKey(CheckProblemExpoundVO record);

    void batchInsert(@Param("list") List<CheckProblemExpoundVO> paramList);

    void deleteCheckProblemExpoundByCheckEntID(String pkId);

    void updateAllCheckProblemExpoundStatusByParam(Map<String, Object> param);

    List<Map<String,Object>> getCheckProblemExpoundsByParamMap(Map<String, Object> param);

    Map<String,Object> getCheckProblemExpoundDetailByID(String id);

    List<Map<String,Object>> getCheckProblemExpoundProcedureByID(String id);

    List<Map<String,Object>> countCheckProblemNumForEntRank(Map<String, Object> param);

    List<Map<String,Object>> countCheckProblemNumGroupByProblemClass(Map<String, Object> param);

    List<Map<String,Object>> countCheckNumGroupByEnt(Map<String, Object> param);

    Map<String,Object> getCheckPollutionInfoByParam(Map<String, Object> param);

    List<Map<String,Object>> getOneCheckProbleReportDataByParamMap(Map<String, Object> parammap);

    List<Map<String,Object>> getOverdueTimeConfigData();

    Map<String,Object> getCheckProblemExpoundDataByID(String id);

    List<Map<String,Object>> getLastReportProblemByParam(Map<String, Object> param);

    List<Map<String,Object>> getManyCheckProblemExpoundDataByParamMap(Map<String, Object> paramtMap);

    List<Map<String,Object>> countCheckNumGroupByMonthDate(Map<String, Object> param);

    List<Map<String,Object>> countCheckProblemNumGroupByMonthDate(Map<String, Object> param);

    List<Map<String,Object>> countProblemRectificationRateDataGroupByEnt(Map<String, Object> param);

    List<Map<String,Object>> countProblemRateDataGroupByIndustryTypeAndEnt(Map<String, Object> param);

    List<Map<String,Object>> countPollutionProblemDataGroupByCategory(Map<String, Object> param);

    List<Map<String,Object>> getCurrentYearEntCheckProblemDataByParamMap(Map<String, Object> param);

    List<Map<String,Object>> getProblemDataGroupByEntForYearRank(Map<String, Object> param);

    List<Map<String,Object>> countLastMonthEntProblemDataSituation(Map<String, Object> param);

    List<Map<String,Object>> getEntSelfExaminationSituationByParam(Map<String, Object> param);

    Map<String,Object> countNotCompleteCheckProblemNum(Map<String, Object> paramMap);

    List<Map<String, Object>> getHBProblemDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> countProblemForType(Map<String, Object> paramMap);

    List<Map<String, Object>> getMonthProblemByParam(Map<String, Object> paramMap);

    long getTotalTaskNumByParam(Map<String, Object> paramMap);

    List<String> getUserModuleByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getDataListByParamMap(Map<String, Object> paramMap);
}