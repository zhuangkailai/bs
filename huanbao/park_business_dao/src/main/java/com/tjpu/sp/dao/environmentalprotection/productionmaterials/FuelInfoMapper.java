package com.tjpu.sp.dao.environmentalprotection.productionmaterials;

import com.tjpu.sp.model.environmentalprotection.productionmaterials.FuelInfoVO;

import java.util.List;
import java.util.Map;

public interface FuelInfoMapper {
    int deleteByPrimaryKey(String pkFuelinfoid);

    int insert(FuelInfoVO record);

    int insertSelective(FuelInfoVO record);

    FuelInfoVO selectByPrimaryKey(String pkFuelinfoid);

    int updateByPrimaryKeySelective(FuelInfoVO record);

    int updateByPrimaryKey(FuelInfoVO record);

    List<Map<String,Object>> getFuelInfosByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getFuelInfoDetailByID(String pkid);
}