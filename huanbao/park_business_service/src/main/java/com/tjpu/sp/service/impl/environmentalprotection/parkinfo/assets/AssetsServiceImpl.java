package com.tjpu.sp.service.impl.environmentalprotection.parkinfo.assets;

import com.tjpu.sp.dao.environmentalprotection.parkinfo.assets.AssetsMapper;
import com.tjpu.sp.service.environmentalprotection.parkinfo.assets.AssetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AssetsServiceImpl implements AssetsService {
    @Autowired
    private AssetsMapper assetsMapper;


    /**
     * @author: chengzq
     * @date: 2020/11/12 0012 下午 5:26
     * @Description: 通过年份获取主要经济指标及金额信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [year]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAssertsTypeInfoByYear(String year) {
        return assetsMapper.getAssertsTypeInfoByYear(year);
    }


    /**
     * @author: chengzq
     * @date: 2020/11/12 0012 下午 5:03
     * @Description: 获取逐年的净利情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAssretsInfos() {
        return assetsMapper.getAssretsInfos();
    }

    @Override
    public List<Map<String, Object>> getPrimeIndustryAssretsInfos() {
        return assetsMapper.getPrimeIndustryAssretsInfos();
    }
}
