package com.tjpu.sp.controller.environmentalprotection.alarm;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import com.tjpu.sp.model.common.mongodb.OverDataVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.AlarmLevelService;
import com.tjpu.sp.service.common.pubcode.PollutantCommonService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.EntDevOpsInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.*;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import com.tjpu.sp.service.impl.environmentalprotection.online.OnlineServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AlarmMenus.getNameByString;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AlarmMonitorTypeMenu.getMenuCodeByType;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeCodeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.FlowChangeEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:11
 * @Description: 超标告警数据控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("overAlarmData")
public class OverAlarmController {


    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private AlarmLevelService alarmLevelService;

    @Autowired
    private OnlineMonitorService onlineMonitorService;
    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    private final OnlineDataCountService onlineDataCountService;

    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;


    @Autowired
    private EffectiveTransmissionService effectiveTransmissionService;

    @Autowired
    private EntDevOpsInfoService entDevOpsInfoService;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String earlyWarnDataCollect = "EarlyWarnData";
    private String overDataCollect = "OverData";
    private String exceptionDataCollect = "ExceptionData";
    private String hourDataCollect = "HourData";
    private String DB_OverModel = "OverModel";

    private String dayDataCollect = "DayData";
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private PollutantCommonService pollutantCommonService;
    @Autowired
    private DeviceStatusService deviceStatusService;

