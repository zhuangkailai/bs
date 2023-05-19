package com.tjpu.sp.service.envhousekeepers.treatmentrunrecord;

import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordVO;

import java.util.List;
import java.util.Map;

public interface GasTreatmentRunRecordService {

    List<Map<String,Object>> getGasTreatmentRunRecordByParamMap(Map<String, Object> paramMap);

    void insert(GasTreatmentRunRecordVO entity);

    Map<String, Object> selectByPrimaryKey(String id);

    void updateByPrimaryKey(GasTreatmentRunRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getGasTreatmentRunRecordDetailByID(String id);
}
