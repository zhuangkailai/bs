package com.tjpu.sp.dao.environmentalprotection.anticontroltemplate;

import com.tjpu.sp.model.environmentalprotection.anticontroltemplate.AntiControlTemplateConfigVO;

import java.util.List;
import java.util.Map;

public interface AntiControlTemplateConfigMapper {
    int deleteByPrimaryKey(String pkTemplateid);

    int insert(AntiControlTemplateConfigVO record);

    int insertSelective(AntiControlTemplateConfigVO record);

    AntiControlTemplateConfigVO selectByPrimaryKey(String pkTemplateid);

    int updateByPrimaryKeySelective(AntiControlTemplateConfigVO record);

    int updateByPrimaryKey(AntiControlTemplateConfigVO record);

    List<Map<String,Object>> getAntiControlTemplateDataByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAntiControlFieldDataByTemplateid(String templateid);

    List<Map<String,Object>> getAntiControlFieldDataByParam(Map<String, Object> param);

    List<Map<String,Object>> getPointPollutantDataByParamMap(Map<String, Object> param);

    Map<String,Object> getOnePointAccessPasswordByParamMap(Map<String, Object> param);

    List<Map<String,Object>> getInformationEncoding();
}