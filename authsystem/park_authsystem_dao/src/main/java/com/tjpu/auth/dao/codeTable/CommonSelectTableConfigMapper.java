package com.tjpu.auth.dao.codeTable;

import com.tjpu.auth.model.codeTable.CommonSelectTableConfigVO;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author: zhangzc
 * @date: 2018/4/22 12:16
 * @Description: 持久层表操作接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param
 * @return
 */
@Repository
public interface CommonSelectTableConfigMapper {
    int deleteByPrimaryKey(String pkTableconfigid);

    int insert(CommonSelectTableConfigVO record);

    int insertSelective(CommonSelectTableConfigVO record);

    CommonSelectTableConfigVO selectByPrimaryKey(String pkTableConfigId);

    int updateByPrimaryKeySelective(CommonSelectTableConfigVO record);

    int updateByPrimaryKey(CommonSelectTableConfigVO record);

    /**
     * @author: zhangzc
     * @date: 2018/6/6 11:35
     * @Description: 根据表名称查询表配置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
	CommonSelectTableConfigVO getTableConfigByName(@Param("tablename") String tableName);



    CommonSelectTableConfigVO getTableConfigVOBySysModel(@Param("sysmodel") String sysModel);
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
    int getTableHasIdentity(@Param("tableName") String tableName);

    /**
     *
     * @author: lip
     * @date: 2019/1/7 0007 上午 10:42
     * @Description: 根据表名称获取主键为数字类型的表主键最大值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    int getMaxNumByTableName(Map<String,Object> paramMap);
}