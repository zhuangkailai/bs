package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.WaterTreatmentFacilityMapper;
import com.tjpu.sp.model.envhousekeepers.WaterTreatmentFacilityVO;
import com.tjpu.sp.service.envhousekeepers.WaterTreatmentFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class WaterTreatmentFacilityServiceImpl implements WaterTreatmentFacilityService {
    @Autowired
    private WaterTreatmentFacilityMapper waterTreatmentFacilityMapper;


    @Override
    public void insertData(WaterTreatmentFacilityVO waterTreatmentFacilityVO)   {
        waterTreatmentFacilityMapper.insert(waterTreatmentFacilityVO);

    }

    @Override
    public List<Map<String, Object>> getWaterOutPutByPollutionId(Map<String, Object> paramMap) {
        return waterTreatmentFacilityMapper.getWaterOutPutByPollutionId(paramMap);
    }

    @Override
    public void updateData(WaterTreatmentFacilityVO waterTreatmentFacilityVO)   {
        waterTreatmentFacilityMapper.updateByPrimaryKey(waterTreatmentFacilityVO);

    }

    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return waterTreatmentFacilityMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void deleteInfoById(String id) {
        waterTreatmentFacilityMapper.deleteByPrimaryKey(id);
    }


}
