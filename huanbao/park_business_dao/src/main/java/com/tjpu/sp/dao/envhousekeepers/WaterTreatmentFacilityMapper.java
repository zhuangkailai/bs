package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.WaterTreatmentFacilityVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WaterTreatmentFacilityMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WaterTreatmentFacilityVO record);

    int insertSelective(WaterTreatmentFacilityVO record);

    WaterTreatmentFacilityVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WaterTreatmentFacilityVO record);

    int updateByPrimaryKey(WaterTreatmentFacilityVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> getWaterOutPutByPollutionId(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterFacilityDataListByParam(Map<String, Object> paramMap);
}