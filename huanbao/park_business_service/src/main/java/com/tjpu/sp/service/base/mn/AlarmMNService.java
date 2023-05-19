package com.tjpu.sp.service.base.mn;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;

import java.util.List;
import java.util.Set;

/**
 * @author: zhangzhenchao
 * @date: 2019/11/2 14:29
 * @Description: 监测提醒报警MN号
 */
public interface AlarmMNService {

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/2 14:30
     * @Description: mn号获取
     * @param:
     * @return:
     * @throws:
     */
    List<DeviceStatusVO> getMnsByMonitorPointTypes(Set<String> monitorPointTypes);
}
