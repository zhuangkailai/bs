package com.tjpu.sp.service.environmentalprotection.licence;


import java.util.List;
import java.util.Map;

public interface RPStandingBookRecordDataService {

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getInfoPublicDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getYearTextContentByParam(Map<String, Object> paramMap);
}
