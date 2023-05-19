package com.tjpu.auth.controller.common;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.tjpu.auth.jgpush.JPushClientUtil;
import com.tjpu.auth.model.system.UserInfoVO;
import com.tjpu.auth.service.system.UserInfoService;
import com.tjpu.pk.common.annotation.RequestJson;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: lip
 * @date: 2018/11/6 0006 下午 4:59
 * @Description: socketIO消息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RequestMapping("jgpush")
@RestController

public class JGPushController {
    /**
     * 极光客户端对象
     */

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    public JPushClientUtil jPushClientUtil;
    private int sampleString = 180164;
    private int passwordString = 180413;
    private int alarmString = 189207;

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 4:03
     * @Description: 发送消息给指定APP客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendMessageToAppClient", method = RequestMethod.POST)
    public void sendMessageToAppClient(
            @RequestJson(value = "messagetype") String messagetype,
            @RequestJson(value = "contenttype") String contenttype,
            @RequestJson(value = "messagetypename") String messagetypename,
            @RequestJson(value = "messageanduserdata") Object messageanduserdata
    ) throws Exception {
        try {
            List<Map<String, Object>> messageAndUserData = (List<Map<String, Object>>) messageanduserdata;
            String regId;
            String reminddata;
            for (Map<String, Object> map : messageAndUserData) {
                regId = map.get("regid").toString();
                reminddata = map.get("reminddata").toString();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("messagetype", messagetype);
                jsonObject.put("reminddata", reminddata);
                jPushClientUtil.sendMessageToAppointAndroid(regId, contenttype, jsonObject.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 4:03
     * @Description: 发送消息给指定APP客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendMessageAndTitleToAppClient", method = RequestMethod.POST)
    public void sendMessageAndTitleToAppClient(
            @RequestJson(value = "contenttype", required = false) String contenttype,
            @RequestJson(value = "messagetype", required = false) String messagetype,
            @RequestJson(value = "messagetypename", required = false) String messagetypename,
            @RequestJson(value = "messageanduserdata") Object messageanduserdata
    ) {
        try {
            List<Map<String, Object>> messageAndUserData = (List<Map<String, Object>>) messageanduserdata;
            String regId;
            String messagetitle;
            JSONObject reminddata;
            for (Map<String, Object> map : messageAndUserData) {
                regId = map.get("regid").toString();
                messagetitle = map.get("messagetitle").toString();
                reminddata = JSONObject.fromObject(map.get("reminddata"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("messagetype", messagetype);
                jsonObject.put("reminddata", reminddata);
                jPushClientUtil.sendMessageAndTitleToAppointAndroid(
                        regId,
                        messagetitle,
                        messagetypename,
                        contenttype,
                        jsonObject.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 4:03
     * @Description: 发送消息给指定APP客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendTaskSMSToClient", method = RequestMethod.POST)
    public void sendTaskSMSToClient(
            @RequestJson(value = "userids") List<String> userids,
            @RequestJson(value = "username") String username,
            @RequestJson(value = "time") String time,
            @RequestJson(value = "mointorpointname") String mointorpointname,
            @RequestJson(value = "tasktype") String tasktype
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userids", userids);
            List<UserInfoVO> userInfoList = userInfoService.getUserInfoVOsByParam(paramMap);
            paramMap.clear();
            if (userInfoList.size() > 0) {
                for (UserInfoVO userInfoVO : userInfoList) {
                    if (StringUtils.isNotBlank(userInfoVO.getPhone())) {
                        paramMap.put(userInfoVO.getUserId(), userInfoVO.getPhone());
                    }
                }
                for (String userid : paramMap.keySet()) {
                    jPushClientUtil.sendTaskSMS(paramMap.get(userid).toString(), sampleString, username, time, mointorpointname, tasktype);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 4:03
     * @Description: 发送消息给指定APP客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendPassWordSMSToClient", method = RequestMethod.POST)
    public void sendPassWordSMSToClient(
            @RequestJson(value = "phone") String phone,
            @RequestJson(value = "code") String code
    ) {
        try {
            jPushClientUtil.sendPasswordSMS(phone, passwordString, code);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 4:03
     * @Description: 发送消息给指定APP客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendAlarmSMSToClient", method = RequestMethod.POST)
    public void sendAlarmSMSToClient(
            @RequestJson(value = "messageParam") Object messageParam
    ) {
        try {
            jPushClientUtil.sendAlarmSMS(messageParam, alarmString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2019/8/2 0002 下午 4:03
     * @Description: 发送短信消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "sendOffLineSMSToClient", method = RequestMethod.POST)
    public Object sendOffLineSMSToClient(
            @RequestJson(value = "messageParamList") Object messageParamList
    ) throws APIConnectionException, APIRequestException {
        try {
            List<Boolean> objects = new ArrayList<>();
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) messageParamList;
            String phoneNumber;
            Integer sampleNum;
            Map<String, String> paramSMS;
            for (Map<String, Object> dataMap : dataList) {
                sampleNum = Integer.parseInt(dataMap.get("sampleNum").toString());
                phoneNumber = dataMap.get("phoneNumber").toString();
                paramSMS = (Map<String, String>) dataMap.get("paramSMS");
                boolean resString = jPushClientUtil.sendOffSMS(phoneNumber, paramSMS, sampleNum);
                objects.add(resString);
            }
            return objects;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


}
