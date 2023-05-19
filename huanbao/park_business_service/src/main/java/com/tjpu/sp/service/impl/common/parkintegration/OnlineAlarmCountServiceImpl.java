package com.tjpu.sp.service.impl.common.parkintegration;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;
import com.tjpu.sp.service.common.parkintegration.OnlineAlarmCountService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineAlarmCountServiceImpl implements OnlineAlarmCountService {

    private final MongoTemplate mongoTemplate;
    public OnlineAlarmCountServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private VideoCameraMapper videoCameraMapper;


    @Override
    public List<Map> countIntegrationAlarmDataByParamForApp(Integer remind, List<String> allmns, List<Map<String, Object>> hb_allpoints, Date startDate, Date endDate, Map<String, Object> pollutantinfo)  throws ParseException {
        List<Map> listdata = new ArrayList<>();
        String timefield = "";
        String collection = "";
        List<Document> listItems = new ArrayList<>();
        Map<String,Object> param = new HashMap<>();
        List<Map<String, Object>> aqstands = new ArrayList<>();
        Map<String, List< Map<String, Object>>> aqmn_stands = new HashMap<>();
        Map<String, Object> pollutants = new HashMap<>();
        Map<String, Object> codeandunit = new HashMap<>();

        if (pollutantinfo.get("pollutants")!=null){
            pollutants = (Map<String, Object>) pollutantinfo.get("pollutants");
        }
        if (pollutantinfo.get("codeandunit")!=null){
            codeandunit = (Map<String, Object>) pollutantinfo.get("codeandunit");
        }
        Map<String, Map<String, Object>> mnAndPointData = new HashMap<>();
        List<String> mns = new ArrayList<>();
        if (hb_allpoints!=null&&hb_allpoints.size()>0) {
            for (Map<String, Object> tempMn : hb_allpoints) {
                if (tempMn.get("dgimn") != null&& allmns.contains(tempMn.get("dgimn").toString())) {
                    mns.add(tempMn.get("dgimn").toString());
                    mnAndPointData.put(tempMn.get("dgimn").toString(), tempMn);
                }
            }
        }
        List<AggregationOperation> operations = new ArrayList<>();
        Criteria criteria = new Criteria();
        if (remind == EarlyAlarmEnum.getCode()) {
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").is(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","PollutantCode", "LastMonitorValue","FirstOverTime", "LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN",timefield));
        } else if (remind == OverAlarmEnum.getCode()) {
            //超限报警
            timefield = "FirstOverTime";
            collection = "OverModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN","PollutantCode", "LastMonitorValue","FirstOverTime","LastOverTime", "OverTime", "AlarmLevel")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN",timefield));
        } else if (remind == ExceptionAlarmEnum.getCode()) {
            //异常
            timefield = "FirstExceptionTime";
            collection = "ExceptionModel";
            criteria.and("MN").in(mns).and(timefield).gte(startDate).lte(endDate);
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "PollutantCode","MonitorValue","FirstExceptionTime", "LastExceptionTime", "ExceptionTime","ExceptionType")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC,  "MN",timefield));
        }
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        listItems = pageResults.getMappedResults();
        if (listItems != null && listItems.size() > 0) {
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = listItems.stream().collect(Collectors.groupingBy(m -> m.get("MN").toString()));
            List<String> dgimns = new ArrayList<>();
            for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                dgimns.add(entry.getKey());
            }
            if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                param.put("dgimns", mns);
                aqstands = pollutantFactorMapper.getEnvPollutantStandardDataByParam(param);
                if (aqstands != null && aqstands.size() > 0) {
                    aqmn_stands = aqstands.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
                }
            }
            for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                Map<String, Object> mappedResult = new HashMap<>();
                String mn = entry.getKey();    //mn
                mappedResult = mnAndPointData.get(mn);
                mappedResult.put("tasktatus","");
                List<Document> valuedata = entry.getValue();
                String alarmlasttime = "";
                mappedResult.put("alarmtype", remind);
                if (remind == EarlyAlarmEnum.getCode()) {
                    mappedResult.put("alarmname", EarlyAlarmEnum.getName());
                } else if (remind == OverAlarmEnum.getCode()) {
                    mappedResult.put("alarmname", OverAlarmEnum.getName());
                } else if (remind == ExceptionAlarmEnum.getCode()) {
                    mappedResult.put("alarmname", ExceptionAlarmEnum.getName());
                }
                Map<String,Set<String>> codeandlevel = new HashMap<>();
                Map<String,String> codeandtime = new HashMap<>();
                List<String> pollutantdata = new ArrayList<>();
                List<Map<String,Object>> poalarmlist = new ArrayList<>();
                String monitorpointtype = mappedResult.get("monitorpointtype")!=null?mappedResult.get("monitorpointtype").toString():"";
                if (valuedata!=null&&valuedata.size()>0) {
                    String firsttime = "";
                    String endtime = "";
                    Set<String> set;
                    for (Document document : valuedata) {
                        String standardname = "";
                        String standardvalue = "";
                        String overname = "";
                        String levelcode = "";
                        //污染物
                        String code = document.getString("PollutantCode");
                        Map<String,Object> onemap = new HashMap<>();
                        //异常类型
                        if (remind == ExceptionAlarmEnum.getCode()) {
                            //报警时间
                            firsttime = DataFormatUtil.getDateHM(document.getDate("FirstExceptionTime"));
                            endtime = DataFormatUtil.getDateHM(document.getDate("LastExceptionTime"));
                            if (codeandlevel.get(code)!=null){
                                set = codeandlevel.get(code);
                                set.add(CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType")));
                                codeandlevel.put(code,set);
                            }else{
                                set =new HashSet<>();
                                set.add(CommonTypeEnum.ExceptionTypeEnum.getNameByCode(document.getString("ExceptionType")));
                                codeandlevel.put(code,set);
                            }
                        }
                        List<Map<String, Object>> aq_stands = null;
                        //标准值
                        if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                            firsttime = DataFormatUtil.getDateHM(document.getDate("FirstOverTime"));
                            endtime = DataFormatUtil.getDateHM(document.getDate("LastOverTime"));
                            if (aqmn_stands.get(mn)!=null){//是安全类型
                                aq_stands = aqmn_stands.get(mn);
                                for (Map<String, Object> standobj : aq_stands) {
                                    if (standobj.get("Code") != null && code.equals(standobj.get("Code").toString())) {
                                        if (document.get("AlarmLevel")!=null){
                                            String level = document.getInteger("AlarmLevel").toString();
                                            levelcode = level;
                                            if ("-1".equals(level)){
                                                overname = "超标";
                                                standardvalue = standobj.get("StandardMaxValue")!=null?standobj.get("StandardMaxValue").toString():"";
                                                break;
                                            }else{
                                                if (standobj.get("FK_AlarmLevelCode") != null && level.equals(standobj.get("FK_AlarmLevelCode").toString())) {
                                                    standardvalue = standobj.get("ConcenAlarmMaxValue")!=null?standobj.get("ConcenAlarmMaxValue").toString():"";
                                                    if (level.equals("0") ){
                                                        overname = "预警";
                                                    }else if(level.equals("1") ){
                                                        overname = "超限";
                                                    }else if(level.equals("2") ){
                                                        overname = "超限";
                                                    }else if(level.equals("3") ){
                                                        overname = "超限";
                                                    }
                                                    break;
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                            if (codeandlevel.get(code)!=null){
                                set = codeandlevel.get(code);
                                set.add(overname);
                                codeandlevel.put(code,set);
                            }else{
                                set =new HashSet<>();
                                set.add(overname);
                                codeandlevel.put(code,set);
                            }
                        }
                        if (!firsttime.equals(endtime)){//报警开始时间不等于结束时间
                            if (codeandtime!=null&&codeandtime.size()>0){
                                if (codeandtime.get(code)!=null){
                                    codeandtime.put(code,codeandtime.get(code)+"、"+firsttime+"-"+endtime);
                                }else{
                                    codeandtime.put(code,firsttime+"-"+endtime);
                                }

                            }else{
                                codeandtime.put(code,firsttime+"-"+endtime);
                            }
                        }else{
                            if (codeandtime!=null&&codeandtime.size()>0){
                                if (codeandtime.get(code)!=null){
                                    codeandtime.put(code,codeandtime.get(code)+"、"+firsttime);
                                }else{
                                    codeandtime.put(code,firsttime);
                                }
                            }else{
                                codeandtime.put(code,firsttime);
                            }
                        }
                        if (!"".equals(alarmlasttime)){
                            String lasttimestr ="";
                            if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                                lasttimestr = DataFormatUtil.getDateYMDHMS(document.getDate("LastOverTime"));
                            }else{
                                lasttimestr = DataFormatUtil.getDateYMDHMS(document.getDate("LastExceptionTime"));
                            }
                            if (alarmlasttime.equals(lasttimestr)){
                                if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                                    alarmlasttime = DataFormatUtil.getDateYMDHMS(document.getDate("LastOverTime"));
                                    onemap.put("monitorvalue", document.get("LastMonitorValue"));
                                }else{
                                    alarmlasttime = DataFormatUtil.getDateYMDHMS(document.getDate("LastExceptionTime"));
                                    onemap.put("monitorvalue", document.get("MonitorValue"));
                                }
                                onemap.put("pollutantcode",document.get("PollutantCode"));
                                onemap.put("pollutantname", pollutants.get(document.getString("PollutantCode")+"_"+monitorpointtype));
                                onemap.put("pollutantunit", codeandunit.get(document.getString("PollutantCode")+"_"+monitorpointtype));
                                onemap.put("standardvalue", standardvalue);
                                onemap.put("standardname", standardname);
                                onemap.put("alarmlevel", levelcode);
                                poalarmlist.add(onemap);
                            }else{
                                poalarmlist.clear();
                                if (compare(alarmlasttime,lasttimestr)) {
                                    alarmlasttime = lasttimestr;
                                    onemap.put("pollutantcode",document.get("PollutantCode"));
                                    if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                                        onemap.put("monitorvalue", document.get("LastMonitorValue"));
                                    }else{
                                        onemap.put("monitorvalue", document.get("MonitorValue"));
                                    }
                                    onemap.put("pollutantname", pollutants.get(document.getString("PollutantCode")+"_"+monitorpointtype));
                                    onemap.put("pollutantunit", codeandunit.get(document.getString("PollutantCode")+"_"+monitorpointtype));
                                    onemap.put("standardvalue", standardvalue);
                                    onemap.put("standardname", standardname);
                                    onemap.put("alarmlevel", levelcode);
                                    poalarmlist.add(onemap);
                                }
                            }

                        }else{
                            if (remind == EarlyAlarmEnum.getCode() || remind == OverAlarmEnum.getCode()) {
                                alarmlasttime = DataFormatUtil.getDateYMDHMS(document.getDate("LastOverTime"));
                                onemap.put("monitorvalue", document.get("LastMonitorValue"));
                            }else{
                                alarmlasttime = DataFormatUtil.getDateYMDHMS(document.getDate("LastExceptionTime"));
                                onemap.put("monitorvalue", document.get("MonitorValue"));
                            }
                            onemap.put("pollutantcode",document.get("PollutantCode"));
                            onemap.put("pollutantname", pollutants.get(document.getString("PollutantCode")+"_"+monitorpointtype));
                            onemap.put("pollutantunit", codeandunit.get(document.getString("PollutantCode")+"_"+monitorpointtype));
                            onemap.put("standardvalue", standardvalue);
                            onemap.put("standardname", standardname);
                            onemap.put("alarmlevel", levelcode);
                            poalarmlist.add(onemap);
                        }
                    }
                }

                for(String key:codeandlevel.keySet()){
                    String remark = "";
                    String name = pollutants.get(key+"_"+monitorpointtype)+"";
                    if (remind == EarlyAlarmEnum.getCode()) {
                        remark = name+"出现超阈值预警,"+codeandtime.get(key);
                    } else if (remind == OverAlarmEnum.getCode()) {
                        remark = name+"出现超限报警,"+codeandtime.get(key);
                    } else if (remind == ExceptionAlarmEnum.getCode()) {
                        String strtt = "";
                        Set<String> set1 =codeandlevel.get(key);
                        for (String str1:set1){
                            strtt = strtt+str1+"、";
                        }
                        if (!"".equals(strtt)){
                            strtt = strtt.substring(0,strtt.length()-1);
                        }
                        remark = name+"出现"+strtt+","+codeandtime.get(key);
                    }
                    pollutantdata.add(remark);
                }
                mappedResult.put("alarmlasttime", DataFormatUtil.FormatDateOneToOther(alarmlasttime,"yyyy-MM-dd HH:mm:ss", "HH:mm:ss"));
                mappedResult.put("pollutantlist",poalarmlist);
                mappedResult.put("alarmdescribe",pollutantdata);
                listdata.add(mappedResult);
            }
        }
        return listdata;
    }


    @Override
    public List<Map<String, Object>> countVideoAlarmDataNumByParam(Map<String, Object> paramMap) {
        return  videoCameraMapper.countVideoAlarmDataNumByParam(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2020/12/17 0017 下午 5:54
     * @Description: 统计监测点在开始时间到结束时间范围内的浓度、排放量突变
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countIntegrationChangeDataByParamMap(Integer remind, List<String> mns, Date startDate, Date endDate, Map<String, Object> pollutantinfo, Map<String, Map<String, Object>> mnAndPointData) throws ParseException {
        List<Map> result = new ArrayList<>();
        Map<String, Object> pollutants = new HashMap<>();
        Map<String, Object> codeandunit = new HashMap<>();
        if (pollutantinfo.get("pollutants")!=null){
            pollutants = (Map<String, Object>) pollutantinfo.get("pollutants");
        }
        if (pollutantinfo.get("codeandunit")!=null){
            codeandunit = (Map<String, Object>) pollutantinfo.get("codeandunit");
        }
        //浓度突变
        Criteria criteria = new Criteria();
        Criteria criteria2 = new Criteria();
        List<Document> listdata = new ArrayList<>();
        Map<String, Object> timelist = new HashMap<>();
        if (remind == FlowChangeEnum.getCode()) {  //排放量
            timelist.put("time", "$MonitorTime");
            criteria.and("DataGatherCode").in(mns).and("HourFlowDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            criteria2.and("HourFlowDataList.IsSuddenChange").is(true);
            listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), unwind("HourFlowDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").and("HourFlowDataList.PollutantCode").as("code").and("HourFlowDataList.AvgFlow").as("MonitorValue")
                    , group("DataGatherCode", "code")
                            .last("MonitorTime").as("lastime")
                            .max("MonitorValue").as("value")
                            .push(timelist).as("timelist")
            ), "HourFlowData", Document.class).getMappedResults();

        } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
            timelist.put("time", "$MonitorTime");
            criteria.and("DataGatherCode").in(mns).and("MinuteDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
            criteria2.and("MinuteDataList.IsSuddenChange").is(true);
            listdata = mongoTemplate.aggregate(newAggregation(
                    match(criteria), unwind("MinuteDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").and("MinuteDataList.PollutantCode").as("code").and("MinuteDataList.AvgStrength").as("MonitorValue")
                    , group("DataGatherCode", "code")
                            .last("MonitorValue").as("value")
                            .max("MonitorTime").as("lasttime")
                            .push(timelist).as("timelist")
            ), "MinuteData", Document.class).getMappedResults();
        }
        if (listdata.size() > 0) {
            //通过mn号分组数据
            Map<String, List<Document>> mapDocuments = listdata.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
            String monitorpointtype = "";
            for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                Map<String,Object> mappedResult = new HashMap<>();
                String mn = entry.getKey();    //mn
                mappedResult = mnAndPointData.get(mn);
                List<Document> valuedata = entry.getValue();
                String alarmlasttime = "";
                String alarmtypename = "";
                if (remind == FlowChangeEnum.getCode()) {  //排放量
                    alarmtypename = FlowChangeEnum.getName();
                    mappedResult.put("alarmtype", remind);
                    mappedResult.put("alarmname", FlowChangeEnum.getName());
                } else if (remind == ConcentrationChangeEnum.getCode()) {    //浓度
                    alarmtypename = ConcentrationChangeEnum.getName();
                    mappedResult.put("alarmtype", remind);
                    mappedResult.put("alarmname", ConcentrationChangeEnum.getName());
                }
                monitorpointtype = mappedResult.get("monitorpointtype")!=null?mappedResult.get("monitorpointtype").toString():"";
                List<String> pollutantdata = new ArrayList<>();
                List<Map<String,Object>> lastalarmdata = new ArrayList<>();
                //Map<String,Map<String,Object>> lastalarmdata = new HashMap<>();
                if (valuedata!=null&&valuedata.size()>0) {
                    for (Document document : valuedata) {
                       String str = "";
                       List<Document> polist = (List<Document>) document.get("timelist");
                       Map<String,Object> onedata = new HashMap<>();
                       if (polist!=null&&polist.size()>0) {
                           str = pollutants.get(document.getString("code")+"_"+monitorpointtype)+"出现"+alarmtypename+",";
                           for (Document doc : polist) {
                               str = str + DataFormatUtil.getDateHM(doc.getDate("time"))+"、";
                           }
                       }
                       if (!"".equals(str)){
                           str = str.substring(0,str.length() -1);
                       }
                        pollutantdata.add(str);
                        if (!"".equals(alarmlasttime)){//同个污染物
                            if (alarmlasttime.equals(DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")))){
                                onedata.put("pollutantcode",document.get("code"));
                                onedata.put("monitorvalue", document.get("value"));
                                onedata.put("pollutantname", pollutants.get(document.getString("code")+"_"+monitorpointtype));
                                onedata.put("pollutantunit", codeandunit.get(document.getString("code")+"_"+monitorpointtype));
                                lastalarmdata.add(onedata);
                            }else{
                                if (compare(alarmlasttime, DataFormatUtil.getDateYMDHMS(document.getDate("lasttime")))) {
                                    lastalarmdata.clear();
                                    alarmlasttime = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                                    onedata.put("pollutantcode",document.get("code"));
                                    onedata.put("monitorvalue", document.get("value"));
                                    onedata.put("pollutantname", pollutants.get(document.getString("code")+"_"+monitorpointtype));
                                    onedata.put("pollutantunit", codeandunit.get(document.getString("code")+"_"+monitorpointtype));
                                    lastalarmdata.add(onedata);
                                }
                            }
                        }else{
                            alarmlasttime = DataFormatUtil.getDateYMDHMS(document.getDate("lasttime"));
                            onedata.put("pollutantcode",document.get("code"));
                            onedata.put("monitorvalue", document.get("value"));
                            onedata.put("pollutantname", pollutants.get(document.getString("code")+"_"+monitorpointtype));
                            onedata.put("pollutantunit", codeandunit.get(document.getString("code")+"_"+monitorpointtype));
                            lastalarmdata.add(onedata);
                        }
                    }
                }
                mappedResult.put("alarmlasttime",DataFormatUtil.FormatDateOneToOther(alarmlasttime,"yyyy-MM-dd HH:mm:ss", "HH:mm:ss"));
                mappedResult.put("pollutantlist",lastalarmdata);
                mappedResult.put("alarmdescribe",pollutantdata);
                result.add(mappedResult);
            }
        }
        return result;
    }



    /**
     * @author: xsm
     * @date: 2021/01/25 0025 上午 11:26
     * @Description: 统计监测点在开始时间到结束时间范围各报警类型的报警数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map> countAlarmDataByParamGroupByAlarmType(List<Integer> alarmtypes, List<String> mns,List<String> hb_mns,List<String>aq_mns, Date startDate, Date endDate,Map<String, Object> paramMap) {
        List<Map> result = new ArrayList<>();

        for (Integer remind:alarmtypes){
            Map<String,Integer> alarm_num = new HashMap<>();
            Criteria criteria = new Criteria();
            List<Document> listdata = new ArrayList<>();
            Criteria criteria2 = new Criteria();
            List<Document> env_listdata = new ArrayList<>();
            int totalnum = 0;
            if (mns!=null&&mns.size()>0) {
                if (remind == ConcentrationChangeEnum.getCode()) {//浓度突变
                    criteria.and("DataGatherCode").in(mns).and("MinuteDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "MinuteData", Document.class).getMappedResults();
                } else if (remind == EarlyAlarmEnum.getCode()) {//超阈值
                    criteria.and("DataGatherCode").in(aq_mns).and("EarlyWarnTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "EarlyWarnData", Document.class).getMappedResults();
                    //环保
                    criteria2.and("MN").in(hb_mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").is(0);
                    env_listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria2), project("MN", "count")
                            , group("MN").count().as("count")
                    ), "OverModel", Document.class).getMappedResults();
                } else if (remind == OverAlarmEnum.getCode()) {//超限
                    criteria.and("DataGatherCode").in(aq_mns).and("OverTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "OverData", Document.class).getMappedResults();
                    //环保
                    criteria2.and("MN").in(hb_mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
                    env_listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria2), project("MN", "count")
                            , group("MN").count().as("count")
                    ), "OverModel", Document.class).getMappedResults();
                } else if (remind == ExceptionAlarmEnum.getCode()) {//异常
                    criteria.and("DataGatherCode").in(aq_mns).and("ExceptionTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "ExceptionData", Document.class).getMappedResults();
                    //环保
                    criteria2.and("MN").in(hb_mns).and("FirstExceptionTime").gte(startDate).lte(endDate);
                    env_listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria2), project("MN", "count")
                            , group("MN").count().as("count")
                    ), "ExceptionModel", Document.class).getMappedResults();
                }
            }
                if (remind == EarlyAlarmEnum.getCode()||remind == OverAlarmEnum.getCode()||remind == ExceptionAlarmEnum.getCode()){
                    totalnum = listdata.size()+env_listdata.size();
                }else{
                    totalnum = listdata.size();
                }
                alarm_num.put("alarmtype",remind);
                alarm_num.put("alarmnum",totalnum);
                result.add(alarm_num);

        }
        return result;
    }

    @Override
    public List<Map> countAlarmDataByParamGroupByMonitorType(List<Integer> alarmtypes, List<String> mns,List<String> hb_mns,List<String>aq_mns, Date startDate, Date endDate, Map<String, Object> paramMap,Map<String,String> mn_type) {
        List<Map> result = new ArrayList<>();
        Map<String,Integer> type_num = new HashMap<>();
        for (Integer remind:alarmtypes){
            Criteria criteria = new Criteria();
            List<Document> listdata = new ArrayList<>();
            Criteria criteria2 = new Criteria();
            List<Document> env_listdata = new ArrayList<>();
            if (mns!=null&&mns.size()>0) {
                if (remind == ConcentrationChangeEnum.getCode()) {//浓度突变
                    criteria.and("DataGatherCode").in(mns).and("MinuteDataList.IsSuddenChange").is(true).and("MonitorTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "MinuteData", Document.class).getMappedResults();
                } else if (remind == EarlyAlarmEnum.getCode()) {//超阈值
                    criteria.and("DataGatherCode").in(aq_mns).and("EarlyWarnTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "EarlyWarnData", Document.class).getMappedResults();
                    //环保
                    criteria2.and("MN").in(hb_mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").is(0);
                    env_listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria2), project("MN", "count")
                            , group("MN").count().as("count")
                    ), "OverModel", Document.class).getMappedResults();
                } else if (remind == OverAlarmEnum.getCode()) {//超限
                    criteria.and("DataGatherCode").in(aq_mns).and("OverTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "OverData", Document.class).getMappedResults();
                    //环保
                    criteria2.and("MN").in(hb_mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0);
                    env_listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria2), project("MN", "count")
                            , group("MN").count().as("count")
                    ), "OverModel", Document.class).getMappedResults();
                } else if (remind == ExceptionAlarmEnum.getCode()) {//异常
                    criteria.and("DataGatherCode").in(aq_mns).and("ExceptionTime").gte(startDate).lte(endDate);
                    listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria), project("DataGatherCode", "count")
                            , group("DataGatherCode").count().as("count")
                    ), "ExceptionData", Document.class).getMappedResults();
                    //环保
                    criteria2.and("MN").in(hb_mns).and("FirstExceptionTime").gte(startDate).lte(endDate);
                    env_listdata = mongoTemplate.aggregate(newAggregation(
                            match(criteria2), project("MN", "count")
                            , group("MN").count().as("count")
                    ), "ExceptionModel", Document.class).getMappedResults();
                }
            }
            if (listdata!=null&&listdata.size()>0){
                for (Document document : listdata) {
                    String DataGatherCode = document.getString("_id");
                        if (mn_type.get(DataGatherCode)!=null) {
                            String type = mn_type.get(DataGatherCode);
                            if (type_num.get(type) != null) {
                                type_num.put(type, type_num.get(type) +1);
                            } else {
                                type_num.put(type,1);
                            }
                        }
                }
            }
            if (env_listdata!=null&&env_listdata.size()>0){
                for (Document document : env_listdata) {
                    String DataGatherCode = document.getString("_id");
                    if (mn_type.get(DataGatherCode)!=null) {
                        String type = mn_type.get(DataGatherCode);
                        if (type_num.get(type) != null) {
                            type_num.put(type, type_num.get(type) +1);
                        } else {
                            type_num.put(type,1);
                        }
                    }
                }
            }
        }
        if (type_num!=null&&type_num.size()>0){
            for (Map.Entry<String, Integer> entry : type_num.entrySet()) {
                Map<String,Object> obj = new HashMap<>();
                obj.put("monitorpointtype",entry.getKey());
                obj.put("alarmnum",entry.getValue());
                result.add(obj);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2021/01/25 0025 上午 11:26
     * @Description: 统计监测点在开始时间到结束时间范围各报警类型的报警数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public  List<Map<String,Object>> getTodayAlarmAndDevOpsTasks(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTodayTaskInfoByTaskType(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTodayAlarmTasksByTaskTypes(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getTaskDisposeManagementDataByParam(paramMap);
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


}
