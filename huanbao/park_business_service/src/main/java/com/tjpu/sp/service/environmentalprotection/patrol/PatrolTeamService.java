package com.tjpu.sp.service.environmentalprotection.patrol;


import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamEntOrPointVO;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolTeamVO;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface PatrolTeamService {


    void updateData(PatrolTeamVO obj);

    void insertData(PatrolTeamVO obj);

    void deleteByPrimaryKey(String id);

    Map<String, Object> getDetailOrEditById(String id);

    List<Map<String, Object>> getUserIdsById(String id);

    List<Map<String, Object>> getEntOrPointDataListById(String id);

    List<Map<String, Object>> getDataListByParamMap(JSONObject jsonObject);

    Map<String, Object> getDataMapByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPointDataListByParamMap(JSONObject jsonObject);

    void updatePointData(PatrolTeamEntOrPointVO obj);

    void insertPointData(PatrolTeamEntOrPointVO obj);

    void deletePointDataById(String id);

    List<Map<String, Object>> getEntDataListByParamMap(JSONObject jsonObject);

    List<Map<String, Object>> getTeamDataList();


    List<Map<String, Object>> getUserDataListById(String id);

    List<Map<String,Object>> getOverReviewerUserTreeData(Map<String, Object> param);
}
