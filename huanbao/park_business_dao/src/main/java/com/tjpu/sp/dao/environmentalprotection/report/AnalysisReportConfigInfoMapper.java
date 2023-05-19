package com.tjpu.sp.dao.environmentalprotection.report;

import com.tjpu.sp.model.environmentalprotection.report.AnalysisReportConfigInfo;

public interface AnalysisReportConfigInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(AnalysisReportConfigInfo record);

    int insertSelective(AnalysisReportConfigInfo record);

    AnalysisReportConfigInfo selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(AnalysisReportConfigInfo record);

    int updateByPrimaryKey(AnalysisReportConfigInfo record);
}