package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.common.pubcode.PubCodeMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterStationMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.service.environmentalprotection.online.OnlineWaterQualityService;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineWaterQualityServiceImpl implements OnlineWaterQualityService {
    private final WaterStationMapper waterStationMapper;

    public OnlineWaterQualityServiceImpl(WaterStationMapper waterStationMapper) {
        this.waterStationMapper = waterStationMapper;
    }

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private PubCodeMapper pubCodeMapper;
    private final String DB_WaterEvaluateData = "WaterStationEvaluateData";
    private final String DB_HourData = "HourData";
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;

    /**
     * @author: zhangzc
     * @date: 2019/9/18 15:25
     * @Description: 获取水质监测站信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWaterQualityStationByParamMap(Map<String, Object> paramMap) {
        return waterStationMapper.getOnlineWaterStationInfoByParamMap(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/11/19 0019 下午 3:21
     * @Description: 自定义查询条件查询水质评价标准数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getWaterQualityStandardByParam(Map<String, Object> paramMap) {
        return waterStationMapper.getWaterQualityStandardByParam(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/5/14 0014 下午 2:53
     * @Description: 设置实时水质等级
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datalist]
     * @throws:
     */
    @Override
    public void setWaterQaulity(List<Map<String, Object>> datalist) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tablename", "PUB_CODE_WaterQualityClass");
        List<Map<String, Object>> waterclass = pubCodeMapper.getPubCodeDataByParam(paramMap);
        Set<String> dgimns = datalist.stream().filter(m -> m.get("DGIMN") != null || m.get("dgimn") != null).map(m -> m.get("DGIMN") == null ?
                m.get("dgimn") == null ? "" : m.get("dgimn").toString() : m.get("DGIMN").toString()).collect(Collectors.toSet());

        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        List<Document> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria),
                group("DataGatherCode").max("MonitorTime").as("lastTime")),
                "RealTimeData",
                Document.class).getMappedResults();
        if (mappedResults.size() == 0) {
            return;
        }
        Criteria criteria1 = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();
        for (Document mappedResult : mappedResults) {
            criteriaList.add(Criteria.where("DataGatherCode").is(mappedResult.get("_id")).and("MonitorTime").is(mappedResult.get("lastTime")));
        }
        criteria1.orOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

        List<Document> hourData = mongoTemplate.aggregate(newAggregation(match(criteria1)), "RealTimeData", Document.class).getMappedResults();


        for (Map<String, Object> map : datalist) {
            map.put("WaterLevel", "");
            String dgimn = map.get("DGIMN") == null ? map.get("dgimn") == null ? "" : map.get("dgimn").toString() : map.get("DGIMN").toString();
            Optional<Document> first = hourData.stream().filter(m -> m.get("DataGatherCode") != null && dgimn.equals(m.get("DataGatherCode").toString())).findFirst();
            if (first.isPresent()) {
                Document document = first.get();
                String WaterLevel = document.get("WaterLevel") == null ? "" : document.get("WaterLevel").toString();
                map.put("WaterLevel", WaterLevel);
                String WaterLevelName = waterclass.stream().filter(m -> m.get("Code") != null && m.get("Name") != null && WaterLevel.equals(m.get("Code").toString())).map(m -> m.get("Name").toString()).findFirst().orElse("");
                map.put("WaterLevelName", WaterLevelName);
            }
        }
    }

    @Override
    public List<Document> setWaterQualityDataByParam(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("mns");
        Aggregation aggregation = newAggregation(
                match(Criteria.where("DataGatherCode").in(mns)
                        .and("DataType").is("HourData")
                        .and("EvaluateTime").is(paramMap.get("monitortime"))),
                project("DataGatherCode", "WaterQualityClass")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, DB_WaterEvaluateData, Document.class);
        List<Document> documents = results.getMappedResults();
        return documents;
    }

    /**
     * @author: xsm
     * @date: 2022/06/15 0015 上午 9:46
     * @Description: 通过数据类型标记（实时数据-1，分钟数据-2，小时数据-3，日数据-4）自定义参数获取单个水质站点单污染物列表页面数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datamark, monitorpointid, pollutantcode,chartorlist:1图2列表， starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @Override
    public Map<String, Object> getOneWaterStationOnePollutantDataByParams(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        String sort = paramMap.get("sort").toString();
        String datamark = paramMap.get("datamark").toString();
        String dgimn = paramMap.get("mn").toString();
        String collection = paramMap.get("collection").toString();
        resultmap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
        Map<String, String> codeAndName = new HashMap<>();
        List<Map<String, Object>> pollutant = pollutantFactorMapper.getPollutantsByCodesAndType(resultmap);
        for (Map<String, Object> map : pollutant) {
            codeAndName.put(map.get("code").toString(), map.get("name").toString());
        }
        //获取所有水质级别等级
        List<Map<String, Object>> qaulitylist = waterStationMapper.getAllWaterQualityLevelData();
        Map<String, Object> levelandname = new HashMap<>();
        for (Map<String, Object> map : qaulitylist) {
            if (map.get("code") != null && map.get("levelnum") != null) {
                levelandname.put(map.get("code").toString(), map.get("name"));
            }
        }
        resultmap.clear();
        String liststr = "";
        String valuestr = "";
        String timestr = "";
        if ("1".equals(datamark)) {
            valuestr = "MonitorValue";
            liststr = "RealDataList";
            timestr = "%Y-%m-%d %H:%M:%S";
        } else if ("2".equals(datamark)) {
            valuestr = "AvgStrength";
            liststr = "MinuteDataList";
            timestr = "%Y-%m-%d %H:%M";
        } else if ("3".equals(datamark)) {
            valuestr = "AvgStrength";
            liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
        } else if ("4".equals(datamark)) {
            valuestr = "AvgStrength";
            liststr = "DayDataList";
            timestr = "%Y-%m-%d";
        } else if ("5".equals(datamark)) {
            valuestr = "AvgStrength";
            liststr = "MonthDataList";
            timestr = "%Y-%m";
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime));
        List<AggregationOperation> operations2 = new ArrayList<>();
        if ("waterquality".equals(pollutantcode)) {//水质类别
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                List<AggregationOperation> operations = new ArrayList<>();
                long totalCount = 0;
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", "WaterLevel", liststr).and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                        .andExclude("_id"));
                //获取分组总数
                Aggregation aggregationCount = Aggregation.newAggregation(operations);
                AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
                totalCount = resultsCount.getMappedResults().size();
                resultmap.put("total", totalCount);
            }
            //数据
            operations2.add(Aggregation.match(criteria));
            operations2.add(Aggregation.project("DataGatherCode", "WaterLevel", liststr).and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            if ("asc".equals(sort)) {
                operations2.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            } else {
                operations2.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations2.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations2.add(Aggregation.limit(pageEntity.getPageSize()));
            }
        } else {//其它污染物
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                List<AggregationOperation> operations = new ArrayList<>();
                long totalCount = 0;
                operations.add(Aggregation.match(criteria));
                operations.add(unwind(liststr));
                operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
                operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                        .and(liststr + ".WaterLevel").as("codewaterlevel")
                        .and(liststr + ".PollutantCode").as("code")
                        .and(liststr + "." + valuestr).as("value")
                        .andExclude("_id"));
                //获取分组总数
                Aggregation aggregationCount = Aggregation.newAggregation(operations);
                AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
                totalCount = resultsCount.getMappedResults().size();
                resultmap.put("total", totalCount);
            }
            //数据
            operations2.add(Aggregation.match(criteria));
            operations2.add(unwind(liststr));
            operations2.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations2.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(liststr + ".WaterLevel").as("codewaterlevel")
                    .and(liststr + ".PollutantCode").as("code")
                    .and(liststr + ".IsOverStandard").as("IsOverStandard")
                    .and(liststr + ".IsOver").as("IsOver")
                    .and(liststr + "." + valuestr).as("value")
                    .andExclude("_id"));
            if ("asc".equals(sort)) {
                operations2.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            } else {
                operations2.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime"));
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations2.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations2.add(Aggregation.limit(pageEntity.getPageSize()));
            }
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations2)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        String overstr = "";
        String targetlevel = "";
        if (paramMap.get("targetlevel") != null && !"".equals(paramMap.get("targetlevel").toString())) {
            targetlevel = paramMap.get("targetlevel").toString();
        }
        List<Document> podoc;
        String code;
        String codelevel;
        double overmultiple;
        int isover = -1;
        if (mappedResults.size() > 0) {
            for (Document document : mappedResults) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", document.get("MonitorTime"));
                if ("waterquality".equals(pollutantcode)) {
                    map.put("levelcode", document.get("WaterLevel"));
                    map.put("value", document.get("WaterLevel") != null ? levelandname.get(document.getString("WaterLevel")) : "");
                    //判断当前点位水质 是否 大于点位目标水质
                    if (document.get("WaterLevel") != null && !"".equals(targetlevel)) {
                        if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(document.get("WaterLevel").toString()) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(targetlevel)) {
                            //大于
                            //判断超标污染物及超标倍数
                            if (document.get(liststr) != null) {
                                podoc = (List<Document>) document.get(liststr);
                                for (Document doc : podoc) {
                                    if (doc.get("WaterLevel") != null) {
                                        code = doc.getString("PollutantCode");
                                        codelevel = doc.getString("WaterLevel");
                                        overmultiple = doc.getDouble("OverMultiple");
                                        if (CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(codelevel) > CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(targetlevel)) {
                                            overstr = codeAndName.get(code) + (overmultiple > 0 ? "(" + overmultiple + ")" : "") + "、";
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!"".equals(overstr)) {
                        overstr = overstr.substring(0, overstr.length() - 1);
                    }
                    map.put("overstr", overstr);
                } else {
                    map.put("value", document.get("value"));
                    if (document.get("codewaterlevel") != null && !"".equals(document.getString("codewaterlevel"))) {
                        map.put("waterlevel", document.get("codewaterlevel"));
                        map.put("waterlevelname", levelandname.get(document.getString("codewaterlevel")));
                    } else {
                        map.put("waterlevel", "Ⅰ");
                        map.put("waterlevelname", "Ⅰ类");
                    }
                    //判断污染物浓度超标
                    if (document.get("IsOver") != null) {
                        isover = document.getInteger("IsOver");
                    }
                    if (document.get("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {
                        isover = 4;
                    }
                    map.put("isover", isover);
                }
                if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                    result.add(map);
                } else {
                    if (map.get("value") != null && !"".equals(map.get("value").toString())) {
                        result.add(map);
                    }
                }

            }
        }
        resultmap.put("datalist", result);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2022/06/15 0015 下午 14:51
     * @Description: 获取水质污染物报警排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterStationPollutantAlarmRankDataByParams(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String dgimn = paramMap.get("mn").toString();
        paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
        Map<String, String> codeAndName = new HashMap<>();
        List<Map<String, Object>> pollutant = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : pollutant) {
            codeAndName.put(map.get("code").toString(), map.get("name").toString());
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("OverTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime)).and("DataType").is("HourData");
        List<AggregationOperation> operations = new ArrayList<>();
        //数据
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", "PollutantCode"));
        operations.add(Aggregation.group("DataGatherCode", "PollutantCode").count().as("countnum"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "OverData", Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        if (mappedResults.size() > 0) {
            for (Document document : mappedResults) {
                Map<String, Object> map = new HashMap<>();
                map.put("pollutantcode", document.get("PollutantCode"));
                map.put("pollutantname", codeAndName.get(document.getString("PollutantCode")));
                map.put("count", document.get("countnum") != null ? document.getInteger("countnum") : 0);
                result.add(map);
            }
        }
        if (result.size() > 0) {
            result = result.stream().sorted(
                    Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("countnum").toString())).reversed()).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * @Description: 获取水质监测点当前、同比、环比分析数据
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/06/16 9:28
     */
    @Override
    public List<Document> getWaterStationContrastDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        String datatype = paramMap.get("datatype").toString();
        String dgimn = paramMap.get("dgimn").toString();
        String collection = "";
        String liststr = "";
        String valuestr = "";
        String timestr = "";
        if ("hour".equals(datatype)) {
            starttime = starttime + ":00:00";
            endtime = endtime + ":59:59";
            collection = "HourData";
            valuestr = "AvgStrength";
            liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
        } else if ("day".equals(datatype)) {
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            collection = "DayData";
            valuestr = "AvgStrength";
            liststr = "DayDataList";
            timestr = "%Y-%m-%d";
        } else if ("month".equals(datatype)) {
            starttime = starttime + "-01 00:00:00";
            endtime = DataFormatUtil.getLastDayOfMonth(endtime) + " 23:59:59";
            collection = "MonthData";
            valuestr = "AvgStrength";
            liststr = "MonthDataList";
            timestr = "%Y-%m";
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(dgimn).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime));
        List<AggregationOperation> operations = new ArrayList<>();
        if ("waterquality".equals(pollutantcode)) {//水质类别
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "WaterLevel").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        } else {//其它污染物
            //数据
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(liststr));
            operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(liststr + ".PollutantCode").as("code")
                    .and(liststr + "." + valuestr).as("value")
                    .andExclude("_id"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        return mappedResults;
    }

    /**
     * @author: xsm
     * @date: 2022/06/16 0016 下午 15:17
     * @Description: 通过自定义参数获取多个水质站点单污染物图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getManyWaterStationOnePollutantDataByParams(Map<String, Object> paramMap) {
        Map<String, Object> resultmap = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        String datatype = paramMap.get("datatype").toString();
        String sort = paramMap.get("sort").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Map<String, Object> mnandname = (Map<String, Object>) paramMap.get("mnandname");
        /*paramMap.clear();
        paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
        Map<String, String> codeAndName = new HashMap<>();
        List<Map<String, Object>> pollutant = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> map : pollutant) {
            codeAndName.put(map.get("code").toString(), map.get("name").toString());
        }*/
        //获取所有水质级别等级
        List<Map<String, Object>> qaulitylist = waterStationMapper.getAllWaterQualityLevelData();
        Map<String, Object> levelandname = new HashMap<>();
        for (Map<String, Object> map : qaulitylist) {
            if (map.get("code") != null && map.get("levelnum") != null) {
                levelandname.put(map.get("code").toString(), map.get("name"));
            }
        }
        String collection = "";
        String liststr = "";
        String valuestr = "";
        String timestr = "";
        if ("hour".equals(datatype)) {
            starttime = starttime + ":00:00";
            endtime = endtime + ":59:59";
            collection = "HourData";
            valuestr = "AvgStrength";
            liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
        } else if ("day".equals(datatype)) {
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            collection = "DayData";
            valuestr = "AvgStrength";
            liststr = "DayDataList";
            timestr = "%Y-%m-%d";
        } else if ("month".equals(datatype)) {
            starttime = starttime + "-01 00:00:00";
            String lastDay = DataFormatUtil.getLastDayOfMonth(endtime);
            endtime = lastDay + " 23:59:59";
            collection = "MonthData";
            valuestr = "AvgStrength";
            liststr = "MonthDataList";
            timestr = "%Y-%m";
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime));
        List<AggregationOperation> operations2 = new ArrayList<>();
        if ("waterquality".equals(pollutantcode)) {//水质类别
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                List<AggregationOperation> operations = new ArrayList<>();
                long totalCount = 0;
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", "WaterLevel").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                        .andExclude("_id"));
                operations.add(Aggregation.group("MonitorTime"));
                //获取分组总数
                Aggregation aggregationCount = Aggregation.newAggregation(operations);
                AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
                totalCount = resultsCount.getMappedResults().size();
                resultmap.put("total", totalCount);
            }
            Map<String, Object> mnvaluemap = new HashMap<>();
            mnvaluemap.put("mn", "$DataGatherCode");
            mnvaluemap.put("waterlevel", "$WaterLevel");
            //数据
            operations2.add(Aggregation.match(criteria));
            operations2.add(Aggregation.project("DataGatherCode", "WaterLevel").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            operations2.add(Aggregation.group("MonitorTime").push(mnvaluemap).as("valuelist"));
            if ("asc".equals(sort)) {
                operations2.add(Aggregation.sort(Sort.Direction.ASC, "_id"));
            } else {
                operations2.add(Aggregation.sort(Sort.Direction.DESC, "_id"));
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations2.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations2.add(Aggregation.limit(pageEntity.getPageSize()));
            }
        } else {//其它污染物
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                List<AggregationOperation> operations = new ArrayList<>();
                long totalCount = 0;
                operations.add(Aggregation.match(criteria));
                operations.add(unwind(liststr));
                operations.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
                operations.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                        .and(liststr + ".WaterLevel").as("codewaterlevel")
                        .and(liststr + ".PollutantCode").as("code")
                        .and(liststr + "." + valuestr).as("value")
                        .andExclude("_id"));
                operations.add(Aggregation.group("MonitorTime"));
                //获取分组总数
                Aggregation aggregationCount = Aggregation.newAggregation(operations);
                AggregationResults<Document> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Document.class);
                totalCount = resultsCount.getMappedResults().size();
                resultmap.put("total", totalCount);
            }
            //数据
            Map<String, Object> mnvaluemap = new HashMap<>();
            mnvaluemap.put("mn", "$DataGatherCode");
            mnvaluemap.put("waterlevel", "$WaterLevel");
            mnvaluemap.put("IsOverStandard", "$IsOverStandard");
            mnvaluemap.put("IsOver", "$IsOver");
            mnvaluemap.put("value", "$value");
            operations2.add(Aggregation.match(criteria));
            operations2.add(unwind(liststr));
            operations2.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations2.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(liststr + ".WaterLevel").as("WaterLevel")
                    .and(liststr + ".PollutantCode").as("code")
                    .and(liststr + ".IsOverStandard").as("IsOverStandard")
                    .and(liststr + ".IsOver").as("IsOver")
                    .and(liststr + "." + valuestr).as("value")
                    .andExclude("_id"));
            operations2.add(Aggregation.group("MonitorTime").push(mnvaluemap).as("valuelist"));
            if ("asc".equals(sort)) {
                operations2.add(Aggregation.sort(Sort.Direction.ASC, "_id"));
            } else {
                operations2.add(Aggregation.sort(Sort.Direction.DESC, "_id"));
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations2.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations2.add(Aggregation.limit(pageEntity.getPageSize()));
            }
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations2)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<Document> valuelist;
        if (mappedResults.size() > 0) {
            for (Document document : mappedResults) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", document.get("_id"));
                valuelist = (List<Document>) document.get("valuelist");
                for (String mn : dgimns) {
                    map.put("pointname_" + mn, mnandname.get(mn));
                    setOtherKeyValueData(valuelist, map, mn, pollutantcode, levelandname);
                }
                result.add(map);
            }
        }
        resultmap.put("datalist", result);
        return resultmap;
    }

    private void setOtherKeyValueData(List<Document> valuelist, Map<String, Object> map, String mn, String pollutantcode, Map<String, Object> levelandname) {
        int isover = -1;
        if ("waterquality".equals(pollutantcode)) {
            map.put("value_" + mn, "");
        } else {
            map.put("value_" + mn, "");
            map.put("isover_" + mn, -1);
        }
        if (valuelist != null && valuelist.size() > 0) {
            for (Document document : valuelist) {
                if (document.getString("mn").equals(mn)) {
                    if ("waterquality".equals(pollutantcode)) {
                        map.put("value_" + mn, document.get("waterlevel") != null ? levelandname.get(document.getString("waterlevel")) : "");

                    } else {
                        map.put("value_" + mn, document.get("value"));
                        map.put("waterlevel_" + mn, document.get("waterlevel") != null ? levelandname.get(document.get("waterlevel").toString()) : "Ⅰ类");
                        //判断污染物浓度超标
                        if (document.get("IsOver") != null) {
                            isover = document.getInteger("IsOver");
                        }
                        if (document.get("IsOverStandard") != null && document.getBoolean("IsOverStandard") == true) {
                            isover = 4;
                        }
                        map.put("isover_" + mn, isover);
                    }
                    break;
                }
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/17 0017 下午 14:14
     * @Description: 通过自定义参数获取多个水质站点24小时监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:datatype:1 浓度对比
     * @throws:
     */
    @Override
    public List<Document> getManyWaterStationPollutantChangeDataByParams(Map<String, Object> paramMap) {
        String monitortime = paramMap.get("monitortime").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        String collection = "HourData";
        String valuestr = "AvgStrength";
        String liststr = "HourDataList";
        String timestr = "%Y-%m-%d %H";
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59"));
        List<AggregationOperation> operations2 = new ArrayList<>();
        if ("waterquality".equals(pollutantcode)) {//水质类别
            Map<String, Object> mnvaluemap = new HashMap<>();
            mnvaluemap.put("monitortime", "$MonitorTime");
            mnvaluemap.put("waterlevel", "$WaterLevel");
            //数据
            operations2.add(Aggregation.match(criteria));
            operations2.add(Aggregation.project("DataGatherCode", "WaterLevel").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .andExclude("_id"));
            operations2.add(Aggregation.group("DataGatherCode").push(mnvaluemap).as("valuelist"));
        } else {//其它污染物
            //数据
            Map<String, Object> mnvaluemap = new HashMap<>();
            mnvaluemap.put("monitortime", "$MonitorTime");
            mnvaluemap.put("waterlevel", "$WaterLevel");
            mnvaluemap.put("value", "$value");
            operations2.add(Aggregation.match(criteria));
            operations2.add(unwind(liststr));
            operations2.add(match(Criteria.where(liststr + ".PollutantCode").is(pollutantcode)));
            operations2.add(Aggregation.project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    .and(liststr + ".WaterLevel").as("WaterLevel")
                    .and(liststr + "." + valuestr).as("value")
                    .andExclude("_id"));
            operations2.add(Aggregation.group("DataGatherCode").push(mnvaluemap).as("valuelist"));

        }
        Aggregation aggregationList = Aggregation.newAggregation(operations2)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        return mappedResults;
    }

    /**
     * 获取所有水质级别
     */
    @Override
    public List<Map<String, Object>> getAllWaterQualityLevelData() {
        return waterStationMapper.getAllWaterQualityLevelData();
    }

    @Override
    public List<Document> getWaterQualityAssessmentDataByParams(Map<String, Object> paramMap) {
        String monitortime = paramMap.get("monitortime").toString();
        String datatype = paramMap.get("datatype").toString();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        String starttime = "";
        String endtime = "";
        String collection = "";
        String liststr = "";
        String timestr = "";
        if ("hour".equals(datatype)) {
            starttime = monitortime + ":00:00";
            endtime = monitortime + ":59:59";
            collection = "HourData";
            liststr = "HourDataList";
            timestr = "%Y-%m-%d %H";
        } else if ("day".equals(datatype)) {
            starttime = monitortime + " 00:00:00";
            endtime = monitortime + " 23:59:59";
            collection = "DayData";
            liststr = "DayDataList";
            timestr = "%Y-%m-%d";
        }
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime));
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("DataGatherCode", liststr, "WaterLevel").and(DateOperators.DateToString.dateOf("MonitorTime").toString(timestr).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                .andExclude("_id"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        return mappedResults;
    }

    @Override
    public List<Map<String, Object>> getPollutantSetDataListByParam(Map<String, Object> paramMap) {
        return waterStationMapper.getPollutantSetDataListById(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPubStandardListByParam(Map<String, Object> paramMap) {
        return waterStationMapper.getPubStandardListByParam(paramMap);
    }

    @Override
    public List<Document> getHourMonitorDataByParam(Map<String, Object> paramMap) {

        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("MonitorTime")
                .gte(DataFormatUtil.getDateYMDHMS(starttime))
                .lte(DataFormatUtil.getDateYMDHMS(endtime));
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("HourDataList"));
        operations.add(match(Criteria.where("HourDataList.PollutantCode").in(pollutantcodes)));
        operations.add(Aggregation.project("DataGatherCode")
                .and( "WaterLevel").as("PWaterLevel")
                .and( "HourDataList.PollutantCode").as("code")
                .and( "HourDataList.AvgStrength").as("value")
                .and( "HourDataList.WaterLevel").as("WaterLevel")
                .andExclude("_id"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, DB_HourData, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        return documents;
    }

}
