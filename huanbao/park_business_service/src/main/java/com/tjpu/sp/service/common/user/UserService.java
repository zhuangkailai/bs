package com.tjpu.sp.service.common.user;

import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * @author: zhangzhenchao
     * @date: 2020/1/10 15:27
     * @Description: 获取部门用户树形结构
     * @param:
     * @return:
     */
    List<Map<String, Object>> getUserDepartmentTree();

    /**
     * @author: xsm
     * @date: 2020/04/29 0009 上午11:27
     * @Description: 根据部门ID获取相关用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getUserInfosByDepartmentId(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2020/5/7 0007 下午 3:14
     * @Description: 获取所有用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getAllUserInfo();
    /**
     *
     * @author: lip
     * @date: 2020/6/2 0002 下午 5:10
     * @Description: 更新用户密码操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    void updatePassword(Map<String, Object> paramMap);

    Map<String,Object> getUserInfoByAccount(String userAccount);


    /**
     * @author: chengzq
     * @date: 2020/12/14 0014 下午 4:33
     * @Description: 获取企业法人，环保负责人，通讯录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getMailListInfo(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/12/14 0014 下午 5:00
     * @Description: 获取用户通讯录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getPinYinUserMailListInfo(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2020/12/15 0015 下午 3:57
     * @Description: 获取用户通讯录按部门
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getDepartmentUserMailListInfo(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/1/25 16:59
     * @Description: 获取运维人员信息树
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDevOpsUserDepartmentTree();


    List<Map<String,Object>> getAllDepartmentUserMailListInfo(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskAssignUserData(Map<String,Object> param);

    Map<String,Object> getUserInfoByUserAccount(String assignuseraccount);

    /**
     * @author: xsm
     * @date: 2021/9/10 8:50
     * @Description: 根据菜单Code获取拥有该菜单权限的用户信息
     * @param:
     * @return:
     */
    List<String> getAllUserIdsByMenuCode(String menucode);
}
