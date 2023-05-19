package com.tjpu.sp.service.impl.environmentalprotection.monitorpoint;


import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.MonitorPointService;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

@Service
@Transactional
public class MonitorPointServiceImpl implements MonitorPointService {

    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;

    @Autowired
    private EarlyWarningSetMapper earlyWarningSetMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper unorganizedMonitorPointInfoMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private WaterStationMapper waterStationMapper;


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    //超标数据表
    private final String overData_db = "OverData";
    //异常数据表
    private final String exceptionData_db = "ExceptionData";
    //超标model表
    private final String overModelCollection = "OverModel";


    /**
     * @author: xsm
     * @date: 2019/8/22 0022 上午 11:07
     * @Description: 根据监测点类型返回按点位状态分组的点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getGroupByStatusMonitorPointInfoByTypes(List<Integer> monitortypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> datalist = new ArrayList<>();
        List<Map<String, Object>> zx_listdata = new ArrayList<>();//在线
        List<Map<String, Object>> lx_listdata = new ArrayList<>();//离线
        List<Map<String, Object>> yc_listdata = new ArrayList<>();//异常
        List<Map<String, Object>> cb_listdata = new ArrayList<>();//超标
        String online = CommonTypeEnum.OnlineStatusEnum.NormalStatusEnum.getCode();
        String offline = CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum.getCode();
        String over = CommonTypeEnum.OnlineStatusEnum.OverStatusEnum.getCode();
        String exception = CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum.getCode();
        Map<String, Object> paramMap = new HashMap<>();
        for (Integer type : monitortypes) {
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(type)) {
                case WasteWaterEnum: //废水
                    paramMap.put("outputtype", "water");
                    datalist = waterOutputInfoMapper.getAllWaterOrRainOutPutInfoByOutputType(paramMap);//废水
                    break;
                case WasteGasEnum: //废气
                    paramMap.clear();
                    datalist = gasOutPutInfoMapper.getAllMonitorGasOutPutInfo(paramMap);//废气
                    break;
                case RainEnum: //雨水
                    paramMap.put("outputtype", "rain");
                    datalist = waterOutputInfoMapper.getAllWaterOrRainOutPutInfoByOutputType(paramMap);//雨水
                    break;
                case AirEnum: //空气
                    paramMap.clear();
                    datalist = airMonitorStationMapper.getAllAirMonitorStation(paramMap);//空气
                    break;
                case EnvironmentalVocEnum: //voc
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                    datalist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);//VOC
                    break;
                case meteoEnum: //气象
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
                    datalist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
                    datalist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case MicroStationEnum: //微站
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
                    datalist = otherMonitorPointMapper.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
                    datalist = unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap);//厂界恶臭
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    paramMap.clear();
                    paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum.getCode());
                    datalist = unorganizedMonitorPointInfoMapper.getOutPutUnorganizedInfoByIDAndType(paramMap);//厂界小型站
                    break;
            }
            if (datalist.size() > 0) {
                for (Map<String, Object> map : datalist) {
                    Map<String, Object> resultMap = new HashMap<>();
                    if (CommonTypeEnum.getOutPutTypeList().contains(type)) {
                        if (type == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()) {
                            resultMap.put("monitorpointid", map.get("PK_ID"));
                            resultMap.put("longitude", map.get("OutPutLongitude"));
                            resultMap.put("latitude", map.get("OutPutLatitude"));
                        } else {
                            resultMap.put("monitorpointid", map.get("pkid"));
                            resultMap.put("longitude", map.get("OutputLongitude"));
                            resultMap.put("latitude", map.get("OutputLatitude"));
                        }
                        resultMap.put("pollutionid", map.get("FK_PollutionID"));
                        resultMap.put("pollutionname", map.get("PollutionName"));

                    } else {
                        if (type == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                            resultMap.put("monitorpointid", map.get("PK_AirID"));
                        } else {
                            resultMap.put("monitorpointid", map.get("pkid"));
                        }
                        resultMap.put("longitude", map.get("Longitude"));
                        resultMap.put("latitude", map.get("Latitude"));
                    }

                    resultMap.put("monitorpointname", map.get("OutputName"));
                    resultMap.put("dgimn", map.get("DGIMN"));
                    resultMap.put("onlinestatus", map.get("OnlineStatus"));
                    resultMap.put("monitorpointtype", type);
                    if (map.get("OnlineStatus") != null) {
                        if (online.equals(map.get("OnlineStatus").toString())) {//在线
                            zx_listdata.add(resultMap);
                        } else if (offline.equals(map.get("OnlineStatus").toString())) {//离线
                            lx_listdata.add(resultMap);
                        } else if (over.equals(map.get("OnlineStatus").toString())) {//超标
                            cb_listdata.add(resultMap);
                        } else if (exception.equals(map.get("OnlineStatus").toString())) {//异常
                            yc_listdata.add(resultMap);
                        }
                    }
                }
            }
        }
        //点位在线状态统计数据
        Map<String, Object> zx_map = new HashMap<>();
        zx_map.put("statustype", online);
        zx_map.put("num", (zx_listdata != null && zx_listdata.size() > 0) ? zx_listdata.size() : 0);
        zx_map.put("listdata", zx_listdata);
        result.add(zx_map);
        //点位离线状态统计数据
        Map<String, Object> lx_map = new HashMap<>();
        lx_map.put("statustype", offline);
        lx_map.put("num", (lx_listdata != null && lx_listdata.size() > 0) ? lx_listdata.size() : 0);
        lx_map.put("listdata", lx_listdata);
        result.add(lx_map);
        //点位异常状态统计数据
        Map<String, Object> yc_map = new HashMap<>();
        yc_map.put("statustype", exception);
        yc_map.put("num", (yc_listdata != null && yc_listdata.size() > 0) ? yc_listdata.size() : 0);
        yc_map.put("listdata", yc_listdata);
        result.add(yc_map);
        //点位超标状态统计数据
        Map<String, Object> cb_map = new HashMap<>();
        cb_map.put("statustype", over);
        cb_map.put("num", (cb_listdata != null && cb_listdata.size() > 0) ? cb_listdata.size() : 0);
        cb_map.put("listdata", cb_listdata);
        result.add(cb_map);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2021/3/23 0023 上午 9:07
     * @Description: 判断最近两个小时是否存在报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Object getHourOverDataIsContinueOverJudge(Object mn, String daytime) {
        Object IsContinueOver = null;
        //判断最近两个小时是否存在报警数据
        if (mn != null && !"".equals(daytime)) {
            String ymdh = daytime.substring(0, 13);
            Date ymdhdate = DataFormatUtil.getDateYMDHMS(ymdh + ":00:00");
            Calendar instance = Calendar.getInstance();
            instance.setTime(ymdhdate);
            instance.add(Calendar.HOUR_OF_DAY, -1);
            Date startdate = instance.getTime();//一个小时前为开始时间
            Date enddate = DataFormatUtil.getDateYMDHMS(ymdh + ":59:59");

            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(mn + ""));
            query.addCriteria(Criteria.where("OverTime").gte(startdate).lte(enddate));
            //总条数
            query.with(new Sort(Sort.Direction.ASC, "OverTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, overData_db);
            if (documents != null && documents.size() > 0) {
                IsContinueOver = false;
                for (Document document : documents) {
                    if (document.get("OverTime") != null && !daytime.equals(DataFormatUtil.getDateYMDHMS(document.getDate("OverTime")))) {
                        IsContinueOver = true;
                        break;
                    }
                }
            }
        }
        return IsContinueOver;
    }

    /**
     * @author: xsm
     * @date: 2021/3/23 0023 上午 9:07
     * @Description: 判断最近两个小时是否存在异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Object getHourDataIsContinueExceptionJudge(Object mn, String daytime) {
        Object IsContinueException = null;
        //判断最近两个小时是否存在异常数据
        if (mn != null && !"".equals(daytime)) {
            String ymdh = daytime.substring(0, 13);
            Date ymdhdate = DataFormatUtil.getDateYMDHMS(ymdh + ":00:00");
            Calendar instance = Calendar.getInstance();
            instance.setTime(ymdhdate);
            instance.add(Calendar.HOUR_OF_DAY, -1);
            Date startdate = instance.getTime();//一个小时前为开始时间
            Date enddate = DataFormatUtil.getDateYMDHMS(ymdh + ":59:59");

            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(mn + ""));
            query.addCriteria(Criteria.where("ExceptionTime").gte(startdate).lte(enddate));
            //总条数
            query.with(new Sort(Sort.Direction.ASC, "ExceptionTime"));
            List<Document> documents = mongoTemplate.find(query, Document.class, exceptionData_db);
            if (documents != null && documents.size() > 0) {
                IsContinueException = false;
                for (Document document : documents) {
                    if (document.get("ExceptionTime") != null && !daytime.equals(DataFormatUtil.getDateYMDHMS(document.getDate("ExceptionTime")))) {
                        IsContinueException = true;
                        break;
                    }
                }
            }
        }
        return IsContinueException;
    }

    /**
     * @author: xsm
     * @date: 2021/3/23 0023 上午 9:07
     * @Description: 判断最近两个小时是否存在浓度突变数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Object getHourDataIsContinueChangeJudge(Object mn, String daytime) {
        Object IsContinue = null;
        //判断最近两个小时是否存在浓度突变数据
        if (mn != null && !"".equals(daytime)) {
            String ymdh = daytime.substring(0, 13);
            Date ymdhdate = DataFormatUtil.getDateYMDHMS(ymdh + ":00:00");
            Calendar instance = Calendar.getInstance();
            instance.setTime(ymdhdate);
            instance.add(Calendar.HOUR_OF_DAY, -1);
            Date startdate = instance.getTime();//一个小时前为开始时间
            Date enddate = DataFormatUtil.getDateYMDHMS(ymdh + ":59:59");
            Date hourenddate = DataFormatUtil.getDateYMDHMS(ymdh + ":00:00");

            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(mn + ""));
            query.addCriteria(Criteria.where("MonitorTime").gt(startdate).lte(enddate).and("MinuteDataList.IsSuddenChange").is(true));
            //总条数
            query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
            long alarmcount = mongoTemplate.count(query, "MinuteData");
            if (alarmcount > 0) {
                IsContinue = true;
            }
            Query querytwo = new Query();
            querytwo.addCriteria(Criteria.where("DataGatherCode").is(mn + ""));
            querytwo.addCriteria(Criteria.where("MonitorTime").gte(startdate).lt(hourenddate).and("HourDataList.IsSuddenChange").is(true));
            long alarmcounttwo = mongoTemplate.count(querytwo, "HourData");
            if (alarmcounttwo > 0) {
                IsContinue = true;
            }
        }
        return IsContinue;
    }

    @Override
    public List<Map<String, Object>> getMonitorPointDataTimeSetListByParam(Map<String, Object> paramMap) {
        return earlyWarningSetMapper.getMonitorPointDataTimeSetListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTimeDataSetByParam(Map<String, Object> paramMap) {
        return earlyWarningSetMapper.getTimeDataSetByParam(paramMap);
    }

    @Override
    public void updateListData(List<Map<String, Object>> updateDataList, Integer monitorpointtype) {
        for (Map<String, Object> updateMap : updateDataList) {
            updateData(updateMap, monitorpointtype);
        }
    }

    private void updateData(Map<String, Object> updateMap, Integer monitorpointtype) {
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case WasteWaterEnum:
            case RainEnum:
                waterOutputInfoMapper.setTimeDataByParam(updateMap);
                break;
            case WasteGasEnum:
            case FactoryBoundaryStinkEnum:
            case FactoryBoundarySmallStationEnum:
            case unOrganizationWasteGasEnum:
            case SmokeEnum:
                gasOutPutInfoMapper.setTimeDataByParam(updateMap);
                break;
            case EnvironmentalStinkEnum:
            case EnvironmentalVocEnum:
            case meteoEnum:
            case MicroStationEnum:
            case EnvironmentalDustEnum:
                otherMonitorPointMapper.setTimeDataByParam(updateMap);
                break;
            case AirEnum:
                airMonitorStationMapper.setTimeDataByParam(updateMap);
                break;
            case WaterQualityEnum:
                waterStationMapper.setTimeDataByParam(updateMap);
                break;
        }

    }

    /**
     * @author: xsm
     * @date: 2022/01/11 0011 下午 13:21
     * @Description:统计各类型点位的浓度及排放情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Map<String, Object>> countAllPointFlowAndConcentrationData(Map<String, Object> param) {
        Map<String, Map<String, Object>> resultInfo = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        String datetype = param.get("datetype").toString();
        String pollutantcode = param.get("pollutantcode").toString();
        Date starttime = null;
        Date endtime = null;
        List<Integer> monitorpointtypes = (List<Integer>) param.get("monitorpointtypes");
        String collection_nd = null;
        String collection_pfl = null;
        String liststr_nd = null;
        String liststr_pfl = null;
        String valuestr_pfl = null;
        if ("year".equals(datetype)) {
            if (param.get("monitortime")!=null){
                String monitortime = param.get("monitortime").toString();
                starttime = DataFormatUtil.parseDate(monitortime + "-01-01 00:00:00");
                endtime = DataFormatUtil.parseDate(monitortime + "-12-31 23:59:59");
            }else {
                starttime = DataFormatUtil.parseDate(param.get("starttime") + "-01 00:00:00");
                endtime = DataFormatUtil.parseDate(DataFormatUtil.getLastDayOfMonth(param.get("endtime")+"") + " 23:59:59");
            }
            collection_nd = "MonthData";
            collection_pfl = "MonthFlowData";
            liststr_nd = "MonthDataList";
            liststr_pfl = "MonthFlowDataList";
            valuestr_pfl = "PollutantFlow";
        } else if ("month".equals(datetype)) {
            if (param.get("monitortime")!=null){
                String monitortime = param.get("monitortime").toString();
                starttime = DataFormatUtil.parseDate(monitortime + "-01 00:00:00");
                endtime = DataFormatUtil.parseDate(monitortime + "-31 23:59:59");
            }else {
                starttime = DataFormatUtil.parseDate(param.get("starttime") + " 00:00:00");
                endtime = DataFormatUtil.parseDate(param.get("endtime") + " 23:59:59");
            }
            collection_nd = "DayData";
            collection_pfl = "DayFlowData";
            liststr_nd = "DayDataList";
            liststr_pfl = "DayFlowDataList";
            valuestr_pfl = "AvgFlow";
        } else if ("day".equals(datetype)) {
            if (param.get("monitortime")!=null){
                String monitortime = param.get("monitortime").toString();
                starttime = DataFormatUtil.parseDate(monitortime + " 00:00:00");
                endtime = DataFormatUtil.parseDate(monitortime + " 23:59:59");
            }else {
                starttime = DataFormatUtil.parseDate(param.get("starttime") + ":00:00");
                endtime = DataFormatUtil.parseDate(param.get("endtime") + ":59:59");
            }
            collection_nd = "HourData";
            collection_pfl = "HourFlowData";
            liststr_nd = "HourDataList";
            liststr_pfl = "HourFlowDataList";
            valuestr_pfl = "AvgFlow";
        }
        //根据监测类型获取点位信息
        paramMap.put("pollutantcode", pollutantcode);
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode());
        List<Map<String, Object>> pfl_outputs = getOutPutsAndPollutantsByMonitortypes(monitorpointtypes, paramMap);
        paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
        List<Map<String, Object>> nongdu_outputs = getOutPutsAndPollutantsByMonitortypes(monitorpointtypes, paramMap);
        List<String> pfl_mns = pfl_outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toList());
        List<String> nongdu_mns = nongdu_outputs.stream().map(output -> output.get("MN").toString()).collect(Collectors.toList());
        //浓度
       /* Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("mn", "$DataGatherCode");
        pollutantList.put("value", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(nongdu_mns).and("MonitorTime").gte(starttime).lte(endtime);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(liststr_nd));
        operations.add(match(Criteria.where(liststr_nd+".PollutantCode").is(pollutantcode)));
        operations.add(Aggregation.project("DataGatherCode","MonitorTime") .and(liststr_nd+".AvgStrength").as("AvgStrength")
                .and(liststr_nd+".PollutantCode").as("PollutantCode"));
        operations.add(group("PollutantCode","MonitorTime").push(pollutantList).as("pollutantList"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection_nd, Document.class);
        List<Document> documents = pageResults.getMappedResults();*/
        //排放量
        /*Map<String, Object> mn_values = new HashMap<>();
        mn_values.put("mn", "$DataGatherCode");
        mn_values.put("value", "$AvgStrength");
        Criteria criteria_pfl = new Criteria();
        criteria_pfl.and("DataGatherCode").in(pfl_mns).and("MonitorTime").gte(starttime).lte(endtime);
        List<AggregationOperation> operations_pfl = new ArrayList<>();
        operations_pfl.add(Aggregation.match(criteria_pfl));
        operations_pfl.add(unwind(liststr_pfl));
        operations_pfl.add(match(Criteria.where(liststr_pfl+".PollutantCode").is(pollutantcode)));
        operations_pfl.add(Aggregation.project("DataGatherCode","MonitorTime") .and(liststr_pfl+"."+valuestr_pfl).as("AvgStrength")
                .and(liststr_pfl+".PollutantCode").as("PollutantCode"));
        operations_pfl.add(group("PollutantCode","MonitorTime").push(mn_values).as("pollutantList"));
        operations_pfl.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        Aggregation aggregationList_pfl = Aggregation.newAggregation(operations_pfl)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults_pfl = mongoTemplate.aggregate(aggregationList_pfl, collection_pfl, Document.class);
        List<Document> documents_pfl = pageResults_pfl.getMappedResults();
        List<Map<String,Object>> nd_list = formatFlowAndConcentrationData(documents,datetype);
        List<Map<String,Object>> pfl_list = formatFlowAndConcentrationData(documents_pfl,datetype);*/
        Bson nongdu_bson = Filters.and(
                in("DataGatherCode", nongdu_mns),
                lte("MonitorTime", endtime),
                gte("MonitorTime", starttime),
                eq(liststr_nd + ".PollutantCode", pollutantcode));
        Bson pfl_bson = Filters.and(
                in("DataGatherCode", pfl_mns),
                lte("MonitorTime", endtime),
                gte("MonitorTime", starttime),
                eq(liststr_pfl + ".PollutantCode", pollutantcode));
        FindIterable<Document> nongdu_documents = mongoTemplate.getCollection(collection_nd).find(nongdu_bson);
        FindIterable<Document> pfl_documents = mongoTemplate.getCollection(collection_pfl).find(pfl_bson);
        List<Map<String, Object>> nongdudata = new ArrayList<>();  //浓度的数据
        List<Map<String, Object>> pfldata = new ArrayList<>();  //排放量的数据
        for (Document nongdu_document : nongdu_documents) {
            List<Document> DataList = nongdu_document.get(liststr_nd, new ArrayList<Document>());
            String monitorTime = "";
            if ("year".equals(datetype)) {
                monitorTime = DataFormatUtil.getDateYM((Date) nongdu_document.get("MonitorTime"));
            } else if ("month".equals(datetype)) {
                monitorTime = DataFormatUtil.getDateYMD((Date) nongdu_document.get("MonitorTime"));
            } else if ("day".equals(datetype)) {
                monitorTime = DataFormatUtil.getDateYMDH((Date) nongdu_document.get("MonitorTime"));
            }
            for (Document document : DataList) {
                if (document.get("PollutantCode").equals(pollutantcode) && document.get("AvgStrength") != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time", monitorTime);
                    map.put("value", document.get("AvgStrength"));
                    nongdudata.add(map);
                    break;
                }
            }
        }

