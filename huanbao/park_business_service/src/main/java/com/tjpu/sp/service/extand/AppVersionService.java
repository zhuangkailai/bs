package com.tjpu.sp.service.extand;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/9/25 0025 11:39
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface AppVersionService {
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
     *@date:2019/9/25 0025 14:01
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
    *@date:2019/9/29 0029 10:41
    *@Description: 新增App版本管理信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [param]
    *@throws:
    */
    Object addAppVersion(String param);
    /**
    *@author: liyc
    *@date:2019/9/29 0029 11:18
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
    *@date:2019/9/29 0029 11:43
    *@Description: 修改app版本管理信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [param]
    *@throws:
    */
    Object updateAppVersion(String param);

    String getAppVersionMaxVersion(Map<String, Object> paramMap);

}
