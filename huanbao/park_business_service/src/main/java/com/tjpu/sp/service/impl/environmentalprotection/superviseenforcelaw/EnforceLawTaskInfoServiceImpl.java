package com.tjpu.sp.service.impl.environmentalprotection.superviseenforcelaw;

import com.tjpu.sp.dao.environmentalprotection.superviseenforcelaw.TaskInfoMapper;
import com.tjpu.sp.model.environmentalprotection.superviseenforcelaw.TaskInfoVO;
import com.tjpu.sp.service.environmentalprotection.superviseenforcelaw.EnforceLawTaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnforceLawTaskInfoServiceImpl implements EnforceLawTaskInfoService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 1:38
     * @Description:根据自定义参数获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getEnforceLawTaskInfosByParamMap(Map<String, Object> paramMap) {
        return taskInfoMapper.getEnforceLawTaskInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:19
     * @Description:新增执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(TaskInfoVO taskInfoVO) {
        taskInfoMapper.insert(taskInfoVO);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:25
     * @Description:修改执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(TaskInfoVO taskInfoVO) {
        taskInfoMapper.updateByPrimaryKey(taskInfoVO);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:27
     * @Description:根据主键ID删除执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        taskInfoMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:30
     * @Description:根据主键ID获取执法任务详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getEnforceLawTaskInfoDetailByID(String pkid) {
        return taskInfoMapper.getEnforceLawTaskInfoDetailByID(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 4:00
     * @Description:获取执法任务表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getTableTitleForEnforceLawTaskInfo() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"任务编号", "任务名称", "发布时间", "办结期限", "任务执行人", "任务来源", "任务类型", "紧急程度"};
        String[] titlefiled = new String[]{"taskid", "taskname", "publishtime", "endtime", "executepersion", "tasksourcename", "tasktypename", "enerlvlname"};
        for (int i = 0; i < titlefiled.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", titlefiled[i]);
            map.put("label", titlename[i]);
            map.put("align", "center");
            tableTitleData.add(map);
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 4:00
     * @Description:根据id获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public TaskInfoVO selectByPrimaryKey(String id) {
        return taskInfoMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 18:44
    *@Description: 根据企业id统计监察执法信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @Override
    public List<Map<Object, Object>> countEnforceLawTaskByPollutionId(String pollutionid) {
        return taskInfoMapper.countEnforceLawTaskByPollutionId(pollutionid);
    }
}
