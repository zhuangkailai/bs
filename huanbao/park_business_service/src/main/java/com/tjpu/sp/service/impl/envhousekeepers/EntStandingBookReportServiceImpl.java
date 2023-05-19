package com.tjpu.sp.service.impl.envhousekeepers;


import com.tjpu.sp.dao.envhousekeepers.EntStandingBookReportMapper;
import com.tjpu.sp.model.envhousekeepers.EntStandingBookReportVO;
import com.tjpu.sp.service.envhousekeepers.EntStandingBookReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class EntStandingBookReportServiceImpl implements EntStandingBookReportService {
    @Autowired
    private EntStandingBookReportMapper entStandingBookReportMapper;


    @Override
    public void updateInfo(EntStandingBookReportVO entStandingBookReportVO) {
        entStandingBookReportMapper.updateByPrimaryKey(entStandingBookReportVO);
    }

    @Override
    public void insertInfo(EntStandingBookReportVO entStandingBookReportVO) {
        entStandingBookReportMapper.insert(entStandingBookReportVO);
    }

    @Override
    public void deleteInfoById(String id) {
        entStandingBookReportMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getListDataByParamMap(Map<String, Object> jsonObject) {
        return entStandingBookReportMapper.getListDataByParamMap(jsonObject);
    }

    @Override
    public List<Map<String, Object>> countEntStandingBookData(String pollutionid) {
        return entStandingBookReportMapper.countEntStandingBookData(pollutionid);
    }

    @Override
    public Map<String, Object> getEntStandingBookDetailByID(String id) {
        return entStandingBookReportMapper.getEntStandingBookDetailByID(id);
    }

    @Override
    public List<Map<String, Object>> getUpdateDataByParamMap(Map<String, Object> paramMap) {
        return entStandingBookReportMapper.getUpdateDataByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getNoUpdateDataByParamMap(Map<String, Object> paramMap) {
        return entStandingBookReportMapper.getNoUpdateDataByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllEntStandingByParamMap(Map<String, Object> jsonObject) {
        return entStandingBookReportMapper.getAllEntStandingByParamMap(jsonObject);
    }

    @Override
    public List<Map<String, Object>> getEntLastStandingDataByParamMap(Map<String, Object> paramMap) {
        return entStandingBookReportMapper.getEntLastStandingDataByParamMap(paramMap);
    }
}
