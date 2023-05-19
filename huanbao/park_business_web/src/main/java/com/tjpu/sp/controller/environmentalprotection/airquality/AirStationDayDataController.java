package com.tjpu.sp.controller.environmentalprotection.airquality;

import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.controller.environmentalprotection.alarm.OverAlarmController;
import com.tjpu.sp.model.common.mongodb.StationDayAQIDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: chengzq
 * @date: 2019/6/04 0021 09:48
 * @Description: 空气站点小时数据控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("airStationDay")
public class AirStationDayDataController {
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    /**
     * 大气监测点类型编码
     **/
    private final int monitorPointTypeCode = CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode();
    @Autowired
    private OnlineService onlineService;
    private final String dayCollection = "StationDayAQIData";

    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 上午 10:14
     * @Description: 通过监测时间，分页信息获取空气站日数据列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/getAirStationDayDataListByParams", method = RequestMethod.POST)
    public Object getAirStationDayDataListByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                   @RequestJson(value = "monitortime", required = false) String monitortime
    ) {
        try {
            StationDayAQIDataVO stationDayAQIDataVO = new StationDayAQIDataVO();
            List<Map<String, Object>> dataList = new ArrayList<>();

            //设置查询时间
            if (StringUtils.isNotBlank(monitortime)) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("starttime", monitortime + " 00");
                paramMap.put("endtime", monitortime + " 23");
                stationDayAQIDataVO.setMonitorTime(JSONObject.fromObject(paramMap).toString());
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
            String dgimns = allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));

            stationDayAQIDataVO.setStationCode(dgimns);
            List<StationDayAQIDataVO> listWithPageByParam = mongoBaseService.getListWithPageByParam(stationDayAQIDataVO, mongoSearchEntity, "StationDayAQIData", "yyyy-MM-dd HH");
            for (StationDayAQIDataVO dayAQIDataVO : listWithPageByParam) {
                List<Map<String, Object>> datas = dayAQIDataVO.getDataList();
                Map<String, Object> dataMap = new HashMap<>();
                dataList.add(dataMap);
                for (Map<String, Object> map : datas) {
                    String pollutantCode = map.get("PollutantCode").toString();
                    String stationCode = dayAQIDataVO.getStationCode();
                    //将污染物code设置为name
                    pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(pollutantCode)).
                            peek(m -> dataMap.put(m.get("name").toString().toLowerCase(), map.get("Strength"))).collect(Collectors.toList());
                    //将首要污染物code设置为name
                    pollutantInfo.stream().filter(m -> m.get("code") != null && m.get("name") != null && m.get("code").toString().equals(dayAQIDataVO.getPrimaryPollutant()))
                            .map(m -> dataMap.put("pimarypllutant", m.get("name").toString().toLowerCase())).collect(Collectors.toList());
                    allAirMonitorStation.stream().filter(m -> m.get("DGIMN") != null && m.get("DGIMN").toString().equals(stationCode)).
                            peek(m -> dataMap.put("monitorpointname", m.get("MonitorPointName"))).collect(Collectors.toList());
                    dataMap.put("aqi", dayAQIDataVO.getaQi());
                    dataMap.put("quality", dayAQIDataVO.getAirQuality());
                    dataMap.put("monitorTime", OverAlarmController.format(dayAQIDataVO.getMonitorTime(), "yyyy-MM-dd"));
                }
            }
            List<Map<String, Object>> collect = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("aqi").toString()).reversed()).collect(Collectors.toList());
            resultMap.put("total", listWithPageByParam.size());
            resultMap.put("datalist", collect);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 上午 10:14
     * @Description: 通过监测时间，分页信息导出空气站日数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "/exportAirStationDayDataByParams", method = RequestMethod.POST)
    public void exportAirStationDayDataByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
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

            JSONObject jsonObject = JSONObject.fromObject(getAirStationDayDataListByParams(pagesize, pagenum, monitortime));
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
            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "yyyy-MM-dd");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("AQI日排行", response, request, bytesForWorkBook);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件统计站点（空气及其他站点）日风向图表数据（风速，风向，频次）
     * @updateUser: xsm
     * @updateDate: 2020/06/26
     * @updateDescription: 自定义查询条件统计气象站点日风向图表数据（风速，风向，频次）
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countDayWindChartDataByParams", method = RequestMethod.POST)
    public Object countDayWindChartDataByParams(
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
            if (dgimn!=null&&!"".equals(dgimn)){//有气象点MN号则根据MN号去查
                mns.add(dgimn);
            }else {
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
                    starttime = starttime + " 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                String collection = MongoDataUtils.getCollectionByDataMark(4);

                paramMap.put("pollutantcodes", pollutantcodes);
                paramMap.put("collection", collection);
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
     * @Description: 自定义查询条件统计空气站点日风向列表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countAirStationDayWindListDataByParams", method = RequestMethod.POST)
    public Object countAirStationDayWindListDataByParams(
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
                List<String> days = DataFormatUtil.getYMDBetween(starttime, endtime);
                Map<String, Object> paramMap = new HashMap<>();
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
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(4);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = MongoDataUtils.countWindDataList(documents, collection, mnAndOutPutName, days.size());
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
     * @author: xsm
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件统计空气站点日风向列表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countAirStationMinuteWindListDataByParams", method = RequestMethod.POST)
    public Object countAirStationMinuteWindListDataByParams(
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
                List<String> days = DataFormatUtil.getYMDHMBetween(starttime, endtime);
                Map<String, Object> paramMap = new HashMap<>();
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                if (mns.size() > 0) {
                    paramMap.put("mns", mns);
                }
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(2);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = MongoDataUtils.countWindDataList(documents, collection, mnAndOutPutName, days.size());
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
     * @author: xsm
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件导出空气站点分钟风向列表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/exportAirStationMinuteWindListDataByParams", method = RequestMethod.POST)
    public void exportAirStationMinuteWindListDataByParams(HttpServletRequest request, HttpServletResponse response,
                                                        @RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime) throws Exception {

        try {
            //表头
            List<Map<String, Object>> headers = MongoDataUtils.getHeaderData("分钟数");
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
                List<String> minutes = DataFormatUtil.getYMDHMBetween(starttime, endtime);
                Map<String, Object> paramMap = new HashMap<>();
                if (StringUtils.isNotBlank(starttime)) {
                    starttime = starttime + ":00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + ":59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                if (mns.size() > 0) {
                    paramMap.put("mns", mns);
                }
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(2);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = MongoDataUtils.countWindDataList(documents, collection, mnAndOutPutName, minutes.size());
                String fileName = "分钟风力统计报表" + new Date().getTime();
                ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headers, dataList, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件获取站点(空气及其他站点)日风向数据（时间，风速，风向）
     * @updateUser: xsm
     * @updateDate: 2020/06/10 0010 下午6:18
     * @updateDescription: 根据气象站点MN号获取日风向数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getDayWindDataByParams", method = RequestMethod.POST)
    public Object getDayWindDataByParams(
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
            if (dgimn!=null&&!"".equals(dgimn)){//有气象点MN号则根据MN号去查
                mns.add(dgimn);
            }else {
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
                    starttime = starttime + " 00:00:00";
                    paramMap.put("starttime", starttime);
                }
                if (StringUtils.isNotBlank(endtime)) {
                    endtime = endtime + " 23:59:59";
                    paramMap.put("endtime", endtime);
                }
                paramMap.put("sort", "asc");
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(4);
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
     * @author: xsm
     * @date: 2021/04/12 0012 上午 9:02
     * @Description: 根据气象站点MN号获取分钟风向数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getMinuteWindDataByParams", method = RequestMethod.POST)
    public Object getMinuteWindDataByParams(
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
            if (dgimn!=null&&!"".equals(dgimn)){//有气象点MN号则根据MN号去查
                mns.add(dgimn);
            }else {
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
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(2);
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
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件获取空气站点日气象数据（相对湿度，降水量，大气压，风速）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getAirStationDayWeatherDataByParams", method = RequestMethod.POST)
    public Object getAirStationDayWeatherDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> times = DataFormatUtil.getYMDBetween(starttime, endtime);
            times.add(endtime);
            List<String> mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorPointTypeCode, new HashMap<>());
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
            List<String> pollutantcodes = Arrays.asList(
                    CommonTypeEnum.WeatherPollutionEnum.HumidityEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.RainfallEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.PressureEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.TemperatureEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode(),
                    CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
            paramMap.put("pollutantcodes", pollutantcodes);
            String collection = MongoDataUtils.getCollectionByDataMark(4);
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
     * @date: 2019/6/26 0026 下午 3:51
     * @Description: 自定义查询条件导出空气站点日风向列表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/exportAirStationDayWindListDataByParams", method = RequestMethod.POST)
    public void exportAirStationDayWindListDataByParams(HttpServletRequest request, HttpServletResponse response,
                                                        @RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime) throws Exception {

        try {
            //表头
            List<Map<String, Object>> headers = MongoDataUtils.getHeaderData("天数");
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
                List<String> hours = DataFormatUtil.getYMDBetween(starttime, endtime);
                Map<String, Object> paramMap = new HashMap<>();
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
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                paramMap.put("pollutantcodes", pollutantcodes);
                String collection = MongoDataUtils.getCollectionByDataMark(4);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                List<Map<String, Object>> dataList = MongoDataUtils.countWindDataList(documents, collection, mnAndOutPutName, hours.size());
                String fileName = "日风力统计报表" + new Date().getTime();
                ExcelUtil.exportManyHeaderExcelFile(fileName, response, request, "", headers, dataList, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/9 0009 上午 10:00
     * @Description: 获取单站点最新小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getAirStationLatelyHourDataByParams", method = RequestMethod.POST)
    public Object getAirStationLatelyHourDataByParams(HttpServletRequest request, HttpServletResponse response,
                                                      @RequestJson(value = "pkid") String pkid
    ) throws Exception {

        try {
            //获取站点MN号
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("pkid", pkid);
            Map<String, Object> oldobj = airMonitorStationService.getAirStationDeviceStatusByID(paramMap);
            String airmn = (oldobj != null && oldobj.size() > 0) ? oldobj.get("DGIMN").toString() : "";
            Map<String, Object> result = new HashMap<>();
            if (!"".equals(airmn)) {
                result = onlineService.getAirStationLatelyHourDataByAirMn(airmn);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/10/29 0029 下午 3:36
     * @Description: 自定义查询条件统计站点（空气及其他站点）分钟风向图表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countMinuteWindChartDataByParams", method = RequestMethod.POST)
    public Object countMinuteWindChartDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("pointtype", monitorpointtype);
            //获取关联信息
            List<String> mns = new ArrayList<>();
            String mn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
            if (StringUtils.isNotBlank(mn)) { //有，使用关联信息id
                mns.add(mn);
            } else {  //无，使用当前id
                mns = onlineService.getMNsAndSetOutPutIdAndMnByOutPutIds(Arrays.asList(monitorpointid), monitorpointtype, new HashMap<>());
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
                List<String> pollutantcodes = Arrays.asList(CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode()
                        , CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode());
                String collection = MongoDataUtils.getCollectionByDataMark(2);

                paramMap.put("pollutantcodes", pollutantcodes);
                paramMap.put("collection", collection);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = MongoDataUtils.countWindChartData(documents, collection);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}