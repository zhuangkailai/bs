package com.tjpu.sp.service.common;


import java.util.List;
import java.util.Map;

/**
 * @author: lip
 * @date: 2019/10/15 0015 上午 9:18
 * @Description: 用户权限接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public interface UserAuthSupportService {

    /**
     * @author: lip
     * @date: 2019/10/15 0015 上午 9:21
     * @Description: 通过菜单标记和会话ID获取用户按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String, Object> getUserButtonAuthBySysmodelAndSessionId(String sysmodel, String sessionId);

    List<Map<String, Object>> getUserModuleDataListByParam(Map<String, Object> paramMap);

    void batchAdd(String userid, List<String> moduleids);
}
