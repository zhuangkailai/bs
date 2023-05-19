package com.tjpu.sp.service.impl.environmentalprotection.monitorstandard;

import com.tjpu.sp.dao.environmentalprotection.monitorstandard.StandardMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.PollutantSetDataVO;
import com.tjpu.sp.service.environmentalprotection.monitorstandard.MonitorStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MonitorStandardServiceImpl implements MonitorStandardService {

    @Autowired
    private StandardMapper standardMapper;

    /**
     * @author: xsm
     * @date: 2019/09/05 0005 下午 2:57
     * @Description:根据自定义参数获取排放标准管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getMonitorStandardListsByParamMap(Map<String, Object> paramMap) {
        return standardMapper.getMonitorStandardListsByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/10 0010 上午 9:04
     * @Description: 获取所有标准
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllStandard() {
        return standardMapper.getAllStandard();
    }

    @Override
    public List<Map<String, Object>> getMonitorStandardsByParamMap(Map<String, Object> paramMap) {
        return standardMapper.getMonitorStandardsByParamMap(paramMap);
    }

    @Override
    public List<PollutantSetDataVO> getWaterStandardList(Map<String, Object> paramMap) {
        return standardMapper.getWaterStandardList(paramMap);
    }

    @Override
    public List<PollutantSetDataVO> getOtherStandardList(Map<String, Object> paramMap) {
        return standardMapper.getOtherStandardList(paramMap);
    }

    @Override
    public List<PollutantSetDataVO> getWQStandardList(Map<String, Object> paramMap) {
        return standardMapper.getWQStandardList(paramMap);
    }

    @Override
    public List<PollutantSetDataVO> getGasStandardList(Map<String, Object> paramMap) {
        return standardMapper.getGasStandardList(paramMap);
    }

    @Override
    public List<PollutantSetDataVO> getGasPointStandardDataList(Map<String, Object> paramMap) {
        return standardMapper.getGasPointStandardDataList(paramMap);
    }

}
