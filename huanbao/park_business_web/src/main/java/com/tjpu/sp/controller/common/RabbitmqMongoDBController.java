package com.tjpu.sp.controller.common;

import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.PrintToFile;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.config.rabbitmq.RabbitMqConfig;
import com.tjpu.sp.service.impl.common.rabbitmq.RabbitSender;
import io.netty.util.internal.StringUtil;
import net.sf.json.JSONObject;
import org.bson.Document;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description: 消息队列处理mongodb数据
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/8/5 13:08
 */
@RestController
@RequestMapping("rabbitmqdb")
public class RabbitmqMongoDBController {

    @Autowired
    private RabbitSender rabbitSender;

    @Autowired
    private MongoTemplate mongoTemplate;


    @RequestMapping("/test")
    public void test() {
        String beforeDgimn = "test002";
        String rabbitMQErrorTimesKey = "rabbitMQErrorTimes" + "-" + RabbitMqConfig.POINT_MN_UPDATE_DIRECT_QUEUE + beforeDgimn;
        RedisTemplateUtil.deleteCache(rabbitMQErrorTimesKey);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("oldMN", beforeDgimn);
        jsonObject.put("dgimn", "test001");
        jsonObject.put("monitorpointtype", 5);
        MessageProperties properties = new MessageProperties();
        Message message = new Message(jsonObject.toString().getBytes(), properties);
        rabbitSender.sendMessage(RabbitMqConfig.POINT_UPDATE_DIRECT_EXCHANGE, RabbitMqConfig.POINT__MN_UPDATE_DIRECT_KEY, message);
    }

