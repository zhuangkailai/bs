package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GroundWaterService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterStationService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineWaterQualityService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: zhangzc
 * @date: 2019/9/18 15:13
 * @Description: 水质质量控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RequestMapping("onlineWaterQuality")
@RestController
public class OnlineWaterQualityController {

    private final OnlineService onlineService;
    private final GroundWaterService groundWaterService;
    private final OnlineMonitorService onlineMonitorService;


    private final OnlineWaterQualityService onlineWaterQualityService;
    private final PollutantService pollutantService;


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    //日在线
    private final String dayCollection = "DayData";

    //日在线
    private final String GroundWaterData_db = "WaterDetectData";

    public OnlineWaterQualityController(OnlineService onlineService, GroundWaterService groundWaterService, OnlineMonitorService onlineMonitorService, OnlineWaterQualityService onlineWaterQualityService, PollutantService pollutantService) {
        this.onlineService = onlineService;
        this.groundWaterService = groundWaterService;
        this.onlineMonitorService = onlineMonitorService;
        this.onlineWaterQualityService = onlineWaterQualityService;
        this.pollutantService = pollutantService;
    }

    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;
    @Autowired
    private PubCodeService pubCodeService;
    @Autowired
    private WaterStationService waterStationService;


    private final int monitorPointTypeCode = WaterQualityEnum.getCode();


    private final String DB_HourData = CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getName();
    private final String DB_MonthData = "MonthData";

    private final String DB_WaterEvaluateData = "WaterStationEvaluateData";

