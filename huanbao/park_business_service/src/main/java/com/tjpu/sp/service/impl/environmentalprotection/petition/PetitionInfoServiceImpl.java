package com.tjpu.sp.service.impl.environmentalprotection.petition;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.AirMonitorStationMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointPollutantSetMapper;
import com.tjpu.sp.dao.environmentalprotection.petition.PetitionInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.tracesource.TaskFlowRecordInfoMapper;
import com.tjpu.sp.model.environmentalprotection.petition.PetitionInfoVO;
import com.tjpu.sp.service.environmentalprotection.petition.PetitionInfoService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Service
@Transactional
public class PetitionInfoServiceImpl implements PetitionInfoService {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Autowired
    private PetitionInfoMapper petitionInfoMapper;
    @Autowired
    private OtherMonitorPointPollutantSetMapper otherMonitorPointPollutantSetMapper;
    @Autowired
    private AirMonitorStationMapper airMonitorStationMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private TaskFlowRecordInfoMapper taskFlowRecordInfoMapper;

    //实时数据
    private final String realTimeCollection = "RealTimeData";

    @Override
    public int insert(PetitionInfoVO record) {
        return petitionInfoMapper.insert(record);
    }

    @Override
    public int updateByPrimaryKey(PetitionInfoVO record) {
        return petitionInfoMapper.updateByPrimaryKey(record);
    }

