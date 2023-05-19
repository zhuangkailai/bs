package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.EntRuleInfoMapper;
import com.tjpu.sp.model.envhousekeepers.EntRuleInfoVO;
import com.tjpu.sp.service.envhousekeepers.EntRuleInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntRuleInfoServiceImpl implements EntRuleInfoService {
    @Autowired
    private EntRuleInfoMapper entRuleInfoMapper;


    @Override
    public void updateInfo(EntRuleInfoVO entRuleInfoVO) {
        entRuleInfoMapper.updateByPrimaryKey(entRuleInfoVO);
    }

    @Override
    public void insertInfo(EntRuleInfoVO entRuleInfoVO) {
        entRuleInfoMapper.insert(entRuleInfoVO);
    }

    @Override
    public void deleteInfoById(String id) {
        entRuleInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getEntRuleListDataByParamMap(Map<String, Object> jsonObject) {
        return entRuleInfoMapper.getEntRuleListDataByParamMap(jsonObject);
    }

    @Override
    public List<Map<String, Object>> getAllRuleTypeList() {
        return entRuleInfoMapper.getAllRuleTypeList();
    }

    @Override
    public List<Map<String, Object>> getRuleTypeNum(Map<String, Object> paramMap) {
        return entRuleInfoMapper.getRuleTypeNum(paramMap);
    }
}
