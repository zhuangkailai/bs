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
public interface PollutantCommonService {


    void setPollutantStandardValueDataByParam(List<Map<String,Object>> datalist,Map<String,String> contrast,String  pollutanttype);


    void setAlarmTaskStatusDataByParam(List<Map<String,Object>> datalist,Map<String,String> contrast);


    void setUnexpiredDeviceDevOpsDataByParam(List<Map<String, Object>> datalist,Map<String,String> contrast,String pollutanttype);

}
