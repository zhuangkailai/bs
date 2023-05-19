package com.tjpu.auth.dao.system;

import com.tjpu.auth.model.system.OperateLogVO;
import org.springframework.stereotype.Repository;

@Repository
public interface OperateLogMapper {
    int deleteByPrimaryKey(String baseOperateId);

    int insert(OperateLogVO record);

    int insertSelective(OperateLogVO record);

    OperateLogVO selectByPrimaryKey(String baseOperateId);

    int updateByPrimaryKeySelective(OperateLogVO record);

    int updateByPrimaryKey(OperateLogVO record);

}