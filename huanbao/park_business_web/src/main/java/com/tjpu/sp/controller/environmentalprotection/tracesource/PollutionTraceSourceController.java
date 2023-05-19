package com.tjpu.sp.controller.environmentalprotection.tracesource;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutInfoService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OtherMonitorPointService;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.OutPutUnorganizedService;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationStandardService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineCountAlarmService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.tracesource.PollutionTraceSourceService;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.FactoryBoundaryStinkEnum;
import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * @author: xsm
 * @date: 2019/8/29 0029 上午 8:48
 * @Description: 污染溯源处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@RestController
@RequestMapping("pollutionTraceSource")
public class PollutionTraceSourceController {

    @Autowired
    private PollutionTraceSourceService pollutionTraceSourceService;
    @Autowired
    private GasOutPutInfoService gasOutPutInfoService;
    @Autowired
    private OutPutUnorganizedService outPutUnorganizedService;
    @Autowired
    private OtherMonitorPointService otherMonitorPointService;

    @Autowired
    private OnlineService onlineService;
    @Autowired
    private OnlineCountAlarmService onlineCountAlarmService;
    @Autowired
    private NavigationStandardService navigationStandardService;

    private final String db_hourData = "HourData";
    private final String db_minuteData = "MinuteData";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    /**
     * @author: xsm
     * @date: 2019/9/03 0003 下午 4:31
     * @Description: 根据监测时间获取当天主导风向、风速
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getLeadingWindDirectionAndWindSpeedByMonitorTime", method = RequestMethod.POST)
    public Object getLeadingWindDirectionAndWindSpeedByMonitorTime(@RequestJson(value = "monitortime", required = true) String monitortime) {
        try {
            //获取主导风向
            Map<String, Object> airmap = pollutionTraceSourceService.getLeadingWindDirectionAndWindSpeed(monitortime);
            return AuthUtil.parseJsonKeyToLower("success", airmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 8:53
     * @Description: 根据监测时间获取所有废气浓度值
     * @updateUser:chegnzq
     * @updateDate: 原接口名getAllTraceSourceMonitorPointDataByParamMap
     * @updateDescription:将所有点位接口拆分成废气点位数据和恶臭点位数据
     * @param:
     * @return:
     */
    @RequestMapping(value = "getGasOutPutTraceSourceDataByParamMap", method = RequestMethod.POST)
    public Object getGasOutPutTraceSourceDataByParamMap(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                        @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                        @RequestJson(value = "dgimns", required = false) Object dgimns,
                                                        @RequestJson(value = "smellcode", required = false) String smellcode) {
        try {
            //获取所有恶臭、厂界恶臭、voc点位关联气象的点位MN号
            Set<String> airmns = new HashSet<>();
            Set<String> othermns = new HashSet<>();
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> airmap = new HashMap<>();
            if("non".equals(smellcode)){
                return AuthUtil.parseJsonKeyToLower("success", airmap);
            }
            if (StringUtils.isNotBlank(smellcode)) {
                paramMap.put("smellcode", smellcode);
                List<Map<String, Object>> list = gasOutPutInfoService.selectGasInfoBySmellCodeAndMns(paramMap);
                List<String> collect = list.stream().filter(m -> m.get("FK_OutPutID") != null).map(m -> m.get("FK_OutPutID").toString()).collect(Collectors.toList());
                if(collect.size()==0){
                    return AuthUtil.parseJsonKeyToLower("success", airmap);
                }
                paramMap.clear();
                paramMap.put("outputids", collect);
            }
            if (dgimns != null) {
                paramMap.put("dgimns", JSONArray.fromObject(dgimns));
            }
            //获取所有废气排口信息
            List<Map<String, Object>> gasmns = pollutionTraceSourceService.getGasOutPutInfo(paramMap);
            for (Map<String, Object> map : gasmns) {
                othermns.add(map.get("DGIMN").toString());
            }
            List<Map<String, Object>> datalist = pollutionTraceSourceService.getTraceSourceMonitorPointOnlineData(monitortime, airmns, othermns, pollutantcode, gasmns);

            airmap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", airmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/10/23 0023 下午 3:54
     * @Description: 获取恶臭, voc监测点的浓度值和风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getStenchMonitorTraceSourceDataByParamMap", method = RequestMethod.POST)
    public Object getStenchMonitorTraceSourceDataByParamMap(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                            @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                            @RequestJson(value = "dgimns", required = false) Object dgimns,
                                                            @RequestJson(value = "monitorpointcategory", required = false) String monitorpointcategory,
                                                            @RequestJson(value = "monitorpointtypes") Object monitorpointtypes,
                                                            @RequestJson(value = "smellcode", required = false) String smellcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> airmap = new HashMap<>();
            paramMap.put("monitorpointcategory", monitorpointcategory);
            if("non".equals(smellcode)){
                return AuthUtil.parseJsonKeyToLower("success", airmap);
            }
            if (StringUtils.isNotBlank(smellcode)) {
                paramMap.put("smellcode", smellcode);
                //获取恶臭,voc监测点
                List<Map<String, Object>> list = otherMonitorPointService.selectStinkInfoBySmellcodeAndMns(paramMap);
                List<String> collect = list.stream().filter(m -> m.get("FK_OutPutID") != null).map(m -> m.get("FK_OutPutID").toString()).collect(Collectors.toList());
                if(collect.size()==0){
                    return AuthUtil.parseJsonKeyToLower("success", airmap);
                }
                paramMap.clear();
                paramMap.put("outputids", collect);
            }

            if (dgimns != null) {
                paramMap.put("dgimns", JSONArray.fromObject(dgimns));
            }
            paramMap.put("monitorpointtypes", JSONArray.fromObject(monitorpointtypes));
            List<Map<String, Object>> mns = pollutionTraceSourceService.getTraceSourceMonitorPointMN(paramMap);
            Set<String> airmns = new HashSet<>();
            Set<String> othermns = new HashSet<>();
            for (Map<String, Object> map : mns) {
                if (map.get("airmn") != null) {
                    airmns.add(map.get("airmn").toString());
                }
                othermns.add(map.get("DGIMN").toString());
            }

            List<Map<String, Object>> datalist = pollutionTraceSourceService.getTraceSourceMonitorPointOnlineData(monitortime, airmns, othermns, pollutantcode, mns);

            airmap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", airmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/10/23 0023 下午 3:54
     * @Description: 获取厂界恶臭监测点的浓度值和风向、风速信息
     * @updateUser:xsm
     * @updateDate:2021/09/22
     * @updateDescription:点位状态根据该时刻的数据判断
     * @param: [monitortime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getFactoryStenchTraceSourceDataByParamMap", method = RequestMethod.POST)
    public Object getFactoryStenchTraceSourceDataByParamMap(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                            @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                            @RequestJson(value = "monitorpointtypes") Object monitorpointtypes,
                                                            @RequestJson(value = "dgimns", required = false) Object dgimns,
                                                            @RequestJson(value = "smellcode", required = false) String smellcode) {
        try {
            //获取所有恶臭点位MN号
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> airmap = new HashMap<>();
            if("non".equals(smellcode)){
                return AuthUtil.parseJsonKeyToLower("success", airmap);
            }

            if (StringUtils.isNotBlank(smellcode)) {
                paramMap.put("smellcode", smellcode);
                List<Map<String, Object>> list = outPutUnorganizedService.selectFactStenchInfoBySmellCodeAndMns(paramMap);
                List<String> collect = list.stream().filter(m -> m.get("FK_OutPutID") != null).map(m -> m.get("FK_OutPutID").toString()).collect(Collectors.toList());
                if(collect.size()==0){
                    return AuthUtil.parseJsonKeyToLower("success", airmap);
                }
                paramMap.clear();
                paramMap.put("outputids", collect);
            }
            if (dgimns != null) {
                paramMap.put("dgimns", JSONArray.fromObject(dgimns));
            }
            paramMap.put("monitorpointtypes", JSONArray.fromObject(monitorpointtypes));
            List<Map<String, Object>> mns = pollutionTraceSourceService.getTraceSourceMonitorPointMN(paramMap);
            Set<String> airmns = new HashSet<>();
            Set<String> othermns = new HashSet<>();
            for (Map<String, Object> map : mns) {
                if (map.get("airmn") != null) {
                    airmns.add(map.get("airmn").toString());
                }
                othermns.add(map.get("DGIMN").toString());
            }

            List<Map<String, Object>> datalist = pollutionTraceSourceService.getTraceSourceMonitorPointOnlineData(monitortime, airmns, othermns, pollutantcode, mns);

            airmap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", airmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2020/12/10 0010 上午 9:45
     * @Description: 根据监测时间获取所有微站浓度值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMicroStationTraceSourceDataByParamMap", method = RequestMethod.POST)
    public Object getMicroStationTraceSourceDataByParamMap(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                        @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                        @RequestJson(value = "pointtype", required = false) Integer pointtype,
                                                        @RequestJson(value = "dgimns", required = false) Object dgimns
                                                        ) {
        try {
            //获取所有微站关联气象的点位MN号
            Set<String> airmns = new HashSet<>();
            Set<String> othermns = new HashSet<>();
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> airmap = new HashMap<>();
            if (dgimns != null) {
                paramMap.put("dgimns", JSONArray.fromObject(dgimns));
            }
            if(pointtype!=null){
                paramMap.put("pointtype", pointtype);
            }else{
                paramMap.put("pointtype", CommonTypeEnum.MonitorPointTypeEnum.MicroStationEnum.getCode());
            }
            //获取所有微站信息
            List<Map<String, Object>> gasmns = pollutionTraceSourceService.getOthorPointInfoByPointType(paramMap);
            for (Map<String, Object> map : gasmns) {
                othermns.add(map.get("DGIMN").toString());
            }
            List<Map<String, Object>> datalist = pollutionTraceSourceService.getTraceSourceMonitorPointOnlineData(monitortime, airmns, othermns, pollutantcode, gasmns);
            airmap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", airmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/29 0029 上午 8:53
     * @Description: 根据监测时间获取当前时间点的点位的风向风速及经纬度信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointInfoAndWindDataByParamMap", method = RequestMethod.POST)
    public Object getMonitorPointInfoAndWindDataByParamMap(@RequestJson(value = "monitortime", required = true) String monitortime) {
        try {
            //获取所有在该时间点的点位Mn号的风向风速信息
            List<Map<String, Object>> datalist = pollutionTraceSourceService.getMonitorPointDgimnAndWindDataByMonitortime(monitortime);
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2019/9/4 0004 上午 10:12
     * @Description: 自定义查询条件获取突高点和报警时段数据
     * @updateUser:xsm
     * @updateDate:2021/05/18 0018 下午 12:03
     * @updateDescription:突增、超标数据换源查询（从超标model表和突变model表中查询）
     * @param:
     * @return:
     */
    @RequestMapping(value = "countHighAlarmTimeDataByParam", method = RequestMethod.POST)
    public Object countHighAlarmTimeDataByParam(
            @RequestJson(value = "starttime", required = false) String starttime,
            @RequestJson(value = "endtime", required = false) String endtime,
            @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes,
            @RequestJson(value = "monitorpointcategory",required = false) Integer monitorpointcategory
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            int preHour = Integer.parseInt(DataFormatUtil.parseProperties("pre.hour"));
            String ymdh = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH");
            String mm = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", ":mm");
            String preYMDH = DataFormatUtil.getBeforeByHourTime(preHour, ymdh);
            starttime = preYMDH + mm;
            Map<String, Object> mnAndMonitorPointName = new HashMap<>();
            Map<String, Object> mnAndMonitorPointId = new HashMap<>();
            Map<String, Object> mnAndPollutionName = new HashMap<>();
            Map<String, Object> mnAndLongitude = new HashMap<>();
            Map<String, Object> mnAndLatitude = new HashMap<>();
            Map<String, Integer> mnAndMonitorPointType = new HashMap<>();
            List<Map<String, Object>> monitorPoints;
            String mnCommon;
            List<String> mns = new ArrayList<>();
            Map<String,Object> paramTemp = new HashMap<>();
            paramTemp.put("outputids",Arrays.asList());
            if (monitorpointcategory!=null){
                paramTemp.put("monitorPointCategory",monitorpointcategory);
            }
            for (Integer type : monitorpointtypes) {
                paramTemp.put("monitorpointtype",type);
                monitorPoints = onlineService.getMonitorPointDataByParam(paramTemp);
                for (Map<String, Object> map : monitorPoints) {
                    mnCommon = map.get("dgimn").toString();
                    mns.add(mnCommon);
                    mnAndPollutionName.put(mnCommon, map.get("pollutionname"));
                    mnAndMonitorPointName.put(mnCommon, map.get("monitorpointname"));
                    mnAndMonitorPointId.put(mnCommon, map.get("monitorpointid"));
                    mnAndLongitude.put(mnCommon, map.get("Longitude"));
                    mnAndLatitude.put(mnCommon, map.get("Latitude"));
                    mnAndMonitorPointType.put(mnCommon,type);
                }
            }
            //获取主要污染物信息
            paramTemp.clear();
            paramTemp.put("pollutanttypes",monitorpointtypes);
            List<Map<String, Object>> keyPollutants =  onlineService.getKeyPollutantsByParam(paramTemp);
            Set<String> pollutantCodes = new HashSet<>();
            Map<String,Object> codeAndName = new HashMap<>();
            String pollutantCode;
            for (Map<String,Object> keyPollutant:keyPollutants ){
                if (keyPollutant.get("Code")!=null){
                    pollutantCode = keyPollutant.get("Code").toString();
                    pollutantCodes.add(pollutantCode);
                    codeAndName.put(pollutantCode,keyPollutant.get("Name"));
                }
            }
            paramMap.put("pollutantcodes", pollutantCodes);
            paramMap.put("mns", mns);
            paramMap.put("starttime", starttime + ":00");
            paramMap.put("endtime", endtime + ":59");
            //1，浓度突变
            paramMap.put("collection", "SuddenRiseData");
            paramMap.put("monitortimekey", "ChangeTime");
            Map<String,Object> conChange  = onlineCountAlarmService.getChangeAndOverAlarmDataByParamMap(paramMap);
            //2，数据超限
            paramMap.put("collection", "OverData");
            paramMap.put("monitortimekey", "OverTime");
            Map<String,Object> conOver  = onlineCountAlarmService.getChangeAndOverAlarmDataByParamMap(paramMap);
            for (String dgimn:mns){
                Map<String,Object> onemap = null;
                Map<String,Object> twomap = null;
                if (conChange!=null&&conChange.size()>0){
                    if (conChange.get(dgimn)!=null){
                        onemap = (Map<String, Object>) conChange.get(dgimn);
                    }
                }
                if (conOver!=null&&conOver.size()>0){
                    if (conOver.get(dgimn)!=null){
                        twomap = (Map<String, Object>) conOver.get(dgimn);
                    }
                }
            List< Map<String,Object>> dataList = new ArrayList<>();
            for (String code:pollutantCodes){
                String strs = "";
                Map<String,Object> objmap = new HashMap<>();
                if (onemap!=null){
                    if (onemap.get(code)!=null){
                        strs =strs+onemap.get(code);
                    }
                }
                if (twomap!=null){
                    if (twomap.get(code)!=null){
                        strs =strs+twomap.get(code);
                    }
                }
                if (!"".equals(strs)) {
                    objmap.put("timelist", strs);
                    objmap.put("pollutantname", codeAndName.get(code));
                    objmap.put("pollutantcode", code);
                    dataList.add(objmap);
                    //objmap.put("orderindex",strs);
                }
            }
            if (dataList!=null&&dataList.size()>0) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("dgimn", dgimn);
                resultMap.put("monitorpointtype", mnAndMonitorPointType.get(dgimn));
                resultMap.put("pollutionname", mnAndPollutionName.get(dgimn));
                resultMap.put("monitorpointname", mnAndMonitorPointName.get(dgimn));
                resultMap.put("monitorpointid", mnAndMonitorPointId.get(dgimn));
                resultMap.put("Longitude", mnAndLongitude.get(dgimn));
                resultMap.put("Latitude", mnAndLatitude.get(dgimn));
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

    /**
     * @author: lip
     * @date: 2019/10/25 0025 下午 3:45
     * @Description: 获取污染物突增报警时间数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getPollutantTimeData(Map<String, List<String>> pollutantAndTime, Map<String, Object> codeAndName) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        String timelist;
        for (String pollutantCode : pollutantAndTime.keySet()) {
            if (codeAndName.keySet().contains(pollutantCode)){
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("pollutantcode", pollutantCode);
                resultMap.put("orderindex", pollutantAndTime.get(pollutantCode).size());
                timelist = DataFormatUtil.mergeContinueDate(pollutantAndTime.get(pollutantCode), 20,"yyyy-MM-dd HH:mm", "、", "HH:mm");
                resultMap.put("timelist", timelist);
                resultList.add(resultMap);
            }
        }
        for (Map<String, Object> resultMap : resultList) {
            if (codeAndName.get(resultMap.get("pollutantcode")) != null) {
                resultMap.put("pollutantname", codeAndName.get(resultMap.get("pollutantcode")));
            } else {
                resultMap.put("pollutantname", codeAndName.get(resultMap.get("pollutantcode")));
            }
        }
        //排序
        List<Map<String, Object>> sortResultList = resultList.stream().sorted(
                Comparator.comparingInt((Map m) -> Integer.parseInt(m.get("orderindex").toString())).reversed()).collect(Collectors.toList());
        return sortResultList;
    }

    /**
     * @author: lip
     * @date: 2019/10/25 0025 下午 3:34
     * @Description: 组装突变数据（浓度突变+排放量突变）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: {"1":"0.1","2":""}
     * @return:
     */
    private void setChangeData(List<Document> changeData, String hourDataKey, Map<String, Map<String, List<String>>> mnAndPollutantAndTime) {
        Map<String, Map<String, Map<String, Float>>> mnAndPollutantAndTimeAndValue = new HashMap<>();
        Map<String, Map<String, Float>> pollutantAndTimeAndValue;
        Map<String, Float> timeAndValue;
        String mn;
        String ymdhm;
        Object pollutantCode;
        Float pollutantValue;
        List<Map<String, Object>> pollutantList;
        for (Document document : changeData) {
            mn = document.getString("DataGatherCode");

            ymdhm = DataFormatUtil.getDateYMDHM(document.getDate("MonitorTime"));
            if (mnAndPollutantAndTimeAndValue.containsKey(mn)) {
                pollutantAndTimeAndValue = mnAndPollutantAndTimeAndValue.get(mn);
            } else {
                pollutantAndTimeAndValue = new LinkedHashMap<>();
            }
            pollutantList = document.get(hourDataKey, List.class);
            for (Map<String, Object> pollutant : pollutantList) {
                pollutantCode = pollutant.get("PollutantCode");
                pollutantValue = pollutant.get("AvgStrength") != null ? Float.parseFloat(pollutant.get("AvgStrength").toString()) : null;
                if (pollutantCode != null && pollutantValue != null) {
                    if (pollutantAndTimeAndValue.containsKey(pollutantCode)) {
                        timeAndValue = pollutantAndTimeAndValue.get(pollutantCode);
                    } else {
                        timeAndValue = new LinkedHashMap<>();
                    }
                    timeAndValue.put(ymdhm, pollutantValue);
                    pollutantAndTimeAndValue.put(pollutantCode.toString(), timeAndValue);
                }
            }
            mnAndPollutantAndTimeAndValue.put(mn, pollutantAndTimeAndValue);
        }
        if (mnAndPollutantAndTimeAndValue.size() > 0) {
            for (String mnKey : mnAndPollutantAndTimeAndValue.keySet()) {

                pollutantAndTimeAndValue = mnAndPollutantAndTimeAndValue.get(mnKey);
                Map<String, List<String>> pollutantAndTime = new HashMap<>();
                for (String pollutant : pollutantAndTimeAndValue.keySet()) {

                    timeAndValue = pollutantAndTimeAndValue.get(pollutant);
                    timeAndValue = findOutLiers((LinkedHashMap<String, Float>) timeAndValue);
                    if (timeAndValue.size() > 0) {
                        List<String> times = new ArrayList<>();
                        for (String time : timeAndValue.keySet()) {
                            times.add(time);
                        }
                        pollutantAndTime.put(pollutant, times);
                    }
                }
                mnAndPollutantAndTime.put(mnKey, pollutantAndTime);
            }
        }
    }


    /**
     * @author: lip
     * @date: 2019/10/25 0025 下午 5:32
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: paramsInfo：{key:value}
     * @return: 符合条件的{key:value}
     */
    private Map<String, Float> findOutLiers(LinkedHashMap<String, Float> paramsInfo) {
        if (paramsInfo == null || paramsInfo.size() == 0) return null;
        Map<String, Float> params = paramsInfo.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
        Map<String, Float> resultMap = new LinkedHashMap<>();
        BigDecimal[] datas = new BigDecimal[params.size()];
        String[] names = new String[params.size()];
        Float[] values = new Float[params.size()];
        int i = -1;
        for (Map.Entry<String, Float> entry : params.entrySet()) {
            Float value = entry.getValue();
            i++;
            datas[i] = BigDecimal.valueOf(value);
            names[i] = entry.getKey();
            values[i] = value;
        }
        int len = datas.length;
        if (1 < len && len < 4) {
            if (len == 2) {
                if (datas[1].divide(datas[0], ROUND_HALF_UP, 4).floatValue() > 2) {
                    resultMap.put(names[1], values[1]);
                }
            } else {
                BigDecimal standardDeviation = getStandardDeviation(datas);
                float v = (datas[1].subtract(datas[0])).floatValue();
                if (v > standardDeviation.floatValue()) {
                    resultMap.put(names[1], values[1]);
                }
                float v1 = (datas[2].subtract(datas[1])).floatValue();
                if (v1 > standardDeviation.floatValue()) {
                    resultMap.put(names[2], values[2]);
                }
            }
        } else if (len >= 4) {
            BigDecimal q1;
            BigDecimal q3;
            BigDecimal upperLimitValue;
            int index;
            // n代表项数，因为下标是从0开始所以这里理解为：len = n+1
            if (len % 2 == 0) { // 偶数
                index = new BigDecimal(len).divide(new BigDecimal("4")).intValue();
                q1 = datas[index - 1].multiply(new BigDecimal("0.25")).add(datas[index].multiply(new BigDecimal("0.75")));
                index = new BigDecimal(3 * (len + 1)).divide(new BigDecimal("4")).intValue();
                q3 = datas[index - 1].multiply(new BigDecimal("0.75")).add(datas[index].multiply(new BigDecimal("0.25")));
            } else { // 奇数
                q1 = datas[new BigDecimal(len).multiply(new BigDecimal("0.25")).intValue()];
                q3 = datas[new BigDecimal(len).multiply(new BigDecimal("0.75")).intValue()];
            }
            BigDecimal num1 = new BigDecimal("1.5");
            BigDecimal num05 = new BigDecimal("0.3");
            upperLimitValue = q3.subtract(q1).multiply(num1).add(q3);
            for (int k = 0; k < datas.length; k++) {
                if (datas[k].compareTo(upperLimitValue) > 0&&
                        (datas[k].subtract(upperLimitValue)).compareTo(upperLimitValue.multiply(num05))>0) {
                    resultMap.put(names[k], values[k]);
                }
            }
        }
        return resultMap;
    }

    /**
     * @author: zzc
     * @date: 2019/9/25 18:58
     * @Description: 求出标准差
     * @param:
     * @return:
     */
    private static BigDecimal getStandardDeviation(BigDecimal[] datas) {
        BigDecimal sum = new BigDecimal(0);
        for (BigDecimal data : datas) {
            sum = sum.add(data);
        }
        BigDecimal avg = sum.divide(new BigDecimal(datas.length), ROUND_HALF_UP, 4);
        BigDecimal total = new BigDecimal(0);   //平方和
        for (BigDecimal data : datas) total = total.add((data.subtract(avg)).pow(2));
        BigDecimal divide = total.divide(new BigDecimal(datas.length), ROUND_HALF_UP, 4);
        return DataFormatUtil.sqrt(divide, 4).add(avg);
    }


    /**
     * @author: lip
     * @date: 2019/10/25 0025 下午 3:34
     * @Description: 组装超阈值，超限，异常数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private void setEarlyOverExceptionData(List<Document> changeData, String timeKey, Map<String, Map<String, List<String>>> mnAndPollutantAndTime) {
        Map<String, List<String>> pollutantAndTime;
        String mn;
        String ymdhm;
        Object pollutantCode;
        List<String> times;
        for (Document document : changeData) {
            mn = document.getString("DataGatherCode");
            ymdhm = DataFormatUtil.getDateYMDHM(document.getDate(timeKey));

            if (mnAndPollutantAndTime.containsKey(mn)) {
                pollutantAndTime = mnAndPollutantAndTime.get(mn);
            } else {
                pollutantAndTime = new HashMap<>();
            }
            pollutantCode = document.get("PollutantCode");
            if (pollutantCode != null) {
                if (pollutantAndTime.containsKey(pollutantCode)) {
                    times = pollutantAndTime.get(pollutantCode);
                } else {
                    times = new ArrayList<>();
                }
                if (!times.contains(ymdhm)) {
                    times.add(ymdhm);
                    pollutantAndTime.put(pollutantCode.toString(), times);
                }
            }
            mnAndPollutantAndTime.put(mn, pollutantAndTime);
        }
    }


    /**
     * @author: lip
     * @date: 2019/9/4 0004 上午 10:12
     * @Description: 统计厂界恶臭溯源占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEntStinkTraceRateByParam", method = RequestMethod.POST)
    public Object countEntStinkTraceRateByParam(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "longitude") String longitude,
            @RequestJson(value = "latitude") String latitude
    ) {

        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            int preHour = Integer.parseInt(DataFormatUtil.parseProperties("pre.hour"));
            String ymdh = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH");
            String mm = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", ":mm");
            String preYMDH = DataFormatUtil.getBeforeByHourTime(preHour, ymdh);
            starttime = preYMDH + mm;
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("longitude", longitude);
            paramMap.put("latitude", latitude);
            paramMap.put("collection", "MinuteData");

            paramMap.put("monitorpointtypes", Arrays.asList(
                    CommonTypeEnum.MonitorPointTypeEnum.EnvironmentalStinkEnum.getCode()
            ));

            List<Map<String, Object>> stinkPointList = pollutionTraceSourceService.getSensitivePointDataByParamMap(paramMap);
            if (stinkPointList.size() > 0) {
                List<String> mns = new ArrayList<>();
                String dgimn;
                List<String> stinkMns = new ArrayList<>();
                for (Map<String, Object> map : stinkPointList) {
                    dgimn = map.get("dgimn").toString();
                    stinkMns.add(dgimn);
                }
                mns.addAll(stinkMns);
                resultList = getTraceRate(starttime, endtime, mns, pollutantcode);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2019/9/20 0020 上午 10:18
     * @Description: 获取环境恶臭和厂界恶臭溯源占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getTraceRate(String starttime, String endtime, List<String> mns, String pollutantcode) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        //环境恶臭
        List<String> stinkMns = mns;

        //厂界恶臭
        Map<String, String> mnAndMonitorPoint = onlineService.getMNAndMonitorPoint(new ArrayList<>(), FactoryBoundaryStinkEnum.getCode());
        List<String> entStinkMns = new ArrayList<>(mnAndMonitorPoint.keySet());
        mns.addAll(entStinkMns);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("starttime", starttime + ":00");
        paramMap.put("endtime", endtime + ":59");
        paramMap.put("mns", mns);
        paramMap.put("collection", db_hourData);
        paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
        List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
        if (documents.size() > 0) {
            Map<String, Map<String, Double>> mnAndTimeAndValue = new HashMap<>();
            Map<String, Double> timeAndValue;
            String mn;
            String ymdh;
            Double value = null;
            List<Map<String, Object>> pollutantList;
            for (Document document : documents) {
                mn = document.getString("DataGatherCode");
                ymdh = DataFormatUtil.getDateYMDH(document.getDate("MonitorTime"));
                pollutantList = (List<Map<String, Object>>) document.get("HourDataList");
                for (Map<String, Object> map : pollutantList) {
                    if (pollutantcode.equals(map.get("PollutantCode"))) {
                        value = map.get("AvgStrength") != null ? Double.parseDouble(map.get("AvgStrength").toString()) : null;
                        break;
                    }
                }
                if (value != null) {
                    if (mnAndTimeAndValue.containsKey(mn)) {
                        timeAndValue = mnAndTimeAndValue.get(mn);
                    } else {
                        timeAndValue = new HashMap<>();
                    }
                    timeAndValue.put(ymdh, value);
                    mnAndTimeAndValue.put(mn, timeAndValue);
                }
            }
            if (mnAndTimeAndValue.size() > 0) {
                String startYMDH = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH");
                String endYMDH = DataFormatUtil.FormatDateOneToOther(endtime, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH");
                List<String> ymdhs = DataFormatUtil.getYMDHBetween(startYMDH, endYMDH);
                ymdhs.add(endYMDH);
                Map<String, Double> stinkTimeAndValue;
                Map<String, Double> entStinkTimeAndValue;
                Double RValue;
                Double RStandValue = Double.parseDouble(DataFormatUtil.parseProperties("r.value"));
                Map<String, Double> entMNAndRValue = new HashMap<>();
                for (String mnKey : stinkMns) {
                    stinkTimeAndValue = mnAndTimeAndValue.get(mnKey);
                    if (stinkTimeAndValue != null) {
                        for (String entMN : entStinkMns) {
                            entStinkTimeAndValue = mnAndTimeAndValue.get(entMN);
                            if (entStinkTimeAndValue != null) {
                                List<Double> xData = new ArrayList<>();
                                List<Double> yData = new ArrayList<>();
                                for (String ymdhK : ymdhs) {
                                    if (stinkTimeAndValue.get(ymdhK) != null && entStinkTimeAndValue.get(ymdhK) != null) {
                                        xData.add(stinkTimeAndValue.get(ymdhK));
                                        yData.add(entStinkTimeAndValue.get(ymdhK));
                                    }
                                }
                                RValue = DataFormatUtil.getRelationPercent(xData, yData);
                                if (RStandValue <= 100 * RValue) {
                                    if (entMNAndRValue.containsKey(entMN)) {
                                        if (RValue < entMNAndRValue.get(entMN)) {
                                            RValue = entMNAndRValue.get(entMN);
                                        }
                                    }
                                    entMNAndRValue.put(entMN, RValue);
                                }
                            }
                        }
                    }

                }
                if (entMNAndRValue.size() > 0) {
                    //排序map
                    Map<String, Double> result = new LinkedHashMap<>();
                    entMNAndRValue.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
                    paramMap.put("starttime", starttime);
                    paramMap.put("endtime", endtime);
                    List<String> dgimns = new ArrayList<>(entMNAndRValue.keySet());
                    List<Map<String, Object>> overData = onlineService.countMonitorPointEarlyAndOverDataByParamMap(starttime, endtime, dgimns);
                    Map<String, Integer> mnAndAlarmNum = new HashMap<>();
                    if (overData.size() > 0) {
                        Integer over;
                        Integer early;
                        Integer concentrationchange;
                        for (Map<String, Object> map : overData) {
                            over = map.get("over").equals("") ? 0 : Integer.parseInt(map.get("over").toString());
                            early = map.get("early").equals("") ? 0 : Integer.parseInt(map.get("early").toString());
                            concentrationchange = map.get("concentrationchange").equals("") ? 0 : Integer.parseInt(map.get("concentrationchange").toString());
                            mnAndAlarmNum.put(map.get("dgimn").toString(), over + early + concentrationchange);
                        }
                    }

                    Map<String, String> mnAndPollution = onlineService.getMNAndPollution(new ArrayList<>(), FactoryBoundaryStinkEnum.getCode());
                    Map<String, String> mnAndPollutionId = onlineService.getMNAndPollutionId(new ArrayList<>(), FactoryBoundaryStinkEnum.getCode());

                    for (String mnKey : result.keySet()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("pollutionid", mnAndPollutionId.get(mnKey));
                        map.put("pollutionname", mnAndPollution.get(mnKey));
                        map.put("monitorpointname", mnAndMonitorPoint.get(mnKey));
                        map.put("alarmnum", mnAndAlarmNum.get(mnKey));
                        resultList.add(map);
                    }
                }
            }
        }


        return resultList;

    }


    /**
     * @author: lip
     * @date: 2019/9/4 0004 上午 10:12
     * @Description: 统计厂界恶臭溯源占比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countEntStinkTraceRateByDgimn", method = RequestMethod.POST)
    public Object countEntStinkTraceRateByDgimn(
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "dgimn") String dgimn

    ) {

        try {
            int preHour = Integer.parseInt(DataFormatUtil.parseProperties("pre.hour"));
            String ymdh = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH");
            String mm = DataFormatUtil.FormatDateOneToOther(starttime, "yyyy-MM-dd HH:mm", ":mm");
            String preYMDH = DataFormatUtil.getBeforeByHourTime(preHour, ymdh);
            starttime = preYMDH + mm;
            List<String> mns = new ArrayList<>();
            mns.add(dgimn);
            List<Map<String, Object>> resultList = getTraceRate(starttime, endtime, mns, pollutantcode);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/8/31 11:49
     * @Description: 根据自定义参数获取溯源监测点污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getTraceSourceMonitorPointPollutantByParamMap", method = RequestMethod.POST)
    public Object getTraceSourceMonitorPointPollutantByParamMap(@RequestJson(value = "monitorpointid") String monitorpointid,
                                                                @RequestJson(value = "monitorpointtype") Integer monitorpointtype

    ) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointid", monitorpointid);
            //获取该点位关联的污染物信息
            List<Map<String, Object>> pollutans = pollutionTraceSourceService.getTraceSourceMonitorPointPollutantInfoByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", pollutans);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getTraceSourceMonitorPointPollutantOnlineDataByParamMap", method = RequestMethod.POST)
    public Object getTraceSourceMonitorPointPollutantOnlineDataByParamMap(@RequestJson(value = "monitorpointid") String monitorpointid,
                                                                          @RequestJson(value = "dgimn") String dgimn,
                                                                          @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                          @RequestJson(value = "monitortime") String monitortime) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointid", monitorpointid);
            //获取该点位关联的污染物信息
            List<Map<String, Object>> pollutants = pollutionTraceSourceService.getTraceSourceMonitorPointPollutantInfoByParam(paramMap);
            if (pollutants != null && pollutants.size() > 0) {
                result = pollutionTraceSourceService.getTraceSourceMonitorPointPollutantOnlineDataByParamMap(dgimn, pollutants, monitortime);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2019/8/31 11:49
     * @Description: 根据自定义参数获取溯源监测点某个时间段内所有监测污染物的预警、报警、异常条数统计
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countTraceSourcePointPollutantAlarmNumByParamMap", method = RequestMethod.POST)
    public Object countTraceSourcePointPollutantAlarmNumByParamMap(@RequestJson(value = "monitorpointid") String monitorpointid,
                                                                   @RequestJson(value = "dgimn") String dgimn,
                                                                   @RequestJson(value = "monitorpointtype") Integer monitorpointtype,
                                                                   @RequestJson(value = "starttime") String starttime,
                                                                   @RequestJson(value = "endtime") String endtime) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("monitorpointtype", monitorpointtype);
            paramMap.put("monitorpointid", monitorpointid);
            //获取该点位关联的污染物信息
            List<Map<String, Object>> pollutants = pollutionTraceSourceService.getTraceSourceMonitorPointPollutantInfoByParam(paramMap);
            if (pollutants != null && pollutants.size() > 0) {
                result = pollutionTraceSourceService.countTraceSourcePointPollutantAlarmNumByParamMap(dgimn, pollutants, starttime,endtime);
            }
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2019/9/4 0004 上午 9:25
     * @Description: 根据自定义参数获取相关敏感点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Object getSensitivePointDataByParamMap(Integer longitude, Integer latitude, String starttime, String
            endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            List<Map<String, Object>> result = new ArrayList<>();
            paramMap.put("longitude", longitude);
            paramMap.put("latitude", latitude);
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            result = pollutionTraceSourceService.getSensitivePointDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/04/13 0013 上午 10:05
     * @Description: 根据监测点类型获取监测点的浓度值和风向、风速信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [monitortime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getMinuteMonitorDataAndWeatherDataByParamMap", method = RequestMethod.POST)
    public Object getMinuteMonitorDataAndWeatherDataByParamMap(@RequestJson(value = "monitortime", required = true) String monitortime,
                                                               @RequestJson(value = "pollutantcode", required = true) String pollutantcode,
                                                               @RequestJson(value = "dgimns", required = false) Object dgimns,
                                                               @RequestJson(value = "monitorpointtypes") List<Integer> monitorpointtypes) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> airmap = new HashMap<>();
            paramMap.put("monitorpointtypes", JSONArray.fromObject(monitorpointtypes));
            List<Map<String, Object>> mns = pollutionTraceSourceService.getTraceSourceMonitorPointInfoByParam(paramMap);
            Set<String> airmns = new HashSet<>();//查询点位风向 风速信息
            Set<String> othermns = new HashSet<>();//用来查询该时刻污染物浓度值
            for (Map<String, Object> map : mns) {
                if (map.get("airmn") != null) {
                    airmns.add(map.get("airmn").toString());
                }
                othermns.add(map.get("DGIMN").toString());
            }
            paramMap.put("monitorpointtypes", monitorpointtypes);
            paramMap.put("pollutantcode", pollutantcode);
            List<Map<String, Object>> colorDataList = navigationStandardService.getStandardColorDataByParamMap(paramMap);

            List<Map<String, Object>> datalist = pollutionTraceSourceService.getMinuteMonitorDataAndWeatherData(monitortime, airmns, othermns, pollutantcode, mns,colorDataList);
            airmap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", airmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getEntTraceSourceListDataByParamMap", method = RequestMethod.POST)
    public Object getEntTraceSourceListDataByParamMap(@RequestJson(value = "starttime", required = false) String starttime,
                                                      @RequestJson(value = "endtime", required = false) String endtime,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                      @RequestJson(value = "pagesize", required = false) Integer pagesize) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            //获取该点位关联的污染物信息
            Map<String, Object> pollutants = pollutionTraceSourceService.getEntTraceSourceListDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", pollutants);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
    @RequestMapping(value = "getEntTraceSourceDetailDataByParamMap", method = RequestMethod.POST)
    public Object getEntTraceSourceDetailDataByParamMap(@RequestJson(value = "monitortime", required = false) String monitortime,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                      @RequestJson(value = "pagesize", required = false) Integer pagesize) {
        try {
            //设置参数,根据点位ID及点位类型获取该点位的信息及该点位监测的污染物
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("monitortime", monitortime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            //获取该点位关联的污染物信息
            Map<String, Object> pollutants = pollutionTraceSourceService.getEntTraceSourceDetailDataByParamMap(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", pollutants);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
