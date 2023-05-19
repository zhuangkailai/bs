package com.tjpu.sp.service.impl.environmentalprotection.productionmaterials;

import com.tjpu.sp.dao.environmentalprotection.productionmaterials.ProductInfoMapper;
import com.tjpu.sp.model.environmentalprotection.productionmaterials.ProductInfoVO;
import com.tjpu.sp.service.environmentalprotection.productionmaterials.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProductInfoServiceImpl implements ProductInfoService {
    @Autowired
    private ProductInfoMapper productInfoMapper;

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:43
     * @Description:根据自定义参数获取生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getProductInfosByParamMap(Map<String, Object> paramMap) {
        return productInfoMapper.getProductInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:43
     * @Description:新增生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(ProductInfoVO obj) {
        productInfoMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:43
     * @Description:修改生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(ProductInfoVO obj) {
        productInfoMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:43
     * @Description:根据主键ID删除生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        productInfoMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:43
     * @Description:根据主键ID获取生产物料产品详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getProductInfoDetailByID(String pkid) {
        return productInfoMapper.getProductInfoDetailByID(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:43
     * @Description:根据id获取生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public ProductInfoVO selectByPrimaryKey(String id) {
        return productInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getProductInfoAndPollutionInfoByParamMap(Map<String, Object> paramMap) {
        return productInfoMapper.getProductInfoAndPollutionInfoByParamMap(paramMap);
    }
}
