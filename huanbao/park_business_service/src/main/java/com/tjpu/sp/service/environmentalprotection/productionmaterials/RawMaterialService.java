package com.tjpu.sp.service.environmentalprotection.productionmaterials;


import com.tjpu.sp.model.environmentalprotection.productionmaterials.RawMaterialVO;

import java.util.List;
import java.util.Map;

public interface RawMaterialService {

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:15
     * @Description:根据自定义参数获取企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getRawMaterialsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:15
     * @Description:新增企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(RawMaterialVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:15
     * @Description:修改企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(RawMaterialVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:15
     * @Description:根据主键ID删除企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:15
     * @Description:根据主键ID获取企业主要原料及辅料详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getRawMaterialDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/18 0018 上午 10:15
     * @Description:根据id获取企业主要原料及辅料信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    RawMaterialVO selectByPrimaryKey(String id);
}
