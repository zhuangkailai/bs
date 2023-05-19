package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.StationHourAQIDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirParkHourDataService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AirCommonSixIndexEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: chengzq
 * @date: 2019/5/21 0021 09:48
 * @Description: 城市空气质量小时数据控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("airParkHour")
public class AirParkHourDataController {
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private AirParkHourDataService airParkHourDataService;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String collection = "ParkHourAQIData";
    private final String citycollection = "CityHourAQIData";
    private final String stationcollection = "StationHourAQIData";


    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 上午 10:14
     * @Description: 获取最新一条城市空气小时及标准值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime yyyy-MM-dd HH
     * @param: type 默认查询园区内空气
     * @throws:
     */
    @RequestMapping(value = "/getLastAirAQIAndIndexStandardData", method = RequestMethod.POST)
    public Object getLastAirAQIAndIndexStandardData(@RequestJson(value = "type", required = false) String type,
                                                    @RequestJson(value = "monitortime", required = false) String monitortime,
                                                    @RequestJson(value = "dgimn", required = false) String dgimn) throws Exception {
        try {

            StationHourAQIDataVO cityHourAQIData = new StationHourAQIDataVO();
            Map<String, Object> resultMap = new HashMap<>();
            if (StringUtils.isNotBlank(monitortime)) {
                Map<String, Object> map = new HashMap<>();
                map.put("starttime", monitortime);
                map.put("endtime", monitortime);
                cityHourAQIData.setMonitorTime(JSONObject.fromObject(map).toString());
            }
            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            List<Object> listWithPageByParam = new ArrayList<>();
            //分页获取一条
            mongoSearchEntity.setPage(0);
            mongoSearchEntity.setSize(1);
            //按时间倒序
            mongoSearchEntity.setSortorder(2);
            List<String> sortNames = new ArrayList<>();
            sortNames.add("MonitorTime");
            mongoSearchEntity.setSortname(sortNames);
            if ("city".equals(type)) {
                listWithPageByParam = mongoBaseService.getListWithPageByParam(cityHourAQIData, mongoSearchEntity, citycollection, "yyyy-MM-dd HH");
            } else if ("station".equals(type)) {
                cityHourAQIData.setStationCode(dgimn);
                listWithPageByParam = mongoBaseService.getListWithPageByParam(cityHourAQIData, mongoSearchEntity, stationcollection, "yyyy-MM-dd HH");
            } else {
                listWithPageByParam = mongoBaseService.getListWithPageByParam(cityHourAQIData, mongoSearchEntity, collection, "yyyy-MM-dd HH");
            }

            Map<String, Object> params = new HashMap<>();
            //获取污染物码表信息
            params.put("pollutanttype", 5);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(params);

            for (Object parkHourAQIDataVO : listWithPageByParam) {
                resultMap = getWeatherAqiInfo(parkHourAQIDataVO, pollutantInfo, null);
                String compositeIndex = getCompositeIndex(parkHourAQIDataVO);
                resultMap.put("compositeIndex", compositeIndex);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 获取环比园区及城市AQI数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/14 10:36
     */
    @RequestMapping(value = "/getHBAQIData", method = RequestMethod.POST)
    public Object getHBAQIData(@RequestJson(value = "monitortime") String monitortime) {
        try {
             Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            String starttime = DataFormatUtil.getBeforeByHourTime(1, monitortime);
            String endtime = monitortime;
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("collection", collection);
            List<Document> documents = getMongodbData(paramMap);

            setResultList(resultMap,documents,starttime,endtime,"park");
            paramMap.put("collection", citycollection);
            documents = getMongodbData(paramMap);
            setResultList(resultMap,documents,starttime,endtime,"city");
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void setResultList(Map<String, Object> resultMap, List<Document> documents, String starttime, String endtime, String dataMark) {

        Map<String,Object> dataMap = new HashMap<>();
        if (documents.size()>0){
            Integer thisAQI=null;
            Integer hbAQI = null;
            for (Document document:documents){
                if (DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")).equals(endtime)){
                    thisAQI = document.getInteger("AQI");
                }
                if (DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")).equals(starttime)){
                    hbAQI = document.getInteger("AQI");
                }
            }
            dataMap.put("aqi",thisAQI);
            if (thisAQI!=null&&hbAQI!=null){
                dataMap.put("change",thisAQI-hbAQI);
            }else {
                dataMap.put("change","");
            }

        }
        resultMap.put(dataMark,dataMap);

    }

    private List<Document> getMongodbData(Map<String, Object> paramMap) {
        Date startDate = DataFormatUtil.getDateYMDH(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDH(paramMap.get("endtime").toString());
        Fields fields = fields("MonitorTime", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> documents = aggregationResults.getMappedResults();
        return documents;
    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 下午 3:22
     * @Description: 获取最新24条城市空气小时及标准值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type, monitortime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "/getLast24AirAQIAndIndexStandardDataByParams", method = RequestMethod.POST)
    public Object getLast24AirAQIAndIndexStandardDataByParams(@RequestJson(value = "monitortime") String monitortime
            , @RequestJson(value = "pollutantcode") String pollutantcode, @RequestJson(value = "dgimn") String dgimn) throws Exception {
        try {
            StationHourAQIDataVO cityHourAQIData = new StationHourAQIDataVO();
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (StringUtils.isNotBlank(monitortime)) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                Map<String, Object> map = new HashMap<>();
                map.put("endtime", monitortime);
                LocalDateTime parse = LocalDateTime.parse(monitortime, dateTimeFormatter);
                LocalDateTime minus = parse.minus(24, ChronoUnit.HOURS);
                String endtime = minus.format(dateTimeFormatter);
                map.put("starttime", endtime);
                cityHourAQIData.setMonitorTime(JSONObject.fromObject(map).toString());
            }
            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            List<Object> listWithPageByParam = new ArrayList<>();
            //分页获取一条
            mongoSearchEntity.setPage(0);
            mongoSearchEntity.setSize(24);
            //按时间倒序
            mongoSearchEntity.setSortorder(2);
            List<String> sortNames = new ArrayList<>();
            sortNames.add("MonitorTime");
            mongoSearchEntity.setSortname(sortNames);

            cityHourAQIData.setStationCode(dgimn);
            listWithPageByParam = mongoBaseService.getListWithPageByParam(cityHourAQIData, mongoSearchEntity, stationcollection, "yyyy-MM-dd HH");

            Map<String, Object> params = new HashMap<>();
            //获取污染物码表信息
            params.put("pollutanttype", 5);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(params);
            for (Object parkHourAQIDataVO : listWithPageByParam) {
                Map<String, Object> resultMap = getWeatherAqiInfo(parkHourAQIDataVO, pollutantInfo, pollutantcode);
                JSONObject jsonObject = JSONObject.fromObject(resultMap.get(pollutantcode));
                jsonObject.put("monitorTime", resultMap.get("monitorTime"));
                resultList.add(jsonObject);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().sorted(Comparator.comparing(m -> m.get("monitorTime").toString())).collect(Collectors.toList()));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 下午 1:30
     * @Description: 设置天气aqi信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [listWithPageByParam, pollutantInfo]
     * @throws:
     */
    private Map<String, Object> getWeatherAqiInfo(Object hourAQIData, List<Map<String, Object>> pollutantInfo, String pollutantcode) throws Exception {
        Method getDataList = hourAQIData.getClass().getMethod("getDataList");
        Method getaQi = hourAQIData.getClass().getMethod("getaQi");
        Method getAirQuality = hourAQIData.getClass().getMethod("getAirQuality");
        Method getMonitorTime = hourAQIData.getClass().getMethod("getMonitorTime");
        Method getPrimaryPollutant = hourAQIData.getClass().getMethod("getPrimaryPollutant");
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) getDataList.invoke(hourAQIData);
        boolean flag = pollutantcode == null ? true : false;
        for (Map<String, Object> map : dataList) {
            if (map.get("PollutantCode") != null) {
                Map<String, Object> aqiMap = new HashMap<>();
                String pollutantCode = map.get("PollutantCode").toString();
                pollutantcode = pollutantcode == null ? pollutantCode : pollutantcode;

                String airLevel = map.get("AirLevel") == null ? "" : map.get("AirLevel").toString();
                if ("一级".equals(airLevel)) {
                    aqiMap.put("iAQIhi", 50);
                    aqiMap.put("iAQIlo", 0);
                } else if ("二级".equals(airLevel)) {
                    aqiMap.put("iAQIhi", 100);
                    aqiMap.put("iAQIlo", 51);
                } else if ("三级".equals(airLevel)) {
                    aqiMap.put("iAQIhi", 150);
                    aqiMap.put("iAQIlo", 101);
                } else if ("四级".equals(airLevel)) {
                    aqiMap.put("iAQIhi", 200);
                    aqiMap.put("iAQIlo", 151);
                } else if ("五级".equals(airLevel)) {
                    aqiMap.put("iAQIhi", 300);
                    aqiMap.put("iAQIlo", 201);
                }
                //将污染物code设置为name
                String finalPollutantcode = pollutantcode;
                pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && (flag || pollutantCode.equals(finalPollutantcode)) && m.get("code").toString().equals(pollutantCode)).
                        peek(m -> {
                            if (!flag) {
                                resultMap.put(m.get("code").toString().toLowerCase(), aqiMap);
                            } else {
                                resultMap.put(m.get("name").toString().toLowerCase(), aqiMap);
                            }
                            aqiMap.put("iAQI", map.get("IAQI"));
                            aqiMap.put("Strength", map.get("Strength"));
                            aqiMap.put("pollutantcode", pollutantCode);
                        }).collect(Collectors.toList());
                //将首要污染物code设置为name
                pollutantInfo.stream().filter(m -> {
                    try {
                        return m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(getPrimaryPollutant.invoke(hourAQIData));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).map(m -> resultMap.put("pimarypllutant", m.get("name").toString())).collect(Collectors.toList());
                resultMap.put("aqi", getaQi.invoke(hourAQIData));
                resultMap.put("quality", getAirQuality.invoke(hourAQIData));
                resultMap.put("monitorTime", OverAlarmController.format(getMonitorTime.invoke(hourAQIData).toString(), "yyyy-MM-dd HH"));
            }
        }
        return resultMap;
    }


    /**
     * @author: chengzq
     * @date: 2020/5/20 0020 下午 4:36
     * @Description: 计算空气质量小时综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [hourAQIData]
     * @throws:
     */
    public static String getCompositeIndex(Object hourAQIData) throws Exception {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");

        Method getDataList = hourAQIData.getClass().getMethod("getDataList");
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) getDataList.invoke(hourAQIData);

        Double PM25 = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("Strength") != null && AIR_COMMON_SIX_INDEX_PM25.getCode().equals(m.get("PollutantCode").toString()))
                .map(m -> Double.valueOf(m.get("Strength").toString())).collect(Collectors.averagingDouble(m -> m));
        Double PM10 = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("Strength") != null && AIR_COMMON_SIX_INDEX_PM10.getCode().equals(m.get("PollutantCode").toString()))
                .map(m -> Double.valueOf(m.get("Strength").toString())).collect(Collectors.averagingDouble(m -> m));
        Double O3 = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("Strength") != null && AIR_COMMON_SIX_INDEX_O3.getCode().equals(m.get("PollutantCode").toString()))
                .map(m -> Double.valueOf(m.get("Strength").toString())).collect(Collectors.averagingDouble(m -> m));
        Double NO2 = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("Strength") != null && AIR_COMMON_SIX_INDEX_NO2.getCode().equals(m.get("PollutantCode").toString()))
                .map(m -> Double.valueOf(m.get("Strength").toString())).collect(Collectors.averagingDouble(m -> m));
        Double CO = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("Strength") != null && AIR_COMMON_SIX_INDEX_CO.getCode().equals(m.get("PollutantCode").toString()))
                .map(m -> Double.valueOf(m.get("Strength").toString())).collect(Collectors.averagingDouble(m -> m));
        Double SO2 = dataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("Strength") != null && AIR_COMMON_SIX_INDEX_SO2.getCode().equals(m.get("PollutantCode").toString()))
                .map(m -> Double.valueOf(m.get("Strength").toString())).collect(Collectors.averagingDouble(m -> m));

        return decimalFormat.format(SO2 / 60 + NO2 / 40 + CO / 4 + O3 / 160 + PM25 / 35 + PM10 / 70);
    }


    /**
     * @author: lip
     * @date: 2020/5/18 0018 下午 3:56
     * @Description: 获取园区最新空气质量统计数据（六参数、AQI、累计AQI、累计综合指数、污染指数等）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime：日期（yyyy-MM-dd）
     * @return:
     */
    @RequestMapping(value = "/getLastAirParkCountData", method = RequestMethod.POST)
    public Object getLastAirParkCountData(@RequestJson(value = "monitortime") String monitortime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();

            String startTime = monitortime + " 00:00:00";
            String endTime = monitortime + " 23:59:59";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            List<String> pollutantCodes = Arrays.asList(
                    AIR_COMMON_SIX_INDEX_PM25.getCode(),
                    AIR_COMMON_SIX_INDEX_PM10.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_CO.getCode(),
                    AIR_COMMON_SIX_INDEX_O3.getCode(),
                    AIR_COMMON_SIX_INDEX_NO2.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_SO2.getCode());
            paramMap.put("pollutantcodes", pollutantCodes);
            List<Document> hourDataList = airParkHourDataService.getAirParkHourDataByParam(paramMap);
            if (hourDataList.size() > 0) {
                Map<String, Double> dayCompositeIndex = airParkHourDataService.computeDayCompositeIndex(hourDataList, pollutantCodes);
                Document lastData = hourDataList.get(0);
                resultMap.put("updatetime", DataFormatUtil.getDateYMDH(lastData.getDate("MonitorTime")));
                resultMap.put("aqi", lastData.getInteger("AQI"));
                String primaryPollutant = lastData.getString("PrimaryPollutant");
                if (primaryPollutant.indexOf(",") >= 0) {
                    String[] codes = primaryPollutant.split(",");
                    List<String> names = new ArrayList<>();
                    String name;
                    for (int i = 0; i < codes.length; i++) {
                        name = CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(codes[i]);
                        if (StringUtils.isNotBlank(name) && !names.contains(name)) {
                            names.add(name);
                        }
                    }
                    primaryPollutant = DataFormatUtil.FormatListToString(names, "、");
                } else {
                    primaryPollutant = CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(primaryPollutant);
                }
                resultMap.put("primarypollutant", primaryPollutant);
                resultMap.put("airquality", lastData.get("AirQuality"));
                Map<String, Double> codeAndTotal = new HashMap<>();
                Map<String, Integer> codeAndNum = new HashMap<>();
                List<Document> pollutantData;
                String pollutantCode;
                Double Strength;
                int totalAqi = 0;
                for (Document document : hourDataList) {
                    totalAqi += document.getInteger("AQI");
                    pollutantData = document.get("DataList", List.class);
                    for (Document pollutant : pollutantData) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (pollutantCodes.contains(pollutantCode)) {
                            Strength = pollutant.get("Strength") != null ? Double.parseDouble(pollutant.get("Strength").toString()) : 0;
                            if (codeAndTotal.containsKey(pollutantCode)) {
                                codeAndTotal.put(pollutantCode, codeAndTotal.get(pollutantCode) + Strength);
                                codeAndNum.put(pollutantCode, codeAndNum.get(pollutantCode) + 1);
                            } else {
                                codeAndTotal.put(pollutantCode, Strength);
                                codeAndNum.put(pollutantCode, 1);
                            }
                        }
                    }
                }
                Map<String, Object> lastCodeAndValue = new HashMap<>();
                pollutantData = lastData.get("DataList", List.class);
                String value;
                for (Document pollutant : pollutantData) {
                    Strength = pollutant.get("Strength") != null ? Double.parseDouble(pollutant.get("Strength").toString()) : 0;
                    pollutantCode = pollutant.getString("PollutantCode");
                    if (pollutantCode.equals(CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_CO.getCode())) {
                        value = DataFormatUtil.SaveOneAndSubZero(Strength);
                    } else {
                        value = DataFormatUtil.subZeroAndDot(Strength.toString());
                    }
                    lastCodeAndValue.put(pollutantCode, value);
                }
                resultMap.put("sumaqi", DataFormatUtil.formatDoubleSaveNo((1d * totalAqi) / hourDataList.size()));
                Double compositeIndex = dayCompositeIndex.get("total") != null ? dayCompositeIndex.get("total") : 0d;
                resultMap.put("sumcompositeindex", DataFormatUtil.SaveOneAndSubZero(compositeIndex));
                String name;
                Double total;
                int num;
                Object sumdata;
                for (String code : pollutantCodes) {
                    name = CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code);
                    if (StringUtils.isNotBlank(name)) {
                        if (name.indexOf(".") >= 0) {
                            name = name.replace(".", "");
                        }
                        if (codeAndTotal.containsKey(code)) {
                            total = codeAndTotal.get(code);
                            num = codeAndNum.get(code);
                            if (code.equals(CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_CO.getCode())) {
                                sumdata = DataFormatUtil.SaveOneAndSubZero(total / num);
                            } else {
                                sumdata = DataFormatUtil.formatDoubleSaveNo(total / num);
                            }
                        } else {
                            sumdata = 0;
                        }
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("hourdata", lastCodeAndValue.get(code));
                        dataMap.put("sumdata", sumdata);
                        resultMap.put(name, dataMap);
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
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 通过监测污染物和时间段获取城市空气质量小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkAirHourDataByPollutantCodeAndMonitorTimes", method = RequestMethod.POST)
    public Object getParkAirHourDataByPollutantCodeAndMonitorTimes(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
            Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
            Aggregation aggregation;
            if (pollutantcode.equals("aqi") || pollutantcode.equals("compositeindex")) {
                aggregation = newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                        sort(Sort.Direction.ASC, "MonitorTime")
                );
            } else {
                aggregation = newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("DataList.PollutantCode").is(pollutantcode)),
                        sort(Sort.Direction.ASC, "MonitorTime")
                );
            }
            AggregationResults<Document> cityHourAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityHourAQIData.getMappedResults();
            if (documents.size() > 0) {
                if (pollutantcode.equals("aqi")) {//AQI
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("pollutanttype", 5);
                    List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                    paramMap.clear();
                    for (Map<String, Object> map : pollutants) {
                        paramMap.put(map.get("code").toString(), map.get("name"));
                    }
                    for (Document document : documents) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                        map.put("aqi", document.get("AQI"));
                        map.put("primarypollutant", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                        dataList.add(map);
                    }
                } else if (pollutantcode.equals("compositeindex")) {//综合指数

                } else {//污染物
                    for (Document document : documents) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                        map.put("aqi", document.get("AQI"));
                        List<Map<String, Object>> pollutantDataList = document.get("DataList", List.class);
                        for (Map<String, Object> dataMap : pollutantDataList) {
                            if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                map.put("pollutantvalue", dataMap.get("Strength"));
                                map.put("iaqi", dataMap.get("IAQI"));
                                break;
                            }
                        }
                        dataList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 9:27
     * @Description: 由code转换name
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getPrimaryPollutant(Map<String, Object> paramMap, Object primaryPollutant) {
        String primaryPollutants = "";
        if (primaryPollutant != null) {
            String tempValue = primaryPollutant.toString();
            if (tempValue.indexOf(",") > -1) {
                String[] tempValues = tempValue.split(",");
                for (int i = 0; i < tempValues.length; i++) {
                    primaryPollutants += paramMap.get(tempValues[i]) + ",";
                }
                primaryPollutants = primaryPollutants.substring(0, primaryPollutants.length() - 1);
            } else {
                primaryPollutants = paramMap.get(tempValue) != null ? paramMap.get(tempValue).toString() : "";
            }
        }
        return primaryPollutants;
    }


}
