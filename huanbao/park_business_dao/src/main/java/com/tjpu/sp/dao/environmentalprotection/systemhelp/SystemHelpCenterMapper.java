package com.tjpu.sp.dao.environmentalprotection.systemhelp;

import com.tjpu.sp.model.environmentalprotection.systemhelp.SystemHelpCenterVO;

import java.util.List;
import java.util.Map;

public interface SystemHelpCenterMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(SystemHelpCenterVO record);

    int insertSelective(SystemHelpCenterVO record);

    SystemHelpCenterVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(SystemHelpCenterVO record);

    int updateByPrimaryKey(SystemHelpCenterVO record);

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