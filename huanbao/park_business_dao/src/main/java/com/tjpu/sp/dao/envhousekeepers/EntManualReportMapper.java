package com.tjpu.sp.dao.envhousekeepers;

import com.tjpu.sp.model.envhousekeepers.EntManualReportVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntManualReportMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EntManualReportVO record);

    int insertSelective(EntManualReportVO record);

    EntManualReportVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EntManualReportVO record);

    int updateByPrimaryKey(EntManualReportVO record);

    List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject);
}