package com.tjpu.auth.dao.system;

import com.tjpu.auth.model.system.SysAppVO;
import org.springframework.stereotype.Service;

@Service
public interface SysAppMapper {
    int deleteByPrimaryKey(String appid);

    int insert(SysAppVO record);

    int insertSelective(SysAppVO record);

    SysAppVO selectByPrimaryKey(String appid);

    int updateByPrimaryKeySelective(SysAppVO record);

    int updateByPrimaryKey(SysAppVO record);
}