package com.tjpu.sp.service.environmentalprotection.tracesource;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PollutionTraceSourceService {

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 9:57
     * @Description: 根据监测时间获取所有恶臭、voc、厂界恶臭的MN号和关联气象的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceMonitorPointMN(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 10:09
     * @Description: 获取主导风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getLeadingWindDirectionAndWindSpeed(String monitortime);

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 11:20
     * @Description: 获取所有废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorGasOutPutInfo();


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:06
     * @Description: 获取废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getGasOutPutInfo(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 11:20
     * @Description: 根据监测时间和污染物获取溯源监测点的风向、风速和污染物浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceMonitorPointOnlineData(String monitortime, Set<String> airmns, Set<String> othermns, String pollutantcode, List<Map<String, Object>> mns);

    /**
     * @author: xsm
     * @date: 2019/8/31 11:49
     * @Description: 根据自定义参数获取溯源监测点污染物监测值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceMonitorPointPollutantOnlineDataByParamMap(String dgimn, List<Map<String, Object>> pollutants, String monitortime);

    /**
     * @author: xsm
     * @date: 2019/8/31 14:36
     * @Description: 根据自定义参数获取单个溯源监测点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceMonitorPointPollutantInfoByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/03 0003 下午 5:14
     * @Description: 获取某时间点的点位dgimn和风向风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMonitorPointDgimnAndWindDataByMonitortime(String monitortime);

    List<Map<String, Object>> getSensitivePointDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/27 0027 下午 4:30
     * @Description: 获取某个监测点在某段时间内的主导风向（小时）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMonitorPointLeadingWindDirectionDataByParamMap(String starttime, String endtime, String dgimn);

    /**
     * @author: xsm
     * @date: 2019/9/27 0027 下午 4:30
     * @Description: 获取某监测点在某段时间内的风向和OU浓度值（小时）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMonitorPointWindDirectionAndOUDataByParamMap(String starttime, String endtime, String dgimn);

    /**
     * @author: xsm
     * @date: 2019/11/12 0012 下午 6:23
     * @Description: 获取溯源监测点在某个时间段内各污染物预警、超限、异常的条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countTraceSourcePointPollutantAlarmNumByParamMap(String dgimn, List<Map<String, Object>> pollutants, String starttime, String endtime);

    List<Map<String,Object>> getOthorPointInfoByPointType(Map<String, Object> paramMap);

    List<Map<String,Object>> getTraceSourceMonitorPointInfoByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getMinuteMonitorDataAndWeatherData(String monitortime, Set<String> airmns, Set<String> othermns, String pollutantcode, List<Map<String, Object>> mns, List<Map<String, Object>> colorDataList);

    /**
     * @author: xsm
     * @date: 2022/04/13 18:39
     * @Description: 获取溯源列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getEntTraceSourceListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/14 09:00
     * @Description: 获取溯源详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getEntTraceSourceDetailDataByParamMap(Map<String, Object> paramMap);
}
