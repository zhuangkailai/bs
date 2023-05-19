package com.tjpu.sp.service.impl.common.knowledge;


import com.tjpu.sp.dao.common.knowledge.ScienceKnowledgeMapper;
import com.tjpu.sp.dao.common.standard.StandardInfoMapper;
import com.tjpu.sp.model.common.knowledge.ScienceKnowledgeVO;
import com.tjpu.sp.model.common.standard.StandardInfoVO;
import com.tjpu.sp.service.common.knowledge.ScienceKnowledgeService;
import com.tjpu.sp.service.common.standard.StandardInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class ScienceKnowledgeServiceImpl implements ScienceKnowledgeService {
    @Autowired
    private ScienceKnowledgeMapper scienceKnowledgeMapper;


    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return scienceKnowledgeMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void updateInfo(ScienceKnowledgeVO scienceKnowledgeVO) {
        scienceKnowledgeMapper.updateByPrimaryKey(scienceKnowledgeVO);
    }

    @Override
    public void insertInfo(ScienceKnowledgeVO scienceKnowledgeVO) {
        scienceKnowledgeMapper.insert(scienceKnowledgeVO);
    }

    @Override
    public void deleteInfoById(String id) {
        scienceKnowledgeMapper.deleteByPrimaryKey(id);
    }



    @Override
    public Map<String, Object> getEditOrDetailsDataById(String id) {
        return scienceKnowledgeMapper.getEditOrDetailsDataById(id);
    }


}
