package com.tjpu.auth.controller.common;

import com.corundumstudio.socketio.SocketIOServer;
import com.tjpu.auth.socketIo.MessageEventHandler;

import com.tjpu.auth.socketIo.SocketClientInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import io.swagger.annotations.*;
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
@RequestMapping("socket")
@RestController
@Api(value = "socketIO消息处理类", tags = "socketIO消息处理类")
public class SocketController {
    /**
     * socketIo服务器
     */
    public SocketIOServer socketIoServer;


    /**
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:02
     * @Description: 发送消息给全部客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: messagetype:消息类型,messagedata:消息数据,messagemethod:消息接收的方法名称
     * @return:
     */
    @RequestMapping(value = "sendAllClientMessage", method = RequestMethod.POST)
    public void sendAllClientMessage(
            @RequestJson(value = "messagetype",required = false) Integer messagetype,
            @RequestJson(value = "messagedata", required = false) Object messagedata,
            @RequestJson(value = "messagemethod") String messagemethod) throws Exception {

        try {
            Map<String, Set<SocketClientInfo>> mapClient = MessageEventHandler.mapClient;
            if (mapClient.size() > 0) {
                String dateTime = new Date().toString();
                for (Map.Entry<String, Set<SocketClientInfo>> entry : mapClient.entrySet()) {
                    Set<SocketClientInfo> socketClientInfos = entry.getValue();
                    for (SocketClientInfo client : socketClientInfos) {
                        client.getSocketIOClient().sendEvent(messagemethod, dateTime, messagedata);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:02
     * @Description: 发送指定的消息给指定的客户端
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: messagetype：消息类型，messageanduserdata：消息数据和指定用户集合，messagemethod：socketio接收方法
     * @return:
     */
    @RequestMapping(value = "sendAppointMessageToAppointClient", method = RequestMethod.POST)
    public void sendAppointClientAppointMessage(
            @RequestJson(value = "messagetype", required = false) String messagetype,
            @RequestJson(value = "messageanduserdata", required = false) Object messageanduserdata,
            @RequestJson(value = "messagemethod") String messagemethod) throws Exception {
        try {
            Map<String, Set<SocketClientInfo>> mapClient = MessageEventHandler.mapClient;
            if (messageanduserdata != null && mapClient.size() > 0) {
                List<Map<String, Object>> messageAndUserData = (List<Map<String, Object>>) messageanduserdata;
                String userid;
                for (Map<String, Object> map : messageAndUserData) {
                    userid = map.get("userid").toString();
                    Object messagedata = map.get("messagedata");
                    String dateTime = new Date().toString();
                    for (Map.Entry<String, Set<SocketClientInfo>> entry : mapClient.entrySet()) {
                        Set<SocketClientInfo> socketClientInfos = entry.getValue();
                        for (SocketClientInfo client : socketClientInfos) {
                            if (userid.equals(client.getUserId())) {
                                client.getSocketIOClient().sendEvent(messagemethod, dateTime, messagedata);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:02
     * @Description: 获取全部客户端信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllClientInfo", method = RequestMethod.POST)
    public Object getAllClientInfo() {
        try {
            List<SocketClientInfo> socketClientInfos = new ArrayList<>();

            Map<String, Set<SocketClientInfo>> mapClient = MessageEventHandler.mapClient;

            for (String key:mapClient.keySet()){
                socketClientInfos.addAll(mapClient.get(key));
            }
            return socketClientInfos;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

}
