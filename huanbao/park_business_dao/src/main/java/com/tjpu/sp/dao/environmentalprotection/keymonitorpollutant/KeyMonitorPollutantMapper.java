package com.tjpu.sp.dao.environmentalprotection.keymonitorpollutant;

import com.tjpu.sp.model.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface KeyMonitorPollutantMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(KeyMonitorPollutantVO record);

    int insertSelective(KeyMonitorPollutantVO record);

    KeyMonitorPollutantVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(KeyMonitorPollutantVO record);

    int updateByPrimaryKey(KeyMonitorPollutantVO record);

    List<Map<String, Object>> getKeyMonitorPollutantsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 1:27
     * @Description: 通过污染物类型获取重点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    List<Map<String, Object>> selectByPollutanttype(String pollutanttype);


    List<Map<String,Object>> selectByPollutanttypes(Map<String, Object> paramMap);
}