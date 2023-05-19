package com.tjpu.sp.service.impl.base.mn;

import com.tjpu.sp.dao.environmentalprotection.monitorpoint.DeviceStatusMapper;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.base.mn.AlarmMNService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AlarmMNServiceImpl implements AlarmMNService {
    private final DeviceStatusMapper deviceStatusMapper;

    public AlarmMNServiceImpl(DeviceStatusMapper deviceStatusMapper) {
        this.deviceStatusMapper = deviceStatusMapper;
    }


    /**
     * @author: zhangzhenchao
     * @date: 2019/11/2 14:30
     * @Description: mn号条件查询
     * @param:
     * @return:
     * @throws:
     */
    @Override
    public List<DeviceStatusVO> getMnsByMonitorPointTypes(Set<String> monitorPointTypes) {
        return deviceStatusMapper.getMnsByMonitorPointTypes(monitorPointTypes);
    }
}
