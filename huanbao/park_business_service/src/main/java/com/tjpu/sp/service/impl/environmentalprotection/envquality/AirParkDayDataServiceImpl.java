package com.tjpu.sp.service.impl.environmentalprotection.envquality;

import com.tjpu.sp.service.environmentalprotection.envquality.AirParkDayDataService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class AirParkDayDataServiceImpl implements AirParkDayDataService {
    private final MongoTemplate mongoTemplate;

    public AirParkDayDataServiceImpl(@Qualifier("primaryMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/23 11:19
     * @Description: AirQuality 空气质量分组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countEachAirQualityDaysByTime(Date startDate, Date endDate, String collection) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("MonitorTime").gte(startDate).lte(endDate);
        ProjectionOperation projects = project(fields("AirQuality"));
        aggregations.add(match(criteria));
        aggregations.add(projects);
        aggregations.add(group("AirQuality").count().as("num"));
        ProjectionOperation projects2 = project(fields().and("AirQuality", "_id").and("num")).andExclude("_id");
        aggregations.add(projects2);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/23 13:19
     * @Description: 根据首要污染物分组统计各个污染物天数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getSixPollutantDaysAndTB(Date startDate, Date endDate, List<String> pollutants, String collection) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("PrimaryPollutant").in(pollutants);
        ProjectionOperation projects = project(fields("PrimaryPollutant"));
        aggregations.add(match(criteria));
        aggregations.add(projects);
        aggregations.add(group("PrimaryPollutant").count().as("num"));
        ProjectionOperation projects2 = project(fields().and("PollutantCode", "_id").and("num")).andExclude("_id");
        aggregations.add(projects2);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        return aggregationResults.getMappedResults();
    }


}
