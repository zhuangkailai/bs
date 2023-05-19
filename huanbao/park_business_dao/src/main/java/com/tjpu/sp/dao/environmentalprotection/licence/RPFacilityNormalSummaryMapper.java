package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPFacilityNormalSummaryVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPFacilityNormalSummaryMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPFacilityNormalSummaryVO record);

    int insertSelective(RPFacilityNormalSummaryVO record);

    RPFacilityNormalSummaryVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPFacilityNormalSummaryVO record);

    int updateByPrimaryKey(RPFacilityNormalSummaryVO record);

    List<Map<String, Object>> getNormalDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getSpecialTimeGasPollutantByParam(Map<String, Object> paramMap);
}