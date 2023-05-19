package com.tjpu.sp.service.environmentalprotection.upgradefunction;


import org.bson.Document;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface WallChartOperationService {

    /**
     * @author: xsm
     * @date: 2022/02/15 08:33
     * @Description: 通过自定义参数获取所有点位单个污染物每小时的总排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-dd-mm HH  endtime yyyy-dd-mm HH
     * @return:
     */
    List<Map<String,Object>> countOnePollutantDischargeRankByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的浓度值及浓度排名情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime datatype:数据类型(1:实时/2：分钟/3：小时)
     * @return:
     */
    List<Map<String,Object>> countEnvPointPollutantConcentrationRankByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物最新的浓度值及浓度排名情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime datatype:数据类型(1:实时/2：分钟/3：小时)
     * @return:
     */
    List<Map<String,Object>> countEnvPointPollutantLastDataRankByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getAllPointWindChartData(List<Document> documents, String collection);

    /**
     * @author: xsm
     * @date: 2022/02/16 0016 上午 9:57
     * @Description: 通过自定义条件获取园区恶臭小时在线监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    List<Map<String,Object>> getParkOnlinePollutantHourDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/16 10:44
     * @Description: 根据污染物编码和监测类型获取该类型监测点（恶臭、Voc、扬尘）某时段该污染物小时浓度数据
     * @param: code 污染物编码
     * @return:starttime、endtime yyyy-mm-dd HH
     */
    Map<String,Object> getOtherPointHourMonitorDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时浓度排名和环比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    List<Map<String,Object>> countVocHourMpnitorDataRankByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/17 08:57
     * @Description: 通过自定义参数获取某类型所有监测点某个污染物某日的所有报警时刻
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    List<String> getAllPointPollutanAlarmTimeDataByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/17 11:51
     * @Description: 通过监测类型和污染物获取污染物排放标准
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    List<Map<String,Object>> getPollutanDischargeStandardDataByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/17 13:29
     * @Description: 通过自定义参数获取所有企业单个污染物某小时的总排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:monitortime yyyy-dd-mm HH
     * @return:
     */
    List<Map<String,Object>> countEnvPollutantHourFlowDataByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/17 0017 下午 16:11
     * @Description: 获取一段时间内园区内和园区外污染物浓度趋势对比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime yyyy-mm-dd HH, endtime yyyy-mm-dd HH]
     * @throws:
     */
    List<Map<String,Object>> getParkInAndOutsideAirPollutantDataByParam(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2022/02/18 0018 上午 10:17
     * @Description: 获取水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getWaterQualityStationByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/18 0018 上午 10:17
     * @Description: 获取所有水质级别信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllWaterQualityLevelData();

    /**
     * @author: xsm
     * @date: 2022/2/18 0018 上午 10:31
     * @Description: 统计水质整体达标率情况（小时分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    List<Map<String,Object>> getWaterQualityComplianceDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countAirHourOrDayMonitorDataRankByParam(Map<String, Object> parammap);

    List<Map<String,Object>> countWaterQualityHourMpnitorDataRankByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/24 10:38
     * @Description: 根据监测类型和时间段获取某个点的突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime yyyy-mm-dd HH,endtime yyyy-mm-dd HH
     * @return:
     */
    List<Map<String,Object>> getOnePointChangeWarnPollutantData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/24 10:53
     * @Description: 获取单个点单污染物的浓度突增情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-mm-dd HH   endtime yyyy-mm-dd HH
     * @return:
     */
    Map<String,Object> getOnePointPollutantChangeWarnDataByParams(String starttime, String endtime, String dgimn, String pollutantcode);

    /**
     * @author: xsm
     * @date: 2022/02/24 11:08
     * @Description: 获取单点位单污染物某时间段的浓度突增详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-mm-dd HH  endtime yyyy-mm-dd HH
     * @return:
     */
    List<Map<String,Object>> getOnePointPollutantChangeWarnDetailParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/24 0024 上午 11:28
     * @Description: 通过多参数获取单个点位某时间段的报警时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getPointAlarmTimesDataGroupByTime(Map<String, Object> paramMap)throws ParseException;

    /**
     * @author: xsm
     * @date: 2022/02/28 0028 下午 1:56
     * @Description: 统计单个点位某时段内污染物报警时长占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countPollutantAlarmTimeProportionDataByParam(Map<String, Object> paramMap)throws ParseException;

    /**
     * @author: xsm
     * @date: 2022/03/30 0030 上午 10:52
     * @Description: 获取单个点位最新一条实时数据数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Document> getOnePointLatestRealTimeDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/07 007 下午 4:33
     * @Description: 统计某类型报警点位某日各报警污染物报警时长
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countAllPollutantAlarmTimeByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/18 10:42
     * @Description: 通过自定义参数获取空气所有监测点某个污染物某时间段内所有报警日期（日数据报警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    List<String> getAirAllPointPollutanAlarmDayDataByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/05/17 16:39
     * @Description: 获取废水排口污染物排污企业排名（非污水处理厂）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countEntPollutantDischargeRankByParam(String pollutantCode, String datatype, Date start, Date end, List<Integer> monitorpointtypes);

    /**
     * @author: xsm
     * @date: 2022/05/19 14:58
     * @Description: 通过自定义参数获取非污水厂排口的总排放量情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    List<Map<String,Object>> countGeneralWasteOutPutTotalFlowDataByParam(Map<String, Object> parammap);

    List<Document> getOneAirPointHourDataByParam(Map<String, Object> paramMap);

    List<Document> getOneWaterQualityStationHourDataByParam(Map<String, Object> paramMap);

    List<Document> getOnePointHourDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getDeviceConnectivityData(Map<String, Object> paramMap);

    List<Map<String,Object>> getOutPutExceptionWorkOrderData(Map<String, Object> paramMap);

    List<Map<String,Object>> countEntPointOverAlarmTimesDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getUserMonitorPointRelationDataByUserId(Map<String, Object> paramMap);
}
