package com.tjpu.sp.dao.environmentalprotection.licence;

import com.tjpu.sp.model.environmentalprotection.licence.RPBlowdownUnitBaseVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RPBlowdownUnitBaseMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RPBlowdownUnitBaseVO record);

    int insertSelective(RPBlowdownUnitBaseVO record);

    RPBlowdownUnitBaseVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RPBlowdownUnitBaseVO record);

    int updateByPrimaryKey(RPBlowdownUnitBaseVO record);

    List<Map<String, Object>> getBlowdownUnitDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitYLDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitNYDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitTZDataListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getBlowdownUnitCPDataListByParam(Map<String, Object> paramMap);
}