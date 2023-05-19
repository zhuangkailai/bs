package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.EntExecuteReportVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntExecuteReportMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntExecuteReportVO record);

    int insertSelective(EntExecuteReportVO record);

    EntExecuteReportVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntExecuteReportVO record);

    int updateByPrimaryKey(EntExecuteReportVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);

    List<Map<String, Object>> countExecuteReportData(Map<String, Object> paramMap);

    List<Map<String, Object>> getEntLastExecuteDataByParamMap(Map<String, Object> paramMap);
}