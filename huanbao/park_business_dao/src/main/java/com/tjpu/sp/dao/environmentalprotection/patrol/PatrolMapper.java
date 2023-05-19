package com.tjpu.sp.dao.environmentalprotection.patrol;

import com.tjpu.sp.model.environmentalprotection.patrol.PatrolVO;

import java.util.List;
import java.util.Map;

public interface PatrolMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PatrolVO record);

    int insertSelective(PatrolVO record);

    PatrolVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PatrolVO record);

    int updateByPrimaryKey(PatrolVO record);

    List<Map<String,Object>> getPatrolsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getPatrolDetailByID(String id);
}