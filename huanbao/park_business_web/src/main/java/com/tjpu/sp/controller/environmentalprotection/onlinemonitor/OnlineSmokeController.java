package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.common.mongodb.BathUpdateOptions;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_AQI;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.AirEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @author: lip
 * @date: 2019/5/27 0027 下午 7:53
 * @Description: 在线烟气处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineSmoke")
public class OnlineSmokeController {
    private final GasOutPutPollutantSetService gasOutPutPollutantSetService;
    private final GasOutPutInfoService gasOutPutInfoService;
    private final PollutantService pollutantService;
    private final MongoTemplate mongoTemplate;
    private final PollutantFactorMapper pollutantFactorMapper;
    private RabbitSender rabbitSender;
    private String hourFlowData_db = "HourFlowData";
    private String dayFlowData_db = "DayFlowData";
    private String monthFlowData_db = "MonthFlowData";
    private String yearFlowData_db = "YearFlowData";
    private String hourData_db = "HourData";
    private String dayData_db = "DayData";
    @Autowired
    private PubCodeService pubCodeService;
    private final int monitorPointTypeCode = SmokeEnum.getCode();

    public OnlineSmokeController(GasOutPutPollutantSetService gasOutPutPollutantSetService, GasOutPutInfoService gasOutPutInfoService, PollutantService pollutantService, MongoTemplate mongoTemplate,PollutantFactorMapper pollutantFactorMapper,RabbitSender rabbitSender) {
        this.gasOutPutPollutantSetService = gasOutPutPollutantSetService;
        this.gasOutPutInfoService = gasOutPutInfoService;
        this.pollutantService = pollutantService;
        this.mongoTemplate = mongoTemplate;
        this.pollutantFactorMapper = pollutantFactorMapper;
        this.rabbitSender = rabbitSender;
    }


