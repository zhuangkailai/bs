package com.tjpu.sp.service.impl.common.standard;


import com.tjpu.sp.dao.common.standard.StandardInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.EntWorkDynamicMapper;
import com.tjpu.sp.model.common.standard.StandardInfoVO;
import com.tjpu.sp.model.envhousekeepers.EntWorkDynamicVO;
import com.tjpu.sp.service.common.standard.StandardInfoService;
import com.tjpu.sp.service.envhousekeepers.EntWorkDynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class StandardInfoServiceImpl implements StandardInfoService {
    @Autowired
    private StandardInfoMapper standardInfoMapper;


    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return standardInfoMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void updateInfo(StandardInfoVO standardInfoVO) {
        standardInfoMapper.updateByPrimaryKey(standardInfoVO);
    }

    @Override
    public void insertInfo(StandardInfoVO standardInfoVO) {
        standardInfoMapper.insert(standardInfoVO);
    }

    @Override
    public void deleteInfoById(String id) {
        standardInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> countKnowledgeData() {
        return standardInfoMapper.countKnowledgeData();
    }

    @Override
    public Map<String, Object> getEditOrDetailsDataById(String id) {
        return standardInfoMapper.getEditOrDetailsDataById(id);
    }


}
