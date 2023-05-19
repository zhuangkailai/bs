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
import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.AlarmTaskDisposeService;
import com.tjpu.sp.service.environmentalprotection.taskmanagement.ComplaintTaskDisposeService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年8月6日 上午11:50
 * @Description:投诉任务处置管理接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("complaintTaskManagement")
public class ComplaintTaskDisposeController {

    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private ComplaintTaskDisposeService complaintTaskDisposeService;
    @Autowired
    private AlarmTaskDisposeService alarmTaskDisposeService;
    @Autowired
    private RabbitmqController rabbitmqController;
    @Autowired
    private AuthSystemMicroService authSystemMicroService;
    @Autowired
    private AlarmTaskDisposeController alarmTaskDisposeController;

    private String sysmodel = "complaintTaskManagement";



    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 上午 8:43
     * @Description:根据自定义参数获取投诉任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getComplaintTaskDisposeListDataByParamMap", method = RequestMethod.POST)
    public Object getComplaintTaskDisposeListDataByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                            @RequestJson(value = "endtime", required = false) String endtime,
                                                            @RequestJson(value = "statuslist", required = false) List<String> statuslist,
                                                            @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                            @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                            @RequestJson(value = "sign", required = false) String sign,
                                                            HttpServletRequest request, HttpSession session) throws Exception {
        try {
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            if (sign!=null&&"app".equals(sign)){
                paramMap.put("sysmodel",  CommonTypeEnum.SocketTypeEnum.APPComplaintTaskDisposeEnum.getMenucode());
            }else {
                paramMap.put("sysmodel", sysmodel);
            }
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("statuslist", statuslist);
            //根据任务状态类型赋值报警任务状态
            paramMap.put("sign", sign);
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            Map<String, Object> datas = new HashMap<>();
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
            listdata = complaintTaskDisposeService.getAllComplaintTaskDisposeListDataByParamMap(paramMap);
            if (sign == null) {//PC端调用 返回按钮
                Map<String, Object> buttondatamap = new HashMap<>();//按钮
                buttondatamap.put("topbuttondata", topoperations);
                buttondatamap.put("tablebuttondata", listoperation);
                Comparator<Object> comparebystatus = Comparator.comparing(m -> ((Map) m).get("Status").toString());
                Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("SubmitTime").toString()).reversed();
                List<Map<String, Object>> collect = listdata.stream().sorted(comparebystatus.thenComparing(comparebytime)).collect(Collectors.toList());
                //处理分页数据
                if (pagenum != null && pagesize != null) {
                    datas.put("total", collect.size());
                    collect = getPageData(collect, pagenum, pagesize);
                    datas.put("datalist", collect);
                } else {
                    datas.put("datalist", collect);
                }
                //返回数据
                datas.put("buttondata", buttondatamap);
            } else {
                //处理分页数据
                if (pagenum != null && pagesize != null) {
                    datas.put("total", listdata.size());
                    listdata = getPageData(listdata, pagenum, pagesize);
                    datas.put("datalist", listdata);
                } else {
                    datas.put("datalist", listdata);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", datas);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:分派投诉任务
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addComplaintTaskDisposeInfo", method = RequestMethod.POST)
    public Object addComplaintTaskDisposeInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            //保存投诉任务信息
            complaintTaskDisposeService.saveComplaintTaskInfo(userId, username, formdata);
            List<String> userids = (List<String>) formdata.get("userids");
            //推送到首页
            if (userids.size()>0) {
                alarmTaskDisposeController.pushAlarmTaskToPageHome(userids);
            }
            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //sendComplaintTaskMessage(username,formdata);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     *
     * @author: lip
     * @date: 2020/5/26 0026 下午 1:44
     * @Description: 投诉任务首页+短信推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendComplaintTaskMessage(String username, Map<String, Object> formdata) {
        List<String> userIds = (List<String>) formdata.get("userids");
        if (userIds.size() > 0) {
            String pollutionId = formdata.get("pk_id").toString();
            PetitionInfoVO petitionInfoVO = complaintTaskDisposeService.selectByPrimaryKey(pollutionId);
            String pollutionName = petitionInfoVO.getPetitiontitle();
            String time = DataFormatUtil.getDateYMDHM(new Date());
            time = DataFormatUtil.FormatDateOneToOther(time, "yyyy-MM-dd HH:mm", "H点mm分");
            String context = "您收到来自" + username + time + "派发的" + pollutionName + "的投诉事件处置工单，请及时处理。";
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
            socketJson  = new JSONObject();
            socketJson.put("userids",userIds);
            socketJson.put("username",username);
            socketJson.put("time",time);
            socketJson.put("mointorpointname",pollutionName);
            socketJson.put("tasktype","投诉事件处置工单");
            authSystemMicroService.sendTaskSMSToClient(socketJson);
        }
    }
    /**
     * @author: xsm
     * @date: 2019/9/29 0029 上午 9:44
     * @Description:暂存投诉任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addComplaintTaskTemporaryInfo", method = RequestMethod.POST)
    public Object addComplaintTaskTemporaryInfo(@RequestJson(value = "pk_id") String pk_taskid,
                                                @RequestJson(value = "completetime") String completetime,
                                                @RequestJson(value = "completereply", required = false) String completereply,
                                                @RequestJson(value = "undertakedepartment") String undertakedepartment,
                                                @RequestJson(value = "fileid", required = false) String fileid,
                                                HttpServletRequest request, HttpSession session) throws Exception {
        try {
            PetitionInfoVO petitionInfo = complaintTaskDisposeService.selectByPrimaryKey(pk_taskid);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            petitionInfo.setCompletetime(completetime);
            petitionInfo.setCompletereply(completereply);
            petitionInfo.setFkFileid(fileid);
            petitionInfo.setUndertakedepartment(undertakedepartment);
            petitionInfo.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            petitionInfo.setUpdateuser(username);
            complaintTaskDisposeService.temporaryTaskInfo(petitionInfo);
            return AuthUtil.parseJsonKeyToLower("success", null);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/7/18 0018 上午 10:36
     * @Description:获取暂存回显信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getComplaintTaskTemporaryInfo", method = RequestMethod.POST)
    public Object getComplaintTaskTemporaryInfo(@RequestJson(value = "pk_taskid") String pk_taskid, HttpSession session) throws Exception {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            result = complaintTaskDisposeService.getComplaintTaskTemporaryInfo(pk_taskid, userId, username);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/16 0016 下午 7:58
     * @Description:反馈投诉任务结果信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "addComplaintTaskFeedbackInfo", method = RequestMethod.POST)
    public Object addComplaintTaskFeedbackInfo(HttpServletRequest request, HttpSession session) throws Exception {
        try {
            Map<String, Object> paramMap = RequestUtil.parseRequest(request);
            Map<String, Object> formdata = (Map<String, Object>) paramMap.get("formdata");
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username",  String.class);
            PetitionInfoVO petitionInfo = complaintTaskDisposeService.selectByPrimaryKey(formdata.get("pk_id").toString());
            petitionInfo.setCompletetime(formdata.get("completetime") != null ? formdata.get("completetime").toString() : "");
            petitionInfo.setCompletereply(formdata.get("completereply") != null ? formdata.get("completereply").toString() : "");
            petitionInfo.setUndertakedepartment(formdata.get("undertakedepartment") != null ? formdata.get("undertakedepartment").toString() : "");
            petitionInfo.setFkFileid(formdata.get("fileid") != null ? formdata.get("fileid").toString() : "");
            petitionInfo.setUpdatetime(DataFormatUtil.getDateYMDHMS(new Date()));
            petitionInfo.setUpdateuser(username);
            complaintTaskDisposeService.updateAlarmTaskInfo(userId, petitionInfo);
            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(petitionInfo.getPkId());
            alarmTaskDisposeController.pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/6 0006 下午 5:12
     * @Description:获取处置人下拉列表框数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @RequestMapping(value = "getDisposePersonSelectData", method = RequestMethod.POST)
    public Object getDisposePersonSelectData() throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
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
     * @date: 2019/8/06 0006 下午5:18
     * @Description: 保存任务状态为处理中
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "addComplaintTaskStatusToHandle", method = RequestMethod.POST)
    public Object addComplaintTaskStatusToHandle(@RequestJson(value = "id", required = true) String id, HttpSession session) throws Exception {
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            complaintTaskDisposeService.addComplaintTaskStatusToHandle(id, userId, username);
            //String messagetype = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
            //rabbitmqController.sendTaskDirectQueue(messagetype);
            //推送消息到首页
            List<String> useridsByTaskid = alarmTaskDisposeService.getUseridsByTaskid(id);
            alarmTaskDisposeController.pushAlarmTaskToPageHome(useridsByTaskid);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 下午 6:47
     * @Description:导出-投诉任务处置管理信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "exportComplaintTaskDisposeManagement", method = RequestMethod.POST)
    public void exportComplaintTaskDisposeManagement(@RequestJson(value = "starttime", required = false) String starttime,
                                                     @RequestJson(value = "endtime", required = false) String endtime,
                                                     HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //获取表头数据
            List<Map<String, Object>> tabletitledata = complaintTaskDisposeService.getTableTitleForComplaintTask();
            //根据用户ID获取该用户的按钮权限，判断其是否有处置的按钮权限，有则显示全部任务，没有则只显示分派给自己的任务
            String sessionId = session.getId();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid",  String.class);
            //按钮数据
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userid", userId);
            paramMap.put("datasource", datasource);
            paramMap.put("sysmodel", sysmodel);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            listdata = complaintTaskDisposeService.getAllComplaintTaskDisposeListDataByParamMap(paramMap);
            Comparator<Object> comparebytime = Comparator.comparing(m -> ((Map) m).get("Status").toString());
            List<Map<String, Object>> collect = listdata.stream().sorted(comparebytime).collect(Collectors.toList());
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "投诉任务处置管理导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, collect, "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/7/17 0017 上午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }


}
