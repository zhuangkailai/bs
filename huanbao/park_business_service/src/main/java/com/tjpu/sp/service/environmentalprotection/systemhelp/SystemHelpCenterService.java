package com.tjpu.sp.service.environmentalprotection.systemhelp;


import com.tjpu.sp.model.environmentalprotection.systemhelp.SystemHelpCenterVO;

import java.util.List;
import java.util.Map;

public interface SystemHelpCenterService {

    /**
     * @Author: xsm
     * @Date: 2020/02/14 9:31
     * @Description: 自定义条件查询系统问题帮助列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    List<Map<String,Object>> getSystemHelpInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @Author: xsm
     * @Date: 2020/02/14 9:31
     * @Description: 根据主键ID获取系统帮助详情信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    Map<String,Object> getSystemHelpInfoDetailByID(String id);

    /**
     * @Author: xsm
     * @Date: 2020/02/14 11:19
     * @Description: 新增系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    void insert(SystemHelpCenterVO systemHelpCenterVO);

    /**
     * @Author: xsm
     * @Date: 2020/02/14 11:20
     * @Description: 根据ID获取系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    SystemHelpCenterVO selectByPrimaryKey(String id);

    /**
     * @Author: xsm
     * @Date: 2020/02/14 11:21
     * @Description: 修改系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    void updateByPrimaryKey(SystemHelpCenterVO systemHelpCenterVO);

    /**
     * @Author: xsm
     * @Date: 2020/02/14 12:53
     * @Description: 删除系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2020/02/14 0014 下午 14:09
     * @Description: 获取所有系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> getAllSystemHelpInfos();
}
