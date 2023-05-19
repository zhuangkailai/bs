package com.tjpu.sp.service.environmentalprotection.patrol;


import com.tjpu.sp.model.environmentalprotection.patrol.PatrolVO;

import java.util.List;
import java.util.Map;

public interface PatrolService {

    List<Map<String,Object>> getPatrolsByParamMap(Map<String, Object> paramMap);

    void insert(PatrolVO patrolVO);

    PatrolVO selectByPrimaryKey(String id);

    void updateByPrimaryKey(PatrolVO patrolVO);

    void deleteByPrimaryKey(String id);

    Map<String,Object> getPatrolDetailByID(String id);
}
