package com.tjpu.sp.service.impl.environmentalprotection.patrol;


import com.tjpu.sp.dao.environmentalprotection.patrol.PatrolMapper;
import com.tjpu.sp.model.environmentalprotection.patrol.PatrolVO;
import com.tjpu.sp.service.environmentalprotection.patrol.PatrolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class PatrolServiceImpl implements PatrolService {
    @Autowired
    private PatrolMapper patrolMapper;


    @Override
    public List<Map<String, Object>> getPatrolsByParamMap(Map<String, Object> paramMap) {
        return patrolMapper.getPatrolsByParamMap(paramMap);
    }

    @Override
    public void insert(PatrolVO patrolVO) {
        patrolMapper.insert(patrolVO);
    }

    @Override
    public PatrolVO selectByPrimaryKey(String id) {
        return patrolMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKey(PatrolVO patrolVO) {
        patrolMapper.updateByPrimaryKey(patrolVO);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        patrolMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getPatrolDetailByID(String id) {
        return patrolMapper.getPatrolDetailByID(id);
    }
}
