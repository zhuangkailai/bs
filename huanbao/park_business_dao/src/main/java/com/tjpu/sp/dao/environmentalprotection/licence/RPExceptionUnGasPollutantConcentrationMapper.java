package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPExceptionUnGasPollutantConcentrationVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPExceptionUnGasPollutantConcentrationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPExceptionUnGasPollutantConcentrationVO record);

    int insertSelective(RPExceptionUnGasPollutantConcentrationVO record);

    RPExceptionUnGasPollutantConcentrationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPExceptionUnGasPollutantConcentrationVO record);

    int updateByPrimaryKey(RPExceptionUnGasPollutantConcentrationVO record);

    List<Map<String, Object>> getExceptionUnGasConcentrationListByParam(Map<String, Object> paramMap);
}