    public OverAlarmController(OnlineDataCountService onlineDataCountService) {
        this.onlineDataCountService = onlineDataCountService;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 上午 9:44
     * @Description: 通过排口类型获取近一个月污染源超标报警排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "/getLastMonthAlarmRankByType", method = RequestMethod.POST)
    public Object getLastMonthAlarmRankByType(@RequestJson(value = "type", required = true) Integer type) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("type", type);
            if (type == WasteWaterEnum.getCode()) {
                paramMap.put("outputtype", 1);
                data = pollutionService.getWaterOutPutInfoByPollutionid(paramMap);
            } else if (type == WasteGasEnum.getCode() || type == SmokeEnum.getCode()) {
                data = pollutionService.getGasOutPutInfoByPollutionid(paramMap);
            }
            List<String> collect1 = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            paramMap.clear();
            paramMap.put("dgimns", collect1);
            List<Map> maps = onlineCountAlarmService.countLastMonthAlarmByParamMap(paramMap);
            for (Map map : maps) {
                String dataGatherCode = map.get("_id") == null ? "" : map.get("_id").toString();
                for (Map<String, Object> datum : data) {
                    String dgimn = datum.get("DGIMN") == null ? "" : datum.get("DGIMN").toString();
                    if (dgimn.equals(dataGatherCode)) {
                        map.put("pollutionid", datum.get("fk_pollutionid"));
                        map.put("pollutantname", datum.get("PollutionName"));
                    }
                }
            }
            Map<String, List<Map>> collect = maps.stream().filter(m -> m.get("pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("pollutionid").toString()));
            for (String key : collect.keySet()) {
                List<Map> list = collect.get(key);
                Integer count = list.stream().filter(m -> m.get("count") != null).map(m -> Integer.valueOf(m.get("count").toString())).collect(Collectors.summingInt(m -> m));
                if (list.size() > 0) {
                    Map map = list.get(0);
                    map.put("count", count);
                    resultList.add(map);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("count") != null).sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("count").toString())).reversed()).collect(Collectors.toList()));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/12/16 0016 上午 10:50
     * @Description: 获取当前报警及传输率数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getAlarmTransmissionDataByParam", method = RequestMethod.POST)
    public Object getAlarmTransmissionDataByParam(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "monitortime") String monitortime

    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();


            List<Map<String, Object>> pointDataList = pollutionService.getHBPointDataList();

            if (pointDataList.size() > 0) {
                Map<String, Map<String, Object>> mnAndPointData = new HashMap<>();
                List<String> userdgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
                String mnCommon;
                int type;
                Map<String, Integer> mnAndType = new HashMap<>();
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("dgimn") != null) {
                        mnCommon = pointData.get("dgimn").toString();
                        type = Integer.parseInt(pointData.get("monitorpointtype") + "");
                        if (userdgimns.contains(mnCommon) && monitorpointtypes.contains(type)) {
                            mnAndType.put(mnCommon, type);
                            mnAndPointData.put(mnCommon, pointData);
                            mns.add(mnCommon);
                        }
                    }
                }
                String startTime = monitortime + " 00:00:00";
                String endTime = monitortime + " 23:59:59";
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("starttime", startTime);
                paramMap.put("endtime", endTime);
                paramMap.put("mns", mns);

                List<Integer> alarmtypes = Arrays.asList(CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                        CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());

                List<Document> alarmList = new ArrayList<>();
                for (Integer alarmType : alarmtypes) {
                    setAlarmTypeAndAlarmList(alarmType, paramMap, alarmList);
                }
                //传输率数据
                Map<Integer, Object> typeAndTransmissionData = new HashMap<>();
                Map<String, Object> estMap = new HashMap<>();

                for (Integer typeIndex : monitorpointtypes) {
                    estMap.put("starttime", monitortime);
                    estMap.put("endtime", monitortime);
                    estMap.put("monitorpointtype", typeIndex);
                    List<Map<String, Object>> estData = effectiveTransmissionService.getEffectiveTransmissionByParamMap(estMap);
                    typeAndTransmissionData.put(typeIndex, getTransmissionData(estData));
                }
                Map<Integer, List<Map<String, Object>>> typeAndAlarmDataList = new HashMap<>();
                if (alarmList.size() > 0) {

                    paramMap.put("pollutanttypes", monitorpointtypes);
                    List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                    Map<String, Object> codeAndName = setCodeAndName(pollutants);

                    Map<Integer, List<Document>> typeAndAlarmList = new HashMap<>();
                    List<Document> subDocs;
                    for (Document document : alarmList) {
                        mnCommon = document.getString("DataGatherCode");
                        type = mnAndType.get(mnCommon);
                        if (typeAndAlarmList.containsKey(type)) {
                            subDocs = typeAndAlarmList.get(type);
                        } else {
                            subDocs = new ArrayList<>();
                        }
                        subDocs.add(document);
                        typeAndAlarmList.put(type, subDocs);
                    }
                    String key;
                    String maxtime;
                    String alarmtype;
                    Map<String, List<Document>> keyAndPollutants = new HashMap<>();
                    List<Document> pollutantDocs;
                    for (Integer typeIndex : typeAndAlarmList.keySet()) {
                        List<Map<String, Object>> alarmDataList = new ArrayList<>();
                        subDocs = typeAndAlarmList.get(typeIndex);
                        keyAndPollutants.clear();
                        for (Document document : subDocs) {
                            mnCommon = document.getString("DataGatherCode");
                            maxtime = DataFormatUtil.getDateHMS(document.getDate("maxtime"));
                            alarmtype = document.get("alarmtype").toString();
                            key = mnCommon + "," + maxtime + "," + alarmtype;
                            if (keyAndPollutants.containsKey(key)) {
                                pollutantDocs = keyAndPollutants.get(key);
                            } else {
                                pollutantDocs = new ArrayList<>();
                            }
                            pollutantDocs.add(document);
                            keyAndPollutants.put(key, pollutantDocs);
                        }
                        for (String keyIndex : keyAndPollutants.keySet()) {
                            maxtime = keyIndex.split(",")[1];
                            alarmtype = keyIndex.split(",")[2];
                            mnCommon = keyIndex.split(",")[0];

                            Map<String, Object> dataMap = new HashMap<>();

                            dataMap.put("monitortime", maxtime);
                            dataMap.put("alarmtype", alarmtype);
                            dataMap.putAll(mnAndPointData.get(mnCommon));
                            pollutantDocs = keyAndPollutants.get(keyIndex);
                            Set<Object> pollutantname = new HashSet<>();
                            String alarmname = "";
                            if ("1".equals(alarmtype)) {
                                alarmname = "浓度突变";
                            } else if ("4".equals(alarmtype)) {
                                alarmname = "异常";
                            } else if ("5".equals(alarmtype)) {
                                alarmname = "超标";
                            }

                            for (Document pollutant : pollutantDocs) {
                                if (codeAndName.containsKey(pollutant.get("PollutantCode"))) {
                                    if ("4".equals(alarmtype)) {
                                        String excname = "";
                                        if (pollutant.get("exceptiontypes") != null) {
                                            Set<String> exceptiontypelist = new HashSet<>();
                                            List<Document> extypelist = (List<Document>) pollutant.get("exceptiontypes");
                                            for (Document doc : extypelist) {
                                                if (!exceptiontypelist.contains(doc.getString("type"))) {
                                                    if ("1".equals(doc.getString("type"))) {
                                                        excname = excname + "零值、";
                                                    }
                                                    if ("2".equals(doc.getString("type"))) {
                                                        excname = excname + "恒值、";
                                                    }
                                                    if ("7".equals(doc.getString("type"))) {
                                                        excname = excname + "无流量、";
                                                    }
                                                    exceptiontypelist.add(doc.getString("type"));
                                                } else {
                                                    continue;
                                                }
                                            }
                                            if (!"".equals(excname)) {
                                                excname = excname.substring(0, excname.length() - 1);
                                            }
                                        }
                                        pollutantname.add(codeAndName.get(pollutant.get("PollutantCode")) + "(" + excname + ")");
                                    } else {
                                        pollutantname.add(codeAndName.get(pollutant.get("PollutantCode")) + "(" + alarmname + ")");
                                    }
                                }
                            }
                            dataMap.put("pollutantname", pollutantname);
                            alarmDataList.add(dataMap);
                        }
                        //排序
                        Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed();
                        alarmDataList = alarmDataList.stream().sorted(comparebynum).collect(Collectors.toList());
                        typeAndAlarmDataList.put(typeIndex, alarmDataList);
                    }
                }
                for (Integer typeIndex : monitorpointtypes) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointtype", typeIndex);
                    resultMap.put("transmissionrate", typeAndTransmissionData.get(typeIndex));
                    resultMap.put("alarmdatalist", typeAndAlarmDataList.get(typeIndex));
                    resultList.add(resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getTransmissionData(List<Map<String, Object>> estData) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        //实传输数量
        Double transmissionnumber = estData.stream().peek(m -> m.put("transmissionnumber", decimalFormat.format(Integer.valueOf(m.get("transmissionnumber") == null ? "0" : m.get("transmissionnumber")
                .toString())))).map(m -> Double.valueOf(m.get("transmissionnumber") == null ? "0d" : m.get("transmissionnumber").toString())).collect(Collectors.summingDouble(m -> m));

        //应传输数量
        Double shouldnumber = estData.stream().peek(m -> m.put("shouldnumber", decimalFormat.format(Integer.valueOf(m.get("shouldnumber") == null ? "0" : m.get("shouldnumber")
                .toString())))).map(m -> Double.valueOf(m.get("shouldnumber") == null ? "0d" : m.get("shouldnumber").toString())).collect(Collectors.summingDouble(m -> m));
        //传输率
        double transmissionrate = shouldnumber == 0d ? 0d : transmissionnumber / shouldnumber;
        String rate = decimalFormat.format(transmissionrate * 100) + "%";
        return rate;
    }

    private Map<String, Object> setCodeAndName(List<Map<String, Object>> pollutants) {
        Map<String, Object> codeAndName = new HashMap<>();
        for (Map<String, Object> pollutant : pollutants) {
            codeAndName.put(pollutant.get("code") + "", pollutant.get("name"));
        }
        return codeAndName;
    }

    private void setAlarmTypeAndAlarmList(Integer alarmType, Map<String, Object> paramMap, List<Document> alarmList) {
        boolean isSelect = true;

        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(alarmType)) {
            case EarlyAlarmEnum:
                paramMap.remove("unwindkey", "HourDataList");
                paramMap.put("collection", earlyWarnDataCollect);
                paramMap.put("timeKey", "EarlyWarnTime");
                break;
            case OverAlarmEnum:
                paramMap.remove("unwindkey", "HourDataList");
                paramMap.put("collection", overDataCollect);
                paramMap.put("timeKey", "OverTime");
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
                break;
            default:
                isSelect = false;
                break;
        }
        if (isSelect) {
            List<Document> documents = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);
            if (documents.size() > 0) {
                for (Document document : documents) {
                    document.put("alarmtype", alarmType);
                    alarmList.add(document);
                }
            }
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 上午 8:55
     * @Description: 通过企业、排口类型、排口名称获取近一个月报警列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getLastMonthAlarmListByParams", method = RequestMethod.POST)
    public Object getLastMonthAlarmListByParams(@RequestJson(value = "pollutionid", required = true) String pollutionid, @RequestJson(value = "type", required = true) Integer type,
                                                @RequestJson(value = "outputname", required = false) String outputname, @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                @RequestJson(value = "pagesize", required = false) Integer pagesize) throws Exception {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            Date now = new Date();
            paramMap.put("fk_pollutionid", pollutionid);
            paramMap.put("outputname", outputname);
            if (type == 1) {
                data = pollutionService.getWaterOutPutInfoByPollutionid(paramMap);
            } else if (type == 2 || type == 22) {
                paramMap.put("type", type);
                data = pollutionService.getGasOutPutInfoByPollutionid(paramMap);
            }
            List<Map<String, Object>> resultList = SetPollutant(data, type, pagenum, pagesize, now);
            if (pagenum != null && pagesize != null) {
                String dgimns = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
                resultMap.put("total", getLastMonthAlarmSize(dgimns, now));
                resultMap.put("datalist", resultList);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/17 0017 下午 4:22
     * @Description: 获取近一个月报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private List<OverDataVO> getLastMonthAlarmData(String codes, Integer pagenum, Integer pagesize, Date now) {
        Map<String, Object> timeJson = getLastMonthTimeMap(now);
        //设置实体查询条件
        OverDataVO overDataVo = new OverDataVO();
        overDataVo.setOverTime(JSONObject.fromObject(timeJson).toString());
        overDataVo.setDataGatherCode(codes);

        MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
        mongoSearchEntity.setPage(pagenum);
        mongoSearchEntity.setSize(pagesize);
        List<OverDataVO> overData = mongoBaseService.getListWithPageByParam(overDataVo, mongoSearchEntity, "OverData", "yyyy-MM-dd HH:mm:ss");
        List<OverDataVO> collect = overData.stream().peek(m -> m.setOverTime(format(m.getOverTime(), "yyyy-MM-dd HH"))).collect(Collectors.toList());
        return collect;
    }


    private Long getLastMonthAlarmSize(String codes, Date now) {
        Map<String, Object> timeJson = getLastMonthTimeMap(now);
        //设置实体查询条件
        OverDataVO overDataVo = new OverDataVO();
        overDataVo.setOverTime(JSONObject.fromObject(timeJson).toString());
        overDataVo.setDataGatherCode(codes);
        long size = mongoBaseService.getCount(overDataVo, "OverData", "yyyy-MM-dd HH:mm:ss");
        return size;
    }


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 6:55
     * @Description: 获取近一个月时间map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    public Map<String, Object> getLastMonthTimeMap(Date now) {
        Calendar instance = Calendar.getInstance();
        String format = dateFormat.format(now);
        instance.setTime(now);
        instance.add(Calendar.MONTH, -1);
        Date endtime = instance.getTime();
        String format1 = dateFormat.format(endtime);
        //设置查询时间json
        Map<String, Object> timeJson = new HashMap();
        timeJson.put("starttime", format1);
        timeJson.put("endtime", format);
        return timeJson;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 下午 3:03
     * @Description: 组装数据结果集
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [resultList, type]
     * @throws:
     */
    private List SetPollutant(List<Map<String, Object>> resultList, Integer type, Integer pagenum, Integer pagesize, Date now) {
        List<Map<String, Object>> list = new ArrayList<>();
        String dgimns = resultList.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
        List<OverDataVO> lastMonthAlarmData = getLastMonthAlarmData(dgimns, pagenum, pagesize, now);
        List<String> codes = lastMonthAlarmData.stream().map(m -> m.getPollutantCode()).distinct().collect(Collectors.toList());
        Map<String, Object> params = new HashMap<>();
        params.put("pollutanttype", type);
        params.put("codes", codes);
        List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(params);
        List<Map<String, Object>> alarmLevelPubCodeInfo = alarmLevelService.getAlarmLevelPubCodeInfo();


        for (OverDataVO lastMonthAlarmDatum : lastMonthAlarmData) {
            String dataGatherCode = lastMonthAlarmDatum.getDataGatherCode();
            List<Map<String, Object>> collect = resultList.stream().filter(m -> m.get("DGIMN") != null && m.get("DGIMN").toString().equals(dataGatherCode)).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            if (collect.size() > 0) {
                Map<String, Object> map1 = collect.get(0);
                String pollutantCode = lastMonthAlarmDatum.getPollutantCode();
                map.put("overtime", lastMonthAlarmDatum.getOverTime());
                map.put("alarmtype", CommonTypeEnum.AlarmTypeEnum.getNameByCode(lastMonthAlarmDatum.getAlarmType()));
                map.put("alarmlevel", setAlarmLevel(lastMonthAlarmDatum.getAlarmLevel(), alarmLevelPubCodeInfo));
                map.put("monitorvalue", lastMonthAlarmDatum.getMonitorValue());
                map.put("outputname", map1.get("OutputName"));
                map.put("pollutionname", map1.get("PollutionName"));
                map.put("StorageTankAreaName", map1.get("StorageTankAreaName"));
                map.put("shorternameAndStorageTankAreaName", map1.get("shorternameAndStorageTankAreaName"));
                map.put("pollutantname", "");
                List<Map<String, Object>> collect1 = pollutantInfo.stream().filter(m -> m.get("code") != null && pollutantCode.equals(m.get("code").toString())).collect(Collectors.toList());
                if (collect1.size() > 0) {
                    Map<String, Object> pollutant = collect1.get(0);
                    map.put("pollutantname", pollutant.get("name"));
                }
                list.add(map);
            }
        }
        return list;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/17 0017 下午 4:18
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [date]
     * @throws:
     */
    public static String format(String date, String parttern) {
        if (StringUtils.isNotBlank(date)) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US);
                Date parse = format.parse(date);
                SimpleDateFormat format1 = new SimpleDateFormat(parttern);
                String format2 = format1.format(parse);
                return format2;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/21 0021 上午 10:58
     * @Description: 设置报警等级
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [level]
     * @throws:
     */
    private String setAlarmLevel(String level, List<Map<String, Object>> alarmLevelPubCodeInfo) {
        List<String> name = alarmLevelPubCodeInfo.stream().filter(m -> m.get("Code") != null && m.get("Code").toString().equals(level)).map(m -> m.get("Name").toString()).collect(Collectors.toList());
        if (name.size() > 0) {
            return name.get(0);
        }
        return "";
    }


    /**
     * @author: lip
     * @date: 2019/8/24 0024 上午 10:13
     * @Description: 根据mn号+污染物统计当天浓度突变+排放量突变+超阈值预警+数据超限+数据异常次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countTodayAlarmDetailData", method = RequestMethod.POST)
    public Object countTodayAlarmDetailData(@RequestJson(value = "nowday", required = false) String nowday,
                                            @RequestJson(value = "monitorpointtypes") List<Integer>
                                                    monitorpointtypes) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Date today = new Date();
            if (StringUtils.isNotBlank(nowday)) {
                today = DataFormatUtil.getDateYMD(nowday);
            }
            String starttime = DataFormatUtil.getDateYMD(today);
            String endtime = DataFormatUtil.getDateYMD(today);
            Map<String, Object> paramMap = new HashMap<>();
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            Map<String, String> AllMNAndPollution = new HashMap<>();
            Map<String, String> AllMNAndMonitorPointName = new HashMap<>();
            Map<String, String> AllMNAndMonitorPointId = new HashMap<>();
            Map<String, String> AllMNAndPollutionId = new HashMap<>();
            Map<Integer, Set<String>> typeAndMN = new HashMap<>();
            Map<Integer, Map<String, Object>> typeAndCodeAndName = new HashMap<>();
            List<String> mns = new ArrayList<>();
            for (Integer type : monitorpointtypes) {
                Map<String, String> MNAndPollution = onlineService.getMNAndPollution(new ArrayList<>(), type);
                Map<String, String> MNAndPollutionId = onlineService.getMNAndPollutionId(new ArrayList<>(), type);
                Map<String, String> MNAndMonitorPointName = onlineService.getMNAndMonitorPointName(new ArrayList<>(), type);
                Map<String, String> MNAndMonitorPointId = onlineService.getMNAndMonitorPointId(new ArrayList<>(), type);
                AllMNAndMonitorPointName.putAll(MNAndMonitorPointName);
                AllMNAndPollution.putAll(MNAndPollution);
                AllMNAndPollutionId.putAll(MNAndPollutionId);
                AllMNAndMonitorPointId.putAll(MNAndMonitorPointId);
                typeAndMN.put(type, MNAndMonitorPointId.keySet());
                typeAndCodeAndName.put(type, getCodeAndNameByType(type));
                mns = new ArrayList<>(AllMNAndMonitorPointName.keySet());
            }


            //公用mn号
            String dgimn;
            //公用监测时间
            String monitortime;
            //公用污染物编码
            String pollutantcode;
            //公用污染物集合
            Map<String, List<Map<String, Object>>> codeAndAlarmData;
            List<Map<String, Object>> alarmData;
            Map<String, Map<String, List<Map<String, Object>>>> mnAndCodeAndAlarmData = new HashMap<>();
            //1，浓度突变预警
            paramMap.put("mns", mns);
            paramMap.put("collection", "MinuteData");
            paramMap.put("timeKey", "MonitorTime");
            paramMap.put("unwindkey", "MinuteDataList");
            int countNum;
            List<Document> conChange = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);

            if (conChange.size() > 0) {
                for (Document document : conChange) {
                    dgimn = document.getString("DataGatherCode");
                    monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    pollutantcode = document.getString("PollutantCode");
                    countNum = document.getInteger("countnum");
                    if (mnAndCodeAndAlarmData.containsKey(dgimn)) {
                        codeAndAlarmData = mnAndCodeAndAlarmData.get(dgimn);
                    } else {
                        codeAndAlarmData = new HashMap<>();
                    }
                    if (codeAndAlarmData.containsKey(pollutantcode)) {
                        alarmData = codeAndAlarmData.get(pollutantcode);
                    } else {
                        alarmData = new ArrayList<>();
                    }
                    Map<String, Object> conchangenum = new HashMap<>();
                    conchangenum.put("conchangenum", countNum);
                    conchangenum.put("lastalarmtime", monitortime);
                    alarmData.add(conchangenum);
                    codeAndAlarmData.put(pollutantcode, alarmData);
                    mnAndCodeAndAlarmData.put(dgimn, codeAndAlarmData);
                }
            }
            //2，排放量突变预警
            paramMap.put("collection", "HourFlowData");
            paramMap.put("timeKey", "MonitorTime");
            paramMap.put("unwindkey", "HourFlowDataList");
            List<Document> flowChange = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);
            if (flowChange.size() > 0) {
                for (Document document : flowChange) {
                    dgimn = document.getString("DataGatherCode");
                    monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    pollutantcode = document.getString("PollutantCode");
                    countNum = document.getInteger("countnum");
                    if (mnAndCodeAndAlarmData.containsKey(dgimn)) {
                        codeAndAlarmData = mnAndCodeAndAlarmData.get(dgimn);
                    } else {
                        codeAndAlarmData = new HashMap<>();
                    }
                    if (codeAndAlarmData.containsKey(pollutantcode)) {
                        alarmData = codeAndAlarmData.get(pollutantcode);
                    } else {
                        alarmData = new ArrayList<>();
                    }
                    Map<String, Object> flowchangenum = new HashMap<>();
                    flowchangenum.put("flowchangenum", countNum);
                    flowchangenum.put("lastalarmtime", monitortime);
                    alarmData.add(flowchangenum);
                    codeAndAlarmData.put(pollutantcode, alarmData);
                    mnAndCodeAndAlarmData.put(dgimn, codeAndAlarmData);
                }
            }
            //浓度超阈值
            paramMap.remove("unwindkey");
            paramMap.put("collection", "EarlyWarnData");
            paramMap.put("timeKey", "EarlyWarnTime");
            List<Document> CYZData = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);
            if (CYZData.size() > 0) {
                for (Document document : CYZData) {
                    dgimn = document.getString("DataGatherCode");
                    monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    pollutantcode = document.getString("PollutantCode");
                    countNum = document.getInteger("countnum");
                    if (mnAndCodeAndAlarmData.containsKey(dgimn)) {
                        codeAndAlarmData = mnAndCodeAndAlarmData.get(dgimn);
                    } else {
                        codeAndAlarmData = new HashMap<>();
                    }
                    if (codeAndAlarmData.containsKey(pollutantcode)) {
                        alarmData = codeAndAlarmData.get(pollutantcode);
                    } else {
                        alarmData = new ArrayList<>();
                    }
                    Map<String, Object> cyznum = new HashMap<>();
                    cyznum.put("cyznum", countNum);
                    cyznum.put("lastalarmtime", monitortime);
                    alarmData.add(cyznum);
                    codeAndAlarmData.put(pollutantcode, alarmData);
                    mnAndCodeAndAlarmData.put(dgimn, codeAndAlarmData);
                }
            }
            //4，数据超限报警
            paramMap.put("collection", "OverData");
            paramMap.put("timeKey", "OverTime");
            List<Document> OverData = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);
            if (OverData.size() > 0) {
                for (Document document : OverData) {
                    dgimn = document.getString("DataGatherCode");
                    monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    pollutantcode = document.getString("PollutantCode");
                    countNum = document.getInteger("countnum");
                    if (mnAndCodeAndAlarmData.containsKey(dgimn)) {
                        codeAndAlarmData = mnAndCodeAndAlarmData.get(dgimn);
                    } else {
                        codeAndAlarmData = new HashMap<>();
                    }
                    if (codeAndAlarmData.containsKey(pollutantcode)) {
                        alarmData = codeAndAlarmData.get(pollutantcode);
                    } else {
                        alarmData = new ArrayList<>();
                    }
                    Map<String, Object> overnum = new HashMap<>();
                    overnum.put("overnum", countNum);
                    overnum.put("lastalarmtime", monitortime);
                    alarmData.add(overnum);
                    codeAndAlarmData.put(pollutantcode, alarmData);
                    mnAndCodeAndAlarmData.put(dgimn, codeAndAlarmData);
                }
            }


            //5，数据异常报警
            paramMap.put("collection", "ExceptionData");
            paramMap.put("timeKey", "ExceptionTime");
            List<Document> exceptionData = onlineCountAlarmService.countPollutantMaxTimeByParam(paramMap);

            if (exceptionData.size() > 0) {
                for (Document document : exceptionData) {

                    dgimn = document.getString("DataGatherCode");
                    monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    pollutantcode = document.getString("PollutantCode");
                    countNum = document.getInteger("countnum");
                    if (mnAndCodeAndAlarmData.containsKey(dgimn)) {
                        codeAndAlarmData = mnAndCodeAndAlarmData.get(dgimn);
                    } else {
                        codeAndAlarmData = new HashMap<>();
                    }
                    if (codeAndAlarmData.containsKey(pollutantcode)) {
                        alarmData = codeAndAlarmData.get(pollutantcode);
                    } else {
                        alarmData = new ArrayList<>();
                    }
                    Map<String, Object> exceptionnum = new HashMap<>();
                    exceptionnum.put("exceptionnum", countNum);
                    exceptionnum.put("lastalarmtime", monitortime);
                    alarmData.add(exceptionnum);
                    codeAndAlarmData.put(pollutantcode, alarmData);
                    mnAndCodeAndAlarmData.put(dgimn, codeAndAlarmData);
                }
            }
            if (mnAndCodeAndAlarmData.size() > 0) {
                Integer type;
                for (String mnKey : mnAndCodeAndAlarmData.keySet()) {
                    Map<String, Object> reMap = new LinkedHashMap<>();
                    reMap.put("pollutionname", AllMNAndPollution.get(mnKey) == null ? "" : AllMNAndPollution.get(mnKey));
                    reMap.put("pollutionid", AllMNAndPollutionId.get(mnKey));
                    reMap.put("monitorpointname", AllMNAndMonitorPointName.get(mnKey));
                    reMap.put("monitorpointid", AllMNAndMonitorPointId.get(mnKey));
                    reMap.put("mn", mnKey);
                    type = getTypeByMN(mnKey, typeAndMN);
                    if (type != null) {
                        reMap.put("monitorpointtype", type);
                        reMap.put("index", CommonTypeEnum.MonitorPointTypeEnum.getEnumIndex(type));
                        reMap.put("pollutantdata", getPollutantData(typeAndCodeAndName.get(type), mnAndCodeAndAlarmData.get(mnKey)));
                        resultList.add(reMap);
                    }
                }
            }
            //排序 监测点类型 污染源名称 监测点名称 升序
            List<Map<String, Object>> collect = resultList.stream().sorted(Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("index").toString())).thenComparing(Comparator.comparing((Map m) -> m.get("pollutionname").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString()))).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/24 0024 上午 10:13
     * @Description: 自定义查询条件统计监测点报警数据（报警次数和监测点信息）
     * @updateUser: xsm
     * @updateDate: 2020/06/18 0018 上午12:02
     * @updateDescription: 新增企业或点位名称查询（企业关联类型查企业名称，不关联类型查点位名称）
     * @param:customname：企业或点位名称 根据类型判断
     * @return:
     */
    @RequestMapping(value = "countMonitorPointAlarmDataByParam", method = RequestMethod.POST)
    public Object countMonitorPointAlarmDataByParam(@RequestJson(value = "nowday", required = false) String nowday,
                                                    @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                    @RequestJson(value = "remindtype") Integer remindtype,
                                                    @RequestJson(value = "pagesize") Integer pagesize,
                                                    @RequestJson(value = "pagenum") Integer pagenum,
                                                    @RequestJson(value = "customname", required = false) String customname


    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Date today = new Date();
            if (StringUtils.isNotBlank(nowday)) {
                today = DataFormatUtil.getDateYMD(nowday);
            }
            String starttime = DataFormatUtil.getDateYMD(today);
            String endtime = DataFormatUtil.getDateYMD(today);
            Map<String, Object> paramMap = new HashMap<>();
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("remindtype", remindtype);
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndShortername = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            Map<String, Object> mnAndStatus = new HashMap<>();
            List<String> mns = new ArrayList<>();
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            Map<String, Object> paramTemp = new HashMap<>();
            paramTemp.put("outputids", Arrays.asList());
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            for (Integer type : monitorpointtypes) {
                paramTemp.put("monitorpointtype", type);
                paramTemp.put("userid", userId);
                if (StringUtils.isNotBlank(customname)) {
                    if (pollutiontypes.contains(type)) {//若为关联企业的监测点类型  取企业名称
                        paramTemp.put("pollutionname", customname);
                    } else {
                        paramTemp.put("monitorpointname", customname);
                    }
                }
                monitorPoints.addAll(onlineService.getMonitorPointDataByParam(paramTemp));
            }
            String mnCommon;
            for (Map<String, Object> map : monitorPoints) {
                mnCommon = map.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                mnAndShortername.put(mnCommon, map.get("shortername"));
                mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                mnAndMonitorPointId.put(mnCommon, map.get("monitorpointid"));
                mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                mnAndStatus.put(mnCommon, map.get("onlinestatus"));
            }
            paramMap.put("mns", mns);
            switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindtype)) {
                case ConcentrationChangeEnum:
                    paramMap.put("collection", "HourData");
                    paramMap.put("timeKey", "MonitorTime");
                    paramMap.put("unwindkey", "HourDataList");
                    break;
                case FlowChangeEnum:
                    paramMap.put("collection", "HourFlowData");
                    paramMap.put("timeKey", "MonitorTime");
                    paramMap.put("unwindkey", "HourFlowDataList");
                    break;
                case OverAlarmEnum:
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "OverData");
                    paramMap.put("timeKey", "OverTime");
                    break;
                case EarlyAlarmEnum:
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "EarlyWarnData");
                    paramMap.put("timeKey", "EarlyWarnTime");
                    break;
                case ExceptionAlarmEnum:
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "ExceptionData");
                    paramMap.put("timeKey", "ExceptionTime");
                    break;
                case WaterNoFlowEnum://废水无流量异常
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "ExceptionData");
                    paramMap.put("timeKey", "ExceptionTime");
                    break;
                default:
                    break;
            }
            //公用mn号
            int countNum;
            Map<String, Integer> mnAndCountNum = new HashMap<>();
            List<Document> documents = countPollutantDataByParam(paramMap);
            if (documents.size() > 0) {
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    countNum = document.getInteger("countnum");
                    if (mnAndCountNum.containsKey(mnCommon)) {
                        mnAndCountNum.put(mnCommon, mnAndCountNum.get(mnCommon) + countNum);
                    } else {
                        mnAndCountNum.put(mnCommon, countNum);
                    }
                }
                if (mnAndCountNum.size() > 0) {
                    String onlinestatus;
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    for (String mnKey : mnAndCountNum.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("pollutionid", mnAndPollutionId.get(mnKey));
                        dataMap.put("pollutionname", mnAndPollutionName.get(mnKey));
                        dataMap.put("shortername", mnAndShortername.get(mnKey));
                        dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnKey));
                        dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnKey));
                        onlinestatus = mnAndStatus.get(mnKey) != null ? mnAndStatus.get(mnKey).toString() : "";
                        dataMap.put("onlinestatus", onlinestatus);
                        dataMap.put("orderindex", CommonTypeEnum.onlineStatusOrderEnum.getIndexByCode(onlinestatus));
                        dataMap.put("alarmnum", mnAndCountNum.get(mnKey));
                        dataMap.put("dgimn", mnKey);
                        dataList.add(dataMap);
                    }
                    //分页+排序
                    dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                    List<Map<String, Object>> subDataList = dataList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                    resultMap.put("datalist", subDataList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2020/3/19 0019 上午 8:44
     * @Description: 自定义查询条件获取报警统计数据(日报 、 周报 、 月报 、 年报)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getAlarmCountDataByParam", method = RequestMethod.POST)
    public Object getAlarmCountDataByParam(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "monitortime") String monitortime
    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = getAlarmCountListData(timetype, monitortime);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2020/3/19 0019 上午 8:44
     * @Description: 自定义查询条件导出报警统计数据(日报 、 周报 、 月报 、 年报)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "exportAlarmCountDataByParam", method = RequestMethod.POST)
    public Object exportAlarmCountDataByParam(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "monitortime") String monitortime,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = getAlarmCountListData(timetype, monitortime);
            String preName = "";
            String starttime = "";
            String endtime = "";

            switch (timetype) {
                case "day":
                    preName = "日报（" + monitortime + ")";
                    break;
                case "week":
                    String year = monitortime.split("-")[0];
                    String week = monitortime.split("-")[1];
                    int yearNum = Integer.parseInt(year);
                    int weekNum = Integer.parseInt(week);
                    starttime = DataFormatUtil.getStartDayOfWeekNo(yearNum, weekNum);
                    endtime = DataFormatUtil.getEndDayOfWeekNo(yearNum, weekNum);
                    preName = "周报（" + starttime + "-" + endtime + ")";
                    break;
                case "month":
                    starttime = DataFormatUtil.getFirstDayOfMonth(monitortime);
                    endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
                    preName = "月报（" + starttime + "-" + endtime + ")";
                    break;
                case "year":
                    starttime = DataFormatUtil.getYearFirst(monitortime);
                    endtime = DataFormatUtil.getYearLast(monitortime);
                    preName = "年报（" + starttime + "-" + endtime + ")";
                    break;
            }
            //导出文件
            String fileName = "预警报警统计" + preName + new Date().getTime();
            //设置导出文件数据格式
            List<String> headers = Arrays.asList("监测类型", "监测点名称", "污染物名称", "报警类型", "报警时间", "报警次数");
            List<String> headersField = Arrays.asList("monitorpointtypename", "monitorpointname", "pollutantname", "alarmtype", "alarmtime", "alarmnum");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2020/3/20 0020 上午 8:57
     * @Description: 获取报警统计列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getAlarmCountListData(String timetype, String monitortime) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        List<Integer> monitorPointTypes = Arrays.asList(
                CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()
        );

        List<String> mns = new ArrayList<>();
        Map<String, Object> mnAndMonitorPointName = new HashMap<>();
        Map<String, Integer> mnAndType = new HashMap<>();
        Map<String, Object> paramTemp = new HashMap<>();
        paramTemp.put("outputids", Arrays.asList());
        List<Map<String, Object>> monitorPoints;
        String mnCommon;
        String monitorPointName;
        for (Integer type : monitorPointTypes) {
            paramTemp.put("monitorpointtype", type);
            monitorPoints = onlineService.getMonitorPointDataByParam(paramTemp);
            for (Map<String, Object> map : monitorPoints) {
                mnCommon = map.get("dgimn").toString();
                if (!mns.contains(mnCommon)) {
                    mns.add(mnCommon);
                }
                monitorPointName = map.get("monitorpointname").toString();
                if (map.get("pollutionname") != null) {
                    monitorPointName = map.get("pollutionname") + "-" + monitorPointName;
                }
                mnAndMonitorPointName.put(mnCommon, monitorPointName);
                mnAndType.put(mnCommon, type);
            }

        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("mns", mns);
        String starttime = "";
        String endtime = "";

        switch (timetype) {
            case "day":
                starttime = monitortime;
                endtime = monitortime;
                break;
            case "week":
                String year = monitortime.split("-")[0];
                String week = monitortime.split("-")[1];
                int yearNum = Integer.parseInt(year);
                int weekNum = Integer.parseInt(week);
                starttime = DataFormatUtil.getStartDayOfWeekNo(yearNum, weekNum) + " 00:00:00";
                endtime = DataFormatUtil.getEndDayOfWeekNo(yearNum, weekNum) + " 23:59:59";
                break;
            case "month":
                starttime = DataFormatUtil.getFirstDayOfMonth(monitortime);
                endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
                break;
            case "year":
                starttime = DataFormatUtil.getYearFirst(monitortime);
                endtime = DataFormatUtil.getYearLast(monitortime);
                break;
        }
        starttime = starttime + " 00:00:00";
        endtime = endtime + " 23:59:59";
        paramMap.put("starttime", starttime);
        paramMap.put("endtime", endtime);

        List<String> codes = new ArrayList<>();
        String pollutantCode;
        String monitorpointtypename;
        List<Document> timeList;
        String alarmTime;
        //1，浓度突变
        paramMap.put("collection", "HourData");
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("unwindkey", "HourDataList");
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
        List<Document> nd_documents = countPollutantDataByParam(paramMap);
        if (nd_documents.size() > 0) {
            for (Document document : nd_documents) {
                mnCommon = document.getString("DataGatherCode");
                Map<String, Object> resultMap = new HashMap<>();
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnCommon)).replace("点类型", "");
                resultMap.put("monitorpointtypecode", mnAndType.get(mnCommon));
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                pollutantCode = document.getString("PollutantCode");
                resultMap.put("pollutantname", pollutantCode);
                codes.add(pollutantCode);
                resultMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName());
                timeList = (List<Document>) document.get("timeList");

                if (timetype.equals("day")) {
                    alarmTime = getHourAlarmTime(timeList);
                } else {
                    alarmTime = getDayAlarmTime(timeList);
                }
                resultMap.put("alarmtime", alarmTime);
                resultMap.put("alarmnum", document.get("countnum"));
                resultList.add(resultMap);
            }
        }


        //2，超阈值
        paramMap.remove("unwindkey");
        paramMap.put("collection", "EarlyWarnData");
        paramMap.put("timeKey", "EarlyWarnTime");
        paramMap.put("remindtype", EarlyAlarmEnum.getCode());
        List<Document> cyz_documents = countPollutantDataByParam(paramMap);
        if (cyz_documents.size() > 0) {
            for (Document document : cyz_documents) {
                mnCommon = document.getString("DataGatherCode");
                Map<String, Object> resultMap = new HashMap<>();
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnCommon)).replace("点类型", "");
                resultMap.put("monitorpointtypecode", mnAndType.get(mnCommon));
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                pollutantCode = document.getString("PollutantCode");
                resultMap.put("pollutantname", pollutantCode);
                codes.add(pollutantCode);
                resultMap.put("alarmtype", EarlyAlarmEnum.getName());
                timeList = (List<Document>) document.get("timeList");
                if (timetype.equals("day")) {
                    alarmTime = getMinuteAlarmTime(timeList);
                } else {
                    alarmTime = getDayAlarmTime(timeList);
                }

                resultMap.put("alarmtime", alarmTime);
                resultMap.put("alarmnum", document.get("countnum"));
                resultList.add(resultMap);
            }
        }


        //3，数据超限
        paramMap.remove("unwindkey");
        paramMap.put("collection", "OverData");
        paramMap.put("timeKey", "OverTime");
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode());
        List<Document> over_documents = countPollutantDataByParam(paramMap);
        if (over_documents.size() > 0) {
            for (Document document : over_documents) {
                mnCommon = document.getString("DataGatherCode");
                Map<String, Object> resultMap = new HashMap<>();
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnCommon)).replace("点类型", "");
                resultMap.put("monitorpointtypecode", mnAndType.get(mnCommon));
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                pollutantCode = document.getString("PollutantCode");
                resultMap.put("pollutantname", pollutantCode);
                codes.add(pollutantCode);
                resultMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getName());
                timeList = (List<Document>) document.get("timeList");
                if (timetype.equals("day")) {
                    alarmTime = getMinuteAlarmTime(timeList);
                } else {
                    alarmTime = getDayAlarmTime(timeList);
                }

                resultMap.put("alarmtime", alarmTime);
                resultMap.put("alarmnum", document.get("countnum"));
                resultList.add(resultMap);
            }
        }


        //4，数据异常
        paramMap.remove("unwindkey");
        paramMap.put("collection", "ExceptionData");
        paramMap.put("timeKey", "ExceptionTime");
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());
        List<Document> exp_documents = countPollutantDataByParam(paramMap);
        if (exp_documents.size() > 0) {
            for (Document document : exp_documents) {
                mnCommon = document.getString("DataGatherCode");
                Map<String, Object> resultMap = new HashMap<>();
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnCommon)).replace("点类型", "");
                resultMap.put("monitorpointtypecode", mnAndType.get(mnCommon));
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                pollutantCode = document.getString("PollutantCode");
                resultMap.put("pollutantname", pollutantCode);
                codes.add(pollutantCode);
                resultMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getName());
                timeList = (List<Document>) document.get("timeList");
                if (timetype.equals("day")) {
                    alarmTime = getMinuteAlarmTime(timeList);
                } else {
                    alarmTime = getDayAlarmTime(timeList);
                }

                resultMap.put("alarmtime", alarmTime);
                resultMap.put("alarmnum", document.get("countnum"));
                resultList.add(resultMap);
            }
        }
        //5，无流量异常
        paramMap.remove("unwindkey");
        paramMap.put("collection", "ExceptionData");
        paramMap.put("timeKey", "ExceptionTime");
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode());
        List<Document> noFlow_documents = countPollutantDataByParam(paramMap);
        if (noFlow_documents.size() > 0) {
            for (Document document : noFlow_documents) {
                mnCommon = document.getString("DataGatherCode");
                Map<String, Object> resultMap = new HashMap<>();
                monitorpointtypename = CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(mnAndType.get(mnCommon)).replace("点类型", "");
                resultMap.put("monitorpointtypecode", mnAndType.get(mnCommon));
                resultMap.put("monitorpointtypename", monitorpointtypename);
                resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                pollutantCode = document.getString("PollutantCode");
                resultMap.put("pollutantname", pollutantCode);
                codes.add(pollutantCode);
                resultMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getName());
                timeList = (List<Document>) document.get("timeList");
                if (timetype.equals("day")) {
                    alarmTime = getMinuteAlarmTime(timeList);
                } else {
                    alarmTime = getDayAlarmTime(timeList);
                }

                resultMap.put("alarmtime", alarmTime);
                resultMap.put("alarmnum", document.get("countnum"));
                resultList.add(resultMap);
            }
        }
        //排放量突变（暂时不要）
        //paramMap.put("collection", "HourFlowData");
        //paramMap.put("timeKey", "MonitorTime");
        //paramMap.put("unwindkey", "HourFlowDataList");
        //替换code为name
        Map<String, Object> params = new HashMap<>();
        params.put("codes", codes);
        //获取污染物码表信息
        List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(params);
        Map<Integer, Map<String, Object>> typeAndCodeAndName = new HashMap<>();
        Map<String, Object> codeAndName;
        Integer type;
        if (pollutantList.size() > 0) {

            for (Map<String, Object> pollutant : pollutantList) {
                if (pollutant.get("pollutanttype") != null) {
                    type = Integer.parseInt(pollutant.get("pollutanttype").toString());
                    if (typeAndCodeAndName.containsKey(type)) {
                        codeAndName = typeAndCodeAndName.get(type);
                    } else {
                        codeAndName = new HashMap<>();
                    }
                    codeAndName.put(pollutant.get("code").toString(), pollutant.get("name"));

                    typeAndCodeAndName.put(type, codeAndName);
                }

            }
        }
        for (Map<String, Object> resultMap : resultList) {
            pollutantCode = resultMap.get("pollutantname").toString();
            type = Integer.parseInt(resultMap.get("monitorpointtypecode").toString());
            codeAndName = typeAndCodeAndName.get(type);
            if (codeAndName != null && codeAndName.containsKey(pollutantCode)) {
                resultMap.put("pollutantname", codeAndName.get(pollutantCode));
            }
        }
        //排序
        resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("alarmnum").toString())).reversed()).collect(Collectors.toList());
        return resultList;
    }

    private String getDayAlarmTime(List<Document> timeList) {
        String ymd;
        List<String> ymds = new ArrayList<>();
        for (Document document : timeList) {
            ymd = DataFormatUtil.getDateYMD(document.getDate("time"));
            ymds.add(ymd);
        }
        String times = DataFormatUtil.mergeContinueDayDate(ymds, 1440, "yyyy-MM-dd", "、", "M月d日");
        return times;
    }

    /**
     * @author: lip
     * @date: 2020/3/19 0019 下午 1:19
     * @Description: 合并小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getHourAlarmTime(List<Document> timeList) {
        String ymdh;
        List<String> ymdhs = new ArrayList<>();
        for (Document document : timeList) {
            ymdh = DataFormatUtil.getDateYMDH(document.getDate("time"));
            ymdhs.add(ymdh);
        }
        String times = DataFormatUtil.mergeContinueDate(ymdhs, 1, "yyyy-MM-dd HH", "、", "HH时");
        return times;
    }

    /**
     * @author: lip
     * @date: 2020/3/19 0019 下午 1:19
     * @Description: 合并分钟数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getMinuteAlarmTime(List<Document> timeList) {
        String ymdhm;
        List<String> ymdhms = new ArrayList<>();
        for (Document document : timeList) {

            ymdhm = DataFormatUtil.getDateYMDHM(document.getDate("time"));
            ymdhms.add(ymdhm);
        }
        int interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
        String times = DataFormatUtil.mergeContinueDate(ymdhms, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
        return times;
    }


    /**
     * @author: lip
     * @date: 2019/10/29 0029 上午 9:03
     * @Description: 自定义条件统计污染物数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> countPollutantDataByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            UnwindOperation unwindOperation = unwind(unwindkey);
            aggregations.add(unwindOperation);
        }
        int remindtypecode = Integer.parseInt(paramMap.get("remindtype").toString());
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = new Criteria();
        if (remindtypecode == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
        } else if (remindtypecode == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
        } else {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        }
        //Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        Fields fields;
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            fields = fields("DataGatherCode", timeKey).and("PollutantCode", unwindkey + ".PollutantCode");
        } else {
            fields = fields("DataGatherCode", "PollutantCode", timeKey);
        }
        aggregations.add(project(fields));

        Map<String, Object> times = new HashMap<>();
        times.put("time", "$" + timeKey);
        GroupOperation groupOperation = group("DataGatherCode", "PollutantCode").count().as("countnum")
                .push(times).as("timeList");
        aggregations.add(groupOperation);
        Aggregation aggregation = Aggregation.newAggregation(aggregations).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;

    }


    /**
     * @author: lip
     * @date: 2019/8/24 0024 下午 2:11
     * @Description: 获取单个监测点类型的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getCodeAndNameByType(Integer type) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", type);
        List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
        paramMap.clear();
        for (Map<String, Object> map : pollutantData) {
            paramMap.put(map.get("code").toString(), map.get("name"));
        }
        return paramMap;
    }

    /**
     * @author: lip
     * @date: 2019/8/24 0024 下午 1:39
     * @Description: 设置污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object getPollutantData(Map<String, Object> codeAndName, Map<String, List<Map<String, Object>>> codeAndAlarmData) {
        List<Map<String, Object>> pollutantdata = new ArrayList<>();
        for (String code : codeAndAlarmData.keySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("pollutantcode", code);
            map.put("pollutantname", codeAndName.get(code));
            map.put("alramdata", codeAndAlarmData.get(code));
            pollutantdata.add(map);
        }
        return pollutantdata;
    }


    /**
     * @author: lip
     * @date: 2019/8/20 0020 下午 3:15
     * @Description: 根据mn号获取该点位类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Integer getTypeByMN(String dgimn, Map<Integer, Set<String>> typeAndMN) {
        Integer monitorpointtype = null;
        for (Integer type : typeAndMN.keySet()) {
            if (typeAndMN.get(type).contains(dgimn)) {
                monitorpointtype = type;
                break;
            }
        }
        return monitorpointtype;
    }


    /**
     * @author: lip
     * @date: 2019/7/31 11:15
     * @Description: 获取报警详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorAlarmDataDetailByParam", method = RequestMethod.POST)
    public Object getMonitorAlarmDataDetailByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "monitortype") List<Integer> monitortypes,
            @RequestJson(value = "timetype") String timeType,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            String starttime = "";
            String endtime = "";

            List<String> datTypes = new ArrayList<>();
            switch (timeType) {
                case "hour":
                    starttime = monitortime + ":00:00";
                    endtime = monitortime + ":59:59";
                    datTypes.add(CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName());
                    datTypes.add(CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getName());
                    datTypes.add(CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getName());
                    break;
                case "day":
                    starttime = monitortime + " 00:00:00";
                    endtime = monitortime + " 23:59:59";
                    break;
                case "week":
                    String year = monitortime.split("-")[0];
                    String month = monitortime.split("-")[1];
                    String week = monitortime.split("-")[2];
                    int yearNum = Integer.parseInt(year);
                    int monthNum = Integer.parseInt(month);
                    int weekNum = Integer.parseInt(week);
                    Date startTemp = DataFormatUtil.getFirstDayOfWeek(yearNum, monthNum, weekNum);
                    Date endTemp = DataFormatUtil.getLastDayOfWeek(yearNum, monthNum, weekNum);
                    starttime = DataFormatUtil.getDateYMD(startTemp) + " 00:00:00";
                    endtime = DataFormatUtil.getDateYMD(endTemp) + " 23:59:59";
                    break;
                case "month":
                    starttime = DataFormatUtil.getFirstDayOfMonth(monitortime) + " 00:00:00";
                    endtime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
                    break;
                case "year":
                    starttime = DataFormatUtil.getYearFirst(monitortime) + " 00:00:00";
                    endtime = DataFormatUtil.getYearLast(monitortime) + " 23:59:59";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + timeType);
            }
            List<Map<String, Object>> tabledatalist = new ArrayList<>();
            List<String> mns;
            String mn;
            String monitorTime;
            List<Map<String, Object>> pollutantList;
            List<Map<String, Object>> pollutantData;
            Map<String, String> MNAndPollution;
            Map<String, String> MNAndMonitorPoint;
            Map<String, Object> paramMap = new HashMap<>();
            for (Integer type : monitortypes) {
                mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(new ArrayList<>(), type, new HashMap<>());
                //MN和污染源对照关系
                MNAndPollution = onlineService.getMNAndPollution(new ArrayList<>(), type);
                MNAndMonitorPoint = onlineService.getMNAndMonitorPoint(new ArrayList<>(), type);
                paramMap.put("pollutanttype", type);
                pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
                Map<String, Object> codeAndName = new HashMap<>();
                for (Map<String, Object> pollutant : pollutantData) {
                    codeAndName.put(pollutant.get("code").toString(), pollutant.get("name"));
                }
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("issuddenchange", true);
                //1，浓度突变预警
                paramMap.put("collection", "HourData");
                List<Document> conChange = onlineService.getMonitorDataByParamMap(paramMap);
                if (conChange.size() > 0) {
                    for (Document con : conChange) {
                        mn = con.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(con.getDate("MonitorTime"));
                        pollutantList = (List<Map<String, Object>>) con.get("HourDataList");
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutant.get("IsSuddenChange") != null && Boolean.parseBoolean(pollutant.get("IsSuddenChange").toString())) {
                                Map<String, Object> rowMap = new HashMap<>();
                                rowMap.put("pollutionname", MNAndPollution.get(mn));
                                rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                                rowMap.put("monitortime", monitorTime);
                                rowMap.put("pollutantname", codeAndName.get(pollutant.get("PollutantCode")));
                                rowMap.put("monitorvalue", pollutant.get("AvgStrength"));
                                rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName());
                                if (pollutant.get("ChangeMultiple") != null && !pollutant.get("ChangeMultiple").equals("")) {
                                    Double changeMultiple = Double.valueOf(pollutant.get("ChangeMultiple").toString());
                                    BigDecimal b = new BigDecimal(changeMultiple);
                                    String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    rowMap.put("changemultiple", f1);
                                } else {
                                    rowMap.put("changemultiple", pollutant.get("ChangeMultiple"));
                                }
                                tabledatalist.add(rowMap);
                            }
                        }
                    }
                }
                //2，排放量突变预警
                paramMap.put("collection", "HourFlowData");
                List<Document> flowChange = onlineService.getHourFlowMonitorDataByParamMap(paramMap);
                if (flowChange.size() > 0) {
                    for (Document flow : flowChange) {
                        mn = flow.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(flow.getDate("MonitorTime"));
                        pollutantList = (List<Map<String, Object>>) flow.get("HourFlowDataList");
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutant.get("IsSuddenChange") != null && Boolean.parseBoolean(pollutant.get("IsSuddenChange").toString())) {
                                Map<String, Object> rowMap = new HashMap<>();
                                rowMap.put("pollutionname", MNAndPollution.get(mn));
                                rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                                rowMap.put("monitortime", monitorTime);
                                rowMap.put("pollutantname", codeAndName.get(pollutant.get("PollutantCode")));
                                rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getName());
                                rowMap.put("monitorvalue", pollutant.get("CorrectedFlow"));
//                                rowMap.put("changemultiple", pollutant.get("ChangeMultiple"));
                                if (pollutant.get("ChangeMultiple") != null && !pollutant.get("ChangeMultiple").equals("")) {
                                    Double changeMultiple = Double.valueOf(pollutant.get("ChangeMultiple").toString());
                                    BigDecimal b = new BigDecimal(changeMultiple);
                                    String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    rowMap.put("changemultiple", f1);
                                } else {
                                    rowMap.put("changemultiple", pollutant.get("ChangeMultiple"));
                                }

                                tabledatalist.add(rowMap);
                            }
                        }
                    }
                }
                //3，超阈值预警
                if (datTypes.size() > 0) {
                    paramMap.put("datatypes", datTypes);
                }
                paramMap.put("monitortimekey", "EarlyWarnTime");
                paramMap.put("collection", "EarlyWarnData");
                List<Document> CYZData = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (CYZData.size() > 0) {
                    for (Document CYZ : CYZData) {
                        mn = CYZ.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(CYZ.getDate("EarlyWarnTime"));
                        Map<String, Object> rowMap = new HashMap<>();
                        rowMap.put("pollutionname", MNAndPollution.get(mn));
                        rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                        rowMap.put("monitortime", monitorTime);
                        rowMap.put("pollutantname", codeAndName.get(CYZ.get("PollutantCode")));
                        rowMap.put("monitorvalue", CYZ.get("MonitorValue"));
                        rowMap.put("alarmtype", EarlyAlarmEnum.getName());
                        rowMap.put("changemultiple", "");
                        tabledatalist.add(rowMap);
                    }
                }
                //4，数据超限报警
                paramMap.put("monitortimekey", "OverTime");
                paramMap.put("collection", "OverData");
                List<Document> OverData = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (OverData.size() > 0) {
                    for (Document over : OverData) {
                        mn = over.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(over.getDate("OverTime"));

                        Map<String, Object> rowMap = new HashMap<>();
                        rowMap.put("pollutionname", MNAndPollution.get(mn));
                        rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                        rowMap.put("monitortime", monitorTime);
                        rowMap.put("pollutantname", codeAndName.get(over.get("PollutantCode")));
                        rowMap.put("monitorvalue", over.get("MonitorValue"));
                        rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getName());
//                        rowMap.put("changemultiple", over.get("ChangeMultiple"));

                        if (over.get("ChangeMultiple") != null && !over.get("ChangeMultiple").equals("")) {
                            Double changeMultiple = Double.valueOf(over.get("ChangeMultiple").toString());
                            BigDecimal b = new BigDecimal(changeMultiple);
                            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                            rowMap.put("changemultiple", f1);
                        } else {
                            rowMap.put("changemultiple", over.get("ChangeMultiple"));
                        }
                        tabledatalist.add(rowMap);
                    }
                }
                //5，数据异常报警
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("collection", "ExceptionData");
                List<Document> exceptionData = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (exceptionData.size() > 0) {
                    for (Document exception : exceptionData) {
                        mn = exception.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(exception.getDate("ExceptionTime"));
                        Map<String, Object> rowMap = new HashMap<>();
                        rowMap.put("pollutionname", MNAndPollution.get(mn));
                        rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                        rowMap.put("monitortime", monitorTime);
                        rowMap.put("pollutantname", codeAndName.get(exception.get("PollutantCode")));
                        rowMap.put("monitorvalue", exception.get("MonitorValue"));
                        rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getName());
                        rowMap.put("changemultiple", "");
                        tabledatalist.add(rowMap);
                    }
                }
            }
            //排序+分页
            if (tabledatalist.size() > 0) {
                Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                if (comparebynum != null) {
                    tabledatalist = tabledatalist.stream().sorted(comparebynum).collect(Collectors.toList());
                }
                if (pagesize != null && pagenum != null) {
                    List<Map<String, Object>> dataList = getPageData(tabledatalist, pagenum, pagesize);
                    resultMap.put("total", tabledatalist.size());
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", tabledatalist);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "getMonitorAlarmListDataByParam", method = RequestMethod.POST)
    public Object getMonitorAlarmListDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "monitortype") List<Integer> monitortypes,
            @RequestJson(value = "monitorpointids") List<String> monitorpointids,
            @RequestJson(value = "timetype") String timeType,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            List<String> datTypes = new ArrayList<>();
            switch (timeType) {
                case "hour":
                    starttime = starttime + ":00:00";
                    endtime = endtime + ":59:59";
                    datTypes.add(CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName());
                    datTypes.add(CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getName());
                    datTypes.add(CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getName());
                    break;
                case "day":
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                case "week":
                    String year = starttime.split("-")[0];
                    String month = starttime.split("-")[1];
                    String week = starttime.split("-")[2];
                    int yearNum = Integer.parseInt(year);
                    int monthNum = Integer.parseInt(month);
                    int weekNum = Integer.parseInt(week);
                    Date startTemp = DataFormatUtil.getFirstDayOfWeek(yearNum, monthNum, weekNum);

                    String endyear = endtime.split("-")[0];
                    String endmonth = endtime.split("-")[1];
                    String endweek = endtime.split("-")[2];
                    int endyearNum = Integer.parseInt(endyear);
                    int endmonthNum = Integer.parseInt(endmonth);
                    int endweekNum = Integer.parseInt(endweek);
                    Date endTemp = DataFormatUtil.getLastDayOfWeek(endyearNum, endmonthNum, endweekNum);
                    starttime = DataFormatUtil.getDateYMD(startTemp) + " 00:00:00";
                    endtime = DataFormatUtil.getDateYMD(endTemp) + " 23:59:59";
                    break;
                case "month":
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime) + " 00:00:00";
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                    break;
                case "year":
                    starttime = DataFormatUtil.getYearFirst(starttime) + " 00:00:00";
                    endtime = DataFormatUtil.getYearLast(endtime) + " 23:59:59";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + timeType);
            }
            List<Map<String, Object>> tabledatalist = new ArrayList<>();
            List<String> mns;
            String mn;
            String monitorTime;
            List<Map<String, Object>> pollutantList;
            List<Map<String, Object>> pollutantData;

            Map<String, String> contrast = new HashMap<>();
            contrast.put("fkpollutantcode", "pollutantcode");
            contrast.put("fkmonitorpointid", "outputid");
            contrast.put("alarmtime", "monitortime");
            contrast.put("alarmtype", "alarmtype");


            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("fkmonitorpointtypecodes", monitortypes);
            paramMap.put("monitorpointids", monitorpointids);
            List<Map<String, Object>> allPointDataByTypes = pollutionService.getOutPutInfosByParamMap(paramMap);
            Map<String, String> MNAndMonitorPoint = allPointDataByTypes.stream().filter(m -> m.get("DGIMN") != null && m.get("OutputName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("OutputName").toString(), (a, b) -> a));
            Map<String, String> MNAndMonitorPointId = allPointDataByTypes.stream().filter(m -> m.get("DGIMN") != null && m.get("outputid") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("outputid").toString(), (a, b) -> a));
            Map<String, String> MNAndPollution = allPointDataByTypes.stream().filter(m -> m.get("DGIMN") != null && m.get("PollutionName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("PollutionName").toString(), (a, b) -> a));

            Map<String, List<String>> dgimnMap = allPointDataByTypes.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null && m.get("DGIMN") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString(), Collectors.mapping(m -> m.get("DGIMN").toString(), Collectors.toList())));

            for (Integer type : monitortypes) {
                mns = dgimnMap.get(type.toString());
                paramMap.put("pollutanttype", type);
                pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
                Map<String, Object> codeAndName = new HashMap<>();
                Map<String, Object> codeAndunit = new HashMap<>();
                for (Map<String, Object> pollutant : pollutantData) {
                    codeAndName.put(pollutant.get("code").toString(), pollutant.get("name"));
                    codeAndunit.put(pollutant.get("code").toString(), pollutant.get("unit"));
                }
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("issuddenchange", true);
                //1，浓度突变预警
                paramMap.put("collection", "HourData");
                List<Document> conChange = onlineService.getMonitorDataByParamMap(paramMap);
                if (conChange.size() > 0) {
                    for (Document con : conChange) {
                        mn = con.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(con.getDate("MonitorTime"));
                        pollutantList = (List<Map<String, Object>>) con.get("HourDataList");
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutant.get("IsSuddenChange") != null && Boolean.parseBoolean(pollutant.get("IsSuddenChange").toString())) {
                                Map<String, Object> rowMap = new HashMap<>();
                                rowMap.put("pollutionname", MNAndPollution.get(mn));
                                rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                                rowMap.put("outputid", MNAndMonitorPointId.get(mn));
                                rowMap.put("monitortime", monitorTime);
                                rowMap.put("pollutantname", codeAndName.get(pollutant.get("PollutantCode")));
                                rowMap.put("unit", codeAndunit.get(pollutant.get("PollutantCode")));
                                rowMap.put("pollutantcode", pollutant.get("PollutantCode"));
                                rowMap.put("monitorvalue", pollutant.get("AvgStrength"));
                                rowMap.put("type", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(type));
                                rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName());
                                if (pollutant.get("ChangeMultiple") != null && !pollutant.get("ChangeMultiple").equals("")) {
                                    Double changeMultiple = Double.valueOf(pollutant.get("ChangeMultiple").toString());
                                    BigDecimal b = new BigDecimal(changeMultiple);
                                    String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    rowMap.put("changemultiple", f1);
                                } else {
                                    rowMap.put("changemultiple", pollutant.get("ChangeMultiple"));
                                }
                                tabledatalist.add(rowMap);
                            }
                        }
                    }
                }
                //2，排放量突变预警
                paramMap.put("collection", "HourFlowData");
                List<Document> flowChange = onlineService.getHourFlowMonitorDataByParamMap(paramMap);
                if (flowChange.size() > 0) {
                    for (Document flow : flowChange) {
                        mn = flow.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(flow.getDate("MonitorTime"));
                        pollutantList = (List<Map<String, Object>>) flow.get("HourFlowDataList");
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (pollutant.get("IsSuddenChange") != null && Boolean.parseBoolean(pollutant.get("IsSuddenChange").toString())) {
                                Map<String, Object> rowMap = new HashMap<>();
                                rowMap.put("pollutionname", MNAndPollution.get(mn));
                                rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                                rowMap.put("outputid", MNAndMonitorPointId.get(mn));
                                rowMap.put("monitortime", monitorTime);
                                rowMap.put("pollutantname", codeAndName.get(pollutant.get("PollutantCode")));
                                rowMap.put("unit", codeAndunit.get(pollutant.get("PollutantCode")));
                                rowMap.put("pollutantcode", pollutant.get("PollutantCode"));
                                rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getName());
                                rowMap.put("type", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(type));
                                rowMap.put("monitorvalue", pollutant.get("CorrectedFlow"));
//                                rowMap.put("changemultiple", pollutant.get("ChangeMultiple"));
                                if (pollutant.get("ChangeMultiple") != null && !pollutant.get("ChangeMultiple").equals("")) {
                                    Double changeMultiple = Double.valueOf(pollutant.get("ChangeMultiple").toString());
                                    BigDecimal b = new BigDecimal(changeMultiple);
                                    String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    rowMap.put("changemultiple", f1);
                                } else {
                                    rowMap.put("changemultiple", pollutant.get("ChangeMultiple"));
                                }

                                tabledatalist.add(rowMap);
                            }
                        }
                    }
                }
                //3，超阈值预警
                if (datTypes.size() > 0) {
                    paramMap.put("datatypes", datTypes);
                }
                paramMap.put("monitortimekey", "EarlyWarnTime");
                paramMap.put("collection", "EarlyWarnData");
                List<Document> CYZData = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (CYZData.size() > 0) {
                    for (Document CYZ : CYZData) {
                        mn = CYZ.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(CYZ.getDate("EarlyWarnTime"));
                        Map<String, Object> rowMap = new HashMap<>();
                        rowMap.put("pollutionname", MNAndPollution.get(mn));
                        rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                        rowMap.put("outputid", MNAndMonitorPointId.get(mn));
                        rowMap.put("monitortime", monitorTime);
                        rowMap.put("pollutantname", codeAndName.get(CYZ.get("PollutantCode")));
                        rowMap.put("unit", codeAndunit.get(CYZ.get("PollutantCode")));
                        rowMap.put("pollutantcode", CYZ.get("PollutantCode"));
                        rowMap.put("monitorvalue", CYZ.get("MonitorValue"));
                        rowMap.put("type", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(type));
                        rowMap.put("alarmtype", EarlyAlarmEnum.getName());
                        rowMap.put("DataType", CommonTypeEnum.MongodbDataTypeEnum.getTextByName(CYZ.get("DataType") == null ? "" : CYZ.get("DataType").toString()));
                        rowMap.put("changemultiple", "");
                        tabledatalist.add(rowMap);
                    }
                }
                //4，数据超限报警
                paramMap.put("monitortimekey", "OverTime");
                paramMap.put("collection", "OverData");
                List<Document> OverData = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (OverData.size() > 0) {
                    for (Document over : OverData) {
                        mn = over.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(over.getDate("OverTime"));

                        Map<String, Object> rowMap = new HashMap<>();
                        rowMap.put("pollutionname", MNAndPollution.get(mn));
                        rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                        rowMap.put("outputid", MNAndMonitorPointId.get(mn));
                        rowMap.put("monitortime", monitorTime);
                        rowMap.put("pollutantname", codeAndName.get(over.get("PollutantCode")));
                        rowMap.put("unit", codeAndunit.get(over.get("PollutantCode")));
                        rowMap.put("type", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(type));
                        rowMap.put("pollutantcode", over.get("PollutantCode"));
                        rowMap.put("monitorvalue", over.get("MonitorValue"));
                        rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getName());
                        rowMap.put("DataType", CommonTypeEnum.MongodbDataTypeEnum.getTextByName(over.get("DataType") == null ? "" : over.get("DataType").toString()));
//                        rowMap.put("changemultiple", over.get("ChangeMultiple"));

                        if (over.get("ChangeMultiple") != null && !over.get("ChangeMultiple").equals("")) {
                            Double changeMultiple = Double.valueOf(over.get("ChangeMultiple").toString());
                            BigDecimal b = new BigDecimal(changeMultiple);
                            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                            rowMap.put("changemultiple", f1);
                        } else {
                            rowMap.put("changemultiple", over.get("ChangeMultiple"));
                        }
                        tabledatalist.add(rowMap);
                    }
                }
                //5，数据异常报警
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("collection", "ExceptionData");
                List<Document> exceptionData = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (exceptionData.size() > 0) {
                    for (Document exception : exceptionData) {
                        mn = exception.getString("DataGatherCode");
                        monitorTime = DataFormatUtil.getDateYMDHMS(exception.getDate("ExceptionTime"));
                        Map<String, Object> rowMap = new HashMap<>();
                        rowMap.put("pollutionname", MNAndPollution.get(mn));
                        rowMap.put("outputname", MNAndMonitorPoint.get(mn));
                        rowMap.put("outputid", MNAndMonitorPointId.get(mn));
                        rowMap.put("monitortime", monitorTime);
                        rowMap.put("pollutantname", codeAndName.get(exception.get("PollutantCode")));
                        rowMap.put("unit", codeAndunit.get(exception.get("PollutantCode")));
                        rowMap.put("type", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(type));
                        rowMap.put("pollutantcode", exception.get("PollutantCode"));
                        rowMap.put("monitorvalue", exception.get("MonitorValue"));
                        rowMap.put("alarmtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getName());
                        rowMap.put("DataType", CommonTypeEnum.MongodbDataTypeEnum.getTextByName(exception.get("DataType") == null ? "" : exception.get("DataType").toString()));
                        rowMap.put("changemultiple", "");
                        tabledatalist.add(rowMap);
                    }
                }
                //设置污染物标准值
                pollutantCommonService.setPollutantStandardValueDataByParam(tabledatalist, contrast, type.toString());
                //设置报警任务状态
                pollutantCommonService.setAlarmTaskStatusDataByParam(tabledatalist, contrast);
                //设置运维信息
                pollutantCommonService.setUnexpiredDeviceDevOpsDataByParam(tabledatalist, contrast, type.toString());

            }
            //排序+分页
            if (tabledatalist.size() > 0) {
                Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                if (comparebynum != null) {
                    tabledatalist = tabledatalist.stream().sorted(comparebynum).collect(Collectors.toList());
                }
            }
            if (pagesize != null && pagenum != null) {
                List<Map<String, Object>> dataList = getPageData(tabledatalist, pagenum, pagesize);
                resultMap.put("total", tabledatalist.size());
                resultMap.put("datalist", dataList);
            } else {
                resultMap.put("datalist", tabledatalist);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void setTimeAndRemindTypeAndNum(Map<String, Map<Integer, Integer>> timeAndRemindTypeAndNum, List<Document> documents, Integer remindType) {
        String time;
        Integer countNum;
        Map<Integer, Integer> remindTypeAndNum;
        for (Document document : documents) {
            if (remindType == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                String ExceptionType = document.getString("ExceptionType");
                if (ExceptionType.equals(CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum.getCode())) {
                    remindType = CommonTypeEnum.RemindTypeEnum.ZeroExceptionAlarmEnum.getCode();
                } else {
                    remindType = CommonTypeEnum.RemindTypeEnum.ContinuousExceptionAlarmEnum.getCode();
                }
            }
            time = document.getString("MonitorDate");
            if (time == null) {
                time = document.getString("_id");
            }
            countNum = document.getInteger("countnum");
            if (timeAndRemindTypeAndNum.containsKey(time)) {
                remindTypeAndNum = timeAndRemindTypeAndNum.get(time);
            } else {
                remindTypeAndNum = new HashMap<>();
            }
            if (remindTypeAndNum.containsKey(remindType)) {
                remindTypeAndNum.put(remindType, remindTypeAndNum.get(remindType) + countNum);
            } else {
                remindTypeAndNum.put(remindType, countNum);
            }
            timeAndRemindTypeAndNum.put(time, remindTypeAndNum);
        }
    }


    private void setIdAndRemindTypeAndNum(Map<String, Map<Integer, Integer>> idAndRemindTypeAndNum, List<Document> documents, Integer remindType, Map<String, String> allMNAndPollutionId) {
        String mnCommon;
        String pollutionId;
        Integer countNum;
        Map<Integer, Integer> remindTypeAndNum;
        for (Document document : documents) {
            if (remindType == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                String ExceptionType = document.getString("ExceptionType");
                if (ExceptionType.equals(CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum.getCode())) {
                    remindType = CommonTypeEnum.RemindTypeEnum.ZeroExceptionAlarmEnum.getCode();
                } else {
                    remindType = CommonTypeEnum.RemindTypeEnum.ContinuousExceptionAlarmEnum.getCode();
                }
            }
            mnCommon = document.getString("DataGatherCode");
            if (mnCommon == null) {
                mnCommon = document.getString("_id");
            }
            pollutionId = allMNAndPollutionId.get(mnCommon);
            if (pollutionId != null) {
                countNum = document.getInteger("countnum");
                if (idAndRemindTypeAndNum.containsKey(pollutionId)) {
                    remindTypeAndNum = idAndRemindTypeAndNum.get(pollutionId);
                } else {
                    remindTypeAndNum = new HashMap<>();
                }
                if (remindTypeAndNum.containsKey(remindType)) {
                    remindTypeAndNum.put(remindType, remindTypeAndNum.get(remindType) + countNum);
                } else {
                    remindTypeAndNum.put(remindType, countNum);
                }
                idAndRemindTypeAndNum.put(pollutionId, remindTypeAndNum);
            }
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/3 0003 下午 5:03
     * @Description: 获取告警提醒信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getAlarmRemindDataFromRedis", method = RequestMethod.POST)
    public Object getAlarmRemindDataFromRedis(@RequestJson(value = "sessionid", required = false) String sessionid) throws ParseException {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            if (StringUtils.isBlank(sessionid)) {
                sessionid = SessionUtil.getSessionID();
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            if (StringUtils.isNotBlank(userId)) {
                List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByToken("userauth", List.class);
                JSONArray jsonArray = RedisTemplateUtil.getCache("alarmremind", JSONArray.class) == null ? new JSONArray() : RedisTemplateUtil.getCache("alarmremind", JSONArray.class);
                List<Map<String, Object>> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    boolean isread = true;
                    Map map = (Map) jsonArray.get(i);
                    List<String> pollutants = new ArrayList<>();
                    List<String> alarms = new ArrayList<>();
                    List<String> alarmtype = new ArrayList<>();
                    List<String> mns = new ArrayList<>();
                    List<Map<String, Object>> alarmData = map.get("AlarmData") == null ? new ArrayList() : (List) map.get("AlarmData");
                    List<Integer> remindType = new ArrayList<>();
                    for (Map<String, Object> stringObjectMap : alarmData) {
                        List<String> alarmTypes = stringObjectMap.get("AlarmType") == null ? new ArrayList() : (List) stringObjectMap.get("AlarmType");
                        String monitorPointTypeCode = stringObjectMap.get("MonitorPointTypeCode") == null ? "" : stringObjectMap.get("MonitorPointTypeCode").toString();
                        String MN = stringObjectMap.get("MN") == null ? "" : stringObjectMap.get("MN").toString();
                        for (String alarmType : alarmTypes) {
                            if (objectList != null && objectList.size() > 0) {
                                boolean isHaveRight = OnlineServiceImpl.isHaveRight(objectList, getMenuCodeByType(monitorPointTypeCode + "_" + alarmType));
                                if (isHaveRight) {
                                    pollutants.add(stringObjectMap.get("PollutantName") == null ? "" : stringObjectMap.get("PollutantName").toString());
                                    alarms.add(getNameByString(getMenuCodeByType(monitorPointTypeCode + "_" + alarmType).get(0)));
                                    alarmtype.add(alarmType);
                                    mns.add(MN);
                                }
                            }
                            remindType.add(CommonTypeEnum.RemindTypeCodeEnum.getCodeByString(alarmType));
                        }
                    }
                    if (pollutants.size() > 0 && alarms.size() > 0) {
                        List<String> collect = alarmtype.stream().distinct().collect(Collectors.toList());
                        List<String> collect1 = mns.stream().filter(m -> !"".equals(m)).distinct().collect(Collectors.toList());
                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("dgimns", collect1);
                        map1.put("monitortime", map.get("DateTime"));
                        for (String s : collect) {
                            if (CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName().equals(s)) {
                                map1.put("collect", hourDataCollect);
                                map1.put("datalist", "HourDataList");
                                map1.put("monitortimetype", "MonitorTime");
                            } else if (CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getName().equals(s)) {
                                map1.put("collect", hourDataCollect);
                                map1.put("datalist", "HourFlowDataList");
                                map1.put("monitortimetype", "MonitorTime");
                            } else if (EarlyWarnEnum.getName().equals(s)) {
                                map1.put("collect", earlyWarnDataCollect);
                                map1.put("monitortimetype", "EarlyWarnTime");
                            } else if (ExceptionEnum.getName().equals(s)) {
                                map1.put("collect", exceptionDataCollect);
                                map1.put("monitortimetype", "ExceptionTime");
                            } else if (OverLimitEnum.getName().equals(s)) {
                                map1.put("collect", overDataCollect);
                                map1.put("monitortimetype", "OverTime");
                            } else if (OverStandardEnum.getName().equals(s)) {
                                map1.put("collect", overDataCollect);
                                map1.put("monitortimetype", "OverTime");
                            }
                            List<Map> readUserIdsByParamMap = onlineService.getReadUserIdsByParamMap(map1);
                            /*if(readUserIdsByParamMap.size()==0){
                                isread=false;
                                break;
                            }*/
                            for (Map map2 : readUserIdsByParamMap) {
                                List<String> readUserIds = (List<String>) map2.get("ReadUserIds");
                                if (readUserIds == null || !readUserIds.contains(usercode)) {
                                    isread = false;
                                    break;
                                }
                            }
                        }
                        map.put("remindType", remindType.stream().distinct().collect(Collectors.toList()));
                        map.put("isread", isread);
                        map.put("PollutantName", pollutants.stream().distinct().collect(Collectors.joining("、")));
                        map.put("AlarmType", alarms.stream().distinct().collect(Collectors.toList()));
                        map.put("dgimns", collect1);
                        map.remove("AlarmData");
                        result.add(map);
                    }
                }

                resultMap.put("userid", userId);
                resultMap.put("datalist", result.stream().filter(m -> m.get("DateTime") != null).sorted(Comparator.comparing(m -> {
                    try {
                        return format.parse(((Map) m).get("DateTime").toString()).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0l;
                    }
                }).reversed()).collect(Collectors.toList()));
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/31 11:15
     * @Description: 获取报警详情信息
     * @updateUser:xsm
     * @updateDate:2021/04/27 10:31
     * @updateDescription:去掉突变 只统计超标 异常数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmDataForPollutionByParam", method = RequestMethod.POST)
    public Object countAlarmDataForPollutionByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "timetype") String timeType,
            @RequestJson(value = "remindtypes") List<Integer> remindtypes
    ) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            switch (timeType) {
                case "day":
                    break;
                case "week":
                    break;
                case "month":
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + timeType);
            }
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            List<String> mns = new ArrayList<>();
            Map<String, String> MNAndPollution;
            Map<String, String> MNAndPollutionId;

            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitorPointTypes = new ArrayList<>();
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            Map<String, Object> pidandalarmnum = new HashMap<>();
            //monitorPointTypes.add();
            Map<String, Map<String, String>> pomap = new HashMap<>();
            Map<String, String> AllMNAndPollution = new HashMap<>();
            Map<String, String> AllMNAndPollutionId = new HashMap<>();
            for (Integer type : monitorPointTypes) {
                pomap = onlineService.getMNAndShortName(new ArrayList<>(), type);
                MNAndPollution = pomap.get("mnandpollution");
                MNAndPollutionId = pomap.get("mnandpollutionid");
                AllMNAndPollution.putAll(MNAndPollution);
                AllMNAndPollutionId.putAll(MNAndPollutionId);
            }
            Map<String, String> idAndName = new HashMap<>();
            for (String mnIndex : AllMNAndPollutionId.keySet()) {
                idAndName.put(AllMNAndPollutionId.get(mnIndex), AllMNAndPollution.get(mnIndex));
            }
            mns.addAll(AllMNAndPollutionId.keySet());
            Integer remindType;
            Map<String, Map<Integer, Integer>> idAndRemindTypeAndNum = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", mns);
            remindType = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            if (remindtypes.contains(remindType)) {
                //1，浓度突变预警
                paramMap.put("collection", "MinuteData");
                paramMap.put("timeKey", "MonitorTime");
                paramMap.put("unwindkey", "MinuteDataList");
                List<Document> conChange = onlineCountAlarmService.countDataGroupByMNByParam(paramMap);
                if (conChange.size() > 0) {
                    setIdAndRemindTypeAndNum(idAndRemindTypeAndNum, conChange, remindType, AllMNAndPollutionId);
                }
            }
            remindType = CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode();
            if (remindtypes.contains(remindType)) {
                //2浓度超阈值
                paramMap.remove("unwindkey");
                paramMap.put("collection", "EarlyWarnData");
                paramMap.put("timeKey", "EarlyWarnTime");
                List<Document> CYZData = onlineCountAlarmService.countDataGroupByMNByParam(paramMap);
                if (CYZData.size() > 0) {
                    setIdAndRemindTypeAndNum(idAndRemindTypeAndNum, CYZData, remindType, AllMNAndPollutionId);
                }
            }
            //3，数据超限报警
            remindType = CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode();
            if (remindtypes.contains(remindType)) {
                paramMap.remove("unwindkey");
                paramMap.put("collection", "OverData");
                paramMap.put("timeKey", "OverTime");
                List<Document> OverData = onlineCountAlarmService.countDataGroupByMNByParam(paramMap);
                if (OverData.size() > 0) {
                    setIdAndRemindTypeAndNum(idAndRemindTypeAndNum, OverData, remindType, AllMNAndPollutionId);
                }
            }
            //4，数据异常报警
            remindType = CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode();
            if (remindtypes.contains(remindType)) {
                paramMap.remove("unwindkey");
                paramMap.put("collection", "ExceptionData");
                paramMap.put("timeKey", "ExceptionTime");
                paramMap.put("remindtype", remindType);
                List<Document> exceptionData = onlineCountAlarmService.countDataGroupByMNByParam(paramMap);
                if (exceptionData.size() > 0) {
                    setIdAndRemindTypeAndNum(idAndRemindTypeAndNum, exceptionData, remindType, AllMNAndPollutionId);
                }
            }
            if (idAndRemindTypeAndNum.size() > 0) {
                Map<Integer, Integer> typeAndNum;
                int totalNum;
                for (String idInex : idAndRemindTypeAndNum.keySet()) {
                    totalNum = 0;
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("pollutionid", idInex);
                    resultMap.put("pollutionname", idAndName.get(idInex));
                    typeAndNum = idAndRemindTypeAndNum.get(idInex);
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    for (Integer type : typeAndNum.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("alarmtypecode", type);
                        dataMap.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.getObjectByCode(type).getName());
                        totalNum += typeAndNum.get(type);
                        dataMap.put("alarmnum", typeAndNum.get(type));
                        dataList.add(dataMap);
                    }
                    resultMap.put("datalist", dataList);
                    if (remindtypes.contains(CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode())) {
                        if (pidandalarmnum != null && pidandalarmnum.get(idInex) != null) {
                            totalNum = totalNum + Integer.parseInt(pidandalarmnum.get(idInex).toString());
                        }
                    }
                    resultMap.put("totalNum", totalNum);
                    resultList.add(resultMap);
                }
                //排序
                resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("totalNum").toString())).reversed()).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/31 11:15
     * @Description: 获取报警详情信息
     * @updateUser:xsm
     * @updateDate:2021/04/27 12:00
     * @updateDescription:不统计分钟突变 预警 只统计超标 异常
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmDataForTimeByParam", method = RequestMethod.POST)
    public Object countAlarmDataForTimeByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "timetype") String timeType,
            @RequestJson(value = "remindtypes") List<Integer> remindtypes
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            switch (timeType) {
                case "day":
                    break;
                case "week":
                    break;
                case "month":
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + timeType);
            }
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            List<String> mns = new ArrayList<>();
            Map<String, String> MNAndPollutionId;
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitorPointTypes = new ArrayList<>();
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
            monitorPointTypes.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            Map<String, Object> dateandalarmnum = new HashMap<>();
            Map<String, String> AllMNAndPollution = new HashMap<>();
            Map<String, String> AllMNAndPollutionId = new HashMap<>();
            for (Integer type : monitorPointTypes) {
                MNAndPollutionId = onlineService.getMNAndPollutionId(new ArrayList<>(), type);
                AllMNAndPollutionId.putAll(MNAndPollutionId);
            }
            Map<String, String> idAndName = new HashMap<>();
            for (String mnIndex : AllMNAndPollutionId.keySet()) {
                idAndName.put(AllMNAndPollutionId.get(mnIndex), AllMNAndPollution.get(mnIndex));
            }
            mns.addAll(AllMNAndPollutionId.keySet());
            Integer remindType;
            Map<String, Map<Integer, Integer>> timeAndRemindTypeAndNum = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("timetype", timeType);
            paramMap.put("mns", mns);
            //3，数据超限报警
            remindType = CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode();
            if (remindtypes.contains(remindType)) {
                paramMap.remove("unwindkey");
                paramMap.put("collection", "OverData");
                paramMap.put("timeKey", "OverTime");
                List<Document> OverData = onlineCountAlarmService.countDataGroupByTimeByParam(paramMap);
                if (OverData.size() > 0) {
                    setTimeAndRemindTypeAndNum(timeAndRemindTypeAndNum, OverData, remindType);
                }
            }
            //4，数据异常报警
            remindType = CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode();
            if (remindtypes.contains(remindType)) {
                paramMap.remove("unwindkey");
                paramMap.put("collection", "ExceptionData");
                paramMap.put("timeKey", "ExceptionTime");
                paramMap.put("remindtype", remindType);
                List<Document> exceptionData = onlineCountAlarmService.countDataGroupByTimeByParam(paramMap);
                if (exceptionData.size() > 0) {
                    setTimeAndRemindTypeAndNum(timeAndRemindTypeAndNum, exceptionData, remindType);
                }
            }
            if (timeAndRemindTypeAndNum.size() > 0) {
                Map<Integer, Integer> typeAndNum;
                int totalNum;
                for (String timeInex : timeAndRemindTypeAndNum.keySet()) {
                    totalNum = 0;
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitortime", timeInex);
                    typeAndNum = timeAndRemindTypeAndNum.get(timeInex);
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    for (Integer type : typeAndNum.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("alarmtypecode", type);
                        dataMap.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.getObjectByCode(type).getName());
                        totalNum += typeAndNum.get(type);
                        dataMap.put("alarmnum", typeAndNum.get(type));
                        dataList.add(dataMap);
                    }
                    resultMap.put("datalist", dataList);
                    if (remindtypes.contains(CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode())) {
                        if (dateandalarmnum != null && dateandalarmnum.get(timeInex) != null) {
                            totalNum = totalNum + Integer.parseInt(dateandalarmnum.get(timeInex).toString());
                        }
                    }
                    resultMap.put("totalNum", totalNum);
                    resultList.add(resultMap);
                }
                //排序
                resultList = resultList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }


    /**
     * @author: lip
     * @date: 2020/05/06 11:15
     * @Description: 统计首页报警数据(汕头首页)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countHomePageAlarmData", method = RequestMethod.POST)
    public Object countHomePageAlarmData(
            @RequestJson(value = "monitortime", required = false) String monitortime,
            @RequestJson(value = "monitorpointtypecodes", required = false) List<Integer> monitorpointtypecodes,
            @RequestJson(value = "remindcodes", required = false) List<Integer> remindcodes,
            @RequestJson(value = "taskstatuscodes", required = false) List<String> taskstatuscodes,
            @RequestJson(value = "searchname", required = false) String searchname) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            if (StringUtils.isBlank(monitortime)) {
                monitortime = DataFormatUtil.getDateYMD(new Date());
            }
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String startTime = monitortime + " 00:00:00";
            String endTime = monitortime + " 23:59:59";
            Map<String, Object> mnData = getMnDataByParam(monitorpointtypecodes, searchname);
            if (mnData.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", mnData.get("mns"));
                paramMap.put("starttime", startTime);
                paramMap.put("endtime", endTime);
                List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
                Map<String, Object> codeAndName = new HashMap<>();
                Map<String, Object> codeAndUnit = new HashMap<>();
                if (pollutantData.size() > 0) {
                    String key;
                    for (Map<String, Object> pollutant : pollutantData) {
                        key = pollutant.get("code").toString() + "#" + pollutant.get("pollutanttype");
                        if (pollutant.get("name") != null) {
                            codeAndName.put(key, pollutant.get("name"));
                        }
                        if (pollutant.get("unit") != null) {
                            codeAndUnit.put(key, pollutant.get("unit"));
                        }
                    }
                }
                Map<String, List<Map<String, Object>>> mnAndPollutantDataList;
                String pollutionId;
                Map<String, Object> mnAndType = (Map<String, Object>) mnData.get("mnAndType");
                Map<String, Object> mnAndPollutionId = (Map<String, Object>) mnData.get("mnAndPollutionId");
                Map<String, Object> mnAndPollutionName = (Map<String, Object>) mnData.get("mnAndPollutionName");
                Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) mnData.get("mnAndMonitorPointId");
                Map<String, Object> mnAndMonitorPointName = (Map<String, Object>) mnData.get("mnAndMonitorPointName");
                List<Integer> exceptionType = new ArrayList<>();
                List<Integer> exceptionTypeTemp = Arrays.asList(ExceptionAlarmEnum.getCode());
                List<Integer> noExceptionType = new ArrayList<>();
                List<Integer> noExceptionTypeTemp = Arrays.asList(CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode(),
                        EarlyAlarmEnum.getCode(), OverAlarmEnum.getCode());
                if (remindcodes != null && remindcodes.size() > 0) {
                    for (Integer code : exceptionTypeTemp) {
                        if (remindcodes.contains(code)) {
                            exceptionType.add(code);
                        }
                    }
                    for (Integer code : noExceptionTypeTemp) {
                        if (remindcodes.contains(code)) {
                            noExceptionType.add(code);
                        }
                    }
                } else {
                    exceptionType = exceptionTypeTemp;
                    noExceptionType = noExceptionTypeTemp;

                }
                List<Map<String, Object>> dataList = new ArrayList<>();
                //非异常提醒
                List<String> pollutionIds = new ArrayList<>();
                List<String> pointids = new ArrayList<>();
                //查当日最新的报警数据
                paramMap.put("tasktypelist", Arrays.asList(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()));
                //paramMap.put("pollutionids", pollutionIds);
                paramMap.put("userid", userId);
                //获取所有点位当日最新的报警数据
                List<Map<String, Object>> taskDisposeManagementDataList = alarmTaskDisposeService.getTaskDisposeManagementDataByParam(paramMap);
                for (Integer code : noExceptionType) {
                    Map<String, Date> mnAndTime = new HashMap<>();
                    mnAndPollutantDataList = getMonitorDataByParam(paramMap, code, mnAndTime, mnData, codeAndName, codeAndUnit, mnAndType);
                    if (mnAndPollutantDataList.size() > 0) {
                        Date tempTime;
                        Map<String, Date> pollutionIdAndLastTime = new HashMap<>();
                        Map<String, Date> mnAndLastTime = new HashMap<>();
                        for (String mnIndex : mnAndTime.keySet()) {
                            tempTime = mnAndTime.get(mnIndex);
                            if (code == OverAlarmEnum.getCode() || code == ConcentrationChangeEnum.getCode()) {//报警、浓度突变信息以点位为主
                                if (mnAndLastTime.containsKey(mnIndex)) {
                                    if (tempTime.after(mnAndLastTime.get(mnIndex))) {
                                        mnAndLastTime.put(mnIndex, tempTime);
                                    }
                                } else {
                                    mnAndLastTime.put(mnIndex, tempTime);
                                }
                            } else {
                                if (mnAndPollutionId.get(mnIndex) != null && !"".equals(mnAndPollutionId.get(mnIndex))) {//企业相关：合并点位
                                    pollutionId = mnAndPollutionId.get(mnIndex).toString();
                                    if (pollutionIdAndLastTime.containsKey(pollutionId)) {
                                        if (tempTime.after(pollutionIdAndLastTime.get(pollutionId))) {
                                            pollutionIdAndLastTime.put(pollutionId, tempTime);
                                        }
                                    } else {
                                        pollutionIdAndLastTime.put(pollutionId, tempTime);
                                    }
                                } else {
                                    if (mnAndLastTime.containsKey(mnIndex)) {
                                        if (tempTime.after(mnAndLastTime.get(mnIndex))) {
                                            mnAndLastTime.put(mnIndex, tempTime);
                                        }
                                    } else {
                                        mnAndLastTime.put(mnIndex, tempTime);
                                    }
                                }
                            }
                        }
                        for (String pollutionIdIndex : pollutionIdAndLastTime.keySet()) {
                            Map<String, Object> pollutionData = new HashMap<>();
                            pollutionData.put("remindcode", code);
                            pollutionData.put("remindname", CommonTypeEnum.RemindTypeEnum.getObjectByCode(code).getName());
                            pollutionData.put("lasttime", DataFormatUtil.getDateHMS(pollutionIdAndLastTime.get(pollutionIdIndex)));
                            pollutionData.put("pollutionid", pollutionIdIndex);
                            //pollutionIds.add(pollutionIdIndex);
                            List<Map<String, Object>> pointDataList = new ArrayList<>();
                            for (String mnIndex : mnAndPollutantDataList.keySet()) {
                                tempTime = mnAndTime.get(mnIndex);
                                if (pollutionIdAndLastTime.get(pollutionIdIndex).equals(tempTime) && mnAndPollutionId.get(mnIndex).equals(pollutionIdIndex)) {
                                    pollutionData.putIfAbsent("pollutionname", mnAndPollutionName.get(mnIndex));
                                    Map<String, Object> pointData = new HashMap<>();
                                    pointData.put("monitorpointtypecode", mnAndType.get(mnIndex));
                                    pointData.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                                    pointData.put("dgimn", mnIndex);
                                    pointData.put("monitorpointname", mnAndMonitorPointName.get(mnIndex));
                                    pointData.put("pollutantlist", mnAndPollutantDataList.get(mnIndex));
                                    pointDataList.add(pointData);
                                }
                            }
                            pollutionData.put("pointdatalist", pointDataList);
                            dataList.add(pollutionData);
                        }
                        for (String mnIndexOut : mnAndLastTime.keySet()) {
                            Map<String, Object> pollutionData = new HashMap<>();
                            pollutionData.put("remindcode", code);
                            pollutionData.put("remindname", CommonTypeEnum.RemindTypeEnum.getObjectByCode(code).getName());
                            pollutionData.put("monitortime", DataFormatUtil.getDateYMDHMS(mnAndLastTime.get(mnIndexOut)));
                            pollutionData.put("lasttime", DataFormatUtil.getDateHMS(mnAndLastTime.get(mnIndexOut)));
                            pollutionData.put("pollutionid", mnAndPollutionId.get(mnIndexOut) != null ? mnAndPollutionId.get(mnIndexOut) : "");
                            pollutionData.put("monitorpointid", mnAndMonitorPointId.get(mnIndexOut));
                            List<Map<String, Object>> pointDataList = new ArrayList<>();
                            pollutionData.putIfAbsent("pollutionname", mnAndPollutionName.get(mnIndexOut) != null ? mnAndPollutionName.get(mnIndexOut) : "");
                            Map<String, Object> pointData = new HashMap<>();
                            pointData.put("monitorpointtypecode", mnAndType.get(mnIndexOut));

                            pollutionIds.add(mnAndMonitorPointId.get(mnIndexOut) + "");

                            pointData.put("monitorpointid", mnAndMonitorPointId.get(mnIndexOut));
                            pointData.put("dgimn", mnIndexOut);
                            pointData.put("monitorpointname", mnAndMonitorPointName.get(mnIndexOut));
                            pointData.put("pollutantlist", mnAndPollutantDataList.get(mnIndexOut));
                            pointDataList.add(pointData);
                            pollutionData.put("pointdatalist", pointDataList);
                            dataList.add(pollutionData);
                        }
                    }
                }
                //添加环保信息+报警任务信息
                Integer overCode = CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode();
                //突变任务
                Integer tbCode = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
                if (pollutionIds.size() > 0) {
                    // paramMap.put("tasktypelist",  Arrays.asList(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode(),CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()));
                    //paramMap.put("pollutionids", pollutionIds);
                    paramMap.put("userid", userId);
                    //获取所有点位当日最新的报警数据
                    //List<Map<String, Object>> taskDisposeManagementDataList = alarmTaskDisposeService.getTaskDisposeManagementDataByParam(paramMap);
                    if (taskDisposeManagementDataList.size() > 0) {
                        Map<String, Map<String, Object>> idandtaskinfo = new HashMap<>();
                        for (Map<String, Object> taskDisposeManagementData : taskDisposeManagementDataList) {
                            if (taskDisposeManagementData.get("FK_Pollutionid") != null) {
                                idandtaskinfo.put(taskDisposeManagementData.get("FK_Pollutionid") + "_" + taskDisposeManagementData.get("FK_TaskType"), taskDisposeManagementData);
                            }
                        }
                        //根据查询条件筛选任务
                        List<Map<String, Object>> tempDataList = new ArrayList<>();
                        for (Map<String, Object> dataMap : dataList) {
                            if (dataMap.get("remindcode").toString().equals(overCode.toString()) ||
                                    dataMap.get("remindcode").toString().equals(tbCode.toString())) {
                                String id = dataMap.get("monitorpointid").toString();
                                String taskstatus = "";
                                dataMap.put("taskid", "");
                                dataMap.put("tasktype", "");
                                dataMap.put("taskcreatetime", "");
                                dataMap.put("alarmstarttime", "");
                                dataMap.put("taskendtime", "");
                                dataMap.put("usernames", "");
                                dataMap.put("taskstatuscode", "");
                                dataMap.put("taskstatusname", "");
                                if (dataMap.get("remindcode").toString().equals(overCode.toString())) {
                                    id = id + "_" + CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode();
                                } else if (dataMap.get("remindcode").toString().equals(tbCode.toString())) {
                                    id = id + "_" + CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode();
                                }
                                if (idandtaskinfo.get(id) != null) {
                                    String monitor_time = dataMap.get("monitortime") != null ? dataMap.get("monitortime").toString() : "";
                                    Map<String, Object> taskobj = idandtaskinfo.get(id);
                                    //判断报警时间是否在任务开始 结束时间范围内
                                    if (!"".equals(monitor_time) && taskobj.get("AlarmStartTime") != null && !"".equals(taskobj.get("AlarmStartTime").toString())
                                            && taskobj.get("TaskEndTime") != null && !"".equals(taskobj.get("TaskEndTime").toString())) {
                                        String start = taskobj.get("AlarmStartTime").toString();
                                        String end = taskobj.get("TaskEndTime").toString();
                                        boolean flag_one = false;
                                        boolean flag_two = false;
                                        try {

                                            if (monitor_time.equals(start)) {
                                                flag_one = true;
                                            } else {
                                                if (compare(start, monitor_time)) {
                                                    flag_one = true;
                                                } else {
                                                    flag_one = false;
                                                }
                                            }
                                            if (monitor_time.equals(end)) {
                                                flag_two = false;
                                            } else {
                                                if (compare(end, monitor_time)) {
                                                    flag_two = true;
                                                } else {
                                                    flag_two = false;
                                                }
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        if (flag_one == true && flag_two == false) {
                                            taskstatus = taskobj.get("TaskStatus") != null ? taskobj.get("TaskStatus").toString() : "";
                                            dataMap.put("taskid", taskobj.get("PK_TaskID"));
                                            dataMap.put("tasktype", taskobj.get("FK_TaskType"));
                                            dataMap.put("taskcreatetime", taskobj.get("TaskCreateTime"));
                                            dataMap.put("alarmstarttime", taskobj.get("AlarmStartTime"));
                                            dataMap.put("taskendtime", taskobj.get("TaskEndTime"));
                                            dataMap.put("usernames", taskobj.get("user_name"));
                                            dataMap.put("taskstatuscode", taskstatus);
                                            dataMap.put("taskstatusname", taskobj.get("statusname"));
                                        }
                                    }
                                }
                                if (taskstatuscodes != null && taskstatuscodes.size() > 0) {
                                    if (!"".equals(taskstatus) && taskstatuscodes.contains(Integer.parseInt(taskstatus))) {
                                        tempDataList.add(dataMap);
                                    }
                                }
                            }
                        }
                        //有任务状态参数
                        if (taskstatuscodes != null && taskstatuscodes.size() > 0) {
                            dataList = tempDataList;
                        }
                    }
                    //添加环保信息
                    paramMap.putIfAbsent("pollutionids", pollutionIds);
                    List<Map<String, Object>> pollutionDataList = pollutionService.getPollutionNameAndPkid(paramMap);
                    if (pollutionDataList.size() > 0) {
                        Map<String, Object> pollutionIdAndEnvironmentalManager = new HashMap<>();
                        Map<String, Object> pollutionIdAndLinkManPhone = new HashMap<>();
                        for (Map<String, Object> pollutionData : pollutionDataList) {
                            pollutionIdAndEnvironmentalManager.put(pollutionData.get("pollutionid").toString(), pollutionData.get("EnvironmentalManager"));
                            pollutionIdAndLinkManPhone.put(pollutionData.get("pollutionid").toString(), pollutionData.get("LinkManPhone"));
                        }
                        for (Map<String, Object> dataMap : dataList) {
                            Map<String, Object> hbdata = new HashMap<>();
                            hbdata.put("leader", pollutionIdAndEnvironmentalManager.get(dataMap.get("pollutionid")));
                            hbdata.put("phone", pollutionIdAndLinkManPhone.get(dataMap.get("pollutionid")));
                            dataMap.put("hbdata", hbdata);
                        }
                    }
                }
                //异常提醒
                pollutionIds.clear();
                monitorpointtypecodes = new ArrayList<>();
                List<String> monitorpointIds = new ArrayList<>();
                for (Integer code : exceptionType) {
                    Map<String, Date> mnAndTime = new HashMap<>();
                    mnAndPollutantDataList = getMonitorDataByParam(paramMap, code, mnAndTime, mnData, codeAndName, codeAndUnit, mnAndType);
                    if (mnAndPollutantDataList.size() > 0) {
                        Date tempTime;
                        Map<String, Date> mnAndLastTime = new HashMap<>();
                        for (String mnIndex : mnAndTime.keySet()) {
                            tempTime = mnAndTime.get(mnIndex);
                            if (mnAndLastTime.containsKey(mnIndex)) {
                                if (tempTime.after(mnAndLastTime.get(mnIndex))) {
                                    mnAndLastTime.put(mnIndex, tempTime);
                                }
                            } else {
                                mnAndLastTime.put(mnIndex, tempTime);
                            }
                        }
                        for (String mnIndexOut : mnAndLastTime.keySet()) {
                            Map<String, Object> pollutionData = new HashMap<>();
                            pollutionData.put("remindcode", code);
                         /*  if (code==4){//铅山 异常报警==》异常提醒
                                pollutionData.put("remindname", "异常提醒");
                            }else{*/
                            pollutionData.put("remindname", CommonTypeEnum.RemindTypeEnum.getObjectByCode(code).getName());
                            //}
                            pollutionData.put("lasttime", DataFormatUtil.getDateHMS(mnAndLastTime.get(mnIndexOut)));
                            pollutionData.put("monitortime", DataFormatUtil.getDateYMDHMS(mnAndLastTime.get(mnIndexOut)));
                            pollutionData.put("pollutionid", mnAndPollutionId.get(mnIndexOut));
                            pollutionData.put("monitorpointtypecode", mnAndType.get(mnIndexOut));
                            pollutionData.put("pointid", mnAndMonitorPointId.get(mnIndexOut));
                            pointids.add(mnAndMonitorPointId.get(mnIndexOut).toString());
                            if (mnAndPollutionId.get(mnIndexOut) != null) {
                                pollutionIds.add(mnAndPollutionId.get(mnIndexOut).toString());
                                pollutionData.put("monitorpointid", "");
                                pollutionData.put("dgimn", "");
                            } else {
                                pollutionData.put("monitorpointid", mnAndMonitorPointId.get(mnIndexOut));
                                pollutionData.put("dgimn", mnIndexOut);
                                monitorpointIds.add(mnAndMonitorPointId.get(mnIndexOut).toString());
                            }
                            pollutionData.putIfAbsent("pollutionname", mnAndPollutionName.get(mnIndexOut));

                            List<Map<String, Object>> pointDataList = new ArrayList<>();
                            Map<String, Object> pointData = new HashMap<>();
                            if (mnAndType.get(mnIndexOut) != null) {
                                monitorpointtypecodes.add(Integer.parseInt(mnAndType.get(mnIndexOut).toString()));
                            }
                            pointData.put("monitorpointtypecode", mnAndType.get(mnIndexOut));
                            pointData.put("monitorpointid", mnAndMonitorPointId.get(mnIndexOut));
                            pointData.put("dgimn", mnIndexOut);
                            pointData.put("monitorpointname", mnAndMonitorPointName.get(mnIndexOut));
                            pointData.put("pollutantlist", mnAndPollutantDataList.get(mnIndexOut));
                            pointDataList.add(pointData);
                            pollutionData.put("pointdatalist", pointDataList);
                            dataList.add(pollutionData);
                        }
                    }
                }
                if (monitorpointtypecodes.size() > 0) {
                    //企业运维信息
                    paramMap.put("monitorpointtypecodes", monitorpointtypecodes);
                    paramMap.put("pollutionIds", pollutionIds);
                    paramMap.put("monitorpointIds", monitorpointIds);
                    List<Map<String, Object>> devOpsDataList = entDevOpsInfoService.getEntDevOpsDataByParamMap(paramMap);
                    if (devOpsDataList.size() > 0) {
                        Map<String, Object> keyAndUnit = new HashMap<>();
                        Map<String, Object> keyAndPhone = new HashMap<>();
                        Map<String, Object> keyAndUser = new HashMap<>();
                        String key;
                        for (Map<String, Object> devOpsData : devOpsDataList) {
                            key = devOpsData.get("pollutionid") + "#" + devOpsData.get("monitorpointid") + "#" + devOpsData.get("monitorpointtypecode");
                            keyAndUnit.put(key, devOpsData.get("DevOpsUnit"));
                            keyAndPhone.put(key, devOpsData.get("Telephone"));
                            keyAndUser.put(key, devOpsData.get("usernames"));
                        }
                        for (Map<String, Object> dataMap : dataList) {
                            if (exceptionType.contains(dataMap.get("remindcode"))) {
                                key = dataMap.get("pollutionid") + "#" + dataMap.get("monitorpointid") + "#" + dataMap.get("monitorpointtypecode");
                                Map<String, Object> ywdata = new HashMap<>();
                                ywdata.put("company", keyAndUnit.get(key));
                                ywdata.put("people", keyAndUser.get(key));
                                ywdata.put("phone", keyAndPhone.get(key));
                                dataMap.put("ywdata", ywdata);
                            }
                        }
                    } else {
                        for (Map<String, Object> dataMap : dataList) {
                            if (exceptionType.contains(dataMap.get("remindcode"))) {

                                Map<String, Object> ywdata = new HashMap<>();
                                ywdata.put("company", "");
                                ywdata.put("people", "");
                                ywdata.put("phone", "");
                                dataMap.put("ywdata", ywdata);
                            }
                        }
                    }
                    //运维任务信息
                    paramMap.clear();
                    paramMap.put("pollutionids", pointids);
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
                    paramMap.put("userid", userId);
                    taskDisposeManagementDataList = alarmTaskDisposeService.getTaskDisposeManagementDataByParam(paramMap);
                    if (taskDisposeManagementDataList.size() > 0) {
                        Map<String, Map<String, Object>> idandtaskinfo = new HashMap<>();
                        for (Map<String, Object> taskDisposeManagementData : taskDisposeManagementDataList) {
                            if (taskDisposeManagementData.get("FK_Pollutionid") != null) {
                                idandtaskinfo.put(taskDisposeManagementData.get("FK_Pollutionid") + "", taskDisposeManagementData);
                            }
                        }
                        List<Map<String, Object>> tempDataList = new ArrayList<>();
                        for (Map<String, Object> dataMap : dataList) {
                            if (exceptionType.contains(dataMap.get("remindcode"))) {
                                String taskstatus = "";
                                String id = dataMap.get("pointid").toString();
                                dataMap.put("taskid", "");
                                dataMap.put("tasktype", "");
                                dataMap.put("taskcreatetime", "");
                                dataMap.put("alarmstarttime", "");
                                dataMap.put("taskendtime", "");
                                dataMap.put("usernames", "");
                                dataMap.put("taskstatuscode", "");
                                dataMap.put("taskstatusname", "");
                                if (idandtaskinfo.get(id) != null) {
                                    String monitor_time = dataMap.get("monitortime") != null ? dataMap.get("monitortime").toString() : "";
                                    Map<String, Object> taskobj = idandtaskinfo.get(id);
                                    //判断报警时间是否在任务开始 结束时间范围内
                                    if (!"".equals(monitor_time) && taskobj.get("AlarmStartTime") != null && !"".equals(taskobj.get("AlarmStartTime").toString())
                                            && taskobj.get("TaskEndTime") != null && !"".equals(taskobj.get("TaskEndTime").toString())) {
                                        String start = taskobj.get("AlarmStartTime").toString();
                                        String end = taskobj.get("TaskEndTime").toString();
                                        boolean flag_one = false;
                                        boolean flag_two = false;
                                        try {

                                            if (monitor_time.equals(start)) {
                                                flag_one = true;
                                            } else {
                                                if (compare(start, monitor_time)) {
                                                    flag_one = true;
                                                } else {
                                                    flag_one = false;
                                                }
                                            }
                                            if (monitor_time.equals(end)) {
                                                flag_two = false;
                                            } else {
                                                if (compare(end, monitor_time)) {
                                                    flag_two = true;
                                                } else {
                                                    flag_two = false;
                                                }
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        if (flag_one == true && flag_two == false) {
                                            taskstatus = taskobj.get("TaskStatus") != null ? taskobj.get("TaskStatus").toString() : "";
                                            dataMap.put("taskid", taskobj.get("PK_TaskID"));
                                            dataMap.put("tasktype", taskobj.get("FK_TaskType"));
                                            dataMap.put("taskcreatetime", taskobj.get("TaskCreateTime"));
                                            dataMap.put("alarmstarttime", taskobj.get("AlarmStartTime"));
                                            dataMap.put("taskendtime", taskobj.get("TaskEndTime"));
                                            dataMap.put("usernames", taskobj.get("user_name"));
                                            dataMap.put("taskstatuscode", taskstatus);
                                            dataMap.put("taskstatusname", taskobj.get("statusname"));
                                        }
                                    }
                                }
                                if (taskstatuscodes != null && taskstatuscodes.size() > 0) {
                                    if (!"".equals(taskstatus) && taskstatuscodes.contains(Integer.parseInt(taskstatus))) {
                                        tempDataList.add(dataMap);
                                    }
                                }
                            }
                        }
                        //有任务状态参数
                        if (taskstatuscodes != null && taskstatuscodes.size() > 0) {
                            dataList = tempDataList;
                        }
                    }
                }
                if (dataList.size() > 0) {
                    Map<String, Object> countData = new HashMap<>();
                    int earlynum = 0;
                    int overnum = 0;
                    int exceptionnum = 0;
                    for (Map<String, Object> dataMap : dataList) {
                        if (exceptionType.contains(dataMap.get("remindcode"))) {
                            exceptionnum++;
                        } else if (dataMap.get("remindcode").toString().equals(CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode().toString())) {
                            overnum++;
                        } else {
                            earlynum++;
                        }
                    }
                    countData.put("earlynum", earlynum);
                    countData.put("overnum", overnum);
                    countData.put("exceptionnum", exceptionnum);
                    resultMap.put("countdata", countData);
                }
                dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("lasttime").toString()).reversed()).collect(Collectors.toList());
                resultMap.put("datalist", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2020/05/06 11:15
     * @Description: 统计首页报警数据（预警看板左侧）
     * @updateUser:xsm
     * @updateDate:2022/06/09 下午13:12
     * @updateDescription:新增排口累计报警时长
     * @updateUser:xsm
     * @updateDate:2022/07/06 上午11:17
     * @updateDescription:新增时间类型查询 hour/day 不传则查所有，hour 则只查小时报警数据  日只查日报警数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "countHomePageAllAlarmData", method = RequestMethod.POST)
    public Object countHomePageAllAlarmData(
            @RequestJson(value = "monitortime", required = false) String monitortime,
            @RequestJson(value = "monitorpointtypecodes", required = false) List<Integer> monitorpointtypecodes,
            @RequestJson(value = "ismerge", required = false) String ismerge,
            @RequestJson(value = "remindcodes", required = false) List<Integer> remindcodes,
            @RequestJson(value = "taskstatuscodes", required = false) List<String> taskstatuscodes,
            @RequestJson(value = "datetype", required = false) String datetype,
            @RequestJson(value = "searchname", required = false) String searchname) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            if (StringUtils.isBlank(monitortime)) {
                monitortime = DataFormatUtil.getDateYMD(new Date());
            }
            if (datetype == null) {
                datetype = "";
            }
            String startTime = monitortime + " 00:00:00";
            String endTime = monitortime + " 23:59:59";
            List<Map<String, Object>> env_dataList = new ArrayList<>();
            Map<String, Object> env_mnData = new HashMap<>();//环保数据集合
            Map<String, Object> idandtaskinfo = new HashMap<>();//存储（监测点ID_任务类型,任务信息）格式的数据
            List<Map<String, Object>> datalist = new ArrayList<>();
            List<Integer> tasklist = new ArrayList<>();
            List<String> taskids = new ArrayList<>();
            //获取环保点位信息
            env_mnData = getMnDataByParam(monitorpointtypecodes, searchname);
            tasklist.add(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());//报警任务
            tasklist.add(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());//运维任务
            tasklist.add(CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode());//突变任务
            //获取所有点位当日最新的报警任务数据
            if (tasklist.size() > 0) {
                param.put("tasktypelist", tasklist);
                param.put("todaytime", DataFormatUtil.getDateYMD(new Date()));
                param.put("monitorpointtypecodes", monitorpointtypecodes);
                List<Map<String, Object>> taskDisposeManagementDataList = alarmTaskDisposeService.getFidAndTypeByParam(param);

                String nowDay = DataFormatUtil.getDateYMD(new Date());

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("starttime", nowDay);
                paramMap.put("endtime", nowDay);
                paramMap.put("monitorpointtypes", monitorpointtypecodes);
                paramMap.put("tasktypes", tasklist);
                paramMap.put("category", 1);
                //已完成  状态
                paramMap.put("completions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode(),
                        CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
                paramMap.put("uncompletions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode(),
                        CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode(), CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode()));
                Map<String, Object> taskData = alarmTaskDisposeService.countAlarmTaskCompletionStatusByParamMap(paramMap);
                resultMap.put("taskData", taskData);


                if (taskDisposeManagementDataList != null && taskDisposeManagementDataList.size() > 0) {
                    for (Map<String, Object> map : taskDisposeManagementDataList) {
                        if (map.get("FK_Pollutionid") != null && map.get("FK_TaskType") != null) {
                            //存储格式（监测点ID_任务类型,任务信息）
                            idandtaskinfo.put(map.get("FK_Pollutionid") + "_" + map.get("FK_TaskType"), map);
                            taskids.add(map.get("PK_TaskID").toString());
                        }
                    }
                }
            }
            //处理环保首页报警数据
            env_dataList = setEnvAlarmDataForHomePage(env_mnData, startTime, endTime, remindcodes, taskstatuscodes, idandtaskinfo, datetype);
            datalist.addAll(env_dataList);
            //统计各类型报警点位个数
            if (datalist.size() > 0) {
                Map<String, Object> countData = new HashMap<>();
                Map<Integer, Map<String, Integer>> typeAndRemindAndNum = new HashMap<>();
                Map<String, Integer> remindAndNum;
                int earlynum = 0;
                int overnum = 0;
                int exceptionnum = 0;
                int type;
                String remindcode;
                int totalTask = 0;
                int comTask = 0;
                //遍历报警数据
                for (Map<String, Object> dataMap : datalist) {
                    if (dataMap.get("monitorpointtypecode") != null && dataMap.get("remindcode") != null) {
                        type = Integer.parseInt(dataMap.get("monitorpointtypecode").toString());
                        if (ismerge != null && Boolean.parseBoolean(ismerge) && SmokeEnum.getCode() == type) {//合并废气、烟气
                            type = WasteGasEnum.getCode();
                        }
                        //根据报警类型统计 个类型报警点位个数
                        remindcode = dataMap.get("remindcode").toString();
                        if (CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode().toString().equals(remindcode)) {
                            exceptionnum++;
                        } else if (CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode().toString().equals(remindcode)) {
                            overnum++;
                        } else {
                            earlynum++;
                        }
                        if (typeAndRemindAndNum.containsKey(type)) {
                            remindAndNum = typeAndRemindAndNum.get(type);
                        } else {
                            remindAndNum = new HashMap<>();
                        }
                        //浓度突变也算预警
                        if (remindcode.equals(ConcentrationChangeEnum.getCode().toString())) {
                            remindcode = EarlyAlarmEnum.getCode().toString();
                        }
                        remindAndNum.put(remindcode, remindAndNum.get(remindcode) != null ? remindAndNum.get(remindcode) + 1 : 1);
                        typeAndRemindAndNum.put(type, remindAndNum);
                        //统计报警任务条数 和已完成条数
                        if (dataMap.get("taskstatuscode") != null && !"".equals(dataMap.get("taskstatuscode"))) {
                            totalTask++;
                            if (CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode() ==
                                    Integer.parseInt(dataMap.get("taskstatuscode").toString())) {
                                comTask++;
                            }
                        }

                    }

                }
                countData.put("earlynum", earlynum);//预警点位个数
                countData.put("overnum", overnum);//超标点位个数
                countData.put("exceptionnum", exceptionnum);//异常点位个数
                resultMap.put("countdata", countData);
                List<Map<String, Object>> typeCountData = new ArrayList<>();
                boolean flag;
                //监测类型报警点位个数
                for (Integer typeIndex : monitorpointtypecodes) {
                    if (ismerge != null && Boolean.parseBoolean(ismerge) && SmokeEnum.getCode() == typeIndex) {//合并废气、烟气
                        flag = false;
                    } else {
                        flag = true;
                    }
                    if (flag) {
                        Map<String, Object> typeMap = new HashMap<>();
                        typeMap.put("monitorpointtypecode", typeIndex);
                        typeMap.put("countdata", typeAndRemindAndNum.get(typeIndex));
                        typeMap.put("monitorpointtypename", CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(typeIndex).replaceAll("监测点", ""));
                        typeCountData.add(typeMap);
                    }
                }
                resultMap.put("typeCountData", typeCountData);
                //任务数据

            }
            //按报警时间 降序 排序
            if (datalist.size() > 0) {
                datalist = datalist.stream().sorted(Comparator.comparing(m -> ((Map) m).get("lasttime").toString()).reversed()).collect(Collectors.toList());
            }
            resultMap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * 首页环保报警数据查询处理
     */
    private List<Map<String, Object>> setEnvAlarmDataForHomePage(Map<String, Object> env_mnData, String startTime, String endTime, List<Integer> remindcodes, List<String> taskstatuscodes, Map<String, Object> idandtaskinfo, String datetype) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (env_mnData.size() > 0) {
            Map<String, Object> param = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", env_mnData.get("mns"));
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("datetype", datetype);
            //获取污染物信息
            List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> codeAndUnit = new HashMap<>();
            if (pollutantData.size() > 0) {
                String key;
                for (Map<String, Object> pollutant : pollutantData) {
                    if (pollutant.get("code") != null && !"".equals(pollutant.get("code").toString())
                            && pollutant.get("pollutanttype") != null && !"".equals(pollutant.get("pollutanttype").toString())) {
                        key = pollutant.get("code") + "#" + pollutant.get("pollutanttype");
                        if (pollutant.get("name") != null) {
                            codeAndName.put(key, pollutant.get("name"));
                        }
                        if (pollutant.get("unit") != null) {
                            codeAndUnit.put(key, pollutant.get("unit"));
                        }
                    }
                }
            }
            //点位相关信息
            Map<String, Map<String, Object>> mnAndPollutantDataList;
            Map<String, Object> mnAndType = (Map<String, Object>) env_mnData.get("mnAndType");
            Map<String, Object> codeAndTypeName = (Map<String, Object>) env_mnData.get("codeAndTypeName");
            Map<String, Object> mnAndPollutionId = (Map<String, Object>) env_mnData.get("mnAndPollutionId");
            Map<String, Object> mnAndPollutionName = (Map<String, Object>) env_mnData.get("mnAndPollutionName");
            Map<String, Object> mnAndShorterName = (Map<String, Object>) env_mnData.get("mnAndShorterName");
            Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) env_mnData.get("mnAndMonitorPointId");
            Map<String, Object> mnAndMonitorPointName = (Map<String, Object>) env_mnData.get("mnAndMonitorPointName");
            Map<String, Map<String, Object>> mnAndmapdata = (Map<String, Map<String, Object>>) env_mnData.get("mnAndmapdata");
            if (remindcodes == null || remindcodes.size() == 0) {//当报警类型为空 默认查所有
                remindcodes = new ArrayList<>();
                remindcodes.add(ExceptionAlarmEnum.getCode());//异常
                remindcodes.add(ConcentrationChangeEnum.getCode());//浓度突变
                remindcodes.add(EarlyAlarmEnum.getCode());//预警
                remindcodes.add(OverAlarmEnum.getCode());//超标
            }
            String tasktypestr = "";
            List<String> pollutionids = new ArrayList<>();
            Set<String> monitorpointIds = new HashSet<>();
            Set<String> monitorpointtypecodes = new HashSet<>();
            String type = "";
            //遍历报警类型
            for (Integer code : remindcodes) {
                Map<String, Date> mnAndTime = new HashMap<>();
                //根据类型查询该类型的报警数据信息
                mnAndPollutantDataList = getAllAlarmMonitorDataByParam(paramMap, code, mnAndTime, env_mnData, codeAndName, codeAndUnit, mnAndType);
                if (mnAndPollutantDataList != null) {
                    Map<String, Object> onemap = new HashMap<>();
                    //遍历报警数据
                    for (String onemn : mnAndPollutantDataList.keySet()) {
                        //整理数据
                        onemap = mnAndPollutantDataList.get(onemn);
                        onemap.put("pollutionid", mnAndPollutionId.get(onemn) != null ? mnAndPollutionId.get(onemn) : "");
                        pollutionids.add(mnAndPollutionId.get(onemn) != null ? mnAndPollutionId.get(onemn).toString() : "");
                        onemap.put("monitorpointid", mnAndMonitorPointId.get(onemn));
                        monitorpointIds.add(mnAndMonitorPointId.get(onemn) != null ? mnAndMonitorPointId.get(onemn).toString() : "");
                        onemap.putIfAbsent("pollutionname", mnAndPollutionName.get(onemn) != null ? mnAndPollutionName.get(onemn) : "");
                        onemap.putIfAbsent("shortername", mnAndShorterName.get(onemn) != null ? mnAndShorterName.get(onemn) : "");
                        onemap.put("mapdata", mnAndmapdata.get(onemn));
                        onemap.put("monitorpointtypecode", mnAndType.get(onemn));
                        if (mnAndType.get(onemn) != null) {
                            type = mnAndType.get(onemn).toString();
                            onemap.put("monitorpointtypename", codeAndTypeName.get(type));
                        } else {
                            onemap.put("monitorpointtypename", "");
                        }
                        monitorpointtypecodes.add(mnAndType.get(onemn) != null ? mnAndType.get(onemn).toString() : "");
                        onemap.put("dgimn", onemn);
                        onemap.put("category", "1");
                        onemap.put("monitorpointname", mnAndMonitorPointName.get(onemn));
                        //找出关联任务
                        if (code == OverAlarmEnum.getCode()) {
                            tasktypestr = CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode() + "";
                        } else if (code == ExceptionAlarmEnum.getCode()) {
                            tasktypestr = CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode() + "";
                        } else if (code == ConcentrationChangeEnum.getCode()) {
                            tasktypestr = CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode() + "";
                        }
                        onemap.put("taskid", "");
                        onemap.put("tasktype", "");
                        onemap.put("taskcreatetime", "");
                        onemap.put("alarmstarttime", "");
                        onemap.put("taskendtime", "");
                        onemap.put("usernames", "");
                        onemap.put("taskstatuscode", "");
                        onemap.put("taskstatusname", "");
                        if (idandtaskinfo.get(onemap.get("monitorpointid").toString() + "_" + tasktypestr) != null) {
                            Map<String, Object> taskobj = (Map<String, Object>) idandtaskinfo.get(onemap.get("monitorpointid").toString() + "_" + tasktypestr);
                            onemap.put("taskid", taskobj.get("PK_TaskID"));
                            onemap.put("tasktype", taskobj.get("FK_TaskType"));
                            onemap.put("taskcreatetime", taskobj.get("TaskCreateTime"));
                            onemap.put("alarmstarttime", taskobj.get("AlarmStartTime"));
                            onemap.put("taskendtime", taskobj.get("TaskEndTime"));
                            onemap.put("usernames", taskobj.get("user_name"));
                            onemap.put("recoverystatus", taskobj.get("RecoveryStatus"));
                            onemap.put("taskstatuscode", taskobj.get("TaskStatus"));
                            onemap.put("taskstatusname", taskobj.get("statusname"));
                        }
                        //pollutionIds.add(mnAndMonitorPointId.get(mnIndexOut) + "");
                        if (taskstatuscodes != null && taskstatuscodes.size() > 0) {
                            if (taskstatuscodes.contains(onemap.get("taskstatuscode") != null ? Integer.parseInt(onemap.get("taskstatuscode").toString()) : "")) {
                                dataList.add(onemap);
                            }
                        } else {
                            dataList.add(onemap);
                        }
                    }
                }
            }
            //获取巡查人员及运维人员
            if (remindcodes.contains(OverAlarmEnum.getCode()) || remindcodes.contains(ConcentrationChangeEnum.getCode())) {
                param.put("pollutionids", pollutionids);
                List<Map<String, Object>> pollutionDataList = pollutionService.getPollutionNameAndPkid(paramMap);
                if (pollutionDataList.size() > 0) {
                    Map<String, Object> pollutionIdAndEnvironmentalManager = new HashMap<>();
                    Map<String, Object> pollutionIdAndLinkManPhone = new HashMap<>();
                    for (Map<String, Object> pollutionData : pollutionDataList) {
                        pollutionIdAndEnvironmentalManager.put(pollutionData.get("pollutionid").toString(), pollutionData.get("EnvironmentalManager"));
                        pollutionIdAndLinkManPhone.put(pollutionData.get("pollutionid").toString(), pollutionData.get("LinkManPhone"));
                    }
                    for (Map<String, Object> dataMap : dataList) {
                        Map<String, Object> hbdata = new HashMap<>();
                        hbdata.put("leader", pollutionIdAndEnvironmentalManager.get(dataMap.get("pollutionid")));
                        hbdata.put("phone", pollutionIdAndLinkManPhone.get(dataMap.get("pollutionid")));
                        dataMap.put("hbdata", hbdata);
                    }
                }
            } else if (remindcodes.contains(ExceptionAlarmEnum.getCode())) {
                //企业运维信息
                paramMap.put("monitorpointtypecodes", monitorpointtypecodes);
                paramMap.put("pollutionIds", pollutionids);
                paramMap.put("monitorpointIds", pollutionids);
                List<Map<String, Object>> devOpsDataList = entDevOpsInfoService.getEntDevOpsDataByParamMap(paramMap);
                if (devOpsDataList.size() > 0) {
                    Map<String, Object> keyAndUnit = new HashMap<>();
                    Map<String, Object> keyAndPhone = new HashMap<>();
                    Map<String, Object> keyAndUser = new HashMap<>();
                    String key;
                    for (Map<String, Object> devOpsData : devOpsDataList) {
                        key = devOpsData.get("pollutionid") + "#" + devOpsData.get("monitorpointid") + "#" + devOpsData.get("monitorpointtypecode");
                        keyAndUnit.put(key, devOpsData.get("DevOpsUnit"));
                        keyAndPhone.put(key, devOpsData.get("Telephone"));
                        keyAndUser.put(key, devOpsData.get("usernames"));
                    }
                    for (Map<String, Object> dataMap : dataList) {
                        key = dataMap.get("pollutionid") + "#" + dataMap.get("monitorpointid") + "#" + dataMap.get("monitorpointtypecode");
                        Map<String, Object> ywdata = new HashMap<>();
                        ywdata.put("company", keyAndUnit.get(key));
                        ywdata.put("people", keyAndUser.get(key));
                        ywdata.put("phone", keyAndPhone.get(key));
                        dataMap.put("ywdata", ywdata);
                    }
                } else {
                    for (Map<String, Object> dataMap : dataList) {
                        Map<String, Object> ywdata = new HashMap<>();
                        ywdata.put("company", "");
                        ywdata.put("people", "");
                        ywdata.put("phone", "");
                        dataMap.put("ywdata", ywdata);
                    }
                }
            }
        }
        return dataList;
    }

    /**
     * 根据报警类型 查询该类型的报警数据
     */
    private Map<String, Map<String, Object>> getAllAlarmMonitorDataByParam(Map<String, Object> paramMap, Integer remindCode, Map<String, Date> mnAndTime, Map<String, Object> mnData, Map<String, Object> codeAndName, Map<String, Object> codeAndUnit, Map<String, Object> mnAndType) {
        Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) mnData.get("mnAndMonitorPointId");
        Map<String, Map<String, Object>> mnAndPollutantDataList = new HashMap<>();
        List<Map<String, Object>> pollutantDataList;
        Map<String, Object> paramMapTemp = new HashMap<>();
        List<String> pollutantcodes = new ArrayList<>();
        List<Document> documents;
        List<Document> pollutantList;
        String mnCommon;
        String pollutantcode;
        String monitorvalue;
        Date monitorTime;
        Double ChangeMultiple;
        Integer typeKey;
        String key;
        Map<String, Object> mn_alarmstr;
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindCode)) {
            case ConcentrationChangeEnum:
                //1，浓度突变
                paramMap.put("collection", "SuddenRiseData");
                paramMap.put("monitortimekey", "ChangeTime");
                if (paramMap.get("datetype") != null && !"".equals(paramMap.get("datetype").toString())) {
                    //新增 小时 日查询
                    documents = onlineCountAlarmService.getHourOrDayChangeAlarmDataByParamMap(paramMap);
                } else {
                    documents = onlineService.getChangeAlarmDataByParamMap(paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        Map<String, Object> objdata = new HashMap<>();
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        Map<String, String> code_time = new HashMap<>();
                        List<String> pollutantstrs = new ArrayList<>();
                        for (Document pollutant : pollutantList) {
                            pollutantcode = pollutant.getString("PollutantCode");
                            String hm = DataFormatUtil.getDateHM(pollutant.getDate("ChangeTime"));
                            if (paramMap.get("datetype") != null && !"".equals(paramMap.get("datetype").toString()) && hm.length() > 2) {
                                hm = Integer.valueOf(hm.substring(0, 2)) + "时";
                            }
                            if (code_time.get(pollutantcode) != null) {
                                code_time.put(pollutantcode, code_time.get(pollutantcode) + "、" + hm);
                            } else {
                                code_time.put(pollutantcode, hm);
                            }
                            if (pollutant.getDate("ChangeTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcodes.add(pollutantcode);
                                ChangeMultiple = pollutant.getDouble("ChangeMultiple");
                                monitorvalue = pollutant.getString("MonitorValue");
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantMap.put("changemultiple", DataFormatUtil.formatDoubleSaveOne(ChangeMultiple));
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        if (code_time != null && code_time.size() > 0) {
                            for (String pocode : code_time.keySet()) {
                                String str = codeAndName.get(pocode + "#" + typeKey) + "浓度突变：" + code_time.get(pocode) + "；";
                                pollutantstrs.add(str);
                            }
                        }
                        objdata.put("alarmstr", pollutantstrs);
                        objdata.put("remindcode", ConcentrationChangeEnum.getCode());
                        objdata.put("remindname", ConcentrationChangeEnum.getName());
                        objdata.put("lasttime", DataFormatUtil.getDateHMS(monitorTime));
                        objdata.put("pollutantDataList", pollutantDataList);
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                }
                break;
            case EarlyAlarmEnum:
                //2，超阈值
                paramMap.put("collection", "EarlyWarnData");
                paramMap.put("monitortimekey", "EarlyWarnTime");
                if (paramMap.get("datetype") != null && !"".equals(paramMap.get("datetype").toString())) {
                    //新增小时、日查询
                    documents = onlineCountAlarmService.getHourOrDayLastAlarmDataByParamMap(paramMap);
                    mn_alarmstr = onlineCountAlarmService.setHourOrDayIntegrationAlarmData(remindCode, paramMap);
                } else {
                    documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                    mn_alarmstr = onlineService.setIntegrationAlarmData(remindCode, paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    pollutantcodes.clear();
                    Map<String, Object> objdata;
                    for (Document document : documents) {
                        objdata = new HashMap<>();
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                pollutantcodes.add(pollutantcode);
                                monitorvalue = pollutant.getString("MonitorValue");
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantMap.put("datatype", pollutant.get("DataType"));
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        List<String> strs = new ArrayList<>();
                        if (mn_alarmstr.get(mnCommon) != null) {
                            Map<String, Object> strmap = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                            for (String pocode : strmap.keySet()) {
                                strs.add(codeAndName.get(pocode + "#" + typeKey) + "" + strmap.get(pocode));
                            }
                        }
                        //点位报警总时长
                        if (mn_alarmstr.get(mnCommon + "_totaltime") != null) {
                            objdata.put("alarmtotaltimes", mn_alarmstr.get(mnCommon + "_totaltime"));
                        } else {
                            objdata.put("alarmtotaltimes", "");
                        }
                        objdata.put("alarmstr", strs);
                        objdata.put("remindcode", EarlyAlarmEnum.getCode());
                        objdata.put("remindname", EarlyAlarmEnum.getName());
                        objdata.put("lasttime", DataFormatUtil.getDateHMS(monitorTime));
                        objdata.put("pollutantDataList", pollutantDataList);
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                    if (mnAndPollutantDataList.size() > 0) {
                        List<String> outputids = new ArrayList<>();
                        for (String mnIndex : mnAndPollutantDataList.keySet()) {
                            if (mnAndMonitorPointId.containsKey(mnIndex)) {
                                outputids.add(mnAndMonitorPointId.get(mnIndex).toString());
                            }
                        }
                        paramMapTemp.clear();
                        paramMapTemp.put("outputids", outputids);
                        paramMapTemp.put("pollutantcodes", pollutantcodes);
                        List<Map<String, Object>> earlyDataList = pollutantService.getEarlyValueByParams(paramMapTemp);
                        if (earlyDataList.size() > 0) {
                            Map<String, Object> codeAndValue = new HashMap<>();
                            for (Map<String, Object> earlyData : earlyDataList) {
                                codeAndValue.put(earlyData.get("outputid") + "#" + earlyData.get("pollutantcode"), earlyData.get("concenalarmmaxvalue"));
                            }
                            String codeKey;
                            for (String mnIndex : mnAndPollutantDataList.keySet()) {
                                objdata = mnAndPollutantDataList.get(mnIndex);
                                pollutantDataList = (List<Map<String, Object>>) objdata.get("pollutantDataList");
                                for (Map<String, Object> pollutantData : pollutantDataList) {
                                    codeKey = mnAndMonitorPointId.get(mnIndex) + "#" + pollutantData.get("pollutantcode");
                                    pollutantData.put("earlyvalue", codeAndValue.get(codeKey));
                                }
                            }
                        }
                    }
                }
                break;
            case OverAlarmEnum:
                //3，数据超限
                paramMap.put("collection", "OverData");
                paramMap.put("monitortimekey", "OverTime");
                if (paramMap.get("datetype") != null && !"".equals(paramMap.get("datetype").toString())) {
                    //新增小时、日查询
                    documents = onlineCountAlarmService.getHourOrDayLastAlarmDataByParamMap(paramMap);
                    mn_alarmstr = onlineCountAlarmService.setHourOrDayIntegrationAlarmData(remindCode, paramMap);
                } else {
                    documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                    mn_alarmstr = onlineService.setIntegrationAlarmData(remindCode, paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    pollutantcodes.clear();
                    Map<String, Object> objdata;
                    for (Document document : documents) {
                        objdata = new HashMap<>();
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        //获取标准值
                        paramMapTemp.clear();
                        paramMapTemp.put("monitorpointtype", mnAndType.get(mnCommon));
                        paramMapTemp.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                        List<Map<String, Object>> standValueslist = pollutantService.getEarlyAndStandardValueByParams(paramMapTemp);
                        Map<String, Object> hourmap = new HashMap<>();
                        //通过mn号分组数据
                        Map<String, List<Document>> mapDocuments = pollutantList.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                        for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                            String onepollutancode = entry.getKey();
                            List<Document> onelist = entry.getValue();
                            String houralarmstr = "";//小时超标时段
                            String newhour = "";
                            int newhournum = 0;
                            String str1 = "";
                            for (Document pollutant : onelist) {
                                String datatype = pollutant.getString("DataType");
                                if ("HourData".equals(datatype)) {
                                    int hour = DataFormatUtil.getDateHourNum(pollutant.getDate("MonitorTime"));
                                    str1 = DataFormatUtil.getDateHM(pollutant.getDate("MonitorTime"));
                                    if ("".equals(newhour)) {
                                        newhour = hour + "";
                                        newhournum += 1;
                                        houralarmstr = str1 + "、";
                                    } else {
                                        if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                                            //连续
                                            newhour = hour + "";
                                            newhournum += 1;
                                        } else {
                                            houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                                            if (newhournum > 1) {
                                                if (Integer.parseInt(newhour) > 9) {
                                                    houralarmstr = houralarmstr + "-" + newhour + ":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                                } else {
                                                    houralarmstr = houralarmstr + "-" + "0" + newhour + ":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                                }
                                            } else {
                                                houralarmstr = houralarmstr + "、" + str1 + "、";
                                            }
                                            newhour = hour + "";
                                            newhournum = 1;
                                        }
                                    }
                                }
                            }
                            if (!"".equals(houralarmstr)) {
                                houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                            }
                            if (newhournum > 1) {
                                if (Integer.parseInt(newhour) > 9) {
                                    houralarmstr = houralarmstr + "-" + newhour + ":00" + "【" + newhournum + "小时】";
                                } else {
                                    houralarmstr = houralarmstr + "-" + "0" + newhour + ":00" + "【" + newhournum + "小时】";
                                }
                            }
                            hourmap.put(onepollutancode, houralarmstr);
                        }
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                pollutantcodes.add(pollutantcode);
                                monitorvalue = pollutant.getString("MonitorValue");
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantMap.put("datatype", pollutant.get("DataType"));
                                pollutantMap.put("standvalue", "-");
                                String levelcode = pollutant.getInteger("AlarmLevel") + "";
                                pollutantMap.put("levelcode", levelcode);
                                if (standValueslist != null && standValueslist.size() > 0) {
                                    if ("-1".equals(levelcode)) {//超标
                                        for (Map<String, Object> standValue : standValueslist) {
                                            if (pollutantcode.equals(standValue.get("FK_PollutantCode").toString())) {
                                                if (standValue.get("AlarmType") != null) {
                                                    if ("1".equals(standValue.get("AlarmType").toString())) {//上限报警
                                                        pollutantMap.put("standvalue", standValue.get("StandardMaxValue") != null ? DataFormatUtil.subZeroAndDot(standValue.get("StandardMaxValue").toString()) : "");
                                                    } else if ("1".equals(standValue.get("AlarmType").toString())) {
                                                        pollutantMap.put("standvalue", standValue.get("StandardMinValue") != null ? DataFormatUtil.subZeroAndDot(standValue.get("StandardMinValue").toString()) : "");
                                                    } else if ("3".equals(standValue.get("AlarmType").toString())) {
                                                        if (standValue.get("StandardMaxValue") != null && standValue.get("StandardMinValue") != null) {
                                                            pollutantMap.put("standvalue", DataFormatUtil.subZeroAndDot(standValue.get("StandardMinValue").toString())
                                                                    + "-" +
                                                                    DataFormatUtil.subZeroAndDot(standValue.get("StandardMaxValue").toString()));
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    } else {
                                        for (Map<String, Object> standValue : standValueslist) {
                                            if (standValue.get("FK_PollutantCode") != null && standValue.get("FK_AlarmLevelCode") != null
                                                    && pollutantcode.equals(standValue.get("FK_PollutantCode").toString()) &&
                                                    levelcode.equals(standValue.get("FK_AlarmLevelCode").toString())) {
                                                pollutantMap.put("standvalue", standValue.get("ConcenAlarmMaxValue"));
                                                break;
                                            }
                                        }
                                    }
                                }
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        List<String> strs = new ArrayList<>();
                        if (mn_alarmstr.get(mnCommon) != null) {
                            Map<String, Object> strmap = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                            for (String pocode : strmap.keySet()) {
                                strs.add(codeAndName.get(pocode + "#" + typeKey) + "" + strmap.get(pocode));
                            }
                        } else {
                            for (String pocode : hourmap.keySet()) {
                                strs.add(codeAndName.get(pocode + "#" + typeKey) + "超限" + hourmap.get(pocode));
                            }
                        }
                        //点位报警总时长
                        if (mn_alarmstr.get(mnCommon + "_totaltime") != null) {
                            objdata.put("alarmtotaltimes", mn_alarmstr.get(mnCommon + "_totaltime"));
                        } else {
                            objdata.put("alarmtotaltimes", "");
                        }
                        objdata.put("alarmstr", strs);
                        objdata.put("remindcode", OverAlarmEnum.getCode());
                        objdata.put("remindname", OverAlarmEnum.getName());
                        objdata.put("lasttime", DataFormatUtil.getDateHMS(monitorTime));
                        objdata.put("pollutantDataList", pollutantDataList);
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                }
                break;
            case ExceptionAlarmEnum:
                //4，数据异常
                String exceptionType;
                paramMap.put("collection", "ExceptionData");
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("exceptiontype", "-1");
                if (paramMap.get("datetype") != null && !"".equals(paramMap.get("datetype").toString())) {
                    //新增小时、日查询
                    documents = onlineCountAlarmService.getHourOrDayLastAlarmDataByParamMap(paramMap);
                    mn_alarmstr = onlineCountAlarmService.setHourOrDayIntegrationAlarmData(remindCode, paramMap);
                } else {
                    documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                    mn_alarmstr = onlineService.setIntegrationAlarmData(remindCode, paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    Map<String, Object> objdata;
                    for (Document document : documents) {
                        objdata = new HashMap<>();
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                monitorvalue = pollutant.getString("MonitorValue");
                                exceptionType = pollutant.getString("ExceptionType");
                                pollutantMap.put("exceptioneype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(exceptionType));
                                pollutantMap.put("exceptioneypecode", exceptionType);
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantMap.put("datatype", pollutant.get("DataType"));
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        List<String> strs = new ArrayList<>();
                        if (mn_alarmstr.get(mnCommon) != null) {
                            Map<String, Object> strmap = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                            for (String pocode : strmap.keySet()) {
                                strs.add(codeAndName.get(pocode + "#" + typeKey) + "" + strmap.get(pocode));
                            }
                        }
                        //点位报警总时长
                        if (mn_alarmstr.get(mnCommon + "_totaltime") != null) {
                            objdata.put("alarmtotaltimes", mn_alarmstr.get(mnCommon + "_totaltime"));
                        } else {
                            objdata.put("alarmtotaltimes", "");
                        }
                        objdata.put("alarmstr", strs);
                        objdata.put("remindcode", ExceptionAlarmEnum.getCode());
                        objdata.put("remindname", ExceptionAlarmEnum.getName());
                        objdata.put("lasttime", DataFormatUtil.getDateHMS(monitorTime));
                        objdata.put("pollutantDataList", pollutantDataList);
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                }
                break;
        }
        return mnAndPollutantDataList;
    }

    private Map<String, List<Map<String, Object>>> getMonitorDataByParam(Map<String, Object> paramMap, Integer remindCode, Map<String, Date> mnAndTime, Map<String, Object> mnData, Map<String, Object> codeAndName, Map<String, Object> codeAndUnit, Map<String, Object> mnAndType) {
        Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) mnData.get("mnAndMonitorPointId");
        Map<String, List<Map<String, Object>>> mnAndPollutantDataList = new HashMap<>();
        List<Map<String, Object>> pollutantDataList;
        Map<String, Object> paramMapTemp = new HashMap<>();
        List<String> pollutantcodes = new ArrayList<>();
        List<Document> documents;
        List<Document> pollutantList;
        String mnCommon;
        String pollutantcode;
        String monitorvalue;
        Date monitorTime;
        Double ChangeMultiple;
        Integer typeKey;
        String key;

        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindCode)) {
            case ConcentrationChangeEnum:
                //1，浓度突变
                paramMap.put("collection", "MinuteData");
                paramMap.put("monitortimekey", "MonitorTime");
                documents = onlineService.getMinuteLastChangeDataByParamMap(paramMap);
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("MinuteDataList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                ChangeMultiple = pollutant.getDouble("ChangeMultiple");
                                pollutantcode = pollutant.getString("PollutantCode");
                                key = pollutantcode + "#" + typeKey;
                                monitorvalue = pollutant.getString("MonitorValue");
                                pollutantMap.put("pollutantcode", pollutantcode);
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantMap.put("changemultiple", DataFormatUtil.formatDoubleSaveOne(ChangeMultiple));
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        mnAndPollutantDataList.put(mnCommon, pollutantDataList);
                    }
                }
                break;
            case EarlyAlarmEnum:
                //2，超阈值
                paramMap.put("collection", "EarlyWarnData");
                paramMap.put("monitortimekey", "EarlyWarnTime");
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (documents != null && documents.size() > 0) {
                    pollutantcodes.clear();
                    for (Document document : documents) {
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                pollutantcodes.add(pollutantcode);
                                monitorvalue = pollutant.getString("MonitorValue");
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantDataList.add(pollutantMap);
                            }

                        }
                        mnAndPollutantDataList.put(mnCommon, pollutantDataList);
                    }
                    if (mnAndPollutantDataList.size() > 0) {
                        List<String> outputids = new ArrayList<>();
                        for (String mnIndex : mnAndPollutantDataList.keySet()) {
                            if (mnAndMonitorPointId.containsKey(mnIndex)) {
                                outputids.add(mnAndMonitorPointId.get(mnIndex).toString());
                            }
                        }
                        paramMapTemp.clear();
                        paramMapTemp.put("outputids", outputids);
                        paramMapTemp.put("pollutantcodes", pollutantcodes);
                        List<Map<String, Object>> earlyDataList = pollutantService.getEarlyValueByParams(paramMapTemp);
                        if (earlyDataList.size() > 0) {
                            Map<String, Object> codeAndValue = new HashMap<>();
                            for (Map<String, Object> earlyData : earlyDataList) {
                                codeAndValue.put(earlyData.get("outputid") + "#" + earlyData.get("pollutantcode"), earlyData.get("concenalarmmaxvalue"));
                            }
                            String codeKey;
                            for (String mnIndex : mnAndPollutantDataList.keySet()) {
                                pollutantDataList = mnAndPollutantDataList.get(mnIndex);
                                for (Map<String, Object> pollutantData : pollutantDataList) {
                                    codeKey = mnAndMonitorPointId.get(mnIndex) + "#" + pollutantData.get("pollutantcode");
                                    pollutantData.put("earlyvalue", codeAndValue.get(codeKey));
                                }
                            }
                        }
                    }
                }
                break;
            case OverAlarmEnum:
                //3，数据超限
                paramMap.put("collection", "OverData");
                paramMap.put("monitortimekey", "OverTime");
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (documents != null && documents.size() > 0) {
                    pollutantcodes.clear();
                    for (Document document : documents) {
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                pollutantcodes.add(pollutantcode);
                                monitorvalue = pollutant.getString("MonitorValue");
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        paramMapTemp.clear();
                        paramMapTemp.put("monitorpointtype", mnAndType.get(mnCommon));
                        paramMapTemp.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                        paramMapTemp.put("pollutants", pollutantcodes);
                        //获取标准值
                        List<Map<String, Object>> standValues = pollutantService.getEarlyAndStandardValueByParams(paramMapTemp);
                        if (standValues.size() > 0) {
                            Map<String, Object> codeAndValue = new HashMap<>();
                            for (Map<String, Object> standValue : standValues) {
                                if (standValue.get("StandardMaxValue") != null && standValue.get("StandardMinValue") != null) {
                                    codeAndValue.put(standValue.get("FK_PollutantCode").toString(),
                                            DataFormatUtil.subZeroAndDot(standValue.get("StandardMinValue").toString())
                                                    + "-" +
                                                    DataFormatUtil.subZeroAndDot(standValue.get("StandardMaxValue").toString()));
                                } else if (standValue.get("StandardMaxValue") != null) {
                                    codeAndValue.put(standValue.get("FK_PollutantCode").toString(),
                                            DataFormatUtil.subZeroAndDot(standValue.get("StandardMaxValue").toString()));
                                } else {
                                    codeAndValue.put(standValue.get("FK_PollutantCode").toString(), "-");
                                }
                            }
                            for (Map<String, Object> pollutantData : pollutantDataList) {
                                pollutantData.put("standvalue", codeAndValue.get(pollutantData.get("pollutantcode")));
                            }
                        }
                        mnAndPollutantDataList.put(mnCommon, pollutantDataList);
                    }
                }

                break;
            case ExceptionAlarmEnum:
                //4，数据异常

                String exceptionType;

                paramMap.put("collection", "ExceptionData");
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("exceptiontype", "-1");
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                monitorvalue = pollutant.getString("MonitorValue");
                                exceptionType = pollutant.getString("ExceptionType");
                                pollutantMap.put("exceptioneype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(exceptionType));
                                pollutantMap.put("exceptioneypecode", exceptionType);
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        mnAndPollutantDataList.put(mnCommon, pollutantDataList);
                    }
                }
                break;
            case WaterNoFlowEnum:
                //5，无流量异常
                paramMap.put("collection", "ExceptionData");
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("exceptiontype", CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum.getCode());
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        pollutantDataList = new ArrayList<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        mnAndTime.put(mnCommon, monitorTime);
                        pollutantList = document.get("pollutantList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutant.getDate("MonitorTime").equals(monitorTime)) {
                                Map<String, Object> pollutantMap = new HashMap<>();
                                pollutantcode = pollutant.getString("PollutantCode");
                                monitorvalue = pollutant.getString("MonitorValue");
                                exceptionType = pollutant.getString("ExceptionType");
                                pollutantMap.put("exceptioneype", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(exceptionType));
                                pollutantMap.put("exceptioneypecode", exceptionType);
                                pollutantMap.put("pollutantcode", pollutantcode);
                                key = pollutantcode + "#" + typeKey;
                                pollutantMap.put("pollutantname", codeAndName.get(key));
                                pollutantMap.put("pollutantunit", codeAndUnit.get(key) != null ? codeAndUnit.get(key) : "");
                                pollutantMap.put("monitorvalue", monitorvalue);
                                pollutantDataList.add(pollutantMap);
                            }
                        }
                        mnAndPollutantDataList.put(mnCommon, pollutantDataList);
                    }
                }
                break;
        }
        return mnAndPollutantDataList;

    }

    private Map<String, Object> getMnDataByParam(List<Integer> monitorpointtypecodes, String searchname) {
        List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
        List<Map<String, Object>> alltypes = deviceStatusService.getAllMonitorPointTypeData();
        Map<String, Object> codeAndTypeName = new HashMap<>();
        for (Map<String, Object> typemap : alltypes) {
            codeAndTypeName.put(typemap.get("typecode").toString(), typemap.get("typename"));
        }
        Map<String, Object> paramMap = new HashMap<>();
        if (monitorpointtypecodes == null || monitorpointtypecodes.size() == 0) {
            monitorpointtypecodes = Arrays.asList(
                    EnvironmentalVocEnum.getCode(),
                    AirEnum.getCode(),
                    EnvironmentalStinkEnum.getCode(),
                    MicroStationEnum.getCode(),
                    RainEnum.getCode(),
                    WasteWaterEnum.getCode(),
                    WasteGasEnum.getCode(),
                    SmokeEnum.getCode(),
                    WaterQualityEnum.getCode(),
                    EnvironmentalDustEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode());
        }

        List<String> mns = new ArrayList<>();
        Map<String, Object> mnAndType = new HashMap<>();

        Map<String, Object> mnAndPollutionId = new HashMap<>();
        Map<String, Object> mnAndPollutionName = new HashMap<>();
        Map<String, Object> mnAndShorterName = new HashMap<>();
        Map<String, Object> mnAndMonitorPointId = new HashMap<>();
        Map<String, Object> mnAndMonitorPointName = new HashMap<>();
        Map<String, Map<String, Object>> mnAndmapdata = new HashMap<>();
        String mnCommon;
        List<Map<String, Object>> pointDataList;
        for (Integer code : monitorpointtypecodes) {
            paramMap.clear();
            paramMap.put("monitorpointtypecode", code);
            if (CommonTypeEnum.getMonitorPointTypeList().contains(code)) {
                paramMap.put("monitorpointname", searchname);
            } else {
                paramMap.put("pollutionname", searchname);
            }
            pointDataList = onlineService.getMNAndMonitorPointByParam(paramMap);
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("dgimn") != null) {
                        mnCommon = pointData.get("dgimn").toString();
                        mns.add(mnCommon);
                        mnAndPollutionId.put(mnCommon, pointData.get("pk_pollutionid") != null ? pointData.get("pk_pollutionid") : "");
                        mnAndPollutionName.put(mnCommon, pointData.get("pollutionname") != null ? pointData.get("pollutionname") : "");
                        mnAndShorterName.put(mnCommon, pointData.get("shortername") != null ? pointData.get("shortername") : "");
                        mnAndMonitorPointId.put(mnCommon, pointData.get("monitorpointid"));
                        mnAndMonitorPointName.put(mnCommon, pointData.get("monitorpointname"));
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("longitude", pointData.get("Longitude"));
                        onemap.put("latitude", pointData.get("Latitude"));
                        onemap.put("alarmlevel", pointData.get("AlarmLevel"));
                        onemap.put("status", pointData.get("onlinestatus"));
                        if (pointData.get("MonitorPointCategory") != null) {
                            onemap.put("pointcategory", pointData.get("MonitorPointCategory"));
                        }
                        mnAndmapdata.put(mnCommon, onemap);
                        mnAndType.put(mnCommon, code);
                    }
                }
            }
        }
        paramMap.clear();
        paramMap.put("mns", mns);
        paramMap.put("mnAndType", mnAndType);
        paramMap.put("codeAndTypeName", codeAndTypeName);
        paramMap.put("mnAndPollutionId", mnAndPollutionId);
        paramMap.put("mnAndPollutionName", mnAndPollutionName);
        paramMap.put("mnAndShorterName", mnAndShorterName);
        paramMap.put("mnAndMonitorPointId", mnAndMonitorPointId);
        paramMap.put("mnAndMonitorPointName", mnAndMonitorPointName);
        paramMap.put("mnAndmapdata", mnAndmapdata);
        return paramMap;
    }


    /**
     * @author: xsm
     * @date: 2020/5/07 0007 下午 9:07
     * @Description: 通过排口类型获取近一个月污染源超标、超限、异常报警排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "/getLastMonthAlarmAndExceptionRankByType", method = RequestMethod.POST)
    public Object getLastMonthAlarmAndExceptionRankByType() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            data = pollutionService.getPollutionOutputMn(paramMap);
            //获取近一个月报警数据
            if (data != null && data.size() > 0) {
                List<String> collect1 = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
                paramMap.clear();
                paramMap.put("dgimns", collect1);
                List<Map> maps = onlineCountAlarmService.countLastMonthAlarmAndExceptionByParamMap(paramMap);
                if (maps != null) {
                    for (Map map : maps) {
                        String dataGatherCode = map.get("_id") == null ? "" : map.get("_id").toString();
                        for (Map<String, Object> datum : data) {
                            String dgimn = datum.get("DGIMN") == null ? "" : datum.get("DGIMN").toString();
                            if (dgimn.equals(dataGatherCode)) {
                                map.put("pollutionid", datum.get("Pollutionid"));
                                map.put("pollutantname", datum.get("PollutionName"));
                                map.put("shortername", datum.get("ShorterName"));

                            }
                        }
                    }
                }
                if (maps != null && maps.size() > 0) {
                    Map<String, List<Map>> collect = maps.stream().filter(m -> m.get("pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("pollutionid").toString()));
                    for (String key : collect.keySet()) {
                        List<Map> list = collect.get(key);
                        Integer count = list.stream().filter(m -> m.get("count") != null).map(m -> Integer.valueOf(m.get("count").toString())).collect(Collectors.summingInt(m -> m));
                        if (list.size() > 0) {
                            Map map = list.get(0);
                            map.put("count", count);
                            if (count > 0) {
                                resultList.add(map);
                            }
                        }
                    }
                }
            }
            if (resultList != null && resultList.size() > 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("count") != null).sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("count").toString())).reversed()).collect(Collectors.toList()));
            } else {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/02 0002 下午 16:22
     * @Description: 跳转获取企业某个报警类型的报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Document> getPollutionAlarmRemindNumByParam(OnlineAlarmCountQueryVO queryVO, CommonTypeEnum.RemindTypeEnum remindTypeEnum, String timeType) {
        if (remindTypeEnum == ConcentrationChangeEnum || remindTypeEnum == FlowChangeEnum) {
            if (remindTypeEnum == ConcentrationChangeEnum) {
                queryVO.setUnwindFieldName("MinuteDataList");
            } else {
                queryVO.setUnwindFieldName("HourFlowDataList");
            }
            return onlineCountAlarmService.countNDAndPFLAlarmNumByTimeType(queryVO, timeType);
        } else {
            queryVO.setUnwindFieldName(null);
            return onlineCountAlarmService.countOtherLAlarmNumByTimeType(queryVO, timeType);
        }
    }


    /**
     * @author: xsm
     * @date: 2020/5/29 0029 下午 13:13
     * @Description: 通过企业ID、监测月份和报警类型统计各报警类型（和企业关联）某个月的报警次数和占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countPollutionAlarmNumGroupByAlarmTypeByParamMap", method = RequestMethod.POST)
    public Object countPollutionAlarmNumGroupByAlarmTypeByParamMap(@RequestJson(value = "monthtime") String monthtime,
                                                                   @RequestJson(value = "pollutionid") String pollutionid,
                                                                   @RequestJson(value = "reminds") List<Integer> reminds) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fk_pollutionid", pollutionid);
            paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode()));
            //根据污染源ID获取和企业关联的所有点位信息
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
            Set<String> mns = new HashSet();
            if (outPutInfosByParamMap != null && outPutInfosByParamMap.size() > 0) {
                for (Map<String, Object> map : outPutInfosByParamMap) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                    }
                }
            }
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", monthtime, monthtime);
            OnlineAlarmCountQueryVO queryVO = new OnlineAlarmCountQueryVO();
            queryVO.setMns(mns);
            queryVO.setStartTime(dates[0]);
            queryVO.setEndTime(dates[1]);
            List<Integer> remindlist = new ArrayList<>();
            List<String> exceptiontypes = new ArrayList<>();
            int count = 0;
            for (Integer i : reminds) {
                if (i == CommonTypeEnum.RemindTypeEnum.ZeroExceptionAlarmEnum.getCode()) {
                    exceptiontypes.add(ZeroExceptionEnum.getCode());
                } else if (i == CommonTypeEnum.RemindTypeEnum.ContinuousExceptionAlarmEnum.getCode()) {
                    exceptiontypes.add(ContinuousExceptionEnum.getCode());
                } else {
                    remindlist.add(i);
                }
            }
            Map<String, Object> overmap = new HashMap<>();
            Map<String, Object> earlymap = new HashMap<>();
            Map<String, Object> changemap = new HashMap<>();
            String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb("month");
            for (int remind : remindlist) {
                CommonTypeEnum.RemindTypeEnum remindTypeEnum = CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind);
                String timeFieldName = "";
                String collection = "";
                if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {
                    timeFieldName = "OverTime";
                    collection = "OverData";
                } else if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()) {
                    timeFieldName = "MonitorTime";
                    collection = "MinuteData";
                } else if (remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                    timeFieldName = "MonitorTime";
                    collection = "HourFlowData";
                } else if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {
                    timeFieldName = "EarlyWarnTime";
                    collection = "EarlyWarnData";
                }
                queryVO.setTimeFieldName(timeFieldName);
                queryVO.setCollection(collection);

                List<Document> documents = getPollutionAlarmRemindNumByParam(queryVO, remindTypeEnum, timeStyle);
                if (documents.size() > 0) {
                    if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {  //阈值
                        earlymap.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getName());
                        earlymap.put("alarmtype", "early");
                        earlymap.put("num", documents.get(0).get("num"));
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode() ||
                            remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {    //突变
                        changemap.put("alarmtypename", "突变预警");
                        changemap.put("alarmtype", "change");
                        changemap.put("num", changemap.get("num") != null ? Integer.parseInt(changemap.get("num").toString()) + documents.get(0).getInteger("num") : documents.get(0).getInteger("num"));
                    } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                        overmap.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getName());
                        overmap.put("alarmtype", "over");
                        overmap.put("num", documents.get(0).get("num"));
                    }
                    count += documents.get(0).getInteger("num");
                }
            }
            if (overmap != null && overmap.size() > 0) {
                result.add(overmap);
            }
            if (earlymap != null && earlymap.size() > 0) {
                result.add(earlymap);
            }
            if (changemap != null && changemap.size() > 0) {
                result.add(changemap);
            }
            List<Document> list = onlineCountAlarmService.countAllExceptionTypeDataNumByDayTime(dates[0], dates[1], mns, timeStyle);
            for (String type : exceptiontypes) {
                Map<String, Object> map = new HashMap<>();
                map.put("exceptiontype", type);
                if (type.equals(ZeroExceptionEnum.getCode())) {
                    map.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.ZeroExceptionAlarmEnum.getName());
                    map.put("alarmtype", "zeroexception");
                } else if (type.equals(ContinuousExceptionEnum.getCode())) {
                    map.put("alarmtypename", CommonTypeEnum.RemindTypeEnum.ContinuousExceptionAlarmEnum.getName());
                    map.put("alarmtype", "continuousexception");
                }
                int num = 0;
                if (list.size() > 0) {
                    for (Document document : list) {
                        if (type.equals(document.getString("ExceptionType"))) {
                            count += document.getInteger("num");
                            num = document.getInteger("num");

                        }
                    }
                }
                if (num > 0) {
                    map.put("num", num);
                    result.add(map);
                }
            }
            if (count > 0) {
                for (Map<String, Object> obj : result) {
                    obj.put("totalnum", count);
                    obj.put("proportion", DataFormatUtil.SaveOneAndSubZero(Double.parseDouble(obj.get("num").toString()) * 100 / count) + "%");
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/5/29 0029 下午 13:13
     * @Description: 通过企业ID、监测月份和报警类型统计各排口/点位（和企业关联）某个月的报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countPollutionAlarmNumGroupByMonitorTypeByParamMap", method = RequestMethod.POST)
    public Object countPollutionAlarmNumGroupByMonitorTypeByParamMap(@RequestJson(value = "monthtime") String monthtime,
                                                                     @RequestJson(value = "pollutionid") String pollutionid,
                                                                     @RequestJson(value = "reminds") List<Integer> reminds) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fk_pollutionid", pollutionid);
            paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode()));
            //根据污染源ID获取和企业关联的所有点位信息
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
            Set<String> mns = new HashSet();
            Map<String, Object> mnandname = new HashMap<>();
            if (outPutInfosByParamMap != null && outPutInfosByParamMap.size() > 0) {
                for (Map<String, Object> map : outPutInfosByParamMap) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                        mnandname.put(map.get("DGIMN").toString(), map.get("OutputName"));
                        /*mnandtypecode.put(map.get("DGIMN").toString(), map.get("FK_MonitorPointTypeCode"));
                        typename.put(map.get("FK_MonitorPointTypeCode").toString(), map.get("FK_MonitorPointTypeName"));*/
                    }
                }
            }
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", monthtime, monthtime);
            List<Integer> remindlist = new ArrayList<>();
            List<String> exceptiontypes = new ArrayList<>();
            int count = 0;
            for (Integer i : reminds) {
                if (i == CommonTypeEnum.RemindTypeEnum.ZeroExceptionAlarmEnum.getCode()) {
                    exceptiontypes.add(ZeroExceptionEnum.getCode());
                } else if (i == CommonTypeEnum.RemindTypeEnum.ContinuousExceptionAlarmEnum.getCode()) {
                    exceptiontypes.add(ContinuousExceptionEnum.getCode());
                } else {
                    remindlist.add(i);
                }
            }
            Map<String, Integer> mnandnum = new HashMap<>();
            for (int remind : remindlist) {
                List<Map> documents = onlineCountAlarmService.countChangeAndOverAlarmNumByTimeType(remind, mns, dates[0], dates[1]);
                if (documents.size() > 0) {
                    for (Map map : documents) {
                        String dgimn = map.get("_id") != null ? map.get("_id").toString() : "";
                        int num = 0;
                        if (map.get("alarmcount") != null) {
                            num = Integer.parseInt(map.get("alarmcount").toString());
                        }
                        count += num;
                        if (!"".equals(dgimn) && mnandnum.get(dgimn) != null) {
                            mnandnum.put(dgimn, mnandnum.get(dgimn) + num);
                        } else {
                            if (!"".equals(dgimn) && mnandnum.get(dgimn) == null) {
                                mnandnum.put(dgimn, num);
                            }
                        }
                    }
                }
            }
            for (String exceptiontype : exceptiontypes) {
                List<Map> list = onlineCountAlarmService.countZeroOrContinuousExceptionNum(exceptiontype, mns, dates[0], dates[1]);
                if (list.size() > 0) {
                    for (Map map : list) {
                        String dgimn = map.get("_id") != null ? map.get("_id").toString() : "";
                        int num = 0;
                        if (map.get("alarmcount") != null) {
                            num = Integer.parseInt(map.get("alarmcount").toString());
                        }
                        count += num;
                        if (!"".equals(dgimn) && mnandnum.get(dgimn) != null) {
                            mnandnum.put(dgimn, mnandnum.get(dgimn) + num);
                        } else {
                            if (!"".equals(dgimn) && mnandnum.get(dgimn) == null) {
                                mnandnum.put(dgimn, num);
                            }
                        }
                    }
                }
            }
            if (count > 0) {
                for (Map.Entry<String, Integer> entry : mnandnum.entrySet()) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("mn", entry.getKey());
                    obj.put("totalnum", count);
                    obj.put("monitorpointname", mnandname.get(entry.getKey()));
                    obj.put("num", entry.getValue());
                    obj.put("proportion", DataFormatUtil.SaveOneAndSubZero(Double.valueOf(entry.getValue() * 100) / count) + "%");
                    result.add(obj);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/18 0018 下午 6:22
     * @Description: 自定义查询条件统计各监测类型各报警类型报警数量（app）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countMonitorPointAlarmDataByParamForApp", method = RequestMethod.POST)
    public Object countMonitorPointAlarmDataByParamForApp(@RequestJson(value = "nowday", required = false) String nowday,
                                                          @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                          @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                          @RequestJson(value = "remindtype") Integer remindtype


    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Date today = new Date();
            if (StringUtils.isNotBlank(nowday)) {
                today = DataFormatUtil.getDateYMD(nowday);
            }
            String starttime = DataFormatUtil.getDateYMD(today);
            String endtime = DataFormatUtil.getDateYMD(today);
            Map<String, Object> paramMap = new HashMap<>();
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("remindtype", remindtype);
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            List<String> mns = new ArrayList<>();
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            Map<String, Object> paramTemp = new HashMap<>();
            paramTemp.put("outputids", Arrays.asList());
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Integer type : monitorpointtypes) {
                    paramTemp.put("monitorpointtype", type);
                    paramTemp.put("userid", userId);
                    monitorPoints.addAll(onlineService.getMonitorPointDataByParam(paramTemp));
                }
            } else {
                if (monitorpointtype != null) {
                    paramTemp.put("monitorpointtype", monitorpointtype);
                    paramTemp.put("userid", userId);
                    monitorPoints.addAll(onlineService.getMonitorPointDataByParam(paramTemp));
                }
            }
            String mnCommon;
            for (Map<String, Object> map : monitorPoints) {
                mnCommon = map.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndMonitorPointId.put(mnCommon, map.get("monitorpointid"));
                mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
            }
            paramMap.put("mns", mns);
            switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindtype)) {
                case ConcentrationChangeEnum:
                    paramMap.put("collection", "HourData");
                    paramMap.put("timeKey", "MonitorTime");
                    paramMap.put("unwindkey", "HourDataList");
                    break;
                case FlowChangeEnum:
                    paramMap.put("collection", "HourFlowData");
                    paramMap.put("timeKey", "MonitorTime");
                    paramMap.put("unwindkey", "HourFlowDataList");
                    break;
                case OverAlarmEnum:
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "OverData");
                    paramMap.put("timeKey", "OverTime");
                    break;
                case EarlyAlarmEnum:
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "EarlyWarnData");
                    paramMap.put("timeKey", "EarlyWarnTime");
                    break;
                case ExceptionAlarmEnum:
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "ExceptionData");
                    paramMap.put("timeKey", "ExceptionTime");
                    break;
                case WaterNoFlowEnum://废水无流量异常
                    paramMap.remove("unwindkey");
                    paramMap.put("collection", "ExceptionData");
                    paramMap.put("timeKey", "ExceptionTime");
                    break;
                default:
                    break;
            }
            Set pollutionids = new HashSet();
            Set monitorpointids = new HashSet();
            Set pollutants = new HashSet();
            List<Document> documents = countPollutantDataByParam(paramMap);
            if (documents.size() > 0) {
                paramMap.clear();
                paramMap.put("monitorpointtypes", monitorpointtypes);
                List<Map<String, Object>> listdata = pollutantService.getPollutantsByPollutantType(paramMap);
                Map<String, Object> codeandname = new HashMap<>();
                for (Map<String, Object> map : listdata) {
                    codeandname.put(map.get("code").toString(), map.get("name"));
                }
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutionids.add(mnAndMonitorPointId.get(mnCommon));
                    monitorpointids.add(mnAndMonitorPointId.get(mnCommon));
                    pollutants.add(codeandname.get(document.getString("PollutantCode")));
                }
            }
            resultMap.put("pollutionnum", pollutionids.size());
            resultMap.put("pointnum", monitorpointids.size());
            resultMap.put("pollutantnames", pollutants);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2020/5/07 0007 下午 9:07
     * @Description: 通过报警类型获取当前月污染源超标、超限、异常报警排名
     * @updateUser:xsm
     * @updateDate:2021/04/25 0025 下午2:33
     * @updateDescription:根据配置增加统计安全报警
     * @updateUser:xsm
     * @updateDate:2022/01/24 0024 上午8:46
     * @updateDescription:去掉安全相关
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "/getLastMonthPollutionAlarmRankByAlarmTypes", method = RequestMethod.POST)
    public Object getLastMonthPollutionAlarmRankByAlarmTypes(@RequestJson(value = "monthtime") String monthtime, @RequestJson(value = "reminds") List<Integer> reminds) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            Map<String, List<Map<String, Object>>> collect = new HashMap<>();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            data = pollutionService.getPollutionOutputMn(paramMap);

            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", monthtime, monthtime);
            //获取近一个月报警数据
            if (data != null && data.size() > 0) {
                List<String> mns = new ArrayList<>();
                Set<String> pollutionids = new HashSet<>();
                Map<String, Object> mnandid = new HashMap<>();
                Map<String, Object> mnandshortername = new HashMap<>();
                Map<String, Object> mnandname = new HashMap<>();
                for (Map<String, Object> map : data) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                        pollutionids.add(map.get("Pollutionid").toString());
                        mnandid.put(map.get("DGIMN").toString(), map.get("Pollutionid"));
                        mnandshortername.put(map.get("Pollutionid").toString(), map.get("ShorterName"));
                        mnandname.put(map.get("Pollutionid").toString(), map.get("PollutionName"));
                    }
                }
                List<Map> maps = new ArrayList<>();
                for (Integer remind : reminds) {
                    maps.addAll(onlineCountAlarmService.getLastMonthPollutionAlarmNum(remind, mns, dates[0], dates[1]));
                }

                if (maps != null && maps.size() > 0) {
                    for (String pollutionid : pollutionids) {
                        Set<String> timeset = new HashSet<>();
                        Map<String, Object> resultmap = new HashMap<>();
                        for (Map objmap : maps) {
                            String oldid = mnandid.get(objmap.get("DataGatherCode").toString()) != null ? mnandid.get(objmap.get("DataGatherCode").toString()).toString() : "";
                            if (pollutionid.equals(oldid)) {
                                timeset.add(objmap.get("theDate").toString());
                            }
                        }
                        resultmap.put("pollutionid", pollutionid);
                        resultmap.put("shortername", mnandshortername.get(pollutionid));
                        resultmap.put("pollutionname", mnandname.get(pollutionid));
                        resultmap.put("count", timeset.size());
                        if (timeset.size() > 0) {
                            resultList.add(resultmap);
                        }
                    }
                }
            }
            if (resultList != null && resultList.size() > 0) {
                return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("count") != null).sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("count").toString())).reversed()).collect(Collectors.toList()));
            } else {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/5/19 0019 下午 6:22
     * @Description: 通过自定义条件获取企业安全报警数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monthtime, reminds]
     * @throws:
     */
    @RequestMapping(value = "/countPollutionSecurityAlarmRankByParams", method = RequestMethod.POST)
    public Object countPollutionSecurityAlarmRankByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> data = pollutionService.getPollutionSecurityPointMn(paramMap);
            Map<String, List<Map<String, Object>>> PollutionidMap = data.stream().filter(m -> m.get("Pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("Pollutionid").toString()));

            List<String> DGIMNs = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());

            String startTime = JSONObjectUtil.getStartTime(starttime);
            String endTime = JSONObjectUtil.getEndTime(endtime);

            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("mns", DGIMNs);
            paramMap.put("collection", overDataCollect);

            List<Map> list = onlineMonitorService.countOverDataByParams(paramMap);

            for (String Pollutionid : PollutionidMap.keySet()) {
                List<Map<String, Object>> monitordata = PollutionidMap.get(Pollutionid);
                Map<String, Object> pollutionmap = monitordata.stream().filter(m -> m.get("ShorterName") != null).findFirst().orElse(new HashMap<>());
                Integer sum = 0;
                List<String> collect = monitordata.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
                for (Map map : list) {
                    String dgimn = map.get("_id") == null ? "" : map.get("_id").toString();
                    int count = map.get("count") == null ? 0 : Integer.valueOf(map.get("count").toString());
                    if (collect.contains(dgimn)) {
                        sum += count;
                    }
                }

                if (sum > 0) {
                    Map<String, Object> resultdata = new HashMap<>();
                    resultdata.put("ShorterName", pollutionmap.get("ShorterName"));
                    resultdata.put("Pollutionid", Pollutionid);
                    resultdata.put("count", sum);
                    resultList.add(resultdata);
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("count") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("count").toString()).reversed()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/5/20 0020 上午 10:12
     * @Description: 通过多参数获取在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monthtime, pollutionid, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "/getPollutionAlarmRankByParams", method = RequestMethod.POST)
    public Object getPollutionAlarmRankByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime,
                                                @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                @RequestJson(value = "pagenum", required = false) Integer pagenum) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> data = pollutionService.getPollutionSecurityPointMn(paramMap);

            List<String> DGIMNs = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            Map<String, String> outputMap = data.stream().filter(m -> m.get("DGIMN") != null && m.get("outputname") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("outputname").toString(), (a, b) -> a));
            Map<String, String> PollutionNameMap = data.stream().filter(m -> m.get("DGIMN") != null && m.get("PollutionName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("PollutionName").toString(), (a, b) -> a));
            Map<String, String> outputInfoMap = data.stream().filter(m -> m.get("DGIMN") != null && m.get("outputid") != null && m.get("type") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("type").toString() + "_" + m.get("outputid").toString(), (a, b) -> a));


            String startTime = JSONObjectUtil.getStartTime(starttime);
            String endTime = JSONObjectUtil.getEndTime(endtime);

            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("sortfield", "OverTime");
            paramMap.put("sort", "desc");
            paramMap.put("monitortimefield", "OverTime");
            paramMap.put("mns", DGIMNs);
            paramMap.put("collection", "OverData");

            List<Document> monitorDataByParamMap = onlineMonitorService.getAlarmMonitorDataByParamMap(paramMap);

            List<Integer> pollutanttypes = new ArrayList<>();
            List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = new ArrayList<>();
            for (Integer pollutanttype : pollutanttypes) {
                paramMap.put("pollutanttype", pollutanttype);
                gasOutPutPollutantSetsByOutputIds.addAll(gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap));
            }
            Map<String, List<Map<String, Object>>> collect = gasOutPutPollutantSetsByOutputIds.stream().filter(m -> m.get("pollutanttype") != null && m.get("outputid") != null).collect(Collectors.groupingBy(m -> m.get("outputid").toString() + "_" + m.get("pollutanttype").toString()));
            for (Document document : monitorDataByParamMap) {
                Map<String, Object> datamap = new HashMap<>();
                String DataGatherCode = document.get("DataGatherCode") == null ? "" : document.get("DataGatherCode").toString();
                String AlarmType = document.get("AlarmType") == null ? "" : document.get("AlarmType").toString();
                String MonitorValue = document.get("MonitorValue") == null ? "" : document.get("MonitorValue").toString();
                String PollutantCode = document.get("PollutantCode") == null ? "" : document.get("PollutantCode").toString();
                String OverTime = document.get("OverTime") == null ? "" : FormatUtils.formatCSTString(document.get("OverTime").toString(), "yyyy-MM-dd HH:mm:ss");
                String typeandid = outputInfoMap.get(DGIMNs);
                List<Map<String, Object>> objects = collect.get(typeandid) == null ? new ArrayList<>() : collect.get(typeandid);
                Map<String, Object> pollutantdata = objects.stream().findFirst().orElse(new HashMap<>());
                datamap.put("PollutionName", PollutionNameMap.get(DataGatherCode));
                datamap.put("outputname", outputMap.get(DataGatherCode));
                datamap.put("PollutantCode", PollutantCode);
                datamap.put("MonitorValue", MonitorValue);
                datamap.put("AlarmType", AlarmType);
                datamap.put("PollutantName", pollutantdata.get("pollutantname"));
                datamap.put("PollutantUnit", pollutantdata.get("PollutantUnit"));
                datamap.put("StandardMaxValue", pollutantdata.get("StandardMaxValue"));
                datamap.put("StandardMinValue", pollutantdata.get("StandardMinValue"));
                datamap.put("OverTime", OverTime);
                resultList.add(datamap);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("OverTime") != null && m.get("outputname") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("OverTime").toString()).reversed()
                    .thenComparing(m -> ((Map<String, Object>) m).get("outputname").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/21 0021 下午 6:15
     * @Description: 通过自定义参数获取报警污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "/getAlarmPollutantInfoByParamForApp", method = RequestMethod.POST)
    public Object getAlarmPollutantInfoByParamForApp(@RequestJson(value = "daytime") String daytime,
                                                     @RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                     @RequestJson(value = "remind") Integer remind) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            Date startDate = DataFormatUtil.parseDate(daytime + " 00:00:00");
            Date endDate = DataFormatUtil.parseDate(daytime + " 23:59:59");
            //获取近一个月报警数据
            List<Map> maps = new ArrayList<>();
            maps = onlineCountAlarmService.getAlarmPollutantInfoByParamForApp(remind, dgimn, startDate, endDate, monitorpointtype);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/1/25 0025 上午 11:25
     * @Description: 获取（微站、恶臭日超标统计数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointDayOverDataByParam", method = RequestMethod.POST)
    public Object getPointDayOverDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                             @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                             @RequestJson(value = "avgvalue", required = false) String avgvalue,
                                             @RequestJson(value = "pollutantcode") String pollutantcode


    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            List<Map<String, Object>> allPointList = new ArrayList<>();
            List<Map<String, Object>> pointList;
            for (Integer type : monitorpointtypes) {
                paramMap.put("outputids", Arrays.asList());
                paramMap.put("monitorpointtype", type);
                pointList = onlineService.getMonitorPointDataByParam(paramMap);
                allPointList.addAll(pointList);
            }

            if (allPointList.size() > 0) {
                List<String> mns = new ArrayList<>();
                String mnCommon;
                Map<String, Object> mnAndId = new HashMap<>();
                Map<String, Object> mnAndName = new HashMap<>();
                for (Map<String, Object> point : allPointList) {
                    mnCommon = point.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndId.put(mnCommon, point.get("pk_id"));
                    mnAndName.put(mnCommon, point.get("monitorpointname"));
                }
                paramMap.clear();
                paramMap.put("starttime", monitortime + " 00:00:00");
                paramMap.put("endtime", monitortime + " 23:59:59");
                paramMap.put("mns", mns);
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("collection", dayDataCollect);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Document> pollutantList;
                Map<String, Double> mnAndThisValue = new HashMap<>();
                String value;
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutantList = document.get("DayDataList", List.class);
                    for (Document pollutant : pollutantList) {
                        if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                            value = pollutant.getString("AvgStrength");
                            if (StringUtils.isNotBlank(value)) {
                                mnAndThisValue.put(mnCommon, Double.parseDouble(value));
                            }
                            break;
                        }
                    }
                }
                String thatDay = DataFormatUtil.getBeforeByDayTime(1, monitortime);
                paramMap.put("starttime", thatDay + " 00:00:00");
                paramMap.put("endtime", thatDay + " 23:59:59");
                documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Double> mnAndThatValue = new HashMap<>();
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutantList = document.get("DayDataList", List.class);
                    for (Document pollutant : pollutantList) {
                        if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                            value = pollutant.getString("AvgStrength");
                            if (StringUtils.isNotBlank(value)) {
                                mnAndThatValue.put(mnCommon, Double.parseDouble(value));
                            }
                            break;
                        }
                    }
                }

                Double thisValue;
                Double thatValue;
                Double changeValueD;
                Double totalValue = 0d;
                Map<String, Double> mnAndChange = new HashMap<>();
                Map<String, Double> mnAndChangeZ = new HashMap<>();
                for (String mnIndex : mnAndThisValue.keySet()) {
                    thisValue = mnAndThisValue.get(mnIndex);
                    if (mnAndThatValue.containsKey(mnIndex)) {
                        thatValue = mnAndThatValue.get(mnIndex);
                        if (thatValue > 0) {
                            changeValueD = DataFormatUtil.formatDoubleSaveTwoDouble((thisValue - thatValue) * 100d / thatValue);
                            if (changeValueD > 0) {
                                mnAndChangeZ.put(mnIndex, changeValueD);
                            }
                            mnAndChange.put(mnIndex, changeValueD);
                        }

                    }
                    totalValue += thisValue;
                }
                Double avgValue;
                if (StringUtils.isBlank(avgvalue)) {
                    if (mnAndThisValue.size() > 0) {
                        avgValue = totalValue / mnAndThisValue.size();
                    } else {
                        avgValue = 0d;
                    }
                } else {
                    avgValue = Double.parseDouble(avgvalue);
                }

                mnAndChange = DataFormatUtil.sortMapByValue(mnAndChange, true);
                Map<String, Integer> mnAndRank = new HashMap<>();
                int rank = 1;
                for (String mnIndex : mnAndChange.keySet()) {
                    mnAndRank.put(mnIndex, rank);
                    rank++;
                }
                Map<String, Double> mnAndChangeZD = new HashMap<>();

                Double changeDay;
                for (String mnIndex : mnAndId.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("monitorpointid", mnAndId.get(mnIndex));
                    dataMap.put("monitorpointname", mnAndName.get(mnIndex));
                    thisValue = mnAndThisValue.get(mnIndex);
                    if (thisValue != null) {
                        dataMap.put("thisvalue", thisValue);
                    } else {
                        dataMap.put("thisvalue", -99999);
                    }
                    thatValue = mnAndThatValue.get(mnIndex);
                    if (thatValue != null) {
                        dataMap.put("thatvalue", thatValue);
                    } else {
                        dataMap.put("thatvalue", "-");
                    }
                    if (mnAndRank.containsKey(mnIndex)) {
                        dataMap.put("changevalue", DataFormatUtil.SaveTwoAndSubZero(mnAndChange.get(mnIndex)) + "%");
                        dataMap.put("thatrank", mnAndRank.get(mnIndex));
                    } else {
                        dataMap.put("changevalue", "-");
                        dataMap.put("thatrank", "-");
                    }
                    if (thisValue != null && avgValue > 0) {
                        changeDay = 100d * (thisValue - avgValue) / avgValue;
                        if (changeDay > 0) {
                            mnAndChangeZD.put(mnIndex, changeDay);
                        }
                        dataMap.put("changeday", DataFormatUtil.SaveTwoAndSubZero(changeDay) + "%");
                    } else {
                        dataMap.put("changeday", "-");
                    }
                    dataList.add(dataMap);
                }
                //排序
                int thisrank = 1;
                dataList = dataList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("thisvalue").toString())).reversed()).collect(Collectors.toList());
                for (Map<String, Object> dataMap : dataList) {
                    if ("-99999".equals(dataMap.get("thisvalue").toString())) {
                        dataMap.put("thisvalue", "-");
                    }
                    dataMap.put("thisrank", thisrank);
                    thisrank++;
                }
                if (avgValue > 0) {
                    resultMap.put("avgvalue", DataFormatUtil.SaveOneAndSubZero(avgValue));
                } else {
                    resultMap.put("avgvalue", "");
                }
                resultMap.put("datalist", dataList);
                //主导风向
                paramMap.put("outputids", Arrays.asList());
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                List<Map<String, Object>> qxPoints = onlineService.getMonitorPointDataByParam(paramMap);
                if (qxPoints.size() > 0) {
                    mns.clear();
                    for (Map<String, Object> point : qxPoints) {
                        mnCommon = point.get("dgimn").toString();
                        mns.add(mnCommon);
                    }
                }
                paramMap.clear();
                paramMap.put("starttime", monitortime + " 00:00:00");
                paramMap.put("endtime", monitortime + " 23:59:59");
                paramMap.put("mns", mns);
                paramMap.put("collection", dayDataCollect);
                pollutantcode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Integer> speedAndNum = new HashMap<>();
                String speedName = "XX";
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        pollutantList = document.get("DayDataList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                                value = pollutant.getString("AvgStrength");
                                if (StringUtils.isNotBlank(value)) {
                                    speedName = DataFormatUtil.windDirectionSwitch(Double.parseDouble(value), "name");
                                    if (speedAndNum.containsKey(speedName)) {
                                        speedAndNum.put(speedName, speedAndNum.get(speedName) + 1);
                                    } else {
                                        speedAndNum.put(speedName, 1);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                //排序浓度较大点
                mnAndChangeZD = DataFormatUtil.sortMapByValue(mnAndChangeZD, true);
                //排序增幅较大点
                mnAndChangeZ = DataFormatUtil.sortMapByValue(mnAndChangeZ, true);
                int topNum = 5;
                int indexNum = 1;
                List<String> highPointList = new ArrayList<>();
                for (String mnIndex : mnAndChangeZD.keySet()) {
                    highPointList.add(mnAndName.get(mnIndex).toString());
                    indexNum++;
                    if (indexNum > topNum) {
                        break;
                    }
                }
                indexNum = 1;
                List<String> changePointList = new ArrayList<>();
                for (String mnIndex : mnAndChangeZ.keySet()) {
                    changePointList.add(mnAndName.get(mnIndex).toString());
                    indexNum++;
                    if (indexNum > topNum) {
                        break;
                    }
                }
                int highPoint = highPointList.size();
                String pointNames = "XX";
                if (highPoint > 0) {
                    pointNames = "是" + DataFormatUtil.FormatListToString(highPointList, "、");
                }


                int changePoint = changePointList.size();
                String changeNames = "XX";

                if (changePoint > 0) {
                    changeNames = "是" + DataFormatUtil.FormatListToString(changePointList, "、");
                }
                speedAndNum = DataFormatUtil.sortMapByValue(speedAndNum, true);
                for (String index : speedAndNum.keySet()) {
                    speedName = index;
                    break;
                }
                String monitorPointTypeName;
                if (monitorpointtypes.contains(CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode())) {
                    monitorPointTypeName = "TVOC";
                } else {
                    monitorPointTypeName = "恶臭";
                }
                String ymd = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd", "yyyy年M月d日");
                int pointNum = mnAndId.size();
                String sumText = "【园区" + ymd + pointNum + "个" + monitorPointTypeName + "监测点总体情况】：根据平台数据统计分析，"
                        + ymd + pointNum + "个" + monitorPointTypeName + "监测点浓度较大的" + highPoint + "个点位"
                        + pointNames
                        + "；"
                        + "环比昨日增幅变化较大的" + changePoint + "个点位"
                        + changeNames
                        + "，依据园区主导风向，获取" + monitorPointTypeName + "的来源"
                        + speedName + "方向及XXXXX重点区域，确定XXXXX" + monitorPointTypeName + "监测点位昨日范围内对其有影响的哪家企业位置，"
                        + "确定其为今日重点检查企业。";
                resultMap.put("sumtext", sumText);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2021/1/25 0025 上午 11:25
     * @Description: 获取（微站、恶臭日超标统计数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPointDayOverDataByParam", method = RequestMethod.POST)
    public void exportPointDayOverDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                              @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                              @RequestJson(value = "avgvalue", required = false) String avgvalue,
                                              @RequestJson(value = "pollutantcode") String pollutantcode,
                                              HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {


            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            List<Map<String, Object>> allPointList = new ArrayList<>();
            List<Map<String, Object>> pointList;
            for (Integer type : monitorpointtypes) {
                paramMap.put("outputids", Arrays.asList());
                paramMap.put("monitorpointtype", type);
                pointList = onlineService.getMonitorPointDataByParam(paramMap);
                allPointList.addAll(pointList);
            }
            if (allPointList.size() > 0) {
                List<String> mns = new ArrayList<>();
                String mnCommon;
                Map<String, Object> mnAndId = new HashMap<>();
                Map<String, Object> mnAndName = new HashMap<>();
                for (Map<String, Object> point : allPointList) {
                    mnCommon = point.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndId.put(mnCommon, point.get("pk_id"));
                    mnAndName.put(mnCommon, point.get("monitorpointname"));
                }
                paramMap.clear();
                paramMap.put("starttime", monitortime + " 00:00:00");
                paramMap.put("endtime", monitortime + " 23:59:59");
                paramMap.put("mns", mns);
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("collection", dayDataCollect);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Document> pollutantList;
                Map<String, Double> mnAndThisValue = new HashMap<>();
                String value;
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutantList = document.get("DayDataList", List.class);
                    for (Document pollutant : pollutantList) {
                        if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                            value = pollutant.getString("AvgStrength");
                            if (StringUtils.isNotBlank(value)) {
                                mnAndThisValue.put(mnCommon, Double.parseDouble(value));
                            }
                            break;
                        }
                    }
                }
                String thatDay = DataFormatUtil.getBeforeByDayTime(1, monitortime);
                paramMap.put("starttime", thatDay + " 00:00:00");
                paramMap.put("endtime", thatDay + " 23:59:59");
                documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Double> mnAndThatValue = new HashMap<>();
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutantList = document.get("DayDataList", List.class);
                    for (Document pollutant : pollutantList) {
                        if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                            value = pollutant.getString("AvgStrength");
                            if (StringUtils.isNotBlank(value)) {
                                mnAndThatValue.put(mnCommon, Double.parseDouble(value));
                            }
                            break;
                        }
                    }
                }

                Double thisValue;
                Double thatValue;
                Double changeValueD;
                Double totalValue = 0d;
                Map<String, Double> mnAndChange = new HashMap<>();
                Map<String, Double> mnAndChangeZ = new HashMap<>();
                for (String mnIndex : mnAndThisValue.keySet()) {
                    thisValue = mnAndThisValue.get(mnIndex);
                    if (mnAndThatValue.containsKey(mnIndex)) {
                        thatValue = mnAndThatValue.get(mnIndex);
                        if (thatValue > 0) {
                            changeValueD = DataFormatUtil.formatDoubleSaveTwoDouble((thisValue - thatValue) * 100d / thatValue);
                            if (changeValueD > 0) {
                                mnAndChangeZ.put(mnIndex, changeValueD);
                            }
                            mnAndChange.put(mnIndex, changeValueD);
                        }

                    }
                    totalValue += thisValue;
                }
                Double avgValue;
                if (StringUtils.isBlank(avgvalue)) {
                    avgValue = totalValue / mnAndThisValue.size();
                } else {
                    avgValue = Double.parseDouble(avgvalue);
                }

                mnAndChange = DataFormatUtil.sortMapByValue(mnAndChange, true);
                Map<String, Integer> mnAndRank = new HashMap<>();
                int rank = 1;
                for (String mnIndex : mnAndChange.keySet()) {
                    mnAndRank.put(mnIndex, rank);
                    rank++;
                }
                Map<String, Double> mnAndChangeZD = new HashMap<>();

                Double changeDay;
                for (String mnIndex : mnAndId.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("monitorpointid", mnAndId.get(mnIndex));
                    dataMap.put("monitorpointname", mnAndName.get(mnIndex));
                    thisValue = mnAndThisValue.get(mnIndex);
                    if (thisValue != null) {
                        dataMap.put("thisvalue", thisValue);
                    } else {
                        dataMap.put("thisvalue", -99999);
                    }
                    thatValue = mnAndThatValue.get(mnIndex);
                    if (thatValue != null) {
                        dataMap.put("thatvalue", thatValue);
                    } else {
                        dataMap.put("thatvalue", "-");
                    }
                    if (mnAndRank.containsKey(mnIndex)) {
                        dataMap.put("changevalue", DataFormatUtil.SaveTwoAndSubZero(mnAndChange.get(mnIndex)) + "%");
                        dataMap.put("thatrank", mnAndRank.get(mnIndex));
                    } else {
                        dataMap.put("changevalue", "-");
                        dataMap.put("thatrank", "-");
                    }
                    if (thisValue != null && avgValue > 0) {
                        changeDay = 100d * (thisValue - avgValue) / avgValue;
                        if (changeDay > 0) {
                            mnAndChangeZD.put(mnIndex, changeDay);
                        }
                        dataMap.put("changeday", DataFormatUtil.SaveTwoAndSubZero(changeDay) + "%");
                    } else {
                        dataMap.put("changeday", "-");
                    }
                    dataList.add(dataMap);
                }
                //排序
                dataList = dataList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("thisvalue").toString())).reversed()).collect(Collectors.toList());
                int thisrank = 1;
                for (Map<String, Object> dataMap : dataList) {
                    if ("-99999".equals(dataMap.get("thisvalue").toString())) {
                        dataMap.put("thisvalue", "-");
                    }
                    dataMap.put("thisrank", thisrank);
                    thisrank++;
                }
                //导出excel

                //设置导出文件数据格式
                List<String> headersField = Arrays.asList("thisrank", "monitorpointname", "thisvalue", "changeday", "thatvalue", "changevalue", "thatrank");
                List<String> headers = Arrays.asList("排名", "点位名称", "浓度", "对比日均值", "昨日浓度", "对比上一天浓度", "浓度变化排名");
                //设置文件名称
                String monitorPointTypeName;
                if (monitorpointtypes.contains(CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode())) {
                    monitorPointTypeName = "TVOC";
                } else {
                    monitorPointTypeName = "恶臭";
                }
                String fileName = monitorPointTypeName + "监测统计数据导出文件_" + new Date().getTime();
                ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2019/8/20 0020 下午 1:08
     * @Description: 比较两个时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private boolean compare(String time1, String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //Date类的一个方法，如果a早于b返回true，否则返回false
        if (a.before(b))
            return true;
        else
            return false;
    }


    /**
     * @Description: 获取单个监测点突变数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/4/1 15:33
     */
    @RequestMapping(value = "getOnePollutantChangeWarnByParams", method = RequestMethod.POST)
    public Object getOnePollutantChangeWarnByParams(@RequestJson(value = "dgimn") String dgimn,
                                                    @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime,
                                                    @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> result = onlineService.getPollutantUpRushDischargeInfo(starttime, endtime, remindtype, dgimn, pollutantcode, collectiontype);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/01 0001 下午 1:17
     * @Description: 通过多参数获取单个点位某时间段的报警时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countOnePointAlarmTimesDataByParam", method = RequestMethod.POST)
    public Object countOnePointAlarmTimesDataByParam(@RequestJson(value = "datetype") String datetype,
                                                     @RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "remindtype") Integer remindtype,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                     @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("remindtype", remindtype);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            if ("hour".equals(datetype)) {//小时分组
                result = onlineCountAlarmService.getPointAlarmTimesDataGroupByHourTime(paramMap);
            } else if ("day".equals(datetype)) {//日分组
                result = onlineCountAlarmService.getPointAlarmTimesDataGroupByDayTime(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/24 0024 上午 11:13
     * @Description: 通过多参数获取多个点位某时间段的报警总时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:yyyy-mm-dd HH endtime:yyyy-mm-dd HH monitorpointtype:监测点类型
     * @throws:
     */
    @RequestMapping(value = "/countOverAlarmPointNumDataByParam", method = RequestMethod.POST)
    public Object countOverAlarmPointNumDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime,
                                                    @RequestJson(value = "datetype", required = false) String datetype) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            for (Integer monitorpointtype : monitorpointtypes) {
                paramMap.put("monitortype", monitorpointtype);
                if (monitortypes.contains(monitorpointtype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (monitorpointtype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else if (monitorpointtype == EnvironmentalVocEnum.getCode()
                        || monitorpointtype == EnvironmentalStinkEnum.getCode()
                        || monitorpointtype == EnvironmentalDustEnum.getCode()
                        || monitorpointtype == MicroStationEnum.getCode()) {//voc//恶臭
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            String mnCommon;
            List<String> dgimns = new ArrayList<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                }
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", datetype);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            result = onlineCountAlarmService.countOverAlarmPointNumDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/24 0024 下午 16:01
     * @Description: 通过多参数获取多个点位某小时的报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: hourtime yyyy-mm-dd hh  monitorpointtype:监测点类型
     * @throws:
     */
    @RequestMapping(value = "/getAllPointOverAlarmDataByParamForHour", method = RequestMethod.POST)
    public Object getAllPointOverAlarmDataByParamForHour(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                         @RequestJson(value = "shortername", required = false) String shortername,
                                                         @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                         @RequestJson(value = "daytime") String daytime,
                                                         @RequestJson(value = "hournum") Integer hournum
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("daytime", daytime);
            paramMap.put("hournum", hournum);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("shortername", shortername);
            paramMap.put("monitorpointname", monitorpointname);
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            for (Integer monitorpointtype : monitorpointtypes) {
                paramMap.put("monitortype", monitorpointtype);
                if (monitortypes.contains(monitorpointtype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (monitorpointtype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else if (monitorpointtype == EnvironmentalVocEnum.getCode()
                        || monitorpointtype == EnvironmentalStinkEnum.getCode()
                        || monitorpointtype == EnvironmentalDustEnum.getCode()
                        || monitorpointtype == MicroStationEnum.getCode()) {//voc//恶臭
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            String mnCommon;
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> mnandshortername = new HashMap<>();
            Map<String, Object> mnandmonitorpointname = new HashMap<>();
            Map<String, Object> mnandmonitorpointid = new HashMap<>();
            Map<String, Object> mnandtype = new HashMap<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnandshortername.put(mnCommon, map.get("shortername"));
                    mnandmonitorpointname.put(mnCommon, map.get("monitorpointname"));
                    mnandmonitorpointid.put(mnCommon, map.get("pk_id"));
                    mnandtype.put(mnCommon, map.get("fk_monitorpointtypecode"));
                }
            }
            paramMap.put("mnandmonitorpointname", mnandmonitorpointname);
            paramMap.put("mnandmonitorpointid", mnandmonitorpointid);
            paramMap.put("mnandshortername", mnandshortername);
            paramMap.put("mnandtype", mnandtype);
            paramMap.put("dgimns", dgimns);
            List<Map<String, Object>> datalist = onlineCountAlarmService.getAllPointOverAlarmDataByParamForHour(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/17 0017 下午 2:57
     * @Description: 通过多参数获取多个点位某时间段的报警总时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:yyyy-mm-dd endtime:yyyy-mm-dd  monitorpointtype:监测点类型
     * @throws:
     */
    @RequestMapping(value = "/countAllPointDayOverAlarmTimesDataByParam", method = RequestMethod.POST)
    public Object countAllPointDayOverAlarmTimesDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime,
                                                            @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes

    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            for (Integer monitorpointtype : monitorpointtypes) {
                paramMap.put("monitortype", monitorpointtype);
                if (monitortypes.contains(monitorpointtype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (monitorpointtype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else if (monitorpointtype == EnvironmentalVocEnum.getCode()
                        || monitorpointtype == EnvironmentalStinkEnum.getCode()
                        || monitorpointtype == EnvironmentalDustEnum.getCode()
                        || monitorpointtype == MicroStationEnum.getCode()) {//voc//恶臭
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            String mnCommon;
            List<String> dgimns = new ArrayList<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                }
            }
          /*  paramMap.put("dgimns", dgimns);
            result = onlineCountAlarmService.countAllPointDayOverAlarmTimesDataByParam(paramMap);*/


            String startTime = starttime + " 00:00:00";
            String endTime = endtime + " 23:59:59";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimns", dgimns);
            if (pollutantcodes != null) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }

            paramMap.put("collection",DB_OverModel);
            paramMap.put("mns",dgimns);
            long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
            boolean isMany = DataFormatUtil.dataIsMany(totalCount);
            if (isMany) {
                String flag = ReturnInfo.other_many.split("#")[0];
                String message = ReturnInfo.other_many.split("#")[1];
                return AuthUtil.returnObject(flag, message);
            }

            List<Document> documents = onlineDataCountService.getOverModelDataByParam(paramMap);
            result = getHourDataForDay(documents, "day", starttime, endtime);

            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/12/01 0001 下午 1:17
     * @Description: 通过多参数获取单个点位某时间段的报警时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime:yyyy-MM-dd HH:mm:ss
     * @throws:
     */
    @RequestMapping(value = "/countAllPointAlarmTimesDataByParam", method = RequestMethod.POST)
    public Object countAllPointAlarmTimesDataByParam(@RequestJson(value = "datetype") String datetype,
                                                     @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            for (Integer monitorpointtype : monitorpointtypes) {
                paramMap.put("monitortype", monitorpointtype);
                if (monitortypes.contains(monitorpointtype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (monitorpointtype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else if (monitorpointtype == EnvironmentalVocEnum.getCode()
                        || monitorpointtype == EnvironmentalStinkEnum.getCode()
                        || monitorpointtype == EnvironmentalDustEnum.getCode()
                        || monitorpointtype == MicroStationEnum.getCode()) {//voc//恶臭
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            String mnCommon;
            List<String> dgimns = new ArrayList<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                }
            }
            paramMap.put("dgimns", dgimns);
            if ("hour".equals(datetype)) {//小时分组
                result = onlineCountAlarmService.getPointAlarmTimesDataGroupByHourTime(paramMap);
            } else if ("day".equals(datetype)) {//日分组
                result = onlineCountAlarmService.getPointAlarmTimesDataGroupByDayTime(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/24 0024 上午 11:13
     * @Description: 通过多参数获取多个点位某时间段的报警总时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:yyyy-mm-dd endtime:yyyy-mm-dd  monitorpointtype:监测点类型
     * @throws:
     */
    @RequestMapping(value = "/countAllPointOverAlarmTimesDataByParam", method = RequestMethod.POST)
    public Object countAllPointOverAlarmTimesDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime,
                                                         @RequestJson(value = "pollutantcode", required = false) List<String> pollutantcodes

    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            for (Integer monitorpointtype : monitorpointtypes) {
                paramMap.put("monitortype", monitorpointtype);
                if (monitortypes.contains(monitorpointtype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (monitorpointtype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else if (monitorpointtype == EnvironmentalVocEnum.getCode()
                        || monitorpointtype == EnvironmentalStinkEnum.getCode()
                        || monitorpointtype == EnvironmentalDustEnum.getCode()
                        || monitorpointtype == MicroStationEnum.getCode()) {//voc//恶臭
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            String mnCommon;
            List<String> dgimns = new ArrayList<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                }
            }
            String startTime = starttime + " 00:00:00";
            String endTime = endtime + " 23:59:59";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimns", dgimns);
            if (pollutantcodes != null) {
                paramMap.put("polluantcodes", pollutantcodes);
            }
            paramMap.put("collection",DB_OverModel);
            paramMap.put("mns",dgimns);
            long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
            boolean isMany = DataFormatUtil.dataIsMany(totalCount);
            if (isMany) {
                String flag = ReturnInfo.other_many.split("#")[0];
                String message = ReturnInfo.other_many.split("#")[1];
                return AuthUtil.returnObject(flag, message);
            }
            List<Document> documents = onlineDataCountService.getOverModelDataByParam(paramMap);
            starttime = starttime + " 00";
            endtime = endtime + " 23";
            result = getHourDataForDay(documents, "hour", starttime, endtime);

            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private List<Map<String, Object>> getHourDataForDay(List<Document> documents, String dataType,
                                                        String starttime, String endtime) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Long>> dayAndCodeAndMinute = new HashMap<>();
        Map<String, List<List<Date>>> dayAndStart_End = new HashMap<>();
        if (documents.size() > 0) {
            Date startT;
            Date endT;
            Integer Sday;
            Integer Eday;
            String pollutantCode;
            int dayN;
            String dayString;
            for (Document document : documents) {
                startT = document.getDate("FirstOverTime");
                endT = document.getDate("LastOverTime");
                Sday = DataFormatUtil.getDateDayNum(startT);
                Eday = DataFormatUtil.getDateDayNum(endT);
                pollutantCode = document.getString("PollutantCode");
                //判断是否跨日
                dayN = Eday - Sday;
                if (dayN == 0) {//否
                    setDayMapToNum(pollutantCode, startT, startT, endT, dayAndCodeAndMinute);
                    setDayAndSE(Sday, startT, endT, dayAndStart_End);
                } else {//是
                    for (int i = Sday; i < Eday; i++) {
                        if (i < 10) {
                            dayString = "0" + i;
                        } else {
                            dayString = "" + i;
                        }
                        if (i != Sday) {
                            startT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYM(startT) + "-" + dayString + "00:00:00");
                        }
                        Date endTE = DataFormatUtil.getDateYMDHMS((DataFormatUtil.getDateYM(startT) + "-" + dayString + "23:59:59"));
                        setDayMapToNum(pollutantCode, startT, startT, endTE, dayAndCodeAndMinute);
                        setDayAndSE(i, startT, endT, dayAndStart_End);
                    }
                    startT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(endT) + " 00:00:00");
                    setDayMapToNum(pollutantCode, endT, startT, endT, dayAndCodeAndMinute);
                    setDayAndSE(Eday, startT, endT, dayAndStart_End);
                }
            }


        }
        if (dataType.contains("hour")) {
            List<String> ymdh = DataFormatUtil.getYMDHBetween(starttime, endtime);
            ymdh.add(endtime);
            Map<String, Integer> dayAndM = setHourAndM(dayAndStart_End);
            for (String hour : ymdh) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitortime", hour);
                resultMap.put("value", dayAndM.get(hour));
                resultList.add(resultMap);
            }
        } else {
            List<String> days = DataFormatUtil.getYMDBetween(starttime, endtime);
            days.add(endtime);
            Map<String, Integer> dayAndM = setDayAndM(dayAndStart_End);
            for (String day : days) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitortime", day);
                resultMap.put("value", dayAndM.get(day));
                resultList.add(resultMap);
            }
        }


        return resultList;
    }

    private Map<String, Integer> setDayAndM(Map<String, List<List<Date>>> dayAndStart_end) {
        Map<String, Integer> dayAndNum = new HashMap<>();
        Date startTime;
        Date endTime;
        String mS;
        String mE;
        String ymdhms;
        List<List<Date>> dateList;
        List<String> minuteNums;
        List<String> sumNum;
        for (String day : dayAndStart_end.keySet()) {
            dateList = dayAndStart_end.get(day);
            minuteNums = new ArrayList<>();
            for (int i = 0; i < dateList.size(); i++) {
                List<Date> dates = dateList.get(i);
                startTime = dates.get(0);
                endTime = dates.get(1);
                mS = DataFormatUtil.getDateYMDHM(startTime);
                mE = DataFormatUtil.getDateYMDHM(endTime);
                ymdhms = DataFormatUtil.getDateYMDHMS(endTime);
                sumNum = DataFormatUtil.getYMDHM2Between(mS, mE);
                if (sumNum.size() == 0) {
                    sumNum.add(mE);
                }
                if (ymdhms.contains(":59:59")) {
                    sumNum.add(mE + ":59");
                }
                minuteNums.addAll(sumNum);
            }
            minuteNums = minuteNums.stream().distinct().collect(Collectors.toList());
            dayAndNum.put(day, minuteNums.size());
        }
        return dayAndNum;
    }

    private Map<String, Integer> setHourAndM(Map<String, List<List<Date>>> dayAndStart_end) {
        Map<String, Integer> hourAndNum = new HashMap<>();
        Date startTime;
        Date endTime;
        String mS;
        String mE;
        String ymdhms;
        List<List<Date>> dateList;
        List<String> minuteNums = new ArrayList<>();
        List<String> sumNum;
        for (String day : dayAndStart_end.keySet()) {
            dateList = dayAndStart_end.get(day);
            for (int i = 0; i < dateList.size(); i++) {
                List<Date> dates = dateList.get(i);
                startTime = dates.get(0);
                endTime = dates.get(1);
                mS = DataFormatUtil.getDateYMDHM(startTime);
                mE = DataFormatUtil.getDateYMDHM(endTime);
                ymdhms = DataFormatUtil.getDateYMDHMS(endTime);
                sumNum = DataFormatUtil.getYMDHM2Between(mS, mE);
                if (sumNum.size() == 0) {
                    sumNum.add(mE);
                }
                if (ymdhms.contains(":59:59")) {
                    sumNum.add(mE + ":59");
                }
                minuteNums.addAll(sumNum);
            }
            minuteNums = minuteNums.stream().distinct().collect(Collectors.toList());
        }
        if (minuteNums.size() > 0) {
            String ymdh;
            for (String minute : minuteNums) {
                ymdh = DataFormatUtil.FormatDateOneToOther(minute, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH");
                if (hourAndNum.containsKey(ymdh)) {
                    hourAndNum.put(ymdh, hourAndNum.get(ymdh) + 1);
                } else {
                    hourAndNum.put(ymdh, 1);
                }
            }
        }
        return hourAndNum;
    }

    private void setDayAndSE(int SDay, Date startT, Date endT, Map<String, List<List<Date>>> dayAndStart_end) {

        List<List<Date>> S_EList;
        String dayKey = DataFormatUtil.getDateYMD(startT);
        if (dayAndStart_end.containsKey(dayKey)) {
            S_EList = dayAndStart_end.get(dayKey);
        } else {
            S_EList = new ArrayList<>();
        }
        S_EList.add(Arrays.asList(startT, endT));
        dayAndStart_end.put(dayKey, S_EList);

    }

    private void setDayMapToNum(String pollutantCode, Date ymdD, Date startT, Date endT,
                                Map<String, Map<String, Long>> dayAndCodeAndMinute) {
        long minuteNum = DataFormatUtil.getDateMinutes(startT, endT) == 0 ? 1 : DataFormatUtil.getDateMinutes(startT, endT);
        String ymd = DataFormatUtil.getDateYMD(ymdD);
        Map<String, Long> codeAndMinute;
        if (dayAndCodeAndMinute.containsKey(ymd)) {
            codeAndMinute = dayAndCodeAndMinute.get(ymd);
        } else {
            codeAndMinute = new HashMap<>();
        }
        if (codeAndMinute.containsKey(pollutantCode)) {
            codeAndMinute.put(pollutantCode, codeAndMinute.get(pollutantCode) + minuteNum);
        } else {
            codeAndMinute.put(pollutantCode, minuteNum);
        }
        dayAndCodeAndMinute.put(ymd, codeAndMinute);
    }

    /**
     * @author: xsm
     * @date: 2022/07/07 0007 下午 14:54
     * @Description: 通过自定义参数获取报警污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getOnePointAlarmPollutantDataByParamForApp", method = RequestMethod.POST)
    public Object getOnePointAlarmPollutantDataByParamForApp(@RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "dgimn") String dgimn,
                                                             @RequestJson(value = "monitorpointtype") Integer monitorpointtype
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            //获取该时间段的超标报警污染物
            List<Map> maps = new ArrayList<>();
            maps = onlineCountAlarmService.getOnePointAlarmPollutantDataByParamForApp(dgimn, startDate, endDate, monitorpointtype);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
