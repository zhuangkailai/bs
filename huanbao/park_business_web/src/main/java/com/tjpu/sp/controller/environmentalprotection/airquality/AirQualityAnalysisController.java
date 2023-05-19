package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.model.common.mongodb.StationDayAQIDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirCityMonthDataService;
import com.tjpu.sp.service.environmentalprotection.envquality.AirParkMonthDataService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirStationPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RequestMapping("airQualityAnalysis")
@RestController
public class AirQualityAnalysisController {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoBaseService mongoBaseService;

    private final String DB_StationDayAQIData = "StationDayAQIData";
    private final String DB_CityDayAQIData = "CityDayAQIData";
    private final String DB_ParkDayAQIData = "ParkDayAQIData";
    @Autowired
    private AirCityMonthDataService airCityMonthDataService;
    @Autowired
    private OnlineService onlineService;


    private final PollutantService pollutantService;
    private final AirStationPollutantSetService airStationPollutantSetService;
    private final AirParkMonthDataService airParkMonthDataService;
    private final AirMonitorStationService airMonitorStationService;

    public AirQualityAnalysisController(PollutantService pollutantService, AirStationPollutantSetService airStationPollutantSetService, AirParkMonthDataService airParkMonthDataService, AirMonitorStationService airMonitorStationService) {
        this.pollutantService = pollutantService;
        this.airStationPollutantSetService = airStationPollutantSetService;
        this.airParkMonthDataService = airParkMonthDataService;
        this.airMonitorStationService = airMonitorStationService;
    }

