package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPUnGasPollutantConcentrationVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPUnGasPollutantConcentrationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPUnGasPollutantConcentrationVO record);

    int insertSelective(RPUnGasPollutantConcentrationVO record);

    RPUnGasPollutantConcentrationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPUnGasPollutantConcentrationVO record);

    int updateByPrimaryKey(RPUnGasPollutantConcentrationVO record);

    List<Map<String, Object>> getUnGasConcentrationListByParam(Map<String, Object> paramMap);
}