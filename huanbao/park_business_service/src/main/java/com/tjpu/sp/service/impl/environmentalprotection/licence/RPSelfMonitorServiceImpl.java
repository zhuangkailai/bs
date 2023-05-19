package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.sp.dao.environmentalprotection.licence.*;
import com.tjpu.sp.service.environmentalprotection.licence.RPSelfMonitorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RPSelfMonitorServiceImpl implements RPSelfMonitorService {

    private final RPGasPollutantConcentrationMapper rpGasPollutantConcentrationMapper;
    private final RPGasPollutantSpeedMapper rpGasPollutantSpeedMapper;
    private final RPUnGasPollutantConcentrationMapper rpUnGasPollutantConcentrationMapper;
    private final RPWaterPollutantConcentrationMapper rpWaterPollutantConcentrationMapper;
    private final RPExceptionUnGasPollutantConcentrationMapper rpExceptionUnGasPollutantConcentrationMapper;
    private final RPExceptionGasPollutantConcentrationMapper rpExceptionGasPollutantConcentrationMapper;
    private final RPSpecialGasPollutantConcentrationMapper rpSpecialGasPollutantConcentrationMapper;


    public RPSelfMonitorServiceImpl(RPGasPollutantConcentrationMapper rpGasPollutantConcentrationMapper, RPGasPollutantSpeedMapper rpGasPollutantSpeedMapper, RPUnGasPollutantConcentrationMapper rpUnGasPollutantConcentrationMapper, RPWaterPollutantConcentrationMapper rpWaterPollutantConcentrationMapper, RPExceptionUnGasPollutantConcentrationMapper rpExceptionUnGasPollutantConcentrationMapper, RPExceptionGasPollutantConcentrationMapper rpExceptionGasPollutantConcentrationMapper, RPSpecialGasPollutantConcentrationMapper rpSpecialGasPollutantConcentrationMapper) {
        this.rpGasPollutantConcentrationMapper = rpGasPollutantConcentrationMapper;
        this.rpGasPollutantSpeedMapper = rpGasPollutantSpeedMapper;
        this.rpUnGasPollutantConcentrationMapper = rpUnGasPollutantConcentrationMapper;
        this.rpWaterPollutantConcentrationMapper = rpWaterPollutantConcentrationMapper;
        this.rpExceptionUnGasPollutantConcentrationMapper = rpExceptionUnGasPollutantConcentrationMapper;
        this.rpExceptionGasPollutantConcentrationMapper = rpExceptionGasPollutantConcentrationMapper;
        this.rpSpecialGasPollutantConcentrationMapper = rpSpecialGasPollutantConcentrationMapper;
    }
    
    /**
     * @Description: 获取废气有组织浓度统计数据
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/13 9:53
     */ 
    @Override
    public List<Map<String, Object>> getGasConcentrationListByParam(Map<String, Object> paramMap) {
        return rpGasPollutantConcentrationMapper.getGasConcentrationListByParam(paramMap);
    }

    /**
     * @Description: 获取废气有组织速率统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/13 10:12
     */
    @Override
    public List<Map<String, Object>> getGasSpeedListByParam(Map<String, Object> paramMap) {
        return rpGasPollutantSpeedMapper.getGasSpeedListByParam(paramMap);
    }

    /**
     * @Description: 获取无组织浓度统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/13 10:30
     */
    @Override
    public List<Map<String, Object>> getUnGasConcentrationListByParam(Map<String, Object> paramMap) {
        return rpUnGasPollutantConcentrationMapper.getUnGasConcentrationListByParam(paramMap);
    }

    /**
     * @Description: 获取废气浓度统计数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/13 10:31
     */
    @Override
    public List<Map<String, Object>> getWaterConcentrationListByParam(Map<String, Object> paramMap) {
        return rpWaterPollutantConcentrationMapper.getWaterConcentrationListByParam(paramMap);
    }

    
    /**
     * @Description: 获取非正常工况下废气浓度数据 
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/13 13:32
     */ 
    @Override
    public List<Map<String, Object>> getExceptionGasConcentrationListByParam(Map<String, Object> paramMap) {
        return rpExceptionGasPollutantConcentrationMapper.getExceptionGasConcentrationListByParam(paramMap);
    }
    
    /**
     * @Description: 获取非正常工况下无组织废气浓度数据
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/13 13:32
     */ 
    @Override
    public List<Map<String, Object>> getExceptionUnGasConcentrationListByParam(Map<String, Object> paramMap) {
        return rpExceptionUnGasPollutantConcentrationMapper.getExceptionUnGasConcentrationListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getSpecialGasConcentrationListByParam(Map<String, Object> paramMap) {
        return rpSpecialGasPollutantConcentrationMapper.getSpecialGasConcentrationListByParam(paramMap);
    }
}
