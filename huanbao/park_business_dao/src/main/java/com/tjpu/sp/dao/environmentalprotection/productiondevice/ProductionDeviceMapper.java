package com.tjpu.sp.dao.environmentalprotection.productiondevice;

import com.tjpu.sp.model.environmentalprotection.productiondevice.ProductionDeviceVO;

import java.util.List;
import java.util.Map;

public interface ProductionDeviceMapper {
    int deleteByPrimaryKey(String pkId);

    int deleteByParentid(String parentid);

    int insert(ProductionDeviceVO record);

    Map<String, Object> selectByPrimaryKey(String pkId);

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

    List<Map<String,Object>> getProductionByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getProductionDeviceByPollutionid(Map<String, Object> paramMap);

    Map<String,Object> getProductionDeviceInfoByID(String id);

    Map<String,Object> getProductionDeviceDetailById(Map<String, Object> paramMap);

    List<Map<String,Object>> getProductDeviceAndPollutionInfoByParamMap(Map<String, Object> paramMap);
}