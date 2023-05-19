package com.tjpu.sp.service.impl.environmentalprotection.constructionproject;

import com.tjpu.sp.dao.environmentalprotection.constructionproject.AcceptanceMapper;
import com.tjpu.sp.model.environmentalprotection.constructionproject.CheckVO;
import com.tjpu.sp.service.environmentalprotection.constructionproject.ProjectAcceptanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author: liyc
 * @date:2019/10/17 0017 8:34
 * @Description:${description}
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
@Service
@Transactional
public class ProjectAcceptanceServiceImpl implements ProjectAcceptanceService {
    @Autowired
    private AcceptanceMapper acceptanceMapper;
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 8:58
    *@Description: 获取项目验收信息列表+分页+条件查询
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [paramMap]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> getProjectAcceptanceListPage(Map<String, Object> paramMap) {
        return acceptanceMapper.getProjectAcceptanceListPage(paramMap);
    }
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 9:51
    *@Description: 往验收信息列表添加一条数据
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [checkVO]
    *@throws:
    **/
    @Override
    public void addProjectAcceptanceInfo(CheckVO checkVO) {
        acceptanceMapper.insert(checkVO);
    }
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
    @Override
    public void deleteProjectAcceptanceById(String pkCheckid) {
        acceptanceMapper.deleteByPrimaryKey(pkCheckid);
    }
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 10:49
    *@Description: 通过主键id获取验收详情信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [id]
    *@throws:
    **/
    @Override
    public Map<String, Object> getProjectAcceptanceDetailById(String id) {
        return acceptanceMapper.getProjectAcceptanceDetailById(id);
    }
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
    @Override
    public CheckVO getProjectAcceptanceById(String id) {
        return acceptanceMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/10/17 0017 13:29
    *@Description: 编辑保存验收信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [checkVO]
    *@throws:
    **/
    @Override
    public void updateProjectAcceptance(CheckVO checkVO) {
        acceptanceMapper.updateByPrimaryKey(checkVO);
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 16:17
    *@Description: 根据企业id统计环评验收的信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> countCheckNatureByPollutionId(String pollutionid) {
        return acceptanceMapper.countCheckNatureByPollutionId(pollutionid);
    }
}
