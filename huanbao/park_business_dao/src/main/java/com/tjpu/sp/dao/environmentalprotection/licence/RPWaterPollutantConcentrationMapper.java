package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPWaterPollutantConcentrationVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPWaterPollutantConcentrationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPWaterPollutantConcentrationVO record);

    int insertSelective(RPWaterPollutantConcentrationVO record);

    RPWaterPollutantConcentrationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPWaterPollutantConcentrationVO record);

    int updateByPrimaryKey(RPWaterPollutantConcentrationVO record);

    List<Map<String, Object>> getWaterConcentrationListByParam(Map<String, Object> paramMap);
}