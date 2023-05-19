package com.tjpu.sp.dao.envhousekeepers.facilitiesrunrecord;

import com.tjpu.sp.model.envhousekeepers.facilitiesrunrecord.ProductionFacilitiesRunRecordVO;

import java.util.List;
import java.util.Map;

public interface ProductionFacilitiesRunRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(ProductionFacilitiesRunRecordVO record);

    int insertSelective(ProductionFacilitiesRunRecordVO record);

    ProductionFacilitiesRunRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(ProductionFacilitiesRunRecordVO record);

    int updateByPrimaryKey(ProductionFacilitiesRunRecordVO record);

    List<Map<String,Object>> getFacilitiesRunRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getFacilitiesRunRecordDetailByID(String id);
}