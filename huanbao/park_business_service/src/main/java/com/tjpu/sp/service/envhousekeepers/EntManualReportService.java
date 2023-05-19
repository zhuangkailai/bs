package com.tjpu.sp.service.envhousekeepers;


import com.tjpu.sp.model.envhousekeepers.EntManualReportVO;

import java.util.List;
import java.util.Map;


public interface EntManualReportService {


    void updateInfo(EntManualReportVO entManualReportVO);

    void insertInfo(EntManualReportVO entManualReportVO);

    void deleteInfoById(String id);


    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);
}
