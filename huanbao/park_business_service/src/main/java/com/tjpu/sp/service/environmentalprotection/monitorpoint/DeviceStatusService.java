package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;

import java.util.List;
import java.util.Map;

public interface DeviceStatusService {

    int insert(DeviceStatusVO record);

    List<DeviceStatusVO> selectByDgimn(String Dgimn);

    int updateByPrimaryKey(DeviceStatusVO record);

    int deleteByPrimaryKey(String pkId);

    List<DeviceStatusVO> getDeviceStatusInfosByDgimn(String mnnum);

    void deleteDeviceStatusByMN(String dgimn);

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
    List<Map<String,Object>> countMonitorPointStatusNumByMonitorPointTypes(Map<String, Object> paramMap,Boolean userAuth);

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
     * @date: 2020/3/03 0003 下午 14:36
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
     * @date: 2020/3/04 0004 上午 10:43
     * @Description:根据监测类型和点位ID获取点位信息(包含经纬度)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getMonitorPointInfoByDgimn(Map<String, Object> paramMap);

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

    /**
     * @author: xsm
     * @date: 2020/4/20 0020 下午 3:12
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllMonitorPointTypeDataForEnvSupervisionHomeMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/5/11 0011 下午 2:54
     * @Description:根据用户数据权限获取环境监管首页的所有监测类型点位的状态（在线，离线，超标，异常，停产）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countMonitorPointStatusNumForEnvSupervisionHomeMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getMonitorPointInfoForEnvSupervisionHomeMap(Map<String, Object> paramMap);

    void updateMonitorDgimn(String befordgimn,String afterdgimn,String monitortype);

    void updateStatusByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countSecurityMonitorPointAllStatusNumByTypes(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllSecurityMonitorPointInfoByParamForHomePage(Map<String, Object> paramMap);

    List<Map<String,Object>> getDeviceStatusDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllHBMonitorPointDataList(Map<String, Object> paramMap);

    List<Map<String,Object>> countMonitorPointDataSendStatusByParam(List<String> mns, Map<String, Map<String, Object>> mnAndPointData);

    List<Map<String,Object>> getAllHBMonitorPointDataListByParam(Map<String, Object> paramMap);

    long getHBMonitorPointInfoNumByParamMap(Map<String, Object> paramMap);

    List<String> getOnLinePoints(String code);

    List<Map<String,Object>> getAllMonitorTypesForManagementHomeMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/01/11 0011 下午 13:21
     * @Description:统计各类型点位在线离线数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countAllPointStatusNumForMonitorType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/14 15:40
     * @Description: 获取挂图作战所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllWallChartOperationMonitorTypes(Map<String,Object> param);

    List<Map<String,Object>> getEnvPointMnDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getEnvPointInfoDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getEnvAirPointInfoDataByParam(Map<String, Object> parammap);

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时浓度排名和环比情况(水质)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    List<Map<String,Object>> getEnvWaterQualityPointInfoDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getAllMonitorPointTypeData();

    List<Map<String, Object>> getMonitorTypeListByParam(Map<String, Object> paramMap);

    List<String> getRealTimeAlarmStatusDgimns();
}
