package com.tjpu.sp.controller.environmentalprotection.monitorcontrol;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.JSONObjectUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.model.environmentalprotection.monitorcontrol.MonitorPointMonitorControlVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import com.tjpu.sp.service.environmentalprotection.monitorcontrol.MonitorControlService;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @version V1.0
 * @author: xsm
 * @date: 2019年11月27日 下午6:39:29
 * @Description:监测点位监测控制接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */

@RestController
@RequestMapping("monitorControl")
public class MonitorControlController {

    @Autowired
    private MonitorControlService monitorControlService;
    @Autowired
    private RabbitSender rabbitSender;
    @Autowired
    private RabbitmqController rabbitmqController;

    private final String rainTypeCode = CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode() + "";

    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 1:37
     * @Description: 自定义查询条件获取监测控制记录表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMonitorControlLogDataByParamMap", method = RequestMethod.POST)
    public Object getMonitorControlLogDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            Map<String, Object> jsonObject = (Map<String, Object>) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            setSortData(jsonObject);
            List<Map<String, Object>> datalist = monitorControlService.getMonitorControlLogDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 3:40
     * @Description: 设置排序数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setSortData(Map<String, Object> jsonObject) {
        if (jsonObject.get("sortdata") != null) {
            Map<String, Object> sortdata = (Map<String, Object>) jsonObject.get("sortdata");
            for (String key : sortdata.keySet()) {
                jsonObject.put("sortkey", key);
                jsonObject.put("sorttype", sortdata.get(key).equals("ascending") ? "asc" : "desc");
            }
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/27 0027 下午 7:05
     * @Description: 添加检测出控制信息操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "addMonitorControlInfo", method = RequestMethod.POST)
    public Object addMonitorControlInfo(@RequestJson(value = "addformdata") Object addformdata,
                                        HttpSession session) {
        try {
            JSONObject jsonFormData = JSONObject.fromObject(addformdata);
            MonitorPointMonitorControlVO monitorPointMonitorControlVO = JSONObjectUtil.parseStringToJavaObject(jsonFormData.toString(), MonitorPointMonitorControlVO.class);
            String sessionId = session.getId();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            String pkid = UUID.randomUUID().toString();
            String monitorpointid = monitorPointMonitorControlVO.getFkMonitorpointid();
            Date nowTime = new Date();
            WaterOutputInfoVO waterOutputInfoVO = monitorControlService.getWaterOutputInfoVOById(monitorpointid);
            monitorPointMonitorControlVO.setPkId(pkid);
            monitorPointMonitorControlVO.setUpdateuser(username);
            monitorPointMonitorControlVO.setUpdatetime(nowTime);
            monitorPointMonitorControlVO.setDgimn(waterOutputInfoVO.getDgimn());
            monitorPointMonitorControlVO.setFkMonitorpointtypecode(rainTypeCode);

            monitorControlService.insert(monitorPointMonitorControlVO);
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointid", monitorpointid);
            param.put("status", 1);
            //修改雨水排口点位状态
            monitorControlService.updateRainOutPutStatusByParam(param);

            boolean effectiveDate = DataFormatUtil.isEffectiveDate(nowTime, monitorPointMonitorControlVO.getStartmointortime(), monitorPointMonitorControlVO.getStopmointortime());
            Map<String, Object> resultmap = monitorControlService.getMonitorControlInfoById(pkid);
            if (resultmap != null) {
                if (effectiveDate) {//当前时间在范围内
                    String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.RainMonitorMessage.getCode();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("pk_id", resultmap.get("pk_id"));
                    jsonobj.put("outputname", resultmap.get("outputname"));
                    jsonobj.put("pollutionname", resultmap.get("pollutionname"));
                    jsonobj.put("endtime", resultmap.get("EndTime"));
                    jsonobj.put("starttime", resultmap.get("startmointortime"));
                    jsonobj.put("updatetime", resultmap.get("stopmointortime"));
                    jsonobj.put("MN", resultmap.get("dgimn"));
                    jsonobj.put("messagestr", resultmap.get("pollutionname") + "上报雨水排放检测报告");
                    jsonobj.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.RainDischargeMessage.getCode());
                    jsonobj.put("isread", "0");
                    //推送到首页
                    rabbitmqController.sendEmissionControlInfo(jsonobj, messageType);
                }
            }
            //放到消息队列中
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dgimn", waterOutputInfoVO.getDgimn());
            jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(monitorPointMonitorControlVO.getStartmointortime()));
            jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(monitorPointMonitorControlVO.getStopmointortime()));
            jsonObject.put("messagetype", CommonTypeEnum.RabbitMQMessageTypeEnum.RainMonitorMessage.getCode());
            sendMessageToRabbit(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/27 0027 下午 7:05
     * @Description: 添加检测出控制信息操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "updateMonitorControlInfo", method = RequestMethod.POST)
    public Object updateMonitorControlInfo(@RequestJson(value = "updateformdata") Object updateformdata,
                                           HttpSession session) {
        try {


            JSONObject jsonFormData = JSONObject.fromObject(updateformdata);
            MonitorPointMonitorControlVO monitorPointMonitorControlVO = JSONObjectUtil.parseStringToJavaObject(jsonFormData.toString(), MonitorPointMonitorControlVO.class);
            String sessionId = session.getId();
            String username = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
            Date nowTime = new Date();
            monitorPointMonitorControlVO.setUpdateuser(username);
            monitorPointMonitorControlVO.setUpdatetime(nowTime);
            monitorPointMonitorControlVO.setFkMonitorpointtypecode(rainTypeCode);
            String monitorpointid = monitorPointMonitorControlVO.getFkMonitorpointid();
            monitorControlService.updateEntity(monitorPointMonitorControlVO);
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointid", monitorpointid);
            param.put("status", 1);
            //修改雨水排口点位状态
            monitorControlService.updateRainOutPutStatusByParam(param);

           /* boolean effectiveDate = DataFormatUtil.isEffectiveDate(nowTime, monitorPointMonitorControlVO.getStartmointortime(), monitorPointMonitorControlVO.getStopmointortime());
            Map<String, Object> resultmap = monitorControlService.getMonitorControlInfoById(monitorPointMonitorControlVO.getPkId());
            if (resultmap!=null){
                if (effectiveDate) {//当前时间在范围内
                    String messageType = CommonTypeEnum.RabbitMQMessageTypeEnum.RainMonitorMessage.getCode();
                    //推送到首页
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("pk_id",resultmap.get("PK_ID"));
                    jsonobj.put("outputname",resultmap.get("outputname"));
                    jsonobj.put("pollutionname",resultmap.get("pollutionname"));
                    jsonobj.put("endtime",  resultmap.get("EndTime"));
                    jsonobj.put("starttime", resultmap.get("StartTime"));
                    jsonobj.put("updatetime", resultmap.get("UpdateTime"));
                    jsonobj.put("messagestr", resultmap.get("pollutionname")+"上报雨水排放检测报告");
                    jsonobj.put("messagetype",CommonTypeEnum.RabbitMQMessageTypeEnum.RainMonitorMessage.getCode());
                    jsonobj.put("isread","0");
                    JSONObject json = JSONObject.fromObject(resultmap);
                    rabbitmqController.sendEmissionControlInfo(json, messageType);
                }
            }*/
            //放到消息队列中
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("dgimn", monitorPointMonitorControlVO.getDgimn());
            jsonObject.put("starttime", DataFormatUtil.getDateYMDHMS(monitorPointMonitorControlVO.getStartmointortime()));
            jsonObject.put("endtime", DataFormatUtil.getDateYMDHMS(monitorPointMonitorControlVO.getStopmointortime()));
            jsonObject.put("messagetype", CommonTypeEnum.RabbitMQMessageTypeEnum.RainMonitorMessage.getCode());
            sendMessageToRabbit(jsonObject);
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/28 0028 上午 8:51
     * @Description: 发送消息到雨水监控队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void sendMessageToRabbit(JSONObject jsonObject) {
        MessageProperties properties = new MessageProperties();
        Message message = new Message(jsonObject.toString().getBytes(), properties);
        rabbitSender.sendMessage(RabbitMqConfig.RAIN_MONITOR_DIRECT_EXCHANGE, RabbitMqConfig.RAIN_MONITOR_DIRECT_KEY, message);
    }

    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 1:21
     * @Description: 自定义查询参数获取点位监控配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointMonitorControlInfo", method = RequestMethod.POST)
    public Object getMonitorPointMonitorControlInfo(@RequestJson(value = "paramsjson", required = true) Object paramsjson) {
        try {
            Map<String, Object> paramMap = (Map<String, Object>) paramsjson;
            Map<String, Object> result = new HashMap<>();
            setSortData(paramMap);
            List<Map<String, Object>> monitorPointMonitorControlInfo = monitorControlService.getMonitorPointMonitorControlInfo(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(monitorPointMonitorControlInfo);
            long total = pageInfo.getTotal();
            result.put("total", total);
            result.put("datalist", monitorPointMonitorControlInfo);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 1:21
     * @Description: 根据主键获取点位监控信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorControlInfoById", method = RequestMethod.POST)
    public Object getMonitorControlInfoById(@RequestJson(value = "id") String id) {
        try {
            Map<String, Object> resultMap = monitorControlService.getMonitorControlInfoById(id);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/2/25 0025 上午 9:59
     * @Description: 验证停产信息是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/validTime", method = RequestMethod.POST)
    public Object isTableDataHaveInfoByParamMap(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "starttime") String starttime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("starttime", starttime);
            Map<String, Object> lastTimeData = monitorControlService.getLastEndTimeByParamMap(paramMap);
            String flag = "yes";
            if (lastTimeData != null) {    //等于空，可以添加
                Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
                Date endMaxTime = DataFormatUtil.getDateYMDHMS(lastTimeData.get("maxtime").toString());
                if (startDate.before(endMaxTime)) {//开始时间小于最新的结束时间
                    flag = "no";
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/11/28 0028 下午 2:23
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "deleteMonitorControlInfo", method = RequestMethod.POST)
    public Object deleteMonitorControlInfo(@RequestJson(value = "dgimn", required = false) String dgimn,
                                           @RequestJson(value = "pkid") String pkid,
                                           @RequestJson(value = "monitorpointid") String monitorpointid) {
        try {

            monitorControlService.deleteEntity(pkid);
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointid", monitorpointid);
            param.put("status", 0);
            //修改雨水排口点位状态
            monitorControlService.updateRainOutPutStatusByParam(param);
            //放到消息队列中
            if (StringUtils.isNotBlank(dgimn)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dgimn", dgimn);
                jsonObject.put("starttime", "@");
                jsonObject.put("endtime", "@");
                jsonObject.put("messagetype", CommonTypeEnum.RabbitMQMessageTypeEnum.RainMonitorMessage.getCode());
                sendMessageToRabbit(jsonObject);
            }
            return AuthUtil.parseJsonKeyToLower("success", "success");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/09/27 0027 下午 2:15
     * @Description: 自定义查询条件获取雨水排放历史记录（不包括正在排放中）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMonitorControlHistoryLogDataByParamMap", method = RequestMethod.POST)
    public Object getMonitorControlHistoryLogDataByParamMap(@RequestJson(value = "paramsjson") Object paramsJson) {
        try {
            Map<String, Object> jsonObject = (Map<String, Object>) paramsJson;
            Map<String, Object> resultMap = new HashMap<>();
            setSortData(jsonObject);
            List<Map<String, Object>> datalist = monitorControlService.getMonitorControlHistoryLogDataByParamMap(jsonObject);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(datalist);
            long total = pageInfo.getTotal();
            resultMap.put("datalist", datalist);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
