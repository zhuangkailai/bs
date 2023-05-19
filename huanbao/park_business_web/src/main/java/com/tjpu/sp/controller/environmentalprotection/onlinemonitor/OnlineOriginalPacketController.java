package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.environmentalprotection.online.OnlineOriginalPacketService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author: lip
 * @date: 2021/3/11 10:57
 * @Description: 在线原始数据处理类
 */
@RestController
@ControllerAdvice
@RequestMapping("onlineOriginalPacket")
public class OnlineOriginalPacketController {

    private final OnlineOriginalPacketService onlineOriginalPacketService;

    public OnlineOriginalPacketController(OnlineOriginalPacketService onlineOriginalPacketService) {
        this.onlineOriginalPacketService = onlineOriginalPacketService;
    }

    /**
     * @author: lip
     * @date: 2021/3/11 11:19
     * @Description: 获取原始数据分页数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "getOriginalDataListByParam", method = RequestMethod.POST)
    public Object getOriginalDataListByParam(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum
    ) {
        try {

             Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mnlike",  dgimn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("direction", "desc");

            PageEntity<Document> pageEntity = onlineOriginalPacketService.getOriginalDataPackageDataByParam(paramMap);
            List<Map<String,Object>> dataList = new ArrayList<>();
            List<Document> documents = pageEntity.getListItems();
            for (Document document:documents){
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("id",document.get("_id").toString());
                dataMap.put("mn",document.getString("MN"));
                dataMap.put("picktime",DataFormatUtil.getDateYMDHMS(document.getDate("PacketTime")));
                dataMap.put("monitordata",document.getString("Packet"));
                dataList.add(dataMap);
            }
            resultMap.put("total",pageEntity.getTotalCount());
            resultMap.put("datalist",dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
