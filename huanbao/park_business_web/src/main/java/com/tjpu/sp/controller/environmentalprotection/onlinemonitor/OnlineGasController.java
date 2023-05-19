package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.FlowDataVO;
import com.tjpu.sp.model.common.mongodb.OnlineDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.entpermittendflowlimit.EntPermittedFlowLimitValueService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.EffectiveTransmissionService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
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

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum;


/**
 * @author: lip
 * @date: 2019/5/27 0027 下午 7:53
 * @Description: 在线废气监测数据处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineGas")
public class OnlineGasController {
    private final GasOutPutInfoService gasOutPutInfoService;
    private final OnlineService onlineService;
    private final PollutantService pollutantService;
    private final MongoBaseService mongoBaseService;
    private final OtherMonitorPointService otherMonitorPointService;
    private final GasOutPutPollutantSetService gasOutPutPollutantSetService;
    private final EntPermittedFlowLimitValueService entPermittedFlowLimitValueService;

    @Autowired
    public OnlineGasController(GasOutPutInfoService gasOutPutInfoService,
                               OnlineService onlineService,
                               PollutantService pollutantService, EntPermittedFlowLimitValueService entPermittedFlowLimitValueService,
                               GasOutPutPollutantSetService gasOutPutPollutantSetService,
                               MongoBaseService mongoBaseService,
                               OtherMonitorPointService otherMonitorPointService) {
        this.gasOutPutInfoService = gasOutPutInfoService;
        this.onlineService = onlineService;
        this.pollutantService = pollutantService;
        this.entPermittedFlowLimitValueService = entPermittedFlowLimitValueService;
        this.gasOutPutPollutantSetService = gasOutPutPollutantSetService;
        this.mongoBaseService = mongoBaseService;
        this.otherMonitorPointService = otherMonitorPointService;
    }

    @Autowired
    private OnlineMonitorService onlineMonitorService;
    @Autowired
    private EffectiveTransmissionService effectiveTransmissionService;

    private String hourFlowData = "HourFlowData";

    private String dayData_db = "DayData";
    /**
     * 废气监测点类型编码
     **/
    private final int monitorPointTypeCode = WasteGasEnum.getCode();

    /**
     * @author: zhangzc
     * @date: 2019/5/22 15:16
     * @Description: 动态条件查询每个废气排口最新一条监测数据列表数据
     * @param:
     * @return: DataGatherCode   MonitorTime
     */
    @RequestMapping(value = "getGasLastDatasByParamMap", method = RequestMethod.POST)
    public Object getGasLastDatasByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            //排口数据
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                paramMap.put("dgimns", dgimns);
                onlineOutPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            } else {
                onlineOutPuts = new ArrayList<>();
            }
            Map<String, Object> resultMap = onlineService.getOutPutLastDatasByParamMap(onlineOutPuts, Integer.parseInt(paramMap.get("monitorpointtype").toString()), paramMap);
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
    @RequestMapping(value = "getGasMonitorDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getGasMonitorDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            int monitortype = monitorPointTypeCode;
            if (monitorpointtype != null) {
                monitortype = monitorpointtype;
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", outputids);
            paramMap.put("monitorPointType", monitortype);
            List<Map<String, Object>> gasOutputs = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
            List<String> mns = new ArrayList<>();
            if (gasOutputs != null && gasOutputs.size() > 0) {
                if (gasOutputs.get(0).get("dgimn") != null) {
                    mns.add(gasOutputs.get(0).get("dgimn").toString());
                    if (gasOutputs.get(0).get("monitorpointtype") != null) {
                        monitortype = Integer.parseInt(gasOutputs.get(0).get("monitorpointtype").toString());
                    }
                }
            }
            //List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            paramMap.clear();
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
                paramMap.put("monitorpointtype", monitortype);
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
                param.put("monitorpointtype", monitortype);
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个废气排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneGasListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneGasListPageDataByDataMarkAndParams(
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个雨水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneGasListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneGasListDataByDataMarkAndParams(
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
            paramMap.put("pollutantcodes", pollutantcodes);

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
    @RequestMapping(value = "getManyGasListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyGasListPageDataByDataMarkAndParams(
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
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            if (outputids.size() > 0) {
                List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
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

                tabledata.put("tablelistdata", tableListData);
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多排口雨水排口列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyGasListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyGasListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            if (outputids.size() > 0) {

                Integer reportType = 2;
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
                paramMap.put("reportType", reportType);
                paramMap.put("pointType", monitorPointTypeCode);
                paramMap.put("pollutantType", monitorPointTypeCode);
                List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
                Map<String, Object> tabledata = new HashMap<>();
                tabledata.put("tablelistdata", tableListData);
                dataMap.put("tabledata", tabledata);
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }

            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个废气排口报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneGasOutPutReport", method = RequestMethod.POST)
    public void exportOneGasOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            if (monitorpointtype == null) {
                monitorpointtype = monitorPointTypeCode;
            }
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 1);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);

            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);

            //获取表格数据
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorpointtype, new HashMap<>());
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
            String fileName = "废气监测数据导出文件_" + new Date().getTime();
            if (monitorpointtype != null && monitorpointtype != monitorPointTypeCode) {
                fileName = "烟气监测数据导出文件_" + new Date().getTime();
            }
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个废气排口报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyGasOutPutReport", method = RequestMethod.POST)
    public void exportManyGasOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            if (monitorpointtype == null) {
                monitorpointtype = monitorPointTypeCode;
            }
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<Map<String, Object>> tableListData = new ArrayList<>();
            if (outputids.size() > 0) {
                //获取表格数据
                List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorpointtype, new HashMap<>());
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
                tableListData = onlineService.getTableListDataForReport(paramMap);
            }


            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "废气监测数据导出文件_" + new Date().getTime();
            if (monitorpointtype != null && monitorpointtype != monitorPointTypeCode) {
                fileName = "烟气监测数据导出文件_" + new Date().getTime();
            }
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
    @RequestMapping(value = "getGasOrPollutantFlowRankByParams", method = RequestMethod.POST)
    public Object getGasOrPollutantFlowRankByParams(@RequestJson(value = "datetype") String datetype,
                                                    @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                    @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                                    @RequestJson(value = "monitorpointtypes", required = false) List<String> monitorpointtypes,
                                                    @RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            if (monitorpointtype != null) {
                paramMap.put("type", monitorpointtype);
            } else {
                paramMap.put("monitorpointtypes", monitorpointtypes);
            }
            List<Map<String, Object>> allOutPutInfo = gasOutPutInfoService.getAllOutPutInfo(paramMap);
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
                Map<String, Object> timeMap = getTimeMapDay(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH");

                //去年同一天
                Map<String, Object> timeMapYear = getTimeMapYear(time, parse, format);
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
                Map<String, Object> timeMap = getTimeMapMonth(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");

                //去年同月
                Map<String, Object> timeMapYear = getTimeMapYear(time, parse, format);
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
                Map<String, Object> timeMap = getTimeMapQuarter(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH");


                //去年同季度
                Map<String, Object> timeMapYear = getTimeMapYear(time, parse, format);
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
                Map<String, Object> timeMap = getTimeMapYear(time, parse, format);
                flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                lastFlowData = mongoBaseService.getListByParam(flowDataVO, "YearFlowData", "yyyy-MM-dd HH");

                //去年同期时间设置
                lastYearFlowData = lastFlowData;
            }
        }

        //废气排放量
        if ("gasflow".equals(pollutantcode)) {
            getGasFlowInfo(allOutPutInfo, thisFlowData, lastFlowData, lastYearFlowData, resultList);
        } else {
            getPollutantFlowInfo(datetype, pollutantcode, allOutPutInfo, thisFlowData, lastFlowData, lastYearFlowData, resultList);
        }

        return resultList;
    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 10:55
     * @Description: 获取去年同期时间map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [time, parse, format]
     * @throws:
     */
    public static Map<String, Object> getTimeMapYear(Date time, Date parse, SimpleDateFormat format) {
        Map<String, Object> timeParam = new HashMap<>();
        Calendar lastInstance = Calendar.getInstance();
        lastInstance.setTime(time);
        lastInstance.add(Calendar.YEAR, -1);
        Date time1 = lastInstance.getTime();
        String lastEndTime = format.format(time1);
        lastInstance.setTime(parse);
        lastInstance.add(Calendar.YEAR, -1);
        Date time2 = lastInstance.getTime();
        String lastStartTime = format.format(time2);
        timeParam.put("starttime", lastStartTime);
        timeParam.put("endtime", lastEndTime);
        return timeParam;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 10:55
     * @Description: 获取上个季度时间map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [time, parse, format]
     * @throws:
     */
    public static Map<String, Object> getTimeMapQuarter(Date time, Date parse, SimpleDateFormat format) {
        Map<String, Object> timeParam = new HashMap<>();
        Calendar lastInstance = Calendar.getInstance();
        lastInstance.setTime(time);
        lastInstance.add(Calendar.MONTH, -3);
        Date time1 = lastInstance.getTime();
        String lastEndTime = format.format(time1);
        lastInstance.setTime(parse);
        lastInstance.add(Calendar.MONTH, -3);
        Date time2 = lastInstance.getTime();
        String lastStartTime = format.format(time2);
        timeParam.put("starttime", lastStartTime);
        timeParam.put("endtime", lastEndTime);
        return timeParam;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 10:55
     * @Description: 获取上个月时间map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [time, parse, format]
     * @throws:
     */
    public static Map<String, Object> getTimeMapMonth(Date time, Date parse, SimpleDateFormat format) {
        Map<String, Object> timeParam = new HashMap<>();
        Calendar lastInstance = Calendar.getInstance();
        lastInstance.setTime(time);
        lastInstance.add(Calendar.MONTH, -1);
        Date time1 = lastInstance.getTime();
        String lastEndTime = format.format(time1);
        lastInstance.setTime(parse);
        lastInstance.add(Calendar.MONTH, -1);
        Date time2 = lastInstance.getTime();
        String lastStartTime = format.format(time2);
        timeParam.put("starttime", lastStartTime);
        timeParam.put("endtime", lastEndTime);
        return timeParam;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 10:55
     * @Description: 获取昨天时间map
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [time, parse, format]
     * @throws:
     */
    public static Map<String, Object> getTimeMapDay(Date time, Date parse, SimpleDateFormat format) {
        Map<String, Object> timeParam = new HashMap<>();
        Calendar lastInstance = Calendar.getInstance();
        lastInstance.setTime(time);
        lastInstance.add(Calendar.DAY_OF_YEAR, -1);
        Date time1 = lastInstance.getTime();
        String lastEndTime = format.format(time1);
        lastInstance.setTime(parse);
        lastInstance.add(Calendar.DAY_OF_YEAR, -1);
        Date time2 = lastInstance.getTime();
        String lastStartTime = format.format(time2);
        timeParam.put("starttime", lastStartTime);
        timeParam.put("endtime", lastEndTime);
        return timeParam;
    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 11:15
     * @Description: 计算同比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [lastData, thisDaya]
     * @throws:
     */
    public static String getYearOnYear(Float lastFlow, Float thisFlow, String defaultstr) {
        DecimalFormat format = new DecimalFormat("0.##");
        /*//上一年数据为0
        if (lastFlow == 0) {
            if (thisFlow == 0) {
                return "-";
            } else {
                return "↑100";
            }
        }
        //今年数据为0
        if (thisFlow == 0) {
            if (lastFlow == 0) {
                return "-";
            } else {
                return "↓100";
            }
        }*/
        if (lastFlow == 0 || thisFlow == 0) {
            return defaultstr;
        }

        //都不为0计算同比
        float data = (thisFlow - lastFlow) / lastFlow * 100;

        if (data > 0) {
            return "↑" + format.format(Math.abs(data));
        } else if (data < 0) {
            return "↓" + format.format(Math.abs(data));
        } else {
            return format.format(data);
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/6/11 0011 上午 9:59
     * @Description: 获取占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [lastFlow, thisFlow, defaultstr]
     * @throws:
     */
    public static Object getYearOnYear(Float lastFlow, Float thisFlow, Object defaultstr) {
        DecimalFormat format = new DecimalFormat("0.##");
        if (lastFlow == 0 || thisFlow == 0) {
            return defaultstr;
        }

        //都不为0计算同比
        float data = (thisFlow - lastFlow) / lastFlow * 100;

        return format.format(data);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 下午 3:45
     * @Description: 获取废气排放信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutantcode, allOutPutInfo, thisFlowData, lastFlowData, resultList]
     * @throws:
     */
    public static void getGasFlowInfo(List<Map<String, Object>> allOutPutInfo, List<FlowDataVO> thisFlowData, List<FlowDataVO> lastFlowData, List<FlowDataVO> lastYearFlowData, List<Map<String, Object>> resultList) {

        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        //所有污染源排放量
        Float allFlow = thisFlowData.stream().filter(m -> StringUtils.isNotBlank(m.getTotalFlow())).map(m -> Float.valueOf(m.getTotalFlow())).reduce(0f, (a, b) -> a + b);
        //按污染源分组排口信息
        Map<String, List<Map<String, Object>>> collect = allOutPutInfo.stream().filter(m -> m.get("pollution") != null).collect(Collectors.groupingBy(m -> m.get("pollution").toString()));
        for (String pollutionname : collect.keySet()) {
            Map<String, Object> data = new HashMap<>();
            List<Map<String, Object>> list = collect.get(pollutionname);
            //某一个污染源当年排放量
            Float thisyearflow = 0f;
            //某一个污染源去年排放量
            Float lastflow = 0f;
            //某一个污染源去年排放量
            Float lastYearflow = 0f;
            //是否有数据标记
            Boolean thisflag = false;
            Boolean lastflag = false;
            Boolean lastYearflag = false;
            for (Map<String, Object> map : list) {
                if (map.get("DGIMN") != null) {
                    String dgimn = map.get("DGIMN").toString();
                    //某排口当年排放量
                    Float thisreduce = thisFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode()) && StringUtils.isNotBlank(m.getTotalFlow())).map(m -> Float.valueOf(m.getTotalFlow())).reduce(0f, (a, b) -> a + b);
                    //某排口上个周期排放量
                    Float lastreduce = lastFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode()) && StringUtils.isNotBlank(m.getTotalFlow())).map(m -> Float.valueOf(m.getTotalFlow())).reduce(0f, (a, b) -> a + b);
                    //某排口去年同期排放量
                    Float lastYearreduce = lastYearFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode()) && StringUtils.isNotBlank(m.getTotalFlow())).map(m -> Float.valueOf(m.getTotalFlow())).reduce(0f, (a, b) -> a + b);
                    thisyearflow += thisreduce;
                    lastflow += lastreduce;
                    lastYearflow += lastYearreduce;

                    Long collect1 = thisFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode()) && StringUtils.isNotBlank(m.getTotalFlow())).count();
                    Long collect2 = lastFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode()) && StringUtils.isNotBlank(m.getTotalFlow())).count();
                    Long collect3 = lastYearFlowData.stream().filter(m -> dgimn.equals(m.getDataGatherCode()) && StringUtils.isNotBlank(m.getTotalFlow())).count();
                    if (collect1 > 0) {
                        thisflag = true;
                    }
                    if (collect2 > 0) {
                        lastflag = true;
                    }
                    if (collect3 > 0) {
                        lastYearflag = true;
                    }
                }
            }

            data.put("pollutionname", pollutionname);

            data.put("proportion", decimalFormat.format(thisyearflow / allFlow * 100) + "%");
            if (allFlow == 0) {
                data.put("proportion", "0%");
            }

            if (thisflag) {
                data.put("thisyearflow", thisyearflow);
            } else {
                data.put("thisyearflow", "-");
            }
            if (lastflag) {
                data.put("lastyearflow", lastflow);
            } else {
                data.put("lastyearflow", "-");
            }
            if (thisflag && lastflag) {
                data.put("yearonyear", getYearOnYear(lastflow, thisyearflow, "-"));//环比
            } else {
                data.put("yearonyear", "-");
            }
            if (thisflag && lastYearflag) {
                data.put("ratio", getYearOnYear(lastYearflow, thisyearflow, "-"));//同比
            } else {
                data.put("ratio", "-");
            }
            if (data.size() > 0) {
                resultList.add(data);
            }
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 下午 5:29
     * @Description:获取污染物排放信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datetype, pollutantcode, allOutPutInfo, thisFlowData, lastFlowData, resultList]
     * @throws:
     */
    public static void getPollutantFlowInfo(String datetype, String pollutantcode, List<Map<String, Object>> allOutPutInfo, List<FlowDataVO> thisFlowData, List<FlowDataVO> lastFlowData, List<FlowDataVO> lastYearFlowData, List<Map<String, Object>> resultList) {

        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        //按污染源分组排口信息
        Map<String, List<Map<String, Object>>> collect = allOutPutInfo.stream().filter(m -> m.get("pollution") != null).collect(Collectors.groupingBy(m -> m.get("pollution").toString()));
        //所有污染物排放量
        Float allFlow = 0f;
        if ("day".equals(datetype)) {
            for (FlowDataVO thisDayFlowDatum : thisFlowData) {
                List<Map<String, Object>> dayFlowDataList = thisDayFlowDatum.getDayFlowDataList();
                Float reduce = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                        m.get("CorrectedFlow") != null).map(m -> Float.valueOf(m.get("CorrectedFlow").toString())).reduce(0f, (a, b) -> a + b);
                allFlow += reduce;
            }


            for (String pollutionname : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();

                List<Map<String, Object>> list = collect.get(pollutionname);
                //某一个污染源当年排放量
                Float thisyearflow = 0f;
                //某一个污染源去年排放量
                Float lastflow = 0f;
                //某一个污染源去年排放量
                Float lastYearflow = 0f;
                //是否有数据标记
                Boolean thisflag = false;
                Boolean lastflag = false;
                Boolean lastYearflag = false;
                for (Map<String, Object> map : list) {
                    if (map.get("DGIMN") != null) {
                        String dgimn = map.get("DGIMN").toString();

                        for (FlowDataVO thisDayFlowDatum : thisFlowData) {
                            if (dgimn.equals(thisDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> dayFlowDataList = thisDayFlowDatum.getDayFlowDataList();
                                Float thisreduce = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("CorrectedFlow") != null && StringUtils.isNotBlank(m.get("CorrectedFlow").toString())).
                                        map(m -> Float.valueOf(m.get("CorrectedFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("CorrectedFlow") != null && StringUtils.isNotBlank(m.get("CorrectedFlow").toString())).count();
                                if (count > 0) {
                                    thisflag = true;
                                }
                                thisyearflow += thisreduce;
                            }
                        }

                        for (FlowDataVO lastDayFlowDatum : lastFlowData) {
                            if (dgimn.equals(lastDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> dayFlowDataList = lastDayFlowDatum.getDayFlowDataList();
                                Float lastreduce = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("CorrectedFlow") != null && StringUtils.isNotBlank(m.get("CorrectedFlow").toString())).
                                        map(m -> Float.valueOf(m.get("CorrectedFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("CorrectedFlow") != null && StringUtils.isNotBlank(m.get("CorrectedFlow").toString())).count();
                                if (count > 0) {
                                    lastflag = true;
                                }
                                lastflow += lastreduce;
                            }
                        }

                        for (FlowDataVO lastDayFlowDatum : lastYearFlowData) {
                            if (dgimn.equals(lastDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> dayFlowDataList = lastDayFlowDatum.getDayFlowDataList();
                                Float lastreduce = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("CorrectedFlow") != null && StringUtils.isNotBlank(m.get("CorrectedFlow").toString())).
                                        map(m -> Float.valueOf(m.get("CorrectedFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = dayFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("CorrectedFlow") != null && StringUtils.isNotBlank(m.get("CorrectedFlow").toString())).count();
                                if (count > 0) {
                                    lastYearflag = true;
                                }
                                lastYearflow += lastreduce;
                            }
                        }
                    }
                }
                data.put("pollutionname", pollutionname);

                data.put("proportion", decimalFormat.format(thisyearflow / allFlow * 100) + "%");
                if (allFlow == 0) {
                    data.put("proportion", "0%");
                }

                if (thisflag) {
                    data.put("thisyearflow", thisyearflow);
                } else {
                    data.put("thisyearflow", "-");
                }
                if (lastflag) {
                    data.put("lastyearflow", lastflow);
                } else {
                    data.put("lastyearflow", "-");
                }
                if (thisflag && lastflag) {
                    data.put("yearonyear", getYearOnYear(lastflow, thisyearflow, "-"));
                } else {
                    data.put("yearonyear", "-");
                }
                if (thisflag && lastYearflag) {
                    data.put("ratio", getYearOnYear(lastYearflow, thisyearflow, "-"));
                } else {
                    data.put("ratio", "-");
                }
                if (data.size() > 0) {
                    resultList.add(data);
                }
            }


        } else if ("month".equals(datetype) || "quarter".equals(datetype)) {
            for (FlowDataVO thisDayFlowDatum : thisFlowData) {
                List<Map<String, Object>> monthFlowDataList = thisDayFlowDatum.getMonthFlowDataList();
                Float reduce = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                        m.get("PollutantFlow") != null).map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                allFlow += reduce;
            }
            for (String pollutionname : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> list = collect.get(pollutionname);
                //某一个污染源当年排放量
                Float thisyearflow = 0f;
                //某一个污染源去年排放量
                Float lastflow = 0f;
                //某一个污染源去年排放量
                Float lastYearflow = 0f;
                //是否有数据标记
                Boolean thisflag = false;
                Boolean lastflag = false;
                Boolean lastYearflag = false;
                for (Map<String, Object> map : list) {
                    if (map.get("DGIMN") != null) {
                        String dgimn = map.get("DGIMN").toString();

                        for (FlowDataVO thisDayFlowDatum : thisFlowData) {
                            if (dgimn.equals(thisDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> monthFlowDataList = thisDayFlowDatum.getMonthFlowDataList();
                                Float thisreduce = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).map(m -> Float.valueOf(m.get("PollutantFlow").
                                        toString())).reduce(0f, (a, b) -> a + b);
                                long count = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).count();
                                if (count > 0) {
                                    thisflag = true;
                                }
                                thisyearflow += thisreduce;
                            }
                        }

                        for (FlowDataVO lastDayFlowDatum : lastFlowData) {
                            if (dgimn.equals(lastDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> monthFlowDataList = lastDayFlowDatum.getMonthFlowDataList();
                                Float lastreduce = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                                        m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).
                                        map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                                        m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).count();
                                if (count > 0) {
                                    lastflag = true;
                                }
                                lastflow += lastreduce;
                            }
                        }
                        for (FlowDataVO lastDayFlowDatum : lastYearFlowData) {
                            if (dgimn.equals(lastDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> monthFlowDataList = lastDayFlowDatum.getMonthFlowDataList();
                                Float lastreduce = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                                        m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).
                                        map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = monthFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                                        m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).count();
                                if (count > 0) {
                                    lastYearflag = true;
                                }
                                lastYearflow += lastreduce;
                            }
                        }

                    }
                }
                data.put("pollutionname", pollutionname);

                data.put("proportion", decimalFormat.format(thisyearflow / allFlow * 100) + "%");
                if (allFlow == 0) {
                    data.put("proportion", "0%");
                }

                if (thisflag) {
                    data.put("thisyearflow", thisyearflow);
                } else {
                    data.put("thisyearflow", "-");
                }
                if (lastflag) {
                    data.put("lastyearflow", lastflow);
                } else {
                    data.put("lastyearflow", "-");
                }
                if (thisflag && lastflag) {
                    data.put("yearonyear", getYearOnYear(lastflow, thisyearflow, "-"));
                } else {
                    data.put("yearonyear", "-");
                }
                if (thisflag && lastYearflag) {
                    data.put("ratio", getYearOnYear(lastYearflow, thisyearflow, "-"));
                } else {
                    data.put("ratio", "-");
                }
                if (data.size() > 0) {
                    resultList.add(data);
                }
            }
        } else if ("year".equals(datetype)) {
            for (FlowDataVO thisDayFlowDatum : thisFlowData) {
                List<Map<String, Object>> yearFlowDataList = thisDayFlowDatum.getYearFlowDataList();
                Float reduce = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString()) &&
                        m.get("PollutantFlow") != null).map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                allFlow += reduce;
            }
            for (String pollutionname : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> list = collect.get(pollutionname);
                //某一个污染源当年排放量
                Float thisyearflow = 0f;
                //某一个污染源去年排放量
                Float lastflow = 0f;
                Float lastYearflow = 0f;
                //是否有数据标记
                Boolean thisflag = false;
                Boolean lastflag = false;
                Boolean lastYearflag = false;
                for (Map<String, Object> map : list) {
                    if (map.get("DGIMN") != null) {
                        String dgimn = map.get("DGIMN").toString();

                        for (FlowDataVO thisDayFlowDatum : thisFlowData) {
                            if (dgimn.equals(thisDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> yearFlowDataList = thisDayFlowDatum.getYearFlowDataList();
                                Float thisreduce = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).
                                        map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).count();
                                if (count > 0) {
                                    thisflag = true;
                                }
                                thisyearflow += thisreduce;
                            }
                        }

                        for (FlowDataVO lastDayFlowDatum : lastFlowData) {
                            if (dgimn.equals(lastDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> yearFlowDataList = lastDayFlowDatum.getYearFlowDataList();
                                Float lastreduce = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).
                                        map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).count();
                                if (count > 0) {
                                    lastflag = true;
                                }
                                lastflow += lastreduce;
                            }
                        }
                        for (FlowDataVO lastDayFlowDatum : lastYearFlowData) {
                            if (dgimn.equals(lastDayFlowDatum.getDataGatherCode())) {
                                List<Map<String, Object>> yearFlowDataList = lastDayFlowDatum.getYearFlowDataList();
                                Float lastreduce = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).
                                        map(m -> Float.valueOf(m.get("PollutantFlow").toString())).reduce(0f, (a, b) -> a + b);
                                long count = yearFlowDataList.stream().filter(m -> m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())
                                        && m.get("PollutantFlow") != null && StringUtils.isNotBlank(m.get("PollutantFlow").toString())).count();
                                if (count > 0) {
                                    lastYearflag = true;
                                }
                                lastYearflow += lastreduce;
                            }
                        }

                    }
                }
                data.put("pollutionname", pollutionname);

                data.put("proportion", decimalFormat.format(thisyearflow / allFlow * 100) + "%");
                if (allFlow == 0) {
                    data.put("proportion", "0%");
                }

                if (thisflag) {
                    data.put("thisyearflow", thisyearflow);
                } else {
                    data.put("thisyearflow", "-");
                }
                if (lastflag) {
                    data.put("lastyearflow", lastflow);
                } else {
                    data.put("lastyearflow", "-");
                }
                if (thisflag && lastflag) {
                    data.put("yearonyear", getYearOnYear(lastflow, thisyearflow, "-"));
                } else {
                    data.put("yearonyear", "-");
                }
                if (thisflag && lastYearflag) {
                    data.put("ratio", getYearOnYear(lastYearflow, thisyearflow, "-"));
                } else {
                    data.put("ratio", "-");
                }
                if (data.size() > 0) {
                    resultList.add(data);
                }
            }
        }


    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:24
     * @Description: 获取废气污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getGasPollutantEarlyAndOverAlarmsByParamMap(
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
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
            if (monitorpointtype == null) {
                monitorpointtype = monitorPointTypeCode;
            }
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> outputs = gasOutPutInfoService.getGasOutPutAndStatusByParamMap(paramMap);
            paramMap.put("pointtype", monitorpointtype);
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
     * @Description: 获取废气污染物预警/超标/异常详情页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getGasPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getGasPollutantEarlyOrOverOrExceptionDetailsPage(

            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "datatype") List<String> datatypes,
            @RequestJson(value = "counttype") Integer counttype,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "pagesize", required = false) Integer pageSize,
            @RequestJson(value = "pagenum", required = false) Integer pageNum) {
        Map<String, Object> dataMap = new HashMap<>();
        try {

            if (monitorpointtype == null) {
                monitorpointtype = monitorPointTypeCode;
            }

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
    @RequestMapping(value = "exportGasPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportGasPollutantEarlyOrOverOrExceptionDetailsData(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "datatype") List<String> datatypes,
            @RequestJson(value = "counttype") Integer counttype, HttpServletResponse response, HttpServletRequest request) throws IOException {
        try {
            if (monitorpointtype == null) {
                monitorpointtype = monitorPointTypeCode;
            }

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
            String fileName = "废气污染物报警详情导出文件_" + new Date().getTime();
            if (monitorpointtype != null && monitorpointtype != monitorPointTypeCode) {
                fileName = "烟气污染物报警详情导出文件_" + new Date().getTime();
            }
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取废气多排口多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getGasManyOutPutManyPollutantMonitorDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            if (outputids.size() > 0) {
                Map<String, String> outPutIdAndMn = new HashMap<>();
                if (monitorpointtype == null) {
                    monitorpointtype = monitorPointTypeCode;
                }
                List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorpointtype, outPutIdAndMn);
                Map<String, String> idAndName = onlineService.getOutPutIdAndPollution(outputids, monitorpointtype);
                Map<String, String> codeAndName = onlineService.getPollutantCodeAndName(pollutantcodes, monitorpointtype);
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
                charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(documents, pollutantcodes,
                        collection, outPutIdAndMn, outputids, idAndName, codeAndName);
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
     * @Description: 自定义查询条件查询废气重点监控污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasKeyPollutantDataByParams", method = RequestMethod.POST)
    public Object getGasKeyPollutantDataByParams(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            if (paramMap.get("datatype") != null) {
                Integer datatype = Integer.parseInt(paramMap.get("datatype").toString());
                String collection = MongoDataUtils.getCollectionByDataMark(datatype);
                List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
                if (paramMap.get("starttime") != null && StringUtils.isNotBlank(paramMap.get("starttime").toString())) {
                    String starttime = MongoDataUtils.setStartTimeByDataMark(datatype, paramMap.get("starttime").toString());
                    paramMap.put("starttime", starttime);
                }
                if (paramMap.get("endtime") != null && StringUtils.isNotBlank(paramMap.get("endtime").toString())) {
                    String endtime = MongoDataUtils.setEndTimeByDataMark(datatype, paramMap.get("endtime").toString());
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("collection", collection);
                resultMap = onlineService.getKeyPollutantMonitorDataByParamMap(outPuts, monitorPointTypeCode, paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废气污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getGasPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            /*if (paramMap.get("starttime") != null && StringUtils.isNotBlank(paramMap.get("starttime").toString())) {
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
            }*/
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
     * @Description: 自定义查询条件导出废气污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportGasPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportGasPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
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
            String fileName = "废气污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 下午 3:14
     * @Description: 通过监测时间，dgimn号集合，单个污染物查询在线监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDataByParams", method = RequestMethod.POST)
    public Object getOnlineDataByParams(@RequestJson(value = "dgimns", required = true) List<String> dgimns,
                                        @RequestJson(value = "datetype", required = true) String datetype,
                                        @RequestJson(value = "starttime", required = false) String starttime,
                                        @RequestJson(value = "endtime", required = false) String endtime,
                                        @RequestJson(value = "pollutantcode", required = true) String pollutantcode) throws Exception {
        try {
            Object onlineDataByParams = getOnlineDataByParams(dgimns, datetype, starttime, endtime, Arrays.asList(pollutantcode));

            return AuthUtil.parseJsonKeyToLower("success", onlineDataByParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 下午 3:14
     * @Description: 通过监测时间，dgimn号集合，多个污染物查询在线监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getOnlineDatasByParams", method = RequestMethod.POST)
    public Object getOnlineDatasByParams(@RequestJson(value = "dgimns", required = true) List<String> dgimns,
                                         @RequestJson(value = "datetype", required = true) String datetype,
                                         @RequestJson(value = "starttime", required = false) String starttime,
                                         @RequestJson(value = "endtime", required = false) String endtime,
                                         @RequestJson(value = "pollutantcodes", required = true) List<String> pollutantcodes) throws Exception {
        try {

            Object onlineDataByParams = getOnlineDataByParams(dgimns, datetype, starttime, endtime, pollutantcodes);

            return AuthUtil.parseJsonKeyToLower("success", onlineDataByParams);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 下午 3:14
     * @Description: 通过监测时间，dgimn号集合，多个污染物查询在线监测列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorOnlineDataListByParams", method = RequestMethod.POST)
    public Object getOtherMonitorOnlineDataListByParams(@RequestJson(value = "dgimns", required = true) List<String> dgimns,
                                                        @RequestJson(value = "datetype", required = true) String datetype,
                                                        @RequestJson(value = "starttime", required = false) String starttime,
                                                        @RequestJson(value = "endtime", required = false) String endtime,
                                                        @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                        @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                        @RequestJson(value = "pollutantcodes", required = true) List<String> pollutantcodes) throws Exception {
        try {

            OnlineDataVO onlineDataVO = new OnlineDataVO();
            DecimalFormat format = new DecimalFormat("0.##");
            onlineDataVO.setDataGatherCode(StringUtils.join(dgimns, ","));
            List<OnlineDataVO> listByParam = new ArrayList();
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            String pattern = "";
            String collects = "";
            List<Map<String, Object>> allMonitorPoint = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(new HashMap<>());

            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            if (pagenum != null && pagesize != null) {
                mongoSearchEntity.setPage(pagenum);
                mongoSearchEntity.setSize(pagesize);
            }

            if ("hour".equals(datetype)) {
                pattern = "yyyy-MM-dd HH";
                collects = "HourData";
            } else if ("day".equals(datetype)) {
                pattern = "yyyy-MM-dd";
                collects = "DayData";
            } else if ("minute".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm";
                collects = "MinuteData";
            } else if ("realtime".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
                collects = "RealTimeData";
            }
            listByParam = mongoBaseService.getListWithPageByParam(onlineDataVO, mongoSearchEntity, collects, pattern);
            long count = mongoBaseService.getCount(onlineDataVO, collects, pattern);
            for (OnlineDataVO dataVO : listByParam) {
                String dataGatherCode = dataVO.getDataGatherCode();
                Map<String, Object> dataMap = new HashMap<>();
                List<Object> collect1 = allMonitorPoint.stream().filter(m -> m.get("DGIMN") != null && dataGatherCode.equals(m.get("DGIMN").toString())).map(m -> m.get("MonitorPointName")).collect(Collectors.toList());
                if (collect1.size() > 0) {
                    dataMap.put("monitorname", collect1.get(0));
                } else {
                    dataMap.put("monitorname", "");
                }
                dataMap.put("monitortime", OverAlarmController.format(dataVO.getMonitorTime(), pattern));
                List<Map<String, Object>> data = dataVO.getDayDataList() == null ? (dataVO.getHourDataList() == null ? (dataVO.getMinuteDataList() == null ? dataVO.getRealDataList() : dataVO.getMinuteDataList()) : dataVO.getHourDataList()) : dataVO.getDayDataList();
                for (String pollutantcode : pollutantcodes) {
                    List<String> collect = data.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString())).map(m -> m.get("AvgStrength").toString()).collect(Collectors.toList());
                    if (collect.size() > 0) {
                        dataMap.put(pollutantcode, format.format(Double.valueOf(collect.get(0))));
                    } else {
                        dataMap.put(pollutantcode, "");
                    }
                }
                resultList.add(dataMap);
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("datalist", resultList);
            resultMap.put("total", count);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/6 0006 下午 1:19
     * @Description: 通过监测时间，dgimn号集合，多个污染物导出在线监测列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, datetype, starttime, endtime, pagesize, pagenum, pollutantcodes]
     * @throws:
     */
    @RequestMapping(value = "exportOtherMonitorOnlineDataListByParams", method = RequestMethod.POST)
    public void exportOtherMonitorOnlineDataListByParams(@RequestJson(value = "dgimns", required = true) List<String> dgimns,
                                                         @RequestJson(value = "datetype", required = true) String datetype,
                                                         @RequestJson(value = "monitortype", required = true) Integer monitortype,
                                                         @RequestJson(value = "starttime", required = false) String starttime,
                                                         @RequestJson(value = "endtime", required = false) String endtime,
                                                         HttpServletRequest request, HttpServletResponse response,
                                                         @RequestJson(value = "pollutantcodes", required = true) List<String> pollutantcodes) throws Exception {
        try {
            Object otherMonitorOnlineDataListByParams = getOtherMonitorOnlineDataListByParams(dgimns, datetype, starttime, endtime, Integer.MAX_VALUE, 1, pollutantcodes);
            JSONObject jsonObject = JSONObject.fromObject(otherMonitorOnlineDataListByParams);
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object datalist = jsonObject1.get("datalist");


            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", monitortype);
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);
            List<String> pollutantnames = new ArrayList<>();
            for (String pollutantcode : pollutantcodes) {
                List<String> collect = dataList.stream().filter(m -> m.get("pollutantcode") != null && m.get("pollutantname") != null && pollutantcode.equals(m.get("pollutantcode").toString())).map(m -> m.get("pollutantname").toString()).collect(Collectors.toList());
                if (collect.size() > 0) {
                    pollutantnames.add(collect.get(0));
                }

            }


            String pattern = "";
            if ("hour".equals(datetype)) {
                pattern = "yyyy-MM-dd HH";
            } else if ("day".equals(datetype)) {
                pattern = "yyyy-MM-dd";
            } else if ("minute".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm";
            } else if ("realtime".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
            }

            String sheetname = "";
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {
                sheetname = "恶臭污染统计分析";
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                sheetname = "voc污染统计分析";
            }
            List<String> headers = new ArrayList<>();
            headers.add("站点名称");
            headers.add("监测时间");
            headers.addAll(pollutantnames);

            List<String> headersField = new ArrayList<>();
            headersField.add("monitorname");
            headersField.add("monitortime");
            headersField.addAll(pollutantcodes);
            if (datalist != null) {
                JSONArray jsonArray = JSONArray.fromObject(datalist);
                HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, pattern);
                byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
                ExcelUtil.downLoadExcel(sheetname, response, request, bytesForWorkBook);

            }


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 下午 4:52
     * @Description: 通过mn号集合，日期类型，监测时间，污染物集合获取在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimns, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    private Object getOnlineDataByParams(List<String> dgimns, String datetype, String starttime, String endtime, List<String> pollutantcode) throws Exception {
        try {
            OnlineDataVO onlineDataVO = new OnlineDataVO();
            DecimalFormat format = new DecimalFormat("0.#");
            onlineDataVO.setDataGatherCode(StringUtils.join(dgimns, ","));

            List<OnlineDataVO> listByParam = new ArrayList();
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            String pattern = "";
            if ("hour".equals(datetype)) {
                pattern = "yyyy-MM-dd HH";
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "HourData", pattern);
            } else if ("day".equals(datetype)) {
                pattern = "yyyy-MM-dd";
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "DayData", pattern);
            } else if ("minute".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm";
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "MinuteData", pattern);
            } else if ("realtime".equals(datetype)) {
                pattern = "yyyy-MM-dd HH:mm:ss";
                onlineDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
                listByParam = mongoBaseService.getListByParam(onlineDataVO, "RealTimeData", pattern);
            }

            Map<String, List<OnlineDataVO>> collect1 = listByParam.stream().collect(Collectors.groupingBy(m -> m.getDataGatherCode()));

            for (String dgimn : collect1.keySet()) {
                List<OnlineDataVO> onlineDataVOS = collect1.get(dgimn);
                Map<String, Object> resultMap = new HashMap<>();
                List<Map<String, Object>> pollutantdata = new ArrayList<>();
                resultMap.put("dgimn", dgimn);
                resultList.add(resultMap);
                for (String code : pollutantcode) {

                    Map<String, Object> pollutant = new HashMap<>();
                    List<Map<String, Object>> datalist = new ArrayList<>();
                    pollutantdata.add(pollutant);
                    pollutant.put("pollutantcode", code);
                    pollutant.put("datalist", datalist);
                    resultMap.put("pollutantdata", pollutantdata);
                    for (OnlineDataVO dataVO : onlineDataVOS) {
                        String monitorTime = dataVO.getMonitorTime();
                        List<Map<String, Object>> data = dataVO.getDayDataList() == null ? (dataVO.getHourDataList() == null ? (dataVO.getMinuteDataList() == null ? dataVO.getRealDataList() : dataVO.getMinuteDataList()) : dataVO.getHourDataList()) : dataVO.getDayDataList();
                        List<Map<String, Object>> collect = data.stream().filter(m -> m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString())).collect(Collectors.toList());
                        Map<String, Object> map = new HashMap<>();
                        if (collect.size() > 0) {
                            Map<String, Object> map1 = collect.get(0);
                            Object monitorvalue = map1.get("AvgStrength") == null ? map1.get("MonitorValue") : map1.get("AvgStrength");
                            if (monitorvalue != null) {
                                map.put("monitorvalue", format.format(Float.valueOf(monitorvalue.toString())));
                            } else {
                                map.put("monitorvalue", "");
                            }
                            map.put("monitortime", OverAlarmController.format(monitorTime, pattern));
                            datalist.add(map);
                        }
                    }
                }
            }

            for (int i = 0; i < resultList.size(); i++) {
                Map<String, Object> map = resultList.get(i);
                List<Map<String, Object>> pollutantdata = (List) map.get("pollutantdata");
                for (int j = 0; j < pollutantdata.size(); j++) {
                    Map<String, Object> map1 = pollutantdata.get(j);
                    List<Map<String, Object>> datalist = (List) map1.get("datalist");
                    List<Map<String, Object>> collect = datalist.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
                    map1.put("datalist", collect);
                }
            }

            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废气相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasRelationListDataByParams", method = RequestMethod.POST)
    public Object getGasRelationListDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                 @RequestJson(value = "monitorpointmn") String monitorpointmn,
                                                 @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "outputpollutant") String outputpollutant,
                                                 @RequestJson(value = "beforetime") Integer beforetime,
                                                 @RequestJson(value = "pagesize") Integer pagesize,
                                                 @RequestJson(value = "pagenum") Integer pagenum

    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            String beforestarttime = "";
            String beforeendtime = "";
            String relationstarttime = "";
            String relationendtime = "";
            if (collection.indexOf("Hour") > -1) {//小时数据
                starttime = starttime + ":00:00";
                relationstarttime = DataFormatUtil.getBeforeByHourTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00:00";

                endtime = endtime + ":59:59";
                relationendtime = DataFormatUtil.getBeforeByHourTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59:59";

            } else if (collection.indexOf("Minute") > -1) {//分钟数据
                starttime = starttime + ":00";
                relationstarttime = DataFormatUtil.getBeforeByMinuteTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00";

                endtime = endtime + ":59";
                relationendtime = DataFormatUtil.getBeforeByMinuteTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59";
            }
            paramMap.put("beforetime", beforetime);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", beforestarttime);
            paramMap.put("relationstarttime", relationstarttime);
            paramMap.put("relationendtime", relationendtime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", beforeendtime);
            paramMap.put("monitorpointmn", monitorpointmn);
            paramMap.put("monitorpointpollutant", monitorpointpollutant);
            paramMap.put("outputpollutant", outputpollutant);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            resultMap = onlineService.getGasRelationListDataByParamMap(outPuts, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件导出废气相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportGasRelationListDataByParams", method = RequestMethod.POST)
    public void exportGasRelationListDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                  @RequestJson(value = "monitorpointmn") String monitorpointmn,
                                                  @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "outputpollutant") String outputpollutant,
                                                  @RequestJson(value = "beforetime") Integer beforetime, HttpServletResponse response,
                                                  HttpServletRequest request

    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            String beforestarttime = "";
            String beforeendtime = "";
            String relationstarttime = "";
            String relationendtime = "";
            if (collection.indexOf("Hour") > -1) {//小时数据
                starttime = starttime + ":00:00";
                relationstarttime = DataFormatUtil.getBeforeByHourTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00:00";

                endtime = endtime + ":59:59";
                relationendtime = DataFormatUtil.getBeforeByHourTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59:59";

            } else if (collection.indexOf("Minute") > -1) {//分钟数据
                starttime = starttime + ":00";
                relationstarttime = DataFormatUtil.getBeforeByMinuteTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00";

                endtime = endtime + ":59";
                relationendtime = DataFormatUtil.getBeforeByMinuteTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59";
            }
            paramMap.put("beforetime", beforetime);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", beforestarttime);
            paramMap.put("relationstarttime", relationstarttime);
            paramMap.put("relationendtime", relationendtime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", beforeendtime);
            paramMap.put("monitorpointmn", monitorpointmn);
            paramMap.put("monitorpointpollutant", monitorpointpollutant);
            paramMap.put("outputpollutant", outputpollutant);

            resultMap = onlineService.getGasRelationListDataByParamMap(outPuts, paramMap);
            List<Map<String, Object>> tablelistdata = resultMap.get("tablelistdata") != null ? (List<Map<String, Object>>) resultMap.get("tablelistdata") : null;
            //设置导出文件数据格式
            List<String> headers = Arrays.asList("企业名称", "排放口名称", "相关开始时间", "相关结束时间", "相关度");
            List<String> headersField = Arrays.asList("pollutionname", "outputname", "relationstarttime", "relationendtime", "relationpercent");
            //设置文件名称
            String fileName = "相关性分析导出列表文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废气相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasRelationChartDataByParams", method = RequestMethod.POST)
    public Object getGasRelationChartDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                  @RequestJson(value = "outputid") String outputid,
                                                  @RequestJson(value = "monitorpointmn") String monitorpointmn,
                                                  @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "outputpollutant") String outputpollutant,
                                                  @RequestJson(value = "beforetime", required = false) Integer beforetime,
                                                  @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                  @RequestJson(value = "pagenum", required = false) Integer pagenum


    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", Arrays.asList(outputid));
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            String beforestarttime = "";
            String beforeendtime = "";
            String relationstarttime = "";
            String relationendtime = "";
            if (collection.indexOf("Day") > -1) {//日数据
                starttime = starttime + " 00:00:00";
                beforestarttime = starttime;
                endtime = endtime + " 23:59:59";
                beforeendtime = endtime;
            } else if (collection.indexOf("Hour") > -1) {//小时数据
                starttime = starttime + ":00:00";
                relationstarttime = DataFormatUtil.getBeforeByHourTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00:00";
                endtime = endtime + ":59:59";
                relationendtime = DataFormatUtil.getBeforeByHourTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59:59";

            } else if (collection.indexOf("Minute") > -1) {//分钟数据
                starttime = starttime + ":00";
                relationstarttime = DataFormatUtil.getBeforeByMinuteTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00";

                endtime = endtime + ":59";
                relationendtime = DataFormatUtil.getBeforeByMinuteTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59";
            }
            paramMap.put("beforetime", beforetime);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", beforestarttime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", beforeendtime);
            paramMap.put("monitorpointmn", monitorpointmn);
            paramMap.put("monitorpointpollutant", monitorpointpollutant);
            paramMap.put("outputpollutant", outputpollutant);
            if (pagesize != null && pagenum != null) {
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
            }
            resultMap = onlineService.getGasRelationChartDataByParamMap(outPuts, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废气相关性详情列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasRelationListDetailDataByParams", method = RequestMethod.POST)
    public Object getGasRelationListDetailDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                       @RequestJson(value = "outputid") String outputid,
                                                       @RequestJson(value = "monitorpointmn") String monitorpointmn,
                                                       @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
                                                       @RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "endtime") String endtime,
                                                       @RequestJson(value = "outputpollutant") String outputpollutant,
                                                       @RequestJson(value = "beforetime") Integer beforetime
    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", Arrays.asList(outputid));
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            List<Map<String, Object>> outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            String beforestarttime = "";
            String beforeendtime = "";
            String relationstarttime = "";
            String relationendtime = "";
            if (collection.indexOf("Hour") > -1) {//小时数据
                starttime = starttime + ":00:00";
                relationstarttime = DataFormatUtil.getBeforeByHourTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00:00";

                endtime = endtime + ":59:59";
                relationendtime = DataFormatUtil.getBeforeByHourTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59:59";

            } else if (collection.indexOf("Minute") > -1) {//分钟数据
                starttime = starttime + ":00";
                relationstarttime = DataFormatUtil.getBeforeByMinuteTime(beforetime, starttime);
                beforestarttime = relationstarttime + ":00";

                endtime = endtime + ":59";
                relationendtime = DataFormatUtil.getBeforeByMinuteTime(beforetime, endtime);
                beforeendtime = relationendtime + ":59";
            }
            paramMap.put("beforetime", beforetime);
            paramMap.put("collection", collection);
            paramMap.put("starttime", starttime);
            paramMap.put("beforestarttime", beforestarttime);
            paramMap.put("endtime", endtime);
            paramMap.put("beforeendtime", beforeendtime);
            paramMap.put("monitorpointmn", monitorpointmn);
            paramMap.put("monitorpointpollutant", monitorpointpollutant);
            paramMap.put("outputpollutant", outputpollutant);

            resultMap = onlineService.getGasRelationChartDataByParamMap(outPuts, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
    @RequestMapping(value = "getGasChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getGasChangeWarnDetailParams(@RequestJson(value = "dgimn", required = false) String dgimn,
                                               @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
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
     * @Description: 获取废气浓度突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getChangeWarnPollutantInfo", method = RequestMethod.POST)
    public Object getChangeWarnPollutantInfo(@RequestJson(value = "dgimn", required = false) String dgimn,
                                             @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                             @RequestJson(value = "starttime") String starttime,
                                             @RequestJson(value = "monitortype") Integer monitortype,
                                             @RequestJson(value = "endtime") String endtime
    ) {
        try {
            if (WasteGasEnum.getCode() == monitortype || SmokeEnum.getCode() == monitortype) {
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
            } else {
                return AuthUtil.parseJsonKeyToLower("success", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:38
     * @Description: 获取废气单个污染物浓度突增数据
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
                                                    @RequestJson(value = "monitortype", required = false) Integer monitortype,
                                                    @RequestJson(value = "endtime") String endtime,
                                                    @RequestJson(value = "pollutantcode") String pollutantcode,
                                                    @RequestJson(value = "pollutants", required = false) Object pollutants
    ) {
        try {
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> result;
            if (monitortype != null && CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() == monitortype && pollutants != null) {

                Map<String, Integer> codeAndIs = (Map<String, Integer>) pollutants;
                result = onlineService.getSmokePollutantUpRushDischargeInfo(starttime, endtime, remindtype, dgimn, codeAndIs, collectiontype);
            } else {
                result = onlineService.getPollutantUpRushDischargeInfo(starttime, endtime, remindtype, dgimn, pollutantcode, collectiontype);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

//    /**
//     * @author: chengzq
//     * @date: 2019/7/11 0011 上午 11:23
//     * @Description: 导出废气浓度突增数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [pollutionname, starttime, outputname, endtime, pagesize, pagenum]
//     * @throws:
//     */
//    @RequestMapping(value = "exportGasChangeWarnListByParams", method = RequestMethod.POST)
//    public void exportGasChangeWarnListByParams(@RequestJson(value = "pollutionname", required = false) String pollutionname, @RequestJson(value = "starttime") String starttime,
//                                                @RequestJson(value = "outputname", required = false) String outputname, @RequestJson(value = "endtime") String endtime,
//                                                @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "pagenum", required = false) Integer pagenum,
//                                                HttpServletResponse response, HttpServletRequest request
//    ) throws Exception {
//        try {
//
//            Object gasChangeWarnListByParams = getGasChangeWarnListByParams(pollutionname, starttime, outputname, endtime, Integer.MAX_VALUE, 1);
//
//            JSONObject jsonObject = JSONObject.fromObject(gasChangeWarnListByParams);
//            Object data = jsonObject.get("data");
//            JSONObject jsonObject1 = JSONObject.fromObject(data);
//            Object datalist = jsonObject1.get("datalist");
//
//            JSONArray jsonArray = JSONArray.fromObject(datalist);
//
//            Object collect = jsonArray.stream().filter(m -> ((Map) m).get("timepoints") != null).peek(m -> {
//                Object timepoints = ((Map) m).get("timepoints");
//                List<Integer> integers = JSONArray.fromObject(timepoints);
//                List<List<Integer>> lists = groupIntegerList(integers);
//                String line = getLine(lists);
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
//                ExcelUtil.downLoadExcel("废气浓度突变预警", response, request, bytesForWorkBook);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }


    /**
     * @author: chengzq
     * @date: 2019/7/11 0011 上午 11:45
     * @Description: 将一个集合中的所有数字根据是否连续分成多个集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    public static List<List<Integer>> groupIntegerList(List<Integer> list) {
        List<List<Integer>> datas = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        Integer temp = null;
        for (int i = 0; i < list.size(); i++) {
            Integer integer = list.get(i);
            if (temp != null && integer - temp > 1) {
                datas.add(data);
                data = new ArrayList<>();
            }
            if (list.size() - 1 == i) {
                datas.add(data);
            }

            data.add(integer);
            temp = integer;
        }
        return datas;
    }


    /**
     * @author: chengzq
     * @date: 2019/7/11 0011 上午 11:55
     * @Description: 转换字符串
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    public static String getLine(List<List<Integer>> list) {
        String line = "";
        for (List<Integer> integers : list) {
            if (integers.size() > 0) {
                List<String> data = new ArrayList<>();
                Integer integer = integers.get(0);
                Integer integer1 = integers.get(integers.size() - 1);
                data.add(integer + "时");
                data.add(integer1 + "时");
                String collect = data.stream().distinct().collect(Collectors.joining("-"));
                line += collect + "、";
            }
        }
        return line;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/11 15:52
     * @Description: 获取废气排放量突增污染物信息
     * @param: dgimn, starttime, endtime
     * @return:
     */
    @RequestMapping(value = "getGasDischargeUpRushPollutantInfo", method = RequestMethod.POST)
    public Object getGasDischargeUpRushPollutantInfo(@RequestJson(value = "dgimn", required = false) String dgimn,
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
    @RequestMapping(value = "getGasPollutantUpRushDischargeInfo", method = RequestMethod.POST)
    public Object getGasPollutantUpRushDischargeInfo(@RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode
    ) {
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
     * @Description: 条件查询废气排口下各个污染物排放量信息列表
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasAbruptPollutantsByParam", method = RequestMethod.POST)
    public Object getGasAbruptPollutantsByParam(@RequestJson(value = "endtime") String endtime,
                                                @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
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
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:24
     * @Description: 根据监测年份导出废气废水排放量许可预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPermittedFlowEarly", method = RequestMethod.POST)
    public void exportPermittedFlowEarly(@RequestJson(value = "year") String year, @RequestJson(value = "monitorpointtype") Integer monitorpointtype, HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            List<Map<String, Object>> tabletitledata = gasOutPutInfoService.getTableTitleForGasPermittedFlowEarly();
            //获取表格数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("flowyear", year);
            paramMap.put("monitorpointtype", monitorpointtype);
            //根据年份获取各企业的排放许可限值
            List<Map<String, Object>> allflowvalues = entPermittedFlowLimitValueService.getEntPermittedFlowLimitInfoByYearAndType(paramMap);
            //根据年份和类型获取配置有排放量许可预警值的所有企业下排口的MN号
            List<Map<String, Object>> allMN = entPermittedFlowLimitValueService.getAllDgimnsByYearAndType(paramMap);
            //从MongoDB中查询因子排放量信息，并统计计算，拼接后返回列表信息
            List<Map<String, Object>> resultlist = gasOutPutInfoService.getFlowPermitListData(allflowvalues, allMN, year);
            List<Map<String, Object>> collect = resultlist.stream().sorted(Comparator.comparing(m -> m.get("surplusflowday").toString())).collect(Collectors.toList());
            //处理分页数据
            /*if (pagenum != null && pagesize != null) {
                resultlist = getPageData(resultlist, pagenum, pagesize);
            }*/
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "";
            if (monitorpointtype.equals(WasteGasEnum.getCode())) {//废气
                fileName = "废气排放量许可预警导出文件_" + new Date().getTime();
            } else if (monitorpointtype.equals(CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode())) {//废水
                fileName = "废水排放量许可预警导出文件_" + new Date().getTime();
            } else if (monitorpointtype.equals(SmokeEnum.getCode())) {//废气
                fileName = "烟气排放量许可预警导出文件_" + new Date().getTime();
            }
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//    /**
//     * @author: zhangzc
//     * @date: 2019/7/13 10:52 导出废水排放量突增信息
//     * @Description:
//     * @param:
//     * @return:
//     */
//    @RequestMapping(value = "exportGasAbruptChangeInfoByParam", method = RequestMethod.POST)
//    public void exportGasAbruptChangeInfoByParam(@RequestJson(value = "endtime") String endtime,
//                                                 @RequestJson(value = "starttime") String starttime,
//                                                 @RequestJson(value = "pollutionname", required = false) String pollutionname,
//                                                 @RequestJson(value = "outputname", required = false) String outputname,
//                                                 HttpServletResponse response, HttpServletRequest request) {
//        try {
//            Map<String, Object> paramMap = new HashMap<>();
//            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode();
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
//            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("废气排放量突变预警", headers, headersField, collect, "");
//            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
//            ExcelUtil.downLoadExcel("废气排放量突变预警" + new Date().getTime(), response, request, bytesForWorkBook);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:08
     * @Description: 根据污染源ID统计该企业近一年废气污染物排放量及浓度变化趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countGasDischargeAndDensityByPollutionID", method = RequestMethod.POST)
    public Object countGasDischargeAndDensityByPollutionID(@RequestJson(value = "pollutionid") String pollutionid,
                                                           @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            LocalDate localDate = LocalDate.now();
            String endtime = localDate.toString();
            String starttime = localDate.plusYears(-1).toString();
            Map<String, Map<String, Object>> result = onlineService.countDischargeAndDensityByCodeAndMns(pollutantcode, starttime, endtime, pollutionid, WasteGasEnum.getCode());
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/17 0017 下午 2:08
     * @Description: 通过监测时间，监测点类型集合，污染物查询所有监测点该污染物总排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, monitorpointtypes, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getHourTotalFlowDataByParams", method = RequestMethod.POST)
    public Object getHourTotalFlowDataByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes, @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            List<Map<String, Object>> allOutPutInfo = getAllDgimns(monitorpointtypes);
            DecimalFormat format = new DecimalFormat("0.##");
            String dgimns = allOutPutInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.joining(","));
            FlowDataVO flowDataVO = new FlowDataVO();
            flowDataVO.setDataGatherCode(dgimns);

            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());
            Map<String, Object> pollutantMap = new HashMap<>();
            List<Map<String, Object>> params = new ArrayList<>();

            pollutantMap.put("PollutantCode", pollutantcode);
            params.add(pollutantMap);
            flowDataVO.setHourFlowDataList(params);


            List<FlowDataVO> listByParam = mongoBaseService.getListByParam(flowDataVO, hourFlowData, "yyyy-MM-dd HH:mm");

            Map<String, List<FlowDataVO>> collect = listByParam.stream().collect(Collectors.groupingBy(m -> m.getDataGatherCode()));


            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Map<String, Object> map : allOutPutInfo.stream().filter(map -> map.get("DGIMN") != null).collect(Collectors.toList())) {
                String dgimn = map.get("DGIMN").toString();
                Float total = 0f;
                Map<String, Object> data = new HashMap<>();
                for (FlowDataVO dataVO : collect.get(dgimn) == null ? new ArrayList<FlowDataVO>() : collect.get(dgimn)) {
                    List<Map<String, Object>> hourFlowDataList = dataVO.getHourFlowDataList();
                    total = hourFlowDataList.stream().filter(m -> m.get("CorrectedFlow") != null && m.get("PollutantCode") != null && pollutantcode.equals(m.get("PollutantCode").toString())).
                            map(m -> Float.valueOf(m.get("CorrectedFlow").toString())).reduce(total, Float::sum);
                }
                data.put("dgimn", dgimn);
                data.put("OutPutLongitude", map.get("OutPutLongitude") == null ? "" : map.get("OutPutLongitude").toString());
                data.put("OutPutLatitude", map.get("OutPutLatitude") == null ? "" : map.get("OutPutLatitude").toString());
                data.put("Longitude", map.get("Longitude") == null ? "" : map.get("Longitude").toString());
                data.put("Latitude", map.get("Latitude") == null ? "" : map.get("Latitude").toString());
                data.put("pollutionid", map.get("FK_PollutionID") == null ? "" : map.get("FK_PollutionID").toString());
                data.put("pollutionname", map.get("pollution") == null ? "" : map.get("pollution").toString());
                data.put("totalflow", format.format(total));
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
     * @date: 2019/10/29 0029 下午 6:56
     * @Description: 自定义参数查询分钟数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getMinuteDataByParams", method = RequestMethod.POST)
    public Object getMinuteDataByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime,
                                        @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            DecimalFormat format = new DecimalFormat("0.##");
            List<Integer> monitorpointtypes = new ArrayList<>();
            List<String> pollutantcodes = new ArrayList<>();
            monitorpointtypes.add(WasteGasEnum.getCode());
            pollutantcodes.add(pollutantcode);
            List<Map<String, Object>> allOutPutInfo = getAllDgimns(monitorpointtypes);

            List<String> dgimns = allOutPutInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());

            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantcodes", pollutantcodes);

            List<Map> minuteDataByParams = onlineService.getMinuteDataByParams(paramMap);

            Map<String, List<Map<String, Object>>> collect = allOutPutInfo.stream().filter(m -> m.get("pollution") != null).collect(Collectors.groupingBy(m -> m.get("pollution").toString()));
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (String pollutionname : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(pollutionname);
                Double sum = 0d;
                int num = 0;

                Map<String, Object> result = new HashMap<>();
                for (Map<String, Object> map : list) {
                    String dgimn = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                    for (Map m : minuteDataByParams) {
                        if (m.get("DataGatherCode") != null && m.get("AvgStrength") != null && dgimn.equals(m.get("DataGatherCode"))) {
                            num++;
                            sum += Double.valueOf(m.get("AvgStrength").toString());
                        }
                    }
                }
                if (num > 0) {
                    result.put("pollutionname", pollutionname);
                    result.put("AvgStrength", DataFormatUtil.SaveTwoAndSubZero(sum / num));
                }
                resultList.add(result);
            }
//            List<Map<String, Object>> collect1 = resultList.stream().filter(m -> m.get("AvgStrength") != null && "-".equals(m.get("AvgStrength").toString())).collect(Collectors.toList());
            List<Map<String, Object>> collect2 = resultList.stream().filter(m -> m.get("AvgStrength") != null && !"-".equals(m.get("AvgStrength").toString())).sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("AvgStrength").toString())).reversed()).limit(5).collect(Collectors.toList());
            Double total = collect2.stream().filter(m -> m.get("AvgStrength") != null).map(m -> Double.valueOf(m.get("AvgStrength").toString())).reduce(0d, Double::sum);
//            collect2.addAll(collect1);
            resultMap.put("total", format.format(total));
            resultMap.put("datalist", collect2);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 8:01
     * @Description: 自定义参数查询小时排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getHourFlowDataByParams", method = RequestMethod.POST)
    public Object getHourFlowDataByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime,
                                          @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            DecimalFormat format = new DecimalFormat("0.##");
            List<Integer> monitorpointtypes = new ArrayList<>();
            List<String> pollutantcodes = new ArrayList<>();
            monitorpointtypes.add(WasteGasEnum.getCode());
            pollutantcodes.add(pollutantcode);
            List<Map<String, Object>> allOutPutInfo = getAllDgimns(monitorpointtypes);

            List<String> dgimns = allOutPutInfo.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());


            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("dgimns", dgimns);
            paramMap.put("pollutantcodes", pollutantcodes);

            List<Map> minuteDataByParams = onlineService.getHourFlowDataByParams(paramMap);

            Map<String, List<Map<String, Object>>> collect = allOutPutInfo.stream().filter(m -> m.get("pollution") != null).collect(Collectors.groupingBy(m -> m.get("pollution").toString()));
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (String pollutionname : collect.keySet()) {
                List<Map<String, Object>> list = collect.get(pollutionname);
                Double sum = 0d;
                final boolean[] flag = {false};
                Map<String, Object> result = new HashMap<>();
                for (Map<String, Object> map : list) {
                    String dgimn = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                    sum = minuteDataByParams.stream().filter(m -> m.get("DataGatherCode") != null && m.get("AvgFlow") != null && dgimn.equals(m.get("DataGatherCode").toString()))
                            .map(m -> Double.valueOf(m.get("AvgFlow").toString())).reduce(sum, (a, b) -> {
                                flag[0] = true;
                                return a + b;
                            });
                }
                if (flag[0]) {
                    result.put("pollutionname", pollutionname);
                    result.put("AvgFlow", format.format(sum));
                }/*else{
                    result.put("AvgFlow","-");
                }*/
                resultList.add(result);
            }
//            List<Map<String, Object>> collect1 = resultList.stream().filter(m -> m.get("AvgFlow") != null && "-".equals(m.get("AvgFlow").toString())).collect(Collectors.toList());
            List<Map<String, Object>> collect2 = resultList.stream().filter(m -> m.get("AvgFlow") != null && !"-".equals(m.get("AvgFlow").toString())).sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("AvgFlow").toString())).reversed()).limit(5).collect(Collectors.toList());
            Double total = collect2.stream().filter(m -> m.get("AvgFlow") != null).map(m -> Double.valueOf(m.get("AvgFlow").toString())).reduce(0d, Double::sum);

//            collect2.addAll(collect1);
            resultMap.put("total", format.format(total));
            resultMap.put("datalist", collect2);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/17 0017 下午 1:29
     * @Description: 通过监测点类型集合获取到所有监测点的dgimn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    private List<Map<String, Object>> getAllDgimns(List<Integer> monitorpointtypes) {
        List<Map<String, Object>> allOutPutInfo = new ArrayList<>();
        if (monitorpointtypes.contains(WasteGasEnum.getCode())) {
            allOutPutInfo.addAll(gasOutPutInfoService.getAllOutPutInfo(new HashMap<>()));
        }
        return allOutPutInfo;
    }

    /**
     * @author: xsm
     * @date: 2022/01/17 0017 上午 11:13
     * @Description: 获取废气、烟气污染物日均浓度情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "getGasPollutantDayConcentrationDataByParam", method = RequestMethod.POST)
    public Object getGasPollutantDayConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                             @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                             @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                             @RequestJson(value = "pollutantcode") String pollutantcode


    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allPointList = new ArrayList<>();
            if (monitorpointtypes == null || monitorpointtypes.size() == 0) {
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype != null) {
                    monitorpointtypes.add(monitorpointtype);
                }
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (monitorpointtypes.size() > 0) {
                List<Map<String, Object>> pollutantdata = new ArrayList<>();
                for (Integer i : monitorpointtypes) {
                    paramMap.put("monitorpointtype", i);
                    paramMap.put("pollutantcode", pollutantcode);
                    pollutantdata.addAll(pollutantService.getEarlyAndStandardValueByParams(paramMap));
                    paramMap.put("outputids", Arrays.asList());
                    allPointList.addAll(onlineService.getMonitorPointDataByParam(paramMap));
                }
                dataList = getGasPollutantDayConcentrationRankData(monitorpointtypes, allPointList, pollutantdata, pollutantcode, monitortime);
            }

            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/04/16 0016 下午 02:01
     * @Description: 组织废气污染物日均浓度数据
     */
    private List<Map<String, Object>> getGasPollutantDayConcentrationRankData(List<Integer> monitorpointtypes, List<Map<String, Object>> allPointList, List<Map<String, Object>> pollutantdata, String pollutantcode, String monitortime) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //IsHasConvertData
        Map<String, Object> paramMap = new HashMap<>();
        //获取所有废水排口不包含污水处理厂
        boolean ishasconvertdata = false;
        if (allPointList != null && allPointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndtype = new HashMap<>();//监测类型
            Map<String, Object> mnAndShorterName = new HashMap<>();//污染源简称
            Map<String, Object> mnAndPoName = new HashMap<>();//污染源名称
            Map<String, Object> mnAndName = new HashMap<>();//排口名称
            Map<String, Object> mnAndstand = new HashMap<>();//标准值
            if (pollutantdata != null && pollutantdata.size() > 0) {
                for (Map<String, Object> map : pollutantdata) {
                    if (map.get("DGIMN") != null && map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                        mnAndstand.put(map.get("DGIMN").toString(), map.get("StandardMaxValue"));
                    }
                    if (map.get("IsHasConvertData") != null && "1".equals(map.get("IsHasConvertData").toString())) {
                        ishasconvertdata = true;
                    }
                }
            }
            for (Map<String, Object> point : allPointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndId.put(mnCommon, point.get("pk_id"));
                mnAndPoName.put(mnCommon, point.get("pollutionname"));
                mnAndShorterName.put(mnCommon, point.get("shortername"));
                mnAndName.put(mnCommon, point.get("monitorpointname"));
                mnAndtype.put(mnCommon, point.get("monitorpointtype"));
            }
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", "DayData");
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
                        if (mnAndtype.get(mnCommon) != null && Integer.valueOf(mnAndtype.get(mnCommon).toString()) == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() && ishasconvertdata == true) {
                            value = pollutant.getString("AvgConvertStrength");
                        } else {
                            value = pollutant.getString("AvgStrength");
                        }
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
                        if (mnAndtype.get(mnCommon) != null && Integer.valueOf(mnAndtype.get(mnCommon).toString()) == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() && ishasconvertdata == true) {
                            value = pollutant.getString("AvgConvertStrength");
                        } else {
                            value = pollutant.getString("AvgStrength");
                        }
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
            //获取废气传输有效率
            Map<String, Map<String, Object>> csyxl_data = getGasOutPutEffectiveTransmissionData(monitortime, mns, pollutantcode, monitorpointtypes);
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
    private Map<String, Map<String, Object>> getGasOutPutEffectiveTransmissionData(String monitortime, List<String> mns, String pollutantcode, List<Integer> types) {
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> effectiveTransmissionInfoByParamMap = new ArrayList<>();
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Integer i : types) {
            paramMap.put("fkmonitorpointtypecode", i);
            paramMap.put("starttime", monitortime);
            paramMap.put("endtime", monitortime);
            paramMap.put("dgimns", mns);
            if (!"".equals(pollutantcode)) {
                paramMap.put("pollutantcode", pollutantcode);
            }
            effectiveTransmissionInfoByParamMap.addAll(effectiveTransmissionService.getOutPutEffectiveTransmissionInfoByParamMap(paramMap));
        }
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
     * @date: 2022/01/17 0017 下午 1:28
     * @Description: 导出废气、烟气污染物日均浓度情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "exportGasPollutantDayConcentrationDataByParam", method = RequestMethod.POST)
    public void exportGasPollutantDayConcentrationDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                              @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                              @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                              @RequestJson(value = "pollutantcode") String pollutantcode,
                                                              @RequestJson(value = "pollutantname") String pollutantname,
                                                              @RequestJson(value = "pollutantunit", required = false) String pollutantunit,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) throws Exception {
        try {

            exportGasPDCD(monitortime, monitorpointtype, monitorpointtypes, pollutantcode, pollutantname, pollutantunit, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/01/17 0017 下午 1:28
     * @Description: 导出废气、烟气污染物日均浓度情况
     */
    @RequestMapping(value = "exportGasPDCDByParam", method = RequestMethod.GET)
    public void exportGasPDCDByParam(@RequestParam(value = "monitortime") String monitortime,
                                     @RequestParam(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                     @RequestParam(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                     @RequestParam(value = "pollutantcode") String pollutantcode,
                                     @RequestParam(value = "pollutantname") String pollutantname,
                                     @RequestParam(value = "pollutantunit", required = false) String pollutantunit,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        try {

            exportGasPDCD(monitortime, monitorpointtype, monitorpointtypes, pollutantcode, pollutantname, pollutantunit, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void exportGasPDCD(String monitortime, Integer monitorpointtype, List<Integer> monitorpointtypes, String pollutantcode, String pollutantname, String pollutantunit, HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> allPointList = new ArrayList<>();
        if (monitorpointtypes == null || monitorpointtypes.size() == 0) {
            monitorpointtypes = new ArrayList<>();
            if (monitorpointtype != null) {
                monitorpointtypes.add(monitorpointtype);
            }
        }
        paramMap.put("pollutantcode", pollutantcode);
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (monitorpointtypes.size() > 0) {
            List<Map<String, Object>> pollutantdata = new ArrayList<>();
            for (Integer i : monitorpointtypes) {
                paramMap.put("monitorpointtype", i);
                paramMap.put("pollutantcode", pollutantcode);
                pollutantdata.addAll(pollutantService.getEarlyAndStandardValueByParams(paramMap));
                paramMap.put("outputids", Arrays.asList());
                allPointList.addAll(onlineService.getMonitorPointDataByParam(paramMap));
            }
            dataList = getGasPollutantDayConcentrationRankData(monitorpointtypes, allPointList, pollutantdata, pollutantcode, monitortime);
        }
        String fileName = "";

        //获取表头数据
        pollutantunit = pollutantunit != null ? pollutantunit : "";
        List<Map<String, Object>> tabletitledata = getGasPollutantDayConcentrationTableTitle(pollutantunit);
        //设置文件名称
        String titlename = "";
        String ymd = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd", "yyyy年M月d日");
        if (monitorpointtypes.size() == 1) {
            if (monitorpointtypes.get(0) == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {
                titlename = ymd + "废气" + pollutantname + "日均浓度总体情况";
            } else if (monitorpointtypes.get(0) == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                titlename = ymd + "烟气" + pollutantname + "日均浓度总体情况";
            }
            if (monitorpointtypes.get(0) == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {
                fileName = "废气数据分析" + new Date().getTime();
            } else if (monitorpointtypes.get(0) == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                fileName = "烟气数据分析" + new Date().getTime();
            }
        } else {
            titlename = ymd + "废气" + pollutantname + "日均浓度总体情况";
            fileName = "废气数据分析" + new Date().getTime();
        }
        ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, dataList, "", titlename);

    }

    /**
     * @author: xsm
     * @date: 2021/02/26 0026 上午 09:14
     * @Description: 获取废气分析表头
     */
    private List<Map<String, Object>> getGasPollutantDayConcentrationTableTitle(String pollutantunit) {
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


}

