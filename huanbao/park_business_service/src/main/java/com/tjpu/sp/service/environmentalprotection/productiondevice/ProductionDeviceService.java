package com.tjpu.sp.service.environmentalprotection.productiondevice;


import com.tjpu.sp.model.environmentalprotection.productiondevice.ProductionDeviceVO;

import java.util.List;
import java.util.Map;

public interface ProductionDeviceService {

    int deleteByPrimaryKey(String pkId);

    int insert(List<ProductionDeviceVO> record);

    Map<String, Object> selectByPrimaryKey(String pkId);

    int update(ProductionDeviceVO record ,List<ProductionDeviceVO> list);

    int updateByPrimaryKey(ProductionDeviceVO record);

    /**
     * @author: chengzq
     * @date: 2019/11/01 0016 下午 2:37
     * @Description:  通过自定义参数获取生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getProductionDeviceByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/11/01 0016 下午 2:37
     * @Description:  通过id获取生产装置设备详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String, Object> getProductionDeviceDetailByID(String pkid);

    Map<String,Object> getProductionDeviceInfoByID(String id);

    List<Map<String,Object>> getProductDeviceAndPollutionInfoByParamMap(Map<String, Object> paramMap);


}
