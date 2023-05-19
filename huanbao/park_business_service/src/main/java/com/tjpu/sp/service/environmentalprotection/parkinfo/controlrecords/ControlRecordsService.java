package com.tjpu.sp.service.environmentalprotection.parkinfo.controlrecords;

import com.github.pagehelper.PageInfo;
import com.tjpu.sp.model.environmentalprotection.controlrecords.ControlRecordsVO;

import java.util.List;
import java.util.Map;

public interface ControlRecordsService {

    Map<String,Object> getLastData();

    List<Map<String,Object>> getControlRecordsDataByParam(Map<String, Object> paramMap);

    PageInfo<Map<String,Object>> getPageDataByParam(Map<String, Object> paramMap);

    void addData(ControlRecordsVO controlRecordsVO);

    void updateData(ControlRecordsVO controlRecordsVO);

    void deleteById(String id);

    Map<String, Object> getEditOrDetailById(String id);

    List<Map<String, Object>> getAllStinkPoint();
}
