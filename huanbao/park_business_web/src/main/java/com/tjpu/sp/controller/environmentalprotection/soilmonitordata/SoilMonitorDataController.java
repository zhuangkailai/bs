package com.tjpu.sp.controller.environmentalprotection.soilmonitordata;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.environmentalprotection.onlinemonitor.OnlineController;
import com.tjpu.sp.model.common.mongodb.DayDataSelectVO;
import com.tjpu.sp.model.common.mongodb.DayDataVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.SoilPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.WaterOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.soilEnum;

/**
 * @author: lip
 * @date: 2020/3/23 0023 下午 4:23
 * @Description: 土壤监测数据处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("soilMonitorData")
public class SoilMonitorDataController {

    @Autowired
    private SoilPointService soilPointService;

    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;


    @Autowired
    private PollutantService pollutantService;

    @Autowired
    private OnlineService onlineService;
    @Autowired
    private MongoBaseService mongoBaseService;

    private final String db_dayData = "DayData";
    private final String db_monthData = "MonthData";


    /**
     * @author: lip
     * @date: 2020/3/23 0023 下午 4:48
     * @Description: 自定义查询条件获取废气与土壤相关性列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasWaterAndSoilRelationListDataByParams", method = RequestMethod.POST)
    public Object getGasAndSoilRelationListDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointname") String monitorpointname,
            @RequestJson(value = "soilpollutantcode") String soilpollutantcode,
            @RequestJson(value = "soilpollutantname") String soilpollutantname,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputpollutantcode") String outputpollutantcode,
            @RequestJson(value = "outputpollutantname") String outputpollutantname,
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "pagesize") Integer pagesize,
            @RequestJson(value = "pagenum") Integer pagenum

    ) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> dataList = getRelationDataList(monitorpointid, monitorpointname,
                    soilpollutantcode, soilpollutantname, starttime, endtime, outputpollutantcode,
                    outputpollutantname, monitorpointtypecode);
            //处理分页数据
            int total = dataList.size();
            dataList = getPageData(dataList, pagenum, pagesize);
            resultMap.put("tablelistdata", dataList);
            resultMap.put("total", total);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/3/23 0023 下午 4:48
     * @Description: 自定义查询条件获取废气与土壤相关性列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "exportGasWaterAndSoilRelationListDataByParams", method = RequestMethod.POST)
    public void exportGasWaterAndSoilRelationListDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "monitorpointname") String monitorpointname,
            @RequestJson(value = "soilpollutantcode") String soilpollutantcode,
            @RequestJson(value = "soilpollutantname") String soilpollutantname,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputpollutantcode") String outputpollutantcode,
            @RequestJson(value = "outputpollutantname") String outputpollutantname,
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            HttpServletResponse response, HttpServletRequest request
    ) throws IOException {
        try {
            List<Map<String, Object>> dataList = getRelationDataList(monitorpointid, monitorpointname,
                    soilpollutantcode, soilpollutantname, starttime, endtime, outputpollutantcode,
                    outputpollutantname, monitorpointtypecode);
            String preName;
            if (monitorpointtypecode == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
                preName = "废水";
            } else {
                preName = "废气";
            }
            String time = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd", "yyyy年M月d日")
                    + "至" + DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd", "yyyy年M月d日");
            //导出文件
            String fileName = preName + "与土壤" + time + "相关性分析" + new Date().getTime();
            //设置导出文件数据格式
            List<String> headers = Arrays.asList("土壤监测点名称", "土壤污染物", preName + "监测点名称", preName + "污染物", "相关度");
            List<String> headersField = Arrays.asList("soilpointname", "soilpollutantname", "outputname", "outputpollutantname", "relationpercent");
            ExcelUtil.exportExcelFile(fileName, response, request, "", headers, headersField, dataList, "");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/7/1 0001 上午 10:38
     * @Description: 自定义查询条件查询废气废水和土壤相关性散点图数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasWaterAndSoilRelationChartDataByParams", method = RequestMethod.POST)
    public Object getGasWaterAndSoilRelationChartDataByParams(
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "soilpollutantcode") String soilpollutantcode,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "outputid") String outputid,
            @RequestJson(value = "outputpollutantcode") String outputpollutantcode,
            @RequestJson(value = "monitorpointtypecode") Integer monitorpointtypecode,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum


    ) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputids", Arrays.asList(outputid));
            List<Map<String, Object>> outPuts;
            if (monitorpointtypecode == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
                paramMap.put("outputtype", "water");
                outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
            } else {
                outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
            }
            if (outPuts.size() > 0) {

                List<String> mns = new ArrayList<>();
                String mnCommon;
                for (Map<String, Object> output : outPuts) {
                    mnCommon = output.get("dgimn") != null ? output.get("dgimn").toString() : "";
                    if (StringUtils.isNotBlank(mnCommon) && !mns.equals(mnCommon)) {
                        mns.add(mnCommon);
                    }
                }
                //获取土壤监测数据
                paramMap.clear();
                starttime = starttime + " 00:00:00";
                endtime = endtime + " 23:59:59";
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("pollutantcodes", Arrays.asList(soilpollutantcode));
                paramMap.put("mns", Arrays.asList(monitorpointid));
                paramMap.put("collection", db_dayData);
                List<Document> soilDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                if (soilDocuments.size() > 0) {
                    Map<String, Double> timeAndValue = new HashMap<>();
                    String ym;
                    Double value;
                    List<Document> pollutantList;
                    for (Document document : soilDocuments) {
                        ym = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                        pollutantList = document.get("DayDataList", List.class);
                        for (Document pollutant : pollutantList) {
                            if (soilpollutantcode.equals(pollutant.get("PollutantCode"))) {
                                value = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                if (value != null) {
                                    timeAndValue.put(ym, value);
                                }
                                break;
                            }
                        }
                    }
                    if (timeAndValue.size() > 0) {
                        //获取废气点位月数据
                        paramMap.put("mns", mns);
                        paramMap.put("collection", db_monthData);
                        paramMap.put("pollutantcodes", Arrays.asList(outputpollutantcode));
                        List<Document> outputDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                        if (outputDocuments.size() > 0) {
                            Map<String, Map<String, Double>> mnAndTimeAndValue = new HashMap<>();
                            Map<String, Double> timeAndValueOutPut;
                            for (Document document : outputDocuments) {
                                mnCommon = document.getString("DataGatherCode");
                                ym = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                                pollutantList = document.get("MonthDataList", List.class);
                                for (Document pollutant : pollutantList) {
                                    if (outputpollutantcode.equals(pollutant.get("PollutantCode"))) {
                                        value = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                        if (value != null) {
                                            if (mnAndTimeAndValue.containsKey(mnCommon)) {
                                                timeAndValueOutPut = mnAndTimeAndValue.get(mnCommon);
                                            } else {
                                                timeAndValueOutPut = new HashMap<>();
                                            }
                                            timeAndValueOutPut.put(ym, value);
                                            mnAndTimeAndValue.put(mnCommon, timeAndValueOutPut);
                                        }
                                        break;
                                    }
                                }
                            }

                            if (mnAndTimeAndValue.size() > 0) {
                                List<Map<String, Object>> xListData = new ArrayList<>();
                                List<Map<String, Object>> yListData = new ArrayList<>();
                                List<Double> xData = new ArrayList<>();
                                List<Double> yData = new ArrayList<>();
                                mnCommon = mns.get(0);
                                timeAndValueOutPut = mnAndTimeAndValue.get(mnCommon);
                                for (String time : timeAndValue.keySet()) {
                                    if (timeAndValueOutPut.containsKey(time)) {
                                        Map<String, Object> xMap = new LinkedHashMap<>();
                                        Map<String, Object> yMap = new LinkedHashMap<>();
                                        xMap.put("monitortime", time);
                                        xMap.put("value", timeAndValue.get(time));
                                        yMap.put("monitortime", time);
                                        yMap.put("value", timeAndValueOutPut.get(time));
                                        yData.add(timeAndValueOutPut.get(time));
                                        xData.add(timeAndValue.get(time));
                                        xListData.add(xMap);
                                        yListData.add(yMap);
                                    }
                                }
                                Double xMax = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(Collections.max(xData)));
                                Double yMax = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(Collections.max(yData)));
                                Double slope = DataFormatUtil.getRelationSlope(xData, yData);
                                Double constant = DataFormatUtil.getRelationConstant(xData, yData, slope);
                                resultMap.put("slope", slope);
                                resultMap.put("constant", constant);
                                if (pagesize != null && pagenum != null) {
                                    resultMap.put("total", xListData.size());
                                    xListData = getPageData(xListData, pagenum, pagesize);
                                    yListData = getPageData(yListData, pagenum, pagesize);
                                }
                                resultMap.put("xlistdata", xListData);
                                resultMap.put("ylistdata", yListData);
                                resultMap.put("startPointData", Arrays.asList(0, yMax));
                                Double y = slope * xMax + constant;
                                y = Double.parseDouble(DataFormatUtil.formatDoubleSaveTwo(y));
                                resultMap.put("endPointData", Arrays.asList(xMax, y));
                            }
                        }
                    }
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/3/24 0024 下午 3:47
     * @Description: 获取相似性列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getRelationDataList(String monitorpointid, String monitorpointname, String soilpollutantcode, String soilpollutantname, String starttime, String endtime, String outputpollutantcode, String outputpollutantname, Integer monitorpointtypecode) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitorpointtype", monitorpointtypecode);

        List<Map<String, Object>> outPuts;
        if (monitorpointtypecode == CommonTypeEnum.MonitorPointTypeEnum.WasteWaterEnum.getCode()) {
            paramMap.put("outputtype", "water");
            outPuts = waterOutPutInfoService.getOnlineWaterOutPutInfoByParamMap(paramMap);
        } else {
            outPuts = gasOutPutInfoService.getOnlineGasOutPutInfoByParamMap(paramMap);
        }
        if (outPuts.size() > 0) {
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndOutPutName = new HashMap<>();
            Map<String, Object> mnAndOutPutId = new HashMap<>();
            String mnCommon;
            String outPutName;
            String pollutionName;
            for (Map<String, Object> output : outPuts) {
                mnCommon = output.get("dgimn") != null ? output.get("dgimn").toString() : "";
                if (StringUtils.isNotBlank(mnCommon) && !mns.equals(mnCommon)) {
                    outPutName = output.get("outputname") + "";
                    pollutionName = output.get("pollutionname") + "";
                    mnAndOutPutName.put(mnCommon, pollutionName + "-" + outPutName);
                    mnAndOutPutId.put(mnCommon, output.get("monitorpointid"));
                    mns.add(mnCommon);
                }
            }
            //获取土壤监测数据
            paramMap.clear();
            starttime = starttime + " 00:00:00";
            endtime = endtime + " 23:59:59";
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantcodes", Arrays.asList(soilpollutantcode));
            paramMap.put("mns", Arrays.asList(monitorpointid));
            paramMap.put("collection", db_dayData);
            List<Document> soilDocuments = onlineService.getMonitorDataByParamMap(paramMap);
            if (soilDocuments.size() > 0) {
                Map<String, Double> timeAndValue = new HashMap<>();
                String ym;
                Double value;
                List<Document> pollutantList;
                for (Document document : soilDocuments) {
                    ym = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                    pollutantList = document.get("DayDataList", List.class);
                    for (Document pollutant : pollutantList) {
                        if (soilpollutantcode.equals(pollutant.get("PollutantCode"))) {
                            value = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                            if (value != null) {
                                timeAndValue.put(ym, value);
                            }
                            break;
                        }
                    }
                }
                if (timeAndValue.size() > 0) {
                    //获取废气点位月数据
                    paramMap.put("mns", mns);
                    paramMap.put("collection", db_monthData);
                    paramMap.put("pollutantcodes", Arrays.asList(outputpollutantcode));
                    List<Document> outputDocuments = onlineService.getMonitorDataByParamMap(paramMap);
                    if (outputDocuments.size() > 0) {
                        Map<String, Map<String, Double>> mnAndTimeAndValue = new HashMap<>();
                        Map<String, Double> timeAndValueOutPut;
                        for (Document document : outputDocuments) {
                            mnCommon = document.getString("DataGatherCode");
                            ym = DataFormatUtil.getDateYM(document.getDate("MonitorTime"));
                            pollutantList = document.get("MonthDataList", List.class);
                            for (Document pollutant : pollutantList) {
                                if (outputpollutantcode.equals(pollutant.get("PollutantCode"))) {
                                    value = pollutant.get("AvgStrength") != null ? Double.parseDouble(pollutant.get("AvgStrength").toString()) : null;
                                    if (value != null) {
                                        if (mnAndTimeAndValue.containsKey(mnCommon)) {
                                            timeAndValueOutPut = mnAndTimeAndValue.get(mnCommon);
                                        } else {
                                            timeAndValueOutPut = new HashMap<>();
                                        }
                                        timeAndValueOutPut.put(ym, value);
                                        mnAndTimeAndValue.put(mnCommon, timeAndValueOutPut);
                                    }
                                    break;
                                }
                            }
                        }
                        if (mnAndTimeAndValue.size() > 0) {
                            Double relationpercent;
                            for (String mnKey : mnAndTimeAndValue.keySet()) {
                                Map<String, Object> dataMap = new HashMap<>();

                                dataMap.put("monitorpointid", monitorpointid);
                                dataMap.put("soilpointname", monitorpointname);
                                dataMap.put("soilpollutantname", soilpollutantname);
                                dataMap.put("outputid", mnAndOutPutId.get(mnKey));
                                dataMap.put("outputname", mnAndOutPutName.get(mnKey));
                                dataMap.put("outputpollutantname", outputpollutantname);
                                relationpercent = getRelationPercent(mnAndTimeAndValue.get(mnKey), timeAndValue);
                                if (relationpercent != null) {
                                    dataMap.put("relationpercent", DataFormatUtil.SaveTwoAndSubZero(relationpercent));
                                    dataList.add(dataMap);
                                }

                            }
                        }

                    }
                }
            }
        }
        //根据相关度倒序
        Collections.sort(dataList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Double one = Double.parseDouble(o1.get("relationpercent").toString());
                Double other = Double.parseDouble(o2.get("relationpercent").toString());
                Double one1 = Math.abs(one);
                Double other1 = Math.abs(other);
                return other1.compareTo(one1);
            }
        });

        return dataList;

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
     * @author: lip
     * @date: 2020/3/24 0024 下午 2:17
     * @Description: 获取相关性系数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Double getRelationPercent(Map<String, Double> timeAndValueOutPut, Map<String, Double> timeAndValueSoil) {
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        for (String time : timeAndValueSoil.keySet()) {
            if (timeAndValueOutPut.containsKey(time)) {
                yData.add(timeAndValueSoil.get(time));
                xData.add(timeAndValueOutPut.get(time));
            }
        }
        Double relationpercent = DataFormatUtil.getRelationPercent(xData, yData);
        return relationpercent;
    }


    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 3:42
     * @Description: 新增土壤监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "addSoilOnlineData", method = RequestMethod.POST)
    public Object addSoilOnlineData(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            List<Map<String, Object>> datalist = (List<Map<String, Object>>) paramsjson;
            for (Map<String, Object> map : datalist) {
                DayDataVO dayDataVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(map), new DayDataVO());
                mongoBaseService.save(dayDataVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 4:32
     * @Description: 修改土壤在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "updateSoilOnlineData", method = RequestMethod.POST)
    public Object updateSoilOnlineData(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            List<Map<String, Object>> datalist = (List<Map<String, Object>>) paramsjson;
            for (Map<String, Object> map : datalist) {
                DayDataVO dayDataVO = JSONObjectUtil.JsonObjectToEntity(JSONObject.fromObject(map), new DayDataVO());
                mongoBaseService.update(dayDataVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 6:22
     * @Description: 在线数据回显
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "getSoilOnlineDataById", method = RequestMethod.POST)
    public Object getSoilOnlineDataById(@RequestJson(value = "id") String id) throws Exception {
        try {
            DayDataSelectVO dayDataSelectVO = new DayDataSelectVO();
            dayDataSelectVO.setId(id);
            List<DayDataSelectVO> dayData = mongoBaseService.getListByParam(dayDataSelectVO, db_dayData, "yyyy-MM-dd");

            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> soil = soilPointService.getSoilPointInfoByParamMap(paramMap);

            for (DayDataSelectVO dayDatum : dayData) {
                String dataGatherCode = dayDatum.getDataGatherCode();
                dayDatum.setMonitorTime(OnlineController.formatCSTString(dayDatum.getMonitorTime(), "yyyy-MM-dd"));
                soil.stream().filter(m -> m.get("monitorpointid") != null && dataGatherCode.equals(m.get("monitorpointid").toString())).forEach(m -> {
                    Map<String, Object> extdata = new HashMap<>();
                    extdata.put("pk_pollutionid", m.get("pk_pollutionid"));
                    extdata.put("fk_soilpointtypecode", m.get("fk_soilpointtypecode"));
                    dayDatum.setExtdata(extdata);
                });

            }

            return AuthUtil.parseJsonKeyToLower("success", dayData);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/9/9 0009 上午 8:56
     * @Description: 自定义查询条件获取土壤监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getSoilMonitorDataByParam", method = RequestMethod.POST)
    public Object getSoilMonitorDataByParam(
            @RequestJson(value = "monitorpointids") List<String> monitorpointids,
            @RequestJson(value = "pollutantdata") List<Map<String, Object>> pollutantdata,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime


    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointids", monitorpointids);
            List<Map<String, Object>> pointDataList = soilPointService.getEntSoilPointByParamMap(paramMap);
            if (pointDataList.size() > 0) {
                Map<String, Object> mnAndName = new HashMap<>();
                String mnCommon;
                for (Map<String, Object> pointData : pointDataList) {
                    mnCommon = pointData.get("monitorpointid").toString();
                    mnAndName.put(mnCommon, pointData.get("shortername") + "-" + pointData.get("monitorpointname"));
                }
                starttime = DataFormatUtil.getFirstDayOfMonth(starttime + "-01") + " 00:00:00";
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";

                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", monitorpointids);
                Map<String, Object> codeAndName = new HashMap<>();
                List<String> pollutantCodes = new ArrayList<>();
                String pollutantCode;
                for (Map<String, Object> pollutant : pollutantdata) {
                    pollutantCode = pollutant.get("pollutantcode").toString();
                    codeAndName.put(pollutantCode, pollutant.get("pollutantname"));
                    pollutantCodes.add(pollutantCode);
                }
                paramMap.put("pollutantcodes", pollutantCodes);
                paramMap.put("collection", db_dayData);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                String monitorTime;

                Map<String, List<Map<String, Object>>> keyAndDataList = new HashMap<>();
                List<Map<String, Object>> dataList;
                String key;
                List<Document> pollutantList;
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    monitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    pollutantList = document.get("DayDataList", List.class);
                    for (Document pollutant : pollutantList) {
                        pollutantCode = pollutant.getString("PollutantCode");
                        if (pollutantCodes.contains(pollutantCode)) {
                            key = mnCommon + "_" + pollutantCode;
                            if (keyAndDataList.containsKey(key)) {
                                dataList = keyAndDataList.get(key);
                            } else {
                                dataList = new ArrayList<>();
                            }
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitortime", monitorTime);
                            dataMap.put("monitorvalue", pollutant.get("AvgStrength"));
                            dataList.add(dataMap);
                            keyAndDataList.put(key, dataList);
                        }
                    }
                }
                if (keyAndDataList.size() > 0) {
                    for (String keyIndex : keyAndDataList.keySet()) {
                        Map<String, Object> resultMap = new HashMap<>();
                        mnCommon = keyIndex.split("_")[0];
                        pollutantCode = keyIndex.split("_")[1];
                        resultMap.put("monitorpointid", mnCommon);
                        resultMap.put("monitorpointname", mnAndName.get(mnCommon));
                        resultMap.put("pollutantcode", pollutantCode);
                        resultMap.put("pollutantname", codeAndName.get(pollutantCode));
                        dataList = keyAndDataList.get(keyIndex);
                        //排序
                        Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                        if (comparebynum != null) {
                            dataList = dataList.stream().sorted(comparebynum).collect(Collectors.toList());
                        }
                        resultMap.put("datalist", dataList);
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
     * @date: 2020/9/9 0009 上午 8:56
     * @Description: 自定义查询条件统计土壤评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countSoilEvaluateDataByParam", method = RequestMethod.POST)
    public Object countSoilEvaluateDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> pointDataList = soilPointService.getEntSoilPointByParamMap(paramMap);
            if (pointDataList.size() > 0) {
                String mnCommon;
                List<String> mns = new ArrayList<>();
                Map<String, Object> typeAndName = new HashMap<>();
                String type;
                Map<String, String> mnAndType = new HashMap<>();
                for (Map<String, Object> pointData : pointDataList) {
                    mnCommon = pointData.get("monitorpointid").toString();
                    mns.add(mnCommon);
                    if (pointData.get("soilpointtype") != null) {
                        type = pointData.get("soilpointtype").toString();
                        mnAndType.put(mnCommon, type);
                        typeAndName.put(type, pointData.get("shortername"));

                    }
                }
                starttime = DataFormatUtil.getFirstDayOfMonth(starttime + "-01") + " 00:00:00";
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", mns);
                paramMap.put("collection", db_dayData);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    paramMap.put("monitorpointids", mns);
                    List<Map<String, Object>> standardDataList = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
                    if (standardDataList.size() > 0) {
                        List<String> YJ_Codes = new ArrayList<>();
                        List<String> WJ_Codes = new ArrayList<>();
                        for (Map<String, Object> standardData : standardDataList) {
                            if ("1".equals(standardData.get("organicinorganic") + "")) {
                                YJ_Codes.add(standardData.get("PollutantCode") + "");
                            } else if ("2".equals(standardData.get("organicinorganic") + "")) {
                                WJ_Codes.add(standardData.get("PollutantCode") + "");
                            }
                        }


                        String pollutantCode;
                        List<Document> pollutantList;
                        int levelCode;
                        Map<Integer, Integer> YJ_LevelAndNum = new HashMap<>();
                        Map<Integer, Integer> WJ_LevelAndNum = new HashMap<>();
                        int countnum;
                        Map<String, Map<Integer, Integer>> typeAndLevelAndNum = new HashMap<>();
                        Map<Integer, Integer> levelAndNum;
                        for (Document document : documents) {
                            mnCommon = document.getString("DataGatherCode");
                            pollutantList = document.get("DayDataList", List.class);
                            for (Document pollutant : pollutantList) {
                                pollutantCode = pollutant.getString("PollutantCode");
                                levelCode = getLevelCode(pollutant, mnCommon, standardDataList);
                                if (levelCode > 0) {
                                    if (YJ_Codes.contains(pollutantCode)) {
                                        countnum = YJ_LevelAndNum.get(levelCode) != null ? YJ_LevelAndNum.get(levelCode) : 0;
                                        YJ_LevelAndNum.put(levelCode, countnum + 1);
                                    } else if (WJ_Codes.contains(pollutantCode)) {
                                        countnum = WJ_LevelAndNum.get(levelCode) != null ? WJ_LevelAndNum.get(levelCode) : 0;
                                        WJ_LevelAndNum.put(levelCode, countnum + 1);
                                    }
                                    type = mnAndType.get(mnCommon);
                                    if (typeAndLevelAndNum.containsKey(type)) {
                                        levelAndNum = typeAndLevelAndNum.get(type);
                                    } else {
                                        levelAndNum = new HashMap<>();
                                    }
                                    countnum = levelAndNum.get(levelCode) != null ? levelAndNum.get(levelCode) : 0;
                                    levelAndNum.put(levelCode, countnum + 1);
                                    typeAndLevelAndNum.put(type, levelAndNum);
                                }
                            }
                        }
                        if (typeAndLevelAndNum.size() > 0) {
                            String counttype;
                            for (String typeIndex : typeAndLevelAndNum.keySet()) {
                                if (typeIndex.equals("1")) {
                                    counttype = "企业用地";
                                } else {
                                    counttype = typeAndName.get(typeIndex) + "用地";
                                }
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("counttype", counttype);
                                List<Map<String, Object>> dataList = new ArrayList<>();
                                levelAndNum = typeAndLevelAndNum.get(typeIndex);
                                for (Integer levelIndex : levelAndNum.keySet()) {
                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("levelcode", levelIndex);
                                    dataMap.put("levelname", CommonTypeEnum.SoilEvaluateLevelEnum.getNameByCode(levelIndex));
                                    dataMap.put("countnum", levelAndNum.get(levelIndex));
                                    dataList.add(dataMap);
                                }
                                resultMap.put("dataList", dataList);
                                resultList.add(resultMap);
                            }

                            if (YJ_LevelAndNum.size() > 0) {
                                Map<String, Object> YJ_ResultMap = new HashMap<>();
                                YJ_ResultMap.put("counttype", "有机土壤");
                                List<Map<String, Object>> dataList = new ArrayList<>();
                                for (Integer levelIndex : YJ_LevelAndNum.keySet()) {
                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("levelcode", levelIndex);
                                    dataMap.put("levelname", CommonTypeEnum.SoilEvaluateLevelEnum.getNameByCode(levelIndex));
                                    dataMap.put("countnum", YJ_LevelAndNum.get(levelIndex));
                                    dataList.add(dataMap);
                                }
                                YJ_ResultMap.put("dataList", dataList);
                                resultList.add(YJ_ResultMap);
                            }

                            if (WJ_LevelAndNum.size() > 0) {
                                Map<String, Object> WJ_ResultMap = new HashMap<>();
                                WJ_ResultMap.put("counttype", "无机土壤");
                                List<Map<String, Object>> dataList = new ArrayList<>();
                                for (Integer levelIndex : WJ_LevelAndNum.keySet()) {
                                    Map<String, Object> dataMap = new HashMap<>();
                                    dataMap.put("levelcode", levelIndex);
                                    dataMap.put("levelname", CommonTypeEnum.SoilEvaluateLevelEnum.getNameByCode(levelIndex));
                                    dataMap.put("countnum", WJ_LevelAndNum.get(levelIndex));
                                    dataList.add(dataMap);
                                }
                                WJ_ResultMap.put("dataList", dataList);
                                resultList.add(WJ_ResultMap);
                            }
                        }
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
     * @date: 2020/9/9 0009 上午 8:56
     * @Description: 自定义查询条件统计土壤评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLastSoilEvaluateDataByParam", method = RequestMethod.POST)
    public Object countSoilEvaluateDataByParam(
            @RequestJson(value = "soilpointtype") Integer soilpointtype) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("soilpointtype", soilpointtype);

            List<Map<String, Object>> pointDataList = soilPointService.getEntSoilPointByParamMap(paramMap);
            if (pointDataList.size() > 0) {
                String mnCommon;
                List<String> mns = new ArrayList<>();
                Map<String, Object> idAndName = new HashMap<>();
                for (Map<String, Object> pointData : pointDataList) {
                    mnCommon = pointData.get("monitorpointid").toString();
                    mns.add(mnCommon);
                    idAndName.put(mnCommon, pointData.get("shortername") + "-" + pointData.get("monitorpointname"));
                }
                paramMap.put("mns", mns);
                paramMap.put("collection", db_dayData);
                paramMap.put("sort", "desc");
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                Map<String, Map<String, Integer>> mnAndTimeAndLevel = new HashMap();
                Map<String, Integer> timeAndLevel;
                if (documents.size() > 0) {
                    paramMap.put("monitorpointids", mns);
                    List<Map<String, Object>> standardDataList = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
                    if (standardDataList.size() > 0) {
                        String monitortime;
                        List<Document> pollutantList;
                        int pointLevelCode;
                        int pollutantLevelCode;
                        for (Document document : documents) {
                            mnCommon = document.getString("DataGatherCode");
                            if (mnAndTimeAndLevel.containsKey(mnCommon)) {
                                timeAndLevel = mnAndTimeAndLevel.get(mnCommon);
                            } else {
                                timeAndLevel = new HashMap<>();
                            }
                            monitortime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                            if (timeAndLevel.containsKey(monitortime)) {
                                pointLevelCode = timeAndLevel.get(monitortime);
                            } else {
                                pointLevelCode = 0;
                            }
                            pollutantList = document.get("DayDataList", List.class);
                            for (Document pollutant : pollutantList) {
                                pollutantLevelCode = getLevelCode(pollutant, mnCommon, standardDataList);
                                if (pollutantLevelCode > pointLevelCode) {
                                    pointLevelCode = pollutantLevelCode;
                                }
                            }
                            timeAndLevel.put(monitortime, pointLevelCode);
                            mnAndTimeAndLevel.put(mnCommon, timeAndLevel);
                        }
                    }
                }
                List<String> timeCode;
                for (String id : idAndName.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointid", id);
                    resultMap.put("monitorpointntname", idAndName.get(id));
                    if (mnAndTimeAndLevel.containsKey(id)) {
                        timeAndLevel = mnAndTimeAndLevel.get(id);
                        timeAndLevel = DataFormatUtil.sortByKey(timeAndLevel, true);
                        timeCode = new ArrayList<>();
                        for (String time : timeAndLevel.keySet()) {
                            timeCode.add(time + "," + timeAndLevel.get(time));
                        }
                        if (timeAndLevel.size() >= 2) {
                            resultMap.put("evaluatedate", timeCode.get(0).split(",")[0]);
                            resultMap.put("thislevel", timeCode.get(0).split(",")[1]);
                            resultMap.put("thatlevel", timeCode.get(1).split(",")[1]);
                        } else {
                            resultMap.put("evaluatedate", timeCode.get(0).split(",")[0]);
                            resultMap.put("thislevel", timeCode.get(0).split(",")[1]);
                            resultMap.put("thatlevel", "-");
                        }
                    } else {
                        resultMap.put("evaluatedate", "-");
                        resultMap.put("thislevel", "-");
                        resultMap.put("thatlevel", "-");
                    }
                    resultList.add(resultMap);
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
     * @date: 2020/9/9 0009 上午 8:56
     * @Description: 自定义查询条件统计土壤监测点评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countSoilPointEvaluateDataByParam", method = RequestMethod.POST)
    public Object countSoilPointEvaluateDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> pointDataList = soilPointService.getEntSoilPointByParamMap(paramMap);
            if (pointDataList.size() > 0) {
                String mnCommon;
                List<String> mns = new ArrayList<>();
                Map<String, Object> typeAndName = new HashMap<>();
                Map<String, Object> idAndName = new HashMap<>();
                String type;
                Map<String, String> mnAndType = new HashMap<>();
                for (Map<String, Object> pointData : pointDataList) {
                    mnCommon = pointData.get("monitorpointid").toString();
                    idAndName.put(mnCommon, pointData.get("shortername") + "-" + pointData.get("monitorpointname"));
                    mns.add(mnCommon);
                    if (pointData.get("soilpointtype") != null) {
                        type = pointData.get("soilpointtype").toString();
                        mnAndType.put(mnCommon, type);
                        typeAndName.put(type, pointData.get("shortername"));

                    }
                }
                starttime = DataFormatUtil.getFirstDayOfMonth(starttime + "-01") + " 00:00:00";
                endtime = DataFormatUtil.getLastDayOfMonth(endtime + "-12") + " 23:59:59";
                paramMap.put("starttime", starttime);
                paramMap.put("endtime", endtime);
                paramMap.put("mns", mns);
                paramMap.put("collection", db_dayData);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                if (documents.size() > 0) {
                    paramMap.put("monitorpointids", mns);
                    List<Map<String, Object>> standardDataList = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
                    if (standardDataList.size() > 0) {
                        List<Document> pollutantList;
                        int levelCode;
                        int countnum;
                        Map<String, Map<Integer, Integer>> mnAndLevelAndNum = new HashMap<>();
                        Map<Integer, Integer> levelAndNum;
                        for (Document document : documents) {
                            mnCommon = document.getString("DataGatherCode");
                            pollutantList = document.get("DayDataList", List.class);
                            for (Document pollutant : pollutantList) {
                                levelCode = getLevelCode(pollutant, mnCommon, standardDataList);
                                if (levelCode > 0) {

                                    if (mnAndLevelAndNum.containsKey(mnCommon)) {
                                        levelAndNum = mnAndLevelAndNum.get(mnCommon);
                                    } else {
                                        levelAndNum = new HashMap<>();
                                    }
                                    countnum = levelAndNum.get(levelCode) != null ? levelAndNum.get(levelCode) : 0;
                                    levelAndNum.put(levelCode, countnum + 1);
                                    mnAndLevelAndNum.put(mnCommon, levelAndNum);
                                }
                            }
                        }
                        if (mnAndLevelAndNum.size() > 0) {
                            String soilType;
                            List<Integer> levelCodes = Arrays.asList(CommonTypeEnum.SoilEvaluateLevelEnum.OneLevelEnum.getCode()
                                    , CommonTypeEnum.SoilEvaluateLevelEnum.TwoLevelEnum.getCode(),
                                    CommonTypeEnum.SoilEvaluateLevelEnum.ThreeLevelEnum.getCode(),
                                    CommonTypeEnum.SoilEvaluateLevelEnum.FourLevelEnum.getCode());
                            for (String mnIndex : mnAndLevelAndNum.keySet()) {
                                type = mnAndType.get(mnIndex);
                                if (type.toString().equals("1")) {
                                    soilType = "企业用地";
                                } else {
                                    soilType = typeAndName.get(type) + "用地";
                                }
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("soiltype", soilType);
                                resultMap.put("monitorpointid", mnIndex);
                                resultMap.put("monitorpointname", idAndName.get(mnIndex));
                                levelAndNum = mnAndLevelAndNum.get(mnIndex);
                                for (Integer levelIndex : levelCodes) {
                                    if (levelAndNum.containsKey(levelIndex)) {
                                        resultMap.put("level" + levelIndex, levelAndNum.get(levelIndex));
                                    } else {
                                        resultMap.put("level" + levelIndex, 0);
                                    }
                                }
                                resultList.add(resultMap);
                            }
                        }
                    }
                }
            }
            if (resultList.size() > 0) {
                //排序
                Comparator<Object> compare1 = Comparator.comparing(m -> ((Map) m).get("soiltype").toString());
                Comparator<Object> compare2 = Comparator.comparing(m -> ((Map) m).get("monitorpointname").toString());
                Comparator<Object> finalComparator = compare1.thenComparing(compare2);
                resultList = resultList.stream().sorted(finalComparator).collect(Collectors.toList());
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private int getLevelCode(Document pollutant, String monitortpointid, List<Map<String, Object>> standardDataList) {
        int levelCode = CommonTypeEnum.SoilEvaluateLevelEnum.NormalLevelEnum.getCode();
        if (pollutant.get("AvgStrength") != null) {
            for (Map<String, Object> pointStandard : standardDataList) {
                if (monitortpointid.equals(pointStandard.get("PK_ID"))
                        && pollutant.getString("PollutantCode").equals(pointStandard.get("PollutantCode"))) {
                    Double monitorValue = Double.parseDouble(pollutant.getString("AvgStrength"));
                    Double oneLevel = pointStandard.get("OneLevel") != null ? Double.parseDouble(pointStandard.get("OneLevel").toString()) : 0d;
                    Double twoLevel = pointStandard.get("TwoLevel") != null ? Double.parseDouble(pointStandard.get("TwoLevel").toString()) : 0d;
                    Double threeLevel = pointStandard.get("ThreeLevel") != null ? Double.parseDouble(pointStandard.get("ThreeLevel").toString()) : 0d;
                    Double fourLevel = pointStandard.get("FourLevel") != null ? Double.parseDouble(pointStandard.get("FourLevel").toString()) : 0d;
                    if (monitorValue > oneLevel && monitorValue <= twoLevel) {
                        levelCode = CommonTypeEnum.SoilEvaluateLevelEnum.OneLevelEnum.getCode();
                    } else if (monitorValue > twoLevel && monitorValue <= threeLevel) {
                        levelCode = CommonTypeEnum.SoilEvaluateLevelEnum.TwoLevelEnum.getCode();
                    } else if (monitorValue > threeLevel && monitorValue <= fourLevel) {
                        levelCode = CommonTypeEnum.SoilEvaluateLevelEnum.ThreeLevelEnum.getCode();
                    } else if (monitorValue > fourLevel) {
                        levelCode = CommonTypeEnum.SoilEvaluateLevelEnum.FourLevelEnum.getCode();
                    }
                }
            }
        }
        return levelCode;

    }


    /**
     * @author: chengzq
     * @date: 2020/3/27 0027 上午 10:26
     * @Description: 删除土壤在线数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "deleteSoilOnlineDataById", method = RequestMethod.POST)
    public Object deleteSoilOnlineDataById(@RequestJson(value = "id") String id) throws Exception {
        try {
            DayDataSelectVO dayDataSelectVO = new DayDataSelectVO();
            dayDataSelectVO.setId(id);
            mongoBaseService.delete(dayDataSelectVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 下午 1:34
     * @Description: 通过自定义条件查询土壤监测数据列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson]
     * @throws:
     */
    @RequestMapping(value = "getSoilOnlineData", method = RequestMethod.POST)
    public Object getSoilOnlineData(@RequestJson(value = "paramsjson") Object paramsjson) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(paramsjson);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> allSoilPointInfo = soilPointService.getSoilPointInfoByParamMap(jsonObject);
            jsonObject.put("pollutanttype", soilEnum.getCode());
            List<Map<String, Object>> soilPollutantsByParam = soilPointService.getSoilPollutantsByParam(jsonObject);

            List<String> monitorpointids = allSoilPointInfo.stream().filter(m -> m.get("monitorpointid") != null).map(m -> m.get("monitorpointid").toString()).collect(Collectors.toList());

            jsonObject.put("mns", monitorpointids);
            if (jsonObject.get("starttime") != null && jsonObject.get("endtime") != null && !jsonObject.get("starttime").toString().equals("") && !jsonObject.get("endtime").toString().equals("")) {
                jsonObject.put("starttime", jsonObject.getString("starttime") + " 00:00:00");
                jsonObject.put("endtime", jsonObject.getString("endtime") + " 23:59:59");
            } else {
                jsonObject.remove("starttime");
                jsonObject.remove("endtime");
            }
            jsonObject.put("monitortimekey", "DayDataList");
            jsonObject.put("collection", "DayData");
            Integer pagesize = jsonObject.get("pagesize") == null ? Integer.MAX_VALUE : Integer.valueOf(jsonObject.get("pagesize").toString());
            Integer pagenum = jsonObject.get("pagenum") == null ? 1 : Integer.valueOf(jsonObject.get("pagenum").toString());
            jsonObject.remove("pagesize");
            jsonObject.remove("pagenum");

            List<Map<String, Object>> daydata = onlineService.getMonitorDataByParamMap(jsonObject);


            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Map<String, Object> daydatum : daydata) {
                Map<String, Object> data = new HashMap<>();
                String monitorTime = OnlineController.formatCSTString(daydatum.get("MonitorTime").toString(), "yyyy-MM-dd");
                String DataGatherCode = daydatum.get("DataGatherCode") == null ? "" : daydatum.get("DataGatherCode").toString();
                String id = daydatum.get("_id") == null ? "" : daydatum.get("_id").toString();
                List<Map<String, Object>> DayDataList = (List<Map<String, Object>>) daydatum.get("DayDataList");
                data.put("monitortime", monitorTime);
                data.put("DataGatherCode", DataGatherCode);
                data.put("id", id);
                for (Map<String, Object> map : DayDataList) {
                    String PollutantCode = map.get("PollutantCode") == null ? "" : map.get("PollutantCode").toString();
                    String AvgStrength = map.get("AvgStrength") == null ? "" : map.get("AvgStrength").toString();
                    data.put(PollutantCode, AvgStrength);
                }
                Optional<Map<String, Object>> first = allSoilPointInfo.stream().filter(m -> m.get("monitorpointid") != null && DataGatherCode.equals(m.get("monitorpointid").toString())).findFirst();
                if (first.isPresent()) {
                    data.put("monitorpointname", first.get().get("monitorpointname"));
                    data.put("pollutionname", first.get().get("pollutionname"));
                }

                resultList.add(data);

            }

            int total = resultList.size();
            resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());

            resultMap.put("total", total);
            resultMap.put("resultList", resultList);
            resultMap.put("title", soilPollutantsByParam.stream().distinct().collect(Collectors.toList()));
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/9/7 0007 下午 1:36
     * @Description: 获取土壤点位地理信息数据及评价数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getSoilGisOnlineDataByParam", method = RequestMethod.POST)
    public Object getSoilGisOnlineDataByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime


    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();


            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> allSoilPointInfo = soilPointService.getSoilPointInfoByParamMap(paramMap);
            List<String> monitorpointids = allSoilPointInfo.stream().filter(m -> m.get("monitorpointid") != null).map(m -> m.get("monitorpointid").toString()).collect(Collectors.toList());
            paramMap.put("mns", monitorpointids);
            paramMap.put("starttime", starttime + " 00:00:00");
            paramMap.put("endtime", endtime + " 23:59:59");
            paramMap.put("monitortimekey", "DayDataList");
            paramMap.put("collection", db_dayData);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            if (documents.size() > 0) {
                List<Document> pollutantList;
                String mnCommon;
                String monitorTime;
                Map<String, Map<String, List<Document>>> mnAndTimeAndPollutant = new HashMap<>();
                Map<String, List<Document>> timeAndPollutant;
                for (Document document : documents) {
                    mnCommon = document.getString("DataGatherCode");
                    pollutantList = document.get("DayDataList", List.class);
                    monitorTime = DataFormatUtil.getDateYMD(document.getDate("MonitorTime"));
                    if (mnAndTimeAndPollutant.containsKey(mnCommon)) {
                        timeAndPollutant = mnAndTimeAndPollutant.get(mnCommon);
                    } else {
                        timeAndPollutant = new HashMap<>();
                    }
                    timeAndPollutant.put(monitorTime, pollutantList);
                    mnAndTimeAndPollutant.put(mnCommon, timeAndPollutant);
                }
                paramMap.put("monitorpointids", monitorpointids);
                List<Map<String, Object>> pointStandardDataList = soilPointService.getSoilPointsAndPollutantStandard(paramMap);
                Map<String, Map<String, Map<String, Object>>> mnAndTimeAndPollutantData = new HashMap<>();
                paramMap.put("pollutanttype", soilEnum.getCode());
                List<Map<String, Object>> pollutants = pollutantService.getPollutantsByCodesAndType(paramMap);
                Map<String, Object> codeAndName = new HashMap<>();
                for (Map<String, Object> pollutant : pollutants) {
                    codeAndName.put(pollutant.get("code").toString(), pollutant.get("name"));
                }
                Map<String, Map<String, Object>> mnAndLevelData = new HashMap<>();
                if (pointStandardDataList.size() > 0) {
                    Map<String, Object> levelData;
                    for (String mnIndex : mnAndTimeAndPollutant.keySet()) {
                        timeAndPollutant = mnAndTimeAndPollutant.get(mnIndex);
                        Map<String, Map<String, Object>> timeAndPollutantData = new HashMap<>();
                        for (String timeIndex : timeAndPollutant.keySet()) {
                            pollutantList = timeAndPollutant.get(timeIndex);
                            Map<String, Object> codeAndLevelData = new HashMap<>();
                            for (Document pollutant : pollutantList) {
                                levelData = setMnAndDataList(mnAndLevelData, mnIndex, pollutant, pointStandardDataList);
                                codeAndLevelData.put(pollutant.getString("PollutantCode"), levelData);
                            }
                            timeAndPollutantData.put(timeIndex, codeAndLevelData);
                        }
                        mnAndTimeAndPollutantData.put(mnIndex, timeAndPollutantData);
                    }
                }
                String pollutantCode;
                for (Map<String, Object> point : allSoilPointInfo) {
                    mnCommon = point.get("monitorpointid").toString();
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointid", mnCommon);
                    resultMap.put("monitorpointname", point.get("monitorpointname"));
                    resultMap.put("longitude", point.get("Longitude"));
                    resultMap.put("latitude", point.get("Latitude"));
                    if (mnAndLevelData.containsKey(mnCommon)) {
                        resultMap.putAll(mnAndLevelData.get(mnCommon));
                    } else {
                        resultMap.put("levelcode", "");
                        resultMap.put("levelname", "");
                    }
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    timeAndPollutant = mnAndTimeAndPollutant.get(mnCommon);
                    if (timeAndPollutant != null) {
                        for (String timeIndex : timeAndPollutant.keySet()) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("monitortime", timeIndex);
                            List<Map<String, Object>> pollutantlist = new ArrayList<>();
                            pollutantList = timeAndPollutant.get(timeIndex);
                            for (Document document : pollutantList) {
                                pollutantCode = document.getString("PollutantCode");
                                if (codeAndName.containsKey(pollutantCode)) {
                                    Map<String, Object> pollutant = new HashMap<>();
                                    pollutant.put("pollutantcode", pollutantCode);
                                    pollutant.put("pollutantname", codeAndName.get(pollutantCode));
                                    pollutant.put("monitorvalue", document.get("AvgStrength"));
                                    if (mnAndTimeAndPollutantData.containsKey(mnCommon)
                                            && mnAndTimeAndPollutantData.get(mnCommon).containsKey(timeIndex)
                                            && mnAndTimeAndPollutantData.get(mnCommon).get(timeIndex).containsKey(pollutantCode)) {
                                        pollutant.putAll((Map<String, Object>) mnAndTimeAndPollutantData.get(mnCommon).get(timeIndex).get(pollutantCode));
                                    } else {
                                        pollutant.put("levelcode", "");
                                        pollutant.put("levelname", "");
                                    }
                                    pollutantlist.add(pollutant);
                                }
                            }
                            if (pollutantlist.size() > 0) {
                                dataMap.put("pollutantlist", pollutantlist);
                                dataList.add(dataMap);
                            }

                        }
                    }
                    //排序
                    Comparator<Object> comparebynum = Comparator.comparing(m -> ((Map) m).get("monitortime").toString());
                    if (comparebynum != null) {
                        dataList = dataList.stream().sorted(comparebynum).collect(Collectors.toList());
                    }
                    resultMap.put("datalist", dataList);
                    resultList.add(resultMap);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Map<String, Object> setMnAndDataList(Map<String, Map<String, Object>> mnAndLevelData, String mnIndex, Document pollutant, List<Map<String, Object>> pointStandardDataList) {
        Map<String, Object> levelPollutantData = new HashMap<>();

        Integer levelCode = getLevelCode(pollutant, mnIndex, pointStandardDataList);
        if (levelCode > 0) {
            String levelName = CommonTypeEnum.SoilEvaluateLevelEnum.getNameByCode(levelCode);
            levelPollutantData.put("levelcode", levelCode);
            levelPollutantData.put("levelname", levelName);

            Map<String, Object> levelMnData = new HashMap<>();

            if (mnAndLevelData.containsKey(mnIndex)) {
                int tempCode = Integer.parseInt(mnAndLevelData.get(mnIndex).get("levelcode").toString());
                levelMnData = mnAndLevelData.get(mnIndex);
                if (tempCode < levelCode) {
                    levelMnData.put("levelcode", levelCode);
                    levelMnData.put("levelname", levelName);
                }
            } else {
                levelMnData.put("levelcode", levelCode);
                levelMnData.put("levelname", levelName);
            }
            mnAndLevelData.put(mnIndex, levelMnData);

        }


        return levelPollutantData;
    }


    /**
     * @author: chengzq
     * @date: 2020/3/27 0027 上午 11:20
     * @Description: 导出土壤监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, response, request]
     * @throws:
     */
    @RequestMapping(value = "exportSoilOnlineDataByParams", method = RequestMethod.POST)
    public void exportSoilOnlineDataByParams(@RequestJson(value = "paramsjson") Object paramsjson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            Object soilOnlineData = getSoilOnlineData(paramsjson);

            JSONObject jsonObject = JSONObject.fromObject(soilOnlineData);
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object resultlist = jsonObject1.get("resultlist");
            Object title = jsonObject1.get("title");
            JSONArray jsonArray = JSONArray.fromObject(resultlist);
            JSONArray jsonArray1 = JSONArray.fromObject(title);


            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("企业名称");
            headers.add("监测点名称");
            headers.add("监测点名称");
            headersField.add("pollutionname");
            headersField.add("monitorpointname");
            headersField.add("monitortime");
            for (Object o : jsonArray1) {
                Map<String, Object> pollutant = (Map<String, Object>) o;
                headers.add(pollutant.get("pollutantname") == null ? "" : pollutant.get("pollutantname").toString());
                headersField.add(pollutant.get("pollutantcode") == null ? "" : pollutant.get("pollutantcode").toString());
            }
            for (Object o : jsonArray) {
                Map<String, Object> map = (Map<String, Object>) o;
                if (map.get("pollutionname") == null || (map.get("pollutionname") != null && map.get("pollutionname").toString().equals("null"))) {
                    map.put("pollutionname", "");
                }
            }
            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("土壤监测数据", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/8/27 0027 下午 2:15
     * @Description: 通过多参数获取土壤在线监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointid, starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getSoilOnlineDataByParams", method = RequestMethod.POST)
    public Object getSoilOnlineDataByParams(@RequestJson(value = "monitorpointids") Object monitorpointids,
                                            @RequestJson(value = "starttime", required = false) String starttime,
                                            @RequestJson(value = "endtime", required = false) String endtime,
                                            @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                            @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                            @RequestJson(value = "pagenum", required = false) Integer pagenum) throws Exception {
        try {
            JSONObject jsonObject = new JSONObject();
            Map<String, Object> resultMap = new HashMap<>();
            jsonObject.put("pollutanttype", soilEnum.getCode());
            List<Map<String, Object>> soilPollutantsByParam = soilPointService.getSoilPollutantsByParam(jsonObject);
            List<Map<String, Object>> soilPoints = soilPointService.getSoilPointsAndPollutantStandardInfo(jsonObject);
            Map<String, String> collect = soilPollutantsByParam.stream().filter(m -> m.get("pollutantname") != null && m.get("pollutantcode") != null)
                    .collect(Collectors.toMap(m -> m.get("pollutantcode").toString(), m -> m.get("pollutantname").toString() + "_" + m.get("pollutantunit")
                            , BinaryOperator.maxBy(String::compareTo)));
            jsonObject.put("mns", monitorpointids);


            //默认查5年
            if (StringUtils.isBlank(starttime) && StringUtils.isBlank(endtime)) {
                Date date = new Date();
                endtime = DataFormatUtil.getDateYMD(date);
                Calendar instance = Calendar.getInstance();
                instance.setTime(date);
                instance.add(Calendar.YEAR, -5);
                Date time = instance.getTime();
                starttime = DataFormatUtil.getDateYMD(time);
            }
            jsonObject.put("starttime", starttime + " 00:00:00");
            jsonObject.put("endtime", endtime + " 23:59:59");
            jsonObject.put("monitortimekey", "DayDataList");
            jsonObject.put("collection", "DayData");
            List<Map<String, Object>> daydata = onlineService.getMonitorDataByParamMap(jsonObject);

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Map<String, Object> daydatum : daydata) {
                String monitorTime = OnlineController.formatCSTString(daydatum.get("MonitorTime").toString(), "yyyy-MM-dd");
                String DataGatherCode = daydatum.get("DataGatherCode") == null ? "" : daydatum.get("DataGatherCode").toString();
                List<Map<String, Object>> DayDataList = (List<Map<String, Object>>) daydatum.get("DayDataList");
                for (Map<String, Object> map : DayDataList) {
                    String PollutantCode = map.get("PollutantCode") == null ? "" : map.get("PollutantCode").toString();

                    //1如果不传污染物可以进入逻辑；2如果传污染物判断和上面污染物代码一直也可以进入
                    if (collect.get(PollutantCode) != null && (StringUtils.isBlank(pollutantcode) || (StringUtils.isNotBlank(pollutantcode) && PollutantCode.equals(pollutantcode)))) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("monitortime", monitorTime);
                        data.put("monitorid", DataGatherCode);
                        String[] split = collect.get(PollutantCode).split("_");
                        data.put("pollutantname", split[0]);
                        if (split.length > 1) {
                            data.put("pollutantunit", split[1]);
                        }
                        String AvgStrength = map.get("AvgStrength") == null ? "" : map.get("AvgStrength").toString();
                        data.put("value", AvgStrength);
                        data.put("pollutantcode", PollutantCode);
                        Map<String, Object> first = soilPoints.stream().filter(m -> m.get("PK_ID") != null && DataGatherCode.equals(m.get("PK_ID").toString())).findFirst().orElse(new HashMap<>());
                        data.put("monitorpointname", first.get("MonitorPointName"));
                        data.put("normalvalue", first.get("NormalValue"));
                        data.put("polluLevel", getPolluLevel(AvgStrength, first));
                        resultList.add(data);
                    }
                }
            }
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                int total = resultList.size();
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                resultMap.put("total", total);
            }
            resultMap.put("resultList", resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList()));
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/8/27 0027 上午 11:43
     * @Description: 获取污染程度
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private String getPolluLevel(String value, Map<String, Object> polluLevel) {
        Float NormalValue = polluLevel.get("NormalValue") == null ? 0f : Float.valueOf(polluLevel.get("NormalValue").toString());
        Float OneLevel = polluLevel.get("OneLevel") == null ? 0f : Float.valueOf(polluLevel.get("OneLevel").toString());
        Float TwoLevel = polluLevel.get("TwoLevel") == null ? 0f : Float.valueOf(polluLevel.get("TwoLevel").toString());
        Float ThreeLevel = polluLevel.get("ThreeLevel") == null ? 0f : Float.valueOf(polluLevel.get("ThreeLevel").toString());
        Float FourLevel = polluLevel.get("FourLevel") == null ? 0f : Float.valueOf(polluLevel.get("FourLevel").toString());
        if (FourLevel == 0) {
            return "";
        }
        Float aFloat = Float.valueOf(value);
        return aFloat <= NormalValue ? "无污染" : aFloat <= OneLevel ? "轻微污染" : aFloat <= TwoLevel ? "轻度污染" : aFloat <= ThreeLevel ? "中度污染" : aFloat <= FourLevel ? "重度污染" : "重度污染";
    }


}
