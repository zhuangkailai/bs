package com.tjpu.sp.dao.environmentalprotection.navigation;

import com.tjpu.sp.model.environmentalprotection.navigation.NavigationRecordInfoVO;

import java.util.List;
import java.util.Map;

public interface NavigationRecordInfoMapper {
    int deleteByPrimaryKey(String pkNavigationid);

    int insert(NavigationRecordInfoVO record);

    int insertSelective(NavigationRecordInfoVO record);

    NavigationRecordInfoVO selectByPrimaryKey(String pkNavigationid);

    int updateByPrimaryKeySelective(NavigationRecordInfoVO record);

    int updateByPrimaryKey(NavigationRecordInfoVO record);

    List<Map<String,Object>> getNavigationRecordInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getNavigationRecordInfoDetailByID(String pkid);

    List<Map<String,Object>> getNavigationDataGroupByNavigationDateByMonth(Map<String, Object> paramMap);

    List<Map<String,Object>> getNavigationDataByNavigationDate(Map<String, Object> paramMap);
}