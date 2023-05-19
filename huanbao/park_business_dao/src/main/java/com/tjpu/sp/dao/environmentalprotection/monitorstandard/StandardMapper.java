package com.tjpu.sp.dao.environmentalprotection.monitorstandard;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorstandard.StandardVO;

import java.util.List;
import java.util.Map;

public interface StandardMapper {
    int deleteByPrimaryKey(String pkStandardid);

    int insert(StandardVO record);

    int insertSelective(StandardVO record);

    StandardVO selectByPrimaryKey(String pkStandardid);

    int updateByPrimaryKeySelective(StandardVO record);

    int updateByPrimaryKey(StandardVO record);

    List<Map<String,Object>> getAllStandard();

    List<Map<String,Object>> getMonitorStandardListsByParamMap(Map<String, Object> paramMap);

    List<Map<String,Object>> getMonitorStandardsByParamMap(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getWaterStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getOtherStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getWQStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getGasStandardList(Map<String, Object> paramMap);

    List<PollutantSetDataVO> getGasPointStandardDataList(Map<String, Object> paramMap);
}