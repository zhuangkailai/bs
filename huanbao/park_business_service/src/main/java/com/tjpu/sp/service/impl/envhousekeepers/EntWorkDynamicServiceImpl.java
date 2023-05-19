package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.EntWorkDynamicMapper;

import com.tjpu.sp.model.envhousekeepers.EntWorkDynamicVO;
import com.tjpu.sp.service.envhousekeepers.EntWorkDynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntWorkDynamicServiceImpl implements EntWorkDynamicService {
    @Autowired
    private EntWorkDynamicMapper entWorkDynamicMapper;


    @Override
    public void updateInfo(EntWorkDynamicVO entWorkDynamicVO) {
        entWorkDynamicMapper.updateByPrimaryKey(entWorkDynamicVO);
    }

    @Override
    public void insertInfo(EntWorkDynamicVO entWorkDynamicVO) {
        entWorkDynamicMapper.insert(entWorkDynamicVO);
    }

    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return entWorkDynamicMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void deleteInfoById(String id) {
        entWorkDynamicMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getEditOrDetailsDataById(String id) {
        return entWorkDynamicMapper.getEditOrDetailsDataById(id);
    }
}
