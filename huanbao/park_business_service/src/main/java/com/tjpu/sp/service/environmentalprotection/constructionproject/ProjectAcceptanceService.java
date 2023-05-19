package com.tjpu.sp.service.environmentalprotection.constructionproject;

import com.tjpu.sp.model.environmentalprotection.constructionproject.CheckVO;

import java.util.List;
import java.util.Map; /**
 * @author: liyc
 * @date:2019/10/17 0017 8:33
 * @Description: 建设项目--项目验收信息模块实现层接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public interface ProjectAcceptanceService {
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 8:57
    *@Description: 获取项目验收信息列表+分页+条件查询
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    List<Map<String,Object>> getProjectAcceptanceListPage(Map<String, Object> paramMap);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 9:50
    *@Description: 往验收信息列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [checkVO]
    *@throws:
    **/
    void addProjectAcceptanceInfo(CheckVO checkVO);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 10:14
    *@Description: 通过主键id删除验收信息列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    void deleteProjectAcceptanceById(String pkCheckid);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 10:48
    *@Description: 通过主键id获取验收详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    Map<String,Object> getProjectAcceptanceDetailById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 11:54
    *@Description: 项目验收编辑回显
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    CheckVO getProjectAcceptanceById(String id);
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 13:28
    *@Description: 编辑保存验收信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [checkVO]
    *@throws:
    **/
    void updateProjectAcceptance(CheckVO checkVO);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 16:16
    *@Description: 根据企业id统计环评验收的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> countCheckNatureByPollutionId(String pollutionid);
}
