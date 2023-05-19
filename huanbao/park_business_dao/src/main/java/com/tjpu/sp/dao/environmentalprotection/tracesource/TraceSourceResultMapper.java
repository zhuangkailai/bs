package com.tjpu.sp.dao.environmentalprotection.tracesource;

import com.tjpu.sp.model.environmentalprotection.tracesource.TraceSourceResultVO;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TraceSourceResultMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(TraceSourceResultVO record);

    int insertSelective(TraceSourceResultVO record);

    TraceSourceResultVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(TraceSourceResultVO record);

    int updateByPrimaryKey(TraceSourceResultVO record);

    List<Map<String, Object>> getDataListByParam(JSONObject jsonObject);

}