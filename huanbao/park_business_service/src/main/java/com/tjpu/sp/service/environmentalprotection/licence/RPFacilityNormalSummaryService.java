package com.tjpu.sp.service.environmentalprotection.licence;


import java.util.List;
import java.util.Map;

public interface RPFacilityNormalSummaryService {

    List<Map<String, Object>> getNormalDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getSpecialTimeGasPollutantByParam(Map<String, Object> paramMap);
}
