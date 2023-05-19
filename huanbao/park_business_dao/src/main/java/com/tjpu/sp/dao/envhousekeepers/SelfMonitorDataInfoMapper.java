package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.SelfMonitorDataInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SelfMonitorDataInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(SelfMonitorDataInfoVO record);

    int insertSelective(SelfMonitorDataInfoVO record);

    SelfMonitorDataInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(SelfMonitorDataInfoVO record);

    int updateByPrimaryKey(SelfMonitorDataInfoVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);
}