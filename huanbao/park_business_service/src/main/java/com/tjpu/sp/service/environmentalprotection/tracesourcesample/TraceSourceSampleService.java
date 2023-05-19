package com.tjpu.sp.service.environmentalprotection.tracesourcesample;


import com.tjpu.sp.model.environmentalprotection.tracesourcesample.TraceSourceSampleVO;

import java.util.List;
import java.util.Map;

public interface TraceSourceSampleService {

    int deleteByPrimaryKey(String pkId);

    int insert(TraceSourceSampleVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(TraceSourceSampleVO record);

    /**
     * @author: chengzq
     * @date: 2020/10/21 0016 下午 2:37
     * @Description:  通过自定义参数获取溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getTraceSourceSampleByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/10/21 0016 下午 2:37
     * @Description:  通过id获取溯源样品详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    Map<String,Object> getTraceSourceSampleDetailByID(String pkid);
}
