package com.tjpu.sp.service.environmentalprotection.cjpz;


import java.util.List;
import java.util.Map;

public interface ProgramExecutionLogService {

    void clearProgramExecutionLogs();

    Map<String,Object> getProgramExecutionLogDetailByID(String id);

    List<Map<String,Object>> getProgramExecutionLogsByParamMap(Map<String, Object> parammap);

    Long countProgramExecutionLogNumByTimes(Map<String, Object> paramMap);
}
