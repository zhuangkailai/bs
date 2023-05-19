package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.SelfMonitorInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SelfMonitorInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(SelfMonitorInfoVO record);

    int insertSelective(SelfMonitorInfoVO record);

    SelfMonitorInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(SelfMonitorInfoVO record);

    int updateByPrimaryKey(SelfMonitorInfoVO record);

    List<Map<String, Object>> getOutPutByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<String> getMonitorContentByPollutionId(Map<String, Object> paramMap);
}