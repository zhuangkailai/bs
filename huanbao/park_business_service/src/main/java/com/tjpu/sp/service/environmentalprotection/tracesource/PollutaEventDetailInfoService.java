package com.tjpu.sp.service.environmentalprotection.tracesource;


import com.tjpu.sp.model.environmentalprotection.tracesource.PollutaEventDetailInfoVO;

public interface PollutaEventDetailInfoService {

    PollutaEventDetailInfoVO selectByPolluteeventid(String id);

    /**
     * @author: chengzq
     * @date: 2019/8/29 0029 下午 5:54
     * @Description: 通过污染事件id获取监测点类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    String selectMonitorInfoByPolluteeventid(String id);

}
