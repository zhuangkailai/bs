package com.tjpu.sp.service.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceConfigInfoVO;

import java.util.List;
import java.util.Map;

public interface TraceSourceConfigInfoService {


    List<Map<String, Object>> getTraceSourceConfigDataByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/28 0028 下午 5:01
     * @Description: 修改溯源配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void updateTraceSourceConfigInfo(List<TraceSourceConfigInfoVO> adddata, List<String> delete);

    /**
     * @author: xsm
     * @date: 2019/8/30 0030 上午 9:38
     * @Description: 获取溯源污染物下拉框信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourcePollutantSelectData();

    /**
     * @author: xsm
     * @date: 2019/8/30 0030 上午 10:31
     * @Description: 获取按属性Code分组的溯源配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getTraceSourceConfigInfoGroupByCode(Map<String, Object> paramMap);
}
