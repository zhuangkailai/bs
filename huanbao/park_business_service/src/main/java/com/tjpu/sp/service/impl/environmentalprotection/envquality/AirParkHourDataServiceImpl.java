package com.tjpu.sp.service.impl.environmentalprotection.envquality;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.envquality.AirParkHourDataService;
import io.swagger.models.auth.In;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
@Transactional
public class AirParkHourDataServiceImpl implements AirParkHourDataService {

    private final String hourCollection = "CityHourAQIData";
    private final MongoTemplate mongoTemplate;

    public AirParkHourDataServiceImpl(@Qualifier("primaryMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @author: lip
     * @date: 2020/5/18 0018 下午 4:34
     * @Description: 自定义查询条件获取园区空气小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getAirParkHourDataByParam(Map<String, Object> paramMap) {

        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        List<String> pollutantCodes = (List<String>) paramMap.get("pollutantcodes");
        Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "DataList", "AQI", "_id");
        Aggregation aggregation = newAggregation(
                project(fields),
                match(Criteria.where("MonitorTime").gte(startDate).lte(endDate).and("DataList.PollutantCode").in(pollutantCodes)),
                sort(Sort.Direction.DESC, "MonitorTime")
        );
        AggregationResults<Document> hourAQIData = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        return hourAQIData.getMappedResults();
    }

    /**
     * @author: lip
     * @date: 2020/5/18 0018 下午 4:43
     * @Description: 计算日综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Double> computeDayCompositeIndex(List<Document> hourDataList, List<String> pollutantCodes) {
        Map<String, Double> codeAndTotal = new HashMap<>();
        Map<String, Integer> codeAndNum = new HashMap<>();
        Map<String, List<Double>> codeAndStrengths = new HashMap<>();
        List<Document> pollutantData;
        String pollutantCode;
        List<Double> strengths;
        Double Strength;
        for (Document document : hourDataList) {
            pollutantData = document.get("DataList", List.class);
            for (Document pollutant : pollutantData) {
                pollutantCode = pollutant.getString("PollutantCode");
                if (pollutantCodes.contains(pollutantCode)) {
                    Strength = pollutant.get("Strength") != null ? Double.parseDouble(pollutant.get("Strength").toString()) : 0;
                    if (codeAndTotal.containsKey(pollutantCode)) {
                        codeAndTotal.put(pollutantCode, codeAndTotal.get(pollutantCode) + Strength);
                        codeAndNum.put(pollutantCode, codeAndNum.get(pollutantCode) + 1);
                        strengths = codeAndStrengths.get(pollutantCode);


                    } else {
                        codeAndTotal.put(pollutantCode, Strength);
                        codeAndNum.put(pollutantCode, 1);
                        strengths = new ArrayList<>();
                    }
                    strengths.add(Strength);
                    codeAndStrengths.put(pollutantCode, strengths);

                }
            }
        }
        Map<String, Integer> codeAndPercentile = new HashMap<>();
        codeAndPercentile.put("a21005", 95);
        codeAndPercentile.put("a05024", 90);
        Map<String, Double> codeAndStandardValue = new HashMap<>();
        codeAndStandardValue.put("a21026", 60D);
        codeAndStandardValue.put("a21004", 40D);
        codeAndStandardValue.put("a21005", 4D);
        codeAndStandardValue.put("a05024", 160D);
        codeAndStandardValue.put("a34002", 70D);
        codeAndStandardValue.put("a34004", 35D);
        int value;
        Double sumItem;
        Double compositeIndex =0d;

        Map<String, Double> codeAndValue = new HashMap<>();
        for (String code : codeAndStandardValue.keySet()) {
            if (codeAndTotal.containsKey(code)) {
                if (codeAndPercentile.containsKey(code)) {
                    int n = codeAndNum.get(code);
                    int p = codeAndPercentile.get(code);
                    Double k = 1 + (n - 1) * p / 100d;
                    int s;
                    Double mp;
                    strengths = codeAndStrengths.get(code);
                    Collections.sort(strengths);

                    s = (int) Math.floor(k) - 1;
                    mp = strengths.get(s) + (strengths.get(s + 1) - strengths.get(s)) * (k - s);
                    mp = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp));
                    sumItem = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", mp / codeAndStandardValue.get(code)));
                } else {
                    value = (int) Math.round(codeAndTotal.get(code) / codeAndNum.get(code));
                    sumItem = Double.parseDouble(DataFormatUtil.formatDouble("######0.000", (double) value / codeAndStandardValue.get(code)));
                }
            } else {
                sumItem = 0d;
            }
            compositeIndex+=sumItem;
            codeAndValue.put(code, sumItem);
        }
        codeAndValue.put("total",compositeIndex);
        return codeAndValue;
    }
}
