package com.tjpu.sp.service.impl.envhousekeepers.enterpriseportal;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.envhousekeepers.enterpriseportal.EnterprisePortalService;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;

@Transactional
@Service
public class EnterprisePortalServiceImpl implements EnterprisePortalService {

    @Autowired
    private PollutionMapper pollutionMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    private final String exceptionModelCollection = "ExceptionModel";
    private final String overModelCollection = "OverModel";
    private final String change_db = "SuddenRiseData";

    @Override
    public List<Map<String, Object>> getEnterpriseArchivesDataByPollutionID(String pollutionid) {
        return  pollutionMapper.getEnterpriseArchivesDataByPollutionID(pollutionid);
    }

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 上午 10:16
     * @Description: 根据企业ID获取该企业的最新动态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntNewDynamicDataByPollutionID( Map<String,Object> param) {
        return pollutionMapper.getEntNewDynamicDataByPollutionID(param);
    }

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 2:13
     * @Description: 根据企业ID获取该企业的最新台账记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntNewStandingBookDataByPollutionID(Map<String, Object> param) {
        return pollutionMapper.getEntNewStandingBookDataByPollutionID(param);
    }

    @Override
    public Map<String, Object> getOneEntEarlyOverOrExceptionListDataByParams(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            Map<String, Object> mnandtype = (Map<String, Object>) paramMap.get("mnandtype");
            Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) paramMap.get("mnandmonitorpointid");
            Map<String, Object> mnAndMonitorPointName = (Map<String, Object>) paramMap.get("mnandmonitorpointname");
            //污染物标准值
            List<Map<String, Object>> stands = pollutantFactorMapper.getEnvPollutantStandardDataByParam(paramMap);
            Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
            //MN_污染物 分组
            if (stands!=null&&stands.size()>0){
                listmap = stands.stream().collect(Collectors.groupingBy(m -> m.get("mnandcode").toString()));
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString() + " 00:00:00";
            String endtime = paramMap.get("endtime").toString() + " 23:59:59";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            Integer remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            if (remindtype == EarlyAlarmEnum.getCode()||remindtype == OverAlarmEnum.getCode()) {  //阈值
                timefield = "FirstOverTime";
                collection = overModelCollection;
                lasttimestr = "LastOverTime";
            } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                timefield = "FirstExceptionTime";
                collection = exceptionModelCollection;
                lasttimestr = "LastExceptionTime";
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
            } else  if (remindtype == OverAlarmEnum.getCode()){
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            }else if(remindtype == EarlyAlarmEnum.getCode()){
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0).and("DataType").ne("HourData");
            }
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", timefield,"PollutantCode")
                    .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(
                    Aggregation.group("MN", "MonitorTime","PollutantCode")
            );
            long totalCount = 0;
            //获取分组总数
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Map.class);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                totalCount = resultsCount.getMappedResults().size();
                pageEntity.setTotalCount(totalCount);
                int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
                pageEntity.setPageCount(pageCount);
            }
            operations.clear();
            criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
            } else if (remindtype == OverAlarmEnum.getCode()){
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            }else if(remindtype == EarlyAlarmEnum.getCode()){
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0).and("DataType").ne("HourData");
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
                                .last("MonitorValue").as("lastvalue")
                                .min(timefield).as("firsttime")
                                .max("LastExceptionTime").as("lasttime")
                                .push(childmap).as("pollutanttimes")
                );
            } else { //超标 超阈值
                operations.add(Aggregation.project("MN", "MinOverMultiple","MaxOverMultiple",lasttimestr, timefield, "PollutantCode","AlarmLevel","LastMonitorValue")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("starttime", "$"+timefield);
                childmap.put("endtime", "$"+lasttimestr);
                childmap.put("minovermultiple", "$MinOverMultiple");
                childmap.put("maxovermultiple", "$MaxOverMultiple");
                operations.add(
                        Aggregation.group("MN", "MonitorTime","PollutantCode")
                                .last("LastMonitorValue").as("lastvalue")
                                .min(timefield).as("firsttime")
                                .max(lasttimestr).as("lasttime")
                                .last("AlarmLevel").as("lastlevel")
                                .push(childmap).as("pollutanttimes")
                );
            }
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime","PollutantCode", "MN"));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("total", pageEntity.getTotalCount());
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                String mnandcode = document.get("MN")+"_"+mnandtype.get(document.getString("MN"))+"_"+document.get("PollutantCode");
                dataMap.put("datagathercode", document.get("MN"));
                dataMap.put("pollutantcode", document.get("PollutantCode"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                dataMap.put("monitorpointid", mnAndMonitorPointId.get(document.getString("MN")));
                dataMap.put("monitorpointtype", mnandtype.get(document.getString("MN")));
                dataMap.put("monitorpointname", mnAndMonitorPointName.get(document.getString("MN")));
                dataMap.put("lastvalue", document.get("lastvalue"));
                dataMap.put("lastlevel", document.get("lastlevel"));
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
                            /*if (podo.get("starttime")!=null&&podo.get("endtime")!=null){
                                if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(podo.getDate("endtime")))){
                                    continuityvalue = continuityvalue+DataFormatUtil.getDateHM(podo.getDate("starttime"))+"、";
                                }else{
                                    continuityvalue = continuityvalue+DataFormatUtil.getDateHM(podo.getDate("starttime"))+"-"+DataFormatUtil.getDateHM(podo.getDate("endtime"))+"、";
                                }
                            }*/
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
                Object standsvalue = "-";
                Map<String, Object> onestandmap =null;
                if (listmap.get(mnandcode)!=null){
                    List<Map<String, Object>> polist = listmap.get(mnandcode);
                    if (polist!=null&&polist.size()>0){
                        if ("".equals(pollutantname)){
                            pollutantname =polist.get(0).get("pollutantname")!=null?polist.get(0).get("pollutantname").toString():"";
                        }
                        if (remindtype != ExceptionAlarmEnum.getCode()&& document.get("lastlevel")!=null){
                            int level = document.getInteger("lastlevel");
                            if (level==-1){//超标
                                onestandmap = polist.get(0);
                                if (onestandmap.get("AlarmType")!=null){
                                        if (CommonTypeEnum.AlarmTypeEnum.UpperAlarmEnum.getCode().equals(onestandmap.get("AlarmType").toString())){//上限报警
                                            standsvalue = onestandmap.get("StandardMaxValue");
                                        }else if (CommonTypeEnum.AlarmTypeEnum.LowerAlarmEnum.getCode().equals(onestandmap.get("AlarmType").toString())){//下限报警
                                            standsvalue = onestandmap.get("StandardMinValue");
                                        }else {
                                            standsvalue = onestandmap.get("StandardMinValue") + "-" + onestandmap.get("StandardMaxValue");
                                        }
                                }
                            }else{
                                for (Map<String, Object> map:polist){
                                    if (map.get("FK_AlarmLevelCode")!=null&&!"".equals(map.get("FK_AlarmLevelCode").toString())
                                            &&level == Integer.valueOf(map.get("FK_AlarmLevelCode").toString())){
                                        standsvalue = map.get("ConcenAlarmMaxValue");
                                    }
                                }
                            }
                        }
                    }

                }
                if (alarmtimenum>0){
                    dataMap.put("timetotalnum", countHourMinuteTime(alarmtimenum));
                }else{
                    dataMap.put("timetotalnum", "-");
                }
                dataMap.put("standsvalue",standsvalue);
                if (!"".equals(pollutantname)) {
                    dataMap.put("pollutantname",pollutantname);
                    dataList.add(dataMap);
                }
            }
            resultMap.put("datalist", dataList);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/01 0001 下午 3:54
     * @Description: 通过自定义条件统计监测点浓度（预警、异常、超限）连续预警数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Integer countOneEntEarlyOverOrExceptionDataByParams(Map<String, Object> paramMap) {
        try {
            int totalCount = 0;
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString() + " 00:00:00";
            String endtime = paramMap.get("endtime").toString() + " 23:59:59";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            Integer remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            String timefield = "";
            String collection = "";
            if (remindtype == EarlyAlarmEnum.getCode()||remindtype == OverAlarmEnum.getCode()) {  //阈值
                timefield = "FirstOverTime";
                collection = overModelCollection;
            } else if (remindtype == ExceptionAlarmEnum.getCode()) {    //异常
                timefield = "FirstExceptionTime";
                collection = exceptionModelCollection;
            }else if (remindtype == ConcentrationChangeEnum.getCode()) {    //浓度突变
                timefield = "ChangeTime";
                collection = change_db;
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            if (remindtype == ExceptionAlarmEnum.getCode()) {
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate);
            } else  if (remindtype == OverAlarmEnum.getCode()){
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
            }else if(remindtype == EarlyAlarmEnum.getCode()){
                criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0);
            }else if(remindtype == ConcentrationChangeEnum.getCode()){
                criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
            }
            if(remindtype == ConcentrationChangeEnum.getCode()){
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", timefield, "PollutantCode")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode")
                );
            }else {
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("MN", timefield, "PollutantCode")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
                operations.add(
                        Aggregation.group("MN", "MonitorTime", "PollutantCode")
                );
            }
            //获取分组总数
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultsCount = mongoTemplate.aggregate(aggregationCount, collection, Map.class);
            totalCount = resultsCount.getMappedResults().size();
            return totalCount;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/01 0001 下午 3:54
     * @Description: 通过自定义条件统计某个企业所有监测点浓度突变预警数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getOneEntConcentrationChangeDataByParams(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            Map<String, Object> mnandtype = (Map<String, Object>) paramMap.get("mnandtype");
            Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) paramMap.get("mnandmonitorpointid");
            Map<String, Object> mnAndMonitorPointName = (Map<String, Object>) paramMap.get("mnandmonitorpointname");
            //污染物标准值
            List<Map<String, Object>> stands = pollutantFactorMapper.getEnvPollutantStandardDataByParam(paramMap);
            Map<String, List<Map<String, Object>>> listmap = new HashMap<>();
            //MN_污染物 分组
            if (stands != null && stands.size() > 0) {
                listmap = stands.stream().collect(Collectors.groupingBy(m -> m.get("mnandcode").toString()));
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString() + " 00:00:00";
            String endtime = paramMap.get("endtime").toString() + " 23:59:59";
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("ChangeTime").gte(startDate).lte(endDate).and("DataType").is("MinuteData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "ChangeTime", "PollutantCode")
                    .and(DateOperators.DateToString.dateOf("ChangeTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            operations.add(
                    Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode")
            );
            long totalCount = 0;
            //获取分组总数
            Aggregation aggregationCount = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultsCount = mongoTemplate.aggregate(aggregationCount, change_db, Map.class);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                totalCount = resultsCount.getMappedResults().size();
                pageEntity.setTotalCount(totalCount);
                int pageCount = ((int) totalCount + pageSize - 1) / pageSize;
                pageEntity.setPageCount(pageCount);
            }
            operations.clear();
            criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("ChangeTime").gte(startDate).lte(endDate).and("DataType").is("MinuteData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "ChangeTime", "PollutantCode", "MonitorValue","ChangeMultiple")
                        .and(DateOperators.DateToString.dateOf("ChangeTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("time", "$ChangeTime");
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime", "PollutantCode")
                                .last("MonitorValue").as("lastvalue")
                                .min("ChangeMultiple").as("min")
                                .max("ChangeMultiple").as("max")
                                .min("ChangeTime").as("firsttime")
                                .max("ChangeTime").as("lasttime")
                                .push(childmap).as("pollutanttimes")
                );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "DataGatherCode", "PollutantCode"));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, change_db, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultMap.put("total", pageEntity.getTotalCount());
            }
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                String mnandcode = document.get("DataGatherCode") + "_" + mnandtype.get(document.getString("DataGatherCode")) + "_" + document.get("PollutantCode");
                dataMap.put("datagathercode", document.get("DataGatherCode"));
                dataMap.put("pollutantcode", document.get("PollutantCode"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                dataMap.put("monitorpointid", mnAndMonitorPointId.get(document.getString("DataGatherCode")));
                dataMap.put("monitorpointtype", mnandtype.get(document.getString("DataGatherCode")));
                dataMap.put("monitorpointname", mnAndMonitorPointName.get(document.getString("DataGatherCode")));
                dataMap.put("lastvalue", document.get("lastvalue"));
                int alarmnum = 0;
                if (document.get("firsttime") != null && document.getDate("lasttime") != null) {
                    String first_time = DataFormatUtil.getDateYMDHMS(document.getDate("firsttime"));
                    String last_time = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                    if (first_time.equals(last_time)) {
                        dataMap.put("firsttime", DataFormatUtil.getDateYMD(document.getDate("firsttime")) + " 00:00:00");
                    } else {
                        dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                    }
                    dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                } else {
                    dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                    dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                }
                //突变幅度
                Double min_changebs = document.getDouble("min");
                Double max_changebs = document.getDouble("max");
                String min = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(min_changebs * 100));
                String max = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(max_changebs * 100));
                if (!min.equals(max)) {//最大超标倍数和最小超标倍数不相等
                    dataMap.put("overmultiple", min + "%-" + max + "%");
                } else {//相等
                    dataMap.put("overmultiple", min + "%");
                }
                List<Document> pollutanttimes = (List<Document>) document.get("pollutanttimes");
                if (pollutanttimes != null && pollutanttimes.size() > 0) {
                    String continuityvalue = "";
                    for (Document podo : pollutanttimes) {
                        if (podo.get("time") != null) {
                            alarmnum++;
                            continuityvalue = continuityvalue + DataFormatUtil.getDateHM(podo.getDate("time")) + "、";
                        }
                    }
                    //突变时段
                    if (!"".equals(continuityvalue)) {
                        dataMap.put("continuityvalue", continuityvalue.substring(0, continuityvalue.length() - 1));
                    } else {
                        dataMap.put("continuityvalue", "");
                    }
                }
                String pollutantname = "";
                if (listmap.get(mnandcode) != null) {
                    List<Map<String, Object>> polist = listmap.get(mnandcode);
                    if (polist != null && polist.size() > 0) {
                        if ("".equals(pollutantname)) {
                            pollutantname = polist.get(0).get("pollutantname") != null ? polist.get(0).get("pollutantname").toString() : "";
                        }
                    }
                }
                if (alarmnum>0){
                    dataMap.put("timetotalnum", countHourMinuteTime(alarmnum));
                }else{
                    dataMap.put("timetotalnum", "-");
                }
                if (!"".equals(pollutantname)) {
                    dataMap.put("pollutantname", pollutantname);
                    dataList.add(dataMap);
                }
            }
            resultMap.put("datalist", dataList);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/20 0020 下午 1:08
     * @Description: 比较两个时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private boolean compare(String time1, String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //Date类的一个方法，如果a早于b返回true，否则返回false
        if (a.before(b))
            return true;
        else
            return false;
    }


    private String countHourMinuteTime(int tatalnum) {
        String str = "";
        if (tatalnum < 60) {
            str = tatalnum + "分钟";
        } else if (tatalnum == 60) {
            str = "1小时";
        } else {
            int onenum = tatalnum / 60;
            str = onenum + "小时" + ((tatalnum - onenum * 60) > 0 ? (tatalnum - onenum * 60) + "分钟" : "");
        }
        return str;
    }
}
