package com.tjpu.sp.service.environmentalprotection.taskmanagement;

import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;

import java.util.List;
import java.util.Map;

public interface ComplaintTaskDisposeService {



    List<Map<String, Object>> getAllComplaintTaskDisposeListDataByParamMap(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 11:29
     * @Description: 添加投诉任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    void saveComplaintTaskInfo(String userId, String username, Map<String, Object> formdata);

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
    List<Map<String, Object>> getTableTitleForComplaintTask();

    /**
     * @author: xsm
     * @date: 2019/8/6 0006 下午 5:26
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void addComplaintTaskStatusToHandle(String id, String userId, String username);

    /**
     * @author: lip
     * @date: 2019/8/12 0012 下午 3:42
     * @Description: 自定义查询条件获取任务处置数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTaskDisposeNumDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/24 0024 下午 1:17
     * @Description: 自定义查询条件获取某个状态的投诉任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getComplaintTaskDisposeNumDataByParams(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:02
     * @Description:保存投诉任务反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateAlarmTaskInfo(String userId, PetitionInfoVO petitionInfo);

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:25
     * @Description:根据投诉任务ID获取投诉任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    PetitionInfoVO selectByPrimaryKey(String pk_id);

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:42
     * @Description:暂存投诉任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void temporaryTaskInfo(PetitionInfoVO petitionInfo);

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:53
     * @Description:获取投诉任务暂存信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getComplaintTaskTemporaryInfo(String pk_taskid, String userId, String username);

    List<Map<String,Object>> countTaskDisposeNumGroupByStatusByParams(Map<String, Object> paramMap);
}
