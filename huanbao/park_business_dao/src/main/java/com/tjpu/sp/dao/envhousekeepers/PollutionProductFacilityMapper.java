package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.PollutionProductFacilityVO;
import org.springframework.stereotype.Repository;

@Repository
public interface PollutionProductFacilityMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PollutionProductFacilityVO record);

    int insertSelective(PollutionProductFacilityVO record);

    PollutionProductFacilityVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PollutionProductFacilityVO record);

    int updateByPrimaryKey(PollutionProductFacilityVO record);
}