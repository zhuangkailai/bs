package com.tjpu.sp.dao.environmentalprotection.report;

import com.tjpu.sp.model.environmentalprotection.report.AnalysisReportDataVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AnalysisReportDataMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(AnalysisReportDataVO record);

    int insertSelective(AnalysisReportDataVO record);

    AnalysisReportDataVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(AnalysisReportDataVO record);

    int updateByPrimaryKey(AnalysisReportDataVO record);

    /**
     *
     * @author: lip
     * @date: 2019/8/26 0026 上午 11:14
     * @Description: 自定义查询条件获取分析报告属性数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getReportAttributeDataByParam(Map<String, Object> paramMap);

    void deleteReportAttributeDataByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/8/26 0026 下午 4:26
     * @Description: 批量插入数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void batchInsert(List<AnalysisReportDataVO> analysisReportDataVOS);

    List<Map<String, Object>> countReportDataByParam(Map<String, Object> paramMap);
}