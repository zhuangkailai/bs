package com.tjpu.sp.service.environmentalprotection.online;

import com.tjpu.sp.model.common.PageEntity;
import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface OnlineOriginalPacketService {

    /**
     * @author: xsm
     * @date: 2020/1/16 0016 下午 3:33
     * @Description: 根据监测点类型获取原始数据包表头
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutanttype]
     * @throws:
     */
    List<Map<String,Object>> getTableTitleForOriginalPackageList(Integer pollutanttype);

    /**
     * @author: xsm
     * @date: 2020/1/16 0016 下午 3:33
     * @Description: 根据自定义参数获取原始数据表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String,Object> getOriginalDataPackageListDataByParam(Map<String, Object> paramMap);

    PageEntity<Document> getOriginalDataPackageDataByParam(Map<String, Object> paramMap);
}
