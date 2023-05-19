package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.sp.dao.environmentalprotection.licence.*;
import com.tjpu.sp.service.environmentalprotection.licence.RPExecuteSummaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RPExecuteSummaryServiceImpl implements RPExecuteSummaryService {

    private final RPExecuteSummaryMapper rpExecuteSummaryMapper;

    public RPExecuteSummaryServiceImpl(RPExecuteSummaryMapper rpExecuteSummaryMapper) {
        this.rpExecuteSummaryMapper = rpExecuteSummaryMapper;
    }


    /**
     * @Description: 获取参数数据信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/15 10:46
     */
    @Override
    public List<Map<String, Object>> getParamDataListByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getParamDataListByParam(paramMap);
    }

    
    /**
     * @Description: 获取原料信息数据
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/15 11:00
     */ 
    @Override
    public List<Map<String, Object>> getYFLDataListByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getYFLDataListByParam(paramMap);
    }
    
    /**
     * @Description: 燃料信息 
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/15 13:01
     */ 
    @Override
    public List<Map<String, Object>> getRLDataListByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getRLDataListByParam(paramMap);
    }

    /**
     * @Description: 获取废水治理设施信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/15 13:07
     */
    @Override
    public List<Map<String, Object>> getWaterFacilityDataListByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getWaterFacilityDataListByParam(paramMap);
    }
    /**
     * @Description: 获取废气治理设施信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/15 13:07
     */
    @Override
    public List<Map<String, Object>> getGasFacilityDataListByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getGasFacilityDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getZXDataListByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getZXDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getReportRequireByParam(Map<String, Object> paramMap) {
        return rpExecuteSummaryMapper.getReportRequireByParam(paramMap);
    }
}