    /**
     * @author: zhangzc
     * @date: 2019/5/22 15:16
     * @Description: 动态条件查询每个水质监测点最新一条监测数据列表数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterQualityLastDataByParamMap", method = RequestMethod.POST)
    public Object getWaterQualityLastDataByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                paramMap.put("dgimns", dgimns);
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                onlineOutPuts = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            } else {
                onlineOutPuts = new ArrayList<>();
            }
            Map<String, Object> resultMap = onlineService.getOutPutLastDatasByParamMap(onlineOutPuts, WaterQualityEnum.getCode(), paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 上午 9:10
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取水质站点监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, monitorpointid, pollutantcodes, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getWaterStationDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getWaterStationDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(monitorpointid);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());

            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("sort", "asc");

            Boolean isPage = false;
            if (pagenum != null && pagesize != null) {
                paramMap.put("pagenum", pagenum);
                paramMap.put("pagesize", pagesize);
                paramMap.put("sort", "desc");
                isPage = true;
            }

            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = setCharDataList(documents, pollutantcodes, collection);
            //获取排口污染物的预警、超限、异常值
            Map<String, Object> standardmap = new HashMap<>();
            if (mns != null && mns.size() > 0 && pollutantcodes.size() > 0 && mns.get(0) != null && pollutantcodes.get(0) != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("dgimn", mns.get(0));
                param.put("pollutantcode", pollutantcodes.get(0));
                param.put("monitorpointtype", monitorPointTypeCode);
                standardmap = onlineService.getPollutantEarlyAlarmStandardDataByParamMap(param);
            }
            if (isPage) {
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                pageMap.put("datalist", charDataList);
                return AuthUtil.parseJsonKeyToLower("success", pageMap);
            } else {
                if (charDataList != null && charDataList.size() > 0) {
                    charDataList.get(0).put("standard", standardmap);
                }
                return AuthUtil.parseJsonKeyToLower("success", charDataList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }


    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 上午 9:53
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个水质站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, monitorpointid, pollutantcodes, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getOneWaterStationListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneWaterStationListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(monitorpointid);

            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 1);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);


            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
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
     * @author: chengzq
     * @date: 2019/9/19 0019 上午 9:16
     * @Description: 转换mongodb数据成图表格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [documents, pollutantcodes, collection]
     * @throws:
     */
    private List<Map<String, Object>> setCharDataList(List<Document> documents, List<String> pollutantcodes, String collection) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> monitorDataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        if ("RealTimeData".equals(collection)) {
            pollutantDataKey = "RealDataList";
            valueKey = "MonitorValue";
        } else if ("MinuteData".equals(collection)) {
            pollutantDataKey = "MinuteDataList";
            valueKey = "AvgStrength";
        } else if ("HourData".equals(collection)) {
            pollutantDataKey = "HourDataList";
            valueKey = "AvgStrength";
        } else if ("DayData".equals(collection)) {
            pollutantDataKey = "DayDataList";
            valueKey = "AvgStrength";
        }
        for (String tempCode : pollutantcodes) {
            Map<String, Object> pollutantMap = new HashMap<>();
            for (Document document : documents) {
                List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
                for (Document temp : pollutantDataList) {
                    if (tempCode.equals(temp.get("PollutantCode"))) {
                        Map<String, Object> map = new HashMap<>();
                        String MonitorTime = "";
                        if ("RealTimeData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                        } else if ("MinuteData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                        } else if ("HourData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        } else if ("DayData".equals(collection)) {
                            MonitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                        }
                        map.put("monitortime", MonitorTime);
                        map.put("monitorvalue", temp.get(valueKey));
                        map.put("isoverstandard", temp.get("IsOverStandard"));
                        monitorDataList.add(map);
                        break;
                    }
                }
            }
            pollutantMap.put("pollutantcode", tempCode);
            pollutantMap.put("monitorDataList", monitorDataList);
            dataList.add(pollutantMap);
        }
        return dataList;
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/19 10:09
     * @Description: 获取水质污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterQualityAlarmDataByParamMap", method = RequestMethod.POST)
    public Object getWaterQualityAlarmDataByParamMap(
            @RequestJson(value = "paramsjson", required = false) Object paramsjson,
            @RequestJson(value = "pagesize", required = false) Integer pageSize,
            @RequestJson(value = "pagenum", required = false) Integer pageNum) {

        Map<String, Object> paramMap = new HashMap<>();
        if (paramsjson != null) {
            paramMap = JSONObject.fromObject(paramsjson);
        }
        paramMap.putIfAbsent("datatype", "RealTimeData");
        if (paramMap.get("counttime") == null) {
            LocalDate today = LocalDate.now();
            //本月的第一天
            LocalDate firstday = LocalDate.of(today.getYear(), today.getMonth(), 1);
            String start = firstday.toString() + " 00:00:00";
            String end = today.toString() + " 23:59:59";
            List<String> counttimes = Arrays.asList(start, end);
            paramMap.putIfAbsent("counttime", counttimes);
        }
        try {
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            paramMap.put("pointtype", WaterQualityEnum.getCode());
            Map<String, Object> resultMap = onlineService.getPollutantEarlyAndOverAlarmsByParamMap(pageNum, pageSize, outputs, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/9/19 11:51
     * @Description: 获取水质污染物超标预警数据详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterQualityAlarmDetailsPage", method = RequestMethod.POST)
    public Object getWaterQualityAlarmDetailsPage(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "datatype") List<String> datatypes,
            @RequestJson(value = "counttype") Integer counttype,
            @RequestJson(value = "pagesize", required = false) Integer pageSize,
            @RequestJson(value = "pagenum", required = false) Integer pageNum) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            int monitorPointTypeCode = WaterQualityEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = endtime + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("datatype", datatypes);
            paramMap.put("counttype", counttype);
            if (pageSize != null) {
                paramMap.put("pagesize", pageSize);
            }
            if (pageSize != null) {
                paramMap.put("pagenum", pageNum);
            }
            paramMap.put("pollutantcodes", pollutantcodes);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorPointTypeCode, new HashMap<>());
            paramMap.put("mns", mns);
            paramMap.put("pointtype", monitorPointTypeCode);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> tablelistdata = onlineService.getPollutantEarlyAndOverAlarmsTableListDataByParamMap(paramMap);
            boolean isoverstandard = false;
            if (tablelistdata != null && tablelistdata.size() > 0) {
                if (tablelistdata.get(0).get("isoverstandard") != null) {
                    isoverstandard = (boolean) tablelistdata.get(0).get("isoverstandard");
                }
            }
            List<Map<String, Object>> tabletitledata = onlineService.getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(isoverstandard, counttype, pollutantcodes, monitorPointTypeCode);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tablelistdata);
            tabledata.put("total", paramMap.get("total"));
            tabledata.put("pages", paramMap.get("pages"));
            tabledata.put("pagesize", pageSize);
            tabledata.put("pagenum", pageNum);
            dataMap.put("tabledata", tabledata);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 下午 3:29
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多个水质站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, monitorpointids, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getManyWaterStationListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyWaterStationListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointids") List<String> monitorpointids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {

            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", monitorpointids);


            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(monitorpointids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
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
     * @date: 2019/11/19 0019 下午 3:15
     * @Description: 自定义查询条件查询水质站点小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterStationHourDataByParams", method = RequestMethod.POST)
    public Object getWaterStationHourDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode) {

        try {
            Map<String, Object> resultMap = new HashMap<>();
            //1,获取三类水评价标准
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("standardlevel", "3");
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> standardValues = onlineWaterQualityService.getWaterQualityStandardByParam(paramMap);
            if (standardValues.size() > 0) {
                Map<String, Object> standardValue = standardValues.get(0);
                resultMap.put("standardvalue", standardValue.get("standardvalue"));
            } else {
                resultMap.put("standardvalue", "");
            }
            paramMap.clear();
            paramMap.put("monitorpointtype", monitorPointTypeCode);
            paramMap.put("outputids", Arrays.asList());
            List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            if (monitorPoints.size() > 0) {
                for (Map<String, Object> monitorPoint : monitorPoints) {
                    mnCommon = monitorPoint.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndMonitorPointName.put(mnCommon, monitorPoint.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, monitorPoint.get("pk_id"));
                }
            }
            paramMap.clear();
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime + ":00:00");
            paramMap.put("endtime", endtime + ":59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", DB_HourData);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (documents.size() > 0) {
                Map<String, List<Map<String, Object>>> mnAndMonitorDataList = setMnAndMonitorDataList(documents, pollutantcode);
                List<Map<String, Object>> monitorDataList;
                for (String mnIndex : mnAndMonitorDataList.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("dgimn", mnIndex);
                    dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                    dataMap.put("monitorpintname", mnAndMonitorPointName.get(mnIndex));
                    monitorDataList = mnAndMonitorDataList.get(mnIndex);
                    //根据时间排序
                    monitorDataList = monitorDataList.stream().sorted(Comparator.comparing(m -> (m.get("monitortime").toString()))).collect(Collectors.toList());
                    dataMap.put("monitordatalist", monitorDataList);
                    dataList.add(dataMap);
                }
            }
            resultMap.put("dataList", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 3:15
     * @Description: 按日、月、年统计单水质站点数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countWaterStationDayMonthData", method = RequestMethod.POST)
    public Object countWaterStationDayMonthData(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode
    ) throws Exception {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            //当前时间
            String thisStartTime = "";
            String thisEndTime = "";

            //同比时间
            String thatStartTime = "";
            String thatEndTime = "";
            String collection = "";
            List<String> timeList = new ArrayList<>();
            switch (timetype) {
                case "day":
                    timeList = DataFormatUtil.getYMDBetween(starttime, endtime);
                    timeList.add(endtime);
                    thisStartTime = starttime + " 00:00:00";
                    thisEndTime = endtime + " 23:59:59";
                    thatStartTime = DataFormatUtil.getDayTBDate(starttime) + " 00:00:00";
                    thatEndTime = DataFormatUtil.getDayTBDate(endtime) + " 23:59:59";
                    collection = dayCollection;
                    break;
                case "month":
                    timeList = DataFormatUtil.getMonthBetween(starttime, endtime);
                    thisStartTime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    thisEndTime = DataFormatUtil.getLastDayOfMonth(endtime);
                    thatStartTime = DataFormatUtil.getMonthTBDate(thisStartTime) + " 00:00:00";
                    thatEndTime = DataFormatUtil.getMonthTBDate(thisEndTime) + " 23:59:59";
                    thisStartTime = thisStartTime + " 00:00:00";
                    thisEndTime = thisEndTime + " 23:59:59";
                    collection = DB_MonthData;
                    break;
                case "year":
                    //根据月数据统计
                    timeList = DataFormatUtil.getYearBetween(starttime, endtime);
                    thisStartTime = starttime + "-01-01 00:00:00";
                    thisEndTime = endtime + "-12-31 23:59:59";
                    collection = DB_MonthData;
                    break;
                default:
                    timetype = "";
                    break;
            }
            if (StringUtils.isBlank(timetype)) {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }

            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("starttime", thisStartTime);
            paramMap.put("endtime", thisEndTime);
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", collection);
            //当前值
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, Object> thisTimeAndValue = new HashMap<>();
            if (documents.size() > 0) {
                thisTimeAndValue = getTimeAndValue(documents, timetype, pollutantcode);
            }
            paramMap.put("starttime", thatStartTime);
            paramMap.put("endtime", thatEndTime);
            //同比值
            Map<String, Object> thatTimeAndValue = new HashMap<>();
            if (!timetype.equals("year")) {
                documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    thatTimeAndValue = getTimeAndValue(documents, timetype, pollutantcode);
                }
            }
            String tbTime;
            for (String time : timeList) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitortime", time);
                resultMap.put("thisvalue", thisTimeAndValue.get(time));
                if (timetype.equals("day")) {
                    tbTime = DataFormatUtil.getDayTBDate(time);
                } else if (timetype.equals("month")) {
                    tbTime = DataFormatUtil.getMonthTBDate(time);
                } else {
                    tbTime = "";
                }
                resultMap.put("thatvalue", thatTimeAndValue.get(tbTime));
                resultList.add(resultMap);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private Map<String, Object> getTimeAndValue(List<Document> documents, String timetype, String pollutantcode) {
        Map<String, Object> timeAndValue = new HashMap<>();
        List<Document> pollutantList;
        String monitorTime = "";
        String dataKey = "";

        if (timetype.equals("year")) {
            Map<String, List<Double>> yearAndValueList = new HashMap<>();
            List<Double> valueList;
            Double value;
            for (Document document : documents) {
                monitorTime = DataFormatUtil.getDateY(document.getDate("MonitorTime"));
                dataKey = "MonthDataList";
                pollutantList = document.get(dataKey, List.class);
                for (Document pollutant : pollutantList) {
                    if (pollutantcode.equals(pollutant.getString("PollutantCode"))) {
                        if (yearAndValueList.containsKey(monitorTime)) {
                            valueList = yearAndValueList.get(monitorTime);
                        } else {
                            valueList = new ArrayList<>();
                        }
                        if (pollutant.get("AvgStrength") != null) {
                            value = Double.parseDouble(pollutant.getString("AvgStrength"));
                            valueList.add(value);
                            yearAndValueList.put(monitorTime, valueList);
                        }
                    }
                }
            }
            for (String year : yearAndValueList.keySet()) {
                timeAndValue.put(year, DataFormatUtil.getListAvgValue(yearAndValueList.get(year)));
            }
        } else {
            //日、月数据
            for (Document document : documents) {
                if (timetype.equals("day")) {
                    monitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    dataKey = "DayDataList";
                } else if (timetype.equals("month")) {
                    monitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    dataKey = "MonthDataList";
                }
                pollutantList = document.get(dataKey, List.class);
                for (Document pollutant : pollutantList) {
                    if (pollutantcode.equals(pollutant.getString("PollutantCode"))) {
                        timeAndValue.put(monitorTime, pollutant.get("AvgStrength"));
                    }
                }

            }
        }
        return timeAndValue;
    }


    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 3:15
     * @Description: 按月、年统计水质站点评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countWaterStationDayEvaluateData", method = RequestMethod.POST)
    public Object countWaterStationDayEvaluateData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "timetype") String timetype) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorPointTypeCode);
            paramMap.put("outputids", Arrays.asList());
            List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndWaterQualityClass = new HashMap<>();
            if (monitorPoints.size() > 0) {
                for (Map<String, Object> monitorPoint : monitorPoints) {
                    mnCommon = monitorPoint.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndMonitorPointName.put(mnCommon, monitorPoint.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, monitorPoint.get("pk_id"));
                    mnAndWaterQualityClass.put(mnCommon, monitorPoint.get("fk_funwaterqaulitycode"));
                }
                //当前+环比时间
                String thisStartTime = "";
                String thisEndTime = "";
                String HBTime = "";
                //同比时间
                String thatStartTime = "";
                String thatEndTime = "";
                String collection = dayCollection;
                switch (timetype) {
                    case "month":
                        //yyyy-MM
                        HBTime = DataFormatUtil.getBeforeByMonthTime(1, monitortime);
                        thisStartTime = DataFormatUtil.getFirstDayOfMonth(HBTime) + " 00:00:00";
                        thisEndTime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
                        thatStartTime = DataFormatUtil.getMonthTBDate(DataFormatUtil.getFirstDayOfMonth(monitortime)) + " 00:00:00";
                        thatEndTime = DataFormatUtil.getMonthTBDate(DataFormatUtil.getLastDayOfMonth(monitortime)) + " 23:59:59";
                        break;
                    case "year":
                        //yyyy
                        HBTime = Integer.parseInt(monitortime) - 1 + "";
                        thisStartTime = HBTime + "-01-01 00:00:00";
                        thisEndTime = monitortime + "-12-31 23:59:59";
                        break;
                    default:
                        timetype = "";
                        break;
                }
                if (StringUtils.isBlank(timetype)) {
                    return AuthUtil.parseJsonKeyToLower("success", resultList);
                }
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", thisStartTime);
                paramMap.put("endtime", thisEndTime);
                paramMap.put("collection", collection);
                //当前值+环比值
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                //当前值
                Map<String, Map<String, Object>> thisMnAndLevelMap = new HashMap<>();
                if (documents.size() > 0) {
                    setThisMnAndLevelMap(thisMnAndLevelMap, documents, timetype, monitortime, mnAndWaterQualityClass, "this");
                }
                //环比值
                Map<String, Map<String, Object>> hbMnAndLevelMap = new HashMap<>();
                if (documents.size() > 0) {
                    setThisMnAndLevelMap(hbMnAndLevelMap, documents, timetype, monitortime, mnAndWaterQualityClass, "hb");
                }
                paramMap.put("starttime", thatStartTime);
                paramMap.put("endtime", thatEndTime);
                //同比值
                Map<String, Map<String, Object>> thatMnAndLevelMap = new HashMap<>();
                documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    setThatMnAndLevelMap(thatMnAndLevelMap, documents, timetype, mnAndWaterQualityClass);
                }
                for (String mnIndex : mnAndMonitorPointId.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                    resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnIndex));
                    resultMap.putAll(thisMnAndLevelMap.get(mnIndex));
                    resultMap.putAll(thatMnAndLevelMap.get(mnIndex));
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
     * @date: 2019/11/19 0019 下午 3:15
     * @Description: 获取小时水质站点评价对比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterStationHourEvaluateData", method = RequestMethod.POST)
    public Object getWaterStationHourEvaluateData(@RequestJson(value = "monitortime") String monitortime) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorPointTypeCode);
            paramMap.put("outputids", Arrays.asList());
            List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndWaterQualityClass = new HashMap<>();
            if (monitorPoints.size() > 0) {
                for (Map<String, Object> monitorPoint : monitorPoints) {
                    mnCommon = monitorPoint.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndMonitorPointName.put(mnCommon, monitorPoint.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, monitorPoint.get("pk_id"));
                    mnAndWaterQualityClass.put(mnCommon, monitorPoint.get("waterqualityclass"));
                }
                //当前+环比时间
                Date thisTime = DataFormatUtil.getDateYMDH(monitortime);
                Date HBTime = DataFormatUtil.getDateYMDH(DataFormatUtil.getBeforeByHourTime(1, monitortime));
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("monitortime", thisTime);
                List<Document> documents = onlineWaterQualityService.setWaterQualityDataByParam(paramMap);
                Map<String, Object> thisMnAndLevel = new HashMap<>();
                Map<String, Object> hbMnAndLevel = new HashMap<>();
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        mnCommon = document.getString("DataGatherCode");
                        thisMnAndLevel.put(mnCommon, document.getString("WaterQualityClass"));
                    }
                    paramMap.clear();
                    paramMap.put("mns", mns);
                    paramMap.put("monitortime", HBTime);
                    //环比数据
                    documents = onlineWaterQualityService.setWaterQualityDataByParam(paramMap);
                    for (Document document : documents) {
                        mnCommon = document.getString("DataGatherCode");
                        hbMnAndLevel.put(mnCommon, document.getString("WaterQualityClass"));
                    }
                }
                int thiscode;
                int hbcode;
                for (String mnIndex : mnAndMonitorPointId.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                    resultMap.put("monitorpointname", mnAndMonitorPointName.get(mnIndex));
                    resultMap.put("mblevel", mnAndWaterQualityClass.get(mnIndex));
                    resultMap.put("thislevel", thisMnAndLevel.get(mnIndex));
                    resultMap.put("hblevel", hbMnAndLevel.get(mnIndex));
                    if (thisMnAndLevel.get(mnIndex) != null && hbMnAndLevel.get(mnIndex) != null) {
                        thiscode = CommonTypeEnum.WaterLevelEnum.getObjectByName(thisMnAndLevel.get(mnIndex).toString()).getIndex();
                        hbcode = CommonTypeEnum.WaterLevelEnum.getObjectByName(hbMnAndLevel.get(mnIndex).toString()).getIndex();
                        if (thiscode < hbcode) {
                            resultMap.put("change", "up");
                        } else if (thiscode == hbcode) {
                            resultMap.put("change", "is");
                        } else {
                            resultMap.put("change", "down");
                        }
                    } else {
                        resultMap.put("change", "-");
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

    private void setThatMnAndLevelMap(Map<String, Map<String, Object>> thatMnAndLevelMap, List<Document> documents, String timetype, Map<String, Object> mnAndWaterQualityClass) {
        String targetLevel;
        String pointLevel;
        String mnCommon;
        Map<String, Object> levelMap;
        for (Document document : documents) {
            pointLevel = document.getString("WaterLevel");
            mnCommon = document.getString("DataGatherCode");
            if (StringUtils.isNotBlank(pointLevel)) {
                if (thatMnAndLevelMap.containsKey(mnCommon)) {
                    levelMap = thatMnAndLevelMap.get(mnCommon);
                } else {
                    levelMap = new HashMap<>();
                }
                //有效天数
                if (levelMap.get("effectiveday") != null) {
                    levelMap.put("effectiveday", Integer.parseInt(levelMap.get("effectiveday").toString()) + 1);
                } else {
                    levelMap.put("effectiveday", 1);
                }
                //达标天数
                if (mnAndWaterQualityClass.get(mnCommon) != null) {
                    targetLevel = mnAndWaterQualityClass.get(mnCommon).toString();
                    pointLevel = pointLevel.replaceAll("类", "");
                    if (DataFormatUtil.formatLevelToNum(pointLevel) <= DataFormatUtil.formatLevelToNum(targetLevel)) {//达标
                        //有效天数
                        if (levelMap.get("standardday") != null) {
                            levelMap.put("standardday", Integer.parseInt(levelMap.get("standardday").toString()) + 1);
                        } else {
                            levelMap.put("standardday", 1);
                        }
                    }
                }
                if (levelMap.get("effectiveday") != null && levelMap.get("standardday") != null) {
                    levelMap.put("tbstandardrate", DataFormatUtil.SaveOneAndSubZero(
                            100d * Integer.parseInt(levelMap.get("standardday").toString()) /
                                    Integer.parseInt(levelMap.get("effectiveday").toString())) + "%"
                    );
                } else {
                    levelMap.put("tbstandardrate", "-");
                }
            }
        }
    }

    private void setThisMnAndLevelMap(Map<String, Map<String, Object>> thisMnAndLevelMap, List<Document> documents, String timetype, String monitortime, Map<String, Object> mnAndWaterQualityClass, String dataMark) {
        Map<String, Object> codeAndName = getCodeAndNameByType(monitorPointTypeCode);
        String targetLevel;
        String pointLevel;
        String pollutantLevel;
        String mnCommon;
        String time;
        Map<String, Object> levelMap;
        List<Document> polluantList;
        String pollutantCode;
        Set<String> overitem;
        for (Document document : documents) {
            pointLevel = document.getString("WaterLevel");
            mnCommon = document.getString("DataGatherCode");
            if (timetype.equals("month")) {
                time = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
            } else {
                time = DataFormatUtil.getDateY(document.getDate("MonitorTime"));
            }
            if (StringUtils.isNotBlank(pointLevel)) {
                if (dataMark.equals("this") && monitortime.equals(time)) {//只取当前值
                    if (thisMnAndLevelMap.containsKey(mnCommon)) {
                        levelMap = thisMnAndLevelMap.get(mnCommon);
                    } else {
                        levelMap = new HashMap<>();
                    }
                    //有效天数
                    if (levelMap.get("effectiveday") != null) {
                        levelMap.put("effectiveday", Integer.parseInt(levelMap.get("effectiveday").toString()) + 1);
                    } else {
                        levelMap.put("effectiveday", 1);
                    }
                    //达标天数
                    if (mnAndWaterQualityClass.get(mnCommon) != null) {
                        targetLevel = mnAndWaterQualityClass.get(mnCommon).toString();
                        pointLevel = pointLevel.replaceAll("类", "");
                        if (DataFormatUtil.formatLevelToNum(pointLevel) <= DataFormatUtil.formatLevelToNum(targetLevel)) {//达标
                            //有效天数
                            if (levelMap.get("standardday") != null) {
                                levelMap.put("standardday", Integer.parseInt(levelMap.get("standardday").toString()) + 1);
                            } else {
                                levelMap.put("standardday", 1);
                            }
                        } else {//不达标
                            polluantList = document.get("DayDataList", List.class);
                            for (Document pollutant : polluantList) {
                                pollutantLevel = pollutant.getString("WaterLevel");
                                if (StringUtils.isNotBlank(pollutantLevel) &&
                                        DataFormatUtil.formatLevelToNum(pollutantLevel) > DataFormatUtil.formatLevelToNum(targetLevel)) {
                                    pollutantCode = pollutant.getString("PollutantCode");
                                    //有效天数
                                    if (levelMap.get("overitem") != null) {
                                        overitem = (Set<String>) levelMap.get("overitem");
                                    } else {
                                        overitem = new HashSet<>();
                                    }
                                    if (codeAndName.get(pollutantCode) != null) {
                                        overitem.add(codeAndName.get(pollutantCode).toString());
                                    }
                                    levelMap.put("overitem", overitem);
                                }
                            }
                        }
                    }
                    if (levelMap.get("effectiveday") != null && levelMap.get("standardday") != null) {
                        levelMap.put("thisstandardrate", DataFormatUtil.SaveOneAndSubZero(
                                100d * Integer.parseInt(levelMap.get("standardday").toString()) /
                                        Integer.parseInt(levelMap.get("effectiveday").toString())) + "%"
                        );
                    } else {
                        levelMap.put("thisstandardrate", "-");
                    }
                } else if (!monitortime.equals(time)) {//只取环比值
                    if (thisMnAndLevelMap.containsKey(mnCommon)) {
                        levelMap = thisMnAndLevelMap.get(mnCommon);
                    } else {
                        levelMap = new HashMap<>();
                    }
                    //有效天数
                    if (levelMap.get("effectiveday") != null) {
                        levelMap.put("effectiveday", Integer.parseInt(levelMap.get("effectiveday").toString()) + 1);
                    } else {
                        levelMap.put("effectiveday", 1);
                    }
                    //达标天数
                    if (mnAndWaterQualityClass.get(mnCommon) != null) {
                        targetLevel = mnAndWaterQualityClass.get(mnCommon).toString();
                        pointLevel = pointLevel.replaceAll("类", "");
                        if (DataFormatUtil.formatLevelToNum(pointLevel) <= DataFormatUtil.formatLevelToNum(targetLevel)) {//达标
                            //有效天数
                            if (levelMap.get("standardday") != null) {
                                levelMap.put("standardday", Integer.parseInt(levelMap.get("standardday").toString()) + 1);
                            } else {
                                levelMap.put("standardday", 1);
                            }
                        }
                    }
                    if (levelMap.get("effectiveday") != null && levelMap.get("standardday") != null) {
                        levelMap.put("hbstandardrate", DataFormatUtil.SaveOneAndSubZero(
                                100d * Integer.parseInt(levelMap.get("standardday").toString()) /
                                        Integer.parseInt(levelMap.get("effectiveday").toString())) + "%"
                        );
                    } else {
                        levelMap.put("hbstandardrate", "-");
                    }
                }
            }
        }

    }


    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 3:15
     * @Description: 自定义查询条件查询多个水质站点评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyWaterStationEvaluateDataByParams", method = RequestMethod.POST)
    public Object getManyWaterStationEvaluateDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datatype") String dataType,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {

        try {
            Map<String, Object> resultMap = new HashMap<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.clear();
            paramMap.put("monitorpointtype", monitorPointTypeCode);
            paramMap.put("outputids", Arrays.asList());
            List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndWaterQualityClass = new HashMap<>();
            if (monitorPoints.size() > 0) {
                for (Map<String, Object> monitorPoint : monitorPoints) {
                    mnCommon = monitorPoint.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndMonitorPointName.put(mnCommon, monitorPoint.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, monitorPoint.get("pk_id"));
                    mnAndWaterQualityClass.put(mnCommon, monitorPoint.get("waterqualityclass"));
                }
            }
            paramMap.clear();
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime + ":00:00");
            paramMap.put("endtime", endtime + ":59:59");
            paramMap.put("collection", DB_WaterEvaluateData);
            paramMap.put("dataTypes", Arrays.asList(dataType));
            if (pagesize != null && pagenum != null) {
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
            }
            List<Document> documents = onlineService.getWaterEvaluateDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (documents.size() > 0) {
                Map<String, Object> codeAndName = getCodeAndNameByType(monitorPointTypeCode);
                Map<String, Map<String, Object>> mnAndEvaluateData = setMnAndEvaluateData(documents, codeAndName);
                for (String mnIndex : mnAndEvaluateData.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("dgimn", mnIndex);
                    dataMap.put("monitorpointid", mnAndMonitorPointId.get(mnIndex));
                    dataMap.put("monitorpintname", mnAndMonitorPointName.get(mnIndex));
                    dataMap.put("waterqualityclass", mnAndWaterQualityClass.get(mnIndex));
                    dataMap.putAll(mnAndEvaluateData.get(mnIndex));
                    dataList.add(dataMap);
                }
            }
            resultMap.put("dataList", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 3:15
     * @Description: 自定义查询条件查询多个水质站点评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneWaterStationEvaluateDataByParams", method = RequestMethod.POST)
    public Object getOneWaterStationEvaluateDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datatype") String dataType,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {

        try {
            Map<String, Object> resultMap = new HashMap<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.clear();
            paramMap.put("monitorpointtype", monitorPointTypeCode);
            paramMap.put("outputids", Arrays.asList(outputid));
            List<Map<String, Object>> monitorPoints = onlineService.getMonitorPointDataByParam(paramMap);
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndWaterQualityClass = new HashMap<>();
            if (monitorPoints.size() > 0) {
                for (Map<String, Object> monitorPoint : monitorPoints) {
                    mnCommon = monitorPoint.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndWaterQualityClass.put(mnCommon, monitorPoint.get("waterqualityclass"));
                }

                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime + ":00:00");
                paramMap.put("endtime", endtime + ":59:59");
                paramMap.put("collection", DB_WaterEvaluateData);
                paramMap.put("dataTypes", Arrays.asList(dataType));
                if (pagesize != null && pagenum != null) {
                    paramMap.put("pagesize", pagesize);
                    paramMap.put("pagenum", pagenum);
                }
                List<Document> documents = onlineService.getWaterEvaluateDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = new ArrayList<>();
                if (documents.size() > 0) {
                    Map<String, Object> codeAndName = getCodeAndNameByType(monitorPointTypeCode);
                    Map<String, Map<String, Object>> mnAndEvaluateData = setTimeAndEvaluateData(documents, codeAndName);
                    for (String timeIndex : mnAndEvaluateData.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("waterqualityclass", monitorPoints.get(0).get("waterqualityclass"));
                        dataMap.putAll(mnAndEvaluateData.get(timeIndex));
                        dataList.add(dataMap);
                    }
                }
                resultMap.put("dataList", dataList);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 5:00
     * @Description: 组装时间和评价数据信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Map<String, Object>> setTimeAndEvaluateData(List<Document> documents, Map<String, Object> codeAndName) {
        Map<String, Map<String, Object>> timeAndEvaluateData = new HashMap<>();
        Map<String, Object> evaluateData;
        String evaluateTime;
        List<Document> overDataList;
        for (Document document : documents) {
            List<Map<String, Object>> dataList = new ArrayList<>();
            evaluateTime = DataFormatUtil.getDateYMDH(document.getDate("EvaluateTime"));
            if (timeAndEvaluateData.containsKey(evaluateTime)) {
                evaluateData = timeAndEvaluateData.get(evaluateTime);
            } else {
                evaluateData = new HashMap<>();
            }
            evaluateData.put("EvaluateTime", evaluateTime);
            evaluateData.put("EvaluateWaterQualityClass", document.get("WaterQualityClass"));
            evaluateData.put("IsOverStandard", document.get("IsOverStandard"));
            overDataList = document.get("OverDataList", List.class);
            for (Document overData : overDataList) {
                if (codeAndName.get(overData.get("PollutantCode")) != null) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("PollutantName", codeAndName.get(overData.get("PollutantCode")));
                    dataMap.put("MonitorValue", overData.get("MonitorValue"));
                    dataMap.put("OverMultiple", overData.get("OverMultiple"));
                    dataMap.put("PollutantCode", overData.get("PollutantCode"));
                    dataList.add(dataMap);
                }
            }
            evaluateData.put("overdata", dataList);
            timeAndEvaluateData.put(evaluateTime, evaluateData);
        }
        timeAndEvaluateData = DataFormatUtil.sortByKey(timeAndEvaluateData, true);
        return timeAndEvaluateData;

    }


    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 5:00
     * @Description: 组装mn和评价数据信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Map<String, Object>> setMnAndEvaluateData(List<Document> documents, Map<String, Object> codeAndName) {
        Map<String, Map<String, Object>> mnAndEvaluateData = new HashMap<>();
        Map<String, Object> evaluateData;
        String mnCommon;
        String evaluateTime;
        List<Document> overDataList;
        for (Document document : documents) {
            List<Map<String, Object>> dataList = new ArrayList<>();
            mnCommon = document.getString("DataGatherCode");
            evaluateTime = DataFormatUtil.getDateYMDH(document.getDate("EvaluateTime"));
            if (mnAndEvaluateData.containsKey(mnCommon)) {
                evaluateData = mnAndEvaluateData.get(mnCommon);
            } else {
                evaluateData = new HashMap<>();
            }
            evaluateData.put("EvaluateTime", evaluateTime);
            evaluateData.put("EvaluateWaterQualityClass", document.get("WaterQualityClass"));
            evaluateData.put("IsOverStandard", document.get("IsOverStandard"));
            overDataList = document.get("OverDataList", List.class);
            for (Document overData : overDataList) {
                if (codeAndName.get(overData.get("PollutantCode")) != null) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("PollutantName", codeAndName.get(overData.get("PollutantCode")));
                    dataMap.put("MonitorValue", overData.get("MonitorValue"));
                    dataMap.put("OverMultiple", overData.get("OverMultiple"));
                    dataMap.put("PollutantCode", overData.get("PollutantCode"));
                    dataList.add(dataMap);
                }
            }
            evaluateData.put("overdata", dataList);
            mnAndEvaluateData.put(mnCommon, evaluateData);
        }
        return mnAndEvaluateData;

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
     * @date: 2019/11/19 0019 下午 4:08
     * @Description: 组装mn和监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, List<Map<String, Object>>> setMnAndMonitorDataList(List<Document> documents, String pollutantcode) {
        Map<String, List<Map<String, Object>>> mnAndMonitorDataList = new HashMap<>();
        List<Map<String, Object>> monitorDataList;
        String mnCommon;
        String monitorTime;
        List<Document> pollutants;
        Object monitorvalue;
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            monitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            if (mnAndMonitorDataList.containsKey(mnCommon)) {
                monitorDataList = mnAndMonitorDataList.get(mnCommon);
            } else {
                monitorDataList = new ArrayList<>();
            }
            pollutants = document.get("HourDataList", List.class);
            monitorvalue = null;
            for (Document pollutant : pollutants) {
                if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                    monitorvalue = pollutant.get("AvgStrength");
                    break;
                }
            }
            Map<String, Object> monitorData = new HashMap<>();
            monitorData.put("monitortime", monitorTime);
            monitorData.put("monitorvalue", monitorvalue);
            monitorDataList.add(monitorData);
            mnAndMonitorDataList.put(mnCommon, monitorDataList);
        }
        return mnAndMonitorDataList;
    }


    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 下午 3:30
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取水质多点位多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, outputids, pollutantcodes, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getWaterStationManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getWaterStationManyPollutantMonitorDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, String> idAndName = onlineService.getOutPutIdAndPollution(outputids, monitorPointTypeCode);
            Map<String, String> codeAndName = onlineService.getPollutantCodeAndName(pollutantcodes, monitorPointTypeCode);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(
                    documents,
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
     * @author: chengzq
     * @date: 2019/9/19 0019 下午 5:06
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个水质站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, outputid, starttime, endtime, request, response]
     * @throws:
     */
    @RequestMapping(value = "exportOneWaterStationReport", method = RequestMethod.POST)
    public void exportOneWaterStationReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 1);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            //获取表格数据
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
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
            String fileName = "水质监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 下午 5:07
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个水质站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, outputids, starttime, endtime, request, response]
     * @throws:
     */
    @RequestMapping(value = "exportManyWaterStationReport", method = RequestMethod.POST)
    public void exportManyWaterStationReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据

            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);

            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);

            //获取表格数据
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
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
            String fileName = "水质监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: zhangzc
     * @date: 2019/9/19 15:58
     * @Description: 导出水质监测数据报警数据详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportWaterQualityAlarmData", method = RequestMethod.POST)
    public void exportWaterQualityAlarmData(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "datatype") List<String> datatypes,
            @RequestJson(value = "counttype") Integer counttype, HttpServletResponse response, HttpServletRequest request) throws IOException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = endtime + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("datatype", datatypes);
            paramMap.put("counttype", counttype);
            paramMap.put("pollutantcodes", pollutantcodes);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorPointTypeCode, new HashMap<>());
            paramMap.put("mns", mns);
            paramMap.put("pointtype", monitorPointTypeCode);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> tablelistdata = onlineService.getPollutantEarlyAndOverAlarmsTableListDataByParamMap(paramMap);
            boolean isoverstandard = false;
            if (tablelistdata != null && tablelistdata.size() > 0) {
                if (tablelistdata.get(0).get("isoverstandard") != null) {
                    isoverstandard = (boolean) tablelistdata.get(0).get("isoverstandard");
                }
            }
            List<Map<String, Object>> tabletitledata = onlineService.getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(isoverstandard, counttype, pollutantcodes, monitorPointTypeCode);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "水质污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/11/20 0020 上午 10:36
     * @Description: 获取水质浓度突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getChangeWarnPollutantInfo", method = RequestMethod.POST)
    public Object getChangeWarnPollutantInfo(@RequestJson(value = "dgimn") String dgimn,
                                             @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "endtime") String endtime) {
        try {
            Integer monitortype = WaterQualityEnum.getCode();
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
     * @author: chengzq
     * @date: 2019/11/20 0020 上午 10:41
     * @Description: 获取水质单个污染物浓度突增数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime, pollutantcode]
     * @throws:
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
     * @author: chengzq
     * @date: 2019/11/20 0020 上午 10:43
     * @Description: 条件查询水质浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getWaterChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getWaterChangeWarnDetailParams(@RequestJson(value = "dgimn") String dgimn,
                                                 @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            int monitortype = WaterQualityEnum.getCode();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            paramMap.put("collectiontype", collectiontype);
            paramMap.put("monitortype", monitortype);
            paramMap.put("remindtype", remindtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> abruptChangeInfoByParam = onlineService.getAbruptPollutantsDischargeInfoByParam(paramMap);
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum != null && pagesize != null) {
                List<Map<String, Object>> collect = abruptChangeInfoByParam.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("monitortime").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
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
     * @author: chengzq
     * @date: 2020/2/10 0010 下午 3:38
     * @Description: 获取水质等级占比信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, datetype, endtime]
     * @throws:
     */
    @RequestMapping(value = "getWaterStationQaulityInfosByMonitorTime", method = RequestMethod.POST)
    public Object getWaterStationQaulityInfosByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "datetype") String datetype,
                                                           @RequestJson(value = "endtime") String endtime,
                                                           @RequestJson(value = "dgimn", required = false) String dgimn) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<String> dgimns = new ArrayList<>();
            if (dgimn != null) {
                dgimns.add(dgimn);
            } else {//若未传MN则默认查所有水质站点的水质构成
                List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
                //获取所有mn号
                dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", datetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);
            if (waterStationOnlineDataByParamMap != null && waterStationOnlineDataByParamMap.size() > 0) {
                //有效天数
                long total = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("WaterLevel") != null).count();
                //按水质级别分组
                Map<String, List<Map>> collect = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("WaterLevel") != null).collect(Collectors.groupingBy(m -> m.get("WaterLevel").toString()));
                for (String level : collect.keySet()) {
                    Double count = Double.valueOf(collect.get(level).size() + "");
                    Map<String, Object> data = new HashMap<>();
                    data.put("fk_waterqaulitycode", level);
                    data.put("proportion", decimalFormat.format(count / total * 100));
                    resultList.add(data);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/10 0010 下午 4:30
     * @Description: 获取一段时间内水质达标率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, datetype, endtime]
     * @throws:
     */
    @RequestMapping(value = "getStandardReachingRateInfosByMonitorTime", method = RequestMethod.POST)
    public Object getStandardReachingRateInfosByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "datetype") String datetype,
                                                            @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();

            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", datetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);
            if (waterStationOnlineDataByParamMap.size() == 0) {
                resultMap.put("total", 0);
                resultMap.put("standardreachingnum", 0);
                resultMap.put("standardreachingrate", 0);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            int total = 0;
            int dabiao = 0;
            for (Map map : waterStationOnlineDataByParamMap) {
                String DataGatherCode = map.get("DataGatherCode") == null ? "" : map.get("DataGatherCode").toString();
                if (map.get("WaterLevel") != null) {
                    total += 1;
                    String WaterLevel = map.get("WaterLevel") == null ? "" : map.get("WaterLevel").toString();
                    for (Map<String, Object> output : outputs) {
                        String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                        if (output.get("FK_FunWaterQaulityCode") != null) {
                            String FK_FunWaterQaulityCode = output.get("FK_FunWaterQaulityCode") == null ? "" : output.get("FK_FunWaterQaulityCode").toString();
                            //如果数据等级小于目标等级为达标数据
                            if (dgimn.equals(DataGatherCode) && CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(FK_FunWaterQaulityCode)) {
                                dabiao++;
                            }
                        }
                    }
                }
            }
            if (total > 0) {
                resultMap.put("total", total);
                resultMap.put("standardreachingnum", dabiao);
                resultMap.put("standardreachingrate", DataFormatUtil.SaveOneAndSubZero(100 * Double.valueOf(dabiao) / Double.valueOf(total)));
            } else {
                resultMap.put("total", 0);
                resultMap.put("standardreachingnum", 0);
                resultMap.put("standardreachingrate", 0);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/11 0011 下午 4:13
     * @Description: 超标站点排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getStationUnStandardReachingRankingByMonitorTime", method = RequestMethod.POST)
    public Object getStationUnStandardReachingRankingByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                                   @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("pollutanttype", WaterQualityEnum.getCode());
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "hour");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

            Map<String, List<Map>> collect = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String DataGatherCode : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map> list = collect.get(DataGatherCode);
                Integer stationchaobiao = 0;//站点超标次数
                for (Map map : list) {
                    String WaterLevel = map.get("WaterLevel") == null ? "" : map.get("WaterLevel").toString();
                    //统计站点超标次数
                    for (Map<String, Object> output : outputs) {

                        String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                        String FK_FunWaterQaulityCode = output.get("FK_FunWaterQaulityCode") == null ? "" : output.get("FK_FunWaterQaulityCode").toString();
                        //如果数据等级小于目标等级为达标数据
                        if (dgimn.equals(DataGatherCode)) {
                            if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(FK_FunWaterQaulityCode)) {
                                stationchaobiao++;
                                data.put("stationovernum", stationchaobiao);
                                data.put("monitorpointname", output.get("monitorpointname"));
                            }
                        }
                    }
                }
                if (stationchaobiao > 0) {
                    resultList.add(data);
                }

            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("stationovernum") != null).sorted(Comparator.comparing(m -> ((Map) m).get("stationovernum").toString()).reversed()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/11 0011 下午 4:13
     * @Description: 超标污染物排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getPollutantUnStandardReachingRankingByMonitorTime", method = RequestMethod.POST)
    public Object getPollutantUnStandardReachingRankingByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                                     @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("pollutanttype", WaterQualityEnum.getCode());
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);

            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "hour");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

            //筛选出未达标数据
            List<Map<String, Object>> overdatas = new ArrayList<>();
            Map<String, List<Map>> collect = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            for (String DataGatherCode : collect.keySet()) {
                List<Map> list = collect.get(DataGatherCode);
                for (Map map : list) {
                    String WaterLevel = map.get("WaterLevel") == null ? "" : map.get("WaterLevel").toString();
                    //统计站点超标次数
                    for (Map<String, Object> output : outputs) {
                        String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                        String FK_FunWaterQaulityCode = output.get("FK_FunWaterQaulityCode") == null ? "" : output.get("FK_FunWaterQaulityCode").toString();
                        //如果数据等级小于目标等级为达标数据
                        if (dgimn.equals(DataGatherCode)) {
                            if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(FK_FunWaterQaulityCode)) {
                                overdatas.add(map);
                            }
                        }
                    }
                }

            }


            //统计污染物超标次数
            List<Map<String, Object>> pollutantoverdata = new ArrayList<>();
            for (Map<String, Object> overdata : overdatas) {
                String WaterLevel = overdata.get("WaterLevel") == null ? "" : overdata.get("WaterLevel").toString();
                JSONArray hourDataList = JSONArray.fromObject(overdata.get("HourDataList"));

                for (Object o : hourDataList) {
                    Map pollutantdata = (Map) o;
                    String pollutantLevel = pollutantdata.get("WaterLevel") == null ? "" : pollutantdata.get("WaterLevel").toString();
                    if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(pollutantLevel)) {
                        pollutantoverdata.add(pollutantdata);
                    }
                }
            }


            Map<String, Long> collect1 = pollutantoverdata.stream().filter(m -> m.get("PollutantCode") != null).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString(), Collectors.counting()));
            for (String PollutantCode : collect1.keySet()) {
                Map<String, Object> data = new HashMap<>();
                Long aLong = collect1.get(PollutantCode);

                List<Map<String, Object>> collects = dataList.stream().filter(m -> m.get("pollutantcode") != null && m.get("pollutantcode").toString().equals(PollutantCode)).collect(Collectors.toList());

                if (collects.size() > 0) {
                    Map<String, Object> stringObjectMap = collects.get(0);
                    Object pollutantname = stringObjectMap.get("pollutantname");
                    data.put("pollutantname", pollutantname);
                    data.put("pollutantcode", PollutantCode);
                    data.put("pollutantovernum", aLong);
                    resultList.add(data);
                }

            }


            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("pollutantovernum") != null).sorted(Comparator.comparing(m -> ((Map) m).get("pollutantovernum").toString()).reversed()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 1:58
     * @Description: 通过监测时间获取水质评价小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, pagesize, pagenum, endtime]
     * @throws:
     */
    @RequestMapping(value = "getHourEvaluateInfoByMonitorTime", method = RequestMethod.POST)
    public Object getHourEvaluateInfoByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                   @RequestJson(value = "alarmtypes", required = false) Object alarmtypes,
                                                   @RequestJson(value = "datetype") String datetype,
                                                   @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
