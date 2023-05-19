package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.StationHourAQIDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.*;

/**
 * @author: chengzq
 * @date: 2019/6/04 0021 09:48
 * @Description: 空气站点小时数据控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("airStationHour")
public class AirStationHourDataController {
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private PollutionTraceSourceService pollutionTraceSourceService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    /**
     * 大气监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();
    @Autowired
    private OnlineService onlineService;
    private final String hourCollection = "StationHourAQIData";
    private final String db_hourData = "HourData";


    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;
    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 上午 10:14
     * @Description: 通过监测时间，分页信息获取空气站小时数据列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getAirStationHourDataListByParams", method = RequestMethod.POST)
    public Object getAirStationHourDataListByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                    @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                    @RequestJson(value = "monitortime", required = false) String monitortime
    ) {
        try {
            StationHourAQIDataVO stationHourAQIDataVO = new StationHourAQIDataVO();
            List<Map<String, Object>> dataList = new ArrayList<>();

            //设置查询时间
            if (StringUtils.isNotBlank(monitortime)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("starttime", monitortime);
                paramMap.put("endtime", monitortime);
                stationHourAQIDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
            }
            Map<String, Object> resultMap = new HashMap<>();
            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            //设置分页
            if (pagenum != null && pagesize != null) {
                mongoSearchEntity.setPage(pagenum - 1);
                mongoSearchEntity.setSize(pagesize);
            }
            //按时间,aqi倒序
            mongoSearchEntity.setSortorder(2);
            List<String> sortNames = new ArrayList<>();
            sortNames.add("MonitorTime");
            sortNames.add("AQI");
            mongoSearchEntity.setSortname(sortNames);

            Map<String, Object> params = new HashMap<>();
            //获取污染物码表信息
            params.put("pollutanttype", 5);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(params);
            //获取所有空气站信息
            List<Map<String, Object>> allAirMonitorStation = airMonitorStationService.getAllAirMonitorStation(new HashMap<>());
            Set<String> collect1 = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toSet());
            stationHourAQIDataVO.setStationCode(Joiner.on(",").join(collect1));
            List<StationHourAQIDataVO> listWithPageByParam = mongoBaseService.getListWithPageByParam(stationHourAQIDataVO, mongoSearchEntity, "StationHourAQIData", "yyyy-MM-dd HH");
            for (StationHourAQIDataVO hourAQIDataVO : listWithPageByParam) {
                List<Map<String, Object>> datas = hourAQIDataVO.getDataList();
                Map<String, Object> dataMap = new HashMap<>();

                for (Map<String, Object> map : datas) {
                    String pollutantCode = map.get("PollutantCode").toString();
                    String stationCode = hourAQIDataVO.getStationCode();
                    //将污染物code设置为name
                    pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(pollutantCode)).
                            peek(m -> dataMap.put(m.get("name").toString().toLowerCase(), map.get("Strength"))).collect(Collectors.toList());
                    //将首要污染物code设置为name
                    pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(hourAQIDataVO.getPrimaryPollutant()))
                            .map(m -> dataMap.put("pimarypllutant", m.get("name").toString().toLowerCase())).collect(Collectors.toList());
                    allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null && m.get("DGIMN").toString().equals(stationCode)).
                            peek(m -> dataMap.put("monitorpointname", m.get("MonitorPointName"))).collect(Collectors.toList());
                    dataMap.put("aqi", hourAQIDataVO.getaQi());
                    dataMap.put("quality", hourAQIDataVO.getAirQuality());
                    dataMap.put("monitorTime", OverAlarmController.format(hourAQIDataVO.getMonitorTime(), "yyyy-MM-dd HH"));
                }
                if (dataMap.get("aqi") != null) {
                    dataList.add(dataMap);
                }
            }
            dataList = dataList.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("aqi").toString())).reversed()).collect(Collectors.toList());
            resultMap.put("total", listWithPageByParam.size());
            resultMap.put("datalist", dataList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 上午 10:14
     * @Description: 通过监测时间，分页信息导出空气站小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/exportAirStationHourDataByParams", method = RequestMethod.POST)
    public void exportAirStationHourDataByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                 @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                 @RequestJson(value = "monitortime", required = false) String monitortime,
                                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            List headers = new ArrayList<>();
            headers.add("站点名称");
            headers.add("AQI");
            headers.add("空气质量");
            headers.add("PM2.5（ug/m3）");
            headers.add("PM10（ug/m3）");
            headers.add("SO2（ug/m3）");
            headers.add("NO2（ug/m3）");
            headers.add("O3（ug/m3）");
            headers.add("CO（mg/m3）");
            headers.add("首要污染物");

            List headersField = new ArrayList<>();
            headersField.add("monitorpointname");
            headersField.add("aqi");
            headersField.add("quality");
            headersField.add("pm2.5");
            headersField.add("pm10");
            headersField.add("so2");
            headersField.add("no2");
            headersField.add("o3");
            headersField.add("co");
            headersField.add("pimarypllutant");

            JSONObject jsonObject = JSONObject.fromObject(getAirStationHourDataListByParams(pagesize, pagenum, monitortime));
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object data1 = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(data1);

            /*List<Map<String,Object>> list=new ArrayList<>(jsonArray);
            //首要污染物有多个是个集合，需要将集合内的所有元素拼接成字符串
            for (Map<String, Object> stringObjectMap : list) {
                if(stringObjectMap.get("primarypollutant")!=null){
                    JSONArray primarypollutant = JSONArray.fromObject(stringObjectMap.get("primarypollutant"));
                    List<String> pollutant=new ArrayList<>(primarypollutant);
                    String join = String.join(",", pollutant);
                    stringObjectMap.put("primarypollutant",join);
                }
            }*/
            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "yyyy-MM-dd HH");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("AQI实时排行", response, request, bytesForWorkBook);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件获取空气站点小时气象数据（相对湿度，降水量，大气压，风速）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getAirStationHourWeatherDataByParams", method = RequestMethod.POST)
    public Object getAirStationDayWeatherDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> times = DataFormatUtil.getYMDHBetween(starttime, endtime);
            times.add(endtime);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorPointTypeCode, new HashMap<>());
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + ":00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = endtime + ":59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("sort", "asc");
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
            }
            List<String> pollutantcodes = Arrays.asList(
                    HumidityEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.RainfallEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.PressureEnum.getCode(),
                    TemperatureEnum.getCode(),
                    WindDirectionEnum.getCode(),
                    WindSpeedEnum.getCode());
            paramMap.put("pollutantcodes", pollutantcodes);
            String collection = MongoDataUtils.getCollectionByDataMark(3);
            paramMap.put("collection", collection);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = MongoDataUtils.getWeatherDataList(documents, collection, times, pollutantcodes);
            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/12/16 0026 下午 3:51
     * @Description: 获取站点小时空气质量状况（六参数、aqi、温度、风向、风速、湿度、首要污染物、等级）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getStationHourWeatherData", method = RequestMethod.POST)
    public Object getAirStationDayWeatherDataByParams(
            @RequestJson(value = "dgimn") String dgimn,
            @RequestJson(value = "monitortime") String monitortime) {

        try {
            Map<String,Object> resultMap = new LinkedHashMap<>();
            String startTime = monitortime + ":00:00";
            String endTime = monitortime + ":59:59";
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("collection", db_hourData);
            paramMap.put("leftCollection", hourCollection);

            List<Document> documents = getAirHourLookupData(paramMap);
            if (documents.size()>0){
                Document document = documents.get(0);
                paramMap.put("pollutanttype",monitorPointTypeCode);
                List<Map<String,Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                Map<String,Object> codeAndName = setCodeAndName(pollutants);
                List<Document> pollutantList = document.get("HourDataList",List.class);
                List<String> sixCode = CommonTypeEnum.getSixIndexList();
                String pollutantCode;
                String value;
                for (Document pollutant:pollutantList){
                    pollutantCode = pollutant.get("PollutantCode")!=null?pollutant.getString("PollutantCode"):"";
                    value = pollutant.get("AvgStrength")!=null?pollutant.getString("AvgStrength"):"0";
                    if (sixCode.contains(pollutantCode)){
                        if (pollutantCode.equals(CommonTypeEnum.AirCommonSixIndexEnum.AIR_COMMON_SIX_INDEX_CO.getCode())){
                            value = DataFormatUtil.SaveOneAndSubZero(Double.parseDouble(value));
                        }else {
                            value = DataFormatUtil.formatDoubleSaveNo(Double.parseDouble(value));
                        }
                        resultMap.put(codeAndName.get(pollutantCode).toString().replace(".",""),value);
                    }else if (pollutantCode.equals(WindSpeedEnum.getCode())){
                        resultMap.put("windspeed",value);
                    }else if (pollutantCode.equals(WindDirectionEnum.getCode())){
                        resultMap.put("winddirection",value);
                    }else if (pollutantCode.equals(TemperatureEnum.getCode())){
                        resultMap.put("temperature",value);
                    }else if (pollutantCode.equals(HumidityEnum.getCode())){
                        resultMap.put("humidity",value);
                    }
                }
                List<Document> stationDataList = document.get("StationData",List.class);
                if (stationDataList.size()>0){
                    Document stationData = stationDataList.get(0);
                    resultMap.put("aqi",stationData.get("AQI"));
                    resultMap.put("airquality",stationData.get("AirQuality"));

                    String primarypollutant = stationData.getString("PrimaryPollutant");
                    if (StringUtils.isNotBlank(primarypollutant)){
                        primarypollutant = getPrimaryPollutant(codeAndName,primarypollutant);
                    }
                    resultMap.put("primarypollutant",primarypollutant);
                    resultMap.put("aqi",stationData.get("AQI"));
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String getPrimaryPollutant(Map<String, Object> codeAndName, Object primaryPollutant) {
        String primaryPollutants = "";
        if (primaryPollutant != null) {
            String tempValue = primaryPollutant.toString();
            if (tempValue.indexOf(",") > -1) {
                String[] tempValues = tempValue.split(",");
                for (int i = 0; i < tempValues.length; i++) {
                    primaryPollutants += codeAndName.get(tempValues[i]) + ",";
                }
                primaryPollutants = primaryPollutants.substring(0, primaryPollutants.length() - 1);
            } else {
                primaryPollutants = codeAndName.get(tempValue) != null ? codeAndName.get(tempValue).toString() : "";
            }
        }
        return primaryPollutants;
    }

    private Map<String,Object> setCodeAndName(List<Map<String, Object>> pollutants) {
        Map<String,Object> codeAndName = new HashMap<>();
        for (Map<String,Object> pollutant:pollutants){
            codeAndName.put(pollutant.get("code")+"",pollutant.get("name"));
        }
        return codeAndName;
    }

    /**
     * @author: lip
     * @date: 2020/12/16 0016 上午 9:21
     * @Description: 获取小时空气质量关联查询数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Document> getAirHourLookupData(Map<String, Object> paramMap) {
        List<AggregationOperation> operations = new ArrayList<>();
        Date startDate = DataFormatUtil.getDateYMDHMS(paramMap.get("starttime").toString());
        Date endDate = DataFormatUtil.getDateYMDHMS(paramMap.get("endtime").toString());
        operations.add(Aggregation.match(Criteria.where("MonitorTime").gte(startDate).lte(endDate)));
        List<String> mns = (List<String>) paramMap.get("mns");
        operations.add(Aggregation.match(Criteria.where("DataGatherCode").in(mns)));
        String collection = paramMap.get("collection").toString();
        String leftCollection = paramMap.get("leftCollection").toString();
        LookupOperation lookupOperation = LookupOperation.newLookup().from(leftCollection).localField("MonitorTime").foreignField("MonitorTime").as("StationData");
        operations.add(lookupOperation);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> pageResults = mongoTemplate.aggregate(aggregation, collection, Document.class);
        List<Document> listItems = pageResults.getMappedResults();
        return listItems;

    }


    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件统计空气站点小时风向图表数据（风速，风向，频次）
     * @updateUser: xsm
     * @updateDate: 2020/06/10 0010 下午 4:14
     * @updateDescription: 根据气象站点MN去查询风向信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countHourWindChartDataByParams", method = RequestMethod.POST)
    public Object countHourWindChartDataByParams(
            @RequestJson(value = "dgimn", required = false) String dgimn,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //获取关联信息
            List<String> mns = new ArrayList<>();
            if (dgimn != null && !"".equals(dgimn)) {//有气象点MN号则根据MN号去查
                mns.add(dgimn);
            } else {
                paramMap.put("monitorpointid", monitorpointid);
                paramMap.put("pointtype", monitorpointtype);
                //获取关联信息
                String mn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
                if (StringUtils.isNotBlank(mn)) { //有，使用关联信息id
                    mns.add(mn);
                } else {  //无，使用当前id
                    mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorpointtype, new HashMap<>());
                }
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
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
                String collection = MongoDataUtils.getCollectionByDataMark(3);
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("collection", collection);
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = MongoDataUtils.countWindChartData(documents, collection);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件统计空气站点分钟风向图表数据（风速，风向，频次）
     * @updateUser: xsm
     * @updateDate: 2020/06/10 0010 下午 4:14
     * @updateDescription: 根据气象站点MN去查询风向信息
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countMinuteWindChartDataByParams", method = RequestMethod.POST)
    public Object countMinuteWindChartDataByParams(
            @RequestJson(value = "dgimn", required = false) String dgimn,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {
        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //获取关联信息
            List<String> mns = new ArrayList<>();
            if (dgimn != null && !"".equals(dgimn)) {//有气象点MN号则根据MN号去查
                mns.add(dgimn);
            } else {
                paramMap.put("monitorpointid", monitorpointid);
                paramMap.put("pointtype", monitorpointtype);
                //获取关联信息
                String mn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
                if (StringUtils.isNotBlank(mn)) { //有，使用关联信息id
                    mns.add(mn);
                } else {  //无，使用当前id
                    mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorpointtype, new HashMap<>());
                }
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                paramMap.put("mns", mns);
                String collection = MongoDataUtils.getCollectionByDataMark(2);
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("collection", collection);
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = MongoDataUtils.countWindChartData(documents, collection);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件统计空气站点小时风向列表数据（风速，风向，频次）
     * @updateUser: xsm
     * @updateDate: 2020/06/10
     * @updateDescription: 获取气象站点小时风向列表数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countAirStationHourWindListDataByParams", method = RequestMethod.POST)
    public Object countAirStationHourWindListDataByParams(
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) throws Exception {

        try {
            Map<String, Object> dataMap = new LinkedHashMap<>();
            //PageInfo<Map<String, Object>> pageInfo = airMonitorStationService.getOnlineAirStationInfoByParamMapForPage(pagesize, pagenum, new HashMap<>());
            PageInfo<Map<String, Object>> pageInfo = otherMonitorPointService.getOnlineMeteoInfoByParamMapForPage(pagesize, pagenum, new HashMap<>());
            List<Map<String, Object>> list = pageInfo.getList();
            if (list.size() > 0) {
                List<String> mns = new ArrayList<>();
                Map<String, Object> mnAndOutPutName = new HashMap<>();
                for (Map<String, Object> map : list) {
                    if (map.get("dgimn") != null) {
                        mns.add(map.get("dgimn").toString());
                        mnAndOutPutName.put(map.get("dgimn").toString(), map.get("monitorpointname"));
                    }
                }
                List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);
                hours.add(endtime);
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
                if (mns.size() > 0) {
                    paramMap.put("mns", mns);
                }
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(3);

                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = MongoDataUtils.countWindDataList(documents, collection, mnAndOutPutName, hours.size());
                dataMap.put("total", pageInfo.getTotal());
                dataMap.put("tablelistdata", dataList);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件获取站点(空气站点以及其他站点)小时风向数据（时间，风速，风向）
     * @updateUser: xsm
     * @updateDate: 2020/06/10 0010 下午6:18
     * @updateDescription: 根据气象站点MN号获取小时风向数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getHourWindDataByParams", method = RequestMethod.POST)
    public Object getHourWindDataByParams(
            @RequestJson(value = "dgimn", required = false) String dgimn,
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            if (dgimn != null && !"".equals(dgimn)) {//有气象点MN号则根据MN号去查
                mns.add(dgimn);
            } else {
                paramMap.put("monitorpointid", monitorpointid);
                paramMap.put("pointtype", monitorpointtype);
                //获取关联信息
                String mn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
                if (StringUtils.isNotBlank(mn)) { //有，使用关联信息id
                    mns.add(mn);
                } else {  //无，使用当前id
                    mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorpointtype, new HashMap<>());
                }
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(3);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = MongoDataUtils.getWindDataList(documents, collection);
            }

            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/7/27 0026 下午 10:51
     * @Description: 统计站点一段时间内小时各个风级次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countAirStationHourWindLevelDataByMonitorTimes", method = RequestMethod.POST)
    public Object countAirStationHourWindLevelDataByMonitorTimes(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();

            Map<String, String> mnAndOutputName = new HashMap<>();

            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(new ArrayList<>(), monitorPointTypeCode, mnAndOutputName);
            if (StringUtils.isNotBlank(starttime)) {
                starttime = starttime + " 00:00:00";
                paramMap.put("starttime", starttime);
            }
            if (StringUtils.isNotBlank(endtime)) {
                endtime = endtime + " 23:59:59";
                paramMap.put("endtime", endtime);
            }
            paramMap.put("sort", "asc");
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
            }
            List<String> pollutantcodes = Arrays.asList(WindSpeedEnum.getCode());
            paramMap.put("pollutantcodes", pollutantcodes);
            String collection = MongoDataUtils.getCollectionByDataMark(3);
            paramMap.put("collection", collection);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = MongoDataUtils.getWindLevelDataList(documents, collection, mnAndOutputName);
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件导出空气站点小时风向列表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/exportAirStationHourWindListDataByParams", method = RequestMethod.POST)
    public void exportAirStationHourWindListDataByParams(HttpServletRequest request, HttpServletResponse response,
                                                         @RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime) throws Exception {

        try {
            //表头
            List<Map<String, Object>> headers = MongoDataUtils.getHeaderData("小时数");
            //List<Map<String, Object>> list = airMonitorStationService.getOnlineAirStationInfoByParamMap(new HashMap<>());
            List<Map<String, Object>> list = otherMonitorPointService.getAllMonitorEnvironmentalMeteoAndStatusInfo();//气象
            if (list.size() > 0) {
                List<String> mns = new ArrayList<>();
                Map<String, Object> mnAndOutPutName = new HashMap<>();
                for (Map<String, Object> map : list) {
                    if (map.get("dgimn") != null) {
                        mns.add(map.get("dgimn").toString());
                        mnAndOutPutName.put(map.get("dgimn").toString(), map.get("monitorpointname"));
                    }
                }
                List<String> hours = DataFormatUtil.getYMDHBetween(starttime, endtime);
                hours.add(endtime);
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
                if (mns.size() > 0) {
                    paramMap.put("mns", mns);
                }
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(3);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                //List<Document> documents = airMonitorStationService.getAirStationMongodbDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = MongoDataUtils.countWindDataList(documents, collection, mnAndOutPutName, hours.size());
                String fileName = "小时风力统计报表" + new Date().getTime();
                ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headers, dataList, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/4 0004 上午 11:41
     * @Description: 统计站点小时常规六参数数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countStationHourCommonSixIndexByMonitorTimes", method = RequestMethod.POST)
    public Object countStationHourCommonSixIndexByMonitorTimes(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> airPoints = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> ymdhs = DataFormatUtil.getYMDHBetween(starttime, endtime);
            ymdhs.add(endtime);
            if (ymdhs.size() > 0 && airPoints.size() > 0) {
                List<String> mns = new ArrayList<>();
                Map<String, Object> mnAndId = new HashMap<>();
                Map<String, Object> mnAndName = new HashMap<>();
                String mn;
                for (Map<String, Object> map : airPoints) {
                    if (map.get("dgimn") != null && !mns.contains(map.get("dgimn"))) {
                        mn = map.get("dgimn").toString();
                        mns.add(mn);
                        mnAndId.put(mn, map.get("pk_id"));
                        mnAndName.put(mn, map.get("monitorpointname"));
                    }
                }

                List<String> codes = CommonTypeEnum.getSixIndexList();
                paramMap.put("starttime", starttime + ":00:00");
                paramMap.put("endtime", endtime + ":59:59");
                paramMap.put("pollutantcodes", codes);
                paramMap.put("mns", mns);
                paramMap.put("collection", hourCollection);
                List<Document> documents = airMonitorStationService.getAirStationMongodbDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    List<Map<String, Object>> pollutantList;
                    for (String code : codes) {
                        for (String ymdh : ymdhs) {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("pollutantcode", code);
                            map.put("pollutantname", CommonTypeEnum.AirCommonSixIndexEnum.getNameByCode(code));
                            map.put("monitortime", ymdh);
                            List<Map<String, Object>> pointdata = new ArrayList<>();
                            for (String mnKey : mns) {
                                Map<String, Object> pointMap = new HashMap<>();
                                pointMap.put("pointid", mnAndId.get(mnKey));
                                pointMap.put("pointname", mnAndName.get(mnKey));
                                for (Document document : documents) {
                                    if (ymdh.equals(DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")))) {
                                        if (code.equals("aqi")) {
                                            pointMap.put("monoitorvalue", document.get("AQI"));
                                            pointMap.put("iaqi", document.get("AQI"));
                                            break;
                                        } else {
                                            pollutantList = (List<Map<String, Object>>) document.get("DataList");
                                            for (Map<String, Object> pollutant : pollutantList) {
                                                if (code.equals(pollutant.get("PollutantCode"))) {
                                                    pointMap.put("monoitorvalue", pollutant.get("Strength"));
                                                    pointMap.put("iaqi", pollutant.get("IAQI"));
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                                pointdata.add(pointMap);
                            }
                            map.put("pointdata", pointdata);
                            dataList.add(map);
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/12/02 0002 上午 9:47
     * @Description: 自定义查询条件获取站点(空气站点以及其他站点)小时风向数据（时间，风速，风向）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getHourWindDataByParamMap", method = RequestMethod.POST)
    public Object getHourWindDataByParamMap(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("pointtype", monitorpointtype);
            //获取关联信息
            List<String> mns = new ArrayList<>();
            String mn = "";
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                List<Map<String, Object>> airlist = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                if (airlist != null && airlist.size() > 0) {
                    if (airlist.get(0).get("dgimn") != null) {
                        mn = airlist.get(0).get("dgimn").toString();
                    }
                }
            } else {
                List<Map<String, Object>> stinklist = pollutionTraceSourceService.getTraceSourceMonitorPointMN(paramMap);
                if (stinklist != null && stinklist.size() > 0) {
                    if (stinklist.get(0).get("airmn") != null) {
                        mn = stinklist.get(0).get("airmn").toString();
                    }
                }
            }
            if (StringUtils.isNotBlank(mn)) { //有，使用关联信息id
                mns.add(mn);
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(3);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = MongoDataUtils.getWindDataList(documents, collection);
            }

            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/5/21 0021 上午 9:46
     * @Description: 通过监测点id，监测时间点获取空气站小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointid, monitortime]
     * @throws:
     */
    @RequestMapping(value = "/getAirStationHourDataByParams", method = RequestMethod.POST)
    public Object getAirStationHourDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitortime") String monitortime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorPointTypeCode, new HashMap<>());

            //获取污染物码表信息
            paramMap.put("pollutanttype", monitorPointTypeCode);
            List<Map<String, Object>> pollutantInfo = pollutantService.getPollutantsByCodesAndType(paramMap);

            if (StringUtils.isNotBlank(monitortime)) {
                monitortime = monitortime + ":00:00";
                paramMap.put("starttime", monitortime);
            }
            if (StringUtils.isNotBlank(monitortime)) {
                monitortime = monitortime + ":00:00";
                paramMap.put("endtime", monitortime);
            }
            if (mns.size() > 0) {
                paramMap.put("mns", mns);
            }


            List<String> pollutantcodes = Arrays.asList(
                    HumidityEnum.getCode(),
                    TemperatureEnum.getCode(),
                    WindDirectionEnum.getCode(),
                    WindSpeedEnum.getCode());
            paramMap.put("pollutantcodes", pollutantcodes);
            String collection = MongoDataUtils.getCollectionByDataMark(3);
            paramMap.put("collection", collection);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);


            List<Map<String, Object>> dataList = MongoDataUtils.getStationHourDataList(documents, pollutantInfo);
            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测时间段获取空气点位AQI报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getAirStationReportDataByParams", method = RequestMethod.POST)
    public Object getAirStationReportDataByParams(
            @RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datetype") String datetype,
            @RequestJson(value = "pagenum", required = false) Integer pagenum,
            @RequestJson(value = "pagesize", required = false) Integer pagesize) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("airids",monitorpointids);
            List<Map<String, Object>> airlist = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<String> mns = new ArrayList<>();
            Map<String,Object> mnandname = new HashMap<>();
            for (Map<String, Object> outPut : airlist) {
                if (outPut.get("dgimn") != null && dgimns.contains(outPut.get("dgimn").toString())) {
                    mns.add(outPut.get("dgimn").toString());
                    mnandname.put(outPut.get("dgimn").toString(),outPut.get("monitorpointname"));
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("datetype", datetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            Map<String, Object> result = airMonitorStationService.getAirStationReportDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测时间段获取空气点位AQI报表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/exceptAirStationReportDataByParams", method = RequestMethod.POST)
    public void exceptAirStationReportDataByParams(
            @RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "datetype") String datetype,
            HttpServletRequest request, HttpServletResponse response) throws Exception{

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("airids",monitorpointids);
            List<Map<String, Object>> airlist = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> mns = new ArrayList<>();
            Map<String,Object> mnandname = new HashMap<>();
            for (Map<String, Object> outPut : airlist) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                    mnandname.put(outPut.get("dgimn").toString(),outPut.get("monitorpointname"));
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("datetype", datetype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            Map<String, Object> result = airMonitorStationService.getAirStationReportDataByParams(paramMap);
            List<Map<String, Object>> tablelistdata = (List<Map<String, Object>>) result.get("datalist");
            List<Map<String, Object>> tabletitledata = new ArrayList<>();
            String fileName = "";
            String titlename = "";
            if ("hour".equals(datetype)){
                titlename = "常规空气站小时AQI数据报表"+"【"+starttime+"至"+endtime+"】";
                fileName =   "空气站AQI数据报表_" + new Date().getTime();
                String[] titlenames = new String[]{"二氧化硫(SO2)1小时平均值", "二氧化氮(NO2)1小时平均值", "颗粒物(颗粒小于等于10μm)1小时平均值", "一氧化碳(CO)1小时平均值", "臭氧(O3)1小时平均值", "颗粒物(颗粒小于等于2.5μm)1小时平均值"};
                String[] titlefiled = new String[]{"a21026", "a21004", "a34002", "a21005", "a05024", "a34004"};
                tabletitledata = airMonitorStationService.getAirStationReportTitleDataByType(titlenames,titlefiled);
            }else if("day".equals(datetype)){
                titlename = "常规空气站日AQI数据报表"+"【"+starttime+"至"+endtime+"】";
                fileName = "空气站AQI数据报表"+ "_" + new Date().getTime();
                String[] titlenames = new String[]{"二氧化硫(SO2)24小时平均值", "二氧化氮(NO2)24小时平均值", "颗粒物(颗粒小于等于10μm)24小时平均值", "一氧化碳(CO)24小时平均值", "臭氧(O3)最大1小时平均", "颗粒物(颗粒小于等于2.5μm)24小时平均值"};
                String[] titlefiled = new String[]{"a21026", "a21004", "a34002", "a21005", "a05024", "a34004"};
                tabletitledata = airMonitorStationService.getAirStationReportTitleDataByType(titlenames,titlefiled);
            }
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/19 0019 下午 3:22
     * @Description: 通过站点MN和监测日期获取空气点位综合评价报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getAirStationOverallMeritDataByParams", method = RequestMethod.POST)
    public Object getAirStationOverallMeritDataByParams(
            @RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "datetype") String datetype
           ) throws Exception {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("airids",monitorpointids);
            List<Map<String, Object>> airlist = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            List<String> mns = new ArrayList<>();
            Map<String,Object> mnandname = new HashMap<>();
            for (Map<String, Object> outPut : airlist) {
                if (outPut.get("dgimn") != null && dgimns.contains(outPut.get("dgimn").toString())) {
                    mns.add(outPut.get("dgimn").toString());
                    mnandname.put(outPut.get("dgimn").toString(),outPut.get("monitorpointname"));
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("datetype", datetype);
            paramMap.put("monitortime", monitortime);
            List<Map<String, Object>> result = airMonitorStationService.getAirStationOverallMeritDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/01/21 0021 上午 8:55
     * @Description: 导出通过站点MN和监测日期获取空气点位综合评价报表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/exceptAirStationOverallMeritDataByParams", method = RequestMethod.POST)
    public void exceptAirStationOverallMeritDataByParams(
            @RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids,
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "datetype") String datetype,
            HttpServletRequest request, HttpServletResponse response) throws Exception{

        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("airids",monitorpointids);
            List<Map<String, Object>> airlist = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
            List<String> mns = new ArrayList<>();
            Map<String,Object> mnandname = new HashMap<>();
            for (Map<String, Object> outPut : airlist) {
                if (outPut.get("dgimn") != null) {
                    mns.add(outPut.get("dgimn").toString());
                    mnandname.put(outPut.get("dgimn").toString(),outPut.get("monitorpointname"));
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("datetype", datetype);
            paramMap.put("monitortime", monitortime);
            List<Map<String, Object>> tablelistdata = airMonitorStationService.getAirStationOverallMeritDataByParams(paramMap);
            List<Map<String, Object>> tabletitledata = new ArrayList<>();
            String fileName = "";
            String titlename = "";
            titlename = "空气综合评价表"+"【"+monitortime+"】";
            fileName =   "空气综合评价表_" + new Date().getTime();
            tabletitledata = airMonitorStationService.getAirStationOverallMeritDataTitle();
            ExcelUtil.exportManyHeaderExcelDataReportFile(fileName, response, request, "", tabletitledata, tablelistdata, "", titlename);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
