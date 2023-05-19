package com.tjpu.sp.dao.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceEventInfoVO;

import java.util.List;
import java.util.Map;

public interface TraceSourceEventInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TraceSourceEventInfoVO record);

    int insertSelective(TraceSourceEventInfoVO record);

    TraceSourceEventInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TraceSourceEventInfoVO record);

    int updateByPrimaryKey(TraceSourceEventInfoVO record);
    int updateByParamMap(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 上午 10:32
     * @Description: 通过自定义条件查询溯源事件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<TraceSourceEventInfoVO> getTraceSourceEventInfoByParamMap(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 2:16
     * @Description: 通过自定义参数获取污染事件及污染事件详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<TraceSourceEventInfoVO> getTraceSourceEventAndDetailByParamMap(Map<String,Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/9/20 0020 下午 1:22
     * @Description: 通过id获取污染事件详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    TraceSourceEventInfoVO getTraceSourceEventDetailById(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/10/29 0029 下午 4:49
     * @Description: 通过自定义参数获取溯源事件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> selectTraceEventInfoByParamMap(Map<String,Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/07/07 0007 下午 2:20
     * @Description: 根据事件ID获取历史走航数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [eventid]
     * @throws:
     */
    Map<String,Object> getHistoryNavigationDataByEventID(Map<String, Object> paramMap);

    List<Map<String, Object>> countEventTypeDataByYear(String year);
}