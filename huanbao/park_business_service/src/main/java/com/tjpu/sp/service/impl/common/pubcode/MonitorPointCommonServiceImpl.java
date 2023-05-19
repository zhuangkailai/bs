package com.tjpu.sp.service.impl.common.pubcode;

import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.service.common.pubcode.MonitorPointCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: chengzq
 * @date: 2021/3/10 0010 13:22
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@Transactional
public class MonitorPointCommonServiceImpl implements MonitorPointCommonService {

    @Autowired
    private PollutionMapper pollutionMapper;

    /**
     * @author: chengzq
     * @date: 2021/3/30 0030 上午 8:59
     * @Description: 通过自定义条件获取所有环保监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOutPutInfosByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.getOutPutInfosByParamMap(paramMap);
    }

    


}
