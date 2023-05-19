package com.tjpu.sp.service.environmentalprotection.envquality;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AirParkDayDataService {
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
    List<Document> countEachAirQualityDaysByTime(Date startDate, Date endDate, String collection);

    /**
     * @author: zhangzc
     * @date: 2019/8/23 11:01
     * @Description: 获取六参数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getSixPollutantDaysAndTB(Date tbDate, Date tbDate1, List<String> pollutants, String collection);


}
