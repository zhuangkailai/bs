package com.tjpu.sp.dao.environmentalprotection.devopsinfo;

import com.tjpu.sp.model.environmentalprotection.devopsinfo.DevOpsUnitInfoVO;

import java.util.List;
import java.util.Map;

public interface DevOpsUnitInfoMapper {
    int deleteByPrimaryKey(String pkDevopsunitid);

    int insert(DevOpsUnitInfoVO record);

    int insertSelective(DevOpsUnitInfoVO record);

    DevOpsUnitInfoVO selectByPrimaryKey(String pkDevopsunitid);

    int updateByPrimaryKeySelective(DevOpsUnitInfoVO record);

    int updateByPrimaryKey(DevOpsUnitInfoVO record);

    List<Map<String,Object>> getDevOpsUnitInfoListDataByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getDevOpsUnitInfoDetailById(String id);
}