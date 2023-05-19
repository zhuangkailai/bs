package com.tjpu.auth.service.system;

import java.util.List;
import java.util.Map;

/**
 * @author: xsm
 * @date: 2018/7/6 14.15
 * @Description: 部门 sevice层 接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

public interface OrganizationService {
    /**
     * @author: xsm
     * @date: 2018/7/6 14:15
     * @Description: 获取部门树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOrganizationTreeData();
}
