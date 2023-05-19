package com.tjpu.sp.service.impl.envhousekeepers.fuelconsumption;


import com.tjpu.sp.dao.envhousekeepers.fuelconsumption.FuelConsumptionRecordMapper;
import com.tjpu.sp.model.envhousekeepers.fuelconsumption.FuelConsumptionRecordVO;
import com.tjpu.sp.service.envhousekeepers.fuelconsumption.FuelConsumptionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class FuelConsumptionRecordServiceImpl implements FuelConsumptionRecordService {

    @Autowired
    private FuelConsumptionRecordMapper fuelConsumptionRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:11
     * @Description: 通过自定义参数查询废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getFuelConsumptionRecordByParamMap(Map<String,Object> paramMap) {
        return fuelConsumptionRecordMapper.getFuelConsumptionRecordByParamMap(paramMap);
    }

    @Override
    public void insert(FuelConsumptionRecordVO entity) {
        fuelConsumptionRecordMapper.insert(entity);
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String id) {
        return fuelConsumptionRecordMapper.getFuelConsumptionRecordDetailByID(id);
    }

    @Override
    public void updateByPrimaryKey(FuelConsumptionRecordVO entity) {
        fuelConsumptionRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        fuelConsumptionRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getFuelConsumptionRecordDetailByID(String id) {
       return fuelConsumptionRecordMapper.getFuelConsumptionRecordDetailByID(id);
    }
}
