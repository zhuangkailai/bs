package com.tjpu.sp.service.envhousekeepers.facilitiesrunrecord;

import com.tjpu.sp.model.envhousekeepers.facilitiesrunrecord.ProductionFacilitiesRunRecordVO;

import java.util.List;
import java.util.Map;

public interface FacilitiesRunRecordService {

    List<Map<String,Object>> getFacilitiesRunRecordByParamMap(Map<String,Object> paramMap);

    void insert(ProductionFacilitiesRunRecordVO entity);

    Map<String, Object> selectByPrimaryKey(String id);

    void updateByPrimaryKey(ProductionFacilitiesRunRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getFacilitiesRunRecordDetailByID(String id);
}
