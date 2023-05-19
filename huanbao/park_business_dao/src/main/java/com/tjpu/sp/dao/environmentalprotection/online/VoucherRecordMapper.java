package com.tjpu.sp.dao.environmentalprotection.online;

import com.tjpu.sp.model.environmentalprotection.online.VoucherRecordVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VoucherRecordMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(VoucherRecordVO record);

    int insertSelective(VoucherRecordVO record);

    VoucherRecordVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(VoucherRecordVO record);

    int updateByPrimaryKey(VoucherRecordVO record);

    List<Map<String, Object>> getVoucherRecordListDataByParam(Map<String, Object> paramMap);
}