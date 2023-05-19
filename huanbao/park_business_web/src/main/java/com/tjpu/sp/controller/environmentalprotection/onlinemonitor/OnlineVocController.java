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
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.pollutantvaluescope.PollutantValueScopeService;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: zhangzc
 * @date: 2019/5/28 15:50
 * @Description: 在线VOC监测数据
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineVoc")
public class OnlineVocController {

    private final OnlineMonitorService onlineMonitorService;
    private final OnlineService onlineService;
    private final PollutantService pollutantService;
    private final OtherMonitorPointService otherMonitorPointService;

    public OnlineVocController(OnlineService onlineService, PollutantService pollutantService, OtherMonitorPointService otherMonitorPointService,OnlineMonitorService onlineMonitorService) {
        this.onlineService = onlineService;
        this.pollutantService = pollutantService;
        this.otherMonitorPointService = otherMonitorPointService;
        this.onlineMonitorService = onlineMonitorService;
    }

    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private PollutantValueScopeService pollutantValueScopeService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;

    private final String DB_HourFlowData = "HourFlowData";
    /**
     * voc监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode();

    /**
     * @author: zhangzc
     * @date: 2019/5/22 15:16
     * @Description: 动态条件查询每个Voc监测点最新一条监测数据列表数据
     * @param:
     * @return: DataGatherCode   MonitorTime
     */
    @RequestMapping(value = "getVocLastDatasByParamMap", method = RequestMethod.POST)
    public Object getVocLastDatasByParamMap(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            List<Map<String, Object>> onlineOutPuts;
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            if (dgimns != null && dgimns.size() > 0) {
                OnlineController.formatParamMapForRealTimeSee(paramMap);
                paramMap.put("monitorPointType", monitorPointTypeCode);
                paramMap.put("dgimns", dgimns);
                if (paramMap.get("monitorpointcategory") != null && !"".equals(paramMap.get("monitorpointcategory"))) {
                    Integer monitorpointcategory = Integer.parseInt(paramMap.get("monitorpointcategory").toString());
                    paramMap.put("monitorPointCategorys", Arrays.asList(monitorpointcategory));
                }
                onlineOutPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取VOC站点监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocMonitorDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getVocMonitorDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        List<Map<String, Object>> charDataList = new ArrayList<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(monitorpointid);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(outputids, monitorPointTypeCode, new HashMap<>());
            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
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
            paramMap.put("sort", "asc");
            Boolean isPage = false;
            if (pagenum != null && pagesize != null) {
                paramMap.put("pagenum", pagenum);
                paramMap.put("pagesize", pagesize);
                paramMap.put("sort", "desc");
                isPage = true;
            }
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个空气站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneVocListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneVocListPageDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(monitorpointid);


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
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个空气站点列表内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOneVocListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getOneVocListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            List<String> outputids = new ArrayList<>();
            outputids.add(monitorpointid);
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
    @RequestMapping(value = "getManyVocListPageDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyVocListPageDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointids") List<String> monitorpointids,
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
            titleMap.put("outputids", monitorpointids);

            List<Map<String, Object>> tabletitledata = onlineService.getTableTitleForReport(titleMap);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(monitorpointids, monitorPointTypeCode, new HashMap<>());
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
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取多个空气站点列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getManyVocListDataByDataMarkAndParams", method = RequestMethod.POST)
    public Object getManyVocListDataByDataMarkAndParams(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "monitorpointids") List<String> monitorpointids,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Integer reportType = 2;

            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(monitorpointids, monitorPointTypeCode, new HashMap<>());

            Map<String, Object> paramMap = new HashMap<>();
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);

            paramMap.put("outputids", monitorpointids);
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
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description: 获取voc污染类别累加数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/7 13:19
     */
    @RequestMapping(value = "getVocCategorySumDataByParam", method = RequestMethod.POST)
    public Object getVocCategorySumDataByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime

    ) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantList.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                Map<Integer, List<String>> categoryAndCodes = new HashMap<>();
                Map<String, Integer> codeAndCategory = new HashMap<>();
                List<String> codes;
                String pollutantCode;
                Integer pollutantCategory;
                for (Map<String, Object> pollutant : pollutantList) {
                    if (pollutant.get("code") != null && pollutant.get("pollutantcategory") != null) {
                        pollutantCode = pollutant.get("code").toString();
                        pollutantCategory = Integer.parseInt(pollutant.get("pollutantcategory").toString());
                        pollutantcodes.add(pollutantCode);
                        if (categoryAndCodes.containsKey(pollutantCategory)) {
                            codes = categoryAndCodes.get(pollutantCategory);
                        } else {
                            codes = new ArrayList<>();
                        }
                        codes.add(pollutantCode);
                        codeAndCategory.put(pollutantCode, pollutantCategory);
                        categoryAndCodes.put(pollutantCategory, codes);
                    }
                }
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);

                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);

                paramMap.put("collection", collection);
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }

                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Map<String, Map<Integer, Double>> timeAndCategoryAndValue = new HashMap<>();
                    Map<Integer, Double> categoryAndValue;
                    String monitorTime;
                    Double value;
                    List<Document> pollutants;
                    String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                    String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                    for (Document document : documents) {
                        monitorTime = MongoDataUtils.getMonitorTimeByCollection(collection, document.getDate("MonitorTime"));
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (pollutantcodes.contains(pollutantCode) && pollutant.get(valueKey) != null) {
                                value = Double.parseDouble(pollutant.getString(valueKey));
                                pollutantCategory = codeAndCategory.get(pollutantCode);
                                if (timeAndCategoryAndValue.containsKey(monitorTime)) {
                                    categoryAndValue = timeAndCategoryAndValue.get(monitorTime);
                                } else {
                                    categoryAndValue = new HashMap<>();
                                }
                                if (categoryAndValue.containsKey(pollutantCategory)) {
                                    categoryAndValue.put(pollutantCategory, categoryAndValue.get(pollutantCategory) + value);
                                } else {
                                    categoryAndValue.put(pollutantCategory, value);
                                }
                                timeAndCategoryAndValue.put(monitorTime, categoryAndValue);
                            }
                        }
                    }
                    for (Integer index : categoryAndCodes.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("categorycode", index);
                        resultMap.put("categoryname", CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(index));
                        List<Map<String, Object>> dataList = new ArrayList<>();
                        for (String timeIndex : timeAndCategoryAndValue.keySet()) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitortime", timeIndex);
                            categoryAndValue = timeAndCategoryAndValue.get(timeIndex);
                            dataMap.put("monitorvalue", DataFormatUtil.SaveTwoAndSubZero(categoryAndValue.get(index)));
                            dataList.add(dataMap);
                        }
                        //排序
                        dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
                        resultMap.put("datalist", dataList);
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


    /**
     * @Description: 获取voc关联企业排放信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/20 15:20
     */
    @RequestMapping(value = "getVocRelationFlowDataByParam", method = RequestMethod.POST)
    public Object getVocRelationFlowDataByParam(
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (pollutantcodes.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("pollutantcodes", pollutantcodes);
                //根据分析因子获取特征企业下的设备号
                List<Map<String, Object>> entList = otherMonitorPointService.getVocRelationDgimnByParam(paramMap);
                List<String> mns = new ArrayList<>();
                Map<String, String> mnAndId = new HashMap<>();
                Map<String, Object> idAndName = new HashMap<>();
                String mnCommon;
                String pollutionid;
                for (Map<String, Object> entMap : entList) {
                    mnCommon = entMap.get("dgimn").toString();
                    mns.add(mnCommon);
                    pollutionid = entMap.get("pk_pollutionid").toString();
                    mnAndId.put(mnCommon, pollutionid);
                    idAndName.put(pollutionid, entMap.get("pollutionname"));
                }
                paramMap.clear();
                paramMap.put("starttime", starttime + ":00:00");
                paramMap.put("endtime", endtime + ":59:59");
                paramMap.put("mns", mns);
                paramMap.put("collection", DB_HourFlowData);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                //获取排放量
                Map<String, Double> idAndFlow = new HashMap<>();
                Double flow;
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutionid = mnAndId.get(mnCommon);
                    flow = document.get("TotalFlow") != null ? Double.parseDouble(document.getString("TotalFlow")) : 0;
                    idAndFlow.put(pollutionid, idAndFlow.get(pollutionid) != null ? idAndFlow.get(pollutionid) + flow : flow);
                }
                for (String idIndex : idAndFlow.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("pollutionid", idIndex);
                    resultMap.put("pollutionname", idAndName.get(idIndex));
                    resultMap.put("flow", idAndFlow.get(idIndex));
                    resultList.add(resultMap);
                }
                //排序
                if (resultList.size() > 0) {
                    Comparator<Object> doubleS = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("flow").toString())).reversed();
                    resultList = resultList.stream().sorted(doubleS).collect(Collectors.toList());
                    if (resultList.size() > 10) {
                        resultList = getPageData(resultList, 1, 10);
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
     * @Description: 获取voc污染类别堆叠数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/7 13:19
     */
    @RequestMapping(value = "getVocCategoryStackDataByParam", method = RequestMethod.POST)
    public Object getVocCategoryStackDataByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime

    ) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantList.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                Map<Integer, List<String>> categoryAndCodes = new HashMap<>();
                Map<String, Integer> codeAndCategory = new HashMap<>();
                List<String> codes;
                String pollutantCode;
                Integer pollutantCategory;
                for (Map<String, Object> pollutant : pollutantList) {
                    if (pollutant.get("code") != null && pollutant.get("pollutantcategory") != null) {
                        pollutantCode = pollutant.get("code").toString();
                        pollutantCategory = Integer.parseInt(pollutant.get("pollutantcategory").toString());
                        pollutantcodes.add(pollutantCode);
                        if (categoryAndCodes.containsKey(pollutantCategory)) {
                            codes = categoryAndCodes.get(pollutantCategory);
                        } else {
                            codes = new ArrayList<>();
                        }
                        codes.add(pollutantCode);
                        codeAndCategory.put(pollutantCode, pollutantCategory);
                        categoryAndCodes.put(pollutantCategory, codes);
                    }
                }
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);

                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);

                paramMap.put("collection", collection);
                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }

                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Map<String, Map<Integer, Double>> timeAndCategoryAndValue = new HashMap<>();
                    Map<Integer, Double> categoryAndValue;
                    String monitorTime;
                    Double value;
                    List<Document> pollutants;
                    String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                    String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                    for (Document document : documents) {
                        monitorTime = MongoDataUtils.getMonitorTimeByCollection(collection, document.getDate("MonitorTime"));
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (pollutantcodes.contains(pollutantCode) && pollutant.get(valueKey) != null) {
                                value = Double.parseDouble(pollutant.getString(valueKey));
                                pollutantCategory = codeAndCategory.get(pollutantCode);
                                if (timeAndCategoryAndValue.containsKey(monitorTime)) {
                                    categoryAndValue = timeAndCategoryAndValue.get(monitorTime);
                                } else {
                                    categoryAndValue = new HashMap<>();
                                }
                                if (categoryAndValue.containsKey(pollutantCategory)) {
                                    categoryAndValue.put(pollutantCategory, categoryAndValue.get(pollutantCategory) + value);
                                } else {
                                    categoryAndValue.put(pollutantCategory, value);
                                }
                                timeAndCategoryAndValue.put(monitorTime, categoryAndValue);
                            }
                        }
                    }

                    if (timeAndCategoryAndValue.size() > 0) {
                        for (String timeIndex : timeAndCategoryAndValue.keySet()) {
                            Map<String, Object> resultMap = new HashMap<>();
                            List<Map<String, Object>> dataList = new ArrayList<>();
                            resultMap.put("monitortime", timeIndex);
                            categoryAndValue = timeAndCategoryAndValue.get(timeIndex);
                            if (categoryAndValue != null) {
                                for (Integer index : categoryAndValue.keySet()) {
                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("categorycode", index);
                                    dataMap.put("categoryname", CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(index));
                                    dataMap.put("monitorvalue", DataFormatUtil.SaveTwoAndSubZero(categoryAndValue.get(index)));
                                    dataList.add(dataMap);
                                }
                            }
                            resultMap.put("datalist", dataList);
                            resultList.add(resultMap);
                        }
                    }
                    resultList = resultList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description: 获取voc污染类别堆叠数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/7 13:19
     */
    @RequestMapping(value = "getVocCategorySubRateDataByParam", method = RequestMethod.POST)
    public Object getVocCategorySubRateDataByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime

    ) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantList.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                Map<Integer, List<String>> categoryAndCodes = new HashMap<>();
                Map<String, Integer> codeAndCategory = new HashMap<>();
                List<String> codes;
                String pollutantCode;
                Integer pollutantCategory;
                for (Map<String, Object> pollutant : pollutantList) {
                    if (pollutant.get("code") != null && pollutant.get("pollutantcategory") != null) {
                        pollutantCode = pollutant.get("code").toString();
                        pollutantCategory = Integer.parseInt(pollutant.get("pollutantcategory").toString());
                        pollutantcodes.add(pollutantCode);
                        if (categoryAndCodes.containsKey(pollutantCategory)) {
                            codes = categoryAndCodes.get(pollutantCategory);
                        } else {
                            codes = new ArrayList<>();
                        }
                        codes.add(pollutantCode);
                        codeAndCategory.put(pollutantCode, pollutantCategory);
                        categoryAndCodes.put(pollutantCategory, codes);
                    }
                }
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);
                paramMap.put("collection", collection);

                long totalCount = onlineMonitorService.getMongodbCountByParam(paramMap);
                boolean isMany = DataFormatUtil.dataIsMany(totalCount);
                if (isMany) {
                    String flag = ReturnInfo.other_many.split("#")[0];
                    String message = ReturnInfo.other_many.split("#")[1];
                    return AuthUtil.returnObject(flag, message);
                }
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Map<String, Map<Integer, Double>> timeAndCategoryAndValue = new HashMap<>();
                    Map<Integer, Double> categoryAndValue;
                    String monitorTime;
                    Double value;
                    List<Document> pollutants;
                    String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                    String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                    for (Document document : documents) {
                        monitorTime = MongoDataUtils.getMonitorTimeByCollection(collection, document.getDate("MonitorTime"));
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (pollutantcodes.contains(pollutantCode) && pollutant.get(valueKey) != null) {
                                value = Double.parseDouble(pollutant.getString(valueKey));
                                pollutantCategory = codeAndCategory.get(pollutantCode);
                                if (timeAndCategoryAndValue.containsKey(monitorTime)) {
                                    categoryAndValue = timeAndCategoryAndValue.get(monitorTime);
                                } else {
                                    categoryAndValue = new HashMap<>();
                                }
                                if (categoryAndValue.containsKey(pollutantCategory)) {
                                    categoryAndValue.put(pollutantCategory, categoryAndValue.get(pollutantCategory) + value);
                                } else {
                                    categoryAndValue.put(pollutantCategory, value);
                                }
                                timeAndCategoryAndValue.put(monitorTime, categoryAndValue);
                            }
                        }
                    }

                    if (timeAndCategoryAndValue.size() > 0) {
                        for (String timeIndex : timeAndCategoryAndValue.keySet()) {
                            Map<String, Object> resultMap = new HashMap<>();
                            List<Map<String, Object>> dataList = new ArrayList<>();
                            resultMap.put("monitortime", timeIndex);
                            categoryAndValue = timeAndCategoryAndValue.get(timeIndex);
                            if (categoryAndValue != null) {
                                Double total = getTotalValue(categoryAndValue);
                                for (Integer index : categoryAndValue.keySet()) {
                                    value = categoryAndValue.get(index);
                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("categorycode", index);
                                    dataMap.put("categoryname", CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(index));
                                    dataMap.put("ratedata", DataFormatUtil.SaveTwoAndSubZero(value * 100 / total) + "%");
                                    dataList.add(dataMap);
                                }
                            }
                            resultMap.put("datalist", dataList);
                            resultList.add(resultMap);
                        }
                    }
                    resultList = resultList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private Double getTotalValue(Map<Integer, Double> categoryAndValue) {
        Double total = 0d;
        for (Integer index : categoryAndValue.keySet()) {
            total += categoryAndValue.get(index);
        }
        return total;
    }


    /**
     * @Description: 获取voc污染类别占比数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/7 13:19
     */
    @RequestMapping(value = "getVocCategoryRateDataByParam", method = RequestMethod.POST)
    public Object getVocCategoryRateDataByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantList.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                Map<Integer, List<String>> categoryAndCodes = new HashMap<>();
                Map<String, Integer> codeAndCategory = new HashMap<>();
                List<String> codes;
                String pollutantCode;
                Integer pollutantCategory;
                for (Map<String, Object> pollutant : pollutantList) {
                    if (pollutant.get("code") != null && pollutant.get("pollutantcategory") != null) {
                        pollutantCode = pollutant.get("code").toString();
                        pollutantCategory = Integer.parseInt(pollutant.get("pollutantcategory").toString());
                        pollutantcodes.add(pollutantCode);
                        if (categoryAndCodes.containsKey(pollutantCategory)) {
                            codes = categoryAndCodes.get(pollutantCategory);
                        } else {
                            codes = new ArrayList<>();
                        }
                        codes.add(pollutantCode);
                        codeAndCategory.put(pollutantCode, pollutantCategory);
                        categoryAndCodes.put(pollutantCategory, codes);
                    }
                }
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                Double total = 0d;
                if (documents.size() > 0) {
                    Map<Integer, Double> categoryAndValue = new HashMap<>();
                    Double value;
                    List<Document> pollutants;
                    String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                    String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                    for (Document document : documents) {
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (pollutantcodes.contains(pollutantCode) && pollutant.get(valueKey) != null) {
                                value = Double.parseDouble(pollutant.getString(valueKey));
                                total += value;
                                pollutantCategory = codeAndCategory.get(pollutantCode);
                                if (categoryAndValue.containsKey(pollutantCategory)) {
                                    categoryAndValue.put(pollutantCategory, categoryAndValue.get(pollutantCategory) + value);
                                } else {
                                    categoryAndValue.put(pollutantCategory, value);
                                }
                            }
                        }
                    }
                    for (Integer index : categoryAndValue.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("categorycode", index);
                        resultMap.put("categoryname", CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(index));
                        value = categoryAndValue.get(index);
                        resultMap.put("ratedata", total > 0 ? DataFormatUtil.SaveTwoAndSubZero(value * 100 / total) + "%" : "0%");
                        if (total > 0) {
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
     * @Description: 获取voc因子OFP, SOA数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/7 13:19
     */
    @RequestMapping(value = "getVocOFPAndSOADataByParam", method = RequestMethod.POST)
    public Object getVocOFPAndSOADataByParam(
            @RequestJson(value = "datamark") Integer datamark,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutantList.size() > 0) {
                List<String> pollutantcodes = new ArrayList<>();
                Map<String, Double> codeAndMir = new HashMap<>();
                Map<String, Double> codeAndFac = new HashMap<>();
                Map<String, String> codeAndName = new HashMap<>();
                String pollutantCode;
                for (Map<String, Object> pollutant : pollutantList) {
                    if (pollutant.get("code") != null) {
                        pollutantCode = pollutant.get("code").toString();
                        if (pollutant.get("mir") != null) {
                            pollutantcodes.add(pollutantCode);
                            codeAndMir.put(pollutantCode, Double.parseDouble(pollutant.get("mir").toString()));
                        }
                        if (pollutant.get("fac") != null) {
                            pollutantcodes.add(pollutantCode);
                            codeAndFac.put(pollutantCode, Double.parseDouble(pollutant.get("fac").toString()));
                        }
                        codeAndName.put(pollutantCode, pollutant.get("name") + "");
                    }
                }
                starttime = MongoDataUtils.setStartTimeByDataMark(datamark, starttime);
                endtime = MongoDataUtils.setEndTimeByDataMark(datamark, endtime);
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", Arrays.asList(dgimn));
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(datamark);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    Double value;
                    List<Document> pollutants;
                    String dataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);
                    String valueKey = MongoDataUtils.getValueKeyByCollection(collection);
                    Map<String, Double> codeAndSumD = new HashMap<>();
                    for (Document document : documents) {
                        pollutants = document.get(dataKey, List.class);
                        for (Document pollutant : pollutants) {
                            pollutantCode = pollutant.getString("PollutantCode");
                            if (pollutantcodes.contains(pollutantCode) && pollutant.get(valueKey) != null) {
                                value = Double.parseDouble(pollutant.getString(valueKey));
                                if (codeAndSumD.containsKey(pollutantCode)) {
                                    codeAndSumD.put(pollutantCode, codeAndSumD.get(pollutantCode) + value);
                                } else {
                                    codeAndSumD.put(pollutantCode, value);
                                }
                            }
                        }
                    }
                    List<Map<String, Object>> ofpList = new ArrayList<>();
                    List<Map<String, Object>> soaList = new ArrayList<>();
                    for (String code : codeAndSumD.keySet()) {
                        if (codeAndFac.containsKey(code)) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("pollutantcode", code);
                            dataMap.put("pollutantname", codeAndName.get(code));
                            dataMap.put("value", DataFormatUtil.SaveTwoAndSubZero(codeAndFac.get(code) * codeAndSumD.get(code)));
                            ofpList.add(dataMap);
                        }
                    }
                    for (String code : codeAndSumD.keySet()) {
                        if (codeAndMir.containsKey(code)) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("pollutantcode", code);
                            dataMap.put("pollutantname", codeAndName.get(code));
                            dataMap.put("value", DataFormatUtil.SaveTwoAndSubZero(codeAndMir.get(code) * codeAndSumD.get(code)));
                            soaList.add(dataMap);
                        }
                    }
                    Comparator<Object> doubleS = Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("value").toString())).reversed();
                    if (ofpList.size() > 0) {//排序，取前十
                        ofpList = ofpList.stream().sorted(doubleS).collect(Collectors.toList());
                        if (ofpList.size() > 10) {
                            ofpList = getPageData(ofpList, 1, 10);
                        }
                    }
                    if (soaList.size() > 0) {//排序，取前十
                        soaList = soaList.stream().sorted(doubleS).collect(Collectors.toList());
                        if (soaList.size() > 10) {
                            soaList = getPageData(soaList, 1, 10);
                        }
                    }
                    resultMap.put("ofp", ofpList);
                    resultMap.put("soa", soaList);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出单个VOC站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportOneVocOutPutReport", method = RequestMethod.POST)
    public void exportOneVocOutPutReport(
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
            String fileName = "VOC监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数导出多个VOC站点报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportManyVocOutPutReport", method = RequestMethod.POST)
    public void exportManyVocOutPutReport(
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
            String fileName = "VOC监测数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tableListData, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: zhangzc
     * @date: 2019/5/30 10:51
     * @Description: 根据污染物编码获取各VOC监测点近24小时该污染物浓度数据
     * @param: code 污染物编码
     * @return:
     */
    @RequestMapping(value = "get24HourMonitorDataByPollutantCodeForVOC", method = RequestMethod.POST)
    public Object get24HourMonitorDataByPollutantCodeForVOC(@RequestJson(value = "code") String code,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime
    ) {
        try {

            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> map = onlineService.get24HourMonitorDataByPollutantCodeForMonitorPoint(code, monitorPointTypeCode, starttime, endtime, userid);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:24
     * @Description: 获取恶臭污染物超标预警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocPollutantEarlyAndOverAlarmsByParamMap", method = RequestMethod.POST)
    public Object getVocPollutantEarlyAndOverAlarmsByParamMap(
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
            paramMap.put("monitorPointType", monitorPointTypeCode);
            List<Map<String, Object>> outputs = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
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
     * @Description: 获取voc污染物预警/超标/异常详情页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datatype-数据类型标记：1-实时，2-分钟，3-小时，4-日 counttype-统计类型标记：1-预警，2-超标，3-异常
     * @return:
     */
    @RequestMapping(value = "getVocPollutantEarlyOrOverOrExceptionDetailsPage", method = RequestMethod.POST)
    public Object getVocPollutantEarlyOrOverOrExceptionDetailsPage(
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
    @RequestMapping(value = "exportVocPollutantEarlyOrOverOrExceptionDetailsData", method = RequestMethod.POST)
    public void exportVocPollutantEarlyOrOverOrExceptionDetailsData(
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
            String fileName = "VOC污染物报警详情导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 3:44
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取VOC多点位多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getVocManyOutPutManyPollutantMonitorDataByParams(
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
            charDataList = MongoDataUtils.setManyOutPutManyPollutantsCharDataList(
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
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询VOC污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocPollutantUpRushDataByParams", method = RequestMethod.POST)
    public Object getVocPollutantUpRushDataByParams(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("monitorPointType", monitorPointTypeCode);
            List<Map<String, Object>> outPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
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
     * @Description: 自定义查询条件导出VOC污染物突增监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportVocPollutantUpRushDataByParams", method = RequestMethod.POST)
    public void exportVocPollutantUpRushDataByParams(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> resultMap = new HashMap<>();
            if (paramMap == null) {
                paramMap = new HashMap<>();
            }
            Integer datatype = 3;//查询小时数据
            String collection = MongoDataUtils.getCollectionByDataMark(datatype);
            paramMap.put("monitorPointType", monitorPointTypeCode);
            List<Map<String, Object>> outPuts = otherMonitorPointService.getOnlineOtherPointInfoByParamMap(paramMap);
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
            String fileName = "VOC污染物突增数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, tablelistdata, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/31 10:40
     * @Description: 条件查询VOC浓度污染物突增列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVOCChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getVOCChangeWarnDetailParams(@RequestJson(value = "dgimn") String dgimn,
                                               @RequestJson(value = "collectiontype", required = false) Integer collectiontype,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                               @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            int monitortype = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode();
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
     * @Description: 获取VOC浓度突增污染物
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
            Integer monitortype = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode();
            Integer remindtype = CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitortype);
            paramMap.put("collectiontype", collectiontype);
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
     * @Description: 获取VOC单个污染物浓度突增数据
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
//     * @Description: 导出VOC浓度突增数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [pollutionname, starttime, outputname, endtime, pagesize, pagenum]
//     * @throws:
//     */
//    @RequestMapping(value = "exportVOCChangeWarnListByParams", method = RequestMethod.POST)
//    public void exportVOCChangeWarnListByParams(@RequestJson(value = "starttime") String starttime,
//                                                @RequestJson(value = "outputname", required = false) String outputname, @RequestJson(value = "endtime") String endtime,
//                                                @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "pagenum", required = false) Integer pagenum,
//                                                HttpServletResponse response, HttpServletRequest request
//    ) throws Exception {
//        try {
//
//            Object VOCChangeWarnListByParams = getVOCChangeWarnListByParams(starttime, outputname, endtime, Integer.MAX_VALUE, 1);
//
//            JSONObject jsonObject = JSONObject.fromObject(VOCChangeWarnListByParams);
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
//            headers.add("监测点名称");
//            headers.add("日期");
//            headers.add("突增时段");
//            headers.add("突增幅度");
//            List<String> headersField = new ArrayList<>();
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
//                ExcelUtil.downLoadExcel("VOC浓度突变预警", response, request, bytesForWorkBook);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }

    /**
     * @author: xsm
     * @date: 2020/11/17 4:51
     * @Description: 获取VOC某因子组某段时间各因子小时浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocPollutantConcentrationDataByParam", method = RequestMethod.POST)
    public Object getVocPollutantConcentrationDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                          @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
                                                          @RequestJson(value = "pollutantcategory") Integer pollutantcategory,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("pollutantcategory", pollutantcategory);
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            if (pollutantcodes != null) {
                param.put("pollutantcodes", pollutantcodes);
            }
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandunit = new HashMap<>();
            List<String> codes = new ArrayList<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null) {
                        codes.add(map.get("pollutantcode").toString());
                        codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                        if (map.get("pollutantunit") != null) {
                            codeandunit.put(map.get("pollutantcode").toString(), map.get("pollutantunit"));
                        }
                    }
                }
            }
            param.put("dgimn", dgimn);
            param.put("codes", codes);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("codeandname", codeandname);
            param.put("codeandunit", codeandunit);
            List<Map<String, Object>> result = otherMonitorPointService.getVocPollutantConcentrationDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/18 10:31
     * @Description: 根据自定义参数统计VOC因子组占比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countVocFactorGroupProportionDataByParam", method = RequestMethod.POST)
    public Object countVocFactorGroupProportionDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                           @RequestJson(value = "datemark") String datemark,
                                                           @RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime,
                                                           @RequestJson(value = "secondstarttime") String secondstarttime,
                                                           @RequestJson(value = "secondendtime") String secondendtime) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            Map<String, Object> codeandcategory = new HashMap<>();
            List<String> codes = new ArrayList<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null && map.get("pollutantcategory") != null) {
                        codes.add(map.get("pollutantcode").toString());
                        codeandcategory.put(map.get("pollutantcode").toString(), map.get("pollutantcategory"));
                    }
                }
            }
            param.put("codes", codes);
            param.put("dgimn", dgimn);
            param.put("datemark", datemark);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("secondstarttime", secondstarttime);
            param.put("secondendtime", secondendtime);
            param.put("codeandcategory", codeandcategory);
            Map<String, Object> result = otherMonitorPointService.countVocFactorGroupProportionDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/19 08:50
     * @Description: 根据自定义参数统计VOC因子OFP排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countVocPollutantOFPRankDataByParam", method = RequestMethod.POST)
    public Object countVocPollutantOFPRankDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      @RequestJson(value = "secondstarttime") String secondstarttime,
                                                      @RequestJson(value = "secondendtime") String secondendtime) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            Map<String, Object> codeandname = new HashMap<>();
            List<String> codes = new ArrayList<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null && map.get("pollutantname") != null) {
                        codes.add(map.get("pollutantcode").toString());
                        codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    }
                }
            }
            param.put("codes", codes);
            param.put("dgimn", dgimn);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("secondstarttime", secondstarttime);
            param.put("secondendtime", secondendtime);
            param.put("codeandname", codeandname);
            List<Map<String, Object>> result = otherMonitorPointService.countVocPollutantOFPRankDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/19 15:50
     * @Description: 获取VOC污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllVocPollutantData", method = RequestMethod.POST)
    public Object getAllVocPollutantData(@RequestJson(value = "dgimn") String dgimn) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", pollutants);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/26 16:53
     * @Description: 获取某VOC点位单个或多个污染物的小时浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocPollutantHourConcentrationDataByParam", method = RequestMethod.POST)
    public Object getVocPollutantHourConcentrationDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime,
                                                              @RequestJson(value = "pollutantcodes") List<String> pollutantcodes
    ) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            param.put("codes", pollutantcodes);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            Map<String, Object> result = otherMonitorPointService.getVocPollutantHourConcentrationDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/27 10:28
     * @Description: 根据自定义参数统计VOC因子组各小时浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countVocPointFactorGroupHourDataByParam", method = RequestMethod.POST)
    public Object countVocPointFactorGroupHourDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                          @RequestJson(value = "datamark") String datamark,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime
    ) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            Map<String, Object> codeandcategory = new HashMap<>();
            List<String> codes = new ArrayList<>();
            Set<String> factorgroup = new HashSet<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null && map.get("pollutantcategory") != null) {
                        if (map.get("pollutantcategory") != null) {
                            factorgroup.add(map.get("pollutantcategory").toString());
                        }
                        codes.add(map.get("pollutantcode").toString());
                        codeandcategory.put(map.get("pollutantcode").toString(), map.get("pollutantcategory"));
                    }
                }
            }
            param.put("datamark", datamark);//"1" 时序分析（根据监测时间）  "2":昼夜分析（当天）
            param.put("factorgroup", factorgroup);
            param.put("codes", codes);
            param.put("dgimn", dgimn);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("codeandcategory", codeandcategory);
            Map<String, Object> result = otherMonitorPointService.countVocPointFactorGroupHourDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/27 14:05
     * @Description: 根据自定义参数统计VOC因子组各小时浓度数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointVocPollutantDataByParam", method = RequestMethod.POST)
    public Object getMonitorPointVocPollutantDataByParam(@RequestJson(value = "vocdata") Object vocdata,
                                                         @RequestJson(value = "stinkdata") Object stinkdata,
                                                         @RequestJson(value = "gasdata") Object gasdata,
                                                         @RequestJson(value = "datemark") String datemark,
                                                         @RequestJson(value = "monitortime") String monitortime

    ) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> voclist = (List<Map<String, Object>>) vocdata;
            List<Map<String, Object>> otherlist = (List<Map<String, Object>>) stinkdata;
            otherlist.addAll((List<Map<String, Object>>) gasdata);
            List<String> vocmns = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            Map<String, String> mnandname = new HashMap<>();
            List<String> timelist = new ArrayList<>();
            if (voclist.size() > 0) {
                for (Map<String, Object> vocmap : voclist) {
                    vocmns.add(vocmap.get("dgimn").toString());
                    mnandname.put(vocmap.get("dgimn").toString(), vocmap.get("name").toString());
                }
            }
            if (otherlist.size() > 0) {
                for (Map<String, Object> othermap : otherlist) {
                    mns.add(othermap.get("dgimn").toString());
                    mnandname.put(othermap.get("dgimn").toString(), othermap.get("name").toString());
                }
            }
            if ("minute".equals(datemark)) {
                timelist = DataFormatUtil.getYMDHMBetween(monitortime + " 00:00", monitortime + " 23:59");//时间组
                timelist.add(monitortime + " 23:59");
            } else if ("hour".equals(datemark)) {
                timelist = DataFormatUtil.getYMDHBetween(monitortime + " 00", monitortime + " 23");//时间组
                timelist.add(monitortime + " 23");
            }
            //获取voc点位监测的因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimns", vocmns);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            param.put("datemark", datemark);//"minute" 分钟  "hour":小时
            param.put("pollutants", pollutants);
            param.put("vocmns", vocmns);
            param.put("mns", mns);
            param.put("monitortime", monitortime);
            param.put("mnandname", mnandname);
            param.put("timelist", timelist);
            result.put("timelist", timelist);
            List<Map<String, Object>> listdata = new ArrayList<>();
            if (vocmns.size() > 0) {
                listdata.addAll(otherMonitorPointService.counVocPollutantSumDataByParam(param));
            }
            if (mns.size() > 0) {
                listdata.addAll(otherMonitorPointService.getStinkAndGasOutPutPollutantDataByParam(param));
            }
            result.put("valuedata", listdata);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/11/30 0030 下午 1:07
     * @Description: 根据监测类型获取点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointDataByTypeForVocAnalysis", method = RequestMethod.POST)
    public Object getMonitorPointDataByTypeForVocAnalysis(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) throws Exception {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Integer monitorpointtype : monitorpointtypes) {
                    dataList.addAll(otherMonitorPointService.getMonitorPointDataByTypeForVocAnalysis(monitorpointtype, paramMap));
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/12/7 0007 上午 09:53
     * @Description: 自定义查询条件查询VOC与废气相关性列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocRelationListDataByParams", method = RequestMethod.POST)
    public Object getVocRelationListDataByParams(@RequestJson(value = "datamark") Integer datamark,
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
            resultMap = onlineService.getVocRelationListDataByParamMap(outPuts, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/12/7 0007 上午 10:38
     * @Description: 自定义查询条件查询Voc与废气相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVocAndGasRelationChartDataByParams", method = RequestMethod.POST)
    public Object getVocAndGasRelationChartDataByParams(@RequestJson(value = "datamark") Integer datamark,
                                                        @RequestJson(value = "outputid") String outputid,
                                                        @RequestJson(value = "monitorpointmn") String monitorpointmn,
                                                        @RequestJson(value = "monitorpointpollutant") String monitorpointpollutant,
                                                        @RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "outputpollutant") String outputpollutant,
                                                        @RequestJson(value = "beforetime") Integer beforetime,
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
            if (pagesize != null && pagenum != null) {
                paramMap.put("pagesize", pagesize);
                paramMap.put("pagenum", pagenum);
            }
            resultMap = onlineService.getVocAndGasRelationChartDataByParamMap(outPuts, paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/05/23 13:30
     * @Description: 根据自定义参数统计站点VOCs因子时段均值排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countVocPollutantVocsAvgValueRankDataByParam", method = RequestMethod.POST)
    public Object countVocPollutantVocsAvgValueRankDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                               @RequestJson(value = "starttime") String starttime,
                                                               @RequestJson(value = "endtime") String endtime,
                                                               @RequestJson(value = "datatype") String datatype) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            Map<String, Object> codeandname = new HashMap<>();
            List<String> codes = new ArrayList<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null && map.get("pollutantname") != null) {
                        codes.add(map.get("pollutantcode").toString());
                        codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    }
                }
            }
            param.put("codes", codes);
            param.put("dgimn", dgimn);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("datatype", datatype);
            param.put("codeandname", codeandname);
            List<Map<String, Object>> result = otherMonitorPointService.countVocPollutantVocsAvgValueRankDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/23 13:30
     * @Description: 根据自定义参数统计站点化学活性Loh排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countVocPollutantLohRankDataByParam", method = RequestMethod.POST)
    public Object countVocPollutantLohRankDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      @RequestJson(value = "datatype") String datatype) {
        try {
            //获取因子
            Map<String, Object> param = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            List<Map<String, Object>> pollutants = otherMonitorPointService.getAllVocPollutantByParam(param);
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandkohi = new HashMap<>();
            List<String> codes = new ArrayList<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null && map.get("pollutantname") != null &&
                            map.get("kohi") != null && !"".equals(map.get("kohi").toString())) {
                        codeandkohi.put(map.get("pollutantcode").toString(), map.get("kohi"));
                        codes.add(map.get("pollutantcode").toString());
                        codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    }
                }
            }
            param.put("codes", codes);
            param.put("dgimn", dgimn);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("datatype", datatype);
            param.put("codeandname", codeandname);
            param.put("codeandkohi", codeandkohi);
            List<Map<String, Object>> result = otherMonitorPointService.countVocPollutantLohRankDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
