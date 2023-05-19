package com.tjpu.auth.service.codeTable;

import com.tjpu.auth.model.codeTable.CommonSelectTableConfigVO;

import java.util.Map;

/**
 * @author: zzc
 * @date: 2018/4/2212:11
 * @Description:service层表操作接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public interface CommonSelectTableConfigService {


    /**
     * @author: zhangzc
     * @date: 2018/6/3 11:57
     * @Description: 表名称获取配置表实体
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    CommonSelectTableConfigVO getTableConfigByName(String tableName);

    CommonSelectTableConfigVO getTableConfigVOBySysModel(String sysModel);

    /**
     * 
     * @author: lip
     * @date: 2019/1/7 0007 上午 10:15
     * @Description: 判断指定表是否有自增字段，是：返回1，否：返回0
     * @updateUser: 
     * @updateDate: 
     * @updateDescription: 
     * @param: 
     * @return: 
    */
    int getTableHasIdentity(String tableName);

    /**
     * 
     * @author: lip
     * @date: 2019/1/7 0007 上午 10:41
     * @Description: 根据表名称获取主键为数字类型的表主键最大值
     * @updateUser: 
     * @updateDate: 
     * @updateDescription: 
     * @param: 
     * @return: 
    */
    int getMaxNumByTableName(Map<String,Object> paramMap);
}
