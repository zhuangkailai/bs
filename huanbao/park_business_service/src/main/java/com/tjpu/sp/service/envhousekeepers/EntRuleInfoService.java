package com.tjpu.sp.service.envhousekeepers;


import com.tjpu.sp.model.envhousekeepers.EntRuleInfoVO;

import java.util.List;
import java.util.Map;

public interface EntRuleInfoService {


    void updateInfo(EntRuleInfoVO entRuleInfoVO);

    void insertInfo(EntRuleInfoVO entRuleInfoVO);

    void deleteInfoById(String id);

    List<Map<String, Object>> getEntRuleListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> getAllRuleTypeList();

    List<Map<String, Object>> getRuleTypeNum(Map<String, Object> paramMap);
}
