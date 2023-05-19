package com.tjpu.sp.service.environmentalprotection.online;

import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import org.bson.Document;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.*;

public interface OnlineCountAlarmService {
    /**
     * @author: zhangzc
     * @date: 2019/8/15 18:05
     * @Description: 获取小时数据的报警个数(根据mn号和时间来统计 时间是天统计)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    int countNDAndPFLAlarmNumInHourData(@NotNull OnlineAlarmCountQueryVO queryVO);

    /**
     * @author: zhangzc
     * @date: 2019/8/15 18:05
     * @Description: 异常超阈值超限统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    int countOtherLAlarmNum(@NotNull OnlineAlarmCountQueryVO queryVO);

    List<Document> countOtherAlarmDataByMN(OnlineAlarmCountQueryVO queryVO);

    List<Document> countNDAndPFLAlarmDataByMN(OnlineAlarmCountQueryVO queryVO);

    List<Document> getOtherAlarmHourTimes(OnlineAlarmCountQueryVO queryVO);

    List<Document> getNDAndPFLAlarmHourTimes(OnlineAlarmCountQueryVO queryVO);

    List<Document> countNDAndPFLAlarmDataByPollutantCode(OnlineAlarmCountQueryVO queryVO);

    List<Document> countOtherAlarmDataByPollutantCode(OnlineAlarmCountQueryVO queryVO);

    List<Document> countNDAndPFLAlarmDataByPollutantCode2(OnlineAlarmCountQueryVO queryVO, String timeStyle);

    List<Document> countOtherAlarmDataByPollutantCode2(OnlineAlarmCountQueryVO queryVO, String timeStyle);

    List<Document> countNDAndPFLAlarmDataByPollutantCode3(OnlineAlarmCountQueryVO queryVO, String timeStyle);

    List<Document> countOtherAlarmDataByPollutantCode3(OnlineAlarmCountQueryVO queryVO, String timeStyle);


    List<Document> countNDAndPFLAlarmNumByTimeType(OnlineAlarmCountQueryVO queryVO, String timeType);

    List<Document> countOtherLAlarmNumByTimeType(OnlineAlarmCountQueryVO queryVO, String timeType);

    List<Document> countAlarmDataByMnAndPollutantCode(OnlineAlarmCountQueryVO queryVO);

    List<Map> countConcentrationChangeDataByParamMap(Map<String,Object> paramMap) throws ParseException;

    List<Map> countFlowChangeDataByParamMap(Map<String,Object> paramMap) throws ParseException;

    List<Map> countEarlyWarnDataByParamMap(Map<String,Object> paramMap) throws ParseException;

    List<Map> countOverDataDataByParamMap(Map<String,Object> paramMap) throws ParseException;

    List<Map> countExceptionDatayParamMap(Map<String,Object> paramMap) throws ParseException;

    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 4:56
     * @Description: 统计最近一个月企业报警数目
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> countLastMonthAlarmByParamMap(Map<String,Object> paramMap) throws ParseException;

    /**
     *
     * @author: lip
     * @date: 2019/11/2 0002 上午 11:22
     * @Description: 自定义条件根据mn号统计报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> countAlarmDataForMnByParam(Map<String, Object> paramMap);

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/2 15:39
     * @Description: 获取报警的MN号
     * @param:
     * @return:
     * @throws:
     */
    List<String> getAlarmMnsByParams(OnlineAlarmCountQueryVO queryVO);
    /**
     * @author: zhangzhenchao
     * @date: 2019/11/2 15:39
     * @Description: 条件查询获取报警未读mn个数
     * @param:
     * @return:
     * @throws:
     */
    int getAlarmNumByParams(OnlineAlarmCountQueryVO queryVO);

