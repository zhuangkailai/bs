package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.SelfMonitorInfoMapper;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;
import com.tjpu.sp.service.envhousekeepers.SelfMonitorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class SelfMonitorInfoServiceImpl implements SelfMonitorInfoService {
    @Autowired
    private SelfMonitorInfoMapper selfMonitorInfoMapper;


    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return selfMonitorInfoMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void deleteInfoById(String id) {
        selfMonitorInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void updateData(SelfMonitorInfoVO selfMonitorInfoVO) {
        selfMonitorInfoMapper.updateByPrimaryKey(selfMonitorInfoVO);
    }

    @Override
    public void insertData(SelfMonitorInfoVO selfMonitorInfoVO) {
        selfMonitorInfoMapper.insert(selfMonitorInfoVO);
    }

    @Override
    public List<Map<String, Object>> getOutPutByParam(Map<String, Object> paramMap) {
        return selfMonitorInfoMapper.getOutPutByParam(paramMap);
    }

    @Override
    public List<String> getMonitorContentByPollutionId(Map<String, Object> paramMap) {
        return selfMonitorInfoMapper.getMonitorContentByPollutionId(paramMap);
    }
}