//            List<String> alarmtypesinfo = (List<String>) alarmtypes;

            paramMap.put("tablename", "PUB_CODE_WaterQualityClass");
            List<Map<String, Object>> waterclass = pubCodeService.getPubCodeDataByParam(paramMap);


            paramMap.put("pollutanttype", WaterQualityEnum.getCode());
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);

            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", datetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("alarmtypes", alarmtypes);
            List<Map> waterStationOverDataByParamMap = onlineService.getOverDataByParamMap(paramMap);

            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);


            Map<String, List<Map>> collects = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString() + "," + m.get("MonitorTime").toString()));

            for (String DataGatherCode : collects.keySet()) {
                String[] split = DataGatherCode.split(",");
                String dgimn = "";
                String MonitorTime = "";
                if (split.length > 0) {
                    dgimn = split[0];
                    MonitorTime = split[1];
                }
                //站点下在线数据
                List<Map> list = collects.get(DataGatherCode)/*.stream().filter(m -> ((Map) m).get("MonitorTime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("MonitorTime").toString()).reversed()).collect(Collectors.toList())*/;

                String finalDgimn = dgimn;
                String finalMonitorTime = MonitorTime;
                //小时在线数据筛选和超标数据相同mn号和超标时间相同的数据
                List<Map> collect1 = waterStationOverDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("OverTime") != null && finalDgimn.equals(m.get("DataGatherCode").toString()) && finalMonitorTime.equals(m.get("OverTime").toString())).collect(Collectors.toList());
                //筛选相同站点的站点数据
                List<Map> collect2 = outputs.stream().filter(m -> m.get("dgimn") != null && finalDgimn.equals(m.get("dgimn").toString())).collect(Collectors.toList());
                if (collect2.size() > 0) {
                    Map map = collect2.get(0);//站点数据
                    //水质目标
                    String waterqualityclass = map.get("waterqualityclass") == null ? "" : map.get("waterqualityclass").toString();
                    String fk_funwaterqaulitycode = map.get("fk_funwaterqaulitycode") == null ? "" : map.get("fk_funwaterqaulitycode").toString();
                    String fk_controllevelname = map.get("fk_controllevelname") == null ? "" : map.get("fk_controllevelname").toString();
                    String outputname = map.get("outputname") == null ? "" : map.get("outputname").toString();
                    String monitorpointid = map.get("monitorpointid") == null ? "" : map.get("monitorpointid").toString();
                    String mn = map.get("dgimn") == null ? "" : map.get("dgimn").toString();

                    for (Map maps : list) {//水质在线数据
                        String datalist = "";
                        if ("day".equals(datetype)) {
                            datalist = "DayDataList";
                        } else {
                            datalist = "HourDataList";
                        }
                        List<Map<String, Object>> HourDataList = maps.get(datalist) == null ? new ArrayList<>() : (ArrayList) maps.get(datalist);
                        List<Map<String, Object>> pollutantdata = new ArrayList<>();
                        Map<String, Object> data = new HashMap<>();


                        //添加污染物信息
                        for (Map<String, Object> stringObjectMap : HourDataList) {
                            String PollutantCode = stringObjectMap.get("PollutantCode") == null ? "" : stringObjectMap.get("PollutantCode").toString();
                            String AvgStrength = stringObjectMap.get("AvgStrength") == null ? "" : stringObjectMap.get("AvgStrength").toString();
                           /* String OverMultiple = stringObjectMap.get("OverMultiple") == null ? "" : stringObjectMap.get("OverMultiple").toString();
                            Integer IsOver = stringObjectMap.get("IsOver") == null ? -20 :Integer.valueOf(stringObjectMap.get("IsOver").toString());
                            Boolean IsOverStandard = stringObjectMap.get("IsOverStandard") == null ? false : (Boolean)stringObjectMap.get("IsOverStandard");*/
                            //设置超标，异常等标记
                            makeFlag(data, HourDataList, new ArrayList<>());
                            Optional<Map<String, Object>> collect3 = dataList.stream().filter(m -> m.get("pollutantcode") != null && m.get("pollutantname") != null && PollutantCode.equals(m.get("pollutantcode").toString())).findFirst();
                            if (collect3.isPresent()) {
                                Map<String, Object> pollutantinfo = collect3.get();
                                pollutantinfo.put("AvgStrength", AvgStrength);
                                /*pollutantinfo.put("OverMultiple",OverMultiple);
                                pollutantinfo.put("IsOver",false);
                                if(IsOver>0 && IsOverStandard){
                                    pollutantinfo.put("IsOver",true);
                                }*/
                                pollutantdata.add(pollutantinfo);
                            }

                        }


                        data.put("pollutantdata", pollutantdata);
                        data.put("outputname", outputname);
                        data.put("dgimn", mn);
                        data.put("fk_controllevelname", fk_controllevelname);
                        data.put("monitorpointid", monitorpointid);
                        data.put("funqaulity", waterqualityclass);//设置目标水质类别
                        data.put("stationname", map.get("monitorpointname"));//设置站点名称
                        //水质类别
                        String WaterLevel = maps.get("WaterLevel") == null ? "" : maps.get("WaterLevel").toString();

                        List<Map<String, Object>> collect = waterclass.stream().filter(m -> m.get("Code") != null && m.get("Code").toString().equals(WaterLevel)).collect(Collectors.toList());
                        if (collect.size() > 0) {
                            data.put("qaulity", collect.get(0).get("Name"));//设置水质类别
                            //如果水质类别小于目标类别  为达标
                            if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(fk_funwaterqaulitycode)) {
                                data.put("isupstandard", true);//设置是否达标
                            } else {
                                data.put("isupstandard", false);//设置是否达标
                            }
                        }

                        data.put("monitortime", DataFormatUtil.formatCST(MonitorTime));


                        List<String> pollutants = new ArrayList();
                        List<String> OverMultiples = new ArrayList();
                        for (Map overdata : collect1) {
                            String PollutantCode = overdata.get("PollutantCode") == null ? "" : overdata.get("PollutantCode").toString();
                            String OverMultiple = overdata.get("OverMultiple") == null ? "" : overdata.get("OverMultiple").toString();
                            List<String> collect3 = dataList.stream().filter(m -> m.get("pollutantcode") != null && m.get("pollutantname") != null && PollutantCode.equals(m.get("pollutantcode").toString())).map(m -> m.get("pollutantname").toString()).collect(Collectors.toList());
                            if (collect3.size() > 0) {
                                String s = collect3.get(0);
                                pollutants.add(s);
                            }
                            OverMultiples.add(decimalFormat.format(Float.valueOf(OverMultiple)));

                        }
                        if (pollutants.size() > 0 && OverMultiples.size() > 0) {
                            String soveritem = pollutants.stream().collect(Collectors.joining("、")) + "(" + OverMultiples.stream().collect(Collectors.joining("、")) + ")";
                            data.put("soveritem", soveritem);

                        } else {
                            data.put("soveritem", "-");

                        }
                        resultList.add(data);
                    }
                }
            }

            /*//正常数据
            if(alarmtypesinfo.contains("normal")){
                resultList=resultList.stream().filter(m->m.get("isover")!=null && m.get("isexception")!=null && m.get("issuddenchange")!=null &&
                        !(Boolean) m.get("isover") && !(Boolean) m.get("isexception") && !(Boolean) m.get("issuddenchange")).collect(Collectors.toList());
            }*/
            resultMap.put("total", resultList.size());
            if (pagenum != null && pagesize != null) {
                resultList = resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            } else {
                resultList = resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            }
            resultMap.put("data", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/19 0019 上午 10:22
     * @Description: 通过监测时间获取各点位水质评价最新小时数据并根据当天的小时数据判断点位状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, pagesize, pagenum, endtime]
     * @throws:
     */
    @RequestMapping(value = "getHourEvaluateInfoByMonitorTimeForApp", method = RequestMethod.POST)
    public Object getHourEvaluateInfoByMonitorTimeForApp(@RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "alarmtypes", required = false) Object alarmtypes,
                                                         @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            paramMap.put("tablename", "PUB_CODE_WaterQualityClass");
            List<Map<String, Object>> waterclass = pubCodeService.getPubCodeDataByParam(paramMap);
            paramMap.put("pollutanttype", WaterQualityEnum.getCode());
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "hour");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("alarmtypes", alarmtypes);
            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);
            //Map<String, List<Map>> collects = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString() + "," + m.get("MonitorTime").toString()));
            Map<String, List<Map>> collects = waterStationOnlineDataByParamMap.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            //筛选相同站点的站点数据
            Map<String, List<Map<String, Object>>> mnandpoint = outputs.stream().collect(Collectors.groupingBy(m -> m.get("dgimn").toString()));
            for (String DataGatherCode : collects.keySet()) {
                //站点下在线数据
                List<Map> list = collects.get(DataGatherCode);
                if (list.size() > 0) {
                    String datalist = "HourDataList";
                    //排序
                    Comparator<Object> orderbytime = Comparator.comparing(m -> ((Map) m).get("MonitorTime").toString()).reversed();
                    list = list.stream().sorted(orderbytime).collect(Collectors.toList());
                    //设置超标，异常等标记
                    boolean isexception = false;
                    boolean isover = false;
                    boolean iswarn = false;
                    boolean IsSuddenChange = false;
                    for (Map objmap : list) {
                        Map<String, Object> overflagmap = new HashMap<>();
                        List<Map<String, Object>> HourDataList = objmap.get(datalist) == null ? new ArrayList<>() : (ArrayList) objmap.get(datalist);
                        makeFlag(overflagmap, HourDataList, new ArrayList<>());
                        isexception = Boolean.valueOf(overflagmap.get("isexception").toString()) == true ? Boolean.valueOf(overflagmap.get("isexception").toString()) : false;
                        isover = Boolean.valueOf(overflagmap.get("isover").toString()) == true ? Boolean.valueOf(overflagmap.get("isover").toString()) : false;
                        iswarn = Boolean.valueOf(overflagmap.get("iswarn").toString()) == true ? Boolean.valueOf(overflagmap.get("iswarn").toString()) : false;
                        IsSuddenChange = Boolean.valueOf(overflagmap.get("IsSuddenChange").toString()) == true ? Boolean.valueOf(overflagmap.get("IsSuddenChange").toString()) : false;
                    }
                    Map map = list.get(0);//最新一条小时数据
                    List<Map<String, Object>> pointlist = mnandpoint.get(DataGatherCode);
                    Map<String, Object> pointmap = pointlist.get(0);
                    //水质目标
                    String waterqualityclass = pointmap.get("waterqualityclass") == null ? "" : pointmap.get("waterqualityclass").toString();
                    String fk_funwaterqaulitycode = pointmap.get("fk_funwaterqaulitycode") == null ? "" : pointmap.get("fk_funwaterqaulitycode").toString();
                    String fk_controllevelname = pointmap.get("fk_controllevelname") == null ? "" : pointmap.get("fk_controllevelname").toString();
                    String outputname = pointmap.get("outputname") == null ? "" : pointmap.get("outputname").toString();
                    String monitorpointid = pointmap.get("monitorpointid") == null ? "" : pointmap.get("monitorpointid").toString();
                    String mn = pointmap.get("dgimn") == null ? "" : pointmap.get("dgimn").toString();
                    List<Map<String, Object>> HourDataList = map.get(datalist) == null ? new ArrayList<>() : (ArrayList) map.get(datalist);
                    List<Map<String, Object>> pollutantdata = new ArrayList<>();
                    Map<String, Object> data = new HashMap<>();
                    data.put("isexception", isexception);
                    data.put("isover", isover);
                    data.put("iswarn", iswarn);
                    data.put("IsSuddenChange", IsSuddenChange);
                    //添加污染物信息
                    for (Map<String, Object> stringObjectMap : HourDataList) {
                        String PollutantCode = stringObjectMap.get("PollutantCode") == null ? "" : stringObjectMap.get("PollutantCode").toString();
                        String AvgStrength = stringObjectMap.get("AvgStrength") == null ? "" : stringObjectMap.get("AvgStrength").toString();
                        Optional<Map<String, Object>> collect3 = dataList.stream().filter(m -> m.get("pollutantcode") != null && m.get("pollutantname") != null && PollutantCode.equals(m.get("pollutantcode").toString())).findFirst();
                        if (collect3.isPresent()) {
                            Map<String, Object> pollutantinfo = collect3.get();
                            pollutantinfo.put("AvgStrength", AvgStrength);
                            pollutantdata.add(pollutantinfo);
                        }
                    }
                    data.put("monitortime", DataFormatUtil.getDateYMDHMS((Date) map.get("MonitorTime")));
                    data.put("pollutantdata", pollutantdata);
                    data.put("outputname", outputname);
                    data.put("dgimn", mn);
                    data.put("fk_controllevelname", fk_controllevelname);
                    data.put("monitorpointid", monitorpointid);
                    data.put("funqaulity", waterqualityclass);//设置目标水质类别
                    data.put("stationname", map.get("monitorpointname"));//设置站点名称
                    //水质类别
                    String WaterLevel = map.get("WaterLevel") == null ? "" : map.get("WaterLevel").toString();

                    List<Map<String, Object>> collect = waterclass.stream().filter(m -> m.get("Code") != null && m.get("Code").toString().equals(WaterLevel)).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        data.put("qaulity", collect.get(0).get("Name"));//设置水质类别
                        //如果水质类别小于目标类别  为达标
                        if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(fk_funwaterqaulitycode)) {
                            data.put("isupstandard", true);//设置是否达标
                        } else {
                            data.put("isupstandard", false);//设置是否达标
                        }
                    }
                    resultList.add(data);
                }
            }
            resultList = resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/5/13 0013 上午 9:40
     * @Description: 给在线数据设置超标，异常，预警等标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map, collect, pollutantcode, PollutantSuddenChangeDataByParamMap]
     * @throws:
     */
    private void makeFlag(Map<String, Object> map, List<Map<String, Object>> collect, List<Map<String, Object>> PollutantSuddenChangeDataByParamMap) {
        map.put("isexception", false);//异常
        map.put("isover", false);//超域
        map.put("iswarn", false);//预警
        map.put("IsSuddenChange", false);//突变

        //超标
        long isover = collect.stream().filter(m -> m.get("PollutantCode") != null && m.get("IsOver") != null &&
                Integer.valueOf(m.get("IsOver").toString()) > 0).count();
        //预警
        long iswarn = collect.stream().filter(m -> m.get("PollutantCode") != null && m.get("IsOver") != null &&
                Integer.valueOf(m.get("IsOver").toString()) == 0).count();

        long isexception = collect.stream().filter(m -> m.get("PollutantCode") != null && m.get("IsException") != null &&
                Integer.valueOf(m.get("IsException").toString()) >= 1).count();
        //超标
        long IsOverStandard = collect.stream().filter(m -> m.get("PollutantCode") != null && m.get("IsOverStandard") != null &&
                (Boolean) m.get("IsOverStandard")).count();
        //突变
        long IsSuddenChange = PollutantSuddenChangeDataByParamMap.stream().filter(m -> m.get("pollutantcode") != null).count();

        if (isexception > 0) {
            map.put("isexception", true);//异常
        }
        if (isover > 0 && IsOverStandard > 0) {
            map.put("isover", true);//超标
        }
        if (iswarn > 0) {
            map.put("iswarn", true);//预警
        }
        if (IsSuddenChange > 0) {
            map.put("IsSuddenChange", true);//突变
        }
    }


    /**
     * @author: lip
     * @date: 2020/3/17 0017 下午 2:32
     * @Description: 统计水质达标天数以及达标率（周统计，月统计，年统计）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countStandardDayData", method = RequestMethod.POST)
    public Object countStandardDayData(@RequestJson(value = "monitortime") String monitortime,
                                       @RequestJson(value = "timetype") String timeType

    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> stationDataList = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            if (stationDataList.size() > 0) {
                String mnCommon;
                String monitorpointname;
                String waterqualityclass;
                List<String> mns = new ArrayList<>();

                Map<String, String> mnAndWaterLevel = new HashMap<>();
                Map<String, String> mnAndName = new HashMap<>();
                for (Map<String, Object> stationData : stationDataList) {
                    mnCommon = stationData.get("dgimn") != null ? stationData.get("dgimn").toString() : "";
                    monitorpointname = stationData.get("monitorpointname") != null ? stationData.get("monitorpointname").toString() : "";
                    waterqualityclass = stationData.get("FK_FunWaterQaulityCode") != null ? stationData.get("FK_FunWaterQaulityCode").toString() : "";
                    if (StringUtils.isNotBlank(mnCommon) && StringUtils.isNotBlank(monitorpointname) && StringUtils.isNotBlank(waterqualityclass)) {
                        mnAndName.put(mnCommon, monitorpointname);
                        mnAndWaterLevel.put(mnCommon, waterqualityclass);
                        mns.add(mnCommon);
                    }
                }
                if (mnAndName.size() > 0) {
                    String starttime = "";
                    String endtime = "";
                    switch (timeType) {
                        case "week":
                            String year = monitortime.split("-")[0];
                            String week = monitortime.split("-")[1];
                            int yearNum = Integer.parseInt(year);
                            int weekNum = Integer.parseInt(week);
                            starttime = DataFormatUtil.getStartDayOfWeekNo(yearNum, weekNum) + " 00:00:00";
                            endtime = DataFormatUtil.getEndDayOfWeekNo(yearNum, weekNum) + " 23:59:59";
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
                    paramMap.clear();
                    paramMap.put("starttime", starttime);
                    paramMap.put("endtime", endtime);
                    paramMap.put("mns", mns);
                    paramMap.put("collection", dayCollection);
                    List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                    if (documents.size() > 0) {
                        String monitorTime;
                        Map<String, List<String>> mnAndTimes = new HashMap<>();
                        List<String> times;
                        List<String> levels;
                        Map<String, List<String>> mnAndLevels = new HashMap<>();
                        for (Document document : documents) {
                            mnCommon = document.getString("DataGatherCode");
                            waterqualityclass = document.getString("WaterLevel");
                            monitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                            if (mnAndTimes.containsKey(mnCommon)) {
                                times = mnAndTimes.get(mnCommon);
                                levels = mnAndLevels.get(mnCommon);
                            } else {
                                times = new ArrayList<>();
                                levels = new ArrayList<>();
                            }
                            times.add(monitorTime);
                            levels.add(waterqualityclass);
                            mnAndTimes.put(mnCommon, times);
                            mnAndLevels.put(mnCommon, levels);
                        }

                        int effectiveday;
                        int standardday;
                        for (String mnKey : mnAndName.keySet()) {
                            Map<String, Object> resultMap = new HashMap<>();
                            times = mnAndTimes.get(mnKey);
                            levels = mnAndLevels.get(mnKey);
                            effectiveday = 0;
                            standardday = 0;
                            if (times != null) {
                                effectiveday = times.size();
                                standardday = getStandardDay(levels, mnAndWaterLevel.get(mnKey));
                            }
                            resultMap.put("monitorpointname", mnAndName.get(mnKey));
                            resultMap.put("effectiveday", effectiveday);
                            resultMap.put("standardday", standardday);
                            resultMap.put("standardrate", getStandardRate(effectiveday, standardday));
                            resultList.add(resultMap);
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

    private Object getStandardRate(int effectiveday, int standardday) {
        if (effectiveday > 0) {
            return DataFormatUtil.SaveOneAndSubZero(100d * standardday / effectiveday) + "%";
        } else {
            return "0%";
        }
    }

    private int getStandardDay(List<String> levels, String level) {
        int day = 0;
        int monitorNum;
        int targetNum = DataFormatUtil.formatLevelToNum(level);
        if (targetNum > 0) {
            for (String levelIndex : levels) {
                monitorNum = DataFormatUtil.formatLevelToNum(levelIndex);
                if (monitorNum > 0 && targetNum >= monitorNum) {
                    day++;
                }
            }
        }
        return day;

    }


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 1:56
     * @Description: 通过监测时间统计站点水质达标率同比占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "countCountySectionEvaluateDataByTime", method = RequestMethod.POST)
    public Object countCountySectionEvaluateDataByTime(@RequestJson(value = "monitortime") String monitortime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");

            int actualMaximum = getActualMaximum(monitortime);

            /*paramMap.put("pollutanttype", WaterQualityEnum.getCode());
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);*/

            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "day");
            paramMap.put("starttime", monitortime + "-01");
            paramMap.put("endtime", monitortime + "-" + actualMaximum);
            List<Map> thisMonthwaterStationOnlineDatap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

            List<Map<String, Object>> thismonthmaps = standardReachingRate(outputs, thisMonthwaterStationOnlineDatap);


            String afterMonthStirng = getAfterMonthStirng(monitortime);
            int afterMonthActualMaximum = getActualMaximum(afterMonthStirng);
            paramMap.put("starttime", afterMonthStirng + "-01");
            paramMap.put("endtime", afterMonthStirng + "-" + afterMonthActualMaximum);

            List<Map> AfterMonthwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);
            List<Map<String, Object>> aftermonthmaps = standardReachingRate(outputs, AfterMonthwaterStationOnlineData);


            String afterYearStirng = getAfterYearStirng(monitortime);
            int afterYearActualMaximum = getActualMaximum(afterYearStirng);
            paramMap.put("starttime", afterYearStirng + "-01");
            paramMap.put("endtime", afterYearStirng + "-" + afterYearActualMaximum);

            List<Map> afterYearwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);
            List<Map<String, Object>> afteryearmaps = standardReachingRate(outputs, afterYearwaterStationOnlineData);

            for (Map<String, Object> output : outputs) {
                Map<String, Object> data = new HashMap<>();
                String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                String monitorpointname = output.get("monitorpointname") == null ? "" : output.get("monitorpointname").toString();
                thismonthmaps.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).peek(m -> data.put("thismonthrate", m.get("standardreachingrate"))).collect(Collectors.toList());
                aftermonthmaps.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).peek(m -> data.put("aftermonthrate", m.get("standardreachingrate"))).collect(Collectors.toList());
                afteryearmaps.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).peek(m -> data.put("afteryearrate", m.get("standardreachingrate"))).collect(Collectors.toList());


                //设置同比占比信息
                if (data.get("thismonthrate") == null) {
                    data.put("thismonthrate", "-");
                    //设置数据设置-
                    if (data.get("aftermonthrate") == null) {
                        data.put("aftermonthrate", "-");

                        data.put("yearonyear", "-");
                    } else {
                        data.put("yearonyear", "-");
                    }
                    //设置数据为-
                    if (data.get("afteryearrate") == null) {
                        data.put("afteryearrate", "-");
                        data.put("Proportion", "-");
                    } else {
                        data.put("Proportion", "-");
                    }
                } else {
                    Float thismonthrate = Float.valueOf(data.get("thismonthrate").toString());

                    if (data.get("aftermonthrate") == null) {
                        data.put("aftermonthrate", "-");
                        data.put("yearonyear", "-");
                    } else {
                        Float aftermonthrate = Float.valueOf(data.get("aftermonthrate").toString());
                        String yearonyear = decimalFormat.format((aftermonthrate - thismonthrate) / aftermonthrate * 100);
                        data.put("yearonyear", yearonyear);
                    }


                    if (data.get("afteryearrate") == null) {
                        data.put("afteryearrate", "-");
                        data.put("Proportion", "-");
                    } else {
                        Float afteryearrate = Float.valueOf(data.get("afteryearrate").toString());
                        String Proportion = decimalFormat.format((afteryearrate - thismonthrate) / afteryearrate * 100);
                        data.put("Proportion", Proportion);
                    }

                }

                data.put("dgimn", dgimn);
                data.put("thismonthtime", monitortime);
                data.put("aftermonthtime", afterMonthStirng);
                data.put("afteryeartime", afterYearStirng);
                data.put("monitorpointname", monitorpointname);
                resultList.add(data);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 1:53
     * @Description: 通过监测时间获取所有水质站点水质类别占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "getAllWaterQualityProportionByMonitorTime", method = RequestMethod.POST)
    public Object getAllWaterQualityProportionByMonitorTime(
            @RequestJson(value = "monitortime", required = false) String monitortime,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "dgimn", required = false) String dgimn

    ) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            String startT = "";
            String endT = "";
            if (StringUtils.isNotBlank(monitortime)) {
                int actualMaximum = getActualMaximum(monitortime + "-12");
                startT = monitortime + "-01-01";
                endT = monitortime + "-12-" + actualMaximum;
            } else if (StringUtils.isNotBlank(starttime) && StringUtils.isNotBlank(endtime)) {
                startT = DataFormatUtil.getFirstDayOfMonth(starttime);
                endT = DataFormatUtil.getLastDayOfMonth(endtime);
            }
            if (StringUtils.isBlank(startT)) {
                return "";
            }

            //获取所有mn号
            List<String> dgimns;
            if (StringUtils.isNotBlank(dgimn)) {
                dgimns = Arrays.asList(dgimn);
            } else {
                List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
                dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "day");
            paramMap.put("starttime", startT);
            paramMap.put("endtime", endT);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

            for (Map thisMonthwaterStationOnlineDatum : thisMonthwaterStationOnlineData) {
                String MonitorTime = thisMonthwaterStationOnlineDatum.get("MonitorTime").toString();
                thisMonthwaterStationOnlineDatum.put("MonitorTime", DataFormatUtil.formatCST(MonitorTime).substring(0, 7));
            }

            Map<String, List<Map>> collect = thisMonthwaterStationOnlineData.stream().filter(m -> m.get("WaterLevel") != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));

            for (String MonitorTime : collect.keySet()) {

                Map<String, Object> data = new HashMap<>();
                data.put("MonitorTime", MonitorTime);
                List<Map> list = collect.get(MonitorTime);

                Map<String, Long> waterLevel1 = list.stream().collect(Collectors.groupingBy(m -> m.get("WaterLevel").toString(), Collectors.counting()));

                Optional<Long> reduce = waterLevel1.values().stream().reduce(Long::sum);

                if (reduce.isPresent()) {
                    Float aLong = reduce.get().floatValue();
                    for (String o : waterLevel1.keySet()) {
                        data.put(o, decimalFormat.format(waterLevel1.get(o) / aLong * 100));
                    }
                }
                resultList.add(data);
            }
            if (resultList.size() > 0) {//排序
                resultList = resultList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("MonitorTime").toString())).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/14 0014 下午 1:24
     * @Description: 通过监测时间获取水质站点每个月水质i类别信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "getEveryMonthWaterQualityInfoByMonitorTime", method = RequestMethod.POST)
    public Object getEveryMonthWaterQualityInfoByMonitorTime(@RequestJson(value = "monitortime") String monitortime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            int actualMaximum = getActualMaximum(monitortime);


            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "day");
            paramMap.put("starttime", monitortime + "-01");
            paramMap.put("endtime", monitortime + "-" + actualMaximum);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);


            Map<String, List<Map>> collect = thisMonthwaterStationOnlineData.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> {
                try {
                    return DataFormatUtil.formatCST(m.get("MonitorTime").toString()).substring(0, 10);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return "";
            }));

            for (String MonitorTime : collect.keySet()) {

                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> datalist = new ArrayList<>();
                List<Map> list = collect.get(MonitorTime);
                list.stream().peek(m -> {
                    try {
                        Map<String, Object> hourdata = new HashMap<>();
                        hourdata.put("MonitorTime", DataFormatUtil.formatCST(m.get("MonitorTime").toString()).substring(0, 10));
                        hourdata.put("WaterLevel", m.get("WaterLevel"));
                        String dgimn = m.get("DataGatherCode") == null ? "" : m.get("DataGatherCode").toString();
                        outputs.stream().filter(n -> n.get("dgimn") != null && n.get("dgimn").toString().equals(dgimn)).forEach(n -> hourdata.put("monitorpointname", n.get("monitorpointname")));
                        datalist.add(hourdata);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }).sorted(Comparator.comparing(m -> m.get("MonitorTime").toString())).collect(Collectors.toList());

                data.put("Monitortime", MonitorTime);
                data.put("monitorpoint", datalist);
                resultList.add(data);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/4 0004 上午 10:04
     * @Description: 通过监测时间获取水质站点达标情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype, pollutantcode, dgimns]
     * @throws:
     */
    @RequestMapping(value = "getWaterQualityComplianceInfoByMonitorTime", method = RequestMethod.POST)
    public Object getWaterQualityComplianceInfoByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");

            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("datetype", "day");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);

            Map<String, List<Map>> collect = thisMonthwaterStationOnlineData.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String DataGatherCode : collect.keySet()) {
                List<Map> list = collect.get(DataGatherCode);
                Map<String, Object> data = new HashMap<>();
                Map<String, Object> output = outputs.stream().filter(m -> m.get("dgimn") != null && DataGatherCode.equals(m.get("dgimn").toString())).findFirst().orElse(new HashMap<>());
                String fk_funwaterqaulitycode = output.get("fk_funwaterqaulitycode") == null ? "" : output.get("fk_funwaterqaulitycode").toString();
                Integer funwaterqaulity = CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(fk_funwaterqaulitycode);
                //达标天数
                long qualified = list.stream().filter(m -> m.get("WaterLevel") != null && funwaterqaulity > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(m.get("WaterLevel").toString())).count();
                //有效天数
                long size = list.stream().filter(m -> m.get("WaterLevel") != null).count();

                //达标率
                double qualifiedrate = Double.valueOf(qualified) / Double.valueOf(size);

                data.put("qualified", qualified);
                data.put("Effective", size);//有效
                data.put("qualifiedrate", decimalFormat.format(qualifiedrate * 100));//有效率
                data.put("monitorpointname", output.get("monitorpointname"));


                resultList.add(data);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/6/4 0004 上午 10:04
     * @Description: 通过多参数获取水质站点监测均值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype, pollutantcode, dgimns]
     * @throws:
     */
    @RequestMapping(value = "getWaterQualityAvgStrengthByParams", method = RequestMethod.POST)
    public Object getWaterQualityAvgStrengthByParams(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "datetype") String datetype,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode,
                                                     @RequestJson(value = "dgimns") Object dgimns) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");

            paramMap.put("pollutanttype", WaterQualityEnum.getCode());
            List<Map<String, Object>> pollutants = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);
            paramMap.put("dgimns", dgimns);
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            paramMap.put("datetype", "day");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getWaterStationOnlineDataByParamMap(paramMap);


            //格式化日期
            for (Map<String, Object> map : thisMonthwaterStationOnlineData) {
                if ("week".equals(datetype) && map.get("MonitorTime") != null) {
                    String monitorTime = DataFormatUtil.formatCST(map.get("MonitorTime").toString()).substring(0, 10);
                    Object timeOfYMWByString = DataFormatUtil.getTimeOfYMWByString(monitorTime);
                    map.put("MonitorTime", timeOfYMWByString);
                } else if ("month".equals(datetype) && map.get("MonitorTime") != null) {
                    String monitorTime = DataFormatUtil.formatCST(map.get("MonitorTime").toString()).substring(0, 10);
                    Object timeOfYMWByString = DataFormatUtil.getTimeOfYMByString(monitorTime);
                    map.put("MonitorTime", timeOfYMWByString);
                } else if ("year".equals(datetype) && map.get("MonitorTime") != null) {
                    String monitorTime = DataFormatUtil.formatCST(map.get("MonitorTime").toString()).substring(0, 4);
                    map.put("MonitorTime", monitorTime + "年");
                }
            }

            Map<String, List<Map>> collect = thisMonthwaterStationOnlineData.stream().filter(m -> m.get("MonitorTime") != null && m.get("DayDataList") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));

            for (String MonitorTime : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map> list = collect.get(MonitorTime);
                Double AvgStrength = list.stream().flatMap(m -> ((List<Map<String, Object>>) m.get("DayDataList")).stream()).filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null
                        && pollutantcode.equals(m.get("PollutantCode").toString())).map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));
                String DataGatherCode = list.stream().filter(m -> m.get("DataGatherCode") != null).findFirst().orElse(new HashMap()).get("DataGatherCode").toString();
                String monitorpointname = outputs.stream().filter(m -> m.get("dgimn") != null && m.get("monitorpointname") != null && DataGatherCode.equals(m.get("dgimn").toString())).findFirst().orElse(new HashMap<>()).get("monitorpointname").toString();

                Map<String, Object> pollutant = pollutants.stream().filter(m -> m.get("pollutantcode") != null && pollutantcode.equals(m.get("pollutantcode").toString())).findFirst().orElse(new HashMap<>());

                data.put("AvgStrength", decimalFormat.format(AvgStrength));
                data.put("DataGatherCode", DataGatherCode);
                data.put("monitorpointname", monitorpointname);
                data.put("pollutantname", pollutant.get("pollutantname"));
                data.put("pollutantunit", pollutant.get("PollutantUnit"));
                data.put("monitortime", MonitorTime);
                resultList.add(data);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/4 0004 下午 4:44
     * @Description: 导出水质监测均值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype, pollutantcode, dgimns, request, response]
     * @throws:
     */
    @RequestMapping(value = "ExportWaterQualityAvgStrengthByParams", method = RequestMethod.POST)
    public void ExportWaterQualityAvgStrengthByParams(@RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      @RequestJson(value = "datetype") String datetype,
                                                      @RequestJson(value = "pollutantcode") String pollutantcode,
                                                      @RequestJson(value = "dgimns") Object dgimns, HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException {
        try {

            Object data = JSONObject.fromObject(getWaterQualityAvgStrengthByParams(starttime, endtime, datetype, pollutantcode, dgimns)).get("data");
            JSONArray jsonArray = JSONArray.fromObject(data);

            Object pollutantunit = ((Map<String, Object>) jsonArray.stream().filter(m -> ((Map<String, Object>) m).get("pollutantunit") != null).findFirst().orElse(new HashMap<>())).get("pollutantunit");

            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("水质站点");
            headers.add("监测时间");
            headers.add("监测因子");
            headers.add("监测值" + ("null".equals(pollutantunit.toString()) || pollutantcode == null ? "" : pollutantunit.toString()));
            headersField.add("monitorpointname");
            headersField.add("monitortime");
            headersField.add("pollutantname");
            headersField.add("avgstrength");
            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("水质站点监测数据", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    //计算各站点达标率
    public static List<Map<String, Object>> standardReachingRate(List<Map<String, Object>> outputs, List<Map> waterStationOnlineDataByParamMap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, List<Map>> collect = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        for (String DataGatherCode : collect.keySet()) {
            List<Map> maps = collect.get(DataGatherCode);
            Map<String, Object> data = new HashMap<>();
            Float dabiao = 0f;
            for (Map map : maps) {
                String WaterLevel = map.get("WaterLevel") == null ? "" : map.get("WaterLevel").toString();
                for (Map<String, Object> output : outputs) {

                    String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                    String FK_FunWaterQaulityCode = output.get("FK_FunWaterQaulityCode") == null ? "" : output.get("FK_FunWaterQaulityCode").toString();
                    //如果数据等级小于目标等级为达标数据
                    if (dgimn.equals(DataGatherCode) && CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) != -1 && CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(FK_FunWaterQaulityCode)) {
                        dabiao++;
                    }

                }
            }
            data.put("dgimn", DataGatherCode);
            data.put("standardreachingrate", decimalFormat.format(dabiao / maps.size() * 100));
            resultList.add(data);
        }
        return resultList;
    }

    //获取当前月份最大天数
    private int getActualMaximum(String monitortime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date parse = simpleDateFormat.parse(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        return instance.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    //获取上个月字符串
    private String getAfterMonthStirng(String monitortime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date parse = simpleDateFormat.parse(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.MONTH, -1);
        return simpleDateFormat.format(instance.getTime());
    }

    //获取去年字符串
    private String getAfterYearStirng(String monitortime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date parse = simpleDateFormat.parse(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.YEAR, -1);
        return simpleDateFormat.format(instance.getTime());
    }


    /**
     * @author: lip
     * @date: 2020/12/22 0022 下午 1:58
     * @Description: 获取地下水最新监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getLastGroundWaterData", method = RequestMethod.POST)
    public Object getLastGroundWaterData() {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> dataList = groundWaterService.getGroundWaterInfoByParamMap(paramMap);
            if (dataList.size() > 0) {
                List<String> mns = new ArrayList<>();
                Map<String, Integer> idAndTarLevel = new HashMap<>();
                Map<String, Object> idAndName = new HashMap<>();
                String mnCommon;
                String levelName;
                for (Map<String, Object> dataMap : dataList) {
                    mnCommon = dataMap.get("pkid").toString();
                    mns.add(mnCommon);
                    if (dataMap.get("waterqualityclassname") != null) {
                        levelName = dataMap.get("waterqualityclassname").toString();
                        idAndTarLevel.put(mnCommon, CommonTypeEnum.WaterLevelEnum.getObjectByName(levelName).getIndex());
                    }
                    idAndName.put(mnCommon, dataMap.get("MonitorPointName"));
                }
                List<AggregationOperation> aggregations = new ArrayList<>();

                Criteria criteria = new Criteria();
                criteria.and("DataGatherCode").in(mns);
                aggregations.add(match(criteria));
                aggregations.add(project("DataGatherCode", "MonitorTime", "WaterQualityClass"));
                aggregations.add(sort(Sort.Direction.DESC, "MonitorTime"));
                Aggregation aggregation = newAggregation(aggregations);
                AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, GroundWaterData_db, Document.class);
                List<Document> documents = results.getMappedResults();
                if (documents.size() > 0) {
                    Map<String, List<Date>> mnAndDateList = new HashMap<>();
                    List<Date> dateList;
                    Date time;
                    for (Document document : documents) {
                        mnCommon = document.getString("DataGatherCode");
                        time = document.getDate("MonitorTime");
                        if (mnAndDateList.containsKey(mnCommon)) {
                            dateList = mnAndDateList.get(mnCommon);
                        } else {
                            dateList = new ArrayList<>();
                        }
                        dateList.add(time);
                        mnAndDateList.put(mnCommon, dateList);


                    }
                    Map<String, Date> mnAndThisDate = new HashMap<>();
                    Map<String, Date> mnAndThatDate = new HashMap<>();
                    for (String mnIndex : mnAndDateList.keySet()) {
                        dateList = mnAndDateList.get(mnIndex);
                        if (dateList.size() > 1) {
                            mnAndThisDate.put(mnIndex, dateList.get(0));
                            mnAndThatDate.put(mnIndex, dateList.get(1));
                        } else {
                            mnAndThisDate.put(mnIndex, dateList.get(0));
                        }
                    }

                    Map<String, Integer> idAndThisLevel = new HashMap<>();
                    Map<String, Integer> idAndThatLevel = new HashMap<>();
                    for (Document document : documents) {
                        mnCommon = document.getString("DataGatherCode");
                        time = document.getDate("MonitorTime");
                        if (mnAndThisDate.containsKey(mnCommon)
                                && time.getTime() == mnAndThisDate.get(mnCommon).getTime()
                                && document.get("WaterQualityClass") != null) {
                            levelName = document.getString("WaterQualityClass");
                            if (StringUtils.isNotBlank(levelName)) {
                                idAndThisLevel.put(mnCommon, CommonTypeEnum.WaterLevelEnum.getObjectByName(levelName).getIndex());
                            }


                        }

                        if (mnAndThatDate.containsKey(mnCommon)
                                && time.getTime() == mnAndThatDate.get(mnCommon).getTime()
                                && document.get("WaterQualityClass") != null) {
                            levelName = document.getString("WaterQualityClass");

                            if (StringUtils.isNotBlank(levelName)) {
                                idAndThatLevel.put(mnCommon, CommonTypeEnum.WaterLevelEnum.getObjectByName(levelName).getIndex());
                            }

                        }
                    }
                    for (String idIndex : idAndName.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("monitorpointid", idIndex);
                        resultMap.put("monitorpointname", idAndName.get(idIndex));
                        if (mnAndThisDate.containsKey(idIndex)) {
                            resultMap.put("monitortime", DataFormatUtil.getDateYMD(mnAndThisDate.get(idIndex)));
                        } else {
                            resultMap.put("monitortime", "-");
                        }

                        if (idAndThisLevel.containsKey(idIndex)) {
                            resultMap.put("thiswaterqualityclass", CommonTypeEnum.WaterLevelEnum.getObjectByIndex(idAndThisLevel.get(idIndex)).getName());
                        } else {
                            resultMap.put("thiswaterqualityclass", "-");
                        }
                        if (idAndThatLevel.containsKey(idIndex)) {
                            resultMap.put("thatwaterqualityclass", CommonTypeEnum.WaterLevelEnum.getObjectByIndex(idAndThatLevel.get(idIndex)).getName());
                        } else {
                            resultMap.put("thatwaterqualityclass", "-");
                        }
                        if (idAndThatLevel.containsKey(idIndex) && idAndThisLevel.containsKey(idIndex)) {
                            if (idAndThatLevel.get(idIndex) > idAndThisLevel.get(idIndex)) {
                                resultMap.put("change", "up");
                            } else if (idAndThatLevel.get(idIndex) == idAndThisLevel.get(idIndex)) {
                                resultMap.put("change", "is");
                            } else {
                                resultMap.put("change", "down");
                            }
                        } else {
                            resultMap.put("change", "-");
                        }
                        if (idAndTarLevel.containsKey(idIndex) && idAndThisLevel.containsKey(idIndex)) {
                            if (idAndTarLevel.get(idIndex) >= idAndThisLevel.get(idIndex)) {
                                resultMap.put("isover", "false");
                            }
                        } else {
                            resultMap.put("isover", "true");
                        }
                        resultList.add(resultMap);
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
     * @Description: 获取自动站某小时时段达标率情况
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/9 15:36
     */
    @RequestMapping(value = "getOnlinePointEnvDataList", method = RequestMethod.POST)
    public Object getOnlinePointEnvDataList(@RequestJson(value = "monitortime") String monitortime) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> pointList = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            if (pointList.size() > 0) {
                //获取所有mn号
                List<String> dgimns = pointList.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
                Map<String, Object> idAndLevel = new HashMap<>();
                List<AggregationOperation> aggregations = new ArrayList<>();
                Criteria criteria = new Criteria();
                criteria.and("DataGatherCode").in(dgimns).and("MonitorTime")
                        .gte(DataFormatUtil.getDateYMD(monitortime))
                        .lte(DataFormatUtil.getDateYMD(monitortime));
                ;
                aggregations.add(match(criteria));
                aggregations.add(project("DataGatherCode", "MonitorTime", "WaterLevel"));
                aggregations.add(sort(Sort.Direction.DESC, "MonitorTime"));
                Aggregation aggregation = newAggregation(aggregations);
                AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
                List<Document> documents = results.getMappedResults();
                String id;
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        id = document.getString("DataGatherCode");
                        idAndLevel.put(id, document.get("WaterLevel") != null ? document.get("WaterLevel") + "类" : "-");
                    }
                }
                for (Map<String, Object> point : pointList) {
                    id = point.get("dgimn").toString();
                    point.put("targetlevel", point.get("waterqualityclassname"));
                    if (idAndLevel.get(id) != null) {
                        point.put("envlevel", idAndLevel.get(id));
                        point.put("envtime", DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd", "yyyy年MM月dd日"));
                    } else {
                        point.put("envlevel", "-");
                        point.put("envtime", "-");
                    }
                    resultList.add(point);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/16 0016 上午 10:41
     * @Description: 获取一段时间内水质优良情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime,  endtime]
     * @throws:
     */
    @RequestMapping(value = "getWaterQualityGoodSituationDataByParam", method = RequestMethod.POST)
    public Object getWaterQualityGoodSituationDataByParam(@RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "datetype") String datetype,
                                                          @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("datetype", datetype);
            paramMap.put("endtime", endtime);
            List<Map> waterStationOnlineDataByParamMap = onlineService.getWaterStationOnlineDataByParamMap(paramMap);
            if (waterStationOnlineDataByParamMap.size() == 0) {
                resultMap.put("badnum", 0);
                resultMap.put("excellentnum", 0);
                resultMap.put("total", 0);
                resultMap.put("excellentrate", 0);
                resultMap.put("badrate", 0);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            int total = 0;
            int good = 0;
            int bad = 0;
            for (Map map : waterStationOnlineDataByParamMap) {
                if (map.get("WaterLevel") != null) {
                    String WaterLevel = map.get("WaterLevel") == null ? "" : map.get("WaterLevel").toString();
                    total += 1;
                    //如果数据等级小于优劣等级  Ⅰ 、Ⅱ 、Ⅲ 为优  其它为劣
                    if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.QaulityThreeEnum.getCode()) {
                        good += 1;
                    } else {
                        bad += 1;
                    }
                }
            }
            if (total > 0) {
                resultMap.put("badnum", bad);
                resultMap.put("excellentnum", good);
                resultMap.put("total", total);
                resultMap.put("excellentrate", DataFormatUtil.SaveOneAndSubZero(100 * Double.valueOf(good) / Double.valueOf(total)));
                resultMap.put("badrate", DataFormatUtil.SaveOneAndSubZero(100 * Double.valueOf(bad) / Double.valueOf(total)));
            } else {
                resultMap.put("badnum", 0);
                resultMap.put("excellentnum", 0);
                resultMap.put("total", 0);
                resultMap.put("excellentrate", 0);
                resultMap.put("badrate", 0);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/15 0015 上午 9:46
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4 月数据-5）自定义参数获取单个水质站点单污染物列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, monitorpointid, pollutantcode,chartorlist:1图2列表， starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getOneWaterStationOnePollutantDataByParams", method = RequestMethod.POST)
    public Object getOneWaterStationOnePollutantDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "mn") String mn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "chartorlist") Integer chartorlist,
            @RequestJson(value = "targetlevel", required = false) String targetlevel,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            if (pagenum != null && pagesize != null) {
                paramMap.put("pagenum", pagenum);
                paramMap.put("pagesize", pagesize);
            }
            if (chartorlist == 1) { //图表
                paramMap.put("sort", "asc");
            } else if (chartorlist == 2) {//列表
                paramMap.put("sort", "desc");
            }
            if (targetlevel != null) {
                paramMap.put("targetlevel", "targetlevel");
            }
            paramMap.put("collection", collection);
            paramMap.put("datamark", datamark + "");
            paramMap.put("mn", mn);
            paramMap.put("pollutantcode", pollutantcode);
            if (pagesize==null||pagenum==null){
                Map<String,Object> countMap = new HashMap<>();
                countMap.put("starttime",starttime);
                countMap.put("endtime",endtime);
                countMap.put("collection",collection);
                countMap.put("mns",Arrays.asList(mn));
                if (!"waterquality".equals(pollutantcode)){
                    countMap.put("pollutantcodes",Arrays.asList(pollutantcode));
                }
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
            }
            dataMap = onlineWaterQualityService.getOneWaterStationOnePollutantDataByParams(paramMap);
            paramMap.clear();
            paramMap.put("dgimn", mn);
            List<Map<String, Object>> monitorInfo = waterStationService.getAllWaterStationInfoByParamMap(paramMap);
            dataMap.put("pointinfo", (monitorInfo != null && monitorInfo.size() > 0) ? monitorInfo.get(0) : null);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/15 0015 下午 14:51
     * @Description: 获取水质污染物报警排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getWaterStationPollutantAlarmRankDataByParams", method = RequestMethod.POST)
    public Object getWaterStationPollutantAlarmRankDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "mn") String mn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("datamark", datamark + "");
            paramMap.put("mn", mn);
            List<Map<String, Object>> result = onlineWaterQualityService.getWaterStationPollutantAlarmRankDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取水质监测点当前、同比、环比分析数据
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/06/16 9:28
     */
    @RequestMapping(value = "getWaterStationContrastDataByParam", method = RequestMethod.POST)
    public Object getWaterStationContrastDataByParam(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "datatype") String datatype,
                                                     @RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode
    ) throws Exception {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            //当前时间
            Map<String, Object> paramMap = new HashMap<>();
            String hb_starttime = "";
            String hb_endtime = "";
            String tb_starttime = "";
            String tb_endtime = "";
            List<String> times = new ArrayList<>();
            List<String> tb_times = new ArrayList<>();
            List<String> hb_times = new ArrayList<>();
            if ("hour".equals(datatype)) {//小时
                hb_starttime = DataFormatUtil.getBeforeByHourTime(1, starttime);
                hb_endtime = DataFormatUtil.getBeforeByHourTime(1, endtime);
                tb_starttime = DataFormatUtil.getHourTBDate(starttime);
                tb_endtime = DataFormatUtil.getHourTBDate(endtime);
                times = DataFormatUtil.getYMDHBetween(starttime, endtime);
                times.add(endtime);
                tb_times = DataFormatUtil.getYMDHBetween(tb_starttime, tb_endtime);
                tb_times.add(tb_endtime);
                hb_times = DataFormatUtil.getYMDHBetween(hb_starttime, hb_endtime);
                hb_times.add(hb_endtime);
            } else if ("day".equals(datatype)) {//日
                hb_starttime = DataFormatUtil.getBeforeByDayTime(1, starttime);
                hb_endtime = DataFormatUtil.getBeforeByDayTime(1, endtime);
                tb_starttime = DataFormatUtil.getDayTBDate(starttime);
                tb_endtime = DataFormatUtil.getDayTBDate(endtime);
                times = DataFormatUtil.getYMDBetween(starttime, endtime);
                times.add(endtime);
                tb_times = DataFormatUtil.getYMDBetween(tb_starttime, tb_endtime);
                tb_times.add(tb_endtime);
                hb_times = DataFormatUtil.getYMDBetween(hb_starttime, hb_endtime);
                hb_times.add(hb_endtime);
            } else if ("month".equals(datatype)) {//月
                hb_starttime = DataFormatUtil.getBeforeByMonthTime(1, starttime);
                hb_endtime = DataFormatUtil.getBeforeByMonthTime(1, endtime);
                tb_starttime = DataFormatUtil.getMonthTBDate(starttime);
                tb_endtime = DataFormatUtil.getMonthTBDate(endtime);
                times = DataFormatUtil.getMonthBetween(starttime, endtime);
                times.add(endtime);
                tb_times = DataFormatUtil.getMonthBetween(tb_starttime, tb_endtime);
                tb_times.add(tb_endtime);
                hb_times = DataFormatUtil.getMonthBetween(hb_starttime, hb_endtime);
                hb_times.add(hb_endtime);
            }
            paramMap.put("dgimn", dgimn);
            paramMap.put("datatype", datatype);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //当前污染物浓度趋势
            List<Document> xz_list = onlineWaterQualityService.getWaterStationContrastDataByParam(paramMap);
            //同比
            paramMap.put("starttime", tb_starttime);
            paramMap.put("endtime", tb_endtime);
            List<Document> tb_list = onlineWaterQualityService.getWaterStationContrastDataByParam(paramMap);
            //环比
            paramMap.put("starttime", hb_starttime);
            paramMap.put("endtime", hb_endtime);
            List<Document> hb_list = onlineWaterQualityService.getWaterStationContrastDataByParam(paramMap);
            for (int i = 0; i < times.size(); i++) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("monitortime", times.get(i));
                onemap.put("value", getPollutantValue(times.get(i), xz_list, pollutantcode));
                onemap.put("tb_value", getPollutantValue(tb_times.get(i), tb_list, pollutantcode));
                onemap.put("hb_value", getPollutantValue(hb_times.get(i), hb_list, pollutantcode));
                resultList.add(onemap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getPollutantValue(String time, List<Document> list, String pollutantcode) {
        String value = "";
        if (list != null && list.size() > 0) {
            for (Document document : list) {
                if (time.equals(document.getString("MonitorTime"))) {
                    if ("waterquality".equals(pollutantcode)) {
                        value = document.get("WaterLevel") != null ? document.getString("WaterLevel") : "";
                    } else {
                        value = document.get("value") != null ? DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(document.get("value").toString())) : "";
                    }
                    break;
                }
            }
        }
        return value;
    }

    /**
     * @author: xsm
     * @date: 2022/06/16 0016 下午 15:17
     * @Description: 通过自定义参数获取多个水质站点单污染物图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getManyWaterStationOnePollutantDataByParams", method = RequestMethod.POST)
    public Object getManyWaterStationOnePollutantDataByParams(
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "chartorlist") Integer chartorlist,
            @RequestJson(value = "dgimns", required = false) List<String> dgimns,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            //获取所有水质站点信息
            paramMap.put("dgimns", dgimns);
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            dgimns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            for (Map<String, Object> map : outputs) {
                if (map.get("dgimn") != null) {
                    dgimns.add(map.get("dgimn").toString());
                    mnandname.put(map.get("dgimn").toString(), map.get("outputname"));
                }
            }
            if (chartorlist == 1) { //图表
                paramMap.put("sort", "asc");
            } else if (chartorlist == 2) {//列表
                paramMap.put("sort", "desc");
            }
            paramMap.put("datatype", datatype);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("dgimns", dgimns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> result = onlineWaterQualityService.getManyWaterStationOnePollutantDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/17 0017 下午 14:14
     * @Description: 通过自定义参数获取多个水质站点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:1 浓度对比
     * @throws:
     */
    @RequestMapping(value = "getManyWaterStationPollutantChangeDataByParams", method = RequestMethod.POST)
    public Object getManyWaterStationPollutantChangeDataByParams(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            //获取所有水质站点信息
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            for (Map<String, Object> map : outputs) {
                if (map.get("dgimn") != null) {
                    dgimns.add(map.get("dgimn").toString());
                    mnandname.put(map.get("dgimn").toString(), map.get("outputname"));
                }
            }
            //获取所有水质级别等级
            List<Map<String, Object>> qaulitylist = onlineWaterQualityService.getAllWaterQualityLevelData();
            Map<String, Object> levelandname = new HashMap<>();
            for (Map<String, Object> map : qaulitylist) {
                if (map.get("code") != null && map.get("levelnum") != null) {
                    levelandname.put(map.get("code").toString(), map.get("name"));
                }
            }
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("dgimns", dgimns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("monitortime", monitortime);
            List<Document> xz_result = onlineWaterQualityService.getManyWaterStationPollutantChangeDataByParams(paramMap);
            //环比前一天数据
            String startTime = DataFormatUtil.getBeforeByDayTime(1, monitortime);
            paramMap.put("monitortime", startTime);
            List<Document> hb_result = onlineWaterQualityService.getManyWaterStationPollutantChangeDataByParams(paramMap);
            for (String mn : dgimns) {
                List<Document> xz_list = new ArrayList<>();
                List<Document> hb_list = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("pointname", mnandname.get(mn));
                if (xz_result != null && xz_result.size() > 0) {
                    for (Document xz_doc : xz_result) {
                        if (mn.equals(xz_doc.getString("_id"))) {
                            xz_list = (List<Document>) xz_doc.get("valuelist");
                        }
                    }
                }
                if (hb_result != null && hb_result.size() > 0) {
                    for (Document hb_doc : hb_result) {
                        if (mn.equals(hb_doc.getString("_id"))) {
                            hb_list = (List<Document>) hb_doc.get("valuelist");
                        }
                    }
                }
                setWaterStationConcentrationData(map, xz_list, hb_list, monitortime, startTime, pollutantcode, levelandname);
                result.add(map);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取小时数据（监测值、水质类别、目标水质、标准值（标准表））
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/10/26 9:56
     */
    @RequestMapping(value = "getOnePointHourDataByParam", method = RequestMethod.POST)
    public Object getOnePointHourDataByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "monitorpointid") String monitorpointid) {
        try {

            Map<String, Object> resultMap = new HashMap<>();

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //点位set污染物
            paramMap.put("monitorpointid", monitorpointid);
            List<Map<String, Object>> setList = onlineWaterQualityService.getPollutantSetDataListByParam(paramMap);
            String levelCode = "";
            String thislevel = "";
            Map<String, Object> codeAndJudge = new HashMap<>();
            if (setList.size() > 0) {
                //因子标准值
                String pollutantCode;
                List<String> pollutantcodes = setList.stream().map(m -> m.get("code").toString()).collect(Collectors.toList());
                Object targetLevel = setList.get(0).get("fk_funwaterqaulitycode");
                if (targetLevel != null) {
                    levelCode = targetLevel+"类";
                    String standardValue;
                    if (CommonTypeEnum.WaterLevelEnum.getObjectByName(levelCode) != null) {
                        int indexLevel = CommonTypeEnum.WaterLevelEnum.getObjectByName(levelCode).getIndex();
                        paramMap.clear();
                        paramMap.put("levelcode", indexLevel);
                        paramMap.put("watertype", 1);//地表水
                        paramMap.put("pollutantcodes", pollutantcodes);
                        List<Map<String, Object>> pubStandList = onlineWaterQualityService.getPubStandardListByParam(paramMap);
                        for (Map<String, Object> pub : pubStandList) {
                            if (pub.get("judgementtype") != null&&pub.get("standardvalue")!=null) {
                                pollutantCode = pub.get("fk_pollutantcode").toString();
                                standardValue = setStandardValue(pub.get("judgementtype").toString());
                                codeAndJudge.put(pollutantCode,standardValue+pub.get("standardvalue"));
                            }
                        }
                    }
                }
                //监测值
                paramMap.put("dgimns", Arrays.asList(dgimn));
                paramMap.put("starttime", monitortime + ":00:00");
                paramMap.put("endtime", monitortime + ":59:59");
                paramMap.put("pollutantcodes", pollutantcodes);

                List<Document> documents = onlineWaterQualityService.getHourMonitorDataByParam(paramMap);
                Map<String, Map<String, Object>> codeAndMap = getCodeAndMap(documents);
                thislevel = documents.size() > 0 ? documents.get(0).getString("PWaterLevel")+"类" : "";
                for (Map<String, Object> setData : setList) {
                    Map<String, Object> dataMap = new HashMap<>();
                    pollutantCode = setData.get("code").toString();
                    dataMap.put("pollutantcode", pollutantCode);
                    dataMap.put("pollutantname", setData.get("name"));
                    if (codeAndMap.containsKey(pollutantCode)) {
                        dataMap.putAll(codeAndMap.get(pollutantCode));
                    } else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("monitorvalue", "");
                        map.put("pollutantlevel", "");
                        dataMap.putAll(map);
                    }
                    if (setData.get("name").toString().toLowerCase().contains("ph")){
                        dataMap.put("standardvalue", "6-9");
                    }else {
                        dataMap.put("standardvalue", codeAndJudge.get(pollutantCode) != null ? codeAndJudge.get(pollutantCode) : "");
                    }
                    dataList.add(dataMap);
                }
            }

            resultMap.put("targetlevel", levelCode);
            resultMap.put("thislevel", thislevel);
            resultMap.put("datalist", dataList);

            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String setStandardValue(String judgementtype) {
        judgementtype = judgementtype.replaceAll("x<", "<")
                .replaceAll("x>", ">")
                .replaceAll("<=", "≤")
                .replaceAll(">=", "≥");
        return judgementtype;
    }

    private Map<String, Map<String, Object>> getCodeAndMap(List<Document> documents) {
        Map<String, Map<String, Object>> codeAndMap = new HashMap<>();
        for (Document document : documents) {
            Map<String, Object> map = new HashMap<>();
            map.put("monitorvalue", document.get("value"));
            map.put("pollutantlevel", document.get("WaterLevel"));
            codeAndMap.put(document.getString("code"), map);
        }
        return codeAndMap;

    }

    /**
     * Set水质站点污染物浓度环比对比数据
     */
    private void setWaterStationConcentrationData(Map<String, Object> map, List<Document> xz_list, List<Document> hb_list, String dq_time, String hb_time, String pollutantcode, Map<String, Object> levelandname) {
        String xz_value;
        String hb_value;
        String hour;
        for (int i = 0; i < 24; i++) {
            map.put("value_" + i, "");
            if (!"waterquality".equals(pollutantcode)) {
                map.put("level_" + i, "Ⅰ类");
            }
            xz_value = "";
            hb_value = "";
            if (i < 10) {
                hour = "0" + i;
            } else {
                hour = i + "";
            }
            for (Document document : xz_list) {
                if ((dq_time + " " + hour).equals(document.getString("monitortime"))) {
                    if ("waterquality".equals(pollutantcode)) {
                        xz_value = document.getString("waterlevel");
                    } else {
                        xz_value = document.getString("value");
                        if (document.get("waterlevel") != null) {
                            map.put("level_" + i, document.getString("waterlevel"));
                        }
                    }
                    break;
                }
            }
            for (Document document : hb_list) {
                if ((hb_time + " " + hour).equals(document.getString("monitortime"))) {
                    if ("waterquality".equals(pollutantcode)) {
                        hb_value = document.getString("waterlevel");
                    } else {
                        hb_value = document.getString("value");
                    }
                    break;
                }
            }
            //比较值 判断 上升还是下降
            //当时水质类别
            if ("waterquality".equals(pollutantcode)) {
                if (!"".equals(xz_value)) {
                    if (!"".equals(hb_value)) {
                        if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(xz_value) == CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(hb_value)) {
                            map.put("value_" + i, levelandname.get(xz_value));
                        } else if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(xz_value) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(hb_value)) {
                            map.put("value_" + i, levelandname.get(xz_value) + "↑");
                        } else {
                            map.put("value_" + i, levelandname.get(xz_value) + "↓");
                        }
                    } else {
                        map.put("value_" + i, levelandname.get(xz_value));
                    }
                }
            } else {
                if (!"".equals(xz_value)) {
                    if (!"".equals(hb_value)) {
                        double num = Double.valueOf(xz_value) - Double.valueOf(hb_value);
                        if (num == 0) {
                            map.put("value_" + i, xz_value);
                        } else if (num > 0) {
                            map.put("value_" + i, xz_value + "↑");
                        } else {
                            map.put("value_" + i, xz_value + "↓");
                        }
                    } else {
                        map.put("value_" + i, xz_value);
                    }
                }
            }
        }
    }


    /**
     * @author: xsm
     * @date: 2022/06/17 0017 下午 14:14
     * @Description: 综合分析-水质评价（APP）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:1
     * @throws:
     */
    @RequestMapping(value = "getWaterQualityAssessmentDataByParams", method = RequestMethod.POST)
    public Object getWaterQualityAssessmentDataByParams(
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "monitortime") String monitortime
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            //获取所有水质站点信息
            List<Map<String, Object>> outputs = onlineWaterQualityService.getWaterQualityStationByParamMap(paramMap);
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            Map<String, Object> mnandlevel = new HashMap<>();
            for (Map<String, Object> map : outputs) {
                if (map.get("dgimn") != null) {
                    dgimns.add(map.get("dgimn").toString());
                    mnandname.put(map.get("dgimn").toString(), map.get("outputname"));
                    mnandlevel.put(map.get("dgimn").toString(), map.get("fk_funwaterqaulitycode"));
                }
            }
            //获取所有水质级别等级
            List<Map<String, Object>> qaulitylist = onlineWaterQualityService.getAllWaterQualityLevelData();
            Map<String, Object> levelandname = new HashMap<>();
            for (Map<String, Object> map : qaulitylist) {
                if (map.get("code") != null && map.get("levelnum") != null) {
                    levelandname.put(map.get("code").toString(), map.get("name"));
                }
            }
            //获取主要污染物
            List<String> pocodes = new ArrayList<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
            List<Map<String, Object>> keyPollutants = onlineService.getKeyPollutantsByParam(paramMap);
            Map<String, Object> codeAndName = new HashMap<>();
            for (Map<String, Object> keyPollutant : keyPollutants) {
                if (keyPollutant.get("Code") != null) {
                    pocodes.add(keyPollutant.get("Code").toString());
                    codeAndName.put(keyPollutant.get("Code").toString(), keyPollutant.get("Name"));
                }
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("datatype", datatype);
            paramMap.put("monitortime", monitortime);
            List<Document> datalist = onlineWaterQualityService.getWaterQualityAssessmentDataByParams(paramMap);
            for (String mn : dgimns) {
                Map<String, Object> map = new HashMap<>();
                List<Document> podocuments = new ArrayList<>();
                map.put("dgimn", mn);
                map.put("pointname", mnandname.get(mn));
                boolean iover = false;
                String overstr = "";
                String level = "";
                String levelnum = mnandlevel.get(mn).toString();
                if (datalist != null && datalist.size() > 0) {
                    for (Document document : datalist) {
                        if (mn.equals(document.getString("DataGatherCode"))) {
                            level = document.getString("WaterLevel");
                            if ("hour".equals(datatype)) {
                                podocuments = (List<Document>) document.get("HourDataList");
                            } else if ("day".equals(datatype)) {
                                podocuments = (List<Document>) document.get("DayDataList");
                            }
                            break;
                        }
                    }
                }
                if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(level) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(levelnum)) {
                    iover = true;
                }
                for (String code : pocodes) {
                    map.put(code, "");
                    if (podocuments != null && podocuments.size() > 0) {
                        for (Document document : podocuments) {
                            if (code.equals(document.getString("PollutantCode"))) {
                                map.put(code, DataFormatUtil.subZeroAndDot(document.getString("AvgStrength")));
                                if (document.get("WaterLevel") != null) {
                                    if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(document.get("WaterLevel").toString()) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(levelnum)) {
                                        //大于
                                        //判断超标污染物及超标倍数
                                        overstr = codeAndName.get(code) + "(" + document.getDouble("OverMultiple") + ")" + "、";
                                    }
                                }
                            }
                        }
                    }
                }
                if (!"".equals(overstr)) {
                    overstr = overstr.substring(0, overstr.length() - 1);
                }
                map.put("overstr", overstr);
                if (iover) {
                    map.put("compliance", "超标");
                } else {
                    map.put("compliance", "达标");
                }
                map.put("targetlevel", levelandname.get(levelnum));
                map.put("waterlevel", levelandname.get(level));
                result.add(map);
            }
            List<Map<String, Object>> tabletitledata = getWaterQualityAssessmentTableTitle(keyPollutants);
            resultMap.put("tablelistdata", result);// 数据
            resultMap.put("tabletitledata", tabletitledata);// 表头
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/18 10:06
     * @Description: 表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getWaterQualityAssessmentTableTitle(List<Map<String, Object>> pollutantsInfo) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("headeralign", "center");
        map1.put("minwidth", "180px");
        map1.put("showhide", true);
        map1.put("prop", "pointname");
        map1.put("label", "站点名称");
        map1.put("align", "center");
        result.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("headeralign", "center");
        map2.put("minwidth", "180px");
        map2.put("showhide", true);
        map2.put("prop", "waterlevel");
        map2.put("label", "水质类别");
        map2.put("align", "center");
        result.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("headeralign", "center");
        map3.put("minwidth", "180px");
        map3.put("showhide", true);
        map3.put("prop", "targetlevel");
        map3.put("label", "目标水质");
        map3.put("align", "center");
        result.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("headeralign", "center");
        map4.put("minwidth", "180px");
        map4.put("showhide", true);
        map4.put("prop", "compliance");
        map4.put("label", "达标情况");
        map4.put("align", "center");
        result.add(map4);

        Map<String, Object> map5 = new HashMap<>();
        map5.put("headeralign", "center");
        map5.put("minwidth", "180px");
        map5.put("showhide", true);
        map5.put("prop", "overstr");
        map5.put("label", "主要污染指标(超标倍数)");
        map5.put("align", "center");
        result.add(map5);

        for (Map<String, Object> poc : pollutantsInfo) {
            Map<String, Object> pomap = new HashMap<>();
            pomap.put("headeralign", "center");
            pomap.put("minwidth", "180px");
            pomap.put("showhide", true);
            pomap.put("prop", poc.get("Code") + (poc.get("PollutantUnit") != null ? poc.get("Code").toString() : ""));
            pomap.put("label", poc.get("Name"));
            pomap.put("align", "center");
            result.add(pomap);
        }
        return result;
    }
}
