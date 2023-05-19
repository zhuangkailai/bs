package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.mongodb.FlowDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: lip
 * @date: 2019/5/28 0028 上午 9:42
 * @Description: 在线雨水监测数据处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineRain")
public class OnlineRainController {

    private final WaterOutPutInfoService waterOutPutInfoService;
    private final OnlineService onlineService;
    private final PollutantService pollutantService;
    private final MongoBaseService mongoBaseService;
    private final MonitorControlService monitorControlService;


    @Autowired
    public OnlineRainController(WaterOutPutInfoService waterOutPutInfoService, OnlineService onlineService, PollutantService pollutantService, MongoBaseService mongoBaseService, MonitorControlService monitorControlService) {
        this.waterOutPutInfoService = waterOutPutInfoService;
        this.onlineService = onlineService;
        this.pollutantService = pollutantService;
        this.mongoBaseService = mongoBaseService;
        this.monitorControlService = monitorControlService;
    }
    @Autowired
    private OnlineMonitorService onlineMonitorService;

    /**
     * voc监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode();

    /**
     * @author: zhangzc
     * @date: 2019/5/22 15:16
     * @Description: 动态条件查询每个雨水排口最新一条监测数据列表数据
     * @param:
     * @return: DataGatherCode   MonitorTime
     */
    @RequestMapping(value = "getRainLastDatasByParamMap", method = RequestMethod.POST)
    public Object getRainLastDatasByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns",List.class);
            if (dgimns!=null&&dgimns.size()>0){
                paramMap.put("outputtype", "rain");
                paramMap.put("dgimns",dgimns);
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                onlineOutPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取雨水排口监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getRainMonitorDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getRainMonitorDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
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

            Boolean isPage = false;
            if (pagenum != null && pagesize != null) {
                paramMap.put("pagenum", pagenum);
                paramMap.put("pagesize", pagesize);
                paramMap.put("sort", "desc");
                isPage = true;
            }
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);

