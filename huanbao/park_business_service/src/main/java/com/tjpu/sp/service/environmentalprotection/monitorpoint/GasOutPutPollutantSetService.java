package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GasOutPutPollutantSetVO;

import java.util.List;
import java.util.Map;

public interface GasOutPutPollutantSetService {


    List<Map<String, Object>> getGasPollutantsByOutputId(Map<String,Object> paramMap);

    List<Map<String, Object>> getGasPollutantByOutputId(Map<String,Object> paramMap);

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
    int insertPollutants(GasOutPutPollutantSetVO record, List<EarlyWarningSetVO> list);

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
    int updatePollutants(GasOutPutPollutantSetVO record, List<EarlyWarningSetVO> list);

    /**
     * @author: chengzq
     * @date: 2019/6/22 0022 下午 7:00
     * @Description: 通过排口id集合查询废气，或废气无组织污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getGasOutPutPollutantSetsByOutputIds(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/2/18 0018 下午 8:09
     * @Description: 通过自定义条件获取废水，废气，雨水，水质的污染物报警类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantSetInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAlarmLevelDataByParam(Map<String, Object> paramMap);


    List<Map<String,Object>> getAllGasPollutantInfo(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutantSetListByParam(Map<String, Object> paramMap);
}
