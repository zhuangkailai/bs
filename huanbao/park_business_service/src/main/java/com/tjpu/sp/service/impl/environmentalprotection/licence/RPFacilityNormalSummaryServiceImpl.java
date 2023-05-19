package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.sp.dao.environmentalprotection.licence.RPFacilityNormalSummaryMapper;
import com.tjpu.sp.service.environmentalprotection.licence.RPFacilityNormalSummaryService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RPFacilityNormalSummaryServiceImpl implements RPFacilityNormalSummaryService {

    private final RPFacilityNormalSummaryMapper rpFacilityNormalSummaryMapper;

    public RPFacilityNormalSummaryServiceImpl(RPFacilityNormalSummaryMapper rpFacilityNormalSummaryMapper) {
        this.rpFacilityNormalSummaryMapper = rpFacilityNormalSummaryMapper;
    }


    @Override
    public List<Map<String, Object>> getNormalDataListByParam(Map<String, Object> paramMap) {
        return rpFacilityNormalSummaryMapper.getNormalDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getSpecialTimeGasPollutantByParam(Map<String, Object> paramMap) {
        return rpFacilityNormalSummaryMapper.getSpecialTimeGasPollutantByParam(paramMap);
    }
}
