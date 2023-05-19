package com.tjpu.sp.dao.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface TaskFlowRecordInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TaskFlowRecordInfoVO record);

    int insertSelective(TaskFlowRecordInfoVO record);

    TaskFlowRecordInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TaskFlowRecordInfoVO record);

    int updateByPrimaryKey(TaskFlowRecordInfoVO record);


    int updateUserByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskFlowRecordInfoByTaskID(String pkTaskid);

    String getFlowRecordInfoByTaskID(String pkTaskid);

    int deleteByTaskid(String pkTaskid);

    List<Map<String,Object>> getTaskFlowRecordInfoByParamMap(Map<String, Object> paramMap);

    void batchInsert(@Param("list") List<TaskFlowRecordInfoVO> paramList);

    List<Map<String,Object>> getTaskCarbonCopyUsersByTaskID(Map<String, Object> paramMap);
}