package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: lip
 * @date: 2019/6/6 0006 上午 9:00
 * @Description: 城市（园区外）空气日数据操作类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("cityDayData")
public class CityDayDataController {
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private final String collection = "CityDayAQIData";

    /**
     * @author: lip
     * @date: 2019/6/3 14:56
     * @Description: 统计单月城市（园区外）每天的空气质量数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "countCityAirAQIDataByMonitorMonth", method = RequestMethod.POST)
    public Object countCityAirAQIDataByMonitorMonth(@RequestJson(value = "monitormonth") String monitormonth) throws Exception {

        Map<String, Object> dataMap = new HashMap<>();
        try {
            Date nowTime = new Date();
            String nowMonth = DataFormatUtil.getDateYM(nowTime);
            Date startTime = DataFormatUtil.parseDate(monitormonth + "-01 00:00:00");
            Date endTime;
            if (nowMonth.equals(monitormonth)) {
                endTime = nowTime;
            } else {
                endTime = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getLastDayOfMonth(monitormonth) + " 23:59:59");
            }
            System.out.println(DataFormatUtil.getDateYMDHMS(endTime));
            Fields fields = fields("PrimaryPollutant", "MonitorTime", "AirQuality", "AirLevel", "RegionCode", "AQI", "_id");
            Aggregation aggregation = newAggregation(
                    project(fields),
                    match(Criteria.where("MonitorTime").gte(startTime).lte(endTime)),
                    sort(Sort.Direction.ASC, "MonitorTime")
            );
            AggregationResults<Document> cityDayAQIData = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = cityDayAQIData.getMappedResults();
            if (documents.size() > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
                List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                paramMap.clear();
                for (Map<String, Object> map : pollutants) {
                    paramMap.put(map.get("code").toString(), map.get("name"));
                }
                List<Map<String, Object>> ymdList = new ArrayList<>();
                for (Document document : documents) {
                    Map<String, Object> ymdMap = new HashMap<>();
                    ymdMap.put("time", DataFormatUtil.getDateYMD(document.getDate("MonitorTime")));
                    ymdMap.put("aqi", document.get("AQI"));
                    ymdMap.put("aq", document.get("AirQuality"));
                    ymdMap.put("keyname", getPrimaryPollutant(paramMap, document.get("PrimaryPollutant")));
                    ymdList.add(ymdMap);
                }
                dataMap.put("monitormonth", monitormonth);
                dataMap.put("airdata", ymdList);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 9:28
     * @Description: 由code转换name
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getPrimaryPollutant(Map<String, Object> paramMap, Object primaryPollutant) {
        String primaryPollutants = "";
        if (primaryPollutant != null) {
            String tempValue = primaryPollutant.toString();
            if (tempValue.indexOf(",") > -1) {
                String[] tempValues = tempValue.split(",");
                for (int i = 0; i < tempValues.length; i++) {
                    primaryPollutants += paramMap.get(tempValues[i]) + ",";
                }
                primaryPollutants = primaryPollutants.substring(0, primaryPollutants.length() - 1);
            } else {
                primaryPollutants = paramMap.get(tempValue) != null ? paramMap.get(tempValue).toString() : "";
            }
        }
        return primaryPollutants;
    }


}
