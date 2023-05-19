package com.tjpu.sp.service.environmentalprotection.useelectricfacility;


import com.tjpu.sp.model.environmentalprotection.useelectricfacility.UseElectricFacilityVO;

import java.util.List;
import java.util.Map;

public interface UseElectricFacilityService {

    int deleteByPrimaryKey(String pkId);

    int insert(UseElectricFacilityVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(UseElectricFacilityVO record);

    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:37
     * @Description:  通过自定义参数获取用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getUseElectricFacilityByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/06/18 0016 下午 2:37
     * @Description:  通过id获取用电设施详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getUseElectricFacilityDetailByID(String pkid);


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 9:06
     * @Description:  通过自定义参数获取用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> countUseElectricFacilityInfo(Map<String, Object> paramMap);



    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 10:31
     * @Description: 通过自定义条件查询用电设施信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getUseElectricFacilityAndDGIMNByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:44
     * @Description: 获取企业用电设施统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getEntFacilityCountData(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2020/6/19 0019 下午 2:51
     * @Description: 获取用电设施树形结构数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getFacilityTreeDataByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/7/2 0002 下午 5:11
     * @Description: 通过自定义条件统计企业和设施及监测点数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutionAndFacilityInfoParams(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/7/8 0008 上午 11:51
     * @Description: 获取所有关联用电设施的企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getAllPollutionInfo(Map<String, Object> paramMap);



    List<Map<String,Object>> getPollutionAndFacilityInfoParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getUseElectricAndOutputByParamMap(Map<String, Object> paramMap);



}
