package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.EntWorkDynamicVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntWorkDynamicMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntWorkDynamicVO record);

    int insertSelective(EntWorkDynamicVO record);

    EntWorkDynamicVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntWorkDynamicVO record);

    int updateByPrimaryKey(EntWorkDynamicVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    Map<String, Object> getEditOrDetailsDataById(String id);
}