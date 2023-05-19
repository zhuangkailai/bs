package com.tjpu.sp.service.environmentalprotection.online;

import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OnlineFlowDataService {

    /**
     * @author: zhangzc
     * @date: 2019/8/28 13:21
     * @Description: 获取小时和天的排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getHourAndDayFlowDataByParam(List<String> mns, List<String> pollutantCodes, Date startTime, Date endTime, String unwindFieldName, String collection);

    /**
     * @author: zhangzc
     * @date: 2019/9/2 10:37
     * @Description: 获取浓度或者排放量数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Document> getNDOrPFLDataByParam(String valueFiledName, List<String> mns, List<String> pollutantCodes, Date startTime, Date endTime, String unwindFieldName, String collection, Integer pageSize, Integer pageNum);

    int countNDOrPFLDataByParam(List<String> mns, List<String> pollutantCodes, Date startTime, Date endTime, String unwindFieldName, String collection);


}
