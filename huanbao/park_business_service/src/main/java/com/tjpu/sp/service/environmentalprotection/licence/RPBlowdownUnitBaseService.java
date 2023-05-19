package com.tjpu.sp.service.environmentalprotection.licence;


import java.util.List;
import java.util.Map;

public interface RPBlowdownUnitBaseService {


    List<Map<String, Object>> getBlowdownUnitDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitYLDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitNYDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitTZDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitCPDataListByParam(Map<String, Object> paramMap);
}