package com.tjpu.sp.service.impl.environmentalprotection.deviceproblemrecord;

import com.tjpu.sp.dao.environmentalprotection.deviceproblemrecord.DeviceProblemRecordMapper;
import com.tjpu.sp.service.environmentalprotection.deviceproblemrecord.DeviceProblemRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DeviceProblemRecordServiceImpl implements DeviceProblemRecordService {
    @Autowired
    private DeviceProblemRecordMapper deviceProblemRecordMapper;


    @Override
    public List<Map<String, Object>> getDeviceProblemRecordsByParamMap(Map<String, Object> paramMap) {
        return deviceProblemRecordMapper.getDeviceProblemRecordsByParamMap(paramMap);
    }


    @Override
    public Map<String, Object> getDeviceProblemRecordDetailById(Map<String, Object> paramMap) {
        return deviceProblemRecordMapper.getDeviceProblemRecordDetailById(paramMap);
    }


}
