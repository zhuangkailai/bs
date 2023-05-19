package com.tjpu.auth.service.impl.codeTable;

import com.tjpu.auth.dao.codeTable.CommonSelectTableConfigMapper;
import com.tjpu.auth.model.codeTable.CommonSelectTableConfigVO;
import com.tjpu.auth.service.codeTable.CommonSelectTableConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author: zzc
 * @date: 2018/4/2212:11
 * @Description:表操作实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@Transactional
public class CommonSelectTableConfigServiceImpl implements CommonSelectTableConfigService {
    @Autowired
    private CommonSelectTableConfigMapper commonSelectTableConfigMapper;

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
    @Override
    public CommonSelectTableConfigVO getTableConfigByName(String tableName) {
        return commonSelectTableConfigMapper.getTableConfigByName(tableName);
    }

    @Override
    public CommonSelectTableConfigVO getTableConfigVOBySysModel(String sysModel) {
        return commonSelectTableConfigMapper.getTableConfigVOBySysModel(sysModel);
    }

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
    @Override
    public int getTableHasIdentity(String tableName) {
        return commonSelectTableConfigMapper.getTableHasIdentity(tableName);
    }

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
    @Override
    public int getMaxNumByTableName(Map<String,Object> paramMap) {
        return commonSelectTableConfigMapper.getMaxNumByTableName(paramMap);
    }

}
