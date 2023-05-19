package com.tjpu.sp.dao.environmentalprotection.cjpz;

import com.tjpu.sp.model.environmentalprotection.cjpz.EntConnectSetVO;

import java.util.List;
import java.util.Map;

public interface EntConnectSetMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntConnectSetVO record);

    int insertSelective(EntConnectSetVO record);

    EntConnectSetVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntConnectSetVO record);


    int updateByPrimaryKey(EntConnectSetVO record);

    List<Map<String,Object>> getEntConnectSetsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getEntConnectSetDetailByID(String id);

    long getEntConnectSetNumByParamMap(Map<String, Object> paramMap);
}