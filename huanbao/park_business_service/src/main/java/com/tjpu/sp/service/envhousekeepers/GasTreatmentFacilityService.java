package com.tjpu.sp.service.envhousekeepers;


import com.tjpu.sp.model.envhousekeepers.PollutionProductFacilityVO;

import java.util.List;
import java.util.Map;

public interface GasTreatmentFacilityService {


    void insertData(PollutionProductFacilityVO pollutionProductFacilityVO, Object chlidformdata) throws Exception;

    void updateData(PollutionProductFacilityVO pollutionProductFacilityVO, Object chlidformdata) throws Exception;

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void deleteInfoById(String id);

    void deleteProductById(String id);

    PollutionProductFacilityVO getProductInfoById(String facilityid);

    List<Map<String, Object>> getGasOutPutByPollutionId(String pollutionid);

    List<Map<String,Object>> getGasTreatmentListDataByParamMap(Map<String, Object> paramMap);
}
