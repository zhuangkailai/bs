package com.tjpu.sp.service.impl.environmentalprotection.keymonitorpollutant;

import com.tjpu.sp.dao.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantMapper;
import com.tjpu.sp.service.environmentalprotection.keymonitorpollutant.KeyMonitorPollutantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class KeyMonitorPollutantServiceImpl implements KeyMonitorPollutantService {

    private final KeyMonitorPollutantMapper keyMonitorPollutantMapper;

    @Autowired
    public KeyMonitorPollutantServiceImpl(KeyMonitorPollutantMapper keyMonitorPollutantMapper) {
        this.keyMonitorPollutantMapper = keyMonitorPollutantMapper;
    }



    /**
     * @author: xsm
     * @date: 2019/6/22 0022 下午 2:19
     * @Description: 获取重点监测污染物初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getKeyMonitorPollutantsByParamMap(Map<String, Object> paramMap) {
        return keyMonitorPollutantMapper.getKeyMonitorPollutantsByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 1:27
     * @Description: 通过污染物类型获取重点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectByPollutanttype(String pollutanttype) {
        return keyMonitorPollutantMapper.selectByPollutanttype(pollutanttype);
    }

}
