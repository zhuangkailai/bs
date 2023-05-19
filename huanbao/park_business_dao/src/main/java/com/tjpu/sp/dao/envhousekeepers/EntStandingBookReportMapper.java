package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.EntStandingBookReportVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntStandingBookReportMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntStandingBookReportVO record);

    int insertSelective(EntStandingBookReportVO record);

    EntStandingBookReportVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntStandingBookReportVO record);

    int updateByPrimaryKey(EntStandingBookReportVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> countEntStandingBookData(String pollutionid);

    Map<String,Object> getEntStandingBookDetailByID(String id);

    List<Map<String, Object>> getUpdateDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getNoUpdateDataByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> getAllEntStandingByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> countStandingBookReport(Map<String, Object> paramMap);

    List<Map<String, Object>> getEntLastStandingDataByParamMap(Map<String, Object> paramMap);
}