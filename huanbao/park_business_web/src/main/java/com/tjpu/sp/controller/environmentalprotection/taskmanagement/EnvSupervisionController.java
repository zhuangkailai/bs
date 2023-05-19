package com.tjpu.sp.controller.environmentalprotection.taskmanagement;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.SessionUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.AlarmRemindUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.ComplaintTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.DevOpsTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.TaskFlowRecordService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum;


/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年7月19日 上午9:31
 * @Description:环境监管管理接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("envSupervision")
public class EnvSupervisionController {

    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;
    @Autowired
    private ComplaintTaskDisposeService complaintTaskDisposeService;
    @Autowired
    private DevOpsTaskDisposeService devOpsTaskDisposeService;
    @Autowired
    private TaskFlowRecordService taskFlowRecordService;


    /**
     * @author: xsm
     * @date: 2019/7/19 0019 上午 9:32
     * @Description:统计环境监管模块下子级模块当天任务数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getEnvSupervisionChildMenusTaskNums", method = RequestMethod.POST)
    public Object getEnvSupervisionChildMenusTaskNums(@RequestJson(value = "menuid") String menuid,
                                                      @RequestJson(value = "sessionid", required = false) String sessionid) {
        try {
            if (StringUtils.isBlank(sessionid)) {//判断所传sessionid是否为空
                sessionid = SessionUtil.getRequest().getHeader("token");
            }
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionid, String.class);

            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionid, List.class);
            //根据菜单ID获取用户的报警菜单集合
            List<Map<String, Object>> childrenMenus = AlarmRemindUtil.getChildrenMenusByMenuId(menuid, objectList, null);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (childrenMenus == null || childrenMenus.size() == 0) {
                resultMap.put("sum", 0);
                resultMap.put("datalist", dataList);
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            }
            List<CommonTypeEnum.EnvSupervisionMenus> menuEnums = new ArrayList<>();
            getPcAqTaskAlarmMenuEnums(childrenMenus, menuEnums);
            //redis 获取菜单下的按钮权限
            Map<String, List<Map<String, Object>>> menus = RedisTemplateUtil.getRedisCacheDataByKey("usermenuandbuttonauth", sessionid, Map.class);
            int sum = 0;
            List<Map<String, Object>> ListMap = new ArrayList<>();
            if (menus != null && menus.size() > 0) {

                for (CommonTypeEnum.EnvSupervisionMenus menuEnum : menuEnums) {
                    String code = menuEnum.getCode();
                    int count = 0;
                    if (menus.containsKey(code.toLowerCase())) {
                        //新增菜单统计修改此处
                        count = getPCTaskRemindRightData(userid, code);
                        sum += count;
                        Map<String, Object> map = new HashMap<>();
                        map.put("sysmodel", code);
                        map.put("name", menuEnum.getName());
                        map.put("count", count);
                        ListMap.add(map);
                    }
                }
            }
            resultMap.put("sum", sum);
            resultMap.put("datalist", ListMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletypes",
                Arrays.asList(
                        CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.DevEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.TBEnum.getCode()
                ));
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/1 15:39
     * @Description: 获取需要报警的菜单枚举
     * @param:
     * @return:
     * @throws:
     */
    @SuppressWarnings("unchecked")
    private void getPcAqTaskAlarmMenuEnums(List<Map<String, Object>> childrenByMenuId,
                                           List<CommonTypeEnum.EnvSupervisionMenus> menus) {
        for (Map<String, Object> map : childrenByMenuId) {
            String menuCode = map.get("menucode").toString();
            CommonTypeEnum.EnvSupervisionMenus menuEnum = CommonTypeEnum.EnvSupervisionMenus.getCodeByString(menuCode);
            if (menuEnum != null) {
                menus.add(menuEnum);
            }
            Object children = map.get("datalistchildren");
            if (children != null) {
                childrenByMenuId = (List<Map<String, Object>>) children;
                getPcAqTaskAlarmMenuEnums(childrenByMenuId, menus);
            }
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/5/18 0018 下午 1:22
     * @Description: 获取人员资质证书数量 即合并安全管理人员资格证，特种作业操作证，职业健康证三种证书为一种的总数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [overduedataList]
     * @throws:
     */
    private int getPeopleQualificationCertificateSafetyNum(List<Map<String, Object>> overduedataList) {
        return overduedataList.stream().filter(m -> m.get("Code") != null && m.get("num") != null && ((m.get("Code").toString())
                .equals(CommonTypeEnum.CertificateManagementMenus.SafetyManagementCertificate.getType())
                || (m.get("Code").toString()).equals(CommonTypeEnum.CertificateManagementMenus.SpecialOperationCertificate.getType())
                || (m.get("Code").toString()).equals(CommonTypeEnum.CertificateManagementMenus.OccupationalHealthCertificate.getType())))
                .map(m -> Integer.valueOf(m.get("num").toString())).collect(Collectors.summingInt(a -> a));
    }


    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:05
     * @Description: 获取PC报警任务提醒数据（关联权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private int getPCTaskRemindRightData(String userId, String menuCode) {
        int num = 0;
        Map<String, Object> paramMap = new HashMap<>();
        //PC端任务处置模块 研发人员内部用  不根据按钮权限判断
        boolean isDassign = true;
        paramMap.put("userid", userId);
        num = getUnassignedAndUndisposedData(paramMap, menuCode, isDassign, userId);
        return num;
    }


    /**
     * @author: xsm
     * @date: 2019/8/24 10:58
     * @Description: 获取待分派和待处理数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private int getUnassignedAndUndisposedData(Map<String, Object> paramMap, String menuCode, Boolean isDassign, String userid) {
        List<Map<String, Object>> listData = new ArrayList<>();
        List<Map<String, Object>> alarmlistData = new ArrayList<>();
        int num = 0;
        CommonTypeEnum.EnvSupervisionMenus menuInfo = CommonTypeEnum.EnvSupervisionMenus.getCodeByString(menuCode);
        List<String> rightList = getRightList(userid);

        switch (menuInfo) {
            case alarmTaskManagement:    //报警任务处置管理 有数据权限控制
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                    paramMap.put("ishaveflag", "yes");
                } else {
                    paramMap.put("ishaveflag", "no");
                }

                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());
                alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataByParams(paramMap);
                paramMap.clear();
                break;
            case complaintTaskManagement:    //投诉任务处置管理
                if (isDassign) {
                    paramMap.remove("userid");
                    paramMap.put("taskstatus", CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode());
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode());
                    listData.addAll(complaintTaskDisposeService.getComplaintTaskDisposeNumDataByParams(paramMap));
                }
                paramMap.put("userid", userid);
                paramMap.put("taskstatus", CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode());
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode());
                listData.addAll(complaintTaskDisposeService.getComplaintTaskDisposeNumDataByParams(paramMap));
                if (listData != null) {
                    num += listData.size();
                }
                paramMap.clear();
                break;
            case TaskDisposeManagement:    //日常任务处置管理
                if (isDassign) {
                    paramMap.remove("userid");
                    paramMap.put("taskstatus", CommonTypeEnum.DaliyTaskEnum.UnassignedTaskEnum.getCode());
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
                    listData.addAll(alarmTaskDisposeService.getTaskDisposeNumDataByParams(paramMap));
                }
                paramMap.put("userid", userid);
                paramMap.put("taskstatus", CommonTypeEnum.DaliyTaskEnum.UndisposedEnum.getCode());
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
                listData = alarmTaskDisposeService.getTaskDisposeNumDataByParams(paramMap);
                if (listData != null) {
                    num += listData.size();
                }
                paramMap.clear();
                break;
            case operateTaskManagement:    //运维任务处置管理 有数据权限 控制
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                    paramMap.put("ishaveflag", "yes");
                } else {
                    paramMap.put("ishaveflag", "no");
                }
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
                alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataByParams(paramMap);
                paramMap.clear();
                break;
            case mutationTaskManagement:    //突变任务处置管理 有数据权限 控制
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.TBEnum.getCode())) {
                    paramMap.put("ishaveflag", "yes");
                } else {
                    paramMap.put("ishaveflag", "no");
                }
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode());
                alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataByParams(paramMap);
                paramMap.clear();
                break;
            case alarmtaskmanagementsafety:    //安全报警任务 有数据权限控制
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode());
                alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataByParams(paramMap);
                paramMap.clear();
                break;
            case operatetaskmanagementsafety:    //安全运维任务 有数据权限控制
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode());
                alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataByParams(paramMap);
                paramMap.clear();
                break;
            case watchtaskmanage:    //监控任务 有数据权限控制
                paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode());
                alarmlistData = alarmTaskDisposeService.getAssignAndUndisposedTaskNumDataByParams(paramMap);
                paramMap.clear();
                break;
        }
        if (alarmlistData != null && alarmlistData.size() > 0) {
            for (Map<String, Object> map : alarmlistData) {
                if (map.get("num") != null && !"".equals(map.get("num").toString())) {
                    num += Integer.valueOf(map.get("num").toString());
                }
            }
        }

        return num;
    }


    /**
     * @author: zhangzc
     * @date: 2019/7/17 16:57
     * @Description: 处理父子结构菜单，变为单一结构
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> formatMenus(List<Map<String, Object>> menuChildren, List<Map<String, Object>> resultList) {
        for (Map<String, Object> menuChild : menuChildren) {
            Map<String, Object> menuMap = new HashMap<>();
            menuChild.forEach(menuMap::put);
            menuMap.remove("datalistchildren");
            resultList.add(menuMap);
            Object info = menuChild.get("datalistchildren");
            if (info != null) {
                List<Map<String, Object>> datalistchildren = (List<Map<String, Object>>) info;
                if (datalistchildren.size() > 0) {
                    formatMenus(datalistchildren, resultList);
                }
            }
        }
        return resultList;
    }

    /**
     * @author: zhangzc
     * @date: 2019/7/18 13:23
     * @Description: 通过menuid获取子菜单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getChildrenByMenuId(String menuid, List<JSONObject> objectList, List<Map<String, Object>> dataList) {
        if (objectList != null && dataList == null) {
            for (JSONObject jsonObject : objectList) {
                if (menuid.equals(jsonObject.get("menuid").toString())) {
                    dataList = (List<Map<String, Object>>) jsonObject.get("datalistchildren");
                    break;
                } else {
                    objectList = (List<JSONObject>) jsonObject.get("datalistchildren");
                    dataList = getChildrenByMenuId(menuid, objectList, dataList);
                }
            }
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2019/09/07 0007 上午 11:21
     * @Description:首页任务提醒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTaskRemindDataFromRedis", method = RequestMethod.POST)
    public Object getTaskRemindDataFromRedis(@RequestJson(value = "sessionid", required = false) String sessionid) {
        try {
            if (StringUtils.isBlank(sessionid)) {
                sessionid = SessionUtil.getRequest().getHeader("token");
            }
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> taskMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionid, String.class);
            Map<String, List<Map<String, Object>>> menus = RedisTemplateUtil.getRedisCacheDataByKey("usermenuandbuttonauth", sessionid, Map.class);
            if (StringUtils.isNotBlank(userId)) {
                //投诉任务数


                Map<String, Object> complainttask = getComplaintTaskRemindRightData(userId);

                List<Integer> tasktypes = new ArrayList<>();
                tasktypes.add(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());//报警任务
                tasktypes.add(CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());//日常任务
                tasktypes.add(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());//运维任务

                paramMap.put("tasktypes", tasktypes);
                List<Map<String, Object>> wfp_tasklist = alarmTaskDisposeService.countUnassignedTaskDataNum(paramMap);
                paramMap.put("userid", userId);
                List<Map<String, Object>> otherstatus_tasklist = alarmTaskDisposeService.countOtherStatusTaskDataNumForUserID(paramMap);
                //判断是否有权限 并获取各状态任务数量
                List<String> rightList = getRightList(userId);
                boolean isHaveAlarm = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                    isHaveAlarm = true;
                }
                Map<String, Object> alarmtask = getTaskStatsDataNum(wfp_tasklist, otherstatus_tasklist, CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode().toString(), isHaveAlarm);
                Map<String, Object> dailytask = getTaskStatsDataNum(wfp_tasklist, otherstatus_tasklist, CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode().toString(), true);
                boolean isHaveDev = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                    isHaveDev = true;
                }
                Map<String, Object> devopstask = getTaskStatsDataNum(wfp_tasklist, otherstatus_tasklist, CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString(), isHaveDev);
                taskMap.put("alarmtask", alarmtask);
                taskMap.put("complainttask", complainttask);
                taskMap.put("dailytask", dailytask);
                taskMap.put("devopstask", devopstask);
                resultMap.put("userid", userId);
                resultMap.put("taskdata", taskMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> getTaskStatsDataNum(List<Map<String, Object>> wfp_tasklist, List<Map<String, Object>> otherstatus_tasklist, String code, boolean isDassign) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("needassign", 0);
        resultMap.put("needfeedback", 0);
        resultMap.put("feedbacking", 0);
        resultMap.put("hasclose", 0);
        if (isDassign) {
            if (wfp_tasklist != null && wfp_tasklist.size() > 0) {
                for (Map<String, Object> map : wfp_tasklist) {
                    //任务类型相同
                    if (map.get("FK_TaskType") != null && code.equals(map.get("FK_TaskType").toString())) {
                        resultMap.put("needassign", map.get("num") != null ? map.get("num") : 0);
                    }
                }
            }
        }
        if (otherstatus_tasklist != null && otherstatus_tasklist.size() > 0) {
            for (Map<String, Object> map : otherstatus_tasklist) {
                //任务类型相同
                if (map.get("FK_TaskType") != null && code.equals(map.get("FK_TaskType").toString())) {
                    if (map.get("TaskStatus") != null) {
                        String status = map.get("TaskStatus").toString();
                        if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(status)) {
                            //待处理
                            resultMap.put("needfeedback", map.get("num"));
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(status)) {
                            //处理中
                            resultMap.put("feedbacking", map.get("num"));
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(status)) {
                            //已完成
                            resultMap.put("hasclose", map.get("num"));
                        }
                    }
                }
            }
        }

        return resultMap;
    }


    /**
     * @author: xsm
     * @date: 2019/10/30 0030 下午 7:43
     * @Description: 获取首页超标报警任务、日常任务报警数量
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getAlarmTaskRemindRightData(String userId, boolean isDassign, String type, String tasktype, String starttime, String endtime, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        paramMap.put("userid", userId);
        paramMap.put("type", type);
        paramMap.put("tasktype", tasktype);
        if (starttime != null && !"".equals(starttime)) {
            paramMap.put("starttime", starttime);
        }
        if (endtime != null && !"".equals(endtime)) {
            paramMap.put("endtime", endtime);
        }

        List<Map<String, Object>> listdata = new ArrayList<>();
        if (isDassign) {
            paramMap.put("flag", "hasauthority");
            listdata = alarmTaskDisposeService.countTaskDisposeNumGroupByStatusByParams(paramMap);
        } else {
            paramMap.put("flag", "noauthority");
            listdata = alarmTaskDisposeService.countTaskDisposeNumGroupByStatusByParams(paramMap);
        }
        resultMap.put("needassign", 0);
        resultMap.put("needfeedback", 0);
        resultMap.put("feedbacking", 0);
        resultMap.put("hasclose", 0);
        if (listdata != null && listdata.size() > 0) {
            for (Map<String, Object> map : listdata) {
                String status = map.get("TaskStatus").toString();
                if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode().toString()).equals(status)) {
                    //待分派
                    resultMap.put("needassign", map.get("num"));
                } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(status)) {
                    //待处理
                    resultMap.put("needfeedback", map.get("num"));
                } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(status)) {
                    //处理中
                    resultMap.put("feedbacking", map.get("num"));
                } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(status)) {
                    //已完成
                    resultMap.put("hasclose", map.get("num"));
                }
            }
        }
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2019/10/30 0030 下午 7:43
     * @Description: 获取首页投诉任务报警数量
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getComplaintTaskRemindRightData(String userId) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        paramMap.put("userid", userId);
        List<Map<String, Object>> listdata = complaintTaskDisposeService.countTaskDisposeNumGroupByStatusByParams(paramMap);
        resultMap.put("needassign", 0);
        resultMap.put("needfeedback", 0);
        resultMap.put("feedbacking", 0);
        resultMap.put("hasclose", 0);
        if (listdata != null && listdata.size() > 0) {
            for (Map<String, Object> map : listdata) {
                String status = map.get("TaskStatus").toString();
                if ((CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode().toString()).equals(status)) {
                    resultMap.put("needassign", map.get("num"));
                } else if ((CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode().toString()).equals(status)) {
                    //待处理
                    resultMap.put("needfeedback", map.get("num"));
                } else if ((CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getCode().toString()).equals(status)) {
                    //处理中
                    resultMap.put("feedbacking", map.get("num"));
                } else if ((CommonTypeEnum.ComplaintTaskEnum.CompletedEnum.getCode().toString()).equals(status)) {
                    //已完成
                    resultMap.put("hasclose", map.get("num"));
                }
            }
        }
        return resultMap;
    }


    /**
     * @author: xsm
     * @date: 2019/12/13 0013 上午 9:13
     * @Description: 获取首页运维任务报警数量
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getDevOpsTaskRemindRightData(String userId, boolean isDassign, String tasktype, String starttime, String endtime, Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        paramMap.put("userid", userId);
        paramMap.put("type", "devops");
        paramMap.put("tasktype", tasktype);
        if (starttime != null && !"".equals(starttime)) {
            paramMap.put("starttime", starttime);
        }
        if (endtime != null && !"".equals(endtime)) {
            paramMap.put("endtime", endtime);
        }
        List<Map<String, Object>> listdata = new ArrayList<>();
        if (isDassign) {
            paramMap.put("flag", "hasauthority");
            listdata = alarmTaskDisposeService.countTaskDisposeNumGroupByStatusByParams(paramMap);
        } else {
            paramMap.put("flag", "noauthority");
            listdata = alarmTaskDisposeService.countTaskDisposeNumGroupByStatusByParams(paramMap);
        }
        resultMap.put("needassign", 0);
        resultMap.put("needfeedback", 0);
        resultMap.put("feedbacking", 0);
        resultMap.put("hasclose", 0);
        if (listdata != null && listdata.size() > 0) {
            for (Map<String, Object> map : listdata) {
                String status = map.get("TaskStatus").toString();
                if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode().toString()).equals(status)) {
                    //待分派
                    resultMap.put("needassign", map.get("num"));
                } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(status)) {
                    //待处理
                    resultMap.put("needfeedback", map.get("num"));
                } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(status)) {
                    //处理中
                    resultMap.put("feedbacking", map.get("num"));
                } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(status)) {
                    //已完成
                    resultMap.put("hasclose", map.get("num"));
                }
            }
        }
        return resultMap;
    }

    /**
     * @author: lip
     * @date: 2019/8/28 0028 上午 9:30
     * @Description: 判断是否有按钮权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean getIsHaveButton(Object userButtonAuthInMenu, String buttonCode) {
        boolean isHaveButton = false;
        if (userButtonAuthInMenu != null) {
            Map<String, Object> objectMap = (Map<String, Object>) userButtonAuthInMenu;
            if (!"".equals(objectMap.get("data"))) {
                Map<String, Object> dataMap = (Map<String, Object>) objectMap.get("data");
                if (!"".equals(dataMap.get("tablebuttondata"))) {
                    List<Map<String, Object>> buttons = (List<Map<String, Object>>) dataMap.get("tablebuttondata");
                    if (buttons != null && buttons.size() > 0) {
                        for (Map<String, Object> button : buttons) {
                            if (buttonCode.equals(button.get("type"))) {
                                isHaveButton = true;
                                break;
                            }
                        }
                    }

                }
            }
        }
        return isHaveButton;
    }

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 下午 13:10
     * @Description: 根据自定义参数获取某一段时间内所有日期的任务完成情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTaskSituationByParamMap", method = RequestMethod.POST)
    public Object getTaskSituationByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                             @RequestJson(value = "endtime", required = false) String endtime,
                                             @RequestJson(value = "tasktype", required = false) String tasktype,
                                             @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                             @RequestJson(value = "hasdataauthor", required = false) Boolean hasdataauthor, HttpSession session
    ) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //判断配置文件是查询环保或安全数据
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> types = new ArrayList<>();
            if (tasktype != null && !"".equals(tasktype)) {
                paramMap.put("tasktypes", Arrays.asList(tasktype));
            } else {//任务类型为空 默认根据配置查询任务  且不需要 数据权限
                if (categorys.contains("1")) {
                    types.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
                }
                if (categorys.contains("2")) {
                    types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
                }
            }
            paramMap.put("tasktypes", types);
            paramMap.put("monitorpointtype", monitorpointtype);
            //已完成  状态
            List<Map<String, Object>> resultlist = alarmTaskDisposeService.getTaskSituationByParamMap(paramMap);
            List<String> ymds = DataFormatUtil.getYMDBetween(starttime, endtime);
            ymds.add(endtime);
            List<Map<String, Object>> result = new ArrayList<>();
            for (String str : ymds) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", str);
                map.put("alarmflag", 0);
                if (resultlist != null && resultlist.size() > 0) {
                    for (Map<String, Object> obj : resultlist) {
                        if (str.equals(obj.get("TaskCreateTime").toString())) {
                            map.put("alarmflag", obj.get("alarmflag"));
                        }
                    }
                }
                result.add(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 10:18
     * @Description: 统计(报警, 运维)任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countTaskCompletionStatusByParamMap", method = RequestMethod.POST)
    public Object countTaskCompletionStatusByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                      @RequestJson(value = "endtime", required = false) String endtime,
                                                      @RequestJson(value = "tasktype", required = false) String tasktype,
                                                      @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            if (tasktype != null && !"".equals(tasktype)) {
                paramMap.put("tasktypes", Arrays.asList(tasktype));
            } else {//任务类型为空 根据配置文件 获取要查询的任务类型
                List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
                List<Integer> types = new ArrayList<>();
                if (categorys.contains("1")) {
                    types.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
                }
                if (categorys.contains("2")) {
                    types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
                }
                paramMap.put("tasktypes", types);
            }
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("completions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
            paramMap.put("uncompletions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode(), CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode()));
            Map<String, Object> resultMap = alarmTaskDisposeService.countAlarmTaskCompletionStatusByParamMap(paramMap);
            resultMap.put("completionrate", "");
            resultMap.put("completionvalue", "");
            if (resultMap != null) {
                int completionnum = 0;
                int total = Integer.parseInt(resultMap.get("totalnum").toString());
                if (total > 0) {
                    if (resultMap.get("completed") != null) {
                        completionnum = Integer.parseInt(resultMap.get("completed").toString());
                        resultMap.put("completionrate", DataFormatUtil.SaveTwoAndSubZero(100d * completionnum / total) + "%");
                        resultMap.put("completionvalue", Double.parseDouble(DataFormatUtil.SaveTwoAndSubZero((double) completionnum / total)));
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/1/13 0013 上午 10:12
     * @Description: 根据时间范围获取报警任务信息（数据分析）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTaskListDataByTaskTypeAndMonitorTimes", method = RequestMethod.POST)
    public Object getTaskListDataByTaskTypeAndMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime,
                                                           @RequestJson(value = "tasktype") Integer tasktype,
                                                           @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                           @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("tasktype", tasktype);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> resultmap = new HashMap<>();
            if (tasktype == CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode()) {
                resultmap = alarmTaskDisposeService.getAlarmTaskListDataByParam(paramMap);
            } else if (tasktype == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                resultmap = devOpsTaskDisposeService.getDevOpsTaskListDataByParam(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/18 0018 上午 10:23
     * @Description: 根据时间范围统计报警任务和运维任务条数（数据分析）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countTaskListDataNumByMonitorTimes", method = RequestMethod.POST)
    public Object countTaskListDataNumByMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        try {
            Map<String, Object> param = new HashMap<>();
            Map<String, Object> resultmap = new HashMap<>();
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("hasauthor", "1");
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> types = new ArrayList<>();
            if (categorys.contains("1")) {
                types.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode()}));
            }
            if (categorys.contains("2")) {
                types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode(),
                        CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode()}));
            }
            param.put("tasktypes", types);
            Long alarmtasknum = alarmTaskDisposeService.countAllOverAlarmTaskCountByParams(param);
            //运维
            types.clear();
            if (categorys.contains("1")) {
                types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()}));
            }
            if (categorys.contains("2")) {
                types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
            }
            param.put("tasktype", types);
            Long devtasknum = alarmTaskDisposeService.countAllOverAlarmTaskCountByParams(param);
            //突变
            types.clear();
            types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()}));
            param.put("tasktype", types);
            Long tbtasknum = alarmTaskDisposeService.countAllOverAlarmTaskCountByParams(param);
            resultmap.put("alarmtasknum", alarmtasknum);
            resultmap.put("devopstasknum", devtasknum);
            resultmap.put("tbtasknum", tbtasknum);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 上午 12:05
     * @Description:根据任务类型和自定义参数获取报警、运维任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getTaskListDataByTaskTypeAndParamMap", method = RequestMethod.POST)
    public Object getTaskListDataByTaskTypeAndParamMap(@RequestJson(value = "tasktype") String tasktype,
                                                       @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                       @RequestJson(value = "dgimn") String dgimn,
                                                       @RequestJson(value = "taskid", required = false) String taskid,
                                                       @RequestJson(value = "starttime", required = false) String starttime,
                                                       @RequestJson(value = "endtime", required = false) String endtime,
                                                       @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                       @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                       HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("dgimn", dgimn);
            paramMap.put("tasktype", tasktype);
            paramMap.put("taskid", taskid);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> resultdata = alarmTaskDisposeService.getTaskListDataByTaskTypeAndParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/28 0028 下午 16:23
     * @Description:根据任务类型统计该类型任务的时效性
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countTaskDisposeTimelinessNumByTaskType", method = RequestMethod.POST)
    public Object countTaskDisposeTimelinessNumByTaskType(@RequestJson(value = "tasktype") String tasktype
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tasktype", tasktype);
            List<Map<String, Object>> resultdata = alarmTaskDisposeService.countTaskDisposeTimelinessNumByTaskType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/02 0002 下午 16:36
     * @Description:各任务状态任务数量统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countTaskNumGroupByTaskStatusByParamMap", method = RequestMethod.POST)
    public Object countTaskNumGroupByTaskStatusByParamMap(@RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                          @RequestJson(value = "tasktype") String tasktype,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointid", monitorpointid);
            List<String> types = new ArrayList<>();
            types.add(tasktype);
            paramMap.put("tasktypelist", types);
            List<Map<String, Object>> resultdata = alarmTaskDisposeService.countTaskNumGroupByTaskStatusByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/02 0002 下午 16:58
     * @Description:根据时间和任务类型统计每日任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countTaskNumGroupByMonitorTimeByTaskType", method = RequestMethod.POST)
    public Object countTaskNumGroupByMonitorTimeByTaskType(@RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                           @RequestJson(value = "tasktype") String tasktype,
                                                           @RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointid", monitorpointid);
            List<String> types = new ArrayList<>();
            types.add(tasktype);
            paramMap.put("tasktypelist", types);
            List<Map<String, Object>> resultdata = alarmTaskDisposeService.countTaskNumGroupByMonitorTimeByTaskType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/3/03 0003 上午 8:56
     * @Description:根据自定义查询条件统计处置人员任务完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countTaskNumGroupByDisposerPeopleByParamMap", method = RequestMethod.POST)
    public Object countTaskNumGroupByDisposerPeopleByParamMap(@RequestJson(value = "tasktype") String tasktype,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //判断配置文件是查询环保或安全数据
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> typelist = new ArrayList<>();
            if (tasktype.equals(AlarmTaskEnum.getCode().toString())) {
                if (categorys.contains("1")) {
                    typelist.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode()}));
                }
                if (categorys.contains("2")) {
                    typelist.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode()}));
                }
                paramMap.put("tasktypelist", typelist);
            } else if (tasktype.equals(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString())) {
                if (categorys.contains("1")) {
                    typelist.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()}));
                }
                if (categorys.contains("2")) {
                    typelist.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
                }
                paramMap.put("tasktypelist", typelist);
            } else {
                paramMap.put("tasktype", tasktype);
            }
            List<Map<String, Object>> resultdata = alarmTaskDisposeService.countTaskNumGroupByDisposerPeopleByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/13 0013 下午 4:31
     * @Description: 自定义查询条件根据运维公司统计任务数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countTaskNumGroupByCompanyByParamMap", method = RequestMethod.POST)
    public Object countTaskNumGroupByCompanyByParamMap(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //判断配置文件是查询环保或安全数据
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> typelist = new ArrayList<>();
            if (categorys.contains("1")) {
                typelist.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()}));
            }
            if (categorys.contains("2")) {
                typelist.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
            }
            paramMap.put("tasktypelist", typelist);
            List<Map<String, Object>> dataList = alarmTaskDisposeService.countTaskNumGroupByCompanyByParamMap(paramMap);
            if (dataList.size() > 0) {
                Map<String, Integer> nameAndTotalNum = new HashMap<>();
                Map<String, Integer> nameAndCompleteNum = new HashMap<>();
                String companyname;
                int countnum;
                for (Map<String, Object> dataMap : dataList) {
                    companyname = dataMap.get("Organization_Name").toString();
                    countnum = dataMap.get("countnum") != null ? Integer.parseInt(dataMap.get("countnum").toString()) : 0;
                    if (nameAndTotalNum.containsKey(companyname)) {
                        nameAndTotalNum.put(companyname, nameAndTotalNum.get(companyname) + countnum);
                    } else {
                        nameAndTotalNum.put(companyname, countnum);
                    }
                    if ("已完成".equals(dataMap.get("status"))) {
                        if (nameAndCompleteNum.containsKey(companyname)) {
                            nameAndCompleteNum.put(companyname, nameAndCompleteNum.get(companyname) + countnum);
                        } else {
                            nameAndCompleteNum.put(companyname, countnum);
                        }
                    }
                }
                if (nameAndTotalNum.size() > 0) {
                    int totalnum;
                    for (String company : nameAndTotalNum.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("companyname", company);
                        totalnum = nameAndTotalNum.get(company);
                        resultMap.put("totalnum", totalnum);
                        countnum = nameAndCompleteNum.get(company) != null ? nameAndCompleteNum.get(company) : 0;
                        resultMap.put("completednum", countnum);
                        if (countnum > 0) {
                            resultMap.put("completedrate", DataFormatUtil.SaveOneAndSubZero(100D * countnum / totalnum) + "%");
                        } else {
                            resultMap.put("completedrate", "0%");
                        }
                        resultList.add(resultMap);
                    }
                    //排序
                    resultList = resultList.stream().sorted(Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("totalnum").toString())).reversed()).collect(Collectors.toList());
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/03/09 0009 上午 11:24
     * @Description:当天超标、异常处置工单统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countOnlineTaskNumGroupByTaskStatusByParamMap", method = RequestMethod.POST)
    public Object countOnlineTaskNumGroupByTaskStatusByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                                @RequestJson(value = "endtime", required = false) String endtime,
                                                                @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                                @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                                @RequestJson(value = "sysmodel", required = false) String sysmodel,
                                                                HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> taskMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            Map<String, List<Map<String, Object>>> menus = RedisTemplateUtil.getRedisCacheDataByToken("usermenuandbuttonauth", Map.class);
            if (StringUtils.isNotBlank(userId)) {

                List<String> rightList = getRightList(userId);
                boolean isHaveAlarm = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                    isHaveAlarm = true;
                }
                //报警任务数
                Map<String, Object> alarmtask = getAlarmTaskRemindRightData(userId, isHaveAlarm, "alarm", CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode().toString(), starttime, endtime, paramMap);
                //运维任务数
                boolean isHaveDev = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                    isHaveDev = true;
                }
                Map<String, Object> devopstask = getDevOpsTaskRemindRightData(userId, isHaveDev, CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString(), starttime, endtime, paramMap);
                taskMap.put("alarmtask", alarmtask);
                taskMap.put("devopstask", devopstask);
            }
            return AuthUtil.parseJsonKeyToLower("success", taskMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getTaskFlowRecordInfoByParamMap", method = RequestMethod.POST)
    public Object getTaskFlowRecordInfoByParamMap(@RequestJson(value = "taskid") String taskid,
                                                  @RequestJson(value = "tasktype") Integer tasktype
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("taskid", taskid);
            paramMap.put("tasktype", tasktype);
            List<Map<String, Object>> taslflow = taskFlowRecordService.getTaskFlowRecordInfoByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", taslflow);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/5/15 0015 下午 13:17
     * @Description: 统计某一段时间内报警及运维工单各状态情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmAndDevopsTaskStatusNumByMonitorTimes", method = RequestMethod.POST)
    public Object countAlarmAndDevopsTaskStatusNumByMonitorTimes(@RequestJson(value = "starttime", required = false) String starttime,
                                                                 @RequestJson(value = "endtime", required = false) String endtime,
                                                                 HttpSession session
    ) throws Exception {
        try {
            String sessionid = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //paramMap.put("userid", userId);
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> types = new ArrayList<>();
            if (types.size() == 0) {//无数据权限
                if (categorys.contains("1")) {
                    types.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
                }
                if (categorys.contains("2")) {
                    types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
                }
            }

            paramMap.put("tasktypes", types);
            //已完成  状态
            List<Map<String, Object>> resultlist = alarmTaskDisposeService.countAlarmAndDevopsTaskStatusNumByMonitorTimes(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/1/13 0013 上午 10:12
     * @Description: 根据时间范围获取工单信息（超标、运维）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAlarmAndDevOpsTaskListDataByMonitorTimes", method = RequestMethod.POST)
    public Object getAlarmAndDevOpsTaskListDataByMonitorTimes(@RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime,
                                                              @RequestJson(value = "tasktype", required = false) Integer tasktype,
                                                              @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                              @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                              HttpSession session
    ) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("hasauthor", "1");
            paramMap.put("feedbackuserid", userId);
            //判断配置文件是查询环保或安全数据
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            if (categorys.contains("1")) {//环保
                if (tasktype != null) {
                    if (tasktype == CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode()) {
                        paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());
                        List<Map<String, Object>> alarmlist = alarmTaskDisposeService.getAllAlarmTaskByParamForHome(paramMap);
                        if (alarmlist != null) {
                            result.addAll(alarmlist);
                        }
                    } else if (tasktype == CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()) {
                        //突变
                        paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode());
                        List<Map<String, Object>> tb_alarmlist = alarmTaskDisposeService.getAllAlarmTaskByParamForHome(paramMap);
                        if (tb_alarmlist != null) {
                            for (Map<String, Object> map : tb_alarmlist) {
                                map.put("tasktypename", "突变工单");
                            }
                            result.addAll(tb_alarmlist);
                        }
                    } else if (tasktype == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                        //运维
                        paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
                        List<Map<String, Object>> devtaskresult = devOpsTaskDisposeService.getAllDevOpsTaskByParamForHome(paramMap);
                        if (devtaskresult != null) {
                            result.addAll(devtaskresult);
                        }
                    }
                } else {
                    //查所有
                    //报警任务
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());
                    List<Map<String, Object>> alarmlist = alarmTaskDisposeService.getAllAlarmTaskByParamForHome(paramMap);
                    if (alarmlist != null) {
                        result.addAll(alarmlist);
                    }
                    //突变
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode());
                    List<Map<String, Object>> tb_alarmlist = alarmTaskDisposeService.getAllAlarmTaskByParamForHome(paramMap);
                    if (tb_alarmlist != null) {
                        for (Map<String, Object> map : tb_alarmlist) {
                            map.put("tasktypename", "突变工单");
                        }
                        result.addAll(tb_alarmlist);
                    }
                    //运维
                    paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
                    List<Map<String, Object>> devtaskresult = devOpsTaskDisposeService.getAllDevOpsTaskByParamForHome(paramMap);
                    if (devtaskresult != null) {
                        result.addAll(devtaskresult);
                    }
                }
            }
            if (result != null && result.size() > 0) {
                Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("TaskCreateTime").toString()).reversed();
                List<Map<String, Object>> collect = result.stream().sorted(comparebytime).collect(Collectors.toList());
                return AuthUtil.parseJsonKeyToLower("success", collect);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "countTaskRemindDataFromRedis", method = RequestMethod.POST)
    public Object countTaskRemindDataFromRedis() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> taskMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (StringUtils.isNotBlank(userId)) {
                //投诉任务数


                Map<String, Object> complainttask = getComplaintTaskRemindRightData(userId);
                List<Integer> tasktypes = new ArrayList<>();
                tasktypes.add(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());//报警任务
                tasktypes.add(CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());//日常任务
                tasktypes.add(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());//运维任务
                paramMap.put("tasktypes", tasktypes);
                List<Map<String, Object>> wfp_tasklist = alarmTaskDisposeService.countUnassignedTaskDataNum(paramMap);
                paramMap.put("userid", userId);
                List<Map<String, Object>> otherstatus_tasklist = alarmTaskDisposeService.countOtherStatusTaskDataNumForUserID(paramMap);
                //判断是否有权限 并获取各状态任务数量

                List<String> rightList = getRightList(userId);
                boolean isHaveAlarm = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                    isHaveAlarm = true;
                }
                Map<String, Object> alarmtask = getTaskStatsDataNum(wfp_tasklist, otherstatus_tasklist, CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode().toString(), isHaveAlarm);
                Map<String, Object> dailytask = getTaskStatsDataNum(wfp_tasklist, otherstatus_tasklist, CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode().toString(), true);
                boolean isHaveDev = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                    isHaveDev = true;
                }
                Map<String, Object> devopstask = getTaskStatsDataNum(wfp_tasklist, otherstatus_tasklist, CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString(), isHaveDev);
                int petitionnum = (complainttask.get("needassign") == null ? 0 : Integer.valueOf(complainttask.get("needassign").toString())) + (complainttask.get("needfeedback") == null ? 0 : Integer.valueOf(complainttask.get("needfeedback").toString())) + (complainttask.get("feedbacking") == null ? 0 : Integer.valueOf(complainttask.get("feedbacking").toString()));
                int alarmtasknum = (alarmtask.get("needassign") == null ? 0 : Integer.valueOf(alarmtask.get("needassign").toString())) + (alarmtask.get("needfeedback") == null ? 0 : Integer.valueOf(alarmtask.get("needfeedback").toString())) + (alarmtask.get("feedbacking") == null ? 0 : Integer.valueOf(alarmtask.get("feedbacking").toString()));
                int dailytasknum = (dailytask.get("needassign") == null ? 0 : Integer.valueOf(dailytask.get("needassign").toString())) + (dailytask.get("needfeedback") == null ? 0 : Integer.valueOf(dailytask.get("needfeedback").toString())) + (dailytask.get("feedbacking") == null ? 0 : Integer.valueOf(dailytask.get("feedbacking").toString()));
                int devopstasknum = (devopstask.get("needassign") == null ? 0 : Integer.valueOf(devopstask.get("needassign").toString())) + (devopstask.get("needfeedback") == null ? 0 : Integer.valueOf(devopstask.get("needfeedback").toString())) + (devopstask.get("feedbacking") == null ? 0 : Integer.valueOf(devopstask.get("feedbacking").toString()));
                taskMap.put("petitionnum", petitionnum);
                taskMap.put("alarmtasknum", alarmtasknum);
                taskMap.put("dailytasknum", dailytasknum);
                taskMap.put("devopstasknum", devopstasknum);
            }
            return AuthUtil.parseJsonKeyToLower("success", taskMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
