package com.tjpu.sp.service.impl.environmentalprotection.entevaluation;

import com.tjpu.sp.dao.environmentalprotection.entevaluation.EntSynthesizeEvaluationMapper;
import com.tjpu.sp.service.environmentalprotection.entevaluation.EntEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntEvaluationServiceImpl implements EntEvaluationService {
    @Autowired
    private EntSynthesizeEvaluationMapper entSynthesizeEvaluationMapper;

    @Override
    public List<Map<String, Object>> getEntLastTwoEvaluationData() {
        return entSynthesizeEvaluationMapper.getEntLastTwoEvaluationData();
    }
}
