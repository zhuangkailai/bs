package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DeviceStatusMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(DeviceStatusVO record);

    int insertSelective(DeviceStatusVO record);

    DeviceStatusVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(DeviceStatusVO record);

    int updateByPrimaryKey(DeviceStatusVO record);

    /**
     * @author: chengzq
     * @date: 2019/6/12 0012 下午 1:25
     * @Description: 通过mn号查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [Dgimn]
     * @throws:
     */
    List<DeviceStatusVO> selectByDgimn(String Dgimn);

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/2 14:31
     * @Description: mn号获取
     * @param:
     * @return:
     * @throws:
     */
    List<DeviceStatusVO> getMnsByMonitorPointTypes(@Param("monitorPointTypes") Set<String> monitorPointTypes);

    void deleteDeviceStatusByMN(String dgimn);
    void deleteDeviceStatusByMNs(List<String> dgimns);
    void deleteDeviceStatusByParamMap(Map<String,Object> paramMap);

    void deleteDeviceStatusByStorageTankAreaID(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/17 0017 上午 10:35
     * @Description:根据监测类型统计各类型监测点的点位状态情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    List<Map<String,Object>> countMonitorPointStatusNumByMonitorPointTypes(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2020/2/17 0017 下午 16:36
     * @Description:获取在线空气监测点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getNormalStatusAirMonitorPoints(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 9:45
     * @Description:统计按点位状态分组的各类型点位数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    List<Map<String,Object>> countMonitorPointNumGroupByStatusByTypes(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/04 0004 上午 9:03
     * @Description:根据监测类型和点位状态获取点位信息(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes]
     * @throws:
     */
    List<Map<String,Object>> getMonitorPointInfoByMonitorTypeAndPointStatus(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/31 0031 上午 10:39
     * @Description:根据监测类型获取相关监测类型信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getALLMonitorPointTypeInfoByTypes(Map<String, Object> paramMap);

    List<Map<String,Object>> countMeteoMonitorPointStatusNumByMonitorPointTypes(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 1:22
     * @Description:根据用户数据权限获取在线监控首页的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllMonitorPointTypeDataForOnlineMonitorHomeMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllMonitorPointTypeDataForEnvSupervisionHomeMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countAllMonitorPointTypeDataOnlineNum(Map<String, Object> param);

    List<Map<String,Object>> getMonitorPointInfoForEnvSupervisionHomeMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countMonitorPointStatusNumForEnvSupervisionHomeMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/5/18 0018 下午 1:23
     * @Description:根据用户数据权限获取传输通道点或敏感点的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getMonitorPointCategoryTypeByParamMap(Map<String, Object> paramMap);

    void updateStatusByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countAllMonitorTypePointOnlineStatusNum(Map<String, Object> param);

    List<Map<String,Object>> countSecurityMonitorPointAllStatusNumByTypes(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllSecurityMonitorPointInfoByParamForHomePage(Map<String, Object> paramMap);

    List<Map<String,Object>> getDeviceStatusDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllHBMonitorPointDataList(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllHBMonitorPointDataListByParam(Map<String, Object> paramMap);

    long getHBMonitorPointInfoNumByParamMap(Map<String, Object> paramMap);

    List<String> getOnLinePoints(@Param("status") String code);

    List<Map<String,Object>> getAllMonitorTypesForManagementHomeMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countAllPointStatusNumForMonitorType(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllWallChartOperationMonitorTypes(Map<String,Object> param);

    List<Map<String,Object>> getEnvPointMnDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getEnvPointInfoDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getEnvAirPointInfoDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getEnvWaterQualityPointInfoDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getAllMonitorPointTypeData();

    List<Map<String, Object>> getMonitorTypeListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getRealTimeAlarmStatusDgimns();

    int updateByMnKey(DeviceStatusVO deviceStatusVO);


    DeviceStatusVO selectByMnKey(String mn);
}