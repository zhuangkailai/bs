package com.tjpu.sp.controller.common;

import com.rabbitmq.client.Channel;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.controller.envhousekeepers.enterpriseportal.EnterprisePortalController;
import com.tjpu.sp.controller.environmentalprotection.AlarmRemind.PcHBAlarmRemindController;
import com.tjpu.sp.controller.environmentalprotection.onlinemonitor.OnlineCountController;
import com.tjpu.sp.controller.environmentalprotection.pointofflinerecord.PointOffLineRecordController;
import com.tjpu.sp.controller.environmentalprotection.taskmanagement.AlarmTaskDisposeController;
import com.tjpu.sp.controller.environmentalprotection.taskmanagement.EnvSupervisionController;
import com.tjpu.sp.controller.environmentalprotection.taskmanagement.TaskManagementController;
import com.tjpu.sp.scheduletask.StaticScheduleTask;
import com.tjpu.sp.service.base.output.UserMonitorPointRelationDataService;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.micro.JnaServiceMicroService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.report.ReportManagementService;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RabbitMQAlarmTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WechatPushSetAlarmTypeEnum.OfflineStatusEnum;

/**
 * @author: lip
 * @date: 2019/7/19 0019 上午 9:14
 * @Description: rabbitmq消息队列处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */

@RestController
@RequestMapping("rabbitmq")
public class RabbitmqController {
    @Autowired
    private RabbitSender rabbitSender;

    @Autowired
    private JGUserRegisterInfoService jgUserRegisterInfoService;

    @Autowired
    private OnlineCountController onlineCountController;
    @Autowired
    private ReportManagementService reportManagementService;

    @Autowired
    private PollutionService pollutionService;

    @Autowired
    private OnlineService onlineService;

    @Autowired
    private EnvSupervisionController envSupervisionController;

    @Autowired
    private AlarmTaskDisposeController alarmTaskDisposeController;


    @Autowired
    private TaskManagementController taskManagementController;



    @Autowired
    private AuthSystemMicroService authSystemMicroService;

    @Autowired
    private JnaServiceMicroService jnaServiceMicroService;

    @Autowired
    private PcHBAlarmRemindController pcHBAlarmRemindController;

    @Autowired
    private UserMonitorPointRelationDataService userMonitorPointRelationDataService;

    @Autowired
    private PointOffLineRecordController pointOffLineRecordController;

    @Autowired
    private EnterprisePortalController enterprisePortalController;


    /**
     * 上次推送时间
     */
    private Date pcBefore5Time = null;

    /**
     * 上次推送时间(60min)
     */
    private Date pcBefore60Time = null;


