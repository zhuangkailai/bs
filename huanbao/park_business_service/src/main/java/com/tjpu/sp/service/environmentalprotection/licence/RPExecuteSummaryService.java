package com.tjpu.sp.service.environmentalprotection.licence;


import java.util.List;
import java.util.Map;

public interface RPExecuteSummaryService {


    List<Map<String, Object>> getParamDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getYFLDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getRLDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterFacilityDataListByParam(Map<String, Object> paramMap);
    List<Map<String, Object>> getGasFacilityDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getZXDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getReportRequireByParam(Map<String, Object> paramMap);
}