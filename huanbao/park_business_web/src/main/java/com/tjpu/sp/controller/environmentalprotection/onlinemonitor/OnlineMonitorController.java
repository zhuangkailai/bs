package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationStandardService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.video.VideoCameraService;
import com.tjpu.sp.service.impl.environmentalprotection.online.OnlineServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.FlowChangeEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum;

/**
 * @author: lip
 * @date: 2020/6/15 0015 下午 2:28
 * @Description: 在线监测相关接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@ControllerAdvice
@RequestMapping("onlineMonitor")
public class OnlineMonitorController {

    private final OnlineMonitorService onlineMonitorService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private VideoCameraService videoCameraService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private WaterStationService waterStationService;

    @Autowired
    private NavigationStandardService navigationStandardService;
    @Autowired
    private OnlineService onlineService;
    private final String db_OverData = "OverData";

    public OnlineMonitorController(OnlineMonitorService onlineMonitorService) {
        this.onlineMonitorService = onlineMonitorService;
    }

    /**
     * @author: lip
     * @date: 2020/6/15 0015 下午 2:31
     * @Description: 获取最新监测数据(实时数据一览)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastMonitorDataByParamMap", method = RequestMethod.POST)
    public Object getLastMonitorDataByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);

            if (paramMap.get("monitorpointtype") != null) {
                Integer monitorPointType = Integer.parseInt(paramMap.get("monitorpointtype").toString());
                List<Map<String, Object>> onlineOutPuts;
                List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
                if (dgimns != null && dgimns.size() > 0) {
                    formatParamMap(paramMap);
                    if (monitorPointType != CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode()) {
                        paramMap.put("dgimns", dgimns);
                    }
                    paramMap.put("monitorPointType", monitorPointType);
                    onlineOutPuts = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
                } else {
                    onlineOutPuts = new ArrayList<>();
                }
                resultMap = onlineMonitorService.getOutPutLastDataByParamMap(onlineOutPuts, monitorPointType, paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void formatParamMap(Map<String, Object> paramMap) {
        if (paramMap.get("onlineoutputstatus") != null && !paramMap.get("onlineoutputstatus").equals("")) {
            List<String> onlineoutputstatus = new ArrayList<>();
            String[] types = paramMap.get("onlineoutputstatus").toString().split(",");
            Collections.addAll(onlineoutputstatus, types);
            paramMap.put("onlineoutputstatus", onlineoutputstatus);
        } else {
            paramMap.remove("onlineoutputstatus");
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询图表数据（单站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneMonitorCharDataByParamMap", method = RequestMethod.POST)
    public Object getOneMonitorCharDataByParamMap(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorPointType,
            @RequestJson(value = "outputid", required = false) String outputid,
            @RequestJson(value = "mn", required = false) String mn,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {

        try {
            List<Map<String, Object>> resultList;
            List<String> mns;
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotBlank(outputid)) {
                List<String> outputids = new ArrayList<>();
                outputids.add(outputid);
                paramMap.put("outputids", outputids);
                paramMap.put("monitorpointtype", monitorPointType);
                mns = onlineMonitorService.getMNListByParam(paramMap);
            } else {
                mns = Arrays.asList(mn);
            }

            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            /*if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == monitorPointType) {
                String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                paramMap.put("leftCollection", leftCollection);
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                    paramMap.put("endtime", endtime);
                }
            } else {

            }*/
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

            long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
            boolean isMany = DataFormatUtil.dataIsMany(totalCount);
            if (isMany) {
                String flag = ReturnInfo.other_many.split("#")[0];
                String message = ReturnInfo.other_many.split("#")[1];
                return AuthUtil.returnObject(flag, message);
            }
            List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> pollutants = new ArrayList<>();
            if (documents.size() > 0) {
                paramMap.clear();
                paramMap.put("monitorpointtype", monitorPointType);
                paramMap.put("codes", pollutantcodes);
                pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            }
            resultList = MongoDataUtils.setOneOutPutManyPollutantsCharDataList(documents, pollutants, collection);
            //获取排口污染物的预警、超限、异常值
            Map<String, Object> standardmap = new HashMap<>();
            if (mns != null && mns.size() > 0 && pollutantcodes.size() > 0 && mns.get(0) != null && pollutantcodes.get(0) != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("dgimn", mns.get(0));
                param.put("pollutantcode", pollutantcodes.get(0));
                param.put("monitorpointtype", monitorPointType);
                standardmap = onlineMonitorService.getPollutantEarlyAlarmStandardDataByParamMap(param);
            }
            if (isPage) {
                Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
                pageMap.put("datalist", resultList);
                return AuthUtil.parseJsonKeyToLower("success", pageMap);
            } else {
                if (resultList != null && resultList.size() > 0) {
                    resultList.get(0).put("standard", standardmap);
                }
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询图表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyMonitorCharDataByParamMap", method = RequestMethod.POST)
    public Object getManyMonitorCharDataByParamMap(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {

        try {


            List<Map<String, Object>> charDataList = new ArrayList<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("outputids", outputids);
                List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
                Map<String, String> idAndName = onlineMonitorService.getOutPutIdAndPollution(outputids, monitorpointtype);
                Map<String, String> codeAndName = onlineMonitorService.getPollutantCodeAndName(pollutantcodes, monitorpointtype);
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
                if (pollutantcodes.contains("waterquality")) {
                    pollutantcodes.remove("waterquality");
                    codeAndName.put("waterquality", "水质类别");
                }
                if (pollutantcodes.size() > 0) {
                    paramMap.put("pollutantcodes", pollutantcodes);
                }
                paramMap.put("sort", "asc");
                Map<String, String> outPutIdAndMn = (Map<String, String>) paramMap.get("pointidandmn");

                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> pollutants = new ArrayList<>();
                if (documents.size() > 0 && pollutantcodes.size() > 0) {
                    paramMap.clear();
                    paramMap.put("monitorpointtype", monitorpointtype);
                    paramMap.put("codes", pollutantcodes);
                    pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
                }
                charDataList = MongoDataUtils.setManySomkeOutPutManyPollutantsCharDataList(documents, pollutants,
                        collection, outPutIdAndMn, idAndName, codeAndName);

            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: chengzq
     * @date: 2021/2/4 0004 上午 11:30
     * @Description: 数据查询图表数据
     * @updateUser:xsm
     * @updateDate:2022/6/6 上午10:30
     * @updateDescription:
     * @param: [datamark, monitorpointtype, outputid, pollutantcodes, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getManyMonitorCharDataByParamMapForApp", method = RequestMethod.POST)
    public Object getManyMonitorCharDataByParamMapForApp(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {

        try {
            List<Map<String, Object>> charDataList = new ArrayList<>();
            List<String> outputids = Arrays.asList(outputid);
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("outputids", outputids);
                List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
                Map<String, String> idAndName = onlineMonitorService.getOutPutIdAndPollution(outputids, monitorpointtype);
                Map<String, String> codeAndName = onlineMonitorService.getPollutantCodeAndName(pollutantcodes, monitorpointtype);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);

                if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == monitorpointtype) {
                    String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                    collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                    paramMap.put("leftCollection", leftCollection);
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                        paramMap.put("endtime", endtime);
                    }
                } else {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                        paramMap.put("endtime", endtime);
                    }
                }
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                paramMap.put("pollutantcodes", pollutantcodes);
                paramMap.put("sort", "asc");

                Map<String, String> outPutIdAndMn = (Map<String, String>) paramMap.get("pointidandmn");


                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }

                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);

                List<Map<String, Object>> pollutants = new ArrayList<>();
                if (documents.size() > 0) {
                    paramMap.clear();
                    paramMap.put("monitorpointtype", monitorpointtype);
                    paramMap.put("codes", pollutantcodes);
                    pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
                }
                charDataList = MongoDataUtils.setManySomkeOutPutManyPollutantsCharDataList(documents, pollutants,
                        collection, outPutIdAndMn, idAndName, codeAndName);

            }
            if (pagenum != null && pagesize != null) {
                Map<String, Object> resultMap = new HashMap<>();
                Map<String, Object> stringObjectMap = charDataList.stream().findFirst().orElse(new HashMap<>());
                List<Map<String, Object>> datas = stringObjectMap.get("monitorDataList") == null ? new ArrayList<>() : (List<Map<String, Object>>) stringObjectMap.get("monitorDataList");
                resultMap.put("total", datas.size());
                datas = datas.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("monitortime").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                resultMap.put("datalist", datas);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/06 0006 下午 14:34
     * @Description: 获取点位单污染物列表数据 且显示流量、排放量、折算值等（app）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, monitorpointtype, outputid, pollutantcodes, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getManyMonitorListDataByParamMapForApp", method = RequestMethod.POST)
    public Object getManyMonitorListDataByParamMapForApp(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "mn") String dgimn,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {

        try {
            List<Map<String, Object>> charDataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //获取监测的污染物相关信息
            paramMap.put("pollutanttype", monitorpointtype);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(paramMap);
            Map<String, Object> onepollutant = null;
            if (pollutantInfo != null && pollutantInfo.size() > 0) {
                onepollutant = pollutantInfo.get(0);
            }
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimn", dgimn);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pollutantmap", onepollutant);
            paramMap.put("collection", collection);
            paramMap.put("sort", "asc");
            Map<String, Object> result = onlineMonitorService.getMonitorDataByParamMapForApp(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询列表初始化页面数据（单站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneListPageDataByParams", method = RequestMethod.POST)
    public Object getOneListPageDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineMonitorService.getTableTitleForReport(titleMap);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);

            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
            paramMap.put("isReal", "");
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
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            List<Map<String, Object>> tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tabletitledata);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询列表初始化页面数据（烟气单站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneSmokePointListPageDataByParams", method = RequestMethod.POST)
    public Object getOneSmokePointListPageDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getSmokeTableTitleByParam(titleMap);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
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
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            List<Map<String, Object>> tableListData = onlineMonitorService.getSmokeTableListDataByParam(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tabletitledata", tableTitleData);
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询列表数据（单站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneListDataByParams", method = RequestMethod.POST)
    public Object getOneListDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Integer reportType = 2;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("isReal", "");
            paramMap.put("outputids", outputids);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("reportType", reportType);
            paramMap.put("pointType", monitorpointtype);
            paramMap.put("pollutantType", monitorpointtype);
            List<Map<String, Object>> tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
            Map<String, Object> tabledata = new HashMap<>();
            tabledata.put("tablelistdata", tableListData);
            dataMap.put("tabledata", tabledata);
            Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
            dataMap.putAll(pageMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataMap);
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询列表初始化页面数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyListPageDataByParams", method = RequestMethod.POST)
    public Object getManyListPageDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
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
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            titleMap.put("datamark", datamark);
            List<Map<String, Object>> tabletitledata = onlineMonitorService.getTableTitleForReport(titleMap);
            Map<String, Object> tabledata = new HashMap<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("outputids", outputids);
                List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
                paramMap.putAll(titleMap);
                paramMap.put("mns", mns);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);

                if (StringUtils.isNotBlank(starttime)) {
                    starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("isReal", "");
                paramMap.put("collection", collection);
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                List<Map<String, Object>> tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
                tabledata.put("tablelistdata", tableListData);
                Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
            tabledata.put("tabletitledata", tabletitledata);
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
     * @Description: 数据查询列表初始化页面数据（烟气多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManySmokePointListPageDataByParams", method = RequestMethod.POST)
    public Object getManySmokePointListPageDataByParams(
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
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getSmokeTableTitleByParam(titleMap);
            Map<String, Object> tabledata = new HashMap<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
                paramMap.put("outputids", outputids);
                List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
                paramMap.putAll(titleMap);
                paramMap.put("mns", mns);
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
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                List<Map<String, Object>> tableListData = onlineMonitorService.getSmokeTableListDataByParam(paramMap);
                tabledata.put("tablelistdata", tableListData);
                Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
            tabledata.put("tabletitledata", tableTitleData);
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
     * @Description: 数据查询列表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyListDataByParams", method = RequestMethod.POST)
    public Object getManyGasListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
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
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("outputids", outputids);
                List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);
                paramMap.put("mns", mns);
               /* if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == monitorpointtype) {
                    String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                    collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                    paramMap.put("leftCollection", leftCollection);
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                        paramMap.put("endtime", endtime);
                    }
                } else {

                }*/
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("isReal", "");
                paramMap.put("collection", collection);
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                paramMap.put("reportType", reportType);
                paramMap.put("pointType", monitorpointtype);
                paramMap.put("pollutantType", monitorpointtype);
                List<Map<String, Object>> tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
                Map<String, Object> tabledata = new HashMap<>();
                tabledata.put("tablelistdata", tableListData);
                dataMap.put("tabledata", tabledata);
                Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
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
     * @Description: 导出列表数据（单站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneOutPutReport", method = RequestMethod.POST)
    public void exportOneOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tabletitledata = onlineMonitorService.getTableTitleForReport(titleMap);
            //获取表格数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            paramMap.putAll(titleMap);
            paramMap.put("mns", mns);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            /*if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == monitorpointtype) {
                String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                paramMap.put("leftCollection", leftCollection);
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                    paramMap.put("endtime", endtime);
                }
            } else {

            }*/
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("isReal", "");
            paramMap.put("collection", collection);
            List<Map<String, Object>> tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = getFileNameByType(monitorpointtype);
            fileName = fileName + "监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    /**
     * @Description: 验证是否可以导出
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/8 15:56
     */
    @RequestMapping(value = "isExportOneOutPutReport", method = RequestMethod.POST)
    public Object isExportOneOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime
            ) {

        try {
            //获取表头数据
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            //获取表格数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            paramMap.put("mns", mns);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("isReal", "");
            paramMap.put("collection", collection);

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
            throw  e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 导出列表数据（烟气单站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneSmokeOutPutReport", method = RequestMethod.POST)
    public void exportOneSmokeOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getSmokeTableTitleByParam(titleMap);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
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
            List<Map<String, Object>> tableListData = onlineMonitorService.getSmokeTableListDataByParam(paramMap);
            //设置导出文件数据格式
            List<Map<String, Object>> headerDataList = ExcelUtil.setManyExportHeaderDataByKey(tableTitleData);
            List<Map<String, Object>> valueDataList = ExcelUtil.setManyExportValueDataByKey(tableListData);
            //设置文件名称
            String fileName = getFileNameByType(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            fileName = fileName + "监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headerDataList, valueDataList, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    
    /**
     * @Description: 判断是否可以导出
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/11/8 16:02
     */ 
    @RequestMapping(value = "isExportOneSmokeOutPutReport", method = RequestMethod.POST)
    public Object isExportOneSmokeOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime
            ) {

        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(outputid);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
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
     * @date: 2020/6/15 0015 下午 5:33
     * @Description: 根据监测类型获取导出文件名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getFileNameByType(Integer monitorpointtype) {
        String name = "";
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteWaterEnum:
                name = "废水";
                break;
            case WasteGasEnum:
                name = "废气";
                break;
            case RainEnum:
                name = "雨水";
                break;
            case AirEnum:
                name = "空气";
                break;
            case WaterQualityEnum:
                name = "水质";
                break;
            case EnvironmentalStinkEnum:
                name = "环境恶臭";
                break;
            case EnvironmentalVocEnum:
                name = "VOC";
                break;
            case EnvironmentalDustEnum:
                name = "扬尘";
                break;
            case FactoryBoundaryStinkEnum:
                name = "厂界扬尘";
                break;
            case MicroStationEnum:
                name = "微站";
                break;
            case SmokeEnum:
                name = "烟气";
                break;
            case meteoEnum:
                name = "气象";
                break;
            default:
                name = "";
                break;
        }
        return name;

    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 导出列表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManySmokeOutPutReport", method = RequestMethod.POST)
    public void exportManySmokeOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getSmokeTableTitleByParam(titleMap);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
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
            List<Map<String, Object>> tableListData = onlineMonitorService.getSmokeTableListDataByParam(paramMap);
            //设置导出文件数据格式
            List<Map<String, Object>> headerDataList = ExcelUtil.setManyExportHeaderDataByKey(tableTitleData);
            List<Map<String, Object>> valueDataList = ExcelUtil.setManyExportValueDataByKey(tableListData);
            //设置文件名称
            String fileName = getFileNameByType(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            fileName = fileName + "监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headerDataList, valueDataList, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 导出列表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyOutPutReport", method = RequestMethod.POST)
    public void exportManyOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            titleMap.put("datamark", datamark);
            List<Map<String, Object>> tabletitledata = onlineMonitorService.getTableTitleForReport(titleMap);

            List<Map<String, Object>> tableListData = new ArrayList<>();

            //获取表格数据
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("outputids", outputids);
                List<String> mns = onlineMonitorService.getMNListByParam(paramMap);

                paramMap.putAll(titleMap);
                paramMap.put("mns", mns);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);
               /* if (CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode() == monitorpointtype) {
                    String leftCollection = MongoDataUtils.getAirCollectionByDataMark(datamark);
                    collection = MongoDataUtils.getCollectionByDataMark(datamark + 2);
                    paramMap.put("leftCollection", leftCollection);
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = MongoDataUtils.setAirStartTimeByDataMark(datamark, starttime);
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = MongoDataUtils.setAirEndTimeByDataMark(datamark, endtime);
                        paramMap.put("endtime", endtime);
                    }
                } else {

                }*/
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("isReal", "");
                paramMap.put("collection", collection);
                tableListData = onlineMonitorService.getTableListDataForReport(paramMap);
            }
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = getFileNameByType(monitorpointtype);
            fileName = fileName + "监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    
    /**
     * @Description: 判断是否可以导出数据
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/11/7 10:18
     */ 
    @RequestMapping(value = "isExportManyOutPutReport", method = RequestMethod.POST)
    public Object isExportManyOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("reportType", 2);
            titleMap.put("pointType", monitorpointtype);
            titleMap.put("pollutantType", monitorpointtype);
            titleMap.put("outputids", outputids);
            titleMap.put("datamark", datamark);
            List<Map<String, Object>> tableListData = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("outputids", outputids);
            List<String> mns = onlineMonitorService.getMNListByParam(paramMap);
            paramMap.putAll(titleMap);
            paramMap.put("mns", mns);
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("isReal", "");

            paramMap.put("collection", collection);
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
     * @date: 2020/09/05 0005 上午 9:14
     * @Description: 根据监测类型获取该类型下所有监测点位最近小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllMonitorPointLastHourDataByParamMap", method = RequestMethod.POST)
    public Object getAllMonitorPointLastHourDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "monitortime", required = false) String monitortime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> onlineData = new ArrayList<>();
            if (monitortime != null && !"".equals(monitortime)) {
                String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                paramMap.put("userid", userid);
                paramMap.put("onlydataauthor", "1");
                paramMap.put("fkmonitorpointtypecodes", monitorpointtypes);
                List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
                //List<String> dgimns = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
                //获取污染物
                paramMap.put("pollutanttypes", monitorpointtypes);
                List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
                //获取点位信息及点位最新小时数据和报警状态
                onlineData = onlineMonitorService.getAllMonitorPointLastHourDataByParamMap(outPutInfosByParamMap, pollutantData, monitortime);

                String dataSort = DataFormatUtil.parseProperties("data.sort");
                JSONObject jsonObject = StringUtils.isNotBlank(dataSort) ? JSONObject.fromObject(dataSort) : new JSONObject();
                String statusKey;
                for (Map<String, Object> map : onlineData) {
                    String onlineStatus = map.get("Status") + "";
                    if (map.get("isstop") != null && "1".equals(map.get("isstop").toString())) {
                        onlineStatus = "4";
                    }
                    statusKey = CommonTypeEnum.StatusOrderSetEnum.getIndexByCode(onlineStatus);
                    map.put("orderindex", jsonObject.get(statusKey) != null ? jsonObject.get(statusKey) : 11);
                }
                onlineData = onlineData.stream().sorted(
                        Comparator.comparingInt(
                                m -> ((Map) m).get("orderindex") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("orderindex").toString())
                        )

                ).collect(Collectors.toList());


            }
            return AuthUtil.parseJsonKeyToLower("success", onlineData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2020/09/05 0005 上午 9:14
     * @Description: 根据监测类型和数据类型和自定义参数获取在线监测数据和报警标记
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointOnlineDataAndAlarmFlagByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointOnlineDataAndAlarmFlagByParamMap(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            Map<String, String> outPutIdAndMn = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorpointtype, outPutIdAndMn);
            Map<String, String> idAndName = onlineService.getOutPutIdAndPollution(outputids, monitorpointtype);
            Map<String, String> codeAndName = onlineService.getPollutantCodeAndName(pollutantcodes, monitorpointtype);
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("sort", "asc");
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, Object> onlineData = onlineMonitorService.getEarlyAndOverDataGroupMnAndByTime(mns, pollutantcodes, datamark, starttime, endtime);
            charDataList = setManyOutPutManyPollutantsAlarmFlagDataList(
                    documents,
                    pollutantcodes,
                    collection,
                    outPutIdAndMn,
                    idAndName,
                    codeAndName,
                    onlineData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }

    public static List<Map<String, Object>> setManyOutPutManyPollutantsAlarmFlagDataList(List<Document> documents,
                                                                                         List<String> pollutantcodes,
                                                                                         String collection,
                                                                                         Map<String, String> outPutIdAndMn,
                                                                                         Map<String, String> idAndName,
                                                                                         Map<String, String> codeAndName, Map<String, Object> onlineData) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        String pollutantDataKey = "";
        String valueKey = "";
        Map<String, Object> earlymap = (Map<String, Object>) onlineData.get("early");
        Map<String, Object> overmap = (Map<String, Object>) onlineData.get("over");
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
        } else if ("MonthData".equals(collection)) {
            pollutantDataKey = "MonthDataList";
            valueKey = "AvgStrength";
        }
        //数采仪mn号
        String mnCode = "";
        //污染物编码
        String pollutantCode = "";
        //数采仪MN号+编码
        String mnPollutantCode = "";
        String mnandcode = "";
        //key:数采仪MN号+编码,value:list(monitortime+monitorvalue)
        Map<String, List<Map<String, Object>>> tempMap = new HashMap<>();
        List<Map<String, Object>> tempList;
        for (Document document : documents) {
            mnCode = document.getString("DataGatherCode");
            List<Document> pollutantDataList = (List<Document>) document.get(pollutantDataKey);
            for (Document pollutant : pollutantDataList) {
                pollutantCode = pollutant.get("PollutantCode") != null ? pollutant.getString("PollutantCode") : "";
                if (pollutantcodes.contains(pollutantCode) && !"".equals(pollutantCode)) {
                    mnPollutantCode = mnCode + "," + pollutantCode;
                    mnandcode = mnCode + "_" + pollutantCode;
                    if (tempMap.get(mnPollutantCode) != null) {
                        tempList = tempMap.get(mnPollutantCode);
                    } else {
                        tempList = new ArrayList<>();
                    }
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
                    } else if ("MonthData".equals(collection)) {
                        MonitorTime = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    }
                    List<String> earlytimes = (List<String>) earlymap.get(mnandcode);
                    List<String> overtimes = (List<String>) overmap.get(mnandcode);
                    map.put("early", false);
                    map.put("over", false);
                    if (earlytimes != null) {
                        for (String timeone : earlytimes) {
                            if (timeone.equals(MonitorTime)) {
                                map.put("early", true);
                                break;
                            }
                        }
                    }
                    if (overtimes != null) {
                        for (String timetwo : overtimes) {
                            if (timetwo.equals(MonitorTime)) {
                                map.put("over", true);
                                break;
                            }
                        }
                    }
                    map.put("monitortime", MonitorTime);
                    map.put("monitorvalue", pollutant.get(valueKey));
                    tempList.add(map);
                    tempMap.put(mnPollutantCode, tempList);
                }
            }
        }
        List<Map<String, Object>> monitorDataList;
        for (String outputid : idAndName.keySet()) {
            for (String tempCode : pollutantcodes) {
                mnPollutantCode = outPutIdAndMn.get(outputid) + "," + tempCode;
                monitorDataList = tempMap.get(mnPollutantCode);
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantMap.put("outputid", outputid);
                pollutantMap.put("dataname", idAndName.get(outputid) + "-" + codeAndName.get(tempCode));
                pollutantMap.put("pollutantcode", tempCode);
                pollutantMap.put("monitorDataList", monitorDataList);

                if (monitorDataList != null && monitorDataList.size() > 0) {
                    dataList.add(pollutantMap);
                }

            }
        }

        return dataList;
    }


    /**
     * @author: xsm
     * @date: 2020/09/07 0007 下午 6:18
     * @Description: 根据监测类型和监测点MN号获取该点位监测污染物的标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutantStandardValueDataByParam", method = RequestMethod.POST)
    public Object getPollutantStandardValueDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "dgimn") String dgimn) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtype", monitorpointtype);
            param.put("dgimn", dgimn);
            result = pollutantService.getPollutantStandardValueDataByParam(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @author: lip
     * @date: 2020/11/24 0024 上午 11:26
     * @Description: 自定义查询条件获取报警规律数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/getOverRegularDataByParam", method = RequestMethod.POST)
    public Object getOverRegularDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "dgimns") List<String> dgimns
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("mns", dgimns);

            paramMap.put("collection",db_OverData);
            long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
            boolean isMany = DataFormatUtil.dataIsMany(totalCount);
            if (isMany) {
                String flag = ReturnInfo.other_many.split("#")[0];
                String message = ReturnInfo.other_many.split("#")[1];
                return AuthUtil.returnObject(flag, message);
            }

            //paramMap.put("DataType", CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName());
            List<Document> documents = onlineMonitorService.getOverDataByParam(paramMap);
            Map<String, List<Double>> timeAndValueList = new HashMap<>();
            List<Double> valueList;
            int maxNum = 0;
            if (documents.size() > 0) {
                String monitorTime;
                Double monitorValue;
                for (Document document : documents) {
                    if (document.get("MonitorValue") != null) {
                        monitorTime = DataFormatUtil.formatDateToOtherFormat(document.getDate("OverTime"), "HH:mm");
                        monitorValue = Double.parseDouble(document.getString("MonitorValue"));
                        if (timeAndValueList.containsKey(monitorTime)) {
                            valueList = timeAndValueList.get(monitorTime);
                        } else {
                            valueList = new ArrayList<>();
                        }
                        valueList.add(monitorValue);
                        if (valueList.size() > maxNum) {
                            maxNum = valueList.size();
                        }

                        timeAndValueList.put(monitorTime, valueList);
                    }

                }
            }
            List<String> xData = new ArrayList<>(timeAndValueList.keySet());
            Collections.sort(xData);
            List<Object> yData = new ArrayList<>();
            for (int i = 0; i < maxNum; i++) {
                List<Object> values = new ArrayList<>();
                for (String time : timeAndValueList.keySet()) {
                    valueList = timeAndValueList.get(time);
                    if (valueList.size() > i) {
                        values.add(valueList.get(i));
                    } else {
                        values.add("");
                    }
                }
                yData.add(values);
            }
            resultMap.put("xData", xData);
            resultMap.put("yData", yData);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/11/24 0024 上午 11:26
     * @Description: 自定义查询条件获取多个站点监测及气象数据数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/getManyPointMonitorDataByParam", method = RequestMethod.POST)
    public Object getManyPointMonitorDataByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "datatype") Integer datatype,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "pointdatalist") List<Map<String, Object>> pointdatalist
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (pointdatalist.size() > 0) {
                Map<String, Object> mnAndName = new HashMap<>();
                String commonMn;
                List<String> mns = new ArrayList<>();
                for (Map<String, Object> pointData : pointdatalist) {
                    if (pointData.get("dgimn") != null) {
                        commonMn = pointData.get("dgimn").toString();
                        mnAndName.put(commonMn, pointData.get("monitorpointname"));
                        mns.add(commonMn);
                    }
                }
                String collection = CommonTypeEnum.MongodbDataTypeEnum.getCodeByString(datatype).getName();
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mns", mns);
                String starttime = "";
                String endtime = "";
                String timeForm = "";
                String dataKey = "";
                int minnueNum = 0;
                switch (CommonTypeEnum.MongodbDataTypeEnum.getCodeByString(datatype)) {
                    case MinuteDataEnum:
                        timeForm = "H:m";
                        starttime = monitortime + " 00:00:00";
                        endtime = monitortime + " 23:59:59";
                        dataKey = "MinuteDataList";
                        minnueNum = 5;
                        break;
                    case HourDataEnum:
                        timeForm = "H";
                        starttime = monitortime + " 00:00:00";
                        endtime = monitortime + " 23:59:59";
                        dataKey = "HourDataList";
                        minnueNum = 60;
                        break;
                    case DayDataEnum:
                        timeForm = "d";
                        starttime = DataFormatUtil.getFirstDayOfMonth(monitortime) + " 00:00:00";
                        endtime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
                        dataKey = "DayDataList";
                        minnueNum = 1440;
                        break;
                }
                List<String> pollutantCodes = Arrays.asList(pollutantcode, WindDirectionEnum.getCode(), WindSpeedEnum.getCode());

                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("collection", collection);
                paramMap.put("pollutantcodes", pollutantCodes);
                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Map<String, Map<Date, Map<String, Object>>> mnAndTimeAndDataMap = new HashMap<>();
                    paramMap.clear();
                    paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    paramMap.put("pollutantcode", pollutantcode);
                    List<Map<String, Object>> colorDataList = navigationStandardService.getStandardColorDataByParamMap(paramMap);

                    Map<Date, Map<String, Object>> timeAndDataMap;
                    List<Document> pollutants;
                    String code;
                    Double value = null;
                    Double windDirection = null;
                    Double windSpeed = null;
                    for (Document document : documents) {
                        commonMn = document.getString("DataGatherCode");
                        if (mnAndTimeAndDataMap.containsKey(commonMn)) {
                            timeAndDataMap = mnAndTimeAndDataMap.get(commonMn);
                        } else {
                            timeAndDataMap = new HashMap<>();
                        }
                        Map<String, Object> dataMap = new HashMap<>();
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            code = pollutant.getString("PollutantCode");
                            if (pollutantCodes.contains(code)) {
                                if (pollutantcode.equals(code)) {//监测值
                                    value = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.getString("AvgStrength")) : null;
                                } else if (code.equals(WindDirectionEnum.getCode())) {//风向
                                    windDirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.getString("AvgStrength")) : null;
                                } else if (code.equals(WindSpeedEnum.getCode())) {//风速
                                    windSpeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.getString("AvgStrength")) : null;
                                }
                            }
                        }
                        dataMap.put("monitortime", document.getDate("MonitorTime"));
                        dataMap.put("monitorvalue", value);
                        dataMap.put("colorvalue", getColorValue(value, colorDataList));
                        if (windDirection != null) {
                            dataMap.put("winddirection", DataFormatUtil.windDirectionSwitch(windDirection, "name"));
                        } else {
                            dataMap.put("winddirection", "");
                        }
                        dataMap.put("windspeed", windSpeed);
                        timeAndDataMap.put(document.getDate("MonitorTime"), dataMap);
                        mnAndTimeAndDataMap.put(commonMn, timeAndDataMap);

                    }
                    if (mnAndTimeAndDataMap.size() > 0) {
                        Date startTime = DataFormatUtil.getDateYMDHMS(starttime);
                        Date endTime = DataFormatUtil.getDateYMDHMS(endtime);
                        List<Date> times = DataFormatUtil.getIntervalTimeList(startTime, endTime, minnueNum);
                        times.remove(endTime);
                        for (String mnIndex : mnAndTimeAndDataMap.keySet()) {
                            Map<String, Object> resultMap = new HashMap<>();
                            resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                            resultMap.put("dgimn", mnIndex);
                            List<Map<String, Object>> dataList = new ArrayList<>();

                            timeAndDataMap = mnAndTimeAndDataMap.get(mnIndex);
                            for (Date time : times) {
                                Map<String, Object> dataMap = new HashMap<>();
                                if (timeAndDataMap.containsKey(time)) {
                                    dataMap.putAll(timeAndDataMap.get(time));
                                } else {
                                    dataMap.put("monitorvalue", "");
                                    dataMap.put("colorvalue", "");
                                    dataMap.put("winddirection", "");
                                    dataMap.put("windspeed", "");
                                }
                                dataMap.put("monitortime", DataFormatUtil.formatDateToOtherFormat(time, timeForm));
                                dataList.add(dataMap);
                            }
                            resultMap.put("dataList", dataList);
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

    /**
     * @author: lip
     * @date: 2020/11/24 0024 上午 11:26
     * @Description: 自定义查询条件获取单个站点监测及气象数据数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/getOnePointMonitorDataByParam", method = RequestMethod.POST)
    public Object getOnePointMonitorDataByParam(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "dgimn") String dgimn
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();


            String commonMn;
            List<String> mns = Arrays.asList(dgimn);

            String collection = CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getName();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", mns);

            String starttime = DataFormatUtil.getFirstDayOfMonth(monitortime) + " 00:00:00";
            String endtime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
            List<String> pollutantCodes = Arrays.asList(pollutantcode, WindDirectionEnum.getCode(), WindSpeedEnum.getCode());
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            paramMap.put("pollutantcodes", pollutantCodes);
            List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
            if (documents.size() > 0) {

                paramMap.clear();
                paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                paramMap.put("pollutantcode", pollutantcode);
                List<Map<String, Object>> colorDataList = navigationStandardService.getStandardColorDataByParamMap(paramMap);
                String monitorTime;
                Map<String, Map<String, Map<String, Object>>> dayAndTimeAndDataMap = new HashMap<>();
                Map<String, Map<String, Object>> timeAndDataMap;
                List<Document> pollutants;
                String code;
                Double value = null;
                Double windDirection;
                Double windSpeed;
                String hour = "";
                for (Document document : documents) {
                    windDirection = null;
                    windSpeed = null;
                    monitorTime = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), "yyyy-MM-dd");
                    if (dayAndTimeAndDataMap.containsKey(monitorTime)) {
                        timeAndDataMap = dayAndTimeAndDataMap.get(monitorTime);
                    } else {
                        timeAndDataMap = new HashMap<>();
                    }
                    Map<String, Object> dataMap = new HashMap<>();
                    pollutants = document.get("HourDataList", List.class);
                    for (Document pollutant : pollutants) {
                        code = pollutant.getString("PollutantCode");
                        if (pollutantCodes.contains(code)) {
                            if (pollutantcode.equals(code)) {//监测值
                                value = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.getString("AvgStrength")) : null;
                            } else if (code.equals(WindDirectionEnum.getCode())) {//风向
                                windDirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.getString("AvgStrength")) : null;
                            } else if (code.equals(WindSpeedEnum.getCode())) {//风速
                                windSpeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.getString("AvgStrength")) : null;
                            }
                        }
                    }
                    hour = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), "H");
                    dataMap.put("monitortime", hour);
                    dataMap.put("monitorvalue", value);
                    dataMap.put("colorvalue", getColorValue(value, colorDataList));
                    if (windDirection != null) {
                        dataMap.put("winddirection", DataFormatUtil.windDirectionSwitch(windDirection, "name"));
                    } else {
                        dataMap.put("winddirection", "");
                    }
                    dataMap.put("windspeed", windSpeed);
                    timeAndDataMap.put(hour, dataMap);
                    dayAndTimeAndDataMap.put(monitorTime, timeAndDataMap);
                }
                if (dayAndTimeAndDataMap.size() > 0) {

                    String startYMD = DataFormatUtil.getFirstDayOfMonth(monitortime);
                    String endYMD = DataFormatUtil.getLastDayOfMonth(monitortime);

                    List<String> days = DataFormatUtil.getYMDBetween(startYMD,
                            endYMD);
                    days.add(endYMD);
                    for (String day : days) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("daytime", DataFormatUtil.FormatDateOneToOther(day, "yyyy-MM-dd", "yyyy年M月d日"));
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        if (dayAndTimeAndDataMap.containsKey(day)) {
                            timeAndDataMap = dayAndTimeAndDataMap.get(day);
                            for (int i = 0; i < 24; i++) {
                                Map<String, Object> dataMap = new HashMap<>();
                                hour = i + "";
                                dataMap.put("monitortime", hour);
                                if (timeAndDataMap.containsKey(hour)) {
                                    dataMap.putAll(timeAndDataMap.get(hour));
                                } else {
                                    dataMap.put("monitorvalue", "");
                                    dataMap.put("colorvalue", "");
                                    dataMap.put("winddirection", "");
                                    dataMap.put("windspeed", "");
                                }
                                dataList.add(dataMap);
                            }
                            resultMap.put("datalist", dataList);
                        }
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


    private String getColorValue(Double value, List<Map<String, Object>> colorDataList) {
        String color = "";
        if (value != null) {
            Double minValue;
            Double maxValue;
            for (Map<String, Object> colorData : colorDataList) {
                if (colorData.get("StandardMinValue") != null && colorData.get("StandardMaxValue") != null) {
                    minValue = Double.parseDouble(colorData.get("StandardMinValue").toString());
                    maxValue = Double.parseDouble(colorData.get("StandardMaxValue").toString());
                    if (value >= minValue && maxValue > value) {
                        color = colorData.get("ColourValue") != null ? colorData.get("ColourValue").toString() : "";
                    }
                }
            }
        }
        return color;
    }

    /**
     * @author: xsm
     * @date: 2020/11/20 0020 下午 14:51
     * @Description: 根据排口类型获取排口最新实时数据（APP）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOutPutPointLastOnlineDataByParamForApp", method = RequestMethod.POST)
    public Object getOutPutPointLastOnlineDataByParamForApp(
            @RequestJson(value = "datatype", required = false) String datatype,
            @RequestJson(value = "isauth", required = false) Boolean isauth,
            @RequestJson(value = "searchname", required = false) String searchname,
            @RequestJson(value = "controllevels", required = false) String controllevels,
            @RequestJson(value = "status", required = false) Integer status,
            @RequestJson(value = "onlineoutputstatus", required = false) List<Integer> onlineoutputstatus,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("controllevels", controllevels);
            paramMap.put("searchname", searchname);
            paramMap.put("orderfield", "status");//添加状态排序字段
            paramMap.put("onlineoutputstatus", onlineoutputstatus);
            paramMap.put("datatypeflag", "app");
            paramMap.put("pointstatus", status);//设备状态  1：启用 0停用
            if (isauth != null && isauth) {
                paramMap.put("userid", userid);
            }
            if (dgimns != null && dgimns.size() > 0) {
                for (Integer type : monitorpointtypes) {
                    if (type != CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode()) {
                        paramMap.put("dgimns", dgimns);
                    } else {
                        paramMap.remove("dgimns");
                    }
                    paramMap.put("monitorPointType", type);
                    paramMap.put("monitorpointtype", type);
                    List<Map<String, Object>> onlineOutPuts = new ArrayList<>();
                    onlineOutPuts = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
                    if (onlineOutPuts != null && onlineOutPuts.size() > 0) {
                        onlineMonitorService.getOutPutPointLastOnlineDataByParamForApp(result, onlineOutPuts, type, datatype);
                    }
                }
            }
            if (result.size() > 0) {
                result = result.stream().sorted(
                        Comparator.comparingInt(
                                m -> ((Map) m).get("orderstatus") == null ? Integer.MAX_VALUE : Integer.parseInt(((Map) m).get("orderstatus").toString())
                        ).thenComparing(m -> ((Map) m).get("pollutionname") == null ? "" : ((Map) m).get("pollutionname").toString())
                ).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: xsm
     * @date: 2020/11/23 0023 上午 11:32
     * @Description: 自定义查询条件查询排口报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOutPutPointAlarmDetailDataByParamForApp", method = RequestMethod.POST)
    public Object getOutPutPointAlarmDetailDataByParamForApp(@RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "remindtypes") List<Integer> remindtypes,
                                                             @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                             @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                             @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                             @RequestJson(value = "monitorpointcategorys", required = false) List<Integer> monitorpointcategorys,
                                                             HttpServletRequest request) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<Map<String, Object>> allpoints = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnandid = new HashMap<>();
            Map<String, Object> mnandname = new HashMap<>();
            Map<String, Object> mnandshoname = new HashMap<>();
            Map<String, Object> mnandtype = new HashMap<>();
            Map<String, Object> mnandcategory = new HashMap<>();
            for (Integer type : monitorpointtypes) {
                if (dgimns != null) {
                    if (type != CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()) {
                        paramMap.put("monitorpointtype", type);
                        allpoints.addAll(onlineService.getAllPollutionOutputDgimnInfoByParam(paramMap));
                    } else {
                        if (monitorpointcategorys != null && monitorpointcategorys.size() > 0) {
                            if (dgimns != null) {
                                paramMap.put("monitorpointtype", type);
                                paramMap.put("monitorpointcategorys", monitorpointcategorys);
                                allpoints.addAll(onlineService.getAllPollutionOutputDgimnInfoByParam(paramMap));
                            }
                        }
                    }
                }
            }
            Date startDate = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.parseDate(endtime + " 23:59:59");
            //获取报警数据
            List<Map> maps = new ArrayList<>();
            List<Map> changelist = new ArrayList<>();
            List<Map> alarmlst = new ArrayList<>();
            if (allpoints != null && allpoints.size() > 0) {
                for (Map<String, Object> tempMn : allpoints) {
                    if (tempMn.get("dgimn") != null && dgimns.contains(tempMn.get("dgimn").toString())) {
                        mns.add(tempMn.get("dgimn").toString());
                        mnandid.put(tempMn.get("dgimn").toString(), tempMn.get("pk_id"));
                        mnandname.put(tempMn.get("dgimn").toString(), tempMn.get("monitorpointname"));
                        if (tempMn.get("shortername") != null) {
                            mnandshoname.put(tempMn.get("dgimn").toString(), tempMn.get("shortername"));
                        }
                        mnandtype.put(tempMn.get("dgimn").toString(), tempMn.get("monitorpointtype"));
                        if (tempMn.get("MonitorPointCategory") != null) {
                            mnandcategory.put(tempMn.get("dgimn").toString(), tempMn.get("MonitorPointCategory"));
                        }
                    }
                }
                String mnCommon;
                for (Integer remind : remindtypes) {
                    if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode() || remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                        changelist.addAll(onlineMonitorService.countMonitorPointChangeDataByParamMap(remind, mns, startDate, endDate, monitorpointtypes));
                    } else {
                        alarmlst.addAll(onlineMonitorService.getLastOutPutPointAlarmDataByParam(remind, mns, startDate, endDate, monitorpointtypes, mnandtype));
                    }
                }
                //突变
                for (Map<String, Object> dataMap : changelist) {
                    mnCommon = dataMap.get("DataGatherCode").toString();
                    Map<String, Object> objmap = new HashMap<>();
                    objmap.put("monitorpointid", mnandid.get(mnCommon));
                    objmap.put("dgimn", mnCommon);
                    objmap.put("monitorpointtype", mnandtype.get(mnCommon));
                    objmap.put("firsttime", dataMap.get("starttime"));
                    objmap.put("lasttime", dataMap.get("endtime"));
                    objmap.put("remindcode", dataMap.get("remindcode"));
                    objmap.put("remindname", dataMap.get("remindname"));
                    objmap.put("value", dataMap.get("value"));
                    objmap.put("count", dataMap.get("count"));
                    objmap.put("standardvalue", "");
                    objmap.put("name", dataMap.get("name"));
                    objmap.put("pollutantcode", dataMap.get("code"));
                    objmap.put("shortername", mnandshoname.get(mnCommon));
                    objmap.put("monitorpointname", mnandname.get(mnCommon));
                    objmap.put("continuityvalue", dataMap.get("continuityvalue"));
                    objmap.put("overmultiple", dataMap.get("overmultiple"));
                    if (mnandcategory.get(mnCommon) != null) {
                        objmap.put("category", mnandcategory.get(mnCommon));
                    } else {
                        objmap.put("category", "");
                    }
                    maps.add(objmap);
                }
                //超标 异常 超阈值
                for (Map<String, Object> dataMap : alarmlst) {
                    Map<String, Object> objmap = new HashMap<>();
                    mnCommon = dataMap.get("datagathercode").toString();
                    //String remindcode = dataMap.get("remindcode").toString();
                    objmap.put("dgimn", mnCommon);
                    objmap.put("monitorpointtype", mnandtype.get(mnCommon));
                    objmap.put("monitorpointid", mnandid.get(mnCommon));
                    objmap.put("firsttime", dataMap.get("firsttime"));
                    objmap.put("lasttime", dataMap.get("lasttime"));
                    objmap.put("remindcode", dataMap.get("remindcode"));
                    objmap.put("remindname", dataMap.get("remindname"));
                    objmap.put("value", dataMap.get("value"));
                    objmap.put("standardvalue", dataMap.get("standardvalue"));
                    objmap.put("name", dataMap.get("name"));
                    objmap.put("count", dataMap.get("count"));
                    objmap.put("pollutantcode", dataMap.get("pollutantcode"));
                    objmap.put("shortername", mnandshoname.get(mnCommon));
                    objmap.put("monitorpointname", mnandname.get(mnCommon));
                    objmap.put("continuityvalue", dataMap.get("continuityvalue"));
                    objmap.put("overmultiple", dataMap.get("overmultiple"));
                    if (mnandcategory.get(mnCommon) != null) {
                        objmap.put("category", mnandcategory.get(mnCommon));
                    } else {
                        objmap.put("category", "");
                    }
                    maps.add(objmap);
                }
            }
            //排序 监测点类型 污染源名称 监测点名称 升序
            if (maps.size() > 0) {
                List<Map> collect = maps.stream().sorted(Comparator.comparing((Map m) -> m.get("lasttime").toString()).reversed()
                        .thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
                List<Map> subDataList;
                if (pagenum != null && pagesize != null) {
                    subDataList = collect.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                } else {
                    subDataList = collect;
                }
                resultMap.put("datalist", subDataList);
                resultMap.put("total", collect.size());
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: czq
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 数据查询图表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyMonitorCharDatasByParamMap", method = RequestMethod.POST)
    public Object getManyMonitorCharDatasByParamMap(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtypes") Object monitorpointtypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {

        try {
            List<Map<String, Object>> charDataList = new ArrayList<>();
            List<Integer> types = (List<Integer>) monitorpointtypes;
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("outputids", outputids);
                List<String> mns = new ArrayList<>();
                Map<String, String> idAndName = new HashMap<>();
                Map<String, String> codeAndName = new HashMap<>();
                List<Map<String, Object>> pollutants = new ArrayList<>();
                Map<String, String> outPutIdAndMn = new HashMap<>();
                Map<String, String> outPutIdAndMn_sub;
                Map<String, Integer> mnAndType = new HashMap<>();


                for (Integer type : types) {
                    paramMap.put("monitorpointtype", type);
                    mns.addAll(onlineMonitorService.getMNListByParam(paramMap));
                    outPutIdAndMn_sub = (Map<String, String>) paramMap.get("pointidandmn");
                    for (String outId : outPutIdAndMn_sub.keySet()) {
                        mnAndType.put(outPutIdAndMn_sub.get(outId), type);
                    }
                    outPutIdAndMn.putAll(outPutIdAndMn_sub);
                    idAndName.putAll(onlineMonitorService.getOutPutIdAndPollution(outputids, type));
                    codeAndName.putAll(onlineMonitorService.getPollutantCodeAndName(pollutantcodes, type));


                }
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


                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);

                if (documents.size() > 0) {
                    paramMap.clear();
                    for (Integer type : types) {
                        paramMap.put("monitorpointtype", type);
                        paramMap.put("codes", pollutantcodes);
                        pollutants.addAll(pollutantService.getPollutantsByPollutantType(paramMap));
                    }
                }
                charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(documents, pollutants,
                        collection, outPutIdAndMn, idAndName, codeAndName, mnAndType);

            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 获取监测点环比小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointHBHourDataByParamMap", method = RequestMethod.POST)
    public Object getPointHBHourDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime) {

        try {
            List<Map<String, Object>> resultList = getPointHBData(monitorpointtypes, pollutantcode, monitortime, "BH");
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 获取监测点环比日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointHBDayDataByParamMap", method = RequestMethod.POST)
    public Object getPointHBDayDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime) {

        try {
            List<Map<String, Object>> resultList = getPointHBDayData(monitorpointtypes, pollutantcode, monitortime, "BH");
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 获取监测点环比小时浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointHBHourNDDataByParamMap", method = RequestMethod.POST)
    public Object getPointHBHourNDDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "ordertimeindex", required = false) Integer ordertimeindex,
            @RequestJson(value = "order", required = false) String order) {
        try {
            List<Map<String, Object>> resultList = getPointHBData(monitorpointtypes, pollutantcode, monitortime, "ND", ordertimeindex, order);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 获取监测点环比日浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointHBDayNDDataByParamMap", method = RequestMethod.POST)
    public Object getPointHBDayNDDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "ordertimeindex", required = false) Integer ordertimeindex,
            @RequestJson(value = "order", required = false) String order) {
        try {
            List<Map<String, Object>> resultList = getPointHBDayData(monitorpointtypes, pollutantcode, monitortime, "ND", ordertimeindex, order);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 导出监测点环比小时浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPointHBHourDNDataByParamMap", method = RequestMethod.POST)
    public void exportPointHBHourDNDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            exportPHBHDNData(monitorpointtypes, pollutantcode, monitortime, request, response, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 导出监测点环比小时浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPHBHDNDataByParamMap", method = RequestMethod.GET)
    public void exportPHBHDNDataByParamMap(
            @RequestParam(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestParam(value = "pollutantcode") String pollutantcode,
            @RequestParam(value = "monitortime") String monitortime,
            HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "ordertimeindex", required = false) Integer ordertimeindex,
            @RequestParam(value = "order", required = false) String order) throws IOException {

        try {
            exportPHBHDNData(monitorpointtypes, pollutantcode, monitortime, request, response, ordertimeindex, order);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void exportPHBHDNData(List<Integer> monitorpointtypes, String pollutantcode, String monitortime, HttpServletRequest request,
                                  HttpServletResponse response, Integer ordertimeindex, String order) throws IOException {
        List<Map<String, Object>> resultList = getPointHBData(monitorpointtypes, pollutantcode, monitortime, "NDDC", ordertimeindex, order);


        //设置导出文件数据格式
        String day = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd HH", "yyyy-MM-dd");
        List<String> times = DataFormatUtil.getYMDHBetween(day + " 00", monitortime);
        times.add(monitortime);
        List<String> headers = new ArrayList<>();
        headers.add("监测点名称/排口名称");
        for (String timeIndex : times) {
            headers.add("" + DataFormatUtil.FormatDateOneToOther(timeIndex, "yyyy-MM-dd HH", "H时") + "  ");
        }
        List<String> headersField = new ArrayList<>();
        headersField.add("monitorpointname");
        for (String timeIndex : times) {
            headersField.add(timeIndex);
        }

        String fileName = "监测浓度数据导出文件_" + new Date().getTime();
        ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 导出监测点环比小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPointHBHourDataByParamMap", method = RequestMethod.POST)
    public void exportPointHBHourDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {

            List<Map<String, Object>> resultList = getPointHBData(monitorpointtypes, pollutantcode, monitortime, "NDHB");
            //设置导出文件数据格式
            String day = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd HH", "yyyy-MM-dd");
            List<String> times = DataFormatUtil.getYMDHBetween(day + " 00", monitortime);
            times.add(monitortime);
            List<String> headers = new ArrayList<>();
            headers.add("监测点名称/排口名称");
            for (String timeIndex : times) {
                headers.add("" + DataFormatUtil.FormatDateOneToOther(timeIndex, "yyyy-MM-dd HH", "H时") + "  ");
            }
            List<String> headersField = new ArrayList<>();
            headersField.add("monitorpointname");
            for (String timeIndex : times) {
                headersField.add(timeIndex);
            }
            //设置文件名称
            String fileName = "监测浓度变化幅度导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 导出监测点环比日浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPointHBDayDNDataByParamMap", method = RequestMethod.POST)
    public void exportPointHBDayDNDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            exportPHBDDND(monitorpointtypes, pollutantcode, monitortime, request, response, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 导出监测点环比日浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPHBDDNDByParamMap", method = RequestMethod.GET)
    public void exportPHBDDNDByParamMap(
            @RequestParam(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestParam(value = "pollutantcode") String pollutantcode,
            @RequestParam(value = "monitortime") String monitortime,
            HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "ordertimeindex", required = false) Integer ordertimeindex,
            @RequestParam(value = "order", required = false) String order) throws IOException {
        try {
            exportPHBDDND(monitorpointtypes, pollutantcode, monitortime, request, response, ordertimeindex, order);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void exportPHBDDND(List<Integer> monitorpointtypes, String pollutantcode, String monitortime, HttpServletRequest request, HttpServletResponse response
            , Integer ordertimeindex, String order) throws IOException {

        List<Map<String, Object>> resultList = getPointHBDayData(monitorpointtypes, pollutantcode, monitortime, "NDDC", ordertimeindex, order);
        //设置导出文件数据格式
        String starttime = DataFormatUtil.getFirstDayOfMonth(monitortime);
        String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
        List<String> times = DataFormatUtil.getYMDBetween(starttime, endtime);
        times.add(endtime);
        List<String> headers = new ArrayList<>();
        headers.add("监测点名称/排口名称");
        for (String timeIndex : times) {
            headers.add("" + DataFormatUtil.FormatDateOneToOther(timeIndex, "yyyy-MM-dd", "d天") + "  ");
        }
        List<String> headersField = new ArrayList<>();
        headersField.add("monitorpointname");
        for (String timeIndex : times) {
            headersField.add(timeIndex);
        }

        String fileName = "监测浓度数据导出文件_" + new Date().getTime();
        ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
    }

    /**
     * @author: lip
     * @date: 2021/2/5 0005 上午 9:18
     * @Description: 导出监测点环比日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPointHBDayDataByParamMap", method = RequestMethod.POST)
    public void exportPointHBDayDataByParamMap(
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitortime") String monitortime,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {

            List<Map<String, Object>> resultList = getPointHBDayData(monitorpointtypes, pollutantcode, monitortime, "NDHB");
            //设置导出文件数据格式

            //设置导出文件数据格式
            String starttime = DataFormatUtil.getFirstDayOfMonth(monitortime);
            String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
            List<String> times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
            List<String> headers = new ArrayList<>();
            headers.add("监测点名称/排口名称");
            for (String timeIndex : times) {
                headers.add("" + DataFormatUtil.FormatDateOneToOther(timeIndex, "yyyy-MM-dd", "d天") + "  ");
            }
            List<String> headersField = new ArrayList<>();
            headersField.add("monitorpointname");
            for (String timeIndex : times) {
                headersField.add(timeIndex);
            }
            //设置文件名称
            String fileName = "监测浓度变化幅度导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultList, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getPointHBData(List<Integer> monitorpointtypes, String pollutantcode, String monitortime, String mark) {
        return getPointHBData(monitorpointtypes, pollutantcode, monitortime, mark, null, null);
    }

    private List<Map<String, Object>> getPointHBData(List<Integer> monitorpointtypes, String pollutantcode, String monitortime, String mark
            , Integer ordertimeindex, String order) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> allPointList = new ArrayList<>();
        List<Map<String, Object>> pointList;
        Map<String, Object> paramMap = new HashMap<>();
        for (Integer type : monitorpointtypes) {
            paramMap.put("outputids", Arrays.asList());
            paramMap.put("monitorpointtype", type);
            pointList = onlineService.getMonitorPointDataByParam(paramMap);
            allPointList.addAll(pointList);
        }
        if (allPointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String monitorpointname;
            for (Map<String, Object> point : allPointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndId.put(mnCommon, point.get("pk_id"));
                monitorpointname = point.get("shortername") != null ? point.get("shortername") + "-" + point.get("monitorpointname") : point.get("monitorpointname").toString();
                mnAndName.put(mnCommon, monitorpointname);
            }
            paramMap.clear();
            String day = DataFormatUtil.FormatDateOneToOther(monitortime, "yyyy-MM-dd HH", "yyyy-MM-dd");
            String hbDay = DataFormatUtil.getBeforeByDayTime(1, day);
            String startTime = hbDay + " 23";
            paramMap.put("starttime", startTime + ":00:00");
            paramMap.put("endtime", monitortime + ":59:59");
            paramMap.put("mns", mns);
            Map<String, Map<String, String>> mnAndTimeAndValue;
            if (pollutantcode.equals("aqi")) {
                paramMap.put("collection", "StationHourAQIData");
                mnAndTimeAndValue = getMnAndTimeAndAqi(paramMap);
            } else {
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("collection", "HourData");
                mnAndTimeAndValue = getMnAndTimeAndValue(paramMap, pollutantcode, "HourDataList");
            }
            Map<String, String> timeAndValue;
            List<String> times = DataFormatUtil.getYMDHBetween(day + " 00", monitortime);
            times.add(monitortime);
            for (String mnIndex : mnAndName.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitorpointid", mnAndId.get(mnIndex));
                resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                timeAndValue = mnAndTimeAndValue.get(mnIndex);
                setDataToResultMap(resultMap, timeAndValue, times, mark, monitortime);
                resultList.add(resultMap);
            }

            if (ordertimeindex == null && order == null) {
                resultList = resultList.stream().sorted(
                        Comparator.comparingDouble((Map m) -> Double.parseDouble(m.get("ordindex").toString())).reversed()
                                .thenComparing(
                                        Comparator.comparing(m -> m.get("monitorpointname").toString(), (x, y) -> {
                                            // ToFirstChar 将汉字首字母转为拼音
                                            x = FormatUtils.ToFirstChar(x).toUpperCase();
                                            y = FormatUtils.ToFirstChar(y).toUpperCase();
                                            Collator clt = Collator.getInstance();
                                            return clt.compare(x, y);
                                        })
                                )
                ).collect(Collectors.toList());
                //排序
            } else {
                int index = ordertimeindex == null ? 0 : ordertimeindex;
                Comparator<Map> mapComparator = Comparator.comparingDouble((Map m) -> {
                    String value = "";
                    if ("ND".equals(mark)) {
                        value = JSONArray.fromObject(m.get("datalist")).getJSONObject(index).getString("value");
                        String markVal = JSONArray.fromObject(m.get("datalist")).getJSONObject(index).getString("mark");
                        if (markVal != null && "-".equals(markVal) && !"".equals(value.trim())) {
                            return 0 - Double.valueOf(value);
                        }
                    } else {
                        value = index < 10 ? m.get(monitortime.substring(0, 10) + " 0" + index).toString() : m.get(monitortime.substring(0, 10) + " " + index).toString();
                        if (value.endsWith("↓")) {
                            return 0 - Double.valueOf(value.replace("↓", ""));
                        }
                        value = value.replace("↑", "");
                    }
                    return "".equals(value.trim()) ? -999999d : Double.valueOf(value);
                });
                if ("asc".equals(order)) {
                    resultList = resultList.stream().sorted(mapComparator).collect(Collectors.toList());
                } else {
                    resultList = resultList.stream().sorted(mapComparator.reversed()).collect(Collectors.toList());
                }
            }
        }
        return resultList;

    }


    private List<Map<String, Object>> getPointHBDayData(List<Integer> monitorpointtypes, String pollutantcode, String monitortime, String mark) {
        return getPointHBDayData(monitorpointtypes, pollutantcode, monitortime, mark, null, null);
    }

    private List<Map<String, Object>> getPointHBDayData(List<Integer> monitorpointtypes, String pollutantcode, String monitortime, String mark
            , Integer ordertimeindex, String order) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> allPointList = new ArrayList<>();
        List<Map<String, Object>> pointList;
        Map<String, Object> paramMap = new HashMap<>();
        for (Integer type : monitorpointtypes) {
            paramMap.put("outputids", Arrays.asList());
            paramMap.put("monitorpointtype", type);
            pointList = onlineService.getMonitorPointDataByParam(paramMap);
            allPointList.addAll(pointList);
        }
        if (allPointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String monitorpointname;
            for (Map<String, Object> point : allPointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndId.put(mnCommon, point.get("pk_id"));
                monitorpointname = point.get("shortername") != null ? point.get("shortername") + "-" + point.get("monitorpointname") : point.get("monitorpointname").toString();
                mnAndName.put(mnCommon, monitorpointname);
            }
            paramMap.clear();

            String hbMonth = DataFormatUtil.getBeforeMonthTime(1, monitortime);
            String startTime = DataFormatUtil.getLastDayOfMonth(hbMonth);
            String endTime = DataFormatUtil.getLastDayOfMonth(monitortime);

            //上个月最后一天
            paramMap.put("starttime", startTime + " 00:00:00");
            paramMap.put("endtime", endTime + " 23:59:59");
            paramMap.put("mns", mns);
            Map<String, Map<String, String>> mnAndTimeAndValue;
            if (pollutantcode.equals("aqi")) {
                paramMap.put("collection", "StationDayAQIData");
                mnAndTimeAndValue = getMnAndTimeAndAqi(paramMap);
            } else {
                paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
                paramMap.put("collection", "DayData");
                mnAndTimeAndValue = getMnAndTimeAndValue(paramMap, pollutantcode, "DayDataList");
            }
            Map<String, String> timeAndValue;
            List<String> times = DataFormatUtil.getYMDBetween(monitortime + "-01", endTime);
            times.add(endTime);
            for (String mnIndex : mnAndName.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("monitorpointid", mnAndId.get(mnIndex));
                resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                timeAndValue = mnAndTimeAndValue.get(mnIndex);
                setDayDataToResultMap(resultMap, timeAndValue, times, mark, monitortime);
                resultList.add(resultMap);
            }
            if (ordertimeindex == null && order == null) {
                resultList = resultList.stream().sorted(
                        Comparator.comparingDouble((Map m) -> Double.parseDouble(m.get("ordindex").toString())).reversed()
                                .thenComparing(
                                        Comparator.comparing(m -> m.get("monitorpointname").toString(), (x, y) -> {
                                            // ToFirstChar 将汉字首字母转为拼音
                                            x = FormatUtils.ToFirstChar(x).toUpperCase();
                                            y = FormatUtils.ToFirstChar(y).toUpperCase();
                                            Collator clt = Collator.getInstance();
                                            return clt.compare(x, y);
                                        })
                                )
                ).collect(Collectors.toList());
                //排序
            } else {
                Comparator<Map> comparator = Comparator.comparingDouble((Map m) -> {
                    String value = "";
                    if ("ND".equals(mark)) {

                        int index = ordertimeindex == null ? 0 : ordertimeindex - 1;
                        value = JSONArray.fromObject(m.get("datalist")).getJSONObject(index).getString("value");
                        String markVal = JSONArray.fromObject(m.get("datalist")).getJSONObject(index).getString("mark");
                        if (markVal != null && "-".equals(markVal) && !"".equals(value.trim())) {
                            return 0 - Double.valueOf(value);
                        }
                    } else {
                        int index = ordertimeindex == null ? 0 : ordertimeindex;
                        value = index < 10 ? m.get(monitortime + "-0" + index).toString() : m.get(monitortime + "-" + index).toString();
                        if (value.endsWith("↓")) {
                            return 0 - Double.valueOf(value.replace("↓", ""));
                        }
                        value = value.replace("↑", "");
                    }
                    return "".equals(value.trim()) ? -999999d : Double.valueOf(value);
                });
                if ("asc".equals(order)) {
                    resultList = resultList.stream().sorted(comparator).collect(Collectors.toList());
                } else {
                    resultList = resultList.stream().sorted(comparator.reversed()).collect(Collectors.toList());
                }
            }
        }
        return resultList;
    }

    private Map<String, Map<String, String>> getMnAndTimeAndAqi(Map<String, Object> paramMap) {
        List<Document> documents = onlineService.getAirMonitorDataByParamMap(paramMap);
        Map<String, Map<String, String>> mnAndTimeAndValue = new HashMap<>();
        Map<String, String> timeAndValue;
        if (documents.size() > 0) {
            String mnCommon;
            String time;
            Double value;
            String isOver = "-1";
            for (Document document : documents) {
                if (document.get("AQI") != null) {
                    mnCommon = document.getString("StationCode");
                    time = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    if (mnAndTimeAndValue.containsKey(mnCommon)) {
                        timeAndValue = mnAndTimeAndValue.get(mnCommon);
                    } else {
                        timeAndValue = new HashMap<>();
                    }
                    value = Double.parseDouble(document.get("AQI").toString());
                    timeAndValue.put(time, value + "," + isOver);
                    mnAndTimeAndValue.put(mnCommon, timeAndValue);
                }
            }
        }
        return mnAndTimeAndValue;
    }

    private Map<String, Map<String, String>> getMnAndTimeAndValue(Map<String, Object> paramMap, String pollutantcode, String dataKey) {
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        Map<String, Map<String, String>> mnAndTimeAndValue = new HashMap<>();
        Map<String, String> timeAndValue;
        if (documents.size() > 0) {
            List<Document> pollutantList;
            String mnCommon;
            String time;
            Double value;
            String isOver;
            String timeF = dataKey.equals("HourDataList") ? "yyyy-MM-dd HH" : "yyyy-MM-dd";

            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), timeF);
                pollutantList = document.get(dataKey, List.class);

                if (mnAndTimeAndValue.containsKey(mnCommon)) {
                    timeAndValue = mnAndTimeAndValue.get(mnCommon);
                } else {
                    timeAndValue = new HashMap<>();
                }
                for (Document pollutant : pollutantList) {
                    if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                        if (StringUtils.isNotBlank(pollutant.getString("AvgStrength"))) {
                            if (pollutant.getBoolean("IsOverStandard")) {
                                isOver = "4";
                            } else {
                                isOver = pollutant.get("IsOver") + "";
                            }
                            value = Double.parseDouble(pollutant.getString("AvgStrength"));
                            timeAndValue.put(time, value + "," + isOver);
                        }
                        break;
                    }
                }
                mnAndTimeAndValue.put(mnCommon, timeAndValue);
            }
        }
        return mnAndTimeAndValue;
    }

    private void setDataToResultMap(Map<String, Object> resultMap, Map<String, String> timeAndValue, List<String> times, String mark, String monitortime) {
        String value;

        String thisValueO;
        Double thisValue;

        String thatValueO;
        Double thatValue;

        Double hbValue;
        String thatTime;
        Double orderIndex;
        String first;
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (String timeIndex : times) {
            orderIndex = -999999d;
            if ("BH".equals(mark)) {
                value = "";
                if (timeAndValue != null) {

                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByHourTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);
                    if (thisValueO != null && thatValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                        if (thatValue > 0) {
                            hbValue = 100d * (thisValue - thatValue) / thatValue;
                            value = DataFormatUtil.SaveTwoAndSubZero(hbValue) + "%";
                            if (timeIndex.equals(monitortime)) {
                                orderIndex = hbValue;
                            }
                        }


                    }
                }
                resultMap.put("ordindex", orderIndex);
                resultMap.put(timeIndex, value);
            } else if ("ND".equals(mark)) {
                Map<String, Object> dataMap = new HashMap<>();
                if (timeAndValue != null) {
                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByHourTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);
                    if (thisValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        if (timeIndex.equals(monitortime)) {
                            orderIndex = thisValue;
                        }
                        if (thatValueO != null) {
                            thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                            hbValue = thisValue - thatValue;
                            if (hbValue > 0) {
                                dataMap.put("mark", "+");
                            } else if (hbValue < 0) {
                                dataMap.put("mark", "-");
                            }
                        }
                        dataMap.put("value", DataFormatUtil.SaveThreeAndSubZero(thisValue));
                        dataMap.put("isover", timeAndValue.get(timeIndex).split(",")[1]);
                    }
                }
                dataMap.putIfAbsent("value", "");
                dataMap.putIfAbsent("mark", "");
                dataMap.putIfAbsent("isover", "-1");
                dataMap.put("monitortime", timeIndex);
                dataList.add(dataMap);
                resultMap.put("datalist", dataList);
                resultMap.put("ordindex", orderIndex);
            } else if ("NDDC".equals(mark)) {
                value = "";
                first = "";
                if (timeAndValue != null) {
                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByHourTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);
                    if (thisValueO != null && thatValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                        hbValue = thisValue - thatValue;
                        if (hbValue > 0) {
                            first = "↑";
                        } else if (hbValue < 0) {
                            first = "↓";
                        }
                        if (timeIndex.equals(monitortime)) {
                            orderIndex = thisValue;
                        }
                    }
                    if (thisValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        value = thisValue + "";
                    }
                }
                resultMap.put("ordindex", orderIndex);
                resultMap.put(timeIndex, value + first);
            } else if ("NDHB".equals(mark)) {
                first = "";
                value = "";
                if (timeAndValue != null) {


                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByHourTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);

                    if (thisValueO != null && thatValueO != null) {

                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                        if (thatValue > 0) {
                            hbValue = 100d * (thisValue - thatValue) / thatValue;
                            value = DataFormatUtil.SaveTwoAndSubZero(hbValue) + "%";
                            hbValue = thisValue - thatValue;
                            if (hbValue > 0) {
                                first = "↑";
                            } else if (hbValue < 0) {
                                first = "↓";
                            }
                            if (timeIndex.equals(monitortime)) {
                                orderIndex = hbValue;
                            }
                        }
                    }
                }
                resultMap.put("ordindex", orderIndex);
                resultMap.put(timeIndex, value + first);
            }
        }

    }

    private void setDayDataToResultMap(Map<String, Object> resultMap, Map<String, String> timeAndValue, List<String> times, String mark, String monitortime) {
        String value;
        String thisValueO;
        Double thisValue;
        String thatValueO;
        Double thatValue;
        Double hbValue;
        String thatTime;
        Double orderIndex;
        String first;
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (String timeIndex : times) {
            orderIndex = -999999d;
            if ("BH".equals(mark)) {
                value = "";
                if (timeAndValue != null) {
                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByDayTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);


                    if (thisValueO != null && thatValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                        if (thatValue > 0) {
                            hbValue = 100d * (thisValue - thatValue) / thatValue;
                            value = DataFormatUtil.SaveTwoAndSubZero(hbValue) + "%";
                            if (timeIndex.equals(monitortime)) {
                                orderIndex = hbValue;
                            }
                        }

                    }
                }
                resultMap.put("ordindex", orderIndex);
                resultMap.put(timeIndex, value);
            } else if ("ND".equals(mark)) {
                Map<String, Object> dataMap = new HashMap<>();
                if (timeAndValue != null) {


                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByDayTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);


                    if (thisValueO != null) {

                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        if (timeIndex.equals(monitortime)) {
                            orderIndex = thisValue;
                        }
                        if (thatValueO != null) {
                            thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                            hbValue = thisValue - thatValue;
                            if (hbValue > 0) {
                                dataMap.put("mark", "+");
                            } else if (hbValue < 0) {
                                dataMap.put("mark", "-");
                            }
                        }
                        dataMap.put("value", DataFormatUtil.SaveThreeAndSubZero(thisValue));
                        dataMap.put("isover", timeAndValue.get(timeIndex).split(",")[1]);
                    }
                }
                dataMap.putIfAbsent("value", "");
                dataMap.putIfAbsent("mark", "");
                dataMap.putIfAbsent("isover", "");
                dataMap.put("monitortime", timeIndex);
                dataList.add(dataMap);
                resultMap.put("datalist", dataList);
                resultMap.put("ordindex", orderIndex);
            } else if ("NDDC".equals(mark)) {
                value = "";
                first = "";
                if (timeAndValue != null) {


                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByDayTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);


                    if (thisValueO != null && thatValueO != null) {

                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                        hbValue = thisValue - thatValue;
                        if (hbValue > 0) {
                            first = "↑";
                        } else if (hbValue < 0) {
                            first = "↓";
                        }
                        if (timeIndex.equals(monitortime)) {
                            orderIndex = thisValue;
                        }
                    }
                    if (thisValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        value = thisValue + "";
                    }
                }
                resultMap.put("ordindex", orderIndex);
                resultMap.put(timeIndex, value + first);
            } else if ("NDHB".equals(mark)) {
                first = "";
                value = "";
                if (timeAndValue != null) {
                    thisValueO = timeAndValue.get(timeIndex);
                    thatTime = DataFormatUtil.getBeforeByDayTime(1, timeIndex);
                    thatValueO = timeAndValue.get(thatTime);
                    if (thisValueO != null && thatValueO != null) {
                        thisValue = Double.parseDouble(thisValueO.split(",")[0]);
                        thatValue = Double.parseDouble(thatValueO.split(",")[0]);
                        if (thatValue > 0) {
                            hbValue = 100d * (thisValue - thatValue) / thatValue;
                            value = DataFormatUtil.SaveTwoAndSubZero(hbValue) + "%";
                            hbValue = thisValue - thatValue;
                            if (hbValue > 0) {
                                first = "↑";
                            } else if (hbValue < 0) {
                                first = "↓";
                            }
                            if (timeIndex.equals(monitortime)) {
                                orderIndex = hbValue;
                            }
                        }
                    }
                }
                resultMap.put("ordindex", orderIndex);
                resultMap.put(timeIndex, value + first);
            }
        }

    }

    /**
     * @author: xsm
     * @date: 2021/02/22 0022上午 11:31
     * @Description: 自定义查询某类型污染物的颜色标准值（恶臭支持传多类型）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/getMonitorPollutantStandardColorDataByParam", method = RequestMethod.POST)
    public Object getMonitorPollutantStandardColorDataByParam(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> colorDataList = navigationStandardService.getStandardColorDataByParamMap(paramMap);
            if (colorDataList != null && colorDataList.size() > 0) {
                Set<String> set = new HashSet<>();
                String level = "";
                for (Map<String, Object> map : colorDataList) {
                    map.remove("PK_ID");
                    level = map.get("StandardLevel") != null ? map.get("StandardLevel").toString() : "";
                    if (!set.contains(level)) {
                        set.add(level);
                        result.add(map);
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/02/22 0022上午 11:41
     * @Description: 自定义查询某类型所有监测设备数据发送情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/countMonitorPointDataSendStatusByParam", method = RequestMethod.POST)
    public Object countMonitorPointDataSendStatusByParam(
            @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "sortdata", required = false) Object sortdata,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allpoints = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("customname", monitorpointname);
            long total = 0;
            String orderkey = "";
            String orderstr = "";
            if (sortdata != null) {
                Map<String, Object> sortdatamap = (Map<String, Object>) sortdata;
                if (sortdatamap.size() > 0) {
                    for (String key : sortdatamap.keySet()) {
                        if (key.equals("monitorpointname") || key.equals("dgimn") || key.equals("onlinestatus")) {
                            paramMap.put("pagenum", pagenum);
                            paramMap.put("pagesize", pagesize);
                            if (sortdatamap.get(key).equals("descending")) {
                                sortdatamap.put(key, "desc");
                            } else if (sortdatamap.get(key).equals("ascending")) {
                                sortdatamap.put(key, "asc");
                            }
                            paramMap.put("sortdata", sortdatamap);
                        } else {
                            orderkey = key;
                            orderstr = sortdatamap.get(key).toString();
                        }
                    }

                } else {//无排序字段 默认按名称排(sql已加判断)
                    paramMap.put("pagenum", pagenum);
                    paramMap.put("pagesize", pagesize);
                    paramMap.put("sortdata", sortdatamap);
                }
            }
            total = deviceStatusService.getHBMonitorPointInfoNumByParamMap(paramMap);
            allpoints = deviceStatusService.getAllHBMonitorPointDataListByParam(paramMap);
            Map<String, Map<String, Object>> mnAndPointData = new HashMap<>();
            if (allpoints != null && allpoints.size() > 0) {
                for (Map<String, Object> tempMn : allpoints) {
                    if (tempMn.get("dgimn") != null && !mns.contains(tempMn.get("dgimn").toString())) {
                        mns.add(tempMn.get("dgimn").toString());
                        mnAndPointData.put(tempMn.get("dgimn").toString(), tempMn);
                    }
                }
                result = deviceStatusService.countMonitorPointDataSendStatusByParam(mns, mnAndPointData);
            }
            if (!"".equals(orderkey)) {
                String finalOrderkey = orderkey;
                if (orderstr.equals("descending")) {
                    result = result.stream().sorted(Comparator.comparing((Map m) -> m.get(finalOrderkey).toString()).reversed()).collect(Collectors.toList());
                } else if (orderstr.equals("ascending")) {
                    result = result.stream().sorted(Comparator.comparing((Map m) -> m.get(finalOrderkey).toString())).collect(Collectors.toList());
                }
                //处理分页数据
                if (pagenum != null && pagesize != null) {
                    resultMap.put("total", result.size());
                    result = getPageData(result, pagenum, pagesize);
                    resultMap.put("datalist", result);
                } else {
                    resultMap.put("datalist", result);
                }
            } else {
                resultMap.put("datalist", result);
                if (pagenum != null && pagesize != null) {
                    resultMap.put("total", total);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/4/9 0009 上午 11:29
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointname, datatype 小时传‘hour’，日传‘day’, monitorpointtypes, sortdata, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "/getMonitorPointDataSendStatusByParam", method = RequestMethod.POST)
    public Object getMonitorPointDataSendStatusByParam(
            @RequestJson(value = "outputname", required = false) String outputname,
            @RequestJson(value = "pollutionname", required = false) String pollutionname,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new LinkedList<>();
            Map<String, Object> paramMap = new HashMap<>();
            SimpleDateFormat format = new SimpleDateFormat("HH");
            Date now = new Date();
            paramMap.put("fkmonitorpointtypecodes", monitorpointtypes);
            paramMap.put("outputname", outputname);
            paramMap.put("pollutionname", pollutionname);
            List<Map<String, Object>> allHBMonitorPointDataListByParam = pollutionService.getOutPutInfosByParamMap(paramMap);
            List<String> dgimns = allHBMonitorPointDataListByParam.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            List<String> allDays = JSONObjectUtil.getAllDays(starttime, endtime);
            paramMap.put("mns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            if ("hour".equals(datatype)) {
                paramMap.put("collection", "HourData");
            } else if ("day".equals(datatype)) {
                paramMap.put("collection", "DayData");
            }
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null).
                    collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString() + "_" + FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd")));

            for (Map<String, Object> map : allHBMonitorPointDataListByParam) {
                String mn = map.get("DGIMN") == null ? "" : map.get("DGIMN").toString();
                String monitorpointname = map.get("OutputName") == null ? "" : map.get("OutputName").toString();
                String PollutionName = map.get("PollutionName") == null ? "" : map.get("PollutionName").toString();
                String FK_MonitorPointTypeName = map.get("FK_MonitorPointTypeName") == null ? "" : map.get("FK_MonitorPointTypeName").toString();
                String Status = map.get("Status") == null ? "" : map.get("Status").toString();
                for (String daytime : allDays) {
                    List<Document> documentList = collect.get(mn + "_" + daytime);
                    Map<String, Object> datamap = new HashMap<>();
                    datamap.put("outputname", monitorpointname);
                    datamap.put("monitortime", daytime);
                    datamap.put("Status", Status);
                    datamap.put("pollutionname", PollutionName);
                    datamap.put("FKMonitorPointTypeName", FK_MonitorPointTypeName);
                    if ("hour".equals(datatype)) {
                        List<String> hourTimePoints = JSONObjectUtil.getHourTimePoints(daytime, format);
                        //当天没有数据
                        if (documentList == null) {
                            List<Integer> collect1 = hourTimePoints.stream().map(m -> Integer.valueOf(m)).collect(Collectors.toList());
                            List<List<Integer>> lists = OnlineServiceImpl.groupIntegerList(collect1);
                            String line = OnlineServiceImpl.getLine(lists);
                            if (line.length() > 0) {
                                line = line.substring(0, line.length() - 1);
                                datamap.put("timepointstr", line);
                            }
                            result.add(datamap);
                            continue;
                        }
                        documentList.stream().filter(m -> m.get("MonitorTime") != null).map(m -> FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "HH")).forEach(m -> hourTimePoints.remove(m));
                        if (hourTimePoints.size() != 0) {
                            List<Integer> collect1 = hourTimePoints.stream().map(m -> Integer.valueOf(m)).collect(Collectors.toList());
                            List<List<Integer>> lists = OnlineServiceImpl.groupIntegerList(collect1);
                            String line = OnlineServiceImpl.getLine(lists);
                            if (line.length() > 0) {
                                line = line.substring(0, line.length() - 1);
                                datamap.put("timepointstr", line);
                            }
                            result.add(datamap);
                        }
                    } else if ("day".equals(datatype) && documentList == null && !daytime.equals(DataFormatUtil.getDateYMD(now))) {
                        result.add(datamap);
                    }
                }
            }
            int total = result.size();
            result = result.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            if (pagenum != null && pagesize != null) {
                result = result.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total", total);
            resultMap.put("datalist", result);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/06/10 10:10
     * @Description: 根据污染物编码和监测类型获取该类型监测点近24小时该污染物浓度数据
     * @param: code 污染物编码
     * @return:
     */
    @RequestMapping(value = "getAllMonitorPointHourTrendDataByParamMap", method = RequestMethod.POST)
    public Object getAllMonitorPointHourTrendDataByParamMap(@RequestJson(value = "code") String code,
                                                            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime

    ) {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userid);
            paramMap.put("fkmonitorpointtypecodes", monitorpointtypes);
            List<Map<String, Object>> outPutInfosByParamMap = pollutionService.getOutPutInfosByParamMap(paramMap);
            Map<String, Object> map = onlineMonitorService.getAllMonitorPointHourTrendDataByParamMap(code, outPutInfosByParamMap, starttime, endtime);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }

    @RequestMapping(value = "getmemroylog", method = RequestMethod.POST)
    public void getmemroylog() {
        String name = "helloword";
        for (int i = 0; i < 10000000; i++) {
            name += name;
        }
        System.out.println(name);
    }


    /**
     * @author: lip
     * @date: 2020/6/15 0015 下午 2:31
     * @Description: 获取最新点位最新监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointLastDataByPollutantCode", method = RequestMethod.POST)
    public Object getPointLastDataByPollutantCode(@RequestJson(value = "pollutantcode") String pollutantCode,
                                                  @RequestJson(value = "monitorpointtype") Integer monitorpointtype
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            Map<String, Object> paramMap = new HashMap<>();
            if (dgimns != null && dgimns.size() > 0) {
                formatParamMap(paramMap);
                paramMap.put("pollutantcode", pollutantCode);
                paramMap.put("dgimns", dgimns);
                paramMap.put("monitorPointType", monitorpointtype);
                onlineOutPuts = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
            } else {
                onlineOutPuts = new ArrayList<>();
            }
            if (onlineOutPuts.size() > 0) {
                resultList = onlineMonitorService.getPointLastDataByParam(onlineOutPuts, paramMap);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description:
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/1/19 10:30
     */
    @RequestMapping(value = "getLastDataByMnAndType", method = RequestMethod.POST)
    public Object getLastDataByMnAndType(@RequestJson(value = "dgimn") String dgimn,
                                         @RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("pollutanttype", monitorpointtype);
            List<Document> documents = onlineMonitorService.getLastDataByMns(Arrays.asList(dgimn));
            if (documents.size() > 0) {
                List<Map<String, Object>> pollutantList = onlineMonitorService.getPollutantListByParam(paramMap);
                if (pollutantList.size() > 0) {
                    Map<String, Object> codeAndName = new HashMap<>();
                    Map<String, Object> codeAndUnit = new HashMap<>();
                    Map<String, Object> codeAndIndex = new HashMap<>();
                    String pollutantCode;
                    for (Map<String, Object> pollutant : pollutantList) {
                        pollutantCode = pollutant.get("code") + "";
                        codeAndName.put(pollutantCode, pollutant.get("name"));
                        codeAndUnit.put(pollutantCode, pollutant.get("unit"));
                        codeAndIndex.put(pollutantCode, pollutant.get("orderindex"));
                    }
                    Document document = documents.get(0);
                    List<Document> pollutants = document.get("DataList", List.class);
                    for (Document pollutant : pollutants) {
                        Map<String, Object> resultMap = new HashMap<>();
                        pollutantCode = pollutant.getString("PollutantCode");
                        resultMap.put("pollutantcode", pollutantCode);
                        resultMap.put("pollutantname", codeAndName.get(pollutantCode));
                        resultMap.put("pollutantunit", codeAndUnit.get(pollutantCode));
                        resultMap.put("orderindex", codeAndIndex.get(pollutantCode) != null ? codeAndIndex.get(pollutantCode) : 999);
                        resultMap.put("monitorvalue", pollutant.get("AvgStrength"));
                        resultMap.put("isover", pollutant.get("IsOver"));
                        resultMap.put("isexception", pollutant.get("IsException"));
                        resultMap.put("isoverstandard", pollutant.get("IsOverStandard"));
                        resultMap.put("issuddenchange", pollutant.get("IsSuddenChange"));
                        resultList.add(resultMap);
                    }
                    //排序
                    resultList = resultList.stream().sorted(
                            Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString()))
                    ).collect(Collectors.toList());
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/25 0025 上午 10:05
     * @Description: 数据查询图表数据（多站点）（废气、烟气）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasManyMonitorCharDataByParamMap", method = RequestMethod.POST)
    public Object getGasManyMonitorCharDataByParamMap(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {

        try {
            List<Map<String, Object>> charDataList = new ArrayList<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtypes", monitorpointtypes);
                paramMap.put("outputids", outputids);
                Map<String, Object> mnmaps = onlineMonitorService.getGasMNListByParam(paramMap);
                List<String> mns = (List<String>) mnmaps.get("mns");
                Map<String, String> idAndName = (Map<String, String>) mnmaps.get("idandname");
                Map<String, String> codeAndName = onlineMonitorService.getPollutantCodeAndNameByTypes(pollutantcodes, monitorpointtypes);
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

                Map<String, String> outPutIdAndMn = (Map<String, String>) mnmaps.get("pointidandmn");
                List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);

                List<Map<String, Object>> pollutants = new ArrayList<>();
                if (documents.size() > 0) {
                    paramMap.clear();
                    paramMap.put("monitorpointtypes", monitorpointtypes);
                    paramMap.put("codes", pollutantcodes);
                    pollutants = pollutantService.getPollutantsByPollutantType(paramMap);
                }
                charDataList = MongoDataUtils.setManySomkeOutPutManyPollutantsCharDataList(documents, pollutants,
                        collection, outPutIdAndMn, idAndName, codeAndName);

            }
            return AuthUtil.parseJsonKeyToLower("success", charDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2021/01/25 0025 上午 10:33
     * @Description: 数据查询列表初始化页面数据（废气、烟气多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyGasPointListPageDataByParams", method = RequestMethod.POST)
    public Object getManyGasPointListPageDataByParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            titleMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getGasTableTitleByParam(titleMap);
            Map<String, Object> tabledata = new HashMap<>();
            if (outputids.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtypes", monitorpointtypes);
                paramMap.put("outputids", outputids);
                Map<String, Object> mnmaps = onlineMonitorService.getGasMNListByParam(paramMap);
                List<String> mns = (List<String>) mnmaps.get("mns");
                paramMap.putAll(titleMap);
                paramMap.put("mns", mns);
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
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
                List<Map<String, Object>> tableListData = onlineMonitorService.getGasTableListDataByParam(paramMap);
                tabledata.put("tablelistdata", tableListData);
                Map<String, Object> pageMap = onlineMonitorService.getPageMapForReport(paramMap);
                dataMap.putAll(pageMap);
            }
            tabledata.put("tabletitledata", tableTitleData);
            dataMap.put("tabledata", tabledata);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2021/01/25 0025 下午 1:07
     * @Description: 导出废气、烟气列表数据（多站点）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyGasOutPutReport", method = RequestMethod.POST)
    public void exportManyGasOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            HttpServletRequest request, HttpServletResponse response) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            titleMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getGasTableTitleByParam(titleMap);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("outputids", outputids);
            Map<String, Object> mnmaps = onlineMonitorService.getGasMNListByParam(paramMap);
            List<String> mns = (List<String>) mnmaps.get("mns");
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
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
            List<Map<String, Object>> tableListData = onlineMonitorService.getGasTableListDataByParam(paramMap);
            //设置导出文件数据格式
            List<Map<String, Object>> headerDataList = ExcelUtil.setManyExportHeaderDataByKey(tableTitleData);
            List<Map<String, Object>> valueDataList = ExcelUtil.setManyExportValueDataByKey(tableListData);
            //设置文件名称
            String fileName = getFileNameByType(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode());
            fileName = fileName + "监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headerDataList, valueDataList, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * @Description: 验证是否可以导出
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/11/8 14:59
     */
    @RequestMapping(value = "isExportManyGasOutPutReport", method = RequestMethod.POST)
    public Object isExportManyGasOutPutReport(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "outputids") List<String> outputids,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {

        try {
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("tableType", "many");
            titleMap.put("outputids", outputids);
            titleMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> tableTitleData = onlineMonitorService.getGasTableTitleByParam(titleMap);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("outputids", outputids);
            Map<String, Object> mnmaps = onlineMonitorService.getGasMNListByParam(paramMap);
            List<String> mns = (List<String>) mnmaps.get("mns");
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            paramMap.putAll(titleMap);
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
     * @Description: 实时数据一览（废气、烟气）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/2/11 10:07
     */
    @RequestMapping(value = "getGasLastMonitorDataByParamMap", method = RequestMethod.POST)
    public Object getGasLastMonitorDataByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            if (paramMap.get("monitorpointtypes") != null) {
                List<Integer> monitorpointtypes = (List<Integer>) paramMap.get("monitorpointtypes");
                List<Map<String, Object>> onlineOutPuts = new ArrayList<>();
                List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
                if (dgimns != null && dgimns.size() > 0) {
                    formatParamMap(paramMap);
                    paramMap.put("monitorPointType", monitorpointtypes.get(0));
                    paramMap.put("dgimns", dgimns);
                    onlineOutPuts = onlineMonitorService.getOnlineOutPutListByParamMap(paramMap);
                    resultMap = onlineMonitorService.getGasOutPutLastDataByParamMap(onlineOutPuts, monitorpointtypes, paramMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/02/11 0011 上午 11:20
     * @Description: 通过自定义条件查询在线分钟突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOnlineChangeDatasByParamMap", method = RequestMethod.POST)
    public Object getOnlineChangeDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson) throws ParseException {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            JSONObject paramMap = JSONObject.fromObject(paramsjson);
            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            List<Integer> monitortypes = (List<Integer>) paramMap.get("monitortypes");
            List<Integer> enttypes = new ArrayList<>();
            enttypes.add(WasteWaterEnum.getCode());
            enttypes.add(WasteGasEnum.getCode());
            enttypes.add(SmokeEnum.getCode());
            enttypes.add(RainEnum.getCode());
            enttypes.add(unOrganizationWasteGasEnum.getCode());
            enttypes.add(FactoryBoundarySmallStationEnum.getCode());
            enttypes.add(FactoryBoundaryStinkEnum.getCode());
            paramMap.put("userid", userid);
            //多个类型
            for (Integer type : monitortypes) {
                paramMap.put("monitortype", type);
                if (enttypes.contains(type)) {
                    monitorInfo.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (type == AirEnum.getCode()) {//大气
                    monitorInfo.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (type == WaterQualityEnum.getCode()) {//水质
                    monitorInfo.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else {//其它监测点类型
                    monitorInfo.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            //添加rtsp
            Map<String, Object> params = new HashMap<>();
            List<String> monitorpointids = monitorInfo.stream().filter(m -> m.get("pk_id") != null).map(m -> m.get("pk_id").toString()).collect(Collectors.toList());
            params.put("monitorpointids", monitorpointids);
            params.put("monitorpointtypes", monitortypes);
            List<Map<String, Object>> videoCameraInfoByParamMap = videoCameraService.getVideoCameraInfoByParamMap(params);
            for (Map<String, Object> map : monitorInfo) {
                List<Map<String, Object>> cameras = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : videoCameraInfoByParamMap) {
                    if (map.get("pk_id") != null && stringObjectMap.get("monitorpointid") != null && map.get("pk_id").toString().equals(stringObjectMap.get("monitorpointid").toString())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("rtsp", stringObjectMap.get("rtsp"));
                        data.put("id", stringObjectMap.get("PK_VedioCameraID"));
                        data.put("name", stringObjectMap.get("VedioCameraName"));
                        cameras.add(data);
                    }
                }
                map.put("rtsplist", cameras);
            }
            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("pollutanttypes", monitortypes);
            if (paramMap.get("starttime") != null) {
                paramMap.put("starttime", paramMap.get("starttime").toString() + " 00:00:00");
            }
            if (paramMap.get("endtime") != null) {
                paramMap.put("endtime", paramMap.get("endtime").toString() + " 23:59:59");
            }
            Map<String, Object> onlineDataGroupMmAndMonitortime = onlineMonitorService.getOnlineChangeDataGroupMmAndMonitortime(paramMap);
            List<Map<String, Object>> data = (List) onlineDataGroupMmAndMonitortime.get("data");
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> map = data.get(i);
                if (map.get("DataGatherCode") != null) {
                    String dataGatherCode = map.get("DataGatherCode").toString();
                    Optional<Map<String, Object>> first = monitorInfo.stream().filter(m -> m.get("dgimn") != null && m.get("dgimn").toString().equals(dataGatherCode)).findFirst();
                    if (first.isPresent()) {
                        Map<String, Object> map1 = first.get();
                        if (map1.get("pollutionname") != null) {
                            String pollutionname = map1.get("pollutionname") == null ? "" : map1.get("pollutionname").toString();
                            String shortername = map1.get("shortername") == null ? "" : map1.get("shortername").toString();
                            map.put("pollutionname", pollutionname);
                            map.put("pollutionid", map1.get("pk_pollutionid"));
                        }
                        map.put("monitorname", map1.get("monitorpointname") + (map1.get("Status") == null ? "" : "0".equals(map1.get("Status").toString()) ? "【停产】" : ""));
                        map.put("dgimn", dataGatherCode);
                        map.put("remindtype", 1);
                        map.put("remindtypename", "浓度突变");
                        map.put("outputid", map1.get("pk_id"));
                        map.put("RTSP", map1.get("RTSP"));
                        map.put("monitorpointtype", map1.get("fk_monitorpointtypecode") != null ? Integer.valueOf(map1.get("fk_monitorpointtypecode").toString()) : null);
                    }
                }
            }
            List<Map<String, Object>> collect = data.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            resultMap.put("total", onlineDataGroupMmAndMonitortime.get("total"));
            resultMap.put("datalist", collect);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/11 0011 上午 11:20
     * @Description: 通过自定义条件查询在线分钟突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "exportOnlineChangeDatasByParamMap", method = RequestMethod.POST)
    public void exportOnlineChangeDatasByParamMap(@RequestJson(value = "paramsjson") Object paramsjson,
                                                  HttpServletResponse response, HttpServletRequest request) throws ParseException, IOException {
        try {
            JSONObject paramMap = JSONObject.fromObject(paramsjson);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<String> dgimns = new ArrayList<>();
            List<Map<String, Object>> monitorInfo = new ArrayList<>();
            List<Integer> monitortypes = (List<Integer>) paramMap.get("monitortypes");
            List<Integer> enttypes = new ArrayList<>();
            enttypes.add(WasteWaterEnum.getCode());
            enttypes.add(WasteGasEnum.getCode());
            enttypes.add(SmokeEnum.getCode());
            enttypes.add(RainEnum.getCode());
            enttypes.add(unOrganizationWasteGasEnum.getCode());
            enttypes.add(FactoryBoundarySmallStationEnum.getCode());
            enttypes.add(FactoryBoundaryStinkEnum.getCode());
            paramMap.put("userid", userid);
            //多个类型
            for (Integer type : monitortypes) {
                paramMap.put("monitortype", type);
                if (enttypes.contains(type)) {
                    monitorInfo.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
                } else if (type == AirEnum.getCode()) {//大气
                    monitorInfo.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
                } else if (type == WaterQualityEnum.getCode()) {//水质
                    monitorInfo.addAll(waterStationService.getWaterStationByParamMap(paramMap));
                } else {
                    monitorInfo.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
                }
            }
            dgimns = monitorInfo.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("pollutanttypes", monitortypes);
            if (paramMap.get("starttime") != null) {
                paramMap.put("starttime", paramMap.get("starttime").toString() + " 00:00:00");
            }
            if (paramMap.get("endtime") != null) {
                paramMap.put("endtime", paramMap.get("endtime").toString() + " 23:59:59");
            }
            paramMap.put("dgimns", dgimns);
            Map<String, Object> datamap = onlineMonitorService.getOnlineChangeDataGroupMmAndMonitortime(paramMap);
            List<Map<String, Object>> allData = (List) datamap.get("data");
            for (int i = 0; i < allData.size(); i++) {
                Map<String, Object> map = allData.get(i);
                if (map.get("DataGatherCode") != null) {
                    String dataGatherCode = map.get("DataGatherCode").toString();
                    Optional<Map<String, Object>> first = monitorInfo.stream().filter(m -> m.get("dgimn") != null && m.get("dgimn").toString().equals(dataGatherCode)).findFirst();
                    if (first.isPresent()) {
                        Map<String, Object> map1 = first.get();
                        if (map1.get("pollutionname") != null) {
                            String pollutionname = map1.get("pollutionname") == null ? "" : map1.get("pollutionname").toString();
                            map.put("pollutionname", pollutionname);
                            map.put("pollutionid", map1.get("pk_pollutionid"));
                        }
                        map.put("monitorname", map1.get("monitorpointname") + (map1.get("Status") == null ? "" : "0".equals(map1.get("Status").toString()) ? "【停产】" : ""));
                    }
                }
            }
            List<Map<String, Object>> collect = allData.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
            Map<String, Object> excelTitleInfo = getAllOnlineChangeExcelTitle(monitortypes.get(0));
            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", (LinkedList) excelTitleInfo.get("headers"), (LinkedList) excelTitleInfo.get("headersfield"), collect, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
            ExcelUtil.downLoadExcel(excelTitleInfo.get("excelname").toString(), response, request, bytesForWorkBook);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> getAllOnlineChangeExcelTitle(Integer monitortype) {
        Map<String, Object> excelTitleInfo = new HashMap<>();
        LinkedList<String> headers = new LinkedList<>();
        headers.add("日期");
        headers.add("突增时段");
        headers.add("突增幅度");
        LinkedList<String> headersField = new LinkedList<>();
        headersField.add("monitortime");
        headersField.add("noread");
        headersField.add("flowrate");
        String excelname = "";
        String partname = "";
        partname = FlowChangeEnum.getName() + "预警";
        switch (getCodeByInt(monitortype)) {
            case WasteWaterEnum:
                excelname = "废水" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case WasteGasEnum:
                excelname = "废气" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case SmokeEnum:
                excelname = "烟气" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case RainEnum:
                excelname = "雨水" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case unOrganizationWasteGasEnum:
                excelname = "无组织废气" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case FactoryBoundarySmallStationEnum:
                excelname = "厂界小型站" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case FactoryBoundaryStinkEnum:
                excelname = "厂界恶臭" + partname;
                headers.addFirst("排口名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;

            case AirEnum:
                excelname = "大气" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case EnvironmentalVocEnum:
                excelname = "VOC" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case EnvironmentalStinkEnum:
                excelname = "恶臭" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case MicroStationEnum:
                excelname = "微站" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;

            case EnvironmentalDustEnum:
                excelname = "扬尘" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
                break;
            case WaterQualityEnum:
                excelname = "水质" + partname;
                headers.addFirst("监测点名称");
                headersField.addFirst("monitorname");
            case StorageTankAreaEnum:
                excelname = "储罐" + partname;
                headers.addFirst("储罐编码");
                headers.addFirst("区域名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("storagetankareaname");
            case SecurityLeakageMonitor:
                excelname = "安全泄露" + partname;
                headers.addFirst("监测点名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case SecurityCombustibleMonitor:
                excelname = "可燃易爆气体" + partname;
                headers.addFirst("监测点名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
            case SecurityToxicMonitor:
                excelname = "有毒有害气体" + partname;
                headers.addFirst("监测点名称");
                headers.addFirst("企业名称");
                headersField.addFirst("monitorname");
                headersField.addFirst("pollutionname");
                break;
        }

        excelTitleInfo.put("headers", headers);
        excelTitleInfo.put("headersfield", headersField);
        excelTitleInfo.put("excelname", excelname);
        return excelTitleInfo;
    }
}
