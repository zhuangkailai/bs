package com.tjpu.sp.dao.environmentalprotection.report;

import com.tjpu.sp.model.environmentalprotection.report.ReportInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface ReportInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(ReportInfoVO record);

    int insertSelective(ReportInfoVO record);

    ReportInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(ReportInfoVO record);

    int updateByPrimaryKey(ReportInfoVO record);

    List<Map<String,Object>> getReportInfosByParamMap(Map<String, Object> paramMap);
}