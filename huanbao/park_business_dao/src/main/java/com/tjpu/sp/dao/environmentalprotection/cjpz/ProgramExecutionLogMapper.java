package com.tjpu.sp.dao.environmentalprotection.cjpz;

import com.tjpu.sp.model.environmentalprotection.cjpz.ProgramExecutionLogVO;

import java.util.List;
import java.util.Map;

public interface ProgramExecutionLogMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(ProgramExecutionLogVO record);

    int insertSelective(ProgramExecutionLogVO record);

    ProgramExecutionLogVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(ProgramExecutionLogVO record);

    int updateByPrimaryKey(ProgramExecutionLogVO record);

    void clearProgramExecutionLogs();

    Map<String,Object> getProgramExecutionLogDetailByID(String id);

    List<Map<String,Object>> getProgramExecutionLogsByParamMap(Map<String, Object> parammap);
}