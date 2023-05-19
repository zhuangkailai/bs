package com.tjpu.sp.service.environmentalprotection.productionmaterials;


import com.tjpu.sp.model.environmentalprotection.productionmaterials.ProductInfoVO;

import java.util.List;
import java.util.Map;

public interface ProductInfoService {

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:40
     * @Description:根据自定义参数获取生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getProductInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:40
     * @Description:新增生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(ProductInfoVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:40
     * @Description:修改生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(ProductInfoVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:40
     * @Description:根据主键ID删除生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:40
     * @Description:根据主键ID获取生产物料产品详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getProductInfoDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/23 0023 上午 8:40
     * @Description:根据id获取生产物料产品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    ProductInfoVO selectByPrimaryKey(String id);

    List<Map<String,Object>> getProductInfoAndPollutionInfoByParamMap(Map<String, Object> paramMap);

}
