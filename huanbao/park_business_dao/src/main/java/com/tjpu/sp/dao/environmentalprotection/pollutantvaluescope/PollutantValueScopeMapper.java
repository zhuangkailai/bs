package com.tjpu.sp.dao.environmentalprotection.pollutantvaluescope;

import com.tjpu.sp.model.environmentalprotection.pollutantvaluescope.PollutantValueScopeVO;

import java.util.List;
import java.util.Map;

public interface PollutantValueScopeMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(PollutantValueScopeVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(PollutantValueScopeVO record);


    /**
     * @author: chengzq
     * @date: 2020/05/19 0016 下午 2:37
     * @Description:  通过自定义参数获取污染物监测值范围信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getPollutantValueScopeByParamMap(Map<String, Object> paramMap);

}