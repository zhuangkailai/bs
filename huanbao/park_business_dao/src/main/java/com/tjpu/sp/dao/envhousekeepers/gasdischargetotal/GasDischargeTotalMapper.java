package com.tjpu.sp.dao.envhousekeepers.gasdischargetotal;

import com.tjpu.sp.model.envhousekeepers.gasdischargetotal.GasDischargeTotalVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface GasDischargeTotalMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(GasDischargeTotalVO record);

    int insertSelective(GasDischargeTotalVO record);

    GasDischargeTotalVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(GasDischargeTotalVO record);

    int updateByPrimaryKey(GasDischargeTotalVO record);

    List<Map<String,Object>> getGasDischargeTotalByParamMap(Map<String, Object> paramMap);

    void batchInsert(@Param("list") List<GasDischargeTotalVO> paramList);

    void deleteByPollutionIDAndPollutantCode(Map<String, Object> param);

}