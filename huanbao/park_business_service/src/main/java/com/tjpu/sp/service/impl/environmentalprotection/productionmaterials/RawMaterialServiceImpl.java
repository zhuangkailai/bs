package com.tjpu.sp.service.impl.environmentalprotection.productionmaterials;

import com.tjpu.sp.dao.environmentalprotection.productionmaterials.RawMaterialMapper;
import com.tjpu.sp.model.environmentalprotection.productionmaterials.RawMaterialVO;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.RawMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RawMaterialServiceImpl implements RawMaterialService {
    @Autowired
    private RawMaterialMapper rawMaterialMapper;

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
    public List<Map<String, Object>> getRawMaterialsByParamMap(Map<String, Object> paramMap) {
        return rawMaterialMapper.getRawMaterialsByParamMap(paramMap);
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
    public void insert(RawMaterialVO obj) {
        rawMaterialMapper.insert(obj);
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
    public void updateByPrimaryKey(RawMaterialVO obj) {
        rawMaterialMapper.updateByPrimaryKey(obj);
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
        rawMaterialMapper.deleteByPrimaryKey(id);
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
    public Map<String, Object> getRawMaterialDetailByID(String pkid) {
        return rawMaterialMapper.getRawMaterialDetailByID(pkid);
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
    public RawMaterialVO selectByPrimaryKey(String id) {
        return rawMaterialMapper.selectByPrimaryKey(id);
    }
}
