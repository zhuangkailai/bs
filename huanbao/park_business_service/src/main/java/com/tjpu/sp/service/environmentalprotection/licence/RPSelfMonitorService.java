package com.tjpu.sp.service.environmentalprotection.licence;


import java.util.List;
import java.util.Map;

public interface RPSelfMonitorService {


    List<Map<String, Object>> getGasConcentrationListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getGasSpeedListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getUnGasConcentrationListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterConcentrationListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getExceptionGasConcentrationListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getExceptionUnGasConcentrationListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getSpecialGasConcentrationListByParam(Map<String, Object> paramMap);
}