package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutPutPollutantSetVO;

import java.util.List;
import java.util.Map;

public interface WaterOutPutPollutantSetService {


    List<Map<String, Object>> getWaterOrRainPollutantsByParamMap( Map<String,Object> paramMap);




    /**
     * @author: chengzq
     * @date: 2019/5/28 0028 上午 11:56
     * @Description: 通过自定义参数获取废水排口污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantByParamMap( Map<String,Object> paramMap);

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
    int insertPollutants(WaterOutPutPollutantSetVO record, List<EarlyWarningSetVO> list);

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
    int updatePollutants(WaterOutPutPollutantSetVO record, List<EarlyWarningSetVO> list);


    List<Map<String,Object>> getAllWaterPollutantInfo(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetListByParam(Map<String, Object> paramMap);
}
