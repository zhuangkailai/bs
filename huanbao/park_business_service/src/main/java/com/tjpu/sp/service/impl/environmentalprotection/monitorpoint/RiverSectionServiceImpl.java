package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GroundWaterMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.RiverSectionMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GroundWaterVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GroundWaterService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.RiverSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RiverSectionServiceImpl implements RiverSectionService {
    @Autowired
    private RiverSectionMapper riverSectionMapper;
    @Override
    public List<Map<String, Object>> getRiverSectionPointListByParam(Map<String, Object> paramMap) {
        return riverSectionMapper.getRiverSectionPointListByParam(paramMap);
    }

    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return riverSectionMapper.countTotalByParam(paramMap);
    }
}
