package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.sp.dao.envhousekeepers.EntExecuteReportMapper;
import com.tjpu.sp.dao.envhousekeepers.EntStandingBookReportMapper;
import com.tjpu.sp.dao.environmentalprotection.licence.LicenceInfoMapper;
import com.tjpu.sp.service.environmentalprotection.licence.CheckProblemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CheckProblemServiceImpl implements CheckProblemService {

    private final LicenceInfoMapper licenceInfoMapper;
    private final EntExecuteReportMapper entExecuteReportMapper;
    private final EntStandingBookReportMapper entStandingBookReportMapper;


    public CheckProblemServiceImpl(LicenceInfoMapper licenceInfoMapper, EntExecuteReportMapper entExecuteReportMapper, EntStandingBookReportMapper entStandingBookReportMapper) {
        this.licenceInfoMapper = licenceInfoMapper;
        this.entExecuteReportMapper = entExecuteReportMapper;
        this.entStandingBookReportMapper = entStandingBookReportMapper;
    }

    /**
     * @Description: 自定义查询条件，统计问题来源数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/5/11 13:44
     */
    @Override
    public List<Map<String, Object>> getProblemSourceDataByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getProblemSourceDataByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> countExecuteReportData(Map<String, Object> paramMap) {
        return entExecuteReportMapper.countExecuteReportData(paramMap);
    }

    @Override
    public List<Map<String, Object>> countStandingBookReport(Map<String, Object> paramMap) {
        return entStandingBookReportMapper.countStandingBookReport(paramMap);
    }
}