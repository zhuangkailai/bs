package com.tjpu.sp.dao.envhousekeepers.checktemplateconfig;

import com.tjpu.sp.model.envhousekeepers.checktemplateconfig.CheckTemplateConfigVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CheckTemplateConfigMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(CheckTemplateConfigVO record);

    int insertSelective(CheckTemplateConfigVO record);

    CheckTemplateConfigVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(CheckTemplateConfigVO record);

    int updateByPrimaryKey(CheckTemplateConfigVO record);

    List<Map<String,Object>> getCheckTemplateConfigsByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllInspectTypes(Map<String,Object> param);

    List<Map<String,Object>> IsValidForValueByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> IsHasCheckTemplateConfigHistoryData(Map<String, Object> paramMap);

    List<Map<String,Object>> getAllCheckCategoryDataByInspectTypeID(Map<String, Object> param);

    List<Map<String,Object>> getCheckContentDataByCheckCategoryID(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntCheckItemConfigDataByParam(Map<String, Object> paramMap);

    void batchInsertEntCheckItemConfig(@Param("list")List<Map<String, Object>> addlist);

    void deleteEntCheckItemConfigByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> IsHasEntCheckItemConfigHistoryData(Map<String, Object> paramMap);
}