            paramMap.putIfAbsent("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Map<String,Object>> pollutants = new ArrayList<>();
            if (documents.size()>0){
                paramMap.put("monitorpointtype",monitorPointTypeCode);
                paramMap.put("codes",pollutantcodes);
                pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            }

            charDataList = MongoDataUtils.setOneOutPutManyPollutantsCharDataList(documents, pollutants, collection);

            paramMap.put("monitorpointtype", monitorPointTypeCode);
            Map<String, List<Map<String, Object>>> idAndTimeMap = monitorControlService.getMonitorPointIdAndTimesByParam(paramMap);
            for (Map<String, Object> charData : charDataList) {
                charData.put("startendtime", idAndTimeMap.get(outputid));
            }
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
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个雨水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneRainListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneRainListPageDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);

            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 1);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);

            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个雨水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneRainListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneRainListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Integer reportType = 1;

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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多排口雨水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyRainListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyRainListPageDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
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
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);

            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.put("outputids", outputids);
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
            paramMap.put("reportType", 2);
            paramMap.put("pointType", monitorPointTypeCode);
            paramMap.put("pollutantType", monitorPointTypeCode);
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多排口雨水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyRainListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyRainListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Integer reportType = 2;
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
            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);

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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个雨水排口报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneRainOutPutReport", method = RequestMethod.POST)
    public void exportOneRainOutPutReport(
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
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.put("outputids", outputids);
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
            paramMap.put("reportType", 1);
            paramMap.put("pointType", monitorPointTypeCode);
            paramMap.put("pollutantType", monitorPointTypeCode);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "雨水监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个雨水排口报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyRainOutPutReport", method = RequestMethod.POST)
    public void exportManyRainOutPutReport(
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
            String fileName = "雨水监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 8:37
     * @Description: 通过监测时间，污染物，时间类型查询废气或污染物排放量排行
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getRainOrPollutantFlowRankByParams", method = RequestMethod.POST)
    public Object getRainOrPollutantFlowRankByParams(@RequestJson(value = "datetype") String datetype, @RequestJson(value = "pollutantcode",required = false) String pollutantcode,
                                                     @RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("flag", "rain");
            List<Map<String, Object>> allOutPutInfo = waterOutPutInfoService.getAllOutPutInfoByType(paramMap);
            List<Map<String, Object>> outPutFlowInfo = getOutPutFlowInfo(allOutPutInfo, datetype, monitortime, pollutantcode);
            List<Map<String, Object>> collect = outPutFlowInfo.stream().filter(m -> m.get("proportion") != null).sorted(Comparator.comparing(m -> ((Map) m).get("proportion").toString()).reversed()).collect(Collectors.toList());
            resultMap.put("listdata", collect);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 10:58
     * @Description: 通过dgimn，日期类型，监测时间，污染物code获取排口或污染物排放量、同比占比信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, datetype, monitortime, pollutantcode]
     * @throws:
     */
    public List<Map<String, Object>> getOutPutFlowInfo(List<Map<String, Object>> allOutPutInfo, String datetype, String monitortime, String pollutantcode) throws ParseException {
        String dgimns = allOutPutInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
        List<FlowDataVO> thisFlowData = new ArrayList<>();
        List<FlowDataVO> lastFlowData = new ArrayList<>();
        List<FlowDataVO> lastYearFlowData = new ArrayList<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        FlowDataVO flowDataVO = new FlowDataVO();
        Map<String, Object> timeParam = new HashMap<>();
        if (StringUtils.isNotBlank(dgimns)) {
            flowDataVO.setDataGatherCode(dgimns);
        }

        if ("day".equals(datetype)) {
            if (StringUtils.isNotBlank(monitortime)) {
                //今天时间设置
                String thisStartTime = monitortime + " 00";
                Date parse = format.parse(thisStartTime);
                Calendar thisInstance = Calendar.getInstance();
                thisInstance.setTime(parse);
                thisInstance.add(Calendar.DAY_OF_MONTH, 1);
                thisInstance.add(Calendar.HOUR, -1);
                Date time = thisInstance.getTime();
                String thisEndTime = format.format(time);
                timeParam.put("starttime", thisStartTime);
                timeParam.put("endtime", thisEndTime);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeParam).toString());
                thisFlowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH");


                //昨天时间设置
                Map<String, Object> timeMap = OnlineGasController.getTimeMapDay(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH");



                //去年同一天
                Map<String, Object> timeMapYear = OnlineGasController.getTimeMapYear(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMapYear).toString());
                lastYearFlowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH");

            }
        } else if ("month".equals(datetype)) {
            if (StringUtils.isNotBlank(monitortime)) {
                //本月时间设置
                String thisStartTime = monitortime + "-01 00";
                Date parse = format.parse(thisStartTime);
                Calendar thisInstance = Calendar.getInstance();
                thisInstance.setTime(parse);
                thisInstance.add(Calendar.MONTH, 1);
                thisInstance.add(Calendar.HOUR, -1);
                Date time = thisInstance.getTime();
                String thisEndTime = format.format(time);
                timeParam.put("starttime", thisStartTime);
                timeParam.put("endtime", thisEndTime);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeParam).toString());
                thisFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");

                //上个月时间设置
                Map<String, Object> timeMap = OnlineGasController.getTimeMapMonth(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");



                //去年同月
                Map<String, Object> timeMapYear = OnlineGasController.getTimeMapYear(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMapYear).toString());
                lastYearFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");
            }
        } else if ("quarter".equals(datetype)) {
            String[] monitortimes = monitortime.split(",");
            if (monitortimes.length > 0) {
                //本年时间设置
                String thisStartTime = monitortimes[0] + "-01 00";
                Date parse = format.parse(thisStartTime);
                Calendar thisInstance = Calendar.getInstance();
                thisInstance.setTime(parse);
                thisInstance.add(Calendar.MONTH, 3);
                thisInstance.add(Calendar.HOUR, -1);
                Date time = thisInstance.getTime();
                String thisEndTime = format.format(time);
                timeParam.put("starttime", thisStartTime);
                timeParam.put("endtime", thisEndTime);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeParam).toString());
                thisFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");

                //上个季度时间设置
                Map<String, Object> timeMap = OnlineGasController.getTimeMapQuarter(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");


                //去年同季度
                Map<String, Object> timeMapYear = OnlineGasController.getTimeMapYear(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMapYear).toString());
                lastYearFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");
            }


        } else if ("year".equals(datetype)) {
            if (StringUtils.isNotBlank(monitortime)) {
                //本年时间设置
                String thisStartTime = monitortime + "-01-01 00";
                Date parse = format.parse(thisStartTime);
                Calendar thisInstance = Calendar.getInstance();
                thisInstance.setTime(parse);
                thisInstance.add(Calendar.YEAR, 1);
                thisInstance.add(Calendar.HOUR, -1);
                Date time = thisInstance.getTime();
                String thisEndTime = format.format(time);
                timeParam.put("starttime", thisStartTime);
                timeParam.put("endtime", thisEndTime);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeParam).toString());
                thisFlowData = mongoBaseService.getListByParam(flowDataVO, "YearFlowData", "yyyy-MM-dd HH");


                //去年同期时间设置
                Map<String, Object> timeMap = OnlineGasController.getTimeMapYear(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "YearFlowData", "yyyy-MM-dd HH");


                //去年同期时间设置
                lastYearFlowData=lastFlowData;
            }
        }

        //废气排放量
        if ("gasflow".equals(pollutantcode)) {
            OnlineGasController.getGasFlowInfo(allOutPutInfo, thisFlowData, lastFlowData,lastYearFlowData, resultList);
        } else {
            OnlineGasController.getPollutantFlowInfo(datetype, pollutantcode, allOutPutInfo, thisFlowData, lastFlowData,lastYearFlowData, resultList);
        }

        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:24
     * @Description: 获取雨水污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getRainPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getRainPollutantEarlyAndOverAlarmsByParamMap(
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
            paramMap.put("outputtype", "rain");
            List<Map<String, Object>> outputs = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
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
     * @Description: 获取雨水污染物预警/超标/异常详情页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getRainPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getRainPollutantEarlyOrOverOrExceptionDetailsPage(
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
     * @Description: 导出雨水污染物预警/超标/异常详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "exportRainPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportRainPollutantEarlyOrOverOrExceptionDetailsData(
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
            String fileName = "雨水污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取雨水多排口多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getRainManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getRainManyOutPutManyPollutantMonitorDataByParams(
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

            if (documents.size() > 0) {
                paramMap.put("monitorpointtype", monitorPointTypeCode);
                Map<String, List<Map<String, Object>>> idAndTimeMap = monitorControlService.getMonitorPointIdAndTimesByParam(paramMap);
                charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(
                        documents,
                        pollutantcodes,
                        collection,
                        outPutIdAndMn,
                        outputids,
                        idAndName,
                        codeAndName);
                String monitorpointid;
                for (Map<String, Object> charData : charDataList) {
                    monitorpointid = charData.get("outputid").toString();
                    charData.put("startendtime", idAndTimeMap.get(monitorpointid));
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询雨水污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getRainPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getRainPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("outputtype", "rain");
            List<Map<String, Object>> outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
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
     * @Description: 自定义查询条件导出雨水污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportRainPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportRainPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("outputtype", "rain");
            List<Map<String, Object>> outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
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
            String fileName = "雨水污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:40
     * @Description: 条件查询雨水浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getRainChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getRainChangeWarnDetailParams(@RequestJson(value = "dgimn") String dgimn,
                                                @RequestJson(value = "collectiontype",required = false) Integer collectiontype,
                                                @RequestJson(value = "starttime") String starttime,
                                                @RequestJson(value = "endtime") String endtime,
                                                @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode();
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
     * @Description: 获取雨水浓度突增污染物
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
                                             @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Integer monitortype = CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode();
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
     * @Description: 获取雨水单个污染物浓度突增数据
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
                                                    @RequestJson(value = "pollutantcode") String pollutantcode
    ) {
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
//     * @Description: 导出雨水浓度突增数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [pollutionname, starttime, outputname, endtime, pagesize, pagenum]
//     * @throws:
//     */
//    @RequestMapping(value = "exportRainChangeWarnListByParams", method = RequestMethod.POST)
//    public void exportRainChangeWarnListByParams(@RequestJson(value = "pollutionname", required = false) String pollutionname, @RequestJson(value = "starttime") String starttime,
//                                                 @RequestJson(value = "outputname", required = false) String outputname, @RequestJson(value = "endtime") String endtime,
//                                                 @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "pagenum", required = false) Integer pagenum,
//                                                 HttpServletResponse response, HttpServletRequest request
//    ) throws Exception {
//        try {
//
//            Object RainChangeWarnListByParams = getRainChangeWarnListByParams(pollutionname, starttime, outputname, endtime, Integer.MAX_VALUE, 1);
//
//            JSONObject jsonObject = JSONObject.fromObject(RainChangeWarnListByParams);
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
//            headers.add("企业名称");
//            headers.add("排口名称");
//            headers.add("日期");
//            headers.add("突增时段");
//            headers.add("突增幅度");
//            List<String> headersField = new ArrayList<>();
//            headersField.add("pollutionname");
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
//                ExcelUtil.downLoadExcel("雨水浓度突变预警", response, request, bytesForWorkBook);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }

    /**
     * @author: xsm
     * @date: 2020/11/20 0020 上午 09:23
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4，月数据-5,年数据-6）自定义参数获取雨水多排口多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllPointOnePollutantOnlineDataByParam", method = RequestMethod.POST)
    public Object getAllPointOnePollutantOnlineDataByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime", required = false) String monitortime,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                paramMap.put("monitorpointtypecode", monitorpointtype);
                onlineOutPuts = onlineService.getMNAndMonitorPointByParam(paramMap);
            } else {
                onlineOutPuts = new ArrayList<>();
            }
            List<String> mns = new ArrayList<>();
            if (onlineOutPuts.size()>0){
                for (Map<String, Object> map:onlineOutPuts){
                    if (map.get("dgimn")!=null){
                        mns.add(map.get("dgimn").toString());
                    }
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("onlineOutPuts", onlineOutPuts);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("monitortime", monitortime);
            paramMap.put("datamark", datamark);
            List<Map<String, Object>> result = waterOutPutInfoService.getRainAndWaterOutPutPollutantOnlineDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
