package com.tjpu.sp.controller.environmentalprotection.report;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.FileController;
import com.tjpu.sp.controller.environmentalprotection.onlinemonitor.OnlineController;
import com.tjpu.sp.model.environmentalprotection.report.ReportInfoVO;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.petition.PetitionInfoService;
import com.tjpu.sp.service.environmentalprotection.report.ReportManagementService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年7月23日 下午 4:13
 * @Description:报告管理处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("reportManagement")
public class ReportManagementController {

    @Autowired
    private ReportManagementService reportManagementService;

    @Autowired
    private OnlineService onlineService;

    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;

    @Autowired
    private PetitionInfoService petitionInfoService;


    @Autowired
    private PollutionTraceSourceService pollutionTraceSourceService;

    @Autowired
    private FileController fileController;

    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;

    @Autowired
    private OnlineMonitorService onlineMonitorService;

    @Autowired
    private OnlineController onlineController;

    @Autowired
    private PollutantService pollutantService;

    private final String db_dayData = "DayData";

    private final String db_hourData = "HourData";

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 下午 4:14
     * @Description:根据监测时间和报告类型获取报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getReportManagementInfosByParamMap", method = RequestMethod.POST)
    public Object getReportManagementInfosByParamMap(@RequestJson(value = "reporttypes") List<String> reporttypes,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttypes", reporttypes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> result = reportManagementService.getReportInfosByParamMap(paramMap, reporttypes);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 下午 4:49
     * @Description:保存报告上传信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addReportInfo", method = RequestMethod.POST)
    public Object addReportInfo(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            ReportInfoVO reportInfoVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new ReportInfoVO());
            reportInfoVO.setPkId(UUID.randomUUID().toString());
            reportInfoVO.setUpdatedate(new Date());
            reportInfoVO.setUpdateuser(username);
            reportInfoVO.setFkFileid(jsonObject.getString("fk_fileid"));
            reportManagementService.insertSelective(reportInfoVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/27 0027 下午 3:22
     * @Description:删除报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "deleteReportInfo", method = RequestMethod.POST)
    public Object deleteReportInfo(@RequestJson("pkid") String pkid,
                                   @RequestJson("file_id") String fileId,
                                   @RequestJson("business_type") String businessType) throws Exception {
        try {
            //删除mongodb中的文件
            fileController.deleteFile(fileId, businessType);
            reportManagementService.deleteReportInfo(pkid);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/26 0026 上午 10:00
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "editAnalysisReportInfo", method = RequestMethod.POST)
    public Object editAnalysisReportInfo(
            @RequestJson("reporttype") Integer reporttype,
            @RequestJson("starttime") String starttime,
            @RequestJson("endtime") String endtime,
            @RequestJson("reportmakedate") String reportmakedate

    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, String> MNAndPoints = getAllStinkMNAndPointNames();
            List<Map<String, Object>> tableList;
            List<Map<String, Object>> chartDataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", reporttype);

            paramMap.put("reportmakedate", reportmakedate);
            List<Map<String, Object>> reportAttributeDataList;
            switch (CommonTypeEnum.AnalysisReportTypeEnum.getCodeByInteger(reporttype)) {
                case DayReportEnum:
                    paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
                    paramMap.put("analysisreportendtime", endtime + " 23:59:59");
                    reportAttributeDataList = reportManagementService.getReportAttributeDataByParam(paramMap);
                    chartDataList = getStinkPointWeekDayChartData(starttime, endtime);
                    tableList = getStinkPointHourTableData(starttime, MNAndPoints, reportAttributeDataList, chartDataList);
                    resultMap.put("tabletitle", MNAndPoints.values());
                    resultMap.put("reportAttributeDataList", reportAttributeDataList);
                    resultMap.put("tableList", tableList);
                    resultMap.put("chartDataList", chartDataList);
                    break;
                case WeekReportEnum:
                    paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
                    paramMap.put("analysisreportendtime", endtime + " 23:59:59");
                    reportAttributeDataList = reportManagementService.getReportAttributeDataByParam(paramMap);
                    chartDataList = getStinkPointWeekDayChartData(starttime, endtime);
                    tableList = getStinkPointWeekDayData(starttime, endtime, reportAttributeDataList, chartDataList);
                    resultMap.put("tableList", tableList);
                    resultMap.put("chartDataList", chartDataList);
                    resultMap.put("reportAttributeDataList", reportAttributeDataList);
                    break;
                case MonthReportEnum:
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
                    paramMap.put("analysisreportendtime", endtime + " 23:59:59");
                    reportAttributeDataList = reportManagementService.getReportAttributeDataByParam(paramMap);
                    List<Map<String, Object>> dataList = getStinkManyPointDataByParams(4, starttime, endtime);
                    if (dataList.size() > 0) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("monitortime", starttime);
                        map.put("chartdata", dataList);
                        chartDataList.add(map);
                    }
                    tableList = getStinkPointDayTableData(starttime, endtime, MNAndPoints, reportAttributeDataList, chartDataList);
                    resultMap.put("tabletitle", MNAndPoints.values());
                    resultMap.put("reportAttributeDataList", reportAttributeDataList);
                    resultMap.put("tableList", tableList);
                    resultMap.put("chartDataList", chartDataList);
                    break;
                case SeasonReportEnum:
                    break;
                case HalfYearReportEnum:
                    break;
                case YearReportEnum:
                    break;
                case ExpertReportEnum:
                    paramMap.clear();
                    paramMap.put("starttime", starttime + ":00:00");
                    paramMap.put("endtime", endtime + ":59:59");
                    Map<String, Object> gisData = new HashMap<>();
                    //获取投诉点位数据
                    List<Map<String, Object>> petitionPoint = petitionInfoService.getPetitionDataByParam(paramMap);
                    gisData.put("petitionPoint", petitionPoint);
                    //获取敏感点数据
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    List<Map<String, Object>> stinkPointData = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                    gisData.put("stinkPointData", stinkPointData);
                    //获取厂界恶臭点位数据
                    paramMap.clear();
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    List<Map<String, Object>> entStinkData = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                    gisData.put("entStinkData", entStinkData);
                    resultMap.put("gisData", gisData);
                    //获取敏感点图表及描述信息
                    paramMap.clear();
                    paramMap.put("stinkPointData", stinkPointData);
                    paramMap.put("starttime", starttime);
                    paramMap.put("endtime", endtime);

                    paramMap.put("analysisreportstarttime", starttime + ":00:00");
                    paramMap.put("analysisreportendtime", endtime + ":59:59");
                    paramMap.put("reportmakedate", reportmakedate);
                    reportAttributeDataList = reportManagementService.getReportAttributeDataByParam(paramMap);
                    //初始化地图信息描述文字
                    initGisText(reportAttributeDataList, petitionPoint, starttime, endtime);
                    paramMap.put("reportAttributeDataList", reportAttributeDataList);

                    //初始化敏感点风向数据描述
                    initMGWindData(starttime, endtime, reportAttributeDataList);

                    List<Map<String, Object>> pointDataList = getPointDataListByParam(paramMap);
                    resultMap.put("pointDataList", pointDataList);
                    //获取厂界恶臭图表数据
                    MNAndPoints.clear();
                    MNAndPoints.putAll(onlineService.getMNAndMonitorPoint(new ArrayList<>(), CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()));
                    List<String> mns = new ArrayList<>(MNAndPoints.keySet());
                    List<Map<String, Object>> entStinkCharData = new ArrayList<>();
                    List<Map<String, Object>> stinkCharData = getStinkManyPointDataByParams(3, starttime, endtime);
                    for (Map<String, Object> map : stinkCharData) {
                        if (mns.contains(map.get("dgimn"))) {
                            entStinkCharData.add(map);
                        }
                    }
                    resultMap.put("entStinkCharData", entStinkCharData);
                    //初始化厂界恶臭图表文字描述
                    initChartText(reportAttributeDataList, entStinkCharData, starttime, endtime);
                    //获取厂界恶臭列表数据
                    List<Map<String, Object>> entStinkTableData = getStinkPointManyHourTableData(starttime, endtime, MNAndPoints, reportAttributeDataList);
                    Map<String, Object> tableData = new HashMap<>();
                    tableData.put("tabletitle", MNAndPoints.values());
                    tableData.put("entStinkTableData", entStinkTableData);
                    resultMap.put("tableData", tableData);
                    resultMap.put("reportAttributeDataList", reportAttributeDataList);
                    break;
                default:
                    break;
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/9/27 0027 下午 2:07
     * @Description: 初始化敏感点风向数据描述
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void initMGWindData(String starttime, String endtime, List<Map<String, Object>> reportAttributeDataList) {
        if (reportAttributeDataList.size() > 0) {
            String reportattributevalue;
            for (Map<String, Object> reportAttribute : reportAttributeDataList) {
                if ("pointPollutantDataBZ_102".equals(reportAttribute.get("reportattributecode"))) {
                    reportattributevalue = getMGPointWindDataText(starttime, endtime, "102", "八中站点");
                    reportAttribute.put("reportattributevalue", reportattributevalue);
                } else if ("pointPollutantDataGWH_108".equals(reportAttribute.get("reportattributecode"))) {
                    reportattributevalue = getMGPointWindDataText(starttime, endtime, "108", "管委会站点");
                    reportAttribute.put("reportattributevalue", reportattributevalue);
                }
            }
        }
    }

    /**
     * @author: lip
     * @date: 2019/9/27 0027 下午 3:46
     * @Description: 获取敏感点风向数据描述文字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getMGPointWindDataText(String starttime, String endtime, String mn, String pointname) {
        List<Map<String, Object>> mainWindDirectionList = pollutionTraceSourceService.getMonitorPointLeadingWindDirectionDataByParamMap(starttime, endtime, "102");
        String windDirectionText = "";
        List<Map<String, Object>> pollutantWindData = pollutionTraceSourceService.getMonitorPointWindDirectionAndOUDataByParamMap(starttime, endtime, mn);
        if (pollutantWindData.size() > 0) {
            String minAndMax = "";
            List<Double> ouValues = new ArrayList<>();
            Double value;
            for (Map<String, Object> pollutant : pollutantWindData) {
                if (pollutant.get("ou") != null && !"".equals(pollutant.get("ou"))) {
                    value = Double.parseDouble(pollutant.get("ou").toString());
                    ouValues.add(value);
                }
            }
            Collections.sort(ouValues);
            Double min = ouValues.get(0);
            Double max = ouValues.get(ouValues.size() - 1);
            if (ouValues.size() > 1) {
                minAndMax = "恶臭浓度在" + DataFormatUtil.subZeroAndDot(min.toString()) + "到" + DataFormatUtil.subZeroAndDot(max.toString()) + "之间，";
                windDirectionText = getWindDirectionText(pollutantWindData, minAndMax, max, pointname);
            } else if (ouValues.size() == 1) {
                minAndMax = "恶臭浓度为" + DataFormatUtil.subZeroAndDot(max.toString()) + "，";
                windDirectionText = getWindDirectionText(pollutantWindData, minAndMax, max, pointname);
            }
        }
        String mainWindDirection = "";
        if (mainWindDirectionList.size() > 0) {
            List<String> winddirections = new ArrayList<>();
            for (Map<String, Object> wind : mainWindDirectionList) {
                if (wind.get("winddirection") != null) {
                    winddirections.add(wind.get("winddirection").toString());
                }
            }
            String windData = DataFormatUtil.FormatListToString(winddirections, "、");
            mainWindDirection = "在此时段内" + pointname + "主要为" + windData + "，";
        }
        String reportattributevalue = mainWindDirection + windDirectionText;
        return reportattributevalue;
    }

    private String getWindDirectionText(List<Map<String, Object>> pollutantWindData, String minAndMax, Double value, String pointname) {
        String windDirectionText = "";
        if (value <= 12) {
            windDirectionText = minAndMax + "相对不高，该点位对投诉影响分析作用不大";
        } else {
            List<Integer> hours = new ArrayList<>();
            List<String> winddirections = new ArrayList<>();
            String winddirection;
            String hour;
            String monitortime;
            for (Map<String, Object> pollutant : pollutantWindData) {
                value = Double.parseDouble(pollutant.get("ou").toString());
                if (value > 12) {
                    monitortime = pollutant.get("monitortime").toString();
                    hour = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd HH", "H");
                    hours.add(Integer.parseInt(hour));
                    winddirection = pollutant.get("winddirection").toString();
                    winddirection.replaceAll("风", "");
                    if (!winddirections.contains(winddirection)) {
                        winddirections.add(winddirection);
                    }
                }
            }
            String hourText = DataFormatUtil.mergeContinueNum(hours, 0, "、", "时");
            hourText = hourText.substring(0, hourText.length() - 1);
            String windText = DataFormatUtil.FormatListToString(winddirections, "、");
            windDirectionText = minAndMax + "其中在"
                    + hourText + "恶臭浓度较高，"
                    + "且在浓度较高时的风向为" + windText + "，"
                    + "故此时段恶臭高值源为" + pointname + windText + "方向。";

        }
        return windDirectionText;
    }

    /**
     * @author: lip
     * @date: 2019/9/26 0026 下午 1:54
     * @Description: 初始化厂界恶臭图表文字描述
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void initChartText(List<Map<String, Object>> reportAttributeDataList, List<Map<String, Object>> entStinkCharData, String starttime, String endtime) {
        if (reportAttributeDataList != null && entStinkCharData.size() > 0) {
            boolean isUpper = false;
            List<String> highDgimn = new ArrayList<>();
            String pointNames = "";
            List<String> MGMN = new ArrayList<>();
            Map<String, String> mnAndStinkPoints = onlineService.getMNAndMonitorPoint(new ArrayList<>(),
                    CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            for (String mnKey : mnAndStinkPoints.keySet()) {
                MGMN.add(mnKey);
            }
            for (Map<String, Object> reportAttribute : reportAttributeDataList) {
                if ("entStinkChartData".equals(reportAttribute.get("reportattributecode"))) {
                    String pointChangeSituation = "由图4可看出，各企业厂界监测点位恶臭浓度变化不大。";
                    //获取浓度较高的时间及点位信息（时间：点位=1：n）
                    List<Map<String, Object>> pointChangeList = getSpecialReportTimeToMonitorValues(entStinkCharData, MGMN, false, starttime, endtime);
                    if (pointChangeList.size() > 0) {
                        isUpper = true;
                        List<Integer> numList = new ArrayList<>();
                        String ymdh;
                        Integer hour;
                        String pointname;
                        String dgimn;
                        List<Map<String, Object>> upperpointdata;
                        Map<String, Integer> pointAndNum = new HashMap<>();
                        for (Map<String, Object> pointChange : pointChangeList) {
                            ymdh = DataFormatUtil.FormatDateOneToOther(pointChange.get("time").toString(), "yyyy-MM-dd HH", "H");
                            hour = Integer.parseInt(ymdh);
                            if (!numList.contains(hour)) {
                                numList.add(hour);
                            }
                            upperpointdata = (List<Map<String, Object>>) pointChange.get("upperpointdata");
                            for (Map<String, Object> upperPoint : upperpointdata) {
                                pointname = upperPoint.get("pointname").toString();
                                dgimn = upperPoint.get("dgimn").toString();
                                if (pointAndNum.containsKey(pointname)) {
                                    pointAndNum.put(pointname, pointAndNum.get(pointname) + 1);
                                } else {
                                    pointAndNum.put(pointname, 1);
                                }
                                if (!highDgimn.contains(dgimn)) {
                                    highDgimn.add(dgimn);
                                }

                            }
                        }
                        Collections.sort(numList);
                        ymdh = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                        if (StringUtils.isNotBlank(ymdh)) {
                            ymdh = ymdh.substring(0, ymdh.length() - 1);
                        }
                        Map<String, Integer> sortMap = DataFormatUtil.sortMapByValue(pointAndNum, true);
                        List<String> pointNameList = new ArrayList<>();
                        List<String> CXNameList = new ArrayList<>();
                        for (String name : sortMap.keySet()) {
                            CXNameList.add(name);
                            pointNameList.add(name);
                        }
                        String CXNames = DataFormatUtil.FormatListToString(CXNameList, "、");
                        pointNames = DataFormatUtil.FormatListToString(pointNameList, "、");
                        pointChangeSituation = "由图4可看出，各企业厂界监测点位恶臭浓度在" + ymdh
                                + "的恶臭浓度相对比较高，" + CXNames + "浓度一直持续相对较高 。";
                    }
                    reportAttribute.put("reportattributevalue", pointChangeSituation);

                } else if ("conclusionData".equals(reportAttribute.get("reportattributecode"))) {
                    String controlSuggestions = "无";
                    if (isUpper) {
                        String BQ = "";
                        List<Map<String, Object>> pointChangeList = getSpecialReportMonitorToTimeValues(entStinkCharData, highDgimn, starttime, endtime);
                        String upperName = "";
                        if (pointChangeList.size() > 0) {
                            List<String> pointNameList = new ArrayList<>();
                            String pointName;
                            String ymdh;
                            Integer hour;
                            List<Integer> numList = new ArrayList<>();
                            List<Map<String, Object>> timeData;
                            for (Map<String, Object> pointChange : pointChangeList) {
                                pointName = pointChange.get("pointname").toString();
                                pointNameList.add(pointName);
                                timeData = (List<Map<String, Object>>) pointChange.get("uppertimedata");
                                for (Map<String, Object> uppertime : timeData) {
                                    ymdh = DataFormatUtil.FormatDateOneToOther(uppertime.get("time").toString(), "yyyy-MM-dd HH", "H");
                                    hour = Integer.parseInt(ymdh);
                                    if (!numList.contains(hour)) {
                                        numList.add(hour);
                                    }
                                }
                            }
                            upperName = DataFormatUtil.FormatListToString(pointNameList, "、");
                            Collections.sort(numList);
                            ymdh = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                            if (StringUtils.isNotBlank(ymdh)) {
                                ymdh = ymdh.substring(0, ymdh.length() - 1);
                            }
                            BQ = "并且" + upperName + "在" + ymdh + "浓度均有突升，";
                        }
                        String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
                        List<Map<String, Object>> QZDataList = new ArrayList<>();
                              //相似度去掉1115  onlineService.getEntStinkHourRelationDataByParamMap(starttime, endtime, pollutantCode, highDgimn);
                        String qz = "";
                        if (QZDataList.size() > 0) {
                            String stinkPoint;
                            String entStinkPoint;
                            List<String> entStinkPoints = new ArrayList<>();
                            List<Map<String, Object>> similaritydata;
                            for (Map<String, Object> qzData : QZDataList) {
                                similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                if (similaritydata.size() > 0) {
                                    entStinkPoints.clear();
                                    stinkPoint = qzData.get("pointname").toString();
                                    similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                    for (Map<String, Object> similarity : similaritydata) {
                                        entStinkPoint = similarity.get("pointname").toString();
                                        entStinkPoints.add(entStinkPoint);
                                    }
                                    entStinkPoint = DataFormatUtil.FormatListToString(entStinkPoints, "、");
                                    qz = qz + entStinkPoint + "与" + stinkPoint + "敏感点浓度变化趋势有一定的相似，";
                                }
                            }
                            if (StringUtils.isNotBlank(qz)) {
                                qz = "其中，" + qz;
                            }
                        }
                        String YQ = "";
                        if (StringUtils.isNotBlank(upperName)) {
                            // YQ = "，尤其是" + upperName;
                        }

                        controlSuggestions = "根据图4各点位小时恶臭浓度变化趋势分析，" +
                                pointNames + "恶臭浓度相对比较高，" + BQ + qz +
                                "根据当天气象条件分析，建议加大对" + pointNames + "监督巡查力度" + YQ
                                + "。";
                    }
                    reportAttribute.put("reportattributevalue", controlSuggestions);
                }


            }
        }
    }

    /**
     * @author: lip
     * @date: 2019/9/25 0025 下午 4:53
     * @Description: 地理位置文字描述
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void initGisText(List<Map<String, Object>> reportAttributeDataList, List<Map<String, Object>> petitionPoint, String starttime, String endtime) {
        if (reportAttributeDataList != null && petitionPoint.size() > 0) {
            for (Map<String, Object> reportAttribute : reportAttributeDataList) {
                if ("gisTextData".equals(reportAttribute.get("reportattributecode"))) {
                    Map<String, Object> paramMap = new HashMap<>();

                    List<Map<String, Object>> sensitivePointData = new ArrayList<>();
                    List<String> TSPointList = new ArrayList<>();
                    Double centerlongitude = Double.parseDouble(DataFormatUtil.parseProperties("park.center.longitude"));
                    Double centerlatitude = Double.parseDouble(DataFormatUtil.parseProperties("park.center.latitude"));
                    String petitiontitle;
                    Double longitude;
                    Double latitude;
                    Double azimuth;
                    String windDirection;
                    String directions = "";
                    for (Map<String, Object> petition : petitionPoint) {
                        if (petition.get("petitiontitle") != null
                                && petition.get("longitude") != null
                                && petition.get("latitude") != null) {
                            petitiontitle = petition.get("petitiontitle").toString();
                            longitude = Double.parseDouble(petition.get("longitude").toString());
                            latitude = Double.parseDouble(petition.get("latitude").toString());
                            TSPointList.add(petitiontitle);
                            azimuth = DataFormatUtil.getAngle1(centerlatitude, centerlongitude, latitude, longitude);
                            windDirection = DataFormatUtil.windDirectionSwitch(azimuth, "name");
                            directions += petitiontitle + "位于工业园的" + windDirection + "，";
                            paramMap.put("longitude", longitude);
                            paramMap.put("latitude", latitude);
                            paramMap.put("starttime", starttime);
                            paramMap.put("endtime", endtime);
                            paramMap.put("collection", "HourData");
                            sensitivePointData.addAll(pollutionTraceSourceService.getSensitivePointDataByParamMap(paramMap));
                        }
                    }
                    if (StringUtils.isNotBlank(directions)) {
                        directions = directions.substring(0, directions.length() - 1) + "。";
                    }
                    String ymd = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH", "yyyy-MM-dd");
                    Map<String, Object> windData = pollutionTraceSourceService.getLeadingWindDirectionAndWindSpeed(ymd);

                    String mainWindDirection = windData.get("winddirection").toString();
                    String TSPointNames = DataFormatUtil.FormatListToString(TSPointList, "、");

                    String MGPoint = "";

                    if (sensitivePointData.size() > 0) {
                        List<String> MGPoints = new ArrayList<>();
                        for (Map<String, Object> point : sensitivePointData) {
                            MGPoints.add(point.get("monitorpointname").toString());
                        }
                        String MGPointNames = DataFormatUtil.FormatListToString(MGPoints, "、");
                        MGPoint = "，根据当天气象分析，若恶臭气体扩散到举报点位，必然途经" + MGPointNames;
                    }
                    String gisTextData = "从图1可以看出举报点位是"
                            + TSPointNames
                            + "，" + directions
                            + "当天主导风向为" + mainWindDirection + MGPoint + "。";
                    reportAttribute.put("reportattributevalue", gisTextData);
                    break;
                }
            }
        }

    }

    /**
     * @author: lip
     * @date: 2019/9/3 0003 上午 11:25
     * @Description: 获取各个敏感点数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPointDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> stinkPointData = (List<Map<String, Object>>) paramMap.get("stinkPointData");
        if (stinkPointData.size() > 0) {
            String pointName;
            String mn;
            String attributeCode;
            List<Map<String, Object>> pointWindData;
            List<Map<String, Object>> pointPollutantData;
            List<Map<String, Object>> reportAttributeDataList = (List<Map<String, Object>>) paramMap.get("reportAttributeDataList");
            Map<String, Object> mnAndValue = new HashMap<>();

            for (Map<String, Object> map : reportAttributeDataList) {
                attributeCode = map.get("reportattributecode").toString();
                if (attributeCode.indexOf("_") >= 0) {
                    mn = attributeCode.split("_")[1];
                    mnAndValue.put(mn, map.get("reportattributevalue"));
                }
            }
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
            for (Map<String, Object> map : stinkPointData) {
                Map<String, Object> resultMap = new HashMap<>();
                pointName = map.get("OutputName").toString();
                resultMap.put("pointName", pointName);
                mn = map.get("DGIMN").toString();
                pointWindData = getStinkPointWindData(starttime, endtime, pollutantCode, mn, "hour");
                pointPollutantData = getPointPollutantData(mn, starttime, endtime, pollutantCode);
                resultMap.put("dgimn", mn);
                resultMap.put("pointWindData", pointWindData);
                resultMap.put("pointPollutantData", pointPollutantData);
                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/9/6 0006 上午 9:14
     * @Description: 获取污染玫瑰图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPointPollutantData(String mn, String starttime, String endtime, String pollutantCode) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        Object object = onlineController.getHourWeatherDataByMNAndPollutant(mn, starttime, endtime, pollutantCode);
        if (object != null) {
            Map<String, Object> resultMap = (Map<String, Object>) object;
            resultList = (List<Map<String, Object>>) resultMap.get("data");
        }
        return resultList;

    }

    /**
     * @author: lip
     * @date: 2019/9/3 0003 上午 11:58
     * @Description: 获取各个点位风向数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointWindData(String starttime, String endtime, String pollutantCode, String mn, String hour) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        Object object = onlineController.getOnlineDataAndAirDatasByParams(mn, hour, pollutantCode, starttime, endtime);
        if (object != null) {
            Map<String, Object> resultMap = (Map<String, Object>) object;
            resultList = (List<Map<String, Object>>) resultMap.get("data");
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/8/26 0026 上午 10:00
     * @Description: 暂存分析报告数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "saveAnalysisReportData", method = RequestMethod.POST)
    public Object saveAnalysisReportData(
            @RequestJson("reporttype") Integer reporttype,
            @RequestJson("starttime") String starttime,
            @RequestJson("endtime") String endtime,
            @RequestJson("reportmakedate") String reportmakedate,
            @RequestJson("attributedata") Object attributedata

    ) {
        try {
            if (attributedata != null) {

                if (CommonTypeEnum.AnalysisReportTypeEnum.MonthReportEnum.getCode() == reporttype) {
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                }
                List<Map<String, Object>> attributeList = (List<Map<String, Object>>) attributedata;
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("reporttype", reporttype);
                paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
                paramMap.put("analysisreportendtime", endtime + " 23:59:59");
                paramMap.put("reportmakedate", reportmakedate);
                paramMap.put("attributedata", attributeList);
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
                paramMap.put("username", username);
                reportManagementService.updateReportAttributeDataByParam(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/26 0026 下午 1:09
     * @Description: 获取各个点位一周小时
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointWeekDayChartData(String starttime, String endtime) throws IOException {
        List<Map<String, Object>> imageData = new ArrayList<>();
        List<String> ymds = DataFormatUtil.getYMDBetween(starttime, endtime);
        ymds.add(endtime);
        String Hstarttime;
        String Hendtime;
        List<Map<String, Object>> dataList;

        for (int i = 0; i < ymds.size(); i++) {
            Hstarttime = ymds.get(i) + " 00";
            Hendtime = ymds.get(i) + " 23";
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("monitortime", ymds.get(i));
            dataList = getStinkManyPointDataByParams(3, Hstarttime, Hendtime);
            if (dataList.size() > 0) {
                map.put("chartdata", dataList);
                imageData.add(map);
            }
        }
        return imageData;

    }


    /**
     * @author: lip
     * @date: 2019/8/19 0019 上午 11:32
     * @Description: 生成恶臭周报文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "createStinkWeekReport", method = RequestMethod.POST)
    public void createStinkWeekReport(@RequestJson("starttime") String starttime,
                                      @RequestJson("endtime") String endtime,
                                      @RequestJson("reportmakedate") String reportmakedate,
                                      @RequestJson("attributedata") Object attributedata,
                                      @RequestJson("imagedatalist") Object imagedatalist,
                                      HttpServletRequest request,
                                      HttpServletResponse response

    ) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            //设置时间值
            String weekDate = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd", "yyyy年MM月dd日")
                    + "-" + DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd", "MM月dd日");
            String reportmakedateTemp = DataFormatUtil.FormatDateOneToOther(reportmakedate, "yyyy-MM-dd", "yyyyMMdd");
            resultData.put("weekdate", weekDate);
            String space = getSomeSpace(24);
            resultData.put("reportmarktime", space + reportmakedateTemp);
            //获取属性值
            List<Map<String, Object>> attributeList = (List<Map<String, Object>>) attributedata;
            for (Map<String, Object> map : attributeList) {
                if ("stinkQualityStatus".equals(map.get("reportattributecode"))) {
                    resultData.put("totaldesc", map.get("reportattributevalue"));
                }
                if ("pointChangeSituation".equals(map.get("reportattributecode"))) {
                    resultData.put("imgcontext", map.get("reportattributevalue"));
                }
                //管控建议
                if ("controlSuggestions".equals(map.get("reportattributecode"))) {
                    resultData.put("controlSuggestions", map.get("reportattributevalue"));
                }
            }
            //统计表格内容
            List<Map<String, Object>> tableListInfo = getStinkPointWeekDayData(starttime, endtime, null, null);
            resultData.put("tableListInfo", tableListInfo);
            //从前端获取图表图片数据
            resultData.put("imageData", imagedatalist);
            String imgtitle = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd", "yyyy/MM/dd")
                    + "-" + DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd", "MM/dd") + "日变化趋势";
            resultData.put("imgtitle", imgtitle);
            //文件名称
            String fileName = "工业园恶臭分析周报" + weekDate + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/恶臭分析周报模板.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);

            //存入数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", CommonTypeEnum.AnalysisReportTypeEnum.WeekReportEnum.getCode());
            paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
            paramMap.put("analysisreportendtime", endtime + " 23:59:59");
            paramMap.put("reportmakedate", reportmakedate);
            paramMap.put("attributedata", attributeList);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            paramMap.put("username", username);
            reportManagementService.updateReportAttributeDataByParam(paramMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 8:34
     * @Description: 获取文档中空格填充字符串
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getSomeSpace(int num) {
        String space = "";
        for (int i = 0; i < num; i++) {
            space += "&#x0020;";
        }
        return space;
    }


    /**
     * @author: lip
     * @date: 2019/8/19 0019 上午 11:32
     * @Description: 生成恶臭日报文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "createStinkDayReport", method = RequestMethod.POST)
    public void createStinkDayReport(
            @RequestJson("starttime") String starttime,
            @RequestJson("endtime") String endtime,
            @RequestJson("reportmakedate") String reportmakedate,
            @RequestJson("attributedata") Object attributedata,
            @RequestJson(value = "chartdata", required = false) String chartdata,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            //设置时间值
            String dayDate = DataFormatUtil.FormatDateOneToOther(starttime + " 00", "yyyy-MM-dd HH", "yyyy年MM月dd日H时")
                    + "-" + DataFormatUtil.FormatDateOneToOther(endtime + " 23", "yyyy-MM-dd HH", "H时");
            String reportmakedateTemp = DataFormatUtil.FormatDateOneToOther(reportmakedate, "yyyy-MM-dd", "yyyyMMdd");
            resultData.put("daydate", dayDate);
            String space = getSomeSpace(24);
            resultData.put("reportmarktime", space + reportmakedateTemp);

            //获取属性值
            List<Map<String, Object>> attributeList = (List<Map<String, Object>>) attributedata;
            for (Map<String, Object> map : attributeList) {
                //恶臭质量状况
                if ("stinkQualityStatus".equals(map.get("reportattributecode"))) {
                    resultData.put("totaldesc", map.get("reportattributevalue"));
                }
                //各点位恶臭质量逐日变化情况
                if ("pointChangeSituation".equals(map.get("reportattributecode"))) {
                    resultData.put("imgcontext", map.get("reportattributevalue"));
                }
                //管控建议
                if ("controlSuggestions".equals(map.get("reportattributecode"))) {
                    resultData.put("controlSuggestions", map.get("reportattributevalue"));
                }
            }
            resultData.put("imgtitle", "图1 " + dayDate + "变化趋势");
            Map<String, String> MNAndPoints = getAllStinkMNAndPointNames();
            List<Map<String, Object>> tableList = getStinkPointHourTableData(starttime, MNAndPoints, null, null);
            List<String> topData = new ArrayList<>();
            for (String mnKey : MNAndPoints.keySet()) {
                topData.add(MNAndPoints.get(mnKey));
            }
            resultData.put("topData", topData);
            resultData.put("tableList", tableList);
            Map<String, Object> imgDataMap = new HashMap<>();
            imgDataMap.put("imagedata", chartdata);
            resultData.put("imageData", Arrays.asList(imgDataMap));
            //文件名称
            String fileName = "工业园恶臭分析日报" + dayDate + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/恶臭分析日报模板.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);
            //存入数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", CommonTypeEnum.AnalysisReportTypeEnum.DayReportEnum.getCode());
            paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
            paramMap.put("analysisreportendtime", endtime + " 23:59:59");
            paramMap.put("reportmakedate", reportmakedate);
            paramMap.put("attributedata", attributeList);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            paramMap.put("username", username);
            reportManagementService.updateReportAttributeDataByParam(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/19 0019 上午 11:32
     * @Description: 生成恶臭月报文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "createStinkMonthReport", method = RequestMethod.POST)
    public void createStinkMonthReport(
            @RequestJson("starttime") String starttime,
            @RequestJson("endtime") String endtime,
            @RequestJson("reportmakedate") String reportmakedate,
            @RequestJson("attributedata") Object attributedata,
            @RequestJson(value = "chartdata", required = false) String chartdata,
            HttpServletRequest request,
            HttpServletResponse response

    ) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            //设置时间值

            starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);

            String dayDate = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd", "yyyy年MM月dd日")
                    + "-" + DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd", "dd日");
            String reportmakedateTemp = DataFormatUtil.FormatDateOneToOther(reportmakedate, "yyyy-MM-dd", "yyyyMMdd");
            resultData.put("daydate", dayDate);
            String space = getSomeSpace(24);
            resultData.put("reportmarktime", space + reportmakedateTemp);

            //获取属性值

            List<Map<String, Object>> attributeList = (List<Map<String, Object>>) attributedata;
            for (Map<String, Object> map : attributeList) {
                if ("stinkQualityStatus".equals(map.get("reportattributecode"))) {
                    resultData.put("totaldesc", map.get("reportattributevalue"));
                }
                if ("pointChangeSituation".equals(map.get("reportattributecode"))) {
                    resultData.put("imgcontext", map.get("reportattributevalue"));
                }

                if ("controlSuggestions".equals(map.get("reportattributecode"))) {
                    resultData.put("controlSuggestions", map.get("reportattributevalue"));
                }


            }
            resultData.put("imgtitle", dayDate + "变化趋势");
            Map<String, String> MNAndPoints = getAllStinkMNAndPointNames();
            List<Map<String, Object>> tableList = getStinkPointDayTableData(starttime, endtime, MNAndPoints, null, null);
            List<String> topData = new ArrayList<>();
            for (String mnKey : MNAndPoints.keySet()) {
                topData.add(MNAndPoints.get(mnKey));
            }
            resultData.put("topData", topData);
            resultData.put("tableList", tableList);
            Map<String, Object> imgMap = new HashMap<>();
            imgMap.put("imagedata", chartdata);
            resultData.put("imageData", Arrays.asList(imgMap));
            //文件名称
            String ym = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd", "yyyy-MM");
            String fileName = "工业园恶臭分析月报" + ym + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/恶臭分析月报模板.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);

            //存入数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", CommonTypeEnum.AnalysisReportTypeEnum.MonthReportEnum.getCode());
            paramMap.put("analysisreportstarttime", starttime + " 00:00:00");
            paramMap.put("analysisreportendtime", endtime + " 23:59:59");
            paramMap.put("reportmakedate", reportmakedate);
            paramMap.put("attributedata", attributeList);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            paramMap.put("username", username);
            reportManagementService.updateReportAttributeDataByParam(paramMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/19 0019 上午 11:32
     * @Description: 生成恶臭专报文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime：yyyy-mm-dd HH
     * @return:
     */
    @RequestMapping(value = "createStinkSpecialReport", method = RequestMethod.POST)
    public void createStinkSpecialReport(
            @RequestJson("starttime") String starttime,
            @RequestJson("endtime") String endtime,
            @RequestJson(value = "gismapdata", required = false) String gismapdata,
            @RequestJson(value = "entstinkchartdata", required = false) String entstinkchartdata,
            @RequestJson("reportmakedate") String reportmakedate,
            @RequestJson(value = "BZpointchartdata", required = false) Object BZpointchartdata,
            @RequestJson(value = "GWHpointchartdata", required = false) Object GWHpointchartdata,
            @RequestJson("attributedata") Object attributedata,
            HttpServletRequest request,
            HttpServletResponse response

    ) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            //设置时间值
            String dayDate = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH", "yyyy年MM月dd日");
            String reportmakedateTemp = DataFormatUtil.FormatDateOneToOther(reportmakedate, "yyyy-MM-dd", "yyyyMMdd");
            resultData.put("daydate", dayDate);
            String space = getSomeSpace(24);
            resultData.put("reportmarktime", space + reportmakedateTemp);
            //厂界恶臭图片数据
            resultData.put("entstinkchartdata", entstinkchartdata);
            //地图图片数据
            resultData.put("gismapdata", gismapdata);
            //配置属性值数据
            List<Map<String, Object>> attributeList = (List<Map<String, Object>>) attributedata;
            for (Map<String, Object> map : attributeList) {
                resultData.put(map.get("reportattributecode").toString(), map.get("reportattributevalue"));
            }
            //敏感点图片数据
            Map<String, Object> BZpointChart = (Map<String, Object>) BZpointchartdata;
            resultData.put("BZpointWindData", BZpointChart.get("pointWindData"));
            resultData.put("BZpointPollutantData", BZpointChart.get("pointPollutantData"));
            Map<String, Object> GWHpointChartList = (Map<String, Object>) GWHpointchartdata;
            resultData.put("GWHpointWindData", GWHpointChartList.get("pointWindData"));
            resultData.put("GWHpointPollutantData", GWHpointChartList.get("pointPollutantData"));

            //监测企业表格数据
            Map<String, String> MNAndPoints = new LinkedHashMap<>();
            MNAndPoints.putAll(onlineService.getMNAndMonitorPoint(new ArrayList<>(), CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode()));
            List<String> mns = new ArrayList<>(MNAndPoints.keySet());
            //获取厂界恶臭列表数据
            List<Map<String, Object>> entStinkTableData = getStinkPointManyHourTableData(starttime, endtime, MNAndPoints, null);
            resultData.put("tabletitle", MNAndPoints.values());
            resultData.put("tableList", entStinkTableData);
            //文件名称
            String ymd = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH", "yyyy-MM-dd");
            String fileName = "恶臭举报分析专报" + ymd + ".doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/恶臭举报分析专报模板.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);

            //存入数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("reporttype", CommonTypeEnum.AnalysisReportTypeEnum.ExpertReportEnum.getCode());
            paramMap.put("analysisreportstarttime", starttime + ":00:00");
            paramMap.put("analysisreportendtime", endtime + ":59:59");
            paramMap.put("reportmakedate", reportmakedate);
            paramMap.put("attributedata", attributeList);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            paramMap.put("username", username);
            reportManagementService.updateReportAttributeDataByParam(paramMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 9:05
     * @Description: 获取日表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointDayTableData(String starttime, String endtime, Map<String, String> MNAndPoints, List<Map<String, Object>> reportAttributeDataList, List<Map<String, Object>> chartDataList) {
        List<String> ymds = DataFormatUtil.getYMDBetween(starttime, endtime);
        ymds.add(endtime);
        Map<String, Object> dayAndText = new LinkedHashMap<>();
        for (int i = 0; i < ymds.size(); i++) {
            dayAndText.put(ymds.get(i), (i + 1) + "日");
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
        List<String> mns = new ArrayList<>(MNAndPoints.keySet());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("starttime", starttime + " 00:00:00");
        paramMap.put("endtime", endtime + " 23:59:59");
        paramMap.put("mns", mns);
        paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
        paramMap.put("collection", db_dayData);
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        if (documents.size() > 0) {
            String mn;
            String monitortime;
            List<Map<String, Object>> pollutantList;
            Double monitorvalue = null;
            //获取上个月数据
            Date preMonth = DataFormatUtil.getDateYM(starttime);
            String preStartTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreMonthDate(preMonth, 1));
            String preM = DataFormatUtil.getDateYM(preMonth);
            String preEndTime = DataFormatUtil.getLastDayOfMonth(preM) + " 23:59:59";
            paramMap.put("starttime", preStartTime);
            paramMap.put("endtime", preEndTime);
            List<Document> preDocuments = onlineService.getMonitorDataByParamMap(paramMap);

            Map<String, List<Double>> mnAndValues = new HashMap<>();
            List<Double> values;
            for (Document document : preDocuments) {
                mn = document.getString("DataGatherCode");
                pollutantList = (List<Map<String, Object>>) document.get("DayDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                }
            }
            //获取上个月各个点位平均值
            Map<String, Double> mnAndAvgPre = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgPre.put(mnKey, monitorvalue);
                }
            }
            //当前月数据
            mnAndValues.clear();
            Map<String, Map<String, Double>> timeAndMnAndValue = new HashMap<>();
            Map<String, Double> mnAndValue;
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                pollutantList = (List<Map<String, Object>>) document.get("DayDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                    if (timeAndMnAndValue.containsKey(monitortime)) {
                        mnAndValue = timeAndMnAndValue.get(monitortime);
                    } else {
                        mnAndValue = new HashMap<>();
                    }
                    mnAndValue.put(mn, monitorvalue);
                    timeAndMnAndValue.put(monitortime, mnAndValue);
                }
            }
            //获取当天各个点位平均值
            Map<String, Double> mnAndAvgCurr = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgCurr.put(mnKey, monitorvalue);
                }
            }
            //排序
            Map<String, Double> mnAndAvgCurrSort = DataFormatUtil.sortMapByValue(mnAndAvgCurr, true);

            Map<String, String> MNAndPointsSort = new LinkedHashMap<>();
            Map<String, Integer> mnAndSort = new HashMap<>();
            int num = 0;
            List<Object> dayList = new ArrayList<>();

            for (String mnKey : mnAndAvgCurrSort.keySet()) {
                num++;
                mnAndSort.put(mnKey, num);
                dayList.add(num);
                MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                MNAndPoints.remove(mnKey);
            }
            if (MNAndPoints.size() > 0) {
                for (String mnKey : MNAndPoints.keySet()) {
                    MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                    dayList.add("-");
                }
            }
            for (String day : dayAndText.keySet()) {
                Map<String, Object> reMap = new LinkedHashMap<>();
                reMap.put("yData", dayAndText.get(day));
                reMap.put("xyData", setPointData(timeAndMnAndValue.get(day), MNAndPointsSort));
                resultList.add(reMap);
            }
            Map<String, Object> dayAvgMap = new LinkedHashMap<>();
            dayAvgMap.put("yData", "月平均");
            dayAvgMap.put("xyData", setPointData(mnAndAvgCurr, MNAndPointsSort));
            resultList.add(dayAvgMap);
            Map<String, Object> dayHBMap = new LinkedHashMap<>();
            dayHBMap.put("yData", "月环比");
            dayHBMap.put("xyData", setDayHBData(mnAndAvgPre, mnAndAvgCurr, MNAndPointsSort));
            resultList.add(dayHBMap);
            Map<String, Object> dayListMap = new LinkedHashMap<>();
            dayListMap.put("yData", "月排名");
            dayListMap.put("xyData", dayList);
            resultList.add(dayListMap);
            //排序
            Map<String, Double> mnAndAvgPreSort = mnAndAvgPre.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            Map<String, Integer> mnAndSortPre = new HashMap<>();
            num = 0;
            for (String mnKey : mnAndAvgPreSort.keySet()) {
                num++;
                mnAndSortPre.put(mnKey, num);
            }
            Map<String, Object> dayChangeMap = new LinkedHashMap<>();
            dayChangeMap.put("yData", "排名变化");
            dayChangeMap.put("xyData", getDayChange(mnAndSortPre, mnAndSort, MNAndPointsSort));
            resultList.add(dayChangeMap);
            MNAndPoints.clear();
            MNAndPoints.putAll(MNAndPointsSort);

            if (reportAttributeDataList != null) {

                boolean isUpper = false;
                List<String> pointNameList = new ArrayList<>();

                //浓度较高点位名称
                String highPointNames = "";
                //超标点名称
                String overPointNames = "";
                String overTimeString = "";


                List<String> highDgimn = new ArrayList<>();


                //环境恶臭点位信息
                Map<String, Object> paramMapTemp = new HashMap<>();
                paramMapTemp.put("outputids", new ArrayList<>());
                paramMapTemp.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                paramMapTemp.put("monitorPointCategory", "1");
                List<Map<String, Object>> pointDataList = onlineService.getMonitorPointDataByParam(paramMapTemp);
                List<String> MGPointList = new ArrayList<>();
                List<String> MGMN = new ArrayList<>();
                String mnCommon;
                String pointname;
                for (Map<String, Object> pointData : pointDataList) {
                    pointname = pointData.get("monitorpointname").toString();
                    mnCommon = pointData.get("dgimn").toString();
                    MGPointList.add(pointname);
                    MGMN.add(mnCommon);
                }

                for (Map<String, Object> map : reportAttributeDataList) {
                    //各点位恶臭质量逐日变化情况
                    if ("pointChangeSituation".equals(map.get("reportattributecode"))) {
                        String pointChangeSituation = "由图1可看出，各企业厂界监测点位恶臭浓度变化不大。";
                        String highPointString = "";
                        //获取浓度较高的时间及点位信息（时间：点位=1：n）
                        Map<String, Object> timeListAndPointData = getTimeListAndPointData(chartDataList, "month", MGMN, true);
                        if (timeListAndPointData.size() > 0) {
                            isUpper = true;
                            List<Integer> numList = new ArrayList<>();
                            String ymd;
                            Integer hour;
                            String dgimn;

                            Map<String, Integer> pointAndNum = new HashMap<>();
                            List<Map<String, Object>> upperpointdata = (List<Map<String, Object>>) timeListAndPointData.get("upperpointdata");
                            for (Map<String, Object> upperPoint : upperpointdata) {
                                pointname = upperPoint.get("pointname").toString();
                                dgimn = upperPoint.get("dgimn").toString();
                                if (pointAndNum.containsKey(pointname)) {
                                    pointAndNum.put(pointname, pointAndNum.get(pointname) + 1);
                                } else {
                                    pointAndNum.put(pointname, 1);
                                }
                                if (!highDgimn.contains(dgimn)) {
                                    highDgimn.add(dgimn);
                                }
                            }
                            List<String> timeList = (List<String>) timeListAndPointData.get("timeList");
                            for (String time : timeList) {
                                ymd = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd", "d");
                                hour = Integer.parseInt(ymd);
                                if (!numList.contains(hour)) {
                                    numList.add(hour);
                                }

                            }
                            Collections.sort(numList);
                            ymd = DataFormatUtil.mergeContinueNum(numList, 0, "、", "日");
                            if (StringUtils.isNotBlank(ymd)) {
                                ymd = ymd.substring(0, ymd.length() - 1);
                            }
                            Map<String, Integer> sortMap = DataFormatUtil.sortMapByValue(pointAndNum, true);
                            List<String> upperPointNameList = new ArrayList<>();
                            List<String> CXNameList = new ArrayList<>();
                            for (String name : sortMap.keySet()) {
                                CXNameList.add(name);
                                upperPointNameList.add(name);
                            }
                            //String CXNames = DataFormatUtil.FormatListToString(CXNameList, "、");
                            highPointNames = DataFormatUtil.FormatListToString(upperPointNameList, "、");
                            highPointString = "各企业厂界监测点位恶臭浓度在" + ymd + "的恶臭浓度相对比较高";
                        }
                        //获取超标点位，及时间段
                        List<Map<String, Object>> chartdata;
                        List<String> overTimeList;
                        List<String> timeList = new ArrayList<>();
                        List<String> overPointList = new ArrayList<>();
                        for (Map<String, Object> dataMap : chartDataList) {
                            chartdata = (List<Map<String, Object>>) dataMap.get("chartdata");
                            for (Map<String, Object> data : chartdata) {
                                if (data.get("isoverstandard") != null &&
                                        Boolean.parseBoolean(data.get("isoverstandard").toString())) {
                                    highDgimn.add(data.get("dgimn").toString());
                                    overPointList.add(data.get("monitorpointname").toString());
                                    overTimeList = data.get("overtimelist") != null ? (List<String>) data.get("overtimelist") : null;
                                    if (overTimeList != null) {
                                        timeList.addAll(overTimeList);
                                    }
                                }
                            }
                        }
                        String overString = "";
                        if (timeList.size() > 0) {
                            isUpper = true;
                            pointNameList.addAll(overPointList);
                            overPointNames = DataFormatUtil.FormatListToString(overPointList, "、");
                            String ymdh;
                            int hour;
                            List<Integer> numList = new ArrayList<>();
                            for (String time : timeList) {
                                ymdh = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd", "d");
                                hour = Integer.parseInt(ymdh);
                                if (!numList.contains(hour)) {
                                    numList.add(hour);
                                }
                            }
                            Collections.sort(numList);
                            overTimeString = DataFormatUtil.mergeContinueNum(numList, 0, "、", "日");
                            if (StringUtils.isNotBlank(overTimeString)) {
                                overTimeString = overTimeString.substring(0, overTimeString.length() - 1);
                            }
                            overString = overPointNames + overTimeString + "有超标现象";
                        }
                        if (isUpper) {

                            String mark = "";
                            if (StringUtils.isNotBlank(highPointString) && StringUtils.isNotBlank(overString)) {
                                mark = "，";
                            }
                            pointChangeSituation = "由图1可看出，" + highPointString + mark + overString + "。";
                        }
                        map.put("reportattributevalue", pointChangeSituation);
                    }
                    //管控建议
                    if ("controlSuggestions".equals(map.get("reportattributecode"))) {
                        String controlSuggestions = "无";
                        if (isUpper) {
                            String BQ = "";
                            List<Map<String, Object>> pointChangeList = getMonitorToTimeValues(chartDataList, highDgimn, "month");
                            String upperName = "";
                            if (pointChangeList.size() > 0) {
                                List<String> upperPointNameList = new ArrayList<>();
                                String pointName;
                                String ymd;
                                Integer hour;
                                List<Integer> numList = new ArrayList<>();
                                List<Map<String, Object>> timeData;
                                for (Map<String, Object> pointChange : pointChangeList) {
                                    pointName = pointChange.get("pointname").toString();
                                    upperPointNameList.add(pointName);
                                    timeData = (List<Map<String, Object>>) pointChange.get("uppertimedata");
                                    for (Map<String, Object> uppertime : timeData) {
                                        ymd = DataFormatUtil.FormatDateOneToOther(uppertime.get("time").toString(), "yyyy-MM-dd", "d");
                                        hour = Integer.parseInt(ymd);
                                        if (!numList.contains(hour)) {
                                            numList.add(hour);
                                        }
                                    }
                                }
                                upperName = DataFormatUtil.FormatListToString(upperPointNameList, "、");
                                Collections.sort(numList);
                                ymd = DataFormatUtil.mergeContinueNum(numList, 0, "、", "日");
                                if (StringUtils.isNotBlank(ymd)) {
                                    ymd = ymd.substring(0, ymd.length() - 1);
                                }
                                BQ = "并且" + upperName + "在" + ymd + "浓度均有突升，";
                            }

                            List<Map<String, Object>> QZDataList = new ArrayList<>();
                                 //1115   onlineService.getEntStinkDayRelationDataByParamMap(starttime, endtime, pollutantCode, highDgimn);
                            String qz = "";
                            if (QZDataList.size() > 0) {
                                String stinkPoint;
                                String entStinkPoint;
                                List<String> entStinkPoints = new ArrayList<>();
                                List<Map<String, Object>> similaritydata;
                                for (Map<String, Object> qzData : QZDataList) {
                                    similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                    if (similaritydata.size() > 0) {
                                        entStinkPoints.clear();
                                        stinkPoint = qzData.get("pointname").toString();
                                        similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                        for (Map<String, Object> similarity : similaritydata) {
                                            entStinkPoint = similarity.get("pointname").toString();
                                            if (pointNameList.contains(entStinkPoint)){
                                                entStinkPoints.add(entStinkPoint);
                                            }

                                        }
                                        if (entStinkPoints.size()>0){
                                            entStinkPoint = DataFormatUtil.FormatListToString(entStinkPoints, "、");
                                            qz = qz + entStinkPoint + "与" + stinkPoint + "敏感点浓度变化趋势有一定的相似，";
                                        }

                                    }
                                }
                                if (StringUtils.isNotBlank(qz)) {
                                    qz = "其中，" + qz;
                                }
                            }
                            controlSuggestions = "根据图1各点位日恶臭浓度变化趋势分析，";
                            if (StringUtils.isNotBlank(overPointNames)) {
                                controlSuggestions += overPointNames + overTimeString + "有超标，";
                            }
                            if (StringUtils.isNotBlank(highPointNames)) {
                                controlSuggestions += highPointNames + "恶臭浓度相对比较高，";
                            }
                            pointNameList = pointNameList.stream().distinct().collect(Collectors.toList());
                            String pointNames = DataFormatUtil.FormatListToString(pointNameList, "、");
                            controlSuggestions += BQ
                                    + qz + "根据当天气象条件分析，建议加大对"
                                    + pointNames + "监督巡查力度"
                                    + "。";

                        }
                        map.put("reportattributevalue", controlSuggestions);
                    }
                    //恶臭质量状况
                    if ("stinkQualityStatus".equals(map.get("reportattributecode"))) {
                        if (DataFormatUtil.isObjectNull(map.get("reportattributevalue"))) {
                            String MGPoints = DataFormatUtil.FormatListToString(MGPointList, "、");
                            //厂界恶臭点位信息
                            Map<String, String> mnAndEntStinkPoints = onlineService.getMNAndMonitorPoint(new ArrayList<>(),
                                    CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                            String stinkQualityStatusPartOne =
                                    "以解决工业园区恶臭扰民事件为目的，" +
                                            "提升园区环境精细化监管水平，" +
                                            "在园区企业厂界设置" + mnAndEntStinkPoints.size() + "个监测点位，" +
                                            "并根据历史投诉事件和气象条件分析在园区及其周边设置" + MGPointList.size() + "个敏感监测点位， " +
                                            "来加强恶臭浓度监测；" +
                                            "本月所有监测点位的恶臭浓度汇总情况如下表1所示，" +
                                            "其中" + MGPoints + "为环境敏感点，其他监测点位均布设在企业的厂界位置。";

                            List<String> topThreePoints = new ArrayList<>();
                            num = 0;
                            for (String mnKey : MNAndPointsSort.keySet()) {
                                if (num < 3) {
                                    topThreePoints.add(MNAndPointsSort.get(mnKey));
                                }
                                num++;
                                if (num >= 3) {
                                    break;
                                }
                            }
                            String topThree = DataFormatUtil.FormatListToString(topThreePoints, "、");
                            //获取前三个月的开始日期

                            Date preThreeMonthStartDate = DataFormatUtil.getPreMonthDate(DataFormatUtil.getDateYMD(starttime), 3);
                            preStartTime = DataFormatUtil.getDateYMDHMS(preThreeMonthStartDate);
                            Date preThreeMonthEndDate = DataFormatUtil.getPreMonthDate(DataFormatUtil.getDateYMD(starttime), 1);
                            String tempYM = DataFormatUtil.getDateYM(preThreeMonthEndDate);

                            preEndTime = DataFormatUtil.getYearMothLast(tempYM) + " 23:59:59";
                            paramMap.put("starttime", preStartTime);
                            paramMap.put("endtime", preEndTime);
                            Date monitorDate;
                            String ym;
                            Map<String, List<Document>> ymAndData = new HashMap<>();
                            List<Document> documentData;
                            List<Document> pre21Documents = onlineService.getMonitorDataByParamMap(paramMap);
                            for (Document document : pre21Documents) {
                                monitorDate = document.getDate("MonitorTime");
                                ym = DataFormatUtil.getDateYM(DataFormatUtil.getFirstDayOfWeek(monitorDate));
                                if (ymAndData.containsKey(ym)) {
                                    documentData = ymAndData.get(ym);
                                } else {
                                    documentData = new ArrayList<>();
                                }
                                documentData.add(document);
                                ymAndData.put(ym, documentData);
                            }
                            String QZPoint = "";
                            if (ymAndData.size() > 0) {
                                Map<String, Integer> pointAndNum = new HashMap<>();
                                mnAndAvgPre.clear();
                                mnAndValues.clear();
                                for (String ymKey : ymAndData.keySet()) {
                                    documentData = ymAndData.get(ymKey);
                                    for (Document document : documentData) {
                                        pollutantList = (List<Map<String, Object>>) document.get("DayDataList");
                                        for (Map<String, Object> pollutant : pollutantList) {
                                            if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                                                monitorvalue = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                                break;
                                            }
                                        }
                                        if (monitorvalue != null) {
                                            mn = document.getString("DataGatherCode");
                                            if (mnAndValues.containsKey(mn)) {
                                                values = mnAndValues.get(mn);
                                            } else {
                                                values = new ArrayList<>();
                                            }
                                            values.add(monitorvalue);
                                            mnAndValues.put(mn, values);
                                        }
                                    }
                                    if (mnAndValues.size() > 0) {
                                        for (String mnKey : mnAndValues.keySet()) {
                                            monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                                            mnAndAvgPre.put(mnKey, monitorvalue);
                                        }
                                        //排序
                                        mnAndAvgPreSort = DataFormatUtil.sortMapByValue(mnAndAvgPre, true);
                                        num = 0;
                                        String name;
                                        for (String mnKey : mnAndAvgPreSort.keySet()) {
                                            name = MNAndPoints.get(mnKey);
                                            if (num < 3 && topThreePoints.contains(name)) {
                                                if (pointAndNum.containsKey(name)) {
                                                    pointAndNum.put(name, pointAndNum.get(name) + 1);
                                                } else {
                                                    pointAndNum.put(name, 1);
                                                }
                                            }

                                            num++;
                                            if (num >= 3) {
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (pointAndNum.size() > 0) {
                                    List<String> QZPoints = new ArrayList<>();
                                    for (String point : pointAndNum.keySet()) {
                                        if (pointAndNum.get(point) >= 2) {
                                            QZPoints.add(point);
                                        }
                                    }
                                    QZPoint = DataFormatUtil.FormatListToString(QZPoints, "、");
                                }
                            }
                            String QZText = "";
                            if (StringUtils.isNotBlank(QZPoint)) {
                                QZText = "，其中" + QZPoint + "恶臭月平均浓度连续三个月排名位于前三位";
                            }
                            String changeText = getHBChangeText(mnAndAvgPre, mnAndAvgCurr);
                            String stinkQualityStatusPartTwo = "由表1中各监测点位恶臭的日浓度分析可知，" + topThree + "的恶臭浓度相对较高" + QZText + "。"
                                    + "由周环比情况分析可知，本月工业园区的恶臭质量变化" + changeText + " 。";
                            map.put("reportattributevalue", stinkQualityStatusPartOne + "\r\n" + stinkQualityStatusPartTwo);
                        }
                    }
                }
            }

        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 9:05
     * @Description: 获取小时表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointHourTableData(String ymd, Map<String, String> MNAndPoints, List<Map<String, Object>> reportAttributeDataList, List<Map<String, Object>> chartDataList) {

        Map<String, Object> hourAndText = new LinkedHashMap<>();
        for (int i = 0; i < 24; i++) {
            hourAndText.put(ymd + " " + i, i + "时");
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
        List<String> mns = new ArrayList<>(MNAndPoints.keySet());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("starttime", ymd + " 00:00:00");
        paramMap.put("endtime", ymd + " 23:59:59");
        paramMap.put("mns", mns);
        paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
        paramMap.put("collection", db_hourData);
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        if (documents.size() > 0) {
            String mn;
            String monitortime;
            List<Map<String, Object>> pollutantList;
            Double monitorvalue;
            //获取昨天数据
            String preStartTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMD(ymd), 1));
            String preEndTime = DataFormatUtil.getDateYMD(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMD(ymd), 1)) + " 23:59:59";
            paramMap.put("starttime", preStartTime);
            paramMap.put("endtime", preEndTime);
            List<Document> preDocuments = onlineService.getMonitorDataByParamMap(paramMap);

            Map<String, List<Double>> mnAndValues = new HashMap<>();
            List<Double> values;
            for (Document document : preDocuments) {
                monitorvalue = null;
                pollutantList = (List<Map<String, Object>>) document.get("HourDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    mn = document.getString("DataGatherCode");
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                }
            }
            //获取昨天各个点位平均值
            Map<String, Double> mnAndAvgPre = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgPre.put(mnKey, monitorvalue);
                }
            }
            //当前天数据
            mnAndValues.clear();
            Map<String, Map<String, Double>> timeAndMnAndValue = new HashMap<>();
            Map<String, Double> mnAndValue;
            for (Document document : documents) {
                monitorvalue = null;
                mn = document.getString("DataGatherCode");
                monitortime = DataFormatUtil.getDateYMDoneH(document.getDate("MonitorTime"));
                pollutantList = (List<Map<String, Object>>) document.get("HourDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                    if (timeAndMnAndValue.containsKey(monitortime)) {
                        mnAndValue = timeAndMnAndValue.get(monitortime);
                    } else {
                        mnAndValue = new HashMap<>();
                    }
                    mnAndValue.put(mn, monitorvalue);
                    timeAndMnAndValue.put(monitortime, mnAndValue);
                }
            }
            //获取当天各个点位平均值
            Map<String, Double> mnAndAvgCurr = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgCurr.put(mnKey, monitorvalue);
                }
            }
            //排序
            Map<String, Double> mnAndAvgCurrSort = DataFormatUtil.sortMapByValue(mnAndAvgCurr, true);
            Map<String, String> MNAndPointsSort = new LinkedHashMap<>();
            Map<String, Integer> mnAndSort = new HashMap<>();
            int num = 0;
            List<Object> dayList = new ArrayList<>();
            for (String mnKey : mnAndAvgCurrSort.keySet()) {
                num++;
                mnAndSort.put(mnKey, num);
                dayList.add(num);
                MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                MNAndPoints.remove(mnKey);
            }
            int tempNum = MNAndPoints.size();
            if (MNAndPoints.size() > 0) {
                for (String mnKey : MNAndPoints.keySet()) {
                    MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                    dayList.add("-");
                }
            }
            for (String hour : hourAndText.keySet()) {
                Map<String, Object> reMap = new LinkedHashMap<>();
                reMap.put("yData", hourAndText.get(hour));
                reMap.put("xyData", setPointData(timeAndMnAndValue.get(hour), MNAndPointsSort));
                resultList.add(reMap);
            }
            Map<String, Object> dayAvgMap = new LinkedHashMap<>();
            dayAvgMap.put("yData", "日平均");
            dayAvgMap.put("xyData", setPointData(mnAndAvgCurr, MNAndPointsSort));
            resultList.add(dayAvgMap);
            Map<String, Object> dayHBMap = new LinkedHashMap<>();
            dayHBMap.put("yData", "日环比");
            dayHBMap.put("xyData", setDayHBData(mnAndAvgPre, mnAndAvgCurr, MNAndPointsSort));
            resultList.add(dayHBMap);
            Map<String, Object> dayListMap = new LinkedHashMap<>();
            dayListMap.put("yData", "日排名");
            dayListMap.put("xyData", dayList);
            resultList.add(dayListMap);
            //排序
            Map<String, Double> mnAndAvgPreSort = DataFormatUtil.sortMapByValue(mnAndAvgPre, true);
            Map<String, Integer> mnAndSortPre = new HashMap<>();
            num = 0;
            for (String mnKey : mnAndAvgPreSort.keySet()) {
                num++;
                mnAndSortPre.put(mnKey, num);
            }
            Map<String, Object> dayChangeMap = new LinkedHashMap<>();
            dayChangeMap.put("yData", "排名变化");
            dayChangeMap.put("xyData", getDayChange(mnAndSortPre, mnAndSort, MNAndPointsSort));
            resultList.add(dayChangeMap);
            MNAndPoints.clear();
            MNAndPoints.putAll(MNAndPointsSort);
            //初始化报表属性数据
            if (reportAttributeDataList != null) {
                //是否有浓度较高的点位
                boolean isUpper = false;

                List<String> pointNameList = new ArrayList<>();

                //浓度较高点位名称
                String highPointNames = "";
                //超标点名称
                String overPointNames = "";
                String overTimeString = "";
                //浓度较高的mn号集合
                List<String> highDgimn = new ArrayList<>();
                //环境恶臭点位信息
                Map<String, Object> paramMapTemp = new HashMap<>();
                paramMapTemp.put("outputids", new ArrayList<>());
                paramMapTemp.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                List<Map<String, Object>> pointDataList = onlineService.getMonitorPointDataByParam(paramMapTemp);
                List<String> MGPointList = new ArrayList<>();
                List<String> outMNs = new ArrayList<>();
                String mnCommon;
                String pointname;
                for (Map<String, Object> pointData : pointDataList) {
                    pointname = pointData.get("monitorpointname").toString();
                    mnCommon = pointData.get("dgimn").toString();
                    if ("1".equals(pointData.get("MonitorPointCategory").toString())) {
                        MGPointList.add(pointname);
                    }
                    outMNs.add(mnCommon);
                }
                for (Map<String, Object> map : reportAttributeDataList) {
                    //恶臭质量状况
                    if ("stinkQualityStatus".equals(map.get("reportattributecode"))) {
                        if (map.get("reportattributevalue") == null || "".equals(map.get("reportattributevalue"))) {
                            String MGPointNames = DataFormatUtil.FormatListToString(MGPointList, "、");
                            String stinkQualityStatus1 =
                                    "以解决工业园区恶臭扰民事件为目的，" +
                                            "提升园区环境精细化监管水平，" +
                                            "在园区企业厂界设置" + (MNAndPointsSort.size() - outMNs.size()) + "个监测点位，" +
                                            "并根据历史投诉事件和气象条件分析在园区及其周边设置" + MGPointList.size() + "个敏感监测点位， " +
                                            "来加强恶臭浓度监测；" +
                                            "当日所有监测点位的恶臭浓度汇总情况如下表1所示，" +
                                            "其中" + MGPointNames + "为环境敏感点，其他监测点位均布设在企业的厂界位置。";
                            //列表文字描述
                            String starttime = ymd + " 00";
                            String endtime = ymd + " 23";
                            String tableText = getHourTableText(MNAndPointsSort, dayList, starttime, endtime, mnAndAvgPre, mnAndAvgCurr);
                            map.put("reportattributevalue", stinkQualityStatus1 + "\r\n" + tableText);
                        }
                    }
                    //各点位恶臭质量逐日变化情况
                    if ("pointChangeSituation".equals(map.get("reportattributecode"))) {
                        String pointChangeSituation = "由图1可看出，各企业厂界监测点位恶臭浓度变化不大。";
                        String highPointString = "";
                        //获取浓度较高的时间及点位信息（时间：点位=1：n）
                        Map<String, Object> timeListAndPointData = getTimeListAndPointData(chartDataList, "day", outMNs, true);
                        if (timeListAndPointData.size() > 0) {
                            isUpper = true;
                            List<Integer> numList = new ArrayList<>();
                            String ymdh;
                            Integer hour;
                            String dgimn;
                            Map<String, Integer> pointAndNum = new HashMap<>();
                            List<Map<String, Object>> upperpointdata = (List<Map<String, Object>>) timeListAndPointData.get("upperpointdata");
                            for (Map<String, Object> upperPoint : upperpointdata) {
                                pointname = upperPoint.get("pointname").toString();
                                dgimn = upperPoint.get("dgimn").toString();
                                if (pointAndNum.containsKey(pointname)) {
                                    pointAndNum.put(pointname, pointAndNum.get(pointname) + 1);
                                } else {
                                    pointAndNum.put(pointname, 1);
                                }
                                if (!highDgimn.contains(dgimn)) {
                                    highDgimn.add(dgimn);
                                }
                            }
                            List<String> timeList = (List<String>) timeListAndPointData.get("timeList");
                            for (String time : timeList) {
                                ymdh = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH", "H");
                                hour = Integer.parseInt(ymdh);
                                if (!numList.contains(hour)) {
                                    numList.add(hour);
                                }
                            }
                            Collections.sort(numList);
                            ymdh = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                            if (StringUtils.isNotBlank(ymdh)) {
                                ymdh = ymdh.substring(0, ymdh.length() - 1);
                            }
                            Map<String, Integer> sortMap = DataFormatUtil.sortMapByValue(pointAndNum, true);

                            List<String> CXNameList = new ArrayList<>();
                            for (String name : sortMap.keySet()) {
                                CXNameList.add(name);
                                pointNameList.add(name);
                            }
                            String CXNames = DataFormatUtil.FormatListToString(CXNameList, "、");
                            highPointNames = DataFormatUtil.FormatListToString(pointNameList, "、");
                            highPointString = "各企业厂界监测点位恶臭浓度在" + ymdh + "的恶臭浓度相对比较高";
                        }
                        //获取超标点位，及时间段

                        List<Map<String, Object>> chartdata;
                        List<String> overTimeList;
                        List<String> timeList = new ArrayList<>();
                        List<String> overPointList = new ArrayList<>();
                        for (Map<String, Object> dataMap : chartDataList) {
                            chartdata = (List<Map<String, Object>>) dataMap.get("chartdata");
                            for (Map<String, Object> data : chartdata) {
                                if (data.get("isoverstandard") != null &&
                                        Boolean.parseBoolean(data.get("isoverstandard").toString())) {
                                    highDgimn.add(data.get("dgimn").toString());
                                    overPointList.add(data.get("monitorpointname").toString());
                                    overTimeList = data.get("overtimelist") != null ? (List<String>) data.get("overtimelist") : null;
                                    if (overTimeList != null) {
                                        timeList.addAll(overTimeList);
                                    }

                                }
                            }
                        }
                        String overString = "";
                        if (timeList.size() > 0) {
                            isUpper = true;
                            pointNameList.addAll(overPointList);
                            overPointNames = DataFormatUtil.FormatListToString(overPointList, "、");
                            String ymdh;
                            int hour;
                            List<Integer> numList = new ArrayList<>();
                            for (String time : timeList) {
                                ymdh = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH", "H");
                                hour = Integer.parseInt(ymdh);
                                if (!numList.contains(hour)) {
                                    numList.add(hour);
                                }
                            }
                            Collections.sort(numList);
                            overTimeString = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                            if (StringUtils.isNotBlank(overTimeString)) {
                                overTimeString = overTimeString.substring(0, overTimeString.length() - 1);
                            }
                            overString = overPointNames + overTimeString + "有超标现象";
                        }
                        if (isUpper) {
                            String mark = "";
                            if (StringUtils.isNotBlank(highPointString) && StringUtils.isNotBlank(overString)) {
                                mark = "，";
                            }
                            pointChangeSituation = "由图1可看出，" + highPointString + mark + overString + "。";
                        }
                        map.put("reportattributevalue", pointChangeSituation);
                    }
                    //管控建议
                    if ("controlSuggestions".equals(map.get("reportattributecode"))) {
                        String controlSuggestions = "无";
                        if (isUpper) {
                            String BQ = "";
                            List<Map<String, Object>> pointChangeList = getMonitorToTimeValues(chartDataList, highDgimn, "day");
                            String upperName = "";
                            if (pointChangeList.size() > 0) {
                                List<String> upperPointNameList = new ArrayList<>();
                                String pointName;
                                String ymdh;
                                Integer hour;
                                List<Integer> numList = new ArrayList<>();
                                List<Map<String, Object>> timeData;
                                for (Map<String, Object> pointChange : pointChangeList) {
                                    pointName = pointChange.get("pointname").toString();
                                    upperPointNameList.add(pointName);
                                    timeData = (List<Map<String, Object>>) pointChange.get("uppertimedata");
                                    for (Map<String, Object> uppertime : timeData) {
                                        ymdh = DataFormatUtil.FormatDateOneToOther(uppertime.get("time").toString(), "yyyy-MM-dd HH", "H");
                                        hour = Integer.parseInt(ymdh);
                                        if (!numList.contains(hour)) {
                                            numList.add(hour);
                                        }
                                    }
                                }
                                upperName = DataFormatUtil.FormatListToString(upperPointNameList, "、");
                                Collections.sort(numList);
                                ymdh = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                                if (StringUtils.isNotBlank(ymdh)) {
                                    ymdh = ymdh.substring(0, ymdh.length() - 1);
                                }
                                BQ = "并且" + upperName + "在" + ymdh + "浓度均有突升，";
                            }
                            String starttime = ymd + " 00";
                            String endtime = ymd + " 23";
                            List<Map<String, Object>> QZDataList = new ArrayList<>();
                                   //去掉相似度 1115 onlineService.getEntStinkHourRelationDataByParamMap(starttime, endtime, pollutantCode, highDgimn);
                            String qz = "";
                            if (QZDataList.size() > 0) {
                                String stinkPoint;
                                String entStinkPoint;
                                List<String> entStinkPoints = new ArrayList<>();
                                List<Map<String, Object>> similaritydata;
                                for (Map<String, Object> qzData : QZDataList) {
                                    similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                    if (similaritydata.size() > 0) {
                                        entStinkPoints.clear();
                                        stinkPoint = qzData.get("pointname").toString();
                                        similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                        System.out.println(similaritydata);
                                        for (Map<String, Object> similarity : similaritydata) {
                                            entStinkPoint = similarity.get("pointname").toString();
                                            if (pointNameList.contains(entStinkPoint)) {
                                                entStinkPoints.add(entStinkPoint);
                                            }
                                        }
                                        if (entStinkPoints.size() > 0) {
                                            entStinkPoint = DataFormatUtil.FormatListToString(entStinkPoints, "、");
                                            qz = qz + entStinkPoint + "与" + stinkPoint + "敏感点浓度变化趋势有一定的相似，";
                                        }
                                    }
                                }
                                if (StringUtils.isNotBlank(qz)) {
                                    qz = "其中，" + qz;
                                }
                            }
                            String YQ = "";
                            if (StringUtils.isNotBlank(upperName)) {
                                // YQ = "，尤其是" + upperName;
                            }
                            controlSuggestions = "根据图1各点位小时恶臭浓度变化趋势分析，";
                            if (StringUtils.isNotBlank(overPointNames)) {
                                controlSuggestions += overPointNames + overTimeString + "有超标，";
                            }
                            if (StringUtils.isNotBlank(highPointNames)) {
                                controlSuggestions += highPointNames + "恶臭浓度相对比较高，";
                            }
                            pointNameList = pointNameList.stream().distinct().collect(Collectors.toList());
                            String pointNames = DataFormatUtil.FormatListToString(pointNameList, "、");
                            controlSuggestions += BQ
                                    + qz + "根据当天气象条件分析，建议加大对"
                                    + pointNames + "监督巡查力度"
                                    + "。";


                        }
                        map.put("reportattributevalue", controlSuggestions);
                    }
                }
            }


        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/9/27 0027 上午 11:15
     * @Description: 获取小时数据列表描述文字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getHourTableText(Map<String, String> MNAndPointsSort,
                                    List<Object> dayList,
                                    String starttime,
                                    String endtime,
                                    Map<String, Double> mnAndAvgPre,
                                    Map<String, Double> mnAndAvgCurr
    ) {
        String tableText = "";

        List<String> entPoints = new ArrayList<>();
        int num = 0;
        List<String> inMn = new ArrayList<>();
        List<String> mns = new ArrayList<>();
        for (String entKey : MNAndPointsSort.keySet()) {
            if (!dayList.get(num).equals("-") && num < 3) {
                entPoints.add(MNAndPointsSort.get(entKey));

                inMn.add(entKey);
            }
            num++;
            mns.add(entKey);
        }
        String entPointNames = DataFormatUtil.FormatListToString(entPoints, "、");
        String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
        Map<String, Object> paramMap = new HashMap<>();

        String preStartTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMDH(starttime), 6));
        paramMap.put("starttime", preStartTime);
        paramMap.put("endtime", endtime + ":59:59");
        paramMap.put("mns", mns);
        paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
        paramMap.put("collection", db_hourData);
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        Map<String, Map<String, List<Double>>> timeAndMnAndValues = new HashMap<>();

        List<Map<String, Object>> pollutantList;
        Double monitorvalue;
        String monitortime;
        String mn;
        Map<String, List<Double>> mnAndValues;
        List<Double> values;
        for (Document document : documents) {
            monitorvalue = null;
            pollutantList = (List<Map<String, Object>>) document.get("HourDataList");
            for (Map<String, Object> pollutant : pollutantList) {
                if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                    monitorvalue = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                    break;
                }
            }
            if (monitorvalue != null) {
                monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                mn = document.getString("DataGatherCode");
                if (timeAndMnAndValues.containsKey(monitortime)) {
                    mnAndValues = timeAndMnAndValues.get(monitortime);
                } else {
                    mnAndValues = new HashMap<>();
                }

                if (mnAndValues.containsKey(mn)) {
                    values = mnAndValues.get(mn);
                } else {
                    values = new ArrayList<>();
                }
                values.add(monitorvalue);
                mnAndValues.put(mn, values);
                timeAndMnAndValues.put(monitortime, mnAndValues);
            }
        }
        String sevenData = "";
        if (timeAndMnAndValues.size() > 0) {

            Map<String, Integer> mnAndNum = new HashMap<>();

            Map<String, Double> mnAndAvgSort;
            for (String time : timeAndMnAndValues.keySet()) {
                Map<String, Double> mnAndAvg = new HashMap<>();
                mnAndValues = timeAndMnAndValues.get(time);
                for (String mnkey : mnAndValues.keySet()) {
                    mnAndAvg.put(mnkey, Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnkey))));
                }
                mnAndAvgSort = DataFormatUtil.sortMapByValue(mnAndAvg, true);
                num = 0;
                for (String mnKey : mnAndAvgSort.keySet()) {
                    if (mnAndNum.containsKey(mnKey)) {
                        mnAndNum.put(mnKey, mnAndNum.get(mnKey) + 1);
                    } else {
                        mnAndNum.put(mnKey, 1);
                    }
                    num++;
                    if (num > 2) {
                        break;
                    }
                }
            }
            String sevenPoint = "";
            if (mnAndNum.size() > 0) {
                for (String mnKey : mnAndNum.keySet()) {
                    if (mnAndNum.get(mnKey) > 3 && inMn.contains(mnKey)) {
                        sevenPoint += MNAndPointsSort.get(mnKey) + "、";
                    }
                }
                if (StringUtils.isNotBlank(sevenPoint)) {
                    sevenPoint = sevenPoint.substring(0, sevenPoint.length() - 1);
                }
                sevenData = "其中" + sevenPoint + "恶臭日平均浓度连续七天排名位于前三位 。";
            }
        }
        String HBData = getHBChangeText(mnAndAvgPre, mnAndAvgCurr);
        tableText = "由表1中各监测点位恶臭的小时浓度分析可知，" + entPointNames + "的恶臭浓度相对较高，" +
                sevenData + "由日环比情况分析可知，今日工业园区的恶臭质量变化" + HBData + "。";
        return tableText;
    }


    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 9:05
     * @Description: 获取小时表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointManyHourTableData(String starttime, String endtime, Map<String, String> MNAndPoints, List<Map<String, Object>> reportAttributeDataList) {

        Map<String, Object> hourAndText = new LinkedHashMap<>();
        List<String> ymdhs = DataFormatUtil.getYMDHBetween(starttime, endtime);
        ymdhs.add(endtime);
        for (String ymdh : ymdhs) {
            hourAndText.put(ymdh, DataFormatUtil.FormatDateOneToOther(ymdh, "yyyy-MM-dd HH", "d日H时"));
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
        List<String> mns = new ArrayList<>(MNAndPoints.keySet());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("starttime", starttime + ":00:00");
        paramMap.put("endtime", endtime + ":59:59");
        paramMap.put("mns", mns);
        paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
        paramMap.put("collection", db_hourData);
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        if (documents.size() > 0) {
            String mn;
            String monitortime;
            List<Map<String, Object>> pollutantList;
            Double monitorvalue = null;
            Map<String, List<Double>> mnAndValues = new HashMap<>();
            List<Double> values;

            mnAndValues.clear();
            //获取昨天数据
            String preStartTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMDH(starttime), 1));
            String preEndTime = DataFormatUtil.getDateYMDH(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMDH(endtime), 1)) + ":59:59";
            paramMap.put("starttime", preStartTime);
            paramMap.put("endtime", preEndTime);
            List<Document> preDocuments = onlineService.getMonitorDataByParamMap(paramMap);
            for (Document document : preDocuments) {
                monitorvalue = null;
                pollutantList = (List<Map<String, Object>>) document.get("HourDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    mn = document.getString("DataGatherCode");
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                }
            }
            //获取昨天各个点位平均值
            Map<String, Double> mnAndAvgPre = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgPre.put(mnKey, monitorvalue);
                }
            }


            Map<String, Map<String, Double>> timeAndMnAndValue = new HashMap<>();
            Map<String, Double> mnAndValue;
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                pollutantList = (List<Map<String, Object>>) document.get("HourDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                    if (timeAndMnAndValue.containsKey(monitortime)) {
                        mnAndValue = timeAndMnAndValue.get(monitortime);
                    } else {
                        mnAndValue = new HashMap<>();
                    }
                    mnAndValue.put(mn, monitorvalue);
                    timeAndMnAndValue.put(monitortime, mnAndValue);
                }
            }
            Map<String, String> MNAndPointsSort = new LinkedHashMap<>();
            if (MNAndPoints.size() > 0) {
                for (String mnKey : MNAndPoints.keySet()) {
                    MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                }
            }
            for (String hour : hourAndText.keySet()) {
                Map<String, Object> reMap = new LinkedHashMap<>();
                reMap.put("yData", hourAndText.get(hour));
                reMap.put("xyData", setPointData(timeAndMnAndValue.get(hour), MNAndPointsSort));
                resultList.add(reMap);
            }
            //获取当天各个点位平均值
            Map<String, Double> mnAndAvgCurr = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgCurr.put(mnKey, monitorvalue);
                }
            }
            //排序
            Map<String, Double> mnAndAvgCurrSort = DataFormatUtil.sortMapByValue(mnAndAvgCurr, true);
            Map<String, Integer> mnAndSort = new HashMap<>();
            int num = 0;
            List<Object> dayList = new ArrayList<>();
            for (String mnKey : mnAndAvgCurrSort.keySet()) {
                num++;
                mnAndSort.put(mnKey, num);
                dayList.add(num);
                MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                MNAndPoints.remove(mnKey);
            }
            if (MNAndPoints.size() > 0) {
                for (String mnKey : MNAndPoints.keySet()) {
                    MNAndPointsSort.put(mnKey, MNAndPoints.get(mnKey));
                    dayList.add("-");
                }
            }
            Map<String, Object> dayAvgMap = new LinkedHashMap<>();
            dayAvgMap.put("yData", "日平均");
            dayAvgMap.put("xyData", setPointData(mnAndAvgCurr, MNAndPointsSort));
            resultList.add(dayAvgMap);
            Map<String, Object> dayHBMap = new LinkedHashMap<>();
            dayHBMap.put("yData", "日环比");
            dayHBMap.put("xyData", setDayHBData(mnAndAvgPre, mnAndAvgCurr, MNAndPointsSort));
            resultList.add(dayHBMap);
            Map<String, Object> dayListMap = new LinkedHashMap<>();
            dayListMap.put("yData", "日排名");
            dayListMap.put("xyData", dayList);
            resultList.add(dayListMap);

            MNAndPoints.clear();
            MNAndPoints.putAll(MNAndPointsSort);
            if (reportAttributeDataList != null) {//
                for (Map<String, Object> map : reportAttributeDataList) {
                    //恶臭质量状况
                    if ("entStinkTableData".equals(map.get("reportattributecode"))) {
                        if (map.get("reportattributevalue") == null || "".equals(map.get("reportattributevalue"))) {
                            //列表文字描述
                            String tableText = getHourTableText(MNAndPointsSort, dayList, starttime, endtime, mnAndAvgPre, mnAndAvgCurr);
                            map.put("reportattributevalue", tableText);
                        }
                    }
                }

            }

        }
        return resultList;
    }


    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 10:46
     * @Description: 设置各个点位排名变化数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Object> getDayChange(Map<String, Integer> mnAndSortPre, Map<String, Integer> mnAndSort, Map<String, String> mnAndPoints) {
        List<Object> mnData = new ArrayList<>();
        for (String mnKey : mnAndPoints.keySet()) {
            mnData.add(getChangeRate(mnAndSortPre.get(mnKey), mnAndSort.get(mnKey)));
        }
        return mnData;
    }

    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 10:46
     * @Description: 设置各个点位日环比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Object> setDayHBData(Map<String, Double> mnAndAvgPre, Map<String, Double> mnAndAvgCurr, Map<String, String> mnAndPoints) {
        List<Object> mnData = new ArrayList<>();
        for (String mnKey : mnAndPoints.keySet()) {
            mnData.add(getHBRate(mnAndAvgPre.get(mnKey), mnAndAvgCurr.get(mnKey)));
        }
        return mnData;
    }

    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 9:55
     * @Description: 设置点位小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Object> setPointData(Map<String, Double> mnAndValue, Map<String, String> mnAndPoints) {
        List<Object> mnData = new ArrayList<>();
        for (String mnKey : mnAndPoints.keySet()) {
            if (mnAndValue != null) {
                mnData.add(mnAndValue.get(mnKey) != null ? mnAndValue.get(mnKey) : "-");
            } else {
                mnData.add("-");
            }

        }
        return mnData;
    }


    /**
     * @author: lip
     * @date: 2019/8/23 0023 上午 8:45
     * @Description: 获取所有恶臭（厂界+环境）mn和名称关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, String> getAllStinkMNAndPointNames() {
        Map<String, String> MNAndPoints = new LinkedHashMap<>();
        List<Integer> stinkPoints = Arrays.asList(
                CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(),
                CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        for (Integer type : stinkPoints) {
            MNAndPoints.putAll(onlineService.getMNAndMonitorPoint(new ArrayList<>(), type));
        }
        return MNAndPoints;
    }


    /**
     * @author: lip
     * @date: 2019/8/21 0021 下午 2:17
     * @Description: 获取恶臭点位小时图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointHourChartData(String starttime, String endtime) throws Exception {
        List<Map<String, Object>> imageData = new ArrayList<>();
        List<String> ymds = DataFormatUtil.getYMDBetween(starttime, endtime);
        ymds.add(endtime);
        String Hstarttime;
        String Hendtime;
        List<Map<String, Object>> dataList;
        for (int i = 0; i < ymds.size(); i++) {
            Hstarttime = ymds.get(i) + " 00";
            Hendtime = ymds.get(i) + " 23";
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("monitortime", ymds.get(i));
            dataList = getStinkManyPointDataByParams(3, Hstarttime, Hendtime);
            if (dataList.size() > 0) {
                map.put("chartdata", dataList);
                imageData.add(map);
            }
        }
        return imageData;

    }


    /**
     * @author: lip
     * @date: 2019/8/21 0021 上午 9:02
     * @Description: 获取恶臭周数据（各个点位）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkPointWeekDayData(String starttime, String endtime, List<Map<String, Object>> reportAttributeDataList, List<Map<String, Object>> chartDataList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, String> MNAndPoints = getAllStinkMNAndPointNames();
        String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
        List<String> mns = new ArrayList<>(MNAndPoints.keySet());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("starttime", starttime + " 00:00:00");
        paramMap.put("endtime", endtime + " 23:59:59");
        paramMap.put("mns", mns);
        paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
        paramMap.put("collection", db_dayData);
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        if (documents.size() > 0) {
            String mn;
            String monitortime;
            List<Map<String, Object>> pollutantList;
            Double monitorvalue = null;
            //获取上周数据
            String preStartTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMD(starttime), 7));
            String preEndTime = DataFormatUtil.getDateYMD(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMD(endtime), 7)) + " 23:59:59";
            paramMap.put("starttime", preStartTime);
            paramMap.put("endtime", preEndTime);
            List<Document> preDocuments = onlineService.getMonitorDataByParamMap(paramMap);

            Map<String, List<Double>> mnAndValues = new HashMap<>();
            List<Double> values;
            for (Document document : preDocuments) {
                mn = document.getString("DataGatherCode");
                pollutantList = (List<Map<String, Object>>) document.get("DayDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);
                }
            }
            //获取上周各个点位平均值
            Map<String, Double> mnAndAvgPre = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgPre.put(mnKey, monitorvalue);
                }
            }
            //当前周数据
            mnAndValues.clear();
            Map<String, Map<String, Object>> mnAndTimeAndValue = new HashMap<>();
            Map<String, Object> itemAndValue;
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                pollutantList = (List<Map<String, Object>>) document.get("DayDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantCode.equals(map.get("PollutantCode"))) {
                        monitorvalue = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (monitorvalue != null) {
                    if (mnAndValues.containsKey(mn)) {
                        values = mnAndValues.get(mn);
                    } else {
                        values = new ArrayList<>();
                    }
                    values.add(monitorvalue);
                    mnAndValues.put(mn, values);

                    if (mnAndTimeAndValue.containsKey(mn)) {
                        itemAndValue = mnAndTimeAndValue.get(mn);
                    } else {
                        itemAndValue = new HashMap<>();
                    }
                    itemAndValue.put(monitortime, monitorvalue);
                    mnAndTimeAndValue.put(mn, itemAndValue);
                }
            }
            //获取当周各个点位平均值
            Map<String, Double> mnAndAvgCurr = new HashMap<>();
            if (mnAndValues.size() > 0) {
                for (String mnKey : mnAndValues.keySet()) {
                    monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                    mnAndAvgCurr.put(mnKey, monitorvalue);
                }
            }
            List<String> ymds = DataFormatUtil.getYMDBetween(starttime, endtime);
            ymds.add(endtime);
            List<String> notInKey = new ArrayList<>();
            for (String mnKey : MNAndPoints.keySet()) {
                itemAndValue = mnAndTimeAndValue.get(mnKey);
                if (itemAndValue != null) {
                    Map<String, Object> reMap = new HashMap<>();
                    reMap.put("mn", mnKey);
                    reMap.put("OU", MNAndPoints.get(mnKey));
                    reMap.put("sunday", itemAndValue.get(ymds.get(0)) != null ? itemAndValue.get(ymds.get(0)) : "-");
                    reMap.put("monday", itemAndValue.get(ymds.get(1)) != null ? itemAndValue.get(ymds.get(1)) : "-");
                    reMap.put("tuesday", itemAndValue.get(ymds.get(2)) != null ? itemAndValue.get(ymds.get(2)) : "-");
                    reMap.put("wednesday", itemAndValue.get(ymds.get(3)) != null ? itemAndValue.get(ymds.get(3)) : "-");
                    reMap.put("thursday", itemAndValue.get(ymds.get(4)) != null ? itemAndValue.get(ymds.get(4)) : "-");
                    reMap.put("friday", itemAndValue.get(ymds.get(5)) != null ? itemAndValue.get(ymds.get(5)) : "-");
                    reMap.put("saturday", itemAndValue.get(ymds.get(6)) != null ? itemAndValue.get(ymds.get(6)) : "-");
                    reMap.put("weekavg", mnAndAvgCurr.get(mnKey));
                    reMap.put("preweek", getHBRate(mnAndAvgPre.get(mnKey), mnAndAvgCurr.get(mnKey)));
                    resultList.add(reMap);
                } else {
                    notInKey.add(mnKey);
                }

            }
            //根据周平均排序
            Collections.sort(resultList, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Double s1 = (Double) o1.get("weekavg");
                    Double s2 = (Double) o2.get("weekavg");
                    if (s1 > s2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
            //设置排名变化
            Map<String, Double> mnAndAvgDoublePre = new LinkedHashMap<>();
            for (String mnKey : mnAndAvgPre.keySet()) {
                mnAndAvgDoublePre.put(mnKey, mnAndAvgPre.get(mnKey));
            }

            Map<String, Double> mnAndAvgPreSort = DataFormatUtil.sortMapByValue(mnAndAvgDoublePre, true);
            int num = 0;
            Map<String, Integer> mnAndPreIndex = new HashMap<>();
            for (String mnKey : mnAndAvgPreSort.keySet()) {
                num++;
                mnAndPreIndex.put(mnKey, num);
            }

            num = 0;
            for (Map<String, Object> map : resultList) {
                num++;
                mn = map.get("mn").toString();
                map.put("weeklist", num);

                map.put("weekchange", getChangeRate(mnAndPreIndex.get(mn), num));
            }
            //添加无数据的点位
            for (String mnKey : notInKey) {
                Map<String, Object> reMap = new HashMap<>();
                reMap.put("mn", mnKey);
                reMap.put("OU", MNAndPoints.get(mnKey));
                reMap.put("sunday", "-");
                reMap.put("monday", "-");
                reMap.put("tuesday", "-");
                reMap.put("wednesday", "-");
                reMap.put("thursday", "-");
                reMap.put("friday", "-");
                reMap.put("saturday", "-");
                reMap.put("weekavg", "-");
                reMap.put("preweek", "-");
                reMap.put("weeklist", "-");
                reMap.put("weekchange", "-");
                resultList.add(reMap);
            }

            //初始化报表属性数据
            if (reportAttributeDataList != null) {
                boolean isUpper = false;

                List<String> pointNameList = new ArrayList<>();
                //浓度较高点位名称
                String highPointNames = "";
                //超标点名称
                String overPointNames = "";
                String overTimeString = "";


                List<String> highDgimn = new ArrayList<>();
                //环境恶臭点位信息
                Map<String, Object> paramMapTemp = new HashMap<>();
                paramMapTemp.put("outputids", new ArrayList<>());
                paramMapTemp.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                List<Map<String, Object>> pointDataList = onlineService.getMonitorPointDataByParam(paramMapTemp);
                List<String> MGPointList = new ArrayList<>();
                List<String> outMN = new ArrayList<>();
                String mnCommon;
                String pointname;
                for (Map<String, Object> pointData : pointDataList) {
                    pointname = pointData.get("monitorpointname").toString();
                    mnCommon = pointData.get("dgimn").toString();
                    if (pointData.get("MonitorPointCategory") != null && "1".equals(pointData.get("MonitorPointCategory").toString())) {
                        MGPointList.add(pointname);
                    }
                    outMN.add(mnCommon);
                }
                for (Map<String, Object> map : reportAttributeDataList) {
                    //点位变化分析
                    if ("pointChangeSituation".equals(map.get("reportattributecode"))) {
                        String pointChangeSituation = "由图上图可看出，各企业厂界监测点位恶臭浓度变化不大。";

                        String highPointString = "";

                        //获取浓度较高的时间及点位信息（时间：点位=1：n）
                        // List<Map<String, Object>> pointChangeList = getTimeToMonitorValues(chartDataList, "week", outMN, false);
                        Map<String, Object> timeListAndPointData = getTimeListAndPointData(chartDataList, "week", outMN, true);
                        if (timeListAndPointData.size() > 0) {
                            isUpper = true;
                            List<Integer> numList = new ArrayList<>();
                            String ymdh;
                            Integer hour;
                            String dgimn;
                            Map<String, Integer> pointAndNum = new HashMap<>();
                            List<Map<String, Object>> upperpointdata = (List<Map<String, Object>>) timeListAndPointData.get("upperpointdata");
                            for (Map<String, Object> upperPoint : upperpointdata) {
                                pointname = upperPoint.get("pointname").toString();
                                dgimn = upperPoint.get("dgimn").toString();
                                if (pointAndNum.containsKey(pointname)) {
                                    pointAndNum.put(pointname, pointAndNum.get(pointname) + 1);
                                } else {
                                    pointAndNum.put(pointname, 1);
                                }
                                if (!highDgimn.contains(dgimn)) {
                                    highDgimn.add(dgimn);
                                }

                            }
                            List<String> timeList = (List<String>) timeListAndPointData.get("timeList");
                            for (String time : timeList) {
                                ymdh = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH", "H");
                                hour = Integer.parseInt(ymdh);
                                if (!numList.contains(hour)) {
                                    numList.add(hour);
                                }

                            }
                            Collections.sort(numList);
                            ymdh = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                            if (StringUtils.isNotBlank(ymdh)) {
                                ymdh = ymdh.substring(0, ymdh.length() - 1);
                            }
                            Map<String, Integer> sortMap = DataFormatUtil.sortMapByValue(pointAndNum, true);
                            List<String> highPointNameList = new ArrayList<>();
                            List<String> CXNameList = new ArrayList<>();
                            for (String name : sortMap.keySet()) {
                                CXNameList.add(name);
                                highPointNameList.add(name);
                            }
                            highPointNameList = highPointNameList.stream().distinct().collect(Collectors.toList());

                            String CXNames = DataFormatUtil.FormatListToString(CXNameList, "、");
                            pointNameList.addAll(highPointNameList);

                            highPointNames = DataFormatUtil.FormatListToString(highPointNameList, "、");
                            highPointString = "各企业厂界监测点位恶臭浓度在" + ymdh + "的恶臭浓度相对比较高";
                        }

                        //获取超标点位，及时间段

                        List<Map<String, Object>> chartdata;
                        List<String> overTimeList;
                        List<String> timeList = new ArrayList<>();
                        List<String> overPointList = new ArrayList<>();
                        for (Map<String, Object> dataMap : chartDataList) {
                            chartdata = (List<Map<String, Object>>) dataMap.get("chartdata");
                            for (Map<String, Object> data : chartdata) {
                                if (data.get("isoverstandard") != null &&
                                        Boolean.parseBoolean(data.get("isoverstandard").toString())) {
                                    highDgimn.add(data.get("dgimn").toString());
                                    overPointList.add(data.get("monitorpointname").toString());
                                    overTimeList = data.get("overtimelist") != null ? (List<String>) data.get("overtimelist") : null;
                                    if (overTimeList != null) {
                                        timeList.addAll(overTimeList);
                                    }

                                }
                            }
                        }
                        String overString = "";
                        if (timeList.size() > 0) {
                            isUpper = true;
                            overPointList = overPointList.stream().distinct().collect(Collectors.toList());
                            pointNameList.addAll(overPointList);
                            overPointNames = DataFormatUtil.FormatListToString(overPointList, "、");
                            String ymdh;
                            int hour;
                            List<Integer> numList = new ArrayList<>();
                            for (String time : timeList) {
                                ymdh = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH", "H");
                                hour = Integer.parseInt(ymdh);
                                if (!numList.contains(hour)) {
                                    numList.add(hour);
                                }
                            }
                            Collections.sort(numList);
                            overTimeString = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                            if (StringUtils.isNotBlank(overTimeString)) {
                                overTimeString = overTimeString.substring(0, overTimeString.length() - 1);
                            }
                            overString = overPointNames + overTimeString + "有超标现象";
                        }
                        if (isUpper) {
                            String mark = "";
                            if (StringUtils.isNotBlank(highPointString) && StringUtils.isNotBlank(overString)) {
                                mark = "，";
                            }
                            pointChangeSituation = "由图1可看出，" + highPointString + mark + overString + "。";
                        }
                        map.put("reportattributevalue", pointChangeSituation);

                    }
                    //管控建议
                    if ("controlSuggestions".equals(map.get("reportattributecode"))) {
                        String controlSuggestions = "无";
                        if (isUpper) {
                            String BQ = "";

                            highDgimn = highDgimn.stream().distinct().collect(Collectors.toList());
                            List<Map<String, Object>> pointChangeList = getMonitorToTimeValues(chartDataList, highDgimn, "week");
                            String upperName = "";
                            if (pointChangeList.size() > 0) {
                                List<String> upperPointNameList = new ArrayList<>();
                                String pointName;
                                String ymdh;
                                Integer hour;
                                List<Integer> numList = new ArrayList<>();
                                List<Map<String, Object>> timeData;
                                for (Map<String, Object> pointChange : pointChangeList) {
                                    pointName = pointChange.get("pointname").toString();
                                    upperPointNameList.add(pointName);
                                    timeData = (List<Map<String, Object>>) pointChange.get("uppertimedata");
                                    for (Map<String, Object> uppertime : timeData) {
                                        ymdh = DataFormatUtil.FormatDateOneToOther(uppertime.get("time").toString(), "yyyy-MM-dd HH", "H");
                                        hour = Integer.parseInt(ymdh);
                                        if (!numList.contains(hour)) {
                                            numList.add(hour);
                                        }
                                    }
                                }
                                upperName = DataFormatUtil.FormatListToString(upperPointNameList, "、");
                                Collections.sort(numList);
                                ymdh = DataFormatUtil.mergeContinueNum(numList, 0, "、", "时");
                                if (StringUtils.isNotBlank(ymdh)) {
                                    ymdh = ymdh.substring(0, ymdh.length() - 1);
                                }
                                BQ = "并且" + upperName + "在" + ymdh + "浓度均有突升，";
                            }

                            List<Map<String, Object>> resultDataList = new ArrayList<>();
                                    //相似度去掉1115 onlineService.getEntStinkRelationDataGroupByWeekDateByParamMap(starttime, endtime, pollutantCode, highDgimn);
                            String qz = "";
                            if (resultDataList.size() > 0) {
                                String stinkPoint;
                                String entStinkPoint;
                                List<String> entStinkPoints = new ArrayList<>();
                                List<Map<String, Object>> similaritydata;
                                List<Map<String, Object>> QZDataList;
                                String monitorTime;

                                for (Map<String, Object> resultMap : resultDataList) {
                                    QZDataList = (List<Map<String, Object>>) resultMap.get("datalist");
                                    if (QZDataList.size() > 0) {
                                        monitorTime = DataFormatUtil.FormatDateOneToOther(resultMap.get("monitortime").toString(), "yyyy-MM-dd", "d日");
                                        for (Map<String, Object> qzData : QZDataList) {
                                            similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                            if (similaritydata.size() > 0) {
                                                entStinkPoints.clear();
                                                stinkPoint = qzData.get("pointname").toString();
                                                similaritydata = (List<Map<String, Object>>) qzData.get("similaritydata");
                                                for (Map<String, Object> similarity : similaritydata) {
                                                    entStinkPoint = similarity.get("pointname").toString();
                                                    if (pointNameList.contains(entStinkPoint)) {
                                                        entStinkPoints.add(entStinkPoint);
                                                    }
                                                }
                                                if (entStinkPoints.size() > 0) {
                                                    entStinkPoint = DataFormatUtil.FormatListToString(entStinkPoints, "、");
                                                    qz = qz + monitorTime + entStinkPoint + "与" + stinkPoint + "敏感点浓度变化趋势有一定的相似，";
                                                }
                                            }
                                        }
                                    }
                                }
                                if (StringUtils.isNotBlank(qz)) {
                                    qz = "其中，" + qz;
                                }
                            }
                            String YQ = "";
                            if (StringUtils.isNotBlank(upperName)) {
                                // YQ = "，尤其是" + upperName;
                            }
                            controlSuggestions = "根据图1各点位小时恶臭浓度变化趋势分析，";
                            if (StringUtils.isNotBlank(overPointNames)) {
                                controlSuggestions += overPointNames + overTimeString + "有超标，";
                            }
                            if (StringUtils.isNotBlank(highPointNames)) {
                                controlSuggestions += highPointNames + "恶臭浓度相对比较高，";
                            }
                            pointNameList = pointNameList.stream().distinct().collect(Collectors.toList());
                            String pointNames = DataFormatUtil.FormatListToString(pointNameList, "、");
                            controlSuggestions += BQ
                                    + qz + "根据当天气象条件分析，建议加大对"
                                    + pointNames + "监督巡查力度"
                                    + "。";


                        }
                        map.put("reportattributevalue", controlSuggestions);
                    }
                    //恶臭质量状况
                    if ("stinkQualityStatus".equals(map.get("reportattributecode"))) {
                        if (DataFormatUtil.isObjectNull(map.get("reportattributevalue"))) {
                            String MGPoints = "";
                            MGPoints = DataFormatUtil.FormatListToString(MGPointList, "、");
                            //厂界恶臭点位信息
                            Map<String, String> mnAndEntStinkPoints = onlineService.getMNAndMonitorPoint(new ArrayList<>(),
                                    CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                            String stinkQualityStatusPartOne =
                                    "以解决工业园区恶臭扰民事件为目的，" +
                                            "提升园区环境精细化监管水平，" +
                                            "在园区企业厂界设置" + mnAndEntStinkPoints.size() + "个监测点位，" +
                                            "并根据历史投诉事件和气象条件分析在园区及其周边设置" + MGPointList.size() + "个敏感监测点位， " +
                                            "来加强恶臭浓度监测；" +
                                            "本周所有监测点位的恶臭浓度汇总情况如下表1所示，" +
                                            "其中" + MGPoints + "为环境敏感点，其他监测点位均布设在企业的厂界位置。";

                            List<String> topThreePoints = new ArrayList<>();
                            num = 0;
                            for (Map<String, Object> resultMap : resultList) {
                                if (!"-".equals(resultMap.get("weekavg")) && num < 3) {
                                    topThreePoints.add(resultMap.get("OU").toString());
                                }
                                num++;
                            }
                            String topThree = DataFormatUtil.FormatListToString(topThreePoints, "、");

                            //获取上周数据
                            preStartTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMD(starttime), 21));
                            preEndTime = DataFormatUtil.getDateYMD(DataFormatUtil.getPreDate(DataFormatUtil.getDateYMD(endtime), 7)) + " 23:59:59";
                            paramMap.put("starttime", preStartTime);
                            paramMap.put("endtime", preEndTime);
                            Date monitorDate;
                            String weekStartTime;
                            Map<String, List<Document>> weekStartTimeAndData = new HashMap<>();
                            List<Document> documentData;
                            List<Document> pre21Documents = onlineService.getMonitorDataByParamMap(paramMap);
                            for (Document document : pre21Documents) {
                                monitorDate = document.getDate("MonitorTime");
                                weekStartTime = DataFormatUtil.getDateYMD(DataFormatUtil.getFirstDayOfWeek(monitorDate));
                                if (weekStartTimeAndData.containsKey(weekStartTime)) {
                                    documentData = weekStartTimeAndData.get(weekStartTime);
                                } else {
                                    documentData = new ArrayList<>();
                                }
                                documentData.add(document);
                                weekStartTimeAndData.put(weekStartTime, documentData);
                            }
                            String QZPoint = "";
                            if (weekStartTimeAndData.size() > 0) {
                                Map<String, Integer> pointAndNum = new HashMap<>();

                                mnAndAvgPre.clear();

                                mnAndValues.clear();

                                for (String weekKey : weekStartTimeAndData.keySet()) {
                                    documentData = weekStartTimeAndData.get(weekKey);
                                    for (Document document : documentData) {
                                        pollutantList = (List<Map<String, Object>>) document.get("DayDataList");
                                        for (Map<String, Object> pollutant : pollutantList) {
                                            if (pollutantCode.equals(pollutant.get("PollutantCode"))) {
                                                monitorvalue = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                                break;
                                            }
                                        }
                                        if (monitorvalue != null) {
                                            mn = document.getString("DataGatherCode");
                                            if (mnAndValues.containsKey(mn)) {
                                                values = mnAndValues.get(mn);
                                            } else {
                                                values = new ArrayList<>();
                                            }
                                            values.add(monitorvalue);
                                            mnAndValues.put(mn, values);
                                        }
                                    }
                                    if (mnAndValues.size() > 0) {
                                        for (String mnKey : mnAndValues.keySet()) {
                                            monitorvalue = Double.parseDouble(DataFormatUtil.getListAvgValue(mnAndValues.get(mnKey)));
                                            mnAndAvgPre.put(mnKey, monitorvalue);
                                        }
                                        //排序
                                        mnAndAvgPreSort = DataFormatUtil.sortMapByValue(mnAndAvgPre, true);
                                        num = 0;
                                        String name;
                                        for (String mnKey : mnAndAvgPreSort.keySet()) {
                                            name = MNAndPoints.get(mnKey);
                                            if (num < 3 && topThreePoints.contains(name)) {
                                                if (pointAndNum.containsKey(name)) {
                                                    pointAndNum.put(name, pointAndNum.get(name) + 1);
                                                } else {
                                                    pointAndNum.put(name, 1);
                                                }
                                            }

                                            num++;
                                            if (num >= 3) {
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (pointAndNum.size() > 0) {
                                    List<String> QZPoints = new ArrayList<>();
                                    for (String point : pointAndNum.keySet()) {
                                        if (pointAndNum.get(point) >= 2) {
                                            QZPoints.add(point);
                                        }
                                    }
                                    QZPoint = DataFormatUtil.FormatListToString(QZPoints, "、");
                                }
                            }

                            String QZText = "";
                            if (StringUtils.isNotBlank(QZPoint)) {
                                QZText = "，其中" + QZPoint + "恶臭周平均浓度连续四周排名位于前三位";
                            }
                            String changeText = getHBChangeText(mnAndAvgPre, mnAndAvgCurr);
                            String stinkQualityStatusPartTwo = "由表1中各监测点位恶臭的日浓度分析可知，" + topThree + "的恶臭浓度相对较高" + QZText + "。"
                                    + "由周环比情况分析可知，本周工业园区的恶臭质量变化" + changeText + " 。";
                            map.put("reportattributevalue", stinkQualityStatusPartOne + "\r\n" + stinkQualityStatusPartTwo);
                        }
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/9/23 0023 下午 3:43
     * @Description: 获取周环比变化描述
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getHBChangeText(Map<String, Double> mnAndAvgPre, Map<String, Double> mnAndAvgCurr) {

        String HBData = "";
        if (mnAndAvgPre.size() > 0 && mnAndAvgCurr.size() > 0) {
            Double currAvg = 0d;
            Double preAvg = 0d;

            for (String mnKey : mnAndAvgCurr.keySet()) {
                currAvg += mnAndAvgCurr.get(mnKey);
            }
            currAvg = currAvg / mnAndAvgCurr.size();
            for (String mnKey : mnAndAvgPre.keySet()) {
                preAvg += mnAndAvgPre.get(mnKey);
            }
            preAvg = preAvg / mnAndAvgPre.size();
            Double sumChange = 100 * (currAvg - preAvg) / preAvg;
            if (sumChange <= 10 && sumChange >= -10) {
                HBData = "趋于稳定";
            } else if (sumChange > 10 && sumChange <= 20) {
                HBData = "有所上升";
            } else if (sumChange > 20) {
                HBData = "明显上升";
            } else if (sumChange < -10 && sumChange >= -20) {
                HBData = "有所下滑";
            } else if (sumChange < -20) {
                HBData = "明显下滑";
            }
        }
        return HBData;
    }

    /**
     * @author: lip
     * @date: 2019/8/21 0021 上午 11:28
     * @Description: 计算排名变化情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getChangeRate(Integer pre, Integer curr) {
        String weekchange = "-";
        if (pre != null && curr != null) {
            int change = curr - pre;
            if (change < 0) {
                weekchange = "↑" + (-change);
            } else if (change == 0) {
                weekchange = "持平";
            } else {
                weekchange = "↓" + change;
            }
        }
        return weekchange;

    }

    /**
     * @author: lip
     * @date: 2019/8/21 0021 上午 11:28
     * @Description: 计算变化率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getHBRate(Object pre, Object curr) {
        if (pre != null && curr != null) {
            Double preDouble = Double.parseDouble(pre.toString());
            Double currDouble = Double.parseDouble(curr.toString());
            return DataFormatUtil.SaveOneAndSubZero(100 * (currDouble - preDouble) / preDouble);
        } else {
            return "-";
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/21 0021 下午 1:43
     * @Description: 自定义查询条件获取恶臭（厂界+环境）图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStinkManyPointDataByParams(Integer timetype, String starttime, String endtime) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            //获取所有恶臭点位（厂界+环境）
            List<Integer> stinkPoints = Arrays.asList(
                    CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode(),
                    CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
            Map<String, String> MNAndPoints = new LinkedHashMap<>();

            for (Integer type : stinkPoints) {
                MNAndPoints.putAll(onlineService.getMNAndMonitorPoint(new ArrayList<>(), type));
            }

            String pollutantCode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
            List<String> mns = new ArrayList<>(MNAndPoints.keySet());
            String collection = MongoDataUtils.getCollectionByDataMark(timetype);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(timetype, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(timetype, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pollutantcodes", Arrays.asList(pollutantCode));
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("sort", "asc");
            List<Document> monitorData = onlineService.getMonitorDataByParamMap(paramMap);
            if (monitorData.size() > 0) {
                dataList = MongoDataUtils.setManyMNChartData(mns, monitorData, collection, pollutantCode);
                for (Map<String, Object> map : dataList) {
                    map.put("monitorpointname", MNAndPoints.get(map.get("dgimn")));
                }
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zzc
     * @date: 2019/9/26 14:10
     * @Description: 专报单个时间对应多个监测点
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getSpecialReportTimeToMonitorValues(List<Map<String, Object>> chartdatalist, List<String> mns, boolean isContains, String startTime, String endTime) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, LinkedHashMap<String, Float>> mapMaps = new LinkedHashMap<>();
        Map<String, Object> mnName = new HashMap<>();
        //获取时间
        List<String> times = DataFormatUtil.separateTimeForHour(startTime + ":00:00", endTime + ":00:00", 1);
        times.add(endTime);
        for (String time : times) {
            //每个监测点的值
            LinkedHashMap<String, Float> mapInfo = new LinkedHashMap<>();
            for (Map<String, Object> chartdatum : chartdatalist) {
                //监测点名称
                String dgimn = chartdatum.get("dgimn").toString();
                if (mns.contains(dgimn) == isContains) {
                    String monitorpointname = chartdatum.get("monitorpointname").toString();
                    mnName.put(monitorpointname, dgimn);
                    //监测值
                    List<Map<String, Object>> monitordatalist = chartdatum.get("monitordatalist") != null ? (List<Map<String, Object>>) chartdatum.get("monitordatalist") : new ArrayList<>();
                    for (Map<String, Object> objectMap : monitordatalist) {
                        if (objectMap.get("monitortime").equals(time)) {
                            if (objectMap.get("monitorvalue") != null) {
                                Object monitorvalue = objectMap.get("monitorvalue");
                                mapInfo.put(monitorpointname, Float.parseFloat(monitorvalue.toString()));
                            }
                            break;
                        }
                    }
                }
            }
            mapMaps.put(time, mapInfo);
        }
        for (Map.Entry<String, LinkedHashMap<String, Float>> entry : mapMaps.entrySet()) {
            String key = entry.getKey();
            LinkedHashMap<String, Float> value = entry.getValue();
            Map<String, Float> outLier = AlarmRemindUtil.findOutLiers(value);
            if (outLier != null && outLier.size() > 0) {
                Map<String, Object> mapInfo = new HashMap<>();
                List<Map<String, Object>> upperpointdata = new ArrayList<>();
                for (Map.Entry<String, Float> entryInfo : outLier.entrySet()) {
                    Map<String, Object> map1 = new HashMap<>();
                    String keyInfo = entryInfo.getKey();
                    Object valueInfo = entryInfo.getValue();
                    map1.put("pointname", keyInfo);
                    map1.put("dgimn", mnName.get(keyInfo));
                    map1.put("monitorvalue", valueInfo.toString());
                    upperpointdata.add(map1);
                }
                mapInfo.put("time", key);
                mapInfo.put("upperpointdata", upperpointdata);
                resultList.add(mapInfo);
            }
        }
        return resultList;
    }

    /**
     * @author: zzc
     * @date: 2019/9/24 11:27
     * @Description: 单个监测点对应多个时间
     * @param: [date, param]
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getSpecialReportMonitorToTimeValues(List<Map<String, Object>> chartdatalist, List<String> mns, String startTime, String endTime) {
        List<Map<String, Object>> timeToMonitorValues = getSpecialReportTimeToMonitorValues(chartdatalist, mns, true, startTime, endTime);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, LinkedHashMap<String, Float>> mapMaps = new LinkedHashMap<>();
        for (Map<String, Object> chartdatum : chartdatalist) {
            //监测点名称
            String dgimn = chartdatum.get("dgimn").toString();
            if (mns.contains(dgimn)) {
                String monitorpointname = chartdatum.get("monitorpointname").toString();
                List<Map<String, Object>> monitordatalist = chartdatum.get("monitordatalist") != null ? (List<Map<String, Object>>) chartdatum.get("monitordatalist") : new ArrayList<>();
                LinkedHashMap<String, Float> stringFloatLinkedHashMap;
                if (mapMaps.containsKey(monitorpointname)) {
                    stringFloatLinkedHashMap = mapMaps.get(monitorpointname);
                } else {
                    stringFloatLinkedHashMap = new LinkedHashMap<>();
                }
                for (Map<String, Object> objectMap : monitordatalist) {
                    Object monitorvalue = objectMap.get("monitorvalue");
                    if (monitorvalue != null) {
                        float value = Float.parseFloat(monitorvalue.toString());
                        String monitortime = objectMap.get("monitortime").toString();
                        stringFloatLinkedHashMap.put(monitortime, value);
                    }
                }
                mapMaps.put(monitorpointname, stringFloatLinkedHashMap);
            }
        }
        for (Map.Entry<String, LinkedHashMap<String, Float>> entry : mapMaps.entrySet()) {
            String key = entry.getKey();
            LinkedHashMap<String, Float> value = entry.getValue();
            Map<String, Float> outLier = AlarmRemindUtil.findOutLiers(value);
            if (outLier != null && outLier.size() > 0) {
                Map<String, Object> mapInfo = new HashMap<>();
                List<Map<String, Object>> upperpointdata = new ArrayList<>();
                for (Map.Entry<String, Float> entryInfo : outLier.entrySet()) {
                    Map<String, Object> map1 = new HashMap<>();
                    String keyInfo = entryInfo.getKey();
                    Object valueInfo = entryInfo.getValue();
                    map1.put("time", keyInfo);
                    map1.put("monitorvalue", valueInfo.toString());
                    upperpointdata.add(map1);
                }
                mapInfo.put("pointname", key);
                mapInfo.put("uppertimedata", upperpointdata);
                resultList.add(mapInfo);
            }
        }
        if (timeToMonitorValues.size() > 0) {
            for (Map<String, Object> monitorValue : timeToMonitorValues) {
                String time = monitorValue.get("time").toString();
                List<Map<String, Object>> upperpointdata = (List<Map<String, Object>>) monitorValue.get("upperpointdata");
                for (Map<String, Object> upperpointdatum : upperpointdata) {
                    String pointname = upperpointdatum.get("pointname").toString();
                    boolean isContains = false;
                    for (Map<String, Object> map : resultList) {
                        String pointnameInfo = map.get("pointname").toString();
                        if (pointnameInfo.equals(pointname)) {
                            isContains = true;
                            List<Map<String, Object>> uppertimedataInfo = (List<Map<String, Object>>) map.get("uppertimedata");
                            boolean isContains2 = false;
                            for (Map<String, Object> objectMap : uppertimedataInfo) {
                                String time1 = objectMap.get("time").toString();
                                if (time.equals(time1)) {
                                    isContains2 = true;
                                    break;
                                }
                            }
                            //如果不包含
                            if (!isContains2) {
                                Map<String, Object> objectMapInfo = new HashMap<>();
                                objectMapInfo.put("time", time);
                                objectMapInfo.put("monitorvalue", upperpointdatum.get("monitorvalue"));
                                uppertimedataInfo.add(objectMapInfo);
                            }
                            map.replace("uppertimedata", uppertimedataInfo);
                            break;
                        }
                    }
                    //如果不包含
                    if (!isContains) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pointname", pointname);
                        List<Map<String, Object>> uppertimedata = new ArrayList<>();
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("time", time);
                        map2.put("monitorvalue", upperpointdatum.get("monitorvalue"));
                        uppertimedata.add(map2);
                        map.put("uppertimedata", uppertimedata);
                        resultList.add(map);
                    }

                }
            }
        }
        return resultList.stream().sorted(Comparator.comparingInt(m -> ((List) ((Map) m).get("uppertimedata")).size()).reversed()).collect(Collectors.toList());
    }


    /**
     * @author: zzc
     * @date: 2019/9/24 11:27
     * @Description: 单个时间对应多个监测点
     * @param: [date, param]
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTimeToMonitorValues(List<Map<String, Object>> chartdatalist, String flag, List<String> mns, boolean isContains) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, LinkedHashMap<String, Float>> mapMaps = new LinkedHashMap<>();
        Map<String, Object> mnName = new HashMap<>();
        for (Map<String, Object> map : chartdatalist) {
            int num = 24;
            int start = 0;
            String monitortime = map.get("monitortime").toString();
            if (flag.equals("month")) {
                monitortime = map.get("monitortime").toString().substring(0, monitortime.length() - 3);
                start = 1;
                num = DataFormatUtil.getYearMotDays(monitortime);
            }
            //每个时间内每个监测点的值
            List<Map<String, Object>> chartdata = map.get("chartdata") != null ? (List<Map<String, Object>>) map.get("chartdata") : new ArrayList<>();
            for (int i = start; i < num; i++) {
                String hour = i >= 10 ? String.valueOf(i) : "0" + i;
                String time;
                if (flag.equals("month")) {
                    time = monitortime + "-" + hour;
                } else {
                    time = monitortime + " " + hour;
                }
                //每个监测点的值
                LinkedHashMap<String, Float> mapInfo = new LinkedHashMap<>();
                for (Map<String, Object> chartdatum : chartdata) {
                    //监测点名称
                    String dgimn = chartdatum.get("dgimn").toString();
                    if (mns.contains(dgimn) == isContains) {
                        String monitorpointname = chartdatum.get("monitorpointname").toString();
                        mnName.put(monitorpointname, dgimn);
                        //监测值
                        List<Map<String, Object>> monitordatalist = chartdatum.get("monitordatalist") != null ? (List<Map<String, Object>>) chartdatum.get("monitordatalist") : new ArrayList<>();
                        for (Map<String, Object> objectMap : monitordatalist) {
                            if (objectMap.get("monitortime").equals(time)) {
                                if (objectMap.get("monitorvalue") != null) {
                                    Object monitorvalue = objectMap.get("monitorvalue");
                                    mapInfo.put(monitorpointname, Float.parseFloat(monitorvalue.toString()));
                                }
                                break;
                            }
                        }
                    }
                }
                mapMaps.put(time, mapInfo);
            }
        }
        for (Map.Entry<String, LinkedHashMap<String, Float>> entry : mapMaps.entrySet()) {
            String key = entry.getKey();
            LinkedHashMap<String, Float> value = entry.getValue();
            Map<String, Float> outLier = AlarmRemindUtil.findOutLiers(value);
            if (outLier != null && outLier.size() > 0) {
                Map<String, Object> mapInfo = new HashMap<>();
                List<Map<String, Object>> upperpointdata = new ArrayList<>();
                for (Map.Entry<String, Float> entryInfo : outLier.entrySet()) {
                    Map<String, Object> map1 = new HashMap<>();
                    String keyInfo = entryInfo.getKey();
                    Object valueInfo = entryInfo.getValue();
                    map1.put("pointname", keyInfo);
                    map1.put("dgimn", mnName.get(keyInfo));
                    map1.put("monitorvalue", valueInfo.toString());
                    upperpointdata.add(map1);
                }
                mapInfo.put("time", key);
                mapInfo.put("upperpointdata", upperpointdata);
                resultList.add(mapInfo);
            }
        }
        return resultList;
    }

    private Map<String, Object> getTimeListAndPointData(List<Map<String, Object>> chartdatalist, String flag, List<String> mns, boolean isContains) {
        Map<String, LinkedHashMap<String, Float>> mapMaps = new LinkedHashMap<>();
        Map<String, Object> mnName = new HashMap<>();
        for (Map<String, Object> map : chartdatalist) {
            int num = 24;
            int start = 0;
            String monitortime = map.get("monitortime").toString();
            if (flag.equals("month")) {
                monitortime = map.get("monitortime").toString().substring(0, monitortime.length() - 3);
                start = 1;
                num = DataFormatUtil.getYearMotDays(monitortime);
            }
            //每个时间内每个监测点的值
            List<Map<String, Object>> chartdata = map.get("chartdata") != null ? (List<Map<String, Object>>) map.get("chartdata") : new ArrayList<>();
            for (int i = start; i < num; i++) {
                String hour = i >= 10 ? String.valueOf(i) : "0" + i;
                String time;
                if (flag.equals("month")) {
                    time = monitortime + "-" + hour;
                } else {
                    time = monitortime + " " + hour;
                }
                //每个监测点的值
                LinkedHashMap<String, Float> mapInfo = new LinkedHashMap<>();
                for (Map<String, Object> chartdatum : chartdata) {
                    //监测点名称
                    String dgimn = chartdatum.get("dgimn").toString();
                    if (mns.contains(dgimn) == isContains) {
                        String monitorpointname = chartdatum.get("monitorpointname").toString();
                        mnName.put(monitorpointname, dgimn);
                        //监测值
                        List<Map<String, Object>> monitordatalist = chartdatum.get("monitordatalist") != null ? (List<Map<String, Object>>) chartdatum.get("monitordatalist") : new ArrayList<>();
                        for (Map<String, Object> objectMap : monitordatalist) {
                            if (objectMap.get("monitortime").equals(time)) {
                                if (objectMap.get("monitorvalue") != null) {
                                    Object monitorvalue = objectMap.get("monitorvalue");
                                    mapInfo.put(monitorpointname, Float.parseFloat(monitorvalue.toString()));
                                }
                                break;
                            }
                        }
                    }
                }
                mapMaps.put(time, mapInfo);
            }
        }
        LinkedHashMap<String, Float> pointTimeAndValue = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, Float>> entry : mapMaps.entrySet()) {
            String time = entry.getKey();
            LinkedHashMap<String, Float> value = entry.getValue();
            Map<String, Float> highMap = AlarmRemindUtil.findOutLiers(value);
            if (highMap != null && highMap.size() > 0) {
                for (String point : highMap.keySet()) {
                    pointTimeAndValue.put(point + "," + time, highMap.get(point));
                }
            } else {
                Map<String, Float> sortMap = DataFormatUtil.sortMapByValue(value, true);
                for (String point : sortMap.keySet()) {
                    pointTimeAndValue.put(point + "," + time, sortMap.get(point));
                    break;
                }
            }
        }
        Map<String, Float> highMap = AlarmRemindUtil.findOutLiers(pointTimeAndValue);
        Map<String, Object> mapInfo = new HashMap<>();
        if (highMap != null && highMap.size() > 0) {
            List<String> timeList = new ArrayList<>();
            String pointName;
            String time;
            List<Map<String, Object>> upperpointdata = new ArrayList<>();
            for (Map.Entry<String, Float> entryInfo : highMap.entrySet()) {
                Map<String, Object> map1 = new HashMap<>();
                String keyInfo = entryInfo.getKey();
                pointName = keyInfo.split(",")[0];
                time = keyInfo.split(",")[1];
                Object valueInfo = entryInfo.getValue();
                map1.put("pointname", pointName);
                map1.put("dgimn", mnName.get(pointName));
                map1.put("monitorvalue", valueInfo.toString());
                upperpointdata.add(map1);
                timeList.add(time);
            }
            mapInfo.put("timeList", timeList);
            mapInfo.put("upperpointdata", upperpointdata);
        }
        return mapInfo;
    }


    /**
     * @author: zzc
     * @date: 2019/9/24 11:27
     * @Description: 单个监测点对应多个时间
     * @param: [date, param]
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMonitorToTimeValues(List<Map<String, Object>> chartdatalist, List<String> mns, String flag) {
        List<Map<String, Object>> timeToMonitorValues = getTimeToMonitorValues(chartdatalist, flag, mns, true);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, LinkedHashMap<String, Float>> mapMaps = new LinkedHashMap<>();
        for (Map<String, Object> map : chartdatalist) {
            List<Map<String, Object>> chartdata = map.get("chartdata") != null ? (List<Map<String, Object>>) map.get("chartdata") : new ArrayList<>();
            for (Map<String, Object> chartdatum : chartdata) {
                //监测点名称
                String dgimn = chartdatum.get("dgimn").toString();
                if (mns.contains(dgimn)) {
                    String monitorpointname = chartdatum.get("monitorpointname").toString();
                    List<Map<String, Object>> monitordatalist = chartdatum.get("monitordatalist") != null ? (List<Map<String, Object>>) chartdatum.get("monitordatalist") : new ArrayList<>();
                    LinkedHashMap<String, Float> stringFloatLinkedHashMap;
                    if (mapMaps.containsKey(monitorpointname)) {
                        stringFloatLinkedHashMap = mapMaps.get(monitorpointname);
                    } else {
                        stringFloatLinkedHashMap = new LinkedHashMap<>();
                    }
                    for (Map<String, Object> objectMap : monitordatalist) {
                        Object monitorvalue = objectMap.get("monitorvalue");
                        if (monitorvalue != null) {
                            float value = Float.parseFloat(monitorvalue.toString());
                            String monitortime = objectMap.get("monitortime").toString();
                            stringFloatLinkedHashMap.put(monitortime, value);
                        }
                    }
                    mapMaps.put(monitorpointname, stringFloatLinkedHashMap);
                }
            }
        }
        for (Map.Entry<String, LinkedHashMap<String, Float>> entry : mapMaps.entrySet()) {
            String key = entry.getKey();
            LinkedHashMap<String, Float> value = entry.getValue();
            Map<String, Float> outLier = AlarmRemindUtil.findOutLiers(value);
            if (outLier != null && outLier.size() > 0) {
                Map<String, Object> mapInfo = new HashMap<>();
                List<Map<String, Object>> upperpointdata = new ArrayList<>();
                for (Map.Entry<String, Float> entryInfo : outLier.entrySet()) {
                    Map<String, Object> map1 = new HashMap<>();
                    String keyInfo = entryInfo.getKey();
                    Object valueInfo = entryInfo.getValue();
                    map1.put("time", keyInfo);
                    map1.put("monitorvalue", valueInfo.toString());
                    upperpointdata.add(map1);
                }
                mapInfo.put("pointname", key);
                mapInfo.put("uppertimedata", upperpointdata);
                resultList.add(mapInfo);
            }
        }
        if (timeToMonitorValues.size() > 0) {
            for (Map<String, Object> monitorValue : timeToMonitorValues) {
                String time = monitorValue.get("time").toString();
                List<Map<String, Object>> upperpointdata = (List<Map<String, Object>>) monitorValue.get("upperpointdata");
                for (Map<String, Object> upperpointdatum : upperpointdata) {
                    String pointname = upperpointdatum.get("pointname").toString();
                    boolean isContains = false;
                    for (Map<String, Object> map : resultList) {
                        String pointnameInfo = map.get("pointname").toString();
                        if (pointnameInfo.equals(pointname)) {
                            isContains = true;
                            List<Map<String, Object>> uppertimedataInfo = (List<Map<String, Object>>) map.get("uppertimedata");
                            boolean isContains2 = false;
                            for (Map<String, Object> objectMap : uppertimedataInfo) {
                                String time1 = objectMap.get("time").toString();
                                if (time.equals(time1)) {
                                    isContains2 = true;
                                    break;
                                }
                            }
                            //如果不包含
                            if (!isContains2) {
                                Map<String, Object> objectMapInfo = new HashMap<>();
                                objectMapInfo.put("time", time);
                                objectMapInfo.put("monitorvalue", upperpointdatum.get("monitorvalue"));
                                uppertimedataInfo.add(objectMapInfo);
                            }
                            map.replace("uppertimedata", uppertimedataInfo);
                            break;
                        }
                    }
                    //如果不包含
                    if (!isContains) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pointname", pointname);
                        List<Map<String, Object>> uppertimedata = new ArrayList<>();
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("time", time);
                        map2.put("monitorvalue", upperpointdatum.get("monitorvalue"));
                        uppertimedata.add(map2);
                        map.put("uppertimedata", uppertimedata);
                        resultList.add(map);
                    }

                }
            }
        }
        return resultList.stream().sorted(Comparator.comparingInt(m -> ((List) ((Map) m).get("uppertimedata")).size()).reversed()).collect(Collectors.toList());
    }

    /**
     * @author: xsm
     * @date: 2021/07/23 0023 下午 1:57
     * @Description: 自定义查询某个恶臭（环境+厂界）点位某日的报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneStinkPointOverDataByDayTime", method = RequestMethod.POST)
    public Object getOneStinkPointOverDataByDayTime(@RequestJson("daytime") String daytime,
                                                    @RequestJson("monitorpointname") String monitorpointname,
                                                    @RequestJson("monitorpointtype") String monitorpointtype,
                                                    @RequestJson("dgimn") String dgimn) throws Exception {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> standardmap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            String pollutantcode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
            param.put("monitortime",daytime);
            param.put("dgimn",dgimn);
            param.put("pollutantcode",pollutantcode);
            //获取某个点某个污染物某一天的超标时段
            List<Document> overtimes = onlineCountAlarmService.getOneStinkPointOverDataByParam(param);
            String starttime = daytime + " 00:00:00";
            String endtime = daytime + " 23:59:59";
            String dayDate = "";
            String cbqk_str = "";
            String syfx_str = "";
            int datatype = 1;
            if (overtimes!=null&&overtimes.size()>0){
                //starttime = DataFormatUtil.getDateYMDHMS(overtimes.get(0).getDate("FirstOverTime"));
                //endtime = DataFormatUtil.getDateYMDHMS(overtimes.get(overtimes.size()-1).getDate("LastOverTime"));
                for (Document doc : overtimes){
                    if (DataFormatUtil.getDateYMDHM(doc.getDate("FirstOverTime")).equals(DataFormatUtil.getDateYMDHM(doc.getDate("LastOverTime")))){
                        dayDate = dayDate + DataFormatUtil.FormatDateOneToOther(DataFormatUtil.getDateYMDHM(doc.getDate("FirstOverTime")), "yyyy-MM-dd HH:mm", "H时mm分")+ "、";
                    }else {
                        dayDate = dayDate + DataFormatUtil.FormatDateOneToOther(DataFormatUtil.getDateYMDHM(doc.getDate("FirstOverTime")), "yyyy-MM-dd HH:mm", "H时mm分")
                                + "-" + DataFormatUtil.FormatDateOneToOther(DataFormatUtil.getDateYMDHM(doc.getDate("LastOverTime")), "yyyy-MM-dd HH:mm", "H时mm分") + "、";
                    }
                    }
                    if (overtimes.get(0).get("DataType")!=null) {
                        if (overtimes.get(0).getString("DataType").equals("RealTimeData")) {
                            datatype = 1;
                        } else if (overtimes.get(0).getString("DataType").equals("MinuteData")) {
                            datatype = 2;
                        }
                    }
                if (!"".equals(dayDate)){
                    dayDate = dayDate.substring(0,dayDate.length()-1);
                    cbqk_str = DataFormatUtil.FormatDateOneToOther(daytime+" 00:00:00","yyyy-MM-dd HH:mm", "MM月dd日")+",在"+dayDate+"左右,"+monitorpointname+
                            "恶臭站点恶臭浓度出现超标现象，雪迪龙团队迅速启动溯源工作。";

                    syfx_str = "从下图可看出"+monitorpointname+"恶臭站点在"+dayDate+"时间段内出现了超标现象。";
                }
            //组装数据

            }else{
                cbqk_str = "暂无超标现象。";
                syfx_str = "暂无超标现象";
            }
            //获取分钟数据
            param.clear();
            List<String> mns = Arrays.asList(dgimn);
            List<String> pollutantcodes = Arrays.asList(pollutantcode);
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("collection", collection);
            param.put("mns", mns);
            param.put("pollutantcodes", pollutantcodes);
            param.putIfAbsent("sort", "asc");
            List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(param);
            List<Map<String, Object>> pollutants = new ArrayList<>();
            if (documents.size() > 0) {
                param.clear();
                param.put("monitorpointtype", monitorpointtype);
                param.put("codes", pollutantcodes);
                pollutants = pollutantService.getPollutantsByPollutantType(param);
            }
            resultList = MongoDataUtils.setOneOutPutManyPollutantsCharDataList(documents, pollutants, collection);
            //获取污染物标准值
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("monitorpointtype", monitorpointtype);
            standardmap = onlineMonitorService.getPollutantEarlyAlarmStandardDataByParamMap(paramMap);
            result.put("standdata",standardmap);
            result.put("datalist",resultList);
            result.put("cbqk_str",cbqk_str);
            result.put("syfx_str",syfx_str);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @Description: 统计报告类型数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/14 14:38
     */
    @RequestMapping(value = "countReportDataByYear", method = RequestMethod.POST)
    public Object countReportDataByYear(@RequestJson(value = "year") String year) {
        try {
            Map<String,Object> paramMap = new HashMap<>();
            List<Integer> reportTypes = Arrays.asList(CommonTypeEnum.AnalysisReportTypeEnum.WeekReportEnum.getCode(),
                    CommonTypeEnum.AnalysisReportTypeEnum.MonthReportEnum.getCode(),
                    CommonTypeEnum.AnalysisReportTypeEnum.ExpertReportEnum.getCode()
                    );
            paramMap.put("reporttypes",reportTypes);
            paramMap.put("year",year);
            List<Map<String, Object>> dataList = reportManagementService.countReportDataByParam(paramMap);
            Map<Integer,Object> typeAndNum = new HashMap<>();
            int type;
            for (Map<String,Object> dataMap:dataList){
                type = Integer.parseInt(dataMap.get("reporttype").toString());
                typeAndNum.put(type,dataMap.get("countnum"));
            }
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Integer typeIndex:reportTypes){
                Map<String,Object> resultMap = new HashMap<>();
                resultMap.put("reporttypecode",typeIndex);
                resultMap.put("reporttypename", CommonTypeEnum.AnalysisReportTypeEnum.getCodeByInteger(typeIndex).getName());
                resultMap.put("countnum", typeAndNum.get(typeIndex)!=null?typeAndNum.get(typeIndex):0);
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/23 0023 下午 1:06
     * @Description: 生成恶臭简报文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "createStinkBriefingReport", method = RequestMethod.POST)
    public void createStinkBriefingReport(
            @RequestJson("daytime") String daytime,
            @RequestJson("monitorpointname") String monitorpointname,
            @RequestJson("attributedata") Object attributedata,
            @RequestJson(value = "chartdata", required = false) String chartdata,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("pointname", monitorpointname);
            //设置时间值
            String dayDate = DataFormatUtil.FormatDateOneToOther(daytime + " 00:00", "yyyy-MM-dd HH:mm", "yyyy年MM月dd日H时mm分")
                    + "-" + DataFormatUtil.FormatDateOneToOther(daytime + " 23:59", "yyyy-MM-dd HH:mm", "H时mm分");
            String reportmakedateTemp = DataFormatUtil.FormatDateOneToOther(daytime, "yyyy-MM-dd", "yyyyMMdd");
            String space = getSomeSpace(24);
            resultData.put("timenum", space+reportmakedateTemp);

            //获取属性值
            List<Map<String, Object>> attributeList = (List<Map<String, Object>>) attributedata;
            for (Map<String, Object> map : attributeList) {
                //超标情况
                if ("cbqk".equals(map.get("reportattributecode"))) {
                    resultData.put("cbqk", map.get("reportattributevalue"));
                }
                //管控建议
                if ("gkjy".equals(map.get("reportattributecode"))) {
                    resultData.put("gkjy", map.get("reportattributevalue"));
                }
                //变化趋势
                if ("bhqs".equals(map.get("reportattributecode"))) {
                    resultData.put("bhqs", map.get("reportattributevalue"));
                }
                //软件溯源结果
                if ("syjg".equals(map.get("reportattributecode"))) {
                    resultData.put("syjg", map.get("reportattributevalue"));
                }
                //企业现场情况
                if ("xcqk".equals(map.get("reportattributecode"))) {
                    resultData.put("xcqk", map.get("reportattributevalue"));
                }
            }
            resultData.put("imagedata", chartdata);
            //文件名称
            String fileName = monitorpointname+"恶臭站点超标简报(" + dayDate + ").doc";
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultData, "templates/恶臭站点简报模板.ftl");
            ExcelUtil.downLoadFile(fileName, response, request, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
