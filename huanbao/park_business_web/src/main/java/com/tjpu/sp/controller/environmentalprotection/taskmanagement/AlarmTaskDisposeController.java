package com.tjpu.sp.controller.environmentalprotection.taskmanagement;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.model.extand.TextMessageVO;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.SocketCommonService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.ComplaintTaskDisposeService;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.AlarmTaskStatusEnum.GenerateTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.SocketTypeEnum.HomePageMonitorTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.*;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年7月15日 下午7:36
 * @Description:报警任务处置管理接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("alarmTaskDisposeManagement")
public class AlarmTaskDisposeController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;

    @Autowired
    private AuthSystemMicroService authSystemMicroService;
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;
    @Autowired
    private ComplaintTaskDisposeService complaintTaskDisposeService;
    @Autowired
    private JGUserRegisterInfoService jgUserRegisterInfoService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private SocketCommonService socketCommonService;


    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;

    private String sysmodel = "alarmTaskManagement";
    private String suddenChangeSysmodel = "mutationTaskManagement";

    //反馈按钮
    private String buttoncode = "feedbackButton";


    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: mmt
     * @date: 2022/8/23
     * @Description:根据自定义参数获取报警、运维、突变、日常任务数统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countTaskByParamMap", method = RequestMethod.POST)
    public Object countTaskByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                      @RequestJson(value = "endtime", required = false) String endtime,
                                      @RequestJson(value = "showcurrentuser", required = false) Integer showcurrentuser,
                                      @RequestJson(value = "csstatus", required = false) Integer csstatus,
                                      @RequestJson(value = "statuslist", required = false) List<String> statuslist
    ) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("showcurrentuser", showcurrentuser);
            paramMap.put("csstatus", csstatus);
            paramMap.put("statuslist", statuslist);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            //显示我已审核数据
            if (statuslist != null && statuslist.size() > 0 && statuslist.contains("-1")) {
                statuslist = statuslist == null ? new ArrayList<>() : statuslist;
                paramMap.put("isAudit", 1);
                statuslist.remove("-1");
                paramMap.put("statuslist", statuslist);
            }
            return AuthUtil.parseJsonKeyToLower("success", alarmTaskDisposeService.countTaskByParamMap(paramMap));
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 上午 8:43
     * @Description:根据自定义参数获取报警任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAlarmTaskDisposeManagementListDataByParamMap", method = RequestMethod.POST)
    public Object getAlarmTaskDisposeManagementListDataByParamMap(@RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                                  @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                                  @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                                  @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                                  @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                                  @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                                  @RequestJson(value = "starttime", required = false) String starttime,
                                                                  @RequestJson(value = "endtime", required = false) String endtime,
                                                                  @RequestJson(value = "sysmodelcode", required = false) String sysmodelcode,
                                                                  @RequestJson(value = "fktasktype", required = false) String fktasktype,
                                                                  @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                                  @RequestJson(value = "recoverystatuslist", required = false) List<String> recoverystatuslist,
                                                                  @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                                  @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                                  @RequestJson(value = "showcurrentuser", required = false) Integer showcurrentuser,
                                                                  @RequestJson(value = "isshowall", required = false) Object isshowall,
                                                                  @RequestJson(value = "csstatus", required = false) Integer csstatus,
                                                                  HttpServletRequest request, HttpSession session) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("showcurrentuser", showcurrentuser);
            paramMap.put("csstatus", csstatus);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            if (sysmodelcode != null && !"".equals(sysmodelcode)) {
                paramMap.put("sysmodel", sysmodelcode);
            } else {//为空则传默认的菜单code
                paramMap.put("sysmodel", sysmodel);
            }
            paramMap.put("recoverystatuslist", recoverystatuslist);
            paramMap.put("starttime", starttime);
            paramMap.put("fktaasktype", fktasktype);
            //根据任务状态类型赋值报警任务状态
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);

            //paramMap.put("datauserid", userId);
            paramMap.put("sign", "pc");
            Map<String, Object> resultdata = new HashMap<>();
            //按钮数据
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject = JSONObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject.getString("data");
            JSONObject jsonObject2 = JSONObject.fromObject(buttonData);
            String topoperations = "";
            String listoperation = "";
            if (jsonObject2.size() > 0) {
                topoperations = jsonObject2.getString("topbuttondata");
                listoperation = jsonObject2.getString("tablebuttondata");
            }

            List<String> rightList = getRightList(userId);
            boolean isRight = false;
            String rightCode = CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode();
            if (ChangeAlarmTaskEnum.getCode().toString().equals(fktasktype)){//突变任务
                rightCode = CommonTypeEnum.ModuleItemCodeEnum.TBEnum.getCode();
            }
            if (isshowall!=null||StringUtils.isNotBlank(monitorpointid)||rightList.contains(rightCode)) {
                isRight = true;
            }
            if (showcurrentuser != null) {//关联处理人
                if (showcurrentuser == 0) {//我分派、已完成
                    //已分派：所有已分派任务
                    if (isRight) {
                        paramMap.put("hasauthor", "1");
                    } else {
                        paramMap.put("hasauthor", "0");
                    }
                    if (statuslist.size() == 1 && statuslist.get(0).equals("4")) {//已完成

                    } else {//已分派（没有权限，没有任务）
                        if (!isRight) {//没有权限
                            statuslist = Arrays.asList("-1");
                        }
                    }
                } else if (showcurrentuser == 1 && csstatus != null) {//抄送我
                    paramMap.put("hasauthor", "0");
                    if (csstatus == 0) {//未读
                        paramMap.put("currenttaskstatuss", Arrays.asList("抄送"));
                    } else {//已读
                        paramMap.put("currenttaskstatuss", Arrays.asList("抄送已读"));
                    }
                } else if (showcurrentuser == 1) {//我处理、我审核、我完成
                    //待处理（statuslist=1,2），已处理（statuslist=3,4,5）
                    paramMap.put("hasauthor", "0");
                    if (statuslist.contains("1")) {//待处理：查询流程最新状态
                        paramMap.put("currentstatus", "1");
                    }
                    paramMap.put("currenttaskstatuss", Arrays.asList("待处理", "转办"));
                } else if (showcurrentuser == 2) {
                    //待审核（statuslist=5），已审核（statuslist=2,3,4）
                    paramMap.put("hasauthor", "0");
                    paramMap.put("currenttaskstatuss", Arrays.asList("审核"));
                } else if (showcurrentuser == 3) {//我完成（statuslist=4）
                    paramMap.put("hasauthor", "0");
                }
            }
            if (isshowall!=null||StringUtils.isNotBlank(monitorpointid)||rightList.contains(rightCode)) {
                paramMap.putIfAbsent("hasauthor", "1");
            } else {
                paramMap.putIfAbsent("hasauthor", "0");
            }
            List<String> categorys = new ArrayList<>(Arrays.asList(DataFormatUtil.parseProperties("system.category").split(",")));
            if (MonitorTaskEnum.getCode().toString().equals(fktasktype)) {
                categorys.add("2");
            }
            paramMap.put("categorys", categorys);

            paramMap.put("statuslist", statuslist);

            resultdata = alarmTaskDisposeService.getAllAlarmTaskDisposeListDataByParamMap(paramMap);
            //resultdata = alarmTaskDisposeService.getAssignAlarmTaskDisposeListDataByParamMap(paramMap);

            Map<String, Object> result = new HashMap<>();
            Map<String, Object> buttondatamap = new HashMap<>();//按钮
            buttondatamap.put("topbuttondata", topoperations);
            buttondatamap.put("tablebuttondata", listoperation);
            //返回数据
            result.put("total", resultdata.get("total"));
            result.put("datalist", resultdata.get("datalist"));
            result.put("buttondata", buttondatamap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 上午 8:43
     * @Description:根据自定义参数获取企业各类型报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getPollutionOutPutOverDataByParamMap", method = RequestMethod.POST)
    public Object getPollutionOutPutOverDataByParamMap(@RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                       @RequestJson(value = "taskcreatetime", required = false) String taskcreatetime,
                                                       @RequestJson(value = "outputname", required = false) String outputname,
                                                       @RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype,
                                                       @RequestJson(value = "pagesize", required = true) Integer pagesize,
                                                       @RequestJson(value = "pagenum", required = true) Integer pagenum
    ) throws Exception {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("outputname", outputname);
            paramMap.put("starttime", taskcreatetime);
            paramMap.put("endtime", taskcreatetime);
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            Map<String, Object> result = alarmTaskDisposeService.getPollutionOutPutOverDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/1/27 0027 上午 10:49
     * @Description: 根据自定义参数获取其他监测点各类型报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [taskcreatetime, monitorpointtype, dgimn, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getMonitorpointOverDataByParamMap", method = RequestMethod.POST)
    public Object getMonitorpointOverDataByParamMap(@RequestJson(value = "taskcreatetime", required = true) String taskcreatetime,
                                                    @RequestJson(value = "taskendtime", required = true) String taskendtime,
                                                    @RequestJson(value = "monitorpointtype", required = true) String monitorpointtype,
                                                    @RequestJson(value = "dgimn", required = true) String dgimn,
                                                    @RequestJson(value = "datatype", required = false) List<String> datatypes,
                                                    @RequestJson(value = "pagesize", required = true) Integer pagesize,
                                                    @RequestJson(value = "pagenum", required = true) Integer pagenum
    ) throws Exception {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", taskcreatetime);
            paramMap.put("endtime", taskendtime);
            paramMap.put("dgimn", dgimn);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            if (datatypes == null) {
                datatypes = new ArrayList<>();
            }
            paramMap.put("datatypes", datatypes);
            Map<String, Object> result = alarmTaskDisposeService.getMonitorPointOverDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:分派报警任务
     * @updateUser: lip
     * @updateDate: 2020-05-26
     * @updateDescription: 发送socket消息及短信消息
     * @param: []
     */
    @RequestMapping(value = "addAlarmTaskDisposeInfo", method = RequestMethod.POST)
    public Object addAlarmTaskDisposeInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Integer fktasktype = formdata.get("fktasktype") == null ? AlarmTaskEnum.getCode() : Integer.valueOf(formdata.get("fktasktype").toString());
            addTaskDisposeInfo(formdata, userId, username, fktasktype);
            //sendAlarmTaskMessage(username, formdata);
            //报警任务 分派  发送短信
            if (formdata.get("fktasktype") != null && !"".equals(formdata.get("fktasktype").toString()) &&
                    AlarmTaskEnum.getCode() == Integer.valueOf(formdata.get("fktasktype").toString())) {
                String taskid = formdata.get("pk_taskid").toString();
                List<String> userids = (List<String>) formdata.get("userids");
                sendNewAlarmTaskMessage(userids, taskid);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/08 0008 上午 10:48
     * @Description: 自动分派且 发送报警短信
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void sendNewAlarmTaskMessage(List<String> userids, String taskid) {
        //取配置文件中的模板ID
        String samplenum_one = DataFormatUtil.parseProperties("samplenum.onenum");
        //未配置模板ID 则不需要短信通知
        //获取该报警数据关联的企业或点位 的负责组长信息
        Map<String, Object> param = new HashMap<>();
        //未配置模板ID 则不需要短信通知
        if (StringUtils.isNotBlank(samplenum_one)) {
            //组织短信发送数据
            JSONObject socketJson = new JSONObject();
            List<String> phones = new ArrayList<>();
            List<Map<String, Object>> phoneanduser = new ArrayList<>();
            String Message = "";
            param.put("taskid", taskid);
            param.put("pointdataflag", "1");
            //获取单条任务信息
            Map<String, Object> taskobj = alarmTaskDisposeService.getTaskInfoDataByTaskID(param);
            if (taskobj != null) {
                socketJson.put("pointname", taskobj.get("monitorpointname"));
                socketJson.put("reminddata", taskobj.get("overlevelname"));
                Message = "您收到" + taskobj.get("monitorpointname") + "-" + taskobj.get("overlevelname") + "处置工单，请及时处理。";
            }
            //获取被分派任务的用户信息
            param.put("userids", userids);
            List<Map<String, Object>> users = alarmTaskDisposeService.getDisposePersonSelectDataByParams(param);
            if (users != null && users.size() > 0) {
                for (Map<String, Object> usermap : users) {
                    if (usermap.get("Phone") != null && !"".equals(usermap.get("Phone").toString())) {
                        phones.add(usermap.get("Phone").toString());
                        phoneanduser.add(usermap);
                    }
                }
            }
            socketJson.put("phonelist", phones);
            if (phones.size() > 0 && socketJson.get("pointname") != null && socketJson.get("reminddata") != null) {
                socketJson.put("samplenum", samplenum_one);
                Object resultobj = rabbitmqController.sendAlarmTaskRemindMessageToUser(socketJson);
                Object flag = null;
                if (resultobj != null) {
                    JSONArray jsonArray = JSONArray.fromObject(resultobj);
                    if (jsonArray.get(0) instanceof Boolean) {
                        boolean sendflag = (boolean) jsonArray.get(0);
                        if (sendflag) {
                            flag = 1;
                        } else {
                            flag = 0;
                        }
                    } else {
                        flag = 0;
                    }
                } else {
                    flag = 0;
                }
                //保存短信发送记录到库中
                if (flag != null && phoneanduser != null && phoneanduser.size() > 0) {
                    addTextMessageData(flag, phoneanduser, Message);
                }
            }
        }
    }


    private void addTextMessageData(Object flag, List<Map<String, Object>> phoneanduser, String Message) {
        List<TextMessageVO> listobj = new ArrayList<>();
        String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        Date senddate = new Date();
        for (Map<String, Object> map : phoneanduser) {
            TextMessageVO obj = new TextMessageVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setUsername(map.get("User_Name") != null ? map.get("User_Name").toString() : "");
            obj.setSenduser(username);
            obj.setSendtime(senddate);
            obj.setUserphonenumber(map.get("Phone").toString());
            obj.setMessage(Message);
            obj.setSendstatus(Integer.valueOf(flag.toString()));
            listobj.add(obj);
        }
        if (listobj.size() > 0) {
            alarmTaskDisposeService.addTextMessageDatas(listobj);
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/26 0026 下午 1:44
     * @Description: 报警任务首页+短信推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendAlarmTaskMessage(String username, Map<String, Object> formdata) {
        List<String> userIds = (List<String>) formdata.get("userids");
        if (userIds.size() > 0) {
            String monitorpointname = formdata.get("monitorpointname").toString();
            //PollutionVO pollutionVO = pollutionService.selectByPrimaryKey(pollutionId);
            //String pollutionName = pollutionVO.getPollutionname();
            String time = DataFormatUtil.getDateYMDHM(new Date());
            time = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm", "H点mm分");
            String context = "您收到来自" + username + time + "派发的" + monitorpointname + "的报警处置工单，请及时处理。";
            //发送socket消息
            List<Map<String, Object>> messageanduserdata = new ArrayList<>();
            for (String userid : userIds) {
                Map<String, Object> map = new HashMap<>();
                map.put("userid", userid);
                map.put("messagedata", context);
                messageanduserdata.add(map);
            }
            String messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageTaskDataEnum.getSocketMethod();
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
            //发送短信消息
            socketJson = new JSONObject();
            socketJson.put("userids", userIds);
            socketJson.put("username", username);
            socketJson.put("time", time);
            socketJson.put("mointorpointname", monitorpointname);
            socketJson.put("tasktype", "报警处置工单");
            authSystemMicroService.sendTaskSMSToClient(socketJson);
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:分派日常任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addDailyTaskDisposeInfo", method = RequestMethod.POST)
    public Object addDailyTaskDisposeInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            formdata.put("tasktype", CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
            addTaskDisposeInfo(formdata, userId, username, CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
            //sendDailyTaskMessage(username, formdata);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/5/26 0026 下午 1:44
     * @Description: 日常任务首页+短信推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendDailyTaskMessage(String username, Map<String, Object> formdata) {
        List<String> userIds = (List<String>) formdata.get("userids");
        if (userIds.size() > 0) {
            String pollutionId = formdata.get("pk_taskid").toString();
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = alarmTaskDisposeService.selectByPrimaryKey(pollutionId);
            String pollutionName = alarmTaskDisposeManagementVO.getTaskname();
            String time = DataFormatUtil.getDateYMDHM(new Date());
            time = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm", "H点mm分");
            String context = "您收到来自" + username + time + "派发的" + pollutionName + "的日常任务，请及时处理。";
            //发送socket消息
            List<Map<String, Object>> messageanduserdata = new ArrayList<>();
            for (String userid : userIds) {
                Map<String, Object> map = new HashMap<>();
                map.put("userid", userid);
                map.put("messagedata", context);
                messageanduserdata.add(map);
            }
            String messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageTaskDataEnum.getSocketMethod();
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
            //发送短信消息
            socketJson = new JSONObject();
            socketJson.put("userids", userIds);
            socketJson.put("username", username);
            socketJson.put("time", time);
            socketJson.put("mointorpointname", pollutionName);
            socketJson.put("tasktype", "日常任务");
            authSystemMicroService.sendTaskSMSToClient(socketJson);
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/20 0020 下午 4:14
     * @Description:分派任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private void addTaskDisposeInfo(Map<String, Object> formdata, String userId, String username, Integer tasktype) throws Exception {
        try {
            //消息推送
            List<String> userids = (List<String>) formdata.get("userids");
            Map<String, Object> usidmap = new HashMap<>();
            usidmap.put("userids", userids);
            alarmTaskDisposeService.saveTaskInfo(userId, username, formdata, tasktype);
            List<String> pushuserids = new ArrayList<>();
            pushuserids = (List<String>) formdata.get("userids");
            List<String> cs_userids = formdata.get("cs_userids") != null ? (List<String>) formdata.get("cs_userids") : new ArrayList<>();
            if (cs_userids != null && cs_userids.size() > 0) {
                pushuserids.addAll(cs_userids);
            }
            pushAlarmTaskToPageHome(pushuserids);
            // String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/25 0025 下午 1:38
     * @Description:转办任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addTransferTaskInfo", method = RequestMethod.POST)
    public Object addTransferTaskInfo(HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            alarmTaskDisposeService.addTransferTaskInfo(paramMap);
            //推送到首页
            List<String> pushuserids = (List<String>) paramMap.get("userids");
            //报警任务 分派  发送短信
            if (pushuserids.size() > 0 && paramMap.get("fktasktype") != null && !"".equals(paramMap.get("fktasktype").toString()) &&
                    AlarmTaskEnum.getCode() == Integer.valueOf(paramMap.get("fktasktype").toString())) {
                String taskid = paramMap.get("taskid").toString();
                sendNewAlarmTaskMessage(pushuserids, taskid);
            }
            if (pushuserids != null && pushuserids.size() > 0) {
                pushAlarmTaskToPageHome(pushuserids);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:反馈报警任务结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addAlarmTaskFeedbackInfo", method = RequestMethod.POST)
    public Object addAlarmTaskFeedbackInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Integer fktasktype = formdata.get("fktasktype") == null ? AlarmTaskEnum.getCode() : Integer.valueOf(formdata.get("fktasktype").toString());
            formdata.put("tasktype", fktasktype);
            addTaskFeedbackInfo(formdata, userId, username);
            //报警任务 添加审核人时  发送短信
            if (formdata.get("sc_userids") != null) {
                List<String> pushuserids = (List<String>) formdata.get("sc_userids");
                if (pushuserids.size() > 0 && formdata.get("fktasktype") != null && !"".equals(formdata.get("fktasktype").toString()) &&
                        AlarmTaskEnum.getCode() == Integer.valueOf(formdata.get("fktasktype").toString())) {
                    String taskid = formdata.get("pk_taskid").toString();
                    sendNewAlarmTaskMessage(pushuserids, taskid);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:反馈日常任务结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addDailyTaskFeedbackInfo", method = RequestMethod.POST)
    public Object addDailyTaskFeedbackInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            formdata.put("taskrealenddate", DataFormatUtil.getDateYMDHMS(new Date()));
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            formdata.put("tasktype", CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
            addTaskFeedbackInfo(formdata, userId, username);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:反馈任务结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    private void addTaskFeedbackInfo(Map<String, Object> formdata, String userId, String username) throws Exception {
        try {
            //判断是否存在审核人
            List<String> cs_userids = formdata.get("sc_userids") != null ? (List<String>) formdata.get("sc_userids") : new ArrayList<>();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(formdata.get("pk_taskid").toString());
            alarmTaskDisposeManagement.setFeedbackresults(formdata.get("feedbackresults") != null ? formdata.get("feedbackresults").toString() : "");
            alarmTaskDisposeManagement.setFileid(formdata.get("fileid") != null ? formdata.get("fileid").toString() : "");
            alarmTaskDisposeManagement.setFkProblemtype(formdata.get("problemtype") != null ? formdata.get("problemtype").toString() : "");
            if (formdata.get("taskrealenddate") != null) {
                alarmTaskDisposeManagement.setTaskrealenddate(formdata.get("taskrealenddate").toString());
            }
            alarmTaskDisposeManagement.setUpdatetime(new Date());
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setDisposer(formdata.get("disposer") != null ? formdata.get("disposer").toString() : "");
            alarmTaskDisposeManagement.setDisposaltime(formdata.get("disposaltime") != null ? formdata.get("disposaltime").toString() : "");
            alarmTaskDisposeManagement.setReportingtime(DataFormatUtil.getDateYMDHMS(new Date()));
            alarmTaskDisposeManagement.setTaskrealenddate(format.format(new Date()));
            if (cs_userids != null && cs_userids.size() > 0) {//有审核人时  需要审核人确认无误 才能办结任务
                alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.ConfirmEndEnum.getCode());
                alarmTaskDisposeService.updateTaskCarbonCopyStatus(userId, alarmTaskDisposeManagement);
            } else {//无抄送人  直接完成任务
                alarmTaskDisposeService.updateAlarmTaskInfo(userId, alarmTaskDisposeManagement);
            }

            List<TaskFlowRecordInfoVO> objs = new ArrayList<>();
            if (cs_userids != null && cs_userids.size() > 0) {
                Date datetime = new Date();
                for (String str : cs_userids) {
                    //添加任务处置记录信息
                    TaskFlowRecordInfoVO taskFlowRecordInfo = new TaskFlowRecordInfoVO();
                    taskFlowRecordInfo.setPkId(UUID.randomUUID().toString());//主键ID
                    taskFlowRecordInfo.setFkTaskid(alarmTaskDisposeManagement.getPkTaskid());//任务ID
                    taskFlowRecordInfo.setFkTaskhandleuserid(str.toString());//被抄送该任务的抄送人人ID
                    taskFlowRecordInfo.setCurrenttaskstatus(CommonTypeEnum.AlarmTaskStatusEnum.ReviewedEnum.getName().toString());//任务状态
                    taskFlowRecordInfo.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());//任务类型
                    taskFlowRecordInfo.setTaskhandletime(datetime);//被分派该任务的时间
                    objs.add(taskFlowRecordInfo);
                }
            }
            if (objs != null && objs.size() > 0) {
                alarmTaskDisposeService.insertReviewedInfo(objs);
            }
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(alarmTaskDisposeManagement.getPkTaskid());
            pushAlarmTaskToPageHome(useridsByTaskid);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/30 0030 下午 3:55
     * @Description:忽略报警任务信息
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:直接完成任务 去掉忽略状态
     * @param: []
     */
    @RequestMapping(value = "addAlarmTaskNeglectInfo", method = RequestMethod.POST)
    public Object addAlarmTaskNeglectInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String problemtype = formdata.get("problemtype") != null ? formdata.get("problemtype").toString() : "";
            String fileid = formdata.get("fileid") != null ? formdata.get("fileid").toString() : "";
            String pk_taskid = formdata.get("pk_taskid") != null ? formdata.get("pk_taskid").toString() : "";
            String taskcomment = formdata.get("feedbackresults") != null ? formdata.get("feedbackresults").toString() : "";
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(pk_taskid);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            alarmTaskDisposeManagement.setUpdatetime(new Date());
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setFkProblemtype(problemtype);
            alarmTaskDisposeManagement.setFileid(fileid);
            alarmTaskDisposeManagement.setFeedbackresults(taskcomment);
            //反馈任务
            alarmTaskDisposeService.updateAlarmTaskInfo(userId, alarmTaskDisposeManagement);

            //忽略任务信息
            //alarmTaskDisposeService.neglectTaskInfo(alarmTaskDisposeManagement, userId);
            // String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(alarmTaskDisposeManagement.getPkTaskid());
            pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/03/09 0009 上午 9:12
     * @Description:保存评论任务意见信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addAlarmTaskCommentInfo", method = RequestMethod.POST)
    public Object addAlarmTaskCommentInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String pk_taskid = formdata.get("pk_taskid") != null ? formdata.get("pk_taskid").toString() : "";
            String taskcomment = formdata.get("taskcomment") != null ? formdata.get("taskcomment").toString() : "";
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(pk_taskid);
            String Code = alarmTaskDisposeManagement.getTaskstatus() != null ? alarmTaskDisposeManagement.getTaskstatus().toString() : "";
            String taskstatuname = "";
            if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode().toString()).equals(Code)) {
                //待分派
                taskstatuname = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getName();
            } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString()).equals(Code)) {
                //待处理
                taskstatuname = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getName();
            } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(Code)) {
                //处理中
                taskstatuname = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getName();
            } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(Code)) {
                //已完成
                taskstatuname = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getName();
            } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString()).equals(Code)) {
                //已忽略
                taskstatuname = CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getName();
            }

            //添加抄送人的评论意见
            TaskFlowRecordInfoVO obj = new TaskFlowRecordInfoVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setFkTaskid(pk_taskid);
            obj.setFkTaskhandleuserid(userId);
            obj.setCurrenttaskstatus(taskstatuname);
            obj.setFkTasktype(alarmTaskDisposeManagement.getFkTasktype());
            obj.setTaskhandletime(new Date());
            obj.setTaskcomment(taskcomment);
            alarmTaskDisposeService.insertTaskFlowRecordInfoVO(obj);
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午2:07
     * @Description: 根据报警任务ID获取详情信息
     * @updateUser:xsm
     * @updateDate:2019/7/30 0030 下午1:54
     * @updateDescription:若查看详情信息的用户为被分派该任务的人，则记录任务状态为处置中
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getAlarmTaskDisposeManagementDetailByID", method = RequestMethod.POST)
    public Object getAlarmTaskDisposeManagementDetailByID(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            result = alarmTaskDisposeService.getAlarmTaskDisposeManagementDetailByID(id, userId, username);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/02 0002 上午11:19
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "addAlarmTaskStatusToHandle", method = RequestMethod.POST)
    public Object addAlarmTaskStatusToHandle(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            alarmTaskDisposeService.addAlarmTaskStatusToHandle(id, userId, username);

            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(id);
            pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0017 上午 10:54
     * @Description:获取拥有处置按钮权限的处置人下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getDisposePersonSelectData", method = RequestMethod.POST)
    public Object getDisposePersonSelectData(@RequestJson(value = "sysmodel", required = false) String sysmodelparam) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("sysmodel", sysmodel);
            if (StringUtils.isNotBlank(sysmodelparam)) {
                paramMap.put("sysmodel", sysmodelparam);
            }
            paramMap.put("buttoncode", buttoncode);
            List<Map<String, Object>> listdata = alarmTaskDisposeService.getDisposePersonSelectData(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("User_Name"));
                    objmap.put("value", map.get("User_ID"));
                    result.add(objmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 09:46
     * @Description:获取问题类型下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getProblemTypeSelectData", method = RequestMethod.POST)
    public Object getProblemTypeSelectData(@RequestJson(value = "monitorpointtypecode", required = true) String monitorpointtypecode) throws Exception {
        try {
            List<Map<String, Object>> listdata = alarmTaskDisposeService.getProblemTypeSelectData(monitorpointtypecode);
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("Name"));
                    objmap.put("value", map.get("Code"));
                    result.add(objmap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 6:47
     * @Description:导出-报警任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportAlarmTaskDisposeManagement", method = RequestMethod.POST)
    public void exportAlarmTaskDisposeManagement(@RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                 @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                 @RequestJson(value = "recoverystatuslist", required = false) List<String> recoverystatuslist,
                                                 @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                 @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                 @RequestJson(value = "starttime", required = false) String starttime,
                                                 @RequestJson(value = "fktasktype", required = false) String fktasktype,
                                                 @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                 @RequestJson(value = "endtime", required = false) String endtime,
                                                 HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //获取表头数据
            List<Map<String, Object>> tabletitledata = alarmTaskDisposeService.getTableTitleForAlarmTask();
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            //paramMap.put("datauserid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("fktaasktype", fktasktype);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("recoverystatuslist", recoverystatuslist);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("monitorpointname", monitorpointname);
            //按钮数据
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject = JSONObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject.getString("data");
            JSONObject jsonObject2 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject2.getString("topbuttondata");
            String listoperation = jsonObject2.getString("tablebuttondata");
            List<Object> list = JSON.parseArray(listoperation);
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            Map<String, Object> datas = new HashMap<>();
            boolean flag = false;
            if (list != null && list.size() > 0) {
                for (Object obj : list) {
                    Map entry = (Map) obj;
                    String value = entry.get("name").toString();
                    if ("assignButton".equals(value)) {//当拥有分派按钮时，该用户拥有权限，显示全部数据
                        flag = true;
                    }
                }
            }
            paramMap.put("hasauthor", "1");
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);
            paramMap.put("statuslist", statuslist);
            datas = alarmTaskDisposeService.getAllAlarmTaskDisposeListDataByParamMap(paramMap);
            listdata = (List<Map<String, Object>>) datas.get("datalist");
            Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("TaskStatus").toString());
            List<Map<String, Object>> collect = listdata.stream().sorted(comparebytime).collect(Collectors.toList());
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "报警任务处置管理导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/25 0025 下午 6:01
     * @Description: 突变任务导出
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionname, monitorpointname, starttime, endtime, session, request, response]
     * @throws:
     */
    @RequestMapping(value = "exportSuddenChangeDisposeManagement", method = RequestMethod.POST)
    public void exportSuddenChangeDisposeManagement(@RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                    @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                    @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                    @RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime,
                                                    HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //获取表头数据
            List<Map<String, Object>> tabletitledata = getTableTitleForSuddenChangeTask();
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("datauserid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("sysmodel", suddenChangeSysmodel);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("fktaasktype", 9);
            paramMap.put("statuslist", statuslist);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("fktasktype", ChangeAlarmTaskEnum.getCode());
            //按钮数据
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject = JSONObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject.getString("data");
            JSONObject jsonObject2 = JSONObject.fromObject(buttonData);
            String topoperations = jsonObject2.getString("topbuttondata");
            String listoperation = jsonObject2.getString("tablebuttondata");
            List<Object> list = JSON.parseArray(listoperation);
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            Map<String, Object> datas = new HashMap<>();
            boolean flag = false;
            if (list != null && list.size() > 0) {
                for (Object obj : list) {
                    Map entry = (Map) obj;
                    String value = entry.get("name").toString();
                    if ("assignButton".equals(value)) {//当拥有分派按钮时，该用户拥有权限，显示全部数据
                        flag = true;
                    }
                }
            }
            paramMap.put("hasauthor", "1");
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);
           /* if (flag == false) {//无分派按钮权限  值显示和自己相关的报警任务
                List<String> statuslist = new ArrayList<>();
                statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString());
                statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString());
                statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString());
                statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString());
                paramMap.put("statuslist", statuslist);
                paramMap.put("hasauthor", "0");
            } else {
                paramMap.put("hasauthor", "1");
            }*/
            datas = alarmTaskDisposeService.getAllAlarmTaskDisposeListDataByParamMap(paramMap);
            listdata = (List<Map<String, Object>>) datas.get("datalist");
            Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("TaskStatus").toString());
            List<Map<String, Object>> collect = listdata.stream().sorted(comparebytime).collect(Collectors.toList());
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "突变任务处置管理导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Map<String, Object>> getTableTitleForSuddenChangeTask() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"监测点名称", "突变污染物", "突变时间", "任务生成时间", "任务派发时间", "处置完成时间", "处置人", "状态"};
        String[] titlefiled = new String[]{"monitorpointname", "pollutantname", "alarmstarttime", "taskcreatetime", "assignmenttime", "completetime", "user_name", "taskstatuname"};
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
     * @date: 2019/7/18 0018 上午 10:36
     * @Description:暂存报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addAlarmTaskTemporaryInfo", method = RequestMethod.POST)
    public Object addAlarmTaskTemporaryInfo(@RequestJson(value = "pk_taskid") String pk_taskid,
                                            @RequestJson(value = "fileid", required = false) String fileid,
                                            @RequestJson(value = "feedbackresults", required = false) String feedbackresults,
                                            @RequestJson(value = "problemtype", required = false) String problemtype,
                                            @RequestJson(value = "disposer", required = false) String disposer,
                                            @RequestJson(value = "disposaltime", required = false) String disposaltime,
                                            HttpServletRequest request, HttpSession session) throws Exception {
        try {
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(pk_taskid);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Date date = new Date();
            alarmTaskDisposeManagement.setFeedbackresults(feedbackresults);
            alarmTaskDisposeManagement.setFileid(fileid);
            alarmTaskDisposeManagement.setFkProblemtype(problemtype);
            alarmTaskDisposeManagement.setUpdatetime(date);
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setDisposer(disposer);
            alarmTaskDisposeManagement.setDisposaltime(disposaltime);
            alarmTaskDisposeManagement.setReportingtime(DataFormatUtil.getDateYMDHMS(date));
            alarmTaskDisposeService.temporaryTaskInfo(alarmTaskDisposeManagement);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 10:36
     * @Description:获取按月份分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countAlarmTaskNumGroupByMonth", method = RequestMethod.POST)
    public Object countAlarmTaskNumGroupByMonth() throws Exception {
        try {
            //获取当前年月
            Calendar cale = Calendar.getInstance();
            int year = cale.get(Calendar.YEAR);
            int month = cale.get(Calendar.MONTH) + 1;
            String currentdate = year + "-" + month;
            //获取该年第一个月
            String firstdate = year + "-01";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", firstdate);
            paramMap.put("endtime", currentdate);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            //根据时间范围和时间集合，获取报警任务数量统计数据
            List<Map<String, Object>> datalist = alarmTaskDisposeService.countAlarmTaskNumDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/23 0023 上午 9:08
     * @Description:根据监测时间获取该月份报警任务总数和已完成数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAlarmTaskListDataByMonitortime", method = RequestMethod.POST)
    public Object getAlarmTaskListDataByMonitortime(@RequestJson(value = "monitortime") String monitortime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortime", monitortime);
            //根据时间范围和时间集合，获取报警任务数量统计数据
            List<Map<String, Object>> datalist = alarmTaskDisposeService.getAlarmTaskListDataByMonitortime(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @Description: 统计一段时间内任务数量，完成数量
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/3/10 10:08
     */
    @RequestMapping(value = "countTaskDataByParam", method = RequestMethod.POST)
    public Object countTaskDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "tasktype") Integer tasktype,
            @RequestJson(value = "pollutionid", required = false) String pollutionid

    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("tasktype", tasktype);
            Map<String, Object> resultMap;
            if (tasktype == 3) {//巡查任务
                paramMap.put("pollutionid", pollutionid);
                resultMap = getCheckTask(paramMap);
            } else {//报警工单
                resultMap = getOverTask(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> getCheckTask(Map<String, Object> paramMap) {
        long totalNum = checkProblemExpoundService.getTotalTaskNumByParam(paramMap);
        paramMap.put("taskstatus", Arrays.asList(3));
        long comNum = checkProblemExpoundService.getTotalTaskNumByParam(paramMap);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("comNum", comNum);
        return resultMap;
    }

    private Map<String, Object> getOverTask(Map<String, Object> paramMap) {
        long totalNum = alarmTaskDisposeService.getTotalTaskNumByParam(paramMap);
        paramMap.put("taskstatus", Arrays.asList(3, 4));
        long comNum = alarmTaskDisposeService.getTotalTaskNumByParam(paramMap);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("comNum", comNum);
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 2:39
     * @Description:获取按点位分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countAlarmTaskNumGroupByPollution", method = RequestMethod.POST)
    public Object countAlarmTaskNumGroupByPollution(@RequestJson(value = "starttime") String starttime,
                                                    @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            //获取当前年月
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            //根据时间范围和时间集合，获取按企业分组的报警任务数量统计数据
            List<Map<String, Object>> datalist = alarmTaskDisposeService.countAlarmTaskNumGroupByPollution(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/18 0018 下午 3:03
     * @Description:获取按问题类型分组的报警任务数量统计信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "countAlarmTaskNumGroupByProblemType", method = RequestMethod.POST)
    public Object countAlarmTaskNumGroupByProblemType(@RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            //获取当前年月
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            //根据时间范围和时间集合，获取按企业分组的报警任务数量统计数据
            List<Map<String, Object>> datalist = alarmTaskDisposeService.countAlarmTaskNumGroupByProblemType(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 9:40
     * @Description: 获取当前登录人处置任务提醒数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getLoginUserAlarmTaskDisposeRemindData", method = RequestMethod.POST)
    public Object getLoginUserAlarmTaskDisposeRemindData(HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> buttonmap = new HashMap<>();
            paramMap.put("userid", userId);
            //报警任务菜单id
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPOverAlarmTaskDisposeEnum.getMenuid());
            Object alarmuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
            //投诉任务菜单id
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPComplaintTaskDisposeEnum.getMenuid());
            Object complaintuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
            //日常任务菜单id
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPDailyTaskDisposeEnum.getMenuid());
            Object dailyuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
            //运维任务菜单id
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPDevOpsTaskDisposeEnum.getMenuid());
            Object devOpsuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
            //按钮数据
            buttonmap.put("alarmtask", alarmuserButtonAuthInMenu);
            buttonmap.put("complainttask", complaintuserButtonAuthInMenu);
            buttonmap.put("dailytask", dailyuserButtonAuthInMenu);
            buttonmap.put("devOpstask", devOpsuserButtonAuthInMenu);
            //判断是否有待分派、反馈的按钮
            Map<String, Object> resultMap = getAPPTaskRemindRightData(buttonmap, userId);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 9:40
     * @Description: 获取所有app用户报警任务提醒数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllAppUserAlarmTaskDisposeRemindData", method = RequestMethod.POST)
    public List<JSONObject> getAllAppUserAlarmTaskDisposeRemindData() {
        try {
            List<JSONObject> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> userRegisterInfo = jgUserRegisterInfoService.getUserRegisterInfoListByParam(paramMap);
            if (userRegisterInfo.size() > 0) {
                String userid = "";
                for (Map<String, Object> map : userRegisterInfo) {
                    Map<String, Object> buttonmap = new HashMap<>();
                    userid = map.get("fk_userid").toString();
                    paramMap.put("userid", userid);
                    //报警任务菜单id
                    paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPOverAlarmTaskDisposeEnum.getMenuid());
                    Object alarmuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
                    //投诉任务菜单id
                    paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPComplaintTaskDisposeEnum.getMenuid());
                    Object complaintuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
                    //日常任务菜单id
                    paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPDailyTaskDisposeEnum.getMenuid());
                    Object dailyuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
                    //运维任务菜单id
                    paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.APPDevOpsTaskDisposeEnum.getMenuid());
                    Object devOpsuserButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(AuthUtil.paramDataFormat(paramMap));
                    //按钮数据
                    buttonmap.put("alarmtask", alarmuserButtonAuthInMenu);//报警任务按钮权限
                    buttonmap.put("complainttask", complaintuserButtonAuthInMenu);//投诉任务按钮权限
                    buttonmap.put("dailytask", dailyuserButtonAuthInMenu);//日常任务按钮权限
                    buttonmap.put("devOpstask", devOpsuserButtonAuthInMenu);//运维任务按钮权限
                    //判断是否有待分派、反馈的按钮
                    Map<String, Object> taskRemindRightData = getAPPTaskRemindRightData(buttonmap, userid);
                    JSONObject resultJson = new JSONObject();
                    resultJson.put("userid", userid);
                    resultJson.put("regid", map.get("regid"));
                    resultJson.put("reminddata", taskRemindRightData);
                    resultList.add(resultJson);
                }
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 9:40
     * @Description: 获取环境监管任务提醒数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getEnvSupervisionRemindData", method = RequestMethod.POST)
    public Object getEnvSupervisionRemindData(
            @RequestJson(value = "userids", required = false) List<String> userids,
            HttpSession session) {
        try {
            if (userids != null && userids.size() > 0) {
                List<Map<String, Object>> resultList = getManyUserRemindData(userids);
                return resultList;
            } else {
                Map<String, Object> resultMap = getLoginUserRemindData(session);
                return resultMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/8/28 0028 上午 9:54
     * @Description: 获取当前登录用户提醒数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getLoginUserRemindData(HttpSession session) {
        Map<String, Object> resultList = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        List<Map<String, Object>> remindData = new ArrayList<>();
        paramMap.put("userid", userId);
        //1，获取报警任务提醒数据
        paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.AlarmTaskManagementEnum.getMenuid());
        String params = AuthUtil.paramDataFormat(paramMap);
        Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
        Map<String, Object> taskRemindRightData = getAlarmTaskRemindRightData(userButtonAuthInMenu, userId);
        taskRemindRightData.put("sysmodel", CommonTypeEnum.SocketTypeEnum.AlarmTaskManagementEnum.getMenucode());
        taskRemindRightData.put("name", CommonTypeEnum.SocketTypeEnum.AlarmTaskManagementEnum.getDes());
        remindData.add(taskRemindRightData);
        //2，获取投诉任务提醒数据
        paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.TSTaskManagementEnum.getMenuid());
        params = AuthUtil.paramDataFormat(paramMap);
        userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
        Map<String, Object> TSRemindRightData = getTSTaskRemindRightData(userButtonAuthInMenu, userId);
        TSRemindRightData.put("sysmodel", CommonTypeEnum.SocketTypeEnum.TSTaskManagementEnum.getMenucode());
        TSRemindRightData.put("name", CommonTypeEnum.SocketTypeEnum.TSTaskManagementEnum.getDes());
        remindData.add(TSRemindRightData);
        //3，获取日常任务提醒数据
        paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.GeneralTaskManagementEnum.getMenuid());
        params = AuthUtil.paramDataFormat(paramMap);
        userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
        Map<String, Object> RCRemindRightData = getRCTaskRemindRightData(userButtonAuthInMenu, userId);
        RCRemindRightData.put("sysmodel", CommonTypeEnum.SocketTypeEnum.GeneralTaskManagementEnum.getMenucode());
        RCRemindRightData.put("name", CommonTypeEnum.SocketTypeEnum.GeneralTaskManagementEnum.getDes());
        remindData.add(RCRemindRightData);
        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("datalist", remindData);
        resultMap.put("sum", taskRemindRightData.get("count"));
        return resultList;

    }

    /**
     * @author: lip
     * @date: 2019/8/28 0028 上午 9:59
     * @Description: 获取多个用户的报警提醒数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getManyUserRemindData(List<String> userids) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        for (String userid : userids) {
            List<Map<String, Object>> remindData = new ArrayList<>();
            paramMap.put("userid", userid);
            //1，获取报警任务提醒数据
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.AlarmTaskManagementEnum.getMenuid());
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            Map<String, Object> taskRemindRightData = getAlarmTaskRemindRightData(userButtonAuthInMenu, userid);
            taskRemindRightData.put("sysmodel", CommonTypeEnum.SocketTypeEnum.AlarmTaskManagementEnum.getMenucode());
            taskRemindRightData.put("name", CommonTypeEnum.SocketTypeEnum.AlarmTaskManagementEnum.getDes());
            remindData.add(taskRemindRightData);
            //2，获取投诉任务提醒数据
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.TSTaskManagementEnum.getMenuid());
            params = AuthUtil.paramDataFormat(paramMap);
            userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            Map<String, Object> TSRemindRightData = getTSTaskRemindRightData(userButtonAuthInMenu, userid);
            TSRemindRightData.put("sysmodel", CommonTypeEnum.SocketTypeEnum.TSTaskManagementEnum.getMenucode());
            TSRemindRightData.put("name", CommonTypeEnum.SocketTypeEnum.TSTaskManagementEnum.getDes());
            remindData.add(TSRemindRightData);
            //3，获取日常任务提醒数据
            paramMap.put("menuid", CommonTypeEnum.SocketTypeEnum.GeneralTaskManagementEnum.getMenuid());
            params = AuthUtil.paramDataFormat(paramMap);
            userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            Map<String, Object> RCRemindRightData = getRCTaskRemindRightData(userButtonAuthInMenu, userid);
            RCRemindRightData.put("sysmodel", CommonTypeEnum.SocketTypeEnum.GeneralTaskManagementEnum.getMenucode());
            RCRemindRightData.put("name", CommonTypeEnum.SocketTypeEnum.GeneralTaskManagementEnum.getDes());
            remindData.add(RCRemindRightData);
            Map<String, Object> resultMap = new LinkedHashMap<>();
            resultMap.put("userid", userid);
            resultMap.put("datalist", remindData);
            resultMap.put("sum", taskRemindRightData.get("count"));
            resultList.add(resultMap);
        }

        return resultList;
    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:05
     * @Description: 获取APP报警任务提醒数据（关联权限）
     * @updateUser:xsm
     * @updateDate:2019/8/26 上午 9:37
     * @updateDescription:只统计两天的报警任务（昨天、今天）
     * @updateUser:xsm
     * @updateDate:2019/9/6 上午 11:13
     * @updateDescription:只统计当天的报警任务
     * @updateUser:xsm
     * @updateDate:2021/12/13 下午 3:13
     * @updateDescription:统计所有报警任务
     * @param:
     * @return:
     */
    private Map<String, Object> getAPPTaskRemindRightData(Map<String, Object> buttonmap, String userId) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        //Date date = new Date();//取时间
        //获取九天前日期
        // String starttime = DataFormatUtil.getDateYMD(DataFormatUtil.getPreDate(date, 9));
        //String endtime = DataFormatUtil.getDateYMD(date);
        //String endtime = DataFormatUtil.getDateYMD(date);
        paramMap.clear();
        List<Integer> tasktypes = new ArrayList<>();
        tasktypes.add(CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());//日常任务
        //paramMap.put("starttime", starttime);
        //paramMap.put("endtime", endtime);
        paramMap.put("tasktypes", tasktypes);
        //统计待分派任务 日常任务不关联点位 无数据权限控制
        List<Map<String, Object>> wfp_tasklist = alarmTaskDisposeService.countUnassignedTaskDataNum(paramMap);
        //添加数据权限
        paramMap.put("datauserid", userId);
        tasktypes.add(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode());//报警任务
        tasktypes.add(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());//运维任务
        wfp_tasklist.addAll(alarmTaskDisposeService.countUnassignedTaskDataNum(paramMap));
        //统计抄送给我的报警、运维任务
        List<Map<String, Object>> cs_tasklist = alarmTaskDisposeService.countCarbonCopyTaskDataNum(paramMap);
        //统计时间段内所有已完成的任务数量
        List<Map<String, Object>> ywc_tasklist = alarmTaskDisposeService.countCompletedStatusTaskDataNumForUserID(paramMap);
        //统计 待处理 处理中 已完成 任务数量  有用户筛选
        paramMap.put("userid", userId);
        List<Map<String, Object>> otherstatus_tasklist = alarmTaskDisposeService.countOtherStatusAlarmTaskNumByUserID(paramMap);
        //List<Map<String, Object>> otherstatus_tasklist = alarmTaskDisposeService.countAlarmTaskByParamMap(paramMap);
        resultMap.put("needassign", new HashMap<>());
        resultMap.put("needfeedback", new HashMap<>());
        resultMap.put("feedbacking", new HashMap<>());
        resultMap.put("hasclose", new HashMap<>());
        resultMap.put("confirmend", new HashMap<>());
        //判断是否有权限 并获取各状态任务数量
        getTaskStatsDataNumForApp("alarmtask", resultMap, wfp_tasklist, otherstatus_tasklist, ywc_tasklist, CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode().toString(), getIsHaveButton(buttonmap.get("alarmtask"), "assignButton"));
        getTaskStatsDataNumForApp("dailytask", resultMap, wfp_tasklist, otherstatus_tasklist, ywc_tasklist, CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode().toString(), getIsHaveButton(buttonmap.get("dailytask"), "assignButton"));
        getTaskStatsDataNumForApp("devopstask", resultMap, wfp_tasklist, otherstatus_tasklist, ywc_tasklist, CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString(), getIsHaveButton(buttonmap.get("devOpstask"), "assignButton"));
        getComplaintTaskRemindRightDataForApp("complainttask", paramMap, resultMap, CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode().toString(), getIsHaveButton(buttonmap.get("complainttask"), "assignButton"));
        //抄送给我的
        Map<String, Integer> cs_map = new HashMap<>();
        cs_map.put("alarmtask", 0);
        cs_map.put("dailytask", 0);
        cs_map.put("devopstask", 0);
        cs_map.put("complainttask", 0);
        if (cs_tasklist != null && cs_tasklist.size() > 0) {
            for (Map<String, Object> onemap : cs_tasklist) {
                if (onemap.get("FK_TaskType") != null && (CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode().toString()).equals(onemap.get("FK_TaskType").toString())) {
                    cs_map.put("alarmtask", cs_map.get("alarmtask") + Integer.valueOf(onemap.get("num").toString()));
                }
                if (onemap.get("FK_TaskType") != null && (CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode().toString()).equals(onemap.get("FK_TaskType").toString())) {
                    cs_map.put("devopstask", cs_map.get("devopstask") + Integer.valueOf(onemap.get("num").toString()));
                }
            }
        }
        result.put("needassign", resultMap.get("needassign"));
        result.put("needfeedback", resultMap.get("needfeedback"));
        result.put("feedbacking", resultMap.get("feedbacking"));
        result.put("hasclose", resultMap.get("hasclose"));
        result.put("carboncopy", cs_map);//抄送给我
        result.put("confirmend", resultMap.get("confirmend"));//待审核
        return result;
    }

    private void getTaskStatsDataNumForApp(String key, Map<String, Map<String, Object>> resultMap, List<Map<String, Object>> wfp_tasklist, List<Map<String, Object>> otherstatus_tasklist, List<Map<String, Object>> ywc_tasklist, String code, boolean isDassign) {
        resultMap.get("needassign").put(key, 0);
        if (isDassign) {
            if (wfp_tasklist != null && wfp_tasklist.size() > 0) {
                for (Map<String, Object> map : wfp_tasklist) {
                    //任务类型相同
                    if (map.get("FK_TaskType") != null && code.equals(map.get("FK_TaskType").toString())) {
                        resultMap.get("needassign").put(key, map.get("num") != null ? map.get("num") : 0);
                    }
                }
            }
        }
        resultMap.get("needfeedback").put(key, 0);
        resultMap.get("feedbacking").put(key, 0);
        resultMap.get("confirmend").put(key, 0);
        resultMap.get("hasclose").put(key, 0);
        //判断有无分派权限 有则显示所有已完成任务
        if (isDassign) {
            if (ywc_tasklist != null && ywc_tasklist.size() > 0) {
                for (Map<String, Object> map : ywc_tasklist) {
                    //任务类型相同
                    if (map.get("FK_TaskType") != null && code.equals(map.get("FK_TaskType").toString())) {
                        resultMap.get("hasclose").put(key, map.get("num") != null ? map.get("num") : 0);
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
                            resultMap.get("needfeedback").put(key, map.get("num"));
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString()).equals(status)) {
                            //处理中
                            resultMap.get("feedbacking").put(key, map.get("num"));
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString()).equals(status)) {
                            if (isDassign == false) {//当无分派权限 只显示自己的任务
                                //已完成
                                resultMap.get("hasclose").put(key, map.get("num"));
                            }
                        } else if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.ConfirmEndEnum.getCode().toString()).equals(status)) {
                            resultMap.get("confirmend").put(key, map.get("num"));//待审核
                        }
                    }
                }
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/30 0030 下午 7:43
     * @Description: 获取APP首页投诉任务报警数量
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getComplaintTaskRemindRightDataForApp(String key, Map<String, Object> paramMap, Map<String, Map<String, Object>> resultMap, String code, boolean isDassign) {
        List<Map<String, Object>> listdata = complaintTaskDisposeService.countTaskDisposeNumGroupByStatusByParams(paramMap);
        resultMap.get("needassign").put(key, 0);
        resultMap.get("needfeedback").put(key, 0);
        resultMap.get("feedbacking").put(key, 0);
        resultMap.get("hasclose").put(key, 0);
        if (listdata != null && listdata.size() > 0) {
            for (Map<String, Object> map : listdata) {
                String status = map.get("TaskStatus").toString();
                if ((CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode().toString()).equals(status)) {
                    //待分派
                    if (isDassign) {
                        resultMap.get("needassign").put(key, map.get("num") != null ? map.get("num") : 0);
                    }
                } else if ((CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode().toString()).equals(status)) {
                    //待处理
                    resultMap.get("needfeedback").put(key, map.get("num"));
                } else if ((CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getCode().toString()).equals(status)) {
                    //处理中
                    resultMap.get("feedbacking").put(key, map.get("num"));
                } else if ((CommonTypeEnum.ComplaintTaskEnum.CompletedEnum.getCode().toString()).equals(status)) {
                    //已完成
                    resultMap.get("hasclose").put(key, map.get("num"));
                }
            }
        }
    }


    /**
     * @author: xsm
     * @date: 2019/10/31 0031 下午 4:46
     * @Description: 组装app投诉任务提醒数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void composeComplaintTaskRemindData(Map<String, Object> resultMap, List<Map<String, Object>> dailylistdata, String type) {
        String keyname = "complainttask";
        List<Integer> statuslist = CommonTypeEnum.getComplaintTaskStatusList();
        for (int status : statuslist) {
            Map<String, Object> needassign = new HashMap<>();
            String statuskeyname = "";
            int num = 0;
            if (CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode() == status) {
                //待分派
                statuskeyname = "needassign";
            } else if (CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode() == status) {
                //待处理
                statuskeyname = "needfeedback";
            } else if (CommonTypeEnum.ComplaintTaskEnum.HandleEnum.getCode() == status) {
                //处理中
                statuskeyname = "feedbacking";
            } else if (CommonTypeEnum.ComplaintTaskEnum.CompletedEnum.getCode() == status) {
                //已完成
                statuskeyname = "hasclose";
            }
            for (Map<String, Object> objmap : dailylistdata) {
                if (status == Integer.parseInt(objmap.get("TaskStatus").toString())) {//状态相等
                    num = (objmap.get("num") != null || !"".equals(objmap.get("num").toString())) ? Integer.parseInt(objmap.get("num").toString()) : 0;
                }
            }
            if (resultMap.get(statuskeyname) != null) {
                needassign = (Map<String, Object>) resultMap.get(statuskeyname);
                needassign.put(keyname, num);
                resultMap.put(statuskeyname, needassign);
            } else {
                needassign.put(keyname, num);
                resultMap.put(statuskeyname, needassign);
            }
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:05
     * @Description: 获取报警任务提醒数据（关联权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getAlarmTaskRemindRightData(Object userButtonAuthInMenu, String userId) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        boolean isFeedback = getIsHaveButton(userButtonAuthInMenu, "feedbackButton");
        boolean isDassign = getIsHaveButton(userButtonAuthInMenu, "assignButton");
        ;
        List<Map<String, Object>> dataNum = new ArrayList<>();
        if (isDassign) {
            paramMap.clear();
            paramMap.put("type", "alarm");
            paramMap.put("taskstatus", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
            paramMap.put("tasktype", AlarmTaskEnum.getCode());
            dataNum.addAll(alarmTaskDisposeService.getTaskDisposeNumDataByParams(paramMap));
        }
        //待处理
        if (isFeedback) {
            paramMap.clear();
            paramMap.put("userid", userId);
            paramMap.put("currenttaskstatus", CommonTypeEnum.AlarmTaskStatusEnum.UntreatedEnum.getName());
            dataNum.addAll(alarmTaskDisposeService.getTaskDisposeNumDataByParams(paramMap));
        }
        resultMap.put("count", dataNum.size());
        return resultMap;
    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:05
     * @Description: 获取报警任务提醒数据（关联权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getTSTaskRemindRightData(Object userButtonAuthInMenu, String userId) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();

        boolean isDassign = getIsHaveButton(userButtonAuthInMenu, "assignButton");
        List<Map<String, Object>> dataNum = new ArrayList<>();
        if (isDassign) {
            paramMap.clear();
            paramMap.put("taskstatus", CommonTypeEnum.ComplaintTaskEnum.UnassignedTaskEnum.getCode());
            paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode());
            dataNum.addAll(complaintTaskDisposeService.getComplaintTaskDisposeNumDataByParams(paramMap));
        }
        //待处理
        paramMap.clear();
        paramMap.put("userid", userId);
        paramMap.put("taskstatus", CommonTypeEnum.ComplaintTaskEnum.UndisposedEnum.getCode());
        paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode());
        dataNum.addAll(complaintTaskDisposeService.getComplaintTaskDisposeNumDataByParams(paramMap));
        resultMap.put("count", dataNum.size());
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
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:05
     * @Description: 获取日常任务提醒数据（关联权限）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getRCTaskRemindRightData(Object userButtonAuthInMenu, String userId) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        boolean isFeedback = getIsHaveButton(userButtonAuthInMenu, "feedbackButton");
        boolean isDassign = getIsHaveButton(userButtonAuthInMenu, "assignButton");

        List<Map<String, Object>> dataNum = new ArrayList<>();
        if (isDassign) {
            paramMap.clear();
            paramMap.put("taskstatus", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
            paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
            dataNum.addAll(alarmTaskDisposeService.getTaskDisposeNumDataByParams(paramMap));
        }
        //待处理
        if (isFeedback) {
            paramMap.clear();
            //待处理
            paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode());
            paramMap.put("userid", userId);
            paramMap.put("taskstatus", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
            dataNum.addAll(alarmTaskDisposeService.getAlarmTaskDisposeNumDataByParams(paramMap));
        }
        resultMap.put("count", dataNum.size());

        return resultMap;
    }


    /**
     * @author: lip
     * @date: 2019/8/1 0001 上午 9:40
     * @Description: 获取处置任务人员选择数据（部门下）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getAlarmTaskDisposeUserData", method = RequestMethod.POST)
    public Object getAlarmTaskDisposeUserData(@RequestJson(value = "username", required = false) String username) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            param.put("username", username);
            List<Map<String, Object>> organizationUser = alarmTaskDisposeService.getAllOrganizationUser(param);
            if (organizationUser.size() > 0) {
                Set<String> userids = new HashSet<>();
                //获取拥有处置权限的用户ID
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("sysmodel", sysmodel);
                paramMap.put("buttoncode", buttoncode);
                List<Map<String, Object>> listdata = alarmTaskDisposeService.getDisposePersonSelectData(paramMap);
                if (listdata != null && listdata.size() > 0) {
                    for (Map<String, Object> map : listdata) {
                        userids.add(map.get("User_ID").toString());
                    }
                }
                Map<String, Set<Map<String, Object>>> organizationIdAndUser = new LinkedHashMap<>();
                Map<String, Object> organizationIdAndName = new LinkedHashMap<>();
                String organizationId;
                for (Map<String, Object> map : organizationUser) {
                    Set<Map<String, Object>> users = new HashSet<>();
                    organizationId = map.get("organization_id").toString();
                    organizationIdAndName.put(organizationId, map.get("organization_name"));
                    Map<String, Object> user = new HashMap<>();
                    if (map.get("user_id") != null && userids.contains(map.get("user_id"))) {
                        user.put("userid", map.get("user_id"));
                        user.put("username", map.get("user_name"));
                        user.put("phone", map.get("Phone"));
                        if (organizationIdAndUser.containsKey(organizationId)) {
                            users = organizationIdAndUser.get(organizationId);
                        }
                        users.add(user);
                        organizationIdAndUser.put(organizationId, users);
                    }
                }
                if (organizationIdAndUser.size() > 0) {
                    for (String key : organizationIdAndUser.keySet()) {
                        Map<String, Object> organization = new HashMap<>();
                        organization.put("organizationid", key);
                        organization.put("organizationname", organizationIdAndName.get(key));
                        organization.put("organizationsize", organizationIdAndUser.get(key).size());
                        organization.put("organizationuser", organizationIdAndUser.get(key));
                        resultList.add(organization);
                    }
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
     * @date: 2019/8/2 0002 下午 2:37
     * @Description:根据自定义参数获取待分派报警任务信息(app)
     * @updateUser:xsm
     * @updateDate:2019/8/26 上午8:58
     * @updateDescription:只获取两天的报警任务
     * @updateUser:xsm
     * @updateDate:2019/09/06 上午11:10
     * @updateDescription:只获取当天的报警任务
     * @updateUser:xsm
     * @updateDate:2021/12/13 上午10:08
     * @updateDescription:获取所有报警任务
     * @param: []
     */
    @RequestMapping(value = "getAlarmTaskListDataByParamMap", method = RequestMethod.POST)
    public Object getAlarmTaskListDataByParamMap(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                 @RequestJson(value = "starttime", required = false) String starttime,
                                                 @RequestJson(value = "endtime", required = false) String endtime,
                                                 @RequestJson(value = "currentstatus", required = false) String currentstatus,
                                                 @RequestJson(value = "statuslist") List<Integer> statuslist,
                                                 HttpServletRequest request, HttpSession session) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<String> statuss = new ArrayList<>();
            if (statuslist != null && statuslist.size() > 0) {
                for (Integer i : statuslist) {
                    statuss.add(i + "");
                }
            }
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("datasource", datasource);
            paramMap.put("userid", userId);
            paramMap.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPOverAlarmTaskDisposeEnum.getMenucode());
            if (starttime != null && endtime != null) {
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> datas = new HashMap<>();
            //按钮数据
            String params = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
            JSONObject jsonObject = JSONObject.fromObject(userButtonAuthInMenu);
            String buttonData = jsonObject.getString("data");
            JSONObject jsonObject2 = JSONObject.fromObject(buttonData);
            String listoperation = "";
            boolean flag = false;
            if (jsonObject2.size() > 0) {
                listoperation = jsonObject2.getString("tablebuttondata");
                List<Object> list = JSON.parseArray(listoperation);
                if (list != null && list.size() > 0) {
                    for (Object obj : list) {
                        Map entry = (Map) obj;
                        String value = entry.get("name").toString();
                        if ("assignButton".equals(value)) {//当拥有分派按钮时，该用户拥有权限，显示全部数据
                            flag = true;
                        }
                    }
                }
            }
            if (statuss.get(0).equals(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode() + "") ||
                    statuss.get(0).equals(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode() + "")) {
                if (flag == true) {
                    paramMap.put("hasauthor", "1");
                } else {
                    paramMap.put("hasauthor", "0");
                }
                paramMap.put("datauserid", userId);//数据权限
            } else {
                paramMap.put("hasauthor", "0");
            }
            paramMap.put("tasktype", AlarmTaskEnum.getCode());

            paramMap.put("statuslist", statuss);
            paramMap.put("currentstatus", currentstatus);//isconfirmend 不为空 且为1 则查需要自己审核的
            datas = alarmTaskDisposeService.getAllAlarmTaskDisposeListDataByParamMap(paramMap);
            //处理分页数据
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午2:07
     * @Description: 根据报警任务ID获取详情信息-app
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getAlarmTaskDetailByID", method = RequestMethod.POST)
    public Object getAlarmTaskDetailByID(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            result = alarmTaskDisposeService.getAlarmTaskDetailByID(id, userId, username);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/1/27 0027 下午 5:30
     * @Description: 根据报警任务ID获取详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, session]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointAlarmTaskDetailByID", method = RequestMethod.POST)
    public Object getMonitorPointAlarmTaskDetailByID(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Map<String, Object> result = alarmTaskDisposeService.getMonitorPointAlarmTaskDetailByID(id, userId, username);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/27 0027 上午 8:51
     * @Description: 按任务状态分组统计单个企业的任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "countPollutionAlarmTaskGroupByStatusByParamMap", method = RequestMethod.POST)
    public Object countPollutionAlarmTaskGroupByStatusByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                                 @RequestJson(value = "endtime", required = false) String endtime,
                                                                 @RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutionid", pollutionid);
            List<Map<String, Object>> resultMap = alarmTaskDisposeService.countPollutionAlarmTaskGroupByStatusByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/1/10 0010 下午 3:39
     * @Description: 通过自定义参数统计任务个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype]
     * @throws:
     */
    @RequestMapping(value = "countTaskInfoByParamMap", method = RequestMethod.POST)
    public Object countTaskInfoByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                          @RequestJson(value = "endtime", required = false) String endtime,
                                          @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                          @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                          @RequestJson(value = "tasktypes", required = false) Object tasktypes,
                                          @RequestJson(value = "datetype") String datetype, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List types = tasktypes == null ? new ArrayList() : (List) tasktypes;
            //判断配置文件是查询环保或安全数据
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            if (types.size() == 0) {//无数据权限
                if (categorys.contains("1")) {
                    types.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
                }
                if (categorys.contains("2")) {
                    types.addAll(Arrays.asList(new Integer[]{MonitorTaskEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode(),
                            CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode()}));
                }
            }
            List<Integer> onetypelist = new ArrayList<>();
            onetypelist.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
            //根据配置统计环保或安全的任务信息
            paramMap.put("fktasktypes", types);
            if ("month".equals(datetype)) {
                starttime += "-01";
                Calendar instance = Calendar.getInstance();
                Date dateYM = DataFormatUtil.getDateYM(endtime);
                instance.setTime(dateYM);
                int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
                endtime += actualMaximum;
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointtype", monitorpointtype);
            if (StringUtils.isNotBlank(monitorpointid)) {
                paramMap.put("monitorpointid", monitorpointid);
            }

            List<Map<String, Object>> resultList = alarmTaskDisposeService.getTaskInfoByTaskTypes(paramMap);
            //格式化日期
            for (Map<String, Object> map : resultList) {
                if ("week".equals(datetype) && map.get("TaskCreateTime") != null) {
                    String taskCreateTime = map.get("TaskCreateTime").toString();
                    Object timeOfYMWByString = DataFormatUtil.getTimeOfYMWByString(taskCreateTime);
                    map.put("TaskCreateTime", timeOfYMWByString);
                } else if ("month".equals(datetype) && map.get("TaskCreateTime") != null) {
                    String taskCreateTime = map.get("TaskCreateTime").toString();
                    Object timeOfYMWByString = DataFormatUtil.getTimeOfYMByString(taskCreateTime);
                    map.put("TaskCreateTime", timeOfYMWByString);
                }
            }

            Map<String, List<Map<String, Object>>> collect = resultList.stream().filter(m -> m.get("TaskCreateTime") != null).collect(Collectors.groupingBy(m -> m.get("TaskCreateTime").toString()));
            List<Map<String, Object>> resultlist = new ArrayList<>();
            //将数据组装分组统计个数
            for (String s : collect.keySet()) {
                Map<String, Object> resultdata = new HashMap<>();
                List<Map<String, Object>> tasktypelist = new ArrayList<>();
                resultdata.put("TaskCreateTime", s);
                resultdata.put("TaskTypedata", tasktypelist);
                List<Map<String, Object>> data = collect.get(s);
                Map<String, List<Map<String, Object>>> collect1 = data.stream().filter(m -> m.get("FK_TaskType") != null).collect(Collectors.groupingBy(m -> m.get("FK_TaskType").toString()));
                for (Integer i : onetypelist) {
                    Map<String, Object> tasktypedata = new HashMap<>();
                    List<String> onelist = new ArrayList<>();
                    if (i == AlarmTaskEnum.getCode()) {
                        onelist.add(AlarmTaskEnum.getCode() + "");
                        onelist.add(CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode() + "");
                        onelist.add(MonitorTaskEnum.getCode() + "");
                    } else if (i == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                        onelist.add(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode() + "");
                        onelist.add(CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode() + "");
                    } else if (i == ChangeAlarmTaskEnum.getCode()) {
                        onelist.add(CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode() + "");
                    }
                    long totalcount = 0;
                    long totalsize = 0;
                    for (String s1 : collect1.keySet()) {
                        if (onelist.contains(s1)) {
                            List<Map<String, Object>> data1 = collect1.get(s1);
                            //统计完成和忽略总个数
                            long count = data1.stream().filter(m -> m.get("TaskStatus") != null && ("3".equals(m.get("TaskStatus").toString()) || "4".equals(m.get("TaskStatus").toString()))).count();
                            totalcount = totalcount + count;
                            //所有状态任务个数
                            int size = data1.size();
                            totalsize = totalsize + size;
                        }
                    }
                    tasktypedata.put("TaskType", i);
                    tasktypedata.put("count", totalcount);
                    tasktypedata.put("size", totalsize);
                    tasktypelist.add(tasktypedata);
                }
                resultlist.add(resultdata);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultlist.stream().sorted(Comparator.comparing(m -> m.get("TaskCreateTime").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/4/8 0008 下午 5:32
     * @Description: 通过多参数获取历史任务人员完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, tasktypes]
     * @throws:
     */
    @RequestMapping(value = "countUserTaskInfoByParamMap", method = RequestMethod.POST)
    public Object countUserTaskInfoByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                              @RequestJson(value = "endtime", required = false) String endtime,
                                              @RequestJson(value = "tasktypes", required = false) Object tasktypes) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fktasktypes", tasktypes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> resultList = alarmTaskDisposeService.countUserTaskInfoByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 10:18
     * @Description: 统计报警任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countAlarmTaskCompletionStatusByParamMap", method = RequestMethod.POST)
    public Object countAlarmTaskCompletionStatusByParamMap(@RequestJson(value = "tasktype", required = false) Integer tasktype,
                                                           @RequestJson(value = "starttime", required = false) String starttime,
                                                           @RequestJson(value = "endtime", required = false) String endtime,
                                                           @RequestJson(value = "tasktypes", required = false) List<Integer> tasktypes,
                                                           @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                           @RequestJson(value = "monitorpointtypes", required = false) List<String> monitorpointtypes
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("pollutionid", pollutionid);
            if (tasktypes != null) {
                tasktypes.add(tasktype);
            } else {
                tasktypes = new ArrayList<>();
                tasktypes.add(AlarmTaskEnum.getCode());
            }
            paramMap.put("tasktypes", tasktypes);
            paramMap.put("category", 1);
            //已完成  状态
            paramMap.put("completions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
            paramMap.put("uncompletions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode(), CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode()));
            Map<String, Object> resultMap = alarmTaskDisposeService.countAlarmTaskCompletionStatusByParamMap(paramMap);
            resultMap.putIfAbsent("completed", "0");
            resultMap.putIfAbsent("uncompleted", "0");
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/06/03 0003 下午 4:22
     * @Description: 首页工单一键派发功能(分派任务)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
   /* @RequestMapping(value = "oneClickAssignTaskByParamMap", method = RequestMethod.POST)
    public Object oneClickAssignTaskByParamMap(@RequestJson(value = "tasktype") Integer tasktype,
                                               @RequestJson(value = "formdata") Object jsondata) throws Exception {
        try {
            Map<String, Object> formdata = (Map<String, Object>) jsondata;
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            if (tasktype == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                devOpsTaskDisposeService.saveDevOpsTaskInfo(userId, username, formdata);
                //消息推送
                String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
                rabbitmqController.sendTaskDirectQueue(messagetype);
                devOpsTaskDisposeController.sendTaskMessage(username, formdata);
            } else if (tasktype == CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode()) {//报警任务
                addTaskDisposeInfo(formdata, userId, username, tasktype);//保存报警信息
                sendAlarmTaskMessage(username, formdata);//推送信息
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/

    /**
     * @author: chengzq
     * @date: 2021/1/25 0025 下午 5:28
     * @Description: 通过多参数获取监测点报警任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum, isAuth 1只查自己和处置人为null的数据，2全查]
     * @throws:
     */
    @RequestMapping(value = "getMonitorPointAlarmTaskInfoParamMap", method = RequestMethod.POST)
    public Object getMonitorPointAlarmTaskInfoParamMap(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                       @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                       @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                       @RequestJson(value = "starttime", required = false) String starttime,
                                                       @RequestJson(value = "endtime", required = false) String endtime,
                                                       @RequestJson(value = "taskstatus", required = false) String taskstatus,
                                                       @RequestJson(value = "isAuth") Integer isAuth) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            String user = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            if (isAuth == 1) {
                paramMap.put("userid", RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class));
            }
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("user", user);
            paramMap.put("taskstatus", taskstatus);
            List<Map<String, Object>> monitrpointTaskInfoByParamMap = alarmTaskDisposeService.getMonitrpointTaskInfoByParamMap(paramMap);
            resultMap.put("total", monitrpointTaskInfoByParamMap.size());
            if (pagenum != null && pagesize != null) {
                monitrpointTaskInfoByParamMap = monitrpointTaskInfoByParamMap.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", monitrpointTaskInfoByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "CountMonitorPointAlarmTask", method = RequestMethod.POST)
    public Object CountMonitorPointAlarmTask() throws Exception {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            return AuthUtil.parseJsonKeyToLower("success", alarmTaskDisposeService.CountMonitrpointTaskInfoByParamMap(userid));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @RequestMapping(value = "ExportMonitorPointAlarmTaskInfoParamMap", method = RequestMethod.POST)
    public Object ExportMonitorPointAlarmTaskInfoParamMap(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "isAuth") Integer isAuth, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            String user = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);

            if (isAuth == 1) {
                paramMap.put("userid", RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class));
            }
            paramMap.put("starttime", starttime);
            paramMap.put("user", user);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> monitrpointTaskInfoByParamMap = alarmTaskDisposeService.getMonitrpointTaskInfoByParamMap(paramMap);

            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("环境监测点名称");
            headers.add("报警污染物");
            headers.add("报警时间");
            headers.add("处置人");
            headers.add("状态");
            headersField.add("monitorpointname");
            headersField.add("pollutantnames");
            headersField.add("taskcreatetime");
            headersField.add("users");
            headersField.add("taskstatusname");


            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, monitrpointTaskInfoByParamMap, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("环境监测点任务管理", response, request, bytesForWorkBook);


            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/12 0012 下午 12:05
     * @Description: 获取自己的报警任务和日常任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum, fktasktype]
     * @throws:
     */
    @RequestMapping(value = "getOwnTaskDisposeInfoByParamMap", method = RequestMethod.POST)
    public Object getOwnTaskDisposeInfoByParamMap(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                  @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                  @RequestJson(value = "fktasktype", required = false) String fktasktype,
                                                  @RequestJson(value = "starttime", required = false) String starttime,
                                                  @RequestJson(value = "endtime", required = false) String endtime,
                                                  @RequestJson(value = "hasauthor", required = false) String hasauthor,
                                                  @RequestJson(value = "sysmodel", required = false) String sysmodelflag,
                                                  @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                  @RequestJson(value = "monitorpointtypes", required = false) Object monitorpointtypes,
                                                  @RequestJson(value = "recoverystatuslist", required = false) Object recoverystatuslist,
                                                  @RequestJson(value = "currenttaskstatuslist", required = false) Object currenttaskstatuslist,
                                                  @RequestJson(value = "statuslist", required = false) Object statuslist) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("recoverystatuslist", recoverystatuslist);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("categorys", categorys);
            if (StringUtils.isNotBlank(sysmodelflag)) {
                setAssignButton(userid, resultMap, sysmodelflag);
            } else {
                setAssignButton(userid, resultMap, sysmodel);
            }
            List<String> rightList = getRightList(userid);
            String itemCode = "";
            if (StringUtils.isBlank(fktasktype) || fktasktype.equals(DailyEnum.getCode().toString())) {//日常任务
                paramMap.put("fktasktype", DailyEnum.getCode());//默认查询日常任务
                itemCode = CommonTypeEnum.ModuleItemCodeEnum.RCFPEnum.getCode();
            } else if (fktasktype.equals(AlarmTaskEnum.getCode().toString())) {//报警任务
                paramMap.put("fktasktype", AlarmTaskEnum.getCode());
                itemCode = CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode();
            }
            if (rightList.contains(itemCode)) {
                resultMap.put("ishaveassignbutton", true);
            } else {
                resultMap.put("ishaveassignbutton", false);
            }
            if (pagenum != null && pagesize != null) {
                PageHelper.startPage(pagenum, pagesize);
            }
            long total = 0;
            List<Map<String, Object>> ownTaskDisposeInfoByParamMap = new ArrayList<>();
            if (currenttaskstatuslist != null && ((List<String>) currenttaskstatuslist).size() == 0 && (boolean) resultMap.get("ishaveassignbutton")) {
                //待分派  有权限
                //直接分页查询
                if (paramMap.get("fktasktype") != null || paramMap.get("fktasktypes") != null) {
                    ownTaskDisposeInfoByParamMap = alarmTaskDisposeService.getUnassignedTaskDisposeInfoByParamMap(paramMap);
                    PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(ownTaskDisposeInfoByParamMap);
                    total = pageInfo.getTotal();
                }
            } else {//其它状态任务  （已分派、待处理、处理中、我完成的、抄送给我的、待审核）
                paramMap.put("userid", userid);
                List<Integer> statuslists = new ArrayList<>();
                if (currenttaskstatuslist != null) {
                    statuslists = (List<Integer>) currenttaskstatuslist;
                    //如果是已分派则不需要权限
                    if (statuslists.contains(GenerateTaskEnum.getCode()) && (boolean) resultMap.get("ishaveassignbutton")) {
                        paramMap.remove("userid");
                    }
                    //如果是已完成  且权限标记为no
                    if (statuslists.contains(CommonTypeEnum.AlarmTaskStatusEnum.ComplateTaskEnum.getCode()) && "no".equals(hasauthor)) {
                        paramMap.remove("userid");
                    }
                    List<String> currenttaskstatusstrlist = new ArrayList<>();
                    for (Integer integer : statuslists) {
                        currenttaskstatusstrlist.add(CommonTypeEnum.AlarmTaskStatusEnum.getCodeByInteger(integer).getName());
                    }
                    paramMap.put("currenttaskstatuslist", currenttaskstatusstrlist);
                }
                paramMap.put("statuslist", statuslist);
                if (paramMap.get("fktasktype") != null || paramMap.get("fktasktypes") != null) {
                    ownTaskDisposeInfoByParamMap = alarmTaskDisposeService.getOwnTaskDisposeIdsByParamMap(paramMap);
                    PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(ownTaskDisposeInfoByParamMap);
                    total = pageInfo.getTotal();
                }
            }
            resultMap.put("total", total);
            resultMap.put("datalist", ownTaskDisposeInfoByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/3/12 0012 下午 12:05
     * @Description: 获取自己的投诉任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum, fktasktype]
     * @throws:
     */
    @RequestMapping(value = "getOwnPetitionInfoByParamMap", method = RequestMethod.POST)
    public Object getOwnPetitionInfoByParamMap(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                               @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                               @RequestJson(value = "hasauthor", required = false) String hasauthor,
                                               @RequestJson(value = "sysmodel", required = false) String sysmodelflag,
                                               @RequestJson(value = "currenttaskstatuslist", required = false) Object currenttaskstatuslist,
                                               @RequestJson(value = "statuslist", required = false) Object statuslist) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            //是否拥有分派按钮
            if (StringUtils.isNotBlank(sysmodelflag)) {
                setAssignButton(userid, resultMap, sysmodelflag);
            } else {
                setAssignButton(userid, resultMap, sysmodel);
            }
            List<Integer> statuslists = new ArrayList<>();
            if (currenttaskstatuslist != null) {
                statuslists = (List<Integer>) currenttaskstatuslist;
                //如果是已分派则不需要权限
                if (statuslists.contains(GenerateTaskEnum.getCode()) && (boolean) resultMap.get("ishaveassignbutton")) {
                    paramMap.remove("userid");
                }
                //如果是已完成  且权限标记为no
                if (statuslists.contains(CommonTypeEnum.AlarmTaskStatusEnum.ComplateTaskEnum.getCode()) && "no".equals(hasauthor)) {
                    paramMap.remove("userid");
                }
                List<String> currenttaskstatusstrlist = new ArrayList<>();
                for (Integer integer : statuslists) {
                    currenttaskstatusstrlist.add(CommonTypeEnum.AlarmTaskStatusEnum.getCodeByInteger(integer).getName());
                }
                paramMap.put("currenttaskstatuslist", currenttaskstatusstrlist);
            }
            paramMap.put("statuslist", statuslist);
            List<Map<String, Object>> ownTaskDisposeInfoByParamMap = alarmTaskDisposeService.getOwnPetitionByParamMap(paramMap);

            Iterator<Map<String, Object>> iterator = ownTaskDisposeInfoByParamMap.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> stringObjectMap = iterator.next();
                List<Map<String, Object>> list = stringObjectMap.get("taskrecordlist") == null ? new ArrayList<>() : (List<Map<String, Object>>) stringObjectMap.get("taskrecordlist");
                String taskstatus = stringObjectMap.get("taskstatus") == null ? "" : stringObjectMap.get("taskstatus").toString();
                Map<String, Object> complateMap = list.stream().filter(m -> m.get("currenttaskstatus") != null && ("完成".equals(m.get("currenttaskstatus").toString()) || "已完成".equals(m.get("currenttaskstatus").toString()) || "反馈信息".equals(m.get("currenttaskstatus").toString()) || "忽略任务".equals(m.get("currenttaskstatus").toString()))).findFirst().orElse(new HashMap<>());
                Map<String, Object> fenpairenMap = list.stream().filter(m -> m.get("currenttaskstatus") != null && "分派任务".equals(m.get("currenttaskstatus").toString())).findFirst().orElse(new HashMap<>());
//                Map<String, Object> fenpairenMap = list.stream().filter(m -> m.get("currenttaskstatus") != null && "完成".equals(m.get("currenttaskstatus").toString())).findFirst().orElse(new HashMap<>());
                Set<String> zhuanbanlist = new HashSet<>();
                Set<String> zhixinguserlist = new HashSet<>();
                if (list.stream().filter(n -> n.get("currenttaskstatus") != null && "转办".equals(n.get("currenttaskstatus").toString())).count() > 0) {
                    String max = list.stream().filter(n -> n.get("currenttaskstatus") != null && n.get("statusandtime") != null && "转办".equals(n.get("currenttaskstatus").toString())).map(m -> m.get("statusandtime").toString()).max(String::compareTo).orElse("");
                    zhixinguserlist = list.stream().filter(n -> n.get("currenttaskstatus") != null && n.get("statusandtime") != null && n.get("username") != null && "转办".equals(n.get("currenttaskstatus").toString())
                            && max.equals(n.get("statusandtime").toString())).map(m -> m.get("username").toString()).collect(Collectors.toSet());
                    zhuanbanlist = zhixinguserlist;
                } else {
                    zhixinguserlist = (Set<String>) list.stream().filter(m -> m.get("currenttaskstatus") != null && "待处理".equals(m.get("currenttaskstatus").toString())).findFirst().orElse(new HashMap<>()).get("users");
                }

                List<Map<String, Object>> copylist = list.stream().filter(m -> m.get("currenttaskstatus") != null
                        && ("抄送已读".equals(m.get("currenttaskstatus").toString())
                        || "抄送".equals(m.get("currenttaskstatus").toString()))).collect(Collectors.toList());

                //转办
                if (zhuanbanlist != null && zhuanbanlist.size() > 0) {
                    String zhuanbanrens = zhuanbanlist.stream().collect(Collectors.joining("、"));
                    String copyperson = copylist.stream().filter(m -> m.get("users") != null).map(m -> (Set<String>) m.get("users")).flatMap(m -> m.stream()).collect(Collectors.joining("、"));

                    //将转办给别人的任务去掉
                    if (!zhuanbanrens.contains(username) && !copyperson.contains(username) && !statuslists.contains(0)) {
                        iterator.remove();
                        continue;
                    }
                }
                if (fenpairenMap != null && !"1".equals(taskstatus)) {
                    //分派人
                    Set<String> fenpaiuserlist = fenpairenMap.get("users") == null ? new HashSet<>() : (Set<String>) fenpairenMap.get("users");
                    String user = fenpaiuserlist.stream().findFirst().orElse("");
                    stringObjectMap.put("fenpaitime", fenpairenMap.get("taskhandletime"));
                    stringObjectMap.put("fenpairen", user);
                }
                if (zhixinguserlist != null && !"1".equals(taskstatus)) {
                    //执行人
                    String user = zhixinguserlist.stream().collect(Collectors.joining("、"));
                    stringObjectMap.put("zhixingren", user);
                    if (user.contains(username)) {
                        stringObjectMap.put("isfeedback", 1);//能否反馈 1是0否
                    } else {
                        stringObjectMap.put("isfeedback", 0);
                    }
                } else {
                    stringObjectMap.put("zhixingren", "");
                    stringObjectMap.put("isfeedback", 0);
                }
                stringObjectMap.put("completetime", complateMap.get("taskhandletime"));
                stringObjectMap.remove("taskrecordlist");
            }
            int size = ownTaskDisposeInfoByParamMap.size();
            resultMap.put("total", size);
            if (pagesize != null && pagenum != null) {
                ownTaskDisposeInfoByParamMap = ownTaskDisposeInfoByParamMap.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("datalist", ownTaskDisposeInfoByParamMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2021/3/18 0018 下午 1:19
     * @Description: 统计
     * @updateUser:xsm
     * @updateDate:2021/11/8 0008 下午 2:45
     * @updateDescription:任务表新增恢复状态字段 根据该字段进行筛选 只查未恢复的
     * @param: [userid]
     * @throws:
     */
    @RequestMapping(value = "countAlarmTaskData", method = RequestMethod.POST)
    public Object countAlarmTaskData(@RequestJson(value = "userid", required = false) String userid) throws Exception {
        try {
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            if (StringUtils.isBlank(userid)) {
                userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            }

            List<String> rightList = getRightList(userid);

            int num = 0;
            //报警工单
            boolean isHaveAlarm;
            Integer tasktype = AlarmTaskEnum.getCode();
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                isHaveAlarm = true;
            } else {
                isHaveAlarm = false;
            }
            Map<String, Object> alarmMap = getDataMap(isHaveAlarm, tasktype, userid);
            datalist.add(alarmMap);
            num += Integer.parseInt(alarmMap.get("subnum").toString());
            //运维工单

            boolean isHaveDev;
            tasktype = DevOpsTaskEnum.getCode();
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                isHaveDev = true;
            } else {
                isHaveDev = false;
            }
            Map<String, Object> devMap = getDataMap(isHaveDev, tasktype, userid);
            datalist.add(devMap);
            num += Integer.parseInt(devMap.get("subnum").toString());


            //突变工单
            boolean isHaveTB;
            tasktype = ChangeAlarmTaskEnum.getCode();
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.TBEnum.getCode())) {
                isHaveTB = true;
            } else {
                isHaveTB = false;
            }
            Map<String, Object> tbMap = getDataMap(isHaveTB, tasktype, userid);
            datalist.add(tbMap);
            num += Integer.parseInt(tbMap.get("subnum").toString());
            //日常工单

            boolean isHaveRC;
            tasktype = DailyEnum.getCode();
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.RCFPEnum.getCode())) {
                isHaveRC = true;
            } else {
                isHaveRC = false;
            }

            Map<String, Object> rcMap = getDataMap(isHaveRC, tasktype, userid);
            datalist.add(rcMap);
            num += Integer.parseInt(rcMap.get("subnum").toString());
            result.put("ishaveassignbutton", isHaveRC || isHaveTB || isHaveDev || isHaveAlarm);
            result.put("datalist", datalist);
            result.put("num", num);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> getDataMap(boolean isHaveAlarm, int tasktype, String userid) {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("userid", userid);
        dataMap.put("isHave", isHaveAlarm);
        dataMap.put("tasktype", tasktype);
        List<Map<String, Object>> dataList = alarmTaskDisposeService.getTaskDisposeDataListByParam(dataMap);

        List<Map<String, Object>> lastFlowData = alarmTaskDisposeService.getLastFlowDataListByParam(dataMap);

        dataMap.clear();
        dataMap.put("FK_TaskType", tasktype);
        dataMap.put("tasktypename", CommonTypeEnum.TaskTypeEnum.getCodeByInteger(Integer.valueOf(tasktype)).getName());
        Map<String, Object> alarmDataMap = new HashMap<>();
        int num = countTaskDataList(dataList, isHaveAlarm, alarmDataMap, userid, lastFlowData);
        dataMap.put("countmap", alarmDataMap);
        dataMap.put("subnum", num);
        return dataMap;
    }

    private int countTaskDataList(List<Map<String, Object>> dataList, boolean isHave, Map<String, Object> result, String userid, List<Map<String, Object>> lastFlowData) {

        //待分派
        Set<String> needassignSet = new HashSet<>();
        //已分派（currenttaskstatus=分派任务）
        Set<String> distributorSet = new HashSet<>();
        Set<String> needfeedbackSet = new HashSet<>();
        //已处理
        Set<String> feedbackingSet = new HashSet<>();
        //抄送
        Set<String> fcopytomeSet = new HashSet<>();
        //抄送已读
        Set<String> havecopytomeSet = new HashSet<>();

        //待审核（审核）
        Set<String> reviewedSet = new HashSet<>();

        //已审核（taskstatus!=0,1；关联处理人）
        Set<String> havereviewedSet = new HashSet<>();

        //已完成
        Set<String> ascloseSet = new HashSet<>();

        //我完成
        Set<String> mecloseSet = new HashSet<>();

        int taskstatus;
        String id;


        //0待分派、1待处理、2处理中、4已完成、5待审核
        if (isHave) {//有分派权限
            for (Map<String, Object> dataMap : dataList) {
                taskstatus = Integer.parseInt(dataMap.get("taskstatus").toString());
                id = dataMap.get("pk_taskid").toString();
                if (taskstatus == 0) {//待分派
                    needassignSet.add(id);
                } else {//已分派
                    distributorSet.add(id);
                }
                //已完成（看到所有已完成的任务）
                if (taskstatus == 4) {
                    ascloseSet.add(id);
                }
            }
        }

        Map<String, List<Map<String, Object>>> idAndDataList = lastFlowData.stream().collect(Collectors.groupingBy(m -> m.get("fk_taskid").toString()));
        boolean isLastData;
        for (Map<String, Object> dataMap : dataList) {
            taskstatus = Integer.parseInt(dataMap.get("taskstatus").toString());
            id = dataMap.get("pk_taskid").toString();
            if (userid.equals(dataMap.get("fk_taskhandleuserid"))) {
                //待处理、已处理
                if (Arrays.asList(1, 2).contains(taskstatus)
                        && Arrays.asList("待处理", "处理中", "转办").contains(dataMap.get("currenttaskstatus"))
                ) {//待处理(taskhandletime)
                    isLastData = isLastData(idAndDataList.get(id), userid);
                    if (isLastData) {
                        needfeedbackSet.add(id);
                    }

                } else if (Arrays.asList(3, 4, 5).contains(taskstatus)
                        && Arrays.asList("待处理", "转办").contains(dataMap.get("currenttaskstatus"))
                ) {//已处理
                    feedbackingSet.add(id);
                }

                //抄送未读、抄送已读
                if ("抄送".equals(dataMap.get("currenttaskstatus"))) {
                    fcopytomeSet.add(id);
                } else if ("抄送已读".equals(dataMap.get("currenttaskstatus"))) {
                    havecopytomeSet.add(id);
                }


                //待审核、已审核
                if (taskstatus == 5 && "审核".equals(dataMap.get("currenttaskstatus"))) {
                    reviewedSet.add(id);
                } else if ("审核".equals(dataMap.get("currenttaskstatus"))) {
                    havereviewedSet.add(id);
                }
                //我完成
                if (taskstatus == 4) {
                    mecloseSet.add(id);
                    ascloseSet.add(id);
                }
            }


        }
        //待分派
        result.put("needassign", needassignSet.size());
        //已分派
        result.put("distributor", distributorSet.size());
        //待处理
        result.put("needfeedback", needfeedbackSet.size());
        //已处理
        result.put("feedbacking", feedbackingSet.size());
        //抄送未读
        result.put("copytome", fcopytomeSet.size());
        //抄送已读
        result.put("havecopytome", havecopytomeSet.size());
        //待审核
        result.put("reviewed", reviewedSet.size());
        //已审核
        result.put("havereviewed", havereviewedSet.size());
        //已完成
        result.put("hasclose", ascloseSet.size());
        //我完成
        result.put("meclose", mecloseSet.size());
        return needassignSet.size() + needfeedbackSet.size() + fcopytomeSet.size() + reviewedSet.size();


    }

    private boolean isLastData(List<Map<String, Object>> dataList, String userid) {
        boolean isLastData = false;
        if (dataList != null) {
            for (Map<String, Object> dataMap : dataList) {
                if (userid.equals(dataMap.get("fk_taskhandleuserid"))) {
                    isLastData = true;
                    break;
                }
            }
        }
        return isLastData;


    }

    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletypes", Arrays.asList(
                CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode(),
                CommonTypeEnum.ModuleTypeEnum.DevEnum.getCode(),
                CommonTypeEnum.ModuleTypeEnum.TBEnum.getCode(),
                CommonTypeEnum.ModuleTypeEnum.RCFPEnum.getCode()
                )


        );
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }


    @RequestMapping(value = "updateAlarmTaskDataByTaskid", method = RequestMethod.POST)
    public Object updateAlarmTaskDataByTaskid(@RequestJson(value = "taskid") String taskid) throws Exception {
        try {
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(taskid);
            useridsByTaskid.add(userid);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userid);
            paramMap.put("currenttaskstatus", "抄送");
            paramMap.put("taskid", taskid);
            alarmTaskDisposeService.updateByUseridAndTaskid(paramMap);
            pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private Map<String, Integer> initTaskCountMap(Integer needassign, Integer needfeedback, Integer feedbacking, Integer hasclose, Integer copytome, Integer distributor, Integer reviewed) {
        Map<String, Integer> result = new HashMap<>();
        //待分派
        result.put("needassign", needassign);
        //待处理
        result.put("needfeedback", needfeedback);
        //处理中
        result.put("feedbacking", feedbacking);
        //已完成
        result.put("hasclose", hasclose);
        //抄送
        result.put("copytome", copytome);
        //分派
        result.put("distributor", distributor);
        //待审核
        result.put("reviewed", reviewed);
        return result;
    }


    /**
     * @author: chengzq
     * @date: 2021/3/16 0016 下午 7:12
     * @Description: 设置任务按钮信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [userid, resultMap]
     * @throws:
     */
    private void setAssignButton(String userid, Map<String, Object> resultMap, String sysmodel) {
        //按钮数据
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("datasource", datasource);
        paramMap.put("sysmodel", sysmodel);
        //按钮数据
        String params = AuthUtil.paramDataFormat(paramMap);
        Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(params);
        JSONObject jsonObject = JSONObject.fromObject(userButtonAuthInMenu);
        String buttonData = jsonObject.getString("data");
        JSONObject jsonObject2 = JSONObject.fromObject(buttonData);
        Map<String, Object> buttondatamap = new HashMap<>();//按钮
        String topoperations = "";
        String listoperation = "";
        if (jsonObject2.size() > 0) {
            topoperations = jsonObject2.getString("topbuttondata");
            listoperation = jsonObject2.getString("tablebuttondata");
        }
        buttondatamap.put("topbuttondata", topoperations);
        buttondatamap.put("tablebuttondata", listoperation);
        resultMap.put("buttondatamap", buttondatamap);

    }


    /**
     * @author: chengzq
     * @date: 2021/3/17 0017 上午 11:04
     * @Description: 推送任务个数到首页
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [userid, resultMap]
     * @throws:
     */
    public void pushAlarmTaskToPageHome(List<String> userids) throws Exception {
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        for (String userid : userids) {
            Map<String, Object> map = new HashMap<>();
            map.put("userid", userid);
            map.put("messagedata", countAlarmTaskData(userid));
            messageanduserdata.add(map);
        }
        socketCommonService.sendAppointMessageToAppointClient(HomePageMonitorTaskEnum.getSocketMethod(), messageanduserdata);
    }

    /**
     * @author: xsm
     * @date: 2021/12/03 0003 下午 3:06
     * @Description:抄送人确认完成任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addCarbonCopyUserCompleteTaskInfo", method = RequestMethod.POST)
    public Object addCarbonCopyUserCompleteTaskInfo(@RequestJson(value = "taskid") String taskid,
                                                    @RequestJson(value = "comment", required = false) Object comment) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(taskid);
            alarmTaskDisposeManagement.setUpdatetime(new Date());
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode());
            alarmTaskDisposeService.addCarbonCopyUserCompleteTaskInfo(userId, comment, alarmTaskDisposeManagement);

            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            //List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(alarmTaskDisposeManagement.getPkTaskid());
            //pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/03 0003 下午 4:17
     * @Description:抄送人打回任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addCarbonCopyUserRepulseTaskInfo", method = RequestMethod.POST)
    public Object addCarbonCopyUserRepulseTaskInfo(@RequestJson(value = "formdata") Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(jsonObject.get("pktaskid").toString());
            alarmTaskDisposeManagement.setUpdatetime(new Date());
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode());
            alarmTaskDisposeService.addCarbonCopyUserRepulseTaskInfo(userId, alarmTaskDisposeManagement, jsonObject);

            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            //List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(alarmTaskDisposeManagement.getPkTaskid());
            //pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/08 0008 下午 3:59
     * @Description: 根据自定义参数获取某报警时段的超标超限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [taskcreatetime, monitorpointtype, dgimn, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getOnePointOverDataByParamMap", method = RequestMethod.POST)
    public Object getOnePointOverDataByParamMap(@RequestJson(value = "starttime", required = true) String starttime,
                                                @RequestJson(value = "endtime", required = true) String endtime,
                                                @RequestJson(value = "monitorpointtype", required = true) String monitorpointtype,
                                                @RequestJson(value = "dgimn", required = true) String dgimn,
                                                @RequestJson(value = "pointname") String pointname,
                                                @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                @RequestJson(value = "datatype") String datatype,
                                                @RequestJson(value = "pagesize", required = true) Integer pagesize,
                                                @RequestJson(value = "pagenum", required = true) Integer pagenum
    ) throws Exception {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("dgimn", dgimn);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("codes", pollutantcodes);
            paramMap.put("pointname", pointname);
            paramMap.put("pollutantname", pollutantname);
            paramMap.put("datatype", datatype);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            Map<String, Object> result = alarmTaskDisposeService.getOnePointOverDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2022/05/24 0024 下午 17:31
     * @Description:根据自定义参数获取报警任务列表信息（2.0概览首页弹窗）（无处置）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getAlarmTaskDataForOverViewByParamMap", method = RequestMethod.POST)
    public Object getAlarmTaskDataForOverViewByParamMap(@RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                        @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                        @RequestJson(value = "starttime", required = false) String starttime,
                                                        @RequestJson(value = "endtime", required = false) String endtime,
                                                        @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                        @RequestJson(value = "recoverystatuslist", required = false) List<String> recoverystatuslist,
                                                        @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                        @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                        HttpServletRequest request, HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("recoverystatuslist", recoverystatuslist);
            paramMap.put("starttime", starttime);
            paramMap.put("tasktype", AlarmTaskEnum.getCode());
            paramMap.put("userid", userId);
            paramMap.put("endtime", endtime);
            //根据任务状态类型赋值报警任务状态
            paramMap.put("hasauthor", "1");
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("statuslist", statuslist);
            Map<String, Object> resultdata = new HashMap<>();
            resultdata = alarmTaskDisposeService.getAlarmTaskDataForOverViewByParamMap(paramMap);
            Map<String, Object> result = new HashMap<>();
            //返回数据
            result.put("total", resultdata.get("total"));
            result.put("datalist", resultdata.get("datalist"));
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/14 0014 下午 2:37
     * @Description: 通过自定义参数统计任务完成情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, datetype]
     * @throws:
     */
    @RequestMapping(value = "countTaskCompletionDataByParamMap", method = RequestMethod.POST)
    public Object countTaskCompletionDataByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                    @RequestJson(value = "endtime", required = false) String endtime,
                                                    @RequestJson(value = "tasktypes", required = false) List<String> tasktypes,
                                                    HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            List<Integer> onetypelist = new ArrayList<>();
            onetypelist.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
            paramMap.put("fktasktypes", tasktypes);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("category", 1);
            List<Map<String, Object>> resultList = alarmTaskDisposeService.countTaskCompletionDataByParamMap(paramMap);
            if (resultList != null && resultList.size() > 0) {
                Map<String, List<Map<String, Object>>> collect = resultList.stream().filter(m -> m.get("FK_TaskType") != null).collect(Collectors.groupingBy(m -> m.get("FK_TaskType").toString()));
                List<Map<String, Object>> onelist;
                for (String tasktype : collect.keySet()) {
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("tasktype", tasktype);
                    onelist = collect.get(tasktype);
                    onelist = onelist.stream().sorted(Comparator.comparing(m -> m.get("TaskCreateTime").toString())).collect(Collectors.toList());
                    onemap.put("listdata", onelist);
                    result.add(onemap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
