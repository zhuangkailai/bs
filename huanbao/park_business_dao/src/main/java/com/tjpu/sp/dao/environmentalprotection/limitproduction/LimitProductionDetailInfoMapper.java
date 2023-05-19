package com.tjpu.sp.dao.environmentalprotection.limitproduction;

import com.tjpu.sp.model.environmentalprotection.limitproduction.LimitProductionDetailInfoVO;

public interface LimitProductionDetailInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int deleteByLimitProductionID(String LimitProductionID);

    int insert(LimitProductionDetailInfoVO record);

    int insertSelective(LimitProductionDetailInfoVO record);

    LimitProductionDetailInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(LimitProductionDetailInfoVO record);

    int updateByPrimaryKey(LimitProductionDetailInfoVO record);
}