package com.tjpu.sp.service.environmentalprotection.superviseenforcelaw;


import com.tjpu.sp.model.environmentalprotection.superviseenforcelaw.TaskInfoVO;

import java.util.List;
import java.util.Map;

public interface EnforceLawTaskInfoService {

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 1:38
     * @Description:根据自定义参数获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getEnforceLawTaskInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:19
     * @Description:新增执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(TaskInfoVO taskInfoVO);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:25
     * @Description:修改执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(TaskInfoVO taskInfoVO);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:27
     * @Description:根据主键ID删除执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:30
     * @Description:根据主键ID获取执法任务详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getEnforceLawTaskInfoDetailByID(String taskid);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 4:00
     * @Description:获取执法任务表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getTableTitleForEnforceLawTaskInfo();

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 4:00
     * @Description:根据id获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    TaskInfoVO selectByPrimaryKey(String id);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 18:43
    *@Description: 根据企业id统计监察执法信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<Object,Object>> countEnforceLawTaskByPollutionId(String pollutionid);
}
