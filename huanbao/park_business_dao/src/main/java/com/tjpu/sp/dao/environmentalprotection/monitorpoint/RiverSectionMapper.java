package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.RiverSectionVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RiverSectionMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(RiverSectionVO record);

    int insertSelective(RiverSectionVO record);

    RiverSectionVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(RiverSectionVO record);

    int updateByPrimaryKey(RiverSectionVO record);

    List<Map<String, Object>> getRiverSectionPointListByParam(Map<String, Object> paramMap);

    long countTotalByParam(Map<String, Object> paramMap);
}