package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.alibaba.fastjson.JSON;
import com.aspose.pdf.facades.IFacade;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.SessionUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.base.mn.AlarmMNService;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.DeviceDevOpsInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.ContinuousExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @Description: 企业统计处理类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2021/4/20 13:24
 */
@RestController
@ControllerAdvice
@RequestMapping("onlineEntCount")
public class OnlineEntCountController {

    @Autowired
    private PollutionService pollutionService;

    @Autowired
    private PollutantService pollutantService;

    private final String DB_OverData = "OverData";
    private final String DB_ExceptionData = "ExceptionData";
    private final String DB_LatestData = "LatestData";
    private final String DB_MinuteData = "MinuteData";
    private final String DB_OverModel = "OverModel";
    private final String DB_ExceptionModel = "ExceptionModel";
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @Description: 获取企业各个监测类型下的报警异常数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/20 13:26
     */
    @RequestMapping(value = "getEntMonitorTypeNumByParam", method = RequestMethod.POST)
    public Object getEntMonitorTypeNumByParam(@RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "endtime", required = false) String endtime,
                                              @RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //获取企业下关联的mn号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
            if (pointList.size() > 0) {
                Map<String, Integer> mnAndType = new HashMap<>();
                String mnCommon;
                Integer type;
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointMap : pointList) {
                    if (pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null) {
                        mnCommon = pointMap.get("dgimn").toString();
                        type = Integer.parseInt(pointMap.get("monitorpointtype").toString());
                        mnAndType.put(mnCommon, type);
                        mns.add(mnCommon);
                    }
                }
                paramMap.clear();
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", mns);
                //报警统计
                paramMap.put("timeKey", "OverTime");
                paramMap.put("collection", DB_OverData);
                List<Document> overList = countMnOverOrExceptionByParam(paramMap);
                Map<Integer, Integer> typeAndOverNum = new HashMap<>();
                if (overList.size() > 0) {
                    for (Document document : overList) {
                        mnCommon = document.getString("_id");
                        type = mnAndType.get(mnCommon);
                        if (typeAndOverNum.containsKey(type)) {
                            typeAndOverNum.put(type, typeAndOverNum.get(type) + document.getInteger("countnum"));
                        } else {
                            typeAndOverNum.put(type, document.getInteger("countnum"));
                        }
                    }
                }

