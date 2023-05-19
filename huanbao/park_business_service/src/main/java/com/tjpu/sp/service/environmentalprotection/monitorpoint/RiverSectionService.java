package com.tjpu.sp.service.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.GroundWaterVO;

import java.util.List;
import java.util.Map;


public interface RiverSectionService {

    List<Map<String, Object>> getRiverSectionPointListByParam(Map<String, Object> paramMap);

    long countTotalByParam(Map<String, Object> paramMap);
}
