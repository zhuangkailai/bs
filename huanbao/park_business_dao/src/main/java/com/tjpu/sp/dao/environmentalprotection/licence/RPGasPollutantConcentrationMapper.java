package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPGasPollutantConcentrationVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPGasPollutantConcentrationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPGasPollutantConcentrationVO record);

    int insertSelective(RPGasPollutantConcentrationVO record);

    RPGasPollutantConcentrationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPGasPollutantConcentrationVO record);

    int updateByPrimaryKey(RPGasPollutantConcentrationVO record);

    List<Map<String, Object>> getGasConcentrationListByParam(Map<String, Object> paramMap);
}