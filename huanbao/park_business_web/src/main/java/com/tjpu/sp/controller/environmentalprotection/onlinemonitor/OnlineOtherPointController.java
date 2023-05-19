package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMonitorService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineOtherPointService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: xsm
 * @date: 2022/04/20 16:04
 * @Description: 其它监测类型通用接口控制层
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineOtherPoint")
public class OnlineOtherPointController {

    @Autowired
    private OnlineOtherPointService onlineOtherPointService;

    @Autowired
    private OnlineMonitorService onlineMonitorService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private PollutantService pollutantService;

    private final String DB_HourData = "HourData";
    private final String DB_DayData = "DayData";

    /**
     * @author: xsm
     * @date: 2022/04/20 0020 下午 16:06
     * @Description: 获取某点位某时间段内各污染物小时浓度趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, datatype：数据类型（hour/day）， starttime, endtime:根据数据类型 时间传到小时和日 ,aircode：常规空气污染物,dgimns]
     * @throws:
     */
    @RequestMapping(value = "getOtherMonitorPointHourOnlineDataByParams", method = RequestMethod.POST)
    public Object getOtherMonitorPointHourOnlineDataByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
            @RequestJson(value = "parkorpoint", required = false) String parkorpoint,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "aircode", required = false) String aircode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcategorys", required = false) List<Integer> pollutantcategorys

    ) {
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimns", dgimns);
            List<Map<String, Object>> onlineOutPuts = otherMonitorPointService.getOtherPointInfoAndAirMNByParamMap(paramMap);
            paramMap.put("ispartakecount", 1);
            //获取该类型监测的污染物
            paramMap.put("pollutanttype", monitorpointtype);
            paramMap.put("codes", pollutantcodes);
            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                paramMap.put("pollutantcategorys", pollutantcategorys);
            }
            List<Map<String, Object>> pollutantlist = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (parkorpoint == null) {
                parkorpoint = "point";//默认查站点关联的空气站  PM2.5/PM10
            }
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Integer> codeAndType = new HashMap<>();
            Map<String, Object> mnandname = new HashMap<>();
            Map<String, Object> airmnandname = new HashMap<>();
            Map<String, Object> mnandairmn = new HashMap<>();
            List<String> codes = new ArrayList<>();
            List<String> airmns = new ArrayList<>();
            for (Map<String, Object> map : onlineOutPuts) {
                if (map.get("dgimn") != null) {
                    mnandname.put(map.get("dgimn").toString(), map.get("monitorpointname"));
                    if (map.get("airmn") != null) {
                        airmns.add(map.get("airmn").toString());
                        airmnandname.put(map.get("airmn").toString(), map.get("airname"));
                        mnandairmn.put(map.get("dgimn").toString(), map.get("airmn"));
                    }
                }
            }
            String pollutantCode;
            for (Map<String, Object> map : pollutantlist) {
                if (map.get("code") != null) {
                    pollutantCode = map.get("code").toString();
                    if (!codes.contains(pollutantCode)) {
                        codes.add(pollutantCode);
                    }
                    codeandname.put(pollutantCode, map.get("name"));
                    if (map.get("pollutantcategory") != null) {
                        codeAndType.put(pollutantCode, Integer.parseInt(map.get("pollutantcategory") + ""));
                    }
                }
            }
            paramMap.put("mnandairmn", mnandairmn);//其它监测点MN 关联空气点MN
            paramMap.put("parkorpoint", parkorpoint);//园区/点位 标记
            paramMap.put("mnandname", mnandname);//颗粒物MN和名称
            paramMap.put("datatype", datatype);
            paramMap.put("codes", codes);
            paramMap.put("airmns", airmns);//关联空气MN
            paramMap.put("airmnandname", airmnandname);//空气点名称
            if (StringUtils.isNotBlank(aircode)) {
                paramMap.put("aircode", aircode);//空气污染物
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("codeandname", codeandname);
            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                paramMap.put("monitorpointtype", monitorpointtype);
                paramMap.put("codeAndType", codeAndType);
                result = onlineOtherPointService.getOtherOnlineGroupDataByParams(paramMap);
            } else {
                result = onlineOtherPointService.getOtherMonitorPointHourOnlineDataByParams(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取监测因子占比数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/21 10:59
     */
    @RequestMapping(value = "getPollutantRateDataByParam", method = RequestMethod.POST)
    public Object getPollutantRateDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
            @RequestJson(value = "pollutantcategorys", required = false) List<Integer> pollutantcategorys
    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimns", dgimns);
            paramMap.put("ispartakecount", 1);
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                paramMap.put("pollutantcategorys", pollutantcategorys);
            }
            //获取该点位监测的污染物
            List<Map<String, Object>> pollutantlist = onlineOtherPointService.getPointMonitorPollutantDataParamMap(paramMap);

            Map<String, Map<String, Object>> mnAndCodeAndName = new HashMap<>();
            Map<String, Object> codeAndName;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            Map<String, Integer> codeAndType = new HashMap<>();
            String mnCommon;
            String pollutantCode;
            pollutantcodes = new ArrayList<>();
            ;
            for (Map<String, Object> map : pollutantlist) {
                if (map.get("pollutantcode") != null) {
                    mnCommon = map.get("dgimn").toString();
                    codeAndName = mnAndCodeAndName.get(mnCommon) != null ? mnAndCodeAndName.get(mnCommon) : new HashMap<>();
                    pollutantCode = map.get("pollutantcode").toString();
                    codeAndName.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    mnAndCodeAndName.put(mnCommon, codeAndName);
                    mnAndId.put(mnCommon, map.get("monitorpointid"));
                    mnAndName.put(mnCommon, map.get("monitorpointname"));
                    pollutantcodes.add(pollutantCode);
                    if (map.get("pollutantcategory") != null) {
                        codeAndType.put(pollutantCode, Integer.parseInt(map.get("pollutantcategory") + ""));
                    }

                }
            }
            String collection;
            String dataKey;
            if (timetype.equals("1")) {
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
                collection = DB_HourData;
                dataKey = "HourDataList";
            } else {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                collection = DB_DayData;
                dataKey = "DayDataList";
            }
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datakey", dataKey);
            paramMap.put("collection", collection);
            List<Document> documents = onlineOtherPointService.getMongoDBListByParam(paramMap);

            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                resultList = getGroupDataList(documents, mnAndId, mnAndName, codeAndType, monitorpointtype);
            } else {
                resultList = getPollutantDataList(documents, mnAndId, mnAndName, mnAndCodeAndName);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 站点臭氧污染多维分析（烷烃、烯烃、芳香烃，空气：温度、湿度、O3、NO2、Ox(O3+NO2)）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/6/7 13:19
     */
    @RequestMapping(value = "getVocO3DataByParam", method = RequestMethod.POST)
    public Object getVocO3DataByParam(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            Integer monitorpointtype = CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode();
            paramMap.put("pollutanttype", monitorpointtype);
            List<Integer> pollutantcategorys = Arrays.asList(CommonTypeEnum.VocPollutantFactorGroupEnum.AlkaneEnum.getCode(),
                    CommonTypeEnum.VocPollutantFactorGroupEnum.OlefinEnum.getCode(),
                    CommonTypeEnum.VocPollutantFactorGroupEnum.AromaticHydrocarbonEnum.getCode());
            paramMap.put("pollutantcategorys", pollutantcategorys);
            List<Map<String, Object>> pollutantList = pollutantService.getPollutantsByCodesAndType(paramMap);
            List<String> timeList = DataFormatUtil.getYMDHBetween(starttime, endtime);
            timeList.add(endtime);
            List<String> pollutantcodes = new ArrayList<>();
            String pollutantCode;
            String collection = DB_HourData;
            starttime = starttime + ":00:00";
            endtime = endtime + ":59:59";
            String dataKey = "HourDataList";
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datakey", dataKey);
            paramMap.put("collection", collection);
            if (pollutantList.size() > 0) {
                Map<String, Integer> codeAndType = new HashMap<>();
                Integer type;
                for (Map<String, Object> pollutant : pollutantList) {
                    if (pollutant.get("code") != null) {
                        pollutantCode = pollutant.get("code").toString();
                        type = Integer.parseInt(pollutant.get("pollutantcategory").toString());
                        codeAndType.put(pollutantCode, type);
                        pollutantcodes.add(pollutantCode);
                    }
                }
                paramMap.put("pollutantcodes", pollutantcodes);
                paramMap.put("dgimns", Arrays.asList(dgimn));
                List<Document> documents = onlineOtherPointService.getMongoDBListByParam(paramMap);
                Map<Integer, Map<String, Double>> typeAndTimeAndTotal = new HashMap<>();
                Map<String, Double> timeAndTotal;
                List<Document> valueList;
                Double svalue;
                String time;
                for (Document document : documents) {
                    pollutantCode = document.getString("PollutantCode");
                    valueList = document.get("valuelist", List.class);
                    type = codeAndType.get(pollutantCode);
                    timeAndTotal = typeAndTimeAndTotal.get(type) != null ? typeAndTimeAndTotal.get(type) : new HashMap<>();
                    for (Document value : valueList) {
                        svalue = value.get("value") != null ? Double.parseDouble(value.get("value").toString()) : 0d;
                        time = DataFormatUtil.getDateYMDH(value.getDate("time"));
                        timeAndTotal.put(time, timeAndTotal.get(time) != null ? timeAndTotal.get(time) + svalue : svalue);
                    }
                    typeAndTimeAndTotal.put(type, timeAndTotal);
                }
                for (Integer typeIndex : typeAndTimeAndTotal.keySet()) {
                    Map<String, Object> vocMap = new HashMap<>();
                    vocMap.put("countcode", typeIndex);
                    vocMap.put("countname", getGroupName(monitorpointtype, typeIndex));
                    timeAndTotal = typeAndTimeAndTotal.get(typeIndex);
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    for (String timeIndex : timeList) {
                        Map<String, Object> dataMap = new HashMap<>();
                        if (timeAndTotal.get(timeIndex) != null) {
                            dataMap.put("value", DataFormatUtil.SaveThreeAndSubZero(timeAndTotal.get(timeIndex)));
                        } else {
                            dataMap.put("value", "");
                        }
                        dataMap.put("time", timeIndex);
                        dataList.add(dataMap);
                    }
                    vocMap.put("datalist", dataList);
                    resultList.add(vocMap);
                }
            }
            //关联空气站数据
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimns", Arrays.asList(dgimn));
            List<Map<String, Object>> airPoint = otherMonitorPointService.getOtherPointInfoAndAirMNByParamMap(paramMap);
            if (airPoint.size() > 0) {
                List<String> airmns = new ArrayList<>();
                pollutantcodes = CommonTypeEnum.O3PollutionEnum.getAllCodes();
                for (Map<String, Object> map : airPoint) {
                    if (map.get("airmn") != null) {
                        airmns.add(map.get("airmn").toString());
                    }
                }
                paramMap.put("dgimns", airmns);
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineOtherPointService.getMongoDBListByParam(paramMap);
                List<String> NOX = Arrays.asList(
                        CommonTypeEnum.O3PollutionEnum.O3Enum.getCode(),
                        CommonTypeEnum.O3PollutionEnum.NO2Enum.getCode());
                List<Document> valueList;
                String time;
                Double svalue;
                Map<String, Double> timeAndNOXTotal = new HashMap<>();
                for (Document document : documents) {
                    pollutantCode = document.getString("PollutantCode");
                    valueList = document.get("valuelist", List.class);
                    Map<String, Double> timeAndTotal = new HashMap<>();
                    for (Document value : valueList) {
                        svalue = value.get("value") != null ? Double.parseDouble(value.get("value").toString()) : 0d;
                        time = DataFormatUtil.getDateYMDH(value.getDate("time"));
                        timeAndTotal.put(time, svalue);
                    }
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    for (String timeIndex : timeList) {
                        Map<String, Object> dataMap = new HashMap<>();
                        if (timeAndTotal.get(timeIndex) != null) {
                            dataMap.put("value", DataFormatUtil.SaveThreeAndSubZero(timeAndTotal.get(timeIndex)));
                        } else {
                            dataMap.put("value", "");
                        }
                        dataMap.put("time", timeIndex);
                        dataList.add(dataMap);
                    }
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("countcode", pollutantCode);
                    resultMap.put("countname", CommonTypeEnum.O3PollutionEnum.getNameByCode(pollutantCode));
                    resultMap.put("datalist", dataList);
                    //NOX
                    if (NOX.contains(pollutantCode)) {
                        for (Document value : valueList) {
                            svalue = value.get("value") != null ? Double.parseDouble(value.get("value").toString()) : 0d;
                            time = DataFormatUtil.getDateYMDH(value.getDate("time"));
                            timeAndNOXTotal.put(time, timeAndNOXTotal.get(time) != null ? timeAndNOXTotal.get(time) + svalue : svalue);
                        }
                    }
                    resultList.add(resultMap);

                }

                List<Map<String, Object>> dataList = new ArrayList<>();
                for (String timeIndex : timeList) {
                    Map<String, Object> dataMap = new HashMap<>();
                    if (timeAndNOXTotal.get(timeIndex) != null) {
                        dataMap.put("value", DataFormatUtil.SaveThreeAndSubZero(timeAndNOXTotal.get(timeIndex)));
                    } else {
                        dataMap.put("value", "");
                    }
                    dataMap.put("time", timeIndex);
                    dataList.add(dataMap);
                }
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("countcode", "Ox");
                resultMap.put("countname", "Ox");
                resultMap.put("datalist", dataList);
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @Description: 获取监测因子占比数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/21 10:59
     */
    @RequestMapping(value = "getPollutantCategory", method = RequestMethod.POST)
    public Object getPollutantCategory(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype
    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("ispartakecount", 1);
            //获取该点位监测的污染物
            List<Map<String, Object>> pollutantlist = onlineOtherPointService.getPollutantDataParamMap(paramMap);
            Map<Integer, List<Map<String, Object>>> typeAndPollutant = pollutantlist.stream().filter(m -> m != null && m.get("pollutantcategory") != null).collect(Collectors.groupingBy(m -> Integer.parseInt(m.get("pollutantcategory").toString())));
            for (Integer typeIndex : typeAndPollutant.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("typecode", typeIndex);
                resultMap.put("typename", getGroupName(monitorpointtype, typeIndex));
                resultMap.put("pollutantlist", typeAndPollutant.get(typeIndex));
                resultList.add(resultMap);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private List<Map<String, Object>> getGroupDataList(List<Document> documents, Map<String, Object> mnAndId,
                                                       Map<String, Object> mnAndName,
                                                       Map<String, Integer> codeAndType,
                                                       Integer monitorpointtype) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<Integer, Double>> mnAndTypeAndTotal = new HashMap<>();
        Map<String, Double> mnAndTotal = new HashMap<>();
        Map<Integer, Double> typeAndTotal;
        if (documents.size() > 0) {
            Double svalue;
            String mnCommon;
            String pollutantCode;
            List<Document> valueList;
            Integer type;
            for (Document document : documents) {
                Double total = 0D;
                mnCommon = document.getString("DataGatherCode");
                typeAndTotal = mnAndTypeAndTotal.get(mnCommon) != null ? mnAndTypeAndTotal.get(mnCommon) : new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                valueList = document.get("valuelist", List.class);
                type = codeAndType.get(pollutantCode);
                for (Document value : valueList) {
                    svalue = value.get("value") != null ? Double.parseDouble(value.get("value").toString()) : 0d;
                    total += svalue;
                }
                typeAndTotal.put(type, typeAndTotal.get(type) != null ? typeAndTotal.get(type) + total : total);
                mnAndTypeAndTotal.put(mnCommon, typeAndTotal);
                mnAndTotal.put(mnCommon, mnAndTotal.get(mnCommon) != null ? mnAndTotal.get(mnCommon) + total : total);
            }
        }
        for (String mnIndex : mnAndId.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("monitorpointid", mnAndId.get(mnIndex));
            resultMap.put("monitorpointname", mnAndName.get(mnIndex));
            typeAndTotal = mnAndTypeAndTotal.get(mnIndex);
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            if (typeAndTotal != null) {
                Double rate;
                for (Integer code : typeAndTotal.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutantcode", code);
                    dataMap.put("pollutantname", getGroupName(monitorpointtype, code));
                    rate = 100d * typeAndTotal.get(code) / mnAndTotal.get(mnIndex);
                    dataMap.put("rate", DataFormatUtil.SaveOneAndSubZero(rate));
                    pollutantList.add(dataMap);
                }
            }
            resultMap.put("pollutantlist", pollutantList);
            resultList.add(resultMap);
        }
        return resultList;
    }


    private Object getGroupName(Integer monitorpointtype, Integer key) {
        String name = "未定义";
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case TZFEnum:
            case XKLWEnum:
                break;
            case EnvironmentalVocEnum:
                name = CommonTypeEnum.VocPollutantFactorGroupEnum.getNameByCode(key);
                break;
        }
        return name;
    }

    private List<Map<String, Object>> getPollutantDataList(List<Document> documents,
                                                           Map<String, Object> mnAndId,
                                                           Map<String, Object> mnAndName,
                                                           Map<String, Map<String, Object>> mnAndCodeAndName) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Double>> mnAndCodeAndTotal = new HashMap<>();
        Map<String, Double> mnAndTotal = new HashMap<>();
        Map<String, Double> codeAndTotal;
        if (documents.size() > 0) {
            Double svalue;
            String mnCommon;
            String pollutantCode;

            List<Document> valueList;
            for (Document document : documents) {
                Double total = 0D;
                mnCommon = document.getString("DataGatherCode");
                codeAndTotal = mnAndCodeAndTotal.get(mnCommon) != null ? mnAndCodeAndTotal.get(mnCommon) : new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                valueList = document.get("valuelist", List.class);
                for (Document value : valueList) {
                    svalue = value.get("value") != null ? Double.parseDouble(value.get("value").toString()) : 0d;
                    total += svalue;
                }
                codeAndTotal.put(pollutantCode, total);
                mnAndCodeAndTotal.put(mnCommon, codeAndTotal);
                mnAndTotal.put(mnCommon, mnAndTotal.get(mnCommon) != null ? mnAndTotal.get(mnCommon) + total : total);

            }
        }
        Map<String, Object> codeAndName;
        for (String mnIndex : mnAndId.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("monitorpointid", mnAndId.get(mnIndex));
            resultMap.put("monitorpointname", mnAndName.get(mnIndex));
            codeAndTotal = mnAndCodeAndTotal.get(mnIndex);
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            if (codeAndTotal != null) {
                Double rate;
                codeAndName = mnAndCodeAndName.get(mnIndex);
                for (String code : codeAndTotal.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutantcode", code);
                    dataMap.put("pollutantname", codeAndName.get(code));
                    if (mnAndTotal.get(mnIndex) > 0) {
                        rate = 100d * codeAndTotal.get(code) / mnAndTotal.get(mnIndex);
                    } else {
                        rate = 0d;
                    }


                    dataMap.put("rate", DataFormatUtil.SaveOneAndSubZero(rate));
                    pollutantList.add(dataMap);
                }
            }
            resultMap.put("pollutantlist", pollutantList);
            resultList.add(resultMap);
        }
        return resultList;
    }


    /**
     * @Description: 获取监测因子数组数据（箱型图、因子均值图）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/21 10:59
     */
    @RequestMapping(value = "getPollutantListDataByParam", method = RequestMethod.POST)
    public Object getPollutantListDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
            @RequestJson(value = "pollutantcategorys", required = false) List<Integer> pollutantcategorys,
            @RequestJson(value = "isavg", required = false) String isavg
    ) {
        try {

            List<Map<String, Object>> resultList;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimns", dgimns);
            paramMap.put("ispartakecount", 1);
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                paramMap.put("pollutantcategorys", pollutantcategorys);
            }
            //获取该点位监测的污染物
            List<Map<String, Object>> pollutantlist = onlineOtherPointService.getPointMonitorPollutantDataParamMap(paramMap);
            Map<String, Map<String, Object>> mnAndCodeAndName = new HashMap<>();
            Map<String, Object> codeAndName;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String mnCommon;
            String pollutantCode;
            pollutantcodes = new ArrayList<>();
            Map<String, Integer> codeAndType = new HashMap<>();
            for (Map<String, Object> map : pollutantlist) {
                if (map.get("pollutantcode") != null) {
                    mnCommon = map.get("dgimn").toString();
                    codeAndName = mnAndCodeAndName.get(mnCommon) != null ? mnAndCodeAndName.get(mnCommon) : new HashMap<>();
                    pollutantCode = map.get("pollutantcode").toString();
                    codeAndName.put(map.get("pollutantcode").toString(), map.get("pollutantname") + "#"
                            + (map.get("orderindex") != null ? map.get("orderindex") : -99));
                    mnAndCodeAndName.put(mnCommon, codeAndName);
                    mnAndId.put(mnCommon, map.get("monitorpointid"));
                    mnAndName.put(mnCommon, map.get("monitorpointname"));
                    pollutantcodes.add(pollutantCode);
                    if (map.get("pollutantcategory") != null) {
                        codeAndType.put(pollutantCode, Integer.parseInt(map.get("pollutantcategory") + ""));
                    }

                }
            }
            String collection;
            String dataKey;
            if (timetype.equals("1")) {
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
                collection = DB_HourData;
                dataKey = "HourDataList";
            } else {
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                collection = DB_DayData;
                dataKey = "DayDataList";
            }
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datakey", dataKey);
            paramMap.put("collection", collection);
            List<Document> documents = onlineOtherPointService.getMongoDBListByParam(paramMap);

            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                resultList = getGroupLDataList(documents, mnAndId, mnAndName, codeAndType, monitorpointtype, isavg);
            } else {
                resultList = getPollutantLDataList(documents, mnAndId, mnAndName, mnAndCodeAndName, isavg,monitorpointtype);
            }

            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取监测因子数组数据（箱型图、因子均值图）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/21 10:59
     */
    @RequestMapping(value = "getPollutantAvgDataByParam", method = RequestMethod.POST)
    public Object getPollutantAvgDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
             ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("ispartakecount", 1);
            List<String> dgimns = new ArrayList<>();
            //获取该点位监测的污染物
            List<Map<String, Object>> pollutantlist = onlineOtherPointService.getPointMonitorPollutantDataParamMap(paramMap);
            Map<String, String> codeAndName = new HashMap<>();
            Map<String, String> codeAndUnit = new HashMap<>();
            String pollutantCode;
            List<String> pollutantcodes = new ArrayList<>();
            for (Map<String, Object> map : pollutantlist) {
                if (map.get("pollutantcode") != null) {
                    dgimns.add(map.get("dgimn")+"");
                    pollutantCode = map.get("pollutantcode").toString();
                    codeAndName.put(map.get("pollutantcode").toString(), map.get("pollutantname") + "#"
                            + (map.get("orderindex") != null ? map.get("orderindex") : -99));
                    codeAndUnit.put(map.get("pollutantcode").toString(),map.get("pollutantunit")+"");

                    pollutantcodes.add(pollutantCode);
                }
            }
            String collection;
            String dataKey;
            String timeF;
            List<String> timeList;
            if (timetype.equals("1")) {
                timeList = DataFormatUtil.getYMDHBetween(starttime,endtime);
                timeList.add(endtime);
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
                collection = DB_HourData;
                dataKey = "HourDataList";
                timeF = "yyyy-MM-dd HH";
            } else {
                timeList = DataFormatUtil.getYMDBetween(starttime,endtime);
                timeList.add(endtime);
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                collection = DB_DayData;
                dataKey = "DayDataList";
                timeF = "yyyy-MM-dd";
            }
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", dgimns);
            paramMap.put("collection", collection);
            List<Document> documents = onlineMonitorService.getMonitorDataByParamMap(paramMap);
            Map<String, Map<String, List<Double>>> timeAndCodeAndValues = new HashMap<>();
            Map<String, List<Double>> codeAndValues;
            List<Double> values;
            String time;
            Double value;
            List<Document> pollutantList;
            for (Document document : documents) {
                time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"),timeF);
                codeAndValues = timeAndCodeAndValues.get(time)!=null?timeAndCodeAndValues.get(time):new HashMap<>();
                pollutantList = document.get(dataKey,List.class);
                for (Document pollutant:pollutantList){
                    pollutantCode = pollutant.getString("PollutantCode");
                    if (pollutantcodes.contains(pollutantCode)){
                        values = codeAndValues.get(pollutantCode)!=null?codeAndValues.get(pollutantCode):new ArrayList<>();
                        value = pollutant.get("AvgStrength")!=null?Double.parseDouble(pollutant.get("AvgStrength").toString()):0D;
                        values.add(value);
                        codeAndValues.put(pollutantCode,values);
                    }
                    timeAndCodeAndValues.put(time,codeAndValues);
                }
            }
            if (timeAndCodeAndValues.size()>0){
                for (String timeIndex:timeList){
                    Map<String,Object> resultMap = new HashMap<>();
                    resultMap.put("monitortime",timeIndex);
                    codeAndValues = timeAndCodeAndValues.get(timeIndex);
                    List<Map<String,Object>> dataList = new ArrayList<>();
                    if (codeAndValues!=null){
                        for (String codeIndex:codeAndValues.keySet()){
                            Map<String,Object> dataMap = new HashMap<>();
                            dataMap.put("pollutantcode",codeIndex);
                            dataMap.put("pollutantname",codeAndName.get(codeIndex).split("#")[0]);
                            dataMap.put("orderindex",codeAndName.get(codeIndex).split("#")[1]);
                            dataMap.put("pollutantunit",codeAndUnit.get(codeIndex));
                            dataMap.put("pollutantvalue",DataFormatUtil.getListAvgValue(codeAndValues.get(codeIndex)));
                            dataList.add(dataMap);
                        }
                    }
                    //排序
                    dataList = dataList.stream().sorted(
                            Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString()))).collect(Collectors.toList());
                    resultMap.put("datalist",dataList);
                    resultList.add(resultMap);
                }
            }

            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取监测因子变化率数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/21 10:59
     */
    @RequestMapping(value = "getPollutantChangeDataByParam", method = RequestMethod.POST)
    public Object getPollutantChangeDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "thatstarttime") String thatstarttime,
            @RequestJson(value = "thatendtime") String thatendtime,
            @RequestJson(value = "htb") String htb,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
            @RequestJson(value = "pollutantcategorys", required = false) List<Integer> pollutantcategorys

    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("dgimns", dgimns);
            paramMap.put("ispartakecount", 1);
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                paramMap.put("pollutantcategorys", pollutantcategorys);
            }
            //获取该点位监测的污染物
            List<Map<String, Object>> pollutantlist = onlineOtherPointService.getPointMonitorPollutantDataParamMap(paramMap);
            Map<String, Map<String, Object>> mnAndCodeAndName = new HashMap<>();
            Map<String, Object> codeAndName;
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String mnCommon;
            String pollutantCode;
            pollutantcodes = new ArrayList<>();
            Map<String, Integer> codeAndType = new HashMap<>();
            for (Map<String, Object> map : pollutantlist) {
                if (map.get("pollutantcode") != null) {
                    mnCommon = map.get("dgimn").toString();
                    codeAndName = mnAndCodeAndName.get(mnCommon) != null ? mnAndCodeAndName.get(mnCommon) : new HashMap<>();
                    pollutantCode = map.get("pollutantcode").toString();
                    codeAndName.put(map.get("pollutantcode").toString(), map.get("pollutantname") + "#"
                            + (map.get("orderindex") != null ? map.get("orderindex") : -99));
                    mnAndCodeAndName.put(mnCommon, codeAndName);
                    mnAndId.put(mnCommon, map.get("monitorpointid"));
                    mnAndName.put(mnCommon, map.get("monitorpointname"));
                    pollutantcodes.add(pollutantCode);
                    if (map.get("pollutantcategory") != null) {
                        codeAndType.put(pollutantCode, Integer.parseInt(map.get("pollutantcategory") + ""));
                    }

                }
            }


            String collection;
            String dataKey;
            String timeF;
            List<String> timeList;
            if (timetype.equals("1")) {
                timeF = "yyyy-MM-dd HH";
            } else {
                timeF = "yyyy-MM-dd";
            }

            thatstarttime = DataFormatUtil.getHTBDate(starttime, timeF, htb);
            thatendtime = DataFormatUtil.getHTBDate(endtime, timeF, htb);
            if (timetype.equals("1")) {
                timeList = DataFormatUtil.getYMDHBetween(starttime, endtime);
                timeList.add(endtime);
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
                thatstarttime = thatstarttime + ":00:00";
                thatendtime = thatendtime + ":59:59";
                collection = DB_HourData;
                dataKey = "HourDataList";
            } else {
                timeList = DataFormatUtil.getYMDBetween(starttime, endtime);
                timeList.add(endtime);
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                thatstarttime = thatstarttime + " 00:00:00";
                thatendtime = thatendtime + " 23:59:59";
                collection = DB_DayData;
                dataKey = "DayDataList";

            }
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datakey", dataKey);
            paramMap.put("collection", collection);
            List<Document> thisDoc = onlineOtherPointService.getMongoDBListByParam(paramMap);
            if (thisDoc.size() > 0) {
                Map<String, List<Double>> thisTimeAndValues = getTimeAndValues(thisDoc, timeF);
                paramMap.put("starttime", thatstarttime);
                paramMap.put("endtime", thatendtime);
                List<Document> thatDoc = onlineOtherPointService.getMongoDBListByParam(paramMap);
                Map<String, List<Double>> thatTimeAndValues = getTimeAndValues(thatDoc, timeF);
                List<Double> thisValues;
                List<Double> thatValues;
                double thisT;
                double thatT;
                double change;
                String thatTime;
                for (String timeIndex : timeList) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("time", timeIndex);
                    thatTime = DataFormatUtil.getHTBDate(timeIndex, timeF, htb);
                    if (thisTimeAndValues.containsKey(timeIndex) && thatTimeAndValues.containsKey(thatTime)) {
                        thisValues = thisTimeAndValues.get(timeIndex);
                        thisT = thisValues.stream().mapToDouble(Double::doubleValue).sum();
                        thatValues = thatTimeAndValues.get(thatTime);
                        thatT = thatValues.stream().mapToDouble(Double::doubleValue).sum();
                        if (thatT > 0) {
                            change = 100d * (thisT - thatT) / thatT;
                            resultMap.put("changedata", DataFormatUtil.SaveOneAndSubZero(change));
                        } else {
                            resultMap.put("changedata", 0);
                        }

                    } else {
                        resultMap.put("changedata", 0);
                    }
                    resultList.add(resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, List<Double>> getTimeAndValues(List<Document> documents, String timeF) {
        Map<String, List<Double>> timeAndValues = new HashMap<>();
        List<Double> values;
        String time;
        if (documents.size() > 0) {
            List<Document> valueList;
            for (Document document : documents) {
                valueList = document.get("valuelist", List.class);

                for (Document value : valueList) {
                    if (value.get("value") != null) {
                        time = DataFormatUtil.formatDateToOtherFormat(value.getDate("time"), timeF);
                        values = timeAndValues.get(time) != null ? timeAndValues.get(time) : new ArrayList<>();
                        values.add(Double.parseDouble(value.get("value").toString()));
                        timeAndValues.put(time, values);
                    }
                }
            }
        }
        return timeAndValues;
    }


    private List<Map<String, Object>> getPollutantLDataList(List<Document> documents, Map<String, Object> mnAndId, Map<String, Object> mnAndName, Map<String, Map<String, Object>> mnAndCodeAndName, String isavg, Integer monitorpointtype) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, List<Double>>> mnAndCodeAndValues = new HashMap<>();
        Map<String, List<Double>> codeAndValues;
        List<Double> values;
        String mnCommon;
        String pollutantCode;
        Map<String, Object> codeAndName;
        String formart = CommonTypeEnum.MonitorPointTypeEnum.TZFEnum.getCode()==monitorpointtype?
                "######0.000000":"######0.0";
        if (documents.size() > 0) {
            List<Document> valueList;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                codeAndValues = mnAndCodeAndValues.get(mnCommon) != null ? mnAndCodeAndValues.get(mnCommon) : new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                valueList = document.get("valuelist", List.class);
                values = codeAndValues.get(pollutantCode) != null ? codeAndValues.get(pollutantCode) : new ArrayList<>();
                for (Document value : valueList) {
                    if (value.get("value") != null) {
                        values.add(Double.parseDouble(value.get("value").toString()));
                    }
                }
                codeAndValues.put(pollutantCode, values);
                mnAndCodeAndValues.put(mnCommon, codeAndValues);
            }
        }
        for (String mnIndex : mnAndId.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("monitorpointid", mnAndId.get(mnIndex));
            resultMap.put("monitorpointname", mnAndName.get(mnIndex));
            codeAndValues = mnAndCodeAndValues.get(mnIndex);
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            if (codeAndValues != null) {
                codeAndName = mnAndCodeAndName.get(mnIndex);
                for (String code : codeAndValues.keySet()) {
                    if (codeAndName.containsKey(code)) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("pollutantcode", code);
                        dataMap.put("pollutantname", codeAndName.get(code).toString().split("#")[0]);
                        dataMap.put("orderindex", codeAndName.get(code).toString().split("#")[1]);
                        values = codeAndValues.get(code);
                        if (isavg != null && "true".equals(isavg)) {
                            dataMap.put("avgvalue", DataFormatUtil.getListAvgValueFormatData(values,formart));
                        } else {
                            dataMap.put("values", values);
                        }
                        pollutantList.add(dataMap);
                    }
                }
            }
            //排序
            pollutantList = pollutantList.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString()))).collect(Collectors.toList());
            resultMap.put("pollutantlist", pollutantList);
            resultList.add(resultMap);
        }
        return resultList;
    }

    private List<Map<String, Object>> getGroupLDataList(List<Document> documents,
                                                        Map<String, Object> mnAndId,
                                                        Map<String, Object> mnAndName,
                                                        Map<String, Integer> codeAndType,
                                                        Integer monitorpointtype, String isavg) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<Integer, List<Double>>> mnAndTypeAndValues = new HashMap<>();
        Map<Integer, List<Double>> typeAndValues;
        List<Double> values;
        String mnCommon;
        String pollutantCode;
        Integer type;
        if (documents.size() > 0) {
            List<Document> valueList;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                typeAndValues = mnAndTypeAndValues.get(mnCommon) != null ? mnAndTypeAndValues.get(mnCommon) : new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                valueList = document.get("valuelist", List.class);
                type = codeAndType.get(pollutantCode);
                values = typeAndValues.get(type) != null ? typeAndValues.get(type) : new ArrayList<>();
                for (Document value : valueList) {
                    if (value.get("value") != null) {
                        values.add(Double.parseDouble(value.get("value").toString()));
                    }
                }
                typeAndValues.put(type, values);
                mnAndTypeAndValues.put(mnCommon, typeAndValues);
            }
        }
        for (String mnIndex : mnAndId.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("monitorpointid", mnAndId.get(mnIndex));
            resultMap.put("monitorpointname", mnAndName.get(mnIndex));
            typeAndValues = mnAndTypeAndValues.get(mnIndex);
            List<Map<String, Object>> pollutantList = new ArrayList<>();
            if (typeAndValues != null) {
                for (Integer code : typeAndValues.keySet()) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("pollutantcode", code);
                    dataMap.put("pollutantname", getGroupName(monitorpointtype, code));
                    values = typeAndValues.get(code);
                    if (isavg != null && "true".equals(isavg)) {
                        dataMap.put("avgvalue", DataFormatUtil.getListAvgValue(values));
                    } else {
                        dataMap.put("values", values);
                    }
                    pollutantList.add(dataMap);
                }
            }
            //排序
            pollutantList = pollutantList.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("pollutantcode").toString()))).collect(Collectors.toList());
            resultMap.put("pollutantlist", pollutantList);
            resultList.add(resultMap);
        }
        return resultList;
    }

    /**
     * @Description: 获取监测因子数组数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/21 10:59
     */
    @RequestMapping(value = "getManyPointOnePollutantDataByParam", method = RequestMethod.POST)
    public Object getManyPointOnePollutantDataByParam(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "dgimns") List<String> dgimns,
            @RequestJson(value = "pollutantcodes", required = false) List<String> pollutantcodes,
            @RequestJson(value = "pollutantcategorys", required = false) List<Integer> pollutantcategorys
    ) {
        try {

            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("ispartakecount", 1);
            paramMap.put("dgimns", dgimns);
            if (pollutantcodes != null && pollutantcodes.size() > 0) {
                paramMap.put("pollutantcodes", pollutantcodes);
            }
            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                paramMap.put("pollutantcategorys", pollutantcategorys);
            }
            paramMap.put("dgimns", dgimns);
            //获取该点位监测的污染物
            List<Map<String, Object>> pollutantlist = onlineOtherPointService.getPointMonitorPollutantDataParamMap(paramMap);
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> codeAndUnit = new HashMap<>();
            Map<String, Integer> codeAndType = new HashMap<>();
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String mnCommon;
            String pollutantCode;
            pollutantcodes = new ArrayList<>();
            for (Map<String, Object> map : pollutantlist) {
                if (map.get("pollutantcode") != null) {
                    mnCommon = map.get("dgimn").toString();
                    pollutantCode = map.get("pollutantcode").toString();
                    codeAndName.put(map.get("pollutantcode").toString(), map.get("pollutantname") + "#"
                            + (map.get("orderindex") != null ? map.get("orderindex") : -99)
                    );
                    codeAndUnit.put(pollutantCode, map.get("pollutantunit") != null ? map.get("pollutantunit") : "");
                    mnAndId.put(mnCommon, map.get("monitorpointid"));
                    mnAndName.put(mnCommon, map.get("monitorpointname"));
                    pollutantcodes.add(pollutantCode);
                    dgimns.add(mnCommon);
                    if (map.get("pollutantcategory") != null) {
                        codeAndType.put(pollutantCode, Integer.parseInt(map.get("pollutantcategory") + ""));
                    }
                }
            }

            String collection;
            String dataKey;
            String timeF;
            List<String> timeList;
            if (timetype.equals("1")) {
                timeList = DataFormatUtil.getYMDHBetween(starttime, endtime);
                timeList.add(endtime);
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";

                collection = DB_HourData;
                dataKey = "HourDataList";
                timeF = "yyyy-MM-dd HH";

            } else {
                timeList = DataFormatUtil.getYMDBetween(starttime, endtime);
                timeList.add(endtime);
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                collection = DB_DayData;
                dataKey = "DayDataList";
                timeF = "yyyy-MM-dd";
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("pollutantcodes", pollutantcodes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datakey", dataKey);
            paramMap.put("collection", collection);
            List<Document> documents = onlineOtherPointService.getMongoDBListByParam(paramMap);

            if (pollutantcategorys != null && pollutantcategorys.size() > 0) {
                resultList = getManyGroupPollutant(documents, mnAndId, mnAndName, codeAndType, monitorpointtype, timeF, timeList);
            } else {
                resultList = getManyOnePollutant(documents, mnAndId, mnAndName, timeF, codeAndName, codeAndUnit, timeList);
            }


            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getManyGroupPollutant(List<Document> documents,
                                                            Map<String, Object> mnAndId,
                                                            Map<String, Object> mnAndName,
                                                            Map<String, Integer> codeAndType,
                                                            Integer monitorpointtype,
                                                            String timeF,
                                                            List<String> timeList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<Integer, Map<String, Map<String, Double>>> typeAndMnAndDataMap = new HashMap<>();
        Map<String, Map<String, Double>> MnAndDataMap;
        Map<String, Double> DataMap;
        Double value;
        if (documents.size() > 0) {
            List<Document> valueList;
            String pollutantCode;
            String mnCommon;
            Integer type;
            String time;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                pollutantCode = document.getString("PollutantCode");
                type = codeAndType.get(pollutantCode);
                MnAndDataMap = typeAndMnAndDataMap.get(type) != null ? typeAndMnAndDataMap.get(type) : new HashMap<>();
                DataMap = MnAndDataMap.get(mnCommon) != null ? MnAndDataMap.get(mnCommon) : new HashMap<>();
                valueList = document.get("valuelist", List.class);
                Map<String, Double> timeAndValue = new HashMap<>();
                for (Document data : valueList) {
                    if (data.get("value") != null) {
                        time = DataFormatUtil.formatDateToOtherFormat(data.getDate("time"), timeF);
                        value = Double.parseDouble(data.get("value").toString());
                        timeAndValue.put(time, value);
                    }
                }
                for (String timeIndex : timeList) {
                    value = timeAndValue.get(timeIndex);
                    if (DataMap.get(timeIndex) != null) {
                        value = value != null ? value : 0;
                        DataMap.put(timeIndex, DataMap.get(timeIndex) + value);
                    } else {
                        DataMap.put(timeIndex, value);
                    }
                }
                MnAndDataMap.put(mnCommon, DataMap);
                typeAndMnAndDataMap.put(type, MnAndDataMap);
            }
        }
        for (Integer codeIndex : typeAndMnAndDataMap.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("pollutantcode", codeIndex);
            resultMap.put("pollutantname", getGroupName(monitorpointtype, codeIndex));

            MnAndDataMap = typeAndMnAndDataMap.get(codeIndex);
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (String mnIndex : MnAndDataMap.keySet()) {
                Map<String, Object> mnDataMap = new HashMap<>();
                mnDataMap.put("monitorpointid", mnAndId.get(mnIndex));
                mnDataMap.put("monitorpointname", mnAndName.get(mnIndex));
                List<Map<String, Object>> values = new ArrayList<>();
                DataMap = MnAndDataMap.get(mnIndex);
                for (String timeIndex : DataMap.keySet()) {
                    Map<String, Object> valueMap = new HashMap<>();
                    valueMap.put("time", timeIndex);
                    value = DataMap.get(timeIndex);
                    if (value != null) {
                        valueMap.put("value", DataFormatUtil.SaveThreeAndSubZero(value));
                    } else {
                        valueMap.put("value", "");
                    }

                    values.add(valueMap);
                }
                //排序
                values = values.stream().sorted(Comparator.comparing(m -> ((Map) m).get("time").toString())).collect(Collectors.toList());
                mnDataMap.put("datalist", values);
                dataList.add(mnDataMap);
            }
            resultMap.put("datalist", dataList);
            resultList.add(resultMap);
        }
        //排序
        resultList = resultList.stream().sorted(
                Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("pollutantcode").toString()))).collect(Collectors.toList());
        return resultList;
    }

    private List<Map<String, Object>> getManyOnePollutant(List<Document> documents,
                                                          Map<String, Object> mnAndId,
                                                          Map<String, Object> mnAndName,
                                                          String timeF,
                                                          Map<String, Object> codeAndName,
                                                          Map<String, Object> codeAndUnit, List<String> timeList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, List<Map<String, Object>>> codeAndMnDataList = new HashMap<>();
        List<Map<String, Object>> MnDataList;

        if (documents.size() > 0) {
            List<Document> valueList;
            String pollutantCode;
            String mnCommon;
            String time;
            Double value;
            for (Document document : documents) {
                mnCommon = document.getString("DataGatherCode");
                pollutantCode = document.getString("PollutantCode");
                valueList = document.get("valuelist", List.class);
                MnDataList = codeAndMnDataList.get(pollutantCode) != null ? codeAndMnDataList.get(pollutantCode) : new ArrayList<>();
                Map<String, Object> mnDataMap = new HashMap<>();
                mnDataMap.put("monitorpointid", mnAndId.get(mnCommon));
                mnDataMap.put("monitorpointname", mnAndName.get(mnCommon));
                Map<String, Double> timeAndValue = new HashMap<>();
                for (Document data : valueList) {
                    if (data.get("value") != null) {
                        time = DataFormatUtil.formatDateToOtherFormat(data.getDate("time"), timeF);
                        value = Double.parseDouble(data.get("value").toString());
                        timeAndValue.put(time, value);
                    }
                }
                List<Map<String, Object>> dataList = new ArrayList<>();
                for (String timeIndex : timeList) {
                    Map<String, Object> DataMap = new HashMap<>();
                    DataMap.put("time", timeIndex);

                    if (timeAndValue.containsKey(timeIndex)) {
                        DataMap.put("value", timeAndValue.get(timeIndex));
                    } else {
                        DataMap.put("value", "");
                    }
                    dataList.add(DataMap);
                }
                mnDataMap.put("datalist", dataList);
                MnDataList.add(mnDataMap);
                codeAndMnDataList.put(pollutantCode, MnDataList);
            }
        }
        for (String codeIndex : codeAndMnDataList.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("pollutantcode", codeIndex);
            resultMap.put("pollutantname", codeAndName.get(codeIndex).toString().split("#")[0]);
            resultMap.put("pollutantunit", codeAndUnit.get(codeIndex));
            resultMap.put("orderindex", codeAndName.get(codeIndex).toString().split("#")[1]);
            resultMap.put("datalist", codeAndMnDataList.get(codeIndex));
            resultList.add(resultMap);
        }
        //排序
        resultList = resultList.stream().sorted(
                Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString()))).collect(Collectors.toList());
        return resultList;
    }

    /**
     * @author: xsm
     * @date: 2022/05/06 0006 上午 10:47
     * @Description: 自定义查询条件获取污染物数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutantSelectDataByParams", method = RequestMethod.POST)
    public Object getPollutantSelectDataByParams(@RequestJson(value = "pollutanttype") Integer pollutanttype,
                                                 @RequestJson(value = "wherestr", required = false) String wherestr,
                                                 @RequestJson(value = "codes", required = false) List<String> codes,
                                                 @RequestJson(value = "outputid", required = false) String outputid,
                                                 @RequestJson(value = "ordercategory", required = false) String ordercategory
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", pollutanttype);
            paramMap.put("codes", codes);
            paramMap.put("wherestr", wherestr);
            paramMap.put("ispartakecount", 1);
            //现只针对其他监测点点位
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (ordercategory != null && "yes".equals(ordercategory)) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                Map<Integer, List<Map<String, Object>>> typeAndPollutant = pollutantInfo.stream().filter(m -> m != null && m.get("pollutantcategory") != null).collect(Collectors.groupingBy(m -> Integer.parseInt(m.get("pollutantcategory").toString())));
                for (Integer typeIndex : typeAndPollutant.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("typecode", typeIndex);
                    resultMap.put("typename", getGroupName(pollutanttype, typeIndex));
                    resultMap.put("pollutantlist", typeAndPollutant.get(typeIndex));
                    resultList.add(resultMap);
                }
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
            } else {
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, pollutantInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/07 0007 上午 9:33
     * @Description: 根据自定义参数获取点位监测的污染物下拉数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOnePointMonitorPollutantSelectDataByParams", method = RequestMethod.POST)
    public Object getOnePointMonitorPollutantSelectDataByParams(
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "wherestr", required = false) String wherestr,
            @RequestJson(value = "ordercategory", required = false) String ordercategory
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            //获取该类型监测的污染物
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("outputid", outputid);
            paramMap.put("wherestr", wherestr);
            List<Map<String, Object>> pollutantInfo = onlineOtherPointService.getOnePointMonitorPollutantSelectDataByParams(paramMap);
            if (ordercategory != null && "yes".equals(ordercategory)) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                Map<Integer, List<Map<String, Object>>> typeAndPollutant = pollutantInfo.stream().filter(m -> m != null && m.get("pollutantcategory") != null).collect(Collectors.groupingBy(m -> Integer.parseInt(m.get("pollutantcategory").toString())));
                for (Integer typeIndex : typeAndPollutant.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("typecode", typeIndex);
                    resultMap.put("typename", getGroupName(monitorpointtype, typeIndex));
                    resultMap.put("pollutantlist", typeAndPollutant.get(typeIndex));
                    resultList.add(resultMap);
                }
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
            } else {
                return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, pollutantInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/06 0006 上午 10:47
     * @Description: 自定义查询条件查询VOC变化率数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointPollutantChangeRateDataByParam", method = RequestMethod.POST)
    public Object getPointPollutantChangeRateDataByParam(@RequestJson(value = "datatype", required = false) String datatype,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime,
                                                         @RequestJson(value = "pointdata") Object pointdata

    ) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> pointlist = (List<Map<String, Object>>) pointdata;
            if (datatype == null) {
                datatype = "hour";
            }
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> mnandcode = new HashMap<>();
            List<String> codes = new ArrayList<>();
            for (Map<String, Object> map : pointlist) {
                if (map.get("dgimn") != null) {
                    dgimns.add(map.get("dgimn").toString());
                    //mnandname.put(map.get("dgimn").toString(),map.get("pointname"));
                    if (map.get("pollutantcode") != null) {
                        codes.add(map.get("pollutantcode").toString());
                        //codeandname.put(map.get("dgimn")+"_"+map.get("pollutantcode"),map.get("pollutantname"));
                        //mnandcode.put(map.get("dgimn").toString(),map.get("pollutantcode"));
                    }
                }
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //paramMap.put("codeandname", codeandname);
            paramMap.put("datatype", datatype);
            //paramMap.put("mnandname", mnandname);
            //paramMap.put("mnandcode", mnandcode);
            paramMap.put("codes", codes);
            paramMap.put("dgimns", dgimns);
            paramMap.put("pointlist", pointlist);
            result = onlineOtherPointService.getPointPollutantChangeRateDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/06 0006 上午 10:47
     * @Description: 自定义查询条件查询VOC比值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPointPollutantContrastValueDataByParam", method = RequestMethod.POST)
    public Object getPointPollutantContrastValueDataByParam(@RequestJson(value = "datatype", required = false) String datatype,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime,
                                                            @RequestJson(value = "pointdata") Object pointdata

    ) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> pointlist = (List<Map<String, Object>>) pointdata;
            if (datatype == null) {
                datatype = "hour";
            }
            List<String> dgimns = new ArrayList<>();
            List<String> codes = new ArrayList<>();
            for (Map<String, Object> map : pointlist) {
                //分子
                if (map.get("fz_dgimn") != null) {
                    dgimns.add(map.get("fz_dgimn").toString());
                    if (map.get("fz_pollutantcode") != null) {
                        codes.add(map.get("fz_pollutantcode").toString());
                    }
                }
                //分母
                if (map.get("fm_dgimn") != null) {
                    dgimns.add(map.get("fm_dgimn").toString());
                    if (map.get("fm_pollutantcode") != null) {
                        codes.add(map.get("fm_pollutantcode").toString());
                    }
                }
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            paramMap.put("codes", codes);
            paramMap.put("dgimns", dgimns);
            paramMap.put("pointlist", pointlist);
            result = onlineOtherPointService.getPointPollutantContrastValueDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
