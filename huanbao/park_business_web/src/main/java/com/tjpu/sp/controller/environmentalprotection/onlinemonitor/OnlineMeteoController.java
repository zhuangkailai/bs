package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMeteoService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.meteoEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: xsm
 * @date: 2020/4/2 9:41
 * @Description: 在线气象站监测数据
 * @param:
 * @return:
 */
@RestController
@RequestMapping("onlineMeteo")
public class OnlineMeteoController {

    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private OnlineMeteoService OnlineMeteoService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 气象站监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode();

    /**
     * @author: xsm
     * @date: 2020/4/2 0002 下午 1:06
     * @Description: 根据MN号获取气象站点位最新一条小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMeteoMonitorPointLastHourDataByMN", method = RequestMethod.POST)
    public Object getMeteoMonitorPointLastHourDataByMN(@RequestJson(value = "dgimn", required = false) String dgimn) {
        try {
            Set<String> mns = new HashSet<>();
            //判断是查单站点 还是园区气象
            if (dgimn != null && !"".equals(dgimn)) {
                mns.add(dgimn);
            } else {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("monitorpointtype", meteoEnum.getCode());
                List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                            mns.add(map.get("dgimn").toString());
                        }
                    }
                }
            }
            Map<String, Object> resultMap = OnlineMeteoService.getMeteoMonitorPointLastHourData(mns);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/4/2 0002 下午 3:51
     * @Description: 自定义查询条件统计气象站点小时风向图表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countMeteoHourWindChartDataByParams", method = RequestMethod.POST)
    public Object countMeteoHourWindChartDataByParams(
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //获取关联信息
            List<String> mns = new ArrayList<>();
            if (monitorpointid != null) {
                OtherMonitorPointVO otherMonitorPointVO = otherMonitorPointService.getOtherMonitorPointByID(monitorpointid);
                mns.add(otherMonitorPointVO.getDgimn());
            } else {
                paramMap.put("monitorpointtype", meteoEnum.getCode());
                List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                            mns.add(map.get("dgimn").toString());
                        }
                    }
                }
            }
            if (mns.size() > 0) {
                paramMap.put("sort", "asc");
                paramMap.put("mns", mns);
                String collection = "";
                if (datatype.equals("hour")) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + ":00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + ":59:59";
                        paramMap.put("endtime", endtime);
                    }
                    collection = MongoDataUtils.getCollectionByDataMark(3);
                } else if (datatype.equals("day")) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + " 00:00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + " 23:59:59";
                        paramMap.put("endtime", endtime);
                    }
                    collection = MongoDataUtils.getCollectionByDataMark(4);
                }else if (datatype.equals("minute")) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + ":00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + ":59";
                        paramMap.put("endtime", endtime);
                    }
                    collection = MongoDataUtils.getCollectionByDataMark(2);
                }
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("collection", collection);
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = getParkWindChartData(documents, collection);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getParkWindChartData(List<Document> documents, String collection) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> parkList = new ArrayList<>();
        String[] speedValueList = DataFormatUtil.speedValue;
        String[] speedNameList = DataFormatUtil.speedName;
        String[] directCodeList = DataFormatUtil.directCode;
        String[] directNameList = DataFormatUtil.directName;
        Map<String, String> windcodename = new HashMap<>();
        for (int i = 0; i < directNameList.length; i++) {
            windcodename.put(directNameList[i], directCodeList[i]);
        }
        parkList = OnlineMeteoService.getParkWindData(collection, documents);
        int totalnum = parkList.size();
        for (int i = 0; i < speedNameList.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("windspeedname", speedNameList[i]);
            map.put("windspeedvalue", speedValueList[i]);
            Double windspeed = 0d;
            String winddirection = "";
            String winddirectioncode = "";
            List<Double> speedList;
            Map<String, List<Double>> directioncodeAndSpeedMap = new HashMap<>();
            for (Map<String, Object> objmap : parkList) {
                windspeed = (objmap.get("windspeed") != null && !"".equals(objmap.get("windspeed").toString())) ? Double.parseDouble(objmap.get("windspeed").toString()) : 0d;
                winddirection = (objmap.get("winddirectionname") != null && !"".equals(objmap.get("winddirectionname").toString())) ? objmap.get("winddirectionname").toString() : null;
                if (winddirection != null) {
                    if (DataFormatUtil.windSpeedSwitch(windspeed, "name").equals(speedNameList[i])) {//同一风级
                        winddirectioncode = windcodename.get(winddirection);
                        if (directioncodeAndSpeedMap.get(winddirectioncode) != null) {
                            speedList = directioncodeAndSpeedMap.get(winddirectioncode);
                        } else {
                            speedList = new ArrayList<>();
                        }
                        speedList.add(windspeed);
                        directioncodeAndSpeedMap.put(winddirectioncode, speedList);
                    }
                }
            }
            List<Map<String, Object>> winddatalist = new ArrayList<>();
            int subnum = 0;
            int windspeednum;
            for (int j = 0; j < directCodeList.length; j++) {
                Map<String, Object> windDataMap = new HashMap<>();
                if (directioncodeAndSpeedMap.get(directCodeList[j]) != null) {
                    windspeednum = directioncodeAndSpeedMap.get(directCodeList[j]).size();
                } else {
                    windspeednum = 0;
                }
                windDataMap.put("windspeednum", windspeednum);
                subnum +=windspeednum;
                windDataMap.put("winddirectioncode", directCodeList[j]);
                windDataMap.put("winddirectionname", directNameList[j]);
                winddatalist.add(windDataMap);
            }
            map.put("winddatalist", winddatalist);
            map.put("subnum", subnum);
            if (totalnum>0){
                map.put("ratedata", DataFormatUtil.SaveOneAndSubZero(100D*subnum/totalnum)+"%");
            }
            dataList.add(map);
        }

        return dataList;
    }


    /**
     * @author: xsm
     * @date: 2020/4/3 0003 下午 1:13
     * @Description: 自定义查询条件获取空气站点小时风向数据（时间，风速，风向）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMeteoHourWindDataByParams", method = RequestMethod.POST)
    public Object getMeteoHourWindDataByParams(
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            //获取关联信息
            List<String> mns = new ArrayList<>();
            if (monitorpointid != null) {
                OtherMonitorPointVO otherMonitorPointVO = otherMonitorPointService.getOtherMonitorPointByID(monitorpointid);
                mns.add(otherMonitorPointVO.getDgimn());
            } else {
                paramMap.put("monitorpointtype", meteoEnum.getCode());
                List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                            mns.add(map.get("dgimn").toString());
                        }
                    }
                }
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);

                paramMap.put("sort", "asc");
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = "";
                if (datatype.equals("hour")) {
                    collection = MongoDataUtils.getCollectionByDataMark(3);
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + ":00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + ":59:59";
                        paramMap.put("endtime", endtime);
                    }
                } else if (datatype.equals("day")) {
                    collection = MongoDataUtils.getCollectionByDataMark(4);
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + " 00:00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + " 23:59:59";
                        paramMap.put("endtime", endtime);
                    }
                }else if (datatype.equals("minute")) {
                    collection = MongoDataUtils.getCollectionByDataMark(2);
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + ":00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + ":59";
                        paramMap.put("endtime", endtime);
                    }
                }
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = OnlineMeteoService.getParkWindData(collection, documents);
            }

            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/4/3 1:15
     * @Description: 根据污染物Code获取该监测点类型下每个监测点该污染物的浓度以及风速和风力
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMeteoMonitorPointMinuteAndWeatherDataByParams", method = RequestMethod.POST)
    public Object getMeteoMonitorPointMinuteAndWeatherDataByParams(
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "dgimn", required = false) String dgimn,
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            Map<String, Object> codemap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            if (!StringUtils.isNotBlank(starttime) || !StringUtils.isNotBlank(endtime)) {
                Date today = new Date();
                starttime = DataFormatUtil.getDateYMD(today);
                endtime = DataFormatUtil.getDateYMD(today);
            }
            Date startTime = DataFormatUtil.parseDate(starttime + " 00:00:00");
            Date endTime = DataFormatUtil.parseDate(endtime + " 23:59:59");
            Set<String> mns = new HashSet<>();
            if (dgimn != null && !"".equals(dgimn)) {
                mns.add(dgimn);
            } else {
                paramMap.put("monitorpointtype", meteoEnum.getCode());
                List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                            mns.add(map.get("dgimn").toString());
                        }
                    }
                }
            }
            String[] directCodeList = DataFormatUtil.directCode;
            String[] directNameList = DataFormatUtil.directName;
            Map<String, String> windcodename = new HashMap<>();
            for (int i = 0; i < directNameList.length; i++) {
                windcodename.put(directCodeList[i], directNameList[i]);
            }
            if (mns.size() > 0) {
                //String code = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
                //String code2 = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
                Set<String> pollutants = new HashSet<>();
                pollutants.addAll(pollutantcodes);
                List<AggregationOperation> aggregations = new ArrayList<>();
                Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime).and("MinuteDataList.PollutantCode").in(pollutants);
                aggregations.add(match(criteria));
                UnwindOperation unwindOperation = unwind("MinuteDataList");
                aggregations.add(unwindOperation);
                Criteria in = Criteria.where("MinuteDataList.PollutantCode").in(pollutants);
                aggregations.add(match(in));
                Fields fields = fields("DataGatherCode", "MonitorTime").and("PollutantCode", "MinuteDataList.PollutantCode").and("AvgStrength", "MinuteDataList.AvgStrength");
                aggregations.add(project(fields));
                Map<String, Object> map2 = new HashMap<>();
                map2.put("PollutantCode", "$PollutantCode");
                map2.put("AvgStrength", "$AvgStrength");
                GroupOperation groupOperation = group("DataGatherCode", "MonitorTime").push(map2).as("MinuteDataList");
                aggregations.add(groupOperation);
                Aggregation aggregation = newAggregation(aggregations);
                AggregationResults<Document> minuteData = mongoTemplate.aggregate(aggregation, "MinuteData", Document.class);
                List<Document> documents = minuteData.getMappedResults();
                if (documents.size() > 0) {

                    List<String> times = getHMTimesSpiltMFor24Hours();
                    for (String time : times) {
                        for (String code : pollutantcodes) {
                            int num = 0;
                            double value = 0d;
                            Map<String, Integer> windmap = new HashMap<>();
                            Map<String, Object> windvaluemap = new HashMap<>();
                            if (WindDirectionEnum.getCode().equals(code)) {
                                List<Map<String, Object>> onelist = null;
                                if (codemap.get(code) != null) {
                                    onelist = (List<Map<String, Object>>) codemap.get(code);
                                } else {
                                    onelist = new ArrayList<>();
                                }
                                for (Document document : documents) {
                                    String date = DataFormatUtil.getDate(document.getDate("MonitorTime"));
                                    String hms = date.substring(11, 16);    //监测时间
                                    if (time.equals(hms)) {
                                        List<Document> pollutantDatas = document.get("MinuteDataList", new ArrayList<>().getClass());
                                        for (Document pollutantData : pollutantDatas) {
                                            String PollutantCode = pollutantData.getString("PollutantCode");
                                            if (PollutantCode.equals(code)) {
                                                if (pollutantData.get("AvgStrength") != null && !"".equals(pollutantData.get("AvgStrength").toString())) {
                                                    if (Double.parseDouble(pollutantData.get("AvgStrength").toString()) > 0 || Double.parseDouble(pollutantData.get("AvgStrength").toString()) < 0) {
                                                        String windDirection = DataFormatUtil.windDirectionSwitch(Double.parseDouble(pollutantData.get("AvgStrength").toString()), "code");
                                                        windvaluemap.put(windDirection, pollutantData.get("AvgStrength"));
                                                        if (windmap.get(windDirection) != null) {
                                                            windmap.put(windDirection, windmap.get(windDirection) + 1);
                                                        } else {
                                                            windmap.put(windDirection, 1);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                String wind = "";
                                if (windmap != null && windmap.size() > 0) {
                                    int windnum = 0;
                                    for (String key : windmap.keySet()) {
                                        if (windnum != 0) {
                                            if (windmap.get(key) > windnum) {
                                                windnum = windmap.get(key);
                                                wind = key;
                                            }
                                        } else {
                                            windnum = windmap.get(key);
                                            wind = key;
                                        }
                                    }
                                }
                                if (!"".equals(wind)) {
                                    Map<String, Object> onecodemap = new HashMap<>();
                                    onecodemap.put("winddirectioncode", wind);
                                    onecodemap.put("winddirectionname", windcodename.get(wind));
                                    onecodemap.put("windirectionvalue", windvaluemap.get(wind));
                                    onecodemap.put("monitortime", time);
                                    onecodemap.put("monitorvalue", windvaluemap.get(wind));
                                    onelist.add(onecodemap);
                                    codemap.put(code, onelist);
                                }
                            } else {
                                List<Map<String, Object>> onelist = null;
                                if (codemap.get(code) != null) {
                                    onelist = (List<Map<String, Object>>) codemap.get(code);
                                } else {
                                    onelist = new ArrayList<>();
                                }
                                for (Document document : documents) {
                                    String date = DataFormatUtil.getDate(document.getDate("MonitorTime"));
                                    String hms = date.substring(11, 16);    //监测时间
                                    if (time.equals(hms)) {
                                        List<Document> pollutantDatas = document.get("MinuteDataList", new ArrayList<>().getClass());
                                        for (Document pollutantData : pollutantDatas) {
                                            String PollutantCode = pollutantData.getString("PollutantCode");
                                            if (PollutantCode.equals(code)) {
                                                if (pollutantData.get("AvgStrength") != null && !"".equals(pollutantData.get("AvgStrength").toString())) {
                                                    if (Double.parseDouble(pollutantData.get("AvgStrength").toString()) > 0 || Double.parseDouble(pollutantData.get("AvgStrength").toString()) < 0) {
                                                        value = value + Double.parseDouble(pollutantData.get("AvgStrength").toString());
                                                        num += 1;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (num > 0) {
                                    Map<String, Object> onecodemap = new HashMap<>();
                                    onecodemap.put("winddirectioncode", "");
                                    onecodemap.put("winddirectionname", "");
                                    onecodemap.put("windirectionvalue", "");
                                    onecodemap.put("monitortime", time);
                                    onecodemap.put("monitorvalue", DataFormatUtil.formatDoubleSaveTwo(value / num));
                                    onelist.add(onecodemap);
                                    codemap.put(code, onelist);
                                }
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", codemap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/4/3 1:15
     * @Description: 根据污染物Code获取该监测点类型下每个监测点该污染物的浓度以及风速和风力
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMeteoMonitorPointDayAndWeatherDataByParams", method = RequestMethod.POST)
    public Object getMeteoMonitorPointDayAndWeatherDataByParams(
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "dgimn", required = false) String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) throws ParseException {
        try {
            Map<String, Object> codemap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startTime = sdf.parse(starttime);
            Date endTime = sdf.parse(endtime);
            Set<String> mns = new HashSet<>();
            if (dgimn != null && !"".equals(dgimn)) {
                mns.add(dgimn);
            } else {
                paramMap.put("monitorpointtype", meteoEnum.getCode());
                List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                            mns.add(map.get("dgimn").toString());
                        }
                    }
                }
            }
            String[] directCodeList = DataFormatUtil.directCode;
            String[] directNameList = DataFormatUtil.directName;
            Map<String, String> windcodename = new HashMap<>();
            for (int i = 0; i < directNameList.length; i++) {
                windcodename.put(directCodeList[i], directNameList[i]);
            }
            if (mns.size() > 0) {
                Set<String> pollutants = new HashSet<>();
                pollutants.addAll(pollutantcodes);
                List<AggregationOperation> aggregations = new ArrayList<>();
                Criteria criteria = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime).and("DayDataList.PollutantCode").in(pollutants);
                aggregations.add(match(criteria));
                UnwindOperation unwindOperation = unwind("DayDataList");
                aggregations.add(unwindOperation);
                Criteria in = Criteria.where("DayDataList.PollutantCode").in(pollutants);
                aggregations.add(match(in));
                Fields fields = fields("DataGatherCode", "MonitorTime").and("PollutantCode", "DayDataList.PollutantCode").and("AvgStrength", "DayDataList.AvgStrength");
                aggregations.add(project(fields));
                Map<String, Object> map2 = new HashMap<>();
                map2.put("PollutantCode", "$PollutantCode");
                map2.put("AvgStrength", "$AvgStrength");
                GroupOperation groupOperation = group("DataGatherCode", "MonitorTime").push(map2).as("DayDataList");
                aggregations.add(groupOperation);
                Aggregation aggregation = newAggregation(aggregations);
                AggregationResults<Document> minuteData = mongoTemplate.aggregate(aggregation, "DayData", Document.class);
                List<Document> documents = minuteData.getMappedResults();
                if (documents.size() > 0) {


                    List<String> times = findDates(startTime, endTime);
                    for (String time : times) {
                        for (String code : pollutantcodes) {
                            int num = 0;
                            double value = 0d;
                            Map<String, Integer> windmap = new HashMap<>();
                            Map<String, Object> windvaluemap = new HashMap<>();
                            if (WindDirectionEnum.getCode().equals(code)) {
                                List<Map<String, Object>> onelist = null;
                                if (codemap.get(code) != null) {
                                    onelist = (List<Map<String, Object>>) codemap.get(code);
                                } else {
                                    onelist = new ArrayList<>();
                                }
                                for (Document document : documents) {

                                    String hms = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));    //监测时间
                                    if (time.equals(hms)) {
                                        List<Document> pollutantDatas = document.get("DayDataList", new ArrayList<>().getClass());
                                        for (Document pollutantData : pollutantDatas) {
                                            String PollutantCode = pollutantData.getString("PollutantCode");
                                            if (PollutantCode.equals(code)) {
                                                if (pollutantData.get("AvgStrength") != null && !"".equals(pollutantData.get("AvgStrength").toString())) {
                                                    if (Double.parseDouble(pollutantData.get("AvgStrength").toString()) > 0 || Double.parseDouble(pollutantData.get("AvgStrength").toString()) < 0) {
                                                        String windDirection = DataFormatUtil.windDirectionSwitch(Double.parseDouble(pollutantData.get("AvgStrength").toString()), "code");
                                                        windvaluemap.put(windDirection, pollutantData.get("AvgStrength"));
                                                        if (windmap.get(windDirection) != null) {
                                                            windmap.put(windDirection, windmap.get(windDirection) + 1);
                                                        } else {
                                                            windmap.put(windDirection, 1);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                String wind = "";
                                if (windmap != null && windmap.size() > 0) {
                                    int windnum = 0;
                                    for (String key : windmap.keySet()) {
                                        if (windnum != 0) {
                                            if (windmap.get(key) > windnum) {
                                                windnum = windmap.get(key);
                                                wind = key;
                                            }
                                        } else {
                                            windnum = windmap.get(key);
                                            wind = key;
                                        }
                                    }
                                }
                                if (!"".equals(wind)) {
                                    Map<String, Object> onecodemap = new HashMap<>();
                                    onecodemap.put("winddirectioncode", wind);
                                    onecodemap.put("winddirectionname", windcodename.get(wind));
                                    onecodemap.put("windirectionvalue", windvaluemap.get(wind));
                                    onecodemap.put("monitortime", time);
                                    onecodemap.put("monitorvalue", windvaluemap.get(wind));
                                    onelist.add(onecodemap);
                                    codemap.put(code, onelist);
                                }
                            } else {
                                List<Map<String, Object>> onelist = null;
                                if (codemap.get(code) != null) {
                                    onelist = (List<Map<String, Object>>) codemap.get(code);
                                } else {
                                    onelist = new ArrayList<>();
                                }
                                for (Document document : documents) {
                                    String hms = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));   //监测时间
                                    if (time.equals(hms)) {
                                        List<Document> pollutantDatas = document.get("DayDataList", new ArrayList<>().getClass());
                                        for (Document pollutantData : pollutantDatas) {
                                            String PollutantCode = pollutantData.getString("PollutantCode");
                                            if (PollutantCode.equals(code)) {
                                                if (pollutantData.get("AvgStrength") != null && !"".equals(pollutantData.get("AvgStrength").toString())) {
                                                    if (Double.parseDouble(pollutantData.get("AvgStrength").toString()) > 0 || Double.parseDouble(pollutantData.get("AvgStrength").toString()) < 0) {
                                                        value = value + Double.parseDouble(pollutantData.get("AvgStrength").toString());
                                                        num += 1;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (num > 0) {
                                    Map<String, Object> onecodemap = new HashMap<>();
                                    onecodemap.put("winddirectioncode", "");
                                    onecodemap.put("winddirectionname", "");
                                    onecodemap.put("windirectionvalue", "");
                                    onecodemap.put("monitortime", time);
                                    onecodemap.put("monitorvalue", DataFormatUtil.formatDoubleSaveTwo(value / num));
                                    onelist.add(onecodemap);
                                    codemap.put(code, onelist);
                                }
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", codemap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/4/4 12:26
     * @Description: 通过自定义参数统计气象监测点位的监测数据及气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countMeteoPollutantWeatherDataByParamMap", method = RequestMethod.POST)
    public Object countMeteoPollutantWeatherDataByParamMap(
            @RequestJson(value = "dgimn", required = false) String dgimn,
            @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            List<String> mns = new ArrayList<>();
            if (dgimn != null) {
                mns.add(dgimn);
            } else {
                paramMap.put("monitorpointtype", meteoEnum.getCode());
                List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                            mns.add(map.get("dgimn").toString());
                        }
                    }
                }
            }
            resultMap = OnlineMeteoService.countWeatherAndMeteoMonitorPointDataByParamMap(mns, pollutantcodes, starttime, endtime);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/29 11:07
     * @Description: 将24小时通过分钟分割
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getHMTimesSpiltMFor24Hours() {
        Date date = DataFormatUtil.parseDate("2000-01-01 00:05:00");
        Date date1 = DataFormatUtil.parseDate("2000-01-01 23:55:00");
        List<String> times = DataFormatUtil.getIntervalTimeStringList(date, date1, 10);
        return times.stream().map(time -> time.substring(11, 16)).collect(Collectors.toList());
    }

    public static List<String> findDates(Date dStart, Date dEnd) {
        Calendar cStart = Calendar.getInstance();
        cStart.setTime(dStart);

        List dateList = new ArrayList();
        //别忘了，把起始日期加上
        dateList.add(DataFormatUtil.getDateYMD(dStart));
        // 此日期是否在指定日期之后
        while (dEnd.after(cStart.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            cStart.add(Calendar.DAY_OF_MONTH, 1);
            dateList.add(DataFormatUtil.getDateYMD(cStart.getTime()));
        }
        return dateList;
    }


    /**
     * @author: chengzq
     * @date: 2020/4/10 0010 上午 10:58
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取气象多点位多污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, outputids, pollutantcodes, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getMeteoManyOutPutManyPollutantMonitorDataByParams", method = RequestMethod.POST)
    public Object getMeteoManyOutPutManyPollutantMonitorDataByParams(
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", charDataList);
    }


    /**
     * @author: chengzq
     * @date: 2020/5/11 0011 下午 4:36
     * @Description: 获取一段时间内气象站监测点风向偏向于哪个方向及风速等信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getMeteoWindDeviationInfoByParams", method = RequestMethod.POST)
    public Object getMeteoWindDeviationInfoByParams(@RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime) {
        try {
            List<String> paramList = new ArrayList<>();
            Integer datamark = 2;
            DecimalFormat decimalFormat = new DecimalFormat("0.###");
            Map<String, String> outPutIdAndMn = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(paramList, monitorPointTypeCode, outPutIdAndMn);
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
            paramMap.put("pollutantcodes", Arrays.asList(WindDirectionEnum.getCode(), WindSpeedEnum.getCode()));
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            for (Document document : documents) {
                Object minuteDataList = document.get("MinuteDataList");
                if (minuteDataList != null) {
                    List<Map<String, Object>> datalist = (List<Map<String, Object>>) minuteDataList;
                    datalist.stream().filter(m -> m.get("PollutantCode") != null && m.get("AvgStrength") != null).forEach(m -> {
                        String pollutantCode = m.get("PollutantCode").toString();
                        if(pollutantCode.equals(WindDirectionEnum.getCode())){
                            //将风向值转换成风向名称
                            document.put("WindDirectionName", DataFormatUtil.windDirectionSwitch(Double.valueOf(m.get("AvgStrength").toString()),"code"));
                            document.put("WindDirection", Double.valueOf(m.get("AvgStrength").toString()));
                        }else if(pollutantCode.equals(WindSpeedEnum.getCode())){
                            document.put("WindSpeed", Double.valueOf(m.get("AvgStrength").toString()));
                        }
                    });
                }
            }

            Map<String, List<Document>> collect = documents.stream().filter(m -> m.get("WindDirectionName") != null).collect(Collectors.groupingBy(m -> m.get("WindDirectionName").toString()));

            //获取最大次数的风向
            Optional<String> max = collect.entrySet().stream().max(Comparator.comparing(m -> m.getValue().size())).map(m -> m.getKey());
            if(max.isPresent()){
                String WindDeviation = max.get();
                //风向平均值
                Double WindDirection = collect.entrySet().stream().filter(m -> WindDeviation.equals(m.getKey())).flatMap(m -> m.getValue().stream()).
                        filter(m -> m.get("WindDirection") != null).collect(Collectors.averagingDouble(m -> Double.valueOf(m.get("WindDirection").toString())));
                //风速平均值
                Double WindSpeed = collect.entrySet().stream().filter(m -> WindDeviation.equals(m.getKey())).flatMap(m -> m.getValue().stream()).
                        filter(m -> m.get("WindSpeed") != null).collect(Collectors.averagingDouble(m -> Double.valueOf(m.get("WindSpeed").toString())));
                resultMap.put("WindDeviation",WindDeviation);//偏向哪个方位
                resultMap.put("WindDirection",decimalFormat.format(WindDirection));//风向
                resultMap.put("WindSpeed",decimalFormat.format(WindSpeed));//风速

            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", null);
    }



    /**
     * @author: chengzq
     * @date: 2020/9/10 0010 下午 1:51
     * @Description: 获取气象站点风场数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getMeteoOnlineDataByParams", method = RequestMethod.POST)
    public Object getMeteoOnlineDataByParams(@RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "datamark") Integer datamark) {
        try {
            List<Map<String,Object>> datalist=new ArrayList<>();
            Map<String,Object> paramMap=new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0");
            DecimalFormat decimalFormat1 = new DecimalFormat("0.##");
            paramMap.put("monitorpointtype",monitorPointTypeCode);
            List<Map<String, Object>> listdata = otherMonitorPointService.getOtherMonitorPointInfoAndStateByparamMap(paramMap);
            List<String> mns = listdata.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            String collection = MongoDataUtils.getCollectionByDataMark(datamark);
            if (StringUtils.isNotBlank(starttime)) {
                starttime=JSONObjectUtil.getStartTime(starttime);
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = JSONObjectUtil.getEndTime(endtime);
                paramMap.put("endtime", endtime);
            }
            String pollutantDataKey = MongoDataUtils.getPollutantDataKeyByCollection(collection);

            paramMap.put("collection", collection);
            paramMap.put("mns", mns);
            paramMap.put("pollutantcodes", Arrays.asList(WindDirectionEnum.getCode(), WindSpeedEnum.getCode()));
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);

            Map<String, List<Document>> collect = documents.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String dgimn : collect.keySet()) {
                Map<String,Object> data=new HashMap<>();
                List<Document> documentList = collect.get(dgimn);
                //风向
                Double WindDirection = documentList.stream().filter(m -> m.get(pollutantDataKey) != null).flatMap(m -> ((List<Map<String, Object>>) (m.get(pollutantDataKey))).stream()).filter(m -> m.get("PollutantCode") != null
                        && WindDirectionEnum.getCode().equals(m.get("PollutantCode")) && m.get("AvgStrength") != null).map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));
                Double WindSpeed = documentList.stream().filter(m -> m.get(pollutantDataKey) != null).flatMap(m -> ((List<Map<String, Object>>) (m.get(pollutantDataKey))).stream()).filter(m -> m.get("PollutantCode") != null
                        && WindSpeedEnum.getCode().equals(m.get("PollutantCode")) && m.get("AvgStrength") != null).map(m -> Double.valueOf(m.get("AvgStrength").toString())).collect(Collectors.averagingDouble(m -> m));
                Map<String, Object> output = listdata.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).findFirst().orElse(new HashMap<>());

                data.put("dgimn",dgimn);
                data.put("longitude",output.get("longitude"));
                data.put("latitude",output.get("latitude"));
                data.put("windspeed",decimalFormat1.format(WindSpeed));
                data.put("winddirection",decimalFormat.format(WindDirection));
                datalist.add(data);

            }

            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", null);
    }

}
