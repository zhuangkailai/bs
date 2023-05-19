package com.tjpu.sp.service.impl.common.pubcode;


import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.dao.common.pubcode.PubCodeMapper;
import com.tjpu.sp.service.common.micro.AuthSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.common.pubcode.SocketCommonService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SocketCommonServiceImpl implements SocketCommonService {

    @Autowired
    private AuthSystemMicroService authSystemMicroService;

    @Override
    public void sendAllClientMessage(String messagemethod,Object messagedata) {
        //发送消息给指定客户端
        JSONObject socketJson = new JSONObject();
        socketJson.put("messagemethod", messagemethod);
        socketJson.put("messagedata", messagedata);
        authSystemMicroService.sendAllClientMessage(socketJson);
    }

    @Override
    public void sendAppointMessageToAppointClient(String messagemethod, List<Map<String,Object>> messageanduserdata) {
        if (messageanduserdata.size() > 0) {
            //发送消息给指定客户端
            JSONObject socketJson = new JSONObject();
            socketJson.put("messagemethod", messagemethod);
            socketJson.put("messageanduserdata", messageanduserdata);
            authSystemMicroService.sendAppointMessageToAppointClient(socketJson);
        }
    }
}