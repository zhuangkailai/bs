package com.tjpu.sp.service.impl.environmentalprotection.taskmanagement;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.TaskFlowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskFlowRecordServiceImpl implements TaskFlowRecordService {

    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;
    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;


    @Override
    public int insert(TaskFlowRecordInfoVO record) {
        return taskFlowRecordInfoMapper.insert(record);
    }

    @Override
    public int updateByPrimaryKey(TaskFlowRecordInfoVO record) {
        return taskFlowRecordInfoMapper.updateByPrimaryKey(record);
    }

    /**
     * @author: xsm
     * @date: 2020/03/13 0013 下午 15:38
     * @Description:根据任务ID和任务类型获取该任务的流程信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTaskFlowRecordInfoByParamMap(Map<String, Object> paramMap) {
        //获取报警任务处置详情
        AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = alarmTaskDisposeManagementMapper.selectByPrimaryKey(paramMap.get("taskid").toString());
        //根据任务ID获取处理过该任务的相关人员信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByParamMap(paramMap);
        List<Map<String, Object>> resultlist = new ArrayList<>();
        String username = "";
        Object taskhandletime = null;
        Map<String, Object> map0 = new HashMap<>();
        //根据当前任务状态信息 拼装任务流程
        //1.生成任务
        map0.put("username", "");
        map0.put("taskstatus", "生成任务");
        map0.put("taskhandletime", alarmTaskDisposeManagementVO.getTaskcreatetime());
        resultlist.add(map0);
        boolean flag = false;
        for (Map<String, Object> obj : datalist) {
            //2.分派任务
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("username", obj.get("User_Name"));
                map1.put("taskstatus", CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName());
                map1.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map1);
            }
            //3.待处理 可能为多个用户，拼接用户名
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                username = username + obj.get("User_Name") + "、";
                taskhandletime = DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime"));
            }
            //4.处理中
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("username", obj.get("User_Name"));
                map1.put("taskstatus", CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName());
                map1.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map1);
            }
            //5.反馈信息
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.FeedbackEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map3 = new HashMap<>();
                map3.put("username", obj.get("User_Name"));
                map3.put("taskstatus", CommonTypeEnum.AlarmTaskStatusEnum.FeedbackEnum.getName());
                map3.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map3);
                flag = true;
            }
            //6.忽略任务
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.NeglectTaskEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                Map<String, Object> map3 = new HashMap<>();
                map3.put("username", obj.get("User_Name"));
                map3.put("taskstatus", CommonTypeEnum.AlarmTaskStatusEnum.NeglectTaskEnum.getName());
                map3.put("taskhandletime", DataFormatUtil.getDateYMDHMS((Date) obj.get("TaskHandleTime")));
                resultlist.add(map3);
            }
        }
        if (!"".equals(username)) {
            username = username.substring(0, username.length() - 1);
        }
        //3.待处理
        Map<String, Object> map2 = new HashMap<>();
        map2.put("username", username);
        map2.put("taskstatus", CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName());
        map2.put("taskhandletime", taskhandletime);
        if (!"".equals(username)) {
            resultlist.add(map2);
        }
        //按时间正序排
        Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("taskhandletime").toString());
        List<Map<String, Object>> collect = resultlist.stream().sorted(comparebytime).collect(Collectors.toList());
        //判断任务是否已有反馈信息，有则返回状态 完成任务
        if (flag == true) {
            Map<String, Object> map4 = new HashMap<>();
            map4.put("username", "");
            map4.put("taskstatus", "完成任务");
            map4.put("taskhandletime", "");
            collect.add(map4);
        }
        return collect;
    }
}
