package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPSpecialGasPollutantConcentrationVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPSpecialGasPollutantConcentrationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPSpecialGasPollutantConcentrationVO record);

    int insertSelective(RPSpecialGasPollutantConcentrationVO record);

    RPSpecialGasPollutantConcentrationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPSpecialGasPollutantConcentrationVO record);

    int updateByPrimaryKey(RPSpecialGasPollutantConcentrationVO record);

    List<Map<String, Object>> getSpecialGasConcentrationListByParam(Map<String, Object> paramMap);
}