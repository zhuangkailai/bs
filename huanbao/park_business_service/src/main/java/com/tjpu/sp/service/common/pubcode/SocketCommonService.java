package com.tjpu.sp.service.common.pubcode;

import java.util.List;
import java.util.Map;

/**
 * @author: chengzq
 * @date: 2021/3/10 0010 13:21
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public interface SocketCommonService {


    void sendAllClientMessage(String messagemethod,Object messagedata);

    void sendAppointMessageToAppointClient(String messagemethod, List<Map<String,Object>> messageanduserdata);

}
