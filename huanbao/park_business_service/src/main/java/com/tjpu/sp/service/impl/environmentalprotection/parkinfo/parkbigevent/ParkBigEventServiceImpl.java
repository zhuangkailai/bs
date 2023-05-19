package com.tjpu.sp.service.impl.environmentalprotection.parkinfo.parkbigevent;

import com.tjpu.sp.dao.environmentalprotection.parkinfo.parkbigevent.ParkBigEventMapper;
import com.tjpu.sp.service.environmentalprotection.parkinfo.parkbigevent.ParkBigEventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ParkBigEventServiceImpl implements ParkBigEventService {
    private final ParkBigEventMapper parkBigEventMapper;

    public ParkBigEventServiceImpl(ParkBigEventMapper parkBigEventMapper) {
        this.parkBigEventMapper = parkBigEventMapper;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:43
     * @Description: 获取大事件信息按时间倒序
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getDescBigEventsInTime() {
        return parkBigEventMapper.getDescBigEventsInTime();
    }
}
