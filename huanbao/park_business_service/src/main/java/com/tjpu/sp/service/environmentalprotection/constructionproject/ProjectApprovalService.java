package com.tjpu.sp.service.environmentalprotection.constructionproject;

import com.tjpu.sp.model.environmentalprotection.constructionproject.ApprovalVO;

import java.util.List;
import java.util.Map; /**
 * @author: liyc
 * @date:2019/10/14 0014 14:08
 * @Description: 建设项目--项目审批信息模块实现层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface ProjectApprovalService {

    /**
    *@author:liyc
    *@date:2019/10/15 0015 9:21
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
    *@date:2019/10/15 0015 10:57
    *@Description: 通过主键id删除审批信息列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    */
    void deleteProjectApprovalById(String pkApprovalid);

    /**
    *@author:liyc
    *@date:2019/10/15 0015 13:25
    *@Description: 往审批信息列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [record]
    *@throws:
    */
    void addProjectApprovalInfo(ApprovalVO record);
    /**
    *@author:liyc
    *@date:2019/10/15 0015 15:26
    *@Description: 通过主键id获取详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    */
    Map<String,Object>getProjectApprovalDetailById(String id);
    /**
    *@author:liyc
    *@date:2019/10/15 0015 16:51
    *@Description: 编辑审批信息列表一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    void updateProjectApprovalInfo(ApprovalVO record);

    /**
    *@author: liyc
    *@date: 2019/10/16 0016 20:07
    *@Description: 编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    ApprovalVO getProjectApprovalById(String id);

    /**
    *@author: liyc
    *@date: 2019/11/5 0005 11:34
    *@Description: 根据企业id统计环评审批的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    List<Map<String,Object>> countApprovalNatureByPollutionId(String pollutionid);
}
