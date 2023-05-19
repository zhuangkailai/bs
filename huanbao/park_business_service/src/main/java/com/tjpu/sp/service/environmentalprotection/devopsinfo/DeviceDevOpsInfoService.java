package com.tjpu.sp.service.environmentalprotection.devopsinfo;


import com.tjpu.sp.model.environmentalprotection.devopsinfo.DeviceDevOpsInfoVO;

import java.util.List;
import java.util.Map;

public interface DeviceDevOpsInfoService {

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
     * @date: 2019/12/05 0005 下午 1:20
     * @Description: 新增设备运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void addDeviceDevOpsInfo(DeviceDevOpsInfoVO entity);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:20
     * @Description: 根据主键ID获取运维设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    DeviceDevOpsInfoVO getDeviceDevOpsInfoByPkid(String id);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 1:20
     * @Description: 修改运维设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void editDeviceDevOpsInfo(DeviceDevOpsInfoVO entity);

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
     * @date: 2019/12/05 0005 下午 2:26
     * @Description: 根据污染源ID和监测类型获取排口或监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getMonitorPointDataByPollutionIDAndType(List<String> pollutionids, Integer monitorpointtype);

    /**
     * @author: xsm
     * @date: 2019/12/05 0005 下午 2:41
     * @Description: 根据监测点ID和监测类型获取排口或监测点设置污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getMonitorPointPollutantDataByIDAndType(String monitorpointid, Integer monitorpointtype);

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

    /**
     *
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


    List<Map<String,Object>> getEntMonitorPointDataByPollutionID(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/02/24 0024 下午 2:25
     * @Description: 通过运维记录ID获取运维详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
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

    /**
     * @author: xsm
     * @date: 2022/03/03 0003 上午 9:09
     * @Description: 通过运维记录ID删除运维详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void deleteDeviceDevOpsInfoByID(String id);

    List<Map<String,Object>> getDevOpsPointTreeData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取例行运维列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    List<Map<String,Object>> getRoutineDevOpsInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getRoutineDevOpsInfoDetailByID(String id);


    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取运维记录统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    List<Map<String,Object>> getDevOpsRecordStatisticsDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 获取运维记录表头列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    List<Map<String,Object>> getDevOpsRecordStatisticsTableTitleData();

    /**
     * @author: xsm
     * @date: 2022/04/12 0012 上午 9:40
     * @Description: 根据自定义参数获取企业说明列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    List<Map<String,Object>> getEntExplainInfosByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2022/04/13 0013 上午 8:52
     * @Description: 获取企业说明详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    Map<String,Object> getEntExplainInfoDetailByID(String id);
}
