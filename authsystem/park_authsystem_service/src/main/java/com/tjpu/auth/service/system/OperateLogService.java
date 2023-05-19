package com.tjpu.auth.service.system;

import java.util.Map;

import com.tjpu.auth.model.system.OperateLogVO;

/**
 * @Author: zzc
 * @Date: 2018/7/11 19:01
 * @Description:日志service接口层
 */
public interface OperateLogService {
    /**
     * @author: zzc
     * @date: 2018/7/19 8:57
     * @Description: 添加日志方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: operateLogVO 日志实体类
     * @return: int
     */
    int insert(OperateLogVO operateLogVO);

    /**
     * 
     * @author: lip
     * @date: 2018年8月6日 下午2:01:34
     * @Description: 通过切面配置的插入方法插入数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param operateLogVO
     */
	void insertByAop(OperateLogVO operateLogVO);

	/**      
	 * @author: xsm
	 * @date: 2018年8月8日 下午3:16:25
	 * @Description:获取用户数据权限操作描述 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param oldMap:修改前的用户数据权限数据
	 * @param newMap:修改后的用户数据权限数据
	 * @return    
	 */
    String getUserDataPermissionsEditLog(Map<String, Object> oldMap,
			Map<String, Object> newMap);



	/**      
	 * @author: xsm
	 * @date: 2018年8月9日 下午3:13:19
	 * @Description:获取用户进行添加、修改、删除操作时的操作描述 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param operateType:操作类型
	 * @param tableName：操作的表的名称
	 * @param oldMapData：修改前数据
	 * @param newMapData：修改后数据
	 * @return    
	 */
	String doOperateLogVO(String operateType, String tableName,
			Map<String, Object> oldMapData, Map<String, Object> newMapData);

	/**      
	 * @author: xsm
	 * @date: 2018年8月9日 下午6:45:26
	 * @Description:通用日志存储，保存记录用户操作的日志信息 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param operateType:操作类型
	 * @param paramMap:自定义参数（tablename:被操作的表的表名称;resetUserId:被重置密码的用户的用户ID）
	 * @param compareDataMap:进行修改操作后的对比的数据
	 * @param oldMapData:修改前的数据（回显在修改页面的数据）
	 * @param newMapData:修改后的数据（点击保存时页面上的数据）    
	 */
	void saveUserOperationLog(String operateType,Map<String, Object> paramMap,Map<String, Object> compareDataMap,
			Map<String, Object> oldMapData,Map<String, Object> newMapData);

	/**      
	 * @author: Administrator
	 * @param tableName 
	 * @date: 2018年8月10日 下午2:05:52
	 * @Description: 
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param oldMaps
	 * @param newMaps
	 * @return    
	 */
	String fomateEditLog(String tableName, Map<String, Object> oldMaps,
			Map<String, Object> newMaps);
     
}
