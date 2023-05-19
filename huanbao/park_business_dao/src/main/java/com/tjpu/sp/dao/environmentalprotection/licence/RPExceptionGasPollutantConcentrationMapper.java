package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPExceptionGasPollutantConcentrationVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPExceptionGasPollutantConcentrationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPExceptionGasPollutantConcentrationVO record);

    int insertSelective(RPExceptionGasPollutantConcentrationVO record);

    RPExceptionGasPollutantConcentrationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPExceptionGasPollutantConcentrationVO record);

    int updateByPrimaryKey(RPExceptionGasPollutantConcentrationVO record);

    List<Map<String, Object>> getExceptionGasConcentrationListByParam(Map<String, Object> paramMap);
}