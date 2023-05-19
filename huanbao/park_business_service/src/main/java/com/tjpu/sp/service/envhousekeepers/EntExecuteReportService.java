package com.tjpu.sp.service.envhousekeepers;


import com.tjpu.sp.model.envhousekeepers.EntExecuteReportVO;

import java.util.List;
import java.util.Map;

public interface EntExecuteReportService {


    void updateInfo(EntExecuteReportVO entExecuteReportVO);

    void insertInfo(EntExecuteReportVO entExecuteReportVO);

    void deleteInfoById(String id);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> getEntLastExecuteDataByParamMap(Map<String, Object> paramMap);
}
