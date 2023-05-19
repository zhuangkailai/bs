package com.tjpu.sp.service.environmentalprotection.licence;


import java.util.List;
import java.util.Map;

public interface OrganizedGasDischargeLimitService {

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);


    List<Map<String, Object>> getTotalDataListByParam(Map<String, Object> paramMap);
}
