package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.common.pubcode.MonitorPointCommonService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.petition.PetitionInfoService;
import com.tjpu.sp.service.environmentalprotection.similarityanalysis.SimilarityAnalysisService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;

/**
 * @author: zhangzc
 * @date: 2019/5/28 15:50
 * @Description: 在线恶臭监测数据
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineStink")
public class OnlineStinkController {

    @Autowired
    private PetitionInfoService petitionInfoService;
    @Autowired
    private SimilarityAnalysisService similarityAnalysisService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private MonitorPointCommonService monitorPointCommonService;


    private final OtherMonitorPointService otherMonitorPointService;
    private final OnlineService onlineService;
    private final OnlineMonitorService onlineMonitorService;

    public OnlineStinkController(OtherMonitorPointService otherMonitorPointService, OnlineService onlineService, OnlineMonitorService onlineMonitorService) {
        this.otherMonitorPointService = otherMonitorPointService;
        this.onlineService = onlineService;
        this.onlineMonitorService = onlineMonitorService;
    }

    /**
     * 厂界恶臭监测点类型编码
     **/
    private final int entMonitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode();
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode();

    /**
     * @author: lip
     * @date: 2020/11/30 0030 上午 9:33
     * @Description: 实时数据一览（环境恶臭、厂界恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkRealTimeDataByParamMap", method = RequestMethod.POST)
    public Object getStinkRealTimeDataByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                paramMap.put("monitortypecode", monitorPointTypeCode);
                paramMap.put("entmonitortypecode", entMonitorPointTypeCode);
                paramMap.put("dgimns", dgimns);
                onlineOutPuts = otherMonitorPointService.getStinkPointDataByParamMap(paramMap);
            } else {
                onlineOutPuts = new ArrayList<>();
            }
            Map<String, Object> resultMap = onlineMonitorService.getOutPutLastDataByParamMap(onlineOutPuts, entMonitorPointTypeCode, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询图表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyMonitorCharDataByParamMap", method = RequestMethod.POST)
    public Object getManyMonitorCharDataByParamMap(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {

        try {
            List<Map<String, Object>> charDataList = new ArrayList<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitortypecode", monitorPointTypeCode);
                paramMap.put("entmonitortypecode", entMonitorPointTypeCode);
                paramMap.put("outputids", outputids);

                List<Map<String, Object>> pointDataList = otherMonitorPointService.getStinkPointDataByParamMap(paramMap);
                List<String> mns = new ArrayList<>();
                Map<String, String> idAndName = new HashMap<>();
                Map<String, String> outPutIdAndMn = new HashMap<>();
                String outputid;
                String name;
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("dgimn") != null) {
                        mns.add(pointData.get("dgimn").toString());
                        name = "";
                        outputid = pointData.get("pk_id").toString();
                        if (pointData.get("shortername") != null) {
                            name = pointData.get("shortername") + "-";
                        }
                        name += pointData.get("monitorpointname");
                        idAndName.put(outputid, name);
                        outPutIdAndMn.put(outputid, pointData.get("dgimn").toString());
                    }

                }
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


                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> pollutants = new ArrayList<>();
                Map<String, String> codeAndName = new HashMap<>();
                if (documents.size() > 0) {
                    paramMap.clear();
                    paramMap.put("codes", pollutantcodes);
                    pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
                    for (Map<String, Object> pollutant : pollutants) {
                        codeAndName.put(pollutant.get("code").toString(), pollutant.get("name").toString());
                    }
                }
                charDataList = MongoDataUtils.setManySomkeOutPutManyPollutantsCharDataList(documents, pollutants,
                        collection, outPutIdAndMn, idAndName, codeAndName);

            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询列表初始化页面数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyListPageDataByParams", method = RequestMethod.POST)
    public Object getManyListPageDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineMonitorService.getTableTitleForReport(titleMap);
            Map<String, Object> tabledata = new HashMap<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitortypecode", monitorPointTypeCode);
                paramMap.put("entmonitortypecode", entMonitorPointTypeCode);
                paramMap.put("outputids", outputids);

                List<Map<String, Object>> pointDataList = otherMonitorPointService.getStinkPointDataByParamMap(paramMap);
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("dgimn") != null) {
                        mns.add(pointData.get("dgimn").toString());
                    }
                }
                paramMap.putAll(titleMap);
                paramMap.put("mns", mns);
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
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                List<Map<String, Object>> tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
                tabledata.put("tablelistdata", tableListData);
                Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
            tabledata.put("tabletitledata", tabletitledata);
            dataMap.put("tabledata", tabledata);


            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 导出列表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyOutPutReport", method = RequestMethod.POST)
    public void exportManyOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineMonitorService.getTableTitleForReport(titleMap);
            List<Map<String, Object>> tableListData = new ArrayList<>();
            //获取表格数据
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitortypecode", monitorPointTypeCode);
                paramMap.put("entmonitortypecode", entMonitorPointTypeCode);
                paramMap.put("outputids", outputids);
                List<Map<String, Object>> pointDataList = otherMonitorPointService.getStinkPointDataByParamMap(paramMap);
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("dgimn") != null) {
                        mns.add(pointData.get("dgimn").toString());
                    }
                }
                paramMap.putAll(titleMap);
                paramMap.put("mns", mns);
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
                tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
            }
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "恶臭监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 7:09
     * @Description: 获取厂界恶臭污染物超标、预警、异常统计页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEntStinkPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getEntStinkPollutantEarlyAndOverAlarmsByParamMap(
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
            //排口数据
            paramMap.put("monitortypecode", monitorPointTypeCode);
            paramMap.put("entmonitortypecode", entMonitorPointTypeCode);
            List<Map<String, Object>> pointDataList = otherMonitorPointService.getStinkPointDataByParamMap(paramMap);
            paramMap.put("pointtype", entMonitorPointTypeCode);
            Map<String, Object> resultMap = onlineService.getPollutantEarlyAndOverAlarmsByParamMap(pageNum, pageSize, pointDataList, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }



    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取VOC站点监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkMonitorDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getStinkMonitorDataByDataMarkAndParams(
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
     * @author: lip
     * @date: 2019/5/27 0027 下午 7:21
     * @Description: 转换mongodb数据成图表格式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
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
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个空气站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneStinkListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneStinkListPageDataByDataMarkAndParams(
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
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个空气站点列表内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneStinkListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneStinkListDataByDataMarkAndParams(
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
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.put("outputids", outputids);
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
            paramMap.put("reportType", 1);
            paramMap.put("pointType", monitorPointTypeCode);
            paramMap.put("pollutantType", monitorPointTypeCode);
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
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多个空气站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyStinkListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyStinkListPageDataByDataMarkAndParams(
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
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多个空气站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyStinkListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyStinkListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointids") List<String> monitorpointids,

            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Integer reportType = 2;
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(monitorpointids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.put("outputids", monitorpointids);
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
            paramMap.put("reportType", reportType);
            paramMap.put("pointType", monitorPointTypeCode);
            paramMap.put("pollutantType", monitorPointTypeCode);
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
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个恶臭站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneStinkOutPutReport", method = RequestMethod.POST)
    public void exportOneStinkOutPutReport(
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
            String fileName = "恶臭监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个恶臭站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyStinkOutPutReport", method = RequestMethod.POST)
    public void exportManyStinkOutPutReport(
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
            String fileName = "恶臭监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: zhangzc
     * @date: 2019/5/30 10:51
     * @Description: 根据污染物编码获取各VOC监测点近24小时该污染物浓度数据
     * @param: code 污染物编码
     * @return:
     */
    @RequestMapping(value = "get24HourMonitorDataByPollutantCodeForStink", method = RequestMethod.POST)
    public Object get24HourMonitorDataByPollutantCodeForStink(@RequestJson(value = "code") String code,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime
    ) {
        try {

            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> map = onlineService.get24HourMonitorDataByPollutantCodeForMonitorPoint(code, monitorPointTypeCode, starttime, endtime, userid);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/7/7 10:10
     * @Description: 根据污染物编码和监测类型获取该类型监测点（恶臭、Voc、扬尘）近24小时该污染物浓度数据
     * @param: code 污染物编码
     * @return:
     */
    @RequestMapping(value = "get24HourMonitorDataByPollutantCodeAndMonitorType", method = RequestMethod.POST)
    public Object get24HourMonitorDataByPollutantCodeAndMonitorType(@RequestJson(value = "code") String code,
                                                                    @RequestJson(value = "monitortype") Integer monitortype,
                                                                    @RequestJson(value = "starttime") String starttime,
                                                                    @RequestJson(value = "endtime") String endtime
    ) {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> map = onlineService.get24HourMonitorDataByPollutantCodeAndMonitorType(code, monitortype, starttime, endtime, userid);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:24
     * @Description: 获取恶臭污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getStinkPollutantEarlyAndOverAlarmsByParamMap(
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
            paramMap.put("monitorPointType", monitorPointTypeCode);
            List<Map<String, Object>> outputs = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
            paramMap.put("pointtype", monitorPointTypeCode);
            Map<String, Object> resultMap = onlineService.getPollutantEarlyAndOverAlarmsByParamMap(pageNum, pageSize, outputs, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 4:52
     * @Description: 获取恶臭污染物预警/超标/异常详情页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getStinkPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getStinkPollutantEarlyOrOverOrExceptionDetailsPage(
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
            //根据统计类型获取表头数据
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
     * @author: lip
     * @date: 2019/6/13 0013 下午 4:52
     * @Description: 导出废水污染物预警/超标/异常详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "exportStinkPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportStinkPollutantEarlyOrOverOrExceptionDetailsData(
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
            //根据统计类型获取表头数据
            List<Map<String, Object>> tabletitledata = onlineService.getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(isoverstandard, counttype, pollutantcodes, monitorPointTypeCode);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "恶臭污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取恶臭多点位多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getStinkManyOutPutManyPollutantMonitorDataByParams(
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
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询恶臭污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getStinkPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("monitorPointType", monitorPointTypeCode);
            List<Map<String, Object>> outPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
            if (paramMap.get("starttime") != null && StringUtils.isNotBlank(paramMap.get("starttime").toString())) {
                //多查询4个小时数据
                String startdate = paramMap.get("starttime").toString() + " 00";
                paramMap.put("startdate", startdate);
                String starttime = DataFormatUtil.getBeforeByHourTime(4, startdate);
                starttime = MongoDataUtils.setStartTimeByDataMark(datatype, starttime);
                paramMap.put("starttime", starttime);
            }
            if (paramMap.get("endtime") != null && StringUtils.isNotBlank(paramMap.get("endtime").toString())) {
                String endtime = MongoDataUtils.setEndTimeByDataMark(datatype, paramMap.get("endtime").toString() + " 23");
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            resultMap = onlineService.getPollutantUpRushDataByParams(outPuts, monitorPointTypeCode, paramMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: chengzq
     * @date: 2020/12/4 0004 下午 5:21
     * @Description:  自定义查询条件查询所有恶臭污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "getAllStinkPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getAllStinkPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            List<Integer> monitorpointtypes = (List<Integer>)paramMap.get("monitorpointtypes");
            List<Map<String, Object>> outPuts=new ArrayList<>();

            if(monitorpointtypes.contains(EnvironmentalStinkEnum.getCode())){
                paramMap.put("monitorPointType", monitorPointTypeCode);
                outPuts .addAll( otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap));
            }
            if(monitorpointtypes.contains(FactoryBoundaryStinkEnum.getCode())){
                paramMap.put("monitorpointtype", FactoryBoundaryStinkEnum.getCode());
                outPuts.addAll(outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap));
            }


            if (paramMap.get("starttime") != null && StringUtils.isNotBlank(paramMap.get("starttime").toString())) {
                //多查询4个小时数据
                String startdate = paramMap.get("starttime").toString() + " 00";
                paramMap.put("startdate", startdate);
                String starttime = DataFormatUtil.getBeforeByHourTime(4, startdate);
                starttime = MongoDataUtils.setStartTimeByDataMark(datatype, starttime);
                paramMap.put("starttime", starttime);
            }
            if (paramMap.get("endtime") != null && StringUtils.isNotBlank(paramMap.get("endtime").toString())) {
                String endtime = MongoDataUtils.setEndTimeByDataMark(datatype, paramMap.get("endtime").toString() + " 23");
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            resultMap = onlineService.getPollutantUpRushDataByParams(outPuts, StinkEnum.getCode(), paramMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询恶臭污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportStinkPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportStinkPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("monitorPointType", monitorPointTypeCode);
            List<Map<String, Object>> outPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
            if (paramMap.get("starttime") != null && StringUtils.isNotBlank(paramMap.get("starttime").toString())) {
                //多查询4个小时数据
                String startdate = paramMap.get("starttime").toString() + " 00";
                paramMap.put("startdate", startdate);
                String starttime = DataFormatUtil.getBeforeByHourTime(4, startdate);
                starttime = MongoDataUtils.setStartTimeByDataMark(datatype, starttime);
                paramMap.put("starttime", starttime);
            }
            if (paramMap.get("endtime") != null && StringUtils.isNotBlank(paramMap.get("endtime").toString())) {
                String endtime = MongoDataUtils.setEndTimeByDataMark(datatype, paramMap.get("endtime").toString() + " 23");
                paramMap.put("endtime", endtime);
            }
            paramMap.put("collection", collection);
            resultMap = onlineService.getPollutantUpRushDataByParams(outPuts, monitorPointTypeCode, paramMap);
            List<Map<String, Object>> tablelistdata = resultMap.get("tablelistdata") != null ? (List<Map<String, Object>>) resultMap.get("tablelistdata") : null;
            //设置导出文件数据格式
            List<String> headers = MongoDataUtils.getHeaderDataPollutantUpRushByMonitorPointType(monitorPointTypeCode);
            List<String> headersField = MongoDataUtils.setHeaderFieldDataPollutantUpRushByMonitorPointType(monitorPointTypeCode);
            //设置文件名称
            String fileName = "恶臭污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:40
     * @Description: 条件查询环境恶臭浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getStinkChangeWarnDetailParams(@RequestJson(value = "dgimn") String dgimn,
                                                 @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode();
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
     * @Description: 获取环境恶臭浓度突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getChangeWarnPollutantInfo", method = RequestMethod.POST)
    public Object getChangeWarnPollutantInfo(@RequestJson(value = "dgimn") String dgimn,
                                             @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "endtime") String endtime) {
        try {
            Integer monitortype = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitortype);
            paramMap.put("collectiontype", collectiontype);
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
     * @Description: 获取环境恶臭单个污染物浓度突增数据
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


//    /**
//     * @author: chengzq
//     * @date: 2019/7/11 0011 上午 11:23
//     * @Description: 导出恶臭浓度突增数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [pollutionname, starttime, outputname, endtime, pagesize, pagenum]
//     * @throws:
//     */
//    @RequestMapping(value = "exportStinkChangeWarnListByParams", method = RequestMethod.POST)
//    public void exportStinkChangeWarnListByParams(@RequestJson(value = "starttime") String starttime,
//                                                  @RequestJson(value = "outputname", required = false) String outputname, @RequestJson(value = "endtime") String endtime,
//                                                  @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "pagenum", required = false) Integer pagenum,
//                                                  HttpServletResponse response, HttpServletRequest request
//    ) throws Exception {
//        try {
//
//            Object StinkChangeWarnListByParams = getStinkChangeWarnListByParams(starttime, outputname, endtime, Integer.MAX_VALUE, 1);
//
//            JSONObject jsonObject = JSONObject.fromObject(StinkChangeWarnListByParams);
//            Object data = jsonObject.get("data");
//            JSONObject jsonObject1 = JSONObject.fromObject(data);
//            Object datalist = jsonObject1.get("datalist");
//
//            JSONArray jsonArray = JSONArray.fromObject(datalist);
//
//            Object collect = jsonArray.stream().filter(m -> ((Map) m).get("timepoints") != null).peek(m -> {
//                Object timepoints = ((Map) m).get("timepoints");
//                List<Integer> integers = JSONArray.fromObject(timepoints);
//                List<List<Integer>> lists = OnlineGasController.groupIntegerList(integers);
//                String line = OnlineGasController.getLine(lists);
//                ((Map) m).put("timepoints", line.substring(0, line.length() - 1));
//
//            }).collect(Collectors.toList());
//
//            List<String> headers = new ArrayList<>();
//            headers.add("监测点名称");
//            headers.add("日期");
//            headers.add("突增时段");
//            headers.add("突增幅度");
//            List<String> headersField = new ArrayList<>();
//            headersField.add("outputname");
//            headersField.add("monitortime");
//            headersField.add("timepoints");
//            headersField.add("flowrate");
//
//
//            if (jsonArray != null) {
//                JSONArray array = JSONArray.fromObject(collect);
//                HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", headers, headersField, array, "yyyy-MM-dd");
//                byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
//                ExcelUtil.downLoadExcel("恶臭浓度突变预警", response, request, bytesForWorkBook);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 下午 6:48
     * @Description: 根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getStenchOnlineDataAndWeatherDataByParamMap", method = RequestMethod.POST)
    public Object getStenchOnlineDataAndWeatherDataByParamMap(@RequestJson(value = "dgimns", required = false) List<String> dgimns,
                                                              @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        //根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dgimns", dgimns);
        paramMap.put("pollutantcode", pollutantcode);
        paramMap.put("starttime", starttime);
        paramMap.put("endtime", endtime);
        List<Map<String, Object>> resultlist = petitionInfoService.getStenchOnlineDataAndWeatherDataByParamMap(paramMap);

        return AuthUtil.parseJsonKeyToLower("success", resultlist);
    }

    /**
     * @author: xsm
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询监测点相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointRelationListDataByParams", method = RequestMethod.POST)
    public Object getMonitorPointRelationListDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime,
                                                          @RequestJson(value = "dgimn") String dgimn,
                                                          @RequestJson(value = "pollutantcode") String pollutantcode,
                                                          @RequestJson(value = "comparedgimns") List<String> comparedgimns,
                                                          @RequestJson(value = "beforetime") Integer beforetime,
                                                          @RequestJson(value = "comparepollutantcode") String comparepollutantcode,
                                                          @RequestJson(value = "comparetype", required = false) Integer comparetype
    ) throws Exception {
        try {
            String comparestarttime = "";
            String compareendtime = "";
            //根据比较点位的监测时间判断比较的是哪个时间段的监测数据
            if (beforetime == 0) {//同时间时
                comparestarttime = starttime;
                compareendtime = endtime;
            } else {
                comparestarttime = addDate(starttime, datamark, beforetime);
                compareendtime = addDate(endtime, datamark, beforetime);
            }
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> listdatas = similarityAnalysisService.getOnePointMonitorDataByParamMap(paramMap);
            paramMap.clear();
            paramMap.put("starttime", comparestarttime);
            paramMap.put("endtime", compareendtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimnlist", comparedgimns);
            paramMap.put("pollutantcode", comparepollutantcode);
            if (comparetype!=null) {
                paramMap.put("monitorpointtype", comparetype);
            }
            List<Map<String, Object>> comparelistdatas = similarityAnalysisService.getMorePointMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> resultlist = onlineService.getMonitorPointRelationByParamMap(listdatas, comparelistdatas, paramMap);
            //按相似度倒序排
            Comparator<Object> comparebysimilarity = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("similarity").toString())).reversed();
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebysimilarity).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", collect);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 9:14
     * @Description:根据自定义参数获取某类型监测点和其它监测点比较的相关性分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getMonitorPointRelationDataByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointRelationDataByParamMap(@RequestJson(value = "datamark") Integer datamark,
                                                        @RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "dgimn") String dgimn,
                                                        @RequestJson(value = "pollutantcode") String pollutantcode,
                                                        @RequestJson(value = "comparedgimns") List<String> comparedgimns,
                                                        @RequestJson(value = "beforetime") Integer beforetime,
                                                        @RequestJson(value = "comparepollutantcode") String comparepollutantcode,
                                                        @RequestJson(value = "comparetype", required = false) Integer comparetype) {
        try {
            String comparestarttime = "";
            String compareendtime = "";
            //根据比较点位的监测时间判断比较的是哪个时间段的监测数据
            if (beforetime == 0) {//同时间时
                comparestarttime = starttime;
                compareendtime = endtime;
            } else {
                comparestarttime = addDate(starttime, datamark, beforetime);
                compareendtime = addDate(endtime, datamark, beforetime);
            }
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> listdatas = similarityAnalysisService.getOnePointMonitorDataByParamMap(paramMap);
            paramMap.clear();
            paramMap.put("starttime", comparestarttime);
            paramMap.put("endtime", compareendtime);
            paramMap.put("collection", collection);
            paramMap.put("dgimnlist", comparedgimns);
            paramMap.put("pollutantcode", comparepollutantcode);
            if (comparetype!=null) {
                paramMap.put("monitorpointtype", comparetype);
            }
            List<Map<String, Object>> comparelistdatas = similarityAnalysisService.getMorePointMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> resultlist = onlineService.getMonitorPointRelationByParamMap(listdatas, comparelistdatas, paramMap);
            Comparator<Object> comparebysimilarity = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("similarity").toString())).reversed();
            List<Map<String, Object>> collect = resultlist.stream().sorted(comparebysimilarity).collect(Collectors.toList());
            Map<String, Object> resultmap = new HashMap<>();
            if (comparelistdatas != null && comparelistdatas.size() > 0 && resultlist != null && resultlist.size() > 0) {
                List<Map<String, Object>> orderdata = getOrderBySimilarityData(collect, comparelistdatas);
                resultmap.put("comparedatalist", orderdata);
            } else {
                resultmap.put("comparedatalist", comparelistdatas);
            }
            resultmap.put("datalist", listdatas);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/7 0007 下午 4:38
     * @Description: 自定义查询条件查询监测点位相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointRelationChartDataByParams", method = RequestMethod.POST)
    public Object getMonitorPointRelationChartDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                           @RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime,
                                                           @RequestJson(value = "dgimn") String dgimn,
                                                           @RequestJson(value = "pollutantcode") String pollutantcode,
                                                           @RequestJson(value = "comparedgimns") List<String> comparedgimns,
                                                           @RequestJson(value = "beforetime") Integer beforetime,
                                                           @RequestJson(value = "comparepollutantcode") String comparepollutantcode,
                                                           @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                           @RequestJson(value = "pagenum", required = false) Integer pagenum


    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            String comparestarttime = "";
            String compareendtime = "";
            //根据比较点位的监测时间判断比较的是哪个时间段的监测数据
            if (beforetime == 0) {//同时间时
                comparestarttime = starttime;
                compareendtime = endtime;
            } else {
                comparestarttime = addDate(starttime, datamark, beforetime);
                compareendtime = addDate(endtime, datamark, beforetime);
            }
            if ("MinuteData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//分钟数据
                starttime = starttime + ":00";// 分钟
                endtime = endtime + ":59";
                comparestarttime = comparestarttime + ":00";
                compareendtime = compareendtime + ":59";
            } else if ("HourData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//小时数据
                starttime = starttime + ":00:00";// 小时
                endtime = endtime + ":59:59";
                comparestarttime = comparestarttime + ":00:00";
                compareendtime = compareendtime + ":59:59";
            } else if ("DayData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//日数据
                starttime = starttime + " 00:00:00";// 日
                endtime = endtime + " 23:59:59";
                comparestarttime = comparestarttime + " 00:00:00";
                compareendtime = compareendtime + " 23:59:59";
            }
            paramMap.put("beforetime", beforetime);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", comparestarttime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", compareendtime);
            paramMap.put("monitorpointmn", dgimn);
            paramMap.put("dgimnlist", comparedgimns);
            paramMap.put("monitorpointpollutant", pollutantcode);
            paramMap.put("comparepollutantcode", comparepollutantcode);
            if (pagesize != null && pagenum != null) {
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
            }
            resultMap = onlineService.getMonitorPointRelationChartDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/6 0006 下午 7:07
     * @Description:按相似度进行排序，并返回数据（从大到小）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private List<Map<String, Object>> getOrderBySimilarityData(List<Map<String, Object>> collect, List<Map<String, Object>> comparelistdatas) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> obj : collect) {
            for (Map<String, Object> objmap : comparelistdatas) {
                if ((obj.get("dgimn").toString()).equals(objmap.get("dgimn").toString())) {
                    result.add(objmap);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 9:14
     * @Description:根据时间、时间类型和时间差值获取时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private String addDate(String datetime, Integer datamark, int num) {
        SimpleDateFormat format = null;
        if ("MinuteData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//分钟数据
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");// 分钟
        } else if ("HourData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//小时数据
            format = new SimpleDateFormat("yyyy-MM-dd HH");// 24小时制
        } else if ("DayData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//日数据
            format = new SimpleDateFormat("yyyy-MM-dd");// 日
        }
        Date date = null;
        try {
            date = format.parse(datetime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (date == null)
            return "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if ("MinuteData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//分钟数据
            cal.add(Calendar.MINUTE, -num);// 分钟
        } else if ("HourData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//小时数据
            cal.add(Calendar.HOUR, -num);// 24小时制
        } else if ("DayData".equals(MongoDataUtils.getCollectionByDataMark(datamark))) {//日数据
            cal.add(Calendar.DAY_OF_YEAR, -num);// 日
        }
        date = cal.getTime();
        cal = null;
        return format.format(date);
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和MN号获取该时间段内超标污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getStenchOverPollutantByParamMap", method = RequestMethod.POST)
    public Object getStenchOverPollutantByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        //根据监测时间和MN号获取该时间段内超标污染物
        List<Map<String, Object>> result = otherMonitorPointService.getStenchOverPollutantByParamMap(dgimn, starttime, endtime);
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和恶臭点位MN号及污染物获取恶臭污染物超标数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime，pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getStenchOverDataAndWeatherDataByParamMap", method = RequestMethod.POST)
    public Object getStenchOverDataAndWeatherDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                            @RequestJson(value = "pollutantcode") String pollutantcode,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        //根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dgimn", dgimn);
        paramMap.put("pollutantcode", pollutantcode);
        paramMap.put("starttime", starttime);
        paramMap.put("endtime", endtime);
        Map<String, Object> result = otherMonitorPointService.getStenchOverDataAndWeatherDataByParamMap(paramMap);
        return AuthUtil.parseJsonKeyToLower("success", result);
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和恶臭点位MN号及污染物获取恶臭污染物超标列表数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime，pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getStenchOverAndWeatherListDataByParamMap", method = RequestMethod.POST)
    public Object getStenchOverAndWeatherListDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                                            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime,
                                                            @RequestJson(value = "pagesize") Integer pagesize,
                                                            @RequestJson(value = "pagenum") Integer pagenum
    ) throws Exception {
        //根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dgimn", dgimn);
        paramMap.put("pollutantcodes", pollutantcodes);
        paramMap.put("starttime", starttime);
        paramMap.put("endtime", endtime);
        paramMap.put("pagesize", pagesize);
        paramMap.put("pagenum", pagenum);
        Map<String, Object> result = otherMonitorPointService.getStenchOverAndWeatherListDataByParamMap(paramMap);
        return AuthUtil.parseJsonKeyToLower("success", result);
    }

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和恶臭点位MN号及污染物统计风向下污染物监测值范围次数(多个图表)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime，pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "countManyWindOverPollutantValueData", method = RequestMethod.POST)
    public Object countManyWindOverPollutantValueData(@RequestJson(value = "dgimn") String dgimn,
                                                      @RequestJson(value = "pollutantcode") String pollutantcode,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            //根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> result = otherMonitorPointService.countManyWindOverPollutantValueData(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 根据监测点id 监测类型 (厂界、通道、敏感点) 监测时间获取监测点污染物日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOneStenchDayMonitorDataByParamsForApp", method = RequestMethod.POST)
    public Object getOneStenchDayMonitorDataByParamsForApp(@RequestJson(value = "stinkflag", required = false) Integer stinkflag,
                                                           @RequestJson(value = "monitorpointid", required = true) String monitorpointid,
                                                           @RequestJson(value = "monitortime", required = true) String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> stenchMonitorPointInfo = new ArrayList<>();
            Map<String, Object> resultmap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);//数据权限
            paramMap.put("monitorpointid", monitorpointid);
            if (stinkflag != null) {
                List<Integer> flags = new ArrayList<>();
                flags.add(stinkflag);
                paramMap.put("stinkflag", flags);
            }
            stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
            if (stenchMonitorPointInfo.size() > 0) {
                String dgimn = stenchMonitorPointInfo.get(0).get("DGIMN") != null ? stenchMonitorPointInfo.get(0).get("DGIMN").toString() : "";
                resultmap.put("pollutionname", stenchMonitorPointInfo.get(0).get("PollutionName"));
                resultmap.put("monitorpointname", stenchMonitorPointInfo.get(0).get("MonitorPointName"));
                paramMap.put("dgimn", dgimn);
                paramMap.put("monitortime", monitortime);//到日 yyyy-mm-dd
                resultmap = otherMonitorPointService.getOneStenchDayMonitorDataByParamsForApp(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 返回所有恶臭点位其中最近一条监测数据时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getStenchLastOneMonitorTimeByParamsForApp", method = RequestMethod.POST)
    public Object getStenchLastOneMonitorTimeByParamsForApp(@RequestJson(value = "datamark", required = true) String datamark) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> stenchMonitorPointInfo = new ArrayList<>();
            Map<String, Object> resultmap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);//数据权限
            stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
            if (stenchMonitorPointInfo.size() > 0) {
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> map : stenchMonitorPointInfo) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                    }
                }
                resultmap = otherMonitorPointService.getStenchLastOneMonitorTimeByParamsForApp(mns, datamark);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 根据时间标记、监测时间 监测类型 (厂界、通道、敏感点) 获取恶臭监测点该时间的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getStenchMonitorListDataByParamsForApp", method = RequestMethod.POST)
    public Object getStenchMonitorListDataByParamsForApp(@RequestJson(value = "stinkflag", required = false) Integer stinkflag,
                                                         @RequestJson(value = "datamark", required = true) String datamark,
                                                         @RequestJson(value = "monitortime", required = true) String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> stenchMonitorPointInfo = new ArrayList<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);//数据权限
            if (stinkflag != null) {
                List<Integer> flags = new ArrayList<>();
                flags.add(stinkflag);
                paramMap.put("stinkflag", flags);
            }
            stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
            if (stenchMonitorPointInfo.size() > 0) {
                List<String> mns = new ArrayList<>();
                Map<String, Object> mnandent = new HashMap<>();
                Map<String, Object> mnandname = new HashMap<>();
                for (Map<String, Object> map : stenchMonitorPointInfo) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                        if (map.get("PollutionName") != null) {
                            mnandent.put(map.get("DGIMN").toString(), map.get("PollutionName"));
                        }
                        if (map.get("MonitorPointName") != null) {
                            mnandname.put(map.get("DGIMN").toString(), map.get("MonitorPointName"));
                        }
                    }
                }
                paramMap.put("mns", mns);
                paramMap.put("monitortime", monitortime);
                paramMap.put("datamark", datamark);
                paramMap.put("mnandent", mnandent);
                paramMap.put("mnandname", mnandname);
                result = otherMonitorPointService.getStenchMonitorListDataByParamsForApp(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/04 14:42
     * @Description: 根据监测时间和恶臭标记类型获取报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStinkAlarmDataByParamForApp", method = RequestMethod.POST)
    public Object getStinkAlarmDataByParamForApp(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                 @RequestJson(value = "stinkflag", required = true) Integer stinkflag,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);//数据权限
            List<Integer> flags = new ArrayList<>();
            flags.add(stinkflag);
            paramMap.put("stinkflag", flags);
            List<Map<String, Object>> stenchMonitorPointInfo = otherMonitorPointService.getStenchMonitorPointInfo(paramMap);
            Date starttime = DataFormatUtil.parseDate(monitortime + " 00:00:00");
            Date endtime = DataFormatUtil.parseDate(monitortime + " 23:59:59");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> pollutantlist = new ArrayList<>();
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandunit = new HashMap<>();
            Map<String, Object> polltantparams = new HashMap<>();
            if ("3".equals(stinkflag)) {
                polltantparams.put("pollutanttype", FactoryBoundaryStinkEnum.getCode());
                pollutantlist = pollutantService.getPollutantsByCodesAndType(polltantparams);
            } else {
                polltantparams.put("pollutanttype", EnvironmentalStinkEnum.getCode());
                pollutantlist = pollutantService.getPollutantsByCodesAndType(polltantparams);
            }
            if (pollutantlist != null && pollutantlist.size() > 0) {
                for (Map<String, Object> map : pollutantlist) {
                    codeandname.put(map.get("code").toString(), map.get("name"));
                    if (map.get("unit") != null) {
                        codeandunit.put(map.get("code").toString(), map.get("unit"));
                    }
                }
            }
            if (stenchMonitorPointInfo != null && stenchMonitorPointInfo.size() > 0) {
                Set mns = new HashSet();
                Map<String, Object> mnandname = new HashMap<>();
                Map<String, Object> mnandid = new HashMap<>();
                for (Map<String, Object> map : stenchMonitorPointInfo) {
                    if (map.get("DGIMN") != null) {
                        mns.add(map.get("DGIMN").toString());
                        if (map.get("MonitorPointName") != null) {
                            mnandname.put(map.get("DGIMN").toString(), map.get("MonitorPointName"));
                            mnandid.put(map.get("DGIMN").toString(), map.get("pkid"));
                        }
                    }
                }
                if (mns.size() > 0) {
                    paramMap.put("mns", mns);
                    paramMap.put("codeandname", codeandname);
                    paramMap.put("codeandunit", codeandunit);
                    paramMap.put("mnandname", mnandname);
                    paramMap.put("mnandid", mnandid);
                    //恶臭浓度突变
                    result.addAll(otherMonitorPointService.getStinkConcentrationDetailDataByParam(paramMap));
                    //恶臭超限
                    paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode());
                    result.addAll(otherMonitorPointService.getStinkEarlyOrOverAlarmDetailDataByParam(paramMap));
                    //恶臭预警
                    paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode());
                    result.addAll(otherMonitorPointService.getStinkEarlyOrOverAlarmDetailDataByParam(paramMap));
                    //恶臭异常
                    result.addAll(otherMonitorPointService.getStinkExceptionAlarmDetailDataByParam(paramMap));
                }
            }
            if (pagenum != null && pagesize != null) {
                if (result != null && result.size() > 0) {
                    result = result.stream().sorted(Comparator.comparing((Map m) -> m.get("monitortime").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
                }
                int size = result.size();
                int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
                int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
                if (size > pageStart) {
                    result = result.subList(pageStart, pageEnd);
                }
                resultmap.put("total", size);
                resultmap.put("datalist", result);
            } else {
                if (result != null && result.size() > 0) {
                    result = result.stream().sorted(Comparator.comparing((Map m) -> m.get("monitortime").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
                }
                resultmap.put("datalist", result);
            }
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/5/12 0012 上午 9:31
     * @Description: 通过自定义条件获取监测点在线监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointOnlineDataByParams", method = RequestMethod.POST)
    public Object getMonitorPointOnlineDataByParams(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "datetype") String datetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String collection="MinuteData";
            String collectionkey="MinuteDataList";
            String pattern="yyyy-MM-dd HH:mm";
            if("hour".equals(datetype)){
                collection="HourData";
                pattern="yyyy-MM-dd HH";
                collectionkey="HourDataList";

            }else if("day".equals(datetype)){
                collection="DayData";
                pattern="yyyy-MM-dd";
                collectionkey="DayDataList";

            }else if("month".equals(datetype)){
                collection="MonthData";
                pattern="yyyy-MM";
                collectionkey="MonthDataList";

            }
            paramMap.put("fkmonitorpointtypecodes",monitorpointtypes);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("onlydataauthor", "1");
            List<Map<String, Object>> outPutInfosByParamMap = monitorPointCommonService.getOutPutInfosByParamMap(paramMap);
            Map<String, String> dgimnAndOutputname = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null && m.get("OutputName") != null).collect(Collectors.toMap(m -> m.get("DGIMN").toString(), m -> m.get("OutputName").toString(), (a, b) -> a));
            List<String> mns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            paramMap.put("mns",mns);
            paramMap.put("starttime",JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime",JSONObjectUtil.getEndTime(endtime));
            paramMap.put("collection",collection);
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            String finalCollectionkey = collectionkey;
            String finalPattern = pattern;
            for (String dgimn : collect.keySet()) {
                Map<String,Object> moniotrdata=new HashMap<>();
                List<Map<String,Object>> datalist=new ArrayList<>();
                List<Document> documentList = collect.get(dgimn);
                String outputname = dgimnAndOutputname.get(dgimn);
                moniotrdata.put("monitorname",outputname);
                documentList.stream().filter(m->m.get("MonitorTime")!=null).sorted(Comparator.comparing(m->m.get("MonitorTime").toString())).forEach(m->{
                    Map<String,Object> data=new HashMap<>();
                    String monitorTime = FormatUtils.formatDate(FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss"), finalPattern);
                    List<Map<String, Object>> pollutantdatalist = m.get(finalCollectionkey) == null ? new ArrayList<>() : (List<Map<String, Object>>) m.get(finalCollectionkey);
                    String value = pollutantdatalist.stream().filter(n -> n.get("PollutantCode") != null && pollutantcode.equals(n.get("PollutantCode").toString()) && n.get("AvgStrength") != null)
                            .map(n -> n.get("AvgStrength").toString()).findFirst().orElse("");
                    data.put("monitortime",monitorTime);
                    data.put("monitorvalue",value);
                    if(StringUtils.isNotBlank(value)){
                        datalist.add(data);
                    }
                });
                moniotrdata.put("monitordata",datalist);
                if(datalist.size()>0){
                    resultList.add(moniotrdata);
                }
            }

            Map<String, List<Map<String, Object>>> collect1 = resultList.stream().filter(m -> m.get("monitordata") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitordata")).stream()).
                    filter(m -> m.get("monitortime") != null && m.get("monitorvalue") != null).collect(Collectors.groupingBy(m -> m.get("monitortime").toString()));


            Map<String,Object> allMonitorMap=new HashMap<>();
            List<Map<String,Object>> allList=new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.###");
            allMonitorMap.put("monitorname","园区整体");
            allMonitorMap.put("monitordata",allList);
            for (String monitortime : collect1.keySet()) {
                Map<String,Object> data=new HashMap<>();
                List<Map<String, Object>> list = collect1.get(monitortime);
                Double monitorvalue = list.stream().map(m -> m.get("monitorvalue").toString()).collect(Collectors.averagingDouble(m -> Double.valueOf(m)));
                data.put("monitortime",monitortime);
                data.put("monitorvalue",decimalFormat.format(monitorvalue));
                allList.add(data);
            }

            Set<String> times = collect1.keySet();

            resultMap.put("monitorpoint",resultList);
            resultMap.put("times",times.stream().sorted(String::compareTo).collect(Collectors.toList()));
            resultMap.put("park",allList.stream().filter(m->m.get("monitortime")!=null).sorted(Comparator.comparing(m->m.get("monitortime").toString())).collect(Collectors.toList()));

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/5/12 0012 下午 3:54
     * @Description: 获取24小时内小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "get24HourAvgMonitorPointHourOnlineDataByParams", method = RequestMethod.POST)
    public Object get24HourAvgMonitorPointHourOnlineDataByParams(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String collection="HourData";
            String collectionkey="HourDataList";
            String pattern="HH";
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            List<String> hourTimePoints = JSONObjectUtil.get24HourTimePoints(JSONObjectUtil.getStartTime(starttime), JSONObjectUtil.getEndTime(endtime), format);
            DecimalFormat decimalFormat = new DecimalFormat("0.###");


            paramMap.put("fkmonitorpointtypecodes",monitorpointtypes);
            List<Map<String, Object>> outPutInfosByParamMap = monitorPointCommonService.getOutPutInfosByParamMap(paramMap);
            List<String> mns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
            paramMap.put("mns",mns);
            paramMap.put("starttime",JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime",JSONObjectUtil.getEndTime(endtime));
            paramMap.put("collection",collection);
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            for (String hourTimePoint : hourTimePoints) {
                Map<String, Object> data = new HashMap<>();
                Double collect = monitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null && m.get("HourDataList") != null && hourTimePoint.equals(FormatUtils.formatDate(FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss"), pattern)))
                        .flatMap(m -> ((List<Map<String, Object>>) m.get(collectionkey)).stream()).filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString())).map(m -> m.get("AvgStrength").toString())
                        .collect(Collectors.averagingDouble(m -> Double.valueOf(m)));

                data.put("monitortime",hourTimePoint);
                data.put("monitorvalue",decimalFormat.format(collect));
                resultList.add(data);
            }
            resultMap.put("monitorname","园区整体");
            resultMap.put("monitordata",resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 上午 11:20
     * @Description: 获取其它监测点表类型污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOtherPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getOtherPollutantEarlyAndOverAlarmsByParamMap(
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
            paramMap.put("monitorPointType",paramMap.get("othertype")!=null?Integer.valueOf(paramMap.get("othertype").toString()): monitorPointTypeCode);
            List<Map<String, Object>> outputs = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
            paramMap.put("pointtype", paramMap.get("othertype")!=null?Integer.valueOf(paramMap.get("othertype").toString()): monitorPointTypeCode);
            Map<String, Object> resultMap = onlineService.getOtherPollutantEarlyAndOverAlarmsByParamMap(pageNum, pageSize, outputs, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 13:02
     * @Description: 获取其它类型污染物预警/超标/异常详情页面（其它监测点表中类型）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getOtherPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getOtherPollutantEarlyOrOverOrExceptionDetailsPage(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "datatype") List<String> datatypes,
            @RequestJson(value = "counttype") Integer counttype,
            @RequestJson(value = "pagesize", required = false) Integer pageSize,
            @RequestJson(value = "pagenum", required = false) Integer pageNum) {
        Map<String, Object> dataMap = new HashMap<>();
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
            if (pageSize != null) {
                paramMap.put("pagesize", pageSize);
            }
            if (pageSize != null) {
                paramMap.put("pagenum", pageNum);
            }
            paramMap.put("pollutantcodes", pollutantcodes);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorpointtype, new HashMap<>());
            paramMap.put("mns", mns);
            paramMap.put("pointtype", monitorpointtype);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> tablelistdata = onlineService.getPollutantEarlyAndOverAlarmsTableListDataByParamMap(paramMap);
            boolean isoverstandard = false;
            if (tablelistdata != null && tablelistdata.size() > 0) {
                if (tablelistdata.get(0).get("isoverstandard") != null) {
                    isoverstandard = (boolean) tablelistdata.get(0).get("isoverstandard");
                }
            }
            //根据统计类型获取表头数据
            List<Map<String, Object>> tabletitledata = onlineService.getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(isoverstandard, counttype, pollutantcodes, monitorpointtype);
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
     * @author: lip
     * @date: 2019/6/13 0013 下午 4:52
     * @Description: 导出废水污染物预警/超标/异常详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "exportOtherPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportOtherPollutantEarlyOrOverOrExceptionDetailsData(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
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
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorpointtype, new HashMap<>());
            paramMap.put("mns", mns);
            paramMap.put("pointtype", monitorpointtype);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> tablelistdata = onlineService.getPollutantEarlyAndOverAlarmsTableListDataByParamMap(paramMap);
            boolean isoverstandard = false;
            if (tablelistdata != null && tablelistdata.size() > 0) {
                if (tablelistdata.get(0).get("isoverstandard") != null) {
                    isoverstandard = (boolean) tablelistdata.get(0).get("isoverstandard");
                }
            }
            //根据统计类型获取表头数据
            List<Map<String, Object>> tabletitledata = onlineService.getPollutantEarlyAndOverAlarmsTableTitleDataByCountType(isoverstandard, counttype, pollutantcodes, monitorpointtype);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
