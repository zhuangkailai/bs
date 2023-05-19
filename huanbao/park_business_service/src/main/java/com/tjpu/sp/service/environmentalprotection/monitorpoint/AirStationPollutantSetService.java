package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirStationPollutantSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;

import java.util.List;
import java.util.Map;

public interface AirStationPollutantSetService {


    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:18
     * @Description: 通过监测点id查询空气站相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAirPollutantByParamMap(Map<String,Object> paramMap);
    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 10:18
     * @Description: 通过监测点id查询空气站相关污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getAirPollutantsByParamMap(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 6:13
     * @Description: 新增污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     * @throws:
     */
    int insertPollutants(AirStationPollutantSetVO record, List<EarlyWarningSetVO> list);

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:15
     * @Description: 删除污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     */
    int deletePollutants(String id, Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:17
     * @Description: 修改污染物set数据以及报警set数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, list]
     * @throws:
     */
    int updatePollutants(AirStationPollutantSetVO record, List<EarlyWarningSetVO> list);

    /**
     *
     * @author: lip
     * @date: 2019/6/5 0005 下午 3:41
     * @Description: 获取城市空气质量污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<Map<String,Object>> getCityAirPollutantSetInfo();

    /**
     *
     * @author: xsm
     * @date: 2019/7/8 0008 下午 1:41
     * @Description: 批量新增空气质量污染物设置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void insertAirStationPollutantSets(List<AirStationPollutantSetVO> airlist);
}