                //异常统计
                paramMap.put("timeKey", "ExceptionTime");
                paramMap.put("collection", DB_ExceptionData);
                Map<Integer, Integer> typeAndExceptionNum = new HashMap<>();
                List<Document> exceptionList = countMnOverOrExceptionByParam(paramMap);
                if (exceptionList.size() > 0) {
                    for (Document document : exceptionList) {
                        mnCommon = document.getString("_id");
                        type = mnAndType.get(mnCommon);
                        if (typeAndExceptionNum.containsKey(type)) {
                            typeAndExceptionNum.put(type, typeAndExceptionNum.get(type) + document.getInteger("countnum"));
                        } else {
                            typeAndExceptionNum.put(type, document.getInteger("countnum"));
                        }
                    }
                }
                List<Integer> typeList = Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), RainEnum.getCode(), SmokeEnum.getCode());
                for (Integer typeIndex : typeList) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointtypecode", typeIndex);
                    resultMap.put("monitorpointtypename", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(typeIndex).replaceAll("监测点", ""));
                    resultMap.put("overnum", typeAndOverNum.get(typeIndex) != null ? typeAndOverNum.get(typeIndex) : 0);
                    resultMap.put("exceptionnum", typeAndExceptionNum.get(typeIndex) != null ? typeAndExceptionNum.get(typeIndex) : 0);
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
     * @Description: 获取企业各个监测点类型报警点位数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/25 11:00
     */
    @RequestMapping(value = "getEntPointAlarmNumByParam", method = RequestMethod.POST)
    public Object getEntPointAlarmNumByParam(@RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "endtime", required = false) String endtime,
                                             @RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //获取企业下关联的mn号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
            if (pointList.size() > 0) {
                Map<String, Integer> mnAndType = new HashMap<>();
                String mnCommon;
                Integer type;
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointMap : pointList) {
                    if (pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null) {
                        mnCommon = pointMap.get("dgimn").toString();
                        type = Integer.parseInt(pointMap.get("monitorpointtype").toString());
                        mnAndType.put(mnCommon, type);
                        mns.add(mnCommon);
                    }
                }
                paramMap.clear();
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", mns);
                //报警统计
                paramMap.put("timeKey", "OverTime");
                paramMap.put("collection", DB_OverData);
                List<Document> overList = countMnOverOrExceptionByParam(paramMap);
                Map<Integer, Integer> typeAndNum = new HashMap<>();
                if (overList.size() > 0) {
                    for (Document document : overList) {
                        mnCommon = document.getString("_id");
                        type = mnAndType.get(mnCommon);
                        if (typeAndNum.containsKey(type)) {
                            typeAndNum.put(type, typeAndNum.get(type));
                        } else {
                            typeAndNum.put(type, 1);
                        }
                    }
                }

                //异常统计
                paramMap.put("timeKey", "ExceptionTime");
                paramMap.put("collection", DB_ExceptionData);

                List<Document> exceptionList = countMnOverOrExceptionByParam(paramMap);
                if (exceptionList.size() > 0) {
                    for (Document document : exceptionList) {
                        mnCommon = document.getString("_id");
                        type = mnAndType.get(mnCommon);
                        if (typeAndNum.containsKey(type)) {
                            typeAndNum.put(type, typeAndNum.get(type));
                        } else {
                            typeAndNum.put(type, 1);
                        }
                    }
                }
                List<Integer> typeList = Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), RainEnum.getCode(), SmokeEnum.getCode());
                for (Integer typeIndex : typeList) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointtypecode", typeIndex);
                    resultMap.put("monitorpointtypename", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(typeIndex).replaceAll("监测点", ""));
                    resultMap.put("countnum", typeAndNum.get(typeIndex) != null ? typeAndNum.get(typeIndex) : 0);
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
     * @Description: 获取企业污染物报警、预警、异常时段数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/25 11:00
     */
    @RequestMapping(value = "getEntPollutantTimeListByParam", method = RequestMethod.POST)
    public Object getEntPollutantTimeListByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "remindtype") Integer remindType,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //获取企业下关联的mn号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
            if (pointList.size() > 0) {
                String mnCommon;
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointMap : pointList) {
                    if (pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null
                            && monitorpointtype == Integer.parseInt(pointMap.get("monitorpointtype").toString())) {
                        mnCommon = pointMap.get("dgimn").toString();
                        mns.add(mnCommon);
                    }
                }
                paramMap.clear();
                paramMap.put("starttime", monitortime + " 00:00:00");
                paramMap.put("endtime", monitortime + " 23:59:59");
                paramMap.put("mns", mns);
                Map<String, List<Document>> mnAndPollutantList = getRemindTypeAndDataList(remindType, paramMap);
                if (mnAndPollutantList.size() > 0) {
                    Map<String, List<String>> codeAndTimeList = new HashMap<>();
                    List<Document> pollutantList;
                    String pollutantCode;
                    String times;
                    List<String> timeList;
                    for (String mnIndex : mnAndPollutantList.keySet()) {

                        pollutantList = mnAndPollutantList.get(mnIndex);
                        for (Document pollutant : pollutantList) {
                            times = "";
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (remindType == EarlyAlarmEnum.getCode()) {//预警
                                times = DataFormatUtil.getDateHM(pollutant.getDate("FirstOverTime")) + "~"
                                        + DataFormatUtil.getDateHM(pollutant.getDate("LastOverTime"));
                            } else if (remindType == ExceptionAlarmEnum.getCode()) {//异常
                                times = DataFormatUtil.getDateHM(pollutant.getDate("FirstExceptionTime")) + "~"
                                        + DataFormatUtil.getDateHM(pollutant.getDate("LastExceptionTime"));
                            } else if (remindType == OverAlarmEnum.getCode()) {//报警
                                times = DataFormatUtil.getDateHM(pollutant.getDate("FirstOverTime")) + "~"
                                        + DataFormatUtil.getDateHM(pollutant.getDate("LastOverTime"));
                            }
                            if (StringUtils.isNotBlank(times)) {
                                if (codeAndTimeList.containsKey(pollutantCode)) {
                                    timeList = codeAndTimeList.get(pollutantCode);
                                } else {
                                    timeList = new ArrayList<>();
                                }
                                timeList.add(times);
                                codeAndTimeList.put(pollutantCode, timeList);
                            }

                        }
                    }
                    if (codeAndTimeList.size() > 0) {
                        List<Map<String, Object>> pollutantDList = pollutantService.getPollutantsByCodesAndType(paramMap);
                        Map<Integer, Map<String, Object>> typeAndCodeAndPollutant = getPollutantMap(pollutantDList);
                        Map<String, Object> codeAndPollutant = typeAndCodeAndPollutant.get(monitorpointtype);
                        for (String codeIndex : codeAndTimeList.keySet()) {
                            if (codeAndPollutant != null && codeAndPollutant.containsKey(codeIndex)) {
                                timeList = codeAndTimeList.get(codeIndex);
                                Collections.sort(timeList);
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("pollutantcode", codeIndex);
                                resultMap.put("pollutantname", codeAndPollutant.get(codeIndex).toString().split(",")[0]);
                                resultMap.put("timelist", timeList);
                                resultList.add(resultMap);
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取企业报警时段数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/25 11:00
     */
    @RequestMapping(value = "getEntOverTimeListByParam", method = RequestMethod.POST)
    public Object getEntOverTimeListByParam(
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //获取企业下关联的mn号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
            if (pointList.size() > 0) {
                String mnCommon;
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointMap : pointList) {
                    if (pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null
                            && monitorpointtype == Integer.parseInt(pointMap.get("monitorpointtype").toString())) {
                        mnCommon = pointMap.get("dgimn").toString();
                        mns.add(mnCommon);
                    }
                }
                paramMap.clear();
                paramMap.put("starttime", starttime + " 00:00:00");
                paramMap.put("endtime", endtime + " 23:59:59");
                paramMap.put("mns", mns);
                //报警统计
                paramMap.put("timeKey", "OverTime");
                paramMap.put("collection", DB_OverData);
                List<Document> overList = getOverOrExceptionByParam(paramMap);
                Map<String,Integer> timeAndNum = new HashMap<>();
                if (overList.size() > 0) {
                    String time;
                    for (Document document : overList) {
                        time =DataFormatUtil.getDateHourNum(document.getDate("OverTime"))+"时" ;
                        if (timeAndNum.containsKey(time)){
                            timeAndNum.put(time,timeAndNum.get(time)+1);
                        }else {
                            timeAndNum.put(time,1);
                        }
                    }
                }
                for (String timeIndex:timeAndNum.keySet()){
                    Map<String,Object> resultMap = new HashMap<>();
                    resultMap.put("hourtime",timeIndex);
                    resultMap.put("countnum",timeAndNum.get(timeIndex));
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
     * @Description: 获取企业报警统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/21 8:45
     */
    @RequestMapping(value = "getEntAlarmCountData", method = RequestMethod.POST)
    public Object getEntAlarmCountData(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "remindtypelist", required = false) List<Integer> remindtypelist) {
        try {
            Map<String, Object> dataMap = new HashMap<>();

            //获取企业下关联的mn号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);

            List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
            if (pointList.size() > 0) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                Map<Integer, Integer> remindAndNum = new HashMap<>();
                Map<String, Integer> mnAndType = new HashMap<>();
                Map<String, String> mnAndId = new HashMap<>();
                Map<String, String> mnAndName = new HashMap<>();
                String mnCommon;
                Integer type;
                List<Integer> typeList = new ArrayList<>();
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointMap : pointList) {
                    if (pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null) {
                        mnCommon = pointMap.get("dgimn").toString();
                        type = Integer.parseInt(pointMap.get("monitorpointtype").toString());
                        mnAndType.put(mnCommon, type);
                        mnAndId.put(mnCommon, pointMap.get("monitorpointid").toString());
                        mnAndName.put(mnCommon, pointMap.get("monitorpointname").toString());
                        mns.add(mnCommon);
                        typeList.add(type);
                    }
                }
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                if (remindtypelist == null || remindtypelist.size() == 0) {
                    remindtypelist = Arrays.asList(ConcentrationChangeEnum.getCode(),
                            EarlyAlarmEnum.getCode(),
                            ExceptionAlarmEnum.getCode(),
                            OverAlarmEnum.getCode());
                }

                Map<String, List<Document>> mnAndPollutantList;
                for (Integer remindType : remindtypelist) {
                    mnAndPollutantList = getRemindTypeAndDataList(remindType, paramMap);
                    if (mnAndPollutantList.size() > 0) {
                        paramMap.put("pollutanttypes", typeList);
                        List<Map<String, Object>> pollutantDList = pollutantService.getPollutantsByCodesAndType(paramMap);
                        Map<Integer, Map<String, Object>> typeAndCodeAndPollutant = getPollutantMap(pollutantDList);
                        List<Document> pollutantList;
                        Date maxTime = DataFormatUtil.getDateYMDHMS(starttime);
                        Date monitortime;
                        Object monitorValue;
                        String pollutantCode;
                        Integer monitorType;
                        String monitorpointid;
                        for (String mnIndex : mnAndPollutantList.keySet()) {
                            Map<String, Object> resultMap = new HashMap<>();
                            monitorpointid = mnAndId.get(mnIndex);
                            resultMap.put("monitorpointid", monitorpointid);
                            resultMap.put("dgimn", mnIndex);
                            resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                            monitorType = mnAndType.get(mnIndex);
                            resultMap.put("monitorpointtypecode", monitorType);
                            resultMap.put("monitorpointtypename", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnIndex)).replaceAll("监测点", ""));
                            resultMap.put("remindtype", remindType);
                            pollutantList = mnAndPollutantList.get(mnIndex);
                            Map<String, Map<String, Object>> codeAndTimeAndValue = new HashMap<>();
                            Map<String, Object> timeAndValue;
                            String time;
                            for (Document pollutant : pollutantList) {
                                pollutantCode = pollutant.getString("PollutantCode");
                                if (remindType == ConcentrationChangeEnum.getCode()) {
                                    monitortime = pollutant.getDate("MonitorTime");
                                    if (monitortime.after(maxTime)) {
                                        maxTime = monitortime;
                                    }
                                    monitorValue = pollutant.get("AvgStrength") + ","
                                            + DataFormatUtil.SaveOneAndSubZero(pollutant.getDouble("ChangeMultiple"));
                                    if (codeAndTimeAndValue.containsKey(pollutantCode)) {
                                        timeAndValue = codeAndTimeAndValue.get(pollutantCode);
                                    } else {
                                        timeAndValue = new HashMap<>();
                                    }
                                    timeAndValue.put(DataFormatUtil.getDateHM(monitortime), monitorValue);
                                    codeAndTimeAndValue.put(pollutantCode, timeAndValue);
                                } else if (remindType == EarlyAlarmEnum.getCode() || remindType == OverAlarmEnum.getCode()) {
                                    monitortime = pollutant.getDate("LastOverTime");
                                    if (monitortime.after(maxTime)) {
                                        maxTime = monitortime;
                                    }
                                    monitorValue = pollutant.get("LastMonitorValue") + "," + pollutant.get("OverTime");
                                    if (codeAndTimeAndValue.containsKey(pollutantCode)) {
                                        timeAndValue = codeAndTimeAndValue.get(pollutantCode);
                                    } else {
                                        timeAndValue = new HashMap<>();
                                    }
                                    time = DataFormatUtil.getDateHM(pollutant.getDate("FirstOverTime")) + "~" + DataFormatUtil.getDateHM(monitortime);
                                    timeAndValue.put(time, monitorValue);
                                    codeAndTimeAndValue.put(pollutantCode, timeAndValue);
                                } else {
                                    monitortime = pollutant.getDate("LastExceptionTime");
                                    if (monitortime.after(maxTime)) {
                                        maxTime = monitortime;
                                    }
                                    monitorValue = pollutant.get("MonitorValue") + "," + pollutant.get("ExceptionTime") + "," + pollutant.get("ExceptionType");
                                    if (codeAndTimeAndValue.containsKey(pollutantCode)) {
                                        timeAndValue = codeAndTimeAndValue.get(pollutantCode);
                                    } else {
                                        timeAndValue = new HashMap<>();
                                    }
                                    time = DataFormatUtil.getDateHM(pollutant.getDate("FirstExceptionTime")) + "~" + DataFormatUtil.getDateHM(monitortime);
                                    timeAndValue.put(time, monitorValue);
                                    codeAndTimeAndValue.put(pollutantCode, timeAndValue);
                                }
                            }
                            resultMap.put("monitortime", DataFormatUtil.getDateYMDHMS(maxTime));
                            if (remindAndNum.containsKey(remindType)) {
                                remindAndNum.put(remindType, remindAndNum.get(remindType) + 1);
                            } else {
                                remindAndNum.put(remindType, 1);
                            }

                            Map<String, Object> codeAndPollutant = typeAndCodeAndPollutant.get(mnAndType.get(mnIndex));
                            if (remindType == ConcentrationChangeEnum.getCode()) {
                                setTBData(resultMap, codeAndPollutant, codeAndTimeAndValue, resultList);
                            } else if (remindType == EarlyAlarmEnum.getCode()) {
                                setEarlyAlarmData(monitorpointid, monitorType, resultMap, codeAndPollutant, codeAndTimeAndValue, resultList);
                            } else if (remindType == OverAlarmEnum.getCode()) {
                                setOverAlarmData(monitorpointid, monitorType, resultMap, codeAndPollutant, codeAndTimeAndValue, resultList);
                            } else {
                                setExceptionData(resultMap, codeAndPollutant, codeAndTimeAndValue, resultList);
                            }
                        }
                    }
                }
                //分页+排序
                if (resultList.size() > 0) {
                    resultList = resultList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
                    List<Map<String, Object>> subDataList;
                    if (pagenum != null && pagesize != null) {
                        subDataList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                    } else {
                        subDataList = resultList;
                    }
                    dataMap.put("datalist", subDataList);
                    dataMap.put("total", resultList.size());
                    List<Map<String, Object>> remindList = new ArrayList<>();
                    for (Integer remind : remindAndNum.keySet()) {
                        Map<String, Object> remindMap = new HashMap<>();
                        remindMap.put("remindcode", remind);
                        remindMap.put("remindname", CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind).getName());
                        remindMap.put("remindnum", remindAndNum.get(remind));
                        remindList.add(remindMap);
                    }
                    dataMap.put("counttype", remindList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void setExceptionData(Map<String, Object> resultMap, Map<String, Object> codeAndPollutant, Map<String, Map<String, Object>> codeAndTimeAndValue, List<Map<String, Object>> resultList) {
        if (codeAndTimeAndValue.size() > 0) {//异常数据
            List<Map<String, Object>> pollutantlist = new ArrayList<>();
            String value = "";
            Map<String, Object> timeAndValue;
            String exceptionType = "";
            for (String codeIndex : codeAndTimeAndValue.keySet()) {
                if (codeAndPollutant != null && codeAndPollutant.containsKey(codeIndex)) {
                    Map<String, Object> pollutant = new HashMap<>();
                    pollutant.put("pollutantcode", codeIndex);
                    pollutant.put("pollutantname", codeAndPollutant.get(codeIndex).toString().split(",")[0]);
                    pollutant.put("pollutantunit", codeAndPollutant.get(codeIndex).toString().split(",")[1]);
                    pollutant.put("orderIndex", codeAndPollutant.get(codeIndex).toString().split(",")[2]);
                    timeAndValue = codeAndTimeAndValue.get(codeIndex);
                    List<String> timeList = new ArrayList<>();
                    List<String> intervalTimeList = new ArrayList<>();
                    timeAndValue = DataFormatUtil.sortByKey(timeAndValue, false);
                    for (String timeIndex : timeAndValue.keySet()) {
                        timeList.add(timeIndex);
                        value = timeAndValue.get(timeIndex).toString();
                        exceptionType = value.split(",")[2];
                        intervalTimeList.add(value.split(",")[1]);
                    }
                    pollutant.put("valuemark", "【" + CommonTypeEnum.ExceptionTypeEnum.getNameByCode(exceptionType) + "】");
                    pollutant.put("monitorvalue", value.split(",")[0]);
                    pollutant.put("timeList", timeList);
                    pollutant.put("intervaltime", intervalTimeList);
                    pollutantlist.add(pollutant);
                }
            }
            pollutantlist = pollutantlist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderIndex").toString()))).collect(Collectors.toList());
            resultMap.put("pollutantlist", pollutantlist);
            resultList.add(resultMap);
        }

    }

    private void setOverAlarmData(String monitorpointid, Integer monitorType, Map<String, Object> resultMap, Map<String, Object> codeAndPollutant, Map<String, Map<String, Object>> codeAndTimeAndValue, List<Map<String, Object>> resultList) {
        if (codeAndTimeAndValue.size() > 0) {//超预警
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorType);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("alarmlevel", 0);
            Map<String, Object> codeAndStandardValue = new HashMap<>();
            List<Map<String, Object>> standValues = pollutantService.getEarlyAndStandardValueByParams(paramMap);
            if (standValues.size() > 0) {
                String pollutantCode;
                Double standV;
                for (Map<String, Object> standValue : standValues) {
                    pollutantCode = standValue.get("FK_PollutantCode").toString();
                    if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(standValue.get("AlarmType") + "")) {
                        if (standValue.get("StandardMaxValue") != null) {
                            standV = Double.parseDouble(standValue.get("StandardMaxValue").toString());
                            codeAndStandardValue.put(pollutantCode, DataFormatUtil.SaveOneAndSubZero(standV));
                        }
                    } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(standValue.get("AlarmType") + "")) {
                        if (standValue.get("StandardMinValue") != null) {
                            standV = Double.parseDouble(standValue.get("StandardMinValue").toString());
                            codeAndStandardValue.put(pollutantCode, DataFormatUtil.SaveOneAndSubZero(standV));
                        }
                    } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(standValue.get("AlarmType") + "")) {
                        if (standValue.get("StandardMinValue") != null && standValue.get("StandardMaxValue") != null) {
                            Double standMin = Double.parseDouble(standValue.get("StandardMinValue").toString());
                            Double standMax = Double.parseDouble(standValue.get("StandardMaxValue").toString());
                            codeAndStandardValue.put(pollutantCode, DataFormatUtil.SaveOneAndSubZero(standMin) + "-" + DataFormatUtil.SaveOneAndSubZero(standMax));
                        }
                    }
                }
            }
            List<Map<String, Object>> pollutantlist = new ArrayList<>();
            String value = "";
            Map<String, Object> timeAndValue;
            for (String codeIndex : codeAndTimeAndValue.keySet()) {
                if (codeAndPollutant != null && codeAndPollutant.containsKey(codeIndex)) {
                    Map<String, Object> pollutant = new HashMap<>();
                    pollutant.put("pollutantcode", codeIndex);
                    pollutant.put("pollutantname", codeAndPollutant.get(codeIndex).toString().split(",")[0]);
                    pollutant.put("pollutantunit", codeAndPollutant.get(codeIndex).toString().split(",")[1]);
                    pollutant.put("orderIndex", codeAndPollutant.get(codeIndex).toString().split(",")[2]);
                    timeAndValue = codeAndTimeAndValue.get(codeIndex);
                    List<String> timeList = new ArrayList<>();
                    List<String> intervalTimeList = new ArrayList<>();
                    timeAndValue = DataFormatUtil.sortByKey(timeAndValue, false);
                    for (String timeIndex : timeAndValue.keySet()) {
                        timeList.add(timeIndex);
                        value = timeAndValue.get(timeIndex).toString();
                        intervalTimeList.add(value.split(",")[1]);
                    }
                    pollutant.put("valuemark", "【标准值：" + codeAndStandardValue.get(codeIndex) + "】");
                    pollutant.put("monitorvalue", value.split(",")[0]);
                    pollutant.put("timeList", timeList);
                    pollutant.put("intervaltime", intervalTimeList);
                    pollutantlist.add(pollutant);
                }

            }
            pollutantlist = pollutantlist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderIndex").toString()))).collect(Collectors.toList());
            resultMap.put("pollutantlist", pollutantlist);
            resultList.add(resultMap);
        }

    }

    private void setEarlyAlarmData(String monitorpointid, Integer monitorType, Map<String, Object> resultMap, Map<String, Object> codeAndPollutant, Map<String, Map<String, Object>> codeAndTimeAndValue, List<Map<String, Object>> resultList) {
        if (codeAndTimeAndValue.size() > 0) {//超预警
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorType);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("alarmlevel", 0);
            Map<String, Object> codeAndStandardValue = new HashMap<>();
            List<Map<String, Object>> standValues = pollutantService.getEarlyAndStandardValueByParams(paramMap);
            if (standValues.size() > 0) {
                String pollutantCode;
                Double standV;
                for (Map<String, Object> standValue : standValues) {
                    pollutantCode = standValue.get("FK_PollutantCode").toString();
                    if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(standValue.get("AlarmType") + "")) {
                        if (standValue.get("ConcenAlarmMaxValue") != null) {
                            standV = Double.parseDouble(standValue.get("ConcenAlarmMaxValue").toString());
                            codeAndStandardValue.put(pollutantCode, DataFormatUtil.SaveOneAndSubZero(standV));
                        }
                    } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(standValue.get("AlarmType") + "")) {
                        if (standValue.get("ConcenAlarmMinValue") != null) {
                            standV = Double.parseDouble(standValue.get("ConcenAlarmMinValue").toString());
                            codeAndStandardValue.put(pollutantCode, DataFormatUtil.SaveOneAndSubZero(standV));
                        }
                    } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(standValue.get("AlarmType") + "")) {
                        if (standValue.get("ConcenAlarmMinValue") != null && standValue.get("ConcenAlarmMaxValue") != null) {
                            Double standMin = Double.parseDouble(standValue.get("ConcenAlarmMinValue").toString());
                            Double standMax = Double.parseDouble(standValue.get("ConcenAlarmMaxValue").toString());
                            codeAndStandardValue.put(pollutantCode, DataFormatUtil.SaveOneAndSubZero(standMin) + "-" + DataFormatUtil.SaveOneAndSubZero(standMax));
                        }
                    }
                }
            }
            List<Map<String, Object>> pollutantlist = new ArrayList<>();
            String value = "";
            Map<String, Object> timeAndValue;
            for (String codeIndex : codeAndTimeAndValue.keySet()) {
                if (codeAndPollutant != null && codeAndPollutant.containsKey(codeIndex)) {
                    Map<String, Object> pollutant = new HashMap<>();
                    pollutant.put("pollutantcode", codeIndex);
                    pollutant.put("pollutantname", codeAndPollutant.get(codeIndex).toString().split(",")[0]);
                    pollutant.put("pollutantunit", codeAndPollutant.get(codeIndex).toString().split(",")[1]);
                    pollutant.put("orderIndex", codeAndPollutant.get(codeIndex).toString().split(",")[2]);
                    timeAndValue = codeAndTimeAndValue.get(codeIndex);
                    List<String> timeList = new ArrayList<>();
                    List<String> intervalTimeList = new ArrayList<>();
                    timeAndValue = DataFormatUtil.sortByKey(timeAndValue, false);
                    for (String timeIndex : timeAndValue.keySet()) {
                        timeList.add(timeIndex);
                        value = timeAndValue.get(timeIndex).toString();
                        intervalTimeList.add(value.split(",")[1]);
                    }
                    pollutant.put("valuemark", "【预警值：" + codeAndStandardValue.get(codeIndex) + "】");
                    pollutant.put("monitorvalue", value.split(",")[0]);
                    pollutant.put("timeList", timeList);
                    pollutant.put("intervaltime", intervalTimeList);
                    pollutantlist.add(pollutant);
                }

            }
            pollutantlist = pollutantlist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderIndex").toString()))).collect(Collectors.toList());
            resultMap.put("pollutantlist", pollutantlist);
            resultList.add(resultMap);
        }
    }

    private void setTBData(Map<String, Object> resultMap, Map<String, Object> codeAndPollutant, Map<String, Map<String, Object>> codeAndTimeAndValue, List<Map<String, Object>> resultList) {
        if (codeAndTimeAndValue.size() > 0) {//浓度突变
            List<Map<String, Object>> pollutantlist = new ArrayList<>();
            String value = "";
            Map<String, Object> timeAndValue;
            for (String codeIndex : codeAndTimeAndValue.keySet()) {
                if (codeAndPollutant != null && codeAndPollutant.containsKey(codeIndex)) {
                    Map<String, Object> pollutant = new HashMap<>();
                    pollutant.put("pollutantcode", codeIndex);
                    pollutant.put("pollutantname", codeAndPollutant.get(codeIndex).toString().split(",")[0]);
                    pollutant.put("pollutantunit", codeAndPollutant.get(codeIndex).toString().split(",")[1]);
                    pollutant.put("orderIndex", codeAndPollutant.get(codeIndex).toString().split(",")[2]);
                    timeAndValue = codeAndTimeAndValue.get(codeIndex);
                    List<String> timeList = new ArrayList<>();
                    timeAndValue = DataFormatUtil.sortByKey(timeAndValue, false);
                    for (String timeIndex : timeAndValue.keySet()) {
                        timeList.add(timeIndex);
                        value = timeAndValue.get(timeIndex).toString();
                    }
                    pollutant.put("valuemark", "【突变倍数：" + value.split(",")[1] + "】");
                    pollutant.put("monitorvalue", value.split(",")[0]);
                    pollutant.put("timeList", timeList);
                    pollutant.put("intervaltime", "");
                    pollutantlist.add(pollutant);
                }

            }
            //排序
            pollutantlist = pollutantlist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderIndex").toString()))).collect(Collectors.toList());
            resultMap.put("pollutantlist", pollutantlist);
            resultList.add(resultMap);
        }
    }

    private Map<String, List<Document>> getRemindTypeAndDataList(Integer remindType, Map<String, Object> paramMap) {
        Map<String, List<Document>> mnAndPollutantList = new HashMap<>();

        List<Document> documents = new ArrayList<>();
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindType)) {
            case ConcentrationChangeEnum:
                paramMap.put("collection", DB_MinuteData);
                paramMap.put("mnKey", "DataGatherCode");
                paramMap.put("timeKey", "MonitorTime");
                paramMap.put("alarmLevel", null);
                documents = getMonitorDataByParam(paramMap);
                if (documents.size() > 0) {
                    List<Document> dataList = new ArrayList<>();
                    List<Document> pollutantList;
                    for (Document document : documents) {
                        pollutantList = document.get("MinuteDataList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.get("IsSuddenChange") != null && pollutant.getBoolean("IsSuddenChange")) {
                                pollutant.put("MN", document.get("DataGatherCode"));
                                pollutant.put("MonitorTime", document.get("MonitorTime"));
                                dataList.add(pollutant);
                            }
                        }
                    }
                    documents = dataList;
                }

                break;
            case EarlyAlarmEnum:
                paramMap.put("collection", DB_OverModel);
                paramMap.put("mnKey", "MN");
                paramMap.put("timeKey", "FirstOverTime");
                paramMap.put("alarmLevel", 0);
                documents = getMonitorDataByParam(paramMap);
                break;
            case OverAlarmEnum:
                paramMap.put("collection", DB_OverModel);
                paramMap.put("mnKey", "MN");
                paramMap.put("timeKey", "FirstOverTime");
                paramMap.put("alarmLevel", -1);
                documents = getMonitorDataByParam(paramMap);
                break;
            case ExceptionAlarmEnum:
                paramMap.put("collection", DB_ExceptionModel);
                paramMap.put("mnKey", "MN");
                paramMap.put("timeKey", "FirstExceptionTime");
                paramMap.put("alarmLevel", null);
                documents = getMonitorDataByParam(paramMap);
                break;
        }
        if (documents.size() > 0) {
            List<Document> pollutantList;
            String mnCommon;
            for (Document document : documents) {
                mnCommon = document.getString("MN");
                if (mnAndPollutantList.containsKey(mnCommon)) {
                    pollutantList = mnAndPollutantList.get(mnCommon);
                } else {
                    pollutantList = new ArrayList<>();
                }
                pollutantList.add(document);
                mnAndPollutantList.put(mnCommon, pollutantList);
            }
        }
        return mnAndPollutantList;
    }


    /**
     * @Description: 获取企业最新实时数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/21 8:45
     */
    @RequestMapping(value = "getEntLastData", method = RequestMethod.POST)
    public Object getEntAlarmTimeData(
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "statuslist") List<Integer> statuslist,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            //获取企业下关联的mn号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("statuslist", statuslist);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> pointList = pollutionService.getPointListByParam(paramMap);
            if (pointList.size() > 0) {
                Map<String, Object> countMap = new HashMap<>();
                countMap.put("totalnum", pointList.size());
                int normalnum = 0;
                int offnum = 0;
                int overnum = 0;
                int exceptionnum = 0;
                String status;
                Map<String, Integer> mnAndType = new HashMap<>();
                Map<String, String> mnAndId = new HashMap<>();
                Map<String, String> mnAndName = new HashMap<>();
                Map<String, String> mnAndStatus = new HashMap<>();
                String mnCommon;
                Integer type;
                List<Integer> typeList = new ArrayList<>();
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointMap : pointList) {
                    if (pointMap.get("status") != null && pointMap.get("dgimn") != null && pointMap.get("monitorpointtype") != null) {
                        status = pointMap.get("status").toString();
                        if (status.equals(CommonTypeEnum.OnlineStatusEnum.NormalStatusEnum.getCode())) {
                            normalnum++;
                        } else if (status.equals(CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode())) {
                            offnum++;
                        } else if (status.equals(CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode())) {
                            overnum++;
                        } else if (status.equals(CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum.getCode())) {
                            exceptionnum++;
                        }
                        mnCommon = pointMap.get("dgimn").toString();
                        type = Integer.parseInt(pointMap.get("monitorpointtype").toString());
                        mnAndType.put(mnCommon, type);
                        mnAndId.put(mnCommon, pointMap.get("monitorpointid").toString());
                        mnAndName.put(mnCommon, pointMap.get("monitorpointname").toString());
                        mnAndStatus.put(mnCommon, status);
                        mns.add(mnCommon);
                        typeList.add(type);
                    }
                }
                countMap.put("normalnum", normalnum);
                countMap.put("offnum", offnum);
                countMap.put("overnum", overnum);
                countMap.put("exceptionnum", exceptionnum);

                resultMap.put("countMap", countMap);
                paramMap.clear();
                paramMap.put("mns", mns);
                List<Document> lastDataList = getLastDataByParam(paramMap);
                List<Map<String, Object>> dataList = new ArrayList<>();

                if (lastDataList.size() > 0) {
                    paramMap.put("pollutanttypes", typeList);
                    List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
                    Map<Integer, Map<String, Object>> typeAndCodeAndPollutant = getPollutantMap(pollutantList);
                    List<Document> docList;
                    String code;
                    Map<String, Object> codeAndName;
                    for (Document document : lastDataList) {
                        Map<String, Object> dataMap = new HashMap<>();
                        mnCommon = document.getString("DataGatherCode");
                        dataMap.put("monitorpointid", mnAndId.get(mnCommon));
                        dataMap.put("monitorpointname", mnAndName.get(mnCommon));
                        type = mnAndType.get(mnCommon);
                        dataMap.put("monitorpointtype", type);
                        dataMap.put("dgimn", mnCommon);
                        dataMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                        dataMap.put("status", mnAndStatus.get(mnCommon));
                        dataMap.put("dgimn", mnCommon);
                        docList = document.get("DataList", List.class);
                        List<Map<String, Object>> pollutantlist = new ArrayList<>();
                        for (Document doc : docList) {
                            Map<String, Object> pollutantMap = new HashMap<>();
                            code = doc.getString("PollutantCode");
                            codeAndName = typeAndCodeAndPollutant.get(type);
                            if (codeAndName != null && codeAndName.get(code) != null) {
                                pollutantMap.put("pollutantcode", code);
                                pollutantMap.put("pollutantname", codeAndName.get(code).toString().split(",")[0]);
                                pollutantMap.put("pollutantunit", codeAndName.get(code).toString().split(",")[1]);
                                pollutantMap.put("orderIndex", codeAndName.get(code).toString().split(",")[2]);
                                pollutantMap.put("value", doc.get("AvgStrength"));
                                pollutantMap.put("isover", doc.get("IsOver"));
                                pollutantMap.put("isexception", doc.get("IsException"));
                                pollutantMap.put("isoverstandard", doc.get("IsOverStandard"));
                                pollutantlist.add(pollutantMap);
                            }
                        }
                        //排序
                        pollutantlist = pollutantlist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderIndex").toString()))).collect(Collectors.toList());
                        dataMap.put("pollutantlist", pollutantlist);
                        dataList.add(dataMap);
                    }
                }
                //排序
                dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("status").toString())).reversed()).collect(Collectors.toList());
                resultMap.put("datalist", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<Integer, Map<String, Object>> getPollutantMap(List<Map<String, Object>> pollutantList) {
        int type;
        String code;
        String name;
        String unit;
        Integer orderindex;
        Map<Integer, Map<String, Object>> typeAndCodeAndPollutant = new HashMap<>();

        for (Map<String, Object> pollutant : pollutantList) {
            code = pollutant.get("code").toString();
            name = pollutant.get("name").toString();
            unit = pollutant.get("unit") != null ? pollutant.get("unit").toString() : "";
            orderindex = pollutant.get("orderindex") != null ? Integer.parseInt(pollutant.get("orderindex").toString()) : 9999;
            type = Integer.parseInt(pollutant.get("pollutanttype").toString());
            if (typeAndCodeAndPollutant.containsKey(type)) {
                typeAndCodeAndPollutant.get(type).put(code, name + "," + unit + "," + orderindex);
            } else {
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put(code, name + "," + unit + "," + orderindex);
                typeAndCodeAndPollutant.put(type, pollutantMap);
            }

        }
        return typeAndCodeAndPollutant;

    }

    private List<Document> getMonitorDataByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String mnKey = (String) paramMap.get("mnKey");
        String timeKey = (String) paramMap.get("timeKey");
        Criteria criteria;
        if (paramMap.get("alarmLevel") != null && Integer.parseInt(paramMap.get("alarmLevel").toString()) == 0) {
            criteria = Criteria.where(mnKey).in(mns).and(timeKey).gte(startTime).lte(endTime).and("AlarmLevel").is(paramMap.get("alarmLevel"));
        } else if (paramMap.get("alarmLevel") != null && Integer.parseInt(paramMap.get("alarmLevel").toString()) != 0) {
            criteria = Criteria.where(mnKey).in(mns).and(timeKey).gte(startTime).lte(endTime).ne(0);
        } else if (paramMap.get("collection").equals(DB_MinuteData)) {
            criteria = Criteria.where(mnKey).in(mns).and(timeKey).gte(startTime).lte(endTime).and("MinuteDataList.IsSuddenChange").is(true);
        } else {
            criteria = Criteria.where(mnKey).in(mns).and(timeKey).gte(startTime).lte(endTime);
        }
        aggregations.add(match(criteria));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }


    private List<Document> getLastDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("Type").is("RealTimeData");
        aggregations.add(match(criteria));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, DB_LatestData, Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }


    private List<Document> countMnOverOrExceptionByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        aggregations.add(match(criteria));
        GroupOperation groupOperation = group("DataGatherCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    private List<Document> getOverOrExceptionByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("DataType").is("MinuteData");
        aggregations.add(match(criteria));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

}
