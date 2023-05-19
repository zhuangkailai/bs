package com.tjpu.sp.service.impl.common.emergency;


import com.tjpu.sp.dao.common.emergency.EmergencyCaseInfoMapper;
import com.tjpu.sp.dao.common.standard.StandardInfoMapper;
import com.tjpu.sp.model.common.emergency.EmergencyCaseInfoVO;
import com.tjpu.sp.model.common.standard.StandardInfoVO;
import com.tjpu.sp.service.common.emergency.EmergencyCaseInfoService;
import com.tjpu.sp.service.common.standard.StandardInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EmergencyCaseInfoServiceImpl implements EmergencyCaseInfoService {
    @Autowired
    private EmergencyCaseInfoMapper emergencyCaseInfoMapper;


    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return emergencyCaseInfoMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void updateInfo(EmergencyCaseInfoVO emergencyCaseInfoVO) {
        emergencyCaseInfoMapper.updateByPrimaryKey(emergencyCaseInfoVO);
    }

    @Override
    public void insertInfo(EmergencyCaseInfoVO emergencyCaseInfoVO) {
        emergencyCaseInfoMapper.insert(emergencyCaseInfoVO);
    }

    @Override
    public void deleteInfoById(String id) {
        emergencyCaseInfoMapper.deleteByPrimaryKey(id);
    }



    @Override
    public Map<String, Object> getEditOrDetailsDataById(String id) {
        return emergencyCaseInfoMapper.getEditOrDetailsDataById(id);
    }


}
