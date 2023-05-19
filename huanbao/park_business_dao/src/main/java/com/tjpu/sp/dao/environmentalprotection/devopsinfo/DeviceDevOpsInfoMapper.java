package com.tjpu.sp.dao.environmentalprotection.devopsinfo;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.DeviceDevOpsInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface DeviceDevOpsInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(DeviceDevOpsInfoVO record);

    int insertSelective(DeviceDevOpsInfoVO record);

    DeviceDevOpsInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(DeviceDevOpsInfoVO record);

    int updateByPrimaryKey(DeviceDevOpsInfoVO record);

    /**
     * @author: xsm
     * @date: 2019/12/04 0004 下午 3:01
     * @Description: 根据自定义参数获取设备运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getDeviceDevOpsInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:30
     * @Description: 根据主键ID获取运维设备详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getDeviceDevOpsInfoDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:21
     * @Description: 根据自定义参数获取运维设备信息总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Long getAllDeviceDevOpsInfoCountByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:33
     * @Description: 修改污染物因子监测状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updatePollutantStatusByParam(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:33
     * @Description: 根据自定义参数获取运维设备历史信息总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Long getAllDeviceDevOpsHistoryInfoCountByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 3:52
     * @Description: 根据自定义参数获取运维设备历史信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getDeviceDevOpsHistoryInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 4:41
     * @Description: 根据自定义参数修改监测点状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updateMonitorPointStatusByParam(Map<String, Object> param);

    List<Map<String,Object>> getIsDevOpsDeviceByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/9 0009 下午 16:04
     * @Description: 根据自定义参数获取相关运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDeviceDevOpsHistoryListDataByParamMap(Map<String, Object> parammap);

    /**
     *
     * @author: xsm
     * @date: 2020/1/10 0010 下午 13:32
     * @Description: 根据自定义参数获取相关运维信息总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Long CountDeviceDevOpsHistoryListDataNumByParams(Map<String, Object> parammap);


    /**
     * @author: chengzq
     * @date: 2020/3/31 0031 下午 3:00
     * @Description: 通过自定义参数获取最新的排口运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    List<Map<String,Object>> getLastDeviceDevOpsInfoByParamMap(Map<String, Object> parammap);

    /**
     * @author: chengzq
     * @date: 2020/4/9 0009 下午 1:53
     * @Description: 通过自定义参数获取设备运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    List<Map<String,Object>> getDeviceDevOpsInfoByParamMap(Map<String, Object> parammap);

    /**
     * @author: chengzq
     * @date: 2021/3/11 0011 下午 1:50
     * @Description:  获取未过期的设备运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    List<Map<String,Object>> getUnexpiredDeviceDevOpsInfos(Map<String, Object> parammap);

    List<Map<String,Object>> getDevicesDevOpsInfoByTimesAndType(Map<String, Object> paramMap);

    Map<String,Object> getDeviceDevOpsDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2022/03/01 0001 下午 2:34
     * @Description: 统计某段时间例行运维完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> countDeviceDevOpsCompletionDataByParam(Map<String, Object> parammap);


    Map<String,Object> countAllPonitStatusDataByParam(Map<String, Object> paramMap);

    Map<String,Object> countAllDeviceDevOpsNumDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countDeviceDevOpsWorkOrderData(Map<String, Object> param);

    List<Map<String,Object>> countDeviceDevOpsDataGroupByMonitorType(Map<String, Object> param);

    List<Map<String,Object>> countDeviceDevOpsDataGroupByPollution(Map<String, Object> param);

    List<Map<String,Object>> countDeviceDevOpsDataGroupByMonth(Map<String, Object> param);

    List<Map<String,Object>> countDeviceExceptionRateDataByParamMap(Map<String, Object> param);

    List<Map<String,Object>> getAllDeviceDevOpsPointDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countDeviceDevOpsDataGroupByPoint(Map<String, Object> param);

    List<Map<String,Object>> getDevOpsPointTreeData(Map<String, Object> paramMap);

    List<Map<String,Object>> getRoutineDevOpsInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getRoutineDevOpsInfoDetailByID(String id);

    List<Map<String,Object>> getEntExplainInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEntExplainInfoDetailByID(String id);

    List<Map<String,Object>> getEntDevOpsExplainsByTimesAndType(Map<String, Object> paramMap);
}