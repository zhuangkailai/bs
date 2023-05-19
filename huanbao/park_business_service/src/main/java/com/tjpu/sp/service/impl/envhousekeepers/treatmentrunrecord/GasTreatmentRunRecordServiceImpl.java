package com.tjpu.sp.service.impl.envhousekeepers.treatmentrunrecord;

import com.tjpu.sp.dao.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordMapper;
import com.tjpu.sp.model.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordVO;
import com.tjpu.sp.service.envhousekeepers.treatmentrunrecord.GasTreatmentRunRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class GasTreatmentRunRecordServiceImpl implements GasTreatmentRunRecordService {

    @Autowired
    private GasTreatmentRunRecordMapper gasTreatmentRunRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过自定义参数查询废气治理设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getGasTreatmentRunRecordByParamMap(Map<String,Object> paramMap) {
        return gasTreatmentRunRecordMapper.getGasTreatmentRunRecordByParamMap(paramMap);
    }

    @Override
    public void insert(GasTreatmentRunRecordVO entity) {
        gasTreatmentRunRecordMapper.insert(entity);
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String id) {
        return gasTreatmentRunRecordMapper.getGasTreatmentRunRecordByID(id);
    }

    @Override
    public void updateByPrimaryKey(GasTreatmentRunRecordVO entity) {
        gasTreatmentRunRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        gasTreatmentRunRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getGasTreatmentRunRecordDetailByID(String id) {
       return gasTreatmentRunRecordMapper.getGasTreatmentRunRecordDetailByID(id);
    }
}
