package com.tjpu.sp.service.impl.environmentalprotection.taskmanagement;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.petition.PetitionInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.ComplaintTaskDisposeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ComplaintTaskDisposeServiceImpl implements ComplaintTaskDisposeService {

    @Autowired
    private PetitionInfoMapper petitionInfoMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;


    //任务类型  投诉
    private final String tasktype = "2";

    /**
     * @author: xsm
     * @date: 2019/8/6 0006 下午 1:38
     * @Description: 获取投诉任务处置管理信息（有分派按钮权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllComplaintTaskDisposeListDataByParamMap(Map<String, Object> paramMap) {
        String theuserid = paramMap.get("userid").toString();
        List<String> statuslist = new ArrayList<>();
        if (paramMap.get("statuslist") != null) {
            statuslist = (List<String>) paramMap.get("statuslist");
        }
        //获取所有报警任务信息
        paramMap.put("feedbackuserid", theuserid);

        List<Map<String, Object>> datalist = petitionInfoMapper.getComplaintTaskDisposeDataByParams(paramMap);
        List<Map<String, Object>> resultlist = new ArrayList<>();
        List<Map<String, Object>> applist = new ArrayList<>();
        if (datalist != null && datalist.size() > 0) {//查找有无该企业在当天的任务信息
            for (Map<String, Object> objectMap : datalist) {
                if (paramMap.get("sign") != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pk_id", objectMap.get("PK_ID"));
                    map.put("petitiontitle", objectMap.get("PetitionTitle"));
                    map.put("submittime", objectMap.get("SubmitTime"));
                    applist.add(map);
                } else {
                    String status = objectMap.get("Status") != null ? objectMap.get("Status").toString() : "";
                    if (objectMap.get("isfeedbackuserid") != null) {
                        objectMap.put("isfeedback", "1");
                        objectMap.remove("isfeedbackuserid");
                    } else {
                        objectMap.put("isfeedback", "0");
                        objectMap.remove("isfeedbackuserid");
                    }
                    if ((CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode().toString()).equals(status)) {
                        //待分派
                        objectMap.put("taskstatuname", CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getName());
                    } else if ((CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode().toString()).equals(status)) {
                        //待处理
                        objectMap.put("taskstatuname", CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getName());
                    } else if ((CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getCode().toString()).equals(status)) {
                        //处理中
                        objectMap.put("taskstatuname", CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getName());
                    } else if ((CommonTypeEnum.ComplaintTaskEnum.CompletedEnum.getCode().toString()).equals(status)) {
                        //已完成
                        objectMap.put("taskstatuname", CommonTypeEnum.ComplaintTaskEnum.CompletedEnum.getName());
                    }
                    if (statuslist != null && statuslist.size() > 0 && !"".equals(status)) {
                        if (statuslist.contains(status)) {
                            if ((CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode().toString()).equals(status)) {
                                resultlist.add(objectMap);
                            } else {//待分派 已完成 已忽略
                                if (objectMap.get("iscommentuserid") != null && !"".equals(objectMap.get("iscommentuserid").toString())) {
                                    resultlist.add(objectMap);
                                }
                            }
                        }
                    } else {
                        resultlist.add(objectMap);
                    }
                }
            }
        }
        if (paramMap.get("sign") != null) {
            return applist;
        } else {
            return resultlist;
        }
    }





    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 11:29
     * @Description: 分派投诉任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public void saveComplaintTaskInfo(String userid, String username, Map<String, Object> formdata) {
        try {
            String taskid = formdata.get("pk_id").toString();
            List<String> userids = (List<String>) formdata.get("userids");
            //添加任务处置信息
            //根据ID获取投诉事件信息
            PetitionInfoVO petitionInfoVO = petitionInfoMapper.selectByPrimaryKey(taskid);
            int statuscode = CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode();
            petitionInfoVO.setStatus((short) statuscode);//任务状态
            petitionInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));//更新时间
            petitionInfoVO.setUpdateuser(username);//更新人
            petitionInfoMapper.updateByPrimaryKey(petitionInfoVO);
            //任务分派人
            //添加任务处置记录信息
            Calendar calendar = Calendar.getInstance();
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());//主键ID
            obj.setFkTaskid(taskid);//任务ID
            obj.setFkTaskhandleuserid(userid);//分派人用户ID
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum.getName().toString());//任务状态
            obj.setFkTasktype(tasktype);//任务类型
            obj.setTaskhandletime(calendar.getTime());//任务分派时间
            taskFlowRecordInfoMapper.insert(obj);
            //在当前时间的基础上添加一秒
            calendar.add(Calendar.SECOND, 1);
            // 处置人信息
            for (String str : userids) {
                //添加任务处置记录信息
                TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
                taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());//主键ID
                taskFlowRecordInfo.setFkTaskid(taskid);//任务ID
                taskFlowRecordInfo.setFkTaskhandleuserid(str.toString());//被分派该任务的处置人ID
                taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName().toString());//任务状态
                taskFlowRecordInfo.setFkTasktype(tasktype);//任务类型
                taskFlowRecordInfo.setTaskhandletime(calendar.getTime());//被分派该任务的时间
                taskFlowRecordInfoMapper.insert(taskFlowRecordInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 7:02
     * @Description: 获取报警任务处置管理表头信息(导出)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getTableTitleForComplaintTask() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"投诉事件", "投诉日期", "污染开始时间", "污染结束时间", "持续时长（分钟）", "处置人", "状态"};
        String[] titlefiled = new String[]{"petitiontitle", "submittime", "pollutestarttime", "polluteendtime", "duration", "user_name", "taskstatuname"};
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
     * @date: 2019/8/6 0006 下午 5:27
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void addComplaintTaskStatusToHandle(String id, String userId, String username) {
        //获取报警任务处置详情信息
        PetitionInfoVO petitionInfoVO = petitionInfoMapper.selectByPrimaryKey(id);
        //根据任务ID获取处理过该任务的相关人员信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(id);
        boolean isdisposaluser = false;//判断是否是被分派处理该任务的处置人
        boolean isdisposal = false;//判断该任务是否有人正在处理
        for (Map<String, Object> obj : datalist) {
            //待处理 可能为多个用户
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                if (userId.equals(obj.get("FK_TaskHandleUserID").toString())) {//判断当前用户是否为被分派处理该任务的处置人
                    isdisposaluser = true;
                }
            }
            //处理中
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                //已有人处置该任务
                isdisposal = true;
            }
        }
        //判断并记录任务状态信息为处置中
        if (isdisposaluser == true) {//是该任务的处理人
            if (isdisposal == false) {//任务还未处理
                //修改任务状态
                int statuscode = CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getCode();
                petitionInfoVO.setStatus((short) statuscode);
                petitionInfoVO.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                petitionInfoVO.setUpdateuser(username);
                int i = petitionInfoMapper.updateByPrimaryKey(petitionInfoVO);
                if (i > 0) {
                    //任务处理人
                    //添加处理人信息
                    TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
                    obj.setPkId(UUID.randomUUID().toString());
                    obj.setFkTaskid(id);
                    obj.setFkTaskhandleuserid(userId);
                    obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.SuperviseEnum.getName().toString());
                    obj.setFkTasktype(tasktype);
                    obj.setTaskhandletime(new Date());
                    taskFlowRecordInfoMapper.insert(obj);
                }
            }
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/12 0012 下午 3:42
     * @Description: 自定义查询条件获取任务处置数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTaskDisposeNumDataByParams(Map<String, Object> paramMap) {
        return petitionInfoMapper.getTaskDisposeNumDataByParams(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/24 0024 下午 1:17
     * @Description: 自定义查询条件获取某个状态的投诉任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getComplaintTaskDisposeNumDataByParams(Map<String, Object> paramMap) {
        return petitionInfoMapper.getComplaintTaskDisposeNumDataByParams(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:02
     * @Description:保存投诉任务反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateAlarmTaskInfo(String userId, PetitionInfoVO petitionInfo) {
        try {
            short status = (short) (Integer.parseInt(CommonTypeEnum.ComplaintTaskEnum.CompletedEnum.getCode().toString()));
            petitionInfo.setStatus(status);
            petitionInfoMapper.updateByPrimaryKey(petitionInfo);
            //任务反馈人
            //添加任务处置记录信息
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(petitionInfo.getPkId());
            obj.setFkTaskhandleuserid(userId);
            obj.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.FeedbackEnum.getName().toString());
            obj.setFkTasktype(CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode().toString());
            obj.setTaskhandletime(new Date());
            taskFlowRecordInfoMapper.insert(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:25
     * @Description:根据投诉任务ID获取投诉任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public PetitionInfoVO selectByPrimaryKey(String pk_id) {
        return petitionInfoMapper.selectByPrimaryKey(pk_id);
    }

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:42
     * @Description:暂存投诉任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void temporaryTaskInfo(PetitionInfoVO petitionInfo) {
        try {
            petitionInfoMapper.updateByPrimaryKey(petitionInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:53
     * @Description:获取投诉任务暂存信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getComplaintTaskTemporaryInfo(String pk_taskid, String userId, String currentuser) {
        Map<String, Object> result = new HashMap<>();
        //获取报警任务处置详情信息
        PetitionInfoVO petitionInfo = petitionInfoMapper.selectByPrimaryKey(pk_taskid);
        //根据任务ID获取处理过该任务的相关人员信息
        List<Map<String, Object>> datalist = taskFlowRecordInfoMapper.getTaskFlowRecordInfoByTaskID(pk_taskid);
        boolean ishandleuser = false;//判断该处理中任务是否为该用户
        for (Map<String, Object> obj : datalist) {
            //待处理
            if (obj.get("CurrentTaskStatus") != null && (CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName()).equals(obj.get("CurrentTaskStatus").toString())) {
                //已有人处置该任务
                if (userId.equals(obj.get("FK_TaskHandleUserID").toString())) {//判断当前用户是否为被分派处理该任务的处置人
                    ishandleuser = true;
                }
            }
        }
        if (petitionInfo.getStatus() != null) {//判断是否为处理中任务
            int i = petitionInfo.getStatus();
            int n = CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getCode();
            if (i == n) {
                //是处理中的状态
                //判断
                if (ishandleuser == true) {//是该任务的处理人
                    result.put("pk_id", petitionInfo.getPkId());
                    result.put("completetime", petitionInfo.getCompletetime()!=null?(DataFormatUtil.getDateYMD(DataFormatUtil.getDateYMD(petitionInfo.getCompletetime()))):"");
                    result.put("completereply", petitionInfo.getCompletereply());
                    result.put("fileid", petitionInfo.getFkFileid());
                    result.put("undertakedepartment", petitionInfo.getUndertakedepartment());
                } else {
                    result.put("pk_id", petitionInfo.getPkId());
                    result.put("completetime", "");
                    result.put("completereply", "");
                    result.put("fileid", "");
                    result.put("undertakedepartment", "");
                }
            } else {//不是处理中 显示全部信息
                result.put("pk_id", petitionInfo.getPkId());
                result.put("completetime", "");
                result.put("completereply", "");
                result.put("fileid", "");
                result.put("undertakedepartment", "");
            }
        }

        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/10/31 0031 上午 11:13
     * @Description:获取投诉任务各状态任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> countTaskDisposeNumGroupByStatusByParams(Map<String, Object> paramMap) {
        return petitionInfoMapper.countTaskDisposeNumGroupByStatusByParams(paramMap);
    }

}
