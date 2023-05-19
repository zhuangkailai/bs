package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationVO;

import java.util.List;
import java.util.Map;

public interface WaterStationService {



    /**
     * @author: chengzq
     * @date: 2019/9/18 0018 下午 4:43
     * @Description: 动态条件获取水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getOnlineWaterStationInfoByParamMap(Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 上午 11:51
     * @Description: 获取所有水质监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getWaterStationByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 13:23
    *@Description: 根据监测点名称和MN号获取新增的那条水质站点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [params]
    *@throws:
    **/
    Map<String,Object> selectWaterStationInfoByPointNameAndDgimn(Map<String, Object> params);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 14:12
    *@Description: 根据监测点ID获取该监测点在线监测设备基础信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [parammap]
    *@throws:
    **/
    List<Map<String,Object>> getWaterStationDeviceStatusByID(Map<String, Object> parammap);
    /**
    *@author: liyc
    *@date: 2019/11/4 0004 14:41
    *@Description: 根据监测点ID获取附件表对应关系
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [parammap]
    *@throws:
    **/
    List<String> getfileIdsByID(Map<String, Object> parammap);

    /**
     *@author: xsm
     *@date: 2019/11/14 0014 14:56
     *@Description: 获取所有水质监测点信息和状态
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    List<Map<String,Object>> getAllWaterStationAndStatusInfo();
    /**
    *@author: liyc
    *@date: 2019/11/18 0018 10:17
    *@Description: 通过主键ID获取水质站点信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pkid]
    *@throws:
    **/
    WaterStationVO getWaterStationByID(String pkid);

    Map<String, Object> getAllWaterStationByType(Map<String, Object> paramMap);

    long countTotalByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllWaterStationInfoByParamMap(Map<String, Object> paramMap);
}
