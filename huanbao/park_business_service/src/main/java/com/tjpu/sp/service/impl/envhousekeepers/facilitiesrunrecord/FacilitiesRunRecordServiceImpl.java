package com.tjpu.sp.service.impl.envhousekeepers.facilitiesrunrecord;

import com.tjpu.sp.dao.envhousekeepers.facilitiesrunrecord.ProductionFacilitiesRunRecordMapper;
import com.tjpu.sp.model.envhousekeepers.facilitiesrunrecord.ProductionFacilitiesRunRecordVO;
import com.tjpu.sp.service.envhousekeepers.facilitiesrunrecord.FacilitiesRunRecordService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public class FacilitiesRunRecordServiceImpl implements FacilitiesRunRecordService {

    @Autowired
    private ProductionFacilitiesRunRecordMapper productionFacilitiesRunRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/17 0017 上午 11:58
     * @Description: 通过自定义参数查询生产设施运行记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getFacilitiesRunRecordByParamMap(Map<String,Object> paramMap) {
        return productionFacilitiesRunRecordMapper.getFacilitiesRunRecordByParamMap(paramMap);
    }

    @Override
    public void insert(ProductionFacilitiesRunRecordVO entity) {
        productionFacilitiesRunRecordMapper.insert(entity);
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String id) {
        return productionFacilitiesRunRecordMapper.getFacilitiesRunRecordDetailByID(id);
    }

    @Override
    public void updateByPrimaryKey(ProductionFacilitiesRunRecordVO entity) {
        productionFacilitiesRunRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        productionFacilitiesRunRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getFacilitiesRunRecordDetailByID(String id) {
       return productionFacilitiesRunRecordMapper.getFacilitiesRunRecordDetailByID(id);
    }
}
