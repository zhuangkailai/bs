package com.tjpu.sp.service.impl.environmentalprotection.envquality;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.common.pubcode.PubCodeMapper;
import com.tjpu.sp.service.environmentalprotection.envquality.AirCityMonthDataService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class AirCityMonthDataServiceImpl implements AirCityMonthDataService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String hourCollection = "CityHourAQIData";
    private final String dayCollection = "CityDayAQIData";
    @Autowired
    private PubCodeMapper pubCodeMapper;

    /**
     * @author: chengzq
     * @date: 2019/6/5 0005 下午 6:33
     * @Description: 获取一段时间园区外月的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:月份开始日期（yyyy-mm）
     * @param: endtime:月份结束日期（yyyy-mm）
     * @param: type:默认查询园区，当为"city"时查询城市综合指数
     * @return: key：yyyy-mm，value:综合指数值map
     */
    @Override
    public Map<String, Map<String, Object>> getAirCityMonthCompositeIndexByMonitorTimes(String starttime, String endtime) throws Exception {

        starttime = starttime + "-01";
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        //获取一段时间的小时数据

        Map<String, Map<String, Object>> monthAndCompositeIndex = new LinkedHashMap<>();

        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);

        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityHourAQIData;
        cityHourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);

        List<Document> hourDocuments = cityHourAQIData.getMappedResults();
        //根据小时数据获取有效日期
        if (hourDocuments.size() > 0) {

            String tempYMD = "";
            Map<String, Integer> dayAndNum = new HashMap<>();
            Map<String, Map<String, Integer>> codeAndDayAndNum = new HashMap<>();
            String code;

            List<Map<String, Object>> pollutantDataList;
            for (Document document : hourDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                pollutantDataList = document.get("DataList", List.class);
                for (Map<String, Object> map : pollutantDataList) {
                    if (map.get("PollutantCode") != null && map.get("Strength") != null) {
                        code = map.get("PollutantCode").toString();
                        if (codeAndDayAndNum.get(code) != null) {
                            dayAndNum = codeAndDayAndNum.get(code);
                            if (dayAndNum.get(tempYMD) != null) {
                                dayAndNum.put(tempYMD, dayAndNum.get(tempYMD) + 1);
                            } else {
                                dayAndNum.put(tempYMD, 1);
                            }
                            codeAndDayAndNum.put(code, dayAndNum);
                        } else {
                            dayAndNum = new HashMap<>();
                            dayAndNum.put(tempYMD, 1);
                            codeAndDayAndNum.put(code, dayAndNum);
                        }
                    }
                }
            }
            int num = 20;
            for (String codeKey : codeAndDayAndNum.keySet()) {
                dayAndNum = codeAndDayAndNum.get(codeKey);
                if ("a05024".equals(codeKey)) {//臭氧
                    num = 18;
                }
                List<String> effectiveHourList = new ArrayList<>();
                for (String dayKey : dayAndNum.keySet()) {
                    if (dayAndNum.get(dayKey) >= num) {
                        effectiveHourList.add(dayKey);
                        codeAndEffectiveDay.put(codeKey, effectiveHourList);
                    }
                }

            }
        }
        //根据日数据获取有效日数据
        startDate = DataFormatUtil.getDateYMD(starttime + " 00");
        endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData;
        cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> dayDocuments = cityDayAQIData.getMappedResults();
        if (dayDocuments.size() > 0) {
            Map<String, Double> monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
            Map<String, Double> monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
            Map<String, Double> monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
            Map<String, Double> monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
            Map<String, Double> monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
            Map<String, Double> monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
            Double compositeIndex;
            Double SO2;
            Double NO2;
            Double CO;
            Double O3;
            Double PM10;
            Double PM25;
            for (String ym : yms) {
                SO2 = monthSO2.get(ym) != null ? monthSO2.get(ym) : 0d;
                NO2 = monthNO2.get(ym) != null ? monthNO2.get(ym) : 0d;
                CO = monthCO.get(ym) != null ? monthCO.get(ym) : 0d;
                O3 = monthO3.get(ym) != null ? monthO3.get(ym) : 0d;
                PM10 = monthPM10.get(ym) != null ? monthPM10.get(ym) : 0d;
                PM25 = monthPM25.get(ym) != null ? monthPM25.get(ym) : 0d;
                compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("total", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                map.put("SO2", SO2);
                map.put("NO2", NO2);
                map.put("CO", CO);
                map.put("O3", O3);
                map.put("PM10", PM10);
                map.put("PM25", PM25);
                monthAndCompositeIndex.put(ym, map);
            }
        }
        return monthAndCompositeIndex;
    }


    @Override
    public Map<String, Map<String, Object>> getMonthCompositeIndexByParam(Map<String, Object> param) {


        String monitortime = (String) param.get("monitortime");
        String starttime = monitortime + "-01";
        String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
        String mn = (String) param.get("dgimn");
        //获取一段时间的小时数据

        Map<String, Map<String, Object>> monthAndCompositeIndex = new LinkedHashMap<>();

        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();


        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("RegionCode").is(mn)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityHourAQIData;
        cityHourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);

        List<Document> hourDocuments = cityHourAQIData.getMappedResults();
        //根据小时数据获取有效日期
        if (hourDocuments.size() > 0) {

            String tempYMD = "";
            Map<String, Integer> dayAndNum = new HashMap<>();
            Map<String, Map<String, Integer>> codeAndDayAndNum = new HashMap<>();
            String code;

            List<Map<String, Object>> pollutantDataList;
            for (Document document : hourDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                pollutantDataList = document.get("DataList", List.class);
                for (Map<String, Object> map : pollutantDataList) {
                    if (map.get("PollutantCode") != null && map.get("Strength") != null) {
                        code = map.get("PollutantCode").toString();
                        if (codeAndDayAndNum.get(code) != null) {
                            dayAndNum = codeAndDayAndNum.get(code);
                            if (dayAndNum.get(tempYMD) != null) {
                                dayAndNum.put(tempYMD, dayAndNum.get(tempYMD) + 1);
                            } else {
                                dayAndNum.put(tempYMD, 1);
                            }
                            codeAndDayAndNum.put(code, dayAndNum);
                        } else {
                            dayAndNum = new HashMap<>();
                            dayAndNum.put(tempYMD, 1);
                            codeAndDayAndNum.put(code, dayAndNum);
                        }
                    }
                }
            }
            int num = 20;
            for (String codeKey : codeAndDayAndNum.keySet()) {
                dayAndNum = codeAndDayAndNum.get(codeKey);
                if ("a05024".equals(codeKey)) {//臭氧
                    num = 18;
                }
                List<String> effectiveHourList = new ArrayList<>();
                for (String dayKey : dayAndNum.keySet()) {
                    if (dayAndNum.get(dayKey) >= num) {
                        effectiveHourList.add(dayKey);
                        codeAndEffectiveDay.put(codeKey, effectiveHourList);
                    }
                }

            }
        }
        //根据日数据获取有效日数据
        startDate = DataFormatUtil.getDateYMD(starttime + " 00");
        endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("RegionCode").is(mn)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData;
        cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> dayDocuments = cityDayAQIData.getMappedResults();
        if (dayDocuments.size() > 0) {
            Map<String, Double> monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
            Map<String, Double> monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
            Map<String, Double> monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
            Map<String, Double> monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
            Map<String, Double> monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
            Map<String, Double> monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
            Double compositeIndex;
            Double SO2;
            Double NO2;
            Double CO;
            Double O3;
            Double PM10;
            Double PM25;
            String ym = monitortime;
            SO2 = monthSO2.get(ym) != null ? monthSO2.get(ym) : 0d;
            NO2 = monthNO2.get(ym) != null ? monthNO2.get(ym) : 0d;
            CO = monthCO.get(ym) != null ? monthCO.get(ym) : 0d;
            O3 = monthO3.get(ym) != null ? monthO3.get(ym) : 0d;
            PM10 = monthPM10.get(ym) != null ? monthPM10.get(ym) : 0d;
            PM25 = monthPM25.get(ym) != null ? monthPM25.get(ym) : 0d;
            compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("total", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
            map.put("SO2", SO2);
            map.put("NO2", NO2);
            map.put("CO", CO);
            map.put("O3", O3);
            map.put("PM10", PM10);
            map.put("PM25", PM25);
            monthAndCompositeIndex.put(ym, map);

        }
        return monthAndCompositeIndex;
    }

    @Override
    public List<Map<String, Object>> getRegionDataList() {
        return pubCodeMapper.getRegionDataList();
    }


    /**
     * @author: lip
     * @date: 2019/6/6 0006 上午 10:23
     * @Description: 计算百分位加和项
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Double> getSumPercentileItemByCode(List<Document> dayDocuments, Map<String, List<String>> codeAndEffectiveDay, String code, int percentile, int standardValue) {

        Map<String, Double> monthAndValue = new HashMap<>();
        List<String> effectiveHourList = codeAndEffectiveDay.get(code);
        if (effectiveHourList != null) {
            String tempYMD;
            String tempYM;
            Double strength = 0d;
            List<Map<String, Object>> pollutantDataList;
            Map<String, Integer> monthAndNum = new HashMap<>();
            Map<String, List<Double>> monthAndStrength = new HashMap<>();
            for (Document document : dayDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (effectiveHourList.contains(tempYMD)) {
                    tempYM = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> map : pollutantDataList) {
                        if (code.equals(map.get("PollutantCode"))) {
                            strength = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                            break;
                        }
                    }
                    if (strength != null) {
                        if (monthAndNum.get(tempYM) != null) {
                            monthAndNum.put(tempYM, monthAndNum.get(tempYM) + 1);
                            List<Double> strengths = monthAndStrength.get(tempYM);
                            strengths.add(strength);
                            monthAndStrength.put(tempYM, monthAndStrength.get(tempYM));
                        } else {
                            monthAndNum.put(tempYM, 1);
                            List<Double> strengths = new ArrayList<>();
                            strengths.add(strength);
                            monthAndStrength.put(tempYM, strengths);
                        }
                    }
                }
            }


            //计算 k=1+(n-1)p%  -> mp=X(s)+(X(s+1)-X(s))×(k-s）
            //n：有效天数；p:Percentile
            int n;
            Double k;
            int p = percentile;
            int s;
            Double mp;
            Double value;
            for (String monthKey : monthAndStrength.keySet()) {
                n = monthAndNum.get(monthKey);
                k = 1 + (n - 1) * p / 100d;
                List<Double> strengths = monthAndStrength.get(monthKey);
                if (strengths.size() > 1) {
                    Collections.sort(strengths);
                    s = (int) Math.floor(k) - 1;
                    mp = strengths.get(s) + (strengths.get(s + 1) - strengths.get(s)) * (k - s);
                    mp = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp));
                    value = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp / standardValue));
                    monthAndValue.put(monthKey, value);
                }

            }
        }

        return monthAndValue;
    }

    /**
     * @author: lip
     * @date: 2019/6/6 0006 上午 10:23
     * @Description: 计算普通加和项
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Double> getSumItemByCode(List<Document> dayDocuments, Map<String, List<String>> codeAndEffectiveDay, String code, int standardValue) {
        List<String> effectiveHourList = codeAndEffectiveDay.get(code);
        Map<String, Double> monthAndValue = new HashMap<>();
        if (effectiveHourList != null) {
            Double sumItem = 0d;
            String tempYMD;
            String tempYM;
            Double strength = 0d;
            List<Map<String, Object>> pollutantDataList;
            Map<String, Integer> monthAndNum = new HashMap<>();
            Map<String, Double> monthAndTotal = new HashMap<>();

            for (Document document : dayDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (effectiveHourList.contains(tempYMD)) {
                    tempYM = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> map : pollutantDataList) {
                        if (code.equals(map.get("PollutantCode"))) {
                            strength = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                            break;
                        }
                    }
                    if (strength != null) {
                        if (monthAndNum.get(tempYM) != null) {
                            monthAndNum.put(tempYM, monthAndNum.get(tempYM) + 1);
                            monthAndTotal.put(tempYM, monthAndTotal.get(tempYM) + strength);
                        } else {
                            monthAndNum.put(tempYM, 1);
                            monthAndTotal.put(tempYM, strength);
                        }
                    }
                }
            }
            int value;
            for (String monthKey : monthAndTotal.keySet()) {
                value = (int) Math.round(monthAndTotal.get(monthKey) / monthAndNum.get(monthKey));
                sumItem = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", (double) value / standardValue));
                monthAndValue.put(monthKey, sumItem);
            }
        }
        return monthAndValue;
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

}
