package com.tjpu.sp.service.impl.environmentalprotection.tracesource;

import com.tjpu.sp.dao.environmentalprotection.tracesource.PollutaEventDetailInfoMapper;
import com.tjpu.sp.model.environmentalprotection.tracesource.PollutaEventDetailInfoVO;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutaEventDetailInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class PollutaEventDetailInfoServiceImpl implements PollutaEventDetailInfoService {


    @Autowired
    private PollutaEventDetailInfoMapper pollutaEventDetailInfoMapper;


    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 5:12
     * @Description: 通过污染事件id获取事件详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public PollutaEventDetailInfoVO selectByPolluteeventid(String id) {
        return pollutaEventDetailInfoMapper.selectByPolluteeventid(id);
    }

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 5:56
     * @Description: 通过污染事件id获取监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public String selectMonitorInfoByPolluteeventid(String id) {
        return pollutaEventDetailInfoMapper.selectMonitorInfoByPolluteeventid(id);
    }
}
