package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.SpecialGasPollutantLimitVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SpecialGasPollutantLimitMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(SpecialGasPollutantLimitVO record);

    int insertSelective(SpecialGasPollutantLimitVO record);

    SpecialGasPollutantLimitVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(SpecialGasPollutantLimitVO record);

    int updateByPrimaryKey(SpecialGasPollutantLimitVO record);

    List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap);
}