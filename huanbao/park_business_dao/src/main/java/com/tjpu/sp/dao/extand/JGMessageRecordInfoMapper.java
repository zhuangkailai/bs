package com.tjpu.sp.dao.extand;

import com.tjpu.sp.model.extand.JGMessageRecordInfoVO;
import org.springframework.stereotype.Repository;

@Repository
public interface JGMessageRecordInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(JGMessageRecordInfoVO record);

    int insertSelective(JGMessageRecordInfoVO record);

    JGMessageRecordInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(JGMessageRecordInfoVO record);

    int updateByPrimaryKey(JGMessageRecordInfoVO record);
}