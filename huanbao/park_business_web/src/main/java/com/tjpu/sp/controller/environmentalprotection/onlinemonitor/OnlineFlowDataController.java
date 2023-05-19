package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.common.mongodb.FlowDataVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineFlowDataService;
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: zhangzc
 * @date: 2019/8/28 11:56
 * @Description: 排放量数据相关接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RequestMapping("onlineFlowData")
@RestController
public class OnlineFlowDataController {

    private final OnlineFlowDataService onlineFlowDataService;
    private final OnlineMonitorService onlineMonitorService;
    private final OnlineService onlineService;
    private final PollutionService pollutionService;
    private final PollutantService pollutantService;
    private final GasOutPutPollutantSetService gasOutPutPollutantSetService;

    @Autowired
    private MongoBaseService mongoBaseService;

    public OnlineFlowDataController(OnlineFlowDataService onlineFlowDataService, OnlineMonitorService onlineMonitorService, OnlineService onlineService, PollutionService pollutionService, PollutantService pollutantService, GasOutPutPollutantSetService gasOutPutPollutantSetService) {
        this.onlineFlowDataService = onlineFlowDataService;
        this.onlineMonitorService = onlineMonitorService;

        this.onlineService = onlineService;
        this.pollutionService = pollutionService;
        this.pollutantService = pollutantService;
        this.gasOutPutPollutantSetService = gasOutPutPollutantSetService;
    }

    private final String db_dayFlowData = "DayFlowData";
    private final String db_monthFlowData = "MonthFlowData";
    private final String db_yearFlowData = "YearFlowData";

    /**
     * @author: zhangzc
     * @date: 2019/8/28 11:59
     * @Description: 小时和天数据获取排放量的
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getHourAndDayFlowDataByParam", method = RequestMethod.POST)
    public Object getHourAndDayFlowDataByParam(@RequestJson(value = "mns") List<String> mns,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "timetype") String timetype,
                                               @RequestJson(value = "pollutantcodes") List<String> pollutantcodes) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String collection;
            String unwindFieldName;
            Date startTime;
            Date endTime;
            switch (timetype) {
                case "hour":
                    collection = "HourFlowData";
                    unwindFieldName = "HourFlowDataList";
                    startTime = DataFormatUtil.parseDate(starttime + ":00:00");
                    endTime = DataFormatUtil.parseDate(endtime + ":59:59");
                    break;
                case "day":
                    startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
                    endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
                    collection = "DayFlowData";
                    unwindFieldName = "DayFlowDataList";
                    break;
                default:
                    return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
            List<Document> documentList = onlineFlowDataService.getHourAndDayFlowDataByParam(mns, pollutantcodes, startTime, endTime, unwindFieldName, collection);
            //根据mn号和污染物code分组
            Map<String, Map<String, List<Document>>> listMap = documentList.stream().collect(Collectors.groupingBy(m -> m.getString("DataGatherCode"), Collectors.groupingBy(m -> m.getString("PollutantCode"))));
            for (String mn : mns) {
                if (listMap.containsKey(mn)) {
                    Map<String, Object> mnMap = new HashMap<>();
                    List<Map<String, Object>> pollutantList = new ArrayList<>();
                    for (String pollutantcode : pollutantcodes) {
                        Map<String, List<Document>> pollutantMap = listMap.get(mn);
                        if (pollutantMap.containsKey(pollutantcode)) {
                            Map<String, Object> pollutantMapInfo = new HashMap<>();
                            List<Map<String, Object>> monitordatalist = new ArrayList<>();
                            List<Document> documents = pollutantMap.get(pollutantcode);
                            for (Document document : documents) {
                                Map<String, Object> dataMap = new HashMap<>();
                                Date monitorTimeInfo = document.getDate("MonitorTime");
                                String monitorTime = "";
                                switch (timetype) {
                                    case "hour":
                                        monitorTime = DataFormatUtil.getDateYMDH(monitorTimeInfo);
                                        break;
                                    case "day":
                                        monitorTime = DataFormatUtil.getDateYMD(monitorTimeInfo);
                                        break;
                                }
                                Object FlowValue = document.get("FlowValue");
                                if (FlowValue!=null&&!"".equals(FlowValue.toString())){
                                    FlowValue = DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(FlowValue.toString()));
                                }
                                dataMap.put("monitortime", monitorTime);
                                dataMap.put("monitorvalue", FlowValue.toString());
                                monitordatalist.add(dataMap);

                            }

                            //排序
                            monitordatalist = monitordatalist.stream().sorted(
                                    Comparator.comparing((Map m) -> m.get("monitortime").toString())).collect(Collectors.toList());
                            pollutantMapInfo.put("pollutantcode", pollutantcode);
                            pollutantMapInfo.put("monitordatalist", monitordatalist);
                            pollutantList.add(pollutantMapInfo);
                        }
                    }
                    mnMap.put("mn", mn);
                    mnMap.put("pollutantdatalist", pollutantList);
                    resultList.add(mnMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取近一年月排放量、浓度，同比环比数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/3/19 9:11
     */
    @RequestMapping(value = "getMonthFlowAndConDataByParam", method = RequestMethod.POST)
    public Object getMonthFlowAndConDataByParam(@RequestJson(value = "pollutantcode") String pollutantCode,
                                                @RequestJson(value = "monitorpointtype") Integer monitorPointType) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            //获取当前排放量、浓度数据
            String nowDay = DataFormatUtil.getDateYMD(new Date());
            String thisStartTime = DataFormatUtil.getBeforeByYearTime(1, nowDay);
            String thisEndTime = nowDay;
            Map<String, Map<String, Object>> thisDataMap = onlineService.countDischargeAndDensityByCodeAndMns(pollutantCode,
                    thisStartTime, thisEndTime, "", monitorPointType);

