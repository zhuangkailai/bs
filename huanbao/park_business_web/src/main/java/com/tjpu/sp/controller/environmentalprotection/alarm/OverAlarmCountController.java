package com.tjpu.sp.controller.environmentalprotection.alarm;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;


/**
 * @Description: 报警数据统计
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/7/5 16:10
 */
@RestController
@RequestMapping("overAlarmCount")
public class OverAlarmCountController {
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private OnlineMonitorService onlineMonitorService;
    private String overDataCollect = "OverData";
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;


    /**
     * @Description: 获取报警列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/8 16:12
     */
    @RequestMapping(value = "/getOverDataListByParam", method = RequestMethod.POST)
    public Object getOverDataListByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "pointlist") Object pointlist,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "ismerge", required = false) String ismerge

    ) {
        try {

            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> tabledata = new HashMap<>();

            List<Map<String, Object>> tabletitledata = getTableTitleData(monitorpointtype);
            List<Map<String, Object>> pointList = (List<Map<String, Object>>) pointlist;
            Map<String, Object> mnAndName = new HashMap<>();
            Map<String, Object> mnAndEnt = new HashMap<>();
            String mnCommon;
            List<String> mns = new ArrayList<>();
            List<String> pointIds = new ArrayList<>();
            for (Map<String, Object> pointMap : pointList) {
                if (pointMap.get("dgimn") != null) {
                    mnCommon = pointMap.get("dgimn").toString();
                    mnAndName.put(mnCommon, pointMap.get("monitorpointname"));
                    mnAndEnt.put(mnCommon, pointMap.get("pollutionname"));
                    mns.add(mnCommon);
                    pointIds.add(pointMap.get("monitorpointid").toString());
                }
            }

            Map<String, Object> paramMap = new HashMap<>();
            starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
            endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("isoverstandard", true);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.put("collection", collection);
            List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> tablelistdata = new ArrayList<>();
            if (documents.size() > 0) {
                Map<String, Object> flag_codeAndName = new HashMap<>();
                Map<String,Object> f_map = new HashMap<>();
                f_map.put("monitorpointtypes",Arrays.asList(monitorpointtype));
                List<Map<String, Object>> flagList =   pollutantService.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }


                paramMap.put("outputids", pointIds);
                paramMap.put("outputtype", monitorpointtype);
                Map<String, Map<String, Map<String, Object>>> mnAndCodeAndStandardData = onlineMonitorService.getMnAndCodeAndLevelStandardData(paramMap);
                List<String> flowCodes = Arrays.asList("b01", "b02");//流量
                List<String> pollutantCodes = new ArrayList<>();
                String pollutantCode;
                String monitorTime;
                String standardData;
                String isOver = "4";
                Object flag;
                String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                String timeF = MongoDataUtils.getTimeFByCollection(collection);
                List<Document> pollutantList;
                for (Document document : documents) {
                    pollutantList = document.get(dataKey, List.class);
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (pollutant.getBoolean("IsOverStandard")) {
                            pollutantCodes.add(pollutantCode);
                        }
                    }
                }
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    monitorTime = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), timeF);
                    Map<String, Object> dataMap = new HashMap<>();
                    if (mnAndEnt.containsKey(mnCommon)) {
                        dataMap.put("pollutionname", mnAndEnt.get(mnCommon));
                    }
                    dataMap.put("monitorpointname", mnAndName.get(mnCommon));
                    dataMap.put("monitortime", monitorTime);
                    pollutantList = document.get(dataKey, List.class);
                    for (Document pollutant : pollutantList) {

                        flag = flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "");

                        pollutantCode = pollutant.getString("PollutantCode");
                        if (mnAndCodeAndStandardData.containsKey(mnCommon) && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode) != null
                                && mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) != null) {
                            standardData = mnAndCodeAndStandardData.get(mnCommon).get(pollutantCode).get(isOver) + "";
                        } else {
                            standardData = "-";
                        }
                        if (flowCodes.contains(pollutantCode)) {//流量
                            dataMap.put(pollutantCode, pollutant.get(valueKey));
                        } else if (pollutantCodes.contains(pollutantCode)) {
                            if (pollutant.getBoolean("IsOverStandard")) {
                                dataMap.put(pollutantCode + "#" + isOver + "#" + standardData+"#-#-#"+flag, pollutant.get(valueKey));
                            } else {
                                dataMap.put(pollutantCode + "#-1", pollutant.get(valueKey));
                            }
                        }
                    }
                    tablelistdata.add(dataMap);
                }
                paramMap.put("codes", pollutantCodes);
                List<Integer> pollutanttypes;
                if (StringUtils.isNotBlank(ismerge) && "ismerge".equals(ismerge)) {
                    pollutanttypes = Arrays.asList(WasteGasEnum.getCode(), SmokeEnum.getCode());
                    paramMap.put("monitorpointtypes", pollutanttypes);
                } else {
                    paramMap.put("monitorpointtype", monitorpointtype);
                }
                List<Map<String, Object>> pollutantDList = pollutantService.getPollutantsByPollutantType(paramMap);
                String name;
                String code;
                Set<String> codes = new HashSet<>();
                for (Map<String, Object> polllutant : pollutantDList) {
                    code = polllutant.get("code").toString();
                    if (!codes.contains(code)) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        pollutantMap.put("headeralign", "center");
                        pollutantMap.put("prop", code);
                        pollutantMap.put("width", "120px");
                        pollutantMap.put("showhide", true);
                        if (polllutant.get("PollutantUnit") != null) {
                            name = polllutant.get("name") + "(" + polllutant.get("PollutantUnit") + ")";
                        } else {
                            name = polllutant.get("name") + "";
                        }
                        pollutantMap.put("label", name);
                        pollutantMap.put("type", "contaminated");
                        pollutantMap.put("align", "center");
                        tabletitledata.add(pollutantMap);
                        codes.add(code);
                    }
                }
            }
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tablelistdata);
            resultMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
            resultMap.putAll(pageMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getTableTitleData(Integer monitorpointtype) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteGasEnum:
            case RainEnum:
            case WasteWaterEnum:
            case SmokeGasEnum:
                Map<String, Object> entMap = new HashMap<>();
                entMap.put("minwidth", "200px");
                entMap.put("headeralign", "center");
                entMap.put("prop", "pollutionname");
                entMap.put("showhide", true);
                entMap.put("fixed", "left");
                entMap.put("label", "企业名称");
                entMap.put("align", "center");
                dataList.add(entMap);
                break;

        }
        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("minwidth", "200px");
        pointMap.put("headeralign", "center");
        pointMap.put("prop", "monitorpointname");
        pointMap.put("showhide", true);
        pointMap.put("fixed", "left");
        pointMap.put("label", "监测点名称");
        pointMap.put("align", "center");
        dataList.add(pointMap);
        Map<String, Object> timeMap = new HashMap<>();
        timeMap.put("width", "160px");
        timeMap.put("headeralign", "center");
        timeMap.put("prop", "monitortime");
        timeMap.put("showhide", true);
        timeMap.put("fixed", "left");
        timeMap.put("label", "监测时间");
        timeMap.put("align", "center");
        dataList.add(timeMap);
        Map<String, Object> flowMap = new HashMap<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteGasEnum:
            case SmokeGasEnum:
                flowMap.put("width", "100px");
                flowMap.put("headeralign", "center");
                flowMap.put("prop", "b02");
                flowMap.put("showhide", true);
                flowMap.put("label", "流量(m3/s)");
                flowMap.put("align", "center");
                dataList.add(flowMap);
                break;
            case WasteWaterEnum:
                flowMap.put("width", "100px");
                flowMap.put("headeralign", "center");
                flowMap.put("prop", "b01");
                flowMap.put("showhide", true);
                flowMap.put("label", "流量(l/s)");
                flowMap.put("align", "center");
                dataList.add(flowMap);
                break;
        }
        return dataList;
    }


    /**
     * @Description: 导出报警列表数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/8 16:12
     */
    @RequestMapping(value = "/exportOverDataListByParam", method = RequestMethod.POST)
    public void exportOverDataListByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "pointlist") Object pointlist,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "ismerge", required = false) String ismerge,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        try {
            List<Map<String, Object>> tabletitledata = getTableTitleData(monitorpointtype);
            List<Map<String, Object>> pointList = (List<Map<String, Object>>) pointlist;
            Map<String, Object> mnAndName = new HashMap<>();
            Map<String, Object> mnAndEnt = new HashMap<>();
            String mnCommon;
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> pointMap : pointList) {
                if (pointMap.get("dgimn") != null) {
                    mnCommon = pointMap.get("dgimn").toString();
                    mnAndName.put(mnCommon, pointMap.get("monitorpointname"));
                    mnAndEnt.put(mnCommon, pointMap.get("pollutionname"));
                    mns.add(mnCommon);
                }
            }
            Map<String, Object> paramMap = new HashMap<>();
            starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
            endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("isoverstandard", true);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.put("collection", collection);
            List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> tablelistdata = new ArrayList<>();
            if (documents.size() > 0) {


                Map<String, Object> flag_codeAndName = new HashMap<>();

                Map<String,Object> f_map = new HashMap<>();
                f_map.put("monitorpointtypes",Arrays.asList(monitorpointtype));
                List<Map<String, Object>> flagList =   pollutantService.getFlagListByParam(f_map);
                String flag_code;
                for (Map<String, Object> map : flagList) {
                    if (map.get("code") != null) {
                        flag_code = map.get("code").toString();
                        flag_codeAndName.put(flag_code, map.get("name"));
                    }
                }

                List<String> flowCodes = Arrays.asList("b01", "b02");//流量
                List<String> pollutantCodes = new ArrayList<>();
                String pollutantCode;
                String monitorTime;
                String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                String timeF = MongoDataUtils.getTimeFByCollection(collection);
                List<Document> pollutantList;
                for (Document document : documents) {
                    pollutantList = document.get(dataKey, List.class);
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (pollutant.getBoolean("IsOverStandard")) {
                            pollutantCodes.add(pollutantCode);
                        }
                    }
                }
                String isOver = "4";
                Object flag;
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    monitorTime = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), timeF);
                    Map<String, Object> dataMap = new HashMap<>();
                    if (mnAndEnt.containsKey(mnCommon)) {
                        dataMap.put("pollutionname", mnAndEnt.get(mnCommon));
                    }
                    dataMap.put("monitorpointname", mnAndName.get(mnCommon));
                    dataMap.put("monitortime", monitorTime);
                    pollutantList = document.get(dataKey, List.class);
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        flag = flag_codeAndName.get(pollutant.get("Flag") != null ? pollutant.get("Flag").toString().toLowerCase() : "");
                        if (flowCodes.contains(pollutantCode)) {//流量
                            dataMap.put(pollutantCode, pollutant.get(valueKey));
                        } else if (pollutantCodes.contains(pollutantCode)) {
                            if (pollutant.getBoolean("IsOverStandard")) {
                                dataMap.put(pollutantCode + "#" + isOver+"#-#-#-#"+flag, pollutant.get(valueKey));
                            } else {
                                dataMap.put(pollutantCode + "#-1", pollutant.get(valueKey));
                            }
                        }
                    }
                    tablelistdata.add(dataMap);
                }
                paramMap.put("codes", pollutantCodes);
                List<Integer> pollutanttypes;
                if (StringUtils.isNotBlank(ismerge) && "ismerge".equals(ismerge)) {
                    pollutanttypes = Arrays.asList(WasteGasEnum.getCode(), SmokeEnum.getCode());
                    paramMap.put("monitorpointtypes", pollutanttypes);
                } else {
                    paramMap.put("monitorpointtype", monitorpointtype);
                }
                List<Map<String, Object>> pollutantDList = pollutantService.getPollutantsByPollutantType(paramMap);
                String name;
                String code;
                Set<String> codes = new HashSet<>();
                for (Map<String, Object> polllutant : pollutantDList) {
                    code = polllutant.get("code").toString();
                    if (!codes.contains(code)) {
                        Map<String, Object> pollutantMap = new HashMap<>();
                        pollutantMap.put("headeralign", "center");
                        pollutantMap.put("prop", code);
                        pollutantMap.put("width", "120px");
                        pollutantMap.put("showhide", true);
                        if (polllutant.get("PollutantUnit") != null) {
                            name = polllutant.get("name") + "(" + polllutant.get("PollutantUnit") + ")";
                        } else {
                            name = polllutant.get("name") + "";
                        }
                        pollutantMap.put("label", name);
                        pollutantMap.put("type", "contaminated");
                        pollutantMap.put("align", "center");
                        tabletitledata.add(pollutantMap);
                        codes.add(code);
                    }
                }
            }

            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");

            //设置文件名称
            String fileName = "报警统计数据";
            fileName = fileName + "导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
