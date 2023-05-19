package com.tjpu.auth.service.system;

import java.util.List;
import java.util.Map;

/**
 * @author: zhangzc
 * @date: 2018/6/2 19:04
 * @Description: 角色 sevice层 接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

public interface RolesService {
    /**
     * @author: zhangzc
     * @date: 2018/6/21 13:50
     * @Description: 获取角色树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getRoleTreeData();

     /**
       * @Author: zhangzc
       * @Date: 2018/9/27 12:45
       * @Description: 根据角色id集合获取角色拥有的功能权限
       * @UpdateUser:
       * @UpdateDate:
       * @UpdateDescription:
       * @Param:
       * @Return:
       */
    List<String> getRoleAuthByRoleIDs(List<String> roleids);

    /**
     * @Author: zhangzc
     * @Date: 2018/9/27 12:45
     * @Description: 根据角色id获取角色拥有的功能权限
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<String> getRoleAuthByRoleID(String id);
}
