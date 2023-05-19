package com.tjpu.sp.service.environmentalprotection.envquality;

import java.util.List;
import java.util.Map;

public interface AirCityMonthDataService {

    /**
     * @author: lip
     * @date: 2019/6/5 0005 下午 6:33
     * @Description: 获取一段时间城市月的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:月份开始日期（yyyy-mm）
     * @param: endtime:月份结束日期（yyyy-mm）
     * @return: key：yyyy-mm，value:综合指数值map
     */
    Map<String, Map<String,Object>> getAirCityMonthCompositeIndexByMonitorTimes(String starttime, String endtime) throws Exception;


    List<Map<String, Object>> getRegionDataList();

    Map<String, Map<String, Object>> getMonthCompositeIndexByParam(Map<String, Object> map);
}
