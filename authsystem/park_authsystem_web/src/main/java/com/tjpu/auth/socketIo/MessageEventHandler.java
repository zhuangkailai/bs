package com.tjpu.auth.socketIo;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.pk.common.utils.AESUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * @version V1.0
 * @author: lip
 * @date: 2018年7月3日 上午8:33:38
 * @Description:socket消息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Component
public class MessageEventHandler {

    /**
     * socketIo服务器
     */
    public SocketIOServer socketIoServer;

    public static Map<String, Set<SocketClientInfo>> mapClient = new HashMap<String, Set<SocketClientInfo>>();

    static Map<String, Set<SocketClientInfo>> mapClientBak = new HashMap<String, Set<SocketClientInfo>>();

    static final int limitSeconds = 60;

    /**
     * 多用户
     */
    private static List<SocketClientInfo> tipManyUserList = new ArrayList<SocketClientInfo>();
    /**
     * 多会话
     */
    private static List<SocketClientInfo> tipManySessionList = new ArrayList<SocketClientInfo>();


    public static Map<String, SocketIOClient> keyAndLoginUser = new HashMap<>();

    /**
     * 多用户黑名单
     */
    private static Set<String> manyUserBlack = new HashSet<String>();
    /**
     * 多会话黑名单
     */
    private static Set<String> manySessionBlack = new HashSet<String>();

    /**
     * 和黑名单相关的sessionKey的map
     */
    private static Map<String, Set<String>> relationBlack = new HashMap<String, Set<String>>();

    /**
     * 恢复黑名单map
     */
    private static Map<String, Set<String>> recoveryManyUserBlack = new HashMap<String, Set<String>>();

    /**
     * 恢复黑名单map
     */
    private static Map<String, Set<String>> recoveryManySessionBlack = new HashMap<String, Set<String>>();

    /**
     * 组-组内客户端数
     **/
    private static Map<String, Integer> groupMap = new HashMap<String, Integer>();


    @Autowired
    public MessageEventHandler(SocketIOServer server) {
        this.socketIoServer = server;
    }

