package com.tjpu.sp.dao.environmentalprotection.patrol;

import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PatrolTeamMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PatrolTeamVO record);

    int insertSelective(PatrolTeamVO record);

    PatrolTeamVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(PatrolTeamVO record);

    int updateByPrimaryKey(PatrolTeamVO record);

    List<Map<String, Object>> getDataListByParamMap(JSONObject jsonObject);

    Map<String, Object> getDetailOrEditById(String id);

    Map<String, Object> getDataMapByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getTeamDataList();

    List<Map<String,Object>> getOverReviewerUserTreeData(Map<String, Object> param);
}