    /**
     * 上次推送时间
     */
    private Date appBefore10Time = null;
    /**
     * 上次推送时间
     */
    private Date appBefore20Time = null;
    //配置的超标报警数据类型


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:50
     * @Description: 发送在线监测直接队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @PostMapping("/sendTaskDirectQueue")
    public void sendTaskDirectQueue(@RequestJson(value = "messagetype") String messagetype) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("messagetype", messagetype);
            MessageProperties properties = new MessageProperties();
            Message message = new Message(jsonObject.toString().getBytes(), properties);
            rabbitSender.sendMessage(RabbitMqConfig.TASK_DIRECT_EXCHANGE, RabbitMqConfig.TASK_DIRECT_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostMapping("/test")
    public void test() {
        try {

            String S = "{\n" +
                    "    \"PollutionName\": \"城市污水处理厂\",\n" +
                    "    \"DataType\": \"RealTimeData\",\n" +
                    "    \"OnlineStatus\": \"\",\n" +
                    "    \"MQMessage\": [\n" +
                    "        {\n" +
                    "            \"AlarmType\": \"Online_Exception\",\n" +
                    "            \"PollutantName\": \"化学需氧量\",\n" +
                    "            \"MonitorValue\": 11.7,\n" +
                    "            \"StandValue\": \"连续值值为：14.700\",\n" +
                    "            \"PollutantCode\": \"011\",\n" +
                    "            \"PollutantUnit\": \"mg/L\",\n" +
                    "            \"Multiple\": 0,\n" +
                    "            \"ContinueTime\": 0,\n" +
                    "            \"ExceptionType\": \"2\",\n" +
                    "            \"AlarmLevel\": null,\n" +
                    "            \"FirstAlarmTime\": \"2022-09-29 15:57:00\",\n" +
                    "            \"LastAlarmTime\": null\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"TaskID\": null,\n" +
                    "    \"PollutionID\": \"010515e3-15c7-41f4-981e-749747b8530c\",\n" +
                    "    \"MessageType\": \"2\",\n" +
                    "    \"DateTime\": \"2022-09-29 15:57:00\",\n" +
                    "    \"MonitorPointId\": \"ba59fb74-1478-488c-ad6f-54a5ebc72977\",\n" +
                    "    \"OutPutName\": \"废水排口\",\n" +
                    "    \"MonitorPointTypeCode\": \"1\",\n" +
                    "    \"MN\": \"47568770410280\",\n" +
                    "    \"MonitorPointCategory\": \"\",\n" +
                    "    \"IsContinueOver\": null,\n" +
                    "    \"IsContinueException\": false,\n" +
                    "    \"IsContinueChange\": null,\n" +
                    "    \"RecoveryTime\": \"2022-09-29 15:57:00\"\n" +
                    "}";
            JSONObject jsonObject = JSONObject.fromObject(S);
            sendMessageToHomePage(jsonObject,"2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: xsm
     * @date: 2020/2/18 0018 下午 19:42
     * @Description: 发送视频叠加队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @PostMapping("/sendVideoOverlayDirectQueue")
    public void sendVideoOverlayDirectQueue(@RequestJson(value = "jsonobject") Object jsonObject) {
        try {
            MessageProperties properties = new MessageProperties();
            Message message = new Message(jsonObject.toString().getBytes(), properties);
            rabbitSender.sendMessage(RabbitMqConfig.VIDEO_OVERLAY_DIRECT_EXCHANGE, RabbitMqConfig.VIDEO_OVERLAY_DIRECT_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/2/18 0018 下午 19:42
     * @Description: 发送点位更新队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @PostMapping("/sendPointUpdateDirectQueue")
    public void sendPointUpdateDirectQueue(@RequestJson(value = "jsonobject") Object jsonObject) {
        try {
            MessageProperties properties = new MessageProperties();
            Message message = new Message(jsonObject.toString().getBytes(), properties);
            rabbitSender.sendMessage(RabbitMqConfig.POINT_UPDATE_DIRECT_EXCHANGE, RabbitMqConfig.POINT_UPDATE_DIRECT_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: lip
     * @date: 2020/2/18 0018 下午 19:42
     * @Description: 发送点位更新队列(安全相关)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @PostMapping("/sendAQPointUpdateDirectQueue")
    public void sendAQPointUpdateDirectQueue(@RequestJson(value = "jsonobject") Object jsonObject) {
        try {
            MessageProperties properties = new MessageProperties();
            Message message = new Message(jsonObject.toString().getBytes(), properties);
            rabbitSender.sendMessage(RabbitMqConfig.AQPOINT_UPDATE_DIRECT_EXCHANGE, RabbitMqConfig.AQPOINT_UPDATE_DIRECT_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:53
     * @Description: 监听报警任务队列消息处理方法：推送到首页、环境监管、移动app
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    /*@RabbitListener(queues = RabbitMqConfig.TASK_DIRECT_QUEUE)
    @RabbitHandler
    public void taskDirectMessage(Channel channel, Message message) throws Exception {
        try {
            channel.basicQos(1);
            String sendMessage = new String(message.getBody(), "utf-8");
            JSONObject jsonObject = JSONObject.fromObject(sendMessage);
            if (CommonTypeEnum.RabbitMQMessageTypeEnum.SecurityHiddenTaskMessage.getCode().equals(jsonObject.get("messagetype"))) {//安全隐患任务
                sendMessageToSecurityHiddenTask(jsonObject);
            } else if (CommonTypeEnum.RabbitMQMessageTypeEnum.SecurityTaskMessage.getCode().equals(jsonObject.get("messagetype"))) {//安全任务处置
                sendMessageToSecurityDisposeTask(jsonObject);
            } else {
                String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmTaskMessage.getCode();
                //推送到首页
                sendMessageToHomePage(jsonObject, messageType);
                //推送到PC(环境监管)
                sendMessageToEnvModel();
                //发送应急接口
                sendPostToEmergencyApi(jsonObject);
            }
            //推送到app
            //sendMessageToAPP(jsonObject, messageType);
        } catch (IOException e) {
            // 拒绝当前消息，并把消息返回原队列
            //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            e.printStackTrace();
        } finally {//不可多次确认消息
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }

        }
    }
*/

    /**
     * @author: lip
     * @date: 2020/4/17 0017 下午 5:42
     * @Description: 调用应急接口
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendPostToEmergencyApi(JSONObject jsonObject) throws IOException {
        //获取接口地址
        String url = DataFormatUtil.parseProperties("emergency.api");
        if (StringUtils.isNotBlank(url)) {
            //拼接参数
            if (jsonObject.get("PollutionID") != null) {
                String pollutionId = jsonObject.getString("PollutionID");
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("pollutionId", pollutionId);
                List<Map<String, Object>> pollutionList = pollutionService.getPollutionsInfoByParamMap(paramMap);
                if (pollutionList.size() > 0) {
                    paramMap = pollutionList.get(0);
                    JSONObject paramJson = new JSONObject();
                    paramJson.put("datasourcePkid", jsonObject.get("TaskID"));
                    paramJson.put("alarmcontent", jsonObject.get("PollutionName"));
                    paramJson.put("longitude", paramMap.get("longitude") != null ? paramMap.get("longitude") : "");
                    paramJson.put("latitude", paramMap.get("latitude") != null ? paramMap.get("latitude") : "");
                    paramJson.put("fkPollutionid", pollutionId);
                    paramJson.put("fkRegion", paramMap.get("regioncode") != null ? paramMap.get("regioncode") : "");
                    paramJson.put("regionname", paramMap.get("regionname") != null ? paramMap.get("regionname") : "");
                    paramJson.put("address", paramMap.get("address") != null ? paramMap.get("address") : "");
                    paramJson.put("industryname", paramMap.get("fkindustrytype") != null ? paramMap.get("fkindustrytype") : "");
                    paramJson.put("happentime", jsonObject.get("DateTime") + " 00:00:00");
                    //发送请求
                    System.out.println(RequestUtil.sendPostForJson(url, paramJson, "UTF-8"));
                }
            }


        }

    }

    /**
     * @author: lip
     * @date: 2020/1/8 0008 上午 10:07
     * @Description: 发送消息到安全隐患排查模块
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
   /* private void sendMessageToSecurityHiddenTask(JSONObject jsonObject) throws Exception {
        List<String> sessionIds = getOnlineSession();
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        Map<String, Object> dataMap;
        Object resultObject;
        String userId;
        String messagemethod = "";
        if (sessionIds.size() > 0) {
            JSONObject resultJson;

            for (String sessionId : sessionIds) {
                Map<String, Object> map = new HashMap<>();
                resultObject = hiddenDangerTaskController.countHiddenDangerTaskMenuRemind(sessionId);
                resultObject = AuthUtil.decryptData(resultObject);
                if (resultObject != null) {
                    resultJson = JSONObject.fromObject(resultObject);
                    dataMap = (Map<String, Object>) resultJson.get("data");
                    if (dataMap.size() > 0) {
                        userId = dataMap.get("userid").toString();
                        map.put("userid", userId);
                        map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", dataMap));
                        messageanduserdata.add(map);
                    }
                }
            }
        }
        messagemethod = CommonTypeEnum.SocketTypeEnum.AQHiddenTaskEnum.getSocketMethod();
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }

    }*/

    /**
     * @author: lip
     * @date: 2020/1/8 0008 上午 10:07
     * @Description: 发送消息到安全处置任务模块
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
   /* private void sendMessageToSecurityDisposeTask(JSONObject jsonObject) throws Exception {
        List<String> userids = new ArrayList<>();
        List<Map<String, Object>> userIdAndAuths = new ArrayList<>();
        Map<String, Object> userIdAndCode = new HashMap<>();
        List<JSONObject> userauth;
        Set<String> validUserIds = new HashSet<>();
        String userId;
        String userCode;
        List<String> sessionIds = getOnlineSession();
        //否，获取redis中的权限
        for (String sessionId : sessionIds) {
            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
            userCode = RedisTemplateUtil.getRedisCacheDataByKey("usercode", sessionId, String.class);
            userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
            Map<String, Object> userIdAndAuth = new HashMap<>();
            if (StringUtils.isNotBlank(userId)) {
                userids.add(userId);
                userIdAndAuth.put("userid", userId);
                userIdAndAuth.put("userauth", userauth);
                userIdAndAuths.add(userIdAndAuth);
                userIdAndCode.put(userId, userCode);
                validUserIds.add(userId);
            }
        }
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        List<JSONObject> objectList;
        Object resultObject;
        JSONObject messagedata;
        String usercode;
        String menuid = CommonTypeEnum.SocketTypeEnum.SafetyTaskDisposalEnum.getMenuid();
        for (Map<String, Object> userIdAndAuth : userIdAndAuths) {
            userId = userIdAndAuth.get("userid").toString();
            usercode = userIdAndCode.get(userId).toString();
            objectList = (List<JSONObject>) userIdAndAuth.get("userauth");
            resultObject = taskAlarmController.getTaskAlarmNumForMenusByMenuID(menuid, usercode, objectList);
            resultObject = AuthUtil.decryptData(resultObject);
            messagedata = JSONObject.fromObject(resultObject);
            Map<String, Object> map = new HashMap<>();
            map.put("userid", userId);
            map.put("messagedata", messagedata);
            messageanduserdata.add(map);
        }
        String messagemethod = CommonTypeEnum.SocketTypeEnum.SafetyTaskDisposalEnum.getSocketMethod();
        //发送消息给指定客户端
        JSONObject socketJson = new JSONObject();
        socketJson.put("messagemethod", messagemethod);
        socketJson.put("messageanduserdata", messageanduserdata);
        authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
    }*/

    /**
     * @author: lip
     * @date: 2019/9/7 0007 上午 11:16
     * @Description: 发送消息到PC(环境监管模块)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToEnvModel() throws Exception {
        List<String> sessionIds = getOnlineSession();
        if (sessionIds.size() > 0) {
            List<String> userIds = new ArrayList<>();
            String userId;
            Map<String, String> sessionIdAndUserId = new HashMap<>();
            for (String sessionId : sessionIds) {
                userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                if (StringUtils.isNotBlank(userId)) {
                    sessionIdAndUserId.put(sessionId, userId);
                    userIds.add(userId);
                }
            }
            if (sessionIdAndUserId.size() > 0) {
                List<Map<String, Object>> messageanduserdata = new ArrayList<>();
                Object alarmNumDataList;
                JSONObject messagedata;

                String menuId = CommonTypeEnum.SocketTypeEnum.EnvSupervisionEnum.getMenuid();
                for (String sessionIndex : sessionIdAndUserId.keySet()) {
                    alarmNumDataList = envSupervisionController.getEnvSupervisionChildMenusTaskNums(menuId, sessionIndex);
                    alarmNumDataList = AuthUtil.decryptData(alarmNumDataList);
                    messagedata = JSONObject.fromObject(alarmNumDataList);
                    Map<String, Object> map = new HashMap<>();
                    map.put("userid", sessionIdAndUserId.get(sessionIndex));
                    map.put("messagedata", messagedata);
                    messageanduserdata.add(map);
                }
                String messagemethod = CommonTypeEnum.SocketTypeEnum.EnvSupervisionEnum.getSocketMethod();
                //发送消息给指定客户端
                JSONObject socketJson = new JSONObject();
                socketJson.put("messagemethod", messagemethod);
                socketJson.put("messageanduserdata", messageanduserdata);
                authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
            }
        }
    }


    /**
     * @author: lip
     * @date: 2020/1/13 0019 上午 10:53
     * @Description: 监听安全在线监测队列消息处理方法:分级预警
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
   /* @RabbitListener(queues = RabbitMqConfig.AQ_ONLINE_DIRECT_QUEUE)
    @RabbitHandler
    public void AQOnlineDirectMessage(Channel channel, Message message) throws Exception {
        try {
            channel.basicQos(1, false);
            String sendMessage = new String(message.getBody(), "utf-8");
            JSONObject jsonObject = JSONObject.fromObject(sendMessage);
            if (isSendMessage(jsonObject)) {
                String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.SecurityAlarmDataMessage.getCode();
                //分级预警
                if (isSendPc()) {
                    sendMessageToAQGradeAlarm(jsonObject, messageType);
                    sendMessageToAQHomePage(jsonObject, messageType);
                    sendMessageToAQApp(jsonObject, messageType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {//不可多次确认消息
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }*/


    /**
     * @author: lip
     * @date: 2020/1/13 0019 上午 10:53
     * @Description: 监听点位离线队列
     * @updateUser:xsm
     * @updateDate:2021/05/12 0012 下午 17:15
     * @updateDescription:将离线点位信息保存到T_BAS_PointOffLineRecord表中
     * @param:
     * @return:
     */
    @RabbitListener(queues = RabbitMqConfig.OFFLINE_POINT_DIRECT_QUEUE)
    @RabbitHandler
    public void AQOffPointDirectMessage(Channel channel, Message message) throws Exception {
        try {
            channel.basicQos(1, false);
            String sendMessage = new String(message.getBody(), "utf-8");
            JSONObject jsonObject = JSONObject.fromObject(sendMessage);
            sendOffMessageToUser(jsonObject);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {//不可多次确认消息
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/08 0008 下午 1:45
     * @Description: 监听超标超限报警任务队列消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RabbitListener(queues = RabbitMqConfig.ALARM_TASK_DIRECT_QUEUE)
    @RabbitHandler
    public void AlarmTaskDirectMessage(Channel channel, Message message) throws Exception {
        try {
            channel.basicQos(1, false);
            String sendMessage = new String(message.getBody(), "utf-8");
            JSONObject jsonObject = JSONObject.fromObject(sendMessage);
            //获取是否生成任务标记
            String isgeneratetask = DataFormatUtil.parseProperties("isgeneratetask");
            //获取是否使用新的生成逻辑生成任务
            String isusenewlogic = DataFormatUtil.parseProperties("isusenewlogic");
            if (isgeneratetask != null && !"".equals(isgeneratetask)&&isgeneratetask.equals("1")) {
                if (isusenewlogic!= null && !"".equals(isusenewlogic)&&isusenewlogic.equals("1")) {
                    taskManagementController.addNewAlarmTaskInfoByMQData(jsonObject);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {//不可多次确认消息
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }


    /**
     * @author: xsm
     * @date: 2021/07/27 0027 上午 11:37
     * @Description: 报警任务短信提醒（分派、反馈、超时未分派）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public Object sendAlarmTaskRemindMessageToUser(JSONObject jsonObject) {
        //组装任务短信数据
        List<String> phonelist = (List<String>) jsonObject.get("phonelist");
        JSONObject messageParam = new JSONObject();
        List<JSONObject> paramList = new ArrayList<>();
        Map<String, String> paramSMS = new HashMap<>();
        if (jsonObject.get("pointname")!=null){
            paramSMS.put("pointname", jsonObject.get("pointname")!=null?jsonObject.get("pointname").toString():"");
        }
        if (jsonObject.get("reminddata")!=null){
            paramSMS.put("reminddata", jsonObject.get("reminddata")!=null?jsonObject.get("reminddata").toString():"");
        }
        if (jsonObject.get("num")!=null){
            paramSMS.put("num", jsonObject.get("num")!=null?jsonObject.get("num").toString():"");
        }
        for (String phone : phonelist) {
            JSONObject subParam = new JSONObject();
            subParam.put("sampleNum", jsonObject.get("samplenum").toString());
            subParam.put("phoneNumber", phone);
            subParam.put("paramSMS", paramSMS);
            paramList.add(subParam);
        }
        messageParam.put("messageParamList", paramList);
        Object resultMap = authSystemMicroService.sendOffLineSMSToClient(messageParam);
        return resultMap;
        }

    /**
     * @author: lip
     * @date: 2021/1/19 0019 下午 6:06
     * @Description: 模板：{{entname}}，在{{monitortime}}时，发生离线现象，请及时处理。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendOffMessageToUser(JSONObject jsonObject) {
        //1，获取要发送短信的用户
        Map<String, Object> paramMap = new HashMap<>();
        Integer sampleNum = 189223;
        paramMap.put("dgimns", jsonObject.get("dgimns"));
        paramMap.put("SendPush", CommonTypeEnum.PushTypeEnum.MessageEnum.getCode());
        List<Map<String, Object>> pushDataList = jgUserRegisterInfoService.getUserPushDataByParam(paramMap);
        if (pushDataList.size() > 0) {
            Map<String, List<String>> phoneAndMns = new HashMap<>();
            String phone;
            List<String> mns;
            for (Map<String, Object> pushData : pushDataList) {
                phone = pushData.get("phone").toString();
                if (phoneAndMns.containsKey(phone)) {
                    mns = phoneAndMns.get(phone);
                } else {
                    mns = new ArrayList<>();
                }
                mns.add(pushData.get("dgimn").toString());
                phoneAndMns.put(phone, mns);
            }
            List<Map<String, Object>> dataList = jsonObject.getJSONArray("datalist");
            String mnCommon;
            String pointName = "";
            Map<String, String> paramSMS;
            JSONObject messageParam = new JSONObject();
            List<JSONObject> paramList = new ArrayList<>();
            for (String phoneIndex : phoneAndMns.keySet()) {
                mns = phoneAndMns.get(phoneIndex);
                for (Map<String, Object> dataMap : dataList) {
                    mnCommon = dataMap.get("dgimn").toString();
                    if (mns.contains(mnCommon)) {
                        pointName += dataMap.get("pollutionname") + "（" + dataMap.get("monitorpointname") + "）、";
                    }
                }
                if (StringUtils.isNotBlank(pointName)) {
                    pointName = pointName.substring(0, pointName.length() - 1);
                }
                JSONObject subParam = new JSONObject();
                subParam.put("sampleNum", sampleNum);
                subParam.put("phoneNumber", phoneIndex);
                paramSMS = new HashMap<>();
                paramSMS.put("entname", pointName);
                paramSMS.put("monitortime", jsonObject.get("monitortime") + "");
                subParam.put("paramSMS", paramSMS);
                paramList.add(subParam);
            }
            messageParam.put("messageParamList", paramList);
            Object resultMap = authSystemMicroService.sendOffLineSMSToClient(messageParam);

        }

    }


    /**
     * @author: lip
     * @date: 2021/1/18 0018 下午 4:56
     * @Description: 发送短信到手机端  模板：{{entname}}-{{monitorpointname}}，在{{monitortime}}时，发生{{overcontent}}现象。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToAQApp(JSONObject jsonObject, String messageType) {
        //1，获取要发送短信的用户
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dgimn", jsonObject.get("MN"));
        paramMap.put("SendPush", CommonTypeEnum.PushTypeEnum.MessageEnum.getCode());
        List<String> phoneNumbers = jgUserRegisterInfoService.getUserPushPhoneByParam(paramMap);
        if (phoneNumbers.size() > 0) {
            //3，根据模板，组织报警数据
            JSONObject messageParam = new JSONObject();
            messageParam.put("phoneNumbers", phoneNumbers);
            messageParam.put("entName", jsonObject.get("PollutionName"));
            messageParam.put("monitorPointName", jsonObject.get("OutPutName"));
            messageParam.put("monitorTime", jsonObject.get("DateTime"));
            JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
            String overContent = "";
            String PollutantName;
            String AlarmType;
            String PollutantUnit;
            String MonitorValue;
            String StandValue;
            String isOver;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject pollutant = jsonArray.getJSONObject(i);
                PollutantName = pollutant.getString("PollutantName");
                PollutantUnit = pollutant.getString("PollutantUnit");
                MonitorValue = pollutant.getString("MonitorValue");
                StandValue = pollutant.getString("StandValue");
                AlarmType = pollutant.getString("AlarmType");
                if (CommonTypeEnum.RabbitMQAlarmTypeEnum.EarlyWarnMessage.getCode().equals(AlarmType)) {//超阈
                    PollutantName += "预警（监测值：" + MonitorValue + PollutantUnit + "、预警值：" + StandValue + PollutantUnit + "）；";
                } else if (CommonTypeEnum.RabbitMQAlarmTypeEnum.OverStandardMessage.getCode().equals(AlarmType)) {//超标
                    isOver = pollutant.getString("isOver");
                    isOver = switchData(isOver);
                    PollutantName += "超标（监测值：" + MonitorValue + PollutantUnit + "、" + isOver + "级报警值：" + StandValue + PollutantUnit + "）；";
                } else if (CommonTypeEnum.RabbitMQAlarmTypeEnum.ExceptionMessage.getCode().equals(AlarmType)) {//异常
                    PollutantName += "异常（监测值：" + MonitorValue + PollutantUnit + "）；";
                }
                overContent += PollutantName;
            }
            if (StringUtils.isNotBlank(overContent)) {
                overContent = overContent.substring(0, overContent.length() - 1);
            }
            messageParam.put("overContent", overContent);
            messageParam.put("messageParam", messageParam);
            authSystemMicroService.sendAlarmSMSToClient(messageParam);
        }
    }

    private String switchData(String isOver) {
        switch (isOver) {
            case "1":
                isOver = "一";
                break;
            case "2":
                isOver = "二";
                break;
            case "3":
                isOver = "三";
                break;
            case "4":
                isOver = "四";
                break;
        }
        return isOver;
    }


    /**
     * @author: chengzq
     * @date: 2020/9/19 0019 下午 2:47
     * @Description: 视频报警推送到前端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [channel, message]
     * @throws:
     */
    @RabbitListener(queues = RabbitMqConfig.AQ_ONLINE_VIDEO_QUEUE)
    @RabbitHandler
    public void AQOnlineVideoMessage(Channel channel, Message message) throws Exception {
        try {
            channel.basicQos(1, false);
            String sendMessage = new String(message.getBody(), "utf-8");
            String messagemethod = CommonTypeEnum.SocketTypeEnum.AQVideoAlarmEnum.getSocketMethod();
            //{"pollutionid":"","pollutionname":"","monitorpointid":"","monitorpointname":"","monitorpointtype":"","pkvediocameraid":"","vediocameraname":"","AlarmType":"","AlarmTime":""}
            List<String> userIs = userMonitorPointRelationDataService.getUserIdByParamMap(JSONObject.fromObject(sendMessage));
            List<Map<String, Object>> messageanduserdata = new ArrayList<>();
            for (String userid : userIs) {
                Map<String, Object> map = new HashMap<>();
                map.put("userid", userid);
                map.put("messagedata", sendMessage);
                messageanduserdata.add(map);
            }

            if (messageanduserdata.size() > 0) {
                //发送消息给指定客户端
                JSONObject socketJson = new JSONObject();
                socketJson.put("messagemethod", messagemethod);
                socketJson.put("messageanduserdata", messageanduserdata);
                authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
            }
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:53
     * @Description: 监听在线监测队列消息处理方法:推送到首页、分级预警、移动app
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RabbitListener(queues = RabbitMqConfig.ONLINE_DIRECT_QUEUE)
    @RabbitHandler
    public void OnlineDirectMessage(Channel channel, Message message) throws Exception {
        try {
            if (channel.isOpen()) {
                channel.basicQos(1);
                String sendMessage = new String(message.getBody(), "utf-8");
                JSONObject jsonObject = JSONObject.fromObject(sendMessage);
                if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("send.type"))) {
                    System.out.println(jsonObject);
                    JSONObject overJson = getOverJson(jsonObject);
                    JSONObject exceptJson = getExceptJson(jsonObject);
                    if (overJson.size()>0){
                        StaticScheduleTask.alarmList.add(overJson);
                    }
                    if (exceptJson.size()>0){
                        StaticScheduleTask.exceptionList.add(exceptJson);
                    }
                }
                //离线推送
                if (jsonObject.getString("OnlineStatus").equals("0")) {
                    //System.out.println("%%%%%%%%%%%%%%%%%" + DataFormatUtil.getDateYMDHMS(new Date()) + "离线原始数据：" + jsonObject.toString());
                    //将离线信息保存到点位离线信息表中 且推送到首页
                    pointOffLineRecordController.addPointOffLineRecordInfo(jsonObject);
                }
                //输出日志
                //taskManagementController.writeAlarmTxt(jsonObject);
                //生成任务
                //获取是否生成任务标记
                String isgeneratetask = DataFormatUtil.parseProperties("isgeneratetask");
                if (isgeneratetask != null && !"".equals(isgeneratetask)&&isgeneratetask.equals("1")) {
                    taskManagementController.addAlarmAndDevOpsTaskInfoByMQData(jsonObject);
                }
                boolean isrecovery = false;
                if (jsonObject.get("RecoveryTime")!=null&&!"null".equals(jsonObject.getString("RecoveryTime"))){
                    isrecovery = true;
                }
                //离线数据已经在上面 推送到首页  判断非离线报警数据推送
                if (!jsonObject.getString("OnlineStatus").equals("0")&&isrecovery==false &&isSendMessage(jsonObject)) {
                    String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmDataMessage.getCode();
                    //推送到首页
                    sendMessageToHomePage(jsonObject, messageType);
                    //分级预警
                    if (isSendPc()) {
                        sendMessageToGradeAlarmModel(jsonObject, messageType);
                        //发送消息到微信端
                        //sendMessageToWeChart(jsonObject, messageType);
                    }
                    //移动app
                    if (isSendApp(jsonObject)) {
                        sendMessageToAPP(jsonObject, messageType);
                        sendMessageToAppHomePage(jsonObject, messageType);
                    }
                    //企业端首页
                    //获取该企业关联的企业用户 且推到企业端首页
                    //enterprisePortalController.getEntUserAndPushEntHomePage(jsonObject);
                }
                //消息放入缓存，通过定时任务发送


                if (isAddControlData()) {
                    if ("2".equals(jsonObject.get("MonitorPointCategory")) && jsonObject.get("MonitorPointTypeCode").equals("9")) {
                        List<String> alarmTypes = Arrays.asList(EarlyWarnMessage.getCode(),
                                OverLimitMessage.getCode(), OverStandardMessage.getCode());
                        String alarmType;
                        List<String> alarmCodes = new ArrayList<>();
                        List<String> pollutantCodes = new ArrayList<>();
                        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
                        if (jsonArray != null) {
                            JSONObject object;
                            for (int i = 0; i < jsonArray.size(); i++) {
                                object = jsonArray.getJSONObject(i);
                                alarmType = object.getString("AlarmType");
                                if (alarmTypes.contains(alarmType)) {
                                    if (StringUtils.isNotBlank(object.getString("PollutantCode"))) {
                                        pollutantCodes.add(object.getString("PollutantCode"));
                                    }
                                    alarmCodes.add(alarmType);
                                }
                            }
                        }
                        if (pollutantCodes.size() > 0) {
                            Map<String, Object> paramMap = new HashMap<>();
                            paramMap.put("mn", jsonObject.get("MN"));
                            paramMap.put("pollutantcodes", pollutantCodes);
                            paramMap.put("outputname", jsonObject.get("OutPutName"));
                            paramMap.put("datetime", jsonObject.get("DateTime"));
                            paramMap.put("alarmCodes", alarmCodes);
                            reportManagementService.addControlData(paramMap);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {//不可多次确认消息
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    private JSONObject getExceptJson(JSONObject jsonObject) {
        List<String> AlarmTypes = Arrays.asList(
                CommonTypeEnum.WechatPushSetAlarmTypeEnum.ExceptionEnum.getCode());
        List<JSONObject> MQMessage = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            if (AlarmTypes.contains(object.getString("AlarmType"))){
                MQMessage.add(object);
            }
        }
        JSONObject exceptJson = new JSONObject();
        if (MQMessage.size()>0){
            exceptJson = jsonObject;
            exceptJson.put("MQMessage",MQMessage);
        }
        return exceptJson;
    }

    private JSONObject getOverJson(JSONObject jsonObject) {
        List<String> AlarmTypes = Arrays.asList(CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverStandardEnum.getCode(),
                CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverLimitEnum.getCode());
        List<JSONObject> MQMessage = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("MQMessage");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            if (AlarmTypes.contains(object.getString("AlarmType"))){
                MQMessage.add(object);
            }
        }
        JSONObject overJson = new JSONObject();
        if (MQMessage.size()>0){
            overJson = jsonObject;
            overJson.put("MQMessage",MQMessage);
        }
        return overJson;

    }


    /**
     * @author: lip
     * @date: 2020/3/12 0012 下午 1:55
     * @Description: 发送消息到微信端（微信好友，微信群）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToWeChart(JSONObject jsonObject, String messageType) throws InterruptedException {
        List<Map<String, Object>> MQMessages = jsonObject.getJSONArray("MQMessage");
        if (MQMessages.size() > 0) {
            //获取具有微信推送标记的用户信息
            String pushType = CommonTypeEnum.PushTypeEnum.WeChartEnum.getCode();
            List<Map<String, Object>> userMapList = jgUserRegisterInfoService.getUserInfoByPushType(pushType);
            if (userMapList.size() > 0) {
                Set<String> userTitles = new HashSet<>();
                String userTitle;
                for (Map<String, Object> userMap : userMapList) {
                    if (userMap.get("title") != null) {
                        userTitle = userMap.get("title").toString();
                        userTitles.add(userTitle);
                    }
                }
                List<Map<String, Object>> userPushSetList = jgUserRegisterInfoService.getUserPushSetList();
                if (userPushSetList.size() > 0) {
                    Map<String, List<String>> userAndAlarmTypes = new HashMap<>();
                    String alarmType;
                    List<String> alarmTypes;
                    for (Map<String, Object> userPushSet : userPushSetList) {
                        if (userPushSet.get("title") != null && userPushSet.get("alarmtype") != null) {
                            userTitle = userPushSet.get("title").toString();
                            alarmType = userPushSet.get("alarmtype").toString();
                            if (userAndAlarmTypes.containsKey(userTitle)) {
                                alarmTypes = userAndAlarmTypes.get(userTitle);
                            } else {
                                alarmTypes = new ArrayList<>();
                            }
                            alarmTypes.add(alarmType);
                            userAndAlarmTypes.put(userTitle, alarmTypes);
                        }
                    }
                    if (userAndAlarmTypes.size() > 0) {
                        String messageContent;
                        JSONObject sendObject = new JSONObject();
                        for (String user : userAndAlarmTypes.keySet()) {
                            if (userTitles.contains(user)) {
                                messageContent = getMessageContent(userAndAlarmTypes.get(user), jsonObject);
                                sendObject.put("username", user);
                                sendObject.put("message", messageContent);
                                //推送消息到微信好友
                                jnaServiceMicroService.sendUserMessage(sendObject);
                                Thread.sleep(1500);
                            }

                        }
                    }
                }
            }
            Set<String> alarmTypes = new HashSet<>();
            for (Map<String, Object> MQMessage : MQMessages) {
                if (MQMessage.get("AlarmType") != null) {
                    alarmTypes.add(MQMessage.get("AlarmType").toString());
                }
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("AlarmTypes", alarmTypes);
            List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
            if (weChartGroups.size() > 0) {
                Map<String, List<String>> nameAndTypes = new HashMap<>();
                String weChatName;
                String alarmType;
                List<String> types;
                for (Map<String, Object> weChartGroup : weChartGroups) {
                    weChatName = weChartGroup.get("WechatName").toString();
                    alarmType = weChartGroup.get("AlarmType").toString();
                    if (nameAndTypes.containsKey(weChatName)) {
                        types = nameAndTypes.get(weChatName);
                    } else {
                        types = new ArrayList<>();
                    }
                    if (!types.contains(alarmType)) {
                        types.add(alarmType);
                    }
                    nameAndTypes.put(weChatName, types);
                }
                //发送群消息

                JSONObject sendObject = new JSONObject();
                String messageContent;
                for (String name : nameAndTypes.keySet()) {
                    messageContent = getMessageContent(nameAndTypes.get(name), jsonObject);
                    if (StringUtils.isNotBlank(messageContent)) {
                        sendObject.put("groupname", name);
                        sendObject.put("message", messageContent);
                        jnaServiceMicroService.sendGroupMessage(sendObject);
                        Thread.sleep(1500);
                    }
                }
            }
        }
    }

    /**
     * @author: lip
     * @date: 2020/3/12 0012 下午 5:12
     * @Description: 根据报警类型组装消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String getMessageContent(List<String> types, JSONObject jsonObject) {
        String content = "";
        String DataType = jsonObject.getString("DataType");
        content = "【" + CommonTypeEnum.MongodbDataTypeEnum.getTextByName(DataType) + "】";
        content += "【" + jsonObject.getString("DateTime") + "】";
        if (!"".equals(jsonObject.getString("PollutionName"))) {
            content += jsonObject.getString("PollutionName") + "-";
        }
        content += jsonObject.get("OutPutName") + "，";
        List<Map<String, Object>> MQMessages = jsonObject.getJSONArray("MQMessage");
        String PollutantName;
        String AlarmType;
        String AlarmTypeName;
        String StandValue = "";
        String MonitorValue = "";
        String PollutantUnit = "";
        String ContinueTime = "";
        List<String> overList = Arrays.asList(CommonTypeEnum.RabbitMQAlarmTypeEnum.OverLimitMessage.getCode(),
                CommonTypeEnum.RabbitMQAlarmTypeEnum.OverStandardMessage.getCode());
        boolean isHavePollutant = false;
        for (Map<String, Object> MQMessage : MQMessages) {
            StandValue = "";
            PollutantName = MQMessage.get("PollutantName").toString();
            AlarmType = MQMessage.get("AlarmType").toString();
            if (types != null && types.contains(AlarmType)) {
                isHavePollutant = true;
                AlarmTypeName = CommonTypeEnum.RabbitMQAlarmTypeEnum.getNameByCode(AlarmType);
                StandValue = MQMessage.get("StandValue").toString();
                MonitorValue = MQMessage.get("MonitorValue").toString();
                PollutantUnit = MQMessage.get("PollutantUnit").toString();
                if (overList.contains(AlarmType)) {
                    ContinueTime = MQMessage.get("ContinueTime").toString();
                    content += PollutantName + AlarmTypeName +
                            "（监测值：" + MonitorValue + PollutantUnit + "、标准值：" + StandValue + PollutantUnit + "），目前已持续超标" + ContinueTime + "小时；";
                } else if (AlarmType.equals(CommonTypeEnum.RabbitMQAlarmTypeEnum.ConcentrationChangeMessage.getCode())) {
                    String Multiple = MQMessage.get("Multiple").toString();
                    if (StringUtils.isNotBlank(Multiple)) {
                        Multiple = DataFormatUtil.formatDoubleSaveOne(Double.parseDouble(Multiple));
                    }
                    content += PollutantName + "浓度突增预警（监测值：" + MonitorValue + PollutantUnit + "、突增倍数：" + Multiple + "）；";
                } else if (AlarmType.equals(CommonTypeEnum.RabbitMQAlarmTypeEnum.ExceptionMessage.getCode())) {
                    String ExceptionType = MQMessage.get("ExceptionType").toString();
                    if (CommonTypeEnum.ExceptionTypeEnum.ZeroExceptionEnum.getCode().equals(ExceptionType)) {
                        content += PollutantName + "出现零值现象（监测值：" + MonitorValue + PollutantUnit + "）；";
                    } else if (CommonTypeEnum.ExceptionTypeEnum.ContinuousExceptionEnum.getCode().equals(ExceptionType)) {
                        content += PollutantName + "出现连续恒值现象（监测值：" + MonitorValue + PollutantUnit + "）；";
                    } else if (CommonTypeEnum.ExceptionTypeEnum.OverExceptionEnum.getCode().equals(ExceptionType)) {
                        content += PollutantName + "出现超限异常现象（监测值：" + MonitorValue + PollutantUnit + "、标准值：" + StandValue + PollutantUnit + "）；";
                    }
                } else if (AlarmType.equals(CommonTypeEnum.RabbitMQAlarmTypeEnum.EarlyWarnMessage.getCode())) {
                    content += PollutantName + "超阈值预警（监测值：" + MonitorValue + PollutantUnit + "、预警值：" + StandValue + PollutantUnit + "）；";
                }
            }
        }
        if (isHavePollutant) {
            content = content.substring(0, content.length() - 1) + "。";
        } else {
            content = "";
        }
        return content;
    }


    /**
     * @author: lip
     * @date: 2019/11/4 0004 下午 4:18
     * @Description: 判断是否距离上次发生经过5分钟
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean isSendPc() {
        boolean isSend = false;
        Date nowTime = new Date();
        if (pcBefore5Time != null) {
            isSend = !DataFormatUtil.isLessEqualUpdate(pcBefore5Time, nowTime, 5);
        } else {
            isSend = true;
        }
        if (isSend) {
            pcBefore5Time = nowTime;
        }
        return isSend;
    }


    /**
     * @author: lip
     * @date: 2019/11/4 0004 下午 4:18
     * @Description: 判断是否距离上次发生经过60分钟
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean isAddControlData() {
        boolean isSend = false;
        Date nowTime = new Date();
        if (pcBefore60Time != null) {
            isSend = !DataFormatUtil.isLessEqualUpdate(pcBefore60Time, nowTime, 60);
        } else {
            isSend = true;
        }
        if (isSend) {
            pcBefore60Time = nowTime;
        }
        return isSend;
    }

    /**
     * @param jsonObject
     * @author: lip
     * @date: 2019/11/4 0004 下午 4:18
     * @Description: 判断是否距离上次发生经过5分钟
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean isSendApp(JSONObject jsonObject) {

        List<String> waterList = Arrays.asList(
                CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() + "",
                CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() + ""
        );
        String MonitorPointTypeCode = jsonObject.getString("MonitorPointTypeCode");
        boolean isSend = false;
        if (waterList.contains(MonitorPointTypeCode)) {
            Date nowTime = new Date();
            if (appBefore20Time != null) {
                isSend = !DataFormatUtil.isLessEqualUpdate(appBefore20Time, nowTime, 20);
            } else {
                isSend = true;
            }
            if (isSend) {
                appBefore20Time = nowTime;
            }
        } else {
            Date nowTime = new Date();
            if (appBefore10Time != null) {
                isSend = !DataFormatUtil.isLessEqualUpdate(appBefore10Time, nowTime, 10);
            } else {
                isSend = true;
            }
            if (isSend) {
                appBefore10Time = nowTime;
            }
        }
        return isSend;
    }

    /**
     * @author: lip
     * @date: 2019/9/7 0007 下午 2:24
     * @Description: 根据数据类型，报警类型判断是否需要发送消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean isSendMessage(JSONObject jsonObject) {
        boolean isSend = false;
        String hourData = CommonTypeEnum.MongodbDataTypeEnum.HourDataEnum.getName();
        if (jsonObject.get("DataType") != null) {
            if (hourData.equals(jsonObject.get("DataType")) || StringUtils.isNotBlank(jsonObject.getString("OnlineStatus"))) {//小时数据不做判断
                isSend = true;
            } else {//其他数据类型判断是否全部是浓度突变
                JSONArray MQMessages = jsonObject.getJSONArray("MQMessage");
                if (MQMessages != null) {
                    JSONObject object;
                    String concentrationChange = CommonTypeEnum.RabbitMQAlarmTypeEnum.ConcentrationChangeMessage.getCode();
                    for (int i = 0; i < MQMessages.size(); i++) {
                        object = MQMessages.getJSONObject(i);
                        if (object.get("AlarmType") != null && !concentrationChange.equals(object.get("AlarmType"))) {
                            isSend = true;
                            break;
                        }
                    }
                }
            }
        }
        return isSend;
    }


    /**
     * @author: lip
     * @date: 2019/8/30 0030 下午 1:05
     * @Description: APP首页消息（报警数据，报警任务）推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToAppHomePage(JSONObject jsonObject, String messageType) throws ParseException {
        List<JSONObject> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        Object resultObject;
        String userId;
        //获取已注册用户ID
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("apptype", CommonTypeEnum.AppTypeEnum.HBEnum.getCode());
        List<Map<String, Object>> userRegisterInfo = jgUserRegisterInfoService.getUserRegisterInfoListByParam(paramMap);
        if (userRegisterInfo.size() > 0) {
            List<Map<String, Object>> userIdAndAuths = new ArrayList<>();
            List<JSONObject> userauth;
            Set<String> validUserIds = new HashSet<>();
            //判断是否过期
            List<String> sessionids = getOnlineSession();
            //否，获取redis中的权限
            for (String sessionId : sessionids) {
                userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
                Map<String, Object> userIdAndAuth = new HashMap<>();
                if (StringUtils.isNotBlank(userId)) {
                    validUserIds.add(userId);
                    userIdAndAuth.put("userid", userId);
                    userIdAndAuth.put("userauth", userauth);
                    userIdAndAuths.add(userIdAndAuth);
                }
            }
            Map<String, Object> userIdAndRegId = new HashMap<>();
            Set<String> noValidUserIds = new HashSet<>();
            for (Map<String, Object> client : userRegisterInfo) {
                if (client.get("fk_userid") != null) {
                    userId = client.get("fk_userid").toString();
                    userIdAndRegId.put(userId, client.get("regid"));
                    if (!validUserIds.contains(userId)) {
                        noValidUserIds.add(userId);
                    }
                }
            }
            //是，微服务调用获取数据库中的权限
            Object userMenu;
            JSONObject param = new JSONObject();
            for (String noUserId : noValidUserIds) {
                param.put("userid", noUserId);
                userMenu = authSystemMicroService.getSystemMenuRightByUserId(param);
                resultMap = (Map<String, Object>) userMenu;
                userauth = (List<JSONObject>) resultMap.get("data");
                if (userauth != null) {
                    Map<String, Object> userIdAndAuth = new HashMap<>();
                    userIdAndAuth.put("userid", noUserId);
                    userIdAndAuth.put("userauth", userauth);
                    userIdAndAuths.add(userIdAndAuth);
                }
            }
            List<JSONObject> objectList;
            for (Map<String, Object> userIdAndAuth : userIdAndAuths) {
                userId = userIdAndAuth.get("userid").toString();
                if (userIdAndRegId.containsKey(userId)) {
                    objectList = JSONArray.fromObject(userIdAndAuth.get("userauth"));
                    resultObject = onlineService.cacheAlarmRemindDataInRedis(jsonObject.toString(), userId, objectList);
                    if (resultObject != null) {
                        JSONObject resultJson = JSONObject.fromObject(resultObject);
                        if (resultJson.get("messagetitle") != null) {
                            JSONObject subJson = new JSONObject();
                            subJson.put("userid", userId);
                            subJson.put("messagetitle", resultJson.get("messagetitle"));
                            subJson.put("regid", userIdAndRegId.get(userId));
                            subJson.put("reminddata", resultObject);
                            messageanduserdata.add(subJson);
                        }
                    }
                }
            }
        }
        if (messageanduserdata.size() > 0) {
            JSONObject JGJson = new JSONObject();
            JGJson.put("contenttype", CommonTypeEnum.appMessageTypeEnum.AlarmDataHomeMessage.getCode());
            JGJson.put("messagetype", messageType);
            JGJson.put("messagetypename", CommonTypeEnum.appMessageTypeEnum.AlarmDataHomeMessage.getName());
            JGJson.put("messageanduserdata", messageanduserdata);
            //authSystemMicroService.sendMessageAndTitleToAppClient(JGJson);
        }


    }


    /**
     * @author: lip
     * @date: 2019/8/30 0030 下午 1:05
     * @Description: 首页消息（报警数据，报警任务）推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToHomePage(JSONObject jsonObject, String messageType)  {
        try {
            List<String> sessionIds;
            List<Map<String, Object>> messageanduserdata = new ArrayList<>();
            Map<String, Object> resultMap;
            Map<String, Object> dataMap;
            Object resultObject;
            List<Map<String, Object>> dataList;
            String userId;
            String dgimn = jsonObject.getString("MN");
            List<String> userdgimns;
            String messagemethod = "";
            List<JSONObject> userauth;
            switch (CommonTypeEnum.RabbitMQMessageTypeEnum.getCodeByString(messageType)) {
                case AlarmDataMessage:
                    //判断  只推送配置的报警数据类型
                     disposeAlarmDataForconfig(jsonObject);
                        sessionIds = getOnlineSession();
                        if (sessionIds.size() > 0) {
                            for (String sessionId : sessionIds) {
                                Map<String, Object> map = new HashMap<>();
                                userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                                if (userId != null) {
                                    userdgimns = RedisTemplateUtil.getRedisCacheDataByKey("userdgimns", sessionId, List.class);
                                    if (userdgimns != null && userdgimns.size() > 0 && userdgimns.contains(dgimn)) {
                                        userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
                                        resultObject = onlineService.cacheAlarmRemindDataInRedis(jsonObject.toString(), userId, userauth);
                                        if (resultObject != null) {
                                            resultMap = (Map<String, Object>) resultObject;
                                            if (resultMap.size() > 0) {
                                                dataList = (List<Map<String, Object>>) resultMap.get("datalist");
                                                userId = resultMap.get("userid").toString();
                                                map.put("userid", userId);
                                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", dataList));
                                                messageanduserdata.add(map);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageAlarmDataEnum.getSocketMethod();

                    break;
                case AlarmTaskMessage:
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        JSONObject resultJson;
                        for (String sessionId : sessionIds) {
                            Map<String, Object> map = new HashMap<>();
                            resultObject = envSupervisionController.getTaskRemindDataFromRedis(sessionId);
                            resultObject = AuthUtil.decryptData(resultObject);
                            if (resultObject != null) {
                                resultJson = JSONObject.fromObject(resultObject);
                                dataMap = (Map<String, Object>) resultJson.get("data");
                                if (dataMap.size() > 0) {
                                    userId = dataMap.get("userid").toString();
                                    map.put("userid", userId);
                                    map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", dataMap.get("taskdata")));
                                    messageanduserdata.add(map);
                                }
                            }
                        }
                    }
                    messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageAlarmTaskEnum.getSocketMethod();
                    break;
                case OutPutStopProductionMessage://排口停产
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            Map<String, Object> map = new HashMap<>();
                            if (jsonObject != null) {
                                resultMap = (Map<String, Object>) jsonObject;
                                userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                                map.put("userid", userId);
                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                messageanduserdata.add(map);
                            }
                        }
                    }
                    messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageEmissionControlEnum.getSocketMethod();
                    break;
                case RainMonitorMessage://雨水排放监控
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            List<JSONObject> objectList = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
                            boolean havemenu = false;
                            if (objectList != null) {
                                havemenu = isHaveMenuAuthor(objectList, CommonTypeEnum.menuAuthorityControlMenusEnum.RainMonitorEnum.getCode());
                            }
                            Map<String, Object> map = new HashMap<>();
                            if (havemenu == true) {//有该菜单权限
                                if (jsonObject != null) {
                                    resultMap = (Map<String, Object>) jsonObject;
                                    userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
                                    map.put("userid", userId);
                                    map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                    messageanduserdata.add(map);
                                }
                            }
                        }
                    }
                    messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageEmissionControlEnum.getSocketMethod();
                    break;
                case PointOffLineMessage://离线点位
                    sessionIds = getOnlineSession();
                    for (String sessionId : sessionIds) {
                        Map<String, Object> map = new HashMap<>();
                        if (jsonObject != null) {
                            resultMap = (Map<String, Object>) jsonObject;
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            map.put("userid", userId);
                            map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                            messageanduserdata.add(map);
                        }
                    }
                    messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageEmissionControlEnum.getSocketMethod();
                    break;
                default:
                    break;
            }
            if (messageanduserdata.size() > 0) {
                //发送消息给指定客户端
                JSONObject socketJson = new JSONObject();
                socketJson.put("messagemethod", messagemethod);
                socketJson.put("messageanduserdata", messageanduserdata);
                authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理推送数据
     * */
    private void disposeAlarmDataForconfig(JSONObject jsonObject) {
        //报警污染物
        List<String> alarmtypelist  = new ArrayList<>();
        //配置的超标报警数据类型
        if (DataFormatUtil.parseProperties("overpushtype")!=null){
            alarmtypelist = Arrays.asList(DataFormatUtil.parseProperties("overpushtype").split(","));
        }
        //配置的异常报警数据类型
        List<String> exceptiontypelist  = new ArrayList<>();
        if (DataFormatUtil.parseProperties("exceptionpushtype")!=null){
            exceptiontypelist = Arrays.asList(DataFormatUtil.parseProperties("exceptionpushtype").split(","));
        }
        List<Map<String, Object>> mqdata = (List<Map<String, Object>>) jsonObject.get("MQMessage");
        List<Map<String, Object>> alarmpollutants = new ArrayList<>();
        //报警数据类型
        String datatype = jsonObject.get("DataType") != null ? jsonObject.get("DataType").toString() : "";
        List<String> alarmtypes = new ArrayList<>();
        //根据配置的报警推送数据类型 判断要推送的报警信息
        //多个类型用逗号隔开 例：overpushtype = RealTimeData,MiuteData  表示超标报警 只推送实时、分钟报警数据
        if (alarmtypelist.contains(datatype)){//超标、超限
            //要推送的报警类型
            alarmtypes.add(CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverLimitEnum.getCode());
            alarmtypes.add(CommonTypeEnum.WechatPushSetAlarmTypeEnum.OverStandardEnum.getCode());
        }
        if (exceptiontypelist.contains(datatype)){//异常报警
            alarmtypes.add(CommonTypeEnum.WechatPushSetAlarmTypeEnum.ExceptionEnum.getCode());
        }
        //遍历报警污染物  筛掉不符合配置的报警类型数据
        if (mqdata != null && mqdata.size()>0 && alarmtypes.size()>0){
            for (Map<String, Object> map:mqdata){
                if (map.get("AlarmType")!=null && alarmtypes.contains(map.get("AlarmType").toString())){
                    alarmpollutants.add(map);
                }
            }
        }
        //数据时间
        if (jsonObject.get("DateTime")==null) {
            jsonObject.put("DateTime", jsonObject.get("EndTime"));
        }
        jsonObject.put("MQMessage",alarmpollutants);
    }

    /**
     * @author: lip
     * @date: 2019/8/30 0030 下午 1:05
     * @Description: 首页消息（报警数据，报警任务）推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToAQHomePage(JSONObject jsonObject, String messageType) throws Exception {
        List<String> sessionIds;
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        Map<String, Object> dataMap;
        Object resultObject;
        List<Map<String, Object>> dataList;
        String userId;
        String dgimn = jsonObject.getString("MN");
        List<String> userdgimns;
        String messagemethod = "";
        List<JSONObject> userauth;
        switch (CommonTypeEnum.RabbitMQMessageTypeEnum.getCodeByString(messageType)) {
            case SecurityAlarmDataMessage:
                sessionIds = getOnlineSession();
                if (sessionIds.size() > 0) {
                    for (String sessionId : sessionIds) {
                        Map<String, Object> map = new HashMap<>();
                        userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                        userdgimns = RedisTemplateUtil.getRedisCacheDataByKey("userdgimns", sessionId, List.class);
                        if (userdgimns != null && userdgimns.size() > 0 && userdgimns.contains(dgimn)) {
                            userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
                            resultObject = onlineService.cacheAlarmRemindDataInRedis(jsonObject.toString(), userId, userauth);
                            if (resultObject != null) {
                                resultMap = (Map<String, Object>) resultObject;
                                if (resultMap.size() > 0) {
                                    dataList = (List<Map<String, Object>>) resultMap.get("datalist");
                                    userId = resultMap.get("userid").toString();
                                    map.put("userid", userId);
                                    map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", dataList));
                                    messageanduserdata.add(map);
                                }
                            }
                        }
                    }
                }
                messagemethod = CommonTypeEnum.SocketTypeEnum.AQHomePageAlarmDataEnum.getSocketMethod();
                break;
            default:
                break;
        }
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }


    }


    /**
     * @author: xsm
     * @date: 2021/8/30 0030 下午 5:15
     * @Description: 企业端首页消息推送（企业检查反馈、咨询问题回复提醒）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToEntHomePage(JSONObject jsonObject, String messageType) throws Exception {
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        List<String> userids;
        String messagemethod = "";
        List<String> sessionIds;
        String userId = "";
        switch (CommonTypeEnum.HomePageMessageTypeEnum.getCodeByString(messageType)) {
            case EntCheckFeedbackMessage://企业自查反馈记录类型
                 userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                 resultMap = (Map<String, Object>) jsonObject;
                 resultMap.remove("userids");
                 if (userids!=null&&userids.size()>0) {
                     //获取已连接的会话ID集合
                     sessionIds = getOnlineSession();
                     if (sessionIds.size() > 0) {
                         for (String sessionId : sessionIds) {
                             userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            if (userids.contains(userId)){//判断已连接会话用户 是否为被反馈的企业用户
                                Map<String, Object> map = new HashMap<>();
                                map.put("userid", userId);
                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                messageanduserdata.add(map);
                            }
                         }
                     }
                 }
                messagemethod = CommonTypeEnum.SocketTypeEnum.EntHomePageRemindEnum.getSocketMethod();
                break;
            case ProblemReplyMessage://企业咨询问题回复
                userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                resultMap = (Map<String, Object>) jsonObject;
                resultMap.remove("userids");
                if (userids!=null&&userids.size()>0) {
                    //获取已连接的会话ID集合
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            if (userids.contains(userId)){//判断已连接会话用户 是否为被回复咨询问题的企业用户
                                Map<String, Object> map = new HashMap<>();
                                map.put("userid", userId);
                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                messageanduserdata.add(map);
                            }
                        }
                    }
                }
                messagemethod = CommonTypeEnum.SocketTypeEnum.EntHomePageRemindEnum.getSocketMethod();
                break;
            case PointOffLineMessage://企业点位离线
                userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                resultMap = (Map<String, Object>) jsonObject;
                resultMap.remove("userids");
                if (userids!=null&&userids.size()>0) {
                //获取已连接的会话ID集合
                sessionIds = getOnlineSession();
                if (sessionIds.size() > 0) {
                    for (String sessionId : sessionIds) {
                        userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                        if (userids.contains(userId)){//判断已连接会话用户 是否为被回复咨询问题的企业用户
                            Map<String, Object> map = new HashMap<>();
                            map.put("userid", userId);
                            map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                            messageanduserdata.add(map);
                        }
                    }
                }
            }
                messagemethod = CommonTypeEnum.SocketTypeEnum.EntHomePageRemindEnum.getSocketMethod();
                break;
            default:
                break;
        }
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/8/30 0030 下午 5:15
     * @Description: 企业端首页报警消息推送（）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendAlarmMessageToEntHomePage(JSONObject jsonObject, String messageType) throws Exception {
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        List<String> userids;
        String messagemethod = "";
        List<String> sessionIds;
        String userId = "";
        List<JSONObject> userauth;
        Object resultObject;
        List<Map<String, Object>> dataList;
        switch (CommonTypeEnum.RabbitMQMessageTypeEnum.getCodeByString(messageType)) {
            case AlarmDataMessage:
                userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                resultMap = (Map<String, Object>) jsonObject;
                resultMap.remove("userids");
                if (userids!=null&&userids.size()>0) {
                    //获取已连接的会话ID集合
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            if (userids.contains(userId)){//判断已连接会话用户 是否为被回复咨询问题的企业用户
                                userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
                                resultObject = onlineService.cacheAlarmRemindDataInRedis(jsonObject.toString(), userId, userauth);
                                resultMap = (Map<String, Object>) resultObject;
                                if (resultMap.size() > 0) {
                                    dataList = (List<Map<String, Object>>) resultMap.get("datalist");
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("userid", userId);
                                    map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", dataList));
                                    messageanduserdata.add(map);
                                }
                            }
                        }
                    }
                }
                messagemethod = CommonTypeEnum.SocketTypeEnum.EntHomePageAlarmRemindEnum.getSocketMethod();
                break;
            default:
                break;
        }
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/9/10 0010 上午 9:32
     * @Description: 管委会端首页消息推送（企业检查表提交、企业问题咨询提醒）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToManagementHomePage(JSONObject jsonObject, String messageType) throws Exception {
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        List<String> userids;
        String messagemethod = "";
        List<String> sessionIds;
        String userId = "";
        switch (CommonTypeEnum.HomePageMessageTypeEnum.getCodeByString(messageType)) {
            case EntProblemConsultMessage://企业问题咨询记录类型
                //拥有 咨询问题回复 菜单权限的用户ID数组
                //userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                resultMap = (Map<String, Object>) jsonObject;
                //resultMap.remove("userids");
                //if (userids!=null&&userids.size()>0) {
                    //获取已连接的会话ID集合
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            Map<String, Object> map = new HashMap<>();
                            map.put("userid", userId);
                            map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                            messageanduserdata.add(map);
                        }
                    }
                messagemethod = CommonTypeEnum.SocketTypeEnum.ManagementHomePageRemindEnum.getSocketMethod();
                break;
            case EntCheckSubmitMessage://企业自查提交记录类型
                //拥有 企业巡查信息数据查询 菜单权限的用户ID数组
                //userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                resultMap = (Map<String, Object>) jsonObject;
                resultMap.remove("userids");
                //if (userids!=null&&userids.size()>0) {
                    //获取已连接的会话ID集合
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            //if (userids.contains(userId)){//判断已连接会话用户 是否为拥有菜单权限的用户
                                Map<String, Object> map = new HashMap<>();
                                map.put("userid", userId);
                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                messageanduserdata.add(map);
                           // }
                        }
                    }
                //}
                messagemethod = CommonTypeEnum.SocketTypeEnum.ManagementHomePageRemindEnum.getSocketMethod();
                break;
            case PointOffLineMessage://点位离线
                resultMap = (Map<String, Object>) jsonObject;
                resultMap.remove("userids");
                    //获取已连接的会话ID集合
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                                Map<String, Object> map = new HashMap<>();
                                map.put("userid", userId);
                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                messageanduserdata.add(map);
                        }
                    }
                messagemethod = CommonTypeEnum.SocketTypeEnum.ManagementHomePageRemindEnum.getSocketMethod();
                break;
            default:
                break;
        }
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/9/10 0010 上午 11:03
     * @Description: 通知消息推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendNoticeMessageToHomePage(JSONObject jsonObject, String messageType) throws Exception {
        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        Map<String, Object> resultMap;
        List<String> userids;
        String messagemethod = "";
        List<String> sessionIds;
        String userId = "";
        switch (CommonTypeEnum.HomePageMessageTypeEnum.getCodeByString(messageType)) {
            case NoticeMessage:
                //拥有 咨询问题回复 菜单权限的用户ID数组
                userids = jsonObject.get("userids")!=null? (List<String>) jsonObject.get("userids") :new ArrayList<>();
                resultMap = (Map<String, Object>) jsonObject;
                resultMap.remove("userids");
                if (userids!=null&&userids.size()>0) {
                    //获取已连接的会话ID集合
                    sessionIds = getOnlineSession();
                    if (sessionIds.size() > 0) {
                        for (String sessionId : sessionIds) {
                            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                            if (userids.contains(userId)){//判断已连接会话用户 是否为接收通知推送信息的用户
                                Map<String, Object> map = new HashMap<>();
                                map.put("userid", userId);
                                map.put("messagedata", AuthUtil.parseJsonKeyToLower("success", resultMap));
                                messageanduserdata.add(map);
                            }
                        }
                    }
                }
                messagemethod = CommonTypeEnum.SocketTypeEnum.HomePageNoticeEnum.getSocketMethod();
                break;
            default:
                break;
        }
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }
    }


    /**
     * @author: lip
     * @date: 2019/8/30 0030 下午 1:08
     * @Description: 获取已连接的会话ID集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<String> getOnlineSession() {
        Object object = authSystemMicroService.getAllClientInfo();
        List<String> sessionIds = new ArrayList<>();
        String sessionId;
        List<Map<String, Object>> clients = (List<Map<String, Object>>) object;
        for (Map<String, Object> client : clients) {
            sessionId = client.get("sessionId").toString();
            sessionIds.add(sessionId);
        }
        if (sessionIds.size() > 0) {
            sessionIds = sessionIds.stream().distinct().collect(Collectors.toList());
        }
        return sessionIds;
    }


    /**
     * @author: lip
     * @date: 2019/8/2 0002 上午 10:24
     * @Description: 发送消息（报警数据，报警任务）到APP端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToAPP(JSONObject jsonObject, String messageType) {
        List<JSONObject> remindData = null;
        switch (CommonTypeEnum.RabbitMQMessageTypeEnum.getCodeByString(messageType)) {
            case AlarmDataMessage:
                //获取已注册用户ID
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("apptype", CommonTypeEnum.AppTypeEnum.HBEnum.getCode());
                List<Map<String, Object>> userRegisterInfo = jgUserRegisterInfoService.getUserRegisterInfoListByParam(paramMap);
                if (userRegisterInfo.size() > 0) {
                    List<Map<String, Object>> userIdAndAuths = new ArrayList<>();
                    List<JSONObject> userauth;
                    Set<String> validUserIds = new HashSet<>();
                    String userId;
                    //判断是否过期
                    List<String> sessionids = getOnlineSession();
                    //否，获取redis中的权限
                    for (String sessionId : sessionids) {
                        userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
                        userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
                        Map<String, Object> userIdAndAuth = new HashMap<>();
                        if (StringUtils.isNotBlank(userId)) {
                            validUserIds.add(userId);
                            userIdAndAuth.put("userid", userId);
                            userIdAndAuth.put("userauth", userauth);
                            userIdAndAuths.add(userIdAndAuth);
                        }
                    }
                    Map<String, Object> userIdAndRegId = new HashMap<>();
                    Set<String> noValidUserIds = new HashSet<>();
                    for (Map<String, Object> client : userRegisterInfo) {
                        if (client.get("fk_userid") != null) {
                            userId = client.get("fk_userid").toString();
                            userIdAndRegId.put(userId, client.get("regid"));
                            if (!validUserIds.contains(userId)) {
                                noValidUserIds.add(userId);
                            }
                        }
                    }
                    //是，微服务调用获取数据库中的权限
                    Object userMenu;
                    JSONObject param = new JSONObject();
                    for (String noUserId : noValidUserIds) {
                        param.put("userid", noUserId);
                        userMenu = authSystemMicroService.getSystemMenuRightByUserId(param);
                        Map<String, Object> resultMap = (Map<String, Object>) userMenu;
                        userauth = (List<JSONObject>) resultMap.get("data");
                        if (userauth != null) {
                            Map<String, Object> userIdAndAuth = new HashMap<>();
                            userIdAndAuth.put("userid", noUserId);
                            userIdAndAuth.put("userauth", userauth);
                            userIdAndAuths.add(userIdAndAuth);
                        }
                    }
                    remindData = new ArrayList<>();
                    List<JSONObject> objectList;
                    Object resultObject;
                    String menuid;
                    for (Map<String, Object> userIdAndAuth : userIdAndAuths) {
                        userId = userIdAndAuth.get("userid").toString();
                        if (userIdAndRegId.containsKey(userId)) {
                            menuid = CommonTypeEnum.SocketTypeEnum.APPAlarmRemindDisposeEnum.getMenuid();
                            objectList = JSONArray.fromObject(userIdAndAuth.get("userauth"));
                            resultObject = onlineCountController.countAlarmDataForAppMenus(menuid, null, objectList);
                            JSONObject subJson = new JSONObject();
                            subJson.put("userid", userId);
                            subJson.put("regid", userIdAndRegId.get(userId));
                            subJson.put("reminddata", resultObject);
                            remindData.add(subJson);
                        }
                    }
                }
                break;
            case AlarmTaskMessage:
                remindData = alarmTaskDisposeController.getAllAppUserAlarmTaskDisposeRemindData();
                break;
        }
        if (remindData != null) {
            JSONObject JGJson = new JSONObject();
            JGJson.put("contenttype", CommonTypeEnum.appMessageTypeEnum.AlarmDataCountMessage.getCode());
            JGJson.put("messagetype", messageType);
            JGJson.put("messagetypename", CommonTypeEnum.appMessageTypeEnum.AlarmDataCountMessage.getName());
            JGJson.put("messageanduserdata", remindData);
            //authSystemMicroService.sendMessageToAppClient(JGJson);
        }

    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 11:44
     * @Description: 发送消息到环保分级预警
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToGradeAlarmModel(JSONObject jsonObject, String messageType) throws Exception {
        List<String> userids = new ArrayList<>();
        List<Map<String, Object>> userIdAndAuths = new ArrayList<>();
        Map<String, Object> userIdAndCode = new HashMap<>();
        List<JSONObject> userauth;
        Set<String> validUserIds = new HashSet<>();
        String userId;
        String userCode;
        List<String> sessionIds = getOnlineSession();
        //否，获取redis中的权限
        for (String sessionId : sessionIds) {
            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
            userCode = RedisTemplateUtil.getRedisCacheDataByKey("usercode", sessionId, String.class);
            userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
            Map<String, Object> userIdAndAuth = new HashMap<>();
            if (StringUtils.isNotBlank(userId)) {
                userids.add(userId);
                userIdAndAuth.put("userid", userId);
                userIdAndAuth.put("userauth", userauth);
                userIdAndAuths.add(userIdAndAuth);
                userIdAndCode.put(userId, userCode);
                validUserIds.add(userId);
            }

        }

        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        List<JSONObject> objectList;
        Object resultObject;
        JSONObject messagedata;
        String usercode;
        String menuid;
        for (Map<String, Object> userIdAndAuth : userIdAndAuths) {
            userId = userIdAndAuth.get("userid").toString();
            usercode = userIdAndCode.get(userId).toString();
            objectList = (List<JSONObject>) userIdAndAuth.get("userauth");
            menuid = CommonTypeEnum.SocketTypeEnum.GradeAlarmEnum.getMenuid();
            resultObject = pcHBAlarmRemindController.getMonitorAlarmNumForMenusByMenuID(menuid, usercode, objectList);
            resultObject = AuthUtil.decryptData(resultObject);
            messagedata = JSONObject.fromObject(resultObject);
            Map<String, Object> map = new HashMap<>();
            map.put("userid", userId);
            map.put("messagedata", messagedata);
            messageanduserdata.add(map);
        }
        String messagemethod = CommonTypeEnum.SocketTypeEnum.GradeAlarmEnum.getSocketMethod();
        //发送消息给指定客户端
        JSONObject socketJson = new JSONObject();
        socketJson.put("messagemethod", messagemethod);
        socketJson.put("messageanduserdata", messageanduserdata);
        authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
    }


    /**
     * @author: lip
     * @date: 2020/01/13 0019 上午 11:44
     * @Description: 发送消息到安全分级预警模块
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    /*private void sendMessageToAQGradeAlarm(JSONObject jsonObject, String messageType) throws Exception {
        List<String> userids = new ArrayList<>();
        List<Map<String, Object>> userIdAndAuths = new ArrayList<>();
        Map<String, Object> userIdAndCode = new HashMap<>();
        List<JSONObject> userauth;
        Set<String> validUserIds = new HashSet<>();
        String userId;
        String userCode;
        List<String> sessionIds = getOnlineSession();
        //否，获取redis中的权限
        for (String sessionId : sessionIds) {
            userId = RedisTemplateUtil.getRedisCacheDataByKey("userid", sessionId, String.class);
            userCode = RedisTemplateUtil.getRedisCacheDataByKey("usercode", sessionId, String.class);
            userauth = RedisTemplateUtil.getRedisCacheDataByKey("userauth", sessionId, List.class);
            Map<String, Object> userIdAndAuth = new HashMap<>();
            if (StringUtils.isNotBlank(userId)) {
                userids.add(userId);
                userIdAndAuth.put("userid", userId);
                userIdAndAuth.put("userauth", userauth);
                userIdAndAuths.add(userIdAndAuth);
                userIdAndCode.put(userId, userCode);
                validUserIds.add(userId);
            }
        }

        List<Map<String, Object>> messageanduserdata = new ArrayList<>();
        List<JSONObject> objectList;
        Object resultObject;
        String usercode;
        String menuid;
        JSONObject messagedata;

        for (Map<String, Object> userIdAndAuth : userIdAndAuths) {
            userId = userIdAndAuth.get("userid").toString();
            usercode = userIdAndCode.get(userId).toString();
            objectList = (List<JSONObject>) userIdAndAuth.get("userauth");
            menuid = CommonTypeEnum.SocketTypeEnum.AQGradeAlarmEnum.getMenuid();
            resultObject = securityAlarmController.getMonitorAlarmNumForMenusByMenuID(menuid, usercode, objectList);
            resultObject = AuthUtil.decryptData(resultObject);
            messagedata = JSONObject.fromObject(resultObject);
            Map<String, Object> map = new HashMap<>();
            map.put("userid", userId);
            map.put("messagedata", messagedata);
            messageanduserdata.add(map);
        }
        String messagemethod = CommonTypeEnum.SocketTypeEnum.AQGradeAlarmEnum.getSocketMethod();
        //发送消息给指定客户端
        JSONObject socketJson = new JSONObject();
        socketJson.put("messagemethod", messagemethod);
        socketJson.put("messageanduserdata", messageanduserdata);
        authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
    }*/

    /**
     * @author: xsm
     * @date: 2020/03/17 0017 下午 4:15
     * @Description: 发送排口停产、排放控制消息到首页
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendEmissionControlInfo(JSONObject jsonObject, String messageType) {
        try {
            sendMessageToHomePage(jsonObject, messageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/30 0030 下午 5:28
     * @Description: 发送企业检查反馈消息到首页
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendEntCheckFeedbackInfo(JSONObject jsonObject, String messageType) {
        try {
            sendMessageToEntHomePage(jsonObject, messageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: xsm
     * @date: 2020/08/30 0030 下午 5:28
     * @Description: 发送企业点位报警消息到首页
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendEntAlarmData(JSONObject jsonObject, String messageType) {
        try {
            //sendAlarmMessageToEntHomePage(jsonObject, messageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/10 0010 上午 9:15
     * @Description: 发送消息到管委会首页（问题咨询、检查表单提交）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendMessageToManagementSide(JSONObject jsonObject, String messageType) {
        try {
            sendMessageToManagementHomePage(jsonObject, messageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/30 0030 下午 5:28
     * @Description: 发送通知消息到首页
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public void sendNoticeInfo(JSONObject jsonObject, String messageType) {
        try {
            //sendMessageToHomePage(jsonObject, messageType);
            sendNoticeMessageToHomePage(jsonObject, messageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: xsm
     * @date: 2020/08/08 0008 下午 7:11
     * @Description: 递归菜单权限
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [objectList, menuCode]
     * @throws:
     */
    public static boolean isHaveMenuAuthor(List<JSONObject> objectList, String menuCode) {
        boolean isHaveRight = false;
        for (JSONObject jsonObject : objectList) {
            if (menuCode.equals(jsonObject.get("menucode") != null ? jsonObject.get("menucode").toString() : "")) {
                isHaveRight = true;
                break;
            } else if (jsonObject.get("datalistchildren") != null && (jsonObject.getJSONArray("datalistchildren")).size() > 0) {
                isHaveRight = isHaveMenuAuthor(jsonObject.getJSONArray("datalistchildren"), menuCode);
                if (isHaveRight) {
                    break;
                }
            } else {
                isHaveRight = false;
            }

        }
        return isHaveRight;
    }


    /**
     * @author: chengzq
     * @date: 2021/4/26 0026 下午 3:18
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonObject]
     * @throws:
     */
    @PostMapping("/sendOnlineSupplyDirectQueue")
    public Object sendOnlineSupplyDirectQueue(@RequestJson(value = "jsonobject") Object jsonObject) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(jsonObject);
            for (Object data : jsonArray) {
                MessageProperties properties = new MessageProperties();
                //Message message = new Message(data.toString().getBytes(), properties);
                Message message = new Message(data.toString().getBytes("UTF-8"), properties);
                rabbitSender.sendMessage(RabbitMqConfig.ONLINE_Supply_DIRECT_EXCHANGE, RabbitMqConfig.ONLINE_Supply_DIRECT_KEY, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AuthUtil.parseJsonKeyToLower("success", null);
    }


    /**
     * @author: xsm
     * @date: 2021/01/13 0013 下午 16:14
     * @Description: 发送寄存器更新队列(安全相关)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @PostMapping("/sendModBusUpdateDirectQueue")
    public void sendModBusUpdateDirectQueue(@RequestJson(value = "jsonobject") Object jsonObject) {
        try {
            MessageProperties properties = new MessageProperties();
            Message message = new Message(jsonObject.toString().getBytes(), properties);
            rabbitSender.sendMessage(RabbitMqConfig.MODBUS_UPDATE_DIRECT_EXCHANGE, RabbitMqConfig.MODBUS_UPDATE_DIRECT_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToWeChartUser(JSONObject jsonObject) throws InterruptedException {
        String messageContent = "【" + jsonObject.getString("DateTime") + "】";
        if (StringUtils.isNotBlank(jsonObject.getString("PollutionName"))) {
            messageContent += jsonObject.getString("PollutionName") + "-";
        }
        messageContent += jsonObject.getString("OutPutName") + "发生离线现象，请及时处理。";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("AlarmTypes", Arrays.asList(OfflineStatusEnum.getCode()));
        List<Map<String, Object>> weChartGroups = jgUserRegisterInfoService.getWeChartGroupByParam(paramMap);
        String weChartGroupName = "";
        if (weChartGroups.size() > 0) {
            for (Map<String, Object> weChartGroup : weChartGroups) {
                weChartGroupName = weChartGroup.get("WechatName").toString();
            }
        }

        System.out.println("微信群：" + weChartGroupName + "，消息：" + messageContent);
        JSONObject sendObject = new JSONObject();
        sendObject.put("groupname", weChartGroupName);
        sendObject.put("message", messageContent);
        //推送消息到微信好友
        jnaServiceMicroService.sendGroupMessage(sendObject);
        Thread.sleep(2000);

    }

    /**
     * 测试首页报警数据推送
     * */
    public void sendMessageToHomePageceshi(JSONObject jsonObject, String messageType){
        sendMessageToHomePage(jsonObject, messageType);
    }

}
