package com.tjpu.sp.controller.environmentalprotection.weather;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.AirMonitorStationService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
import com.tjpu.sp.service.environmentalprotection.weather.WeatherService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum;

@RestController
@RequestMapping("weatherAnalysis")
public class WeatherAnalysisController {

    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private AirMonitorStationService airMonitorStationService;
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private PollutionTraceSourceService pollutionTraceSourceService;
    @Autowired
    private OnlineService onlineService;


    /**
     * @author: xsm
     * @date: 2019/6/26 4:53
     * @Description: 通过自定义参数统计监测点位的监测数据及气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countPollutantWeatherDataByParamMap", method = RequestMethod.POST)
    public Object countPollutantWeatherDataByParamMap(@RequestJson(value = "timetype") String timetype,
                                                      @RequestJson(value = "dgimn", required = false) String dgimn,
                                                      @RequestJson(value = "monitorpointid") String monitorpointid,
                                                      @RequestJson(value = "pointtype") Integer pointtype,
                                                      @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            paramMap.put("monitorpointid", monitorpointid);
            String mn = "";
            String airmn = "";
            paramMap.put("pointtype", pointtype);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointtype)) {
                case EnvironmentalVocEnum: //voc
                case MicroStationEnum: //微站
                case EnvironmentalStinkEnum: //恶臭
                case meteoEnum: //气象
                    listdata = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case AirEnum: //空气站
                    listdata = airMonitorStationService.getOnlineAirStationInfoByParamMap(paramMap);
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    listdata = outPutUnorganizedService.getOutPutUnorganizedInfoByIDAndType(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    listdata = outPutUnorganizedService.getOutPutUnorganizedInfoByIDAndType(paramMap);
                    break;
                case WasteGasEnum: //废气排口
                case SmokeEnum:
                    mn = dgimn;
            }
            //获取该点位的MN号
            if (listdata != null && listdata.size() > 0 && listdata.get(0).get("DGIMN") != null) {
                mn = listdata.get(0).get("DGIMN").toString();
            }
            /*if (listdata != null && listdata.size() > 0 && listdata.get(0).get("dgimn") != null) {
                mn = listdata.get(0).get("dgimn").toString();
            }*/
            if (pointtype == CommonTypeEnum.MonitorPointTypeEnum.WasteGasEnum.getCode()||pointtype == CommonTypeEnum.MonitorPointTypeEnum.SmokeEnum.getCode()
                    ||pointtype == CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode()) {
                airmn = "";
            } else if(pointtype == CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode()) {
                airmn = mn;
            }else{
                airmn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
                if (!StringUtils.isNotBlank(airmn)) {//无，使用当前MN
                    airmn = mn;
                }
            }
            resultMap = weatherService.countWeatherAndMonitorPointDataByParamMap(airmn, mn, pointtype, timetype, pollutantcodes, starttime, endtime);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/6/26 4:53
     * @Description: 通过自定义参数统计监测点位的监测数据及气候信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getPollutantWeatherListsByParamMap", method = RequestMethod.POST)
    public Object getPollutantWeatherListsByParamMap(@RequestJson(value = "timetype") String timetype,
                                                     @RequestJson(value = "monitorpointid") String monitorpointid,
                                                     @RequestJson(value = "pointtype") Integer pointtype,
                                                     @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                     @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            paramMap.put("monitorpointid", monitorpointid);
            String mn = "";
            paramMap.put("pointtype", pointtype);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointtype)) {
                case EnvironmentalVocEnum: //voc
                    listdata = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    listdata = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    listdata = outPutUnorganizedService.getOutPutUnorganizedInfoByIDAndType(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    listdata = outPutUnorganizedService.getOutPutUnorganizedInfoByIDAndType(paramMap);
                    break;
            }
            //获取该点位的MN号
            if (listdata != null && listdata.size() > 0 && listdata.get(0).get("DGIMN") != null) {
                mn = listdata.get(0).get("DGIMN").toString();
            }
            //获取该点位关联的气象数据表中空气监测点的MN号
            String airmn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
            if (!StringUtils.isNotBlank(airmn)) {//无，使用当前MN
                airmn = mn;
            }
            resultlist = weatherService.getWeathersAndMonitorPointListsByParamMap(airmn, mn, pointtype, timetype, pollutantcodes, starttime, endtime);
            //处理分页数据
            if (pagenum != null && pagesize != null) {
                resultMap.put("total", resultlist.size());
                resultlist = getPageData(resultlist, pagenum, pagesize);
                resultMap.put("datalist", resultlist);
            } else {
                resultMap.put("datalist", resultlist);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/04/02
     * @Description: 通过自定义参数统计气象点位的风向数据（平均风速，主导风向）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getWeatherWindDataByParamMap", method = RequestMethod.POST)
    public Object getPollutantWeatherListsByParamMap(@RequestJson(value = "dgimn") String dgimn) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Date nowDay = new Date();
            String endTime = DataFormatUtil.getDateYMD(nowDay);
            String startTime = DataFormatUtil.getBeforeByMonthTime(1, endTime);
            String windSpeedCode = CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum.getCode();
            String windDirectionCode = CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", startTime + " 00:00:00");
            paramMap.put("endtime", endTime + " 23:59:59");
            paramMap.put("mns", Arrays.asList(dgimn));
            paramMap.put("timeKey", "MonitorTime");
            paramMap.put("pollutantcodes", Arrays.asList(windSpeedCode, windDirectionCode));
            paramMap.put("unwindkey", "HourDataList");
            paramMap.put("collection", "HourData");
            List<Document> documents = airMonitorStationService.getMonitorDataByParam(paramMap);
            if (documents.size() > 0) {
                Map<String, List<Double>> ymdAndSpeed = new HashMap<>();
                Map<String, List<Double>> ymdAndDirection = new HashMap<>();
                List<Double> values;
                Double value;
                String ymd;
                String pollutantCode;
                for (Document document : documents) {
                    ymd = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    value = document.get("AvgStrength") != null ? Double.parseDouble(document.getString("AvgStrength")) : null;
                    if (value != null) {
                        pollutantCode = document.getString("PollutantCode");
                        if (pollutantCode.equals(windSpeedCode)) {
                            if (ymdAndSpeed.containsKey(ymd)) {
                                values = ymdAndSpeed.get(ymd);
                            } else {
                                values = new ArrayList<>();
                            }
                            values.add(value);
                            ymdAndSpeed.put(ymd, values);
                        } else if (pollutantCode.equals(windDirectionCode)) {
                            if (ymdAndDirection.containsKey(ymd)) {
                                values = ymdAndDirection.get(ymd);
                            } else {
                                values = new ArrayList<>();
                            }
                            values.add(value);
                            ymdAndDirection.put(ymd, values);
                        }
                    }
                }
                if (ymdAndSpeed.size() > 0) {
                    Map<String, Double> ymdAndMainDirectionValue = new HashMap<>();
                    Map<String, String> ymdAndMainDirectionName = new HashMap<>();
                    if (ymdAndDirection.size() > 0) {
                        String code;

                        Map<String, Integer> codeAndNum;
                        for (String ymdIndex : ymdAndDirection.keySet()) {
                            codeAndNum = new LinkedHashMap<>();
                            values = ymdAndDirection.get(ymdIndex);
                            for (Double valueIndex : values) {
                                code = DataFormatUtil.windDirectionSwitch(valueIndex, "code");
                                if (codeAndNum.containsKey(code)) {
                                    codeAndNum.put(code, codeAndNum.get(code) + 1);
                                } else {
                                    codeAndNum.put(code, 1);
                                }
                            }
                            codeAndNum = DataFormatUtil.sortMapByValue(codeAndNum, true);
                            code = DataFormatUtil.getFirstKey(codeAndNum);
                            value = DataFormatUtil.windDirectionSwitch(code);
                            ymdAndMainDirectionValue.put(ymdIndex,value );
                            ymdAndMainDirectionName.put(ymdIndex, DataFormatUtil.windDirectionSwitch(value,"name"));
                        }
                    }
                    List<String> ymds = DataFormatUtil.getYMDBetween(startTime, endTime);
                    ymds.add(endTime);
                    String avgValue;
                    for (String ymdIndex : ymds) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("monitortime", DataFormatUtil.FormatDateOneToOther(ymdIndex, "yyyy-MM-dd", "d日"));
                        values = ymdAndSpeed.get(ymdIndex);
                        if (values != null) {
                            avgValue = DataFormatUtil.getListAvgValue(values);
                        } else {
                            avgValue = "";
                        }
                        resultMap.put("windspeed", avgValue);
                        resultMap.put("winddirectionvalue", ymdAndMainDirectionValue.get(ymdIndex));
                        resultMap.put("winddirectionname", ymdAndMainDirectionName.get(ymdIndex));
                        resultList.add(resultMap);
                    }

                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/6/25 0025 下午 7:58
     * @Description: 截取list分页数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPageData(List<Map<String, Object>> dataList, Integer pagenum, Integer pagesize) {
        int size = dataList.size();
        int pageStart = pagenum == 1 ? 0 : (pagenum - 1) * pagesize;//截取的开始位置
        int pageEnd = size < pagenum * pagesize ? size : pagenum * pagesize;//截取的结束位置
        if (size > pageStart) {
            dataList = dataList.subList(pageStart, pageEnd);
        }
        return dataList;
    }

    /**
     * @author: xsm
     * @date: 2019/6/27 0027 下午 6:22
     * @Description: 通过数据类型标记（hour:小时，day:日）及自定义参数导出污染物气象数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportPollutantWeather", method = RequestMethod.POST)
    public void exportPollutantWeather(@RequestJson(value = "timetype") String timetype,
                                       @RequestJson(value = "monitorpointid") String monitorpointid,
                                       @RequestJson(value = "pointtype") Integer pointtype,
                                       @RequestJson(value = "pollutantcodes") List<String> pollutantcodes,
                                       @RequestJson(value = "starttime") String starttime,
                                       @RequestJson(value = "endtime") String endtime,
                                       @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                       @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                       HttpServletRequest request, HttpServletResponse response) {

        try {
            //获取表头数据
            Map<String, Object> titleMap = new HashMap<>();
            titleMap.put("pointtype", pointtype);
            titleMap.put("pollutantcodes", pollutantcodes);
            titleMap.put("monitorpointid", monitorpointid);
            List<Map<String, Object>> tabletitledata = weatherService.getTableTitleForWeathers(titleMap);
            //获取表格数据
            Map<String, Object> paramMap = new HashMap<String, Object>();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> listdata = new ArrayList<Map<String, Object>>();
            paramMap.put("monitorpointid", monitorpointid);
            String mn = "";
            paramMap.put("pointtype", pointtype);
            switch (CommonTypeEnum.MonitorPointTypeEnum.getCodeByInt(pointtype)) {
                case EnvironmentalVocEnum: //voc
                    listdata = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case EnvironmentalStinkEnum: //恶臭
                    listdata = otherMonitorPointService.getOtherMonitorPointInfoByIDAndType(paramMap);
                    break;
                case FactoryBoundaryStinkEnum: //厂界恶臭
                    listdata = outPutUnorganizedService.getOutPutUnorganizedInfoByIDAndType(paramMap);
                    break;
                case FactoryBoundarySmallStationEnum: //厂界小型站
                    listdata = outPutUnorganizedService.getOutPutUnorganizedInfoByIDAndType(paramMap);
                    break;
            }
            //获取该点位的MN号
            if (listdata != null && listdata.size() > 0 && listdata.get(0).get("DGIMN") != null) {
                mn = listdata.get(0).get("DGIMN").toString();
            }
            //获取该点位关联的气象数据表中空气监测点的MN号
            String airmn = airMonitorStationService.getAirMnByOtherMonitorPointIdAndType(paramMap);
            if (!StringUtils.isNotBlank(airmn)) {//无，使用当前MN
                airmn = mn;
            }
            resultlist = weatherService.getWeathersAndMonitorPointListsByParamMap(airmn, mn, pointtype, timetype, pollutantcodes, starttime, endtime);
            //处理分页数据
            if (pagenum != null && pagesize != null) {
                resultlist = getPageData(resultlist, pagenum, pagesize);
            }
            //设置导出文件数据格式
            List<String> headers = ExcelUtil.setExportTableDataByKey(tabletitledata, "label");
            List<String> headersField = ExcelUtil.setExportTableDataByKey(tabletitledata, "prop");
            //设置文件名称
            String fileName = "污染物气象数据导出文件_" + new Date().getTime();
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, resultlist, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

     /**
       * @author: xsm
       * @date: 2021/4/25 0025 上午 8:54
       * @Description:统计各气象点位的风向风速信息（分钟、小时）
       * @updateUser:
       * @updateDate:
       * @updateDescription:
       * @param: starttime、endtime根据日期类型到分钟或是小时
       * @return:
       */
     @RequestMapping(value = "/countAllMeteWindChartDataByParams", method = RequestMethod.POST)
     public Object countAllMeteWindChartDataByParams(
             @RequestJson(value = "datetype") String datetype,
             @RequestJson(value = "starttime") String starttime,
             @RequestJson(value = "endtime") String endtime) {
         try {
             List<Object> result= new ArrayList<>();
             List<Map<String,Object>> hasdata= new ArrayList<>();
             List<Map<String,Object>> nodata= new ArrayList<>();
             Map<String, Object> paramMap = new HashMap<>();
             //获取关联信息
             paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.meteoEnum.getCode());
             //获取关联信息
             List<Map<String, Object>> stinklist = pollutionTraceSourceService.getTraceSourceMonitorPointMN(paramMap);
             List<String> mns = new ArrayList<>();
             Map<String,Object> mnandnames = new HashMap<>();
             if (stinklist != null && stinklist.size() > 0) {
                 for (Map<String,Object> obj:stinklist){
                     if (obj.get("airmn") != null){
                         mns.add(obj.get("airmn").toString());
                         mnandnames.put(obj.get("airmn").toString(),obj.get("MonitorPointName"));
                     }
                 }
             }
             String collection = "";
             if (mns.size() > 0) {
                 if ("minute".equals(datetype)){
                     collection = MongoDataUtils.getCollectionByDataMark(2);
                     if (StringUtils.isNotBlank(starttime)) {
                         starttime = starttime + ":00";
                         paramMap.put("starttime", starttime);
                     }
                     if (StringUtils.isNotBlank(endtime)) {
                         endtime = endtime + ":59";
                         paramMap.put("endtime", endtime);
                     }
                 }else if ("hour".equals(datetype)){
                     collection = MongoDataUtils.getCollectionByDataMark(3);
                     if (StringUtils.isNotBlank(starttime)) {
                         starttime = starttime + ":00:00";
                         paramMap.put("starttime", starttime);
                     }
                     if (StringUtils.isNotBlank(endtime)) {
                         endtime = endtime + ":59:59";
                         paramMap.put("endtime", endtime);
                     }
                 }
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
                 List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                         , WindSpeedEnum.getCode());
                 paramMap.put("collection", collection);
                 paramMap.put("pollutantcodes", pollutantcodes);
                 List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                 if (documents!=null&&documents.size()>0){
                     Map<String, List<Document>> collect = documents.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                     for (String mn:mns){
                         Map<String,Object> onemap = new HashMap<>();
                         List<Document> onelist = new ArrayList<>();
                         List<Map<String, Object>> onedataList =new ArrayList<>();
                         onemap.put("dgimn",mn);
                         onemap.put("monitorpointname",mnandnames.get(mn));
                         if (collect.get(mn)!=null){
                             onelist = collect.get(mn);
                             onedataList = MongoDataUtils.countWindChartData(onelist, collection);
                             onemap.put("winddata",onedataList);
                             hasdata.add(onemap);
                         }else{
                             onemap.put("winddata",onedataList);
                             nodata.add(onemap);
                         }
                     }
                     //排序
                     hasdata = hasdata.stream().sorted(
                                             Comparator.comparing(m -> m.get("monitorpointname").toString(), (x, y) -> {
                                                 // ToFirstChar 将汉字首字母转为拼音
                                                 x = FormatUtils.ToFirstChar(x).toUpperCase();
                                                 y = FormatUtils.ToFirstChar(y).toUpperCase();
                                                 Collator clt = Collator.getInstance();
                                                 return clt.compare(x, y);
                                             })

                     ).collect(Collectors.toList());
                     nodata = nodata.stream().sorted(
                             Comparator.comparing(m -> m.get("monitorpointname").toString(), (x, y) -> {
                                 // ToFirstChar 将汉字首字母转为拼音
                                 x = FormatUtils.ToFirstChar(x).toUpperCase();
                                 y = FormatUtils.ToFirstChar(y).toUpperCase();
                                 Collator clt = Collator.getInstance();
                                 return clt.compare(x, y);
                             })

                     ).collect(Collectors.toList());
                     result.addAll(hasdata);
                     result.addAll(nodata);
                 }

             }
             return AuthUtil.parseJsonKeyToLower("success", result);
         } catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
     }
}
