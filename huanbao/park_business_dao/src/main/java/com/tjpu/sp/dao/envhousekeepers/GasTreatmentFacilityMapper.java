package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.GasTreatmentFacilityVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GasTreatmentFacilityMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(GasTreatmentFacilityVO record);

    int insertSelective(GasTreatmentFacilityVO record);

    GasTreatmentFacilityVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(GasTreatmentFacilityVO record);

    int updateByPrimaryKey(GasTreatmentFacilityVO record);

    void deleteByFacilityId(String id);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> getGasOutPutByPollutionId(@Param("pollutionid") String pollutionid);

    List<Map<String,Object>> getGasTreatmentListDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getGasFacilityDataListByParam(Map<String, Object> paramMap);
}