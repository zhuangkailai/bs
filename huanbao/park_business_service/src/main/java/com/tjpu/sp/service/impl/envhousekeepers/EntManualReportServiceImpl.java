package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.EntManualReportMapper;
import com.tjpu.sp.model.envhousekeepers.EntManualReportVO;
import com.tjpu.sp.service.envhousekeepers.EntManualReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntManualReportServiceImpl implements EntManualReportService {
    @Autowired
    private EntManualReportMapper entManualReportMapper;


    @Override
    public void updateInfo(EntManualReportVO entManualReportVO) {
        entManualReportMapper.updateByPrimaryKey(entManualReportVO);
    }

    @Override
    public void insertInfo(EntManualReportVO entManualReportVO) {
        entManualReportMapper.insert(entManualReportVO);
    }

    @Override
    public void deleteInfoById(String id) {
        entManualReportMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return entManualReportMapper.getListDataByParamMap(jsonObject);
    }
}
