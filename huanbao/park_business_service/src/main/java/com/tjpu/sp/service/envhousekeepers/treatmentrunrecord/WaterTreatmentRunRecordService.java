package com.tjpu.sp.service.envhousekeepers.treatmentrunrecord;

import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordVO;

import java.util.List;
import java.util.Map;

public interface WaterTreatmentRunRecordService {

    List<Map<String,Object>> getWaterTreatmentRunRecordByParamMap(Map<String, Object> paramMap);

    void insert(WaterTreatmentRunRecordVO entity);

    Map<String, Object> selectByPrimaryKey(String id);

    void updateByPrimaryKey(WaterTreatmentRunRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getWaterTreatmentRunRecordDetailByID(String id);
}
