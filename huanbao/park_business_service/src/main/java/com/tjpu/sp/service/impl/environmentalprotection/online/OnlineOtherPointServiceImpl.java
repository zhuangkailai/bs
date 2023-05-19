package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.OnlineOtherPointService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;


@Service
public class OnlineOtherPointServiceImpl implements OnlineOtherPointService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    //小时在线
    private final String hourCollection = "HourData";
    //小时在线
    private final String dayCollection = "DayData";
    //园区小时在线
    private final String parkHourCollection = "ParkHourAQIData";
    //园区日在线
    private final String parkDayCollection = "ParkDayAQIData";
    //站点小时在线
    private final String stationHourCollection = "StationHourAQIData";
    //站点日在线
    private final String stationDayCollection = "StationDayAQIData";

    @Autowired
    private OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper;
    @Autowired
    private WaterOutPutPollutantSetMapper waterOutPutPollutantSetMapper;
    @Autowired
    private GasOutPutPollutantSetMapper gasOutPutPollutantSetMapper;
    @Autowired
    private AirStationPollutantSetMapper airStationPollutantSetMapper;
    @Autowired
    private WaterStationPollutantSetMapper waterStationPollutantSetMapper;

    /**
     * @author: xsm
     * @date: 2022/04/20 0020 下午 16:26
     * @Description: 获取其它监测点监测的污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPointMonitorPollutantDataParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointPollutantSetMapper.getPointMonitorPollutantDataParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/04/20 0020 下午 16:06
     * @Description: 获取某点位某时间段内各污染物小时浓度趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @Override
    public Map<String, Object> getOtherMonitorPointHourOnlineDataByParams(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        String parkorpoint = (String) paramMap.get("parkorpoint");

        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        List<String> codes = (List<String>) paramMap.get("codes");
        Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
        Map<String, Object> mnandairmn = (Map<String, Object>) paramMap.get("mnandairmn");
        Map<String, Object> airmnandname = (Map<String, Object>) paramMap.get("airmnandname");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        //获取所有小时/日 日期
        List<String> times = new ArrayList<>();
        String timeliststr = "";
        String collection = "";
        String timestr = "";
        Date startDate = null;
        Date endDate = null;
        if ("hour".equals(datatype)) {
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            timeliststr = "HourDataList";
            collection = hourCollection;
            timestr = "%Y-%m-%d %H";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        } else if ("day".equals(datatype)) {
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            timeliststr = "DayDataList";
            collection = dayCollection;
            timestr = "%Y-%m-%d";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        }
        times.add(endtime);
        //空气污染物 浓度值
        Map<String, Object> timeandairvalue = new HashMap<>();
        List<Map<String, Object>> airlist = new ArrayList<>();
        //空气污染物 浓度值
        Map<String, Map<String, Object>> point_timevalue = new HashMap<>();
        Map<String, List<Map<String, Object>>> point_airvlues = new HashMap<>();
        if (paramMap.get("aircode")!=null){
            String aircode = (String) paramMap.get("aircode");
            if ("park".equals(parkorpoint)) {
                getParkHourData(airlist, timeandairvalue, aircode, startDate, endDate, datatype);
            } else if ("point".equals(parkorpoint)) {
                getAirPointHourData(point_timevalue, point_airvlues, startDate, endDate, paramMap);
            }
        }

        if (dgimns.size() > 0) {
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate).and(timeliststr + ".PollutantCode").in(codes);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("code", "$PollutantCode");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind(timeliststr));
            operations.add(match(Criteria.where(timeliststr + ".PollutantCode").in(codes)));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(timeliststr + ".PollutantCode").as("PollutantCode")
                    .and(timeliststr + ".AvgStrength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime")
                    .push(valuemap).as("valuelist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            //按mn分组
            Map<String, List<Document>> mn_document = mappedResults.stream().filter(m -> m != null && m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            List<Document> mndatalist;
            List<Document> valuelist;
            for (String mn : dgimns) {
                Map<String, Object> mn_valuemap = new HashMap<>();
                mn_valuemap.put("dgmin", mn);
                mn_valuemap.put("pointname", mnandname.get(mn));
                if (paramMap.get("aircode")!=null) {
                    if (parkorpoint != null) {
                        if ("point".equals(parkorpoint)) {
                            if (mnandairmn.get(mn) != null) {
                                mn_valuemap.put("airmn", mnandairmn.get(mn));
                                mn_valuemap.put("airname", airmnandname.get(mnandairmn.get(mn)));
                                mn_valuemap.put("air_data", point_airvlues.get(mn));
                            } else {
                                mn_valuemap.put("airmn", "");
                                mn_valuemap.put("airname", "");
                                mn_valuemap.put("air_data", new ArrayList<>());
                            }
                        }
                    }
                    if (point_timevalue.get(mn) != null) {
                        timeandairvalue = point_timevalue.get(mn);
                    }
                }
                Map<String, List<Map<String, Object>>> objmap = new HashMap<>();
                List<Map<String, Object>> mn_valuelist = new ArrayList<>();
                //获取该点位监测的污染物
                for (String codestr : codes) {
                    objmap.put(codestr, new ArrayList<>());
                }
                if (mn_document.get(mn) != null) {
                    mndatalist = mn_document.get(mn);
                    for (String time : times) {
                        Double total = 0d;
                        for (Document document : mndatalist) {
                            if (time.equals(document.getString("MonitorTime"))) {
                                valuelist = (List<Document>) document.get("valuelist");
                                for (Document doc : valuelist) {
                                    if (doc.get("value") != null) {
                                        total += Double.valueOf(doc.get("value").toString());
                                    }
                                }
                            }
                        }
                        if (total > 0) {
                            for (Document document : mndatalist) {
                                if (time.equals(document.getString("MonitorTime"))) {
                                    valuelist = (List<Document>) document.get("valuelist");
                                    for (Document doc : valuelist) {
                                        if (doc.get("value") != null) {
                                            Map<String, Object> onemap = new HashMap<>();
                                            onemap.put("time", time);
                                            onemap.put("value", doc.get("value"));
                                            onemap.put("proportion", DataFormatUtil.SaveOneAndSubZero(Double.valueOf(doc.get("value").toString()) * 100 / total));
                                            if (paramMap.get("aircode")!=null) {
                                                if (timeandairvalue.get(time) != null) {
                                                    onemap.put("gx_value", DataFormatUtil.SaveOneAndSubZero(Double.valueOf(doc.get("value").toString()) * 100 / Double.valueOf(timeandairvalue.get(time).toString())));
                                                } else {
                                                    onemap.put("gx_value", "");
                                                }
                                            }
                                            List<Map<String, Object>> onepolist = objmap.get(doc.getString("code"));
                                            onepolist.add(onemap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (objmap.size() > 0) {
                    for (String key : objmap.keySet()) {
                        Map<String, Object> obj = new HashMap<>();
                        obj.put("code", key);
                        obj.put("name", codeandname.get(key));
                        obj.put("valuelist", objmap.get(key));
                        mn_valuelist.add(obj);
                    }
                }
                mn_valuemap.put("data", mn_valuelist);
                result.add(mn_valuemap);
            }
        }
        resultmap.put("zb_data", result);
        if (paramMap.get("aircode")!=null) {
            resultmap.put("air_data", airlist);
        }
        return resultmap;
    }


    @Override
    public List<Document> getMongoDBListByParam(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("dgimns");
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        String dataKey = paramMap.get("datakey").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate).and(dataKey + ".PollutantCode").in(pollutantcodes);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("time", "$MonitorTime");
        valuemap.put("value", "$AvgStrength");
        operations.add(unwind(dataKey));
        operations.add(match(Criteria.where(dataKey + ".PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime")
                .and(dataKey + ".PollutantCode").as("PollutantCode")
                .and(dataKey + ".AvgStrength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("DataGatherCode", "PollutantCode")
                .push(valuemap).as("valuelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        String collection = paramMap.get("collection").toString();
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        return documents;
    }

    /**
     * @Description: 获取组分数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/25 11:08
     */
    @Override
    public Map<String, Object> getOtherOnlineGroupDataByParams(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        Integer monitorpointtype = (Integer) paramMap.get("monitorpointtype");
        String parkorpoint = (String) paramMap.get("parkorpoint");
        String aircode = (String) paramMap.get("aircode");
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        List<String> codes = (List<String>) paramMap.get("codes");

        Map<String, Object> mnandairmn = (Map<String, Object>) paramMap.get("mnandairmn");
        Map<String, Object> airmnandname = (Map<String, Object>) paramMap.get("airmnandname");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        Map<String, Integer> codeAndType = (Map<String, Integer>) paramMap.get("codeAndType");
        //获取所有小时/日 日期
        List<String> times = new ArrayList<>();
        String timeliststr = "";
        String collection = "";
        String timestr = "";
        Date startDate = null;
        Date endDate = null;
        if ("hour".equals(datatype)) {
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            timeliststr = "HourDataList";
            collection = hourCollection;
            timestr = "%Y-%m-%d %H";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        } else if ("day".equals(datatype)) {
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            timeliststr = "DayDataList";
            collection = dayCollection;
            timestr = "%Y-%m-%d";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        }
        times.add(endtime);
        //空气污染物 浓度值
        Map<String, Object> timeandairvalue = new HashMap<>();
        List<Map<String, Object>> airlist = new ArrayList<>();
        //空气污染物 浓度值
        Map<String, Map<String, Object>> point_timevalue = new HashMap<>();
        Map<String, List<Map<String, Object>>> point_airvlues = new HashMap<>();
        if ("park".equals(parkorpoint)) {
            getParkHourData(airlist, timeandairvalue, aircode, startDate, endDate, datatype);
        } else if ("point".equals(parkorpoint)) {
            getAirPointHourData(point_timevalue, point_airvlues, startDate, endDate, paramMap);
        }
        if (dgimns.size() > 0) {
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate).and(timeliststr + ".PollutantCode").in(codes);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("code", "$PollutantCode");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind(timeliststr));
            operations.add(match(Criteria.where(timeliststr + ".PollutantCode").in(codes)));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(timeliststr + ".PollutantCode").as("PollutantCode")
                    .and(timeliststr + ".AvgStrength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime")
                    .push(valuemap).as("valuelist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            //按mn分组
            Map<String, List<Document>> mn_document = mappedResults.stream().filter(m -> m != null && m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            List<Document> mndatalist;
            List<Document> valuelist;
            String pollutantCode;
            Double value;
            Integer type;
            for (String mn : dgimns) {
                Map<String, Object> mn_valuemap = new HashMap<>();
                mn_valuemap.put("dgmin", mn);
                mn_valuemap.put("pointname", mnandname.get(mn));
                if ("point".equals(parkorpoint)) {
                    if (mnandairmn.get(mn) != null) {
                        mn_valuemap.put("airmn", mnandairmn.get(mn));
                        mn_valuemap.put("airname", airmnandname.get(mnandairmn.get(mn)));
                        mn_valuemap.put("air_data", point_airvlues.get(mn));
                    } else {
                        mn_valuemap.put("airmn", "");
                        mn_valuemap.put("airname", "");
                        mn_valuemap.put("air_data", new ArrayList<>());
                    }
                }
                if (point_timevalue.get(mn) != null) {
                    timeandairvalue = point_timevalue.get(mn);
                }
                Map<Integer, List<Map<String, Object>>> typeAndListMap = new HashMap<>();
                List<Map<String, Object>> mn_valuelist = new ArrayList<>();
                //获取该点位监测的污染物
                for (String codestr : codes) {
                    type = codeAndType.get(codestr);
                    typeAndListMap.put(type, new ArrayList<>());
                }
                if (mn_document.get(mn) != null) {
                    mndatalist = mn_document.get(mn);
                    for (String time : times) {
                        Double total = 0d;
                        for (Document document : mndatalist) {
                            if (time.equals(document.getString("MonitorTime"))) {
                                valuelist = (List<Document>) document.get("valuelist");
                                for (Document doc : valuelist) {
                                    if (doc.get("value") != null) {
                                        total += Double.valueOf(doc.get("value").toString());
                                    }
                                }
                            }
                        }
                        if (total > 0) {
                            for (Document document : mndatalist) {
                                if (time.equals(document.getString("MonitorTime"))) {
                                    valuelist = (List<Document>) document.get("valuelist");
                                    Map<Integer, Double> typeAndValue = new HashMap<>();
                                    for (Document doc : valuelist) {
                                        if (doc.get("value") != null) {
                                            value = Double.parseDouble(doc.get("value").toString());
                                            pollutantCode = doc.getString("code");
                                            type = codeAndType.get(pollutantCode);
                                            typeAndValue.put(type, typeAndValue.get(type) != null ? typeAndValue.get(type) + value : value);
                                        }
                                    }
                                    for (Integer typeIndex : typeAndValue.keySet()) {
                                        Map<String, Object> onemap = new HashMap<>();
                                        onemap.put("time", time);
                                        value = typeAndValue.get(typeIndex);
                                        onemap.put("value", DataFormatUtil.SaveThreeAndSubZero(value));
                                        onemap.put("proportion", DataFormatUtil.SaveOneAndSubZero(value * 100 / total));
                                        if (timeandairvalue.get(time) != null) {
                                            onemap.put("gx_value", DataFormatUtil.SaveOneAndSubZero(value * 100 / Double.valueOf(timeandairvalue.get(time).toString())));
                                        } else {
                                            onemap.put("gx_value", "");
                                        }
                                        List<Map<String, Object>> onepolist = typeAndListMap.get(typeIndex);
                                        onepolist.add(onemap);
                                    }
                                }
                            }
                        }
                    }
                }

                if (typeAndListMap.size() > 0) {
                    for (Integer key : typeAndListMap.keySet()) {
                        Map<String, Object> obj = new HashMap<>();
                        obj.put("code", key);
                        obj.put("name", getGroupName(monitorpointtype, key));
                        obj.put("valuelist", typeAndListMap.get(key));
                        mn_valuelist.add(obj);
                    }
                }
                mn_valuemap.put("data", mn_valuelist);
                result.add(mn_valuemap);
            }
        }
        resultmap.put("zb_data", result);
        resultmap.put("air_data", airlist);
        return resultmap;
    }

    @Override
    public List<Map<String, Object>> getPollutantDataParamMap(Map<String, Object> paramMap) {
        return otherMonitorPointPollutantSetMapper.getPollutantDataParamMap(paramMap);
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

    /**
     * 获取园区气象某个污染物一段时间内的浓度值
     */
    private void getParkHourData(List<Map<String, Object>> airlist, Map<String, Object> timeandairvalue, String aircode, Date startDate, Date endDate, String datatype) {
        String collection = "";
        String timestr = "";
        if ("hour".equals(datatype)) {
            collection = parkHourCollection;
            timestr = "%Y-%m-%d %H";
        } else if ("day".equals(datatype)) {
            collection = parkDayCollection;
            timestr = "%Y-%m-%d";
        }
        //List<Map<String,Object>> result = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("MonitorTime").gte(startDate).lte(endDate).and("DataList.PollutantCode").is(aircode);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("DataList"));
        operations.add(match(Criteria.where("DataList.PollutantCode").is(aircode)));
        operations.add(Aggregation.project().and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and("DataList.PollutantCode").as("PollutantCode")
                .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        if (mappedResults.size() > 0) {
            for (Document doc : mappedResults) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", doc.get("MonitorTime"));
                map.put("value", doc.get("AvgStrength"));
                if (doc.get("AvgStrength") != null && !"".equals(doc.get("AvgStrength").toString())) {
                    timeandairvalue.put(doc.get("MonitorTime").toString(), doc.get("AvgStrength"));
                }
                airlist.add(map);
            }
        }
    }

    /**
     * 获取空气点位某个污染物浓度趋势
     */
    private void getAirPointHourData(Map<String, Map<String, Object>> point_timevalue, Map<String, List<Map<String, Object>>> point_airvlues, Date startDate, Date endDate, Map<String, Object> paramMap) {
        String datatype = (String) paramMap.get("datatype");
        String aircode = (String) paramMap.get("aircode");
        List<String> airmns = (List<String>) paramMap.get("airmns");
        if (airmns.size() > 0) {
            Map<String, Object> mnandairmn = (Map<String, Object>) paramMap.get("mnandairmn");
            Map<String, Object> airmnandname = (Map<String, Object>) paramMap.get("airmnandname");
            String collection = "";
            String timestr = "";
            if ("hour".equals(datatype)) {
                collection = stationHourCollection;
                timestr = "%Y-%m-%d %H";
            } else if ("day".equals(datatype)) {
                collection = stationDayCollection;
                timestr = "%Y-%m-%d";
            }
            //List<Map<String,Object>> result = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("StationCode").in(airmns).and("MonitorTime").gte(startDate).lte(endDate).and("DataList.PollutantCode").is(aircode);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(unwind("DataList"));
            operations.add(match(Criteria.where("DataList.PollutantCode").is(aircode)));
            operations.add(Aggregation.project("StationCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and("DataList.PollutantCode").as("PollutantCode")
                    .and("DataList.Strength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            Map<String, List<Document>> mapDocuments = new HashMap<>();
            List<Document> podoc;
            String othermn = "";
            if (mappedResults.size() > 0) {
                //按MN号分组
                mapDocuments = mappedResults.stream().filter(m -> m.get("StationCode") != null).collect(Collectors.groupingBy(m -> m.get("StationCode").toString()));
                for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                    podoc = entry.getValue();
                    Map<String, Object> timeandairvalue = new HashMap<>();
                    List<Map<String, Object>> airlist = new ArrayList<>();
                    for (Document doc : podoc) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("monitortime", doc.get("MonitorTime"));
                        map.put("value", doc.get("AvgStrength"));
                        if (doc.get("AvgStrength") != null && !"".equals(doc.get("AvgStrength").toString())) {
                            timeandairvalue.put(doc.get("MonitorTime").toString(), doc.get("AvgStrength"));
                        }
                        airlist.add(map);
                    }
                    for (Map.Entry<String, Object> airentry : mnandairmn.entrySet()) {
                        if (airentry.getValue() != null && entry.getKey().equals(airentry.getValue().toString())) {
                            othermn = airentry.getKey();
                            break;
                        }
                    }
                    if (!"".equals(othermn)) {
                        point_timevalue.put(othermn, timeandairvalue);
                        point_airvlues.put(othermn, airlist);
                    }
                }
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/07 0007 上午 9:51
     * @Description: 根据监测点ID和监测类型获取排口或监测点设置污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOnePointMonitorPollutantSelectDataByParams(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        int monitorpointtype = Integer.valueOf(paramMap.get("monitorpointtype").toString());
        //废水
        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()){
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("datamark", 1);
            dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantSetInfoByParamMap(paramMap);
        }else if(monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()||
                monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()){
            //废气、烟气
            paramMap.put("pollutanttype", monitorpointtype);
            paramMap.put("unorgflag", false);
            dataList = gasOutPutPollutantSetMapper.getGasOutPutPollutantSetInfoByParam(paramMap);
        }else if(monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()){
           //雨水
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("datamark", 3);
            dataList = waterOutPutPollutantSetMapper.getWaterOrRainPollutantSetInfoByParamMap(paramMap);
        }else if(monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()){
            //空气
            dataList = airStationPollutantSetMapper.getAirStationPollutantSetInfoByParam(paramMap);
        }else if(monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode()){
            //水质
            paramMap.put("monitorpointtype", monitorpointtype);
            dataList =  waterStationPollutantSetMapper.getWaterStationAllPollutantsByIDAndType(paramMap);
        }else{
            //其它监测点类型
            paramMap.put("monitorpointtype",monitorpointtype);
            dataList = otherMonitorPointPollutantSetMapper.getPointMonitorPollutantDataParamMap(paramMap);
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2022/05/07 0007 上午 10:47
     * @Description: 获取多个点位污染物某段时间内各时刻的浓度变化率（对比上个小时/前一天）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPointPollutantChangeRateDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        List<String> codes = (List<String>) paramMap.get("codes");
       // Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
        //Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        //Map<String, Object> mnandcode = (Map<String, Object>) paramMap.get("mnandcode");
        List<Map<String, Object>> pointlist = (List<Map<String, Object>>) paramMap.get("pointlist");
        //获取所有小时/日 日期
        List<String> times = new ArrayList<>();
        String timeliststr = "";
        String collection = "";
        String timestr = "";
        Date startDate = null;
        Date endDate = null;
        //时间往前推一个小时或一天
        starttime = getLastHourOrLastDay(starttime,datatype);
        if ("hour".equals(datatype)) {
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            timeliststr = "HourDataList";
            collection = hourCollection;
            timestr = "%Y-%m-%d %H";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        } else if ("day".equals(datatype)) {
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            timeliststr = "DayDataList";
            collection = dayCollection;
            timestr = "%Y-%m-%d";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        }
        times.add(endtime);
        if (dgimns.size() > 0) {
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate).and(timeliststr + ".PollutantCode").in(codes);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("code", "$PollutantCode");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind(timeliststr));
            operations.add(match(Criteria.where(timeliststr + ".PollutantCode").in(codes)));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(timeliststr + ".PollutantCode").as("PollutantCode")
                    .and(timeliststr + ".AvgStrength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime")
                    .push(valuemap).as("valuelist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            //按mn分组
            Map<String, List<Document>> mn_document = mappedResults.stream().filter(m -> m != null && m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            List<Document> mndatalist;
            List<Document> valuelist;
            String code;
            String lastvalue;
            String mn;
            for (Map<String, Object> map : pointlist) {
                Map<String, Object> mn_valuemap = new HashMap<>();
                mn =map.get("dgimn").toString();
                mn_valuemap.put("dgimn", map.get("dgimn"));
                mn_valuemap.put("pointname", map.get("pointname"));
                code = map.get("pollutantcode")!=null?map.get("pollutantcode").toString():"";
                mn_valuemap.put("pollutantcode", code);
                mn_valuemap.put("pollutantname", map.get("pollutantname"));
                List<Map<String, Object>> mn_valuelist = new ArrayList<>();
                //获取该点位查询的污染物
                if (mn_document.get(mn) != null) {
                    lastvalue = "";
                    mndatalist = mn_document.get(mn);
                    for (int i = 0;i<times.size();i++){
                        for (Document document : mndatalist) {
                            if (times.get(i).equals(document.getString("MonitorTime"))) {
                                valuelist = (List<Document>) document.get("valuelist");
                                for (Document doc : valuelist) {
                                    if (code.equals(doc.getString("code"))&&doc.get("value") != null) {
                                        if (i==0){
                                            lastvalue = doc.getString("value");
                                        }else{
                                            Map<String, Object> onemap = new HashMap<>();
                                            onemap.put("time", times.get(i));
                                            onemap.put("value", doc.get("value"));
                                            onemap.put("lastvalue", lastvalue);
                                            if (!"".equals(lastvalue)) {
                                                //变化率  = 当前值 - 上个小时/或前一天 的值   除以 上个小时/或前一天 的值
                                                if (Double.valueOf(lastvalue)>0){
                                                    if (Double.valueOf(doc.get("value").toString())>0) {
                                                        onemap.put("changerate", DataFormatUtil.SaveOneAndSubZero((Double.valueOf(doc.get("value").toString()) - Double.valueOf(lastvalue)) / Double.valueOf(lastvalue)));
                                                    }else{
                                                        onemap.put("changerate", "-100");
                                                    }
                                                }else{
                                                    if (Double.valueOf(doc.get("value").toString())>0){
                                                        onemap.put("changerate", "100");
                                                    }else{
                                                        onemap.put("changerate", "0");
                                                    }
                                                }
                                            }else{
                                                //上个小时 或前一天无值 且当前值 >0 则变化率为100
                                                if (Double.valueOf(doc.get("value").toString())>0){
                                                    onemap.put("changerate", "100");
                                                }else{
                                                    onemap.put("changerate", "0");
                                                }
                                            }
                                            mn_valuelist.add(onemap);
                                            lastvalue = doc.get("value").toString();
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                mn_valuemap.put("data", mn_valuelist);
                result.add(mn_valuemap);
            }
        }
        return result;
    }

    /**
     * 获取上个小时或前一天的时间
     * */
    private String getLastHourOrLastDay(String starttime, String datatype) {
        //getInstance()将返回一个Calendar的对象。
        Date onedate =null;
        int i =0;
        SimpleDateFormat df =null;
        if ("hour".equals(datatype)){
            i=1;
            onedate = DataFormatUtil.getDateYMDHMS(starttime+":00:00");
            //格式化时间
            df = new SimpleDateFormat("yyyy-MM-dd HH");
        }else if("day".equals(datatype)){
            i=24;
            onedate = DataFormatUtil.getDateYMDHMS(starttime+" 00:00:00");
            //格式化时间
            df = new SimpleDateFormat("yyyy-MM-dd");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(onedate);
        //设置小时
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - i);
        return df.format(calendar.getTime());
    }

    /**
     * @author: xsm
     * @date: 2022/05/07 0007 上午 10:47
     * @Description: 获取多个点位污染物某段时间内各时刻的浓度对比值（分子/分母）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPointPollutantContrastValueDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) paramMap.get("starttime");
        String endtime = (String) paramMap.get("endtime");
        String datatype = (String) paramMap.get("datatype");
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        List<String> codes = (List<String>) paramMap.get("codes");
        List<Map<String, Object>> pointlist = (List<Map<String, Object>>) paramMap.get("pointlist");
        //获取所有小时/日 日期
        List<String> times = new ArrayList<>();
        String timeliststr = "";
        String collection = "";
        String timestr = "";
        Date startDate = null;
        Date endDate = null;
        if ("hour".equals(datatype)) {
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            timeliststr = "HourDataList";
            collection = hourCollection;
            timestr = "%Y-%m-%d %H";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        } else if ("day".equals(datatype)) {
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            timeliststr = "DayDataList";
            collection = dayCollection;
            timestr = "%Y-%m-%d";
            startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
        }
        times.add(endtime);
        if (dgimns.size() > 0) {
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startDate).lte(endDate).and(timeliststr + ".PollutantCode").in(codes);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("code", "$PollutantCode");
            valuemap.put("value", "$AvgStrength");
            operations.add(unwind(timeliststr));
            operations.add(match(Criteria.where(timeliststr + ".PollutantCode").in(codes)));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(timeliststr + ".PollutantCode").as("PollutantCode")
                    .and(timeliststr + ".AvgStrength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(Aggregation.group("DataGatherCode", "MonitorTime")
                    .push(valuemap).as("valuelist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            //按mn分组
            Map<String, List<Document>> mn_document = mappedResults.stream().filter(m -> m != null && m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            List<Document> fz_mndatalist;
            List<Document> fm_mndatalist;
            List<Document> fz_valuelist;
            List<Document> fm_valuelist;
            String fz_mn;
            String fm_mn;
            String fz_code;
            String fm_code;
            String fz_value;
            String fm_value;
            for (Map<String, Object> map : pointlist) {
                fz_mn = map.get("fz_dgimn").toString();
                fm_mn = map.get("fm_dgimn").toString();
                fz_code = map.get("fz_pollutantcode").toString();
                fm_code = map.get("fm_pollutantcode").toString();
                List<Map<String, Object>> valuelist = new ArrayList<>();
                for (String time : times) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("monitortime", time);
                    fz_value = "";
                    fm_value = "";
                    //获取该点位查询的污染物
                    //分子
                    if (mn_document.get(fz_mn)!=null) {
                        //分子
                        fz_mndatalist = mn_document.get(fz_mn);
                        for (Document document : fz_mndatalist) {
                            if (time.equals(document.getString("MonitorTime"))) {
                                fz_valuelist = (List<Document>) document.get("valuelist");
                                for (Document doc : fz_valuelist) {
                                    if (fz_code.equals(doc.getString("code")) && doc.get("value") != null) {
                                        fz_value = doc.getString("value");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (mn_document.get(fm_mn)!=null) {
                        //分母
                        fm_mndatalist = mn_document.get(fm_mn);
                        //分母
                        for (Document document : fm_mndatalist) {
                            if (time.equals(document.getString("MonitorTime"))) {
                                fm_valuelist = (List<Document>) document.get("valuelist");
                                for (Document doc : fm_valuelist) {
                                    if (fm_code.equals(doc.getString("code")) && doc.get("value") != null) {
                                        fm_value = doc.getString("value");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    onemap.put("fz_value", fz_value);
                    onemap.put("fm_value", fm_value);
                    if (!"".equals(fz_value) && !"".equals(fm_value)) {
                        //判断 分母不能为0
                        if (Double.valueOf(fm_value) > 0) {
                            onemap.put("value", DataFormatUtil.SaveOneAndSubZero(Double.valueOf(fz_value) / Double.valueOf(fm_value)));
                        } else {
                            onemap.put("value", "");
                        }
                    } else {
                        onemap.put("value", "");
                    }
                    valuelist.add(onemap);
                }
                map.put("valuedata", valuelist);
                result.add(map);
            }
        }
        return result;
    }
}
