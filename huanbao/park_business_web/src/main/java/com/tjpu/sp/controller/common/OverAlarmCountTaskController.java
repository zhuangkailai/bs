package com.tjpu.sp.controller.common;

import com.mongodb.bulk.BulkWriteResult;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineDataCountService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description: 报警统计定时任务
 * @Param:
 * @return:
 * @Author: lip
 * @Date: 2022/8/23 8:58
 */
@RestController
@RequestMapping("overAlarmTask")
public class OverAlarmCountTaskController {


    private static final String DB_OverCountData = "OverCountData";
    private static final String dataType = "HourData";

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private OnlineDataCountService onlineDataCountService;

    private List<String> getMnListByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> dataList = deviceStatusService.getDeviceStatusDataByParam(paramMap);
        List<String> mns = dataList.stream().filter(m -> m.get("dgimn") != null)
                .map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
        return mns;
    }

    /**
     * @Description: 统计当天小时报警分钟数
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/8/23 9:07
     */
    @GetMapping("/countOverHourData")
    public void countOverHourData() {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = getMnListByParam(paramMap);
            //当天
            Date nowTime = new Date();
            String startTime = DataFormatUtil.getDateYMD(nowTime) + " 00";
            String endTime = DataFormatUtil.getDateYMDH(nowTime);
            addData(paramMap, startTime, endTime, mns);

            //前一天
            Date beforeDay = DataFormatUtil.getDateYMD(DataFormatUtil.getBeforeByDayTime(1, DataFormatUtil.getDateYMD(nowTime)));
            startTime = DataFormatUtil.getDateYMD(beforeDay) + " 00";
            endTime = DataFormatUtil.getDateYMD(beforeDay) + " 23";
            addData(paramMap, startTime, endTime, mns);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addData(Map<String, Object> paramMap, String startTime, String endTime, List<String> mns) {
        paramMap.put("starttime", startTime + ":00:00");
        paramMap.put("endtime", endTime + ":59:59");
        paramMap.put("dgimns", mns);
        List<Document> documents = onlineDataCountService.getOverModelDataByParam(paramMap);
        if (documents.size() > 0) {
            List<Map<String, Object>> addList = new ArrayList<>();
            Map<String, List<Document>> mnAndList = documents.stream().filter(m -> m.get("DataGatherCode") != null)
                    .collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            for (String mnIndex : mnAndList.keySet()) {
                addList.addAll(getMinuteDataForHour(startTime, endTime, mnAndList.get(mnIndex), mnIndex));
            }
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("MonitorTime", "MonitorTime");
            batchUpdateOrAdd(addList, DB_OverCountData, "DataGatherCode", "DataGatherCode", queryMap);
        }


    }


    private List<Map<String, Object>> getMinuteDataForHour(String starttime,
                                                           String endtime,
                                                           List<Document> documents,
                                                           String mnCommon) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Map<String, Long>> hourAndCodeAndMinute = new HashMap<>();
        Map<Integer, List<List<Date>>> hourAndStart_End = new HashMap<>();
        Date startT;
        Date endT;
        Integer Shour;
        Integer Ehour;
        String pollutantCode;
        int hourN;
        String hourString;
        for (Document document : documents) {
            startT = document.getDate("FirstOverTime");
            endT = document.getDate("LastOverTime");
            Shour = DataFormatUtil.getDateHourNum(startT);
            Ehour = DataFormatUtil.getDateHourNum(endT);
            pollutantCode = document.getString("PollutantCode");
            //判断是否跨小时
            hourN = Ehour - Shour;
            if (hourN == 0) {//否
                setMapToNum(pollutantCode, startT, startT, endT, hourAndCodeAndMinute);
                setHourAndSE(Shour, startT, endT, hourAndStart_End);
            } else {//是
                for (int i = Shour; i < Ehour; i++) {
                    if (i < 10) {
                        hourString = " 0" + i;
                    } else {
                        hourString = " " + i;
                    }
                    if (i != Shour) {
                        startT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMD(startT) + hourString + ":00:00");
                    }
                    Date endTE = DataFormatUtil.getDateYMDHMS((DataFormatUtil.getDateYMD(startT) + hourString + ":59:59"));
                    setMapToNum(pollutantCode, startT, startT, endTE, hourAndCodeAndMinute);
                    setHourAndSE(i, startT, endTE, hourAndStart_End);
                }
                startT = DataFormatUtil.getDateYMDHMS(DataFormatUtil.getDateYMDH(endT) + ":00:00");
                setMapToNum(pollutantCode, endT, startT, endT, hourAndCodeAndMinute);
                setHourAndSE(Ehour, startT, endT, hourAndStart_End);
            }
        }
        Map<String, Long> codeAndMinute;
        Integer subtime;
        List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);
        hours.add(endtime);
        String dayDate = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH", "yyyy-MM-dd");
        Map<String, Integer> hourAndM = setHourAndM(hourAndStart_End);
        String ymdh;

        for (String hourIndex : hourAndM.keySet()) {
            ymdh = dayDate + " " + hourIndex;
            codeAndMinute = hourAndCodeAndMinute.get(ymdh);
            if (codeAndMinute != null) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("DataGatherCode", mnCommon);
                resultMap.put("MonitorTime", DataFormatUtil.getDateYMDH(ymdh));
                List<Map<String, Object>> pollutantList = new ArrayList<>();
                if (codeAndMinute != null) {
                    for (String code : codeAndMinute.keySet()) {
                        subtime = codeAndMinute.get(code).intValue();
                        Map<String, Object> pollutant = new HashMap<>();
                        pollutant.put("PollutantCode", code);
                        pollutant.put("AlarmDuration", subtime);
                        pollutantList.add(pollutant);
                    }
                }
                resultMap.put("AlarmDuration", hourAndM.get(hourIndex));
                resultMap.put("DataList", pollutantList);
                resultMap.put("DataType", dataType);
                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    private void setMapToNum(String pollutantCode, Date ymdhD, Date startT, Date endT,
                             Map<String, Map<String, Long>> hourAndCodeAndMinute) {
        long minuteNum = DataFormatUtil.getDateMinutes(startT, endT) == 0 ? 1 : DataFormatUtil.getDateMinutes(startT, endT);


        String ymdh = DataFormatUtil.getDateYMDH(ymdhD);
        Map<String, Long> codeAndMinute;
        if (hourAndCodeAndMinute.containsKey(ymdh)) {
            codeAndMinute = hourAndCodeAndMinute.get(ymdh);
        } else {
            codeAndMinute = new HashMap<>();
        }
        if (codeAndMinute.containsKey(pollutantCode)) {
            codeAndMinute.put(pollutantCode, codeAndMinute.get(pollutantCode) + minuteNum);
        } else {
            codeAndMinute.put(pollutantCode, minuteNum);
        }
        if (codeAndMinute.get(pollutantCode) > 60) {
            codeAndMinute.put(pollutantCode, 60l);
        }
        hourAndCodeAndMinute.put(ymdh, codeAndMinute);
    }

    private void setHourAndSE(Integer Shour, Date startT, Date endT, Map<Integer, List<List<Date>>> hourAndStart_End) {
        List<List<Date>> S_EList;
        if (hourAndStart_End.containsKey(Shour)) {
            S_EList = hourAndStart_End.get(Shour);
        } else {
            S_EList = new ArrayList<>();
        }
        S_EList.add(Arrays.asList(startT, endT));
        hourAndStart_End.put(Shour, S_EList);
    }

    private Map<String, Integer> setHourAndM(Map<Integer, List<List<Date>>> hourAndStart_end) {
        Map<String, Integer> hourAndNum = new HashMap<>();
        Date startTime;
        Date endTime;
        String mS;
        String mE;
        List<List<Date>> dateList;
        String hourS;
        List<String> minuteNums;
        List<String> sumNum;
        String ymdhms;
        for (Integer hour : hourAndStart_end.keySet()) {
            dateList = hourAndStart_end.get(hour);
            minuteNums = new ArrayList<>();
            for (int i = 0; i < dateList.size(); i++) {
                List<Date> dates = dateList.get(i);
                startTime = dates.get(0);
                endTime = dates.get(1);
                mS = DataFormatUtil.getDateYMDHM(startTime);
                mE = DataFormatUtil.getDateYMDHM(endTime);
                ymdhms = DataFormatUtil.getDateYMDHMS(endTime);
                sumNum = DataFormatUtil.getYMDHM2Between(mS, mE);
                if (sumNum.size() == 0) {
                    sumNum.add(mE);
                }
                if ( ymdhms.contains(":59:59")) {
                    sumNum.add(mE + ":59");
                }
                minuteNums.addAll(sumNum);
            }
            hourS = DataFormatUtil.FormatDateOneToOther(hour.toString(), "H", "HH");
            minuteNums = minuteNums.stream().distinct().collect(Collectors.toList());
            hourAndNum.put(hourS, minuteNums.size());
        }
        return hourAndNum;

    }


    private void batchUpdateOrAdd(List<Map<String, Object>> list, String collection, String TKey, String SKey, Map<String, String> queryMap) {
        // list为要更新或者新增的数据，使用时填充上自己的数据集合
        List<Pair<Query, Update>> updateList = new ArrayList<>(list.size());
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        list.forEach(data -> {
            Criteria criteria = new Criteria(TKey).is(data.get(SKey));

            for (String key : queryMap.keySet()) {
                criteria.and(key).is(data.get(queryMap.get(key)));
            }
            Query query = new Query(
                    criteria
            );
            Update update;
            Document doc = new Document();
            mongoTemplate.getConverter().write(data, doc);
            update = Update.fromDocument(new Document("$set", doc));
            Pair<Query, Update> updatePair = Pair.of(query, update);
            updateList.add(updatePair);
        });
        operations.upsert(updateList);
        BulkWriteResult result = operations.execute();
    }


}
