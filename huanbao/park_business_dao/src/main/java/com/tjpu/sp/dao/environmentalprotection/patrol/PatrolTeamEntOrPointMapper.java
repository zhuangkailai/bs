package com.tjpu.sp.dao.environmentalprotection.patrol;

import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamEntOrPointVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PatrolTeamEntOrPointMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PatrolTeamEntOrPointVO record);

    int insertSelective(PatrolTeamEntOrPointVO record);

    PatrolTeamEntOrPointVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PatrolTeamEntOrPointVO record);

    int updateByPrimaryKey(PatrolTeamEntOrPointVO record);

    void deleteByFkId(String pkId);

    List<Map<String, Object>> getEntOrPointDataListById(String id);

    List<Map<String, Object>> getEntOrPointDataList();

    List<Map<String, Object>> getPointDataListByParamMap(JSONObject jsonObject);

    List<Map<String, Object>> getEntDataListByParamMap(JSONObject jsonObject);
}