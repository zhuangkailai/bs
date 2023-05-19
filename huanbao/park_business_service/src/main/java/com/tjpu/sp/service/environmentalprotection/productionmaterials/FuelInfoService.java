package com.tjpu.sp.service.environmentalprotection.productionmaterials;


import com.tjpu.sp.model.environmentalprotection.productionmaterials.FuelInfoVO;

import java.util.List;
import java.util.Map;

public interface FuelInfoService {

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 下午 1:29
     * @Description:根据自定义参数获取企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getFuelInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 下午 1:29
     * @Description:新增企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(FuelInfoVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 下午 1:29
     * @Description:修改企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(FuelInfoVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 下午 1:29
     * @Description:根据主键ID删除企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 下午 1:29
     * @Description:根据主键ID获取企业燃料详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getFuelInfoDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 下午 1:29
     * @Description:根据id获取企业燃料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    FuelInfoVO selectByPrimaryKey(String id);
}
