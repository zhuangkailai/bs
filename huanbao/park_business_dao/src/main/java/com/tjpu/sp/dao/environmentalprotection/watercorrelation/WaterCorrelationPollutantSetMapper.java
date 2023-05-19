package com.tjpu.sp.dao.environmentalprotection.watercorrelation;

import com.tjpu.sp.model.environmentalprotection.watercorrelation.WaterCorrelationPollutantSetVO;

import java.util.List;
import java.util.Map;

public interface WaterCorrelationPollutantSetMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WaterCorrelationPollutantSetVO record);

    int insertSelective(WaterCorrelationPollutantSetVO record);

    WaterCorrelationPollutantSetVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WaterCorrelationPollutantSetVO record);

    int updateByPrimaryKey(WaterCorrelationPollutantSetVO record);

    List<WaterCorrelationPollutantSetVO> selectByParam(Map<String,Object> param);
}