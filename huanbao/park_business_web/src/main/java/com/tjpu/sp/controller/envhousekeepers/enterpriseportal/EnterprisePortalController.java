package com.tjpu.sp.controller.envhousekeepers.enterpriseportal;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.controller.common.RabbitmqController;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.pubcode.PollutantService;
import com.tjpu.sp.service.envhousekeepers.checkentinfo.CheckEntInfoService;
import com.tjpu.sp.service.envhousekeepers.enterpriseportal.EnterprisePortalService;
import com.tjpu.sp.service.envhousekeepers.problemconsult.EntProblemConsultRecordService;
import com.tjpu.sp.service.environmentalprotection.notice.NoticeService;
import com.tjpu.sp.service.environmentalprotection.online.OnlineService;
import com.tjpu.sp.service.environmentalprotection.pointofflinerecord.PointOffLineRecordService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.MonitorPointTypeEnum.*;
import static com.tjpu.sp.common.enumconfig.CommonTypeEnum.RemindTypeEnum.*;


/**
 * @author: xsm
 * @date: 2021/08/14 0014 下午 14:46
 * @Description: 企业门户控制层
 */
@RestController
@RequestMapping("enterprisePortal")
public class EnterprisePortalController {

    @Autowired
    private EnterprisePortalService enterprisePortalService;
    @Autowired
    private OnlineService onlineService;
    @Autowired
    private PollutantService pollutantService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private EntProblemConsultRecordService entProblemConsultRecordService;
    @Autowired
    private CheckEntInfoService checkEntInfoService;
    @Autowired
    private PointOffLineRecordService pointOffLineRecordService;
    @Autowired
    private PollutionService pollutionService;
    @Autowired
    private RabbitmqController rabbitmqController;




    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID获取该企业的企业档案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEnterpriseArchivesDataByPollutionID", method = RequestMethod.POST)
    public Object getEnterpriseArchivesDataByPollutionID(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            List<Map<String, Object>> maps = enterprisePortalService.getEnterpriseArchivesDataByPollutionID(pollutionid);
            long count = maps.stream().filter(m -> m.get("count") != null && m.get("iscalculat") != null && "yes".equals(m.get("iscalculat").toString()) && Integer.valueOf(m.get("count").toString()) > 0).count();
            //计算资料完善程度
            String format = decimalFormat.format(Double.valueOf(count) / Double.valueOf(maps.size()) * 100);
            resultMap.clear();
            resultMap.put("complatedatarate", format);
            resultMap.put("datalist", maps);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID获取该企业的最新动态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEntNewDynamicDataByPollutionID", method = RequestMethod.POST)
    public Object getEntNewDynamicDataByPollutionID(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            param.put("userid",userid);
            param.put("pollutionid",pollutionid);
            List<Map<String, Object>> maps = enterprisePortalService.getEntNewDynamicDataByPollutionID(param);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 2:13
     * @Description: 根据企业ID获取该企业的最新台账记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @RequestMapping(value = "getEntNewStandingBookDataByPollutionID", method = RequestMethod.POST)
    public Object getEntNewStandingBookDataByPollutionID(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String,Object> param = new HashMap<>();
            param.put("pollutionid",pollutionid);
            List<Map<String, Object>> maps = enterprisePortalService.getEntNewStandingBookDataByPollutionID(param);
            return AuthUtil.parseJsonKeyToLower("success", maps);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    /**
     * @author: xsm
     * @date: 2021/08/24 9:24
     * @Description: 统计企业门户单个企业报警数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "countOnePollutionAllAlarmData", method = RequestMethod.POST)
    public Object countOnePollutionAllAlarmData(
            @RequestJson(value = "pollutionid") String pollutionid,
            @RequestJson(value = "daydate", required = false) String daydate) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String,Object>> datalist = new ArrayList<>();
            if (StringUtils.isBlank(daydate)) {
                daydate = DataFormatUtil.getDateYMD(new Date());
            }
            String startTime = daydate + " 00:00:00";
            String endTime = daydate + " 23:59:59";
            //判断配置文件是查询环保或安全数据
            //List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
            Map<String, Object> mnData = getEntMnDataByParam(pollutionid,"");
            datalist = setOneEntAlarmData(mnData,startTime,endTime);
            //按报警时间排序
            datalist = datalist.stream().sorted(Comparator.comparing(m -> ((Map) m).get("lasttime").toString()).reversed()).collect(Collectors.toList());
            return AuthUtil.parseJsonKeyToLower("success", datalist);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: xsm
     * @date: 2021/04/19 0019 上午 10:20
     * @Description: 获取企业下点位MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [type]
     * @throws:
     */
    private Map<String, Object> getEntMnDataByParam(String pollutionid,String monitorpointname) {
        List<String> dgimns = RedisTemplateUtil.getRedisCacheDataByToken("userdgimns", List.class);
        Map<String, Object> paramMap = new HashMap<>();
        List<Integer> types = new ArrayList<>();
        types.add(WasteWaterEnum.getCode());
        types.add(RainEnum.getCode());
        types.add(WasteGasEnum.getCode());
        types.add(SmokeEnum.getCode());
        types.add(FactoryBoundaryStinkEnum.getCode());
        List<String> mns = new ArrayList<>();
        Map<String, Object> mnAndType = new HashMap<>();
        Map<String, Object> mnAndMonitorPointId = new HashMap<>();
        Map<String, Object> mnAndMonitorPointName = new HashMap<>();
        String mnCommon;
        List<Map<String, Object>> pointDataList;
        for (Integer code : types) {
            paramMap.clear();
            paramMap.put("pollutionids", Arrays.asList(pollutionid));
            paramMap.put("pollutionid", pollutionid);
            paramMap.put("monitorpointtypecode", code);
            paramMap.put("outputname", monitorpointname);
            paramMap.put("monitorpointname", monitorpointname);
            pointDataList = onlineService.getMNAndMonitorPointByParam(paramMap);
            if (pointDataList.size() > 0) {
                for (Map<String, Object> pointData : pointDataList) {
                    if (pointData.get("DGIMN") != null||pointData.get("dgimn") != null) {
                        mnCommon =  pointData.get("DGIMN")!=null?pointData.get("DGIMN").toString():(pointData.get("dgimn")!=null?pointData.get("dgimn").toString():"");
                        if (dgimns != null && dgimns.contains(mnCommon)) {
                            mns.add(mnCommon);
                            mnAndMonitorPointId.put(mnCommon, pointData.get("monitorpointid"));
                            mnAndMonitorPointName.put(mnCommon, pointData.get("monitorpointname"));
                            mnAndType.put(mnCommon, code);
                        }
                    }
                }
            }
        }
        paramMap.clear();
        paramMap.put("mns", mns);
        paramMap.put("mnAndType", mnAndType);
        paramMap.put("mnAndMonitorPointId", mnAndMonitorPointId);
        paramMap.put("mnAndMonitorPointName", mnAndMonitorPointName);
        return paramMap;
    }

    private List<Map<String,Object>> setOneEntAlarmData(Map<String, Object> mnData, String startTime, String endTime) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (mnData.size() > 0) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("mns", mnData.get("mns"));
            paramMap.put("starttime", startTime);
            paramMap.put("endtime", endTime);
            List<Map<String, Object>> pollutantData = pollutantService.getPollutantsByCodesAndType(paramMap);
            Map<String, Object> codeAndName = new HashMap<>();
            Map<String, Object> codeAndUnit = new HashMap<>();
            if (pollutantData.size() > 0) {
                String key;
                for (Map<String, Object> pollutant : pollutantData) {
                    key = pollutant.get("code").toString() + "#" + pollutant.get("pollutanttype");
                    if (pollutant.get("name") != null) {
                        codeAndName.put(key, pollutant.get("name"));
                    }
                    if (pollutant.get("unit") != null) {
                        codeAndUnit.put(key, pollutant.get("unit"));
                    }
                }
            }
            Map<String, Map<String, Object>> mnAndPollutantDataList;
            Map<String, Object> mnAndType = (Map<String, Object>) mnData.get("mnAndType");
            Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) mnData.get("mnAndMonitorPointId");
            Map<String, Object> mnAndMonitorPointName = (Map<String, Object>) mnData.get("mnAndMonitorPointName");
            List<Integer> remindcodes = new ArrayList<>();
            remindcodes.add(ExceptionAlarmEnum.getCode());
            remindcodes.add(ConcentrationChangeEnum.getCode());
            remindcodes.add(EarlyAlarmEnum.getCode());
            remindcodes.add(OverAlarmEnum.getCode());
            for (Integer code : remindcodes) {
                mnAndPollutantDataList = getAllAlarmMonitorDataByParam(paramMap, code, mnData, codeAndName, mnAndType);
                if (mnAndPollutantDataList!=null){
                    Map<String,Object> onemap = new HashMap<>();
                    for(String onemn:mnAndPollutantDataList.keySet()){
                        onemap = mnAndPollutantDataList.get(onemn);
                        onemap.put("monitorpointid", mnAndMonitorPointId.get(onemn));
                        onemap.put("monitorpointtypecode", mnAndType.get(onemn));
                        onemap.put("dgimn", onemn);
                        onemap.put("monitorpointname", mnAndMonitorPointName.get(onemn));
                        dataList.add(onemap);
                    }
                }
            }
        }
        return dataList;
    }


