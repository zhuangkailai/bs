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
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: zhangzc
 * @date: 2019/5/28 15:50
 * @Description: 在线空气监测数据
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineAir")
public class OnlineAirController {

    private final PollutantService pollutantService;
    private final AirMonitorStationService airMonitorStationService;
    private final OnlineService onlineService;
    private final OnlineMonitorService onlineMonitorService;

    public OnlineAirController(PollutantService pollutantService, AirMonitorStationService airMonitorStationService, OnlineService onlineService, OnlineMonitorService onlineMonitorService) {
        this.pollutantService = pollutantService;
        this.airMonitorStationService = airMonitorStationService;
        this.onlineService = onlineService;
        this.onlineMonitorService = onlineMonitorService;
    }

    private final String db_hourData = "HourData";
    private final String db_dayData = "DayData";
    private final String db_monthData = "MonthData";
    private final String DB_StationHourAQIData = "StationHourAQIData";
    private final String DB_StationDayAQIData = "StationDayAQIData";


    /**
     * 大气监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();

    /**
     * @author: zhangzc
     * @date: 2019/5/22 15:16
     * @Description: 动态条件查询每个空气监测点最新一条监测数据列表数据
     * @param:
     * @return: DataGatherCode   MonitorTime
     */
    @RequestMapping(value = "getAirLastDatasByParamMap", method = RequestMethod.POST)
    public Object getAirLastDatasByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                paramMap.put("dgimns", dgimns);
                onlineOutPuts = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
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
     * @Description: 站点小时六参数数据信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/15 14:01
     */
    @RequestMapping(value = "getPointLastDataByParamMap", method = RequestMethod.POST)
    public Object getPointLastDataByParamMap(@RequestJson(value = "dgimn") String dgimn,
                                             @RequestJson(value = "timetype", required = false) String timetype,
                                             @RequestJson(value = "monitortime") String monitortime
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection;
            String starttime;
            String endtime;
            if ("day".equals(timetype)) {
                collection = DB_StationDayAQIData;
                starttime = monitortime + " 00:00:00";
                endtime = monitortime + " 23:59:59";
            } else {
                collection = DB_StationHourAQIData;
                starttime = monitortime + ":00:00";
                endtime = monitortime + ":59:59";
            }

            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            List<Document> documents = airMonitorStationService.getStationHourDataListByParam(paramMap);
            if (documents.size() > 0) {
                Map<String, Object> codeAndName = getCodeAndName();
                Document document = documents.get(0);
                List<Document> pollutants = document.get("DataList", List.class);
                String pollutantCode;
                String pollutantName;
                String orderIndex;
                for (Document pollutant : pollutants) {
                    pollutantCode = pollutant.getString("PollutantCode");
                    if (codeAndName.containsKey(pollutantCode)) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("pollutantcode", pollutantCode);
                        resultMap.put("value", pollutant.get("Strength"));
                        pollutantName = codeAndName.get(pollutantCode).toString().split(",")[0];
                        orderIndex = codeAndName.get(pollutantCode).toString().split(",")[1];
                        resultMap.put("pollutantname", pollutantName);
                        resultMap.put("orderIndex", orderIndex);
                        resultList.add(resultMap);
                    }
                }
                //排序
                resultList = resultList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderIndex").toString()))).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private Map<String, Object> getCodeAndName() {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", monitorPointTypeCode);
        List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
        paramMap.clear();
        for (Map<String, Object> pollutant : pollutants) {
            paramMap.put(pollutant.get("code").toString(), pollutant.get("name") + "," +
                    (pollutant.get("orderindex") != null ? pollutant.get("orderindex") : -999));
        }
        return paramMap;
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取空气站点监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirMonitorDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getAirMonitorDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "airid") String airid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(airid);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "";

            paramMap.put("mns", mns);
            boolean isHaveAqi = false;
            boolean isHavePollutant = false;
            if (pollutantcodes.contains("aqi")) {
                pollutantcodes.remove("aqi");
                isHaveAqi = true;
            }
            if (pollutantcodes.size() > 0 || !isHaveAqi) {
                paramMap.put("pollutantcodes", pollutantcodes);
                isHavePollutant = true;
            }
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }

            Boolean isPage = false;
            if (pagenum != null && pagesize != null) {
                paramMap.put("pagenum", pagenum);
                paramMap.put("pagesize", pagesize);
                paramMap.put("sort", "desc");
                isPage = true;
            }
            paramMap.put("sort", "asc");
            List<Document> documents = new ArrayList<>();
            if (isHavePollutant) {
                collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                paramMap.put("collection", collection);
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> pollutantDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                documents.addAll(pollutantDocuments);
            }
            if (isHaveAqi) {
                collection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                paramMap.put("collection", collection);
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> AQIdocuments = onlineService.getAirMonitorDataByParamMap(paramMap);
                documents.addAll(AQIdocuments);
                pollutantcodes.add("aqi");
            }
            charDataList = MongoDataUtils.setOneAirStationManyPollutantsCharDataList(documents, pollutantcodes, collection);
            if (isPage) {
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                pageMap.put("datalist", charDataList);
                return AuthUtil.parseJsonKeyToLower("success", pageMap);
            } else {
                //获取排口污染物的预警、超限、异常值
                Map<String, Object> standardmap = new HashMap<>();
                if (mns != null && mns.size() > 0 && pollutantcodes.size() > 0 && mns.get(0) != null && pollutantcodes.get(0) != null) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("dgimn", mns.get(0));
                    param.put("pollutantcode", pollutantcodes.get(0));
                    param.put("monitorpointtype", monitorPointTypeCode);
                    standardmap = onlineService.getPollutantEarlyAlarmStandardDataByParamMap(param);
                }
                if (charDataList != null && charDataList.size() > 0) {
                    charDataList.get(0).put("standard", standardmap);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 自定义参数获取空气站点实时监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirRealTimeDataByParams", method = RequestMethod.POST)
    public Object getAirRealTimeDataByParams(
            @RequestJson(value = "airid") String airid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {

        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(airid);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "RealTimeData";
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);

            Boolean isPage = false;
            if (pagenum != null && pagesize != null) {
                paramMap.put("pagenum", pagenum);
                paramMap.put("pagesize", pagesize);
                paramMap.put("sort", "desc");
                isPage = true;
            }
            paramMap.put("sort", "asc");
            List<Document> documents = new ArrayList<>();
            paramMap.put("collection", collection);
            List<Document> pollutantDocuments = onlineService.getMonitorDataByParamMap(paramMap);
            documents.addAll(pollutantDocuments);
            List<Map<String, Object>> charDataList = MongoDataUtils.setOneAirStationManyPollutantsCharDataList(documents, pollutantcodes, collection);
            if (isPage) {
                Map<String, Object> pageMap = onlineService.getPageMapForReport(paramMap);
                pageMap.put("datalist", charDataList);
                return AuthUtil.parseJsonKeyToLower("success", pageMap);
            } else {
                //获取排口污染物的预警、超限、异常值
                Map<String, Object> standardmap = new HashMap<>();
                if (mns != null && mns.size() > 0 && pollutantcodes.size() > 0 && mns.get(0) != null && pollutantcodes.get(0) != null) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("dgimn", mns.get(0));
                    param.put("pollutantcode", pollutantcodes.get(0));
                    param.put("monitorpointtype", monitorPointTypeCode);
                    standardmap = onlineService.getPollutantEarlyAlarmStandardDataByParamMap(param);
                }
                if (charDataList != null && charDataList.size() > 0) {
                    charDataList.get(0).put("standard", standardmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description: 获取空气站点数据报表（小时报、日报）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:13
     */
    @RequestMapping(value = "getAirStationReportDataByParams", method = RequestMethod.POST)
    public Object getAirStationReportDataByParams(
            @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "sortmap", required = false) Object sortmap
    ) {

        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointtype", monitorPointTypeCode);

            //表头数据
            List<Map<String, Object>> titleList = airMonitorStationService.getStationTitleListByParam(paramMap);
            resultMap.put("titlelist", titleList);
            //表格数据
            paramMap.put("timetype", timetype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("sortmap", sortmap);
            List<Map<String, Object>> dataList = airMonitorStationService.getStationReportDataListByParam(paramMap);
            resultMap.put("dataList", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description: 获取空气站点排名数据（小时、日）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:13
     */
    @RequestMapping(value = "getStationRantDataByParams", method = RequestMethod.POST)
    public Object getStationRantDataByParams(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode
    ) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            //表格数据
            paramMap.put("timetype", timetype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> resultList = airMonitorStationService.getStationRantDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description: 获取空气站点六参数同比数据（小时、日）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:13
     */
    @RequestMapping(value = "getStationSixTBDataByParams", method = RequestMethod.POST)
    public Object getStationSixTBDataByParams(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimn") String dgimn
    ) throws Exception {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            //表格数据
            paramMap.put("timetype", timetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("dgimn", dgimn);
            List<Map<String, Object>> resultList = airMonitorStationService.getStationSixTBDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description: 获取空气站点空气质量数据分布（月、日）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:13
     */
    @RequestMapping(value = "getStationDistributeDataByParams", method = RequestMethod.POST)
    public Object getStationDistributeDataByParams(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode
    ) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            //表格数据
            paramMap.put("timetype", timetype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> resultList = airMonitorStationService.getStationDistributeDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description: 导出空气站点数据报表（小时报、日报）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:13
     */
    @RequestMapping(value = "exportAirStationReportDataByParams", method = RequestMethod.POST)
    public void exportAirStationReportDataByParams(
            @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "sortmap", required = false) Object sortmap, HttpServletResponse response,
            HttpServletRequest request
    ) throws IOException {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointtype", monitorPointTypeCode);
            //表头数据
            List<Map<String, Object>> titleList = airMonitorStationService.getStationTitleListByParam(paramMap);
            //表格数据
            paramMap.put("timetype", timetype);
            paramMap.put("monitortime", monitortime);
            paramMap.put("sortmap", sortmap);
            List<Map<String, Object>> dataList = airMonitorStationService.getStationReportDataListByParam(paramMap);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(titleList, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(titleList, "prop");
            //设置文件名称
            String name = "小时";
            if (timetype.equals("day")) {
                name = "日";
            }
            String fileName = name + "空气质量数据报表_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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
    @RequestMapping(value = "getOneAirListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneAirListPageDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "airid") String airid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(airid);

            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);

            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();


            paramMap.putAll(titleMap);

            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);

            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            dataMap.put("total", paramMap.get("total"));
            dataMap.put("pages", paramMap.get("pages"));
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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
    @RequestMapping(value = "getOneAirListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneAirListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "airid") String airid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(airid);
            Integer reportType = 2;

            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
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
            dataMap.put("total", paramMap.get("total"));
            dataMap.put("pages", paramMap.get("pages"));
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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
    @RequestMapping(value = "getManyAirListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyAirListPageDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "airids") List<String> outputids,
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
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.putAll(titleMap);

            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("direction", "desc");

            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            dataMap.put("total", paramMap.get("total"));
            dataMap.put("pages", paramMap.get("pages"));
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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
    @RequestMapping(value = "getManyAirListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyAirListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "airids") List<String> airids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Integer reportType = 2;
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(airids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();


            paramMap.put("outputids", airids);
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
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
            dataMap.put("total", paramMap.get("total"));
            dataMap.put("pages", paramMap.get("pages"));
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个空气站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneAirOutPutReport", method = RequestMethod.POST)
    public void exportOneAirOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);

            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            //获取表格数据
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.putAll(titleMap);
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "空气监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }



    /**
     * @Description: 判断是否可以导出
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/8 16:04
     */
    @RequestMapping(value = "isExportOneAirOutPutReport", method = RequestMethod.POST)
    public Object isExportOneAirOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime
             ) throws IOException {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            //获取表格数据
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
            boolean isMany = DataFormatUtil.exportDataIsMany(totalCount);
            if (isMany) {
                String flag = ReturnInfo.other_many.split("#")[0];
                String message = ReturnInfo.other_many.split("#")[1];
                return AuthUtil.returnObject(flag, message);
            }
            return AuthUtil.returnObject(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个空气站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyAirOutPutReport", method = RequestMethod.POST)
    public void exportManyAirOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

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
            paramMap.putAll(titleMap);
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            List<Map<String, Object>> tableListData = onlineService.getTableListDataForReport(paramMap);

            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "空气监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    @RequestMapping(value = "isExportManyAirOutPutReport", method = RequestMethod.POST)
    public Object isExportManyAirOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime
    ) throws IOException {

        try {
            //获取表头数据
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorPointTypeCode);
            titleMap.put("pollutantType", monitorPointTypeCode);
            titleMap.put("outputids", outputids);
            //获取表格数据
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.putAll(titleMap);
            String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
            paramMap.put("collection", collection);
            paramMap.put("leftCollection", leftCollection);
            paramMap.put("mns", mns);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
            boolean isMany = DataFormatUtil.exportDataIsMany(totalCount);
            if (isMany) {
                String flag = ReturnInfo.other_many.split("#")[0];
                String message = ReturnInfo.other_many.split("#")[1];
                return AuthUtil.returnObject(flag, message);
            }
            return AuthUtil.returnObject(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午 2:17
     * @Description: 根据监测点主键和监测时间以及污染物编码和时间类型来获取空气站点的小时或日监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getAirStationHourOrDayMonitorDatasByParams", method = RequestMethod.POST)
    public Object getAirStationHourOrDayMonitorDatasByParams(@RequestJson(value = "pkids", required = true) List<String> pkidlist,
                                                             @RequestJson(value = "pollutantcode") String pollutantcode,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "timetype") String timetype
    ) {
        try {
            List<Map<String, Object>> resultlist = airMonitorStationService.getAirStationHourOrDayDataByParams(pkidlist, pollutantcode, starttime, endtime, timetype);
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:24
     * @Description: 获取空气污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getAirPollutantEarlyAndOverAlarmsByParamMap(
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
            List<Map<String, Object>> outputs = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
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
     * @Description: 获取大气污染物预警/超标/异常详情页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getAirPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getAirPollutantEarlyOrOverOrExceptionDetailsPage(
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
    @RequestMapping(value = "exportAirPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportAirPollutantEarlyOrOverOrExceptionDetailsData(
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
            String fileName = "大气污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取大气多站点多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getAirManyOutPutManyPollutantMonitorDataByParams(
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
            String collection = "";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }

            paramMap.put("mns", mns);
            boolean isHaveAqi = false;
            boolean isHavePollutant = false;
            if (pollutantcodes.contains("aqi")) {
                pollutantcodes.remove("aqi");
                isHaveAqi = true;
            }
            if (pollutantcodes.size() > 0 || !isHaveAqi) {
                paramMap.put("pollutantcodes", pollutantcodes);
                isHavePollutant = true;
            }
            paramMap.put("sort", "asc");

            List<Document> documents = new ArrayList<>();

            if (isHaveAqi) {
                collection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                paramMap.put("collection", collection);

                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> AQIdocuments = onlineService.getAirMonitorDataByParamMap(paramMap);
                documents.addAll(AQIdocuments);
                pollutantcodes.add("aqi");
            }
            if (isHavePollutant) {
                collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                paramMap.put("collection", collection);

                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> pollutantDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                documents.addAll(pollutantDocuments);
            }

            charDataList = MongoDataUtils.setManyAirStationManyPollutantsCharDataList(
                    documents,
                    pollutantcodes,
                    collection,
                    outPutIdAndMn,
                    outputids,
                    idAndName,
                    codeAndName);
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取站点因子列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/20 8:58
     */
    @RequestMapping(value = "getAirPointDataListByParam", method = RequestMethod.POST)
    public Object getAirPointDataListByParam(
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {

        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "";
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("mns", mns);
            List<Document> documents;
            paramMap.put("sort", "asc");
            Map<String, Map<String, Object>> timeAndMnAndValue = new HashMap<>();
            Map<String, Object> mnAndValue;
            String time;
            String timeF;
            String mnCommon;
            String dataKey;
            if (datamark == 1) {
                timeF = "yyyy-MM-dd HH";
                dataKey = "HourDataList";
            } else if (datamark == 2) {
                timeF = "yyyy-MM-dd";
                dataKey = "DayDataList";
            } else {
                timeF = "yyyy-MM";
                dataKey = "MonthDataList";
            }
            if (pollutantcode.equals("aqi")) {
                collection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                paramMap.put("collection", collection);
                documents = onlineService.getAirMonitorDataByParamMap(paramMap);
                for (Document document : documents) {
                    time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), timeF);
                    mnAndValue = timeAndMnAndValue.get(time) != null ? timeAndMnAndValue.get(time) : new HashMap<>();
                    mnCommon = document.getString("StationCode");
                    mnAndValue.put(mnCommon, document.get("AQI"));
                    timeAndMnAndValue.put(time, mnAndValue);
                }
            } else {
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                paramMap.put("collection", collection);
                documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Document> dataList;
                for (Document document : documents) {
                    time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), timeF);
                    mnAndValue = timeAndMnAndValue.get(time) != null ? timeAndMnAndValue.get(time) : new HashMap<>();
                    mnCommon = document.getString("DataGatherCode");
                    dataList = document.get(dataKey, List.class);
                    for (Document data : dataList) {
                        if (pollutantcode.equals(data.get("PollutantCode"))) {
                            mnAndValue.put(mnCommon, data.get("AvgStrength"));
                            break;
                        }
                    }
                    timeAndMnAndValue.put(time, mnAndValue);
                }
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (String timeIndex : timeAndMnAndValue.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("monitortime", timeIndex);
                mnAndValue = timeAndMnAndValue.get(timeIndex);
                for (String mnIndex : mns) {
                    dataMap.put(mnIndex, mnAndValue.get(mnIndex) != null ? mnAndValue.get(mnIndex) : "");
                }
                dataList.add(dataMap);
            }
            Map<String, Object> resultMap = new HashMap<>();
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
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取站点六参数列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/20 8:58
     */
    @RequestMapping(value = "getAirPointSixDataListByParam", method = RequestMethod.POST)
    public Object getAirPointSixDataListByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "sortmap", required = false) Object sortMap

    ) {

        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> outputids = new ArrayList<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, String> mnAndName = onlineService.getMNAndMonitorPointName(outputids, monitorPointTypeCode);
            Map<String, Object> paramMap = new HashMap<>();
            String starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, monitortime);
            String endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", mns);
            List<Document> documents;
            paramMap.put("sort", "asc");
            Map<String, Map<String, Object>> mnAndCodeAndValue = new HashMap<>();
            Map<String, Object> codeAndValue;
            String mnCommon;
            String collection = MongoDataUtils.getAirCollectionByDataMark(datamark);
            paramMap.put("collection", collection);
            documents = onlineService.getAirMonitorDataByParamMap(paramMap);
            List<Document> pollutantList;

            for (Document document : documents) {
                mnCommon = document.getString("StationCode");
                codeAndValue = mnAndCodeAndValue.get(mnCommon) != null ? mnAndCodeAndValue.get(mnCommon) : new HashMap<>();
                codeAndValue.put("aqi", document.get("AQI"));
                pollutantList = document.get("DataList", List.class);
                for (Document pollutant : pollutantList) {
                    codeAndValue.put(pollutant.getString("PollutantCode"), pollutant.get("Strength"));
                }
                mnAndCodeAndValue.put(mnCommon, codeAndValue);
            }

            List<Map<String, Object>> dataList = new ArrayList<>();

            List<String> codes = CommonTypeEnum.getSixIndexList();
            for (String mnIndex : mns) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("monitorpointname", mnAndName.get(mnIndex));
                codeAndValue = mnAndCodeAndValue.get(mnIndex);
                for (String codeIndex : codes) {
                    if (codeAndValue != null) {
                        dataMap.put(codeIndex, codeAndValue.get(codeIndex) != null ? codeAndValue.get(codeIndex) : "");
                    } else {
                        dataMap.put(codeIndex, "");
                    }
                }
                dataList.add(dataMap);
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (dataList.size() > 0) { //排序分页
                if (sortMap != null) {
                    JSONObject sortJson = JSONObject.fromObject(sortMap);
                    String sortKey = sortJson.getString("sortkey");
                    String type = sortJson.getString("sorttype");
                    if ("asc".equals(type)) {
                        dataList = dataList.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(m, sortKey))
                        ).collect(Collectors.toList());
                    } else {
                        dataList = dataList.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey((Map<String, Object>) m, sortKey)).reversed()
                        ).collect(Collectors.toList());
                    }
                }
                if (pagesize != null && pagenum != null) {
                    resultMap.put("total", dataList.size());
                    dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", dataList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取站点六参数列表对比数据（当前值，同比值，环比值）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/20 8:58
     */
    @RequestMapping(value = "getAirPointSixCompareDataListByParam", method = RequestMethod.POST)
    public Object getAirPointSixCompareDataListByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "sortmap", required = false) Object sortMap

    ) throws Exception {

        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> outputids = new ArrayList<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, String> mnAndName = onlineService.getMNAndMonitorPointName(outputids, monitorPointTypeCode);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (pollutantcode.equals("zhzs")) {//综合指数
                if (datamark == 3) {//月综合指数
                    String tb_time = DataFormatUtil.getMonthTBYearDate(monitortime);
                    String hb_time = DataFormatUtil.getBeforeMonthTime(1, monitortime);
                    Map<String, Map<String, Object>> mnAndThisMap = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(monitortime);
                    Map<String, Map<String, Object>> mnAndTBMap = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(tb_time);
                    Map<String, Map<String, Object>> mnAndHBMap = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(hb_time);
                    for (String mnIndex : mnAndName.keySet()) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("monitorpointname", mnAndName.get(mnIndex));
                        dataMap.put("thisvalue", mnAndThisMap.get(mnIndex) != null ? mnAndThisMap.get(mnIndex).get("total") : "");
                        dataMap.put("tbvalue", mnAndTBMap.get(mnIndex) != null ? mnAndTBMap.get(mnIndex).get("total") : "");
                        dataMap.put("hbvalue", mnAndHBMap.get(mnIndex) != null ? mnAndHBMap.get(mnIndex).get("total") : "");
                        dataList.add(dataMap);
                    }
                }
            } else {
                Map<String, Object> paramMap = new HashMap<>();
                String collection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);


                String starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, monitortime);
                String endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, monitortime);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                List<Document> thisDoc = onlineService.getAirMonitorDataByParamMap(paramMap);
                Map<String, Object> mnAndThisValue = getMnAndValue(thisDoc, pollutantcode);


                String tb_time;
                String hb_time;
                if (datamark == 1) {//小时
                    tb_time = DataFormatUtil.getHourYearTBDate(monitortime, 1);
                    hb_time = DataFormatUtil.getBeforeByHourTime(1, monitortime);
                } else if (datamark == 2) {//日
                    tb_time = DataFormatUtil.getDayYearTBDate(monitortime, 1);
                    hb_time = DataFormatUtil.getBeforeByDayTime(1, monitortime);
                } else {//月
                    tb_time = DataFormatUtil.getMonthTBYearDate(monitortime);
                    hb_time = DataFormatUtil.getBeforeMonthTime(1, monitortime);
                }
                //同比
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, tb_time);
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, tb_time);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                List<Document> tb_Doc = onlineService.getAirMonitorDataByParamMap(paramMap);
                Map<String, Object> mnAndTBValue = getMnAndValue(tb_Doc, pollutantcode);

                //环比
                starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, hb_time);
                endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, hb_time);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                List<Document> hb_Doc = onlineService.getAirMonitorDataByParamMap(paramMap);
                Map<String, Object> mnAndHBValue = getMnAndValue(hb_Doc, pollutantcode);
                for (String mnIndex : mnAndName.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("monitorpointname", mnAndName.get(mnIndex));
                    dataMap.put("thisvalue", mnAndThisValue.get(mnIndex));
                    dataMap.put("tbvalue", mnAndTBValue.get(mnIndex));
                    dataMap.put("hbvalue", mnAndHBValue.get(mnIndex));
                    dataList.add(dataMap);
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (dataList.size() > 0) { //排序分页
                if (sortMap != null) {
                    JSONObject sortJson = JSONObject.fromObject(sortMap);
                    String sortKey = sortJson.getString("sortkey");
                    String type = sortJson.getString("sorttype");
                    if ("asc".equals(type)) {
                        dataList = dataList.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(m, sortKey))
                        ).collect(Collectors.toList());
                    } else {
                        dataList = dataList.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey((Map<String, Object>) m, sortKey)).reversed()
                        ).collect(Collectors.toList());
                    }
                }
                if (pagesize != null && pagenum != null) {
                    resultMap.put("total", dataList.size());
                    dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", dataList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取站点空气质量等级天数及占比
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/6/20 8:58
     */
    @RequestMapping(value = "getAirPointLevelDataListByParam", method = RequestMethod.POST)
    public Object getAirPointLevelDataListByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws Exception {

        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> outputids = new ArrayList<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, outPutIdAndMn);
            Map<String, String> mnAndName = onlineService.getMNAndMonitorPointName(outputids, monitorPointTypeCode);
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String collection = "StationDayAQIData";
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            int datamark = 3;
            String starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, monitortime);
            String endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, monitortime);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Document> documents = onlineService.getAirMonitorDataByParamMap(paramMap);
            Map<String, Map<String, Integer>> mnAndLevelAndNum = new HashMap<>();
            Map<String, Integer> levelAndNum;
            Map<String, Integer> mnAndNum = new HashMap<>();
            String mnCommon;
            Integer AQI;
            String levelName;
            for (Document document : documents) {
                mnCommon = document.getString("StationCode");
                AQI = document.getInteger("AQI");
                levelName = DataFormatUtil.getQualityCodeByAQI(AQI);
                levelAndNum = mnAndLevelAndNum.get(mnCommon) != null ? mnAndLevelAndNum.get(mnCommon) : new HashMap<>();
                levelAndNum.put(levelName, levelAndNum.get(levelName) != null ? levelAndNum.get(levelName) + 1 : 1);
                mnAndNum.put(mnCommon, mnAndNum.get(mnCommon) != null ? mnAndNum.get(mnCommon) + 1 : 1);
                mnAndLevelAndNum.put(mnCommon, levelAndNum);
            }
            List<String> levelNames = Arrays.asList(
                    "you", "lianghao", "qingdu", "zhongdu", "zhongdu1", "yanzhong"
            );
            int num;
            int total;
            for (String mnIndex : mnAndName.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("monitorpointname", mnAndName.get(mnIndex));
                levelAndNum = mnAndLevelAndNum.get(mnIndex);
                total = mnAndNum.get(mnIndex) != null ? mnAndNum.get(mnIndex) : 0;
                for (String levelCode : levelNames) {
                    if (levelAndNum != null) {
                        num = levelAndNum.get(levelCode) != null ? levelAndNum.get(levelCode) : 0;
                        dataMap.put(levelCode + "_num", num);
                        if (total > 0) {
                            dataMap.put(levelCode + "_rate", DataFormatUtil.SaveOneAndSubZero(100D * num / total));
                        } else {
                            dataMap.put(levelCode + "_rate", "0");
                        }

                    } else {
                        dataMap.put(levelCode + "_rate", "0");
                        dataMap.put(levelCode + "_num", 0);
                    }

                }
                dataList.add(dataMap);
            }
            Map<String, Object> resultMap = new HashMap<>();
            if (dataList.size() > 0) { //排序分页
                if (pagesize != null && pagenum != null) {
                    resultMap.put("total", dataList.size());
                    dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", dataList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> getMnAndValue(List<Document> documents, String pollutantCode) {
        String mnCommon;
        Map<String, Object> mnAndValue = new HashMap<>();
        List<Document> pollutantList;
        for (Document document : documents) {
            mnCommon = document.getString("StationCode");
            if (pollutantCode.equals("aqi")) {
                mnAndValue.put(mnCommon, document.get("AQI"));
            } else {
                pollutantList = document.get("DataList", List.class);
                for (Document pollutant : pollutantList) {
                    if (pollutant.getString("PollutantCode").equals(pollutantCode)) {
                        mnAndValue.put(mnCommon, pollutant.get("Strength"));
                        break;
                    }

                }
            }
        }
        return mnAndValue;
    }

    private static Double comparingDoubleByKey(Map<String, Object> map, String key) {
        Object value;
        if (map.get(key) instanceof Map) {
            value = ((Map) map.get(key)).get("value");
        } else {
            value = map.get(key);
        }
        return value != null && !"".equals(value) ? Double.parseDouble(value.toString()) : 999;
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询空气污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getAirPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            List<Map<String, Object>> outPuts = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
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
     * @Description: 自定义查询条件导出空气污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportAirPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportAirPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            List<Map<String, Object>> outPuts = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
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
            String fileName = "大气污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:40
     * @Description: 条件查询空气浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getAirChangeWarnDetailParams(@RequestJson(value = "dgimn") String dgimn,
                                               @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();
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
     * @Description: 获取空气浓度突增污染物
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
            Integer monitortype = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();
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
     * @Description: 获取大气单个污染物浓度突增数据
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


    /**
     * @author: lip
     * @date: 2020/3/6 0006 上午 8:46
     * @Description: 获取站点统计分析（箱形图+正态分布图）数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getStationStatisticsDataByParams", method = RequestMethod.POST)
    public Object getStationStatisticsDataByParams(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            String collection = "";
            String dataKey = "";
            String valueKey = "AvgStrength";

            switch (timetype) {
                case "hour":
                    collection = db_hourData;
                    dataKey = "HourDataList";
                    starttime = starttime + ":00:00";
                    endtime = endtime + ":59:59";
                    break;
                case "day":
                    collection = db_dayData;
                    dataKey = "DayDataList";
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                case "month":
                    collection = db_monthData;
                    dataKey = "MonthDataList";
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime) + " 00:00:00";
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(collection)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    List<Document> pollutants;
                    List<Double> values = new ArrayList<>();
                    Map<Double, Integer> valueAndNum = new HashMap<>();
                    Double value;
                    for (Document document : documents) {
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                                value = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.getString(valueKey)) : null;
                                if (value != null) {
                                    values.add(value);
                                    if (valueAndNum.containsKey(value)) {
                                        valueAndNum.put(value, valueAndNum.get(value) + 1);
                                    } else {
                                        valueAndNum.put(value, 1);
                                    }

                                }
                            }
                        }
                    }
                    //1，获取箱形图数据
                    resultMap.put("boxshapedata", values);
                    //2，获取正态分布图数据
                    //2.1排序
                    valueAndNum = DataFormatUtil.sortByKey(valueAndNum, false);
                    //2.2遍历
                    List<Map<String, Object>> columnarDataList = new ArrayList<>();
                    List<Map<String, Object>> curveDataList = new ArrayList<>();

                    //获取平均数，标准差
                    Double mean = Double.parseDouble(DataFormatUtil.getListAvgValue(values));
                    Double sd = DataFormatUtil.standardDeviation(values);
                    for (Double valueIndex : valueAndNum.keySet()) {
                        Map<String, Object> columnarData = new HashMap<>();
                        Map<String, Object> curveData = new HashMap<>();
                        columnarData.put("value", valueIndex);
                        columnarData.put("frequency", valueAndNum.get(valueIndex));
                        columnarDataList.add(columnarData);
                        curveData.put("value", valueIndex);
                        curveData.put("normalstatevalue", getNormalStateValue(mean, sd, valueIndex));
                        curveDataList.add(curveData);
                    }
                    resultMap.put("columnardata", columnarDataList);
                    resultMap.put("curvedata", curveDataList);
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
     * @date: 2020/3/6 0006 下午 4:09
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: mean:平均数，sd：标准差，x：输入值
     * @return:
     */
    private Object getNormalStateValue(Double mean, Double sd, Double x) {
        NormalDistribution normalDistribution = new NormalDistribution(mean, sd);
        double y = normalDistribution.cumulativeProbability(x);
        return DataFormatUtil.SaveTwoAndSubZero(y);
    }


    /**
     * @author: lip
     * @date: 2020/3/6 0006 上午 8:46
     * @Description: 获取站点监测数据频率分布数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getFrequencyDistributionDataByParams", method = RequestMethod.POST)
    public Object getFrequencyDistributionDataByParams(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String collection = "";
            String dataKey = "";
            String valueKey = "AvgStrength";
            switch (timetype) {
                case "hour":
                    collection = db_hourData;
                    dataKey = "HourDataList";
                    starttime = starttime + ":00:00";
                    endtime = endtime + ":59:59";
                    break;
                case "day":
                    collection = db_dayData;
                    dataKey = "DayDataList";
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                case "month":
                    collection = db_monthData;
                    dataKey = "MonthDataList";
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime) + " 00:00:00";
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(collection)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    List<Document> pollutants;
                    List<Double> values = new ArrayList<>();
                    Double value;
                    for (Document document : documents) {
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                                value = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.getString(valueKey)) : null;
                                if (value != null) {
                                    values.add(value);
                                }
                            }
                        }
                    }
                    if (values.size() > 0) {
                        paramMap.put("pollutantcode", pollutantcode);
                        paramMap.put("dgimn", dgimn);
                        paramMap.put("alarmlevelcode", "0");
                        //获取站点污染物标准值+预警值
                        Map<String, Object> monitorStandard = airMonitorStationService.getMonitorStandardByParam(paramMap);
                        Double minValue = Collections.min(values);
                        Double maxValue = Collections.max(values);
                        int share = Integer.parseInt(DataFormatUtil.parseProperties("airstation.share"));
                        Double shareValue = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo((maxValue - minValue) / share));
                        Double indexDouble;
                        String indexString;
                        List<String> rangeList = new ArrayList<>();
                        for (int i = 0; i < share; i++) {
                            if (i == 4) {
                                indexDouble = maxValue;
                            } else {
                                indexDouble = minValue + shareValue;
                            }
                            indexString = minValue + "-" + DataFormatUtil.SaveTwoAndSubZero(indexDouble);
                            minValue = Double.parseDouble(DataFormatUtil.SaveTwoAndSubZero(indexDouble));
                            rangeList.add(indexString);
                        }

                        Double standardValue = monitorStandard != null && monitorStandard.get("standardvalue") != null ?
                                Double.parseDouble(monitorStandard.get("standardvalue").toString()) : null;
                        Double earlyValue = monitorStandard != null && monitorStandard.get("earlyvalue") != null ?
                                Double.parseDouble(monitorStandard.get("earlyvalue").toString()) : null;
                        Double maxIndex;
                        Double minIndex;
                        boolean isBelong;
                        int normalNum;
                        //设置预警区间次数
                        Map<String, List<Double>> earlyAndNum = new HashMap<>();
                        Map<String, List<Double>> standardAndNum = new HashMap<>();
                        Set<Double> noSet = new HashSet<>();
                        List<Double> tempList;

                        for (String range : rangeList) {
                            minIndex = Double.parseDouble(range.split("-")[0]);
                            maxIndex = Double.parseDouble(range.split("-")[1]);
                            if (standardValue != null) {
                                if (earlyValue != null && isBelongRange(earlyValue, minIndex, maxIndex)) {
                                    double tempMax = maxIndex;
                                    if (standardValue != null && isBelongRange(standardValue, minIndex, maxIndex) && standardValue >= earlyValue) {
                                        tempMax = standardValue;
                                    }
                                    for (Double valueIndex : values) {
                                        isBelong = isBelongRange(valueIndex, earlyValue, tempMax);
                                        if (isBelong) {
                                            if (earlyAndNum.containsKey(range)) {
                                                tempList = earlyAndNum.get(range);
                                            } else {
                                                tempList = new ArrayList<>();
                                            }
                                            noSet.add(valueIndex);
                                            tempList.add(valueIndex);
                                            earlyAndNum.put(range, tempList);

                                        }
                                    }
                                }
                                if (isBelongRange(standardValue, minIndex, maxIndex)) {
                                    for (Double valueIndex : values) {
                                        isBelong = isBelongRange(valueIndex, standardValue, maxIndex);
                                        if (isBelong) {
                                            if (standardAndNum.containsKey(range)) {
                                                tempList = standardAndNum.get(range);
                                            } else {
                                                tempList = new ArrayList<>();
                                            }
                                            noSet.add(valueIndex);
                                            tempList.add(valueIndex);
                                            standardAndNum.put(range, tempList);
                                        }
                                    }
                                } else if (minIndex >= standardValue) {
                                    for (Double valueIndex : values) {
                                        isBelong = isBelongRange(valueIndex, standardValue, maxIndex);
                                        if (!noSet.contains(valueIndex) && isBelong) {
                                            if (standardAndNum.containsKey(range)) {
                                                tempList = standardAndNum.get(range);
                                            } else {
                                                tempList = new ArrayList<>();
                                            }
                                            noSet.add(valueIndex);
                                            tempList.add(valueIndex);
                                            standardAndNum.put(range, tempList);
                                        }
                                    }
                                }
                            }
                        }
                        for (String range : rangeList) {
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("valuerange", range);
                            normalNum = 0;
                            minIndex = Double.parseDouble(range.split("-")[0]);
                            maxIndex = Double.parseDouble(range.split("-")[1]);
                            for (Double valueIndex : values) {
                                isBelong = isBelongRange(valueIndex, minIndex, maxIndex);
                                if (!noSet.contains(valueIndex) && isBelong) {
                                    normalNum++;
                                }
                            }
                            resultMap.put("normalnum", normalNum);
                            resultMap.put("standardnum", standardAndNum.get(range) != null ? standardAndNum.get(range).size() : 0);
                            resultMap.put("earlynum", earlyAndNum.get(range) != null ? earlyAndNum.get(range).size() : 0);
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

    private boolean isBelongRange(Double valueIndex, Double minIndex, Double maxIndex) {
        boolean isBelong = false;

        if ((valueIndex >= minIndex && valueIndex < maxIndex) ||
                (Double.doubleToLongBits(valueIndex) == Double.doubleToLongBits(maxIndex))) {
            isBelong = true;
        }
        return isBelong;
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 上午 10:56
     * @Description: 获取空气站点同比监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirTBMonitorDataByParam", method = RequestMethod.POST)
    public Object getAirTBMonitorDataByParam(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            String finalStartTime = starttime;
            String finalEndTime = endtime;

            //设置时间及集合
            String collection = "";
            String dataKey = "";
            String valueKey = "AvgStrength";
            String tb_starttime = "";
            String tb_endtime = "";
            switch (timetype) {
                case "hour":
                    collection = db_hourData;
                    dataKey = "HourDataList";
                    tb_starttime = DataFormatUtil.getHourTBDate(starttime) + ":00:00";
                    tb_endtime = DataFormatUtil.getHourTBDate(endtime) + ":59:59";
                    starttime = starttime + ":00:00";
                    endtime = endtime + ":59:59";
                    break;
                case "day":
                    collection = db_dayData;
                    dataKey = "DayDataList";
                    tb_starttime = DataFormatUtil.getDayTBDate(starttime) + " 00:00:00";
                    tb_endtime = DataFormatUtil.getDayTBDate(endtime) + " 23:59:59";
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                case "month":
                    collection = db_monthData;
                    dataKey = "MonthDataList";
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    tb_starttime = DataFormatUtil.getMonthTBDate(starttime) + " 00:00:00";
                    tb_endtime = DataFormatUtil.getMonthTBDate(endtime) + " 23:59:59";
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(collection)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("collection", collection);
                List<Document> curr_documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Object> curr_timeAndValue = getTimeAndValue(curr_documents, dataKey, valueKey, pollutantcode);

                paramMap.put("starttime", tb_starttime);
                paramMap.put("endtime", tb_endtime);
                List<Document> tb_documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Object> tb_timeAndValue = getTimeAndValue(tb_documents, dataKey, valueKey, pollutantcode);
                List<String> times = getTimesByCollection(collection, finalStartTime, finalEndTime);
                String tbTime;
                for (String time : times) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitortime", time);
                    resultMap.put("currvalue", curr_timeAndValue.get(time) != null ? curr_timeAndValue.get(time) : "");
                    tbTime = getTBTimeByCollection(time, collection);
                    resultMap.put("tbvalue", tb_timeAndValue.get(tbTime) != null ? tb_timeAndValue.get(tbTime) : "");
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
     * @date: 2020/3/4 0004 上午 10:56
     * @Description: 获取空气站点绝对浓度监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirAbsoluteMonitorDataByParam", method = RequestMethod.POST)
    public Object getAirAbsoluteMonitorDataByParam(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String finalStartTime = starttime;
            String finalEndTime = endtime;

            //设置时间及集合
            String collection = "";
            String dataKey = "";
            String valueKey = "AvgStrength";
            switch (timetype) {
                case "hour":
                    collection = db_hourData;
                    dataKey = "HourDataList";
                    starttime = starttime + ":00:00";
                    endtime = endtime + ":59:59";
                    break;
                case "day":
                    collection = db_dayData;
                    dataKey = "DayDataList";
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                case "month":
                    collection = db_monthData;
                    dataKey = "MonthDataList";
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(collection)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("collection", collection);
                List<Document> curr_documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Object> timeAndValue = getTimeAndValue(curr_documents, dataKey, valueKey, pollutantcode);

                List<String> times = getTimesByCollection(collection, finalStartTime, finalEndTime);
                String thisTime;
                String nextTime;
                Double thisDouble;
                Double nextDouble;
                for (int i = 0; i < times.size(); i++) {
                    if (i + 1 == times.size()) {
                        thisTime = times.get(i - 1);
                        nextTime = times.get(i);
                    } else {
                        thisTime = times.get(i);
                        nextTime = times.get(i + 1);
                    }
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitortime", thisTime + "~" + nextTime);
                    nextDouble = timeAndValue.get(nextTime) != null ? Double.parseDouble(timeAndValue.get(nextTime).toString()) : null;
                    thisDouble = timeAndValue.get(thisTime) != null ? Double.parseDouble(timeAndValue.get(thisTime).toString()) : null;
                    if (nextDouble != null && thisDouble != null) {
                        resultMap.put("absolutevalue", DataFormatUtil.SaveTwoAndSubZero(nextDouble - thisDouble));
                    } else {
                        resultMap.put("absolutevalue", "");
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

    /**
     * @author: lip
     * @date: 2020/3/6 0006 下午 3:05
     * @Description: 获取同比时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getTBTimeByCollection(String time, String collection) {
        String tbTime;
        if (collection.indexOf("Hour") >= 0) {
            tbTime = DataFormatUtil.getHourTBDate(time);
        } else if (collection.indexOf("Day") >= 0) {
            tbTime = DataFormatUtil.getDayTBDate(time);
        } else {
            tbTime = DataFormatUtil.getMonthTBDate(time + "-1");
            tbTime = DataFormatUtil.FormatDateOneToOther(tbTime, "yyyy-MM-dd", "yyyy-MM");
        }
        return tbTime;
    }

    /**
     * @author: lip
     * @date: 2020/3/6 0006 下午 2:46
     * @Description: 根据时间段获取日期集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getTimesByCollection(String collection, String starttime, String endtime) throws Exception {
        List<String> times;
        if (collection.indexOf("Hour") >= 0) {
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
        } else if (collection.indexOf("Day") >= 0) {
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
        } else {
            times = DataFormatUtil.getMonthBetween(starttime, endtime);
        }
        times.add(endtime);
        return times;
    }

    /**
     * @author: lip
     * @date: 2020/3/6 0006 下午 2:28
     * @Description: 转换数据对应关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getTimeAndValue(List<Document> documents, String dataKey, String valueKey, String polluantCode) {
        Map<String, Object> timeAndValue = new HashMap<>();
        String monitorTime;
        List<Document> pollutants;
        for (Document document : documents) {
            if (dataKey.indexOf("Hour") >= 0) {
                monitorTime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            } else if (dataKey.indexOf("Day") >= 0) {
                monitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            } else {
                monitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
            }
            pollutants = document.get(dataKey, List.class);
            for (Document pollutant : pollutants) {
                if (polluantCode.equals(pollutant.get("PollutantCode"))) {
                    if (pollutant.get(valueKey) != null) {
                        timeAndValue.put(monitorTime, pollutant.get(valueKey));
                    }
                    break;
                }
            }
        }
        return timeAndValue;
    }

}
