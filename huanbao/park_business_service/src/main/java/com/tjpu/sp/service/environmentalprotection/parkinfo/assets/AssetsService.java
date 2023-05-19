package com.tjpu.sp.service.environmentalprotection.parkinfo.assets;


import java.util.List;
import java.util.Map;

public interface AssetsService {

    List<Map<String,Object>> getAssertsTypeInfoByYear(String year);
    List<Map<String,Object>> getAssretsInfos();
    List<Map<String,Object>> getPrimeIndustryAssretsInfos();


}
