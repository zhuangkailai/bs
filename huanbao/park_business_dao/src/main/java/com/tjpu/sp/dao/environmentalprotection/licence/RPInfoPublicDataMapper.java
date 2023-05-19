package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPInfoPublicDataVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPInfoPublicDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPInfoPublicDataVO record);

    int insertSelective(RPInfoPublicDataVO record);

    RPInfoPublicDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPInfoPublicDataVO record);

    int updateByPrimaryKey(RPInfoPublicDataVO record);

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getYearTextContentByParam(Map<String, Object> paramMap);
}