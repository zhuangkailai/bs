package com.tjpu.sp.service.envhousekeepers;



import com.tjpu.sp.model.envhousekeepers.EntStandingBookReportVO;

import java.util.List;
import java.util.Map;

public interface EntStandingBookReportService {


    void updateInfo(EntStandingBookReportVO entStandingBookReportVO);

    void insertInfo(EntStandingBookReportVO entStandingBookReportVO);

    void deleteInfoById(String id);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> countEntStandingBookData(String pollutionid);

    Map<String,Object> getEntStandingBookDetailByID(String id);

    List<Map<String, Object>> getUpdateDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getNoUpdateDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getAllEntStandingByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> getEntLastStandingDataByParamMap(Map<String, Object> paramMap);
}
