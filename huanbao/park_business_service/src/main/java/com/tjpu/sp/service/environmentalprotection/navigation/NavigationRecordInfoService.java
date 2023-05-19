package com.tjpu.sp.service.environmentalprotection.navigation;


import com.tjpu.sp.model.environmentalprotection.navigation.NavigationRecordInfoVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NavigationRecordInfoService {


    List<Map<String,Object>> getNavigationRecordInfosByParamMap(Map<String, Object> paramMap);

    void insert(NavigationRecordInfoVO navigationRecordInfoVO);

    NavigationRecordInfoVO selectByPrimaryKey(String id);

    void updateByPrimaryKey(NavigationRecordInfoVO navigationRecordInfoVO);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getNavigationRecordInfoDetailByID(String id);

    List<Map<String,Object>> getNavigationDataGroupByNavigationDateByMonth(Map<String, Object> paramMap);

    List<Map<String,Object>> getNavigationDataByNavigationDate(Map<String, Object> paramMap);

    List<Map<String,Object>> countNavigationPollutantDataByMonitorTimes(String dgimn, Date startdate, Date enddate);

    Map<String,Object> getNavigationRealTimeSumDataByParam(String dgimn, Date startdate, Date enddate, Integer pagenum);

    Map<String,Object> getNavigationRealTimeDataByParam(String dgimn, Date startdate, Date enddate, Integer pagenum,List<String> pollutantcodes);
}
