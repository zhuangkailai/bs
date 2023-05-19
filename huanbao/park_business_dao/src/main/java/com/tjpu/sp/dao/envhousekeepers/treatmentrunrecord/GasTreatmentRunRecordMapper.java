package com.tjpu.sp.dao.envhousekeepers.treatmentrunrecord;

import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordVO;

import java.util.List;
import java.util.Map;

public interface GasTreatmentRunRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(GasTreatmentRunRecordVO record);

    int insertSelective(GasTreatmentRunRecordVO record);

    GasTreatmentRunRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(GasTreatmentRunRecordVO record);

    int updateByPrimaryKey(GasTreatmentRunRecordVO record);

    List<Map<String,Object>> getGasTreatmentRunRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getGasTreatmentRunRecordDetailByID(String id);

    Map<String,Object> getGasTreatmentRunRecordByID(String id);
}