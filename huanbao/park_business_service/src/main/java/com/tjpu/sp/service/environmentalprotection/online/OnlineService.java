package com.tjpu.sp.service.environmentalprotection.online;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import net.sf.json.JSONObject;
import org.bson.Document;

import java.text.ParseException;
import java.util.*;

public interface OnlineService {


    /**
     * @author: zhangzc
     * @date: 2019/5/27 13:12
     * @Description: 获取查询条件
     * @param: Type 1 为污染源 2 为 监测点
     * @return:
     */
    Map<String, Object> getQueryDataForRealTime(Integer type);

    /**
     * @author: zhangzc
     * @date: 2019/6/11 9:41
     * @Description: 获取数据预警及超标报警的查询条件
     * @param:
     * @return:
     */
    Map<String, Object> getQueryCriteriaForEarlyAndOverAlarm(Integer type);


    /**
     * @author: zhangzc
     * @date: 2019/5/28 14:05
     * @Description: 数据报表表头获取
     * @param: reportType  1 为实时数据一览不带污染源名称 2 为数据报表带污染源名称 ；     pointType 1 为排口 2 为监测点 ； polltantType 点位业务类型用于获取污染物 1 废水 2 废气 3 雨水
     * @return:
     */
    List<Map<String, Object>> getTableTitleForReport(Map<String, Object> titleMap);

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 4:17
     * @Description: 自定义查询条件查询mongodb监测数据数据信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getMonitorDataByParamMap(Map<String, Object> paramMap);


    List<Document> getExceptionModelData(Map<String, Object> paramMap);