    /**
     * @param client
     * @author: lip
     * @date: 2018年7月3日 上午8:35:43
     * @Description: 连接时调用的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        String dateTime = new DateTime().toString("hh:mm:ss");
        try {


            //添加二维码关联关系
            addKeyAndLoginUser(client);
            String sessionKey = client.getHandshakeData().getSingleUrlParam("sessionkey");

            if (sessionKey != null && !"undefined".equals(sessionKey) && !sessionKey.equals("null")) {
                sessionKey = AESUtil.Decrypt(sessionKey, AESUtil.KEY_Secret);

                if (sessionKey.split("_").length >= 3) {// 三个key值
                    // 用于判断是否同一个浏览器
                    String sessionId = sessionKey.split("_")[0];
                    // 用于判断是否同一个账号
                    String useraccount = sessionKey.split("_")[1];
                    // 用于判断是否同一个系统
                    String userId = sessionKey.split("_")[2];
                    String groupId = sessionKey.split("_")[3];

                    SocketClientInfo newClientInfo = new SocketClientInfo();
                    newClientInfo.setGroupId(groupId);
                    newClientInfo.setSessionId(sessionId);
                    newClientInfo.setUserId(userId);
                    newClientInfo.setSocketIOClient(client);
                    newClientInfo.setUserAccount(useraccount);
                    // 当前登录信息集合
                    JSONObject jsonObject = RedisTemplateUtil.getCache(sessionId, JSONObject.class);
                    // 断开时清空
                    boolean isAdd = true;

                    Set<SocketClientInfo> bakSets = mapClientBak.get(sessionKey);
                    if (bakSets != null) {
                        mapClient.put(sessionKey, bakSets);

                    }
                    Set<String> removeKey = new HashSet<String>();
                    if (mapClient.size() == 0) {// 服务器第一次连接
                        if (jsonObject != null && useraccount.equals(jsonObject.get("useraccount"))) {// 登录信息有效
                            client.sendEvent("messageSever", dateTime, MessageInfo.successMark);
                        } else {// 登录信息无效
                            client.sendEvent("messageSever", dateTime, MessageInfo.noSession);
                        }
                    } else {// 非第一次连接
                        // 根据sessoinKey恢复黑名单
                        Set<String> recoveryManyUserBlackSet = recoveryManyUserBlack.get(sessionKey);
                        Set<String> recoveryManySessionBlackSet = recoveryManySessionBlack.get(sessionKey);

                        if (recoveryManyUserBlackSet != null) {
                            manyUserBlack.addAll(recoveryManyUserBlackSet);
                        }
                        if (recoveryManySessionBlackSet != null) {
                            manySessionBlack.addAll(recoveryManySessionBlackSet);
                        }

                        if (manyUserBlack.contains(sessionKey)) {// 在多用户黑名单中
                            tipManyUserList.add(newClientInfo);
                            isAdd = false;
                        } else if (manySessionBlack.contains(sessionKey)) {// 在多session黑名单中
                            tipManySessionList.add(newClientInfo);
                            isAdd = false;
                        } else {// 不在黑名单中
                            if (jsonObject != null && useraccount.equals(jsonObject.get("useraccount"))) {// 登录信息有效
                                // 1，当前客户端发送连接成功标记
                                client.sendEvent("messageSever", dateTime, MessageInfo.successMark);

                                // 遍历mapClient

                                for (String sKey : mapClient.keySet()) {
                                    Set<SocketClientInfo> sockets = mapClient.get(sKey);
                                    if (sKey.split("_")[0].equals(sessionId)) {// 同一个浏览器
                                        if (sKey.split("_")[3].equals(groupId)) {// 同一组：统一系统添加到当前中
                                            sockets.add(newClientInfo);
                                            mapClient.put(sKey, sockets);
                                            // 组内客户端+1
                                            Integer currNum = groupMap.get(sKey) + 1;
                                            groupMap.put(sKey, currNum);

                                            isAdd = false;
                                            break;
                                        } else {// 不同组：踢出原来的，放入最新的
                                            for (SocketClientInfo socketClientInfo : sockets) {
                                                tipManyUserList.add(socketClientInfo);
                                                manyUserBlack.add(sKey);

                                                Set<String> blackSet = relationBlack.get(sessionKey);
                                                if (blackSet == null) {
                                                    blackSet = new HashSet<String>();
                                                }
                                                blackSet.add(sKey);
                                                relationBlack.put(sessionKey, blackSet);

                                            }
                                            removeKey.add(sKey);
                                        }
                                    } else {// 不同浏览器
                                        if (sKey.split("_")[1].equals(useraccount)) {
                                            for (SocketClientInfo socketClientInfo : sockets) {
                                                tipManySessionList.add(socketClientInfo);
                                                manySessionBlack.add(sKey);

                                                Set<String> blackSet = relationBlack.get(sessionKey);
                                                if (blackSet == null) {
                                                    blackSet = new HashSet<String>();
                                                }
                                                blackSet.add(sKey);
                                                relationBlack.put(sessionKey, blackSet);
                                            }
                                            removeKey.add(sKey);
                                        }
                                    }
                                }

                            } else {// 登录信息无效

                                Set<SocketClientInfo> sockets = mapClient.get(sessionKey);
                                if (sockets != null) {

                                    for (SocketClientInfo socketIOClient : sockets) {
                                        if (socketIOClient != null) {
                                            socketIOClient.getSocketIOClient().sendEvent("messageSever", dateTime, MessageInfo.noSession);
                                        }
                                    }
                                    mapClient.remove(sessionKey);
                                    Integer currNum = groupMap.get(sessionKey) + 1;
                                    groupMap.put(sessionKey, currNum);
                                }
                                client.sendEvent("messageSever", dateTime, MessageInfo.noSession);

                                isAdd = false;
                            }
                        }
                    }

                    // 是否添加当前客户端
                    if (isAdd) {
                        Set<SocketClientInfo> socketsTemp = new HashSet<SocketClientInfo>();
                        socketsTemp.add(newClientInfo);
                        mapClient.put(sessionKey, socketsTemp);
                        groupMap.put(sessionKey, 1);

                    }
                    // 处理mapClient要删除的key
                    if (removeKey.size() > 0) {
                        for (String key : removeKey) {
                            mapClient.remove(key);
                        }
                    }

                    // 处理消息发送
                    if (tipManySessionList.size() > 0) {
                        sendtipManySession();
                    }
                    if (tipManyUserList.size() > 0) {
                        sendtipManyUser();
                    }

                }

            } else {
                client.sendEvent("messageSever", dateTime, MessageInfo.noSession);
            }

        } catch (Exception e) {
            client.sendEvent("messageSever", dateTime, MessageInfo.failMark);
            e.printStackTrace();
        }

    }

    private void addKeyAndLoginUser(SocketIOClient client) {
        String qrCode = client.getHandshakeData().getSingleUrlParam("qrcode");
        if (StringUtils.isNotBlank(qrCode)) {
            keyAndLoginUser.put(qrCode, client);
        }
    }

    /**
     * @param client
     * @throws Exception
     * @author: lip
     * @date: 2018年7月3日 上午8:37:37
     * @Description: 客户端断开时的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) throws Exception {

        //去掉二维码关联关系
        removeKeyAndLoginUser(client);

        String sessionKey = client.getHandshakeData().getSingleUrlParam("sessionkey");

        sessionKey = AESUtil.Decrypt(sessionKey, AESUtil.KEY_Secret);

        if (!(manyUserBlack.contains(sessionKey) || manySessionBlack.contains(sessionKey))) {// 不在黑名单中

            if (groupMap != null && groupMap.get(sessionKey) != null) {

                Integer currNum = groupMap.get(sessionKey) - 1;

                groupMap.put(sessionKey, currNum);
                if (currNum < 1) {// 当前没有客户端时清除当前key，清除黑名单
                    Set<SocketClientInfo> bakSets = mapClient.get(sessionKey);
                    if (bakSets != null) {
                        mapClientBak.put(sessionKey, mapClient.get(sessionKey));
                    }
                    mapClient.remove(sessionKey);
                    Set<String> blackSet = relationBlack.get(sessionKey);

                    Set<String> recoveryManyUserBlackSet = new HashSet<String>();
                    Set<String> recoveryManySessionBlackSet = new HashSet<String>();

                    if (blackSet != null) {
                        for (String string : blackSet) {
                            if (manyUserBlack.contains(string)) {
                                manyUserBlack.remove(string);
                                recoveryManyUserBlackSet.add(string);
                                recoveryManyUserBlack.put(sessionKey, recoveryManyUserBlackSet);
                            }
                            if (manySessionBlack.contains(string)) {
                                manySessionBlack.remove(string);

                                recoveryManySessionBlackSet.add(string);
                                recoveryManySessionBlack.put(sessionKey, recoveryManySessionBlackSet);
                            }

                        }
                    }
                    System.out.println("客户端:" + sessionKey + "断开连接");
                }
            }
        }

        //删除redis中的key值


    }
    private void removeKeyAndLoginUser(SocketIOClient client) {
        String qrCode = client.getHandshakeData().getSingleUrlParam("qrcode");
        if (StringUtils.isNotBlank(qrCode) && keyAndLoginUser.containsKey(qrCode)) {
            keyAndLoginUser.remove(qrCode, client);
        }
    }

    /**
     * @param client
     * @param request
     * @param data
     * @author: lip
     * @date: 2018年7月3日 上午8:38:04
     * @Description: 客户端发来消息处理的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @OnEvent(value = "messageevent")
    public void onEvent(SocketIOClient client, AckRequest request, MessageInfo data) {
        System.out.println("发来消息：" + data.getMsgContent());
        socketIoServer.getClient(client.getSessionId()).sendEvent("messageevent", "back data");
    }

    public static void sendtipManyUser() throws Exception { // 这里就是向客户端推消息了
        String dateTime = new DateTime().toString("hh:mm:ss");

        for (SocketClientInfo socketIOClient : tipManyUserList) {
            if (socketIOClient != null) {
                socketIOClient.getSocketIOClient().sendEvent("messageSever", dateTime, MessageInfo.markManyUser);
            }
        }
        tipManyUserList.clear();
    }

    public static void sendtipManySession() throws Exception { // 这里就是向客户端推消息了
        String dateTime = new DateTime().toString("hh:mm:ss");

        for (SocketClientInfo socketIOClient : tipManySessionList) {
            if (socketIOClient != null) {
                socketIOClient.getSocketIOClient().sendEvent("messageSever", dateTime, MessageInfo.markManySession);

            }
        }

        tipManySessionList.clear();
    }

}