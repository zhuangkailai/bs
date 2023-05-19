package com.tjpu.sp.dao.envhousekeepers.entproblemrectifyreport;
import com.tjpu.sp.model.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportVO;

import java.util.List;
import java.util.Map;

public interface EntProblemRectifyReportMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntProblemRectifyReportVO record);

    int insertSelective(EntProblemRectifyReportVO record);

    EntProblemRectifyReportVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntProblemRectifyReportVO record);

    int updateByPrimaryKey(EntProblemRectifyReportVO record);

    List<Map<String,Object>> getEntProblemRectifyReportByParamMap(Map<String, Object> param);

    Map<String,Object> getEntProblemRectifyReportByID(String id);

    Map<String,Object> getEntProblemRectifyReportDetailByID(String id);

    List<Map<String,Object>> IsEntCheckReportValidByParam(Map<String, Object> paramMap);
}