    /**
     * @author: lip
     * @date: 2019/6/3 14:56
     * @Description: 统计一年内每天的空气AQI质量然后按月份分组
     * @updateUser: xsm
     * @updateDate: 2021/1/11 11:11
     * @updateDescription: 统计每个月各个空气质量级别的天数
     * @param:
     * @return:
     */
    @RequestMapping(value = "countDailyAirAQIForMonthByYear", method = RequestMethod.POST)
    public Object countDailyAirAQIForMonthByYear(
            @RequestJson(value = "year", required = false) Integer year,
            @RequestJson(value = "month", required = false) String month,
            @RequestJson(value = "dgimn", required = false) String dgimn
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            String startTime;
            String endTime;
            if (StringUtils.isNotBlank(month)) {//单个月
                startTime = DataFormatUtil.getFirstDayOfMonth(month);
                endTime = DataFormatUtil.getLastDayOfMonth(month);
            } else {//年
                startTime = DataFormatUtil.getYearFirst(year);
                LocalDateTime localDateTime = LocalDateTime.now();
                int nowYear = localDateTime.getYear();
                if (year == nowYear) {
                    endTime =DataFormatUtil.getDateYMD(new Date());
                } else {
                    endTime = DataFormatUtil.getYearLast(year);
                }
            }
            Date startDate = DataFormatUtil.getDateYMD(startTime);
            Date endDate = DataFormatUtil.getDateYMD(endTime);
            Fields fields;
            String collection;
            Aggregation aggregation;
            if (StringUtils.isNotBlank(dgimn)) {
                collection = DB_StationDayAQIData;
                fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "StationCode", "AQI", "_id", "DataList");
                aggregation = newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("StationCode").is(dgimn)),
                        sort(Sort.Direction.ASC, "MonitorTime")
                );
            } else {
                collection = DB_ParkDayAQIData;
                fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id", "DataList");
                aggregation = newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)),
                        sort(Sort.Direction.ASC, "MonitorTime")
                );
            }


            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            if (documents.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("pollutanttype", 5);
                List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                paramMap.clear();
                for (Map<String, Object> map : pollutants) {
                    paramMap.put(map.get("code").toString(), map.get("name"));
                }
                Map<String, List<Map<String, Object>>> tempMap = new LinkedHashMap<>();
                String tempYM = "";
                List<Document> pollutantList;
                for (Document document : documents) {
                    tempYM = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    if (tempMap.get(tempYM) != null) {
                        List<Map<String, Object>> ymdList = tempMap.get(tempYM);
                        Map<String, Object> ymdMap = new HashMap<>();
                        ymdMap.put("time", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                        ymdMap.put("aqi", document.get("AQI"));
                        ymdMap.put("aq", document.get("AirQuality"));
                        pollutantList = document.get("DataList", List.class);
                        for (Document pollutant : pollutantList) {
                            pollutant.put("pollutantname", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(pollutant.getString("PollutantCode")));
                            pollutant.put("pollutantunit", CommonTypeEnum.AirCommonSixIndexEnum.getCodeByString(pollutant.getString("PollutantCode")).getUnit());
                        }
                        ymdMap.put("datalist", pollutantList);
                        ymdMap.put("keyname", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                        ymdList.add(ymdMap);
                    } else {
                        List<Map<String, Object>> ymdList = new ArrayList<>();
                        Map<String, Object> ymdMap = new HashMap<>();
                        ymdMap.put("time", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                        ymdMap.put("aqi", document.get("AQI"));
                        ymdMap.put("aq", document.get("AirQuality"));
                        pollutantList = document.get("DataList", List.class);
                        for (Document pollutant : pollutantList) {
                            pollutant.put("pollutantname", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(pollutant.getString("PollutantCode")));
                            pollutant.put("pollutantunit", CommonTypeEnum.AirCommonSixIndexEnum.getCodeByString(pollutant.getString("PollutantCode")).getUnit());
                        }
                        ymdMap.put("datalist", pollutantList);
                        ymdMap.put("keyname", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                        ymdList.add(ymdMap);
                        tempMap.put(tempYM, ymdList);
                    }
                }
                for (String key : tempMap.keySet()) {
                    Map<String, Object> map = new HashMap<>();
                    List<Map<String, Object>> ymdList = tempMap.get(key);
                    Map<String, Integer> levelandnum = new HashMap<>();
                    List<Map<String, Object>> levelList = new ArrayList<>();
                    if (ymdList != null && ymdList.size() > 0) {
                        for (Map<String, Object> obj : ymdList) {
                            if (obj.get("aq") != null) {
                                String aqlevel = obj.get("aq").toString();
                                if (levelandnum.get(aqlevel) != null) {
                                    levelandnum.put(aqlevel, levelandnum.get(aqlevel) + 1);
                                } else {
                                    levelandnum.put(aqlevel, 1);
                                }
                            }
                        }
                    }
                    if (levelandnum != null && levelandnum.size() > 0) {
                        for (String keytwo : levelandnum.keySet()) {
                            Map<String, Object> objmap = new HashMap<>();
                            objmap.put("airquality", keytwo);
                            objmap.put("daynum", levelandnum.get(keytwo));
                            levelList.add(objmap);
                        }
                    }
                    map.put("label", DataFormatUtil.getLabelByDate(key.split("-")[1]));
                    map.put("airdata", tempMap.get(key));
                    map.put("airqualitydata", levelList);
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
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 统计一段时间内园区优良天数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAirCityExcellentDayNumByMonitorTimes", method = RequestMethod.POST)
    public Object countAirCityExcellentDayNumByMonitorTimes(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "type", required = false) String type,
            @RequestJson(value = "endtime") String endtime) {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            List<String> yms = DataFormatUtil.getMonthBetween(starttime, endtime);
            starttime = starttime + "-01";
            endtime = DataFormatUtil.getLastDayOfMonth(endtime);
            Date startDate = DataFormatUtil.getDateYMD(starttime);
            Date endDate = DataFormatUtil.getDateYMD(endtime);
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startDate).lte(endDate))
            );
            AggregationResults<Document> cityDayAQIData;
            if ("city".equals(type)) {
                cityDayAQIData = mongoTemplate.aggregate(aggregation, "CityDayAQIData", Document.class);
            } else {
                cityDayAQIData = mongoTemplate.aggregate(aggregation, "ParkDayAQIData", Document.class);
            }

            List<Document> documents = cityDayAQIData.getMappedResults();
            List<String> excellentCodes = new ArrayList<>();
            excellentCodes.add("优");
            excellentCodes.add("良好");
            excellentCodes.add("良");
            Map<String, Integer> monitorTimeAndExcellentDayNum = new HashMap<>();
            if (documents.size() > 0) {
                String tempYM = "";
                for (Document document : documents) {
                    if (excellentCodes.contains(document.get("AirQuality"))) {
                        tempYM = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                        if (monitorTimeAndExcellentDayNum.get(tempYM) != null) {
                            monitorTimeAndExcellentDayNum.put(tempYM, monitorTimeAndExcellentDayNum.get(tempYM) + 1);
                        } else {
                            monitorTimeAndExcellentDayNum.put(tempYM, 1);
                        }
                    }
                }
            }
            for (String ym : yms) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", ym);
                map.put("daynum", monitorTimeAndExcellentDayNum.get(ym) != null ? monitorTimeAndExcellentDayNum.get(ym) : 0);
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
     * @Description: 2.5    获取城市空气质量配置的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getCityAirPollutantSetInfo", method = RequestMethod.POST)
    public Object getCityAirPollutantSetInfo() {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> aqi = new HashMap<>();
            aqi.put("pollutantcode", "aqi");
            aqi.put("pollutantname", "AQI");
            aqi.put("pollutantunit", "");
            dataList.add(aqi);
            Map<String, Object> compositeinde = new HashMap<>();
            compositeinde.put("pollutantcode", "compositeindex");
            compositeinde.put("pollutantname", "综合指数");
            compositeinde.put("pollutantunit", "");
            dataList.add(compositeinde);
            List<Map<String, Object>> pollutantList = airStationPollutantSetService.getCityAirPollutantSetInfo();
            dataList.addAll(pollutantList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }


    /**
     * @author: xsm
     * @date: 2019/6/6 0004 上午 10:36
     * @Description: 根据监测时间统计某个月园区空气质量各污染物的同比环比信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAirCityMonthContrastInfoByMonitorTime", method = RequestMethod.POST)
    public Object countAirCityMonthContrastInfoByMonitorTime(@RequestJson(value = "monitortime") String monitortime) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM");
            //当前月各因子平均浓度
            Map<String, Double> currentMonth = airParkMonthDataService.getAirCityConcentrationByMonitorTimes(monitortime);
            //当前月各因子综合指数
            Map<String, Map<String, Object>> compositeindex = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(monitortime, monitortime);
            //获取上一个时间
            Date currdate = sd.parse(monitortime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currdate);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
            String lastmon = sd.format(calendar.getTime());
            //环比--上个月各因子平均浓度
            Map<String, Double> lastmoncurrent = airParkMonthDataService.getAirCityConcentrationByMonitorTimes(lastmon);
            //环比--上个月各因子综合指数
            Map<String, Map<String, Object>> lastmoncompositeindex = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(lastmon, lastmon);
            YearMonth yearMonth = YearMonth.parse(monitortime);
            String yearmon = yearMonth.minusYears(1).toString();
            //同比--去年同期各因子平均浓度
            Map<String, Double> lastyearcurrent = airParkMonthDataService.getAirCityConcentrationByMonitorTimes(yearmon);
            //同比--去年同期各因子综合指数
            Map<String, Map<String, Object>> lastyearcompositeindex = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(yearmon, yearmon);
            //遍历拼接数据
            for (String key : currentMonth.keySet()) {
                Double concentration = currentMonth.get(key);//当月因子监测浓度
                Double composite = null;
                if (compositeindex != null && compositeindex.size() > 0) {
                    composite = (Double) (compositeindex.get(monitortime)).get(key);//当月因子综合指数
                }
                Double lastmonconcen = lastmoncurrent.get(key);//上个月因子监测浓度
                Double lastmoncomposite = null;
                if (lastmoncompositeindex != null && lastmoncompositeindex.size() > 0) {
                    lastmoncomposite = (Double) (lastmoncompositeindex.get(lastmon)).get(key);//上个月因子综合指数
                }
                Double yearonyearconcen = lastyearcurrent.get(key);//去年同期因子浓度
                Double yearonyearcomposite = null;
                if (lastyearcompositeindex != null && lastyearcompositeindex.size() > 0) {
                    yearonyearcomposite = (Double) (lastyearcompositeindex.get(yearmon)).get(key);//去年同期因子综合指数
                }
                String lastmonconcenchange = countChange(concentration, lastmonconcen);//上个月因子浓度变化幅度
                String lastmoncompositechange = countChange(composite, lastmoncomposite); //上个月因子综合指数变化幅度
                String yearonyearconcenchange = countChange(concentration, yearonyearconcen); //去年同期因子浓度变化幅度
                String yearonyearcompositechange = countChange(composite, yearonyearcomposite); //去年同期因子综合指数变化幅度
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("pollutantname", key);
                resultmap.put("concentration", concentration);
                resultmap.put("composite", composite == 0 ? "-" : composite);
                resultmap.put("lastmonconcen", lastmonconcen);
                resultmap.put("lastmonconcenchange", lastmonconcenchange);
                resultmap.put("lastmoncomposite", lastmoncomposite == 0 ? "-" : lastmoncomposite);
                resultmap.put("lastmoncompositechange", lastmoncompositechange);
                resultmap.put("yearonyearconcen", yearonyearconcen);
                resultmap.put("yearonyearconcenchange", yearonyearconcenchange);
                resultmap.put("yearonyearcomposite", yearonyearcomposite == 0 ? "-" : yearonyearcomposite);
                resultmap.put("yearonyearcompositechange", yearonyearcompositechange);
                dataList.add(resultmap);
            }
            result.put("datalist", dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }

    /**
     * @author: xsm
     * @date: 2019/6/6 0004 下午 5:07
     * @Description: 根据监测时间统计某个月所有空气站点综合指数的月排名信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAirStationCompositeMonthRankByMonitorTime", method = RequestMethod.POST)
    public Object countAirStationCompositeMonthRankByMonitorTime(@RequestJson(value = "monitortime") String monitortime) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM");
            //当前月各站点月综合指数
            Map<String, Map<String, Object>> stationAndValue = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(monitortime);
            //当前月各站点的月首要污染物
            Map<String, String> primarypollutants = airMonitorStationService.getOneMonthManyStationMonthPrimarypollutant(monitortime);
            //获取上一个时间
            Date currdate = sd.parse(monitortime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currdate);
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
            String lastmon = sd.format(calendar.getTime());
            //环比--上个月各站点月综合指数
            Map<String, Map<String, Object>> stationAndValue2 = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(lastmon);
            YearMonth yearMonth = YearMonth.parse(monitortime);
            String yearmon = yearMonth.minusYears(1).toString();
            //同比--去年同期各站点月综合指数
            Map<String, Map<String, Object>> stationAndValue3 = airMonitorStationService.getOneMonthManyStationMonthCompositeIndex(yearmon);
            List<Map<String, Object>> airMonitorStation = airMonitorStationService.getAllAirMonitorStation(new HashMap<>());
            //遍历拼接数据
            for (Map<String, Object> obj : airMonitorStation) {//遍历站点
                Double composite = null;//综合指数
                Double maxcomposite = null;
                if (stationAndValue != null && stationAndValue.size() > 0) {
                    if (obj.get("DGIMN") != null) {
                        Map<String, Object> map = stationAndValue.get(obj.get("DGIMN"));
                        if (map != null) {
                            composite = Double.parseDouble(map.get("total") != null ? map.get("total").toString() : null);
                            maxcomposite = getMaxCompositeIndex(map);
                        }
                    }
                }
                Double lastmoncomposite = null;
                if (stationAndValue2 != null && stationAndValue2.size() > 0) {
                    if (stationAndValue2.get(obj.get("DGIMN")) != null) {
                        lastmoncomposite = stationAndValue2.get(obj.get("DGIMN")).get("total") != null ? Double.parseDouble(stationAndValue2.get(obj.get("DGIMN")).get("total").toString()) : null;//上个月站点综合指数
                    }
                }
                Double yearonyearcomposite = null;
                if (stationAndValue3 != null && stationAndValue3.size() > 0) {
                    if (stationAndValue3.get(obj.get("DGIMN")) != null) {
                        yearonyearcomposite = stationAndValue3.get(obj.get("DGIMN")).get("total") != null ? Double.parseDouble(stationAndValue3.get(obj.get("DGIMN")).get("total").toString()) : null;//去年同期站点综合指数
                    }
                }
                String primarypollutant = null;
                if (primarypollutants != null && primarypollutants.size() > 0) {
                    primarypollutant = primarypollutants.get(obj.get("DGIMN"));//去年同期站点综合指数
                }
                String lastmoncompositechange = countChange(composite, lastmoncomposite); //上个月因子综合指数变化幅度
                String yearonyearcompositechange = countChange(composite, yearonyearcomposite); //去年同期因子综合指数变化幅度
                if (composite == null && lastmoncomposite == null && "".equals(lastmoncompositechange) && yearonyearcomposite == null && "".equals(yearonyearcompositechange) && maxcomposite == null && primarypollutant == null) {
                } else {
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("monitorpointname", obj.get("MonitorPointName"));
                    resultmap.put("composite", (composite == null || composite == 0) ? "-" : composite);
                    resultmap.put("lastmoncomposite", (lastmoncomposite == null || lastmoncomposite == 0) ? "-" : lastmoncomposite);
                    resultmap.put("lastmoncompositechange", lastmoncompositechange);
                    resultmap.put("yearonyearcomposite", (yearonyearcomposite == null || yearonyearcomposite == 0) ? "-" : yearonyearcomposite);
                    resultmap.put("yearonyearcompositechange", yearonyearcompositechange);
                    resultmap.put("maxcomposite", (maxcomposite == null || maxcomposite == 0) ? "-" : maxcomposite);
                    resultmap.put("primarypollutant", (primarypollutant == null || primarypollutant == "") ? "-" : primarypollutant);
                    dataList.add(resultmap);
                }
            }
            List<Map<String, Object>> collect = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("composite").toString()).reversed()).collect(Collectors.toList());
            result.put("datalist", collect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    //根据两个值计算变化幅度
    private String countChange(Double num1, Double num2) {
        String resultvalue = "";
        if (num2 != null && num2 > 0) {
            if (num1 != null) {
                Double result = (num1 - num2) / num2;
                result = result * 100;
                if (result > 0) {
                    resultvalue = "↑" + DataFormatUtil.formatDoubleSaveTwo(result) + "%";
                } else if (result < 0) {
                    resultvalue = "↓" + (DataFormatUtil.formatDoubleSaveTwo(result)).substring(1, (DataFormatUtil.formatDoubleSaveTwo(result)).length()) + "%";
                } else {
                    resultvalue = "-";
                }
            } else {
                resultvalue = "-";
            }
        } else {//上个月或去年没有数据 而今年有变化 则变化率为100%
            if (num1 != null) {
                resultvalue = "-";
            } else {//否则为空
                resultvalue = "";
            }
        }
        return resultvalue;
    }

    //比较得出最大综合指数
    private Double getMaxCompositeIndex(Map<String, Object> map) {
        Double resultvalue = 0d;
        for (String key : map.keySet()) {
            if (!"total".equals(key)) {//当key不为总综合指数key时
                Double num = map.get(key) != null ? Double.parseDouble(map.get(key).toString()) : 0;
                if (num > resultvalue) {
                    resultvalue = num;
                }
            }
        }
        return resultvalue;
    }

    /**
     * @author: xsm
     * @date: 2019/6/10 0010 上午 08:56
     * @Description: 通过监测时间导出空气质量所有站点的综合指数月排名信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "exportAirStationCompositeMonthRankByMonitortime", method = RequestMethod.POST)
    public void exportAirStationCompositeMonthRankByMonitortime(@RequestJson(value = "monitortime", required = false) String monitortime,
                                                                HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(countAirStationCompositeMonthRankByMonitorTime(monitortime));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object data1 = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);
            HSSFWorkbook wb = airMonitorStationService.getCityAirCompositeByMonitortime(jsonArray);
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(wb);
            ExcelUtil.downLoadExcel("综合指数月排行", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/28 0028 下午 4:55
     * @Description: 统计一段时间内园区内和园区外优良天数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countAirCityAndParkExcellentDayNumByMonitorTimes", method = RequestMethod.POST)
    public Object countAirCityAndParkExcellentDayNumByMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                                   @RequestJson(value = "endtime") String endtime) throws Exception {
        try {

            String parkdata = JSONObject.fromObject(countAirCityExcellentDayNumByMonitorTimes(starttime, "", endtime)).getString("data");
            String citydata = JSONObject.fromObject(countAirCityExcellentDayNumByMonitorTimes(starttime, "city", endtime)).getString("data");

            Map<String, Object> resultMap = new HashMap<>();

            resultMap.put("citydata", citydata);
            resultMap.put("parkdata", parkdata);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 上午 11:17
     * @Description: 获取一段时间内园区和城市月的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getAirCityAndParkCompositeIndexByMonitorTimes", method = RequestMethod.POST)
    public Object getAirCityAndParkCompositeIndexByMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "endtime") String endtime) throws Exception {
        try {

            Map<String, Map<String, Object>> parkdata = airParkMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(starttime, endtime);
            Map<String, Map<String, Object>> citydata = airCityMonthDataService.getAirCityMonthCompositeIndexByMonitorTimes(starttime, endtime);


            List<Map<String, Object>> city = new ArrayList<>();
            for (String data : citydata.keySet()) {
                Map<String, Object> map1 = new HashMap<>();
                Map<String, Object> map = citydata.get(data);
                Object total = map.get("total");
                map1.put("monitortime", data);
                map1.put("total", total);
                city.add(map1);
            }
            List<Map<String, Object>> park = new ArrayList<>();
            for (String data : parkdata.keySet()) {
                Map<String, Object> map1 = new HashMap<>();
                Map<String, Object> map = parkdata.get(data);
                Object total = map.get("total");
                map1.put("monitortime", data);
                map1.put("total", total);
                park.add(map1);
            }


            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("citydata", city);
            resultMap.put("parkdata", park);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 下午 1:05
     * @Description: 统计一段时间内园区内外首要污染物占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countAirCityAndParkDayPrimaryPollutantByMonitorTime", method = RequestMethod.POST)
    public Object countAirCityAndParkDayPrimaryPollutantByMonitorTime(@RequestJson(value = "starttime") String starttime,
                                                                      @RequestJson(value = "endtime") String endtime) throws Exception {
        try {


            Object cityDayAQIData = countAirDayPrimaryPollutantByMonitorTime(starttime, endtime, "CityDayAQIData");
            Object parkDayAQIData = countAirDayPrimaryPollutantByMonitorTime(starttime, endtime, "ParkDayAQIData");
            Map<String, Object> resultdata = new HashMap<>();

            resultdata.put("citydata", cityDayAQIData);
            resultdata.put("parkdata", parkDayAQIData);

            return AuthUtil.parseJsonKeyToLower("success", resultdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/28 0028 下午 4:55
     * @Description: 统计一段时间内空气站优良天数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countAirStationExcellentDayNumByMonitorTimes", method = RequestMethod.POST)
    public Object countAirStationExcellentDayNumByMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                               @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            StationDayAQIDataVO stationDayAQIDataVO = new StationDayAQIDataVO();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            starttime = starttime + "-01 00:00:00";
            Date parse = format.parse(endtime);
            Calendar instance = Calendar.getInstance();
            instance.setTime(parse);
            instance.add(Calendar.MONTH, 1);
            instance.add(Calendar.SECOND, -1);
            Date time = instance.getTime();
            endtime = format1.format(time);

            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            stationDayAQIDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());


            List<Map<String, Object>> allAirMonitorStation = airMonitorStationService.getAllAirMonitorStation(new HashMap<>());
            String dgimns = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            stationDayAQIDataVO.setStationCode(dgimns);

            List<StationDayAQIDataVO> parkDayAQIData = mongoBaseService.getListByParam(stationDayAQIDataVO, "StationDayAQIData", "yyyy-MM-dd HH:mm:ss");
            Map<String, List<StationDayAQIDataVO>> collect = parkDayAQIData.stream().peek(m -> m.setMonitorTime(OverAlarmController.format(m.getMonitorTime(), "yyyy-MM"))).collect(Collectors.groupingBy(m -> m.getStationCode()));


            List<Map<String, Object>> resultlist = new ArrayList<>();
            for (String s : collect.keySet()) {
                Map<String, Object> monitordata = new HashMap<>();
                List<Map<String, Object>> datas = new ArrayList<>();
                List<Map<String, Object>> collect2 = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null && s.equals(m.get("DGIMN").toString())).collect(Collectors.toList());
                if (collect2.size() > 0) {
                    monitordata.put("monitorpointname", collect2.get(0).get("MonitorPointName"));
                    monitordata.put("datalist", datas);
                    List<StationDayAQIDataVO> stationDayAQIDataVOS = collect.get(s);
                    Map<String, Long> collect1 = stationDayAQIDataVOS.stream().filter(m -> "优".equals(m.getAirQuality()) || "良".equals(m.getAirQuality()) || "良好".equals(m.getAirQuality())).collect(Collectors.groupingBy(m -> m.getMonitorTime(), Collectors.counting()));
                    for (String s1 : collect1.keySet()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("monitortime", s1);
                        data.put("daynum", collect1.get(s1));
                        datas.add(data);
                    }
                    Collections.sort(datas, (o1, o2) -> {
                        if (o1.get("monitortime") != null && o2.get("monitortime") != null) {
                            return o1.get("monitortime").toString().compareTo(o2.get("monitortime").toString());
                        }
                        return 0;
                    });
                    resultlist.add(monitordata);
                }

            }
            return AuthUtil.parseJsonKeyToLower("success", resultlist);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/28 0028 下午 4:55
     * @Description: 通过监测时间分页信息站点日监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getAirStationDayDataByParams", method = RequestMethod.POST)
    public Object getAirStationDayDataByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "pagenum") Integer pagenum,
                                               @RequestJson(value = "endtime") String endtime, @RequestJson(value = "pagesize") Integer pagesize) throws Exception {
        try {
            StationDayAQIDataVO stationDayAQIDataVO = new StationDayAQIDataVO();
            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            mongoSearchEntity.setPage(pagenum);
            mongoSearchEntity.setSize(pagesize);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            starttime = starttime + "-01 00:00:00";
            Date parse = format.parse(endtime);
            Calendar instance = Calendar.getInstance();
            instance.setTime(parse);
            instance.add(Calendar.MONTH, 1);
            instance.add(Calendar.SECOND, -1);
            Date time = instance.getTime();
            endtime = format1.format(time);

            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            stationDayAQIDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());


            List<Map<String, Object>> allAirMonitorStation = airMonitorStationService.getAllAirMonitorStation(new HashMap<>());
            String dgimns = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            stationDayAQIDataVO.setStationCode(dgimns);


            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", 5);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);

            List<StationDayAQIDataVO> parkDayAQIData = mongoBaseService.getListWithPageByParam(stationDayAQIDataVO, mongoSearchEntity, "StationDayAQIData", "yyyy-MM-dd HH:mm:ss");
            long total = mongoBaseService.getCount(stationDayAQIDataVO, "StationDayAQIData", "yyyy-MM-dd HH:mm:ss");

            List<StationDayAQIDataVO> collect = parkDayAQIData.stream().peek(m -> m.setMonitorTime(OverAlarmController.format(m.getMonitorTime(), "yyyy-MM-dd"))).sorted(Comparator.comparing(m -> ((StationDayAQIDataVO) m).
                    getStationCode()).thenComparing(m -> ((StationDayAQIDataVO) m).getMonitorTime())).collect(Collectors.toList());


            for (StationDayAQIDataVO dayAQIDataVO : collect) {
                List<Map<String, Object>> collect1 = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null && dayAQIDataVO.getStationCode().equals(m.get("DGIMN").toString())).collect(Collectors.toList());
                if (collect1.size() > 0) {
                    dayAQIDataVO.setMonitorPointName(collect1.get(0).get("MonitorPointName"));
                    pollutants.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(dayAQIDataVO.getPrimaryPollutant())).
                            peek(m -> dayAQIDataVO.setPrimaryPollutant(m.get("name").toString())).collect(Collectors.toList());
                }

            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", total);
            resultMap.put("listdata", collect);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 上午 11:17
     * @Description: 获取一段时间内空气站点的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getAirStationCompositeIndexByMonitorTimes", method = RequestMethod.POST)
    public Object getAirStationCompositeIndexByMonitorTimes(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            List<Map<String, Object>> manyStationMonthCompositeIndexByMonitorTimes = airMonitorStationService.getManyStationMonthCompositeIndexByMonitorTimes(starttime, endtime);


            return AuthUtil.parseJsonKeyToLower("success", manyStationMonthCompositeIndexByMonitorTimes);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 上午 11:17
     * @Description: 获取一段时间内空气站点的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getAirStationCompositeIndexListByMonitorTimes", method = RequestMethod.POST)
    public Object getAirStationCompositeIndexListByMonitorTimes(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                                @RequestJson(value = "pagesize", required = false) Integer pagesize, @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            List<Map<String, Object>> manyStationMonthCompositeIndexByMonitorTimes = airMonitorStationService.getManyStationMonthCompositeIndexByMonitorTimes(starttime, endtime);

            if (pagenum != null && pagesize != null) {
                manyStationMonthCompositeIndexByMonitorTimes = manyStationMonthCompositeIndexByMonitorTimes.stream().skip(pagenum - 1).limit(pagesize).collect(Collectors.toList());
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", manyStationMonthCompositeIndexByMonitorTimes.size());
            resultMap.put("listdata", manyStationMonthCompositeIndexByMonitorTimes);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/7/2 0002 下午 7:04
     * @Description: 获取一段时间内空气站首要污染物占比等信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countAirStationDayPrimaryPollutantByMonitorTime", method = RequestMethod.POST)
    public Object countAirStationDayPrimaryPollutantByMonitorTime(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            StationDayAQIDataVO stationDayAQIDataVO = new StationDayAQIDataVO();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            starttime = starttime + "-01 00:00:00";
            Date parse = format.parse(endtime);
            Calendar instance = Calendar.getInstance();
            instance.setTime(parse);
            instance.add(Calendar.MONTH, 1);
            instance.add(Calendar.SECOND, -1);
            Date time = instance.getTime();
            endtime = format1.format(time);

            Map<String, Object> timeMap = new HashMap<>();
            timeMap.put("starttime", starttime);
            timeMap.put("endtime", endtime);
            stationDayAQIDataVO.setMonitorTime(JSONObject.fromObject(timeMap).toString());

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutanttype", 5);
            List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
            List<Map<String, Object>> allAirMonitorStation = airMonitorStationService.getAllAirMonitorStation(new HashMap<>());
            String dgimns = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            stationDayAQIDataVO.setStationCode(dgimns);


            List<StationDayAQIDataVO> parkDayAQIData = mongoBaseService.getListByParam(stationDayAQIDataVO, "StationDayAQIData", "yyyy-MM");
            Map<String, List<StationDayAQIDataVO>> collect = parkDayAQIData.stream().collect(Collectors.groupingBy(m -> m.getStationCode()));
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (String StationCode : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> datalist = new ArrayList<>();
                List<Map<String, Object>> collect3 = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null && m.get("DGIMN").toString().equals(StationCode)).collect(Collectors.toList());

                if (collect3.size() > 0) {
                    data.put("monitorname", collect3.get(0).get("MonitorPointName"));
                    List<StationDayAQIDataVO> stationDayAQIDataVOS = collect.get(StationCode);
                    Map<String, Long> collect1 = stationDayAQIDataVOS.stream().collect(Collectors.groupingBy(m -> m.getPrimaryPollutant(), Collectors.counting()));
                    for (String s1 : collect1.keySet()) {
                        Map<String, Object> datas = new HashMap<>();
                        List<Map<String, Object>> collect2 = pollutants.stream().filter(m -> m.get("code") != null && m.get("code").equals(s1)).collect(Collectors.toList());
                        if (collect2.size() > 0) {
                            datas.put("primarypollutant", collect2.get(0).get("name"));
                        } else {
                            datas.put("primarypollutant", "");
                        }
                        datas.put("daynum", collect1.get(s1));
                        datas.put("percentage", decimalFormat.format(Float.valueOf(collect1.get(s1)) / stationDayAQIDataVOS.size() * 100) + "%");
                        datalist.add(datas);
                    }
                    data.put("datalist", datalist);
                    resultList.add(data);
                }


            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 上午 11:38
     * @Description: 统计一段时间内园区内外首要污染物占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    private Object countAirDayPrimaryPollutantByMonitorTime(String starttime, String endtime, String collect) throws Exception {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            try {
                starttime = starttime + "-01";
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-01");
                Date startDate = DataFormatUtil.getDateYMD(starttime);
                Date endDate = DataFormatUtil.getDateYMD(endtime);
                Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id");
                Aggregation aggregation = newAggregation(
                        project(fields),
                        match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("PrimaryPollutant").ne("null")
                        )
                );
                AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collect, Document.class);
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
            return dataList;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 上午 11:38
     * @Description: 组织mongodb中查询的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [documents, key, codes]
     * @throws:
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
                    if (StringUtils.isNotBlank(tempValue) && !"null".equals(tempValue)) {
                        setKeyAndNum(keyAndNum, tempValue);
                        codes.add(tempValue);
                    }

                }
            }
        }
        return keyAndNum;
    }


    /**
     * @author: chengzq
     * @date: 2019/6/29 0029 上午 11:38
     * @Description: 设置map中key的数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [keyAndNum, key]
     * @throws:
     */
    private void setKeyAndNum(Map<String, Integer> keyAndNum, String key) {
        if (keyAndNum.get(key) != null) {
            keyAndNum.put(key, keyAndNum.get(key) + 1);
        } else {
            keyAndNum.put(key, 1);
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/27 0027 上午 10:14
     * @Description: 获取aqi+常规六参数数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAirCommonSixIndexData", method = RequestMethod.POST)
    public Object getAirCommonSixIndexData(@RequestJson(value = "isaqi", required = false) String isAqi) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<String> codes = CommonTypeEnum.getSixIndexList();
            for (String code : codes) {
                if ("no".equals(isAqi) && code.equals("aqi")) {

                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pollutantcode", code);
                    map.put("pollutantname", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                    map.put("pollutantunit", CommonTypeEnum.AirCommonSixIndexEnum.getUnitByCode(code));
                    resultList.add(map);
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
     * @date: 2022/04/19 0019 上午 10:39
     * @Description: 通过监测污染物和时间段和数据类型获取点位空气质量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查空气站点小时表（数据按小时分组）  day:查空气站点日表（数据按日分组）  month：查空气站点月表（数据按月分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd   month:yyyy-MM）
     * @return:
     */
    @RequestMapping(value = "getAllAirStationDataByParamMap", method = RequestMethod.POST)
    public Object getAllAirStationDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                 @RequestJson(value = "stationids", required = false) List<String> stationids,
                                                 @RequestJson(value = "pollutantcode") String pollutantcode,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (stationids == null) {
                stationids = new ArrayList<>();
            }
            //获取点位信息
            paramMap.put("airids", stationids);
            List<Map<String, Object>> outPuts = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            for (Map<String, Object> map : outPuts) {
                mns.add(map.get("dgimn").toString());
                mnandname.put(map.get("dgimn").toString(), map.get("monitorpointname"));
            }
            paramMap.put("mns", mns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            paramMap.put("pollutantcode", pollutantcode);
            if ("compositeindex".equals(pollutantcode)) {
                //当前月各站点月综合指数
                dataList = airMonitorStationService.getManyMonthManyStationMonthCompositeIndex(paramMap);
            } else {
                dataList = airMonitorStationService.getAllAirStationDataByParamMap(paramMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 4:00
     * @Description: 通过监测污染物和时间段和数据类型获取城市空气质量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查城市小时表（数据按小时分组）  day:查城市日表（数据按日分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd）
     * @return:
     */
    @RequestMapping(value = "getAllAirCityDataByParamMap", method = RequestMethod.POST)
    public Object getAllAirCityDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                              @RequestJson(value = "pollutantcode") String pollutantcode,
                                              @RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "endtime") String endtime
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            paramMap.put("pollutantcode", pollutantcode);
            if ("compositeindex".equals(pollutantcode)) {
                //当前月各站点月综合指数
                //dataList =  airMonitorStationService.getManyMonthManyCityMonthCompositeIndex(paramMap);
            } else {
                dataList = airMonitorStationService.getAllAirCityDataByParamMap(paramMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
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
    @RequestMapping(value = "getAirStationPollutantCumulativeDataByParamMap", method = RequestMethod.POST)
    public Object getAirStationPollutantCumulativeDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                                 @RequestJson(value = "stationids", required = false) List<String> stationids,
                                                                 @RequestJson(value = "starttime") String starttime,
                                                                 @RequestJson(value = "endtime") String endtime
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (stationids == null) {
                stationids = new ArrayList<>();
            }
            //获取点位信息
            paramMap.put("airids", stationids);
            List<Map<String, Object>> outPuts = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            for (Map<String, Object> map : outPuts) {
                mns.add(map.get("dgimn").toString());
                mnandname.put(map.get("dgimn").toString(), map.get("monitorpointname"));
            }
            paramMap.put("mns", mns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            dataList = airMonitorStationService.getAirStationPollutantCumulativeDataByParamMap(paramMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }

    /**
     * @author: xsm
     * @date: 2022/04/19 0019 下午 4:00
     * @Description: 通过数据类型和时间段获取城市六参数污染物累计浓度（平均浓度）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查城市小时表（数据按小时分组）  day:查城市日表（数据按日分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd）
     * @return:
     */
    @RequestMapping(value = "getAirPollutantCumulativeDataByParamMap", method = RequestMethod.POST)
    public Object getAirPollutantCumulativeDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            dataList = airMonitorStationService.getAirPollutantCumulativeDataByParamMap(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
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
    @RequestMapping(value = "getAirStationYearOnYeraAnalysisDataByParamMap", method = RequestMethod.POST)
    public Object getAirStationYearOnYeraAnalysisDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                                @RequestJson(value = "mn") String mn,
                                                                @RequestJson(value = "pollutantcode") String pollutantcode,
                                                                @RequestJson(value = "starttime") String starttime,
                                                                @RequestJson(value = "endtime") String endtime
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mn", mn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            paramMap.put("pollutantcode", pollutantcode);
            if ("compositeindex".equals(pollutantcode)) {
                //当前月各站点月综合指数
                dataList = airMonitorStationService.getOneStationMonthYearOnYeraCompositeIndex(paramMap);
            } else {
                dataList = airMonitorStationService.getAirStationYearOnYeraAnalysisDataByParamMap(paramMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
    }

    /**
     * @author: xsm
     * @date: 2022/04/20 0020 上午 09:30
     * @Description: 通过监测污染物和时间段和数据类型获取城市同比分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查城市小时表（数据按小时分组）  day:查城市日表（数据按日分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd）
     * @return:
     */
    @RequestMapping(value = "getAirCityYearOnYeraAnalysisDataByParamMap", method = RequestMethod.POST)
    public Object getAirCityYearOnYeraAnalysisDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                             @RequestJson(value = "regioncode") String regioncode,
                                                             @RequestJson(value = "pollutantcode") String pollutantcode,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime
    ) throws Exception {

        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("regioncode", regioncode);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            paramMap.put("pollutantcode", pollutantcode);
            dataList = airMonitorStationService.getAirCityYearOnYeraAnalysisDataByParamMap(paramMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", dataList);
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
    @RequestMapping(value = "getAirStationYearOnYeraCumulativeDataByParamMap", method = RequestMethod.POST)
    public Object getAirStationYearOnYeraCumulativeDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                                  @RequestJson(value = "mn") String mn,
                                                                  @RequestJson(value = "starttime") String starttime,
                                                                  @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mn", mn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            result = airMonitorStationService.getAirStationYearOnYeraCumulativeDataByParamMap(paramMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @author: xsm
     * @date: 2022/04/20 0020 上午 10:55
     * @Description: 通过时间段和数据类型获取单行政区划各污染物累计同比分析数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:hour 查城市小时表（数据按小时分组）  day:查城市日表（数据按日分组）
     * @param: starttime, endtime  根据datatype（hour：yyyy-MM-dd HH    day:yyyy-MM-dd）
     * @return:
     */
    @RequestMapping(value = "getCityYearOnYeraCumulativeDataByParamMap", method = RequestMethod.POST)
    public Object getCityYearOnYeraCumulativeDataByParamMap(@RequestJson(value = "datatype") String datatype,
                                                            @RequestJson(value = "regioncode") String regioncode,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("regioncode", regioncode);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("datatype", datatype);
            result = airMonitorStationService.getCityYearOnYeraCumulativeDataByParamMap(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    /**
     * @Description: 园区/城区空气质量排名（日、月、年，综合指数、六参数、优良天数、重污染天数、首要污染物）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/19 13:24
     */
    @RequestMapping(value = "getStationOrCityRankDataList", method = RequestMethod.POST)
    public Object getStationOrCityRankDataList(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "sortmap", required = false) Object sortmap
    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> idAndName = new HashMap<>();
            List<Map<String, Object>> pointList;
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String mnCommon;
            String collection;
            String mnKey;
            if (datatype.equals("city")) {//城区
                pointList = airCityMonthDataService.getRegionDataList();
                collection = DB_CityDayAQIData;
                mnKey = "RegionCode";

            } else {//园区
                pointList = airMonitorStationService.getAirStationInfoByParamMap(paramMap);
                collection = DB_StationDayAQIData;
                mnKey = "StationCode";
            }
            if (pointList.size() > 0) {
                for (Map<String, Object> point : pointList) {
                    if (point.get("dgimn") != null) {
                        mnCommon = point.get("dgimn").toString();
                        idAndName.put(mnCommon, point.get("monitorpointname"));
                        mns.add(mnCommon);
                    }
                }
                paramMap.put("idAndName", idAndName);
                paramMap.put("mnKey", mnKey);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                paramMap.put("monitortime", monitortime);
                paramMap.put("sortmap", sortmap);
                paramMap.put("timetype", timetype);
                paramMap.put("datatype", datatype);
                resultList = airMonitorStationService.getStationOrCityRankDataListByParam(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @Description: 导出园区/城区空气质量排名（日、月、年，综合指数、六参数、优良天数、重污染天数、首要污染物）
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/19 13:24
     */
    @RequestMapping(value = "exportStationOrCityRankDataList", method = RequestMethod.POST)
    public void exportStationOrCityRankDataList(
            @RequestJson(value = "timetype") String timetype,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "sortmap", required = false) Object sortmap,
            HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> idAndName = new HashMap<>();
            List<Map<String, Object>> pointList;
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String mnCommon;
            String collection;
            String mnKey;
            if (datatype.equals("city")) {//城区
                pointList = airCityMonthDataService.getRegionDataList();
                collection = DB_CityDayAQIData;
                mnKey = "RegionCode";

            } else {//园区
                pointList = airMonitorStationService.getAirStationInfoByParamMap(paramMap);
                collection = DB_StationDayAQIData;
                mnKey = "StationCode";
            }
            String pointName = datatype.equals("city") ? "城市" : "监测站点";
            List<Map<String, Object>> titleList = setExportTitle(pointName);

            if (pointList.size() > 0) {
                for (Map<String, Object> point : pointList) {
                    if (point.get("dgimn") != null) {
                        mnCommon = point.get("dgimn").toString();
                        idAndName.put(mnCommon, point.get("monitorpointname"));
                        mns.add(mnCommon);
                    }
                }
                paramMap.put("idAndName", idAndName);
                paramMap.put("mnKey", mnKey);
                paramMap.put("collection", collection);
                paramMap.put("mns", mns);
                paramMap.put("monitortime", monitortime);
                paramMap.put("sortmap", sortmap);
                paramMap.put("timetype", timetype);
                paramMap.put("datatype", datatype);
                resultList = airMonitorStationService.getStationOrCityRankDataListByParam(paramMap);
                if (resultList.size() > 0) {
                    int ordernum = 0;
                    for (Map<String, Object> data : resultList) {
                        ordernum++;
                        data.put("ordernum", ordernum);
                    }
                }


                datatype = datatype.equals("city") ? "城市" : "园区";
                String fileName = datatype + "空气质量排名分析" + "_" + new Date().getTime();
                List<Map<String, Object>> headers = FormatUtils.setManyHeaderExportData(titleList);
                ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headers, resultList, "");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private List<Map<String, Object>> setExportTitle(String name) {

        List<Map<String, Object>> headers = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("label", "排名");
        map1.put("prop", "ordernum");
        headers.add(map1);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("label", name);
        map3.put("prop", "monitorpointname");
        headers.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("label", "综合指数");
        map4.put("prop", "zhzs");
        List<Map<String, Object>> chlid4 = new ArrayList<>();
        chlid4.add(getTableMap("zhzs_value", "指数"));
        chlid4.add(getTableMap("zhzs_tb", "同比"));
        map4.put("children", chlid4);
        headers.add(map4);
        List<String> pollutantCodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
        for (String codeIndex : pollutantCodes) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(codeIndex));
            map.put("prop", codeIndex);
            List<Map<String, Object>> chlid = new ArrayList<>();
            chlid.add(getTableMap(codeIndex + "_value", "浓度"));
            chlid.add(getTableMap(codeIndex + "_tb", "同比"));
            map.put("children", chlid);
            headers.add(map);
        }
        Map<String, Object> map5 = new HashMap<>();
        map5.put("label", "优良天数");
        map5.put("prop", "ylts");
        List<Map<String, Object>> chlid5 = new ArrayList<>();
        chlid5.add(getTableMap("ylts_value", "天数"));
        chlid5.add(getTableMap("ylts_tb", "同比"));
        map5.put("children", chlid5);
        headers.add(map5);

        Map<String, Object> map6 = new HashMap<>();
        map6.put("label", "重污染天数");
        map6.put("prop", "ylts");
        List<Map<String, Object>> chlid6 = new ArrayList<>();
        chlid6.add(getTableMap("zwrts_value", "天数"));
        chlid6.add(getTableMap("zwrts_tb", "同比"));
        map6.put("children", chlid6);
        headers.add(map6);

        Map<String, Object> map7 = new HashMap<>();
        map7.put("label", "首要污染物");
        map7.put("prop", "primarypollutant");

        headers.add(map7);
        return headers;

    }

    private Map<String, Object> getTableMap(String prop, String label) {
        Map<String, Object> map = new HashMap<>();
        map.put("prop", prop);
        map.put("label", label);
        return map;
    }

}
