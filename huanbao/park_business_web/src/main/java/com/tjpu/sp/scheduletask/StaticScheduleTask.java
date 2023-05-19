package com.tjpu.sp.scheduletask;

import com.tjpu.pk.common.utils.AESUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.controller.common.OverAlarmCountTaskController;
import com.tjpu.sp.controller.common.ScheduleMeteTaskController;
import com.tjpu.sp.controller.common.ScheduleTaskController;
import com.tjpu.sp.service.common.user.UserService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务*/
public class StaticScheduleTask {

    @Autowired
    private ScheduleTaskController scheduleTaskController;

    @Autowired
    private OverAlarmCountTaskController overAlarmCountTaskController;

    @Autowired
    private UserService userService;

    public static List<JSONObject> alarmList = new ArrayList<>();


    public static List<JSONObject> exceptionList = new ArrayList<>();


    /**
     * @author: lip
     * @date: 2019/10/9 0009 上午 11:50
     * @Description: 间隔1小时发送预报数据到前端
     * 1，获取接口需要参数；
     * 2，调用python服务接口；
     * 3，判断是否需要推送；
     * 4，推送服务；
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    private void sendForecastDataToFront() {
        //scheduleTaskController.startScheduleTask();
    }


    /**
     * @Description: 每小时05分时统计小时报警数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/23 9:54
     */
    @Scheduled(cron = "0 05 0-23 * * ? ")
    private void countHourOverData() {
        overAlarmCountTaskController.countOverHourData();
    }


    /**
     * @author: lip
     * @date: 2020/3/13 0013 上午 9:17
     * @Description: 每天晚上9点修改密码并通知
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    /*@Scheduled(cron = "0 5/25 * * * ? ")
    private void updatePassword() {
        //生成密码、加密密码
        String password = DataFormatUtil.generateRandomArray(6);
        String jm_password = AESUtil.Encrypt(password, AESUtil.KEY_Secret);
        //修改密码
        String userAccount = "public";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userPwd", jm_password);
        paramMap.put("userRemark", password);
        paramMap.put("userAccount", userAccount);
        // userService.updatePassword(paramMap);

    }*/



   /* @Scheduled(cron = "0 00 00 * * ?")
    private void fixedTimeParseMeteData() {

    }*/

