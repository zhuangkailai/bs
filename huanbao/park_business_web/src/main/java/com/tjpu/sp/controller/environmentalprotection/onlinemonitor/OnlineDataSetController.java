package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.controller.environmentalprotection.effectivetransmission.EffectiveTransmissionController;
import com.tjpu.sp.model.common.mongodb.BathUpdateOptions;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AlarmLevelDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: lip
 * @date: 2021/3/11 10:57
 * @Description: 在线数据维护处理类
 */
@RestController
@ControllerAdvice
@RequestMapping("onlineDataSet")
public class OnlineDataSetController {

    private final OnlineService onlineService;
    private final OnlineMonitorService onlineMonitorService;
    private final GasOutPutPollutantSetService gasOutPutPollutantSetService;
    private final WaterOutPutPollutantSetService waterOutPutPollutantSetService;
    private final EffectiveTransmissionController effectiveTransmissionController;

    public OnlineDataSetController(OnlineService onlineService, OnlineMonitorService onlineMonitorService, GasOutPutPollutantSetService gasOutPutPollutantSetService, WaterOutPutPollutantSetService waterOutPutPollutantSetService, EffectiveTransmissionController effectiveTransmissionController) {
        this.onlineService = onlineService;
        this.onlineMonitorService = onlineMonitorService;
        this.gasOutPutPollutantSetService = gasOutPutPollutantSetService;
        this.waterOutPutPollutantSetService = waterOutPutPollutantSetService;
        this.effectiveTransmissionController = effectiveTransmissionController;
    }

