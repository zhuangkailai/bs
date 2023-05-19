package com.tjpu.sp.service.envhousekeepers;


import com.tjpu.sp.model.envhousekeepers.EntExecuteReportVO;
import com.tjpu.sp.model.envhousekeepers.EntWorkDynamicVO;

import java.util.List;
import java.util.Map;

public interface EntWorkDynamicService {


    void updateInfo(EntWorkDynamicVO entWorkDynamicVO);

    void insertInfo(EntWorkDynamicVO entWorkDynamicVO);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void deleteInfoById(String id);

    Map<String, Object> getEditOrDetailsDataById(String id);
}
