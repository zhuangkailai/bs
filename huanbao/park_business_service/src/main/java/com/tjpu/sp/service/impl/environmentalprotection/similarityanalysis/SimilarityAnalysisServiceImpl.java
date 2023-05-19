package com.tjpu.sp.service.impl.environmentalprotection.similarityanalysis;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.service.environmentalprotection.similarityanalysis.SimilarityAnalysisService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundarySmallStationEnum;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class SimilarityAnalysisServiceImpl implements SimilarityAnalysisService {

    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private PollutionMapper pollutionMapper;



    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: xsm
     * @date: 2019/7/19 0019 下午 2:59
     * @Description:获取所有恶臭监测点信息（包括厂界恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getAllStenchMonitorPointInfo() {
        //获取所有恶臭的点位信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("enttypecode", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        paramMap.put("othertypecode", EnvironmentalStinkEnum.getCode());
        List<Map<String, Object>> alllist = otherMonitorPointMapper.getAllStenchMonitorPointInfoByParamMap(paramMap);
        return alllist;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 8:43
     * @Description:根据监测点类型获取对应类型的重要污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getAllKeyPollutantsByTypes(List<String> monitorpointtypes) {
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("pollutanttypes", monitorpointtypes);
        List<Map<String, Object>> allpollutants = pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(parammap);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Set set = new HashSet();
        if (allpollutants != null && allpollutants.size() > 0) {
            for (Map<String, Object> objmap : allpollutants) {
                if (set.contains(objmap.get("Code"))) {//判断是否污染物编码重复
                    continue;//重复
                } else {//不重复
                    set.add(objmap.get("Code"));
                    result.add(objmap);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 上午 11:09
     * @Description:根据自定义参数获取单个监测点监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getOnePointMonitorDataByParamMap(Map<String, Object> paramMap) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        String collection = paramMap.get("collection").toString();
        String mn = paramMap.get("dgimn")!=null?paramMap.get("dgimn").toString():"";
        String pollutantcode = paramMap.get("pollutantcode").toString();//污染物编码
        List<String> codes = new ArrayList<>();
        boolean queryall = false;
        String voccode  = DataFormatUtil.parseProperties("pollutant.voccode");
        //污染物信息
        if (paramMap.get("monitorpointtype")!=null&&(Integer.parseInt(paramMap.get("monitorpointtype").toString())== EnvironmentalVocEnum.getCode())){
            if (pollutantcode.equals(voccode)){//若类型为VOC且查询因子为voc因子  则查该VOC点位监测的全部因子数据
                queryall = true;
                Map<String,Object> param = new HashMap<>();
                param.put("dgimn",mn);
                param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                List<Map<String,Object>> pollutants = otherMonitorPointMapper.getAllVocPollutantByParam(param);
                    if (pollutants!=null&&pollutants.size()>0){
                        for (Map<String,Object> map:pollutants){
                            if (map.get("pollutantcode")!=null){
                                codes.add(map.get("pollutantcode").toString());
                            }
                        }
                    }
            }else{
                codes.add(pollutantcode);
            }
        }else{
            codes.add(pollutantcode);
        }
        //点位名称
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        Map<String,Object> mnandname = new HashMap<>();
        if (monitorpointlist!=null&&monitorpointlist.size()>0){
            for (Map<String,Object> map:monitorpointlist){
                if (map.get("DGIMN")!=null&&map.get("MonitorPointName")!=null) {
                    mnandname.put(map.get("DGIMN").toString(), map.get("MonitorPointName"));
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Date startDate = null;
        Date endDate = null;
        String datalistname = "";
        if ("MinuteData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString()+":00");
            endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString()+":59");
            datalistname = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMDH(paramMap.get("starttime").toString()+":00:00");
            endDate = DataFormatUtil.getDateYMDH(paramMap.get("endtime").toString()+":59:59");
            datalistname = "HourDataList";
        } else if ("DayData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMD(paramMap.get("starttime").toString()+" 00:00:00");
            endDate = DataFormatUtil.getDateYMD(paramMap.get("endtime").toString()+" 23:59:59");
            datalistname = "DayDataList";
        }
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("monitortime", "$MonitorTime");
        pollutantList.put("pollutantcode", "$PollutantCode");
        pollutantList.put("monitorvalue", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").is(mn).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(datalistname));
        operations.add(match(Criteria.where(datalistname+".PollutantCode").in(codes)));
        operations.add(Aggregation.project("DataGatherCode","MonitorTime") .and(datalistname+".AvgStrength").as("AvgStrength")
                .and(datalistname+".PollutantCode").as("PollutantCode"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        operations.add(group("DataGatherCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
        if (queryall == false) {
            if (mappedResults.size() > 0 && mappedResults.get(0).get("pollutantList") != null) {//判断查询数据是否为空
                List<Document> documents = (List<Document>) mappedResults.get(0).get("pollutantList");
                for (Document document : documents) {
                    String monitorDate = "";
                    if ("MinuteData".equals(collection)) {
                        monitorDate = DataFormatUtil.getDateYMDHM(document.getDate("monitortime"));
                    } else if ("HourData".equals(collection)) {
                        monitorDate = DataFormatUtil.getDateYMDH(document.getDate("monitortime"));
                    } else if ("DayData".equals(collection)) {
                        monitorDate = DataFormatUtil.getDateYMD(document.getDate("monitortime"));
                    }
                    Map<String, Object> resultmap = new HashMap<>();
                    resultmap.put("monitortime", monitorDate);
                    resultmap.put("pollutantcode", document.get("pollutantcode"));
                    resultmap.put("monitorpointname", mnandname.get(mn));
                    resultmap.put("monitorvalue", document.get("monitorvalue"));
                    result.add(resultmap);
                }
            }
        }else{
            if (mappedResults.size() > 0 && mappedResults.get(0).get("pollutantList") != null) {//判断查询数据是否为空
                List<Document> documents = (List<Document>) mappedResults.get(0).get("pollutantList");
                Map<String, List<Document>> mapDocuments = new HashMap<>();
                if (documents.size()>0){
                    if ("MinuteData".equals(collection)) {
                        mapDocuments =  documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("monitortime"))));
                    } else if ("HourData".equals(collection)) {
                        mapDocuments =  documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDH(m.getDate("monitortime"))));
                    } else if ("DayData".equals(collection)) {
                        mapDocuments =  documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMD(m.getDate("monitortime"))));
                    }
                    if (mapDocuments!=null&&mapDocuments.size()>0) {
                        for (String key : mapDocuments.keySet()) {
                            Map<String, Object> resultmap = new HashMap<>();
                            resultmap.put("monitortime", key);
                            List<Document>  onetimedata =  mapDocuments.get(key);
                            String totalvalue ="";
                            if (onetimedata!=null&&onetimedata.size()>0){
                                for (Document document1 : onetimedata) {//计算该时间点 VOC污染物浓度值之和
                                    if (!"".equals(totalvalue)) {
                                        totalvalue = (Float.valueOf(totalvalue) + ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d)) + "";
                                    } else {
                                        totalvalue = ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d) + "";
                                    }
                                }
                            }
                            if (!"".equals(totalvalue)){
                                totalvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(totalvalue)));
                            }
                            resultmap.put("pollutantcode", voccode);
                            resultmap.put("monitorpointname", mnandname.get(mn));
                            resultmap.put("monitorvalue", totalvalue);
                            result.add(resultmap);
                        }
                    }
                }
            }
        }
        if (result!=null&&result.size()>0){
            //根据时间排序
            result = result.stream().sorted(Comparator.comparing((Map m) -> (m.get("monitortime").toString())).reversed()).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 1:43
     * @Description:根据自定义参数获取多个监测点的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getMorePointMonitorDataByParamMap(Map<String, Object> paramMap) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        String collection = paramMap.get("collection").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();//污染物编码
        List<String> mnlist = (List<String>) paramMap.get("dgimnlist");
        List<String> codes = new ArrayList<>();
        boolean queryall = false;
        String voccode  = DataFormatUtil.parseProperties("pollutant.voccode");
        //污染物信息
        Map<String,Set<String>> mnandcodelist = new HashMap<>();
        if (paramMap.get("monitorpointtype")!=null&&(Integer.parseInt(paramMap.get("monitorpointtype").toString())== EnvironmentalVocEnum.getCode())){
            if (pollutantcode.equals(voccode)){//若类型为VOC且查询因子为voc因子  则查该VOC点位监测的全部因子数据
                queryall = true;
                Map<String,Object> param = new HashMap<>();
                param.put("dgimns",mnlist);
                param.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
                List<Map<String,Object>> pollutants = otherMonitorPointMapper.getAllVocPollutantByParam(param);
                if (pollutants!=null&&pollutants.size()>0){
                    for (Map<String,Object> map:pollutants){
                        if (map.get("pollutantcode")!=null){
                            String vocmn = map.get("Dgimn").toString();
                            codes.add(map.get("pollutantcode").toString());
                            if (mnandcodelist.get(vocmn)!=null){
                                mnandcodelist.get(vocmn).add(map.get("pollutantcode").toString());
                            }else{
                                Set<String> pocodes = new HashSet<>();
                                pocodes.add(map.get("pollutantcode").toString());
                                mnandcodelist.put(vocmn,pocodes);
                            }
                        }
                    }
                }
            }else{
                codes.add(pollutantcode);
            }
        }else{
            codes.add(pollutantcode);
        }
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        Map<String,Object> mnandname = new HashMap<>();
        Map<String,Object> mnandponame = new HashMap<>();
        if (monitorpointlist!=null&&monitorpointlist.size()>0){
            for (Map<String,Object> map:monitorpointlist){
                if (map.get("DGIMN")!=null&&map.get("MonitorPointName")!=null) {
                    mnandname.put(map.get("DGIMN").toString(), map.get("MonitorPointName"));
                    mnandponame.put(map.get("DGIMN").toString(), map.get("pollutionname"));
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Date startDate = null;
        Date endDate = null;
        String datalistname = "";
        if ("MinuteData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString()+":00");
            endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString()+":59");
            datalistname = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMDH(paramMap.get("starttime").toString()+":00:00");
            endDate = DataFormatUtil.getDateYMDH(paramMap.get("endtime").toString()+":59:59");
            datalistname = "HourDataList";
        } else if ("DayData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMD(paramMap.get("starttime").toString()+" 00:00:00");
            endDate = DataFormatUtil.getDateYMD(paramMap.get("endtime").toString()+" 23:59:59");
            datalistname = "DayDataList";
        }
        Map<String, Object> pollutantList = new HashMap<>();
        pollutantList.put("monitortime", "$MonitorTime");
        pollutantList.put("pollutantcode", "$PollutantCode");
        pollutantList.put("monitorvalue", "$AvgStrength");
        Criteria criteria = new Criteria();
        criteria.and("DataGatherCode").in(mnlist).and("MonitorTime").gte(startDate).lte(endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(criteria));
        operations.add(unwind(datalistname));
        operations.add(match(Criteria.where(datalistname+".PollutantCode").in(codes)));
        operations.add(Aggregation.project("DataGatherCode","MonitorTime") .and(datalistname+".AvgStrength").as("AvgStrength")
                .and(datalistname+".PollutantCode").as("PollutantCode"));
        operations.add(Aggregation.sort(Sort.Direction.ASC, "MonitorTime"));
        operations.add(group("DataGatherCode").push(pollutantList).as("pollutantList"));
        Aggregation aggregationList = Aggregation.newAggregation(operations)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregationList, collection, Document.class);
        List<Document> mappedResults = pageResults.getMappedResults();
            if (mappedResults.size() > 0) {//判断查询数据是否为空
                if (queryall == false) {
                for (String mn : mnlist) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dgimn", mn);
                    List<Map<String, Object>> listmap = new ArrayList<>();
                    for (Document document : mappedResults) {
                        if (mn.equals(document.getString("_id"))) {
                            List<Document> documents = (List<Document>) document.get("pollutantList");
                            if (documents != null && documents.size() > 0) {
                                for (Document obj : documents) {
                                    String monitorDate = "";
                                    if ("MinuteData".equals(collection)) {
                                        monitorDate = DataFormatUtil.getDateYMDHM(obj.getDate("monitortime"));
                                    } else if ("HourData".equals(collection)) {
                                        monitorDate = DataFormatUtil.getDateYMDH(obj.getDate("monitortime"));
                                    } else if ("DayData".equals(collection)) {
                                        monitorDate = DataFormatUtil.getDateYMD(obj.getDate("monitortime"));
                                    }
                                    Map<String, Object> resultmap = new HashMap<>();
                                    resultmap.put("pollutionname", mnandponame.get(mn));
                                    resultmap.put("monitortime", monitorDate);
                                    resultmap.put("pollutantcode", pollutantcode);
                                    resultmap.put("monitorpointname", mnandname.get(mn));
                                    resultmap.put("dgmin", mn);
                                    resultmap.put("monitorvalue", obj.get("monitorvalue"));
                                    listmap.add(resultmap);
                                }
                            }
                        }
                    }
                    map.put("monitordatalist", listmap);
                    result.add(map);
                }
                }else{
                    for (String mn : mnlist) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("dgimn", mn);
                        List<Map<String, Object>> listmap = new ArrayList<>();
                        for (Document document : mappedResults) {
                            if (mn.equals(document.getString("_id"))) {
                                List<Document> documents = (List<Document>) document.get("pollutantList");
                                Map<String, List<Document>> mapDocuments = new HashMap<>();
                                if (documents.size()>0){
                                    if ("MinuteData".equals(collection)) {
                                        mapDocuments =  documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDHM(m.getDate("monitortime"))));
                                    } else if ("HourData".equals(collection)) {
                                        mapDocuments =  documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMDH(m.getDate("monitortime"))));
                                    } else if ("DayData".equals(collection)) {
                                        mapDocuments =  documents.stream().collect(Collectors.groupingBy(m -> DataFormatUtil.getDateYMD(m.getDate("monitortime"))));
                                    }
                                    if (mapDocuments!=null&&mapDocuments.size()>0) {
                                        for (String key : mapDocuments.keySet()) {
                                            Map<String, Object> resultmap = new HashMap<>();
                                            resultmap.put("monitortime", key);
                                            List<Document>  onetimedata =  mapDocuments.get(key);
                                            String totalvalue ="";
                                            if (onetimedata!=null&&onetimedata.size()>0){
                                                for (Document document1 : onetimedata) {//计算该时间点 VOC污染物浓度值之和
                                                    if (!"".equals(totalvalue)) {
                                                        totalvalue = (Float.valueOf(totalvalue) + ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d)) + "";
                                                    } else {
                                                        totalvalue = ((document1.get("monitorvalue") != null && !"".equals(document1.get("monitorvalue").toString())) ? Float.valueOf(document1.get("monitorvalue").toString()) : 0d) + "";
                                                    }
                                                }
                                            }
                                            if (!"".equals(totalvalue)){
                                                totalvalue = DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(Double.valueOf(totalvalue)));
                                            }
                                            resultmap.put("dgmin", mn);
                                            resultmap.put("pollutionname", mnandponame.get(mn));
                                            resultmap.put("pollutantcode", voccode);
                                            resultmap.put("monitorpointname", mnandname.get(mn));
                                            resultmap.put("monitorvalue", totalvalue);
                                            listmap.add(resultmap);
                                        }
                                    }
                                }
                            }
                        }
                        if (listmap!=null&&listmap.size()>0){
                            //根据时间排序
                            listmap = listmap.stream().sorted(Comparator.comparing((Map m) -> (m.get("monitortime").toString())).reversed()).collect(Collectors.toList());
                        }
                        map.put("monitordatalist", listmap);
                        result.add(map);
                    }
                }
            }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 8:49
     * @Description:获取所有企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getSelectPollutionInfo() {
        List<Map<String, Object>> listdata = pollutionMapper.getSelectPollutionInfo(new HashMap<>());
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        if (listdata != null && listdata.size() > 0) {
            for (Map<String, Object> map : listdata) {
                boolean flag = set.contains(map.get("Pollutionid").toString());
                if (flag == false) {//没有重复
                    Map<String, Object> objmap = new HashMap<String, Object>();
                    objmap.put("labelname", map.get("ShorterName"));
                    objmap.put("pollutionid", map.get("Pollutionid"));
                    result.add(objmap);
                    set.add(map.get("Pollutionid").toString());
                } else {
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 9:48
     * @Description:获取监测点类型为废气的重点污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getGsaOutPutKeyPollutants() {
        Map<String, Object> paramMap = new HashMap<>();
        List<String> list = new ArrayList<>();
        //获取废气两种类型的重点污染物
        list.add(String.valueOf(CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()));//废气
        paramMap.put("pollutanttypes", list);
        List<Map<String, Object>> result = pollutantFactorMapper.getAllKeyPollutantsByMonitorPointTypes(paramMap);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 上午 10:39
     * @Description:根据自定义参数获取多个排口的监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getMoreOutputMonitorDataByParamMap(Map<String, Object> paramMap) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        String collection = paramMap.get("collection").toString();
        String pollutantcode = paramMap.get("pollutantcode").toString();//污染物编码
        List<String> pollutionids = (List<String>) paramMap.get("pollutionids");
        //根据污染源ID获取污染源下在线废气排口的MN号
        Map<String, Object> param = new HashMap<>();
        param.put("pollutions", pollutionids);
        List<Map<String, Object>> listdata = pollutionMapper.getSelectPollutionInfo(param);
        Set<String> mnlist = new HashSet<>();
        for (Map<String, Object> map : listdata) {
            mnlist.add(map.get("DGIMN").toString());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        String datalistname = "";
        if ("MinuteData".equals(collection)) {
            datalistname = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            datalistname = "HourDataList";
        } else if ("DayData".equals(collection)) {
            datalistname = "DayDataList";
        }
        Query query = getMongoQuerys(collection, pollutantcode, mnlist, paramMap);
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        Map<String, Object> resultmap = new HashMap<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                //MN号
                String mn = document.getString("DataGatherCode");
                String monitorpointname = "";
                String pollutionname = "";
                for (Map<String, Object> mnmap : listdata) {
                    if (mn.equals(mnmap.get("DGIMN").toString())) {
                        monitorpointname = mnmap.get("OutPutName").toString();
                        pollutionname = mnmap.get("ShorterName").toString();
                        break;
                    }
                }
                //boolean flag = false;
                String monitorDate = "";
                if ("MinuteData".equals(collection)) {
                    monitorDate = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
                } else if ("HourData".equals(collection)) {
                    monitorDate = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                } else if ("DayData".equals(collection)) {
                    monitorDate = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                }
                Object value = "";
                if (mn.equals(document.getString("DataGatherCode"))) {//MN号相同
                    List<Map<String, Object>> pollutantDataList = document.get(datalistname, List.class);
                    for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                        if (pollutantcode.equals(dataMap.get("PollutantCode"))) {
                            value = dataMap.get("AvgStrength");
                            break;
                        }
                    }
                }
                //判断map中是否有该mn的数据
                boolean flag = resultmap.containsKey(mn);
                if (flag == true) {//有
                    List<Map<String, Object>> thelist = (List<Map<String, Object>>) resultmap.get(mn);
                    if (value!=null&&!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitorDate);
                        objmap.put("pollutantcode", pollutantcode);
                        objmap.put("monitorpointname", monitorpointname);
                        objmap.put("pollutionname", pollutionname);
                        objmap.put("dgmin", mn);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                    }
                } else {//无
                    List<Map<String, Object>> thelist = new ArrayList<>();
                    if (value!=null&&!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitorDate);
                        objmap.put("pollutantcode", pollutantcode);
                        objmap.put("monitorpointname", monitorpointname);
                        objmap.put("pollutionname", pollutionname);
                        objmap.put("dgmin", mn);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                    }
                    resultmap.put(mn, thelist);
                }
            }
        }
        for (Map.Entry<String, Object> entry : resultmap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("dgimn", entry.getKey());
            map.put("monitordatalist", entry.getValue());
            result.add(map);
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/7/23 0023 下午 7:57
     * @Description: 获取MongoDB查询条件query
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private Query getMongoQuerys(String collection, String pollutantcode, Set<String> mnlist, Map<String, Object> paramMap) {
        Query query = new Query();
        Date startDate = null;
        Date endDate = null;
        String datalistname = "";
        if ("MinuteData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMDHM(paramMap.get("starttime").toString());
            endDate = DataFormatUtil.getDateYMDHM(paramMap.get("endtime").toString());
            datalistname = "MinuteDataList";
        } else if ("HourData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMDH(paramMap.get("starttime").toString());
            endDate = DataFormatUtil.getDateYMDH(paramMap.get("endtime").toString());
            datalistname = "HourDataList";
        } else if ("DayData".equals(collection)) {
            startDate = DataFormatUtil.getDateYMD(paramMap.get("starttime").toString());
            endDate = DataFormatUtil.getDateYMD(paramMap.get("endtime").toString());
            datalistname = "DayDataList";
        }
        //构建Mongdb查询条件
        query.addCriteria(Criteria.where("DataGatherCode").in(mnlist));
        query.addCriteria(Criteria.where(datalistname + ".PollutantCode").is(pollutantcode));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        return query;
    }

    /**
     * @author: xsm
     * @date: 2019/7/22 0022 下午 5:58
     * @Description:根据自定义参数获取各监测点相似度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getMonitorPointSimilarityByParamMap(List<Map<String, Object>> listdatas, List<Map<String, Object>> comparelistdatas, Map<String, Object> paramMap) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        //String collection = paramMap.get("collection").toString();
        List<Map<String, Object>> monitorpointlist = otherMonitorPointMapper.getMonitorPointInfoByMns(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();

        //构建Mongdb查询条件
        if (comparelistdatas.size() > 0) {//判断查询数据是否为空
            for (Map<String, Object> mnmap : comparelistdatas) {
                String mn = mnmap.get("dgimn").toString();
                String monitorpointname = "";
                String pollutionname = "";
                for (Map<String, Object> map : monitorpointlist) {
                    if (mn.equals(map.get("DGIMN").toString())) {
                        monitorpointname = map.get("MonitorPointName").toString();
                        if (map.get("pollutionname") != null) {
                            pollutionname = map.get("pollutionname").toString();
                        }
                        break;
                    }
                }
                List<Map<String, Object>> thelist = (List<Map<String, Object>>) mnmap.get("monitordatalist");
                List<Double> listone = new ArrayList<>();
                List<Double> listtwo = new ArrayList<>();
                for (Map<String, Object> objmap : thelist) {
                    String monitortime = objmap.get("monitortime").toString();
                    for (Map<String, Object> objmap2 : listdatas) {
                        if (monitortime.equals(objmap2.get("monitortime").toString())) {
                            listone.add(Double.parseDouble(objmap2.get("monitorvalue").toString()));
                            listtwo.add(Double.parseDouble(objmap.get("monitorvalue").toString()));
                            break;
                        }
                    }
                }
                //两组要对比的数据都不能为空
                if (listone != null && listone.size() > 0 && listtwo != null && listtwo.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pollutionname", pollutionname);
                    map.put("monitorpointname", monitorpointname);
                    map.put("dgimn", mn);
                    //获取点位相似度
                    // listone  左侧点位数据    listtwo：右侧点位数据
                    Double value = DataFormatUtil.getRelationPercent(listone, listtwo);
                    map.put("similarity", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(value * 100)));
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/23 0023 下午 1:06
     * @Description:根据自定义参数获取各排口相似度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getOutputSimilarityByParamMap(List<Map<String, Object>> listdatas, List<Map<String, Object>> comparelistdatas, Map<String, Object> paramMap) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        //String collection = paramMap.get("collection").toString();
        List<String> pollutionids = (List<String>) paramMap.get("pollutionids");
        //根据污染源ID获取污染源下在线排口的MN号
        Map<String, Object> param = new HashMap<>();
        param.put("pollutions", pollutionids);
        List<Map<String, Object>> listdata = pollutionMapper.getSelectPollutionInfo(param);
        List<Map<String, Object>> result = new ArrayList<>();
        //构建Mongdb查询条件
        if (comparelistdatas.size() > 0) {//判断查询数据是否为空
            for (Map<String, Object> mnmap : comparelistdatas) {
                String mn = mnmap.get("dgimn").toString();
                String monitorpointname = "";
                String pollutionname = "";
                for (Map<String, Object> map : listdata) {
                    if (mn.equals(map.get("DGIMN").toString())) {
                        monitorpointname = map.get("OutPutName").toString();
                        pollutionname = map.get("PollutionName").toString();
                        break;
                    }
                }
                List<Map<String, Object>> thelist = (List<Map<String, Object>>) mnmap.get("monitordatalist");
                List<Double> listone = new ArrayList<>();
                List<Double> listtwo = new ArrayList<>();
                for (Map<String, Object> objmap : thelist) {
                    String monitortime = objmap.get("monitortime").toString();
                    for (Map<String, Object> objmap2 : listdatas) {
                        if (monitortime.equals(objmap2.get("monitortime").toString())) {
                            listone.add(Double.parseDouble(objmap2.get("monitorvalue").toString()));
                            listtwo.add(Double.parseDouble(objmap.get("monitorvalue").toString()));
                            break;
                        }
                    }
                }
                //两组要对比的数据都不能为空
                if (listone != null && listone.size() > 0 && listtwo != null && listtwo.size() > 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("pollutionname", pollutionname);
                    map.put("monitorpointname", monitorpointname);
                    map.put("dgimn", mn);
                    //获取点位相似度
                    // listone  左侧点位数据    listtwo：右侧点位数据
                    Double value = DataFormatUtil.getRelationPercent(listone, listtwo);
                    map.put("similarity", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(value * 100)));
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/08/25 0025 下午 1:09
     * @Description:根据自定义参数获取统计恶臭点位相似度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getMorePointMonitorSimilarityDataByParamMap(Map<String, Object> paramMap, List<Map<String, Object>> otherMonitorPoint) {
        //根据污染物编码和监测点类型去获取对应的污染物信息
        List<Map<String, Object>> result = new ArrayList<>();
        String datetype = paramMap.get("datetype").toString();
        List<String> pollutantcodes = (List<String>)paramMap.get("pollutantcodes");//污染物编码
        List<String> mnlist = new ArrayList<>();
        String datalistname = "";
        String collection = "";
        Date startDate = null;
        Date endDate = null;
        if ("minute".equals(datetype)) {
            startDate = DataFormatUtil.getDateYMDHM(paramMap.get("starttime").toString());
            endDate = DataFormatUtil.getDateYMDHM(paramMap.get("endtime").toString());
            datalistname = "MinuteDataList";
            collection = "MinuteData";
        } else if ("hour".equals(datetype)) {
            startDate = DataFormatUtil.getDateYMDH(paramMap.get("starttime").toString());
            endDate = DataFormatUtil.getDateYMDH(paramMap.get("endtime").toString());
            datalistname = "HourDataList";
            collection = "HourData";
        }

        List<String> stinklist1 = new ArrayList<>();
        List<String> stinklist2 = new ArrayList<>();
        List<String> stinklist3 = new ArrayList<>();
        Map<String, String> codeandname = new HashMap<>();
        Map<String, String> mnandname = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("pollutanttypes",Arrays.asList(EnvironmentalStinkEnum.getCode()
                , FactoryBoundarySmallStationEnum.getCode()));
        param.put("codes",pollutantcodes);
        List<Map<String,Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(param);

        for (Map<String, Object> map:pollutants) {
            codeandname.put(map.get("code").toString(),map.get("name").toString());
        }
       for (Map<String, Object> map:otherMonitorPoint) {
           if (map.get("DGIMN") != null) {
               mnlist.add(map.get("DGIMN").toString());
              mnandname.put(map.get("DGIMN").toString(),map.get("MonitorPointName").toString());
               /*mnandpkid.put(map.get("DGIMN").toString(),map.get("pkid").toString());
               mnandtype.put(map.get("DGIMN").toString(),map.get("FK_MonitorPointTypeCode").toString());*/
               if (map.get("MonitorPointCategory")!=null){
                   if ("1".equals(map.get("MonitorPointCategory").toString())){
                       stinklist1.add(map.get("DGIMN").toString());
                   }else if ("2".equals(map.get("MonitorPointCategory").toString())){
                       stinklist2.add(map.get("DGIMN").toString());
                   }else if ("3".equals(map.get("MonitorPointCategory").toString())){
                       stinklist3.add(map.get("DGIMN").toString());
                   }
               }
           }
       }
        param.clear();
        param.put("pointlist",otherMonitorPoint);
        param.put("codeandname",codeandname);
        param.put("mnandname",mnandname);
        Map<String, Object> timeandvalue = new HashMap<>();
        timeandvalue.put("value", "$AvgStrength");
        timeandvalue.put("time", "$theDate");
        Aggregation aggregation = newAggregation(
                match(Criteria.where("DataGatherCode").in(mnlist)
                        .and("MonitorTime").gte(startDate).lte(endDate)
                        .and(datalistname+".PollutantCode").in(pollutantcodes)),
                unwind(datalistname),
                match(Criteria.where(datalistname+".PollutantCode").in(pollutantcodes)),
                project("MonitorTime", "DataGatherCode").andExpression("add(MonitorTime,8 * 3600000)").as("date8")
                        .and(datalistname+".AvgStrength").as("AvgStrength")
                        .and(datalistname+".PollutantCode").as("PollutantCode"),
                project("date8", "DataGatherCode","AvgStrength","PollutantCode").andExpression("substr(date8,0,19)").as("theDate"),
                        group("DataGatherCode","PollutantCode")
                        .push(timeandvalue).as("timevalue")

        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> documents = aggregationResults.getMappedResults();
        if (documents.size()>0) {
            if (stinklist1.size() > 0 && stinklist2.size() > 0) {
                result.addAll(countStinkPointSimilarityData(stinklist1,stinklist2,documents,pollutantcodes,param));
            }
            if (stinklist1.size() > 0 && stinklist3.size() > 0) {
                result.addAll(countStinkPointSimilarityData(stinklist1,stinklist3,documents,pollutantcodes,param));
            }
            if (stinklist2.size() > 0 && stinklist3.size() > 0) {
                result.addAll(countStinkPointSimilarityData(stinklist2,stinklist3,documents,pollutantcodes,param));
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getWaterOutputPointInfo(HashMap<String, Object> paramMap) {
        List<Map<String, Object>> alllist = otherMonitorPointMapper.getWaterOutputPointInfo(paramMap);
        return alllist;
    }


    private List<Map<String,Object>> countStinkPointSimilarityData(List<String> stinklist1, List<String> stinklist2, List<Document> documents, List<String> pollutantcodes, Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> otherMonitorPoint = (List<Map<String, Object>>) param.get("pointlist");
        //通过Mn分组数据
        Map<String, List<Map<String, Object>>> listMap = otherMonitorPoint.stream().collect(Collectors.groupingBy(m -> m.get("DGIMN").toString()));
        Map<String, String> codeandname = (Map<String, String>) param.get("codeandname");
        Map<String, String> mnandname = (Map<String, String>) param.get("mnandname");
        for (String code : pollutantcodes) {
            for (String mnone : stinklist1) {
                List<Map<String, Object>> onelist = new ArrayList<>();
                for (Document doc : documents) {
                    if (mnone.equals(doc.getString("DataGatherCode")) && code.equals(doc.getString("PollutantCode")) && doc.get("timevalue") != null) {
                        onelist = (List<Map<String, Object>>) doc.get("timevalue");
                        break;
                    }
                }
                for (String mntwo : stinklist2) {
                    List<Map<String, Object>> twolist = new ArrayList<>();
                    for (Document doc : documents) {
                        if (mntwo.equals(doc.getString("DataGatherCode"))&& code.equals(doc.getString("PollutantCode")) && doc.get("timevalue") != null) {
                            twolist = (List<Map<String, Object>>) doc.get("timevalue");
                            break;
                        }
                    }
                    List<Double> listone = new ArrayList<>();
                    List<Double> listtwo = new ArrayList<>();
                    for (Map<String, Object> objmap : onelist) {
                        String monitortime = objmap.get("time").toString();
                        for (Map<String, Object> objmap2 : twolist) {
                            if (monitortime.equals(objmap2.get("time").toString())) {
                                if (objmap.get("value")!=null&&objmap2.get("value")!=null) {
                                    listone.add(Double.parseDouble(objmap.get("value").toString()));
                                    listtwo.add(Double.parseDouble(objmap2.get("value").toString()));
                                    break;
                                }
                            }
                        }
                    }
                    //两组要对比的数据都不能为空
                     if (listone != null && listone.size() > 0 && listtwo != null && listtwo.size() > 0) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("monitorpointname", mnandname.get(mnone) + "与" + mnandname.get(mntwo));
                        map.put("previouspoint", listMap.get(mnone)!=null?listMap.get(mnone).get(0):null);
                        map.put("afterpoint",listMap.get(mntwo)!=null?listMap.get(mntwo).get(0):null);
                        map.put("dgimn", Arrays.asList(mnone, mntwo));
                        map.put("pollutantcode", code);
                        map.put("pollutantname", codeandname.get(code));
                        //获取点位相似度
                        // listone  左侧点位数据    listtwo：右侧点位数据
                        Double value = DataFormatUtil.getRelationPercent(listone, listtwo);
                        map.put("similarity", DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveOne(value * 100)));
                        if (value*100>50) {
                            result.add(map);
                        }
                    }
                }
            }
        }
        return result;
    }
}
