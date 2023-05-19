package com.tjpu.sp.service.common.micro;


import net.sf.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年3月19日 下午5:57:29
 * @Description:微服务调用接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@FeignClient(value = "authSystemServer", path = "/authSystem")

public interface AuthSystemMicroService {
    /**
     * 
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:15
     * @Description: 通过socketIO给所有客户端推送消息
     * @updateUser: 
     * @updateDate: 
     * @updateDescription: 
     * @param: 
     * @return: 
    */
    @RequestMapping(value = "socket/sendAllClientMessage", method = RequestMethod.POST)
    Object sendAllClientMessage(@RequestBody JSONObject jsonObject);
/**
     *
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:15
     * @Description: 通过socketIO给指定客户端推送消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "socket/sendAppointMessageToAppointClient", method = RequestMethod.POST)
    Object sendAppointMessageToAppointClient(@RequestBody JSONObject jsonObject);

    /**
     *
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:15
     * @Description: 获取所有连接客户端信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "socket/getAllClientInfo", method = RequestMethod.POST)
    Object getAllClientInfo();

    /**
     *
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:37
     * @Description: 极光推送内容消息制定客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "jgpush/sendMessageToAppClient", method = RequestMethod.POST)
    void sendMessageToAppClient(JSONObject jgJson);


    /**
     *
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:37
     * @Description: 极光推送内容消息和通知消息给制定客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "jgpush/sendMessageAndTitleToAppClient", method = RequestMethod.POST)
    Object sendMessageAndTitleToAppClient(JSONObject jgJson);


    /**
     *
     * @author: lip
     * @date: 2019/8/2 0002 下午 3:37
     * @Description: 极光推送制定客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "userInfo/getSystemMenuRightByUserId", method = RequestMethod.POST)
    Object getSystemMenuRightByUserId(JSONObject jgJson);


    /**
     *
     * @author: lip
     * @date: 2020/5/26 0026 上午 11:18
     * @Description: 发送短信到指定人
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "jgpush/sendTaskSMSToClient", method = RequestMethod.POST)
    Object sendTaskSMSToClient(JSONObject socketJson);
    /**
     *
     * @author: lip
     * @date: 2020/5/26 0026 上午 11:18
     * @Description: 发送密码短信到指定人
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @RequestMapping(value = "jgpush/sendPassWordSMSToClient", method = RequestMethod.POST)
    Object sendPassWordSMSToClient(JSONObject socketJson);

    /**
     *
     * @author: lip
     * @date: 2020/5/26 0026 上午 11:18
     * @Description: 发送报警短信到指定人
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "jgpush/sendAlarmSMSToClient", method = RequestMethod.POST)
    Object sendAlarmSMSToClient(JSONObject socketJson);
    @RequestMapping(value = "jgpush/sendOffLineSMSToClient", method = RequestMethod.POST)
    Object sendOffLineSMSToClient(JSONObject messageParam);
}
