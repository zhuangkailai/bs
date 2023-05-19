package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.UnorganizedGasDischargeLimitVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UnorganizedGasDischargeLimitMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(UnorganizedGasDischargeLimitVO record);

    int insertSelective(UnorganizedGasDischargeLimitVO record);

    UnorganizedGasDischargeLimitVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(UnorganizedGasDischargeLimitVO record);

    int updateByPrimaryKey(UnorganizedGasDischargeLimitVO record);

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getVolatilityDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getTotalDataListByParam(Map<String, Object> paramMap);
}