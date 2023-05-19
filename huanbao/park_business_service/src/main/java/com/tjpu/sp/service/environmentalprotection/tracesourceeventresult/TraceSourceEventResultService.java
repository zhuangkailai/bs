package com.tjpu.sp.service.environmentalprotection.tracesourceeventresult;


import com.tjpu.sp.model.environmentalprotection.tracesourceeventresult.TraceSourceEventResultVO;

import java.util.List;
import java.util.Map;

public interface TraceSourceEventResultService {

    int deleteByPrimaryKey(String pkId);

    int insert(List<TraceSourceEventResultVO> records);

    int update(List<TraceSourceEventResultVO> records);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(TraceSourceEventResultVO record);

    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:37
     * @Description:  通过自定义参数获取溯源事件结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getTraceSourceEventResultByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2021/05/10 0016 下午 2:37
     * @Description:  通过id获取溯源事件结果详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getTraceSourceEventResultDetailByID(String pkid);
}
