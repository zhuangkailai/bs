package com.tjpu.sp.service.environmentalprotection.monitorstandard;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;

import java.util.List;
import java.util.Map;

public interface MonitorStandardService {

    /**
     * @author: xsm
     * @date: 2019/09/05 0005 下午 2:57
     * @Description:根据自定义参数获取排放标准管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getMonitorStandardListsByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/10 0010 上午 9:04
     * @Description: 获取所有标准
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllStandard();

    List<Map<String,Object>> getMonitorStandardsByParamMap(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getWaterStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getOtherStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getWQStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getGasStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getGasPointStandardDataList(Map<String, Object> paramMap);
}
