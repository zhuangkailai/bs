package com.tjpu.sp.dao.envhousekeepers.treatmentrunrecord;

import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordVO;

import java.util.List;
import java.util.Map;

public interface WaterTreatmentRunRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WaterTreatmentRunRecordVO record);

    int insertSelective(WaterTreatmentRunRecordVO record);

    WaterTreatmentRunRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WaterTreatmentRunRecordVO record);

    int updateByPrimaryKey(WaterTreatmentRunRecordVO record);

    List<Map<String,Object>> getWaterTreatmentRunRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getWaterTreatmentRunRecordDetailByID(String id);

    Map<String,Object> getWaterTreatmentRunRecordByID(String id);
}