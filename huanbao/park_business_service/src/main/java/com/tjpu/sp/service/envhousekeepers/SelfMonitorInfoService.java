package com.tjpu.sp.service.envhousekeepers;




import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;


import java.util.List;
import java.util.Map;

public interface SelfMonitorInfoService {

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void deleteInfoById(String id);

    void updateData(SelfMonitorInfoVO selfMonitorInfoVO);

    void insertData(SelfMonitorInfoVO selfMonitorInfoVO);

    List<Map<String, Object>> getOutPutByParam(Map<String, Object> paramMap);

    List<String> getMonitorContentByPollutionId(Map<String, Object> paramMap);
}
