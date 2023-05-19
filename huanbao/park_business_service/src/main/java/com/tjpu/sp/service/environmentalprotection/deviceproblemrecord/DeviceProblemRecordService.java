package com.tjpu.sp.service.environmentalprotection.deviceproblemrecord;


import java.util.List;
import java.util.Map;

public interface DeviceProblemRecordService {

    List<Map<String,Object>> getDeviceProblemRecordsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getDeviceProblemRecordDetailById(Map<String, Object> paramMap);


}
