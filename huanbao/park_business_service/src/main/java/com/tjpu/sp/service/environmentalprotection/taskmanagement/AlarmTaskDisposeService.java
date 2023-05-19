package com.tjpu.sp.service.environmentalprotection.taskmanagement;

import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.model.extand.TextMessageVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface AlarmTaskDisposeService {



    Map<String, Object> getAllAlarmTaskDisposeListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 上午 11:50
     * @Description: 根据监测时间获取污染源各类型报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> getPollutionOutPutOverDataByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getMonitorPointOverDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 10:59
     * @Description: 获取处置人下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getDisposePersonSelectData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 11:29
     * @Description: 添加任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    void saveTaskInfo(String userid, String username, Map<String, Object> formdata, Integer tasktype);

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 2:17
     * @Description: 根据主键ID获取报警任务处置详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    Map<String, Object> getAlarmTaskDisposeManagementDetailByID(String id, String userId, String username);

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 4:23
     * @Description: 根据主键ID获取报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    AlarmTaskDisposeManagementVO selectByPrimaryKey(String pk_taskid);

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 4:26
     * @Description: 修改报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    void updateAlarmTaskInfo(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement);


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 7:02
     * @Description: 获取报警任务处置管理表头信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getTableTitleForAlarmTask();

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 9:48
     * @Description: 获取问题类型下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getProblemTypeSelectData(String monitorpointtypecode);

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 11:21
     * @Description: 暂存报警任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    void temporaryTaskInfo(AlarmTaskDisposeManagementVO alarmTaskDisposeManagement);

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 1:36
     * @Description: 根据时间范围获取该时间段内报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> countAlarmTaskNumDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 2:49
     * @Description: 根据时间范围获取该时间段内按企业进行分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> countAlarmTaskNumGroupByPollution(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 3:10
     * @Description: 根据时间范围获取该时间段内按问题类型进行分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> countAlarmTaskNumGroupByProblemType(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2019/7/30 0030 下午 3:59
     * @Description: 保存报警任务忽略信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    void neglectTaskInfo(AlarmTaskDisposeManagementVO alarmTaskDisposeManagement, String userId);

    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 11:23
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
     * @date: 2019/8/1 0001 下午 6:25
     * @Description: 获取所有部门下的用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getAllOrganizationUser(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2019/8/2 0002 上午 11:27
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void addAlarmTaskStatusToHandle(String id, String userId, String username);

    /**
     * @author: xsm
     * @date: 2019/8/3 0003 上午 11:23
     * @Description: 根据任务主键ID获取报警任务详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getAlarmTaskDetailByID(String id, String userId, String username);

    Map<String, Object> getMonitorPointAlarmTaskDetailByID(String id, String userId, String name);

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

    /**
     * @author: chengzq
     * @date: 2019/8/19 0019 下午 1:08
     * @Description: 通过主键删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkTaskid]
     * @throws:
     */
    int deleteByPrimaryKey(String pkTaskid);

    /**
     * @author: chengzq
     * @date: 2019/8/21 0021 上午 9:51
     * @Description: 修改任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    int updateByPrimaryKey(AlarmTaskDisposeManagementVO record);


    /**
     * @author: chengzq
     * @date: 2019/8/21 0021 上午 9:51
     * @Description: 新增任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    int insert(AlarmTaskDisposeManagementVO record);


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
     * @Description: 通过自定义参数获取任务流程信息
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
    List<Map<String,Object>> getTaskDisposeNumDataByParams(Map<String, Object> paramMap);

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
    List<Map<String,Object>> countPollutionAlarmTaskGroupByStatusByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/8/27 0027 上午 11:18
     * @Description: 通过id查询处理中用户id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkTaskid]
     * @throws:
     */
    String getFlowRecordInfoByTaskID(String pkTaskid);

    List<Map<String,Object>> countTaskDisposeNumGroupByStatusByParams(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getAlarmTaskInfoByParamMap(Map<String, Object> paramMap);


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
     * @date: 2020/1/13 0013 上午 10:23
     * @Description: 根据自定义参数获取一段时间范围内的报警任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> getAlarmTaskListDataByParam(Map<String, Object> paramMap);

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

    Map<String,Object> getAlarmAndDevOpsTaskNumByParam(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 13:07
     * @Description:根据任务类型和自定义参数获取报警、运维任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String,Object> getTaskListDataByTaskTypeAndParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 16:23
     * @Description:根据任务类型统计该类型任务的时效性
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> countTaskDisposeTimelinessNumByTaskType(Map<String, Object> paramMap);

    List<Map<String,Object>>  getTaskInfo(Map<String, Object> paramMap);

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

    List<Map<String,Object>> getUserMonitorPointRelationDataByUserId(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskDisposeManagementDataByParam(Map<String, Object> paramMap);

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


    List<Map<String,Object>> getTodayTaskInfoByTaskType( Map<String,Object> param);


    /**
     * @author: xsm
     * @date: 2020/09/25 0025 下午 13:42
     * @Description: 添加任务转办流程信息
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void addTransferTaskInfo( Map<String, Object> paramMap);


    List<Map<String,Object>> getMonitrpointTaskInfoByParamMap(Map<String, Object> paramMap);

    Integer CountMonitrpointTaskInfoByParamMap(String userid);

    List<Map<String,Object>> getMonitorPointLastTaskInfoByTaskType(Map<String, Object> param);

    void addTaskPollutantInfo(List<String> addcode, Object taskid, String s);

    void insertTaskFlowRecordInfoVO(TaskFlowRecordInfoVO obj);

    List<Map<String,Object>> countUnassignedTaskDataNum(Map<String, Object> paramMap);

    List<Map<String,Object>> countOtherStatusTaskDataNumForUserID(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllAlarmTaskByParamForHome(Map<String, Object> param);

    Map<String,Object> getAllAlarmTaskDisposeListDataByParamMapForApp(Map<String, Object> paramMap);

    List<Map<String,Object>> getOwnTaskDisposeInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOwnPetitionByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countAlarmTaskByParamMap(Map<String, Object> paramMap);

    void updateByUseridAndTaskid(Map<String, Object> paramMap);

    List<String> getUseridsByTaskid(String taskid);

    Map<String,Object> getPointLastTaskInfoByParamMap(Map<String, Object> paramMap);

    void updateAlarmTaskEndTime(Object taskid,String daytime);
    void addAlarmTaskInfo(AlarmTaskDisposeManagementVO obj, List<String> overcodes, JSONObject messageobject);

    Long getAlarmTaskNumByMonitorTimes(Map<String, Object> param);

    List<Map<String,Object>> countUserTaskInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countTaskDataByParamGroupByTaskType(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllVideoAlarmTaskByParamForHome(Map<String, Object> paramMap);

    Long countAllOverAlarmTaskCountByParams(Map<String, Object> param);

    List<Map<String,Object>> countSecurityAlarmTaskByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAPPDaliyTaskByParamMap(JSONObject paramMap);

    List<Map<String,Object>> countCompletedStatusTaskDataNumForUserID(Map<String, Object> paramMap);

    Map<String,Object> getTaskInfoDataByTaskID(Map<String, Object> param);

    List<Map<String,Object>> getDisposePersonSelectDataByParams(Map<String, Object> param);

    void addTextMessageDatas(List<TextMessageVO> listobj);

    Map<String,Object> getAssignmentTaskUserInfo(Map<String, Object> param);

    List<Map<String,Object>> getAssignAndUndisposedTaskNumDataByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskCarbonCopyUsersByTaskID(Map<String, Object> paramMap);

    void insertReviewedInfo(List<TaskFlowRecordInfoVO> objs);

    void updateTaskCarbonCopyStatus(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement);

    void addCarbonCopyUserCompleteTaskInfo(String userId, Object comment,AlarmTaskDisposeManagementVO alarmTaskDisposeManagement);

    void addCarbonCopyUserRepulseTaskInfo(String userId, AlarmTaskDisposeManagementVO alarmTaskDisposeManagement, JSONObject jsonObject);

    void automaticDispatchAlarmTask(String taskid,List<String> userids);

    void addNewAlarmTaskInfo(AlarmTaskDisposeManagementVO obj, List<String> overcodes, JSONObject messageobject);

    Map<String,Object> getOnePointOverDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getNewPointLastTaskInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countCarbonCopyTaskDataNum(Map<String, Object> paramMap);

    List<Map<String,Object>> countOtherStatusAlarmTaskNumByUserID(Map<String, Object> paramMap);

    List<Map<String,Object>> getUnassignedTaskDisposeInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getOwnTaskDisposeIdsByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAssignAndUndisposedTaskNumDataGroupByType(Map<String, Object> paramMap);

    long getTotalTaskNumByParam(Map<String, Object> paramMap);

    Map<String,Object> getAlarmTaskDataForOverViewByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> countTaskCompletionDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getFidAndTypeByParam(Map<String, Object> param);

    Map<String, Object> countTaskByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getTaskDisposeDataListByParam(Map<String, Object> paramMap);



    List<Map<String, Object>> getLastFlowDataListByParam(Map<String, Object> dataMap);
}
