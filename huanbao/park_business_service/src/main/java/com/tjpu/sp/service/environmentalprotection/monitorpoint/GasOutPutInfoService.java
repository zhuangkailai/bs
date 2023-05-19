package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.GASOutPutInfoVO;

import java.util.List;
import java.util.Map;

public interface GasOutPutInfoService {
    GASOutPutInfoVO selectByPrimaryKey(String pkId);

    int deleteByPrimaryKey(String pkId);

    int insertSelective(GASOutPutInfoVO record);


    long countTotalByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutionGasOuputsAndStatus(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 下午 1:55
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
//    List<Map<String, Object>> getGasOutPutListByParamMap(Map<String, Object> paramMap);


    /**
     * @author: lip
     * @date: 2019/5/27 0027 下午 8:04
     * @Description: 获取在线废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOnlineGasOutPutInfoByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 上午 9:09
     * @Description: 获取所有dgimn
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllOutPutInfo(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 上午 11:34
     * @Description: 查询所有废水废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllWaterOutputAndGasOutputInfo();

//    PageInfo<Map<String, Object>> getOnlineGasOutPutInfoByParamMapForPage(Integer pageSize, Integer pageNum, Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 2:29
     * @Description: 组装污染源、排口、监测污染物、特征污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> setGasOutPutAndPollutantDetail(List<Map<String, Object>> detailDataTemp, String id);

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:21
     * @Description: 获取所有已监测废气排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorGasOutPutAndStatusInfo();

//    /**
//     * @author: chengzq
//     * @date: 2019/6/25 0025 下午 2:55
//     * @Description: 通过名称和污染源id查询排口
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [params]
//     * @throws:
//     */
//    Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> params);


    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 3:59
     * @Description: 通过污染源id获取排口名称和id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> selectByPollutionid(String pollutionid);

    /**
     * @author: xsm
     * @date: 2019/7/9 0009 下午 5:03
     * @Description: 获取废气排放量许可预警列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [allflowvalues，allMN，year]
     * @throws:
     */
    List<Map<String, Object>> getFlowPermitListData(List<Map<String, Object>> allflowvalues, List<Map<String, Object>> allMN, String year);

    /**
     * @author: xsm
     * @date: 2019/7/10 0010 上午 11:32
     * @Description: 获取废气排放量许可预警列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getTableTitleForGasPermittedFlowEarly();


    /**
     * @author: xsm
     * @date: 2019/7/11 0011 下午 1:10
     * @Description: 获取废气浓度阈值预警列表表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getTableTitleForGasConcentrationThresholdEarly(Integer monitorpointtype);

//    /**
//     * @param paramMap
//     * @author: zhangzc
//     * @date: 2019/7/10 14:00
//     * @Description: 获取废气排口相关污染物和企业信息(废气排放量突变预警涉及的企业排口污染物信息)
//     * @param:
//     * @return:
//     */
//    List<Map<String, Object>> getPollutionGasOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/29 0029 上午 9:44
     * @Description: 自定义查询条件获取排口及状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getGasOutPutAndStatusByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:25
     * @Description: gis-获取所有废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAllMonitorGasOutPutInfo(Map<String, Object> pollutionMap);


    /**
     * @author: chengzq
     * @date: 2019/8/20 0020 下午 3:21
     * @Description: 通过自定义参数查询排口和污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getOutPutAndPollutionInfoByParams(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 2:57
     * @Description: 通过味道code和mn号集合查询包含这种味道的企业和排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> selectGasInfoBySmellCodeAndMns(Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:22
     * @Description: 删除垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int deleteGarbageData();


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 4:58
     * @Description: 获取企业和检测点树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,String>> getMonitorPointAndPollutionTree(Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/2/14 0014 上午 11:11
     * @Description: 获取企业和监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,String>> getMonitorPointAndPollutionInfo(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/17 0017 下午 14:53
     * @Description: 获取所有烟气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllMonitorSmokeOutPutAndStatusInfo();

    /**
     * @author: xsm
     * @date: 2021/12/20 0020 上午 09:57
     * @Description: 根据监测点ID删除该点位设置的报警污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteGasOutPutPollutantByID(String id);
}
