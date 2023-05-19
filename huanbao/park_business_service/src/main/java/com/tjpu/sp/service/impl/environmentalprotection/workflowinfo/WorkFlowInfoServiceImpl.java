package com.tjpu.sp.service.impl.environmentalprotection.workflowinfo;

import com.tjpu.sp.dao.environmentalprotection.workflowinfo.WorkFlowInfoMapper;
import com.tjpu.sp.model.environmentalprotection.workflowinfo.WorkFlowInfoVO;
import com.tjpu.sp.service.environmentalprotection.workflowinfo.WorkFlowInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class WorkFlowInfoServiceImpl implements WorkFlowInfoService {

    @Autowired
    private WorkFlowInfoMapper workFlowInfoMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return workFlowInfoMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(WorkFlowInfoVO record) {
        return workFlowInfoMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return workFlowInfoMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public Map<String, Object> selectByWorkFlowType(String fkworkflowtype) {
        return workFlowInfoMapper.selectByWorkFlowType(fkworkflowtype);
    }

    @Override
    public int updateByPrimaryKey(WorkFlowInfoVO record) {
        return workFlowInfoMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateByWorkFlowType(WorkFlowInfoVO record) {
        return workFlowInfoMapper.updateByWorkFlowType(record);
    }


    /**
     * @author: chengzq
     * @date: 2021/05/07 0016 下午 2:38
     * @Description:  通过自定义参数获取工作流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWorkFlowInfoByParamMap(Map<String, Object> paramMap) {
        return workFlowInfoMapper.getWorkFlowInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2021/05/07 0016 下午 2:38
     * @Description: 通过id获取工作流程详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getWorkFlowInfoDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = workFlowInfoMapper.getWorkFlowInfoByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }

}
