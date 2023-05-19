package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.WaterDischargeLimitVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WaterDischargeLimitMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(WaterDischargeLimitVO record);

    int insertSelective(WaterDischargeLimitVO record);

    WaterDischargeLimitVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(WaterDischargeLimitVO record);

    int updateByPrimaryKey(WaterDischargeLimitVO record);

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getSpecialDataListByParam(Map<String, Object> paramMap);
}