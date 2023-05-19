package com.tjpu.sp.controller.environmentalprotection.taskmanagement;


import com.alibaba.fastjson.JSON;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.environmentalprotection.devopsinfo.EntDevOpsInfoVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.AirMonitorStationVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.OtherMonitorPointVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterStationVO;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.environmentalprotection.tracesource.TaskFlowRecordInfoVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.SocketCommonService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterStationService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.ComplaintTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.DevOpsTaskDisposeService;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年12月10日 下午5:02
 * @Description:运维任务处置管理接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("devOpsTaskDisposeManagement")
public class DevOpsTaskDisposeController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private DevOpsTaskDisposeService devOpsTaskDisposeService;
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;
    @Autowired
    private ComplaintTaskDisposeService complaintTaskDisposeService;
    @Autowired
    private JGUserRegisterInfoService jgUserRegisterInfoService;
    @Autowired
    private AuthSystemMicroService authSystemMicroService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private WaterStationService waterStationService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private AlarmTaskDisposeController alarmTaskDisposeController;

    private String sysmodel = "operateTaskManagement";

    //反馈按钮
    private String buttoncode = "feedbackButton";


    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;



    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletypes",
                Arrays.asList(
                        CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.DevEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.TBEnum.getCode()
                )
        );
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }
    /**
     * @author: xsm
     * @date: 2019/7/16 0016 上午 8:43
     * @Description:根据自定义参数获取运维报警任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getDevOpsTaskDisposeManagementListDataByParamMap", method = RequestMethod.POST)
    public Object getDevOpsTaskDisposeManagementListDataByParamMap(@RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                                   @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                                   @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                                   @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                                   @RequestJson(value = "monitorpointtypes", required = false) List<String> monitorpointtypes,
                                                                   @RequestJson(value = "recoverystatuslist", required = false) List<String> recoverystatuslist,
                                                                   @RequestJson(value = "starttime", required = false) String starttime,
                                                                   @RequestJson(value = "endtime", required = false) String endtime,
                                                                   @RequestJson(value = "fktasktype", required = false) String fktasktype,
                                                                   @RequestJson(value = "sysmodelcode", required = false) String sysmodelcode,
                                                                   @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                                   @RequestJson(value = "showcurrentuser", required = false) Integer showcurrentuser,
                                                                   @RequestJson(value = "csstatus", required = false) Integer csstatus,
                                                                   HttpServletRequest request, HttpSession session) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            //paramMap.put("datauserid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("showcurrentuser", showcurrentuser);
            paramMap.put("csstatus", csstatus);
            paramMap.put("statuslist", statuslist);
            if (sysmodelcode != null && !"".equals(sysmodelcode)) {
                paramMap.put("sysmodel", sysmodelcode);
            } else {//为空则传默认的菜单code
                paramMap.put("sysmodel", sysmodel);
            }
            paramMap.put("starttime", starttime);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("recoverystatuslist", recoverystatuslist);
            paramMap.put("endtime", endtime);
            paramMap.put("fktasktype", fktasktype);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("sign", "pc");
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
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
                List<Object> list = JSON.parseArray(listoperation);
            }//显示我已审核数据
           /* if (statuslist != null && statuslist.size() > 0 && statuslist.contains("-1") ) {
                statuslist = statuslist == null ? new ArrayList<>() : statuslist;
                paramMap.put("isAudit", 1);
                statuslist.remove("-1");
                paramMap.put("statuslist", statuslist);
            }*/

            List<String> rightList = getRightList(userId);
            boolean isRight = false;
            if (StringUtils.isNotBlank(monitorpointid)||rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())){
                isRight = true;
            }
            if (showcurrentuser!=null){//关联处理人
                if (showcurrentuser==0){//我分派、已完成
                    //已分派：所有已分派任务
                    if (isRight){
                        paramMap.put("hasauthor", "1");
                    }else {
                        paramMap.put("hasauthor", "0");
                    }
                    if (statuslist.size()==1&&statuslist.get(0).equals("4")){//已完成

                    }else {//已分派（没有权限，没有任务）
                        if (!isRight){//没有权限
                            statuslist = Arrays.asList("-1");
                        }
                    }
                }else if (showcurrentuser==1&&csstatus!=null){//抄送我
                    paramMap.put("hasauthor", "0");
                    if (csstatus==0){//未读
                        paramMap.put("currenttaskstatuss",Arrays.asList("抄送"));
                    }else {//已读
                        paramMap.put("currenttaskstatuss",Arrays.asList("抄送已读"));
                    }
                }else if (showcurrentuser==1){//我处理、我审核、我完成
                    //待处理（statuslist=1,2），已处理（statuslist=3,4,5）
                    paramMap.put("hasauthor", "0");
                    if (statuslist.contains("1")){//待处理：查询流程最新状态
                        paramMap.put("currentstatus","1");
                    }
                    paramMap.put("currenttaskstatuss",Arrays.asList("待处理","转办"));

                }else if (showcurrentuser==2){
                    //待审核（statuslist=5），已审核（statuslist=2,3,4）
                    paramMap.put("hasauthor", "0");
                    paramMap.put("currenttaskstatuss",Arrays.asList("审核"));
                }else if (showcurrentuser==3){//我完成（statuslist=4）
                    paramMap.put("hasauthor", "0");
                }
            }
            if (StringUtils.isNotBlank(monitorpointid)||rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())){
                paramMap.putIfAbsent("hasauthor", "1");
            }else {
                paramMap.putIfAbsent("hasauthor", "0");
            }
            resultdata = devOpsTaskDisposeService.getAllDevOpsTaskDisposeListDataByParamMap(paramMap);
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
     * @Description:根据自定义参数获取各类型报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getDevOpsMonitorPointExceptionDataDataByParamMap", method = RequestMethod.POST)
    public Object getDevOpsMonitorPointExceptionDataDataByParamMap(
                                                                   @RequestJson(value = "dgimn") String dgimn,
                                                                   @RequestJson(value = "taskcreatetime", required = false) String taskcreatetime,
                                                                   @RequestJson(value = "taskendtime", required = false) String taskendtime,
                                                                   @RequestJson(value = "outputname", required = false) String outputname,
                                                                   @RequestJson(value = "pollutantname", required = false) String pollutantname,
                                                                   @RequestJson(value = "monitorpointtype", required = true) Integer monitorpointtype,
                                                                   @RequestJson(value = "exceptiontypes", required = false) List<String> exceptiontypes,
                                                                   @RequestJson(value = "datatype", required = false) List<String> datatypes,
                                                                   @RequestJson(value = "pagesize", required = true) Integer pagesize,
                                                                   @RequestJson(value = "pagenum", required = true) Integer pagenum
    ) throws Exception {
        try {
            //设置参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("outputname", outputname);
            paramMap.put("starttime", taskcreatetime);
            paramMap.put("endtime", taskendtime);
            paramMap.put("monitorpointtype", monitorpointtype.toString());
            paramMap.put("exceptiontypes", exceptiontypes);
            paramMap.put("pollutantname", pollutantname);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            if (datatypes == null) {
                datatypes = new ArrayList<>();
            }
            paramMap.put("datatypes", datatypes);
            Map<String, Object> result = devOpsTaskDisposeService.getDevOpsMonitorPointExceptionDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:分派运维任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addDevOpsTaskDisposeInfo", method = RequestMethod.POST)
    public Object addDevOpsTaskDisposeInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            formdata.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
            devOpsTaskDisposeService.saveDevOpsTaskInfo(userId, username, formdata);
            //消息推送
            List<String> pushuserids = new ArrayList<>();
            pushuserids = (List<String>) formdata.get("userids");
            List<String> cs_userids = formdata.get("cs_userids")!=null?(List<String>) formdata.get("cs_userids"):new ArrayList<>();
            if (cs_userids!=null&&cs_userids.size()>0){
                pushuserids.addAll(cs_userids);
            }
            //推送到首页
            if (pushuserids.size()>0) {
                alarmTaskDisposeController.pushAlarmTaskToPageHome(pushuserids);
            }
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //发送短信
            //sendTaskMessage(username,formdata);

            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void sendTaskMessage(String username, Map<String, Object> formdata) {
        List<String> userIds = (List<String>) formdata.get("userids");
        if (userIds.size() > 0) {
            String pollutionId = formdata.get("fk_pollutionid").toString();
            String taskId = formdata.get("pk_taskid").toString();
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = devOpsTaskDisposeService.selectByPrimaryKey(taskId);
            if (StringUtils.isNotBlank(alarmTaskDisposeManagementVO.getFkmonitorpointtypecode())){
                String monitorpointtypecode = alarmTaskDisposeManagementVO.getFkmonitorpointtypecode();
                String pollutionName = "";
                //获取所有关联企业的监测点类型
                List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
                List<String> monitortypes = pollutiontypes.stream().map(x -> x + "").collect(Collectors.toList());
                if (monitortypes.contains(monitorpointtypecode)) {//若为关联企业的监测点类型  取企业名称
                    PollutionVO pollutionVO = pollutionService.selectByPrimaryKey(pollutionId);
                    pollutionName = pollutionVO.getPollutionname();
                } else if (monitorpointtypecode.equals(String.valueOf(AirEnum.getCode()))) {//大气
                    AirMonitorStationVO airMonitorStation = airMonitorStationService.getAirMonitorStationByID(pollutionId);
                    pollutionName = airMonitorStation.getMonitorpointname();
                } else if (monitorpointtypecode.equals(String.valueOf(WaterQualityEnum.getCode()))) {//水质
                    WaterStationVO waterStation = waterStationService.getWaterStationByID(pollutionId);
                    pollutionName = waterStation.getMonitorpointname();
                } else if (monitorpointtypecode.equals(String.valueOf(EnvironmentalVocEnum.getCode())) || monitorpointtypecode.equals(String.valueOf(EnvironmentalStinkEnum.getCode())) || monitorpointtypecode.equals(String.valueOf(MicroStationEnum.getCode()))) {//voc//恶臭
                    OtherMonitorPointVO otherMonitorPoint = otherMonitorPointService.getOtherMonitorPointByID(pollutionId);
                    pollutionName = otherMonitorPoint.getMonitorpointname();
                }
                monitorpointtypecode =  CommonTypeEnum.MonitorPointTypeEnum.getNameByCode(Integer.parseInt(monitorpointtypecode));
                String monitorpointtype = monitorpointtypecode.replace("监测点类型","");
                String time = DataFormatUtil.getDateYMDHM(new Date());
                time = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm", "H点mm分");
                String context = "您收到来自" + username + time + "派发的" + pollutionName + "的"+monitorpointtype+"运维处置工单，请及时处理。";
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
                socketJson.put("tasktype", monitorpointtype+"运维处置工单");
                authSystemMicroService.sendTaskSMSToClient(socketJson);
            }
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
    @RequestMapping(value = "addDevOpsTaskFeedbackInfo", method = RequestMethod.POST)
    public Object addDevOpsTaskFeedbackInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");

            List<String> cs_userids = formdata.get("sc_userids")!=null?(List<String>) formdata.get("sc_userids"):new ArrayList<>();

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = devOpsTaskDisposeService.selectByPrimaryKey(formdata.get("pk_taskid").toString());
            alarmTaskDisposeManagement.setFeedbackresults(formdata.get("feedbackresults") != null ? formdata.get("feedbackresults").toString() : "");
            alarmTaskDisposeManagement.setFileid(formdata.get("fileid") != null ? formdata.get("fileid").toString() : "");
            alarmTaskDisposeManagement.setFkProblemtype(formdata.get("problemtype") != null ? formdata.get("problemtype").toString() : "");
            if (formdata.get("taskrealenddate") != null) {
                alarmTaskDisposeManagement.setTaskrealenddate(formdata.get("taskrealenddate").toString());
            }
            alarmTaskDisposeManagement.setUpdatetime(new Date());
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setTaskrealenddate(format.format(new Date()));

            if (cs_userids != null && cs_userids.size()>0) {//有审查人时  需要审查人确认无误 才能办结任务
                alarmTaskDisposeManagement.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.ConfirmEndEnum.getCode());
                alarmTaskDisposeService.updateTaskCarbonCopyStatus(userId, alarmTaskDisposeManagement);
            }else{//无抄送人  直接完成任务
                devOpsTaskDisposeService.updateDevOpsTaskInfo(userId, alarmTaskDisposeManagement);
            }

            List<TaskFlowRecordInfoVO> objs =new ArrayList<>();
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
            if (objs!=null&&objs.size()>0) {
                alarmTaskDisposeService.insertReviewedInfo(objs);
            }
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(alarmTaskDisposeManagement.getPkTaskid());
            alarmTaskDisposeController.pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 下午3:08
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "addDevOpsTaskStatusToHandle", method = RequestMethod.POST)
    public Object addDevOpsTaskStatusToHandle(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            devOpsTaskDisposeService.addDevOpsTaskStatusToHandle(id, userId, username);
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(id);
            alarmTaskDisposeController.pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/25 0025 上午 11:31
     * @Description:忽略运维任务信息
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:无忽略状态  直接完成任务
     * @param: []
     */
    @RequestMapping(value = "addDevOpsTaskNeglectInfo", method = RequestMethod.POST)
    public Object addDevOpsTaskNeglectInfo( HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String problemtype = formdata.get("problemtype") != null ? formdata.get("problemtype").toString() : "";
            String fileid = formdata.get("fileid") != null ? formdata.get("fileid").toString() : "";
            String pk_taskid = formdata.get("pk_taskid") != null ? formdata.get("pk_taskid").toString() : "";
            String taskcomment = formdata.get("feedbackresults") != null ? formdata.get("feedbackresults").toString() : "";
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(pk_taskid);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            alarmTaskDisposeManagement.setUpdatetime(new Date());
            alarmTaskDisposeManagement.setUpdateuser(username);
            alarmTaskDisposeManagement.setFkProblemtype(problemtype);
            alarmTaskDisposeManagement.setFileid(fileid);
            alarmTaskDisposeManagement.setFeedbackresults(taskcomment);
            devOpsTaskDisposeService.updateDevOpsTaskInfo(userId, alarmTaskDisposeManagement);
            //忽略任务信息
            //devOpsTaskDisposeService.neglectTaskInfo(alarmTaskDisposeManagement, userId);
            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(alarmTaskDisposeManagement.getPkTaskid());
            alarmTaskDisposeController.pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/12 0012 上午 11:55
     * @Description:导出-运维任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportDevOpsTaskDisposeManagement", method = RequestMethod.POST)
    public void exportDevOpsTaskDisposeManagement(@RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                  @RequestJson(value = "starttime", required = false) String starttime,
                                                  @RequestJson(value = "monitorpointtypes", required = false) List<String> monitorpointtypes,
                                                  @RequestJson(value = "recoverystatuslist", required = false) List<String> recoverystatuslist,
                                                  @RequestJson(value = "endtime", required = false) String endtime,
                                                  @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                  @RequestJson(value = "fktasktype", required = false) String fktasktype,
                                                  @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                  @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                  HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //获取表头数据
            List<Map<String, Object>> tabletitledata = devOpsTaskDisposeService.getTableTitleForDevOpsTask();
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            //paramMap.put("datauserid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("starttime", starttime);
            paramMap.put("fktasktype", fktasktype);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("recoverystatuslist", recoverystatuslist);
            paramMap.put("statuslist", statuslist);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("sign", "pc");//自定义标记
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
            /*if (flag == false) {//无分派按钮权限  值显示和自己相关的报警任务
               List<String> statuslist = new ArrayList<>();
                statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode().toString());
               statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode().toString());
               statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString());
               statuslist.add(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode().toString());
               paramMap.put("statuslist", statuslist);
                paramMap.put("hasauthor", "0");
            }else{
                paramMap.put("hasauthor", "1");
            }*/
            List<String> rightList = getRightList(userId);
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())){
                paramMap.put("hasauthor", "1");
            }else {
                paramMap.put("hasauthor", "");
            }
           /* List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            paramMap.put("categorys", categorys);*/
            datas = devOpsTaskDisposeService.getAllDevOpsTaskDisposeListDataByParamMap(paramMap);
            listdata = (List<Map<String, Object>>) datas.get("datalist");
            Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("TaskStatus").toString());
            List<Map<String, Object>> collect = listdata.stream().sorted(comparebytime).collect(Collectors.toList());
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "运维任务处置管理导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/19 0019 下午 1:52
     * @Description:获取拥有处置按钮权限的处置人下拉列表框数据并展示该企业的默认运维人员
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getDisposePersonSelectData", method = RequestMethod.POST)
    public Object getDisposePersonSelectData(@RequestJson(value = "id") String id,
                                             @RequestJson(value = "monitorpointtype") String monitorpointtype,
                                             @RequestJson(value = "menusysmodel", required = false) String menusysmodel) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            if (menusysmodel != null) {
                paramMap.put("sysmodel", menusysmodel);
            } else {
                paramMap.put("sysmodel", sysmodel);
            }
            paramMap.put("buttoncode", buttoncode);
            List<Map<String, Object>> listdata = devOpsTaskDisposeService.getDisposePersonSelectData(paramMap);
            paramMap.clear();
            paramMap.put("id", id);
            paramMap.put("monitorpointtype", monitorpointtype);
            EntDevOpsInfoVO obj = devOpsTaskDisposeService.getEntDevOpsInfoVOByParam(paramMap);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("User_Name"));
                    objmap.put("value", map.get("User_ID"));
                    result.add(objmap);
                }
            }
            resultMap.put("datalist", result);
            resultMap.put("userids", obj != null ? obj.getDevopspeople() : "");
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/23 0023 下午 3:30
     * @Description:根据自定义参数获取待分派运维任务信息(app)
     * @updateUser:xsm
     * @updateDate:2021/12/15
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getDevOpsTaskListDataByParamMapForApp", method = RequestMethod.POST)
    public Object getDevOpsTaskListDataByParamMapForApp(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                        @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                        @RequestJson(value = "starttime", required = false) String starttime,
                                                        @RequestJson(value = "endtime", required = false) String endtime,
                                                        @RequestJson(value = "currentstatus", required = false) String currentstatus,
                                                        @RequestJson(value = "statuslist") List<Integer> statuslist,
                                                        HttpServletRequest request, HttpSession session) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //按钮数据
            List<String> statuss = new ArrayList<>();
            if (statuslist != null && statuslist.size() > 0) {
                for (Integer i : statuslist) {
                    statuss.add(i + "");
                }
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPDevOpsTaskDisposeEnum.getMenucode());
            paramMap.put("sign", "app");
            if (starttime!=null&&endtime!=null) {
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
            }
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> datas = new HashMap<>();
            List<String> rightList = getRightList(userId);
            if (statuss.get(0).equals(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode() + "")||
                    statuss.get(0).equals(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode() + "")){
                if ( rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())){
                    paramMap.put("hasauthor", "1");
                }else{
                    paramMap.put("hasauthor", "0");
                }
            }else{
                paramMap.put("hasauthor", "0");
            }
            paramMap.put("tasktype", DevOpsTaskEnum.getCode());
            paramMap.put("statuslist", statuss);
            paramMap.put("currentstatus", currentstatus);//isconfirmend 不为空 且为1 则查需要自己审核的
            datas = devOpsTaskDisposeService.getAllDevOpsTaskDisposeListDataByParamMap(paramMap);
            //处理分页数据
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/24 0024 上午9:45
     * @Description: 根据运维任务ID获取详情信息-app
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getDevOpsTaskDetailByID", method = RequestMethod.POST)
    public Object getDevOpsTaskDetailByID(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            result = devOpsTaskDisposeService.getDevOpsTaskDetailByID(id, userId, username);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/25 0025 上午 11:40
     * @Description: 获取处置运维任务人员选择数据（部门下）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "getDevOpsTaskDisposeUserData", method = RequestMethod.POST)
    public Object getDevOpsTaskDisposeUserData(@RequestJson(value = "tasktype") Integer tasktype,
                                               @RequestJson(value = "id", required = false) String id,
                                               @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> organizationUser = alarmTaskDisposeService.getAllOrganizationUser(param);
            if (organizationUser.size() > 0) {
                Set<String> userids = new HashSet<>();
                //获取拥有处置权限的用户ID
                Map<String, Object> paramMap = new HashMap<>();
                if (tasktype == CommonTypeEnum.TaskTypeEnum.ComplaintEnum.getCode()) {
                    paramMap.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPComplaintTaskDisposeEnum.getMenucode());
                } else if (tasktype == CommonTypeEnum.TaskTypeEnum.DailyEnum.getCode()) {
                    paramMap.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPDailyTaskDisposeEnum.getMenucode());
                } else if (tasktype == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                    paramMap.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPDevOpsTaskDisposeEnum.getMenucode());
                }
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
                    }
                    if (organizationIdAndUser.containsKey(organizationId) == false) {
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
            if (tasktype == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {//运维任务时  回显企业运维人员
                param.clear();
                param.put("id", id);
                param.put("monitorpointtype", monitorpointtype);
                EntDevOpsInfoVO obj = devOpsTaskDisposeService.getEntDevOpsInfoVOByParam(param);
                resultMap.put("datalist", resultList);
                resultMap.put("userids", obj != null ? obj.getDevopspeople() : "");
                return AuthUtil.parseJsonKeyToLower("success", resultMap);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", resultList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/1/10 0010 上午 11:36
     * @Description: 统计运维任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countDevOpsTaskCompletionStatusByParamMap", method = RequestMethod.POST)
    public Object countDevOpsTaskCompletionStatusByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                            @RequestJson(value = "endtime", required = false) String endtime,
                                                            @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype,
                                                            HttpSession session
    ) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("userid", userId);
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> types = new ArrayList<>();
            if (categorys.contains("1")) {
                types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()}));
            }
            if(categorys.contains("2")){
                types.addAll(Arrays.asList(new Integer[]{SecurityDevOpsTaskEnum.getCode()}));
            }
            paramMap.put("tasktypes", types);
            //已完成  状态
            paramMap.put("completions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
            paramMap.put("uncompletions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode(), CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode()));
            List<Map<String, Object>> resultlist = devOpsTaskDisposeService.countDevOpsTaskCompletionStatusByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/04/22 0022 上午 10:50
     * @Description: 统计某个时间范围内运维任务已完成和未完成任务占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countDevOpsTaskCompletionStatusByMonitorTimes", method = RequestMethod.POST)
    public Object countDevOpsTaskCompletionStatusByMonitorTimes(@RequestJson(value = "starttime", required = false) String starttime,
                                                                @RequestJson(value = "endtime", required = false) String endtime,
                                                                HttpSession session
    ) throws Exception {
        try {
            String sessionid = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            List<Integer> types = new ArrayList<>();
            if (categorys.contains("1")) {
                types.addAll(Arrays.asList(new Integer[]{CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()}));
            }
            if (categorys.contains("2")) {
                types.addAll(Arrays.asList(new Integer[]{SecurityDevOpsTaskEnum.getCode()}));
            }
            paramMap.put("tasktypes", types);
            //已完成  状态
            paramMap.put("completions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
            paramMap.put("uncompletions", Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode(),
                    CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode(), CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode()));
            List<Map<String, Object>> resultlist = devOpsTaskDisposeService.getDevOpsTaskRemindDataByMonitorTimes(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/20 0020 下午 14:19
     * @Description:根据时间范围获取该时间段内运维任务各个任务状态的统计情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getDevOpsTaskRemindDataByMonitorTimes", method = RequestMethod.POST)
    public Object getDevOpsTaskRemindDataByMonitorTimes(@RequestJson(value = "starttime", required = false) String starttime,
                                                        @RequestJson(value = "endtime", required = false) String endtime,
                                                        @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype, HttpSession session) {
        try {
            String sessionid = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("tasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("userid", userId);
            List<Map<String, Object>> listdata = alarmTaskDisposeService.countDevOpsTaskNumGroupByStatusByParamMap(paramMap);
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
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/02/21 0021 上午 9:37
     * @Description:根据自定义参数获取运维任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getDevOpsTaskDataByParamMap", method = RequestMethod.POST)
    public Object getDevOpsTaskDataByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                              @RequestJson(value = "endtime", required = false) String endtime,
                                              @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                              @RequestJson(value = "monitorpointtype", required = false) String monitorpointtype, HttpSession session) {
        try {
            String sessionid = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("userid", userId);
            if (statuslist != null && statuslist.size() > 0) {
                paramMap.put("statuslist", statuslist);
            }
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listdata = alarmTaskDisposeService.getDevOpsTaskInfosGroupByMonitorPointByParamMap(paramMap);
            Set set = new HashSet();
            List<Map<String, Object>> result = new ArrayList<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    if (set.contains(map.get("PollutionName"))) {//判断点位或企业名称是否重复
                        continue;//重复
                    } else {//不重复
                        set.add(map.get("PollutionName"));
                        Map<String, Object> resultmap = new HashMap<>();
                        resultmap.put("pollutionid", map.get("FK_Pollutionid"));
                        resultmap.put("pollutionname", map.get("PollutionName"));
                        resultmap.put("longitude", map.get("Longitude"));
                        resultmap.put("latitude", map.get("Latitude"));
                        List<Map<String, Object>> typemaplist = new ArrayList<>();
                        String status = "";
                        for (Map<String, Object> obj : listdata) {
                            if ((map.get("PollutionName").toString()).equals(obj.get("PollutionName").toString())) {
                                Map<String, Object> typemap = new HashMap<>();
                                typemap.put("monitorpointtypename", obj.get("monitorpointtypename"));
                                typemap.put("num", obj.get("num"));
                                typemap.put("status", obj.get("TaskStatus"));
                                typemap.put("monitorpointid", obj.get("outputid"));
                                typemap.put("monitorpointtypetype", obj.get("FK_MonitorPointTypeCode"));
                                typemap.put("taskstatusname", "");
                                if (obj.get("TaskStatus") != null && !"".equals(obj.get("TaskStatus").toString())) {
                                    if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString()).equals(Integer.parseInt(obj.get("TaskStatus").toString()))) {
                                        typemap.put("taskstatusname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.getNameByCode(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
                                    } else {
                                        typemap.put("taskstatusname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.getNameByCode(Integer.parseInt(obj.get("TaskStatus").toString())));
                                    }
                                    if (!"".equals(status)) {
                                        if (Integer.parseInt(status) > Integer.parseInt(obj.get("TaskStatus").toString())) {
                                            status = obj.get("TaskStatus").toString();
                                        }
                                    } else {
                                        status = obj.get("TaskStatus").toString();
                                    }
                                }
                                typemaplist.add(typemap);
                            }
                        }
                        resultmap.put("status", status);
                        if ((CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode().toString()).equals(status)) {
                            resultmap.put("taskstatusname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.getNameByCode(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode()));
                        } else {
                            resultmap.put("taskstatusname", CommonTypeEnum.AlarmTaskDisposalScheduleEnum.getNameByCode(Integer.parseInt(status)));
                        }
                        resultmap.put("typedata", typemaplist);
                        result.add(resultmap);
                    }
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
     * @date: 2020/02/21 0021 上午 10:52
     * @Description:根据自定义参数统计按监测类型分组的运维任务条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countDevOpsTaskDataNumGroupByMonitorType", method = RequestMethod.POST)
    public Object countDevOpsTaskDataNumGroupByMonitorType(@RequestJson(value = "starttime", required = false) String starttime,
                                                           @RequestJson(value = "endtime", required = false) String endtime,
                                                           HttpSession session) {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            List<Map<String, Object>> monitorpointtypes = alarmTaskDisposeService.getUserMonitorPointRelationDataByUserId(paramMap);
            List<Map<String, Object>> result = new ArrayList<>();
            Map<Integer, Object> typeandname = new HashMap<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                Set<Integer> types = new HashSet();
                for (Map<String, Object> map : monitorpointtypes) {
                    if (map.get("FK_MonitorPointType") != null) {
                        types.add(Integer.parseInt(map.get("FK_MonitorPointType").toString()));
                        typeandname.put(Integer.parseInt(map.get("FK_MonitorPointType").toString()),map.get("monitorpointtypename"));
                    }
                }
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                List<Map<String, Object>> listdata = alarmTaskDisposeService.countDevOpsTaskDataNumGroupByMonitorTypeByParamMap(paramMap);
                for (Integer code : types) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("monitorpointtypecode", code);
                    map.put("monitorpointtypename", typeandname.get(code)!=null?typeandname.get(code)+"运维":"");
                    map.put("num", 0);
                    if (listdata != null && listdata.size() > 0) {
                        for (Map<String, Object> objmap : listdata) {
                            if (objmap.get("monitorpointtype") != null && (objmap.get("monitorpointtype").toString()).equals(code.toString())) {
                                map.put("num", objmap.get("num"));
                            }
                        }
                    }
                    result.add(map);
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
     * @date: 2020/02/26 0026 上午 11:03
     * @Description:根据自定义参数获取点位运维任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointDevOpsTaskDataByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointDevOpsTaskDataByParamMap(@RequestJson(value = "monitorpointid") String monitorpointid,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime,
                                                          @RequestJson(value = "menusysmodel") String menusysmodel,
                                                          HttpServletRequest request, HttpSession session) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("sysmodel", menusysmodel);
            Map<String, Object> result = devOpsTaskDisposeService.getMonitorPointDevOpsTaskDataByParamMap(paramMap);
            if (result != null) {
                List<String> rightList = getRightList(userId);
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())){
                    result.put("assignflag", true);
                }else {
                    result.put("assignflag", false);
                }


            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
