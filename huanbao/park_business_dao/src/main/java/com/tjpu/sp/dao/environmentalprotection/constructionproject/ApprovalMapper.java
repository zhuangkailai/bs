package com.tjpu.sp.dao.environmentalprotection.constructionproject;

import com.tjpu.sp.model.environmentalprotection.constructionproject.ApprovalVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface ApprovalMapper {
    int deleteByPrimaryKey(String pkApprovalid);

    int insert(ApprovalVO record);

    int insertSelective(ApprovalVO record);

    ApprovalVO selectByPrimaryKey(String pkApprovalid);

    int updateByPrimaryKeySelective(ApprovalVO record);

    int updateByPrimaryKey(ApprovalVO record);

    /**
    *@author:liyc
    *@date:2019/10/15 0015 9:22
    *@Description: 获取项目审批信息列表+分页+条件查询
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    List<Map<String,Object>> getProjectApprovalInfoListPage(Map<String, Object> paramMap);

    /**
    *@author:liyc
    *@date:2019/10/15 0015 15:28
    *@Description: 通过主键id获取详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    */
    Map<String,Object> getProjectApprovalDetailById(String id);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 11:35
    *@Description: 根据企业id统计环评审批的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    List<Map<String,Object>> countApprovalNatureByPollutionId(String pollutionid);
}