    @RequestMapping("/sendPointMNUpdateDirectQueue")
    public void sendPointMNUpdateDirectQueue(@RequestJson(value = "jsonobject") Object jsonObject) {
        try {
            //测试数据
            /*JSONObject jsonObject = new JSONObject();
            jsonObject.put("oldMN", "test002");
            jsonObject.put("dgimn", "test001");
            jsonObject.put("monitorpointtype", 5);*/

            MessageProperties properties = new MessageProperties();
            Message message = new Message(jsonObject.toString().getBytes(), properties);
            rabbitSender.sendMessage(RabbitMqConfig.POINT_UPDATE_DIRECT_EXCHANGE, RabbitMqConfig.POINT__MN_UPDATE_DIRECT_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: 监听点位mn号改变时，更新数据库中的数据
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/5 13:12
     */
    @RabbitListener(queues = RabbitMqConfig.POINT_MN_UPDATE_DIRECT_QUEUE)
    @RabbitHandler
    public void taskDirectMessage(Channel channel, Message message) throws Exception {
        String oldMN = "";
        try {
            System.out.println("队列");
            long startTime = System.currentTimeMillis();
            channel.basicQos(1);
            String sendMessage = new String(message.getBody(), "utf-8");
            JSONObject jsonObject = JSONObject.fromObject(sendMessage);
            oldMN = jsonObject.getString("oldMN");
            String newMN = jsonObject.getString("dgimn");
            if (StringUtil.isNullOrEmpty(oldMN) || StringUtil.isNullOrEmpty(newMN)) {
                return;
            }
            updateDBByType(jsonObject.get("monitorpointtype"), oldMN, newMN);
            updateCommonDB(oldMN, newMN);
            long endTime = System.currentTimeMillis();
            System.out.println("队列执行时间：" + (endTime - startTime));
        } catch (Exception e) {
            // 拒绝当前消息，并把消息返回原队列
            String queue = RabbitMqConfig.POINT_MN_UPDATE_DIRECT_QUEUE;
            String rabbitMQErrorTimesKey = "rabbitMQErrorTimes" + "-" + queue + oldMN;
            Integer rabbitMQErrorTimes = RedisTemplateUtil.getCache(rabbitMQErrorTimesKey, Integer.class);
            if (rabbitMQErrorTimes == null) {//第一次处理失败，重新放入队列
                rabbitSender.sendMessage(RabbitMqConfig.POINT_UPDATE_DIRECT_EXCHANGE, RabbitMqConfig.POINT__MN_UPDATE_DIRECT_KEY, message);
                //更新Redis失败次数
                RedisTemplateUtil.putCacheWithExpireTime(rabbitMQErrorTimesKey, 1, RedisTemplateUtil.CAHCE12HOUR);
            } else {//两次都处理失败，不放入队列，记录入日志，并删除当前redis存储的信息
                RedisTemplateUtil.deleteCache(rabbitMQErrorTimesKey);
                String location = System.getProperty("user.dir") + "/data/tmp/更新MN号失败日志.txt";
                PrintToFile.print(location, "队列" + queue + "报错：" + e.getLocalizedMessage());
            }
            e.printStackTrace();
        } finally {//不可多次确认消息
            if (channel.isOpen()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    private void updateCommonDB(String beforeDgimn, String dgimn) {
        List<String> collectionList = new ArrayList<>();
        String latestData = "LatestData";
        String yearFlowData1 = "YearFlowData";
        String monthData = "MonthData";
        String dayData = "DayData";
        String hourData = "HourData";
        String earlyWarnData = "EarlyWarnData";
        String pollutantAlarmInfo = "PollutantAlarmInfo";
        String suddenRiseData = "SuddenRiseData";
        String overData = "OverData";
        String overModel = "OverModel";
        String exceptionData = "ExceptionData";
        String exceptionModel = "ExceptionModel";
        String minuteData = "MinuteData";
        String realTimeData = "RealTimeData";
        collectionList.add(latestData);
        collectionList.add(yearFlowData1);
        collectionList.add(monthData);
        collectionList.add(dayData);
        collectionList.add(hourData);
        collectionList.add(earlyWarnData);
        collectionList.add(pollutantAlarmInfo);
        collectionList.add(suddenRiseData);
        collectionList.add(overData);
        collectionList.add(overModel);
        collectionList.add(exceptionData);
        collectionList.add(exceptionModel);
        collectionList.add(minuteData);
        collectionList.add(realTimeData);
        try {
            //LatestData
            updateMnByDataGatherCode(beforeDgimn, dgimn, latestData, "DataGatherCode");
            collectionList.remove(latestData);

            //YearFlowData
            updateMnByDataGatherCode(beforeDgimn, dgimn, yearFlowData1, "DataGatherCode");
            collectionList.remove(yearFlowData1);

            //MonthData
            updateMnByDataGatherCode(beforeDgimn, dgimn, monthData, "DataGatherCode");
            collectionList.remove(monthData);

            //DayData
            updateMnByDataGatherCode(beforeDgimn, dgimn, dayData, "DataGatherCode");
            collectionList.remove(dayData);

            //HourData
            updateMnByDataGatherCode(beforeDgimn, dgimn, hourData, "DataGatherCode");
            collectionList.remove(hourData);

            //SuddenRiseData
            updateMnByDataGatherCode(beforeDgimn, dgimn, suddenRiseData, "DataGatherCode");
            collectionList.remove(suddenRiseData);

            //PollutantAlarmInfo
            updateMnByDataGatherCode(beforeDgimn, dgimn, pollutantAlarmInfo, "DataGatherCode");
            collectionList.remove(pollutantAlarmInfo);

            //EarlyWarnData
            updateMnByDataGatherCode(beforeDgimn, dgimn, earlyWarnData, "DataGatherCode");
            collectionList.remove(earlyWarnData);

            //4、OverData
            updateMnByDataGatherCode(beforeDgimn, dgimn, overData, "DataGatherCode");
            collectionList.remove(overData);

            //OverModel
            updateMnByDataGatherCode(beforeDgimn, dgimn, overModel, "MN");
            collectionList.remove(overModel);

            //3、ExceptionData
            updateMnByDataGatherCode(beforeDgimn, dgimn, exceptionData, "DataGatherCode");
            collectionList.remove(exceptionData);

            //ExceptionModel
            updateMnByDataGatherCode(beforeDgimn, dgimn, exceptionModel, "MN");
            collectionList.remove(exceptionModel);

            //2、MinuteData
            updateMnByDataGatherCode(beforeDgimn, dgimn, minuteData, "DataGatherCode");
            collectionList.remove(minuteData);

            //1、RealTimeData
            updateMnByDataGatherCode(beforeDgimn, dgimn, realTimeData, "DataGatherCode");
            collectionList.remove(realTimeData);

        } catch (Exception e) {
            writeToInfo(beforeDgimn, collectionList, e);
            e.printStackTrace();
            throw e;
        }
    }

    private void updateMnByDataGatherCode(String beforeDgimn, String dgimn, String collection, String FieldName) {
        long startTime = System.currentTimeMillis();
        Query query = new Query();
        query.addCriteria(Criteria.where(FieldName).is(beforeDgimn));
//        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
//        System.out.println(com.alibaba.fastjson.JSONObject.toJSONString(documents));
        Update update = new Update();
        update.set(FieldName, dgimn);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, collection);
        long endTime = System.currentTimeMillis();
        System.out.println("修改" + collection + "文档，将mn号从" + beforeDgimn + "改为"
                + dgimn+"，执行行数："+updateResult.getModifiedCount() + ",耗时：" + (endTime - startTime) );
    }

    private List<String> updateDBByType(Object monitorpointtypeParam, String beforeDgimn, String dgimn) {
        List<String> collectionList = new ArrayList<>();
        String stationMonthAQIData = "StationMonthAQIData";
        String stationDayAQIData = "StationDayAQIData";
        String stationHourAQIData = "StationHourAQIData";
        String hourAQIData = "HourAQIData"; // 新增
        String minuteAQIData = "MinuteAQIData";
        String waterStationEvaluateData = "WaterStationEvaluateData";
        String yearFlowData = "YearFlowData";
        String monthFlowData = "MonthFlowData";
        String dayFlowData = "DayFlowData";
        String hourFlowData = "HourFlowData";
        collectionList.add(stationMonthAQIData);
        collectionList.add(stationDayAQIData);
        collectionList.add(stationHourAQIData);
        collectionList.add(hourAQIData);
        collectionList.add(minuteAQIData);
        collectionList.add(waterStationEvaluateData);
        collectionList.add(yearFlowData);
        collectionList.add(monthFlowData);
        collectionList.add(dayFlowData);
        collectionList.add(hourFlowData);
        try {
            Integer monitorpointtype = null;
            if (monitorpointtypeParam != null) {
                monitorpointtype = Integer.valueOf(monitorpointtypeParam.toString());
            }
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
                case AirEnum:
                    //StationMonthAQIData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, stationMonthAQIData, "StationCode");
                    collectionList.remove(stationMonthAQIData);

                    //StationDayAQIData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, stationDayAQIData, "StationCode");
                    collectionList.remove(stationDayAQIData);

                    //StationHourAQIData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, stationHourAQIData, "StationCode");
                    collectionList.remove(stationHourAQIData);

                    //HourAQIData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, hourAQIData, "StationCode");
                    collectionList.remove(hourAQIData);

                    //MinuteAQIData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, minuteAQIData, "StationCode");
                    collectionList.remove(minuteAQIData);
                    break;
                case WaterQualityEnum:
                    //WaterStationEvaluateData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, waterStationEvaluateData, "DataGatherCode");
                    collectionList.remove(waterStationEvaluateData);
                    break;
                case SmokeGasEnum:
                case WasteWaterEnum:
                case RainEnum:
                case WasteGasEnum:
                    //YearFlowData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, yearFlowData, "DataGatherCode");
                    collectionList.remove(yearFlowData);

                    //MonthFlowData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, monthFlowData, "DataGatherCode");
                    collectionList.remove(monthFlowData);

                    //DayFlowData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, dayFlowData, "DataGatherCode");
                    collectionList.remove(dayFlowData);

                    //HourFlowData
                    updateMnByDataGatherCode(beforeDgimn, dgimn, hourFlowData, "DataGatherCode");
                    collectionList.remove(hourFlowData);
                    break;

            }
        } catch (Exception e) {
            writeToInfo(beforeDgimn, collectionList, e);
            updateCommonDB(beforeDgimn, dgimn);
            e.printStackTrace();
            throw e;
        }
        return collectionList;
    }

    /**
     * 记录没有更新到的mongo表到日志文件
     *
     * @param beforeDgimn
     * @param collectionList
     */
    private void writeToInfo(String beforeDgimn, List<String> collectionList, Exception e) {
        String rabbitMQErrorTimesKey = "rabbitMQErrorTimes" + "-" + RabbitMqConfig.POINT_MN_UPDATE_DIRECT_QUEUE + beforeDgimn;
        Integer rabbitMQErrorTimes = RedisTemplateUtil.getCache(rabbitMQErrorTimesKey, Integer.class);
        if (rabbitMQErrorTimes != null) { //第一次报错不记录
            StringBuffer str = new StringBuffer("[");
            for (String collectionStr : collectionList) {
                str.append(collectionStr.concat("更新失败；"));
            }
            str.append("]");
            String location = System.getProperty("user.dir") + "/data/tmp/更新MN号失败日志.txt";
            PrintToFile.print(location, "队列" + RabbitMqConfig.POINT_MN_UPDATE_DIRECT_QUEUE + "执行失败：" +
                    str.toString());
        }
    }

}
