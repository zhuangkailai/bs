package com.tjpu.sp.service.environmentalprotection.online;


import com.tjpu.sp.model.environmentalprotection.online.VoucherRecordVO;

import java.util.List;
import java.util.Map;

public interface VoucherRecordService {

    List<Map<String, Object>> getVoucherRecordListDataByParam(Map<String, Object> paramMap);

    void insert(VoucherRecordVO voucherRecordVO);

    void deleteById(String id);
}
