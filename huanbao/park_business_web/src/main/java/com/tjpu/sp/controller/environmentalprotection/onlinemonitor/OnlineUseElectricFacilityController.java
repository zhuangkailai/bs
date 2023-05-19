package com.tjpu.sp.controller.environmentalprotection.onlinemonitor;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.*;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.mongo.MongoDataUtils;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.GasOutPutPollutantSetService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.useelectricfacility.UseElectricFacilityService;
import com.tjpu.sp.service.environmentalprotection.useelectricfacilitymonitorpoint.UseElectricFacilityMonitorPointService;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.ElectricFacilityEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.OnlineStatusEnum.ExceptionStatusEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.OnlineStatusEnum.OfflineStatusEnum;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.useElectricFacilityEnum.*;


/**
 * @author: chengzq
 * @date: 2020/6/19 0019 上午 9:09
 * @Description: 用电设施在线数据控制层
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
@RestController
@RequestMapping("onlineuseelectricfacility")
public class OnlineUseElectricFacilityController {

    @Autowired
    private UseElectricFacilityService useElectricFacilityService;
    @Autowired
    private OnlineService onlineService;

    @Autowired
    private UseElectricFacilityMonitorPointService useElectricFacilityMonitorPointService;

    @Autowired
    private GasOutPutPollutantSetService gasOutPutPollutantSetService;

    private final String db_hourData = "HourData";
    private final String db_RealTimeData = "RealTimeData";
    private final String db_dayData = "DayData";
    private final String db_monthData = "MonthData";

    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 9:13
     * @Description: 统计用电企业，用电设施，用电监测点个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countPollutionAndUseElectricFacilityInfo", method = RequestMethod.POST)
    public Object countPollutionAndUseElectricFacilityInfo() {
        try {
            List<Map<String, Object>> maps = useElectricFacilityService.countUseElectricFacilityInfo(new HashMap<>());
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 上午 10:33
     * @Description: 统计报警的用电设施数目
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "countUseElectricFacilityAlarmInfo", method = RequestMethod.POST)
    public Object countUseElectricFacilityAlarmInfo(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.countExceptoinModelDataByParamMap(paramMap);

            //获取有异常的mn号
            Set<String> collect = documents.stream().filter(m -> m.get("_id") != null).map(m -> m.get("_id").toString()).collect(Collectors.toSet());

            //异常的产污设施个数
            long productcount = useElectricFacility.stream().filter(m -> m.get("FacilityType") != null && m.get("dgimn") != null && m.get("PK_ID") != null && collect.contains(m.get("dgimn").toString())
                    && "1".equals(m.get("FacilityType").toString())).map(m -> m.get("PK_ID").toString()).distinct().count();
            //异常的治污设施个数
            long controlcount = useElectricFacility.stream().filter(m -> m.get("FacilityType") != null && m.get("dgimn") != null && m.get("PK_ID") != null && collect.contains(m.get("dgimn").toString())
                    && "2".equals(m.get("FacilityType").toString())).map(m -> m.get("PK_ID").toString()).distinct().count();
            resultMap.put("productcount", productcount);
            resultMap.put("controlcount", controlcount);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 下午 2:14
     * @Description: 统计用电企业报警信息排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countPollutionUseElectricFacilityAlarmInfo", method = RequestMethod.POST)
    public Object countPollutionUseElectricFacilityAlarmInfo(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //统计报警次数
            List<Document> documents = onlineService.countExceptoinModelDataByParamMap(paramMap);

            Map<String, List<Map<String, Object>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("FK_Pollutionid").toString()));

            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> maps = collect.get(FK_Pollutionid);
                Map<String, Object> stringObjectMap = maps.stream().findFirst().orElse(new HashMap<>());
                Set<String> mns = maps.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());

                data.put("pollutionname", stringObjectMap.get("pollutionname"));
                data.put("fk_pollutionid", stringObjectMap.get("FK_Pollutionid"));
                Integer count = documents.stream().filter(m -> m.get("_id") != null && m.get("count") != null && mns.contains(m.get("_id").toString())).collect(Collectors.summingInt(m -> Integer.valueOf(m.get("count").toString())));

                data.put("count", count);
                if (count > 0) {
                    resultList.add(data);
                }
            }


            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("count") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("count").toString()).reversed()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/20 0020 上午 10:46
     * @Description: 获取所有企业下每天用电量数据CouStrength累加趋势
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, pollutantcode, fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getAllPollutionUseElectricFacilityInfo", method = RequestMethod.POST)
    public Object getAllPollutionUseElectricFacilityInfo(@RequestJson(value = "starttime") String starttime,
                                                         @RequestJson(value = "endtime") String endtime) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String pollutantcode = powerUsageCode.getCode();
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", "DayData");
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, List<Document>> collect1 = monitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null).peek(m -> m.put("MonitorTime", FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd")))
                    .collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            List<Map<String, Object>> thisyearlist = getResultList(collect1, pollutantcode);


            String startTime = JSONObjectUtil.getStartTime(starttime);
            Calendar instance = Calendar.getInstance();
            instance.setTime(format.parse(startTime));
            instance.add(Calendar.YEAR, -1);
            paramMap.put("starttime", format.format(instance.getTime()));

            String endTime = JSONObjectUtil.getEndTime(endtime);
            instance.clear();
            instance.setTime(format.parse(endTime));
            instance.add(Calendar.YEAR, -1);
            paramMap.put("endtime", format.format(instance.getTime()));
            //查询日在线数据
            List<Document> lastmonitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, List<Document>> collect2 = lastmonitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null).peek(m -> m.put("MonitorTime", FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd")))
                    .collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            List<Map<String, Object>> lastyearlist = getResultList(collect2, pollutantcode);

            resultMap.put("thisyearlist", thisyearlist);
            resultMap.put("lastyearlist", lastyearlist);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/28 0028 下午 5:18
     * @Description: 统计每日用电量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [collect1, pollutantcode]
     * @throws:
     */
    private List<Map<String, Object>> getResultList(Map<String, List<Document>> collect1, String pollutantcode) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (String MonitorTime : collect1.keySet()) {
            List<Document> documents = collect1.get(MonitorTime);
            Map<String, Object> data = new HashMap<>();
            //污染物总量CouStrength
            Double total = documents.stream().filter(m -> m.get("DayDataList") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("DayDataList")).stream())
                    .filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString()))
                    .collect(Collectors.summingDouble(m -> Double.valueOf(m.get("CouStrength").toString())));
            data.put("total", total);
            data.put("monitortime", MonitorTime);
            resultList.add(data);
        }
        List<Map<String, Object>> collect = resultList.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList());
        return collect;
    }


    /**
     * @author: chengzq
     * @date: 2020/6/19 0019 下午 2:14
     * @Description: 获取用电企业报警趋势信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getPollutionUseElectricFacilityAlarmInfo", method = RequestMethod.POST)
    public Object getPollutionUseElectricFacilityAlarmInfo(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //统计报警次数
            List<Document> documents = onlineService.countDayExceptoinModelDataByParamMap(paramMap);
            documents.stream().filter(m -> m.get("_id") != null).forEach(m -> m.put("monitortime", m.remove("_id")));
            return AuthUtil.parseJsonKeyToLower("success", documents.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("monitortime").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/20 0020 上午 10:46
     * @Description: 统计各企业下用电量日数据CouStrength累加和排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, pollutantcode, fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEveryPollutionUseElectricFacilityInfo", method = RequestMethod.POST)
    public Object getEveryPollutionUseElectricFacilityInfo(@RequestJson(value = "starttime") String starttime,
                                                           @RequestJson(value = "endtime") String endtime) {
        try {
            String pollutantcode = powerUsageCode.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", "DayData");
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);


            Map<String, List<Map<String, Object>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null).collect(Collectors.groupingBy(m -> m.get("FK_Pollutionid").toString()));

            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> maps = collect.get(FK_Pollutionid);
                Set<String> mns = maps.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
                Map<String, Object> stringObjectMap = maps.stream().findFirst().orElse(new HashMap<>());

                data.put("pollutionname", stringObjectMap.get("pollutionname"));
                data.put("fk_pollutionid", stringObjectMap.get("FK_Pollutionid"));

                //污染物总量CouStrength
                Double total = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("DayDataList") != null && mns.contains(m.get("DataGatherCode").toString())).flatMap(m -> ((List<Map<String, Object>>) m.get("DayDataList")).stream())
                        .filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.summingDouble(m -> Double.valueOf(m.get("CouStrength").toString())));
                data.put("total", total);
                if (total > 0) {
                    resultList.add(data);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("total") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("total").toString()).reversed()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 上午 9:44
     * @Description: 统计企业产污设施和治污设施个数及报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "countPollutionAndUseElectricFacilityByParams", method = RequestMethod.POST)
    public Object countPollutionAndUseElectricFacilityByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                               @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                               @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            Map<String, List<Map<String, Object>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null).collect(Collectors
                    .groupingBy(m -> m.get("FK_Pollutionid").toString()));

            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> maps = collect.get(FK_Pollutionid);
                // 设施类型（1表示产污环节，2表示治污环节）
                //产污个数
                int productnum = maps.stream().filter(m -> m.get("FacilityType") != null && m.get("EquipmentName") != null && "1".equals(m.get("FacilityType").toString()))
                        .collect(Collectors.groupingBy(m -> m.get("EquipmentName").toString())).keySet().size();
                //治污个数
                int controlnum = maps.stream().filter(m -> m.get("FacilityType") != null && m.get("EquipmentName") != null && "2".equals(m.get("FacilityType").toString()))
                        .collect(Collectors.groupingBy(m -> m.get("EquipmentName").toString())).keySet().size();

                //产污报警个数
                int productalarmnum = maps.stream().filter(m -> m.get("FacilityType") != null && m.get("status") != null && m.get("EquipmentName") != null && "1"
                        .equals(m.get("FacilityType").toString()) && ExceptionStatusEnum.getCode().equals(m.get("status").toString()))
                        .collect(Collectors.groupingBy(m -> m.get("EquipmentName").toString())).keySet().size();
                //治污报警个数
                int controlalarmnum = maps.stream().filter(m -> m.get("FacilityType") != null && m.get("status") != null && m.get("EquipmentName") != null && "2"
                        .equals(m.get("FacilityType").toString()) && ExceptionStatusEnum.getCode().equals(m.get("status").toString()))
                        .collect(Collectors.groupingBy(m -> m.get("EquipmentName").toString())).keySet().size();

                //产污监测点个数
                long productmonitornum = maps.stream().filter(m -> m.get("FacilityType") != null && "1".equals(m.get("FacilityType").toString())).count();
                //治污监测点个数
                long controlmonitornum = maps.stream().filter(m -> m.get("FacilityType") != null && "2".equals(m.get("FacilityType").toString())).count();

                Map<String, Object> data = maps.stream().findFirst().orElse(new HashMap<>());
                result.put("productnum", productnum);
                result.put("controlnum", controlnum);
                result.put("productalarmnum", productalarmnum);
                result.put("controlalarmnum", controlalarmnum);
                result.put("productmonitornum", productmonitornum);
                result.put("controlmonitornum", controlmonitornum);
                result.put("monitormnum", productmonitornum + controlmonitornum);
                result.put("equipmentalarmnum", productalarmnum + controlalarmnum);//设施报警总数
                result.put("pollutionname", data.get("pollutionname"));
                result.put("address", data.get("address"));
                result.put("fkindustrytypename", data.get("fkindustrytypename"));
                result.put("fk_pollutionid", data.get("FK_Pollutionid"));
                result.put("corporationname", data.get("corporationname"));
                result.put("entsocialcreditcode", data.get("EntSocialcreditCode"));
                result.put("environmentalmanager", data.get("EnvironmentalManager"));
                result.put("linkmanphone", data.get("LinkManPhone"));

                resultList.add(result);
            }
            int size = resultList.size();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", size);
            resultMap.put("datalist", resultList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 上午 9:44
     * @Description: 统计企业产污设施信息及点位个数及报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "countPollutionAndProductFacilityByParams", method = RequestMethod.POST)
    public Object countPollutionAndProductFacilityByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                           @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            // 设施类型（1表示产污环节，2表示治污环节）
            paramMap.put("facilitytype", "1");
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            Map<String, Map<String, List<Map<String, Object>>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null && m.get("EquipmentName") != null).collect(Collectors
                    .groupingBy(m -> m.get("FK_Pollutionid").toString(), Collectors.groupingBy(m -> m.get("EquipmentName").toString())));

            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, List<Map<String, Object>>> stringListMap = collect.get(FK_Pollutionid);
                for (String EquipmentName : stringListMap.keySet()) {
                    Map<String, Object> result = new HashMap<>();
                    List<Map<String, Object>> maps = stringListMap.get(EquipmentName);
                    //产污设施监测点个数
                    long productnum = maps.size();
                    //报警产污设施监测点个数
                    long productalarmnum = maps.stream().filter(m -> m.get("status") != null && ExceptionStatusEnum.getCode().equals(m.get("status").toString())).count();

                    Map<String, Object> data = maps.stream().findFirst().orElse(new HashMap<>());
                    result.put("productmonitornum", productnum);
                    result.put("productmonitoralarmnum", productalarmnum);
                    result.put("EquipmentName", EquipmentName);
                    result.put("pollutionname", data.get("pollutionname"));
                    result.put("fk_pollutionid", data.get("FK_Pollutionid"));
                    result.put("PowerOnOffThreshold", data.get("PowerOnOffThreshold"));
                    result.put("LoadOnOffThreshold", data.get("LoadOnOffThreshold"));
                    result.put("PutIntoDate", data.get("PutIntoDate"));
                    resultList.add(result);
                }
            }
            int size = resultList.size();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", size);
            resultMap.put("datalist", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 上午 9:44
     * @Description: 统计企业产污设施信息及点位个数及报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "countPollutionAndControlFacilityByParams", method = RequestMethod.POST)
    public Object countPollutionAndControlFacilityByParams(@RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                           @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            // 设施类型（1表示产污环节，2表示治污环节）
            paramMap.put("facilitytype", "2");
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            Map<String, Map<String, List<Map<String, Object>>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null && m.get("EquipmentName") != null).collect(Collectors
                    .groupingBy(m -> m.get("FK_Pollutionid").toString(), Collectors.groupingBy(m -> m.get("EquipmentName").toString())));

            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, List<Map<String, Object>>> stringListMap = collect.get(FK_Pollutionid);
                for (String EquipmentName : stringListMap.keySet()) {
                    Map<String, Object> result = new HashMap<>();
                    List<Map<String, Object>> maps = stringListMap.get(EquipmentName);
                    //产污设施监测点个数
                    long productnum = maps.size();
                    //报警产污设施监测点个数
                    long productalarmnum = maps.stream().filter(m -> m.get("status") != null && ExceptionStatusEnum.getCode().equals(m.get("status").toString())).count();

                    Map<String, Object> data = maps.stream().findFirst().orElse(new HashMap<>());
                    result.put("controlmonitornum", productnum);
                    result.put("controlmonitoralarmnum", productalarmnum);
                    result.put("EquipmentName", EquipmentName);
                    result.put("pollutionname", data.get("pollutionname"));
                    result.put("fk_pollutionid", data.get("FK_Pollutionid"));
                    result.put("PowerOnOffThreshold", data.get("PowerOnOffThreshold"));
                    result.put("LoadOnOffThreshold", data.get("LoadOnOffThreshold"));
                    result.put("PutIntoDate", data.get("PutIntoDate"));
                    resultList.add(result);
                }
            }
            int size = resultList.size();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", size);
            resultMap.put("datalist", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 下午 12:04
     * @Description: 统计企业用电设施点位信息及报警个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @RequestMapping(value = "countUseElectricFacilityMonitorByParams", method = RequestMethod.POST)
    public Object countUseElectricFacilityMonitorByParams(@RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime,
                                                          @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                          @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(useElectricFacility);
            long total = pageInfo.getTotal();
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.countExceptoinModelDataByParamMap(paramMap);

            for (Map<String, Object> stringObjectMap : useElectricFacility) {
                String dgimn = stringObjectMap.get("dgimn") == null ? "" : stringObjectMap.get("dgimn").toString();
                String status = stringObjectMap.get("status") == null ? "" : stringObjectMap.get("status").toString();
                String nameByCode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(status);
                stringObjectMap.put("status", nameByCode);
                Integer collect = documents.stream().filter(m -> m.get("_id") != null && m.get("count") != null && dgimn.equals(m.get("_id").toString()))
                        .map(m -> m.get("count").toString()).collect(Collectors.summingInt(m -> Integer.valueOf(m)));
                stringObjectMap.put("alarmcount", collect);
            }
            resultMap.put("total", total);
            resultMap.put("datalist", useElectricFacility);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 下午 3:11
     * @Description: 通过监测时间查询产污设施异常信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getProductFacilityAlarmInfoByParams", method = RequestMethod.POST)
    public Object getProductFacilityAlarmInfoByParams(@RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            // 设施类型（1表示产污环节，2表示治污环节）
            paramMap.put("facilitytype", "1");
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);

            for (Document document : documents) {
                document.put("FirstExceptionTime", document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "HH:mm"));
                document.put("LastExceptionTime", document.get("LastExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("LastExceptionTime").toString(), "HH:mm"));
            }


            Map<String, Map<String, List<Map<String, Object>>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null && m.get("EquipmentName") != null)
                    .collect(Collectors.groupingBy(m -> m.get("FK_Pollutionid").toString(), Collectors.groupingBy(m -> m.get("EquipmentName").toString())));
            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, List<Map<String, Object>>> stringListMap = collect.get(FK_Pollutionid);
                for (String EquipmentName : stringListMap.keySet()) {
                    Map<String, Object> data = new HashMap<>();
                    List<Map<String, Object>> maps = stringListMap.get(EquipmentName);
                    Object pollutionname = maps.stream().filter(m -> m.get("pollutionname") != null).findFirst().orElse(new HashMap<>()).get("pollutionname");
                    Set<String> mns = maps.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
                    List<Document> alarmData = documents.stream().filter(m -> m.get("MN") != null && mns.contains(m.get("MN").toString())).collect(Collectors.toList());
                    //报警次数
                    int size = alarmData.size();
                    //时间点
                    String timepoint = alarmData.stream().filter(m -> m.get("FirstExceptionTime") != null && m.get("LastExceptionTime") != null).map(m -> m.get("FirstExceptionTime")
                            .toString() + "-" + m.get("LastExceptionTime").toString()).distinct().collect(Collectors.joining("、"));
                    data.put("pollutionname", pollutionname);
                    data.put("FK_Pollutionid", FK_Pollutionid);
                    data.put("EquipmentName", EquipmentName);
                    data.put("alarmcount", size);
                    data.put("timepoint", timepoint);
                    datalist.add(data);
                }
            }
            int size = datalist.size();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                datalist = datalist.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", size);
            resultMap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 下午 3:11
     * @Description: 通过监测时间查询治污设施异常信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getControlFacilityAlarmInfoByParams", method = RequestMethod.POST)
    public Object getControlFacilityAlarmInfoByParams(@RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            // 设施类型（1表示产污环节，2表示治污环节）
            paramMap.put("facilitytype", "2");
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);

            for (Document document : documents) {
                document.put("FirstExceptionTime", document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "HH:mm"));
                document.put("LastExceptionTime", document.get("LastExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("LastExceptionTime").toString(), "HH:mm"));
            }

            Map<String, Map<String, List<Map<String, Object>>>> collect = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null && m.get("EquipmentName") != null)
                    .collect(Collectors.groupingBy(m -> m.get("FK_Pollutionid").toString(), Collectors.groupingBy(m -> m.get("EquipmentName").toString())));
            for (String FK_Pollutionid : collect.keySet()) {
                Map<String, List<Map<String, Object>>> stringListMap = collect.get(FK_Pollutionid);
                for (String EquipmentName : stringListMap.keySet()) {
                    Map<String, Object> data = new HashMap<>();
                    List<Map<String, Object>> maps = stringListMap.get(EquipmentName);
                    Object pollutionname = maps.stream().filter(m -> m.get("pollutionname") != null).findFirst().orElse(new HashMap<>()).get("pollutionname");
                    Set<String> mns = maps.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
                    List<Document> alarmData = documents.stream().filter(m -> m.get("MN") != null && mns.contains(m.get("MN").toString())).collect(Collectors.toList());
                    //报警次数
                    int size = alarmData.size();
                    //时间点
                    String timepoint = alarmData.stream().filter(m -> m.get("FirstExceptionTime") != null && m.get("LastExceptionTime") != null).map(m -> m.get("FirstExceptionTime")
                            .toString() + "-" + m.get("LastExceptionTime").toString()).distinct().collect(Collectors.joining("、"));
                    data.put("pollutionname", pollutionname);
                    data.put("FK_Pollutionid", FK_Pollutionid);
                    data.put("EquipmentName", EquipmentName);
                    data.put("alarmcount", size);
                    data.put("timepoint", timepoint);
                    datalist.add(data);
                }
            }
            int size = datalist.size();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                datalist = datalist.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", size);
            resultMap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0023 下午 4:30
     * @Description: 通过多参数获取监测点异常信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, fkpollutionid, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getFacilityMonitorPointAlarmDetailByParams", method = RequestMethod.POST)
    public Object getFacilityMonitorPointAlarmDetailByParams(@RequestJson(value = "starttime") String starttime,
                                                             @RequestJson(value = "endtime") String endtime,
                                                             @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                             @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                             @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            // 设施类型（1表示产污环节，2表示治污环节）
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);

            for (Document document : documents) {
                document.put("FirstExceptionTime", document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "yyyy/MM/dd HH:mm"));
                document.put("LastExceptionTime", document.get("LastExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("LastExceptionTime").toString(), "yyyy/MM/dd HH:mm"));
            }

            for (Document document : documents) {
                Map<String, Object> data = new HashMap<>();
                String MN = document.get("MN") == null ? "" : document.get("MN").toString();
                String FirstExceptionTime = document.get("FirstExceptionTime") == null ? "" : document.get("FirstExceptionTime").toString();
                String LastExceptionTime = document.get("LastExceptionTime") == null ? "" : document.get("LastExceptionTime").toString();
                Map<String, Object> EquipmentMap = useElectricFacility.stream().filter(m -> m.get("MonitorPointName") != null && m.get("monitorid") != null && m.get("EquipmentName") != null).findFirst().orElse(new HashMap<>());

                data.put("dgimn", MN);
                data.put("FirstExceptionTime", FirstExceptionTime);
                data.put("LastExceptionTime", LastExceptionTime);
                data.put("MonitorPointName", EquipmentMap.get("MonitorPointName"));
                data.put("monitorid", EquipmentMap.get("monitorid"));
                data.put("equipmentname", EquipmentMap.get("EquipmentName"));

                datalist.add(data);
            }
            int size = datalist.size();
            if (pagenum != null && pagesize != null && pagenum > 0 && pagesize > 0) {
                datalist = datalist.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total", size);
            resultMap.put("datalist", datalist);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/23 0020 下午 4:46
     * @Description: 通过多参数获取用电设施每日用电量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, pollutantcode, fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getUseElectricFacilityInfoByParams", method = RequestMethod.POST)
    public Object getUseElectricFacilityInfoByParams(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "fkpollutionid") String fkpollutionid,
                                                     @RequestJson(value = "pollutantcode", required = false) String pollutantcode) {
        try {

            if (StringUtils.isBlank(pollutantcode)) {
                pollutantcode = powerUsageCode.getCode();
            }
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", "DayData");
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            // 设施类型（1表示产污环节，2表示治污环节）
            List<Integer> FacilityTypes = Arrays.asList(1, 2);

            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            for (Document document : monitorDataByParamMap) {
                document.put("MonitorTime", document.get("MonitorTime") == null ? "" : FormatUtils.formatCSTString(document.get("MonitorTime").toString(), "yyyy-MM-dd"));
            }

            Map<String, List<Document>> monitorTimeMap = monitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            String finalPollutantcode = pollutantcode;
            for (Integer facilityType : FacilityTypes) {
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> list = new ArrayList<>();
                Set<String> mns = useElectricFacility.stream().filter(m -> m.get("FacilityType") != null && m.get("dgimn") != null && facilityType == Integer.valueOf(m.get("FacilityType").toString())).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
                for (String MonitorTime : monitorTimeMap.keySet()) {
                    Map<String, Object> data = new HashMap<>();
                    List<Document> documents = monitorTimeMap.get(MonitorTime);
                    Double total = documents.stream().filter(m -> m.get("DataGatherCode") != null && m.get("DayDataList") != null && mns.contains(m.get("DataGatherCode").toString())).flatMap(m -> ((List<Map<String, Object>>) m.get("DayDataList")).stream())
                            .filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null && finalPollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.summingDouble(m -> Double.valueOf(m.get("CouStrength").toString())));
                    data.put("monitortime", MonitorTime);
                    data.put("total", total);
                    list.add(data);
                }
                result.put("facilityType", facilityType);
                result.put("datalist", list.stream().filter(m -> m.get("monitortime") != null).sorted(Comparator.comparing(m -> m.get("monitortime").toString())).collect(Collectors.toList()));
                resultList.add(result);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电设施下所有点位小时监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getFacilityMonitorChartData", method = RequestMethod.POST)
    public Object getFacilityMonitorChartData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "useelectricfacilityid") String useelectricfacilityid
    ) {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("useelectricfacilityid", useelectricfacilityid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            List<Map<String, Object>> resultList = setMonitorCharData(monitortime, pollutantcode, pointList);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电设施下所有点位小时监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getFacilityMonitorListData", method = RequestMethod.POST)
    public Object getFacilityMonitorListData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "useelectricfacilityid") String useelectricfacilityid,
            @RequestJson(value = "monitorpointids", required = false) List<String> monitorpointids,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();

            if (monitorpointids != null && monitorpointids.size() > 0) {
                paramMap.put("monitorpointids", monitorpointids);
            }
            paramMap.put("useelectricfacilityid", useelectricfacilityid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            Map<String, Object> resultMap = setMonitorListData(monitortime,
                    pollutantcode,
                    pointList,
                    pagesize,
                    pagenum);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电点位小时监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointMonitorListData", method = RequestMethod.POST)
    public Object getFacilityMonitorListData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            Map<String, Object> resultMap = setMonitorListData(monitortime,
                    pollutantcode,
                    pointList,
                    pagesize,
                    pagenum);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电设施某个点位小时监测数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointMonitorChartData", method = RequestMethod.POST)
    public Object getMonitorPointMonitorChartData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointid") String monitorpointid
    ) {
        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            List<Map<String, Object>> resultList = setMonitorCharData(monitortime, pollutantcode, pointList);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电设施下所有点位实时异常图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getFacilityExceptionCharData", method = RequestMethod.POST)
    public Object getFacilityExceptCharData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "useelectricfacilityid") String useelectricfacilityid
    ) {
        try {
            List<Map<String, Object>> resultList = new ArrayList<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("useelectricfacilityid", useelectricfacilityid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> cwAndZwDataList = useElectricFacilityMonitorPointService.getCWAndZWMonitorPointListByParam(paramMap);
            List<String> cwmns = new ArrayList<>();
            List<String> zwmns = new ArrayList<>();
            List<String> mns = new ArrayList<>();
            String mnCommon;
            String cwid = "";
            String zwid = "";
            for (Map<String, Object> point : cwAndZwDataList) {
                mnCommon = point.get("cwdgimn").toString();
                cwmns.add(mnCommon);
                cwid = point.get("cwid").toString();
                if (point.get("zwdgimn") != null) {
                    mnCommon = point.get("zwdgimn").toString();
                    zwmns.add(mnCommon);
                    zwid = point.get("zwid").toString();
                }
            }
            mns.addAll(cwmns);
            mns.addAll(zwmns);
            mns = mns.stream().distinct().collect(Collectors.toList());
            paramMap.put("mns", mns);
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("sort", "asc");
            paramMap.put("dataTypes", Arrays.asList(CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName()));
            List<Document> exceptionDataList = onlineService.getExceptionDetailDataByParamMap(paramMap);
            if (exceptionDataList.size() > 0) {
                String starttime;
                String enttime;
                List<Map<String, Object>> cwList = new ArrayList<>();
                List<Map<String, Object>> zwList = new ArrayList<>();
                for (Document document : exceptionDataList) {
                    mnCommon = document.getString("MN");
                    starttime = DataFormatUtil.getDateYMDHMS(document.getDate("FirstExceptionTime"));
                    enttime = DataFormatUtil.getDateYMDHMS(document.getDate("LastExceptionTime"));
                    Map<String, Object> timeMap = new HashMap<>();
                    timeMap.put("starttime", starttime);
                    timeMap.put("enttime", enttime);
                    if (cwmns.contains(mnCommon)) {
                        cwList.add(timeMap);
                    } else if (zwmns.contains(mnCommon)) {
                        zwList.add(timeMap);
                    }
                }
                if (cwList.size() > 0) {
                    cwList = cwList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("starttime").toString())).collect(Collectors.toList());
                }
                if (zwList.size() > 0) {
                    zwList = zwList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("starttime").toString())).collect(Collectors.toList());
                }
                Map<String, Object> cwMap = new HashMap<>();
                cwMap.put("useelectricfacilityid", cwid);
                cwMap.put("facilitytype", "产污设施");
                cwMap.put("exceptiontimes", cwList);
                resultList.add(cwMap);

                Map<String, Object> zwMap = new HashMap<>();
                zwMap.put("useelectricfacilityid", zwid);
                zwMap.put("facilitytype", "治污设施");
                zwMap.put("exceptiontimes", zwList);
                resultList.add(zwMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电设施下所有点位实时异常图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getFacilityExceptionCharDetailsData", method = RequestMethod.POST)
    public Object getFacilityExceptionCharDetailsData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "useelectricfacilityid") String useelectricfacilityid
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("useelectricfacilityid", useelectricfacilityid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            List<Map<String, Object>> resultList = getExceptionCharData(monitortime, pollutantcode, pointList);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/23 0023 下午 3:02
     * @Description: 获取异常图表数据集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> getExceptionCharData(String monitortime, String pollutantcode, List<Map<String, Object>> pointList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (pointList.size() > 0) {
            Map<String, Object> paramMap = new HashMap<>();

            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndPId = new HashMap<>();
            Map<String, Object> mnAndPName = new HashMap<>();
            String mnCommon;
            for (Map<String, Object> point : pointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndPId.put(mnCommon, point.get("monitorpointid").toString());
                mnAndPName.put(mnCommon, point.get("monitorpointname").toString());
            }
            mns = mns.stream().distinct().collect(Collectors.toList());
            paramMap.put("mns", mns);
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", db_RealTimeData);
            paramMap.put("sort", "asc");
            List<Document> monitorDataList = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, List<Map<String, Object>>> mnAndDataList = MongoDataUtils.setMnAndDataList(monitorDataList, pollutantcode, db_RealTimeData);
            List<Map<String, Object>> dataList;
            paramMap.put("dataTypes", Arrays.asList(CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName()));
            List<Document> exceptionDataList = onlineService.getExceptionDetailDataByParamMap(paramMap);
            Map<String, List<Map<String, Object>>> mnAndTimeList = new HashMap<>();
            List<Map<String, Object>> timeList;
            if (exceptionDataList.size() > 0) {
                Date startTime;
                Date endTime;
                Date time;
                String timeString;
                for (Document document : exceptionDataList) {
                    mnCommon = document.getString("MN");
                    startTime = document.getDate("FirstExceptionTime");
                    endTime = document.getDate("LastExceptionTime");
                    dataList = mnAndDataList.get(mnCommon);
                    if (dataList != null && dataList.size() > 0) {
                        for (Map<String, Object> dataMap : dataList) {
                            timeString = dataMap.get("monitortime").toString();
                            time = DataFormatUtil.getDateYMDHMS(timeString);
                            if (DataFormatUtil.belongCalendar(time, startTime, endTime)) {
                                dataMap.put("monitorvalue", null);
                            }
                        }
                    }
                    if (mnAndTimeList.containsKey(mnCommon)) {
                        timeList = mnAndTimeList.get(mnCommon);
                    } else {
                        timeList = new ArrayList<>();
                    }
                    Map<String, Object> timeMap = new HashMap<>();
                    timeMap.put("starttime", DataFormatUtil.getDateYMDHMS(startTime));
                    timeMap.put("enttime", DataFormatUtil.getDateYMDHMS(endTime));
                    timeList.add(timeMap);
                    mnAndTimeList.put(mnCommon, timeList);
                }
            }
            if (mnAndDataList.size() > 0) {
                for (String mnKey : mnAndDataList.keySet()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("monitorpointid", mnAndPId.get(mnKey));
                    resultMap.put("monitorpointname", mnAndPName.get(mnKey));
                    resultMap.put("monitordatalist", mnAndDataList.get(mnKey));
                    resultMap.put("exceptiontimes", mnAndTimeList.get(mnKey));
                    resultList.add(resultMap);
                }
            }
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2020/6/23 0023 下午 3:02
     * @Description: 组装异常图表数据集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> setExceptionListData(String monitortime,
                                                     String pollutantcode,
                                                     List<Map<String, Object>> pointList,
                                                     Integer pagesize,
                                                     Integer pagenum
    ) {

        Map<String, Object> resultMap = new HashMap<>();
        if (pointList.size() > 0) {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndPId = new HashMap<>();
            Map<String, Object> mnAndPName = new HashMap<>();
            Map<String, Object> mnAndEId = new HashMap<>();
            Map<String, Object> mnAndEName = new HashMap<>();
            String mnCommon;
            for (Map<String, Object> point : pointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndPId.put(mnCommon, point.get("monitorpointid").toString());
                mnAndPName.put(mnCommon, point.get("monitorpointname").toString());
                mnAndEId.put(mnCommon, point.get("equipmentid").toString());
                mnAndEName.put(mnCommon, point.get("equipmentname").toString());
            }
            mns = mns.stream().distinct().collect(Collectors.toList());
            paramMap.put("mns", mns);
            paramMap.put("pagesize", pagesize);
            paramMap.put("pagenum", pagenum);
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("sort", "asc");
            paramMap.put("dataTypes", Arrays.asList(CommonTypeEnum.MongodbDataTypeEnum.RealTimeDataEnum.getName()));
            List<Document> exceptionDataList = onlineService.getExceptionDetailDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (exceptionDataList.size() > 0) {
                for (Document document : exceptionDataList) {
                    mnCommon = document.getString("MN");
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("equipmentid", mnAndEId.get(mnCommon));
                    dataMap.put("equipmentname", mnAndEName.get(mnCommon));
                    dataMap.put("monitorpointid", mnAndPId.get(mnCommon));
                    dataMap.put("monitorpointname", mnAndPName.get(mnCommon));
                    dataMap.put("starttime", DataFormatUtil.getDateYMDHMS(document.getDate("FirstExceptionTime")));
                    dataMap.put("endtime", DataFormatUtil.getDateYMDHMS(document.getDate("LastExceptionTime")));
                    dataList.add(dataMap);
                }
            }
            resultMap = onlineService.getExceptionPageMap(paramMap);
            resultMap.put("datalist", dataList);
        }
        return resultMap;
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电点位实时异常图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointExceptionCharData", method = RequestMethod.POST)
    public Object getMonitorPointExceptionCharData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointid") String monitorpointid
    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            List<Map<String, Object>> resultList = getExceptionCharData(monitortime, pollutantcode, pointList);
            return AuthUtil.parseJsonKeyToLower("success", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: lip
     * @date: 2020/6/19 0019 下午 1:11
     * @Description: 获取用电点位实时异常图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getMonitorPointExceptionListData", method = RequestMethod.POST)
    public Object getMonitorPointExceptionListData(
            @RequestJson(value = "monitortime") String monitortime,
            @RequestJson(value = "pollutantcode") String pollutantcode,
            @RequestJson(value = "monitorpointid") String monitorpointid,
            @RequestJson(value = "pagesize", required = false) Integer pagesize,
            @RequestJson(value = "pagenum", required = false) Integer pagenum

    ) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("monitorpointid", monitorpointid);
            paramMap.put("monitorpointtype", ElectricFacilityEnum.getCode());
            List<Map<String, Object>> pointList = useElectricFacilityMonitorPointService.getOnlineMonitorPointListByParam(paramMap);
            Map<String, Object> resultMap = setExceptionListData(monitortime, pollutantcode, pointList, pagesize, pagenum);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/6/24 0024 下午 1:47
     * @Description: 通过多参数获取工况实时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getLastFacilityMonitorDataByParams", method = RequestMethod.POST)
    public Object getLastFacilityMonitorDataByParams(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                     @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                     @RequestJson(value = "equipmentname", required = false) String equipmentname,
                                                     @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                     @RequestJson(value = "facilitytype", required = false) String facilitytype) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> tableMap = new HashMap<>();
            Map<String, Object> countMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<String> pollutants = Arrays.asList(electricCode.getCode(), voltageCode.getCode());
            paramMap.put("pollutanttype", ElectricFacilityEnum.getCode());
            paramMap.put("excludepollutantcodes", Arrays.asList(powerCode.getCode()));
            List<Map<String, Object>> dataList = gasOutPutPollutantSetService.getGasOutPutPollutantSetsByOutputIds(paramMap);

            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("equipmentname", equipmentname);
            paramMap.put("monitorpointname", monitorpointname);
            paramMap.put("facilitytype ", facilitytype);
            //用电设施监测点信息
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);

            //企业个数
            long countpollution = useElectricFacility.stream().filter(m -> m.get("FK_Pollutionid") != null).map(m -> m.get("FK_Pollutionid").toString()).distinct().count();
            //设备个数
            long countequipment = useElectricFacility.stream().filter(m -> m.get("PK_ID") != null).map(m -> m.get("PK_ID").toString()).distinct().count();
            //监测点个数
            long countmonitor = useElectricFacility.stream().filter(m -> m.get("monitorid") != null).map(m -> m.get("monitorid").toString()).distinct().count();
            //在线个数
            long countonline = useElectricFacility.stream().filter(m -> m.get("status") != null && "0".equals(m.get("status").toString())).count();
            //离线个数
            long countoffline = useElectricFacility.stream().filter(m -> m.get("status") != null && "1".equals(m.get("status").toString())).count();
            //异常个数
            long countexception = useElectricFacility.stream().filter(m -> m.get("status") != null && "3".equals(m.get("status").toString())).count();
            countMap.put("pollutioncount", countpollution);
            countMap.put("equipmentcount", countequipment);
            countMap.put("outputcount", countmonitor);
            countMap.put("offlinecount", countoffline);
            countMap.put("normalcount", countonline);
            countMap.put("exceptioncount", countexception);


            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", "LatestData");
            paramMap.put("type", "RealTimeData");
            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            for (Map<String, Object> stringObjectMap : useElectricFacility) {
                String dgimn = stringObjectMap.get("dgimn") == null ? "" : stringObjectMap.get("dgimn").toString();
                String status = stringObjectMap.get("status") == null ? "" : stringObjectMap.get("status").toString();
                String nameByCode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(status);
                stringObjectMap.put("onlinestatus", status);
                stringObjectMap.put("status", nameByCode);
                monitorDataByParamMap.stream().filter(map -> map.get("DataGatherCode") != null && dgimn.equals(map.get("DataGatherCode").toString()) && map.get("DataList") != null)
                        .peek(m -> stringObjectMap.put("monitortime", FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss")))
                        .flatMap(m -> ((List<Map<String, Object>>) m.get("DataList")).stream()).filter(m -> m.get("PollutantCode") != null && pollutants.contains(m.get("PollutantCode").toString())).forEach(m -> {
                    Map<String, Object> data = new HashMap<>();
                    String PollutantCode = m.get("PollutantCode").toString();
                    String value = m.get("AvgStrength") == null ? "-" : decimalFormat.format(Double.valueOf(m.get("AvgStrength").toString()));
                    data.put("value", value);
                    data.put("isover", m.get("IsOver"));
                    data.put("isexception", m.get("IsException"));
                    stringObjectMap.put(PollutantCode, data);
                });
            }


            tableMap.put("tablelistdata", useElectricFacility);
            tableMap.put("tabletitledata", dataList);
            resultMap.put("datacount", countMap);
            resultMap.put("tabledata", tableMap);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/6/28 0028 上午 8:58
     * @Description: 通过企业id获取工况最新数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "getLastFacilityMonitorDataByPollutionid", method = RequestMethod.POST)
    public Object getLastFacilityMonitorDataByPollutionid(@RequestJson(value = "fkpollutionid") String fkpollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<String> pollutants = Arrays.asList(electricCode.getCode(), voltageCode.getCode());
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", "LatestData");
            paramMap.put("type", "RealTimeData");
            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            for (Map<String, Object> stringObjectMap : useElectricFacility) {
                String dgimn = stringObjectMap.get("dgimn") == null ? "" : stringObjectMap.get("dgimn").toString();
                String status = stringObjectMap.get("status") == null ? "" : stringObjectMap.get("status").toString();
                String nameByCode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(status);
                stringObjectMap.put("status", nameByCode);

                monitorDataByParamMap.stream().filter(map -> map.get("DataGatherCode") != null && dgimn.equals(map.get("DataGatherCode").toString()) && map.get("DataList") != null)
                        .peek(m -> stringObjectMap.put("monitortime", FormatUtils.formatCSTString(m.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss")))
                        .flatMap(m -> ((List<Map<String, Object>>) m.get("DataList")).stream()).filter(m -> m.get("PollutantCode") != null && pollutants.contains(m.get("PollutantCode").toString())).forEach(m -> {
                    Map<String, Object> data = new HashMap<>();
                    String PollutantCode = m.get("PollutantCode").toString();
                    String value = m.get("AvgStrength") == null ? "-" : decimalFormat.format(Double.valueOf(m.get("AvgStrength").toString()));
                    data.put("value", value);
                    data.put("isover", m.get("IsOver"));
                    data.put("isexception", m.get("IsException"));
                    stringObjectMap.put(PollutantCode, data);
                });
            }
            return AuthUtil.parseJsonKeyToLower("success", useElectricFacility);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/3 0003 下午 4:33
     * @Description: 统计企业工况实时信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionname, isstop, fkindustrytype, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "countPollutionAndFacilityInfoParams", method = RequestMethod.POST)
    public Object countPollutionAndFacilityInfoParams(@RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                      @RequestJson(value = "isstop", required = false) Boolean isstop,
                                                      @RequestJson(value = "fkindustrytype", required = false) String fkindustrytype,
                                                      @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                      @RequestJson(value = "pagenum", required = false) Integer pagenum) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> countMap = new HashMap<>();
            paramMap.put("isstop", isstop);
            paramMap.put("fkindustrytype", fkindustrytype);
            paramMap.put("pollutionname", pollutionname);

            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getPollutionAndFacilityInfoParams(paramMap);
            resultMap.put("total", useElectricFacility.size());

            //企业在线
            long pollutiononline = useElectricFacility.stream().filter(m -> m.get("fkpollutionid") != null && m.get("equipments") != null).collect(Collectors.groupingBy(m -> m.get("fkpollutionid").toString(),
                    Collectors.collectingAndThen(Collectors.toList(), n -> n.stream().flatMap(m -> ((List<Map<String, Object>>) m.get("equipments")).stream()).filter(m -> m.get("monitorpoints") != null)
                            .flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null && !OfflineStatusEnum.getCode().equals(m.get("status").toString()))
                            .count()))).values().stream().filter(m -> m > 0).count();
            //企业离线
            long pollutionoffline = useElectricFacility.stream().filter(m -> m.get("fkpollutionid") != null && m.get("equipments") != null).collect(Collectors.groupingBy(m -> m.get("fkpollutionid").toString(),
                    Collectors.collectingAndThen(Collectors.toList(), n -> n.stream().flatMap(m -> ((List<Map<String, Object>>) m.get("equipments")).stream()).filter(m -> m.get("monitorpoints") != null)
                            .flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null && OfflineStatusEnum.getCode().equals(m.get("status").toString()))
                            .count()))).values().stream().filter(m -> m > 0).count();
            //设备在线
            long equipmentonline = useElectricFacility.stream().filter(m -> m.get("equipments") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("equipments")).stream()).filter(m -> m.get("equipmentid") != null)
                    .collect(Collectors.groupingBy(m -> m.get("equipmentid").toString(), Collectors.collectingAndThen(Collectors.toList(), n -> n.stream()
                            .filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null &&
                                    !OfflineStatusEnum.getCode().equals(m.get("status").toString())).count()))).values().stream().filter(m -> m > 0).count();
            //设备离线
            long equipmentoffline = useElectricFacility.stream().filter(m -> m.get("equipments") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("equipments")).stream()).filter(m -> m.get("equipmentid") != null)
                    .collect(Collectors.groupingBy(m -> m.get("equipmentid").toString(), Collectors.collectingAndThen(Collectors.toList(), n -> n.stream()
                            .filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null &&
                                    OfflineStatusEnum.getCode().equals(m.get("status").toString())).count()))).values().stream().filter(m -> m > 0).count();
            //设备异常
            long equipmentalarm = useElectricFacility.stream().filter(m -> m.get("equipments") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("equipments")).stream()).filter(m -> m.get("equipmentid") != null)
                    .collect(Collectors.groupingBy(m -> m.get("equipmentid").toString(), Collectors.collectingAndThen(Collectors.toList(), n -> n.stream()
                            .filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null &&
                                    ExceptionStatusEnum.getCode().equals(m.get("status").toString())).count()))).values().stream().filter(m -> m > 0).count();

            for (Map<String, Object> stringObjectMap : useElectricFacility) {
                List<Map<String, Object>> equipments = (List<Map<String, Object>>) stringObjectMap.remove("equipments");
                //1表示产污环节，2表示治污环节
                long productcount = equipments.stream().filter(m -> m.get("facilitytype") != null && "1".equals(m.get("facilitytype").toString())).count();//产污个数
                long controlcount = equipments.stream().filter(m -> m.get("facilitytype") != null && "2".equals(m.get("facilitytype").toString())).count();//治污个数

                //运行监测点个数
                long monitgoronline = equipments.stream().filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null
                        && !OfflineStatusEnum.getCode().equals(m.get("status").toString())).count();
                //离线监测点个数
                long monitoroffline = equipments.stream().filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null
                        && OfflineStatusEnum.getCode().equals(m.get("status").toString())).count();
                //异常监测点个数
                long monitoralarm = equipments.stream().filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null
                        && ExceptionStatusEnum.getCode().equals(m.get("status").toString())).count();

                long status = equipments.stream().filter(m -> m.get("monitorpoints") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitorpoints")).stream()).filter(m -> m.get("status") != null
                ).map(m -> Integer.valueOf(m.get("status").toString())).max(Integer::compareTo).orElse(-1);
                String nameByCode = CommonTypeEnum.OnlineStatusEnum.getNameByCode(status + "");


                stringObjectMap.put("productcount", productcount);
                stringObjectMap.put("controlcount", controlcount);
                stringObjectMap.put("monitgoronline", monitgoronline);
                stringObjectMap.put("monitoroffline", monitoroffline);
                stringObjectMap.put("monitoralarm", monitoralarm);
                stringObjectMap.put("status", nameByCode);
            }


            countMap.put("pollutiononline", pollutiononline);
            countMap.put("pollutionoffline", pollutionoffline);
            countMap.put("equipmentonline", equipmentonline);
            countMap.put("equipmentoffline", equipmentoffline);
            countMap.put("equipmentalarm", equipmentalarm);

            if (pagenum != null && pagesize != null) {
                useElectricFacility = useElectricFacility.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("countmap", countMap);
            resultMap.put("datalist", useElectricFacility);


            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/6 0006 下午 2:18
     * @Description: 通过多参数获取企业小时数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, starttime, endtime, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "getPollutionRealDataByParams", method = RequestMethod.POST)
    public Object getPollutionRealDataByParams(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                               @RequestJson(value = "fkuseelectricfacilityid", required = false) String fkuseelectricfacilityid,
                                               @RequestJson(value = "starttime") String starttime,
                                               @RequestJson(value = "endtime") String endtime,
                                               @RequestJson(value = "monitorpointids", required = false) Object monitorpointids,
                                               @RequestJson(value = "pollutantcode", required = false) String pollutantcode) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("fkuseelectricfacilityid", fkuseelectricfacilityid);
            paramMap.put("monitorpointids", monitorpointids);
            //用电设施监测点信息
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);

            if (StringUtils.isBlank(pollutantcode)) {
                pollutantcode = powerUsageCode.getCode();
            }

            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", db_hourData);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            //查询小时在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            for (Document document : monitorDataByParamMap) {
                document.put("MonitorTime", document.get("MonitorTime") == null ? "" : FormatUtils.formatCSTString(document.get("MonitorTime").toString(), "yyyy-MM-dd HH"));
            }
            String finalPollutantcode = pollutantcode;
            Map<String, Double> collect = monitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString(), TreeMap::new, Collectors.collectingAndThen(Collectors.toList(),
                    m -> m.stream().filter(n -> n.get("HourDataList") != null).flatMap(n -> ((List<Map<String, Object>>) n.get("HourDataList")).stream()).filter(n -> n.get("PollutantCode") != null && n.get("CouStrength") != null
                            && finalPollutantcode.equals(n.get("PollutantCode").toString())).collect(Collectors.summingDouble(n -> Double.valueOf(n.get("CouStrength").toString()))))));

            for (String monitortime : collect.keySet()) {

                Map<String, Object> data = new HashMap<>();
                data.put("monitortime", monitortime);
                data.put("value", collect.get(monitortime));

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
     * @date: 2020/7/6 0006 下午 2:30
     * @Description: 通过监测时间查询企业异常信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getPollutionAlarmInfoByParams", method = RequestMethod.POST)
    public Object getPollutionAlarmInfoByParams(@RequestJson(value = "starttime") String starttime,
                                                @RequestJson(value = "endtime") String endtime,
                                                @RequestJson(value = "fkpollutionid") String fkpollutionid) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            Object pollutionRealDataByParams = getPollutionRealDataByParams(fkpollutionid, null, starttime, endtime, new ArrayList<>(), null);
            Object realdata = JSONObject.fromObject(pollutionRealDataByParams).get("data");

            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);

            for (Document document : documents) {
                Map<String, Object> data = new HashMap<>();
                data.put("FirstExceptionTime", document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "yyyy-MM-dd HH:mm:ss"));
                data.put("LastExceptionTime", document.get("LastExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("LastExceptionTime").toString(), "yyyy-MM-dd HH:mm:ss"));
                datalist.add(data);
            }
            List<Map<String, Object>> exceptiondata = datalist.stream().filter(m -> m.get("FirstExceptionTime") != null).sorted(Comparator.comparing(m -> m.get("FirstExceptionTime").toString())).collect(Collectors.toList());
            resultMap.put("realdata", realdata);
            resultMap.put("exceptiondata", exceptiondata);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/6 0006 下午 3:09
     * @Description: 统计企业用电量同比环比数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, fkuseelectricfacilityid, starttime, endtime, monitorpointids, pollutantcode]
     * @throws:
     */
    @RequestMapping(value = "countPollutionPowerUsedDataByParams", method = RequestMethod.POST)
    public Object countPollutionPowerUsedDataByParams(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                      @RequestJson(value = "fkuseelectricfacilityid", required = false) String fkuseelectricfacilityid,
                                                      @RequestJson(value = "starttime") String starttime,
                                                      @RequestJson(value = "endtime") String endtime,
                                                      @RequestJson(value = "monitorpointids", required = false) Object monitorpointids,
                                                      @RequestJson(value = "datetype") String datetype,
                                                      @RequestJson(value = "datatype") String datatype,
                                                      @RequestJson(value = "pollutantcode", required = false) String pollutantcode) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            String format = JSONObjectUtil.getFormat(starttime.length());
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("fkuseelectricfacilityid", fkuseelectricfacilityid);
            paramMap.put("monitorpointids", monitorpointids);
            //用电设施监测点信息
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            if (StringUtils.isBlank(pollutantcode)) {
                pollutantcode = powerUsageCode.getCode();
            }
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            //默认为日查询类型
            String pattern = "yyyy-MM-dd HH";
            String collection = db_hourData;
            String liststr = "HourDataList";
            if ("month".equals(datetype)) {
                pattern = "yyyy-MM-dd";
                collection = db_dayData;
                liststr = "DayDataList";
            } else if ("year".equals(datetype)) {
                pattern = "yyyy-MM";
                collection = db_monthData;
                liststr = "MonthDataList";
            }
            paramMap.put("collection", collection);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            //查询在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            if ("yearonyear".equals(datatype)) {
                //同比
                Calendar instance = Calendar.getInstance();
                instance.setTime(dateFormat.parse(starttime));
                instance.add(Calendar.YEAR, -1);
                Date time = instance.getTime();
                instance.clear();
                instance.setTime(dateFormat.parse(endtime));
                instance.add(Calendar.YEAR, -1);
                Date time1 = instance.getTime();
                paramMap.put("starttime", JSONObjectUtil.getStartTime(dateFormat.format(time)));
                paramMap.put("endtime", JSONObjectUtil.getEndTime(dateFormat.format(time1)));
            } else if ("monthonmonth".equals(datatype)) {
                //环比
                Calendar instance = Calendar.getInstance();
                instance.setTime(dateFormat.parse(starttime));
                instance.add(Calendar.MONTH, -1);
                Date time = instance.getTime();
                instance.clear();
                instance.setTime(dateFormat.parse(endtime));
                instance.add(Calendar.MONTH, -1);
                Date time1 = instance.getTime();
                paramMap.put("starttime", JSONObjectUtil.getStartTime(dateFormat.format(time)));
                paramMap.put("endtime", JSONObjectUtil.getEndTime(dateFormat.format(time1)));
            }



            String code = pollutantcode;
            //查询在线数据
            List<Document> lastMonitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
            Map<String, List<Document>> collect1 = lastMonitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));


            for (String monitortime : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                resultList.add(data);
                List<Document> documents = collect.get(monitortime);
                String monitorTime = FormatUtils.formatCSTString(monitortime, pattern);
                Double CouStrength = 0d;
                for (Document document:documents){
                    List<Document> pollutantlist = (List<Document>) document.get(liststr);
                    for (Document podoc:pollutantlist){
                        if (code.equals(podoc.getString("PollutantCode"))){
                            CouStrength+=(podoc.get("CouStrength")!=null?Double.valueOf(podoc.get("CouStrength").toString()):0d);
                            break;
                        }
                    }
                }
               /* Double CouStrength = documents.stream().filter(m -> m.get(liststr) != null).flatMap(m -> ((List<Map<String, Object>>) m.get(liststr)).stream())
                        .filter(m -> m.get("PollutantCode") != null && m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString()) && m.get("CouStrength") != null)
                        .map(m -> Double.valueOf(m.get("CouStrength").toString())).collect(Collectors.summingDouble(m -> m));*/
                data.put("monitorTime", monitorTime);
                data.put("thisCouStrength", decimalFormat.format(CouStrength));
                for (String lastmonitortime : collect1.keySet()) {
                    String point = "";
                    String lastpoint = "";
                    if ("day".equals(datetype)) {
                        point = FormatUtils.formatCSTString(monitortime, "HH");
                        lastpoint = FormatUtils.formatCSTString(lastmonitortime, "HH");

                    } else if ("month".equals(datetype)) {
                        point = FormatUtils.formatCSTString(monitortime, "dd");
                        lastpoint = FormatUtils.formatCSTString(lastmonitortime, "dd");

                    } else if ("year".equals(datetype)) {
                        point = FormatUtils.formatCSTString(monitortime, "MM");
                        lastpoint = FormatUtils.formatCSTString(lastmonitortime, "MM");

                    }
                    if (point.equals(lastpoint)) {
                        List<Document> documents1 = collect1.get(lastmonitortime);
                        Double lastCouStrength = 0d;
                        for (Document document:documents1){
                            List<Document> pollutantlist1 = (List<Document>) document.get(liststr);
                            for (Document podoc:pollutantlist1){
                                if (code.equals(podoc.getString("PollutantCode"))){
                                    lastCouStrength+=(podoc.get("CouStrength")!=null?Double.valueOf(podoc.get("CouStrength").toString()):0d);
                                    break;
                                }
                            }
                        }
                       /* Double lastCouStrength = documents1.stream().filter(m -> m.get(liststr) != null).flatMap(m -> ((List<Map<String, Object>>) m.get(liststr)).stream())
                                .filter(m -> m.get("PollutantCode") != null && m.get("PollutantCode") != null && code.equals(m.get("PollutantCode").toString()) && m.get("CouStrength") != null)
                                .map(m -> Double.valueOf(m.get("CouStrength").toString())).collect(Collectors.summingDouble(m -> m));*/
                        data.put("lastCouStrength", decimalFormat.format(lastCouStrength));
                        data.put("proportion", OnlineGasController.getYearOnYear(Float.valueOf(decimalFormat.format(lastCouStrength)), Float.valueOf(decimalFormat.format(CouStrength)), ""));
                    }
                }
            }

            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("monitorTime") != null).sorted(Comparator.comparing(m -> m.get("monitorTime").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/6 0006 下午 3:28
     * @Description: 获取各用电设施用电量排行信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getEveryUseElectricFacilityRanking", method = RequestMethod.POST)
    public Object getEveryUseElectricFacilityRanking(@RequestJson(value = "starttime") String starttime,
                                                     @RequestJson(value = "endtime") String endtime,
                                                     @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid) {
        try {
            String pollutantcode = powerUsageCode.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", "DayData");
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);


            Map<String, List<Map<String, Object>>> collect = useElectricFacility.stream().filter(m -> m.get("PK_ID") != null).collect(Collectors.groupingBy(m -> m.get("PK_ID").toString()));

            for (String PK_ID : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> maps = collect.get(PK_ID);//PK_ID用电设施id
                Set<String> mns = maps.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
                Map<String, Object> stringObjectMap = maps.stream().findFirst().orElse(new HashMap<>());

                data.put("EquipmentName", stringObjectMap.get("EquipmentName"));
                data.put("Equipmentid", stringObjectMap.get("PK_ID"));

                //污染物总量CouStrength
                Double total = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("DayDataList") != null && mns.contains(m.get("DataGatherCode").toString())).flatMap(m -> ((List<Map<String, Object>>) m.get("DayDataList")).stream())
                        .filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null && pollutantcode.equals(m.get("PollutantCode").toString())).collect(Collectors.summingDouble(m -> Double.valueOf(m.get("CouStrength").toString())));
                data.put("total", total);
                if (total > 0) {
                    resultList.add(data);
                }
            }
            return AuthUtil.parseJsonKeyToLower("success", resultList.stream().filter(m -> m.get("total") != null).sorted(Comparator.comparing(m -> ((Map<String, Object>) m).get("total").toString()).reversed()).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/8 0008 下午 3:41
     * @Description: 通过多参数获取用电设备用电量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, fkuseelectricfacilityid, starttime, endtime, datetype, monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "countFacilityPowerUsageListData", method = RequestMethod.POST)
    public Object countFacilityPowerUsageListData(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                  @RequestJson(value = "fkuseelectricfacilityid", required = false) String fkuseelectricfacilityid,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  @RequestJson(value = "datetype") String datetype,
                                                  @RequestJson(value = "monitorpointid", required = false) String monitorpointid) {
        try {
            String pollutantcode = powerUsageCode.getCode();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("fkuseelectricfacilityid", fkuseelectricfacilityid);
            paramMap.put("monitorpointid", monitorpointid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", db_hourData);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //默认查询日类型
            String pattern = "HH时";
            if ("month".equals(datetype)) {
                pattern = "dd日";
            } else if ("year".equals(datetype)) {
                pattern = "MM月";
            }

            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            for (Document document : monitorDataByParamMap) {
                document.put("MonitorTime", document.get("MonitorTime") == null ? "" : FormatUtils.formatCSTString(document.get("MonitorTime").toString(), pattern));
            }


            Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String dgimn : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Document> documents = collect.get(dgimn);
                Map<String, List<Document>> collect1 = documents.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
                Double total = 0d;
                List<Map<String, Object>> monitordata = new ArrayList<>();
                for (String MonitorTime : collect1.keySet()) {
                    Map<String, Object> monitormap = new HashMap<>();
                    List<Document> documents1 = collect1.get(MonitorTime);
                    Double collect2 = documents1.stream().filter(m -> m.get("HourDataList") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("HourDataList")).stream()).filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null
                            && pollutantcode.equals(m.get("PollutantCode").toString())).map(m -> Double.valueOf(m.get("CouStrength").toString())).collect(Collectors.summingDouble(m -> m));
                    total += collect2;
                    monitormap.put("monitorpoint", MonitorTime);
                    monitormap.put("value", decimalFormat.format(collect2));
                    monitordata.add(monitormap);
                }

                Map<String, Object> stringObjectMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).findFirst().orElse(new HashMap<>());

                data.put("total", total);
                data.put("monitordata", monitordata.stream().filter(m -> m.get("monitorpoint") != null).sorted(Comparator.comparing(m -> m.get("monitorpoint").toString())).collect(Collectors.toList()));
                data.put("MonitorPointName", stringObjectMap.get("MonitorPointName"));
                resultList.add(data);
            }

            List<String> collect1 = resultList.stream().filter(m -> m.get("monitordata") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("monitordata")).stream()).filter(m -> m.get("monitorpoint") != null)
                    .map(m -> m.get("monitorpoint").toString()).distinct().sorted(String::compareTo).collect(Collectors.toList());


            resultMap.put("titledata", collect1);
            resultMap.put("tabledata", resultList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/16 0016 下午 1:36
     * @Description: 通过多参数获取用电设备用电量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, fkuseelectricfacilityid, starttime, endtime, datetype, monitorpointid]
     * @throws:
     */
    @RequestMapping(value = "countFacilityPowerUsageData", method = RequestMethod.POST)
    public Object countFacilityPowerUsageData(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                              @RequestJson(value = "fkuseelectricfacilityid", required = false) String fkuseelectricfacilityid,
                                              @RequestJson(value = "starttime") String starttime,
                                              @RequestJson(value = "endtime") String endtime,
                                              @RequestJson(value = "datetype") String datetype,
                                              @RequestJson(value = "monitorpointid", required = false) String monitorpointid) {
        try {
            String pollutantcode = powerUsageCode.getCode();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("fkuseelectricfacilityid", fkuseelectricfacilityid);
            paramMap.put("monitorpointid", monitorpointid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", db_hourData);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //默认查询日类型
            String pattern = "HH时";
            if ("month".equals(datetype)) {
                pattern = "dd日";
            } else if ("year".equals(datetype)) {
                pattern = "MM月";
            }

            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);

            for (Document document : monitorDataByParamMap) {
                document.put("MonitorTime", document.get("MonitorTime") == null ? "" : FormatUtils.formatCSTString(document.get("MonitorTime").toString(), pattern));
            }


            Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String dgimn : collect.keySet()) {
                List<Document> documents = collect.get(dgimn);
                Map<String, List<Document>> collect1 = documents.stream().filter(m -> m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("MonitorTime").toString()));
                Double total = 0d;
                Map<String, Object> monitormap = new HashMap<>();
                for (String MonitorTime : collect1.keySet()) {
                    List<Document> documents1 = collect1.get(MonitorTime);
                    Double collect2 = documents1.stream().filter(m -> m.get("HourDataList") != null).flatMap(m -> ((List<Map<String, Object>>) m.get("HourDataList")).stream()).filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null
                            && pollutantcode.equals(m.get("PollutantCode").toString())).map(m -> Double.valueOf(m.get("CouStrength").toString())).collect(Collectors.summingDouble(m -> m));
                    total += collect2;
                    monitormap.put(MonitorTime, decimalFormat.format(collect2));
                }
                Map<String, Object> stringObjectMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && dgimn.equals(m.get("dgimn").toString())).findFirst().orElse(new HashMap<>());
                monitormap.put("total", total);
                monitormap.put("MonitorPointName", stringObjectMap.get("MonitorPointName"));
                resultList.add(monitormap);
            }
            resultMap.put("tabledata", resultList);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: chengzq
     * @date: 2020/7/9 0009 上午 8:45
     * @Description: 通过多参数导出用电设备用电量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid, fkuseelectricfacilityid, starttime, endtime, datetype, monitorpointid, response, request]
     * @throws:
     */
    @RequestMapping(value = "ExportFacilityPowerUsageListData", method = RequestMethod.POST)
    public void ExportFacilityPowerUsageListData(@RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                 @RequestJson(value = "fkuseelectricfacilityid", required = false) String fkuseelectricfacilityid,
                                                 @RequestJson(value = "starttime") String starttime,
                                                 @RequestJson(value = "endtime") String endtime,
                                                 @RequestJson(value = "datetype") String datetype,
                                                 @RequestJson(value = "monitorpointid", required = false) String monitorpointid,
                                                 HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("监测点名称");
            headers.add("总计");
            headersField.add("monitorpointname");
            headersField.add("total");


            Object object = countFacilityPowerUsageListData(fkpollutionid, fkuseelectricfacilityid, starttime, endtime, datetype, monitorpointid);
            JSONObject jsonObject = JSONObject.fromObject(object);
            Object data1 = jsonObject.get("data");
            JSONObject jsonObject2 = JSONObject.fromObject(data1);
            Object jsonobject = jsonObject2.get("tabledata");
            JSONArray tabledata = JSONArray.fromObject(jsonobject);


            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Object tabledatum : tabledata) {
                Map<String, Object> datamap = new HashMap<>();
                JSONObject jsonObject1 = JSONObject.fromObject(tabledatum);
                Object monitordata = jsonObject1.get("monitordata") == null ? new HashMap<>() : jsonObject1.get("monitordata");
                JSONArray jsonArray = JSONArray.fromObject(monitordata);
                datamap.put("total", jsonObject1.get("total"));
                datamap.put("monitorpointname", jsonObject1.get("monitorpointname"));
                for (Object o : jsonArray) {
                    Map<String, Object> data = (Map<String, Object>) o;
                    datamap.put(data.get("monitorpoint") == null ? "" : data.get("monitorpoint").toString(), data.get("value"));
                    headers.add(data.get("monitorpoint") == null ? "" : data.get("monitorpoint").toString());
                    headersField.add(data.get("monitorpoint") == null ? "" : data.get("monitorpoint").toString());
                }
                resultList.add(datamap);
            }

            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers.stream().distinct().collect(Collectors.toList()),
                    headersField.stream().distinct().collect(Collectors.toList()), resultList, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("用电设施用电量统计", response, request, bytesForWorkBook);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/8 0008 下午 4:42
     * @Description: 统计企业用电量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionids, starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "countPollutionPowerUsageListData", method = RequestMethod.POST)
    public Object countPollutionPowerUsageListData(@RequestJson(value = "fkpollutionids", required = false) Object fkpollutionids,
                                                   @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                   @RequestJson(value = "pagenum", required = false) Integer pagenum,
                                                   @RequestJson(value = "starttime") String starttime,
                                                   @RequestJson(value = "endtime") String endtime) {
        try {
            String pollutantcode = powerUsageCode.getCode();
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<Map<String, Object>> resultList = new ArrayList<>();
            paramMap.put("fkpollutionids", fkpollutionids);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("mns", dgimns);
            paramMap.put("collection", db_hourData);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            //查询日在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);


            Map<String, List<Map<String, Object>>> collect = useElectricFacility.stream().filter(m -> m.get("pollutionname") != null).collect(Collectors.groupingBy(m -> m.get("pollutionname").toString()));


            for (String pollutionname : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> maps = collect.get(pollutionname);
                //（1表示产污环节，2表示治污环节）
                Set<String> productmns = maps.stream().filter(m -> m.get("FacilityType") != null && m.get("dgimn") != null && "1".equals(m.get("FacilityType").toString())).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());
                Set<String> controlmns = maps.stream().filter(m -> m.get("FacilityType") != null && m.get("dgimn") != null && "2".equals(m.get("FacilityType").toString())).map(m -> m.get("dgimn").toString()).collect(Collectors.toSet());

                List<Double> product = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("HourDataList") != null && productmns.contains(m.get("DataGatherCode").toString()))
                        .flatMap(m -> ((List<Map<String, Object>>) m.get("HourDataList")).stream()).filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null && pollutantcode
                                .equals(m.get("PollutantCode").toString())).map(m -> Double.valueOf(m.get("CouStrength").toString())).collect(Collectors.toList());
                List<Double> control = monitorDataByParamMap.stream().filter(m -> m.get("DataGatherCode") != null && m.get("HourDataList") != null && controlmns.contains(m.get("DataGatherCode").toString()))
                        .flatMap(m -> ((List<Map<String, Object>>) m.get("HourDataList")).stream()).filter(m -> m.get("PollutantCode") != null && m.get("CouStrength") != null && pollutantcode
                                .equals(m.get("PollutantCode").toString())).map(m -> Double.valueOf(m.get("CouStrength").toString())).collect(Collectors.toList());

                //产污用电总量
                Double producttotal = product.stream().collect(Collectors.summingDouble(m -> m));
                //治污用电总量
                Double controltotal = control.stream().collect(Collectors.summingDouble(m -> m));

                product.addAll(control);

                //企业用电总量
                Double pollutiontotal = product.stream().collect(Collectors.summingDouble(m -> m));
                //企业日平均用电
                Double pollutionavg = product.stream().collect(Collectors.averagingDouble(m -> m));


                data.put("producttotal", decimalFormat.format(producttotal));
                data.put("controltotal", decimalFormat.format(controltotal));
                data.put("pollutiontotal", decimalFormat.format(pollutiontotal));
                data.put("pollutionavg", decimalFormat.format(pollutionavg));
                data.put("pollutionname", pollutionname);
                //产污/治污（用电比%）
                data.put("powerratio", OnlineGasController.getYearOnYear(Float.valueOf(decimalFormat.format(controltotal)), Float.valueOf(decimalFormat.format(producttotal)), "-"));
                //治污/全厂（电量比%）
                data.put("electricityratio", OnlineGasController.getYearOnYear(Float.valueOf(decimalFormat.format(pollutiontotal)), Float.valueOf(decimalFormat.format(controltotal)), "-"));

                resultList.add(data);
            }

            int size = resultList.size();
            if (pagenum != null && pagesize != null) {
                resultList = resultList.stream().skip((pagenum - 1) * pagesize).limit(pagesize).collect(Collectors.toList());
            }

            resultMap.put("total", size);
            resultMap.put("datalist", resultList);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/9 0009 上午 9:02
     * @Description: 导出企业用电量信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionids, starttime, endtime, response, request]
     * @throws:
     */
    @RequestMapping(value = "ExportPollutionPowerUsageListData", method = RequestMethod.POST)
    public void ExportPollutionPowerUsageListData(@RequestJson(value = "fkpollutionids", required = false) Object fkpollutionids,
                                                  @RequestJson(value = "starttime") String starttime,
                                                  @RequestJson(value = "endtime") String endtime,
                                                  HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            List<String> headers = new ArrayList<>();
            List<String> headersField = new ArrayList<>();
            headers.add("企业名称");
            headers.add("用电总计");
            headers.add("日平均用电");
            headers.add("生产用电总计");
            headers.add("治污用电总计");
            headers.add("产污/治污（用电比%）");
            headers.add("治污/全厂（电量比%）");
            headersField.add("pollutionname");
            headersField.add("pollutiontotal");
            headersField.add("pollutionavg");
            headersField.add("producttotal");
            headersField.add("controltotal");
            headersField.add("powerratio");
            headersField.add("electricityratio");


            Object object = countPollutionPowerUsageListData(fkpollutionids, Integer.MAX_VALUE, 1, starttime, endtime);
            JSONObject jsonObject = JSONObject.fromObject(object);
            Object jsonobject = jsonObject.get("data");
            JSONObject jsonObject1 = JSONObject.fromObject(jsonobject);
            Object datalist = jsonObject1.get("datalist");
            JSONArray tabledata = JSONArray.fromObject(datalist);


            HSSFWorkbook sheet1 = ExcelUtil.exportExcel("sheet1", headers, headersField, tabledata, "");
            byte[] bytesForWorkBook = ExcelUtil.getBytesForWorkBook(sheet1);
            ExcelUtil.downLoadExcel("企业用电量统计", response, request, bytesForWorkBook);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/16 0016 下午 4:18
     * @Description: 通过多参数统计监测点异常数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, fkpollutionid]
     * @throws:
     */
    @RequestMapping(value = "countMonitorPointAlarmRanking", method = RequestMethod.POST)
    public Object countMonitorPointAlarmRanking(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime
            , @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.countExceptoinModelDataByParamMap(paramMap);

            for (Document document : documents) {
                String dgimn = document.get("_id") == null ? "" : document.remove("_id").toString();
                String MonitorPointName = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("MonitorPointName") != null && dgimn.equals(m.get("dgimn").toString())).map(m -> m.get("MonitorPointName").toString())
                        .findFirst().orElse("");
                document.put("dgimn", dgimn);
                document.put("MonitorPointName", MonitorPointName);
            }

            return AuthUtil.parseJsonKeyToLower("success", documents.stream().filter(m -> m.get("count") != null).sorted(Comparator.comparing(m -> m.get("count").toString())).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/16 0016 下午 6:55
     * @Description: 通过监测时间获取企业发生异常时间及次数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime]
     * @throws:
     */
    @RequestMapping(value = "getPollutionAlarmTimeByParams", method = RequestMethod.POST)
    public Object getPollutionAlarmTimeByParams(@RequestJson(value = "starttime") String starttime, @RequestJson(value = "endtime") String endtime
            , @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            List<String> datalist = new ArrayList<>();
            List<Map<String, Object>> resultlist = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricFacilityAndDGIMNByParamMap(paramMap);
            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);
            for (Document document : documents) {
                String time = document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "yyyy-MM-dd HH");
                datalist.add(time);
            }
            Map<String, Long> collect = datalist.stream().collect(Collectors.groupingBy(m -> m, Collectors.counting()));
            for (String s : collect.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("monitortime", s);
                map.put("count", collect.get(s));
                resultlist.add(map);
            }

            return AuthUtil.parseJsonKeyToLower("success", resultlist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/23 0023 下午 4:26
     * @Description: 导出企业用电报告
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsjson, response, request]
     * @throws:
     */
    @RequestMapping(value = "ExportPollutionUseElectricityReportByParamMap", method = RequestMethod.POST)
    public void ExportPollutionUseElectricityReportByParamMap(@RequestJson(value = "paramsjson") Object paramsjson, HttpServletResponse response, HttpServletRequest request) throws Exception {
        try {
            Map<String, Object> paramMap = (Map<String, Object>) paramsjson;
            Map<String, Object> resultMap = new HashMap<>();
            String fkpollutionid = paramMap.get("fkpollutionid") == null ? "" : paramMap.get("fkpollutionid").toString();
            String starttime = paramMap.get("starttime") == null ? "" : paramMap.get("starttime").toString();
            String endtime = paramMap.get("endtime") == null ? "" : paramMap.get("endtime").toString();
            if (paramMap.get("pollutionrealdata") != null && paramMap.get("pollutionrealdata").toString().split(",").length > 1) {
                paramMap.put("pollutionrealdata", paramMap.remove("pollutionrealdata").toString().split(",")[1]);
            }
            if (paramMap.get("everyuseelectricfacility") != null && paramMap.get("everyuseelectricfacility").toString().split(",").length > 1) {
                paramMap.put("everyuseelectricfacility", paramMap.remove("everyuseelectricfacility").toString().split(",")[1]);
            }
            if (paramMap.get("pollutionanduseelectricfacility") != null && paramMap.get("pollutionanduseelectricfacility").toString().split(",").length > 1) {
                paramMap.put("pollutionanduseelectricfacility", paramMap.remove("pollutionanduseelectricfacility").toString().split(",")[1]);
            }
            if (paramMap.get("pollutionalarm") != null && paramMap.get("pollutionalarm").toString().split(",").length > 1) {
                paramMap.put("pollutionalarm", paramMap.remove("pollutionalarm").toString().split(",")[1]);
            }

            //企业基本信息
            Object pollutioninfo = countPollutionAndUseElectricFacilityByParams(1, 1, fkpollutionid);
            JSONObject jsonObject = JSONObject.fromObject(pollutioninfo);
            Object data = jsonObject.get("data");
            Object datalist = JSONObject.fromObject(data).get("datalist");
            JSONObject pollutiondata = JSONObject.fromObject(JSONArray.fromObject(datalist).stream().findFirst().orElse(new Object()));


            //用电明细信息
            Object day = countFacilityPowerUsageData(fkpollutionid, null, starttime, endtime, "day", null);
            JSONObject jsonObject1 = JSONObject.fromObject(day);
            Object data1 = jsonObject1.get("data");
            Object tabledata = JSONObject.fromObject(data1).get("tabledata");
            JSONArray jsonArray = JSONArray.fromObject(tabledata);

            //异常统计信息
            Object UseElectricFacility = countPollutionAndUseElectricFacilityByParams(Integer.MAX_VALUE, 1, fkpollutionid);
            JSONObject jsonObject2 = JSONObject.fromObject(UseElectricFacility);
            Object data2 = jsonObject2.get("data");
            Object datalist1 = JSONObject.fromObject(data2).get("datalist");
            JSONObject useelectricfacilitydata = JSONObject.fromObject(JSONArray.fromObject(datalist1).stream().findFirst().orElse(new Object()));

            //异常监测点排名信息
            Object MonitorPointAlarmRanking = countMonitorPointAlarmRanking(starttime, endtime, fkpollutionid);
            JSONObject jsonObject3 = JSONObject.fromObject(MonitorPointAlarmRanking);
            Object data3 = jsonObject3.get("data");
            JSONArray jsonArray2 = JSONArray.fromObject(data3);
            List<Map<String, Object>> monitorpointalarmrankingdata = new ArrayList<>();
            for (int i = 0; i < jsonArray2.size(); i++) {
                Map<String, Object> rankingdata = (Map<String, Object>) jsonArray2.get(i);
                rankingdata.put("orderindex", i + 1);
                monitorpointalarmrankingdata.add(rankingdata);
            }

            resultMap.put("pollutiondata", pollutiondata);
            resultMap.put("useelectricfacilitydata", useelectricfacilitydata);
            resultMap.put("monitorpointalarmranking", monitorpointalarmrankingdata);
            resultMap.put("hourdata", fillData(jsonArray));
            resultMap.putAll(paramMap);
            byte[] fileBytes = FreeMarkerWordUtil.createWord(resultMap, "templates/企业用电报告模板.ftl");
            ExcelUtil.downLoadFile("企业用电报告.doc", response, request, fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/11 0011 上午 9:03
     * @Description: 获取排口用电在线数据及用电报警信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, fkpollutionid, outputtype, outputid]
     * @throws:
     */
    @RequestMapping(value = "getOutPutOnlineDataAndUseElectricAlarmByParams", method = RequestMethod.POST)
    public Object getOutPutOnlineDataAndUseElectricAlarmByParams(@RequestJson(value = "starttime") String starttime,
                                                                 @RequestJson(value = "endtime") String endtime,
                                                                 @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                                 @RequestJson(value = "outputtype", required = false) String outputtype,
                                                                 @RequestJson(value = "outputid", required = false) String outputid) {
        try {
            String pollutantcode = powerUsageCode.getCode();
            Map<String, Object> paramMap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("outputtype", outputtype);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricAndOutputByParamMap(paramMap);

            Map<String, String> useElectricMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("MonitorPointName") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("MonitorPointName").toString()));


            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));

            //用电异常数据
            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);
            Map<String, List<Document>> alarmmap = documents.stream().filter(m -> m.get("FirstExceptionTime") != null && m.get("LastExceptionTime") != null && m.get("MN") != null).peek(document -> {
                document.put("FirstExceptionTime", document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "yyyy-MM-dd HH:mm:ss"));
                document.put("LastExceptionTime", document.get("LastExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("LastExceptionTime").toString(), "yyyy-MM-dd HH:mm:ss"));
            }).collect(Collectors.groupingBy(m -> m.get("MN").toString()));


            paramMap.put("mns", dgimns);
            paramMap.put("collection", db_RealTimeData);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getStartTime(endtime));
            //查询小时在线数据
            List<Document> monitorDataByParamMap = onlineService.getMonitorDataByParamMap(paramMap);
            Map<String, List<Document>> collect = monitorDataByParamMap.stream().filter(m -> m.get("MonitorTime") != null && m.get("DataGatherCode") != null).peek(document -> document.put("MonitorTime", document.get("MonitorTime") == null ? "" :
                    FormatUtils.formatCSTString(document.get("MonitorTime").toString(), "yyyy-MM-dd HH:mm:ss"))).collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));

            for (String dgimn : collect.keySet()) {
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> reallist = new ArrayList<>();
                List<Map<String, Object>> alarmlist = new ArrayList<>();
                List<Document> documentList = collect.get(dgimn);
                //实时数据
                for (Document document : documentList) {
                    Map<String, Object> monitordata = new HashMap<>();
                    String monitorTime = document.get("MonitorTime").toString();
                    List<Map<String, Object>> realDataList = (List<Map<String, Object>>) document.get("RealDataList");
                    String monitorvalue = realDataList.stream().filter(m -> m.get("PollutantCode") != null && m.get("MonitorValue") != null && pollutantcode.equals(m.get("PollutantCode").toString())).map(m -> m.get("MonitorValue").toString()).findFirst().orElse("");
                    monitordata.put("monitortime", monitorTime);
                    monitordata.put("monitorvalue", monitorvalue);
                    reallist.add(monitordata);
                }
                //异常数据
                List<Document> alarm = alarmmap.get(dgimn);
                for (Document document : alarm) {
                    Map<String, Object> alarmdata = new HashMap<>();
                    alarmdata.put("firstExceptionTime", document.get("FirstExceptionTime"));
                    alarmdata.put("lastExceptionTime", document.get("LastExceptionTime"));
                    alarmdata.put("MonitorValue", document.get("MonitorValue"));
                    alarmlist.add(alarmdata);
                }

                data.put("dgimn", dgimn);
                data.put("useElectricMonitorName", useElectricMap.get(dgimn));
                data.put("realdata", reallist);
                data.put("alarmdata", alarmlist);
                datalist.add(data);
            }
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/9/12 0012 下午 5:25
     * @Description: 通过多参数获取排口下用电监测点异常但实时数据却有流量问题列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [starttime, endtime, fkpollutionid, outputtype, outputid, pagesize, pagenum]
     * @throws:
     */
    @RequestMapping(value = "getOutPutUseElectricAlarmListByParams", method = RequestMethod.POST)
    public Object getOutPutUseElectricAlarmListByParams(@RequestJson(value = "starttime") String starttime,
                                                        @RequestJson(value = "endtime") String endtime,
                                                        @RequestJson(value = "fkpollutionid", required = false) String fkpollutionid,
                                                        @RequestJson(value = "outputtype", required = false) String outputtype,
                                                        @RequestJson(value = "outputid", required = false) String outputid,
                                                        @RequestJson(value = "pollutionname", required = false) String pollutionname,
                                                        @RequestJson(value = "outputname", required = false) String outputname,
                                                        @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                        @RequestJson(value = "pagenum", required = false) Integer pagenum)throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, Object>> datalist = new ArrayList<>();
            paramMap.put("fkpollutionid", fkpollutionid);
            paramMap.put("pollutionname", pollutionname);
            paramMap.put("outputname", outputname);
            paramMap.put("outputtype", outputtype);
            paramMap.put("outputid", outputid);
            List<Map<String, Object>> useElectricFacility = useElectricFacilityService.getUseElectricAndOutputByParamMap(paramMap);


            //用电监测点mn号和用电监测点名称
            Map<String, String> useElectricMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("MonitorPointName") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("MonitorPointName").toString()));
            Map<String, String> outputMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("outputname") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("outputname").toString()));
            Map<String, String> pollutionnameMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("pollutionname") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("pollutionname").toString()));
            Map<String, String> outputidMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("outputid") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("outputid").toString()));
            Map<String, String> outputtypeMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("outputtype") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("outputtype").toString()));
            //用电dgimn和排口dgimn集合
            Map<String, String> outputdgimnMap = useElectricFacility.stream().filter(m -> m.get("dgimn") != null && m.get("outputdgimn") != null).collect(Collectors.toMap(m -> m.get("dgimn").toString(), n -> n.get("outputdgimn").toString()));



            List<String> dgimns = useElectricFacility.stream().filter(m -> m.get("dgimn") != null).map(m -> m.get("dgimn").toString()).distinct().collect(Collectors.toList());
            paramMap.put("dgimns", dgimns);
            paramMap.put("starttime", JSONObjectUtil.getStartTime(starttime));
            paramMap.put("endtime", JSONObjectUtil.getEndTime(endtime));
            //用电异常数据
            List<Document> documents = onlineService.getExceptoinModelDataByParamMap(paramMap);
            Map<String, List<Document>> alarmmap = documents.stream().filter(m -> m.get("FirstExceptionTime") != null && m.get("LastExceptionTime") != null && m.get("MN") != null).peek(document -> {
                document.put("FirstExceptionTime", document.get("FirstExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("FirstExceptionTime").toString(), "yyyy-MM-dd HH:mm:ss"));
                document.put("LastExceptionTime", document.get("LastExceptionTime") == null ? "" : FormatUtils.formatCSTString(document.get("LastExceptionTime").toString(), "yyyy-MM-dd HH:mm:ss"));
            }).collect(Collectors.groupingBy(m -> m.get("MN").toString()));

            paramMap.clear();
            paramMap.put("alarmmap",alarmmap);
            paramMap.put("outputdgimnmap",outputdgimnMap);


            List<Document> countData = onlineService.getMonitorPointFlowProblemByParam(paramMap);
            Map<String, List<Document>> collect = countData.stream().filter(m -> m.get("dgimn") != null && m.get("MonitorTime") != null).collect(Collectors.groupingBy(m -> m.get("dgimn").toString() + "_" + m.get("MonitorTime").toString()));
            for (String dgimnAndTime : collect.keySet()) {
                Map<String,Object> data=new HashMap<>();
                String[] split = dgimnAndTime.split("_");
                //用电监测点dgimn
                String dgimn = split[0];
                String monitortime = split[1];
                List<String> timepointlist=new ArrayList<>();
                List<Document> documentList = collect.get(dgimnAndTime);
                for (Document document : documentList) {
                    int count = document.get("count") == null ? 0 : Integer.valueOf(document.get("count").toString());
                    if(count>0){
                        String firstExceptionTime = document.get("FirstExceptionTime") == null ?"" : document.get("FirstExceptionTime").toString();
                        String lastExceptionTime = document.get("LastExceptionTime") == null ?"" : document.get("LastExceptionTime").toString();
                        timepointlist.add(firstExceptionTime.substring(11,16)+"-"+lastExceptionTime.substring(11,16));
                    }
                }
                if(timepointlist.size()>0){
                    data.put("pollutionname",pollutionnameMap.get(dgimn));
                    data.put("outputname",outputMap.get(dgimn));
                    data.put("outputid",outputidMap.get(dgimn));
                    data.put("useElectricMonitorName",useElectricMap.get(dgimn));
                    data.put("outputtype",outputtypeMap.get(dgimn));
                    data.put("monitortime",monitortime);
                    data.put("timepoint",timepointlist.stream().distinct().collect(Collectors.joining("、")));
                    datalist.add(data);
                }
            }
            int total = datalist.size();
            if(pagesize!=null &&  pagenum!=null){
                datalist=datalist.stream().skip((pagenum-1)*pagesize).limit(pagesize).collect(Collectors.toList());
            }
            resultMap.put("total",total);
            resultMap.put("datalist",datalist);

            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2020/7/20 0020 上午 10:21
     * @Description: 将不足24小时的数据填充为满24小时，数据值为0
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonArray]
     * @throws:
     */
    private List<Map<String, Object>> fillData(JSONArray jsonArray) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("00时", "zero");
        map.put("01时", "one");
        map.put("02时", "two");
        map.put("03时", "three");
        map.put("04时", "four");
        map.put("05时", "five");
        map.put("06时", "six");
        map.put("07时", "seven");
        map.put("08时", "eight");
        map.put("09时", "nine");
        map.put("10时", "ten");
        map.put("11时", "eleven");
        map.put("12时", "twelve");
        map.put("13时", "thirteen");
        map.put("14时", "fourteen");
        map.put("15时", "fifteen");
        map.put("16时", "sixteen");
        map.put("17时", "seventeen");
        map.put("18时", "eighteen");
        map.put("19时", "nineteen");
        map.put("20时", "twenty");
        map.put("21时", "twentyone");
        map.put("22时", "twentytwo");
        map.put("23时", "twentythree");


        List<String> times = Arrays.asList("00时", "01时", "02时", "03时", "04时", "05时", "06时", "07时", "08时", "09时", "10时", "11时", "12时",
                "13时", "14时", "15时", "16时", "17时", "18时", "19时", "20时", "21时", "22时", "23时");
        for (Object o : jsonArray) {
            List<String> timestemp = new ArrayList<>();
            Map<String, Object> data = new HashMap<>();
            JSONObject jsonObject = JSONObject.fromObject(o);
            data.putAll(jsonObject);
            Set set = jsonObject.keySet();
            times.stream().forEach(m -> {
                if (!set.contains(m)) {
                    timestemp.add(m);
                }
            });
            for (String time : timestemp) {
                data.put(time, 0);
            }
            result.add(data);
        }
        for (Map<String, Object> stringObjectMap : result) {
            for (String time : times) {
                stringObjectMap.put(map.get(time), stringObjectMap.remove(time));
            }
        }
        return result;
    }


    /**
     * @author: lip
     * @date: 2020/6/22 0022 下午 6:45
     * @Description: 设置监测图表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private List<Map<String, Object>> setMonitorCharData(String monitortime, String pollutantcode, List<Map<String, Object>> pointList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        if (pointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String mnCommon;
            for (Map<String, Object> point : pointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndId.put(mnCommon, point.get("monitorpointid").toString());
                mnAndName.put(mnCommon, point.get("monitorpointname").toString());
            }
            mns = mns.stream().distinct().collect(Collectors.toList());
            paramMap.put("mns", mns);
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", db_hourData);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            resultList = MongoDataUtils.setManyOutPutOnePollutantCharDataList(documents, pollutantcode, db_hourData, mnAndId, mnAndName);
        }
        return resultList;
    }

    /**
     * @author: lip
     * @date: 2020/6/22 0022 下午 6:45
     * @Description: 设置监测列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    private Map<String, Object> setMonitorListData(String monitortime,
                                                   String pollutantcode,
                                                   List<Map<String, Object>> pointList,
                                                   Integer pagesize,
                                                   Integer pagenum) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        if (pointList.size() > 0) {
            List<String> mns = new ArrayList<>();
            Map<String, Object> mnAndId = new HashMap<>();
            Map<String, Object> mnAndName = new HashMap<>();
            String mnCommon;
            for (Map<String, Object> point : pointList) {
                mnCommon = point.get("dgimn").toString();
                mns.add(mnCommon);
                mnAndId.put(mnCommon, point.get("monitorpointid").toString());
                mnAndName.put(mnCommon, point.get("monitorpointname").toString());
            }
            mns = mns.stream().distinct().collect(Collectors.toList());
            paramMap.put("mns", mns);
            paramMap.put("starttime", monitortime + " 00:00:00");
            paramMap.put("endtime", monitortime + " 23:59:59");
            paramMap.put("pollutantcodes", Arrays.asList(pollutantcode));
            paramMap.put("collection", db_hourData);
            List<Document> documents = onlineService.getMonitorDataByParamMap(paramMap);
            List<Map<String, Object>> dataList = MongoDataUtils.setManyOutPutOnePollutantListDataList(documents, pollutantcode, db_hourData, mnAndId, mnAndName);
            if (dataList.size() > 0) { //排序分页
                dataList = dataList.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitortime").toString()).reversed()).collect(Collectors.toList());
                if (pagesize != null && pagenum != null) {
                    resultMap.put("total", dataList.size());
                    dataList = MongoDataUtils.getPageData(dataList, pagenum, pagesize);
                    resultMap.put("datalist", dataList);
                } else {
                    resultMap.put("datalist", dataList);
                }
            }
        }
        return resultMap;
    }


}
