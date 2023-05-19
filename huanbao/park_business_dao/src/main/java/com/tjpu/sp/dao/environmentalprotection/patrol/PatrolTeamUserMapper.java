package com.tjpu.sp.dao.environmentalprotection.patrol;

import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamUserVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PatrolTeamUserMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PatrolTeamUserVO record);

    int insertSelective(PatrolTeamUserVO record);

    PatrolTeamUserVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PatrolTeamUserVO record);

    int updateByPrimaryKey(PatrolTeamUserVO record);

    void deleteByFkId(String pkId);

    List<Map<String, Object>> getUserIdsById(String id);

    List<Map<String, Object>> getUserDataList();


    List<Map<String, Object>> getUserDataListById(String id);
}