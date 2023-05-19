package com.tjpu.sp.service.environmentalprotection.taskmanagement;

import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;

import java.util.List;
import java.util.Map;

public interface TaskFlowRecordService {

    int insert(TaskFlowRecordInfoVO record);


    int updateByPrimaryKey(TaskFlowRecordInfoVO record);

    /**
     * @author: xsm
     * @date: 2020/03/13 0013 下午 15:38
     * @Description:根据任务ID和任务类型获取该任务的流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getTaskFlowRecordInfoByParamMap(Map<String, Object> paramMap);
}
