package com.tjpu.sp.dao.common.standard;

import com.tjpu.sp.model.common.standard.StandardInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StandardInfoMapper {
    int deleteByPrimaryKey(String pkStandardid);

    int insert(StandardInfoVO record);

    int insertSelective(StandardInfoVO record);

    StandardInfoVO selectByPrimaryKey(String pkStandardid);

    int updateByPrimaryKeySelective(StandardInfoVO record);

    int updateByPrimaryKey(StandardInfoVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> countKnowledgeData();

    Map<String, Object> getEditOrDetailsDataById(@Param(value = "id") String id);


}