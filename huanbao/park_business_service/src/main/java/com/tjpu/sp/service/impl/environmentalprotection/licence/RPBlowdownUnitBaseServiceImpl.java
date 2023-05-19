package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.sp.dao.environmentalprotection.licence.RPBlowdownUnitBaseMapper;
import com.tjpu.sp.service.environmentalprotection.licence.RPBlowdownUnitBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RPBlowdownUnitBaseServiceImpl implements RPBlowdownUnitBaseService {

    private final RPBlowdownUnitBaseMapper rpBlowdownUnitBaseMapper;

    public RPBlowdownUnitBaseServiceImpl(RPBlowdownUnitBaseMapper rpBlowdownUnitBaseMapper) {
        this.rpBlowdownUnitBaseMapper = rpBlowdownUnitBaseMapper;
    }


    @Override
    public List<Map<String, Object>> getBlowdownUnitDataListByParam(Map<String, Object> paramMap) {
        return rpBlowdownUnitBaseMapper.getBlowdownUnitDataListByParam(paramMap);
    }

    /**
     * @Description: 获取原料辅料用量信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/12 10:42
     */
    @Override
    public List<Map<String, Object>> getBlowdownUnitYLDataListByParam(Map<String, Object> paramMap) {
        return rpBlowdownUnitBaseMapper.getBlowdownUnitYLDataListByParam(paramMap);
    }

    
    /**
     * @Description: 获取能源消耗信息
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/12 13:10
     */ 
    @Override
    public List<Map<String, Object>> getBlowdownUnitNYDataListByParam(Map<String, Object> paramMap) {
        return rpBlowdownUnitBaseMapper.getBlowdownUnitNYDataListByParam(paramMap);
    }

    
    /**
     * @Description: 获取投资信息
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/4/12 14:41
     */ 
    @Override
    public List<Map<String, Object>> getBlowdownUnitTZDataListByParam(Map<String, Object> paramMap) {
        return rpBlowdownUnitBaseMapper.getBlowdownUnitTZDataListByParam(paramMap);
    }

    /**
     * @Description: 获取产品产量信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/4/12 14:42
     */
    @Override
    public List<Map<String, Object>> getBlowdownUnitCPDataListByParam(Map<String, Object> paramMap) {
        return rpBlowdownUnitBaseMapper.getBlowdownUnitCPDataListByParam(paramMap);
    }
}
