package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.AirMonitorStationMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirMonitorStationVO;
import com.tjpu.sp.service.environmentalprotection.envquality.AirCityMonthDataService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.impl.environmentalprotection.envquality.AirCityMonthDataServiceImpl;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class AirMonitorStationServiceImpl implements AirMonitorStationService {

    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    private final String hourCollection = "StationHourAQIData";
    private final String dayCollection = "StationDayAQIData";
    private final String monthCollection = "StationMonthAQIData";

    private final String DB_CityDayAQIData = "CityDayAQIData";
    private final String DB_CityHourAQIData = "CityHourAQIData";
    private final String DB_HourData = "HourData";
    private final String DB_DayData = "DayData";

    private final AirMonitorStationMapper airMonitorStationMapper;
    private final PollutantFactorMapper pollutantFactorMapper;


    public AirMonitorStationServiceImpl(AirMonitorStationMapper airMonitorStationMapper, PollutantFactorMapper pollutantFactorMapper) {
        this.airMonitorStationMapper = airMonitorStationMapper;
        this.pollutantFactorMapper = pollutantFactorMapper;
    }

    /**
     * @author: lip
     * @date: 2018/9/11 0011 下午 4:23
     * @Description: 自定义查询参数查询总的记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return airMonitorStationMapper.countTotalByParam(paramMap);

    }

    /**
     * @author: zhangzc
     * @date: 2019/5/28 16:20
     * @Description: 获取在线空气监测点信息
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnlineAirStationInfoByParamMap(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getOnlineAirStationInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/4 0004 下午 5:49
     * @Description: 获取所有空气站信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllAirMonitorStation(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAllAirMonitorStation(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/6/5 0005 下午 6:33
     * @Description: 获取单月多站点的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: month:月份日期（yyyy-mm）
     * @return: key：stationCode，value:综合指数值的map
     */
    @Override
    public Map<String, Map<String, Object>> getOneMonthManyStationMonthCompositeIndex(String month) throws Exception {
        String starttime = month + "-01";
        String endtime = DataFormatUtil.getLastDayOfMonth(month);
        //获取一段时间的小时数据
        Map<String, Map<String, Object>> stationCodeAndCompositeIndex = new LinkedHashMap<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        Set<String> stationCodes = new LinkedHashSet<>();
        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> hourDocuments = hourAQIData.getMappedResults();
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
                        stationCodes.add(document.getString("StationCode"));
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
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
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
            for (String stationCode : stationCodes) {
                SO2 = monthSO2.get(stationCode) != null ? monthSO2.get(stationCode) : 0d;
                NO2 = monthNO2.get(stationCode) != null ? monthNO2.get(stationCode) : 0d;
                CO = monthCO.get(stationCode) != null ? monthCO.get(stationCode) : 0d;
                O3 = monthO3.get(stationCode) != null ? monthO3.get(stationCode) : 0d;
                PM10 = monthPM10.get(stationCode) != null ? monthPM10.get(stationCode) : 0d;
                PM25 = monthPM25.get(stationCode) != null ? monthPM25.get(stationCode) : 0d;
                compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("total", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                map.put("SO2", SO2);
                map.put("NO2", NO2);
                map.put("CO", CO);
                map.put("O3", O3);
                map.put("PM10", PM10);
                map.put("PM2.5", PM25);
                stationCodeAndCompositeIndex.put(stationCode, map);
            }
        }
        return stationCodeAndCompositeIndex;
    }


    /**
     * @author: chengzq
     * @date: 2019/7/2 0002 下午 4:12
     * @Description: 获取时间段内多站点的首要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getManyStationMonthCompositeIndexByMonitorTimes(String starttime, String endtime) throws Exception {

        starttime = starttime + "-01";
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        //获取一段时间的小时数据

        Set<String> stationCodes = new LinkedHashSet<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);

        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "StationCode", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        List<Map<String, Object>> allAirMonitorStation = airMonitorStationMapper.getAllAirMonitorStation(new HashMap<>());
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> hourDocuments = hourAQIData.getMappedResults();
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
                        stationCodes.add(document.getString("StationCode"));
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
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> dayDocuments = cityDayAQIData.getMappedResults();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Map<Date, List<Document>> collect = dayDocuments.stream().filter(m -> m.get("MonitorTime") != null).peek(m -> {
            try {
                m.put("MonitorTime", format.parse(format(m.get("MonitorTime").toString(), "yyyy-MM")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }).collect(Collectors.groupingBy(m -> m.getDate("MonitorTime")));
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Date date : collect.keySet()) {
            String tempYMD = DataFormatUtil.getDateYM(date);
            if (collect.get(date).size() > 0) {
                Map<String, Object> datas = new HashMap<>();
                List<Document> documents = collect.get(date);
                Map<String, List<Document>> collect1 = documents.stream().filter(m -> m.get("StationCode") != null).collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
                for (String stationCode : collect1.keySet()) {
                    Map<String, Double> monthSO2 = getSumItemByCode(collect.get(date), codeAndEffectiveDay, "a21026", 60);
                    Map<String, Double> monthNO2 = getSumItemByCode(collect.get(date), codeAndEffectiveDay, "a21004", 40);
                    Map<String, Double> monthCO = getSumPercentileItemByCode(collect.get(date), codeAndEffectiveDay, "a21005", 95, 4);
                    Map<String, Double> monthO3 = getSumPercentileItemByCode(collect.get(date), codeAndEffectiveDay, "a05024", 90, 160);
                    Map<String, Double> monthPM10 = getSumItemByCode(collect.get(date), codeAndEffectiveDay, "a34002", 70);
                    Map<String, Double> monthPM25 = getSumItemByCode(collect.get(date), codeAndEffectiveDay, "a34004", 35);
                    Double SO2 = monthSO2.get(stationCode) != null ? monthSO2.get(stationCode) : 0d;
                    Double NO2 = monthNO2.get(stationCode) != null ? monthNO2.get(stationCode) : 0d;
                    Double CO = monthCO.get(stationCode) != null ? monthCO.get(stationCode) : 0d;
                    Double O3 = monthO3.get(stationCode) != null ? monthO3.get(stationCode) : 0d;
                    Double PM10 = monthPM10.get(stationCode) != null ? monthPM10.get(stationCode) : 0d;
                    Double PM25 = monthPM25.get(stationCode) != null ? monthPM25.get(stationCode) : 0d;
                    Double compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                    datas.put("total", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                    datas.put("SO2", SO2);
                    datas.put("NO2", NO2);
                    datas.put("CO", CO);
                    datas.put("O3", O3);
                    datas.put("PM10", PM10);
                    datas.put("PM25", PM25);
                    List<Map<String, Object>> collect2 = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null && m.get("DGIMN").toString().equals(stationCode)).collect(Collectors.toList());
                    datas.put("dgimn", stationCode);
                    datas.put("monitortime", tempYMD);
                    if (collect2.size() > 0) {
                        datas.put("monitorname", collect2.get(0).get("MonitorPointName"));
                    } else {
                        datas.put("monitorname", "");
                    }
                }
                resultList.add(datas);
            }
        }

        return resultList;
    }

    /**
     * @author: xsm
     * @date: 2019/7/8 0008 上午 11:37
     * @Description: 根据监测点名称和MN号获取某点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    @Override
    public Map<String, Object> selectAirStationInfoByPointNameAndDgimn(Map<String, Object> params) {
        return airMonitorStationMapper.selectAirStationInfoByPointNameAndDgimn(params);
    }

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/30 17:02
//     * @Description: 条件查询空气监测站信息污染物信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    @Override
//    public List<Map<String, Object>> getAirStationPollutants(Map<String, Object> paramMap) {
//        return airMonitorStationMapper.getAirStationPollutants(paramMap);
//    }


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
        Map<String, Double> stationAndValue = new HashMap<>();
        List<String> effectiveHourList = codeAndEffectiveDay.get(code);
        if (effectiveHourList != null) {
            String tempYMD;
            String tempStationCode;
            Double strength = 0d;
            List<Map<String, Object>> pollutantDataList;
            Map<String, Integer> stationAndNum = new HashMap<>();
            Map<String, List<Double>> stationAndStrength = new HashMap<>();
            for (Document document : dayDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (effectiveHourList.contains(tempYMD)) {
                    tempStationCode = document.getString("StationCode");
                    pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> map : pollutantDataList) {
                        if (code.equals(map.get("PollutantCode"))) {
                            strength = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                            break;
                        }
                    }
                    if (strength != null) {
                        if (stationAndNum.get(tempStationCode) != null) {
                            stationAndNum.put(tempStationCode, stationAndNum.get(tempStationCode) + 1);
                            List<Double> strengths = stationAndStrength.get(tempStationCode);
                            strengths.add(strength);
                            stationAndStrength.put(tempStationCode, stationAndStrength.get(tempStationCode));
                        } else {
                            stationAndNum.put(tempStationCode, 1);
                            List<Double> strengths = new ArrayList<>();
                            strengths.add(strength);
                            stationAndStrength.put(tempStationCode, strengths);
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
            for (String stationKey : stationAndStrength.keySet()) {
                n = stationAndNum.get(stationKey);
                k = 1 + (n - 1) * p / 100d;
                List<Double> strengths = stationAndStrength.get(stationKey);
                if (strengths.size() > 1) {
                    Collections.sort(strengths);
                    s = (int) Math.floor(k) - 1;
                    mp = strengths.get(s) + (strengths.get(s + 1) - strengths.get(s)) * (k - s);
                    mp = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp));
                    value = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp / standardValue));
                    stationAndValue.put(stationKey, value);
                }
            }
        }
        return stationAndValue;
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

        Map<String, Double> stationAndValue = new HashMap<>();
        List<String> effectiveHourList = codeAndEffectiveDay.get(code);
        if (effectiveHourList != null) {

            Double sumItem = 0d;
            String tempYMD;
            String tempStationCode;
            Double strength = 0d;
            List<Map<String, Object>> pollutantDataList;
            Map<String, Integer> stationAndNum = new HashMap<>();
            Map<String, Double> stationAndTotal = new HashMap<>();
            for (Document document : dayDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (effectiveHourList.contains(tempYMD)) {
                    tempStationCode = document.getString("StationCode");
                    pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> map : pollutantDataList) {
                        if (code.equals(map.get("PollutantCode"))) {
                            strength = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                            break;
                        }
                    }
                    if (strength != null) {
                        if (stationAndNum.get(tempStationCode) != null) {
                            stationAndNum.put(tempStationCode, stationAndNum.get(tempStationCode) + 1);
                            stationAndTotal.put(tempStationCode, stationAndTotal.get(tempStationCode) + strength);
                        } else {
                            stationAndNum.put(tempStationCode, 1);
                            stationAndTotal.put(tempStationCode, strength);
                        }
                    }
                }
            }

            int value;
            for (String stationKey : stationAndTotal.keySet()) {
                value = (int) Math.round(stationAndTotal.get(stationKey) / stationAndNum.get(stationKey));
                sumItem = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", (double) value / standardValue));
                stationAndValue.put(stationKey, sumItem);
            }
        }
        return stationAndValue;
    }

    /**
     * @author: xsm
     * @date: 2019/6/6 0006 下午 5:54
     * @Description: 获取单月多站点的首要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: month:月份日期（yyyy-mm）
     * @return: key：stationCode，value:首要污染物
     */
    @Override
    public Map<String, String> getOneMonthManyStationMonthPrimarypollutant(String monitortime) {
        String starttime = monitortime + "-01";
        String endtime = DataFormatUtil.getLastDayOfMonth(monitortime);
        //转换时间
        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Set<String> stationCodes = new LinkedHashSet<>();
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> datas = mongoTemplate.aggregate(aggregation, monthCollection, Document.class);
        List<Document> monthDocuments = datas.getMappedResults();
        Map<String, String> resultmap = new HashMap<>();
        if (monthDocuments.size() > 0) {
            for (Document document : monthDocuments) {
                String stationmn = document.getString("StationCode");
                String code = document.getString("PrimaryPollutant");
                if (code != null && !"".equals(code)) {
                    Map<String, Object> parammap = new HashMap<>();
                    List<String> list = new ArrayList<>();
                    list.add(code);
                    parammap.put("pollutanttype", 5);
                    parammap.put("codes", list);
                    List<Map<String, Object>> primarypollutant = pollutantFactorMapper.getPollutantsByCodesAndType(parammap);
                    resultmap.put(stationmn, (primarypollutant != null && primarypollutant.size() > 0 ? (primarypollutant.get(0)).get("name").toString() : ""));
                } else {
                    resultmap.put(stationmn, "");
                }
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2019/6/6 0006 下午 5:54
     * @Description: 生成站点的综合指数月排名excel表格
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public HSSFWorkbook getCityAirCompositeByMonitortime(JSONArray jsonArray) {
        String[] excelHeader0 = {"监测点名称", "综合指数", "上月", "上月", "去年", "去年", "最大指数", "首要污染物"};
        //  “0,2,0,0”  ===>  “起始行，截止行，起始列，截止列”
        String[] headnum0 = {"0,1,0,0", "0,1,1,1", "0,0,2,3", "0,0,4,5", "0,1,6,6", "0,1,7,7"};

        //第二行表头字段，其中的空的双引号是为了补全表格边框
        String[] excelHeader1 = {"综合指数", "变化幅度", "综合指数", "变化幅度"};
        // 合并单元格
        String[] headnum1 = {"1,1,2,2", "1,1,3,3", "1,1,4,4", "1,1,5,5"};
        // 声明一个工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = wb.createSheet("综合指数月排名");
        // 生成一种样式
        HSSFCellStyle style = wb.createCellStyle();
        // 设置表头样式
        // 设置字体
        HSSFFont font = wb.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("微软雅黑");
        //设置底边框;
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        //****** 表头样式     *****/

        // 设置表内容样式
        HSSFCellStyle style2 = wb.createCellStyle();
        // 设置字体
        HSSFFont font2 = wb.createFont();
        //设置字体大小
        //font.setFontHeightInPoints((short)10);
        //字体加粗
        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //设置字体名字
        font2.setFontName("微软雅黑");
        //设置底边框;
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        //设置底边框颜色;
        style2.setBottomBorderColor(HSSFColor.BLACK.index);
        //设置左边框;
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        //设置左边框颜色;
        style2.setLeftBorderColor(HSSFColor.BLACK.index);
        //设置右边框;
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置右边框颜色;
        style2.setRightBorderColor(HSSFColor.BLACK.index);
        //设置顶边框;
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //设置顶边框颜色;
        style2.setTopBorderColor(HSSFColor.BLACK.index);
        //在样式用应用设置的字体;
        style2.setFont(font2);
        //设置自动换行;
        style2.setWrapText(true);
        //设置水平对齐的样式为居中对齐;
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //设置垂直对齐的样式为居中对齐;
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        //*****   表格内容样式       *****/
        // 生成表格的第一行
        // 第一行表头
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < excelHeader0.length; i++) {
            //sheet.autoSizeColumn(i, true);
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(excelHeader0[i]);
            int length = cell.getStringCellValue().getBytes().length;
            sheet.setColumnWidth(i, length * 256);
            cell.setCellStyle(style);
        }

        // 动态合并单元格
        for (int i = 0; i < headnum0.length; i++) {
            //sheet.autoSizeColumn(i, true);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 17 / 10);
            String[] temp = headnum0[i].split(",");
            Integer startrow = Integer.parseInt(temp[0]);
            Integer overrow = Integer.parseInt(temp[1]);
            Integer startcol = Integer.parseInt(temp[2]);
            Integer overcol = Integer.parseInt(temp[3]);
            sheet.addMergedRegion(new CellRangeAddress(startrow, overrow, startcol, overcol));
        }

        // 第二行表头
        row = sheet.createRow(1);
        for (int i = 0; i < excelHeader1.length; i++) {
            sheet.autoSizeColumn(i, true);// 自动调整宽度
            HSSFCell cell = row.createCell(i + 2);
            cell.setCellValue(excelHeader1[i]);
            cell.setCellStyle(style);
        }
        HSSFCell Cell6 = row.createCell(6);
        Cell6.setCellStyle(style);
        HSSFCell Cell7 = row.createCell(7);
        Cell7.setCellStyle(style);

        // 第三行数据
        for (int i = 0; i < jsonArray.size(); i++) {
            row = sheet.createRow(i + 2);
            Map<String, Object> obj = (Map<String, Object>) jsonArray.get(i);

            // 导入对应列的数据
            HSSFCell cell = row.createCell(0);
            cell.setCellValue((obj.get("monitorpointname") != null && !"null".equals(obj.get("monitorpointname").toString())) ? obj.get("monitorpointname").toString() : "");
            cell.setCellStyle(style2);

            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue((obj.get("composite") != null && !"null".equals(obj.get("composite").toString())) ? obj.get("composite").toString() : "");
            cell1.setCellStyle(style2);

            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue((obj.get("lastmoncomposite") != null && !"null".equals(obj.get("lastmoncomposite").toString())) ? obj.get("lastmoncomposite").toString() : "");
            cell2.setCellStyle(style2);

            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue((obj.get("lastmoncompositechange") != null && !"null".equals(obj.get("lastmoncompositechange").toString())) ? obj.get("lastmoncompositechange").toString() : "");
            cell3.setCellStyle(style2);

            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue((obj.get("yearonyearcomposite") != null && !"null".equals(obj.get("yearonyearcomposite").toString())) ? obj.get("yearonyearcomposite").toString() : "");
            cell4.setCellStyle(style2);

            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue((obj.get("yearonyearcompositechange") != null && !"null".equals(obj.get("yearonyearcompositechange").toString())) ? obj.get("yearonyearcompositechange").toString() : "");
            cell5.setCellStyle(style2);

            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue((obj.get("maxcomposite") != null && !"null".equals(obj.get("maxcomposite").toString())) ? obj.get("maxcomposite").toString() : "");
            cell6.setCellStyle(style2);

            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue((obj.get("primarypollutant") != null && !"null".equals(obj.get("primarypollutant").toString())) ? obj.get("primarypollutant").toString() : "");
            cell7.setCellStyle(style2);

        }
        return wb;

    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 上午 11:54
     * @Description: 根据监测点名称和监测点类型获取空气站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirStationInfosByMonitorPointNameAndType(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAirStationInfosByMonitorPointNameAndType(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午1:57
     * @Description: 根据监测点ID获取该监测点下所以监测因子
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirStationAllPollutantsByIDAndType(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAirStationAllPollutantsByIDAndType(paramMap);

    }

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午2:34
     * @Description: 获取空气小时日监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirStationHourOrDayDataByParams(List<String> pkidlist, String pollutantcode, String starttime, String endtime, String timetype) {
        try {
            //获取所有站点信息，并根据监测点ID找出对应的监测点编码
            List<Map<String, Object>> allairpoints = airMonitorStationMapper.getAllAirMonitorStation(new HashMap<>());
            List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            //设置参数
            paramMap.put("pkidlist", pkidlist);
            paramMap.put("monitorpointtype", 5);
            List<Map<String, Object>> pollutantlist = airMonitorStationMapper.getAirStationAllPollutantsByIDAndType(paramMap);
            //去MongoDB中查询数据
            Date startDate = null;
            Date endDate = null;
            String collectionname = "";
            if ("hour".equals(timetype)) {
                startDate = DataFormatUtil.getDateYMDH(starttime);
                endDate = DataFormatUtil.getDateYMDH(endtime);
                collectionname = "StationHourAQIData";
            } else if ("day".equals(timetype)) {
                startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
                endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
                collectionname = "StationDayAQIData";
            }
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> AQIData = mongoTemplate.aggregate(aggregation, collectionname, Document.class);
            List<Document> documents = AQIData.getMappedResults();
            if (allairpoints != null && allairpoints.size() > 0) {//获取站点编码
                for (Map<String, Object> obj : allairpoints) {
                    for (String id : pkidlist) {
                        if (id.equals(obj.get("PK_AirID").toString()) && obj.get("DGIMN") != null) {
                            Map<String, Object> paramMapresultmap = getAirStationMonitorDatas(obj.get("DGIMN").toString(), obj.get("MonitorPointName").toString(), documents, pollutantcode, pollutantlist, timetype);
                            resultlist.add(paramMapresultmap);
                            break;
                        }
                    }

                }
            }
            return resultlist;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;

        }
    }

    /**
     * @author: xsm
     * @date: 2019/6/12 0012 下午3:55
     * @Description: 根据监测点ID获取该监测点在线监测设备基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    @Override
    public Map<String, Object> getAirStationDeviceStatusByID(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAirStationDeviceStatusByID(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/6/27 0027 上午 8:31
     * @Description: 获取空气站点分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public PageInfo<Map<String, Object>> getOnlineAirStationInfoByParamMapForPage(Integer pageSize, Integer pageNum, Map<String, Object> paramMap) {
        if (pageSize != null && pageNum != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Map<String, Object>> listData = airMonitorStationMapper.getAirStationInfoByParamMap(paramMap);
        return new PageInfo<>(listData);
    }

    /**
     * @author: xsm
     * @date: 2019/6/16 0016 下午 5:27
     * @Description: 根据监测点ID获取附件表对应关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<String> getfileIdsByID(Map<String, Object> parammap) {
        List<Map<String, Object>> oldobj = airMonitorStationMapper.getfileIdsByID(parammap);
        List<String> list = new ArrayList<>();
        if (oldobj != null && oldobj.size() > 0) {
            for (Map<String, Object> obj : oldobj) {
                list.add(obj.get("FilePath").toString());
            }
        }
        return list;
    }

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 5:23
     * @Description: 获取所有空气点位信息及其状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorAirStationAndStatusInfo() {
        List<Map<String, Object>> resultlist = airMonitorStationMapper.getAllMonitorAirStationAndStatusInfo();
       /* for (Map<String, Object> obj : resultlist) {
            //构建Mongdb查询条件
            Query query = new Query();
            query.addCriteria(Criteria.where("StationCode").is(obj.get("DGIMN")));
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
            Document document = mongoTemplate.findOne(query, Document.class, hourCollection);
            if (document != null) {
                obj.put("aqi", document.getInteger("AQI"));
            } else {
                obj.put("aqi", "");
            }

        }*/
        return resultlist;
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 4:21
     * @Description: 自定义查询条件获取mongodb空气站点小时/日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getAirStationMongodbDataByParamMap(Map<String, Object> paramMap) {
        Query query = setAirStationDataQuery(paramMap);
        return mongoTemplate.find(query, Document.class, paramMap.get("collection").toString());
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 4:21
     * @Description: 组装空气站点小时/日数据查询条件
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Query setAirStationDataQuery(Map<String, Object> paramMap) {
        Query query = new Query();
        if (paramMap.get("mns") != null) {
            List<String> mns = (List<String>) paramMap.get("mns");
            query.addCriteria(Criteria.where("StationCode").in(mns));
        }
        if (paramMap.get("starttime") != null || paramMap.get("endtime") != null) {
            if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            if (paramMap.get("starttime") != null && paramMap.get("endtime") == null) {
                Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
                query.addCriteria(Criteria.where("MonitorTime").gte(startDate));
            }

            if (paramMap.get("endtime") != null && paramMap.get("starttime") == null) {
                Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
                query.addCriteria(Criteria.where("MonitorTime").lte(endDate));
            }
        }
        if (paramMap.get("pollutantcodes") != null) {
            List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
            query.addCriteria(Criteria.where("DataList.PollutantCode").in(pollutantcodes));
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            query.skip((pagenum - 1) * pagesize).limit(pagesize);
        }
        if (paramMap.get("sort") != null && paramMap.get("sort").equals("asc")) {
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        } else {
            query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        }
        return query;
    }

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 7:12
     * @Description: 根据其它类型的监测点的ID和类型编码获取关联的空气监测点的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public String getAirMnByOtherMonitorPointIdAndType(Map<String, Object> paramMap) {
        String mn = "";
        try {
            List<Map<String, Object>> listdata = otherMonitorPointMapper.getTraceSourceMonitorPointMN(paramMap);
            if (listdata != null && listdata.size() > 0 && (listdata.get(0).get("airmn") != null && !"".equals(listdata.get(0).get("airmn").toString()))) {
                return listdata.get(0).get("airmn").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mn;
    }

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 5:23
     * @Description: 根据点位MN号，筛选出该点位的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getAirStationMonitorDatas(String DGIMN, String monitorPointName, List<Document> documents, String pollutantcode, List<Map<String, Object>> pollutantlist, String timetype) {
        Map<String, Object> resultmap = new HashMap<>();
        resultmap.put("monitorpointname", monitorPointName);
        List<Map<String, Object>> onepollutantDatas = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> allpollutantDatas = new ArrayList<Map<String, Object>>();
        if (documents.size() > 0) {
            for (Document document : documents) {
                if (DGIMN.equals(document.get("StationCode"))) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    Map<String, Object> map2 = new LinkedHashMap<>();
                    if ("hour".equals(timetype)) {
                        map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                        map2.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                    } else if ("day".equals(timetype)) {
                        map.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                        map2.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                    }
                    map.put("monitorpointname", monitorPointName);
                    map.put("aqi", document.get("AQI"));
                    List<Map<String, Object>> pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> dataMap : pollutantDataList) {
                        if (pollutantlist != null && pollutantlist.size() > 0) {
                            for (Map<String, Object> obj : pollutantlist) {
                                if (dataMap.get("PollutantCode").equals(obj.get("code"))) {
                                    map.put(obj.get("name") != null ? obj.get("name").toString() : "", dataMap.get("Strength"));
                                    if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                                        map2.put("pollutantname", obj.get("name"));
                                        map2.put("value", dataMap.get("Strength"));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    onepollutantDatas.add(map2);
                    allpollutantDatas.add(map);
                }
            }
        }
        resultmap.put("onepollutantdatas", onepollutantDatas);
        resultmap.put("allpollutantdatas", allpollutantDatas);
        return resultmap;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/17 0017 下午 4:18
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [date]
     * @throws:
     */
    public static String format(String date, String parttern) {
        if (StringUtils.isNotBlank(date)) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US);
                Date parse = format.parse(date);
                SimpleDateFormat format1 = new SimpleDateFormat(parttern);
                String format2 = format1.format(parse);
                return format2;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 16:13
     * @Description: gis-获取所有空气监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllAirMonitorStationInfo() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderfield", "status");
        List<Map<String, Object>> airdata = airMonitorStationMapper.getAllAirMonitorStation(paramMap);
        int onlinenum = 0;
        int offlinenum = 0;
        if (airdata != null && airdata.size() > 0) {
            for (Map<String, Object> map : airdata) {
                int status = 0;
                if (map.get("OnlineStatus") != null && !"".equals(map.get("OnlineStatus").toString())) {//当状态不为空
                    if ("1".equals(map.get("OnlineStatus").toString())) {//1为在线
                        status = 1;
                    } else {
                        if (status < Integer.parseInt(map.get("OnlineStatus").toString())) {
                            status = Integer.parseInt(map.get("OnlineStatus").toString());
                        }
                    }
                }
                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                map.put("OnlineStatus", status);
            }
        }
        result.put("total", (airdata != null && airdata.size() > 0) ? airdata.size() : 0);
        result.put("onlinenum", onlinenum);
        result.put("offlinenum", offlinenum);
        result.put("listdata", airdata);
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 8:02
     * @Description: 通过自定义参数获取所有空气站
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllAirMonitorStationByParams(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAllAirMonitorStationByParams(paramMap);
    }

    @Override
    public List<Map<String, Object>> countOnlineOutPut(Map<String, Object> paramMap) {
        return airMonitorStationMapper.countOnlineOutPut(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/11/04 0004 下午 6:36
     * @Description: 通过主键ID获取空气站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public AirMonitorStationVO getAirMonitorStationByID(String pkid) {
        return airMonitorStationMapper.selectByPrimaryKey(pkid);
    }

    /**
     * @author: lip
     * @date: 2020/3/6 0006 上午 11:07
     * @Description: 自定义查询参数获取站点监测标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorStandardByParam(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getMonitorStandardByParam(paramMap);
    }

    /**
     * @author: lip
     * @date: 2020/4/2 0002 上午 11:47
     * @Description: 自定义查询条件获取监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getMonitorDataByParam(Map<String, Object> paramMap) {


        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();

        String unwindkey = paramMap.get("unwindkey").toString();
        UnwindOperation unwindOperation = unwind(unwindkey);
        aggregations.add(unwindOperation);

        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        List<String> pollutantCodes = (List<String>) paramMap.get("pollutantcodes");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey)
                .gte(startTime).lte(endTime)
                .and(unwindkey + ".PollutantCode").in(pollutantCodes);
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode", timeKey)
                .and("PollutantCode", unwindkey + ".PollutantCode")
                .and("AvgStrength", unwindkey + ".AvgStrength");
        aggregations.add(project(fields));
        Aggregation aggregation = Aggregation.newAggregation(aggregations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> listItems = pageResults.getMappedResults();

        return listItems;
    }

    /**
     * @author: xsm
     * @date: 2020/5/14 0014 下午 2:33
     * @Description: 设置空气点位aqi（最新空气站小时数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datalist]
     * @throws:
     */
    @Override
    public void setAirStationAqi(List<Map<String, Object>> datalist) {
        Set<String> dgimns = datalist.stream().filter(m -> m.get("DGIMN") != null || m.get("dgimn") != null).map(m -> m.get("DGIMN") == null ?
                m.get("dgimn") == null ? "" : m.get("dgimn").toString() : m.get("DGIMN").toString()).collect(Collectors.toSet());
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = Criteria.where("StationCode").in(dgimns);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("StationCode", "MonitorTime", "AQI", "AirQuality"));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
        operations.add(group("StationCode").first("MonitorTime").as("MonitorTime").first("AQI").as("aqi").first("AirQuality").as("quality"));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
        AggregationOptions aggregationOptions = AggregationOptions.builder().allowDiskUse(true).build();
        Aggregation aggregationquery = Aggregation.newAggregation(operations).withOptions(aggregationOptions);
        List<Document> mappedResults = mongoTemplate.aggregate(aggregationquery, hourCollection, Document.class).getMappedResults();
        for (Map<String, Object> map : datalist) {
            int aircode = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();
            if (map.get("FK_MonitorPointTypeCode") != null && aircode == Integer.parseInt(map.get("FK_MonitorPointTypeCode").toString())) {
                map.put("aqi", "");
                String dgimn = map.get("DGIMN") == null ? map.get("dgimn") == null ? "" : map.get("dgimn").toString() : map.get("DGIMN").toString();
                if (mappedResults.size() > 0) {
                    for (Document obj : mappedResults) {
                        if (dgimn.equals(obj.getString("_id"))) {
                            map.put("aqi", obj.get("aqi"));
                            map.put("quality", obj.get("quality"));
                            if (obj.get("quality") != null && !"".equals(obj.get("quality").toString())) {
                                if (map.get("monitorpointname") != null) {
                                    map.put("monitorpointname", map.get("monitorpointname") + "【" + obj.get("quality") + "】");
                                } else {
                                    if (map.get("MonitorPointName") != null) {
                                        map.put("MonitorPointName", map.get("MonitorPointName") + "【" + obj.get("quality") + "】");
                                    }
                                }
                                if (map.get("outputname") != null) {
                                    map.put("outputname", map.get("outputname") + "【" + obj.get("quality") + "】");
                                }
                            }
                        }
                    }
                }
            }

        }

    }


    /**
     * @author: chengzq
     * @date: 2020/5/19 0019 下午 3:21
     * @Description: 根据恶臭MN号获取对应空气监测点MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAirStationsByMN(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAirStationsByMN(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测时间段获取空气点位AQI报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public Map<String, Object> getAirStationReportDataByParams(Map<String, Object> paramMap) {
        try {
            //六参数污染物
            Map<String, Object> resultMap = new HashMap<>();
            List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> mns = (List<String>) paramMap.get("mns");
            Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
            Date startDate = null;
            Date endDate = null;
            String datetype = "day";
            String collection = "";
            String timestr = "";
            if (paramMap.get("datetype") != null) {
                datetype = paramMap.get("datetype").toString();
            }
            if ("hour".equals(datetype)) {
                startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString() + ":00:00");
                endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString() + ":59:59");
                collection = "StationHourAQIData";
                timestr = "%Y-%m-%d %H";
            } else if ("day".equals(datetype)) {
                startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString() + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString() + " 23:59:59");
                collection = "StationDayAQIData";
                timestr = "%Y-%m-%d";
            }
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            //model 表 不查 小时类型报警数据
            criteria.and("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("StationCode", "AQI", "PrimaryPollutant", "AirQuality", "AirLevel", "DataList")
                    .and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "StationCode"));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                List<AggregationOperation> operations1 = new ArrayList<>();
                Criteria criteria1 = new Criteria();
                //model 表 不查 小时类型报警数据
                criteria1.and("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
                operations1.add(Aggregation.match(criteria1));
                operations1.add(Aggregation.project("StationCode", "DataList")
                        .and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                operations1.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "StationCode"));
                Aggregation aggregationList1 = Aggregation.newAggregation(operations1)
                        .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
                AggregationResults<Document> countmap = mongoTemplate.aggregate(aggregationList1, collection, Document.class);
                List<Document> countlist = countmap.getMappedResults();
                resultMap.put("total", countlist.size());
            }
            if (listItems.size() > 0) {
                for (Document document : listItems) {
                    //按日期分组
                    Map<String, Object> onepomao = new HashMap<>();
                    onepomao.put("dgimn", document.getString("StationCode"));
                    onepomao.put("monitorpointname", mnandname.get(document.getString("StationCode")));
                    onepomao.put("monitortime", document.getString("MonitorTime"));
                    onepomao.put("aqi", document.getInteger("AQI"));
                    onepomao.put("primarypollutant", document.get("PrimaryPollutant") != null ? CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(document.getString("PrimaryPollutant")) : "-");
                    onepomao.put("airquality", document.getString("AirQuality"));
                    onepomao.put("airlevel", document.getString("AirLevel"));
                    List<Document> polist = (List<Document>) document.get("DataList");
                    for (String postr : pollutantcodes) {
                        onepomao.put(postr + "_nd", "-");
                        onepomao.put(postr + "_iaqi", "-");
                        for (Document onepo : polist) {
                            if (postr.equals(onepo.getString("PollutantCode"))) {
                                onepomao.put(postr + "_nd", onepo.get("Strength"));
                                onepomao.put(postr + "_iaqi", onepo.get("IAQI"));
                            }
                        }
                    }
                    result.add(onepomao);
                }
            }
            resultMap.put("datalist", result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getAirStationReportTitleDataByType(String[] titlenames, String[] titlefiled) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        String rownum = "2";
        stationname.put("headername", "站点名称");
        stationname.put("headercode", "monitorpointname");
        stationname.put("rownum", rownum);
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);

        Map<String, Object> counttimenum = new HashMap<>();
        counttimenum.put("headername", "监测时间");
        counttimenum.put("headercode", "monitortime");
        counttimenum.put("rownum", rownum);
        counttimenum.put("columnnum", "1");
        counttimenum.put("chlidheader", new ArrayList<>());
        dataList.add(counttimenum);

        for (int i = 0; i < titlenames.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("headername", titlenames[i]);
            map.put("headercode", titlefiled[i]);
            map.put("rownum", "1");
            map.put("columnnum", "2");
            List<Map<String, Object>> chlidheader = new ArrayList<>();

            Map<String, Object> windtimenum = new HashMap<>();

            if ("a21005".equals(titlefiled[i])) {
                windtimenum.put("headername", "浓度(mg/m³)");
            } else {
                windtimenum.put("headername", "浓度(μg/m³)");
            }

            windtimenum.put("headercode", titlefiled[i] + "_nd");
            windtimenum.put("rownum", "1");
            windtimenum.put("columnnum", "1");
            windtimenum.put("chlidheader", new ArrayList<>());
            chlidheader.add(windtimenum);

            Map<String, Object> windpercent = new HashMap<>();
            windpercent.put("headername", "分指数");
            windpercent.put("headercode", titlefiled[i] + "_iaqi");
            windpercent.put("rownum", "1");
            windpercent.put("columnnum", "1");
            windpercent.put("chlidheader", new ArrayList<>());
            chlidheader.add(windpercent);

            dataList.add(map);
            map.put("chlidheader", chlidheader);
        }

        Map<String, Object> map1 = new HashMap<>();
        map1.put("headername", "空气质量指数(AQI)");
        map1.put("headercode", "aqi");
        map1.put("rownum", rownum);
        map1.put("columnnum", "1");
        map1.put("chlidheader", new ArrayList<>());
        dataList.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("headername", "首要污染物");
        map2.put("headercode", "primarypollutant");
        map2.put("rownum", rownum);
        map2.put("columnnum", "1");
        map2.put("chlidheader", new ArrayList<>());
        dataList.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("headername", "空气质量指数级别");
        map3.put("headercode", "airquality");
        map3.put("rownum", rownum);
        map3.put("columnnum", "1");
        map3.put("chlidheader", new ArrayList<>());
        dataList.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("headername", "空气质量类别");
        map4.put("headercode", "airlevel");
        map4.put("rownum", rownum);
        map4.put("columnnum", "1");
        map4.put("chlidheader", new ArrayList<>());
        dataList.add(map4);
        return dataList;
    }

    @Override
    public List<Map<String, Object>> getAirStationOverallMeritDataByParams(Map<String, Object> paramMap) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            //判断当前时间是否为当前月  或当前年
            String monitortime = paramMap.get("monitortime").toString();
            Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
            List<String> mns = (List<String>) paramMap.get("mns");
            Date newdate = new Date();
            String endym = DataFormatUtil.getDateYM(newdate);
            String endy = DataFormatUtil.getDateY(newdate);
            String starttime = "";
            String endtime = "";
            if ("month".equals(paramMap.get("datetype").toString())) {
                if (monitortime.equals(endym)) {//当查询时间和当前月份相同时
                    return null;
                } else {
                    starttime = monitortime;
                    endtime = monitortime;
                }
            } else if ("year".equals(paramMap.get("datetype").toString())) {
                if (endy.equals(monitortime)) {//当查询时间和当前年相同时
                    if ((endy + "-01").equals(endym)) {//判断当前年第一个月 和当前时间所处的月份是否相同
                        return null;
                    } else {
                        //不同  则结束时间取当前时间月份的前一个月
                        Calendar c = Calendar.getInstance();
                        //过去一月
                        c.setTime(newdate);
                        c.add(Calendar.MONTH, -1);
                        Date lastdate = c.getTime();
                        starttime = endy + "-01";
                        endtime = DataFormatUtil.getDateYM(lastdate);
                    }
                } else {
                    starttime = monitortime + "-01";
                    endtime = monitortime + "-12";
                }

            }
            //获取站点小时  和站点日数据
            if ("month".equals(paramMap.get("datetype").toString())) {
                result = countAirMonthStationOverallMeritData(starttime, endtime, mns, mnandname);
            } else if ("year".equals(paramMap.get("datetype").toString())) {
                result = countAirYearStationOverallMeritData(starttime, endtime, mns, mnandname);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 计算站点月的有效天数、超标率、综合指数
     */
    private List<Map<String, Object>> countAirMonthStationOverallMeritData(String starttime, String endtime, List<String> mns, Map<String, Object> mnandname) {
        starttime = starttime + "-01";
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        //获取一段时间的小时数据
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        Set<String> stationCodes = new LinkedHashSet<>();
        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> hourDocuments = hourAQIData.getMappedResults();
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
                        stationCodes.add(document.getString("StationCode"));
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
                match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> dayDocuments = cityDayAQIData.getMappedResults();
        if (dayDocuments.size() > 0) {
            Map<String, Object> monthSO2 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
            Map<String, Object> monthNO2 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
            Map<String, Object> monthCO = getOtherStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
            Map<String, Object> monthO3 = getOtherStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
            Map<String, Object> monthPM10 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
            Map<String, Object> monthPM25 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
            for (String mn : mns) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("monitorpointname", mnandname.get(mn));
                setCodeAndValueData(onemap, monthSO2, mn, "a21026");
                setCodeAndValueData(onemap, monthNO2, mn, "a21004");
                setCodeAndValueData(onemap, monthCO, mn, "a21005");
                setCodeAndValueData(onemap, monthO3, mn, "a05024");
                setCodeAndValueData(onemap, monthPM10, mn, "a34002");
                setCodeAndValueData(onemap, monthPM25, mn, "a34004");
                result.add(onemap);
            }
        }
        return result;
    }


    /**
     * 计算站点年的有效天数、超标率、综合指数
     */
    private List<Map<String, Object>> countAirYearStationOverallMeritData(String starttime, String endtime, List<String> mns, Map<String, Object> mnandname) throws Exception {
        //获取这段时间内的所有月份
        List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);
        //yms.remove(endtime);
        starttime = starttime + "-01";
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        //获取一段时间的小时数据
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        Set<String> stationCodes = new LinkedHashSet<>();
        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> allhours = hourAQIData.getMappedResults();
        //根据日数据获取有效日数据
        startDate = DataFormatUtil.getDateYMD(starttime + " 00");
        endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> alldays = cityDayAQIData.getMappedResults();
        //小时数据按月份分组
        Map<String, List<Document>> HourMap = allhours.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
        //日数据按月份分组
        Map<String, List<Document>> DayMap = alldays.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
        Map<String, Map<String, Object>> resultmap = new HashMap<>();
        Map<String, Object> monthSO2;
        Map<String, Object> monthNO2;
        Map<String, Object> monthCO;
        Map<String, Object> monthO3;
        Map<String, Object> monthPM10;
        Map<String, Object> monthPM25;
        Map<String, Object> onemap;
        for (String month : yms) {
            if (HourMap.get(month) != null && DayMap.get(month) != null) {
                List<Document> hourDocuments = HourMap.get(month);
                //根据小时数据获取有效日期
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
                            stationCodes.add(document.getString("StationCode"));
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
                List<Document> dayDocuments = DayMap.get(month);
                monthSO2 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
                monthNO2 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
                monthCO = getOtherStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
                monthO3 = getOtherStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
                monthPM10 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
                monthPM25 = conutStationOverallByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
                for (String mn : mns) {
                    if (resultmap.get(mn) != null) {
                        onemap = resultmap.get(mn);
                    } else {
                        onemap = new HashMap<>();
                    }
                    setCodeAndValueDataForYear(onemap, monthSO2, mn, "a21026");
                    setCodeAndValueDataForYear(onemap, monthNO2, mn, "a21004");
                    setCodeAndValueDataForYear(onemap, monthCO, mn, "a21005");
                    setCodeAndValueDataForYear(onemap, monthO3, mn, "a05024");
                    setCodeAndValueDataForYear(onemap, monthPM10, mn, "a34002");
                    setCodeAndValueDataForYear(onemap, monthPM25, mn, "a34004");
                    resultmap.put(mn, onemap);
                }
            }
        }
        for (String key : resultmap.keySet()) {
            if (resultmap.get(key) != null) {
                Map<String, Object> map = (Map<String, Object>) resultmap.get(key);
                map.put("monitorpointname", mnandname.get(key));
                setCodeAndValueDataForYearTwo(map, "a21026");
                setCodeAndValueDataForYearTwo(map, "a21004");
                setCodeAndValueDataForYearTwo(map, "a21005");
                setCodeAndValueDataForYearTwo(map, "a05024");
                setCodeAndValueDataForYearTwo(map, "a34002");
                setCodeAndValueDataForYearTwo(map, "a34004");
                result.add(map);
            }
        }
        return result;
    }

    private void setCodeAndValueDataForYearTwo(Map<String, Object> map, String code) {
        int num = map.get(code + "_num") != null ? Integer.valueOf(map.get(code + "_num").toString()) : 0;
        if (num > 0) {
            Double nd = map.get(code + "_nd") != null ? Double.valueOf(map.get(code + "_nd").toString()) : 0d;
            if (nd > 0) {
                map.put(code + "_nd", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(nd / num)));
            } else {
                map.put(code + "_nd", "-");
            }
            Double dxzs = map.get(code + "_dxzs") != null ? Double.valueOf(map.get(code + "_dxzs").toString()) : 0d;
            if (nd > 0) {
                map.put(code + "_dxzs", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(dxzs / num)));
            } else {
                map.put(code + "_dxzs", "-");
            }
        }
        Double yxts = map.get(code + "_yxts") != null ? Double.valueOf(map.get(code + "_yxts").toString()) : 0d;
        Double cbts = map.get(code + "_cbts") != null ? Double.valueOf(map.get(code + "_cbts").toString()) : 0d;
        if (yxts > 0 && cbts > 0) {
            map.put(code + "_cbl", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(cbts / yxts)));
        } else {
            map.put(code + "_cbl", "-");
        }
        map.remove(code + "_num");
        map.remove(code + "_cbts");
    }

    private void setCodeAndValueDataForYear(Map<String, Object> onemap, Map<String, Object> twomap, String mn, String code) {
        if (twomap.get(mn) != null) {
            Map<String, Object> map = (Map<String, Object>) twomap.get(mn);
            if (onemap.get(code + "_yxts") != null) {
                onemap.put(code + "_yxts", map.get("yxts") != null ? Integer.valueOf(onemap.get(code + "_yxts").toString()) + Integer.valueOf(map.get("yxts").toString()) : onemap.get(code + "_yxts"));
            } else {
                onemap.put(code + "_yxts", map.get("yxts") != null ? map.get("yxts").toString() : null);
            }
            //浓度
            if (onemap.get(code + "_nd") != null) {
                onemap.put(code + "_nd", map.get("pjnd") != null ? Double.valueOf(onemap.get(code + "_nd").toString()) + Double.valueOf(map.get("pjnd").toString()) : onemap.get(code + "_nd"));
            } else {
                onemap.put(code + "_nd", map.get("pjnd") != null ? map.get("pjnd").toString() : null);
            }
            //超标天数
            if (onemap.get(code + "_cbts") != null) {
                onemap.put(code + "_cbts", map.get("cbts") != null ? Integer.valueOf(onemap.get(code + "_cbts").toString()) + Integer.valueOf(map.get("cbts").toString()) : onemap.get(code + "_cbts"));
            } else {
                onemap.put(code + "_cbts", map.get("cbts") != null ? map.get("cbts").toString() : null);
            }
            //单项指数
            if (onemap.get(code + "_dxzs") != null) {
                onemap.put(code + "_dxzs", map.get("dxzs") != null ? Double.valueOf(onemap.get(code + "_dxzs").toString()) + Double.valueOf(map.get("dxzs").toString()) : onemap.get(code + "_dxzs"));
            } else {
                onemap.put(code + "_dxzs", map.get("dxzs") != null ? map.get("dxzs").toString() : null);
            }
            //因子月份数
            if (onemap.get(code + "_num") != null && !"-".equals(onemap.get(code + "_num").toString())) {
                onemap.put(code + "_num", Integer.valueOf(onemap.get(code + "_num").toString()) + 1);
            } else {
                onemap.put(code + "_num", 1);
            }
           /* if (yxts>0&&cbts>0){
                onemap.put(code+"_cbl",DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(cbts/yxts)));
            }else{
                onemap.put(code+"_cbl","-");
            }*/
        } else {
            onemap.put(code + "_yxts", null);
            onemap.put(code + "_nd", null);
            onemap.put(code + "_cbts", null);
            onemap.put(code + "_dxzs", null);
            onemap.put(code + "_nmu", null);
        }
    }


    private void setCodeAndValueData(Map<String, Object> onemap, Map<String, Object> twomap, String mn, String code) {
        if (twomap.get(mn) != null) {
            Map<String, Object> map = (Map<String, Object>) twomap.get(mn);
            onemap.put(code + "_yxts", map.get("yxts") != null ? map.get("yxts").toString() : "-");
            onemap.put(code + "_nd", map.get("pjnd") != null ? map.get("pjnd").toString() : "-");
            Double yxts = map.get("yxts") != null ? Double.valueOf(map.get("yxts").toString()) : 0d;
            Double cbts = map.get("cbts") != null ? Double.valueOf(map.get("cbts").toString()) : 0d;
            if (yxts > 0 && cbts > 0) {
                onemap.put(code + "_cbl", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(cbts / yxts)));
            } else {
                onemap.put(code + "_cbl", "-");
            }
            onemap.put(code + "_dxzs", map.get("dxzs") != null ? map.get("dxzs").toString() : "-");
        } else {
            onemap.put(code + "_yxts", "-");
            onemap.put(code + "_nd", "-");
            onemap.put(code + "_cbl", "-");
            onemap.put(code + "_dxzs", "-");
        }
    }

    /**
     * @author: xsm
     * @date: 2021/6/6 0006 上午 10:23
     * @Description: 计算多站点某月单项指数、有效天数、超标天数、月平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> conutStationOverallByCode(List<Document> dayDocuments, Map<String, List<String>> codeAndEffectiveDay, String code, int standardValue) {

        Map<String, Object> stationAndValue = new HashMap<>();
        List<String> effectiveHourList = codeAndEffectiveDay.get(code);
        if (effectiveHourList != null) {

            Double sumItem = 0d;
            String tempYMD;
            String tempStationCode;
            Double strength = 0d;
            String level = "";
            List<String> yl_level = Arrays.asList("一级", "二级");
            List<Map<String, Object>> pollutantDataList;
            Map<String, Integer> stationAndNum = new HashMap<>();
            Map<String, Double> stationAndTotal = new HashMap<>();
            Map<String, Integer> airoverlevel = new HashMap<>();
            for (Document document : dayDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (effectiveHourList.contains(tempYMD)) {
                    tempStationCode = document.getString("StationCode");
                    pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> map : pollutantDataList) {
                        if (code.equals(map.get("PollutantCode"))) {
                            strength = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                            level = map.get("AirLevel") != null ? map.get("AirLevel").toString() : "";
                            break;
                        }
                    }
                    if (strength != null) {
                        if (stationAndNum.get(tempStationCode) != null) {
                            stationAndNum.put(tempStationCode, stationAndNum.get(tempStationCode) + 1);//有效天数
                            stationAndTotal.put(tempStationCode, stationAndTotal.get(tempStationCode) + strength);
                            if (!"".equals(level) && !yl_level.contains(level)) {
                                airoverlevel.put(tempStationCode, airoverlevel.get(tempStationCode) + 1);//超标次数
                            }
                        } else {
                            stationAndNum.put(tempStationCode, 1);
                            stationAndTotal.put(tempStationCode, strength);
                            if (!"".equals(level) && !yl_level.contains(level)) {
                                airoverlevel.put(tempStationCode, 1);//超标次数
                            } else {
                                airoverlevel.put(tempStationCode, 0);//超标次数
                            }
                        }
                    }
                }
            }
            int value;
            for (String stationKey : stationAndTotal.keySet()) {
                value = (int) Math.round(stationAndTotal.get(stationKey) / stationAndNum.get(stationKey));
                sumItem = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", (double) value / standardValue));
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("yxts", stationAndNum.get(stationKey));//有效天数
                onemap.put("cbts", airoverlevel.get(stationKey));//有效天数
                onemap.put("pjnd", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(stationAndTotal.get(stationKey).toString()) / Double.valueOf(stationAndNum.get(stationKey).toString()))));//平均浓度
                onemap.put("dxzs", sumItem);//单项指数
                stationAndValue.put(stationKey, onemap);
            }
        }
        return stationAndValue;
    }

    /**
     * @author: xsm
     * @date: 2022/01/20 0020 下午 3:13
     * @Description: 计算百分位加和项
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getOtherStationOverallByCode(List<Document> dayDocuments, Map<String, List<String>> codeAndEffectiveDay, String code, int percentile, int standardValue) {
        Map<String, Object> stationAndValue = new HashMap<>();
        List<String> effectiveHourList = codeAndEffectiveDay.get(code);
        if (effectiveHourList != null) {
            String tempYMD;
            String tempStationCode;
            Double strength = 0d;
            String level = "";
            List<String> yl_level = Arrays.asList("一级", "二级");
            List<Map<String, Object>> pollutantDataList;
            Map<String, Integer> stationAndNum = new HashMap<>();
            Map<String, Double> stationAndTotal = new HashMap<>();
            Map<String, Integer> airoverlevel = new HashMap<>();
            Map<String, List<Double>> stationAndStrength = new HashMap<>();
            for (Document document : dayDocuments) {
                tempYMD = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                if (effectiveHourList.contains(tempYMD)) {
                    tempStationCode = document.getString("StationCode");
                    pollutantDataList = document.get("DataList", List.class);
                    for (Map<String, Object> map : pollutantDataList) {
                        if (code.equals(map.get("PollutantCode"))) {
                            strength = map.get("Strength") != null ? Double.parseDouble(map.get("Strength").toString()) : null;
                            break;
                        }
                    }
                    if (strength != null) {
                        if (stationAndNum.get(tempStationCode) != null) {
                            stationAndNum.put(tempStationCode, stationAndNum.get(tempStationCode) + 1);
                            List<Double> strengths = stationAndStrength.get(tempStationCode);
                            stationAndTotal.put(tempStationCode, stationAndTotal.get(tempStationCode) + strength);
                            if (!"".equals(level) && !yl_level.contains(level)) {
                                airoverlevel.put(tempStationCode, airoverlevel.get(tempStationCode) + 1);//超标次数
                            }
                            strengths.add(strength);
                            stationAndStrength.put(tempStationCode, stationAndStrength.get(tempStationCode));
                        } else {
                            stationAndNum.put(tempStationCode, 1);
                            stationAndTotal.put(tempStationCode, strength);
                            if (!"".equals(level) && !yl_level.contains(level)) {
                                airoverlevel.put(tempStationCode, 1);//超标次数
                            } else {
                                airoverlevel.put(tempStationCode, 0);//超标次数
                            }
                            List<Double> strengths = new ArrayList<>();
                            strengths.add(strength);
                            stationAndStrength.put(tempStationCode, strengths);
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
            for (String stationKey : stationAndStrength.keySet()) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("yxts", stationAndNum.get(stationKey));//有效天数
                onemap.put("cbts", airoverlevel.get(stationKey));//有效天数
                onemap.put("pjnd", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(stationAndTotal.get(stationKey).toString()) / Double.valueOf(stationAndNum.get(stationKey).toString()))));//平均浓度
                n = stationAndNum.get(stationKey);
                k = 1 + (n - 1) * p / 100d;
                List<Double> strengths = stationAndStrength.get(stationKey);
                if (strengths.size() > 1) {
                    Collections.sort(strengths);
                    s = (int) Math.floor(k) - 1;
                    mp = strengths.get(s) + (strengths.get(s + 1) - strengths.get(s)) * (k - s);
                    mp = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp));
                    value = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp / standardValue));
                    onemap.put("dxzs", value);//单项指数
                    stationAndValue.put(stationKey, onemap);
                }
            }
        }
        return stationAndValue;
    }


    @Override
    public List<Map<String, Object>> getAirStationOverallMeritDataTitle() {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> stationname = new HashMap<>();
        String rownum = "2";
        stationname.put("headername", "站点名称");
        stationname.put("headercode", "monitorpointname");
        stationname.put("rownum", rownum);
        stationname.put("columnnum", "1");
        stationname.put("chlidheader", new ArrayList<>());
        dataList.add(stationname);
        String[] titlenames = new String[]{"颗粒物(颗粒小于等于2.5μm)", "颗粒物(颗粒小于等于10μm)", "二氧化硫(SO2)", "二氧化氮(NO2)", "一氧化碳(CO)", "臭氧(O3)",};
        String[] titlefiled = new String[]{"a34004", "a34002", "a21026", "a21004", "a21005", "a05024",};
        for (int i = 0; i < titlenames.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("headername", titlenames[i]);
            map.put("headercode", titlefiled[i]);
            map.put("rownum", "1");
            map.put("columnnum", "4");
            List<Map<String, Object>> chlidheader = new ArrayList<>();

            Map<String, Object> ytxs_map = new HashMap<>();
            ytxs_map.put("headername", "有效天数");
            ytxs_map.put("headercode", titlefiled[i] + "_yxts");
            ytxs_map.put("rownum", "1");
            ytxs_map.put("columnnum", "1");
            ytxs_map.put("chlidheader", new ArrayList<>());
            chlidheader.add(ytxs_map);

            Map<String, Object> windtimenum = new HashMap<>();
            if (i == 4) {
                windtimenum.put("headername", "浓度(mg/m³)");
            } else {
                windtimenum.put("headername", "浓度(μg/m³)");
            }
            windtimenum.put("headercode", titlefiled[i] + "_nd");
            windtimenum.put("rownum", "1");
            windtimenum.put("columnnum", "1");
            windtimenum.put("chlidheader", new ArrayList<>());
            chlidheader.add(windtimenum);

            Map<String, Object> windpercent = new HashMap<>();
            windpercent.put("headername", "超标率");
            windpercent.put("headercode", titlefiled[i] + "_cbl");
            windpercent.put("rownum", "1");
            windpercent.put("columnnum", "1");
            windpercent.put("chlidheader", new ArrayList<>());
            chlidheader.add(windpercent);

            Map<String, Object> dxzs_map = new HashMap<>();
            dxzs_map.put("headername", "单项指数");
            dxzs_map.put("headercode", titlefiled[i] + "_dxzs");
            dxzs_map.put("rownum", "1");
            dxzs_map.put("columnnum", "1");
            dxzs_map.put("chlidheader", new ArrayList<>());
            chlidheader.add(dxzs_map);

            dataList.add(map);
            map.put("chlidheader", chlidheader);
        }
        return dataList;
    }

    /**
     * @Description: 获取站点小时数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/18 10:07
     */
    @Override
    public List<Document> getStationHourDataListByParam(Map<String, Object> paramMap) {
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");

        List<String> mns = (List<String>) paramMap.get("mns");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("StationCode").in(mns)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        String collection = (String) paramMap.get("collection");
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> hourDocuments = hourAQIData.getMappedResults();
        return hourDocuments;
    }


    @Override
    public List<Map<String, Object>> getAllAirStationDataByParamMap(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        List<String> mns = (List<String>) paramMap.get("mns");
        Date startDate = null;
        Date endDate = null;
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        if ("hour".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            collection = "StationHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
        } else if ("day".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "StationDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
        } else if ("month".equals(datatype)) {
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            startDate = DataFormatUtil.getDateYMDHMS(starttime + "-01 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "StationMonthAQIData";
            timestr = "%Y-%m";
            times = DataFormatUtil.getMonthBetween(starttime, endtime);
            //times.remove(endtime);
        }

        Criteria criteria = new Criteria();
        criteria.and("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if ("aqi".equals(pollutantcode)) {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AQI");
            operations.add(Aggregation.project("StationCode", "AQI").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            operations.add(Aggregation.group("StationCode")
                    .push(valuemap).as("valuelist")
            );
        } else {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind("DataList"));
            operations.add(match(Criteria.where("DataList.PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("StationCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and("DataList.PollutantCode").as("PollutantCode")
                    .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.group("StationCode")
                    .push(valuemap).as("valuelist")
            );
        }

        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        if (mappedResults.size() > 0) {
            for (String mn : mns) {
                List<Map<String, Object>> mn_valuelist = new ArrayList<>();
                for (Document document : mappedResults) {
                    if (mn.equals(document.getString("_id"))) {
                        List<Document> valuelist = (List<Document>) document.get("valuelist");
                        for (String thetime : times) {
                            for (Document valuedoc : valuelist) {
                                if (thetime.equals(valuedoc.getString("time"))) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("monitortime", thetime);
                                    map.put("value", valuedoc.get("value"));
                                    mn_valuelist.add(map);
                                    break;
                                }
                            }
                        }
                    }
                }
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("dgimn", mn);
                resultmap.put("name", mnandname.get(mn));
                resultmap.put("valuedata", mn_valuelist);
                result.add(resultmap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 14:06
     * @Description: 获取每月多站点的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: month:月份日期（yyyy-mm）
     * @return: key：stationCode，value:综合指数值的map
     */
    @Override
    public List<Map<String, Object>> getManyMonthManyStationMonthCompositeIndex(Map<String, Object> paramMap) throws Exception {
        //获取这段时间内的所有月份
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        List<String> mns = (List<String>) paramMap.get("mns");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);
        //yms.remove(endtime);
        starttime = starttime + "-01";
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        //获取一段时间的小时数据
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        Set<String> stationCodes = new LinkedHashSet<>();
        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> allhours = hourAQIData.getMappedResults();
        //根据日数据获取有效日数据
        startDate = DataFormatUtil.getDateYMD(starttime + " 00");
        endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> alldays = cityDayAQIData.getMappedResults();
        //小时数据按站点分组
        Map<String, List<Document>> mn_HourMap = allhours.stream().filter(m -> m != null && m.get("StationCode") != null).collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
        //日数据按站点分组
        Map<String, List<Document>> mn_DayMap = alldays.stream().filter(m -> m != null && m.get("StationCode") != null).collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
        List<Document> hourdoc;
        List<Document> daydoc;
        Map<String, Double> monthSO2;
        Map<String, Double> monthNO2;
        Map<String, Double> monthCO;
        Map<String, Double> monthO3;
        Map<String, Double> monthPM10;
        Map<String, Double> monthPM25;
        for (String mn : mns) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("dgimn", mn);
            resultMap.put("name", mnandname.get(mn));
            List<Map<String, Object>> valuelist = new ArrayList<>();
            if (mn_HourMap.get(mn) != null && mn_DayMap.get(mn) != null) {
                hourdoc = mn_HourMap.get(mn);
                daydoc = mn_DayMap.get(mn);
                //小时数据按月份分组
                Map<String, List<Document>> HourMap = hourdoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                //日数据按月份分组
                Map<String, List<Document>> DayMap = daydoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                for (String month : yms) {
                    if (HourMap.get(month) != null && DayMap.get(month) != null) {
                        List<Document> hourDocuments = HourMap.get(month);
                        //根据小时数据获取有效日期
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
                                    stationCodes.add(document.getString("StationCode"));
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
                        List<Document> dayDocuments = DayMap.get(month);
                        if (dayDocuments.size() > 0) {
                            monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
                            monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
                            monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
                            monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
                            monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
                            monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
                            Double compositeIndex;
                            Double SO2;
                            Double NO2;
                            Double CO;
                            Double O3;
                            Double PM10;
                            Double PM25;
                            SO2 = monthSO2.get(mn) != null ? monthSO2.get(mn) : 0d;
                            NO2 = monthNO2.get(mn) != null ? monthNO2.get(mn) : 0d;
                            CO = monthCO.get(mn) != null ? monthCO.get(mn) : 0d;
                            O3 = monthO3.get(mn) != null ? monthO3.get(mn) : 0d;
                            PM10 = monthPM10.get(mn) != null ? monthPM10.get(mn) : 0d;
                            PM25 = monthPM25.get(mn) != null ? monthPM25.get(mn) : 0d;
                            compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                            Map<String, Object> valuemap = new HashMap<>();
                            valuemap.put("monitortime", month);
                            valuemap.put("value", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                            valuelist.add(valuemap);
                        }
                    }
                }
            }
            resultMap.put("valuedata", valuelist);
            result.add(resultMap);
        }
        return result;
    }

    public List<Map<String, Object>> getManyMonthManyCityMonthCompositeIndex(Map<String, Object> paramMap) throws Exception {
        //获取这段时间内的所有月份
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        List<String> mns = (List<String>) paramMap.get("mns");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);
        //yms.remove(endtime);
        starttime = starttime + "-01";
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        //获取一段时间的小时数据
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        Set<String> stationCodes = new LinkedHashSet<>();
        Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
        Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "RegionCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                match(Criteria.where("RegionCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, DB_CityHourAQIData, Document.class);
        List<Document> allhours = hourAQIData.getMappedResults();
        //根据日数据获取有效日数据
        startDate = DataFormatUtil.getDateYMD(starttime + " 00");
        endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
        aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                match(Criteria.where("RegionCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, DB_CityDayAQIData, Document.class);
        List<Document> alldays = cityDayAQIData.getMappedResults();
        //小时数据按站点分组
        Map<String, List<Document>> mn_HourMap = allhours.stream().filter(m -> m != null && m.get("RegionCode") != null).collect(Collectors.groupingBy(m -> m.get("RegionCode").toString()));
        //日数据按站点分组
        Map<String, List<Document>> mn_DayMap = alldays.stream().filter(m -> m != null && m.get("RegionCode") != null).collect(Collectors.groupingBy(m -> m.get("RegionCode").toString()));
        List<Document> hourdoc;
        List<Document> daydoc;
        Map<String, Double> monthSO2;
        Map<String, Double> monthNO2;
        Map<String, Double> monthCO;
        Map<String, Double> monthO3;
        Map<String, Double> monthPM10;
        Map<String, Double> monthPM25;
        for (String mn : mns) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("dgimn", mn);
            resultMap.put("name", mnandname.get(mn));
            List<Map<String, Object>> valuelist = new ArrayList<>();
            if (mn_HourMap.get(mn) != null && mn_DayMap.get(mn) != null) {
                hourdoc = mn_HourMap.get(mn);
                daydoc = mn_DayMap.get(mn);
                //小时数据按月份分组
                Map<String, List<Document>> HourMap = hourdoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                //日数据按月份分组
                Map<String, List<Document>> DayMap = daydoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                for (String month : yms) {
                    if (HourMap.get(month) != null && DayMap.get(month) != null) {
                        List<Document> hourDocuments = HourMap.get(month);
                        //根据小时数据获取有效日期
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
                                    stationCodes.add(document.getString("StationCode"));
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
                        List<Document> dayDocuments = DayMap.get(month);
                        if (dayDocuments.size() > 0) {
                            monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
                            monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
                            monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
                            monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
                            monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
                            monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
                            Double compositeIndex;
                            Double SO2;
                            Double NO2;
                            Double CO;
                            Double O3;
                            Double PM10;
                            Double PM25;
                            SO2 = monthSO2.get(mn) != null ? monthSO2.get(mn) : 0d;
                            NO2 = monthNO2.get(mn) != null ? monthNO2.get(mn) : 0d;
                            CO = monthCO.get(mn) != null ? monthCO.get(mn) : 0d;
                            O3 = monthO3.get(mn) != null ? monthO3.get(mn) : 0d;
                            PM10 = monthPM10.get(mn) != null ? monthPM10.get(mn) : 0d;
                            PM25 = monthPM25.get(mn) != null ? monthPM25.get(mn) : 0d;
                            compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                            Map<String, Object> valuemap = new HashMap<>();
                            valuemap.put("monitortime", month);
                            valuemap.put("value", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                            valuelist.add(valuemap);
                        }
                    }
                }
            }
            resultMap.put("valuedata", valuelist);
            result.add(resultMap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 14:06
     * @Description: 获取城市空气质量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllAirCityDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        Date startDate = null;
        Date endDate = null;
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        if ("hour".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            collection = "CityHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
        } else if ("day".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "CityDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
        }
        Criteria criteria = new Criteria();
        criteria.and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if ("aqi".equals(pollutantcode)) {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AQI");
            operations.add(Aggregation.project("RegionCode", "AQI").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            operations.add(Aggregation.group("RegionCode")
                    .push(valuemap).as("valuelist")
            );
        } else {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind("DataList"));
            operations.add(match(Criteria.where("DataList.PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("RegionCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and("DataList.PollutantCode").as("PollutantCode")
                    .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.group("RegionCode")
                    .push(valuemap).as("valuelist")
            );
        }

        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        if (mappedResults.size() > 0) {
            for (Document document : mappedResults) {
                List<Map<String, Object>> mn_valuelist = new ArrayList<>();
                List<Document> valuelist = (List<Document>) document.get("valuelist");
                for (String thetime : times) {
                    for (Document valuedoc : valuelist) {
                        if (thetime.equals(valuedoc.getString("time"))) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("monitortime", thetime);
                            map.put("value", valuedoc.get("value"));
                            mn_valuelist.add(map);
                            break;
                        }
                    }
                }

                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("name", document.get("_id"));
                resultmap.put("valuedata", mn_valuelist);
                result.add(resultmap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 上午 10:39
     * @Description: 通过数据类型和时间段获取站点六参数污染物累计浓度（平均浓度）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查空气站点小时表（数据按小时分组）  day:查空气站点日表（数据按日分组）  month：查空气站点月表（数据按月分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd   month:yyyy-MM）
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirStationPollutantCumulativeDataByParamMap(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        List<String> mns = (List<String>) paramMap.get("mns");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        //获取六参数污染物
        List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        Date startDate = null;
        Date endDate = null;
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        //获取综合质量指数
        Map<String, Object> zhzs = countAirStationCompositeIndex(starttime, endtime, mns, datatype);
        if ("hour".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            collection = "StationHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
        } else if ("day".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "StationDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
        } else if ("month".equals(datatype)) {
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            startDate = DataFormatUtil.getDateYMDHMS(starttime + "-01 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "StationMonthAQIData";
            timestr = "%Y-%m";
            times = DataFormatUtil.getMonthBetween(starttime, endtime);
            //times.remove(endtime);
        }
        //获取优良天数
        Map<String, Object> ylts = countAirStationExcellentRate(startDate, endDate, mns, datatype);
        Criteria criteria = new Criteria();
        criteria.and("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("time", "$MonitorTime");
        valuemap.put("value", "$AvgStrength");
        operations.add(unwind("DataList"));
        operations.add(match(Criteria.where("DataList.PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("StationCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("StationCode", "PollutantCode")
                .push(valuemap).as("valuelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<Document> pollutant_doc;

        if (mappedResults.size() > 0) {
            //按点位分组
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
            for (String mn : mns) {
                Map<String, Object> mn_map = new HashMap<>();
                mn_map.put("dgimn", mn);
                mn_map.put("name", mnandname.get(mn));
                for (String code : pollutantcodes) {
                    mn_map.put(code, "");
                    mn_map.put(code + "_name", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                }
                if ("day".equals(datatype)) {
                    mn_map.put("yl_proportion", ylts.get(mn) != null ? ylts.get(mn) + "" : "");
                }
                if ("month".equals(datatype)) {
                    mn_map.put("zhzs", zhzs.get(mn) != null ? zhzs.get(mn) + "" : "");
                }
                if (mapDocuments.get(mn) != null) {
                    List<Document> mn_doc = mapDocuments.get(mn);
                    //遍历六参数
                    for (String code : pollutantcodes) {
                        Double total = 0d;
                        int num = 0;
                        for (Document doc : mn_doc) {
                            if (code.equals(doc.getString("PollutantCode"))) {
                                pollutant_doc = (List<Document>) doc.get("valuelist");
                                for (Document onepodoc : pollutant_doc) {
                                    if (onepodoc.getString("value") != null && !"".equals(onepodoc.getString("value"))) {
                                        total += Double.valueOf(onepodoc.getString("value"));
                                        num += 1;
                                    }
                                }
                            }
                        }
                        if (total > 0) {
                            mn_map.put(code, DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total / num)));
                        }
                    }
                }
                result.add(mn_map);
            }
        }
        return result;
    }

    /**
     * 统计一段时间内的优良率
     */
    private Map<String, Object> countAirStationExcellentRate(Date startDate, Date endDate, List<String> mns, String datatype) {
        Map<String, Object> resultmap = new HashMap<>();
        if ("day".equals(datatype) || "month".equals(datatype)) {
            Criteria criteria = new Criteria();
            criteria.and("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("StationCode", "AQI").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(Aggregation.group("StationCode", "MonitorTime", "AQI")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "StationDayAQIData", Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            if (mappedResults.size() > 0) {
                //通过mn号分组数据
                Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
                List<Document> mndoc;
                for (String mn : mns) {
                    if (mapDocuments.get(mn) != null) {

                        mndoc = mapDocuments.get(mn);
                        int total = 0;
                        int num = 0;
                        for (Document onedoc : mndoc) {
                            if (onedoc.get("AQI") != null) {
                                total += 1;
                                if (onedoc.getInteger("AQI") <= 100) {
                                    num += 1;
                                }
                            }
                        }
                        if (total > 0) {
                            resultmap.put(mn, total * 100 / num);
                        }
                    }
                }

            }
        }
        return resultmap;
    }

    /**
     * 获取一段时间的平均综合指数
     */
    private Map<String, Object> countAirStationCompositeIndex(String starttime, String endtime, List<String> mns, String datatype) throws Exception {
        Map<String, Object> resultmap = new HashMap<>();
        if ("month".equals(datatype)) {
            //获取这段时间内的所有月份
            List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);
            //yms.remove(endtime);
            starttime = starttime + "-01";
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            //获取一段时间的小时数据
            Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
            Set<String> stationCodes = new LinkedHashSet<>();
            Date startDate = DataFormatUtil.getDateYMDH(starttime + " 00");
            Date endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "StationCode", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                    match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
            List<Document> allhours = hourAQIData.getMappedResults();
            //根据日数据获取有效日数据
            startDate = DataFormatUtil.getDateYMD(starttime + " 00");
            endDate = DataFormatUtil.getDateYMDH(endtime + " 23");
            aggregation = newAggregation(
                    project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym"),
                    match(Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
            List<Document> alldays = cityDayAQIData.getMappedResults();
            //小时数据按站点分组
            Map<String, List<Document>> mn_HourMap = allhours.stream().filter(m -> m != null && m.get("StationCode") != null).collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
            //日数据按站点分组
            Map<String, List<Document>> mn_DayMap = alldays.stream().filter(m -> m != null && m.get("StationCode") != null).collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
            List<Document> hourdoc;
            List<Document> daydoc;
            Map<String, Double> monthSO2;
            Map<String, Double> monthNO2;
            Map<String, Double> monthCO;
            Map<String, Double> monthO3;
            Map<String, Double> monthPM10;
            Map<String, Double> monthPM25;
            for (String mn : mns) {
                Double total = 0d;
                Integer count = 0;
                if (mn_HourMap.get(mn) != null && mn_DayMap.get(mn) != null) {
                    hourdoc = mn_HourMap.get(mn);
                    daydoc = mn_DayMap.get(mn);
                    //小时数据按月份分组
                    Map<String, List<Document>> HourMap = hourdoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                    //日数据按月份分组
                    Map<String, List<Document>> DayMap = daydoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                    for (String month : yms) {
                        if (HourMap.get(month) != null && DayMap.get(month) != null) {
                            List<Document> hourDocuments = HourMap.get(month);
                            //根据小时数据获取有效日期
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
                                        stationCodes.add(document.getString("StationCode"));
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
                            List<Document> dayDocuments = DayMap.get(month);
                            if (dayDocuments.size() > 0) {
                                monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
                                monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
                                monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
                                monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
                                monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
                                monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
                                Double compositeIndex;
                                Double SO2;
                                Double NO2;
                                Double CO;
                                Double O3;
                                Double PM10;
                                Double PM25;
                                SO2 = monthSO2.get(mn) != null ? monthSO2.get(mn) : 0d;
                                NO2 = monthNO2.get(mn) != null ? monthNO2.get(mn) : 0d;
                                CO = monthCO.get(mn) != null ? monthCO.get(mn) : 0d;
                                O3 = monthO3.get(mn) != null ? monthO3.get(mn) : 0d;
                                PM10 = monthPM10.get(mn) != null ? monthPM10.get(mn) : 0d;
                                PM25 = monthPM25.get(mn) != null ? monthPM25.get(mn) : 0d;
                                compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                                if (compositeIndex > 0) {
                                    total += compositeIndex;
                                    count += 1;
                                }
                            }
                        }
                    }
                }
                if (count > 0) {
                    resultmap.put(mn, DataFormatUtil.SaveTwoAndSubZero(total / count));
                }
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 4:00
     * @Description: 通过数据类型和时间段获取城市六参数污染物累计浓度（平均浓度）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查城市小时表  day:查城市日表
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd）
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirPollutantCumulativeDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        //获取六参数污染物
        List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        Date startDate = null;
        Date endDate = null;
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        if ("hour".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            collection = "CityHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
        } else if ("day".equals(datatype)) {
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            collection = "CityDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
        }
        Criteria criteria = new Criteria();
        criteria.and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("time", "$MonitorTime");
        valuemap.put("value", "$AvgStrength");
        operations.add(unwind("DataList"));
        operations.add(match(Criteria.where("DataList.PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("RegionCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("RegionCode", "PollutantCode")
                .push(valuemap).as("valuelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<Document> pollutant_doc;
        //获取优良天数
        Map<String, Object> ylts = countAirCityExcellentRate(startDate, endDate, new ArrayList<>(), datatype);

        if (mappedResults.size() > 0) {
            //按点位分组
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("RegionCode").toString()));
            for (String stationCode : mapDocuments.keySet()) {
                Map<String, Object> mn_map = new HashMap<>();
                mn_map.put("name", stationCode);
                for (String code : pollutantcodes) {
                    mn_map.put(code, "");
                    mn_map.put(code + "_name", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                }
                if ("day".equals(datatype)) {
                    mn_map.put("yl_proportion", ylts.get(stationCode) != null ? ylts.get(stationCode) + "" : "");
                }
                List<Document> mn_doc = mapDocuments.get(stationCode);
                //遍历六参数
                for (String code : pollutantcodes) {
                    Double total = 0d;
                    int num = 0;
                    for (Document doc : mn_doc) {
                        if (code.equals(doc.getString("PollutantCode"))) {
                            pollutant_doc = (List<Document>) doc.get("valuelist");
                            for (Document onepodoc : pollutant_doc) {
                                if (onepodoc.getString("value") != null && !"".equals(onepodoc.getString("value"))) {
                                    total += Double.valueOf(onepodoc.getString("value"));
                                    num += 1;
                                }
                            }
                        }
                    }
                    if (total > 0) {
                        mn_map.put(code, DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total / num)));
                    }
                }

                result.add(mn_map);
            }

        }
        return result;
    }

    /**
     * 城市空气质量优良率
     */
    private Map<String, Object> countAirCityExcellentRate(Date startDate, Date endDate, ArrayList<Object> objects, String datatype) {
        Map<String, Object> resultmap = new HashMap<>();
        if ("day".equals(datatype)) {
            Criteria criteria = new Criteria();
            criteria.and("MonitorTime").gte(startDate).lte(endDate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("RegionCode", "AQI").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(Aggregation.group("RegionCode", "MonitorTime", "AQI")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "CityDayAQIData", Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            if (mappedResults.size() > 0) {
                //通过mn号分组数据
                Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("RegionCode").toString()));
                List<Document> mndoc;
                for (String mn : mapDocuments.keySet()) {
                    mndoc = mapDocuments.get(mn);
                    int total = 0;
                    int num = 0;
                    for (Document onedoc : mndoc) {
                        if (onedoc.get("AQI") != null) {
                            total += 1;
                            if (onedoc.getInteger("AQI") <= 100) {
                                num += 1;
                            }
                        }
                    }
                    if (total > 0) {
                        resultmap.put(mn, total * 100 / num);
                    }
                }
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 6:47
     * @Description: 通过监测污染物和时间段和数据类型获取点位同比分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查空气站点小时表（数据按小时分组）  day:查空气站点日表（数据按日分组）  month：查空气站点月表（数据按月分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd   month:yyyy-MM）
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirStationYearOnYeraAnalysisDataByParamMap(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        String mn = (String) paramMap.get("mn");
        List<String> years = new ArrayList<>();
        String lastyear = starttime.substring(0, 4);
        String start_mdh = starttime.substring(4, starttime.length());//开始时间 的 月日
        String end_mdh = endtime.substring(4, starttime.length());//结束时间 的 月日
        for (int i = 9; i > 0; i--) {
            years.add((Integer.valueOf(lastyear) - i) + "");
        }
        years.add(lastyear);
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        List<String> md_list = new ArrayList<>();
        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        if ("hour".equals(datatype)) {

            collection = "StationHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + ":00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + ":59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        } else if ("day".equals(datatype)) {
            collection = "StationDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + " 00:00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + " 23:59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        } else if ("month".equals(datatype)) {
            collection = "StationMonthAQIData";
            timestr = "%Y-%m";
            times = DataFormatUtil.getMonthBetween(starttime, endtime);
            //times.remove(endtime);
            for (int i = 9; i > 0; i--) {
                String end = DataFormatUtil.getLastDayOfMonth(Integer.valueOf(lastyear) - i + end_mdh);
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + "-01 00:00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(end + " 23:59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + "-01 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        if (times.size() > 0) {
            for (String time : times) {
                md_list.add(time.substring(5, time.length()));
            }
        }
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
        Criteria criteria = new Criteria();
        criteria.and("StationCode").is(mn).andOperator(timecriteria);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if ("aqi".equals(pollutantcode)) {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AQI");
            operations.add(Aggregation.project("StationCode", "AQI").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime").
                    and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
                    .andExclude("_id"));
            operations.add(Aggregation.group("year")
                    .push(valuemap).as("valuelist")
            );
        } else {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind("DataList"));
            operations.add(match(Criteria.where("DataList.PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("StationCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
                    .and("DataList.PollutantCode").as("PollutantCode")
                    .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.group("year")
                    .push(valuemap).as("valuelist")
            );
        }

        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        if (mappedResults.size() > 0) {
            String ymd;
            for (String year : years) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("year", year);
                List<Map<String, Object>> onelist = new ArrayList<>();
                for (Document document : mappedResults) {
                    if (year.equals(document.getString("_id"))) {
                        List<Document> valuelist = (List<Document>) document.get("valuelist");
                        //遍历所有月日
                        for (String mandd : md_list) {
                            Map<String, Object> map = new HashMap<>();
                            //遍历年份
                            ymd = year + "-" + mandd;
                            for (Document valuedoc : valuelist) {
                                if (ymd.equals(valuedoc.getString("time"))) {
                                    map.put("monitortime", ymd);
                                    map.put("value", valuedoc.get("value"));
                                    onelist.add(map);
                                    break;
                                }
                            }
                        }
                    }
                }
                onemap.put("datalist", onelist);
                result.add(onemap);
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2022/04/20 0020 上午 9:47
     * @Description: 通过监测污染物和时间段和数据类型获取城市同比分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查城市小时表（数据按小时分组）  day:查城市日表（数据按日分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd）
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirCityYearOnYeraAnalysisDataByParamMap(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        String regioncode = (String) paramMap.get("regioncode");
        List<String> years = new ArrayList<>();
        String lastyear = starttime.substring(0, 4);
        String start_mdh = starttime.substring(4, starttime.length());//开始时间 的 月日
        String end_mdh = endtime.substring(4, starttime.length());//结束时间 的 月日
        for (int i = 9; i > 0; i--) {
            years.add((Integer.valueOf(lastyear) - i) + "");
        }
        years.add(lastyear);
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        List<String> md_list = new ArrayList<>();
        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        if ("hour".equals(datatype)) {
            collection = "CityHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + ":00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + ":59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        } else if ("day".equals(datatype)) {
            collection = "CityDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + " 00:00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + " 23:59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        if (times.size() > 0) {
            for (String time : times) {
                md_list.add(time.substring(5, time.length()));
            }
        }
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
        Criteria criteria = new Criteria();
        criteria.and("RegionCode").is(regioncode).andOperator(timecriteria);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if ("aqi".equals(pollutantcode)) {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AQI");
            operations.add(Aggregation.project("RegionCode", "AQI").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime").
                    and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
                    .andExclude("_id"));
            operations.add(Aggregation.group("year")
                    .push(valuemap).as("valuelist")
            );
        } else {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("time", "$MonitorTime");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind("DataList"));
            operations.add(match(Criteria.where("DataList.PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("RegionCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
                    .and("DataList.PollutantCode").as("PollutantCode")
                    .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.group("year")
                    .push(valuemap).as("valuelist")
            );
        }

        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<Map<String, Object>> mn_valuelist = new ArrayList<>();
        if (mappedResults.size() > 0) {
            String ymd;
            for (String year : years) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("year", year);
                List<Map<String, Object>> onelist = new ArrayList<>();
                for (Document document : mappedResults) {
                    if (year.equals(document.getString("_id"))) {
                        List<Document> valuelist = (List<Document>) document.get("valuelist");
                        //遍历所有月日
                        for (String mandd : md_list) {
                            Map<String, Object> map = new HashMap<>();
                            //遍历年份
                            ymd = year + "-" + mandd;
                            for (Document valuedoc : valuelist) {
                                if (ymd.equals(valuedoc.getString("time"))) {
                                    map.put("monitortime", ymd);
                                    map.put("value", valuedoc.get("value"));
                                    onelist.add(map);
                                    break;
                                }
                            }
                        }
                    }
                }
                onemap.put("datalist", onelist);
                result.add(onemap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/04/20 0020 上午 10:15
     * @Description: 通过时间段和数据类型获取单点位各污染物累计同比分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查空气站点小时表（数据按小时分组）  day:查空气站点日表（数据按日分组）  month：查空气站点月表（数据按月分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd   month:yyyy-MM）
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAirStationYearOnYeraCumulativeDataByParamMap(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        //获取六参数污染物
        List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        String mn = (String) paramMap.get("mn");
        List<String> years = new ArrayList<>();
        String lastyear = starttime.substring(0, 4);
        String start_mdh = starttime.substring(4, starttime.length());//开始时间 的 月日
        String end_mdh = endtime.substring(4, starttime.length());//结束时间 的 月日
        for (int i = 9; i > 0; i--) {
            years.add((Integer.valueOf(lastyear) - i) + "");
        }
        years.add(lastyear);
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        List<String> md_list = new ArrayList<>();
        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        if ("hour".equals(datatype)) {

            collection = "StationHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + ":00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + ":59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        } else if ("day".equals(datatype)) {
            collection = "StationDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + " 00:00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + " 23:59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        } else if ("month".equals(datatype)) {
            collection = "StationMonthAQIData";
            timestr = "%Y-%m";
            times = DataFormatUtil.getMonthBetween(starttime, endtime);
            //times.remove(endtime);
            for (int i = 9; i > 0; i--) {
                String end = DataFormatUtil.getLastDayOfMonth(Integer.valueOf(lastyear) - i + end_mdh);
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + "-01 00:00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(end + " 23:59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + "-01 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        if (times.size() > 0) {
            for (String time : times) {
                md_list.add(time.substring(5, time.length()));
            }
        }
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
        Criteria criteria = new Criteria();
        criteria.and("StationCode").is(mn).andOperator(timecriteria);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("time", "$MonitorTime");
        valuemap.put("value", "$AvgStrength");
        operations.add(unwind("DataList"));
        operations.add(match(Criteria.where("DataList.PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("StationCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("year", "PollutantCode")
                .push(valuemap).as("valuelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<Document> pollutant_doc;
        List<Document> pollutantlist_doc;
        //获取优良天数
        Map<String, Object> ylts = countOneStationMoreTimesExcellentRate(years, criteria, datatype, "StationDayAQIData");
        //获取综合质量指数
        Map<String, Object> zhzs = countOneStationMoreTimesCompositeIndex(years, md_list, mn, timecriteria, datatype);
        if (mappedResults.size() > 0) {
            //通过年分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("year").toString()));
            for (String year : years) {
                Map<String, Object> code_map = new HashMap<>();
                if (mapDocuments.get(year) != null) {
                    code_map.put("yeartime", year);
                    pollutantlist_doc = mapDocuments.get(year);
                    //求平均值
                    for (String code : pollutantcodes) {
                        Double total = 0d;
                        int num = 0;
                        code_map.put(code + "_name", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                        code_map.put(code, "");
                        for (Document podoc : pollutantlist_doc) {
                            if (code.equals(podoc.getString("PollutantCode"))) {
                                pollutant_doc = (List<Document>) podoc.get("valuelist");
                                for (Document onepodoc : pollutant_doc) {
                                    if (onepodoc.getString("value") != null && !"".equals(onepodoc.getString("value"))) {
                                        total += Double.valueOf(onepodoc.getString("value"));
                                        num += 1;
                                    }
                                }
                                break;
                            }
                        }
                        if (total > 0) {
                            code_map.put(code, DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total / num)));
                        }

                    }
                    if ("day".equals(datatype)) {
                        code_map.put("yl_proportion", ylts.get(year) != null ? ylts.get(year) + "" : "");
                    }
                    if ("month".equals(datatype)) {
                        code_map.put("zhzs", zhzs.get(year) != null ? zhzs.get(year) + "" : "");
                    }
                } else {
                    code_map.put("yeartime", year);
                    for (String code : pollutantcodes) {
                        code_map.put(code + "_name", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                        code_map.put(code, "");
                    }
                    code_map.put("yl_proportion", "");
                    code_map.put("zhzs", "");
                }


                result.add(code_map);
            }
        }
        return result;
    }

    /**
     * 单站点多时段累计综合指数
     */
    private Map<String, Object> countOneStationMoreTimesCompositeIndex(List<String> years, List<String> md_list, String mn, Criteria timecriteria, String datatype) {
        Map<String, Object> resultmap = new HashMap<>();
        if ("month".equals(datatype)) {
            //获取一段时间的小时数据
            Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
            Set<String> stationCodes = new LinkedHashSet<>();
            Fields fields = fields("MonitorTime", "StationCode", "DataList", "AQI");
            Aggregation aggregation = newAggregation(
                    match(Criteria.where("StationCode").is(mn).andOperator(timecriteria)),
                    project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym").
                            and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year"),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
            List<Document> allhours = hourAQIData.getMappedResults();
            aggregation = newAggregation(
                    match(Criteria.where("StationCode").is(mn).andOperator(timecriteria)),
                    project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym").
                            and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year"),

                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
            List<Document> alldays = cityDayAQIData.getMappedResults();
            //小时数据按年分组
            Map<String, List<Document>> Hour_YearMap = allhours.stream().filter(m -> m != null && m.get("year") != null).collect(Collectors.groupingBy(m -> m.get("year").toString()));
            //日数据按年分组
            Map<String, List<Document>> Day_YearMap = alldays.stream().filter(m -> m != null && m.get("year") != null).collect(Collectors.groupingBy(m -> m.get("year").toString()));
            List<Document> hourdoc;
            List<Document> daydoc;
            Map<String, List<Document>> HourMap;
            Map<String, List<Document>> DayMap;

            for (String year : years) {
                Double total = 0d;
                int count = 0;
                if (Hour_YearMap.get(year) != null && Day_YearMap.get(year) != null) {
                    hourdoc = Hour_YearMap.get(year);
                    daydoc = Day_YearMap.get(year);
                    //小时数据按月份分组
                    HourMap = hourdoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                    //日数据按月份分组
                    DayMap = daydoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                    Map<String, Double> monthSO2;
                    Map<String, Double> monthNO2;
                    Map<String, Double> monthCO;
                    Map<String, Double> monthO3;
                    Map<String, Double> monthPM10;
                    Map<String, Double> monthPM25;
                    for (String month : md_list) {
                        if (HourMap.get(year + "-" + month) != null && DayMap.get(year + "-" + month) != null) {
                            List<Document> hourDocuments = HourMap.get(year + "-" + month);
                            //根据小时数据获取有效日期
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
                                        stationCodes.add(document.getString("StationCode"));
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
                            List<Document> dayDocuments = DayMap.get(year + "-" + month);
                            if (dayDocuments.size() > 0) {
                                monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
                                monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
                                monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
                                monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
                                monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
                                monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
                                Double compositeIndex;
                                Double SO2;
                                Double NO2;
                                Double CO;
                                Double O3;
                                Double PM10;
                                Double PM25;
                                SO2 = monthSO2.get(mn) != null ? monthSO2.get(mn) : 0d;
                                NO2 = monthNO2.get(mn) != null ? monthNO2.get(mn) : 0d;
                                CO = monthCO.get(mn) != null ? monthCO.get(mn) : 0d;
                                O3 = monthO3.get(mn) != null ? monthO3.get(mn) : 0d;
                                PM10 = monthPM10.get(mn) != null ? monthPM10.get(mn) : 0d;
                                PM25 = monthPM25.get(mn) != null ? monthPM25.get(mn) : 0d;
                                compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                                if (compositeIndex > 0) {
                                    if (count == 0) {
                                        total += compositeIndex;
                                        count += 1;
                                    } else {
                                        total = compositeIndex;
                                        count = 1;
                                    }
                                }
                            }
                        }
                    }
                }
                if (count > 0) {
                    resultmap.put(year, DataFormatUtil.SaveTwoAndSubZero(total / count));
                }

            }
        }
        return resultmap;
    }

    /**
     * 获取单站点或城市多时间段的优良率
     */
    private Map<String, Object> countOneStationMoreTimesExcellentRate(List<String> years, Criteria criteria, String datatype, String coolection) {
        Map<String, Object> resultmap = new HashMap<>();
        if ("day".equals(datatype) || "month".equals(datatype)) {
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("AQI", "MonitorTime").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
            );
            operations.add(Aggregation.group("year", "MonitorTime", "AQI")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            if (mappedResults.size() > 0) {
                //通过年份分组数据
                Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("year").toString()));
                List<Document> mndoc;
                for (String year : years) {
                    if (mapDocuments.get(year) != null) {
                        mndoc = mapDocuments.get(year);
                        int total = 0;
                        int num = 0;
                        for (Document onedoc : mndoc) {
                            if (onedoc.get("AQI") != null) {
                                total += 1;
                                if (onedoc.getInteger("AQI") <= 100) {
                                    num += 1;
                                }
                            }
                        }
                        if (total > 0) {
                            resultmap.put(year, total * 100 / num);
                        }
                    }
                }

            }
        }
        return resultmap;
    }

    /**
     * 城市同比六参数累计浓度值
     */
    @Override
    public List<Map<String, Object>> getCityYearOnYeraCumulativeDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        //获取六参数污染物
        List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        String regioncode = (String) paramMap.get("regioncode");
        List<String> years = new ArrayList<>();
        String lastyear = starttime.substring(0, 4);
        String start_mdh = starttime.substring(4, starttime.length());//开始时间 的 月日
        String end_mdh = endtime.substring(4, starttime.length());//结束时间 的 月日
        for (int i = 9; i > 0; i--) {
            years.add((Integer.valueOf(lastyear) - i) + "");
        }
        years.add(lastyear);
        String collection = "";
        String timestr = "";
        List<String> times = new ArrayList<>();
        List<String> md_list = new ArrayList<>();
        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        if ("hour".equals(datatype)) {
            collection = "CityHourAQIData";
            timestr = "%Y-%m-%d %H";
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + ":00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + ":59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        } else if ("day".equals(datatype)) {
            collection = "CityDayAQIData";
            timestr = "%Y-%m-%d";
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
            for (int i = 9; i > 0; i--) {
                Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + " 00:00:00");
                Date endDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + end_mdh + " 23:59:59");
                criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            }
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        if (times.size() > 0) {
            for (String time : times) {
                md_list.add(time.substring(5, time.length()));
            }
        }
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
        Criteria criteria = new Criteria();
        criteria.and("RegionCode").is(regioncode).andOperator(timecriteria);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("time", "$MonitorTime");
        valuemap.put("value", "$AvgStrength");
        operations.add(unwind("DataList"));
        operations.add(match(Criteria.where("DataList.PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("RegionCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("year", "PollutantCode")
                .push(valuemap).as("valuelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<Document> pollutant_doc;
        List<Document> pollutantlist_doc;
        //获取优良天数
        Map<String, Object> ylts = countOneStationMoreTimesExcellentRate(years, criteria, datatype, collection);
        //获取综合质量指数
        //Map<String, Object> zhzs = countAirStationCompositeIndex(starttime,endtime,mns,datatype);
        if (mappedResults.size() > 0) {
            //通过年分组数据
            Map<String, List<Document>> mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("year").toString()));
            for (String year : years) {
                Map<String, Object> code_map = new HashMap<>();
                if (mapDocuments.get(year) != null) {
                    code_map.put("yeartime", year);
                    pollutantlist_doc = mapDocuments.get(year);
                    //求平均值
                    for (String code : pollutantcodes) {
                        Double total = 0d;
                        int num = 0;
                        code_map.put(code + "_name", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                        code_map.put(code, "");
                        for (Document podoc : pollutantlist_doc) {
                            if (code.equals(podoc.getString("PollutantCode"))) {
                                pollutant_doc = (List<Document>) podoc.get("valuelist");
                                for (Document onepodoc : pollutant_doc) {
                                    if (onepodoc.getString("value") != null && !"".equals(onepodoc.getString("value"))) {
                                        total += Double.valueOf(onepodoc.getString("value"));
                                        num += 1;
                                    }
                                }
                                break;
                            }
                        }
                        if (total > 0) {
                            code_map.put(code, DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total / num)));
                        }

                    }
                    if ("day".equals(datatype)) {
                        code_map.put("yl_proportion", ylts.get(year) != null ? ylts.get(year) + "" : "");
                    }
                } else {

                    code_map.put("yeartime", year);
                    for (String code : pollutantcodes) {
                        code_map.put(code + "_name", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                        code_map.put(code, "");
                    }
                    code_map.put("yl_proportion", "");
                }
                /*if ("month".equals(datatype)){
                    mn_map.put("zhzs",zhzs.get(mn)!=null?zhzs.get(mn)+"":"");
                }*/
                result.add(code_map);
            }
        }
        return result;
    }

    /**
     * 统计单站点月同比数据
     */
    @Override
    public List<Map<String, Object>> getOneStationMonthYearOnYeraCompositeIndex(Map<String, Object> paramMap) throws Exception {
        //获取这段时间内的所有月份
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String mn = (String) paramMap.get("mn");
        List<String> years = new ArrayList<>();
        String lastyear = starttime.substring(0, 4);
        String start_mdh = starttime.substring(4, starttime.length());//开始时间 的 月日
        String end_mdh = endtime.substring(4, starttime.length());//结束时间 的 月日
        for (int i = 9; i > 0; i--) {
            years.add((Integer.valueOf(lastyear) - i) + "");
        }
        years.add(lastyear);
        List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);
        //yms.remove(endtime);
        List<String> md_list = new ArrayList<>();
        if (yms.size() > 0) {
            for (String time : yms) {
                md_list.add(time.substring(5, time.length()));
            }
        }
        Criteria timecriteria = new Criteria();
        List<Criteria> criterialist = new ArrayList<>();
        for (int i = 9; i > 0; i--) {
            String end = DataFormatUtil.getLastDayOfMonth(Integer.valueOf(lastyear) - i + end_mdh);
            Date startDate = DataFormatUtil.getDateYMDHMS(Integer.valueOf(lastyear) - i + start_mdh + "-01 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(end + " 23:59:59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        endtime = DataFormatUtil.getLastDayOfMonth(endtime);
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + "-01 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
        //获取一段时间的小时数据
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, List<String>> codeAndEffectiveDay = new HashMap<>();
        Set<String> stationCodes = new LinkedHashSet<>();
        Fields fields = fields("MonitorTime", "StationCode", "DataList", "AQI");
        Aggregation aggregation = newAggregation(
                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym").
                        and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year"),
                match(Criteria.where("StationCode").is(mn).andOperator(timecriteria)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> allhours = hourAQIData.getMappedResults();
        aggregation = newAggregation(

                project(fields).and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("ym").
                        and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("year"),
                match(Criteria.where("StationCode").is(mn).andOperator(timecriteria)),
                sort(Sort.Direction.ASC, "MonitorTime")
        );
        AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, dayCollection, Document.class);
        List<Document> alldays = cityDayAQIData.getMappedResults();
        //小时数据按年分组
        Map<String, List<Document>> Hour_YearMap = allhours.stream().filter(m -> m != null && m.get("year") != null).collect(Collectors.groupingBy(m -> m.get("year").toString()));
        //日数据按年分组
        Map<String, List<Document>> Day_YearMap = alldays.stream().filter(m -> m != null && m.get("year") != null).collect(Collectors.groupingBy(m -> m.get("year").toString()));
        List<Document> hourdoc;
        List<Document> daydoc;
        Map<String, List<Document>> HourMap;
        Map<String, List<Document>> DayMap;
        Map<String, List<Map<String, Object>>> onemap = new HashMap<>();
        for (String year : years) {
            if (Hour_YearMap.get(year) != null && Day_YearMap.get(year) != null) {
                hourdoc = Hour_YearMap.get(year);
                daydoc = Day_YearMap.get(year);
                //小时数据按月份分组
                HourMap = hourdoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                //日数据按月份分组
                DayMap = daydoc.stream().filter(m -> m != null && m.get("ym") != null).collect(Collectors.groupingBy(m -> m.get("ym").toString()));
                Map<String, Double> monthSO2;
                Map<String, Double> monthNO2;
                Map<String, Double> monthCO;
                Map<String, Double> monthO3;
                Map<String, Double> monthPM10;
                Map<String, Double> monthPM25;
                for (String month : md_list) {
                    if (HourMap.get(year + "-" + month) != null && DayMap.get(year + "-" + month) != null) {
                        List<Document> hourDocuments = HourMap.get(year + "-" + month);
                        //根据小时数据获取有效日期
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
                                    stationCodes.add(document.getString("StationCode"));
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
                        List<Document> dayDocuments = DayMap.get(year + "-" + month);
                        if (dayDocuments.size() > 0) {
                            monthSO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21026", 60);
                            monthNO2 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a21004", 40);
                            monthCO = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a21005", 95, 4);
                            monthO3 = getSumPercentileItemByCode(dayDocuments, codeAndEffectiveDay, "a05024", 90, 160);
                            monthPM10 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34002", 70);
                            monthPM25 = getSumItemByCode(dayDocuments, codeAndEffectiveDay, "a34004", 35);
                            Double compositeIndex;
                            Double SO2;
                            Double NO2;
                            Double CO;
                            Double O3;
                            Double PM10;
                            Double PM25;
                            SO2 = monthSO2.get(mn) != null ? monthSO2.get(mn) : 0d;
                            NO2 = monthNO2.get(mn) != null ? monthNO2.get(mn) : 0d;
                            CO = monthCO.get(mn) != null ? monthCO.get(mn) : 0d;
                            O3 = monthO3.get(mn) != null ? monthO3.get(mn) : 0d;
                            PM10 = monthPM10.get(mn) != null ? monthPM10.get(mn) : 0d;
                            PM25 = monthPM25.get(mn) != null ? monthPM25.get(mn) : 0d;
                            compositeIndex = SO2 + NO2 + CO + O3 + PM10 + PM25;
                            if (onemap.get(year) != null) {
                                List<Map<String, Object>> valuelist = onemap.get(year);
                                Map<String, Object> valuemap = new HashMap<>();
                                valuemap.put("monitortime", year + "-" + month);
                                valuemap.put("value", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                                valuelist.add(valuemap);
                                onemap.put(year, valuelist);
                            } else {
                                List<Map<String, Object>> valuelist = new ArrayList<>();
                                Map<String, Object> valuemap = new HashMap<>();
                                valuemap.put("monitortime", year + "-" + month);
                                valuemap.put("value", DataFormatUtil.SaveTwoAndSubZero(compositeIndex));
                                valuelist.add(valuemap);
                                onemap.put(year, valuelist);
                            }

                        }
                    }
                }
            }
        }
        for (String key : onemap.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("year", key);
            resultMap.put("valuedata", onemap.get(key));
            result.add(resultMap);
        }
        return result;
    }


    /**
     * @Description: 获取数据报表表头数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:21
     */
    @Override
    public List<Map<String, Object>> getStationTitleListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //序号
        Map<String, Object> map1 = new HashMap<>();
        map1.put("label", "序号");
        map1.put("prop", "ordernum");
        map1.put("align", "center");
        map1.put("headeralign", "center");
        map1.put("showhide", false);
        map1.put("sortable", false);
        map1.put("minwidth", "20px");
        resultList.add(map1);
        //站点名称
        Map<String, Object> map2 = new HashMap<>();
        map2.put("label", "站点名称");
        map2.put("prop", "monitorpointname");
        map2.put("align", "center");
        map2.put("fixed", "left");
        map2.put("headeralign", "center");
        map2.put("showhide", true);
        map2.put("sortable", false);
        map2.put("minwidth", "200px");
        resultList.add(map2);
        //AQI
        Map<String, Object> map3 = new HashMap<>();
        map3.put("label", "AQI");
        map3.put("prop", "aqi");
        map3.put("align", "center");
        map3.put("headeralign", "center");
        map3.put("showhide", true);
        map3.put("sortable", "custom");
        map3.put("width", "120px");
        resultList.add(map3);
        //站点因子
        List<Map<String, Object>> pollutantSetList = airMonitorStationMapper.getAirStationAllPollutantsByIDAndType(paramMap);
        String name;
        for (Map<String, Object> pollutant : pollutantSetList) {
            if (pollutant.get("name") != null && pollutant.get("code") != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("prop", pollutant.get("code"));
                name = pollutant.get("name").toString();
                if (pollutant.get("PollutantUnit") != null) {
                    name = name + "(" + pollutant.get("PollutantUnit") + ")";
                }
                map.put("label", name);
                map.put("align", "center");
                map.put("headeralign", "center");
                map.put("showhide", true);
                map.put("sortable", "custom");
                map.put("width", "120px");
                resultList.add(map);
            }

        }
        //
        //首要污染物
        Map<String, Object> map4 = new HashMap<>();
        map4.put("label", "首要污染物");
        map4.put("prop", "primarypollutant");
        map4.put("align", "center");
        map4.put("headeralign", "center");
        map4.put("fixed", "left");
        map4.put("showhide", true);
        map4.put("sortable", false);
        map4.put("width", "130px");
        resultList.add(map4);
        return resultList;
    }

    /**
     * @Description: 获取报表数据信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 9:47
     */
    @Override
    public List<Map<String, Object>> getStationReportDataListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> pointList = airMonitorStationMapper.getAirStationInfoByParamMap(paramMap);
        List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
        if (pointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndName = new HashMap<>();
            for (Map<String, Object> point : pointList) {
                if (point.get("dgimn") != null && dgimns.contains(point.get("dgimn").toString())) {
                    mnCommon = point.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndName.put(mnCommon, point.get("monitorpointname"));
                }
            }
            String timeType = paramMap.get("timetype").toString();
            String monitortime = paramMap.get("monitortime").toString();
            String collection;
            String collectionAqi;
            String dataKey = "HourDataList";
            Date time;
            if (timeType.equals("day")) {
                collection = DB_DayData;
                collectionAqi = dayCollection;
                dataKey = "DayDataList";
                time = DataFormatUtil.getDateYMD(monitortime);
            } else {
                collectionAqi = hourCollection;
                collection = DB_HourData;
                time = DataFormatUtil.getDateYMDH(monitortime);
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").is(time);
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(dataKey));
            operations.add(Aggregation.project("DataGatherCode")
                    .and(dataKey + ".PollutantCode").as("PollutantCode")
                    .and(dataKey + ".AvgStrength").as("AvgStrength")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations);
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> dataDoc = pageResults.getMappedResults();
            Map<String, Map<String, Object>> mnAndPollutant = new HashMap<>();
            Map<String, Object> keyAndValue;
            for (Document document : dataDoc) {
                mnCommon = document.getString("DataGatherCode");
                keyAndValue = mnAndPollutant.get(mnCommon) != null ? mnAndPollutant.get(mnCommon) : new HashMap<>();
                keyAndValue.put(document.getString("PollutantCode"), document.get("AvgStrength"));
                mnAndPollutant.put(mnCommon, keyAndValue);
            }
            operations.clear();
            criteria = Criteria.where("StationCode").in(mns).and("MonitorTime").is(time);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("StationCode", "AQI", "PrimaryPollutant")
            );
            aggregationList = Aggregation.newAggregation(operations);
            pageResults = mongoTemplate.aggregate(aggregationList, collectionAqi, Document.class);
            List<Document> aqiDoc = pageResults.getMappedResults();
            Map<String, Object> mnAndAqi = new HashMap<>();
            Map<String, Object> mnAndPol = new HashMap<>();
            String PrimaryPollutant;
            for (Document document : aqiDoc) {
                mnCommon = document.getString("StationCode");
                mnAndAqi.put(mnCommon, document.get("AQI"));
                if (document.get("PrimaryPollutant") != null) {
                    PrimaryPollutant = document.getString("PrimaryPollutant");
                    PrimaryPollutant = getPrimaryPollutant(paramMap, PrimaryPollutant);
                    mnAndPol.put(mnCommon, PrimaryPollutant);
                }
            }
            for (String mnIndex : mnAndName.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("mn", mnIndex);
                resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                resultMap.put("aqi", mnAndAqi.get(mnIndex));
                resultMap.put("primarypollutant", mnAndPol.get(mnIndex));
                if (mnAndPollutant.get(mnIndex) != null) {
                    resultMap.putAll(mnAndPollutant.get(mnIndex));
                }
                resultList.add(resultMap);
            }
            //排序
            if (paramMap.get("sortmap") != null) {
                Map<String, Object> sortMap = (Map<String, Object>) paramMap.get("sortmap");

                String sortKey = sortMap.get("sortkey").toString();
                String sortType = sortMap.get("sorttype").toString();

                if ("desc".equals(sortType)) {
                    resultList = resultList.stream().sorted(
                            Comparator.comparing(m -> comparingDoubleByKey(m, sortKey))
                    ).collect(Collectors.toList());
                } else {
                    resultList = resultList.stream().sorted(
                            Comparator.comparing(m -> comparingDoubleByKey((Map<String, Object>) m, sortKey)).reversed()
                    ).collect(Collectors.toList());
                }
            }
            //添加序号列
            int index = 0;
            for (Map<String, Object> dataMap : resultList) {
                index++;
                dataMap.put("ordernum", index);
            }
        }
        return resultList;
    }


    /**
     * @Description: 获取站点排名数据（小时、日数据）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/16 13:31
     */
    @Override
    public List<Map<String, Object>> getStationRantDataByParams(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> pointList = airMonitorStationMapper.getAirStationInfoByParamMap(paramMap);
        if (pointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndName = new HashMap<>();
            for (Map<String, Object> point : pointList) {
                if (point.get("dgimn") != null) {
                    mnCommon = point.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndName.put(mnCommon, point.get("monitorpointname"));
                }
            }
            String timeType = paramMap.get("timetype").toString();
            String monitortime = paramMap.get("monitortime").toString();

            String collectionAqi = hourCollection;
            String dataKey = "DataList";
            Date time = DataFormatUtil.getDateYMDH(monitortime);
            if (timeType.equals("day")) {
                collectionAqi = dayCollection;
                time = DataFormatUtil.getDateYMD(monitortime);
            }
            String pollutantcode = paramMap.get("pollutantcode").toString();
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = Criteria.where("StationCode").in(mns).and("MonitorTime").is(time);
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(dataKey));

            if (!pollutantcode.equals("aqi")) {
                Criteria criteria1 = Criteria.where(dataKey + ".PollutantCode").is(pollutantcode);
                operations.add(match(criteria1));
            }
            operations.add(Aggregation.project("StationCode", "AQI")
                    .and(dataKey + ".PollutantCode").as("PollutantCode")
                    .and(dataKey + ".Strength").as("Strength")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations);
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collectionAqi, Document.class);
            List<Document> dataDoc = pageResults.getMappedResults();
            Map<String, Object> mnAndValue = new HashMap<>();

            for (Document document : dataDoc) {
                mnCommon = document.getString("StationCode");
                if (pollutantcode.equals("aqi")) {
                    mnAndValue.put(mnCommon, document.get("AQI"));
                } else {
                    mnAndValue.put(mnCommon, document.get("Strength"));
                }

            }
            for (String mnIndex : mnAndName.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("mn", mnIndex);
                resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                resultMap.put("value", mnAndValue.get(mnIndex));
                resultList.add(resultMap);
            }
            resultList = resultList.stream().sorted(
                    Comparator.comparing(m -> comparingDoubleByKey((Map<String, Object>) m, "value")).reversed()
            ).collect(Collectors.toList());
        }
        return resultList;
    }


    /**
     * @Description: 获取空气站点空气质量数据分布（月、日）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/17 8:44
     */
    @Override
    public List<Map<String, Object>> getStationDistributeDataByParams(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> pointList = airMonitorStationMapper.getAirStationInfoByParamMap(paramMap);
        if (pointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            String mnCommon;
            Map<String, Object> mnAndName = new HashMap<>();
            for (Map<String, Object> point : pointList) {
                if (point.get("dgimn") != null) {
                    mnCommon = point.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndName.put(mnCommon, point.get("monitorpointname"));
                }
            }
            String timeType = paramMap.get("timetype").toString();
            String monitortime = paramMap.get("monitortime").toString();
            String collectionAqi;
            String dataKey = "DataList";
            Date startTime;
            Date endTime;
            String timeF;
            if (timeType.equals("day")) {
                collectionAqi = dayCollection;
                timeF = "dd";
                startTime = DataFormatUtil.getDateYMD(DataFormatUtil.getFirstDayOfMonth(monitortime));
                endTime = DataFormatUtil.getDateYMD(DataFormatUtil.getLastDayOfMonth(monitortime));
            } else {
                collectionAqi = hourCollection;
                timeF = "HH";
                startTime = DataFormatUtil.getDateYMDH(monitortime + " 00");
                endTime = DataFormatUtil.getDateYMDH(monitortime + " 23");
            }
            String pollutantcode = paramMap.get("pollutantcode").toString();
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = Criteria.where("StationCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime);
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(dataKey));
            if (!pollutantcode.equals("aqi")) {
                Criteria criteria1 = Criteria.where(dataKey + ".PollutantCode").is(pollutantcode);
                operations.add(match(criteria1));
            }
            operations.add(Aggregation.project("StationCode", "AQI", "MonitorTime")
                    .and(dataKey + ".PollutantCode").as("PollutantCode")
                    .and(dataKey + ".Strength").as("Strength")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations);
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collectionAqi, Document.class);
            List<Document> dataDoc = pageResults.getMappedResults();
            Map<String, Map<String, Object>> mnAndTimeAndValue = new HashMap<>();
            Map<String, Object> timeAndValue;
            String time;
            for (Document document : dataDoc) {
                mnCommon = document.getString("StationCode");
                time = DataFormatUtil.formatDateToOtherFormat(document.getDate("MonitorTime"), timeF);
                timeAndValue = mnAndTimeAndValue.get(mnCommon) != null ? mnAndTimeAndValue.get(mnCommon) : new HashMap<>();
                if (pollutantcode.equals("aqi")) {
                    timeAndValue.put(time, document.get("AQI"));
                } else {
                    timeAndValue.put(time, document.get("Strength"));
                }
                mnAndTimeAndValue.put(mnCommon, timeAndValue);
            }
            for (String mnIndex : mnAndName.keySet()) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("mn", mnIndex);
                resultMap.put("monitorpointname", mnAndName.get(mnIndex));
                if (mnAndTimeAndValue.get(mnIndex) != null) {
                    resultMap.putAll(mnAndTimeAndValue.get(mnIndex));
                }
                resultList.add(resultMap);
            }
            resultList = resultList.stream()
                    .sorted(Comparator.comparing(m -> ((Map) m).get("monitorpointname").toString()))
                    .collect(Collectors.toList());
        }
        return resultList;
    }


    /**
     * @Description: 获取站点六参数同比数据（小时、日）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/18 9:02
     */
    @Override
    public List<Map<String, Object>> getStationSixTBDataByParams(Map<String, Object> paramMap) throws Exception {
        String timetype = paramMap.get("timetype").toString();
        String dgimn = paramMap.get("dgimn").toString();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        String collection;
        if (timetype.equals("hour")) {
            collection = hourCollection;
        } else {
            collection = dayCollection;
        }
        int year = 11;
        Map<String, Double> codeAndAvg;
        Map<String, Map<String, Double>> timeAndCodeAndAvg = new LinkedHashMap<>();
        String times;
        Map<String, Integer> timeAndYLTS = new LinkedHashMap<>();
        Map<String, Double> timeAndZHZS = new LinkedHashMap<>();

        int ylts;
        Double zhzs;
        Map<String, Object> zhzsParam = new HashMap<>();
        zhzsParam.put("mns", Arrays.asList(dgimn));
        zhzsParam.put("mnandname", new HashMap<>());
        for (int i = 0; i < year; i++) {
            codeAndAvg = getCodeAndValue(timetype, i, starttime, endtime, dgimn, collection, pollutantcodes);
            times = getTimes(timetype, i, starttime, endtime);


            timeAndCodeAndAvg.put(times, codeAndAvg);

            if (!timetype.equals("hour")){
                ylts = getYLTS(timetype, i, starttime, endtime, dgimn, collection);
                timeAndYLTS.put(times, ylts);
            }else {
                timeAndYLTS.put(times, 0);
            }
            if (timetype.equals("month")){
                zhzsParam.put("starttime", times.split(" - ")[0]);
                zhzsParam.put("endtime", times.split(" - ")[1]);
                zhzs = getZHZS(zhzsParam);
                timeAndZHZS.put(times,zhzs);
            }else {
                timeAndZHZS.put(times,0d);
            }

        }
        Map<String, Double> thisAvg;
        Map<String, Double> thatAvg;
        Double thisValue;
        Double thatValue;
        int thisNum;
        int thatNum;
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String timeIndex : timeAndCodeAndAvg.keySet()) {
            starttime = timeIndex.split(" - ")[0];
            endtime = timeIndex.split(" - ")[1];
            times = getTimes(timetype, 1, starttime, endtime);
            thisAvg = timeAndCodeAndAvg.get(timeIndex);
            thatAvg = timeAndCodeAndAvg.get(times);

            if (thatAvg != null) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("times", timeIndex);
                for (String code : pollutantcodes) {
                    if (thisAvg != null && thisAvg.get(code) != null) {
                        thisValue = thisAvg.get(code);
                        if (CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code).equals("CO")){
                            resultMap.put(code + "_value", DataFormatUtil.SaveOneAndSubZero(thisValue));
                        }else {
                            resultMap.put(code + "_value", DataFormatUtil.formatDoubleSaveNo(thisValue));
                        }

                    } else {
                        resultMap.put(code + "_value", "-");
                    }
                    if (thatAvg != null && thisAvg != null && thatAvg.get(code) != null && thisAvg.get(code) != null) {
                        thisValue = thisAvg.get(code);
                        thatValue = thatAvg.get(code);
                        if (thatValue > 0) {
                            resultMap.put(code + "_tb", DataFormatUtil.SaveOneAndSubZero(100D * (thisValue - thatValue) / thatValue));
                        } else {
                            resultMap.put(code + "_tb", "-");
                        }
                    } else {
                        resultMap.put(code + "_tb", "-");
                    }
                }
                thisNum = timeAndYLTS.get(timeIndex);
                thatNum = timeAndYLTS.get(times);
                resultMap.put("ylts_value", thisNum);
                if (thisNum > 0 && thatNum > 0) {
                    resultMap.put("ylts_tb", DataFormatUtil.SaveOneAndSubZero(100D * (thisNum - thatNum) / thatNum));
                } else {
                    resultMap.put("ylts_tb", "-");
                }


                thisValue = timeAndZHZS.get(timeIndex);
                thatValue = timeAndZHZS.get(times);
                resultMap.put("zhzs_value", DataFormatUtil.SaveTwoAndSubZero(thisValue));
                if (thisValue > 0 && thatValue > 0) {
                    resultMap.put("zhzs_tb", DataFormatUtil.SaveOneAndSubZero(100D * (thisValue - thatValue) / thatValue));
                } else {
                    resultMap.put("zhzs_tb", "-");
                }
                resultList.add(resultMap);
            }


        }
        return resultList;
    }

    private Double getZHZS(Map<String, Object> zhzsParam) throws Exception {
        Double avgD = 0d;
        List<Map<String, Object>> dataList = getManyMonthManyStationMonthCompositeIndex(zhzsParam);
        if (dataList.size()>0){
            List<Double> values = new ArrayList<>();
            Double valueD;
            Map<String,Object> dataMap = dataList.get(0);
            List<Map<String,Object>> valueList = (List<Map<String, Object>>) dataMap.get("valuedata");
            for (Map<String,Object> value:valueList){
                if (value.get("value")!=null){
                    valueD = Double.parseDouble(value.get("value").toString());
                    values.add(valueD);
                }

            }
            avgD = DataFormatUtil.getListAvgDValue(values);
        }
        return avgD;
    }

    private int getYLTS(String timetype, int i, String starttime, String endtime, String dgimn, String collection) {
        Date startTime;
        Date endTime;

        if (timetype.equals("day")) {
            starttime = DataFormatUtil.getDayYearTBDate(starttime, i);
            startTime = DataFormatUtil.getDateYMD(starttime);
            endtime = DataFormatUtil.getDayYearTBDate(endtime, i);
            endTime = DataFormatUtil.getDateYMD(endtime);
        } else if (timetype.equals("month")) {
            starttime = DataFormatUtil.getFirstDayOfMonth(starttime);
            starttime = DataFormatUtil.getDayYearTBDate(starttime, i);
            startTime = DataFormatUtil.getDateYMD(starttime);
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            endtime = DataFormatUtil.getDayYearTBDate(endtime, i);
            endTime = DataFormatUtil.getDateYMD(endtime);
        } else {
            return 0;
        }
        int maxAqi = 100;
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = Criteria.where("StationCode").is(dgimn)
                .and("MonitorTime").gte(startTime).lte(endTime).and("AQI").lte(maxAqi);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("StationCode", "AQI", "MonitorTime")

        );
        Aggregation aggregationList = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> dataDoc = pageResults.getMappedResults();
        return dataDoc.size();
    }

    @Override
    public List<Map<String, Object>> getAirStationInfoByParamMap(Map<String, Object> paramMap) {
        return airMonitorStationMapper.getAirStationInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getStationOrCityRankDataListByParam(Map<String, Object> paramMap) throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String datatype = (String) paramMap.get("datatype");
        String timetype = (String) paramMap.get("timetype");
        String monitortime = (String) paramMap.get("monitortime");
        String collection = (String) paramMap.get("collection");
        String mnKey = (String) paramMap.get("mnKey");
        Map<String, Object> idAndName = (Map<String, Object>) paramMap.get("idAndName");
        Date startTime;
        Date endTime;
        Date startTimeThat;
        Date endTimeThat;
        String tbTime;
        if (timetype.equals("day")) {//日
            startTime = DataFormatUtil.getDateYMD(monitortime);
            endTime = DataFormatUtil.getDateYMD(monitortime);
            tbTime = DataFormatUtil.getDayYearTBDate(monitortime, 1);

            startTimeThat = DataFormatUtil.getDateYMD(tbTime);
            endTimeThat = DataFormatUtil.getDateYMD(tbTime);

        } else if (timetype.equals("month")) {//月
            startTime = DataFormatUtil.getDateYMD(DataFormatUtil.getFirstDayOfMonth(monitortime));
            endTime = DataFormatUtil.getDateYMD(DataFormatUtil.getLastDayOfMonth(monitortime));

            tbTime = DataFormatUtil.getMonthTBYearDate(monitortime);
            startTimeThat = DataFormatUtil.getDateYMD(DataFormatUtil.getFirstDayOfMonth(tbTime));
            endTimeThat = DataFormatUtil.getDateYMD(DataFormatUtil.getLastDayOfMonth(tbTime));

        } else {//年
            startTime = DataFormatUtil.getDateYMD(DataFormatUtil.getYearFirst(monitortime));
            endTime = DataFormatUtil.getDateYMD(DataFormatUtil.getYearLast(monitortime));

            tbTime = (Integer.parseInt(monitortime) - 1) + "";
            startTimeThat = DataFormatUtil.getDateYMD(DataFormatUtil.getYearFirst(tbTime));
            endTimeThat = DataFormatUtil.getDateYMD(DataFormatUtil.getYearLast(tbTime));
        }
        List<String> pollutantcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        List<Document> thisDoc = getDayDoc(mnKey, mns, startTime, endTime, pollutantcodes, collection);
        List<Document> thatDoc = getDayDoc(mnKey, mns, startTimeThat, endTimeThat, pollutantcodes, collection);
        Map<String, Map<String, List<Double>>> thisMnCodeAndAvg = getMnAndCodeAndAvg(thisDoc, mnKey);
        Map<String, Map<String, List<Double>>> thatMnCodeAndAvg = getMnAndCodeAndAvg(thatDoc, mnKey);

        Map<String, Integer> thisMnAndYLTS = getMnAndYLTS(thisDoc, mnKey);
        Map<String, Integer> thatMnAndYLTS = getMnAndYLTS(thatDoc, mnKey);

        Map<String, Integer> thisMnAndZWRTS = getMnAndZWRTS(thisDoc, mnKey);
        Map<String, Integer> thatMnAndZWRTS = getMnAndZWRTS(thatDoc, mnKey);
        Map<String, List<String>> mnAndP = getThisPrimaryPollutant(thisDoc, mnKey);
        List<String> pollutants;
        Map<String, Double> thisMnAndZHZS = getMnAndZHZS(monitortime, datatype, timetype, mns, idAndName);
        Map<String, Double> thatMnAndZHZS = getMnAndZHZS(tbTime, datatype, timetype, mns, idAndName);
        Double thisZHZS;
        Double thatZHZS;
        Integer thisYLTS;
        Integer thatYLTS;
        Integer thisZWRTS;
        Integer thatZWRTS;

        Double thisAvg;
        Double thatAvg;
        Map<String, List<Double>> thisCodeAndValues;
        Map<String, List<Double>> thatCodeAndValues;

        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
        String pollutantString;
        Double tb;
        for (String mnIndex : idAndName.keySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("dgimn", mnIndex);
            resultMap.put("monitorpointname", idAndName.get(mnIndex));
            thisZHZS = thisMnAndZHZS.get(mnIndex);
            thatZHZS = thatMnAndZHZS.get(mnIndex);
            resultMap.put("zhzs_value", DataFormatUtil.SaveOneAndSubZero(thisZHZS));
            if (thisZHZS != null && thatZHZS != null && thatZHZS > 0) {
                resultMap.put("zhzs_tb", DataFormatUtil.SaveOneAndSubZero(100d * (thisZHZS - thatZHZS) / thatZHZS));
            } else {
                resultMap.put("zhzs_tb", "-");
            }
            thisYLTS = thisMnAndYLTS.get(mnIndex);
            thatYLTS = thatMnAndYLTS.get(mnIndex);
            resultMap.put("ylts_value", thisYLTS);
            if (thisYLTS != null && thatYLTS != null && thatYLTS > 0) {
                resultMap.put("ylts_tb", DataFormatUtil.SaveOneAndSubZero(100d * (thisYLTS - thatYLTS) / thatYLTS));
            } else {
                resultMap.put("ylts_tb", "-");
            }
            thisZWRTS = thisMnAndZWRTS.get(mnIndex);
            thatZWRTS = thatMnAndZWRTS.get(mnIndex);
            resultMap.put("zwrts_value", thisZWRTS);
            if (thisZWRTS != null && thatZWRTS != null && thatZWRTS > 0) {
                resultMap.put("zwrts_tb", DataFormatUtil.SaveOneAndSubZero(100d * (thisZWRTS - thatZWRTS) / thatZWRTS));
            } else {
                resultMap.put("zwrts_tb", "-");
            }
            thisCodeAndValues = thisMnCodeAndAvg.get(mnIndex) != null ? thisMnCodeAndAvg.get(mnIndex) : new HashMap<>();
            thatCodeAndValues = thatMnCodeAndAvg.get(mnIndex) != null ? thatMnCodeAndAvg.get(mnIndex) : new HashMap<>();
            for (String codeIndex : pollutantcodes) {
                thisAvg = thisCodeAndValues.get(codeIndex) != null ? DataFormatUtil.getListAvgDValue(thisCodeAndValues.get(codeIndex)) : 0;
                thatAvg = thatCodeAndValues.get(codeIndex) != null ? DataFormatUtil.getListAvgDValue(thatCodeAndValues.get(codeIndex)) : 0;

                if ("CO".equals(CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(codeIndex))){
                    resultMap.put(codeIndex + "_value", DataFormatUtil.SaveOneAndSubZero(thisAvg));
                }else {
                    resultMap.put(codeIndex + "_value", DataFormatUtil.formatDoubleSaveNo(thisAvg));
                }

                if (thisAvg > 0 && thatAvg > 0) {
                    tb = 100 * (thisAvg - thatAvg) / thatAvg;
                    resultMap.put(codeIndex + "_tb", DataFormatUtil.SaveOneAndSubZero(tb));
                } else {
                    resultMap.put(codeIndex + "_tb", "-");
                }
            }
            pollutants = mnAndP.get(mnIndex);
            if (pollutants != null) {
                pollutants = pollutants.stream().distinct().collect(Collectors.toList());
                pollutantString = DataFormatUtil.FormatListToString(pollutants, ",");
                resultMap.put("primarypollutant", getPrimaryPollutant(paramMap, pollutantString));
            } else {
                resultMap.put("primarypollutant", "");
            }
            resultList.add(resultMap);
        }
        if (resultList.size() > 0 && paramMap.get("sortmap") != null) {
            Map<String, Object> sortMap = (Map<String, Object>) paramMap.get("sortmap");
            String sortKey = sortMap.get("sortkey").toString();
            String sortType = sortMap.get("sorttype").toString();
            if ("desc".equals(sortType)) {
                resultList = resultList.stream().sorted(
                        Comparator.comparing(m -> comparingDoubleByKey(m, sortKey))
                ).collect(Collectors.toList());
            } else {
                resultList = resultList.stream().sorted(
                        Comparator.comparing(m -> comparingDoubleByKey((Map<String, Object>) m, sortKey)).reversed()
                ).collect(Collectors.toList());
            }
        }
        return resultList;
    }

    private Map<String, List<String>> getThisPrimaryPollutant(List<Document> thisDoc, String mnKey) {
        Map<String, List<String>> mnAndP = new HashMap<>();
        List<String> pollutants;
        List<String> sub;
        String mnCommon;
        String PrimaryPollutant;
        for (Document document : thisDoc) {
            if (document.get("PrimaryPollutant") != null) {
                mnCommon = document.getString(mnKey);
                PrimaryPollutant = document.getString("PrimaryPollutant");
                pollutants = mnAndP.get(mnCommon) != null ? mnAndP.get(mnCommon) : new ArrayList<>();
                sub = Arrays.asList(PrimaryPollutant.split(","));
                pollutants.addAll(sub);
                mnAndP.put(mnCommon, pollutants);
            }
        }
        return mnAndP;
    }

    /**
     * @Description:
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/19 15:47
     */
    private Map<String, Double> getMnAndZHZS(String monitortime, String datatype, String timetype,
                                             List<String> mns, Map<String, Object> idAndName) throws Exception {
        Map<String, Double> MnAndZHZS = new HashMap<>();
        if (timetype.equals("month")) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", monitortime);
            paramMap.put("endtime", monitortime);
            paramMap.put("mns", mns);
            paramMap.put("mnandname", idAndName);
            String dgimn;
            List<Map<String, Object>> dataList;
            if (datatype.equals("city")) {//城市
                dataList = getManyMonthManyCityMonthCompositeIndex(paramMap);
            } else {//园区站点
                dataList = getManyMonthManyStationMonthCompositeIndex(paramMap);
            }
            List<Map<String, Object>> valuedata;
            Map<String, Object> valueMap;
            Double value;
            for (Map<String, Object> dataMap : dataList) {
                dgimn = dataMap.get("dgimn").toString();
                valuedata = (List<Map<String, Object>>) dataMap.get("valuedata");
                if (valuedata.size() > 0) {
                    valueMap = valuedata.get(0);
                    value = Double.parseDouble(valueMap.get("value").toString());
                    MnAndZHZS.put(dgimn, value);
                }
            }
        }else if(timetype.equals("year")) {//月综合指数均值
            Map<String,List<Double>> mnAndValues = new HashMap<>();
            List<Double> values;
            Map<String, Object> paramMap = new HashMap<>();
            String starttime =  monitortime+"-01";
            String endtime = monitortime+"-12";

            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mns", mns);
            paramMap.put("mnandname", idAndName);
            String dgimn;
            List<Map<String, Object>> dataList;
            if (datatype.equals("city")) {//城市
                dataList = getManyMonthManyCityMonthCompositeIndex(paramMap);
            } else {//园区站点
                dataList = getManyMonthManyStationMonthCompositeIndex(paramMap);
            }
            List<Map<String, Object>> valuedata;

            Double value;
            for (Map<String, Object> dataMap : dataList) {
                dgimn = dataMap.get("dgimn").toString();
                valuedata = (List<Map<String, Object>>) dataMap.get("valuedata");
                if (valuedata.size() > 0) {
                    for (Map<String,Object> valueMap:valuedata){
                        value = Double.parseDouble(valueMap.get("value").toString());

                        values = mnAndValues.get(dgimn)!=null?mnAndValues.get(dgimn):new ArrayList<>();
                        values.add(value);
                        mnAndValues.put(dgimn,values);
                    }
                }
            }
            for (String mnIndex:mnAndValues.keySet()){
                MnAndZHZS.put(mnIndex,DataFormatUtil.getListAvgDValue(mnAndValues.get(mnIndex)));
            }
        }
        return MnAndZHZS;
    }

    private Map<String, Integer> getMnAndZWRTS(List<Document> thisDoc, String mnKey) {

        Map<String, Integer> mnAndZWRTS = new HashMap<>();
        Map<String, Date> mnAndTime = new HashMap<>();
        String mnCommon;
        int minAqi = 201;
        Date time;
        for (Document document : thisDoc) {
            if (document.get("AQI") != null && document.getInteger("AQI") >= minAqi) {
                mnCommon = document.getString(mnKey);

                time = document.getDate("MonitorTime");
                if (mnAndTime.get(mnCommon) == null) {
                    mnAndZWRTS.put(mnCommon, mnAndZWRTS.get(mnCommon) != null ? mnAndZWRTS.get(mnCommon) + 1 : 1);
                    mnAndTime.put(mnCommon, time);
                } else if (!time.equals(mnAndTime.get(mnCommon))) {
                    mnAndZWRTS.put(mnCommon, mnAndZWRTS.get(mnCommon) != null ? mnAndZWRTS.get(mnCommon) + 1 : 1);
                    mnAndTime.put(mnCommon, time);
                }

                mnAndZWRTS.put(mnCommon, mnAndZWRTS.get(mnCommon) != null ? mnAndZWRTS.get(mnCommon) + 1 : 1);
            }
        }
        return mnAndZWRTS;
    }

    private Map<String, Integer> getMnAndYLTS(List<Document> thisDoc, String mnKey) {
        Map<String, Integer> mnAndYLTS = new HashMap<>();
        Map<String, Date> mnAndTime = new HashMap<>();
        String mnCommon;
        int maxAqi = 100;
        Date time;
        for (Document document : thisDoc) {
            if (document.get("AQI") != null && document.getInteger("AQI") <= maxAqi) {
                mnCommon = document.getString(mnKey);
                time = document.getDate("MonitorTime");
                if (mnAndTime.get(mnCommon) == null) {
                    mnAndYLTS.put(mnCommon, mnAndYLTS.get(mnCommon) != null ? mnAndYLTS.get(mnCommon) + 1 : 1);
                    mnAndTime.put(mnCommon, time);
                } else if (!time.equals(mnAndTime.get(mnCommon))) {
                    mnAndYLTS.put(mnCommon, mnAndYLTS.get(mnCommon) != null ? mnAndYLTS.get(mnCommon) + 1 : 1);
                    mnAndTime.put(mnCommon, time);
                }
            }
        }
        return mnAndYLTS;
    }

    private Map<String, Map<String, List<Double>>> getMnAndCodeAndAvg(List<Document> thisDoc, String mnKey) {
        Map<String, Map<String, List<Double>>> mnAndCodeAndValues = new HashMap<>();
        Map<String, List<Double>> codeAndValues;
        List<Double> values;
        String mnCommon;
        String pollutantCode;
        Double value;
        for (Document document : thisDoc) {
            if (document.get("Strength") != null) {
                value = Double.parseDouble(document.getString("Strength"));
                mnCommon = document.getString(mnKey);
                codeAndValues = mnAndCodeAndValues.get(mnCommon) != null ? mnAndCodeAndValues.get(mnCommon) : new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                values = codeAndValues.get(pollutantCode) != null ? codeAndValues.get(pollutantCode) : new ArrayList<>();
                values.add(value);
                codeAndValues.put(pollutantCode, values);
                mnAndCodeAndValues.put(mnCommon, codeAndValues);
            }
        }

        return mnAndCodeAndValues;

    }

    private List<Document> getDayDoc(String mnKey, List<String> mns, Date startTime, Date endTime,
                                     List<String> pollutantcodes, String collection) {
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = Criteria.where(mnKey).in(mns)
                .and("MonitorTime").gte(startTime).lte(endTime);
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("DataList"));
        Criteria criteria1 = Criteria.where("DataList.PollutantCode").in(pollutantcodes);
        operations.add(Aggregation.match(criteria1));
        operations.add(Aggregation.project(mnKey, "AQI", "MonitorTime", "PrimaryPollutant")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("Strength")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        return documents;
    }

    private String getTimes(String timetype, int i, String starttime, String endtime) {
        if (timetype.equals("hour")) {
            starttime = DataFormatUtil.getHourYearTBDate(starttime, i);
            endtime = DataFormatUtil.getHourYearTBDate(endtime, i);
        } else if (timetype.equals("day")) {
            starttime = DataFormatUtil.getDayYearTBDate(starttime, i);
            endtime = DataFormatUtil.getDayYearTBDate(endtime, i);
        } else {
            starttime = DataFormatUtil.getMonthYearTBDate(starttime, i);
            endtime = DataFormatUtil.getMonthYearTBDate(endtime, i);
        }
        return starttime + " - " + endtime;
    }

    private Map<String, Double> getCodeAndValue(String timetype, int i, String starttime, String endtime, String dgimn, String collection, List<String> pollutantcodes) {
        Date startTime;
        Date endTime;
        if (timetype.equals("hour")) {
            starttime = DataFormatUtil.getHourYearTBDate(starttime, i);
            startTime = DataFormatUtil.getDateYMDH(starttime);
            endtime = DataFormatUtil.getHourYearTBDate(endtime, i);
            endTime = DataFormatUtil.getDateYMDH(endtime);
        } else if (timetype.equals("day")) {
            starttime = DataFormatUtil.getDayYearTBDate(starttime, i);
            startTime = DataFormatUtil.getDateYMD(starttime);
            endtime = DataFormatUtil.getDayYearTBDate(endtime, i);
            endTime = DataFormatUtil.getDateYMD(endtime);
        } else {
            starttime = DataFormatUtil.getFirstDayOfMonth(starttime);

            starttime = DataFormatUtil.getDayYearTBDate(starttime, i);
            startTime = DataFormatUtil.getDateYMD(starttime);


            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            endtime = DataFormatUtil.getDayYearTBDate(endtime, i);
            endTime = DataFormatUtil.getDateYMD(endtime);
        }
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = Criteria.where("StationCode").is(dgimn)
                .and("MonitorTime").gte(startTime).lte(endTime);
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("DataList"));
        Criteria criteria1 = Criteria.where("DataList.PollutantCode").in(pollutantcodes);
        operations.add(Aggregation.match(criteria1));
        operations.add(Aggregation.project("StationCode", "AQI", "MonitorTime")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("Strength")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> dataDoc = pageResults.getMappedResults();
        Map<String, Double> codeAndAvg = new HashMap<>();
        if (dataDoc.size() > 0) {
            Map<String, List<Double>> codeAndValues = new HashMap<>();
            List<Double> values;
            String pollutantCode;
            Double value;
            for (Document document : dataDoc) {
                if (document.get("Strength") != null) {
                    value = Double.parseDouble(document.getString("Strength"));
                    pollutantCode = document.getString("PollutantCode");
                    values = codeAndValues.get(pollutantCode) != null ? codeAndValues.get(pollutantCode) : new ArrayList<>();
                    values.add(value);
                    codeAndValues.put(pollutantCode, values);
                }
            }
            for (String code : codeAndValues.keySet()) {
                codeAndAvg.put(code, DataFormatUtil.getListAvgDValue(codeAndValues.get(code)));
            }
        }
        return codeAndAvg;

    }

    private static Double comparingDoubleByKey(Map<String, Object> map, String key) {
        Object value;
        if (map.get(key) instanceof Map) {
            value = ((Map) map.get(key)).get("value");
        } else {
            value = map.get(key);
        }
        return value != null && !"-".equals(value) ? Double.parseDouble(value.toString()) : -1;
    }

    private String getPrimaryPollutant(Map<String, Object> paramMap, String primaryPollutant) {
        paramMap.put("pollutanttype", paramMap.get("monitorpointtype"));
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : pollutants) {
            paramMap.put(map.get("code").toString(), map.get("name"));
        }
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
