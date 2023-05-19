package com.tjpu.sp.dao.common;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserMapper {

    /**
     * @author: zhangzhenchao
     * @date: 2020/1/10 15:27
     * @Description: 获取部门用户树形结构
     * @param:
     * @return:
     */
    List<Map<String,Object>> getDepartments();

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

    int updatePassword(Map<String, Object> paramMap);

    Map<String,Object> getUserInfoByAccount(String userAccount);

    Map<String,Object> getUserInfoByUserAccount(@Param("useraccount") String assignuserid);

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
     * @Description: 获取用户通讯录按字母
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
    List<Map<String,Object>> getAllDepartmentUserMailListInfo(Map<String, Object> paramMap);

    List<Map<String,Object>> getDevOpsUserDepartments(Map<String, Object> paramMap);

    List<Map<String,Object>> getTaskAssignUserData(Map<String,Object> param);

    List<Map<String,Object>> getAllUserInfoByMenuCode(@Param("menucode")String menucode);

    List<Map<String, Object>> getUserModuleDataListByParam(Map<String, Object> paramMap);

    void deleteModuleDataByUserid(String userid);

    void batchAddModuleData(List<Map<String, Object>> addList);
}
