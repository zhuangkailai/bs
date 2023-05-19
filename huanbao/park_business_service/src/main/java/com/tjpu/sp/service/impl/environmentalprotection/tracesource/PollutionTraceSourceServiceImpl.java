package com.tjpu.sp.service.impl.environmentalprotection.tracesource;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GasOutPutInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.OtherMonitorPointMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.weather.WeatherMapper;
import com.tjpu.sp.model.common.PageEntity;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@Transactional
public class PollutionTraceSourceServiceImpl implements PollutionTraceSourceService {

    @Autowired
    private WeatherMapper weatherMapper;
    @Autowired
    private OtherMonitorPointMapper otherMonitorPointMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private UnorganizedMonitorPointInfoMapper outPutUnorganizedMapper;
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private PollutionMapper pollutionMapper;



    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 9:57
     * @Description: 根据监测时间获取所有恶臭、voc、厂界恶臭的MN号和关联气象的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceMonitorPointMN(Map<String, Object> param) {
        return otherMonitorPointMapper.getTraceSourceMonitorPointMN(param);
    }


    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 11:20
     * @Description: 获取所有废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllMonitorGasOutPutInfo() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("type", 1);
        return gasOutPutInfoMapper.getAllMonitorGasOutPutInfo(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:07
     * @Description: 获取废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getGasOutPutInfo(Map<String, Object> paramMap) {
        paramMap.put("type", 1);
        return gasOutPutInfoMapper.getAllMonitorGasOutPutInfo(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 10:09
     * @Description: 获取主导风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getLeadingWindDirectionAndWindSpeed(String monitortime) {
        Map<String, Object> resultMap = new HashMap<>();
        Date time = DataFormatUtil.parseDateYMD(monitortime);
        //气候信息
        Criteria criteria = new Criteria();
        criteria.and("WeatherDate").gte(time).lte(time);
        String[] directCodeList = DataFormatUtil.directCode;
        String[] directNameList = DataFormatUtil.directName;
        List<Map> mappedResults = mongoTemplate.aggregate(newAggregation(
                match(criteria), project("WeatherTime", "WindDirection", "WindPower")
                , sort(Sort.Direction.DESC, "WeatherTime"), limit(1)), "WeatherData", Map.class).getMappedResults();
        if (mappedResults.size() > 0) {
            Map<String, Object> map = mappedResults.get(0);
            if (map.get("WindDirection") != null) {
                String winddirection = map.get("WindDirection").toString();
                winddirection = winddirection.replace("风", "");
                resultMap.put("winddirection", winddirection);
                resultMap.put("winddirectioncode", "");
                for (int i = 0; i < directNameList.length; i++) {
                    if (directNameList[i].equals(winddirection)) {
                        resultMap.put("winddirectioncode", directCodeList[i]);
                        break;
                    }
                }
            } else {
                resultMap.put("winddirection", "");
            }
            resultMap.put("windspeed", map.get("WindPower"));
        } else {
            resultMap.put("winddirection", "");
            resultMap.put("windspeed", "");
        }
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 11:20
     * @Description: 根据监测时间和污染物获取溯源监测点的风向、风速和污染物浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceMonitorPointOnlineData(String monitortime, Set<String> airmns, Set<String> othermns, String pollutantcode, List<Map<String, Object>> mns) {
        //构建Mongdb查询条件
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59");
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode(), CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        //监测点该时刻的气象数据
        Query query = new Query();
        if (airmns!=null&&airmns.size()>0) {
            query.addCriteria(Criteria.where("DataGatherCode").in(airmns));
        }else{
            //若关联空气点位 为空  则默认查自身点位的 气象数据
            query.addCriteria(Criteria.where("DataGatherCode").in(othermns));
        }
        query.addCriteria(Criteria.where("MinuteDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        List<Document> airdocuments = mongoTemplate.find(query, Document.class, "MinuteData");
        //监测点 该时刻  点位污染物的浓度
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("DataGatherCode").in(othermns));
        query2.addCriteria(Criteria.where("MinuteDataList.PollutantCode").is(pollutantcode));
        query2.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        List<Document> documents = mongoTemplate.find(query2, Document.class, "MinuteData");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode());
        paramMap.put("pollutantcode", pollutantcode);
        boolean isHaveCon = false;
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByPollutantType(paramMap);
        if (pollutants.size() > 0) {
            for (Map<String, Object> pollutant : pollutants) {
                if (pollutant.get("IsHasConvertData") != null && Integer.parseInt(pollutant.get("IsHasConvertData").toString()) == 1) {
                    isHaveCon = true;
                }
            }
        }
        String SCValueKey = "AvgStrength";
        String ZSValueKey = "AvgConvertStrength";
        for (Map<String, Object> map : mns) {
            List<Map<String, Object>> pollutantList;
            if (map.get("airmn") != null) {
                Double windspeed = 0d;
                Double winddirection = null;
                for (Document obj : airdocuments) {//风速风向
                    if ((map.get("airmn").toString()).equals(obj.getString("DataGatherCode"))) {//MN号相等
                        pollutantList = (List<Map<String, Object>>) obj.get("MinuteDataList");
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                windspeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : 0d;
                                break;
                            }
                        }
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                winddirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                break;
                            }
                        }
                    }
                }
                if (winddirection != null) {
                    map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                    map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                } else {
                    map.put("winddirectionname", winddirection);
                }
                map.put("windspeed", windspeed);
            } else {
                map.put("winddirectionname", "");
                map.put("winddirectioncode", "");
                map.put("windspeed", "");
            }
            String value = "";
            int alarmlevel =-1;
            int exception=-1;
            boolean overstandard=false;
            int onlinestatus = 0;

            for (Document obj : documents) {//监测值
                if ((map.get("DGIMN").toString()).equals(obj.getString("DataGatherCode"))) {//MN号相等
                    pollutantList = (List<Map<String, Object>>) obj.get("MinuteDataList");
                    for (Map<String, Object> pollutant : pollutantList) {
                        if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                            onlinestatus = 1;//有该污染物的数据  表示点位在线
                            if (isHaveCon&&
                                    map.get("monitorpointtype")!=null
                                    &&Integer.parseInt(map.get("monitorpointtype").toString())==CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()){
                                value = pollutant.get(ZSValueKey) != null ? pollutant.get(ZSValueKey).toString() : "";
                            }else{
                                value = pollutant.get(SCValueKey) != null ? pollutant.get(SCValueKey).toString() : "";
                            }


                            int IsOver = pollutant.get("IsOver") == null ? -1 : Integer.valueOf(pollutant.get("IsOver").toString());
                            int IsException = pollutant.get("IsException") == null ? -1 : Integer.valueOf(pollutant.get("IsException").toString());
                            boolean IsOverStandard = pollutant.get("IsOverStandard") == null ? false : (boolean)pollutant.get("IsOverStandard");
                            if(IsOver>0 && IsOver<alarmlevel){
                                onlinestatus = 2;//点位超限
                                alarmlevel=IsOver;
                            }
                            if(IsException>0){
                                onlinestatus = 3;//点位异常
                                exception=IsException;
                            }
                            if(IsOverStandard){
                                onlinestatus = 2;//点位超标
                                overstandard=IsOverStandard;
                            }
                            break;
                        }
                    }
                }
            }
            map.put("OnlineStatus", onlinestatus);
            map.put("pollutantcode", pollutantcode);
            map.put("IsOverStandard", overstandard);
            map.put("IsException", exception);
            map.put("alarmlevel", alarmlevel);
            map.put("monitorvalue", value);
        }
        return mns;
    }

    /**
     * @author: xsm
     * @date: 2019/8/31 11:49
     * @Description: 根据自定义参数获取溯源监测点污染物监测值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceMonitorPointPollutantOnlineDataByParamMap(String dgimn, List<Map<String, Object>> pollutants, String monitortime) {
        //去MongoDB中查询数据
        List<Map<String, Object>> pollutantvalue = new ArrayList<Map<String, Object>>();
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
        List<String> pollutantlist = new ArrayList<>();
        Map<String, Object> pollutantmap = new HashMap<>();
        for (Map<String, Object> map : pollutants) {
            pollutantlist.add(map.get("code").toString());
            pollutantmap.put(map.get("code").toString(), map.get("name"));
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("MinuteDataList.PollutantCode").in(pollutantlist));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        Document document = mongoTemplate.findOne(query, Document.class, "MinuteData");
        if (document != null) {//判断查询数据是否为空
            Map<String, Object> map = new LinkedHashMap<>();
            List<Map<String, Object>> pollutantDataList = document.get("MinuteDataList", List.class);
            for (String code : pollutantlist) {//遍历污染物
                Object value = "";
                Map<String, Object> objmap = new HashMap<>();
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if (code.equals(dataMap.get("PollutantCode"))) {
                        if (dataMap.get("AvgStrength") != null) {
                            value = dataMap.get("AvgStrength");
                            break;
                        } else {
                            break;
                        }
                    }
                }
                objmap.put("pollutantcode", code);
                objmap.put("pollutantname", pollutantmap.get(code));
                objmap.put("pollutantvalue", value);
                pollutantvalue.add(objmap);
            }
        } else {
            for (String code : pollutantlist) {//遍历污染物
                Map<String, Object> objmap = new HashMap<>();
                objmap.put("pollutantcode", code);
                objmap.put("pollutantname", pollutantmap.get(code));
                objmap.put("pollutantvalue", "");
                pollutantvalue.add(objmap);
            }
        }

        return pollutantvalue;
    }

    /**
     * @author: xsm
     * @date: 2019/8/31 14:36
     * @Description: 根据自定义参数获取单个溯源监测点污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getTraceSourceMonitorPointPollutantInfoByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> listmap = new ArrayList<>();
        int monitorpointtype = Integer.parseInt(paramMap.get("monitorpointtype").toString());
        switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(monitorpointtype)) {
            case EnvironmentalVocEnum: //voc
            case EnvironmentalStinkEnum: //恶臭
            case MicroStationEnum: //微站
            case meteoEnum: //气象
                paramMap.put("pkidlist", Arrays.asList(paramMap.get("monitorpointid")));
                listmap = otherMonitorPointMapper.getOtherMonitorPointAllPollutantsByIDAndType(paramMap);//恶臭
                break;
            case FactoryBoundaryStinkEnum: //厂界恶臭
                paramMap.put("pkidlist", Arrays.asList(paramMap.get("monitorpointid")));
                listmap = outPutUnorganizedMapper.getEntBoundaryAllPollutantsByIDAndType(paramMap);//厂界恶臭
                break;
            case FactoryBoundarySmallStationEnum: //厂界小型站
                paramMap.put("pkidlist", Arrays.asList(paramMap.get("monitorpointid")));
                listmap = outPutUnorganizedMapper.getEntBoundaryAllPollutantsByIDAndType(paramMap);//厂界小型站
                break;
            case WasteGasEnum: //废气排口
            case SmokeEnum:
                paramMap.put("outputid", paramMap.get("monitorpointid"));
                listmap = pollutantFactorMapper.getPollutantSetInfoByParamMap(paramMap);//废气排口
                break;
        }
        if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()||monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()) {
            if (listmap != null && listmap.size() > 0) {
                for (Map<String, Object> map : listmap) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("code", map.get("Code"));
                    obj.put("name", map.get("pollutantname"));
                    obj.put("pollutantunit", map.get("PollutantUnit"));
                    result.add(obj);
                }
            }
        } else {
            if (listmap != null && listmap.size() > 0) {
                for (Map<String, Object> map : listmap) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("code", map.get("code"));
                    obj.put("name", map.get("name"));
                    obj.put("pollutantunit", map.get("PollutantUnit"));
                    result.add(obj);
                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/9/03 0003 下午 5:14
     * @Description: 获取某时间点的点位dgimn和风向风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointDgimnAndWindDataByMonitortime(String monitortime) {
        //去MongoDB中查询数据
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");

        Date dateYMDHM = DataFormatUtil.getDateYMDHM(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(dateYMDHM);
        instance.add(Calendar.MINUTE, -30);
        //instance.add(Calendar.MINUTE, -5);
        Date startDate = instance.getTime();
        List<Map<String, Object>> pointlist = otherMonitorPointMapper.getTraceSourceMeteoMonitorPointMN(new HashMap<>());
        List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
        Set<String> mns = new HashSet<>();
        Map<String, Object> mnandlongitude = new HashMap<>();
        Map<String, Object> mnandlatitude = new HashMap<>();
        Map<String, Object> mnandtype = new HashMap<>();
        for (Map<String, Object> map : pointlist) {
            if (!dgimns.contains(map.get("airmn").toString()))continue;
            mns.add(map.get("airmn").toString());
            mnandlongitude.put(map.get("airmn").toString(), map.get("Longitude"));
            mnandlatitude.put(map.get("airmn").toString(), map.get("Latitude"));
            mnandtype.put(map.get("airmn").toString(), map.get("FK_MonitorPointTypeCode"));
        }
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.addCriteria(Criteria.where("RealDataList.PollutantCode").in(pollutantcodes));
        List<Document> documents = mongoTemplate.find(query, Document.class, "RealTimeData");

        documents = documents.stream().filter(m -> m.get("DataGatherCode") != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString(),
                Collectors.maxBy(Comparator.comparing(m -> m.get("MonitorTime").toString())))).values().stream().map(m -> m.orElse(new Document())).collect(Collectors.toList());


        List<Map<String, Object>> result = new ArrayList<>();
        if (documents != null) {//判断查询数据是否为空
            for (Document document : documents) {
                List<Map<String, Object>> pollutantDataList = document.get("RealDataList", List.class);
                Map<String, Object> objmap = new HashMap<>();
                Object winddirection = "";
                Object windspeed = "";
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(dataMap.get("PollutantCode"))) {
                        winddirection = dataMap.get("MonitorValue");
                        break;
                    }
                }
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if ((CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode()).equals(dataMap.get("PollutantCode"))) {
                        windspeed = dataMap.get("MonitorValue");
                    }
                }
                objmap.put("winddirection", winddirection);
                objmap.put("windspeed", windspeed);
                objmap.put("dgimn", document.getString("DataGatherCode"));
                objmap.put("longitude", mnandlongitude.get(document.getString("DataGatherCode")));
                objmap.put("latitude", mnandlatitude.get(document.getString("DataGatherCode")));
                objmap.put("monitorpointtypecode", mnandtype.get(document.getString("DataGatherCode")));
                result.add(objmap);
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/09/04 13:15
     * @Description: 根据自定义参数返回敏感点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getSensitivePointDataByParamMap(Map<String, Object> paramMap) {
        //MinuteData
        String collection = paramMap.get("collection").toString();
        String starttime = paramMap.get("starttime").toString();
        String endtime = paramMap.get("endtime").toString();
        String pollutantDataKey = "";

        if (collection.indexOf("Hour") > -1) {
            starttime = starttime + ":00:00";
            endtime = endtime + ":59:59";
            pollutantDataKey = "HourDataList";
        } else if (collection.indexOf("Minute") > -1) {
            starttime = starttime + ":00";
            endtime = endtime + ":59";
            pollutantDataKey = "MinuteDataList";
        }
        //投诉事件经纬度
        List<Map<String, Object>> result = new ArrayList<>();
        double lat_a = Double.parseDouble(paramMap.get("latitude").toString());
        double lng_a = Double.parseDouble(paramMap.get("longitude").toString());
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime);
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime);
        //获取所有敏感点信息
        List<Map<String, Object>> pointlist = otherMonitorPointMapper.getTraceSourceMonitorPointMN(paramMap);
        Set<String> airmns = new HashSet<>();
        for (Map<String, Object> map : pointlist) {
            if (map.get("airmn") != null) {
                airmns.add(map.get("airmn").toString());
            }
        }
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(airmns));
        query.addCriteria(Criteria.where(pollutantDataKey + ".PollutantCode").is(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        List<Document> documents = mongoTemplate.find(query, Document.class, collection);
        //统计各个点位在这段时间的主导风向
        Map<String, Object> airmnvalue = new HashMap<>();
        if (documents.size() > 0) {
            airmnvalue = countLeadingWindDirection(airmns, documents, pollutantDataKey);
            //获取符合的点位信息
            for (Map<String, Object> pointmap : pointlist) {
                if (pointmap.get("Longitude") != null && pointmap.get("Latitude") != null) {
                    //获取事件相当于点位的方位角
                    double azimuth = DataFormatUtil.getAngle1(lat_a,
                            lng_a,
                            Double.parseDouble(pointmap.get("Latitude").toString()),
                            Double.parseDouble(pointmap.get("Longitude").toString())
                    );
                    //获取点位风向值
                    Object main_wind = (pointmap.get("airmn") != null && airmnvalue != null && airmnvalue.size() > 0) ? airmnvalue.get(pointmap.get("airmn").toString()) : null;
                    //判断是否属于上风向点位
                    if (main_wind != null && isUpperWindDirection(main_wind, azimuth)) {
                        Map<String, Object> newmap = new HashMap<>();
                        newmap.put("monitorpointname", pointmap.get("MonitorPointName"));
                        newmap.put("dgimn", pointmap.get("DGIMN"));
                        result.add(newmap);
                    }

                }
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/09/27 13:28
     * @Description: 根据自定义参数获取某个点位的主导风向
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointLeadingWindDirectionDataByParamMap(String starttime, String endtime, String dgimn) {
        List<Map<String, Object>> result = new ArrayList<>();
        //开始时间和结束时间
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        Map<String, Object> paramMap = new HashMap<>();
        //要查询主导风向的点位
        paramMap.put("dgimn", dgimn);
        List<Map<String, Object>> pointlist = otherMonitorPointMapper.getTraceSourceMonitorPointMN(paramMap);
        //风向和风速污染物编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        if (pointlist != null && pointlist.size() > 0 && (pointlist.get(0).get("airmn") != null && !"".equals(pointlist.get(0).get("airmn").toString()))) {
            String airmn = pointlist.get(0).get("airmn").toString();
            //构建Mongdb查询条件
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
            query.addCriteria(Criteria.where("HourDataList.PollutantCode").in(pollutantcodes));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            List<Document> documents = mongoTemplate.find(query, Document.class, "HourData");
            if (documents.size() > 0) {
                Map<String, Object> map = new HashMap<>();
                for (Document document : documents) {
                    List<Map<String, Object>> pollutantDataList = document.get("HourDataList", List.class);
                    Double winddirection = null;
                    Double windspeed = null;
                    for (Map<String, Object> dataMap : pollutantDataList) {//风向
                        if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(dataMap.get("PollutantCode"))) {
                            winddirection = dataMap.get("AvgStrength") != null ? Double.parseDouble(dataMap.get("AvgStrength").toString()) : null;
                            break;
                        }
                    }
                    for (Map<String, Object> pollutant : pollutantDataList) {//风速
                        if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                            windspeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                            break;
                        }
                    }
                    if (winddirection != null) {
                        String winddirectionname = DataFormatUtil.windDirectionSwitch(winddirection, "name");
                        if (map.get(winddirectionname) != null) {
                            Map<String, Object> valuemap = (Map<String, Object>) map.get(winddirectionname);
                            valuemap.put("winddirection", winddirectionname);
                            valuemap.put("num", Integer.parseInt(valuemap.get("num").toString()) + 1);
                            if (windspeed != null) {//风速不为空
                                valuemap.put("windspeed", Double.parseDouble(valuemap.get("windspeed").toString()) + windspeed);
                                valuemap.put("windspeednum", Integer.parseInt(valuemap.get("windspeednum").toString()) + 1);
                            }
                        } else {
                            Map<String, Object> valuemap = new HashMap<>();
                            valuemap.put("winddirection", winddirectionname);
                            valuemap.put("num", 1);
                            if (windspeed != null) {//风速不为空
                                valuemap.put("windspeed", windspeed);
                                valuemap.put("windspeednum", 1);
                            } else {
                                valuemap.put("windspeed", 0d);
                                valuemap.put("windspeednum", 0);
                            }
                            map.put(winddirectionname, valuemap);
                        }
                    }
                }
                List<Map<String, Object>> resultlist = new ArrayList<>();
                int num = 0;
                for (Map.Entry<String, Object> obj : map.entrySet()) {
                    Map<String, Object> objmap = (Map<String, Object>) obj.getValue();
                    if (objmap.get("num") != null && Integer.parseInt(objmap.get("num").toString()) >= num) {//判断主流风向
                        if (num == Integer.parseInt(objmap.get("num").toString())) {//相等时
                            resultlist.add(objmap);
                        } else if (Integer.parseInt(objmap.get("num").toString()) > num) {//大于时
                            resultlist.clear();
                            resultlist.add(objmap);
                            num = Integer.parseInt(objmap.get("num").toString());
                        }

                    }
                }
                if (resultlist != null && resultlist.size() > 0) {
                    for (Map<String, Object> themap : resultlist) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("winddirection", themap.get("winddirection"));
                        resultMap.put("windspeed", String.format("%.2f", Double.parseDouble(themap.get("windspeed").toString()) / Double.parseDouble(themap.get("windspeednum").toString())));
                        result.add(resultMap);
                    }
                }
            }

        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/09/27 13:28
     * @Description: 根据自定义参数获取某个点位的风向和OU浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMonitorPointWindDirectionAndOUDataByParamMap(String starttime, String endtime, String dgimn) {
        //开始时间和结束时间
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00:00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59:59");
        String oucode = CommonTypeEnum.StinkPollutionEnum.OUEnum.getCode();
        List<Map<String, Object>> pollutantvalue = new ArrayList<Map<String, Object>>();
        pollutantvalue = getMonitorPointOnePollutantConcentrationData(dgimn, oucode, startDate, endDate);
        //获取风向
        Map<String, Object> paramMap = new HashMap<>();
        //获取关联风向
        paramMap.put("dgimn", dgimn);
        List<Map<String, Object>> pointlist = otherMonitorPointMapper.getTraceSourceMonitorPointMN(paramMap);
        paramMap.clear();
        if (pointlist != null && pointlist.size() > 0 && (pointlist.get(0).get("airmn") != null && !"".equals(pointlist.get(0).get("airmn").toString()))) {
            String airmn = pointlist.get(0).get("airmn").toString();
            //构建Mongdb查询条件
            Query query = new Query();
            query.addCriteria(Criteria.where("DataGatherCode").is(airmn));
            query.addCriteria(Criteria.where("HourDataList.PollutantCode").is(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()));
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            List<Document> documents = mongoTemplate.find(query, Document.class, "HourData");
            if (documents.size() > 0) {
                for (Document document : documents) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    List<Map<String, Object>> pollutantDataList = document.get("HourDataList", List.class);
                    Double winddirection = null;
                    String monitortime = "";
                    monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                    for (Map<String, Object> dataMap : pollutantDataList) {//风向
                        if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(dataMap.get("PollutantCode"))) {
                            winddirection = dataMap.get("AvgStrength") != null ? Double.parseDouble(dataMap.get("AvgStrength").toString()) : null;
                            break;
                        }
                    }
                    if (winddirection != null) {
                        paramMap.put(monitortime, DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                    }
                }
            }
        }
        if (pollutantvalue != null && pollutantvalue.size() > 0) {
            for (Map<String, Object> themap : pollutantvalue) {
                themap.put("winddirection", paramMap.get(themap.get("monitortime")) != null ? paramMap.get(themap.get("monitortime")) : "");
            }
        }
        return pollutantvalue;
    }


    /**
     * @author: xsm
     * @date: 2019/9/27 16:23
     * @Description: 获取监测点某个污染物的在一段时间内的小时浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getMonitorPointOnePollutantConcentrationData(String dgimn, String oucode, Date startDate, Date endDate) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        //构建Mongdb查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").is(dgimn));
        query.addCriteria(Criteria.where("HourDataList.PollutantCode").is(oucode));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        query.with(new Sort(Sort.Direction.ASC, "MonitorTime"));
        List<Document> documents = mongoTemplate.find(query, Document.class, "HourData");
        if (documents.size() > 0) {//判断查询数据是否为空
            for (Document document : documents) {
                Map<String, Object> map = new LinkedHashMap<>();
                String monitortime = "";
                monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                List<Map<String, Object>> pollutantDataList = document.get("HourDataList", List.class);
                Object value = "";
                for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                    if (oucode.equals(dataMap.get("PollutantCode"))) {
                        if (dataMap.get("AvgStrength") != null) {
                            value = dataMap.get("AvgStrength");
                            break;
                        }
                    }
                }
                if (!"".equals(value)) {
                    map.put("monitortime", monitortime);
                    map.put("ou", value);
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * @author: lip
     * @date: 2019/9/12 0012 下午 1:17
     * @Description: 判断是否属于上风向点位
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private boolean isUpperWindDirection(Object main_wind, double azimuth) {
        boolean isUpper = false;
        Double mainWind = Double.parseDouble(main_wind.toString());
        Double interval = Double.parseDouble(DataFormatUtil.parseProperties("wind.direction.interval"));
        Double min;
        Double max;
        for (Double i = 0d; i <= 360d; i = i + interval) {
            min = i;
            max = interval + i;
            if (mainWind >= min && mainWind <= max) {//反向区间
                if (azimuth > min && azimuth < max) {//方位角是否在反向区间内
                    isUpper = true;
                }
                break;
            }
        }

        return isUpper;

    }


    /**
     * @author: xsm
     * @date: 2019/09/04 13:15
     * @Description: 统计一段时间内各个风向点位的主导风向
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> countLeadingWindDirection(Set<String> airmns, List<Document> documents, String pollutantDataKey) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> objmap = new HashMap<>();
        for (Document document : documents) {
            String mn = document.getString("DataGatherCode");
            List<Map<String, Object>> pollutantDataList = document.get(pollutantDataKey, List.class);
            Double winddirection = null;
            for (Map<String, Object> dataMap : pollutantDataList) {//根据污染物code获取污染物值
                if ((CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()).equals(dataMap.get("PollutantCode"))) {
                    winddirection = dataMap.get("AvgStrength") != null ? Double.parseDouble(dataMap.get("AvgStrength").toString()) : null;
                    break;
                }
            }
            if (winddirection != null) {
                String winddirectionname = DataFormatUtil.windDirectionSwitch(winddirection, "name");
                if (objmap.get(mn) != null) {
                    Map<String, Object> map = (Map<String, Object>) objmap.get(mn);//该点位的所有方位数据
                    if (map.get(winddirectionname) != null) {
                        Map<String, Object> valuemap = (Map<String, Object>) map.get(winddirectionname);
                        valuemap.put("winddirection", Double.parseDouble(valuemap.get("winddirection").toString()) + winddirection);
                        valuemap.put("num", Integer.parseInt(valuemap.get("num").toString()) + 1);
                    } else {
                        Map<String, Object> valuemap = new HashMap<>();
                        valuemap.put("winddirection", winddirection);
                        valuemap.put("num", 1);
                        map.put(winddirectionname, valuemap);
                    }
                } else {
                    Map<String, Object> map = new HashMap<>();//该点位的所有方位数据
                    Map<String, Object> valuemap = new HashMap<>();
                    valuemap.put("winddirection", winddirection);
                    valuemap.put("num", 1);
                    map.put(winddirectionname, valuemap);
                    objmap.put(mn, map);
                }

            }
        }
        for (Map.Entry<String, Object> entry : objmap.entrySet()) {
            //System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
            Map<String, Object> valuemap = (Map<String, Object>) entry.getValue();
            Map<String, Object> resultmap = new HashMap<>();
            int num = 0;
            for (Map.Entry<String, Object> obj : valuemap.entrySet()) {
                Map<String, Object> map = (Map<String, Object>) obj.getValue();
                if (map.get("num") != null && Integer.parseInt(map.get("num").toString()) > num) {
                    num = Integer.parseInt(map.get("num").toString());
                    resultmap = map;
                }
            }
            if (resultmap != null && resultmap.size() > 0) {
                result.put(entry.getKey(), String.format("%.2f", Double.parseDouble(resultmap.get("winddirection").toString()) / Double.parseDouble(resultmap.get("num").toString())));
            }
        }
        return result;
    }


    /**
     * @author: xsm
     * @date: 2019/11/12 0012 下午 6:23
     * @Description: 获取溯源监测点在某个时间段内各污染物预警、超限、异常的条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countTraceSourcePointPollutantAlarmNumByParamMap(String dgimn, List<Map<String, Object>> pollutants, String starttime, String endtime) {
        //去MongoDB中查询数据
        List<Map<String, Object>> pollutantvalue = new ArrayList<Map<String, Object>>();
        Date startDate = DataFormatUtil.getDateYMDHMS(starttime + ":00");
        Date endDate = DataFormatUtil.getDateYMDHMS(endtime + ":59");
        List<String> pollutantlist = new ArrayList<>();
        Map<String, Object> pollutantmap = new HashMap<>();
        Map<String, Object> unitmap = new HashMap<>();
        for (Map<String, Object> map : pollutants) {
            pollutantlist.add(map.get("code").toString());
            pollutantmap.put(map.get("code").toString(), map.get("name"));
            unitmap.put(map.get("code").toString(), map.get("pollutantunit"));
        }
        //超阈值
        Map<String, Object> earlyData = countOneMonitorPointAlarmDataNumByParamMap(dgimn, pollutantlist, startDate, endDate, "EarlyWarnTime", "EarlyWarnData");
        //超限
        Map<String, Object> overData = countOneMonitorPointAlarmDataNumByParamMap(dgimn, pollutantlist, startDate, endDate, "OverTime", "OverData");
        //异常
        Map<String, Object> exceptionData = countOneMonitorPointAlarmDataNumByParamMap(dgimn, pollutantlist, startDate, endDate, "ExceptionTime", "ExceptionData");
        for (String code : pollutantlist) {
            Map<String, Object> map = new HashMap<>();
            map.put("pollutantcode", code);
            map.put("pollutantname", pollutantmap.get(code));
            map.put("pollutantunit", unitmap.get(code));
            map.put("earlydata", earlyData.get(code));
            map.put("overdata", overData.get(code));
            map.put("exceptiondata", exceptionData.get(code));
            pollutantvalue.add(map);
        }
        return pollutantvalue;
    }


    @Override
    public List<Map<String, Object>> getOthorPointInfoByPointType(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getOthorPointInfoByPointType(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTraceSourceMonitorPointInfoByParam(Map<String, Object> paramMap) {
        return otherMonitorPointMapper.getTraceSourceMonitorPointInfoByParam(paramMap);
    }


    /**
     * @author: xsm
     * @date: 2019/11/12 0012 下午 5:30
     * @Description: 统计单个监测点在一段时间内预警，报警，异常条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> countOneMonitorPointAlarmDataNumByParamMap(String dgimn, List<String> pollutantlist, Date startDate, Date endDate, String timekey, String collection) {
        Map<String, Object> earlyData = new HashMap<>();
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").is(dgimn).and(timekey).gte(startDate).lte(endDate)));
        operations.add(Aggregation.match(Criteria.where("PollutantCode").in(pollutantlist)));
        operations.add(Aggregation.project("PollutantCode", "num"));
        GroupOperation group = group("PollutantCode").count().as("num");
        operations.add(group);

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> documents = results.getMappedResults();
        if (documents.size() > 0) {
            for (Document document : documents) {
                int num = document.getInteger("num");
                earlyData.put(document.getString("_id"), num);
            }
        }
        return earlyData;
    }

    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 11:20
     * @Description: 根据监测时间和污染物获取溯源监测点的风向、风速和污染物浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getMinuteMonitorDataAndWeatherData(String monitortime, Set<String> airmns, Set<String> othermns, String pollutantcode, List<Map<String, Object>> mns, List<Map<String, Object>> colorDataList) {
        //构建Mongdb查询条件
        Date startDate = DataFormatUtil.getDateYMDHMS(monitortime + ":00");
        Date endDate = DataFormatUtil.getDateYMDHMS(monitortime + ":59");
        //从枚举类中获取风速风向的编码
        List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode(), CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(airmns));
        query.addCriteria(Criteria.where("MinuteDataList.PollutantCode").in(pollutantcodes));
        query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        List<Document> airdocuments = mongoTemplate.find(query, Document.class, "MinuteData");
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("DataGatherCode").in(othermns));
        query2.addCriteria(Criteria.where("MinuteDataList.PollutantCode").is(pollutantcode));
        query2.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
        List<Document> documents = mongoTemplate.find(query2, Document.class, "MinuteData");
        String SCValueKey = "AvgStrength";
        for (Map<String, Object> map : mns) {
            List<Map<String, Object>> pollutantList;
            if (map.get("airmn") != null) {
                Double windspeed = null;
                Double winddirection = null;
                for (Document obj : airdocuments) {//风速风向
                    if ((map.get("airmn").toString()).equals(obj.getString("DataGatherCode"))) {//MN号相等
                        pollutantList = (List<Map<String, Object>>) obj.get("MinuteDataList");
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                windspeed = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                break;
                            }
                        }
                        for (Map<String, Object> pollutant : pollutantList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(pollutant.get("PollutantCode"))) {
                                winddirection = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                break;
                            }
                        }
                    }
                }
                if (winddirection != null) {
                    map.put("winddirection", winddirection);
                    map.put("winddirectionname", DataFormatUtil.windDirectionSwitch(winddirection, "name"));
                    map.put("winddirectioncode", DataFormatUtil.windDirectionSwitch(winddirection, "code"));
                } else {
                    map.put("winddirection", null);
                    map.put("winddirectionname", winddirection);
                }
                map.put("windspeed", windspeed);
            } else {
                map.put("winddirectionname", "");
                map.put("winddirectioncode", "");
                map.put("windspeed", "");
            }
            String value = "-";
            String isOver = "";
            String isException = "";
            Boolean isOverStandard;
            for (Document obj : documents) {//监测值
                if ((map.get("DGIMN").toString()).equals(obj.getString("DataGatherCode"))) {//MN号相等
                    pollutantList = (List<Map<String, Object>>) obj.get("MinuteDataList");
                    for (Map<String, Object> pollutant : pollutantList) {
                        if (pollutantcode.equals(pollutant.get("PollutantCode"))) {
                            value = pollutant.get(SCValueKey) != null ? pollutant.get(SCValueKey).toString() : "";
                            isOver = pollutant.get("IsOver") != null ? pollutant.get("IsOver").toString() : "-1";
                            isException = pollutant.get("IsException") != null ? pollutant.get("IsException").toString() : "0";
                            isOverStandard = pollutant.get("IsOverStandard") != null ? Boolean.parseBoolean(pollutant.get("IsOverStandard").toString()) : false;
                            if (isOverStandard) {
                                isOver = "4";
                            }
                            break;
                        }
                    }
                }
            }
            map.put("alarmlevel", isOver);
            map.put("isException", isException);
            map.put("monitorvalue", value);
            map.put("pollutantcode", pollutantcode);
            if (!value.equals("-")&&map.get("FK_MonitorPointTypeCode")!=null){
                Double valueD = Double.parseDouble(value);
                Integer type = Integer.parseInt(map.get("FK_MonitorPointTypeCode").toString());
                map.put("colorvalue", getColorValue(type,valueD, colorDataList));
            }else {
                map.put("colorvalue", "");
            }
        }
        return mns;
    }

    /**
     * @author: xsm
     * @date: 2022/04/13 18:39
     * @Description: 获取溯源列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getEntTraceSourceListDataByParamMap(Map<String, Object> paramMap) {
       Map<String,Object> result = new HashMap<>();
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Query query = new Query();
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        if (paramMap.get("starttime")!=null&&paramMap.get("endtime")!=null){
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime") + " 00:00:00");
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime") + " 23:59:59");
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
        }
        //总条数
        long totalCount = mongoTemplate.count(query, "TraceSourcePointData");
        pageEntity.setTotalCount(totalCount);
        //排序条件
        String orderBy = "MonitorTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("MonitorTime", "EntDataList", "PollutantCode", "Total"));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, "TraceSourcePointData", Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        result.put("total", pageEntity.getTotalCount());
       if (documents.size()>0){
           for (Document doc:documents ){
               Map<String,Object> map = new HashMap<>();
               map.put("monitortime",DataFormatUtil.getDateYMDHMS(doc.getDate("MonitorTime")));
               resultlist.add(map);
           }
       }
        result.put("datalist",resultlist);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2022/04/14 09:00
     * @Description: 获取溯源详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getEntTraceSourceDetailDataByParamMap(Map<String, Object> paramMap) {
        Map<String,Object> result = new HashMap<>();
        List<Map<String, Object>> pollutionDataList = pollutionMapper.getPollutionNameAndPkid(new HashMap<>());
        Map<String, Object> pollutionIdAndName = new HashMap<>();
        if (pollutionDataList.size() > 0) {
            for (Map<String, Object> pollutionData : pollutionDataList) {
               if (pollutionData.get("pollutionid")!=null) {
                   pollutionIdAndName.put(pollutionData.get("pollutionid").toString(), pollutionData.get("pollutionname"));
               }
            }
        }
        PageEntity<Document> pageEntity = new PageEntity<>();
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            pageEntity.setPageNum(Integer.parseInt(paramMap.get("pagenum").toString()));
            pageEntity.setPageSize(Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Query query = new Query();
        //列表查询条件set
        List<AggregationOperation> operations = new ArrayList<>();
        if (paramMap.get("monitortime")!=null&&paramMap.get("monitortime")!=null){
            Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("monitortime").toString());
            Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("monitortime").toString());
            query.addCriteria(Criteria.where("MonitorTime").gte(startDate).lte(endDate));
            operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
        }
        //总条数
        long totalCount = mongoTemplate.count(query, "TraceSourcePointData");
        pageEntity.setTotalCount(totalCount);
        //排序条件
        String orderBy = "MonitorTime";
        Sort.Direction direction = Sort.Direction.DESC;
        operations.add(Aggregation.sort(direction, orderBy.split(",")));
        operations.add(Aggregation.project("MonitorTime", "EntDataList", "PollutantCode", "Total"));
        //插入分页、排序条件
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
            operations.add(Aggregation.skip((long) (pageEntity.getPageNum() - 1) * pageEntity.getPageSize()));
            operations.add(Aggregation.limit(pageEntity.getPageSize()));
        }
        Aggregation aggregationquery = Aggregation.newAggregation(operations);
        AggregationResults<Document> resultdocument = mongoTemplate.aggregate(aggregationquery, "TraceSourcePointData", Document.class);
        List<Document> documents = resultdocument.getMappedResults();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        result.put("total", pageEntity.getTotalCount());
        if (documents.size()>0){
            for (Document doc:documents ){
                List<Document> polist = (List<Document>) doc.get("EntDataList");
                for (Document onedoc:polist){
                    Map<String,Object> map = new HashMap<>();
                    map.put("pollutionid",onedoc.getString("PollutionID"));
                    map.put("pollutionname",pollutionIdAndName.get(onedoc.getString("PollutionID"))!=null?pollutionIdAndName.get(onedoc.getString("PollutionID")):"");
                    map.put("value", DataFormatUtil.subZeroAndDot(onedoc.getString("MonitorValue")));
                    map.put("orderkey", DataFormatUtil.subZeroAndDot(onedoc.getString("Proportion")));
                    map.put("proportion", DataFormatUtil.subZeroAndDot(onedoc.getString("Proportion"))+"%");
                    resultlist.add(map);
                }
            }
        }
        if (resultlist.size()>0){
            //排序
            Comparator<Object> compare1 = Comparator.comparing(m -> Double.valueOf(((Map) m).get("orderkey").toString())).reversed();
            Comparator<Object> compare2 = Comparator.comparing(m -> Double.valueOf(((Map) m).get("value").toString())).reversed();
            Comparator<Object> finalComparator = compare1.thenComparing(compare2);
            resultlist = resultlist.stream().sorted(finalComparator).collect(Collectors.toList());
        }
        result.put("datalist",resultlist);
        return result;
    }

    public static String getColorValue(Integer type ,Double value, List<Map<String, Object>> colorDataList) {
        String color = "";
        if (value != null) {
            Double minValue;
            Double maxValue;
            for (Map<String, Object> colorData : colorDataList) {
                if (colorData.get("monitorpointtype")!=null&&type==Integer.parseInt(colorData.get("monitorpointtype").toString())){
                    if (colorData.get("StandardMinValue") != null && colorData.get("StandardMaxValue") != null) {
                        minValue = Double.parseDouble(colorData.get("StandardMinValue").toString());
                        maxValue = Double.parseDouble(colorData.get("StandardMaxValue").toString());
                        if (value >= minValue && maxValue > value) {
                            color = colorData.get("ColourValue") != null ? colorData.get("ColourValue").toString() : "";
                        }
                    }
                }
            }
        }
        return color;
    }
}
