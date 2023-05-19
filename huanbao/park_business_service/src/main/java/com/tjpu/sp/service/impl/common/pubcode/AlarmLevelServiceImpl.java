package com.tjpu.sp.service.impl.common.pubcode;

import com.tjpu.sp.dao.common.pubcode.AlarmLevelMapper;
import com.tjpu.sp.service.common.pubcode.AlarmLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AlarmLevelServiceImpl implements AlarmLevelService {

    @Autowired
    private AlarmLevelMapper alarmLevelMapper;

    @Override
    public List<Map<String, Object>> getAlarmLevelPubCodeInfo() {
        return alarmLevelMapper.getAlarmLevelPubCodeInfo();
    }
}
