package com.pub.service;

import com.pub.model.OperateLogVO;

import java.util.Map;


public interface OperateLogService {

    int insert(OperateLogVO operateLogVO);

    String getUserDataPermissionsEditLog(Map<String, Object> oldMap,
                                         Map<String, Object> newMap);



	String doOperateLogVO(String operateType, String tableName,
                          Map<String, Object> oldMapData, Map<String, Object> newMapData);


	void saveUserOperationLog(String operateType, Map<String, Object> paramMap, Map<String, Object> compareDataMap,
                              Map<String, Object> oldMapData, Map<String, Object> newMapData);

	String fomateEditLog(String tableName, Map<String, Object> oldMaps,
                         Map<String, Object> newMaps);
     
}