    /**
     * @Description: 获取审核小时数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/29 10:57
     */
    @RequestMapping(value = "getAuditHourData", method = RequestMethod.POST)
    public Object getAuditHourData(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pagesize") Integer pageSize,
            @RequestJson(value = "pagenum") Integer pageNum,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
            @RequestJson(value = "dgimns") List<String> dgimns
    ) {
        try {

            Map<String, Object> resultListMap = new HashMap<>();

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                paramMap.put("monitorpointtypes", monitorpointtypes);
            } else {
                paramMap.put("monitorpointtype", monitorPointTypeCode);
            }
            paramMap.put("dgimns", dgimns);
            List<Map<String, Object>> pollutantSetList = gasOutPutPollutantSetService.getPollutantSetListByParam(paramMap);
            Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData = new HashMap<>();
            Map<String, Map<String, Object>> codeAndSetData;
            String mnCommon;
            String pollutantCode;
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
                    codeAndSetData.put(pollutantCode, setData);
                    mnAndCodeAndSetData.put(mnCommon, codeAndSetData);
                }
            }
            Date startTime = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            Date endTime = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(dgimns)));
            operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startTime).lte(endTime)));
            Sort.Direction direction = Sort.Direction.DESC;
            PageEntity<Document> pageEntity = new PageEntity<>();
            pageEntity.setPageNum(pageNum);
            pageEntity.setPageSize(pageSize);
            String collection = hourData_db;
            long totalCount = 0;
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
            totalCount = resultsCount.getMappedResults().size();
            pageEntity.setTotalCount(totalCount);
            int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
            pageEntity.setPageCount(pageCount);
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
            operations.add(Aggregation.sort(direction, "MonitorTime"));
            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = pageResults.getMappedResults();
            if (documents.size() > 0) {

                Map<String, Map<String, Map<String, Object>>> mnAndTimeAndDataMap = getMnAndTimeAndDataMap(documents);
                Map<String, Map<String, Object>> codeAndDataMap;

                List<Map<String, String>> pointDataList = gasOutPutInfoService.getMonitorPointAndPollutionInfo(paramMap);

                Map<String, Object> flag_codeAndName = new HashMap<>();

                //获取mongodb的flag标记
                Map<String,Object> f_map = new HashMap<>();
                f_map.put("monitorpointtypes",monitorpointtypes);
                List<Map<String, Object>> flagList = pollutantService.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }
                String monitorTime;
                Object monitorValue;
                List<Document> pollutantList;
                for (Document document : documents) {
                    String DataGatherCode = document.getString("DataGatherCode");
                    monitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("dgimn", DataGatherCode);
                    resultMap.put("monitorTime", monitorTime);
                    pointDataList.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN")))
                            .forEach(m -> {
                                resultMap.put("PollutionName", m.get("PollutionName"));
                                resultMap.put("monitorpointname", m.get("outputname"));
                                resultMap.put("Pollutionid", m.get("Pollutionid"));
                                resultMap.put("monitorpointid", m.get("outputid"));
                            });
                    //浓度数据
                    pollutantList = document.get("HourDataList", List.class);
                    codeAndSetData = mnAndCodeAndSetData.get(DataGatherCode);
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (codeAndSetData != null && codeAndSetData.containsKey(pollutantCode)) {
                            Map<String, Object> dataMap = new HashMap<>();
                            Map<String, Object> setData = codeAndSetData.get(pollutantCode);
                            if (pollutant.get("AuditTime") != null) {//有修约过
                                dataMap.put("isAudit", true);
                                resultMap.put("isAudit", true);
                                resultMap.put("AuditTime", DataFormatUtil.getDateYMDHMS(pollutant.getDate("AuditTime")));
                            } else {
                                dataMap.put("isAudit", false);
                            }
                            dataMap.put("IsOver", pollutant.get("IsOver"));
                            dataMap.put("flag", flag_codeAndName.get(pollutant.get("Flag") != null
                                    ? pollutant.get("Flag").toString().toLowerCase() : ""));
                            dataMap.put("IsException", pollutant.get("IsException"));
                            dataMap.put("IsOverStandard", pollutant.get("IsOverStandard"));
                            if (Integer.parseInt(setData.get("ishasconvertdata").toString()) == 1) {
                                monitorValue = pollutant.get("AvgConvertStrength");
                            } else {
                                monitorValue = pollutant.get("AvgStrength");
                            }
                            dataMap.put("xvalue", monitorValue);
                            dataMap.put("yvalue", pollutant.get("RepairVal"));
                            resultMap.put(pollutantCode + "_ND", dataMap);
                            resultMap.put("AuditDes", pollutant.get("AuditDes"));
                            resultMap.put("AuditManId", pollutant.get("AuditManId"));
                            codeAndDataMap = mnAndTimeAndDataMap.get(DataGatherCode + "," + monitorTime);
                            if (codeAndDataMap != null) {
                                resultMap.put(pollutantCode + "_PFL", codeAndDataMap.get(pollutantCode));
                            }
                        }
                    }
                    resultList.add(resultMap);
                }
            }
            if (resultList.size() > 0) {
                //排序
                resultList = resultList.stream().sorted(Comparator.comparing((Map m) -> m.get("monitorTime").toString()).reversed()
                        .thenComparing((Map m) -> m.get("PollutionName").toString())
                        .thenComparing((Map m) -> m.get("monitorpointname").toString())
                ).collect(Collectors.toList());

            }

            resultListMap.put("total", totalCount);
            resultListMap.put("datalist", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultListMap);
        } catch (
                Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private Map<String, Map<String, Map<String, Object>>> getMnAndTimeAndDataMap(List<Document> documents) {

        List<String> mns = documents.stream().map(m -> m.getString("DataGatherCode")).collect(Collectors.toList());
        List<Date> times = documents.stream().map(m -> m.getDate("MonitorTime")).collect(Collectors.toList());
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        operations.add(Aggregation.match(Criteria.where("MonitorTime").in(times)));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, hourFlowData_db, Document.class);
        List<Document> pfl_documents = pageResults.getMappedResults();
        Map<String, Map<String, Map<String, Object>>> mnAndTimeAndDataMap = new HashMap<>();
        if (pfl_documents.size() > 0) {
            Map<String, Map<String, Object>> timeAndDataMap;
            String mn_time;
            String pollutantCode;
            List<Document> pollutantList;
            for (Document document : pfl_documents) {
                mn_time = document.getString("DataGatherCode") + "," + DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                timeAndDataMap = mnAndTimeAndDataMap.get(mn_time) != null ? mnAndTimeAndDataMap.get(mn_time) : new HashMap<>();
                pollutantList = document.get("HourFlowDataList", List.class);
                for (Document pollutant : pollutantList) {
                    pollutantCode = pollutant.getString("PollutantCode");
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("yflow", pollutant.get("AvgFlow"));
                    dataMap.put("xflow", pollutant.get("CorrectedFlow"));
                    timeAndDataMap.put(pollutantCode, dataMap);
                }
                mnAndTimeAndDataMap.put(mn_time, timeAndDataMap);
            }
        }
        return mnAndTimeAndDataMap;

    }


    /**
     * @Description: 更新烟气小时数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/29 10:57
     */
    @RequestMapping(value = "updateHourData", method = RequestMethod.POST)
    public Object updateHourData(@RequestJson(value = "updateform") Object updateform) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateform);
            if (jsonObject != null) {
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                //获取烟气污染物设置信息
                Map<String, Object> paramMap = new HashMap<>();
                JSONArray monitorpointtypes = jsonObject.getJSONArray("monitorpointtypes");
                if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                    paramMap.put("monitorpointtypes", monitorpointtypes);
                } else {
                    paramMap.put("monitorpointtype", monitorPointTypeCode);
                }
                List<Map<String, Object>> pollutantSetList = gasOutPutPollutantSetService.getPollutantSetListByParam(paramMap);
                Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData = new HashMap<>();
                Map<String, Map<String, Map<Integer, Double>>> mnAndCodeAndEarlyMap = new HashMap<>();
                Map<String, Map<String, Object>> codeAndSetData;
                Map<String, Map<Integer, Double>> codeAndEarlyMap;
                Map<Integer, Double> earlyMap;
                String mnCommon;
                String pollutantCode;
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
                        if (pollutantSet.get("fk_alarmlevelcode") != null && !"".equals(pollutantSet.get("fk_alarmlevelcode").toString())
                                && pollutantSet.get("concenalarmmaxvalue") != null) {
                            if (mnAndCodeAndEarlyMap.containsKey(mnCommon)) {
                                codeAndEarlyMap = mnAndCodeAndEarlyMap.get(mnCommon);
                            } else {
                                codeAndEarlyMap = new HashMap<>();
                            }
                            if (codeAndEarlyMap.containsKey(pollutantCode)) {
                                earlyMap = codeAndEarlyMap.get(pollutantCode);
                            } else {
                                earlyMap = new HashMap<>();
                            }
                            earlyMap.put(Integer.parseInt(pollutantSet.get("fk_alarmlevelcode").toString()),
                                    Double.parseDouble(pollutantSet.get("concenalarmmaxvalue").toString()));
                            codeAndEarlyMap.put(pollutantCode, earlyMap);
                            mnAndCodeAndEarlyMap.put(mnCommon, codeAndEarlyMap);
                        }
                    }
                }
                //修改小时数据

                List<BathUpdateOptions> bathUpdateOptions = new ArrayList<>();
                bathUpdateOptions.addAll(getBathUpdateOptions(jsonObject, mnAndCodeAndSetData, mnAndCodeAndEarlyMap, userid));
                if (bathUpdateOptions.size() > 0) {
                    doBathUpdate(mongoTemplate.getDb().getCollection(hourData_db), bathUpdateOptions);
                }
                bathUpdateOptions.clear();
                //更新小时排放量数据
                bathUpdateOptions.addAll(getFlowBathUpdateOptions(jsonObject, mnAndCodeAndSetData));
                if (bathUpdateOptions.size() > 0) {
                    doBathUpdate(mongoTemplate.getDb().getCollection(hourFlowData_db), bathUpdateOptions);
                }
                //更新日数据
                bathUpdateOptions.clear();
                bathUpdateOptions.addAll(getDayBathUpdateOptions(jsonObject, mnAndCodeAndSetData, mnAndCodeAndEarlyMap, userid));
                if (bathUpdateOptions.size() > 0) {
                    doBathUpdate(mongoTemplate.getDb().getCollection(dayData_db), bathUpdateOptions);
                }
                //更新日排放量数据
                bathUpdateOptions.clear();
                bathUpdateOptions.addAll(getSubFlowBathUpdateOptions("day", jsonObject));
                //更新日排放量数据(小时排放量累加)
                if (bathUpdateOptions.size() > 0) {
                    doBathUpdate(mongoTemplate.getDb().getCollection(dayFlowData_db), bathUpdateOptions);
                }
                bathUpdateOptions.clear();
                bathUpdateOptions.addAll(getSubFlowBathUpdateOptions("month", jsonObject));
                //更新月排放量数据(日排放量累加)
                if (bathUpdateOptions.size() > 0) {
                    doBathUpdate(mongoTemplate.getDb().getCollection(monthFlowData_db), bathUpdateOptions);
                }

                bathUpdateOptions.clear();
                bathUpdateOptions.addAll(getSubFlowBathUpdateOptions("year", jsonObject));
                //更新年排放量数据(月排放量累加)
                if (bathUpdateOptions.size() > 0) {
                    doBathUpdate(mongoTemplate.getDb().getCollection(yearFlowData_db), bathUpdateOptions);
                }
                //发送消息队列，补充其他表的数据
                if (monitorpointtypes != null) {
                    monitorpointtypes.forEach(pointType->{
                        int monitorpointtype = Integer.parseInt(pointType.toString());
                        sendOnlineSupplyDirectQueue(jsonObject.getString("dgimn"),
                                DataFormatUtil.getDateYMDHMS(jsonObject.getString("monitortime")+":00:00"), monitorpointtype);
                    });
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void sendOnlineSupplyDirectQueue(String mn,Date monitortime,Integer monitorpointtype) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("monitorpointtype", monitorpointtype);
            jsonObject.put("dgimn", mn);
            jsonObject.put("sendtime", DataFormatUtil.getDateYMDHMS(new Date()));
            jsonObject.put("instructtype", null);
            jsonObject.put("sendinstruct", null);
            DeleteResult deleteResult;
            if (monitorpointtype == AirEnum.getCode()) {//空气

                jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(monitortime));
                jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(monitortime));
                //AQI站点小时数据
                Query query = new Query();
                query.addCriteria(Criteria.where("MonitorTime").is(monitortime).and("StationCode").is(mn));
                deleteResult = mongoTemplate.remove(query,"StationHourAQIData");
                System.out.println("AQI站点小时数据删除条数："+deleteResult.getDeletedCount());
                jsonObject.put("datatype", "AQI站点小时数据");
                sendOnlineSupplyDirectQueue(jsonObject);
                Thread.sleep(1000);
                //AQI园区小时数据
                query = new Query();
                query.addCriteria(Criteria.where("MonitorTime").is(monitortime));
                deleteResult = mongoTemplate.remove(query,"ParkHourAQIData");
                System.out.println("AQI园区小时数据删除条数："+deleteResult.getDeletedCount());
                jsonObject.put("datatype", "AQI园区小时数据");
                sendOnlineSupplyDirectQueue(jsonObject);
                Thread.sleep(1000);
                if (!DataFormatUtil.getDateYMD(monitortime).equals(DataFormatUtil.getDateYMD(new Date()))) {
                    Date recordTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(monitortime)+" 00:00:00");
                    jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(recordTime));
                    jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(recordTime));
                    if (Integer.parseInt(jsonObject.get("monitorpointtype").toString()) == AirEnum.getCode()) {
                        //AQI站点日数据
                        query = new Query();
                        query.addCriteria(Criteria.where("MonitorTime").is(recordTime).and("StationCode").is(mn));
                        deleteResult = mongoTemplate.remove(query,"StationDayAQIData");
                        System.out.println("AQI站点日数据删除条数："+deleteResult.getDeletedCount());
                        jsonObject.put("datatype", "AQI站点日数据");
                        sendOnlineSupplyDirectQueue(jsonObject);
                        Thread.sleep(1000);
                        //AQI园区日数据
                        query = new Query();
                        query.addCriteria(Criteria.where("MonitorTime").is(recordTime));
                        deleteResult = mongoTemplate.remove(query,"ParkDayAQIData");
                        System.out.println("AQI园区日数据删除条数："+deleteResult.getDeletedCount());
                        jsonObject.put("datatype", "AQI园区日数据");
                        sendOnlineSupplyDirectQueue(jsonObject);
                        Thread.sleep(1000);
                    }
                }

                //AQI站点月数据
                String recordDateYM = DataFormatUtil.getDateYM(monitortime);
                if (!recordDateYM.equals(DataFormatUtil.getDateYM(new Date()))) {
                    Date recordTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYM(monitortime)+"-01 00:00:00");
                    jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(recordTime));
                    jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(recordTime));
                    query = new Query();
                    query.addCriteria(Criteria.where("MonitorTime").is(recordTime).and("StationCode").is(mn));
                    deleteResult = mongoTemplate.remove(query, "StationMonthAQIData");
                    System.out.println("AQI站点月数据删除条数："+deleteResult.getDeletedCount());
                    jsonObject.put("starttime", DataFormatUtil.getFirstDayOfMonth(recordDateYM) + " 00:00:00");
                    jsonObject.put("endtime", DataFormatUtil.getLastDayOfMonth(recordDateYM) + " 23:59:59");
                    jsonObject.put("datatype", "AQI站点月数据");
                    sendOnlineSupplyDirectQueue(jsonObject);
                    Thread.sleep(1000);
                }
            }
            //水质小时数据
            if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(monitortime));
                jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(monitortime));
                Query query = new Query();
                query.addCriteria(Criteria.where("EvaluateTime").is(monitortime).and("DataGatherCode").is(mn).and("DataType").is("HourData"));
                deleteResult = mongoTemplate.remove(query,"WaterStationEvaluateData");
                System.out.println("水质小时数据删除条数："+deleteResult.getDeletedCount());
                jsonObject.put("datatype", "水质小时数据");
                sendOnlineSupplyDirectQueue(jsonObject);
                Thread.sleep(1000);
            }

            //月数据
            String recordDateYM = DataFormatUtil.getDateYM(monitortime);
            if (!recordDateYM.equals(DataFormatUtil.getDateYM(new Date()))) {
                Date recordTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYM(monitortime)+"-01 00:00:00");
                jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(recordTime));
                jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(recordTime));
                Query query = new Query();
                query.addCriteria(Criteria.where("MonitorTime").is(recordTime).and("DataGatherCode").is(mn));
                deleteResult = mongoTemplate.remove(query, "MonthData");
                System.out.println("月数据删除条数："+deleteResult.getDeletedCount());
                jsonObject.put("starttime", DataFormatUtil.getFirstDayOfMonth(recordDateYM) + " 00:00:00");
                jsonObject.put("endtime", DataFormatUtil.getLastDayOfMonth(recordDateYM) + " 23:59:59");
                jsonObject.put("datatype", "月数据");
                sendOnlineSupplyDirectQueue(jsonObject);
                Thread.sleep(1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 发送队列：在线数据补充
     * @param jsonObject
     */
    private void sendOnlineSupplyDirectQueue(JSONObject jsonObject) {
        Message message;
        MessageProperties properties = new MessageProperties();
        message = new Message(jsonObject.toString().getBytes(), properties);
        System.out.println("发送数据："+jsonObject.toString());
        rabbitSender.sendMessage(RabbitMqConfig.ONLINE_Supply_DIRECT_EXCHANGE, RabbitMqConfig.ONLINE_Supply_DIRECT_KEY, message);
    }

    private List<BathUpdateOptions> getDayBathUpdateOptions(JSONObject jsonObject, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData, Map<String, Map<String, Map<Integer, Double>>> mnAndCodeAndEarlyMap, String userid) {
        List<BathUpdateOptions> options = new ArrayList<>();

        String today = DataFormatUtil.getDateYMD(new Date());
        String monitorTime = DataFormatUtil.FormatDateOneToOther(jsonObject.getString("monitortime"), "yyyy-MM-dd HH", "yyyy-MM-dd");
        if (!today.equals(monitorTime)) {
            String pollutantCode;

            String mnCommon = jsonObject.getString("dgimn");
            Map<String, Map<String, Object>> codeAndSetData = mnAndCodeAndSetData.get(mnCommon);
            List<AggregationOperation> aggregations = new ArrayList<>();
            UnwindOperation unwindOperation = unwind("HourDataList");
            aggregations.add(unwindOperation);
            Date startTime = DataFormatUtil.getDateYMDHMS(monitorTime + " 00:00:00");
            Date endTime = DataFormatUtil.getDateYMDHMS(monitorTime + " 23:59:59");
            Criteria criteria = Criteria.where("DataGatherCode").is(mnCommon).and("MonitorTime").gte(startTime).lte(endTime);
            aggregations.add(match(criteria));
            Fields fields = fields("DataGatherCode")
                    .and("PollutantCode", "HourDataList.PollutantCode")
                    .and("AvgConvertStrength", "HourDataList.AvgConvertStrength")
                    .and("AvgStrength", "HourDataList.AvgStrength");
            aggregations.add(project(fields));
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, hourData_db, Document.class);
            List<Document> documents = results.getMappedResults();
            if (documents.size() > 0) {
                Map<String, List<Double>> codeAndValueList = new HashMap<>();
                List<Double> valueList;
                for (Document document : documents) {
                    pollutantCode = document.getString("PollutantCode");
                    if (codeAndSetData.containsKey(pollutantCode)) {
                        Map<String, Object> setData = codeAndSetData.get(pollutantCode);
                        if (codeAndValueList.containsKey(pollutantCode)) {
                            valueList = codeAndValueList.get(pollutantCode);
                        } else {
                            valueList = new ArrayList<>();
                        }
                        if (Integer.parseInt(setData.get("ishasconvertdata").toString()) == 1) {
                            if (document.get("AvgConvertStrength") != null) {
                                valueList.add(Double.parseDouble(document.getString("AvgConvertStrength")));
                            }
                        } else {
                            if (document.get("AvgStrength") != null) {
                                valueList.add(Double.parseDouble(document.getString("AvgStrength")));
                            }
                        }
                        codeAndValueList.put(pollutantCode, valueList);
                    }
                }
                if (codeAndValueList.size() > 0) {
                    Double value;
                    Double maxValue;
                    Map<String, Map<Integer, Double>> codeAndEarlyMap;
                    Map<Integer, Double> earlyMap;
                    for (String codeIndex : codeAndValueList.keySet()) {
                        BathUpdateOptions bathUpdateOptions = new BathUpdateOptions();
                        Query query = new Query();

                        Boolean IsOverStandard = false;
                        Double OverMultiple = 0D;

                        //查询条件
                        query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
                        query.addCriteria(Criteria.where("MonitorTime").is(
                                DataFormatUtil.getDateYMD(monitorTime)));
                        query.addCriteria(Criteria.where("DayDataList.PollutantCode").is(codeIndex));
                        bathUpdateOptions.setQuery(query);
                        bathUpdateOptions.setMulti(true);
                        Update update = new Update();
                        //更新内容
                        Map<String, Object> setData = codeAndSetData.get(codeIndex);
                        String valueString = DataFormatUtil.getListAvgValueSaveTwo(codeAndValueList.get(codeIndex));
                        value = Double.parseDouble(valueString);
                        if (Integer.parseInt(setData.get("ishasconvertdata").toString()) == 1) {
                            update.set("DayDataList.$.AvgConvertStrength", valueString);
                        } else {
                            update.set("DayDataList.$.AvgStrength", valueString);
                        }
                        update.set("DayDataList.$.IsException", 0);
                        update.set("DayDataList.$.AuditManId", userid);
                        update.set("DayDataList.$.AuditTime", new Date());
                        //判断超标
                        if (setData.get("standardmaxvalue") != null) {
                            maxValue = Double.parseDouble(setData.get("standardmaxvalue").toString());
                            if (value > maxValue && maxValue > 0) {
                                IsOverStandard = true;
                                OverMultiple = Double.parseDouble(DataFormatUtil.formatDouble("######0.00", value / maxValue));
                            }
                        }
                        Integer level = -1;
                        if (!IsOverStandard && mnAndCodeAndEarlyMap.containsKey(mnCommon)) {
                            codeAndEarlyMap = mnAndCodeAndEarlyMap.get(mnCommon);
                            earlyMap = codeAndEarlyMap.get(codeIndex);
                            if (earlyMap != null) {
                                for (Integer code : earlyMap.keySet()) {
                                    maxValue = earlyMap.get(code);
                                    if (maxValue != null && value >= maxValue) {
                                        level = level > code ? level : code;
                                    }
                                }
                            }

                        }
                        update.set("DayDataList.$.IsOver", level);
                        update.set("DayDataList.$.IsOverStandard", IsOverStandard);
                        update.set("DayDataList.$.OverMultiple", OverMultiple);
                        bathUpdateOptions.setUpdate(update);
                        options.add(bathUpdateOptions);
                    }
                }
            }
        }
        return options;


    }

    private List<BathUpdateOptions> getSubFlowBathUpdateOptions(String dataType, JSONObject jsonObject) {
        List<BathUpdateOptions> options = new ArrayList<>();
        String today = "";
        String monitorTime = "";
        String startTimeString = "";
        String endTimeString = "";
        String collection = "";
        String dataFKey = "";
        String dataFVKey = "CorrectedFlow";
        String dataKey = "";
        String dataVKey = "";
        Double chu = 1d;
        if (dataType.equals("day")) {
            today = DataFormatUtil.getDateYMD(new Date());
            monitorTime = DataFormatUtil.FormatDateOneToOther(jsonObject.getString("monitortime"), "yyyy-MM-dd HH", "yyyy-MM-dd");
            startTimeString = monitorTime + " 00:00:00";
            endTimeString = monitorTime + " 23:59:59";
            collection = hourFlowData_db;
            dataFKey = "HourFlowDataList";
            dataKey = "DayFlowDataList";
            dataVKey = "DayFlowDataList.$.CorrectedFlow";
            chu = 1d;
        } else if (dataType.equals("month")) {
            today = DataFormatUtil.getDateYM(new Date());
            monitorTime = DataFormatUtil.FormatDateOneToOther(jsonObject.getString("monitortime"), "yyyy-MM-dd HH", "yyyy-MM");
            startTimeString = DataFormatUtil.getFirstDayOfMonth(monitorTime) + " 00:00:00";
            endTimeString = DataFormatUtil.getLastDayOfMonth(monitorTime) + " 23:59:59";
            monitorTime = startTimeString;
            collection = dayFlowData_db;
            dataFKey = "DayFlowDataList";
            dataKey = "MonthFlowDataList";
            dataVKey = "MonthFlowDataList.$.PollutantFlow";
            chu = 1000d;
        } else if (dataType.equals("year")) {
            today = DataFormatUtil.getDateYM(new Date());
            monitorTime = DataFormatUtil.FormatDateOneToOther(jsonObject.getString("monitortime"), "yyyy-MM-dd HH", "yyyy");
            startTimeString = DataFormatUtil.getYearFirst(monitorTime) + " 00:00:00";
            endTimeString = DataFormatUtil.getYearLast(monitorTime) + " 23:59:59";
            monitorTime = startTimeString;
            collection = monthFlowData_db;
            dataFKey = "MonthFlowDataList";
            dataFVKey = "PollutantFlow";
            dataKey = "YearFlowDataList";
            dataVKey = "YearFlowDataList.$.PollutantFlow";
            chu = 1000d;
        }
        if (StringUtils.isNotBlank(today) && !today.equals(monitorTime)) {
            String pollutantCode;
            String mnCommon = jsonObject.getString("dgimn");

            List<AggregationOperation> aggregations = new ArrayList<>();
            UnwindOperation unwindOperation = unwind(dataFKey);
            aggregations.add(unwindOperation);
            Date startTime = DataFormatUtil.getDateYMDHMS(startTimeString);
            Date endTime = DataFormatUtil.getDateYMDHMS(endTimeString);
            Criteria criteria = Criteria.where("DataGatherCode").is(mnCommon).and("MonitorTime").gte(startTime).lte(endTime);
            aggregations.add(match(criteria));
            Fields fields = fields("DataGatherCode")
                    .and("PollutantCode", dataFKey + ".PollutantCode")
                    .and("CorrectedFlow", dataFKey + "." + dataFVKey);
            aggregations.add(project(fields));
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = results.getMappedResults();
            Double value;
            if (documents.size() > 0) {
                Map<String, Double> codeAndValueSum = new HashMap<>();
                for (Document document : documents) {
                    pollutantCode = document.getString("PollutantCode");
                    if (document.get("CorrectedFlow") != null) {
                        value = Double.parseDouble(document.getString("CorrectedFlow"));
                        if (codeAndValueSum.containsKey(pollutantCode)) {
                            codeAndValueSum.put(pollutantCode, codeAndValueSum.get(pollutantCode) + value);
                        } else {
                            codeAndValueSum.put(pollutantCode, value);
                        }
                    }


                }
                if (codeAndValueSum.size() > 0) {
                    for (String codeIndex : codeAndValueSum.keySet()) {
                        BathUpdateOptions bathUpdateOptions = new BathUpdateOptions();
                        Query query = new Query();
                        //查询条件
                        query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
                        query.addCriteria(Criteria.where("MonitorTime").is(startTime));
                        query.addCriteria(Criteria.where(dataKey + ".PollutantCode").is(codeIndex));
                        bathUpdateOptions.setQuery(query);
                        bathUpdateOptions.setMulti(true);
                        Update update = new Update();
                        //更新内容
                        value = codeAndValueSum.get(codeIndex) / chu;
                        String valueString = DataFormatUtil.SaveThreeAndSubZero(value);
                        update.set(dataVKey, valueString);
                        bathUpdateOptions.setUpdate(update);
                        options.add(bathUpdateOptions);
                    }
                }
            }
        }
        return options;
    }

    private List<BathUpdateOptions> getFlowBathUpdateOptions(JSONObject jsonObject, Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData) {
        List<BathUpdateOptions> options = new ArrayList<>();
        //获取流量，根据单位计算排放量
        Date monitorTime = DataFormatUtil.getDateYMDH(jsonObject.getString("monitortime"));
        Double value;
        String mnCommon = jsonObject.getString("dgimn");
        Date monitortime = DataFormatUtil.getDateYMDH(jsonObject.getString("monitortime"));
        List<AggregationOperation> aggregations = new ArrayList<>();
        UnwindOperation unwindOperation = unwind("HourDataList");
        aggregations.add(unwindOperation);
        Criteria criteria = Criteria.where("DataGatherCode").is(mnCommon).and("MonitorTime").is(monitortime);
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode")
                .and("PollutantCode", "HourDataList.PollutantCode")
                .and("AvgConvertStrength", "HourDataList.AvgConvertStrength")
                .and("AvgStrength", "HourDataList.AvgStrength");
        aggregations.add(project(fields));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, hourData_db, Document.class);
        List<Document> documents = results.getMappedResults();
        if (documents.size() > 0) {
            String pollutantCode;
            /*//查询原始排放量数据
            aggregations.clear();
            unwindOperation = unwind("HourFlowDataList");
            aggregations.add(unwindOperation);
            criteria = Criteria.where("DataGatherCode").is(mnCommon).and("MonitorTime").is(monitortime);
            aggregations.add(match(criteria));
            fields = fields("DataGatherCode")
                    .and("PollutantCode", "HourFlowDataList.PollutantCode")
                    .and("CorrectedFlow", "HourFlowDataList.CorrectedFlow");
            aggregations.add(project(fields));
            aggregation = newAggregation(aggregations);
            results = mongoTemplate.aggregate(aggregation, hourFlowData_db, Document.class);
            List<Document> pflDocument = results.getMappedResults();
            Map<String, Object> codeAndPFL = new HashMap<>();
            if (pflDocument.size() > 0) {
                for (Document document : pflDocument) {
                    if (document.get("PollutantCode")!=null){
                        codeAndPFL.put(document.getString("PollutantCode"),document.get("CorrectedFlow"));
                    }
                }
            }*/
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
                        BathUpdateOptions bathUpdateOptions = new BathUpdateOptions();
                        Query query = new Query();
                        //查询条件
                        query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
                        query.addCriteria(Criteria.where("MonitorTime").is(
                                monitorTime
                        ));
                        query.addCriteria(Criteria.where("HourFlowDataList.PollutantCode").is(codeIndex));
                        bathUpdateOptions.setQuery(query);
                        bathUpdateOptions.setMulti(true);
                        Update update = new Update();
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
                        update.set("HourFlowDataList.$.CorrectedFlow", valueString);
                        //update.set("HourFlowDataList.$.AvgFlow",codeAndPFL.get(codeIndex) );
                        bathUpdateOptions.setUpdate(update);
                        options.add(bathUpdateOptions);
                    }
                }
            }
        }
        return options;

    }

    private static void doBathUpdate(MongoCollection<Document> mongoCollection,
                                     List<BathUpdateOptions> options) {
        List<WriteModel<Document>> requests = new ArrayList<>();  //创建参数集合
        Document queryDocument;
        Document updateDocument;
        UpdateOneModel<Document> updateOneModel;
        for (BathUpdateOptions option : options) {
            queryDocument = option.getQuery().getQueryObject();  //条件
            updateDocument = option.getUpdate().getUpdateObject();  //更改信息
            updateOneModel = new UpdateOneModel<>(queryDocument, updateDocument, new UpdateOptions().upsert(false));
            requests.add(updateOneModel);
        }
        mongoCollection.bulkWrite(requests);
    }


    private List<BathUpdateOptions> getBathUpdateOptions(JSONObject jsonObject,
                                                         Map<String, Map<String, Map<String, Object>>> mnAndCodeAndSetData,
                                                         Map<String, Map<String, Map<Integer, Double>>> mnAndCodeAndEarlyMap,
                                                         String userid) {
        List<BathUpdateOptions> options = new ArrayList<>();
        JSONArray pollutants;
        JSONObject pollutant;
        Double Xvalue;//修约值
        Double maxValue;
        boolean IsOverStandard;
        Double OverMultiple;
        Map<String, Map<String, Object>> codeAndSetData;
        Map<String, Map<Integer, Double>> codeAndEarlyMap;
        Map<Integer, Double> earlyMap;
        String mnCommon = jsonObject.getString("dgimn");
        String monitorTime = jsonObject.getString("monitortime");
        String pollutantCode;
        pollutants = jsonObject.getJSONArray("pollutantlist");
        String auditDes = jsonObject.getString("auditdes");
        if (pollutants.size() == 0) {//
            Date monitortime = DataFormatUtil.getDateYMDH(monitorTime);
            List<AggregationOperation> aggregations = new ArrayList<>();
            UnwindOperation unwindOperation = unwind("HourDataList");
            aggregations.add(unwindOperation);
            Criteria criteria = Criteria.where("DataGatherCode").is(mnCommon).and("MonitorTime").is(monitortime);
            aggregations.add(match(criteria));
            Fields fields = fields("DataGatherCode")
                    .and("PollutantCode", "HourDataList.PollutantCode");
            aggregations.add(project(fields));
            Aggregation aggregation = newAggregation(aggregations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, hourData_db, Document.class);
            List<Document> documents = results.getMappedResults();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    BathUpdateOptions bathUpdateOptions = new BathUpdateOptions();
                    Query query = new Query();
                    //查询条件
                    query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
                    query.addCriteria(Criteria.where("MonitorTime").is(
                            DataFormatUtil.getDateYMDH(monitorTime)
                    ));
                    query.addCriteria(Criteria.where("HourDataList.PollutantCode").is(document.getString("PollutantCode")));
                    bathUpdateOptions.setQuery(query);
                    bathUpdateOptions.setMulti(true);
                    Update update = new Update();
                    update.set("HourDataList.$.AuditManId", userid);
                    update.set("HourDataList.$.AuditTime", new Date());
                    update.set("HourDataList.$.AuditDes", auditDes);
                    bathUpdateOptions.setUpdate(update);
                    options.add(bathUpdateOptions);
                }
            }
        } else {
            for (int j = 0; j < pollutants.size(); j++) {
                pollutant = pollutants.getJSONObject(j);
                pollutantCode = pollutant.getString("pollutantcode");
                IsOverStandard = false;
                OverMultiple = 0D;
                codeAndSetData = mnAndCodeAndSetData.get(mnCommon);
                if (codeAndSetData.containsKey(pollutantCode)) {
                    BathUpdateOptions bathUpdateOptions = new BathUpdateOptions();
                    Query query = new Query();
                    //查询条件
                    query.addCriteria(Criteria.where("DataGatherCode").is(mnCommon));
                    query.addCriteria(Criteria.where("MonitorTime").is(
                            DataFormatUtil.getDateYMDH(monitorTime)
                    ));
                    query.addCriteria(Criteria.where("HourDataList.PollutantCode").is(pollutantCode));
                    bathUpdateOptions.setQuery(query);
                    bathUpdateOptions.setMulti(true);
                    Update update = new Update();
                    //更新内容
                    Xvalue = Double.parseDouble(pollutant.getString("xvalue"));
                    Map<String, Object> setData = codeAndSetData.get(pollutantCode);

                    String XvalueString = getPollutantValueWithAccuracy(pollutantCode,pollutant.getString("xvalue"),jsonObject.get("monitorpointtypes"));

                    if (Integer.parseInt(setData.get("ishasconvertdata").toString()) == 1) {
                        update.set("HourDataList.$.AvgConvertStrength", XvalueString);
                    } else {
                        update.set("HourDataList.$.AvgStrength", XvalueString);
                    }
                    //update.set("HourDataList.$.RepairVal", YvalueString);
                    update.set("HourDataList.$.IsException", 0);
                    update.set("HourDataList.$.AuditManId", userid);
                    update.set("HourDataList.$.AuditTime", new Date());
                    update.set("HourDataList.$.AuditDes", auditDes);
                    //判断超标
                    if (setData.get("standardmaxvalue") != null) {
                        maxValue = Double.parseDouble(setData.get("standardmaxvalue").toString());
                        if (Xvalue > maxValue && maxValue > 0) {
                            IsOverStandard = true;
                            OverMultiple = Double.parseDouble(DataFormatUtil.formatDouble("######0.00", Xvalue / maxValue));
                        }
                    }
                    Integer level = -1;
                    if (!IsOverStandard && mnAndCodeAndEarlyMap.containsKey(mnCommon)) {
                        codeAndEarlyMap = mnAndCodeAndEarlyMap.get(mnCommon);
                        earlyMap = codeAndEarlyMap.get(pollutantCode);
                        if (earlyMap != null) {
                            for (Integer code : earlyMap.keySet()) {
                                maxValue = earlyMap.get(code);
                                if (maxValue != null && Xvalue >= maxValue) {
                                    level = level > code ? level : code;
                                }
                            }
                        }

                    }
                    update.set("HourDataList.$.IsOver", level);
                    update.set("HourDataList.$.IsOverStandard", IsOverStandard);
                    update.set("HourDataList.$.OverMultiple", OverMultiple);
                    bathUpdateOptions.setUpdate(update);
                    options.add(bathUpdateOptions);
                }
            }
        }
        return options;
    }

    private String getPollutantValueWithAccuracy(String pollutantCode, String value, Object monitorpointtypes) {
        //污染物有设置精度
        if (value == null) {
            return null;
        }
        BigDecimal decimal = new BigDecimal(value);
        if (pollutantCode.equals(AIR_COMMON_SIX_INDEX_AQI.getCode())) {
            return decimal.setScale(0, BigDecimal.ROUND_HALF_EVEN).toString();
        }
        HashMap<String, Object> param = new HashMap<>();
        param.put("monitorpointtypes", monitorpointtypes);
        param.put("code", pollutantCode);
        Integer accuracy = pollutantFactorMapper.getPollutantAccuracyByParamMap(param);
        if (accuracy != null && accuracy >= 0) {
            return decimal.setScale(accuracy, BigDecimal.ROUND_HALF_EVEN).toString();
        }
        return value;
    }

}

