package com.tjpu.sp.service.impl.environmentalprotection.online;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.service.environmentalprotection.online.OnlineMeteoService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OnlineMeteoServiceImpl implements OnlineMeteoService {
    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    //小时在线
    private final String hourCollection = "HourData";
    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;
    @Autowired
    private OnlineServiceImpl onlineServiceImpl;


    /**
     * @author: xsm
     * @date: 2020/4/2 0002 下午 2:39
     * @Description: 获取气象监测点最新小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getMeteoMonitorPointLastHourData(Set<String> mns) {
        Map<String, Object> result =new HashMap<>();
        List<Map<String,Object>> resultlist =new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("DataGatherCode").in(mns));
        query.with(new Sort(Sort.Direction.DESC, "MonitorTime"));
        Document document = mongoTemplate.findOne(query, Document.class, hourCollection);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttype", 52);
        List<Map<String, Object>> pollutants = pollutantFactorMapper.getPollutantsByCodesAndType(paramMap);
        Map<String, Object> codenamemap = new HashMap<>();
        Map<String, Object> codeunitmap = new HashMap<>();
        List<String> pollutantcodes = new ArrayList<>();
        for (Map<String, Object> map : pollutants) {
            codenamemap.put(map.get("code").toString(), map.get("name"));
            codeunitmap.put(map.get("code").toString(), map.get("unit"));
            pollutantcodes.add(map.get("code").toString());
        }
        String[] directCodeList = DataFormatUtil.directCode;
        String[] directNameList = DataFormatUtil.directName;
        Map<String, String> windcodename = new HashMap<>();
        for (int i = 0; i < directNameList.length; i++) {
            windcodename.put(directCodeList[i],directNameList[i]);
        }
        if (document != null) {
            Query querytwo = new Query();
            querytwo.addCriteria(Criteria.where("DataGatherCode").in(mns));
            querytwo.addCriteria(Criteria.where("MonitorTime").gte(document.getDate("MonitorTime")).lte(document.getDate("MonitorTime")));
            List<Document> documents = mongoTemplate.find(query, Document.class, "HourData");
            String monitortime=DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            result.put("monitortime",monitortime);
            if (documents.size() > 0) {
                for (String code : pollutantcodes) {
                    Map<String,Object> objmap =new HashMap<>();
                    objmap.put("code",code);
                    objmap.put("name",codenamemap.get(code));
                    objmap.put("unit",codeunitmap.get(code));
                    if (code.equals(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode())){
                        Map<String,Integer> windmap =new HashMap<>();
                        for (Document obj : documents) {
                            List<Map<String, Object>> pollutantDataList = obj.get("HourDataList", List.class);
                            for (Map<String, Object> map : pollutantDataList) {
                                if (code.equals(map.get("PollutantCode").toString())) {
                                    if (map.get("AvgStrength") != null && !"".equals(map.get("AvgStrength").toString())) {
                                        if (Double.parseDouble(map.get("AvgStrength").toString()) > 0 || Double.parseDouble(map.get("AvgStrength").toString()) < 0) {
                                            String windDirection =  DataFormatUtil.windDirectionSwitch(Double.parseDouble(map.get("AvgStrength").toString()),"code");
                                            if (windmap.get(windDirection) !=null){
                                                windmap.put(windDirection,windmap.get(windDirection)+1);
                                            }else{
                                                windmap.put(windDirection,1);
                                            }
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                        String wind ="";
                        if (windmap!=null&&windmap.size()>0){

                            int windnum=0;
                            for (String key:windmap.keySet()){
                                if (windnum!=0) {
                                    if (windmap.get(key)>windnum) {
                                        windnum = windmap.get(key);
                                        wind = key;
                                    }
                                }else{
                                    windnum = windmap.get(key);
                                    wind=key;
                                }
                            }
                        }
                        objmap.put("value",windcodename.get(wind));
                    }else {
                        int num = 0;
                        double value = 0d;
                        for (Document obj : documents) {
                            List<Map<String, Object>> pollutantDataList = obj.get("HourDataList", List.class);
                            for (Map<String, Object> map : pollutantDataList) {
                                if (code.equals(map.get("PollutantCode").toString())) {
                                    if (map.get("AvgStrength") != null && !"".equals(map.get("AvgStrength").toString())) {
                                        if (Double.parseDouble(map.get("AvgStrength").toString()) > 0 || Double.parseDouble(map.get("AvgStrength").toString()) < 0) {
                                            value = value+Double.parseDouble(map.get("AvgStrength").toString());
                                            num += 1;
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                        double avgvalue = 0d;
                        if (num > 0) {
                            avgvalue = value / num;
                        }
                        objmap.put("value",avgvalue!=0d?DataFormatUtil.formatDoubleSaveTwo(avgvalue):"");
                    }
                    resultlist.add(objmap);
                }
            }
            result.put("valuedata",resultlist);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getParkWindData(String collection, List<Document> documents) {
        List<Map<String, Object>> parkList = new ArrayList<>();
        Set<String> times =new HashSet<>();
        String[] directCodeList = DataFormatUtil.directCode;
        String[] directNameList = DataFormatUtil.directName;
        Map<String, String> windcodename = new HashMap<>();
        for (int i = 0; i < directNameList.length; i++) {
            windcodename.put(directCodeList[i],directNameList[i]);
        }
        String pollutantDataKey ="";
        if (collection.equals("HourData")) {
            pollutantDataKey = "HourDataList";
        } else if (collection.equals("DayData")) {
            pollutantDataKey = "DayDataList";
        } else if (collection.equals("MinuteData")) {
            pollutantDataKey = "MinuteDataList";
        }
        for (Document document : documents) {
            String monitortime="";
            if (collection.equals("HourData")) {
                monitortime=DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
            } else if (collection.equals("DayData")) {
                monitortime=DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
            } else if (collection.equals("MinuteData")) {
                monitortime=DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
            }
            if (times.contains(monitortime)) {//判断是否类型重复
                continue;//重复
            } else {//不重复
                Map<String,Object> resultmap = new HashMap<>();
                resultmap.put("monitortime", monitortime);
                times.add(monitortime);
                int num = 0;
                double value = 0d;
                Map<String, Integer> windmap = new HashMap<>();
                Map<String, Object> windvaluemap = new HashMap<>();
                for (Document obj : documents) {
                    String monitortimetwo = "";
                    if (collection.equals("HourData")) {
                        monitortimetwo=DataFormatUtil.getDateYMDH(obj.getDate("MonitorTime"));
                    } else if (collection.equals("DayData")) {
                        monitortimetwo=DataFormatUtil.getDateYMD(obj.getDate("MonitorTime"));
                    }else if (collection.equals("MinuteData")) {
                        monitortimetwo=DataFormatUtil.getDateYMDHM(obj.getDate("MonitorTime"));
                    }
                    if (monitortime.equals(monitortimetwo)) {
                        List<Map<String, Object>> pollutantDataList = obj.get(pollutantDataKey, List.class);
                        for (Map<String, Object> map : pollutantDataList) {
                            if (CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode().equals(map.get("PollutantCode"))) {
                                if (map.get("AvgStrength") != null && !"".equals(map.get("AvgStrength").toString())) {
                                    if (Double.parseDouble(map.get("AvgStrength").toString()) > 0 || Double.parseDouble(map.get("AvgStrength").toString()) < 0) {
                                        value = value+Double.parseDouble(map.get("AvgStrength").toString());
                                        num += 1;
                                    }
                                }
                            }
                            if (CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode().equals(map.get("PollutantCode"))) {
                                if (map.get("AvgStrength") != null && !"".equals(map.get("AvgStrength").toString())) {
                                    if (Double.parseDouble(map.get("AvgStrength").toString()) > 0 || Double.parseDouble(map.get("AvgStrength").toString()) < 0) {
                                        String windDirection = DataFormatUtil.windDirectionSwitch(Double.parseDouble(map.get("AvgStrength").toString()), "code");
                                        windvaluemap.put(windDirection,map.get("AvgStrength"));
                                        if (windmap.get(windDirection) != null) {
                                            windmap.put(windDirection, windmap.get(windDirection) + 1);
                                        } else {
                                            windmap.put(windDirection, 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                String wind = "";
                if (windmap != null && windmap.size() > 0) {
                    int windnum = 0;
                    for (String key : windmap.keySet()) {
                        if (windnum != 0) {
                            if (windmap.get(key) > windnum) {
                                windnum = windmap.get(key);
                                wind = key;
                            }
                        } else {
                            windnum = windmap.get(key);
                            wind = key;
                        }
                    }
                }
                double avgvalue = 0d;
                if (num > 0) {
                    avgvalue = value / num;
                }
                if (!"".equals(wind)) {
                    resultmap.put("winddirectionname", windcodename.get(wind));
                    resultmap.put("winddirectioncode", wind);
                    resultmap.put("winddirectionvalue", windvaluemap.get(wind));
                    resultmap.put("windspeed", avgvalue != 0d ? DataFormatUtil.formatDoubleSaveTwo(avgvalue) : "");
                    parkList.add(resultmap);
                }
            }
        }
        return parkList;
    }

    /**
     * @author: xsm
     * @date: 2020/4/4 12:26
     * @Description: 通过自定义参数统计气象监测点位的监测数据及气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> countWeatherAndMeteoMonitorPointDataByParamMap(List<String> mns, List<String> pollutantcodes, String starttime, String endtime) {
        try {
            //根据污染物编码和监测点类型去获取对应的污染物信息
            Date startDate = null;
            Date endDate = null;
            startDate = DataFormatUtil.getDateYMDH(starttime);
            endDate = DataFormatUtil.getDateYMDH(endtime);
            //获取点位下时段各因子浓度
            List<Map<String, Object>> concentrationlist = getParkPointConcentrationData(mns, pollutantcodes, starttime, endtime);
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> weathermap = getParkPointWeatherData(mns, pollutantcodes, starttime, endtime);
            result.put("weatherlist", weathermap);
            result.put("concentrationlist", concentrationlist);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/6/26 7:52
     * @Description: 获取监测点下因子监测浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getParkPointConcentrationData(List<String> mns,  List<String> pollutantlist, String starttime, String  endtime) {
        //去MongoDB中查询数据
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<String> timelist = DataFormatUtil.getYMDHBetween(starttime, endtime);
        timelist.add(endtime);
        Date startDate = null;
        Date endDate = null;
        startDate = DataFormatUtil.getDateYMDH(starttime);
        endDate = DataFormatUtil.getDateYMDH(endtime);
        Aggregation aggregation = newAggregation(
                match(Criteria.where("DataGatherCode").in(mns)
                        .and("MonitorTime").gte(startDate).lte(endDate)
                        .and("HourDataList.PollutantCode").in(pollutantlist)),
                unwind("HourDataList"),
                project("MonitorTime", "DataGatherCode")
                        .and("HourDataList.AvgStrength").as("AvgStrength")
                        .and("HourDataList.PollutantCode").as("PollutantCode")


        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, hourCollection, Document.class);
        List<Document> documents = aggregationResults.getMappedResults();
        //构建Mongdb查询条件
        if (documents.size() > 0) {//判断查询数据是否为空
            for (String hourtime:timelist){
                Map<String,Object> valuemap =new HashMap<>();
                List<Map<String,Object>> listmap=new ArrayList<>();
            for(String code :pollutantlist){
            int num =0;
            double value = 0d;
            for (Document document : documents) {
                String monitortime = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                        if (hourtime.equals(monitortime)&&code.equals(document.get("PollutantCode"))) {
                            if (document.get("AvgStrength") != null&&!"".equals(document.get("AvgStrength").toString())) {
                                value = value+ Double.parseDouble(document.get("AvgStrength").toString());
                                num +=1;
                                break;
                            }
                        }
                    }
                    String avgvalue ="";
            if (num>0){
                avgvalue = DataFormatUtil.formatDoubleSaveTwo(value/num )+"";
            }
                Map<String, Object> objmap = new HashMap<>();
                    objmap.put("pollutantcode", code);
                    objmap.put("pollutantvalue", avgvalue);
                listmap.add(objmap);
                }
                valuemap.put("monitortime",hourtime);
                valuemap.put("pollutantdata",listmap);
                result.add(valuemap);
            }
        }
        return result;
    }

    /**
     * @author: xsm
     * @date: 2020/4/6 7:52
     * @Description: 获取监测点气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public List<Map<String, Object>> getParkPointWeatherData(List<String> mns,  List<String> pollutantlist, String starttime, String  endtime) {
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtils.isNotBlank(starttime)) {
            starttime = starttime + ":00:00";
            paramMap.put("starttime", starttime);
        }
        if (StringUtils.isNotBlank(endtime)) {
            endtime = endtime + ":59:59";
            paramMap.put("endtime", endtime);
        }
        paramMap.put("sort", "asc");
        paramMap.put("mns", mns);
        paramMap.put("pollutantcodes", pollutantlist);
        paramMap.put("collection", MongoDataUtils.getCollectionByDataMark(3));
        List<Document> documents = onlineServiceImpl.getMonitorDataByParamMap(paramMap);
        List<Map<String, Object>> parkList =getParkWindData(MongoDataUtils.getCollectionByDataMark(3),documents);
        return parkList;
    }


}
