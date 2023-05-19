package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.environmentalprotection.online.OnlineDataCountService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: zhangzc
 * @date: 2019/8/15 13:51
 * @Description: 统计报警数据
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@Service
public class OnlineDataCountServiceImpl implements OnlineDataCountService {
    private final MongoTemplate mongoTemplate;

    private final String DB_OverModel = "OverModel";
    private final String DB_WaterDetectData = "WaterDetectData";
    public OnlineDataCountServiceImpl(@Qualifier("primaryMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    @Override
    public List<Document> getMonUnWindDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString()))
                    .lte(DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString()));
        }
        String unwindKey = paramMap.get("unwind").toString();
        List<String> pollutantcodes = (List<String>) paramMap.get("pollutantcodes");
        criteria.and(unwindKey + "." + "PollutantCode").in(pollutantcodes);
        aggregations.add(match(criteria));
        aggregations.add(unwind(unwindKey));
        aggregations.add(match(Criteria.where(unwindKey + "." + "PollutantCode").in(pollutantcodes)));
        String valueKey = paramMap.get("valuekey").toString();
        ProjectionOperation projects = project().and("DataGatherCode").as("DataGatherCode")
                .and("MonitorTime").as("MonitorTime")
                .and(unwindKey + "." + "PollutantCode").as("PollutantCode")
                .and(unwindKey + "." + valueKey).as("value");
        aggregations.add(projects);
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public List<Document> getMongodbDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString()))
                    .lte(DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString()));
        }
        aggregations.add(match(criteria));
        String valueKey = paramMap.get("valuekey").toString();
        ProjectionOperation projects = project().and("DataGatherCode").as("DataGatherCode")
                .and("MonitorTime").as("MonitorTime").and(valueKey).as("value");
        aggregations.add(projects);
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public List<Document> getOverModelDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Criteria criteria = Criteria.where("MN").in(dgimns);
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            criteria.and("FirstOverTime").gte(DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString()))
                    .lte(DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString()));
        }
        if (paramMap.get("pollutantcodes")!=null){
            List<String> polluantcodes = (List<String>) paramMap.get("pollutantcodes");
            criteria.and("PollutantCode").in(polluantcodes);
        }
        criteria.and("DataType").in(Arrays.asList(
                CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName()
                ,CommonTypeEnum.MongodbDataTypeEnum.MinuteDataEnum.getName()));
        aggregations.add(match(criteria));
        ProjectionOperation projects = project().and("MN").as("DataGatherCode")
                .and("FirstOverTime").as("FirstOverTime")
                .and("LastOverTime").as("LastOverTime")
                .and("PollutantCode").as("PollutantCode");
        aggregations.add(projects);
        aggregations.add(sort(Sort.Direction.ASC, "FirstOverTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, DB_OverModel, Document.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public List<Document> getWaterDetectDataByParam(Map<String, Object> paramMap) {

        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        if (paramMap.get("starttime") != null && paramMap.get("endtime") != null) {
            criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMD(paramMap.get("starttime").toString()))
                    .lte(DataFormatUtil.getDateYMD(paramMap.get("endtime").toString()));
        }
        aggregations.add(match(criteria));
        ProjectionOperation projects = project().and("DataGatherCode").as("DataGatherCode")
                .and("WaterQualityClass").as("WaterQualityClass")
                .and("MonitorTime").as("MonitorTime");
        aggregations.add(projects);
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, DB_WaterDetectData, Document.class);
        return aggregationResults.getMappedResults();


    }

    /**
     * @author: xsm
     * @date: 2022/03/25 0025 上午 11:22
     * @Description: 获取某个企业某段时间内某类型某污染物每小时浓度变化趋势及同比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, pollutionid, pollutantcode]
     * @throws:
     */
    @Override
    public List<Document> getEntPollutantHourOnlineDataByParams(String starttime, String endtime, List<String> mns, String pollutantcode) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(mns);
        criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime))
                .lte(DataFormatUtil.getDateYMDHMS(endtime));
        criteria.and("HourDataList.PollutantCode").is(pollutantcode);
        aggregations.add(match(criteria));
        aggregations.add(unwind("HourDataList"));
        aggregations.add(match(Criteria.where("HourDataList.PollutantCode").is(pollutantcode)));
        ProjectionOperation projects = project().and("DataGatherCode").as("DataGatherCode")
                .and("MonitorTime").as("MonitorTime")
                .and( "HourDataList.PollutantCode").as("PollutantCode")
                .and("HourDataList.AvgStrength").as("value");
        aggregations.add(projects);
        aggregations.add(sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "HourData", Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * 获取河流断面单污染物的监测数据
     * */
    @Override
    public List<Document> getWaterDetectOnePollutantDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> dgimns = (List<String>) paramMap.get("dgimns");
        String code = (String) paramMap.get("pollutantcode");
        Criteria criteria = new Criteria();
        if (paramMap.get("starttime")!=null){
            criteria.and("DataGatherCode").in(dgimns).and("MonitorTime").gte(DataFormatUtil.getDateYMD(paramMap.get("starttime").toString())).lte(DataFormatUtil.getDateYMD(paramMap.get("endtime").toString()));
        }else{
            criteria.and("DataGatherCode").in(dgimns);
        }
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("DataList"));
        operations.add(match(Criteria.where("DataList.PollutantCode").is(code)));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime").and("WaterQualityClass").as("pointquality")
                .and("DataList.PollutantCode").as("code")
                .and("DataList.WaterQualityClass").as("codequality")
                .and("DataList.IsOverStandard").as("isover")
                .and("DataList.MonitorValue").as("value").andExclude("_id"));
        aggregations.add(sort(Sort.Direction.DESC, "MonitorTime"));
        operations.add(group("DataGatherCode", "MonitorTime","code")
                .first("pointquality").as("pointquality")
                .first("value").as("value")
                .first("codequality").as("codequality")
                .first("isover").as("isover")
        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, DB_WaterDetectData, Document.class);
        return pageResults.getMappedResults();


    }

    @Override
    public Document getOneRiverSectionLastDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        String dgimn = (String) paramMap.get("dgimn");
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        Document document = mongoTemplate.findOne(query, Document.class, DB_WaterDetectData);
        return document;
    }

    @Override
    public List<Document> getPollutantFlowDataByParam(Map<String, Object> paramMap) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        String startDate = (String) paramMap.get("startDate");
        String endDate = (String) paramMap.get("endDate");
        String dataKey = (String) paramMap.get("dataKey");
        String valueKey = (String) paramMap.get("valueKey");
        String collection = (String) paramMap.get("collection");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns);
        criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate))
                .lte(DataFormatUtil.getDateYMDHMS(endDate));
        criteria.and(dataKey+".PollutantCode").is(pollutantcode);
        aggregations.add(match(criteria));
        aggregations.add(unwind(dataKey));
        aggregations.add(match(Criteria.where(dataKey+".PollutantCode").is(pollutantcode)));
        ProjectionOperation projects = project().and("DataGatherCode").as("DataGatherCode")
                .and("MonitorTime").as("MonitorTime")
                .and( dataKey+".PollutantCode").as("PollutantCode")
                .and(dataKey+"."+valueKey).as("value");
        aggregations.add(projects);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public List<Document> getMonUnWindOrAirDataByParam(Map<String, Object> paramMap) {

        //waterlevel

        List<AggregationOperation> aggregations = new ArrayList<>();
        String dgimn = (String) paramMap.get("dgimn");
        String pollutantcode = (String) paramMap.get("pollutantcode");
        String startDate = (String) paramMap.get("starttime");
        String endDate = (String) paramMap.get("endtime");
        String dataKey = (String) paramMap.get("dataKey");
        String valueKey = (String) paramMap.get("valueKey");
        String collection = (String) paramMap.get("collection");
        String dgimnCode = pollutantcode.equals("aqi")?"StationCode":"DataGatherCode";
        Criteria criteria = Criteria.where(dgimnCode).is(dgimn);
        criteria.and("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(startDate))
                .lte(DataFormatUtil.getDateYMDHMS(endDate));
        ProjectionOperation projects;
        if (pollutantcode.equals("aqi")){
            aggregations.add(match(criteria));
            projects = project().and(dgimnCode).as("DataGatherCode")
                    .and("MonitorTime").as("MonitorTime")
                    .and("AQI").as("value");

        }else if (pollutantcode.equals("waterlevel")){
            aggregations.add(match(criteria));
            projects = project().and(dgimnCode).as("DataGatherCode")
                    .and("MonitorTime").as("MonitorTime")
                    .and( "WaterLevel").as("value");

        }else {
            aggregations.add(match(criteria));
            criteria.and(dataKey+".PollutantCode").is(pollutantcode);
            aggregations.add(unwind(dataKey));
            aggregations.add(match(Criteria.where(dataKey+".PollutantCode").is(pollutantcode)));
            projects = project().and(dgimnCode).as("DataGatherCode")
                    .and("MonitorTime").as("MonitorTime")
                    .and( dataKey+".PollutantCode").as("PollutantCode")
                    .and(dataKey+"."+valueKey).as("value");

        }
        aggregations.add(projects);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        return aggregationResults.getMappedResults();

    }

}
