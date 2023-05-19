package com.tjpu.sp.dao.envhousekeepers.focusconcernentset;

import com.tjpu.sp.model.envhousekeepers.focusconcernentset.FocusConcernEntSetVO;

import java.util.List;
import java.util.Map;

public interface FocusConcernEntSetMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(FocusConcernEntSetVO record);

    int insertSelective(FocusConcernEntSetVO record);

    FocusConcernEntSetVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(FocusConcernEntSetVO record);

    int updateByPrimaryKey(FocusConcernEntSetVO record);

    List<Map<String,Object>> getFocusConcernEntSetsByParamMap(Map<String, Object> param);

    Map<String,Object> selectByPollutionid(String pollutionid);
}