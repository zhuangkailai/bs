package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPGasPollutantSpeedVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPGasPollutantSpeedMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPGasPollutantSpeedVO record);

    int insertSelective(RPGasPollutantSpeedVO record);

    RPGasPollutantSpeedVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPGasPollutantSpeedVO record);

    int updateByPrimaryKey(RPGasPollutantSpeedVO record);

    List<Map<String, Object>> getGasSpeedListByParam(Map<String, Object> paramMap);
}