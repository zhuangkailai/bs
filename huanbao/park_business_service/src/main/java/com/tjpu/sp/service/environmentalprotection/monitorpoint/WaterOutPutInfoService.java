package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface WaterOutPutInfoService {


    long countTotalByParam(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/5/22 0022 上午 10:46
     * @Description:获取废水、雨水污染源下排放口及状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: datamark：数据标记1-废水、3-雨水
     * @return:
     */
    List<Map<String, Object>> getPollutionWaterOuputsAndStatus(Map<String, Object> paramMap);


    /**
     * @author: zhangzc
     * @date: 2019/5/27 16:01
     * @Description: 获取在线废水排口信息
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOnlineWaterOutPutInfoByParamMap(Map<String, Object> paramMap);

//    /**
//     * @author: chengzq
//     * @date: 2019/5/23 0023 下午 4:30
//     * @Description: 通过自定义参数获取废水列表
//     * @updateUser:
//     * @updateDate:
//     * @updateDescription:
//     * @param: [jsonObject]
//     * @throws:
//     */
//    List<Map<String, Object>> getWatreOutPutByParamMap(JSONObject jsonObject);

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 5:15
     * @Description: 通过id获取废水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    WaterOutputInfoVO selectByPrimaryKey(String pkId);

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 5:42
     * @Description: 通过实体新增排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    int insertSelective(WaterOutputInfoVO record);


    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 5:42
     * @Description: 通过实体修改排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    int updateByPrimaryKey(WaterOutputInfoVO record);

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 上午 8:43
     * @Description: 通过id删除废水排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkId]
     * @throws:
     */
    int deleteByPrimaryKey(String pkId);


    /**
     * @author: chengzq
     * @date: 2019/6/11 0011 下午 6:04
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAllOutPutInfoByType(Map<String, Object> paramMap);


//    /**
//     * @author: zhangzc
//     * @date: 2019/5/27 16:01
//     * @Description: 获取在线废水排口信息(分页)
//     * @param:
//     * @return:
//     */
//    PageInfo<Map<String, Object>> getOnlineWaterOutPutInfoByParamMapForPaging(Integer pageSize, Integer pageNum, Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/21 0021 下午 3:21
     * @Description: 组装污染源、排口、监测污染物、特征污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> setWaterOutPutAndPollutantDetail(List<Map<String, Object>> detailDataTemp, String id, Integer monitorPointType);


    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:05
     * @Description: 获取所有已监测废水排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorWaterOutPutAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:05
     * @Description: 获取所有已监测雨水排口和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorRainOutPutAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 2:15
     * @Description: 通过名称和污染源id查询排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 4:16
     * @Description: 通过污染源id查询排口名称和主键
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> selectByPollutionid(String pollutionid);

    /**
     * @author: zhangzc
     * @date: 2019/7/26 10:54
     * @Description: 动态条件查询污染源排口污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutionWaterOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/7/29 0029 上午 9:30
     * @Description: 获取
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getWaterOuPutAndStatusByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:11
     * @Description: gis-获取所有废水排口信息及排口在线、离线统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAllMonitorWaterOutPutInfo(Map<String, Object> pollutionMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 11:22
     * @Description: gis-获取所有雨水排口信息及排口在线、离线统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAllMonitorRainOutPutInfo(Map<String, Object> pollutionMap);

    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:27
     * @Description: 删除状态表垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int deleteGarbageData();

    /**
     * @author: xsm
     * @date: 2020/06/17 0017 下午 2:27
     * @Description: 根据自定义参数获取废气废水排口信息及状态（包括停产状态）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getGasAndWaterOutPutAndStatusByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/06/17 0017 下午 2:27
     * @Description: 根据自定义参数获取雨水排口信息及状态（包括排放中状态）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getRainOutPutAndStatusByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getRainAndWaterOutPutPollutantOnlineDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> countWGPointData();

    List<String> getInOrOutPutMnListByParam(Map<String, Object> paramMap);

    List<Document> getDayFlowDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getWSPollutionList();

    List<Map<String,Object>>  getEntWaterSupplyDataByParam(Map<String, Object> paramMap);

    List<Document> getMonthFlowDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>>  getPWOutPutSelectData(Map<String, Object> paramMap);
}
