package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.envquality.AirCityMonthDataService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirParkMonthDataService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
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

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: lip
 * @date: 2019/6/6 0006 上午 9:00
 * @Description: 城市空气月数据操作类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("airParkMonthData")
public class AirParkMonthDataController {

    @Autowired
    private PollutantService pollutantService;

    @Autowired
    private AirParkMonthDataService airParkMonthDataService;
    @Autowired
    private AirCityMonthDataService airCityMonthDataService;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String collection = "ParkMonthAQIData";

    /**
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 通过监测污染物和时间段获取城市空气质量月数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getParkAirMonthDataByPollutantCodeAndMonitorTimes", method = RequestMethod.POST)
    public Object getParkAirMonthDataByPollutantCodeAndMonitorTimes(
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
                        map.put("monitortime", DataFormatUtil.getDateYM(document.getDate("MonitorTime")));
                        map.put("aqi", document.get("AQI"));
                        map.put("primarypollutant", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                        dataList.add(map);
                    }
                } else if (pollutantcode.equals("compositeindex")) {//
                    String ym;
                    Map<String, Map<String, Object>> monthCompositeIndex =
                            airCityMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(DataFormatUtil.getDateYM(startDate), DataFormatUtil.getDateYM(endDate));
                    for (Document document : documents) {
                        ym = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("monitortime", ym);
                        map.put("aqi", document.get("AQI"));
                        map.put("compositeindex", getCompositeIndex(ym, monthCompositeIndex));
                        dataList.add(map);
                    }
                } else {//污染物
                    for (Document document : documents) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("monitortime", DataFormatUtil.getDateYM(document.getDate("MonitorTime")));
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
     * @date: 2019/7/1 0001 上午 9:28
     * @Description: 根据月份获取综合指数值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object getCompositeIndex(String ym, Map<String, Map<String, Object>> monthCompositeIndex) {
        if (monthCompositeIndex.get(ym) != null) {
            return monthCompositeIndex.get(ym).get("total");
        } else {
            return null;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 9:28
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
     * @author: zhangzc
     * @date: 2019/8/23 14:10
     * @Description: 园区内综合指数同比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countParkCompositeIndexAndTBData", method = RequestMethod.POST)
    public Object countParkCompositeIndexAndTBData(@RequestJson(value = "type") String type) throws Exception {
        try {
            YearMonth now = YearMonth.now();
            YearMonth tbTime = now.withYear(now.getYear() - 1);
            Map<String, Object> currentMap = new HashMap<>();
            Map<String, Object> tbMap = new HashMap<>();
            if (type.equals("month")) {
                Map<String, Map<String, Object>> currentData = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(now.toString(), now.toString());
                Map<String, Map<String, Object>> tbDate = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(tbTime.toString(), tbTime.toString());
                if (currentData.size() > 0) {
                    currentMap = currentData.get(now.toString());
                }
                if (tbDate.size() > 0) {
                    tbMap = tbDate.get(tbTime.toString());
                }
            } else if (type.equals("year")) {
                String nowStart = now.getYear() + "-01";
                String tbStart = tbTime.getYear() + "-01";
                YearMonth parse = YearMonth.parse(nowStart);
                int i = now.compareTo(parse) + 1;
                Map<String, Map<String, Object>> currentData = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(nowStart, now.toString());
                Map<String, Map<String, Object>> tbDate = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(tbStart, tbTime.toString());
                currentMap = getAvgCompositeIndex(currentData, i);
                tbMap = getAvgCompositeIndex(tbDate, i);
            }
            for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
                double v = Double.parseDouble(currentMap.get(entry.getKey()).toString());
                currentMap.replace(entry.getKey(), BigDecimal.valueOf(v).setScale(2, ROUND_HALF_DOWN).doubleValue());
            }
            double value = 0;
            double totalValue = 0;
            if (currentMap.size() == 0) {
                currentMap.put("total", totalValue);
                currentMap.put("SO2", value);
                currentMap.put("NO2", value);
                currentMap.put("CO", value);
                currentMap.put("O3", value);
                currentMap.put("PM10", value);
                currentMap.put("PM25", value);
            }
            if (tbMap.size() == 0) {
                tbMap.put("total", totalValue);
                tbMap.put("SO2", value);
                tbMap.put("NO2", value);
                tbMap.put("CO", value);
                tbMap.put("O3", value);
                tbMap.put("PM10", value);
                tbMap.put("PM25", value);
            }
            double total = Double.parseDouble(currentMap.get("total").toString());
            double tbTotal = Double.parseDouble(tbMap.get("total").toString());
            double compare = BigDecimal.valueOf(total - tbTotal).setScale(2, ROUND_HALF_DOWN).doubleValue();
            String s = "";
            if (compare > 0) {
                s = "同比增加" + Math.abs(compare);
            } else if (compare < 0) {
                s = "同比减少" + Math.abs(compare);
            } else if (compare == 0) {
                s = "同比减少0";
            }
            currentMap.put("compare", s);
            return AuthUtil.parseJsonKeyToLower("success", currentMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/26 14:25
     * @Description: 求取一年的平均值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getAvgCompositeIndex(Map<String, Map<String, Object>> mapMap, int i) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : mapMap.entrySet()) {
            Map<String, Object> value = entry.getValue();
            for (Map.Entry<String, Object> entry1 : value.entrySet()) {
                String key = entry1.getKey();
                String value1 = entry1.getValue().toString();
                double v = Double.parseDouble(value1);
                if (resultMap.containsKey(key)) {
                    Double aDouble = (Double) resultMap.get(key);
                    double v1 = aDouble + v;
                    resultMap.put(key, v1);
                } else {
                    resultMap.put(key, v);
                }
            }
        }
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            double aDouble = (double) resultMap.get(entry.getKey());
            double v = aDouble / i;
            if (entry.getKey().equals("total")) {
                resultMap.put(entry.getKey(), v + "");
            } else {
                resultMap.put(entry.getKey(), v);
            }
        }
        return resultMap;
    }
}
