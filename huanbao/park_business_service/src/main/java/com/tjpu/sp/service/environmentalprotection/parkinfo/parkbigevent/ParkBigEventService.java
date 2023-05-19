package com.tjpu.sp.service.environmentalprotection.parkinfo.parkbigevent;

import java.util.List;
import java.util.Map;

public interface ParkBigEventService {
    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:43
     * @Description: 获取大事件信息按时间倒序
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDescBigEventsInTime();
}
