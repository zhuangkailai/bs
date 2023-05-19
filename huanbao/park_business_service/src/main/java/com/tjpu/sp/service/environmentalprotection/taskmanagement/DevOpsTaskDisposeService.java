package com.tjpu.sp.service.environmentalprotection.taskmanagement;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface DevOpsTaskDisposeService {


    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 11:50
     * @Description: 获取运维任务处置管理信息（有分派按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> getAllDevOpsTaskDisposeListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 11:50
     * @Description: 获取运维任务处置管理信息（无分派按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> getAssignDevOpsTaskDisposeListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 下午 5:24
     * @Description: 根据自定义参数获取运维监测点异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> getDevOpsMonitorPointExceptionDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 8:52
     * @Description: 分派运维任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void saveDevOpsTaskInfo(String userId, String username, Map<String, Object> formdata);

    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 9:32
     * @Description: 根据任务ID获取任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    AlarmTaskDisposeManagementVO selectByPrimaryKey(String pk_taskid);

    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 9:43
     * @Description: 反馈运维任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    void updateDevOpsTaskInfo(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement);

    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 11:59
     * @Description: 获取运维任务表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getTableTitleForDevOpsTask();

    /**
     * @author: xsm
     * @date: 2019/12/17 0017 上午 9:54
     * @Description: 根据报警类型和监测时间获取报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAlarmTaskInfoByRemindTypeAndParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 2:08
     * @Description: 获取有运维任务处置权限的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getDisposePersonSelectData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 2:08
     * @Description: 根据自定义参数获取企业运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    EntDevOpsInfoVO getEntDevOpsInfoVOByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 上午 9:48
     * @Description: 根据自运维任务主键ID获取运维任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getDevOpsTaskDetailByID(String id, String userId, String username);

    void addDevOpsTaskStatusToHandle(String id, String userId, String username);

    void neglectTaskInfo(AlarmTaskDisposeManagementVO alarmTaskDisposeManagement, String userId);

    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 11:36
     * @Description: 统计运维任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countDevOpsTaskCompletionStatusByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 下午 3:49
     * @Description: 根据时间范围获取运维任务信息（数据分析）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getDevOpsTaskListDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/26 0026 上午 11:36
     * @Description: 根据自定义参数获取点位运维信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getMonitorPointDevOpsTaskDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/04/22 0022 上午 10:50
     * @Description: 统计某个时间范围内运维任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDevOpsTaskRemindDataByMonitorTimes(Map<String, Object> paramMap);

    void addDevOpsTaskInfo(String monitorpointtypecode,String pollutionid, String daytime,JSONObject messageobject,Object mn);

    List<Map<String,Object>> getAllDevOpsTaskByParamForHome(Map<String, Object> param);


}
