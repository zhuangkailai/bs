package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.github.pagehelper.PageInfo;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import net.sf.json.JSONObject;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OtherMonitorPointService {



    /**
     * @author: zhangzc
     * @date: 2019/5/29 15:30
     * @Description: 动态条件获取其他监测点信息
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOnlineOtherPointInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> isTableDataHaveInfoByParamMap(Map<String, Object> paramMap);


    List<Map<String, Object>> getOtherMonitorPointInfoAndStateByparamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getOtherMonitorPointAllPollutantsByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/12 0012 下午 4:54
     * @Description: 通过id获取其它监测点的监测设备状态基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    Map<String, Object> getOtherMonitorPointDeviceStatusByID(Map<String, Object> paramMap);

//    /**
//     * @author: lip
//     * @date: 2019/6/13 0013 下午 6:02
//     * @Description: 获取在线其他监测点分页数据
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    PageInfo<Map<String, Object>> getOnlineOtherMonitorPointInfoByParamMapForPage(Integer pageSize, Integer pageNum, Map<String, Object> paramMap);

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
     * @date: 2019/6/21 0021 下午 5:33
     * @Description: 获取所有VOC点位信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorEnvironmentalVocAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2019/6/21 0021 下午 5:40
     * @Description: 获取所有恶臭点位信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorEnvironmentalStinkAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2019/6/26 0026 下午 6:16
     * @Description: 根据监测点ID和监测点类型获取该监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOtherMonitorPointInfoByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 上午 10:57
     * @Description: 查询所有恶臭及厂界恶臭监测点信息包含状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getStenchMonitorPointInfo(Map<String, Object> paramMap);

    List<Map<String, Object>> getMicroStationInfo(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/1 0001 下午 2:18
     * @Description: 通过监测点集合查询恶臭及厂界恶臭污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getStenchPollutantMonitorPointids(Map<String, Object> paramMap);

    List<Map<String, Object>> getMicroStationPollutantMonitorPointids(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/7/5 0005 上午 10:20
     * @Description: 通过监测点Dgimn号查询空气Dgimn号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn]
     * @throws:
     */
    List<String> getAirDgimnByMonitorDgimn(String dgimn);

    /**
     * @author: xsm
     * @date: 2019/7/8 0008 下午 2:26
     * @Description: 根据监测点名称和监测点类型以及MN号获取其它监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    Map<String, Object> selectOtherMonitorPointInfoByParams(Map<String, Object> params);

//    /**
//     * @author: zhangzc
//     * @date: 2019/7/30 15:37
//     * @Description: 条件查询其他监测点企业、排口、污染物信息
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param:
//     * @return:
//     */
//    List<Map<String, Object>> getOtherMonitorPollutionOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 16:25
     * @Description: gis-根据类型获取所有恶臭或VOC站点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAllOtherMonitorPointInfoByType(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 7:46
     * @Description: 通过自定义参数获取监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorInfoByParams(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/9/4 15:24
     * @Description: 获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getStinkMonitorPoint(int code);


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:58
     * @Description: 通过味道code和mn集合获取恶臭监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    List<Map<String, Object>> selectStinkInfoBySmellcodeAndMns(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2019/11/02 0002 上午 10:45
     * @Description: 获取所有传输通道点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getTransportChannelMonitorPointInfos();

    OtherMonitorPointVO getOtherMonitorPointByID(String pkid);

    /**
     * @author: xsm
     * @date: 2019/11/23 0023 上午 11:51
     * @Description: 根据恶臭MN号获取关联的空气Mn号（无关联则取自身，包括厂界恶臭和环境恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<String> getAirDgimnByStinkMonitorDgimn(String dgimn);

    /**
     * @author: xsm
     * @date: 2020/04/09 0009 上午 10:47
     * @Description: 获取所有气象点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllMonitorEnvironmentalMeteoAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2020/6/10 0010 下午 6:02
     * @Description: 获取气象站点分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    PageInfo<Map<String,Object>> getOnlineMeteoInfoByParamMapForPage(Integer pagesize, Integer pagenum, HashMap<Object, Object> objectObjectHashMap);

    /**
     * @author: xsm
     * @date: 2020/6/11 0011 下午 2:11
     * @Description: 获取所有微站点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllMonitorMicroStationAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 3:41
     * @Description: 根据监测时间和恶臭点位MN号及污染物获取恶臭污染物超标数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getStenchOverDataAndWeatherDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和MN号获取该时间段内超标污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime]
     * @throws:
     */
    List<Map<String,Object>> getStenchOverPollutantByParamMap(String dgimn, String starttime, String endtime);

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和恶臭点位MN号及污染物获取恶臭污染物超标列表数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime，pollutantcode]
     * @throws:
     */
    Map<String, Object> getStenchOverAndWeatherListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/09/18 0018 下午 2:58
     * @Description: 根据监测时间和恶臭点位MN号及污染物统计风向下污染物监测值范围次数(多个图表)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [dgimn, starttime, endtime，pollutantcode]
     * @throws:
     */
    List<Map<String,Object>> countManyWindOverPollutantValueData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 根据监测点id 监测类型 (厂界、通道、敏感点) 监测时间获取监测点污染物日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getOneStenchDayMonitorDataByParamsForApp(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 返回所有恶臭点位其中最近一条监测点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getStenchLastOneMonitorTimeByParamsForApp(List<String> mns,String datamark);

    /**
     * @author: xsm
     * @date: 2020/11/3 0003 上午 09:15
     * @Description: 根据时间标记、监测时间 监测类型 (厂界、通道、敏感点) 获取恶臭监测点该时间的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getStenchMonitorListDataByParamsForApp(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkConcentrationDetailDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkEarlyOrOverAlarmDetailDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getStinkExceptionAlarmDetailDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getVocPollutantFactorGroupData(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllVocPollutantByParam(Map<String, Object> param);

    List<Map<String,Object>> getVocPollutantConcentrationDataByParam(Map<String, Object> param);

    Map<String, Object> countVocFactorGroupProportionDataByParam(Map<String, Object> param);

    List<Map<String, Object>> countVocPollutantOFPRankDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getStinkPointDataByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getVocPollutantHourConcentrationDataByParam(Map<String, Object> param);

    Map<String,Object> countVocPointFactorGroupHourDataByParam(Map<String, Object> param);

    List<Map<String,Object>> counVocPollutantSumDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getStinkAndGasOutPutPollutantDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getMonitorPointDataByTypeForVocAnalysis(Integer monitorpointtype, Map<String, Object> paramMap);

    List<Map<String,Object>> getVocPollutantDataByFactorGroups(Map<String, Object> paramMap);

    List<Map<String,Object>> getTraceSourceMeteoMonitorPointMN(Map<String, Object> paramMap);

    List<Document> getStinkHourOrDayDataByParam(List<String> dgimns, String monitortime, String dateType);

    List<Map<String, Object>> getAllStinkPointDataList();

    List<Map<String,Object>> getAllOnlineOtherPointInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllMonitorPointAndStatusInfo(Map<String, Object> param);

    List<Map<String,Object>> getOtherPointInfoAndAirMNByParamMap(Map<String, Object> paramMap);

    long countTotalByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countVocPollutantVocsAvgValueRankDataByParam(Map<String, Object> param);

    List<Map<String,Object>> countVocPollutantLohRankDataByParam(Map<String, Object> param);

    List<Map<String, Object>> getVocRelationDgimnByParam(Map<String, Object> paramMap);

    Map<String, Object> getDataListMapByParam(JSONObject jsonObject);

    void updateInfo(OtherMonitorPointVO otherMonitorPointVO);

    void insertInfo(OtherMonitorPointVO otherMonitorPointVO);

    void deleteById(String id);

    Map<String, Object> getEditOrViewDataById(String id);
}
