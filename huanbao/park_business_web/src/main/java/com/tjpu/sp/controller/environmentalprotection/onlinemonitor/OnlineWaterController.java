package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.common.mongodb.FlowDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.online.EffectiveTransmissionService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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


/**
 * @author: lip
 * @date: 2019/5/28 0028 下午 2:29
 * @Description: 在线废水处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineWater")
public class OnlineWaterController {

    private final WaterOutPutInfoService waterOutPutInfoService;
    private final OnlineService onlineService;
    private final PollutantService pollutantService;
    private final MongoBaseService mongoBaseService;

    private final FileController fileController;

    @Autowired
    public OnlineWaterController(WaterOutPutInfoService waterOutPutInfoService, OnlineService onlineService, PollutantService pollutantService, MongoBaseService mongoBaseService, FileController fileController) {
        this.waterOutPutInfoService = waterOutPutInfoService;
        this.onlineService = onlineService;
        this.pollutantService = pollutantService;
        this.mongoBaseService = mongoBaseService;
        this.fileController = fileController;
    }

    /**
     * 废水监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
    private String dayDataCollect = "DayData";
    //污水处理厂类型  3 生活污水排放口 6 工艺废气排放口
    private String sh_output = "3";
    private String gy_output = "6";

    @Autowired
    private EffectiveTransmissionService effectiveTransmissionService;


    /**
     * @author: zhangzc
     * @date: 2019/5/22 15:16
     * @Description: 动态条件查询每个废水排口最新一条监测数据列表数据
     * @param:
     * @return: DataGatherCode   MonitorTime
     */
    @RequestMapping(value = "getWaterLastDatasByParamMap", method = RequestMethod.POST)
    public Object getWaterLastDatasByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                paramMap.put("outputtype", "water");
                paramMap.put("dgimns", dgimns);
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                onlineOutPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
            } else {
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取废水排口监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterMonitorDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getWaterMonitorDataByDataMarkAndParams(
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
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
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
            List<Map<String, Object>> pollutants = new ArrayList<>();
            if (documents.size() > 0) {
                paramMap.put("monitorpointtype", monitorPointTypeCode);
                paramMap.put("codes", pollutantcodes);
                pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            }

            charDataList = MongoDataUtils.setOneOutPutManyPollutantsCharDataList(documents, pollutants, collection);
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个废水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneWaterListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneWaterListPageDataByDataMarkAndParams(
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
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("starttime", starttime);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个废水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneWaterListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneWaterListDataByDataMarkAndParams(
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
            Integer pointType = 1;
            Integer pollutantType = 1;

            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
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
            paramMap.put("pointType", pointType);
            paramMap.put("pollutantType", pollutantType);
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多排口废水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyWaterListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyWaterListPageDataByDataMarkAndParams(
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
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
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
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多排口废水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyWaterListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyWaterListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {

            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
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
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/30 0030 上午 10:34
     * @Description: 单个排口报表数据导出
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */


    @RequestMapping(value = "exportOneWaterOutPutReport", method = RequestMethod.POST)
    public void exportOneWaterOutPutReport(
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
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.putAll(titleMap);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }

            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "废水监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个废水排口报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyWaterOutPutReport", method = RequestMethod.POST)
    public void exportManyWaterOutPutReport(
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
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
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

            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "废水监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 8:37
     * @Description: 通过监测时间，污染物，时间类型查询废水或污染物排放量排行
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getWaterOrPollutantFlowRankByParams", method = RequestMethod.POST)
    public Object getWaterOrPollutantFlowRankByParams(@RequestJson(value = "datetype") String datetype, @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                      @RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("flag", "water");
            paramMap.put("isdischarge", "14");
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
                lastYearFlowData = lastFlowData;
            }
        }

        //废气排放量
        if ("gasflow".equals(pollutantcode)) {
            OnlineGasController.getGasFlowInfo(allOutPutInfo, thisFlowData, lastFlowData, lastYearFlowData, resultList);
        } else {
            OnlineGasController.getPollutantFlowInfo(datetype, pollutantcode, allOutPutInfo, thisFlowData, lastFlowData, lastYearFlowData, resultList);
        }

        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 7:09
     * @Description: 获取废水污染物超标、预警、异常统计页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getWaterPollutantEarlyAndOverAlarmsByParamMap(
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
            paramMap.put("outputtype", "water");
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
     * @Description: 获取废水污染物预警/超标/异常详情页面
     * @updateUser:xsm
     * @updateDate:2019/12/16
     * @updateDescription:添加超标倍数
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getWaterPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getWaterPollutantEarlyOrOverOrExceptionDetailsPage(
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
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorPointTypeCode, outPutIdAndMn);

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
    @RequestMapping(value = "exportWaterPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportWaterPollutantEarlyOrOverOrExceptionDetailsData(
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

            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(outputid), monitorPointTypeCode, outPutIdAndMn);
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
            String fileName = "废水污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取废水多排口多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getWaterManyOutPutManyPollutantMonitorDataByParams(
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
            charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(documents, pollutantcodes, collection, outPutIdAndMn, outputids, idAndName, codeAndName);
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废水污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getWaterPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("outputtype", "water");
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
     * @Description: 自定义查询条件导出废水污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportWaterPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportWaterPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("outputtype", "water");
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
            String fileName = "废水污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:40
     * @Description: 条件查询废水浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
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
            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
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
     * @author: zhangzc
     * @date: 2019/7/31 10:34
     * @Description: 获取废水浓度突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getChangeWarnPollutantInfo", method = RequestMethod.POST)
    public Object getChangeWarnPollutantInfo(@RequestJson(value = "dgimn") String dgimn,
                                             @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "endtime") String endtime) {
        try {
            Integer monitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
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


//    /**
//     * @author: chengzq
//     * @date: 2019/7/11 0011 上午 11:23
//     * @Description: 导出废水浓度突增数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [pollutionname, starttime, outputname, endtime, pagesize, pagenum]
//     * @throws:
//     */
//    @RequestMapping(value = "exportWaterChangeWarnListByParams", method = RequestMethod.POST)
//    public void exportWaterChangeWarnListByParams(@RequestJson(value = "pollutionname", required = false) String pollutionname, @RequestJson(value = "starttime") String starttime,
//                                                  @RequestJson(value = "outputname", required = false) String outputname, @RequestJson(value = "endtime") String endtime,
//                                                  @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "pagenum", required = false) Integer pagenum,
//                                                  HttpServletResponse response, HttpServletRequest request
//    ) throws Exception {
//        try {
//
//            Object waterChangeWarnListByParams = getWaterChangeWarnListByParams(pollutionname, starttime, outputname, endtime, Integer.MAX_VALUE, 1);
//
//            JSONObject jsonObject = JSONObject.fromObject(waterChangeWarnListByParams);
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
//                ExcelUtil.downLoadExcel("废水浓度突变预警", response, request, bytesForWorkBook);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }


    /**
     * @author: zhangzc
     * @date: 2019/7/11 15:52
     * @Description: 获取废水排放量突增污染物信息
     * @param: dgimn, starttime, endtime
     * @return:
     */
    @RequestMapping(value = "getWaterDischargeUpRushPollutantInfo", method = RequestMethod.POST)
    public Object getWaterDischargeUpRushPollutantInfo(@RequestJson(value = "dgimn") String dgimn, @RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Integer monitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            Integer remindtyp = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitortype);
            paramMap.put("remindtype", remindtyp);
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
     * @Description: 获取单个废水突增污染物排放信息
     * @param: dgimn, starttime, endtime
     * @return:
     */
    @RequestMapping(value = "getWaterPollutantUpRushDischargeInfo", method = RequestMethod.POST)
    public Object getWaterPollutantUpRushDischargeInfo(@RequestJson(value = "dgimn") String dgimn,
                                                       @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                                       @RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "endtime") String endtime,
                                                       @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
            Map<String, Object> result = onlineService.getPollutantUpRushDischargeInfo(starttime, endtime, remindtype, dgimn, pollutantcode, collectiontype);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/10 11:26
     * @Description: 条件查询废水排口下各个污染物排放量信息列表
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterAbruptPollutantsByParam", method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public Object getWaterAbruptPollutantsByParam(@RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "dgimn") String dgimn,
                                                  @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                  @RequestJson(value = "pagenum", required = false) Integer pagenum) throws ParseException {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
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

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/13 10:52 导出废水排放量突增信息
//     * @Description:
//     * @param:
//     * @return:
//     */
//    @RequestMapping(value = "exportWaterAbruptChangeInfoByParam", method = RequestMethod.POST)
//    public void exportWaterAbruptChangeInfoByParam(@RequestJson(value = "endtime") String endtime,
//                                                   @RequestJson(value = "starttime") String starttime,
//                                                   @RequestJson(value = "pollutionname", required = false) String pollutionname,
//                                                   @RequestJson(value = "outputname", required = false) String outputname,
//                                                   HttpServletResponse response, HttpServletRequest request) {
//        try {
//            Map<String, Object> paramMap = new HashMap<>();
//            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
//            Integer remindtype = CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode();
//            paramMap.put("monitortype", monitortype);
//            paramMap.put("pollutionname", pollutionname);
//            paramMap.put("outputname", outputname);
//            paramMap.put("remindtype", remindtype);
//            paramMap.put("starttime", starttime);
//            paramMap.put("endtime", endtime);
//            List<Map<String, Object>> abruptChangeInfo = onlineService.getAbruptChangeInfoByParam(paramMap);
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
//            List<Map<String, Object>> collect = abruptChangeInfo.stream().filter(m -> m.get("timepoints") != null).peek(m -> {
//                List<Integer> integers = (List<Integer>) m.get("timepoints");
//                List<List<Integer>> lists = OnlineGasController.groupIntegerList(integers);
//                String line = OnlineGasController.getLine(lists);
//                m.put("timepoints", line.substring(0, line.length() - 1));
//            }).collect(Collectors.toList());
//            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("废水排放量突变预警", headers, headersField, collect, "");
//            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
//            ExcelUtil.downLoadExcel("废水排放量突变预警" + new Date().getTime(), response, request, bytesForWorkBook);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:08
     * @Description: 根据污染源ID统计该企业近一年废水污染物排放量及浓度变化趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countWaterDischargeAndDensityByPollutionID", method = RequestMethod.POST)
    public Object countWaterDischargeAndDensityByPollutionID(@RequestJson(value = "pollutionid") String pollutionid,
                                                             @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            LocalDate localDate = LocalDate.now();
            String endtime = localDate.toString();
            String starttime = localDate.plusYears(-1).toString();
            Map<String, Map<String, Object>> result = onlineService.countDischargeAndDensityByCodeAndMns(pollutantcode, starttime, endtime, pollutionid, CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 获取废水污染物日均浓度情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "getWaterPollutantDayConcentrationDataByParam", method = RequestMethod.POST)
    public Object getWaterPollutantDayConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                               @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                               @RequestJson(value = "pollutantcode") String pollutantcode


    ) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allPointList = new ArrayList<>();
            if (monitorpointtype == null) {
                monitorpointtype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
                paramMap.put("outputtype", 1);//废水排口
            } else {
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                    paramMap.put("outputtype", 3);//雨水水排口
                }
            }

            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pollutantcode", pollutantcode);
            //获取污染物标准值 
            List<Map<String, Object>> pollutantdata = pollutantService.getWaterEarlyAndStandardValueById(paramMap);
            paramMap.put("outputids", Arrays.asList());
            allPointList = onlineService.getMonitorPointDataByParam(paramMap);
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
                dataList = getWaterPollutantDayConcentrationRankData(allPointList, pollutantdata, pollutantcode, monitortime);
            } else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                dataList = getRainPollutantDayConcentrationRankData(allPointList, pollutantdata, pollutantcode, monitortime);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 组织废水污染物日均浓度数据
     */
    private List<Map<String, Object>> getWaterPollutantDayConcentrationRankData(List<Map<String, Object>> allPointList, List<Map<String, Object>> pollutantdata, String pollutantcode, String monitortime) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        //获取所有废水排口不包含污水处理厂
        if (allPointList != null && allPointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndShorterName = new HashMap<>();//污染源简称
            Map<String, Object> mnAndPoName = new HashMap<>();//污染源名称
            Map<String, Object> mnAndName = new HashMap<>();//排口名称
            Map<String, Object> mnAndstand = new HashMap<>();//标准值
            if (pollutantdata != null && pollutantdata.size() > 0) {
                for (Map<String, Object> map : pollutantdata) {
                    if (map.get("DGIMN") != null && map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                        mnAndstand.put(map.get("DGIMN").toString(), map.get("StandardMaxValue"));
                    }
                }
            }
            for (Map<String, Object> point : allPointList) {
                //判断废水排口不为污水处理厂
                if (point.get("FK_OutputProperty") != null && !"".equals(point.get("FK_OutputProperty").toString())
                        && (sh_output.equals(point.get("FK_OutputProperty").toString()) || gy_output.equals(point.get("FK_OutputProperty").toString()))) {
                    continue;
                } else {
                    mnCommon = point.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndId.put(mnCommon, point.get("pk_id"));
                    mnAndPoName.put(mnCommon, point.get("pollutionname"));
                    mnAndShorterName.put(mnCommon, point.get("shortername"));
                    mnAndName.put(mnCommon, point.get("monitorpointname"));
                }
            }
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", dayDataCollect);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Document> pollutantList;
            Map<String, Double> mnAndThisValue = new HashMap<>();
            Map<String, Double> mnAndThisOverMultiple = new HashMap<>();
            String value;
            Double multiple;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                pollutantList = document.get("DayDataList", List.class);
                for (Document pollutant : pollutantList) {
                    if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                        value = pollutant.getString("AvgStrength");
                        if (StringUtils.isNotBlank(value)) {
                            mnAndThisValue.put(mnCommon, Double.parseDouble(value));
                        }
                        if (pollutant.get("OverMultiple") != null) {
                            multiple = pollutant.getDouble("OverMultiple");
                            if (StringUtils.isNotBlank(multiple.toString())) {
                                mnAndThisOverMultiple.put(mnCommon, multiple);
                            }
                        }
                        break;
                    }
                }
            }
            //获取前一天的数据
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
            Double standvalue;
            Double zzfdvalue;//增长幅度
            Double db_stand;//对比标准
            Map<String, Double> mnAndChange = new HashMap<>();
            for (String mnIndex : mnAndId.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("dgimn", mnIndex);
                dataMap.put("monitorpointid", mnAndId.get(mnIndex));
                String monitorpointname = mnAndName.get(mnIndex) + "";
                if (mnAndShorterName.get(mnIndex) != null) {
                    monitorpointname = mnAndShorterName.get(mnIndex) + "-" + monitorpointname;
                } else {
                    if (mnAndPoName.get(mnIndex) != null) {
                        monitorpointname = mnAndPoName.get(mnIndex) + "-" + monitorpointname;
                    }
                }
                dataMap.put("monitorpointname", monitorpointname);
                //今日浓度
                thisValue = mnAndThisValue.get(mnIndex);
                if (thisValue != null) {
                    dataMap.put("thisvalue", thisValue);
                } else {
                    dataMap.put("thisvalue", -99999);//排序用
                }
                //昨日浓度
                thatValue = mnAndThatValue.get(mnIndex);
                if (thatValue != null) {
                    dataMap.put("thatvalue", thatValue);
                } else {
                    dataMap.put("thatvalue", "-");
                }
                //标准值
                standvalue = mnAndstand.get(mnIndex) != null ? Double.parseDouble(mnAndstand.get(mnIndex).toString()) : null;
                if (standvalue != null) {
                    dataMap.put("standvalue", standvalue);
                } else {
                    dataMap.put("standvalue", "-");
                }
                //对比标准
                if (thisValue != null && standvalue != null && standvalue > 0) {
                    db_stand = 100d * (thisValue / standvalue);
                    dataMap.put("db_stand", DataFormatUtil.SaveOneAndSubZero(db_stand) + "%");
                } else {
                    dataMap.put("db_stand", "-");
                }
                //超标倍数
                if (mnAndThisOverMultiple.get(mnIndex) != null) {
                    Double overnum = mnAndThisOverMultiple.get(mnIndex);
                    if (overnum > 0 || overnum < 0) {
                        dataMap.put("overmultiple", DataFormatUtil.SaveTwoAndSubZero(overnum));
                    } else {
                        dataMap.put("overmultiple", "-");
                    }
                } else {
                    dataMap.put("overmultiple", "-");
                }
                //增长幅度
                if (thisValue != null && thatValue != null && thatValue > 0) {
                    zzfdvalue = 100d * (thisValue - thatValue) / thatValue;
                    dataMap.put("changerange", DataFormatUtil.SaveOneAndSubZero(zzfdvalue) + "%");
                    //按增长幅度排名
                    mnAndChange.put(mnIndex, zzfdvalue);
                } else {
                    dataMap.put("changerange", "-");
                }
                dataList.add(dataMap);
            }
            //获取废水传输有效率
            Map<String, Map<String, Object>> csyxl_data = getWaterOutPutEffectiveTransmissionData(monitortime, mns, pollutantcode, CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            //按增长幅度排名
            mnAndChange = DataFormatUtil.sortMapByValue(mnAndChange, true);
            Map<String, Integer> mnAndRank = new HashMap<>();
            int rank = 1;
            for (String mnIndex : mnAndChange.keySet()) {
                mnAndRank.put(mnIndex, rank);
                rank++;
            }
            //按浓度大小排序
            int thisrank = 1;
            dataList = dataList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("thisvalue").toString())).reversed()).collect(Collectors.toList());
            for (Map<String, Object> dataMap : dataList) {
                if ("-99999".equals(dataMap.get("thisvalue").toString())) {
                    dataMap.put("thisvalue", "-");
                }
                String mn = dataMap.get("dgimn") != null ? dataMap.get("dgimn").toString() : "";
                if (mnAndRank.get(mn) != null) {
                    dataMap.put("fd_rank", mnAndRank.get(mn));
                } else {
                    dataMap.put("fd_rank", "-");
                }
                if (csyxl_data != null && csyxl_data.get(mn) != null) {
                    dataMap.putAll(csyxl_data.get(mn));
                } else {
                    dataMap.put("TransmissionNumber", "-");
                    dataMap.put("EffectiveNumber", "-");
                    dataMap.put("ShouldNumber", "-");
                    dataMap.put("ShouldEffectiveNumber", "-");
                    dataMap.put("TransmissionRate", "-");
                    dataMap.put("EffectiveRate", "-");
                    dataMap.put("TransmissionEffectiveRate", "-");
                }
                dataMap.put("nd_rank", thisrank);
                thisrank++;
            }
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2021/04/16 0016 下午 02:01
     * @Description: 组织雨水污染物日均浓度数据
     */
    private List<Map<String, Object>> getRainPollutantDayConcentrationRankData(List<Map<String, Object>> allPointList, List<Map<String, Object>> pollutantdata, String pollutantcode, String monitortime) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        //获取所有废水排口不包含污水处理厂
        if (allPointList != null && allPointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndShorterName = new HashMap<>();//污染源简称
            Map<String, Object> mnAndPoName = new HashMap<>();//污染源名称
            Map<String, Object> mnAndName = new HashMap<>();//排口名称
            Map<String, Object> mnAndstand = new HashMap<>();//标准值
            if (pollutantdata != null && pollutantdata.size() > 0) {
                for (Map<String, Object> map : pollutantdata) {
                    if (map.get("DGIMN") != null && map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                        mnAndstand.put(map.get("DGIMN").toString(), map.get("StandardMaxValue"));
                    }
                }
            }
            for (Map<String, Object> point : allPointList) {
                //判断废水排口不为污水处理厂
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndId.put(mnCommon, point.get("pk_id"));
                mnAndPoName.put(mnCommon, point.get("pollutionname"));
                mnAndShorterName.put(mnCommon, point.get("shortername"));
                mnAndName.put(mnCommon, point.get("monitorpointname"));

            }
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", dayDataCollect);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Document> pollutantList;
            Map<String, Double> mnAndThisValue = new HashMap<>();
            Map<String, Double> mnAndThisOverMultiple = new HashMap<>();
            String value;
            Double multiple;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                pollutantList = document.get("DayDataList", List.class);
                for (Document pollutant : pollutantList) {
                    if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                        value = pollutant.getString("AvgStrength");
                        if (StringUtils.isNotBlank(value)) {
                            mnAndThisValue.put(mnCommon, Double.parseDouble(value));
                        }
                        if (pollutant.get("OverMultiple") != null) {
                            multiple = pollutant.getDouble("OverMultiple");
                            if (StringUtils.isNotBlank(multiple.toString())) {
                                mnAndThisOverMultiple.put(mnCommon, multiple);
                            }
                        }
                        break;
                    }
                }
            }
            //获取前一天的数据
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
            Double standvalue;
            Double zzfdvalue;//增长幅度
            Double db_stand;//对比标准
            Map<String, Double> mnAndChange = new HashMap<>();
            for (String mnIndex : mnAndId.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("dgimn", mnIndex);
                dataMap.put("monitorpointid", mnAndId.get(mnIndex));
                String monitorpointname = mnAndName.get(mnIndex) + "";
                if (mnAndShorterName.get(mnIndex) != null) {
                    monitorpointname = mnAndShorterName.get(mnIndex) + "-" + monitorpointname;
                } else {
                    if (mnAndPoName.get(mnIndex) != null) {
                        monitorpointname = mnAndPoName.get(mnIndex) + "-" + monitorpointname;
                    }
                }
                dataMap.put("monitorpointname", monitorpointname);
                //今日浓度
                thisValue = mnAndThisValue.get(mnIndex);
                if (thisValue != null) {
                    dataMap.put("thisvalue", thisValue);
                } else {
                    dataMap.put("thisvalue", -99999);//排序用
                }
                //昨日浓度
                thatValue = mnAndThatValue.get(mnIndex);
                if (thatValue != null) {
                    dataMap.put("thatvalue", thatValue);
                } else {
                    dataMap.put("thatvalue", "-");
                }
                //标准值
                standvalue = mnAndstand.get(mnIndex) != null ? Double.parseDouble(mnAndstand.get(mnIndex).toString()) : null;
                if (standvalue != null) {
                    dataMap.put("standvalue", standvalue);
                } else {
                    dataMap.put("standvalue", "-");
                }
                //对比标准
                if (thisValue != null && standvalue != null && standvalue > 0) {
                    db_stand = 100d * (thisValue / standvalue);
                    dataMap.put("db_stand", DataFormatUtil.SaveOneAndSubZero(db_stand) + "%");
                } else {
                    dataMap.put("db_stand", "-");
                }
                //超标倍数
                if (mnAndThisOverMultiple.get(mnIndex) != null) {
                    Double overnum = mnAndThisOverMultiple.get(mnIndex);
                    if (overnum > 0 || overnum < 0) {
                        dataMap.put("overmultiple", DataFormatUtil.SaveTwoAndSubZero(overnum));
                    } else {
                        dataMap.put("overmultiple", "-");
                    }
                } else {
                    dataMap.put("overmultiple", "-");
                }
                //增长幅度
                if (thisValue != null && thatValue != null && thatValue > 0) {
                    zzfdvalue = 100d * (thisValue - thatValue) / thatValue;
                    dataMap.put("changerange", DataFormatUtil.SaveOneAndSubZero(zzfdvalue) + "%");
                    //按增长幅度排名
                    mnAndChange.put(mnIndex, zzfdvalue);
                } else {
                    dataMap.put("changerange", "-");
                }
                dataList.add(dataMap);
            }
            //获取废水传输有效率
            Map<String, Map<String, Object>> csyxl_data = getWaterOutPutEffectiveTransmissionData(monitortime, mns, pollutantcode, CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode());
            //按增长幅度排名
            mnAndChange = DataFormatUtil.sortMapByValue(mnAndChange, true);
            Map<String, Integer> mnAndRank = new HashMap<>();
            int rank = 1;
            for (String mnIndex : mnAndChange.keySet()) {
                mnAndRank.put(mnIndex, rank);
                rank++;
            }
            //按浓度大小排序
            int thisrank = 1;
            dataList = dataList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("thisvalue").toString())).reversed()).collect(Collectors.toList());
            for (Map<String, Object> dataMap : dataList) {
                if ("-99999".equals(dataMap.get("thisvalue").toString())) {
                    dataMap.put("thisvalue", "-");
                }
                String mn = dataMap.get("dgimn") != null ? dataMap.get("dgimn").toString() : "";
                if (mnAndRank.get(mn) != null) {
                    dataMap.put("fd_rank", mnAndRank.get(mn));
                } else {
                    dataMap.put("fd_rank", "-");
                }
                if (csyxl_data != null && csyxl_data.get(mn) != null) {
                    dataMap.putAll(csyxl_data.get(mn));
                } else {
                    dataMap.put("TransmissionNumber", "-");
                    dataMap.put("EffectiveNumber", "-");
                    dataMap.put("ShouldNumber", "-");
                    dataMap.put("ShouldEffectiveNumber", "-");
                    dataMap.put("TransmissionRate", "-");
                    dataMap.put("EffectiveRate", "-");
                    dataMap.put("TransmissionEffectiveRate", "-");
                }
                dataMap.put("nd_rank", thisrank);
                thisrank++;
            }
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 获取传输有效率
     */
    private Map<String, Map<String, Object>> getWaterOutPutEffectiveTransmissionData(String monitortime, List<String> mns, String pollutantcode, int type) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("fkmonitorpointtypecode", type);
        paramMap.put("starttime", monitortime);
        paramMap.put("endtime", monitortime);
        paramMap.put("dgimns", mns);
        if (!"".equals(pollutantcode)) {
            paramMap.put("pollutantcode", pollutantcode);
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        Map<String, Map<String, Object>> result = new HashMap<>();
        List<Map<String, Object>> effectiveTransmissionInfoByParamMap = effectiveTransmissionService.getOutPutEffectiveTransmissionInfoByParamMap(paramMap);

        Map<String, List<Map<String, Object>>> collect = effectiveTransmissionInfoByParamMap.stream().filter(m -> m.get("FK_MonitorPointID") != null && m.get("CountDate") != null).collect(Collectors.groupingBy
                (m -> m.get("FK_MonitorPointID").toString() + "$" + m.get("CountDate").toString()));

        for (String FK_MonitorPointID : collect.keySet()) {
            List<Map<String, Object>> set = collect.get(FK_MonitorPointID);
            Optional<Map<String, Object>> first = set.stream().findFirst();
            if (first.isPresent()) {
                //实传输数量
                Double transmissionnumber = set.stream().map(m -> Double.valueOf(m.get("TransmissionNumber") == null ? "0d" : m.get("TransmissionNumber").toString())).collect(Collectors.summingDouble(m -> m));
                //实有效数量
                Double effectivenumber = set.stream().map(m -> Double.valueOf(m.get("EffectiveNumber") == null ? "0d" : m.get("EffectiveNumber").toString())).collect(Collectors.summingDouble(m -> m));
                //应传输数量
                Double shouldnumber = set.stream().map(m -> Double.valueOf(m.get("ShouldNumber") == null ? "0d" : m.get("ShouldNumber").toString())).collect(Collectors.summingDouble(m -> m));
                //应有效数量
                Double shouldeffectivenumber = set.stream().map(m -> Double.valueOf(m.get("ShouldEffectiveNumber") == null ? "0d" : m.get("ShouldEffectiveNumber").toString())).collect(Collectors.summingDouble(m -> m));
                //传输率
                double transmissionrate = shouldnumber == 0d ? 0d : transmissionnumber / shouldnumber;
                //有效率
                double effectiverate = shouldeffectivenumber == 0d ? 0d : effectivenumber / shouldeffectivenumber;
                //传输有效率
                double transmissioneffectiverate = transmissionrate * effectiverate;

                Map<String, Object> map = first.get();
                Map<String, Object> obj = new HashMap<>();
                String mn = map.get("DGIMN") != null ? map.get("DGIMN").toString() : "";
                obj.put("TransmissionNumber", transmissionnumber);
                obj.put("EffectiveNumber", effectivenumber);
                obj.put("ShouldNumber", shouldnumber);
                obj.put("ShouldEffectiveNumber", shouldeffectivenumber);
                obj.put("TransmissionRate", decimalFormat.format(transmissionrate * 100) + "%");
                obj.put("EffectiveRate", decimalFormat.format(effectiverate * 100) + "%");
                obj.put("TransmissionEffectiveRate", decimalFormat.format(transmissioneffectiverate * 100) + "%");
                if (!"".equals(mn)) {
                    result.put(mn, obj);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 导出废水污染物日均浓度情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "exportWaterPollutantDayConcentrationDataByParam", method = RequestMethod.POST)
    public void exportWaterPollutantDayConcentrationDataByParam(@RequestJson(value = "monitortime", required = false) String monitortime,
                                                                @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                                @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                                @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                                @RequestJson(value = "pollutantunit", required = false) String pollutantunit,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) throws Exception {
        try {
            exportWaterPDC(monitortime, monitorpointtype, pollutantcode, pollutantname, pollutantunit, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "exportWaterPDCByParam", method = RequestMethod.GET)
    public void exportWaterPDCByParam(@RequestParam(value = "monitortime", required = false) String monitortime,
                                      @RequestParam(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                      @RequestParam(value = "pollutantcode", required = false) String pollutantcode,
                                      @RequestParam(value = "pollutantname", required = false) String pollutantname,
                                      @RequestParam(value = "pollutantunit", required = false) String pollutantunit,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        try {
            exportWaterPDC(monitortime, monitorpointtype, pollutantcode, pollutantname, pollutantunit, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 导出方法
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/28 9:55
     */
    private void exportWaterPDC(String monitortime, Integer monitorpointtype, String pollutantcode, String pollutantname, String pollutantunit, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> allPointList = new ArrayList<>();
        if (monitorpointtype == null) {
            monitorpointtype = CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode();
            paramMap.put("outputtype", 1);//废水排口
        } else {
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
                paramMap.put("outputtype", 3);//雨水水排口
            }
        }
        paramMap.put("monitorpointtype", monitorpointtype);
        //paramMap.put("outputtype", 1);//废水排口
        paramMap.put("pollutantcode", pollutantcode);
        //获取污染物标准值
        List<Map<String, Object>> pollutantdata = pollutantService.getWaterEarlyAndStandardValueById(paramMap);
        paramMap.put("outputids", Arrays.asList());
        allPointList = onlineService.getMonitorPointDataByParam(paramMap);
        String fileName = "";
        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
            fileName = "废水数据分析" + new Date().getTime();
            dataList = getWaterPollutantDayConcentrationRankData(allPointList, pollutantdata, pollutantcode, monitortime);
        } else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
            fileName = "雨水数据分析" + new Date().getTime();
            dataList = getRainPollutantDayConcentrationRankData(allPointList, pollutantdata, pollutantcode, monitortime);
        }
        //获取表头数据
        pollutantunit = pollutantunit != null ? pollutantunit : "";
        List<Map<String, Object>> tabletitledata = getWaterPollutantDayConcentrationTableTitle(pollutantunit);
        //设置文件名称
        String titlename = "";
        String ymd = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd", "yyyy年M月d日");
        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
            titlename = ymd + "废水" + pollutantname + "日均浓度总体情况";
        } else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {
            titlename = ymd + "雨水" + pollutantname + "日均浓度总体情况";
        }
        ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, dataList, "", titlename);

    }


    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 获取废水分析表头
     */
    private List<Map<String, Object>> getWaterPollutantDayConcentrationTableTitle(String pollutantunit) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String str = "标准";
        if (!"".equals(pollutantunit)) {
            str = "标准" + "(" + pollutantunit + ")";
        } else {
            str = "标准";
        }
        String[] titlename = new String[]{"日均浓度排名", "点位名称", "日均浓度", str, "对比标准", "超标倍数", "昨日浓度", "环比变化（%）", "环比变化幅度排名", "在线率"};
        String[] titlefiled = new String[]{"nd_rank", "monitorpointname", "thisvalue", "standvalue", "db_stand", "overmultiple", "thatvalue", "changerange", "fd_rank", "transmissionrate"};
        for (int i = 0; i < titlefiled.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("headercode", titlefiled[i]);
            map.put("headername", titlename[i]);
            map.put("rownum", 1);
            map.put("columnnum", "1");
            map.put("chlidheader", new ArrayList<>());
            tableTitleData.add(map);
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2021/03/01 0001 上午 11:23
     * @Description: 获取污水处理厂污染物日均浓度情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "getTreatmentPlantDayConcentrationDataByParam", method = RequestMethod.POST)
    public Object getTreatmentPlantDayConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime
    ) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allPointList = new ArrayList<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            paramMap.put("outputtype", 1);//废水排口
            paramMap.put("outputids", Arrays.asList());
            allPointList = onlineService.getMonitorPointDataByParam(paramMap);
            String mnCommon;
            List<String> sh_inmns = new ArrayList<>();
            List<String> sh_outmns = new ArrayList<>();
            List<String> gy_inmns = new ArrayList<>();
            List<String> gy_outmns = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> outmap = new HashMap<>();
            Map<String, Object> inmap = new HashMap<>();
            for (Map<String, Object> point : allPointList) {
                //判断废水排口为进水口还是出水口
                if (point.get("FK_OutputProperty") != null && !"".equals(point.get("FK_OutputProperty").toString())
                        && (sh_output.equals(point.get("FK_OutputProperty").toString()) || gy_output.equals(point.get("FK_OutputProperty").toString()))) {
                    if (sh_output.equals(point.get("FK_OutputProperty").toString())) {
                        if (point.get("InputorOutput") != null) {
                            mnCommon = point.get("dgimn").toString();
                            if ("1".equals(point.get("InputorOutput").toString())) {//出水口
                                sh_outmns.add(mnCommon);
                                outmap.put(mnCommon, point.get("monitorpointname"));
                            } else if ("2".equals(point.get("InputorOutput").toString())) {//进水口
                                sh_inmns.add(mnCommon);
                                inmap.put(mnCommon, point.get("monitorpointname"));
                            }
                            mns.add(mnCommon);//所有MN
                        }
                    } else if (gy_output.equals(point.get("FK_OutputProperty").toString())) {
                        if (point.get("InputorOutput") != null) {
                            mnCommon = point.get("dgimn").toString();
                            if ("1".equals(point.get("InputorOutput").toString())) {//出水口
                                gy_outmns.add(mnCommon);
                                outmap.put(mnCommon, point.get("monitorpointname"));
                            } else if ("2".equals(point.get("InputorOutput").toString())) {//进水口
                                gy_inmns.add(mnCommon);
                                inmap.put(mnCommon, point.get("monitorpointname"));
                            }
                            mns.add(mnCommon);//所有MN
                        }
                    }
                }
            }
            //获取污水处理厂动态表头（根据进水口、出水口数量生成表头）
            List<Map<String, Object>> tabletitledata = getTreatmentPlantConcentrationTableTitle(sh_inmns, gy_inmns, inmap, sh_outmns, gy_outmns, outmap);
            paramMap.put("dgimns", mns);
            //获取污水处理厂排口污染物标准值
            List<Map<String, Object>> pollutantdata = pollutantService.getWaterEarlyAndStandardValueById(paramMap);
            dataList = getTreatmentPlantDayConcentrationRankData(mns, pollutantdata, monitortime);
            result.put("tabletitledata", tabletitledata);
            result.put("tablelistdata", dataList);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 组织污水处理厂污染物日均浓度数据
     */
    private List<Map<String, Object>> getTreatmentPlantDayConcentrationRankData(List<String> mns, List<Map<String, Object>> pollutantdata, String monitortime) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        //获取所有废水排口不包含污水处理厂
        if (mns != null && mns.size() > 0) {
            List<String> pollutantcodes = new ArrayList<>();
            Map<String, Object> codeAndname = new HashMap<>();//污染物名称
            Map<String, Object> codeAndunit = new HashMap<>();//污染物名称
            Map<String, Object> mncodeAndstand = new HashMap<>();//标准值
            if (pollutantdata != null && pollutantdata.size() > 0) {
                for (Map<String, Object> map : pollutantdata) {
                    if (map.get("FK_PollutantCode") != null && !"".equals(map.get("FK_PollutantCode").toString())) {
                        if (!pollutantcodes.contains(map.get("FK_PollutantCode").toString()) && map.get("PollutantName") != null) {
                            pollutantcodes.add(map.get("FK_PollutantCode").toString());
                            codeAndname.put(map.get("FK_PollutantCode").toString(), map.get("PollutantName"));
                            if (map.get("PollutantUnit") != null) {
                                codeAndunit.put(map.get("FK_PollutantCode").toString(), map.get("PollutantUnit"));
                            }
                        }
                        if (map.get("AlarmType") != null && "1".equals(map.get("AlarmType").toString())) {
                            //上限报警
                            if (map.get("DGIMN") != null && map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                                mncodeAndstand.put(map.get("DGIMN").toString() + "_" + map.get("FK_PollutantCode").toString(), DataFormatUtil.subZeroAndDot(map.get("StandardMaxValue").toString()));
                            }
                        } else if (map.get("AlarmType") != null && "2".equals(map.get("AlarmType").toString())) {
                            //下限报警
                            if (map.get("DGIMN") != null && map.get("StandardMinValue") != null && !"".equals(map.get("StandardMinValue").toString())) {
                                mncodeAndstand.put(map.get("DGIMN").toString() + "_" + map.get("FK_PollutantCode").toString(), DataFormatUtil.subZeroAndDot(map.get("StandardMinValue").toString()));
                            }
                        } else if (map.get("AlarmType") != null && "3".equals(map.get("AlarmType").toString())) {
                            //范围报警
                            if (map.get("DGIMN") != null && map.get("StandardMaxValue") != null && map.get("StandardMinValue") != null) {
                                if (!"".equals(map.get("StandardMaxValue").toString()) && !"".equals(map.get("StandardMinValue").toString())) {
                                    mncodeAndstand.put(map.get("DGIMN").toString() + "_" + map.get("FK_PollutantCode").toString(), DataFormatUtil.subZeroAndDot(map.get("StandardMinValue").toString()) + "-" + DataFormatUtil.subZeroAndDot(map.get("StandardMaxValue").toString()));
                                }
                            }
                        }
                    }
                }
            }
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", new ArrayList<>(pollutantcodes));
            paramMap.put("collection", dayDataCollect);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            String value;
            List<Document> in_Documents;
            List<Document> out_Documents;
            String in_stand = "-";
            String out_stand = "-";
            Map<String, Map<String, Object>> in_map = new HashMap<>();
            Map<String, Map<String, Object>> out_map = new HashMap<>();
            //获取废水传输有效率
            Map<String, Map<String, Object>> csyxl_data = getWaterOutPutEffectiveTransmissionData(monitortime, mns, "", CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            if (documents != null && documents.size() > 0) {
                Map<String, List<Document>> dateDocuments = documents.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                if (mns != null && mns.size() > 0) {
                    for (String inmn : mns) {
                        Map<String, Object> obj = new HashMap<>();
                        if (dateDocuments.get(inmn) != null && dateDocuments.get(inmn).size() > 0) {
                            Document indoc = dateDocuments.get(inmn).get(0);
                            in_Documents = indoc.get("DayDataList", List.class);
                            for (String code : pollutantcodes) {
                                obj.put(code, "-");
                                in_stand = mncodeAndstand.get(inmn + "_" + code) != null ? mncodeAndstand.get(inmn + "_" + code).toString() : "-";
                                obj.put(code + "_stand", in_stand);
                                for (Document pollutant : in_Documents) {
                                    if (code.equals(pollutant.get("PollutantCode"))) {
                                        value = pollutant.getString("AvgStrength");
                                        if (StringUtils.isNotBlank(value)) {
                                            obj.put(code, DataFormatUtil.subZeroAndDot(value));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        if (csyxl_data != null && csyxl_data.get(inmn) != null) {
                            obj.putAll(csyxl_data.get(inmn));
                        }
                        in_map.put(inmn, obj);
                    }
                }
              /*  if (outmns != null && outmns.size() > 0) {
                    for (String outmn : outmns) {
                        Map<String, Object> obj = new HashMap<>();
                        if (dateDocuments.get(outmn) != null && dateDocuments.get(outmn).size() > 0) {
                            Document outdoc = dateDocuments.get(outmn).get(0);
                            out_Documents = outdoc.get("DayDataList", List.class);
                            for (String code : pollutantcodes) {
                                obj.put(code, "-");
                                out_stand = mncodeAndstand.get(outmn + "_" + code) != null ? mncodeAndstand.get(outmn + "_" + code).toString() : "-";
                                obj.put(code + "_stand", out_stand);
                                for (Document pollutant : out_Documents) {
                                    if (code.equals(pollutant.get("PollutantCode"))) {
                                        value = pollutant.getString("AvgStrength");
                                        if (StringUtils.isNotBlank(value)) {
                                            obj.put(code, value);
                                        }
                                        break;
                                    }
                                }
                            }

                        }
                        if (csyxl_data != null && csyxl_data.get(outmns) != null) {
                            obj.putAll(csyxl_data.get(outmns));
                        }
                        out_map.put(outmn, obj);
                    }
                }*/
            }
            //组装列表数据
            for (String code : pollutantcodes) {
                Map<String, Object> obj = new HashMap<>();
                if (codeAndunit.get(code) != null) {
                    obj.put("pollutant", codeAndname.get(code) + "(" + codeAndunit.get(code) + ")");
                } else {
                    obj.put("pollutant", codeAndname.get(code));
                }
                if (mns != null && mns.size() > 0) {
                    for (int i = 0; i < mns.size(); i++) {
                        String inmn = mns.get(i);
                        if (in_map.get(inmn) != null && in_map.get(inmn).size() > 0) {
                            Map<String, Object> inmap = in_map.get(inmn);
                            obj.put("value_" + inmn, inmap.get(code) != null ? inmap.get(code) : "-");
                            obj.put("stand_" + inmn, inmap.get(code + "_stand") != null ? inmap.get(code + "_stand") : "-");
                        } else {
                            obj.put("value_" + inmn, "-");
                            obj.put("stand_" + inmn, in_stand);
                        }
                    }
                }
                /*if (outmns != null && outmns.size() > 0) {
                    for (int i = 0; i < outmns.size(); i++) {
                        String outmn = outmns.get(i);
                        if (out_map.get(outmn) != null && out_map.get(outmn).size() > 0) {
                            Map<String, Object> outmap = out_map.get(outmn);
                            obj.put("output_" + (i + 1), outmap.get(code) != null ? outmap.get(code) : "-");
                            obj.put("outputstand_" + (i + 1), outmap.get(code + "_stand") != null ? outmap.get(code + "_stand") : "-");
                        } else {
                            obj.put("output_" + (i + 1), "-");
                            obj.put("outputstand_" + (i + 1), out_stand);
                        }
                    }
                }*/
                dataList.add(obj);
            }
            //在线率
            if (dataList.size() > 0) {
                Map<String, Object> csyxl_map = new HashMap<>();
                csyxl_map.put("pollutant", "在线率");
                if (mns != null && mns.size() > 0) {
                    for (int i = 0; i < mns.size(); i++) {
                        String inmn = mns.get(i);
                        csyxl_map.put("inputstand_" + (i + 1), "-");
                        if (in_map.get(inmn) != null && in_map.get(inmn).size() > 0) {
                            Map<String, Object> inmap = in_map.get(inmn);
                            csyxl_map.put("value_" + inmn, inmap.get("TransmissionRate") != null ? inmap.get("TransmissionRate") : "-");
                        } else {
                            csyxl_map.put("value_" + inmn, "-");
                        }
                    }
                }
                /*if (outmns != null && outmns.size() > 0) {
                    for (int i = 0; i < outmns.size(); i++) {
                        String outmn = outmns.get(i);
                        csyxl_map.put("outputstand_" + (i + 1), "-");
                        if (out_map.get(outmn) != null && out_map.get(outmn).size() > 0) {
                            Map<String, Object> outmap = out_map.get(outmn);
                            csyxl_map.put("output_" + (i + 1), outmap.get("TransmissionRate") != null ? outmap.get("TransmissionRate") : "-");
                        } else {
                            csyxl_map.put("output_" + (i + 1), "-");
                        }
                    }
                }*/
                dataList.add(csyxl_map);
            }
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2021/03/01 0001 下午 1:51
     * @Description: 获取污水处理厂分析表头
     */
    private List<Map<String, Object>> getTreatmentPlantConcentrationTableTitle(List<String> sh_inmns, List<String> gy_inmns, Map<String, Object> inmap, List<String> sh_outmns, List<String> gy_outmns, Map<String, Object> outmap) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //map.put("width", "150px");
        map.put("headeralign", "center");
        map.put("showhide", true);
        map.put("prop", "pollutant");
        map.put("label", "参数");
        map.put("align", "center");
        tableTitleData.add(map);
        //工业污水处理厂
        if ((gy_inmns != null && gy_inmns.size() > 0) || (gy_outmns != null && gy_outmns.size() > 0)) {
            Map<String, Object> gy_titlemap = new HashMap<>();
            gy_titlemap.put("headeralign", "center");
            gy_titlemap.put("showhide", true);
            gy_titlemap.put("prop", "gy");
            gy_titlemap.put("label", "工业污水处理厂");
            gy_titlemap.put("align", "center");
            List<Map<String, Object>> gy_children = new ArrayList<>();
            if (gy_inmns != null && gy_inmns.size() > 0) {
                for (int i = 0; i < gy_inmns.size(); i++) {
                    Map<String, Object> in_map = new HashMap<>();
                    //in_map.put("width", "120px");
                    in_map.put("headeralign", "center");
                    in_map.put("showhide", true);
                    in_map.put("prop", "value_" + gy_inmns.get(i));
                    in_map.put("label", inmap.get(gy_inmns.get(i)) + "浓度");
                    in_map.put("align", "center");
                    gy_children.add(in_map);
                    Map<String, Object> instand_map = new HashMap<>();
                    //instand_map.put("width", "120px");
                    instand_map.put("headeralign", "center");
                    instand_map.put("showhide", true);
                    instand_map.put("prop", "stand_" + gy_inmns.get(i));
                    instand_map.put("label", inmap.get(gy_inmns.get(i)) + "标准值");
                    instand_map.put("align", "center");
                    gy_children.add(instand_map);
                }
            }
            if (gy_outmns != null && gy_outmns.size() > 0) {
                for (int i = 0; i < gy_outmns.size(); i++) {
                    Map<String, Object> out_map = new HashMap<>();
                    //out_map.put("width", "120px");
                    out_map.put("headeralign", "center");
                    out_map.put("showhide", true);
                    out_map.put("prop", "value_" + gy_outmns.get(i));
                    out_map.put("label", outmap.get(gy_outmns.get(i)) + "浓度");
                    out_map.put("align", "center");
                    gy_children.add(out_map);
                    Map<String, Object> outstand_map = new HashMap<>();
                    //outstand_map.put("width", "120px");
                    outstand_map.put("headeralign", "center");
                    outstand_map.put("showhide", true);
                    outstand_map.put("prop", "stand_" + gy_outmns.get(i));
                    outstand_map.put("label", outmap.get(gy_outmns.get(i)) + "标准值");
                    outstand_map.put("align", "center");
                    gy_children.add(outstand_map);
                }
            }
            gy_titlemap.put("children", gy_children);
            tableTitleData.add(gy_titlemap);
        }
        if ((sh_inmns != null && sh_inmns.size() > 0) || (sh_outmns != null && sh_outmns.size() > 0)) {
            Map<String, Object> sh_titlemap = new HashMap<>();
            sh_titlemap.put("headeralign", "center");
            sh_titlemap.put("showhide", true);
            sh_titlemap.put("prop", "sh");
            sh_titlemap.put("label", "生活污水处理厂");
            sh_titlemap.put("align", "center");
            List<Map<String, Object>> sh_children = new ArrayList<>();
            if (sh_inmns != null && sh_inmns.size() > 0) {
                for (int i = 0; i < sh_inmns.size(); i++) {
                    Map<String, Object> in_map = new HashMap<>();
                    //in_map.put("width", "120px");
                    in_map.put("headeralign", "center");
                    in_map.put("showhide", true);
                    in_map.put("prop", "value_" + sh_inmns.get(i));
                    in_map.put("label", inmap.get(sh_inmns.get(i)) + "浓度");
                    in_map.put("align", "center");
                    sh_children.add(in_map);
                    Map<String, Object> instand_map = new HashMap<>();
                    //instand_map.put("width", "120px");
                    instand_map.put("headeralign", "center");
                    instand_map.put("showhide", true);
                    instand_map.put("prop", "stand_" + sh_inmns.get(i));
                    instand_map.put("label", inmap.get(sh_inmns.get(i)) + "标准值");
                    instand_map.put("align", "center");
                    sh_children.add(instand_map);
                }
            }
            if (sh_outmns != null && sh_outmns.size() > 0) {
                for (int i = 0; i < sh_outmns.size(); i++) {
                    Map<String, Object> out_map = new HashMap<>();
                    //out_map.put("width", "120px");
                    out_map.put("headeralign", "center");
                    out_map.put("showhide", true);
                    out_map.put("prop", "value_" + sh_outmns.get(i));
                    out_map.put("label", outmap.get(sh_outmns.get(i)) + "浓度");
                    out_map.put("align", "center");
                    sh_children.add(out_map);
                    Map<String, Object> outstand_map = new HashMap<>();
                    //outstand_map.put("width", "120px");
                    outstand_map.put("headeralign", "center");
                    outstand_map.put("showhide", true);
                    outstand_map.put("prop", "stand_" + sh_outmns.get(i));
                    outstand_map.put("label", outmap.get(sh_outmns.get(i)) + "标准值");
                    outstand_map.put("align", "center");
                    sh_children.add(outstand_map);
                }
            }
            sh_titlemap.put("children", sh_children);
            tableTitleData.add(sh_titlemap);
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2021/03/01 0001 下午 1:51
     * @Description: 获取污水处理厂导出分析表头
     */
    private List<Map<String, Object>> getTreatmentPlantExportTableTitle(List<String> sh_inmns, List<String> gy_inmns, Map<String, Object> inmap, List<String> sh_outmns, List<String> gy_outmns, Map<String, Object> outmap) {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("columnnum", "1");
        map.put("rownum", 2);
        map.put("headercode", "pollutant");
        map.put("headername", "参数");
        map.put("chlidheader", new ArrayList<>());
        tableTitleData.add(map);
        //工业污水处理厂
        if ((gy_inmns != null && gy_inmns.size() > 0) || (gy_outmns != null && gy_outmns.size() > 0)) {
            Map<String, Object> gy_titlemap = new HashMap<>();
            gy_titlemap.put("rownum", 1);
            gy_titlemap.put("headercode", "gy");
            gy_titlemap.put("headername", "工业污水处理厂");
            List<Map<String, Object>> gy_children = new ArrayList<>();
            int gyint = 0;
            if (gy_inmns != null && gy_inmns.size() > 0) {
                for (int i = 0; i < gy_inmns.size(); i++) {
                    gyint += 2;
                    Map<String, Object> in_map = new HashMap<>();
                    //in_map.put("width", "120px");
                    in_map.put("columnnum", "1");
                    in_map.put("rownum", 1);
                    in_map.put("headercode", "value_" + gy_inmns.get(i));
                    in_map.put("headername", inmap.get(gy_inmns.get(i)) + "浓度");
                    in_map.put("chlidheader", new ArrayList<>());
                    gy_children.add(in_map);
                    Map<String, Object> instand_map = new HashMap<>();
                    //instand_map.put("width", "120px");
                    instand_map.put("columnnum", "1");
                    instand_map.put("rownum", 1);
                    instand_map.put("headercode", "stand_" + gy_inmns.get(i));
                    instand_map.put("headername", inmap.get(gy_inmns.get(i)) + "标准值");
                    instand_map.put("chlidheader", new ArrayList<>());
                    gy_children.add(instand_map);
                }
            }
            if (gy_outmns != null && gy_outmns.size() > 0) {
                for (int i = 0; i < gy_outmns.size(); i++) {
                    gyint += 2;
                    Map<String, Object> out_map = new HashMap<>();
                    //out_map.put("width", "120px");
                    out_map.put("columnnum", "1");
                    out_map.put("rownum", 1);
                    out_map.put("headercode", "value_" + gy_outmns.get(i));
                    out_map.put("headername", outmap.get(gy_outmns.get(i)) + "浓度");
                    out_map.put("chlidheader", new ArrayList<>());
                    gy_children.add(out_map);
                    Map<String, Object> outstand_map = new HashMap<>();
                    //outstand_map.put("width", "120px");
                    outstand_map.put("columnnum", "1");
                    outstand_map.put("rownum", 1);
                    outstand_map.put("headercode", "stand_" + gy_outmns.get(i));
                    outstand_map.put("headername", outmap.get(gy_outmns.get(i)) + "标准值");
                    outstand_map.put("chlidheader", new ArrayList<>());
                    gy_children.add(outstand_map);
                }
            }
            gy_titlemap.put("columnnum", gyint + "");
            gy_titlemap.put("chlidheader", gy_children);
            tableTitleData.add(gy_titlemap);
        }
        if ((sh_inmns != null && sh_inmns.size() > 0) || (sh_outmns != null && sh_outmns.size() > 0)) {
            Map<String, Object> sh_titlemap = new HashMap<>();
            sh_titlemap.put("rownum", 1);
            sh_titlemap.put("headercode", "sh");
            sh_titlemap.put("headername", "生活污水处理厂");
            List<Map<String, Object>> sh_children = new ArrayList<>();
            int shint = 0;
            if (sh_inmns != null && sh_inmns.size() > 0) {
                for (int i = 0; i < sh_inmns.size(); i++) {
                    shint += 2;
                    Map<String, Object> in_map = new HashMap<>();
                    //in_map.put("width", "120px");
                    in_map.put("columnnum", "1");
                    in_map.put("rownum", 1);
                    in_map.put("headercode", "value_" + sh_inmns.get(i));
                    in_map.put("headername", inmap.get(sh_inmns.get(i)) + "浓度");
                    in_map.put("chlidheader", new ArrayList<>());
                    sh_children.add(in_map);
                    Map<String, Object> instand_map = new HashMap<>();
                    //instand_map.put("width", "120px");
                    instand_map.put("columnnum", "1");
                    instand_map.put("rownum", 1);
                    instand_map.put("headercode", "stand_" + sh_inmns.get(i));
                    instand_map.put("headername", inmap.get(sh_inmns.get(i)) + "标准值");
                    instand_map.put("chlidheader", new ArrayList<>());
                    sh_children.add(instand_map);
                }
            }
            if (sh_outmns != null && sh_outmns.size() > 0) {
                for (int i = 0; i < sh_outmns.size(); i++) {
                    shint += 2;
                    Map<String, Object> out_map = new HashMap<>();
                    //out_map.put("width", "120px");
                    out_map.put("columnnum", "1");
                    out_map.put("rownum", 1);
                    out_map.put("headercode", "value_" + sh_outmns.get(i));
                    out_map.put("headername", outmap.get(sh_outmns.get(i)) + "浓度");
                    out_map.put("chlidheader", new ArrayList<>());
                    sh_children.add(out_map);
                    Map<String, Object> outstand_map = new HashMap<>();
                    //outstand_map.put("width", "120px");
                    outstand_map.put("columnnum", "1");
                    outstand_map.put("rownum", 1);
                    outstand_map.put("headercode", "stand_" + sh_outmns.get(i));
                    outstand_map.put("headername", outmap.get(sh_outmns.get(i)) + "标准值");
                    outstand_map.put("chlidheader", new ArrayList<>());
                    sh_children.add(outstand_map);
                }
            }
            sh_titlemap.put("columnnum", shint + "");
            sh_titlemap.put("chlidheader", sh_children);
            tableTitleData.add(sh_titlemap);
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2021/03/02 0002 上午 08:53
     * @Description: 导出污水处理厂污染物日均浓度情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "exportTreatmentPlantConcentrationDataByParam", method = RequestMethod.POST)
    public void exportTreatmentPlantConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) throws Exception {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allPointList = new ArrayList<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode());
            paramMap.put("outputtype", 1);//废水排口
            paramMap.put("outputids", Arrays.asList());
            allPointList = onlineService.getMonitorPointDataByParam(paramMap);
            String mnCommon;
            List<String> sh_inmns = new ArrayList<>();
            List<String> sh_outmns = new ArrayList<>();
            List<String> gy_inmns = new ArrayList<>();
            List<String> gy_outmns = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> outmap = new HashMap<>();
            Map<String, Object> inmap = new HashMap<>();
            for (Map<String, Object> point : allPointList) {
                //判断废水排口为进水口还是出水口
                if (point.get("FK_OutputProperty") != null && !"".equals(point.get("FK_OutputProperty").toString())
                        && (sh_output.equals(point.get("FK_OutputProperty").toString()) || gy_output.equals(point.get("FK_OutputProperty").toString()))) {
                    if (sh_output.equals(point.get("FK_OutputProperty").toString())) {
                        if (point.get("InputorOutput") != null) {
                            mnCommon = point.get("dgimn").toString();
                            if ("1".equals(point.get("InputorOutput").toString())) {//出水口
                                sh_outmns.add(mnCommon);
                                outmap.put(mnCommon, point.get("monitorpointname"));
                            } else if ("2".equals(point.get("InputorOutput").toString())) {//进水口
                                sh_inmns.add(mnCommon);
                                inmap.put(mnCommon, point.get("monitorpointname"));
                            }
                            mns.add(mnCommon);//所有MN
                        }
                    } else if (gy_output.equals(point.get("FK_OutputProperty").toString())) {
                        if (point.get("InputorOutput") != null) {
                            mnCommon = point.get("dgimn").toString();
                            if ("1".equals(point.get("InputorOutput").toString())) {//出水口
                                gy_outmns.add(mnCommon);
                                outmap.put(mnCommon, point.get("monitorpointname"));
                            } else if ("2".equals(point.get("InputorOutput").toString())) {//进水口
                                gy_inmns.add(mnCommon);
                                inmap.put(mnCommon, point.get("monitorpointname"));
                            }
                            mns.add(mnCommon);//所有MN
                        }
                    }
                }
            }
            //获取污水处理厂动态表头（根据进水口、出水口数量生成表头）
            List<Map<String, Object>> tabletitledata = getTreatmentPlantExportTableTitle(sh_inmns, gy_inmns, inmap, sh_outmns, gy_outmns, outmap);
            paramMap.put("dgimns", mns);
            //获取污水处理厂排口污染物标准值
            List<Map<String, Object>> pollutantdata = pollutantService.getWaterEarlyAndStandardValueById(paramMap);
            dataList = getTreatmentPlantDayConcentrationRankData(mns, pollutantdata, monitortime);
            //设置文件名称
            String fileName = "污水处理厂统计分析" + new Date().getTime();
            String titlename = "";
            String ymd = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd", "yyyy年M月d日");
            titlename = "污水处理厂 " + ymd;
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, dataList, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: mmt
     * @date: 2022/9/19 0007 下午 4:38
     * @Description: 自定义查询条件查询监测点位相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointRelationChartDataByParams", method = RequestMethod.POST)
    public Object getMonitorPointRelationChartDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "comparedgimns") List<String> comparedgimns,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws Exception {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(3);
            starttime = starttime + ":00:00";// 小时
            endtime = endtime + ":59:59";
            String comparestarttime = starttime;
            String compareendtime = endtime;

            paramMap.put("beforetime", 0);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", comparestarttime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", compareendtime);
            paramMap.put("monitorpointmn", dgimn);
            paramMap.put("dgimnlist", comparedgimns);
            paramMap.put("monitorpointpollutant", pollutantcode);
            paramMap.put("comparepollutantcode", pollutantcode);
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
     * @author: mmt
     * @date: 2022/9/19 0007 下午 4:38
     * @Description: 自定义查询条件查询管网监测点位同污水处理厂进口超标相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWaterPipeNetworkPointRelationTableDataByParams", method = RequestMethod.POST)
    public Object getWaterPipeNetworkPointRelationTableDataByParams(/*@RequestJson(value = "datamark") Integer datamark,*/
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "comparedgimn") String comparedgimn,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum


    ) throws Exception {
        try {
            Object data = getMonitorPointRelationChartDataByParams(starttime, endtime, dgimn, pollutantcode, Arrays.asList(comparedgimn),
                    pagesize, pagenum);
            JSONObject jsonObject = JSONObject.fromObject(data);
            Object chartData = jsonObject.get("data");
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("comparedgimn", comparedgimn);
            Map<String, Object> result = onlineService.getWaterPipeNetworkPointRelationTableDataByParams(paramMap);
            result.put("chartData", chartData);
            return AuthUtil.parseJsonKeyToLower("success", result);
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
    @RequestMapping(value = "getMonitorPointRelationTableDataByParams", method = RequestMethod.POST)
    public Object getMonitorPointRelationTableDataByParams(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "comparedgimns") List<String> comparedgimns,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(3);
            starttime = starttime + ":00:00";// 小时
            endtime = endtime + ":59:59";
            String comparestarttime = starttime;
            String compareendtime = endtime;

            paramMap.put("beforetime", 0);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", comparestarttime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", compareendtime);
            paramMap.put("monitorpointmn", dgimn);
            paramMap.put("dgimnlist", comparedgimns);
            paramMap.put("monitorpointpollutant", pollutantcode);
            paramMap.put("comparepollutantcode", pollutantcode);
            if (pagesize != null && pagenum != null) {
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
            }
            resultMap = onlineService.getMonitorPointRelationTableDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 污水排污及处理情况（收集量（进水口总量）、排放量（出水口总量），设计日处理量）工艺流程图
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/24 14:46
     */
    @RequestMapping(value = "getInAndOutFlowData", method = RequestMethod.POST)
    public Object getInAndOutFlowData(@RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            //出水口mn
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 1);
            List<String> inMns = waterOutPutInfoService.getInOrOutPutMnListByParam(paramMap);
            paramMap.clear();
            paramMap.put("mns", inMns);
            paramMap.put("starttime", monitortime);
            paramMap.put("endtime", monitortime);
            List<Document> documents = waterOutPutInfoService.getDayFlowDataByParam(paramMap);
            resultMap.put("outtotal", getFlowValue(documents));
            //进水口mn
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 2);
            List<String> outMns = waterOutPutInfoService.getInOrOutPutMnListByParam(paramMap);
            paramMap.put("mns", outMns);
            documents = waterOutPutInfoService.getDayFlowDataByParam(paramMap);
            resultMap.put("intotal", getFlowValue(documents));

            Double dayDisposeFlow = 0d;
            List<ObjectId> filePath = new ArrayList<>();
            List<Map<String, Object>> wsEntList = waterOutPutInfoService.getWSPollutionList();
            for (Map<String, Object> ws : wsEntList) {
                if (ws.get("daydisposeflow") != null) {
                    dayDisposeFlow += Double.parseDouble(ws.get("daydisposeflow").toString());
                }
                if (ws.get("filepath") != null) {
                    filePath.add(new ObjectId(ws.get("filepath").toString()));
                }
            }
            resultMap.put("dayDisposeFlow", DataFormatUtil.SaveOneAndSubZero(dayDisposeFlow));
            Map<String, Object> idAndData = fileController.getImgIdAndData(filePath, "1");
            resultMap.put("imgdata", idAndData.values());
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取污水处理厂进水口，出水口最新数据信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/24 14:46
     */
    @RequestMapping(value = "getPWOutPutLastDataList", method = RequestMethod.POST)
    public Object getPWOutPutLastDataList() throws Exception {
        try {

            //出水口mn
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 1);
            List<String> outMns = waterOutPutInfoService.getInOrOutPutMnListByParam(paramMap);
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            outMns = outMns.stream().filter(item -> dgimns.contains(item)).collect(Collectors.toList());
            //进水口mn
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 2);
            List<String> inMns = waterOutPutInfoService.getInOrOutPutMnListByParam(paramMap);
            inMns = inMns.stream().filter(item -> dgimns.contains(item)).collect(Collectors.toList());
            paramMap.put("inMns", inMns);
            paramMap.put("outMns", outMns);
            Map<String, Object> resultMap = onlineService.getPWOutPutLastDataList(paramMap);


            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 企业供水数据统计（工业总供水、总排放水量，企业用水，企业排水）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/24 14:46
     */
    @RequestMapping(value = "getEntWaterSupplyData", method = RequestMethod.POST)
    public Object getEntWaterSupplyData(@RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortime", monitortime);
            paramMap.put("userid", RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class));
            List<Map<String, Object>> supplyDataList = waterOutPutInfoService.getEntWaterSupplyDataByParam(paramMap);
            Map<String, Double> idAndSD = new HashMap<>();
            Double supplyTotal = 0d;
            Double SD;
            String pollutionId;
            for (Map<String, Object> sd : supplyDataList) {
                if (sd.get("watersupply") != null) {
                    SD = Double.parseDouble(sd.get("watersupply").toString());
                    pollutionId = sd.get("pollutionid").toString();
                    idAndSD.put(pollutionId, SD);
                    supplyTotal += SD;
                }

            }

            supplyTotal = supplyTotal / 10000;
            resultMap.put("supplyTotal", DataFormatUtil.SaveOneAndSubZero(supplyTotal));

            paramMap.put("outputtype", "water");
            List<Map<String, Object>> pointList = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
            Map<String, Object> idAndName = new HashMap<>();
            Map<String, String> mnAndId = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String mnCommon;
            for (Map<String, Object> point : pointList) {
                if (point.get("fk_pollutionid") != null && point.get("dgimn") != null) {
                    pollutionId = point.get("fk_pollutionid").toString();
                    mnCommon = point.get("dgimn").toString();
                    idAndName.put(pollutionId, point.get("shortername"));
                    mnAndId.put(mnCommon, pollutionId);
                    mns.add(mnCommon);
                }

            }
            paramMap.put("mns", mns);
            List<Document> documents = waterOutPutInfoService.getMonthFlowDataByParam(paramMap);
            Map<String, Double> idAndFlow = new HashMap<>();
            Double value;
            Double totalFlow = 0d;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                if (document.get("TotalFlow") != null && mnAndId.get(mnCommon) != null) {


                    pollutionId = mnAndId.get(mnCommon);
                    value = Double.parseDouble(document.getString("TotalFlow"));
                    totalFlow += value;
                    idAndFlow.put(pollutionId, idAndFlow.get(pollutionId) != null ? idAndFlow.get(pollutionId) + value : value);

                }
            }
            totalFlow = totalFlow / 10000;

            resultMap.put("totalFlow", DataFormatUtil.SaveOneAndSubZero(totalFlow));
            List<Map<String, Object>> dataList = new ArrayList<>();

            for (String idIndex : idAndFlow.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("pollutionid", idIndex);
                dataMap.put("pollutionname", idAndName.get(idIndex));
                SD = idAndSD.get(idIndex) != null ? idAndSD.get(idIndex) : 0d;
                dataMap.put("supply", DataFormatUtil.SaveOneAndSubZero(SD));
                value = idAndFlow.get(idIndex);
                dataMap.put("flow", DataFormatUtil.SaveOneAndSubZero(value));
                dataList.add(dataMap);
            }
            //排序
            dataList = dataList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
            resultMap.put("dataList", dataList);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private String getFlowValue(List<Document> documents) {
        Double inTotal = 0d;
        for (Document document : documents) {
            if (document.get("TotalFlow") != null) {
                inTotal += Double.parseDouble(document.getString("TotalFlow"));
            }
        }
//        inTotal = inTotal / 10000d;
        return DataFormatUtil.SaveOneAndSubZero(inTotal);
    }

}
