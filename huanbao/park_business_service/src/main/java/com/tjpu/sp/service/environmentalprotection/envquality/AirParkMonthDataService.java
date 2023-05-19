package com.tjpu.sp.service.environmentalprotection.envquality;

import java.util.Map;

public interface AirParkMonthDataService {

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


    /**
     * @author: xsm
     * @date: 2019/6/6 0006 下午 1:46
     * @Description: 根据监测时间获取某个月城市空气各污染物的平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:监测时间（yyyy-mm）
     * @return: key：因子名称，value:因子的平均浓度
     */
    Map<String,Double> getAirCityConcentrationByMonitorTimes(String monitortime);
}
