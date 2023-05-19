package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.sp.dao.environmentalprotection.online.VoucherRecordMapper;
import com.tjpu.sp.model.environmentalprotection.online.VoucherRecordVO;
import com.tjpu.sp.service.environmentalprotection.online.OnlineVoyageMonitorService;

import com.tjpu.sp.service.environmentalprotection.online.VoucherRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class VoucherRecordServiceImpl implements VoucherRecordService {

    @Autowired
    private VoucherRecordMapper voucherRecordMapper;

    @Override
    public List<Map<String, Object>> getVoucherRecordListDataByParam(Map<String, Object> paramMap) {
        return voucherRecordMapper.getVoucherRecordListDataByParam(paramMap);
    }

    @Override
    public void insert(VoucherRecordVO voucherRecordVO) {
        voucherRecordMapper.insert(voucherRecordVO);
    }

    @Override
    public void deleteById(String id) {
        voucherRecordMapper.deleteByPrimaryKey(id);
    }
}
