package com.tjpu.sp.dao.envhousekeepers.fuelconsumption;

import com.tjpu.sp.model.envhousekeepers.fuelconsumption.FuelConsumptionRecordVO;

import java.util.List;
import java.util.Map;

public interface FuelConsumptionRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(FuelConsumptionRecordVO record);

    int insertSelective(FuelConsumptionRecordVO record);

    FuelConsumptionRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(FuelConsumptionRecordVO record);

    int updateByPrimaryKey(FuelConsumptionRecordVO record);

    List<Map<String,Object>> getFuelConsumptionRecordByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getFuelConsumptionRecordDetailByID(String id);
}