package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirMonitorStationVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AirMonitorStationMapper {
    int deleteByPrimaryKey(String pkAirid);

    int insert(AirMonitorStationVO record);

    int insertSelective(AirMonitorStationVO record);

    AirMonitorStationVO selectByPrimaryKey(String pkAirid);

    int updateByPrimaryKeySelective(AirMonitorStationVO record);

    int updateByPrimaryKey(AirMonitorStationVO record);

    /**
     * @author: lip
     * @date: 2018/9/11 0011 下午 4:23
     * @Description: 自定义查询参数查询总的记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
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
     * @date: 2019/6/11 0011 下午1:59
     * @Description: 根据监测点ID获取该监测点下所以监测因子
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    List<Map<String, Object>> getAirStationAllPollutantsByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/12 0012 下午3:55
     * @Description: 根据监测点ID获取该监测点在线监测设备基础信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramMap
     * @return:
     */
    Map<String, Object> getAirStationDeviceStatusByID(Map<String, Object> paramMap);

    List<Map<String, Object>> getfileIdsByID(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 5:23
     * @Description: 获取所有空气点位信息及其状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllMonitorAirStationAndStatusInfo();


    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 7:12
     * @Description: 根据其它类型的监测点的ID和类型编码获取关联的空气监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAirMnByOtherMonitorPointIdAndType(Map<String, Object> paramMap);

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

    /**
     * @author: xsm
     * @date: 2019/7/13 0013 上午 11:37
     * @Description: 根据自定义参数获取空气监测点MN号和污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    List<Map<String, Object>> getAirMonitorStationDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

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
     * @author: zhangzc
     * @date: 2019/7/30 17:02
     * @Description: 条件查询空气监测站信息污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAirStationPollutants(Map<String, Object> paramMap);


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
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据监测点类型和mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    List<Map<String,String>> getMonitorPollutantByParam(@Param("mns") List<String> mns);

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

    List<Map<String,Object>> getALLAirStationInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAirStationInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getPollutantStandardDataListByParam(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getPollutantSetDataListByParam(Map<String, Object> paramMap);

    void setTimeDataByParam(Map<String, Object> updateMap);
}