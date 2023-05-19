package com.tjpu.sp.service.environmentalprotection.envquality;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface AirParkHourDataService {

    /**
     *
     * @author: lip
     * @date: 2020/5/18 0018 下午 4:34
     * @Description: 自定义查询条件获取园区空气小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> getAirParkHourDataByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/5/18 0018 下午 4:43
     * @Description: 计算日综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    Map<String, Double> computeDayCompositeIndex(List<Document> hourDataList, List<String> pollutantCodes);
}
