package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.EntExecuteReportMapper;
import com.tjpu.sp.model.envhousekeepers.EntExecuteReportVO;
import com.tjpu.sp.service.envhousekeepers.EntExecuteReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntExecuteReportServiceImpl implements EntExecuteReportService {
    @Autowired
    private EntExecuteReportMapper entExecuteReportMapper;


    @Override
    public void updateInfo(EntExecuteReportVO entExecuteReportVO) {
        entExecuteReportMapper.updateByPrimaryKey(entExecuteReportVO);
    }

    @Override
    public void insertInfo(EntExecuteReportVO entExecuteReportVO) {
        entExecuteReportMapper.insert(entExecuteReportVO);
    }

    @Override
    public void deleteInfoById(String id) {
        entExecuteReportMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return entExecuteReportMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public List<Map<String, Object>> getEntLastExecuteDataByParamMap(Map<String, Object> paramMap) {
        return entExecuteReportMapper.getEntLastExecuteDataByParamMap(paramMap);
    }
}
