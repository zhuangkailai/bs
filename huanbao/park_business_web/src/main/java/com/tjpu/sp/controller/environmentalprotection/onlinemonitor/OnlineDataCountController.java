package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineDataCountService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description: 在线数据统计类
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/2/24 9:27
 */
@RestController
@ControllerAdvice
@RequestMapping("onlineDataCount")
public class OnlineDataCountController {


    private final PollutantService pollutantService;
    private final OnlineDataCountService onlineDataCountService;
    private final OnlineMonitorService onlineMonitorService;
    private final String DB_MinuteData = "MinuteData";
    private final String DB_HourData = "HourData";
    private final String DB_DayData = "DayData";
    private final String DB_StationHourAQIData = "StationHourAQIData";
    private final String DB_StationDayAQIData = "StationDayAQIData";


    private final String DB_HourFlowData = "HourFlowData";
    private final String DB_DayFlowData = "DayFlowData";
    private final String DB_MonthFlowData = "MonthFlowData";
    private final PollutionService pollutionService;

    public OnlineDataCountController(PollutantService pollutantService, OnlineDataCountService onlineDataCountService, OnlineMonitorService onlineMonitorService, PollutionService pollutionService) {
        this.pollutantService = pollutantService;
        this.onlineDataCountService = onlineDataCountService;
        this.onlineMonitorService = onlineMonitorService;
        this.pollutionService = pollutionService;
    }


