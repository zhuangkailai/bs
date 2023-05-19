package com.tjpu.sp.dao.environmentalprotection.constructionproject;

import com.tjpu.sp.model.environmentalprotection.constructionproject.CheckVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface AcceptanceMapper {
    int deleteByPrimaryKey(String pkCheckid);

    int insert(CheckVO record);

    int insertSelective(CheckVO record);

    CheckVO selectByPrimaryKey(String pkCheckid);

    int updateByPrimaryKeySelective(CheckVO record);

    int updateByPrimaryKey(CheckVO record);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 9:01
    *@Description: 获取项目验收信息列表+分页+条件查询
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getProjectAcceptanceListPage(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 10:49
    *@Description: 通过主键id获取验收详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getProjectAcceptanceDetailById(String id);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 16:17
    *@Description: 根据企业id统计环评验收的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> countCheckNatureByPollutionId(String pollutionid);
}