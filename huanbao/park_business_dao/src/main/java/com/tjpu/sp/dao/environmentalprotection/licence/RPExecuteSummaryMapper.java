package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPExecuteSummaryVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPExecuteSummaryMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPExecuteSummaryVO record);

    int insertSelective(RPExecuteSummaryVO record);

    RPExecuteSummaryVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPExecuteSummaryVO record);

    int updateByPrimaryKey(RPExecuteSummaryVO record);

    List<Map<String, Object>> getParamDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getYFLDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getRLDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterFacilityDataListByParam(Map<String, Object> paramMap);
    List<Map<String, Object>> getGasFacilityDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getZXDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getReportRequireByParam(Map<String, Object> paramMap);
}