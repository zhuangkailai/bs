package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.GASOutPutInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GasOutPutInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(GASOutPutInfoVO record);

    int insertSelective(GASOutPutInfoVO record);

    GASOutPutInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(GASOutPutInfoVO record);

    int updateByPrimaryKey(GASOutPutInfoVO record);


    /**
     * @author: lip
     * @date: 2018/9/11 0011 下午 2:56
     * @Description: 自定义查询参数，获取总的排口总数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    long countTotalByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutionGasOuputsAndStatusByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/24 0024 下午 1:55
     * @Description: 通过自定义参数获取废气排口列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getGasOutPutListByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 下午 3:53
     * @Description:通过污染源id获取所有废气排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<GASOutPutInfoVO> selectOutputByPollutionid(String pollutionid);

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


    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 2:55
     * @Description: 通过名称和污染源id查询排口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> params);

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
     * @date: 2019/7/10 0010 下午 5:32
     * @Description: 根据自定义参数获取各企业下各排口监测的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAllOutputDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

    /**
     * @param paramMap
     * @author: zhangzc
     * @date: 2019/7/10 14:00
     * @Description: 获取废气排口相关污染物和企业信息(废气排放量突变预警涉及的企业排口污染物信息)
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutionGasOutPutPollutants(Map<String, Object> paramMap);

    List<Map<String, Object>> getGasOutPutPollutants(Map<String, Object> paramMap);


    List<Map<String, Object>> getGasOutputDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

    /**
     * @author: zhangzc
     * @date: 2019/7/26 18:01
     * @Description: 根据污染源id获取废气排口和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getGasOutPutAndPollutantsByID(@Param("pollutionid") String pollutionid, @Param("monitortype") int monitortype);

    /**
     * @author: zhangzc
     * @date: 2019/7/26 18:01
     * @Description: 根据污染源id获取废气排口和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getGasOutPutDgimnsByParamMap(Map<String, Object> paramMap);

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
    <V, K> List<Map<String, Object>> getAllMonitorGasOutPutInfo(Map<String, Object> paramMap);


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
    List<Map<String,Object>> getOutPutAndPollutionInfoByParams(Map<String,Object> paramMap);


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
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据监测点类型和mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    List<Map<String,String>> getMonitorPollutantByParam(Map<String,Object> paramMap);


    int deleteGarbageData();


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 4:59
     * @Description: 获取企业和监测点树
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

    void setTimeDataByParam(Map<String, Object> updateMap);


}