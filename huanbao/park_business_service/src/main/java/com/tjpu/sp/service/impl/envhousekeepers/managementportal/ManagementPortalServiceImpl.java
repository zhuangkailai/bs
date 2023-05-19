package com.tjpu.sp.service.impl.envhousekeepers.managementportal;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.envhousekeepers.checkentinfo.CheckEntInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.service.envhousekeepers.managementportal.ManagementPortalService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.OverAlarmEnum;

@Transactional
@Service
public class ManagementPortalServiceImpl implements ManagementPortalService {

    @Autowired
    private CheckEntInfoMapper checkEntInfoMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;
    private final String exceptionModelCollection = "ExceptionModel";
    private final String overModelCollection = "OverModel";

    /**
     * @author: xsm
     * @date: 2021/09/07 0007 下午 2:23
     * @Description: 获取管委会监督检查巡查任务提醒(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countManagementCommitteePatrolDataNum() {
        return checkEntInfoMapper.countManagementCommitteePatrolDataNum();
    }

    /**
     * @author: xsm
     * @date: 2021/09/08 0008 上午 09:56
     * @Description: 获取年度问题企业排行(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getProblemDataGroupByEntForYearRank(Map<String, Object> param) {
        List<Map<String, Object>> entlist = checkProblemExpoundMapper.getProblemDataGroupByEntForYearRank(param);
        if (entlist!=null&&entlist.size()>0){
            int alltotal = 0;
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                alltotal +=total;
            }
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                int num1 = map.get("yzgnum")!=null?Integer.valueOf(map.get("yzgnum").toString()):0;
                if (total>0){
                    map.put("zg_proportion",(num1 * 100/total)+"");
                }else{
                    map.put("zg_proportion","-");
                }
                if (alltotal>0){
                    map.put("proportion",(total * 100/alltotal)+"");
                }else{
                    map.put("proportion","-");
                }
            }
        }
        return entlist;
    }

    /**
     * @author: xsm
     * @date: 2021/09/08 0008 下午 16:34
     * @Description: 统计近一个月企业自查问题情况(管委会端)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countLastMonthEntProblemDataSituation(Map<String, Object> param) {
        return checkProblemExpoundMapper.countLastMonthEntProblemDataSituation(param);
    }

    @Override
    public List<Map<String, Object>> getEntSelfExaminationSituationByParam(Map<String, Object> param) {
        List<Map<String, Object>> entlist = checkProblemExpoundMapper.getEntSelfExaminationSituationByParam(param);
        if (entlist!=null&&entlist.size()>0){
            for (Map<String, Object> map:entlist){
                int total = map.get("totalmnum")!=null?Integer.valueOf(map.get("totalmnum").toString()):0;
                int num1 = map.get("yzgnum")!=null?Integer.valueOf(map.get("yzgnum").toString()):0;
                if (total>0){
                    map.put("zg_proportion",(num1 * 100/total)+"");
                }else{
                    map.put("zg_proportion","-");
                }

            }
        }
        return entlist;
    }

    /**
     * @author: xsm
     * @date: 2021/09/14 0014 上午 9:52
     * @Description:统计所有未完成检查问题和本月新增问题个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String,Object> countNotCompleteCheckProblemNum(Map<String, Object> paramMap) {
        return checkProblemExpoundMapper.countNotCompleteCheckProblemNum(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/09/16 16:43
     * @Description: 获取超标或异常报警的mn号
     * @param:
     * @return:
     * @throws:
     */
    @Override
    public List<String> getOverOrExceptionAlarmMnsByParams(Set<String> mns, String daytime, Integer remindtype) {
        List<AggregationOperation> operations = new ArrayList<>();
        Date startDate = DataFormatUtil.parseDate(daytime+ " 00:00:00");
        Date endDate = DataFormatUtil.parseDate(daytime+ " 23:59:59");
        String timefield = "";
        String collection = "";
        if (remindtype == OverAlarmEnum.getCode()) {  //超标
            timefield = "FirstOverTime";
            collection = overModelCollection;
        } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
            timefield = "FirstExceptionTime";
            collection = exceptionModelCollection;
        }
        Criteria criteria = new Criteria();
        if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
        } else if (remindtype == OverAlarmEnum.getCode()){
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
        }
        operations.add(Aggregation.match(criteria));
        if (remindtype == ExceptionAlarmEnum.getCode()) {    //若查异常数据 则需返回异常类型（零值异常、连续值异常、超限异常、无流量异常等）
            operations.add(Aggregation.project("MN", timefield)
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(
                    Aggregation.group("MN") );
        } else { //超标
            operations.add(Aggregation.project("MN", timefield)
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            operations.add(
                    Aggregation.group("MN") );
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        List<String> alarmmns = new ArrayList<>();
        for (Document document : mappedResults) alarmmns.add(document.getString("_id"));
        return alarmmns;
    }

    /**
     * @author: xsm
     * @date: 2021/09/16 0016 下午 15:57
     * @Description:整合超标时段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getOverOrExceptionDataByParam(Integer remindtype, Map<String, Object> paramMap) {
       try{
        //超标  污染物算总报警时长
        //异常   单个污染物算报警时长
        List<Map<String,Object>> result = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        Map<String,Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String end = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(end);
        String timefield = "";
        String collection = "";
        String lasttimestr = "";
        if (remindtype == OverAlarmEnum.getCode()) {  //超标
            timefield = "FirstOverTime";
            collection = overModelCollection;
            lasttimestr = "LastOverTime";
        } else if (remindtype == ExceptionAlarmEnum.getCode()) {//异常
            timefield = "FirstExceptionTime";
            collection = exceptionModelCollection;
            lasttimestr = "LastExceptionTime";
        }
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        //model 表 不查 小时类型报警数据
        if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
        } else if (remindtype == OverAlarmEnum.getCode()){
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
        }
        operations.add(Aggregation.match(criteria));
        if (remindtype == ExceptionAlarmEnum.getCode()) {    //若查异常数据 则需返回异常类型（零值异常、连续值异常、超限异常、无流量异常等）
            operations.add(Aggregation.project("MN", "LastExceptionTime", timefield, "PollutantCode", "ExceptionType","MonitorValue")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$"+timefield);
            childmap.put("endtime", "$LastExceptionTime");
            childmap.put("exceptiontype", "$ExceptionType");
            operations.add(
                    Aggregation.group("MN", "MonitorTime","PollutantCode")
                            .min(timefield).as("firsttime")
                            .max("LastExceptionTime").as("lasttime")
                            .push(childmap).as("pollutanttimes")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime","PollutantCode", "MN"));
        } else { //超标
            operations.add(Aggregation.project("MN", "MinOverMultiple","MaxOverMultiple",lasttimestr, timefield, "PollutantCode","AlarmLevel","LastMonitorValue")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$"+timefield);
            childmap.put("endtime", "$"+lasttimestr);
            childmap.put("pollutantcode", "$PollutantCode");
            childmap.put("minovermultiple", "$MinOverMultiple");
            childmap.put("maxovermultiple", "$MaxOverMultiple");
            operations.add(
                    Aggregation.group("MN", "MonitorTime")
                            .min(timefield).as("firsttime")
                            .max(lasttimestr).as("lasttime")
                            .push(childmap).as("pollutanttimes")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN"));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Document document : listItems) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("datagathercode", document.get("MN"));
            dataMap.put("pollutantcode", document.get("PollutantCode"));
            dataMap.put("monitortime", document.getString("MonitorTime"));
            if (document.get("firsttime")!=null&&document.getDate("lasttime")!=null){
                String first_time = DataFormatUtil.getDateYMDHMS(document.getDate("firsttime"));
                String last_time = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                if (first_time.equals(last_time)){
                    dataMap.put("firsttime", DataFormatUtil.getDateYMD(document.getDate("firsttime"))+" 00:00:00");
                }else{
                    dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                }
                dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
            }else{
                dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
            }

            List<Document> pollutanttimes = (List<Document>) document.get("pollutanttimes");
            int alarmtimenum = 0;

            if (pollutanttimes!=null&&pollutanttimes.size()>0){
                pollutanttimes = pollutanttimes.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("starttime"))).collect(Collectors.toList());
                Double minvalue = 0d;
                Double maxvalue = 0d;
                String continuityvalue ="";
                Set<String> exceptiontypes = new HashSet();
                Date firsttime = null;
                Date lasttime = null;
                for (Document podo:pollutanttimes) {
                    //比较时间 获取报警时段
                    if (podo.get("starttime")!=null&&podo.get("endtime")!=null){
                        if (firsttime==null&&lasttime==null){
                            firsttime = podo.getDate("starttime");
                            lasttime = podo.getDate("endtime");
                        }else{
                            //比较时间段
                            //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                            if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(lasttime))||
                                    podo.getDate("starttime").before(lasttime)){
                                //若被包含 比较两个结束时间
                                if (lasttime.before(podo.getDate("endtime")) ){
                                    //若第一次报警的结束时间 小于 第二个报警时段的结束时间
                                    //则进行赋值
                                    lasttime = podo.getDate("endtime");
                                }
                            }else{
                                //第二次报警时段不被包含于第一个报警时段中
                                if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))){
                                    continuityvalue = continuityvalue+DataFormatUtil.getDateHM(firsttime)+"、";
                                    alarmtimenum +=1;
                                }else{
                                    continuityvalue = continuityvalue+DataFormatUtil.getDateHM(firsttime)+"-"+DataFormatUtil.getDateHM(lasttime)+"、";
                                    long timenum = (lasttime.getTime()-firsttime.getTime())/(1000 * 60);
                                    alarmtimenum +=Integer.valueOf(timenum+"");
                                }

                                //将重新赋值开始 结束时间
                                firsttime = podo.getDate("starttime");
                                lasttime = podo.getDate("endtime");
                            }
                        }
                    }
                    if (podo.get("minovermultiple")!=null){
                        if (minvalue==0d){
                            minvalue = Double.valueOf(podo.getString("minovermultiple"));
                        }else{
                            if (Double.valueOf(podo.getString("minovermultiple")) < minvalue){
                                minvalue = Double.valueOf(podo.getString("minovermultiple"));
                            }
                        }
                    }
                    if (podo.get("maxovermultiple")!=null){
                        if (maxvalue==0d){
                            maxvalue = Double.valueOf(podo.getString("maxovermultiple"));
                        }else{
                            if (Double.valueOf(podo.getString("maxovermultiple")) > maxvalue){
                                maxvalue = Double.valueOf(podo.getString("maxovermultiple"));
                            }
                        }
                    }
                    //异常类型
                    if(podo.get("exceptiontype")!=null){
                        exceptiontypes.add(podo.getString("exceptiontype"));
                    }

                }
                //将最后一次超标时段 或一直连续报警的超标时段 拼接
                if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))){
                    continuityvalue = continuityvalue+DataFormatUtil.getDateHM(firsttime)+"、";
                }else{
                    continuityvalue = continuityvalue+DataFormatUtil.getDateHM(firsttime)+"-"+DataFormatUtil.getDateHM(lasttime)+"、";
                    long timenum = (lasttime.getTime()-firsttime.getTime())/(1000 * 60);
                    alarmtimenum +=Integer.valueOf(timenum+"");
                }
                //超标时段
                if (!"".equals(continuityvalue)){
                    dataMap.put("continuityvalue",continuityvalue.substring(0,continuityvalue.length()-1));
                }else{
                    dataMap.put("continuityvalue","");
                }
                //超标倍数
                String min = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(minvalue * 100));
                String max = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(maxvalue * 100));
                if (!min.equals(max)) {//最大超标倍数和最小超标倍数不相等
                    dataMap.put("overmultiple", min + "%-" + max + "%");
                } else {//相等
                    dataMap.put("overmultiple", min + "%");
                }
                String str = "";
                if (exceptiontypes!=null&&exceptiontypes.size()>0){
                    for (String type : exceptiontypes) {
                        str += CommonTypeEnum.ExceptionTypeEnum.getNameByCode(type) + "、";
                    }
                }
                if ( !"".equals(str) ){
                    str = str.substring(0, str.length() - 1);
                    dataMap.put("exceptionname",str);
                }
            }
            String pollutantname = "";
            if (alarmtimenum>0){
               // dataMap.put("timetotalnum", countHourMinuteTime(alarmtimenum));
            }else{
                dataMap.put("timetotalnum", "-");
            }
            if (!"".equals(pollutantname)) {
                dataMap.put("pollutantname",pollutantname);
                dataList.add(dataMap);
            }
        }
        return null;
    } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
}

}
