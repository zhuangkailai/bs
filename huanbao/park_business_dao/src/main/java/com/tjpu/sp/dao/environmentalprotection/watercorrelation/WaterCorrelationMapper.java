package com.tjpu.sp.dao.environmentalprotection.watercorrelation;

import com.tjpu.sp.model.environmentalprotection.watercorrelation.WaterCorrelationVO;

import java.util.HashMap;
import java.util.List;

public interface WaterCorrelationMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WaterCorrelationVO record);

    int insertSelective(WaterCorrelationVO record);

    WaterCorrelationVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WaterCorrelationVO record);

    int updateByPrimaryKey(WaterCorrelationVO record);

    List<WaterCorrelationVO> selectByParam(HashMap<String, Object> paramMap);
}