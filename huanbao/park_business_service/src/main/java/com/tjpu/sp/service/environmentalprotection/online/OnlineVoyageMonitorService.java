package com.tjpu.sp.service.environmentalprotection.online;

import org.bson.Document;

import java.util.Date;
import java.util.List;

public interface OnlineVoyageMonitorService {
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
    List<Document> getVoyageMonitorDataByParam(String pollutantcode, Date starttime, Date endtime);
}
