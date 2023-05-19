package com.tjpu.sp.dao.environmentalprotection.envquality;


import com.tjpu.sp.model.environmentalprotection.envquality.AirCityHourDataVO;

public interface AirCityHourDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(AirCityHourDataVO record);

    int insertSelective(AirCityHourDataVO record);

    AirCityHourDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(AirCityHourDataVO record);

    int updateByPrimaryKey(AirCityHourDataVO record);

}