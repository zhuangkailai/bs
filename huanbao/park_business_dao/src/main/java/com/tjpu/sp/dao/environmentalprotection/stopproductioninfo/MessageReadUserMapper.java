package com.tjpu.sp.dao.environmentalprotection.stopproductioninfo;

import com.tjpu.sp.model.environmentalprotection.stopproductioninfo.MessageReadUserVO;

public interface MessageReadUserMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(MessageReadUserVO record);

    int insertSelective(MessageReadUserVO record);

    MessageReadUserVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(MessageReadUserVO record);

    int updateByPrimaryKey(MessageReadUserVO record);

    void deleteByRecordID(String fkrecordid);
}