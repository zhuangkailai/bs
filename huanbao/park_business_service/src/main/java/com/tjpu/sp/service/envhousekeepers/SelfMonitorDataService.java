package com.tjpu.sp.service.envhousekeepers;


import com.tjpu.sp.model.envhousekeepers.SelfMonitorDataInfoVO;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;

import java.util.List;
import java.util.Map;

public interface SelfMonitorDataService {

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void deleteInfoById(String id);

    void updateData(SelfMonitorDataInfoVO selfMonitorDataInfoVO);

    void insertData(SelfMonitorDataInfoVO selfMonitorDataInfoVO);


}
