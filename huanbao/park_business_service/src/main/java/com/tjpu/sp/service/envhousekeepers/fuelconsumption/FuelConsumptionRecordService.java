package com.tjpu.sp.service.envhousekeepers.fuelconsumption;

import com.tjpu.sp.model.envhousekeepers.fuelconsumption.FuelConsumptionRecordVO;

import java.util.List;
import java.util.Map;

public interface FuelConsumptionRecordService {

    List<Map<String,Object>> getFuelConsumptionRecordByParamMap(Map<String, Object> paramMap);

    void insert(FuelConsumptionRecordVO entity);

    Map<String, Object> selectByPrimaryKey(String id);

    void updateByPrimaryKey(FuelConsumptionRecordVO entity);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getFuelConsumptionRecordDetailByID(String id);
}
