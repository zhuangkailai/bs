package com.tjpu.sp.service.environmentalprotection.weather;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface WeatherService {
    /**
     * @author: xsm
     * @date: 2019/6/26 7:34
     * @Description: 通过自定义参数统计监测点位的监测数据及气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> countWeatherAndMonitorPointDataByParamMap(String airmn, String mn, Integer pointtype, String timetype, List<String> pollutantcodes, String starttime, String endtime);

    /**
     * @author: xsm
     * @date: 2019/6/27 17:26
     * @Description: 通过自定义参数获取监测点位的列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWeathersAndMonitorPointListsByParamMap(String airmn, String mn, Integer pointtype, String timetype, List<String> pollutantcodes, String starttime, String endtime);

    /**
     * @author: xsm
     * @date: 2019/6/28 11:28
     * @Description: 通过自定义参数获取污染物气象表头
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTableTitleForWeathers(Map<String, Object> titleMap);

    /**
     * @author: xsm
     * @date: 2019/7/26 6:43
     * @Description: 根据监测时间和Mn号获取单个或多个监测点在某个时间点的风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWeatherDataByMonitortimeAndMns(String dateType, String monitortime, String dgimns);

    /**
     * @author: xsm
     * @date: 2019/7/27 2:38
     * @Description: 根据监测时间和Mn号获取单个监测点在某个时间范围的风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWeatherDataByMonitortimesAndMn(String datetype, String starttime, String endtime, String dgimn);

    List<Map<String, Object>> getMonitorPointWeatherData(String airmn, String timetype, Date monitortime, Date endDate);

    /**
     *
     * @author: lip
     * @date: 2019/8/14 0014 下午 3:24
     * @Description: 自定义查询条件获取日气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getDayWeatherByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/20 0020 上午 9:48
     * @Description: 获取当天实时气候和未来四天气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getTodayLastHourAndAfterFourDayWeathers();

    /**
     * @author: xsm
     * @date: 2020/5/19 0019 上午 10:17
     * @Description: 根据日期获取该日期所有小时气候数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getHoursWeathersByDayTime(String daytime);


    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 下午 3:46
     * @Description: 获取当天最新天气信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    Map<String,Object> getTodayLastDayWeather();

    Map<String, Object> getHourWeathersByTime(String monitortime);
}
