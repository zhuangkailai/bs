package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.SelfMonitorDataInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.SelfMonitorInfoMapper;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorDataInfoVO;
import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;
import com.tjpu.sp.service.envhousekeepers.SelfMonitorDataService;
import com.tjpu.sp.service.envhousekeepers.SelfMonitorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class SelfMonitorDataServiceImpl implements SelfMonitorDataService {
    @Autowired
    private SelfMonitorDataInfoMapper selfMonitorDataInfoMapper;


    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return selfMonitorDataInfoMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void deleteInfoById(String id) {
        selfMonitorDataInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void updateData(SelfMonitorDataInfoVO selfMonitorDataInfoVO) {
        selfMonitorDataInfoMapper.updateByPrimaryKey(selfMonitorDataInfoVO);
    }

    @Override
    public void insertData(SelfMonitorDataInfoVO selfMonitorDataInfoVO) {
        selfMonitorDataInfoMapper.insert(selfMonitorDataInfoVO);
    }


}
