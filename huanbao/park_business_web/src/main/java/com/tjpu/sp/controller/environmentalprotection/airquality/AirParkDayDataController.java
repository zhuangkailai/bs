package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.ParkHourAQIDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirParkDayDataService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirParkMonthDataService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: lip
 * @date: 2019/6/6 0006 上午 9:00
 * @Description: 城市空气日数据操作类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("airParkDayData")
public class AirParkDayDataController {

    @Autowired
    private PollutantService pollutantService;

    @Autowired
    private AirParkMonthDataService airParkMonthDataService;
    @Autowired
    private AirParkDayDataService airParkDayDataService;
    @Autowired
    private MongoBaseService mongoBaseService;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String collection = "ParkDayAQIData";
    private final String citycollection = "CityDayAQIData";

    /**
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 通过监测污染物和时间段获取城市空气质量日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkAirDayDataByPollutantCodeAndMonitorTimes", method = RequestMethod.POST)
    public Object getParkAirDayDataByPollutantCodeAndMonitorTimes(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Date startDate = DataFormatUtil.getDateYMD(starttime);
            Date endDate = DataFormatUtil.getDateYMD(endtime);
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
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
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
                        map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                        map.put("aqi", document.get("AQI"));
                        map.put("primarypollutant", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                        dataList.add(map);
                    }
                } else if (pollutantcode.equals("compositeindex")) {//综合指数

                } else {//污染物
                    for (Document document : documents) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
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
     * @date: 2019/6/3 14:56
     * @Description: 统计单月园区内每天的空气质量数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAirParkAQIDataByMonitorMonth", method = RequestMethod.POST)
    public Object countAirParkAQIDataByMonitorMonth(@RequestJson(value = "monitormonth") String monitormonth) throws Exception {

        Map<String, Object> dataMap = new HashMap<>();
        try {
            Date nowTime = new Date();
            String nowMonth = DataFormatUtil.getDateYM(nowTime);
            Date startTime = DataFormatUtil.parseDate(monitormonth + "-01 00:00:00");
            Date endTime;
            if (nowMonth.equals(monitormonth)) {
                endTime = nowTime;
            } else {
                endTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getLastDayOfMonth(monitormonth) + " 23:59:59");
            }
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI","DataList", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startTime).lte(endTime)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            List<Document> pollutantList;
            if (documents.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
                List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                paramMap.clear();
                for (Map<String, Object> map : pollutants) {
                    paramMap.put(map.get("code").toString(), map.get("name"));
                }
                List<Map<String, Object>> ymdList = new ArrayList<>();
                for (Document document : documents) {
                    Map<String, Object> ymdMap = new HashMap<>();
                    ymdMap.put("time", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                    ymdMap.put("aqi", document.get("AQI"));
                    ymdMap.put("aq", document.get("AirQuality"));
                    ymdMap.put("keyname", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                    pollutantList = document.get("DataList",List.class);
                    for (Document pollutant:pollutantList){
                        pollutant.put("pollutantname",CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(pollutant.getString("PollutantCode")));
                        pollutant.put("pollutantunit",CommonTypeEnum.AirCommonSixIndexEnum.getCodeByString(pollutant.getString("PollutantCode")).getUnit());
                    }
                    ymdMap.put("datalist", pollutantList);
                    ymdList.add(ymdMap);
                }
                dataMap.put("monitormonth", monitormonth);
                dataMap.put("airdata", ymdList);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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

    /**
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 统计单月城市空气质量首要污染物占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime(yyyy - mm)
     * @return:
     */
    @RequestMapping(value = "countAirParkDayPrimaryPollutantByMonitorTime", method = RequestMethod.POST)
    public Object countAirParkDayPrimaryPollutantByMonitorTime(@RequestJson(value = "monitortime") String monitortime) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            String starttime = monitortime + "-01";
            String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
            Date startDate = DataFormatUtil.getDateYMD(starttime);
            Date endDate = DataFormatUtil.getDateYMD(endtime);
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("PrimaryPollutant").ne(null))
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            List<String> pollutantCodes = new ArrayList<>();
            Map<String, Integer> keyAndNum = getKeyAndNum(documents, "PrimaryPollutant", pollutantCodes);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("codes", pollutantCodes);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
            if (pollutants.size() > 0) {
                int total = pollutantCodes.size();
                paramMap.clear();
                for (Map<String, Object> map : pollutants) {
                    paramMap.put(map.get("code").toString(), map.get("name"));
                }
                for (String key : keyAndNum.keySet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("primarypollutant", paramMap.get(key));
                    map.put("daynum", keyAndNum.get(key));
                    map.put("percentage", DataFormatUtil.SaveTwoAndSubZero(keyAndNum.get(key) * 100d / total) + "%");
                    dataList.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }


    /**
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 根据时间段查询优良天数（同比）、空气质量等级数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime(yyyy - mm)datamark：数据标记（month/year）
     * @return:
     */
    @RequestMapping(value = "getExcellentDayDataByMonitorTime", method = RequestMethod.POST)
    public Object getExcellentDayDataByMonitorTime(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "datamark") String datamark) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        try {
            String starttime = "";
            String endtime = "";
            if ("month".equals(datamark)) {
                starttime = monitortime + "-01 00:00:00";
                endtime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
            } else if ("year".equals(datamark)) {
                starttime = DataFormatUtil.getYearFirst(monitortime) + " 00:00:00";
                Calendar calendar = Calendar.getInstance();
                String year = String.valueOf(calendar.get(Calendar.YEAR));//当前年
                if (monitortime.equals(year)) {
                    endtime = DataFormatUtil.getDateYMD(new Date()) + " 23:59:59";
                } else {
                    endtime = DataFormatUtil.getYearLast(monitortime) + " 23:59:59";
                }
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(endDate))
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            if (documents.size() > 0) {
                //获取环比数据
                if ("month".equals(datamark)) {
                    startDate = DataFormatUtil.getBeforeMonthByNum(12, startDate);
                    monitortime = DataFormatUtil.getDateYM(startDate);
                    endtime = DataFormatUtil.getLastDayOfMonth(monitortime) + " 23:59:59";
                    endDate = DataFormatUtil.getDateYMDHMS(endtime);
                } else if ("year".equals(datamark)) {
                    startDate = DataFormatUtil.getBeforeYearByNum(1, startDate);
                    endDate = DataFormatUtil.getBeforeYearByNum(1, endDate);
                }
                Aggregation before = newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate))
                );
                AggregationResults<Document> beforeData = mongoTemplate.aggregate(before, collection, Document.class);
                List<Document> beforeDocuments = beforeData.getMappedResults();

                List<String> excellentGood = Arrays.asList("优", "良好");
                int currNum = 0;
                int beforeNum = 0;
                //当前时间的优良天数
                Map<String, Integer> airLevelAndNum = new HashMap<>();
                String airLevel;
                for (Document document : documents) {
                    if (document.get("AQI") != null) {
                        airLevel = DataFormatUtil.getQualityByAQI(document.getInteger("AQI"));
                        if (airLevelAndNum.containsKey(airLevel)) {
                            airLevelAndNum.put(airLevel, airLevelAndNum.get(airLevel) + 1);
                        } else {
                            airLevelAndNum.put(airLevel, 1);
                        }

                        if (excellentGood.contains(airLevel)) {
                            currNum++;
                        }
                    }
                }

                for (Document document : beforeDocuments) {
                    if (document.get("AQI") != null) {
                        airLevel = DataFormatUtil.getQualityByAQI(document.getInteger("AQI"));
                        if (excellentGood.contains(airLevel)) {
                            beforeNum++;
                        }
                    }
                }
                int excellentgoodnum = currNum;
                int tbnum = currNum - beforeNum;
                resultMap.put("excellentgoodnum", excellentgoodnum);
                resultMap.put("tbnum", tbnum);
                resultMap.put("excellentnum", airLevelAndNum.get("优"));
                resultMap.put("goodnum", airLevelAndNum.get("良好"));
                resultMap.put("lightnum", airLevelAndNum.get("轻度污染"));
                resultMap.put("moderatenum", airLevelAndNum.get("中度污染"));
                resultMap.put("severenum", airLevelAndNum.get("重度污染"));
                resultMap.put("seriousnum", airLevelAndNum.get("严重污染"));
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
     * @Description: 统计单月城市空气质量等级占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAirParkDayQualityByMonitorTime", method = RequestMethod.POST)
    public Object countAirParkDayQualityByMonitorTime(@RequestJson(value = "monitortime") String monitortime) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            String starttime = monitortime + "-01";
            String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
            Date startDate = DataFormatUtil.getDateYMD(starttime);
            Date endDate = DataFormatUtil.getDateYMD(endtime);
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(endDate))
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            List<String> codes = new ArrayList<>();
            Map<String, Integer> keyAndNum = getKeyAndNum(documents, "AirQuality", codes);
            int total = codes.size();
            for (String key : keyAndNum.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("quality", key);
                map.put("daynum", keyAndNum.get(key));
                map.put("percentage", DataFormatUtil.SaveTwoAndSubZero(keyAndNum.get(key) * 100d / total) + "%");
                dataList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }

    /**
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 统计单月城市空气质量等级占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countParkDaySixParameterContributionByMonitorTime", method = RequestMethod.POST)
    public Object countParkDaySixParameterContributionByMonitorTime(@RequestJson(value = "monitortime") String monitortime) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Map<String, Object>> monthCompositeIndex = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(monitortime, monitortime);
            if (monthCompositeIndex.get(monitortime) != null) {
                Map<String, Object> pollutantAndValue = monthCompositeIndex.get(monitortime);
                Double total = Double.parseDouble(pollutantAndValue.get("total").toString());
                Double percentage;

                for (String key : pollutantAndValue.keySet()) {
                    if (!"total".equals(key)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pollutantname", key);
                        map.put("pollutantvalue", pollutantAndValue.get(key).toString());
                        percentage = Double.parseDouble(pollutantAndValue.get(key).toString()) * 100 / total;
                        map.put("percentage", DataFormatUtil.formatDoubleSaveOne(percentage) + "%");
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
     * @date: 2019/6/4 0004 上午 11:59
     * @Description: 组织mongodb中查询的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Integer> getKeyAndNum(List<Document> documents, String key, List<String> codes) {
        String tempValue = "";
        String tempValues[] = null;
        Map<String, Integer> keyAndNum = new HashMap<>();
        for (Document document : documents) {
            if (document.get(key) != null && !document.get(key).equals("")) {
                tempValue = document.getString(key);
                if (tempValue.indexOf(",") > -1) {//含有分割符号
                    tempValues = tempValue.split(",");
                    for (int i = 0; i < tempValues.length; i++) {
                        setKeyAndNum(keyAndNum, tempValues[i]);
                        codes.add(tempValues[i]);
                    }
                } else {
                    setKeyAndNum(keyAndNum, tempValue);
                    codes.add(tempValue);
                }
            }
        }
        return keyAndNum;
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 9:27
     * @Description: 设置map中key的数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setKeyAndNum(Map<String, Integer> keyAndNum, String key) {
        if (keyAndNum.get(key) != null) {
            keyAndNum.put(key, keyAndNum.get(key) + 1);
        } else {
            keyAndNum.put(key, 1);
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/9 0009 上午 8:53
     * @Description: 当前年园区内常规六参数有效数据均值和园区外常规六参数有效数据均值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "getParkAirAQIDataAndCityAirAQIData", method = RequestMethod.POST)
    public Object getParkAirAQIDataAndCityAirAQIData() throws Exception {

        Map<String, Object> dataMap = new HashMap<>();
        try {
            Date nowTime = new Date();
            String ymd = DataFormatUtil.getDateYMDHMS(nowTime);//当前时间
            String year = DataFormatUtil.getDateY(nowTime);//当前年
            String startTime = year + "-01-01 00:00:00"; //当前年的第一天
            Date startDate = DataFormatUtil.getDateYMDHMS(startTime);
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(nowTime)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            //查询从该年第一天到当前日的所有有效城市日数据
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> dayDocuments = cityDayAQIData.getMappedResults();
            Map<String, Object> resultmap = new HashMap<>();
            if (dayDocuments.size() > 0) {
                Double monthSO2 = getAverageByCode(dayDocuments, "a21026");//二氧化硫月平均浓度
                Double monthNO2 = getAverageByCode(dayDocuments, "a21004");//二氧化氮月平均浓度
                Double monthCO = getAverageByCode(dayDocuments, "a21005");//一氧化碳月平均浓度
                Double monthO3 = getAverageByCode(dayDocuments, "a05024");//臭氧月平均浓度
                Double monthPM10 = getAverageByCode(dayDocuments, "a34002");//二PM10月平均浓度
                Double monthPM25 = getAverageByCode(dayDocuments, "a34004");//PM2.5月平均浓度
                resultmap.put("SO2", monthSO2);
                resultmap.put("NO2", monthNO2);
                resultmap.put("CO", monthCO);
                resultmap.put("O3", monthO3);
                resultmap.put("PM10", monthPM10);
                resultmap.put("PM25", monthPM25);
            }
            //获取去年同期六参数因子平均值
            Aggregation lastaggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(nowTime)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            //查询从该年第一天到当前日的所有有效城市日数据
            AggregationResults<Document> lastcityDayAQIData = mongoTemplate.aggregate(lastaggregation, citycollection, Document.class);
            List<Document> lastdayDocuments = lastcityDayAQIData.getMappedResults();
            Map<String, Object> lastresultmap = new HashMap<>();
            if (lastdayDocuments.size() > 0) {
                Double monthSO2 = getAverageByCode(lastdayDocuments, "a21026");//二氧化硫月平均浓度
                Double monthNO2 = getAverageByCode(lastdayDocuments, "a21004");//二氧化氮月平均浓度
                Double monthCO = getAverageByCode(lastdayDocuments, "a21005");//一氧化碳月平均浓度
                Double monthO3 = getAverageByCode(lastdayDocuments, "a05024");//臭氧月平均浓度
                Double monthPM10 = getAverageByCode(lastdayDocuments, "a34002");//二PM10月平均浓度
                Double monthPM25 = getAverageByCode(lastdayDocuments, "a34004");//PM2.5月平均浓度
                lastresultmap.put("SO2", monthSO2);
                lastresultmap.put("NO2", monthNO2);
                lastresultmap.put("CO", monthCO);
                lastresultmap.put("O3", monthO3);
                lastresultmap.put("PM10", monthPM10);
                lastresultmap.put("PM25", monthPM25);
            }
            dataMap.put("parkdata", resultmap);
            dataMap.put("citydata", lastresultmap);
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/6/6 0006 下午 2:13
     * @Description: 计算因子月平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Double getAverageByCode(List<Document> dayDocuments, String code) {
        DecimalFormat df = new DecimalFormat("#.00");
        Double total = 0d;//因子总浓度
        int daynum = 0;//因子有监测数据的天数
        List<Map<String, Object>> pollutantDataList;
        for (Document document : dayDocuments) {
            //获取因子集合
            pollutantDataList = document.get("DataList", List.class);
            Double value = 0d;
            for (Map<String, Object> map : pollutantDataList) {
                if (code.equals(map.get("PollutantCode"))) {//当code相等时
                    value = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                    break;
                }
            }
            if (value != null) {//当值不为空  则是有效天，监测天数加1,因子总浓度累加
                daynum += 1;
                total += value;
            }
        }
        Map<String, Double> result = new HashMap<>();
        if (total > 0) {
            Double value = Double.parseDouble(df.format(total / daynum));
            return value;
        } else {
            return null;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/23 11:01
     * @Description: 获取air quality天数同比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEachAirQualityDaysAndTB", method = RequestMethod.POST)
    public Object getEachAirQualityDaysAndTB(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", starttime, endtime);
            List<Document> documents = airParkDayDataService.countEachAirQualityDaysByTime(dates[0], dates[1], collection);
            String tbStarttime = YearMonth.parse(starttime).withYear(YearMonth.parse(starttime).getYear() - 1).toString();
            String tbEndtime = YearMonth.parse(endtime).withYear(YearMonth.parse(endtime).getYear() - 1).toString();
            Date[] tbDates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", tbStarttime, tbEndtime);
            List<Document> tbDocument = airParkDayDataService.countEachAirQualityDaysByTime(tbDates[0], tbDates[1], collection);
            resultMap.put("tb", tbDocument);
            resultMap.put("current", documents);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/23 11:01
     * @Description: 获取六参数占比天数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getSixPollutantDaysAndTB", method = RequestMethod.POST)
    public Object getSixPollutantDaysAndTB(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<String> sixPollutants = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
            Date[] dates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", starttime, endtime);
            List<Document> documents = airParkDayDataService.getSixPollutantDaysAndTB(dates[0], dates[1], sixPollutants, collection);
            String tbStarttime = YearMonth.parse(starttime).withYear(YearMonth.parse(starttime).getYear() - 1).toString();
            String tbEndtime = YearMonth.parse(endtime).withYear(YearMonth.parse(endtime).getYear() - 1).toString();
            Date[] tbDates = DataFormatUtil.getQueryTimeToQueryVoByTimeType("month", tbStarttime, tbEndtime);
            List<Document> tbDocument = airParkDayDataService.getSixPollutantDaysAndTB(tbDates[0], tbDates[1], sixPollutants, collection);
            Map<String, Integer> currentMap = new HashMap<>();
            Map<String, Integer> tbMap = new HashMap<>();
            for (String pollutant : sixPollutants) {
                currentMap.put(pollutant, 0);
                tbMap.put(pollutant, 0);
            }
            for (Document document : documents) {
                String pollutantcode = document.getString("PollutantCode");
                int num = document.getInteger("num");
                currentMap.put(pollutantcode, num);
            }
            for (Document document : tbDocument) {
                String pollutantcode = document.getString("PollutantCode");
                int num = document.getInteger("num");
                tbMap.put(pollutantcode, num);
            }
            resultMap.put("tb", tbMap);
            resultMap.put("current", currentMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/4/3 0003 下午 7:11
     * @Description: 获取城市空气日数据及标准值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "/getParkAirDataAQIAndIndexData", method = RequestMethod.POST)
    public Object getParkAirDataAQIAndIndexData(@RequestJson(value = "type", required = false) String type, @RequestJson(value = "starttime", required = false) String starttime
                                                , @RequestJson(value = "endtime", required = false) String endtime) {
        try {

            ParkHourAQIDataVO cityHourAQIData = new ParkHourAQIDataVO();
            List<Map<String,Object>> resultList=new ArrayList<>();
            if (StringUtils.isNotBlank(starttime)) {
                Map<String, Object> map = new HashMap<>();
                map.put("starttime", starttime);
                map.put("endtime", endtime);
                cityHourAQIData.setMonitorTime(JSONObject.fromObject(map).toString());
            }
            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            List<ParkHourAQIDataVO> listWithPageByParam = new ArrayList<>();
            //分页获取一条
//            mongoSearchEntity.setPage(0);
//            mongoSearchEntity.setSize(1);
            //按时间倒序getPollutantsByOutputIdAndPollutantType
            mongoSearchEntity.setSortorder(2);
            List<String> sortNames = new ArrayList<>();
            sortNames.add("MonitorTime");
            mongoSearchEntity.setSortname(sortNames);
            if ("city".equals(type)) {
                listWithPageByParam = mongoBaseService.getListWithPageByParam(cityHourAQIData, mongoSearchEntity, citycollection, "yyyy-MM-dd HH:mm:ss");
            } else {
                listWithPageByParam = mongoBaseService.getListWithPageByParam(cityHourAQIData, mongoSearchEntity, collection, "yyyy-MM-dd HH:mm:ss");
            }

            Map<String, Object> params = new HashMap<>();
            //获取污染物码表信息
            params.put("pollutanttype", 5);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(params);
            for (ParkHourAQIDataVO hourAQIData : listWithPageByParam) {
                Map<String, Object> resultMap = new HashMap<>();
                resultList.add(resultMap);
                List<Map<String, Object>> dataList = hourAQIData.getDataList();
                for (Map<String, Object> map : dataList) {
                    if (map.get("PollutantCode") != null) {
                        Map<String, Object> aqiMap = new HashMap<>();
                        String pollutantCode = map.get("PollutantCode").toString();
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
                        pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(pollutantCode)).
                                peek(m -> {
                                    resultMap.put(m.get("name").toString().toLowerCase(), aqiMap);
                                    aqiMap.put("iAQI", map.get("IAQI"));
                                    aqiMap.put("Strength", map.get("Strength"));
                                }).collect(Collectors.toList());
                        //将首要污染物code设置为name
                        pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(hourAQIData.getPrimaryPollutant()))
                                .map(m -> resultMap.put("pimarypllutant", m.get("name").toString())).collect(Collectors.toList());
                        resultMap.put("aqi", hourAQIData.getaQi());
                        resultMap.put("quality", hourAQIData.getAirQuality());
                        resultMap.put("monitorTime", OverAlarmController.format(hourAQIData.getMonitorTime(), "yyyy-MM-dd"));
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
     * @author: xsm
     * @date: 2020/5/19 0019 下午 1:47
     * @Description: 通过日期获取园区内空气质量信息（包括同比环比）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkAirDayDataByMonitorTimeAndDateType", method = RequestMethod.POST)
    public Object getParkAirDayDataByMonitorTimeAndDateType(
            @RequestJson(value = "datetype") String datetype,
            @RequestJson(value = "monitortime") String monitortime
    ) {
        Map<String, Object> resultmap = new LinkedHashMap<>();
        try {
            Date startDate =null;
            Date endDate = null;
            String collectionstr = "";
            if ("day".equals(datetype)){//日
                startDate = DataFormatUtil.getDateYMD(monitortime);
                endDate = DataFormatUtil.getDateYMD(monitortime);
                collectionstr = collection;
            }else if("month".equals(datetype)){//月
                startDate = DataFormatUtil.getDateYM(monitortime);
                endDate = DataFormatUtil.getDateYM(monitortime);
                collectionstr = "ParkMonthAQIData";
            }else if("year".equals(datetype)){//年
                startDate = DataFormatUtil.getDateYMD(monitortime+"-01-01");
                endDate = DataFormatUtil.getDateYMD(monitortime+"-12-31");
                collectionstr = collection;
            }
            List<String> pollutantCodes = Arrays.asList(
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_PM25.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_PM10.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_CO.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_O3.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_NO2.getCode(),
                    CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_SO2.getCode());
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
            Aggregation aggregation=newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                        sort(Sort.Direction.ASC, "MonitorTime")
                );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collectionstr, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            if (documents.size() > 0) {
                if ("day".equals(datetype)||"month".equals(datetype)){//日
                    for (Document document : documents) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        List<Map<String, Object>> pollutantDataList = document.get("DataList", List.class);
                        for (String code:pollutantCodes) {
                            map.put(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code), "");
                            for (Map<String, Object> dataMap : pollutantDataList) {
                                if (code.equals(dataMap.get("PollutantCode"))) {
                                    map.put(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code), ""+dataMap.get("Strength"));
                                    break;
                                }
                            }
                        }
                        resultmap.put("nowdata",map);
                    }
                }else if("year".equals(datetype)){//年
                    Map<String, Object> map = new LinkedHashMap<>();
                    Double monthSO2 = getAverageByCode(documents, "a21026");//二氧化硫年平均浓度
                    Double monthNO2 = getAverageByCode(documents, "a21004");//二氧化氮年平均浓度
                    Double monthCO = getAverageByCode(documents, "a21005");//一氧化碳年平均浓度
                    Double monthO3 = getAverageByCode(documents, "a05024");//臭氧年平均浓度
                    Double monthPM10 = getAverageByCode(documents, "a34002");//二PM10年平均浓度
                    Double monthPM25 = getAverageByCode(documents, "a34004");//PM2.5年平均浓度
                    map.put("SO2", ""+monthSO2);
                    map.put("NO2", ""+monthNO2);
                    map.put("CO", ""+monthCO);
                    map.put("O3", ""+monthO3);
                    map.put("PM10", ""+monthPM10);
                    map.put("PM25", ""+monthPM25);
                    resultmap.put("nowdata",map);
                }
                }else{//无数据 返回空值
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("SO2", "-");
                map.put("NO2", "-");
                map.put("CO", "-");
                map.put("O3", "-");
                map.put("PM10", "-");
                map.put("PM25", "-");
                resultmap.put("nowdata",map);
            }
            /*
             * 同比增长率=（本期数 - 同期数）/同期数×100%
             * 环比增长率=（本期数 - 上期数）/上期数×100%
             * */
           //同比
            countAirParkYearOnYearData(datetype,monitortime,resultmap,pollutantCodes);
           //环比
            countAirParkConsecutiveData(datetype,monitortime,resultmap,pollutantCodes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", resultmap);
    }



    /**
     * @author: chengzq
     * @date: 2020/5/21 0021 下午 1:56
     * @Description: 通过监测时间获取污染指数天数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getPollutionIndexByMonitorTime", method = RequestMethod.POST)
    public Object getPollutionIndexByMonitorTime( @RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime ) {
        List<Map<String,Object>> resultList = new ArrayList<>();
        try {
            Criteria criteria=new Criteria();
            String firstDayOfMonth = DataFormatUtil.getFirstDayOfMonth(starttime)+" 00:00:00";
            String lastDayOfMonth = DataFormatUtil.getLastDayOfMonth(endtime)+" 23:59:59";
            criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(firstDayOfMonth)).lte(DataFormatUtil.getDateYMDHMS(lastDayOfMonth));
            List<Document> mappedResults = mongoTemplate.aggregate(newAggregation(match(criteria),project("AirQuality").and(DateOperators
                            .DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                            , group("AirQuality", "MonitorTime").count().as("count")), collection, Document.class).getMappedResults();
            Map<String, List<Document>> collect = mappedResults.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            for (String MonitorTime : collect.keySet()) {
                Map<String,Object> map=new HashMap<>();
                map.put("monitortime",MonitorTime);
                map.put("datalist",collect.get(MonitorTime));
                resultList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m->m.get("monitortime")!=null).sorted(Comparator.comparing(m->m.get("monitortime").toString())).collect(Collectors.toList()));
    }





    private void countAirParkYearOnYearData(String datetype, String monitortime, Map<String, Object> resultmap, List<String> pollutantCodes) {
        Map<String,Object> nowdata = (Map<String, Object>) resultmap.get("nowdata");
        String[] startstr =  monitortime.split("-");
        Integer startyear = (Integer.parseInt(startstr[0]))-1;
        Date startDate =null;
        Date endDate = null;
        String collectionstr = "";
        if ("day".equals(datetype)){//日
            startDate = DataFormatUtil.getDateYMD(startyear+"-"+startstr[1]+"-"+startstr[2]);
            endDate = DataFormatUtil.getDateYMD(startyear+"-"+startstr[1]+"-"+startstr[2]);
            collectionstr = collection;
        }else if("month".equals(datetype)){//月
            startDate = DataFormatUtil.getDateYM(startyear+"-"+startstr[1]);
            endDate = DataFormatUtil.getDateYM(startyear+"-"+startstr[1]);
            collectionstr = "ParkMonthAQIData";
        }
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation=newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collectionstr, Document.class);
        List<Document> documents = cityDayAQIData.getMappedResults();
        Map<String, Object> map = new LinkedHashMap<>();
        if (documents.size() > 0) {
            if ("day".equals(datetype)||"month".equals(datetype)){//日
                for (Document document : documents) {
                    List<Map<String, Object>> pollutantDataList = document.get("DataList", List.class);
                    for (String code:pollutantCodes) {
                        map.put(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code), "");
                        for (Map<String, Object> dataMap : pollutantDataList) {
                            if (code.equals(dataMap.get("PollutantCode"))) {
                                double lastvalue = dataMap.get("Strength")!=null?Double.parseDouble(dataMap.get("Strength").toString()):0;
                                double thisvalue = (nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code))!=null&&!"-".equals(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code)).toString()))
                                        ?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code)).toString()):0;
                                String tbvalue = getYearOnYear(lastvalue,thisvalue,"-");
                                map.put(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code), ""+tbvalue);
                                break;

                            }
                        }
                    }
                }
            }
        }else{//无数据 返回空值
            map.put("SO2", "-");
            map.put("NO2", "-");
            map.put("CO", "-");
            map.put("O3", "-");
            map.put("PM10", "-");
            map.put("PM25", "-");
        }
        resultmap.put("yearonyear",map);
    }

    private void countAirParkConsecutiveData(String datetype, String monitortime, Map<String, Object> resultmap, List<String> pollutantCodes) throws ParseException {

        Map<String,Object> nowdata = (Map<String, Object>) resultmap.get("nowdata");
        String[] startstr =  monitortime.split("-");
        Integer startyear = (Integer.parseInt(startstr[0]))-1;
        Date startDate =null;
        Date endDate = null;
        String collectionstr = "";
        if ("day".equals(datetype)){//日
            //获取上一个时间
            SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd");
            Date  currdate = sd.parse(monitortime);
            Calendar   calendar= Calendar.getInstance();
            calendar.setTime(currdate);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            startDate = calendar.getTime();
            endDate = calendar.getTime();
            collectionstr = collection;
        }else if("month".equals(datetype)){//月
            SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM");
            Date  currdate = sd.parse(monitortime);
            Calendar   calendar= Calendar.getInstance();
            calendar.setTime(currdate);
            calendar.add(Calendar.MONTH, -1);
            startDate = calendar.getTime();
            endDate = calendar.getTime();
            collectionstr = "ParkMonthAQIData";
        }else if("year".equals(datetype)){//年
            startDate = DataFormatUtil.getDateYMD(startyear+"-01-01");
            endDate = DataFormatUtil.getDateYMD(startyear+"-12-31");
            collectionstr = collection;
        }
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation=newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collectionstr, Document.class);
        List<Document> documents = cityDayAQIData.getMappedResults();
        Map<String, Object> map = new LinkedHashMap<>();
        if (documents.size() > 0) {
            if ("day".equals(datetype)||"month".equals(datetype)){//日
                for (Document document : documents) {
                    List<Map<String, Object>> pollutantDataList = document.get("DataList", List.class);
                    for (String code:pollutantCodes) {
                        map.put(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code), "");
                        for (Map<String, Object> dataMap : pollutantDataList) {
                            if (code.equals(dataMap.get("PollutantCode"))) {
                                double lastvalue = dataMap.get("Strength")!=null?Double.parseDouble(dataMap.get("Strength").toString()):0;
                                double thisvalue = (nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code))!=null&&!"-".equals(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code)).toString()))
                                        ?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code)).toString()):0;
                                String tbvalue = getYearOnYear(lastvalue,thisvalue,"-");
                                map.put(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code), tbvalue);
                                break;

                            }
                        }
                    }
                }
            }else if("year".equals(datetype)){//年
                Double monthSO2 = getAverageByCode(documents, "a21026");//二氧化硫年平均浓度
                double thismonthSO2 = nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a21026"))!=null?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a21026")).toString()):0;
                Double monthNO2 = getAverageByCode(documents, "a21004");//二氧化氮年平均浓度
                double thismonthNO2 = nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a21004"))!=null?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a21004")).toString()):0;
                Double monthCO = getAverageByCode(documents, "a21005");//一氧化碳年平均浓度
                double thismonthCO = nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a21005"))!=null?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a21005")).toString()):0;
                Double monthO3 = getAverageByCode(documents, "a05024");//臭氧年平均浓度
                double thismonthO3 = nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a05024"))!=null?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a05024")).toString()):0;
                Double monthPM10 = getAverageByCode(documents, "a34002");//二PM10年平均浓度
                double thismonthPM10 = nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a34002"))!=null?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a34002")).toString()):0;
                Double monthPM25 = getAverageByCode(documents, "a34004");//PM2.5年平均浓度
                double thismonthPM25 = nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a34004"))!=null?Double.parseDouble(nowdata.get(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode("a34004")).toString()):0;

                map.put("SO2", getYearOnYear(monthSO2,thismonthSO2,"-"));
                map.put("NO2", getYearOnYear(monthNO2,thismonthNO2,"-"));
                map.put("CO", getYearOnYear(monthCO,thismonthCO,"-"));
                map.put("O3", getYearOnYear(monthO3,thismonthO3,"-"));
                map.put("PM10", getYearOnYear(monthPM10,thismonthPM10,"-"));
                map.put("PM25", getYearOnYear(monthPM25,thismonthPM25,"-"));
            }
        }else{//无数据 返回空值
            map.put("SO2", "-");
            map.put("NO2", "-");
            map.put("CO", "-");
            map.put("O3", "-");
            map.put("PM10", "-");
            map.put("PM25", "-");
        }
        resultmap.put("lastdata",map);
    }

    public static String getYearOnYear(Double lastFlow, Double thisFlow,String defaultstr) {
        DecimalFormat format = new DecimalFormat("0.##");
        if (lastFlow == 0 || thisFlow == 0) {
            return defaultstr;
        }

        //都不为0计算同比
        double data = (thisFlow - lastFlow) / lastFlow * 100;

        if (data > 0) {
            return  ""+format.format(Math.abs(data));
        } else if (data < 0) {
            return "-" + format.format(Math.abs(data));
        } else {
            return format.format(data)+"";
        }
    }
}
