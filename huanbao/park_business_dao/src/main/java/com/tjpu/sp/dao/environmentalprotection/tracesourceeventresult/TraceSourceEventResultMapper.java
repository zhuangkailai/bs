package com.tjpu.sp.dao.environmentalprotection.tracesourceeventresult;

import com.tjpu.sp.model.environmentalprotection.tracesourceeventresult.TraceSourceEventResultVO;

import java.util.List;
import java.util.Map;

public interface TraceSourceEventResultMapper {
    int deleteByPrimaryKey(String pkId);
    int deleteByTraceSourceEventid(String fktracesourceeventid);

    int insert(TraceSourceEventResultVO record);

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

}