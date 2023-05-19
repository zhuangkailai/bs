package com.tjpu.sp.dao.environmentalprotection.devopsinfo;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsPersonnelVO;

import java.util.List;
import java.util.Map;

public interface DevOpsPersonnelMapper {
    int deleteByPrimaryKey(String pkPersonnelid);

    int insert(DevOpsPersonnelVO record);

    int insertSelective(DevOpsPersonnelVO record);

    DevOpsPersonnelVO selectByPrimaryKey(String pkPersonnelid);

    int updateByPrimaryKeySelective(DevOpsPersonnelVO record);

    int updateByPrimaryKey(DevOpsPersonnelVO record);

    List<Map<String,Object>> countDevOpsPersonneNumGropuByUnitByParam(Map<String, Object> param);

    void deleteByDevOpsUnitID(String id);

    List<Map<String,Object>> getDevOpsPersonnelListDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getDevOpsPersonnelDetailByID(String id);

    Map<String,Object> getDevOpsPersonnelByID(String personnelid);
}