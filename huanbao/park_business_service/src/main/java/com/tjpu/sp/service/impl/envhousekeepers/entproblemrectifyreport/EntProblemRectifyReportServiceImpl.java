package com.tjpu.sp.service.impl.envhousekeepers.entproblemrectifyreport;

import com.tjpu.sp.dao.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportMapper;
import com.tjpu.sp.model.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportVO;
import com.tjpu.sp.service.envhousekeepers.entproblemrectifyreport.EntProblemRectifyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EntProblemRectifyReportServiceImpl implements EntProblemRectifyReportService {
    @Autowired
    private EntProblemRectifyReportMapper entProblemRectifyReportMapper;

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:35
     *@Description: 通过自定义参数获取检查模板配置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getEntProblemRectifyReportByParamMap(Map<String,Object> param) {
        return entProblemRectifyReportMapper.getEntProblemRectifyReportByParamMap(param);
    }

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
    @Override
    public void insert(EntProblemRectifyReportVO entity) {
         entProblemRectifyReportMapper.insert(entity);
    }

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
    @Override
    public Map<String, Object> getEntProblemRectifyReportByID(String id) {
        return entProblemRectifyReportMapper.getEntProblemRectifyReportByID(id);
    }

    /**
     *@author: xsm
     *@date: 2021/07/09 0009 下午 13:31
     *@Description: 修改企业问题整改报告信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    @Override
    public void updateByPrimaryKey(EntProblemRectifyReportVO entity) {
        entProblemRectifyReportMapper.updateByPrimaryKey(entity);
    }

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
    @Override
    public void deleteByPrimaryKey(String id) {
        entProblemRectifyReportMapper.deleteByPrimaryKey(id);
    }

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
    @Override
    public Map<String, Object> getEntProblemRectifyReportDetailByID(String id) {
        return entProblemRectifyReportMapper.getEntProblemRectifyReportDetailByID(id);
    }


    @Override
    public List<Map<String, Object>> IsEntCheckReportValidByParam(Map<String, Object> paramMap) {
        return entProblemRectifyReportMapper.IsEntCheckReportValidByParam(paramMap);
    }
}