    List<Document> getOverModelData(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/28 9:20
     * @Description: 获取排口或者监测点最新监测数据
     * @param: outPutType 排口类型
     * @return:
     */
    Map<String, Object> getOutPutLastDatasByParamMap(List<Map<String, Object>> onlineWaterOutPuts, Integer pointType, Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 3:11
     * @Description: 列表数据获取
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTableListDataForReport(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/5/28 0028 下午 5:06
     * @Description: 获取分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getPageMapForReport(Map<String, Object> paramMap);

    Map<String, Object> get24HourMonitorDataByPollutantCodeForMonitorPoint(String code, Integer monitorPointType, String starttime, String endtime,String userid);

    /**
     * @author: zhangzc
     * @date: 2019/6/11 10:41
     * @Description: 获取废水数据预警和超标报警数据
     * @param:
     * @return:
     */
    Map<String, Object> getPollutantEarlyAndOverAlarmsByParamMap(Integer pageNum, Integer pageSize, List<Map<String, Object>> outputs, Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 7:54
     * @Description: 根据统计类型获取污染物超标预警异常详情表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(boolean isoverstandard,Integer counttype, List<String> pollutantcodes, Integer pointtype);

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 7:54
     * @Description: 根据统计类型获取污染物超标预警异常详情表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutantEarlyAndOverAlarmsTableListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:48
     * @Description: 自定义查询条件统计预警超标数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> countEarlyAndOverDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:42
     * @Description: 获取预警表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getEarlyTableTitleDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:43
     * @Description: 获取预警表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getEarlyTableListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:42
     * @Description: 获取超标表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOverTableTitleDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 3:43
     * @Description: 获取超标表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOverTableListDataByParamMap(Map<String, Object> paramMap);


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:17
     * @Description: 通过排口数组获取mn号数组，设置mn号和排口id对照的map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: outputids：排口id数组，outPutIdAndMn：（outputid+mn）,monitorPointType：监测点类型
     * @return:
     */
    List<String> getMNsAndSetOutPutIdAndMnByOutPutIds(List<String> outputids, Integer monitorPointType,
                                                      Map<String, String> outPutIdAndMn);

    /**
     * @author: lip
     * @date: 2019/8/19 0019 下午 5:03
     * @Description: 获取mn号和污染源对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, String> getMNAndPollution(List<String> outputids, int monitorPointType);

    /**
     * @author: lip
     * @date: 2019/6/25 0025 上午 8:57
     * @Description: 自定义查询条件获取空气监测数据（小时，日）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getAirMonitorDataByParamMap(Map<String, Object> paramMap);

//    Map<String, Object> getAirPageMapForReport(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 3:14
     * @Description: 自定义查询条件获取小时排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getHourFlowMonitorDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/26 0026 上午 8:38
     * @Description: 自定义查询条件获取日排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getDayFlowMonitorDataByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getKeyPollutantMonitorDataByParamMap(List<Map<String, Object>> outPuts, int monitorPointTypeCode, Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/2 0002 下午 4:19
     * @Description: 自定义查询条件获取污染物突增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getPollutantUpRushDataByParams(List<Map<String, Object>> outPuts, int monitorPointTypeCode, Map<String, Object> paramMap) throws Exception;

    /**
     * @author: lip
     * @date: 2019/7/5 0005 上午 9:45
     * @Description: 自定义查询条件查询废气相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getGasRelationListDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/5 0005 下午 2:42
     * @Description: 自定义查询条件查询废气相关性图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getGasRelationChartDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap);


//    /**
//     * @author: chengzq
//     * @date: 2019/7/9 0009 下午 6:33
//     * @Description: 多参数获取浓度突增列表信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [list, starttime, endtime, monitortype, pkid]
//     * @throws:
//     */
//    List<Map<String, Object>> getChangeWarnListByParams(List<Map<String, Object>> list, String starttime, String endtime, Integer monitortype, String pkid, String userid);

//    /**
//     * @author: chengzq
//     * @date: 2019/7/9 0009 下午 6:33
//     * @Description: 多参数获取浓度突增详情
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [list, starttime, endtime, monitortype, pkid]
//     * @throws:
//     */
//    List<OnlineDataVO> getChangeWarnDetailByParams(List<Map<String, Object>> list, String starttime, String endtime, Integer monitortype, String pkid, String DGIMN);

    /**
     * @author: chengzq
     * @date: 2019/7/10 0010 下午 3:17
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list, starttime, endtime, monitortype, pkid, DGIMN]
     * @throws:
     */
//    List<Map<String, Object>> getChangeWarnPollutantInfo(List<Map<String, Object>> list, String starttime, String endtime, Integer monitortype, String pkid, String DGIMN);


    /**
     * @author: chengzq
     * @date: 2019/7/10 0010 下午 4:56
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list, starttime, endtime, monitortype, pkid, DGIMN, Pollutantcode]
     * @throws:
     */
//    List<Map<String, Object>> getOnePollutantChangeWarnByParams(List<Map<String, Object>> list, String starttime, String endtime, Integer monitortype, String pkid, String DGIMN, String Pollutantcode);


//    /**
//     * @author: zhangzc
//     * @date: 2019/7/11 16:35
//     * @Description: 排放量突增污染物信息
//     * @param:
//     * @return:
//     */
//    List<Map<String, Object>> getDischargeUpRushPollutantInfo(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 下午 7:16
     * @Description: 根据自定义参数获取废气排口浓度阈值预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String, Object> getConcentrationThresholdEarlyListData(Map<String, Object> paramMap, String starttime, String endtime, List<String> datatypes, Integer monitorpointtype, String usercode, Integer pagenum, Integer pagesize);

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 1:15
     * @Description: 根据监测点类型和自定义参数获取各企业下各排口监测的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAllOutputDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 2:08
     * @Description: 根据监测点类型和自定义参数获取异常报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String, Object> getExceptionListDataByParam(Map<String, Object> paramMap, String starttime, String endtime, List<String> datatypes, List<String> exceptiontype, Integer monitorpointtype, String usercode, Integer pagenum, Integer pagesize);

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 下午 3:45
     * @Description: 根据监测点类型和自定义参数获取超标报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String, Object> getOverListDataByParam(Map<String, Object> paramMap, String starttime, String endtime, List<String> datatypes, Integer monitorpointtype, String usercode, Integer pagenum, Integer pagesize);


    /**
     * @author: zhangzc
     * @date: 2019/7/11 17:30
     * @Description: 获取单个突增污染物排放信息
     * @param:
     * @return:
     */
    Map<String, Object> getPollutantUpRushDischargeInfo(String starttimeInfo, String endtimeInfo, int remindtype, String mn, String pollutantCode,Integer collectiontype);

    /**
     * @author: zhangzc
     * @date: 2019/7/12 16:19
     * @Description: 条件查询排口下各个污染物排放量信息列表
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAbruptPollutantsDischargeInfoByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/13 0013 下午 3:45
     * @Description: 根据监测点类型获取异常报警表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getTableTitleForException(Integer monitorpointtype);

    /**
     * @author: xsm
     * @date: 2019/7/13 0013 下午 3:56
     * @Description: 根据监测点类型获取超限报警表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getTableTitleForOverAlarm(Integer monitorpointtype);

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/16 16:24
//     * @Description: 获取排口下突出污染物时间数组
//     * @param:
//     * @return:
//     */
//    List<String> getOutPutAbruptTimeByParam(Map<String, Object> paramMap) throws ParseException;


    /**
     * @author: zhangzc
     * @date: 2019/7/26 11:04
     * @Description: 根据mns、污染物code 以及时间获取排放量和浓度在此时间内的变化趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Map<String, Object>> countDischargeAndDensityByCodeAndMns(String pollutantcode, String starttime, String endtime, String pollutionid, Integer monitorType);

    /**
     * @author: zhangzc
     * @date: 2019/7/26 17:34
     * @Description: 根据企业ID获取其排口信息以及主要污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOutPutAndPollutantsByPollutionID(String pollutionid, List<CommonTypeEnum.MonitorPointTypeEnum> enums);


//    /**
//     * @author: zhangzc
//     * @date: 2019/7/30 14:41
//     * @Description: 获取排放量和浓度突增信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    List<Map<String, Object>> getAbruptChangeInfoByParam(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/30 0030 上午 11:44
     * @Description: 自定义查询条件获取污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Set<Map<String, Object>> getPollutantSetDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/30 0030 下午 1:20
     * @Description: 自定义查询条件mongodb最新数据集合中的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getMongodbLatestDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/7/30 19:06
     * @Description: 获取突增污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getUpRushPollutantInfo(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/7/30 19:48
     * @Description: 条件查询浓度和排放量突增的排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMonitorAndPollutants(Map<String, Object> paramMap);

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/31 14:57
//     * @Description: 浓度排放量突变预警污染物信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    List<Map<String, Object>> getAbruptChangePollutantsByParam(Map<String, Object> paramMapInfo);

    /**
     * @author: zhangzc
     * @date: 2019/8/2 13:32
     * @Description: 根据时间、监测点类型和污染物Code统计该污染物排放排放数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Map<String, Object>> countDischargeByParam(Map<String, Object> paramMap);

//    /**
//     * @author: zhangzc
//     * @date: 2019/8/2 13:49
//     * @Description: app已读操作
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    void UpdateDataToReadedForAppByParam(List<Map<String, Object>> listData);

    /**
     * @author: xsm
     * @date: 2019/8/02 9:19
     * @Description: 根据自定义参数统计恶臭监测点在线浓度和风向、风速数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> countStenchOnlineDataAndWeatherDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/07 3:34
     * @Description: 根据自定义参数获取监测点点位相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMonitorPointRelationByParamMap(List<Map<String, Object>> listdatas, List<Map<String, Object>> comparelistdatas, Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/07 3:34
     * @Description: 根据自定义参数获取监测点点位相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getMonitorPointRelationChartDataByParamMap(Map<String, Object> paramMap);
    /**
     * @author: mmt
     * @date: 2022/9/16 3:34
     * @Description: 根据自定义参数获取监测点点位相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
     Map<String, Object> getMonitorPointRelationTableDataByParamMap(Map<String, Object> paramMap);
    /**
     * @author: xsm
     * @date: 2019/8/09 10:47
     * @Description: 根据站点MN号获取最新一条站点小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAirStationLatelyHourDataByAirMn(String airmn);

    /**
     * @author: zhangzc
     * @date: 2019/8/9 15:35
     * @Description: 污染大户排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> countEntDischargeRankByParam(String pollutantCode, Date start, Date end, List<Integer> monitorPointTypes);

    /**
     * @author: zhangzc
     * @date: 2019/8/10 16:01
     * @Description: 根据监测点类型获取重点污染物排放量情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getKeyPollutionsDischargeByMonitorPointType(Date start, Date end, Integer monitorPointType, Integer wastewatertype);

    /**
     * @author: zhangzc
     * @date: 2019/8/12 15:39
     * @Description: 根据监测点类型获取监测点信息以及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOutPutsAndPollutantsByParam(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 1:09
     * @Description: 获取排口ID和污染源对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, String> getOutPutIdAndPollution(List<String> outputids, int monitorPointTypeCode);

    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 1:19
     * @Description: 获取污染物code和name对照关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, String> getPollutantCodeAndName(List<String> pollutantcodes, int monitorPointTypeCode);


    List<Map<String, Object>> getGasMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getAirMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getRainMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getStinkMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getVOCMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getFactorySmallStationMNSByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getFactoryStinkMNSByParam(Map<String, Object> paramMap);

    List<Document> getEarlyOrOverOrExceptionDataByParamMap(Map<String, Object> paramMap);

    Map<String, String> getMNAndMonitorPoint(List<String> outputids, Integer type);

    Map<String, Object> getOnlineDataGroupMmAndMonitortime(Map<String, Object> paramMap) throws ParseException;

    Map<String, String> getMNAndPollutionId(List<String> outputids, Integer type);
    Map<String, String> getMNAndPId(List<String> outputids, Integer type);

//    List<Document> getMonitorGroupDataByParamMap(Map<String, Object> paramMap);

    Map<String, String> getMNAndMonitorPointName(List<String> outputids, Integer type);

    Map<String, Object> countOnlineDataGroupMmAndMonitortime(Map<String, Object> paramMap) throws ParseException;

    /**
     * @author: zhangzc
     * @date: 2019/8/27 16:43
     * @Description: 获取气象相关监测点mn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getMnForWeatherByParam(Integer monitorPointType);

    /**
     * @author: xsm
     * @date: 2019/8/28 11:30
     * @Description: 获取某点位污染物的预警、异常、超限标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getPollutantEarlyAlarmStandardDataByParamMap(Map<String, Object> param);

    Map<String, String> getMNAndMonitorPointId(List<String> objects, Integer type);

    List<Map<String,Object>> getMonitorPointDataByParam(Map<String,Object> paramMap);

    List<Map<String, Object>> countMonitorPointEarlyAndOverDataByParamMap(String starttime, String endtime, List<String> dgimns);

    /**
     * @author: chengzq
     * @date: 2019/9/11 0011 下午 2:39
     * @Description: 通过自定义参数获取已读用户列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> getReadUserIdsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 下午 1:25
     * @Description: 缓存报警提醒信息到redis中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [mqmessage, sessionid]
     * @throws:
     */
    Object cacheAlarmRemindDataInRedis(String mqmessage, String userId,List<JSONObject> objectList) throws ParseException;


    /**
     * @author: xsm
     * @date: 2019/9/24 0024 下午 4:15
     * @Description: 根据自定义参数筛选符合相似度条件的厂界恶臭信息（日）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getEntStinkHourRelationDataByParamMap(String starttime, String endtime, String pollutantcode, List<String> dgimns);

    /**
     * @author: xsm
     * @date: 2019/9/24 0024 下午 4:15
     * @Description: 根据自定义参数筛选符合相似度条件的厂界恶臭信息（周）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getEntStinkRelationDataGroupByWeekDateByParamMap(String starttime, String endtime, String pollutantcode, List<String> dgimns);

    /**
     * @author: xsm
     * @date: 2019/9/24 0024 下午 4:15
     * @Description: 根据自定义参数筛选符合相似度条件的厂界恶臭信息（月）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getEntStinkDayRelationDataByParamMap(String starttime, String endtime, String pollutantcode, List<String> dgimns);

    /**
     *
     * @author: lip
     * @date: 2019/9/30 0030 上午 10:43
     * @Description: 自定义查询条件获取分组监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> getAggregationMonitorDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getOnlineContinuityDataGroupMmAndMonitortime(Map<String, Object> paramMap) throws ParseException;

    /**
     *
     * @author: xsm
     * @date: 2019/10/28 0028 下午 5:24
     * @Description: 自定义查询条件获取分组监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getTableTitleForConcentrationContinuity(Integer remindtype, Integer monitorpointtype, List<Integer> monitortypes);


    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 6:36
     * @Description: 自定义参数查询分钟浓度在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> getMinuteDataByParams(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 8:00
     * @Description:  自定义参数查询小时排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> getHourFlowDataByParams(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/11/2 0002 上午 10:53
     * @Description: 自定义查询条件获取设备状态表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getDeviceStatusDataByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/11/4 0004 上午 11:33
     * @Description: 自定义查询条件获取重点监控污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getKeyPollutantsByParam(Map<String, Object> paramTemp);

    /**
     *
     * @author: lip
     * @date: 2019/11/15 0015 上午 9:11
     * @Description: 自定义查询条件获取水质站点数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getWaterStationMNSByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/11/19 0019 下午 4:43
     * @Description: 自定义查询条件获取水质评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> getWaterEvaluateDataByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: xsm
     * @date: 2019/11/22 0022 上午 9:00
     * @Description: 获取废水、废气、雨水、厂界恶臭和环境恶臭所有点位MN号和基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getPollutionOutputMnAndStinkMonitorPointMn(Map<String,Object> ParamMap);

    /**
     *
     * @author: lip
     * @date: 2019/11/25 0025 上午 9:33
     * @Description: 自定义查询获取实时数据最新的一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> getLatestRealTimeDataByParamMap(Map<String, Object> paramMap);


    Map<String, Object> getPollutantAlarmTypes(String dgimn, List<String> pollutantcodes, String ymd);

    List<Document> getLastHourDataByParamMap(Map<String, Object> paramMap);


    List<Map<String,Object>> getOutPutsAndPollutantAlarmSetByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/1/9 0009 下午 3:36
     * @Description: 获取最新两条实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map> getLatestTwoRealTimeDataByParamMap(Map<String, Object> paramMap);


    List<Map<String,Object>> getAllPollutionOutputDgimnInfoByParam(Map<String, Object> paramMap);

    List<Map> getWaterStationOnlineDataByParamMap(Map<String, Object> paramMap);

    List<Map> getGroundWaterOnlineDataByParamMap(Map<String, Object> paramMap);

    List<Map> getOverDataByParamMap(Map<String, Object> paramMap);

    List<Map> getExceptionDataByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getAlarmsDataByParamMap(Map<String, Object> paramMap) throws ParseException;

    List<Map<String, Object>> getPollutantSuddenChangeDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/12 0012 下午 14:56
     * @Description: 获取废水无流量异常列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> getWasteWaterNoFlowExceptionListDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/12 0012 下午 14:57
     * @Description: 统计无流量异常和非无流量异常类型的数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> countNoFlowAndOtherExceptionTypeDataNum(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/5/6 0006 上午 11:10
     * @Description: 自定义查询条件获取mn和点位关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String, Object>> getMNAndMonitorPointByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2020/5/6 0006 下午 2:38
     * @Description: 获取最新一条监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */


    List<Document> getLastEarlyOrOverOrExceptionDataByParamMap(Map<String, Object> paramMap);

    List<Document> getLastChangeDataByParamMap(Map<String, Object> paramMap);

    List<Document> getUnWindMonitorDataByParamMap(Map<String, Object> paramMap);

    Map<String, Map<String, String>> getMNAndShortName(List<String> outputids, int monitorPointType);

    List<Document> getExceptoinModelDataByParamMap(Map<String, Object> paramMap);

    List<Document> countExceptoinModelDataByParamMap(Map<String, Object> paramMap);

    List<Document> countDayExceptoinModelDataByParamMap(Map<String, Object> paramMap);


    List<Document> getExceptionDetailDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getExceptionPageMap(Map<String, Object> paramMap);

    Map<String,Object> get24HourMonitorDataByPollutantCodeAndMonitorType(String code, Integer monitortype, String starttime, String endtime, String userid);

    Document getOneMonitorPointLastDataByParam(String dgimn, String datatypestr);

    List<Map<String,Object>> getMonitorPointFlowDataByParam(Map<String, Object> paramMap) throws Exception;

    List<Document> getMonitorPointFlowProblemByParam(Map<String, Object> paramMap) throws Exception;

    List<Map> getOnlineConcentrationFlowChangeData(Integer remind, List<String> mns, Date startDate, Date endDate, List<Integer> monitorpointtypes);

    Map<String, Integer> countEntPointEarlyAndOverDataByParamMap(Date startDate, Date endDate, List<String> dgimns,List<Integer> reminds,Map<String, Integer> mnAndType);

    Map<String,Object> getSmokePollutantUpRushDischargeInfo(String starttime, String endtime, Integer remindtype, String dgimn, Map<String,Integer> codeAndIs,Integer collectiontype);

    List<Map<String,Object>> getStinkPointStateAndPollutantsByParam(Map<String, Object> paramMap);

    Map<String,Object> getVocRelationListDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap);

    Map<String,Object> getVocAndGasRelationChartDataByParamMap(List<Map<String, Object>> outPuts, Map<String, Object> paramMap);

    List<String> getMNsAndSetStinkIdAndMnByCategorys(Map<String, String> outPutIdAndMn, List<Integer> monitorpointcategorys);

    List<Map<String,Object>> getMonitorPointRTSPDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWaterFlowInfoByParamsGroupbyTime(Map<String,Object> paramMap);
    List<Map<String, Object>> getFlowInfoByParamsGroupbyTime(Map<String,Object> paramsMap);

    List<Document> getMinuteLastChangeDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> setIntegrationAlarmData(Integer remindCode, Map<String, Object> paramMap);

    List<Map<String,Object>> getSecurityRiskAreaMonitorPointMNSByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getStorageTankAreaMNSByParam(Map<String, Object> paramMap);

    List<Document> getChangeAlarmDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEarlyOverOrExceptionListDataByParams(Map<String, Object> paramMap);

    Map<String,Object> getHierarchicalEarlyDetailDataByParams(Map<String, Object> paramMap, List<String> datatypes, Integer pagenum, Integer pagesize);

    List<Map<String,Object>> getTableTitleForEarlyOverOrException(Integer remindtype, Integer monitorpointtype, List<Integer> monitortypes);

    Map<String,Object> countGasOnlineDataGroupMmAndMonitortime(JSONObject paramMap) throws ParseException;

    Map<String, Object> getPWOutPutLastDataList(Map<String, Object> paramMap);

    List<Map<String,Object>> getOtherPointInfoAndPollutantsByParam(Map<String, Object> paramMap);

    Map<String,Object> getOtherPollutantEarlyAndOverAlarmsByParamMap(Integer pageNum, Integer pageSize, List<Map<String, Object>> outputs, Map<String, Object> paramMap);

    /**
     * @author: mmt
     * @date: 2022/9/19 0007 下午 4:38
     * @Description: 自定义查询条件查询管网监测点位同污水处理厂进口超标相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getWaterPipeNetworkPointRelationTableDataByParams(HashMap<String, Object> paramMap);

    List<Map<String, Object>> countEntDayDischargeRankByParam(String pollutantCode, Date start, Date end, List<Integer> monitorPointTypes);

}
