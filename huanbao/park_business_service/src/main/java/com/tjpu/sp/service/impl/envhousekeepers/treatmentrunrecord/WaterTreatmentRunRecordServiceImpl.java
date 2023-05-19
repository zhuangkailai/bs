package com.tjpu.sp.service.impl.envhousekeepers.treatmentrunrecord;

import com.tjpu.sp.dao.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordMapper;
import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordVO;
import com.tjpu.sp.service.envhousekeepers.treatmentrunrecord.WaterTreatmentRunRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class WaterTreatmentRunRecordServiceImpl implements WaterTreatmentRunRecordService {

    @Autowired
    private WaterTreatmentRunRecordMapper waterTreatmentRunRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过自定义参数查询废水治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterTreatmentRunRecordByParamMap(Map<String,Object> paramMap) {
        return waterTreatmentRunRecordMapper.getWaterTreatmentRunRecordByParamMap(paramMap);
    }

    @Override
    public void insert(WaterTreatmentRunRecordVO entity) {
        waterTreatmentRunRecordMapper.insert(entity);
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String id) {
        return waterTreatmentRunRecordMapper.getWaterTreatmentRunRecordByID(id);
    }

    @Override
    public void updateByPrimaryKey(WaterTreatmentRunRecordVO entity) {
        waterTreatmentRunRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        waterTreatmentRunRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getWaterTreatmentRunRecordDetailByID(String id) {
       return waterTreatmentRunRecordMapper.getWaterTreatmentRunRecordDetailByID(id);
    }
}
