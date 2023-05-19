package com.tjpu.sp.dao.environmentalprotection.taskmanagement;

import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface AlarmTaskDisposeManagementMapper {
    int deleteByPrimaryKey(String pkTaskid);

    int insert(AlarmTaskDisposeManagementVO record);

    int insertSelective(AlarmTaskDisposeManagementVO record);

    AlarmTaskDisposeManagementVO selectByPrimaryKey(String pkTaskid);

    int updateByPrimaryKeySelective(AlarmTaskDisposeManagementVO record);

    int updateByPrimaryKey(AlarmTaskDisposeManagementVO record);

    List<Map<String, Object>> getAllOutputMn(Map<String, Object> paramMap);

    List<Map<String, Object>> getAllPollutants(Map<String, Object> paramMap);

    List<Map<String, Object>> getDisposePersonSelectData(Map<String, Object> paramMap);

    List<Map<String, Object>> getProblemTypeSelectData(@Param("monitorpointtypecode") String monitorpointtypecode);

    List<Map<String, Object>> getAlarmTaskDisposeManagementByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getAlarmTaskNumGroupByPollution(Map<String, Object> paramMap);

    List<Map<String, Object>> getAlarmTaskNumGroupByProblemType(Map<String, Object> paramMap);

    List<Map<String, Object>> getDisposePersonSelectDataByParams(Map<String, Object> usidmap);

    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 11:26
     * @Description: 自定义查询条件获取报警处置数量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAlarmTaskDisposeNumDataByParams(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/8/1 0001 下午 6:26
     * @Description: 获取所有部门下的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllOrganizationUser(Map<String, Object> param);


    /**
     * @author: chengzq
     * @date: 2019/8/19 0019 上午 8:48
     * @Description: 通过自定义条件查询日常任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    List<Map<String, Object>> getDaliyTaskByParamMap(Map<String, Object> param);


    List<Map<String, Object>> getAllAlarmTaskInfoByParams(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/8/22 0022 上午 8:41
     * @Description: 通过id获取日常任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> getDaliyTaskDetailByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/8/23 0023 上午 9:36
     * @Description: 获取任务流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> getDaliyTaskRecordInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/23 0023 上午 9:18
     * @Description: 根据监测时间获取该月份报警任务列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAlarmTaskListDataByMonitortime(Map<String, Object> paramMap);

    int getAlarmTaskNumByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/24 0024 上午 11:28
     * @Description: 根据自定义参数统计某状态的任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getTaskDisposeNumDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/27 0027 上午 9:21
     * @Description: 根据自定义参数按任务状态分组统计单个企业的任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> countPollutionAlarmTaskGroupByStatusByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getAllEntPollutants();

    Long getAllAlarmTaskInfoCountByParams(Map<String, Object> paramMap);

    List<Map<String, Object>> countTaskDisposeNumGroupByStatusByParams(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2019/12/02 0002 下午 1:09
     * @Description: app端根据污染源ID获取关联的报警任务信息及状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAlarmTaskInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 下午 1:24
     * @Description: 获取运维任务总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Long getAllDevOpsTaskInfoCountByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 下午 2:19
     * @Description: 根据自定义条件获取运维任务信息（有分配按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAllDevOpsTaskInfoByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 下午 3:14
     * @Description: 根据监测类型获取相关类型的污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAllEntPollutantsByPointTypes(@Param("pointtypes") Set<String> pointtypes);

    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 10:19
     * @Description: 根据任务ID获取关联运维任务的所有点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorPointInfoByTaskIds(Map<String, Object> paramMap);

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
    List<Map<String, Object>> getAlarmTaskInfoByRemindTypeAndParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/12/27 0027 上午 11:19
     * @Description: 根据任务类型和污染源id获取监测报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getLastRealTimeMonitorTaskByParam(Map<String, Object> param);

    /**
     * @Author: zhangzhenchao
     * @Date: 2019/12/31 17:00
     * @Description: 安全首页任务统计 根据任务类型和状态
     * @Param:
     * @Return:
     */
    List<Map<String, Object>> countTaskForTaskTypeAndStatue();


    /**
     * @author: xsm
     * @date: 2020/1/6 0006 下午 1:57
     * @Description: 根据自定义参数获取逾期安全监测监控任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Long CountBeOverdueTaskNumByParam(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2020/1/6 0006 下午 3:30
     * @Description: 自定义查询条件获取任务及任务流程记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTaskAndRecordListDataByParams(Map<String, Object> paramMap);


    /**
     * @author: zhangzhenchao
     * @date: 2020/1/9 14:25
     * @Description: 根据任务类型统计各个任务类型待分派任务个数
     * @param:
     * @return:
     */
    List<Map<String, Object>> countNeedassignNumByTaskTypes(Map<String, Object> param);

    /**
     * @author: zhangzhenchao
     * @date: 2020/1/9 14:33
     * @Description: 根据任务类型统计各个任务类型待处理任务个数
     * @param:
     * @return:
     */
    List<Map<String, Object>> countNeedFeedBackNumByTaskTypes(@Param("userID") String userID, @Param("taskTypes") List<Integer> taskTypes);


    /**
     * @author: chengzq
     * @date: 2020/1/10 0010 下午 1:51
     * @Description: 通过任务类型集合获取任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getTaskInfoByTaskTypes(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 10:18
     * @Description: 统计报警任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String, Object> countAlarmTaskCompletionStatusByParamMap(Map<String, Object> paramMap);

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
     * @date: 2020/1/13 0013 上午 10:23
     * @Description: 根据自定义参数获取一段时间范围内的报警任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAlarmTaskListDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 下午 13:10
     * @Description: 根据监测时间段返回该时间段内所有日期的报警情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getTaskSituationByParamMap(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getDevOpsTaskListDataByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/20 0020 下午 16:32
     * @Description: 根据时间范围获取该时间段内运维任务各个任务状态的统计情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countDevOpsTaskNumGroupByStatusByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/02/21 0021 上午 9:51
     * @Description:根据自定义参数获取以监测点位分组的运维任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDevOpsTaskInfosGroupByMonitorPointByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/02/21 0021 上午 11:00
     * @Description:根据自定义参数统计按监测类型分组的运维任务条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countDevOpsTaskDataNumGroupByMonitorTypeByParamMap(Map<String, Object> paramMap);


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


    List<Map<String,Object>>  getTaskInfo(Map<String, Object> paramMap);

    List<Map<String, Object>> getAlarmTaskByDgimnAndTimes(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 16:32
     * @Description: 根据任务类型获取任务处置时效性信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getTaskDisposeTimelinessInfoByTaskType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 16:32
     * @Description: 根据任务类型统计该类型任务的时效性
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> countTaskDisposeTimelinessNumByTaskType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/02 0002 下午 16:58
     * @Description:根据时间和任务类型统计每日任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String,Object>> countTaskNumGroupByMonitorTimeByTaskType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 8:56
     * @Description:根据自定义查询条件统计处置人员任务完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String,Object>> countTaskNumGroupByDisposerPeopleByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 8:34
     * @Description:各任务状态任务数量统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String,Object>> countTaskNumGroupByTaskStatusByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getDevOpsTaskRemindDataByMonitorTimes(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskDisposeManagementDataByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2020/5/13 0013 下午 2:29
     * @Description: 自定义查询条件统计处置任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countTaskDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countTaskNumGroupByCompanyByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/5/15 0015 下午 13:17
     * @Description: 统计某一段时间内报警及运维工单各状态情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> countAlarmAndDevopsTaskStatusNumByMonitorTimes(Map<String, Object> paramMap);

    List<Map<String,Object>> getAlarmTaskListDataByParamAndDataUserId(Map<String, Object> paramMap);

    List<Map<String,Object>> getDevOpsTaskListDataByParamAndDataUserId(Map<String, Object> paramMap);

    List<Map<String,Object>> getTodayTaskInfoByTaskType( Map<String,Object> param);

    List<Map<String,Object>> getHiddenTaskRecordCountDataByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskDisposeCountData(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllSecurityMonitorPointInfoByParam(Map<String, Object> paramMap);

    List<Map<String,Object>>  getDayTaskInfoByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAlarmTaskDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getRiskVideoAlarmDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getVideoTaskAndRecordListDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllSecurityOutputMn(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllSecurityMonitorPointInfoByTaskIds(Map<String, Object> paramMap);

    Map<String,Object> getSecurityAlarmTaskByDgimnAndTimes(Map<String, Object> paramMap);

    Map<String,Object> getSecurityMonitorPointDevOpsTaskDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getMonitrpointTaskInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getMonitorPointLastTaskInfoByTaskType(Map<String, Object> param);

    Map<String, Object> getAlarmTaskDisposeManagementDetailByID(Map<String, Object> param);

    List<Map<String,Object>> countUnassignedTaskDataNum(Map<String, Object> paramMap);

    List<Map<String,Object>> countOtherStatusTaskDataNumForUserID(Map<String, Object> paramMap);

    List<Map<String,Object>> getMonitorPointTaskStatusByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getOwnTaskDisposeInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOwnPetitionByParamMap(Map<String, Object> paramMap);


    List<Map<String,Object>> countAlarmTaskByParamMap(Map<String, Object> paramMap);
    void updateByUseridAndTaskid(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllTaskFlowRecordInfoByParams(Map<String, Object> paramMap);

    List<String> getUseridsByTaskid(String taskid);


    Map<String,Object> getPointLastTaskInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAlarmPollutantInfoByParamMap(Map<String, Object> paramMap);
    
    List<Map<String,Object>> countUserTaskInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countSecurityAlarmTaskByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countTaskDataByParamGroupByTaskType(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllVideoAlarmTaskByParamForHome(Map<String, Object> param);

    Long countAllOverAlarmTaskCountByParams(Map<String, Object> param);

    List<Map<String,Object>> getAPPDaliyTaskByParamMap(JSONObject paramMap);

    List<Map<String,Object>> countCompletedStatusTaskDataNumForUserID(Map<String, Object> paramMap);

    Long countAllSecurityAlarmTaskInfoNumByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllSecurityAlarmTaskInfoByParams(Map<String, Object> paramMap);

    Map<String,Object> getTaskInfoDataByTaskID(Map<String, Object> param);

    Map<String,Object> getAssignmentTaskUserInfo(Map<String, Object> param);

    List<Map<String,Object>> getAssignAndUndisposedTaskNumDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllAlarmTaskPollutantDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> countCarbonCopyTaskDataNum(Map<String, Object> paramMap);

    List<Map<String,Object>> countOtherStatusAlarmTaskNumByUserID(Map<String, Object> paramMap);

    List<Map<String,Object>> getUnassignedTaskDisposeInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOwnTaskDisposeIdsByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getUnassignedTaskPollutantDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getLastMonthAlarmTaskDisposalByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAssignAndUndisposedTaskNumDataGroupByType(Map<String, Object> paramMap);

    long getTotalTaskNumByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> countTaskCompletionDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getFidAndTypeByParam(Map<String, Object> param);

    Long getAllDaliyTaskByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getTaskDisposeDataListByParam(Map<String, Object> paramMap);



    List<Map<String, Object>> getLastFlowDataListByParam(Map<String, Object> dataMap);
}