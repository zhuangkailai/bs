package com.tjpu.sp.service.impl.environmentalprotection.upgradefunction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.DeviceStatusMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GasOutPutInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutputInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterStationMapper;
import com.tjpu.sp.dao.environmentalprotection.navigation.NavigationStandardMapper;
import com.tjpu.sp.dao.environmentalprotection.output.UserMonitorPointRelationDataMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.service.environmentalprotection.upgradefunction.WallChartOperationService;
import com.tjpu.sp.service.impl.environmentalprotection.online.OnlineServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Service
@Transactional
public class WallChartOperationServiceImpl implements WallChartOperationService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    //小时排放量
    private final String hourFlowCollection = "HourFlowData";
    private final String dayFlowCollection = "DayFlowData";
    private final String monthFlowCollection = "MonthFlowData";
    private final String yearFlowCollection = "YearFlowData";

    private final String cityhourCollection = "CityHourAQIData";
    private final String parkhourCollection = "ParkHourAQIData";
    private final String citydayCollection = "CityDayAQIData";
    private final String parkdayCollection = "ParkDayAQIData";
    //
    private final String db_LatestData = "LatestData";
    //小时表
    private final String db_hourData = "HourData";
    //日表
    private final String db_dayData = "DayData";
    //分钟在线
    private final String minuteCollection = "MinuteData";
    private final String overModelCollection = "OverModel";
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private NavigationStandardMapper navigationStandardMapper;
    @Autowired
    private PollutionMapper pollutionMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;
    @Autowired
    private OnlineServiceImpl onlineServiceImpl;
    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private DeviceStatusMapper deviceStatusMapper;
    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private UserMonitorPointRelationDataMapper userMonitorPointRelationDataMapper;


    /**
     * @author: xsm
     * @date: 2022/02/15 08:33
     * @Description: 通过自定义参数获取所有点位单个污染物每小时的总排放量
     * @updateUser:xsm
     * @updateDate:2022/05/10
     * @updateDescription: 新增日排放量查询
     * @param:starttime yyyy-dd-mm HH  endtime yyyy-dd-mm HH
     * @return:
     */
    @Override
    public List<Map<String, Object>> countOnePollutantDischargeRankByParam(Map<String, Object> parammap) {
        //排口流量因子代码集合（废水，废气因子代码不同）
        List<String> flowCode = Arrays.asList("b01", "b02");
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = parammap.get("starttime").toString();
        String endtime = parammap.get("endtime").toString();
        String pollutantcode = parammap.get("pollutantcode").toString();
        String datatype = parammap.get("datatype").toString();
        List<String> dgimns = (List<String>) parammap.get("dgimns");
        String collection = "";
        String liststr = "";
        String flowkey = "";
        if ("hour".equals(datatype)) {
            collection = hourFlowCollection;
            liststr = "HourFlowDataList";
            flowkey = "AvgFlow";
        } else if ("day".equals(datatype)) {
            collection = dayFlowCollection;
            liststr = "DayFlowDataList";
            flowkey = "AvgFlow";
        } else if ("month".equals(datatype)) {
            collection = monthFlowCollection;
            liststr = "MonthFlowDataList";
            flowkey = "PollutantFlow";
        } else if ("year".equals(datatype)) {
            collection = yearFlowCollection;
            liststr = "YearFlowDataList";
            flowkey = "PollutantFlow";
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.parseDate(starttime + ":00:00")).lte(DataFormatUtil.parseDate(endtime + ":00:00"));
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        Map<String, Object> pfl_map = new HashMap<>();
        if (flowCode.contains(pollutantcode)) {
            operations.add(unwind(liststr));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime", "TotalFlow", "FlowUnit").andExclude("_id"));
            pfl_map.put("flownum", "$TotalFlow");
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(group("MonitorTime").push(pfl_map).as("pfl_list").last("FlowUnit").as("flowunit"));
        } else {
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(liststr + ".PollutantCode").as("PollutantCode").and(liststr + "." + flowkey).as("AvgFlow").andExclude("_id"));
            pfl_map.put("flownum", "$AvgFlow");
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            operations.add(group("MonitorTime").push(pfl_map).as("pfl_list"));
        }

        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        for (Document document : mappedResults) {
            Map<String, Object> onemap = new HashMap<>();
            if ("hour".equals(datatype)) {
                onemap.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("_id")));
                onemap.put("timestr", DataFormatUtil.getDateYMDH(document.getDate("_id")).substring(11, 13) + "时");
            } else if ("day".equals(datatype)) {
                onemap.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("_id")));
                onemap.put("timestr", DataFormatUtil.getDateYMDH(document.getDate("_id")).substring(8, 10) + "日");
            } else if ("month".equals(datatype)) {
                onemap.put("monitortime", DataFormatUtil.getDateYM(document.getDate("_id")));
                onemap.put("timestr", DataFormatUtil.getDateYMDH(document.getDate("_id")).substring(5, 7) + "月");
            } else if ("year".equals(datatype)) {
                onemap.put("monitortime", DataFormatUtil.getDateY(document.getDate("_id")));
                onemap.put("timestr", DataFormatUtil.getDateYMDH(document.getDate("_id")).substring(0, 4) + "年");
            }
            List<Document> pfl_list = (List<Document>) document.get("pfl_list");
            Double total = 0d;
            for (Document pfl_document : pfl_list) {
                if (pfl_document.get("flownum") != null && !"".equals(pfl_document.getString("flownum"))) {
                    total += Double.valueOf(pfl_document.getString("flownum"));
                }
            }
            if (flowCode.contains(pollutantcode)) {
                onemap.put("unit", document.get("flowunit"));
                onemap.put("pfl_num", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total)));
            } else {
                if ("hour".equals(datatype)) {
                    onemap.put("unit", "kg");
                    onemap.put("pfl_num", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total)));
                } else {
                    onemap.put("unit", "t");
                    onemap.put("pfl_num", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total)));
                }
            }
            result.add(onemap);
        }
        if (result != null && result.size() > 0) {
            //根据时间排序
            result = result.stream().sorted(Comparator.comparing((Map m) -> (m.get("monitortime").toString()))).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的浓度值及浓度排名情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime datatype:数据类型(1:实时/2：分钟/3：小时)
     * @return:
     */
    @Override
    public List<Map<String, Object>> countEnvPointPollutantConcentrationRankByParam(Map<String, Object> parammap) {
        List<Map<String, Object>> result = new LinkedList<>();
        String monitortime = parammap.get("monitortime").toString();
        String pollutantcode = parammap.get("pollutantcode").toString();
        String othersort = parammap.get("othersort").toString();
        String sortorder = parammap.get("sortorder").toString();
        Map<String, Map<String, Object>> mnandpointinfo = (Map<String, Map<String, Object>>) parammap.get("mnandpointinfo");
        List<String> dgimns = (List<String>) parammap.get("dgimns");
        Integer datatype = Integer.valueOf(parammap.get("datatype").toString());
        //判断该污染物是否为折算污染物
        List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(parammap);
        boolean isconvert = false;
        String pollutantname = "";
        if (allpollutants != null) {
            for (Map<String, Object> pomap : allpollutants) {
                if (pomap.get("name") != null) {
                    pollutantname = pomap.get("name").toString();
                }
                if (pomap.get("IsHasConvertData") != null && !"".equals(pomap.get("IsHasConvertData").toString()) && Integer.valueOf(pomap.get("IsHasConvertData").toString()) == 1) {
                    isconvert = true;
                }
            }
        }
        Date starttime = null;
        Date endtime = null;
        String liststr = "";
        String coolection = "";
        String valuestr = "";
        String convertstr = "";
        if (2 == datatype) {//分钟
            starttime = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
            endtime = DataFormatUtil.getDateYMDHMS(monitortime + ":59");
            liststr = "MinuteDataList";
            coolection = "MinuteData";
            valuestr = "AvgStrength";
            convertstr = "AvgConvertStrength";
        } else if (3 == datatype) {//小时
            starttime = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
            endtime = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
            liststr = "HourDataList";
            coolection = "HourData";
            valuestr = "AvgStrength";
            convertstr = "AvgConvertStrength";
        } else if (4 == datatype) {//日
            starttime = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
            endtime = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
            liststr = "DayDataList";
            coolection = "DayData";
            valuestr = "AvgStrength";
            convertstr = "AvgConvertStrength";
        }
        boolean isFlow = parammap.get("isflow") != null ? Boolean.parseBoolean(parammap.get("isflow").toString()) : false;
        //排口流量因子代码集合（废水，废气因子代码不同）
        List<String> flowCode = Arrays.asList("b01", "b02");
        Map<String, Object> mnAndFlow;
        List<Document> mappedResults;
        if (!isFlow) {
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(starttime).lte(endtime);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime")
                    .and(liststr + ".IsOver").as("isover")
                    .and(liststr + ".IsException").as("isexception")
                    .and(liststr + ".Flag").as("Flag")
                    .and(liststr + ".IsOverStandard").as("isoverstandard")
                    .and(liststr + ".PollutantCode").as("PollutantCode")
                    .and(liststr + "." + convertstr).as("convertvalue")
                    .and(liststr + "." + valuestr).as("value").andExclude("_id"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
            mappedResults = pageResults.getMappedResults();
            mnAndFlow = new HashMap<>();
        } else {
            mappedResults = new ArrayList<>();
            mnAndFlow = getMnAndFlow(monitortime, pollutantcode, dgimns, datatype);
        }

        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (mappedResults.size() > 0) {
            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", parammap.get("monitorpointtypes"));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }


        for (String mn : dgimns) {
            Map<String, Object> onemap = mnandpointinfo.get(mn);
            onemap.put("pollutantname", pollutantname);
            onemap.put("monitortime", monitortime);
            onemap.put("value", null);
            onemap.put("flowvalue", mnAndFlow.get(mn));
            int isover = -1;
            boolean isexception = false;
            String flag = "";
            for (Document document : mappedResults) {
                if (mn.equals(document.getString("DataGatherCode"))) {
                    if (onemap.get("fkmonitorpointtypecode") != null && isconvert == true &&
                            onemap.get("fkmonitorpointtypecode").toString().equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {
                        onemap.put("value", document.get("convertvalue") != null ? document.getString("convertvalue") : null);
                    } else {
                        onemap.put("value", document.get("value") != null ? document.getString("value") : null);
                    }

                    if (document.get("isover") != null) {
                        isover = document.getInteger("isover");
                    }
                    if (document.get("isoverstandard") != null && document.getBoolean("isoverstandard") == true) {
                        isover = 4;
                    }
                    if (document.get("isexception") != null && document.getInteger("isexception") > 0) {
                        isexception = true;
                    }
                    flag = document.get("Flag") != null ? document.get("Flag").toString().toLowerCase() : "";
                    break;
                }
            }
            onemap.put("flag", flag_codeAndName.get(flag));
            onemap.put("isover", isover);
            onemap.put("isexception", isexception);
            result.add(onemap);
        }
        if (result.size() > 0) {
            if (!"".equals(othersort)) {
                if ("value".equals(othersort)) {
                    if ("asc".equals(sortorder)) {
                        if (isFlow) {
                            result = result.stream().sorted(
                                    Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), "flowvalue"))
                            ).collect(Collectors.toList());
                        } else {
                            result = result.stream().sorted(
                                    Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), "value"))
                            ).collect(Collectors.toList());
                        }

                    } else {
                        if (isFlow) {
                            result = result.stream().sorted(
                                    Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), "flowvalue")).reversed()
                            ).collect(Collectors.toList());

                        } else {
                            result = result.stream().sorted(
                                    Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), "value")).reversed()
                            ).collect(Collectors.toList());
                        }
                    }

                } else {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString())).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString()).reversed()).collect(Collectors.toList());
                    }
                }

            }
        }
        return result;
    }

    /**
     * 小时排放或日排放
     */
    private Map<String, Object> getMnAndFlow(String monitortime, String pollutantcode, List<String> dgimns, Integer datatype) {
        //排口流量因子代码集合（废水，废气因子代码不同）
        List<String> flowCode = Arrays.asList("b01", "b02");
        String collection = "";
        String liststr = "";
        Criteria criteria = new Criteria();
        if (3 == datatype) {
            collection = hourFlowCollection;
            liststr = "HourFlowDataList";
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + ":00:00")).lte(DataFormatUtil.parseDate(monitortime + ":00:00"));
        } else if (4 == datatype) {
            collection = dayFlowCollection;
            liststr = "DayFlowDataList";
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + " 00:00:00")).lte(DataFormatUtil.parseDate(monitortime + " 23:00:00"));
        }
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if (flowCode.contains(pollutantcode)) {
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and("TotalFlow").as("AvgFlow").andExclude("_id"));
        } else {
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(liststr + ".PollutantCode").as("PollutantCode").and(liststr + ".AvgFlow").as("AvgFlow").andExclude("_id"));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        Map<String, Object> mnAndFlow = new HashMap<>();
        String mnCommon;
        Double value;
        for (Document document : mappedResults) {
            if (document.get("AvgFlow") != null && !"".equals(document.getString("AvgFlow"))) {
                mnCommon = document.getString("DataGatherCode");
                value = Double.parseDouble(document.getString("AvgFlow"));
                mnAndFlow.put(mnCommon, DataFormatUtil.SaveThreeAndSubZero(value));
            }
        }
        return mnAndFlow;
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

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物最新的浓度值及浓度排名情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<Map<String, Object>> countEnvPointPollutantLastDataRankByParam(Map<String, Object> parammap) {
        List<Map<String, Object>> result = new LinkedList<>();
        String pollutantcode = parammap.get("pollutantcode").toString();
        String othersort = parammap.get("othersort").toString();
        String sortorder = parammap.get("sortorder").toString();
        Map<String, Map<String, Object>> mnandpointinfo = (Map<String, Map<String, Object>>) parammap.get("mnandpointinfo");
        List<String> dgimns = (List<String>) parammap.get("dgimns");
        //判断该污染物是否为折算污染物
        List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(parammap);
        boolean isconvert = false;
        String pollutantname = "";
        if (allpollutants != null) {
            for (Map<String, Object> pomap : allpollutants) {
                if (pomap.get("name") != null) {
                    pollutantname = pomap.get("name").toString();
                }
                if (pomap.get("IsHasConvertData") != null && !"".equals(pomap.get("IsHasConvertData").toString()) && Integer.valueOf(pomap.get("IsHasConvertData").toString()) == 1) {
                    isconvert = true;
                }
            }
        }
        Integer datatype = Integer.valueOf(parammap.get("datatype").toString());
        String liststr = "DataList";
        String coolection = db_LatestData;
        String valuestr = "AvgStrength";
        String convertstr = "CouConvertStrength";
        String type = "";
        if (1 == datatype) {//实时
            type = "RealTimeData";
        } else if (2 == datatype) {//分钟
            type = "MinuteData";
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("Type").is(type);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(liststr));
        operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime")
                .and(liststr + ".IsOver").as("isover")
                .and(liststr + ".IsException").as("isexception")
                .and(liststr + ".Flag").as("Flag")
                .and(liststr + ".IsOverStandard").as("isoverstandard")
                .and(liststr + ".PollutantCode").as("PollutantCode")
                .and(liststr + "." + convertstr).as("convertvalue")
                .and(liststr + "." + valuestr).as("value").andExclude("_id"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (mappedResults.size() > 0) {
            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", parammap.get("monitorpointtypes"));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }


        for (String mn : dgimns) {
            Map<String, Object> onemap = mnandpointinfo.get(mn);
            onemap.put("pollutantname", pollutantname);
            onemap.put("monitortime", "-");
            onemap.put("value", null);
            int isover = -1;
            String flag = "";
            boolean isexception = false;
            for (Document document : mappedResults) {
                if (mn.equals(document.getString("DataGatherCode"))) {
                    if (1 == datatype) {//实时
                        onemap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                    } else if (2 == datatype) {//分钟
                        onemap.put("monitortime", DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime")));
                    }
                    if (onemap.get("fkmonitorpointtypecode") != null && isconvert == true &&
                            onemap.get("fkmonitorpointtypecode").toString().equals(CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() + "")) {
                        onemap.put("value", document.get("convertvalue") != null ? document.getString("convertvalue") : null);
                    } else {
                        onemap.put("value", document.get("value") != null ? document.getString("value") : null);
                    }

                    if (document.get("isover") != null) {
                        isover = document.getInteger("isover");
                    }
                    if (document.get("isoverstandard") != null && document.getBoolean("isoverstandard") == true) {
                        isover = 4;
                    }
                    if (document.get("isexception") != null && document.getInteger("isexception") > 0) {
                        isexception = true;
                    }
                    flag = document.get("Flag") != null ? document.get("Flag").toString().toLowerCase() : "";

                    break;
                }
            }
            onemap.put("flag", flag_codeAndName.get(flag));
            onemap.put("isover", isover);
            onemap.put("isexception", isexception);
            result.add(onemap);
        }
        if (result.size() > 0) {
            if (!"".equals(othersort)) {
                if ("value".equals(othersort)) {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), "value"))
                        ).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), "value")).reversed()
                        ).collect(Collectors.toList());
                    }
                } else {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString())).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString()).reversed()).collect(Collectors.toList());
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 0015 下午 18:01
     * @Description: 自定义查询条件统计环保点位小时风向图表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllPointWindChartData(List<Document> documents, String collection) {
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
        parkList = getAllPointWindData(collection, documents);
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
            for (int j = 0; j < directCodeList.length; j++) {
                Map<String, Object> windDataMap = new HashMap<>();
                if (directioncodeAndSpeedMap.get(directCodeList[j]) != null) {
                    windDataMap.put("windspeednum", directioncodeAndSpeedMap.get(directCodeList[j]).size());
                } else {
                    windDataMap.put("windspeednum", 0);
                }
                windDataMap.put("winddirectioncode", directCodeList[j]);
                windDataMap.put("winddirectionname", directNameList[j]);
                winddatalist.add(windDataMap);
            }
            map.put("winddatalist", winddatalist);
            dataList.add(map);
        }

        return dataList;
    }

    /**
     * 组装风向图（所有点）
     */
    private List<Map<String, Object>> getAllPointWindData(String collection, List<Document> documents) {
        List<Map<String, Object>> parkList = new ArrayList<>();
        Set<String> times = new HashSet<>();
        String[] directCodeList = DataFormatUtil.directCode;
        String[] directNameList = DataFormatUtil.directName;
        Map<String, String> windcodename = new HashMap<>();
        for (int i = 0; i < directNameList.length; i++) {
            windcodename.put(directCodeList[i], directNameList[i]);
        }
        String pollutantDataKey = "";
        if (collection.equals("HourData")) {
            pollutantDataKey = "HourDataList";
        } else if (collection.equals("DayData")) {
            pollutantDataKey = "DayDataList";
        } else if (collection.equals("MinuteData")) {
            pollutantDataKey = "MinuteDataList";
        }
        for (Document document : documents) {
            String monitortime = "";
            if (collection.equals("HourData")) {
                monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            } else if (collection.equals("DayData")) {
                monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            } else if (collection.equals("MinuteData")) {
                monitortime = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
            }
            if (times.contains(monitortime)) {//判断是否类型重复
                continue;//重复
            } else {//不重复
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitortime);
                times.add(monitortime);
                int num = 0;
                double value = 0d;
                Map<String, Integer> windmap = new HashMap<>();
                Map<String, Object> windvaluemap = new HashMap<>();
                for (Document obj : documents) {
                    String monitortimetwo = "";
                    if (collection.equals("HourData")) {
                        monitortimetwo = DataFormatUtil.getDateYMDH(obj.getDate("MonitorTime"));
                    } else if (collection.equals("DayData")) {
                        monitortimetwo = DataFormatUtil.getDateYMD(obj.getDate("MonitorTime"));
                    } else if (collection.equals("MinuteData")) {
                        monitortimetwo = DataFormatUtil.getDateYMDHM(obj.getDate("MonitorTime"));
                    }
                    if (monitortime.equals(monitortimetwo)) {
                        List<Map<String, Object>> pollutantDataList = obj.get(pollutantDataKey, List.class);
                        for (Map<String, Object> map : pollutantDataList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(map.get("PollutantCode"))) {
                                if (map.get("AvgStrength") != null && !"".equals(map.get("AvgStrength").toString())) {
                                    if (Double.parseDouble(map.get("AvgStrength").toString()) > 0 || Double.parseDouble(map.get("AvgStrength").toString()) < 0) {
                                        value = value + Double.parseDouble(map.get("AvgStrength").toString());
                                        num += 1;
                                    }
                                }
                            }
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(map.get("PollutantCode"))) {
                                if (map.get("AvgStrength") != null && !"".equals(map.get("AvgStrength").toString())) {
                                    if (Double.parseDouble(map.get("AvgStrength").toString()) > 0 || Double.parseDouble(map.get("AvgStrength").toString()) < 0) {
                                        String windDirection = DataFormatUtil.windDirectionSwitch(Double.parseDouble(map.get("AvgStrength").toString()), "code");
                                        windvaluemap.put(windDirection, map.get("AvgStrength"));
                                        if (windmap.get(windDirection) != null) {
                                            windmap.put(windDirection, windmap.get(windDirection) + 1);
                                        } else {
                                            windmap.put(windDirection, 1);
                                        }
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
                double avgvalue = 0d;
                if (num > 0) {
                    avgvalue = value / num;
                }
                if (!"".equals(wind)) {
                    resultmap.put("winddirectionname", windcodename.get(wind));
                    resultmap.put("winddirectioncode", wind);
                    resultmap.put("winddirectionvalue", windvaluemap.get(wind));
                    resultmap.put("windspeed", avgvalue != 0d ? DataFormatUtil.formatDoubleSaveTwo(avgvalue) : "");
                    parkList.add(resultmap);
                }
            }
        }
        return parkList;
    }

    /**
     * @author: xsm
     * @date: 2022/02/16 0016 上午 9:57
     * @Description: 通过自定义条件获取园区恶臭小时在线监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getParkOnlinePollutantHourDataByParams(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String datatype = paramMap.get("datatype").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        List<String> dgimns = (List<String>) paramMap.get("mns");
        //获取时间段内所有时间
        String coolection = "";
        String liststr = "";
        Date stratdate = null;
        Date enddate = null;
        if ("hour".equals(datatype)) {
            stratdate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            coolection = db_hourData;
            liststr = "HourDataList";
        } else if ("day".equals(datatype)) {
            stratdate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            coolection = db_dayData;
            liststr = "DayDataList";
        }
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(stratdate).lte(enddate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(liststr));
        operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime")
                .and(liststr + ".AvgStrength").as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("PollutantCode", "MonitorTime")
                .push(valuemap).as("valuelist")
        );
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        for (Document document : mappedResults) {
            Map<String, Object> onemap = new HashMap<>();
            if ("hour".equals(datatype)) {
                onemap.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
            } else if ("day".equals(datatype)) {
                onemap.put("monitortime", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
            }
            List<Document> valuelist = (List<Document>) document.get("valuelist");
            Double total = 0d;
            int i = 0;
            for (Document onedo : valuelist) {
                if (onedo.get("value") != null && !"".equals(onedo.getString("value"))) {
                    total += Double.valueOf(onedo.getString("value"));
                    i += 1;
                }
            }
            if (i > 0) {
                onemap.put("value", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(total / i)));
            } else {
                onemap.put("value", null);
            }
            result.add(onemap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/16 10:44
     * @Description: 根据污染物编码和监测类型获取该类型监测点（恶臭、Voc、扬尘）某时段该污染物小时浓度数据
     * @param: code 污染物编码
     * @return:starttime、endtime yyyy-mm-dd HH
     */
    @Override
    public Map<String, Object> getOtherPointHourMonitorDataByParam(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String datatype = paramMap.get("datatype").toString();
        List<String> dgimns = (List<String>) paramMap.get("mns");
        List<String> codes = (List<String>) paramMap.get("codes");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        //获取时间段内所有小时
        List<String> times = new ArrayList<>();
        String coolection = "";
        String liststr = "";
        String timestr = "";
        Date stratdate = null;
        Date enddate = null;
        if ("hour".equals(datatype)) {
            stratdate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            coolection = db_hourData;
            liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
        } else if ("day".equals(datatype)) {
            stratdate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            coolection = db_dayData;
            liststr = "DayDataList";
            timestr = "%Y-%m-%d";
        }
        times.add(endtime);
        resultMap.put("times", times);
        List<Map<String, Object>> result = new ArrayList<>();
        if (dgimns.size() > 0) {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("value", "$AvgStrength");
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(stratdate).lte(enddate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").in(codes)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime")
                    .and(liststr + ".AvgStrength").as("AvgStrength").andExclude("_id"));
            operations.add(Aggregation.group("MonitorTime", "DataGatherCode")
                    .push(valuemap).as("valuelist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            String thetime = "";
            for (String mn : dgimns) {
                Map<String, Object> mnmap = new HashMap<>();
                mnmap.put("pointname", mnandname.get(mn));
                List<String> values = new ArrayList<>();
                for (String time : times) {
                    String value = "";
                    for (Document document : mappedResults) {
                        if ("hour".equals(datatype)) {
                            thetime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        } else if ("day".equals(datatype)) {
                            thetime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                        }
                        if (mn.equals(document.getString("DataGatherCode")) && time.equals(thetime)) {
                            List<Document> valuelist = (List<Document>) document.get("valuelist");
                            for (Document valuedoc : valuelist) {
                                if (!"".equals(value)) {
                                    value = (Double.valueOf(value) + ((valuedoc.get("value") != null && !"".equals(valuedoc.get("value").toString())) ? Double.valueOf(valuedoc.getString("value")) : 0)) + "";
                                } else {
                                    value = valuedoc.get("value") != null ? valuedoc.getString("value") : "";
                                }
                            }
                        }
                    }
                    if (!"".equals(value)) {
                        value = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(value)));
                    }
                    values.add(value);
                }
                mnmap.put("valuelist", values);
                result.add(mnmap);
            }
        }
        resultMap.put("valuedata", result);
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时浓度排名和环比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<Map<String, Object>> countVocHourMpnitorDataRankByParam(Map<String, Object> parammap) {
        List<Map<String, Object>> result = new LinkedList<>();
        String starttime = parammap.get("starttime").toString();
        String endtime = parammap.get("endtime").toString();
        List<String> pollutantcodes = (List<String>) parammap.get("pollutantcodes");
        String pollutantcategoryname = parammap.get("pollutantcategoryname").toString();
        String othersort = parammap.get("othersort").toString();
        String sortorder = parammap.get("sortorder").toString();
        String datatype = parammap.get("datatype").toString();
        Map<String, Map<String, Object>> mnandpointinfo = (Map<String, Map<String, Object>>) parammap.get("mnandpointinfo");
        List<String> dgimns = (List<String>) parammap.get("dgimns");
        Date startdate = null;
        Date enddate = null;
        String liststr = "";
        String coolection = "";
        String valuestr = "";
        String dateformat = "%Y-%m-%d %H";
        if ("hour".equals(datatype)) {
            startdate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            liststr = "HourDataList";
            coolection = "HourData";
            valuestr = "AvgStrength";
        } else if ("day".equals(datatype)) {
            startdate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            liststr = "DayDataList";
            coolection = "DayData";
            valuestr = "AvgStrength";
            dateformat = "%Y-%m-%d";
        }
        Map<String, Object> valuemap = new HashMap<>();
        valuemap.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startdate).lte(enddate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(liststr));
        operations.add(match(Criteria.where(liststr + ".PollutantCode").in(pollutantcodes)));

        operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(dateformat).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and(liststr + ".PollutantCode").as("PollutantCode")
                .and(liststr + ".Flag").as("Flag")
                .and(liststr + "." + valuestr).as("AvgStrength").andExclude("_id"));
        operations.add(Aggregation.group("MonitorTime", "DataGatherCode")
                .push(valuemap).as("valuelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        Map<String, List<Document>> mapDocuments = new HashMap<>();
        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (mappedResults.size() > 0) {
            mapDocuments = mappedResults.stream().collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));

            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", Arrays.asList(EnvironmentalVocEnum.getCode()));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }


        }
        //判断按
        List<Document> value_onelist = mapDocuments.get(starttime);
        List<Document> value_twolist = mapDocuments.get(endtime);
        for (String mn : dgimns) {
            Map<String, Object> onemap = mnandpointinfo.get(mn);
            onemap.put("monitortime", endtime);
            onemap.put("pollutantcategoryname", pollutantcategoryname);
            onemap.put("value", null);
            onemap.put("previousvalue", null);
            String value_one = "";
            String value_two = "";
            String flag = "";
            if (value_onelist != null) {
                for (Document document : value_onelist) {
                    if (mn.equals(document.getString("DataGatherCode"))) {
                        List<Document> valuelist = (List<Document>) document.get("valuelist");
                        for (Document valuedocument : valuelist) {
                            if (!"".equals(value_one)) {
                                value_one = (Double.valueOf(value_one) + ((valuedocument.get("value") != null && !"".equals(valuedocument.get("value").toString())) ? Double.valueOf(valuedocument.getString("value")) : 0)) + "";
                            } else {
                                value_one = valuedocument.get("value") != null ? valuedocument.getString("value") : "";
                            }
                        }

                        flag = document.get("Flag") != null ? document.get("Flag").toString().toLowerCase() : "";
                    }
                }
            }
            if (value_twolist != null) {
                for (Document document : value_twolist) {
                    if (mn.equals(document.getString("DataGatherCode"))) {
                        List<Document> valuelist = (List<Document>) document.get("valuelist");
                        for (Document valuedocument : valuelist) {
                            if (!"".equals(value_two)) {
                                value_two = (Double.valueOf(value_two) + ((valuedocument.get("value") != null && !"".equals(valuedocument.get("value").toString())) ? Double.valueOf(valuedocument.getString("value")) : 0)) + "";
                            } else {
                                value_two = valuedocument.get("value") != null ? valuedocument.getString("value") : "";
                            }
                        }
                    }
                }
            }
            if (!"".equals(value_two)) {
                onemap.put("value", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(value_two))));
            }
            if (!"".equals(value_one)) {
                onemap.put("previousvalue", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(value_one))));
            }
            onemap.put("flag", flag_codeAndName.get(flag));
            result.add(onemap);
        }
        if (result.size() > 0) {
            if (!"".equals(othersort)) {
                if ("value".equals(othersort) || "previousvalue".equals(othersort)) {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), othersort))
                        ).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), othersort)).reversed()
                        ).collect(Collectors.toList());
                    }
                } else {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString())).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString()).reversed()).collect(Collectors.toList());
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 08:57
     * @Description: 通过自定义参数获取某类型所有监测点某个污染物某日的所有报警时刻
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<String> getAllPointPollutanAlarmTimeDataByParam(Map<String, Object> paramMap) {
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String datatype = paramMap.get("datatype").toString();
        Integer intervalnum = Integer.valueOf(paramMap.get("intervalnum").toString());
        String pollutantcode = paramMap.get("pollutantcode").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        String liststr = "";
        String coolection = "";
        if ("minute".equals(datatype)) {
            liststr = "MinuteDataList";
            coolection = "MinuteData";
        } else if ("hour".equals(datatype)) {
            liststr = "HourDataList";
            coolection = "HourData";
        }
        //获取该月所有日期
        List<String> daytimelist = DataFormatUtil.getYMDBetween(starttime, endtime);
        daytimelist.add(endtime);
        Map<String, Object> timemap = new HashMap<>();
        timemap.put("time", "$MonitorTime");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59")).and(liststr + ".IsOverStandard").is(true);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(liststr));
        operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode).and(liststr + ".IsOverStandard").is(true)));
        operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d %H:%M").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and(liststr + ".PollutantCode").as("PollutantCode")
                .andExclude("_id"));
        operations.add(Aggregation.group("PollutantCode", "DataGatherCode")
                .push(timemap).as("timelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<String> alarmtimelist = new ArrayList<>();
        if (mappedResults.size() > 0) {
            List<String> timestrs = new ArrayList<>();
            for (String day : daytimelist) {
                timestrs.addAll(getTimeListByIntervalAndDate(intervalnum, day));
            }
            for (Document document : mappedResults) {
                List<Document> timelist = (List<Document>) document.get("timelist");
                for (Document timedoc : timelist) {
                    for (String time : timestrs) {
                        if (timedoc.getString("time").equals(time) && !alarmtimelist.contains(time)) {
                            alarmtimelist.add(time);
                        }
                    }
                }
            }
        }
        return alarmtimelist;
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 11:51
     * @Description: 通过监测类型和污染物获取污染物排放标准
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutanDischargeStandardDataByParam(Map<String, Object> parammap) {
        return navigationStandardMapper.getPollutanDischargeStandardDataByParam(parammap);
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 13:29
     * @Description: 通过自定义参数获取所有企业单个污染物某小时的总排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:monitortime yyyy-dd-mm HH
     * @return:
     */
    @Override
    public List<Map<String, Object>> countEnvPollutantHourFlowDataByParam(Map<String, Object> paramMap) {
        //排口流量因子代码集合（废水，废气因子代码不同）
        List<String> flowCode = Arrays.asList("b01", "b02");
        DecimalFormat dec = new DecimalFormat("0.0000");
        List<Map<String, Object>> result = new ArrayList<>();
        String monitortime = paramMap.get("monitortime").toString();
        List<String> pollutionids = (List<String>) paramMap.get("pollutionids");
        //Map<String,Map<String,Object>> mnandpointinfo = (Map<String, Map<String, Object>>) paramMap.get("mnandpointinfo");
        Map<String, Object> mnandid = (Map<String, Object>) paramMap.get("mnandid");
        String pollutantcode = paramMap.get("pollutantcode").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        String datatype = paramMap.get("datatype").toString();
        String collection = "";
        String liststr = "";
        if ("hour".equals(datatype)) {
            collection = hourFlowCollection;
            liststr = "HourFlowDataList";
        } else if ("day".equals(datatype)) {
            collection = dayFlowCollection;
            liststr = "DayFlowDataList";
        }
        paramMap.clear();
        paramMap.put("pollutionids", pollutionids);
        List<Map<String, Object>> pollutioninfo = pollutionMapper.getPollutionInfoByParamMaps(paramMap);
        Criteria criteria = new Criteria();
        if ("hour".equals(datatype)) {
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + ":00:00")).lte(DataFormatUtil.parseDate(monitortime + ":00:00"));
        } else if ("day".equals(datatype)) {
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + " 00:00:00")).lte(DataFormatUtil.parseDate(monitortime + " 23:00:00"));
        }
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if (flowCode.contains(pollutantcode)) {
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime", "TotalFlow", "FlowUnit").andExclude("_id"));
        } else {
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and(liststr + ".PollutantCode").as("PollutantCode").and(liststr + ".AvgFlow").as("AvgFlow").andExclude("_id"));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        for (String id : pollutionids) {
            Map<String, Object> onemap = new HashMap<>();
            onemap.put("pollutionid", id);
            boolean hasent = false;
            Double total = null;
            String flowunit = "";
            for (Map<String, Object> pomap : pollutioninfo) {
                if (pomap.get("PK_PollutionID") != null && id.equals(pomap.get("PK_PollutionID").toString())) {
                    hasent = true;
                    onemap.put("pollutionname", pomap.get("ShorterName"));
                    onemap.put("regionjson", pomap.get("RegionJson"));
                    break;
                }
            }
            for (Document document : mappedResults) {
                if (mnandid.get(document.getString("DataGatherCode")) != null && id.equals(mnandid.get(document.getString("DataGatherCode")).toString())) {
                    if (flowCode.contains(pollutantcode)) {
                        if ("".equals(flowunit) && document.get("FlowUnit") != null) {
                            flowunit = document.get("FlowUnit").toString();
                        }
                        if (document.get("TotalFlow") != null && !"".equals(document.getString("TotalFlow"))) {
                            if (total == null) {
                                total = Double.valueOf(document.getString("TotalFlow"));
                            } else {
                                total += Double.valueOf(document.getString("TotalFlow"));
                            }
                        }
                    } else {
                        if (document.get("AvgFlow") != null && !"".equals(document.getString("AvgFlow"))) {
                            if (total == null) {
                                total = Double.valueOf(document.getString("AvgFlow"));
                            } else {
                                total += Double.valueOf(document.getString("AvgFlow"));
                            }
                        }
                    }
                }
            }
            if (flowCode.contains(pollutantcode)) {
                onemap.put("unit", flowunit);
            } else {
                if ("hour".equals(datatype)) {
                    onemap.put("unit", "kg");
                } else if ("day".equals(datatype)) {
                    onemap.put("unit", "t");
                }
            }
            if (total != null) {
                //total = total/1000 ;
                onemap.put("totalflow", DataFormatUtil.subZeroAndDot(dec.format(total)));
            } else {
                onemap.put("totalflow", null);
            }
            if (hasent) {
                result.add(onemap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 0017 下午 16:11
     * @Description: 获取一段时间内园区内和园区外污染物浓度趋势对比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime yyyy-mm-dd HH, endtime yyyy-mm-dd HH]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getParkInAndOutsideAirPollutantDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        String datatype = paramMap.get("datatype").toString();
        //获取时间段内所有小时
        List<String> times = new ArrayList<>();
        if ("day".equals(datatype)) {
            //获取时间段内所有日数据
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
        } else {
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
        }


        Criteria criteria = new Criteria();
        List<AggregationOperation> operations_park = new ArrayList<>();
        List<AggregationOperation> operations_city = new ArrayList<>();
        String parkCollection = "";
        String cityCollection = "";
        if ("day".equals(datatype)) {
            criteria.and("MonitorTime").gte(DataFormatUtil.parseDate(starttime + " 00:00:00")).lte(DataFormatUtil.parseDate(endtime + " 23:59:59"));
            parkCollection = parkdayCollection;
            cityCollection = citydayCollection;
        } else {
            criteria.and("MonitorTime").gte(DataFormatUtil.parseDate(starttime + ":00:00")).lte(DataFormatUtil.parseDate(endtime + ":59:59"));
            parkCollection = parkhourCollection;
            cityCollection = cityhourCollection;
        }

        operations_park.add(Aggregation.match(criteria));
        operations_city.add(Aggregation.match(criteria));
        operations_park.add(Aggregation.project("MonitorTime", "AQI", "PrimaryPollutant", "AirQuality", "AirLevel", "DataList").andExclude("_id"));
        operations_city.add(Aggregation.project("MonitorTime", "AQI", "PrimaryPollutant", "AirQuality", "AirLevel", "DataList").andExclude("_id"));
        Aggregation aggregationList_park = Aggregation.newAggregation(operations_park)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        Aggregation aggregationList_city = Aggregation.newAggregation(operations_city)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults_park = mongoTemplate.aggregate(aggregationList_park, parkCollection, Document.class);
        AggregationResults<Document> pageResults_city = mongoTemplate.aggregate(aggregationList_city, cityCollection, Document.class);
        List<Document> mappedResults_park = pageResults_park.getMappedResults();
        List<Document> mappedResults_city = pageResults_city.getMappedResults();
        List<Document> childlist;
        for (String time : times) {
            Map<String, Object> onemap = new HashMap<>();
            onemap.put("monitortime", time);
            onemap.put("park_value", "");
            onemap.put("city_value", "");
            onemap.put("park_primarypollutant", "");
            onemap.put("city_primarypollutant", "");
            if ("day".equals(datatype)) {
                //园区内
                for (Document document : mappedResults_park) {
                    if (time.equals(DataFormatUtil.getDateYMD(document.getDate("MonitorTime")))) {
                        onemap.put("park_primarypollutant", (document.get("PrimaryPollutant") != null && !"".equals(document.get("PrimaryPollutant").toString())) ? CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(document.get("PrimaryPollutant").toString()) : "");
                        if ("aqi".equals(pollutantcode)) {
                            onemap.put("park_value", document.get("AQI") != null ? document.get("AQI").toString() : "");
                        } else {
                            childlist = (List<Document>) document.get("DataList");
                            for (Document podoc : childlist) {
                                if (pollutantcode.equals(podoc.getString("PollutantCode"))) {
                                    onemap.put("park_value", podoc.get("Strength") != null ? podoc.get("Strength").toString() : "");
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                //园区外
                for (Document document : mappedResults_city) {
                    if (time.equals(DataFormatUtil.getDateYMD(document.getDate("MonitorTime")))) {
                        onemap.put("city_primarypollutant", (document.get("PrimaryPollutant") != null && !"".equals(document.get("PrimaryPollutant").toString())) ? CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(document.get("PrimaryPollutant").toString()) : "");
                        if ("aqi".equals(pollutantcode)) {
                            onemap.put("city_value", document.get("AQI") != null ? document.get("AQI").toString() : "");
                        } else {
                            childlist = (List<Document>) document.get("DataList");
                            for (Document podoc : childlist) {
                                if (pollutantcode.equals(podoc.getString("PollutantCode"))) {
                                    onemap.put("city_value", podoc.get("Strength") != null ? podoc.get("Strength").toString() : "");
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            } else {
                //园区内
                for (Document document : mappedResults_park) {
                    if (time.equals(DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")))) {
                        onemap.put("park_primarypollutant", (document.get("PrimaryPollutant") != null && !"".equals(document.get("PrimaryPollutant").toString())) ? CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(document.get("PrimaryPollutant").toString()) : "");
                        if ("aqi".equals(pollutantcode)) {
                            onemap.put("park_value", document.get("AQI") != null ? document.get("AQI").toString() : "");
                        } else {
                            childlist = (List<Document>) document.get("DataList");
                            for (Document podoc : childlist) {
                                if (pollutantcode.equals(podoc.getString("PollutantCode"))) {
                                    onemap.put("park_value", podoc.get("Strength") != null ? podoc.get("Strength").toString() : "");
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                //园区外
                for (Document document : mappedResults_city) {
                    if (time.equals(DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")))) {
                        onemap.put("city_primarypollutant", (document.get("PrimaryPollutant") != null && !"".equals(document.get("PrimaryPollutant").toString())) ? CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(document.get("PrimaryPollutant").toString()) : "");
                        if ("aqi".equals(pollutantcode)) {
                            onemap.put("city_value", document.get("AQI") != null ? document.get("AQI").toString() : "");
                        } else {
                            childlist = (List<Document>) document.get("DataList");
                            for (Document podoc : childlist) {
                                if (pollutantcode.equals(podoc.getString("PollutantCode"))) {
                                    onemap.put("city_value", podoc.get("Strength") != null ? podoc.get("Strength").toString() : "");
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            result.add(onemap);
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/18 0018 上午 10:17
     * @Description: 获取水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterQualityStationByParamMap(Map<String, Object> paramMap) {
        return waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/02/18 0018 上午 10:17
     * @Description: 获取所有水质级别信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllWaterQualityLevelData() {
        return waterStationMapper.getAllWaterQualityLevelData();
    }

    /**
     * @author: xsm
     * @date: 2022/2/18 0018 上午 10:31
     * @Description: 统计水质整体达标率情况（小时分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterQualityComplianceDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String datatype = paramMap.get("datatype").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Map<String, Integer> codeandlevel = (Map<String, Integer>) paramMap.get("codeandlevel");
        Map<String, Integer> mnandlevel = (Map<String, Integer>) paramMap.get("mnandlevel");
        //获取时间段内所有小时
        List<String> times = new ArrayList<>();
        String coolection = "";
        String liststr = "";
        String timestr = "";
        Date stratdate = null;
        Date enddate = null;
        if ("hour".equals(datatype)) {
            stratdate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
            times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            coolection = db_hourData;
            liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
        } else if ("day".equals(datatype)) {
            stratdate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            enddate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            times = DataFormatUtil.getYMDBetween(starttime, endtime);
            coolection = db_dayData;
            liststr = "DayDataList";
            timestr = "%Y-%m-%d";
        }
        times.add(endtime);
        if (dgimns.size() > 0) {
            Map<String, Object> valuemap = new HashMap<>();
            valuemap.put("mn", "$DataGatherCode");
            valuemap.put("level", "$WaterLevel");
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(stratdate).lte(enddate);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "WaterLevel").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            operations.add(Aggregation.group("MonitorTime")
                    .push(valuemap).as("levellist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
            List<Document> mappedResults = pageResults.getMappedResults();
            String docmn;
            int mb_level;//目标水质级别
            int sj_level;//实际水质级别
            for (String time : times) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("monitortime", time);
                int total = 0;//水质点位总数
                int db_num = 0;//达标点位个数
                for (Document document : mappedResults) {
                    if (time.equals(document.getString("_id"))) {
                        List<Document> levellist = (List<Document>) document.get("levellist");
                        if (levellist != null && levellist.size() > 0) {
                            total = levellist.size();
                            for (Document onedoc : levellist) {
                                docmn = onedoc.getString("mn");
                                mb_level = mnandlevel.get(docmn) != null ? mnandlevel.get(docmn) : 0;
                                if (onedoc.get("level") != null && codeandlevel.get(onedoc.getString("level")) != null) {
                                    sj_level = codeandlevel.get(onedoc.getString("level"));
                                    if (sj_level <= mb_level) {
                                        db_num += 1;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                if (total > 0) {
                    onemap.put("compliance", (db_num * 100 / total) + "");
                } else {
                    onemap.put("compliance", "");
                }
                result.add(onemap);
            }
        }
        return result;
    }

    /**
     * 根据日期和 间隔时间 获取某天的 所有间隔的分钟时刻  例：[2022-02-17 10:00]
     */
    private List<String> getTimeListByIntervalAndDate(int intervalnum, String daytime) {
        int hour = 24;
        int minute = 60;
        String aTime = "";
        //一天所有时间段值
        // 总的时间坐标轴显示X轴的坐标
        List<String> dateTime = new ArrayList<>();
        for (int i = 0; i < hour; i++) {
            for (int j = 0; j < minute; j++) {
                if (j % intervalnum == 0) {
                    //每隔五分钟取一次曲线
                    if (i < 10 && j < 10) {
                        aTime = "0" + i + ":" + "0" + j;
                        dateTime.add(daytime + " " + aTime);
                    }
                    if (i < 10 && j >= 10) {
                        aTime = "0" + i + ":" + j;
                        dateTime.add(daytime + " " + aTime);
                    }
                    if (i >= 10 && j < 10) {
                        aTime = i + ":" + "0" + j;
                        dateTime.add(daytime + " " + aTime);
                    }
                    if (i >= 10 && j >= 10) {
                        aTime = i + ":" + j;
                        dateTime.add(daytime + " " + aTime);
                    }
                }
            }
        }
        return dateTime;
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时或日浓度排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<Map<String, Object>> countAirHourOrDayMonitorDataRankByParam(Map<String, Object> parammap) {
        List<Map<String, Object>> result = new LinkedList<>();
        String monitortime = parammap.get("monitortime").toString();
        String datatype = parammap.get("datatype").toString();
        String pollutantcode = parammap.get("pollutantcode").toString();
        String othersort = parammap.get("othersort").toString();
        String sortorder = parammap.get("sortorder").toString();
        Map<String, Map<String, Object>> mnandpointinfo = (Map<String, Map<String, Object>>) parammap.get("mnandpointinfo");
        List<String> dgimns = (List<String>) parammap.get("dgimns");
        Map<String, Object> codeandename = new HashMap<>();
        //获取污染物信息
        parammap.clear();
        parammap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
        List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(parammap);
        if (allpollutants != null) {
            for (Map<String, Object> pomap : allpollutants) {
                if (pomap.get("code") != null) {
                    codeandename.put(pomap.get("code").toString(), pomap.get("name"));
                }
            }
        }
        if ("aqi".equals(pollutantcode)) {
            codeandename.put("aqi", "AQI");
        }
        Date startdate;
        Date enddate;
        String coolection;
        String timestr = "";
        if ("day".equals(datatype)) {
            startdate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
            enddate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
            coolection = "StationDayAQIData";
            timestr = "%Y-%m-%d";
        } else {
            startdate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
            enddate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
            coolection = "StationHourAQIData";
            timestr = "%Y-%m-%d %H";
        }
        Criteria criteria = new Criteria();
        criteria.and("StationCode").in(dgimns).and("MonitorTime").gte(startdate).lte(enddate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("StationCode", "AQI", "PrimaryPollutant", "AirQuality", "AirLevel", "DataList").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .andExclude("_id"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (mappedResults.size() > 0) {
            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", Arrays.asList(AirEnum.getCode()));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }


        }



        for (String mn : dgimns) {
            Map<String, Object> onemap = mnandpointinfo.get(mn);
            onemap.put("monitortime", monitortime);
            onemap.put("pollutantname", codeandename.get(pollutantcode));
            String value = "-";
            String primarypollutant = "-";
            String airlevel = "-";
            String flag = "";
            for (Document document : mappedResults) {
                if (mn.equals(document.getString("StationCode"))) {
                    if (document.get("PrimaryPollutant") != null) {
                        primarypollutant = codeandename.get(document.get("PrimaryPollutant").toString()) != null ? codeandename.get(document.get("PrimaryPollutant").toString()).toString() : "-";
                    }
                    if ("aqi".equals(pollutantcode)) {
                        value = document.get("AQI") != null ? document.get("AQI").toString() : null;
                        airlevel = document.get("AirLevel") != null ? document.get("AirLevel").toString() : "-";
                    } else {
                        List<Document> datalist = (List<Document>) document.get("DataList");
                        for (Document podocument : datalist) {
                            if (pollutantcode.equals(podocument.getString("PollutantCode"))) {
                                value = podocument.get("Strength") != null ? podocument.get("Strength").toString() : null;
                                flag = podocument.get("Flag")!=null?podocument.get("Flag").toString().toLowerCase():"";
                                airlevel = podocument.get("AirLevel") != null ? podocument.get("AirLevel").toString() : "-";
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            onemap.put("flag", flag_codeAndName.get(flag));
            onemap.put("value", value);
            onemap.put("airlevel", airlevel);
            onemap.put("primarypollutant", primarypollutant);
            result.add(onemap);
        }
        if (result.size() > 0) {
            if (!"".equals(othersort)) {
                if ("value".equals(othersort)) {
                    if ("asc".equals(sortorder)) {
                        result = result.stream()
                                .sorted(
                                        Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), othersort))
                                ).collect(Collectors.toList());
                    } else {
                        result = result.stream()
                                .sorted(
                                        Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), othersort)).reversed()
                                ).collect(Collectors.toList());
                    }
                } else {
                    if ("asc".equals(sortorder)) {
                        result = result.stream()
                                .sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString())).collect(Collectors.toList());
                    } else {
                        result = result.stream()
                                .sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString()).reversed()).collect(Collectors.toList());
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时浓度排名和环比情况(水质)
     * @updateUser:xsm
     * @updateDate:2022/05/11 8:40
     * @updateDescription:新增日数据查询
     * @param:starttime
     * @return:
     */
    @Override
    public List<Map<String, Object>> countWaterQualityHourMpnitorDataRankByParam(Map<String, Object> parammap) {
        List<Map<String, Object>> result = new LinkedList<>();
        String monitortime = parammap.get("monitortime").toString();
        String pollutantcode = parammap.get("pollutantcode").toString();
        String othersort = parammap.get("othersort").toString();
        String sortorder = parammap.get("sortorder").toString();
        String datatype = parammap.get("datatype").toString();
        Map<String, Object> codeandlevel = (Map<String, Object>) parammap.get("codeandlevel");
        Map<String, Map<String, Object>> mnandpointinfo = (Map<String, Map<String, Object>>) parammap.get("mnandpointinfo");
        List<String> dgimns = (List<String>) parammap.get("dgimns");
        Map<String, Object> codeandename = new HashMap<>();
        //获取污染物信息
        parammap.clear();
        parammap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
        List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(parammap);
        if (allpollutants != null) {
            for (Map<String, Object> pomap : allpollutants) {
                if (pomap.get("code") != null) {
                    codeandename.put(pomap.get("code").toString(), pomap.get("name"));
                }
            }
        }
        Date startdate = null;
        Date enddate = null;
        String coolection = "";
        String liststr = "";
        if ("hour".equals(datatype)) {
            startdate = DataFormatUtil.getDateYMDHMS(monitortime + ":00:00");
            enddate = DataFormatUtil.getDateYMDHMS(monitortime + ":59:59");
            coolection = "HourData";
            liststr = "HourDataList";
        } else if ("day".equals(datatype)) {
            startdate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
            enddate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
            coolection = "DayData";
            liststr = "DayDataList";
        }
        List<Document> mappedResults = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(startdate).lte(enddate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        if ("waterquality".equals(pollutantcode)) {
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime", "WaterLevel").andExclude("_id"));
        } else {
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime", "WaterLevel")
                    .and(liststr + ".Flag").as("Flag")
                    .and(liststr + ".AvgStrength").as("AvgStrength")
                    .and(liststr + ".WaterLevel").as("codewaterlevel")
                    .andExclude("_id"));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, coolection, Document.class);
        mappedResults = pageResults.getMappedResults();


        Map<String, Object> flag_codeAndName = new HashMap<>();
        if (mappedResults.size() > 0) {
            //获取mongodb的flag标记
            Map<String, Object> f_map = new HashMap<>();
            f_map.put("monitorpointtypes", Arrays.asList(WaterQualityEnum.getCode()));
            List<Map<String, Object>> flagList = pollutantFactorMapper.getFlagListByParam(f_map);
            String flag_code;
            for (Map<String, Object> map : flagList) {
                if (map.get("code") != null) {
                    flag_code = map.get("code").toString();
                    flag_codeAndName.put(flag_code, map.get("name"));
                }
            }
        }


        for (String mn : dgimns) {
            Map<String, Object> onemap = mnandpointinfo.get(mn);
            onemap.put("monitortime", monitortime);
            if ("waterquality".equals(pollutantcode)) {
                onemap.put("pollutantname", "水质类别");
            } else {
                onemap.put("pollutantname", codeandename.get(pollutantcode));
            }
            String value = "-";
            String pointlevel = "-";
            String codelevel = "-";
            String pointlevelname = "-";
            String codelevelname = "-";
            String flag = "";
            for (Document document : mappedResults) {
                if (mn.equals(document.getString("DataGatherCode"))) {
                    if (document.get("WaterLevel") != null) {
                        pointlevel = codeandlevel.get(document.get("WaterLevel").toString()) != null ? document.get("WaterLevel").toString().toString() : "-";
                        pointlevelname = codeandlevel.get(document.get("WaterLevel").toString()) != null ? codeandlevel.get(document.get("WaterLevel").toString()).toString() : "-";
                    }
                    if (!"waterquality".equals(pollutantcode)) {
                        value = document.get("AvgStrength") != null ? document.get("AvgStrength").toString() : null;
                        if (document.get("codewaterlevel") != null) {
                            codelevel = document.get("codewaterlevel") != null ? document.get("codewaterlevel").toString() : "-";
                            codelevelname = codeandlevel.get(document.get("codewaterlevel").toString()) != null ? codeandlevel.get(document.get("codewaterlevel").toString()).toString() : "-";
                        } else {
                            codelevel = "Ⅰ";
                            codelevelname = "Ⅰ类";
                        }
                    }
                }
                flag = document.get("Flag") != null ? document.get("Flag").toString().toLowerCase() : "";
            }

            onemap.put("flag", flag_codeAndName.get(flag));
            onemap.put("pointlevel", pointlevel);
            onemap.put("pointlevelname", pointlevelname);
            if (!"waterquality".equals(pollutantcode)) {
                onemap.put("value", value);
                onemap.put("codelevel", codelevel);
                onemap.put("codelevelname", codelevelname);
            } else {
                onemap.put("value", CommonTypeEnum.WaterLevelEnum.getObjectByCode(pointlevel) != null ?
                        CommonTypeEnum.WaterLevelEnum.getObjectByCode(pointlevel).getIndex() : -1);
                onemap.put("codelevel", pointlevel);
                onemap.put("codelevelname", pointlevelname);
            }
            result.add(onemap);
        }
        if (result.size() > 0) {
            if (!"".equals(othersort)) {
                if ("value".equals(othersort)) {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), othersort))
                        ).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(
                                Comparator.comparing(m -> comparingDoubleByKey(((Map<String, Object>) m), othersort)).reversed()
                        ).collect(Collectors.toList());
                    }
                } else {
                    if ("asc".equals(sortorder)) {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString())).collect(Collectors.toList());
                    } else {
                        result = result.stream().sorted(Comparator.comparing(m -> ((Map) m).get(othersort).toString()).reversed()).collect(Collectors.toList());
                    }
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 10:38
     * @Description: 根据监测类型和时间段获取某个点的突增污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime yyyy-mm-dd HH,endtime yyyy-mm-dd HH
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnePointChangeWarnPollutantData(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultMap = new ArrayList<>();
        List<Map<String, Object>> outputs = onlineServiceImpl.getMonitorAndPollutants(paramMap);
        Comparator<Map<String, Object>> comparator = Comparator.comparing(a -> Integer.parseInt(a.get("OrderIndex") == null ? "-1" : a.get("OrderIndex").toString()));
        String collection = minuteCollection;
        String datalistkey = "MinuteDataList";
        outputs.sort(comparator);
        if (outputs.size() == 0) {
            return resultMap;
        }
        String mn = paramMap.get("mn").toString();
        Date starttime = DataFormatUtil.parseDate(paramMap.get("starttime") + " 00:00:00");
        Date endtime = DataFormatUtil.parseDate(paramMap.get("endtime") + " 23:59:59");
        Set<String> pollutantCodes = outputs.stream().filter(m -> m.get("PollutantCode") != null).map(m -> m.get("PollutantCode").toString()).collect(Collectors.toSet());
        //查询条件
        Criteria criteria = Criteria.where("DataGatherCode").is(mn)
                .and("PollutantCode").in(pollutantCodes)
                .and("IsSuddenChange").is(true);
        if (starttime != null && endtime != null) {
            criteria.and("MonitorTime").gte(starttime).lte(endtime);
        }
        List<Document> mappedResults = new ArrayList<>();
        //浓度
        Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", "HourDataList.AvgStrength", datalistkey + ".IsSuddenChange", datalistkey + ".ChangeMultiple", "_id");
        Aggregation hourDataList = newAggregation(
                unwind(datalistkey),
                project(fields),
                match(criteria));
        mappedResults = mongoTemplate.aggregate(hourDataList, collection, Document.class).getMappedResults();
        for (Map<String, Object> output : outputs) {
            Map<String, Object> mapInfo = new HashMap<>();
            String pollutantCode = output.get("PollutantCode").toString();
            Object pollutantUnit = output.get("PollutantUnit");
            Object IsHasConvertData = output.get("IsHasConvertData");
            Object rate = output.get("Rate");
            String pollutantName = output.get("PollutantName").toString();
            mapInfo.put("pollutantname", pollutantName);
            mapInfo.put("pollutantcode", pollutantCode);
            mapInfo.put("ishasconvertdata", IsHasConvertData != null ? IsHasConvertData : 0);
            mapInfo.put("ChangeBaseValue", output.get("ChangeBaseValue"));
            mapInfo.put("standardvalue", getStandardValue(output));
            mapInfo.put("flowrate", rate);
            mapInfo.put("pollutantunit", pollutantUnit);
            for (Document mappedResult : mappedResults) {
                String pollutantCodeInfo = mappedResult.getString("PollutantCode");
                if (pollutantCode.equals(pollutantCodeInfo)) {
                    mapInfo.put("ischangewarn", true);
                    break;
                }
            }
            resultMap.add(mapInfo);
        }
        return resultMap;
    }

    /**
     * @author: lip
     * @date: 2020/3/9 0009 下午 2:06
     * @Description: 处理标准值数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object getStandardValue(Map<String, Object> output) {
        String standardvalue = "";
        if (output.get("AlarmType") != null) {

            String AlarmType = output.get("AlarmType").toString();

            if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(AlarmType)) {//上限报警
                standardvalue = output.get("StandardMaxValue") != null ? output.get("StandardMaxValue").toString() : "";
            } else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(AlarmType)) {//下限报警
                standardvalue = output.get("StandardMinValue") != null ? output.get("StandardMinValue").toString() : "";
            } else if (CommonTypeEnum.AlarmTypeEnum.BetweenAlarmEnum.getCode().equals(AlarmType)) {//区间报警
                String minValue = output.get("StandardMinValue") != null ? output.get("StandardMinValue").toString() : "";
                String maxValue = output.get("StandardMaxValue") != null ? output.get("StandardMaxValue").toString() : "";
                if (StringUtils.isNotBlank(minValue) && StringUtils.isNotBlank(maxValue)) {
                    standardvalue = minValue + "-" + maxValue;
                }
            }
        }

        return standardvalue;
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 10:53
     * @Description: 获取单个点单污染物的浓度突增情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-mm-dd HH   endtime yyyy-mm-dd HH
     * @return:
     */
    @Override
    public Map<String, Object> getOnePointPollutantChangeWarnDataByParams(String start, String end, String mn, String pollutantCode) {
        Map<String, Object> resultMap = new HashMap<>();
        Date starttime = DataFormatUtil.getDateYMDHMS(start + " 00:00:00");
        Date endtime = DataFormatUtil.getDateYMDHMS(end + " 23:59:59");
        String datalistkey = "MinuteDataList";
        String collection = minuteCollection;
        List<Document> mappedResults = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").is(mn).and("MonitorTime").gte(starttime).lte(endtime)
                .and("PollutantCode").is(pollutantCode);
        String valueKeyName = "";
        valueKeyName = "AvgStrength";
        Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", datalistkey + ".AvgStrength", datalistkey + ".ChangeMultiple", datalistkey + ".IsSuddenChange", "_id");
        mappedResults = mongoTemplate.aggregate(newAggregation(
                unwind(datalistkey),
                project(fields),
                match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), collection, Document.class).getMappedResults();

        if (mappedResults.size() == 0) {
            return resultMap;
        } else {
            List<Map<String, Object>> datalist = new ArrayList<>();
            Map<Object, Object> starts = new HashMap<>();   //起始点
            Map<Object, List<Map<String, Object>>> tuzengTimes = new HashMap<>();
            Object initValue = null;
            Object startPointTime = null;
            for (Document mappedResult : mappedResults) {
                Date monitortime = mappedResult.getDate("MonitorTime");
                String dateYMDH = DataFormatUtil.getDateYMDHM(monitortime);
                Object value = mappedResult.get(valueKeyName);
                if (value != null) {
                    Map<String, Object> datalistMap = new HashMap<>();
                    datalistMap.put("monitorvalue", value);
                    datalistMap.put("monitortime", dateYMDH);
                    datalistMap.put("issuddenchange", mappedResult.get("IsSuddenChange"));
                    datalist.add(datalistMap);
                }
                boolean IsSuddenChange = mappedResult.get("IsSuddenChange") == null ? false : Boolean.valueOf(mappedResult.get("IsSuddenChange").toString());
                if (initValue != null && startPointTime != null && value != null) {
                    if (IsSuddenChange) {
                        Map<String, Object> mapInfo = new HashMap<>();
                        mapInfo.put("monitortime", dateYMDH);
                        mapInfo.put("monitorvalue", value);
                        if (tuzengTimes.containsKey(startPointTime)) {
                            List<Map<String, Object>> list = tuzengTimes.get(startPointTime);
                            list.add(mapInfo);
                        } else {
                            List<Map<String, Object>> list = new ArrayList<>();
                            list.add(mapInfo);
                            tuzengTimes.put(startPointTime, list);
                        }
                    } else {
                        startPointTime = dateYMDH;
                        starts.put(startPointTime, value);
                    }
                    initValue = value;
                }
                if (initValue == null && value != null) {
                    initValue = value;
                    startPointTime = dateYMDH;
                    starts.put(startPointTime, value);
                }
            }
            List<Map<String, Object>> timepoint = new ArrayList<>();
            if (tuzengTimes.size() > 0) {
                for (Map.Entry<Object, Object> entry : starts.entrySet()) {
                    Object key = entry.getKey();
                    if (tuzengTimes.containsKey(key)) {
                        Map<String, Object> timepointMap = new HashMap<>();
                        List<Map<String, Object>> startpoints = new ArrayList<>();
                        List<Map<String, Object>> endpoint = new ArrayList<>();
                        Map<String, Object> map = new HashMap<>();
                        Object value = entry.getValue();
                        map.put("monitortime", key);
                        map.put("monitorvalue", value);
                        startpoints.add(map);
                        for (Map.Entry<Object, List<Map<String, Object>>> listEntry : tuzengTimes.entrySet()) {
                            if (listEntry.getKey().equals(key)) {
                                endpoint = listEntry.getValue();
                            }
                        }
                        timepointMap.put("endpoint", endpoint);
                        timepointMap.put("startpoint", startpoints);
                        timepoint.add(timepointMap);
                    }
                }
            }
            resultMap.put("timepoint", timepoint);
            resultMap.put("datalist", datalist.stream().filter(distinctByKey(m -> m.get("monitortime"))).collect(Collectors.toList()));
        }
        return resultMap;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        ConcurrentSkipListMap<Object, Boolean> skipListMap = new ConcurrentSkipListMap<>();
        Predicate<T> predicate = t -> skipListMap.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
        return predicate;
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 11:08
     * @Description: 获取单点位单污染物某时间段的浓度突增详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-mm-dd HH  endtime yyyy-mm-dd HH
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnePointPollutantChangeWarnDetailParams(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> outputs = onlineServiceImpl.getMonitorAndPollutants(paramMap);
        if (outputs.size() == 0) {
            return result;
        }
        String mn = paramMap.get("mn").toString();
        Date starttime;
        Date endtime;
        String collection = minuteCollection;
        String datalistkey = "MinuteDataList";
        starttime = DataFormatUtil.parseDate(paramMap.get("starttime") + " 00:00:00");
        endtime = DataFormatUtil.parseDate(paramMap.get("endtime") + " 23:59:59");
        Set<String> pollutantCodes = outputs.stream().filter(m -> m.get("PollutantCode") != null).map(m -> m.get("PollutantCode").toString()).collect(Collectors.toSet());
        List<Document> mappedResults = new ArrayList<>();
        //查询条件
        Criteria criteria = Criteria.where("DataGatherCode").is(mn).and("MonitorTime").gte(starttime).lte(endtime)
                .and("PollutantCode").in(pollutantCodes);
        String valueKeyName = "";
        valueKeyName = "AvgStrength";
        if (paramMap.get("monitortype") != null && CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() ==
                Integer.parseInt(paramMap.get("monitortype").toString())) {
            valueKeyName = "AvgConvertStrength";
        }
        //浓度
        Fields fields = fields("DataGatherCode", "MonitorTime", datalistkey + ".PollutantCode", datalistkey + ".AvgStrength", datalistkey + ".AvgConvertStrength", datalistkey + ".ChangeMultiple", datalistkey + ".IsSuddenChange", "_id");
        mappedResults = mongoTemplate.aggregate(newAggregation(
                unwind(datalistkey),
                project(fields),
                match(criteria), sort(Sort.Direction.ASC, "MonitorTime")), collection, Document.class).getMappedResults();

        if (mappedResults.size() == 0) {
            return result;
        }
        Object PollutionName = outputs.get(0).get("PollutionName");
        //Object shortername = outputs.get(0).get("shortername");
        Object outputname = outputs.get(0).get("OutPutName");
        String pollutantCode;
        Integer IsHasConvertData;
        Map<String, Integer> codeAndIs = new HashMap<>();
        if (paramMap.get("monitortype") != null && CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode() ==
                Integer.parseInt(paramMap.get("monitortype").toString())) {
            Map<String, Object> paramMap1 = new HashMap<>();
            paramMap1.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
            List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap1);
            for (Map<String, Object> pollutant : pollutants) {
                IsHasConvertData = pollutant.get("IsHasConvertData") != null ? Integer.parseInt(pollutant.get("IsHasConvertData").toString()) : 0;
                codeAndIs.put(pollutant.get("code").toString(), IsHasConvertData);
            }
        }
        Map<String, List<Document>> mapData = mappedResults.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("MonitorTime"))));
        for (Map.Entry<String, List<Document>> entry : mapData.entrySet()) {
            Map<String, Object> mapInfo = new HashMap<>();
            String time = entry.getKey();
            List<Map<String, Object>> hourdatalist = new ArrayList<>();
            List<Document> value = entry.getValue();
            for (Document document : value) {
                Map<String, Object> pollutantMap = new HashMap<>();
                pollutantCode = document.getString("PollutantCode");
                if (valueKeyName.indexOf("Strength") >= 0) {
                    if (codeAndIs.containsKey(pollutantCode) && codeAndIs.get(pollutantCode) == 1) {
                        valueKeyName = "AvgConvertStrength";
                    } else {
                        valueKeyName = "AvgStrength";
                    }
                }
                pollutantMap.put("pollutantcode", pollutantCode);
                pollutantMap.put("avgstrength", document.get(valueKeyName));
                pollutantMap.put("ischangewarn", document.get("IsSuddenChange"));
                hourdatalist.add(pollutantMap);
            }
            mapInfo.put("monitortime", time);
            mapInfo.put("outputname", outputname);
            mapInfo.put("pollutionname", PollutionName);
            mapInfo.put("hourdatalist", hourdatalist);
            result.add(mapInfo);
        }
        Comparator<Map<String, Object>> comparator = Comparator.comparing(a -> a.get("monitortime").toString());
        result.sort(comparator);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 0024 上午 11:28
     * @Description: 通过多参数获取单个点位某时间段的报警时长统计数据(按时间类型分组 小时 / 日 / 月)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPointAlarmTimesDataGroupByTime(Map<String, Object> paramMap) throws ParseException {
        try {
            String dgimn = paramMap.get("dgimn").toString();
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            String timetype = paramMap.get("timetype").toString();

            Map<String, Object> codeandename = new HashMap<>();
            //获取污染物信息
            List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(null);
            if (allpollutants != null) {
                for (Map<String, Object> pomap : allpollutants) {
                    if (pomap.get("code") != null) {
                        codeandename.put(pomap.get("code").toString(), pomap.get("name"));
                    }
                }
            }
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            timefield = "FirstOverTime";
            collection = overModelCollection;
            lasttimestr = "LastOverTime";
            String timestr = "";
            if ("hour".equals(timetype)) {//按小时分组
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
                timestr = "%Y-%m-%d";
            } else if ("day".equals(timetype)) {//按日分组
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                timestr = "%Y-%m-%d";
            } else if ("month".equals(timetype)) {//按月分组
                timestr = "%Y-%m";
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                starttime = starttime + "-01 00:00:00";
                Calendar instance = Calendar.getInstance();
                instance.setTime(format.parse(endtime));
                int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
                endtime = endtime + "-" + actualMaximum + " 23:59:59";
            }
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            if (paramMap.get("pollutantcode") != null) {
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("PollutantCode").is(paramMap.get("pollutantcode").toString()).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            } else {
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            }
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", lasttimestr, timefield, "PollutantCode")
                    .and(DateOperators.DateToString.dateOf(timefield).toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$" + timefield);
            childmap.put("endtime", "$" + lasttimestr);
            operations.add(
                    Aggregation.group("MN", "MonitorTime", "PollutantCode")
                            .push(childmap).as("timelist")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN", "PollutantCode"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            List<Map<String, Object>> dataList = new ArrayList<>();
            if ("hour".equals(timetype)) {//按小时分组
                dataList = countPointAlarmTimesDataGroupByHourTime(listItems, codeandename);
            } else {//按日/月分组
                dataList = countPointAlarmTimesDataGroupByDayOrMonthTime(listItems, timetype, codeandename);
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 组装按小时分组的超标时长数据
     */
    private List<Map<String, Object>> countPointAlarmTimesDataGroupByHourTime(List<Document> listItems, Map<String, Object> codeandename) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Document document : listItems) {
                List<Document> timelist = (List<Document>) document.get("timelist");
                List<Map<String, Object>> onelist = new ArrayList<>();
                if (timelist != null && timelist.size() > 0) {
                    String onestarttime = null;
                    String oneenttime = null;
                    for (Document onedocument : timelist) {
                        onestarttime = DataFormatUtil.getDateHM(onedocument.getDate("starttime"));
                        String hourone = onestarttime.substring(0, onestarttime.length() - 3);
                        String minuteone = onestarttime.substring(3, onestarttime.length());
                        oneenttime = DataFormatUtil.getDateHM(onedocument.getDate("endtime"));
                        String minutetwo = oneenttime.substring(3, oneenttime.length());
                        String hourtwo = oneenttime.substring(0, oneenttime.length() - 3);
                        if (hourone.equals(hourtwo)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("min", minuteone);
                            map.put("max", minutetwo);
                            map.put("hournum", Integer.valueOf(hourone));
                            onelist.add(map);
                        } else {
                            for (int i = Integer.valueOf(hourone); i <= (Integer.valueOf(hourtwo)); i++) {
                                Map<String, Object> map = new HashMap<>();
                                if (i == Integer.valueOf(hourone)) {
                                    map.put("min", minuteone);
                                } else {
                                    map.put("min", "0");
                                }
                                if (i != (Integer.valueOf(hourtwo))) {
                                    map.put("max", "60");
                                } else {
                                    map.put("max", minutetwo);
                                }
                                map.put("hournum", i);
                                onelist.add(map);
                            }
                        }
                    }
                }
                if (onelist != null && onelist.size() > 0) {
                    Map<String, List<Map<String, Object>>> mapDocuments = new HashMap<>();
                    mapDocuments = onelist.stream().collect(Collectors.groupingBy(m -> m.get("hournum").toString()));
                    for (int i = 0; i < 24; i++) {
                        if (mapDocuments.get(i + "") != null) {
                            List<Map<String, Object>> twolist = mapDocuments.get(i + "");
                            String min = "";
                            String max = "";
                            int totalminute = 0;
                            twolist = twolist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("min").toString()))).collect(Collectors.toList());
                            for (Map<String, Object> onemap : twolist) {
                                if ("".equals(min) && "".equals(max)) {
                                    min = onemap.get("min").toString();
                                    max = onemap.get("max").toString();
                                } else {
                                    //比较时间段
                                    //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                    if (Integer.valueOf(onemap.get("min").toString()) >= Integer.valueOf(min) && Integer.valueOf(onemap.get("min").toString()) <= Integer.valueOf(max)) {
                                        if (Integer.valueOf(onemap.get("max").toString()) > Integer.valueOf(max)) {
                                            max = onemap.get("max").toString();
                                        }
                                    } else {
                                        //第二次报警时段不被包含于第一个报警时段中
                                        if (min.equals(max)) {
                                            totalminute += 1;
                                        } else {
                                            totalminute += Integer.valueOf(max) - Integer.valueOf(min);
                                        }
                                        min = onemap.get("min").toString();
                                        max = onemap.get("max").toString();
                                    }
                                }

                            }
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("unit", "分钟");
                            dataMap.put("pollutantname", codeandename.get(document.get("PollutantCode")));
                            if (!"".equals(min) && !"".equals(max)) {
                                if (min.equals(max)) {
                                    dataMap.put("alarmtimevalue", totalminute + 1);
                                } else {
                                    dataMap.put("alarmtimevalue", totalminute + (Integer.valueOf(max) - Integer.valueOf(min)));
                                }
                            } else {
                                dataMap.put("alarmtimevalue", 0);
                            }
                            if (i < 10) {
                                dataMap.put("monitortime", document.getString("MonitorTime") + " 0" + i);
                            } else {
                                dataMap.put("monitortime", document.getString("MonitorTime") + " " + i);
                            }
                            dataList.add(dataMap);
                        }
                    }
                }
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 组装按日或月分组的超标时长数据
     */
    public List<Map<String, Object>> countPointAlarmTimesDataGroupByDayOrMonthTime(List<Document> listItems, String timetype, Map<String, Object> codeandename) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("monitortime", document.getString("MonitorTime"));
                List<Map<String, Object>> polist = new ArrayList<>();
                List<Document> timelist = (List<Document>) document.get("timelist");
                String continuityvalue = "";
                Date firsttime = null;
                Date lasttime = null;
                int totalminute = 0;
                //按时间排序
                timelist = timelist.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("starttime"))).collect(Collectors.toList());
                if (timelist != null && timelist.size() > 0) {
                    for (Document podo : timelist) {
                        //比较时间 获取报警时段
                        if (podo.get("starttime") != null && podo.get("endtime") != null) {
                            if (firsttime == null && lasttime == null) {
                                firsttime = podo.getDate("starttime");
                                lasttime = podo.getDate("endtime");
                            } else {
                                //比较时间段
                                //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(lasttime)) ||
                                        podo.getDate("starttime").before(lasttime)) {
                                    //若被包含 比较两个结束时间
                                    if (lasttime.before(podo.getDate("endtime"))) {
                                        //若第一次报警的结束时间 小于 第二个报警时段的结束时间
                                        //则进行赋值
                                        lasttime = podo.getDate("endtime");
                                    }
                                } else {
                                    //第二次报警时段不被包含于第一个报警时段中
                                    if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
                                        continuityvalue = continuityvalue + DataFormatUtil.getDateHM(firsttime) + "、";
                                        totalminute += 1;
                                    } else {
                                        continuityvalue = continuityvalue + DataFormatUtil.getDateHM(firsttime) + "-" + DataFormatUtil.getDateHM(lasttime) + "、";
                                        long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
                                        totalminute += Integer.valueOf(timenum + "");
                                    }
                                    //将重新赋值开始 结束时间
                                    firsttime = podo.getDate("starttime");
                                    lasttime = podo.getDate("endtime");
                                }
                            }
                        }
                    }
                    //将最后一次超标时段 或一直连续报警的超标时段 拼接
                    if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
                        totalminute += 1;
                    } else {
                        long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
                        totalminute += Integer.valueOf(timenum + "");
                    }
                }
                dataMap.put("alarmtimevalue", totalminute + "");
                dataMap.put("unit", "分钟");
                dataMap.put("pollutantname", codeandename.get(document.get("PollutantCode")));
                if ("month".equals(timetype)) {//按月分组
                    dataMap.put("unit", "小时");
                    if (totalminute > 0) {
                        dataMap.put("alarmtimevalue", DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(totalminute + "") / 60));
                    } else {
                        dataMap.put("alarmtimevalue", totalminute + "");
                    }
                }
                dataList.add(dataMap);
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/02/28 0028 下午 1:56
     * @Description: 统计单个点位某时段内污染物报警时长占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countPollutantAlarmTimeProportionDataByParam(Map<String, Object> paramMap) throws ParseException {
        try {
            List<String> dgimns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            String timetype = paramMap.get("timetype").toString();
            if ("hour".equals(timetype)) {//按小时分组
                starttime = starttime + ":00:00";
                endtime = endtime + ":59:59";
            } else if ("day".equals(timetype)) {//按日分组
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
            } else if ("month".equals(timetype)) {//按月分组
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                starttime = starttime + "-01 00:00:00";
                Calendar instance = Calendar.getInstance();
                instance.setTime(format.parse(endtime));
                int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
                endtime = endtime + "-" + actualMaximum + " 23:59:59";
            }
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);

            Map<String, Object> codeandename = new HashMap<>();
            //获取污染物信息
            paramMap.clear();
            if (paramMap.get("monitorpointtype") != null) {
                paramMap.put("monitorpointtype", paramMap.get("monitorpointtype"));
            } else {
                paramMap.put("monitorpointtypes", paramMap.get("monitorpointtypes"));
            }
            List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
            if (allpollutants != null) {
                for (Map<String, Object> pomap : allpollutants) {
                    if (pomap.get("code") != null) {
                        codeandename.put(pomap.get("code").toString(), pomap.get("name"));
                    }
                }
            }
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            timefield = "FirstOverTime";
            collection = overModelCollection;
            lasttimestr = "LastOverTime";
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("MN").in(dgimns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", lasttimestr, timefield, "PollutantCode")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$" + timefield);
            childmap.put("endtime", "$" + lasttimestr);
            operations.add(
                    Aggregation.group("MN", "MonitorTime", "PollutantCode")
                            .push(childmap).as("timelist")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN", "PollutantCode"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();

            List<Map<String, Object>> dataList = new ArrayList<>();
            if (listItems.size() > 0) {
                Map<String, List<Document>> mapDocuments = new HashMap<>();
                mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                int total = 0;
                //分钟去重复

                for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                    Map<String, Object> valuemap = new HashMap<>();
                    valuemap.put("pollutantcode", entry.getKey());
                    valuemap.put("pollutantname", codeandename.get(entry.getKey()));
                    int minutevalue = countSumPollutantAlarmTimes(entry.getValue());
                    total += minutevalue;
                    valuemap.put("value", minutevalue);
                    dataList.add(valuemap);
                }
                if (total > 0) {
                    int onevalue;
                    for (Map<String, Object> map : dataList) {
                        onevalue = Integer.valueOf(map.get("value").toString());
                        map.put("proportion", DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(onevalue * 100 + "") / Double.valueOf(total)));
                        map.put("unit", "分钟");
                        if ("month".equals(timetype)) {
                            map.put("unit", "小时");
                            map.put("value", DataFormatUtil.SaveTwoAndSubZero(Double.valueOf(map.get("value").toString()) / 60));
                        }
                    }
                } else {
                    for (Map<String, Object> map : dataList) {
                        map.put("proportion", "0");
                        map.put("unit", "分钟");
                        if ("month".equals(timetype)) {
                            map.put("unit", "小时");
                        }
                    }
                }
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/03/30 0030 上午 10:52
     * @Description: 获取单个点位最新一条实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Document> getOnePointLatestRealTimeDataByParamMap(Map<String, Object> paramMap) {

        List<AggregationOperation> aggregations = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("Type").is("RealTimeData");
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime", "DataList"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "LatestData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: xsm
     * @date: 2022/04/07 007 下午 4:33
     * @Description: 统计某类型报警点位某日各报警污染物报警时长
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countAllPollutantAlarmTimeByParamMap(Map<String, Object> paramMap) {
        try {
            List<String> dgimns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            Map<String, Object> mnandtype = (Map<String, Object>) paramMap.get("mnandtype");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> mnandpointinfo = (Map<String, Object>) paramMap.get("mnandpointinfo");
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            timefield = "FirstOverTime";
            collection = overModelCollection;
            lasttimestr = "LastOverTime";
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("MN").in(dgimns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", lasttimestr, timefield, "PollutantCode")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$" + timefield);
            childmap.put("endtime", "$" + lasttimestr);
            operations.add(
                    Aggregation.group("MN", "MonitorTime", "PollutantCode").max(lasttimestr).as("lasttime")
                            .push(childmap).as("timelist")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN", "PollutantCode"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (listItems.size() > 0) {
                Map<String, List<Document>> mapDocuments = new HashMap<>();
                //按监测点分组
                mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("MN").toString()));
                List<Document> podoc;
                String type;
                for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                    podoc = entry.getValue();
                    List<Map<String, Object>> polist = new ArrayList<>();
                    Date lasttime = null;
                    for (Document onedoc : podoc) {
                        if (lasttime == null) {
                            lasttime = onedoc.getDate("lasttime");
                        } else {
                            //比较两个时间
                            if (lasttime.before(onedoc.getDate("lasttime"))) {
                                lasttime = onedoc.getDate("lasttime");
                            }
                        }
                        Map<String, Object> pomap = new HashMap<>();
                        type = mnandtype.get(onedoc.getString("MN")) != null ? mnandtype.get(onedoc.getString("MN")).toString() : "";
                        pomap.put("pollutantcode", onedoc.getString("PollutantCode"));
                        pomap.put("pollutantname", !"".equals(type) ? codeandname.get(onedoc.getString("PollutantCode") + "_" + type) : "");
                        int minutevalue = countSumPollutantAlarmTimesTwo(onedoc);
                        pomap.put("value", countHourMinuteTime(minutevalue));
                        polist.add(pomap);
                    }
                    if (polist.size() > 0) {
                        Map<String, Object> valuemap = (Map<String, Object>) mnandpointinfo.get(entry.getKey());
                        valuemap.put("lastalarmtime", lasttime != null ? DataFormatUtil.getDateYMDHMS(lasttime) : "-");
                        valuemap.put("alarmdata", polist);
                        dataList.add(valuemap);
                    }

                }
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 统计单污染物报警总时长(分钟)
     */
    private Integer countSumPollutantAlarmTimes(List<Document> listItems) {
        try {
            List<String> minuteNums = new ArrayList<>();

            List<String> sumNum;
            Date startTime;
            Date endTime;
            String mS;
            String mE;
            String ymdhms;
            for (Document document : listItems) {
                List<Document> timelist = (List<Document>) document.get("timelist");
                for (Document timeDoc : timelist) {
                    startTime = timeDoc.getDate("starttime");
                    endTime = timeDoc.getDate("endtime");
                    mS = DataFormatUtil.getDateYMDHM(startTime);
                    mE = DataFormatUtil.getDateYMDHM(endTime);
                    ymdhms = DataFormatUtil.getDateYMDHMS(endTime);
                    sumNum = DataFormatUtil.getYMDHM2Between(mS, mE);
                    if (sumNum.size() == 0) {
                        sumNum.add(mE);
                    }
                    if (ymdhms.contains(":59:59")) {
                        sumNum.add(mE + ":59");
                    }
                    minuteNums.addAll(sumNum);

                }
            }
            minuteNums = minuteNums.stream().distinct().collect(Collectors.toList());
            int total = minuteNums.size();
            return total;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 统计单污染物报警总时长(分钟)
     */
    private int countSumPollutantAlarmTimesTwo(Document document) {
        try {
            List<String> minuteNums = new ArrayList<>();
            List<String> sumNum;
            Date startTime;
            Date endTime;
            String mS;
            String mE;
            String ymdhms;
            List<Document> timelist = (List<Document>) document.get("timelist");
            for (Document timeDoc : timelist) {
                startTime = timeDoc.getDate("starttime");
                endTime = timeDoc.getDate("endtime");
                mS = DataFormatUtil.getDateYMDHM(startTime);
                mE = DataFormatUtil.getDateYMDHM(endTime);
                ymdhms = DataFormatUtil.getDateYMDHMS(endTime);
                sumNum = DataFormatUtil.getYMDHM2Between(mS, mE);
                if (sumNum.size() == 0) {
                    sumNum.add(mE);
                }
                if (ymdhms.contains(":59:59")) {
                    sumNum.add(mE + ":59");
                }
                minuteNums.addAll(sumNum);

            }
            minuteNums = minuteNums.stream().distinct().collect(Collectors.toList());
            int total = minuteNums.size();
            return total;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/04/18 10:43
     * @Description: 通过自定义参数获取空气所有监测点某个污染物某时间段内所有报警日期（日数据报警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<String> getAirAllPointPollutanAlarmDayDataByParam(Map<String, Object> paramMap) {
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Map<String, Object> timemap = new HashMap<>();
        timemap.put("time", "$MonitorTime");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59")).and("DayDataList.IsOverStandard").is(true);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("DayDataList"));
        operations.add(match(Criteria.where("DayDataList.PollutantCode").is(pollutantcode).and("DayDataList.IsOverStandard").is(true)));
        operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .and("DayDataList.PollutantCode").as("PollutantCode")
                .andExclude("_id"));
        operations.add(Aggregation.group("PollutantCode", "DataGatherCode")
                .push(timemap).as("timelist")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "DayData", Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<String> alarmtimelist = new ArrayList<>();
        if (mappedResults.size() > 0) {
            for (Document document : mappedResults) {
                List<Document> timelist = (List<Document>) document.get("timelist");
                for (Document timedoc : timelist) {
                    alarmtimelist.add(timedoc.getString("time"));
                }
            }
        }
        return alarmtimelist;
    }

    /**
     * @author: xsm
     * @date: 2022/05/17 17:30
     * @Description: 企业污染物排放量排名（小时/日/月/年排放量）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countEntPollutantDischargeRankByParam(String pollutantcode, String datatype, Date starttime, Date endtime, List<Integer> monitorPointTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        Criteria criteria = Criteria.where("MonitorTime").gte(starttime).lte(endtime);
        if (!pollutantcode.equals("totalflow")) {
            paramMap.put("pollutantcode", pollutantcode);
            criteria.and("MonthFlowDataList.PollutantCode").is(pollutantcode);
        }
        //添加数据权限
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("datauserid", userid);
        List<Map<String, Object>> outputs = new ArrayList<>();
        for (Integer i : monitorPointTypes) {
            CommonTypeEnum.MonitorPointTypeEnum monitorEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(i);
            paramMap.put("monitortype", i);
            switch (monitorEnum) {
                case WasteWaterEnum:
                case RainEnum:
                    outputs.addAll(waterOutputInfoMapper.getWaterOutPutPollutants(paramMap));
                    break;
                case SmokeEnum:
                case WasteGasEnum:
                    outputs.addAll(gasOutPutInfoMapper.getGasOutPutPollutants(paramMap));
                    break;
            }
        }
        String collection = "";
        String liststr = "";
        String flowkey = "";
        if ("hour".equals(datatype)) {
            collection = hourFlowCollection;
            liststr = "HourFlowDataList";
            flowkey = "AvgFlow";
        } else if ("day".equals(datatype)) {
            collection = dayFlowCollection;
            liststr = "DayFlowDataList";
            flowkey = "AvgFlow";
        } else if ("month".equals(datatype)) {
            collection = monthFlowCollection;
            liststr = "MonthFlowDataList";
            flowkey = "PollutantFlow";
        } else if ("year".equals(datatype)) {
            collection = yearFlowCollection;
            liststr = "YearFlowDataList";
            flowkey = "PollutantFlow";
        }
        Set<String> mns = outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toSet());
        criteria.and("DataGatherCode").in(mns);
        Aggregation aggregation = newAggregation(match(criteria));
        List<Document> documents = mongoTemplate.aggregate(aggregation, collection, Document.class).getMappedResults();
        //企业id mn号去重
        List<Map<String, Object>> list = outputs.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.get("PollutionID") + "#"
                        + o.get("MN")))),
                ArrayList::new));
        //根据企业id+名称分组
        Map<String, List<Map<String, Object>>> listMap = list.stream().collect(Collectors.groupingBy(m -> m.get("PollutionID").toString()
                + "#!-!#" + m.get("PollutionName").toString()
                + "#!-!#" + m.get("shortername").toString()));
        for (Map.Entry<String, List<Map<String, Object>>> entry : listMap.entrySet()) {
            List<Map<String, Object>> oneEnt = entry.getValue();
            double flow = 0;
            for (Map<String, Object> objectMap : oneEnt) {
                String mn = objectMap.get("MN").toString();
                for (Document document : documents) {
                    if (pollutantcode.equals("totalflow")) {    //总排放量
                        if (document.getString("DataGatherCode").equals(mn)) {
                            flow += Double.parseDouble(document.get("TotalFlow").toString());
                        }
                    } else {     //单个污染物排放量
                        if (document.getString("DataGatherCode").equals(mn)) {
                            List<Document> DataList = document.get(liststr, new ArrayList<Document>());
                            for (Document document1 : DataList) {
                                if (document1.get("PollutantCode").equals(pollutantcode)) {
                                    if (document1.get(flowkey) != null) {
                                        flow += Double.parseDouble(document1.get(flowkey).toString());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            String[] split = entry.getKey().split("#!-!#");
            Map<String, Object> mapInfo = new HashMap<>();
            mapInfo.put("pollutionid", split[0]);
            mapInfo.put("pollutionname", split[1]);
            mapInfo.put("shortername", split[2]);
            BigDecimal bigDecimal = BigDecimal.valueOf(flow);
            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            mapInfo.put("flow", value);
            result.add(mapInfo);
        }
        return result.stream().filter(m -> m.get("flow") != null).sorted(Comparator.comparingDouble(m -> Double.parseDouble(((Map) m).get("flow").toString())).reversed()).collect(Collectors.toList());
    }

    /**
     * @author: xsm
     * @date: 2022/05/19 14:58
     * @Description: 通过自定义参数获取非污水厂排口的总排放量情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @Override
    public List<Map<String, Object>> countGeneralWasteOutPutTotalFlowDataByParam(Map<String, Object> parammap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = (String) parammap.get("starttime");
        String endtime = (String) parammap.get("endtime");
        String datatype = (String) parammap.get("datatype");
        List<String> dgimns = (List<String>) parammap.get("dgimns");

        String collection = "";
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(datatype);
        if ("hour".equals(datatype)) {
            collection = hourFlowCollection;
        } else if ("day".equals(datatype)) {
            collection = dayFlowCollection;
        } else if ("month".equals(datatype)) {
            collection = monthFlowCollection;
        } else if ("year".equals(datatype)) {
            collection = yearFlowCollection;
        }

        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        criteria.and("MonitorTime").gte(DataFormatUtil.parseDate(starttime)).lte(DataFormatUtil.parseDate(endtime));
        aggregations.add(match(criteria));
        // 加8小时
        ProjectionOperation add8h = Aggregation.project("DataGatherCode", "MonitorTime", "TotalFlow", "FlowUnit")
                .andExpression("add(" + "MonitorTime" + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("DataGatherCode", "TotalFlow", "FlowUnit", "date8").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("MonitorDate");
        aggregations.add(add8h);
        aggregations.add(projects);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> mappedResults = aggregationResults.getMappedResults();
        //按时间排序
        Map<String, List<Document>> listMap = mappedResults.stream().filter(m -> m != null && m.get("MonitorDate") != null).collect(Collectors.groupingBy(m -> m.get("MonitorDate").toString()));
        for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
            List<Document> oneEnt = entry.getValue();
            double flow = 0;
            String unit = "";
            for (Document objdoc : oneEnt) {
                if ("".equals(unit)) {
                    unit = objdoc.getString("FlowUnit");
                }
                if (objdoc.get("TotalFlow") != null) {
                    flow += Double.parseDouble(objdoc.getString("TotalFlow"));
                }
            }
            Map<String, Object> mapInfo = new HashMap<>();
            mapInfo.put("monitortime", entry.getKey());
            mapInfo.put("unit", unit);
            BigDecimal bigDecimal = BigDecimal.valueOf(flow);
            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            mapInfo.put("flow", value);
            result.add(mapInfo);
        }
        if (result.size() > 0) {
            return result.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString())).collect(Collectors.toList());
        } else {
            return result;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 上午 10:52
     * @Description: 获取单个点位某条小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Document> getOnePointHourDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        String monitortime = paramMap.get("monitortime").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + ":00:00")).lte(DataFormatUtil.parseDate(monitortime + ":59:59"));
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime", "HourDataList"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 下午 13:17
     * @Description: 获取单个空气点位某个小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Document> getOneAirPointHourDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        String monitortime = paramMap.get("monitortime").toString();
        Criteria criteria = new Criteria();
        criteria.and("StationCode").is(dgimn).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + ":00:00")).lte(DataFormatUtil.parseDate(monitortime + ":59:59"));
        aggregations.add(match(criteria));
        aggregations.add(project("StationCode", "MonitorTime", "AQI", "AirLevel", "DataList"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "StationHourAQIData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 下午 13:17
     * @Description: 获取单个水质点位某个小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Document> getOneWaterQualityStationHourDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        String dgimn = paramMap.get("dgimn").toString();
        String monitortime = paramMap.get("monitortime").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(DataFormatUtil.parseDate(monitortime + ":00:00")).lte(DataFormatUtil.parseDate(monitortime + ":59:59"));
        aggregations.add(match(criteria));
        aggregations.add(project("DataGatherCode", "MonitorTime", "WaterLevel", "HourDataList"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 下午 13:17
     * @Description: 获取单个水质点位某个小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getDeviceConnectivityData(Map<String, Object> paramMap) {
        return deviceStatusMapper.countAllPointStatusNumForMonitorType(paramMap);
    }

    @Override
    public List<Map<String, Object>> getOutPutExceptionWorkOrderData(Map<String, Object> paramMap) {
        List<Map<String, Object>> devopslist = alarmTaskDisposeManagementMapper.getAllDevOpsTaskInfoByParams(paramMap);
        //获取分页后的任务ID
        List<String> taskids = new ArrayList<>();
        List<Map<String, Object>> onelist = new ArrayList<>();
        if (devopslist != null && devopslist.size() > 0) {
            //根据排口ID分组
            Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
            //MN_污染物 分组
            if (devopslist != null && devopslist.size() > 0) {
                listmap = devopslist.stream().collect(Collectors.groupingBy(m -> m.get("monitorpointid").toString()));
            }

            for (String id : listmap.keySet()) {
                List<Map<String, Object>> twolist = listmap.get(id);
                //按报警结束时间排序
                twolist = twolist.stream().sorted(Comparator.comparing(m -> ((Map) m).get("TaskEndTime").toString()).reversed()).collect(Collectors.toList());
                onelist.add(twolist.get(0));
            }
            taskids = onelist.stream().filter(m -> m.get("PK_TaskID") != null).map(m -> m.get("PK_TaskID").toString()).collect(Collectors.toList());
            paramMap.clear();

            paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
            paramMap.put("taskids", taskids);
            List<Map<String, Object>> wrw_listdata = new ArrayList<>();
            //获取污染物
            wrw_listdata = alarmTaskDisposeManagementMapper.getAlarmPollutantInfoByParamMap(paramMap);
            for (Map<String, Object> map : onelist) {
                String taskid = map.get("PK_TaskID").toString();
                //组装污染物数据
                String pollutantname = setTaskAlarmPollutantData(taskid, wrw_listdata);
                map.put("pollutantname", pollutantname);
            }
        }
        return onelist;
    }

    private String setTaskAlarmPollutantData(String taskid, List<Map<String, Object>> wrw_listdata) {
        String str = "";
        Set<String> set = new HashSet<>();
        for (Map<String, Object> map : wrw_listdata) {
            if (map.get("PK_TaskID") != null && taskid.equals(map.get("PK_TaskID").toString())) {
                String name = map.get("Name") != null ? map.get("Name").toString() : "";
                if (!set.contains(name)) {
                    set.add(name);
                    String str_1 = name + "【";
                    String str_2 = "";
                    String str_3 = "】";
                    for (Map<String, Object> obj : wrw_listdata) {
                        if (obj.get("PK_TaskID") != null && taskid.equals(obj.get("PK_TaskID").toString()) &&
                                obj.get("Name") != null && name.equals(obj.get("Name").toString())) {
                            String excstr = obj.get("AlarmType") != null ? obj.get("AlarmType").toString() : "";
                            if (!"".equals(excstr)) {
                                str_2 += CommonTypeEnum.ExceptionTypeEnum.getNameByCode(excstr) + "、";
                            }

                        }
                    }
                    if (!"".equals(str_2)) {
                        str_2 = str_2.substring(0, str_2.length() - 1);
                        str += str_1 + str_2 + str_3 + "、";
                    }
                } else {
                    continue;
                }
            }
        }
        if (!"".equals(str)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * 获取企业下各点位的报警时长
     */
    @Override
    public List<Map<String, Object>> countEntPointOverAlarmTimesDataByParam(Map<String, Object> paramMap) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> dgimns = (List<String>) paramMap.get("dgimns");
            String monitortime = paramMap.get("monitortime").toString();
            Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> mnandtype = (Map<String, Object>) paramMap.get("mnandtype");
            Map<String, Object> mnandid = (Map<String, Object>) paramMap.get("mnandid");
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            Date startDate = DataFormatUtil.parseDate(monitortime + " 00:00:00");
            Date endDate = DataFormatUtil.parseDate(monitortime + " 23:59:59");
            timefield = "FirstOverTime";
            collection = overModelCollection;
            lasttimestr = "LastOverTime";
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("MN").in(dgimns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", lasttimestr, timefield, "PollutantCode")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$" + timefield);
            childmap.put("endtime", "$" + lasttimestr);
            operations.add(
                    Aggregation.group("MN").max(lasttimestr).as("lasttime").last("PollutantCode").as("code")
                            .push(childmap).as("timelist")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (listItems.size() > 0) {
                List<Document> podoc;
                String type;
                List<Map<String, Object>> datalist;
                for (Document document : listItems) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("mn", document.getString("_id"));
                    onemap.put("pointname", mnandname.get(document.getString("_id")));
                    type = mnandtype.get(document.getString("_id")) != null ? mnandtype.get(document.getString("_id")).toString() : "";
                    onemap.put("type", type);
                    onemap.put("monitorpointid", mnandid.get(document.getString("_id")));
                    onemap.put("pollutantcode", document.getString("code"));
                    onemap.put("pollutantname", !"".equals(type) ? codeandname.get(document.getString("code") + "_" + type) : "");
                    onemap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                    podoc = (List<Document>) document.get("timelist");
                    datalist = countSumOnePointAlarmTimesTwo(podoc, monitortime);
                    onemap.put("overtimes", datalist);
                    result.add(onemap);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据用户ID获取数据权限点位信息
     */
    @Override
    public List<Map<String, Object>> getUserMonitorPointRelationDataByUserId(Map<String, Object> paramMap) {
        return userMonitorPointRelationDataMapper.getUserMonitorPointRelationDataByParams(paramMap);
    }

    /**
     * 统计单点位每小时报警总时长(分钟)
     */
    private List<Map<String, Object>> countSumOnePointAlarmTimesTwo(List<Document> timelist, String monitortime) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> onelist = new ArrayList<>();
            if (timelist != null && timelist.size() > 0) {
                String onestarttime = null;
                String oneenttime = null;
                for (Document onedocument : timelist) {
                    onestarttime = DataFormatUtil.getDateHM(onedocument.getDate("starttime"));
                    String hourone = onestarttime.substring(0, onestarttime.length() - 3);
                    String minuteone = onestarttime.substring(3, onestarttime.length());
                    oneenttime = DataFormatUtil.getDateHM(onedocument.getDate("endtime"));
                    String minutetwo = oneenttime.substring(3, oneenttime.length());
                    String hourtwo = oneenttime.substring(0, oneenttime.length() - 3);
                    if (hourone.equals(hourtwo)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("min", minuteone);
                        map.put("max", minutetwo);
                        map.put("hournum", Integer.valueOf(hourone));
                        onelist.add(map);
                    } else {
                        for (int i = Integer.valueOf(hourone); i <= (Integer.valueOf(hourtwo)); i++) {
                            Map<String, Object> map = new HashMap<>();
                            if (i == Integer.valueOf(hourone)) {
                                map.put("min", minuteone);
                            } else {
                                map.put("min", "0");
                            }
                            if (i != (Integer.valueOf(hourtwo))) {
                                map.put("max", "60");
                            } else {
                                map.put("max", minutetwo);
                            }
                            map.put("hournum", i);
                            onelist.add(map);
                        }
                    }
                }
            }
            if (onelist != null && onelist.size() > 0) {
                Map<String, List<Map<String, Object>>> mapDocuments = new HashMap<>();
                mapDocuments = onelist.stream().collect(Collectors.groupingBy(m -> m.get("hournum").toString()));
                for (int i = 0; i < 24; i++) {
                    if (mapDocuments.get(i + "") != null) {
                        List<Map<String, Object>> twolist = mapDocuments.get(i + "");
                        String min = "";
                        String max = "";
                        int totalminute = 0;
                        twolist = twolist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("min").toString()))).collect(Collectors.toList());
                        for (Map<String, Object> onemap : twolist) {
                            if ("".equals(min) && "".equals(max)) {
                                min = onemap.get("min").toString();
                                max = onemap.get("max").toString();
                            } else {
                                //比较时间段
                                //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                if (Integer.valueOf(onemap.get("min").toString()) >= Integer.valueOf(min) && Integer.valueOf(onemap.get("min").toString()) <= Integer.valueOf(max)) {
                                    if (Integer.valueOf(onemap.get("max").toString()) > Integer.valueOf(max)) {
                                        max = onemap.get("max").toString();
                                    }
                                } else {
                                    //第二次报警时段不被包含于第一个报警时段中
                                    if (min.equals(max)) {
                                        totalminute += 1;
                                    } else {
                                        totalminute += Integer.valueOf(max) - Integer.valueOf(min);
                                    }
                                    min = onemap.get("min").toString();
                                    max = onemap.get("max").toString();
                                }
                            }

                        }
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("unit", "分钟");
                        if (!"".equals(min) && !"".equals(max)) {
                            if (min.equals(max)) {
                                dataMap.put("alarmtimevalue", totalminute + 1);
                            } else {
                                dataMap.put("alarmtimevalue", totalminute + (Integer.valueOf(max) - Integer.valueOf(min)));
                            }
                        } else {
                            dataMap.put("alarmtimevalue", 0);
                        }
                        if (i < 10) {
                            dataMap.put("monitortime", monitortime + " 0" + i);
                        } else {
                            dataMap.put("monitortime", monitortime + " " + i);
                        }
                        dataList.add(dataMap);
                    }
                }
            }

            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private String countHourMinuteTime(int tatalnum) {
        String str = "";
        if (tatalnum < 60) {
            str = tatalnum + "分钟";
        } else if (tatalnum == 60) {
            str = "1小时";
        } else {
            int onenum = tatalnum / 60;
            str = onenum + "小时" + ((tatalnum - onenum * 60) > 0 ? (tatalnum - onenum * 60) + "分钟" : "");
        }
        return str;
    }
}
