package com.tjpu.sp.dao.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceEntInfoVO;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public interface TraceSourceEntInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TraceSourceEntInfoVO record);

    int insertSelective(TraceSourceEntInfoVO record);

    TraceSourceEntInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TraceSourceEntInfoVO record);

    int updateByPrimaryKey(TraceSourceEntInfoVO record);

    int deleteByPetitionIdAndResultType(Map<String,Object> paramMap);
    /**
     * @author: chengzq
     * @date: 2019/9/23 0023 上午 10:33
     * @Description: 批量新增溯源企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    int insertEntInfoBatch(List<TraceSourceEntInfoVO> list);
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
}