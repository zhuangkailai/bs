package com.tjpu.sp.controller.common;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @author: lip
 * @date: 2020/11/26 0026 上午 10:42
 * @Description: 公共安全、环保报警数据统计类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("overAlarmCommon")
public class OverAlarmCommonController {
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private PollutantService pollutantService;

    @Autowired
    private DeviceStatusService deviceStatusService;

    private String earlyWarnDataCollect = "EarlyWarnData";
    private String overDataCollect = "OverData";
    private String exceptionDataCollect = "ExceptionData";
    private String hourDataCollect = "HourData";
    private String db_RealTimeData = "RealTimeData";
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: lip
     * @date: 2020/11/26 0026 上午 10:52
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getNowDayLastAlarmDataByParam", method = RequestMethod.POST)
    public Object getNowDayLastAlarmDataByParam(
            @RequestJson(value = "monitortypes") List<Integer> monitortypes,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
            @RequestJson(value = "pagesize", required = false)  Integer  pagesize,
            @RequestJson(value = "pagenum", required = false)  Integer  pagenum,
            @RequestJson(value = "alarmtypes", required = false) List<Integer> alarmtypes
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            Date nowDay = new Date();

            String startTime = DataFormatUtil.getDateYMD(nowDay) + " 00:00:00";
            String endTime = DataFormatUtil.getDateYMDHMS(nowDay);
            List<String> userdgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<String> userpollutionids = RedisTemplateUtil.getRedisCacheDataByToken("pollutionids", List.class);

            Map<String, Map<String, Object>> mnAndPointData = new HashMap<>();
            for (Integer monitortype : monitortypes) {
                setMnAndPointData(monitortype, mnAndPointData);
            }


            if (alarmtypes == null || alarmtypes.size() == 0) {//默认全部报警类型：预警、报警、突变、异常、视频报警
                alarmtypes = Arrays.asList(CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()
                );
            }

            Set<String> mnSet = mnAndPointData.keySet();
            List<String> mns = new ArrayList<>();


            for (String mnIndex:mnSet){
                if (userdgimns.contains(mnIndex)){
                    mns.add(mnIndex);
                }
            }

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("mns", mns);
            paramMap.put("pollutionids", userpollutionids);
            List<Document> alarmList = new ArrayList<>();
            for (Integer alarmType : alarmtypes) {
                setAlarmTypeAndAlarmList(alarmType, paramMap, alarmList);
            }
            String mnCommon;
            int earlyNum = 0;
            int alarmNum = 0;
            int exceptionNum = 0;
            List<Map<String,Object>> dataList = new ArrayList<>();
            if (alarmList.size() > 0) {
                paramMap.put("monitorpointtypes", monitorpointtypes);

                List<Map<String,Object>> deviceStatusDataList = deviceStatusService.getDeviceStatusDataByParam(paramMap);
                Map<String,Integer> mnAndType = getMnAndType(deviceStatusDataList);

                List<Map<String, Object>> pollutantDataList = pollutantService.getPollutantsByPollutantType(paramMap);
                Map<Integer,Map<String,Object>> typeCodeAndName = getTypeAndCodeAndValue(pollutantDataList, "name");
                Map<Integer,Map<String,Object>> typeCodeAndUnit = getTypeAndCodeAndValue(pollutantDataList, "PollutantUnit");
                Map<Integer,Map<String,Object>> typeAndCodeAndOrder = getTypeAndCodeAndValue(pollutantDataList,"OrderIndex");
                List<Date> timeList = new ArrayList<>();
                List<String> alarmMns = new ArrayList<>();
                List<Date> timeHourList = new ArrayList<>();
                List<String> alarmHourMns = new ArrayList<>();
                Map<String, Integer> keyAndAlarmType = new HashMap<>();
                Map<String, Integer> keyAndHourAlarmType = new HashMap<>();
                String key;
                Integer remindType;
                Date maxtime;
                for (Document document : alarmList) {
                    maxtime = document.getDate("maxtime");
                    mnCommon = document.getString("_id");
                    remindType = document.getInteger("remindtype");
                    key = mnCommon + "," + DataFormatUtil.formatDateToOtherFormat(document.getDate("maxtime"), "HH:mm:ss")+"#"+remindType;
                    if (CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode() == remindType) {
                        timeHourList.add(maxtime);
                        alarmHourMns.add(mnCommon);
                        keyAndHourAlarmType.put(key, remindType);
                    } else {
                        timeList.add(maxtime);
                        alarmMns.add(mnCommon);
                        keyAndAlarmType.put(key, remindType);
                    }
                }
                paramMap.put("timeList", timeList);
                paramMap.put("alarmMns", alarmMns);
                List<Document> realTimeDataList = getRealTimeData(paramMap);
                Map<String, List<Map<String, Object>>> keyAndPollutantList = new HashMap<>();
                if (realTimeDataList.size() > 0) {
                    List<Document> pollutants;
                    String pollutantCode;
                    String tempKey;
                    Integer type;
                    for (String keyIndex:keyAndAlarmType.keySet()){
                        for (Document document : realTimeDataList) {
                            mnCommon = document.getString("DataGatherCode");
                            key = mnCommon + "," + DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), "HH:mm:ss");
                            tempKey = keyIndex.split("#")[0];

                            type = mnAndType.get(mnCommon);

                            if (tempKey.contains(key)) {
                                List<Map<String, Object>> pollutantList = new ArrayList<>();
                                pollutants = document.get("RealDataList", List.class);
                                for (Document pollutant : pollutants) {
                                    Map<String, Object> pollutantMap = new HashMap<>();
                                    pollutantCode = pollutant.getString("PollutantCode");
                                    if (type!=null&&typeCodeAndName.containsKey(type)&&typeCodeAndName.get(type).get(pollutantCode)!=null){
                                        if (typeAndCodeAndOrder.get(type)!=null&&typeAndCodeAndOrder.get(type).get(pollutantCode)!=null){
                                            pollutantMap.put("orderindex", typeAndCodeAndOrder.get(type).get(pollutantCode));
                                        }else {
                                            pollutantMap.put("orderindex", 10000);
                                        }
                                        if (typeCodeAndUnit.get(type)!=null&&typeCodeAndUnit.get(type).get(pollutantCode)!=null){
                                            pollutantMap.put("pollutantunit", typeCodeAndUnit.get(type).get(pollutantCode));
                                        }else {
                                            pollutantMap.put("pollutantunit", "");
                                        }
                                        pollutantMap.put("pollutantcode", pollutantCode);
                                        pollutantMap.put("pollutantname", typeCodeAndName.get(type).get(pollutantCode));
                                        pollutantMap.put("monitorvalue", pollutant.get("MonitorValue"));
                                        pollutantMap.put("alarmMark", getAlarmMark(keyAndAlarmType.get(keyIndex), pollutant));
                                        pollutantList.add(pollutantMap);
                                    }

                                }
                                key = keyAndAlarmType.get(keyIndex) + "," + key;
                                keyAndPollutantList.put(key, pollutantList);
                            }
                        }
                    }
                }
                paramMap.put("timeList", timeHourList);
                paramMap.put("alarmMns", alarmHourMns);
                List<Document> hourDataList = getHourData(paramMap);
                if (hourDataList.size() > 0) {
                    List<Document> pollutants;
                    String pollutantCode;
                    Integer type;
                    for (Document document : hourDataList) {
                        mnCommon = document.getString("DataGatherCode");
                        key = mnCommon + "," + DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), "HH:mm:ss");
                        type = mnAndType.get(mnCommon);
                        if (keyAndHourAlarmType.containsKey(key)) {
                            List<Map<String, Object>> pollutantList = new ArrayList<>();
                            pollutants = document.get("HourDataList", List.class);
                            for (Document pollutant : pollutants) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantCode = pollutant.getString("PollutantCode");
                                if (type!=null&&typeCodeAndName.containsKey(type)&&typeCodeAndName.get(type).get(pollutantCode)!=null){
                                    if (typeAndCodeAndOrder.get(type)!=null&&typeAndCodeAndOrder.get(type).get(pollutantCode)!=null){
                                        pollutantMap.put("orderindex", typeAndCodeAndOrder.get(type).get(pollutantCode));
                                    }else {
                                        pollutantMap.put("orderindex", 10000);
                                    }
                                    if (typeCodeAndUnit.get(type)!=null&&typeCodeAndUnit.get(type).get(pollutantCode)!=null){
                                        pollutantMap.put("pollutantunit", typeCodeAndUnit.get(type).get(pollutantCode));
                                    }else {
                                        pollutantMap.put("pollutantunit", "");
                                    }
                                    pollutantMap.put("pollutantcode", pollutantCode);
                                    pollutantMap.put("pollutantname", typeCodeAndName.get(type).get(pollutantCode));
                                    pollutantMap.put("monitorvalue", pollutant.get("AvgStrength"));
                                    pollutantMap.put("alarmMark", getAlarmMark(keyAndHourAlarmType.get(key), pollutant));
                                    pollutantList.add(pollutantMap);
                                    pollutantList.add(pollutantMap);
                                }
                            }
                            key = keyAndHourAlarmType.get(key) + "," + key;
                            keyAndPollutantList.put(key, pollutantList);
                        }
                    }
                }
                if (keyAndPollutantList.size() > 0) {
                    int alarmType;
                    List<Map<String, Object>> pollutantList;
                    for (String keyIndex : keyAndPollutantList.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        mnCommon = keyIndex.split(",")[1];
                        alarmType = Integer.parseInt(keyIndex.split(",")[0]);
                        dataMap.put("alarmtype",alarmType );
                        if (CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()==alarmType
                                ||CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()==alarmType){
                            earlyNum++;
                        }
                        if (CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()==alarmType){
                            exceptionNum++;
                        }
                        if (CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()==alarmType){
                            alarmNum++;
                        }
                        dataMap.put("monitortime", keyIndex.split(",")[2]);
                        dataMap.putAll(mnAndPointData.get(mnCommon));

                        pollutantList = keyAndPollutantList.get(keyIndex);
                        if (pollutantList!=null){//排序
                            pollutantList = pollutantList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                        }
                        dataMap.put("pollutantlist",pollutantList);
                        dataList.add(dataMap);
                    }

                }


            }
            int videoNum = 0;
            if (dataList.size() > 0) { //排序分页
                dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
                if (pagesize != null && pagenum != null) {
                    resultMap.put("total", dataList.size());
                    dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", dataList);
                }
            }
            //统计数据
            Map<String,Object> countdata = new HashMap<>();
            countdata.put("earlynum",earlyNum);
            countdata.put("alarmnum",alarmNum);
            countdata.put("exceptionnum",exceptionNum);
            countdata.put("videonum",videoNum);
            resultMap.put("countdata",countdata);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<Integer,Map<String,Object>> getTypeAndCodeAndValue(List<Map<String, Object>> pollutantDataList,String key) {
        String polutantCode;
        Map<Integer,Map<String,Object>> typeAndCodeAndValue = new HashMap<>();
        Map<String,Object> codeAndValue;
        Integer type;
        for (Map<String, Object> pollutant : pollutantDataList) {
            if (pollutant.get("PollutantType")!=null&&pollutant.get("code")!=null){
                polutantCode = pollutant.get("code").toString();
                type = Integer.parseInt(pollutant.get("PollutantType").toString());
                if (typeAndCodeAndValue.containsKey(type)){
                    codeAndValue = typeAndCodeAndValue.get(type);
                }else {
                    codeAndValue = new HashMap<>();
                }
                codeAndValue.put(polutantCode,pollutant.get(key));

                typeAndCodeAndValue.put(type,codeAndValue);
            }
        }
        return typeAndCodeAndValue;

    }

    private Map<String,Integer> getMnAndType(List<Map<String, Object>> deviceStatusDataList) {
        Map<String,Integer> mnAndType = new HashMap<>();
        for (Map<String, Object> dataMap:deviceStatusDataList){
            if (dataMap.get("dgimn")!=null&&dataMap.get("monitorpointtype")!=null){
                mnAndType.put(dataMap.get("dgimn").toString(),Integer.parseInt(dataMap.get("monitorpointtype").toString()));
            }
        }
       return mnAndType;

    }

    private void setMnAndPointData(Integer monitortype, Map<String, Map<String, Object>> mnAndPointData) {
        List<Map<String, Object>> pointDataList = new ArrayList<>();
        switch (monitortype) {
            case 1://环保
                pointDataList = pollutionService.getHBPointDataList();
                break;
        }
        if (pointDataList.size() > 0) {
            for (Map<String, Object> pointData : pointDataList) {
                if (pointData.get("dgimn") != null) {
                    mnAndPointData.put(pointData.get("dgimn").toString(), pointData);
                }
            }
        }
    }
    private void setAlarmTypeAndAlarmList(Integer alarmType, Map<String, Object> paramMap, List<Document> alarmList) {
        boolean isSelect = true;

        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(alarmType)) {
            case EarlyAlarmEnum:
                paramMap.remove("unwindkey", "HourDataList");
                paramMap.put("collection", earlyWarnDataCollect);
                paramMap.put("timeKey", "EarlyWarnTime");
                //paramMap.put("DataType", db_RealTimeData);
                break;
            case OverAlarmEnum:
                paramMap.remove("unwindkey", "HourDataList");
                paramMap.put("collection", overDataCollect);
                paramMap.put("timeKey", "OverTime");
                //paramMap.put("DataType", db_RealTimeData);
                break;
            case ConcentrationChangeEnum:
                paramMap.put("collection", hourDataCollect);
                paramMap.put("timeKey", "MonitorTime");
                paramMap.put("unwindkey", "HourDataList");
                break;
            case ExceptionAlarmEnum:
                paramMap.remove("unwindkey", "HourDataList");
                paramMap.put("collection", exceptionDataCollect);
                paramMap.put("timeKey", "ExceptionTime");
                //paramMap.put("DataType", db_RealTimeData);
                break;
                default:
                    isSelect = false;
                    break;

        }
        if (isSelect){
            List<Document> documents = countMnMaxTimeByParam(paramMap);
            if (documents.size() > 0) {
                for (Document document : documents) {
                    document.put("remindtype", alarmType);
                    alarmList.add(document);
                }
            }
        }
    }


    private String getAlarmMark(Integer alarmType, Document pollutant) {
        String alarmMark = "";
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(alarmType)) {
            case EarlyAlarmEnum:
                if (pollutant.getInteger("IsOver") == 0) {
                    alarmMark = EarlyAlarmEnum.getCode().toString();
                }
                break;
            case OverAlarmEnum:
                if (pollutant.getInteger("IsOver") > 0 || pollutant.getBoolean("IsOverStandard")) {
                    alarmMark = OverAlarmEnum.getCode().toString();
                }
                break;
            case ConcentrationChangeEnum:
                if (pollutant.getBoolean("IsSuddenChange")) {
                    alarmMark = ConcentrationChangeEnum.getCode().toString();
                }
                break;
            case ExceptionAlarmEnum:
                if (pollutant.getInteger("IsException") > 0) {
                    alarmMark = ExceptionAlarmEnum.getCode().toString();
                }
                break;

        }

        return alarmMark;

    }

    public List<Document> getRealTimeData(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("alarmMns");
        List<Date> timeList = (List<Date>) paramMap.get("timeList");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").in(timeList);
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode", "MonitorTime", "RealDataList");
        aggregations.add(project(fields));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, db_RealTimeData, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    public List<Document> getHourData(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("alarmMns");
        List<Date> timeList = (List<Date>) paramMap.get("timeList");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").in(timeList);
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode", "MonitorTime", "HourDataList");
        aggregations.add(project(fields));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, hourDataCollect, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    public List<Document> countMnMaxTimeByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            UnwindOperation unwindOperation = unwind(unwindkey);
            aggregations.add(unwindOperation);
        }
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        Fields fields;
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            fields = fields("DataGatherCode", timeKey, "maxtime").and("PollutantCode", unwindkey + ".PollutantCode");
        } else {
            fields = fields("DataGatherCode", timeKey, "maxtime", "PollutantCode");
        }
        aggregations.add(project(fields));
        GroupOperation groupOperation = group("DataGatherCode").max(timeKey).as("maxtime");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }


}