    /**
     * @author: lip
     * @date: 2021/2/2 0002 上午 10:05
     * @Description: 小时数据图片推送
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Scheduled(cron = "0 20 0-23 * * ? ")
    private void hourDataImgSendTask() {
        if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("chrome.driver.path"))) {
            scheduleTaskController.sendHourDataImgToWeChart();
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/2 0002 上午 10:05
     * @Description: 废水、污水、设备问题统计查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Scheduled(cron = "0 0 08 1/1 * ? ")
    //@Scheduled(cron = "0 0/2 11 1/1 * ? ")
    private void waterDayDataSendTask() {
        if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("chrome.driver.path"))) {
            scheduleTaskController.waterDayDataSendTask();
            scheduleTaskController.deviceProblemRecord();
        }
    }

    /**
     * @author: mmt
     * @date: 2022/9/19 0002 上午 10:05
     * @Description: 每月1号凌晨三点生成污水进水口超标数据计算后的值存到数据库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Scheduled(cron = "0 0 3 1 * ?")
//    @Scheduled(fixedRate = 10 * 10 * 1000)
    private void waterSimilarityAnalysisSendTask() {
        scheduleTaskController.waterSimilarityAnalysisSendTask();
    }

    /**
     * @author: lip
     * @date: 2021/2/2 0002 上午 10:05
     * @Description: 每五分钟检查一次
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    //@Scheduled(cron = "0 0/1 * * * ? ")
    private void alarmDataImgSendTask() {
        if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("send.type"))) {
            String nowDay = DataFormatUtil.getDateYMD(new Date());
            String pushWeType = DataFormatUtil.parseProperties("push.over.type");
            List<String> types = StringUtils.isNotBlank(pushWeType) ? Arrays.asList(pushWeType.split(",")) : new ArrayList<>();
            for (JSONObject jsonObject : alarmList) {
                if (!types.contains(jsonObject.getString("DataType"))) {
                    continue;
                }
                try {
                    if (nowDay.equals(DataFormatUtil.FormatDateOneToOther(
                            jsonObject.getString("DateTime"), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"))) {
                        if (CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode().equals(jsonObject.getString("OnlineStatus"))) {//离线
                            if (!"yes".equals(jsonObject.get("send"))) {
                                scheduleTaskController.sendMessageToWeChartUser(jsonObject);
                                jsonObject.put("send", "yes");
                            }
                        } else {//报警
                            if (!"yes".equals(jsonObject.get("send"))) {
                                scheduleTaskController.sendOverToWeChartGroup(jsonObject);
                                scheduleTaskController.sendChangeToWeChartGroup(jsonObject);
                                jsonObject.put("send", "yes");
                            }
                        }
                    } else {
                        jsonObject.put("send", "yes");
                    }
                } catch (Exception e) {
                    jsonObject.put("send", "no");
                    e.printStackTrace();
                }
            }
            List<JSONObject> noSend = new ArrayList<>();
            for (JSONObject jsonObject : alarmList) {
                if (!"yes".equals(jsonObject.get("send"))) {
                    noSend.add(jsonObject);
                }
            }
            alarmList = noSend;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/2 0002 上午 10:05
     * @Description: 每五分钟检查一次
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    private void overDataImgSendTask() {
        if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("send.type"))) {
            String nowDay = DataFormatUtil.getDateYMD(new Date());
            String pushWeType = DataFormatUtil.parseProperties("push.over.type");
            List<String> types = StringUtils.isNotBlank(pushWeType) ? Arrays.asList(pushWeType.split(",")) : new ArrayList<>();
            for (JSONObject jsonObject : alarmList) {
                if (types.contains(jsonObject.getString("DataType"))) {
                    try {
                        if (nowDay.equals(DataFormatUtil.FormatDateOneToOther(
                                jsonObject.getString("DateTime"), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"))) {
                            if (!"yes".equals(jsonObject.get("send"))) {
                                scheduleTaskController.sendOverToWeChartGroup(jsonObject);
                                jsonObject.put("send", "yes");
                            }
                        } else {
                            jsonObject.put("send", "yes");
                        }
                    } catch (Exception e) {
                        jsonObject.put("send", "no");
                        e.printStackTrace();
                    }
                }

            }
            List<JSONObject> noSend = new ArrayList<>();
            for (JSONObject jsonObject : alarmList) {
                if (!"yes".equals(jsonObject.get("send"))) {
                    noSend.add(jsonObject);
                }
            }
            alarmList = noSend;
        }
    }

    /**
     * @author: lip
     * @date: 2021/2/2 0002 上午 10:05
     * @Description: 每五分钟检查一次
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    private void exceptionDataImgSendTask() {
        if (StringUtils.isNotBlank(DataFormatUtil.parseProperties("send.type"))) {
            String nowDay = DataFormatUtil.getDateYMD(new Date());
            String pushWeType = DataFormatUtil.parseProperties("push.exception.type");
            List<String> types = StringUtils.isNotBlank(pushWeType) ? Arrays.asList(pushWeType.split(",")) : new ArrayList<>();
            for (JSONObject jsonObject : exceptionList) {
                if (!types.contains(jsonObject.getString("DataType"))) {
                    continue;
                }
                try {
                    if (nowDay.equals(DataFormatUtil.FormatDateOneToOther(
                            jsonObject.getString("DateTime"), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"))) {
                        if (!"yes".equals(jsonObject.get("send"))) {
                            scheduleTaskController.sendExceptionToWeChartGroup(jsonObject);
                            jsonObject.put("send", "yes");
                        }
                    } else {
                        jsonObject.put("send", "yes");
                    }
                } catch (Exception e) {
                    jsonObject.put("send", "no");
                    e.printStackTrace();
                }
            }
            List<JSONObject> noSend = new ArrayList<>();
            for (JSONObject jsonObject : exceptionList) {
                if (!"yes".equals(jsonObject.get("send"))) {
                    noSend.add(jsonObject);
                }
            }
            exceptionList = noSend;
        }
    }

    /**
     * @Description: 所有点位离线提醒
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/8/7 14:36
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    private void isAllOffLineStatus() throws InterruptedException {
        scheduleTaskController.isAllOffLineStatus();
    }

}