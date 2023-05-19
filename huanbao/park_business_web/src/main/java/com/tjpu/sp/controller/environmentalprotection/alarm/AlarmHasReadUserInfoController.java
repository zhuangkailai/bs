package com.tjpu.sp.controller.environmentalprotection.alarm;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.environmentalprotection.alarm.AlarmHasReadUserInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: lip
 * @date: 2019/7/16 0016 上午 9:44
 * @Description: 报警已读用户信息处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RequestMapping("alarmhasreaduserinfo")
@RestController
public class AlarmHasReadUserInfoController {

    @Autowired
    private AlarmHasReadUserInfoService alarmHasReadUserInfoService;



    /**
     * @author: lip
     * @date: 2019/7/16 0016 上午 9:53
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "setHasReadAlarmInfoByParams", method = RequestMethod.POST)
    public Object setHasReadAlarmInfoByParams(
            @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "remindtype") Integer remindtype,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "monitortime") String monitortime,
            HttpSession session) throws Exception {
        try {
            //提醒类型：1表示浓度突变，2表示排放量突变，3表示预警，4表示异常，5表示超限,6表示排放量许可

            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode", String.class);
            if (StringUtils.isNotBlank(usercode)) {
                //根据提醒类型获取监测时间数组
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("dgimns", Arrays.asList(dgimn));
                paramMap.putIfAbsent("datatype", datatype);
                paramMap.putIfAbsent("remindtype", remindtype);
                paramMap.putIfAbsent("monitortime", monitortime);
                paramMap.putIfAbsent("usercode", usercode);
                paramMap.putIfAbsent("pollutantcode", pollutantcode);
                alarmHasReadUserInfoService.addHasReadAlarmInfoByParams(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/16 0016 上午 9:53
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "setManyPointHasReadByParams", method = RequestMethod.POST)
    public Object setManyPointHasReadByParams(
            @RequestJson(value = "pointlist") Object pointlist, HttpSession session) throws Exception {
        try {
            //提醒类型：1表示浓度突变，2表示排放量突变，3表示预警，4表示异常，5表示超限,6表示排放量许可
            String usercode = RedisTemplateUtil.getRedisCacheDataByToken("usercode",  String.class);
            if (StringUtils.isNotBlank(usercode)) {
                //根据提醒类型获取监测时间数组
                List<Map<String, Object>> pointList = (List<Map<String, Object>>) pointlist;
                if (pointList.size() > 0) {
                    Map<String, Object> paramMap = new HashMap<>();
                    List<String> mns;
                    List<Integer> remindtypes;
                    String monitortime;
                    for (Map<String, Object> map : pointList) {
                        if (map.get("dgimns") != null && map.get("monitortime") != null && map.get("remindtype") != null) {
                            mns = (List<String>) map.get("dgimns");
                            if(mns.size()>0){
                                paramMap.put("dgimns", mns);
                                monitortime = map.get("monitortime").toString();
                                paramMap.putIfAbsent("monitortime", monitortime);
                                remindtypes = (List<Integer>) map.get("remindtype");
                                if (remindtypes.size() > 0) {
                                    for (Integer remindtype : remindtypes) {
                                        paramMap.put("remindtype", remindtype);
                                        if (remindtype == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()
                                                || remindtype == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {
                                            paramMap.put("timetype", "hourData");
                                        }
                                        paramMap.putIfAbsent("usercode", usercode);
                                        alarmHasReadUserInfoService.addHasReadAlarmInfoByParams(paramMap);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
