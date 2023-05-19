package com.tjpu.sp.service.common.pubcode;

import java.util.List;
import java.util.Map;

public interface AlarmLevelService {
    /**
     * @author: chengzq
     * @date: 2019/5/21 0021 下午 1:20
     * @Description: 获取所有报警级别码表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getAlarmLevelPubCodeInfo();
}
