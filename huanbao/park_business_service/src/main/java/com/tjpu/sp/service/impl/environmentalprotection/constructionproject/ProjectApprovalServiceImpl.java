package com.tjpu.sp.service.impl.environmentalprotection.constructionproject;

import com.tjpu.sp.dao.environmentalprotection.constructionproject.ApprovalMapper;
import com.tjpu.sp.model.environmentalprotection.constructionproject.ApprovalVO;
import com.tjpu.sp.service.environmentalprotection.constructionproject.ProjectApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/14 0014 14:10
 * @Description: 建设项目--项目审批信息模块实现层实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class ProjectApprovalServiceImpl implements ProjectApprovalService {
    @Autowired
    private ApprovalMapper approvalMapper;

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
    @Override
    public List<Map<String, Object>> getProjectApprovalInfoListPage(Map<String, Object> paramMap) {
        return approvalMapper.getProjectApprovalInfoListPage(paramMap);
    }

    /**
    *@author:liyc
    *@date:2019/10/15 0015 10:58
    *@Description: 通过主键id删除审批信息列表的单条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    */
    @Override
    public void deleteProjectApprovalById(String pkApprovalid) {
        approvalMapper.deleteByPrimaryKey(pkApprovalid);
    }
    /**
    *@author:liyc
    *@date:2019/10/15 0015 13:28
    *@Description: 往审批信息列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [record]
    *@throws:
    */
    @Override
    public void addProjectApprovalInfo(ApprovalVO record) {
        approvalMapper.insert(record);
    }
    /**
    *@author:liyc
    *@date:2019/10/15 0015 15:27
    *@Description: 通过主键id获取详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    */
    @Override
    public Map<String, Object> getProjectApprovalDetailById(String id) {
        return approvalMapper.getProjectApprovalDetailById(id);
    }
    /**
    *@author:liyc
    *@date:2019/10/15 0015 16:52
    *@Description: 编辑审批信息列表一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    */
    @Override
    public void updateProjectApprovalInfo(ApprovalVO record) {
        approvalMapper.updateByPrimaryKey(record);
    }
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
    @Override
    public ApprovalVO getProjectApprovalById(String id) {
        return approvalMapper.selectByPrimaryKey(id);
    }
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
    @Override
    public List<Map<String, Object>> countApprovalNatureByPollutionId(String pollutionid) {
        return approvalMapper.countApprovalNatureByPollutionId(pollutionid);
    }
}