    @Override
    public int deleteByPrimaryKey(String pkId) {
        taskFlowRecordInfoMapper.deleteByTaskid(pkId);
        return petitionInfoMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public PetitionInfoVO selectByPrimaryKey(String pkId) {
        return petitionInfoMapper.selectByPrimaryKey(pkId);
    }

    /**
     * @author: xsm
     * @date: 2019/7/25 0025 下午 4:39
     * @Description: 根据监测时间和恶臭点位MN号获取投诉信息及恶臭在线信息的列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public  Map<String, Object> getPetitionAndStenchOnlineListDataByParamMap(Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        //根据时间查询该时间段内的投诉信息
        List<Map<String, Object>> listData = petitionInfoMapper.getStinkPetitionInfoByParamMap(paramMap);
        //获取分页恶臭点位投诉信息
        PageInfo<Map<String, Object>> pageinfo = new PageInfo<>(listData);
        List<Map<String, Object>> list = pageinfo.getList();
        List<String> mnlist = (List<String>) paramMap.get("dgimns");//恶臭点位MN号
        List<Map<String, Object>> pollutants = getAllStenchPollutantsByDgimns(mnlist);
        Set<String> codelist = new HashSet<String>();
        for (Map<String, Object> obj : pollutants) {//所有污染物
            if (obj.get("code") != null) {
                codelist.add(obj.get("code").toString());
            }
        }
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);
        Set<String> airmnlist = new HashSet<String>();
        for (Map<String, Object> obj : airlist) {//关联的空气点位MN  厂界恶臭不关联则取自身
            if (obj.get("airmn") != null) {
                airmnlist.add(obj.get("airmn").toString());
            }
        }
        List<Map<String, Object>> resultlist = new ArrayList<>();
        List<Document> oneDocuments = new ArrayList<>();
        List<Document> twoDocuments = new ArrayList<>();
        List<Criteria> criterialist = new ArrayList<>();
        Criteria criteria = new Criteria();
        Criteria criteriatwo = new Criteria();
        for (Map<String, Object> map : list) {
            String pollutestarttime = map.get("PolluteStartTime").toString();//开始时间
            String polluteendtime = map.get("PolluteEndTime").toString();//结束时间
            Date startDate = DataFormatUtil.getDateYMDHMS(pollutestarttime + ":00");
            Date endDate = DataFormatUtil.getDateYMDHMS(polluteendtime + ":59");
            criterialist.add(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        }
        if(list.size()>0&&mnlist.size()>0&&codelist.size()>0) {

            Criteria timecriteria = new Criteria();
            timecriteria.orOperator(criterialist.toArray(new Criteria[criterialist.size()]));
            criteria.and("DataGatherCode").in(mnlist).and("PollutantCode").in(codelist).andOperator(timecriteria);
            Aggregation aggregation = newAggregation(
                    unwind("RealDataList"),
                    project("DataGatherCode", "MonitorTime", "RealDataList.PollutantCode", "RealDataList.MonitorValue").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d %H:%M").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MinuteTime"),
                    match(criteria),
                    project("DataGatherCode", "MinuteTime", "PollutantCode", "MonitorValue").andExclude("_id")
            );
            oneDocuments = (mongoTemplate.aggregate(aggregation, realTimeCollection, Document.class)).getMappedResults();
            if (airmnlist!=null&&airmnlist.size()>0) {
                //从枚举类中获取风速风向的编码
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                criteriatwo.and("DataGatherCode").in(airmnlist).and("PollutantCode").in(pollutantcodes).andOperator(timecriteria);
                Aggregation twoaggregation = newAggregation(
                        unwind("RealDataList"),
                        project("DataGatherCode", "MonitorTime", "RealDataList.PollutantCode", "RealDataList.MonitorValue").and(DateOperators.DateToString.dateOf("MonitorTime").toString("%Y-%m-%d %H:%M").withTimezone(DateOperators.Timezone.valueOf("+08"))).as("MinuteTime"),
                        match(criteriatwo),
                        project("DataGatherCode", "MinuteTime", "PollutantCode", "MonitorValue").andExclude("_id")
                );
                twoDocuments = (mongoTemplate.aggregate(twoaggregation, realTimeCollection, Document.class)).getMappedResults();
            }
        }
        getMonitorPointMorePollutantsConcentrationRealListData(codelist,airlist,list,oneDocuments,twoDocuments,resultlist);
        //处理分页数据
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            resultMap.put("total", pageinfo.getTotal());
            resultMap.put("datalist", resultlist);
        }else {
            resultMap.put("datalist", resultlist);
        }
        return resultMap;
    }



    /**
     * @author: xsm
     * @date: 2019/7/25 9:40
     * @Description: 获取监测点某个时间点的气候信息
     * @updateUser:xsm
     * @updateDate: 2019/8/15 8:43
     * @updateDescription: 获取某段时间某个监测点的平均风向风速信息
     * @param:
     * @return:
     */
    private Map<String, Object> getMonitorPointRealTimeWeatherData(String airmn, Date startDate, Date endDate) {
        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
        query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, realTimeCollection);
        Double windspeed = 0d;
        Double winddirection = 0d;
        int windspeednum = 0;
        int winddirectionnum = 0;
        List<Map<String, Object>> pollutantList;
        Map<String, Object> map = new LinkedHashMap<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                pollutantList = (List<Map<String, Object>>) document.get("RealDataList");
                for (Map<String, Object> pollutant : pollutantList) {
                    if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        windspeed += pollutant.get("MonitorValue") != null ? Double.parseDouble(pollutant.get("MonitorValue").toString()) : 0d;
                        if (pollutant.get("MonitorValue") != null) {
                            windspeednum += 1;
                        }
                        break;
                    }
                }
                for (Map<String, Object> pollutant : pollutantList) {
                    if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        winddirection += pollutant.get("MonitorValue") != null ? Double.parseDouble(pollutant.get("MonitorValue").toString()) : 0d;
                        if (pollutant.get("MonitorValue") != null) {
                            winddirectionnum += 1;
                        }
                        break;
                    }
                }
            }
            if (windspeednum>0){
                windspeed =  Double.parseDouble(String.format("%.2f", windspeed / (float) windspeednum ));
            }
            if (winddirectionnum>0){
                winddirection = Double.parseDouble(String.format("%.2f", winddirection / (float) winddirectionnum ));
            }
            if (winddirection != null) {
                map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                map.put("winddirectionvalue", winddirection);
            } else {
                map.put("winddirectioncode", "-");
                map.put("winddirectionvalue", "-");
                map.put("winddirectionname", "-");
            }
            map.put("windspeed", windspeed);
        } else {
            map.put("winddirectioncode", "-");
            map.put("winddirectionvalue", "-");
            map.put("winddirectionname", "-");
            map.put("windspeed", "-");
        }
        return map;
    }

    /**
     * @author: xsm
     * @date: 2019/8/52 8:53
     * @Description: 统计恶臭监测点的平均风向风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String,Object> countStinkMonitorPointWeather(List<Document> documents, String mn) {
        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Double windspeed = 0d;
        Double winddirection = 0d;
        int windspeednum = 0;
        int winddirectionnum = 0;
        List<Map<String, Object>> pollutantList;
        Map<String, Object> map = new LinkedHashMap<>();
            for (Document document : documents) {
                if (mn.equals(document.getString("DataGatherCode"))) {
                    pollutantList = (List<Map<String, Object>>) document.get("RealDataList");
                    for (Map<String, Object> pollutant : pollutantList) {
                        if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                            windspeed += pollutant.get("MonitorValue") != null ? Double.parseDouble(pollutant.get("MonitorValue").toString()) : 0d;
                            if (pollutant.get("MonitorValue") != null) {
                                windspeednum += 1;
                            }
                            break;
                        }
                    }
                    for (Map<String, Object> pollutant : pollutantList) {
                        if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                            winddirection += pollutant.get("MonitorValue") != null ? Double.parseDouble(pollutant.get("MonitorValue").toString()) : 0d;
                            if (pollutant.get("MonitorValue") != null) {
                                winddirectionnum += 1;
                            }
                            break;
                        }
                    }
                }
            }
                if (windspeednum > 0) {
                    windspeed = Double.parseDouble(String.format("%.2f", windspeed / (float) windspeednum));
                }
                if (winddirectionnum > 0) {
                    winddirection = Double.parseDouble(String.format("%.2f", winddirection / (float) winddirectionnum));
                }
                if (winddirection != null) {
                    map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                    map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                    map.put("winddirectionvalue", winddirection);
                } else {
                    map.put("winddirectioncode", "-");
                    map.put("winddirectionvalue", "-");
                    map.put("winddirectionname", "-");
                }
                map.put("windspeed", windspeed);

                return map;

    }


    /**
     * @author: xsm
     * @date: 2019/7/25 9:40
     * @Description: 获取某个投诉事件中恶臭监测点多污染物的在线浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void getMonitorPointMorePollutantsConcentrationRealListData(Set<String> codelist, List<Map<String, Object>> airlist, List<Map<String, Object>> alllist, List<Document> oneDocuments, List<Document> twoDocuments, List<Map<String, Object>> resultlist) {
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        codelist.addAll(pollutantcodes);
        //构建Mongdb查询条件
        for (Map<String, Object> map : alllist) {
            String pollutestarttime = map.get("PolluteStartTime").toString();//开始时间
            String polluteendtime = map.get("PolluteEndTime").toString();//结束时间
            Date startDate = DataFormatUtil.getDateYMDHM(pollutestarttime);
            Date endDate = DataFormatUtil.getDateYMDHM(polluteendtime);
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("submittime", map.get("SubmitTime"));
            resultmap.put("monitorpointtypecode", map.get("FK_MonitorPointTypeCode"));
            resultmap.put("pollutionname", map.get("PollutionName"));
            resultmap.put("monitorpointid", map.get("PK_ID"));
            resultmap.put("petitiontitle", map.get("PetitionTitle"));
            resultmap.put("pollutestarttime", map.get("PolluteStartTime"));
            resultmap.put("polluteendtime", map.get("PolluteEndTime"));
            resultmap.put("duration", map.get("Duration"));
            resultmap.put("monitorpointname", map.get("MonitorPointName"));
            int d1 = (int) (startDate.getTime()) / 1000;
            int d2 = (int) (endDate.getTime()) / 1000;
            String airmn = "";
            for (Map<String, Object> airmap : airlist) {//获取空气点位MN号
                if ((map.get("DGIMN").toString()).equals(airmap.get("stenchmn").toString())) {
                    if (airmap.get("airmn") != null) {
                        airmn = airmap.get("airmn").toString();
                    }
                    break;
                }
            }
            for (String code : pollutantcodes) {
                Double value = 0d;
                int num = 0;
                if (!"".equals(airmn) && twoDocuments.size() > 0) {
                    for (Document document : twoDocuments) {
                        if ((document.getString("DataGatherCode")).equals(airmn)) {//MN号相等
                            if ((document.getString("PollutantCode")).equals(code)) {//污染物相等
                                int d = (int) ((DataFormatUtil.getDateYMDHM(document.getString("MinuteTime"))).getTime()) / 1000;
                                if (d >= d1 && d <= d2) {//判断在该时间范围内
                                    value += document.get("MonitorValue") != null ? Double.parseDouble(document.get("MonitorValue").toString()) : 0d;
                                    if (document.get("MonitorValue") != null) {
                                        num += 1;
                                    }
                                }
                            }
                        }
                    }
                }
                if (num > 0) {
                    value = Double.parseDouble(String.format("%.2f", value / (float) num));
                    if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(code)) {
                        resultmap.put("windspeed", value);
                    } else {
                        resultmap.put("winddirectionvalue", value);
                        resultmap.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(value, "code"));
                        resultmap.put("winddirectionname", DataFormatUtil.windDirectionSwitch(value, "name"));
                    }
                } else {
                    if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(code)) {
                        resultmap.put("windspeed", "-");
                    } else {
                        resultmap.put("winddirectionvalue","-");
                        resultmap.put("winddirectioncode", "-");
                        resultmap.put("winddirectionname", "-");
                    }
                }
            }
            for (String code : codelist) {
                Double minvalue = 0d;
                Double maxvalue = 0d;
                Double avgvalue = 0d;
                int num = 0;
                if (oneDocuments.size() > 0) {//判断查询数据是否为空
                    for (Document document : oneDocuments) {
                        if ((document.getString("DataGatherCode")).equals(map.get("DGIMN").toString())) {//MN号相等
                            if ((document.getString("PollutantCode")).equals(code)) {//污染物相等
                                int d = (int) ((DataFormatUtil.getDateYMDHM(document.getString("MinuteTime"))).getTime()) / 1000;
                                if (d >= d1 && d <= d2) {//判断在该时间范围内
                                    Object value = document.get("MonitorValue");
                                    if (value != null && !"".equals(value)) {//判断是否有该因子的监测值
                                        num += 1;
                                        avgvalue += Double.parseDouble(value.toString());
                                        if (minvalue == 0d) {
                                            minvalue = Double.parseDouble(value.toString());

                                        } else {
                                            if (minvalue > Double.parseDouble(value.toString())) {
                                                minvalue = Double.parseDouble(value.toString());
                                            }
                                        }
                                        if (maxvalue == 0d) {
                                            maxvalue = Double.parseDouble(value.toString());

                                        } else {
                                            if (maxvalue < Double.parseDouble(value.toString())) {
                                                maxvalue = Double.parseDouble(value.toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //存储污染物信息
                if (num > 0) {
                    resultmap.put(code + "-minvalue", minvalue);
                    resultmap.put(code + "-maxvalue", maxvalue);
                    String formatvalue = String.format("%.2f", avgvalue / (float) num);
                    resultmap.put(code + "-avgvalue", formatvalue);
                } else {
                    resultmap.put(code + "-minvalue", "-");
                    resultmap.put(code + "-maxvalue", "-");
                    resultmap.put(code + "-avgvalue", "-");
                }
            }
            resultlist.add(resultmap);
        }
    }





    /**
     * @author: xsm
     * @date: 2019/7/25 0025 下午 5:31
     * @Description:根据恶臭监测点Mn号获取该监测点所监测的所有污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getAllStenchPollutantsByDgimns(List<String> dgimns) {
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("dgimns", dgimns);
        List<Map<String, Object>> allpollutants = otherMonitorPointPollutantSetMapper.getAllStenchPollutantsByDgimns(parammap);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Set set = new HashSet();
        if (allpollutants != null && allpollutants.size() > 0) {
            for (Map<String, Object> objmap : allpollutants) {
                if (set.contains(objmap.get("code"))) {//判断是否污染物编码重复
                    continue;//重复
                } else {//不重复
                    set.add(objmap.get("code"));
                    result.add(objmap);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/7/24 0024 下午 7:20
     * @Description: 根据监测时间和恶臭点位MN号获取恶臭污染物监测数据及风向信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getStenchOnlineDataAndWeatherDataByParamMap(Map<String, Object> paramMap) {
        List<Map<String, Object>> resultlist = new ArrayList<>();
        try{
        String pollutantcode = paramMap.get("pollutantcode").toString();
        //根据时间查询该时间段内的投诉信息
        List<Map<String, Object>> petitionlist = petitionInfoMapper.getPetitionInfosByParamMap(paramMap);
        List<String> mnlist = (List<String>) paramMap.get("dgimns");//恶臭点位MN号
        //获取所有恶臭的点位信息
        Map<String, Object> Map = new HashMap<>();
        Map.put("enttypecode", CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum.getCode());
        Map.put("othertypecode", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode());
        List<Map<String, Object>> alllist = otherMonitorPointMapper.getAllStenchMonitorPointInfoByParamMap(Map);
        //通过MN号获取对应空气站MN号
        List<Map<String, Object>> airlist = airMonitorStationMapper.getAirStationsByMN(paramMap);

        for (Map<String, Object> map : petitionlist) {
            String pollutestarttime = map.get("PolluteStartTime").toString();//开始时间
            String polluteendtime = map.get("PolluteEndTime").toString();//结束时间
            Date startDate = DataFormatUtil.getDateYMDHMS(pollutestarttime + ":00");
            Date endDate = DataFormatUtil.getDateYMDHMS(polluteendtime + ":59");
            countPetitionStenchOnlineAndWeatherDatas(startDate, endDate, pollutantcode, mnlist, airlist,map, alllist, resultlist);
        }
        return resultlist;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultlist;
    }
    /**
     *
     * @author: lip
     * @date: 2019/9/3 0003 上午 10:22
     * @Description: 自定义查询条件获取投诉数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPetitionDataByParam(Map<String, Object> paramMap) {
        return petitionInfoMapper.getPetitionDataByParam(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/9/27 0027 下午 5:06
     * @Description: 获取投诉任务详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPetitionDetailById(Map<String, Object> paramMap) {
        return petitionInfoMapper.getPetitionDetailById(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2019/7/24 0024 下午 7:20
     * @Description: 统计单个投诉事件下各恶臭点位的数据信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    private List<Map<String, Object>> countPetitionStenchOnlineAndWeatherDatas(Date startDate, Date endDate, String pollutantcode, List<String> mnlist, List<Map<String, Object>> airlist,Map<String, Object> petitionmap, List<Map<String, Object>> alllist, List<Map<String, Object>> resultlist) {
        Set<String> airmns = new HashSet<>();
        for (Map<String, Object> map : airlist) {//获取空气点位MN号
            if (map.get("airmn")!=null) {
                airmns.add(map.get("airmn").toString());
            }
        }
        Map<String, Object> airmap = new HashMap<>();
        for (String airmn : airmns) {
            //获取点位实时气候信息
            List<Map<String, Object>> weathermap = getMonitorPointRealTimeWeatherDatas(airmn, startDate, endDate);
            airmap.put(airmn, weathermap);
        }
        //获取以投诉开始时间到投诉结束时间的各恶臭点位的实时监测数据
        Map<String, Object> stenchmap = getMonitorPointOnePollutantConcentrationRealDatas(mnlist, pollutantcode, startDate, endDate);
        for (String str : mnlist) {
            List<Map<String, Object>> datavalues = new ArrayList<>();
            String name = "";
            for (Map<String, Object> map : alllist) {
                if (str.equals(map.get("DGIMN").toString())) {
                    name = map.get("MonitorPointName").toString();
                    break;
                }
            }
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> onestenchmap = (Map<String, Object>) stenchmap.get(str);//获取对应点位的在线数据
            List<Map<String, Object>> oneairlist =new ArrayList<>();
            boolean ishaveairmn =false;
            for (Map<String, Object> map : airlist) {
                if (str.equals(map.get("stenchmn").toString())&&map.get("airmn")!=null&&!"".equals(map.get("airmn").toString())) {
                   ishaveairmn = true;
                   oneairlist = (List<Map<String, Object>>) airmap.get(map.get("airmn"));
                }
            }
            datavalues = getStenchOnlineAndWeatherSplicingDatas(pollutantcode, onestenchmap, oneairlist,ishaveairmn);
            resultmap.put("petitiontitle", petitionmap.get("PetitionTitle"));
            resultmap.put("monitorpointname", name);
            resultmap.put("datavalues", datavalues);
            resultlist.add(resultmap);
        }
        return resultlist;

    }


    /**
     * @author: xsm
     * @date: 2019/7/25 0025 上午 10:45
     * @Description: 获取恶臭点位在线监测与风向、风速的拼接数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getStenchOnlineAndWeatherSplicingDatas(String pollutantcode,  Map<String, Object> onestenchmap, List<Map<String, Object>> oneairlist,boolean ishaveairmn) {
        String[] names = DataFormatUtil.directName;
        String[] codes = DataFormatUtil.directCode;
        List<Map<String, Object>> resultlist = new ArrayList<>();
        for (String str : names) {//遍历风向
            List<String> timelist = new ArrayList<>();
            Double windspeed = 0d;
            int num = 0;
            String winddirectioncode = "";
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(str)) {
                    winddirectioncode = codes[i];
                    break;
                }
            }
            Map<String, Object> valuemap = new HashMap<>();
            if (ishaveairmn == true) {
                for (Map<String, Object> objmap : oneairlist) {
                    if (str.equals(objmap.get("winddirectionname").toString())) {
                        timelist.add(objmap.get("monitortime").toString());
                        if (objmap.get("windspeed") != null) {
                            num += 1;
                            windspeed = windspeed + Double.parseDouble(objmap.get("windspeed").toString());
                        }
                    }
                }
                String value="";
                if (onestenchmap!=null) {
                    List<Map<String, Object>> onestenchlist = (List<Map<String, Object>>) onestenchmap.get(pollutantcode);
                    value = countOnePollutantAvgConcentration(timelist, pollutantcode, onestenchlist);
                }
                String formatvalue = "";
                if (num > 0) {
                    formatvalue = String.format("%.2f", windspeed / (float) num);
                }
                valuemap.put("winddirectionname", str);
                valuemap.put("winddirectioncode", winddirectioncode);
                valuemap.put("winddirectionnum", timelist.size());
                valuemap.put("windspeedavg", "".equals(formatvalue) ? 0 : formatvalue);
                valuemap.put("pollutantcode", pollutantcode);
                valuemap.put("pollutantvalue", "".equals(value) ? 0 : value);
                resultlist.add(valuemap);
            } else {

                Double windspeedtotal = 0d;
                Double concentrationtotal = 0d;
                int num1 = 0;
                int num2 = 0;
                int num3 = 0;
                String speedformat = "";
                String concentrationformat = "";
                if (onestenchmap!=null&&onestenchmap.size()>0) {
                    List<Map<String, Object>> thelist = (List<Map<String, Object>>) onestenchmap.get(pollutantcode);
                    List<Map<String, Object>> directionlist = (List<Map<String, Object>>) onestenchmap.get(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode());
                    List<Map<String, Object>> speedlist = (List<Map<String, Object>>) onestenchmap.get(CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                    for (Map<String, Object> objmap : directionlist) {
                        String winddirectionname = DataFormatUtil.windDirectionSwitch(Double.parseDouble(objmap.get("monitorvalue").toString()), "name");
                        if (str.equals(winddirectionname)) {
                            num3 += 1;
                            for (Map<String, Object> objmap1 : speedlist) {
                                if ((objmap.get("monitortime").toString()).equals(objmap1.get("monitortime").toString())) {
                                    if (objmap1.get("monitorvalue") != null) {
                                        num1 += 1;
                                        windspeedtotal = windspeedtotal + Double.parseDouble(objmap1.get("monitorvalue").toString());
                                    }
                                }
                            }
                            for (Map<String, Object> objmap2 : thelist) {
                                if ((objmap.get("monitortime").toString()).equals(objmap2.get("monitortime").toString())) {
                                    if (objmap2.get("monitorvalue") != null) {
                                        num2 += 1;
                                        concentrationtotal = concentrationtotal + Double.parseDouble(objmap2.get("monitorvalue").toString());
                                    }
                                }
                            }

                        }
                    }
                    if (num1 > 0) {//风速
                        speedformat = String.format("%.2f", windspeedtotal / (float) num1);
                    }
                    if (num2 > 0) {//浓度
                        concentrationformat = String.format("%.2f", concentrationtotal / (float) num2);
                    }
                }
                valuemap.put("winddirectionname", str);
                valuemap.put("winddirectioncode", winddirectioncode);
                valuemap.put("winddirectionnum", num3);
                valuemap.put("windspeedavg", "".equals(speedformat) ? 0 : speedformat);
                valuemap.put("pollutantcode", pollutantcode);
                valuemap.put("pollutantvalue", "".equals(concentrationformat) ? 0 : concentrationformat);
                resultlist.add(valuemap);
            }
        }
        return resultlist;
    }


    /**
     * @author: xsm
     * @date: 2019/7/25 0025 上午 10:45
     * @Description: 获取以mn号进行分组的点位单污染物监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> getMonitorPointOnePollutantConcentrationRealDatas(List<String> mns, String pollutantcode, Date startDate, Date endDate) {
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes =new ArrayList<>();
        pollutantcodes.add(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode());
        pollutantcodes.add(CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        pollutantcodes.add(pollutantcode);
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, realTimeCollection);
        Map<String, Object> resultmap = new HashMap<>();
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                //MN号
                String mnnum = document.getString("DataGatherCode");
                //监测时间
                String monitortime = DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime"));
                List<Map<String, Object>> pollutantDataList = document.get("RealDataList", List.class);
                for (String code : pollutantcodes) {//遍历污染物
                Object value = "";
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if (code.equals(dataMap.get("PollutantCode"))) {
                        value = dataMap.get("MonitorValue");
                        break;
                    }
                }
                //判断map中是否有该mn的数据
                boolean flag = resultmap.containsKey(mnnum);
                if (flag == true) {//有
                    Map<String, Object> themap = (Map<String, Object>) resultmap.get(mnnum);
                    List<Map<String, Object>> thelist = (List<Map<String, Object>>) themap.get(code);
                    if (!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitortime);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                        themap.put(code,thelist);
                    }
                } else {//无
                    Map<String, Object> themap = new HashMap<>();
                    List<Map<String, Object>> thelist = new ArrayList<>();
                    if (!"".equals(value)) {//判断是否有该因子的监测值
                        Map<String, Object> objmap = new HashMap<>();
                        objmap.put("monitortime", monitortime);
                        objmap.put("monitorvalue", value);
                        thelist.add(objmap);
                        themap.put(pollutantcode,new ArrayList<>());
                        themap.put(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode(),new ArrayList<>());
                        themap.put(CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode(),new ArrayList<>());
                        themap.put(code,thelist);
                        resultmap.put(mnnum, themap);
                    }

                }
            }
            }
        }
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2019/7/25 9:40
     * @Description: 获取监测点实时气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointRealTimeWeatherDatas(String airmn, Date startDate, Date endDate) {
        //构建Mongdb查询条件
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
        query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, realTimeCollection);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Double windspeed = 0d;
        Double winddirection = null;
        List<Map<String, Object>> pollutantList;
        for (Document document : documents) {
            if (document.get("MonitorTime") != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("monitortime", DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")));
                pollutantList = (List<Map<String, Object>>) document.get("RealDataList");
                for (Map<String, Object> pollutant : pollutantList) {
                    if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        windspeed = pollutant.get("MonitorValue") != null ? Double.parseDouble(pollutant.get("MonitorValue").toString()) : 0d;
                        break;
                    }
                }
                for (Map<String, Object> pollutant : pollutantList) {
                    if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                        winddirection = pollutant.get("MonitorValue") != null ? Double.parseDouble(pollutant.get("MonitorValue").toString()) : null;
                        break;
                    }
                }
                if (winddirection != null) {
                    map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                    map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                    map.put("winddirectionvalue", winddirection);
                } else {
                    map.put("winddirectioncode", winddirection);
                    map.put("winddirectionvalue", winddirection);
                    map.put("winddirectionname", winddirection);
                }
                map.put("windspeed", windspeed);
                map.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));

                dataList.add(map);
            }
        }
        return dataList;
    }

    /**
     * @param timelist
     * @param code
     * @param concentrationlist
     * @author: xsm
     * @date: 2019/6/28 10:48
     * @Description: 统计单个污染物的平均浓度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private String countOnePollutantAvgConcentration(List<String> timelist, String code, List<Map<String, Object>> concentrationlist) {
        int num = 0;
        double valuenum = 0d;
        DecimalFormat df = new DecimalFormat("#.00");
        for (String timestr : timelist) {
            for (Map<String, Object> objmap2 : concentrationlist) {
                //当时间相同时
                if (timestr.equals(objmap2.get("monitortime").toString())) {
                    num += 1;
                    valuenum += Double.parseDouble(objmap2.get("monitorvalue").toString());
                    break;
                } else {
                    continue;
                }
            }
        }
        String value = "";
        if (num > 0) {
            value = df.format(valuenum / num);
        }
        return value;
    }


}