            //获取同比排放量、浓度数据
            String thatStartTime = DataFormatUtil.getMonthTBDate(thisStartTime);
            String thatEndTime = DataFormatUtil.getMonthTBDate(thisEndTime);
            Map<String, Map<String, Object>> thatDataMap = onlineService.countDischargeAndDensityByCodeAndMns(pollutantCode,
                    thatStartTime, thatEndTime, "", monitorPointType);
            thisStartTime = DataFormatUtil.FormatDateOneToOther(thisStartTime, "yyyy-MM-dd", "yyyy-MM");
            thisEndTime = DataFormatUtil.FormatDateOneToOther(thisEndTime, "yyyy-MM-dd", "yyyy-MM");
            List<String> yms = DataFormatUtil.getMonthBetween(thisStartTime, thisEndTime);
            yms.remove(thisStartTime);
            String tbYM;
            for (String ym : yms) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitortime", ym);
                tbYM = DataFormatUtil.FormatDateOneToOther(DataFormatUtil.getMonthTBDate(ym + "-01"), "yyyy-MM-dd", "yyyy-MM");
                resultMap.put("thisdata", thisDataMap.get(ym));
                resultMap.put("thatdata", thatDataMap.get(tbYM));
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/28 11:59
     * @Description: 小时和天数据获取排放量列表数据获取
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "getConcentrationAndFlowDataByParam", method = RequestMethod.POST)
    public Object getConcentrationAndFlowDataByParam(@RequestJson(value = "mns", required = false) Object mndata,
                                                     @RequestJson(value = "pollutantcodes", required = false) Object pollutants,
                                                     @RequestJson(value = "pagesize", required = false) Integer pageSize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pageNum,
                                                     @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                     @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                     @RequestJson(value = "outputids", required = false) List<String> outputids,
                                                     @RequestJson(value = "timetype") String timeType,
                                                     @RequestJson(value = "flag") String flag,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> mnData = (List<Map<String, Object>>) mndata;
            List<Map<String, Object>> pollutantsInfo = (List<Map<String, Object>>) pollutants;
            Map<String, Object> resultMap = new HashMap<>();
            if (monitorpointtypes==null||monitorpointtypes.size()==0){
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype!=null){
                    monitorpointtypes.add(monitorpointtype);
                }
            }
            if (mnData.size() > 0 && pollutantsInfo.size() > 0) {
                Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData = new HashMap<>();
                if (monitorpointtypes != null && outputids != null) {
                    for (Integer i : monitorpointtypes) {
                        mnAndCodeAndStandardData.putAll(pollutantService.getMnAndCodeAndStandardData(i, outputids));
                    }
                }
                List<Map<String, Object>> tableTitle = getTableTitle(flag, pollutantsInfo, mnData, timeType);
                Integer ishasconvertdata;
                Map<String, Integer> codeAndIs = new HashMap<>();
                for (Map<String, Object> pollutant : pollutantsInfo) {
                    if (pollutant.get("pollutantcode") != null) {
                        ishasconvertdata = pollutant.get("ishasconvertdata") != null ? Integer.parseInt(pollutant.get("ishasconvertdata").toString()) : 0;
                        codeAndIs.put(pollutant.get("pollutantcode").toString(), ishasconvertdata);
                    }

                }

                Map<String, Object> tabledata = new HashMap<>();
                tabledata.put("tabletitledata", tableTitle);
                List<String> mns = mnData.stream().map(mnDatum -> mnDatum.get("mn").toString()).collect(Collectors.toList());
                List<String> pollutantCodes = pollutantsInfo.stream().map(mnDatum -> mnDatum.get("pollutantcode").toString()).collect(Collectors.toList());
                String collection;
                String ND_Collection;
                String unwindFieldName;
                String ND_UnwindFieldName;
                Date startTime;
                Date endTime;
                for (Map<String, Object> map : pollutantsInfo) {
                    map.putIfAbsent("isshowflow", 0);
                }
                switch (timeType) {
                    case "hour":
                        ND_Collection = "HourData";
                        collection = "HourFlowData";
                        ND_UnwindFieldName = "HourDataList";
                        unwindFieldName = "HourFlowDataList";
                        startTime = DataFormatUtil.parseDate(starttime + ":00:00");
                        endTime = DataFormatUtil.parseDate(endtime + ":59:59");
                        break;
                    case "day":
                        startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
                        endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
                        collection = "DayFlowData";
                        ND_Collection = "DayData";
                        ND_UnwindFieldName = "DayDataList";
                        unwindFieldName = "DayFlowDataList";
                        break;
                    default:
                        return AuthUtil.parseJsonKeyToLower("success", resultMap);
                }
                final String valueFiledName = "CorrectedFlow";
                final String ND_valueFiledName = "AvgStrength";
                final String ZSND_valueFiledName = "AvgConvertStrength";
                int total = 0;
                if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
                    total = onlineFlowDataService.countNDOrPFLDataByParam(mns, pollutantCodes, startTime, endTime, ND_UnwindFieldName, ND_Collection);
                }
                List<Document> ND_Document = onlineFlowDataService.getNDOrPFLDataByParam(ND_valueFiledName, mns, pollutantCodes, startTime, endTime, ND_UnwindFieldName, ND_Collection, pageSize, pageNum);
                List<String> PFL_mns = new ArrayList<>();
                List<Map<String, Object>> tablelistdata = new ArrayList<>();
                if (ND_Document.size() > 0) {


                    Map<String, Object> flag_codeAndName = new HashMap<>();
                    if (ND_Document.size() > 0) {
                        Map<String,Object> f_map = new HashMap<>();
                        f_map.put("monitorpointtypes",monitorpointtypes);
                        List<Map<String, Object>> flagList = pollutantService.getFlagListByParam(f_map);
                        String flag_code;
                        for (Map<String, Object> map : flagList) {
                            if (map.get("code") != null) {
                                flag_code = map.get("code").toString();
                                flag_codeAndName.put(flag_code, map.get("name"));
                            }
                        }
                    }


                    startTime = ND_Document.get(ND_Document.size() - 1).getDate("MonitorTime");
                    endTime = ND_Document.get(0).getDate("MonitorTime");

                    String isOver;
                    Object dataFlag;
                    String isException;
                    String isSuddenChange;
                    String pollutantCode;
                    String SCND;
                    String ZSND;
                    String standardData;
                    boolean isOverStandard;
                    for (Document document : ND_Document) {
                        String dataGatherCode = document.getString("DataGatherCode");
                        if (!PFL_mns.contains(dataGatherCode)) {
                            PFL_mns.add(dataGatherCode);
                        }
                        Date date = document.getDate("MonitorTime");
                        String monitorTime = "";
                        if (timeType.equals("day")) {
                            monitorTime = DataFormatUtil.getDateYMD(date);
                        } else if (timeType.equals("hour")) {
                            monitorTime = DataFormatUtil.getDateYMDH(date);
                        }
                        String pollutionname = null;
                        String outputname = null;
                        String outputid = null;
                        for (Map<String, Object> mnDatum : mnData) {
                            if (mnDatum.get("mn").equals(dataGatherCode)) {
                                outputname = mnDatum.get("outname") != null && !mnDatum.get("outname").equals("") ? mnDatum.get("outname").toString() : null;
                                pollutionname = mnDatum.get("pollutionname") != null && !mnDatum.get("pollutionname").equals("") ? mnDatum.get("pollutionname").toString() : null;
                                outputid = mnDatum.get("outputid") != null && !mnDatum.get("outputid").equals("") ? mnDatum.get("outputid").toString() : null;
                                break;
                            }
                        }
                        Map<String, Object> each = new HashMap<>();
                        if (StringUtils.isNotBlank(pollutionname)) {
                            each.put("pollutionname", pollutionname);
                        }
                        if (StringUtils.isNotBlank(outputname)) {
                            each.put("jcdname", outputname);
                        }
                        if (StringUtils.isNotBlank(outputid)) {
                            each.put("outputid", outputid);
                        }
                        each.put("mn", dataGatherCode);
                        each.put("monitortime", monitorTime);
                        List<Document> pollutantDataList = document.get(ND_UnwindFieldName, List.class);
                        for (Document pollutant : pollutantDataList) {
                            isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";

                            dataFlag = flag_codeAndName.get(pollutant.get("Flag")!=null?pollutant.get("Flag").toString().toLowerCase():"");


                            isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                            isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                            isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                            if (isOverStandard) {
                                isOver = "4";
                            }
                            if (!"true".equals(isSuddenChange)) {
                                isSuddenChange = "false";
                            }
                            pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                            if (pollutantCode != null) {
                                if (mnAndCodeAndStandardData.containsKey(dataGatherCode) && mnAndCodeAndStandardData.get(dataGatherCode).get(pollutantCode) != null
                                        && mnAndCodeAndStandardData.get(dataGatherCode).get(pollutantCode).get(isOver) != null) {
                                    standardData = mnAndCodeAndStandardData.get(dataGatherCode).get(pollutantCode).get(isOver) + "";
                                } else {
                                    standardData = "-";
                                }
                                if (codeAndIs.get(pollutantCode) != null && codeAndIs.get(pollutantCode) > 0) {
                                    ZSND = pollutantCode + "_zsnd" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                    +"#"+dataFlag;
                                    Object value = pollutant.get(ZSND_valueFiledName);
                                    each.put(ZSND, value);
                                }
                                SCND = pollutantCode + "_nd" + "#" + isOver + "#" + isException + "#" + isSuddenChange + "#" + standardData
                                        +"#"+dataFlag;
                                Object value = pollutant.get(ND_valueFiledName);
                                each.put(SCND, value);
                            }

                        }
                        tablelistdata.add(each);
                    }
                    List<String> PFL_Pollutants = pollutantsInfo.stream().filter(mnDatum -> mnDatum.get("isshowflow").toString().equals("1")).map(mnDatum -> mnDatum.get("pollutantcode").toString()).collect(Collectors.toList());
                    //以浓度为基准去查询排放量
                    pageNum = 1;
                    List<Document> PFL_Document = onlineFlowDataService.getNDOrPFLDataByParam(valueFiledName, PFL_mns, PFL_Pollutants, startTime, endTime, unwindFieldName, collection, pageSize, pageNum);
                    for (Document document : PFL_Document) {
                        String dataGatherCode = document.getString("DataGatherCode");
                        Date date = document.getDate("MonitorTime");
                        String monitorTime = "";
                        if (timeType.equals("day")) {
                            monitorTime = DataFormatUtil.getDateYMD(date);
                        } else if (timeType.equals("hour")) {
                            monitorTime = DataFormatUtil.getDateYMDH(date);
                        }
                        for (Map<String, Object> map : tablelistdata) {
                            if (map.get("mn").equals(dataGatherCode) && map.get("monitortime").equals(monitorTime)) {
                                List<Document> pollutantDataList = document.get(unwindFieldName, List.class);
                                for (Document pollutant : pollutantDataList) {
                                    isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                                    isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                                    isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                                    isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                                    if (isOverStandard) {
                                        isOver = "4";
                                    }
                                    if (!"true".equals(isSuddenChange)) {
                                        isSuddenChange = "false";
                                    }
                                    pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                                    if (pollutantCode != null) {
                                        pollutantCode = pollutantCode + "_pfl" + "#" + isOver + "#" + isException + "#" + isSuddenChange;
                                        Object value = pollutant.get(valueFiledName);
                                        map.put(pollutantCode + "_pfl", value);
                                    }
                                }
                            }
                        }
                    }
                }
                //排序
                tabledata.put("tablelistdata", tablelistdata);
                resultMap.put("tabledata", tabledata);
                resultMap.put("total", total);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/9/29 0029 下午 6:36
     * @Description: 导出浓度排放量复合表头列表文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "exportConcentrationAndFlowDataByParam", method = RequestMethod.POST)
    public void exportConcentrationAndFlowDataByParam(@RequestJson(value = "mns", required = false) Object mndata,
                                                      @RequestJson(value = "pollutantcodes", required = false) Object pollutants,
                                                      @RequestJson(value = "timetype") String timeType,
                                                      @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                      @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                      @RequestJson(value = "flag") String flag,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      HttpServletResponse response,
                                                      HttpServletRequest request) throws IOException {
        try {
            List<Map<String, Object>> mnData = (List<Map<String, Object>>) mndata;
            List<Map<String, Object>> pollutantsInfo = (List<Map<String, Object>>) pollutants;
            if (monitorpointtypes==null||monitorpointtypes.size()==0){
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype!=null){
                    monitorpointtypes.add(monitorpointtype);
                }
            }
            if (mnData.size() > 0 && pollutantsInfo.size() > 0) {
                List<Map<String, Object>> tableTitle = getTableTitle(flag, pollutantsInfo, mnData, timeType);
                Integer ishasconvertdata;
                Map<String, Integer> codeAndIs = new HashMap<>();
                for (Map<String, Object> pollutant : pollutantsInfo) {
                    if (pollutant.get("pollutantcode") != null) {
                        ishasconvertdata = pollutant.get("ishasconvertdata") != null ? Integer.parseInt(pollutant.get("ishasconvertdata").toString()) : 0;
                        codeAndIs.put(pollutant.get("pollutantcode").toString(), ishasconvertdata);
                    }

                }

                Map<String, Object> tabledata = new HashMap<>();
                tabledata.put("tabletitledata", tableTitle);
                List<String> mns = mnData.stream().map(mnDatum -> mnDatum.get("mn").toString()).collect(Collectors.toList());
                List<String> pollutantCodes = pollutantsInfo.stream().map(mnDatum -> mnDatum.get("pollutantcode").toString()).collect(Collectors.toList());
                String collection;
                String ND_Collection;
                String unwindFieldName;
                String ND_UnwindFieldName;
                Date startTime;
                Date endTime;
                for (Map<String, Object> map : pollutantsInfo) {
                    map.putIfAbsent("isshowflow", 0);
                }
                switch (timeType) {
                    case "hour":
                        ND_Collection = "HourData";
                        collection = "HourFlowData";
                        ND_UnwindFieldName = "HourDataList";
                        unwindFieldName = "HourFlowDataList";
                        startTime = DataFormatUtil.parseDate(starttime + ":00:00");
                        endTime = DataFormatUtil.parseDate(endtime + ":59:59");
                        break;
                    case "day":
                        startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
                        endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
                        collection = "DayFlowData";
                        ND_Collection = "DayData";
                        ND_UnwindFieldName = "DayDataList";
                        unwindFieldName = "DayFlowDataList";
                        break;
                    default:
                        collection = "";
                        ND_Collection = "";
                        ND_UnwindFieldName = "";
                        unwindFieldName = "";
                        startTime = null;
                        endTime = null;
                        break;
                }
                final String valueFiledName = "CorrectedFlow";
                final String ND_valueFiledName = "AvgStrength";
                final String ZSND_valueFiledName = "AvgConvertStrength";

                List<Document> ND_Document = onlineFlowDataService.getNDOrPFLDataByParam(ND_valueFiledName, mns, pollutantCodes, startTime, endTime, ND_UnwindFieldName, ND_Collection, -1, -1);
                List<String> PFL_mns = new ArrayList<>();
                List<Map<String, Object>> tablelistdata = new ArrayList<>();
                if (ND_Document.size() > 0) {
                    startTime = ND_Document.get(ND_Document.size() - 1).getDate("MonitorTime");
                    endTime = ND_Document.get(0).getDate("MonitorTime");

                    Map<String, Object> flag_codeAndName = new HashMap<>();
                    if (ND_Document.size() > 0) {
                        //获取mongodb的flag标记
                        Map<String,Object> f_map = new HashMap<>();
                        f_map.put("monitorpointtypes",monitorpointtypes);
                        List<Map<String, Object>> flagList = pollutantService.getFlagListByParam(f_map);
                        String flag_code;
                        for (Map<String, Object> map : flagList) {
                            if (map.get("code") != null) {
                                flag_code = map.get("code").toString();
                                flag_codeAndName.put(flag_code, map.get("name"));
                            }
                        }
                    }


                    String isOver;
                    Object dataFlag;
                    String isException;
                    String isSuddenChange;
                    String pollutantCode;
                    String SCND;
                    String ZSND;
                    String standardData;
                    boolean isOverStandard;
                    for (Document document : ND_Document) {
                        String dataGatherCode = document.getString("DataGatherCode");
                        if (!PFL_mns.contains(dataGatherCode)) {
                            PFL_mns.add(dataGatherCode);
                        }
                        Date date = document.getDate("MonitorTime");
                        String monitorTime = "";
                        if (timeType.equals("day")) {
                            monitorTime = DataFormatUtil.getDateYMD(date);
                        } else if (timeType.equals("hour")) {
                            monitorTime = DataFormatUtil.getDateYMDH(date);
                        }
                        String pollutionname = null;
                        String outputname = null;
                        String outputid = null;
                        for (Map<String, Object> mnDatum : mnData) {
                            if (mnDatum.get("mn").equals(dataGatherCode)) {
                                outputname = mnDatum.get("outname") != null && !mnDatum.get("outname").equals("") ? mnDatum.get("outname").toString() : null;
                                pollutionname = mnDatum.get("pollutionname") != null && !mnDatum.get("pollutionname").equals("") ? mnDatum.get("pollutionname").toString() : null;
                                outputid = mnDatum.get("outputid") != null && !mnDatum.get("outputid").equals("") ? mnDatum.get("outputid").toString() : null;
                                break;
                            }
                        }
                        Map<String, Object> each = new HashMap<>();
                        if (StringUtils.isNotBlank(pollutionname)) {
                            each.put("pollutionname", pollutionname);
                        }
                        if (StringUtils.isNotBlank(outputname)) {
                            each.put("jcdname", outputname);
                        }
                        if (StringUtils.isNotBlank(outputid)) {
                            each.put("outputid", outputid);
                        }
                        each.put("mn", dataGatherCode);
                        each.put("monitortime", monitorTime);
                        List<Document> pollutantDataList = document.get(ND_UnwindFieldName, List.class);
                        for (Document pollutant : pollutantDataList) {

                            dataFlag = flag_codeAndName.get(pollutant.get("Flag")!=null?pollutant.get("Flag").toString().toLowerCase():"");
                            isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                            isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                            isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                            isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                            if (isOverStandard) {
                                isOver = "4";
                            }
                            if (!"true".equals(isSuddenChange)) {
                                isSuddenChange = "false";
                            }
                            pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                            if (pollutantCode != null) {

                                if (codeAndIs.get(pollutantCode) != null && codeAndIs.get(pollutantCode) > 0) {
                                    ZSND = pollutantCode + "_zsnd" + "#" + isOver + "#" + isException + "#" + isSuddenChange+"#-#"+dataFlag ;
                                    Object value = pollutant.get(ZSND_valueFiledName);
                                    each.put(ZSND, value);
                                }
                                SCND = pollutantCode + "_nd" + "#" + isOver + "#" + isException + "#" + isSuddenChange+"#-#"+dataFlag ;
                                Object value = pollutant.get(ND_valueFiledName);
                                each.put(SCND, value);
                            }

                        }
                        tablelistdata.add(each);
                    }
                    List<String> PFL_Pollutants = pollutantsInfo.stream().filter(mnDatum -> mnDatum.get("isshowflow").toString().equals("1")).map(mnDatum -> mnDatum.get("pollutantcode").toString()).collect(Collectors.toList());
                    //以浓度为基准去查询排放量
                    List<Document> PFL_Document = onlineFlowDataService.getNDOrPFLDataByParam(valueFiledName, PFL_mns, PFL_Pollutants, startTime, endTime, unwindFieldName, collection, -1, -1);
                    for (Document document : PFL_Document) {
                        String dataGatherCode = document.getString("DataGatherCode");
                        Date date = document.getDate("MonitorTime");
                        String monitorTime = "";
                        if (timeType.equals("day")) {
                            monitorTime = DataFormatUtil.getDateYMD(date);
                        } else if (timeType.equals("hour")) {
                            monitorTime = DataFormatUtil.getDateYMDH(date);
                        }
                        for (Map<String, Object> map : tablelistdata) {
                            if (map.get("mn").equals(dataGatherCode) && map.get("monitortime").equals(monitorTime)) {
                                List<Document> pollutantDataList = document.get(unwindFieldName, List.class);
                                for (Document pollutant : pollutantDataList) {
                                    isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                                    isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                                    isSuddenChange = pollutant.get("IsSuddenChange") != null ? pollutant.get("IsSuddenChange").toString() : "false";
                                    isOverStandard = pollutant.get("IsOverStandard") != null ? pollutant.getBoolean("IsOverStandard") : false;
                                    if (isOverStandard) {
                                        isOver = "4";
                                    }
                                    if (!"true".equals(isSuddenChange)) {
                                        isSuddenChange = "false";
                                    }
                                    pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.get("PollutantCode").toString() : null;
                                    if (pollutantCode != null) {
                                        pollutantCode = pollutantCode + "_pfl" + "#" + isOver + "#" + isException + "#" + isSuddenChange;
                                        Object value = pollutant.get(valueFiledName);
                                        map.put(pollutantCode + "_pfl", value);
                                    }
                                }
                            }
                        }
                    }
                }
                List<Map<String, Object>> headers = FormatUtils.setManyHeaderExportData(tableTitle);
                String preName = "";
                if (monitorpointtypes.size()>0) {
                    if (monitorpointtypes.size()==1) {
                        if (CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() == monitorpointtypes.get(0)) {
                            preName = "废水";
                        } else if (CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() == monitorpointtypes.get(0)) {
                            preName = "废气";
                        } else if (CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() == monitorpointtypes.get(0)) {
                            preName = "雨水";
                        } else if (CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() == monitorpointtypes.get(0)) {
                            preName = "烟气";
                        }
                    }else{
                        preName = "废气";
                    }
                }
                String fileName = preName + "监测数据导出文件_" + new Date().getTime();
                ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headers, tablelistdata, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @Description: 验证是否可以导出
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/8 15:03
     */

    @RequestMapping(value = "isExportConcentrationAndFlowDataByParam", method = RequestMethod.POST)
    public Object isExportConcentrationAndFlowDataByParam(@RequestJson(value = "mns", required = false) Object mndata,
                                                      @RequestJson(value = "pollutantcodes", required = false) Object pollutants,
                                                      @RequestJson(value = "timetype") String timeType,
                                                      @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                      @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime
                                                      ) throws IOException {
        try {
            List<Map<String, Object>> mnData = (List<Map<String, Object>>) mndata;
            List<Map<String, Object>> pollutantsInfo = (List<Map<String, Object>>) pollutants;
            if (monitorpointtypes==null||monitorpointtypes.size()==0){
                monitorpointtypes = new ArrayList<>();
                if (monitorpointtype!=null){
                    monitorpointtypes.add(monitorpointtype);
                }
            }
            if (mnData.size() > 0 && pollutantsInfo.size() > 0) {
                Integer ishasconvertdata;
                Map<String, Integer> codeAndIs = new HashMap<>();
                for (Map<String, Object> pollutant : pollutantsInfo) {
                    if (pollutant.get("pollutantcode") != null) {
                        ishasconvertdata = pollutant.get("ishasconvertdata") != null ? Integer.parseInt(pollutant.get("ishasconvertdata").toString()) : 0;
                        codeAndIs.put(pollutant.get("pollutantcode").toString(), ishasconvertdata);
                    }

                }
                List<String> mns = mnData.stream().map(mnDatum -> mnDatum.get("mn").toString()).collect(Collectors.toList());
                List<String> pollutantCodes = pollutantsInfo.stream().map(mnDatum -> mnDatum.get("pollutantcode").toString()).collect(Collectors.toList());
                String ND_Collection;
                Date startTime;
                Date endTime;
                for (Map<String, Object> map : pollutantsInfo) {
                    map.putIfAbsent("isshowflow", 0);
                }
                switch (timeType) {
                    case "hour":
                        ND_Collection = "HourData";
                        startTime = DataFormatUtil.parseDate(starttime + ":00:00");
                        endTime = DataFormatUtil.parseDate(endtime + ":59:59");
                        break;
                    case "day":
                        startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
                        endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
                        ND_Collection = "DayData";
                        break;
                    default:
                        ND_Collection = "";
                        startTime = null;
                        endTime = null;
                        break;
                }


                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("collection",ND_Collection);
                paramMap.put("starttime",startTime);
                paramMap.put("endtime",endTime);
                paramMap.put("pollutantcodes",pollutantCodes);
                paramMap.put("mns",mns);
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.exportDataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
            }
            return AuthUtil.returnObject(ReturnInfo.success, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    /**
     * @author: lip
     * @date: 2019/9/29 0029 下午 6:36
     * @Description: 导出浓度排放量复合表头列表文件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportConcentrationAndFlowDataByParam1", method = RequestMethod.POST)
    public void exportConcentrationAndFlowDataByParam1(@RequestJson(value = "mns", required = false) Object mndata,
                                                       @RequestJson(value = "pollutantcodes", required = false) Object pollutants,
                                                       @RequestJson(value = "timetype") String timeType,
                                                       @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                       @RequestJson(value = "flag") String flag,
                                                       @RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "endtime") String endtime,
                                                       HttpServletResponse response,
                                                       HttpServletRequest request) throws IOException {
        try {


            Map<String, Object> paramMap = new HashMap<>();
            monitorpointtype = 37;
            paramMap.put("outputids", Arrays.asList());
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> mnData = onlineService.getMonitorPointDataByParam(paramMap);

            Map<String, Object> idAndName = new HashMap<>();
            Map<String, Object> idAndPollutionName = new HashMap<>();

            for (Map<String, Object> mn : mnData) {
                idAndName.put(mn.get("pk_id").toString(), mn.get("monitorpointname"));
                idAndPollutionName.put(mn.get("pk_id").toString(), mn.get("pollutionname"));
            }


            paramMap.put("pollutanttype", monitorpointtype);

            List<Map<String, Object>> pollutantsInfo = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);
            List<String> codes = new ArrayList<>();

            for (Map<String, Object> pollutant : pollutantsInfo) {
                if (!codes.contains(pollutant.get("pollutantcode"))) {
                    codes.add(pollutant.get("pollutantcode").toString());
                }
            }

            List<Map<String, Object>> tableTitle = getTableTitleTest(flag, pollutantsInfo, mnData);

            List<Map<String, Object>> headers = FormatUtils.setManyHeaderExportData(tableTitle);

            List<Map<String, Object>> tablelistdata = new ArrayList<>();
            List<Map<String, Object>> dataList = onlineService.getOutPutsAndPollutantAlarmSetByParam(paramMap);
            String idAndCode;
            Map<String, Object> markAndStandValue = new HashMap<>();
            Map<String, Object> markAndAlarmValue = new HashMap<>();
            String standardmaxvalue;
            String standardminvalue;
            String concenalarmminvalue;
            String concenalarmmaxvalue;
            for (Map<String, Object> data : dataList) {
                idAndCode = data.get("gasoutputid") + "#" + data.get("pollutantcode");


                if (data.get("standardminvalue") != null && data.get("standardmaxvalue") != null) {

                    standardmaxvalue = data.get("standardmaxvalue").toString();
                    standardmaxvalue = DataFormatUtil.subZeroAndDot(standardmaxvalue);


                    standardminvalue = data.get("standardminvalue").toString();
                    standardminvalue = DataFormatUtil.subZeroAndDot(standardminvalue);
                    if (!standardminvalue.equals("0")) {
                        markAndStandValue.put(idAndCode, standardminvalue + "-" + standardmaxvalue);
                    } else {
                        markAndStandValue.put(idAndCode, standardmaxvalue);
                    }

                } else if (data.get("standardmaxvalue") != null) {
                    standardmaxvalue = data.get("standardmaxvalue").toString();
                    standardmaxvalue = DataFormatUtil.subZeroAndDot(standardmaxvalue);
                    markAndStandValue.put(idAndCode, standardmaxvalue);
                }

                if (data.get("concenalarmminvalue") != null && data.get("concenalarmmaxvalue") != null) {
                    concenalarmmaxvalue = data.get("concenalarmmaxvalue").toString();
                    concenalarmmaxvalue = DataFormatUtil.subZeroAndDot(concenalarmmaxvalue);

                    concenalarmminvalue = data.get("concenalarmminvalue").toString();
                    concenalarmminvalue = DataFormatUtil.subZeroAndDot(concenalarmminvalue);
                    if (!concenalarmminvalue.equals("0")) {
                        markAndAlarmValue.put(idAndCode, concenalarmminvalue + "-" + concenalarmmaxvalue);
                    } else {
                        markAndAlarmValue.put(idAndCode, concenalarmmaxvalue);
                    }


                } else if (data.get("concenalarmmaxvalue") != null) {
                    concenalarmmaxvalue = data.get("concenalarmmaxvalue").toString();
                    concenalarmmaxvalue = DataFormatUtil.subZeroAndDot(concenalarmmaxvalue);
                    markAndAlarmValue.put(idAndCode, concenalarmmaxvalue);
                }

            }
            for (String id : idAndName.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("pollutionname", idAndPollutionName.get(id));
                resultMap.put("jcdname", idAndName.get(id));
                for (String code : codes) {
                    idAndCode = id + "#" + code;
                    resultMap.put(code + "_nd", markAndStandValue.get(idAndCode));
                    resultMap.put(code + "_pfl", markAndAlarmValue.get(idAndCode));
                }
                tablelistdata.add(resultMap);
            }
            System.out.println();
            String fileName = "监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headers, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/9/3 8:41
     * @Description: 表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getTableTitle(String flag, List<Map<String, Object>> pollutantsInfo, List<Map<String, Object>> mnData, String timeType) {
        // List<Map<String, Object>> pollutants = pollutantsInfo.stream().filter(m -> m.get("isshowflow") != null).sorted(Comparator.comparing(m -> m.get("isshowflow").toString())).collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<>();
        String flowunit = "";
        if ("hour".equals(timeType)) {//时
            flowunit = "千克.时";
        } else if ("day".equals(timeType)) {//日报
            flowunit = "千克.日";
        }
        if (flag.equals("many")) {
            Map<String, Object> map = mnData.get(0);
            Object pollutionname = map.get("pollutionname");
            if (pollutionname != null && !pollutionname.equals("")) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("headeralign", "center");
                map1.put("minwidth", "180px");
                map1.put("showhide", true);
                map1.put("prop", "pollutionname");
                map1.put("label", "企业名称");
                map1.put("align", "center");
                result.add(map1);
            }
            Map<String, Object> jcd = new HashMap<>();
            jcd.put("headeralign", "center");
            jcd.put("minwidth", "180px");
            jcd.put("showhide", true);
            jcd.put("prop", "jcdname");
            jcd.put("label", "监测点名称");
            jcd.put("align", "center");
            result.add(jcd);
        }
        Map<String, Object> monitortime = new HashMap<>();
        monitortime.put("headeralign", "center");
        monitortime.put("minwidth", "180px");
        monitortime.put("showhide", true);
        monitortime.put("prop", "monitortime");
        monitortime.put("label", "监测时间");
        monitortime.put("align", "center");
        result.add(monitortime);

        Integer isHasConvertData;

        for (Map<String, Object> pollutantMapInfo : pollutantsInfo) {
            Map<String, Object> pollutantMap = new HashMap<>();
            pollutantMap.put("headeralign", "center");
            pollutantMap.put("showhide", true);
            pollutantMap.put("align", "center");
            String pollutantname = pollutantMapInfo.get("pollutantname").toString();
            Object isshowflow = pollutantMapInfo.get("isshowflow");

            isHasConvertData = pollutantMapInfo.get("ishasconvertdata") != null ? Integer.parseInt(pollutantMapInfo.get("ishasconvertdata").toString()) : 0;

            pollutantMap.put("isshowflow", isshowflow);
            if (isshowflow != null && isshowflow.toString().equals("1")) {
                pollutantMap.put("label", pollutantname);
            } else {
                if (pollutantMapInfo.get("pollutantunit") != null) {
                    pollutantname = pollutantname + "(" + pollutantMapInfo.get("pollutantunit") + ")";
                }
                pollutantMap.put("label", pollutantname);
            }
            String pllutantcode = pollutantMapInfo.get("pollutantcode").toString();
            if (isshowflow != null && isshowflow.toString().equals("1")) {
                //显示排放量  则单位不和污染物一列

                List<Map<String, Object>> children = new ArrayList<>();
                Map<String, Object> nd = getTableMap(pllutantcode + "_nd", "实测值" + "(" + pollutantMapInfo.get("pollutantunit") + ")");
                Map<String, Object> pfl = getTableMap(pllutantcode + "_pfl", "排放量" + "(" + flowunit + ")");

                children.add(nd);
                if (isHasConvertData > 0) {
                    Map<String, Object> zs = getTableMap(pllutantcode + "_zsnd", "折算值" + "(" + pollutantMapInfo.get("pollutantunit") + ")");
                    children.add(zs);
                }

                children.add(pfl);
                pollutantMap.put("children", children);
                pollutantMap.put("prop", pllutantcode + "_par");
            } else {
                if (isHasConvertData > 0) {
                    List<Map<String, Object>> children = new ArrayList<>();
                    Map<String, Object> nd = getTableMap(pllutantcode + "_nd", "实测值" + "(" + pollutantMapInfo.get("pollutantunit") + ")");
                    children.add(nd);
                    Map<String, Object> zs = getTableMap(pllutantcode + "_zsnd", "折算值" + "(" + pollutantMapInfo.get("pollutantunit") + ")");
                    children.add(zs);
                    pollutantMap.put("children", children);
                    pollutantMap.put("prop", pllutantcode + "_par");
                } else {
                    pollutantMap.put("prop", pllutantcode + "_nd");
                }
            }
            result.add(pollutantMap);
        }
        return result;
    }


    private List<Map<String, Object>> getTableTitleTest(String flag, List<Map<String, Object>> pollutantsInfo, List<Map<String, Object>> mnData) {

        List<Map<String, Object>> result = new ArrayList<>();
        if (flag.equals("many")) {
            Map<String, Object> map = mnData.get(0);
            Object pollutionname = map.get("pollutionname");
            if (pollutionname != null && !pollutionname.equals("")) {
                Map<String, Object> map1 = getTableMap("pollutionname", "企业名称");
                result.add(map1);
            }
            Map<String, Object> jcd = getTableMap("jcdname", "排口名称");
            result.add(jcd);
        }

        for (Map<String, Object> pollutantMapInfo : pollutantsInfo) {
            Map<String, Object> pollutantMap = new HashMap<>();
            pollutantMap.put("headeralign", "center");
            pollutantMap.put("showhide", true);
            pollutantMap.put("align", "center");
            String pollutantname = pollutantMapInfo.get("pollutantname").toString();
            if (pollutantMapInfo.get("pollutantunit") != null) {
                pollutantname = pollutantname + "(" + pollutantMapInfo.get("pollutantunit") + ")";
            }
            pollutantMap.put("label", pollutantname);
            Object isshowflow = pollutantMapInfo.get("isshowflow");
            pollutantMap.put("isshowflow", isshowflow);
            String pllutantcode = pollutantMapInfo.get("pollutantcode").toString();

            List<Map<String, Object>> children = new ArrayList<>();
            Map<String, Object> nd = getTableMap(pllutantcode + "_nd", "标准值");
            Map<String, Object> pfl = getTableMap(pllutantcode + "_pfl", "预警值");
            children.add(nd);
            children.add(pfl);
            pollutantMap.put("children", children);
            pollutantMap.put("prop", pllutantcode + "_par");

            result.add(pollutantMap);
        }
        return result;
    }

    private Map<String, Object> getTableMap(String prop, String label) {
        Map<String, Object> map = new HashMap<>();
        map.put("headeralign", "center");
        map.put("showhide", true);
        map.put("prop", prop);
        map.put("label", label);
        map.put("align", "center");
        return map;
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 上午 10:56
     * @Description: 获取企业（废水、废气、重金属）排污数据（日排放，月排放，年排放）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutionFlowDataByParam", method = RequestMethod.POST)
    public Object getPollutionFlowDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> resultList = getPollutionFlowDataList(monitorpointtype, pollutantcodes, timetype, starttime, endtime);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/3/4 0004 上午 10:56
     * @Description: 导出企业（废水、废气、重金属）排污数据（日排放，月排放，年排放）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPollutionFlowDataByParam", method = RequestMethod.POST)
    public void exportPollutionFlowDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Map<String, Object>> resultList = getPollutionFlowDataList(monitorpointtype, pollutantcodes, timetype, starttime, endtime);
            //设置文件名称
            String firstName = "";
            switch (timetype) {
                case "day":
                    firstName = "日";
                    break;
                case "month":
                    firstName = "月";
                    break;
                case "year":
                    firstName = "年";
                    break;
                default:
                    break;
            }
            String fileName = "企业" + firstName + "排放量导出文件_" + new Date().getTime();
            List<String> headers = Arrays.asList("企业名称", "污染物名称", "统计时段", "当前时段排放量(t)",
                    "同比变化(%)", "环比变化(%)", "同比时段排放量(t)", "环比时段排放量(t)", "占比(%)");
            List<String> headersField = Arrays.asList("pollutionname", "pollutantname", "countdate", "currentflow",
                    "tb_change", "hb_change", "tb_flow", "hb_flow", "proportion");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 下午 4:47
     * @Description: 获取企业排放量数据列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPollutionFlowDataList(Integer monitorpointtype, List<String> pollutantcodes, String timetype, String starttime, String endtime) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //1,获取企业列表
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> pollutionDataList = pollutionService.getPollutionNameAndPkid(paramMap);
        if (pollutionDataList.size() > 0) {
            String pollutionId;
            Map<String, Object> pollutionIdAndName = new HashMap<>();
            for (Map<String, Object> pollutionData : pollutionDataList) {
                pollutionId = pollutionData.get("pollutionid").toString();
                pollutionIdAndName.put(pollutionId, pollutionData.get("pollutionname"));
            }
            paramMap.put("outputids", Arrays.asList());
            paramMap.put("monitorpointtype", monitorpointtype);
            //2,根据监测类型获取mn号集合
            List<Map<String, Object>> outPutDataList = onlineService.getMonitorPointDataByParam(paramMap);
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            String mnCommon;
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : outPutDataList) {
                mnCommon = map.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
            }
            //设置时间及集合
            String collection = "";
            String dataKey = "";
            String valueKey = "";
            String hb_starttime = "";
            String hb_endtime = "";
            String tb_starttime = "";
            String tb_endtime = "";
            String countDate = "";
            switch (timetype) {
                case "day":
                    countDate = starttime + "~" + endtime.split("-")[2];
                    collection = db_dayFlowData;
                    dataKey = "DayFlowDataList";
                    valueKey = "AvgFlow";
                    hb_starttime = DataFormatUtil.getBeforeByDayTime(1, starttime);
                    hb_endtime = DataFormatUtil.getBeforeByDayTime(1, endtime);
                    tb_starttime = DataFormatUtil.getDayTBDate(starttime);
                    tb_endtime = DataFormatUtil.getDayTBDate(endtime);
                    break;
                case "month":
                    countDate = starttime + "~" + endtime.split("-")[1];
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    hb_starttime = DataFormatUtil.getBeforeByMonthTime(1, starttime);
                    hb_endtime = DataFormatUtil.getBeforeByMonthTime(1, endtime);
                    tb_starttime = DataFormatUtil.getMonthTBDate(starttime);
                    tb_endtime = DataFormatUtil.getMonthTBDate(endtime);
                    collection = db_monthFlowData;
                    dataKey = "MonthFlowDataList";
                    valueKey = "PollutantFlow";
                    break;
                case "year":
                    countDate = starttime + "~" + endtime;
                    starttime = DataFormatUtil.getYearFirst(starttime);
                    endtime = DataFormatUtil.getYearLast(endtime);
                    hb_starttime = DataFormatUtil.getBeforeByYearTime(1, starttime);
                    hb_endtime = DataFormatUtil.getBeforeByYearTime(1, endtime);
                    collection = db_yearFlowData;
                    dataKey = "YearFlowDataList";
                    valueKey = "PollutantFlow";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(collection)) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("pollutantcodes", pollutantcodes);
                paramMap.put("collection", collection);
                //获取当前时间区间排放量
                List<Document> currFlowDataList = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                if (currFlowDataList.size() > 0) {
                    Map<String, Map<String, Double>> curr_IdAndCodeAndValue = getIdAndPollutantCodeAndValue(dataKey,
                            valueKey, currFlowDataList, mnAndPollutionId, pollutantcodes);
                    hb_starttime = hb_starttime + " 00:00:00";
                    hb_endtime = hb_endtime + " 23:59:59";
                    paramMap.put(starttime, hb_starttime);
                    paramMap.put(endtime, hb_endtime);
                    List<Document> HBFlowDataList = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                    Map<String, Map<String, Double>> HB_IdAndCodeAndValue = getIdAndPollutantCodeAndValue(dataKey,
                            valueKey, HBFlowDataList, mnAndPollutionId, pollutantcodes);
                    Map<String, Map<String, Double>> TB_IdAndCodeAndValue = new HashMap<>();
                    if (StringUtils.isNotBlank(tb_endtime)) {
                        tb_starttime = tb_starttime + " 00:00:00";
                        tb_endtime = tb_endtime + " 23:59:59";
                        paramMap.put("starttime", tb_starttime);
                        paramMap.put("endtime", tb_endtime);
                        List<Document> TBFlowDataList = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                        TB_IdAndCodeAndValue = getIdAndPollutantCodeAndValue(dataKey,
                                valueKey, TBFlowDataList, mnAndPollutionId, pollutantcodes);
                    }
                    paramMap.clear();
                    paramMap.put("monitorpointtype", monitorpointtype);
                    paramMap.put("codes", pollutantcodes);
                    List<Map<String, Object>> pollutantDataList = pollutantService.getPollutantsByPollutantType(paramMap);
                    if (pollutantDataList.size() > 0) {
                        Map<String, Object> codeAndName = new HashMap<>();
                        for (Map<String, Object> pollutantData : pollutantDataList) {
                            codeAndName.put(pollutantData.get("code").toString(), pollutantData.get("name"));
                        }
                        String currentflow = "";
                        String tb_flow = "";
                        String hb_flow = "";
                        Double totalFlow;
                        Map<String, Double> codeAndValue;
                        for (String pollutionIdKey : pollutionIdAndName.keySet()) {
                            for (String code : pollutantcodes) {
                                 currentflow = "";
                                 tb_flow = "";
                                 hb_flow = "";
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("pollutionname", pollutionIdAndName.get(pollutionIdKey));
                                resultMap.put("pollutantname", codeAndName.get(code));
                                resultMap.put("countdate", countDate);
                                if (curr_IdAndCodeAndValue.containsKey(pollutionIdKey)) {
                                    codeAndValue = curr_IdAndCodeAndValue.get(pollutionIdKey);
                                    currentflow = DataFormatUtil.SaveTwoAndSubZero(codeAndValue.get(code));
                                }
                                if (HB_IdAndCodeAndValue.containsKey(pollutionIdKey)) {
                                    codeAndValue = HB_IdAndCodeAndValue.get(pollutionIdKey);
                                    hb_flow = DataFormatUtil.SaveTwoAndSubZero(codeAndValue.get(code));
                                }
                                if (TB_IdAndCodeAndValue.containsKey(pollutionIdKey)) {
                                    codeAndValue = TB_IdAndCodeAndValue.get(pollutionIdKey);
                                    tb_flow = DataFormatUtil.SaveTwoAndSubZero(codeAndValue.get(code));
                                }
                                resultMap.put("currentflow", currentflow);
                                resultMap.put("orderindex", currentflow.equals("") ? 0d : Double.parseDouble(currentflow));
                                resultMap.put("tb_change", setChangeData(tb_flow, currentflow));
                                resultMap.put("hb_change", setChangeData(hb_flow, currentflow));
                                resultMap.put("tb_flow", tb_flow);
                                resultMap.put("hb_flow", hb_flow);
                                totalFlow = curr_IdAndCodeAndValue.get("totalflow").get("totalflow");
                                resultMap.put("proportion", setProportionData(currentflow, totalFlow));
                                resultList.add(resultMap);
                            }
                        }
                        //排序
                        resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("orderindex").toString())).reversed()).collect(Collectors.toList());
                    }
                }
            }
        }
        return resultList;

    }


    /**
     * @author: lip
     * @date: 2020/3/4 0004 上午 10:56
     * @Description: 获取行业排污数据（日排放，月排放，年排放）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getIndustryFlowDataByParam", method = RequestMethod.POST)
    public Object getIndustryFlowDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> resultList = getIndustryFlowDataList(monitorpointtype, pollutantcodes, timetype, starttime, endtime);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 下午 4:53
     * @Description: 获取行业排污数据（日排放，月排放，年排放）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getIndustryFlowDataList(Integer monitorpointtype, List<String> pollutantcodes, String timetype, String starttime, String endtime) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        //1,获取企业行业信息列表
        Map<String, Object> paramMap = new HashMap<>();
        List<Map<String, Object>> pollutionDataList = pollutionService.getPollutionNameAndPkid(paramMap);
        if (pollutionDataList.size() > 0) {
            String pollutionId;
            String industrycode;
            Map<String, List<String>> codeAndPollutionIds = new HashMap<>();
            List<String> pollutionIds;
            Map<String, Object> industryCodeAndName = new HashMap<>();
            for (Map<String, Object> pollutionData : pollutionDataList) {
                if (pollutionData.get("industrycode") != null) {
                    pollutionId = pollutionData.get("pollutionid").toString();
                    industrycode = pollutionData.get("industrycode").toString();
                    industryCodeAndName.put(industrycode, pollutionData.get("industryname"));
                    if (codeAndPollutionIds.containsKey(industrycode)) {
                        pollutionIds = codeAndPollutionIds.get(industrycode);
                    } else {
                        pollutionIds = new ArrayList<>();
                    }
                    pollutionIds.add(pollutionId);
                    codeAndPollutionIds.put(industrycode, pollutionIds);
                }
            }
            paramMap.put("outputids", Arrays.asList());
            paramMap.put("monitorpointtype", monitorpointtype);
            //2,根据监测类型获取mn号集合
            List<Map<String, Object>> outPutDataList = onlineService.getMonitorPointDataByParam(paramMap);
            Map<String, Object> mnAndPollutionId = new HashMap<>();
            String mnCommon;
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : outPutDataList) {
                mnCommon = map.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndPollutionId.put(mnCommon, map.get("pk_pollutionid"));
            }
            //设置时间及集合
            String collection = "";
            String dataKey = "";
            String valueKey = "";
            String hb_starttime = "";
            String hb_endtime = "";
            String tb_starttime = "";
            String tb_endtime = "";
            String countDate = "";
            switch (timetype) {
                case "day":
                    countDate = starttime + "~" + endtime.split("-")[2];
                    collection = db_dayFlowData;
                    dataKey = "DayFlowDataList";
                    valueKey = "AvgFlow";
                    hb_starttime = DataFormatUtil.getBeforeByDayTime(1, starttime);
                    hb_endtime = DataFormatUtil.getBeforeByDayTime(1, endtime);
                    tb_starttime = DataFormatUtil.getDayTBDate(starttime);
                    tb_endtime = DataFormatUtil.getDayTBDate(endtime);
                    break;
                case "month":
                    countDate = starttime + "~" + endtime.split("-")[1];
                    starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
                    endtime = DataFormatUtil.getLastDayOfMonth(endtime);
                    hb_starttime = DataFormatUtil.getBeforeByMonthTime(1, starttime);
                    hb_endtime = DataFormatUtil.getBeforeByMonthTime(1, endtime);
                    tb_starttime = DataFormatUtil.getMonthTBDate(starttime);
                    tb_endtime = DataFormatUtil.getMonthTBDate(endtime);
                    collection = db_monthFlowData;
                    dataKey = "MonthFlowDataList";
                    valueKey = "PollutantFlow";
                    break;
                case "year":
                    countDate = starttime + "~" + endtime;
                    starttime = DataFormatUtil.getYearFirst(starttime);
                    endtime = DataFormatUtil.getYearLast(endtime);
                    hb_starttime = DataFormatUtil.getBeforeByYearTime(1, starttime);
                    hb_endtime = DataFormatUtil.getBeforeByYearTime(1, endtime);
                    collection = db_yearFlowData;
                    dataKey = "YearFlowDataList";
                    valueKey = "PollutantFlow";
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(collection)) {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.clear();
                paramMap.put("mns", mns);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("pollutantcodes", pollutantcodes);
                paramMap.put("collection", collection);
                //获取当前时间区间排放量
                List<Document> currFlowDataList = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                if (currFlowDataList.size() > 0) {
                    Map<String, Map<String, Double>> curr_IdAndCodeAndValue = getIdAndPollutantCodeAndValue(dataKey,
                            valueKey, currFlowDataList, mnAndPollutionId, pollutantcodes);
                    hb_starttime = hb_starttime + " 00:00:00";
                    hb_endtime = hb_endtime + " 23:59:59";
                    paramMap.put(starttime, hb_starttime);
                    paramMap.put(endtime, hb_endtime);
                    List<Document> HBFlowDataList = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                    Map<String, Map<String, Double>> HB_IdAndCodeAndValue = getIdAndPollutantCodeAndValue(dataKey,
                            valueKey, HBFlowDataList, mnAndPollutionId, pollutantcodes);
                    Map<String, Map<String, Double>> TB_IdAndCodeAndValue = new HashMap<>();
                    if (StringUtils.isNotBlank(tb_endtime)) {
                        tb_starttime = tb_starttime + " 00:00:00";
                        tb_endtime = tb_endtime + " 23:59:59";
                        paramMap.put("starttime", tb_starttime);
                        paramMap.put("endtime", tb_endtime);
                        List<Document> TBFlowDataList = onlineService.getDayFlowMonitorDataByParamMap(paramMap);
                        TB_IdAndCodeAndValue = getIdAndPollutantCodeAndValue(dataKey,
                                valueKey, TBFlowDataList, mnAndPollutionId, pollutantcodes);
                    }
                    paramMap.clear();
                    paramMap.put("monitorpointtype", monitorpointtype);
                    paramMap.put("codes", pollutantcodes);
                    List<Map<String, Object>> pollutantDataList = pollutantService.getPollutantsByPollutantType(paramMap);
                    if (pollutantDataList.size() > 0) {
                        Map<String, Object> codeAndName = new HashMap<>();
                        for (Map<String, Object> pollutantData : pollutantDataList) {
                            codeAndName.put(pollutantData.get("code").toString(), pollutantData.get("name"));
                        }
                        String currentflow = "";
                        String tb_flow = "";
                        String hb_flow = "";
                        Double cur_Double;
                        Double tb_Double;
                        Double hb_Double;
                        Double totalFlow;
                        Map<String, Double> codeAndValue;
                        for (String industryCode : industryCodeAndName.keySet()) {
                            for (String code : pollutantcodes) {
                                 currentflow = "";
                                 tb_flow = "";
                                 hb_flow = "";
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("industryname", industryCodeAndName.get(industryCode));
                                resultMap.put("pollutantname", codeAndName.get(code));
                                resultMap.put("countdate", countDate);
                                pollutionIds = codeAndPollutionIds.get(industryCode);
                                cur_Double = 0d;
                                hb_Double = 0d;
                                tb_Double = 0d;
                                for (String pollutionIdKey : pollutionIds) {
                                    if (curr_IdAndCodeAndValue.containsKey(pollutionIdKey)) {
                                        codeAndValue = curr_IdAndCodeAndValue.get(pollutionIdKey);
                                        cur_Double += codeAndValue.get(code);
                                    }
                                    if (HB_IdAndCodeAndValue.containsKey(pollutionIdKey)) {
                                        codeAndValue = HB_IdAndCodeAndValue.get(pollutionIdKey);
                                        hb_Double += codeAndValue.get(code);
                                    }
                                    if (TB_IdAndCodeAndValue.containsKey(pollutionIdKey)) {
                                        codeAndValue = TB_IdAndCodeAndValue.get(pollutionIdKey);
                                        tb_Double += codeAndValue.get(code);
                                    }
                                }
                                currentflow = DataFormatUtil.SaveTwoAndSubZero(cur_Double);
                                hb_flow = DataFormatUtil.SaveTwoAndSubZero(hb_Double);
                                tb_flow = DataFormatUtil.SaveTwoAndSubZero(tb_Double);
                                resultMap.put("currentflow", currentflow);
                                resultMap.put("orderindex", currentflow.equals("") ? 0d : Double.parseDouble(currentflow));
                                resultMap.put("tb_change", setChangeData(tb_flow, currentflow));
                                resultMap.put("hb_change", setChangeData(hb_flow, currentflow));
                                resultMap.put("tb_flow", tb_flow);
                                resultMap.put("hb_flow", hb_flow);
                                totalFlow = curr_IdAndCodeAndValue.get("totalflow").get("totalflow");
                                resultMap.put("proportion", setProportionData(currentflow, totalFlow));
                                resultList.add(resultMap);
                            }
                        }
                        //排序
                        resultList = resultList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("orderindex").toString())).reversed()).collect(Collectors.toList());
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 上午 10:56
     * @Description: 导出行业排污数据（日排放，月排放，年排放）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportIndustryFlowDataByParam", method = RequestMethod.POST)
    public void exportIndustryFlowDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime, HttpServletResponse response, HttpServletRequest request) throws IOException {
        try {

            pollutantcodes.clear();
            pollutantcodes.add("011");
            pollutantcodes.add("060");
            List<Map<String, Object>> resultList = getIndustryFlowDataList(monitorpointtype, pollutantcodes, timetype, starttime, endtime);
            //设置文件名称
            String firstName = "";
            switch (timetype) {
                case "day":
                    firstName = "日";
                    break;
                case "month":
                    firstName = "月";
                    break;
                case "year":
                    firstName = "年";
                    break;
                default:
                    break;
            }
            String fileName = "行业" + firstName + "排放量导出文件_" + new Date().getTime();
            List<String> headers = Arrays.asList("行业名称", "污染物名称", "统计时段", "当前时段排放量(t)",
                    "同比变化(%)", "环比变化(%)", "同比时段排放量(t)", "环比时段排放量(t)", "占比(%)");
            List<String> headersField = Arrays.asList("industryname", "pollutantname", "countdate", "currentflow",
                    "tb_change", "hb_change", "tb_flow", "hb_flow", "proportion");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 下午 4:54
     * @Description: 计算占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object setProportionData(String currentflow, Double totalFlow) {
        String proportion = "";
        if (StringUtils.isNotBlank(currentflow)) {
            Double currDouble = Double.parseDouble(currentflow);
            if(totalFlow>0) {
                proportion = DataFormatUtil.SaveTwoAndSubZero(100 * (currDouble) / totalFlow);
            }
        }
        if (StringUtils.isNotBlank(proportion)) {
            proportion = proportion + "%";
        }
        return proportion;
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 下午 4:54
     * @Description: 计算变化率
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object setChangeData(String flow, String currentflow) {
        String change = "";
        if (StringUtils.isNotBlank(flow) && StringUtils.isNotBlank(currentflow)) {
            Double flowDouble = Double.parseDouble(flow);
            Double currDouble = Double.parseDouble(currentflow);
            if(flowDouble>0) {
                change = DataFormatUtil.SaveTwoAndSubZero(100 * (flowDouble - currDouble) / flowDouble);
            }
        }
        if (StringUtils.isNotBlank(change)) {
            change = change + "%";
        }
        return change;
    }

    /**
     * @author: lip
     * @date: 2020/3/4 0004 下午 4:54
     * @Description: 组装数据（pollutionId+pollutantCode+value）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Map<String, Double>> getIdAndPollutantCodeAndValue(String dataKey,
                                                                           String valueKey,
                                                                           List<Document> flowDataList,
                                                                           Map<String, Object> mnAndPollutionId,
                                                                           List<String> pollutantcodes) {
        List<Document> pollutantDataList;
        String pollutionId;
        String mnCommon;
        String pollutantCode;
        Double value;
        Double totalFlow = 0d;
        Map<String, Map<String, Double>> idAndPollutantCodeAndValue = new HashMap<>();
        Map<String, Double> pollutantCodeAndValue;
        for (Document document : flowDataList) {
            mnCommon = document.getString("DataGatherCode");
            pollutionId = mnAndPollutionId.get(mnCommon).toString();
            if (idAndPollutantCodeAndValue.containsKey(pollutionId)) {
                pollutantCodeAndValue = idAndPollutantCodeAndValue.get(pollutionId);
            } else {
                pollutantCodeAndValue = new HashMap<>();
            }
            pollutantDataList = document.get(dataKey, List.class);
            for (Document pollutant : pollutantDataList) {
                if (pollutantcodes.contains(pollutant.get("PollutantCode"))) {
                    pollutantCode = pollutant.getString("PollutantCode");
                    value = pollutant.get(valueKey) != null ? Double.parseDouble(pollutant.getString(valueKey)) : 0d;
                    totalFlow = totalFlow + value;
                    if (pollutantCodeAndValue.containsKey(pollutantCode)) {
                        pollutantCodeAndValue.put(pollutantCode, pollutantCodeAndValue.get(pollutantCode) + value);
                    } else {
                        pollutantCodeAndValue.put(pollutantCode, value);
                    }
                }
            }
            idAndPollutantCodeAndValue.put(pollutionId, pollutantCodeAndValue);
        }
        Map<String, Double> totalFlowMap = new HashMap<>();
        totalFlowMap.put("totalflow", totalFlow);
        idAndPollutantCodeAndValue.put("totalflow", totalFlowMap);
        return idAndPollutantCodeAndValue;
    }


    /**
     * @author: chengzq
     * @date: 2020/4/9 0009 下午 4:56
     * @Description: 查询企业近一个月每天排放量及同比排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, fk_pollutionid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutionFlowByParams", method = RequestMethod.POST)
    public Object getPollutionFlowByParams(@RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                           @RequestJson(value = "fk_pollutionid", required = false) String fk_pollutionid,
                                           @RequestJson(value = "pollutantcode", required = false) String pollutantcode) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat format1 = new DecimalFormat("0.##");
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            FlowDataVO flowDataVOTwo = new FlowDataVO();
            paramMap.put("fkmonitorpointtypecode", monitorpointtype);
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            String dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

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
            //今年数据
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
            //去年数据
            List<FlowDataVO> flowDataTwo = mongoBaseService.getListByParam(flowDataVOTwo, "DayFlowData", "yyyy-MM-dd HH:mm:ss");


            List<String> times1 = new ArrayList<>();
            List<String> times2 = new ArrayList<>();

            List<FlowDataVO> flowdata1 = flowData.stream().peek(flowDatum -> {
                String monitortime = FormatUtils.formatCSTString(flowDatum.getMonitorTime(), "MM-dd");
                times1.add(monitortime);
                flowDatum.setMonitorTime(monitortime);
            }).collect(Collectors.toList());
            List<FlowDataVO> flowdata2 = flowDataTwo.stream().peek(flowDatum -> {
                String monitortime = FormatUtils.formatCSTString(flowDatum.getMonitorTime(), "MM-dd");
                times2.add(monitortime);
                flowDatum.setMonitorTime(monitortime);
            }).collect(Collectors.toList());

            times1.addAll(times2);
            List<String> collect = times1.stream().distinct().sorted(String::compareTo).collect(Collectors.toList());


            for (String timepoint : collect) {
                Map<String, Object> map = new HashMap<>();
                Object thissum = flowdata1.stream().filter(m -> m.getMonitorTime().equals(timepoint)).flatMap(m -> m.getDayFlowDataList().stream()).filter(m -> ((Map<String, Object>) m).get("PollutantCode") != null
                        && pollutantcode.equals(((Map<String, Object>) m).get("PollutantCode").toString()) && ((Map<String, Object>) m).get("CorrectedFlow") != null)
                        .map(m -> ((Map<String, Object>) m).get("CorrectedFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                Object lastsum = flowdata2.stream().filter(m -> m.getMonitorTime().equals(timepoint)).flatMap(m -> m.getDayFlowDataList().stream()).filter(m -> ((Map<String, Object>) m).get("PollutantCode") != null
                        && pollutantcode.equals(((Map<String, Object>) m).get("PollutantCode").toString()) && ((Map<String, Object>) m).get("CorrectedFlow") != null)
                        .map(m -> ((Map<String, Object>) m).get("CorrectedFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                String flowUnit = flowdata1.stream().filter(m -> m.getMonitorTime().equals(timepoint)).findFirst().orElse(new FlowDataVO()).getFlowUnit();
                map.put("thissum", format1.format(Double.valueOf(thissum.toString())));//本月排放量
                map.put("lastsum", format1.format(Double.valueOf(lastsum.toString())));//同比排放量
                map.put("timepoint", endtime.substring(0, 4) + "-" + timepoint);
                map.put("flowUnit", flowUnit);
                resultList.add(map);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);


        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/17 0017 下午 6:53
     * @Description: 通过多参数获取不同时间类型企业排放量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, fk_pollutionid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getdiffTimePollutionFlowByParams", method = RequestMethod.POST)
    public Object getdiffTimePollutionFlowByParams(@RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                                   @RequestJson(value = "fk_pollutionid", required = false) String fk_pollutionid,
                                                   @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                   @RequestJson(value = "datetype") String datetype,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat format1 = new DecimalFormat("0.##");
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            FlowDataVO flowDataVOTwo = new FlowDataVO();
            paramMap.put("fkmonitorpointtypecode", monitorpointtype);
            paramMap.put("fk_pollutionid", fk_pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);

            String dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            String pattern = "";
            if ("day".equals(datetype)) {
                starttime += " 00:00:00";
                endtime += " 23:59:59";
                pattern = "yyyy-MM-dd";
            } else if ("month".equals(datetype)) {
                starttime = DataFormatUtil.getYearMothFirst(starttime) + " 00:00:00";
                endtime = DataFormatUtil.getYearMothLast(endtime) + " 23:59:59";
                pattern = "yyyy-MM";
            } else if ("year".equals(datetype)) {
                starttime = DataFormatUtil.getYearFirst(starttime) + " 00:00:00";
                endtime = DataFormatUtil.getYearLast(endtime) + " 23:59:59";
                pattern = "yyyy";
            }


            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);
            //今年数据
            List<FlowDataVO> flowData = mongoBaseService.getListByParam(flowDataVO, "DayFlowData", "yyyy-MM-dd HH:mm:ss");


            Calendar instancetwo = Calendar.getInstance();
            instancetwo.setTime(DataFormatUtil.getDateYMDHMS(starttime));
            instancetwo.add(Calendar.YEAR, -1);
            Date datetwo = instancetwo.getTime();
            String starttimetwo = format.format(datetwo);
            String endtimetwo = starttime;


            paramMap.put("starttime", starttimetwo);
            paramMap.put("endtime", endtimetwo);
            flowDataVOTwo.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVOTwo.setDataGatherCode(dgimns);
            //去年数据
            List<FlowDataVO> flowDataTwo = mongoBaseService.getListByParam(flowDataVOTwo, "DayFlowData", "yyyy-MM-dd HH:mm:ss");

            Set<String> timeSet = new HashSet<>();
            String finalPattern = pattern;
            List<FlowDataVO> flowdata1 = flowData.stream().peek(flowDatum -> {
                String monitortime = FormatUtils.formatCSTString(flowDatum.getMonitorTime(), finalPattern);
                timeSet.add(monitortime);
                flowDatum.setMonitorTime(monitortime);
            }).collect(Collectors.toList());
            List<FlowDataVO> flowdata2 = flowDataTwo.stream().peek(flowDatum -> {
                String monitortime = FormatUtils.formatCSTString(flowDatum.getMonitorTime(), finalPattern);
                flowDatum.setMonitorTime(monitortime);
            }).collect(Collectors.toList());
            flowdata2.addAll(flowdata1);

            for (String timepoint : timeSet) {
                Map<String, Object> map = new HashMap<>();
                Object thissum = flowdata1.stream().filter(m -> m.getMonitorTime().equals(timepoint)).flatMap(m -> m.getDayFlowDataList().stream()).filter(m -> ((Map<String, Object>) m).get("PollutantCode") != null
                        && pollutantcode.equals(((Map<String, Object>) m).get("PollutantCode").toString()) && ((Map<String, Object>) m).get("CorrectedFlow") != null)
                        .map(m -> ((Map<String, Object>) m).get("CorrectedFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                Object lastsum = flowdata2.stream().filter(m -> m.getMonitorTime().equals(getYearonYearTime(timepoint, finalPattern))).flatMap(m -> m.getDayFlowDataList().stream()).filter(m -> ((Map<String, Object>) m).get("PollutantCode") != null
                        && pollutantcode.equals(((Map<String, Object>) m).get("PollutantCode").toString()) && ((Map<String, Object>) m).get("CorrectedFlow") != null)
                        .map(m -> ((Map<String, Object>) m).get("CorrectedFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                String flowUnit = flowdata1.stream().filter(m -> m.getMonitorTime().equals(timepoint)).findFirst().orElse(new FlowDataVO()).getFlowUnit();
                if (Double.valueOf(thissum.toString()) > 0 || Double.valueOf(lastsum.toString()) > 0) {
                    map.put("thissum", format1.format(Double.valueOf(thissum.toString())));//本月排放量
                    map.put("lastsum", format1.format(Double.valueOf(lastsum.toString())));//同比排放量
                    map.put("timepoint", timepoint);
                    map.put("flowUnit", flowUnit);
                    resultList.add(map);
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("timepoint") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("timepoint").toString())).collect(Collectors.toList()));


        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 下午 5:17
     * @Description: 获取去年同期时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime, pattern]
     * @throws:
     */
    private String getYearonYearTime(String monitortime, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            Calendar instancetwo = Calendar.getInstance();
            instancetwo.setTime(format.parse(monitortime));
            instancetwo.add(Calendar.YEAR, -1);
            Date datetwo = instancetwo.getTime();
            return format.format(datetwo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @author: xsm
     * @date: 2022/03/25 0025 上午 11:22
     * @Description: 获取某个企业近一年某类型某污染物每月排放总量趋势及同比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, fk_pollutionid, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getOnePollutionPollutantFlowDataByParams", method = RequestMethod.POST)
    public Object getOnePollutionPollutantFlowDataByParams(@RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                           @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                           @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                           @RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DecimalFormat format1 = new DecimalFormat("0.##");
            List<Map<String, Object>> resultList = new ArrayList<>();
            FlowDataVO flowDataVO = new FlowDataVO();
            FlowDataVO flowDataVOTwo = new FlowDataVO();
            paramMap.put("fkmonitorpointtypecode", monitorpointtype);
            paramMap.put("fk_pollutionid", pollutionid);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
            String dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            String pattern = "";
            starttime = DataFormatUtil.getYearMothFirst(starttime) + " 00:00:00";
            endtime = DataFormatUtil.getYearMothLast(endtime) + " 23:59:59";
            pattern = "yyyy-MM";
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            flowDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVO.setDataGatherCode(dgimns);
            //今年数据
            List<FlowDataVO> flowData = mongoBaseService.getListByParam(flowDataVO, "MonthFlowData", "yyyy-MM-dd HH:mm:ss");

            Calendar instancetwo = Calendar.getInstance();
            instancetwo.setTime(DataFormatUtil.getDateYMDHMS(starttime));
            instancetwo.add(Calendar.YEAR, -1);
            Date datetwo = instancetwo.getTime();
            String starttimetwo = format.format(datetwo);
            String endtimetwo = starttime;

            paramMap.put("starttime", starttimetwo);
            paramMap.put("endtime", endtimetwo);
            flowDataVOTwo.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            flowDataVOTwo.setDataGatherCode(dgimns);
            //去年数据
            List<FlowDataVO> flowDataTwo = mongoBaseService.getListByParam(flowDataVOTwo, "MonthFlowData", "yyyy-MM-dd HH:mm:ss");

            Set<String> timeSet = new HashSet<>();
            String finalPattern = pattern;
            List<FlowDataVO> flowdata1 = flowData.stream().peek(flowDatum -> {
                String monitortime = FormatUtils.formatCSTString(flowDatum.getMonitorTime(), finalPattern);
                timeSet.add(monitortime);
                flowDatum.setMonitorTime(monitortime);
            }).collect(Collectors.toList());
            List<FlowDataVO> flowdata2 = flowDataTwo.stream().peek(flowDatum -> {
                String monitortime = FormatUtils.formatCSTString(flowDatum.getMonitorTime(), finalPattern);
                flowDatum.setMonitorTime(monitortime);
            }).collect(Collectors.toList());
            flowdata2.addAll(flowdata1);

            for (String timepoint : timeSet) {
                Map<String, Object> map = new HashMap<>();
                Object thissum = flowdata1.stream().filter(m -> m.getMonitorTime().equals(timepoint)).flatMap(m -> m.getMonthFlowDataList().stream()).filter(m -> ((Map<String, Object>) m).get("PollutantCode") != null
                        && pollutantcode.equals(((Map<String, Object>) m).get("PollutantCode").toString()) && ((Map<String, Object>) m).get("PollutantFlow") != null)
                        .map(m -> ((Map<String, Object>) m).get("PollutantFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                Object lastsum = flowdata2.stream().filter(m -> m.getMonitorTime().equals(getYearonYearTime(timepoint, finalPattern))).flatMap(m -> m.getMonthFlowDataList().stream()).filter(m -> ((Map<String, Object>) m).get("PollutantCode") != null
                        && pollutantcode.equals(((Map<String, Object>) m).get("PollutantCode").toString()) && ((Map<String, Object>) m).get("PollutantFlow") != null)
                        .map(m -> ((Map<String, Object>) m).get("PollutantFlow")).collect(Collectors.summingDouble(m -> Double.valueOf(m.toString())));
                String flowUnit = flowdata1.stream().filter(m -> m.getMonitorTime().equals(timepoint)).findFirst().orElse(new FlowDataVO()).getFlowUnit();
                if (Double.valueOf(thissum.toString()) > 0 ) {
                    map.put("thissum", format1.format(Double.valueOf(thissum.toString())));//本月排放量
                    map.put("lastsum", format1.format(Double.valueOf(lastsum.toString())));//同比排放量
                    map.put("timepoint", timepoint);
                    map.put("flowUnit", flowUnit);
                    resultList.add(map);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("timepoint") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("timepoint").toString())).collect(Collectors.toList()));


        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
