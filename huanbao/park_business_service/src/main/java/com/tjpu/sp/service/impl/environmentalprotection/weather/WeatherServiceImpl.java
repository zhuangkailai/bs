package com.tjpu.sp.service.impl.environmentalprotection.weather;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.AirMonitorStationMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.weather.WeatherMapper;
import com.tjpu.sp.service.environmentalprotection.weather.WeatherService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    private final String minuteCollection = "MinuteData";
    private final String hourCollection = "HourData";
    private final String dayCollection = "DayData";

    private final WeatherMapper weatherMapper;
    private final PollutantFactorMapper pollutantFactorMapper;
    private final OtherMonitorPointMapper otherMonitorPointMapper;
    private final UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    private final AirMonitorStationMapper airMonitorStationMapper;


    public WeatherServiceImpl(WeatherMapper weatherMapper, PollutantFactorMapper pollutantFactorMapper, OtherMonitorPointMapper otherMonitorPointMapper, UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper, AirMonitorStationMapper airMonitorStationMapper) {
        this.weatherMapper = weatherMapper;
        this.pollutantFactorMapper = pollutantFactorMapper;
        this.otherMonitorPointMapper = otherMonitorPointMapper;
        this.unorganizedMonitorPointInfoMapper = unorganizedMonitorPointInfoMapper;
        this.airMonitorStationMapper = airMonitorStationMapper;
    }

    /**
     * @author: xsm
     * @date: 2019/6/26 7:34
     * @Description: 通过自定义参数统计监测点位的监测数据及气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countWeatherAndMonitorPointDataByParamMap(String airmn, String mn, Integer pointtype, String timetype, List<String> pollutantcodes, String starttime, String endtime) {
        try {
            //根据污染物编码和监测点类型去获取对应的污染物信息
            Date startDate = null;
            Date endDate = null;
            if ("minute".equals(timetype)) {//查询分钟数据
                startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00");
                endDate = DataFormatUtil.getDateYMDHMS(endtime + ":00");
            } else if ("hour".equals(timetype)) {//查询小时数据
                startDate = DataFormatUtil.getDateYMDH(starttime);
                endDate = DataFormatUtil.getDateYMDH(endtime);
            } else if ("day".equals(timetype)) {//查询日数据
                startDate = DataFormatUtil.getDateYMD(starttime);
                endDate = DataFormatUtil.getDateYMD(endtime);
            }
            List<Map<String, Object>> concentrationlist = new ArrayList<>();
            //获取点位下时段各因子浓度
            if (pointtype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
                //烟气获取折算值
                concentrationlist = getMonitorPointConversionConcentrationData(mn, timetype, pollutantcodes, startDate, endDate);
            } else {
                concentrationlist = getMonitorPointConcentrationData(mn, timetype, pollutantcodes, startDate, endDate);
            }

            Map<String, Object> result = new HashMap<>();
            if (pointtype != CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() && pointtype != CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()
                    && pointtype != CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {
                //获取点位下各时段的气候信息
                List<Map<String, Object>> weathermap = getMonitorPointWeatherData(airmn, timetype, startDate, endDate, concentrationlist);
                if (!"minute".equals(timetype)) {//查询分钟数据
                    //获取以风向分组的因子平均浓度统计数据
                    Map<String, Object> windDirection = new HashMap<>();
                    if (concentrationlist.size() > 0 || weathermap.size() > 0) {
                        windDirection = countAvgConcentrationGroupByWindDirection(concentrationlist, weathermap, pollutantcodes);
                    }
                    result.put("windDirection", windDirection);
                }
                result.put("weatherlist", weathermap);
            }
            result.put("concentrationlist", concentrationlist);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/26 7:52
     * @Description: 获取监测点下因子监测浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointConcentrationData(String mn, String timetype, List<String> pollutantlist, Date startDate, Date endDate) {


        //去MongoDB中查询数据
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String collectionname = "";
        String datalistname = "";
        if ("minute".equals(timetype)) {//分钟
            collectionname = minuteCollection;
            datalistname = "MinuteDataList";
        } else if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(mn));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                Map<String, Object> map = new LinkedHashMap<>();
                String monitortime = "";
                if ("minute".equals(timetype)) {//分钟
                    monitortime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                    datalistname = "MinuteDataList";
                } else if ("hour".equals(timetype)) {//根据类型判断出  是查询小时数据
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    datalistname = "HourDataList";
                } else if ("day".equals(timetype)) {//根据类型判断出  是查询日数据
                    monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    datalistname = "DayDataList";
                }
                List<Map<String, Object>> pollutantvalue = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                boolean flag = false;
                for (String code : pollutantlist) {//遍历污染物
                    Object value = "";
                    Map<String, Object> objmap = new HashMap<>();
                    for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                        if (code.equals(dataMap.get("PollutantCode"))) {
                            if (dataMap.get("AvgStrength") != null) {
                                value = dataMap.get("AvgStrength");
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                    if (!"".equals(value)) {
                        flag = true;
                    }
                    objmap.put("pollutantcode", code);
                    objmap.put("pollutantvalue", value);
                    pollutantvalue.add(objmap);
                }
                if (flag == true) {
                    map.put("monitortime", monitortime);
                    map.put("pollutantdata", pollutantvalue);
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/10/12 9:24
     * @Description: 获取烟气监测点下因子折算监测浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointConversionConcentrationData(String mn, String timetype, List<String> pollutantlist, Date startDate, Date endDate) {
        //去MongoDB中查询数据
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String collectionname = "";
        String datalistname = "";
        if ("minute".equals(timetype)) {//分钟
            collectionname = minuteCollection;
            datalistname = "MinuteDataList";
        } else if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(mn));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                Map<String, Object> map = new LinkedHashMap<>();
                String monitortime = "";
                if ("minute".equals(timetype)) {//分钟
                    monitortime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                    datalistname = "MinuteDataList";
                } else if ("hour".equals(timetype)) {//根据类型判断出  是查询小时数据
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    datalistname = "HourDataList";
                } else if ("day".equals(timetype)) {//根据类型判断出  是查询日数据
                    monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    datalistname = "DayDataList";
                }
                List<Map<String, Object>> pollutantvalue = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                boolean flag = false;
                for (String code : pollutantlist) {//遍历污染物
                    Object value = "";
                    Map<String, Object> objmap = new HashMap<>();
                    for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                        if (code.equals(dataMap.get("PollutantCode"))) {
                            if (dataMap.get("AvgConvertStrength") != null) {
                                value = dataMap.get("AvgConvertStrength");
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                    if (!"".equals(value)) {
                        flag = true;
                    }
                    objmap.put("pollutantcode", code);
                    objmap.put("pollutantvalue", value);
                    pollutantvalue.add(objmap);
                }
                if (flag == true) {
                    map.put("monitortime", monitortime);
                    map.put("pollutantdata", pollutantvalue);
                    result.add(map);
                }
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/6/26 7:52
     * @Description: 获取监测点气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointWeatherData(String airmn, String timetype, Date startDate, Date endDate, List<Map<String, Object>> concentrationlist) {
        String collectionname = "";
        String datalistname = "";
        if ("minute".equals(timetype)) {//分钟
            collectionname = minuteCollection;
            datalistname = "MinuteDataList";
        } else if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
        }
        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        List<Map<String, Object>> datas = MongoDataUtils.getWindDataList(documents, collectionname);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (datas != null && datas.size() > 0) {
            for (Map<String, Object> objmap : concentrationlist) {
                Map<String, Object> map = new LinkedHashMap<>();
                boolean flag = false;
                for (Map<String, Object> obj : datas) {
                    String time = obj.get("monitortime").toString();
                    if (time.equals(objmap.get("monitortime").toString())) {//当时间相等时
                        map.put("monitortime", time);
                        map.put("windspeed", obj.get("windspeed"));
                        map.put("winddirection", obj.get("winddirectionname") != null ? obj.get("winddirectionname").toString() : "");
                        map.put("winddirectioncode", obj.get("winddirectioncode"));
                        map.put("winddirectionvalue", obj.get("winddirectionvalue"));
                        flag = true;
                        break;
                    }
                }
                if (flag == true) {
                    result.add(map);
                } else {
                    map.put("monitortime", objmap.get("monitortime").toString());
                    map.put("windspeed", "");
                    map.put("winddirection", "");
                    map.put("winddirectioncode", "");
                    map.put("winddirectionvalue", "");
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/26 7:52
     * @Description: 获取监测点气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public List<Map<String, Object>> getMonitorPointWeatherData(String airmns, String timetype, Date startDate, Date endDate) {
        String[] mnstr = airmns.split(",");
        List<String> mnlist = Arrays.asList(mnstr);
        String collectionname = "";
        String datalistname = "";
        if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
        }
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (String mn : mnlist) {
            String airmn = "";
            for (String dgimn : mnlist) {//获取空气点位MN号
                airmn = dgimn;
                break;
            }
            Map<String, Object> map = new LinkedHashMap<>();
            Double windspeed = 0d;
            Double winddirection = null;
            map.put("dgimn", mn);
            if (documents.size() > 0) {
                for (Document document : documents) {
                    if (airmn.equals(document.get("DataGatherCode"))) {
                        List<Map<String, Object>> pollutantList = (List<Map<String, Object>>) document.get(datalistname);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                windspeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : 0d;
                                break;
                            }
                        }
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                winddirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                break;
                            }
                        }
                        if (winddirection != null) {
                            map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                            map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                            map.put("winddirectionvalue", winddirection);
                        } else {
                            map.put("winddirectioncode", winddirection);
                            map.put("winddirectionvalue", winddirection);
                            map.put("winddirectionname", winddirection);
                        }
                        map.put("windspeed", windspeed);
                    }
                }
            } else {
                map.put("winddirectioncode", "");
                map.put("winddirectionvalue", "");
                map.put("winddirectionname", "");
                map.put("windspeed", "");
            }
            result.add(map);
        }

        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/6/27 13:11
     * @Description: 按风向统计各污染物的平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> countAvgConcentrationGroupByWindDirection(List<Map<String, Object>> concentrationlist, List<Map<String, Object>> weathermap, List<String> pollutantcodes) {
        String[] names = DataFormatUtil.directName;
        Map<String, Object> resultmap = new HashMap<>();
        for (String str : names) {
            List<String> timelist = new ArrayList<>();
            boolean flag = false;
            for (Map<String, Object> objmap : weathermap) {
                if (str.equals(objmap.get("winddirection"))) {
                    timelist.add(objmap.get("monitortime").toString());
                }
            }
            List<Map<String, Object>> resultlist = new ArrayList<>();
            for (String code : pollutantcodes) {
                Map<String, Object> valuemap = new HashMap<>();
                String value = countOnePollutantAvgConcentration(timelist, code, concentrationlist);
                valuemap.put("pollutantcode", code);
                valuemap.put("pollutantvalue", value);
                resultlist.add(valuemap);
                if (!"".equals(value)) {
                    flag = true;
                }
            }
            if (flag == true) {
                resultmap.put(str, resultlist);
            }
        }
        return resultmap;
    }

    /**
     * @param timelist
     * @param code
     * @param concentrationlist
     * @author: xsm
     * @date: 2019/6/28 10:48
     * @Description: 统计单个污染物的平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String countOnePollutantAvgConcentration(List<String> timelist, String code, List<Map<String, Object>> concentrationlist) {
        int num = 0;
        double valuenum = 0d;
        DecimalFormat df = new DecimalFormat("#.00");
        for (String timestr : timelist) {
            for (Map<String, Object> objmap2 : concentrationlist) {
                //当时间相同时
                if (timestr.equals(objmap2.get("monitortime").toString())) {
                    //获取存放污染物的集合
                    List<Map<String, Object>> pollutantvalue = (List<Map<String, Object>>) objmap2.get("pollutantdata");
                    if (pollutantvalue != null && pollutantvalue.size() > 0) {
                        //遍历污染物集合，找出对应的污染物
                        for (Map<String, Object> maps : pollutantvalue) {
                            if (code.equals(maps.get("pollutantcode").toString()) && maps.get("pollutantvalue") != null && !"".equals(maps.get("pollutantvalue"))) {
                                num += 1;
                                valuenum += Double.parseDouble(maps.get("pollutantvalue").toString());
                                break;
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        String value = "";
        if (num > 0) {
            value = df.format(valuenum / num);
        }
        return value;
    }

    /**
     * @author: xsm
     * @date: 2019/6/27 17:26
     * @Description: 通过自定义参数获取监测点位的列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWeathersAndMonitorPointListsByParamMap(String airmn, String mn, Integer pointtype, String timetype, List<String> pollutantcodes, String starttime, String endtime) {
        try {
            Date startDate = null;
            Date endDate = null;
            List<String> timelist = new ArrayList<>();
            if ("hour".equals(timetype)) {//查询小时数据
                startDate = DataFormatUtil.getDateYMDH(starttime);
                endDate = DataFormatUtil.getDateYMDH(endtime);
                timelist = DataFormatUtil.getYMDHBetween(starttime, endtime);
            } else if ("day".equals(timetype)) {//查询日数据
                startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
                endDate = DataFormatUtil.getDateYMDHMS(endtime + " 00:00:00");
                timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
            }
            timelist.add(endtime);
            //获取点位下时段各因子浓度
            List<Map<String, Object>> concentrationlist = getMonitorPointConcentrationData(mn, timetype, pollutantcodes, startDate, endDate);
            //获取点位下各时段的气候信息
            List<Map<String, Object>> weathermap = getMonitorPointWeatherData(airmn, timetype, startDate, endDate, concentrationlist);
            List<Map<String, Object>> tablelistdatas = new ArrayList<Map<String, Object>>();
            // for (String thetime :timelist){
            for (int i = timelist.size() - 1; i >= 0; i--) {
                String thetime = timelist.get(i);
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", thetime);//时间
                boolean flag = false;
                if (weathermap != null && weathermap.size() > 0) {
                    for (Map<String, Object> obj1 : weathermap) {
                        if (thetime.equals(obj1.get("monitortime").toString())) {//当时间相等时
                            resultmap.put("windspeed", obj1.get("windspeed"));
                            resultmap.put("winddirection", obj1.get("winddirection"));
                        }
                    }
                } else {
                    resultmap.put("windspeed", "");
                    resultmap.put("winddirection", "");
                }
                if (concentrationlist != null && concentrationlist.size() > 0) {
                    for (Map<String, Object> obj2 : concentrationlist) {
                        if (thetime.equals(obj2.get("monitortime"))) {//当时间相等时
                            //获取存放污染物的集合
                            List<Map<String, Object>> pollutantvalue = (List<Map<String, Object>>) obj2.get("pollutantdata");
                            if (pollutantvalue != null && pollutantvalue.size() > 0) {
                                //遍历污染物集合，找出对应的污染物
                                for (Map<String, Object> maps : pollutantvalue) {
                                    if (maps.get("pollutantvalue") != null && !"".equals(maps.get("pollutantvalue").toString())) {
                                        flag = true;
                                        resultmap.put(maps.get("pollutantcode").toString(), maps.get("pollutantvalue"));
                                    }
                                }
                            }
                        }
                    }
                }
                if (flag == true) {//浓度没值 不储存该条数据
                    tablelistdatas.add(resultmap);
                }
            }
            return tablelistdatas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/6/28 11:28
     * @Description: 通过自定义参数获取污染物气象表头
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForWeathers(Map<String, Object> titleMap) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("monitorpointtype", titleMap.get("pointtype"));
        paramMap.put("pkidlist", Arrays.asList(titleMap.get("monitorpointid")));
        List<Map<String, Object>> listmap = new ArrayList<>();
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(Integer.parseInt(titleMap.get("pointtype").toString()))) {
            case EnvironmentalVocEnum: //voc
                listmap = otherMonitorPointMapper.getOtherMonitorPointAllPollutantsByIDAndType(titleMap);//VOC
                break;
            case EnvironmentalStinkEnum: //恶臭
                listmap = otherMonitorPointMapper.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);//恶臭
                break;
            case FactoryBoundaryStinkEnum: //厂界恶臭
                listmap = unorganizedMonitorPointInfoMapper.getEntBoundaryAllPollutantsByIDAndType(paramMap);//厂界恶臭
                break;
            case FactoryBoundarySmallStationEnum: //厂界小型站
                listmap = unorganizedMonitorPointInfoMapper.getEntBoundaryAllPollutantsByIDAndType(paramMap);//厂界小型站
                break;
        }
        List<Map<String, Object>> pollutants = new ArrayList<>();
        if (listmap != null && listmap.size() > 0) {
            for (Map<String, Object> obj : listmap) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", obj.get("code"));
                map.put("name", obj.get("name"));
                map.put("unit", obj.get("PollutantUnit"));
                pollutants.add(map);
            }
        }
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Map<String, Object> monitormap = new HashMap<>();
        monitormap.put("minwidth", "180px");
        monitormap.put("headeralign", "center");
        monitormap.put("fixed", "left");
        monitormap.put("showhide", true);
        monitormap.put("prop", "monitortime");
        monitormap.put("label", "监测时间");
        monitormap.put("align", "center");
        tableTitleData.add(monitormap);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("minwidth", "180px");
        map2.put("headeralign", "center");
        map2.put("fixed", "left");
        map2.put("showhide", true);
        map2.put("prop", "windspeed");
        map2.put("label", "风速");
        map2.put("align", "center");
        tableTitleData.add(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("minwidth", "180px");
        map3.put("headeralign", "center");
        map3.put("fixed", "left");
        map3.put("showhide", true);
        map3.put("prop", "winddirection");
        map3.put("label", "风向");
        map3.put("align", "center");
        tableTitleData.add(map3);
        //根据污染物获取表头
        if (pollutants.size() > 0) {
            List<Map<String, Object>> pollutantTableTitleData = getPollutantTableTitleData(pollutants);
            tableTitleData.addAll(pollutantTableTitleData);
        }
        return tableTitleData;
    }


    /**
     * @author: zhangzc
     * @date: 2019/5/28 14:04
     * @Description: 组件污染物表头
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPollutantTableTitleData(List<Map<String, Object>> pollutants) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Map<String, Object> pollutant : pollutants) {
            Map<String, Object> map = new HashMap<>();
            map.put("headeralign", "center");
            map.put("showhide", true);
            map.put("prop", pollutant.get("code"));
            String label = pollutant.get("name").toString();
            if (pollutant.get("unit") != null && pollutant.get("unit") != "") {
                label += "(" + pollutant.get("unit").toString() + ")";
            }
            map.put("label", label);
            map.put("type", "contaminated");
            map.put("align", "center");
            maps.add(map);
        }
        return maps;
    }

    /**
     * @author: xsm
     * @date: 2019/7/26 6:43
     * @Description: 根据监测时间和Mn号获取单个或多个监测点在某个时间点的风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWeatherDataByMonitortimeAndMns(String timetype, String monitortime, String dgimns) {
        String[] mnstr = dgimns.split(",");
        List<String> mnlist = Arrays.asList(mnstr);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dgimns", mnlist);
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);
        Set<String> airmns = new HashSet<>();
        for (Map<String, Object> map : airlist) {//获取空气点位MN号
            if (map.get("airmn") != null) {
                airmns.add(map.get("airmn").toString());
            }
        }
        String collectionname = "";
        String datalistname = "";
        Date startDate = null;
        Date endDate = null;
        if ("hour".equals(timetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
            startDate = DataFormatUtil.getDateYMDH(monitortime);
            endDate = DataFormatUtil.getDateYMDH(monitortime);
        } else if ("day".equals(timetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
            startDate = DataFormatUtil.getDateYMD(monitortime);
            endDate = DataFormatUtil.getDateYMD(monitortime);
        }
        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(airmns));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (String mn : mnlist) {
            String airmn = "";
            for (Map<String, Object> map : airlist) {//获取空气点位MN号
                if (mn.equals(map.get("stenchmn").toString())) {
                    if (map.get("airmn") != null) {
                        airmn = map.get("airmn").toString();
                    }
                    break;
                }
            }
            Map<String, Object> map = new LinkedHashMap<>();
            Double windspeed = 0d;
            Double winddirection = null;
            map.put("dgimn", mn);
            if (documents.size() > 0) {
                for (Document document : documents) {
                    if (airmn.equals(document.get("DataGatherCode"))) {
                        List<Map<String, Object>> pollutantList = (List<Map<String, Object>>) document.get(datalistname);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                windspeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : 0d;
                                break;
                            }
                        }
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                winddirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                break;
                            }
                        }
                        if (winddirection != null) {
                            map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                            map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                            map.put("winddirectionvalue", winddirection);
                        } else {
                            map.put("winddirectioncode", winddirection);
                            map.put("winddirectionvalue", winddirection);
                            map.put("winddirectionname", winddirection);
                        }
                        map.put("windspeed", windspeed);
                    }
                }
            } else {
                map.put("winddirectioncode", "");
                map.put("winddirectionvalue", "");
                map.put("winddirectionname", "");
                map.put("windspeed", "");
            }
            result.add(map);
        }

        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/27 2:38
     * @Description: 根据监测时间和Mn号获取单个监测点在某个时间范围的风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWeatherDataByMonitortimesAndMn(String datetype, String starttime, String endtime, String dgimn) {
        Map<String, Object> paramMap = new HashMap<>();
        List<String> mnlist = new ArrayList<>();
        mnlist.add(dgimn);
        paramMap.put("dgimns", mnlist);
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String airmn = "";
        if (airlist != null && airlist.size() > 0 && (airlist.get(0).get("airmn") != null && !"".equals(airlist.get(0).get("airmn").toString()))) {//判断该点位是否关联空气站
            airmn = airlist.get(0).get("airmn").toString();//获取空气点位MN号
        }
        String collectionname = "";
        String datalistname = "";
        Date startDate = null;
        Date endDate = null;
        List<String> timelist = new ArrayList<>();
        if ("hour".equals(datetype)) {
            collectionname = hourCollection;
            datalistname = "HourDataList";
            startDate = DataFormatUtil.getDateYMDH(starttime);
            endDate = DataFormatUtil.getDateYMDH(endtime);
            timelist = DataFormatUtil.getYMDHBetween(starttime, endtime);
            timelist.add(endtime);
        } else if ("day".equals(datetype)) {
            collectionname = dayCollection;
            datalistname = "DayDataList";
            startDate = DataFormatUtil.getDateYMD(starttime);
            endDate = DataFormatUtil.getDateYMD(endtime);
            timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
            timelist.add(endtime);
        }

        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionname);
        for (String thetime : timelist) {
            Map<String, Object> map = new LinkedHashMap<>();
            Double windspeed = 0d;
            Double winddirection = null;
            map.put("monitortime", thetime);
            if (documents.size() > 0) {
                for (Document document : documents) {
                    //监测时间
                    String monitortime = "";
                    if ("hour".equals(datetype)) {
                        monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    } else if ("day".equals(datetype)) {
                        monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    }
                    if (thetime.equals(monitortime)) {
                        List<Map<String, Object>> pollutantList = (List<Map<String, Object>>) document.get(datalistname);
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                windspeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : 0d;
                                break;
                            }
                        }
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                winddirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                break;
                            }
                        }
                        if (winddirection != null) {
                            map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                            map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                            map.put("winddirectionvalue", winddirection);
                        } else {
                            map.put("winddirectioncode", winddirection);
                            map.put("winddirectionvalue", winddirection);
                            map.put("winddirectionname", winddirection);
                        }
                        map.put("windspeed", windspeed);
                    }
                }
            } else {
                map.put("winddirectioncode", "");
                map.put("winddirectionvalue", "");
                map.put("winddirectionname", "");
                map.put("windspeed", "");
            }
            result.add(map);
        }

        return result;
    }

    /**
     * @author: lip
     * @date: 2019/8/14 0014 下午 3:24
     * @Description: 自定义查询条件获取日气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDayWeatherByParamMap(Map<String, Object> paramMap) {
        return weatherMapper.getDayWeatherByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/9/20 0020 上午 9:48
     * @Description: 获取当天实时气候和未来四天气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getTodayLastHourAndAfterFourDayWeathers() {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> todayMap = new HashMap<>();
        List<Map<String, Object>> afterFourDayWeather = new ArrayList<>();
        Date thedate = DataFormatUtil.parseDateYMD(DataFormatUtil.getDate());
        Date newstarttime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(new Date()) + " 00:00:00");
        Date newendtime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMDH(new Date()) + ":00:00");
        Date postponeDate = DataFormatUtil.parseDateYMD(DataFormatUtil.getPostponeDate(thedate, 4));
        //getYMDBetween
        //当日气候信息
        Criteria criteria = new Criteria();
        criteria.and("WeatherTime").gte(newstarttime).lte(newendtime);
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("WeatherDate", "WeatherTime", "WeatherPhenomenon", "WindDirection", "WindPower", "WeekDate", "Temperature", "Humidity", "AQI", "PrimaryPollutant", "AirQuality")
                , sort(Sort.Direction.DESC, "WeatherTime"), limit(1)), "WeatherData", Map.class).getMappedResults();
        if (mappedResults.size() > 0) {
            for (Map<String, Object> map : mappedResults) {
                Map<String, Object> themap = new HashMap<>();
                themap.put("WeatherDate", DataFormatUtil.getDateYMD((Date) map.get("WeatherDate")));
                themap.put("WeatherTime", DataFormatUtil.getDateYMDH((Date) map.get("WeatherTime")));
                themap.put("WeatherPhenomenon", map.get("WeatherPhenomenon"));
                themap.put("WindDirection", map.get("WindDirection"));
                themap.put("WindPower", map.get("WindPower"));
                themap.put("WeekDate", map.get("WeekDate"));
                themap.put("Humidity", map.get("Humidity"));
                themap.put("Temperature", map.get("Temperature") + "℃");
                themap.put("AQI", map.get("AQI"));
                themap.put("PrimaryPollutant", map.get("PrimaryPollutant"));
                themap.put("AirQuality", map.get("AirQuality"));
                if (DataFormatUtil.getDateYMD((Date) map.get("WeatherDate")).equals(DataFormatUtil.getDateYMD(thedate))) {
                    todayMap = themap;
                }
            }
        }
        //未来四天气候信息
        Criteria criteria2 = new Criteria();
        criteria2.and("WeatherDate").gt(thedate).lte(postponeDate);
        List<Map> mappedResults2 = mongoTemplate.aggregate(newAggregation(
                match(criteria2), project("WeatherDate", "WeatherTime", "WeatherPhenomenon", "WindDirection", "WindPower", "WeekDate", "Temperature", "Humidity", "AQI", "PrimaryPollutant", "AirQuality")
                , sort(Sort.Direction.DESC, "WeatherTime")), "WeatherData", Map.class).getMappedResults();
        if (mappedResults2.size() > 0) {
            for (Map<String, Object> map : mappedResults2) {
                String WeatherDate = DataFormatUtil.getDateYMDHMS((Date) map.get("WeatherDate"));
                String WeatherTime = DataFormatUtil.getDateYMDHMS((Date) map.get("WeatherTime"));
                if (WeatherDate.equals(WeatherTime)) {
                    Map<String, Object> themap = new HashMap<>();
                    themap.put("WeatherDate", DataFormatUtil.getDateYMD((Date) map.get("WeatherDate")));
                    themap.put("WeatherTime", DataFormatUtil.getDateYMDH((Date) map.get("WeatherTime")));
                    themap.put("WeatherPhenomenon", map.get("WeatherPhenomenon"));
                    themap.put("WindDirection", map.get("WindDirection"));
                    themap.put("WindPower", map.get("WindPower"));
                    themap.put("WeekDate", map.get("WeekDate"));
                    themap.put("Humidity", map.get("Humidity"));
                    themap.put("Temperature", map.get("Temperature") + "℃");
                    themap.put("AQI", map.get("AQI"));
                    themap.put("PrimaryPollutant", map.get("PrimaryPollutant"));
                    themap.put("AirQuality", map.get("AirQuality"));
                    afterFourDayWeather.add(themap);
                }
            }
            //排序
            Comparator<Object> orderbytime = Comparator.comparing(m -> ((Map) m).get("WeatherDate").toString());
            afterFourDayWeather = afterFourDayWeather.stream().sorted(orderbytime).collect(Collectors.toList());
        }
        resultMap.put("todayweather", todayMap);
        resultMap.put("afterfourdayweather", afterFourDayWeather);
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2020/5/19 0019 上午 10:17
     * @Description: 根据日期获取该日期所有小时气候数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getHoursWeathersByDayTime(String daytime) {
        List<Map<String, Object>> afterFourDayWeather = new ArrayList<>();
        Date thedate = DataFormatUtil.parseDateYMD(daytime);
        Criteria criteria2 = new Criteria();
        criteria2.and("WeatherDate").gte(thedate).lte(thedate);
        List<Map> mappedResults2 = mongoTemplate.aggregate(newAggregation(
                match(criteria2), project("WeatherDate", "WeatherTime", "WeatherPhenomenon", "WindDirection", "WindPower", "WeekDate", "Temperature", "Humidity")
                , sort(Sort.Direction.DESC, "WeatherTime")), "WeatherData", Map.class).getMappedResults();
        if (mappedResults2.size() > 0) {
            for (Map<String, Object> map : mappedResults2) {
                Map<String, Object> themap = new HashMap<>();
                themap.put("WeatherDate", DataFormatUtil.getDateYMD((Date) map.get("WeatherDate")));
                themap.put("WeatherTime", DataFormatUtil.getDateYMDH((Date) map.get("WeatherTime")));
                themap.put("WeatherPhenomenon", map.get("WeatherPhenomenon"));
                themap.put("WindDirection", map.get("WindDirection"));
                themap.put("WindPower", map.get("WindPower"));
                themap.put("WeekDate", map.get("WeekDate"));
                themap.put("Humidity", map.get("Humidity"));
                themap.put("Temperature", map.get("Temperature") + "℃");
                afterFourDayWeather.add(themap);
            }
            //排序
            Comparator<Object> orderbytime = Comparator.comparing(m -> ((Map) m).get("WeatherDate").toString());
            afterFourDayWeather = afterFourDayWeather.stream().sorted(orderbytime).collect(Collectors.toList());
        }
        return afterFourDayWeather;
    }


    /**
     * @author: chengzq
     * @date: 2020/11/20 0020 下午 4:32
     * @Description: 获取当天最新天气信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public Map<String, Object> getTodayLastDayWeather() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String startTime = JSONObjectUtil.getStartTime(LocalDate.now().toString());
        Criteria criteria = new Criteria();
        criteria.and("WeatherTime").gte(DataFormatUtil.getDateYMDHMS(startTime)).lte(DataFormatUtil.getDateYMDHMS(now));
        return mongoTemplate.aggregate(newAggregation(match(criteria), project("WeatherPhenomenon", "WindDirection", "WindPower", "WeekDate", "Temperature", "Humidity")
                        .and(DateOperators.DateToString.dateOf("WeatherTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("WeatherTime")
                        .and(DateOperators.DateToString.dateOf("WeatherDate").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("WeatherDate")
                , sort(Sort.Direction.DESC, "WeatherTime")), "WeatherData", Map.class).getMappedResults().stream().findFirst().orElse(new HashMap());
    }

    @Override
    public Map<String, Object> getHourWeathersByTime(String monitortime) {
        Map<String, Object> themap = new HashMap<>();
        String ymd = DataFormatUtil.FormatDateOneToOther(monitortime,"yyyy-MM-dd HH","yyyy-MM-dd")+" 00";

        Date stattime = DataFormatUtil.parseDateYMDH(ymd);
        Date endtime = DataFormatUtil.parseDateYMDH(monitortime);
        Criteria criteria2 = new Criteria();
        criteria2.and("WeatherTime").gte(stattime).lte(endtime);
        List<Map> mappedResults2 = mongoTemplate.aggregate(newAggregation(
                match(criteria2), project("WeatherDate", "WeatherTime", "WeatherPhenomenon", "WindDirection", "WindPower", "WeekDate", "Temperature", "Humidity")
                , sort(Sort.Direction.DESC, "WeatherTime")), "WeatherData", Map.class).getMappedResults();
        if (mappedResults2.size() > 0) {
            Map<String, Object> map = mappedResults2.get(0);
            themap.put("WeatherDate", DataFormatUtil.getDateYMD((Date) map.get("WeatherDate")));
            themap.put("WeatherTime", DataFormatUtil.getDateYMDH((Date) map.get("WeatherTime")));
            themap.put("WeatherPhenomenon", map.get("WeatherPhenomenon"));
            themap.put("WindDirection", map.get("WindDirection"));
            themap.put("WindPower", map.get("WindPower"));
            themap.put("WeekDate", map.get("WeekDate"));
            themap.put("Humidity", map.get("Humidity"));
            themap.put("Temperature", map.get("Temperature") + "℃");
        }
        return themap;
    }

}