    /**
     * @Description: 获取点位日排放量（环比）排名
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/6 9:42
     */
    @RequestMapping(value = "countPointDayFlowDataByParam", method = RequestMethod.POST)
    public Object countPointDayFlowDataByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes
    ) {
        try {
            List<Map<String, Object>> resultList = getDataList(monitortime, pollutantcode, monitorpointtypes);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getDataList(String monitortime, String pollutantcode, List<Integer> monitorpointtypes) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();

        List<Map<String, Object>> outputList = new ArrayList<>();
        for (Integer type : monitorpointtypes) {
            paramMap.put("monitorPointType", type);
            outputList.addAll(onlineMonitorService.getOnlineOutPutListByParamMap(paramMap));
        }
        if (outputList.size() > 0) {
            Map<String, String> mnAndName = new HashMap<>();
            String mnCommon;
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> output : outputList) {
                if (output.get("dgimn") != null) {
                    mnCommon = output.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndName.put(mnCommon, output.get("shortername") + "-" + output.get("monitorpointname"));
                }
            }
            paramMap.clear();
            String hb_time = DataFormatUtil.getBeforeByDayTime(1, monitortime);
            String starttime = hb_time + " 00:00:00";
            String endtime = monitortime + " 23:59:59";
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("startDate", starttime);
            paramMap.put("endDate", endtime);
            paramMap.put("mns", mns);
            paramMap.put("dataKey", "DayFlowDataList");
            paramMap.put("valueKey", "AvgFlow");
            paramMap.put("collection", DB_DayFlowData);
            List<Document> documents = onlineDataCountService.getPollutantFlowDataByParam(paramMap);
            if (documents.size() > 0) {
                Map<String, Double> timeAndValue;
                String change;
                Double thisValue;
                Double hbValue;
                Map<String, Map<String, Double>> mnAndTimeAndValue = setMnAndTimeAndValue(documents);
                for (String mnIndex : mnAndTimeAndValue.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("dgimn", mnIndex);
                    resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                    timeAndValue = mnAndTimeAndValue.get(mnIndex);
                    thisValue = timeAndValue.get(monitortime);
                    resultMap.put("thisvalue", thisValue);
                    hbValue = timeAndValue.get(hb_time);
                    resultMap.put("hbvalue", hbValue);
                    resultMap.put("orderindex", thisValue != null ? thisValue : 0);
                    if (hbValue != null && thisValue != null && hbValue > 0) {
                        change = DataFormatUtil.formatDoubleSaveOne(100d * (thisValue - hbValue) / hbValue) + "%";
                        if (change.contains("-")) {
                            change = change + "↓";
                        } else if ((thisValue - hbValue) == 0) {

                        } else {
                            change = change + "↑";
                        }
                        resultMap.put("change", change);
                    } else {
                        resultMap.put("change", "");
                    }
                    resultList.add(resultMap);
                }
                //排序
                resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("orderindex").toString())).reversed()).collect(Collectors.toList());
                int ordernum = 0;
                for (Map<String, Object> resultMap : resultList) {
                    ordernum++;
                    resultMap.put("ordernum", ordernum);
                }
            }
        }
        return resultList;
    }


    /**
     * @Description: 导出点位日排放量（环比）排名
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/6 9:42
     */
    @RequestMapping(value = "exportPointDayFlowDataByParam", method = RequestMethod.POST)
    public void exportPointDayFlowDataByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        try {
            exportPDayFD(monitortime, pollutantcode, monitorpointtypes, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 导出点位日排放量（环比）排名
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/6 9:42
     */
    @RequestMapping(value = "exportPDayFDByParam", method = RequestMethod.GET)
    public void exportPDayFDByParam(
            @RequestParam(value = "monitortime") String monitortime,
            @RequestParam(value = "pollutantcode") String pollutantcode,
            @RequestParam(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        try {
            exportPDayFD(monitortime, pollutantcode, monitorpointtypes, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void exportPDayFD(String monitortime, String pollutantcode, List<Integer> monitorpointtypes, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> resultList = getDataList(monitortime, pollutantcode, monitorpointtypes);
        List<String> headers = Arrays.asList("排名", "监测点名称", "日排放量", "环比值", "环比变化");
        List<String> headersField = Arrays.asList("ordernum", "monitorpointname", "thisvalue", "hbvalue", "change");

        //设置文件名称
        String fileName = "监测站点日排放量数据_" + new Date().getTime();
        ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
    }

    private Map<String, Map<String, Double>> setMnAndTimeAndValue(List<Document> documents) {
        Map<String, Map<String, Double>> mnAndTimeAndValue = new HashMap<>();
        Map<String, Double> timeAndValue;
        String mnCommon;
        String time;
        Double value;
        for (Document document : documents) {
            mnCommon = document.getString("DataGatherCode");
            time = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            if (mnAndTimeAndValue.containsKey(mnCommon)) {
                timeAndValue = mnAndTimeAndValue.get(mnCommon);
            } else {
                timeAndValue = new HashMap<>();
            }
            value = document.get("value") != null ? Double.parseDouble(document.getString("value")) : 0d;
            timeAndValue.put(time, DataFormatUtil.formatDoubleSaveThree(value));
            mnAndTimeAndValue.put(mnCommon, timeAndValue);

        }
        return mnAndTimeAndValue;

    }


    /**
     * @Description: 获取企业排放量排名（小时、日、月）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/9/6 9:42
     */
    @RequestMapping(value = "countEntFlowDataByParam", method = RequestMethod.POST)
    public Object countEntFlowDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes
    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> outputList = new ArrayList<>();
            for (Integer type : monitorpointtypes) {
                paramMap.put("monitorPointType", type);
                outputList.addAll(onlineMonitorService.getOnlineOutPutListByParamMap(paramMap));
            }
            if (outputList.size() > 0) {
                Map<String, String> mnAndId = new HashMap<>();
                Map<String, String> idAndName = new HashMap<>();
                String mnCommon;
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> output : outputList) {
                    if (output.get("dgimn") != null) {
                        mnCommon = output.get("dgimn").toString();
                        mns.add(mnCommon);
                        mnAndId.put(mnCommon, output.get("pk_pollutionid") + "");
                        idAndName.put(output.get("pk_pollutionid") + "",
                                output.get("pollutionname")
                                        + "," + output.get("shortername"));
                    }
                }
                paramMap.clear();
                String startDate = "";
                String endDate = "";
                String collection = "";
                String dataKey = "";
                String valueKey = "";
                if (timetype.equals("hour")) {
                    startDate = starttime + ":00:00";
                    endDate = endtime + ":59:59";
                    collection = DB_HourFlowData;
                    dataKey = "HourFlowDataList";
                    valueKey = "AvgFlow";
                } else if (timetype.equals("day")) {
                    startDate = starttime + " 00:00:00";
                    endDate = endtime + " 23:59:59";
                    collection = DB_DayFlowData;
                    dataKey = "DayFlowDataList";
                    valueKey = "AvgFlow";
                } else if (timetype.equals("month")) {
                    startDate = DataFormatUtil.getFirstDayOfMonth(starttime) + " 00:00:00";
                    endDate = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
                    collection = DB_MonthFlowData;
                    dataKey = "MonthFlowDataList";
                    valueKey = "PollutantFlow";
                }
                if (StringUtils.isBlank(startDate)) {
                    return null;
                }


                paramMap.put("pollutantcode", pollutantcode);
                paramMap.put("startDate", startDate);
                paramMap.put("endDate", endDate);
                paramMap.put("mns", mns);
                paramMap.put("dataKey", dataKey);
                paramMap.put("valueKey", valueKey);
                paramMap.put("collection", collection);
                List<Document> documents = onlineDataCountService.getPollutantFlowDataByParam(paramMap);
                if (documents.size() > 0) {
                    Map<String, BigDecimal> idAndFlow = new HashMap<>();
                    String pollutionId;
                    BigDecimal flow;
                    BigDecimal total = new BigDecimal("0");
                    for (Document document : documents) {
                        mnCommon = document.getString("DataGatherCode");
                        pollutionId = mnAndId.get(mnCommon);
                        flow = document.get("value") != null ? new BigDecimal(document.getString("value")) : new BigDecimal("0");
                        if (idAndFlow.containsKey(pollutionId)) {
                            idAndFlow.put(pollutionId, idAndFlow.get(pollutionId).add(flow));
                        } else {
                            idAndFlow.put(pollutionId, flow);
                        }
                        total = total.add(flow);
                    }
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        String format = "######0.00";
                        Double flowD;
                        Double totalD = total.doubleValue();
                        totalD = DataFormatUtil.formatDoubleByFormat(format, totalD);
                        String rateValue;
                        for (String id : idAndFlow.keySet()) {
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("pollutionid", id);
                            resultMap.put("pollutionname", idAndName.get(id).split(",")[0]);
                            resultMap.put("shortername", idAndName.get(id).split(",")[1]);
                            flowD = idAndFlow.get(id).doubleValue();
                            flowD = DataFormatUtil.formatDoubleByFormat(format, flowD);
                            rateValue = DataFormatUtil.SaveTwoAndSubZero(100d * flowD / totalD) + "%";
                            resultMap.put("flowvalue", DataFormatUtil.SaveTwoAndSubZero(flowD));
                            resultMap.put("orderindex", flowD);
                            resultMap.put("ratevalue", rateValue);
                            resultList.add(resultMap);
                        }
                        //排序
                        resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("orderindex").toString())).reversed()).collect(Collectors.toList());

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
     * @Description: 获取点位当前、同比、环比分析数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/24 9:28
     */
    @RequestMapping(value = "getPointContrastDataByParam", method = RequestMethod.POST)
    public Object getPointContrastDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                              @RequestJson(value = "dgimn") String dgimn,
                                              @RequestJson(value = "pollutantcode") String pollutantcode,
                                              @RequestJson(value = "ishasconvertdata") Integer ishasconvertdata
    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            //当前时间
            Map<String, Object> paramMap = setParamMap(monitortime, ishasconvertdata);
            if (paramMap != null) {
                Map<String, Object> thisMap = getResultMap(paramMap, dgimn, pollutantcode, "当前");
                resultList.add(thisMap);
            }
            //同比时间
            String TBTime = getTBTime(monitortime);
            paramMap = setParamMap(TBTime, ishasconvertdata);
            if (paramMap != null) {
                Map<String, Object> thisMap = getResultMap(paramMap, dgimn, pollutantcode, "同比");
                resultList.add(thisMap);
            }
            //环比时间
            String HBTime = getHBTime(monitortime);
            paramMap = setParamMap(HBTime, ishasconvertdata);
            if (paramMap != null) {
                Map<String, Object> thisMap = getResultMap(paramMap, dgimn, pollutantcode, "环比");
                resultList.add(thisMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取点位散点分布数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/24 9:28
     */
    @RequestMapping(value = "getPointScatterDataByParam", method = RequestMethod.POST)
    public Object getPointScatterDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String enttime,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "ishasconvertdata") Integer ishasconvertdata
    ) {
        try {

            List<List<Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = getParamMap(starttime, enttime, ishasconvertdata);
            paramMap.put("dgimns", Arrays.asList(dgimn));
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            List<Document> documents = onlineDataCountService.getMonUnWindDataByParam(paramMap);
            String time;
            for (Document document : documents) {
                time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), paramMap.get("timef").toString());
                resultList.add(Arrays.asList(time, document.get("value")));
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取水站点水质类别日历图
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/24 9:28
     */
    @RequestMapping(value = "getPointLevelDataByParam", method = RequestMethod.POST)
    public Object getPointLevelDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String enttime,
            @RequestJson(value = "dgimn") String dgimn
    ) {
        try {


            Map<String, Object> paramMap = new HashMap<>();
            String startTime = DataFormatUtil.getFirstDayOfMonth(starttime) + " 00:00:00";
            String endTime = DataFormatUtil.getLastDayOfMonth(enttime) + " 23:59:59";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimns", Arrays.asList(dgimn));
            paramMap.put("valuekey", "WaterLevel");
            paramMap.put("collection", DB_DayData);
            List<Document> documents = onlineDataCountService.getMongodbDataByParam(paramMap);

            Map<String, List<Map<String, Object>>> monthAndDataList = new HashMap<>();
            List<Map<String, Object>> dataList;
            String month;
            String day;
            for (Document document : documents) {
                month = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), "yyyy-MM");
                day = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), "yyyy-MM-dd");
                if (monthAndDataList.containsKey(month)) {
                    dataList = monthAndDataList.get(month);
                } else {
                    dataList = new ArrayList<>();
                }
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("time", day);
                dataMap.put("code", document.get("value"));
                dataList.add(dataMap);
                monthAndDataList.put(month, dataList);
            }
            monthAndDataList = DataFormatUtil.sortByKey(monthAndDataList, false);
            return AuthUtil.parseJsonKeyToLower("success", monthAndDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取站点报警时长
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/24 9:28
     */
    @RequestMapping(value = "getPointAlarmTimeDataByParam", method = RequestMethod.POST)
    public Object getPointAlarmTimeDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype
    ) {
        try {

            List<Map<String, Object>> resultList;
            Map<String, Object> paramMap = new HashMap<>();

            String startTime;
            String endTime;
            if (timetype.equals("real")) {
                startTime = starttime;
                endTime = endtime;
            } else if (timetype.equals("minute")) {
                startTime = starttime + ":00";
                endTime = endtime + ":59";
            } else if (timetype.equals("hour")) {
                startTime = starttime + ":00:00";
                endTime = endtime + ":59:59";
            } else {
                startTime = starttime + " 00:00:00";
                endTime = endtime + " 23:59:59";
            }


            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimns", Arrays.asList(dgimn));
            List<Document> documents = onlineDataCountService.getOverModelDataByParam(paramMap);
            if (timetype.equals("day")) {
                resultList = getHourDataForDay(documents, monitorpointtype, starttime, endtime);
            } else {
                resultList = getMinuteDataForHour(documents, monitorpointtype, starttime, endtime);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getHourDataForDay(List<Document> documents, Integer monitorpointtype,
                                                        String starttime, String endtime) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Long>> dayAndCodeAndMinute = new HashMap<>();
        Map<String, Object> codeAndName = new HashMap<>();
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

            codeAndName = getPollutantCodeAndName(monitorpointtype);
        }
        List<String> days = DataFormatUtil.getYMDBetween(starttime, endtime);
        days.add(endtime);
        Map<String, Long> codeAndMinute;
        long subtime;
        Map<String, Integer> dayAndM = setDayAndM(dayAndStart_End);
        for (String day : days) {
            codeAndMinute = dayAndCodeAndMinute.get(day);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("monitortime", day);
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            if (codeAndMinute != null) {
                for (String code : codeAndMinute.keySet()) {
                    subtime = codeAndMinute.get(code);
                    Map<String, Object> pollutant = new HashMap<>();
                    pollutant.put("pollutantcode", code);
                    pollutant.put("pollutantname", codeAndName.get(code));
                    pollutant.put("subtime", subtime);
                    pollutantList.add(pollutant);
                }
            }
            resultMap.put("sumtime", dayAndM.get(day));
            resultMap.put("pollutantlist", pollutantList);
            resultList.add(resultMap);
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


    private List<Map<String, Object>> getMinuteDataForHour(List<Document> documents,
                                                           Integer monitorpointtype,
                                                           String starttime,
                                                           String endtime) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Long>> hourAndCodeAndMinute = new HashMap<>();
        Map<Integer, List<List<Date>>> hourAndStart_End = new HashMap<>();

        Map<String, Object> codeAndName = new HashMap<>();
        if (documents.size() > 0) {
            Date startT;
            Date endT;
            Integer Shour;
            Integer Ehour;
            String pollutantCode;
            int hourN;
            String hourString;
            for (Document document : documents) {
                startT = document.getDate("FirstOverTime");
                endT = document.getDate("LastOverTime");
                Shour = DataFormatUtil.getDateHourNum(startT);
                Ehour = DataFormatUtil.getDateHourNum(endT);
                pollutantCode = document.getString("PollutantCode");
                //判断是否跨小时
                hourN = Ehour - Shour;
                if (hourN == 0) {//否
                    setMapToNum(pollutantCode, startT, startT, endT, hourAndCodeAndMinute);
                    setHourAndSE(Shour, startT, endT, hourAndStart_End);
                } else {//是
                    for (int i = Shour; i < Ehour; i++) {
                        if (i < 10) {
                            hourString = " 0" + i;
                        } else {
                            hourString = " " + i;
                        }
                        if (i != Shour) {
                            startT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(startT) + hourString + ":00:00");
                        }
                        Date endTE = DataFormatUtil.getDateYMDHMS((DataFormatUtil.getDateYMD(startT) + hourString + ":59:59"));
                        setMapToNum(pollutantCode, startT, startT, endTE, hourAndCodeAndMinute);
                        setHourAndSE(i, startT, endTE, hourAndStart_End);
                    }
                    startT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMDH(endT) + ":00:00");
                    setMapToNum(pollutantCode, endT, startT, endT, hourAndCodeAndMinute);
                    setHourAndSE(Ehour, startT, endT, hourAndStart_End);
                }
            }

            codeAndName = getPollutantCodeAndName(monitorpointtype);
        }
        List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);
        hours.add(endtime);
        Map<String, Long> codeAndMinute;
        long subtime;
        String hKey;
        Map<String, Integer> hourAndM = setHourAndM(hourAndStart_End);
        for (String hour : hours) {
            codeAndMinute = hourAndCodeAndMinute.get(hour);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("monitortime", hour);
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            if (codeAndMinute != null) {
                for (String code : codeAndMinute.keySet()) {
                    subtime = codeAndMinute.get(code);
                    Map<String, Object> pollutant = new HashMap<>();
                    pollutant.put("pollutantcode", code);
                    pollutant.put("pollutantname", codeAndName.get(code));
                    pollutant.put("subtime", subtime);
                    pollutantList.add(pollutant);
                }
            }
            hKey = hour.split(" ")[1];
            resultMap.put("sumtime", hourAndM.get(hKey));
            resultMap.put("pollutantlist", pollutantList);
            resultList.add(resultMap);
        }
        return resultList;
    }

    private Map<String, Integer> setHourAndM(Map<Integer, List<List<Date>>> hourAndStart_end) {
        Map<String, Integer> hourAndNum = new HashMap<>();
        Date startTime;
        Date endTime;
        String mS;
        String mE;
        String ymdhms;
        List<List<Date>> dateList;
        String hourS;
        List<String> minuteNums;
        List<String> sumNum;
        for (Integer hour : hourAndStart_end.keySet()) {
            dateList = hourAndStart_end.get(hour);
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

            hourS = DataFormatUtil.FormatDateOneToOther(hour.toString(), "H", "HH");
            minuteNums = minuteNums.stream().distinct().collect(Collectors.toList());
            hourAndNum.put(hourS, minuteNums.size());
        }
        return hourAndNum;

    }

    private void setHourAndSE(Integer Shour, Date startT, Date endT, Map<Integer, List<List<Date>>> hourAndStart_End) {
        List<List<Date>> S_EList;
        if (hourAndStart_End.containsKey(Shour)) {
            S_EList = hourAndStart_End.get(Shour);
        } else {
            S_EList = new ArrayList<>();
        }
        S_EList.add(Arrays.asList(startT, endT));
        hourAndStart_End.put(Shour, S_EList);
    }

    /**
     * @Description: 获取企业报警时长
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/24 9:28
     */
    @RequestMapping(value = "getEntAlarmTimeDataByParam", method = RequestMethod.POST)
    public Object getEntAlarmTimeDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String enttime) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            List<Map<String, Object>> entPointList = pollutionService.getPointListByParam(paramMap);
            if (entPointList.size() > 0) {
                Map<String, Object> idAndName = new HashMap<>();
                Map<String, Object> mnAndId = new HashMap<>();
                Map<String, Object> mnAndType = new HashMap<>();
                Map<String, Object> mnAndpointname = new HashMap<>();
                Map<String, Object> mnAndpointid = new HashMap<>();
                List<String> dgimns = new ArrayList<>();
                String dgimn;
                String pollutionid;
                for (Map<String, Object> point : entPointList) {
                    if (point.get("dgimn") != null && point.get("pollutionid") != null && point.get("monitorpointtype") != null) {
                        dgimn = point.get("dgimn").toString();
                        pollutionid = point.get("pollutionid").toString();
                        idAndName.put(pollutionid, point.get("pollutionname"));
                        mnAndId.put(dgimn, pollutionid);
                        mnAndType.put(dgimn, point.get("monitorpointtype"));
                        mnAndpointname.put(dgimn, point.get("monitorpointname"));
                        mnAndpointid.put(dgimn, point.get("monitorpointid"));
                        dgimns.add(dgimn);
                    }
                }
                String startTime = starttime + " 00:00:00";
                String endTime = enttime + " 23:59:59";
                paramMap.put("starttime", startTime);
                paramMap.put("endtime", endTime);
                paramMap.put("dgimns", dgimns);
                List<Document> documents = onlineDataCountService.getOverModelDataByParam(paramMap);
                if (documents.size() > 0) {
                    Map<String, Long> idAndMinute = new HashMap<>();
                    Map<String, Set<Integer>> idAndTypes = new HashMap<>();
                    Map<String, Set<String>> idAndDgimn = new HashMap<>();
                    Set<Integer> types;
                    Set<String> mns;
                    Date startT;
                    Date endT;
                    long minute;
                    for (Document document : documents) {
                        dgimn = document.getString("DataGatherCode");
                        startT = document.getDate("FirstOverTime");
                        endT = document.getDate("LastOverTime");
                        if (!DataFormatUtil.getDateYMD(startT).equals(DataFormatUtil.getDateYMD(endT))) {
                            endT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(startT) + " 23:59:59");
                        }
                        minute = DataFormatUtil.getDateMinutes(startT, endT) == 0 ? 1 : DataFormatUtil.getDateMinutes(startT, endT);
                        pollutionid = mnAndId.get(dgimn).toString();
                        idAndMinute.put(pollutionid, idAndMinute.get(pollutionid) != null ? idAndMinute.get(pollutionid) + minute : minute);
                        if (idAndTypes.containsKey(pollutionid)) {
                            types = idAndTypes.get(pollutionid);
                        } else {
                            types = new HashSet<>();
                        }
                        types.add(Integer.parseInt(mnAndType.get(dgimn) + ""));
                        idAndTypes.put(pollutionid, types);
                        if (idAndDgimn.containsKey(pollutionid)) {
                            mns = idAndDgimn.get(pollutionid);
                        } else {
                            mns = new HashSet<>();
                        }
                        mns.add(dgimn);
                        idAndDgimn.put(pollutionid, mns);
                    }
                    idAndMinute = DataFormatUtil.sortMapByValue(idAndMinute, true);
                    int num = 5;
                    if (idAndMinute.size() < num) {
                        num = idAndMinute.size();
                    }
                    int index = 0;
                    String carecontent;
                    List<Integer> typeList;
                    List<String> mnList;
                    List<String> nameList;
                    for (String idIndex : idAndMinute.keySet()) {
                        index++;
                        if (index > num) {
                            break;
                        }
                        List<Map<String, Object>> points = new ArrayList<>();
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("pollutionid", idIndex);
                        resultMap.put("pollutionname", idAndName.get(idIndex));
                        typeList = new ArrayList<>(idAndTypes.get(idIndex));
                        mnList = new ArrayList<>(idAndDgimn.get(idIndex));
                        Collections.sort(typeList);
                        nameList = new ArrayList<>();
                        for (Integer type : typeList) {
                            nameList.add(CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(type)
                                    .replace("监测点", "")
                                    .replace("烟气", "废气"));
                        }
                        for (String mn : mnList) {
                            Map<String, Object> onemap = new HashMap<>();
                            onemap.put("dgimn", mn);
                            onemap.put("monitorpointtype", mnAndType.get(mn));
                            onemap.put("monitorpointname", mnAndpointname.get(mn));
                            onemap.put("monitorpointid", mnAndpointid.get(mn));
                            points.add(onemap);
                        }
                        if (points != null && points.size() > 0) {
                            points = points.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("monitorpointtype").toString()))).collect(Collectors.toList());
                        }
                        resultMap.put("pointdata", points);
                        carecontent = DataFormatUtil.FormatListToString(nameList, "、");
                        resultMap.put("carecontent", carecontent + "超标排放");
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


    private Map<String, Object> getPollutantCodeAndName(Integer monitorpointtype) {
        Map<String, Object> codeAndName = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", monitorpointtype);

        List<Map<String, Object>> dataList = pollutantService.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> dataMap : dataList) {
            codeAndName.put(dataMap.get("code").toString(), dataMap.get("name"));
        }
        return codeAndName;
    }

    private void setMapToNum(String pollutantCode, Date ymdhD, Date startT, Date endT,
                             Map<String, Map<String, Long>> hourAndCodeAndMinute) {
        long minuteNum = DataFormatUtil.getDateMinutes(startT, endT) == 0 ? 1 : DataFormatUtil.getDateMinutes(startT, endT);


        String ymdh = DataFormatUtil.getDateYMDH(ymdhD);
        Map<String, Long> codeAndMinute;
        if (hourAndCodeAndMinute.containsKey(ymdh)) {
            codeAndMinute = hourAndCodeAndMinute.get(ymdh);
        } else {
            codeAndMinute = new HashMap<>();
        }
        if (codeAndMinute.containsKey(pollutantCode)) {
            codeAndMinute.put(pollutantCode, codeAndMinute.get(pollutantCode) + minuteNum);
        } else {
            codeAndMinute.put(pollutantCode, minuteNum);
        }
        if (codeAndMinute.get(pollutantCode) > 60) {
            codeAndMinute.put(pollutantCode, 60l);
        }
        hourAndCodeAndMinute.put(ymdh, codeAndMinute);
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

    private Map<String, Object> getParamMap(String starttime, String enttime, Integer ishasconvertdata) {

        String startTime = "";
        String endTime = "";
        String collection = "";
        String unWind = "";
        String timeF = "";
        String valueKey;
        if (ishasconvertdata != null && ishasconvertdata == 1) {//折算
            valueKey = "AvgConvertStrength";
        } else {
            valueKey = "AvgStrength";
        }
        if (starttime.length() == 13) {//小时时间，分钟段
            startTime = starttime + ":00:00";
            endTime = enttime + ":59:59";
            collection = DB_MinuteData;
            unWind = "MinuteDataList";
            timeF = "HH:mm";
        } else if (starttime.length() == 10) {//日时间，小时段
            startTime = starttime + " 00:00:00";
            endTime = enttime + " 23:59:59";
            collection = DB_HourData;
            unWind = "HourDataList";
            timeF = "H时";
        } else if (starttime.length() == 7) {//月时间，日段
            startTime = DataFormatUtil.getFirstDayOfMonth(starttime) + " 00:00:00";
            endTime = DataFormatUtil.getLastDayOfMonth(enttime) + " 23:59:59";
            collection = DB_DayData;
            unWind = "DayDataList";
            timeF = "d日";
        }
        if (StringUtils.isNotBlank(collection)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("collection", collection);
            paramMap.put("unwind", unWind);
            paramMap.put("valuekey", valueKey);
            paramMap.put("timef", timeF);
            return paramMap;
        } else {
            return null;
        }
    }


    private String getHBTime(String monitortime) {
        String HBTime = "";
        if (monitortime.length() == 13) {//小时
            HBTime = DataFormatUtil.getBeforeByHourTime(1, monitortime);
        } else if (monitortime.length() == 10) {//日
            HBTime = DataFormatUtil.getBeforeByDayTime(1, monitortime);
        } else if (monitortime.length() == 7) {//月
            HBTime = DataFormatUtil.getBeforeByMonthTime(1, monitortime + "-01");
            HBTime = HBTime.substring(0, HBTime.length() - 3);
        }
        return HBTime;
    }

    private String getTBTime(String monitortime) {
        String TBTime = "";
        if (monitortime.length() == 13) {//小时
            TBTime = DataFormatUtil.getHourTBDate(monitortime);
        } else if (monitortime.length() == 10) {//日
            TBTime = DataFormatUtil.getDayTBDate(monitortime);
        } else if (monitortime.length() == 7) {//月
            TBTime = DataFormatUtil.getMonthTBDate(monitortime + "-01");
            TBTime = TBTime.substring(0, TBTime.length() - 3);
        }
        return TBTime;
    }

    private Map<String, Object> getResultMap(Map<String, Object> paramMap, String dgimn, String pollutantcode, String text) {
        Map<String, Object> resultMap = new HashMap<>();
        paramMap.put("dgimns", Arrays.asList(dgimn));
        paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
        List<Document> documents = onlineDataCountService.getMonUnWindDataByParam(paramMap);
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (documents.size() > 0) {
            Set<String> times = new HashSet<>();
            String time;
            for (Document document : documents) {
                Map<String, Object> dataMap = new HashMap<>();

                time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), paramMap.get("timef").toString());
                if (!times.contains(time)) {
                    dataMap.put("monitortime", time);
                    dataMap.put("value", document.get("value"));
                    dataList.add(dataMap);
                    times.add(time);
                }

            }
        }
        resultMap.put("textname", text + paramMap.get("text"));
        resultMap.put("dataList", dataList);
        return resultMap;
    }

    private Map<String, Object> setParamMap(String monitortime, Integer ishasconvertdata) {
        String startTime = "";
        String endTime = "";
        String collection = "";
        String text = "";
        String unWind = "";
        String timeF = "";
        String valueKey;

        if (ishasconvertdata != null && ishasconvertdata == 1) {//折算
            valueKey = "AvgConvertStrength";
        } else {
            valueKey = "AvgStrength";
        }
        if (monitortime.length() == 13) {//小时时间，分钟段
            startTime = monitortime + ":00:00";
            endTime = monitortime + ":59:59";
            collection = DB_MinuteData;
            text = "小时(" + monitortime + ")";
            unWind = "MinuteDataList";
            timeF = "HH:mm";

        } else if (monitortime.length() == 10) {//日时间，小时段
            startTime = monitortime + " 00:00:00";
            endTime = monitortime + " 23:59:59";
            collection = DB_HourData;
            text = "日(" + monitortime + ")";
            unWind = "HourDataList";
            timeF = "H时";
        } else if (monitortime.length() == 7) {//月时间，日段
            startTime = DataFormatUtil.getFirstDayOfMonth(monitortime) + " 00:00:00";
            endTime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
            collection = DB_DayData;
            text = "月(" + monitortime + ")";
            unWind = "DayDataList";
            timeF = "d日";
        }
        if (StringUtils.isNotBlank(collection)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("collection", collection);
            paramMap.put("text", text);
            paramMap.put("unwind", unWind);
            paramMap.put("valuekey", valueKey);
            paramMap.put("timef", timeF);
            return paramMap;
        } else {
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/25 0025 上午 11:22
     * @Description: 获取某个企业24小时内某类型某污染物每小时浓度变化趋势及同比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, pollutionid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getEntPollutantHourOnlineDataByParams", method = RequestMethod.POST)
    public Object getEntPollutantHourOnlineDataByParams(@RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                        @RequestJson(value = "pollutionid") String pollutionid,
                                                        @RequestJson(value = "pollutantcode") String pollutantcode,
                                                        @RequestJson(value = "daydate", required = false) String daydate
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkmonitorpointtypecode", monitorpointtype);
            paramMap.put("fk_pollutionid", pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
            Map<String, Object> mnandname = new HashMap<>();
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : outPutInfosByParamMap) {
                if (map.get("DGIMN") != null) {
                    mns.add(map.get("DGIMN").toString());
                    mnandname.put(map.get("DGIMN").toString(), map.get("OutputName"));
                }
            }
            if (daydate == null) {
                daydate = DataFormatUtil.getDateYMD(new Date());
            }
            String starttime = daydate + " 00:00:00";
            String endtime = daydate + " 23:59:59";
            //今年数据
            List<Document> hourData = onlineDataCountService.getEntPollutantHourOnlineDataByParams(starttime, endtime, mns, pollutantcode);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar instancetwo = Calendar.getInstance();
            instancetwo.setTime(DataFormatUtil.getDateYMDHMS(starttime));
            instancetwo.add(Calendar.YEAR, -1);
            Date datestart = instancetwo.getTime();
            String starttimetwo = format.format(datestart);
            instancetwo.setTime(DataFormatUtil.getDateYMDHMS(endtime));
            instancetwo.add(Calendar.YEAR, -1);
            Date date_end = instancetwo.getTime();
            String endtimetwo = format.format(date_end);
            //去年数据
            List<Document> lasthourData = onlineDataCountService.getEntPollutantHourOnlineDataByParams(starttimetwo, endtimetwo, mns, pollutantcode);
            List<Map<String, Object>> result = new ArrayList<>();
            for (String mn : mns) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("mn", mn);
                onemap.put("pointname", mnandname.get(mn));
                List<Map<String, Object>> valuedata = new ArrayList<>();
                String monitortime;
                int hournum;
                for (int i = 0; i < 24; i++) {
                    Map<String, Object> twomap = new HashMap<>();
                    twomap.put("hourtime", i + "时");
                    twomap.put("thisvalue", "");
                    twomap.put("lastvalue", "");
                    //当前时间数据
                    if (hourData != null && hourData.size() > 0) {
                        for (Document document : hourData) {
                            if (mn.equals(document.getString("DataGatherCode"))) {
                                monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                                hournum = Integer.valueOf(monitortime.substring(monitortime.length() - 2, monitortime.length()));
                                if (i == hournum) {
                                    twomap.put("thisvalue", document.get("value"));
                                    break;
                                }
                            }
                        }
                    }
                    //同比 去年同一天数据
                    if (lasthourData != null && lasthourData.size() > 0) {
                        for (Document document : lasthourData) {
                            if (mn.equals(document.getString("DataGatherCode"))) {
                                monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                                hournum = Integer.valueOf(monitortime.substring(monitortime.length() - 2, monitortime.length()));
                                if (i == hournum) {
                                    twomap.put("lastvalue", document.get("value"));
                                    break;
                                }
                            }
                        }
                    }
                    valuedata.add(twomap);
                }
                onemap.put("valuedata", valuedata);
                result.add(onemap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 单站点时间对比分析
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/10/19 9:00
     */
    @RequestMapping(value = "getPointMonitorDataForDay", method = RequestMethod.POST)
    public Object getPointMonitorDataForDay(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "ishasconvertdata") Integer ishasconvertdata,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datatype") String datatype
    ) {
        try {

            String collection;
            String dataKey;
            String valueKey;
            if (1 == ishasconvertdata) {//取折算
                valueKey = "AvgConvertStrength";
            } else {
                valueKey = "AvgStrength";
            }
            String timeF;
            if ("minute".equals(datatype)) {//分钟
                if ("aqi".equals(pollutantcode)) {
                    dataKey = "";
                    collection = "";
                    valueKey = "";
                } else {
                    dataKey = "MinuteDataList";
                    collection = DB_MinuteData;
                }
                timeF = "yyyy-MM-dd HH:mm";
            } else {//小时
                if ("aqi".equals(pollutantcode)) {
                    collection = DB_StationHourAQIData;
                    dataKey = "";
                } else {
                    dataKey = "HourDataList";
                    collection = DB_HourData;
                }
                timeF = "yyyy-MM-dd HH";
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            paramMap.put("dgimn", dgimn);
            paramMap.put("collection", collection);
            paramMap.put("dataKey", dataKey);
            paramMap.put("valueKey", valueKey);
            paramMap.put("pollutantcode", pollutantcode);
            List<Document> documents = onlineDataCountService.getMonUnWindOrAirDataByParam(paramMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (documents.size() > 0) {

                Map<String, List<Document>> dayAndDataList = setDayAndDoc(documents);
                List<Document> subDoc;
                for (String day : dayAndDataList.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("daytime", day);
                    List<Map<String,Object>> dataList = new ArrayList<>();
                    subDoc = dayAndDataList.get(day);
                    for (Document document : subDoc) {
                        Map<String,Object> dataMap = new HashMap<>();
                        dataMap.put("monitortime",DataFormatUtil.parseDateToStringByFormat(
                                document.getDate("MonitorTime"),timeF));
                        dataMap.put("value",document.get("value"));
                        dataList.add(dataMap);
                    }
                    //排序
                    dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
                    resultMap.put("datalist", dataList);
                    resultList.add(resultMap);
                }
            }


            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, List<Document>> setDayAndDoc(List<Document> documents) {
        Map<String, List<Document>> dayAndDoc = new HashMap<>();
        List<Document> subDoc;
        String day;
        for (Document document : documents) {
            day = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            subDoc = dayAndDoc.containsKey(day) ? dayAndDoc.get(day) : new ArrayList<>();
            subDoc.add(document);
            dayAndDoc.put(day, subDoc);
        }
        return dayAndDoc;
    }


}
