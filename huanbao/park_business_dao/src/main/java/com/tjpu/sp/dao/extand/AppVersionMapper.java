package com.tjpu.sp.dao.extand;

import com.tjpu.sp.model.extand.AppVersionVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppVersionMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(AppVersionVO record);

    int insertSelective(AppVersionVO record);

    AppVersionVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(AppVersionVO record);

    int updateByPrimaryKey(AppVersionVO record);
    /**
     *
     * @author: lip
     * @date: 2019/9/25 0025 下午 1:26
     * @Description: 获取最新app版本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     * @param paramMap
     */
    List<Map<String,Object>> getLastAppVersionInfo(Map<String, Object> paramMap);
    /**
     *@author: liyc
     *@date:2019/9/25 0025 14:05
     *@Description: 获取app初始化列表信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:No [param]
     *@throws:
     */
    List<Map<String, Object>> getVersionListByParam(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date:2019/9/29 0029 10:45
    *@Description: 新增app版本管理信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [param]
    *@throws:
    */
    Object addAppVersion(String param);
    /**
    *@author: liyc
    *@date:2019/9/29 0029 11:22
    *@Description: 通过主键id删除app版本管理一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [param]
    *@throws:
    */
    Object deleteAppVersionByID(String param);
    /**
    *@author: liyc
    *@date:2019/9/29 0029 11:45
    *@Description: 修改app版本管理信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [param]
    *@throws:
    */
    Object updateAppVersion(String param);

    String getAppVersionMaxVersion(Map<String, Object> paramMap);

    /**
    *@author:liyc
    *@date:2019/10/11 0011 14:30
    *@Description: 获取最大的版本号
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    //List<Map<String,Object>> getAppVersionMaxVersion(Map<String, Object> paramMap);
}