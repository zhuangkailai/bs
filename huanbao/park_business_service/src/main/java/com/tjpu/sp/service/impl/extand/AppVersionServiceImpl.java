package com.tjpu.sp.service.impl.extand;


import com.tjpu.sp.dao.extand.AppVersionMapper;
import com.tjpu.sp.service.extand.AppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/9/25 0025 11:50
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class AppVersionServiceImpl implements AppVersionService {
    @Autowired
    private AppVersionMapper appVersionMapper;

    /**
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
    @Override
    public List<Map<String, Object>> getLastAppVersionInfo(Map<String, Object> paramMap) {
        return appVersionMapper.getLastAppVersionInfo(paramMap);
    }

    /**
     * @author: liyc
     * @date:2019/9/25 0025 14:02
     * @Description: 获取app初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:No [param]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getVersionListByParam(Map<String, Object> paramMap) {
        return appVersionMapper.getVersionListByParam(paramMap);
    }

    /**
     * @author: liyc
     * @date:2019/9/29 0029 10:43
     * @Description: 新增App版本管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */

    @Override
    public Object addAppVersion(String param) {
        return appVersionMapper.addAppVersion(param);
    }

    /**
     * @author: liyc
     * @date:2019/9/29 0029 11:21
     * @Description: 通过主键id删除app版本管理一条数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public Object deleteAppVersionByID(String param) {
        return appVersionMapper.deleteAppVersionByID(param);
    }

    /**
     * @author: liyc
     * @date:2019/9/29 0029 11:45
     * @Description: 修改app版本管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public Object updateAppVersion(String param) {
        return appVersionMapper.updateAppVersion(param);
    }

    @Override
    public String getAppVersionMaxVersion(Map<String, Object> paramMap) {
        return appVersionMapper.getAppVersionMaxVersion(paramMap);
    }

    /**
     * @author:liyc
     * @date:2019/10/11 0011 14:29
     * @Description: 获取最大的版本号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    /**@Override
    public List<Map<String, Object>> getAppVersionMaxVersion(Map<String, Object> paramMap) {
        List<Map<String, Object>> appVersionMaxVersion = appVersionMapper.getAppVersionMaxVersion(paramMap);
        //String s = appVersionMaxVersion.toString();
        //String s1 = s.substring(s.lastIndexOf(".")+1, s.length());
        return appVersionMaxVersion;
    }*/


}
