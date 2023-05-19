package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.sp.service.environmentalprotection.online.OnlineFlowDataService;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineFlowDataServiceImpl implements OnlineFlowDataService {

    private final MongoTemplate mongoTemplate;


    public OnlineFlowDataServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    /**
     * @author: zhangzc
     * @date: 2019/8/28 13:22
     * @Description: 获取小时和天的排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getHourAndDayFlowDataByParam(List<String> mns, List<String> pollutantCodes, Date startTime, Date endTime, String unwindFieldName, String collection) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria_1 = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime).and(unwindFieldName + "." + "PollutantCode").in(pollutantCodes);
        UnwindOperation unwind_2 = unwind(unwindFieldName);
        Criteria criteria_3 = Criteria.where(unwindFieldName + "." + "PollutantCode").in(pollutantCodes);
        Fields fields = fields("DataGatherCode", "MonitorTime").and("PollutantCode", unwindFieldName + "." + "PollutantCode").and("FlowValue", unwindFieldName + "." + "CorrectedFlow");
        ProjectionOperation project_4 = project(fields);
        SortOperation sortOperation_5 = sort(Sort.Direction.ASC, "MonitorTime");
        aggregations.add(match(criteria_1));
        aggregations.add(unwind_2);
        aggregations.add(match(criteria_3));
        aggregations.add(project_4);
        aggregations.add(sortOperation_5);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/2 10:38
     * @Description: 获取浓度或者排放量数据 (待思考大数据量时分页慢的问题)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getNDOrPFLDataByParam(String valueFiledName, List<String> mns, List<String> pollutantCodes, Date startTime, Date endTime, String unwindFieldName, String collection, Integer pageSize, Integer pageNum) {
        Criteria criteria_1 = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime).and(unwindFieldName + "." + "PollutantCode").in(pollutantCodes);
        BooleanOperators.Or or = BooleanOperators.Or.or();
        for (String code : pollutantCodes) {
            ComparisonOperators.Eq pollutantCode = ComparisonOperators.Eq.valueOf("$$item.PollutantCode").equalToValue(code);
            or = or.orExpression(pollutantCode);
        }
        ProjectionOperation projectionOperation_2 = project("DataGatherCode", "MonitorTime").and(unwindFieldName).filter("item", or).as(unwindFieldName);
        SortOperation sortOperation_3 = sort(Sort.Direction.DESC, "MonitorTime");
        List<AggregationOperation> aggregations = new ArrayList<>();
        aggregations.add(match(criteria_1));
        aggregations.add(projectionOperation_2);
        aggregations.add(sortOperation_3);
        if (pageSize > 0 && pageNum > 0) {
            Integer skipNum = pageSize * (pageNum - 1);
            SkipOperation skipOperation_5 = Aggregation.skip(Long.parseLong(skipNum.toString()));
            LimitOperation limitOperation_4 = limit(pageSize);
            aggregations.add(skipOperation_5);
            aggregations.add(limitOperation_4);
        }
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        return aggregationResults.getMappedResults();
    }


    @Override
    public int countNDOrPFLDataByParam(List<String> mns, List<String> pollutantCodes, Date startTime, Date endTime, String unwindFieldName, String collection) {
        Criteria criteria_1 = Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(startTime).lte(endTime).and(unwindFieldName + "." + "PollutantCode").in(pollutantCodes);
        List<AggregationOperation> aggregations = new ArrayList<>();
        aggregations.add(match(criteria_1));
        CountOperation countOperation = count().as("count");
        aggregations.add(countOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        //查询出总数
        return aggregationResults.getUniqueMappedResult() != null ? aggregationResults.getUniqueMappedResult().getInteger("count") : 0;
    }


}
