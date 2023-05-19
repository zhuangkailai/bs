package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WaterStationMapper {
    int deleteByPrimaryKey(String pkWaterstationid);

    int insert(WaterStationVO record);

    int insertSelective(WaterStationVO record);

    WaterStationVO selectByPrimaryKey(String pkWaterstationid);

    int updateByPrimaryKeySelective(WaterStationVO record);

    int updateByPrimaryKey(WaterStationVO record);

    /**
     * @author: chengzq
     * @date: 2019/9/18 0018 下午 4:43
     * @Description: 动态条件获取水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getOnlineWaterStationInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/9/19 0019 上午 8:49
     * @Description: 根据监测点类型和自定义参数获取水质监测点某类型MN号和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getWaterStationDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/25 0025 下午 16:58
     * @Description: 根据自定义参数获取想要的水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAllWaterStationInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 上午 11:51
     * @Description: 获取所有水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getWaterStationByParamMap(Map<String, Object> paramMap);
    /**
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    List<Map<String, String>> getMonitorPollutantByParam(@Param("mns") List<String> mns);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 13:27
    *@Description: 根据监测点名称和MN号获取新增的那条水质站点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [params]
    *@throws:
    **/
    Map<String,Object> selectWaterStationInfoByPointNameAndDgimn(Map<String, Object> params);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 14:17
    *@Description: 根据监测点ID获取该监测点在线监测设备基础信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [parammap]
    *@throws:
    **/
    List<Map<String,Object>> getWaterStationDeviceStatusByID(Map<String, Object> parammap);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 14:45
    *@Description: 根据监测点ID获取附件表对应关系
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [parammap]
    *@throws:
    **/
    List<Map<String,Object>> getfileIdsByID(Map<String, Object> parammap);

    /**
     *@author: xsm
     *@date: 2019/11/14 0014 14:56
     *@Description: 获取所有水质监测点信息和状态
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    List<Map<String,Object>> getAllWaterStationAndStatusInfo();
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

    List<Map<String,Object>> getWaterStationPollutants(Map<String, Object> paramMap);

    /**
     *
     * @author: xsm
     * @date: 2020/01/19 0019 下午 4:11
     * @Description: 获取所有水质级别
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllWaterQualityLevel();

    List<Map<String,Object>> getPollutantStandardDataListByParam(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getPollutantSetDataListByParam(Map<String, Object> paramMap);

    void setTimeDataByParam(Map<String, Object> updateMap);

    List<Map<String,Object>> getAllWaterQualityLevelData();

    long countTotalByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetDataListById(Map<String, Object> paramMap);

    List<Map<String, Object>> getPubStandardListByParam(Map<String, Object> paramMap);
}