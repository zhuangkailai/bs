package com.tjpu.sp.service.environmentalprotection.online;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface OnlineWaterQualityService {
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
    List<Map<String, Object>> getWaterQualityStationByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/11/19 0019 下午 3:21
     * @Description: 自定义查询条件查询水质评价标准数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getWaterQualityStandardByParam(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/5/14 0014 下午 1:30
     * @Description: 设置实时水质等级
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    void setWaterQaulity(List<Map<String,Object>> datalist);

    List<Document> setWaterQualityDataByParam(Map<String, Object> paramMap);

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
    Map<String, Object>  getOneWaterStationOnePollutantDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getWaterStationPollutantAlarmRankDataByParams(Map<String, Object> paramMap);

    List<Document> getWaterStationContrastDataByParam(Map<String, Object> paramMap);

    Map<String, Object>  getManyWaterStationOnePollutantDataByParams(Map<String, Object> paramMap);

    List<Document> getManyWaterStationPollutantChangeDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllWaterQualityLevelData();

    List<Document> getWaterQualityAssessmentDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutantSetDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPubStandardListByParam(Map<String, Object> paramMap);

    List<Document> getHourMonitorDataByParam(Map<String, Object> paramMap);
}
