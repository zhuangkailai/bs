package com.tjpu.sp.service.environmentalprotection.report;


import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface EntDataReportService {

    /**
     * @author: xsm
     * @date: 2019/7/31 0031 上午 8:42
     * @Description:根据报表类型和自定义参数获取某个企业的企业报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String,Object> getEntDataReportByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/01 0001 上午 11:34
     * @Description:根据报表类型和自定义参数获取企业汇总报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String,Object> getSummaryEntDataReportByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/01 0001 下午 2:4
     * @Description:根据类型获取关联该类型在线排口的企业信息（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String,Object>> getSelectPollutionInfoByPointType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/1 0001 下午 3:23
     * @Description:根据类型获取该类型的监测污染物（下拉框）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String,Object>> getSelectPollutantInfoByPointtype(Map<String, Object> paramMap);

    Map<String,Object> getStinkSummaryEntDataReportByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllStenchPointInfoByDataAuthor(Map<String, Object> map);

    List<Map<String,Object>> getSelectEntOrPointDataByParam(Map<String, Object> paramMap);

    Map<String,Object> getEntSummaryReportDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEntReportTimeSlotDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getGasSelectPollutionInfoByPointtype(Map<String, Object> paramMap);

    Map<String,Object> getGasSummaryEntDataReportByParamMap(Map<String, Object> paramMap)throws ParseException;

    Map<String,Object> getGasEntSummaryReportDataByParamMap(Map<String, Object> paramMap);
}
