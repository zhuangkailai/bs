package com.tjpu.sp.dao.environmentalprotection.radiationsafety;

import com.tjpu.sp.model.environmentalprotection.radiationsafety.SourcesVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SourcesMapper {
    int deleteByPrimaryKey(String pkRadid);

    int insert(SourcesVO record);

    int insertSelective(SourcesVO record);

    SourcesVO selectByPrimaryKey(String pkRadid);

    int updateByPrimaryKeySelective(SourcesVO record);

    int updateByPrimaryKey(SourcesVO record);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 16:36
    *@Description: 通过自定义参数获取放射源信息列表
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getSourceListByParamMap(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/22 0022 19:15
    *@Description: 获取放射源详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getSourceDetailById(String id);
}