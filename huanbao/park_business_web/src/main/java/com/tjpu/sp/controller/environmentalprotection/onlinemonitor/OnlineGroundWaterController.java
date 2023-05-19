package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.ExcelUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.model.common.mongodb.GroundWaterDataSelectVO;
import com.tjpu.sp.model.common.mongodb.GroundWaterDataVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GroundWaterVO;
import com.tjpu.sp.service.common.MongoBaseService;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GroundWaterService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.tracesourcesample.TraceSourceSampleService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.FunWaterQaulityClassEnum.getNameByValue;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.GroundWaterEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum;


/**
 * @author: chengzq
 * @date: 2020/11/11 0011 下午 1:58
 * @Description: 地下水在线控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("onlinegroundwater")
public class OnlineGroundWaterController {

    @Autowired
    private TraceSourceSampleService traceSourceSampleService;
    @Autowired
    private MongoBaseService mongoBaseService;
    @Autowired
    private PubCodeService pubCodeService;
    @Autowired
    private GroundWaterService groundWaterService;
    @Autowired
    private OnlineService onlineService;


    @RequestMapping(value = "/getGroundWaterListData", method = RequestMethod.POST)
    public Object getGroundWaterListData(@RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                         @RequestJson(value = "starttime", required = false) String starttime,
                                         @RequestJson(value = "endtime  ", required = false) String endtime,
                                         @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                         @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws Exception {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            paramMap.put("monitorpointname", monitorpointname);
            List<Map<String, Object>> groundWaterInfoByParamMap = groundWaterService.getGroundWaterInfoByParamMap(paramMap);

            String collect = groundWaterInfoByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.joining(","));
            GroundWaterDataSelectVO groundWaterDataVO = new GroundWaterDataSelectVO();
            groundWaterDataVO.setDataGatherCode(collect);
            if (StringUtils.isNotBlank(starttime) && StringUtils.isNotBlank(endtime)) {
                Map<String, Object> timemap = new HashMap<>();
                timemap.put("starttime", starttime);
                timemap.put("endtime", endtime);
                groundWaterDataVO.setMonitorTime(JSONObject.fromObject(timemap).toString());
            }

            MongoSearchEntity mongoSearchEntity = new MongoSearchEntity();
            if (pagesize != null && pagenum != null) {
                mongoSearchEntity.setSize(pagesize);
                mongoSearchEntity.setPage(pagenum);
            }
            mongoSearchEntity.setSortname(Arrays.asList("DataGatherCode"));

            List<GroundWaterDataSelectVO> groundWaterData = mongoBaseService.getListWithPageByParam(groundWaterDataVO, mongoSearchEntity, "WaterDetectData", "yyyy-MM-dd");

            long total = mongoBaseService.getCount(groundWaterDataVO, "WaterDetectData", "yyyy-MM-dd");

            //水质污染物
            List<Map<String, Object>> title = getTitle();

            Map<String, String> pollutantmap = title.stream().filter(m -> m.get("name") != null && m.get("code") != null).collect(Collectors.toMap(m -> m.get("code").toString(), m -> m.get("name").toString()));
            boolean isOver;
            for (GroundWaterDataSelectVO groundWaterDatum : groundWaterData) {
                Map<String, Object> data = new HashMap<>();
                Map<String, Object> groundwater = groundWaterInfoByParamMap.stream().filter(m -> m.get("DGIMN") != null && groundWaterDatum.getDataGatherCode().equals(m.get("DGIMN").toString())).findFirst().orElse(new HashMap<>());
                data.putAll(groundwater);
                List<Map<String, Object>> dataList = groundWaterDatum.getDataList();
                data.put("monitortime", FormatUtils.formatCSTString(groundWaterDatum.getMonitorTime(), "yyyy-MM-dd"));
                data.put("waterqualityclass", groundWaterDatum.getWaterqualityclass());
                List<String> overstr = new ArrayList<>();

                for (Map<String, Object> stringObjectMap : dataList) {
                    data.put(stringObjectMap.get("PollutantCode") == null ? "" : stringObjectMap.get("PollutantCode").toString(), stringObjectMap.get("MonitorValue"));
                    String OverMultiple = stringObjectMap.get("OverMultiple") == null ? "" : stringObjectMap.get("OverMultiple").toString();
                    String PollutantCode = stringObjectMap.get("PollutantCode") == null ? "" : stringObjectMap.get("PollutantCode").toString();
                    isOver = Boolean.parseBoolean(stringObjectMap.get("IsOverStandard") + "");
                    if (pollutantmap.get(PollutantCode) != null && isOver) {
                        if (StringUtils.isNotBlank(OverMultiple)) {
                            overstr.add(pollutantmap.get(PollutantCode) + "(" + OverMultiple + ")");
                        } else {
                            overstr.add(pollutantmap.get(PollutantCode));
                        }
                    }

                }
                data.put("OverMultipleStr", overstr.stream().collect(Collectors.joining("、")));
                data.put("onlineid", groundWaterDatum.getId());
                data.remove("waterqualityclassname");
                resultList.add(data);
            }

            resultMap.put("datalist", resultList);
            resultMap.put("total", total);
            resultMap.put("title", title);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/04/02 0011 下午 3:17
     * @Description: 新增地下水监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [addformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/addGroundWaterData", method = RequestMethod.POST)
    public Object addGroundWaterData(@RequestJson(value = "addformdata") Object addformdata) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(addformdata);
            String dgimn = jsonObject.get("dgimn") == null ? "" : jsonObject.get("dgimn").toString();
            String monitortime = jsonObject.get("monitortime") == null ? "" : jsonObject.get("monitortime").toString();
            //新增污染物数据到mongodb
            Object pollutants = jsonObject.get("pollutants");
            JSONArray jsonArray = JSONArray.fromObject(pollutants);
            GroundWaterDataVO groundWaterDataVO = new GroundWaterDataVO();
            groundWaterDataVO.setDataGatherCode(dgimn);
            groundWaterDataVO.setMonitorTime(DataFormatUtil.getDateYMD(monitortime));
            List<Map<String, Object>> pollutantlist = new ArrayList<>();
            setData(groundWaterDataVO, jsonArray, pollutantlist);
            mongoBaseService.save(groundWaterDataVO);
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/04/02 0011 下午 3:19
     * @Description: 通过id获取溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/getGroundWaterDataByID", method = RequestMethod.POST)
    public Object getGroundWaterDataByID(@RequestJson(value = "onlineid") String onlineid) throws Exception {
        try {
            GroundWaterDataVO groundWaterDataVO = new GroundWaterDataVO();
            groundWaterDataVO.setId(onlineid);

            GroundWaterDataVO groundWaterData = (GroundWaterDataVO) mongoBaseService.getListByParam(groundWaterDataVO, "GroundWaterData", "yyyy-MM-dd").stream().findFirst().orElse(new GroundWaterDataVO());

            GroundWaterVO groundWaterByID = groundWaterService.getGroundWaterByID(groundWaterData.getDataGatherCode());


            Map<String, Object> result = traceSourceSampleService.selectByPrimaryKey(onlineid);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/04/02 0011 下午 3:19
     * @Description: 修改溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [updateformdata, session]
     * @throws:
     */
    @RequestMapping(value = "/updateGroundWaterData", method = RequestMethod.POST)
    public Object updateGroundWaterData(@RequestJson(value = "updateformdata") Object updateformdata) throws
            Exception {
        try {
            JSONObject jsonObject = JSONObject.fromObject(updateformdata);
            String onlineid = jsonObject.get("onlineid") == null ? "" : jsonObject.getString("onlineid");
            String monitortime = jsonObject.get("monitortime") == null ? "" : jsonObject.getString("monitortime");


            //新增/修改污染物数据到mongodb
            Object pollutants = jsonObject.get("pollutants");
            JSONArray jsonArray = JSONArray.fromObject(pollutants);

            GroundWaterDataVO groundWaterDataVO = new GroundWaterDataVO();
            groundWaterDataVO.setId(onlineid);

            List<GroundWaterDataVO> groudwaterdata = mongoBaseService.getListByParam(groundWaterDataVO, "GroundWaterData", null);
            groundWaterDataVO.setMonitorTime(DataFormatUtil.getDateYMD(monitortime));
            List<Map<String, Object>> pollutantlist = new ArrayList<>();

            if (groudwaterdata.size() > 0) {
                GroundWaterDataVO realTimeDatum = groudwaterdata.get(0);
                realTimeDatum.setMonitorTime(DataFormatUtil.getDateYMD(monitortime));
                setData(realTimeDatum, jsonArray, pollutantlist);
                mongoBaseService.update(realTimeDatum);
            } else {
                groundWaterDataVO.setId(null);
                setData(groundWaterDataVO, jsonArray, pollutantlist);
                mongoBaseService.save(groundWaterDataVO);
            }


            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/04/02 0011 下午 3:21
     * @Description: 通过id删除溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "/deleteGroundWaterByID", method = RequestMethod.POST)
    public Object deleteGroundWaterByID(@RequestJson(value = "onlineid") String onlineid) throws Exception {
        try {
            if (StringUtils.isNotBlank(onlineid)) {
                GroundWaterDataVO groundWaterDataVO = new GroundWaterDataVO();
                groundWaterDataVO.setId(onlineid);
                mongoBaseService.delete(groundWaterDataVO);
            }
            return AuthUtil.parseJsonKeyToLower("success", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/04/02 0023 下午 4:40
     * @Description: 导出溯源样品信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson, request, response]
     * @throws:
     */
    @RequestMapping(value = "/ExportGroundWaterByParamMap", method = RequestMethod.POST)
    public void ExportGroundWaterByParamMap(@RequestJson(value = "monitorpointname", required = false) String
                                                    monitorpointname,
                                            @RequestJson(value = "starttime", required = false) String starttime,
                                            @RequestJson(value = "endtime  ", required = false) String endtime,
                                            HttpServletRequest request, HttpServletResponse response) {
        try {
            Object groundWaterListData = getGroundWaterListData(monitorpointname, starttime, endtime, Integer.MAX_VALUE, 1);

            JSONObject jsonObject = JSONObject.fromObject(groundWaterListData);
            Object data = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(data);
            Object datalist = jsonObject1.get("datalist");
            JSONArray jsonArray = JSONArray.fromObject(datalist);


            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("监测点名称");
            headers.add("监测时间");
            headers.add("水质类别");
            headers.add("超标污染物及倍数");
            headersField.add("monitorpointname");
            headersField.add("monitortime");
            headersField.add("waterqualityclass");
            headersField.add("overmultiplestr");


            for (Map<String, Object> stringObjectMap : getTitle()) {
                String name = stringObjectMap.get("name") == null ? "" : stringObjectMap.get("name").toString();
                String code = stringObjectMap.get("code") == null ? "" : stringObjectMap.get("code").toString();
                String pollutantunit = stringObjectMap.get("pollutantunit") == null ? "" : stringObjectMap.get("pollutantunit").toString();
                headers.add(name + (StringUtils.isBlank(pollutantunit) ? "" : "(" + pollutantunit + ")"));
                headersField.add(code);
            }

            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, jsonArray, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("地下水数据管理", response, request, bytesForWorkBook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: chengzq
     * @date: 2021/04/02 0022 下午 4:19
     * @Description: 获取voc污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private List<Map<String, Object>> getTitle() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tablename", "PUB_CODE_PollutantFactor");
        paramMap.put("fields", Arrays.asList("code", "name", "pollutantunit"));
        paramMap.put("wherestring", "PollutantType=" + GroundWaterEnum.getCode() + " and isused=1");
        paramMap.put("orderfield", "OrderIndex");
        return pubCodeService.getPubCodesDataByParam(paramMap);
    }


    /**
     * @Description:
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/7/13 14:08
     */
    private void setData(GroundWaterDataVO groundWaterDataVO, JSONArray
            jsonArray, List<Map<String, Object>> pollutantlist) {
        //获取目标水质类别
        String targetLevel = groundWaterService.getTargetLevelByDgimn(groundWaterDataVO.getDataGatherCode());
        String targetLevelCode = CommonTypeEnum.FunWaterQaulityClassEnum.getCodeByName(targetLevel);
        List<Map<String, Object>> standardList = getWaterQualityStandard();

        Map<String, List<Map<String, Object>>> codeAndStandardMap = new HashMap<>();
        Map<String, Double> codeAndStandValue = new HashMap<>();
        List<Map<String, Object>> standards;
        String code;
        for (Map<String, Object> standard : standardList) {
            if (standard.get("FK_PollutantCode") != null
                    && standard.get("JudgementType") != null &&
                    standard.get("FK_FunWaterQaulityCode") != null) {
                code = standard.get("FK_PollutantCode").toString();
                if (standard.get("FK_FunWaterQaulityCode").equals(targetLevelCode)) {
                    codeAndStandValue.put(code, Double.parseDouble(standard.get("StandardValue").toString()));
                }
                if (codeAndStandardMap.containsKey(code)) {
                    standards = codeAndStandardMap.get(code);
                } else {
                    standards = new ArrayList<>();
                }
                standards.add(standard);
                //排序
                standards = standards.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("FK_FunWaterQaulityCode").toString()))).collect(Collectors.toList());
                codeAndStandardMap.put(code, standards);
            }
        }
        JSONObject dataMap;
        Double value;
        Double standValue;
        String judgementType;
        String levelCode;
        Map<String, Object> judgeMap = new HashMap<>();
        boolean flag;
        String WaterQualityClass;
        String OverMultiple;
        boolean IsOverStandard;
        int max = 0;
        for (int i = 0; i < jsonArray.size(); i++) {
            dataMap = jsonArray.getJSONObject(i);
            if (dataMap.get("PollutantCode") != null && dataMap.get("MonitorValue") != null) {
                code = dataMap.getString("PollutantCode");
                value = Double.parseDouble(dataMap.getString("MonitorValue"));
                IsOverStandard = false;
                WaterQualityClass = null;
                OverMultiple = null;
                if (codeAndStandardMap.containsKey(code)) {//设置评价数据
                    standards = codeAndStandardMap.get(code);
                    for (Map<String, Object> standard : standards) {
                        judgementType = standard.get("JudgementType").toString();
                        levelCode = standard.get("FK_FunWaterQaulityCode").toString();
                        standValue = codeAndStandValue.get(code);
                        judgeMap.put("x", value);
                        flag = DataFormatUtil.convertToBoolean(judgementType, judgeMap);
                        if (flag) {
                            if (StringUtils.isNotBlank(targetLevel)) {//判断超标
                                int levelNum = Integer.parseInt(levelCode);
                                if (levelNum > max) {
                                    max = levelNum;
                                }
                                if (Integer.parseInt(targetLevelCode) < levelNum) {
                                    IsOverStandard = true;
                                    if (standValue != null && standValue > 0) {
                                        OverMultiple = DataFormatUtil.SaveOneAndSubZero((value - standValue) / standValue);
                                    }
                                }
                            }
                            WaterQualityClass = CommonTypeEnum.FunWaterQaulityClassEnum.getVauleByCode(levelCode);
                            break;
                        }
                    }
                }
                dataMap.put("OverMultiple", OverMultiple);
                dataMap.put("WaterQualityClass", WaterQualityClass);
                dataMap.put("IsOverStandard", IsOverStandard);
                pollutantlist.add(dataMap);
            }
        }
        groundWaterDataVO.setWaterqualityclass(CommonTypeEnum.FunWaterQaulityClassEnum.getVauleByCode(max + ""));
        groundWaterDataVO.setDataList(pollutantlist);
    }

    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 1:53
     * @Description: 通过监测时间获取所有地下水站点水质类别占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "getAllGroundWaterProportionByMonitorTime", method = RequestMethod.POST)
    public Object getAllGroundWaterProportionByMonitorTime(@RequestJson(value = "monitortime") String
                                                                   monitortime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");

            int actualMaximum = getActualMaximum(monitortime + "-12");

            List<Map<String, Object>> outputs = groundWaterService.getOnlineGroundWaterInfoByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", monitortime + "-01-01");
            paramMap.put("endtime", monitortime + "-12-" + actualMaximum);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getGroundWaterOnlineDataByParamMap(paramMap);

            for (Map thisMonthwaterStationOnlineDatum : thisMonthwaterStationOnlineData) {
                String MonitorTime = thisMonthwaterStationOnlineDatum.get("MonitorTime").toString();
                thisMonthwaterStationOnlineDatum.put("MonitorTime", DataFormatUtil.formatCST(MonitorTime).substring(0, 7));
            }

            Map<String, List<Map>> collect = thisMonthwaterStationOnlineData.stream().filter(m -> m.get("WaterQualityClass") != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));

            for (String MonitorTime : collect.keySet()) {

                Map<String, Object> data = new HashMap<>();
                data.put("MonitorTime", MonitorTime);
                List<Map> list = collect.get(MonitorTime);

                Map<String, Long> waterLevel1 = list.stream().collect(Collectors.groupingBy(m -> m.get("WaterQualityClass").toString(), Collectors.counting()));

                Optional<Long> reduce = waterLevel1.values().stream().reduce(Long::sum);

                if (reduce.isPresent()) {
                    Float aLong = reduce.get().floatValue();
                    for (String o : waterLevel1.keySet()) {
                        data.put(getNameByValue(o), decimalFormat.format(waterLevel1.get(o) / aLong * 100));
                    }
                }
                resultList.add(data);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @author: chengzq
     * @date: 2020/2/14 0014 下午 1:24
     * @Description: 通过监测时间获取地下水站点每个月水质i类别信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "getEveryMonthGroundWaterInfoByMonitorTime", method = RequestMethod.POST)
    public Object getEveryMonthGroundWaterInfoByMonitorTime(@RequestJson(value = "monitortime") String
                                                                    monitortime) throws ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            int actualMaximum = getActualMaximum(monitortime);


            List<Map<String, Object>> outputs = groundWaterService.getOnlineGroundWaterInfoByParamMap(paramMap);

            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", monitortime + "-01");
            paramMap.put("endtime", monitortime + "-" + actualMaximum);
            List<Map> thisMonthwaterStationOnlineData = onlineService.getGroundWaterOnlineDataByParamMap(paramMap);


            Map<String, List<Map>> collect = thisMonthwaterStationOnlineData.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> {
                try {
                    return DataFormatUtil.formatCST(m.get("MonitorTime").toString()).substring(0, 10);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return "";
            }));

            for (String MonitorTime : collect.keySet()) {

                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> datalist = new ArrayList<>();
                List<Map> list = collect.get(MonitorTime);
                list.stream().peek(m -> {
                    try {
                        Map<String, Object> hourdata = new HashMap<>();
                        hourdata.put("MonitorTime", DataFormatUtil.formatCST(m.get("MonitorTime").toString()).substring(0, 10));
                        hourdata.put("WaterLevel", getNameByValue(m.get("WaterQualityClass") == null ? "" : m.get("WaterQualityClass").toString()));
                        String dgimn = m.get("DataGatherCode") == null ? "" : m.get("DataGatherCode").toString();
                        outputs.stream().filter(n -> n.get("dgimn") != null && n.get("dgimn").toString().equals(dgimn)).forEach(n -> hourdata.put("monitorpointname", n.get("monitorpointname")));
                        datalist.add(hourdata);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }).sorted(Comparator.comparing(m -> m.get("MonitorTime").toString())).collect(Collectors.toList());

                data.put("Monitortime", MonitorTime);
                data.put("monitorpoint", datalist);
                resultList.add(data);
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/2/13 0013 下午 1:56
     * @Description: 通过监测时间统计站点水质达标率同比占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "countCountySectionEvaluateDataByTime", method = RequestMethod.POST)
    public Object countCountySectionEvaluateDataByTime(@RequestJson(value = "monitortime") String monitortime) throws
            ParseException {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");

            int actualMaximum = getActualMaximum(monitortime);
            List<Map<String, Object>> outputs = groundWaterService.getOnlineGroundWaterInfoByParamMap(paramMap);
            //获取所有mn号
            List<String> dgimns = outputs.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", monitortime + "-01");
            paramMap.put("endtime", monitortime + "-" + actualMaximum);
            List<Map> thisMonthwaterStationOnlineDatap = onlineService.getGroundWaterOnlineDataByParamMap(paramMap);

            List<Map<String, Object>> thismonthmaps = standardReachingRate(outputs, thisMonthwaterStationOnlineDatap);


            String afterMonthStirng = getAfterMonthStirng(monitortime);
            int afterMonthActualMaximum = getActualMaximum(afterMonthStirng);
            paramMap.put("starttime", afterMonthStirng + "-01");
            paramMap.put("endtime", afterMonthStirng + "-" + afterMonthActualMaximum);

            List<Map> AfterMonthwaterStationOnlineData = onlineService.getGroundWaterOnlineDataByParamMap(paramMap);
            List<Map<String, Object>> aftermonthmaps = standardReachingRate(outputs, AfterMonthwaterStationOnlineData);


            String afterYearStirng = getAfterYearStirng(monitortime);
            int afterYearActualMaximum = getActualMaximum(afterYearStirng);
            paramMap.put("starttime", afterYearStirng + "-01");
            paramMap.put("endtime", afterYearStirng + "-" + afterYearActualMaximum);

            List<Map> afterYearwaterStationOnlineData = onlineService.getGroundWaterOnlineDataByParamMap(paramMap);
            List<Map<String, Object>> afteryearmaps = standardReachingRate(outputs, afterYearwaterStationOnlineData);

            for (Map<String, Object> output : outputs) {
                Map<String, Object> data = new HashMap<>();
                String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                String monitorpointname = output.get("monitorpointname") == null ? "" : output.get("monitorpointname").toString();
                thismonthmaps.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).peek(m -> data.put("thismonthrate", m.get("standardreachingrate"))).collect(Collectors.toList());
                aftermonthmaps.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).peek(m -> data.put("aftermonthrate", m.get("standardreachingrate"))).collect(Collectors.toList());
                afteryearmaps.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).peek(m -> data.put("afteryearrate", m.get("standardreachingrate"))).collect(Collectors.toList());

                //设置同比占比信息
                if (data.get("thismonthrate") == null) {
                    data.put("thismonthrate", "-");
                    //设置数据设置-
                    if (data.get("aftermonthrate") == null) {
                        data.put("aftermonthrate", "-");

                        data.put("yearonyear", "-");
                    } else {
                        data.put("yearonyear", "-");
                    }
                    //设置数据为-
                    if (data.get("afteryearrate") == null) {
                        data.put("afteryearrate", "-");
                        data.put("Proportion", "-");
                    } else {
                        data.put("Proportion", "-");
                    }
                } else {
                    Float thismonthrate = Float.valueOf(data.get("thismonthrate").toString());

                    if (data.get("aftermonthrate") == null) {
                        data.put("aftermonthrate", "-");
                        data.put("yearonyear", "-");
                    } else {
                        Float aftermonthrate = Float.valueOf(data.get("aftermonthrate").toString());
                        String yearonyear = decimalFormat.format((aftermonthrate - thismonthrate) / aftermonthrate * 100);
                        data.put("yearonyear", yearonyear);
                    }


                    if (data.get("afteryearrate") == null) {
                        data.put("afteryearrate", "-");
                        data.put("Proportion", "-");
                    } else {
                        Float afteryearrate = Float.valueOf(data.get("afteryearrate").toString());
                        String Proportion = decimalFormat.format((afteryearrate - thismonthrate) / afteryearrate * 100);
                        data.put("Proportion", Proportion);
                    }

                }

                data.put("dgimn", dgimn);
                data.put("thismonthtime", monitortime);
                data.put("aftermonthtime", afterMonthStirng);
                data.put("afteryeartime", afterYearStirng);
                data.put("monitorpointname", monitorpointname);
                resultList.add(data);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //计算各站点达标率
    public static List<Map<String, Object>> standardReachingRate
    (List<Map<String, Object>> outputs, List<Map> waterStationOnlineDataByParamMap) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, List<Map>> collect = waterStationOnlineDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
        for (String DataGatherCode : collect.keySet()) {
            List<Map> maps = collect.get(DataGatherCode);
            Map<String, Object> data = new HashMap<>();
            Float dabiao = 0f;
            for (Map map : maps) {
                String WaterQualityClass = map.get("WaterQualityClass") == null ? "" : map.get("WaterQualityClass").toString();
                String WaterLevel = getNameByValue(WaterQualityClass);
                for (Map<String, Object> output : outputs) {

                    String dgimn = output.get("dgimn") == null ? "" : output.get("dgimn").toString();
                    String FK_FunWaterQaulityCode = output.get("FK_FunWaterQaulityCode") == null ? "" : output.get("FK_FunWaterQaulityCode").toString();
                    //如果数据等级小于目标等级为达标数据
                    if (dgimn.equals(DataGatherCode) && CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) != -1 && CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(WaterLevel) <= CommonTypeEnum.FunWaterQaulityCodeEnum.getCodeByName(FK_FunWaterQaulityCode)) {
                        dabiao++;
                    }

                }
            }
            data.put("dgimn", DataGatherCode);
            data.put("standardreachingrate", decimalFormat.format(dabiao / maps.size() * 100));
            resultList.add(data);
        }
        return resultList;
    }


    //获取当前月份最大天数
    private int getActualMaximum(String monitortime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date parse = simpleDateFormat.parse(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        return instance.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    //获取上个月字符串
    private String getAfterMonthStirng(String monitortime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date parse = simpleDateFormat.parse(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.MONTH, -1);
        return simpleDateFormat.format(instance.getTime());
    }

    //获取去年字符串
    private String getAfterYearStirng(String monitortime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date parse = simpleDateFormat.parse(monitortime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.YEAR, -1);
        return simpleDateFormat.format(instance.getTime());
    }

    /**
     * @author: chengzq
     * @date: 2020/10/22 0022 下午 4:19
     * @Description: 获取水质标准信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    private List<Map<String, Object>> getWaterQualityStandard() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tablename", "T_BAS_WaterQualityStandard");
        paramMap.put("fields", Arrays.asList("FK_FunWaterQaulityCode", "FK_PollutantCode", "StandardValue", "JudgementType"));
        paramMap.put("wherestring", "WaterType=2");
        return pubCodeService.getPubCodesDataByParam(paramMap);
    }

}
