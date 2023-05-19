package com.tjpu.sp.service.envhousekeepers.entproblemrectifyreport;

import com.tjpu.sp.model.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportVO;

import java.util.List;
import java.util.Map;

public interface EntProblemRectifyReportService {

    /**
    *@author: xsm
    *@date: 2021/07/09 0009 上午 11:47
    *@Description: 通过自定义参数查询企业问题整改报告信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getEntProblemRectifyReportByParamMap(Map<String,Object> param);

    /**
     *@author: xsm
     *@date: 2021/07/09 0009 下午 13:25
     *@Description: 新增企业问题整改报告信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    void insert(EntProblemRectifyReportVO entity);

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 13:08
     * @Description: 通过id获取企业问题整改报告信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    Map<String,Object> getEntProblemRectifyReportByID(String id);

    /**
     *@author: xsm
     *@date: 2021/07/09 0009 下午 13:30
     *@Description: 修改企业问题整改报告信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    void updateByPrimaryKey(EntProblemRectifyReportVO entity);

    /**
     *@author: xsm
     *@date: 2021/07/09 0009 下午 13:38
     *@Description: 根据主键ID删除企业问题整改报告信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2021/07/09 0009 下午 13:40
     * @Description: 通过id获取企业问题整改报告详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    Map<String,Object> getEntProblemRectifyReportDetailByID(String id);

    List<Map<String,Object>> IsEntCheckReportValidByParam(Map<String, Object> paramMap);
}
