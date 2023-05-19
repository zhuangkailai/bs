package com.tjpu.sp.service.envhousekeepers;




import com.tjpu.sp.model.envhousekeepers.WaterTreatmentFacilityVO;

import java.util.List;
import java.util.Map;

public interface WaterTreatmentFacilityService {



    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    void deleteInfoById(String id);


    void updateData(WaterTreatmentFacilityVO waterTreatmentFacilityVO);

    void insertData(WaterTreatmentFacilityVO waterTreatmentFacilityVO);

    List<Map<String, Object>> getWaterOutPutByPollutionId(Map<String, Object> paramMap);
}
