package com.tjpu.sp.controller.environmentalprotection.upgradefunction;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.model.common.ReturnInfo;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.*;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.upgradefunction.WallChartOperationService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindDirectionEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.WeatherPollutionEnum.WindSpeedEnum;
import static java.math.BigDecimal.ROUND_HALF_DOWN;

@RestController
@RequestMapping("wallChartOperation")
public class WallChartOperationController {

    @Autowired
    private DeviceStatusService deviceStatusService;
    @Autowired
    private WallChartOperationService wallChartOperationService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;
    @Autowired
    private WaterOutPutInfoService waterOutPutInfoService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;

    @Autowired
    private AirMonitorStationService airMonitorStationService;

    @Autowired
    private WaterStationService waterStationService;
    //污水处理厂类型  3 生活污水排放口 6 工艺废气排放口
    private String sh_output = "3";
    private String gy_output = "6";

    /**
     * @author: xsm
     * @date: 2022/02/14 15:40
     * @Description: 获取挂图作战所有监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getAllWallChartOperationMonitorTypes", method = RequestMethod.POST)
    public Object getAllWallChartOperationMonitorTypes() {
        try {
            //获取挂图作战所有监测类型 将主要名称相同的类型合并
            Map<String, Object> param = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("userid", userId);
            List<Map<String, Object>> listdata = deviceStatusService.getAllWallChartOperationMonitorTypes(param);
            return AuthUtil.parseJsonKeyToLower("success", listdata);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 08:33
     * @Description: 通过自定义参数获取所有点位单个污染物每小时的总排放量
     * @updateUser:xsm
     * @updateDate:2022/05/10
     * @updateDescription:新增datatype：hour/day 查小时排放量或日排放量
     * @param:starttime yyyy-dd-mm HH  endtime yyyy-dd-mm HH
     * @return:
     */
    @RequestMapping(value = "countEnvPointFlowDataByParam", method = RequestMethod.POST)
    public Object countEnvPointFlowDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "pollutantcode") String pollutantcode,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "dgimn", required = false) String dgimn,
                                               @RequestJson(value = "datatype", required = false) String datatype
    ) {

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (datatype == null) {
                datatype = "hour";
            }
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("monitorpointtypes", monitorpointtypes);
            if (dgimn != null) {//查单个点时  不根据数据权限筛选
                parammap.put("dgimn", dgimn);
            } else {
                parammap.put("userid", userid);
                //查多个 去掉污水处理厂
                parammap.put("wsoutputs", Arrays.asList(sh_output, gy_output));
            }
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointMnDataByParam(parammap);
            List<String> dgimns = dgimnlist.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            parammap.clear();
            parammap.put("dgimns", dgimns);
            //统一取到小时
            if ("year".equals(datatype)) {
                parammap.put("starttime", starttime + "-01-01 00");
                parammap.put("endtime", endtime + "-12-31 23");
            } else if ("month".equals(datatype)) {
                parammap.put("starttime", starttime + "-01 00");
                parammap.put("endtime", DataFormatUtil.getYearMothLast(endtime) + " 23");
            } else if ("day".equals(datatype)) {
                parammap.put("starttime", starttime + " 00");
                parammap.put("endtime", endtime + " 23");
            } else {
                parammap.put("starttime", starttime);
                parammap.put("endtime", endtime);
            }
            parammap.put("datatype", datatype);
            parammap.put("pollutantcode", pollutantcode);
            result = wallChartOperationService.countOnePollutantDischargeRankByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的浓度值及浓度排名情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime datatype:数据类型(1：实时/2：分钟/3：小时) ,monitortime:监测时间（为空则获取点位最新一条数据）sortfield:排序字段 （1：状态，2：站点名称，3：浓度值）
     * @return:
     */
    @RequestMapping(value = "countEnvPointPollutantConcentrationRankByParam", method = RequestMethod.POST)
    public Object countEnvPointPollutantConcentrationRankByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                                 @RequestJson(value = "monitortime", required = false) String monitortime,
                                                                 @RequestJson(value = "pointname", required = false) String pointname,
                                                                 @RequestJson(value = "categorys", required = false) List<Integer> categorys,
                                                                 @RequestJson(value = "inputoroutputs", required = false) List<Integer> inputoroutputs,
                                                                 @RequestJson(value = "outputpropertys", required = false) List<Integer> outputpropertys,
                                                                 @RequestJson(value = "pollutantcode") String pollutantcode,
                                                                 @RequestJson(value = "statuslist", required = false) List<Integer> statuslist,
                                                                 @RequestJson(value = "sortdata", required = false) Object sortdata,
                                                                 @RequestJson(value = "datatype") Integer datatype,
                                                                 @RequestJson(value = "isflow", required = false) String isflow
    ) {

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> parammap = new HashMap<>();
            String othersort = "";
            String sortorder = "";
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorpointname") != null || jsonObject.get("orderstatus") != null) {
                    parammap.put("sortdata", sortdata);
                } else {
                    if (jsonObject.get("value") != null) {
                        othersort = "value";
                        sortorder = jsonObject.get("value").toString();
                    }
                    if (jsonObject.get("monitortime") != null) {
                        othersort = "monitortime";
                        sortorder = jsonObject.get("monitortime").toString();
                    }
                }
            } else {//默认按监测值排
                othersort = "value";
                sortorder = "desc";
            }
            parammap.put("monitorpointtypes", monitorpointtypes);
            parammap.put("outputpropertys", outputpropertys);
            parammap.put("pointname", pointname);
            parammap.put("inputoroutputs", inputoroutputs);
            parammap.put("categorys", categorys);
            parammap.put("userid", userid);
            parammap.put("statuslist", statuslist);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointInfoDataByParam(parammap);
            List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
            Map<String, Map<String, Object>> mnandpointinfo = new HashMap<>();//mn 和 点位信息
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : dgimnlist) {
                if (map.get("dgimn") != null && dgimns.contains(map.get("dgimn").toString())) {
                    mns.add(map.get("dgimn").toString());
                    mnandpointinfo.put(map.get("dgimn").toString(), map);
                }
            }
            parammap.put("othersort", othersort);
            parammap.put("sortorder", sortorder);
            parammap.put("dgimns", mns);
            if (StringUtils.isNotBlank(isflow)) {
                parammap.put("isflow", isflow);
            }
            parammap.put("datatype", datatype);
            parammap.put("pollutantcode", pollutantcode);
            parammap.put("mnandpointinfo", mnandpointinfo);
            if (monitortime != null) {
                parammap.put("monitortime", monitortime);
                result = wallChartOperationService.countEnvPointPollutantConcentrationRankByParam(parammap);
            } else {
                result = wallChartOperationService.countEnvPointPollutantLastDataRankByParam(parammap);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2022/02/15 0015 下午 18:01
     * @Description: 自定义查询条件统计环保点位小时风向图表数据（风速，风向，频次）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "/countAllEnvPointWindChartDataByParams", method = RequestMethod.POST)
    public Object countAllEnvPointWindChartDataByParams(
            @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "monitorpointcategorys", required = false) List<Integer> monitorpointcategorys,
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime) {

        try {
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            //获取关联信息
            List<String> mns = new ArrayList<>();
            paramMap.put("userid", userid);
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("monitorpointcategorys", monitorpointcategorys);
            List<Map<String, Object>> listdata = otherMonitorPointService.getAllOnlineOtherPointInfoByParamMap(paramMap);
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                        mns.add(map.get("dgimn").toString());
                    }
                }
            }
            if (mns.size() > 0) {
                paramMap.put("sort", "asc");
                paramMap.put("mns", mns);
                String collection = "";
                if (datatype.equals("hour")) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + ":00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + ":59:59";
                        paramMap.put("endtime", endtime);
                    }
                    collection = MongoDataUtils.getCollectionByDataMark(3);
                } else if (datatype.equals("day")) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + " 00:00:00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + " 23:59:59";
                        paramMap.put("endtime", endtime);
                    }
                    collection = MongoDataUtils.getCollectionByDataMark(4);
                } else if (datatype.equals("minute")) {
                    if (StringUtils.isNotBlank(starttime)) {
                        starttime = starttime + ":00";
                        paramMap.put("starttime", starttime);
                    }
                    if (StringUtils.isNotBlank(endtime)) {
                        endtime = endtime + ":59";
                        paramMap.put("endtime", endtime);
                    }
                    collection = MongoDataUtils.getCollectionByDataMark(2);
                }
                List<String> pollutantcodes = Arrays.asList(WindDirectionEnum.getCode()
                        , WindSpeedEnum.getCode());
                paramMap.put("collection", collection);
                paramMap.put("pollutantcodes", pollutantcodes);
                List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
                dataList = wallChartOperationService.getAllPointWindChartData(documents, collection);
            }
            return AuthUtil.parseJsonKeyToLower("success", dataList);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/16 0016 上午 9:57
     * @Description: 通过自定义条件获取园区恶臭小时在线监测数据(支持其他监测点)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitorpointtypes, datetype, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getParkOnlinePollutantHourDataByParams", method = RequestMethod.POST)
    public Object getParkOnlinePollutantHourDataByParams(
            @RequestJson(value = "datatype") String datatype,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> listdata = otherMonitorPointService.getAllOnlineOtherPointInfoByParamMap(paramMap);
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                        mns.add(map.get("dgimn").toString());
                    }
                }
            }
            paramMap.put("mns", mns);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("datatype", datatype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            List<Map<String, Object>> result = wallChartOperationService.getParkOnlinePollutantHourDataByParams(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/16 10:44
     * @Description: 根据监测类型获取该类型监测点（Voc）某时段该污染物小时累计浓度数据(多点位趋势)
     * @param:
     * @return:starttime、endtime yyyy-mm-dd HH
     */
    @RequestMapping(value = "getOtherPointHourMonitorDataByParam", method = RequestMethod.POST)
    public Object getOtherPointHourMonitorDataByParam(@RequestJson(value = "datatype") String datatype,
                                                      @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("monitorpointtype", monitorpointtype);
            List<Map<String, Object>> listdata = otherMonitorPointService.getAllOnlineOtherPointInfoByParamMap(paramMap);
            Map<String, Object> mnandname = new HashMap<>();
            if (listdata != null && listdata.size() > 0) {
                for (Map<String, Object> map : listdata) {
                    if (map.get("dgimn") != null && !"".equals(map.get("dgimn").toString())) {
                        mns.add(map.get("dgimn").toString());
                        mnandname.put(map.get("dgimn").toString(), map.get("monitorpointname"));
                    }
                }
            }

            paramMap.put("isuser", "yes");
            List<Map<String, Object>> allpollutants = pollutantService.getPollutantsByPollutantType(paramMap);
            List<String> codes = new ArrayList<>();
            if (allpollutants != null) {
                for (Map<String, Object> pomap : allpollutants) {
                    if (pomap.get("code") != null) {
                        codes.add(pomap.get("code").toString());
                    }
                }
            }
            paramMap.put("mnandname", mnandname);
            paramMap.put("mns", mns);
            paramMap.put("codes", codes);
            paramMap.put("datatype", datatype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            Map<String, Object> map = wallChartOperationService.getOtherPointHourMonitorDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", map);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个种类所有污染物的小时累计浓度排名和环比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "countVocHourMonitorDataRankByParam", method = RequestMethod.POST)
    public Object countVocHourMonitorDataRankByParam(@RequestJson(value = "monitortime") String monitortime,
                                                     @RequestJson(value = "categorys", required = false) List<Integer> categorys,
                                                     @RequestJson(value = "pollutantcategory") Integer pollutantcategory,
                                                     @RequestJson(value = "pollutantcategoryname") String pollutantcategoryname,
                                                     @RequestJson(value = "statuslist", required = false) List<Integer> statuslist,
                                                     @RequestJson(value = "monitorpointname", required = false) Object monitorpointname,
                                                     @RequestJson(value = "sortdata", required = false) Object sortdata,
                                                     @RequestJson(value = "datatype", required = false) String datatype
    ) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (datatype == null) {
                datatype = "hour";
            }
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("pollutantcategory", pollutantcategory);
            List<Map<String, Object>> pollutants = otherMonitorPointService.getVocPollutantDataByFactorGroups(parammap);
            List<String> codes = new ArrayList<>();
            if (pollutants != null && pollutants.size() > 0) {
                for (Map<String, Object> map : pollutants) {
                    if (map.get("pollutantcode") != null) {
                        codes.add(map.get("pollutantcode").toString());
                    }
                }
            }
            parammap.clear();
            String othersort = "";
            String sortorder = "";
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorpointname") != null || jsonObject.get("orderstatus") != null) {
                    parammap.put("sortdata", sortdata);
                } else {
                    if (jsonObject.get("value") != null) {
                        othersort = "value";
                        sortorder = jsonObject.get("value").toString();
                    }
                    if (jsonObject.get("previousvalue") != null) {
                        othersort = "previousvalue";
                        sortorder = jsonObject.get("previousvalue").toString();
                    }
                }
            } else {//默认按监测值排
                othersort = "value";
                sortorder = "desc";
            }
            parammap.put("categorys", categorys);
            parammap.put("monitorpointtype", CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode());
            parammap.put("monitorpointname", monitorpointname);
            parammap.put("userid", userid);
            parammap.put("statuslist", statuslist);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointInfoDataByParam(parammap);
            Map<String, Map<String, Object>> mnandpointinfo = new HashMap<>();//mn 和 点位信息
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : dgimnlist) {
                if (map.get("dgimn") != null) {
                    mns.add(map.get("dgimn").toString());
                    mnandpointinfo.put(map.get("dgimn").toString(), map);
                }
            }

            String startTime = "";
            if ("hour".equals(datatype)) {
                //环比小时
                startTime = DataFormatUtil.getBeforeByHourTime(1, monitortime);
            } else if ("day".equals(datatype)) {
                //环比日
                //获取前一天的数据
                startTime = DataFormatUtil.getBeforeByDayTime(1, monitortime);
            }

            parammap.put("starttime", startTime);
            parammap.put("othersort", othersort);
            parammap.put("sortorder", sortorder);
            parammap.put("datatype", datatype);
            parammap.put("dgimns", mns);
            parammap.put("pollutantcodes", codes);
            parammap.put("mnandpointinfo", mnandpointinfo);
            parammap.put("pollutantcategoryname", pollutantcategoryname);
            parammap.put("endtime", monitortime);
            result = wallChartOperationService.countVocHourMpnitorDataRankByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 08:57
     * @Description: 通过自定义参数获取某类型所有监测点某个污染物某日的所有报警时刻
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "getAllPointPollutanAlarmTimeDataByParam", method = RequestMethod.POST)
    public Object getAllPointPollutanAlarmTimeDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime,
                                                          @RequestJson(value = "datatype") String datatype,
                                                          @RequestJson(value = "pollutantcode") String pollutantcode,
                                                          @RequestJson(value = "intervalnum") Integer intervalnum
    ) {
        try {
            List<String> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("monitorpointtypes", monitorpointtypes);
            parammap.put("userid", userid);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointMnDataByParam(parammap);
            List<String> dgimns = dgimnlist.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            parammap.put("dgimns", dgimns);
            parammap.put("starttime", starttime);
            parammap.put("endtime", endtime);
            parammap.put("datatype", datatype);
            parammap.put("pollutantcode", pollutantcode);
            parammap.put("intervalnum", intervalnum);
            result = wallChartOperationService.getAllPointPollutanAlarmTimeDataByParam(parammap);
            parammap.clear();
            parammap.put("alarmtimelist", result);
            return AuthUtil.parseJsonKeyToLower("success", parammap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 11:51
     * @Description: 通过监测类型和污染物获取污染物排放标准
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "getPollutanDischargeStandardDataByParam", method = RequestMethod.POST)
    public Object getPollutanDischargeStandardDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                          @RequestJson(value = "pollutantcode") String pollutantcode
    ) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("monitorpointtypes", monitorpointtypes);
            parammap.put("pollutantcode", pollutantcode);
            parammap.put("pollutantcategory", 2);
            result = wallChartOperationService.getPollutanDischargeStandardDataByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/17 13:29
     * @Description: 通过自定义参数获取所有企业单个污染物某小时的总排放量(废气 、 废水 、 烟气)
     * @updateUser:xsm
     * @updateDate:2022/05/10 17:52
     * @updateDescription:新增日排放量查询 查询字段datatype：hour/day
     * @param:monitortime yyyy-dd-mm HH
     * @return:
     */
    @RequestMapping(value = "countEnvPollutantHourFlowDataByParam", method = RequestMethod.POST)
    public Object countEnvPollutantHourFlowDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                       @RequestJson(value = "monitortime") String monitortime,
                                                       @RequestJson(value = "datatype", required = false) String datatype,
                                                       @RequestJson(value = "pollutantcode") String pollutantcode
    ) {

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (datatype == null) {
                datatype = "hour";
            }
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("monitorpointtypes", monitorpointtypes);
            parammap.put("userid", userid);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointInfoDataByParam(parammap);
            //Map<String,Map<String,Object>> mnandpointinfo = new HashMap<>();//mn 和 点位信息
            Map<String, Object> mnandid = new HashMap<>();
            List<String> mns = new ArrayList<>();
            List<String> pollutionids = new ArrayList<>();
            for (Map<String, Object> map : dgimnlist) {
                if (map.get("dgimn") != null) {
                    mns.add(map.get("dgimn").toString());
                    //mnandpointinfo.put(map.get("dgimn").toString(),map);
                    if (map.get("pollutionid") != null) {
                        mnandid.put(map.get("dgimn").toString(), map.get("pollutionid"));
                        if (!pollutionids.contains(map.get("pollutionid").toString())) {
                            pollutionids.add(map.get("pollutionid").toString());
                        }
                    }
                }
            }
            parammap.clear();
            parammap.put("dgimns", mns);
            parammap.put("monitortime", monitortime);
            parammap.put("mnandid", mnandid);
            //parammap.put("mnandpointinfo",mnandpointinfo);
            parammap.put("datatype", datatype);
            parammap.put("pollutantcode", pollutantcode);
            parammap.put("pollutionids", pollutionids);
            result = wallChartOperationService.countEnvPollutantHourFlowDataByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: xsm
     * @date: 2022/02/17 0017 下午 16:11
     * @Description: 获取一段时间内园区内和园区外污染物浓度趋势对比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime yyyy-mm-dd HH, endtime yyyy-mm-dd HH]
     * @throws:
     */
    @RequestMapping(value = "getParkInAndOutsideAirPollutantDataByParam", method = RequestMethod.POST)
    public Object getParkInAndOutsideAirPollutantDataByParam(@RequestJson(value = "pollutantcode") String pollutantcode,
                                                             @RequestJson(value = "datatype", required = false) String datatype,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> param = new HashMap<>();
            param.put("starttime", starttime);
            param.put("endtime", endtime);
            param.put("pollutantcode", pollutantcode);
            if (datatype == null || "".equals(datatype)) {
                datatype = "hour";
            }
            param.put("datatype", datatype);
            result = wallChartOperationService.getParkInAndOutsideAirPollutantDataByParam(param);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/2/18 0018 上午 09:04
     * @Description: 统计水质整体达标率情况（小时分组）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime]
     * @throws:
     */
    @RequestMapping(value = "getWaterQualityComplianceDataByParam", method = RequestMethod.POST)
    public Object getWaterQualityComplianceDataByParam(@RequestJson(value = "datatype") String datatype,
                                                       @RequestJson(value = "starttime") String starttime,
                                                       @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            List<Map<String, Object>> outputs = wallChartOperationService.getWaterQualityStationByParamMap(paramMap);
            //获取所有水质级别等级
            List<Map<String, Object>> qaulitylist = wallChartOperationService.getAllWaterQualityLevelData();
            Map<String, Integer> codeandlevel = new HashMap<>();
            Map<String, Integer> mnandlevel = new HashMap<>();
            List<String> dgimns = new ArrayList<>();
            for (Map<String, Object> map : qaulitylist) {
                if (map.get("code") != null && map.get("levelnum") != null) {
                    codeandlevel.put(map.get("code").toString(), Integer.valueOf(map.get("levelnum").toString()));
                }
            }
            for (Map<String, Object> map : outputs) {
                if (map.get("dgimn") != null) {
                    dgimns.add(map.get("dgimn").toString());
                    if (map.get("levelnum") != null) {
                        mnandlevel.put(map.get("dgimn").toString(), Integer.valueOf(map.get("levelnum").toString()));
                    }
                }
            }
            paramMap.put("codeandlevel", codeandlevel);
            paramMap.put("mnandlevel", mnandlevel);
            paramMap.put("dgimns", dgimns);
            paramMap.put("datatype", datatype);

            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            resultList = wallChartOperationService.getWaterQualityComplianceDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时浓度排名（空气）
     * @updateUser:xsm
     * @updateDate:2022/04/15
     * @updateDescription: 新增日浓度排名
     * @param:monitortime 根据数据类型 传时间到小时或日
     * @return:
     */
    @RequestMapping(value = "countAirHourMonitorDataRankByParam", method = RequestMethod.POST)
    public Object countAirHourMonitorDataRankByParam(@RequestJson(value = "monitortime") String monitortime,
                                                     @RequestJson(value = "pollutantcode") String pollutantcode,
                                                     @RequestJson(value = "pointtypes", required = false) List<Integer> pointtypes,
                                                     @RequestJson(value = "controllevels", required = false) List<String> controllevels,
                                                     @RequestJson(value = "datatype", required = false) String datatype,
                                                     @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                     @RequestJson(value = "fkcontrollevelcodes", required = false) List<Integer> fkcontrollevelcodes,
                                                     @RequestJson(value = "statuslist", required = false) List<Integer> statuslist,
                                                     @RequestJson(value = "sortdata", required = false) Object sortdata
    ) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> parammap = new HashMap<>();
            String othersort = "";
            String sortorder = "";
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorpointname") != null || jsonObject.get("orderstatus") != null) {
                    parammap.put("sortdata", sortdata);
                } else {
                    if (jsonObject.get("value") != null) {
                        othersort = "value";
                        sortorder = jsonObject.get("value").toString();
                    }
                }
            } else {//默认按监测值排
                othersort = "value";
                sortorder = "desc";
            }
            if (datatype == null) {
                datatype = "hour";
            }
            parammap.put("fkcontrollevelcodes", fkcontrollevelcodes);
            parammap.put("pointtypes", pointtypes);
            parammap.put("monitorpointname", monitorpointname);
            parammap.put("controllevels", controllevels);
            parammap.put("userid", userid);
            parammap.put("statuslist", statuslist);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvAirPointInfoDataByParam(parammap);
            Map<String, Map<String, Object>> mnandpointinfo = new HashMap<>();//mn 和 点位信息
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : dgimnlist) {
                if (map.get("dgimn") != null) {
                    mns.add(map.get("dgimn").toString());
                    mnandpointinfo.put(map.get("dgimn").toString(), map);
                }
            }
            parammap.put("othersort", othersort);
            parammap.put("sortorder", sortorder);
            parammap.put("dgimns", mns);
            parammap.put("pollutantcode", pollutantcode);
            parammap.put("mnandpointinfo", mnandpointinfo);
            parammap.put("monitortime", monitortime);
            parammap.put("datatype", datatype);
            result = wallChartOperationService.countAirHourOrDayMonitorDataRankByParam(parammap);

            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/15 10:17
     * @Description: 通过自定义参数获取所有点位单个污染物的小时浓度排名和环比情况(水质)
     * @updateUser:xsm
     * @updateDate:2022/05/11 8:37
     * @updateDescription:新增日数据查询
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "countWaterQualityHourMpnitorDataRankByParam", method = RequestMethod.POST)
    public Object countWaterQualityHourMpnitorDataRankByParam(@RequestJson(value = "monitortime") String monitortime,
                                                              @RequestJson(value = "pollutantcode") String pollutantcode,
                                                              @RequestJson(value = "fkcontrollevelcodes", required = false) List<Integer> fkcontrollevelcodes,
                                                              @RequestJson(value = "statuslist", required = false) List<Integer> statuslist,
                                                              @RequestJson(value = "monitorpointname", required = false) Object monitorpointname,
                                                              @RequestJson(value = "sortdata", required = false) Object sortdata,
                                                              @RequestJson(value = "datatype", required = false) String datatype
    ) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> parammap = new HashMap<>();
            String othersort = "";
            String sortorder = "";
            if (datatype == null) {
                datatype = "hour";
            }
            if (sortdata != null) {
                JSONObject jsonObject = JSONObject.fromObject(sortdata);
                if (jsonObject.get("monitorpointname") != null || jsonObject.get("orderstatus") != null) {
                    parammap.put("sortdata", sortdata);
                } else {
                    if (jsonObject.get("value") != null) {
                        othersort = "value";
                        sortorder = jsonObject.get("value").toString();
                    }
                }
            } else {//默认按监测值排
                othersort = "value";
                sortorder = "desc";
            }
            //获取所有水质级别等级
            List<Map<String, Object>> qaulitylist = wallChartOperationService.getAllWaterQualityLevelData();
            Map<String, Object> codeandlevel = new HashMap<>();
            for (Map<String, Object> map : qaulitylist) {
                if (map.get("code") != null && map.get("levelnum") != null) {
                    codeandlevel.put(map.get("code").toString(), map.get("name"));
                }
            }
            //parammap.put("monitorpointtype",);
            parammap.put("fkcontrollevelcodes", fkcontrollevelcodes);
            parammap.put("userid", userid);
            parammap.put("monitorpointname", monitorpointname);
            parammap.put("statuslist", statuslist);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvWaterQualityPointInfoDataByParam(parammap);
            Map<String, Map<String, Object>> mnandpointinfo = new HashMap<>();//mn 和 点位信息
            List<String> mns = new ArrayList<>();
            for (Map<String, Object> map : dgimnlist) {
                if (map.get("dgimn") != null) {
                    mns.add(map.get("dgimn").toString());
                    mnandpointinfo.put(map.get("dgimn").toString(), map);
                }
            }
            parammap.put("codeandlevel", codeandlevel);
            parammap.put("othersort", othersort);
            parammap.put("sortorder", sortorder);
            parammap.put("dgimns", mns);
            parammap.put("datatype", datatype);
            parammap.put("pollutantcode", pollutantcode);
            parammap.put("mnandpointinfo", mnandpointinfo);
            parammap.put("monitortime", monitortime);
            result = wallChartOperationService.countWaterQualityHourMpnitorDataRankByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 10:38
     * @Description: 根据监测类型和时间段获取某个点的突增污染物(浓度突变)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime yyyy-mm-dd,endtime yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "getOnePointChangeWarnPollutantData", method = RequestMethod.POST)
    public Object getOnePointChangeWarnPollutantData(@RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                     @RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitorpointtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> result = wallChartOperationService.getOnePointChangeWarnPollutantData(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 10:53
     * @Description: 获取单个点单污染物的浓度突增情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-mm-dd   endtime yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "getOnePointPollutantChangeWarnDataByParams", method = RequestMethod.POST)
    public Object getOnePointPollutantChangeWarnDataByParams(@RequestJson(value = "dgimn") String dgimn,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "pollutantcode") String pollutantcode) {
        try {
            Map<String, Object> result = wallChartOperationService.getOnePointPollutantChangeWarnDataByParams(starttime, endtime, dgimn, pollutantcode);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 11:08
     * @Description: 获取单点位单污染物某时间段的浓度突增详情数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime yyyy-mm-dd  endtime yyyy-mm-dd
     * @return:
     */
    @RequestMapping(value = "getOnePointPollutantChangeWarnDetailParams", method = RequestMethod.POST)
    public Object getOnePointPollutantChangeWarnDetailParams(@RequestJson(value = "dgimn", required = false) String dgimn,
                                                             @RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                             @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                             @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                             @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitortype", monitorpointtype);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("mn", dgimn);
            List<Map<String, Object>> abruptChangeInfoByParam = wallChartOperationService.getOnePointPollutantChangeWarnDetailParams(paramMap);
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum != null && pagesize != null) {
                List<Map<String, Object>> collect = abruptChangeInfoByParam.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("monitortime").toString()).reversed()).skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
                resultMap.put("datalist", collect);
            } else {
                resultMap.put("datalist", abruptChangeInfoByParam);
            }
            resultMap.put("total", abruptChangeInfoByParam.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/24 0024 上午 11:28
     * @Description: 通过多参数获取单个点位某时间段的报警时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countOneMonitorPointAlarmTimesDataByParam", method = RequestMethod.POST)
    public Object countOneMonitorPointAlarmTimesDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                            @RequestJson(value = "timetype") String timetype,
                                                            @RequestJson(value = "pollutantcode", required = false) String pollutantcode,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("dgimn", dgimn);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("timetype", timetype);
            if (pollutantcode != null) {
                paramMap.put("pollutantcode", pollutantcode);
            }
            result = wallChartOperationService.getPointAlarmTimesDataGroupByTime(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/28 0028 下午 1:56
     * @Description: 统计单个点位某时段内污染物报警时长占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countPollutantAlarmTimeProportionDataByParam", method = RequestMethod.POST)
    public Object countPollutantAlarmTimeProportionDataByParam(@RequestJson(value = "dgimn", required = false) String dgimn,
                                                               @RequestJson(value = "timetype") String timetype,
                                                               @RequestJson(value = "monitorpointtype", required = false) Integer monitorpointtype,
                                                               @RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes,
                                                               @RequestJson(value = "starttime") String starttime,
                                                               @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> dgimns = new ArrayList<>();
            if (StringUtils.isBlank(dgimn)) {
                if (monitorpointtype != null) {
                    dgimns = getDgimns(monitorpointtype);
                } else if (monitorpointtypes != null) {
                    for (Integer type : monitorpointtypes) {
                        dgimns.addAll(getDgimns(type));
                    }
                }
            } else {
                dgimns = Arrays.asList(dgimn);
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("timetype", timetype);
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            List<Map<String, Object>> result = wallChartOperationService.countPollutantAlarmTimeProportionDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> getDgimns(Integer monitorpointtype) {

        List<Integer> monitortypes = Arrays.asList(
                WasteWaterEnum.getCode(),
                SmokeEnum.getCode(),
                WasteGasEnum.getCode(),
                RainEnum.getCode(),
                FactoryBoundarySmallStationEnum.getCode(),
                FactoryBoundaryStinkEnum.getCode()
        );
        //获取所点位名称和MN号
        List<Map<String, Object>> monitorPoints = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("monitortype", monitorpointtype);
        if (monitortypes.contains(monitorpointtype)) {
            monitorPoints.addAll(gasOutPutInfoService.getOutPutAndPollutionInfoByParams(paramMap));
        } else if (monitorpointtype == AirEnum.getCode()) {//大气
            monitorPoints.addAll(airMonitorStationService.getAllAirMonitorStationByParams(paramMap));
        } else if (monitorpointtype == WaterQualityEnum.getCode()) {//水质
            monitorPoints.addAll(waterStationService.getWaterStationByParamMap(paramMap));
        } else if (monitorpointtype == EnvironmentalVocEnum.getCode()
                || monitorpointtype == EnvironmentalStinkEnum.getCode()
                || monitorpointtype == EnvironmentalDustEnum.getCode()
                || monitorpointtype == MicroStationEnum.getCode()) {//voc//恶臭
            monitorPoints.addAll(otherMonitorPointService.getAllMonitorInfoByParams(paramMap));
        }
        String mnCommon;
        List<String> dgimns = new ArrayList<>();
        for (Map<String, Object> map : monitorPoints) {
            if (map.get("dgimn") != null) {
                mnCommon = map.get("dgimn").toString();
                dgimns.add(mnCommon);
            }
        }
        return dgimns;
    }

    /**
     * @author: xsm
     * @date: 2022/02/28 0028 下午 1:56
     * @Description: 获取单个点位最新实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOnePointLastRealTimeDataByParam", method = RequestMethod.POST)
    public Object getOnePointLastRealTimeDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                     @RequestJson(value = "pollutantcategory", required = false) String pollutantcategory,
                                                     @RequestJson(value = "monitorpointid") String monitorpointid,
                                                     @RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Set<Map<String, Object>> pollutantSetData = new HashSet<>();
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandorderindex = new HashMap<>();
            List<String> pollutantcodes = new ArrayList<>();
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                //VOC 根据所传的因子类别  展示因子浓度
                paramMap.put("pollutantcategory", pollutantcategory);
                List<Map<String, Object>> pollutants = otherMonitorPointService.getVocPollutantDataByFactorGroups(paramMap);
                for (Map<String, Object> map : pollutants) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                    codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    codeandorderindex.put(map.get("pollutantcode").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
                }
            } else {
                paramMap.put("outputid", monitorpointid);
                paramMap.put("outputids", Arrays.asList(monitorpointid));
                paramMap.put("pollutanttype", monitorpointtype);
                pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
                for (Map<String, Object> map : pollutantSetData) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                    codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    codeandorderindex.put(map.get("pollutantcode").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
                }
            }
            if (pollutantcodes.size() > 0) {
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("dgimn", dgimn);
                List<Document> documents = wallChartOperationService.getOnePointLatestRealTimeDataByParamMap(paramMap);
                List<Map<String, Object>> valuelist = new ArrayList<>();
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    resultMap.put("dgimn", dgimn);
                    resultMap.put("monitortime", DataFormatUtil.getDateYMDHMS(document.getDate("MonitorTime")));
                    List<Document> polist = (List<Document>) document.get("DataList");
                    String code;
                    String value;
                    String isOver;
                    String isException;
                    boolean isOverStandard;
                    for (Document doc : polist) {
                        Map<String, Object> codemap = new HashMap<>();
                        code = doc.getString("PollutantCode");
                        if (pollutantcodes.contains(code)) {
                            value = doc.getString("AvgStrength");
                            isOver = doc.get("IsOver") != null ? doc.get("IsOver").toString() : "-1";
                            isException = doc.get("IsException") != null ? doc.get("IsException").toString() : "0";
                            isOverStandard = doc.get("IsOverStandard") != null ? doc.getBoolean("IsOverStandard") : false;
                            if (isOverStandard) {
                                isOver = "4";
                            }
                            codemap.put("pollutantcode", code);
                            codemap.put("pollutantname", codeandname.get(code));
                            codemap.put("value", value);
                            codemap.put("orderindex", codeandorderindex.get(code));
                            codemap.put("isover", isOver);
                            codemap.put("isexception", isException);
                            valuelist.add(codemap);
                        }
                    }
                }
                if (valuelist != null && valuelist.size() > 0) {
                    //排序
                    valuelist = valuelist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                }
                resultMap.put("valuedata", valuelist);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/02/28 0028 下午 1:56
     * @Description: 统计某类型报警点位某日各报警污染物报警时长
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/countAllPollutantAlarmTimeByParamMap", method = RequestMethod.POST)
    public Object countAllPollutantAlarmTimeByParamMap(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                       @RequestJson(value = "daytime") String daytime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("userid", userId);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointInfoDataByParam(paramMap);
            Map<String, Map<String, Object>> mnandpointinfo = new HashMap<>();//mn 和 点位信息
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnandtype = new HashMap<>();
            for (Map<String, Object> map : dgimnlist) {
                if (map.get("dgimn") != null) {
                    mns.add(map.get("dgimn").toString());
                    mnandpointinfo.put(map.get("dgimn").toString(), map);
                    mnandtype.put(map.get("dgimn").toString(), map.get("fkmonitorpointtypecode"));
                }
            }
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> codeandname = getPollutantCodeAndNameByTypes(monitorpointtypes);
            String startTime = daytime + " 00:00:00";
            String endTime = daytime + " 23:59:59";
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            paramMap.put("dgimns", mns);
            paramMap.put("mnandtype", mnandtype);
            paramMap.put("mnandpointinfo", mnandpointinfo);
            paramMap.put("codeandname", codeandname);
            resultList = wallChartOperationService.countAllPollutantAlarmTimeByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 根据监测类型获取污染物
     */
    private Map<String, Object> getPollutantCodeAndNameByTypes(List<Integer> pollutanttypes) {
        Map<String, Object> codeAndName = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pollutanttypes", pollutanttypes);

        List<Map<String, Object>> dataList = pollutantService.getPollutantsByCodesAndType(paramMap);
        for (Map<String, Object> dataMap : dataList) {
            codeAndName.put(dataMap.get("code").toString() + "_" + dataMap.get("pollutanttype").toString(), dataMap.get("name"));
        }
        return codeAndName;
    }

    /**
     * @author: xsm
     * @date: 2022/04/18 10:42
     * @Description: 通过自定义参数获取空气所有监测点某个污染物某时间段内所有报警日期（日数据报警）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "getAirAllPointPollutanAlarmDayDataByParam", method = RequestMethod.POST)
    public Object getAirAllPointPollutanAlarmDayDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                            @RequestJson(value = "starttime") String starttime,
                                                            @RequestJson(value = "endtime") String endtime,
                                                            @RequestJson(value = "pollutantcode") String pollutantcode

    ) {
        try {
            List<String> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("monitorpointtypes", monitorpointtypes);
            parammap.put("userid", userid);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointMnDataByParam(parammap);
            List<String> dgimns = dgimnlist.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            parammap.put("dgimns", dgimns);
            parammap.put("starttime", starttime);
            parammap.put("endtime", endtime);
            parammap.put("pollutantcode", pollutantcode);
            result = wallChartOperationService.getAirAllPointPollutanAlarmDayDataByParam(parammap);
            parammap.clear();
            parammap.put("alarmtimelist", result);
            return AuthUtil.parseJsonKeyToLower("success", parammap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/07 0007 上午 9:13
     * @Description: 根据监测类型获取按类型分组的点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "/getAllPointInfoGroupByMonitorTypeByParamMap", method = RequestMethod.POST)
    public Object getAllPointInfoGroupByMonitorTypeByParamMap(@RequestJson(value = "monitorpointtypes", required = false) List<Integer> monitorpointtypes
    ) throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("userid", userId);
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointInfoDataByParam(paramMap);
            //按类型名分组
            Map<String, List<Map<String, Object>>> collect = new HashMap<>();
            if (dgimnlist != null && dgimnlist.size() > 0) {
                collect = dgimnlist.stream().filter(m -> m.get("mainname") != null).collect(Collectors.groupingBy(m -> m.get("mainname").toString()));
            }
            for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                Map<String, Object> onemap = new HashMap<>();
                onemap.put("mainname", entry.getKey());
                onemap.put("child", entry.getValue());
                result.add(onemap);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2022/05/17 13:55
     * @Description: 通过自定义参数获取所有污水处理厂点位单个污染物每小时的总排放量
     * @updateUser:xsm
     * @updateDate:2022/05/10
     * @updateDescription:新增datatype：hour/day 查小时排放量或日排放量
     * @param:starttime yyyy-dd-mm HH  endtime yyyy-dd-mm HH inputoroutput:1进水口 2出水口
     * @return:
     */
    @RequestMapping(value = "countSewageTreatmentPlantFlowDataByParam", method = RequestMethod.POST)
    public Object countSewageTreatmentPlantFlowDataByParam(@RequestJson(value = "monitortime") String monitortime,
                                                           @RequestJson(value = "pollutantcode") String pollutantcode,
                                                           @RequestJson(value = "dgimn") String dgimn,
                                                           @RequestJson(value = "inorout") Integer inorout
    ) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", inorout);
            List<String> inMns = waterOutPutInfoService.getInOrOutPutMnListByParam(paramMap);
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("pollutantcode", pollutantcode);
            paramMap.put("datatype", "hour");
            paramMap.put("dgimns", inMns);
            result = wallChartOperationService.countOnePollutantDischargeRankByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/17 16:39
     * @Description: 获取废水排口污染物排污企业排名（非污水处理厂）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEntPollutantDischargeRankByParam", method = RequestMethod.POST)
    public Object countEntPollutantDischargeRankByParam(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                        @RequestJson(value = "pollutantcode") String pollutantCode,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "datatype") String datatype) {

        try {
            if (monitorpointtypes.size() > 0 && (monitorpointtypes.contains(WasteWaterEnum.getCode()) || monitorpointtypes.contains(WasteGasEnum.getCode()) ||
                    monitorpointtypes.contains(SmokeEnum.getCode()))) {
                //统一取到小时
                if ("year".equals(datatype)) {
                    starttime = starttime + "-01-01 00:00:00";
                    endtime = endtime + "-12-31 23:59:59";
                } else if ("month".equals(datatype)) {
                    starttime = starttime + "-01 00:00:00";
                    endtime = DataFormatUtil.getYearMothLast(endtime) + " 23:59:59";
                } else if ("day".equals(datatype)) {
                    starttime = starttime + " 00:00:00";
                    endtime = endtime + " 23:59:59";
                } else if ("hour".equals(datatype)) {//小时
                    starttime = starttime + ":00:00";
                    endtime = endtime + ":59:59";
                }
                Date start = DataFormatUtil.parseDate(starttime);
                Date end = DataFormatUtil.parseDate(endtime);
                List<Map<String, Object>> resultList = wallChartOperationService.countEntPollutantDischargeRankByParam(pollutantCode, datatype, start, end, monitorpointtypes);
                double count = 0;
                if (resultList.size() > 0) {
                    count = resultList.stream().map(m -> Double.parseDouble(m.get("flow").toString())).reduce(Double::sum).get();
                }
                if (count != 0) {
                    //占比
                    for (Map<String, Object> map : resultList) {
                        map.put("rate", BigDecimal.valueOf(Double.parseDouble(map.get("flow").toString()) / count * 100).setScale(2, ROUND_HALF_DOWN).toString());
                    }
                } else {
                    resultList.forEach(map -> map.put("rate", 0));
                }
                List<Map<String, Object>> resultListInfo = new ArrayList<>();
                for (Map<String, Object> map : resultList) {
                    if (resultListInfo.size() == 10) {
                        break;
                    }
                    resultListInfo.add(map);
                }
                return AuthUtil.parseJsonKeyToLower("success", resultListInfo);
            } else {
                return AuthUtil.parseJsonKeyToLower("success", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @Description: 获取污水处理厂进水口，出水口下拉数据
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/5/19 9:56
     */
    @RequestMapping(value = "getPWOutPutSelectData", method = RequestMethod.POST)
    public Object getPWOutPutSelectData() throws Exception {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            //出水口
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 1);
            paramMap.put("inoroutname", "出水口");
            result.addAll(waterOutPutInfoService.getPWOutPutSelectData(paramMap));
            //进水口
            paramMap.put("outputproperty", 6);
            paramMap.put("inorout", 2);
            paramMap.put("inoroutname", "进水口");
            result.addAll(waterOutPutInfoService.getPWOutPutSelectData(paramMap));
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/05/19 14:58
     * @Description: 通过自定义参数获取非污水厂排口的总排放量情况
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:starttime
     * @return:
     */
    @RequestMapping(value = "countGeneralWasteOutPutTotalFlowDataByParam", method = RequestMethod.POST)
    public Object countGeneralWasteOutPutTotalFlowDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                              @RequestJson(value = "starttime") String starttime,
                                                              @RequestJson(value = "endtime") String endtime,
                                                              @RequestJson(value = "datatype", required = false) String datatype
    ) {

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            if (datatype == null) {
                datatype = "hour";
            }
            Map<String, Object> parammap = new HashMap<>();
            parammap.put("monitorpointtypes", monitorpointtypes);
            parammap.put("userid", userid);
            //查多个 去掉污水处理厂
            parammap.put("wsoutputs", Arrays.asList(sh_output, gy_output));
            List<Map<String, Object>> dgimnlist = deviceStatusService.getEnvPointMnDataByParam(parammap);
            List<String> dgimns = dgimnlist.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).distinct().collect(Collectors.toList());
            parammap.clear();
            parammap.put("dgimns", dgimns);
            //统一取到小时
            if ("year".equals(datatype)) {
                parammap.put("starttime", starttime + "-01-01 00:00:00");
                parammap.put("endtime", endtime + "-12-31 23:59:59");
            } else if ("month".equals(datatype)) {
                parammap.put("starttime", starttime + "-01 00:00:00");
                parammap.put("endtime", DataFormatUtil.getYearMothLast(endtime) + " 23:59:59");
            } else if ("day".equals(datatype)) {
                parammap.put("starttime", starttime + " 00:00:00");
                parammap.put("endtime", endtime + " 23:59:59");
            } else {
                parammap.put("starttime", starttime + ":00:00");
                parammap.put("endtime", endtime + ":59:59");
            }
            parammap.put("datatype", datatype);
            result = wallChartOperationService.countGeneralWasteOutPutTotalFlowDataByParam(parammap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 上午 11:39
     * @Description: 获取单个点位小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOnePointHourDataByParam", method = RequestMethod.POST)
    public Object getOnePointHourDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                             @RequestJson(value = "monitortime") String monitortime,
                                             @RequestJson(value = "pollutantcategory", required = false) String pollutantcategory,
                                             @RequestJson(value = "monitorpointid") String monitorpointid,
                                             @RequestJson(value = "monitorpointtype") Integer monitorpointtype) {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Set<Map<String, Object>> pollutantSetData = new HashSet<>();
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandorderindex = new HashMap<>();
            List<String> pollutantcodes = new ArrayList<>();
            if (monitorpointtype == CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalVocEnum.getCode()) {
                //VOC 根据所传的因子类别  展示因子浓度
                paramMap.put("pollutantcategory", pollutantcategory);
                List<Map<String, Object>> pollutants = otherMonitorPointService.getVocPollutantDataByFactorGroups(paramMap);
                for (Map<String, Object> map : pollutants) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                    codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    codeandorderindex.put(map.get("pollutantcode").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
                }
            } else {
                paramMap.put("outputid", monitorpointid);
                paramMap.put("outputids", Arrays.asList(monitorpointid));
                paramMap.put("pollutanttype", monitorpointtype);
                pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
                for (Map<String, Object> map : pollutantSetData) {
                    pollutantcodes.add(map.get("pollutantcode").toString());
                    codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                    codeandorderindex.put(map.get("pollutantcode").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
                }
            }
            if (pollutantcodes.size() > 0) {
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("dgimn", dgimn);
                paramMap.put("monitortime", monitortime);
                List<Document> documents = wallChartOperationService.getOnePointHourDataByParam(paramMap);
                List<Map<String, Object>> valuelist = new ArrayList<>();
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    resultMap.put("dgimn", dgimn);
                    resultMap.put("monitortime", monitortime);
                    List<Document> polist = (List<Document>) document.get("HourDataList");
                    String code;
                    String value;
                    String isOver;
                    String isException;
                    boolean isOverStandard;
                    for (Document doc : polist) {
                        Map<String, Object> codemap = new HashMap<>();
                        code = doc.getString("PollutantCode");
                        if (pollutantcodes.contains(code)) {
                            value = doc.getString("AvgStrength");
                            isOver = doc.get("IsOver") != null ? doc.get("IsOver").toString() : "-1";
                            isException = doc.get("IsException") != null ? doc.get("IsException").toString() : "0";
                            isOverStandard = doc.get("IsOverStandard") != null ? doc.getBoolean("IsOverStandard") : false;
                            if (isOverStandard) {
                                isOver = "4";
                            }
                            codemap.put("pollutantcode", code);
                            codemap.put("pollutantname", codeandname.get(code));
                            codemap.put("value", value);
                            codemap.put("orderindex", codeandorderindex.get(code));
                            codemap.put("isover", isOver);
                            codemap.put("isexception", isException);
                            valuelist.add(codemap);
                        }
                    }
                }
                if (valuelist != null && valuelist.size() > 0) {
                    //排序
                    valuelist = valuelist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                }
                resultMap.put("valuedata", valuelist);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 上午 11:48
     * @Description: 根据监测时间和污染物获取单个空气点位小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOneAirPointHourDataByParam", method = RequestMethod.POST)
    public Object getOneAirPointHourDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                @RequestJson(value = "monitortime") String monitortime,
                                                @RequestJson(value = "monitorpointid") String monitorpointid,
                                                @RequestJson(value = "isaqi", required = false) Boolean isaqi
    ) {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Set<Map<String, Object>> pollutantSetData = new HashSet<>();
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandorderindex = new HashMap<>();
            List<String> pollutantcodes = new ArrayList<>();
            paramMap.put("outputid", monitorpointid);
            paramMap.put("outputids", Arrays.asList(monitorpointid));
            paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.AirEnum.getCode());
            pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
            for (Map<String, Object> map : pollutantSetData) {
                pollutantcodes.add(map.get("pollutantcode").toString());
                codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                codeandorderindex.put(map.get("pollutantcode").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
            }
            if (pollutantcodes.size() > 0) {
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("dgimn", dgimn);
                paramMap.put("monitortime", monitortime);
                List<Document> documents = wallChartOperationService.getOneAirPointHourDataByParam(paramMap);
                List<Map<String, Object>> valuelist = new ArrayList<>();
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    resultMap.put("dgimn", dgimn);
                    resultMap.put("monitortime", monitortime);
                    List<Document> polist = (List<Document>) document.get("DataList");
                    if (isaqi != null && isaqi == true) {
                        //获取六参数code
                        List<String> sixcodes = CommonTypeEnum.AirCommonSixIndexEnum.getSixPollutantCodes();
                        //若查的AQI  则返回所有污染物的 分指数值 IAQI
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("pollutantcode", "aqi");
                        onemap.put("pollutantname", "AQI");
                        onemap.put("value", document.get("AQI"));
                        onemap.put("airlevel", document.get("AirLevel"));
                        onemap.put("orderindex", 0);
                        valuelist.add(onemap);
                        String code;
                        Object value;
                        for (Document doc : polist) {
                            Map<String, Object> codemap = new HashMap<>();
                            code = doc.getString("PollutantCode");
                            if (sixcodes.contains(code)) {
                                value = doc.get("IAQI");
                                codemap.put("pollutantcode", code);
                                codemap.put("pollutantname", codeandname.get(code));
                                codemap.put("value", value);
                                codemap.put("airlevel", doc.get("AirLevel"));
                                codemap.put("orderindex", codeandorderindex.get(code));
                                valuelist.add(codemap);
                            }
                        }
                    } else {
                        String code;
                        String value;
                        for (Document doc : polist) {
                            Map<String, Object> codemap = new HashMap<>();
                            code = doc.getString("PollutantCode");
                            if (pollutantcodes.contains(code)) {
                                value = doc.getString("Strength");
                                codemap.put("aqi", document.get("AQI"));
                                codemap.put("pollutantcode", code);
                                codemap.put("pollutantname", codeandname.get(code));
                                codemap.put("airlevel", doc.get("AirLevel"));
                                codemap.put("iaqi", doc.get("IAQI"));
                                codemap.put("value", value);
                                codemap.put("orderindex", codeandorderindex.get(code));
                                valuelist.add(codemap);
                            }
                        }
                    }
                }
                if (valuelist != null && valuelist.size() > 0) {
                    //排序
                    valuelist = valuelist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                }
                resultMap.put("valuedata", valuelist);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/08 0008 下午 13:31
     * @Description: 根据监测时间和污染物获取单个水质点位小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOneWaterQualityStationHourDataByParam", method = RequestMethod.POST)
    public Object getOneWaterQualityStationHourDataByParam(@RequestJson(value = "dgimn") String dgimn,
                                                           @RequestJson(value = "monitortime") String monitortime,
                                                           @RequestJson(value = "monitorpointid") String monitorpointid,
                                                           @RequestJson(value = "iswaterquality", required = false) Boolean iswaterquality
    ) {
        try {
            //根据监测点id获取设置的污染物信息
            Map<String, Object> resultMap = new LinkedHashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Set<Map<String, Object>> pollutantSetData = new HashSet<>();
            Map<String, Object> codeandname = new HashMap<>();
            Map<String, Object> codeandorderindex = new HashMap<>();
            List<String> pollutantcodes = new ArrayList<>();
            paramMap.put("outputid", monitorpointid);
            paramMap.put("outputids", Arrays.asList(monitorpointid));
            paramMap.put("pollutanttype", CommonTypeEnum.MonitorPointTypeEnum.WaterQualityEnum.getCode());
            pollutantSetData = onlineService.getPollutantSetDataByParamMap(paramMap);
            for (Map<String, Object> map : pollutantSetData) {
                pollutantcodes.add(map.get("pollutantcode").toString());
                codeandname.put(map.get("pollutantcode").toString(), map.get("pollutantname"));
                codeandorderindex.put(map.get("pollutantcode").toString(), map.get("orderindex") != null ? map.get("orderindex").toString() : "999");
            }
            if (pollutantcodes.size() > 0) {
                //获取所有水质级别等级
                List<Map<String, Object>> qaulitylist = wallChartOperationService.getAllWaterQualityLevelData();
                Map<String, Object> codeandlevel = new HashMap<>();
                for (Map<String, Object> map : qaulitylist) {
                    if (map.get("code") != null && map.get("levelnum") != null) {
                        codeandlevel.put(map.get("code").toString(), map.get("name"));
                    }
                }
                //根据污染物集合和mn号获取最新一条监测数据
                paramMap.clear();
                paramMap.put("dgimn", dgimn);
                paramMap.put("monitortime", monitortime);
                List<Document> documents = wallChartOperationService.getOneWaterQualityStationHourDataByParam(paramMap);
                List<Map<String, Object>> valuelist = new ArrayList<>();
                if (documents.size() > 0) {
                    Document document = documents.get(0);
                    resultMap.put("dgimn", dgimn);
                    resultMap.put("monitortime", monitortime);
                    List<Document> polist = (List<Document>) document.get("HourDataList");
                    if (iswaterquality != null && iswaterquality == true) {
                        //若查的AQI  则返回所有污染物的 分指数值 IAQI
                        Map<String, Object> onemap = new HashMap<>();
                        onemap.put("pollutantcode", "waterquality");
                        onemap.put("pollutantname", "点位水质评价");
                        onemap.put("levelcode", document.get("WaterLevel"));
                        if (document.get("WaterLevel") != null) {
                            onemap.put("value", codeandlevel.get(document.get("WaterLevel").toString()) != null ? codeandlevel.get(document.get("WaterLevel").toString()).toString() : "-");
                        } else {
                            onemap.put("value", "-");
                        }
                        onemap.put("orderindex", 0);
                        valuelist.add(onemap);
                        String code;
                        for (Document doc : polist) {
                            Map<String, Object> codemap = new HashMap<>();
                            code = doc.getString("PollutantCode");
                            if (pollutantcodes.contains(code)) {
                                codemap.put("pollutantcode", code);
                                codemap.put("pollutantname", codeandname.get(code));
                                codemap.put("levelcode", doc.get("WaterLevel"));
                                if (doc.get("WaterLevel") != null) {
                                    codemap.put("value", codeandlevel.get(doc.get("WaterLevel").toString()) != null ? codeandlevel.get(doc.get("WaterLevel").toString()).toString() : "-");
                                } else {
                                    codemap.put("value", "-");
                                }
                                codemap.put("orderindex", codeandorderindex.get(code));
                                valuelist.add(codemap);
                            }
                        }
                    } else {
                        String code;
                        String value;
                        String isOver;
                        String isException;
                        boolean isOverStandard;
                        for (Document doc : polist) {
                            Map<String, Object> codemap = new HashMap<>();
                            code = doc.getString("PollutantCode");
                            if (pollutantcodes.contains(code)) {
                                value = doc.getString("AvgStrength");
                                isOver = doc.get("IsOver") != null ? doc.get("IsOver").toString() : "-1";
                                isException = doc.get("IsException") != null ? doc.get("IsException").toString() : "0";
                                isOverStandard = doc.get("IsOverStandard") != null ? doc.getBoolean("IsOverStandard") : false;
                                if (isOverStandard) {
                                    isOver = "4";
                                }
                                codemap.put("pointwaterlevel", document.get("WaterLevel"));
                                codemap.put("pollutantcode", code);
                                codemap.put("pollutantname", codeandname.get(code));
                                codemap.put("value", value);
                                codemap.put("orderindex", codeandorderindex.get(code));
                                codemap.put("isover", isOver);
                                codemap.put("isexception", isException);
                                valuelist.add(codemap);
                            }
                        }
                    }
                }
                if (valuelist != null && valuelist.size() > 0) {
                    //排序
                    valuelist = valuelist.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("orderindex").toString()))).collect(Collectors.toList());
                }
                resultMap.put("valuedata", valuelist);
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/10 0010 上午 08:39
     * @Description: 获取设备连通情况（运维管理）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getDeviceConnectivityData", method = RequestMethod.POST)
    public Object getDeviceConnectivityData() {
        try {
            List<Map<String, Object>> resultlist = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //添加数据权限
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("userid", userid);
            paramMap.put("monitoringclass", "yes");
            List<Map<String, Object>> result = wallChartOperationService.getDeviceConnectivityData(paramMap);
            if (result != null && result.size() > 0) {
                //按类型名分组
                Map<String, List<Map<String, Object>>> collect = new HashMap<>();
                collect = result.stream().filter(m -> m.get("monitorpointtypename") != null).collect(Collectors.groupingBy(m -> m.get("monitorpointtypename").toString()));

                List<Map<String, Object>> childlist;
                for (Map.Entry<String, List<Map<String, Object>>> entry : collect.entrySet()) {
                    childlist = entry.getValue();
                    Map<String, Object> onemap = new HashMap<>();
                    onemap.put("monitorpointtypename", entry.getKey());
                    Double total = 0d;
                    Double online = 0d;
                    List<String> codes = new ArrayList<>();
                    for (Map<String, Object> map : childlist) {
                        if (map.get("monitorpointtypecode") != null) {
                            codes.add(map.get("monitorpointtypecode").toString());
                        }
                        if (map.get("totalnum") != null && !"".equals(map.get("totalnum").toString()) && Integer.valueOf(map.get("totalnum").toString()) > 0) {
                            if (onemap.get("totalnum") != null) {
                                onemap.put("totalnum", Integer.valueOf(onemap.get("totalnum").toString()) + Integer.valueOf(map.get("totalnum").toString()));
                            } else {
                                onemap.put("totalnum", map.get("totalnum"));
                            }
                            total += Double.valueOf(map.get("totalnum").toString());
                        }
                        if (map.get("onlinenum") != null && !"".equals(map.get("onlinenum").toString()) && Integer.valueOf(map.get("onlinenum").toString()) > 0) {
                            online = Double.valueOf(map.get("onlinenum").toString());
                            if (onemap.get("onlinenum") != null) {
                                onemap.put("onlinenum", Integer.valueOf(onemap.get("onlinenum").toString()) + Integer.valueOf(map.get("onlinenum").toString()));
                            } else {
                                onemap.put("onlinenum", map.get("onlinenum"));
                            }
                        }
                    }
                    onemap.put("monitorpointtypecodes", codes);
                    if (total > 0) {
                        onemap.put("connectivity", DataFormatUtil.SaveOneAndSubZero(100 * online / total) + "%");
                    } else {
                        onemap.put("connectivity", "0%");
                    }
                    resultlist.add(onemap);
                }
            }
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/10 0010 上午 09:08
     * @Description: 根据监测类型和时间获取排口异常动态情况（运维管理）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOutPutExceptionWorkOrderData", method = RequestMethod.POST)
    public Object getOutPutExceptionWorkOrderData(@RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            //添加数据权限
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            paramMap.put("datauserid", userid);
            paramMap.put("hasauthor", "1");
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("fktasktype", CommonTypeEnum.TaskTypeEnum.DevOpsTaskEnum.getCode());
            paramMap.put("monitorpointtypes", monitorpointtypes);
            result = wallChartOperationService.getOutPutExceptionWorkOrderData(paramMap);
            return AuthUtil.parseJsonKeyToLower(ReturnInfo.success, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/12/24 0024 上午 11:13
     * @Description: 通过多参数获取多个点位某时间段的报警总时长统计数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: starttime:yyyy-mm-dd endtime:yyyy-mm-dd  monitorpointtype:监测点类型
     * @throws:
     */
    @RequestMapping(value = "/countEntPointOverAlarmTimesDataByParam", method = RequestMethod.POST)
    public Object countEntPointOverAlarmTimesDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                         @RequestJson(value = "pollutionid") String pollutionid,
                                                         @RequestJson(value = "monitortime") String monitortime
    ) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            List<Integer> pollutiontypes = CommonTypeEnum.getPollutionMonitorPointTypeList();
            Map<String, Object> param = new HashMap<>();
            param.put("monitorpointtypes", pollutiontypes);
            param.put("userid", userId);
            param.put("pollutionid", pollutionid);
            List<Map<String, Object>> dataList = new ArrayList<>();
            //企业下监测点信息
            dataList = pollutionService.getEntPointInfoByEntIDAndTypes(param);
            String mnCommon;
            List<String> dgimns = new ArrayList<>();
            Map<String, Object> mnandname = new HashMap<>();
            Map<String, Object> mnandtype = new HashMap<>();
            Map<String, Object> mnandid = new HashMap<>();
            for (Map<String, Object> map : dataList) {
                if (map.get("DGIMN") != null) {
                    mnCommon = map.get("DGIMN").toString();
                    mnandname.put(mnCommon, map.get("outputname"));
                    mnandtype.put(map.get("DGIMN").toString(), map.get("type"));
                    mnandid.put(map.get("DGIMN").toString(), map.get("outputid"));
                    dgimns.add(mnCommon);
                }
            }
            paramMap.put("dgimns", dgimns);
            paramMap.put("mnandname", mnandname);
            paramMap.put("monitortime", monitortime);
            paramMap.put("mnandtype", mnandtype);
            paramMap.put("mnandid", mnandid);
            Map<String, Object> codeandname = getPollutantCodeAndNameByTypes(monitorpointtypes);
            paramMap.put("codeandname", codeandname);
            result = wallChartOperationService.countEntPointOverAlarmTimesDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2022/06/22 0022 上午 09:16
     * @Description: 通过监测类型和监测时间统计各类型某日的报警数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-mm-dd  monitorpointtypes:监测点类型 reminds:报警类型
     * @throws:
     */
    @RequestMapping(value = "/countEnvMonitorTypeAlarmPointNumDataByParam", method = RequestMethod.POST)
    public Object countEnvMonitorTypeAlarmPointNumDataByParam(@RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
                                                              @RequestJson(value = "reminds") List<Integer> reminds,
                                                              @RequestJson(value = "monitortime") String monitortime,
                                                              @RequestJson(value = "datetype", required = false) String datetype
    ) throws Exception {
        try {
            String userId = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultmMap = new HashMap<>();
            paramMap.put("userid", userId);
            List<String> mns = new ArrayList<>();
            List<Map<String, Object>> allpoints = wallChartOperationService.getUserMonitorPointRelationDataByUserId(paramMap);
            Map<String, Object> mnandtypecode = new HashMap<>();
            Map<String, Object> typename = new HashMap<>();
            Map<String, Object> typeorder = new HashMap<>();
            if (monitorpointtypes != null && monitorpointtypes.size() > 0) {
                for (Map<String, Object> map : allpoints) {
                    if (map.get("DGIMN") != null) {
                        if (!mns.contains(map.get("DGIMN").toString())) {
                            mns.add(map.get("DGIMN").toString());
                            mnandtypecode.put(map.get("DGIMN").toString(), map.get("FK_MonitorPointType"));
                            if (map.get("FK_MonitorPointType") != null) {
                                typename.put(map.get("FK_MonitorPointType").toString(), map.get("monitorpointtypename"));
                            }
                            if (map.get("OrderIndex") != null) {
                                typeorder.put(map.get("FK_MonitorPointType").toString(), map.get("OrderIndex"));
                            } else {
                                if (map.get("FK_MonitorPointType") != null) {
                                    typeorder.put(map.get("FK_MonitorPointType").toString(), map.get("FK_MonitorPointType"));
                                } else {
                                    typeorder.put(map.get("FK_MonitorPointType").toString(), 0);
                                }
                            }
                        }
                    }
                }
            }
            Map<String, Integer> overmap = new HashMap<>();
            Map<String, Integer> earlymap = new HashMap<>();
            Map<String, Integer> changemap = new HashMap<>();
            Map<String, Integer> ycmap = new HashMap<>();
            List<String> alarmMns;
            for (int remind : reminds) {
                Map<String, Date> mnAndTime = new HashMap<>();
                //根据类型查询该类型的报警数据信息
                alarmMns = getAllAlarmMonitorDataByParam(mns, monitortime, remind, datetype);
                for (String dgimn : mns) {
                    if (alarmMns.contains(dgimn)) {
                        String typecode = mnandtypecode.get(dgimn).toString();
                        if (remind == CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode()) {  //阈值
                            if (earlymap.get(typecode) != null) {
                                earlymap.put(typecode, earlymap.get(typecode) + 1);
                            } else {
                                earlymap.put(typecode, 1);
                            }
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode() ||
                                remind == CommonTypeEnum.RemindTypeEnum.FlowChangeEnum.getCode()) {    //突变
                            if (changemap.get(typecode) != null) {
                                changemap.put(typecode, changemap.get(typecode) + 1);
                            } else {
                                changemap.put(typecode, 1);
                            }
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode()) { //超标
                            if (overmap.get(typecode) != null) {
                                overmap.put(typecode, overmap.get(typecode) + 1);
                            } else {
                                overmap.put(typecode, 1);
                            }
                        } else if (remind == CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode()) { //异常
                            if (ycmap.get(typecode) != null) {
                                ycmap.put(typecode, ycmap.get(typecode) + 1);
                            } else {
                                ycmap.put(typecode, 1);
                            }
                        }
                    }
                }
            }
            List<Map<String, Object>> earlylist = new ArrayList<>();
            List<Map<String, Object>> overlist = new ArrayList<>();
            List<Map<String, Object>> changelist = new ArrayList<>();
            List<Map<String, Object>> yclist = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : earlymap.entrySet()) {
                Map<String, Object> earlymap2 = new HashMap<>();
                earlymap2.put("monitortypecode", entry.getKey());
                earlymap2.put("num", entry.getValue());
                earlymap2.put("monitortypename", typename.get(entry.getKey()));
                earlymap2.put("orderindex", typeorder.get(entry.getKey()));
                earlylist.add(earlymap2);

            }
            for (Map.Entry<String, Integer> entry : overmap.entrySet()) {
                Map<String, Object> overmap2 = new HashMap<>();
                overmap2.put("monitortypecode", entry.getKey());
                overmap2.put("num", entry.getValue());
                overmap2.put("monitortypename", typename.get(entry.getKey()));
                overmap2.put("orderindex", typeorder.get(entry.getKey()));
                overlist.add(overmap2);
            }
            for (Map.Entry<String, Integer> entry : changemap.entrySet()) {
                Map<String, Object> changemap2 = new HashMap<>();
                changemap2.put("monitortypecode", entry.getKey());
                changemap2.put("num", entry.getValue());
                changemap2.put("monitortypename", typename.get(entry.getKey()));
                changemap2.put("orderindex", typeorder.get(entry.getKey()));
                changelist.add(changemap2);
            }
            for (Map.Entry<String, Integer> entry : ycmap.entrySet()) {
                Map<String, Object> ycmap2 = new HashMap<>();
                ycmap2.put("monitortypecode", entry.getKey());
                ycmap2.put("num", entry.getValue());
                ycmap2.put("monitortypename", typename.get(entry.getKey()));
                ycmap2.put("orderindex", typeorder.get(entry.getKey()));
                yclist.add(ycmap2);
            }
            if (earlylist.size() > 0) {
                resultmMap.put("early", orderMonitorTypeData(earlylist));
            }
            if (overlist.size() > 0) {
                resultmMap.put("over", orderMonitorTypeData(overlist));
            }
            if (changelist.size() > 0) {
                resultmMap.put("change", orderMonitorTypeData(changelist));
            }
            if (yclist.size() > 0) {
                resultmMap.put("exception", orderMonitorTypeData(yclist));
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据报警类型 查询该类型的报警数据
     */
    private List<String> getAllAlarmMonitorDataByParam(List<String> mns, String monitortime, Integer remind, String datetype) {
        List<String> alarmmns = new ArrayList<>();
        List<Document> documents;
        String mnCommon;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("starttime", monitortime);
        paramMap.put("endtime", monitortime);
        paramMap.put("datetype", datetype);
        paramMap.put("mns", mns);
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remind)) {
            case ConcentrationChangeEnum:
                //1，浓度突变
                paramMap.put("collection", "SuddenRiseData");
                paramMap.put("monitortimekey", "ChangeTime");
                if (!"".equals(datetype)) {
                    //新增 小时 日查询
                    documents = onlineCountAlarmService.getHourOrDayChangeAlarmDataByParamMap(paramMap);
                } else {
                    documents = onlineService.getChangeAlarmDataByParamMap(paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        mnCommon = document.getString("_id");
                        alarmmns.add(mnCommon);
                    }
                }
                break;
            case EarlyAlarmEnum:
                //2，超阈值
                paramMap.put("collection", "EarlyWarnData");
                paramMap.put("monitortimekey", "EarlyWarnTime");
                if (!"".equals(datetype)) {
                    //新增小时、日查询
                    documents = onlineCountAlarmService.getHourOrDayLastAlarmDataByParamMap(paramMap);
                } else {
                    documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        mnCommon = document.getString("_id");
                        alarmmns.add(mnCommon);
                    }
                }
                break;
            case OverAlarmEnum:
                //3，数据超限
                paramMap.put("collection", "OverData");
                paramMap.put("monitortimekey", "OverTime");
                if (!"".equals(datetype)) {
                    //新增小时、日查询
                    documents = onlineCountAlarmService.getHourOrDayLastAlarmDataByParamMap(paramMap);
                } else {
                    documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        mnCommon = document.getString("_id");
                        alarmmns.add(mnCommon);
                    }
                }
                break;
            case ExceptionAlarmEnum:
                //4，数据异常
                String exceptionType;
                paramMap.put("collection", "ExceptionData");
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("exceptiontype", "-1");
                if (!"".equals(datetype)) {
                    //新增小时、日查询
                    documents = onlineCountAlarmService.getHourOrDayLastAlarmDataByParamMap(paramMap);
                } else {
                    documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                }
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        mnCommon = document.getString("_id");
                        alarmmns.add(mnCommon);
                    }
                }
        }
        return alarmmns;
    }


    private List<Map<String, Object>> orderMonitorTypeData(List<Map<String, Object>> list) {
        //排序
        Comparator<Object> orderbyorder = Comparator.comparing(m -> ((Map) m).get("orderindex").toString());
        list = list.stream().sorted(orderbyorder).collect(Collectors.toList());
        return list;
    }
}
