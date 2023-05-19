package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.online.OnlineVoyageMonitorService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineVoyageMonitorServiceImpl implements OnlineVoyageMonitorService {

    private final MongoTemplate mongoTemplate;

    public OnlineVoyageMonitorServiceImpl(@Qualifier("primaryMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/16 14:27
     * @Description: 条件查询走航监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getVoyageMonitorDataByParam(String pollutantcode, Date starttime, Date endtime) {
        final String collection = "VoyageMonitorData";
        final String unwindFiled = "RealDataList";
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("MonitorTime").gte(starttime).lte(endtime).and(unwindFiled + "." + "PollutantCode").is(pollutantcode);
        MatchOperation match_1 = match(criteria);
        UnwindOperation unwind_2 = unwind(unwindFiled);
        MatchOperation match_3 = match(Criteria.where(unwindFiled + "." + "PollutantCode").is(pollutantcode));
        SortOperation sort_4 = sort(Sort.Direction.ASC, "MonitorTime");
//        fields("DataGatherCode","MonitorTime","Longitude","Latitude").and("PollutantCode",unwindFiled + "." + "PollutantCode");
        aggregations.add(match_1);
        aggregations.add(unwind_2);
        aggregations.add(match_3);
        aggregations.add(sort_4);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> mappedResults = aggregationResults.getMappedResults();
        for (Document document : mappedResults) {
            Date monitorTime = document.getDate("MonitorTime");
            String time = DataFormatUtil.getDateYMDHMS(monitorTime);
            document.replace("MonitorTime", time);
            document.remove("_id");
        }
        return mappedResults;
    }
}
