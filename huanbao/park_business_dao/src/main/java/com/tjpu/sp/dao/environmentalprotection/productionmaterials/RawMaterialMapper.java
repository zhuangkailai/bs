package com.tjpu.sp.dao.environmentalprotection.productionmaterials;

import com.tjpu.sp.model.environmentalprotection.productionmaterials.RawMaterialVO;

import java.util.List;
import java.util.Map;

public interface RawMaterialMapper {
    int deleteByPrimaryKey(String pkRawmaterialid);

    int insert(RawMaterialVO record);

    int insertSelective(RawMaterialVO record);

    RawMaterialVO selectByPrimaryKey(String pkRawmaterialid);

    int updateByPrimaryKeySelective(RawMaterialVO record);

    int updateByPrimaryKey(RawMaterialVO record);

    List<Map<String,Object>> getRawMaterialsByParamMap(Map<String, Object> paramMap);

    Map<String,Object> getRawMaterialDetailByID(String pkid);
}