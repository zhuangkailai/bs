package com.tjpu.sp.service.environmentalprotection.tracesource;


import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceEventInfoVO;
import net.sf.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public interface TraceSourceEventInfoService {

    int deleteByPrimaryKey(String pkId);
    TraceSourceEventInfoVO selectByPrimaryKey(String pkId);

    /**
     *
     * @author: lip
     * @date: 2019/8/14 0014 上午 10:43
     * @Description: 添加实体数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    int insert(TraceSourceEventInfoVO traceSourceEventInfoVO,JSONObject jsonObject);
    /**
     * @author: chengzq
     * @date: 2019/8/28 0028 上午 11:53
     * @Description: 修改数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [traceSourceEventInfoVO, petitionInfoVO]
     * @throws:
     */
    int update(TraceSourceEventInfoVO traceSourceEventInfoVO,JSONObject jsonObject);
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
     * @date: 2019/9/23 0023 上午 9:47
     * @Description: 修改污染事件以及溯源企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    void updateEventAndPollution(Map<String, Object> paramMap);


    /**
     * @author: xsm
     * @date: 2019/9/23 0023 上午 11:49
     * @Description: 新增溯源事件流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    void addTraceSourceEventFlowByParamMap(Map<String, Object> map);

    /**
     * @author: xsm
     * @date: 2019/9/23 0023 下午 1:58
     * @Description: 获取溯源事件流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getTraceSourceEventFlowInfoByID(String id);


    /**
     * @author: chengzq
     * @date: 2019/9/24 0024 上午 9:33
     * @Description: 通过事件id获取会商结果
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [eventid]
     * @throws:
     */
    LinkedHashSet<Map<String,Object>> getConsultationResultByEventId(String eventid);

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
