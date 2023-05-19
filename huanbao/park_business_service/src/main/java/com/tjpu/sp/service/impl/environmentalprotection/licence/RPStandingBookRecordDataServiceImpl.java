package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.tjpu.sp.dao.environmentalprotection.licence.RPInfoPublicDataMapper;
import com.tjpu.sp.dao.environmentalprotection.licence.RPStandingBookRecordDataMapper;
import com.tjpu.sp.service.environmentalprotection.licence.RPStandingBookRecordDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class RPStandingBookRecordDataServiceImpl implements RPStandingBookRecordDataService {

    private final RPStandingBookRecordDataMapper rpStandingBookRecordDataMapper;
    private final RPInfoPublicDataMapper rpInfoPublicDataMapper;

    public RPStandingBookRecordDataServiceImpl(RPStandingBookRecordDataMapper rpStandingBookRecordDataMapper, RPInfoPublicDataMapper rpInfoPublicDataMapper) {
        this.rpStandingBookRecordDataMapper = rpStandingBookRecordDataMapper;
        this.rpInfoPublicDataMapper = rpInfoPublicDataMapper;
    }


    @Override
    public List<Map<String, Object>> getDataListByParam(Map<String, Object> paramMap) {
        return rpStandingBookRecordDataMapper.getDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getInfoPublicDataListByParam(Map<String, Object> paramMap) {
        return rpInfoPublicDataMapper.getDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getYearTextContentByParam(Map<String, Object> paramMap) {
        return rpInfoPublicDataMapper.getYearTextContentByParam(paramMap);
    }
}