    private Map<String, Map<String, Object>> getAllAlarmMonitorDataByParam(Map<String, Object> paramMap, Integer remindCode, Map<String, Object> mnData, Map<String, Object> codeAndName,  Map<String, Object> mnAndType) {
        Map<String, Object> mnAndMonitorPointId = (Map<String, Object>) mnData.get("mnAndMonitorPointId");
        Map<String, Map<String, Object>> mnAndPollutantDataList = new HashMap<>();
        List<Map<String, Object>> pollutantDataList;
        Map<String, Object> paramMapTemp = new HashMap<>();
        List<String> pollutantcodes = new ArrayList<>();
        List<Document> documents;
        List<Document> pollutantList;
        String mnCommon;
        String pollutantcode;
        Date monitorTime;
        Integer typeKey;
        Map<String,Object> mn_alarmstr;
        switch (CommonTypeEnum.RemindTypeEnum.getObjectByCode(remindCode)) {
            case ConcentrationChangeEnum:
                //1，浓度突变
                paramMap.put("collection", "SuddenRiseData");
                paramMap.put("monitortimekey", "ChangeTime");
                documents = onlineService.getChangeAlarmDataByParamMap(paramMap);
                if (documents != null && documents.size() > 0) {
                    for (Document document : documents) {
                        Map<String,Object> objdata = new HashMap<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        pollutantList = document.get("pollutantList", List.class);
                        Map<String,String> code_time = new HashMap<>();
                        List<String> pollutantstrs = new ArrayList<>();
                        if (pollutantList!=null) {
                            for (Document pollutant : pollutantList) {
                                pollutantcode = pollutant.getString("PollutantCode");
                                String hm = DataFormatUtil.getDateHM(pollutant.getDate("ChangeTime"));
                                if (code_time.get(pollutantcode) != null) {
                                    code_time.put(pollutantcode, code_time.get(pollutantcode) + "、" + hm);
                                } else {
                                    code_time.put(pollutantcode, hm);
                                }
                            }
                        }
                        if (code_time!=null&&code_time.size()>0){
                            for(String pocode:code_time.keySet()){
                                String str = codeAndName.get(pocode+"#" + typeKey)+"浓度突变"+code_time.get(pocode)+"；";
                                pollutantstrs.add(str);
                            }
                        }
                        objdata.put("alarmstr",pollutantstrs);
                        objdata.put("remindcode",ConcentrationChangeEnum.getCode());
                        objdata.put("remindname",ConcentrationChangeEnum.getName());
                        objdata.put("lasttime",DataFormatUtil.getDateHMS(monitorTime));
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                }
                break;
            case EarlyAlarmEnum:
                //2，超阈值
                paramMap.put("collection", "EarlyWarnData");
                paramMap.put("monitortimekey", "EarlyWarnTime");
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                mn_alarmstr =onlineService.setIntegrationAlarmData(remindCode,paramMap);
                if (documents != null && documents.size() > 0) {
                    pollutantcodes.clear();
                    Map<String,Object> objdata;
                    for (Document document : documents) {
                        objdata = new HashMap<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        List<String> strs = new ArrayList<>();
                        if (mn_alarmstr.get(mnCommon)!=null){
                            Map<String,Object> strmap  = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                            for(String pocode:strmap.keySet()){
                                strs.add(codeAndName.get(pocode+ "#" + typeKey)+""+strmap.get(pocode));
                            }
                        }
                        pollutantList = document.get("pollutantList", List.class);
                        Map<String,Object> hourmap = new HashMap<>();
                        //通过mn号分组数据
                        Map<String, List<Document>> mapDocuments = pollutantList.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                        for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                            String onepollutancode = entry.getKey();
                            List<Document> onelist = entry.getValue();
                            String houralarmstr = "";//小时超标时段
                            String newhour ="";
                            int newhournum = 0;
                            String str1 = "";
                            for (Document pollutant : onelist) {
                                String datatype = pollutant.getString("DataType");
                                if ("HourData".equals(datatype)) {
                                    int hour = DataFormatUtil.getDateHourNum(pollutant.getDate("MonitorTime"));
                                    str1 = DataFormatUtil.getDateHM(pollutant.getDate("MonitorTime"));
                                    if ("".equals(newhour)) {
                                        newhour = hour + "";
                                        newhournum += 1;
                                        houralarmstr = str1 + "、";
                                    } else {
                                        if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                                            //连续
                                            newhour = hour + "";
                                            newhournum += 1;
                                        } else {
                                            houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                                            if (newhournum>1){
                                                if (Integer.parseInt(newhour)>9){
                                                    houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】、"+ str1+"、";
                                                }else {
                                                    houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                                }
                                            }else{
                                                houralarmstr = houralarmstr + "、"+str1+"、";
                                            }
                                            newhour = hour + "";
                                            newhournum = 1;
                                        }
                                    }
                                }
                            }
                            if (!"".equals(houralarmstr)) {
                                houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                            }
                            if (newhournum>1){
                                if (Integer.parseInt(newhour)>9){
                                    houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】";
                                }else {
                                    houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】";
                                }
                            }
                            hourmap.put(onepollutancode,houralarmstr);
                        }
                        if (mn_alarmstr.get(mnCommon)!=null){
                            Map<String,Object> strmap  = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                            for(String pocode:strmap.keySet()){
                                strs.add(codeAndName.get(pocode+ "#" + typeKey)+""+strmap.get(pocode));
                            }
                        }else{
                            for (String pocode:hourmap.keySet()){
                                strs.add(codeAndName.get(pocode+ "#" + typeKey)+"超限"+hourmap.get(pocode));
                            }
                        }
                        objdata.put("alarmstr",strs);
                        objdata.put("remindcode",EarlyAlarmEnum.getCode());
                        objdata.put("remindname",EarlyAlarmEnum.getName());
                        objdata.put("lasttime",DataFormatUtil.getDateHMS(monitorTime));
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                    if (mnAndPollutantDataList.size() > 0) {
                        List<String> outputids = new ArrayList<>();
                        for (String mnIndex : mnAndPollutantDataList.keySet()) {
                            if (mnAndMonitorPointId.containsKey(mnIndex)) {
                                outputids.add(mnAndMonitorPointId.get(mnIndex).toString());
                            }
                        }
                        paramMapTemp.clear();
                        paramMapTemp.put("outputids", outputids);
                        paramMapTemp.put("pollutantcodes", pollutantcodes);
                        List<Map<String, Object>> earlyDataList = pollutantService.getEarlyValueByParams(paramMapTemp);
                        if (earlyDataList.size() > 0) {
                            Map<String, Object> codeAndValue = new HashMap<>();
                            for (Map<String, Object> earlyData : earlyDataList) {
                                codeAndValue.put(earlyData.get("outputid") + "#" + earlyData.get("pollutantcode"), earlyData.get("concenalarmmaxvalue"));
                            }
                            String codeKey;
                            for (String mnIndex : mnAndPollutantDataList.keySet()) {
                                objdata = mnAndPollutantDataList.get(mnIndex);
                                pollutantDataList = (List<Map<String, Object>>) objdata.get("pollutantDataList");
                                if (pollutantDataList!=null) {
                                    for (Map<String, Object> pollutantData : pollutantDataList) {
                                        codeKey = mnAndMonitorPointId.get(mnIndex) + "#" + pollutantData.get("pollutantcode");
                                        pollutantData.put("earlyvalue", codeAndValue.get(codeKey));
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case OverAlarmEnum:
                //3，数据超限
                paramMap.put("collection", "OverData");
                paramMap.put("monitortimekey", "OverTime");
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                mn_alarmstr =onlineService.setIntegrationAlarmData(remindCode,paramMap);
                if (documents != null && documents.size() > 0) {
                    pollutantcodes.clear();
                    Map<String,Object> objdata;
                    for (Document document : documents) {
                        objdata = new HashMap<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        pollutantList = document.get("pollutantList", List.class);
                        //获取标准值
                        paramMapTemp.clear();
                        paramMapTemp.put("monitorpointtype", mnAndType.get(mnCommon));
                        paramMapTemp.put("monitorpointid", mnAndMonitorPointId.get(mnCommon));
                        paramMapTemp.put("dgimns", Arrays.asList(mnCommon));
                        //List<Map<String, Object>> standValueslist = pollutantService.getEarlyAndStandardValueByParams(paramMapTemp);
                        Map<String,Object> hourmap = new HashMap<>();
                        //通过mn号分组数据
                        Map<String, List<Document>> mapDocuments = pollutantList.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCode").toString()));
                        for (Map.Entry<String, List<Document>> entry : mapDocuments.entrySet()) {
                            String onepollutancode = entry.getKey();
                            List<Document> onelist = entry.getValue();
                            String houralarmstr = "";//小时超标时段
                            String newhour ="";
                            int newhournum = 0;
                            String str1 = "";
                            for (Document pollutant : onelist) {
                                String datatype = pollutant.getString("DataType");
                                if ("HourData".equals(datatype)) {
                                    int hour = DataFormatUtil.getDateHourNum(pollutant.getDate("MonitorTime"));
                                    str1 = DataFormatUtil.getDateHM(pollutant.getDate("MonitorTime"));
                                    if ("".equals(newhour)) {
                                        newhour = hour + "";
                                        newhournum += 1;
                                        houralarmstr = str1 + "、";
                                    } else {
                                        if (newhour.equals(String.valueOf(hour - 1))) {//和前一个时间是否连续
                                            //连续
                                            newhour = hour + "";
                                            newhournum += 1;
                                        } else {
                                            houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                                            if (newhournum>1){
                                                if (Integer.parseInt(newhour)>9){
                                                    houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】、"+ str1+"、";
                                                }else {
                                                    houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】、" + str1 + "、";
                                                }
                                            }else{
                                                houralarmstr = houralarmstr + "、"+str1+"、";
                                            }
                                            newhour = hour + "";
                                            newhournum = 1;
                                        }
                                    }
                                }
                            }
                            if (!"".equals(houralarmstr)) {
                                houralarmstr = houralarmstr.substring(0, houralarmstr.length() - 1);
                            }
                            if (newhournum>1){
                                if (Integer.parseInt(newhour)>9){
                                    houralarmstr = houralarmstr + "-" + newhour+":00" + "【" + newhournum + "小时】";
                                }else {
                                    houralarmstr = houralarmstr + "-" + "0"+newhour+":00" + "【" + newhournum + "小时】";
                                }
                            }
                            hourmap.put(onepollutancode,houralarmstr);
                        }
                        List<String> strs = new ArrayList<>();
                        if (mn_alarmstr.get(mnCommon)!=null){
                            Map<String,Object> strmap  = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                            for(String pocode:strmap.keySet()){
                                strs.add(codeAndName.get(pocode+ "#" + typeKey)+""+strmap.get(pocode));
                            }
                        }else{
                            for (String pocode:hourmap.keySet()){
                                strs.add(codeAndName.get(pocode+ "#" + typeKey)+"超限"+hourmap.get(pocode));
                            }
                        }
                        objdata.put("alarmstr",strs);
                        objdata.put("remindcode",OverAlarmEnum.getCode());
                        objdata.put("remindname",OverAlarmEnum.getName());
                        objdata.put("lasttime",DataFormatUtil.getDateHMS(monitorTime));
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                }
                break;
            case ExceptionAlarmEnum:
                //4，数据异常
                String exceptionType;
                paramMap.put("collection", "ExceptionData");
                paramMap.put("monitortimekey", "ExceptionTime");
                paramMap.put("exceptiontype", "-1");
                documents = onlineService.getLastEarlyOrOverOrExceptionDataByParamMap(paramMap);
                mn_alarmstr =onlineService.setIntegrationAlarmData(remindCode,paramMap);
                if (documents != null && documents.size() > 0) {
                    Map<String,Object> objdata;
                    for (Document document : documents) {
                        objdata = new HashMap<>();
                        mnCommon = document.getString("_id");
                        typeKey = mnAndType.get(mnCommon) != null ? Integer.parseInt(mnAndType.get(mnCommon).toString()) : 0;
                        monitorTime = document.getDate("lasttime");
                        List<String> strs = new ArrayList<>();
                        if (mn_alarmstr.get(mnCommon)!=null){
                            Map<String,Object> strmap  = (Map<String, Object>) mn_alarmstr.get(mnCommon);
                                for (String pocode : strmap.keySet()) {
                                    strs.add(codeAndName.get(pocode + "#" + typeKey) + "" + strmap.get(pocode));
                                }
                        }
                        objdata.put("alarmstr",strs);
                        objdata.put("remindcode",ExceptionAlarmEnum.getCode());
                        objdata.put("remindname",ExceptionAlarmEnum.getName());
                        objdata.put("lasttime",DataFormatUtil.getDateHMS(monitorTime));
                        mnAndPollutantDataList.put(mnCommon, objdata);
                    }
                }
                break;
        }
        return mnAndPollutantDataList;
    }


    /**
     * @author: xsm
     * @date: 2021/09/01 0001 下午 3:54
     * @Description: 通过自定义条件查询监测点浓度（预警、异常、超限）连续预警数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "getOneEntEarlyOverOrExceptionListDataByParams", method = RequestMethod.POST)
    public Object getOneEntEarlyOverOrExceptionListDataByParams(
                                                          @RequestJson(value = "monitorpointname", required = false) String monitorpointname,
                                                          @RequestJson(value = "pollutionid", required = false) String pollutionid,
                                                          @RequestJson(value = "remindtype") Integer remindtype,
                                                          @RequestJson(value = "starttime") String starttime,
                                                          @RequestJson(value = "endtime") String endtime,
                                                          @RequestJson(value = "pagesize", required = false) Integer pagesize,
                                                          @RequestJson(value = "pagenum", required = false) Integer pagenum
    ) throws ParseException {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            paramMap.put("pagenum", pagenum);
            paramMap.put("pagesize", pagesize);
            paramMap.put("remindtype", remindtype);
            List<Integer> types = new ArrayList<>();
            types.add(WasteWaterEnum.getCode());
            types.add(RainEnum.getCode());
            types.add(WasteGasEnum.getCode());
            types.add(SmokeEnum.getCode());
            types.add(FactoryBoundaryStinkEnum.getCode());
            paramMap.put("monitorpointtypes", types);
            Map<String, Object> mnData = getEntMnDataByParam(pollutionid,monitorpointname);
            paramMap.put("dgimns", mnData.get("mns"));
            paramMap.put("mnandtype", mnData.get("mnAndType"));
            paramMap.put("mnandmonitorpointid", mnData.get("mnAndMonitorPointId"));
            paramMap.put("mnandmonitorpointname", mnData.get("mnAndMonitorPointName"));
            if (remindtype == CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode()){
                resultmap = enterprisePortalService.getOneEntConcentrationChangeDataByParams(paramMap);
            }else{
                resultmap = enterprisePortalService.getOneEntEarlyOverOrExceptionListDataByParams(paramMap);
            }
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/01 0001 下午 3:54
     * @Description: 通过自定义条件统计监测点浓度（预警、异常、超限）连续预警数据
     * @updateUser:xsm
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @RequestMapping(value = "countOneEntEarlyOverOrExceptionDataByParams", method = RequestMethod.POST)
    public Object countOneEntEarlyOverOrExceptionDataByParams(
            @RequestJson(value = "pollutionid", required = false) String pollutionid,
            @RequestJson(value = "starttime") String starttime,
            @RequestJson(value = "endtime") String endtime
    ) throws ParseException {
        try {
            Map<String, Object> resultmap = new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("starttime", starttime);
            paramMap.put("endtime", endtime);
            Map<String, Object> mnData = getEntMnDataByParam(pollutionid,"");
            paramMap.put("dgimns", mnData.get("mns"));
            //突变
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.ConcentrationChangeEnum.getCode());
            int tb_count = enterprisePortalService.countOneEntEarlyOverOrExceptionDataByParams(paramMap);
            resultmap.put("tb_count",tb_count);
            //预警
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.EarlyAlarmEnum.getCode());
            int yj_count = enterprisePortalService.countOneEntEarlyOverOrExceptionDataByParams(paramMap);
            resultmap.put("yj_count",yj_count);
            //超标超限
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.OverAlarmEnum.getCode());
            int cb_count = enterprisePortalService.countOneEntEarlyOverOrExceptionDataByParams(paramMap);
            resultmap.put("cb_count",cb_count);
            //异常
            paramMap.put("remindtype", CommonTypeEnum.RemindTypeEnum.ExceptionAlarmEnum.getCode());
            int yc_count = enterprisePortalService.countOneEntEarlyOverOrExceptionDataByParams(paramMap);
            resultmap.put("yc_count",yc_count);
            return AuthUtil.parseJsonKeyToLower("success", resultmap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @author: xsm
     * @date: 2021/09/10 0010 上午 11:25
     * @Description: 获取该企业首页所有未读提醒消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @RequestMapping(value = "countEntNoReadRemindDataByParam", method = RequestMethod.POST)
    public Object countEntNoReadRemindDataByParam(@RequestJson(value = "pollutionid") String pollutionid) throws Exception {
        try {
            Map<String, Object> parammap = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            String userid = RedisTemplateUtil.getRedisCacheDataByToken("userid", String.class);
            parammap.put("pollutionid", pollutionid);
            parammap.put("userid", userid);
            //企业检查反馈信息
            parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.EntCheckFeedbackMessage.getCode());
            List<Map<String, Object>> qyfk_list = checkEntInfoService.getEntCheckFeedbackRecordDataByParam(parammap);
            //企业咨询回复
            parammap.put("messagetype", CommonTypeEnum.HomePageMessageTypeEnum.ProblemReplyMessage.getCode());
            List<Map<String, Object>> zxhf_list = entProblemConsultRecordService.getNoReadProblemConsultRecordByParam(parammap);
            //通知信息
            //List<Map<String, Object>> tzxx_list = noticeService.getNoReadNoticeDataByParam(parammap);
            //点位离线
            parammap.put("isread","0");
            parammap.put("messagetype",CommonTypeEnum.HomePageMessageTypeEnum.PointOffLineMessage.getCode());
            List<Map<String, Object>> offlist = pointOffLineRecordService.getEntPointOffLineRecordsByParamMap(parammap);

            resultMap.put("qyfk_num",qyfk_list.size());
            resultMap.put("zxhf_num",zxhf_list.size());
            //resultMap.put("tzxx_num",tzxx_list.size());
            resultMap.put("dwlx_num",offlist.size());
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2021/09/14 0014 上午 08:47
     * @Description: 获取企业用户信息 并将报警信息推送到首页
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "getEntUserAndPushEntHomePage", method = RequestMethod.POST)
    public void getEntUserAndPushEntHomePage(@RequestJson(value = "paramsjson") Object paramsjson) {
        try {
                JSONObject jsonObject = JSONObject.fromObject(paramsjson);
                //推到企业端首页
                //根据企业ID获取企业关联的企业用户ID
                //List<String> userids = pollutionService.getUserInfoByPollution(jsonObject.get("PollutionID").toString());
                //jsonObject.put("userids", userids);
                //rabbitmqController.sendEntAlarmData(jsonObject, CommonTypeEnum.RabbitMQMessageTypeEnum.AlarmDataMessage.getCode());
            //return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