        for (Document pfl_document : pfl_documents) {
            List<Document> MonthDataList = pfl_document.get(liststr_pfl, new ArrayList<Document>());
            String monitorTime = "";
            if ("year".equals(datetype)) {
                monitorTime = DataFormatUtil.getDateYM((Date) pfl_document.get("MonitorTime"));
            } else if ("month".equals(datetype)) {
                monitorTime = DataFormatUtil.getDateYMD((Date) pfl_document.get("MonitorTime"));
            } else if ("day".equals(datetype)) {
                monitorTime = DataFormatUtil.getDateYMDH((Date) pfl_document.get("MonitorTime"));
            }
            for (Document document : MonthDataList) {
                if (document.get("PollutantCode").equals(pollutantcode) && document.get(valuestr_pfl) != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time", monitorTime);
                    map.put("value", document.get(valuestr_pfl));
                    pfldata.add(map);
                    break;
                }
            }
        }
        Map<String, List<Map<String, Object>>> nongdu_data = nongdudata.stream().collect(Collectors.groupingBy(m -> m.get("time").toString()));
        Map<String, List<Map<String, Object>>> pfl_data = pfldata.stream().collect(Collectors.groupingBy(m -> m.get("time").toString()));
        formatDischargeAndDensityResultByKey(resultInfo, nongdu_data, "nongdu_value", datetype, false);
        formatDischargeAndDensityResultByKey(resultInfo, pfl_data, "pfl_value", datetype, false);
        Map<String, Map<String, Object>> resultMap = new LinkedHashMap<>();
        resultInfo.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(m -> resultMap.put(m.getKey(), m.getValue()));
        return resultMap;
       /* resultmap.put("nd_data",nd_list);
        resultmap.put("pfl_data",pfl_list);
        return resultmap;*/
    }

    /**
     * 组装浓度、排放量数据
     */
    private void formatDischargeAndDensityResultByKey(Map<String, Map<String, Object>> resultMap, Map<String, List<Map<String, Object>>> dataList, String
            keyName, String datetype, boolean iscount) {
        for (Map.Entry<String, List<Map<String, Object>>> entry : dataList.entrySet()) {
            String time = entry.getKey();
            double f1 = 0;
            double count = 0d;
            List<Map<String, Object>> valuelist = entry.getValue();
            for (Map<String, Object> map : valuelist) {
                if (map.get("value") != null) {
                    f1 += Double.valueOf(map.get("value").toString());
                    count += 1;
                }
            }
            if (f1 > 0) {
                if ("nongdu_value".equals(keyName)){//平均浓度
                    if (count>0) {
                        f1 = f1 / count;
                    }
                }else {
                    //kg 转化为  t
                    if ("month".equals(datetype) || "day".equals(datetype)) {
                        f1 = f1;
                    }
                }
            }
            BigDecimal bigDecimal = BigDecimal.valueOf(f1);
            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            //如果已经包含这个月份
            if (resultMap.containsKey(time)) {
                Map<String, Object> oldmap = resultMap.get(time);
                oldmap.put(keyName, value);
            } else {
                Map<String, Object> newmap = new HashMap<>();
                if (!iscount) {
                    newmap.put("nongdu_value", 0);
                    newmap.put("pfl_value", 0);
                }
                newmap.put(keyName, value);
                resultMap.put(time, newmap);
            }
        }
    }

    /*private List<Map<String,Object>> formatFlowAndConcentrationData(List<Document> documents,String datetype) {
        List<Map<String,Object>> result = new ArrayList<>();
        for (Document doc :documents){
            Map<String,Object> map = new HashMap<>();
            String time = "";
            if ("year".equals(datetype)){
                time =  DataFormatUtil.getDateYM(doc.getDate("MonitorTime"));
            }else if("month".equals(datetype)){
                time = DataFormatUtil.getDateYMD(doc.getDate("MonitorTime"));
            }else if("day".equals(datetype)){
                time = DataFormatUtil.getDateYMDH(doc.getDate("MonitorTime"));
            }
            map.put("monitortime",time);
            Double value = 0d;
            List<Document> polist = (List<Document>) doc.get("pollutantList");
            for (Document onedoc:polist){
                if (onedoc.get("value") != null && !"null".equals(onedoc.get("value").toString())&&!"".equals(onedoc.get("value").toString())) {
                    value += Double.valueOf(onedoc.get("value").toString());
                }
            }
            map.put("monitortime",time);
            map.put("value",value);
            result.add(map);
        }
        return result;
    }*/

    /**
     * 获取排口污染物信息
     */
    private List<Map<String, Object>> getOutPutsAndPollutantsByMonitortypes(List<Integer> monitorpointtypes, Map<String, Object> paramMap) {
        List<Map<String, Object>> outPuts = new ArrayList<>();
        //添加数据权限
        String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
        paramMap.put("datauserid", userid);
        for (Integer i : monitorpointtypes) {
            CommonTypeEnum.MonitorPointTypeEnum monitorEnum = CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(i);
            paramMap.put("monitortype", i);
            switch (monitorEnum) {
                case WasteWaterEnum:
                case RainEnum:
                    outPuts.addAll(waterOutputInfoMapper.getWaterOutPutPollutants(paramMap));
                    break;
                case SmokeEnum:
                case WasteGasEnum:
                    outPuts.addAll(gasOutPutInfoMapper.getGasOutPutPollutants(paramMap));
                    break;
            }
        }
        return outPuts;
    }

    /**
     * @author: xsm
     * @date: 2022/01/12 0012 上午 11:08
     * @Description:统计各类型点位近七天总超标时长排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEntLastSevenDaysOverTimeRankData(Map<String, Object> paramMap) {
        try {
            //超标  污染物算总报警时长
            //异常   单个污染物算报警时长
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> mns = (List<String>) paramMap.get("mns");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> idandname = (Map<String, Object>) paramMap.get("idandname");
            Map<String, Object> mnandpollutionid = (Map<String, Object>) paramMap.get("mnandpollutionid");
            Date startDate = (Date) paramMap.get("starttime");
            Date endDate = (Date) paramMap.get("endtime");
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            //model 表 不查 小时类型报警数据
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "LastOverTime", "FirstOverTime", "PollutantCode")
                    .and(DateOperators.DateToString.dateOf("FirstOverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$FirstOverTime");
            childmap.put("endtime", "$LastOverTime");
            childmap.put("pollutantcode", "$PollutantCode");
            operations.add(
                    Aggregation.group("MN", "MonitorTime")
                            .push(childmap).as("pollutanttimes")
            );
            operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime", "MN"));
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, overModelCollection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (listItems.size() > 0) {
                for (Document document : listItems) {
                    if (mnandpollutionid.get(document.getString("MN")) != null) {
                        document.put("pollutionid", mnandpollutionid.get(document.getString("MN")));
                    }
                }
                //按企业分组
                Map<String, List<Document>> listMap = listItems.stream().filter(m -> m != null && m.get("pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("pollutionid").toString()));
                for (Map.Entry<String, List<Document>> entry : listMap.entrySet()) {
                    List<Document> allpolist = entry.getValue();
                    //按日期分组
                    Map<String,Object> onepomao = getPointOneDayOverTime(allpolist,codeandname);
                    onepomao.put("pollutionname",idandname.get(entry.getKey()));
                    result.add(onepomao);
                }
            }
            if (result.size()>0){
                //排序
                Comparator<Object> sorted1 = Comparator.comparing(m -> Double.valueOf(((Map) m).get("total").toString())).reversed();
                result = result.stream().sorted(sorted1).collect(Collectors.toList());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 计算点位某天总的超标时段（并集）
     */
    private Map<String, Object> getPointOneDayOverTime(List<Document> allpolist,Map<String, Object> codeandname) {
        Map<String,Object> result = new HashMap<>();
        //按时间排序
        int alarmtimenum = 0;//秒数
        Map<String, List<Document>> listMapTwo = allpolist.stream().filter(m -> m != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
        Set<String> codes = new HashSet<>();
        for (Map.Entry<String, List<Document>> entrytwo : listMapTwo.entrySet()) {
            List<Document> onepolist = entrytwo.getValue();
            List<Document> pollutants = new ArrayList<>();
            Date firsttime = null;
            Date lasttime = null;
            if (onepolist.size() > 0) {
                for (Document twodoc : onepolist) {
                    pollutants.addAll((List<Document>) twodoc.get("pollutanttimes"));
                }
            }
            if (pollutants != null && pollutants.size() > 0) {
                pollutants = pollutants.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("starttime"))).collect(Collectors.toList());
                for (Document podo : pollutants) {
                    codes.add(podo.getString("pollutantcode"));
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
                                    alarmtimenum += 60;
                                } else {
                                    long timenum = (lasttime.getTime() - firsttime.getTime()) / 1000 ;
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
                //第二次报警时段不被包含于第一个报警时段中
                if (DataFormatUtil.getDateYMDHMS(firsttime).equals(DataFormatUtil.getDateYMDHMS(lasttime))){
                    alarmtimenum +=60;
                }else{
                    long timenum = (lasttime.getTime()-firsttime.getTime())/1000 ;
                    alarmtimenum +=Integer.valueOf(timenum+"");
                }
            }
        }
        String names = "";
        for (String code:codes){
            if (codeandname.get(code)!=null){
                names = names+codeandname.get(code)+"、";
            }
        }
        if (names.length()>0){
            names = names.substring(0,names.length()-1);
        }
        if (alarmtimenum>0){
            String total = "";
            if (alarmtimenum<60){
                total = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(60)/(60*60)));
            }else{
                total = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(alarmtimenum)/(60*60)));
            }
            if (Double.valueOf(total) >0) {
                result.put("total",total);
                result.put("pollutantnames",names);
            }else{
                result.put("total","0");
                result.put("pollutantnames","");
            }
        }else{
            result.put("total","0");
            result.put("pollutantnames","");
        }

        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/01/13 0013 上午 08:41
     * @Description:获取当月报警任务处置情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getLastMonthAlarmTaskDisposalByParam(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getLastMonthAlarmTaskDisposalByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/01/17 0017 上午 09:46
     * @Description:获取报警统计清单
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime,endtime [yyyy-mm-dd]
     * @return:
     */
    @Override
    public Map<String, Object> getAlarmStatisticsInventoryByParam(Map<String, Object> paramMap) {
        try {
            //超标  污染物算总报警时长
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            List<String> mns = (List<String>) paramMap.get("mns");
            Map<String, Object> codeandname = (Map<String, Object>) paramMap.get("codeandname");
            Map<String, Object> mnandpointid = (Map<String, Object>) paramMap.get("mnandpointid");
            Map<String, Object> mnandpointname = (Map<String, Object>) paramMap.get("mnandpointname");
            Map<String, Object> mnandpollutionname = (Map<String, Object>) paramMap.get("mnandpollutionname");
            Map<String, Object> mnandshortername = (Map<String, Object>) paramMap.get("mnandshortername");
            Map<String, Object> mnandtype = (Map<String, Object>) paramMap.get("mnandtype");
            Date startDate =  DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString() +" 00:00:00");
            Date endDate =  DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString() +" 23:59:59");
            Integer pageNum = 1;
            Integer pageSize = 20;
            PageEntity<Document> pageEntity = new PageEntity<>();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                pageNum = Integer.parseInt(paramMap.get("pagenum").toString());
                pageSize = Integer.parseInt(paramMap.get("pagesize").toString());
                pageEntity.setPageNum(pageNum);
                pageEntity.setPageSize(pageSize);
            }
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();
            //model 表 不查 小时类型报警数据
            criteria.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
            operations.add(Aggregation.match(criteria));
            operations.add(Aggregation.project("MN", "LastOverTime", "FirstOverTime", "PollutantCode")
                    .and(DateOperators.DateToString.dateOf("FirstOverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
            );
            Map<String, Object> childmap = new HashMap<>();
            childmap.put("starttime", "$FirstOverTime");
            childmap.put("endtime", "$LastOverTime");
            childmap.put("pollutantcode", "$PollutantCode");
            operations.add(
                    Aggregation.group("MN", "MonitorTime")
                            .push(childmap).as("pollutanttimes")
            );
            operations.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime", "MN"));
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
                operations.add(Aggregation.limit(pageEntity.getPageSize()));
            }
            Aggregation aggregationList = Aggregation.newAggregation(operations)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, overModelCollection, Document.class);
            List<Document> listItems = pageResults.getMappedResults();
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                List<AggregationOperation> operations1 = new ArrayList<>();
                Criteria criteria1 = new Criteria();
                //model 表 不查 小时类型报警数据
                criteria1.and("MN").in(mns).and("FirstOverTime").gte(startDate).lte(endDate).and("AlarmLevel").ne(0).and("DataType").ne("HourData");
                operations1.add(Aggregation.match(criteria1));
                operations1.add(Aggregation.project("MN", "LastOverTime", "FirstOverTime", "PollutantCode")
                        .and(DateOperators.DateToString.dateOf("FirstOverTime").toString("%Y-%m-%d").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MonitorTime")
                );
                operations1.add(
                        Aggregation.group("MN", "MonitorTime")
                );
                operations1.add(Aggregation.sort(Sort.Direction.DESC, "MonitorTime", "MN"));
                Aggregation aggregationList1 = Aggregation.newAggregation(operations1)
                        .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
                AggregationResults<Document> countmap = mongoTemplate.aggregate(aggregationList1, overModelCollection, Document.class);
                List<Document> countlist = countmap.getMappedResults();
                resultMap.put("total",countlist.size());
            }
            if (listItems.size() > 0) {
                for (Document document:listItems){
                    //按日期分组
                    Map<String,Object> onepomao = getOnePointOneDayOverTime(document,codeandname);
                    onepomao.put("dgimn",document.getString("MN"));
                    onepomao.put("monitortime",document.getString("MonitorTime"));
                    onepomao.put("pollutionname",mnandpollutionname.get(document.getString("MN")));
                    onepomao.put("shortername",mnandshortername.get(document.getString("MN")));
                    onepomao.put("outputname",mnandpointname.get(document.getString("MN")));
                    onepomao.put("outputid",mnandpointid.get(document.getString("MN")));
                    onepomao.put("monitorpointtype",mnandtype.get(document.getString("MN"))!=null?Integer.valueOf(mnandtype.get(document.getString("MN")).toString()):null);
                    result.add(onepomao);
                }
            }
            if (result.size()>0){
                //排序
                Comparator<Object> sorted1 = Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed();
                Comparator<Object> sorted12 = Comparator.comparing(m -> Double.valueOf(((Map) m).get("total").toString())).reversed();
                Comparator<Object> finalComparator = sorted1.thenComparing(sorted12);
                result = result.stream().sorted(finalComparator).collect(Collectors.toList());
            }
            resultMap.put("datalist",result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 计算单个点位单天总的超标时段（并集）
     */
    private Map<String, Object> getOnePointOneDayOverTime(Document document,Map<String, Object> codeandname) {
        Map<String,Object> result = new HashMap<>();
        //按时间排序
        int alarmtimenum = 0;//秒
        Date firsttime = null;
        Date lasttime = null;
        Set<String> codes = new HashSet<>();
        List<Document> pollutants =(List<Document>) document.get("pollutanttimes");
        if (pollutants != null && pollutants.size() > 0) {
                pollutants = pollutants.stream().sorted(Comparator.comparing(m -> ((Document) m).getDate("starttime"))).collect(Collectors.toList());
                for (Document podo : pollutants) {
                    codes.add(podo.getString("pollutantcode"));
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
                                    alarmtimenum += 60;//若只报警一次  开始报警时间和  报警结束时间一致  按一分钟计算
                                } else {
                                    long timenum = (lasttime.getTime() - firsttime.getTime()) / 1000;
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
                alarmtimenum +=60;
            }else{
                long timenum = (lasttime.getTime()-firsttime.getTime())/1000 ;
                alarmtimenum +=Integer.valueOf(timenum+"");
            }
            }
        String names = "";
        for (String code:codes){
            if (codeandname.get(code)!=null){
                names = names+codeandname.get(code)+"、";
            }
        }
        if (names.length()>0){
            names = names.substring(0,names.length()-1);
        }
        if (alarmtimenum>0){
            String total = "";
            if (alarmtimenum<60){
                //小于一分钟  按一分钟计算
                total = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(60)/(60*60)));
            }else{
                total = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(alarmtimenum)/(60*60)));
            }
            if (Double.valueOf(total) >0) {
                result.put("total",Double.valueOf(total));
                result.put("pollutantnames",names);
            }else{
                result.put("total",0);
                result.put("pollutantnames","");
            }
        }else{
            result.put("total",0);
            result.put("pollutantnames","");
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTableTitleForAlarmStatisticsInventory() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", "pollutionname");
            map.put("label", "企业名称");
            map.put("align", "center");
            tableTitleData.add(map);
        Map<String, Object> map2 = new HashMap<>();
            map2.put("minwidth", "180px");
            map2.put("headeralign", "center");
            map2.put("fixed", "left");
            map2.put("showhide", true);
            map2.put("prop", "outputname");
            map2.put("label", "排口名称");
            map2.put("align", "center");
            tableTitleData.add(map2);
        Map<String, Object> map3 = new HashMap<>();
            map3.put("minwidth", "180px");
            map3.put("headeralign", "center");
            map3.put("fixed", "left");
            map3.put("showhide", true);
            map3.put("prop", "monitortime");
            map3.put("label", "报警日期");
            map3.put("align", "center");
            tableTitleData.add(map3);
        Map<String, Object> map4 = new HashMap<>();
            map4.put("minwidth", "180px");
            map4.put("headeralign", "center");
            map4.put("fixed", "left");
            map4.put("showhide", true);
            map4.put("prop", "pollutantnames");
            map4.put("label", "报警污染物");
            map4.put("align", "center");
            tableTitleData.add(map4);
        Map<String, Object> map5 = new HashMap<>();
            map5.put("minwidth", "180px");
            map5.put("headeralign", "center");
            map5.put("fixed", "left");
            map5.put("showhide", true);
            map5.put("prop", "total");
            map5.put("label", "报警时长(小时)");
            map5.put("align", "center");
            tableTitleData.add(map5);
        return tableTitleData;
    }
}
