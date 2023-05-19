package com.tjpu.sp.dao.environmentalprotection.devopsinfo;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsExplainVO;

import java.util.List;
import java.util.Map;

public interface EntDevOpsExplainMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntDevOpsExplainVO record);

    int insertSelective(EntDevOpsExplainVO record);

    EntDevOpsExplainVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntDevOpsExplainVO record);

    int updateByPrimaryKey(EntDevOpsExplainVO record);

    List<Map<String,Object>> getEntDevOpsExplainsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEntDevOpsExplainDetailByID(String id);

    List<Map<String,Object>> getEntDevOpsExplainsByTimesAndType(Map<String, Object> paramMap);

    List<Map<String,Object>> getOnePointEntDevOpsExplainsByParamMap(Map<String, Object> paramMap);
}