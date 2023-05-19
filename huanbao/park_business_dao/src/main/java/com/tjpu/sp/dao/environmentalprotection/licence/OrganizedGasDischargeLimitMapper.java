package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.OrganizedGasDischargeLimitVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrganizedGasDischargeLimitMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(OrganizedGasDischargeLimitVO record);

    int insertSelective(OrganizedGasDischargeLimitVO record);

    OrganizedGasDischargeLimitVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(OrganizedGasDischargeLimitVO record);

    int updateByPrimaryKey(OrganizedGasDischargeLimitVO record);

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);
}