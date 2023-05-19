package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.alibaba.fastjson.JSONObject;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.AlarmLevelMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.model.common.mongodb.OnlineAlarmCountQueryVO;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sun.net.www.ParseUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.NoFlowExceptionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.ExceptionTypeEnum.getNameByCode;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author: zhangzc
 * @date: 2019/8/15 13:51
 * @Description: 统计报警数据
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@Service
public class OnlineCountAlarmServiceImpl implements OnlineCountAlarmService {
    private final MongoTemplate mongoTemplate;
    //小时浓度在线
    private final String hourCollection = "HourData";
    //实时浓度在线
    private final String realtimeCollection = "RealTimeData";
    //分钟浓度在线
    private final String minuteCollection = "MinuteData";
    //日浓度在线
    private final String dayCollection = "DayData";
    //小时排放量在线
    private final String hourFlowCollection = "HourFlowData";
    //超域在线
    private final String earlyWarnData = "EarlyWarnData";
    //超限在线
    private final String overData = "OverData";
    //异常在线
    private final String exceptionData = "ExceptionData";
    //突变在线
    private final String changeData = "SuddenRiseData";
    private final String exceptionModelCollection = "ExceptionModel";
    private final String overModelCollection = "OverModel";

    public OnlineCountAlarmServiceImpl(@Qualifier("primaryMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private AlarmLevelMapper alarmLevelMapper;


    /**
     * @author: zhangzc
     * @date: 2019/8/15 13:57
     * @Description: 统计  浓度 和 排放量 小时数据 报警个数
     * 如果传入用户id 是获取该用户未读数据，如果不传用户ID则是所有报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public int countNDAndPFLAlarmNumInHourData(OnlineAlarmCountQueryVO queryVO) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        final String mnFieldName = "DataGatherCode";
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project(fields(mnFieldName));
        if (StringUtils.isNotBlank(queryVO.getUserId())) {
            projects = projects.and(getIsReadCon(queryVO.getUserId())).as("IsRead");
            aggregations.add(projects);
            aggregations.add(match(Criteria.where("IsRead").is("0")));
            aggregations.add(group(mnFieldName));
        } else {
            aggregations.add(projects);
        }
        aggregations.add(count().as("count"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getUniqueMappedResult() != null ? aggregationResults.getUniqueMappedResult().getInteger("count") : 0;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/17 17:11
     * @Description: 统计 报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public int countOtherLAlarmNum(OnlineAlarmCountQueryVO queryVO) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        final String mnFieldName = "DataGatherCode";
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project(mnFieldName);
        if (StringUtils.isNotBlank(queryVO.getUserId())) {
            ConditionalOperators.Cond isReadCon = getIsReadCon(queryVO.getUserId());
            projects = projects.and(isReadCon).as("IsRead");
            aggregations.add(projects);
            aggregations.add(match(Criteria.where("IsRead").is("0")));
            aggregations.add(group(mnFieldName));
        } else {
            aggregations.add(projects);
        }
        aggregations.add(count().as("count"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getUniqueMappedResult() != null ? aggregationResults.getUniqueMappedResult().getInteger("count") : 0;
    }

    /**
     * @author: xsm
     * @date: 2021/04/29 14:58
     * @Description: 根据时间统计 返回 时间和该时间个数(突变)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDChangeAlarmNumByTimeType(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        List<AggregationOperation> queryAggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(queryVO.getMns());
        if (queryVO.getStartTime() != null && queryVO.getEndTime() != null && StringUtils.isNotBlank(queryVO.getTimeFieldName())) {
            criteria.and("ChangeTime").gte(queryVO.getStartTime()).lte(queryVO.getEndTime());
        }
        criteria.and("DataType").is("MinuteData");
        queryAggregations.add(match(criteria));
        // 加8小时
        ProjectionOperation add8h = Aggregation.project("ChangeTime")
                .andExpression("add(" + "ChangeTime" + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("date8").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("MonitorDate");
        queryAggregations.add(add8h);
        queryAggregations.add(projects);
        queryAggregations.add(group("MonitorDate").count().as("num"));
        queryAggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation = newAggregation(queryAggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "SuddenRiseData", Document.class);
        return aggregationResults.getMappedResults();
    }


    //根据时间统计 返回 时间和该时间个数
    /**
     * @author: zhangzc
     * @date: 2019/8/15 13:57
     * @Description: 根据时间统计 返回 时间和该时间个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDAndPFLAlarmNumByTimeType(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        // 加8小时
        ProjectionOperation add8h = Aggregation.project(queryVO.getTimeFieldName())
                .andExpression("add(" + queryVO.getTimeFieldName() + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("date8").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("MonitorDate");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("MonitorDate").count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/17 17:11
     * @Description: 根据时间统计 返回 时间和该时间个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countOtherLAlarmNumByTimeType(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        //当为废水监测类型时  异常数据不包括无流量异常
        if (queryVO.getCollection().equals("ExceptionData") && queryVO.getMonitorPointType() == WasteWaterEnum.getCode()) {  //查询异常数据  且监测类型是废水时
            //查询非无流量异常的异常数据
            aggregations.add(
                    Aggregation.match(
                            Criteria.where("ExceptionType").ne(NoFlowExceptionEnum.getCode())
                    )
            );
        }
        if (timeStyle.equals("hour")) {  //不统计天数据
            Criteria.where("DataType").ne("DayData");
        }
       /* DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project().and(dateToString).as("MonitorDate");
        aggregations.add(projects);*/
        // 加8小时
        ProjectionOperation add8h = Aggregation.project(queryVO.getTimeFieldName())
                .andExpression("add(" + queryVO.getTimeFieldName() + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("date8").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("MonitorDate");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("MonitorDate").count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }


    //根据mn号统计 返回mn号和个数


    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据mn号统计 返回mn号和个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDAndPFLAlarmDataByMN(OnlineAlarmCountQueryVO queryVO) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        final String mnFieldName = "DataGatherCode";
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project(fields(mnFieldName));
        if (StringUtils.isNotBlank(queryVO.getUserId())) {
            projects = projects.and(getIsReadCon(queryVO.getUserId())).as("IsRead");
            aggregations.add(projects);
            aggregations.add(match(Criteria.where("IsRead").is("0")));  //筛选已读数据
        } else {
            aggregations.add(projects);
        }
        aggregations.add(group(mnFieldName).count().as("num"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2021/04/29 16:11
     * @Description: 根据mn号统计 返回mn号和个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDChangeLAlarmDataByMN(OnlineAlarmCountQueryVO queryVO) {
        List<AggregationOperation> queryAggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(queryVO.getMns());
        if (queryVO.getStartTime() != null && queryVO.getEndTime() != null && StringUtils.isNotBlank(queryVO.getTimeFieldName())) {
            criteria.and("ChangeTime").gte(queryVO.getStartTime()).lte(queryVO.getEndTime());
        }
        criteria.and("DataType").is("MinuteData");
        queryAggregations.add(match(criteria));
        ProjectionOperation projects = project(fields("DataGatherCode"));
        queryAggregations.add(projects);
        queryAggregations.add(group("DataGatherCode").count().as("num"));
        Aggregation aggregation = newAggregation(queryAggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "SuddenRiseData", Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2021/05/18 13:44
     * @Description: 根据自定义参数获取突变或超标报警各点位下污染物的报警时段
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String,Object> getChangeAndOverAlarmDataByParamMap(Map<String, Object> paramMap) {
        Map<String,Object> result = new HashMap<>();
        List<Document> listItems = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("PollutantCode", "$PollutantCode");
        //pollutantList.put("MonitorValue", "$MonitorValue");
        if (collection.equals("SuddenRiseData")){//突变
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").is("MinuteData");
            pollutantList.put("ChangeTime", "$" + timefield);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timefield, "PollutantCode", "MonitorValue","ChangeMultiple"));
            operations.add(Aggregation.group("DataGatherCode")
                    .push(pollutantList).as("pollutantList")
            );
        }else{//超标
            //超限报警
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","PollutantCode", "LastMonitorValue","FirstOverTime","LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN",timefield));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();
        if (listItems != null && listItems.size() > 0) {
            if (collection.equals("SuddenRiseData")) {//突变
                List<Document> pollutantDataList;
                String mnCommon="";
                List<Document> poList;
                String pollutantcode;
                for (Document document : listItems) {
                    Map<String,Object> objdata = new HashMap<>();
                    mnCommon = document.getString("_id");
                    poList = document.get("pollutantList", List.class);
                    Map<String,String> code_time = new HashMap<>();
                    for (Document pollutant : poList) {
                        pollutantcode = pollutant.getString("PollutantCode");
                        String hm = DataFormatUtil.getDateHM(pollutant.getDate("ChangeTime"));
                        if (code_time.get(pollutantcode)!=null){
                            code_time.put(pollutantcode,code_time.get(pollutantcode)+"、"+hm);
                        }else{
                            code_time.put(pollutantcode,hm);
                        }
                    }
                    if (code_time!=null&&code_time.size()>0){
                        for(String pocode:code_time.keySet()){
                            String str = "浓度突变"+code_time.get(pocode)+"；";
                            objdata.put(pocode,str);
                        }
                    }
                    result.put(mnCommon,objdata);
                }
            }else{//超标
                //通过mn号分组数据
                Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("MN").toString()));
                for(String mn:mapDocuments.keySet()){
                    List<Document> documents = mapDocuments.get(mn);
                    Map<String,Object> codeanddata = new HashMap<>();
                    Map<String,Map<String,Object>> levelmap = new HashMap<>();
                    String firsttime ="";
                    String lasttime ="";
                    Set<String> codes = new HashSet<>();
                    for (Document document:documents){
                        firsttime = DataFormatUtil.getDateHM(document.getDate("FirstOverTime"));
                        lasttime = DataFormatUtil.getDateHM(document.getDate("LastOverTime"));
                        String timestr = "";
                        if (firsttime.equals(lasttime)){
                            timestr = firsttime;
                        }else{
                            timestr = firsttime+"-"+ lasttime;
                        }
                        //污染物
                        String code = document.getString("PollutantCode");
                        codes.add(code);
                        //按污染物和报警级别分组
                        if (levelmap.get(code)!=null){
                            Map<String,Object> levelone = levelmap.get(code);
                            String level = document.getInteger("AlarmLevel").toString();
                            String overstr = "";
                            if ("-1".equals(level)){
                                overstr = "超标";
                            }else{
                                overstr = "超限";
                            }
                            if (levelone.get(overstr)!=null){//判断是否有该级别的数据
                                levelone.put(overstr,levelone.get(overstr)+"、"+timestr);
                            }else{
                                levelone.put(overstr,timestr);
                            }

                        }else{
                            Map<String,Object> levelone = new HashMap<>();
                            String level = document.getInteger("AlarmLevel").toString();
                            String overstr = "";
                            if ("-1".equals(level)){
                                overstr = "超标";
                            }else{
                                overstr = "超限";
                            }
                            levelone.put(overstr,timestr);
                            levelmap.put(code,levelone);
                        }
                    }
                    if (codes!=null&&codes.size()>0){
                        for (String code:codes){
                            String codestr = "";
                            if (levelmap.get(code)!=null){
                                Map<String,Object> onemap = levelmap.get(code);
                                for(String key:onemap.keySet()){
                                    codestr += key +onemap.get(key)+ ";";
                                }
                            }
                            codeanddata.put(code,codestr);
                        }
                    }
                    result.put(mn,codeanddata);
                }
            }
        }
        return result;
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据mn号统计 返回mn号和个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countOtherAlarmDataByMN(OnlineAlarmCountQueryVO queryVO) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        final String mnFieldName = "DataGatherCode";
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project(mnFieldName);
        if (StringUtils.isNotBlank(queryVO.getUserId())) {
            ConditionalOperators.Cond isReadCon = getIsReadCon(queryVO.getUserId());
            projects = projects.and(isReadCon).as("IsRead");
            aggregations.add(projects);
            aggregations.add(match(Criteria.where("IsRead").is("0")));
        } else {
            aggregations.add(projects);
        }
        aggregations.add(group(mnFieldName).count().as("num"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    //根据污染物Code统计 返回污染物code和个数


    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据污染物Code统计 返回污染物code和个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDAndPFLAlarmDataByPollutantCode(OnlineAlarmCountQueryVO queryVO) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode");
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2021/04/29 14:19
     * @Description: 根据污染物Code统计 返回污染物code和个数(突变)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countChangAlarmDataByPollutantCode(OnlineAlarmCountQueryVO queryVO) {
        List<AggregationOperation> queryAggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(queryVO.getMns());
        if (queryVO.getStartTime() != null && queryVO.getEndTime() != null && StringUtils.isNotBlank(queryVO.getTimeFieldName())) {
            criteria.and("ChangeTime").gte(queryVO.getStartTime()).lte(queryVO.getEndTime());
        }
        criteria.and("DataType").is("MinuteData");
        if (queryVO.getPollutantCodes() != null && queryVO.getPollutantCodes().size() > 0 && StringUtils.isNotBlank(queryVO.getPollutantCodeFieldName())) {
            criteria.and(queryVO.getPollutantCodeFieldName()).in(queryVO.getPollutantCodes());
        }
        queryAggregations.add(match(criteria));
        List<AggregationOperation> aggregations = queryAggregations;
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode");
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), "SuddenRiseData", Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据污染物Code统计 返回污染物code和个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countOtherAlarmDataByPollutantCode(OnlineAlarmCountQueryVO queryVO) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode");
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }


    //根据污染物Code统计 返回污染物code号、个数、时间数组

    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据污染物Code统计 返回污染物code号、个数、时间数组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDAndPFLAlarmDataByPollutantCode2(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode").and(dateToString).as("Time");
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num").addToSet("Time").as("Times"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据污染物Code统计 返回污染物code号、个数、时间数组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countOtherAlarmDataByPollutantCode2(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode").and(dateToString).as("Time");
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num").addToSet("Time").as("Times"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    //根据污染物Code统计 返回污染物code号、个数、最新时间

    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据污染物Code统计 返回污染物code号、个数、最新时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNDAndPFLAlarmDataByPollutantCode3(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        /*DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode").and(dateToString).as("Time");
        aggregations.add(projects);*/
        ProjectionOperation add8h = Aggregation.project(queryVO.getTimeFieldName()).and(queryVO.getPollutantCodeFieldName()).as("PollutantCode")
                .andExpression("add(" + queryVO.getTimeFieldName() + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("date8", "PollutantCode").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("Time");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num").max("Time").as("MaxTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/20 20:18
     * @Description: 根据污染物Code统计 返回污染物code号、个数、最新时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countOtherAlarmDataByPollutantCode3(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        /*DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project().and(queryVO.getPollutantCodeFieldName()).as("PollutantCode").and(dateToString).as("Time");
        aggregations.add(projects);*/
        ProjectionOperation add8h = Aggregation.project(queryVO.getTimeFieldName()).and(queryVO.getPollutantCodeFieldName()).as("PollutantCode")
                .andExpression("add(" + queryVO.getTimeFieldName() + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("date8", "PollutantCode").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("Time");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("PollutantCode").count().as("num").max("Time").as("MaxTime"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }


    //报警时间查询
    @Override
    public List<Document> getOtherAlarmHourTimes(OnlineAlarmCountQueryVO queryVO) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        queryVO.setPollutantCodeFieldName("PollutantCode");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString("%H:%M:%S");
        ProjectionOperation projects = project().and(dateToString).as("Time").and("DataGatherCode").as("DataGatherCode").and("PollutantCode").as("PollutantCode");
        aggregations.add(projects);
        GroupOperation group = group("Time", "DataGatherCode", "PollutantCode");
        aggregations.add(group.count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "Time"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public List<Document> getNDAndPFLAlarmHourTimes(OnlineAlarmCountQueryVO queryVO) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        List<AggregationOperation> aggregations = new ArrayList<>();
        final String mnFieldName = "DataGatherCode";
        final String IsSuddenChangeFieldName = "IsSuddenChange";
        Criteria criteria = Criteria.where(mnFieldName).in(queryVO.getMns());
        if (queryVO.getStartTime() != null && queryVO.getEndTime() != null && StringUtils.isNotBlank(queryVO.getTimeFieldName())) {
            criteria.and(queryVO.getTimeFieldName()).gte(queryVO.getStartTime()).lte(queryVO.getEndTime());
        }
        if (StringUtils.isNotBlank(queryVO.getUnwindFieldName())) {
            criteria.and(queryVO.getUnwindFieldName() + "." + IsSuddenChangeFieldName).is(true);
            criteria.and(queryVO.getUnwindFieldName() + "." + "PollutantCode").in(queryVO.getPollutantCodes());
        }
        aggregations.add(match(criteria));
        aggregations.add(unwind(queryVO.getUnwindFieldName()));
        aggregations.add(match(Criteria.where(queryVO.getUnwindFieldName() + "." + IsSuddenChangeFieldName).is(true).and(queryVO.getUnwindFieldName() + "." + "PollutantCode").in(queryVO.getPollutantCodes())));
        DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString("%H:%M:%S");
        ProjectionOperation projects = project().and(dateToString).as("Time").and(mnFieldName).as(mnFieldName).and(queryVO.getUnwindFieldName() + "." + "PollutantCode").as("PollutantCode");
        aggregations.add(projects);
        GroupOperation group = group("Time", "DataGatherCode", "PollutantCode");
        aggregations.add(group.count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "Time"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    //根据MN号和污染物统计返回Mn号、污染物code、次数、最新报警时间

    /**
     * @author: zhangzc
     * @date: 2019/8/26 16:46
     * @Description: 根据MN号和污染物统计返回Mn号、污染物code、次数、最新报警时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countAlarmDataByMnAndPollutantCode(OnlineAlarmCountQueryVO queryVO) {
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        Fields fields = fields("DataGatherCode", queryVO.getTimeFieldName()).and("PollutantCode", queryVO.getPollutantCodeFieldName());
        ProjectionOperation projects = project(fields);
        aggregations.add(projects);
        aggregations.add(group("DataGatherCode", "PollutantCode").count().as("num").max(queryVO.getTimeFieldName()).as("LastTime"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 4:37
     * @Description: 统计污染物浓度突变次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countConcentrationChangeDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        String datetype = null;
        String pattern = null;
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
        criteria1.and("HourDataList.IsSuddenChange").is(true);

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), unwind("HourDataList"), match(criteria1), project("DataGatherCode", "HourDataList.PollutantCode", "count").and(DateOperators.DateToString
                        .dateOf("MonitorTime").toString(dateFormate.get("pattern")).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")), hourCollection, Map.class).getMappedResults();

        return mappedResults;
    }

    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 4:37
     * @Description: 统计污染物排放量突变次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countFlowChangeDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        String datetype = null;
        String pattern = null;
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
        criteria1.and("HourFlowDataList.IsSuddenChange").is(true);

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), unwind("HourFlowDataList"), match(criteria1), project("DataGatherCode", "HourFlowDataList.PollutantCode", "count").and(DateOperators.DateToString
                        .dateOf("MonitorTime").toString(dateFormate.get("pattern")).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")), hourFlowCollection, Map.class).getMappedResults();

        return mappedResults;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 4:38
     * @Description: 统计污染物超限次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countOverDataDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        String datetype = null;
        String pattern = null;
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "PollutantCode", "count").and(DateOperators.DateToString
                        .dateOf("OverTime").toString(dateFormate.get("pattern")).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")), overData, Map.class).getMappedResults();

        return mappedResults;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 4:39
     * @Description: 统计污染物超域次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countEarlyWarnDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        String datetype = null;
        String pattern = null;
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "PollutantCode", "count").and(DateOperators.DateToString
                        .dateOf("EarlyWarnTime").toString(dateFormate.get("pattern")).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")), earlyWarnData, Map.class).getMappedResults();

        return mappedResults;
    }

    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 4:39
     * @Description: 统计污染物异常次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countExceptionDatayParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        String datetype = null;
        String pattern = null;
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime"))).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "PollutantCode", "count").and(DateOperators.DateToString
                        .dateOf("ExceptionTime").toString(dateFormate.get("pattern")).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")), exceptionData, Map.class).getMappedResults();

        return mappedResults;
    }

    /**
     * @author: xsm
     * @date: 2020/03/17 0017 下午 1:25
     * @Description: 统计污染物废水无流量异常次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countNoFlowExceptionDatayParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = null;
        String starttime = null;
        String endtime = null;
        String datetype = null;
        String pattern = null;
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = JSONArray.fromObject(paramMap.get("dgimns"));
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime"))).and("ExceptionType").is(NoFlowExceptionEnum.getCode());

        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "PollutantCode", "count").and(DateOperators.DateToString
                        .dateOf("ExceptionTime").toString(dateFormate.get("pattern")).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                , group("DataGatherCode", "MonitorTime", "PollutantCode").count().as("count")), exceptionData, Map.class).getMappedResults();

        return mappedResults;
    }

    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 4:57
     * @Description: 统计最近一个月企业超标报警数目
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countLastMonthAlarmByParamMap(Map<String, Object> paramMap) throws ParseException {
        Date now = new Date();
        //获取近一个月时间
        Calendar instance = Calendar.getInstance();
        instance.setTime(now);
        instance.add(Calendar.MONTH, -1);
        Date endtime = instance.getTime();
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in((List) paramMap.get("dgimns")).and("OverTime").gte(endtime).lte(now);
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("_id", "DataGatherCode"), group("DataGatherCode").count().as("count"), sort(Sort.Direction.DESC, "count"), limit(5)), overData, Map.class).getMappedResults();
        return mappedResults;
    }

    /**
     * @author: xsm
     * @date: 2020/05/12 0012 上午 11:05
     * @Description: 统计最近一个月企业c超标，异常报警数目之和
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countLastMonthAlarmAndExceptionByParamMap(Map<String, Object> paramMap) {
        Date now = new Date();
        String ym =DataFormatUtil.getDateYM(now);
        Date starttime = DataFormatUtil.getDateYMD(ym+"-01");
        Date endtime = DataFormatUtil.getDateYMD(ym+"-31");
        Criteria criteria = new Criteria();
        List<String> mns = (List<String>) paramMap.get("dgimns");
        criteria.and("DataGatherCode").in((List) paramMap.get("dgimns")).and("OverTime").gte(starttime).lte(endtime);
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("_id", "DataGatherCode"), group("DataGatherCode").count().as("count"), sort(Sort.Direction.DESC, "count")), overData, Map.class).getMappedResults();
        Criteria criteria2 = new Criteria();
        criteria2.and("DataGatherCode").in((List) paramMap.get("dgimns")).and("ExceptionTime").gte(starttime).lte(endtime);
        List<Map> mappedResults2 = mongoTemplate.aggregate(newAggregation(
                match(criteria2), project("_id", "DataGatherCode"), group("DataGatherCode").count().as("count"), sort(Sort.Direction.DESC, "count")), "ExceptionData", Map.class).getMappedResults();
        if (mappedResults != null && mappedResults.size() > 0) {
            if (mappedResults2 != null && mappedResults2.size() > 0) {
                List<Map> result = new ArrayList<>();
                for (String mn : mns) {
                    int count = 0;
                    for (Map map : mappedResults) {
                        if (mn.equals(map.get("_id").toString())) {
                            count += Integer.parseInt(map.get("count").toString());
                        }

                    }
                    for (Map map2 : mappedResults2) {
                        if (mn.equals(map2.get("_id").toString())) {
                            count += Integer.parseInt(map2.get("count").toString());
                        }
                    }
                    Map obj = new HashMap();
                    obj.put("_id", mn);
                    obj.put("count", count);
                    result.add(obj);
                }
                return result;
            } else {
                return mappedResults;
            }

        } else {
            if (mappedResults2 != null && mappedResults2.size() > 0) {
                return mappedResults2;
            } else {
                return null;
            }
        }

    }


    /**
     * @author: zhangzc
     * @date: 2019/8/17 14:26
     * @Description: 已读未读判断
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: ConditionalOperators.Cond
     */
    private static ConditionalOperators.Cond getIsReadCon(String userLog) {
        String readUserIds = "ReadUserIds";
        ArrayOperators.In judgeContainsUser = ArrayOperators.In.arrayOf(readUserIds).containsValue(userLog);
        ConditionalOperators.Cond userCond = ConditionalOperators.when(judgeContainsUser)
                .then("1")
                .otherwise("0");
        DataTypeOperators.Type $ReadUserIds = DataTypeOperators.typeOf(readUserIds);
        return ConditionalOperators.when(ComparisonOperators.Eq.valueOf($ReadUserIds).equalToValue("array"))
                .thenValueOf(userCond)
                .otherwise("0");
    }

    /**
     * @author: zhangzc
     * @date: 2019/8/19 16:17
     * @Description: 构建条件和unwind
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<AggregationOperation> getNDAndPFLQueryAggregations(OnlineAlarmCountQueryVO queryVO) {
        final String mnFieldName = "DataGatherCode";
        final String IsSuddenChangeFieldName = "IsSuddenChange";
        List<AggregationOperation> queryAggregations = new ArrayList<>();
        Criteria criteria = Criteria.where(mnFieldName).in(queryVO.getMns());
        if (queryVO.getStartTime() != null && queryVO.getEndTime() != null && StringUtils.isNotBlank(queryVO.getTimeFieldName())) {
            criteria.and(queryVO.getTimeFieldName()).gte(queryVO.getStartTime()).lte(queryVO.getEndTime());
        }
        if (StringUtils.isNotBlank(queryVO.getUnwindFieldName())) {
            criteria.and(queryVO.getUnwindFieldName() + "." + IsSuddenChangeFieldName).is(true);
        }
        if (queryVO.getExceptionType() != null) {
            String noFlowCode = NoFlowExceptionEnum.getCode();
            if (noFlowCode.equals(queryVO.getExceptionType())) {
                criteria.and("ExceptionType").is(noFlowCode);
            } else {
                criteria.and("ExceptionType").ne(noFlowCode);
            }
        }
        if (queryVO.getPollutantCodes() != null && queryVO.getPollutantCodes().size() > 0 && StringUtils.isNotBlank(queryVO.getPollutantCodeFieldName())) {
            criteria.and(queryVO.getPollutantCodeFieldName()).in(queryVO.getPollutantCodes());
        }
        queryAggregations.add(match(criteria));
        if (StringUtils.isNotBlank(queryVO.getUnwindFieldName())) {
            UnwindOperation unwind = unwind(queryVO.getUnwindFieldName());
            queryAggregations.add(unwind);
            MatchOperation match = match(Criteria.where(queryVO.getUnwindFieldName() + "." + IsSuddenChangeFieldName).is(true));
            queryAggregations.add(match);
        }
        return queryAggregations;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/26 0026 下午 5:28
     * @Description: 补全时间以及设置时间格式化表达式
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [datetype, starttime, endtime, pattern]
     * @throws:
     */
    private Map<String, String> getDateFormate(String datetype, String starttime, String endtime, String pattern) throws ParseException {
        Map<String, String> data = new HashMap<>();
        if ("day".equals(datetype)) {
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            pattern = "%Y-%m-%d";
        } else if ("month".equals(datetype)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            starttime = starttime + "-01 00:00:00";
            Calendar instance = Calendar.getInstance();
            instance.setTime(format.parse(endtime));
            int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
            endtime = endtime + "-" + actualMaximum + " 23:59:59";
            pattern = "%Y-%m";
        } else if ("year".equals(datetype)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
            starttime = starttime + "-01-01 00:00:00";
            Calendar instance = Calendar.getInstance();
            instance.setTime(format.parse(endtime + "-12"));
            int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
            endtime = endtime + "-12-" + actualMaximum + " 23:59:59";
            pattern = "%Y";
        } else if ("hour".equals(datetype)) {
            starttime = starttime + ":00:00";
            endtime = endtime + ":59:59";
            pattern = "%Y-%m-%d %H";
        } else if ("realtime".equals(datetype)) {
            pattern = "%Y-%m-%d %H:%M:%S";
        } else if ("minute".equals(datetype)) {
            starttime = starttime + ":00";
            endtime = endtime + ":59";
            pattern = "%Y-%m-%d %H:%M";
        }
        data.put("starttime", starttime);
        data.put("endtime", endtime);
        data.put("pattern", pattern);
        return data;
    }


    /**
     * @author: lip
     * @date: 2019/10/29 0029 上午 9:03
     * @Description: 自定义条件根据mn号统计报警数据
     * @updateUser:xsm
     * @updateDate:2020/03/13 0013 下午15:22
     * @updateDescription:当查询异常数据时 剔除无流量异常
     * @param:
     * @return:
     */

    @Override
    public List<Document> countAlarmDataForMnByParam(Map<String, Object> paramMap) {

        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = new Criteria();
        if ("ExceptionData".equals(paramMap.get("collection").toString())) {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
        } else {
            if ("SuddenRiseData".equals(paramMap.get("collection").toString())) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("DataType").is("MinuteData");
            }else{
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
            }
        }
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
            UnwindOperation unwind = unwind(unwindkey);
            aggregations.add(unwind);
        }
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode");
        aggregations.add(project(fields));
        GroupOperation groupOperation = group("DataGatherCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: zhangzhenchao
     * @date: 2019/11/2 15:39
     * @Description: 获取报警的mn号
     * @param:
     * @return:
     * @throws:
     */
    @Override
    public List<String> getAlarmMnsByParams(OnlineAlarmCountQueryVO queryVO) {
        final String mnFieldName = "DataGatherCode";
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project(fields(mnFieldName));
            if (queryVO.getUserId() != null) {
                projects = projects.and(getIsReadCon(queryVO.getUserId())).as("IsRead");
                aggregations.add(projects);
                aggregations.add(match(Criteria.where("IsRead").is("0")));
            }
            aggregations.add(group(mnFieldName));

        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        List<Document> mappedResults = aggregationResults.getMappedResults();

        List<String> mns = new ArrayList<>();
        for (Document document : mappedResults) mns.add(document.getString("_id"));
        return mns;
    }


    /**
     * @author: XSM
     * @date: 2021/08/03 15:06
     * @Description: 获取分级预警新菜单报警的mn号
     * @param:
     * @return:
     * @throws:
     */
    @Override
    public List<String> getAlarmMnsByParamsForHierarchicalEarly(OnlineAlarmCountQueryVO queryVO, Integer remindType) {
        final String mnFieldName = "MN";
        List<AggregationOperation> aggregations = new ArrayList<>();
            Criteria criteria = Criteria.where("MN").in(queryVO.getMns());
            criteria.and(queryVO.getTimeFieldName()).gte(queryVO.getStartTime()).lte(queryVO.getEndTime());
            if (queryVO.getExceptionType() != null) {
                String noFlowCode = NoFlowExceptionEnum.getCode();
                if (noFlowCode.equals(queryVO.getExceptionType())) {
                    criteria.and("ExceptionType").is(noFlowCode);
                } else {
                    criteria.and("ExceptionType").ne(noFlowCode);
                }
            }
            if (remindType == EarlyAlarmEnum2.getCode()){
                criteria.and("AlarmLevel").is(0);
            }else if (remindType == OverAlarmEnum2.getCode()){
                criteria.and("AlarmLevel").ne(0);
            }
            aggregations.add(match(criteria));
            aggregations.add(group("MN"));

        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        List<Document> mappedResults = aggregationResults.getMappedResults();

        List<String> mns = new ArrayList<>();
        for (Document document : mappedResults) mns.add(document.getString("_id"));
        return mns;
    }

    @Override
    public Map<String, Object> getEntPointPollutantMonthFlowDataByParam(Map<String, Object> param) {
        Map<String, Object> resultmap = new HashMap<>();
        Double total = 0d;
        String year = param.get("flowyear").toString();
        String lastyear = (Integer.valueOf(year)-1)+"";
        String pollutantcode = param.get("pollutantcode").toString();
        List<String> mns = (List<String>) param.get("mns");
        final String mongdb_moth_pfl = "MonthFlowData";
        //开始时间从前一年 1月开始
        Date starttime = DataFormatUtil.parseDate(lastyear + "-01-01 00:00:00");
        //当前月结束
        Date endtime = DataFormatUtil.parseDate(year + "-12-31 23:59:59");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(starttime).lte(endtime);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("MonthFlowDataList"));
        operations.add(match(Criteria.where("MonthFlowDataList.PollutantCode").is(pollutantcode)));
        operations.add(Aggregation.project("DataGatherCode","MonitorTime").and("MonthFlowDataList.PollutantFlow").as("PollutantFlow"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, mongdb_moth_pfl, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        List<String> ym_list = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<String> lastvalues = new ArrayList<>();
        if (documents.size()>0){
            for (int i =1;i<13;i++){
                String ym = "";
                String lastym = "";
                if (i<10){
                    ym = year+"-0"+i;
                    lastym = lastyear+"-0"+i;
                }else{
                    ym = year+"-"+i;
                    lastym = lastyear+"-"+i;
                }
                String value = "";
                String lastvalue = "";
                boolean ishavevalue = false;
                for (Document document : documents) {
                    //当前年
                    if (ym.equals( DataFormatUtil.getDateYM(document.getDate("MonitorTime")))){
                        if (document.get("PollutantFlow")!=null){
                            value = document.getString("PollutantFlow");
                            if (!"".equals(value)) {
                                total += Double.valueOf(value);
                                ishavevalue = true;
                            }
                        }
                    }
                    //上一年
                    if (lastym.equals( DataFormatUtil.getDateYM(document.getDate("MonitorTime")))){
                        if (document.get("PollutantFlow")!=null){
                            lastvalue = document.getString("PollutantFlow");
                        }
                    }
                }
                if (ishavevalue) {//以当前年 是否有月排放量数据 为主 进行存储
                    values.add(value);
                    lastvalues.add(lastvalue);
                    ym_list.add(i + "月");
                }
            }
        }
        resultmap.put("total",DataFormatUtil.SaveTwoAndSubZero(total));
        resultmap.put("timelist",ym_list);
        resultmap.put("newdata",values);
        resultmap.put("lastdata",lastvalues);
        return resultmap;
    }

    @Override
    public int getAlarmNumByParams(OnlineAlarmCountQueryVO queryVO) {
        final String mnFieldName = "DataGatherCode";
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project(fields(mnFieldName));
        projects = projects.and(getIsReadCon(queryVO.getUserId())).as("IsRead");
        aggregations.add(projects);
        aggregations.add(match(Criteria.where("IsRead").is("0")));
        aggregations.add(group(mnFieldName));
        aggregations.add(count().as("num"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getUniqueMappedResult() != null ? aggregationResults.getUniqueMappedResult().getInteger("num") : 0;
    }

    /**
     * @author: lip
     * @date: 2019/11/6 0006 下午 7:20
     * @Description: 自定义条件根据mn号、污染物、最新时间等报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countPollutantMaxTimeByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();

        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            UnwindOperation unwindOperation = unwind(unwindkey);
            aggregations.add(unwindOperation);
        }

        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = new Criteria();
        if (paramMap.get("remindtype") != null) {
            int remindtypecode = Integer.parseInt(paramMap.get("remindtype").toString());
            if (remindtypecode == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            } else if (remindtypecode == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
            } else {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
            }
        } else {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        }
        //Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        Fields fields;
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            fields = fields("DataGatherCode", timeKey, "maxtime").and("PollutantCode", unwindkey + ".PollutantCode");
        } else {
            fields = fields("DataGatherCode", timeKey, "maxtime", "PollutantCode","ExceptionType");
        }
        aggregations.add(project(fields));
        if("ExceptionData".equals(paramMap.get("collection").toString())){
            Map<String, Object> exceptiontypes = new HashMap<>();
            exceptiontypes.put("type", "$ExceptionType");
            GroupOperation groupOperation = group("DataGatherCode", "PollutantCode").count().as("countnum").max(timeKey).as("maxtime") .push(exceptiontypes).as("exceptiontypes");
            aggregations.add(groupOperation);
        }else{
            GroupOperation groupOperation = group("DataGatherCode", "PollutantCode").count().as("countnum").max(timeKey).as("maxtime");
            aggregations.add(groupOperation);
        }
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    /**
     * @author: lip
     * @date: 2019/11/6 0006 下午 7:20
     * @Description: 自定义条件根据mn号、污染物、最新时间等报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countDataGroupByMNByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            UnwindOperation unwindOperation = unwind(unwindkey);
            aggregations.add(unwindOperation);
        }
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = new Criteria();
        if (paramMap.get("remindtype") != null) {
            int remindtypecode = Integer.parseInt(paramMap.get("remindtype").toString());
            if (remindtypecode == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            } else if (remindtypecode == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
            } else {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
            }
        } else {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        }
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        Fields fields;
        GroupOperation groupOperation;
        if (timeKey.equals("ExceptionTime")) {
            fields = fields("DataGatherCode", "ExceptionType");
            groupOperation = group("DataGatherCode", "ExceptionType").count().as("countnum");
        } else {
            fields = fields("DataGatherCode");
            groupOperation = group("DataGatherCode").count().as("countnum");
        }
        aggregations.add(project(fields));
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }


    /**
     * @author: lip
     * @date: 2019/11/6 0006 下午 7:20
     * @Description: 自定义条件根据mn号、污染物、最新时间等报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countDataGroupByTimeByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            UnwindOperation unwindOperation = unwind(unwindkey);
            aggregations.add(unwindOperation);
        }
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        Criteria criteria = new Criteria();
        if (paramMap.get("remindtype") != null) {
            int remindtypecode = Integer.parseInt(paramMap.get("remindtype").toString());
            if (remindtypecode == CommonTypeEnum.RemindTypeEnum.WaterNoFlowEnum.getCode()) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
            } else if (remindtypecode == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
            } else {
                criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
            }
        } else {
            criteria.and("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime);
        }
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
        }
        aggregations.add(match(criteria));
        String timeType = paramMap.get("timetype").toString();
        String timeStyle = DataFormatUtil.getTimeStyleByTimeTypeForMongdb(timeType);
        if (timeKey.equals("ExceptionTime")) {
            aggregations.add(Aggregation.project(timeKey,"ExceptionType").and(DateOperators.DateToString
                    .dateOf(timeKey).toString(timeStyle).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorDate")
            );
            aggregations.add(Aggregation.group("MonitorDate","ExceptionType").count().as("countnum"));
        } else {
            aggregations.add(Aggregation.project(timeKey).and(DateOperators.DateToString
                    .dateOf(timeKey).toString(timeStyle).withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorDate")
            );
            aggregations.add(Aggregation.group("MonitorDate").count().as("countnum"));
        }
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }


    /**
     * @author: chengzq
     * @date: 2020/6/17 0017 上午 10:06
     * @Description: 统计排口报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> countAlarmsDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        Map<String, Object> resultMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = new ArrayList<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (List) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);

        List<Map> earlyWarn = new ArrayList<>();
        List<Map> exception = new ArrayList<>();
        List<Map> overdata = new ArrayList<>();
        List<Map> concentrationchangedata = new ArrayList<>();
        List<Map> onlyearlyWarn = new ArrayList<>();


        if (remindTypes.contains(ConcentrationChangeEnum.getCode())) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourData = mongoTemplate.aggregate(newAggregation(match(criteria), group("DataGatherCode").count().as("count")), hourCollection, Map.class).getMappedResults();
            earlyWarn.addAll(hourData);
            concentrationchangedata.addAll(hourData);
        }

        if (remindTypes.contains(FlowChangeEnum.getCode())) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourFlowData = mongoTemplate.aggregate(newAggregation(match(criteria), group("DataGatherCode").count().as("count")), hourFlowCollection, Map.class).getMappedResults();
            earlyWarn.addAll(hourFlowData);
        }

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria), group("DataGatherCode").count().as("count")), earlyWarnData, Map.class).getMappedResults();
            earlyWarn.addAll(early);
            onlyearlyWarn.addAll(early);
        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), group("DataGatherCode").count().as("count")), exceptionData, Map.class).getMappedResults();
            exception.addAll(exceptiondata);
        }
        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), group("DataGatherCode").count().as("count")), overData, Map.class).getMappedResults();
            overdata.addAll(exceptiondata);
        }

        resultMap.put("earlywarn", earlyWarn);//包含浓度突变，排放量突变，预警
        resultMap.put("exception", exception);
        resultMap.put("overdata", overdata);
        resultMap.put("concentrationchangedata", concentrationchangedata);
        resultMap.put("onlyearlyWarn", onlyearlyWarn);//只包含预警
        return resultMap;
    }


    /**
     * @author: chengzq
     * @date: 2020/6/17 0017 上午 10:05
     * @Description: 统计排口报警次数（通过时间分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> countAlarmsInfoDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        Map<String, Object> resultMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = new ArrayList<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (List) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);

        List<Map> earlyWarn = new ArrayList<>(); //包含浓度突变，排放量突变，预警
        List<Map> onlyearlyWarn = new ArrayList<>(); //只包含预警
        List<Map> concentrationchange = new ArrayList<>();
        List<Map> flowchange = new ArrayList<>();
        List<Map> exception = new ArrayList<>(); //所有异常类型
        List<Map> everyexception = new ArrayList<>();//不同类型异常集合
        List<Map> overdata = new ArrayList<>();


        if (remindTypes.contains(ConcentrationChangeEnum.getCode())) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("MinuteDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourData = mongoTemplate.aggregate(newAggregation(match(criteria),project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    , group("DataGatherCode", "MonitorTime").count().as("count")), minuteCollection, Map.class).getMappedResults();
            earlyWarn.addAll(hourData);
            concentrationchange.addAll(hourData);
        }

        if (remindTypes.contains(FlowChangeEnum.getCode())) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourFlowData = mongoTemplate.aggregate(newAggregation(match(criteria),project("DataGatherCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    ,group("DataGatherCode", "MonitorTime").count().as("count")), hourFlowCollection, Map.class).getMappedResults();
            earlyWarn.addAll(hourFlowData);
            flowchange.addAll(hourFlowData);
        }

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria),project("DataGatherCode").and(DateOperators.DateToString.dateOf("EarlyWarnTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                    , group("DataGatherCode", "MonitorTime").count().as("count")), earlyWarnData, Map.class).getMappedResults();
            earlyWarn.addAll(early);
            onlyearlyWarn.addAll(early);
        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria),project("DataGatherCode").and(DateOperators.DateToString.dateOf("ExceptionTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime").count().as("count")), exceptionData, Map.class).getMappedResults();
            //统计不同类型异常
            List<Map> everyExceptionData = mongoTemplate.aggregate(newAggregation(match(criteria),project("DataGatherCode","ExceptionType").and(DateOperators.DateToString.dateOf("ExceptionTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime","ExceptionType").count().as("count")), exceptionData, Map.class).getMappedResults();
            exception.addAll(exceptiondata);
            everyexception.addAll(everyExceptionData);
        }
        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria),project("DataGatherCode").and(DateOperators.DateToString.dateOf("OverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"),
                    group("DataGatherCode", "MonitorTime").count().as("count")), overData, Map.class).getMappedResults();
            overdata.addAll(exceptiondata);
        }

        resultMap.put("earlywarn", earlyWarn);
        resultMap.put("onlyearlywarn", onlyearlyWarn);
        resultMap.put("concentrationchange", concentrationchange);
        resultMap.put("flowchange", flowchange);
        resultMap.put("exception", exception);
        resultMap.put("everyexception", everyexception);
        resultMap.put("overdata", overdata);
        return resultMap;
    }


    /**
     * @author: chengzq
     * @date: 2020/6/17 0017 上午 10:05
     * @Description: 统计污染物报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String,Long>> countPollutantAlarmsInfoDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = new ArrayList<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (List) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);

        List<Map<String,Long>> allPollutant = new ArrayList<>();


        if (remindTypes.contains(ConcentrationChangeEnum.getCode())) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourData = mongoTemplate.aggregate(newAggregation(match(criteria),project("HourDataList")), hourCollection, Map.class).getMappedResults();
            Map<String, Long> collect = hourData.stream().filter(m -> m.get("IsOverStandard") != null && (boolean) m.get("IsOverStandard")).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString(), Collectors.counting()));
            if(!collect.isEmpty())
            allPollutant.add(collect);
        }

        if (remindTypes.contains(FlowChangeEnum.getCode())) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourFlowData = mongoTemplate.aggregate(newAggregation(match(criteria),project("HourFlowDataList")), hourFlowCollection, Map.class).getMappedResults();
            Map<String, Long> collect = hourFlowData.stream().filter(m -> m.get("IsOverStandard") != null && (boolean) m.get("IsOverStandard")).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString(), Collectors.counting()));
            if(!collect.isEmpty())
            allPollutant.add(collect);
        }

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria),project("PollutantCode")), earlyWarnData, Map.class).getMappedResults();
            Map<String, Long> pollutantCode = early.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString(), Collectors.counting()));
            if(!pollutantCode.isEmpty())
            allPollutant.add(pollutantCode);

        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria),project("PollutantCode")), exceptionData, Map.class).getMappedResults();
            Map<String, Long> pollutantCode = exceptiondata.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString(), Collectors.counting()));
            if(!pollutantCode.isEmpty())
            allPollutant.add(pollutantCode);
        }
        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria),project("PollutantCode")), overData, Map.class).getMappedResults();
            Map<String, Long> pollutantCode = exceptiondata.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString(), Collectors.counting()));
            if(!pollutantCode.isEmpty())
            allPollutant.add(pollutantCode);
        }

        return allPollutant;
    }


    /**
     * @author: chengzq
     * @date: 2020/12/10 0010 下午 5:26
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countSecurityAlarmTypeDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = new ArrayList<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (List) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);
        List<Map> alarmdata = new ArrayList<>();

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            Query query = new Query();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            query.addCriteria(criteria);
            long count = mongoTemplate.count(query, earlyWarnData);
            Map<String,Object> data=new LinkedHashMap<>();
            data.put("_id",10);
            data.put("count",count);
            alarmdata.add(data);
        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            Query query = new Query();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            query.addCriteria(criteria);
            long count = mongoTemplate.count(query, exceptionData);
            Map<String,Object> data=new LinkedHashMap<>();
            data.put("_id",11);
            data.put("count",count);
            alarmdata.add(data);
        }

        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime"))).and("AlarmLevel").gt(0);
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), group("AlarmLevel").count().as("count")), overData, Map.class).getMappedResults();
            alarmdata.addAll(exceptiondata);
        }

        return alarmdata;
    }



    /**
     * @author: xsm
     * @date: 2019/11/06 0006 下午 3:48
     * @Description: 统计企业预警、超限、异常等报警类型的月报警条数和上月对比信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countAlarmMonthDataByParamMap(String preStartTime, String endtime, List<String> mns) {
        Map<String, Object> result = new HashMap<>();
        //超阈值
        Map<String, Object> earlyData = countOnePollutionAlarmMonthDataByParamMap(mns, preStartTime, endtime, "EarlyWarnTime", earlyWarnData);
        //浓度突变
        Map<String, Object> conData = countPollutionConcentrationOrFlowChangeByParamMap(mns, preStartTime, endtime, ConcentrationChangeEnum.getCode());
        //排放量突变
        Map<String, Object> flowData = countPollutionConcentrationOrFlowChangeByParamMap(mns, preStartTime, endtime, FlowChangeEnum.getCode());
        //超限
        Map<String, Object> overData = countOnePollutionAlarmMonthDataByParamMap(mns, preStartTime, endtime, "OverTime", "OverData");
        //异常
        Map<String, Object> exceptionData = countOnePollutionAlarmMonthDataByParamMap(mns, preStartTime, endtime, "ExceptionTime", "ExceptionData");
        result.put("earlydata", earlyData);
        result.put("overdata", overData);
        result.put("exceptiondata", exceptionData);
        result.put("concentrationdata", conData);
        result.put("flowdata", flowData);
        return result;
    }


    private Map<String, Object> countPollutionConcentrationOrFlowChangeByParamMap(List<String> mns, String preStartTime, String endtime, Integer type) {
        Map<String, Object> earlyData = new HashMap<>();
        if (mns != null && mns.size() > 0) {
            //查询条件
            Date startDate = DataFormatUtil.getDateYMDHMS(preStartTime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            preStartTime = DataFormatUtil.getDateYM(startDate);
            endtime = DataFormatUtil.getDateYM(endDate);
            Criteria criteria = Criteria.where("DataGatherCode").in(mns);
            String collection = "";
            if (type == ConcentrationChangeEnum.getCode()) {//浓度突变
                criteria.and("MinuteDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
                collection = minuteCollection;
            } else if (type == FlowChangeEnum.getCode()) {// 排放量突变
                criteria.and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
                collection = hourCollection;
            }
            List<Document> mappedResults = mongoTemplate.aggregate(newAggregation(
                    match(criteria),
                    project("MonthTime", "num").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonthTime"),
                    group("MonthTime").count().as("num")),
                    collection,
                    Document.class).getMappedResults();
            int num = 0;
            int num2 = 0;
            if (mappedResults != null && mappedResults.size() > 0) {
                for (Document document : mappedResults) {
                    String thetime = document.getString("_id");
                    if (thetime.equals(preStartTime)) {
                        num = document.getInteger("num");
                    }
                    if (thetime.equals(endtime)) {
                        num2 = document.getInteger("num");
                    }
                }
            }
            earlyData = getGrowthRate(num, num2);
        } else {
            earlyData.put("num", 0);
            earlyData.put("change", "nodata");
            earlyData.put("changevalue", "");
            earlyData.put("previousnum", 0);
        }
        return earlyData;
    }


    /**
     * @author: xsm
     * @date: 2019/11/06 0006 下午 4:57
     * @Description: 统计单个企业当月和上个月报警次数，和同比信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> countOnePollutionAlarmMonthDataByParamMap(List<String> mns, String preStartTime, String endtime, String timekey, String collection) {
        Map<String, Object> earlyData = new HashMap<>();
        if (mns != null && mns.size() > 0) {
            List<AggregationOperation> operations = new ArrayList<>();
            Date startDate = DataFormatUtil.getDateYMDHMS(preStartTime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            preStartTime = DataFormatUtil.getDateYM(startDate);
            endtime = DataFormatUtil.getDateYM(endDate);
            operations.add(
                    Aggregation.match(
                            Criteria.where("DataGatherCode").in(mns).and(timekey).gte(startDate).lte(endDate)
                    )
            );
            operations.add(Aggregation.project("MonthTime", "num").and(DateOperators.DateToString.dateOf(timekey).toString("%Y-%m").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonthTime"));
            GroupOperation group = group("MonthTime").count().as("num");
            operations.add(group);
            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection, Document.class);
            List<Document> documents = results.getMappedResults();
            int num = 0;
            int num2 = 0;
            if (documents.size() > 0) {
                for (Document document : documents) {
                    String thetime = document.getString("_id");
                    if (thetime.equals(preStartTime)) {
                        num = document.getInteger("num");
                    }
                    if (thetime.equals(endtime)) {
                        num2 = document.getInteger("num");
                    }
                }
            }
            earlyData = getGrowthRate(num, num2);
        } else {
            earlyData.put("num", 0);
            earlyData.put("change", "nodata");
            earlyData.put("changevalue", "");
            earlyData.put("previousnum", 0);

        }
        return earlyData;
    }

    private Map<String, Object> getGrowthRate(Integer num, Integer num2) {
        Map<String, Object> earlyData = new HashMap<>();
        String change = "";
        String value = "";
        // 同比超标数据
        if (num > 0) {//除数不能为0
            if (num2 > num) {// 增长up
                change = "up";
                value = DataFormatUtil.SaveTwoAndSubZero(100 * (double) ((num2 - num) / num));
            } else if (num2 < num) {// 减少
                change = "down";
                value = DataFormatUtil.SaveTwoAndSubZero(100 * (double) ((num - num2) / num));
            } else {
                change = "unchanged";
            }
        } else {
            change = "noData";
        }
        earlyData.put("change", change);
        earlyData.put("changevalue", value);
        earlyData.put("num", num2);
        earlyData.put("previousnum", num);
        return earlyData;
    }

    /**
     * @author: xsm
     * @date: 2019/11/07 0007 上午 11:41
     * @Description: 统计企业下主要污染物的总排放量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getKeyPollutantYearFlowData(String starttime, String endtime, List<String> mns, List<String> pollutants, Map<String, Object> namemap) {
        List<Map<String, Object>> listdata = new ArrayList<>();
        Criteria criteria = Criteria.where("MonitorTime").gte(DataFormatUtil.getDateYMDHMS(starttime + "-01-01 00:00:00")).lte(DataFormatUtil.getDateYMDHMS(endtime + "-12-31 23:59:59")).and("DataGatherCode").in(mns).and("PollutantCode").in(pollutants);
        Aggregation aggregation = newAggregation(
                unwind("YearFlowDataList"),
                project("DataGatherCode", "MonitorTime", "YearFlowDataList.PollutantCode", "YearFlowDataList.PollutantFlow").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("YearTime"),
                match(criteria),
                project("DataGatherCode", "YearTime", "PollutantCode", "PollutantFlow").andExclude("_id")
        );
        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, "YearFlowData", Document.class).getMappedResults();
        if (mappedResults != null && mappedResults.size() > 0) {
            for (String str : pollutants) {
                Double oldnum = 0d;
                Double newnum = 0d;
                for (Document document : mappedResults) {
                    if (str.equals(document.getString("PollutantCode"))) {//两个污染物相等
                        if (starttime.equals(document.getString("YearTime"))) {//和去年年份相等
                            if (document.get("PollutantFlow") != null) {
                                oldnum += Double.parseDouble(document.getString("PollutantFlow"));
                            }
                        }
                        if (endtime.equals(document.getString("YearTime"))) {//和今年年份相等
                            if (document.get("PollutantFlow") != null) {
                                newnum += Double.parseDouble(document.getString("PollutantFlow"));
                            }
                        }
                    }
                }
                if (oldnum > 0 || newnum > 0) {//判断是否有该污染物的值
                    Map<String, Object> map = new HashMap<>();
                    String change = "";
                    String value = "";
                    if (oldnum > 0) {//除数不能为0
                        if (newnum > oldnum) {// 增长up
                            change = "up";
                            value = DataFormatUtil.SaveTwoAndSubZero(100 * ((newnum - oldnum) / oldnum));
                        } else if (newnum < oldnum) {// 减少
                            change = "down";
                            value = DataFormatUtil.SaveTwoAndSubZero(100 * ((oldnum - newnum) / oldnum));
                        } else {
                            change = "unchanged";
                        }
                    } else {
                        change = "noData";
                    }
                    map.put("code", str);
                    map.put("name", namemap.get(str));
                    map.put("change", change);
                    map.put("changevalue", value);
                    map.put("newyearflow", (newnum > 0 || newnum < 0) ? DataFormatUtil.SaveTwoAndSubZero(newnum) : 0);
                    map.put("oldyearflow", (oldnum > 0 || oldnum < 0) ? DataFormatUtil.SaveTwoAndSubZero(oldnum) : 0);
                    listdata.add(map);
                }
            }
        }
        return listdata;
    }

    /**
     * @author: xsm
     * @date: 2019/11/8 14:06
     * @Description: 根据时间、MN号统计浓度突变或排放量突变报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countConcentrationAndFlowChangeNumGroupByMNAndTime(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.notNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must not be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project("DataGatherCode").and(dateToString).as("MonitorDate");
        aggregations.add(projects);
        aggregations.add(group("DataGatherCode", "MonitorDate").count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2019/11/08 14:33
     * @Description: 根据时间、MN号统计超阈值、超限、异常报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countEarlyOrOverAlarmNumGroupByMNAndTime(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        if (timeStyle.equals("hour")) {  //不统计天数据
            Criteria.where("DataType").ne("DayData");
        }
        DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project("DataGatherCode").and(dateToString).as("MonitorDate");
        aggregations.add(projects);
        aggregations.add(group("DataGatherCode", "MonitorDate").count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 09:26
     * @Description: 根据时间范围统计超标报警的点位名称和企业名称
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOverAlarmMonitorPointInfoByParam(String starttime, String endtime, List<Map<String, Object>> mndata) {
        Map<String, Object> pollutionmn = new HashMap<>();//企业名称
        Map<String, Object> outputmn = new HashMap<>();//点位名称
        Map<String, Object> pointtypemn = new HashMap<>();//点位类型
        List<Map<String, Object>> waterlist = new ArrayList<>();
        List<Map<String, Object>> gaslist = new ArrayList<>();
        List<Map<String, Object>> rainlist = new ArrayList<>();
        List<Map<String, Object>> stinklist = new ArrayList<>();
        List<Map<String, Object>> dustlist = new ArrayList<>();
        List<Map<String, Object>> microlist = new ArrayList<>();
        List<Map<String, Object>> resultlist = new ArrayList<>();
        Set<String> mnset = new HashSet<>();
        if (mndata != null && mndata.size() > 0) {
            for (Map<String, Object> map : mndata) {
                if (map.get("DGIMN") != null && !"".equals(map.get("DGIMN").toString())) {
                    mnset.add(map.get("DGIMN").toString());
                    if (map.get("PollutionName") != null && !"".equals(map.get("PollutionName").toString())) {
                        pollutionmn.put(map.get("DGIMN").toString(), map.get("PollutionName"));
                    }
                    if (map.get("outputname") != null && !"".equals(map.get("outputname").toString())) {
                        outputmn.put(map.get("DGIMN").toString(), map.get("outputname"));
                    }
                    if (map.get("typename") != null && !"".equals(map.get("typename").toString())) {
                        pointtypemn.put(map.get("DGIMN").toString(), map.get("typename"));
                    }
                }
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Date startDate = DataFormatUtil.getDateYMDHMS(starttime + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(endtime + " 23:59:59");
            operations.add(
                    Aggregation.match(
                            Criteria.where("DataGatherCode").in(mnset).and("OverTime").gte(startDate).lte(endDate)
                    )
            );
            operations.add(Aggregation.project("DataGatherCode"));
            GroupOperation group = group("DataGatherCode");
            operations.add(group);
            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "OverData", Document.class);
            List<Document> documents = results.getMappedResults();
            if (documents.size() > 0) {
                for (Document document : documents) {
                    Map<String, Object> map = new HashMap<>();
                    String mn = document.getString("_id");
                    map.put("monitorpointname", outputmn.get(mn) != null ? outputmn.get(mn) : "");
                    map.put("pollution", pollutionmn.get(mn) != null ? pollutionmn.get(mn) : "");
                    if ("water".equals(pointtypemn.get(mn).toString())) {//废水
                        waterlist.add(map);
                    } else if ("gas".equals(pointtypemn.get(mn).toString())) {//废气
                        gaslist.add(map);
                    } else if ("rain".equals(pointtypemn.get(mn).toString())) {//雨水
                        rainlist.add(map);
                    } else if ("stink".equals(pointtypemn.get(mn).toString())) {//恶臭
                        stinklist.add(map);
                  /*  } else if ("dust".equals(pointtypemn.get(mn).toString())) {//扬尘
                        dustlist.add(map);
                    } else if ("micro".equals(pointtypemn.get(mn).toString())) {//微站
                        microlist.add(map);*/
                    }
                }
            }
        }
        if (waterlist != null && waterlist.size() > 0) {
            List<Map<String, Object>> collect = waterlist.stream().sorted(Comparator.comparing((Map m) -> m.get("pollution").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("flagname", "water");
            map.put("datalist", collect);
            resultlist.add(map);
        }
        if (gaslist != null && gaslist.size() > 0) {
            List<Map<String, Object>> collect = gaslist.stream().sorted(Comparator.comparing((Map m) -> m.get("pollution").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("flagname", "gas");
            map.put("datalist", collect);
            resultlist.add(map);
        }
        if (rainlist != null && rainlist.size() > 0) {
            List<Map<String, Object>> collect = rainlist.stream().sorted(Comparator.comparing((Map m) -> m.get("pollution").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("flagname", "rain");
            map.put("datalist", collect);
            resultlist.add(map);
        }
        if (stinklist != null && stinklist.size() > 0) {
            List<Map<String, Object>> collect = stinklist.stream().sorted(Comparator.comparing((Map m) -> m.get("pollution").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("flagname", "stink");
            map.put("datalist", collect);
            resultlist.add(map);
        }
        if (dustlist != null && dustlist.size() > 0) {
            List<Map<String, Object>> collect = dustlist.stream().sorted(Comparator.comparing((Map m) -> m.get("pollution").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("flagname", "dust");
            map.put("datalist", collect);
            resultlist.add(map);
        }
        if (microlist != null && microlist.size() > 0) {
            List<Map<String, Object>> collect = microlist.stream().sorted(Comparator.comparing((Map m) -> m.get("pollution").toString()).thenComparing((Map m) -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>();
            map.put("flagname", " micro");
            map.put("datalist", collect);
            resultlist.add(map);
        }
        return resultlist;
    }

    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 10:56
     * @Description: 统计异常报警各异常类型的报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countExceptionTypeAlarmData(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
       /* DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project("ExceptionType").and(queryVO.getPollutantCodeFieldName()).as("PollutantCode").and(dateToString).as("Time");
        aggregations.add(projects);*/
        // 加8小时
        ProjectionOperation add8h = Aggregation.project("ExceptionType", queryVO.getTimeFieldName()).and(queryVO.getPollutantCodeFieldName()).as("PollutantCode")
                .andExpression("add(" + queryVO.getTimeFieldName() + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("ExceptionType", "date8", "PollutantCode").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("Time");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("ExceptionType", "PollutantCode").count().as("num").max("Time").as("MaxTime"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }


    /**
     * @author: chengzq
     * @date: 2019/11/29 0029 上午 10:29
     * @Description: 统计一段时间内预警和报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countAlarmAndExceptionDataByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<String> dgimns = new ArrayList<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (List) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);


        List<Map> resultList = new ArrayList<>();


        if (remindTypes.contains(ConcentrationChangeEnum.getCode())) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourData = mongoTemplate.aggregate(newAggregation(match(criteria), project("MonitorTime").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d %H")
                    .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("MonitorTime").count().as("hourcount")), hourCollection, Map.class).getMappedResults();
            resultList.addAll(hourData);
        }

        if (remindTypes.contains(FlowChangeEnum.getCode())) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourFlowData = mongoTemplate.aggregate(newAggregation(match(criteria), project("MonitorTime").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d %H")
                    .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("MonitorTime").count().as("hourflowcount")), hourFlowCollection, Map.class).getMappedResults();
            resultList.addAll(hourFlowData);
        }

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria), project("EarlyWarnTime").and(DateOperators.DateToString.dateOf("EarlyWarnTime").toString("%Y-%m-%d %H")
                    .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("MonitorTime").count().as("earlywarncount")), earlyWarnData, Map.class).getMappedResults();
            resultList.addAll(early);
        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), project("ExceptionTime").and(DateOperators.DateToString.dateOf("ExceptionTime").toString("%Y-%m-%d %H")
                    .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("MonitorTime").count().as("exceptioncount")), exceptionData, Map.class).getMappedResults();
            resultList.addAll(exceptiondata);
        }
        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), project("OverTime").and(DateOperators.DateToString.dateOf("OverTime").toString("%Y-%m-%d %H")
                    .withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"), group("MonitorTime").count().as("overdatacount")), overData, Map.class).getMappedResults();
            resultList.addAll(exceptiondata);
        }
        return resultList;
    }


    /**
     * @author: chengzq
     * @date: 2019/11/29 0029 上午 10:29
     * @Description: 统计一段时间内预警和报警次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countAlarmAndExceptionByParamMap(Map<String, Object> paramMap) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Set<String> dgimns = new HashSet<>();
        String starttime = "";
        String endtime = "";
        String datetype = "";
        String pattern = "";
        List<Integer> remindTypes = new ArrayList<>();
        if (paramMap.get("datetype") != null) {
            datetype = paramMap.get("datetype").toString();
        }
        if (paramMap.get("dgimns") != null) {
            dgimns = (Set) paramMap.get("dgimns");
        }
        if (paramMap.get("starttime") != null) {
            starttime = paramMap.get("starttime").toString();
        }
        if (paramMap.get("endtime") != null) {
            endtime = paramMap.get("endtime").toString();
        }
        if (paramMap.get("remindTypes") != null) {
            remindTypes = (List<Integer>) paramMap.get("remindTypes");
        }

        Map<String, String> dateFormate = getDateFormate(datetype, starttime, endtime, pattern);


        List<Map> resultList = new ArrayList<>();


        if (remindTypes.contains(ConcentrationChangeEnum.getCode())) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourData = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("hourcount")), hourCollection, Map.class).getMappedResults();
            resultList.addAll(hourData);
        }

        if (remindTypes.contains(FlowChangeEnum.getCode())) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> hourFlowData = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("hourflowcount")), hourFlowCollection, Map.class).getMappedResults();
            resultList.addAll(hourFlowData);
        }

        if (remindTypes.contains(EarlyAlarmEnum.getCode())) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("EarlyWarnTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> early = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("earlywarncount")), earlyWarnData, Map.class).getMappedResults();
            resultList.addAll(early);
        }


        if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("exceptioncount")), exceptionData, Map.class).getMappedResults();
            resultList.addAll(exceptiondata);
        }
        if (remindTypes.contains(OverAlarmEnum.getCode())) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("OverTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("overdatacount")), overData, Map.class).getMappedResults();
            resultList.addAll(exceptiondata);
        }
        return resultList;
    }


    /**
     * @author: xsm
     * @date: 2019/12/02 0002 下午 3:20
     * @Description: 根据自定义参数获取异常报警各异常类型数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getExceptionAlarmDetailDataByParam(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Date startTime = (Date) paramMap.get("starttime");
            Date endTime = (Date) paramMap.get("endtime");
            String exceptiontype = (String) paramMap.get("exceptiontype");
            int monitortype = Integer.parseInt(paramMap.get("pollutanttype").toString());
            Map<String, Object> codemap = (Map<String, Object>) paramMap.get("codemap");
            Criteria criteria = new Criteria();
            List<AggregationOperation> operations = new ArrayList<>();
            criteria.and("DataGatherCode").is(paramMap.get("dgimn").toString()).and("ExceptionTime").gte(startTime).lte(endTime).and("ExceptionType").is(exceptiontype.toString());
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("ExceptionTime", "PollutantCode","DataType"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, "ExceptionTime"));
            Map<String, Object> timeAndRead = new HashMap<>();
            timeAndRead.put("time", "$ExceptionTime");
            timeAndRead.put("timetype", "$DataType");
            operations.add(
                    Aggregation.group("PollutantCode")
                            .push(timeAndRead).as("timeList")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, exceptionData, Document.class);
            List<Document> documents = pageResults.getMappedResults();
            //手动分页
            int size = documents.size();
            int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
            int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
            if (size > pageStart) {
                documents = documents.subList(pageStart, pageEnd);
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultmap.put("total", size);
            }
            int interval;
            String ymdhms;
            //获取配置的各类型排口的间隔时间
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//废气
                interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//废水、雨水
                interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
            } else {//其它类型监测点
                interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
            }
            List<String> dateList;
            String continuityvalue;
            if (documents.size() > 0) {
                for (Document document : documents) {
                    String pollutantcode = document.getString("_id");
                    List<String> strlist = new ArrayList<>();
                    List<Map<String, Object>> timetypelist = new ArrayList<>();
                    Map<String, Object> map = new HashMap<>();
                    Map<String, Object> timeandtype = new HashMap<>();
                    List<Document> timelist = (List<Document>) document.get("timeList");
                    dateList = new ArrayList<>();
                    for (Document time : timelist) {
                        ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                        if (!dateList.contains(ymdhms)) {
                            dateList.add(ymdhms);
                            String ymn = ymdhms.substring(11, 16);
                            if (timeandtype.get(ymn)!=null){
                                if ("RealTimeData".equals(time.getString("timetype"))){
                                    timeandtype.put(ymn,time.get("timetype"));
                                }
                            }else{
                                timeandtype.put(ymn,time.get("timetype"));
                            }
                        }
                    }
                    continuityvalue = DataFormatUtil.mergeContinueDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                    strlist = Arrays.asList(continuityvalue.split("、"));
                    if (strlist.size()>0){
                        for (String timestr:strlist){
                            Map<String, Object> onemap = new HashMap<>();
                            if (timestr.length()>4){
                                String[] timestrlist = timestr.split("-");
                                onemap.put("time",timestr);
                                onemap.put("timetype",timeandtype.get(timestrlist[0]));
                            }else{
                                onemap.put("time",timestr);
                                onemap.put("timetype",timeandtype.get(timestr));
                            }
                            timetypelist.add(onemap);
                        }
                    }
                    map.put("code", pollutantcode);
                    map.put("name", codemap.get(pollutantcode));
                    map.put("timelist", timetypelist);
                    resultlist.add(map);
                }
            }
            resultmap.put("datalist", resultlist);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 8:47
     * @Description: 根据自定义参数获取超阈值或超限报警的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getEarlyOrOverAlarmDetailDataByParam(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Date startTime = (Date) paramMap.get("starttime");
            Date endTime = (Date) paramMap.get("endtime");
            Integer remindtype = (Integer) paramMap.get("remindtype");
            int monitortype = Integer.parseInt(paramMap.get("pollutanttype").toString());
            Map<String, Object> codemap = (Map<String, Object>) paramMap.get("codemap");
            String timekey = "";
            String collection = "";
            if (remindtype == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {//超阈值
                timekey = "EarlyWarnTime";
                collection = "EarlyWarnData";
            } else if (remindtype == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) {//超限
                timekey = "OverTime";
                collection = "OverData";
            }
            Criteria criteria = new Criteria();
            List<AggregationOperation> operations = new ArrayList<>();
            criteria.and("DataGatherCode").is(paramMap.get("dgimn").toString()).and(timekey).gte(startTime).lte(endTime);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project(timekey, "PollutantCode","DataType"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, timekey));
            Map<String, Object> timeAndRead = new HashMap<>();
            timeAndRead.put("time", "$"+timekey);
            timeAndRead.put("timetype", "$DataType");
            operations.add(
                    Aggregation.group("PollutantCode")
                            .push(timeAndRead).as("timeList")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> documents = pageResults.getMappedResults();
            //手动分页
            int size = documents.size();
            int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
            int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
            if (size > pageStart) {
                documents = documents.subList(pageStart, pageEnd);
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultmap.put("total", size);
            }
            int interval;
            String ymdhms;
            //获取配置的各类型排口的间隔时间
            if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//废气
                interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitortype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//废水、雨水
                interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutputover.minute"));
            } else if (monitortype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
            } else {//其它类型监测点
                interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
            }
            List<String> dateList;
            String continuityvalue;
            if (documents.size() > 0) {
                for (Document document : documents) {
                    String pollutantcode = document.getString("_id");
                    List<String> strlist = new ArrayList<>();
                    List<Map<String, Object>> timetypelist = new ArrayList<>();
                    Map<String, Object> map = new HashMap<>();
                    Map<String, Object> timeandtype = new HashMap<>();
                    List<Document> timelist = (List<Document>) document.get("timeList");
                    dateList = new ArrayList<>();
                    for (Document time : timelist) {
                        ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                        if (!dateList.contains(ymdhms)) {
                            dateList.add(ymdhms);
                            String ymn = ymdhms.substring(11, 16);
                            if (timeandtype.get(ymn)!=null){
                                if ("RealTimeData".equals(time.getString("timetype"))){
                                    timeandtype.put(ymn,time.get("timetype"));
                                }
                            }else{
                                timeandtype.put(ymn,time.get("timetype"));
                            }
                        }
                    }
                    continuityvalue = DataFormatUtil.mergeContinueDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                    strlist = Arrays.asList(continuityvalue.split("、"));
                    if (strlist.size()>0){
                        for (String timestr:strlist){
                            Map<String, Object> onemap = new HashMap<>();
                            if (timestr.length()>4){
                                String[] timestrlist = timestr.split("-");
                                onemap.put("time",timestr);
                                onemap.put("timetype",timeandtype.get(timestrlist[0]));
                            }else{
                                onemap.put("time",timestr);
                                onemap.put("timetype",timeandtype.get(timestr));
                            }
                            timetypelist.add(onemap);
                        }
                    }
                    map.put("code", pollutantcode);
                    map.put("name", codemap.get(pollutantcode));
                    map.put("timelist", timetypelist);
                    resultlist.add(map);
                }
            }
            resultmap.put("datalist", resultlist);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/11 0011 上午 9:09
     * @Description: 根据自定义参数获取浓度突变或排放量突变报警的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> getConcentrationOrFlowChangeDetailDataByParam(Map<String, Object> paramMap) {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Integer pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
            Integer pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
            Date startTime = (Date) paramMap.get("starttime");
            Date endTime = (Date) paramMap.get("endtime");
            Integer remindtype = (Integer) paramMap.get("remindtype");
            Map<String, Object> codemap = (Map<String, Object>) paramMap.get("codemap");
            String listkey = "";
            String collection = "";
            if (remindtype == FlowChangeEnum.getCode()) {  //排放量
                listkey = "HourFlowDataList";
                collection = "HourFlowData";
            } else if (remindtype == ConcentrationChangeEnum.getCode()) { //浓度
                listkey = "HourDataList";
                collection = "HourData";
            }
            Criteria criteria = new Criteria();
            Criteria criteria1 = new Criteria();
            criteria.and("DataGatherCode").is(paramMap.get("dgimn").toString()).and(listkey + ".IsSuddenChange").is(true).and("MonitorTime").gte(startTime).lte(endTime);
            criteria1.and(listkey + ".IsSuddenChange").is(true);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.unwind(listkey));
            operations.add(Aggregation.match(criteria1));
            operations.add(Aggregation.project(listkey + ".PollutantCode").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%H").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime"));
            //插入分页、排序条件
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
            Map<String, Object> timeAndRead = new HashMap<>();
            timeAndRead.put("time", "$MonitorTime");
            operations.add(
                    Aggregation.group("PollutantCode")
                            .push(timeAndRead).as("timeList")
            );
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> documents = pageResults.getMappedResults();
            //手动分页
            int size = documents.size();
            int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
            int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
            if (size > pageStart) {
                documents = documents.subList(pageStart, pageEnd);
            }
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                resultmap.put("total", size);
            }
            if (documents.size() > 0) {
                for (Document document : documents) {
                    String pollutantcode = document.getString("_id");
                    String time1 = "";
                    String time2 = "";
                    List<String> strlist = new ArrayList<>();
                    Map<String, Object> map = new HashMap<>();
                    List<Map<String, String>> timelist = (List<Map<String, String>>) document.get("timeList");
                    for (int i = 0; i < timelist.size(); i++) {
                        String time = timelist.get(i).get("time");
                        if ("".equals(time1)) {
                            time1 = time;
                        } else {
                            int d2 = Integer.parseInt(time);
                            int d1 = Integer.parseInt(time2);
                            int d = (d2 - d1) - 1;
                            if (d > 0) {//大于1小时
                                if (time1.equals(time2)) {
                                    strlist.add(time1);
                                } else {
                                    String str = time1 + "-" + time2;
                                    strlist.add(str);
                                }
                                time1 = time;
                            }
                        }
                        time2 = time;
                        if (i == (timelist.size() - 1)) {
                            if (time2.equals(time1)) {
                                strlist.add(time2);
                            } else {
                                String str = time1 + "-" + time2;
                                strlist.add(str);
                            }
                        }
                    }
                    map.put("code", pollutantcode);
                    map.put("name", codemap.get(pollutantcode));
                    map.put("timelist", strlist);
                    resultlist.add(map);
                }
            }
            resultmap.put("datalist", resultlist);
            return resultmap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/11/22 0022 上午 10:56
     * @Description: 统计异常报警各异常类型的报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getExceptionAlarmChildDetailDataByParam(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        ProjectionOperation projects = project("DataGatherCode", "ExceptionType").and(queryVO.getPollutantCodeFieldName()).as("PollutantCode");
        aggregations.add(projects);
        aggregations.add(group("DataGatherCode", "ExceptionType", "PollutantCode").count().as("num"));
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(newAggregation(aggregations), queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2020/03/13 9:54
     * @Description: 获取废水无流量异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> getNoFlowExceptionDataNumByParam(OnlineAlarmCountQueryVO queryVO, String timeStyle) {
        Assert.isNull(queryVO.getUnwindFieldName(), "queryVO.getUnwindFieldName must  be null!");
        List<AggregationOperation> aggregations = getNDAndPFLQueryAggregations(queryVO);
        //当为废水监测类型时  只查无流量异常
        //查询无流量异常的异常数据
        aggregations.add(
                Aggregation.match(
                        Criteria.where("ExceptionType").is(NoFlowExceptionEnum.getCode())
                )
        );
        if (timeStyle.equals("hour")) {  //不统计天数据
            Criteria.where("DataType").ne("DayData");
        }
       /* DateOperators.Timezone timezone = DateOperators.Timezone.valueOf("+08");
        DateOperators.DateToString dateToString = DateOperators.dateOf(queryVO.getTimeFieldName()).withTimezone(timezone).toString(timeStyle);
        ProjectionOperation projects = project().and(dateToString).as("MonitorDate");
        aggregations.add(projects);*/
        ProjectionOperation add8h = Aggregation.project(queryVO.getTimeFieldName())
                .andExpression("add(" + queryVO.getTimeFieldName() + ",8 * 3600000)").as("date8");
        ProjectionOperation projects = Aggregation.project("date8").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("MonitorDate");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("MonitorDate").count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, queryVO.getCollection(), Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2020/3/13 0013 下午 15:14
     * @Description: 自定义条件根据mn号统计废水无流量异常报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Document> countNoFlowExceptionDataForMnByParam(Map<String, Object> paramMap) {
        Date startTime = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endTime = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        List<AggregationOperation> aggregations = new ArrayList<>();
        String timeKey = paramMap.get("timeKey").toString();
        List<String> mns = (List<String>) paramMap.get("noflowmns");
        Criteria criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and("ExceptionType").is(NoFlowExceptionEnum.getCode());
        if (paramMap.get("unwindkey") != null) {
            String unwindkey = paramMap.get("unwindkey").toString();
            criteria = Criteria.where("DataGatherCode").in(mns).and(timeKey).gte(startTime).lte(endTime).and(unwindkey + ".IsSuddenChange").is(true);
            UnwindOperation unwind = unwind(unwindkey);
            aggregations.add(unwind);
        }
        aggregations.add(match(criteria));
        Fields fields = fields("DataGatherCode");
        aggregations.add(project(fields));
        GroupOperation groupOperation = group("DataGatherCode").count().as("countnum");
        aggregations.add(groupOperation);
        Aggregation aggregation = newAggregation(aggregations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, paramMap.get("collection").toString(), Document.class);
        List<Document> resultDocument = results.getMappedResults();
        return resultDocument;
    }

    @Override
    public List<Document> countAllAlarmTypeDataNumByDayTime(String daytime, Set<String> dgimns) {
        String starttime = daytime + " 00:00:00";
        String endtime = daytime + " 23:59:59";
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        criteria.and("ExceptionTime").gte(DataFormatUtil.getDateYMDHMS(starttime)).lte(DataFormatUtil.getDateYMDHMS(endtime));
        aggregations.add(match(criteria));
        ProjectionOperation projects = Aggregation.project("DataGatherCode", "ExceptionType");
        aggregations.add(projects);
        aggregations.add(group("DataGatherCode", "ExceptionType"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation5 = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation5, exceptionData, Document.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public List<Document> countAllExceptionTypeDataNumByDayTime(Date date, Date date1,Set<String> dgimns,String timeStyle) {
        List<AggregationOperation> aggregations = new ArrayList<>();
        Criteria criteria = Criteria.where("DataGatherCode").in(dgimns);
        criteria.and("ExceptionTime").gte(date).lte(date1);
        aggregations.add(match(criteria));
        // 加8小时
        ProjectionOperation add8h =  Aggregation.project("ExceptionType","ExceptionTime")
                .andExpression("add("+"ExceptionTime"+",8 * 3600000)").as("date8");
        ProjectionOperation projects =  Aggregation.project("ExceptionType","date8").and(DateOperators.DateToString.dateOf("date8").toString(timeStyle)).as("MonitorDate");
        aggregations.add(add8h);
        aggregations.add(projects);
        aggregations.add(group("ExceptionType","MonitorDate").count().as("num"));
        aggregations.add(sort(Sort.Direction.ASC, "_id"));
        Aggregation aggregation5 = newAggregation(aggregations);
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation5, exceptionData, Document.class);
        return aggregationResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2020/6/02 16:28
     * @Description: 统计某个报警类型各企业点位的报警次数（突变，超标，阈警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map> countChangeAndOverAlarmNumByTimeType(int remind, Set<String> mns, Date date, Date date1) {

        List<Map> listdata = new ArrayList<>();
        if (remind ==ConcentrationChangeEnum.getCode()) {
            //浓度
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("alarmcount")), hourCollection, Map.class).getMappedResults();
        }else if (remind == FlowChangeEnum.getCode()) {
            //排放量
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("alarmcount")), hourFlowCollection, Map.class).getMappedResults();

        }else if (remind == EarlyAlarmEnum.getCode()) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("EarlyWarnTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("alarmcount")), earlyWarnData, Map.class).getMappedResults();

        }else if (remind == OverAlarmEnum.getCode()) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("OverTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("alarmcount")), overData, Map.class).getMappedResults();

        }else if(remind == ExceptionAlarmEnum.getCode()){
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("ExceptionTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("exceptioncount")), exceptionData, Map.class).getMappedResults();
        }


       /* if (remindTypes.contains(ExceptionAlarmEnum.getCode())) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(dgimns).and("ExceptionTime").gte(format.parse(dateFormate.get("starttime"))).lte(format.parse(dateFormate.get("endtime")));
            List<Map> exceptiondata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("exceptioncount")), exceptionData, Map.class).getMappedResults();
            resultList.addAll(exceptiondata);
        }*/

        return listdata;
    }


    /**
     * @author: xsm
     * @date: 2020/6/02 16:28
     * @Description: 统计某个异常类型各企业点位的异常次数（零值异常，恒值异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map> countZeroOrContinuousExceptionNum(String exceptiontype, Set<String> mns, Date date, Date date1) {
            //异常
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").in(mns).and("ExceptionTime").gte(date).lte(date1).and("ExceptionType").is(exceptiontype);
           List<Map> listdata = mongoTemplate.aggregate(newAggregation(match(criteria), project("DataGatherCode"), group("DataGatherCode").count().as("alarmcount")), exceptionData, Map.class).getMappedResults();
        return listdata;
    }

    /**
     * @author: xsm
     * @date: 2020/6/15 13:48
     * @Description: 统计某个点位下各污染物的报警次数及最新报警时间（超标，阈警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public  Map<String,Object> countPointChangeAndOverAlarmNumByTimeType(int remind,String mn, Date date, Date date1,Map<String,Object> codeandname) throws ParseException {
        List<Document> listdata = new ArrayList<>();
        Map<String,Object> resultmap = new HashMap<>();
        if (remind == EarlyAlarmEnum.getCode()) {
            //超阈
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").is(mn).and("EarlyWarnTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode", "PollutantCode","EarlyWarnTime", "count")
                    , group("DataGatherCode",  "PollutantCode").count().as("count").max("EarlyWarnTime").as("lasttime")), earlyWarnData, Document.class).getMappedResults();
        }else if (remind == OverAlarmEnum.getCode()) {
            //超限报警
            Criteria criteria = new Criteria();
            criteria.and("DataGatherCode").is(mn).and("OverTime").gte(date).lte(date1);
            listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), project("DataGatherCode", "PollutantCode","OverTime", "count")
                    , group("DataGatherCode", "PollutantCode").count().as("count").max("OverTime").as("lasttime")), overData, Document.class).getMappedResults();
        }
        if (listdata.size()>0){
            List<Map<String,Object>> datalist = new ArrayList<>();
            int totalnum = 0;
            String lasttime = "";
            for (Document document:listdata){
                Map<String,Object> map = new HashMap<>();
                String thetime = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                map.put("pollutantcode",document.get("PollutantCode"));
                map.put("pollutantname",codeandname.get(document.getString("PollutantCode")));
                if (document.get("count")!=null){
                    totalnum += document.getInteger("count");
                }
                if (!"".equals(lasttime)) {
                        if (DataFormatUtil.compare(lasttime, thetime)) {
                            lasttime = thetime;
                        }
                } else {
                    lasttime = thetime;
                }
                map.put("num",document.get("count"));
                map.put("lasttime",thetime);
                datalist.add(map);
            }
            resultmap.put("typelasttime",lasttime);
            resultmap.put("pollutantdata",datalist);
            resultmap.put("totalnum",totalnum);
        }

        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/6/15 13:48
     * @Description: 统计某个点位下各污染物的报警次数及最新报警时间（零值异常，恒值异常）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countPointExceptionAlarmNumByDayTimeAnd(Date date, Date date1, String mn, String exceptiontype,Map<String,Object> codeandname) throws ParseException {
            Map<String,Object> resultmap = new HashMap<>();
        //异常
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(mn).and("ExceptionTime").gte(date).lte(date1).and("ExceptionType").is(exceptiontype);
        List<Document> listdata  = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("DataGatherCode", "PollutantCode","ExceptionTime", "count")
                , group("DataGatherCode", "PollutantCode").count().as("count").max("ExceptionTime").as("lasttime")), exceptionData, Document.class).getMappedResults();
        if (listdata.size()>0){
            List<Map<String,Object>> datalist = new ArrayList<>();
            int totalnum = 0;
            String lasttime = "";
            for (Document document:listdata){
                Map<String,Object> map = new HashMap<>();
                String thetime = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                map.put("pollutantcode",document.get("PollutantCode"));
                map.put("pollutantname",codeandname.get(document.getString("PollutantCode")));
                if (document.get("count")!=null){
                    totalnum += document.getInteger("count");
                }
                if (!"".equals(lasttime)) {
                    if (DataFormatUtil.compare(lasttime, thetime)) {
                        lasttime = thetime;
                    }
                } else {
                    lasttime = thetime;
                }
                map.put("num",document.get("count"));
                map.put("lasttime",thetime);
                datalist.add(map);
            }
            resultmap.put("typelasttime",lasttime);
            resultmap.put("pollutantdata",datalist);
            resultmap.put("totalnum",totalnum);
        }
        return resultmap;

    }

    /**
     * @author: xsm
     * @date: 2020/6/15 13:48
     * @Description: 统计某个点位下各污染物的报警次数及最新报警时间（浓度突变，排放量突变合并为突变）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countPointChangeAlarmNumByTimeType(String mn, Date date, Date date1,Map<String,Object> codeandname) throws ParseException {
        Map<String,Object> resultmap = new HashMap<>();
            //浓度
            Criteria criteria = new Criteria();
            Criteria criteria1 = new Criteria();
            criteria.and("DataGatherCode").is(mn).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(date).lte(date1);
            criteria1.and("HourDataList.IsSuddenChange").is(true);
        List<Document> listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), unwind("HourDataList"), match(criteria1), project("DataGatherCode", "HourDataList.PollutantCode","MonitorTime", "count")
                    , group("DataGatherCode","PollutantCode").count().as("count").max("MonitorTime").as("lasttime")), hourCollection, Document.class).getMappedResults();
            //排放量
            Criteria criteria2 = new Criteria();
            Criteria criteria3 = new Criteria();
            criteria2.and("DataGatherCode").is(mn).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(date).lte(date1);
            criteria3.and("HourFlowDataList.IsSuddenChange").is(true);
        List<Document> pfl_listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria2), unwind("HourFlowDataList"), match(criteria3), project("DataGatherCode", "HourFlowDataList.PollutantCode","MonitorTime", "count")
                    , group("DataGatherCode", "PollutantCode").count().as("count").max("MonitorTime").as("lasttime")), hourFlowCollection, Document.class).getMappedResults();
        if (pfl_listdata.size()>0){
            for (Document document:pfl_listdata){
                listdata.add(document);
            }
        }
        Set<String> codes = new HashSet<>();
        if (listdata.size()>0){
            List<Map<String,Object>> datalist = new ArrayList<>();
            int totalnum = 0;
            String lasttime = "";
            for (Document document:listdata){
                String pollutantcode = document.getString("PollutantCode");
                if (!codes.contains(pollutantcode)) {
                    int num = 0;
                    String timestr = "";
                    for (Document obj:listdata){
                        String thetime = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                        if (pollutantcode.equals(obj.getString("PollutantCode"))){
                            if (document.get("count")!=null){
                                num += document.getInteger("count");
                                totalnum += document.getInteger("count");
                            }
                            if (!"".equals(timestr)) {
                                if (DataFormatUtil.compare(timestr, thetime)) {
                                    timestr = thetime;
                                }
                            } else {
                                timestr = thetime;
                            }
                            if (!"".equals(lasttime)) {
                                if (DataFormatUtil.compare(lasttime, thetime)) {
                                    lasttime = thetime;
                                }
                            } else {
                                lasttime = thetime;
                            }
                        }
                    }
                    Map<String,Object> map = new HashMap<>();
                    map.put("pollutantcode",document.get("PollutantCode"));
                    map.put("pollutantname",codeandname.get(document.getString("PollutantCode")));
                    map.put("num",num);
                    map.put("lasttime",timestr);
                    datalist.add(map);
                    }
            }
            resultmap.put("typelasttime",lasttime);
            resultmap.put("pollutantdata",datalist);
            resultmap.put("totalnum",totalnum);
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/6/02 16:28
     * @Description: 统计某个报警类型各企业点位的报警次数（突变，超标，阈警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map> getLastMonthPollutionAlarmNum(Integer remind, List<String> mns, Date date, Date date1) {

        List<Map> listdata = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        if (remind ==ConcentrationChangeEnum.getCode()) {
            //浓度
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns).and("MonitorTime").gte(date).lte(date1).and("MinuteDataList.IsSuddenChange").is(true)));
            // 加8小时
            operations.add(Aggregation.project("DataGatherCode").andExpression("add(MonitorTime,8 * 3600000)").as("date8"));
            operations.add(Aggregation.project("DataGatherCode","date8").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
            operations.add(Aggregation.group("DataGatherCode","theDate"));
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultdocument = mongoTemplate.aggregate(aggregationquery, minuteCollection, Map.class);
            listdata = resultdocument.getMappedResults();
        }else if (remind == FlowChangeEnum.getCode()) {
            //排放量
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(date).lte(date1)));
            // 加8小时
            operations.add(Aggregation.project("DataGatherCode").andExpression("add(MonitorTime,8 * 3600000)").as("date8"));
            operations.add(Aggregation.project("DataGatherCode","date8").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
            operations.add(Aggregation.group("DataGatherCode","theDate"));
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultdocument = mongoTemplate.aggregate(aggregationquery, hourFlowCollection, Map.class);
            listdata = resultdocument.getMappedResults();
        }else if (remind == EarlyAlarmEnum.getCode()) {
            //超阈
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns).and("EarlyWarnTime").gte(date).lte(date1)));
            // 加8小时
            operations.add(Aggregation.project("DataGatherCode").andExpression("add(EarlyWarnTime,8 * 3600000)").as("date8"));
            operations.add(Aggregation.project("DataGatherCode","date8").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
            operations.add(Aggregation.group("DataGatherCode","theDate"));
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultdocument = mongoTemplate.aggregate(aggregationquery, earlyWarnData, Map.class);
            listdata = resultdocument.getMappedResults();
        }else if (remind == OverAlarmEnum.getCode()) {
            //超限报警
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns).and("OverTime").gte(date).lte(date1)));
            // 加8小时
            operations.add(Aggregation.project("DataGatherCode").andExpression("add(OverTime,8 * 3600000)").as("date8"));
            operations.add(Aggregation.project("DataGatherCode","date8").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
            operations.add(Aggregation.group("DataGatherCode","theDate"));
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultdocument = mongoTemplate.aggregate(aggregationquery, overData, Map.class);
            listdata = resultdocument.getMappedResults();
        }else if (remind == ExceptionAlarmEnum.getCode()) {
            //异常
            operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns).and("ExceptionTime").gte(date).lte(date1)));
            // 加8小时
            operations.add(Aggregation.project("DataGatherCode").andExpression("add(ExceptionTime,8 * 3600000)").as("date8"));
            operations.add(Aggregation.project("DataGatherCode","date8").andExpression("substr(date8,0,10)").as("theDate").andExclude("_id"));
            operations.add(Aggregation.group("DataGatherCode","theDate"));
            Aggregation aggregationquery = Aggregation.newAggregation(operations);
            AggregationResults<Map> resultdocument = mongoTemplate.aggregate(aggregationquery, exceptionData, Map.class);
            listdata = resultdocument.getMappedResults();
        }
        return listdata;
    }

    /**
     * @author: xsm
     * @date: 2020/10/10 13:13
     * @Description: 统计某企业下某类型点位报警类型的报警数据（突变，超标，阈警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map> getLastMonthEntMonitorPointAlarmDataByParam(Integer remind, List<String> mns, Date startDate, Date endDate,Integer monitorpointtype) {
        List<Map> listdata = new ArrayList<>();
        String timefield = "";
        String collection = "";
        List<Document> listItems = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Map<String, Object> timeAndRead = new HashMap<>();
        if (remind == EarlyAlarmEnum.getCode()) {
                timefield = "EarlyWarnTime";
                collection = "EarlyWarnData";
                timeAndRead.put("time", "$" + timefield);
                timeAndRead.put("isread", "$isread");
                criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", "OverMultiple", "maxvalue", "minvalue", "firsttime", "lasttime", timefield, "PollutantCode", "ExceptionType")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime")
                                .max("OverMultiple").as("maxvalue")
                                .min("OverMultiple").as("minvalue")
                                .min(timefield).as("firsttime")
                                .max(timefield).as("lasttime")
                                .push(timeAndRead).as("timeList")
                                .push("PollutantCode").as("pollutantCodes")
                );
            } else if (remind == OverAlarmEnum.getCode()) {
                //超限报警
                timefield = "OverTime";
                collection = "OverData";
            timeAndRead.put("time", "$" + timefield);
            timeAndRead.put("isread", "$isread");
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", "OverMultiple", "maxvalue", "minvalue", "firsttime", "lasttime", timefield, "PollutantCode", "ExceptionType")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime")
                                .max("OverMultiple").as("maxvalue")
                                .min("OverMultiple").as("minvalue")
                                .min(timefield).as("firsttime")
                                .max(timefield).as("lasttime")
                                .push(timeAndRead).as("timeList")
                                .push("PollutantCode").as("pollutantCodes")
                );
            } else if (remind == ExceptionAlarmEnum.getCode()) {
                //异常
                timefield = "ExceptionTime";
                collection = "ExceptionData";
            timeAndRead.put("time", "$" + timefield);
            timeAndRead.put("isread", "$isread");
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate);
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode", "OverMultiple", "maxvalue", "minvalue", "firsttime", "lasttime", timefield, "PollutantCode", "ExceptionType")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> exceptiontypeandread = new HashMap<>();
                exceptiontypeandread.put("pollutantcode", "$PollutantCode");
                exceptiontypeandread.put("exceptiontype", "$ExceptionType");
                operations.add(
                        Aggregation.group("DataGatherCode", "MonitorTime")
                                .max("OverMultiple").as("maxvalue")
                                .min("OverMultiple").as("minvalue")
                                .min(timefield).as("firsttime")
                                .max(timefield).as("lasttime")
                                .push(timeAndRead).as("timeList")
                                .push(exceptiontypeandread).as("ExceptionTypeList")
                                .push("PollutantCode").as("pollutantCodes")
                );
            }
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "DataGatherCode"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();
            List<String> pollutantCodes;
            List<String> exceptiontypes;
            List<String> dateList;
        List<Document> timeList;
            String continuityvalue;
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("remindcode", remind);
                if (remind == EarlyAlarmEnum.getCode()) {
                    dataMap.put("remindname", EarlyAlarmEnum.getName());
                } else if (remind == OverAlarmEnum.getCode()) {
                    dataMap.put("remindname", OverAlarmEnum.getName());
                } else if (remind == ExceptionAlarmEnum.getCode()) {
                    dataMap.put("remindname", ExceptionAlarmEnum.getName());
                }
                dataMap.put("datagathercode", document.get("DataGatherCode"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                dataMap.put("maxvalue", document.get("maxvalue"));
                dataMap.put("minvalue", document.get("minvalue"));
                //dataMap.put("firsttime", DataFormatUtil.getDateYMDHMS(document.getDate("firsttime")));
                //dataMap.put("lasttime", DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")));
                dataMap.put("exceptiontype", document.get("ExceptionType"));
                timeList = (List<Document>) document.get("timeList");
                pollutantCodes = ((List<String>) document.get("pollutantCodes")).stream().distinct().collect(Collectors.toList());
                if (remind == ExceptionAlarmEnum.getCode()) {
                    exceptiontypes = ((List<String>) document.get("ExceptionTypeList")).stream().distinct().collect(Collectors.toList());
                    dataMap.put("exceptiontypes", exceptiontypes);
                }
                dataMap.put("pollutantCodes", pollutantCodes);
                dateList = new ArrayList<>();
                for (Document time : timeList) {
                    String ymdhms = DataFormatUtil.getDateYMDHMS(time.getDate("time"));
                    if (!dateList.contains(ymdhms)) {
                        dateList.add(ymdhms);

                    }
                }
                int interval;
                //获取配置的各类型排口的间隔时间
                if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {//废气
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("gasoutput.minute"));
                } else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode() || monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.RainEnum.getCode()) {//废水、雨水
                    if (monitorpointtype == ExceptionAlarmEnum.getCode()) {
                        interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutput.minute"));
                    } else {
                        interval = Integer.parseInt(DataFormatUtil.parseProperties("wateroutputover.minute"));
                    }
                } else if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalDustEnum.getCode()) {//扬尘
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("dustmonitorpoint.minute"));
                } else {//其它类型监测点
                    interval = Integer.parseInt(DataFormatUtil.parseProperties("othermonitorpoint.minute"));
                }
                continuityvalue = DataFormatUtil.mergeContinueDate(dateList, interval, "yyyy-MM-dd HH:mm", "、", "HH:mm");
                dataMap.put("continuityvalue", continuityvalue);
                listdata.add(dataMap);
            }
        return listdata;
    }

    @Override
    public List<Map> getAlarmPollutantInfoByParamForApp(Integer remind, String dgimn, Date startDate, Date endDate, Integer monitorpointtype) {
        List<Map> result = new ArrayList<>();
        String timefield = "";
        String collection = "";
        List<Document> listItems = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria2 = new Criteria();
        if (remind == FlowChangeEnum.getCode()) {  //排放量
            timefield = "MonitorTime";
            collection = "HourFlowData";
            criteria.and("DataGatherCode").is(dgimn).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            criteria2.and("HourFlowDataList.IsSuddenChange").is(true);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.unwind("HourFlowDataList"));
            operations.add(Aggregation.match(criteria2));
            operations.add(Aggregation.project("DataGatherCode").and("HourFlowDataList.PollutantCode").as("PollutantCode") );
            operations.add(
                    Aggregation.group("DataGatherCode","PollutantCode")
            );
        } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
            timefield = "MonitorTime";
            collection = "HourData";
            criteria.and("DataGatherCode").is(dgimn).and("HourDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            criteria2.and("HourDataList.IsSuddenChange").is(true);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.unwind("HourDataList"));
            operations.add(Aggregation.match(criteria2));
            operations.add(Aggregation.project("DataGatherCode").and("HourDataList.PollutantCode").as("PollutantCode") );
            operations.add(
                    Aggregation.group("DataGatherCode","PollutantCode")
            );
        }else if (remind == EarlyAlarmEnum.getCode()) {
            timefield = "EarlyWarnTime";
            collection = "EarlyWarnData";
            criteria.and("DataGatherCode").is(dgimn).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode",  "PollutantCode" ) );
            operations.add(
                    Aggregation.group("DataGatherCode","PollutantCode")
            );
        } else if (remind == OverAlarmEnum.getCode()) {
            //超限报警
            timefield = "OverTime";
            collection = "OverData";
            criteria.and("DataGatherCode").is(dgimn).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", "PollutantCode")
            );
            operations.add(
                    Aggregation.group( "DataGatherCode","PollutantCode")
            );
        } else if (remind == ExceptionAlarmEnum.getCode()) {
            //异常
            timefield = "ExceptionTime";
            collection = "ExceptionData";
            criteria.and("DataGatherCode").is(dgimn).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode",  "PollutantCode")
            );
            operations.add(
                    Aggregation.group("DataGatherCode", "PollutantCode")
            );
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();
        if (listItems != null && listItems.size() > 0) {
                Map<String, Object> param = new HashMap<>();
                param.put("pollutanttype", monitorpointtype);
                List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(param);
                Map<String, Object> codeandname = new HashMap<>();
                Map<String, Object> codeandunit = new HashMap<>();
                for (Map<String, Object> map:pollutants){
                    if (map.get("code")!=null){
                        codeandname.put(map.get("code").toString(),map.get("name"));
                        codeandunit.put(map.get("code").toString(),map.get("unit"));
                    }
                }
             for (Document document : listItems) {
                 Map<String, Object> obj = new HashMap<>();
                 obj.put("pollutantcode",document.getString("PollutantCode"));
                 obj.put("pollutantname",codeandname.get(document.getString("PollutantCode")));
                 obj.put("pollutantunit",codeandunit.get(document.getString("PollutantCode")));
                 if (document.getString("PollutantCode")!=null) {
                     result.add(obj);
                 }
             }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2021/05/20 09:00
     * @Description: 统计单个站点某日各报警类型的报警时段和总报警时段（超标、异常、超阈值、浓度突变）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getOnePointAlarmMonitorDataByParam(Map<String, Object> paramMap, Map<String, Object> codeAndName,Map<String, Object> codeAndunit, List<Map<String, Object>> standValueslist) throws ParseException {
        List<Map<String, Object>> datalist = new ArrayList<>();
        //浓度突变
        Map<String,Object> ndtb_map = getChangeAlarmDataByParamMap(paramMap,codeAndName,codeAndunit);
        //超阈值
        Map<String,Object> cyz_map = getEarlyAndOverAlarmModelData(EarlyAlarmEnum.getCode(),paramMap,codeAndName,codeAndunit,standValueslist);
        //超标
        Map<String,Object> cb_map = getEarlyAndOverAlarmModelData(OverAlarmEnum.getCode(),paramMap,codeAndName,codeAndunit,standValueslist);
        //异常
        Map<String,Object> yc_map = getExceptionAlarmModelData(paramMap,codeAndName,codeAndunit);
        datalist.add(cb_map);
        datalist.add(cyz_map);
        datalist.add(ndtb_map);
        datalist.add(yc_map);
        return datalist;
    }


    private Map<String,Object> getChangeAlarmDataByParamMap(Map<String, Object> paramMap, Map<String, Object> codeAndName,Map<String, Object> codeAndunit) {
        Map<String,Object> resultmap = new HashMap<>();
        resultmap.put("remindcode",CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
        resultmap.put("remindname",CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getName());
        List<Map<String,Object>> result = new ArrayList<>();
        //分钟浓度突变
        String mn = paramMap.get("mn")!=null?paramMap.get("mn").toString():"";
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = "ChangeTime";
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        List<Document> listItems = new ArrayList<>();
        if (!"".equals(mn)) {
            criteria.and("DataGatherCode").is(mn).and(timefield).gte(startDate).lte(endDate).and("DataType").is("MinuteData");
            Map<String, Object> timelist = new HashMap<>();
            timelist.put("ChangeTime", "$" + timefield);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode", timefield, "PollutantCode"));
            operations.add(Aggregation.sort(Sort.Direction.ASC, timefield));
            operations.add(Aggregation.group("DataGatherCode", "PollutantCode")
                    .push(timelist).as("timelist"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, changeData, Document.class);
            listItems = pageResults.getMappedResults();
        }
        if (listItems!=null&&listItems.size()>0){
            String pollutantcode;
            List<Document> onelist;
            List<String> onetimes;
            for (Document document:listItems){
                pollutantcode = document.getString("PollutantCode");
                onelist = (List<Document>) document.get("timelist");
                Map<String,Object> onemap = new HashMap<>();
                onetimes = new ArrayList<>();
                for (Document onedoc:onelist){
                    onetimes.add( DataFormatUtil.getDateHM(onedoc.getDate("ChangeTime")));
                }
                onemap.put("pollutantcode",pollutantcode);
                onemap.put("pollutantname",codeAndName.get(pollutantcode));
                onemap.put("pollutantunit",codeAndunit.get(pollutantcode));
                onemap.put("changetimes",onetimes);
                result.add(onemap);
            }
        }
        resultmap.put("alarmdata",result);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2021/05/20 0020 上午 10:38
     * @Description: 自定义查询条件获取当天预警、超标报警数据(app)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String,Object> getEarlyAndOverAlarmModelData(Integer remindCode, Map<String, Object> paramMap, Map<String, Object> codeAndName,Map<String, Object> codeAndunit, List<Map<String, Object>> standValueslist) throws ParseException {
        Map<String,Object> result = new HashMap<>();
        result.put("remindcode",remindCode);
        result.put("remindname",CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindCode).getName());
        String dgimn =  paramMap.get("mn").toString();
        String starttime = paramMap.get("starttime").toString();
        String end = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(end);
        String timefield = "";
        String collection = "";
        List<Document> listItems = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remindCode == EarlyAlarmEnum.getCode()) {
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","PollutantCode", "MinMonitorValue","MaxMonitorValue", "FirstOverTime","LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN",timefield));
        } else if (remindCode == OverAlarmEnum.getCode()) {
            //超限报警
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","PollutantCode","MinMonitorValue","MaxMonitorValue","FirstOverTime","LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN",timefield));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();

        if (listItems != null && listItems.size() > 0) {
            //计算报警类型总的报警时间
            String firsttime = DataFormatUtil.getDateYMDHMS(listItems.get(0).getDate("FirstOverTime"));
            String lasttime = DataFormatUtil.getDateYMDHMS(listItems.get(0).getDate("LastOverTime"));
            //List<String> overtime  = new ArrayList<>();
            long overtime = 0;
            List<Map<String,Object>> polist = new ArrayList<>();
            overtime = countAlarmTimenum(firsttime,lasttime,listItems,"FirstOverTime","LastOverTime");
            if (overtime>0) {
                long minutenum = overtime/60;
                result.put("alarmtotaltime",DataFormatUtil.countHourMinuteTime(new Long(minutenum).intValue()) );
            }
            //通过污染物分组数据
            Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
            for(String code:mapDocuments.keySet()){
                Map<String,Object> objmap = new HashMap<>();
                List<Document> documents = mapDocuments.get(code);
                String firstHM = "";
                String lastHM = "";
                String over_time = "";
                List<String> onepolist = new ArrayList<>();
                double totaltime = 0d;
                String minvalue = "";
                String maxvalue = "";
                Object overvalue = "";
                for (Document document:documents){
                    //报警时间
                    firstHM = DataFormatUtil.getDateHM(document.getDate("FirstOverTime"));
                    lastHM = DataFormatUtil.getDateHM(document.getDate("LastOverTime"));
                    over_time = document.getString("OverTime");
                    totaltime = totaltime+Double.valueOf(over_time);
                    if (firstHM.equals(lastHM)){
                        onepolist.add(firstHM);
                    }else{
                        onepolist.add(firstHM+"-"+ lastHM+"【"+DataFormatUtil.countHourMinuteTime(Double.valueOf(over_time).intValue())+"】");
                    }
                    if ("".equals(minvalue)){
                        minvalue = document.getString("MinMonitorValue");
                    }else{
                        //比较数字大小 进行更新
                        double onevalue = Double.parseDouble(document.getString("MinMonitorValue"));
                        if (Double.parseDouble(minvalue)>onevalue){
                            minvalue = document.getString("MinMonitorValue");
                        }
                    }
                    if ("".equals(maxvalue)){
                        maxvalue = document.getString("MaxMonitorValue");
                    }else{
                        //比较数字大小 进行更新
                        double onevalue = Double.parseDouble(document.getString("MaxMonitorValue"));
                        if (Double.parseDouble(maxvalue)<onevalue){
                            maxvalue = document.getString("MaxMonitorValue");
                        }
                    }
                }
                //获取标准值
                if (standValueslist!=null&&standValueslist.size()>0){
                 for (Map<String,Object> obj:standValueslist){
                     if (remindCode == EarlyAlarmEnum.getCode()) {
                         if (code.equals(obj.get("FK_PollutantCode").toString())
                                 &&obj.get("FK_AlarmLevelCode")!=null&&"0".equals(obj.get("FK_AlarmLevelCode").toString())){
                             overvalue = obj.get("ConcenAlarmMaxValue");
                         }
                     } else if (remindCode == OverAlarmEnum.getCode()) {
                         if (code.equals(obj.get("FK_PollutantCode").toString())){
                             overvalue = obj.get("StandardMaxValue");
                         }
                     }

                 }
                }
                objmap.put("pollutantcode",code);
                objmap.put("pollutantname",codeAndName.get(code));
                objmap.put("pollutantunit",codeAndunit.get(code));
                objmap.put("standvalue",overvalue);
                objmap.put("minvalue",minvalue);
                objmap.put("maxvalue",maxvalue);
                objmap.put("alarmtime",DataFormatUtil.countHourMinuteTime(new Double(totaltime).intValue()));
                objmap.put("overtimes",onepolist);
                polist.add(objmap);
            }
            result.put("alarmdata",polist);
        }else{//当overmodel 表中无数据 则获取超标 预警表小时数据
            operations = new ArrayList<>();
            criteria = new Criteria();
            if (remindCode == EarlyAlarmEnum.getCode()) {
                timefield = "EarlyWarnTime";
                collection = "EarlyWarnData";
                criteria.and("DataGatherCode").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("DataType").is("HourData");
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode","PollutantCode", "MonitorValue",timefield)
                );
                operations.add(Aggregation.sort(Sort.Direction.ASC,  "DataGatherCode",timefield));
            } else if (remindCode == OverAlarmEnum.getCode()) {
                //超限报警
                timefield = "OverTime";
                collection = "OverData";
                criteria.and("DataGatherCode").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("DataType").is("HourData");
                operations.add(Aggregation.match(criteria));
                operations.add(Aggregation.project("DataGatherCode","PollutantCode","MonitorValue",timefield)
                );
                operations.add(Aggregation.sort(Sort.Direction.ASC,  "DataGatherCode",timefield));
            }
            Aggregation aggregationList2 = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults2 = mongoTemplate.aggregate(aggregationList2, collection, Document.class);
            listItems = pageResults2.getMappedResults();
            int hournum = 0;
            List<Map<String,Object>> polist = new ArrayList<>();
            if (listItems!=null&&listItems.size()>0){
                //通过污染物分组数据
                Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                for(String code:mapDocuments.keySet()) {
                    Map<String, Object> objmap = new HashMap<>();
                    List<Document> documents = mapDocuments.get(code);
                    String houralarmstr = "";//小时超标时段
                    String newhour ="";
                    int newhournum = 0;
                    String str1 = "";
                    Object overvalue = "";
                    String minvalue = "";
                    String maxvalue = "";
                    if (documents!=null&&documents.size()>0){
                        if (hournum==0){
                            hournum =documents.size();
                        }else{
                            if (hournum<documents.size()){
                                hournum =documents.size();
                            }
                        }
                        for (Document document:documents){
                            int hour = DataFormatUtil.getDateHourNum(document.getDate(timefield));
                            str1 = DataFormatUtil.getDateHM(document.getDate(timefield));
                            if ("".equals(newhour)) {
                                newhour = hour + "";
                                newhournum += 1;
                                houralarmstr = str1 + "、";
                            } else {
                                if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                                    //连续
                                    newhour = hour + "";
                                    newhournum += 1;
                                } else {
                                    houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                                    if (newhournum>1){
                                        if (Integer.parseInt(newhour)>9){
                                            houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】、"+ str1+"、";
                                        }else {
                                            houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                        }
                                    }else{
                                        houralarmstr = houralarmstr + "、"+str1+"、";
                                    }
                                    newhour = hour + "";
                                    newhournum = 1;
                                }
                            }
                            if ("".equals(minvalue)){
                                minvalue = document.getString("MonitorValue");
                            }else{
                                //比较数字大小 进行更新
                                double onevalue = Double.parseDouble(document.getString("MonitorValue"));
                                if (Double.parseDouble(minvalue)>onevalue){
                                    minvalue = document.getString("MonitorValue");
                                }
                            }
                            if ("".equals(maxvalue)){
                                maxvalue = document.getString("MonitorValue");
                            }else{
                                //比较数字大小 进行更新
                                double onevalue = Double.parseDouble(document.getString("MonitorValue"));
                                if (Double.parseDouble(maxvalue)<onevalue){
                                    maxvalue = document.getString("MonitorValue");
                                }
                            }

                        }
                    }
                    houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                    if (newhournum>1){
                        if (Integer.parseInt(newhour)>9){
                            houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】";
                        }else {
                            houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】";
                        }
                    }
                    if (!"".equals(houralarmstr)){
                        String[] strs = houralarmstr.split("、");
                        objmap.put("overtimes",strs);
                    }
                    //获取标准值
                    if (standValueslist!=null&&standValueslist.size()>0){
                        for (Map<String,Object> obj:standValueslist){
                            if (remindCode == EarlyAlarmEnum.getCode()) {
                                if (code.equals(obj.get("FK_PollutantCode").toString())
                                        &&obj.get("FK_AlarmLevelCode")!=null&&"0".equals(obj.get("FK_AlarmLevelCode").toString())){
                                    overvalue = obj.get("ConcenAlarmMaxValue");
                                }
                            } else if (remindCode == OverAlarmEnum.getCode()) {
                                if (code.equals(obj.get("FK_PollutantCode").toString())){
                                    overvalue = obj.get("StandardMaxValue");
                                }
                            }

                        }
                    }
                    objmap.put("pollutantcode",code);
                    objmap.put("pollutantname",codeAndName.get(code));
                    objmap.put("pollutantunit",codeAndunit.get(code));
                    objmap.put("standvalue",overvalue);
                    objmap.put("minvalue",minvalue);
                    objmap.put("maxvalue",maxvalue);
                    objmap.put("alarmtime",documents!=null?documents.size()+"小时":"-");
                    polist.add(objmap);

                }
            }
            result.put("alarmtotaltime",hournum!=0?hournum+"小时":"-");
            result.put("alarmdata",polist);
        }
        return result;
    }

    private Map<String,Object> getExceptionAlarmModelData(Map<String, Object> paramMap, Map<String, Object> codeAndName,Map<String, Object> codeAndunit) throws ParseException {
        Map<String,Object> result = new HashMap<>();
        result.put("remindcode",ExceptionAlarmEnum.getCode());
        result.put("remindname",CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getName());
        String dgimn = paramMap.get("mn").toString();
        String starttime = paramMap.get("starttime").toString();
        String end = paramMap.get("endtime").toString();
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(end);
        List<Document> listItems = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("MN").is(dgimn).and("FirstExceptionTime").gte(startDate).lte(endDate);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("MN", "PollutantCode","FirstExceptionTime", "LastExceptionTime","ExceptionType","ExceptionTime"));
        operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN","FirstExceptionTime"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "ExceptionModel", Document.class);
        listItems = pageResults.getMappedResults();
        if (listItems != null && listItems.size() > 0) {
            //计算报警类型总的报警时间
            String firsttime = DataFormatUtil.getDateYMDHMS(listItems.get(0).getDate("FirstExceptionTime"));
            String lasttime = DataFormatUtil.getDateYMDHMS(listItems.get(0).getDate("LastExceptionTime"));
            long overtime = countAlarmTimenum(firsttime,lasttime,listItems,"FirstExceptionTime","LastExceptionTime");
            if (overtime>0) {
                long minutenum = overtime/60;
                result.put("alarmtotaltime",DataFormatUtil.countHourMinuteTime(new Long(minutenum).intValue()) );
            }
            List<Map<String,Object>> polist = new ArrayList<>();
            //通过污染物分组数据
            Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
            for(String code:mapDocuments.keySet()){
                Map<String,Object> objmap = new HashMap<>();
                List<Document> ex_documents = mapDocuments.get(code);
                double totaltime = 0d;
                List<Map<String,Object>> exceptionlist = new ArrayList<>();
                String po_firsttime = DataFormatUtil.getDateYMDHMS(ex_documents.get(0).getDate("FirstExceptionTime"));
                String po_lasttime = DataFormatUtil.getDateYMDHMS(ex_documents.get(0).getDate("LastExceptionTime"));
                long poovertime = countAlarmTimenum(po_firsttime,po_lasttime,ex_documents,"FirstExceptionTime","LastExceptionTime");
                //通过异常类型分组数据
                Map<String, List<Document>> exceptions = ex_documents.stream().collect(Collectors.groupingBy(m -> m.get("ExceptionType").toString()));
                for(String ex_type:exceptions.keySet()){
                    Map<String,Object> ex_map = new HashMap<>();
                    List<Document> documents = exceptions.get(ex_type);
                    ex_map.put("exceptiontypecode",ex_type);
                    ex_map.put("exceptiontypename",CommonTypeEnum.ExceptionTypeEnum.getNameByCode(ex_type));
                    List<String> onepolist = new ArrayList<>();
                    String firstHM = "";
                    String lastHM = "";
                    String over_time = "";
                    for (Document document:documents){
                        //报警时间
                        firstHM = DataFormatUtil.getDateHM(document.getDate("FirstExceptionTime"));
                        lastHM = DataFormatUtil.getDateHM(document.getDate("LastExceptionTime"));
                        over_time = document.getString("ExceptionTime");
                        totaltime = totaltime+Double.valueOf(over_time);
                        if (firstHM.equals(lastHM)){
                            onepolist.add(firstHM);
                        }else{
                            onepolist.add(firstHM+"-"+ lastHM+"【"+DataFormatUtil.countHourMinuteTime(Double.valueOf(over_time).intValue())+"】");
                        }
                    }
                    ex_map.put("exceptiontimes",onepolist);
                    exceptionlist.add(ex_map);
                }
                objmap.put("pollutantcode",code);
                objmap.put("pollutantname",codeAndName.get(code));
                objmap.put("pollutantunit",codeAndunit.get(code));
                if (poovertime>0) {
                    long po_minutenum = poovertime/60;
                    objmap.put("alarmtime",DataFormatUtil.countHourMinuteTime(new Long(po_minutenum).intValue()) );
                }else{
                    objmap.put("alarmtime",null);
                }
                objmap.put("exceptiondata",exceptionlist);
                polist.add(objmap);
            }
            result.put("alarmdata",polist);
        }else{
            //当overmodel 表中无数据 则获取超标 预警表小时数据
            operations = new ArrayList<>();
            criteria = new Criteria();
            //超限报警
            criteria.and("DataGatherCode").is(dgimn).and("ExceptionTime").gte(startDate).lte(endDate).and("DataType").is("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("DataGatherCode","PollutantCode","MonitorValue","ExceptionTime","ExceptionType")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "DataGatherCode","ExceptionTime"));

            Aggregation aggregationList2 = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults2 = mongoTemplate.aggregate(aggregationList2, "ExceptionData", Document.class);
            listItems = pageResults2.getMappedResults();
            List<Map<String,Object>> polist = new ArrayList<>();
            Set<String> alltimelist = new HashSet<>();
            if (listItems!=null&&listItems.size()>0){
                //通过污染物分组数据
                Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                for(String code:mapDocuments.keySet()) {
                    Map<String, Object> objmap = new HashMap<>();
                    List<Document> po_documents = mapDocuments.get(code);
                    List<Map<String,Object>> exceptionlist = new ArrayList<>();
                    //通过异常类型分组数据
                    Map<String, List<Document>> type_Documents = po_documents.stream().collect(Collectors.groupingBy(m -> m.get("ExceptionType").toString()));
                    Set<String> timelist = new HashSet<>();
                    for(String type:type_Documents.keySet()) {
                        Map<String,Object> ex_map = new HashMap<>();
                        ex_map.put("exceptiontypecode",type);
                        ex_map.put("exceptiontypename",CommonTypeEnum.ExceptionTypeEnum.getNameByCode(type));
                        List<Document> documents = type_Documents.get(type);
                        String houralarmstr = "";//小时异常时段
                        String newhour ="";
                        int newhournum = 0;
                        String str1 = "";
                        if (documents!=null&&documents.size()>0){
                            for (Document document:documents){
                                timelist.add(DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
                                alltimelist.add(DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")));
                                int hour = DataFormatUtil.getDateHourNum(document.getDate("ExceptionTime"));
                                str1 = DataFormatUtil.getDateHM(document.getDate("ExceptionTime"));
                                if ("".equals(newhour)) {
                                    newhour = hour + "";
                                    newhournum += 1;
                                    houralarmstr = str1 + "、";
                                } else {
                                    if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                                        //连续
                                        newhour = hour + "";
                                        newhournum += 1;
                                    } else {
                                        houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                                        if (newhournum>1){
                                            if (Integer.parseInt(newhour)>9){
                                                houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】、"+ str1+"、";
                                            }else {
                                                houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                            }
                                        }else{
                                            houralarmstr = houralarmstr + "、"+str1+"、";
                                        }
                                        newhour = hour + "";
                                        newhournum = 1;
                                    }
                                }
                            }
                        }
                        houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                        if (newhournum>1){
                            if (Integer.parseInt(newhour)>9){
                                houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】";
                            }else {
                                houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】";
                            }
                        }
                        ex_map.put("exceptiontimes",new ArrayList<>());
                        if (!"".equals(houralarmstr)){
                            String[] strs = houralarmstr.split("、");
                            ex_map.put("exceptiontimes",strs);
                        }
                        exceptionlist.add(ex_map);
                    }
                    objmap.put("pollutantcode",code);
                    objmap.put("pollutantname",codeAndName.get(code));
                    objmap.put("pollutantunit",codeAndunit.get(code));
                    objmap.put("alarmtime",timelist!=null?timelist.size()+"小时":"-");
                    objmap.put("exceptiondata",exceptionlist);
                    polist.add(objmap);
                }
            }
            result.put("alarmtotaltime",alltimelist!=null?alltimelist.size()+"小时":"-");
            result.put("alarmdata",polist);
        }
        return result;
    }

    private Long countAlarmTimenum(String firsttime,String lasttime,List<Document> listItems,String key1 ,String key2) throws ParseException {
        long overtime = 0;
        for (Document document:listItems){
            String onefirsttime = DataFormatUtil.getDateYMDHMS(document.getDate(key1));
            String onelasttime = DataFormatUtil.getDateYMDHMS(document.getDate(key2));
            //比较开始时间 若开始时间在初始时间段范围内  则比较结束时间若结束时间大于初始时间 则更新初始结束时间
            //若开始时间不在 初始时间段范围内 则保存前一个初始时间段 且用当前数据的时间更新初始时间段
            boolean firstflag = false;
            boolean lastflag = false;
            if (!onefirsttime.equals(firsttime)){//比较开始时间
                //比较结束时间
                if (!onefirsttime.equals(lasttime)){
                    //判断 开始时间和结束时间是否在 初始时段 的范围内
                    boolean flag1 = DataFormatUtil.compare(firsttime, onefirsttime);
                    boolean flag2 = DataFormatUtil.compare(onefirsttime, lasttime);
                    if (flag1==true&&flag2==true){
                        firstflag = true;
                    }
                }else{
                    firstflag = true;
                }
            }else{
                firstflag =true;
            }
            //比较结束时间
            if (!onelasttime.equals(lasttime)){
                //判断 开始时间和结束时间是否在 初始时段 的范围内
                boolean flag1 = DataFormatUtil.compare(firsttime, onelasttime);
                boolean flag2 = DataFormatUtil.compare(onelasttime, lasttime);
                if (flag1==true&&flag2==true){
                    lastflag = true;
                }
            }else{
                lastflag = true;
            }
            if (firstflag ==false ){
                //开始时间 不包含于初始时间段内 保存当前初始时间段的时间差值 且更新初始时间段的 开始、结束时间
                overtime = overtime+(DataFormatUtil.getDateLong(lasttime) - DataFormatUtil.getDateLong(firsttime));
                firsttime = onefirsttime;
                lasttime = onelasttime;
            }else{
                if (lastflag==false){
                    //开始时间 包含于初始时间段内 结束时间不包含  更新初始时间段的 结束时间
                    lasttime = onelasttime;
                }
            }
        }
        overtime = overtime+(DataFormatUtil.getDateLong(lasttime) - DataFormatUtil.getDateLong(firsttime));
        return overtime;
    }

    @Override
    public List<Document> getOneStinkPointOverDataByParam(Map<String, Object> param) {
        String monitortime = param.get("monitortime").toString();
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + " 00:00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + " 23:59:59");
        String dgimn = param.get("dgimn").toString();
        String pollutantcode = param.get("pollutantcode").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        //超限报警
        criteria.and("MN").is(dgimn).and("FirstOverTime").gte(startDate).lte(endDate).and("PollutantCode").is(pollutantcode).and("AlarmLevel").ne(0);
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("MN", "PollutantCode", "LastMonitorValue", "FirstOverTime", "LastOverTime", "OverTime", "AlarmLevel","DataType")
        );
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MN", "FirstOverTime"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "OverModel", Document.class);
        return pageResults.getMappedResults();
    }

    /**
     * @author: xsm
     * @date: 2021/09/06 0006 下午 3:00
     * @Description: 统计某个企业近五年某类型下单个污染物年排放
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> countEntFlowDataGroupByYearByParam(Map<String, Object> param) {
    Map<String, Object> resultmap = new HashMap<>();
        int startyear = Integer.valueOf(param.get("startyear").toString());
        int  endyear = Integer.valueOf(param.get("endyear").toString());
        String pollutantcode = param.get("pollutantcode").toString();
        List<String> mns = (List<String>) param.get("mns");
        final String mongdb_moth_pfl = "YearFlowData";
        //开始时间从前一年 1月开始
        Date starttime = DataFormatUtil.parseDate(startyear + "-01-01 00:00:00");
        //当前月结束
        Date endtime = DataFormatUtil.parseDate(endyear + "-12-31 23:59:59");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(starttime).lte(endtime);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind("YearFlowDataList"));
        operations.add(match(Criteria.where("MonthFlowDataList.PollutantCode").is(pollutantcode)));
        operations.add(Aggregation.project("DataGatherCode","MonitorTime").and("YearFlowDataList.PollutantFlow").as("PollutantFlow"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, mongdb_moth_pfl, Document.class);
        List<Document> documents = pageResults.getMappedResults();
        List<String> ym_list = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<String> lastvalues = new ArrayList<>();
        if (documents.size()>0){
            for (int i =startyear;i<=endyear;i++){
                String value = "";
                boolean ishavevalue = false;
                for (Document document : documents) {
                    //当前年
                    if (String.valueOf(startyear).equals( DataFormatUtil.getDateY(document.getDate("MonitorTime")))){
                        if (document.get("PollutantFlow")!=null){
                            value = document.getString("PollutantFlow");
                            if (!"".equals(value)) {
                                ishavevalue = true;
                            }
                        }
                    }
                }
                if (ishavevalue) {//以当前年 是否有月排放量数据 为主 进行存储
                    values.add(value);
                    ym_list.add(i + "年");
                }
            }
        }
        resultmap.put("timelist",ym_list);
        resultmap.put("newdata",values);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2021/12/01 0001 下午 1:41
     * @Description: 统计单个点位某时段各个小时报警时长
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPointAlarmTimesDataGroupByHourTime(Map<String, Object> paramMap) {
        try {
            String dgimn = paramMap.get("dgimn").toString();
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            Integer remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            Set<String> alarmtype = new HashSet<>();
            paramMap.put("isuser","1");
            List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codeandname = new HashMap<>();
            for (Map<String, Object> map:pollutants){
                if (map.get("code")!=null) {
                    codeandname.put(map.get("code").toString(), map.get("name"));
                }
            }
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
            if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
            } else if (remindtype == OverAlarmEnum.getCode()){
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            }else if(remindtype == EarlyAlarmEnum.getCode()){
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0).and("DataType").ne("HourData");
            }
            operations.add(Aggregation.match(criteria));

            if (remindtype == ExceptionAlarmEnum.getCode()) {    //若查异常数据 则需返回异常类型（零值异常、连续值异常、超限异常、无流量异常等）
                operations.add(Aggregation.project("MN", "LastExceptionTime", timefield,  "ExceptionType","PollutantCode")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("starttime", "$"+timefield);
                childmap.put("endtime", "$LastExceptionTime");
                childmap.put("exceptiontype", "$ExceptionType");
                childmap.put("pollutantcode", "$PollutantCode");
                operations.add(
                        Aggregation.group("MN", "MonitorTime")
                                .push(childmap).as("timelist")
                );
            } else { //超标 超阈值
                operations.add(Aggregation.project("MN", lasttimestr, timefield, "AlarmLevel","PollutantCode")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("starttime", "$"+timefield);
                childmap.put("endtime", "$"+lasttimestr);
                childmap.put("level", "$AlarmLevel");
                childmap.put("pollutantcode", "$PollutantCode");
                operations.add(
                        Aggregation.group("MN", "MonitorTime")
                                .push(childmap).as("timelist")
                );
            }
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
            Map<String, Object> codeAndLevel = new HashMap<>();
            for (Map<String, Object> map : alarmLevelList) {
                codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
            }
            Map<String,Set<String>> mntimeandcode = new HashMap<>();
            for (Document document : listItems) {
                //dataMap.put("datagathercode", document.get("MN"));
                //dataMap.put("monitortime", document.getString("MonitorTime"));
                //List<Map<String, Object>> polist = new ArrayList<>();
                List<Document> timelist = (List<Document>) document.get("timelist");
                List<Map<String, Object>> onelist = new ArrayList<>();
                if (timelist!=null&&timelist.size()>0){
                    String onestarttime = null;
                    String oneenttime = null;
                    for (Document onedocument:timelist){
                        onestarttime = DataFormatUtil.getDateHM(onedocument.getDate("starttime"));
                        String hourone = onestarttime.substring(0,onestarttime.length()-3);
                        String minuteone = onestarttime.substring(3,onestarttime.length());
                        oneenttime = DataFormatUtil.getDateHM(onedocument.getDate("endtime"));
                        String minutetwo = oneenttime.substring(3,oneenttime.length());
                        String hourtwo = oneenttime.substring(0,oneenttime.length()-3);
                        if (hourone.equals(hourtwo)){
                            Map<String, Object> map = new HashMap<>();
                            map.put("min",minuteone);
                            map.put("max",minutetwo);
                            map.put("hournum",Integer.valueOf(hourone));

                            if (remindtype == ExceptionAlarmEnum.getCode()) {
                                map.put("exceptiontype", onedocument.get("exceptiontype"));
                                alarmtype.add(onedocument.get("exceptiontype")+"");
                            }else{
                                map.put("level", onedocument.get("level"));
                                alarmtype.add(onedocument.get("level")+"");
                            }
                            map.put("code",onedocument.getString("pollutantcode"));
                            onelist.add(map);
                        }else{
                            for (int i = Integer.valueOf(hourone);i<=(Integer.valueOf(hourtwo));i++){
                                Map<String, Object> map = new HashMap<>();
                                if (i==Integer.valueOf(hourone)){
                                    map.put("min",minuteone);
                                }else{
                                    map.put("min","0");
                                }
                                if (i!=(Integer.valueOf(hourtwo))) {
                                    map.put("max", "60");
                                }else{
                                    map.put("max", minutetwo);
                                }
                                if (remindtype == ExceptionAlarmEnum.getCode()) {
                                    map.put("exceptiontype", onedocument.get("exceptiontype"));
                                    alarmtype.add(onedocument.get("exceptiontype")+"");
                                }else{
                                    map.put("level", onedocument.get("level"));
                                    alarmtype.add(onedocument.get("level")+"");
                                }
                                map.put("hournum",i);
                                map.put("code",onedocument.getString("pollutantcode"));
                                onelist.add(map);
                            }
                        }
                    }
                }
                if (onelist!=null&&onelist.size()>0){
                    Map<String, List<Map<String, Object>>> mapDocuments = new HashMap<>();
                   /* if (remindtype == ExceptionAlarmEnum.getCode()){
                        mapDocuments = onelist.stream().collect(Collectors.groupingBy(m -> m.get("exceptiontype").toString()));
                    }else{
                        mapDocuments = onelist.stream().collect(Collectors.groupingBy(m -> m.get("level").toString()));
                    }*/
                    mapDocuments = onelist.stream().collect(Collectors.groupingBy(m -> m.get("hournum").toString()));
                    for (int i =0;i<24;i++){
                        if (mapDocuments.get(i+"")!=null){
                            List<Map<String, Object>> hour_list = new ArrayList<>();
                            List<Map<String, Object>> twolist = mapDocuments.get(i+"");
                            for (String type:alarmtype) {
                                String min = "";
                                String max = "";
                                int totalminute = 0;
                                twolist = twolist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("min").toString()))).collect(Collectors.toList());
                                Set<String> codes = new HashSet<>();
                                for (Map<String, Object> onemap : twolist) {
                                    String the_type = "";
                                    if (remindtype == ExceptionAlarmEnum.getCode()) {
                                        the_type = onemap.get("exceptiontype")+"";
                                    }else{
                                        the_type = onemap.get("level")+"";
                                    }
                                    if (type.equals(the_type)) {
                                        codes.add(onemap.get("code").toString());
                                        if ("".equals(min) &&"".equals(max)){
                                            min = onemap.get("min").toString();
                                            max = onemap.get("max").toString();
                                        }else{
                                            //比较时间段
                                            //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                            if (Integer.valueOf(onemap.get("min").toString())>= Integer.valueOf(min)&&Integer.valueOf(onemap.get("min").toString())<= Integer.valueOf(max)){
                                                if (Integer.valueOf(onemap.get("max").toString())>Integer.valueOf(max)) {
                                                    max = onemap.get("max").toString();
                                                }
                                            }else{
                                                //第二次报警时段不被包含于第一个报警时段中
                                                if (min.equals(max)){
                                                    totalminute+=1;
                                                }else{
                                                    totalminute+=Integer.valueOf(max) - Integer.valueOf(min);
                                                }
                                                min = onemap.get("min").toString();
                                                max = onemap.get("max").toString();
                                            }
                                        }
                                    }
                                }

                                if (!"".equals(min)&&!"".equals(max)){
                                    Map<String, Object> resultmap = new HashMap<>();
                                    resultmap.put("alarmtype",type);
                                    if (remindtype == ExceptionAlarmEnum.getCode()) {
                                        resultmap.put("alarmtypename",CommonTypeEnum.ExceptionTypeEnum.getNameByCode(type));
                                    }else{
                                        if ("-1".equals(type)){
                                            resultmap.put("alarmtypename","超标报警");
                                        }else{
                                            resultmap.put("alarmtypename",codeAndLevel.get(type));
                                        }
                                    }
                                    if (min.equals(max)){
                                        resultmap.put("alarmminute",totalminute+1);
                                    }else{
                                        resultmap.put("alarmminute",totalminute+(Integer.valueOf(max) - Integer.valueOf(min)));
                                    }
                                    //报警污染物
                                    if (codes.size()>0){
                                        List<String> namess = new ArrayList<>();
                                        for (String onecode:codes){
                                            if (codeandname.get(onecode)!=null) {
                                                namess.add(codeandname.get(onecode).toString());
                                            }
                                        }
                                        resultmap.put("pollutantnames",namess);

                                    }else{
                                        resultmap.put("pollutantnames",new ArrayList<>());
                                    }
                                    hour_list.add(resultmap);
                                }
                            }
                            Map<String, Object> dataMap = new HashMap<>();
                            if (i<10){
                                dataMap.put("monitortime",document.getString("MonitorTime")+" 0"+i);
                            }else{
                                dataMap.put("monitortime",document.getString("MonitorTime")+" "+i);
                            }
                            dataMap.put("alarmdata",hour_list);
                            dataList.add(dataMap);
                        }
                    }
                }
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getPointAlarmTimesDataGroupByDayTime(Map<String, Object> paramMap) {
        try {
            String dgimn = paramMap.get("dgimn").toString();
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            Date startDate = DataFormatUtil.parseDate(starttime);
            Date endDate = DataFormatUtil.parseDate(endtime);
            Integer remindtype = Integer.valueOf(paramMap.get("remindtype").toString());
            String timefield = "";
            String collection = "";
            String lasttimestr = "";
            Set<String> alarmtype = new HashSet<>();
            paramMap.put("isuser","1");
            List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codeandname = new HashMap<>();
            for (Map<String, Object> map:pollutants){
                if (map.get("code")!=null) {
                    codeandname.put(map.get("code").toString(), map.get("name"));
                }
            }
            if (remindtype == EarlyAlarmEnum.getCode() || remindtype == OverAlarmEnum.getCode()) {  //阈值
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
            if (remindtype == ExceptionAlarmEnum.getCode()) {//为异常类型时 根据所传异常类型查询
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("DataType").ne("HourData");
            } else if (remindtype == OverAlarmEnum.getCode()) {
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            } else if (remindtype == EarlyAlarmEnum.getCode()) {
                criteria.and("MN").is(dgimn).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0).and("DataType").ne("HourData");
            }
            operations.add(Aggregation.match(criteria));

            if (remindtype == ExceptionAlarmEnum.getCode()) {    //若查异常数据 则需返回异常类型（零值异常、连续值异常、超限异常、无流量异常等）
                operations.add(Aggregation.project("MN", "LastExceptionTime", timefield, "ExceptionType","PollutantCode")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("starttime", "$" + timefield);
                childmap.put("endtime", "$LastExceptionTime");
                childmap.put("exceptiontype", "$ExceptionType");
                childmap.put("pollutantcode", "$PollutantCode");
                operations.add(
                        Aggregation.group("MN", "MonitorTime")
                                .push(childmap).as("timelist")
                );
            } else { //超标 超阈值
                operations.add(Aggregation.project("MN", lasttimestr, timefield, "AlarmLevel","PollutantCode")
                        .and(DateOperators.DateToString.dateOf(timefield).toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                Map<String, Object> childmap = new HashMap<>();
                childmap.put("starttime", "$" + timefield);
                childmap.put("endtime", "$" + lasttimestr);
                childmap.put("level", "$AlarmLevel");
                childmap.put("pollutantcode", "$PollutantCode");
                operations.add(
                        Aggregation.group("MN", "MonitorTime")
                                .push(childmap).as("timelist")
                );
            }
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> alarmLevelList = alarmLevelMapper.getAlarmLevelPubCodeInfo();
            Map<String, Object> codeAndLevel = new HashMap<>();
            for (Map<String, Object> map : alarmLevelList) {
                codeAndLevel.put(map.get("Code").toString(), map.get("Name"));
            }
            for (Document document : listItems) {
                Map<String, Object> dataMap = new HashMap<>();
                //dataMap.put("datagathercode", document.get("MN"));
                dataMap.put("monitortime", document.getString("MonitorTime"));
                List<Map<String, Object>> polist = new ArrayList<>();
                List<Document> timelist = (List<Document>) document.get("timelist");
                if (timelist != null && timelist.size() > 0) {
                    Map<String, List<Document>> mapDocuments = new HashMap<>();
                    if (remindtype == ExceptionAlarmEnum.getCode()) {
                        mapDocuments = timelist.stream().collect(Collectors.groupingBy(m -> m.get("exceptiontype").toString()));
                    } else {
                        mapDocuments = timelist.stream().collect(Collectors.groupingBy(m -> m.get("level").toString()));
                    }
                    for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                        Set<String> overcodes = new HashSet<>();
                        Map<String, Object> onemap = new HashMap<>();
                        List<Document> onelist = entry.getValue();
                        if (remindtype == ExceptionAlarmEnum.getCode()) {
                            onemap.put("alarmtype", entry.getKey());
                            onemap.put("alarmtypename", CommonTypeEnum.ExceptionTypeEnum.getNameByCode(entry.getKey()));
                        } else {
                            onemap.put("alarmtype", entry.getKey());
                            if ("-1".equals(entry.getKey())) {
                                onemap.put("alarmtypename", "超标报警");
                            } else {
                                onemap.put("alarmtypename", codeAndLevel.get(entry.getKey()));
                            }
                        }
                        String continuityvalue = "";
                        Date firsttime = null;
                        Date lasttime = null;
                        int totalminute = 0;
                        for (Document podo : onelist) {
                            overcodes.add(podo.getString("pollutantcode"));
                            //比较时间 获取报警时段
                            if (podo.get("starttime") != null && podo.get("endtime") != null) {
                                if (firsttime == null && lasttime == null) {
                                    firsttime = podo.getDate("starttime");
                                    lasttime = podo.getDate("endtime");
                                } else {
                                    //比较时间段
                                    //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                    if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(lasttime)) ||
                                            podo.getDate("starttime").before(lasttime)) {
                                        //若被包含 比较两个结束时间
                                        if (lasttime.before(podo.getDate("endtime"))) {
                                            //若第一次报警的结束时间 小于 第二个报警时段的结束时间
                                            //则进行赋值
                                            lasttime = podo.getDate("endtime");
                                        }
                                    } else {
                                        //第二次报警时段不被包含于第一个报警时段中
                                        if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
                                            continuityvalue = continuityvalue + DataFormatUtil.getDateHM(firsttime) + "、";
                                            totalminute += 1;
                                        } else {
                                            continuityvalue = continuityvalue + DataFormatUtil.getDateHM(firsttime) + "-" + DataFormatUtil.getDateHM(lasttime) + "、";
                                            long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
                                            totalminute += Integer.valueOf(timenum + "");
                                        }
                                        //将重新赋值开始 结束时间
                                        firsttime = podo.getDate("starttime");
                                        lasttime = podo.getDate("endtime");
                                    }
                                }
                            }
                        }
                        //将最后一次超标时段 或一直连续报警的超标时段 拼接
                        if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
                            totalminute += 1;
                        } else {
                            long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
                            totalminute += Integer.valueOf(timenum + "");
                        }
                        onemap.put("alarmminute", totalminute);

                        if (overcodes!=null&&overcodes.size()>0){
                            List<String> namess = new ArrayList<>();
                            for (String onecode:overcodes){
                                if (codeandname.get(onecode)!=null) {
                                    namess.add(codeandname.get(onecode).toString());
                                }
                            }
                            onemap.put("pollutantnames",namess);
                        }else{
                            onemap.put("pollutantnames",new ArrayList<>());
                        }
                        polist.add(onemap);
                    }
                }
                dataMap.put("alarmdata", polist);
                dataList.add(dataMap);
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/01 0001 下午 1:41
     * @Description: 统计多个点位某时段各个小时报警总时长
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countOverAlarmPointNumDataByParam(Map<String, Object> paramMap) {
        try {
            List<Map<String,Object>> result = new ArrayList<>();
            //根据监测类型获取该类型下所有点位信息
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            String datetype = paramMap.get("datetype")!=null?paramMap.get("datetype").toString():"";
            paramMap.put("isuser","1");
            List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
            Map<String, Object> codeandname = new HashMap<>();
            for (Map<String, Object> map:pollutants){
                if (map.get("code")!=null) {
                    codeandname.put(map.get("code").toString(), map.get("name"));
                }
            }
            Date startDate = null;
            Date endDate = null;
            List<String> timelist = new ArrayList<>();
            // 实时、分钟、小时 都按小时分组  日按日分组
            String  coolection = "";
            String liststr = "";
            String timestr = "";
            if("realtime".equals(datetype)){
                startDate = DataFormatUtil.parseDate(starttime);
                endDate = DataFormatUtil.parseDate(endtime);
                timelist = DataFormatUtil.getYMDHBetween(starttime.substring(0,13), endtime.substring(0,13));
                timelist.add(endtime.substring(0,13));
                coolection = realtimeCollection;
                liststr = "RealDataList";
                timestr = "%Y-%m-%d %H";
            }else if("minute".equals(datetype)){
                startDate = DataFormatUtil.parseDate(starttime+":00");
                endDate = DataFormatUtil.parseDate(endtime+":59");
                timelist = DataFormatUtil.getYMDHBetween(starttime.substring(0,13), endtime.substring(0,13));
                timelist.add(endtime.substring(0,13));
                coolection = minuteCollection;
                liststr = "MinuteDataList";
                timestr = "%Y-%m-%d %H";
            }else if("hour".equals(datetype)){
                 startDate = DataFormatUtil.parseDate(starttime+":00:00");
                 endDate = DataFormatUtil.parseDate(endtime+":59:59");
                timelist = DataFormatUtil.getYMDHBetween(starttime, endtime);
                timelist.add(endtime);
                coolection = hourCollection;
                liststr = "HourDataList";
                timestr = "%Y-%m-%d %H";
            }else if("day".equals(datetype)){
                startDate = DataFormatUtil.parseDate(starttime+" 00:00:00");
                endDate = DataFormatUtil.parseDate(endtime+" 23:59:59");
                timelist = DataFormatUtil.getYMDBetween(starttime, endtime);
                timelist.add(endtime);
                coolection = dayCollection;
                liststr = "DayDataList";
                timestr = "%Y-%m-%d";
            }
            List<Document> listdata = new ArrayList<>();
            Criteria criteria = new Criteria();
            Criteria criteria2 = new Criteria();
            Criteria timecriteria = new Criteria();
            List<Criteria> criterialist = new ArrayList<>();
            criterialist.add(Criteria.where(liststr+".IsOverStandard").is(true));
            criterialist.add(Criteria.where(liststr+".IsOver").gt(0));
            timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
            criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
            //criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate).and("MinuteDataList.IsSuddenChange").is(true);
            criteria2.andOperator(timecriteria);
            listdata = mongoTemplate.aggregate(newAggregation(
                        match(criteria), unwind(liststr), match(criteria2), project("DataGatherCode").andExpression("add(MonitorTime,8 * 3600000)").as("date8").and(liststr+".PollutantCode").as("PollutantCode"),
                        project( "DataGatherCode","PollutantCode").and(DateOperators.DateToString.dateOf("date8").toString(timestr)).as("thetime")
                        , group("DataGatherCode","thetime","PollutantCode")), coolection, Document.class).getMappedResults();

            Map<String, List<Document>> mapDocuments = new HashMap<>();
            if (listdata!=null&&listdata.size()>0){
                //按时间分组
                mapDocuments = listdata.stream().filter(m -> m.get("thetime") != null).collect(Collectors.groupingBy(m -> m.get("thetime").toString()));
            }
            List<Document> documents;
            Map<String, List<Document>> polist;
            for (String hour:timelist){
                Set<String> dgimns = new HashSet<>();
                List<Map<String,Object>> codelist = new ArrayList<>();
                Map<String,Object> hour_valuemap = new HashMap();
                hour_valuemap.put("monitortime",hour);
                hour_valuemap.put("value",0);
                 if (mapDocuments!=null&&mapDocuments.get(hour)!=null){
                     documents = mapDocuments.get(hour);
                     if (documents!=null&&documents.size()>0){
                         //按污染物分组
                         polist = documents.stream().filter(m -> m.get("PollutantCode") != null).collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                         for (Map.Entry<String, List<Document>> entry : polist.entrySet()) {
                             Map<String,Object> codemap = new HashMap();
                             codemap.put("code",entry.getKey());
                             codemap.put("name",codeandname.get(entry.getKey()));
                             codemap.put("num",entry.getValue().size());
                             codelist.add(codemap);
                         }
                          dgimns = documents.stream().filter(m -> m.get("DataGatherCode") != null).map(output -> output.get("DataGatherCode").toString()).collect(Collectors.toSet());
                     }
                     hour_valuemap.put("value",dgimns.size());
                 }
                hour_valuemap.put("codedata",codelist);
                result.add(hour_valuemap);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @Override
    public List<Map<String, Object>> getAllPointOverAlarmDataByParamForHour(Map<String, Object> paramMap) {
        //根据监测类型获取该类型下所有点位信息
        List<Map<String,Object>> result = new ArrayList<>();
        List<String> mns = (List<String>) paramMap.get("dgimns");
        Map<String, Object> mnandshortername = (Map<String, Object>) paramMap.get("mnandshortername");
        Map<String, Object> mnandmonitorpointname = (Map<String, Object>) paramMap.get("mnandmonitorpointname");
        Map<String, Object> mnandmonitorpointid = (Map<String, Object>) paramMap.get("mnandmonitorpointid");
        Map<String, Object> mnandtype = (Map<String, Object>) paramMap.get("mnandtype");
        String daytime = paramMap.get("daytime").toString();
        Integer hournum = Integer.valueOf(paramMap.get("hournum").toString());
        Map<String,Object> codeandname = new HashMap<>();
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        for (Map<String, Object> map:pollutants){
            codeandname.put(map.get("PollutantType")+"_"+map.get("code"),map.get("name"));
        }
        Date startDate = DataFormatUtil.parseDate(daytime+" 00:00:00");
        Date endDate = DataFormatUtil.parseDate(daytime+" 23:59:59");
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
        operations.add(Aggregation.match(criteria));
        operations.add(Aggregation.project("MN", "LastOverTime", "FirstOverTime","PollutantCode"));
        Map<String, Object> childmap = new HashMap<>();
        childmap.put("starttime", "$FirstOverTime");
        childmap.put("endtime", "$LastOverTime");
        operations.add(
                Aggregation.group("MN", "PollutantCode")
                        .push(childmap).as("timelist")
        );
        operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, overModelCollection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        Map<String, List<Document>> mapDocuments = new HashMap<>();
        if (listItems!=null&&listItems.size()>0){
            //按MN分组
            mapDocuments = listItems.stream().filter(m -> m.get("MN") != null).collect(Collectors.groupingBy(m -> m.get("MN").toString()));
        }
        List<Document> documents;
        List<Document> timedocument;
        for (String mn :mns){
            int mn_total = 0;
            List<Map<String,Object>> po_listmap = new ArrayList<>();
            if (mapDocuments!=null&&mapDocuments.get(mn)!=null) {
                documents = mapDocuments.get(mn);
                for (Document doc : documents) {
                    //单个点 单个污染物
                    Map<String,Object> po_map = new HashMap<>();
                    po_map.put("pollutantcode",doc.getString("PollutantCode"));
                    po_map.put("pollutantname",codeandname.get(mnandtype.get(doc.getString("MN"))+"_"+doc.getString("PollutantCode")));
                    List<Map<String, Object>> onelist = new ArrayList<>();
                    timedocument = (List<Document>) doc.get("timelist");
                    String onestarttime = null;
                    String oneenttime = null;
                    int total = 0;
                    String over_times = "";
                    //将报警数据 按小时分隔 保存小时数相同的数据
                    for (Document onedocument : timedocument) {
                        //比较报警开始时间和报警结束时间
                        onestarttime = DataFormatUtil.getDateHM(onedocument.getDate("starttime"));
                        String hourone = onestarttime.substring(0, onestarttime.length() - 3);
                        String minuteone = onestarttime.substring(3, onestarttime.length());
                        oneenttime = DataFormatUtil.getDateHM(onedocument.getDate("endtime"));
                        String minutetwo = oneenttime.substring(3, oneenttime.length());
                        String hourtwo = oneenttime.substring(0, oneenttime.length() - 3);
                        if (hourone.equals(hourtwo)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("min", minuteone);
                            map.put("max", minutetwo);
                            map.put("hournum", Integer.valueOf(hourone));
                            if (hournum == Integer.valueOf(hourone)) {
                                onelist.add(map);
                            }
                        } else {
                            for (int i = Integer.valueOf(hourone); i <= (Integer.valueOf(hourtwo)); i++) {
                                Map<String, Object> map = new HashMap<>();
                                if (i == Integer.valueOf(hourone)) {
                                    map.put("min", minuteone);
                                } else {
                                    map.put("min", "0");
                                }
                                if (i != (Integer.valueOf(hourtwo))) {
                                    map.put("max", "60");
                                } else {
                                    map.put("max", minutetwo);
                                }
                                map.put("hournum", i);
                                if (hournum == i) {
                                    onelist.add(map);
                                }
                            }
                        }
                    }
                    //遍历 且以小时数作为key 保存点位小时超标时长
                    if (onelist != null && onelist.size() > 0) {
                        String min = "";
                        String max = "";
                        int totalminute = 0;
                        onelist = onelist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("min").toString()))).collect(Collectors.toList());
                        for (Map<String, Object> twomap : onelist) {
                            if ("".equals(min) && "".equals(max)) {
                                min = twomap.get("min").toString();
                                max = twomap.get("max").toString();
                            } else {
                                //比较时间段
                                //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                if (Integer.valueOf(twomap.get("min").toString()) >= Integer.valueOf(min) && Integer.valueOf(twomap.get("min").toString()) <= Integer.valueOf(max)) {
                                    if (Integer.valueOf(twomap.get("max").toString()) > Integer.valueOf(max)) {
                                        max = twomap.get("max").toString();
                                    }
                                } else {
                                    //第二次报警时段不被包含于第一个报警时段中
                                    if (min.equals(max)) {
                                        over_times = over_times+min+"、";
                                        totalminute += 1;
                                    } else {
                                        totalminute += Integer.valueOf(max) - Integer.valueOf(min);
                                        over_times = over_times + min+"-"+max + "、";
                                    }
                                    min = twomap.get("min").toString();
                                    max = twomap.get("max").toString();
                                }
                            }
                        }
                        if (!"".equals(min) && !"".equals(max)) {
                            if (min.equals(max)) {
                                total = totalminute + 1;
                                over_times = over_times+min+"、";
                            } else {
                                total = totalminute + (Integer.valueOf(max) - Integer.valueOf(min));
                                over_times = over_times + min+"-"+max + "、";
                            }
                        }
                        if(!"".equals(over_times)){
                            over_times = over_times.substring(0,over_times.length()-1);
                        }
                    }
                    po_map.put("over_times",over_times);
                    po_listmap.add(po_map);
                    if (mn_total == 0) {
                        mn_total = total;
                    } else {
                        if (mn_total < total) {
                            mn_total = total;
                        }
                    }
                }
            }
            if (mn_total>0) {
                Map<String, Object> mn_map = new HashMap<>();
                mn_map.put("shortername",mnandshortername.get(mn));
                mn_map.put("monitorpointname",mnandmonitorpointname.get(mn));
                mn_map.put("monitorpointid",mnandmonitorpointid.get(mn));
                mn_map.put("total",mn_total+"分钟");
                mn_map.put("pollutantdata",po_listmap);
                result.add(mn_map);
            }
            }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/01/17 0017 下午 2:57
     * @Description: 通过多参数获取多个点位某时间段的报警总时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:yyyy-mm-dd endtime:yyyy-mm-dd  monitorpointtype:监测点类型
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countAllPointDayOverAlarmTimesDataByParam(Map<String, Object> paramMap) {
        try {

            Map<String, Object> codeandename = new HashMap<>();
            //获取污染物信息
            List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(null);
            if (allpollutants != null) {
                for (Map<String, Object> pomap : allpollutants) {
                    if (pomap.get("code") != null) {
                        codeandename.put(pomap.get("code").toString(), pomap.get("name"));
                    }
                }
            }
            //根据监测类型获取该类型下所有点位信息
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            //获取时间段内所有日数据
            List<String> days = DataFormatUtil.getYMDBetween(starttime, endtime);
            days.add(endtime);
            Date startDate = DataFormatUtil.parseDate(starttime+" 00:00:00");
            Date endDate = DataFormatUtil.parseDate(endtime+" 23:59:59");
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "LastOverTime", "FirstOverTime","PollutantCode")
                    .and(DateOperators.DateToString.dateOf("FirstOverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$FirstOverTime");
            childmap.put("endtime", "$LastOverTime");
            operations.add(
                    Aggregation.group("MN", "MonitorTime","PollutantCode")
                            .push(childmap).as("timelist")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, overModelCollection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            List<Map<String,Object>> result = new ArrayList<>();
            Map<String, List<Document>> mapDocuments = new HashMap<>();
            if (listItems!=null&&listItems.size()>0){
                //按日分组
                mapDocuments = listItems.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            }
            List<Document> documents;
            List<Document> pollutants;
            for (String daystr:days){
                if (mapDocuments!=null&&mapDocuments.get(daystr)!=null){
                    documents = mapDocuments.get(daystr);
                    int alarmtimenum = 0;
                    HashSet<String> set = new HashSet<>();
                    for (Document doc:documents){
                        set.add(codeandename.get(doc.get("PollutantCode")).toString());
                        //单个点
                        Date firsttime = null;
                        Date lasttime = null;
                        pollutants = (List<Document>) doc.get("timelist");
                        pollutants = pollutants.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("starttime"))).collect(Collectors.toList());
                        for (Document podo : pollutants) {
                            //比较时间 获取报警时段
                            if (podo.get("starttime") != null && podo.get("endtime") != null) {
                                if (firsttime == null && lasttime == null) {
                                    firsttime = podo.getDate("starttime");
                                    lasttime = podo.getDate("endtime");
                                } else {
                                    //比较时间段
                                    //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                    if (DataFormatUtil.getDateYMDHMS(podo.getDate("starttime")).equals(DataFormatUtil.getDateYMDHMS(lasttime)) ||
                                            podo.getDate("starttime").before(lasttime)) {
                                        //若被包含 比较两个结束时间
                                        if (lasttime.before(podo.getDate("endtime"))) {
                                            //若第一次报警的结束时间 小于 第二个报警时段的结束时间
                                            //则进行赋值
                                            lasttime = podo.getDate("endtime");
                                        }
                                    } else {
                                        //第二次报警时段不被包含于第一个报警时段中
                                        if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))) {
                                            alarmtimenum += 1;
                                        } else {
                                            long timenum = (lasttime.getTime() - firsttime.getTime()) / (1000 * 60);
                                            alarmtimenum += Integer.valueOf(timenum + "");
                                        }
                                        //将重新赋值开始 结束时间
                                        firsttime = podo.getDate("starttime");
                                        lasttime = podo.getDate("endtime");
                                    }
                                }
                            }

                        }
                        //将最后一次超标时段 或一直连续报警的超标时段 拼接
                        if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))){
                            alarmtimenum +=1;
                        }else{
                            long timenum = (lasttime.getTime()-firsttime.getTime())/(1000 * 60);
                            alarmtimenum +=Integer.valueOf(timenum+"");
                        }
                    }
                    Map<String,Object> objmap = new HashMap<>();
                    objmap.put("monitortime",daystr);
                    objmap.put("value",alarmtimenum);
                    objmap.put("pollutantname",set.toString().replace("[","").replace("]",""));
                    result.add(objmap);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/12/01 0001 下午 1:41
     * @Description: 统计多个点位某时段各个小时报警总时长
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countAllPointOverAlarmTimesDataByParam(Map<String, Object> paramMap) {
        try {


            Map<String, Object> codeandename = new HashMap<>();
            //获取污染物信息
            List<Map<String, Object>> allpollutants = pollutantFactorMapper.getPollutantsByPollutantType(null);
            if (allpollutants != null) {
                for (Map<String, Object> pomap : allpollutants) {
                    if (pomap.get("code") != null) {
                        codeandename.put(pomap.get("code").toString(), pomap.get("name"));
                    }
                }
            }
            //根据监测类型获取该类型下所有点位信息
            List<String> mns = (List<String>) paramMap.get("dgimns");
            String starttime = paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime").toString();
            //获取时间段内所有日数据
            List<String> days = DataFormatUtil.getYMDBetween(starttime, endtime);
            days.add(endtime);
            Date startDate = DataFormatUtil.parseDate(starttime+" 00:00:00");
            Date endDate = DataFormatUtil.parseDate(endtime+" 23:59:59");
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "LastOverTime", "FirstOverTime","PollutantCode")
                    .and(DateOperators.DateToString.dateOf("FirstOverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$FirstOverTime");
            childmap.put("endtime", "$LastOverTime");
            operations.add(
                    Aggregation.group("MN", "MonitorTime","PollutantCode")
                            .push(childmap).as("timelist")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, overModelCollection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            List<Map<String,Object>> result = new ArrayList<>();
            Map<String, List<Document>> mapDocuments = new HashMap<>();
            if (listItems!=null&&listItems.size()>0){
                //按日分组
                mapDocuments = listItems.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            }
            List<Document> documents;
            List<Document> timedocument;
            for (String daystr:days){
                if (mapDocuments!=null&&mapDocuments.get(daystr)!=null){
                    documents = mapDocuments.get(daystr);
                    Map<String,Object> hour_valuemap = new HashMap();
                    for (Document doc:documents){
                        //单个点
                        List<Map<String, Object>> onelist = new ArrayList<>();
                        timedocument = (List<Document>) doc.get("timelist");
                        String onestarttime = null;
                        String oneenttime = null;
                        int total = 0;
                        //将报警数据 按小时分隔
                        for (Document onedocument:timedocument){
                            //比较报警开始时间和报警结束时间
                            onestarttime = DataFormatUtil.getDateHM(onedocument.getDate("starttime"));
                            String hourone = onestarttime.substring(0,onestarttime.length()-3);
                            String minuteone = onestarttime.substring(3,onestarttime.length());
                            oneenttime = DataFormatUtil.getDateHM(onedocument.getDate("endtime"));
                            String minutetwo = oneenttime.substring(3,oneenttime.length());
                            String hourtwo = oneenttime.substring(0,oneenttime.length()-3);
                            if (hourone.equals(hourtwo)){
                                Map<String, Object> map = new HashMap<>();
                                map.put("min",minuteone);
                                map.put("max",minutetwo);
                                map.put("hournum",Integer.valueOf(hourone));
                                onelist.add(map);
                            }else{
                                for (int i = Integer.valueOf(hourone);i<=(Integer.valueOf(hourtwo));i++){
                                    Map<String, Object> map = new HashMap<>();
                                    if (i==Integer.valueOf(hourone)){
                                        map.put("min",minuteone);
                                    }else{
                                        map.put("min","0");
                                    }
                                    if (i!=(Integer.valueOf(hourtwo))) {
                                        map.put("max", "60");
                                    }else{
                                        map.put("max", minutetwo);
                                    }
                                    map.put("hournum",i);
                                    onelist.add(map);
                                }
                            }
                        }
                        //遍历 获取该点24小时内 每个小时的报警时长 且以小时数作为key 保存点位小时超标时长
                        if (onelist!=null&&onelist.size()>0){
                            Map<String, List<Map<String, Object>>> onemap = new HashMap<>();
                            onemap = onelist.stream().collect(Collectors.groupingBy(m -> m.get("hournum").toString()));
                            for (int i =0;i<24;i++){
                                if (onemap.get(i+"")!=null){
                                    List<Map<String, Object>> twolist = onemap.get(i+"");
                                    String min = "";
                                    String max = "";
                                    int totalminute = 0;
                                    twolist = twolist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("min").toString()))).collect(Collectors.toList());
                                    for (Map<String, Object> twomap : twolist) {
                                        if ("".equals(min) &&"".equals(max)){
                                            min = twomap.get("min").toString();
                                            max = twomap.get("max").toString();
                                        }else{
                                            //比较时间段
                                            //判断第二个报警时段的开始报警时间是否包含于第一个报警时段中
                                            if (Integer.valueOf(twomap.get("min").toString())>= Integer.valueOf(min)&&Integer.valueOf(twomap.get("min").toString())<= Integer.valueOf(max)){
                                                if (Integer.valueOf(twomap.get("max").toString())>Integer.valueOf(max)) {
                                                    max = twomap.get("max").toString();
                                                }
                                            }else{
                                                //第二次报警时段不被包含于第一个报警时段中
                                                if (min.equals(max)){
                                                    totalminute+=1;
                                                }else{
                                                    totalminute+=Integer.valueOf(max) - Integer.valueOf(min);
                                                }
                                                min = twomap.get("min").toString();
                                                max = twomap.get("max").toString();
                                            }
                                        }
                                    }
                                    if (!"".equals(min)&&!"".equals(max)){
                                        if (min.equals(max)){
                                            total = totalminute+1;
                                        }else{
                                            total = totalminute+(Integer.valueOf(max) - Integer.valueOf(min));
                                        }
                                    }
                                    if (total>0) {
                                        HashMap<String, Object> value = new HashMap<>();
                                        value.put("pollutantcode",doc.get("PollutantCode"));
                                        if (hour_valuemap.get(i + "") != null) {
                                            HashMap<String,Object> map = (HashMap) hour_valuemap.get(i + "");
                                            value.put("value",(Integer.parseInt(map.get("value")+"") + total));
                                            hour_valuemap.put(i + "", value);
                                        } else {
                                            value.put("value",total);
                                            hour_valuemap.put(i + "", value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(hour_valuemap.size()>0){
                        for (int i =0;i<24;i++){
                            if (hour_valuemap.get(i+"")!=null){
                                HashMap<String,Object> value = (HashMap) hour_valuemap.get(i + "");
                                Map<String,Object> objmap = new HashMap<>();
                                objmap.put("monitortime",daystr+" "+(i>9?i:"0"+i));
                                objmap.put("value",value.get("value"));
                                objmap.put("pollutantcode",value.get("pollutantcode"));
                                objmap.put("pollutantname",codeandename.get(value.get("pollutantcode")));
                                result.add(objmap);
                            }
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Document> countPointAlarmNumDataByParam(String collection, int remind, String starttime, String endtime, Set<String> mns) {
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remind == EarlyAlarmEnum.getCode()) {
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").is(0);
            operations.add(Aggregation.match(criteria));
            //operations.add(Aggregation.project("MN","PollutantCode", "LastMonitorValue","FirstOverTime", "LastOverTime", "AlarmLevel"));
            operations.add(Aggregation.project("MN","PollutantCode").and(DateOperators.DateToString.dateOf("LastOverTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("LastOverTime"));
            operations.add(Aggregation.group("PollutantCode").count().as("num").max("LastOverTime").as("MaxTime"));
        } else if (remind == OverAlarmEnum.getCode()) {
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
            operations.add(Aggregation.match(criteria));
            //operations.add(Aggregation.project("MN","PollutantCode", "LastMonitorValue","FirstOverTime","LastOverTime",  "AlarmLevel"));
            operations.add(Aggregation.project("MN","PollutantCode").and(DateOperators.DateToString.dateOf("LastOverTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("LastOverTime"));
            operations.add(Aggregation.group("PollutantCode").count().as("num").max("LastOverTime").as("MaxTime"));
        } else if (remind == ExceptionAlarmEnum.getCode()) {
            //异常
            criteria.and("MN").in(mns).and("FirstExceptionTime").gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            //operations.add(Aggregation.project("MN", "PollutantCode","MonitorValue","FirstExceptionTime", "LastExceptionTime","ExceptionType"));
            operations.add(Aggregation.project("MN","PollutantCode").and(DateOperators.DateToString.dateOf("LastExceptionTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("LastExceptionTime"));
            operations.add(Aggregation.group("PollutantCode").count().as("num").max("LastExceptionTime").as("MaxTime"));
        }
        if (operations.size()>0) {
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            return pageResults.getMappedResults();
        }else{
            return new ArrayList<>();
        }
    }

    /**
     * 统计报警时段段数
     * */
    @Override
    public List<Document> countPointAlarmTimesNumDataByParam(String collection, int remind, String starttime, String endtime, Set<String> mns) {
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Map<String,Object> pushmap = new HashMap<>();
        if (remind == EarlyAlarmEnum.getCode()) {
            pushmap.put("starttime", "$FirstOverTime");
            pushmap.put("endtime", "$LastOverTime");
            pushmap.put("code", "$PollutantCode");
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").is(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","FirstOverTime","LastOverTime","PollutantCode").and(DateOperators.DateToString.dateOf("LastOverTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("lasttime"));
            operations.add(Aggregation.group("MN").max("lasttime").as("MaxTime").push(pushmap).as("times"));
        } else if (remind == OverAlarmEnum.getCode()) {
            pushmap.put("starttime", "$FirstOverTime");
            pushmap.put("endtime", "$LastOverTime");
            pushmap.put("code", "$PollutantCode");
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","FirstOverTime","LastOverTime","PollutantCode").and(DateOperators.DateToString.dateOf("LastOverTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("lasttime"));
            operations.add(Aggregation.group("MN").max("lasttime").as("MaxTime").push(pushmap).as("times"));
        } else if (remind == ExceptionAlarmEnum.getCode()) {
            pushmap.put("starttime", "$FirstExceptionTime");
            pushmap.put("endtime", "$LastExceptionTime");
            pushmap.put("code", "$PollutantCode");
            //异常
            criteria.and("MN").in(mns).and("FirstExceptionTime").gte(startDate).lte(endDate).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","FirstExceptionTime","LastExceptionTime","PollutantCode").and(DateOperators.DateToString.dateOf("LastExceptionTime").toString("%Y-%m-%d %H:%M:%S").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("lasttime"));
            operations.add(Aggregation.group("MN").max("lasttime").as("MaxTime").push(pushmap).as("times"));
        }
        if (operations.size()>0) {
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
            return pageResults.getMappedResults();
        }else{
            return new ArrayList<>();
        }
    }

    /**
     * 获取小时、日浓度突变报警数据
     * */
    @Override
    public List<Document> getHourOrDayChangeAlarmDataByParamMap(Map<String, Object> paramMap) {
        //分钟浓度突变
        String datetype = paramMap.get("datetype").toString();
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String collection = "";
        String liststr = "";
        if("hour".equals(datetype)){
            liststr = "HourDataList";
            collection = "HourData";
        }/*else if("day".equals(datetype)){
            liststr = "DayDataList";
            collection = "Daydata";
        }*/
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(mns).and(liststr+".IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
        criteria1.and(liststr+".IsSuddenChange").is(true);
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("PollutantCode", "$PollutantCode");
        pollutantList.put("MonitorValue", "$MonitorValue");
        pollutantList.put("ChangeMultiple", "$ChangeMultiple");
        pollutantList.put("ChangeTime", "$MonitorTime");
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(liststr));
        operations.add(Aggregation.match(criteria1));
        operations.add(Aggregation.project("DataGatherCode", "MonitorTime").
                and(liststr+".PollutantCode").as("PollutantCode").
                and(liststr+".AvgStrength").as("MonitorValue").
                and(liststr+".ChangeMultiple").as("ChangeMultiple")
        );
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        operations.add(Aggregation.group("DataGatherCode")
                .max("MonitorTime").as("lasttime")
                .push(pollutantList).as("pollutantList") );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }

    /**
     * 获取小时或日报警数据（超阈值、超限、异常）
     * */
    @Override
    public List<Document> getHourOrDayLastAlarmDataByParamMap(Map<String, Object> paramMap) {
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String endtime = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(endtime);
        String timefield = paramMap.get("monitortimekey").toString();
        String collection = paramMap.get("collection").toString();
        String datetype = paramMap.get("datetype").toString();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        String DataType = "";
        if ("hour".equals(datetype)){
            DataType = "HourData";
        }/*else if("day".equals(datetype)){
            DataType = "DayData";
        }*/
        if (paramMap.get("exceptiontype") != null) {
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").is(DataType).and("ExceptionType").ne(NoFlowExceptionEnum.getCode());
        } else {
            criteria.and("DataGatherCode").in(mns).and(timefield).gte(startDate).lte(endDate).and("DataType").is(DataType);
        }
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("PollutantCode", "$PollutantCode");
        pollutantList.put("MonitorValue", "$MonitorValue");
        pollutantList.put("DataType", "$DataType");
        if (!timefield.equals("ExceptionTime")) {
            pollutantList.put("AlarmLevel", "$AlarmLevel");
        }
        pollutantList.put("MonitorTime", "$" + timefield);
        operations.add(Aggregation.match(criteria));
        if (timefield.equals("ExceptionTime")) {
            pollutantList.put("ExceptionType", "$ExceptionType");
            operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield, "PollutantCode", "MonitorValue", "DataType", "ExceptionType"));
        } else {
            operations.add(Aggregation.project("DataGatherCode", "lasttime", timefield, "PollutantCode", "MonitorValue", "AlarmLevel", "DataType"));
        }
        operations.add(Aggregation.group("DataGatherCode")
                .max(timefield).as("lasttime")
                .push(pollutantList).as("pollutantList")

        );
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;
    }

    /**
     * 获取小时日报警时段
     * */
    @Override
    public Map<String, Object> setHourOrDayIntegrationAlarmData(Integer remindCode, Map<String, Object> paramMap) {
        Map<String, Object> result = new HashMap<>();
        List<String> mns = (List<String>) paramMap.get("mns");
        String starttime = paramMap.get("starttime").toString() + " 00:00:00";
        String end = paramMap.get("endtime").toString() + " 23:59:59";
        Date startDate = DataFormatUtil.parseDate(starttime);
        Date endDate = DataFormatUtil.parseDate(end);
        String datetype = paramMap.get("datetype").toString();
        String liststr = "";
        String collection = "";
        if("hour".equals(datetype)){
            liststr = "HourDataList";
            collection = "HourData";
        }/*else if("day".equals(datetype)){
            liststr = "DayDataList";
            collection = "DayData";
        }*/
        List<Document> listItems = new ArrayList<>();
        List<Criteria> orOperator = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteria1 = new Criteria();
        criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
        if (remindCode == EarlyAlarmEnum.getCode()) {
            criteria1.and(liststr+".IsOver").is(0);
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(liststr));
            operations.add(Aggregation.match(criteria1));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").
                    and(liststr+".PollutantCode").as("PollutantCode")
            );
            operations.add(Aggregation.group("DataGatherCode","MonitorTime","PollutantCode"));
        } else if (remindCode == OverAlarmEnum.getCode()) {
            //超限报警
            orOperator.add(Criteria.where(liststr + ".IsOver").gt(0));
            orOperator.add(Criteria.where(liststr + ".IsOverStandard").is(true));
            criteria1.orOperator(orOperator.toArray(new Criteria[orOperator.size()]));
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(liststr));
            operations.add(Aggregation.match(criteria1));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").
                    and(liststr+".PollutantCode").as("PollutantCode").and(liststr+".IsOver").as("AlarmLevel")
            );
            operations.add(Aggregation.group("DataGatherCode","MonitorTime","PollutantCode","AlarmLevel"));
        } else if (remindCode == ExceptionAlarmEnum.getCode()) {
            //异常
            criteria1.and(liststr+".IsException").gt(0);
            operations.add(Aggregation.match(criteria));
            operations.add(unwind(liststr));
            operations.add(Aggregation.match(criteria1));
            operations.add(Aggregation.project("DataGatherCode", "MonitorTime").
                    and(liststr+".PollutantCode").as("PollutantCode").and(liststr+".IsException").as("ExceptionType")
            );
            operations.add(Aggregation.group("DataGatherCode","MonitorTime","PollutantCode","ExceptionType"));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();
        if (listItems != null && listItems.size() > 0) {
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            for (String mn : mapDocuments.keySet()) {
                List<Document> documents = mapDocuments.get(mn);
                Map<String, Object> codeanddata = new HashMap<>();
                Map<String, Map<String, Object>> levelmap = new HashMap<>();
                String monitortime;
                Set<String> codes = new HashSet<>();
                if(documents!=null&&documents.size()>0) {
                    //按时间排序
                    documents = documents.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("MonitorTime"))).collect(Collectors.toList());
                    for (Document document : documents) {
                        //报警时间
                        monitortime = "";
                        if ("hour".equals(datetype)) {
                            monitortime = DataFormatUtil.getDateHM(document.getDate("MonitorTime"));
                            monitortime = monitortime.substring(0,2);
                        }/* else if ("day".equals(datetype)) {
                            monitortime = DataFormatUtil.getDateMD(document.getDate("MonitorTime"));
                        }*/
                        //污染物
                        String code = document.getString("PollutantCode");
                        codes.add(code);
                        //按污染物和报警级别分组
                        if (levelmap.get(code) != null) {
                            Map<String, Object> levelone = levelmap.get(code);
                            if (remindCode == OverAlarmEnum.getCode()) {
                                String level = document.getInteger("AlarmLevel").toString();
                                String overstr = "";
                                if ("-1".equals(level)) {
                                    overstr = "超标";
                                } else {
                                    overstr = "超限";
                                }
                                //判断报警时间是否连续
                                if (levelone.get(overstr) != null && !"".equals(monitortime)) {//判断是否有该级别的数据
                                    levelone.put(overstr, levelone.get(overstr) + "、" + monitortime);
                                } else {
                                    levelone.put(overstr, monitortime);
                                }
                            } else if (remindCode == ExceptionAlarmEnum.getCode()) {
                                String overstr = "";
                                overstr = CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"));
                                if (levelone.get(overstr) != null && !"".equals(monitortime)) {//判断是否有该级别的数据
                                    levelone.put(overstr, levelone.get(overstr) + "、" + monitortime);
                                } else {
                                    levelone.put(overstr, monitortime);
                                }
                            }
                        } else {
                            Map<String, Object> levelone = new HashMap<>();
                            if (remindCode == OverAlarmEnum.getCode()) {
                                String level = document.getInteger("AlarmLevel").toString();
                                String overstr = "";
                                if ("-1".equals(level)) {
                                    overstr = "超标";
                                } else {
                                    overstr = "超限";
                                }
                                levelone.put(overstr, monitortime);
                            } else if (remindCode == ExceptionAlarmEnum.getCode()) {
                                String overstr = "";
                                overstr = CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType"));
                                levelone.put(overstr, monitortime);
                            }
                            levelmap.put(code, levelone);
                        }
                    }
                    if (codes != null && codes.size() > 0) {
                        for (String code : codes) {
                            String codestr = "";
                            String alarmtimes ;
                            if (levelmap.get(code) != null) {
                                Map<String, Object> onemap = levelmap.get(code);
                                for (String key : onemap.keySet()) {
                                    if (onemap.get(key)!=null &&!"".equals(onemap.get(key).toString())) {
                                        alarmtimes = setAlarmTimes(onemap.get(key).toString());
                                        codestr += key + ":" + alarmtimes + ";";
                                    }
                                }
                            }
                            codeanddata.put(code, codestr);
                        }
                    }
                    result.put(mn, codeanddata);
                }
                //获取点位 总报警时长
                String totaltime = coutOnePointAlarmTimesForHourOrDay(documents,datetype);
                result.put(mn+"_totaltime", totaltime);
            }
        }
        return result;
    }

    /**
     * 统计单个点报警总时长(小时、日类型)
     * */
    private String coutOnePointAlarmTimesForHourOrDay(List<Document> documents, String datetype) {
        String alarmtotal = "";
        Set<String> timelist = new HashSet<>();
        for (Document podo : documents) {
            //比较时间 获取报警时段
                if("hour".equals(datetype)){
                    timelist.add(DataFormatUtil.getDateHM(podo.getDate("MonitorTime")));
                }/*else if("day".equals(datetype)){
                    timelist.add(DataFormatUtil.getDateMD(podo.getDate("MonitorTime")));
                }*/
        }
        if (timelist.size()>0){
            if("hour".equals(datetype)){
                alarmtotal = timelist.size()+"小时";
            }/*else if("day".equals(datetype)){
                alarmtotal = (timelist.size()) * 24+"小时";
            }*/
        }
        return alarmtotal;
    }

    /**
     * @author: xsm
     * @date: 2022/07/07 0007 下午 14:54
     * @Description: 通过自定义参数获取报警污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map> getOnePointAlarmPollutantDataByParamForApp(String dgimn, Date startDate, Date endDate, Integer monitorpointtype) {
        List<Map> result = new ArrayList<>();
        List<Document> listItems = new ArrayList<>();
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        //超限报警
        criteria.and("MN").is(dgimn).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
        operations.add(Aggregation.match(criteria));
        //operations.add(Aggregation.project("MN","PollutantCode", "LastMonitorValue","FirstOverTime","LastOverTime",  "AlarmLevel"));
        operations.add(Aggregation.project("MN","PollutantCode"));
        operations.add(Aggregation.group("PollutantCode"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, "OverModel", Document.class);
        listItems = pageResults.getMappedResults();
        if (listItems != null && listItems.size() > 0) {
            Map<String, Object> param = new HashMap<>();
            //获取单个点位 的污染物信息 标准值数据
            Map<String, Map<String, Object>> mnCodeAndStandardMap = new HashMap<>();
            param.put("dgimn", dgimn);
            param.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> pollutantStandardDataList = pollutantFactorMapper.getPollutantStandarddataByParam(param);
            if (pollutantStandardDataList!=null&&pollutantStandardDataList.size()>0){
                for (Map<String, Object> map:pollutantStandardDataList){
                    if (map.get("Code")!=null){
                        for (Document document : listItems) {
                            if (document.get("_id")!=null&&map.get("Code").toString().equals(document.getString("_id"))){
                                if (map.get("StandardMinValue") != null && !"".equals(map.get("StandardMinValue").toString()) && map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                                    map.put("standstr",map.get("StandardMinValue") + "-" + map.get("StandardMaxValue"));
                                } else {
                                    if (map.get("StandardMinValue") != null && !"".equals(map.get("StandardMinValue").toString())) {
                                        map.put("standstr",">" + map.get("StandardMinValue"));
                                    } else if (map.get("StandardMaxValue") != null && !"".equals(map.get("StandardMaxValue").toString())) {
                                        map.put("standstr","<" + map.get("StandardMaxValue"));
                                    }
                                }
                                result.add(map);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     *组装小时报警时段
     * */
    private String setAlarmTimes(String alarmTimes) {
        String times ="";
        String start = "";
        String end = "";
        if (!"".equals(alarmTimes)){
            String[] timelist = alarmTimes.split("、");
            for (String onetime:timelist){
                if ("".equals(start) && "".equals(end)){
                    start = onetime;
                    end = onetime;
                }else{
                    //用当前超标时间和上个时间比较 判断是否连续报警 是则合并 如 1时-3时
                    if (Integer.valueOf(onetime) - Integer.valueOf(end) ==1){
                        //相差为1个小时 重新赋值end
                        end = onetime;
                    }else{//不连续 重新赋值 start end
                        if(start.equals(end)){
                            times = times+Integer.valueOf(start)+"时、";
                        }else{
                            times = times+Integer.valueOf(start)+"时-"+Integer.valueOf(end)+"时、";
                        }
                        start = onetime;
                        end = onetime;
                    }
                }
            }
            if(start.equals(end)){
                times = times+Integer.valueOf(start)+"时、";
            }else{
                times = times+Integer.valueOf(start)+"时-"+Integer.valueOf(end)+"时、";
            }
        }
        if (!"".equals(times)){
            times = times.substring(0,times.length()-1);
        }
        return times;
    }
}
