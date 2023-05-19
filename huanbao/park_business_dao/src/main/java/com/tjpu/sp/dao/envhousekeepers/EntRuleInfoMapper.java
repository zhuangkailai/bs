package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.EntRuleInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntRuleInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntRuleInfoVO record);

    int insertSelective(EntRuleInfoVO record);

    EntRuleInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntRuleInfoVO record);

    int updateByPrimaryKey(EntRuleInfoVO record);

    List<Map<String, Object>> getEntRuleListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> getAllRuleTypeList();

    List<Map<String, Object>> getRuleTypeNum(Map<String, Object> paramMap);
}