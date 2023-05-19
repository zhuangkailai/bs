package com.tjpu.sp.service.impl.environmentalprotection.productionmaterials;

import com.tjpu.sp.dao.environmentalprotection.productionmaterials.FuelInfoMapper;
import com.tjpu.sp.model.environmentalprotection.productionmaterials.FuelInfoVO;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.FuelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FuelInfoServiceImpl implements FuelInfoService {
    @Autowired
    private FuelInfoMapper fuelInfoMapper;

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:19
     * @Description:根据自定义参数获取主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getFuelInfosByParamMap(Map<String, Object> paramMap) {
        return fuelInfoMapper.getFuelInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:19
     * @Description:新增主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(FuelInfoVO obj) {
        fuelInfoMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:19
     * @Description:修改主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(FuelInfoVO obj) {
        fuelInfoMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:19
     * @Description:根据主键ID删除主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        fuelInfoMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:19
     * @Description:根据主键ID获取主要原料及辅料详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getFuelInfoDetailByID(String pkid) {
        return fuelInfoMapper.getFuelInfoDetailByID(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:19
     * @Description:根据id获取主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public FuelInfoVO selectByPrimaryKey(String id) {
        return fuelInfoMapper.selectByPrimaryKey(id);
    }
}
