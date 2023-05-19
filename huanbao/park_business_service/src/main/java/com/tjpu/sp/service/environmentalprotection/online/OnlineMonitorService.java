package com.tjpu.sp.service.environmentalprotection.online;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import net.sf.json.JSONObject;
import org.bson.Document;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OnlineMonitorService {
    List<Map<String, Object>> getOnlineOutPutListByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getOutPutLastDataByParamMap(List<Map<String, Object>> onlineOutPuts, Integer monitorPointType, Map<String, Object> paramMap);

    List<String> getMNListByParam( Map<String,Object> paramMap);

    List<Document> getMonitorDataByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getPollutantEarlyAlarmStandardDataByParamMap(Map<String, Object> param);

    Map<String, Object> getPageMapForReport(Map<String, Object> paramMap);

    List<Map<String,Object>> getTableTitleForReport(Map<String, Object> titleMap);

    List<Map<String,Object>> getTableListDataForReport(Map<String, Object> paramMap);

    Map<String,String> getPollutantCodeAndName(List<String> pollutantcodes, int monitorpointtype);

    Map<String,String> getOutPutIdAndPollution(List<String> outputids, int monitorpointtype);

    List<Map<String,Object>> getAllMonitorPointLastHourDataByParamMap(List<Map<String, Object>> outPutInfosByParamMap, List<Map<String, Object>> codeAndName,String monitortime);

    List<Map> countOverDataByParams(Map<String, Object> paramMap);

    Map<String,Object> getEarlyAndOverDataGroupMnAndByTime(List<String> mns, List<String> pollutantcodes, Integer datamark, String starttime, String endtime);

    void getAllMonitorPointLastRealTimeData(List<Map<String, Object>> result, Date startdate, Date enddate,String pollutantcode);

    List<Map<String,Object>> getSmokeTableTitleByParam(Map<String, Object> titleMap);

    List<Map<String,Object>> getSmokeTableListDataByParam(Map<String, Object> paramMap);

    List<Document> getOverDataByParam(Map<String, Object> paramMap);

    void getOutPutPointLastOnlineDataByParamForApp(List<Map<String, Object>> result, List<Map<String, Object>> onlineOutPuts, Integer type, String datatype);

    Collection<? extends Map> getLastOutPutPointAlarmDataByParam(Integer remind, List<String> mns, Date startDate, Date endDate, List<Integer> monitorpointtypes,Map<String, Object> mnandtype);

    Collection<? extends Map> countMonitorPointChangeDataByParamMap(Integer remind, List<String> mns, Date startDate, Date endDate, List<Integer> monitorpointtypes);

    List<PollutantSetDataVO> getStandardDataListByParam(Map<String, Object> paramMap);

    List<Document> getAlarmMonitorDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getAllMonitorPointHourTrendDataByParamMap(String code,List<Map<String, Object>> outPutInfosByParamMap, String starttime, String endtime);

    List<String> getAlarmPollutanByAlarmTypeAndTimes(List<String> dgimns, List<String> pollutantcodes, String starttime, String endtime, Integer code,String overflag);

    List<Map<String, Object>> getOnePointAndOnePollutantAllAlarmTimes(Map<String, Object> paramMap, Map<String, String> pollutantmap);

    List<Map<String, Object>> getPointLastDataByParam(List<Map<String, Object>> onlineOutPuts, Map<String, Object> paramMap);

    Map<String,Object> getManyPonitPollutantDataByParam(Map<String, Object> param);

    List<Map<String, Object>> getPollutantListByParam(Map<String, Object> paramMap);

    List<Document> getLastDataByMns(List<String> mns);

    Map<String,Object> getGasMNListByParam(Map<String, Object> paramMap);

    Map<String,String> getPollutantCodeAndNameByTypes(List<String> pollutantcodes, List<Integer> monitorpointtypes);

    List<Map<String,Object>> getGasTableTitleByParam(Map<String, Object> titleMap);

    List<Map<String,Object>> getGasTableListDataByParam(Map<String, Object> paramMap);

    Map<String,Object> getOnlineChangeDataGroupMmAndMonitortime(JSONObject paramMap);


    Map<String, Object> getGasOutPutLastDataByParamMap(List<Map<String, Object>> onlineOutPuts, List<Integer> monitorpointtypes, Map<String, Object> paramMap);

    Map<String,Object> getMonitorDataByParamMapForApp(Map<String, Object> paramMap);

    Map<String, Map<String, Map<String, Object>>> getMnAndCodeAndLevelStandardData(Map<String, Object> paramMap);

    long getMongodbCountByParam(Map<String, Object> paramMap);
}
