package com.pub.service;

import com.pub.model.CommonSelectTableConfigVO;

import java.util.Map;


public interface CommonSelectTableConfigService {


    CommonSelectTableConfigVO getTableConfigByName(String tableName);

    CommonSelectTableConfigVO getTableConfigVOBySysModel(String sysModel);

    int getTableHasIdentity(String tableName);


    int getMaxNumByTableName(Map<String, Object> paramMap);
}
