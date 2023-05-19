package com.pub.impl;

import com.pub.model.CommonSelectTableConfigVO;
import com.pub.service.CommonSelectTableConfigService;
import com.pub.dao.CommonSelectTableConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@Transactional
public class CommonSelectTableConfigServiceImpl implements CommonSelectTableConfigService {
    @Autowired
    private CommonSelectTableConfigMapper commonSelectTableConfigMapper;


    @Override
    public CommonSelectTableConfigVO getTableConfigByName(String tableName) {
        return commonSelectTableConfigMapper.getTableConfigByName(tableName);
    }


    @Override
    public CommonSelectTableConfigVO getTableConfigVOBySysModel(String sysModel) {
        return commonSelectTableConfigMapper.getTableConfigVOBySysModel(sysModel);
    }


    @Override
    public int getTableHasIdentity(String tableName) {
        return commonSelectTableConfigMapper.getTableHasIdentity(tableName);
    }


    @Override
    public int getMaxNumByTableName(Map<String, Object> paramMap) {
        return commonSelectTableConfigMapper.getMaxNumByTableName(paramMap);
    }

}
