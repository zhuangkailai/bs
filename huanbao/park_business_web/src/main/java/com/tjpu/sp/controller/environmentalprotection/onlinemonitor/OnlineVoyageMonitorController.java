package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.online.OnlineVoyageMonitorService;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RequestMapping("onlineVoyageMonitorController")
@RestController
public class OnlineVoyageMonitorController {

    private final OnlineVoyageMonitorService onlineVoyageMonitorService;

    public OnlineVoyageMonitorController(OnlineVoyageMonitorService onlineVoyageMonitorService) {
        this.onlineVoyageMonitorService = onlineVoyageMonitorService;
    }

    /**
     * @author: zhangzc
     * @date: 2019/9/16 14:27
     * @Description: 条件查询走航监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getVoyageMonitorDataByParam", method = RequestMethod.POST)
    public Object getVoyageMonitorDataByParam(
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Date startTime = DataFormatUtil.parseDate(starttime + ":00");
            Date endTime = DataFormatUtil.parseDate(endtime + ":59");
            List<Document> result = onlineVoyageMonitorService.getVoyageMonitorDataByParam(pollutantcode, startTime, endTime);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}