package com.tjpu.sp.dao.environmentalprotection.parkinfo.assets;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AssetsMapper {

    List<Map<String,Object>> getAssertsTypeInfoByYear(String year);
    List<Map<String,Object>> getAssretsInfos();
    List<Map<String,Object>> getPrimeIndustryAssretsInfos();
}