    /**
     *
     * @author: lip
     * @date: 2019/11/6 0006 下午 7:20
     * @Description: 自定义条件根据mn号、污染物、最新时间等报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> countPollutantMaxTimeByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/11/6 0006 上午 10:52
     * @Description: 统计预警，异常个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> countAlarmsDataByParamMap(Map<String, Object> paramMap) throws ParseException;

    List<Map> countSecurityAlarmTypeDataByParamMap(Map<String, Object> paramMap) throws ParseException;

    /**
     *
     * @author: xsm
     * @date: 2019/11/06 0006 下午 3:48
     * @Description: 统计企业预警、超限、异常等报警类型的月报警条数和上月对比信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> countAlarmMonthDataByParamMap(String preStartTime, String endtime, List<String> mns);

    /**
     * @author: xsm
     * @date: 2019/11/07 0007 上午 11:41
     * @Description: 统计企业下主要污染物的总排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getKeyPollutantYearFlowData(String starttime, String endtime, List<String> mns, List<String> pollutants, Map<String, Object> parammap);

    List<Document> countConcentrationAndFlowChangeNumGroupByMNAndTime(OnlineAlarmCountQueryVO queryVO, String timeType);

    List<Document> countEarlyOrOverAlarmNumGroupByMNAndTime(OnlineAlarmCountQueryVO queryVO, String timeStyle);


    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 09:26
     * @Description: 根据时间范围统计超标报警的点位名称和企业名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getOverAlarmMonitorPointInfoByParam(String starttime, String endtime, List<Map<String, Object>> mndata);

    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 10:56
     * @Description: 统计异常报警各异常类型的报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> countExceptionTypeAlarmData(OnlineAlarmCountQueryVO queryVO, String timeStyle);

    /**
     * @author: chengzq
     * @date: 2019/11/29 0029 上午 10:29
     * @Description:  统计一段时间内预警和报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> countAlarmAndExceptionDataByParamMap(Map<String, Object> paramMap) throws ParseException;

    /**
     * @author: xsm
     * @date: 2019/12/02 0002 下午 3:20
     * @Description:  根据自定义参数获取异常报警各异常类型数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String, Object> getExceptionAlarmDetailDataByParam(Map<String, Object> parammap);

    /**
     * @author: chengzq
     * @date: 2019/12/5 0005 下午 2:18
     * @Description: 统计一段时间内预警和报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> countAlarmAndExceptionByParamMap(Map<String, Object> paramMap) throws ParseException;

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 8:47
     * @Description:  根据自定义参数获取超阈值或超限报警的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getEarlyOrOverAlarmDetailDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 9:09
     * @Description:  根据自定义参数获取浓度突变或排放量突变报警的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getConcentrationOrFlowChangeDetailDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/20 0020 上午 10:58
     * @Description:  根据自定义参数获取单监测类型异常报警各异常类型的详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Document> getExceptionAlarmChildDetailDataByParam(OnlineAlarmCountQueryVO queryVO, String timeStyle);

    /**
     * @author: xsm
     * @date: 2020/03/13 9:53
     * @Description: 获取废水无流量异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getNoFlowExceptionDataNumByParam(OnlineAlarmCountQueryVO queryVO, String timeStyle);

    /**
     * @author: xsm
     * @date: 2020/3/13 0013 下午 15:14
     * @Description: 自定义条件根据mn号统计废水无流量异常报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> countNoFlowExceptionDataForMnByParam(Map<String, Object> paramMap);

    List<Map> countNoFlowExceptionDatayParamMap(Map<String, Object> paramMap) throws ParseException;

    List<Map> countLastMonthAlarmAndExceptionByParamMap(Map<String, Object> paramMap);

    List<Document> countAllAlarmTypeDataNumByDayTime(String daytime,Set<String> dgimns);

    List<Document> countDataGroupByMNByParam(Map<String, Object> paramMap);

    List<Document> countDataGroupByTimeByParam(Map<String, Object> paramMap);

    Map<String, Object> countAlarmsInfoDataByParamMap(Map<String, Object> paramMap) throws ParseException;

    List<Map<String,Long>> countPollutantAlarmsInfoDataByParamMap(Map<String, Object> paramMap) throws ParseException;

    List<Document> countAllExceptionTypeDataNumByDayTime(Date date, Date date1, Set<String> mns, String timeStyle);

    /**
     * @author: xsm
     * @date: 2020/6/02 16:28
     * @Description: 统计某个报警类型各企业点位的报警次数（突变，超标，阈警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map> countChangeAndOverAlarmNumByTimeType(int remind, Set<String> mns, Date date, Date date1);

    /**
     * @author: xsm
     * @date: 2020/6/02 16:28
     * @Description: 统计某个异常类型各企业点位的异常次数（零值异常，恒值异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map> countZeroOrContinuousExceptionNum(String type, Set<String> mns, Date date, Date date1);

    /**
     * @author: xsm
     * @date: 2020/6/15 13:48
     * @Description: 统计某个点位下各污染物的报警次数及最新报警时间（超标，阈警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> countPointChangeAndOverAlarmNumByTimeType(int remind, String mn, Date date, Date date1,Map<String,Object> codeandname) throws ParseException;

    /**
     * @author: xsm
     * @date: 2020/6/15 13:48
     * @Description: 统计某个点位下各污染物的报警次数及最新报警时间（零值异常，恒值异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> countPointExceptionAlarmNumByDayTimeAnd(Date date, Date date1, String mn, String type,Map<String,Object> codeandname) throws ParseException;

    /**
     * @author: xsm
     * @date: 2020/6/15 13:48
     * @Description: 统计某个点位下各污染物的报警次数及最新报警时间（浓度突变，排放量突变合并为突变）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> countPointChangeAlarmNumByTimeType(String mn, Date date, Date date1,Map<String,Object> codeandname) throws ParseException;


    List<Map> getLastMonthPollutionAlarmNum(Integer remind, List<String> mns, Date date, Date date1);

    Collection<? extends Map> getLastMonthEntMonitorPointAlarmDataByParam(Integer remind, List<String> mns, Date startDate,Date endDate,Integer monitorpointtype);

    List<Map> getAlarmPollutantInfoByParamForApp(Integer remind, String dgimn, Date startDate, Date endDate, Integer monitorpointtype);


    List<Document> countChangAlarmDataByPollutantCode(OnlineAlarmCountQueryVO queryVO);

    List<Document> countNDChangeAlarmNumByTimeType(OnlineAlarmCountQueryVO queryVO, String timeType);

    List<Document> countNDChangeLAlarmDataByMN(OnlineAlarmCountQueryVO queryVO);

    Map<String,Object> getChangeAndOverAlarmDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getOnePointAlarmMonitorDataByParam(Map<String, Object> paramMap, Map<String, Object> codeAndName,Map<String, Object> codeAndunit,List<Map<String, Object>> standValueslist)throws ParseException ;

    List<Document> getOneStinkPointOverDataByParam(Map<String, Object> param);

    List<String> getAlarmMnsByParamsForHierarchicalEarly(OnlineAlarmCountQueryVO queryVO, Integer remindType);

    Map<String, Object> getEntPointPollutantMonthFlowDataByParam(Map<String, Object> param);

    Map<String,Object> countEntFlowDataGroupByYearByParam(Map<String, Object> param);

    List<Map<String,Object>> getPointAlarmTimesDataGroupByHourTime(Map<String, Object> paramMap);

    List<Map<String,Object>> getPointAlarmTimesDataGroupByDayTime(Map<String, Object> paramMap);

    List<Map<String,Object>> countOverAlarmPointNumDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllPointOverAlarmDataByParamForHour(Map<String, Object> paramMap);

    List<Map<String,Object>> countAllPointDayOverAlarmTimesDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countAllPointOverAlarmTimesDataByParam(Map<String, Object> paramMap);

    List<Document> countPointAlarmNumDataByParam(String collection, int remind, String starttime, String endtime, Set<String> mns);

    List<Document> countPointAlarmTimesNumDataByParam(String collection, int remind, String starttime, String endtime, Set<String> mns);

    List<Document> getHourOrDayChangeAlarmDataByParamMap(Map<String, Object> paramMap);

    List<Document> getHourOrDayLastAlarmDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> setHourOrDayIntegrationAlarmData(Integer remindCode, Map<String, Object> paramMap);

    List<Map> getOnePointAlarmPollutantDataByParamForApp(String dgimn, Date startDate, Date endDate, Integer monitorpointtype);
}
