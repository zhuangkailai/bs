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
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.meteoEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: chengzq
 * @date: 2020/6/11 0011 上午 11:24
 * @Description: 在线微站监测数据
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("onlineMicroStation")
public class OnlineMicroStationController {

    private final OtherMonitorPointService otherMonitorPointService;
    private final OnlineService onlineService;
    private final OnlineMonitorService onlineMonitorService;

    public OnlineMicroStationController(OtherMonitorPointService otherMonitorPointService, OnlineService onlineService,OnlineMonitorService onlineMonitorService) {
        this.otherMonitorPointService = otherMonitorPointService;
        this.onlineService = onlineService;
        this.onlineMonitorService = onlineMonitorService;
    }

    /**
     * 微站监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode();
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: lip
     * @date: 2019/6/19 0019 下午 2:14
     * @Description: 动态条件查询每个微站最新一条监测数据列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationLastDataByParamMap", method = RequestMethod.POST)
    public Object getMicroStationLastDataByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns",List.class);
            if (dgimns!=null&&dgimns.size()>0){
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                paramMap.put("monitorPointType", monitorPointTypeCode);
                paramMap.put("dgimns",dgimns);
                onlineOutPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
            }else {
                onlineOutPuts = new ArrayList<>();
            }
            Map<String, Object> resultMap = onlineService.getOutPutLastDatasByParamMap(onlineOutPuts, monitorPointTypeCode, paramMap);
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
    @RequestMapping(value = "getMicroStationDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getMicroStationDataByDataMarkAndParams(
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个微站列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneMicroStationListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneMicroStationListPageDataByDataMarkAndParams(
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个微站列表内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneMicroStationListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneMicroStationListDataByDataMarkAndParams(
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
    @RequestMapping(value = "getManyMicroStationListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyMicroStationListPageDataByDataMarkAndParams(
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多个站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyMicroStationListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyMicroStationListDataByDataMarkAndParams(
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneMicroStationOutPutReport", method = RequestMethod.POST)
    public void exportOneMicroStationOutPutReport(
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
            String fileName = "微站监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyMicroStationOutPutReport", method = RequestMethod.POST)
    public void exportManyMicroStationOutPutReport(
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
            String fileName = "微站监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:24
     * @Description: 获取微站污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getMicroStationPollutantEarlyAndOverAlarmsByParamMap(
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
     * @Description: 获取微站污染物预警/超标/异常详情页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getMicroStationPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getMicroStationPollutantEarlyOrOverOrExceptionDetailsPage(
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
    @RequestMapping(value = "exportMicroStationPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportMicroStationPollutantEarlyOrOverOrExceptionDetailsData(
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
            String fileName = "微站污染物报警详情导出文件_" + new Date().getTime();
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
    @RequestMapping(value = "getMicroStationManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getMicroStationManyOutPutManyPollutantMonitorDataByParams(
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
     * @date: 2020/6/11 0011 上午 11:25
     * @Description: 获取微站浓度突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getChangeWarnPollutantInfo", method = RequestMethod.POST)
    public Object getChangeWarnPollutantInfo(@RequestJson(value = "dgimn") String dgimn,
                                             @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "endtime") String endtime) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("collectiontype", collectiontype);
            paramMap.put("monitortype", monitorPointTypeCode);
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
     * @Description: 获取微站单个污染物浓度突增数据
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
     * @date: 2019/7/31 10:40
     * @Description: 条件查询微站浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getMicroStationChangeWarnDetailParams(@RequestJson(value = "dgimn") String dgimn,
                                                 @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            paramMap.put("collectiontype", collectiontype);
            paramMap.put("monitortype", monitorPointTypeCode);
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
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询恶臭污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getMicroStationPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
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
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询恶臭污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportMicroStationPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportMicroStationPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
            String fileName = "微站污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/01/28 13:31
     * @Description: 获取微站在某段时间内某个污染物的小时浓度以及主导风向和平均风速
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationHourAndWeatherDataByParams", method = RequestMethod.POST)
    public Object getMicroStationHourAndWeatherDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Date startTime = DataFormatUtil.parseDate(starttime + ":00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + ":59:59");
            Set<String> mns = new HashSet<>();
            Set<String> qx_mns = new HashSet<>();
            List<Map<String, Object>> outputs = new ArrayList<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Integer type : monitorpointtypes) {
                    outputs.addAll(onlineService.getMnForWeatherByParam(type));
                }
            }
            //outputs = onlineService.getMnForWeatherByParam(MicroStationEnum.getCode());
            //气象监测点
            List<Map<String, Object>> qx_points = otherMonitorPointService.getStinkMonitorPoint(meteoEnum.getCode());
            Map<String, Object> mn_name = new HashMap<>();
            for (Map<String, Object> output : outputs) {
                Object dgimn = output.get("DGIMN"); //mn号
                if (dgimn != null) {
                    mns.add(dgimn.toString());
                    if (output.get("MonitorPointName")!=null) {
                        mn_name.put(dgimn.toString(),output.get("MonitorPointName"));
                    }
                }
            }
            //获取时间
            List<String> times = DataFormatUtil.separateTimeForHour(starttime + ":00:00", endtime + ":00:00", 1);
            times.add(endtime);
            List<Map<String, Object>> pointlist = new ArrayList<>();
            List<Map<String, Object>> weatherlist = new ArrayList<>();
            List<Map<String, Object>> weathertwolist = new ArrayList<>();
            if (mns.size() > 0) {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("collection","HourData");
                paramMap.put("starttime",starttime + ":00:00");
                paramMap.put("endtime",starttime + ":59:59");
                paramMap.put("mns",Arrays.asList(mns.toArray()));
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }

                Criteria criteria =new Criteria();
                Criteria criteria2 =new Criteria();
                criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime);
                criteria2.and("HourDataList.PollutantCode").is(pollutantcode);
                List<Document> documents = mongoTemplate.aggregate(newAggregation(
                        match(criteria), unwind("HourDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").
                                and("HourDataList.PollutantCode").as("PollutantCode").and("HourDataList.AvgStrength").as("AvgStrength"))
                        , "HourData", Document.class).getMappedResults();
                if (documents.size() > 0) {
                    Map<String, List<Document>> documentMap = documents.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                    for (Map<String, Object> output : outputs) {
                        Object dgimn = output.get("DGIMN"); //mn号
                        String mn = dgimn.toString();
                        if (documentMap.containsKey(mn)) {
                            List<Document> documents1 = documentMap.get(mn);
                            Map<String, Object> monitorMap = new HashMap<>();
                            List<Map<String, Object>> mapList = new ArrayList<>();
                            for (String time:times){
                                Object monitorValue = null;
                                for (Document document : documents1) {
                                    String hms = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                                    if (time.equals(hms)) {
                                            String PollutantCode = document.getString("PollutantCode");
                                            if (PollutantCode.equals(pollutantcode)) {
                                                if (document.get("AvgStrength") != null) { //浓度值
                                                    monitorValue = document.get("AvgStrength");
                                                    break;
                                                }
                                            }
                                    }
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("monitortime", time);
                                map.put("monitorvalue", monitorValue != null ? monitorValue : "");
                                if (!"".equals(map.get("monitorvalue").toString()) ) {
                                    mapList.add(map);
                                }
                            }
                            monitorMap.put("monitorname", mn_name.get(mn));
                            monitorMap.put("monitordata", mapList);
                            pointlist.add(monitorMap);
                        }
                    }
                }
            }
            //获取所以气象点位的 主导风向和平均风速
            for (Map<String, Object> qx_obj : qx_points) {
                Object qx_dgimn = qx_obj.get("mn"); //mn号
                if (qx_dgimn != null) {
                    qx_mns.add(qx_dgimn.toString());
                }
            }
            if (mns.size() > 0) {
                String code = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
                String code2 = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
                List<String> pollutants = new ArrayList<>();
                pollutants.add(code);
                pollutants.add(code2);
                Map<String, Object> map2 = new HashMap<>();
                map2.put("PollutantCode", "$PollutantCode");
                map2.put("AvgStrength", "$AvgStrength");
                Criteria criteria =new Criteria();
                Criteria criteria2 =new Criteria();
                criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime);
                criteria2.and("HourDataList.PollutantCode").in(pollutants);
                List<Document> documents = mongoTemplate.aggregate(newAggregation(
                        match(criteria), unwind("HourDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").and("HourDataList.PollutantCode").as("PollutantCode").and("HourDataList.AvgStrength").as("AvgStrength")
                        , group("DataGatherCode", "MonitorTime").push(map2).as("HourDataList"))
                        , "HourData", Document.class).getMappedResults();
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        Object windspeed = null;
                        Object winddirection = null;
                        String hms = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        String dgimn = document.getString("DataGatherCode");
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
                        Map<String, Object> weathermap = new HashMap<>();
                        weathermap.put("monitortime", hms);
                        weathermap.put("dgimn", dgimn);
                        weathermap.put("winddirectioncode", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "code") : "");
                        weathermap.put("winddirectionname", winddirection != null ? DataFormatUtil.windDirectionSwitch(Double.parseDouble(winddirection.toString()), "name") : "");
                        weathermap.put("winddirectionvalue", winddirection != null ? winddirection : "");
                        weathermap.put("windspeed", windspeed != null ? windspeed : "");
                        if (!"".equals(weathermap.get("winddirectionname").toString()))
                        {
                            weatherlist.add(weathermap);
                        }
                    }
                    //统计主导风向  和平均风速
                    if (weatherlist!=null&&weatherlist.size()>0){
                        Map<String, List<Map<String, Object>>> listMap = weatherlist.stream().collect(Collectors.groupingBy(m -> m.get("monitortime").toString()));
                        for (String time:times){
                            String zd_winddirection = "";
                            String zd_windspeed = "";
                            if (listMap.get(time)!=null){
                                List<Map<String, Object>> onelist = listMap.get(time);
                                Map<String, List<Map<String, Object>>> onemap = onelist.stream().collect(Collectors.groupingBy(m -> m.get("winddirectionname").toString()));
                                int i = 0;
                                List<Map<String, Object>> twolist = null;
                                for (Map.Entry<String, List<Map<String, Object>>> entry : onemap.entrySet()) {
                                    int j = entry.getValue().size();
                                    if (j>i){
                                        i=j;
                                        twolist = entry.getValue();
                                        zd_winddirection = entry.getKey();
                                    }
                                }
                                if (i>0&&twolist!=null){
                                    int n = 0;
                                    double total = 0d;
                                    for (Map<String, Object> map:twolist){
                                        if (map.get("windspeed")!=null&&!"".equals(map.get("windspeed").toString())){
                                            n+=1;
                                            total +=  Double.parseDouble(map.get("windspeed").toString());
                                        }
                                    }
                                    if (n>0) {
                                        zd_windspeed = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total/n));
                                    }
                                }
                            }
                            if (!"".equals(zd_winddirection)){
                                Map<String, Object> weathermap = new HashMap<>();
                                weathermap.put("monitortime",time);
                                weathermap.put("winddirectionname",zd_winddirection);
                                weathermap.put("windspeed",zd_windspeed);
                                weathertwolist.add(weathermap);
                            }
                        }
                    }
                }
            }
            resultMap.put("pointdata",pointlist);
            resultMap.put("weatherdata",weathertwolist);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/01/28 13:33
     * @Description: 获取所有微站某个时刻的突增点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationHourChangeDataByParams", method = RequestMethod.POST)
    public Object getMicroStationHourChangeDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantname") String pollutantname,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Date startTime = DataFormatUtil.parseDate(starttime + ":00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + ":59:59");
            Set<String> mns = new HashSet<>();
            List<Map<String, Object>> outputs = new ArrayList<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Integer type : monitorpointtypes) {
                    outputs.addAll(onlineService.getMnForWeatherByParam(type));
                }
            }
            //outputs = onlineService.getMnForWeatherByParam(MicroStationEnum.getCode());
            Map<String, Object> mn_name = new HashMap<>();
            //获取时间
            List<String> times = DataFormatUtil.separateTimeForHour(starttime + ":00:00", endtime + ":00:00", 1);
            times.add(endtime);
            for (Map<String, Object> output : outputs) {
                Object dgimn = output.get("DGIMN"); //mn号
                if (dgimn != null) {
                    mns.add(dgimn.toString());
                    if (output.get("MonitorPointName")!=null) {
                        mn_name.put(dgimn.toString(),output.get("MonitorPointName"));
                    }
                }
            }
            if (mns.size() > 0) {
                Criteria criteria = new Criteria();
                Criteria criteria2 = new Criteria();
                criteria.and("DataGatherCode").in(mns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startTime).lte(endTime);
                criteria2.and("HourDataList.PollutantCode").is(pollutantcode).and("HourDataList.IsSuddenChange").is(true);
                List<Document> documents = mongoTemplate.aggregate(newAggregation(
                        match(criteria), unwind("HourDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").and("HourDataList.PollutantCode").as("PollutantCode").and("HourDataList.AvgStrength").as("AvgStrength")
                                .and("HourDataList.IsSuddenChange").as("IsSuddenChange").and("HourDataList.ChangeMultiple").as("ChangeMultiple")
                ), "HourData", Document.class).getMappedResults();
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        String dgimn = document.getString("DataGatherCode");
                        String date = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        String hms = date.substring(11, 13);    //监测时间
                        Boolean issuddenchange = document.get("IsSuddenChange") != null ? document.getBoolean("IsSuddenChange") : false;
                        Double changemultiple = document.get("ChangeMultiple") != null ? Double.valueOf(document.get("ChangeMultiple").toString()) : null;
                       // String value = document.get("AvgStrength") != null ? document.getString("AvgStrength") : "";
                        if (issuddenchange == true&&changemultiple!=null) {
                            Map<String, Object> obj = new HashMap<>();
                            obj.put("monitorpointname", mn_name.get(dgimn));
                            obj.put("monitortime", date);
                            obj.put("pollutantcode", pollutantcode);
                            obj.put("pollutantname", pollutantname);
                            obj.put("timestr", hms + "时");
                            obj.put("changemultiple", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(changemultiple * 100)) + "%");
                            result.add(obj);
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
