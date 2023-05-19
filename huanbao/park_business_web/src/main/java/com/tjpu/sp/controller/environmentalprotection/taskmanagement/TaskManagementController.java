package com.tjpu.sp.controller.environmentalprotection.taskmanagement;

import com.github.pagehelper.PageHelper;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementVO;
import com.tjpu.sp.model.extand.TextMessageVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.user.UserService;
import com.tjpu.sp.service.envhousekeepers.checkproblemexpound.CheckProblemExpoundService;
import com.tjpu.sp.service.environmentalprotection.devopsinfo.EntDevOpsInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.MonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.DevOpsTaskDisposeService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.DaliyTaskEnum.UnassignedTaskEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.DaliyTaskEnum.UndisposedEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.TaskTypeEnum.*;


/**
 * @author: chengzq
 * @date: 2019/8/17 0009 14:18
 * @Description: 任务分配管理控制类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("taskdisposemanagement")
public class TaskManagementController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;
    @Autowired
    private DevOpsTaskDisposeService devOpsTaskDisposeService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private AuthSystemMicroService authSystemMicroService;

    @Autowired
    private CheckProblemExpoundService checkProblemExpoundService;
    @Autowired
    private EntDevOpsInfoService entDevOpsInfoService;
    @Autowired
    private MonitorPointService monitorPointService;
    @Autowired
    private AlarmTaskDisposeController alarmTaskDisposeController;
    @Autowired
    private UserService userService;
    //分派任务

    private String sysmodel = "TaskDisposeManagement";

    private String listfieldtype = "list-daily";
    /**
     * 数据中心数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;
    /**
     * 存放token的key
     */


    /**
     * @author: chengzq
     * @date: 2019/8/17 0009 下午 2:39
     * @Description: 获取任务分配管理初始化列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "/getTaskManagementListPage", method = RequestMethod.POST)
    public Object getTaskManagementListPage(HttpServletRequest request) {
        try {

            int total = 0;
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            // 获取token
            String param = AuthUtil.paramDataFormat(paramMap);
            Object userButtonAuthInMenu = publicSystemMicroService.getUserButtonAuthInMenu(param);
            JSONObject jsonObject = JSONObject.fromObject(userButtonAuthInMenu);
            Object data = jsonObject.get("data");
            Object statustype = paramMap.get("statustype");
            List<String> rightList = getRightList(userId);
            boolean isRight = false;
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.RCFPEnum.getCode())) {
                isRight = true;
            }
            Integer showcurrentuser = paramMap.get("showcurrentuser") != null ? Integer.parseInt(paramMap.get("showcurrentuser").toString())
                    : null;
            Integer csstatus = paramMap.get("csstatus") != null ? Integer.parseInt(paramMap.get("csstatus").toString())
                    : null;
            List<String> statuslist = paramMap.get("statuslist") != null ? (List<String>) paramMap.get("statuslist") : new ArrayList<>();
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
                    paramMap.put("isown", "1");
                    if (csstatus == 0) {//未读
                        paramMap.put("currenttaskstatuss", Arrays.asList("抄送"));
                    } else {//已读
                        paramMap.put("currenttaskstatuss", Arrays.asList("抄送已读"));
                    }
                } else if (showcurrentuser == 1) {//我处理、我审核、我完成
                    //待处理（statuslist=1,2），已处理（statuslist=3,4,5）
                    paramMap.put("hasauthor", "0");
                    if (statuslist.contains("1")){//待处理：查询流程最新状态
                        paramMap.put("currentstatus","1");
                    }
                    paramMap.put("currenttaskstatuss", Arrays.asList("待处理","转办"));

                } else if (showcurrentuser == 2) {
                    //待审核（statuslist=5），已审核（statuslist=2,3,4）
                    paramMap.put("hasauthor", "0");
                    paramMap.put("currenttaskstatuss", Arrays.asList("审核"));
                } else if (showcurrentuser == 3) {//我完成（statuslist=4）
                    paramMap.put("hasauthor", "0");
                }
            }
            if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                paramMap.putIfAbsent("hasauthor", "1");
            } else {
                paramMap.putIfAbsent("hasauthor", "0");
            }
            paramMap.put("statuslist", statuslist);
            paramMap.put("tasktype", DailyEnum.getCode());
            List<Map<String, Object>> daliyTaskByParamMap = alarmTaskDisposeService.getDaliyTaskByParamMap(paramMap);
            //设置执行人格式及权限
            Iterator<Map<String, Object>> iterator = daliyTaskByParamMap.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> m = iterator.next();
                String zhixingrens = ((HashSet<String>) (m.get("zhixingren"))).stream().filter(n -> StringUtils.isNotBlank(n)).collect(Collectors.joining("、"));
                m.put("zhixingren", zhixingrens);
                if (zhixingrens.contains(username)) {
                    m.put("isfeedback", 0);
                } else {
                    m.put("isfeedback", 1);
                    if ("needfeedback".equals(statustype) || "feedbacking".equals(statustype)) {
                        iterator.remove();
                    }
                }
            }


            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                Integer pagenum = Integer.valueOf(paramMap.get("pagenum").toString());
                Integer pagesize = Integer.valueOf(paramMap.get("pagesize").toString());
                if (statustype != null && ((statustype.toString()).equals(CommonTypeEnum.TaskStatusTypeEnum.needfeedbackEnum.getCode()) || (statustype.toString()).equals(CommonTypeEnum.TaskStatusTypeEnum.feedbackingEnum.getCode()))) {
                    total = daliyTaskByParamMap.stream().filter(m -> m.get("zhixingren") != null && m.get("zhixingren").toString().contains(username)).collect(Collectors.toList()).size();
                    daliyTaskByParamMap = daliyTaskByParamMap.stream().filter(m -> m.get("zhixingren") != null && m.get("zhixingren").toString().contains(username)).sorted(Comparator.comparing(m -> ((Map) m).get("taskstatus") == null ? "" : ((Map) m).get("taskstatus").toString()).reversed().thenComparing(m -> ((Map) m).get("fenpaidate") == null ? "" : ((Map) m).get("fenpaidate").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                } else {
                    total = daliyTaskByParamMap.size();
                    daliyTaskByParamMap = daliyTaskByParamMap.stream().sorted(Comparator.comparing(m -> ((Map) m).get("taskstatus") == null ? "" : ((Map) m).get("taskstatus").toString()).reversed().thenComparing(m -> ((Map) m).get("fenpaidate") == null ? "" : ((Map) m).get("fenpaidate").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                }

            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", total);
            resultMap.put("buttondata", data);
            resultMap.put("tabledata", daliyTaskByParamMap);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/17 0009 下午 3:45
     * @Description: 通过自定义参数获取任务分配管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @RequestMapping(value = "/getTaskManagementByParamMap", method = RequestMethod.POST)
    public Object getTaskManagementByParamMap(@RequestJson(value = "paramsjson", required = false) Object map) throws ParseException {
        try {
            String starttime = null;
            String endtime = null;
            int total = 0;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);

            JSONObject paramMap = new JSONObject();
            Map<String, Object> resultMap = new HashMap<>();
            if (map != null) {
                paramMap = JSONObject.fromObject(map);
            }
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("userid", userId);
            paramMap.put("listfieldtype", listfieldtype);
            paramMap.put("datasource", datasource);
            if (paramMap.get("sign") != null && "app".equals(paramMap.get("sign").toString())) {
                paramMap.put("tasktype", DailyEnum.getCode());
                List<Map<String, Object>> daliyTaskByParamMap = new ArrayList<>();
                paramMap.remove("userid");
                if (paramMap.get("statustype") != null && !"needassign".equals(paramMap.get("statustype").toString()) && !"hasclose".equals(paramMap.get("statustype").toString())) {
                    paramMap.put("userid", userId);
                    paramMap.put("isown", 1);
                }
                paramMap.put("sysmodel", CommonTypeEnum.SocketTypeEnum.APPDailyTaskDisposeEnum.getMenucode());
                daliyTaskByParamMap = alarmTaskDisposeService.getAPPDaliyTaskByParamMap(paramMap);

                List<Map<String, Object>> collect = daliyTaskByParamMap.stream().filter(m -> m.get("zhixingren") != null).peek(m -> {
                    String zhixingrens = ((HashSet<String>) (m.get("zhixingren"))).stream().filter(n -> StringUtils.isNotBlank(n)).collect(Collectors.joining("、"));
                    if (zhixingrens.contains(username)) {
                        m.put("isfeedback", 0);
                    } else {
                        m.put("isfeedback", 1);
                    }
                    m.put("zhixingren", zhixingrens);
                }).collect(Collectors.toList());
                resultMap.put("total", collect.size());
                resultMap.put("datalist", collect);
            } else {

                List<String> rightList = getRightList(userId);
                boolean isRight = false;
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.RCFPEnum.getCode())) {
                    isRight = true;
                }
                Integer showcurrentuser = paramMap.get("showcurrentuser") != null ? Integer.parseInt(paramMap.get("showcurrentuser").toString())
                        : null;
                Integer csstatus = paramMap.get("csstatus") != null ? Integer.parseInt(paramMap.get("csstatus").toString())
                        : null;
                List<String> statuslist = paramMap.get("statuslist") != null ? (List<String>) paramMap.get("statuslist") : new ArrayList<>();
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
                        paramMap.put("isown", "1");
                        if (csstatus == 0) {//未读
                            paramMap.put("currenttaskstatuss", Arrays.asList("抄送"));
                        } else {//已读
                            paramMap.put("currenttaskstatuss", Arrays.asList("抄送已读"));
                        }
                    } else if (showcurrentuser == 1) {//我处理、我审核、我完成
                        //待处理（statuslist=1,2），已处理（statuslist=3,4,5）
                        paramMap.put("hasauthor", "0");
                        if (statuslist.contains("1")){//待处理：查询流程最新状态
                            paramMap.put("currentstatus","1");
                        }
                        paramMap.put("currenttaskstatuss", Arrays.asList("待处理","转办"));

                    } else if (showcurrentuser == 2) {
                        //待审核（statuslist=5），已审核（statuslist=2,3,4）
                        paramMap.put("hasauthor", "0");
                        paramMap.put("currenttaskstatuss", Arrays.asList("审核"));
                    } else if (showcurrentuser == 3) {//我完成（statuslist=4）
                        paramMap.put("hasauthor", "0");
                    }
                }
                if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                    paramMap.putIfAbsent("hasauthor", "1");
                } else {
                    paramMap.putIfAbsent("hasauthor", "0");
                }
                paramMap.put("statuslist", statuslist);
                paramMap.put("tasktype", DailyEnum.getCode());
                List<Map<String, Object>> daliyTaskByParamMap = alarmTaskDisposeService.getDaliyTaskByParamMap(paramMap);
                if (paramMap.get("starttime") != null && !"".equals(paramMap.get("starttime").toString())) {
                    starttime = paramMap.get("starttime").toString() + " 00:00:00";
                    long time = format.parse(starttime).getTime();
                    daliyTaskByParamMap = daliyTaskByParamMap.stream().filter(m -> {
                        try {
                            return m.get("fenpaidate") != null && format.parse(m.get("fenpaidate").toString()).getTime() >= time;
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).collect(Collectors.toList());


                }
                if (paramMap.get("endtime") != null && !"".equals(paramMap.get("endtime").toString())) {
                    endtime = paramMap.get("endtime").toString() + " 23:59:59";
                    long time = format.parse(endtime).getTime();
                    daliyTaskByParamMap = daliyTaskByParamMap.stream().filter(m -> {
                        try {
                            return m.get("fenpaidate") != null && format.parse(m.get("fenpaidate").toString()).getTime() <= time;
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).collect(Collectors.toList());

                }
                List<Map<String, Object>> collect = daliyTaskByParamMap.stream().filter(m -> m.get("zhixingren") != null).peek(m -> {
                    HashSet<String> zhixingrenSet = (HashSet<String>) (m.get("zhixingren"));
                    String zhixingrens = zhixingrenSet.stream().filter(n -> StringUtils.isNotBlank(n)).collect(Collectors.joining("、"));
                    if (zhixingrenSet.contains(username)) {
                        m.put("isfeedback", 0);
                    } else {
                        m.put("isfeedback", 1);
                    }
                    m.put("zhixingren", zhixingrens);
                }).collect(Collectors.toList());


                if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                    Integer pagenum = Integer.valueOf(paramMap.get("pagenum").toString());
                    Integer pagesize = Integer.valueOf(paramMap.get("pagesize").toString());
                    PageHelper.startPage(Integer.valueOf(paramMap.get("pagenum").toString()), Integer.valueOf(paramMap.get("pagesize").toString()));
                    total = collect.size();
                    collect = collect.stream().sorted(Comparator.comparing(m -> ((Map) m).get("taskstatus") == null ? "" : ((Map) m).get("taskstatus").toString()).reversed().thenComparing(m -> ((Map) m).get("fenpaidate") == null ? "" : ((Map) m).get("fenpaidate").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                }
                resultMap.put("total", total);
                resultMap.put("datalist", collect);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/17 0009 下午 3:51
     * @Description: 新增任务分配管理
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "addTaskManagement", method = RequestMethod.POST)
    public Object addTaskManagement(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            String id = session.getId();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Object fromdata = paramMap.get("fromdata");
            JSONObject jsonObject = JSONObject.fromObject(fromdata);
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new AlarmTaskDisposeManagementVO());
            alarmTaskDisposeManagementVO.setPkTaskid(UUID.randomUUID().toString());
            alarmTaskDisposeManagementVO.setTaskstatus(UnassignedTaskEnum.getCode());
            alarmTaskDisposeManagementVO.setTaskcreatetime(format.format(new Date()));
            alarmTaskDisposeManagementVO.setUpdatetime(new Date());
            alarmTaskDisposeManagementVO.setUpdateuser(userid);
            alarmTaskDisposeManagementVO.setFkTasktype(DailyEnum.getCode().toString());
            alarmTaskDisposeService.insert(alarmTaskDisposeManagementVO);
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/17 0009 下午 3:54
     * @Description: 修改任务分配管理
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [request]
     * @throws:
     */
    @RequestMapping(value = "updateTaskManagement", method = RequestMethod.POST)
    public Object updateTaskManagement(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            String id = session.getId();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Object fromdata = paramMap.get("fromdata");
            JSONObject jsonObject = JSONObject.fromObject(fromdata);
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagementVO = JSONObjectUtil.JsonObjectToEntity(jsonObject, new AlarmTaskDisposeManagementVO());
            alarmTaskDisposeManagementVO.setFkTasktype(DailyEnum.getCode().toString());
            alarmTaskDisposeManagementVO.setUpdatetime(new Date());
            alarmTaskDisposeManagementVO.setUpdateuser(userid);
            alarmTaskDisposeService.updateByPrimaryKey(alarmTaskDisposeManagementVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/8/17 0009 下午 3:57
     * @Description: 通过任务分配管理id删除任务分配管理
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteTaskManagementByID", method = RequestMethod.POST)
    public Object deleteTaskManagementByID(@RequestJson(value = "id", required = true) String id) throws Exception {
        try {
            int i = alarmTaskDisposeService.deleteByPrimaryKey(id);
            String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            rabbitmqController.sendTaskDirectQueue(messagetype);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/22 0022 下午 1:51
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pk_taskid, fileid, feedbackresults, problemtype, request, session]
     * @throws:
     */
    @RequestMapping(value = "getAlarmTaskTemporaryById", method = RequestMethod.POST)
    public Object getAlarmTaskTemporaryById(@RequestJson(value = "id") String id) throws Exception {
        try {
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(id);
            return AuthUtil.parseJsonKeyToLower("success", alarmTaskDisposeManagement);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/22 0022 上午 8:50
     * @Description: 通过主键id获取日常任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getTaskManagementDetailById", method = RequestMethod.POST)
    public Object getTaskManagementDetailById(@RequestJson(value = "id") String id) throws ParseException {
        try {
            String user = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tasktype", DailyEnum.getCode());
            paramMap.put("taskid", id);
            //详情逻辑
            Map<String, Object> daliyTaskDetailByParamMap = alarmTaskDisposeService.getDaliyTaskDetailByParamMap(paramMap);

            //查询处理人id
            String flowRecordInfoByTaskID = alarmTaskDisposeService.getFlowRecordInfoByTaskID(id);


            if (daliyTaskDetailByParamMap.get("zhixingren") != null) {
                Object zhixingren = ((Set<String>) daliyTaskDetailByParamMap.get("zhixingren")).stream().filter(m -> StringUtils.isNotBlank(m)).collect(Collectors.joining("、"));
                daliyTaskDetailByParamMap.put("zhixingren", zhixingren);
            } else {
                daliyTaskDetailByParamMap.put("zhixingren", "");
            }
            if (daliyTaskDetailByParamMap.get("problem") != null) {
                Object problem = ((Set) daliyTaskDetailByParamMap.get("problem")).stream().collect(Collectors.joining("、"));
                daliyTaskDetailByParamMap.put("problem", problem);
                if (daliyTaskDetailByParamMap.get("status") != null && "处理中".equals(daliyTaskDetailByParamMap.get("status").toString()) && !flowRecordInfoByTaskID.equals(user)) {
                    daliyTaskDetailByParamMap.put("problem", "");
                }
            } else {
                daliyTaskDetailByParamMap.put("problem", "");
            }
            if (daliyTaskDetailByParamMap.get("feedbackresults") != null) {
                daliyTaskDetailByParamMap.put("feedbackresults", daliyTaskDetailByParamMap.get("feedbackresults"));
                if (daliyTaskDetailByParamMap.get("status") != null && "处理中".equals(daliyTaskDetailByParamMap.get("status").toString()) && !flowRecordInfoByTaskID.equals(user)) {
                    daliyTaskDetailByParamMap.put("feedbackresults", "");
                }
            } else {
                daliyTaskDetailByParamMap.put("feedbackresults", "");
            }

            //流程逻辑
            Map<String, Object> daliyTaskRecordInfoByParamMap = alarmTaskDisposeService.getDaliyTaskRecordInfoByParamMap(paramMap);

            if (daliyTaskRecordInfoByParamMap.get("taskflow") != null) {
                List<Map<String, Object>> list = (List) daliyTaskRecordInfoByParamMap.get("taskflow");
                LinkedList<Map<String, Object>> linkedList = new LinkedList<>(list);
                Map<String, Object> firstMap = new HashMap<>();
                firstMap.put("taskhandletime", daliyTaskDetailByParamMap.get("taskcreatetime"));
                firstMap.put("taskstatus", "生成任务");
                firstMap.put("username", "");

                linkedList.addFirst(firstMap);
                int size = linkedList.size();
                Map<String, Object> undisposed = new HashMap<>();
                linkedList.stream().forEach(m -> {
                    if (m.get("taskhandletime") == null) {
                        m.put("taskhandletime", m.get("taskhandletimestr"));
                    }
                    m.remove("taskhandletimestr");
                    undisposed.put("taskhandletime", m.get("taskhandletime"));
                });
                String username = linkedList.stream().filter(m -> m.get("taskstatus") != null && m.get("username") != null && UndisposedEnum.getName().equals(m.get("taskstatus").toString())).peek(m -> {
                    undisposed.put("taskstatus", m.get("taskstatus"));
                }).map(m -> m.get("username").toString()).distinct().collect(Collectors.joining("、"));
                undisposed.put("username", username);
                Iterator<Map<String, Object>> iterator = linkedList.iterator();
                while (iterator.hasNext()) {
                    Map<String, Object> m = iterator.next();
                    if (m.get("taskstatus") != null && m.get("username") != null && UndisposedEnum.getName().equals(m.get("taskstatus").toString())) {
                        iterator.remove();
                    }
                }
                if (size > 2) {
                    linkedList.add(2, undisposed);
                }

                if (JSONArray.fromObject(linkedList).toString().contains("反馈信息")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("taskstatus", "完成任务");
                    linkedList.add(data);
                }
                daliyTaskDetailByParamMap.put("taskflow", linkedList);
            }


            return AuthUtil.parseJsonKeyToLower("success", daliyTaskDetailByParamMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/3/8 0008 下午 4:43
     * @Description: 通过任务id获取任务流程
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, session]
     * @throws:
     */
    @RequestMapping(value = "/getTaskManagementRecordByTaskId", method = RequestMethod.POST)
    public Object getTaskManagementRecordByTaskId(@RequestJson(value = "id") String id) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            LinkedList<Map<String, Object>> linkedList = new LinkedList<>();
            paramMap.put("taskid", id);
            //详情逻辑
            AlarmTaskDisposeManagementVO alarmTaskDisposeManagement = alarmTaskDisposeService.selectByPrimaryKey(id);

            //流程逻辑
            Map<String, Object> daliyTaskRecordInfoByParamMap = alarmTaskDisposeService.getDaliyTaskRecordInfoByParamMap(paramMap);
            if (daliyTaskRecordInfoByParamMap.get("taskflow") != null) {
                List<Map<String, Object>> list = (List) daliyTaskRecordInfoByParamMap.get("taskflow");

                Map<String, Object> firstMap = new HashMap<>();
                firstMap.put("TaskHandleTime", alarmTaskDisposeManagement.getTaskcreatetime());
                firstMap.put("taskstatus", "生成任务");
                firstMap.put("taskstatuscode", -1);
                firstMap.put("username", "");
                linkedList.addFirst(firstMap);
                Set<String> set = new HashSet<>();
                for (Map<String, Object> map : list) {
                    if (!"抄送已读".equals(map.get("taskstatus") + "") && !"抄送".equals(map.get("taskstatus") + "") && !set.contains(map.get("taskstatus") + "_" + map.get("taskhandletimestr"))) {
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("TaskHandleTime", map.get("taskhandletimestr"));
                        onemap.put("taskstatus", map.get("taskstatus"));
                        if (("待处理".equals(map.get("taskstatus") + "") || "处理中".equals(map.get("taskstatus") + ""))) {
                            if (map.get("TaskComment") != null && !"".equals(map.get("TaskComment").toString())) {
                                onemap.put("taskcomment", map.get("TaskComment"));
                            } else {
                                onemap.put("taskstatuscode", CommonTypeEnum.AlarmTaskStatusEnum.getCodeByName(map.get("taskstatus") + ""));
                            }
                        } else {
                            if (map.get("TaskComment") != null && !"".equals(map.get("TaskComment").toString())) {
                                onemap.put("taskcomment", map.get("TaskComment"));
                            }
                            onemap.put("taskstatuscode", CommonTypeEnum.AlarmTaskStatusEnum.getCodeByName(map.get("taskstatus") + ""));
                        }
                        String username = "";
                        for (Map<String, Object> map2 : list) {
                            if ((map.get("taskstatus") + "_" + map.get("taskhandletimestr")).equals(map2.get("taskstatus") + "_" + map2.get("taskhandletimestr"))) {
                                username = username + map2.get("username") + "、";
                            }
                        }
                        if (!"".equals(username)) {
                            username = username.substring(0, username.length() - 1);
                        }
                        onemap.put("username", username);
                        linkedList.add(onemap);
                        set.add(map.get("taskstatus") + "_" + map.get("taskhandletimestr"));
                    }
                }
                if (alarmTaskDisposeManagement.getTaskstatus() != null && "4".equals(alarmTaskDisposeManagement.getTaskstatus().toString())) {
                    Map<String, Object> endMap = new HashMap<>();
                    endMap.put("TaskHandleTime", "");
                    endMap.put("taskstatus", "完成");
                    endMap.put("taskstatuscode", 9);
                    endMap.put("username", "");
                    linkedList.add(endMap);
                }
                resultMap.put("taskflow", linkedList);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/8/24 0024 上午 11:13
     * @Description: 导出任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [map, session, response, request]
     * @throws:
     */
    @RequestMapping(value = "/exportTaskManagementByParamMap", method = RequestMethod.POST)
    public void exportTaskManagementByParamMap(@RequestJson(value = "paramsjson", required = false) Object map,
                                               HttpServletResponse response, HttpServletRequest request) {
        try {

            Object taskManagementByParamMap = getTaskManagementByParamMap(map);

            JSONObject jsonObject = JSONObject.fromObject(taskManagementByParamMap);
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object datalist = jsonObject1.get("datalist");
            JSONArray array = JSONArray.fromObject(datalist);

            for (Object o : array) {
                Map<String, Object> stringObjectMap = (Map<String, Object>) o;
                for (String key : stringObjectMap.keySet()) {
                    if ("null".equals(stringObjectMap.get(key).toString())) {
                        stringObjectMap.put(key, "");
                    }
                }
            }

            List<String> headers = new ArrayList<>();
            headers.add("任务名称");
            headers.add("任务执行人");
            headers.add("分派人");
            headers.add("分派时间");
            headers.add("状态");

            List<String> headersfield = new ArrayList<>();
            headersfield.add("taskname");
            headersfield.add("zhixingren");
            headersfield.add("fenpairen");
            headersfield.add("fenpaidate");
            headersfield.add("status");


            HSSFWorkbook hssfWorkbook = ExcelUtil.exportExcel("sheet1", headers, headersfield, array, "yyyy-MM-dd hh:mm:ss");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(hssfWorkbook);
            ExcelUtil.downLoadExcel("日常任务管理", response, request, bytesForWorkBook);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 下午 1:23
     * @Description:通过任务id获取任务详情 app使用
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id, session]
     * @throws:
     */
    @RequestMapping(value = "/getTaskDetailById", method = RequestMethod.POST)
    public Object getTaskDetailById(@RequestJson(value = "id") String id) throws ParseException {
        try {

            String user = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> result = new HashMap<>();
            paramMap.put("taskid", id);
            //详情逻辑
            Map<String, Object> daliyTaskDetailByParamMap = alarmTaskDisposeService.getDaliyTaskDetailByParamMap(paramMap);

            //查询处理人id
            String flowRecordInfoByTaskID = alarmTaskDisposeService.getFlowRecordInfoByTaskID(id);

            if (daliyTaskDetailByParamMap != null) {
                if (daliyTaskDetailByParamMap.get("zhixingren") != null) {
                    Object zhixingren = ((Set) daliyTaskDetailByParamMap.get("zhixingren")).stream().collect(Collectors.joining("、"));
                    result.put("zhixingren", zhixingren);
                } else {
                    result.put("zhixingren", "");
                }
                if (daliyTaskDetailByParamMap.get("problem") != null) {
                    Object problem = ((Set) daliyTaskDetailByParamMap.get("problem")).stream().collect(Collectors.joining("、"));
                    result.put("problem", problem);
                    if (daliyTaskDetailByParamMap.get("status") != null && "处理中".equals(daliyTaskDetailByParamMap.get("status").toString()) && !flowRecordInfoByTaskID.equals(user)) {
                        result.put("problem", "");
                    }
                } else {
                    result.put("problem", "");
                }
                if (daliyTaskDetailByParamMap.get("feedbackresults") != null) {
                    result.put("feedbackresults", daliyTaskDetailByParamMap.get("feedbackresults"));
                    if (daliyTaskDetailByParamMap.get("status") != null && "处理中".equals(daliyTaskDetailByParamMap.get("status").toString()) && !flowRecordInfoByTaskID.equals(user)) {
                        result.put("feedbackresults", "");
                    }
                } else {
                    result.put("feedbackresults", "");
                }

                result.put("taskremark", daliyTaskDetailByParamMap.get("taskremark"));
                result.put("fenpairen", daliyTaskDetailByParamMap.get("fenpairen"));
                result.put("status", daliyTaskDetailByParamMap.get("status"));
                result.put("fileid", daliyTaskDetailByParamMap.get("fileid"));

            }
            Map<String, Object> daliyTaskRecordInfoByParamMap = alarmTaskDisposeService.getDaliyTaskRecordInfoByParamMap(paramMap);

            if (daliyTaskRecordInfoByParamMap.get("taskflow") != null) {
                List<Map<String, Object>> list = (List) daliyTaskRecordInfoByParamMap.get("taskflow");
                List<String> collect = list.stream().filter(m -> m.get("taskstatus") != null && m.get("taskhandletimestr") != null && "分派任务".equals(m.get("taskstatus").toString())).map(m -> m.get("taskhandletimestr").toString()).collect(Collectors.toList());
                if (collect.size() > 0) {
                    result.put("taskhandletime", collect.get(0));
                } else {
                    result.put("taskhandletime", "");
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> getRightList(String userid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userid", userid);
        paramMap.put("moduletypes",
                Arrays.asList(CommonTypeEnum.ModuleTypeEnum.AlarmEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.DevEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.TBEnum.getCode(),
                        CommonTypeEnum.ModuleTypeEnum.RCFPEnum.getCode()

                )
        );
        List<String> rightList = checkProblemExpoundService.getUserModuleByParam(paramMap);
        return rightList;
    }

    /**
     * @author: lip
     * @date: 2020/05/06 11:15
     * @Description: 统计首页工单任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countHomePageTaskData", method = RequestMethod.POST)
    public Object countHomePageTaskData(
            @RequestJson(value = "monitortime", required = false) String monitortime,
            @RequestJson(value = "tasktypes", required = false) List<Integer> tasktypes,
            @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
            @RequestJson(value = "taskstatuscodes", required = false) List<String> taskstatuscodes,
            @RequestJson(value = "searchname", required = false) String searchname) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> taskresult = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            if (StringUtils.isBlank(monitortime)) {
                monitortime = DataFormatUtil.getDateYMD(new Date());
            }
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("categorys", 1);
            String startTime = monitortime + " 00:00:00";
            String endTime = monitortime + " 23:59:59";
            param.put("taskstarttime", startTime);
            param.put("taskendtime", endTime);
            param.put("searchname", searchname);
            param.put("statuslist", taskstatuscodes);
            //根据监测时间获取该时间段的报警任务
            if (tasktypes == null || tasktypes.size() == 0) {
                tasktypes.add(AlarmTaskEnum.getCode());
                tasktypes.add(DevOpsTaskEnum.getCode());
                tasktypes.add(ChangeAlarmTaskEnum.getCode());
            }
            //取配置文件时间
            int unallocatedtime = Integer.parseInt(DataFormatUtil.parseProperties("unallocatedtime.minute"));//报警任务
            //取配置文件时间
            int uncompletetime = Integer.parseInt(DataFormatUtil.parseProperties("uncompletetime.minute"));//报警任务
            param.put("unallocatedtime", unallocatedtime);
            param.put("uncompletetime", uncompletetime);
            param.put("monitorpointtypes", monitorpointtypes);
            for (Integer code : tasktypes) {
                if (code == AlarmTaskEnum.getCode()) {
                    param.put("datauserid", userId);
                    param.put("feedbackuserid", userId);
                    param.put("tasktype", code);
                    List<String> rightList = getRightList(userId);
                    if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.AlarmFPEnum.getCode())) {
                        param.put("hasauthor", "1");
                    } else {
                        param.put("hasauthor", "0");
                    }
                    //报警工单
                    List<Map<String, Object>> alarmlist = alarmTaskDisposeService.getAllAlarmTaskByParamForHome(param);
                    if (alarmlist != null && alarmlist.size() > 0) {
                        List<String> pollutionids = new ArrayList<>();
                        for (Map<String, Object> map : alarmlist) {
                            String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                            Map<String, Object> pollutionData = new HashMap<>();
                            pollutionData.put("remindcode", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode());
                            pollutionData.put("tasktype", "报警工单");
                            pollutionData.put("tasktypecode", AlarmTaskEnum.getCode());
                            pollutionData.put("remindname", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getName());
                            pollutionData.put("lasttime", map.get("TaskCreateTime") != null ? (map.get("TaskCreateTime").toString()).substring(11, 19) : "");
                            pollutionData.put("taskstatuscode", status);
                            pollutionData.put("taskid", map.get("PK_TaskID"));
                            pollutionData.put("taskstatusname", map.get("TaskStatusName"));
                            pollutionData.put("overtime", map.get("overtime"));
                            pollutionData.put("username", map.get("user_name"));
                            pollutionData.put("Category", map.get("Category"));
                            pollutionData.put("entpointname", map.get("monitorpointname"));
                            pollutionData.put("alarmovertime", map.get("alarmovertime"));
                            pollutionData.put("recoverystatusname", map.get("recoverystatusname"));
                            pollutionData.put("recoverystatus", map.get("RecoveryStatus"));
                            Map<String, Object> mapmap = new HashMap<>();
                            mapmap.put("longitude", map.get("Longitude"));
                            mapmap.put("latitude", map.get("Latitude"));
                            mapmap.put("alarmlevel", map.get("onlineAlarmLevel"));
                            mapmap.put("status", map.get("onlinestatus"));
                            if (map.get("MonitorPointCategory") != null && !"".equals(map.get("MonitorPointCategory").toString())) {
                                mapmap.put("pointcategory", map.get("MonitorPointCategory"));
                            }
                            pollutionData.put("mapdata", mapmap);
                            pollutionData.put("uncompleteflag", map.get("uncompleteflag"));
                            if (map.get("overuncompletetime") != null) {
                                pollutionData.put("overuncompletetime", map.get("overuncompletetime"));
                            }
                            pollutionData.put("unallocatedflag", map.get("unallocatedflag"));
                            if (map.get("overunallocatedtime") != null) {
                                pollutionData.put("overunallocatedtime", map.get("overunallocatedtime"));
                            }
                            pollutionData.put("isfeedback", map.get("isfeedback"));
                            pollutionData.put("isgenerate", map.get("isgenerate"));
                            pollutionData.put("starttime", map.get("AlarmStartTime"));
                            pollutionData.put("endtime", map.get("TaskEndTime"));
                            pollutionData.put("pollutionid", map.get("pollutionid"));
                            pollutionData.put("monitorpointid", map.get("monitorpointid"));
                            if (map.get("pollutionid") != null && !"".equals(map.get("pollutionid").toString())) {
                                pollutionids.add(map.get("pollutionid").toString());
                            }
                            List<Map<String, Object>> pointDataList = new ArrayList<>();
                            pollutionData.putIfAbsent("pollutionname", map.get("PollutionName"));
                            Map<String, Object> pointData = new HashMap<>();
                            pointData.put("monitorpointtypecode", map.get("FK_MonitorPointTypeCode"));
                            pointData.put("monitorpointtypename", map.get("monitorpointtypename"));
                            pointData.put("monitorpointid", map.get("monitorpointid"));
                            pointData.put("dgimn", map.get("DGIMN"));

                            pointData.put("monitorpointname", map.get("pointname"));
                            Map<String, Object> pollutantmap = new HashMap<>();
                            pollutantmap.put("remindtypename", "");
                            pollutantmap.put("subcountnum", null);
                            pollutantmap.put("pollutantnames", map.get("pollutantname"));
                            pointData.put("pollutantlist", pollutantmap);
                            pointDataList.add(pointData);
                            pollutionData.put("pointdatalist", pointDataList);
                            taskresult.add(pollutionData);
                        }

                        //添加环保信息
                        param.putIfAbsent("pollutionids", pollutionids);
                        List<Map<String, Object>> pollutionDataList = pollutionService.getPollutionNameAndPkid(param);
                        if (pollutionDataList.size() > 0) {
                            Map<String, Object> pollutionIdAndEnvironmentalManager = new HashMap<>();
                            Map<String, Object> pollutionIdAndLinkManPhone = new HashMap<>();
                            for (Map<String, Object> pollutionData : pollutionDataList) {
                                pollutionIdAndEnvironmentalManager.put(pollutionData.get("pollutionid").toString(), pollutionData.get("EnvironmentalManager"));
                                pollutionIdAndLinkManPhone.put(pollutionData.get("pollutionid").toString(), pollutionData.get("LinkManPhone"));
                            }
                            for (Map<String, Object> dataMap : taskresult) {
                                Map<String, Object> hbdata = new HashMap<>();
                                hbdata.put("leader", pollutionIdAndEnvironmentalManager.get(dataMap.get("pollutionid")));
                                hbdata.put("phone", pollutionIdAndLinkManPhone.get(dataMap.get("pollutionid")));
                                dataMap.put("hbdata", hbdata);
                            }
                        }
                    }
                } else if (code == ChangeAlarmTaskEnum.getCode()) {
                    param.put("datauserid", userId);
                    param.put("feedbackuserid", userId);
                    param.put("tasktype", code);
                    List<String> rightList = getRightList(userId);
                    if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.TBEnum.getCode())) {
                        param.put("hasauthor", "1");
                    } else {
                        param.put("hasauthor", "0");
                    }
                    List<Map<String, Object>> alarmlist = alarmTaskDisposeService.getAllAlarmTaskByParamForHome(param);
                    if (alarmlist != null && alarmlist.size() > 0) {
                        List<String> pollutionids = new ArrayList<>();
                        for (Map<String, Object> map : alarmlist) {
                            String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                            Map<String, Object> pollutionData = new HashMap<>();
                            pollutionData.put("remindcode", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
                            pollutionData.put("tasktype", "突变工单");
                            pollutionData.put("tasktypecode", ChangeAlarmTaskEnum.getCode());
                            pollutionData.put("remindname", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName());
                            pollutionData.put("lasttime", map.get("TaskCreateTime") != null ? (map.get("TaskCreateTime").toString()).substring(11, 19) : "");
                            pollutionData.put("taskstatuscode", status);
                            pollutionData.put("taskid", map.get("PK_TaskID"));
                            pollutionData.put("taskstatusname", map.get("TaskStatusName"));
                            pollutionData.put("overtime", map.get("overtime"));
                            pollutionData.put("Category", map.get("Category"));
                            pollutionData.put("entpointname", map.get("monitorpointname"));
                            pollutionData.put("alarmovertime", map.get("alarmovertime"));
                            pollutionData.put("recoverystatusname", map.get("recoverystatusname"));
                            pollutionData.put("recoverystatus", map.get("RecoveryStatus"));
                            Map<String, Object> mapmap = new HashMap<>();
                            mapmap.put("longitude", map.get("Longitude"));
                            mapmap.put("latitude", map.get("Latitude"));
                            mapmap.put("alarmlevel", map.get("onlineAlarmLevel"));
                            mapmap.put("status", map.get("onlinestatus"));
                            pollutionData.put("mapdata", mapmap);
                            pollutionData.put("username", map.get("user_name"));
                            pollutionData.put("uncompleteflag", map.get("uncompleteflag"));
                            if (map.get("overuncompletetime") != null) {
                                pollutionData.put("overuncompletetime", map.get("overuncompletetime"));
                            }
                            pollutionData.put("unallocatedflag", map.get("unallocatedflag"));
                            if (map.get("overunallocatedtime") != null) {
                                pollutionData.put("overunallocatedtime", map.get("overunallocatedtime"));
                            }
                            pollutionData.put("isfeedback", map.get("isfeedback"));
                            pollutionData.put("isgenerate", map.get("isgenerate"));
                            pollutionData.put("starttime", map.get("AlarmStartTime"));
                            pollutionData.put("endtime", map.get("TaskEndTime"));
                            pollutionData.put("pollutionid", map.get("pollutionid"));
                            pollutionData.put("monitorpointid", map.get("monitorpointid"));
                            if (map.get("pollutionid") != null && !"".equals(map.get("pollutionid").toString())) {
                                pollutionids.add(map.get("pollutionid").toString());
                            }
                            List<Map<String, Object>> pointDataList = new ArrayList<>();
                            pollutionData.putIfAbsent("pollutionname", map.get("PollutionName"));
                            Map<String, Object> pointData = new HashMap<>();
                            pointData.put("monitorpointtypecode", map.get("FK_MonitorPointTypeCode"));
                            pointData.put("monitorpointid", map.get("monitorpointid"));
                            pointData.put("dgimn", map.get("DGIMN"));
                            pointData.put("monitorpointname", map.get("pointname"));
                            Map<String, Object> pollutantmap = new HashMap<>();
                            pollutantmap.put("remindtypename", "");
                            pollutantmap.put("subcountnum", null);
                            pollutantmap.put("pollutantnames", map.get("pollutantname"));
                            pointData.put("pollutantlist", pollutantmap);
                            pointDataList.add(pointData);
                            pollutionData.put("pointdatalist", pointDataList);
                            taskresult.add(pollutionData);
                        }

                        //添加环保信息
                        param.putIfAbsent("pollutionids", pollutionids);
                        List<Map<String, Object>> pollutionDataList = pollutionService.getPollutionNameAndPkid(param);
                        if (pollutionDataList.size() > 0) {
                            Map<String, Object> pollutionIdAndEnvironmentalManager = new HashMap<>();
                            Map<String, Object> pollutionIdAndLinkManPhone = new HashMap<>();
                            for (Map<String, Object> pollutionData : pollutionDataList) {
                                pollutionIdAndEnvironmentalManager.put(pollutionData.get("pollutionid").toString(), pollutionData.get("EnvironmentalManager"));
                                pollutionIdAndLinkManPhone.put(pollutionData.get("pollutionid").toString(), pollutionData.get("LinkManPhone"));
                            }
                            for (Map<String, Object> dataMap : taskresult) {
                                Map<String, Object> hbdata = new HashMap<>();
                                hbdata.put("leader", pollutionIdAndEnvironmentalManager.get(dataMap.get("pollutionid")));
                                hbdata.put("phone", pollutionIdAndLinkManPhone.get(dataMap.get("pollutionid")));
                                dataMap.put("hbdata", hbdata);
                            }
                        }
                    }
                } else if (code == DevOpsTaskEnum.getCode()) {//运维工单
                    param.put("datauserid", userId);
                    param.put("feedbackuserid", userId);
                    param.put("tasktype", code);
                    List<String> rightList = getRightList(userId);
                    if (rightList.contains(CommonTypeEnum.ModuleItemCodeEnum.DevFPEnum.getCode())) {
                        param.put("hasauthor", "1");
                    } else {
                        param.put("hasauthor", "0");
                    }
                    List<Map<String, Object>> devtaskresult = devOpsTaskDisposeService.getAllDevOpsTaskByParamForHome(param);
                    if (devtaskresult != null && devtaskresult.size() > 0) {
                        List<String> pollutionids = new ArrayList<>();
                        List<String> monitorpointtype = new ArrayList<>();
                        for (Map<String, Object> map : devtaskresult) {
                            String status = map.get("TaskStatus") != null ? map.get("TaskStatus").toString() : "";
                            Map<String, Object> pollutionData = new HashMap<>();
                            pollutionData.put("remindcode", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());
                            pollutionData.put("tasktype", "运维工单");
                            pollutionData.put("tasktypecode", DevOpsTaskEnum.getCode());
                            pollutionData.put("remindname", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getName());
                            pollutionData.put("lasttime", map.get("TaskCreateTime") != null ? (map.get("TaskCreateTime").toString()).substring(11, 19) : "");
                            pollutionData.put("taskstatuscode", status);
                            pollutionData.put("taskid", map.get("PK_TaskID"));
                            pollutionData.put("Category", map.get("Category"));
                            pollutionData.put("taskstatusname", map.get("TaskStatusName"));
                            pollutionData.put("overtime", map.get("overtime"));
                            pollutionData.put("username", map.get("user_name"));
                            pollutionData.put("entpointname", map.get("monitorpointname"));
                            pollutionData.put("alarmovertime", map.get("alarmovertime"));
                            pollutionData.put("recoverystatusname", map.get("recoverystatusname"));
                            pollutionData.put("recoverystatus", map.get("RecoveryStatus"));
                            Map<String, Object> mapmap = new HashMap<>();
                            mapmap.put("longitude", map.get("Longitude"));
                            mapmap.put("latitude", map.get("Latitude"));
                            mapmap.put("alarmlevel", null);
                            mapmap.put("status", map.get("onlinestatus"));
                            pollutionData.put("mapdata", mapmap);
                            pollutionData.put("uncompleteflag", map.get("uncompleteflag"));
                            if (map.get("overuncompletetime") != null) {
                                pollutionData.put("overuncompletetime", map.get("overuncompletetime"));
                            }
                            pollutionData.put("unallocatedflag", map.get("unallocatedflag"));
                            if (map.get("overunallocatedtime") != null) {
                                pollutionData.put("overunallocatedtime", map.get("overunallocatedtime"));
                            }
                            pollutionData.put("isfeedback", map.get("isfeedback"));
                            pollutionData.put("isgenerate", map.get("isgenerate"));
                            pollutionData.put("starttime", map.get("AlarmStartTime"));
                            pollutionData.put("endtime", map.get("TaskEndTime"));
                            pollutionData.put("pollutionid", map.get("pollutionid"));
                            pollutionData.put("monitorpointid", map.get("monitorpointid"));
                            if (map.get("pollutionid") != null && !"".equals(map.get("pollutionid").toString())) {
                                pollutionids.add(map.get("pollutionid").toString());
                            }
                            if (map.get("FK_MonitorPointTypeCode") != null && !"".equals(map.get("FK_MonitorPointTypeCode").toString())) {
                                monitorpointtype.add(map.get("FK_MonitorPointTypeCode").toString());
                            }

                            List<Map<String, Object>> pointDataList = new ArrayList<>();
                            pollutionData.putIfAbsent("pollutionname", map.get("PollutionName"));
                            Map<String, Object> pointData = new HashMap<>();
                            pointData.put("monitorpointtypecode", map.get("FK_MonitorPointTypeCode"));
                            pointData.put("monitorpointid", map.get("monitorpointid"));
                            pointData.put("dgimn", map.get("DGIMN"));
                            pointData.put("monitorpointname", map.get("pointname"));
                            Map<String, Object> pollutantmap = new HashMap<>();
                            pollutantmap.put("remindtypename", "");
                            pollutantmap.put("subcountnum", null);
                            pollutantmap.put("pollutantnames", map.get("pollutantname"));
                            pointData.put("pollutantlist", pollutantmap);
                            pointDataList.add(pointData);
                            pollutionData.put("pointdatalist", pointDataList);
                            taskresult.add(pollutionData);
                        }
                        //企业运维信息
                        Map<String, Object> paramMap = new HashMap<>();
                        paramMap.put("monitorpointtypecodes", monitorpointtype);
                        paramMap.put("pollutionIds", pollutionids);
                        paramMap.put("monitorpointIds", pollutionids);
                        List<Map<String, Object>> devOpsDataList = entDevOpsInfoService.getEntDevOpsDataByParamMap(paramMap);
                        if (devOpsDataList.size() > 0) {
                            Map<String, Object> keyAndUnit = new HashMap<>();
                            Map<String, Object> keyAndPhone = new HashMap<>();
                            Map<String, Object> keyAndUser = new HashMap<>();
                            String key;
                            for (Map<String, Object> devOpsData : devOpsDataList) {
                                key = devOpsData.get("pollutionid") + "#" + devOpsData.get("monitorpointtypecode");
                                keyAndUnit.put(key, devOpsData.get("DevOpsUnit"));
                                keyAndPhone.put(key, devOpsData.get("Telephone"));
                                keyAndUser.put(key, devOpsData.get("usernames"));
                            }
                            for (Map<String, Object> dataMap : devtaskresult) {
                                key = dataMap.get("pollutionid") + "#" + dataMap.get("FK_MonitorPointTypeCode");
                                Map<String, Object> ywdata = new HashMap<>();
                                ywdata.put("company", keyAndUnit.get(key));
                                ywdata.put("people", keyAndUser.get(key));
                                ywdata.put("phone", keyAndPhone.get(key));
                                dataMap.put("ywdata", ywdata);

                            }
                        } else {
                            for (Map<String, Object> dataMap : devtaskresult) {
                                Map<String, Object> ywdata = new HashMap<>();
                                ywdata.put("company", "");
                                ywdata.put("people", "");
                                ywdata.put("phone", "");
                                dataMap.put("ywdata", ywdata);
                            }
                        }
                    }
                }
                if (taskresult.size() > 0) {
                    Map<String, Object> countData = new HashMap<>();
                    int unassignedTask = 0;
                    int undisposedEnum = 0;
                    int handleEnum = 0;
                    int completedEnum = 0;
                    List<String> unassignedTasks = Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode() + "");
                    List<String> undisposedTasks = Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UndisposedEnum.getCode() + "");
                    List<String> handleTasks = Arrays.asList(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.HandleEnum.getCode() + "");
                    List<String> completedTasks = Arrays.asList(
                            CommonTypeEnum.AlarmTaskDisposalScheduleEnum.CompletedEnum.getCode() + "",
                            CommonTypeEnum.AlarmTaskDisposalScheduleEnum.NeglectEnum.getCode() + ""
                    );
                    for (Map<String, Object> dataMap : taskresult) {
                        if (unassignedTasks.contains(dataMap.get("taskstatuscode"))) {
                            unassignedTask++;
                        } else if (undisposedTasks.contains(dataMap.get("taskstatuscode"))) {
                            undisposedEnum++;
                        } else if (handleTasks.contains(dataMap.get("taskstatuscode"))) {
                            handleEnum++;
                        } else if (completedTasks.contains(dataMap.get("taskstatuscode"))) {
                            completedEnum++;
                        }
                    }
                    countData.put("unassignedTask", unassignedTask);
                    countData.put("undisposedEnum", undisposedEnum);
                    countData.put("handleEnum", handleEnum);
                    countData.put("completedEnum", completedEnum);
                    resultMap.put("countdata", countData);
                }
                taskresult = taskresult.stream().sorted(Comparator.comparing(m -> ((Map) m).get("lasttime").toString()).reversed()).collect(Collectors.toList());
                resultMap.put("datalist", taskresult);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: lip
     * @date: 2020/5/13 0013 下午 2:08
     * @Description: 统计一段时间内报警任务、运维任务数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "/countTaskDataByParam", method = RequestMethod.POST)
    public Object countTaskDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
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
            paramMap.put("tasktypelist", types);
            List<Integer> onetypelist = new ArrayList<>();
            onetypelist.addAll(Arrays.asList(new Integer[]{AlarmTaskEnum.getCode(), CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode(), ChangeAlarmTaskEnum.getCode()}));
            List<Map<String, Object>> countDataList = alarmTaskDisposeService.countTaskDataByParamGroupByTaskType(paramMap);
            if (countDataList.size() > 0) {
                for (Integer i : onetypelist) {
                    Map<String, Object> obj = new HashMap<>();
                    List<String> twotypelist = new ArrayList<>();
                    if (i == CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode()) {
                        obj.put("workordername", "超标报警工单");
                        twotypelist.add(CommonTypeEnum.TaskTypeEnum.AlarmTaskEnum.getCode() + "");
                        twotypelist.add(CommonTypeEnum.TaskTypeEnum.MonitorTaskEnum.getCode() + "");
                        twotypelist.add(CommonTypeEnum.TaskTypeEnum.SupervisoryControlEnum.getCode() + "");
                    } else if (i == CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode()) {
                        obj.put("workordername", "运维报警工单");
                        twotypelist.add(CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode() + "");
                        twotypelist.add(CommonTypeEnum.TaskTypeEnum.SecurityDevOpsTaskEnum.getCode() + "");
                    } else if (i == CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode()) {
                        obj.put("workordername", "突变报警工单");
                        twotypelist.add(CommonTypeEnum.TaskTypeEnum.ChangeAlarmTaskEnum.getCode() + "");
                    }
                    obj.put("tasktype", i);
                    int count = 0;
                    for (Map<String, Object> map : countDataList) {
                        String taskType = map.get("FK_TaskType") != null ? map.get("FK_TaskType").toString() : "";
                        if (twotypelist.contains(taskType)) {
                            int num = map.get("countnum") != null ? Integer.parseInt(map.get("countnum").toString()) : 0;
                            count = count + num;
                        }
                    }
                    obj.put("countnum", count);
                    resultList.add(obj);
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
     * @date: 2020/06/09 0009 上午 8:46
     * @Description: 根据推送的队列数据新建报警及运维任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addAlarmAndDevOpsTaskInfoByMQData", method = RequestMethod.POST)
    public List<JSONObject> addAlarmAndDevOpsTaskInfoByMQData(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        //rediskey
        String alarmtaskrediskey = DataFormatUtil.parseProperties("alarmtaskrediskey");//报警任务
        String devopstaskrediskey = DataFormatUtil.parseProperties("devopstaskrediskey");//运维任务
        String changetaskrediskey = DataFormatUtil.parseProperties("changetaskrediskey");//突变任务
        String overlevelorder = DataFormatUtil.parseProperties("overlevelorder");//报警级别排序
        //获取是否使用旧生成逻辑生成任务
        String isusenewlogic = DataFormatUtil.parseProperties("isusenewlogic");
        //没有该配置 默认走该套逻辑 生成任务
        if (isusenewlogic == null || "".equals(isusenewlogic)) {
            isusenewlogic = "0";
        }
        Long unixTimeInMillis = getUnixTimeInMillis();
        try {
            Map<String, Object> paramMap = JSONObject.fromObject(paramsjson);
            Map<String, Object> param = new HashMap<>();
            List<Map<String, Object>> mqdata = (List<Map<String, Object>>) paramMap.get("MQMessage");
            //报警数据类型
            String datatype = paramMap.get("DataType") != null ? paramMap.get("DataType").toString() : "";
            //判断数据类型
            if (!"RealTimeData".equals(datatype) && !"MinuteData".equals(datatype) && !"HourData".equals(datatype)) {
                return null;
            }
            Object iscontinueover = null;//是否连续超标
            Object iscontinueexception = null;//是否连续异常
            Object iscontinuechange = null;//是否连续突变
            Object overlevelcode = "";//超限级别
            //是否连续 超标 异常 突变
            if (paramMap.get("IsContinueOver") != null && !"null".equals(paramMap.get("IsContinueOver").toString())) {
                iscontinueover = Boolean.parseBoolean(paramMap.get("IsContinueOver").toString());
            }
            if (paramMap.get("IsContinueException") != null && !"null".equals(paramMap.get("IsContinueException").toString())) {
                iscontinueexception = Boolean.parseBoolean(paramMap.get("IsContinueException").toString());
            }
            if (paramMap.get("IsContinueChange") != null && !"null".equals(paramMap.get("IsContinueChange").toString())) {
                iscontinuechange = Boolean.parseBoolean(paramMap.get("IsContinueChange").toString());
            }
            //判断消息类型为2
            if (paramMap.get("MessageType") == null || !"2".equals(paramMap.get("MessageType").toString())) {
                return null;
            }
            //判断报警限值序列 用于判断污染物是报警是更严重还是减轻
            Map<String, Integer> overlevelmap = new HashMap<>();
            if (overlevelorder != null && !"".equals(overlevelorder)) {
                String[] overlevelstr = overlevelorder.split(",");
                int i = overlevelstr.length;
                for (String str : overlevelstr) {
                    overlevelmap.put(str, i);
                    i = i - 1;
                }
            }
            boolean overflag = false;//是否超标
            boolean exceptionflag = false;//是否异常
            boolean changeflag = false;//是否突变
            String daytime = "";//报警日期
            String recoverytime = "";//恢复时间
            String level = "";//报警级别
            String exceptiontime = "";//异常开始时间
            //污染物
            List<String> overcodes = new ArrayList<>();//接收报警数据中的超标污染物
            List<String> exceptioncodes = new ArrayList<>();//接收报警数据中的异常污染物
            List<String> changecodes = new ArrayList<>();//接收报警数据中的突变污染物
            //报警点位ID
            String monitorpointid = (paramMap.get("MonitorPointId") != null && !"null".equals(paramMap.get("MonitorPointId").toString())) ? paramMap.get("MonitorPointId").toString() : "";
            //监测类型
            String monitorpointtypecode = (paramMap.get("MonitorPointTypeCode") != null && !"null".equals(paramMap.get("MonitorPointTypeCode").toString())) ? paramMap.get("MonitorPointTypeCode").toString() : "";
            //获取恢复时间
            if (paramMap.get("RecoveryTime") != null && !"null".equals(paramMap.get("RecoveryTime").toString()) && !"".equals(paramMap.get("RecoveryTime").toString())) {
                recoverytime = paramMap.get("RecoveryTime").toString();
            }
            //获取日期
            if (paramMap.get("DateTime") != null && !"null".equals(paramMap.get("DateTime").toString()) && !"".equals(paramMap.get("DateTime").toString())) {
                daytime = paramMap.get("DateTime").toString();
            }
            //若是当天报警数据  判断报警类型
            if (!"".equals(daytime) && (daytime.substring(0, 10).equals(DataFormatUtil.getDateYMD(new Date()))) && mqdata != null && mqdata.size() > 0) {
                for (Map<String, Object> mqmap : mqdata) {
                    //获取报警类型
                    String str = mqmap.get("AlarmType") != null ? mqmap.get("AlarmType").toString() : "";
                    //判断污染物的报警类型（超标超限、异常、突变）
                    if (CommonTypeEnum.RabbitMQAlarmTypeEnum.ExceptionMessage.getCode().equals(str)) {//异常
                        String firstalarmdatetime = "";
                        //获取报警开始时间（超标、异常）
                        if (mqmap.get("FirstAlarmTime") != null && !"null".equals(mqmap.get("FirstAlarmTime").toString())) {
                            firstalarmdatetime = mqmap.get("FirstAlarmTime").toString();
                        }
                        //异常
                        exceptioncodes.add(mqmap.get("PollutantCode").toString() + "_" + (mqmap.get("ExceptionType").toString()));
                        exceptionflag = true;
                        //获取异常开始时间
                        exceptiontime = getAlarmFirstTime(exceptiontime, firstalarmdatetime);//比较时间 获取最早一条报警时间
                    } else if (CommonTypeEnum.RabbitMQAlarmTypeEnum.OverLimitMessage.getCode().equals(str)
                            || CommonTypeEnum.RabbitMQAlarmTypeEnum.OverStandardMessage.getCode().equals(str)) {
                        //超标超限
                        overflag = true;
                        //超标污染物
                        if (mqmap.get("PollutantCode") != null && !"".equals(mqmap.get("PollutantCode").toString())) {
                            overcodes.add(mqmap.get("PollutantCode").toString());
                        }
                        //超标级别
                        level = mqmap.get("AlarmLevel") != null ? mqmap.get("AlarmLevel").toString() : "";
                        //判断超标级别
                        if ("".equals(overlevelcode.toString())) {
                            overlevelcode = level;
                        } else {//已有级别  比较级别 保存最大的级别
                            if (!"".equals(level) && overlevelmap.get(level) != null && overlevelmap.get(overlevelcode) != null) {//比较级别大小
                                if (Integer.parseInt(overlevelmap.get(level).toString()) > Integer.parseInt(overlevelmap.get(overlevelcode).toString())) {
                                    overlevelcode = level;
                                }
                            }
                        }
                    } else if (CommonTypeEnum.RabbitMQAlarmTypeEnum.ConcentrationChangeMessage.getCode().equals(str)) {
                        //突变
                        changeflag = true;
                        if (mqmap.get("PollutantCode") != null && !"".equals(mqmap.get("PollutantCode").toString())) {
                            changecodes.add(mqmap.get("PollutantCode").toString());
                        }
                    }
                }
            }
            //填充参数
            param.put("daytime", daytime);
            param.put("recoverytime", recoverytime);
            param.put("dgimn", paramMap.get("MN"));
            param.put("datatype", datatype);
            param.put("monitorpointid", monitorpointid);
            param.put("monitorpointtypecode", monitorpointtypecode);
            param.put("overcodes", overcodes);
            param.put("exceptioncodes", exceptioncodes);
            param.put("changecodes", changecodes);
            param.put("exceptionfirsttime", exceptiontime);
            String day = daytime.substring(0, 10);
            day = day.replaceAll("-", "");
            List<JSONObject> listmessage = new ArrayList<>();
            //isusenewlogic 为0  走旧的任务生成逻辑
            if (isusenewlogic.equals("0")) {
                if (overflag) {//超标任务
                    if ("HourData".equals(datatype)) {//为小时数据时  判断是否连续超标
                        iscontinueover = monitorPointService.getHourOverDataIsContinueOverJudge(paramMap.get("MN"), daytime);
                    }
                    param.put("iscontinueover", iscontinueover);
                    //环保 报警任务
                    if (!"".equals(daytime) && iscontinueover != null) {//当天 且有传连续标记
                        JSONObject messageobject = new JSONObject();
                        messageobject.put("PollutionID", paramMap.get("PollutionID"));//用于自动分派
                        boolean isPut = RedisTemplateUtil.hasKey(day + "_" + alarmtaskrediskey);
                        JSONArray jsonObject2 = new JSONArray();
                        if (isPut) {//存在key
                            jsonObject2 = RedisTemplateUtil.getCache(day + "_" + alarmtaskrediskey, JSONArray.class) == null ? new JSONArray() : RedisTemplateUtil.getCache(day + "_" + alarmtaskrediskey, JSONArray.class);
                        } else {//不存在key 从库中查询当天最新报警任务
                            param.put("daytime", daytime.substring(0, 10));
                            param.put("tasktype", AlarmTaskEnum.getCode().toString());
                            List<Map<String, Object>> tasklist = alarmTaskDisposeService.getMonitorPointLastTaskInfoByTaskType(param);
                            jsonObject2 = JSONArray.fromObject(tasklist);
                            param.put("daytime", daytime);
                        }
                        //判断处理报警数据
                        addAlarmTaskInfoForAlarmData(param, alarmtaskrediskey, unixTimeInMillis, messageobject, jsonObject2, overlevelcode, overlevelmap);
                        if (messageobject.get("TaskID") != null) {
                            listmessage.add(messageobject);
                        }
                    }
                }
            }
            //有异常数据时
            if (exceptionflag) {//异常任务
                if ("HourData".equals(datatype)) {//为小时数据时  判断是否连续超标
                    iscontinueexception = monitorPointService.getHourDataIsContinueExceptionJudge(paramMap.get("MN"), daytime);
                }
                param.put("iscontinueexception", iscontinueexception);
                if (!"".equals(daytime) && iscontinueexception != null) {
                    JSONObject messageobject = new JSONObject();
                    messageobject.put("PollutionID", paramMap.get("PollutionID"));//用于自动分派
                    boolean isPut = RedisTemplateUtil.hasKey(day + "_" + devopstaskrediskey);
                    JSONArray jsonObject2 = new JSONArray();
                    if (isPut) {//存在key
                        jsonObject2 = RedisTemplateUtil.getCache(day + "_" + devopstaskrediskey, JSONArray.class) == null ? new JSONArray() : RedisTemplateUtil.getCache(day + "_" + devopstaskrediskey, JSONArray.class);
                    } else {//不存在key 从库中查询当天最新运维任务
                        param.put("daytime", daytime.substring(0, 10));
                        param.put("tasktype", DevOpsTaskEnum.getCode().toString());
                        List<Map<String, Object>> tasklist = alarmTaskDisposeService.getMonitorPointLastTaskInfoByTaskType(param);
                        jsonObject2 = JSONArray.fromObject(tasklist);
                        param.put("daytime", daytime);
                    }
                    addDevOpsTaskInfoForDevOpsData(param, devopstaskrediskey, unixTimeInMillis, messageobject, jsonObject2, DevOpsTaskEnum.getCode());
                    if (messageobject.get("TaskID") != null) {
                        listmessage.add(messageobject);
                    }
                }
            }
            if (changeflag && "MinuteData".equals(datatype)) {//突变任务  只处理分钟数据
               /* if ("HourData".equals(datatype)) {//为小时数据时  判断是否连续突变
                    iscontinuechange = monitorPointService.getHourDataIsContinueChangeJudge(paramMap.get("MN"), daytime);
                }*/
                param.put("iscontinuechange", iscontinuechange);
                if (!"".equals(daytime) && iscontinuechange != null) {
                    JSONObject messageobject = new JSONObject();
                    messageobject.put("PollutionID", paramMap.get("PollutionID"));//用于自动分派
                    boolean isPut = RedisTemplateUtil.hasKey(day + "_" + changetaskrediskey);
                    JSONArray jsonObject2 = new JSONArray();
                    if (isPut) {//存在key
                        jsonObject2 = RedisTemplateUtil.getCache(day + "_" + changetaskrediskey, JSONArray.class) == null ? new JSONArray() : RedisTemplateUtil.getCache(day + "_" + changetaskrediskey, JSONArray.class);
                    } else {//不存在key 从库中查询当天最新突变任务
                        param.put("daytime", daytime.substring(0, 10));
                        param.put("tasktype", ChangeAlarmTaskEnum.getCode().toString());
                        List<Map<String, Object>> tasklist = alarmTaskDisposeService.getMonitorPointLastTaskInfoByTaskType(param);
                        jsonObject2 = JSONArray.fromObject(tasklist);
                        param.put("daytime", daytime);
                    }
                    addTaskInfoForAlarmData(param, changetaskrediskey, unixTimeInMillis, messageobject, jsonObject2, ChangeAlarmTaskEnum.getCode());
                    if (messageobject.get("TaskID") != null) {
                        listmessage.add(messageobject);
                    }
                }
            }
            if (listmessage != null && listmessage.size() > 0) {
                //有生成任务时  获取在线的用户ID
                List<String> userids = getOnlineUsrrIds();
                for (JSONObject messageobject : listmessage) {
                    messageobject.put("MessageType", paramMap.get("MessageType"));
                    messageobject.put("MonitorPointTypeCode", paramMap.get("MonitorPointTypeCode"));
                    messageobject.put("DateTime", !"".equals(daytime) ? daytime.substring(0, 10) : "");
                    messageobject.put("PollutantName", paramMap.get("MessageType"));
                    messageobject.put("PollutionName", paramMap.get("PollutionName"));
                    messageobject.put("MonitorPointName", paramMap.get("OutPutName"));
                    messageobject.put("MN", paramMap.get("MN"));
                    messageobject.put("DataType", paramMap.get("DataType"));
                    messageobject.put("OnlineStatus", paramMap.get("OnlineStatus"));
                    messageobject.put("MQMessage", paramMap.get("MQMessage"));
                    messageobject.put("PollutionID", paramMap.get("PollutionID"));
                    messageobject.put("MonitorPointId", paramMap.get("MonitorPointId"));
                    //推送到首页
                    if (userids != null && userids.size() > 0) {
                        alarmTaskDisposeController.pushAlarmTaskToPageHome(userids);
                    }
                }
                return listmessage;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getAlarmFirstTime(String firsttime, String firstalarmdatetime) throws ParseException {
        String timestr = "";
        if (!"".equals(firsttime)) {
            if (!"".equals(firstalarmdatetime)) {
                //时间都不为空 比较时间 获取最早报警时间
                if (compare(firstalarmdatetime, firsttime)) {
                    timestr = firstalarmdatetime;
                } else {
                    timestr = firsttime;
                }
            } else {
                timestr = firsttime;
            }
        } else {
            if (!"".equals(firstalarmdatetime)) {
                timestr = firstalarmdatetime;
            }
        }
        return timestr;
    }

    //字符串写出到文本
    public static void writeAlarmTxt(Object paramsjson) throws Exception {
        FileWriter fw = null;
        String rootpath = "D:/";
        String fileName = DataFormatUtil.getDateYMD(new Date()) + "alarmdata.txt";
        File file = new File(rootpath + fileName);
        String content = paramsjson.toString() + ",";
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(fw);
            // FileOutputStream fos = new FileOutputStream(f);
            // OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2020/06/10 0010 上午 11:47
     * @Description: 判断redis中是否存在任务信息，并根据判断进行保存操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void addTaskInfoForAlarmData(Map<String, Object> param, String alarmtaskrediskey, long unixTimeInMillis, JSONObject messageobject, JSONArray jsonObject2, Integer tasktypecode) throws Exception {
        try {
            String daytime = param.get("daytime").toString();
            String day = daytime.substring(0, 10);
            String exceptionfirsttime = param.get("exceptionfirsttime") != null ? param.get("exceptionfirsttime").toString() : "";
            day = day.replaceAll("-", "");
            String datatype = param.get("datatype") != null ? param.get("datatype").toString() : "";
            String dgimn = param.get("dgimn") != null ? param.get("dgimn").toString() : "";
            String monitorpointid = param.get("monitorpointid").toString();
            String monitorpointtypecode = param.get("monitorpointtypecode").toString();
            Boolean iscontinueflag = null;
            List<String> pollutantcodes = new ArrayList<>();
            if (tasktypecode == AlarmTaskEnum.getCode()) {
                iscontinueflag = Boolean.parseBoolean(param.get("iscontinueover").toString());
                pollutantcodes = (List<String>) param.get("overcodes");
            } else if (tasktypecode == DevOpsTaskEnum.getCode()) {
                iscontinueflag = Boolean.parseBoolean(param.get("iscontinueexception").toString());
                pollutantcodes = (List<String>) param.get("exceptioncodes");
            } else if (tasktypecode == ChangeAlarmTaskEnum.getCode()) {
                iscontinueflag = Boolean.parseBoolean(param.get("iscontinuechange").toString());
                pollutantcodes = (List<String>) param.get("changecodes");
            }
            List<Map<String, Object>> list2 = jsonObject2;
            List<String> codes = new ArrayList<>();
            List<String> addcode = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> lastmap = new HashMap<>();
            //找到该点位 获取该点位的数据
            if (list2.size() > 0) {
                for (Map<String, Object> map : list2) {
                    String id = map.get("monitorpointid") != null ? map.get("monitorpointid").toString() : "";
                    if (id.equals(monitorpointid)) {
                        lastmap = map;
                        break;
                    }
                }
            }
            if (lastmap != null && lastmap.size() > 0) {//redis 中存在该点位的任务信息时
                if (lastmap.get("taskendtime") != null) {//当结束时间小于当前报警时间时  更新结束时间
                    if (compare(lastmap.get("taskendtime").toString(), daytime)) {
                        //判断推送的报警数据是否持续报警
                        if (iscontinueflag != null && iscontinueflag == true) {
                            //持续异常 比对污染物是否重复
                            codes = lastmap.get("alarmcodes") != null ? new ArrayList((List<String>) lastmap.get("alarmcodes")) : null;
                            if (codes != null) {
                                for (String code : pollutantcodes) {
                                    if (!codes.contains(code)) {
                                        addcode.add(code);
                                        codes.add(code);
                                    }
                                }
                            } else {
                                addcode = pollutantcodes;
                                codes = pollutantcodes;
                            }
                            //遍历  将最新报警结束时间和新增连续报警污染物 存到redis中
                            for (Map<String, Object> map : list2) {
                                if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                    //替换任务的最新结束时间
                                    map.put("taskendtime", daytime);
                                    if (addcode != null && addcode.size() > 0) {
                                        //添加任务污染物信息
                                        alarmTaskDisposeService.addTaskPollutantInfo(addcode, lastmap.get("taskid"), tasktypecode.toString());
                                        map.put("alarmcodes", codes);
                                    }
                                    break;
                                }
                            }
                            //更新点位任务的任务结束时间
                            alarmTaskDisposeService.updateAlarmTaskEndTime(lastmap.get("taskid"), daytime);
                            //保存修改后的redis信息
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        } else if (iscontinueflag != null && iscontinueflag == false) {
                            //非持续报警 添加任务 替换redis里该点位的信息 保留点位最新数据
                            AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                            obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                            obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));//系统时间
                            if (tasktypecode == DevOpsTaskEnum.getCode() && !"".equals(exceptionfirsttime)) {
                                obj.setAlarmstarttime(exceptionfirsttime);//报警开始时间
                            } else {
                                obj.setAlarmstarttime(daytime);//报警开始时间
                            }
                            obj.setTaskendtime(daytime);//报警结束时间
                            obj.setUpdatetime(new Date());
                            obj.setFkmonitorpointtypecode(monitorpointtypecode);
                            obj.setFkPollutionid(monitorpointid);
                            obj.setFkTasktype(tasktypecode.toString());
                            alarmTaskDisposeService.addAlarmTaskInfo(obj, pollutantcodes, messageobject);
                            if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                                for (Map<String, Object> map : list2) {
                                    if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                        map.put("alarmcodes", pollutantcodes);
                                        map.put("taskid", messageobject.get("TaskID"));
                                        //map.put("taskcreatetime", obj.getTaskcreatetime());
                                        if (tasktypecode == DevOpsTaskEnum.getCode() && !"".equals(exceptionfirsttime)) {
                                            map.put("taskstarttime", exceptionfirsttime);
                                        } else {
                                            map.put("taskstarttime", daytime);
                                        }
                                        map.put("taskendtime", daytime);
                                        break;
                                    }
                                }
                                //确认任务添加成功后 自动分配任务
                                RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                            }
                        }
                    }
                }
            } else {  //不存在该点位的任务信息时  判断其是否连续超标 是连续则获取该点最新的 任务数据并保存到redis中  直接添加任务 并保存任务信息到redis中
                if (iscontinueflag != null && iscontinueflag == true) {
                    //连续报警 且redis中无该点位数据 根据用户ID获取最新的点位任务数据并存到redis中
                    paramMap.put("monitorpointid", monitorpointid);
                    paramMap.put("monitorpointtypecode", monitorpointtypecode);
                    paramMap.put("tasktype", tasktypecode);
                    paramMap.put("taskendtime", daytime);
                    paramMap.put("dgimn", dgimn);
                    paramMap.put("datatype", datatype);
                    paramMap.put("overcodes", pollutantcodes);
                    //获取点位最新的一条任务信息  并设置新的报警结束时间
                    Map<String, Object> lasttaskinfo = alarmTaskDisposeService.getPointLastTaskInfoByParamMap(paramMap);
                    if (lasttaskinfo != null) {
                        //判断当前数据报警时间是否大于最新任务报警结束时间
                        if (lasttaskinfo.get("taskendtime") != null) {
                            if (compare(lasttaskinfo.get("taskendtime").toString(), daytime)) {
                                //替换任务的最新结束时间
                                lasttaskinfo.remove("overlevelcode");
                                lasttaskinfo.put("taskendtime", daytime);
                                list2.add(lasttaskinfo);//redis添加任务信息
                                //确认任务添加成功后 自动分配任务
                                RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                            }
                        }
                    } else {
                        AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                        obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                        obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                        if (tasktypecode == DevOpsTaskEnum.getCode() && !"".equals(exceptionfirsttime)) {
                            obj.setAlarmstarttime(exceptionfirsttime);
                        } else {
                            obj.setAlarmstarttime(daytime);
                        }
                        obj.setTaskendtime(daytime);
                        obj.setUpdatetime(new Date());
                        obj.setFkmonitorpointtypecode(monitorpointtypecode);
                        obj.setFkPollutionid(monitorpointid);
                        obj.setFkTasktype(tasktypecode.toString());
                        alarmTaskDisposeService.addAlarmTaskInfo(obj, pollutantcodes, messageobject);
                        if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                            Map<String, Object> onemap = new HashMap<>();
                            onemap.put("monitorpointid", monitorpointid);
                            onemap.put("taskid", messageobject.get("TaskID"));
                            //onemap.put("taskcreatetime", obj.getTaskcreatetime());
                            if (tasktypecode == DevOpsTaskEnum.getCode() && !"".equals(exceptionfirsttime)) {
                                onemap.put("taskstarttime", exceptionfirsttime);
                            } else {
                                onemap.put("taskstarttime", daytime);
                            }
                            onemap.put("taskendtime", daytime);
                            onemap.put("alarmcodes", pollutantcodes);
                            list2.add(onemap);//redis添加任务信息
                            //确认任务添加成功后 自动分配任务
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        }
                    }
                } else {
                    //非连续报警 且redis中无该点位的 添加任务
                    AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                    obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                    obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    if (tasktypecode == DevOpsTaskEnum.getCode() && !"".equals(exceptionfirsttime)) {
                        obj.setAlarmstarttime(exceptionfirsttime);
                    } else {
                        obj.setAlarmstarttime(daytime);
                    }
                    obj.setTaskendtime(daytime);
                    obj.setUpdatetime(new Date());
                    obj.setFkmonitorpointtypecode(monitorpointtypecode);
                    obj.setFkPollutionid(monitorpointid);
                    obj.setFkTasktype(tasktypecode.toString());
                    alarmTaskDisposeService.addAlarmTaskInfo(obj, pollutantcodes, messageobject);
                    if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("monitorpointid", monitorpointid);
                        onemap.put("taskid", messageobject.get("TaskID"));
                        //onemap.put("taskcreatetime", obj.getTaskcreatetime());
                        if (tasktypecode == DevOpsTaskEnum.getCode() && !"".equals(exceptionfirsttime)) {
                            onemap.put("taskstarttime", exceptionfirsttime);
                        } else {
                            onemap.put("taskstarttime", daytime);
                        }
                        onemap.put("taskendtime", daytime);
                        onemap.put("alarmcodes", pollutantcodes);
                        list2.add(onemap);//redis添加任务信息
                        //确认任务添加成功后 自动分配任务
                        RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/06/10 0010 上午 11:47
     * @Description: 判断redis中是否存在报警任务信息，并根据判断进行保存操作(实时、分钟报警数据判断)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void addAlarmTaskInfoForAlarmData(Map<String, Object> param, String alarmtaskrediskey, long unixTimeInMillis, JSONObject messageobject, JSONArray jsonObject2, Object overlevelcode, Map<String, Integer> overlevelmap) throws Exception {
        try {
            //存在key返回true 不存在返回false
            //1.不存在key  则查询当日报警任务信息 并组装数据保存到redis中
            //2.存在key 则判断推送的报警信息是否包含在value中，包含则不处理 不包含则保存到redis中 且新增报警任务
            //boolean isPut = RedisTemplateUtil.putCacheNXWithExpireAtTime(alarmtaskrediskey, jsonObject, unixTimeInMillis);
            String daytime = param.get("daytime").toString();
            String day = daytime.substring(0, 10);
            String datatype = param.get("datatype") != null ? param.get("datatype").toString() : "";
            String dgimn = param.get("dgimn") != null ? param.get("dgimn").toString() : "";
            day = day.replaceAll("-", "");
            String monitorpointid = param.get("monitorpointid").toString();
            Boolean iscontinueover = Boolean.parseBoolean(param.get("iscontinueover").toString());
            String monitorpointtypecode = param.get("monitorpointtypecode").toString();
            List<String> overcodes = (List<String>) param.get("overcodes");
            List<Map<String, Object>> list2 = new ArrayList<>();
            List<String> codes = new ArrayList<>();
            List<String> addcode = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //判断redis中有无该点位的任务数据
            list2 = jsonObject2;
            Map<String, Object> lastmap = new HashMap<>();
            //通过监测点ID 找到该点位 获取该点位的数据
            if (list2.size() > 0) {
                for (Map<String, Object> map : list2) {
                    String id = map.get("monitorpointid") != null ? map.get("monitorpointid").toString() : "";
                    if (id.equals(monitorpointid)) {
                        lastmap = map;
                        break;
                    }
                }
            }
            if (lastmap != null && lastmap.size() > 0) {//redis 中存在该点位的任务信息时
                if (lastmap.get("taskendtime") != null) {//当结束时间小于当前报警时间时  更新结束时间
                    //boolean dyxy = compare(lastmap.get("taskendtime").toString(), daytime);
                    if (compare(lastmap.get("taskendtime").toString(), daytime)) {
                        //判断推送的报警数据是否持续报警
                        if (iscontinueover != null && iscontinueover == true) {
                            //判断该报警数据的报警级别是否大于已生成的报警任务的报警级别
                            //若大于 则新生成报警任务
                            //若小于 则不生成任务 只更新报警时间和报警污染物
                            boolean sc_or_gx = false;
                            String redislevel = lastmap.get("overlevelcode") != null ? lastmap.get("overlevelcode").toString() : "";
                            if (!"".equals(overlevelcode) && !"".equals(redislevel)) {
                                //比较超标级别
                                if (overlevelmap.get(redislevel) != null && overlevelmap.get(overlevelcode) != null) {//比较时间
                                    if (Integer.parseInt(overlevelmap.get(overlevelcode).toString()) > Integer.parseInt(overlevelmap.get(redislevel).toString())) {
                                        //若最新报警数据的报警级别大于redis中的该点位最新任务的报警级别 则新增
                                        sc_or_gx = true;
                                    }
                                }
                            }
                            if (sc_or_gx) {
                                //报警数据的报警级别大于已有任务的报警级别 新增任务
                                AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                                obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                                obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                                obj.setTaskendtime(daytime);
                                obj.setAlarmstarttime(daytime);//报警开始时间
                                obj.setUpdatetime(new Date());
                                obj.setFkmonitorpointtypecode(monitorpointtypecode);
                                obj.setFkPollutionid(monitorpointid);
                                obj.setOverlevelcode(Integer.parseInt(overlevelcode.toString()) + "");
                                obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                                //新增任务
                                alarmTaskDisposeService.addAlarmTaskInfo(obj, overcodes, messageobject);
                                if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                                    for (Map<String, Object> map : list2) {
                                        if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                            map.put("alarmcodes", overcodes);
                                            map.put("taskid", messageobject.get("TaskID"));
                                            map.put("taskstarttime", daytime);
                                            map.put("taskendtime", daytime);
                                            map.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                                            break;
                                        }
                                    }
                                    //发送短信并保存到短信记录表
                                    // sendMessageAndSaveMessageRecord();
                                }
                            } else {//更新报警污染物 及报警时间
                                //持续报警 比对污染物是否重复
                                codes = lastmap.get("alarmcodes") != null ? new ArrayList((List<String>) lastmap.get("alarmcodes")) : null;
                                if (codes != null) {
                                    for (String code : overcodes) {
                                        if (!codes.contains(code)) {
                                            addcode.add(code);
                                            codes.add(code);
                                        }
                                    }
                                } else {
                                    addcode = overcodes;
                                    codes = overcodes;
                                }
                                //遍历  将最新报警结束时间和新增连续报警污染物 存到redis中
                                for (Map<String, Object> map : list2) {
                                    if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                        //替换任务的最新结束时间
                                        map.put("taskendtime", daytime);
                                        if (addcode != null && addcode.size() > 0) {
                                            //添加任务污染物信息
                                            alarmTaskDisposeService.addTaskPollutantInfo(addcode, lastmap.get("taskid"), AlarmTaskEnum.getCode().toString());
                                            map.put("alarmcodes", codes);
                                        }
                                        break;
                                    }
                                }
                                //更新点位任务的任务结束时间
                                alarmTaskDisposeService.updateAlarmTaskEndTime(lastmap.get("taskid"), daytime);
                            }
                            //保存修改后的redis信息
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        } else if (iscontinueover != null && iscontinueover == false) {
                            //非持续报警 添加任务 替换redis里该点位的信息 保留点位最新数据
                            AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                            obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                            obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                            obj.setTaskendtime(daytime);
                            obj.setAlarmstarttime(daytime);//报警开始时间
                            obj.setUpdatetime(new Date());
                            obj.setFkmonitorpointtypecode(monitorpointtypecode);
                            obj.setOverlevelcode(Integer.parseInt(overlevelcode.toString()) + "");
                            obj.setFkPollutionid(monitorpointid);
                            obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                            //判断报警时间是否大于redis中该点位最新的报警结束时间
                            alarmTaskDisposeService.addAlarmTaskInfo(obj, overcodes, messageobject);
                            if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                                for (Map<String, Object> map : list2) {
                                    if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                        map.put("alarmcodes", overcodes);
                                        map.put("taskid", messageobject.get("TaskID"));
                                        map.put("taskstarttime", daytime);
                                        map.put("taskendtime", daytime);
                                        map.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                                        break;
                                    }
                                }
                                //发送短信并保存到短信记录表
                                //sendMessageAndSaveMessageRecord();
                                //确认任务添加成功后 自动分配任务
                                RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                            }
                        }
                    }
                }
            } else {  //不存在该点位的任务信息时  判断其是否连续超标 是连续则获取该点最新的 任务数据并保存到redis中  直接添加任务 并保存任务信息到redis中
                if (iscontinueover != null && iscontinueover == true) {
                    //连续报警 且redis中无该点位数据 根据用户ID获取最新的点位任务数据并存到redis中
                    paramMap.put("monitorpointid", monitorpointid);
                    paramMap.put("monitorpointtypecode", monitorpointtypecode);
                    paramMap.put("tasktype", AlarmTaskEnum.getCode());
                    paramMap.put("taskendtime", daytime);
                    paramMap.put("dgimn", dgimn);
                    paramMap.put("datatype", datatype);
                    paramMap.put("overcodes", overcodes);
                    paramMap.put("overlevelmap", overlevelmap);
                    //获取点位最新的一条任务信息  并判断其是否连续报警  并设置新的报警结束时间
                    Map<String, Object> lasttaskinfo = alarmTaskDisposeService.getPointLastTaskInfoByParamMap(paramMap);
                    if (lasttaskinfo != null) {//有该点位最新任务数据
                        //判断当前数据报警时间是否大于最新任务报警结束时间
                        if (lasttaskinfo.get("taskendtime") != null) {
                            if (compare(lasttaskinfo.get("taskendtime").toString(), daytime)) {
                                //当结束时间小于当前报警时间时  更新结束时间
                                //替换任务的最新结束时间
                                lasttaskinfo.put("taskendtime", daytime);
                                list2.add(lasttaskinfo);//redis添加任务信息
                                //确认任务添加成功后 自动分配任务
                                RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                            }
                        }
                    } else {//若没有最新的点位任务数据  则添加任务
                        AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                        obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                        obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                        obj.setTaskendtime(daytime);
                        obj.setAlarmstarttime(daytime);//报警开始时间
                        obj.setUpdatetime(new Date());
                        obj.setFkmonitorpointtypecode(monitorpointtypecode);
                        obj.setOverlevelcode(Integer.parseInt(overlevelcode.toString()) + "");
                        obj.setFkPollutionid(monitorpointid);
                        obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                        alarmTaskDisposeService.addAlarmTaskInfo(obj, overcodes, messageobject);
                        if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                            Map<String, Object> onemap = new HashMap<>();
                            onemap.put("monitorpointid", monitorpointid);
                            onemap.put("taskid", messageobject.get("TaskID"));
                            onemap.put("taskstarttime", daytime);
                            onemap.put("taskendtime", daytime);
                            onemap.put("alarmcodes", overcodes);
                            onemap.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                            list2.add(onemap);//redis添加任务信息
                            //发送短信并保存到短信记录表
                            //sendMessageAndSaveMessageRecord();
                            //确认任务添加成功后 自动分配任务
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        }
                    }
                } else {
                    //非连续报警 且redis中无该点位的 添加任务
                    AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                    obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                    obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    obj.setTaskendtime(daytime);
                    obj.setAlarmstarttime(daytime);//报警开始时间
                    obj.setUpdatetime(new Date());
                    obj.setFkmonitorpointtypecode(monitorpointtypecode);
                    obj.setOverlevelcode(overlevelcode + "");
                    obj.setFkPollutionid(monitorpointid);
                    obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                    alarmTaskDisposeService.addAlarmTaskInfo(obj, overcodes, messageobject);
                    if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("monitorpointid", monitorpointid);
                        onemap.put("taskid", messageobject.get("TaskID"));
                        onemap.put("taskstarttime", daytime);
                        onemap.put("taskendtime", daytime);
                        onemap.put("alarmcodes", overcodes);
                        onemap.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                        list2.add(onemap);//redis添加任务信息
                        //发送短信并保存到短信记录表
                        //sendMessageAndSaveMessageRecord();
                        //确认任务添加成功后 自动分配任务
                        RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/06/10 0010 上午 11:47
     * @Description: 判断redis中是否存在运维任务信息，并根据判断进行保存操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void addDevOpsTaskInfoForDevOpsData(Map<String, Object> param, String alarmtaskrediskey, long unixTimeInMillis, JSONObject messageobject, JSONArray jsonObject2, Integer tasktypecode) throws Exception {
        try {
            String daytime = param.get("daytime").toString();
            String recoverytime = param.get("recoverytime") != null ? param.get("recoverytime").toString() : "";
            String day = daytime.substring(0, 10);
            String exceptionfirsttime = param.get("exceptionfirsttime") != null ? param.get("exceptionfirsttime").toString() : "";
            day = day.replaceAll("-", "");
            String datatype = param.get("datatype") != null ? param.get("datatype").toString() : "";
            String dgimn = param.get("dgimn") != null ? param.get("dgimn").toString() : "";
            String monitorpointid = param.get("monitorpointid").toString();
            String monitorpointtypecode = param.get("monitorpointtypecode").toString();
            Boolean iscontinueflag = null;
            List<String> pollutantcodes = new ArrayList<>();
            iscontinueflag = Boolean.parseBoolean(param.get("iscontinueexception").toString());
            pollutantcodes = (List<String>) param.get("exceptioncodes");
            List<Map<String, Object>> list2 = jsonObject2;
            List<String> codes = new ArrayList<>();
            List<String> addcode = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> lastmap = new HashMap<>();
            //找到该点位 获取该点位的数据
            if (list2.size() > 0) {
                for (Map<String, Object> map : list2) {
                    String id = map.get("monitorpointid") != null ? map.get("monitorpointid").toString() : "";
                    if (id.equals(monitorpointid)) {
                        lastmap = map;
                        break;
                    }
                }
            }
            if (lastmap != null && lastmap.size() > 0) {//redis 中存在该点位的任务信息时
                //当恢复时间不为空时
                //结束当前任务 更新报警结束时间 和  恢复时间
                if (!"".equals(recoverytime)) {
                    //遍历  将最新报警结束时间和恢复时间 存到redis中
                    for (Map<String, Object> map : list2) {
                        if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                            //替换任务的最新结束时间
                            map.put("taskendtime", daytime);
                            //替换任务的恢复时间
                            map.put("recoverytime", recoverytime);
                            break;
                        }
                    }
                    //更新点位任务的任务结束时间
                    AlarmTaskDisposeManagementVO obj = alarmTaskDisposeService.selectByPrimaryKey(lastmap.get("taskid").toString());
                    if (obj != null) {
                        obj.setRecoverystatus((short) 1);
                        obj.setTaskendtime(daytime);
                        obj.setRecoverytime(DataFormatUtil.getDateYMDHMS(recoverytime));
                        obj.setUpdatetime(new Date());
                        alarmTaskDisposeService.updateByPrimaryKey(obj);
                    }
                } else {
                    //当恢复时间为空时
                    //若为连续报警
                    if (iscontinueflag != null && iscontinueflag == true) {
                        //持续异常 比对污染物是否重复
                        codes = lastmap.get("alarmcodes") != null ? new ArrayList((List<String>) lastmap.get("alarmcodes")) : null;
                        if (codes != null) {
                            for (String code : pollutantcodes) {
                                if (!codes.contains(code)) {
                                    addcode.add(code);
                                    codes.add(code);
                                }
                            }
                        } else {
                            addcode = pollutantcodes;
                            codes = pollutantcodes;
                        }
                        //遍历  将最新报警结束时间和新增连续报警污染物 存到redis中
                        for (Map<String, Object> map : list2) {
                            if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                //替换任务的最新结束时间
                                map.put("taskendtime", daytime);
                                if (addcode != null && addcode.size() > 0) {
                                    //添加任务污染物信息
                                    alarmTaskDisposeService.addTaskPollutantInfo(addcode, lastmap.get("taskid"), tasktypecode.toString());
                                    map.put("alarmcodes", codes);
                                }
                                break;
                            }
                        }
                        //更新点位任务的任务结束时间
                        AlarmTaskDisposeManagementVO obj = alarmTaskDisposeService.selectByPrimaryKey(lastmap.get("taskid").toString());
                        if (obj != null) {
                            //置空恢复状态和时间
                            obj.setRecoverytime(null);
                            obj.setRecoverystatus((short) 0);
                            obj.setTaskendtime(daytime);
                            obj.setUpdatetime(new Date());
                            alarmTaskDisposeService.updateByPrimaryKey(obj);
                        }
                        //保存修改后的redis信息
                        RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                    } else {
                        //若不是连续报警 且没有恢复时间  则新增任务 且同步更新到redis中
                        //非连续报警 且redis中无该点位的 添加任务
                        AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                        obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                        obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                        obj.setAlarmstarttime(exceptionfirsttime);//报警开始时间
                        obj.setTaskendtime(daytime);
                        obj.setUpdatetime(new Date());
                        obj.setFkmonitorpointtypecode(monitorpointtypecode);
                        obj.setFkPollutionid(monitorpointid);
                        obj.setFkTasktype(DevOpsTaskEnum.getCode().toString());
                        if (pollutantcodes != null && pollutantcodes.size() > 0) {//有污染物才生成任务
                            alarmTaskDisposeService.addAlarmTaskInfo(obj, pollutantcodes, messageobject);
                        }
                        if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                            for (Map<String, Object> map : list2) {
                                if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                    map.put("alarmcodes", pollutantcodes);
                                    map.put("taskid", messageobject.get("TaskID"));
                                    //map.put("taskcreatetime", obj.getTaskcreatetime());
                                    map.put("taskstarttime", exceptionfirsttime);
                                    map.put("taskendtime", daytime);
                                    break;
                                }
                            }
                            //确认任务添加成功后 自动分配任务
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        }
                    }
                }
            } else {  //不存在该点位的任务信息时  判断其是否连续超标 是连续则获取该点最新的 任务数据并保存到redis中 不连续 则直接添加任务 并保存任务信息到redis中
                //当为连续时
                if (iscontinueflag != null && iscontinueflag == true) {
                    //连续报警 且redis中无该点位数据 根据用户ID获取最新的点位任务数据并存到redis中
                    paramMap.put("monitorpointid", monitorpointid);
                    paramMap.put("monitorpointtypecode", monitorpointtypecode);
                    paramMap.put("tasktype", tasktypecode);
                    paramMap.put("taskendtime", daytime);
                    paramMap.put("dgimn", dgimn);
                    paramMap.put("datatype", datatype);
                    paramMap.put("overcodes", pollutantcodes);
                    //获取点位最新的一条任务信息  并设置新的报警结束时间
                    Map<String, Object> lasttaskinfo = alarmTaskDisposeService.getPointLastTaskInfoByParamMap(paramMap);
                    if (lasttaskinfo != null) {
                        //判断当前数据报警时间是否大于最新任务报警结束时间
                        if (lasttaskinfo.get("taskendtime") != null) {
                            if (compare(lasttaskinfo.get("taskendtime").toString(), daytime)) {
                                //替换任务的最新结束时间
                                lasttaskinfo.remove("overlevelcode");
                                lasttaskinfo.put("taskendtime", daytime);
                                list2.add(lasttaskinfo);//redis添加任务信息
                                //确认任务添加成功后 自动分配任务
                                RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                            }
                        }
                    } else {//若没有最新的点位任务数据  则添加任务
                        AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                        obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                        obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                        obj.setAlarmstarttime(exceptionfirsttime);
                        obj.setTaskendtime(daytime);
                        obj.setUpdatetime(new Date());
                        obj.setFkmonitorpointtypecode(monitorpointtypecode);
                        obj.setFkPollutionid(monitorpointid);
                        obj.setFkTasktype(DevOpsTaskEnum.getCode().toString());
                        if (!"".equals(recoverytime)) {//若有恢复时间 且没有该点位最新任务信息
                            obj.setRecoverystatus((short) 1);
                            obj.setRecoverytime(DataFormatUtil.getDateYMDHMS(recoverytime));
                        }
                        if (pollutantcodes != null && pollutantcodes.size() > 0) {//有污染物才生成任务
                            alarmTaskDisposeService.addAlarmTaskInfo(obj, pollutantcodes, messageobject);
                        }
                        if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                            Map<String, Object> onemap = new HashMap<>();
                            onemap.put("monitorpointid", monitorpointid);
                            onemap.put("taskid", messageobject.get("TaskID"));
                            onemap.put("taskstarttime", exceptionfirsttime);
                            onemap.put("taskendtime", daytime);
                            onemap.put("alarmcodes", pollutantcodes);
                            list2.add(onemap);//redis添加任务信息
                            //确认任务添加成功后 自动分配任务
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        }
                    }
                } else {
                    //非连续报警 且redis中无该点位的 添加任务
                    AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                    obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                    obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    obj.setAlarmstarttime(exceptionfirsttime);
                    obj.setTaskendtime(daytime);
                    obj.setUpdatetime(new Date());
                    obj.setFkmonitorpointtypecode(monitorpointtypecode);
                    obj.setFkPollutionid(monitorpointid);
                    obj.setFkTasktype(DevOpsTaskEnum.getCode().toString());
                    if (pollutantcodes != null && pollutantcodes.size() > 0) {//有污染物才生成任务
                        alarmTaskDisposeService.addAlarmTaskInfo(obj, pollutantcodes, messageobject);
                    }
                    if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("monitorpointid", monitorpointid);
                        onemap.put("taskid", messageobject.get("TaskID"));
                        //onemap.put("taskcreatetime", obj.getTaskcreatetime());
                        onemap.put("taskstarttime", exceptionfirsttime);
                        onemap.put("taskendtime", daytime);
                        onemap.put("alarmcodes", pollutantcodes);
                        list2.add(onemap);//redis添加任务信息
                        //确认任务添加成功后 自动分配任务
                        RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/04/25 0025 下午 1:08
     * @Description: 获取已连接的会话ID集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getOnlineUsrrIds() {
        Object object = authSystemMicroService.getAllClientInfo();
        List<String> sessionIds = new ArrayList<>();
        List<String> userids = new ArrayList<>();
        String sessionId;
        String userid;
        List<Map<String, Object>> clients = (List<Map<String, Object>>) object;
        for (Map<String, Object> client : clients) {
            sessionId = client.get("sessionId").toString();
            if (!sessionIds.contains(sessionId)) {
                userid = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                if (StringUtils.isNotBlank(userid)) {
                    userids.add(userid);
                    sessionIds.add(sessionId);
                }
            }
        }
        return userids;
    }

    /**
     * @author: chengzq
     * @date: 2019/9/3 0003 下午 7:58
     * @Description: 获取当日23:59:59时间戳
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private Long getUnixTimeInMillis() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar instance = Calendar.getInstance();
        String time = instance.get(Calendar.YEAR) + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(Calendar.DATE) + " 23:59:59";
        long unixTimeInMillis = format.parse(time).getTime();
        return unixTimeInMillis;
    }

    /**
     * @author: xsm
     * @date: 2021/03/15 0015 上午 9:45
     * @Description: 报警任务短信推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendAlarmTaskMessageForTaskCreate(Map<String, Object> formdata) {
        List<String> userIds = (List<String>) formdata.get("userids");
        if (userIds.size() > 0) {
            String monitorpointname = formdata.get("monitorpointname").toString();
            String time = DataFormatUtil.getDateYMDHM(new Date());
            time = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm", "H点mm分");
            String context = "已于" + time + "生成" + monitorpointname + "的报警任务，" + "请及时进行任务分派。";
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
            //发送短信消息
            socketJson = new JSONObject();
            socketJson.put("userids", userIds);
            socketJson.put("time", time);
            socketJson.put("mointorpointname", monitorpointname);
            socketJson.put("tasktype", "报警处置工单");
            authSystemMicroService.sendTaskSMSToClient(socketJson);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/02 0002 下午 2:49
     * @Description: 根据推送的队列数据新建报警任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addNewAlarmTaskInfoByMQData", method = RequestMethod.POST)
    public void addNewAlarmTaskInfoByMQData(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        //rediskey
        String alarmtaskrediskey = DataFormatUtil.parseProperties("alarmtaskrediskey");//报警任务
        String overlevelorder = DataFormatUtil.parseProperties("overlevelorder");//报警级别排序
        Long unixTimeInMillis = getUnixTimeInMillis();
        try {
            Map<String, Object> paramMap = JSONObject.fromObject(paramsjson);
            Map<String, Object> param = new HashMap<>();
            List<Map<String, Object>> mqdata = (List<Map<String, Object>>) paramMap.get("MQMessage");
            Object iscontinueover = null;
            //是否连续 超标 异常 突变
            if (paramMap.get("IsContinueOver") != null && !"null".equals(paramMap.get("IsContinueOver").toString())) {
                iscontinueover = Boolean.parseBoolean(paramMap.get("IsContinueOver").toString());
            }
            //报警数据类型
            String datatype = paramMap.get("DataType") != null ? paramMap.get("DataType").toString() : "";
            String starttime = "";//报警开始时间
            String endtime = "";//报警结束时间
            Object overlevelcode = "";//报警级别
            String recoverytime = "";//恢复时间
            String lastsendtime = "";//数据发送时间
            overlevelcode = paramMap.get("AlarmLevel") != null ? paramMap.get("AlarmLevel").toString() : "";
            //判断报警限值序列
            Map<String, Integer> overlevelmap = new HashMap<>();
            if (overlevelorder != null && !"".equals(overlevelorder)) {
                String[] overlevelstr = overlevelorder.split(",");
                int i = overlevelstr.length;
                for (String str : overlevelstr) {
                    overlevelmap.put(str, i);
                    i = i - 1;
                }
            }
            //污染物
            List<String> overcodes = new ArrayList<>();//接收报警数据中的超标污染物
            String monitorpointid = (paramMap.get("MonitorPointId") != null && !"null".equals(paramMap.get("MonitorPointId").toString())) ? paramMap.get("MonitorPointId").toString() : "";
            //巡查组关联ID
            String pointorentid = "";
            if (paramMap.get("PollutionID") != null && !"".equals(paramMap.get("PollutionID").toString())) {
                pointorentid = paramMap.get("PollutionID").toString();
            } else {
                pointorentid = monitorpointid;
            }
            String monitorpointtypecode = (paramMap.get("MonitorPointTypeCode") != null && !"null".equals(paramMap.get("MonitorPointTypeCode").toString())) ? paramMap.get("MonitorPointTypeCode").toString() : "";
            //获取报警开始时间
            if (paramMap.get("StartTime") != null && !"null".equals(paramMap.get("StartTime").toString()) && !"".equals(paramMap.get("StartTime").toString())) {
                starttime = paramMap.get("StartTime").toString();
            }
            //获取报警结束时间
            if (paramMap.get("EndTime") != null && !"null".equals(paramMap.get("EndTime").toString()) && !"".equals(paramMap.get("EndTime").toString())) {
                endtime = paramMap.get("EndTime").toString();
            }
            //获取恢复时间
            if (paramMap.get("RecoveryTime") != null && !"null".equals(paramMap.get("RecoveryTime").toString()) && !"".equals(paramMap.get("RecoveryTime").toString())) {
                recoverytime = paramMap.get("RecoveryTime").toString();
            }
            //获取发送时间
            if (paramMap.get("LastSendTime") != null && !"null".equals(paramMap.get("LastSendTime").toString()) && !"".equals(paramMap.get("LastSendTime").toString())) {
                lastsendtime = paramMap.get("LastSendTime").toString();
            }

            List<JSONObject> listmessage = new ArrayList<>();
            //获取报警污染物
            if (mqdata != null && mqdata.size() > 0) {
                for (Map<String, Object> mqmap : mqdata) {
                    String str = mqmap.get("AlarmType") != null ? mqmap.get("AlarmType").toString() : "";
                    if (CommonTypeEnum.RabbitMQAlarmTypeEnum.OverLimitMessage.getCode().equals(str)
                            || CommonTypeEnum.RabbitMQAlarmTypeEnum.OverStandardMessage.getCode().equals(str)) {
                        if (mqmap.get("PollutantCode") != null && !"".equals(mqmap.get("PollutantCode").toString())) {
                            overcodes.add(mqmap.get("PollutantCode").toString());
                        }
                    }
                }
            }
            //填充参数
            param.put("datatype", datatype);
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("recoverytime", recoverytime);
            param.put("dgimn", paramMap.get("MN"));
            param.put("monitorpointid", monitorpointid);
            param.put("pointorentid", pointorentid);
            param.put("monitorpointtypecode", monitorpointtypecode);
            param.put("overcodes", overcodes);
            param.put("iscontinueover", iscontinueover);
            String day = endtime.substring(0, 10);
            day = day.replaceAll("-", "");
            //环保 报警任务
            if (iscontinueover != null) {//有传连续标记
                JSONObject messageobject = new JSONObject();
                //messageobject.put("PollutionID", paramMap.get("PollutionID"));//用于自动分派
                boolean isPut = RedisTemplateUtil.hasKey(day + "_" + alarmtaskrediskey);
                JSONArray jsonObject2 = new JSONArray();
                if (isPut) {//存在key
                    jsonObject2 = RedisTemplateUtil.getCache(day + "_" + alarmtaskrediskey, JSONArray.class) == null ? new JSONArray() : RedisTemplateUtil.getCache(day + "_" + alarmtaskrediskey, JSONArray.class);
                } else {//不存在key 从库中查询当天最新报警任务
                    param.put("daytime", lastsendtime.substring(0, 10) + " ");
                    param.put("tasktype", AlarmTaskEnum.getCode().toString());
                    List<Map<String, Object>> tasklist = alarmTaskDisposeService.getMonitorPointLastTaskInfoByTaskType(param);
                    jsonObject2 = JSONArray.fromObject(tasklist);
                    param.put("daytime", endtime);
                }
                //判断处理报警数据
                addNewAlarmTaskInfoForAlarmData(param, alarmtaskrediskey, unixTimeInMillis, messageobject, jsonObject2, overlevelcode, overlevelmap);
                if (messageobject.get("TaskID") != null) {
                    listmessage.add(messageobject);
                }
            }

            if (listmessage != null && listmessage.size() > 0) {
                //有生成任务时  获取在线的用户ID
                List<String> userids = getOnlineUsrrIds();
                for (JSONObject messageobject : listmessage) {
                    messageobject.put("MessageType", paramMap.get("MessageType"));
                    messageobject.put("MonitorPointTypeCode", paramMap.get("MonitorPointTypeCode"));
                    messageobject.put("DateTime", !"".equals(endtime) ? endtime.substring(0, 10) : "");
                    messageobject.put("PollutantName", paramMap.get("MessageType"));
                    messageobject.put("PollutionName", paramMap.get("PollutionName"));
                    messageobject.put("MonitorPointName", paramMap.get("OutPutName"));
                    messageobject.put("MN", paramMap.get("MN"));
                    messageobject.put("DataType", paramMap.get("DataType"));
                    messageobject.put("OnlineStatus", paramMap.get("OnlineStatus"));
                    messageobject.put("MQMessage", paramMap.get("MQMessage"));
                    messageobject.put("PollutionID", paramMap.get("PollutionID"));
                    messageobject.put("MonitorPointId", paramMap.get("MonitorPointId"));
                    //推送到首页
                    if (userids != null && userids.size() > 0) {
                        alarmTaskDisposeController.pushAlarmTaskToPageHome(userids);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/07 0007 下午 4:25
     * @Description: 判断redis中是否存在报警任务信息，并根据判断进行保存操作(实时、分钟报警数据判断)（新）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private void addNewAlarmTaskInfoForAlarmData(Map<String, Object> param, String alarmtaskrediskey, long unixTimeInMillis, JSONObject messageobject, JSONArray jsonObject2, Object overlevelcode, Map<String, Integer> overlevelmap) throws Exception {
        try {
            //持续超过30分钟发送任务格式的MQ信息，如果报警恢复后MQ中增加恢复时间，如果持续超标有某个污染物超标级别变高并且超过30分钟发送MQ消息并且持续标记为true
            //存在key返回true 不存在返回false
            //1.不存在key  则查询当日报警任务信息 并组装数据保存到redis中
            //2.存在key 则判断推送的报警信息是否包含在value中，包含则不处理 不包含则保存到redis中 且新增报警任务
            //boolean isPut = RedisTemplateUtil.putCacheNXWithExpireAtTime(alarmtaskrediskey, jsonObject, unixTimeInMillis);
            String daytime = param.get("endtime").toString();
            String day = daytime.substring(0, 10);
            String starttime = param.get("starttime").toString();//开始报警时间
            String endtime = param.get("endtime").toString();//结束时间
            String recoverytime = param.get("recoverytime").toString();//恢复时间
            String dgimn = param.get("dgimn") != null ? param.get("dgimn").toString() : "";
            String datatype = param.get("datatype").toString();//报警数据类型
            day = day.replaceAll("-", "");//去掉日期横杆-
            String monitorpointid = param.get("monitorpointid").toString();
            String pointorentid = param.get("pointorentid").toString();//企业或监测点ID
            Boolean iscontinueover = Boolean.parseBoolean(param.get("iscontinueover").toString());//连续超标标记
            String monitorpointtypecode = param.get("monitorpointtypecode").toString();//监测点类型
            List<String> overcodes = (List<String>) param.get("overcodes");//超标污染物
            List<Map<String, Object>> list2 = new ArrayList<>();
            List<String> codes = new ArrayList<>();
            List<String> addcode = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //redis中有点位任务数据
            list2 = jsonObject2;
            Map<String, Object> lastmap = new HashMap<>();
            //在redis中找到该点位的最新一条任务数据  获取该点位的数据
            if (list2.size() > 0) {
                for (Map<String, Object> map : list2) {
                    String id = map.get("monitorpointid") != null ? map.get("monitorpointid").toString() : "";
                    if (id.equals(monitorpointid)) {
                        lastmap = map;
                        break;
                    }
                }
            }
            if (lastmap != null && lastmap.size() > 0) {//redis 中存在该点位的任务信息时
                //当恢复时间不为空时
                //结束当前任务 更新报警结束时间 和  恢复时间
                if (!"".equals(recoverytime)) {
                    //遍历  将最新报警结束时间和恢复时间 存到redis中
                    for (Map<String, Object> map : list2) {
                        if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                            //替换任务的最新结束时间
                            map.put("taskendtime", endtime);
                            //替换任务的恢复时间
                            map.put("recoverytime", recoverytime);
                            break;
                        }
                    }
                    //更新点位任务的任务结束时间
                    AlarmTaskDisposeManagementVO obj = alarmTaskDisposeService.selectByPrimaryKey(lastmap.get("taskid").toString());
                    if (obj != null) {
                        obj.setRecoverystatus((short) 1);
                        obj.setTaskendtime(endtime);
                        obj.setRecoverytime(DataFormatUtil.getDateYMDHMS(recoverytime));
                        obj.setUpdatetime(new Date());
                        alarmTaskDisposeService.updateByPrimaryKey(obj);
                    }
                } else {
                    //当恢复时间为空时
                    //若为连续报警
                    //比较报警级别
                    //更新报警污染物和报警结束时间 若当前报警级别 > 任务报警级别 则报警级别也需要更新
                    if (iscontinueover != null && iscontinueover == true) {
                        //比对污染物是否重复
                        codes = lastmap.get("alarmcodes") != null ? new ArrayList((List<String>) lastmap.get("alarmcodes")) : null;
                        if (codes != null) {
                            for (String code : overcodes) {
                                if (!codes.contains(code)) {
                                    addcode.add(code);
                                    codes.add(code);
                                }
                            }
                        } else {
                            addcode = overcodes;
                            codes = overcodes;
                        }
                        //遍历  将最新报警结束时间和报警级别 报警污染物 存到redis中
                        for (Map<String, Object> map : list2) {
                            if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                //替换任务的最新结束时间
                                map.put("taskendtime", endtime);
                                //判断超标级别 比较级别 当任务报警数据的报警级别 大于 已存在的任务的 报警级别
                                if (Integer.parseInt(overlevelmap.get(overlevelcode).toString()) > Integer.parseInt(overlevelmap.get(lastmap.get("overlevelcode").toString()).toString())) {
                                    map.put("overlevelcode", overlevelcode + "");
                                }
                                if (addcode != null && addcode.size() > 0) {
                                    //添加任务污染物信息
                                    alarmTaskDisposeService.addTaskPollutantInfo(addcode, lastmap.get("taskid"), AlarmTaskEnum.getCode().toString());
                                    map.put("alarmcodes", codes);
                                }
                                break;
                            }
                        }
                        //更新点位任务的任务结束时间
                        AlarmTaskDisposeManagementVO obj = alarmTaskDisposeService.selectByPrimaryKey(lastmap.get("taskid").toString());
                        if (obj != null) {
                            //置空恢复状态和时间
                            obj.setRecoverytime(null);
                            obj.setRecoverystatus((short) 0);
                            obj.setTaskendtime(endtime);
                            //判断超标级别 比较级别 当任务报警数据的报警级别 大于 已存在的任务的 报警级别
                            if (Integer.parseInt(overlevelmap.get(overlevelcode).toString()) > Integer.parseInt(overlevelmap.get(lastmap.get("overlevelcode").toString()).toString())) {
                                obj.setOverlevelcode(overlevelcode + "");
                            }
                            obj.setUpdatetime(new Date());
                            alarmTaskDisposeService.updateByPrimaryKey(obj);
                        }
                        //保存修改后的redis信息
                        RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                    } else {
                        //若不是连续报警 且没有恢复时间  则新增任务 且同步更新到redis中
                        //非连续报警 且redis中无该点位的 添加任务
                        AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                        obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                        obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                        obj.setAlarmstarttime(starttime);//报警开始时间
                        obj.setTaskendtime(endtime);
                        obj.setUpdatetime(new Date());
                        obj.setFkmonitorpointtypecode(monitorpointtypecode);
                        obj.setOverlevelcode(overlevelcode + "");
                        obj.setFkPollutionid(monitorpointid);
                        obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                        if (overcodes != null && overcodes.size() > 0) {//有超标污染物才生成任务
                            alarmTaskDisposeService.addNewAlarmTaskInfo(obj, overcodes, messageobject);
                        }
                        if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                            for (Map<String, Object> map : list2) {
                                if (map.get("monitorpointid") != null && monitorpointid.equals(map.get("monitorpointid").toString())) {
                                    map.put("alarmcodes", overcodes);
                                    map.put("taskid", messageobject.get("TaskID"));
                                    map.put("taskstarttime", starttime);
                                    map.put("taskendtime", endtime);
                                    map.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                                    break;
                                }
                            }
                            //自动分配 且发送短信并保存到短信记录表
                            if (!"".equals(pointorentid)) {
                                sendNewAlarmTaskMessage(AlarmTaskEnum.getCode(), messageobject.get("TaskID").toString(), pointorentid, monitorpointtypecode);
                            }
                            //确认任务添加成功后 更新redis
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        }
                    }
                }
            } else {  //不存在该点位的任务信息时  判断其是否连续超标 是连续则获取该点最新的 任务数据并保存到redis中 不连续 则直接添加任务 并保存任务信息到redis中
                //当为连续时
                if (iscontinueover != null && iscontinueover == true) {
                    //连续报警 且redis中无该点位数据 根据用户ID获取最新的点位任务数据并存到redis中
                    paramMap.put("monitorpointid", monitorpointid);
                    paramMap.put("monitorpointtypecode", monitorpointtypecode);
                    paramMap.put("tasktype", AlarmTaskEnum.getCode());
                    paramMap.put("taskendtime", endtime);
                    paramMap.put("taskstarttime", starttime);
                    paramMap.put("dgimn", dgimn);
                    paramMap.put("datatype", datatype);
                    paramMap.put("overcodes", overcodes);
                    paramMap.put("overlevelcode", overlevelcode);
                    paramMap.put("overlevelmap", overlevelmap);
                    paramMap.put("recoverytime", recoverytime);
                    //获取点位最新的一条任务信息  并判断其是否连续报警  并设置新的报警结束时间
                    Map<String, Object> lasttaskinfo = alarmTaskDisposeService.getNewPointLastTaskInfoByParamMap(paramMap);
                    if (lasttaskinfo != null) {//有该点位最新任务数据
                        //判断当前数据报警开始时间是否大于最新任务报警结束时间
                        if (lasttaskinfo.get("taskendtime") != null) {
                            if (compare(lasttaskinfo.get("taskendtime").toString(), endtime)) {
                                //当结束时间小于当前报警时间时  更新结束时间
                                //替换任务的最新结束时间
                                lasttaskinfo.put("taskendtime", endtime);
                                list2.add(lasttaskinfo);//redis添加任务信息
                                //确认任务添加成功后 自动分配任务
                                RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                            }
                        }
                    } else {//若没有最新的点位任务数据  则添加任务
                        AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                        obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                        obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                        obj.setTaskendtime(endtime);
                        obj.setAlarmstarttime(starttime);//报警开始时间
                        obj.setUpdatetime(new Date());
                        obj.setFkmonitorpointtypecode(monitorpointtypecode);
                        obj.setOverlevelcode(overlevelcode + "");
                        obj.setFkPollutionid(monitorpointid);
                        obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                        if (!"".equals(recoverytime)) {//若有恢复时间 且没有该点位最新任务信息
                            obj.setRecoverystatus((short) 1);
                            obj.setRecoverytime(DataFormatUtil.getDateYMDHMS(recoverytime));
                        }
                        if (overcodes != null && overcodes.size() > 0) {//有超标污染物才生成任务
                            alarmTaskDisposeService.addNewAlarmTaskInfo(obj, overcodes, messageobject);
                        }
                        if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                            Map<String, Object> onemap = new HashMap<>();
                            onemap.put("monitorpointid", monitorpointid);
                            onemap.put("taskid", messageobject.get("TaskID"));
                            onemap.put("taskstarttime", starttime);
                            onemap.put("taskendtime", endtime);
                            onemap.put("alarmcodes", overcodes);
                            onemap.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                            list2.add(onemap);//redis添加任务信息
                            //自动分配 且发送短信并保存到短信记录表
                            if (!"".equals(pointorentid)) {
                                sendNewAlarmTaskMessage(AlarmTaskEnum.getCode(), messageobject.get("TaskID").toString(), pointorentid, monitorpointtypecode);
                            }
                            //确认任务添加成功后 自动分配任务
                            RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                        }
                    }
                } else {
                    //非连续报警 且redis中无该点位的 添加任务
                    AlarmTaskDisposeManagementVO obj = new AlarmTaskDisposeManagementVO();
                    obj.setTaskstatus(CommonTypeEnum.AlarmTaskDisposalScheduleEnum.UnassignedTaskEnum.getCode());
                    obj.setTaskcreatetime(DataFormatUtil.getDateYMDHMS(new Date()));
                    obj.setTaskendtime(endtime);
                    obj.setAlarmstarttime(starttime);//报警开始时间
                    obj.setUpdatetime(new Date());
                    obj.setFkmonitorpointtypecode(monitorpointtypecode);
                    obj.setOverlevelcode(overlevelcode + "");
                    obj.setFkPollutionid(monitorpointid);
                    obj.setFkTasktype(AlarmTaskEnum.getCode().toString());
                    if (overcodes != null && overcodes.size() > 0) {//有超标污染物才生成任务
                        alarmTaskDisposeService.addNewAlarmTaskInfo(obj, overcodes, messageobject);
                    }
                    if (messageobject.get("TaskID") != null) {//确认任务添加成功  更新redis
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("monitorpointid", monitorpointid);
                        onemap.put("taskid", messageobject.get("TaskID"));
                        onemap.put("taskstarttime", starttime);
                        onemap.put("taskendtime", endtime);
                        onemap.put("alarmcodes", overcodes);
                        onemap.put("overlevelcode", Integer.parseInt(overlevelcode.toString()) + "");
                        list2.add(onemap);//redis添加任务信息
                        //自动分配 且发送短信并保存到短信记录表
                        if (!"".equals(pointorentid)) {
                            sendNewAlarmTaskMessage(AlarmTaskEnum.getCode(), messageobject.get("TaskID").toString(), pointorentid, monitorpointtypecode);
                        }
                        //确认任务添加成功后 更新redis
                        RedisTemplateUtil.putCacheWithExpireAtTime(day + "_" + alarmtaskrediskey, jsonObject2, unixTimeInMillis);
                    }
                }
            }

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
    private void sendNewAlarmTaskMessage(Integer fktasktype, String taskid, String pointorentid, String monitorpointtype) {
        //取配置文件中的模板ID
        String samplenum_one = DataFormatUtil.parseProperties("samplenum.onenum");
        //未配置模板ID 则不需要短信通知
        //获取该报警数据关联的企业或点位 的负责组长信息
        Map<String, Object> param = new HashMap<>();
        param.put("pointorentid", pointorentid);
        // param.put("monitorpointtype",monitorpointtype);
        List<Map<String, Object>> users = userService.getTaskAssignUserData(param);
        Map<String, Object> taskobj = new HashMap<>();
        if (fktasktype == AlarmTaskEnum.getCode()) {
            param.put("pointdataflag", "1");
            param.put("taskid", taskid);
            //获取单条任务信息
            taskobj = alarmTaskDisposeService.getTaskInfoDataByTaskID(param);
        } else if (fktasktype == DevOpsTaskEnum.getCode()) {

        } else if (fktasktype == ChangeAlarmTaskEnum.getCode()) {

        }
        List<String> phones = new ArrayList<>();
        List<Map<String, Object>> phoneanduser = new ArrayList<>();
        List<String> userids = new ArrayList<>();
        //组织短信发送数据
        for (Map<String, Object> usermap : users) {
            if (usermap.get("Phone") != null && !"".equals(usermap.get("Phone").toString())) {
                phones.add(usermap.get("Phone").toString());
                phoneanduser.add(usermap);
                if (usermap.get("User_ID") != null) {
                    userids.add(usermap.get("User_ID").toString());
                }
            }
        }
        //判断是否需要自动分派任务
        String isautomaticassigntask = DataFormatUtil.parseProperties("isautomaticassigntask");//1 表示自动分派 0 表示否
        if ("1".equals(isautomaticassigntask)) {
            //自动分派任务
            if (userids.size() > 0) {
                alarmTaskDisposeService.automaticDispatchAlarmTask(taskid, userids);
            }
        }
        //判断有没有模板ID 且自动分派人ID不为空 则发送短信
        if (StringUtils.isNotBlank(samplenum_one) && users != null && users.size() > 0) {
            JSONObject socketJson = new JSONObject();
            String Message = "";
            if (taskobj != null) {
                socketJson.put("pointname", taskobj.get("monitorpointname"));
                socketJson.put("reminddata", taskobj.get("overlevelname"));
                Message = "您收到" + taskobj.get("monitorpointname") + "-" + taskobj.get("overlevelname") + "处置工单，请及时处理。";
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
        Date senddate = new Date();
        for (Map<String, Object> map : phoneanduser) {
            TextMessageVO obj = new TextMessageVO();
            obj.setPkId(UUID.randomUUID().toString());
            obj.setUsername(map.get("User_Name") != null ? map.get("User_Name").toString() : "");
            //obj.setSenduser(username);
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
     * @author: xsm
     * @date: 2020/06/09 0009 上午 8:46
     * @Description: 根据推送的队列数据新建报警及运维任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addAlarmAndDevOpsTaskInfoByMQDatassss", method = RequestMethod.POST)
    public void addAlarmAndDevOpsTaskInfoByMQDatassss(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            //推送到首页
            String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmDataMessage.getCode();
            //推送到首页
            rabbitmqController.sendMessageToHomePageceshi(jsonObject, messageType);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/20 0020 下午 1:08
     * @Description: 比较两个时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private boolean compare(String time1, String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //Date类的一个方法，如果a早于b返回true，否则返回false
        if (a.before(b))
            return true;
        else
            return false;
    }

}
