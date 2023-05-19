package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.pubcode.PollutantController;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.*;
import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionInfoVO;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueService;
import com.tjpu.sp.service.environmentalprotection.limitproduction.LimitProductionInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationStandardService;
import com.tjpu.sp.service.environmentalprotection.online.*;
import com.tjpu.sp.service.environmentalprotection.pollutantvaluescope.PollutantValueScopeService;
import com.tjpu.sp.service.environmentalprotection.stopproductioninfo.StopProductionInfoService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.DevOpsTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum;
import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:11
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("online")
public class OnlineController {

    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private EntPermittedFlowLimitValueService entPermittedFlowLimitValueService;
    @Autowired
    private LimitProductionInfoService limitProductionInfoService;
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private DevOpsTaskDisposeService devOpsTaskDisposeService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private OnlineOriginalPacketService onlineOriginalPacketService;
    @Autowired
    private PubCodeService pubCodeService;
    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    @Autowired
    private WaterOutPutPollutantSetService waterOutPutPollutantSetService;
    @Autowired
    private EffectiveTransmissionService effectiveTransmissionService;
    @Autowired
    private PollutantController pollutantController;
    @Autowired
    private StopProductionInfoService stopProductionInfoService;
    @Autowired
    private MonitorControlService monitorControlService;
    @Autowired
    private SoilPointService soilPointService;
    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;
    @Autowired
    private PollutantValueScopeService pollutantValueScopeService;
    @Autowired
    private OnlineMonitorService onlineMonitorService;
    @Autowired
    private NavigationStandardService navigationStandardService;



    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;


    /**
     * @author: chengzq
     * @date: 2019/5/21 0021 下午 7:06
     * @Description: 获取不同类型设备的统计信息（包含个数，单位，名称，编码等信息）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getEquipmentsStatisInfo", method = RequestMethod.GET)
    public Object getEquipmentsStatisInfo() {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String[] equipmentOrder = DataFormatUtil.parseProperties("equipmentOrder").split(",");
            for (String s : equipmentOrder) {
                paramMap.put(s.toLowerCase() + "name", DataFormatUtil.parseProperties(s + ".name"));
            }
            List<Map<String, Object>> list = airMonitorStationService.countOnlineOutPut(paramMap);


            dataMap.put("data", list);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 13:38
     * @Description: 实时数据一览查询条件
     * @param:
     * @return:
     */
    @RequestMapping(value = "getQueryDataForRealTime", method = RequestMethod.POST)
    public Object getQueryDataForRealTime(@RequestJson(value = "type") Integer type) {
        try {
            Map<String, Object> queryDataForRealTime = onlineService.getQueryDataForRealTime(type);
            return AuthUtil.parseJsonKeyToLower("success", queryDataForRealTime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/30 0030 上午 11:24
     * @Description: 根据mn和监测点类型获取最新一条实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getLastRealTimeDataByMNAndMonitorPointType", method = RequestMethod.POST)
    public Object getLastRealTimeDataByMNAndMonitorPointType(@RequestJson(value = "dgimn") String dgimn,
                                                             @RequestJson(value = "monitorpointid") String monitorpointid,
                                                             @RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", monitorpointid);
            paramMap.put("outputids", Arrays.asList(monitorpointid));
            paramMap.put("pollutanttype", monitorpointtype);
            Set<Map<String, Object>> pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
            if (pollutantSetData.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                for (Map<String, Object> map : pollutantSetData) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                }
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getLatestRealTimeDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    String YMD = DataFormatUtil.getDateYMD(document.getDate("maxtime"));
                    Map<String, Object> codeAndAlarmType = getPollutantAlarmType(dgimn, pollutantcodes, YMD);
                    String maxtime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    resultMap.put("updatetime", maxtime);
                    paramMap.put("starttime", maxtime);
                    paramMap.put("endtime", maxtime);
                    paramMap.put("collection", "RealTimeData");
                    List<Document> lastRealTimeData = onlineService.getMonitorDataByParamMap(paramMap);
                    document = lastRealTimeData.get(0);
                    List<Map<String, Object>> pollutants = (List<Map<String, Object>>) document.get("RealDataList");
                    Map<String, Object> codeAndValue = new HashMap<>();
                    for (Map<String, Object> map : pollutants) {
                        codeAndValue.put(map.get("PollutantCode").toString(), map.get("MonitorValue"));
                    }
                    List<Map<String, Object>> datalist = new ArrayList<>();
                    Set<String> codes = new HashSet<>();
                    for (Map<String, Object> map : pollutantSetData) {
                        if (!codes.contains(map.get("pollutantcode"))) {
                            map.put("monitorvalue", codeAndValue.get(map.get("pollutantcode")));
                            map.put("alarmtype", codeAndAlarmType.get(map.get("pollutantcode")));
                            datalist.add(map);
                            codes.add(map.get("pollutantcode").toString());
                        }
                    }
                    resultMap.put("datalist", datalist);
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
     * @date: 2019/11/21 0021 下午 4:39
     * @Description: 获取污染物报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getPollutantAlarmType(String dgimn, List<String> pollutantcodes, String ymd) {

        Map<String, Object> codeAndPollutantAlarmType = new HashMap<>();

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("starttime", ymd + " 00:00:00");
        paramMap.put("endtime", ymd + " 23:59:59");
        paramMap.put("pollutantcodes", pollutantcodes);

        paramMap.put("mns", Arrays.asList(dgimn));
        paramMap.put("collection", "HourData");
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("unwindkey", "HourDataList");
        List<Document> conChanges = countPollutantDataByParam(paramMap);
        String pollutantCode;
        if (conChanges.size() > 0) {
            for (Document conChange : conChanges) {
                pollutantCode = conChange.getString("PollutantCode");
                codeAndPollutantAlarmType.put(pollutantCode, "earlydata");
            }
        }
        paramMap.put("collection", "HourFlowData");
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("unwindkey", "HourFlowDataList");
        List<Document> flowChanges = countPollutantDataByParam(paramMap);
        if (flowChanges.size() > 0) {
            for (Document flowChange : flowChanges) {
                pollutantCode = flowChange.getString("PollutantCode");
                if (!"alarmdata".equals(codeAndPollutantAlarmType.get(pollutantCode))) {
                    codeAndPollutantAlarmType.putIfAbsent(pollutantCode, "earlydata");
                }

            }
        }
        paramMap.remove("unwindkey");
        paramMap.put("collection", "OverData");
        paramMap.put("timeKey", "OverTime");
        List<Document> overDatas = countPollutantDataByParam(paramMap);
        if (overDatas.size() > 0) {
            for (Document overData : overDatas) {
                pollutantCode = overData.getString("PollutantCode");
                codeAndPollutantAlarmType.putIfAbsent(pollutantCode, "alarmdata");
            }
        }
        paramMap.put("collection", "EarlyWarnData");
        paramMap.put("timeKey", "EarlyWarnTime");
        List<Document> cyzDatas = countPollutantDataByParam(paramMap);
        if (cyzDatas.size() > 0) {
            for (Document cyzData : cyzDatas) {
                pollutantCode = cyzData.getString("PollutantCode");
                if (!"alarmdata".equals(codeAndPollutantAlarmType.get(pollutantCode))) {
                    codeAndPollutantAlarmType.putIfAbsent(pollutantCode, "earlydata");
                }
            }
        }
        paramMap.put("collection", "ExceptionData");
        paramMap.put("timeKey", "ExceptionTime");
        List<Document> exceptionDatas = countPollutantDataByParam(paramMap);
        if (exceptionDatas.size() > 0) {
            for (Document exceptionData : exceptionDatas) {
                pollutantCode = exceptionData.getString("PollutantCode");
                codeAndPollutantAlarmType.putIfAbsent(pollutantCode, "alarmdata");
            }
        }
        return codeAndPollutantAlarmType;
    }

    private List<Document> countPollutantDataByParam(Map<String, Object> paramMap) {
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
            fields = fields("DataGatherCode").and("PollutantCode", unwindkey + ".PollutantCode");
        } else {
            fields = fields("DataGatherCode", "PollutantCode");
        }
        aggregations.add(project(fields));
        GroupOperation groupOperation = group("DataGatherCode", "PollutantCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = Aggregation.newAggregation(aggregations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;

    }

    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取单个点位月排放量列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneOutPutMonthFlowListPageDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getOneOutPutMonthFlowListPageDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {

            Map<String, Object> titleMap = new HashMap<>();
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 1);
            //点位类型：1-排口，2-监测点
            if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                titleMap.put("pointType", 1);
            } else {
                titleMap.put("pointType", 2);
            }

            titleMap.put("pollutantType", pollutanttype);
            //是否显示排放量
            titleMap.put("isShowFlow", "show");
            //排口集合
            List<String> outputIds = new ArrayList<>();
            outputIds.add(outputid);
            titleMap.put("outputids", outputIds);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = getMNsByOutPutids(outputIds, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "MonthFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

    /**
     * @author: lip
     * @date: 2019/8/7 0007 下午 3:04
     * @Description: 根据监测时间分组统计点位监测值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getManyPointDataByParams", method = RequestMethod.POST)
    public Object getManyPointDataByParams(
            @RequestJson(value = "timetype") Integer timetype,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();

            String collection = MongoDataUtils.getCollectionByDataMark(timetype);

            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(timetype, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(timetype, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", collection);
            paramMap.put("mns", dgimns);
            List<Document> monitorData = onlineService.getMonitorDataByParamMap(paramMap);
            //数据组装
            if (monitorData.size() > 0) {
                List<Map<String, Object>> dataList = MongoDataUtils.setManyMNData(dgimns, monitorData, collection, pollutantcode);
                if (pagesize != null && pagenum != null) {
                    Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                    dataList = dataList.stream().sorted(comparebynum).collect(Collectors.toList());
                    List<Map<String, Object>> collecta = dataList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                    dataMap.put("datalist", collecta);
                    dataMap.put("total", dataList.size());
                } else {
                    dataMap.put("datalist", dataList);
                }
            } else {
                dataMap.put("datalist", new ArrayList<>());
                dataMap.put("total", 0);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/8 0008 下午 1:59
     * @Description: 根据监测时间分组统计点位监测值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyPointChartDataByParams", method = RequestMethod.POST)
    public Object getManyPointChartDataByParams(
            @RequestJson(value = "timetype") Integer timetype,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime

    ) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();

            String collection = MongoDataUtils.getCollectionByDataMark(timetype);

            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(timetype, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(timetype, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", collection);
            paramMap.put("mns", dgimns);
            paramMap.put("sort", "asc");
            List<Document> monitorData = onlineService.getMonitorDataByParamMap(paramMap);

            if (monitorData.size() > 0) {
                dataList = MongoDataUtils.setManyMNChartData(dgimns, monitorData, collection, pollutantcode);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取单个点位月排放量列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneOutPutMonthFlowListDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getOneOutPutMonthFlowListDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            Integer reportType = 1;
            //点位类型：1-排口，2-监测点
            Integer pointType = 1;
            if (!CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                pointType = 2;
            }

            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "MonthFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }

            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("reportType", reportType);
            paramMap.put("pointType", pointType);
            paramMap.put("pollutantType", pollutanttype);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }


    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取单个点位年排放量列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneOutPutYearFlowListPageDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getOneOutPutYearFlowListPageDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {

            Map<String, Object> titleMap = new HashMap<>();

            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 1);
            //点位类型：1-排口，2-监测点

            if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                titleMap.put("pointType", 1);
            } else {
                titleMap.put("pointType", 2);
            }
            titleMap.put("pollutantType", pollutanttype);
            //是否显示排放量
            titleMap.put("isShowFlow", "show");
            //排口集合
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "YearFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }


    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取单个点位年排放量列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneOutPutYearFlowListDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getOneOutPutYearFlowListDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            Integer reportType = 1;
            //点位类型：1-排口，2-监测点
            Integer pointType = 1;

            if (!CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                pointType = 2;
            }

            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "YearFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("endtime", endtime);
            }

            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("reportType", reportType);
            paramMap.put("pointType", pointType);
            paramMap.put("pollutantType", pollutanttype);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取多个点位月排放量列表页面数据
     * @updateUser:xsm
     * @updateDate:2022/02/09 0009 下午 1:22
     * @updateDescription:支持废气、烟气合并 传多个类型[2,22]
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutMonthFlowListPageDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getManyOutPutMonthFlowListPageDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype",required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes",required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, Object> titleMap = new HashMap<>();
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 2);
            //点位类型：1-排口，2-监测点
            if (pollutanttypes ==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            if(pollutanttypes.size()>0){
                List<String> mns = new ArrayList<>();
                List<Map<String, Object>> tabletitledata = new ArrayList<>();
                //是否显示排放量
                titleMap.put("isShowFlow", "show");
                //排口集合
                titleMap.put("outputids", outputids);
                if (pollutanttypes.size()==1){//单个类型
                    if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttypes.get(0))) {
                        titleMap.put("pointType", 1);
                    } else {
                        titleMap.put("pointType", 2);
                    }
                    titleMap.put("pollutantType", pollutanttype);
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                    mns = getMNsByOutPutids(outputids, pollutanttype);
                }else{//  废气、烟气合并 传多个类型时
                    for (Integer i:pollutanttypes){
                        mns.addAll(getMNsByOutPutids(outputids, i));
                    }
                    titleMap.put("pollutantType", pollutanttypes.get(0));
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                }
                Map<String, Object> paramMap = new HashMap<>();
                String collection = "MonthFlowData";
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + "-01 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.putAll(titleMap);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                //
                List<Map<String, Object>> tableListData = new ArrayList<>();
                if (pollutanttypes.size()==1){//单个类型
                    tableListData = onlineService.getTableListDataForReport(paramMap);
                }else{//废气、烟气合并 传多个类型时
                    paramMap.put("pollutantType",SmokeEnum.getCode());
                    tableListData = onlineService.getTableListDataForReport(paramMap);
                }
                Map<String, Object> tabledata = new HashMap<>();
                tabledata.put("tabletitledata", tabletitledata);
                tabledata.put("tablelistdata", tableListData);
                dataMap.put("tabledata", tabledata);
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }


    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取多个点位月排放量列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutMonthFlowListDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getManyOutPutMonthFlowListDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            Integer reportType = 2;
            //点位类型：1-排口，2-监测点
            Integer pointType = 1;

            if (!CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                pointType = 2;
            }

            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "MonthFlowData";

            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }

            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("reportType", reportType);
            paramMap.put("pointType", pointType);
            paramMap.put("pollutantType", pollutanttype);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

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
    @RequestMapping(value = "getOutPutAndPollutantsByPollutionID", method = RequestMethod.POST)
    public Object getOutPutAndPollutantsByPollutionID(@RequestJson(value = "pollutionid") String pollutionid) {
        try {
            List<CommonTypeEnum.MonitorPointTypeEnum> enums = new ArrayList<>();
            enums.add(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum);
            enums.add(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum);
            enums.add(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum);
            enums.add(CommonTypeEnum.MonitorPointTypeEnum.RainEnum);
            return AuthUtil.parseJsonKeyToLower("success", onlineService.getOutPutAndPollutantsByPollutionID(pollutionid, enums));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取多个点位年排放量列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutYearFlowListPageDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getManyOutPutYearFlowListPageDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype", required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes", required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {

            Map<String, Object> titleMap = new HashMap<>();
            if (pollutanttypes ==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 2);
            if(pollutanttypes.size()>0){
                List<String> mns = new ArrayList<>();
                List<Map<String, Object>> tabletitledata = new ArrayList<>();
                //是否显示排放量
                titleMap.put("isShowFlow", "show");
                //排口集合
                titleMap.put("outputids", outputids);
                if (pollutanttypes.size()==1){//单个类型
                    if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttypes.get(0))) {
                        titleMap.put("pointType", 1);
                    } else {
                        titleMap.put("pointType", 2);
                    }
                    titleMap.put("pollutantType", pollutanttype);
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                    mns = getMNsByOutPutids(outputids, pollutanttype);
                }else{//  废气、烟气合并 传多个类型时
                    for (Integer i:pollutanttypes){
                        mns.addAll(getMNsByOutPutids(outputids, i));
                    }
                    titleMap.put("pollutantType", pollutanttypes.get(0));
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                }
                Map<String, Object> paramMap = new HashMap<>();
                String collection = "YearFlowData";
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + "-01-01 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.putAll(titleMap);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                //
                List<Map<String, Object>> tableListData = new ArrayList<>();
                if (pollutanttypes.size()==1){//单个类型
                    tableListData = onlineService.getTableListDataForReport(paramMap);
                }else{//废气、烟气合并 传多个类型时
                    paramMap.put("pollutantType",SmokeEnum.getCode());
                    tableListData = onlineService.getTableListDataForReport(paramMap);
                }
                Map<String, Object> tabledata = new HashMap<>();
                tabledata.put("tabletitledata", tabletitledata);
                tabledata.put("tablelistdata", tableListData);
                dataMap.put("tabledata", tabledata);
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }


    /**
     * @author: lip
     * @date: 2019/6/10 0010 下午 1:32
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取多个点位年排放量列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutYearFlowListDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getManyOutPutYearFlowListDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            Integer reportType = 2;
            //点位类型：1-排口，2-监测点
            Integer pointType = 1;

            if (!CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                pointType = 2;
            }
            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "YearFlowData";

            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("reportType", reportType);
            paramMap.put("pointType", pointType);
            paramMap.put("pollutantType", pollutanttype);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:17
     * @Description: 通过排口数组获取mn号数组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getMNsByOutPutids(List<String> outputids, Integer pollutanttype) {
        List<String> mns = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> onlineOutPuts = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutanttype)) {
            case WasteWaterEnum://废水
                paramMap.put("outputids", outputids);
                paramMap.put("outputtype", "water");
                onlineOutPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                break;
            case WasteGasEnum://废气
            case SmokeEnum://烟气
                paramMap.put("outputids", outputids);
                onlineOutPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                break;
            case RainEnum://雨水
                paramMap.put("outputids", outputids);
                paramMap.put("outputtype", "rain");
                onlineOutPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                break;
            case AirEnum://空气
                paramMap.put("airids", outputids);
                onlineOutPuts = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                break;
            case EnvironmentalStinkEnum://恶臭
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                paramMap.put("outputids", outputids);
                onlineOutPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
            case EnvironmentalVocEnum://voc
                paramMap.put("monitorPointType", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                paramMap.put("outputids", outputids);
                onlineOutPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
                break;
        }
        for (Map<String, Object> outPut : onlineOutPuts) {
            if (outPut.get("dgimn") != null) {
                mns.add(outPut.get("dgimn").toString());
            }
        }
        return mns;
    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数获取单个点位月排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneOutPutMonthFlowDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getOneOutPutMonthFlowDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, pollutanttype, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "MonthFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = MongoDataUtils.setOneOutPutManyPollutantsMonthYearDataList(documents, pollutantcodes, collection);
            //获取排口污染物的预警、超限、异常值
            Map<String, Object> standardmap = new HashMap<>();
            if (mns != null && mns.size() > 0 && pollutantcodes.size() > 0 && mns.get(0) != null && pollutantcodes.get(0) != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("dgimn", mns.get(0));
                param.put("pollutantcode", pollutantcodes.get(0));
                param.put("monitorpointtype", pollutanttype);
                standardmap = onlineService.getPollutantEarlyAlarmStandardDataByParamMap(param);
            }
            if (charDataList != null && charDataList.size() > 0) {
                charDataList.get(0).put("standard", standardmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }


    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数获取多个点位月排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutMonthFlowDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getManyOutPutMonthFlowDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype", required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes", required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            if (pollutanttypes==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if(pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            List<String> mns = new ArrayList<>();
            Map<String, String> idAndName = new HashMap<>();
            Map<String, String> codeAndName = new HashMap<>();
            for (Integer i:pollutanttypes){
                mns.addAll(onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, i, outPutIdAndMn));
                idAndName.putAll(onlineService.getOutPutIdAndPollution(outputids, i));
                codeAndName.putAll(onlineService.getPollutantCodeAndName(pollutantcodes, i));
            }
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "MonthFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = MongoDataUtils.setManyOutPutManyPollutantsMonthYearCharDataList(documents,
                    pollutantcodes,
                    collection,
                    outPutIdAndMn,
                    outputids,
                    idAndName,
                    codeAndName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数获取多个点位年排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyOutPutYearFlowDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getManyOutPutYearFlowDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype", required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes", required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            if (pollutanttypes==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            List<String> mns = new ArrayList<>();
            Map<String, String> idAndName = new HashMap<>();
            Map<String, String> codeAndName = new HashMap<>();
            for (Integer i:pollutanttypes){
                mns.addAll(onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, i, outPutIdAndMn));
                idAndName.putAll(onlineService.getOutPutIdAndPollution(outputids, i));
                codeAndName.putAll(onlineService.getPollutantCodeAndName(pollutantcodes, i));
            }
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "YearFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = MongoDataUtils.setManyOutPutManyPollutantsMonthYearCharDataList(documents,
                    pollutantcodes,
                    collection,
                    outPutIdAndMn,
                    outputids,
                    idAndName,
                    codeAndName
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数获取单个点位年排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getOneOutPutYearFlowDataByPollutantTypeAndParams", method = RequestMethod.POST)
    public Object getOneOutPutYearFlowDataByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, pollutanttype, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "YearFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = MongoDataUtils.setOneOutPutManyPollutantsMonthYearDataList(documents, pollutantcodes, collection);
            //获取排口污染物的预警、超限、异常值
            Map<String, Object> standardmap = new HashMap<>();
            if (mns != null && mns.size() > 0 && pollutantcodes.size() > 0 && mns.get(0) != null && pollutantcodes.get(0) != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("dgimn", mns.get(0));
                param.put("pollutantcode", pollutantcodes.get(0));
                param.put("monitorpointtype", pollutanttype);
                standardmap = onlineService.getPollutantEarlyAlarmStandardDataByParamMap(param);
            }
            if (charDataList != null && charDataList.size() > 0) {
                charDataList.get(0).put("standard", standardmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 自定义查询条件统计污染源停产小时排放量情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "countPollutionStopHourFlowByParams", method = RequestMethod.POST)
    public Object countPollutionStopHourFlowByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            List<LimitProductionInfoVO> limitProduction = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            List<String> pollutionids = new ArrayList<>();
            for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
                pollutionids.add(limitProductionInfoVO.getFkPollutionid());
            }
            List<Map<String, Object>> outPuts = new ArrayList<>();
            //根据监测点类型，获取排口信息，企业的限产信息
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    paramMap.clear();
                    break;
                case WasteGasEnum:
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
            }
            List<String> mns = new ArrayList<>();
            Map<String, List<String>> pollutionAndOutPut = new HashMap<>();
            setMnsAndPollutionOutPut(mns, pollutionAndOutPut, outPuts);
            paramMap.put("mns", mns);
            String collection = "HourFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getHourFlowMonitorDataByParamMap(paramMap);
            countStopProductionNum(charDataList, documents, pollutionAndOutPut, limitProduction, collection);
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 自定义查询条件统计污染源停产日排放量情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "countPollutionStopDayFlowByParams", method = RequestMethod.POST)
    public Object countPollutionStopDayFlowByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            List<LimitProductionInfoVO> limitProduction = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            List<String> pollutionids = new ArrayList<>();
            for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
                pollutionids.add(limitProductionInfoVO.getFkPollutionid());
            }
            List<Map<String, Object>> outPuts = new ArrayList<>();
            //根据监测点类型，获取排口信息，企业的限产信息
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    paramMap.clear();
                    break;
                case WasteGasEnum:
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
            }
            List<String> mns = new ArrayList<>();
            Map<String, List<String>> pollutionAndOutPut = new HashMap<>();
            setMnsAndPollutionOutPut(mns, pollutionAndOutPut, outPuts);
            paramMap.put("mns", mns);
            String collection = "DayFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
            countStopProductionNum(charDataList, documents, pollutionAndOutPut, limitProduction, collection);
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 自定义查询条件统计污染源限制日排放量情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "countPollutionLimitDayFlowByParams", method = RequestMethod.POST)
    public Object countPollutionLimitDayFlowByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            List<LimitProductionInfoVO> limitProduction = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            List<String> pollutionids = new ArrayList<>();
            for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
                pollutionids.add(limitProductionInfoVO.getFkPollutionid());
            }
            List<Map<String, Object>> outPuts = new ArrayList<>();
            //根据监测点类型，获取排口信息，企业的限产信息
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    paramMap.clear();
                    break;
                case WasteGasEnum:
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
            }
            List<String> mns = new ArrayList<>();
            Map<String, List<String>> pollutionAndOutPut = new HashMap<>();
            setMnsAndPollutionOutPut(mns, pollutionAndOutPut, outPuts);
            paramMap.put("mns", mns);
            String collection = "DayFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
            countLimitProductionNum(charDataList, documents, pollutionAndOutPut, limitProduction, collection);
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private void countLimitProductionNum(List<Map<String, Object>> charDataList, List<Document> documents, Map<String, List<String>> pollutionAndOutPut, List<LimitProductionInfoVO> limitProduction, String collection) {
        Date dateTimeTemp;
        Date dateTemp;
        Date startTime;
        Date endTime;
        Double totalFlow;
        Double benchmarkFlow;
        Map<String, Integer> mnAndNum = new HashMap<>();
        Map<String, String> idAndName = new HashMap<>();
        String mn = "";
        List<String> mns;
        for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
            startTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecutestarttime());
            endTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecuteendtime());
            mns = pollutionAndOutPut.get(limitProductionInfoVO.getFkPollutionid());
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                if (mns != null && mns.contains(mn)) {
                    dateTimeTemp = document.getDate("MonitorTime");
                    dateTemp = DataFormatUtil.parseDateYMD(dateTimeTemp);
                    if (DataFormatUtil.belongCalendar(dateTemp, startTime, endTime)) {
                        if ("DayFlowData".equals(collection)) {
                            totalFlow = document.get("TotalFlow") != null ? Double.parseDouble(document.get("TotalFlow").toString()) : 0d;
                            benchmarkFlow = limitProductionInfoVO.getBenchmarkflow() != null ? limitProductionInfoVO.getBenchmarkflow() : 0d;
                            if (benchmarkFlow <= totalFlow) {
                                mn = document.getString("DataGatherCode");
                                if (mnAndNum.get(mn) != null) {
                                    mnAndNum.put(mn, mnAndNum.get(mn) + 1);
                                } else {
                                    mnAndNum.put(mn, 1);
                                }
                            }
                        }

                    }
                }
            }
            idAndName.put(limitProductionInfoVO.getFkPollutionid(), limitProductionInfoVO.getPollutionname());
        }
        if (mnAndNum.size() > 0) {
            String pollutionid = "";
            Map<String, Integer> pollutonidAndNum = new HashMap<>();

            for (String mnKey : mnAndNum.keySet()) {
                pollutionid = getPollutionId(pollutionAndOutPut, mnKey);
                if (!pollutionid.equals("")) {
                    if (pollutonidAndNum.get(pollutionid) != null) {
                        pollutonidAndNum.put(pollutionid, pollutonidAndNum.get(pollutionid) + mnAndNum.get(mnKey));
                    } else {
                        pollutonidAndNum.put(pollutionid, mnAndNum.get(mnKey));
                    }
                }
            }
            for (String pollutionId : pollutonidAndNum.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollutionid", pollutionId);
                map.put("pollutionname", idAndName.get(pollutionId));
                map.put("nolimitnum", pollutonidAndNum.get(pollutionId));
                charDataList.add(map);
            }
            Collections.sort(charDataList, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer s1 = (Integer) o1.get("nolimitnum");
                    Integer s2 = (Integer) o2.get("nolimitnum");
                    if (s1 > s2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 自定义查询条件获取污染源限制小时排放量详细情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getPollutionStopHourFlowDetailByParams", method = RequestMethod.POST)
    public Object getPollutionLimitHourFlowDetailByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            List<LimitProductionInfoVO> limitProduction = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            List<String> pollutionids = new ArrayList<>();
            for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
                pollutionids.add(limitProductionInfoVO.getFkPollutionid());
            }
            List<Map<String, Object>> outPuts = new ArrayList<>();
            //根据监测点类型，获取排口信息，企业的限产信息
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    paramMap.clear();
                    break;
                case WasteGasEnum:
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
            }
            List<String> mns = new ArrayList<>();
            Map<String, List<String>> pollutionAndOutPut = new HashMap<>();
            setMnsAndPollutionOutPut(mns, pollutionAndOutPut, outPuts);
            paramMap.put("mns", mns);
            String collection = "HourFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("sort", "desc");
            List<Document> documents = onlineService.getHourFlowMonitorDataByParamMap(paramMap);
            getStopProductionNum(dataList, documents, pollutionAndOutPut, limitProduction, collection);
            //处理分页数据
            if (pagenum != null && pagesize != null) {
                dataMap.put("total", dataList.size());
                dataList = getPageData(dataList, pagenum, pagesize);
                dataMap.put("datalist", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 自定义查询条件获取污染源停产日排放量详细情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getPollutionStopDayFlowDetailByParams", method = RequestMethod.POST)
    public Object getPollutionStopDayFlowDetailByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            List<LimitProductionInfoVO> limitProduction = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            List<String> pollutionids = new ArrayList<>();
            for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
                pollutionids.add(limitProductionInfoVO.getFkPollutionid());
            }
            List<Map<String, Object>> outPuts = new ArrayList<>();
            //根据监测点类型，获取排口信息，企业的限产信息
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    paramMap.clear();
                    break;
                case WasteGasEnum:
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
            }
            List<String> mns = new ArrayList<>();
            Map<String, List<String>> pollutionAndOutPut = new HashMap<>();
            setMnsAndPollutionOutPut(mns, pollutionAndOutPut, outPuts);
            paramMap.put("mns", mns);
            String collection = "DayFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("sort", "desc");
            List<Document> documents = onlineService.getHourFlowMonitorDataByParamMap(paramMap);
            getStopProductionNum(dataList, documents, pollutionAndOutPut, limitProduction, collection);
            //处理分页数据
            if (pagenum != null && pagesize != null) {
                dataMap.put("total", dataList.size());
                dataList = getPageData(dataList, pagenum, pagesize);
                dataMap.put("datalist", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/6/11 0011 上午 9:56
     * @Description: 自定义查询条件获取污染源限制日排放量详细情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getPollutionLimitDayFlowDetailByParams", method = RequestMethod.POST)
    public Object getPollutionLimitDayFlowDetailByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            List<LimitProductionInfoVO> limitProduction = limitProductionInfoService.getLimitProductionInfoByParamMap(paramMap);
            List<String> pollutionids = new ArrayList<>();
            for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
                pollutionids.add(limitProductionInfoVO.getFkPollutionid());
            }
            List<Map<String, Object>> outPuts = new ArrayList<>();
            //根据监测点类型，获取排口信息，企业的限产信息
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case WasteWaterEnum:
                    paramMap.put("outputtype", "water");
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
                    paramMap.clear();
                    break;
                case WasteGasEnum:
                    paramMap.put("pollutionids", pollutionids);
                    outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                    break;
            }
            List<String> mns = new ArrayList<>();
            Map<String, List<String>> pollutionAndOutPut = new HashMap<>();
            setMnsAndPollutionOutPut(mns, pollutionAndOutPut, outPuts);
            paramMap.put("mns", mns);
            String collection = "DayFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("sort", "desc");
            List<Document> documents = onlineService.getHourFlowMonitorDataByParamMap(paramMap);
            getLimitProductionNum(dataList, documents, pollutionAndOutPut, limitProduction, collection);
            //处理分页数据
            if (pagenum != null && pagesize != null) {
                dataMap.put("total", dataList.size());
                dataList = getPageData(dataList, pagenum, pagesize);
                dataMap.put("datalist", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
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
     * @date: 2019/6/25 0025 下午 6:58
     * @Description: 组装停产列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getStopProductionNum(List<Map<String, Object>> dataList, List<Document> documents, Map<String, List<String>> pollutionAndOutPut, List<LimitProductionInfoVO> limitProduction, String collection) {
        Date dateTimeTemp;
        Date dateTemp;
        String dateHTemp;
        String dateTempString;
        Date startTime;
        Date endTime;
        int hours;
        int minHour;
        int maxHour;
        Double totalFlow = 0d;
        List<String> mns;
        String mn;
        for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
            mns = pollutionAndOutPut.get(limitProductionInfoVO.getFkPollutionid());
            startTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecutestarttime());
            endTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecuteendtime());
            Map<String, Double> timeAndTotalFlow = new LinkedHashMap<>();
            for (Document document : documents) {
                mn = document.get("DataGatherCode") != null ? document.get("DataGatherCode").toString() : "";
                if (mns != null && mns.contains(mn)) {
                    dateTimeTemp = document.getDate("MonitorTime");
                    dateTemp = DataFormatUtil.parseDateYMD(dateTimeTemp);

                    if (DataFormatUtil.belongCalendar(dateTemp, startTime, endTime)) {
                        totalFlow = document.get("TotalFlow") != null ? Double.parseDouble(document.get("TotalFlow").toString()) : 0d;
                        if ("HourFlowData".equals(collection)) {
                            dateHTemp = DataFormatUtil.getDateYMDH(dateTimeTemp);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dateTimeTemp);
                            hours = calendar.get(Calendar.HOUR_OF_DAY);
                            minHour = limitProductionInfoVO.getStaggeringpeakstarttimepoint();
                            maxHour = limitProductionInfoVO.getStaggeringpeakendtimepoint();
                            if (hours >= minHour && hours <= maxHour) {//符合条件的集合
                                if (timeAndTotalFlow.get(dateHTemp) != null) {
                                    timeAndTotalFlow.put(dateHTemp, timeAndTotalFlow.get(dateHTemp) + totalFlow);
                                } else {
                                    timeAndTotalFlow.put(dateHTemp, totalFlow);
                                }
                            }
                        } else if ("DayFlowData".equals(collection)) {
                            dateTempString = DataFormatUtil.getDateYMD(dateTimeTemp);
                            if (timeAndTotalFlow.get(dateTemp) != null) {
                                timeAndTotalFlow.put(dateTempString, timeAndTotalFlow.get(dateTempString) + totalFlow);
                            } else {
                                timeAndTotalFlow.put(dateTempString, totalFlow);
                            }
                        }


                    }
                }
            }
            if (timeAndTotalFlow.size() > 0) {
                for (String dataHTemp : timeAndTotalFlow.keySet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pollutionid", limitProductionInfoVO.getFkPollutionid());
                    map.put("pollutionname", limitProductionInfoVO.getPollutionname());
                    if ("HourFlowData".equals(collection)) {
                        map.put("nolimittime", dataHTemp + "时");
                        map.put("requiretime", getHourRequireTime(limitProductionInfoVO));
                    } else if ("DayFlowData".equals(collection)) {
                        map.put("nolimittime", dataHTemp);
                        map.put("requiretime", getDayRequireTime(limitProductionInfoVO));
                    }
                    map.put("totalflow", timeAndTotalFlow.get(dataHTemp));
                    dataList.add(map);
                }
            }
        }


    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 6:58
     * @Description: 组装限产列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getLimitProductionNum(List<Map<String, Object>> dataList, List<Document> documents, Map<String, List<String>> pollutionAndOutPut, List<LimitProductionInfoVO> limitProduction, String collection) {
        Date dateTimeTemp;
        Date dateTemp;
        String dateTempString;
        Date startTime;
        Date endTime;
        Double totalFlow = 0d;
        Double benchmarkFlow = 0d;
        List<String> mns;
        String mn;
        for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
            mns = pollutionAndOutPut.get(limitProductionInfoVO.getFkPollutionid());
            startTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecutestarttime());
            endTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecuteendtime());
            Map<String, Double> timeAndTotalFlow = new LinkedHashMap<>();
            for (Document document : documents) {
                mn = document.get("DataGatherCode") != null ? document.get("DataGatherCode").toString() : "";
                if (mns != null && mns.contains(mn)) {
                    dateTimeTemp = document.getDate("MonitorTime");
                    dateTemp = DataFormatUtil.parseDateYMD(dateTimeTemp);
                    if (DataFormatUtil.belongCalendar(dateTemp, startTime, endTime)) {
                        if ("DayFlowData".equals(collection)) {
                            totalFlow = document.get("TotalFlow") != null ? Double.parseDouble(document.get("TotalFlow").toString()) : 0d;
                            benchmarkFlow = limitProductionInfoVO.getBenchmarkflow() != null ? limitProductionInfoVO.getBenchmarkflow() : 0d;
                            if (benchmarkFlow <= totalFlow) {
                                dateTempString = DataFormatUtil.getDateYMD(dateTimeTemp);
                                if (timeAndTotalFlow.get(dateTempString) != null) {
                                    timeAndTotalFlow.put(dateTempString, timeAndTotalFlow.get(dateTempString) + totalFlow);
                                } else {
                                    timeAndTotalFlow.put(dateTempString, totalFlow);
                                }
                            }
                        }
                    }
                }
            }
            if (timeAndTotalFlow.size() > 0) {
                String percent;
                for (String dataHTemp : timeAndTotalFlow.keySet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pollutionid", limitProductionInfoVO.getFkPollutionid());
                    map.put("pollutionname", limitProductionInfoVO.getPollutionname());
                    if ("DayFlowData".equals(collection)) {
                        map.put("nolimittime", dataHTemp);
                        map.put("requiretime", getDayRequireTime(limitProductionInfoVO));
                    }
                    map.put("totalflow", timeAndTotalFlow.get(dataHTemp));
                    map.put("limitflow", limitProductionInfoVO.getBenchmarkflow());
                    if (timeAndTotalFlow.get(dataHTemp) != null && limitProductionInfoVO.getBenchmarkflow() != null && timeAndTotalFlow.get(dataHTemp) > 0) {
                        percent = DataFormatUtil.formatDoubleSaveOne(100 * limitProductionInfoVO.getBenchmarkflow() / timeAndTotalFlow.get(dataHTemp));
                        percent = DataFormatUtil.subZeroAndDot(percent) + "%";
                        map.put("limitpercent", percent);
                    } else {
                        map.put("limitpercent", "0");
                    }
                    dataList.add(map);
                }
            }
        }


    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 上午 8:57
     * @Description: 限产小时时间格式化
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getHourRequireTime(LimitProductionInfoVO limitProductionInfoVO) {
        if (limitProductionInfoVO.getStaggeringpeakstarttimepoint() != null && limitProductionInfoVO.getStaggeringpeakendtimepoint() != null) {
            return limitProductionInfoVO.getStaggeringpeakstarttimepoint() + "时-" + limitProductionInfoVO.getStaggeringpeakendtimepoint() + "时";
        } else {
            return null;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 上午 8:57
     * @Description: 限产日时间格式化
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getDayRequireTime(LimitProductionInfoVO limitProductionInfoVO) {
        if (limitProductionInfoVO.getExecutestarttime() != null && limitProductionInfoVO.getExecuteendtime() != null) {
            return limitProductionInfoVO.getExecutestarttime() + " 至 " + limitProductionInfoVO.getExecuteendtime();
        } else {
            return null;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 5:22
     * @Description: 统计各个企业的停产情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void countStopProductionNum(List<Map<String, Object>> charDataList, List<Document> documents, Map<String, List<String>> pollutionAndOutPut, List<LimitProductionInfoVO> limitProduction, String collection) {
        Date dateTimeTemp;
        Date dateTemp;
        Date startTime;
        Date endTime;
        int hours;
        int minHour;
        int maxHour;
        Map<String, Integer> mnAndNum = new HashMap<>();

        Map<String, String> idAndName = new HashMap<>();
        String mn = "";
        List<String> mns;
        for (LimitProductionInfoVO limitProductionInfoVO : limitProduction) {
            startTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecutestarttime());
            endTime = DataFormatUtil.parseDateYMD(limitProductionInfoVO.getExecuteendtime());
            mns = pollutionAndOutPut.get(limitProductionInfoVO.getFkPollutionid());
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                if (mns != null && mns.contains(mn)) {
                    dateTimeTemp = document.getDate("MonitorTime");
                    dateTemp = DataFormatUtil.parseDateYMD(dateTimeTemp);
                    if (DataFormatUtil.belongCalendar(dateTemp, startTime, endTime)) {
                        if ("HourFlowData".equals(collection)) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dateTimeTemp);
                            hours = calendar.get(Calendar.HOUR_OF_DAY);
                            minHour = limitProductionInfoVO.getStaggeringpeakstarttimepoint();
                            maxHour = limitProductionInfoVO.getStaggeringpeakendtimepoint();
                            if (hours >= minHour && hours <= maxHour) {
                                mn = document.getString("DataGatherCode");
                                if (mnAndNum.get(mn) != null) {
                                    mnAndNum.put(mn, mnAndNum.get(mn) + 1);
                                } else {
                                    mnAndNum.put(mn, 1);
                                }
                            }
                        } else if ("DayFlowData".equals(collection)) {
                            mn = document.getString("DataGatherCode");
                            if (mnAndNum.get(mn) != null) {
                                mnAndNum.put(mn, mnAndNum.get(mn) + 1);
                            } else {
                                mnAndNum.put(mn, 1);
                            }
                        }

                    }
                }
            }
            idAndName.put(limitProductionInfoVO.getFkPollutionid(), limitProductionInfoVO.getPollutionname());
        }
        if (mnAndNum.size() > 0) {
            String pollutionid = "";
            Map<String, Integer> pollutonidAndNum = new HashMap<>();

            for (String mnKey : mnAndNum.keySet()) {
                pollutionid = getPollutionId(pollutionAndOutPut, mnKey);
                if (!pollutionid.equals("")) {
                    if (pollutonidAndNum.get(pollutionid) != null) {
                        pollutonidAndNum.put(pollutionid, pollutonidAndNum.get(pollutionid) + mnAndNum.get(mnKey));
                    } else {
                        pollutonidAndNum.put(pollutionid, mnAndNum.get(mnKey));
                    }
                }
            }
            for (String pollutionId : pollutonidAndNum.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollutionid", pollutionId);
                map.put("pollutionname", idAndName.get(pollutionId));
                map.put("nolimitnum", pollutonidAndNum.get(pollutionId));
                charDataList.add(map);
            }
            Collections.sort(charDataList, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer s1 = (Integer) o1.get("nolimitnum");
                    Integer s2 = (Integer) o2.get("nolimitnum");
                    if (s1 > s2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 6:34
     * @Description: 根据mn号获取企业ID
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getPollutionId(Map<String, List<String>> pollutionAndOutPut, String mnKey) {
        String pollutionid = "";
        for (String id : pollutionAndOutPut.keySet()) {
            if (pollutionAndOutPut.get(id).contains(mnKey)) {
                pollutionid = id;
                break;
            }
        }
        return pollutionid;
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 3:31
     * @Description: 获取mn号集合，污染源和排口的映射关系集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setMnsAndPollutionOutPut(List<String> mns, Map<String, List<String>> pollutionAndOutPut, List<Map<String, Object>> outPuts) {

        List<String> mnsTemp;
        String pollutionid = "";
        String mn = "";
        for (Map<String, Object> output : outPuts) {
            mn = output.get("dgimn").toString();
            mns.add(mn);
            pollutionid = output.get("pk_pollutionid").toString();
            if (pollutionAndOutPut.get(pollutionid) != null) {
                mnsTemp = pollutionAndOutPut.get(pollutionid);
            } else {
                mnsTemp = new ArrayList<>();
            }
            mnsTemp.add(mn);
            pollutionAndOutPut.put(pollutionid, mnsTemp);
        }
    }


    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 10:34
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数导出单个站点年排放量数据报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneOutPutYearFlowDataReportByPollutantTypeAndParams", method = RequestMethod.POST)
    public void exportOneOutPutYearFlowDataReportByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 1);
            //点位类型：1-排口，2-监测点
            if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                titleMap.put("pointType", 1);
            } else {
                titleMap.put("pointType", 2);
            }

            titleMap.put("pollutantType", pollutanttype);
            //是否显示排放量
            titleMap.put("isShowFlow", "show");
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            //获取表格数据
            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "YearFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = getFileNameByPollutantType(pollutanttype);
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getFileNameByPollutantType(Integer pollutantType) {
        String fileName = "";

        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pollutantType)) {
            case WasteWaterEnum:
                fileName = "废水污染物排放量导出文件";
                break;
            case WasteGasEnum:
                fileName = "废气污染物排放量导出文件";
                break;
            case SmokeEnum:
                fileName = "烟气污染物排放量导出文件";
                break;
            case RainEnum:
                fileName = "雨水污染物排放量导出文件";
                break;
            case AirEnum:
                fileName = "大气污染物排放量导出文件";
                break;
            case EnvironmentalStinkEnum:
                fileName = "恶臭污染物排放量导出文件";
                break;
            case EnvironmentalVocEnum:
                fileName = "VOC污染物排放量导出文件";
                break;
        }
        return fileName + new Date().getTime();


    }


    /**
     * @author: zhangzc
     * @date: 2019/8/27 16:06
     * @Description: 根据监测类型和污染物Code获取该监测点类型下每个监测点该污染物的浓度以及风速和风力
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEachMonitorPointMinuteAndWeatherDataByParams", method = RequestMethod.POST)
    public Object getEachMonitorPointMinuteAndWeatherDataByParams(
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            List<Map<String, Object>> resultMap = new ArrayList<>();
            List<Integer> enums = new ArrayList<>();
            enums.add(FactoryBoundaryStinkEnum.getCode());
            enums.add(FactoryBoundarySmallStationEnum.getCode());
            enums.add(EnvironmentalDustEnum.getCode());
            enums.add(EnvironmentalVocEnum.getCode());
            enums.add(EnvironmentalStinkEnum.getCode());
            enums.add(MicroStationEnum.getCode());
            if (!enums.contains(monitorpointtype)) {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            LocalDate localDate = LocalDate.now();
            Date startTime = DataFormatUtil.parseDate(localDate + " 00:00:00");
            Date endTime = DataFormatUtil.parseDate(localDate + " 23:59:59");
            Set<String> mns = new HashSet<>();
            List<Map<String, Object>> outputs = new ArrayList<>();

            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Integer type : monitorpointtypes) {
                    outputs.addAll(onlineService.getMnForWeatherByParam(type));
                }
            } else {
                outputs = onlineService.getMnForWeatherByParam(monitorpointtype);
            }
            for (Map<String, Object> output : outputs) {
                Object airMN = output.get("MN");    //空气监测点的MN号
                Object dgimn = output.get("DGIMN"); //mn号
                if (dgimn != null) {
                    mns.add(dgimn.toString());
                    if (airMN != null) {
                        mns.add(airMN.toString());
                    }
                }
            }
            if (mns.size() > 0) {
                String code = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
                String code2 = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
                List<String> pollutants = new ArrayList<>();
                pollutants.add(code);
                pollutants.add(code2);
                pollutants.add(pollutantcode);
                List<AggregationOperation> aggregations = new ArrayList<>();
                Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime).and("HourDataList.PollutantCode").in(pollutants);
                aggregations.add(match(criteria));
                UnwindOperation unwindOperation = unwind("HourDataList");
                aggregations.add(unwindOperation);
                Criteria in = Criteria.where("HourDataList.PollutantCode").in(pollutants);
                aggregations.add(match(in));
                Fields fields = fields("DataGatherCode", "MonitorTime").and("PollutantCode", "HourDataList.PollutantCode").and("AvgStrength", "HourDataList.AvgStrength");
                aggregations.add(project(fields));
                Map<String, Object> map2 = new HashMap<>();
                map2.put("PollutantCode", "$PollutantCode");
                map2.put("AvgStrength", "$AvgStrength");
                GroupOperation groupOperation = group("DataGatherCode", "MonitorTime").push(map2).as("HourDataList");
                aggregations.add(groupOperation);
                Aggregation aggregation = newAggregation(aggregations);
                AggregationResults<Document> minuteData = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
                List<Document> documents = minuteData.getMappedResults();
                if (documents.size() > 0) {
                    Map<String, List<Document>> documentMap = documents.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                    for (Map<String, Object> output : outputs) {
                        Object dgimn = output.get("DGIMN"); //mn号
                        String mn = dgimn.toString();
                        if (documentMap.containsKey(mn)) {
                            List<Document> documents1 = documentMap.get(mn);
                            Map<String, Object> monitorMap = new HashMap<>();
                            List<Map<String, Object>> mapList = new ArrayList<>();
                            Object MonitorPointName = output.get("MonitorPointName");
                            Object airMN = output.get("MN");    //空气监测点的MN号
                            if (airMN != null) {
                                List<Document> weather = documentMap.get(airMN.toString());
                                for (int i = 0;i<=23;i++) {
                                    String time = "";
                                    if (i<10){
                                        time = "0"+i+":00";
                                    }else{
                                        time = i+":00";
                                    }
                                    Object monitorValue = null;
                                    Object windspeed = null;
                                    Object winddirection = null;
                                    for (Document document : documents1) {
                                        String date = DataFormatUtil.getDate(document.getDate("MonitorTime"));
                                        String hms = date.substring(11, 16);    //监测时间
                                        if (time.equals(hms)) {
                                            List<Document> pollutantDatas = document.get("HourDataList", new ArrayList<>().getClass());
                                            for (Document pollutantData : pollutantDatas) {
                                                String PollutantCode = pollutantData.getString("PollutantCode");
                                                if (PollutantCode.equals(pollutantcode)) {
                                                    if (pollutantData.get("AvgStrength") != null) { //浓度值
                                                        monitorValue = pollutantData.get("AvgStrength");
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (weather!=null) {
                                        for (Document document : weather) {
                                            String date = DataFormatUtil.getDate(document.getDate("MonitorTime"));
                                            String hms = date.substring(11, 16);    //监测时间
                                            if (time.equals(hms)) {
                                                List<Document> pollutantDatas = document.get("HourDataList", new ArrayList<>().getClass());
                                                for (Document pollutantData : pollutantDatas) {
                                                    String PollutantCode = pollutantData.getString("PollutantCode");
                                                    if (PollutantCode.equals(CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode())) {  //风速
                                                        if (pollutantData.get("AvgStrength") != null) {
                                                            windspeed = pollutantData.get("AvgStrength");
                                                        }
                                                    }
                                                    if (PollutantCode.equals(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode())) { //风向
                                                        if (pollutantData.get("AvgStrength") != null) {
                                                            winddirection = pollutantData.get("AvgStrength");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("monitortime", time);
                                    map.put("monitorvalue", monitorValue != null ? monitorValue : "");
                                    map.put("winddirectioncode", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "code") : "");
                                    map.put("winddirectionname", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "name") : "");
                                    map.put("winddirectionvalue", winddirection != null ? winddirection : "");
                                    map.put("windspeed", windspeed != null ? windspeed : "");
                                    if (!"".equals(map.get("monitorvalue").toString()) || !"".equals(map.get("winddirectioncode").toString()) || !"".equals(map.get("winddirectionname").toString()) || !"".equals(map.get("winddirectionvalue").toString()) || !"".equals(map.get("windspeed").toString())) {
                                        mapList.add(map);
                                    }
                                }
                            } else {
                                for (int i = 0;i<=23;i++) {
                                    String time = "";
                                    if (i<10){
                                        time = "0"+i+":00";
                                    }else{
                                        time = i+":00";
                                    }
                                    Object monitorValue = null;
                                    Object windspeed = null;
                                    Object winddirection = null;
                                    for (Document document : documents1) {
                                        String date = DataFormatUtil.getDate(document.getDate("MonitorTime"));
                                        String hms = date.substring(11, 16);    //监测时间
                                        if (time.equals(hms)) {
                                            List<Document> pollutantDatas = document.get("HourDataList", new ArrayList<>().getClass());
                                            for (Document pollutantData : pollutantDatas) {
                                                String PollutantCode = pollutantData.getString("PollutantCode");
                                                if (PollutantCode.equals(pollutantcode)) {
                                                    if (pollutantData.get("AvgStrength") != null) { //浓度值
                                                        monitorValue = pollutantData.get("AvgStrength");
                                                    }
                                                }
                                                if (PollutantCode.equals(CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode())) {  //风速
                                                    if (pollutantData.get("AvgStrength") != null) {
                                                        windspeed = pollutantData.get("AvgStrength");
                                                    }
                                                }
                                                if (PollutantCode.equals(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode())) { //风向
                                                    if (pollutantData.get("AvgStrength") != null) {
                                                        winddirection = pollutantData.get("AvgStrength");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("monitortime", time);
                                    map.put("monitorvalue", monitorValue != null ? monitorValue : "");
                                    map.put("winddirectioncode", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "code") : "");
                                    map.put("winddirectionname", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "name") : "");
                                    map.put("winddirectionvalue", winddirection != null ? winddirection : "");
                                    map.put("windspeed", windspeed != null ? windspeed : "");
                                    if (!"".equals(map.get("monitorvalue").toString()) || !"".equals(map.get("winddirectioncode").toString()) || !"".equals(map.get("winddirectionname").toString()) || !"".equals(map.get("winddirectionvalue").toString()) || !"".equals(map.get("windspeed").toString())) {
                                        mapList.add(map);
                                    }
                                }
                            }
                            monitorMap.put("monitorname", MonitorPointName);
                            monitorMap.put("monitordata", mapList);
                            resultMap.add(monitorMap);
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/27 16:06
     * @Description: 根据监测类型和污染物Code获取该监测点类型下每个监测点该污染物的浓度以及风速和风力
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getHourWeatherDataByMNAndPollutant", method = RequestMethod.POST)
    public Object getHourWeatherDataByMNAndPollutant(@RequestJson(value = "mn") String mn,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            List<Map<String, Object>> resultMap = new ArrayList<>();
            Date startTime = DataFormatUtil.parseDate(starttime + ":00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + ":59:59");
            final String unWindName = "HourDataList";
            final String conllection = "HourData";
            String code = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
            String code2 = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
            List<String> pollutants = new ArrayList<>();
            pollutants.add(code);
            pollutants.add(code2);
            String airMn = "";
            List<String> airDgimns = otherMonitorPointService.getAirDgimnByStinkMonitorDgimn(mn);
            if (airDgimns!=null&&airDgimns.size() > 0) {
                airMn = airDgimns.get(0);
            }
            List<Document> documents;
            List<String> pollutants2 = new ArrayList<>();
            pollutants2.add(pollutantcode);
            if (StringUtils.isNotBlank(airMn)) { //获取气象数据
                List<Document> pollutantsDocuments = getHourAggregationsForWeather(unWindName, conllection, mn, startTime, endTime, pollutants2);  //污染物数据
                if (pollutantsDocuments.size() > 0) {
                    List<Document> weatherDocuments = getHourAggregationsForWeather(unWindName, conllection, airMn, startTime, endTime, pollutants);  //风向数据
                    for (Document document : pollutantsDocuments) {
                        Date monitorTime = document.getDate("MonitorTime");
                        for (Document weatherDocument : weatherDocuments) {
                            Date monitorTime2 = weatherDocument.getDate("MonitorTime");
                            if (monitorTime.equals(monitorTime2)) {
                                List<Document> pollutantDatasInfo = new ArrayList<>();
                                if (document.get(unWindName) != null) {
                                    List<Document> pollutantDatas = document.get(unWindName, new ArrayList<>().getClass());
                                    for (Document pollutantData : pollutantDatas) {
                                        if (pollutantData.get("PollutantCode") != null && pollutantData.get("PollutantCode").equals(pollutantcode)) {
                                            pollutantDatasInfo.add(pollutantData);
                                            break;
                                        }
                                    }
                                }
                                if (weatherDocument.get(unWindName) != null) {
                                    List<Document> weather = weatherDocument.get(unWindName, new ArrayList<>().getClass());
                                    pollutantDatasInfo.addAll(weather);
                                }
                                document.replace(unWindName, pollutantDatasInfo);
                                break;
                            }
                        }
                    }
                }
                documents = pollutantsDocuments;
            } else {
                pollutants.add(pollutantcode);
                List<AggregationOperation> aggregations = new ArrayList<>();
                Criteria criteria = Criteria.where("DataGatherCode").is(mn).and("MonitorTime").gte(startTime).lte(endTime).and(unWindName + ".PollutantCode").is(pollutantcode);
                aggregations.add(match(criteria));
                UnwindOperation unwindOperation = unwind(unWindName);
                aggregations.add(unwindOperation);
                Criteria in = Criteria.where(unWindName + ".PollutantCode").in(pollutants);
                aggregations.add(match(in));
                Fields fields = fields("DataGatherCode", "MonitorTime").and("PollutantCode", unWindName + ".PollutantCode").and("AvgStrength", unWindName + ".AvgStrength");
                aggregations.add(project(fields));
                Map<String, Object> map2 = new HashMap<>();
                map2.put("PollutantCode", "$PollutantCode");
                map2.put("AvgStrength", "$AvgStrength");
                GroupOperation groupOperation = group("DataGatherCode", "MonitorTime").push(map2).as(unWindName);
                aggregations.add(groupOperation);
                Aggregation aggregation = newAggregation(aggregations);
                AggregationResults<Document> minuteData = mongoTemplate.aggregate(aggregation, conllection, Document.class);
                documents = minuteData.getMappedResults();
            }
            if (documents.size() > 0) {
                Object monitorValue = null;
                Object windspeed = null;
                Object winddirection = null;
                for (Document document : documents) {
                    Date monitorTime = document.getDate("MonitorTime");
                    String dateYMDH = DataFormatUtil.getDateYMDH(monitorTime);
                    List<Document> pollutantDatas = document.get(unWindName, new ArrayList<>().getClass());
                    for (Document pollutantData : pollutantDatas) {
                        String PollutantCode = pollutantData.getString("PollutantCode");
                        if (PollutantCode.equals(pollutantcode)) {
                            monitorValue = pollutantData.get("AvgStrength");
                        }
                        if (PollutantCode.equals(CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode())) {  //风速
                            if (pollutantData.get("AvgStrength") != null) {
                                windspeed = pollutantData.get("AvgStrength");
                            }
                        }
                        if (PollutantCode.equals(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode())) { //风向
                            if (pollutantData.get("AvgStrength") != null) {
                                winddirection = pollutantData.get("AvgStrength");
                            }
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("monitortime", dateYMDH);
                    map.put("monitorvalue", monitorValue != null ? monitorValue : "");
                    map.put("winddirectioncode", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "code") : "");
                    map.put("winddirectionname", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "name") : "");
                    map.put("winddirectionvalue", winddirection != null ? winddirection : "");
                    map.put("windspeed", windspeed != null ? windspeed : "");
                    resultMap.add(map);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/6 10:41
     * @Description: 查询浓度或者气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getHourAggregationsForWeather(String unWindName, String conllection, String mn, Date startTime, Date endTime, List<String> pollutantCodes) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").is(mn).and("MonitorTime").gte(startTime).lte(endTime).and(unWindName + ".PollutantCode").in(pollutantCodes);
        aggregations.add(match(criteria));
        UnwindOperation unwindOperation = unwind(unWindName);
        aggregations.add(unwindOperation);
        Criteria in = Criteria.where(unWindName + ".PollutantCode").in(pollutantCodes);
        aggregations.add(match(in));
        Fields fields = fields("DataGatherCode", "MonitorTime").and("PollutantCode", unWindName + ".PollutantCode").and("AvgStrength", unWindName + ".AvgStrength");
        aggregations.add(project(fields));
        Map<String, Object> map2 = new HashMap<>();
        map2.put("PollutantCode", "$PollutantCode");
        map2.put("AvgStrength", "$AvgStrength");
        GroupOperation groupOperation = group("DataGatherCode", "MonitorTime").push(map2).as(unWindName);
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> minuteData = mongoTemplate.aggregate(aggregation, conllection, Document.class);
        return minuteData.getMappedResults();
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/29 11:07
     * @Description: 将24小时通过分钟分割
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getHMTimesSpiltMFor24Hours() {
        Date date = DataFormatUtil.parseDate("2000-01-01 00:05:00");
        Date date1 = DataFormatUtil.parseDate("2000-01-01 23:55:00");
        List<String> times = DataFormatUtil.getIntervalTimeStringList(date, date1, 10);
        return times.stream().map(time -> time.substring(11, 16)).collect(Collectors.toList());
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数导出多个站点年排放量数据报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyOutPutYearFlowDataReportByPollutantTypeAndParams", method = RequestMethod.POST)
    public void exportManyOutPutYearFlowDataReportByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype", required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes", required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 2);
            if (pollutanttypes ==null&&pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            if (pollutanttypes.size()>0){
                List<Map<String, Object>> tabletitledata = new ArrayList<>();
                List<String> mns = new ArrayList<>();
                //设置文件名称
                String fileName = "";
                //是否显示排放量
                titleMap.put("isShowFlow", "show");
                //排口集合
                titleMap.put("outputids", outputids);
                if (pollutanttypes.size()==1){//单个类型
                    if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttypes.get(0))) {
                        titleMap.put("pointType", 1);
                    } else {
                        titleMap.put("pointType", 2);
                    }
                    titleMap.put("pollutantType", pollutanttype);
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                    mns = getMNsByOutPutids(outputids, pollutanttype);
                    fileName = getFileNameByPollutantType(pollutanttype);
                }else{//  废气、烟气合并 传多个类型时
                    for (Integer i:pollutanttypes){
                        mns.addAll(getMNsByOutPutids(outputids, i));
                    }
                    titleMap.put("pollutantType", pollutanttypes.get(0));
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                    fileName = getFileNameByPollutantType(pollutanttypes.get(0));
                }
                Map<String, Object> paramMap = new HashMap<>();
                String collection = "YearFlowData";
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + "-01-01 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.putAll(titleMap);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
                //设置导出文件数据格式
                List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
                List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
                ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 10:34
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数导出单个站点月排放量数据报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */


    @RequestMapping(value = "exportOneOutPutMonthFlowDataReportByPollutantTypeAndParams", method = RequestMethod.POST)
    public void exportOneOutPutMonthFlowDataReportByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 1);
            //点位类型：1-排口，2-监测点
            if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttype)) {
                titleMap.put("pointType", 1);
            } else {
                titleMap.put("pointType", 2);
            }

            titleMap.put("pollutantType", pollutanttype);
            //是否显示排放量
            titleMap.put("isShowFlow", "show");
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            //获取表格数据
            List<String> mns = getMNsByOutPutids(outputids, pollutanttype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "MonthFlowData";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + "-01 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = getFileNameByPollutantType(pollutanttype);
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过污染物类型（水-1，气-2，雨水-37，5-空气，恶臭-9，10-VOC）和查询条件参数导出多个站点月排放量数据报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyOutPutMonthFlowDataReportByPollutantTypeAndParams", method = RequestMethod.POST)
    public void exportManyOutPutMonthFlowDataReportByPollutantTypeAndParams(
            @RequestJson(value = "pollutanttype", required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes", required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            //列表类型：1-监测时间+监测污染物，2-点位+监测时间+监测污染物
            titleMap.put("reportType", 2);
            //点位类型：1-排口，2-监测点
            if (pollutanttypes ==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            if (pollutanttypes.size()>0) {
                List<Map<String, Object>> tabletitledata = new ArrayList<>();
                List<String> mns = new ArrayList<>();
                String fileName = "";
                //是否显示排放量
                titleMap.put("isShowFlow", "show");
                titleMap.put("outputids", outputids);
                if (pollutanttypes.size() == 1) {//单个类型
                    if (CommonTypeEnum.getOutPutTypeList().contains(pollutanttypes.get(0))) {
                        titleMap.put("pointType", 1);
                    } else {
                        titleMap.put("pointType", 2);
                    }
                    titleMap.put("pollutantType", pollutanttype);
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                    mns = getMNsByOutPutids(outputids, pollutanttype);
                    //设置文件名称
                    fileName = getFileNameByPollutantType(pollutanttype);
                } else {//  废气、烟气合并 传多个类型时
                    for (Integer i : pollutanttypes) {
                        mns.addAll(getMNsByOutPutids(outputids, i));
                    }
                    titleMap.put("pollutantType", pollutanttypes.get(0));
                    tabletitledata = onlineService.getTableTitleForReport(titleMap);
                    //设置文件名称
                    fileName = getFileNameByPollutantType(pollutanttypes.get(0));
                }
                Map<String, Object> paramMap = new HashMap<>();
                String collection = "MonthFlowData";
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + "-01 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.putAll(titleMap);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
                //设置导出文件数据格式
                List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
                List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
                ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
            }
            } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 下午 8:37
     * @Description: 通过排口类型获取近一个月排污总量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "getLastMonthFlowSummaryRankByType", method = RequestMethod.POST)
    public Object getLastMonthFlowSummaryRankByType(@RequestJson(value = "type") Integer type, @RequestJson(value = "pollutantcode") String pollutantcode) {

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat format1 = new DecimalFormat("0.#");
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("flag", "water");
            paramMap.put("type", type);

            if (type == WasteWaterEnum.getCode()) {
                data = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
            } else if (type == WasteGasEnum.getCode() || type == SmokeEnum.getCode()) {
                data = gasOutPutInfoService.getAllOutPutInfo(paramMap);
            }

            String dgimns = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            //设置查询条件查询mongo数据

            String month = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String startTime = JSONObjectUtil.getStartTime(month);
            String endTime = JSONObjectUtil.getEndTime(month);

            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);
            List<FlowDataVO> flowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH:mm:ss");


            Map<String, List<Map<String, Object>>> collect = data.stream().filter(m -> m.get("PollutionName") != null).collect(Collectors.groupingBy(m -> m.get("PollutionName").toString()));


            //组装返回结果数据
            for (String pollutionname : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(pollutionname);
                List<String> dgimnlist = collect.get(pollutionname).stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
                Map<String, Object> result = new HashMap<>();
                final Float[] flow = {0f};
                for (FlowDataVO flowDatum : flowData) {
                    String mn = flowDatum.getDataGatherCode();
                    if (dgimnlist.contains(mn)) {
                        final boolean[] flag = {false};
                        List<Map<String, Object>> dayFlowDataList = flowDatum.getDayFlowDataList();
                        flow[0] = dayFlowDataList.stream().filter(map -> map.get("PollutantCode") != null && map.get("CorrectedFlow") != null && Float.valueOf(map.get("CorrectedFlow").toString()) > 0 && pollutantcode.equals(map.get("PollutantCode").toString())).peek(m -> {
                            flag[0] = true;
                        }).map(map -> Float.valueOf(map.get("CorrectedFlow").toString())).reduce(flow[0], Float::sum);
                        if (flag[0] && flow[0] > 0.05) {
                            result.put("shortname", pollutionname);
                            if (list.size() > 0) {
                                result.put("pollutionid", list.get(0).get("PK_PollutionID"));
                                result.put("pollutionname", list.get(0).get("pollution"));
                            }
                            result.put("flow", format1.format(flow[0]));
                        }
                    }
                }
                if (result.size() > 0) {
                    resultList.add(result);
                }
            }
            List<Map<String, Object>> collect1 = resultList.stream().filter(m -> m.get("flow") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect1);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 13:38
     * @Description: 获取数据预警及超标报警的查询条件
     * @param:
     * @return:
     */
    @RequestMapping(value = "getQueryCriteriaForEarlyAndOverAlarm", method = RequestMethod.POST)
    public Object getQueryCriteriaForEarlyAndOverAlarm(@RequestJson(value = "type") Integer type) {
        try {
            Map<String, Object> queryDataForRealTime = onlineService.getQueryCriteriaForEarlyAndOverAlarm(type);
            return AuthUtil.parseJsonKeyToLower("success", queryDataForRealTime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:32
     * @Description: 统计最近一天监测预警超标数据
     * @updateUser: xsm
     * @updateDate: 2019/09/05 0005 下午 5:12
     * @updateDescription: 添加异常数据统计
     * @param:
     * @return:
     */
    @RequestMapping(value = "countLastDayEarlyAndOverData", method = RequestMethod.GET)
    public Object countLastDayEarlyAndOverData(@RequestJson(value = "nowday", required = false) String nowday) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Date today = new Date();
            if (StringUtils.isNotBlank(nowday)) {
                today = DataFormatUtil.getDateYMD(nowday);
            }
            String endTime = DataFormatUtil.getDateYMDHMS(today);
            String startTime = DataFormatUtil.getDateYMD(today) + " 00:00:00";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            Map<String, Object> mapData = onlineService.countEarlyAndOverDataByParamMap(paramMap);
            //统计废水的异常次数 0值异常 其它异常次数之和
            Map<String, Object> param = new HashMap<>();
            param.put("starttime", DataFormatUtil.getDateYMD(today));
            param.put("endtime", DataFormatUtil.getDateYMD(today));
            param.put("monitortype", WasteWaterEnum.getCode());
            List<String> dgimns = new ArrayList<>();
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(param);
            String mnCommon;
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);

                }
            }
            param.put("dgimns", dgimns);
            int waternum = 0;
            Map<String, Object> result = onlineService.countNoFlowAndOtherExceptionTypeDataNum(param);
            if (result != null) {
                waternum = Integer.parseInt(result.get("noflownum").toString());
            }
            if (mapData != null && mapData.get("exceptiondata") != null) {
                Map<String, Object> exceptiondata = (Map<String, Object>) mapData.get("exceptiondata");
                exceptiondata.put("waternum", Integer.parseInt(exceptiondata.get("waternum").toString()) + waternum);
                exceptiondata.put("totalnum", Integer.parseInt(exceptiondata.get("totalnum").toString()) + waternum);
                mapData.put("exceptiondata", exceptiondata);
            }
            return AuthUtil.parseJsonKeyToLower("success", mapData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:32
     * @Description: 统计最近一天监测预警超标数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastDayEarlyDataByPollutantType", method = RequestMethod.POST)
    public Object getLastDayEarlyDataByPollutantType(@RequestJson(value = "pollutanttype") String pollutanttype,
                                                     @RequestJson(value = "pagesize", required = false) Integer pageSize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pageNum) {
        try {
            Map<String, Object> mapData = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Date nowDay = new Date();
            String endTime = DataFormatUtil.getDateYMDHMS(nowDay);
            String startTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreDate(nowDay, 1));
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("pollutanttype", pollutanttype);
            paramMap.put("pagesize", pageSize);
            paramMap.put("pagenum", pageNum);
            List<Map<String, Object>> tabletitledata = onlineService.getEarlyTableTitleDataByParamMap(paramMap);
            List<Map<String, Object>> tablelistdata = onlineService.getEarlyTableListDataByParamMap(paramMap);
            mapData.put("tabletitledata", tabletitledata);
            mapData.put("tablelistdata", tablelistdata);
            mapData.put("total", paramMap.get("total"));
            mapData.put("pages", paramMap.get("pages"));
            mapData.put("pagesize", paramMap.get("pagesize"));
            mapData.put("pagenum", paramMap.get("pagenum"));
            return AuthUtil.parseJsonKeyToLower("success", mapData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 1:32
     * @Description: 统计最近一天监测预警超标数据
     * @updateUser:xsm
     * @updateDate: 2019/9/11 0011 下午 6:19
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastDayOverDataByPollutantType", method = RequestMethod.POST)
    public Object getLastDayOverDataByPollutantType(@RequestJson(value = "pollutanttype") String pollutanttype,
                                                    @RequestJson(value = "pagesize", required = false) Integer pageSize,
                                                    @RequestJson(value = "pagenum", required = false) Integer pageNum) {
        try {
            Map<String, Object> mapData = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Date nowDay = new Date();
            String endTime = DataFormatUtil.getDateYMD(nowDay);
            String startTime = DataFormatUtil.getDateYMD(nowDay);
            paramMap.put("pollutanttype", pollutanttype);
            paramMap.put("monitorpointtype", Integer.parseInt(pollutanttype));
            List<Map<String, Object>> tabletitledata = onlineService.getOverTableTitleDataByParamMap(paramMap);
            //从MongoDB中查询数据
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            Map<String, Object> resultMap = onlineService.getOverListDataByParam(paramMap, startTime, endTime, new ArrayList<>(), Integer.parseInt(pollutanttype), usercode, pageNum, pageSize);
            // List<Map<String, Object>> tablelistdata = onlineService.getOverTableListDataByParamMap(paramMap);
            mapData.put("tabletitledata", tabletitledata);
            mapData.put("tablelistdata", resultMap.get("datalist"));
            mapData.put("total", resultMap.get("total"));
            // mapData.put("pages", paramMap.get("pages"));
            mapData.put("pagesize", paramMap.get("pagesize"));
            mapData.put("pagenum", paramMap.get("pagenum"));
            return AuthUtil.parseJsonKeyToLower("success", mapData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 2:44
     * @Description: 通过排口类型(1水2气)，年份查询污染物年排放量，许可排放量，占比等统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "getYearFlowStatisticsByFlowYearAndType", method = RequestMethod.POST)
    public Object getYearFlowStatisticsByFlowYearAndType(@RequestJson(value = "type") Integer type, @RequestJson(value = "flowyear") Integer flowyear
            , @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {

        try {
            DecimalFormat decimalformat = new DecimalFormat("0.#");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            List<Map<String, Object>> chartdatalist = new ArrayList<>();
            Map<String, Object> resultMap = new HashMap<>();
            Calendar instance = Calendar.getInstance();
            instance.clear();
            instance.set(Calendar.YEAR, flowyear);
            Date time = instance.getTime();

            String starttime = format.format(time);

            instance.clear();
            instance.set(Calendar.YEAR, flowyear + 1);
            instance.add(Calendar.SECOND, -1);
            Date time1 = instance.getTime();

            String endtime = format.format(time1);


            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            Map<String, Object> paramMap = new HashMap<>();
            String dgimns = getOutPutInfoDgimns(type, data, paramMap);


            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);

            Map<String, Object> polltantparams = new HashMap<>();
            polltantparams.put("pollutanttype", type);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(polltantparams);


            List<FlowDataVO> yearFlowData = mongoBaseService.getListByParam(flowDataVO, "YearFlowData", "yyyy-MM-dd HH:mm:ss");
            List<FlowDataVO> dayFlowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH:mm:ss");


            Map<String, List<Map<String, Object>>> collect = data.stream().filter(m -> m.get("pollution") != null).collect(Collectors.groupingBy(m -> m.get("pollution").toString()));
            for (String pollutionname : collect.keySet()) {

                List<Map<String, Object>> list = collect.get(pollutionname);
                Map<String, Object> params = new HashMap<>();
                params.put("monitorpointtype", type);
                params.put("flowyear", flowyear);
                if (list.size() > 0) {
                    Map<String, Object> map = list.get(0);
                    if (map.get("PK_PollutionID") != null) {
                        String pk_pollutionID = map.get("PK_PollutionID").toString();
                        params.put("pollutionid", pk_pollutionID);
                    }
                }
                String flowUnit = "";
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> resultlist = new ArrayList<>();


                List<Map<String, Object>> entPermittedFlowLimitValue = entPermittedFlowLimitValueService.selectByParams(params);


                Map<String, Object> daydata = new HashMap<>();
                List<Map<String, Object>> pollutantsinfo = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    if (map.get("DGIMN") != null) {
                        String dgimn = map.get("DGIMN").toString();
                        dayFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode())).map(m -> m.getDayFlowDataList()).forEach(m -> pollutantsinfo.addAll(m));
                        Map<String, List<Map<String, Object>>> collect1 = pollutantsinfo.stream().filter(m -> m.get("PollutantCode") != null).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                        for (String code : collect1.keySet()) {
                            List<Map<String, Object>> listdata = collect1.get(code);
                            Float reduce = listdata.stream().filter(m -> m.get("CorrectedFlow") != null).map(m -> Float.valueOf(m.get("CorrectedFlow").toString())).reduce(0f, Float::sum);
                            daydata.put(code, reduce / listdata.size());
                        }
                    }
                }


                Map<String, Object> datas = new HashMap<>();
                for (Map<String, Object> datum : list) {
                    if (datum.get("DGIMN") != null) {
                        String dgimn = datum.get("DGIMN").toString();
                        for (FlowDataVO yearFlowDatum : yearFlowData) {
                            if (dgimn.equals(yearFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> yearFlowDataList = yearFlowDatum.getYearFlowDataList();
                                for (Map<String, Object> map : yearFlowDataList) {
                                    String pollutantCode = map.get("PollutantCode").toString();
                                    if (datas.get(pollutantCode) != null) {
                                        datas.put(pollutantCode, Float.valueOf(map.get("PollutantFlow").toString()) + Float.valueOf(datas.get(pollutantCode).toString()));
                                    } else {
                                        datas.put(pollutantCode, Float.valueOf(map.get("PollutantFlow").toString()));
                                    }
                                }
                            }
                        }
                    }
                }
                for (String yearcode : datas.keySet()) {
                    Map<String, Object> data1 = new HashMap<>();
                    List<String> collect4 = pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && yearcode.equals(m.get("code").toString())).map(m -> m.get("name").toString()).collect(Collectors.toList());
                    if (collect4.size() > 0) {
                        data1.put("pollutantname", collect4.get(0));
                    } else {
                        data1.put("pollutantname", "");
                    }

                    data1.put("pollutantcode", yearcode);
                    data1.put("totalflow", datas.get(yearcode));
                    //don't cry stand up lu,do it again
                    for (Map<String, Object> map : entPermittedFlowLimitValue) {
                        if (map.get("FK_PollutantCode") != null) {
                            String fk_pollutantCode = map.get("FK_PollutantCode").toString();
                            if (fk_pollutantCode.equals(yearcode)) {
                                data1.put("flowlimit", map.get("TotalFlow"));
                                if (datas.get(yearcode) != null && map.get("TotalFlow") != null && Float.valueOf(map.get("TotalFlow").toString()) > 0) {
                                    Float aFloat = Float.valueOf(datas.get(yearcode).toString());
                                    Float totalFlow = Float.valueOf(map.get("TotalFlow").toString());
                                    data1.put("proportion", decimalformat.format(aFloat / totalFlow * 100) + "%");
                                    for (String daycode : daydata.keySet()) {
                                        if (daydata.get(daycode) != null) {
                                            if (yearcode.equals(daycode)) {
                                                Float avgflow = Float.valueOf(daydata.get(daycode).toString());
                                                if (aFloat >= totalFlow) {
                                                    data1.put("daysremaining", 0);
                                                } else {
                                                    String format2 = String.format("%tj", now);
                                                    Integer integer = Integer.valueOf(format2);
                                                    instance.clear();
                                                    int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_YEAR);

                                                    int surplusDays = actualMaximum - integer;
                                                    Float v = (totalFlow - aFloat) / avgflow;
                                                    if (v.intValue() > surplusDays) {
                                                        data1.put("daysremaining", surplusDays);
                                                    } else {
                                                        data1.put("daysremaining", v.intValue());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Set<String> yearcodes = datas.keySet().stream().collect(Collectors.toSet());
                    List<String> collect2 = entPermittedFlowLimitValue.stream().filter(m -> m.get("FK_PollutantCode") != null).map(m -> m.get("FK_PollutantCode").toString()).collect(Collectors.toList());
                    yearcodes.removeAll(collect2);
                    for (String s : yearcodes) {
                        if (yearcode.equals(s)) {
                            data1.put("flowlimit", "-");
                            data1.put("proportion", "-");
                        }
                    }

                    Set<String> yearcodes1 = datas.keySet().stream().collect(Collectors.toSet());
                    Set<String> collect3 = daydata.keySet().stream().collect(Collectors.toSet());
                    yearcodes1.removeAll(collect3);
                    for (String s : yearcodes1) {
                        if (yearcode.equals(s)) {
                            data1.put("daysremaining", "-");
                        }
                    }
                    resultlist.add(data1);
                }


                result.put("pollutionname", pollutionname);
                if (list.size() > 0) {
                    result.put("pollutionid", list.get(0).get("PK_PollutionID"));
                }
                Map<String, Object> chardata = new HashMap<>();
                chartdatalist.add(chardata);
                List<Map<String, Object>> collect2 = resultlist.stream().filter(m -> m.get("daysremaining") != null && m.get("proportion") != null).sorted(Comparator.comparing(m -> ((Map) m).get("daysremaining").
                        toString()).thenComparing(m -> ((Map) m).get("proportion").toString()).reversed()).collect(Collectors.toList());
                if (collect2.size() > 0) {
                    chardata.put("daysremaining", collect2.get(0).get("daysremaining"));
                } else {
                    chardata.put("daysremaining", "-");
                }
                if (list.size() > 0) {
                    chardata.put("shortername", list.get(0).get("PollutionName"));
                }
                result.put("flowunit", flowUnit);
                result.put("detail", collect2);
                resultList.add(result);
            }
            List<Map<String, Object>> collect2 = resultList.stream().sorted(Comparator.comparing(m -> {
                List<Map<String, Object>> detail = (List) ((Map) m).get("detail");
                if (detail.size() > 0) {
                    Map<String, Object> map = detail.get(0);
                    if (map.get("proportion") != null && !"-".equals(map.get("proportion").toString())) {
                        NumberFormat numberformat = NumberFormat.getInstance();
                        try {
                            Number proportion = numberformat.parse(map.get("proportion").toString());
                            return proportion.floatValue();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return new Float(-1);
            }).reversed()).collect(Collectors.toList());

            List<Map<String, Object>> collect3 = chartdatalist.stream().filter(m -> m.get("daysremaining") != null && "-".equals(m.get("daysremaining").toString())).collect(Collectors.toList());
            List<Map<String, Object>> collect4 = chartdatalist.stream().filter(m -> m.get("daysremaining") != null && !"-".equals(m.get("daysremaining").toString())).
                    sorted(Comparator.comparing(m -> Float.valueOf(m.get("daysremaining").toString()))).collect(Collectors.toList());

            collect4.addAll(collect3);

            Map<String, Object> tabledata = new HashMap<>();
            List<Map<String, Object>> collect5 = collect2.stream().skip(pagenum - 1).limit(pagesize).collect(Collectors.toList());
            tabledata.put("total", collect2.size());
            tabledata.put("tabledata", collect5);

            resultMap.put("tabledata", tabledata);
            resultMap.put("chartdata", collect4);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: chengzq
     * @date: 2019/7/4 0004 下午 4:49
     * @Description: 根据监测时间，污染源名称，污染物code，日期类型（实时传realtime，分钟传minute，小时传hour，日传day）查询废气信息及在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype, pollutionname, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getGasOutPutAndOnlineInfoByParams", method = RequestMethod.POST)
    public Object getGasOutPutAndOnlineInfoByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime, @RequestJson(value = "datetype") String datetype
            , @RequestJson(value = "sortdata", required = false) Object sortdata, @RequestJson(value = "pollutionname", required = false) String pollutionname, @RequestJson(value = "pollutantcode", required = false) String pollutantcode
    ) {
        try {
            DecimalFormat decimalformat = new DecimalFormat("0.#");
            List<Map<String, Object>> resultList = new ArrayList<>();

            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionname", pollutionname);
            String dgimns = getOutPutInfoDgimns(2, data, paramMap);


            OnlineDataVO onlineDataVO = new OnlineDataVO();
            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            onlineDataVO.setDataGatherCode(dgimns);

            List<OnlineDataVO> onlineData = new ArrayList<>();

            if (datetype.equals("hour")) {
                onlineData = mongoBaseService.getListByParam(onlineDataVO, "HourData", "yyyy-MM-dd HH");
            } else if (datetype.equals("day")) {
                onlineData = mongoBaseService.getListByParam(onlineDataVO, "DayData", "yyyy-MM-dd");
            } else if (datetype.equals("minute")) {
                onlineData = mongoBaseService.getListByParam(onlineDataVO, "MinuteData", "yyyy-MM-dd HH:mm");
            } else if (datetype.equals("realtime")) {
                long l = System.currentTimeMillis();
                onlineData = mongoBaseService.getListByParam(onlineDataVO, "RealTimeData", "yyyy-MM-dd HH:mm:ss");
            }
            for (Map<String, Object> datum : data) {
                if (datum.get("DGIMN") != null) {
                    String dgimn = datum.get("DGIMN").toString();
                    List<Map<String, Object>> pollutantsList = new ArrayList<>();
                    onlineData.stream().filter(m -> dgimn.equals(m.getDataGatherCode())).peek(m -> {
                        if (m.getDayDataList() != null) {
                            pollutantsList.addAll(m.getDayDataList());
                        } else if (m.getHourDataList() != null) {
                            pollutantsList.addAll(m.getHourDataList());
                        } else if (m.getMinuteDataList() != null) {
                            pollutantsList.addAll(m.getMinuteDataList());
                        } else if (m.getRealDataList() != null) {
                            pollutantsList.addAll(m.getRealDataList());
                        }
                    }).collect(Collectors.toList());
                    Double avg = pollutantsList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) && (m.get("MonitorValue") != null
                            || m.get("AvgStrength") != null)).map(m -> m.get("MonitorValue") == null ? Double.valueOf(m.get("AvgStrength").toString()) : Double.valueOf(m.get("MonitorValue").toString()))
                            .collect(Collectors.averagingDouble(m -> m));
                    Map<String, Object> output = new HashMap<>();
                    output.put("dgimn", dgimn);
                    output.put("pollutionname", datum.get("PollutionName"));
                    output.put("outputname", datum.get("OutputName"));
                    if (pollutantsList.size() > 0) {
                        output.put("monitorvalue", decimalformat.format(avg));
                    } else {
                        output.put("monitorvalue", "-");
                    }
                    resultList.add(output);
                }

            }
            List<Map<String, Object>> collect2 = new ArrayList<>();

            String sortvalue = "";
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorvalue") != null) {
                    sortvalue = jsonObject.get("monitorvalue").toString();
                }
            }

            List<Map<String, Object>> collect = resultList.stream().filter(m -> m.get("monitorvalue") != null && "-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());
            List<Map<String, Object>> collect1 = resultList.stream().filter(m -> m.get("monitorvalue") != null && !"-".equals(m.get("monitorvalue").toString())).collect(Collectors.toList());

            if ("ascending".equals(sortvalue)) {
                collect2 = collect1.stream().sorted(Comparator.comparing(m -> Float.valueOf(m.get("monitorvalue").toString()))).collect(Collectors.toList());
                List<Map<String, Object>> finalCollect = collect2;
                final int[] temp = {0};
                IntStream.range(0, collect2.size()).mapToObj(m -> m).peek(i -> {
                    if (!finalCollect.get(i).get("monitorvalue").toString().equals("0")) {
                        finalCollect.get(i).put("sortvalue", finalCollect.size() - 1 - temp[0]);
                        temp[0]++;
                    }
                }).collect(Collectors.toList());
            } else {
                collect2 = collect1.stream().sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("monitorvalue").toString())).reversed()).collect(Collectors.toList());
                List<Map<String, Object>> finalCollect1 = collect2;
                final int[] temp = {0};
                IntStream.range(0, collect2.size()).mapToObj(m -> m).peek(i -> {
                    if (!finalCollect1.get(i).get("monitorvalue").toString().equals("0")) {
                        finalCollect1.get(i).put("sortvalue", temp[0]);
                        temp[0]++;
                    }
                }).collect(Collectors.toList());

            }
            collect2.addAll(collect);
            if ("".equals(sortvalue)) {
                collect2 = resultList.stream().filter(m -> m.get("pollutionname") != null).sorted(Comparator.comparing(m -> m.get("pollutionname").toString())).collect(Collectors.toList());
            }

            return AuthUtil.parseJsonKeyToLower("success", collect2);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 上午 9:41
     * @Description: 通过监测时间，监测点mn号，污染物查询监测点在下数据及风速，风向等空气数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype, pageSize, pageNum]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDataAndAirDataByParams", method = RequestMethod.POST)
    public Object getOnlineDataAndAirDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                                  @RequestJson(value = "datetype") String datetype,
                                                  @RequestJson(value = "pollutantcode") String pollutantcode,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat df = new DecimalFormat("0.##");
            //风向平均浓度计算
            OnlineDataVO onlineDataVO = new OnlineDataVO();
            Map<String, Object> timeMap = new HashMap<>();
            List<OnlineDataVO> listByParam = new ArrayList();

            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            onlineDataVO.setDataGatherCode(dgimn);
            String pattern = "";
            if ("hour".equals(datetype)) {
                pattern = "yyyy-MM-dd HH";
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", pattern);
            } else if ("day".equals(datetype)) {
                pattern = "yyyy-MM-dd";
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", pattern);
            }

            List<String> airDgimns = otherMonitorPointService.getAirDgimnByMonitorDgimn(dgimn);

            String airDgimn = "";
            if (airDgimns.size() > 0) {
                airDgimn = airDgimns.get(0);
            }
            onlineDataVO.setDataGatherCode(airDgimn);
            List<OnlineDataVO> airData = new ArrayList<>();
            if ("hour".equals(datetype)) {
                airData = mongoBaseService.getListByParam(onlineDataVO, "HourData", pattern);
            } else if ("day".equals(datetype)) {
                airData = mongoBaseService.getListByParam(onlineDataVO, "DayData", pattern);
            }

            List<Map<String, Object>> datas = new ArrayList<>();
            for (OnlineDataVO airDatum : airData) {
                List<Map<String, Object>> hourDataList = airDatum.getHourDataList();
                List<Map<String, Object>> dayDataList = airDatum.getDayDataList();
                List<Map<String, Object>> dataList = hourDataList == null ? dayDataList : hourDataList;
                String monitorTime = OverAlarmController.format(airDatum.getMonitorTime(), pattern);
                for (OnlineDataVO dataVO : listByParam) {
                    String monitorTime1 = OverAlarmController.format(dataVO.getMonitorTime(), pattern);
                    if (monitorTime.equals(monitorTime1)) {
                        List<String> collect = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && WindDirectionEnum.getCode().
                                equals(m.get("PollutantCode").toString())).map(m -> m.get("AvgStrength").toString()).collect(Collectors.toList());
                        if (collect.size() > 0) {
                            Double AvgStrength = Double.valueOf(collect.get(0));
                            String code = DataFormatUtil.windDirectionSwitch(AvgStrength, "code");

                            List<Map<String, Object>> hourstrengthdata = dataVO.getHourDataList();
                            List<Map<String, Object>> daystrengthdata = dataVO.getDayDataList();

                            List<Map<String, Object>> strengthdata = hourstrengthdata == null ? daystrengthdata : hourstrengthdata;

                            Double collect1 = strengthdata.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString())).
                                    map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));
                            Map<String, Object> data = new HashMap<>();
                            data.put("winddirection", code);
                            data.put("AvgStrength", collect1);
                            datas.add(data);
                        }
                    }
                }
            }

            Map<String, List<Map<String, Object>>> winddirection = datas.stream().collect(Collectors.groupingBy(m -> m.get("winddirection").toString()));


            List<Map<String, Object>> strengthdatas = new ArrayList<>();
            for (String code : winddirection.keySet()) {
                List<Map<String, Object>> list = winddirection.get(code);

                Double avgStrength = list.stream().map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));

                Map<String, Object> datua = new HashMap<>();
                datua.put("winddirection", code);
                datua.put("avgstrength", df.format(avgStrength));
                strengthdatas.add(datua);
            }


            //下面是风速，风向，次数统计信息
            paramMap.put("sort", "asc");
            paramMap.put("mns", Arrays.asList(airDgimn));
            List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                    , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
            String collection = "";
            if ("hour".equals(datetype)) {
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59:59";
                    paramMap.put("endtime", endtime);
                }
                collection = MongoDataUtils.getCollectionByDataMark(3);
            } else if ("day".equals(datetype)) {
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + " 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                collection = MongoDataUtils.getCollectionByDataMark(4);
            }

            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("collection", collection);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = MongoDataUtils.countWindChartData(documents, collection);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("strengthdata", strengthdatas);
            resultMap.put("winddata", dataList);


            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/2 0002 上午 9:11
     * @Description: 通过监测时间，监测点mn号，污染物查询恶臭监测点的风向、风速、浓度等信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype, pageSize, pageNum]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDataAndWeatherDataByParams", method = RequestMethod.POST)
    public Object getOnlineDataAndWeatherDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                                      @RequestJson(value = "datetype") String datetype,
                                                      @RequestJson(value = "pollutantcode") String pollutantcode,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("datetype", datetype);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> resultlist = onlineService.countStenchOnlineDataAndWeatherDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 下午 3:41
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, datetype, pollutantcode, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDataAndAirDatasByParams", method = RequestMethod.POST)
    public Object getOnlineDataAndAirDatasByParams(@RequestJson(value = "dgimn") String dgimn,
                                                   @RequestJson(value = "datetype") String datetype,
                                                   @RequestJson(value = "pollutantcode") String pollutantcode,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime) {
        //1.通过mn号，时间查询监测点在线数据
        OnlineDataVO onlineDataVO = new OnlineDataVO();
        Map<String, Object> timeMap = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<OnlineDataVO> listByParam = new ArrayList();

        timeMap.put("starttime", starttime);
        timeMap.put("endtime", endtime);
        onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
        onlineDataVO.setDataGatherCode(dgimn);
        String pattern = "";
        if ("hour".equals(datetype)) {
            pattern = "yyyy-MM-dd HH";
            listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", pattern);
        } else if ("day".equals(datetype)) {
            pattern = "yyyy-MM-dd";
            listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", pattern);
        }
        List<String> airDgimns = otherMonitorPointService.getAirDgimnByStinkMonitorDgimn(dgimn);

        String airDgimn = "";
        if (airDgimns.size() > 0) {
            airDgimn = airDgimns.get(0);
        }
        onlineDataVO.setDataGatherCode(airDgimn);
        List<OnlineDataVO> airData = new ArrayList<>();
        if ("hour".equals(datetype)) {
            airData = mongoBaseService.getListByParam(onlineDataVO, "HourData", pattern);
        } else if ("day".equals(datetype)) {
            airData = mongoBaseService.getListByParam(onlineDataVO, "DayData", pattern);
        }
        for (OnlineDataVO Datum : airData) {
            List<Map<String, Object>> hourDataList = Datum.getHourDataList();
            List<Map<String, Object>> dayDataList = Datum.getDayDataList();
            List<Map<String, Object>> datalist = hourDataList == null ? dayDataList : hourDataList;
            String datatime = OverAlarmController.format(Datum.getMonitorTime(), pattern);

            for (OnlineDataVO datum : listByParam) {
                List<Map<String, Object>> hourDataList1 = datum.getHourDataList();
                List<Map<String, Object>> dayDataList1 = datum.getDayDataList();
                List<Map<String, Object>> datas = hourDataList1 == null ? dayDataList1 : hourDataList1;
                String airtime = OverAlarmController.format(datum.getMonitorTime(), pattern);
                if (datatime.equals(airtime)) {
                    //获取浓度
                    Double collect = datas.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString()))
                            .map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));
                    //获取风向
                    Double collect1 = datalist.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && WindDirectionEnum.getCode().equals(m.get("PollutantCode").toString()))
                            .map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));
                    //获取风速
                    Double collect2 = datalist.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(m.get("PollutantCode").toString()))
                            .map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));

                    String windDirection = DataFormatUtil.windDirectionSwitch(collect1, "code");
                    String windDirectionname = DataFormatUtil.windDirectionSwitch(collect1, "name");
                    String windSpeed = DataFormatUtil.windSpeedSwitch(collect2, "value");

                    Map<String, Object> result = new HashMap<>();

                    result.put("monitortime", datatime);
                    result.put("windDirection", windDirection);
                    result.put("windDirectionname", windDirectionname);
                    result.put("windDirectionvalue", collect1);
                    result.put("windSpeed", windSpeed);
                    result.put("windSpeedValue", collect2);
                    result.put("Strength", collect);
                    resultList.add(result);
                }
            }
        }
        List<Map<String, Object>> collect = resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList());
        return AuthUtil.parseJsonKeyToLower("success", collect);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 2:21
     * @Description: 获取废水或废气排口所有mn号字符串
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type, data, paramMap]
     * @throws:
     */
    private String getOutPutInfoDgimns(Integer type, List<Map<String, Object>> data, Map<String, Object> paramMap) {
        if (type == 1) {
            data.addAll(waterOutPutInfoService.getAllOutPutInfoByType(paramMap));
        } else if (type == 2) {
            data.addAll(gasOutPutInfoService.getAllOutPutInfo(paramMap));
        }
        return data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.joining(","));
    }


    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 3:37
     * @Description:根据监测年份获取排放量许可预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [year]
     */
    @RequestMapping(value = "getPermittedFlowListDataByYear", method = RequestMethod.POST)
    public Object getPermittedFlowListDataByYear(@RequestJson(value = "year") String year,
                                                 @RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("flowyear", year);
            paramMap.put("monitorpointtype", monitorpointtype);
            //根据年份和监测点类型获取各企业该类型排口各污染物的总排放许可限值
            List<Map<String, Object>> allflowvalues = entPermittedFlowLimitValueService.getEntPermittedFlowLimitInfoByYearAndType(paramMap);
            //根据年份和类型获取配置有排放量许可预警值的所有企业下排口的MN号
            List<Map<String, Object>> allMN = entPermittedFlowLimitValueService.getAllDgimnsByYearAndType(paramMap);
            //从MongoDB中查询因子排放量信息，并统计计算，拼接后返回列表信息
            List<Map<String, Object>> resultlist = gasOutPutInfoService.getFlowPermitListData(allflowvalues, allMN, year);
            List<Map<String, Object>> collect = resultlist.stream().sorted(Comparator.comparing(m -> m.get("surplusflowday").toString())).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/10 0010 下午 2:26
     * @Description:根据自定义参数和数据类型获取浓度阈值预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getConcentrationThresholdEarlyListDataByParams", method = RequestMethod.POST)
    public Object getConcentrationThresholdEarlyListDataByParams(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                 @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                                 @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                                 @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                                 @RequestJson(value = "outputname", required = false) String outputname,
                                                                 @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                                 @RequestJson(value = "datatype", required = false) List<String> datatypes,
                                                                 @RequestJson(value = "starttime") String starttime,
                                                                 @RequestJson(value = "endtime") String endtime,
                                                                 @RequestJson(value = "dataflag", required = false) String dataflag,
                                                                 @RequestJson(value = "dgimn", required = false) String dgimn,
                                                                 @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                                 @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutantname", pollutantname);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("dataflag", dataflag);
            paramMap.put("dgimn", dgimn);
            paramMap.put("codes", pollutantcodes);
            if (datatypes == null) {
                datatypes = new ArrayList<>();
            }
            //从MongoDB中查询因子浓度预警信息，返回列表数据
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            resultMap = onlineService.getConcentrationThresholdEarlyListData(paramMap, starttime, endtime, datatypes, monitorpointtype, usercode, pagenum, pagesize);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 1:03
     * @Description:根据自定义参数和数据类型导出浓度阈值预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "exportConcentrationThresholdEarly", method = RequestMethod.POST)
    public void exportConcentrationThresholdEarly(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                  @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                  @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                  @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                  @RequestJson(value = "outputname", required = false) String outputname,
                                                  @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                  @RequestJson(value = "datatype") List<String> datatypes,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //根据监测点类型获取浓度阈值预警表头数据
            List<Map<String, Object>> tabletitledata = gasOutPutInfoService.getTableTitleForGasConcentrationThresholdEarly(monitorpointtype);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutantname", pollutantname);
            //从MongoDB中查询因子浓度预警信息，返回列表数据
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            Map<String, Object> resultlist = onlineService.getConcentrationThresholdEarlyListData(paramMap, starttime, endtime, datatypes, monitorpointtype, usercode, null, null);
            List<Map<String, Object>> collect = (List<Map<String, Object>>) resultlist.get("datalist");
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtype, "early");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/12 0012 上午 11:47
     * @Description:根据监测点类型和数据类型以及自定义参数获取该类型异常报警数据（废气、废水、雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getExceptionDataByMonitorPointTypeAndParams", method = RequestMethod.POST)
    public Object getExceptionDataByMonitorPointTypeAndParams(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                              @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                              @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                              @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                              @RequestJson(value = "outputname", required = false) String outputname,
                                                              @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                              @RequestJson(value = "datatype", required = false) List<String> datatypes,
                                                              @RequestJson(value = "exceptiontype", required = false) List<String> exceptiontype,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime,
                                                              @RequestJson(value = "dataflag", required = false) String dataflag,
                                                              @RequestJson(value = "dgimn", required = false) String dgimn,
                                                              @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                              @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                                              @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                              @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutantname", pollutantname);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("dataflag", dataflag);
            paramMap.put("dgimn", dgimn);
            paramMap.put("codes", pollutantcodes);
            if (datatypes == null) {
                datatypes = new ArrayList<>();
            }
            //根据监测点类型和自定义参数获取各企业下各排口监测的污染物信息
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            resultMap = onlineService.getExceptionListDataByParam(paramMap, starttime, endtime, datatypes, exceptiontype, monitorpointtype, usercode, pagenum, pagesize);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/12 0012 上午 11:47
     * @Description:根据监测点类型和数据类型以及自定义参数导出获取该类型异常报警数据（废气、废水、雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportExceptionListData", method = RequestMethod.POST)
    public void exportExceptionListData(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                        @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                        @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                        @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                        @RequestJson(value = "outputname", required = false) String outputname,
                                        @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                        @RequestJson(value = "datatype") List<String> datatypes,
                                        @RequestJson(value = "exceptiontype", required = false) List<String> exceptiontype,
                                        @RequestJson(value = "starttime") String starttime,
                                        @RequestJson(value = "endtime") String endtime,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //根据监测点类型获取浓度阈值预警表头数据
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForException(monitorpointtype);
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutantname", pollutantname);
            //根据监测点类型和自定义参数获取各企业下各排口监测的污染物信息
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            Map<String, Object> resultlist = onlineService.getExceptionListDataByParam(paramMap, starttime, endtime, datatypes, exceptiontype, monitorpointtype, usercode, null, null);
            //获取排序字段
            List<Map<String, Object>> collect = (List<Map<String, Object>>) resultlist.get("datalist");
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtype, "exception");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 上午 11:47
     * @Description:根据监测点类型和数据类型以及自定义参数获取该类型超标（超限）报警数据（废气、废水、雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getOverDataByMonitorPointTypeAndParams", method = RequestMethod.POST)
    public Object getOverDataByMonitorPointTypeAndParams(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                         @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                         @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                         @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                         @RequestJson(value = "outputname", required = false) String outputname,
                                                         @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                         @RequestJson(value = "datatype", required = false) List<String> datatypes,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime,
                                                         @RequestJson(value = "dataflag", required = false) String dataflag,
                                                         @RequestJson(value = "dgimn", required = false) String dgimn,
                                                         @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                         @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                                         @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                         @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutantname", pollutantname);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("dataflag", dataflag);
            paramMap.put("dgimn", dgimn);
            paramMap.put("codes", pollutantcodes);
            if (datatypes == null) {
                datatypes = new ArrayList<>();
            }
            //从MongoDB中查询数据
//            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            String usercode = "5";
            resultMap = onlineService.getOverListDataByParam(paramMap, starttime, endtime, datatypes, monitorpointtype, usercode, pagenum, pagesize);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/12 0012 上午 11:47
     * @Description:根据监测点类型和数据类型以及自定义参数导出该类型超标（超限）报警数据（废气、废水、雨水）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportOverAlarmListData", method = RequestMethod.POST)
    public void exportOverAlarmListData(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                        @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                        @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                        @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                        @RequestJson(value = "outputname", required = false) String outputname,
                                        @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                        @RequestJson(value = "datatype") List<String> datatypes,
                                        @RequestJson(value = "starttime") String starttime,
                                        @RequestJson(value = "endtime") String endtime,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //根据监测点类型获取浓度阈值预警表头数据
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForOverAlarm(monitorpointtype);
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutantname", pollutantname);
            //从MongoDB中查询数据
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            resultMap = onlineService.getOverListDataByParam(paramMap, starttime, endtime, datatypes, monitorpointtype, usercode, null, null);
            List<Map<String, Object>> listData = (List<Map<String, Object>>) resultMap.get("datalist");
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtype, "over");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, listData, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/13 0013 下午 2:54
     * @Description: 根据监测点类型和备注字段获取导出EXCEL表名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private String exportExcelTableNameByMonitorPointTypeAndRemark(Integer type, String remark) {
        String fileName = "";
        String str = "";
        if ("early".equals(remark)) {
            str = "浓度阈值预警";
        } else if ("exception".equals(remark)) {
            str = "数据异常报警";
        } else if ("over".equals(remark)) {
            str = "数据超限报警";
        }
        if (type == WasteGasEnum.getCode()) {//废气
            fileName = "废气" + str + "导出文件_" + new Date().getTime();
        } else if (type == SmokeEnum.getCode()) {//废气
            fileName = "烟气" + str + "导出文件_" + new Date().getTime();
        } else if (type == WasteWaterEnum.getCode()) {//废水
            fileName = "废水" + str + "导出文件_" + new Date().getTime();
        } else if (type == RainEnum.getCode()) {//雨水
            fileName = "雨水" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
            fileName = "厂界小型站" + str + "导出文件_" + new Date().getTime();
        } else if (type == FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
            fileName = "厂界恶臭" + str + "导出文件_" + new Date().getTime();
        } else if (type == EnvironmentalDustEnum.getCode()) {//扬尘
            fileName = "扬尘" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
            fileName = "空气站" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
            fileName = "恶臭" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {//微站
            fileName = "恶臭" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
            fileName = "Voc" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质
            fileName = "水质" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.StorageTankAreaEnum.getCode()) {//储罐
            fileName = "储罐" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.SecurityLeakageMonitor.getCode()) {//安全泄露
            fileName = "安全泄露" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.SecurityCombustibleMonitor.getCode()) {//可燃易爆气体
            fileName = "可燃易爆气体" + str + "导出文件_" + new Date().getTime();
        } else if (type == CommonTypeEnum.MonitorPointTypeEnum.SecurityToxicMonitor.getCode()) {//有毒有害气体
            fileName = "有毒有害气体" + str + "导出文件_" + new Date().getTime();
        }

        return fileName;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:08
     * @Description: 根据时间、监测点类型和污染物Code统计该污染物排放排放数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countDischargeByParam", method = RequestMethod.POST)
    public Object countDischargeByParam(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                        @RequestJson(value = "starttime") String starttime,
                                        @RequestJson(value = "endtime") String endtime,
                                        @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("monitortype", monitorpointtype);
            paramMap.put("remindtype", FlowChangeEnum.getCode());
            Map<String, Map<String, Object>> result = onlineService.countDischargeByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/22 0022 下午 1:43
     * @Description: 获取各个监测点类型的预警报警统计数据
     * @updateUser:xsm
     * @updateDate:2020/12/24 0024 下午 4:58
     * @updateDescription: 恶臭区分敏感点和传输点
     * @param:
     * @return:
     */
    @RequestMapping(value = "countToadyAlarmNumAndHB", method = RequestMethod.POST)
    public Object countToadyAlarmNumAndHB(@RequestJson(value = "starttime", required = false) String starttime,
                                          @RequestJson(value = "endtime", required = false) String endtime,
                                          @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                          @RequestJson(value = "monitorpointcategorys", required = false) List<Integer> monitorpointcategorys) {
        try {
            if (starttime != null && endtime != null) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
            } else {
                Date nowDay = new Date();
                String ymd = DataFormatUtil.getDateYMD(nowDay);
                starttime = ymd + " 00:00:00";
                endtime = ymd + " 23:59:59";
            }
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<String> mns = new ArrayList<>();

            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                if (dgimns != null) {
                    for (Integer type : monitorpointtypes) {
                        List<String> tempMns = new ArrayList<>();
                        if (type != CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()&&type != CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                            tempMns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(new ArrayList<>(), type, new HashMap<>());
                        } else {//判断未恶臭点位时 区分敏感传输点
                            if (monitorpointcategorys != null && monitorpointcategorys.size() > 0) {
                                tempMns = onlineService.getMNsAndSetStinkIdAndMnByCategorys(new HashMap<>(), monitorpointcategorys);
                            }else{
                                tempMns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(new ArrayList<>(), type, new HashMap<>());
                            }
                        }

                        for (String tempMn : tempMns) {
                            if (dgimns.contains(tempMn)) {
                                mns.add(tempMn);
                            }
                        }
                    }

                }
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("issuddenchange", true);

            Map<String, Object> currData = alarmDataByParam(paramMap);
            if (currData.get("pollutantcodes") != null) {
                currData.put("pollutantnames", getPollutantNameByCodes(currData.get("pollutantcodes"), monitorpointtypes));
            }
            return AuthUtil.parseJsonKeyToLower("success", currData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/22 0022 下午 5:11
     * @Description: 污染物code转换name
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Set<String> getPollutantNameByCodes(Object pollutantcodes, List<Integer> monitorpointtypes) {
        Set<String> names = new HashSet<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("codes", pollutantcodes);
        paramMap.put("pollutanttypes", monitorpointtypes);
        List<Map<String, Object>> mapList = pollutantService.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : mapList) {
            names.add(map.get("name").toString());
        }
        return names;
    }

    /**
     * @author: xsm
     * @date: 2019/12/18 0018 上午 11:45
     * @Description: 污染物code转换name 且添加各个污染物的异常类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Set<String> getPollutantNameAndExceptionTypeByCodes(Object pollutantcodes, List<Map<String, Object>> exceptiontypes, List<Integer> monitorpointtypes) {
        Set<String> names = new HashSet<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("codes", pollutantcodes);
        paramMap.put("pollutanttypes", monitorpointtypes);
        List<Map<String, Object>> mapList = pollutantService.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : mapList) {
            String str = "";
            for (Map<String, Object> obj : exceptiontypes) {
                if ((map.get("code").toString().equals(obj.get("pollutantcode").toString()))) {
                    str += CommonTypeEnum.ExceptionTypeEnum.getNameByCode(obj.get("exceptiontype").toString()) + "、";
                }
            }
            str = str.substring(0, str.length() - 1);
            names.add(map.get("name").toString() + "【" + str + "】");
        }
        return names;
    }

    /**
     * @author: lip
     * @date: 2019/8/22 0022 下午 4:51
     * @Description: 自定义查询条件获取报警预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> alarmDataByParam(Map<String, Object> paramMap) {
        Set<String> codes = new HashSet<>();
        //1，浓度突变
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("collection", "HourData");
        paramMap.put("unwindkey", "HourDataList");
        List<Document> conChange = countPollutantByParam(paramMap);
        int conNum = sumCountData(conChange, codes);
        //2，排放量突变预警
        paramMap.put("timeKey", "MonitorTime");
        paramMap.put("collection", "HourFlowData");
        paramMap.put("unwindkey", "HourFlowDataList");
        List<Document> flowChange = countPollutantByParam(paramMap);
        int flowNum = sumCountData(flowChange, codes);
        //3，超阈值预警
        paramMap.remove("unwindkey");
        paramMap.put("timeKey", "EarlyWarnTime");
        paramMap.put("collection", "EarlyWarnData");
        List<Document> CYZData = countPollutantByParam(paramMap);
        int CYZNum = sumCountData(CYZData, codes);
        //4，数据超限
        paramMap.put("timeKey", "OverTime");
        paramMap.put("collection", "OverData");
        List<Document> overData = countPollutantByParam(paramMap);
        int overNum = sumCountData(overData, codes);

        //5，数据异常  （修改：剔除无流量异常 只显示其它异常类型数据 只适用废水监测类型）
        paramMap.put("timeKey", "ExceptionTime");
        paramMap.put("collection", "ExceptionData");
        List<Document> exceptionData = countPollutantByParam(paramMap);
        int exceptionNum = sumCountData(exceptionData, codes);

        //6.废水无流量异常
        paramMap.put("timeKey", "ExceptionTime");
        paramMap.put("collection", "ExceptionData");
        List<Document> noflowexceptionData = countWaterNoFlowExceptionPollutantByParam(paramMap);
        int noflowexceptionNum = sumCountData(noflowexceptionData, codes);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("conchangenum", conNum);
        resultMap.put("flowchangenum", flowNum);
        resultMap.put("cyznum", CYZNum);
        resultMap.put("overnum", overNum);
        resultMap.put("exceptionnum", exceptionNum);
        resultMap.put("waternoflownum", noflowexceptionNum);
        int currtotalnum = conNum + flowNum + CYZNum + overNum + exceptionNum + noflowexceptionNum;
        resultMap.put("currtotalnum", currtotalnum);
        if (codes.size() > 0) {
            resultMap.put("pollutantcodes", codes);
        }
        return resultMap;
    }

    /**
     * @author: lip
     * @date: 2019/10/30 0030 下午 5:19
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private int sumCountData(List<Document> documents, Set<String> codes) {
        int sumNum = 0;
        String pollutantCode;
        for (Document document : documents) {
            sumNum += document.getInteger("countnum");
            pollutantCode = document.getString("PollutantCode");
            codes.add(pollutantCode);
        }
        return sumNum;
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
    private List<Document> countPollutantByParam(Map<String, Object> paramMap) {

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
        //当为异常数据时 去掉无流量异常
        Criteria criteria = new Criteria();
        if (paramMap.get("collection") != null && (paramMap.get("collection").toString()).equals("ExceptionData")) {
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
        } else {
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        }
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        Fields fields;
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            fields = fields("DataGatherCode").and("PollutantCode", unwindkey + ".PollutantCode");
        } else {
            fields = fields("DataGatherCode", "PollutantCode");
        }
        aggregations.add(project(fields));
        GroupOperation groupOperation = group("DataGatherCode", "PollutantCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/8 16:38
     * @Description: 根据条件获取小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAlarmHourDataByParam", method = RequestMethod.POST)
    public Object getAlarmHourDataByParam(@RequestJson(value = "monitorpointtype") Integer monitorPointType,
                                          @RequestJson(value = "pollutantcode") String pollutantCode,
                                          @RequestJson(value = "time") String time) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //现根据监测点类型获取mn号等相关信息
            CommonTypeEnum.MonitorPointTypeEnum codeByInt = getCodeByInt(monitorPointType);
            if (codeByInt == null) {
                return resultList;
            }
            //根据监测点类型和污染物获取监测点
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorPointType);
            paramMap.put("pollutantcode", pollutantCode);
            List<Map<String, Object>> outputs = onlineService.getAllOutputDgimnAndPollutantInfosByParam(paramMap);
            //所有MN号
            Date monitorTime = DataFormatUtil.parseDate(time);
            Set<String> mns = outputs.stream().map(output -> output.get("DGIMN").toString()).collect(Collectors.toSet());
            Fields fields = fields("DataGatherCode",
                    "MonitorTime",
                    "HourDataList.PollutantCode",
                    "HourDataList.AvgStrength",
                    "HourDataList.IsOver",
                    "_id");
            Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").is(monitorTime).and("PollutantCode").is(pollutantCode);
            List<Document> documents = mongoTemplate.aggregate(newAggregation(
                    unwind("HourDataList"),
                    project(fields),
                    match(criteria)), "HourData", Document.class).getMappedResults();
            for (Map<String, Object> output : outputs) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollutionid", output.get("PK_PollutionID"));
                map.put("pollutionname", output.get("PollutionName"));
                map.put("monitorpointid", output.get("PK_ID"));
                map.put("monitorpointname", output.get("MonitorPointName"));
                map.put("longitude", output.get("Longitude"));
                map.put("latitude", output.get("Latitude"));
                map.put("state", output.get("Status"));
                Object isover = -1;
                Object value = null;
                for (Document document : documents) {
                    if (output.get("DGIMN").equals(document.getString("DataGatherCode"))) {
                        isover = document.get("IsOver");
                        value = document.get("AvgStrength");
                        break;
                    }
                }
                map.put("isover", isover);
                map.put("value", value);
                resultList.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:08
     * @Description: 统计该污染物近一年废气污染物排放量及浓度变化趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countDischargeAndDensityByPollutantCode", method = RequestMethod.POST)
    public Object countDischargeAndDensityByPollutantCode(
            @RequestJson(value = "pollutantcode") String pollutantCode,
            @RequestJson(value = "monitorpointtype") Integer monitorPointType) {
        try {
            //现根据监测点类型获取mn号等相关信息
            CommonTypeEnum.MonitorPointTypeEnum codeByInt = getCodeByInt(monitorPointType);
            if (codeByInt == null) {
                return AuthUtil.parseJsonKeyToLower("success", "");
            }
            LocalDate localDate = LocalDate.now();
            String endtime = localDate.toString();
            String starttime = localDate.plusYears(-1).toString();
            Map<String, Map<String, Object>> result = onlineService.countDischargeAndDensityByCodeAndMns(pollutantCode, starttime, endtime, "", monitorPointType);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/9 14:10
     * @Description: 通过监测点类型和时间获取排污大户排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEntDischargeRankByParam", method = RequestMethod.POST)
    public Object countEntDischargeRankByParam(@RequestJson(value = "monitorpointtype",required = false) Integer monitorPointType,
                                               @RequestJson(value = "monitorpointtypes",required = false) List<Integer> monitorPointTypes,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "pollutantcode") String pollutantCode,
                                               @RequestJson(value = "endtime") String endtime) {

        try {
            //现根据监测点类型获取mn号等相关信息
            if (monitorPointTypes ==null||monitorPointTypes.size()==0){
                monitorPointTypes = new ArrayList<>();
                if (monitorPointType!=null) {
                    monitorPointTypes.add(monitorPointType);
                }
            }
            if (monitorPointTypes.size()>0 &&(monitorPointTypes.contains(WasteWaterEnum.getCode())||monitorPointTypes.contains(RainEnum.getCode())||monitorPointTypes.contains(WasteGasEnum.getCode())||
                    monitorPointTypes.contains(SmokeEnum.getCode()))){
                Date start = DataFormatUtil.parseDate(starttime + "-01 00:00:00");
                Date end = DataFormatUtil.parseDate(DataFormatUtil.getYearMothLast(endtime) + " 23:59:59");
                List<Map<String, Object>> resultList = onlineService.countEntDischargeRankByParam(pollutantCode, start, end, monitorPointTypes);
                double count = 0;
                if (resultList.size() > 0) {
                    count = resultList.stream().map(m -> Double.parseDouble(m.get("flow").toString())).reduce(Double::sum).get();
                }
                if (count != 0) {
                    //占比
                    for (Map<String, Object> map : resultList) {
                        map.put("rate", BigDecimal.valueOf(Double.parseDouble(map.get("flow").toString()) / count * 100).setScale(2, ROUND_HALF_DOWN).toString());
                    }
                } else {
                    resultList.forEach(map -> map.put("rate", 0));
                }
                List<Map<String, Object>> resultListInfo = new ArrayList<>();
                for (Map<String, Object> map : resultList) {
                    if (resultListInfo.size() == 10) {
                        break;
                    }
                    resultListInfo.add(map);
                }
                return AuthUtil.parseJsonKeyToLower("success", resultListInfo);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/11/14 14:10
     * @Description: 通过监测点类型和时间获取企日累计排放量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEntDayDischargeRankByParam", method = RequestMethod.POST)
    public Object countEntDayDischargeRankByParam(@RequestJson(value = "monitorpointtype",required = false) Integer monitorPointType,
                                               @RequestJson(value = "monitorpointtypes",required = false) List<Integer> monitorPointTypes,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "pollutantcode") String pollutantCode,
                                               @RequestJson(value = "endtime") String endtime) {

        try {
            //现根据监测点类型获取mn号等相关信息
            if (monitorPointTypes ==null||monitorPointTypes.size()==0){
                monitorPointTypes = new ArrayList<>();
                if (monitorPointType!=null) {
                    monitorPointTypes.add(monitorPointType);
                }
            }
            if (monitorPointTypes.size()>0 &&(monitorPointTypes.contains(WasteWaterEnum.getCode())||monitorPointTypes.contains(RainEnum.getCode())||monitorPointTypes.contains(WasteGasEnum.getCode())||
                    monitorPointTypes.contains(SmokeEnum.getCode()))){
                Date start = DataFormatUtil.parseDate(starttime + "-01 00:00:00");
                Date end = DataFormatUtil.parseDate(DataFormatUtil.getLastDayOfMonth(starttime) + " 23:59:59");
                List<Map<String, Object>> resultList = onlineService.countEntDayDischargeRankByParam(pollutantCode, start, end, monitorPointTypes);
                double count = 0;
                if (resultList.size() > 0) {
                    count = resultList.stream().map(m -> Double.parseDouble(m.get("flow").toString())).reduce(Double::sum).get();
                }
                if (count != 0) {
                    //占比
                    for (Map<String, Object> map : resultList) {
                        map.put("rate", BigDecimal.valueOf(Double.parseDouble(map.get("flow").toString()) / count * 100).setScale(2, ROUND_HALF_DOWN).toString());
                    }
                } else {
                    resultList.forEach(map -> map.put("rate", 0));
                }
                List<Map<String, Object>> resultListInfo = new ArrayList<>();
                for (Map<String, Object> map : resultList) {
                    if (resultListInfo.size() == 10) {
                        break;
                    }
                    resultListInfo.add(map);
                }
                return AuthUtil.parseJsonKeyToLower("success", resultListInfo);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/10 15:52
     * @Description: 根据监测点类型获取重点污染物排放量情况（废水废气雨水）
     * @updateUser:  wastewatertype废水类型，null废水，1出水口，2进水口
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getKeyPollutionsDischargeByMonitorPointType", method = RequestMethod.POST)
    public Object getKeyPollutionsDischargeByMonitorPointType(@RequestJson(value = "monitorpointtype") Integer monitorPointType,
                                                              @RequestJson(value = "inputoroutput",required = false) Integer inputoroutput) {
        //现根据监测点类型获取mn号等相关信息
        try {
            CommonTypeEnum.MonitorPointTypeEnum codeByInt = getCodeByInt(monitorPointType);
            if (codeByInt == WasteWaterEnum || codeByInt == RainEnum || codeByInt == WasteGasEnum || codeByInt == SmokeEnum) {
                LocalDate localDate = LocalDate.now();
                Date start = DataFormatUtil.parseDate(localDate.toString() + " 00:00:00");
                Date end = DataFormatUtil.parseDate(localDate.toString() + " 23:59:59");
                List<Map<String, Object>> resultList = onlineService.getKeyPollutionsDischargeByMonitorPointType(start, end, monitorPointType,inputoroutput);
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/12 15:18
     * @Description: 获取所有污染源数据，按报警标记字段排序
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionListDataByPollutionids", method = RequestMethod.POST)
    public Object getPollutionListDataByPollutionids(@RequestJson(value = "pollutionids") List<String> pollutionids,
                                                     @RequestJson(value = "searchtype", required = false) String searchtype,
                                                     @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                     @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        Map<String, Object> parammap = new HashMap<>();
        Map<String, Object> resultmap = new HashMap<>();

        String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        parammap.put("userid", userId);
        parammap.put("pollutionids", pollutionids);
        if (pagesize != null && pagenum != null) {
            PageHelper.startPage(pagenum, pagesize);
        }
        //获取所有污染源企业
        if (searchtype != null) {
            if ("online".equals(searchtype)) {//在线
                parammap.put("searchtype", searchtype);
            } else if ("offline".equals(searchtype)) {//离线
                parammap.put("searchtype", searchtype);
            } else if ("licence".equals(searchtype)) {//拥有排污许可证
                parammap.put("searchtype", searchtype);
            }
        }
        parammap.put("monitorpointtype", monitorpointtype);
        List<Map<String, Object>> pollutionlist = pollutionService.getAllPollutionInfoByPollutionids(parammap);
        //获取分页信息
        PageInfo<Map<String, Object>> pageinfo = new PageInfo<>(pollutionlist);
        resultmap.put("total", pageinfo.getTotal());
        resultmap.put("datalist", pollutionlist);
        return AuthUtil.parseJsonKeyToLower("success", resultmap);
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 2:40
     * @Description: 通过自定义条件查询在线小时突变数据
     * @updateUser:xsm
     * @updateDate:2019/12/26 0026 下午 1:58
     * @updateDescription:排口名称附加排口停产、是否监控状态
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDatasByParamMap", method = RequestMethod.POST)
    public Object getOnlineDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws ParseException {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            //提醒类型：1表示浓度突变，2表示排放量突变，3表示预警，4表示异常，5表示超限,6表示排放量许可

            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            JSONObject paramMap = JSONObject.fromObject(paramsjson);
            Integer remindtype = null;
            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            Integer monitortype = 0;
            List<Integer> monitortypes = new ArrayList<>();
            monitortypes.add(WasteWaterEnum.getCode());
            monitortypes.add(WasteGasEnum.getCode());
            monitortypes.add(SmokeEnum.getCode());
            monitortypes.add(RainEnum.getCode());
            monitortypes.add(unOrganizationWasteGasEnum.getCode());
            monitortypes.add(FactoryBoundarySmallStationEnum.getCode());
            monitortypes.add(FactoryBoundaryStinkEnum.getCode());
            monitortypes.add(StorageTankAreaEnum.getCode());
            monitortypes.add(SecurityLeakageMonitor.getCode());//安全泄露监测
            monitortypes.add(SecurityCombustibleMonitor.getCode());//可燃易爆气体
            monitortypes.add(SecurityToxicMonitor.getCode());//有毒有害气体
            if (paramMap.get("monitortype") != null) {
                monitortype = Integer.valueOf(paramMap.get("monitortype").toString());
            }
            paramMap.put("userid", userid);

            if (monitortypes.contains(monitortype)) {
                monitorInfo = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
            } else if (monitortype == AirEnum.getCode()) {//大气
                monitorInfo = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            } else if (monitortype == EnvironmentalVocEnum.getCode() ||
                    monitortype == EnvironmentalStinkEnum.getCode() ||
                    monitortype == EnvironmentalDustEnum.getCode() ||
                    monitortype == MicroStationEnum.getCode()) {//voc//恶臭//微站
                monitorInfo = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            } else if (monitortype == WaterQualityEnum.getCode()) {//水质
                monitorInfo = waterStationService.getWaterStationByParamMap(paramMap);
            }

            //添加rtsp
            Map<String, Object> params = new HashMap<>();
            List<String> monitorpointids = monitorInfo.stream().filter(m -> m.get("pk_id") != null).map(m -> m.get("pk_id").toString()).collect(Collectors.toList());
            params.put("monitorpointids", monitorpointids);
            params.put("monitorpointtype", monitortype);

            List<Map<String, Object>> videoCameraInfoByParamMap = videoCameraService.getVideoCameraInfoByParamMap(params);


            for (Map<String, Object> map : monitorInfo) {
                List<Map<String, Object>> cameras = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : videoCameraInfoByParamMap) {
                    if (map.get("pk_id") != null && stringObjectMap.get("monitorpointid") != null && map.get("pk_id").toString().equals(stringObjectMap.get("monitorpointid").toString())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("rtsp", stringObjectMap.get("rtsp"));
                        data.put("id", stringObjectMap.get("PK_VedioCameraID"));
                        data.put("name", stringObjectMap.get("VedioCameraName"));
                        cameras.add(data);
                    }
                }
                map.put("rtsplist", cameras);
            }

            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());


            paramMap.put("dgimns", dgimns);
            paramMap.put("usercode", usercode);
            paramMap.put("pollutanttype", monitortype);
            if (paramMap.get("starttime") != null) {
                paramMap.put("starttime", paramMap.get("starttime").toString() + " 00:00:00");
            }
            if (paramMap.get("endtime") != null) {
                paramMap.put("endtime", paramMap.get("endtime").toString() + " 23:59:59");
            }
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getOnlineDataGroupMmAndMonitortime(paramMap);

            List<Map<String, Object>> data = (List) onlineDataGroupMmAndMonitortime.get("data");
            if (paramMap.get("remindtype") != null) {
                remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            }
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> map = data.get(i);
                if (map.get("DataGatherCode") != null) {
                    String dataGatherCode = map.get("DataGatherCode").toString();
                    Optional<Map<String, Object>> first = monitorInfo.stream().filter(m -> m.get("dgimn") != null && m.get("dgimn").toString().equals(dataGatherCode)).findFirst();
                    if (first.isPresent()) {
                        Map<String, Object> map1 = first.get();
                        if (map1.get("pollutionname") != null) {
                            String pollutionname = map1.get("pollutionname") == null ? "" : map1.get("pollutionname").toString();
                            String shortername = map1.get("shortername") == null ? "" : map1.get("shortername").toString();
                            String storagetankareaname = map1.get("storagetankareaname") == null ? "" : map1.get("storagetankareaname").toString();
                            map.put("pollutionname", pollutionname);
                            map.put("pollutionid", map1.get("pk_pollutionid"));
                            if (map1.get("storagetankareaname") != null) {
                                map.put("storagetankareaname", shortername + "-" + storagetankareaname);
                            }
                        }
                        if (monitortype == WasteWaterEnum.getCode() || monitortype == SmokeEnum.getCode() || monitortype == WasteGasEnum.getCode()) {//废气、废水
                            map.put("monitorname", map1.get("monitorpointname") + (map1.get("Status") == null ? "" : "0".equals(map1.get("Status").toString()) ? "【停产】" : ""));
                        } else if (monitortype == RainEnum.getCode()) {//雨水
                            map.put("monitorname", map1.get("monitorpointname") + (map1.get("Status") == null ? "" : "0".equals(map1.get("Status").toString()) ? "【未排放】" : ""));
                        } else {
                            map.put("monitorname", map1.get("monitorpointname"));
                        }
                        map.put("dgimn", dataGatherCode);
                        map.put("remindtype", paramMap.get(remindtype));
                        if (paramMap.get(remindtype) != null && (Integer.parseInt(paramMap.get(remindtype).toString()) == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode())) {
                            map.put("remindtypename", "浓度突变");
                        } else if (paramMap.get(remindtype) != null && (Integer.parseInt(paramMap.get(remindtype).toString()) == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode())) {
                            map.put("remindtypename", "排放量突变");
                        }
                        map.put("outputid", map1.get("pk_id"));
                        map.put("RTSP", map1.get("RTSP"));
                        map.put("monitorpointtype", monitortype);
                        map.put("remindtype", remindtype);
                    }
                }
            }
            resultMap.put("total", onlineDataGroupMmAndMonitortime.get("total"));
            resultMap.put("datalist", data);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 2:40
     * @Description: 通过自定义条件查询厂界恶臭和恶臭在线小时突变数据
     * @updateUser:xsm
     * @updateDate:2019/12/26 0026 下午 1:58
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getStenchOnlineDatasByParamMap", method = RequestMethod.POST)
    public Object getStenchOnlineDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws ParseException {
        try {
            Map<String, Object> resultMap = new HashMap<>();


            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            JSONObject paramMap = JSONObject.fromObject(paramsjson);
            Integer remindtype = null;
            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            List<Integer> monitortypes = new ArrayList<>();

            if (paramMap.get("monitortypes") != null) {
                monitortypes = (List<Integer>)paramMap.get("monitortypes");
            }
            paramMap.put("userid", userid);

            if (monitortypes.contains(FactoryBoundaryStinkEnum.getCode())) {
                paramMap.put("monitortype", FactoryBoundaryStinkEnum.getCode());
                monitorInfo.addAll( gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
            } else if (monitortypes.contains(EnvironmentalStinkEnum.getCode())) {//恶臭
                paramMap.put("monitortype", EnvironmentalStinkEnum.getCode());
                monitorInfo.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
            }

            //添加rtsp
            for (Integer monitortype : monitortypes) {
                Map<String, Object> params = new HashMap<>();
                List<String> monitorpointids = monitorInfo.stream().filter(m -> m.get("pk_id") != null).map(m -> m.get("pk_id").toString()).collect(Collectors.toList());
                params.put("monitorpointids", monitorpointids);
                params.put("monitorpointtype", monitortype);
                List<Map<String, Object>> videoCameraInfoByParamMap = videoCameraService.getVideoCameraInfoByParamMap(params);
                for (Map<String, Object> map : monitorInfo) {
                    List<Map<String, Object>> cameras = new ArrayList<>();
                    for (Map<String, Object> stringObjectMap : videoCameraInfoByParamMap) {
                        if (map.get("pk_id") != null && stringObjectMap.get("monitorpointid") != null && map.get("pk_id").toString().equals(stringObjectMap.get("monitorpointid").toString())) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("rtsp", stringObjectMap.get("rtsp"));
                            data.put("id", stringObjectMap.get("PK_VedioCameraID"));
                            data.put("name", stringObjectMap.get("VedioCameraName"));
                            cameras.add(data);
                        }
                    }
                    map.put("rtsplist", cameras);
                }
            }


            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("usercode", usercode);
//            paramMap.put("pollutanttype", monitortype);
            if (paramMap.get("starttime") != null) {
                paramMap.put("starttime", paramMap.get("starttime").toString() + " 00:00:00");
            }
            if (paramMap.get("endtime") != null) {
                paramMap.put("endtime", paramMap.get("endtime").toString() + " 23:59:59");
            }
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getOnlineDataGroupMmAndMonitortime(paramMap);

            List<Map<String, Object>> data = (List) onlineDataGroupMmAndMonitortime.get("data");
            if (paramMap.get("remindtype") != null) {
                remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            }
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> map = data.get(i);
                if (map.get("DataGatherCode") != null) {
                    String dataGatherCode = map.get("DataGatherCode").toString();
                    Optional<Map<String, Object>> first = monitorInfo.stream().filter(m -> m.get("dgimn") != null && m.get("dgimn").toString().equals(dataGatherCode)).findFirst();
                    if (first.isPresent()) {
                        Map<String, Object> map1 = first.get();
                        if (map1.get("pollutionname") != null) {
                            String pollutionname = map1.get("pollutionname") == null ? "" : map1.get("pollutionname").toString();
                            String shortername = map1.get("shortername") == null ? "" : map1.get("shortername").toString();
                            String storagetankareaname = map1.get("storagetankareaname") == null ? "" : map1.get("storagetankareaname").toString();
                            map.put("pollutionname", pollutionname);
                            map.put("pollutionid", map1.get("pk_pollutionid"));
                            if (map1.get("storagetankareaname") != null) {
                                map.put("storagetankareaname", shortername + "-" + storagetankareaname);
                            }
                        }
                        map.put("monitorname", map1.get("monitorpointname"));
                        map.put("dgimn", dataGatherCode);
                        map.put("remindtype", paramMap.get(remindtype));
                        if (paramMap.get(remindtype) != null && (Integer.parseInt(paramMap.get(remindtype).toString()) == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode())) {
                            map.put("remindtypename", "浓度突变");
                        } else if (paramMap.get(remindtype) != null && (Integer.parseInt(paramMap.get(remindtype).toString()) == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode())) {
                            map.put("remindtypename", "排放量突变");
                        }
                        map.put("outputid", map1.get("pk_id"));
                        map.put("RTSP", map1.get("RTSP"));
//                        map.put("monitorpointtype", monitortype);
                        map.put("remindtype", remindtype);
                        map.put("datatype", "hour");
                    }
                }
            }
            resultMap.put("total", onlineDataGroupMmAndMonitortime.get("total"));
            resultMap.put("datalist", data);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/23 0023 下午 1:31
     * @Description: 通过自定义条件导出在线小时突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, session]
     * @throws:
     */
    @RequestMapping(value = "exportOnlineChangeDatasByParamMap", method = RequestMethod.POST)
    public void exportOnlineChangeDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson,
                                                  HttpServletResponse response, HttpServletRequest request) throws ParseException, IOException {
        try {
            JSONObject paramMap = JSONObject.fromObject(paramsjson);

            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            Integer monitortype = 0;
            List<Integer> monitortypes = new ArrayList<>();
            monitortypes.add(WasteWaterEnum.getCode());
            monitortypes.add(WasteGasEnum.getCode());
            monitortypes.add(SmokeEnum.getCode());
            monitortypes.add(RainEnum.getCode());
            monitortypes.add(unOrganizationWasteGasEnum.getCode());
            monitortypes.add(FactoryBoundarySmallStationEnum.getCode());
            monitortypes.add(FactoryBoundaryStinkEnum.getCode());
            monitortypes.add(StorageTankAreaEnum.getCode());
            monitortypes.add(SecurityLeakageMonitor.getCode());//安全泄露监测
            monitortypes.add(SecurityCombustibleMonitor.getCode());//可燃易爆气体
            monitortypes.add(SecurityToxicMonitor.getCode());//有毒有害气体
            if (paramMap.get("monitortype") != null) {
                monitortype = Integer.valueOf(paramMap.get("monitortype").toString());
            }
            if (monitortypes.contains(monitortype)) {
                monitorInfo = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
            } else if (monitortype == AirEnum.getCode()) {//大气
                monitorInfo = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            } else if (monitortype == EnvironmentalVocEnum.getCode() || monitortype == EnvironmentalStinkEnum.getCode() || monitortype == MicroStationEnum.getCode()) {//voc//恶臭
                monitorInfo = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            } else if (monitortype == WaterQualityEnum.getCode()) {//水质
                monitorInfo = waterStationService.getWaterStationByParamMap(paramMap);
            }
            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            Map<String, Object> map = onlineService.countOnlineDataGroupMmAndMonitortime(paramMap);
            List<Map<String, Object>> allData = new ArrayList<>();
            if (map.get("total") != null && Integer.valueOf(map.get("total").toString()) > 0) {
                Float total = Float.valueOf(map.get("total").toString());
                for (int i = 1; i < Math.ceil(total / 20) + 1; i++) {
                    paramMap.put("pagenum", i);
                    paramMap.put("pagesize", 20);
                    Object onlineDatasByParamMap = getOnlineDatasByParamMap(paramMap);
                    JSONObject jsonObject = JSONObject.fromObject(onlineDatasByParamMap);
                    Object data = jsonObject.get("data");
                    JSONObject jsonObject1 = JSONObject.fromObject(data);
                    Object datalist = jsonObject1.get("datalist");
                    JSONArray jsonArray = JSONArray.fromObject(datalist);
                    allData.addAll(jsonArray);
                }
            }
            List<Map<String, Object>> collect = allData.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", (LinkedList) map.get("headers"), (LinkedList) map.get("headersfield"), collect, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
            ExcelUtil.downLoadExcel(map.get("excelname").toString(), response, request, bytesForWorkBook);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/25 0025 上午 10:37
     * @Description: 通过自定义条件查询监测点浓度（预警、异常、超限）连续预警数据
     * @updateUser:xsm
     * @updateDate:2019/12/16 0016 上午 10:22
     * @updateDescription:（首页）关联运维任务、污染物增加异常类型
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getConcentrationContinuityListDataByParams", method = RequestMethod.POST)
    public Object getConcentrationContinuityListDataByParams(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                             @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                             @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                             @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                             @RequestJson(value = "remindtype") Integer remindtype,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "exceptionlist", required = false) List<String> exceptionlist,
                                                             @RequestJson(value = "pageflag", required = false) String pageflag,
                                                             @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                             @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws ParseException {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    unOrganizationWasteGasEnum.getCode(),
                    unOrganizationWasteGasEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitortype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("remindtype", remindtype);
            paramMap.put("exceptionlist", exceptionlist);
            paramMap.put("userid", userId);
            List<Map<String, Object>> stoplist = new ArrayList<>();
            List<Map<String, Object>> rainoutputlist = new ArrayList<>();
            //获取所点位名称和MN号
            if (monitortypes.contains(monitorpointtype)) {
                monitorPoints = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
                if (monitorpointtype == WasteWaterEnum.getCode() || monitorpointtype == WasteGasEnum.getCode()
                        || monitorpointtype == SmokeEnum.getCode()
                        || monitorpointtype == RainEnum.getCode()) {//为废气、废水、雨水、烟气排口时
                    paramMap.put("monitorpointtype", monitorpointtype);
                    stoplist = stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(paramMap);//停产排口
                    if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//排放中
                        paramMap.put("monitorpointtype", monitorpointtype);
                        rainoutputlist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
                    }
                }
            } else if (monitorpointtype == AirEnum.getCode()) {//大气
                monitorPoints = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                monitorPoints = waterStationService.getWaterStationByParamMap(paramMap);
            } else{//其它监测点类型
                monitorPoints = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            }
            String mnCommon;
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            Map<String, Object> mnAndStorageTankAreaId = new HashMap<>();//贮罐区ID
            Map<String, Object> mnAndAndStorageTankAreaName = new HashMap<>();//贮罐区名称
            Map<String, Object> pointidAndTaskid = new HashMap<>();
            Map<String, Object> pointidAndStatus = new HashMap<>();
            Map<String, List<Map<String, Object>>> pointidAndrtsp = new HashMap<>();
            //获取相关视频信息
            List<Map<String, Object>> videos = videoCameraService.getVideoInfoByMonitorpointType(monitorpointtype);
            if (videos != null && videos.size() > 0) {
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videos) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videos) {
                                if (map2.get("monitorpointid") != null && (map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))) {
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp", map2.get("rtsp"));
                                    objmap.put("name", map2.get("name"));
                                    objmap.put("id", map2.get("pkid"));
                                    objmap.put("vediomanufactor", map2.get("VedioManufactor"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(), rtsplist);
                        }
                        //
                    }
                }
            }
            //根据报警类型和监测时间获取报警任务信息
            List<Map<String, Object>> tasklist = devOpsTaskDisposeService.getAlarmTaskInfoByRemindTypeAndParamMap(paramMap);
            if ("homepage".equals(pageflag)) {//判断是否为首页调用该接口 是则关联任务
                if (tasklist.size() > 0) {
                    for (Map<String, Object> taskmap : tasklist) {
                        pointidAndTaskid.put(taskmap.get("Pollutionid") != null ? taskmap.get("Pollutionid").toString() : "", taskmap.get("PK_TaskID"));
                        pointidAndStatus.put(taskmap.get("Pollutionid") != null ? taskmap.get("Pollutionid").toString() : "", taskmap.get("TaskStatus"));
                    }
                }
            }
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    if (monitorpointtype == WasteWaterEnum.getCode() || monitorpointtype == WasteGasEnum.getCode() || monitorpointtype == SmokeEnum.getCode() || monitorpointtype == RainEnum.getCode()) {
                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                        boolean rainflag = false;
                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> stopmap : stoplist) {
                                if ((map.get("pk_id").toString()).equals(stopmap.get("FK_Outputid").toString())) {
                                    rainflag = true;
                                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname") + "【停产】");
                                    break;
                                }
                            }
                        }
                        /*if (monitorpointtype == RainEnum.getCode() && rainflag == false) {//若为雨水类型 且未停产  判断是否为排放中
                            if (rainoutputlist != null && rainoutputlist.size() > 0) {
                                for (Map<String, Object> stopmap : rainoutputlist) {
                                    if ((map.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname") + "【排放中】");
                                        break;
                                    }
                                }
                            }
                        }*/
                    } else {
                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                    }
                    mnAndMonitorPointId.put(mnCommon, map.get("pk_id"));
                    mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                    if (monitorpointtype == StorageTankAreaEnum.getCode()) {//储罐
                        mnAndStorageTankAreaId.put(mnCommon, map.get("FK_StorageTankAreaID"));
                        mnAndAndStorageTankAreaName.put(mnCommon, map.get("StorageTankAreaName"));
                    }
                }
            }

            paramMap.put("dgimns", dgimns);
            paramMap.put("usercode", usercode);
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getOnlineContinuityDataGroupMmAndMonitortime(paramMap);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) onlineDataGroupMmAndMonitortime.get("datalist");

            Set<String> pollutantNameList;
            String pollutantNames;
            List<String> tempList;
            for (Map<String, Object> dataMap : dataList) {
                mnCommon = dataMap.get("datagathercode").toString();
                if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                    pollutantNameList = getPollutantNameAndExceptionTypeByCodes(dataMap.get("pollutantCodes"), (List<Map<String, Object>>) dataMap.get("exceptiontypes"), Arrays.asList(monitorpointtype));
                } else { //超标
                    pollutantNameList = getPollutantNameByCodes(dataMap.get("pollutantCodes"), Arrays.asList(monitorpointtype));
                }
                tempList = new ArrayList<>(pollutantNameList);
                pollutantNames = DataFormatUtil.FormatListToString(tempList, "、");
                dataMap.put("pollutantnames", pollutantNames);
                dataMap.put("dgimn", mnCommon);
                dataMap.put("pollutionname", mnAndPollutionName.get(mnCommon));
                dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                if (monitorpointtype == StorageTankAreaEnum.getCode()) {
                    dataMap.put("tankareapkid", mnAndStorageTankAreaId.get(mnCommon));
                    dataMap.put("storagetankareaname", mnAndAndStorageTankAreaName.get(mnCommon));
                }
                dataMap.put("pollutionid", mnAndPollutionId.get(mnCommon));
                if ("homepage".equals(pageflag)) {//判断是否为首页调用该接口 是则关联任务
                    if (monitortypes.contains(monitorpointtype)) {//关联企业的排口和厂界点位
                        dataMap.put("taskid", mnAndPollutionId.get(mnCommon) != null ? pointidAndTaskid.get(mnAndPollutionId.get(mnCommon)) : null);
                        dataMap.put("status", mnAndPollutionId.get(mnCommon) != null ? pointidAndStatus.get(mnAndPollutionId.get(mnCommon)) : null);
                    } else {//不关联企业的点位
                        dataMap.put("taskid", mnAndMonitorPointId.get(mnCommon) != null ? pointidAndTaskid.get(mnAndMonitorPointId.get(mnCommon)) : null);
                        dataMap.put("status", mnAndMonitorPointId.get(mnCommon) != null ? pointidAndStatus.get(mnAndMonitorPointId.get(mnCommon)) : null);
                    }
                }
                dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                dataMap.put("rtsplist", pointidAndrtsp.get(mnAndMonitorPointId.get(mnCommon)));
                if (dataMap.get("minvalue") != null && dataMap.get("maxvalue") != null) {
                    String minvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(dataMap.get("minvalue").toString()) * 100));
                    String maxvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(dataMap.get("maxvalue").toString()) * 100));
                    if (!minvalue.equals(maxvalue)) {//最大超标倍数和最小超标倍数不相等
                        dataMap.put("overmultiple", minvalue + "%-" + maxvalue + "%");
                    } else {//相等
                        dataMap.put("overmultiple", minvalue + "%");
                    }
                } else {
                    dataMap.put("overmultiple", "");
                }
            }
            onlineDataGroupMmAndMonitortime.remove("datalist");
            onlineDataGroupMmAndMonitortime.put("data", dataList);

            return AuthUtil.parseJsonKeyToLower("success", onlineDataGroupMmAndMonitortime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/23 0023 下午 1:31
     * @Description: 通过自定义条件导出监测点（预警、异常、超限）浓度连续数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, session]
     * @throws:
     */
    @RequestMapping(value = "exportConcentrationContinuityListDataByParamMap", method = RequestMethod.POST)
    public void exportConcentrationContinuityListDataByParamMap(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                                @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                                @RequestJson(value = "exceptionlist", required = false) List<String> exceptionlist,
                                                                @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                                @RequestJson(value = "remindtype") Integer remindtype,
                                                                @RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "endtime") String endtime,

                                                                HttpServletResponse response, HttpServletRequest request) throws ParseException, IOException {
        try {
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(), SmokeEnum.getCode(),
                    WasteGasEnum.getCode(), RainEnum.getCode(), unOrganizationWasteGasEnum.getCode(), FactoryBoundarySmallStationEnum.getCode(), FactoryBoundaryStinkEnum.getCode()
            );
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitortype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("exceptionlist", exceptionlist);
            paramMap.put("remindtype", remindtype);
            //获取所点位名称和MN号
            if (monitortypes.contains(monitorpointtype)) {
                monitorPoints = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
            } else if (monitorpointtype == AirEnum.getCode()) {//大气
                monitorPoints = airMonitorStationService.getAllAirMonitorStationByParams(paramMap);
            } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
                monitorPoints = waterStationService.getWaterStationByParamMap(paramMap);
            }  else{//其它监测点类型
                monitorPoints = otherMonitorPointService.getAllMonitorInfoByParams(paramMap);
            }
            String mnCommon;
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();

            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, map.get("pk_id"));
                    mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                }
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("usercode", usercode);
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getOnlineContinuityDataGroupMmAndMonitortime(paramMap);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) onlineDataGroupMmAndMonitortime.get("datalist");

            Set<String> pollutantNameList;
            String pollutantNames;
            List<String> tempList;
            for (Map<String, Object> dataMap : dataList) {
                mnCommon = dataMap.get("datagathercode").toString();
                if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                    pollutantNameList = getPollutantNameAndExceptionTypeByCodes(dataMap.get("pollutantCodes"), (List<Map<String, Object>>) dataMap.get("exceptiontypes"), Arrays.asList(monitorpointtype));
                } else { //超标
                    pollutantNameList = getPollutantNameByCodes(dataMap.get("pollutantCodes"), Arrays.asList(monitorpointtype));
                }
                tempList = new ArrayList<>(pollutantNameList);
                pollutantNames = DataFormatUtil.FormatListToString(tempList, "、");
                dataMap.put("pollutantnames", pollutantNames);
                dataMap.put("pollutionname", mnAndPollutionName.get(mnCommon));
                dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                dataMap.put("pollutionid", mnAndPollutionId.get(mnCommon));
                dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                if (dataMap.get("minvalue") != null && dataMap.get("maxvalue") != null) {
                    String minvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(dataMap.get("minvalue").toString()) * 100));
                    String maxvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(dataMap.get("maxvalue").toString()) * 100));
                    if (!minvalue.equals(maxvalue)) {//最大超标倍数和最小超标倍数不相等
                        dataMap.put("overmultiple", minvalue + "%-" + maxvalue + "%");
                    } else {//相等
                        dataMap.put("overmultiple", minvalue + "%");
                    }
                } else {
                    dataMap.put("overmultiple", "");
                }
            }
            //设置文件名称
            String fileName = "";
            if (remindtype == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {  //阈值
                fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtype, "early");
            } else if (remindtype == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {    //异常
                fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtype, "exception");
            } else if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtype, "over");
            }
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForConcentrationContinuity(remindtype, monitorpointtype, monitortypes);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");

            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/6 0006 下午 2:35
     * @Description: 通过监测点类型，在线状态，数据类型（预警：earlywarn，报警：exception），时间范围（yyyy-MM-dd）查询企业列表信息
     * @updateUser:xsm
     * @updateDate:2020/08/19 下午 2:32
     * @updateDescription:在线状态中新增停产状态
     * @param: [monitorpointtype, status, datatype, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getPollutionInfosByParamMap", method = RequestMethod.POST)
    public Object getPollutionInfosByParamMap(@RequestJson(value = "monitorpointtype", required = false) Object monitorpointtype,
                                              @RequestJson(value = "fkmonitorpointtypecodes", required = false) Object fkmonitorpointtypecodes,
                                              @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                              @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                              @RequestJson(value = "status", required = false) String status,
                                              @RequestJson(value = "datatypes") Object datatypes,
                                              @RequestJson(value = "starttime", required = false) String starttime,
                                              @RequestJson(value = "endtime", required = false) String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String sessionID = SessionUtil.getSessionID();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            paramMap.put("fkmonitorpointtypecode", monitorpointtype);
            paramMap.put("fkmonitorpointtypecodes", fkmonitorpointtypecodes);
            if (status != null && !"".equals(status)) {
                if ("4".equals(status)) {
                    paramMap.put("stopflag", "1");
                } else {
                    paramMap.put("status", status);
                }
            } else {
                paramMap.put("status", status);
            }
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            /*//设置权限
            userMonitorPointRelationDataService.ExcludeNoAuthDGIMNByParamMap(dgimns);*/

            List<Integer> remindTypes = new ArrayList<>();
            if (((List<String>) datatypes).contains("exception")) {
                remindTypes.add(ExceptionAlarmEnum.getCode());

            }
            if (((List<String>) datatypes).contains("overdata")) {
                remindTypes.add(OverAlarmEnum.getCode());
            }
            if (((List<String>) datatypes).contains("earlywarn")) {
                remindTypes.add(ConcentrationChangeEnum.getCode());
                remindTypes.add(FlowChangeEnum.getCode());
                remindTypes.add(EarlyAlarmEnum.getCode());
            }
            if (((List<String>) datatypes).contains("normal")) {
                remindTypes.add(ExceptionAlarmEnum.getCode());
                remindTypes.add(OverAlarmEnum.getCode());
                remindTypes.add(ConcentrationChangeEnum.getCode());
                remindTypes.add(FlowChangeEnum.getCode());
                remindTypes.add(EarlyAlarmEnum.getCode());
            }


            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", "day");
            paramMap.put("remindTypes", remindTypes);
            Map<String, Object> onlineData = onlineCountAlarmService.countAlarmsDataByParamMap(paramMap);
            for (String s : onlineData.keySet()) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) onlineData.get(s);
                for (Map<String, Object> map : outPutInfosByParamMap) {
                    map.put(s, false);
                    for (Map<String, Object> datum : data) {
                        String _id = datum.get("_id") == null ? "" : datum.get("_id").toString();
                        Integer count = datum.get("count") == null ? 0 : Integer.valueOf(datum.get("count").toString());
                        String dgimn = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                        if (_id.equals(dgimn) && count > 0) {
                            map.put(s, true);
                        }
                    }
                }
            }

            Map<String, List<Map<String, Object>>> collect = outPutInfosByParamMap.stream().filter(m -> m.get("PK_PollutionID") != null).collect(Collectors.groupingBy(m -> m.get("PK_PollutionID").toString()));


            for (String pk_pollutionid : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(pk_pollutionid);
                if (list.size() > 0) {
                    Map<String, Object> map = list.get(0);
                    if (list.stream().filter(m -> m.get("status") != null && "".equals("0")).collect(Collectors.toList()).size() > 0) {
                        map.put("status", 0);
                    }
                    if (map.get("isstop") != null && "1".equals(map.get("isstop").toString())) {
                        map.put("Status", 4);
                    }

                    if (list.stream().filter(m -> m.get("earlywarn") != null && (boolean) m.get("earlywarn")).collect(Collectors.toList()).size() > 0) {
                        map.put("earlywarn", true);
                    }

                    if (list.stream().filter(m -> m.get("exception") != null && (boolean) m.get("exception")).collect(Collectors.toList()).size() > 0) {
                        map.put("exception", true);
                    }

                    if (list.stream().filter(m -> m.get("overdata") != null && (boolean) m.get("overdata")).collect(Collectors.toList()).size() > 0) {
                        map.put("overdata", true);
                    }
                    map.remove("FK_MonitorPointTypeCode");
                    map.remove("DGIMN");
                    map.remove("OutputName");

                    result.add(map);
                }
            }
            if (((List<String>) datatypes).contains("exception")) {
                resultList.addAll(result.stream().filter(m -> m.get("exception") != null && (boolean) m.get("exception")).sorted(Comparator.comparing(m -> ((Map) m).get("exception").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("overdata")) {
                resultList.addAll(result.stream().filter(m -> m.get("overdata") != null && (boolean) m.get("overdata")).sorted(Comparator.comparing(m -> ((Map) m).get("overdata").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("earlywarn")) {
                resultList.addAll(result.stream().filter(m -> m.get("earlywarn") != null && (boolean) m.get("earlywarn") && m.get("exception") != null && !(boolean) m.get("exception") && m.get("overdata") != null && !(boolean) m.get("overdata")).sorted(Comparator.comparing(m -> ((Map) m).get("earlywarn").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("normal")) {
                resultList.addAll(result.stream().filter(m -> m.get("exception") != null && !(boolean) m.get("exception") && m.get("earlywarn") != null && !(boolean) m.get("earlywarn") && m.get("overdata") != null && !(boolean) m.get("overdata")).sorted(Comparator.comparing(m -> ((Map) m).get("Status").toString()).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("stop")) {
                resultList.addAll(result.stream().filter(m -> m.get("isstop") != null && "1".equals(m.get("isstop").toString())).sorted(Comparator.comparing(m -> ((Map) m).get("isstop").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            resultList = resultList.stream().distinct().collect(Collectors.toList());
            Integer total = resultList.size();
            if (pagenum != null && pagesize != null) {
                resultList = resultList.stream().distinct().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total", total);
            resultMap.put("datalist", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/8 0008 下午 2:28
     * @Description: 通过监测点类型，在线状态，数据类型（预警：earlywarn，报警：exception），时间范围（yyyy-MM-dd）查询监测点列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, pagenum, pagesize, status, datatypes, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfosByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointInfosByParamMap(@RequestJson(value = "monitorpointtypes", required = false) Object monitorpointtypes,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "status", required = false) String status,
                                                 @RequestJson(value = "datatypes") Object datatypes,
                                                 @RequestJson(value = "starttime", required = false) String starttime,
                                                 @RequestJson(value = "endtime", required = false) String endtime
    ) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userId);
            paramMap.put("onlydataauthor", "1");
            paramMap.put("fkmonitorpointtypecodes", monitorpointtypes);
            paramMap.put("status", status);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            List<Integer> remindTypes = new ArrayList<>();

            if (((List<String>) datatypes).contains("exception")) {
                remindTypes.add(ExceptionAlarmEnum.getCode());
            }
            if (((List<String>) datatypes).contains("overdata")) {
                remindTypes.add(OverAlarmEnum.getCode());
            }
            if (((List<String>) datatypes).contains("earlywarn")) {
                remindTypes.add(ConcentrationChangeEnum.getCode());
                remindTypes.add(FlowChangeEnum.getCode());
                remindTypes.add(EarlyAlarmEnum.getCode());
            }
            if (((List<String>) datatypes).contains("normal")) {
                remindTypes.add(ExceptionAlarmEnum.getCode());
                remindTypes.add(OverAlarmEnum.getCode());
                remindTypes.add(ConcentrationChangeEnum.getCode());
                remindTypes.add(FlowChangeEnum.getCode());
                remindTypes.add(EarlyAlarmEnum.getCode());
            }


            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", "day");
            paramMap.put("remindTypes", remindTypes);
            Map<String, Object> onlineData = onlineCountAlarmService.countAlarmsDataByParamMap(paramMap);
            for (String s : onlineData.keySet()) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) onlineData.get(s);
                for (Map<String, Object> map : outPutInfosByParamMap) {
                    map.put(s, false);
                    for (Map<String, Object> datum : data) {
                        String _id = datum.get("_id") == null ? "" : datum.get("_id").toString();
                        Integer count = datum.get("count") == null ? 0 : Integer.valueOf(datum.get("count").toString());
                        String dgimn = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                        if (_id.equals(dgimn) && count > 0) {
                            map.put(s, true);
                        }
                    }
                    result.add(map);
                }
            }
            if (((List<String>) datatypes).contains("exception")) {
                resultList.addAll(result.stream().filter(m -> m.get("exception") != null && (boolean) m.get("exception")).sorted(Comparator.comparing(m -> ((Map) m).get("exception").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("overdata")) {
                resultList.addAll(result.stream().filter(m -> m.get("exception") != null && !(boolean) m.get("exception") && m.get("overdata") != null && (boolean) m.get("overdata")).sorted(Comparator.comparing(m -> ((Map) m).get("overdata").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("earlywarn")) {
                resultList.addAll(result.stream().filter(m -> m.get("earlywarn") != null && (boolean) m.get("earlywarn") && m.get("exception") != null && !(boolean) m.get("exception") && m.get("overdata") != null && !(boolean) m.get("overdata")).sorted(Comparator.comparing(m -> ((Map) m).get("earlywarn").toString()).thenComparing(m -> Integer.valueOf(((Map) m).get("Status").toString())).reversed()).collect(Collectors.toList()));
            }
            if (((List<String>) datatypes).contains("normal")) {
                resultList.addAll(result.stream().filter(m -> m.get("exception") != null && !(boolean) m.get("exception") && m.get("earlywarn") != null && !(boolean) m.get("earlywarn") && m.get("overdata") != null && !(boolean) m.get("overdata")).sorted(Comparator.comparing(m -> ((Map) m).get("Status").toString()).reversed()).collect(Collectors.toList()));
            }
            resultList = resultList.stream().distinct().collect(Collectors.toList());
            Integer total = resultList.size();
            if (pagenum != null && pagesize != null) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total", total);
            resultMap.put("datalist", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/8 0008 下午 2:28
     * @Description: 通过监测点类型，在线状态，数据类型（预警：earlywarn，报警：exception），时间范围（yyyy-MM-dd）统计企业相关报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countMonitorPointAlarmByParamMap", method = RequestMethod.POST)
    public Object countMonitorPointAlarmByParamMap(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                                   @RequestJson(value = "starttime", required = false) String starttime,
                                                   @RequestJson(value = "endtime", required = false) String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(), unOrganizationWasteGasEnum.getCode()));
//            paramMap.put("status", status);
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            String code = OfflineStatusEnum.getCode();

            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("Status") != null && !code.equals(m.get("Status").toString()))
                    .map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            List<Integer> remindTypes = new ArrayList<>();

            remindTypes.add(ExceptionAlarmEnum.getCode());
            remindTypes.add(OverAlarmEnum.getCode());
            remindTypes.add(ConcentrationChangeEnum.getCode());
            remindTypes.add(EarlyAlarmEnum.getCode());

            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", "day");
            paramMap.put("remindTypes", remindTypes);
            //统计点位报警和预警次数
            Map<String, Object> onlineData = onlineCountAlarmService.countAlarmsDataByParamMap(paramMap);


            List<Map<String, Object>> onlyearlyWarn = (List<Map<String, Object>>) onlineData.get("onlyearlyWarn");
            List<Map<String, Object>> concentrationchangedata = (List<Map<String, Object>>) onlineData.get("concentrationchangedata");
            List<Map<String, Object>> exception = (List<Map<String, Object>>) onlineData.get("exception");
            List<Map<String, Object>> overdata = (List<Map<String, Object>>) onlineData.get("overdata");

            Integer countearlywarn = onlyearlyWarn.stream().filter(m -> m.get("count") != null).map(m -> Integer.valueOf(m.get("count").toString())).collect(Collectors.summingInt(m -> m));
            Integer countconcentrationchange = concentrationchangedata.stream().filter(m -> m.get("count") != null).map(m -> Integer.valueOf(m.get("count").toString())).collect(Collectors.summingInt(m -> m));
            Integer countexception = exception.stream().filter(m -> m.get("count") != null).map(m -> Integer.valueOf(m.get("count").toString())).collect(Collectors.summingInt(m -> m));
            Integer countoverdata = overdata.stream().filter(m -> m.get("count") != null).map(m -> Integer.valueOf(m.get("count").toString())).collect(Collectors.summingInt(m -> m));
            long countOffline = outPutInfosByParamMap.stream().filter(m -> m.get("Status") != null && code.equals(m.get("Status").toString())).count();

            resultMap.put("countearlywarn", countearlywarn);
            resultMap.put("countconcentrationchange", countconcentrationchange);
            resultMap.put("countexception", countexception);
            resultMap.put("countoverdata", countoverdata);
            resultMap.put("countOffline", countOffline);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/4/10 0010 上午 9:17
     * @Description: 查询企业一段时间内的报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, starttime, datatype, pagesize, pagenum, endtime]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointAlarmInfoByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointAlarmInfoByParamMap(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                                     @RequestJson(value = "starttime", required = false) String starttime,
                                                     @RequestJson(value = "datatype") String datatype,
                                                     @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                     @RequestJson(value = "endtime", required = false) String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(), unOrganizationWasteGasEnum.getCode()));
//            paramMap.put("status", status);
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            String offlinestatus = OfflineStatusEnum.getCode();

            //在线排口mn号
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("Status") != null && !offlinestatus.equals(m.get("Status").toString()))
                    .map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());


            Map<String, Object> pollutantMap = new HashMap<>();
            List<Map<String, Object>> pollutants = new ArrayList<>();
            Map<String, List<Map<String, Object>>> collect = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));
            for (String FK_MonitorPointTypeCode : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(FK_MonitorPointTypeCode);
                List<String> outputids = list.stream().filter(m -> m.get("outputid") != null).map(m -> m.get("outputid").toString()).collect(Collectors.toList());
                pollutantMap.put("pollutanttype", FK_MonitorPointTypeCode);
                pollutantMap.put("outputids", outputids);
                if (FK_MonitorPointTypeCode.equals(WasteGasEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(SmokeEnum.getCode() + "")) {
                    pollutantMap.put("unorgflag", false);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetService.getGasPollutantByOutputId(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                else if (FK_MonitorPointTypeCode.equals(unOrganizationWasteGasEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(FactoryBoundaryStinkEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(FactoryBoundarySmallStationEnum.getCode() + "")) {
                    pollutantMap.put("unorgflag", true);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetService.getGasPollutantByOutputId(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                else if (FK_MonitorPointTypeCode.equals(WasteWaterEnum.getCode() + "")) {
                    pollutantMap.put("datamark", "1");
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = waterOutPutPollutantSetService.getPollutantByParamMap(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                else if (FK_MonitorPointTypeCode.equals(RainEnum.getCode() + "")) {
                    pollutantMap.put("datamark", "3");
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = waterOutPutPollutantSetService.getPollutantByParamMap(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
            }


            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", dgimns);

            List<Document> monitorDataByParamMap = new ArrayList<>();

            String monitortimekey = "";
            if (datatype.equals("exception")) {
                monitortimekey = "ExceptionTime";
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("collection", "ExceptionData");//异常
                monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);

                resultList = getAlarmResult(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey);
            } else if (datatype.equals("over")) {
                monitortimekey = "OverTime";
                paramMap.put("monitortimekey", "OverTime");
                paramMap.put("collection", "OverData");//超标
                monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);

                resultList = getAlarmResult(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey);
            } else if (datatype.equals("earlywarn")) {
                monitortimekey = "EarlyWarnTime";
                paramMap.put("monitortimekey", "EarlyWarnTime");
                paramMap.put("collection", "EarlyWarnData");//预警
                monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
                resultList = getAlarmResult(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey);
            } else if (datatype.equals("countconcentrationchange")) {
                paramMap.put("collection", "HourData");
                paramMap.put("issuddenchange", true);//浓度突变
                monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> pollutant = new ArrayList<>();
                monitorDataByParamMap.stream().filter(m -> m.get("HourDataList") != null).flatMap(m -> ((List<Map<String, Object>>) (m.get("HourDataList"))).stream()).forEach(m -> {
                    String PollutantCode = m.get("PollutantCode") == null ? "" : m.get("PollutantCode").toString();
                    Optional<Map<String, Object>> first = pollutants.stream().filter(n -> m.get("pollutantcode") != null && PollutantCode.equals(n.get("pollutantcode").toString())).findFirst();
                    if (first.isPresent()) {
                        String pollutantname = first.get().get("pollutantname") == null ? "" : first.get().get("pollutantname").toString();
                        Map<String, Object> data = new HashMap<>();
                        data.put("pollutantname", pollutantname);
                        data.put("pollutantcode", PollutantCode);
                        pollutant.add(data);
                    }
                });

                List<Map<String, Object>> title = pollutant.stream().distinct().collect(Collectors.toList());
                resultList = getConcentrationChangeResult(monitorDataByParamMap, outPutInfosByParamMap);
                resultMap.put("title", title);
            } else if (datatype.equals("offline")) {
                resultList = outPutInfosByParamMap.stream().filter(m -> m.get("Status") != null && offlinestatus.equals(m.get("Status").toString())).collect(Collectors.toList());
            }
            int total = resultList.size();
            if (pagesize != null && pagenum != null) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", total);
            resultMap.put("datalist", resultList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/4/7 0007 下午 1:11
     * @Description: 获取报警结果数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [data]
     * @throws:
     */
    private List<Map<String, Object>> getAlarmResult(List<Document> data, List<Map<String, Object>> pollutants, List<Map<String, Object>> outPutInfosByParamMap, String monitortimekey) throws ParseException {
        Map<String, List<Document>> collect = data.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (String DataGatherCode : collect.keySet()) {
            List<Document> documents = collect.get(DataGatherCode);
            Optional<Map<String, Object>> firstOutPut = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst();
            for (Document document : documents) {
                Map<String, Object> map = new HashMap<>();
                String PollutantCode = document.get("PollutantCode") == null ? "" : document.get("PollutantCode").toString();
                String MonitorValue = document.get("MonitorValue") == null ? "" : document.get("MonitorValue").toString();
                String DataType = document.get("DataType") == null ? "" : document.get("DataType").toString();
                String MonitorTime = formatCSTString(document.get(monitortimekey) == null ? "" : document.get(monitortimekey).toString(), "yyyy-MM-dd HH:mm:ss");
                String textByName = CommonTypeEnum.MongodbDataTypeEnum.getTextByName(DataType);
                map.put("MonitorValue", MonitorValue);
                map.put("DataType", textByName);
                map.put("monitortime", MonitorTime);
                Optional<Map<String, Object>> firstPollutant = pollutants.stream().filter(m -> m.get("dgimn") != null && m.get("pollutantcode") != null &&
                        DataGatherCode.equals(m.get("dgimn").toString()) && PollutantCode.equals(m.get("pollutantcode").toString())).findFirst();
                //设置污染物信息
                if (firstPollutant.isPresent()) {
                    String pollutantname = firstPollutant.get().get("pollutantname") == null ? "" : firstPollutant.get().get("pollutantname").toString();
                    map.put("pollutantname", pollutantname);
                }
                //设置排口信息
                if (firstOutPut.isPresent()) {
                    String FK_MonitorPointTypeName = firstOutPut.get().get("FK_MonitorPointTypeName") == null ? "" : firstOutPut.get().get("FK_MonitorPointTypeName").toString();
                    String OutputName = firstOutPut.get().get("OutputName") == null ? "" : firstOutPut.get().get("OutputName").toString();
                    map.put("FK_MonitorPointTypeName", FK_MonitorPointTypeName);
                    map.put("OutputName", OutputName);
                }
                result.add(map);
            }
        }

        return result;
    }

    /**
     * @author: chengzq
     * @date: 2020/4/7 0007 下午 1:11
     * @Description: 获取浓度突变结果数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [data]
     * @throws:
     */
    private List<Map<String, Object>> getConcentrationChangeResult(List<Document> data, List<Map<String, Object>> outPutInfosByParamMap) throws ParseException {
        Map<String, List<Document>> collect = data.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (String DataGatherCode : collect.keySet()) {
            List<Document> documents = collect.get(DataGatherCode);
            Optional<Map<String, Object>> firstOutPut = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst();
            for (Document documentdata : documents) {
                Map<String, Object> map = new HashMap<>();
                //设置排口信息
                if (firstOutPut.isPresent()) {
                    String FK_MonitorPointTypeName = firstOutPut.get().get("FK_MonitorPointTypeName") == null ? "" : firstOutPut.get().get("FK_MonitorPointTypeName").toString();
                    String OutputName = firstOutPut.get().get("OutputName") == null ? "" : firstOutPut.get().get("OutputName").toString();
                    map.put("FK_MonitorPointTypeName", FK_MonitorPointTypeName);
                    map.put("OutputName", OutputName);
                }
                List<Map<String, Object>> hourDataList = (List<Map<String, Object>>) documentdata.get("HourDataList");
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (Map<String, Object> document : hourDataList) {
                    Map<String, Object> resultdata = new HashMap<>();
                    String PollutantCode = document.get("PollutantCode") == null ? "" : document.get("PollutantCode").toString();
                    String AvgStrength = document.get("AvgStrength") == null ? "" : document.get("AvgStrength").toString();
                    String MonitorTime = formatCSTString(documentdata.get("MonitorTime") == null ? "" : documentdata.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss");
                    resultdata.put("AvgStrength", AvgStrength);
                    resultdata.put("monitortime", MonitorTime);
                    resultdata.put("PollutantCode", PollutantCode);
                    resultList.add(resultdata);
                }
                map.put("HourDataList", resultList);
                result.add(map);
            }
        }
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2020/4/10 0010 上午 9:50
     * @Description: 统计企业下每个排口的最新报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, starttime, datatype, pagesize, pagenum, endtime]
     * @throws:
     */
    @RequestMapping(value = "statisticsMonitorPointAlarmInfoByParamMap", method = RequestMethod.POST)
    public Object statisticsMonitorPointAlarmInfoByParamMap(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                                            @RequestJson(value = "starttime", required = false) String starttime,
                                                            @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                            @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                            @RequestJson(value = "endtime", required = false) String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            if(categorys.contains("2")){
                paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                        FactoryBoundarySmallStationEnum.getCode(), unOrganizationWasteGasEnum.getCode(),StorageTankAreaEnum.getCode(),ProductionSiteEnum.getCode(),SecurityLeakageMonitor.getCode(),
                        SecurityCombustibleMonitor.getCode(),SecurityToxicMonitor.getCode()));
            }else{
                paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode(),
                        FactoryBoundarySmallStationEnum.getCode(), unOrganizationWasteGasEnum.getCode()));
            }
            paramMap.put("fk_pollutionid", fk_pollutionid);
            paramMap.put("categorys", categorys);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);


            //在线排口mn号
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("Status") != null)
                    .map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());


            Map<String, Object> pollutantMap = new HashMap<>();
            List<Map<String, Object>> pollutants = new ArrayList<>();
            Map<String, List<Map<String, Object>>> collect = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));
            for (String FK_MonitorPointTypeCode : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(FK_MonitorPointTypeCode);
                List<String> outputids = list.stream().filter(m -> m.get("outputid") != null).map(m -> m.get("outputid").toString()).collect(Collectors.toList());
                pollutantMap.put("pollutanttype", FK_MonitorPointTypeCode);
                pollutantMap.put("outputids", outputids);
                if (FK_MonitorPointTypeCode.equals(WasteGasEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(SmokeEnum.getCode() + "")) {
                    pollutantMap.put("unorgflag", false);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetService.getGasPollutantByOutputId(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                else if (FK_MonitorPointTypeCode.equals(unOrganizationWasteGasEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(FactoryBoundaryStinkEnum.getCode() + "") || FK_MonitorPointTypeCode.equals(FactoryBoundarySmallStationEnum.getCode() + "")) {
                    pollutantMap.put("unorgflag", true);
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = gasOutPutPollutantSetService.getGasPollutantByOutputId(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                else if (FK_MonitorPointTypeCode.equals(WasteWaterEnum.getCode() + "")) {
                    pollutantMap.put("datamark", "1");
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = waterOutPutPollutantSetService.getPollutantByParamMap(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
                else if (FK_MonitorPointTypeCode.equals(RainEnum.getCode() + "")) {
                    pollutantMap.put("datamark", "3");
                    List<Map<String, Object>> gasOutPutPollutantSetsByOutputIds = waterOutPutPollutantSetService.getPollutantByParamMap(pollutantMap);
                    pollutants.addAll(gasOutPutPollutantSetsByOutputIds);
                }
            }


            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", dgimns);

            List<Document> monitorDataByParamMap = new ArrayList<>();

            String monitortimekey = "ExceptionTime";
            String alarmtype = "数据异常";
            String alarmtypecode = "exception";
            paramMap.put("monitortimekey", "ExceptionTime");
            paramMap.put("collection", "ExceptionData");//异常
            monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
            resultList.addAll(getAlarmResults(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, alarmtype, alarmtypecode));

            monitortimekey = "OverTime";
            alarmtype = "数据超标";
            alarmtypecode = "over";
            paramMap.put("monitortimekey", "OverTime");
            paramMap.put("collection", "OverData");//超标
            monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);

            resultList.addAll(getAlarmResults(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, alarmtype, alarmtypecode));


            monitortimekey = "EarlyWarnTime";
            alarmtype = "数据预警";
            alarmtypecode = "earlywarn";
            paramMap.put("monitortimekey", "EarlyWarnTime");
            paramMap.put("collection", "EarlyWarnData");//预警
            monitorDataByParamMap = onlineService.getEarlyOrOverOrExceptionDataByParamMap(paramMap);
            resultList.addAll(getAlarmResults(monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, alarmtype, alarmtypecode));

            paramMap.put("collection", "MinuteData");
            alarmtype = "浓度突变";
            alarmtypecode = "concentrationchange";
            paramMap.put("issuddenchange", true);//浓度突变
            monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            resultList.addAll(getConcentrationChangeResults(monitorDataByParamMap, pollutants, outPutInfosByParamMap, alarmtype, alarmtypecode));

            int total = resultList.size();
            if (pagesize != null && pagenum != null) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", total);
            resultMap.put("datalist", resultList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/4/10 0010 下午 3:50
     * @Description: 组装浓度突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorDataByParamMap, pollutants, outPutInfosByParamMap, alarmtype]
     * @throws:
     */
    private List<Map<String, Object>> getConcentrationChangeResults(List<Document> monitorDataByParamMap, List<Map<String, Object>> pollutants, List<Map<String, Object>> outPutInfosByParamMap, String alarmtype, String alarmtypecode) {
        Set<Map<String, Object>> finalpollutants = new HashSet<>();
        pollutants.stream().forEach(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", m.get("pollutantcode"));
            map.put("pollutantname", m.get("pollutantname"));
            finalpollutants.add(map);
        });
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        for (String DataGatherCode : collect.keySet()) {
            Map<String, Object> data = new HashMap<>();
            List<Document> documents = collect.get(DataGatherCode);
            Optional<String> max = documents.stream().filter(m -> m.get("MonitorTime") != null).peek(m -> {
                String monitortime = formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss");
                List<Map<String, Object>> HourDataList = (List<Map<String, Object>>) m.get("MinuteDataList");
                HourDataList.stream().forEach(n -> {
                    n.put("monitortime", monitortime);
                    n.put("DataGatherCode", DataGatherCode);
                });
                m.put("MonitorTime", monitortime);
            }).map(m -> m.getString("MonitorTime")).max(Comparator.comparing(m -> m));

            List<String> pollutantnames = new ArrayList<>();
            List<Map<String, Object>> pollutantnames1 = new ArrayList<>();
            if (max.isPresent()) {
                String maxtime = max.get();
                documents.stream().filter(m -> m.get("MonitorTime") != null && maxtime.equals(m.getString("MonitorTime"))).flatMap(m -> ((List<Map<String, Object>>) m.get("MinuteDataList")).stream()).filter(m -> (Boolean) m.get("IsSuddenChange")).forEach(m -> {
                    String PollutantCode = m.get("PollutantCode") == null ? "" : m.get("PollutantCode").toString();
                    Map<String, Object> pollutant = new HashMap<>();
                    String exceptionType = m.get("ExceptionType") == null ? "" : m.get("ExceptionType").toString();
                    String alarmName = getAlarmName(exceptionType, alarmtypecode);
                    finalpollutants.stream().filter(n -> n.get("pollutantcode") != null && PollutantCode.equals(n.get("pollutantcode").toString())).forEach(n -> {
                        String pollutantname = n.get("pollutantname") == null ? "" : n.get("pollutantname").toString();
                        Map<String, Object> map = pollutantnames1.stream().filter(pollutantdata -> pollutantdata.get("pollutantname") != null && pollutantdata.get("alarmName") != null
                                && alarmName.equals(pollutantdata.get("alarmName").toString()) && pollutantdata.get("num") != null && pollutantname.equals(pollutantdata.get("pollutantname").toString())).findFirst().orElseGet(() -> {
                                    pollutantnames1.add(pollutant);
                                    return pollutant;
                                }
                        );
                        map.put("pollutantname", pollutantname);
                        map.put("alarmName", alarmName);
                        map.put("num", map.get("num") == null ? 1 : Integer.valueOf(map.get("num").toString()) + 1);
                    });
                });


                for (Map<String, Object> map : pollutantnames1) {
                    String pollutantname = map.get("pollutantname") == null ? "" : map.get("pollutantname").toString();
                    String alarmName = map.get("alarmName") == null ? "" : map.get("alarmName").toString();
                    String num = map.get("num") == null ? "" : map.get("num").toString();
                    pollutantnames.add(pollutantname + "【" + alarmName + num + "次" + "】");

                    //统计总数
                    data.put("alarmnum", data.get("alarmnum") == null ? Integer.valueOf(num) : Integer.valueOf(data.get("alarmnum").toString()) + Integer.valueOf(num));
                }

                Optional<Map<String, Object>> first = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst();
                if (first.isPresent()) {
                    Object outputName = first.get().get("OutputName");
                    data.put("OutputName", outputName);
                    data.put("pollutants", pollutantnames.stream().distinct().collect(Collectors.joining("、")));
                    data.put("monitortime", maxtime);
                    data.put("alarmtype", alarmtype);
                    data.put("alarmtypecode", alarmtypecode);
                    data.put("dgimn", DataGatherCode);
                    resultList.add(data);
                }
            }

        }
        return resultList;
    }

    /**
     * @author: chengzq
     * @date: 2020/4/10 0010 上午 10:24
     * @Description: 组装异常，超标报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorDataByParamMap, pollutants, outPutInfosByParamMap, monitortimekey, alarmtype]
     * @throws:
     */
    private List<Map<String, Object>> getAlarmResults(List<Document> monitorDataByParamMap, List<Map<String, Object>> pollutants, List<Map<String, Object>> outPutInfosByParamMap, String monitortimekey, String alarmtype, String alarmtypecode) {
        Set<Map<String, Object>> finalpollutants = new HashSet<>();
        pollutants.stream().forEach(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", m.get("pollutantcode"));
            map.put("pollutantname", m.get("pollutantname"));
            finalpollutants.add(map);
        });
        Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String DataGatherCode : collect.keySet()) {
            Map<String, Object> data = new HashMap<>();
            List<Document> documents = collect.get(DataGatherCode);
            Optional<String> max = documents.stream().filter(m -> m.get(monitortimekey) != null).peek(m -> {
                String monitortime = formatCSTString(m.get(monitortimekey).toString(), "yyyy-MM-dd HH:mm:ss");
                m.put(monitortimekey, monitortime);
            }).map(m -> m.getString(monitortimekey)).max(Comparator.comparing(m -> m));

            List<String> pollutantnames = new ArrayList<>();
            List<Map<String, Object>> pollutantnames1 = new ArrayList<>();
            if (max.isPresent()) {
                String maxtime = max.get();
                for (Document m : documents) {
                    String PollutantCode = m.get("PollutantCode") == null ? "" : m.get("PollutantCode").toString();
                    Map<String, Object> pollutant = new HashMap<>();
                    String exceptionType = m.get("ExceptionType") == null ? "" : m.get("ExceptionType").toString();
                    String alarmName = getAlarmName(exceptionType, alarmtypecode);
                    finalpollutants.stream().filter(n -> n.get("pollutantcode") != null && PollutantCode.equals(n.get("pollutantcode").toString())).forEach(n -> {
                        String pollutantname = n.get("pollutantname") == null ? "" : n.get("pollutantname").toString();
                        Map<String, Object> map = pollutantnames1.stream().filter(pollutantdata -> pollutantdata.get("pollutantname") != null && pollutantdata.get("alarmName") != null
                                && alarmName.equals(pollutantdata.get("alarmName").toString()) && pollutantdata.get("num") != null && pollutantname.equals(pollutantdata.get("pollutantname").toString())).findFirst().orElseGet(() -> {
                                    pollutantnames1.add(pollutant);
                                    return pollutant;
                                }
                        );
                        map.put("pollutantname", pollutantname);
                        map.put("alarmName", alarmName);
                        map.put("num", map.get("num") == null ? 1 : Integer.valueOf(map.get("num").toString()) + 1);
                    });
                }

                for (Map<String, Object> map : pollutantnames1) {
                    String pollutantname = map.get("pollutantname") == null ? "" : map.get("pollutantname").toString();
                    String alarmName = map.get("alarmName") == null ? "" : map.get("alarmName").toString();
                    String num = map.get("num") == null ? "" : map.get("num").toString();
                    pollutantnames.add(pollutantname + "【" + alarmName + num + "次" + "】");

                    data.put("alarmnum", data.get("alarmnum") == null ? Integer.valueOf(num) : Integer.valueOf(data.get("alarmnum").toString()) + Integer.valueOf(num));
                }


                Optional<Map<String, Object>> first = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst();
                if (first.isPresent()) {
                    Object outputName = first.get().get("OutputName");
                    data.put("OutputName", outputName);
                    data.put("pollutants", pollutantnames.stream().distinct().collect(Collectors.joining("、")));
                    data.put("monitortime", maxtime);
                    data.put("alarmtype", alarmtype);
                    data.put("alarmtypecode", alarmtypecode);
                    data.put("dgimn", DataGatherCode);
                    resultList.add(data);
                }
            }
        }
        return resultList;
    }


    /**
     * @author: chengzq
     * @date: 2020/5/22 0022 下午 2:10
     * @Description: 获取异常类型名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [alarmtype, alarmtypecode]
     * @throws:
     */
    public static String getAlarmName(String alarmtype, String alarmtypecode) {
        //异常
        if ("exception".equals(alarmtypecode)) {
            /*ZeroExceptionEnum("1", "零值异常"),
            ContinuousExceptionEnum("2", "连续恒值"),
            OverExceptionEnum("3", "超限异常"),
            ParamExceptionEnum("4", "动态管控参数异常"),
            HandPickingExceptionEnum("5", "人工挑选异常"),
            StatusExceptionEnum("6", "动态管控状态异常"),
            NoFlowExceptionEnum("7", "无流量异常");*/
            if ("1".equals(alarmtype)) {
                return "零值";
            } else if ("2".equals(alarmtype)) {
                return "恒值";
            } else if ("3".equals(alarmtype)) {
                return "超限";
            } else if ("7".equals(alarmtype)) {
                return "无流量";
            } else {
                return CommonTypeEnum.ExceptionTypeEnum.getCodeByString(alarmtype).getName();
            }
        } else if ("concentrationchange".equals(alarmtypecode)) {
            return "浓度突变";
        } else if ("earlywarn".equals(alarmtypecode)) {
            return "超阈值";
        } else if ("over".equals(alarmtypecode)) {
            return "超标";
        } else {
            return "";
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/11/22 0022 下午 3:20
     * @Description: 通过监测点类型，在线状态，数据类型（预警：earlywarn，报警：exception），时间范围（yyyy-MM-dd）查询监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, datetype, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfosByParams", method = RequestMethod.POST)
    public Object getMonitorPointInfosByParams(@RequestJson(value = "monitorpointtype", required = false) Object monitorpointtype,
                                               @RequestJson(value = "monitorpointtypes", required = false) Object monitorpointtypes,
                                               @RequestJson(value = "datetype", required = false) String datetype,
                                               @RequestJson(value = "starttime", required = false) String starttime,
                                               @RequestJson(value = "endtime", required = false) String endtime) throws ParseException {
        try {
            if ("pollution".equals(monitorpointtype)) {
                List<String> strings = Arrays.asList("exception", "overdata", "earlywarn", "normal");
                return getPollutionInfosByParamMap("", monitorpointtypes, null, null, null, strings, starttime, endtime);
            }

            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String sessionID = SessionUtil.getSessionID();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("fkmonitorpointtypecodes", monitorpointtypes);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);


            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            List<Integer> remindTypes = new ArrayList<>();
            remindTypes.add(ConcentrationChangeEnum.getCode());
            remindTypes.add(FlowChangeEnum.getCode());
            remindTypes.add(EarlyAlarmEnum.getCode());
            remindTypes.add(ExceptionAlarmEnum.getCode());
            remindTypes.add(OverAlarmEnum.getCode());

            //默认查询今天
            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", datetype);
            paramMap.put("remindTypes", remindTypes);
            Map<String, Object> onlineData = onlineCountAlarmService.countAlarmsDataByParamMap(paramMap);
            for (String s : onlineData.keySet()) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) onlineData.get(s);
                for (Map<String, Object> map : outPutInfosByParamMap) {
                    map.put(s, false);
                    for (Map<String, Object> datum : data) {
                        String _id = datum.get("_id") == null ? "" : datum.get("_id").toString();
                        Integer count = datum.get("count") == null ? 0 : Integer.valueOf(datum.get("count").toString());
                        String dgimn = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                        if (_id.equals(dgimn) && count > 0) {
                            map.put(s, true);
                        }
                    }
                    result.add(map);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result.stream().distinct().collect(Collectors.toList()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/12/17 0017 下午 4:57
     * @Description: 通过监测点id，监测点类型，监测点mn号查询污染物报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, monitorpointid, monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getPollutantAlarmInfoByParams", method = RequestMethod.POST)
    public Object getPollutantAlarmInfoByParams(@RequestJson(value = "dgimn") String dgimn,
                                                @RequestJson(value = "monitorpointid") String monitorpointid,
                                                @RequestJson(value = "monitorpointtype") Integer monitorpointtype) throws ParseException {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", monitorpointid);
            paramMap.put("outputids", Arrays.asList(monitorpointid));
            paramMap.put("pollutanttype", monitorpointtype);
            Set<Map<String, Object>> pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
            if (pollutantSetData.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                for (Map<String, Object> map : pollutantSetData) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                }
                paramMap.clear();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getLatestRealTimeDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    String YMD = DataFormatUtil.getDateYMD(document.getDate("maxtime"));
                    Map<String, Object> codeAndAlarmType = onlineService.getPollutantAlarmTypes(dgimn, pollutantcodes, YMD);
                    String maxtime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    resultMap.put("updatetime", maxtime);
                    paramMap.put("starttime", maxtime);
                    paramMap.put("endtime", maxtime);
                    paramMap.put("collection", "RealTimeData");
                    List<Document> lastRealTimeData = onlineService.getMonitorDataByParamMap(paramMap);
                    document = lastRealTimeData.get(0);
                    List<Map<String, Object>> pollutants = (List<Map<String, Object>>) document.get("RealDataList");
                    Map<String, Object> codeAndValue = new HashMap<>();
                    for (Map<String, Object> map : pollutants) {
                        codeAndValue.put(map.get("PollutantCode").toString(), map.get("MonitorValue"));
                    }
                    List<Map<String, Object>> datalist = new ArrayList<>();
                    Set<String> codes = new HashSet<>();
                    for (Map<String, Object> map : pollutantSetData) {
                        if (!codes.contains(map.get("pollutantcode"))) {
                            map.put("monitorvalue", codeAndValue.get(map.get("pollutantcode")));
                            map.put("alarmtype", codeAndAlarmType.get(map.get("pollutantcode")));
                            datalist.add(map);
                            codes.add(map.get("pollutantcode").toString());
                        }
                    }
                    resultMap.put("datalist", datalist);
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
     * @date: 2019/12/17 0017 下午 4:57
     * @Description: 通过监测点id，监测点类型，监测点mn号查询污染物报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, monitorpointid, monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getHourPollutantAlarmInfoByParams", method = RequestMethod.POST)
    public Object getHourPollutantAlarmInfoByParams(@RequestJson(value = "dgimn") String dgimn,
                                                    @RequestJson(value = "monitorpointid") String monitorpointid,
                                                    @RequestJson(value = "monitorpointtype") Integer monitorpointtype) throws ParseException {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputid", monitorpointid);
            paramMap.put("outputids", Arrays.asList(monitorpointid));
            paramMap.put("pollutanttype", monitorpointtype);
            Set<Map<String, Object>> pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
            if (pollutantSetData.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                for (Map<String, Object> map : pollutantSetData) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                }
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getLastHourDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    String YMD = DataFormatUtil.getDateYMD(document.getDate("maxtime"));
                    Map<String, Object> codeAndAlarmType = onlineService.getPollutantAlarmTypes(dgimn, pollutantcodes, YMD);
                    String maxtime = DataFormatUtil.getDateYMDHMS(document.getDate("maxtime"));
                    String updatetime = DataFormatUtil.getDateYMDH(document.getDate("maxtime"));
                    resultMap.put("updatetime", updatetime);
                    paramMap.put("starttime", maxtime);
                    paramMap.put("endtime", maxtime);
                    paramMap.put("collection", "HourData");
                    List<Document> lastRealTimeData = onlineService.getMonitorDataByParamMap(paramMap);
                    document = lastRealTimeData.get(0);
                    List<Map<String, Object>> pollutants = (List<Map<String, Object>>) document.get("HourDataList");
                    Map<String, Object> codeAndValue = new HashMap<>();
                    for (Map<String, Object> map : pollutants) {
                        codeAndValue.put(map.get("PollutantCode").toString(), map.get("AvgStrength"));
                    }
                    List<Map<String, Object>> datalist = new ArrayList<>();
                    Set<String> codes = new HashSet<>();
                    for (Map<String, Object> map : pollutantSetData) {
                        if (!codes.contains(map.get("pollutantcode"))) {
                            map.put("monitorvalue", codeAndValue.get(map.get("pollutantcode")));
                            map.put("alarmtype", codeAndAlarmType.get(map.get("pollutantcode")));
                            datalist.add(map);
                            codes.add(map.get("pollutantcode").toString());
                        }
                    }
                    resultMap.put("datalist", datalist);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/18 0018 下午 4:10
     * @Description: 通过监测点id，监测点类型，监测点mn号和数据类型查询该点位监测的污染物以及最新一条监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, monitorpointid, monitorpointtype]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointPollutantAndLastDataByParams", method = RequestMethod.POST)
    public Object getMonitorPointPollutantAndLastDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                                              @RequestJson(value = "monitorpointid") String monitorpointid,
                                                              @RequestJson(value = "datatype") Integer datatype,
                                                              @RequestJson(value = "monitorpointtype") Integer monitorpointtype) throws ParseException {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            if (monitorpointtype == WasteGasEnum.getCode()) {//所传类型为废气时 需判断是否为烟气（app）
                List<String> outputids = new ArrayList<>();
                outputids.add(monitorpointid);
                paramMap.put("outputids", outputids);
                paramMap.put("monitorPointType", monitorpointtype);
                List<Map<String, Object>> gasOutputs = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
                if (gasOutputs != null && gasOutputs.size() > 0) {
                    if (gasOutputs.get(0).get("dgimn") != null) {
                        if (gasOutputs.get(0).get("monitorpointtype") != null) {
                            monitorpointtype = Integer.parseInt(gasOutputs.get(0).get("monitorpointtype").toString());
                        }
                    }
                }
            }
            paramMap.put("outputid", monitorpointid);
            paramMap.put("outputids", Arrays.asList(monitorpointid));
            paramMap.put("pollutanttype", monitorpointtype);
            String datatypestr = "";
            String liststr = "";
            String valuekey = "";
            String zs_valuekey = "";//折算
            if (datatype == 1) {
                datatypestr = "RealTimeData";
                liststr = "RealDataList";
                zs_valuekey = "ConvertConcentration";
                valuekey = "MonitorValue";
            } else if (datatype == 2) {
                datatypestr = "MinuteData";
                liststr = "MinuteDataList";
                zs_valuekey = "AvgConvertStrength";
                valuekey = "AvgStrength";
            } else if (datatype == 3) {
                datatypestr = "HourData";
                liststr = "HourDataList";
                zs_valuekey = "AvgConvertStrength";
                valuekey = "AvgStrength";
            } else if (datatype == 4) {
                datatypestr = "DayData";
                liststr = "DayDataList";
                zs_valuekey = "AvgConvertStrength";
                valuekey = "AvgStrength";
            }
            Set<Map<String, Object>> pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
            if (pollutantSetData.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                for (Map<String, Object> map : pollutantSetData) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                }
                Document document = onlineService.getOneMonitorPointLastDataByParam(dgimn, datatypestr);
                if (document != null) {
                    String YMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    Map<String, Object> codeAndAlarmType = onlineService.getPollutantAlarmTypes(dgimn, pollutantcodes, YMD);
                    List<Map<String, Object>> pollutants = (List<Map<String, Object>>) document.get(liststr);
                    Map<String, Object> codeAndValue = new HashMap<>();
                    Map<String, Object> codeAndValuetwo = new HashMap<>();
                    for (Map<String, Object> map : pollutants) {
                        codeAndValue.put(map.get("PollutantCode").toString(), map.get(valuekey));
                        if (monitorpointtype == SmokeEnum.getCode()) {
                            codeAndValuetwo.put(map.get("PollutantCode").toString(), map.get(zs_valuekey));
                        }
                    }
                    List<Map<String, Object>> datalist = new ArrayList<>();
                    Set<String> codes = new HashSet<>();
                    for (Map<String, Object> map : pollutantSetData) {
                        if (!codes.contains(map.get("pollutantcode"))) {
                            if (monitorpointtype == SmokeEnum.getCode()) {
                                int IsHasConvertData = map.get("ishasconvertdata") != null ? Integer.parseInt(map.get("ishasconvertdata").toString()) : 0;
                                if (IsHasConvertData == 1) {
                                    map.put("monitorvalue", codeAndValuetwo.get(map.get("pollutantcode")));
                                } else {
                                    map.put("monitorvalue", codeAndValue.get(map.get("pollutantcode")));
                                }
                            } else {
                                map.put("monitorvalue", codeAndValue.get(map.get("pollutantcode")));
                            }
                            map.put("alarmtype", codeAndAlarmType.get(map.get("pollutantcode")));
                            datalist.add(map);
                            codes.add(map.get("pollutantcode").toString());
                        }
                    }
                    resultMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                    resultMap.put("datalist", datalist);
                } else {
                    List<Map<String, Object>> datalist = new ArrayList<>();
                    for (Map<String, Object> map : pollutantSetData) {
                        map.put("monitorvalue", "-");
                        map.put("alarmtype", null);
                        datalist.add(map);
                    }
                    resultMap.put("monitortime", "-");
                    resultMap.put("datalist", datalist);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/1/9 0009 下午 6:27
     * @Description: 通过自定义条件获取监测点和实时数据传输频率等信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointInfoAndMHzByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointInfoAndMHzByParamMap(@RequestJson(value = "monitorpointtypes", required = false) Object monitorpointtypes,
                                                      @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                      @RequestJson(value = "monitorpointid", required = false) String monitorpointid) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            paramMap.put("fkmonitorpointtypecodes", monitorpointtypes);
            paramMap.put("monitorpointid", monitorpointid);


            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            paramMap.clear();
            paramMap.put("mns", dgimns);
            Date now = new Date();

            Map<String, List<Map>> collect = onlineService.getLatestTwoRealTimeDataByParamMap(paramMap).stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String dgimn : collect.keySet()) {
                outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && dgimn.equals(m.get("DGIMN").toString())).forEach(m -> {
                    List<Map> list = collect.get(dgimn);
                    if (list.size() > 1) {
                        Map first = list.get(0);
                        Map second = list.get(1);
                        Date MonitorTime1 = first.get("MonitorTime") == null ? null : (Date) first.get("MonitorTime");
                        Date MonitorTime2 = second.get("MonitorTime") == null ? null : (Date) second.get("MonitorTime");
                        m.put("MonitorTime", DataFormatUtil.getDateYMDHMS(MonitorTime1));
                        m.put("realMHz", (MonitorTime1.getTime() - MonitorTime2.getTime()) / 1000);

                        Float time = (Float.valueOf(now.getTime() - MonitorTime1.getTime())) / 1000 / 60;
                        if (time >= 1) {
                            m.put("TimeDifference", "慢" + decimalFormat.format(time) + "分钟");
                            m.put("orderindex1", "慢");
                            m.put("orderindex2", time);
                            m.put("isshow", true);
                        } else if (time <= -1) {
                            m.put("TimeDifference", "快" + decimalFormat.format(Math.abs(time)) + "分钟");
                            m.put("orderindex1", "快");
                            m.put("orderindex2", Math.abs(time));
                            m.put("isshow", true);
                        }
                    }
                });
            }
            Map<String, List<Map<String, Object>>> collect2 = outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null).collect(Collectors.groupingBy(m -> m.get("FK_MonitorPointTypeCode").toString()));
            for (String FK_MonitorPointTypeCode : collect2.keySet()) {
                List<Map<String, Object>> list = collect2.get(FK_MonitorPointTypeCode);
                Map<String, Long> collect1 = list.stream().filter(m -> m.get("realMHz") != null).map(m -> m.get("realMHz").toString()).collect(Collectors.groupingBy(m -> m, Collectors.counting()));

                Optional<Long> max = collect1.values().stream().max(Long::compareTo);
                if (max.isPresent()) {
                    Optional<String> first = collect1.entrySet().stream().filter(m -> m.getValue() == max.get()).map(m -> m.getKey()).findFirst();
                    if (first.isPresent()) {
                        outPutInfosByParamMap.stream().filter(m -> m.get("FK_MonitorPointTypeCode") != null && FK_MonitorPointTypeCode.equals(m.get("FK_MonitorPointTypeCode").toString())).forEach(m -> {
                            m.put("standardMHz", first.get());
                        });
                    }
                }

            }
            Map<String, Object> resultMap = new HashMap<>();
            if (pagesize != null && pagenum != null) {
                long count = outPutInfosByParamMap.stream().filter(m -> m.get("orderindex1") != null && m.get("orderindex2") != null && ((m.get("isshow") != null && (Boolean) m.get("isshow")) || (m.get("realMHz") != null &&
                        m.get("standardMHz") != null && !m.get("realMHz").toString().equals(m.get("standardMHz").toString())))).count();
                List<Map<String, Object>> data = outPutInfosByParamMap.stream().filter(m -> m.get("orderindex1") != null && ((m.get("isshow") != null && (Boolean) m.get("isshow")) ||
                        (m.get("realMHz") != null && m.get("standardMHz") != null && !m.get("realMHz").toString().equals(m.get("standardMHz").toString())))).sorted(Comparator.comparing(m -> ((Map<String, Object>) m)
                        .get("orderindex1").toString()).thenComparing(m -> Float.valueOf(((Map<String, Object>) m).get("orderindex2").toString())).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                resultMap.put("total", count);
                resultMap.put("resultList", data);
            } else {
                List<Map<String, Object>> data = outPutInfosByParamMap.stream().filter(m -> m.get("orderindex1") != null && m.get("orderindex2") != null && ((m.get("isshow") != null && (Boolean) m.get("isshow")) || (m.get("realMHz") != null
                        && m.get("standardMHz") != null && !m.get("realMHz").toString().equals(m.get("standardMHz").toString())))).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("orderindex1").toString())
                        .thenComparing(m -> Float.valueOf(((Map<String, Object>) m).get("orderindex2").toString())).reversed()).collect(Collectors.toList());
                resultMap.put("total", data.size());
                resultMap.put("resultList", data);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/01/16 0016 下午 2:26
     * @Description: 通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取多个点位的原始数据包信息
     * @updateUser:xsm
     * @updateDate:2022/02/10
     * @updateDescription: 支持 废气烟气合并 传多个类型[2,22]
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyPointOriginalDataPackageListDataByParam", method = RequestMethod.POST)
    public Object getManyPointOriginalDataPackageListDataByParam(
            @RequestJson(value = "pollutanttype",required = false) Integer pollutanttype,
            @RequestJson(value = "pollutanttypes",required = false) List<Integer> pollutanttypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "datetypes",required = false) Object datetypes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> mns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", outputids);
            List<Map<String, Object>> onlineOutPuts = new ArrayList<>();
            if (pollutanttypes ==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            //获取点位信息
            for (Integer i:pollutanttypes){
                paramMap.put("monitorpointtype", i);
                onlineOutPuts.addAll(onlineService.getAllPollutionOutputDgimnInfoByParam(paramMap));
            }
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    unOrganizationWasteGasEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode(),
                    SmokeEnum.getCode()

            );
            Map<String, Object> mnAndPollution = new HashMap<>();
            Map<String, Object> mnAndMonitorPoint = new HashMap<>();
            for (Map<String, Object> outPut : onlineOutPuts) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                    if (outPut.get("monitorpointtype")!=null&&monitortypes.contains(Integer.valueOf(outPut.get("monitorpointtype").toString()))) {
                        mnAndPollution.put(outPut.get("dgimn").toString(), outPut.get("shortername"));
                    }
                    mnAndMonitorPoint.put(outPut.get("dgimn").toString(), outPut.get("monitorpointname"));
                }
            }
            paramMap.clear();
            String collection = "OriginalData";
            paramMap.put("mnAndPollution", mnAndPollution);
            paramMap.put("mnAndMonitorPoint", mnAndMonitorPoint);
            paramMap.put("monitorpointtype", pollutanttypes.get(0));
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("datetypes", datetypes);
            dataMap = onlineOriginalPacketService.getOriginalDataPackageListDataByParam(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

    /**
     * @author: xsm
     * @date: 2020/1/16 0016 下午 17:09
     * @Description:导出通过污染物类型（水-1，气-2，雨水-3，5-空气，恶臭-9，10-VOC）和自定义参数获取多个点位的原始数据包信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "exportManyPointOriginalDataPackageListDataByParam", method = RequestMethod.POST)
    public void exportManyPointOriginalDataPackageListDataByParam(@RequestJson(value = "pollutanttype",required = false) Integer pollutanttype,
                                                                  @RequestJson(value = "pollutanttypes",required = false) List<Integer> pollutanttypes,
                                                                  @RequestJson(value = "outputids") List<String> outputids,
                                                                  @RequestJson(value = "starttime") String starttime,
                                                                  @RequestJson(value = "endtime") String endtime,
                                                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            if (pollutanttypes ==null||pollutanttypes.size()==0){
                pollutanttypes = new ArrayList<>();
                if (pollutanttype!=null){
                    pollutanttypes.add(pollutanttype);
                }
            }
            List<Map<String, Object>> tabletitledata = onlineOriginalPacketService.getTableTitleForOriginalPackageList(pollutanttypes.get(0));
            List<String> mns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", outputids);
            List<Map<String, Object>> onlineOutPuts = new ArrayList<>();
            for (Integer i:pollutanttypes){
                paramMap.put("monitorpointtype", i);
                onlineOutPuts.addAll(onlineService.getAllPollutionOutputDgimnInfoByParam(paramMap));
            }
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(),
                    WasteGasEnum.getCode(),
                    SmokeEnum.getCode(),
                    RainEnum.getCode(),
                    unOrganizationWasteGasEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            Map<String, Object> mnAndPollution = new HashMap<>();
            Map<String, Object> mnAndMonitorPoint = new HashMap<>();
            //Map<String, Object> mnAndtype = new HashMap<>();
            for (Map<String, Object> outPut : onlineOutPuts) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                    if (outPut.get("monitorpointtype")!=null&&monitortypes.contains(Integer.valueOf(outPut.get("monitorpointtype").toString()))) {
                        mnAndPollution.put(outPut.get("dgimn").toString(), outPut.get("shortername"));
                    }
                    /*if (outPut.get("monitorpointtype")!=null){
                        mnAndtype.put(outPut.get("dgimn").toString(), outPut.get("monitorpointtype"));
                    }*/
                    mnAndMonitorPoint.put(outPut.get("dgimn").toString(), outPut.get("monitorpointname"));
                }
            }
            paramMap.clear();
            String collection = "OriginalData";
            paramMap.put("mnAndPollution", mnAndPollution);
            paramMap.put("mnAndMonitorPoint", mnAndMonitorPoint);
            paramMap.put("monitorpointtype", pollutanttypes.get(0));
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", mns);
            dataMap = onlineOriginalPacketService.getOriginalDataPackageListDataByParam(paramMap);
            dataMap.put("tabletitledata", tabletitledata);
            List<Map<String, Object>> collect = (List<Map<String, Object>>) dataMap.get("datalist");
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "";
            if (pollutanttypes.size()>1){
                fileName = "废气监测原始数据导出文件_" + new Date().getTime();
            }else {
                if (pollutanttypes.size() ==1) {
                    pollutanttype = pollutanttypes.get(0);
                    if (pollutanttype == WasteGasEnum.getCode()) {//废气
                        fileName = "废气监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == SmokeEnum.getCode()) {//烟气
                        fileName = "烟气监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == WasteWaterEnum.getCode()) {//废水
                        fileName = "废水监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == RainEnum.getCode()) {//雨水
                        fileName = "雨水监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode()) {//厂界小型站
                        fileName = "厂界小型站监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == FactoryBoundaryStinkEnum.getCode()) {//厂界恶臭
                        fileName = "厂界恶臭监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {//空气站
                        fileName = "大气监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {//恶臭
                        fileName = "恶臭监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {//voc
                        fileName = "Voc监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()) {//水质
                        fileName = "水质监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                        fileName = "扬尘监测原始数据导出文件_" + new Date().getTime();
                    } else if (pollutanttype == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {//微站
                        fileName = "微站监测原始数据导出文件_" + new Date().getTime();
                    }
                }
            }
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/14 0014 下午 3:08
     * @Description: 在线数据审核
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype, dgimns]
     * @throws:
     */
    @RequestMapping(value = "examineOnlineDataByParams", method = RequestMethod.POST)
    public Object examineOnlineDataByParams(@RequestJson(value = "starttime") String starttime,
                                            @RequestJson(value = "endtime") String endtime,
                                            @RequestJson(value = "datetype") String datetype,
                                            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                            @RequestJson(value = "dgimns") Object dgimns) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("tablename", "PUB_CODE_PollutantFactor");
            paramMap.put("wherestring", "PollutantType in ('"+monitorpointtype+"') and isused=1");
            List<Map<String, Object>> pollutants = pubCodeService.getPubCodeDataByParam(paramMap);

            String dataList = "";
            if ("day".equals(datetype)) {
                dataList = "DayDataList";
            } else if ("hour".equals(datetype)) {
                dataList = "HourDataList";
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", datetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

            List<Map<String, String>> monitorPointAndPollutionInfo = gasOutPutInfoService.getMonitorPointAndPollutionInfo(paramMap);


            for (Map<String, Object> onlinedata : thisMonthwaterStationOnlineData) {
                Map<String, Object> data = new HashMap<>();
                Integer auditstatus = 0;
                String DataGatherCode = onlinedata.get("DataGatherCode") == null ? "" : onlinedata.get("DataGatherCode").toString();
                String _id = onlinedata.get("_id") == null ? "" : onlinedata.get("_id").toString();
                data.put("dgimn", DataGatherCode);
                if (onlinedata.get("MonitorTime") != null) {
                    String MonitorTime = DataFormatUtil.formatCST(onlinedata.get("MonitorTime").toString());
                    data.put("MonitorTime", MonitorTime);
                }
                monitorPointAndPollutionInfo.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN")))
                        .forEach(m -> {
                            data.put("PollutionName", m.get("PollutionName"));
                            data.put("monitorpointname", m.get("outputname"));
                            data.put("Pollutionid", m.get("Pollutionid"));
                            data.put("monitorpointid", m.get("outputid"));
                        });
                List<Map<String, Object>> maps = (List<Map<String, Object>>) (onlinedata.get(dataList) == null ? new ArrayList<>() : onlinedata.get(dataList));
                for (Map<String, Object> map : maps) {
                    String PollutantCode = map.get("PollutantCode") == null ? "" : map.get("PollutantCode").toString();
                    Optional<Map<String, Object>> first = pollutants.stream().filter(n -> n.get("Code") != null && PollutantCode.equals(n.get("Code").toString())).findFirst();
                    if (first.isPresent()) {
                        Map<String, Object> map1 = first.get();
                        if (map1.get("Name") != null) {
                            String name = map1.get("Name").toString();
                            String code = map1.get("Code").toString();
                            data.put(name + "_name", name);
                            data.put(name + "_code", code);
                            data.put(name + "_AvgStrength", map.get("AvgStrength"));
                            data.put(name + "_isaudit", false);
                            if (map.get("AuditManId") != null && !"".equals(map.get("AuditManId").toString())) {
                                data.put(name + "_isaudit", true);
                                auditstatus++;
                            }
                        }
                    }

                }
                if (maps.size() == 0) {
                    data.put("auditstatus", "无数据");
                } else if (auditstatus == 0) {
                    data.put("auditstatus", "未审核");
                } else if (auditstatus == maps.size()) {
                    data.put("auditstatus", "已审核");

                } else if (auditstatus < maps.size()) {
                    data.put("auditstatus", "部分审");

                }
                data.put("onlineid", _id);
                resultList.add(data);
            }


            String finalDataList = dataList;
            List<Map<String, Object>> titleList = new ArrayList<>();
            thisMonthwaterStationOnlineData.stream().filter(m -> m.get(finalDataList) != null).flatMap(m -> ((List<Map<String, Object>>) m.get(finalDataList)).stream()).
                    filter(m -> m.get("PollutantCode") != null).forEach(m -> {
                Map<String, Object> map = new HashMap<>();
                String pollutantCode = m.get("PollutantCode").toString();
                map.put("pollutantcode", pollutantCode);
                pollutants.stream().filter(n -> n.get("Code") != null && pollutantCode.equals(n.get("Code").toString())).forEach(n -> {
                    map.put("pollutantname", n.get("Name"));
                    map.put("pollutantnamelower", n.get("Name").toString().toLowerCase());
                    titleList.add(map);
                });
            });

            resultMap.put("datalist", resultList);
            resultMap.put("titlelist", titleList.stream().distinct().collect(Collectors.toList()));


            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/2/18 0018 下午 6:29
     * @Description: 数据审核修改操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, datetype, alarmdata, session]
     * @throws:
     */
    @RequestMapping(value = "updateExamineOnlineData", method = RequestMethod.POST)
    public Object updateExamineOnlineData(@RequestJson(value = "paramsjson") Object paramsjson, @RequestJson(value = "datetype") String datetype
            , @RequestJson(value = "alarmdata") Object alarmdata) throws ParseException {
        try {

            JSONArray jsonArray = JSONArray.fromObject(paramsjson);
            Date now = new Date();
            Map<String, Object> alarmData = (Map) alarmdata;
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String collection = "";
            String datatype = "";
            if ("hour".equals(datetype)) {
                collection = "HourData";
                datatype = "HourData";
            } else if ("day".equals(datetype)) {
                collection = "DayData";
                datatype = "DayData";
            }

            Object collect = jsonArray.stream().filter(m -> ((Map) m).get("pollutants") != null).flatMap(m -> ((List<Map<String, Object>>) ((Map) m).get("pollutants")).stream().filter(n -> n.get("code") != null)
                    .map(n -> n.get("code").toString())).collect(Collectors.toList());
            JSONArray Pollutants = JSONArray.fromObject(collect);
            Object collect1 = jsonArray.stream().filter(m -> ((Map) m).get("dgimn") != null).map(m -> ((Map) m).get("dgimn").toString()).collect(Collectors.toList());
            JSONArray DGIMNs = JSONArray.fromObject(collect1);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimns", DGIMNs);
            paramMap.put("pollutants", Pollutants);
            List<Map<String, Object>> alarmTypeInfo = gasOutPutPollutantSetService.getPollutantSetInfoByParamMap(paramMap);

            for (Object o : jsonArray) {
                HourDataVO hourDataVO = new HourDataVO();
                Map<String, Object> map = (Map) o;
                JSONArray pollutants = JSONArray.fromObject(map.get("pollutants"));
                String onlineid = map.get("onlineid") == null ? "" : map.get("onlineid").toString();
                String dgimn = map.get("dgimn") == null ? "" : map.get("dgimn").toString();
                hourDataVO.setId(onlineid);
                List<HourDataVO> hourData = mongoBaseService.getListByParam(hourDataVO, collection, null);
                for (HourDataVO hourDatum : hourData) {
                    List<Map<String, Object>> hourDataList = hourDatum.getHourDataList();
                    Date parse = hourDatum.getMonitorTime();
                    for (Object pollutant : pollutants) {
                        Map<String, Object> pollutantmap = (Map) pollutant;
                        String code = pollutantmap.get("code") == null ? "" : pollutantmap.get("code").toString();
                        String avgstrength = pollutantmap.get("avgstrength") == null ? "" : pollutantmap.get("avgstrength").toString();
                        String finalDatatype = datatype;
                        hourDataList.stream().filter(m -> m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString())).forEach(m -> {
                            m.put("AvgStrength", avgstrength);
                            String alarmdatetype = alarmData.get("alarmdatetype") == null ? "" : alarmData.get("alarmdatetype").toString();
                            String alarmtype = "";
                            m.put("AuditManId", userid);
                            m.put("AuditTime", now);


                            Optional<Map<String, Object>> first = alarmTypeInfo.stream().filter(n -> n.get("FK_PollutantCode") != null && n.get("dgimn") != null && n.get("FK_PollutantCode").toString().equals(code) &&
                                    n.get("dgimn").toString().equals(dgimn)).findFirst();
                            if (first.isPresent() && first.get().get("AlarmType") != null) {
                                alarmtype = first.get().get("AlarmType").toString();
                            }

                            if ("overdata".equals(alarmdatetype)) {
                                m.put("IsOver", alarmData.get("alarmlevel"));
                                m.put("IsOverStandard", true);

                                AlarmDataVO alarmDataVO = new AlarmDataVO();
                                if (alarmData.get("alarmlevel") != null) {
                                    Integer alarmlevel = Integer.valueOf(alarmData.get("alarmlevel").toString());
                                    alarmDataVO.setAlarmLevel(alarmlevel);
                                }
                                alarmDataVO.setAlarmType(alarmtype);
                                alarmDataVO.setDataGatherCode(dgimn);
                                alarmDataVO.setMonitorValue(avgstrength);
                                alarmDataVO.setOverTime(parse);
                                alarmDataVO.setPollutantCode(code);
                                alarmDataVO.setOverStandard(true);
                                alarmDataVO.setDataType(finalDatatype);
                                mongoBaseService.save(alarmDataVO);
                            } else if ("exceptiondata".equals(alarmdatetype)) {
                                m.put("IsException", alarmData.get("exceptionlevel"));
                                ExceptionDataVO exceptionDataVO = new ExceptionDataVO();
                                if (alarmData.get("alarmlevel") != null) {
                                    exceptionDataVO.setExceptionType(alarmData.get("exceptionlevel").toString());
                                }
                                exceptionDataVO.setDataGatherCode(dgimn);
                                exceptionDataVO.setMonitorValue(avgstrength);
                                exceptionDataVO.setExceptionTime(parse);
                                exceptionDataVO.setPollutantCode(code);
                                exceptionDataVO.setDataType(finalDatatype);
                                mongoBaseService.save(exceptionDataVO);
                            } else if ("earlydata".equals(alarmdatetype)) {
                                m.put("IsOver", alarmData.get("alarmlevel"));

                                EarlyDataVO earlyDataVO = new EarlyDataVO();
                                if (alarmData.get("alarmlevel") != null) {
                                    Integer alarmlevel = Integer.valueOf(alarmData.get("alarmlevel").toString());
                                    earlyDataVO.setAlarmLevel(alarmlevel);
                                }
                                earlyDataVO.setAlarmType(alarmtype);
                                earlyDataVO.setDataGatherCode(dgimn);
                                earlyDataVO.setMonitorValue(avgstrength);
                                earlyDataVO.setEarlyWarnTime(parse);
                                earlyDataVO.setPollutantCode(code);
                                earlyDataVO.setDataType(finalDatatype);
                                mongoBaseService.save(earlyDataVO);
                            }
                        });
                    }

                    mongoBaseService.update(hourDatum);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/10 0010 下午 6:03
     * @Description: 获取污染物信息，包含是否异常，超标等信息
     * @updateUser:xsm
     * @updateDate:2021/06/16 0016 上午 10:29
     * @updateDescription:统计当天的点位各污染物是否有报警（突变、预警、异常、超标）
     * @param: [pollutanttype, dgimns, starttime, endtime, outputid]
     * @throws:
     */
    @RequestMapping(value = "getPollutantAndIsalaramInfo", method = RequestMethod.POST)
    public Object getPollutantAndIsalaramInfo(
            @RequestJson(value = "pollutanttype") Integer pollutanttype,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputid") String outputid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            /*Date now = new Date();
            Calendar instance = Calendar.getInstance();
            instance.setTime(now);
            instance.add(Calendar.HOUR,-12);
            Date time = instance.getTime();*/


            Object pollutantsByOutputIdAndPollutantType = pollutantController.getPollutantsByOutputIdAndPollutantType(outputid, pollutanttype);

            pollutantsByOutputIdAndPollutantType = AuthUtil.decryptData(pollutantsByOutputIdAndPollutantType);

            Map<String, Object> pollutants = (Map) JSONObject.fromObject(pollutantsByOutputIdAndPollutantType);
            List<Map<String, Object>> list = (List<Map<String, Object>>) pollutants.get("data");

            List<String> pollutantcodes = list.stream().filter(m -> m.get("pollutantcode") != null).map(m -> m.get("pollutantcode").toString()).collect(Collectors.toList());

            paramMap.put("mns", dgimns);
            paramMap.put("dgimns", dgimns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("endtime", endtime);
            paramMap.put("starttime", starttime);
            //paramMap.put("collection", "RealTimeData");
            //paramMap.put("monitorpointtype", pollutanttype);
            //paramMap.put("pollutanttype", pollutanttype);

           // paramMap.put("remindtype", 1);//浓度突变

            //污染物突变数据
            /*List<Map<String, Object>> PollutantSuddenChangeDataByParamMap = onlineService.getPollutantSuddenChangeDataByParamMap(paramMap);

            List<Map<String, Object>> collect = onlineService.getMonitorDataByParamMap(paramMap).stream().filter(m -> m.get("RealDataList") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("RealDataList")).stream()).collect(Collectors.toList());
            paramMap.put("collection", "MinuteData");
            List<Map<String, Object>> collect1 = onlineService.getMonitorDataByParamMap(paramMap).stream().filter(m -> m.get("MinuteDataList") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("MinuteDataList")).stream()).collect(Collectors.toList());
            paramMap.put("collection", "HourData");
            List<Map<String, Object>> collect2 = onlineService.getMonitorDataByParamMap(paramMap).stream().filter(m -> m.get("HourDataList") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("HourDataList")).stream()).collect(Collectors.toList());

            collect.addAll(collect1);
            collect.addAll(collect2);*/
            //超标
            List<String> cb_codelist = onlineMonitorService.getAlarmPollutanByAlarmTypeAndTimes(dgimns,pollutantcodes,starttime,endtime,OverAlarmEnum.getCode(),"yes");
            //超限
            List<String> cx_codelist = onlineMonitorService.getAlarmPollutanByAlarmTypeAndTimes(dgimns,pollutantcodes,starttime,endtime,OverAlarmEnum.getCode(),"no");
            //预警
            List<String> yj_codelist = onlineMonitorService.getAlarmPollutanByAlarmTypeAndTimes(dgimns,pollutantcodes,starttime,endtime,EarlyAlarmEnum.getCode(),"");
            //异常
            List<String> yc_codelist = onlineMonitorService.getAlarmPollutanByAlarmTypeAndTimes(dgimns,pollutantcodes,starttime,endtime,ExceptionAlarmEnum.getCode(),"");
            //浓度突变
            List<String> ndtb_codelist = onlineMonitorService.getAlarmPollutanByAlarmTypeAndTimes(dgimns,pollutantcodes,starttime,endtime,ConcentrationChangeEnum.getCode(),"");
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                String pollutantcode = map.get("pollutantcode") == null ? "" : map.get("pollutantcode").toString();
                map.put("isexception", false);
                map.put("isover", false);
                map.put("IsOverStandard", false);
                map.put("iswarn", false);//预警
                map.put("IsSuddenChange", false);
                map.put("index", i);

               /* long isover = collect.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) && m.get("IsOver") != null &&
                        Integer.valueOf(m.get("IsOver").toString()) > 0).count();
                long iswarn = collect.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) && m.get("IsOver") != null &&
                        Integer.valueOf(m.get("IsOver").toString()) == 0).count();

                long isexception = collect.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) && m.get("IsException") != null &&
                        Integer.valueOf(m.get("IsException").toString()) >= 1).count();
                long IsOverStandard = collect.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) && m.get("IsOverStandard") != null &&
                        (Boolean) m.get("IsOverStandard")).count();
                long IsSuddenChange = PollutantSuddenChangeDataByParamMap.stream().filter(m -> m.get("pollutantcode") != null && pollutantcode.equals(m.get("pollutantcode").toString())).count();
*/
              /*  if (isexception > 0) {
                    map.put("isexception", true);
                }
                if (IsOverStandard > 0) {
                    map.put("IsOverStandard", true);
                }
                if (isover > 0) {
                    map.put("isover", true);
                }
                if (iswarn > 0) {
                    map.put("iswarn", true);
                }
                if (IsSuddenChange > 0) {
                    map.put("IsSuddenChange", true);
                }*/
                if (cb_codelist!=null&&cb_codelist.size() > 0) {
                    if (cb_codelist.contains(pollutantcode)) {
                        map.put("IsOverStandard", true);
                    }
                }
                if (cx_codelist!=null&&cx_codelist.size() > 0) {
                    if (cx_codelist.contains(pollutantcode)) {
                        map.put("isover", true);
                    }
                }
                if (yj_codelist!=null&&yj_codelist.size() > 0) {
                    if (yj_codelist.contains(pollutantcode)) {
                        map.put("iswarn", true);
                    }
                }
                if (yc_codelist!=null&&yc_codelist.size() > 0) {
                    if (yc_codelist.contains(pollutantcode)) {
                        map.put("isexception", true);
                    }
                }
                if (ndtb_codelist!=null&&ndtb_codelist.size() > 0) {
                    if (ndtb_codelist.contains(pollutantcode)) {
                        map.put("IsSuddenChange", true);
                    }
                }
            }
            list = list.stream().filter(m -> m.get("isexception") != null && m.get("isover") != null && m.get("IsOverStandard") != null && m.get("iswarn") != null && m.get("IsSuddenChange") != null && m.get("index") != null)
                    .sorted(Comparator.comparing(m -> (Boolean) ((Map) m).get("isexception")).thenComparing(m -> (Boolean) ((Map) m).get("IsOverStandard") || (Boolean) ((Map) m).get("isover"))
                            .thenComparing(m -> (Boolean) ((Map) m).get("iswarn")).thenComparing(m -> (Boolean) ((Map) m).get("IsSuddenChange")).reversed().thenComparing(m -> ((Map) m).get("index").toString())).collect(Collectors.toList());

            return AuthUtil.parseJsonKeyToLower("success", list);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzhenchao
     * @date: 2020/2/25 22:58
     * @Description: 实时数据一览添加排口状态查询条件
     * @param:
     * @return:
     */
    public static void formatParamMapForRealTimeSee(Map<String, Object> paramMap) {
        if (paramMap.get("onlineoutputstatus") != null && !paramMap.get("onlineoutputstatus").equals("")) {
            List<String> onlineoutputstatus = new ArrayList<>();
            String[] types = paramMap.get("onlineoutputstatus").toString().split(",");
            Collections.addAll(onlineoutputstatus, types);
            paramMap.put("onlineoutputstatus", onlineoutputstatus);
        } else {
            paramMap.remove("onlineoutputstatus");
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/11 0011 上午 9:14
     * @Description: 查询单个排口下单个污染物报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, starttime, pollutantcode, endtime, pollutanttype]
     * @throws:
     */
    @RequestMapping(value = "/getOnePollutantAlarmdata", method = RequestMethod.POST)
    private Object getOnePollutantAlarmdata(@RequestJson(value = "dgimns") List<String> dgimns,
                                            @RequestJson(value = "starttime") String starttime,
                                            @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                            @RequestJson(value = "endtime") String endtime,
                                            @RequestJson(value = "pollutanttype") Integer pollutanttype) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();

        String dateYMD = DataFormatUtil.getDateYMD(new Date());

        paramMap.put("pollutanttype", pollutanttype);
        paramMap.put("pollutantcodes", Arrays.asList(pollutanttype));
        List<Map<String, Object>> pollutants = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);
        List<String> pollutantcodes = pollutants.stream().filter(m -> m.get("pollutantcode") != null).map(m -> m.get("pollutantcode").toString()).distinct().collect(Collectors.toList());
        Map<String, String> pollutantmap = pollutants.stream()
                .filter(m -> m.get("pollutantcode") != null && m.get("pollutantname") != null)
                .collect(Collectors.toMap(m -> m.get("pollutantcode").toString(), m -> m.get("pollutantname").toString(), (a, b) -> a));

        paramMap.put("dgimns", dgimns);
        paramMap.put("starttime", JSONObjectUtil.getStartTime(dateYMD));
        paramMap.put("endtime", JSONObjectUtil.getEndTime(dateYMD));
        paramMap.put("pollutantcode", pollutantcode);
        List<Map<String, Object>> result = onlineMonitorService.getOnePointAndOnePollutantAllAlarmTimes(paramMap,pollutantmap);
        /*paramMap.put("monitortimekey", "ExceptionTime");
        paramMap.put("collection", "ExceptionData");

        List<Map<String, Object>> PollutantSuddenChangeDataByParamMap = onlineService.getPollutantSuddenChangeDataByParamMap(paramMap);


        List<Document> exceptionModelData = onlineService.getExceptionModelData(paramMap);

        List<Document> overModelData = onlineService.getOverModelData(paramMap);

        Map<String, List<Document>> collect4 = overModelData.stream().filter(m -> m.get("MN") != null).collect(Collectors.groupingBy(m -> m.get("MN").toString()));


        Map<String, List<Document>> collect3 = exceptionModelData.stream()
                .filter(m -> m.get("MN") != null && m.get("ExceptionType") != null)
                .collect(Collectors.groupingBy(m -> m.get("MN").toString()+"_"+m.get("ExceptionType").toString()));
        Map<String, Object> data = new HashMap<>();
        for (String mnAndExceptionType : collect3.keySet()) {
            List<Document> documentList = collect3.get(mnAndExceptionType);

            String[] split = mnAndExceptionType.split("_");
            List<String> linelist=new ArrayList<>();
            documentList.stream().filter(m->m.get("FirstExceptionTime")!=null && m.get("PollutantCode")!=null && pollutantcode.equals(m.get("PollutantCode").toString()))
                    .sorted(Comparator.comparing(m->m.get("FirstExceptionTime").toString())).forEach(m->{
                String FirstExceptionTime = m.get("FirstExceptionTime") == null ? "" : formatCSTString(m.get("FirstExceptionTime").toString(), "HH:mm");
                String LastExceptionTime = m.get("LastExceptionTime") == null ? "" : formatCSTString(m.get("LastExceptionTime").toString(), "HH:mm");
                linelist.add(FirstExceptionTime+"-"+LastExceptionTime);
            });

            if(split.length>1 && linelist.size()>0){
                if (ZeroExceptionEnum.getCode().equals(split[1])) {//零值异常
                    data.put("zeroexception", linelist.stream().collect(Collectors.joining("、")));
                }
                if (ContinuousExceptionEnum.getCode().equals(split[1])) {//连续值
                    data.put("continuousexception", linelist.stream().collect(Collectors.joining("、")));
                }
                if (OverExceptionEnum.getCode().equals(split[1])) {//超限
                    data.put("overexception", linelist.stream().collect(Collectors.joining("、")));
                }
                if (NoFlowExceptionEnum.getCode().equals(split[1])) {//无流量异常
                    data.put("noflow", linelist.stream().collect(Collectors.joining("、")));
                }
            }
            data.put("pollutantname", pollutantmap.get(pollutantcode));
            data.put("pollutantcode", pollutantcode);
        }

        List<Document> overlist = collect4.get(dgimns.get(0));
        if(overlist!=null){
            List<String> overlinelist=new ArrayList<>();
            overlist.stream().filter(m->m.get("FirstOverTime")!=null && m.get("PollutantCode")!=null && pollutantcode.equals(m.get("PollutantCode").toString()))
                    .sorted(Comparator.comparing(m->m.get("FirstOverTime").toString())).forEach(m->{
                String FirstOverTime = m.get("FirstOverTime") == null ? "" : formatCSTString(m.get("FirstOverTime").toString(), "HH:mm");
                String LastOverTime = m.get("LastOverTime") == null ? "" : formatCSTString(m.get("LastOverTime").toString(), "HH:mm");
                overlinelist.add(FirstOverTime+"-"+LastOverTime);
            });
            if (overlinelist.size() > 0) {
                data.put("overdata", overlinelist.stream().collect(Collectors.joining("、")));
            }
        }

        Map<String, Object> first = PollutantSuddenChangeDataByParamMap.stream().filter(m -> m.get("pollutantcode") != null && pollutantcode.equals(m.get("pollutantcode").toString())).findFirst().orElse(new HashMap<>());
        if(first.get("timepoints")!=null){
            data.put("suddenchange", first.get("timepoints"));
        }
        if (!data.isEmpty()) {
            resultList.add(data);
        }*/

        //return AuthUtil.parseJsonKeyToLower("success", resultList.stream().distinct().collect(Collectors.toList()));
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @author: chengzq
     * @date: 2020/2/20 0020 上午 10:17
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [cst, pattern]
     * @throws:
     */
    public static String formatCSTString(String cst, String pattern){
        //获取监测时间
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date d = null;
        try {
            d = sdf.parse(cst);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formatDate = new SimpleDateFormat(pattern).format(d);
        return formatDate;
    }


    public static void formatCSTString(Map<String, Object> data, String pattern) throws ParseException {
        String monitortime = data.get("MonitorTime").toString();
        String newtime = formatCSTString(monitortime, pattern);
        data.put("MonitorTime", newtime);
    }

    /**
     * @author: xsm
     * @date: 2020/3/12 0012 下午 12:38
     * @Description: 通过自定义条件查询废水无流量异常数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getWasteWaterNoFlowExceptionListDataByParams", method = RequestMethod.POST)
    public Object getWasteWaterNoFlowExceptionListDataByParams(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                               @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                               @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                               @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                               @RequestJson(value = "starttime") String starttime,
                                                               @RequestJson(value = "endtime") String endtime,
                                                               @RequestJson(value = "pageflag", required = false) String pageflag,
                                                               @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                               @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws ParseException {
        try {
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode()
            );
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitortype", monitorpointtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            List<Map<String, Object>> stoplist = new ArrayList<>();
            //获取所点位名称和MN号
            if (monitortypes.contains(monitorpointtype)) {
                monitorPoints = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {//为废气、废水排口时
                    paramMap.put("monitorpointtype", monitorpointtype);
                    stoplist = stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(paramMap);
                }/*else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                    paramMap.put("monitorpointtype", monitorpointtype);
                    stoplist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
                }*/
            }
            String mnCommon;
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            Map<String, Object> pointidAndTaskid = new HashMap<>();
            Map<String, Object> pointidAndStatus = new HashMap<>();
            Map<String, List<Map<String, Object>>> pointidAndrtsp = new HashMap<>();
            //获取相关视频信息
            List<Map<String, Object>> videos = videoCameraService.getVideoInfoByMonitorpointType(monitorpointtype);
            if (videos != null && videos.size() > 0) {
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videos) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videos) {
                                if (map2.get("monitorpointid") != null && (map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))) {
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp", map2.get("rtsp"));
                                    objmap.put("name", map2.get("name"));
                                    objmap.put("id", map2.get("pkid"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(), rtsplist);
                        }
                        //
                    }
                }
            }
            //根据报警类型和监测时间获取报警任务信息
            List<Map<String, Object>> tasklist = devOpsTaskDisposeService.getAlarmTaskInfoByRemindTypeAndParamMap(paramMap);
            if ("homepage".equals(pageflag)) {//判断是否为首页调用该接口 是则关联任务
                if (tasklist.size() > 0) {
                    for (Map<String, Object> taskmap : tasklist) {
                        pointidAndTaskid.put(taskmap.get("Pollutionid") != null ? taskmap.get("Pollutionid").toString() : "", taskmap.get("PK_TaskID"));
                        pointidAndStatus.put(taskmap.get("Pollutionid") != null ? taskmap.get("Pollutionid").toString() : "", taskmap.get("TaskStatus"));
                    }
                }
            }
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    if (monitorpointtype == WasteWaterEnum.getCode() || monitorpointtype == WasteGasEnum.getCode() || monitorpointtype == SmokeEnum.getCode()) {
                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> stopmap : stoplist) {
                                if ((map.get("pk_id").toString()).equals(stopmap.get("FK_Outputid").toString())) {
                                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname") + "【停产】");
                                }
                            }
                        }
                    }
                    mnAndMonitorPointId.put(mnCommon, map.get("pk_id"));
                    mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));

                }
            }

            paramMap.put("dgimns", dgimns);
            paramMap.put("usercode", usercode);
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getWasteWaterNoFlowExceptionListDataByParams(paramMap);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) onlineDataGroupMmAndMonitortime.get("datalist");

            Set<String> pollutantNameList;
            String pollutantNames;
            List<String> tempList;
            for (Map<String, Object> dataMap : dataList) {
                mnCommon = dataMap.get("datagathercode").toString();
                pollutantNameList = getPollutantNameAndExceptionTypeByCodes(dataMap.get("pollutantCodes"), (List<Map<String, Object>>) dataMap.get("exceptiontypes"), Arrays.asList(monitorpointtype));
                tempList = new ArrayList<>(pollutantNameList);
                pollutantNames = DataFormatUtil.FormatListToString(tempList, "、");
                dataMap.put("pollutantnames", pollutantNames);
                dataMap.put("pollutionname", mnAndPollutionName.get(mnCommon));
                dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                dataMap.put("pollutionid", mnAndPollutionId.get(mnCommon));
                if ("homepage".equals(pageflag)) {//判断是否为首页调用该接口 是则关联任务
                    if (monitortypes.contains(monitorpointtype)) {//关联企业的排口和厂界点位
                        dataMap.put("taskid", mnAndPollutionId.get(mnCommon) != null ? pointidAndTaskid.get(mnAndPollutionId.get(mnCommon)) : null);
                        dataMap.put("status", mnAndPollutionId.get(mnCommon) != null ? pointidAndStatus.get(mnAndPollutionId.get(mnCommon)) : null);
                    } else {//不关联企业的点位
                        dataMap.put("taskid", mnAndMonitorPointId.get(mnCommon) != null ? pointidAndTaskid.get(mnAndMonitorPointId.get(mnCommon)) : null);
                        dataMap.put("status", mnAndMonitorPointId.get(mnCommon) != null ? pointidAndStatus.get(mnAndMonitorPointId.get(mnCommon)) : null);
                    }
                }
                dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                dataMap.put("rtsplist", pointidAndrtsp.get(mnAndMonitorPointId.get(mnCommon)));
            }
            onlineDataGroupMmAndMonitortime.remove("datalist");
            onlineDataGroupMmAndMonitortime.put("data", dataList);

            return AuthUtil.parseJsonKeyToLower("success", onlineDataGroupMmAndMonitortime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/12 0012 下午 13:38
     * @Description: 通过自定义条件统计废水无流量异常和非无流量异常的报警数据条数
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countNoFlowAndOtherExceptionTypeDataNum", method = RequestMethod.POST)
    public Object countNoFlowAndOtherExceptionTypeDataNum(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime
    ) throws ParseException {
        try {
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitortype", monitorpointtype);
            List<Map<String, Object>> stoplist = new ArrayList<>();
            //获取所点位名称和MN号
            List<Map<String, Object>> monitorPoints = gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap);
            String mnCommon;
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);

                }
            }
            paramMap.put("dgimns", dgimns);
            result = onlineService.countNoFlowAndOtherExceptionTypeDataNum(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    private List<Document> countWaterNoFlowExceptionPollutantByParam(Map<String, Object> paramMap) {

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
        //当为异常数据时 去掉无流量异常
        Criteria criteria = new Criteria();
        criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        Fields fields;
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            fields = fields("DataGatherCode").and("PollutantCode", unwindkey + ".PollutantCode");
        } else {
            fields = fields("DataGatherCode", "PollutantCode");
        }
        aggregations.add(project(fields));
        GroupOperation groupOperation = group("DataGatherCode", "PollutantCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }


    /**
     * @author: chengzq
     * @date: 2020/3/23 0023 下午 9:05
     * @Description: 汉源项目接口，获取空气中重金属或者土壤监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getAirHeavyMetalOrSoilMonitorData", method = RequestMethod.POST)
    public Object getAirHeavyMetalOrSoilMonitorData(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime,
                                                    @RequestJson(value = "pollutantcode") String pollutantcode
    ) throws ParseException {
        try {
            List<Map<String, Object>> monitorpoint = new ArrayList<>();
            String monitortimekey = "";
            String collection = "";
            String pattern = "";
            Map<String, Object> paramMap = new HashMap<>();
            if (monitorpointtype == AirEnum.getCode()) {//查询空气监测点
                monitortimekey = "HourDataList";
                collection = "HourData";
                pattern = "yyyy-MM-dd HH";
                starttime += ":00:00";
                endtime += ":59:59";
                monitorpoint = airMonitorStationService.getAllAirMonitorStation(paramMap);
            } else if (monitorpointtype == soilEnum.getCode()) {//土壤监测点
                monitortimekey = "DayDataList";
                collection = "DayData";
                pattern = "yyyy-MM-dd";
                starttime += " 00:00:00";
                endtime += " 23:59:59";
                monitorpoint = soilPointService.getSoilPointByParamMap(paramMap);
            }
            List<String> dgimns = monitorpoint.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            if (dgimns.size() == 0) {
                return AuthUtil.parseJsonKeyToLower("success", new ArrayList<>());
            }
            paramMap.put("mns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantcodes", Arrays.asList(new String[]{pollutantcode}));
            paramMap.put("monitortimekey", monitortimekey);
            paramMap.put("collection", collection);

            String finalMonitortimekey = monitortimekey;
            String finalPattern = pattern;
            List<Map<String, Object>> data = onlineService.getMonitorDataByParamMap(paramMap).stream().filter(m -> m.get(finalMonitortimekey) != null).peek(m -> {
                String monitorTime = m.get("MonitorTime").toString();
                String DataGatherCode = m.get("DataGatherCode").toString();
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) m.get(finalMonitortimekey);
                dataList.stream().forEach(n -> {
                    n.put("MonitorTime", formatCSTString(monitorTime, finalPattern));
                    n.put("DataGatherCode", DataGatherCode);
                });
            }).flatMap(m -> ((List<Map<String, Object>>) m.get(finalMonitortimekey)).stream()).filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Map<String, Object> map : data) {
                String DataGatherCode = map.get("DataGatherCode") == null ? "" : map.get("DataGatherCode").toString();

                Map<String, Object> temp = new HashMap<>();
                temp.put("MonitorTime", map.get("MonitorTime"));
                temp.put("AvgStrength", map.get("AvgStrength"));
                temp.put("IsException", false);//异常
                temp.put("IsOver", false);//超域
                temp.put("IsOverStandard", false);//超标
                temp.put("iswarn", false);//预警
                //超标
                long isover = data.stream().filter(m -> m.get("IsOver") != null && Integer.valueOf(m.get("IsOver").toString()) > 0).count();
                //预警
                long iswarn = data.stream().filter(m -> m.get("IsOver") != null && Integer.valueOf(m.get("IsOver").toString()) == 0).count();

                long isexception = data.stream().filter(m -> m.get("IsException") != null && Integer.valueOf(m.get("IsException").toString()) >= 1).count();
                //超标
                long IsOverStandard = data.stream().filter(m -> m.get("IsOverStandard") != null && (Boolean) m.get("IsOverStandard")).count();

                Optional<Map<String, Object>> first = monitorpoint.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst();

                if (first.isPresent()) {
                    temp.put("monitorpointname", first.get().get("MonitorPointName"));
                }

                if (isexception > 0) {
                    temp.put("IsException", true);//异常
                }
                if (IsOverStandard > 0) {
                    temp.put("IsOverStandard", true);//超标
                }
                if (isover > 0) {
                    temp.put("IsOver", true);//超标
                }
                if (iswarn > 0) {
                    temp.put("iswarn", true);//预警
                }
                resultList.add(temp);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("IsOver") != null).sorted(Comparator.comparing(m -> m.get("IsOver").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/30 0030 下午 3:30
     * @Description: 通过排口类型获取近一个月排污总量排名和同比排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "getLastMonthAndYearOnYeraFlowSummaryRankByType", method = RequestMethod.POST)
    public Object getLastMonthAndYearOnYeraFlowSummaryRankByType(@RequestJson(value = "type") Integer type,
                                                                 @RequestJson(value = "pollutantcode") String pollutantcode) {

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat format1 = new DecimalFormat("0.##");
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            FlowDataVO flowDataVOTwo = new FlowDataVO();
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("flag", "water");
            paramMap.put("type", type);

            if (type == WasteWaterEnum.getCode()) {
                data = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
            } else if (type == WasteGasEnum.getCode() || type == SmokeEnum.getCode()) {
                data = gasOutPutInfoService.getAllOutPutInfo(paramMap);
            }

            String dgimns = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            //设置查询条件查询mongo数据
            Date now = new Date();
            String endtime = format.format(now);
            Calendar instance = Calendar.getInstance();
            instance.setTime(now);
            instance.add(Calendar.MONTH, -1);
            Date time = instance.getTime();
            String starttime = format.format(time);

            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);
            List<FlowDataVO> flowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            Calendar instancetwo = Calendar.getInstance();
            instancetwo.setTime(now);
            instancetwo.add(Calendar.YEAR, -1);
            Date entdatetwo = instancetwo.getTime();
            String endtimetwo = format.format(entdatetwo);
            instancetwo.add(Calendar.MONTH, -1);
            Date timetwo = instancetwo.getTime();
            String starttimetwo = format.format(timetwo);
            paramMap.put("starttime", starttimetwo);
            paramMap.put("endtime", endtimetwo);
            flowDataVOTwo.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVOTwo.setDataGatherCode(dgimns);
            List<FlowDataVO> flowDataTwo = mongoBaseService.getListByParam(flowDataVOTwo, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            Map<String, List<Map<String, Object>>> collect = data.stream().filter(m -> m.get("PollutionName") != null).collect(Collectors.groupingBy(m -> m.get("PollutionName").toString()));

            //组装返回结果数据
            for (String pollutionname : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(pollutionname);
                List<String> dgimnlist = collect.get(pollutionname).stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
                Map<String, Object> result = new HashMap<>();
                final Float[] flow = {0f};
                final Float[] flow2 = {0f};
                for (FlowDataVO flowDatum : flowData) {
                    String mn = flowDatum.getDataGatherCode();
                    if (dgimnlist.contains(mn)) {
                        final boolean[] flag = {false};
                        List<Map<String, Object>> dayFlowDataList = flowDatum.getDayFlowDataList();
                        flow[0] = dayFlowDataList.stream().filter(map -> map.get("PollutantCode") != null && map.get("CorrectedFlow") != null && pollutantcode.equals(map.get("PollutantCode").toString())).peek(m -> {
                            flag[0] = true;
                        }).map(map -> Float.valueOf(map.get("CorrectedFlow").toString())).reduce(flow[0], Float::sum);
                        if (flag[0]) {
                            result.put("pollutionname", pollutionname);
                            if (list.size() > 0) {
                                result.put("pollutionid", list.get(0).get("PK_PollutionID"));
                            }
                            result.put("flow", format1.format(flow[0]));
                            result.put("lastyearflow", 0);
                        }
                    }
                }
                for (FlowDataVO flowDatum : flowDataTwo) {
                    String mn = flowDatum.getDataGatherCode();
                    if (dgimnlist.contains(mn)) {
                        final boolean[] flag2 = {false};
                        List<Map<String, Object>> dayFlowDataList = flowDatum.getDayFlowDataList();
                        flow2[0] = dayFlowDataList.stream().filter(map -> map.get("PollutantCode") != null && map.get("CorrectedFlow") != null && pollutantcode.equals(map.get("PollutantCode").toString())).peek(m -> {
                            flag2[0] = true;
                        }).map(map -> Float.valueOf(Float.valueOf(map.get("CorrectedFlow").toString()))).reduce(flow2[0], Float::sum);
                        if (flag2[0]) {
                            if (result.get("flow") != null) {
                                result.put("lastyearflow", format1.format(flow2[0]));
                            }
                        }
                    }
                }
                if (result.size() > 0) {
                    resultList.add(result);
                }

            }
            List<Map<String, Object>> collect1 = resultList.stream().filter(m -> m.get("flow") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect1);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2020/5/14 0014 上午 11:48
     * @Description: 通过监测时间, 监测点类型，污染物code查询园区排放量和同比数据（不传时间默认为当前时间）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [time]
     * @throws:
     */
    @RequestMapping(value = "getParkFlowInfoAndYearOnYeraInfoByParams", method = RequestMethod.POST)
    public Object getParkFlowInfoAndYearOnYeraInfoByParams(@RequestJson(value = "type") Integer type, @RequestJson(value = "pollutantcode") String pollutantcode
            , @RequestJson(value = "monitortime") String monitortime
            , @RequestJson(value = "defaultstr", required = false) String defaultstr
            , @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid) throws Exception {

        try {
            DecimalFormat format1 = new DecimalFormat("0.##");
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            FlowDataVO flowDataVOTwo = new FlowDataVO();
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("flag", "water");
            paramMap.put("type", type);
            paramMap.put("fkpollutionid", fkpollutionid);

            if (type == WasteWaterEnum.getCode()) {
                data = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
            } else if (type == WasteGasEnum.getCode() || type == SmokeEnum.getCode()) {
                data = gasOutPutInfoService.getAllOutPutInfo(paramMap);
            }

            String dgimns = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));


            monitortime = JSONObjectUtil.getTimeBetween(monitortime)[1];
            String endtime = monitortime;

            Calendar instance = Calendar.getInstance();
            instance.setTime(DataFormatUtil.getDateYMDHMS(endtime));
            instance.add(Calendar.MONTH, -1);
            String starttime = DataFormatUtil.getDateYMDHMS(instance.getTime());
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);
            List<FlowDataVO> flowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            instance.setTime(DataFormatUtil.getDateYMDHMS(endtime));
            instance.add(Calendar.YEAR, -1);
            String endtimetwo = DataFormatUtil.getDateYMDHMS(instance.getTime());
            instance.add(Calendar.MONTH, -1);
            String starttimetwo = DataFormatUtil.getDateYMDHMS(instance.getTime());

            paramMap.put("starttime", starttimetwo);
            paramMap.put("endtime", endtimetwo);
            flowDataVOTwo.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVOTwo.setDataGatherCode(dgimns);
            List<FlowDataVO> flowDataTwo = mongoBaseService.getListByParam(flowDataVOTwo, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            Map<String, List<Map<String, Object>>> collect = data.stream().filter(m -> m.get("PollutionName") != null).collect(Collectors.groupingBy(m -> m.get("PollutionName").toString()));

            //组装返回结果数据
            for (String pollutionname : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(pollutionname);
                List<String> dgimnlist = collect.get(pollutionname).stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
                Map<String, Object> result = new HashMap<>();
                final Float[] flow = {0f};
                final Float[] flow2 = {0f};
                for (FlowDataVO flowDatum : flowData) {
                    String mn = flowDatum.getDataGatherCode();
                    if (dgimnlist.contains(mn)) {
                        final boolean[] flag = {false};
                        List<Map<String, Object>> dayFlowDataList = flowDatum.getDayFlowDataList();
                        flow[0] = dayFlowDataList.stream().filter(map -> map.get("PollutantCode") != null && map.get("CorrectedFlow") != null && pollutantcode.equals(map.get("PollutantCode").toString())).peek(m -> {
                            flag[0] = true;
                        }).map(map -> Float.valueOf(Float.valueOf(map.get("CorrectedFlow").toString()))).reduce(flow[0], Float::sum);
                        if (flag[0]) {
                            result.put("pollutionname", pollutionname);
                            if (list.size() > 0) {
                                result.put("pollutionid", list.get(0).get("PK_PollutionID"));
                            }
                            result.put("flow", format1.format(flow[0]));
                            result.put("lastflow", 0);
                            result.put("yearonyear", defaultstr == null ? "↑0%" : defaultstr);
                        }
                    }
                }
                for (FlowDataVO flowDatum : flowDataTwo) {
                    String mn = flowDatum.getDataGatherCode();
                    if (dgimnlist.contains(mn)) {
                        final boolean[] flag2 = {false};
                        List<Map<String, Object>> dayFlowDataList = flowDatum.getDayFlowDataList();
                        flow2[0] = dayFlowDataList.stream().filter(map -> map.get("PollutantCode") != null && map.get("CorrectedFlow") != null && pollutantcode.equals(map.get("PollutantCode").toString())).peek(m -> {
                            flag2[0] = true;
                        }).map(map -> Float.valueOf(Float.valueOf(map.get("CorrectedFlow").toString()))).reduce(flow2[0], Float::sum);
                        if (flag2[0]) {
                            if (result.get("flow") != null) {
                                result.put("lastflow", format1.format(flow2[0]));
                                result.put("yearonyear", OnlineGasController.getYearOnYear(Float.valueOf(format1.format(flow2[0])), Float.valueOf(result.get("flow").toString()), defaultstr == null ? "↑0%" : defaultstr));
                            }
                        }
                    }
                }
                if (result.size() > 0) {
                    resultList.add(result);
                }

            }
            List<Map<String, Object>> collect1 = resultList.stream().filter(m -> m.get("flow") != null).sorted(Comparator.comparing(m -> Float.valueOf(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect1);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2020/6/11 0011 上午 9:21
     * @Description: 获取污染源本年各月份的排放量及同比趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type, pollutantcode, monitortime, fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getParkFlowInfoByParams", method = RequestMethod.POST)
    public Object getParkFlowInfoByParams(@RequestJson(value = "type") Integer type, @RequestJson(value = "pollutantcode") String pollutantcode
            , @RequestJson(value = "monitortime") String monitortime
            , @RequestJson(value = "fkpollutionid") String fkpollutionid) throws Exception {

        try {
            DecimalFormat format = new DecimalFormat("0.#");
            List<Map<String, Object>> data = new ArrayList<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            FlowDataVO flowDataVOTwo = new FlowDataVO();
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("flag", "water");
            paramMap.put("type", type);
            paramMap.put("fkpollutionid", fkpollutionid);

            if (type == WasteWaterEnum.getCode()) {
                data = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
            } else if (type == WasteGasEnum.getCode() || type == SmokeEnum.getCode()) {
                data = gasOutPutInfoService.getAllOutPutInfo(paramMap);
            }

            String dgimns = data.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            String starttime = monitortime.substring(0, 4) + "-00-01 00:00:00";
            String endtime = JSONObjectUtil.getTimeBetween(monitortime)[1];
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<String> monthBetween = DataFormatUtil.getMonthBetween(starttime.substring(0, 7), endtime.substring(0, 7));
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);
            List<FlowDataVO> flowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            Calendar instance = Calendar.getInstance();
            instance.setTime(DataFormatUtil.getDateYMDHMS(starttime));
            instance.add(Calendar.YEAR, -1);
            String starttimetwo = DataFormatUtil.getDateYMDHMS(instance.getTime());
            instance.setTime(DataFormatUtil.getDateYMDHMS(endtime));
            instance.add(Calendar.YEAR, -1);
            String endtimetwo = DataFormatUtil.getDateYMDHMS(instance.getTime());

            paramMap.put("starttime", starttimetwo);
            paramMap.put("endtime", endtimetwo);
            flowDataVOTwo.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVOTwo.setDataGatherCode(dgimns);
            List<FlowDataVO> flowDataTwo = mongoBaseService.getListByParam(flowDataVOTwo, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            for (String monitortimestr : monthBetween) {
                Map<String, Object> result = new HashMap<>();
                Object collect = flowData.stream().filter(m -> {
                    try {
                        return m.getMonitorTime().length() > 7 && monitortimestr.equals(DataFormatUtil.getDateYM(DataFormatUtil.zoneToLocalTime(m.getMonitorTime())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).flatMap(m -> m.getDayFlowDataList().stream()).filter(m -> ((Map) m).get("CorrectedFlow") != null && ((Map) m).get("PollutantCode") != null &&
                        pollutantcode.equals(((Map) m).get("PollutantCode").toString())).map(m -> ((Map) m).get("CorrectedFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                Object collect1 = flowDataTwo.stream().filter(m -> {
                    try {
                        return m.getMonitorTime().length() > 7 && monitortimestr.equals(DataFormatUtil.getDateYM(DataFormatUtil.zoneToLocalTime(m.getMonitorTime())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).flatMap(m -> m.getDayFlowDataList().stream()).filter(m -> ((Map) m).get("CorrectedFlow") != null && ((Map) m).get("PollutantCode") != null &&
                        pollutantcode.equals(((Map) m).get("PollutantCode").toString())).map(m -> ((Map) m).get("CorrectedFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                result.put("flow", format.format(collect));
                result.put("lastflow", format.format(collect1));
                result.put("yearonyear", OnlineGasController.getYearOnYear(Float.valueOf(collect1.toString()), Float.valueOf(collect.toString()), 0));
                result.put("monitortime", monitortimestr);
//                ((Map) m).get("CorrectedFlow").toString()
                resultList.add(result);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList);


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2020/5/19 0019 下午 12:02
     * @Description: 通过多参数统计风向下污染物监测值范围次数(单个图表)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, pollutantcode, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countOneWindPollutantValueData", method = RequestMethod.POST)
    public Object countOneWindPollutantValueData(@RequestJson(value = "dgimn") String dgimn,
                                                 @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                 @RequestJson(value = "pollutantcode") String pollutantcode,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = Arrays.asList(dgimn);
            List<Integer> integers = Arrays.asList(EnvironmentalStinkEnum.getCode(), EnvironmentalVocEnum.getCode());
            paramMap.put("dgimns", mns);
            //通过MN号获取对应空气站MN号
            List<Map<String, Object>> airlist = airMonitorStationService.getAirStationsByMN(paramMap);
            List<String> dgimns = airlist.stream().filter(m -> m.get("airmn") != null).map(m -> m.get("airmn").toString()).distinct().collect(Collectors.toList());
            dgimns.addAll(mns);

            paramMap.put("fkpollutantcode", pollutantcode);
//            List<Map<String, Object>> pollutantValueScopeByParamMap = pollutantValueScopeService.getPollutantValueScopeByParamMap(paramMap);

            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> pollutantValueScopeByParamMap = navigationStandardService.getStandardColorDataByParamMap(paramMap);


            if (dgimns.size() > 0) {
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59:59";
                    paramMap.put("endtime", endtime);
                }

                paramMap.put("mns", dgimns);
                String collection = MongoDataUtils.getCollectionByDataMark(3);
                paramMap.put("collection", collection);
                List<Document> airdata = onlineService.getMonitorDataByParamMap(paramMap);
                List<Document> monitordata = new ArrayList<>();

                Map<String, Object> assembledata = getAssembledata(integers, monitorpointtype, airdata, monitordata, dgimn, collection, pollutantcode, pollutantValueScopeByParamMap);
                return AuthUtil.parseJsonKeyToLower("success", assembledata);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 上午 11:16
     * @Description: 通过多参数统计风向下污染物监测值范围次数(多个图表)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, datetype, monitorpointtype, pollutantcode, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countManyWindPollutantValueData", method = RequestMethod.POST)
    public Object countManyWindPollutantValueData(@RequestJson(value = "dgimn") String dgimn,
                                                  @RequestJson(value = "datetype") String datetype,
                                                  @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                  @RequestJson(value = "pollutantcode") String pollutantcode,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<String> mns = Arrays.asList(dgimn);
            String pattern = "yyyy-MM-dd";
            List<Integer> integers = Arrays.asList(EnvironmentalStinkEnum.getCode(), EnvironmentalVocEnum.getCode());
            paramMap.put("dgimns", mns);
            //通过MN号获取对应空气站MN号
            List<Map<String, Object>> airlist = airMonitorStationService.getAirStationsByMN(paramMap);
            List<String> dgimns = airlist.stream().filter(m -> m.get("airmn") != null).map(m -> m.get("airmn").toString()).distinct().collect(Collectors.toList());
            dgimns.addAll(mns);
            paramMap.put("fkpollutantcode", pollutantcode);
//            List<Map<String, Object>> pollutantValueScopeByParamMap = pollutantValueScopeService.getPollutantValueScopeByParamMap(paramMap);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> pollutantValueScopeByParamMap = navigationStandardService.getStandardColorDataByParamMap(paramMap);

            if (dgimns.size() > 0) {
                if ("day".equals(datetype)) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + " 00:00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + " 23:59:59";
                        paramMap.put("endtime", endtime);
                    }
                } else if ("month".equals(datetype)) {
                    if (StringUtils.isNotBlank(starttime)) {
                        String yearMothFirst = DataFormatUtil.getYearMothFirst(starttime);
                        starttime = yearMothFirst + " 00:00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        String yearMothLast = DataFormatUtil.getYearMothLast(endtime);
                        endtime = yearMothLast + " 23:59:59";
                        paramMap.put("endtime", endtime);
                    }
                    pattern = "yyyy-MM";
                }
                paramMap.put("mns", dgimns);
                String collection = MongoDataUtils.getCollectionByDataMark(3);
                paramMap.put("collection", collection);
                List<Document> airdata = onlineService.getMonitorDataByParamMap(paramMap);
                List<Document> monitordata = new ArrayList<>();


                String finalPattern = pattern;
                Map<String, List<Document>> collect = airdata.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> {
                    return formatCSTString(m.get("MonitorTime").toString(), finalPattern);
                }));

                for (String monitortime : collect.keySet()) {
                    airdata = collect.get(monitortime);
                    Map<String, Object> assembledata = getAssembledata(integers, monitorpointtype, airdata, monitordata, dgimn, collection, pollutantcode, pollutantValueScopeByParamMap);
                    assembledata.put("monitortime", monitortime);
                    if (((List) assembledata.get("dataList")).size() > 0) {
                        resultList.add(assembledata);
                    }
                }
                return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList()));
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/2 0002 下午 6:28
     * @Description: 获取监测点每天报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, starttime, endtime, monitortype]
     * @throws:
     */
    @RequestMapping(value = "countMonitorAlarmByParams", method = RequestMethod.POST)
    public Object countMonitorAlarmByParams(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                            @RequestJson(value = "starttime") String starttime,
                                            @RequestJson(value = "endtime") String endtime,
                                            @RequestJson(value = "monitorpointids") Object monitorpointids) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fk_pollutionid", fk_pollutionid);
            paramMap.put("monitorpointids", monitorpointids);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
//            String code = OfflineStatusEnum.getCode();

            //在线排口mn号
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null/* && m.get("Status") != null && !code.equals(m.get("Status").toString())*/)
                    .map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            List<Integer> remindTypes = new ArrayList<>();

            remindTypes.add(ExceptionAlarmEnum.getCode());
            remindTypes.add(OverAlarmEnum.getCode());
            remindTypes.add(FlowChangeEnum.getCode());
            remindTypes.add(ConcentrationChangeEnum.getCode());
            remindTypes.add(EarlyAlarmEnum.getCode());

            //默认查询今天
            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", "day");
            paramMap.put("remindTypes", remindTypes);
            //统计点位报警和预警次数
            Map<String, Object> onlineData = onlineCountAlarmService.countAlarmsInfoDataByParamMap(paramMap);

            List<Map<String, Object>> onlinedatas = new ArrayList<>();
            onlinedatas.addAll(onlineData.get("earlywarn") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("earlywarn"));
            onlinedatas.addAll(onlineData.get("exception") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("exception"));
            onlinedatas.addAll(onlineData.get("overdata") == null ? new ArrayList<>() : (List<Map<String, Object>>) onlineData.get("overdata"));

            Map<String, List<Map<String, Object>>> collect2 = onlinedatas.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            List<Map<String, Object>> datas = new ArrayList<>();
            for (String DataGatherCode : collect2.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> list = new ArrayList<>();
                Map<String, Integer> collect = collect2.get(DataGatherCode).stream().filter(m -> m.get("MonitorTime") != null && m.get("count") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), Collectors.summingInt(n -> Integer.valueOf(n.get("count").toString()))));
                Map<String, Object> map = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && DataGatherCode.equals(m.get("DGIMN").toString())).findFirst().orElse(new HashMap<>());
                for (String monitortime : collect.keySet()) {
                    Integer count = collect.get(monitortime);
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("monitortime", monitortime);
                    resultmap.put("count", count);
                    list.add(resultmap);
                }
                data.put("monitorpointname", map.get("OutputName"));
                data.put("datalist", list.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList()));
                datas.add(data);
            }
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/2 0002 下午 6:28
     * @Description: 统计污染物报警情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countPollutantAlarmByParams", method = RequestMethod.POST)
    public Object countPollutantAlarmByParams(@RequestJson(value = "fk_pollutionid") String fk_pollutionid,
                                              @RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> integers = Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode(), FactoryBoundaryStinkEnum.getCode());
            paramMap.put("fkmonitorpointtypecodes", integers);
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Integer pollutanttype : integers) {
                paramMap.put("pollutanttype", pollutanttype);
                dataList.addAll(gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap));
            }

            //在线排口mn号
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("Status") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            List<Integer> remindTypes = new ArrayList<>();

            remindTypes.add(ExceptionAlarmEnum.getCode());
            remindTypes.add(OverAlarmEnum.getCode());
            remindTypes.add(ConcentrationChangeEnum.getCode());
            remindTypes.add(FlowChangeEnum.getCode());
            remindTypes.add(EarlyAlarmEnum.getCode());

            //默认查询今天
            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date today = new Date();
                endtime = DataFormatUtil.getDateYMD(today);
                starttime = DataFormatUtil.getDateYMD(today);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datetype", "day");
            paramMap.put("remindTypes", remindTypes);
            //统计点位报警和预警次数
            List<Map<String, Long>> maps = onlineCountAlarmService.countPollutantAlarmsInfoDataByParamMap(paramMap);
            Map<String, Long> data = new HashMap<>();
            for (Map<String, Long> map : maps) {
                for (String pollutantcode : map.keySet()) {
                    if (data.containsKey(pollutantcode)) {
                        data.put(pollutantcode, data.get(pollutantcode) + map.get(pollutantcode));
                    } else {
                        data.put(pollutantcode, map.get(pollutantcode));
                    }
                }
            }


            List<Map<String, Object>> resultList = new ArrayList<>();
            for (String pollutant : data.keySet()) {
                Map<String, Object> map = new HashMap<>();
                Map<String, Object> map1 = dataList.stream().filter(m -> m.get("pollutantcode") != null && pollutant.equals(m.get("pollutantcode").toString())).findFirst().orElse(new HashMap<>());
                map.put("pollutantcode", pollutant);
                map.put("pollutantname", map1.get("pollutantname"));
                map.put("count", data.get(pollutant));
                resultList.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 上午 10:55
     * @Description: 获取组装数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [integers, monitorpointtype, airdata, monitordata, dgimn, collection, pollutantcode, pollutantValueScopeByParamMap]
     * @throws:
     */
    private Map<String, Object> getAssembledata(List<Integer> integers, Integer monitorpointtype, List<Document> airdata, List<Document> monitordata, String dgimn,
                                                String collection, String pollutantcode, List<Map<String, Object>> pollutantValueScopeByParamMap) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        if (integers.contains(monitorpointtype)) {
            monitordata = airdata.stream().filter(m -> m.get("DataGatherCode") != null && dgimn.equals(m.get("DataGatherCode").toString())).peek(m -> {
                try {
                    formatCSTString(m, "yyyy-MM-dd HH");
                    m.put("flag", "monitor");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }).collect(Collectors.toList());
            List<Document> finalMonitordata = monitordata;
            if (monitordata.size() != airdata.size()) {
                airdata.removeIf(m -> finalMonitordata.contains(m));
                airdata.stream().forEach(m -> {
                    try {
                        formatCSTString(m, "yyyy-MM-dd HH");
                        m.put("flag", "air");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                airdata = copyList(airdata).stream().peek(m -> {
                    m.put("flag", "air");
                }).collect(Collectors.toList());
            }

        }
        //厂界恶臭类型的dgimn即为气象的dgimn
        else {
            monitordata = copyList(airdata).stream().peek(m -> {
                try {
                    formatCSTString(m, "yyyy-MM-dd HH");
                    m.put("flag", "monitor");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }).collect(Collectors.toList());
            airdata.stream().forEach(m -> {
                try {
                    formatCSTString(m, "yyyy-MM-dd HH");
                    m.put("flag", "air");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        }
        List<Map<String, Object>> list = countWindPollutantValueData(airdata, monitordata, collection, pollutantcode);

        DecimalFormat decimalFormat = new DecimalFormat("0.####");
        //设置valuescope
        for (Map<String, Object> map : pollutantValueScopeByParamMap) {
            if(map.get("StandardMinValue")!=null && map.get("StandardMaxValue")!=null){
                Double StandardMinValue = Double.valueOf(map.get("StandardMinValue").toString());
                Double StandardMaxValue = Double.valueOf(map.get("StandardMaxValue").toString());
                String ColourValue = map.get("ColourValue").toString();
                list.stream().filter(m -> m.get("value") != null && StandardMinValue <= Double.valueOf(m.get("value").toString()) && StandardMaxValue >= Double.valueOf(m.get("value").toString())).forEach(m -> {
                    m.put("valuescope", decimalFormat.format(StandardMinValue)+"-"+decimalFormat.format(StandardMaxValue));
                    m.put("colourvalue", ColourValue);
                });
            }
        }

        Map<String, List<Map<String, Object>>> collect = list.stream().filter(m -> m.get("valuescope") != null).collect(Collectors.groupingBy(m -> m.get("valuescope").toString()));

        for (String valuescope : collect.keySet()) {
            List<Map<String, Object>> list1 = collect.get(valuescope);
            List<Map<String, Object>> windList = new ArrayList<>();
            Map<String, Object> data = new HashMap<>();
            Map<String, Long> collect1 = list1.stream().filter(m -> m.get("windDirectionName") != null && m.get("windDirectionCode") != null).
                    collect(Collectors.groupingBy(m -> m.get("windDirectionName").toString() + "-" + m.get("windDirectionCode").toString(), Collectors.counting()));
            for (String wind : collect1.keySet()) {
                String[] split = wind.split("-");
                if (split.length == 2) {
                    Map<String, Object> winddata = new HashMap<>();
                    String windDirectionName = split[0];
                    String windDirectionCode = split[1];
                    Long num = collect1.get(wind);
                    winddata.put("windDirectionName", windDirectionName);
                    winddata.put("windDirectionCode", windDirectionCode);
                    winddata.put("num", num);
                    windList.add(winddata);
                }
            }
            data.put("valuescope", valuescope);
            data.put("colourvalue", list1.stream().filter(m->m.get("colourvalue")!=null).findFirst().orElse(new HashMap<>()).get("colourvalue"));
            data.put("winddatalist", windList);
            dataList.add(data);
        }

        //获取主导风向
        String predominantWindDirection = MongoDataUtils.getPredominantWindDirection(airdata);
        if (dataList.size() > 0) {
            resultMap.put("predominantWindDirection", predominantWindDirection);
        } else {
            resultMap.put("predominantWindDirection", "-");
        }
        resultMap.put("dataList", dataList.stream().filter(m -> m.get("valuescope") != null).sorted(Comparator.comparing(m -> Double.valueOf(m.get("valuescope").toString().split("-")[0]))).collect(Collectors.toList()));
        return resultMap;
    }


    public static List<Map<String, Object>> countWindPollutantValueData(List<Document> documents, List<Document> monitordata, String collection, String pollutantcode) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        documents.addAll(monitordata);
        //风向
        String WindDirection = WindDirectionEnum.getCode();
        Map<String, List<Document>> collect = documents.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
        for (String monitortime : collect.keySet()) {
            List<Document> data = collect.get(monitortime);
            if (data.size() >= 2) {
                Optional<Document> airoptional = data.stream().filter(m -> m.get("flag") != null && "air".equals(m.get("flag").toString())).findFirst();
                Optional<Document> monitoroptional = data.stream().filter(m -> m.get("flag") != null && "monitor".equals(m.get("flag").toString())).findFirst();
                if (airoptional.isPresent() && monitoroptional.isPresent()) {
                    Map<String, Object> map = new HashMap<>();
                    Document airdata = airoptional.get();
                    Document monitorpointdata = monitoroptional.get();
                    List<Map<String, Object>> airlist = airdata.get("HourDataList") == null ? new ArrayList<Map<String, Object>>() : (List<Map<String, Object>>) airdata.get("HourDataList");
                    List<Map<String, Object>> monitorlist = monitorpointdata.get("HourDataList") == null ? new ArrayList<Map<String, Object>>() : (List<Map<String, Object>>) monitorpointdata.get("HourDataList");
                    Optional<Double> first = airlist.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && WindDirection.equals(m.get("PollutantCode").toString()))
                            .map(m -> Double.valueOf(m.get("AvgStrength").toString())).findFirst();
                    Optional<Double> second = monitorlist.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString()))
                            .map(m -> Double.valueOf(m.get("AvgStrength").toString())).findFirst();
                    if (first.isPresent() && second.isPresent()) {
                        Double aDouble = first.get();
                        Double bDouble = second.get();
                        //获取风向名称
                        String windDirectionName = DataFormatUtil.windDirectionSwitch(aDouble, "name");
                        String windDirectionCode = DataFormatUtil.windDirectionSwitch(aDouble, "code");
                        map.put("windDirectionName", windDirectionName);
                        map.put("windDirectionCode", windDirectionCode);
                        map.put("value", bDouble);
                        dataList.add(map);
                    }
                }
            }
        }
        return dataList;
    }



    public void getMeteoDominantWindDirection(){
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("fk_monitorpointtypecode",meteoEnum.getCode());
        List<Map<String, Object>> OtherPointInfoByParamMap = otherMonitorPointService.isTableDataHaveInfoByParamMap(paramMap);

        List<String> collect = OtherPointInfoByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
        paramMap.put("mns", collect);
        String collection = MongoDataUtils.getCollectionByDataMark(3);
        paramMap.put("collection", collection);
        List<Document> airdata = onlineService.getMonitorDataByParamMap(paramMap);

    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 上午 8:40
     * @Description: 复制list
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    private List<Document> copyList(List<Document> list) {
        List<Document> newlist = new ArrayList<>();
        for (Document document : list) {
            Document data = new Document();
            data.putAll(document);
            newlist.add(data);
        }
        return newlist;
    }

    /**
     * @author: xsm
     * @date: 2020/5/28 0028 下午 1:01
     * @Description: 通过自定义条件查询在线小时突变数据(浓度突变 ， 排放量突变)
     * @updateUser:xsm
     * @updateDate:2021/04/12 0012 下午2:36
     * @updateDescription:查询分钟突变数据
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getChangeOnlineDatasByParamMap", method = RequestMethod.POST)
    public Object getChangeOnlineDatasByParamMap(@RequestJson(value = "monitortype") Integer monitortype,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws ParseException {
        try {
            List<Map<String, Object>> listdata = new ArrayList<>();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitortype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("remindtype", 1);
            paramMap.put("collectiontype", 2);
            Object nd_onlineDatas = getOnlineDatasByParamMap(paramMap);
            JSONObject jsonObject = JSONObject.fromObject(nd_onlineDatas);
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object datalist = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(datalist);
            listdata.addAll(jsonArray);
            paramMap.put("remindtype", 2);
            paramMap.remove("collectiontype");
            Object pfl_onlineDatas = getOnlineDatasByParamMap(paramMap);
            JSONObject jsonObject2 = JSONObject.fromObject(pfl_onlineDatas);
            Object data2 = jsonObject2.get("data");
            JSONObject jsonObjecttwo = JSONObject.fromObject(data2);
            Object datalist2 = jsonObjecttwo.get("datalist");
            JSONArray jsonArray2 = JSONArray.fromObject(datalist2);
            listdata.addAll(jsonArray2);
            //处理分页数据
            if (pagenum != null && pagesize != null) {
                resultMap.put("total", listdata.size());
                listdata = getPageData(listdata, pagenum, pagesize);
                resultMap.put("datalist", listdata);
            } else {
                resultMap.put("datalist", listdata);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/5/28 0028 下午 1:01
     * @Description: 通过自定义条件查询在线小时突变数据(浓度突变 ， 排放量突变)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "exportChangeOnlineDatasByParamMap", method = RequestMethod.POST)
    public void exportChangeOnlineDatasByParamMap(@RequestJson(value = "monitortype") Integer monitortype,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  HttpServletRequest request, HttpServletResponse response
    ) throws ParseException, IOException {
        try {
            List<Map<String, Object>> listdata = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitortype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("remindtype", 1);
            Object nd_onlineDatas = getOnlineDatasByParamMap(paramMap);
            JSONObject jsonObject = JSONObject.fromObject(nd_onlineDatas);
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object datalist = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(datalist);
            listdata.addAll(jsonArray);
            paramMap.put("remindtype", 2);
            Object pfl_onlineDatas = getOnlineDatasByParamMap(paramMap);
            JSONObject jsonObject2 = JSONObject.fromObject(pfl_onlineDatas);
            Object data2 = jsonObject2.get("data");
            JSONObject jsonObjecttwo = JSONObject.fromObject(data2);
            Object datalist2 = jsonObjecttwo.get("datalist");
            JSONArray jsonArray2 = JSONArray.fromObject(datalist2);
            listdata.addAll(jsonArray2);
            //处理分页数据
            List<Map<String, Object>> collect = listdata.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            LinkedList<String> headers = new LinkedList<>();
            headers.add("日期");
            headers.add("突增时段");
            headers.add("突增幅度");
            headers.add("突变类型");
            LinkedList<String> headersField = new LinkedList<>();
            headersField.add("monitortime");
            headersField.add("allpoint");
            headersField.add("flowrate");
            headers.add("remindtypename");
            String excelname = "";
            String partname = "突变预警";
            switch (getCodeByInt(monitortype)) {
                case WasteWaterEnum:
                    excelname = "废水" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case WasteGasEnum:
                    excelname = "废气" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case SmokeEnum:
                    excelname = "烟气" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case RainEnum:
                    excelname = "雨水" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case unOrganizationWasteGasEnum:
                    excelname = "无组织废气" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case FactoryBoundarySmallStationEnum:
                    excelname = "厂界小型站" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case FactoryBoundaryStinkEnum:
                    excelname = "厂界恶臭" + partname;
                    headers.addFirst("排口名称");
                    headers.addFirst("企业名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("pollutionname");
                    break;
                case AirEnum:
                    excelname = "大气" + partname;
                    headers.addFirst("监测点名称");
                    headersField.addFirst("monitorname");
                    break;
                case EnvironmentalVocEnum:
                    excelname = "VOC" + partname;
                    headers.addFirst("监测点名称");
                    headersField.addFirst("monitorname");
                    break;
                case EnvironmentalStinkEnum:
                    excelname = "恶臭" + partname;
                    headers.addFirst("监测点名称");
                    headersField.addFirst("monitorname");
                    break;
                case WaterQualityEnum:
                    excelname = "水质" + partname;
                    headers.addFirst("监测点名称");
                    headersField.addFirst("monitorname");
                case StorageTankAreaEnum:
                    excelname = "储罐" + partname;
                    headers.addFirst("储罐编码");
                    headers.addFirst("区域名称");
                    headersField.addFirst("monitorname");
                    headersField.addFirst("storagetankareaname");
            }
            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", headers, headersField, collect, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
            ExcelUtil.downLoadExcel(excelname, response, request, bytesForWorkBook);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/5 0005 上午 9:33
     * @Description: 获取企业相关排放口流量排放信息（包含排放时间，排放次数等信息）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortype, starttime, endtime, pagesize, pagenum, session]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointFlowInfoByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointFlowInfoByParamMap(@RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime,
                                                    @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                    @RequestJson(value = "outputname", required = false) String outputname,
                                                    @RequestJson(value = "fkmonitorpointtypecodes", required = false) Object fkmonitorpointtypecodes,
                                                    @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                    @RequestJson(value = "pagenum", required = false) Integer pagenum) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
//            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
//            paramMap.put("userid", userid);
            fkmonitorpointtypecodes = fkmonitorpointtypecodes == null ? new ArrayList<>() : (List<String>) fkmonitorpointtypecodes;
            paramMap.put("fkmonitorpointtypecodes", fkmonitorpointtypecodes);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            if (((List) fkmonitorpointtypecodes).size() == 0) {
                paramMap.put("fkmonitorpointtypecodes", Arrays.asList(WasteWaterEnum.getCode(), WasteGasEnum.getCode(), SmokeEnum.getCode(), RainEnum.getCode()));
            }

            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            paramMap.clear();
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            paramMap.put("outPutInfos", outPutInfosByParamMap);

            List<Map<String, Object>> monitorPointFlowDataByParam = onlineService.getMonitorPointFlowDataByParam(paramMap);
            //排序
            Collator instance = Collator.getInstance(Locale.CHINESE);
            monitorPointFlowDataByParam = monitorPointFlowDataByParam.stream().filter(m -> m.get("pollutionname") != null && m.get("outputname") != null && m.get("monitortime") != null)
                    .sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("monitortime").toString()).
                            thenComparing((s1, s2) -> instance.compare(((Map<String, Object>) s1).get("pollutionname").toString(), ((Map<String, Object>) s2).get("pollutionname").toString())).
                            thenComparing((s1, s2) -> instance.compare(((Map<String, Object>) s1).get("outputname").toString(), ((Map<String, Object>) s2).get("outputname").toString()))).
                            collect(Collectors.toList());
            int total = monitorPointFlowDataByParam.size();
            if (pagesize != null && pagenum != null) {
                monitorPointFlowDataByParam = monitorPointFlowDataByParam.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", total);
            resultMap.put("datalist", monitorPointFlowDataByParam);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "/ExportMonitorPointFlowInfoByParamMap", method = RequestMethod.POST)
    public void ExportMonitorPointFlowInfoByParamMap(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                     @RequestJson(value = "outputname", required = false) String outputname,
                                                     @RequestJson(value = "fkmonitorpointtypecodes", required = false) Object fkmonitorpointtypecodes,
                                                     HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> monitorPointFlowInfoByParamMap = (Map<String, Object>) getMonitorPointFlowInfoByParamMap(starttime, endtime, pollutionname, outputname, fkmonitorpointtypecodes, Integer.MAX_VALUE, 1);
            Map<String, Object> data = (Map<String, Object>) monitorPointFlowInfoByParamMap.get("data") == null ? new HashMap<>() : (Map<String, Object>) monitorPointFlowInfoByParamMap.get("data");
            List<Map<String, Object>> list = data.get("datalist") == null ? new ArrayList<>() : (List<Map<String, Object>>) data.get("datalist");


            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("企业名称");
            headers.add("排口名称");
            headers.add("监测点类型");
            headers.add("日期");
            headers.add("排放次数");
            headers.add("排放时段");
            headersField.add("pollutionname");
            headersField.add("outputname");
            headersField.add("fkmonitorpointtypename");
            headersField.add("monitortime");
            headersField.add("num");
            headersField.add("timepoint");


            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, list, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("排放规律分析", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/18 0018 上午 10:45
     * @Description: 通过自定义条件获取传输有效率详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fk_pollutionid, monitorpointid, fkmonitorpointtypecode, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getEffectiveTransmissionDetailByParams", method = RequestMethod.POST)
    public Object getEffectiveTransmissionDetailByParams(@RequestJson(value = "fk_pollutionid", required = false) String fk_pollutionid,
                                                         @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                         @RequestJson(value = "starttime", required = false) String starttime,
                                                         @RequestJson(value = "endtime", required = false) String endtime,
                                                         @RequestJson(value = "fkmonitorpointtypecode", required = false) Integer fkmonitorpointtypecode,
                                                         @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                         @RequestJson(value = "pagenum", required = false) Integer pagenum) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            if(StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)){
                String now = LocalDate.now().toString();
                starttime=now;
                endtime=now;
            }
            List<Map<String, Object>> datalist = new ArrayList<>();
            List<Map<String, Object>> resutllist = new ArrayList<>();
            List<String> monitoridlist = new ArrayList<>();
            if (monitorpointid != null) {
                monitoridlist.add(monitorpointid);
            }

            if(monitorpointid!=null){
                paramMap.put("monitorpointids", Arrays.asList(monitorpointid));
            }
            paramMap.put("fkmonitorpointtypecode", fkmonitorpointtypecode);
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);


//            paramMap.put("fkmonitorpointtypecodes", Arrays.asList(fkmonitorpointtypecode));
            paramMap.put("fkpollutionid", fk_pollutionid);
            paramMap.put("monitorpointtype", fkmonitorpointtypecode+"");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> effectiveTransmissionInfoByParamMap = effectiveTransmissionService.getOutPutEffectiveTransmissionInfoByParamMap(paramMap);


            Map<String, Map<String,Object>> transmissionEffectiveMap = getTransmissionEffectiveMap(effectiveTransmissionInfoByParamMap);


            Map<String, String> pollutantNameMap = effectiveTransmissionInfoByParamMap.stream().filter(m -> m.get("FK_PollutantCode") != null && m.get("pollutantname") != null).collect(Collectors.toMap(m -> m.get("FK_PollutantCode").toString(), m -> m.get("pollutantname").toString(), (a, b) -> a));
            Map<String, String> outputnameMap = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("OutputName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("OutputName").toString(), (a, b) -> a));
//            Map<String, String> outputidMap = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("outputid") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("outputid").toString(),(a,b)->a));
            List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            paramMap.clear();
            paramMap.put("collection", "HourData");
            paramMap.put("mns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            for (Document document : monitorDataByParamMap) {
                String dgimn = document.get("DataGatherCode") == null ? "" : document.get("DataGatherCode").toString();
                String MonitorTime = document.get("MonitorTime") == null ? "" : formatCSTString(document.get("MonitorTime").toString(), "HH");
                List<Map<String, Object>> HourDataList = (List<Map<String, Object>>) document.get("HourDataList");
                for (Map<String, Object> stringObjectMap : HourDataList) {
                    stringObjectMap.put("dgimn", dgimn);
                    stringObjectMap.put("MonitorTime", MonitorTime);
                    datalist.add(stringObjectMap);
                }
            }

            Map<String, List<Map<String, Object>>> collect = datalist.stream().filter(m -> m.get("dgimn") != null && m.get("PollutantCode") != null && StringUtils.isNotBlank(m.get("PollutantCode").toString())).collect(Collectors.groupingBy(m -> m.get("dgimn").toString() + "_" + m.get("PollutantCode").toString()));
            Map<String, String> oneDayHours = getOneDayHours();


            Map<String, List<Map<String, Object>>> collect2 = effectiveTransmissionInfoByParamMap.stream().filter(m -> m.get("FK_PollutantCode") != null && m.get("DGIMN") != null).collect(Collectors.groupingBy(m -> m.get("DGIMN").toString() + "_" + m.get("FK_PollutantCode").toString()));

            for (String codeAndDgimn : collect2.keySet()) {
                String[] split = codeAndDgimn.split("_");
                Map<String, Object> data = new HashMap<>();
                String DGIMN = split[0];
                String pollutantcode = split[1];

                data.put("iseffectivetransmission",collect2.get(codeAndDgimn).get(0).get("isEffectiveTransmission"));
                if (!collect.keySet().contains(codeAndDgimn)) {
                    for (String timepoint : oneDayHours.keySet()) {
                        data.put(timepoint, "×");
                    }
                    data.put("outputname", outputnameMap.get(DGIMN));
                    data.put("dgimn", DGIMN);
                    data.put("pollutantname", pollutantNameMap.get(pollutantcode));
                    data.put("pollutantcode", pollutantcode);
                }

                for (String dgimnAndPollutantcode : collect.keySet()) {
                    if (codeAndDgimn.equals(dgimnAndPollutantcode)) {
                        Map<String, List<Map<String, Object>>> collect1 = collect.get(dgimnAndPollutantcode).stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
                        for (String timepoint : oneDayHours.keySet()) {
                            if (collect1.get(timepoint.replace("_", "")) != null) {
                                Map<String, Object> stringObjectMap = collect1.get(timepoint.replace("_", "")).stream().filter(m -> pollutantcode.equals(m.get("PollutantCode").toString())).findFirst().orElse(new HashMap<>());
                                //在timepoint时间点存在数据 该时间点为 √
                                if (!stringObjectMap.isEmpty()) {
                                    data.put(timepoint, "√");
                                }
                                //在timepoint时间点不存在数据 该时间点为 ×
                                else {
                                    data.put(timepoint, "×");
                                }
                            } else {
                                data.put(timepoint, "×");
                            }
                        }
                        data.put("outputname", outputnameMap.get(DGIMN));
                        data.put("dgimn", DGIMN);
                        data.put("pollutantname", pollutantNameMap.get(pollutantcode));
                        data.put("pollutantcode", pollutantcode);
                    }
                    data.put("transmissioneffectiverate", transmissionEffectiveMap.get(DGIMN).get("transmissioneffectiverate"));
                    data.put("effectiverate", transmissionEffectiveMap.get(DGIMN).get("effectiverate"));
                    data.put("transmissionrate", transmissionEffectiveMap.get(DGIMN).get("transmissionrate"));
                }
                if(data.get("pollutantname")!=null){
                    resutllist.add(data);
                }
            }


            int total = resutllist.size();
            if (pagenum != null && pagesize != null) {
                resutllist = resutllist.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total", total);
            resultMap.put("datalist", resutllist.stream().filter(m -> m.get("outputname") != null).sorted(Comparator.comparing(m -> m.get("outputname").toString())).collect(Collectors.toList()));
            resultMap.put("title", oneDayHours);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String,Map<String,Object>> getTransmissionEffectiveMap(List<Map<String, Object>> effectiveTransmissionInfoByParamMap) {
        Map<String,Map<String,Object>> transmissioneffectiveMap=new HashMap<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        Map<String, List<Map<String, Object>>> collect3 = effectiveTransmissionInfoByParamMap.stream().filter(m -> m.get("DGIMN") != null).collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
        for (String dgimn : collect3.keySet()) {
            Map<String,Object> data=new HashMap<>();
            List<Map<String, Object>> maps = collect3.get(dgimn);

            Double ShouldNumber = maps.stream().filter(m -> m.get("ShouldNumber") != null).map(m -> Double.valueOf(m.get("ShouldNumber").toString())).collect(Collectors.summingDouble(m -> m));
            Double ShouldEffectiveNumber = maps.stream().filter(m -> m.get("ShouldEffectiveNumber") != null).map(m -> Double.valueOf(m.get("ShouldEffectiveNumber").toString())).collect(Collectors.summingDouble(m -> m));
            Double TransmissionNumber = maps.stream().filter(m -> m.get("TransmissionNumber") != null).map(m -> Double.valueOf(m.get("TransmissionNumber").toString())).collect(Collectors.summingDouble(m -> m));
            Double EffectiveNumber = maps.stream().filter(m -> m.get("EffectiveNumber") != null).map(m -> Double.valueOf(m.get("EffectiveNumber").toString())).collect(Collectors.summingDouble(m -> m));
            //传输率
            double transmissionrate = ShouldNumber == 0d ? 0d : TransmissionNumber/ShouldNumber;
            //有效率
            double effectiverate = ShouldEffectiveNumber == 0d ? 0d : EffectiveNumber/ShouldEffectiveNumber;
            //传输有效率
            double transmissioneffectiverate = transmissionrate * effectiverate;

            data.put("transmissionrate",decimalFormat.format(transmissionrate*100)+"%");
            data.put("effectiverate",decimalFormat.format(effectiverate*100)+"%");
            data.put("transmissioneffectiverate",decimalFormat.format(transmissioneffectiverate*100)+"%");

            transmissioneffectiveMap.put(dgimn,data);
        }
        return transmissioneffectiveMap;
    }

    /**
     * @author: chengzq
     * @date: 2020/9/18 0018 上午 9:54
     * @Description: 获取一天时间点集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Map<String, String> getOneDayHours() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("_00", "0时");
        map.put("_01", "1时");
        map.put("_02", "2时");
        map.put("_03", "3时");
        map.put("_04", "4时");
        map.put("_05", "5时");
        map.put("_06", "6时");
        map.put("_07", "7时");
        map.put("_08", "8时");
        map.put("_09", "9时");
        map.put("_10", "10时");
        map.put("_11", "11时");
        map.put("_12", "12时");
        map.put("_13", "13时");
        map.put("_14", "14时");
        map.put("_15", "15时");
        map.put("_16", "16时");
        map.put("_17", "17时");
        map.put("_18", "18时");
        map.put("_19", "19时");
        map.put("_20", "20时");
        map.put("_21", "21时");
        map.put("_22", "22时");
        map.put("_23", "23时");
        return map;
    }

    /**
     * @author: xsm
     * @date: 2020/10/10 0010 上午 10:52
     * @Description: 通过报警类型获取当前月污染源超标、超限、异常报警排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    @RequestMapping(value = "/getLastMonthEntMonitorPointAlarmDataByParam", method = RequestMethod.POST)
    public Object getLastMonthEntMonitorPointAlarmDataByParam(@RequestJson(value = "pollutionid") String pollutionid,
                                                              @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime,
                                                              @RequestJson(value = "reminds") List<Integer> reminds,
                                                              @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                              @RequestJson(value = "pagenum", required = false) Integer pagenum) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pollutionid", pollutionid);
            //该企业下所有监测点位
            data = pollutionService.getPollutionOutputMn(paramMap);
            Date startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
            //获取近一个月报警数据
            List<Map> maps = new ArrayList<>();
            List<Map> changelist = new ArrayList<>();
            List<Map> alarmlst = new ArrayList<>();
            if (data != null && data.size() > 0) {
                List<String> mns = new ArrayList<>();
                Set<String> pollutionids = new HashSet<>();
                Map<String, Object> mnandid = new HashMap<>();
                Map<String, Object> mnandname = new HashMap<>();
                for (Map<String, Object> map : data) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                        pollutionids.add(map.get("Pollutionid").toString());
                        mnandid.put(map.get("DGIMN").toString(), map.get("outputid"));
                        mnandname.put(map.get("DGIMN").toString(), map.get("outputname"));
                    }
                }
                String mnCommon;
                Set<String> pollutantNameList;
                String pollutantNames;
                List<String> tempList;
                List<Integer> monitorpointtypes = Arrays.asList(monitorpointtype);
                for (Integer remind : reminds) {
                    if (remind == ConcentrationChangeEnum.getCode() || remind == FlowChangeEnum.getCode()) {
                        changelist.addAll(onlineService.getOnlineConcentrationFlowChangeData(remind, mns, startDate, endDate, monitorpointtypes));
                    } else {
                        alarmlst.addAll(onlineCountAlarmService.getLastMonthEntMonitorPointAlarmDataByParam(remind, mns, startDate, endDate, monitorpointtype));
                    }
                }
                //突变
                for (Map<String, Object> dataMap : changelist) {
                    mnCommon = dataMap.get("DataGatherCode").toString();
                    Map<String, Object> objmap = new HashMap<>();
                    objmap.put("monitortime", dataMap.get("MonitorTime"));
                    objmap.put("remindcode", dataMap.get("remindcode"));
                    objmap.put("remindname", dataMap.get("remindname"));
                    objmap.put("overmultiple", dataMap.get("flowrate"));
                    objmap.put("pollutantnames", dataMap.get("pollutantStr"));
                    objmap.put("monitorpointname", mnandname.get(mnCommon));
                    objmap.put("monitorpointid", mnandid.get(mnCommon));
                    objmap.put("continuityvalue", dataMap.get("allpoint"));
                    maps.add(objmap);
                }
                //超标 异常 超阈值
                for (Map<String, Object> dataMap : alarmlst) {
                    Map<String, Object> objmap = new HashMap<>();
                    mnCommon = dataMap.get("datagathercode").toString();
                    String remindcode = dataMap.get("remindcode").toString();
                    if (remindcode.equals(ExceptionAlarmEnum.getCode().toString())) {    //异常
                        pollutantNameList = getPollutantNameAndExceptionTypeByCodes(dataMap.get("pollutantCodes"), (List<Map<String, Object>>) dataMap.get("exceptiontypes"), Arrays.asList(monitorpointtype));
                    } else { //超标
                        pollutantNameList = getPollutantNameByCodes(dataMap.get("pollutantCodes"), Arrays.asList(monitorpointtype));
                    }
                    tempList = new ArrayList<>(pollutantNameList);
                    pollutantNames = DataFormatUtil.FormatListToString(tempList, "、");
                    objmap.put("monitortime", dataMap.get("monitortime"));
                    objmap.put("remindcode", dataMap.get("remindcode"));
                    objmap.put("remindname", dataMap.get("remindname"));
                    objmap.put("pollutantnames", pollutantNames);
                    objmap.put("monitorpointname", mnandname.get(mnCommon));
                    objmap.put("monitorpointid", mnandid.get(mnCommon));
                    objmap.put("continuityvalue", dataMap.get("continuityvalue"));
                    if (dataMap.get("minvalue") != null && dataMap.get("maxvalue") != null) {
                        String minvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(dataMap.get("minvalue").toString()) * 100));
                        String maxvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.parseDouble(dataMap.get("maxvalue").toString()) * 100));
                        if (!minvalue.equals(maxvalue)) {//最大超标倍数和最小超标倍数不相等
                            objmap.put("overmultiple", minvalue + "%-" + maxvalue + "%");
                        } else {//相等
                            objmap.put("overmultiple", minvalue + "%");
                        }
                    } else {
                        objmap.put("overmultiple", "");
                    }
                    maps.add(objmap);
                }
            }
            //排序 监测点类型 污染源名称 监测点名称 升序
            if (maps.size() > 0) {
                List<Map> collect = maps.stream().sorted(Comparator.comparing((Map m) -> m.get("monitortime").toString()).reversed().thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
                List<Map> subDataList;
                if (pagenum != null && pagesize != null) {
                    subDataList = collect.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                } else {
                    subDataList = collect;
                }
                resultMap.put("datalist", subDataList);
                resultMap.put("total", collect.size());
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            } else {//无数据
                resultMap.put("datalist", new ArrayList<>());
                resultMap.put("total", 0);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/10/10 0010 上午 9:38
     * @Description: 统计某个企业一段时间内企业关联点位监测预警、超标、异常数据（每个点位一天一条）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutionMonitorPointEarlyAndOverData", method = RequestMethod.POST)
    public Object countPollutionMonitorPointEarlyAndOverData(@RequestJson(value = "pollutionid") String pollutionid,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "reminds") List<Integer> reminds) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            paramMap.put("pollutionid", pollutionid);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            //根据配置获取安全相关报警数据
           //Map<String, List<Map<String, Object>>> collect = new HashMap<>();
            boolean issecurity = false;
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            long countall =0;
            if (categorys!=null&&categorys.size()>0){
                if (categorys.contains("1")){//环保
                    //该企业下所有监测点位
                    data = pollutionService.getPollutionOutputMn(paramMap);
                }
                if(categorys.contains("2")){//安全
                    data.addAll(pollutionService.getPollutionSecurityPointMn(paramMap));
                    issecurity = true;
                    //根据配置获取视频监控报警
                    paramMap.put("starttime", starttime);
                    paramMap.put("endtime", endtime);
                    paramMap.put("pollutionid", pollutionid);
                    countall = videoCameraService.countVideoListDataNumByParamMap(paramMap);//总条数
                }
            }else{//若未配置 默认查环保
                //该企业下所有监测点位
                data = pollutionService.getPollutionOutputMn(paramMap);
            }
            Date startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
            List<String> dgimns = new ArrayList<>();
            Map<String, Integer> mnAndType = new HashMap<>();
            for (Map<String, Object> map : data) {
                if (map.get("DGIMN") != null) {
                    dgimns.add(map.get("DGIMN").toString());
                    if (map.get("type") != null) {
                        mnAndType.put(map.get("DGIMN").toString(), Integer.parseInt(map.get("type").toString()));
                    }
                }
            }
            Map<String, Integer> mapData = onlineService.countEntPointEarlyAndOverDataByParamMap(startDate, endDate, dgimns, reminds, mnAndType);
            if(issecurity){//包含安全 判断查询的报警类型是否是超标报警 是则将视频报警添加进去
            if (reminds.contains(OverAlarmEnum.getCode())){
                mapData.put("videoalarmnum",Integer.parseInt(countall+""));
                mapData.put("totalnum",mapData.get("totalnum")+Integer.parseInt(countall+""));
            }
            }
            return AuthUtil.parseJsonKeyToLower("success", mapData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/1/19 0019 下午 6:01
     * @Description: 通过多参数获取废水、进水口、出水口排放量监测图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [timetype1小时，2日，3月, starttime, endtime, datatype（1表示图表数据，2表示列表数据）, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getWaterFlowInfoByParams", method = RequestMethod.POST)
    public Object getWaterFlowInfoByParams(@RequestJson(value = "timetype") Integer timetype,
                                           @RequestJson(value = "starttime") String starttime,
                                           @RequestJson(value = "endtime") String endtime,
                                           @RequestJson(value = "pollutantcode",required = false) String pollutantcode) {
        //现根据监测点类型获取mn号等相关信息
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("pollutantcode",pollutantcode);
            paramMap.put("endtime",endtime);
            paramMap.put("monitorpointtype",1);
            paramMap.put("timetype",timetype);
            paramMap.put("datatype",1);
            List<Map<String, Object>> waterFlowInfoByParamsGroupbyTime = onlineService.getWaterFlowInfoByParamsGroupbyTime(paramMap);
            return AuthUtil.parseJsonKeyToLower("success",waterFlowInfoByParamsGroupbyTime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "getFlowInfoByParams", method = RequestMethod.POST)
    public Object getFlowInfoByParams(@RequestJson(value = "timetype") Integer timetype,
                                           @RequestJson(value = "starttime") String starttime,
                                           @RequestJson(value = "monitorpointtype") String monitorpointtype,
                                           @RequestJson(value = "pollutionid",required = false) String pollutionid,
                                           @RequestJson(value = "endtime") String endtime,
                                           @RequestJson(value = "pollutantcode",required = false) String pollutantcode) {
        //现根据监测点类型获取mn号等相关信息
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("pollutantcode",pollutantcode);
            paramMap.put("endtime",endtime);
            paramMap.put("monitorpointtype",monitorpointtype);
            paramMap.put("timetype",timetype);
            paramMap.put("pollutionid",pollutionid);
            paramMap.put("datatype",1);
            List<Map<String, Object>> waterFlowInfoByParamsGroupbyTime = onlineService.getFlowInfoByParamsGroupbyTime(paramMap);
            return AuthUtil.parseJsonKeyToLower("success",waterFlowInfoByParamsGroupbyTime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2021/1/20 0020 上午 11:01
     * @Description: 通过多参数获取废水、进水口、出水口排放量监测列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [timetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getWaterFlowListDataByParams", method = RequestMethod.POST)
    public Object getWaterFlowListDataByParams(@RequestJson(value = "timetype") Integer timetype,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "pagesize") Integer pagesize,
                                               @RequestJson(value = "pagenum") Integer pagenum) {
        //现根据监测点类型获取mn号等相关信息
        try {
            Map<String,Object> paramMap=new HashMap<>();
            Map<String,Object> resultMap=new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            paramMap.put("monitorpointtype",1);
            paramMap.put("timetype",timetype);
            paramMap.put("datatype",2);
            List<Map<String, Object>> waterFlowInfoByParamsGroupbyTime = onlineService.getWaterFlowInfoByParamsGroupbyTime(paramMap);
            List<Map<String, Object>> pollutants = pollutantService.getKeyPollutantsByMonitorPointType(1);
            List<Map<String, Object>> keyPollutants = pollutants.stream().filter(m -> m.get("IsShowFlow") != null && Integer.parseInt(m.get("IsShowFlow").toString()) == 1).collect(Collectors.toList());
            resultMap.put("total",waterFlowInfoByParamsGroupbyTime.size());
            List<Map<String, Object>> collect = waterFlowInfoByParamsGroupbyTime.stream().filter(m->m.get("monitortime")!=null).sorted(Comparator.comparing(m->m.get("monitortime").toString()))
                                                .skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            resultMap.put("datalist",collect);
            resultMap.put("title",keyPollutants);

            return AuthUtil.parseJsonKeyToLower("success",resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "ExportWaterFlowListDataByParams", method = RequestMethod.POST)
    public void ExportWaterFlowListDataByParams(@RequestJson(value = "timetype") Integer timetype,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                              HttpServletRequest request,HttpServletResponse response) {
        //现根据监测点类型获取mn号等相关信息
        try {
            Map<String,Object> paramMap=new HashMap<>();
            paramMap.put("starttime",starttime);
            paramMap.put("endtime",endtime);
            paramMap.put("monitorpointtype",1);
            paramMap.put("timetype",timetype);
            paramMap.put("datatype",2);
            List<Map<String, Object>> waterFlowInfoByParamsGroupbyTime = onlineService.getWaterFlowInfoByParamsGroupbyTime(paramMap);
            List<Map<String, Object>> pollutants = pollutantService.getKeyPollutantsByMonitorPointType(1);
            List<Map<String, Object>> keyPollutants = pollutants.stream().filter(m -> m.get("IsShowFlow") != null && Integer.parseInt(m.get("IsShowFlow").toString()) == 1).collect(Collectors.toList());



            List<Map<String,Object>> headers = new ArrayList<>();
            List<Map<String,Object>> headersField = new ArrayList<>();

            Map<String,Object> monitortimehead=new HashMap<>();
            monitortimehead.put("headername","监测时间");
            monitortimehead.put("headercode","monitortime");
            monitortimehead.put("columnnum",1);
            monitortimehead.put("rownum",2);
            headers.add(monitortimehead);

            for (Map<String, Object> keyPollutant : keyPollutants) {
                Map<String,Object> pollutantheaders=new HashMap<>();
                String Code = keyPollutant.get("Code") == null ? "" : keyPollutant.get("Code").toString();
                String Name = keyPollutant.get("Name") == null ? "" : keyPollutant.get("Name").toString();
                pollutantheaders.put("headername",Name);
                pollutantheaders.put("headercode",Code);
                pollutantheaders.put("columnnum",3);
                pollutantheaders.put("rownum",1);

                List<Map<String,Object>> childlist=new ArrayList<>();
                Map<String,Object> child1=new HashMap<>();
                child1.put("headername","企业总排放量");
                child1.put("headercode",Code+"water");
                child1.put("columnnum",1);
                child1.put("rownum",1);
                childlist.add(child1);

                Map<String,Object> child2=new HashMap<>();
                child2.put("headername","工业污水厂进口");
                child2.put("headercode",Code+"in");
                child2.put("columnnum",1);
                child2.put("rownum",1);
                childlist.add(child2);


                Map<String,Object> child3=new HashMap<>();
                child3.put("headername","工业污水厂出口");
                child3.put("headercode",Code+"out");
                child3.put("columnnum",1);
                child3.put("rownum",1);
                childlist.add(child3);
                pollutantheaders.put("chlidheader",childlist);

                headers.add(pollutantheaders);
            }



            for (Map<String, Object> stringObjectMap : waterFlowInfoByParamsGroupbyTime) {
                String monitortime = stringObjectMap.get("monitortime") == null ? "" : stringObjectMap.get("monitortime").toString();
                Map<String,Object> exportdata=new HashMap<>();
                exportdata.put("monitortime",monitortime);
                for (Map<String, Object> keyPollutant : keyPollutants) {
                    String Code = keyPollutant.get("Code") == null ? "" : keyPollutant.get("Code").toString();
                    //
                    List<Map<String, Object>> maps = stringObjectMap.get(Code) == null ? new ArrayList<>() : (List<Map<String, Object>>) stringObjectMap.get(Code);
                    for (Map<String, Object> map : maps) {
                        String flag = map.get("flag") == null ? "" : map.get("flag").toString();
                        String monitorvalue = map.get("monitorvalue") == null ? "" : map.get("monitorvalue").toString();
                        exportdata.put(Code+flag,monitorvalue);
                    }
                }
                headersField.add(exportdata);
            }
            HSSFWorkbook sheet1 = ExcelUtil.exportManyHeaderExcel("sheet1", headers, headersField, JSONObjectUtil.getFormat(starttime.length()));
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("园区废水排放分析", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:40
     * @Description: 条件查询废气浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getChangeWarnDetailParams(@RequestJson(value = "dgimn", required = false) String dgimn,
                                               @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "monitortype") Integer monitortype,
                                               @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                               @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            paramMap.put("collectiontype", collectiontype);
            paramMap.put("monitortype", monitortype);
            paramMap.put("remindtype", remindtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> abruptChangeInfoByParam = onlineService.getAbruptPollutantsDischargeInfoByParam(paramMap);
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum != null && pagesize != null) {
                List<Map<String, Object>> collect = abruptChangeInfoByParam.stream().filter(m->m.get("monitortime")!=null).sorted(Comparator.comparing(m->((Map<String,Object>)m).get("monitortime").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                resultMap.put("datalist", collect);
            } else {
                resultMap.put("datalist", abruptChangeInfoByParam);
            }
            resultMap.put("total", abruptChangeInfoByParam.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:34
     * @Description: 获取废气浓度突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getChangeWarnPollutantInfo", method = RequestMethod.POST)
    public Object getChangeWarnPollutantInfo(@RequestJson(value = "dgimn", required = false) String dgimn,
                                             @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "monitortype") Integer monitortype,
                                             @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("collectiontype", collectiontype);
            paramMap.put("monitortype", monitortype);
            paramMap.put("remindtype", remindtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> result = onlineService.getUpRushPollutantInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:38
     * @Description: 获取废水单个污染物浓度突增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOnePollutantChangeWarnByParams", method = RequestMethod.POST)
    public Object getOnePollutantChangeWarnByParams(@RequestJson(value = "dgimn") String dgimn,
                                                    @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                                    @RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime,
                                                    @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> result = onlineService.getPollutantUpRushDischargeInfo(starttime, endtime, remindtype, dgimn, pollutantcode,collectiontype);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/11 15:52
     * @Description: 获取废气排放量突增污染物信息
     * @param: dgimn, starttime, endtime
     * @return:
     */
    @RequestMapping(value = "getDischargeUpRushPollutantInfo", method = RequestMethod.POST)
    public Object getDischargeUpRushPollutantInfo(@RequestJson(value = "dgimn", required = false) String dgimn,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "monitortype") Integer monitortype,
                                                     @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitortype);
            paramMap.put("remindtype", remindtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> result = onlineService.getUpRushPollutantInfo(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/11 15:52
     * @Description: 获取单个废气突增污染物排放信息
     * @param: dgimn, starttime, endtime
     * @return:
     */
    @RequestMapping(value = "getPollutantUpRushDischargeInfo", method = RequestMethod.POST)
    public Object getPollutantUpRushDischargeInfo(@RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode
    ) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
            Map<String, Object> result = onlineService.getPollutantUpRushDischargeInfo(starttime, endtime, remindtype, dgimn, pollutantcode,collectiontype);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "getAbruptPollutantsByParam", method = RequestMethod.POST)
    public Object getAbruptPollutantsByParam(@RequestJson(value = "endtime") String endtime,
                                                @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                                @RequestJson(value = "starttime") String starttime,
                                                @RequestJson(value = "dgimn") String dgimn,
                                                @RequestJson(value = "monitortype") Integer monitortype,
                                                @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                @RequestJson(value = "pagenum", required = false) Integer pagenum) throws ParseException {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
            paramMap.put("collectiontype", collectiontype);
            paramMap.put("monitortype", monitortype);
            paramMap.put("remindtype", remindtype);
            paramMap.put("starttime", starttime);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("endtime", endtime);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> abruptChangeInfoByParam = onlineService.getAbruptPollutantsDischargeInfoByParam(paramMap);
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum != null && pagesize != null) {
                List<Map<String, Object>> collect = abruptChangeInfoByParam.stream().filter(m->m.get("monitortime")!=null).sorted(Comparator.comparing(m->((Map<String,Object>)m).get("monitortime").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                resultMap.put("datalist", collect);
            } else {
                resultMap.put("datalist", abruptChangeInfoByParam);
            }
            resultMap.put("total", abruptChangeInfoByParam.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/25 0025 上午 10:37
     * @Description: 通过自定义条件查询监测点浓度（预警、异常、超限）连续预警数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getEarlyOverOrExceptionListDataByParams", method = RequestMethod.POST)
    public Object getEarlyOverOrExceptionListDataByParams(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                          @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                             @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                             @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                             @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                             @RequestJson(value = "remindtype") Integer remindtype,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "exceptionlist", required = false) List<String> exceptionlist,
                                                             @RequestJson(value = "datatype", required = false) String datatype,
                                                             @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                             @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws ParseException {
        try {
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitortypes = Arrays.asList(
                    WasteWaterEnum.getCode(),
                    SmokeEnum.getCode(),
                    WasteGasEnum.getCode(),
                    RainEnum.getCode(),
                    unOrganizationWasteGasEnum.getCode(),
                    FactoryBoundarySmallStationEnum.getCode(),
                    FactoryBoundaryStinkEnum.getCode()
            );
            if (datatype == null) {
                datatype = "RealTime";
            }
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("userid", userId);
            paramMap.put("exceptionlist", exceptionlist);
            paramMap.put("remindtype", remindtype);
            List<Map<String, Object>> stoplist = new ArrayList<>();
            List<Map<String, Object>> rainoutputlist = new ArrayList<>();
            if (monitorpointtypes==null||monitorpointtypes.size()==0){
                monitorpointtypes = new ArrayList<>();
                monitorpointtypes.add(monitorpointtype);
            }
            for (Integer onetype:monitorpointtypes) {
                //获取所点位名称和MN号
                paramMap.put("monitortype", onetype);
                if (monitortypes.contains(onetype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                    if (onetype == WasteWaterEnum.getCode() || onetype == WasteGasEnum.getCode()
                            || onetype == SmokeEnum.getCode()
                            || onetype == RainEnum.getCode()) {//为废气、废水、雨水、烟气排口时
                            paramMap.put("monitorpointtype", onetype);
                            stoplist.addAll(stopProductionInfoService.getCurrentTimeStopProductionInfoByParamMap(paramMap));//停产排口
                        if (onetype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//排放中
                            paramMap.put("monitorpointtype", onetype);
                            rainoutputlist = monitorControlService.getCurrentTimeMonitorControlInfoByParamMap(paramMap);
                        }
                    }
                } else if (onetype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (onetype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else{
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            paramMap.put("monitortypes", monitorpointtypes);
            String mnCommon;
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            Map<String, Object> mnAndtype = new HashMap<>();
            Map<String, List<Map<String, Object>>> pointidAndrtsp = new HashMap<>();
            //获取相关视频信息
            List<Map<String, Object>> videos = videoCameraService.getVideoInfoByMonitorpointTypes(monitorpointtypes);
            if (videos != null && videos.size() > 0) {
                Set<String> idset = new HashSet<>();
                for (Map<String, Object> map : videos) {
                    if (map.get("monitorpointid") != null && !"".equals(map.get("monitorpointid").toString())) {
                        if (!idset.contains(map.get("monitorpointid").toString())) {
                            idset.add(map.get("monitorpointid").toString());
                            List<Map<String, Object>> rtsplist = new ArrayList<>();
                            for (Map<String, Object> map2 : videos) {
                                if (map2.get("monitorpointid") != null && (map.get("monitorpointid").toString()).equals((map2.get("monitorpointid").toString()))) {
                                    Map<String, Object> objmap = new HashMap<>();
                                    objmap.put("rtsp", map2.get("rtsp"));
                                    objmap.put("name", map2.get("name"));
                                    objmap.put("id", map2.get("pkid"));
                                    objmap.put("vediomanufactor", map2.get("VedioManufactor"));
                                    rtsplist.add(objmap);
                                }
                            }
                            pointidAndrtsp.put(map.get("monitorpointid").toString(), rtsplist);
                        }
                        //
                    }
                }
            }
            Integer fk_monitorpointtypecode = 0;
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    fk_monitorpointtypecode = map.get("fk_monitorpointtypecode")!=null?Integer.valueOf(map.get("fk_monitorpointtypecode").toString()):0;
                    mnAndtype.put(mnCommon, fk_monitorpointtypecode);
                    if (fk_monitorpointtypecode == WasteWaterEnum.getCode() || fk_monitorpointtypecode == WasteGasEnum.getCode() || fk_monitorpointtypecode == SmokeEnum.getCode() || fk_monitorpointtypecode == RainEnum.getCode()) {
                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                        boolean rainflag = false;
                        if (stoplist != null && stoplist.size() > 0) {
                            for (Map<String, Object> stopmap : stoplist) {
                                if ((map.get("pk_id").toString()).equals(stopmap.get("FK_Outputid").toString())) {
                                    rainflag = true;
                                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname") + "【停产】");
                                    break;
                                }
                            }
                        }
                        /*if (monitorpointtype == RainEnum.getCode() && rainflag == false) {//若为雨水类型 且未停产  判断是否为排放中
                            if (rainoutputlist != null && rainoutputlist.size() > 0) {
                                for (Map<String, Object> stopmap : rainoutputlist) {
                                    if ((map.get("pk_id").toString()).equals(stopmap.get("FK_MonitorPointId").toString())) {
                                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname") + "【排放中】");
                                        break;
                                    }
                                }
                            }
                        }*/
                    } else {
                        mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                    }
                    mnAndMonitorPointId.put(mnCommon, map.get("pk_id"));
                    mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
                }
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datatype",datatype);
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getEarlyOverOrExceptionListDataByParams(paramMap);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) onlineDataGroupMmAndMonitortime.get("datalist");
            for (Map<String, Object> dataMap : dataList) {
                mnCommon = dataMap.get("datagathercode").toString();
                dataMap.put("dgimn", mnCommon);
                dataMap.put("monitorpointtype", mnAndtype.get(mnCommon));
                dataMap.put("pollutionname", mnAndPollutionName.get(mnCommon));
                dataMap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                dataMap.put("pollutionid", mnAndPollutionId.get(mnCommon));
                dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                dataMap.put("rtsplist", pointidAndrtsp.get(mnAndMonitorPointId.get(mnCommon)));

            }
            onlineDataGroupMmAndMonitortime.remove("datalist");
            onlineDataGroupMmAndMonitortime.put("data", dataList);

            return AuthUtil.parseJsonKeyToLower("success", onlineDataGroupMmAndMonitortime);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/02 0002 下午 16:11
     * @Description:根据报警类型和监测类型获取相关报警详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getHierarchicalEarlyDetailDataByParams", method = RequestMethod.POST)
    public Object getHierarchicalEarlyDetailDataByParams(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                         @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                         @RequestJson(value = "remindtype") Integer remindtype,
                                                         @RequestJson(value = "exceptiontype", required = false) List<String> exceptiontype,
                                                         @RequestJson(value = "datatype", required = false) List<String> datatypes,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime,
                                                         @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                         @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                         @RequestJson(value = "dgimn", required = false) String dgimn,
                                                         @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                         @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantname", pollutantname);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("dgimn", dgimn);
            paramMap.put("remindtype", remindtype);
            if (exceptiontype!=null&&exceptiontype.size()>0) {
                paramMap.put("exceptiontype", exceptiontype);
            }
            if (datatypes == null) {
                datatypes = new ArrayList<>();
            }
            resultMap = onlineService.getHierarchicalEarlyDetailDataByParams(paramMap,datatypes,pagenum, pagesize);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/23 0023 下午 1:31
     * @Description: 通过自定义条件导出监测点（预警、异常、超限）浓度连续数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "exportEarlyOverOrExceptionListDataByParams", method = RequestMethod.POST)
    public void exportEarlyOverOrExceptionListDataByParams(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                           @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                           @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                           @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                           @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                           @RequestJson(value = "remindtype") Integer remindtype,
                                                           @RequestJson(value = "exceptionlist", required = false) List<String> exceptionlist,
                                                           @RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime,
                                                           @RequestJson(value = "datatype", required = false) String datatype,
                                                            HttpServletResponse response, HttpServletRequest request) throws ParseException, IOException {
        try {
            List<Map<String, Object>> monitorPoints = new ArrayList<>();
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Integer> monitortypes = Arrays.asList(WasteWaterEnum.getCode(), SmokeEnum.getCode(),
                    WasteGasEnum.getCode(), RainEnum.getCode(), unOrganizationWasteGasEnum.getCode(), FactoryBoundarySmallStationEnum.getCode(), FactoryBoundaryStinkEnum.getCode()
            );
            if (datatype == null) {
                datatype = "RealTime";
            }
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("remindtype", remindtype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("userid", userId);
            paramMap.put("exceptionlist", exceptionlist);
            paramMap.put("pollutionname", pollutionname);
            if (monitorpointtypes==null||monitorpointtypes.size()==0){
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype!=null) {
                    monitorpointtypes.add(monitorpointtype);
                }
            }
            //获取所点位名称和MN号
            for (Integer onetype:monitorpointtypes) {
                paramMap.put("monitortype", onetype);
                if (monitortypes.contains(onetype)) {
                    monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (onetype == AirEnum.getCode()) {//大气
                    monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (onetype == WaterQualityEnum.getCode()) {//水质
                    monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else{//voc//恶臭
                    monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            paramMap.put("monitortypes", monitorpointtypes);
            String mnCommon;
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            for (Map<String, Object> map : monitorPoints) {
                if (map.get("dgimn") != null) {
                    mnCommon = map.get("dgimn").toString();
                    dgimns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                }
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datatype",datatype);
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getEarlyOverOrExceptionListDataByParams(paramMap);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) onlineDataGroupMmAndMonitortime.get("datalist");
            List<Map<String, Object>> resultlist = new ArrayList<>();
            for (Map<String, Object> dataMap : dataList) {
                mnCommon = dataMap.get("datagathercode").toString();
                if (dataMap.get("pollutantlist")!=null) {
                    List<Map<String, Object>> poList = (List<Map<String, Object>>) dataMap.get("pollutantlist");
                    for (Map<String, Object> pomap:poList){
                        pomap.put("pollutionname", mnAndPollutionName.get(mnCommon));
                        pomap.put("monitorpointname", mnAndMonitorPointName.get(mnCommon));
                        pomap.put("monitortime", dataMap.get("monitortime"));
                        resultlist.add(pomap);
                    }
                }
            }
            //设置文件名称
            String fileName = "";
            if (monitorpointtypes.size()>1) {
                if (remindtype == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {  //阈值
                    fileName = exportExcelTableNameByMonitorPointTypeAndRemark( WasteGasEnum.getCode(), "early");
                } else if (remindtype == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {    //异常
                    fileName = exportExcelTableNameByMonitorPointTypeAndRemark( WasteGasEnum.getCode(), "exception");
                } else if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                    fileName = exportExcelTableNameByMonitorPointTypeAndRemark( WasteGasEnum.getCode(), "over");
                }
            }else {
                if (remindtype == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {  //阈值
                    fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtypes.get(0), "early");
                } else if (remindtype == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {    //异常
                    fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtypes.get(0), "exception");
                } else if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                    fileName = exportExcelTableNameByMonitorPointTypeAndRemark(monitorpointtypes.get(0), "over");
                }
            }
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForEarlyOverOrException(remindtype, monitorpointtypes.get(0), monitortypes);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");

            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultlist, "");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/02/08 0008 下午 2:49
     * @Description: 通过自定义条件查询在线分钟突变数据（废气烟气合并）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getGasOnlineDatasByParamMap", method = RequestMethod.POST)
    public Object getGasOnlineDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws ParseException {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            //提醒类型：1表示浓度突变，2表示排放量突变，3表示预警，4表示异常，5表示超限,6表示排放量许可

            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            JSONObject paramMap = JSONObject.fromObject(paramsjson);
            Integer remindtype = null;
            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            List<Integer> monitortypes = (List<Integer>) paramMap.get("monitortypes");
            paramMap.put("userid", userid);
            for (Integer type:monitortypes) {
                paramMap.put("monitortype",type);
                monitorInfo.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
            }
            //添加rtsp
            Map<String, Object> params = new HashMap<>();
            List<String> monitorpointids = monitorInfo.stream().filter(m -> m.get("pk_id") != null).map(m -> m.get("pk_id").toString()).collect(Collectors.toList());
            params.put("monitorpointids", monitorpointids);
            params.put("monitorpointtypes", monitortypes);

            List<Map<String, Object>> videoCameraInfoByParamMap = videoCameraService.getVideoCameraInfoByParamMap(params);
            for (Map<String, Object> map : monitorInfo) {
                List<Map<String, Object>> cameras = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : videoCameraInfoByParamMap) {
                    if (map.get("pk_id") != null && stringObjectMap.get("monitorpointid") != null && map.get("pk_id").toString().equals(stringObjectMap.get("monitorpointid").toString())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("rtsp", stringObjectMap.get("rtsp"));
                        data.put("id", stringObjectMap.get("PK_VedioCameraID"));
                        data.put("name", stringObjectMap.get("VedioCameraName"));
                        cameras.add(data);
                    }
                }
                map.put("rtsplist", cameras);
            }

            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());

            paramMap.put("dgimns", dgimns);
            paramMap.put("usercode", usercode);
            paramMap.put("pollutanttypes", monitortypes);
            if (paramMap.get("starttime") != null) {
                paramMap.put("starttime", paramMap.get("starttime").toString() + " 00:00:00");
            }
            if (paramMap.get("endtime") != null) {
                paramMap.put("endtime", paramMap.get("endtime").toString() + " 23:59:59");
            }
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineService.getOnlineDataGroupMmAndMonitortime(paramMap);

            List<Map<String, Object>> data = (List) onlineDataGroupMmAndMonitortime.get("data");
            if (paramMap.get("remindtype") != null) {
                remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            }
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> map = data.get(i);
                if (map.get("DataGatherCode") != null) {
                    String dataGatherCode = map.get("DataGatherCode").toString();
                    Optional<Map<String, Object>> first = monitorInfo.stream().filter(m -> m.get("dgimn") != null && m.get("dgimn").toString().equals(dataGatherCode)).findFirst();
                    if (first.isPresent()) {
                        Map<String, Object> map1 = first.get();
                        if (map1.get("pollutionname") != null) {
                            String pollutionname = map1.get("pollutionname") == null ? "" : map1.get("pollutionname").toString();
                            String shortername = map1.get("shortername") == null ? "" : map1.get("shortername").toString();
                            String storagetankareaname = map1.get("storagetankareaname") == null ? "" : map1.get("storagetankareaname").toString();
                            map.put("pollutionname", pollutionname);
                            map.put("pollutionid", map1.get("pk_pollutionid"));
                            if (map1.get("storagetankareaname") != null) {
                                map.put("storagetankareaname", shortername + "-" + storagetankareaname);
                            }
                        }
                        map.put("monitorname", map1.get("monitorpointname") + (map1.get("Status") == null ? "" : "0".equals(map1.get("Status").toString()) ? "【停产】" : ""));
                        map.put("dgimn", dataGatherCode);
                        map.put("remindtype", paramMap.get(remindtype));
                        if (paramMap.get(remindtype) != null && (Integer.parseInt(paramMap.get(remindtype).toString()) == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode())) {
                            map.put("remindtypename", "浓度突变");
                        } else if (paramMap.get(remindtype) != null && (Integer.parseInt(paramMap.get(remindtype).toString()) == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode())) {
                            map.put("remindtypename", "排放量突变");
                        }
                        map.put("outputid", map1.get("pk_id"));
                        map.put("RTSP", map1.get("RTSP"));
                        map.put("monitorpointtype",map1.get("fk_monitorpointtypecode")!=null?Integer.valueOf(map1.get("fk_monitorpointtypecode").toString()):null );
                        map.put("remindtype", remindtype);
                    }
                }
            }
            resultMap.put("total", onlineDataGroupMmAndMonitortime.get("total"));
            resultMap.put("datalist", data);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/08 0008 下午 4:05
     * @Description: 通过自定义条件导出在线分钟突变数据（废气、烟气合并）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, session]
     * @throws:
     */
    @RequestMapping(value = "exportGasOnlineChangeDatasByParamMap", method = RequestMethod.POST)
    public void exportGasOnlineChangeDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson,
                                                  HttpServletResponse response, HttpServletRequest request) throws ParseException, IOException {
        try {
            JSONObject paramMap = JSONObject.fromObject(paramsjson);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            List<Integer> monitortypes = (List<Integer>) paramMap.get("monitortypes");
            paramMap.put("userid", userid);
            for (Integer type:monitortypes) {
                paramMap.put("monitortype",type);
                monitorInfo.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
            }
            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("pollutanttypes", monitortypes);
            Map<String, Object> map = onlineService.countGasOnlineDataGroupMmAndMonitortime(paramMap);
            List<Map<String, Object>> allData = new ArrayList<>();
            if (map.get("total") != null && Integer.valueOf(map.get("total").toString()) > 0) {
                Float total = Float.valueOf(map.get("total").toString());
                for (int i = 1; i < Math.ceil(total / 20) + 1; i++) {
                    paramMap.put("pagenum", i);
                    paramMap.put("pagesize", 20);
                    Object onlineDatasByParamMap = getGasOnlineDatasByParamMap(paramMap);
                    JSONObject jsonObject = JSONObject.fromObject(onlineDatasByParamMap);
                    Object data = jsonObject.get("data");
                    JSONObject jsonObject1 = JSONObject.fromObject(data);
                    Object datalist = jsonObject1.get("datalist");
                    JSONArray jsonArray = JSONArray.fromObject(datalist);
                    allData.addAll(jsonArray);
                }
            }
            List<Map<String, Object>> collect = allData.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", (LinkedList) map.get("headers"), (LinkedList) map.get("headersfield"), collect, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
            ExcelUtil.downLoadExcel(map.get("excelname").toString(), response, request, bytesForWorkBook);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
