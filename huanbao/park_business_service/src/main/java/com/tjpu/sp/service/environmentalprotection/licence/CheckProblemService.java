package com.tjpu.sp.service.environmentalprotection.licence;

import java.util.List;
import java.util.Map;

public interface CheckProblemService {

    List<Map<String, Object>> getProblemSourceDataByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> countExecuteReportData(Map<String, Object> paramMap);

    List<Map<String, Object>> countStandingBookReport(Map<String, Object> paramMap);
}
