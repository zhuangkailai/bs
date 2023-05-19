package com.tjpu.sp.service.environmentalprotection.online;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface OnlineOtherPointService {


    /**
     * @author: xsm
     * @date: 2022/04/20 0020 下午 16:26
     * @Description: 获取其它监测点监测的污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getPointMonitorPollutantDataParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/20 0020 下午 16:06
     * @Description: 获取某点位某时间段内各污染物小时浓度趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtype, starttime, endtime, pollutantcode]
     * @throws:
     */
    Map<String,Object> getOtherMonitorPointHourOnlineDataByParams(Map<String, Object> paramMap);

    List<Document> getMongoDBListByParam(Map<String, Object> paramMap);

    Map<String, Object> getOtherOnlineGroupDataByParams(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantDataParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getPointPollutantChangeRateDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getOnePointMonitorPollutantSelectDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getPointPollutantContrastValueDataByParam(Map<String, Object> paramMap);
}
