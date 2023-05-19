package com.tjpu.sp.service.environmentalprotection.petition;


import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;

import java.util.List;
import java.util.Map;

public interface PetitionInfoService {

    int insert(PetitionInfoVO record);
    int updateByPrimaryKey(PetitionInfoVO record);
    int deleteByPrimaryKey(String pkId);
    PetitionInfoVO selectByPrimaryKey(String pkId);

    /**
     * @author: xsm
     * @date: 2019/7/25 0025 下午 4:39
     * @Description: 根据监测时间和恶臭点位MN号获取投诉信息及恶臭在线信息的列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String, Object> getPetitionAndStenchOnlineListDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/7/25 0025 下午 4:45
     * @Description: 根据恶臭监测点MN号获取污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getAllStenchPollutantsByDgimns(List<String> dgimns);

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 下午 7:20
     * @Description: 根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String, Object>> getStenchOnlineDataAndWeatherDataByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/9/3 0003 上午 10:22
     * @Description: 自定义查询条件获取投诉数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getPetitionDataByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 下午 5:06
     * @Description: 获取投诉任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPetitionDetailById(Map<String, Object> paramMap);
}
