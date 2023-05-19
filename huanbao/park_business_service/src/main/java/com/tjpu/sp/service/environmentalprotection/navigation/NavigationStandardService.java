package com.tjpu.sp.service.environmentalprotection.navigation;


import com.tjpu.sp.model.environmentalprotection.navigation.NavigationStandardVO;

import java.util.List;
import java.util.Map;

public interface NavigationStandardService {


    List<Map<String,Object>> getNavigationStandardsByParamMap(Map<String, Object> paramMap);

    void insert(NavigationStandardVO NavigationStandardVO);

    NavigationStandardVO selectByPrimaryKey(String id);

    void updateByPrimaryKey(NavigationStandardVO NavigationStandardVO);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getNavigationStandardDetailByID(String id);

    List<Map<String, Object>> getNavigationStandardDataGroupByCategory(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllLevelNavigationStandardData();

    List<Map<String,Object>> getStandardColorDataByParamMap(Map<String, Object> paramMap);

    Integer CountStandardColorInfoByParamMap(Map<String, Object> paramMap);

}
