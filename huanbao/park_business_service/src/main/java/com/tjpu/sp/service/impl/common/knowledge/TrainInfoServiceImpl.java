package com.tjpu.sp.service.impl.common.knowledge;


import com.tjpu.sp.dao.common.knowledge.ScienceKnowledgeMapper;
import com.tjpu.sp.dao.common.knowledge.TrainInfoMapper;
import com.tjpu.sp.dao.common.knowledge.TrainUserInfoMapper;
import com.tjpu.sp.model.common.knowledge.ScienceKnowledgeVO;
import com.tjpu.sp.model.common.knowledge.TrainInfoVO;
import com.tjpu.sp.model.common.knowledge.TrainUserInfoVO;
import com.tjpu.sp.service.common.knowledge.ScienceKnowledgeService;
import com.tjpu.sp.service.common.knowledge.TrainInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class TrainInfoServiceImpl implements TrainInfoService {
    @Autowired
    private TrainInfoMapper trainInfoMapper;

    @Autowired
    private TrainUserInfoMapper trainUserInfoMapper;


    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return trainInfoMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public void updateInfo(TrainInfoVO trainInfoVO) {
        trainInfoMapper.updateByPrimaryKey(trainInfoVO);
    }

    @Override
    public void insertInfo(TrainInfoVO trainInfoVO) {
        trainInfoMapper.insert(trainInfoVO);
    }

    @Override
    public void deleteInfoById(String id) {
        trainInfoMapper.deleteByPrimaryKey(id);
    }



    @Override
    public Map<String, Object> getEditDataById(String id) {
        return trainInfoMapper.getEditDataById(id);
    }

    /**
     * @Description: 获取已学习人员信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/9/7 15:14
     */
    @Override
    public List<Map<String, Object>> getStudyUserListById(String id) {
        return trainInfoMapper.getStudyUserListById(id);
    }

    @Override
    public void insertUserInfo(TrainUserInfoVO trainUserInfoVO) {
        trainUserInfoMapper.insert(trainUserInfoVO);
    }


}
