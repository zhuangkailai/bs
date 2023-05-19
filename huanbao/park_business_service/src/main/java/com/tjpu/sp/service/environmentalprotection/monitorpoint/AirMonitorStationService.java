package com.tjpu.sp.service.environmentalprotection.monitorpoint;

import com.github.pagehelper.PageInfo;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirMonitorStationVO;
import net.sf.json.JSONArray;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface AirMonitorStationService {
    long countTotalByParam(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/5/28 16:20
     * @Description: 获取在线空气监测点信息
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOnlineAirStationInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/6/4 0004 下午 5:48
     * @Description: 获取所有空气站信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllAirMonitorStation(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/5 0005 下午 6:33
     * @Description: 获取单月多站点的综合指数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: month:月份日期（yyyy-mm）
     * @return: key：stationCode，value:综合指数值的map
     */
    Map<String, Map<String, Object>> getOneMonthManyStationMonthCompositeIndex(String month) throws Exception;

    /**
     * @author: xsm
     * @date: 2019/6/6 0006 下午 5:54
     * @Description: 获取单月多站点的首要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: month:月份日期（yyyy-mm）
     * @return: key：stationCode，value:首要污染物
     */
    Map<String, String> getOneMonthManyStationMonthPrimarypollutant(String monitortime);

    HSSFWorkbook getCityAirCompositeByMonitortime(JSONArray jsonArray);

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 上午 11:54
     * @Description: 根据监测点名称和监测点类型获取空气站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    List<Map<String, Object>> getAirStationInfosByMonitorPointNameAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/11 0011 下午1:54
     * @Description: 根据监测点ID获取该监测点下所以监测因子
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    List<Map<String, Object>> getAirStationAllPollutantsByIDAndType(Map<String, Object> paramMap);

    List<Map<String, Object>> getAirStationHourOrDayDataByParams(List<String> pkidlist, String pollutantcode, String starttime, String endtime, String timetype);

    /**
     * @author: xsm
     * @date: 2019/6/12 0012 下午1:54
     * @Description: 根据监测点ID获取该监测点在线监测设备基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    Map<String, Object> getAirStationDeviceStatusByID(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/13 0013 下午 5:27
     * @Description: 获取空气站点分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    PageInfo<Map<String, Object>> getOnlineAirStationInfoByParamMapForPage(Integer pageSize, Integer pageNum, Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/16 0016 下午 5:27
     * @Description: 根据监测点ID获取附件表对应关系
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<String> getfileIdsByID(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 5:22
     * @Description: 获取所有空气点位信息及其状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorAirStationAndStatusInfo();

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 4:19
     * @Description: 自定义查询条件获取mongodb空气站点小时/日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getAirStationMongodbDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 7:09
     * @Description: 根据其它类型的监测点的ID和类型编码获取关联的空气监测点的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    String getAirMnByOtherMonitorPointIdAndType(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/2 0002 下午 4:07
     * @Description: 获取时间段内多站点的首要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    List<Map<String, Object>> getManyStationMonthCompositeIndexByMonitorTimes(String starttime, String endtime) throws Exception;

    /**
     * @author: xsm
     * @date: 2019/7/8 0008 上午 11:37
     * @Description: 根据监测点名称和MN号获取某点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    Map<String, Object> selectAirStationInfoByPointNameAndDgimn(Map<String, Object> params);

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/30 17:02
//     * @Description: 条件查询空气监测站信息污染物信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    List<Map<String, Object>> getAirStationPollutants(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 16:13
     * @Description: gis-获取所有空气监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAllAirMonitorStationInfo();


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 8:01
     * @Description: 通过自定义参数获取所有空气站
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAllAirMonitorStationByParams(Map<String, Object> paramMap);


    List<Map<String, Object>> countOnlineOutPut(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/11/04 0004 下午 6:36
     * @Description: 通过主键ID获取空气站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    AirMonitorStationVO getAirMonitorStationByID(String pkid);

    /**
     *
     * @author: lip
     * @date: 2020/3/6 0006 上午 11:07
     * @Description: 自定义查询参数获取站点监测标准值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    Map<String,Object> getMonitorStandardByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/4/2 0002 上午 11:47
     * @Description: 自定义查询条件获取监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Document> getMonitorDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/5/14 0014 下午 2:33
     * @Description: 设置空气点位aqi（最新空气站小时数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datalist]
     * @throws:
     */
    void setAirStationAqi(List<Map<String, Object>> datalist);

    /**
     * @author: xsm
     * @date: 2019/7/25 0025 上午 8:30
     * @Description: 根据恶臭MN号获取对应空气监测点MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAirStationsByMN(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测时间段获取空气点位AQI报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    Map<String, Object> getAirStationReportDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测时间段获取空气点位AQI报表表头
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getAirStationReportTitleDataByType(String[] titlenames, String[] titlefiled);

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测日期获取空气点位综合评价报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAirStationOverallMeritDataByParams(Map<String, Object> paramMap) throws Exception;

    List<Map<String,Object>> getAirStationOverallMeritDataTitle();

    List<Document> getStationHourDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllAirStationDataByParamMap(Map<String, Object> paramMap) throws Exception;

    List<Map<String,Object>> getManyMonthManyStationMonthCompositeIndex(Map<String, Object> paramMap) throws Exception;

    List<Map<String,Object>> getAllAirCityDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAirStationPollutantCumulativeDataByParamMap(Map<String, Object> paramMap)throws Exception;

    List<Map<String,Object>> getAirPollutantCumulativeDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAirStationYearOnYeraAnalysisDataByParamMap(Map<String, Object> paramMap)throws Exception;

    List<Map<String,Object>> getAirCityYearOnYeraAnalysisDataByParamMap(Map<String, Object> paramMap)throws Exception;

    List<Map<String, Object>> getAirStationYearOnYeraCumulativeDataByParamMap(Map<String, Object> paramMap)throws Exception;

    List<Map<String, Object>> getCityYearOnYeraCumulativeDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOneStationMonthYearOnYeraCompositeIndex(Map<String, Object> paramMap)throws Exception;

    List<Map<String, Object>> getStationTitleListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getStationReportDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getStationRantDataByParams(Map<String, Object> paramMap);

    List<Map<String, Object>> getStationDistributeDataByParams(Map<String, Object> paramMap);

    List<Map<String, Object>> getStationSixTBDataByParams(Map<String, Object> paramMap) throws Exception;

    List<Map<String, Object>> getAirStationInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getStationOrCityRankDataListByParam(Map<String, Object> paramMap) throws Exception;
}
