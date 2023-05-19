package com.tjpu.sp.service.environmentalprotection.workflowinfo;


import com.tjpu.sp.model.environmentalprotection.workflowinfo.WorkFlowInfoVO;

import java.util.List;
import java.util.Map;

public interface WorkFlowInfoService {

    int deleteByPrimaryKey(String pkId);

    int insert(WorkFlowInfoVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    Map<String,Object> selectByWorkFlowType(String fkworkflowtype);


    int updateByPrimaryKey(WorkFlowInfoVO record);
    int updateByWorkFlowType(WorkFlowInfoVO record);

    /**
     * @author: chengzq
     * @date: 2021/05/07 0016 下午 2:37
     * @Description:  通过自定义参数获取工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getWorkFlowInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2021/05/07 0016 下午 2:37
     * @Description:  通过id获取工作流程详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getWorkFlowInfoDetailByID(String pkid);
}
