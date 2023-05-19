package com.tjpu.sp.dao.environmentalprotection.superviseenforcelaw;

import com.tjpu.sp.model.environmentalprotection.superviseenforcelaw.TaskInfoVO;

import java.util.List;
import java.util.Map;

public interface TaskInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(TaskInfoVO record);

    int insertSelective(TaskInfoVO record);

    TaskInfoVO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TaskInfoVO record);

    int updateByPrimaryKey(TaskInfoVO record);

    List<Map<String, Object>> getEnforceLawTaskInfosByParamMap(Map<String, Object> paramMap);

    Map<String, Object> getEnforceLawTaskInfoDetailByID(String pkid);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 18:44
    *@Description: 根据企业id统计监察执法信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<Object,Object>> countEnforceLawTaskByPollutionId(String pollutionid);
}