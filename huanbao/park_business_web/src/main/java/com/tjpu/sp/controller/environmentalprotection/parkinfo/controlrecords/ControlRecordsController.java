package com.tjpu.sp.controller.environmentalprotection.parkinfo.controlrecords;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.environmentalprotection.controlrecords.ControlRecordsVO;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.parkinfo.controlrecords.ControlRecordsService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: lip
 * @date: 2020/5/9 0009 下午 4:49
 * @Description: 管控建议处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("controlrecords")
public class ControlRecordsController {

    @Autowired
    private ControlRecordsService controlRecordsService;

    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;

    @Autowired
    private OnlineService onlineService;

    @Autowired
    private PollutantService pollutantService;

    /**
     * @author: lip
     * @date: 2020/5/9 0009 下午 4:52
     * @Description: 获取最新一条管控建议记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getLastControlRecordsData", method = RequestMethod.POST)
    public Object getLastControlRecordsData() {
        try {
            Map<String, Object> resultMap = controlRecordsService.getLastData();
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/9 0009 下午 4:52
     * @Description: 获取管控建议记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getControlRecordsData", method = RequestMethod.POST)
    public Object getControlRecordsData(@RequestJson(value = "pagenum") Integer pagenum,
                                        @RequestJson(value = "pagesize") Integer pagesize,
                                        @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                        @RequestJson(value = "starttime", required = false) String starttime,
                                        @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            PageInfo<Map<String, Object>> pageInfo = controlRecordsService.getPageDataByParam(paramMap);
            resultMap.put("datalist", pageInfo.getList());
            resultMap.put("total", pageInfo.getTotal());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 更新或添加数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/11/19 9:34
     */
    @RequestMapping(value = "/addOrUpdateData", method = RequestMethod.POST)
    public Object addOrUpdateData(@RequestJson(value = "formdata") Object formdata) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(formdata);
            ControlRecordsVO controlRecordsVO = JSONObjectUtil.parseStringToJavaObject(jsonObject.toString(), ControlRecordsVO.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            controlRecordsVO.setUpdatetime(new Date());
            controlRecordsVO.setUpdateuser(username);
            if (StringUtils.isNotBlank(controlRecordsVO.getPkId())) {//更新
                controlRecordsService.updateData(controlRecordsVO);
            } else {//添加
                controlRecordsVO.setPkId(UUID.randomUUID().toString());
                controlRecordsService.addData(controlRecordsVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/11/19 9:34
     */
    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public Object deleteById(@RequestJson(value = "id") String id) {
        try {
            controlRecordsService.deleteById(id);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/11/19 9:34
     */
    @RequestMapping(value = "/getEditOrDetailById", method = RequestMethod.POST)
    public Object getEditOrDetailById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> resultMap = controlRecordsService.getEditOrDetailById(id);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 根据ID删除数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/11/19 9:34
     */
    @RequestMapping(value = "/getAllStinkPoint", method = RequestMethod.POST)
    public Object getAllStinkPoint() {
        try {
            List<Map<String, Object>> dataList = controlRecordsService.getAllStinkPoint();
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/9 0009 下午 4:52
     * @Description: 根据查询条件获取获取控分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getControlAnalyseDataByParam", method = RequestMethod.POST)
    public Object getControlAnalyseDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                               @RequestJson(value = "happentime") String happenTime,
                                               @RequestJson(value = "pollutantcodes") String pollutantCodes
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("happentime", happenTime);
            paramMap.put("pollutantcodes", pollutantCodes);
            List<Map<String, Object>> dataList = controlRecordsService.getControlRecordsDataByParam(paramMap);
            if (dataList.size() > 0) {
                Map<String, Object> dataMap = dataList.get(0);
                resultMap.put("controldesc", dataMap.get("controldesc"));
                List<String> codes = Arrays.asList(pollutantCodes.split(","));
                paramMap.clear();
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                List<Map<String, Object>> pointDataList = outPutUnorganizedService.getOnlineUnorganizedMonitorPointInfoByParamMap(paramMap);
                if (pointDataList.size() > 0) {
                    List<String> CJMNList = new ArrayList<>();
                    String mnCommon;
                    Map<String, Object> mnAndPointName = new HashMap<>();
                    for (Map<String, Object> pointData : pointDataList) {
                        if (pointData.get("dgimn") != null) {
                            mnCommon = pointData.get("dgimn").toString();
                            if (pointData.get("monitorpointname") != null) {
                                mnAndPointName.put(mnCommon, pointData.get("monitorpointname").toString());
                                CJMNList.add(mnCommon);
                            }
                        }
                    }
                    String tempTime = DataFormatUtil.formatIntMinuteTime(happenTime);
                    Date nowDay = DataFormatUtil.getDateYMDHMS(tempTime);
                    String ms = DataFormatUtil.getDateMS(nowDay);
                    String endTime = DataFormatUtil.getDateYMDHMS(nowDay);
                    String startTime = DataFormatUtil.getBeforeByHourTime(1, DataFormatUtil.getDateYMDH(nowDay)) + ":" + ms;
                    paramMap.put("starttime", startTime);
                    paramMap.put("endtime", endTime);
                    paramMap.put("mns", CJMNList);
                    String monitorTimeKey = "MonitorTime";
                    paramMap.put("monitortimekey", monitorTimeKey);
                    paramMap.put("unwindkey", "MinuteDataList");
                    paramMap.put("pollutantCodes", codes);
                    paramMap.put("collection", "MinuteData");
                    List<Document> documents = onlineService.getUnWindMonitorDataByParamMap(paramMap);
                    if (documents.size() > 0) {
                        Map<String, Map<String, Map<String, Object>>> pollutantAndMnAndTimeAndValue = new HashMap<>();
                        Map<String, Map<String, LinkedHashMap<String, Float>>> pollutantAndTimeAndMnAndValue = new HashMap<>();
                        Map<String, Map<String, Object>> mnAndTimeAndValue;
                        Map<String, LinkedHashMap<String, Float>> timeAndMnAndValue;
                        Map<String, Object> timeAndValue;
                        LinkedHashMap<String, Float> mnAndValue;
                        String monitorTime;
                        String pollutantCode;
                        float monitorValue;
                        for (Document document : documents) {
                            mnCommon = document.getString("DataGatherCode");
                            monitorTime = DataFormatUtil.getDateYMDHM(document.getDate(monitorTimeKey));
                            pollutantCode = document.getString("PollutantCode");
                            if (StringUtils.isNotBlank(document.getString("AvgStrength"))) {
                                monitorValue = Float.parseFloat(document.getString("AvgStrength"));
                                if (pollutantAndMnAndTimeAndValue.containsKey(pollutantCode)) {
                                    mnAndTimeAndValue = pollutantAndMnAndTimeAndValue.get(pollutantCode);
                                    timeAndMnAndValue = pollutantAndTimeAndMnAndValue.get(pollutantCode);
                                } else {
                                    mnAndTimeAndValue = new HashMap<>();
                                    timeAndMnAndValue = new HashMap<>();
                                }
                                if (mnAndTimeAndValue.containsKey(mnCommon)) {
                                    timeAndValue = mnAndTimeAndValue.get(mnCommon);
                                } else {
                                    timeAndValue = new HashMap<>();
                                }

                                if (timeAndMnAndValue.containsKey(monitorTime)) {
                                    mnAndValue = timeAndMnAndValue.get(monitorTime);
                                } else {
                                    mnAndValue = new LinkedHashMap<>();
                                }

                                timeAndValue.put(monitorTime, monitorValue);
                                mnAndValue.put(mnCommon, monitorValue);
                                mnAndTimeAndValue.put(mnCommon, timeAndValue);
                                timeAndMnAndValue.put(monitorTime, mnAndValue);
                                pollutantAndMnAndTimeAndValue.put(pollutantCode, mnAndTimeAndValue);
                                pollutantAndTimeAndMnAndValue.put(pollutantCode, timeAndMnAndValue);
                            }
                        }
                        if (pollutantAndMnAndTimeAndValue.size() > 0) {
                            paramMap.clear();
                            paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                            List<Map<String, Object>> pollutantDataList = pollutantService.getPollutantsByCodesAndType(paramMap);
                            if (pollutantDataList.size() > 0) {
                                Map<String, Object> codeAndName = new HashMap<>();
                                for (Map<String, Object> pollutantData : pollutantDataList) {
                                    codeAndName.put(pollutantData.get("code").toString(), pollutantData.get("name"));
                                }
                                List<Map<String, Object>> tableDataList;
                                List<Map<String, Object>> chartDataList;
                                String chartDataDesc;
                                pollutantDataList.clear();
                                Date startDate = DataFormatUtil.getDateYMDHMS(startTime);
                                Date endDate = DataFormatUtil.getDateYMDHMS(endTime);
                                List<String> timeList = DataFormatUtil.getIntervalTimeStringList(startDate, endDate, 5);
                                for (String pollutantIndex : pollutantAndMnAndTimeAndValue.keySet()) {
                                    Map<String, Object> pollutantMap = new HashMap<>();
                                    pollutantMap.put("pollutantcode", pollutantIndex);
                                    pollutantMap.put("pollutantname", codeAndName.get(pollutantIndex));
                                    mnAndTimeAndValue = pollutantAndMnAndTimeAndValue.get(pollutantIndex);
                                    timeAndMnAndValue = pollutantAndTimeAndMnAndValue.get(pollutantIndex);
                                    tableDataList = getTableDataList(mnAndTimeAndValue, mnAndPointName, timeList);
                                    pollutantMap.put("tabledatalist", tableDataList);
                                    chartDataList = getChartDataList(mnAndTimeAndValue, mnAndPointName);
                                    pollutantMap.put("chartdatalist", chartDataList);
                                    chartDataDesc = getChartDataDesc(timeAndMnAndValue, mnAndPointName, codeAndName.get(pollutantIndex));
                                    pollutantMap.put("chartdatadesc", chartDataDesc);
                                    pollutantDataList.add(pollutantMap);
                                }
                                resultMap.put("pollutantdatalist", pollutantDataList);
                            }

                        }
                    }
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
     * @date: 2020/5/12 0012 上午 8:33
     * @Description: 获取图表描述文字
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getChartDataDesc(Map<String, LinkedHashMap<String, Float>> timeAndMnAndValue, Map<String, Object> mnAndPointName, Object pollutantName) {
        Map<String, Float> highMap;
        LinkedHashMap<String, Float> mnTimeAndValue = new LinkedHashMap<>();
        List<String> highTime = new ArrayList<>();
        String timeKey;
        for (String timeIndex : timeAndMnAndValue.keySet()) {
            highMap = AlarmRemindUtil.findOutLiers(timeAndMnAndValue.get(timeIndex));
            if (highMap.size() > 0) {

                for (String mnIndex : highMap.keySet()) {
                    mnTimeAndValue.put(mnIndex + "#" + timeIndex, highMap.get(mnIndex));
                }
            }
        }


        String desc = "由上图可以看出，各厂界监测点位" + pollutantName + "浓度";
        if (mnTimeAndValue.size() > 0) {
            highMap = AlarmRemindUtil.findOutLiers(mnTimeAndValue);
            List<String> pointNameList = new ArrayList<>();
            if (highMap.size() > 0) {
                String mnCommon;
                String time;
                for (String mnTime : highMap.keySet()) {
                    mnCommon = mnTime.split("#")[0];
                    time = mnTime.split("#")[1];
                    if (!pointNameList.contains(mnAndPointName.get(mnCommon))) {
                        pointNameList.add(mnAndPointName.get(mnCommon).toString());
                    }
                    timeKey = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm", "H时m分");
                    if (!highTime.contains(timeKey)) {
                        highTime.add(timeKey);
                    }
                }
            }
            Collections.sort(highTime);
            desc += "在" + DataFormatUtil.FormatListToString(highTime, "、") + "相对较高，" + DataFormatUtil.FormatListToString(pointNameList, "、")
                    + "一直持续较高。";
        } else {
            desc += "相对不高。";
        }
        return desc;
    }

    /**
     * @author: lip
     * @date: 2020/5/12 0012 上午 8:34
     * @Description: 组装图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getChartDataList(Map<String, Map<String, Object>> mnAndTimeAndValue, Map<String, Object> mnAndPointName) {
        List<Map<String, Object>> chartDataList = new ArrayList<>();
        if (mnAndTimeAndValue != null) {
            Map<String, Object> timeAndValue;
            for (String mnIndex : mnAndTimeAndValue.keySet()) {
                Map<String, Object> tableData = new HashMap<>();
                tableData.put("mncode", mnIndex);
                tableData.put("pointname", mnAndPointName.get(mnIndex));
                timeAndValue = mnAndTimeAndValue.get(mnIndex);
                List<Map<String, Object>> monitorDataList = new ArrayList<>();
                for (String time : timeAndValue.keySet()) {
                    Map<String, Object> monitorData = new HashMap<>();
                    monitorData.put("monitortime", time);
                    if (timeAndValue.get(time) != null) {
                        monitorData.put("monitorvalue", timeAndValue.get(time));
                    }
                    monitorDataList.add(monitorData);
                }
                //排序
                Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                if (comparebynum != null) {
                    monitorDataList = monitorDataList.stream().sorted(comparebynum).collect(Collectors.toList());
                }
                tableData.put("monitorDataList", monitorDataList);
                chartDataList.add(tableData);
            }
        }
        return chartDataList;
    }

    /**
     * @author: lip
     * @date: 2020/5/12 0012 上午 8:35
     * @Description: 组装表格数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getTableDataList(Map<String, Map<String, Object>> mnAndTimeAndValue, Map<String, Object> mnAndPointName, List<String> timeList) {
        List<Map<String, Object>> tableDataList = new ArrayList<>();
        if (mnAndTimeAndValue != null) {
            String timeString;
            String timeKey;
            Map<String, Object> timeAndValue;
            List<Double> valueList;
            Double value;
            for (String mnIndex : mnAndTimeAndValue.keySet()) {
                Map<String, Object> tableData = new HashMap<>();
                tableData.put("mncode", mnIndex);
                tableData.put("pointname", mnAndPointName.get(mnIndex));
                timeAndValue = mnAndTimeAndValue.get(mnIndex);
                List<Map<String, Object>> monitorDataList = new ArrayList<>();
                valueList = new ArrayList<>();
                for (String time : timeList) {
                    Map<String, Object> monitorData = new HashMap<>();
                    timeString = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm:ss", "H时m分");
                    timeKey = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                    monitorData.put("monitortime", timeString);
                    if (timeAndValue.get(timeKey) != null) {
                        monitorData.put("monitorvalue", timeAndValue.get(timeKey));
                        value = Double.parseDouble(timeAndValue.get(timeKey).toString());
                        valueList.add(value);
                    } else {
                        monitorData.put("monitorvalue", "-");
                    }
                    monitorDataList.add(monitorData);
                }
                //平均值
                Map<String, Object> monitorData = new HashMap<>();
                monitorData.put("monitortime", "平均值");
                if (valueList.size() > 0) {
                    monitorData.put("monitorvalue", DataFormatUtil.getListAvgValue(valueList));
                } else {
                    monitorData.put("monitorvalue", "-");
                }
                monitorDataList.add(monitorData);
                tableData.put("monitorDataList", monitorDataList);
                tableDataList.add(tableData);
            }
        }
        return tableDataList;
    }

}