    private final String DB_HourData = "HourData";
    private final String DB_HourFlowData = "HourFlowData";
    private final String DB_OverData = "OverData";
    private final String DB_EarlyWarnData = "EarlyWarnData";

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: lip
     * @date: 2021/3/11 11:19
     * @Description: 获取小时数据补遗（废水、烟气、重金属、雨水、大气、水质）
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAddendumHourData", method = RequestMethod.POST)
    public Object getAddendumHourData(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            paramMap.put("collection", DB_HourData);
            paramMap.put("pollutantcodes", pollutantcodes);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, Map<String, Object>> timeAndValue = new HashMap<>();
            if (documents.size() > 0) {
                List<Document> pollutantList;
                String pollutantCode;
                String monitortime;
                for (Document document : documents) {
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    pollutantList = document.get("HourDataList", List.class);
                    Map<String, Object> valueMap = new HashMap<>();
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (pollutantcodes.contains(pollutantCode)) {
                            if (StringUtils.isNotBlank(pollutant.getString("AvgStrength"))) {
                                valueMap.put(pollutantCode, pollutant.get("AvgStrength"));
                            } else {

                                valueMap.put(pollutantCode, "");
                            }
                        }
                    }
                    if (valueMap.size() > 0) {
                        timeAndValue.put(monitortime, valueMap);
                    }
                }
            }
            String hourEndTime;
            Date nowDay = new Date();
            if (endtime.equals(DataFormatUtil.getDateYMD(nowDay))) {
                hourEndTime = DataFormatUtil.getDateYMDH(nowDay);
            } else {
                hourEndTime = endtime + " 23";
            }
            List<String> times = DataFormatUtil.getYMDHBetween(starttime + " 00", hourEndTime);
            times.add(hourEndTime);
            Map<String, Object> valueMap;
            for (String time : times) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitortime", time);
                if (timeAndValue.containsKey(time)) {
                    valueMap = timeAndValue.get(time);
                    for (String code : pollutantcodes) {
                        if (valueMap.containsKey(code)) {
                            resultMap.put(code, valueMap.get(code));
                        } else {
                            resultMap.put(code, "");
                        }
                    }
                } else {
                    for (String code : pollutantcodes) {
                        resultMap.put(code, "");
                    }
                }
                resultList.add(resultMap);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/3/11 11:19
     * @Description: 获取小时数据补遗日志（废水、烟气、重金属、雨水、大气、水质）
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAddendumLogData", method = RequestMethod.POST)
    public Object getAddendumLogData(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            paramMap.put("collection", DB_HourData);
            paramMap.put("Addendum", true);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            if (documents.size() > 0) {
                List<Document> pollutantList;
                String pollutantCode;
                String monitortime;
                String addendumTime;
                for (Document document : documents) {
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    if (document.get("AddendumTime") != null) {
                        addendumTime = DataFormatUtil.getDateYMDHMS(document.getDate("AddendumTime"));
                    } else {
                        addendumTime = "";
                    }
                    pollutantList = document.get("HourDataList", List.class);
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitortime", monitortime);
                    resultMap.put("AddendumTime", addendumTime);
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (pollutantcodes.contains(pollutantCode)) {
                            resultMap.put(pollutantCode, pollutant.get("AvgStrength"));
                            if (pollutant.get("Addendum") != null && pollutant.getBoolean("Addendum")) {
                                resultMap.put(pollutantCode + ",Addendum", true);
                            }
                        }
                    }
                    resultList.add(resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2021/3/11 11:19
     * @Description: 恢复小时数据补遗（废水、烟气、重金属、雨水、大气、水质）
     * @param:
     * @return:
     */
    @RequestMapping(value = "recoverAddendumHourData", method = RequestMethod.POST)
    public Object recoverAddendumHourData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid

    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("starttime", monitortime + ":00:00");
            paramMap.put("endtime", monitortime + ":59:59");
            paramMap.put("collection", DB_HourData);
            paramMap.put("Addendum", true);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Document> pollutantList;
            String mnCommon;
            List<Document> newList;
            for (Document document : documents) {
                //更新小时数据
                mnCommon = document.getString("DataGatherCode");
                pollutantList = document.get("HourDataList", List.class);
                newList = new ArrayList<>();
                Query query = setQuery(monitortime, mnCommon);
                for (Document pollutant : pollutantList) {
                    if (pollutant.get("Addendum") != null && pollutant.getBoolean("Addendum")) {
                    } else {
                        newList.add(pollutant);
                    }
                }
                if (newList.size() > 0) {
                    Update update = new Update();
                    update.set("HourDataList", newList);
                    mongoTemplate.updateFirst(query, update, DB_HourData);
                } else {
                    mongoTemplate.remove(query, DB_HourData);
                }
            }


            //删除报警数据
            Query query = setDeleteQuery(monitortime, "OverTime", dgimn);
            mongoTemplate.remove(query, DB_OverData);
            //删除报警数据
            query = setDeleteQuery(monitortime, "EarlyWarnTime", dgimn);
            mongoTemplate.remove(query, DB_EarlyWarnData);
            if (StringUtils.isNotBlank(monitorpointid) && monitorpointtype != null) {  //重新计算传输有效率
                String starttime = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd HH", "yyyy-MM-dd");
                effectiveTransmissionController.supplyOutPutEffectiveTransmissionByParams(Arrays.asList(monitorpointtype), null,
                        monitorpointid, starttime, starttime);
            }

            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/3/11 11:19
     * @Description: 小时数据补遗（废水、烟气、重金属、雨水、大气、水质、恶臭、微站、扬尘、气象）
     * @param:
     * @return:
     */
    @RequestMapping(value = "AddendumHourData", method = RequestMethod.POST)
    public Object AddendumHourData(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "datalist") List<Map<String, Object>> datalist) {
        try {
            if (datalist.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("monitorpointid", monitorpointid);
                List<PollutantSetDataVO> standardDataList = onlineMonitorService.getStandardDataListByParam(paramMap);
                String monitortime;
                Map<String, Object> pollutant;
                List<String> times = new ArrayList<>();
                Map<String, Map<String, Object>> timeAndPollutant = new HashMap<>();
                for (Map<String, Object> dataMap : datalist) {
                    if (dataMap.get("monitortime") != null && dataMap.get("pollutant") != null) {
                        monitortime = dataMap.get("monitortime").toString();
                        times.add(monitortime);
                        pollutant = (Map<String, Object>) dataMap.get("pollutant");
                        timeAndPollutant.put(monitortime, pollutant);
                    }
                }
                if (times.size() > 0) {
                    Collections.sort(times);
                    String starttime = times.get(0) + ":00:00";
                    String endtime = times.get(times.size() - 1) + ":59:59";

                    paramMap.put("mns", Arrays.asList(dgimn));
                    paramMap.put("starttime", starttime);
                    paramMap.put("endtime", endtime);
                    paramMap.put("collection", DB_HourData);
                    List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                    Map<String, Document> timeAndDoc = new HashMap<>();
                    for (Document document : documents) {
                        monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        timeAndDoc.put(monitortime, document);
                    }
                    Document document;
                    List<Document> pollutantList;
                    boolean isHave;
                    String mnCommon;
                    Map<String, Object> overMap;
                    Double value;
                    for (String timeIndex : timeAndPollutant.keySet()) {
                        pollutant = timeAndPollutant.get(timeIndex);
                        if (timeAndDoc.containsKey(timeIndex)) {//存在该时间的数据
                            //更新数据
                            document = timeAndDoc.get(timeIndex);
                            mnCommon = document.getString("DataGatherCode");
                            pollutantList = document.get("HourDataList", List.class);
                            for (String code : pollutant.keySet()) {
                                value = Double.parseDouble(pollutant.get(code).toString());
                                isHave = false;
                                for (Document pollutantIndex : pollutantList) {
                                    if (code.equals(pollutantIndex.get("PollutantCode"))) {//存在该污染物
                                        Update update = new Update();
                                        pollutantIndex.put("Addendum", true);
                                        pollutantIndex.put("AvgStrength", value + "");
                                        pollutantIndex.put("MinStrength", value + "");
                                        pollutantIndex.put("MaxStrength", value + "");
                                        pollutantIndex.put("RepairVal", value + "");
                                        Query query = setQuery(timeIndex, mnCommon);
                                        overMap = setOverMap(standardDataList, code, value);
                                        addOverOrEarlyWarnData(mnCommon, code, timeIndex, overMap, value);
                                        for (String key : overMap.keySet()) {
                                            if (!key.equals("AlarmType")) {
                                                pollutantIndex.put(key, overMap.get(key));
                                            }
                                        }
                                        update.set("HourDataList", pollutantList);
                                        update.set("AddendumTime", new Date());
                                        mongoTemplate.updateFirst(query, update, DB_HourData);

                                        isHave = true;
                                    }
                                }
                                if (!isHave) {//不存在该污染物
                                    if (value != null) {
                                        Update update = new Update();
                                        overMap = setOverMap(standardDataList, code, value);
                                        addOverOrEarlyWarnData(mnCommon, code, timeIndex, overMap, value);
                                        Document pollutantDoc = setPollutantDoc(code, value, overMap);
                                        pollutantList.add(pollutantDoc);
                                        update.set("HourDataList", pollutantList);
                                        update.set("AddendumTime", new Date());
                                        Query query = setQuery(timeIndex, mnCommon);
                                        mongoTemplate.updateFirst(query, update, DB_HourData);
                                    }
                                }
                            }
                        } else {//不存在该时间的数据
                            //添加数据
                            pollutantList = new ArrayList<>();
                            for (String code : pollutant.keySet()) {
                                value = Double.parseDouble(pollutant.get(code).toString());
                                overMap = setOverMap(standardDataList, code, value);
                                addOverOrEarlyWarnData(dgimn, code, timeIndex, overMap, value);
                                Document pollutantDoc = setPollutantDoc(code, value, overMap);
                                pollutantList.add(pollutantDoc);
                            }
                            addHourData(dgimn, timeIndex, pollutantList);
                        }
                    }
                    //小时排放量计算
                    Set<String> timeSet = timeAndPollutant.keySet();
                    updateHourFlowData(timeSet, dgimn, monitorpointtype);
                    if (StringUtils.isNotBlank(monitorpointid) && monitorpointtype != null) {  //重新计算传输有效率
                        starttime = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
                        endtime = DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
                        effectiveTransmissionController.supplyOutPutEffectiveTransmissionByParams(Arrays.asList(monitorpointtype), null,
                                monitorpointid, starttime, endtime);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void updateHourFlowData(Set<String> timeSet, String dgimn, Integer monitorpointtype) {
        List<Map<String, Object>> pollutantSetList;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorpointtype", monitorpointtype);
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteWaterEnum:
                paramMap.put("outputtype", 1);
                pollutantSetList = waterOutPutPollutantSetService.getPollutantSetListByParam(paramMap);
                break;
            case RainEnum:
                // 雨水
                paramMap.put("outputtype", 3);
                pollutantSetList = waterOutPutPollutantSetService.getPollutantSetListByParam(paramMap);
                break;
            case WasteGasEnum:
            case SmokeEnum:
                //重金属、烟气
                pollutantSetList = gasOutPutPollutantSetService.getPollutantSetListByParam(paramMap);
                break;
            default:
                pollutantSetList = new ArrayList<>();
                break;

        }
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData = setMnAndCodeAndSetData(pollutantSetList);
        for (String timeIndex : timeSet) {
             addHourFlowDataList(dgimn, timeIndex, mnAndCodeAndSetData);
        }
    }
    private Map<String, Map<String, Map<String, Object>>> setMnAndCodeAndSetData(List<Map<String, Object>> pollutantSetList) {
        Map<String, Map<String, Object>> codeAndSetData;
        String mnCommon;
        String pollutantCode;
        Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData = new HashMap<>();
        if (pollutantSetList.size() > 0) {
            for (Map<String, Object> pollutantSet : pollutantSetList) {
                mnCommon = pollutantSet.get("dgimn").toString();
                pollutantCode = pollutantSet.get("fk_pollutantcode").toString();
                if (mnAndCodeAndSetData.containsKey(mnCommon)) {
                    codeAndSetData = mnAndCodeAndSetData.get(mnCommon);
                } else {
                    codeAndSetData = new HashMap<>();
                }
                Map<String, Object> setData = new HashMap<>();
                setData.put("ishasconvertdata", pollutantSet.get("ishasconvertdata") != null ?
                        pollutantSet.get("ishasconvertdata") : 0);
                setData.put("pollutantunit", pollutantSet.get("pollutantunit"));
                setData.put("standardmaxvalue", pollutantSet.get("standardmaxvalue"));
                codeAndSetData.put(pollutantCode, setData);
                mnAndCodeAndSetData.put(mnCommon, codeAndSetData);
            }
        }
        return mnAndCodeAndSetData;
    }


    private void addHourFlowDataList(String mnCommon, String monitortime, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData) {
        List<Map<String,Object>> HourFlowDataList = new ArrayList<>();
        //获取流量，根据单位计算排放量
        Double value;
        Date monitorTime = DataFormatUtil.getDateYMDH(monitortime);
        List<AggregationOperation> aggregations = new ArrayList<>();
        UnwindOperation unwindOperation = unwind("HourDataList");
        aggregations.add(unwindOperation);
        Criteria criteria = Criteria.where("DataGatherCode").is(mnCommon).and("MonitorTime").is(monitorTime);
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode")
                .and("PollutantCode", "HourDataList.PollutantCode")
                .and("AvgConvertStrength", "HourDataList.AvgConvertStrength")
                .and("AvgStrength", "HourDataList.AvgStrength");
        aggregations.add(project(fields));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, DB_HourData, Document.class);
        List<Document> documents = results.getMappedResults();
        if (documents.size() > 0) {
            String pollutantCode;

            String flowCode = "b02";
            Double flowValue = 0d;
            Map<String, Map<String, Object>> codeAndSetData = mnAndCodeAndSetData.get(mnCommon);
            Map<String, Double> codeAndValue = new HashMap<>();
            for (Document document : documents) {
                pollutantCode = document.getString("PollutantCode");
                if (flowCode.equals(pollutantCode) && document.get("AvgStrength") != null) {//流量
                    flowValue = Double.parseDouble(document.getString("AvgStrength"));
                } else {
                    if (codeAndSetData != null && codeAndSetData.containsKey(pollutantCode)) {
                        Map<String, Object> setData = codeAndSetData.get(pollutantCode);
                        if (Integer.parseInt(setData.get("ishasconvertdata").toString()) == 1) {
                            if (document.get("AvgConvertStrength") != null) {
                                codeAndValue.put(pollutantCode, Double.parseDouble(document.getString("AvgConvertStrength")));
                            }
                        } else {
                            if (document.get("AvgStrength") != null) {
                                codeAndValue.put(pollutantCode, Double.parseDouble(document.getString("AvgStrength")));
                            }
                        }
                    }
                }
            }
            if (flowValue > 0 && codeAndSetData.containsKey(flowCode)) {
                for (String codeIndex : codeAndValue.keySet()) {
                    if (codeAndSetData.containsKey(codeIndex)) {
                        Map<String,Object> hourData = new HashMap<>();
                        hourData.put("PollutantCode",codeIndex);
                        hourData.put("ChangeMultiple","0");
                        hourData.put("IsSuddenChange",false);
                        //更新内容
                        Double sFlowValue;
                        value = codeAndValue.get(codeIndex);
                        Map<String, Object> setPData = codeAndSetData.get(codeIndex);
                        Map<String, Object> setFData = codeAndSetData.get(flowCode);
                        String pUtil = setPData.get("pollutantunit").toString();
                        String fUtil = setFData.get("pollutantunit").toString();
                        if (fUtil.contains("m³/s") || fUtil.contains("m3/s") || fUtil.contains("l/s")) {
                            sFlowValue = value * flowValue * 3600;
                        } else {
                            sFlowValue = value * flowValue;
                        }
                        if (pUtil.toLowerCase().equals("μg/m³") ||
                                pUtil.toLowerCase().equals("μg/m3") ||
                                pUtil.toLowerCase().equals("μg/l")) {
                            sFlowValue = sFlowValue / 1000000000;

                        } else if (pUtil.toLowerCase().equals("mg/m³") ||
                                pUtil.toLowerCase().equals("mg/m3") ||
                                pUtil.toLowerCase().equals("mg/l")) {
                            sFlowValue = sFlowValue / 1000000;
                        }
                        String valueString = DataFormatUtil.SaveThreeAndSubZero(sFlowValue);
                        hourData.put("MinFlow",valueString);
                        hourData.put("AvgFlow",valueString);
                        hourData.put("MaxFlow",valueString);
                        hourData.put("CorrectedFlow",valueString);
                        HourFlowDataList.add(hourData);
                    }
                }
            }
        }
        if (HourFlowDataList.size()>0){
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
            Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            //根据条件删除
            mongoTemplate.remove(query, DB_HourFlowData);
            //添加记录
            Document document = new Document();
            document.put("DataGatherCode", mnCommon);
            document.put("MonitorTime", DataFormatUtil.getDateYMDH(monitortime));
            document.put("HourFlowDataList", HourFlowDataList);
            mongoTemplate.insert(document, DB_HourFlowData);
        }
    }


    private void addHourData(String dgimn, String timeIndex, List<Document> pollutantList) {
        Document document = new Document();
        document.put("DataGatherCode", dgimn);
        document.put("MonitorTime", DataFormatUtil.getDateYMDH(timeIndex));
        document.put("HourDataList", pollutantList);
        document.put("DataType", "shougong");
        document.put("ReadUserIds", Arrays.asList());
        document.put("WaterLevel", null);
        document.put("AddendumTime", new Date());
        mongoTemplate.insert(document, DB_HourData);

    }

    private void addOverOrEarlyWarnData(String mnCommon, String code, String timeIndex, Map<String, Object> overMap, Double value) {
        //添加数据到OverData
        if (Boolean.parseBoolean(overMap.get("IsOverStandard").toString())
                || Integer.parseInt(overMap.get("IsOver").toString()) > 0) {
            addOverData(mnCommon, code, timeIndex, overMap, value);
        }
        //添加数据到EarlyWarnData
        if (Integer.parseInt(overMap.get("IsOver").toString()) == 0) {
            addEarlyWarnData(mnCommon, code, timeIndex, overMap, value);
        }
    }

    private Document setPollutantDoc(String code, Double value, Map<String, Object> overMap) {
        Document document = new Document();
        document.put("PollutantCode", code);
        document.put("AvgStrength", value + "");
        document.put("MinStrength", value + "");
        document.put("MaxStrength", value + "");
        document.put("CouStrength", "0.0");
        document.put("AvgConvertStrength", null);
        document.put("MinConvertStrength", null);
        document.put("MaxConvertStrength", null);
        document.put("CouConvertStrength", null);
        document.put("Flag", "n");
        document.put("IsOver", overMap.get("IsOver"));
        document.put("IsException", 0);
        document.put("AuditManId", null);
        document.put("AuditTime", null);
        document.put("ExceptRasonId", null);
        document.put("AuditDes", null);
        document.put("RepairTypeId", 0);
        document.put("RepairVal", value + "");
        document.put("State", null);
        document.put("IsOverStandard", overMap.get("IsOverStandard"));
        document.put("OverMultiple", overMap.get("OverMultiple"));
        document.put("IsSuddenChange", false);
        document.put("ChangeMultiple", 0.0);
        document.put("WaterLevel", null);
        document.put("Addendum", true);
        return document;
    }

    /**
     * @author: lip
     * @date: 2020/10/27 0027 上午 11:09
     * @Description: 入库报警数据表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void addOverData(String mnCommon, String code, String timeData, Map<String, Object> standardMap, Double value) {
        Map<String, Object> overMap = new LinkedHashMap<>();
        overMap.put("DataGatherCode", mnCommon);
        overMap.put("PollutantCode", code);
        overMap.put("OverTime", DataFormatUtil.getDateYMDH(timeData));
        overMap.put("DataType", "HourData");
        overMap.put("AlarmType", standardMap.get("AlarmType").toString());
        overMap.put("AlarmLevel", standardMap.get("IsOver"));
        overMap.put("OverMultiple", standardMap.get("OverMultiple"));
        overMap.put("MonitorValue", value + "");
        overMap.put("IsOverStandard", standardMap.get("IsOverStandard"));
        overMap.put("Addendum", true);
        mongoTemplate.insert(overMap, DB_OverData);
    }

    /**
     * @author: lip
     * @date: 2020/10/27 0027 上午 11:01
     * @Description: 入库预警数据表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void addEarlyWarnData(String mnCommon, String code, String timeData, Map<String, Object> standardMap, Double value) {
        Map<String, Object> earlyWarnMap = new LinkedHashMap<>();
        earlyWarnMap.put("DataGatherCode", mnCommon);
        earlyWarnMap.put("PollutantCode", code);
        earlyWarnMap.put("EarlyWarnTime", DataFormatUtil.getDateYMDH(timeData));
        earlyWarnMap.put("DataType", "HourData");
        earlyWarnMap.put("AlarmType", standardMap.get("AlarmType"));
        earlyWarnMap.put("AlarmLevel", standardMap.get("IsOver"));
        earlyWarnMap.put("MonitorValue", value + "");
        earlyWarnMap.put("Addendum", true);
        mongoTemplate.insert(earlyWarnMap, DB_EarlyWarnData);


    }


    private Map<String, Object> setOverMap(List<PollutantSetDataVO> standardDataList, String pollutantcode, Double value) {
        Map<String, Object> overMap = new HashMap<>();
        Integer alarmType;
        Double minValue;
        Double maxValue;
        boolean IsOverStandard = false;
        Double OverMultiple = 0D;
        List<AlarmLevelDataVO> alarmLevelDataVOS;
        Integer level = -1;
        for (PollutantSetDataVO pollutantSet : standardDataList) {
            if (pollutantcode.equals(pollutantSet.getPollutantcode())) {
                alarmType = pollutantSet.getAlarmtype();
                if (alarmType != null) {
                    switch (alarmType) {
                        case 1://上限报警
                            maxValue = pollutantSet.getStandardmaxvalue();
                            if (maxValue != null && value >= maxValue && maxValue > 0) {
                                IsOverStandard = true;
                                OverMultiple = Double.parseDouble(DataFormatUtil.formatDouble("######0.00", value / maxValue));
                            }
                            break;
                        case 2://下限报警
                            minValue = pollutantSet.getStandardminvalue();
                            if (minValue != null && value <= minValue && value > 0) {
                                IsOverStandard = true;
                                if (value > 0) {
                                    OverMultiple = Double.parseDouble(DataFormatUtil.formatDouble("######0.00", minValue / value));
                                }
                            }
                            break;
                        case 3://区间报警
                            maxValue = pollutantSet.getStandardmaxvalue();
                            minValue = pollutantSet.getStandardminvalue();
                            if (minValue != null && value <= minValue && value > 0) {
                                if (value > 0) {
                                    OverMultiple = Double.parseDouble(DataFormatUtil.formatDouble("######0.00", minValue / value));
                                    IsOverStandard = true;
                                }
                            }
                            if (maxValue != null && value >= maxValue && maxValue > 0) {
                                OverMultiple = Double.parseDouble(DataFormatUtil.formatDouble("######0.00", value / maxValue));
                                IsOverStandard = true;
                            }
                    }
                }
                if (IsOverStandard) {
                    overMap.put("IsOver", level);
                    overMap.put("AlarmType", alarmType);
                    overMap.put("IsOverStandard", IsOverStandard);
                    overMap.put("OverMultiple", OverMultiple);
                } else {
                    alarmLevelDataVOS = pollutantSet.getAlarmLevelDataVOList();
                    if (alarmLevelDataVOS.size() > 0 && alarmType != null) {
                        switch (alarmType) {
                            case 1://上限报警
                                for (AlarmLevelDataVO alarmLevelDataVO : alarmLevelDataVOS) {
                                    maxValue = alarmLevelDataVO.getStandardmaxvalue();
                                    if (maxValue != null && value >= maxValue) {
                                        level = level > Integer.parseInt(alarmLevelDataVO.getPkId()) ? level : Integer.parseInt(alarmLevelDataVO.getPkId());
                                    }
                                }
                                break;
                            case 2://下限报警
                                for (AlarmLevelDataVO alarmLevelDataVO : alarmLevelDataVOS) {
                                    minValue = alarmLevelDataVO.getStandardminvalue();
                                    if (minValue != null && value <= minValue) {
                                        level = level > Integer.parseInt(alarmLevelDataVO.getPkId()) ? level : Integer.parseInt(alarmLevelDataVO.getPkId());
                                    }
                                }
                                break;
                            case 3://区间报警
                                for (AlarmLevelDataVO alarmLevelDataVO : alarmLevelDataVOS) {
                                    minValue = alarmLevelDataVO.getStandardminvalue();
                                    maxValue = alarmLevelDataVO.getStandardmaxvalue();
                                    boolean flag = false;
                                    if (minValue != null && value <= minValue) {
                                        flag = true;
                                    }
                                    if (maxValue != null && value >= maxValue) {
                                        flag = true;
                                    }
                                    if (flag) {
                                        level = level > Integer.parseInt(alarmLevelDataVO.getPkId()) ? level : Integer.parseInt(alarmLevelDataVO.getPkId());
                                    }
                                }
                                break;
                        }
                    }
                    overMap.put("AlarmType", alarmType);
                    overMap.put("IsOver", level);
                    overMap.put("IsOverStandard", IsOverStandard);
                    overMap.put("OverMultiple", OverMultiple);
                }
                break;
            }
        }
        if (overMap.size() == 0) {
            overMap.put("AlarmType", -1);
            overMap.put("IsOver", level);
            overMap.put("IsOverStandard", IsOverStandard);
            overMap.put("OverMultiple", OverMultiple);
        }


        return overMap;

    }

    private Query setQuery(String timeIndex, String mnCommon) {
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
        Date startDate = DataFormatUtil.getDateYMDHMS(timeIndex + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(timeIndex + ":59:59");
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        return query;
    }

    private Query setDeleteQuery(String timeIndex, String timeMark, String mnCommon) {
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
        Date startDate = DataFormatUtil.getDateYMDHMS(timeIndex + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(timeIndex + ":59:59");
        query.addCriteria(Criteria.where(timeMark).gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where("Addendum").is(true));
        return query;
    }

}
