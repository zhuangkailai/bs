package com.tjpu.sp.dao.environmentalprotection.navigation;

import com.tjpu.sp.model.environmentalprotection.navigation.NavigationStandardVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface NavigationStandardMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(NavigationStandardVO record);

    int insertSelective(NavigationStandardVO record);

    NavigationStandardVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(NavigationStandardVO record);

    int updateByPrimaryKey(NavigationStandardVO record);

    List<Map<String,Object>> getNavigationStandardsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getNavigationStandardDetailByID(String id);

    List<Map<String, Object>> getAllNavigationPollutantData(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllLevelNavigationStandardData();

    List<Map<String,Object>> getStandardColorDataByParamMap(Map<String, Object> paramMap);

    Integer CountStandardColorInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutanDischargeStandardDataByParam(Map<String, Object> parammap);
}