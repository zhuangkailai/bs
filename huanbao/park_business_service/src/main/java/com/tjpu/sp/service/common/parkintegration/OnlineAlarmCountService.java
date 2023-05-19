package com.tjpu.sp.service.common.parkintegration;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OnlineAlarmCountService {

    List<Map<String,Object>> countVideoAlarmDataNumByParam(Map<String, Object> paramMap);

    List<Map> countAlarmDataByParamGroupByAlarmType(List<Integer> alarmtypes, List<String> mns,List<String> hb_mns,List<String>aq_mns, Date startDate, Date endDate,Map<String, Object> paramMap);

    List<Map> countAlarmDataByParamGroupByMonitorType(List<Integer> alarmtypes, List<String> mns,List<String> hb_mns,List<String>aq_mns, Date startDate, Date endDate, Map<String, Object> paramMap, Map<String,String> mn_type);

    List<Map> countIntegrationChangeDataByParamMap(Integer remind, List<String> mns, Date startDate, Date endDate, Map<String, Object> pollutantdata, Map<String, Map<String, Object>> mnAndPointData) throws ParseException;

    List<Map<String,Object>> getTodayAlarmAndDevOpsTasks(Map<String, Object> paramMap);

    List<Map<String,Object>> getTodayAlarmTasksByTaskTypes(Map<String, Object> paramMap);

    List<Map> countIntegrationAlarmDataByParamForApp(Integer remind,List<String> mns, List<Map<String, Object>> hb_allpoints, Date startDate, Date endDate, Map<String, Object> pollutantdata)throws ParseException;
}
